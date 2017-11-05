package edu.lwjgl_fx_01.ui.model.engine.loaders.assimp;

import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_FixInfacingNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GenSmoothNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_LimitBoneWeights;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import edu.lwjgl_fx_01.ui.utils.Utils;
import edu.lwjgl_fx_01.ui.controller.ModelController;
import edu.lwjgl_fx_01.ui.model.engine.LwjglScene;
import edu.lwjgl_fx_01.ui.model.engine.ModelScene;
import edu.lwjgl_fx_01.ui.model.engine.graph.BuildHelper;
import edu.lwjgl_fx_01.ui.model.engine.graph.LwjglMaterial;
import edu.lwjgl_fx_01.ui.model.engine.graph.LwjglMesh;
import edu.lwjgl_fx_01.ui.model.engine.graph.ModelNode;
import edu.lwjgl_fx_01.ui.model.engine.graph.Skeleton;

import static edu.lwjgl_fx_01.ui.utils.Utils.*;
import edu.lwjgl_fx_01.ui.model.engine.graph.anim.AnimatedFrame;
import edu.lwjgl_fx_01.ui.model.engine.graph.anim.Animation;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;

@SuppressWarnings({ "unchecked", "rawtypes", "unused", "restriction" })
public class AnimMeshesLoader extends StaticMeshesLoader {
	private static final LinkedList<ModelScene> scenesFx = new LinkedList<>();
	private static final LinkedList<ModelNode> nodesFx = new LinkedList<>();
	
	private static final ModelNode modelNode = new ModelNode("1", "CONTROLLER", "CONTROLLER");
	
    private static float[][] bonesVerteicesWeigth;
    private static List<Bone> boneList = new ArrayList<>();
    private static List<TriangleMesh> meshesList = new ArrayList<>();

    private static void buildTransFormationMatrices(AINodeAnim aiNodeAnim, LwjglNode node, ModelNode nodeFx) {
        int numFrames = aiNodeAnim.mNumPositionKeys();
        AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
        AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
        AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

        double timeOfFrame = 0;
        List<Double> timeOfFrames = new ArrayList<>();
        
        for (int i = 0; i < numFrames; i++) {
        	timeOfFrame = 0;
        	
            AIVectorKey aiVecKey = positionKeys.get(i);
            AIVector3D vec = aiVecKey.mValue();
            
            if (timeOfFrame < aiVecKey.mTime()) timeOfFrame = aiVecKey.mTime();
            	
            Matrix4f transfMat = new Matrix4f().translate(vec.x(), vec.y(), vec.z());
            AIQuatKey quatKey = rotationKeys.get(i);
            AIQuaternion aiQuat = quatKey.mValue();
            Quaternionf quat = new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
            transfMat.rotate(quat);
            if (timeOfFrame < quatKey.mTime()) timeOfFrame = quatKey.mTime();
            
            if (i < aiNodeAnim.mNumScalingKeys()) {
                aiVecKey = scalingKeys.get(i);
                vec = aiVecKey.mValue();
                transfMat.scale(vec.x(), vec.y(), vec.z());
                if (timeOfFrame < aiVecKey.mTime()) timeOfFrame = aiVecKey.mTime();
            }

            node.addTransformation(transfMat);
            
            nodeFx.addTransformation(transfMat);
            
            timeOfFrames.add(timeOfFrame);
        }
        
        node.setTimeOfFrames(timeOfFrames);
    }
    
    public static LwjglScene loadAnimGameItem(String resourcePath, String texturesDir)
            throws Exception {
        return loadAnimGameItem(resourcePath, texturesDir,
                aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
                | aiProcess_FixInfacingNormals | aiProcess_LimitBoneWeights);
    }
	
