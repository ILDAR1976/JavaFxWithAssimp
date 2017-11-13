package edu.lwjgl_fx_01.ui.model.engine.loaders.assimp;

import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_FixInfacingNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GenSmoothNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_LimitBoneWeights;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIMetaDataEntry;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVectorKey;
import org.lwjgl.assimp.AIVertexWeight;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.VertexWeight;
import edu.lwjgl_fx_01.ui.model.engine.shape3d.SkinningMesh;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import edu.lwjgl_fx_01.ui.utils.Utils;
import edu.lwjgl_fx_01.ui.model.engine.SceneFx;
import edu.lwjgl_fx_01.ui.model.engine.shape3d.SkinningMesh;
import edu.lwjgl_fx_01.ui.model.engine.graph.LwjglMaterial;
import edu.lwjgl_fx_01.ui.model.engine.graph.MeshFx;
import edu.lwjgl_fx_01.ui.model.engine.graph.NodeFx;
import edu.lwjgl_fx_01.ui.model.engine.graph.NodeFx;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.Skeleton;
import edu.lwjgl_fx_01.ui.model.engine.graph.animation.AnimationFx;

import static edu.lwjgl_fx_01.ui.utils.Utils.*;
import edu.lwjgl_fx_01.ui.model.engine.graph.animation.AnimatedFrame;
import edu.lwjgl_fx_01.ui.model.engine.graph.animation.Animation;
import edu.lwjgl_fx_01.ui.model.engine.graph.animation.SkinningMeshTimer;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;


@SuppressWarnings({ "unchecked", "rawtypes", "unused", "restriction" })
public class AnimMeshesLoader extends StaticMeshesLoader {
	static float timeQuantum = 1f;
	private static final LinkedList<SceneFx> scenesFx = new LinkedList<>();
	private static final LinkedList<NodeFx> nodesFx = new LinkedList<>();
	private static final Map<String, NodeFx> nodesFxMap = new HashMap();
	private final static Map<String, Timeline> timelines = new HashMap<>();
	private static final LinkedList<NodeFx> jointsFx = new LinkedList<>();
	private static Map<String,JointFx> jointsMap = new LinkedHashMap();
	private static List<String> jointNamesList = new ArrayList<>();

	private static final NodeFx modelNode = new NodeFx("1", "CONTROLLER", "CONTROLLER");

	private static List<TriangleMesh> meshesList = new LinkedList<>();

	private static int countMesh;
	private static int countJoint;
	
	
	public static SceneFx loadAnimGameItem(String resourcePath, String texturesDir) throws Exception {
		return loadAnimGameItem(resourcePath, texturesDir, aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices
				| aiProcess_Triangulate | aiProcess_FixInfacingNormals | aiProcess_LimitBoneWeights);
	}