    public static LwjglScene loadAnimGameItem(String resourcePath, String texturesDir, int flags)
            throws Exception {
    	
        final BuildHelper buildHelper = mainScene.getBuilder();
        final Map<String, Skeleton> skeletonsMap = new HashMap();
        final Map<String, List<TriangleMesh>> meshesMap = new HashMap();
        final Map<String, ModelController> controllerMap = new HashMap();
        
        Skeleton skl = new Skeleton("CONTROLLER");
        
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
        LwjglMesh[] meshes = new LwjglMesh[numMeshes];
        List<LwjglMesh> mainMeshes = new ArrayList<>();
 

        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            LwjglMesh mesh = processMesh(aiMesh, materials, boneList, i);
            meshes[i] = mesh;
            mainMeshes.add(mesh);
            
            TriangleMesh meshFx = new TriangleMesh();
            meshFx.getPoints().setAll(mesh.getPointsFx());
            meshFx.getFaces().setAll(mesh.getFaces3Fx());
            meshFx.getNormals().setAll(mesh.getNormalsFx());
            
            if (mesh.getTextureCoordianatesFx().length == 0) 
            	meshFx.getTexCoords().addAll(0,0);
            else
            	meshFx.getTexCoords().setAll(mesh.getTextureCoordianatesFx());
           
            if (mesh.getTextureCoordianatesFx().length % meshFx.getTexCoordElementSize() != 0 && 
            		(mesh.getTextureCoordianatesFx().length != 0))
            	meshFx.getTexCoords().addAll(0);
            System.out.println("faces.size: " + meshFx.getFaces().size() + 
        	       " faceSize: " + meshFx.getFaceElementSize() + 
        	       " points: " + meshFx.getPoints().size() +
        	       " pointSize: " + meshFx.getPointElementSize() +
        	       " normals: " + meshFx.getNormals().size() +
        	       " texCoords: " + meshFx.getTexCoords().size());

            meshesList.add(meshFx);
        }
        meshesMap.put("Meshes", meshesList);
        modelController.setSkinId("Meshes");
        
        mainScene.setMeshes(mainMeshes);
        AINode aiRootNode = aiScene.mRootNode();
        Matrix4f rootTransfromation = AnimMeshesLoader.toMatrix(aiRootNode.mTransformation());
        
        Nodes nodes = processNodesHierarchy(aiRootNode, null, null);
        
        LwjglNode rootNode = nodes.lwjglNode; 
        ModelNode rootNodeFx = nodes.modelNode;
        
        mainScene.setRootNode(rootNode);
        Map<String, Animation> animations = processAnimations(aiScene, boneList, rootNode, rootNodeFx, rootTransfromation);
        mainScene.setAnimations(animations);
        
        scenesFx.add(new ModelScene("MainScene"));
        
        modelNode.setInstanceControllerId("CONTROLLER");
        
        buildSkeletonIfChildrenHaveJoints(rootNodeFx);
        
        List<Skeleton> sk = scenesFx.get(0).skeletons.values().stream()
        		                    .map(skeleton->(Skeleton) skeleton)
        		                    .collect(Collectors.toList());
        skeletonsMap.put("CONTROLLER", sk.get(0));
        
        controllerMap.put("CONTROLLER", modelController); //setup model controller here
        
        buildHelper.withControllers(controllerMap);
        buildHelper.withMeshes(meshesMap);
        buildHelper.withSkeletons(skeletonsMap);
        
        modelNode.buildController(buildHelper);
        modelNode.buildSkeleton(buildHelper);