	public static SceneFx loadAnimGameItem(String resourcePath, String texturesDir, int flags) throws Exception {
		final Map<String, List<TriangleMesh>> meshesMap = new LinkedHashMap();
		
		AIScene aiScene = aiImportFile(resourcePath, flags);
		if (aiScene == null) {
			throw new Exception("Error loading model");
		}

		int numMaterials = aiScene.mNumMaterials();
		PointerBuffer aiMaterials = aiScene.mMaterials();
		List<LwjglMaterial> materials = new ArrayList<>();
		for (int i = 0; i < numMaterials; i++) {
			AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
			processMaterial(aiMaterial, materials, texturesDir);
		}

		int numMeshes = aiScene.mNumMeshes();
		PointerBuffer aiMeshes = aiScene.mMeshes();
		MeshFx[] meshes = new MeshFx[numMeshes];

		SceneFx rootNodeFx = new SceneFx("main_scene");
		AINode aiRootNode = aiScene.mRootNode();
		Matrix4f rootTransfromation = toMatrix(aiRootNode.mTransformation());
		
		for (int i = 0; i < numMeshes; i++) {
			AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			MeshFx mesh = processMesh(aiMesh, aiRootNode, materials, jointsMap, i);
			meshes[i] = mesh;
		}

		for (Map.Entry<String,JointFx> item : jointsMap.entrySet()) {
			jointNamesList.add(item.getKey());
		}
		
	    NodeFx rootNode = processNodesHierarchy(aiRootNode, null);
		
		Parent hierarchy = getParent(rootNode, jointsMap);

		//hierarchy.getTransforms().add(adaptedMatrix(rootTransfromation));
		
		
		for (int i = 0; i < numMeshes; i++) {
			SkinningMesh skinningMesh = new SkinningMesh(
					meshes[i],
					meshes[i].getJointsPointsWeights(), 
					meshes[i].getBindPosList(),
					meshes[i].getBaseTransformMatrix(),
					meshes[i].getJointList(),
					Arrays.asList(hierarchy));
			
			final MeshView meshView = new MeshView(skinningMesh);

			final SkinningMeshTimer skinningMeshTimer = new SkinningMeshTimer(skinningMesh);
			if (meshView.getScene() != null) {
				skinningMeshTimer.start();
			}

			meshView.sceneProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == null) {
					skinningMeshTimer.stop();
				} else {
					skinningMeshTimer.start();
				}
			});

			/*if (i < materials.size()) {
				meshView.setMaterial(materials.get(i));
			} else {*/
			PhongMaterial material = new PhongMaterial(Color.BURLYWOOD);

			meshView.setMaterial(material);

			meshView.setDrawMode(DrawMode.FILL);
			meshView.setCullFace(CullFace.BACK);
			//}

			meshes[i].getChildren().add(meshView);
		}

		Map<String, Animation> animations = processAnimations(aiScene, jointsMap, rootNode);
		buildTimelines(jointsMap, (Skeleton) hierarchy);
		
		rootNodeFx.getChildren().addAll(Arrays.asList(meshes));
		rootNodeFx.getChildren().add(hierarchy);
		
		return rootNodeFx;
	}

	private static MeshFx processMesh(
			AIMesh aiMesh, 
			AINode aiRootNode,
			List<LwjglMaterial> materials, 
			Map<String,JointFx> jointsMap,
			int meshId) {
		List<Float> points = new ArrayList<>();
		List<Float> texCoords = new ArrayList<>();
		List<Float> normals = new ArrayList<>();
		List<Integer> faces = new ArrayList<>();
		List<Integer> currentJoint = new ArrayList<>();
		List<String> jointNamesList = new ArrayList<>();
		List<Affine> bindPosList = new ArrayList<>();
		List<float[][]> jointsPointsWeigth = new ArrayList<>();
		List<JointFx> currentMeshJoints = new ArrayList<>();

		processVertices(aiMesh, points);
		processNormals(aiMesh, normals);
		processTextCoords(aiMesh, texCoords);
		processIndices(aiMesh, faces);
		processJoints(aiMesh, jointsMap, jointNamesList, bindPosList, meshId, faces.size(), 
				jointsPointsWeigth, currentMeshJoints, currentJoint);
		
		MeshFx mesh = new MeshFx(
				Utils.listToArray(points),
				Utils.listToArray(normals),
				Utils.listToArray(texCoords),
				Utils.listIntToArray(faces), 
				jointsPointsWeigth.get(0),
				adaptedMatrix(toMatrix(aiRootNode.mTransformation())),
				Utils.listAffineToArray(bindPosList),
				currentMeshJoints);
			
		LwjglMaterial material;
		
		int materialIdx = aiMesh.mMaterialIndex();
		if (materialIdx >= 0 && materialIdx < materials.size()) {
			material = materials.get(materialIdx);
		} else {
			material = new LwjglMaterial();
		}
		return mesh;
	}

	@SuppressWarnings("static-access")
	private static Parent getParent(NodeFx rootNode, Map<String, JointFx> jointsMap) {
		
		NodeFx job = findOrigin(rootNode);
		
		if ( job != null ) {
			rootNode = rootineHierarchy(job);
		}
		
		Skeleton parent = new Skeleton();
		
		//parent = parent.SetChild(rootNode, jointList);
		parent = parent.fromNodeFx(rootNode, jointsMap);
		
		return parent;
	}

	private static NodeFx findOrigin(NodeFx node) {
		
		if (node.hasJoints()) { 
			return node;
		} else {
			for (Node item : node.getChildren()) {
				NodeFx childNode = findOrigin((NodeFx) item);
				if (childNode != null ) return childNode;
			}
		}
		
		return null;
	}
	
	private static NodeFx rootineHierarchy(NodeFx node) {
		
		List<Node> nodes = node.getChildren().stream().filter(v -> !((NodeFx)v).isJoint()).collect(Collectors.toList());
		
		nodes.forEach(v -> node.getChildren().remove(v));
		
		for (Node item : node.getChildren())

			rootineHierarchy((NodeFx) item);
		
		return node;
	}
	
	private static void processJoints(
			AIMesh aiMesh, 
			Map<String, JointFx> jointsMap,
			List<String> jointNamesList,
			List<Affine> bindPosList,
			int meshId, 
			int countIndices, 
			List<float[][]> jointsPointsWeigth,
			List<JointFx> currnetMeshJoints,
			List<Integer> currentJoints) {
		
		Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();
		int numBones = aiMesh.mNumBones();
		PointerBuffer aiBones = aiMesh.mBones();

		int maxNumWeight = 0;

		Map<Integer, List<Affine>> bindPosMap = new HashMap();
		Map<Integer, List<String>> jointNamesMap = new HashMap();

		for (int i = 0; i < numBones; i++) {
			AIBone aiBone = AIBone.create(aiBones.get(i));
			int numWeights = aiBone.mNumWeights();
			if (maxNumWeight < numWeights)
				maxNumWeight = numWeights;
		}

		int numVertices = aiMesh.mNumVertices();
		
		currentJoints.clear();
		currentJoints.add(numBones);
		
		bindPosList.clear();
		jointNamesList.clear();
		

		List<Affine> bindPosListBuf = null;
		List<String> jointNamesListBuf = null;
		
		JointFx joint = null;
		
		for (int i = 0; i < numBones; i++) {
			AIBone aiBone = AIBone.create(aiBones.get(i));

			if (jointsMap.get(aiBone.mName().dataString().trim()) != null) {
				joint = jointsMap.get(aiBone.mName().dataString().trim());
			} else {
				joint = new JointFx(aiBone.mName().dataString().trim(), aiBone.mName().dataString().trim(), adaptedMatrix(toMatrix(aiBone.mOffsetMatrix())));
				jointsMap.put(aiBone.mName().dataString().trim(),joint);
			}
			
			joint.setJointId(i);
			joint.setMeshId(meshId);
			joint.setOffsetMatrix(adaptedMatrix(toMatrix(aiBone.mOffsetMatrix())));
			//joint.getTransforms().add(adaptedMatrix(toMatrix(aiBone.mOffsetMatrix())));
			bindPosList.add(joint.getOffsetMatrix());
			jointNamesList.add(joint.getJointName());
			currnetMeshJoints.add(joint);
			
			//System.out.println(i + " " + bone.getBoneName());

			int numWeights = aiBone.mNumWeights();
			AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
			for (int j = 0; j < numWeights; j++) {
				AIVertexWeight aiWeight = aiWeights.get(j);
				VertexWeight vw = new VertexWeight(joint.getJointId(), aiWeight.mVertexId(), aiWeight.mWeight());

				List<VertexWeight> vertexWeightList = weightSet.get(vw.getVertexId());
				if (vertexWeightList == null) {
					vertexWeightList = new ArrayList<>();
					weightSet.put(vw.getVertexId(), vertexWeightList);
				}
				vertexWeightList.add(vw);
			}
		}

		int maxBoneDimSize = 0;

		List<Integer> verticeId = new ArrayList<>();
		float[][] jointsPointsWeigthBuff = new float[numBones][numVertices];
		System.out.println(
				"joint: numBones: " + numBones + " numVertices: " + numVertices + " exterpolate points: " + numVertices * 3);

		maxBoneDimSize++;
		Set<Integer> ubone = new HashSet<>();

		for (Map.Entry<Integer, List<VertexWeight>> item : weightSet.entrySet()) {
			for (VertexWeight itemVW : item.getValue()) {
				jointsPointsWeigthBuff[itemVW.getJointId()][itemVW.getVertexId()] = itemVW.getWeight();
				if (itemVW.getJointId() >= numBones)
					System.out.println(itemVW.getJointId());

			}
		}
		
		jointsPointsWeigth.add(jointsPointsWeigthBuff);
}
	
	private static void buildTransFormationMatrices(AINodeAnim aiNodeAnim, NodeFx node) {
		int numFrames = aiNodeAnim.mNumPositionKeys();
		AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
		AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
		AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

		double timeOfFrame = 0;
		List<Float> timeOfFramesFloat = new ArrayList<>();

		double time = 0d;
		boolean flag = false;
		
		for (int i = 0; i < numFrames; i++) {
			AIVectorKey pk = positionKeys.get(i);
			time += pk.mTime();
			AIQuatKey qk = rotationKeys.get(i);
			time += pk.mTime();
			if (i < aiNodeAnim.mNumScalingKeys()) {
				AIVectorKey sk = scalingKeys.get(i);
				time += pk.mTime();
			}
		}	
		
		if (time == 0) flag = true;
			
		for (int i = 0; i < numFrames; i++) {
			timeOfFrame = 0;

			AIVectorKey aiVecKey = positionKeys.get(i);
			AIVector3D vec = aiVecKey.mValue();

			if (flag) {
				timeOfFrame = i * timeQuantum;
			}	else 
				if (timeOfFrame < aiVecKey.mTime())
					timeOfFrame = aiVecKey.mTime();
			
			Matrix4f transfMat = new Matrix4f().translate(vec.x(), vec.y(), vec.z());
			AIQuatKey quatKey = rotationKeys.get(i);
			AIQuaternion aiQuat = quatKey.mValue();
			Quaternionf quat = new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
			transfMat.rotate(quat);
			
			if (!flag) 
				if (timeOfFrame < quatKey.mTime())
					timeOfFrame = quatKey.mTime();

			if (i < aiNodeAnim.mNumScalingKeys()) {
				aiVecKey = scalingKeys.get(i);
				vec = aiVecKey.mValue();
				transfMat.scale(vec.x(), vec.y(), vec.z());
				if (!flag) 
					if (timeOfFrame < aiVecKey.mTime())
						timeOfFrame = aiVecKey.mTime();
			}

			node.addTransformations(adaptedMatrix(transfMat));
			timeOfFramesFloat.add((float) timeOfFrame * timeQuantum);
			nodesFxMap.put(node.getId(), node);
		}
		node.setTimeOfFrames(timeOfFramesFloat);
	}
	
	private static List<AnimatedFrame> buildAnimationFrames(Map<String, JointFx> jointsMap, NodeFx rootNode) {

		List<AnimatedFrame> frameList = new ArrayList<>();
		int numBones = jointsMap.size();
		String[] jointNames = new String[numBones];
		
		int numFrames = rootNode.getAnimationFrames();
		for (int i = 0; i < numFrames; i++) {
			AnimatedFrame frame = new AnimatedFrame();
			frameList.add(frame);

/*
  			for (int j = 0; j < numBones; j++) {
				JointFx joint = jointList.get(j);
				NodeFx node = rootNode.findByName(joint.getId());

				Affine boneMatrix = node.getTransformations().get(i);

				frame.setMatrix(j, boneMatrix);
			}
*/
			int j = 0;
  			for (Map.Entry<String, JointFx> item : jointsMap.entrySet()) {
				JointFx joint = item.getValue();
				NodeFx node = rootNode.findByName(joint.getId());

				Affine boneMatrix = node.getTransformations().get(i);

				frame.setMatrix(j++, boneMatrix);
			}
		}

		return frameList;
	}

	private static Map<String, Animation> processAnimations(
			AIScene aiScene, 
			Map<String, JointFx> jointsMap,
			NodeFx rootNode) {
		Map<String, Animation> animations = new HashMap<>();

		// Process all animations
		int numAnimations = aiScene.mNumAnimations();
		PointerBuffer aiAnimations = aiScene.mAnimations();
		for (int i = 0; i < numAnimations; i++) {
			AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));

			// Calculate transformation matrices for each node
			int numChanels = aiAnimation.mNumChannels();
			PointerBuffer aiChannels = aiAnimation.mChannels();
			for (int j = 1; j < numChanels; j++) {
				AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(j));
				String nodeName = aiNodeAnim.mNodeName().dataString();
				NodeFx node = rootNode.findByName(nodeName);

				buildTransFormationMatrices(aiNodeAnim, node);
			}

			List<AnimatedFrame> frames = buildAnimationFrames(jointsMap, rootNode);
			Animation animation = new Animation(aiAnimation.mName().dataString(), frames, aiAnimation.mDuration());
			animations.put(animation.getName(), animation);

		}
		return animations;
	}

	private static NodeFx processNodesHierarchy(AINode aiNode, NodeFx parentNode) {
		if (aiNode == null)
			return null;

		String nodeName = aiNode.mName().dataString();
		
		JointFx foundJoint = jointsMap.get(nodeName);
		
		NodeFx nodeFx;
		if (foundJoint != null) {
			nodeFx = new NodeFx(nodeName,nodeName,"JOINT");
			nodeFx.getTransforms().addAll(adaptedMatrix(toMatrix(aiNode.mTransformation())));
		} else
			nodeFx = new NodeFx(nodeName,"","");
		
		int numChildren = aiNode.mNumChildren();
		PointerBuffer aiChildren = aiNode.mChildren();
		for (int i = 0; i < numChildren; i++) {
			AINode aiChildNode = AINode.create(aiChildren.get(i));
			NodeFx childNode = processNodesHierarchy(aiChildNode, nodeFx);
			nodeFx.getChildren().add(childNode);
		}

		return nodeFx;
	}

	public static LinkedList<SceneFx> getScenesfx() {
		return scenesFx;
	}

	public static LinkedList<NodeFx> getNodesfx() {
		return nodesFx;
	}

	public static NodeFx getModelNode() {
		return modelNode;
	}

	public Map<String, Timeline> getTimelines() {
		return timelines;
	}
	
	public static float getTimeQuantum() {
		return timeQuantum;
	}

	public static Map<String, List<KeyFrame>> getKeyFramesMap(Map<String, JointFx> jointsMap, Skeleton skeleton) {
		final Map<String, List<KeyFrame>> frames = new HashMap<>();

		jointsMap.entrySet().stream().map((map) -> {
			AnimationFx animation = new AnimationFx(map.getKey());
			NodeFx nodeFx = nodesFxMap.get(map.getKey());

			animation.setOutput(AffineListToDouble(nodeFx.getTransformations()));
			animation.setInput(listToArray(nodeFx.getTimeOfFrames()));

			animation.setTarget(map.getKey());
			return animation;
		}).forEach(animation -> frames.put(animation.id, animation.calculateAnimation(skeleton)));

		return frames;
	}

	public static void buildTimelines(Map<String, JointFx> jointsMap, Skeleton skeleton) {
		getKeyFramesMap(jointsMap, skeleton).forEach((key, value) -> {
			if (!timelines.containsKey(key)) {
				timelines.put(key, new Timeline());
			}
			timelines.get(key).getKeyFrames().addAll(value);
		});
	}

}