        mainScene.setBuilder(buildHelper);
        return mainScene;
    }
    
    private static List<AnimatedFrame> buildAnimationFrames(List<Bone> boneList, LwjglNode rootNode,
            ModelNode rootNodeFx, Matrix4f rootTransformation) {
    	
    	for (int i=0; i<meshesList.size();i++)
    		modelController.setBindShapeMatrix(i, adaptedMatrix(rootTransformation));
        
    	int numFrames = rootNode.getAnimationFrames();
        List<AnimatedFrame> frameList = new ArrayList<>();
        int numBones = boneList.size();
        String[] jointNames = new String[numBones];

        for (int i = 0; i < numFrames; i++) {
            AnimatedFrame frame = new AnimatedFrame();
            frameList.add(frame);
            
            for (int j = 0; j < numBones; j++) {
                Bone bone = boneList.get(j);
                LwjglNode node = rootNode.findByName(bone.getBoneName());
                ModelNode nodeFx = rootNodeFx.findByName(bone.getBoneName());
                Matrix4f boneMatrix = new Matrix4f(nodeFx.getTransformations().get(i));
                frame.setMatrix(j, boneMatrix);
                node.setAffine(adaptedMatrix(bone.getOffsetMatrix()));
             }
            
         }
 
        
        return frameList;
    }

    private static Map<String, Animation> processAnimations(AIScene aiScene, List<Bone> boneList,
    		LwjglNode rootNode, ModelNode rootNodeFx, Matrix4f rootTransformation) {
        Map<String, Animation> animations = new HashMap<>();

        // Process all animations
        int numAnimations = aiScene.mNumAnimations();
        PointerBuffer aiAnimations = aiScene.mAnimations();
        for (int i = 0; i < numAnimations; i++) {
            AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));

            // Calculate transformation matrices for each node
            int numChanels = aiAnimation.mNumChannels();
            PointerBuffer aiChannels = aiAnimation.mChannels();
            for (int j = 0; j < numChanels; j++) {
                AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(j));
                String nodeName = aiNodeAnim.mNodeName().dataString();
                LwjglNode node = rootNode.findByName(nodeName);
                
                ModelNode nodeFx =  rootNodeFx.findByName(nodeName);
                
                buildTransFormationMatrices(aiNodeAnim, node, nodeFx);
            }

            List<AnimatedFrame> frames = buildAnimationFrames(boneList, rootNode, rootNodeFx, rootTransformation);
            Animation animation = new Animation(aiAnimation.mName().dataString(), frames, aiAnimation.mDuration());
            animations.put(animation.getName(), animation);
        }
        return animations;
    }

    private static void processBones(AIMesh aiMesh, List<Bone> boneList, List<Integer> boneIds,
            List<Float> weights, int meshId, int countIndices) {
        Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();
        int numBones = aiMesh.mNumBones();
        PointerBuffer aiBones = aiMesh.mBones();

        int maxNumWeight = 0;

        Map<Integer,List<Affine>> bindPosMap = new HashMap();
        List<Affine> bindPosList = new ArrayList<>();
        
        Map<Integer,List<String>> jointNamesMap = new HashMap();
        List<String> jointNamesList = new ArrayList<>();
        
        for (int i = 0; i < numBones; i++) {
            AIBone aiBone = AIBone.create(aiBones.get(i));
            int numWeights = aiBone.mNumWeights();
            if (maxNumWeight < numWeights) maxNumWeight = numWeights;
        }    
             
        int numVertices = aiMesh.mNumVertices();
        //bonesVerteicesWeigth = new float[numBones + 100][numVertices + 100];
        
        
        for (int i = 0; i < numBones; i++) {
            AIBone aiBone = AIBone.create(aiBones.get(i));
            //int id = boneList.size();
            int id = i;
            Bone bone = new Bone(id, aiBone.mName().dataString().trim(), toMatrix(aiBone.mOffsetMatrix()));
            bone.setGlobalId(i + meshId*1000000);
            bone.setMeshId(meshId);
            boneList.add(bone);
            bonesNames.add(aiBone.mName().dataString().trim());
            //System.out.println(i + " " + bone.getBoneName());
            
            
           	if (bindPosMap.get(bone.getMeshId()) != null) {
           		bindPosList = bindPosMap.get(bone.getMeshId());
           		Matrix4f job = bone.getOffsetMatrix();
           		bindPosList.add(adaptedMatrix(job));
        	} else {
        		bindPosList = new ArrayList<>();
        		Matrix4f job = bone.getOffsetMatrix();
        		bindPosList.add(adaptedMatrix(job));
        		bindPosMap.put(bone.getMeshId(), bindPosList);
        	}

        	if (jointNamesMap.get(bone.getMeshId()) != null) {
        		jointNamesList = jointNamesMap.get(bone.getMeshId());
        		jointNamesList.add(bone.getBoneName());
        	} else {
        		jointNamesList = new ArrayList<>();
        		jointNamesList.add(bone.getBoneName());
        		jointNamesMap.put(bone.getMeshId(), jointNamesList);
        	}
            
            int numWeights = aiBone.mNumWeights();
            AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
            for (int j = 0; j < numWeights; j++) {
                AIVertexWeight aiWeight = aiWeights.get(j);
                VertexWeight vw = new VertexWeight(bone.getBoneId(), aiWeight.mVertexId(),
                        aiWeight.mWeight());
                List<VertexWeight> vertexWeightList = weightSet.get(vw.getVertexId());
                if (vertexWeightList == null) {
                    vertexWeightList = new ArrayList<>();
                    weightSet.put(vw.getVertexId(), vertexWeightList);
                }
                vertexWeightList.add(vw);
            }
        }
        
        bindPosMap.forEach((k, v) ->{
        	modelController.bindPoses.put(k, v);
        });
        
        jointNamesMap.forEach((k, v) ->{
        	modelController.setJointNames(k, v.toArray(new String[v.size()]));
        });

        int maxBoneDimSize = 0;
        
        List<Integer> verticeId = new ArrayList<>();
        bonesVerteicesWeigth = new float[numBones + 100][numVertices + 100];
     
        for (int i = 0; i < numVertices; i++) {
            List<VertexWeight> vertexWeightList = weightSet.get(i);
            int size = vertexWeightList != null ? vertexWeightList.size() : 0;
            for (int j = 0; j < LwjglMesh.MAX_WEIGHTS; j++) {
                if (j < size) {
                    VertexWeight vw = vertexWeightList.get(j);
                    weights.add(vw.getWeight());
                    boneIds.add(vw.getBoneId());
                    verticeId.add(i);
                    bonesVerteicesWeigth[vw.getBoneId()][i] = vw.getWeight();
                    if (maxBoneDimSize < vw.getBoneId()) maxBoneDimSize = vw.getBoneId(); 
                } else {
                    weights.add(0.0f);
                    boneIds.add(0);
                }
            }
        }
        
        maxBoneDimSize++;
         
        for (Map.Entry<Integer, List<VertexWeight>> item : weightSet.entrySet()) {
        	for (VertexWeight itemVW : item.getValue()) {
        		bonesVerteicesWeigth[itemVW.getBoneId()][itemVW.getVertexId()] = itemVW.getWeight();
        	}
        }
     }

    private static LwjglMesh processMesh(AIMesh aiMesh, List<LwjglMaterial> materials, List<Bone> boneList, int meshId) {
        List<Float> vertices = new ArrayList<>();
        List<Float> textures = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Integer> boneIds = new ArrayList<>();
        List<Float> weights = new ArrayList<>();
        
        
        processVertices(aiMesh, vertices);
        processNormals(aiMesh, normals);
        processTextCoords(aiMesh, textures);
        processIndices(aiMesh, indices);
        processBones(aiMesh, boneList, boneIds, weights, meshId, indices.size());

        LwjglMesh mesh = new LwjglMesh(Utils.listToArray(vertices), Utils.listToArray(textures),
                Utils.listToArray(normals), Utils.listIntToArray(indices), bonesVerteicesWeigth, boneList.size(),
                Utils.listIntToArray(boneIds), Utils.listToArray(weights));
        LwjglMaterial material;
        
        modelController.setVertexWeights(meshId, mesh.getBonesPointsWeigth3Fx());
        
        mesh.setBones(boneList);
        int materialIdx = aiMesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = new LwjglMaterial();
        }
        mesh.setMaterial(material);
        mesh.setName(aiMesh.mName().dataString());
        return mesh;
    }

    private static Nodes processNodesHierarchy(AINode aiNode, LwjglNode parentNode, ModelNode parentNodeFx) {
        String nodeName = aiNode.mName().dataString();
        
        final String tJoint = "";
        
        List<String> boneName = bonesNames.stream().filter(val -> nodeName.equals(val)).collect(Collectors.toList());    
        List<Bone> bone = boneList.stream().filter(val -> nodeName.equals(val.getBoneName())).collect(Collectors.toList());    
  
        LwjglNode node = new LwjglNode(nodeName.trim(), parentNode);
        
        ModelNode nodeFx = new ModelNode(
        		aiNode.mName().dataString().trim(),
        		aiNode.mName().dataString().trim(),
        		((aiNode.mNumMeshes() == 0) && (boneName.size() > 0)? "JOINT": ((aiNode.mNumMeshes() > 0) ? "MESH": "")));
        
        //Setup begin matrix here for joints
        nodeFx.setAffine(adaptedMatrix(toMatrix(aiNode.mTransformation())));
        nodeFx.getTransforms().add(adaptedMatrix(toMatrix(aiNode.mTransformation())));
        nodeFx.setEtaloneTransform(toMatrix(aiNode.mTransformation()));
 
        if (bone.size() != 0) nodeFx.setGlobalId(bone.get(0).getGlobalId());
        
        if (aiNode.mNumMeshes() > 0) nodeFx.setInstanceControllerId(nodeName + "_skin");
        
        Nodes outNode = new Nodes(node, nodeFx);
        
        nodesFx.push(nodeFx);
        
        int numChildren = aiNode.mNumChildren();
        PointerBuffer aiChildren = aiNode.mChildren();
        for (int i = 0; i < numChildren; i++) {
            AINode aiChildNode = AINode.create(aiChildren.get(i));
            Nodes childNode = processNodesHierarchy(aiChildNode, node, nodeFx);
            node.addChild(childNode.lwjglNode);
            nodeFx.getChildren().add(childNode.modelNode);
        }

        return outNode;
    }

    private static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {
        Matrix4f result = new Matrix4f();
        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());

        return result;
    }
 
	private static void setModelNode() {
        final ModelNode thisNode = nodesFx.pop();
        if (nodesFx.isEmpty()) {
            scenesFx.peek().getChildren().add(thisNode);
            buildSkeletonIfChildrenHaveJoints(thisNode);
        } else {
            nodesFx.peek().getChildren().add(thisNode);
        }
    }

 	private static void buildSkeletonIfChildrenHaveJoints(final ModelNode node) {
     	if (node.hasJoints()) {
            scenesFx.peek().skeletons.put(node.getId(), Skeleton.fromModelNode(node));
        } else {
            node.getModelNodeChildStream().forEach((value) -> {
            	buildSkeletonIfChildrenHaveJoints(value);	
            });
        }
    }

 	public static LinkedList<ModelScene> getScenesfx() {
		return scenesFx;
	}
	
    public static LinkedList<ModelNode> getNodesfx() {
		return nodesFx;
	}
    
    public static ModelNode getModelNode() {
		return modelNode;
	}

	private static class Nodes {
    	public LwjglNode lwjglNode;
    	public ModelNode modelNode;
    	
    	public Nodes(LwjglNode l, ModelNode m) {
    		this();
    		lwjglNode = l;
    		modelNode = m;
    	}
    	
    	public Nodes() {}
    }

	public static String getBoneName(int index) {
		//System.out.println("  gId:" + boneList.get(index).getGlobalId() + " meshId:" + boneList.get(index).getMeshId());
		return boneList.get(index).getBoneName();
	}

	public static int getQtyBones() {
		return boneList.size();
	}
}
