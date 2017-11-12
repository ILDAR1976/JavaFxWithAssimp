package edu.lwjgl_fx_01.ui.model.engine.graph;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Affine;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joml.Matrix4f;

import edu.lwjgl_fx_01.ui.controller.ModelController;
import edu.lwjgl_fx_01.ui.model.engine.graph.anim.SkinningMeshTimer;
import edu.lwjgl_fx_01.ui.model.engine.items.FeatureToggle;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.Joint;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.LwjglNode;
import edu.lwjgl_fx_01.ui.utils.Utils;

/**
 * @author Eclion, Iha
 */
@SuppressWarnings("restriction")
public final class ModelNode extends Group {
	public final String name;
	public final String type;
	private Category instanceCategory = Category.NONE;
	private String instanceId;
	private Affine affine;
	private List<Matrix4f> transformations = new ArrayList<>();
	private List<Affine> transformationsA = new ArrayList<>();
	private List<ModelNode> meshes = new ArrayList<>();
	private List<Float> timeOfFrames = new ArrayList<>();
	
	private enum Category {
		CAMERA, GEOMETRY, CONTROLLER, LIGHT, NONE
	}

	public ModelNode(final String id, final String name, final String type) {
		this.setId(id);
		this.name = name;
		this.type = type;

	}

	public void setInstanceCameraId(final String instanceCameraId) {
		instanceCategory = Category.CAMERA;
		instanceId = instanceCameraId;
	}

	public void setInstanceGeometryId(final String instanceGeometryId) {
		instanceId = instanceGeometryId;
		instanceCategory = Category.GEOMETRY;
	}

	public void setInstanceControllerId(final String instanceControllerId) {
		instanceId = instanceControllerId;
		instanceCategory = Category.CONTROLLER;
	}

	public void setInstanceLightId(final String instanceLightId) {
		instanceId = instanceLightId;
		instanceCategory = Category.LIGHT;
	}

	public boolean hasJoints() {
		return getModelNodeChildStream().anyMatch(ModelNode::isJoint);
	}

	public boolean isJoint() {
		return "JOINT".equalsIgnoreCase(type);
	}

	@Override
	public String toString() {
		return "ModelNode{" + "id='" + this.getId() + '\'' + ", name='" + this.name + '\'' + ", instance="
				+ this.instanceId + ", instance_category=" + this.instanceCategory.toString().toLowerCase() + '}';
	}

	public void build(final BuildHelper buildHelper) {
		switch (instanceCategory) {
		case CAMERA:
			buildCamera(buildHelper);
			break;
		case CONTROLLER:
			FeatureToggle.onDisplayMeshsChange(bool -> {
				if (bool) {
					buildController(buildHelper);
				}
			});
			FeatureToggle.onDisplaySkeletonsChange(bool -> {
				if (bool) {
					buildSkeleton(buildHelper);
				}
			});
			break;
		case GEOMETRY:
			buildGeometry(buildHelper);
			break;
		case LIGHT:
		case NONE:
		default:
			break;
		}

		getModelNodeChildStream().forEach(child -> child.build(buildHelper));
	}

	public Stream<ModelNode> getModelNodeChildStream() {
		return Utils.getModelNodeChildStream(this);
	}

	private void buildCamera(final BuildHelper buildHelper) {
		getChildren().add(buildHelper.getCamera(instanceId));
	}

	public void buildController(final BuildHelper buildHelper) {
		final ModelController controller = buildHelper.getController(instanceId);
		final Skeleton skeleton = buildHelper.getSkeleton(controller.getName());

		String[] jointNames = null;

		List<Joint> joints = null;

		Affine[] bindTransforms = null;

		final List<TriangleMesh> meshes = buildHelper.getMeshes(controller.getSkinId());
		final List<Material> materials = buildHelper.getMaterials(controller.getSkinId());

		for (int i = 0; i < meshes.size(); i++) {
			jointNames = controller.getJointNames();
			joints = Stream.of(jointNames).map(skeleton.joints::get).collect(Collectors.toList());
			bindTransforms = controller.getBindPoses().toArray(new Affine[joints.size()]);

			meshes.get(i).setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);

			final SkinningMesh skinningMesh = new SkinningMesh(meshes.get(i), controller.getVertexWeights(),
					bindTransforms, controller.getBindShapeMatrix(), joints, Arrays.asList(skeleton));

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

			if (i < materials.size()) {
				meshView.setMaterial(materials.get(i));
			} else {
			        int width = 100;
			        int height = 100;
					
			        final WritableImage diffuseMap = new WritableImage(width, height);
			        final WritableImage specularMap = new WritableImage(width, height);
			        final WritableImage selfIllumMap = new WritableImage(width, height);
			        generateMap(diffuseMap, Color.BISQUE);
			        generateMap(specularMap, Color.ANTIQUEWHITE);
			        final PhongMaterial sharedMaterial = new PhongMaterial();
			        final PhongMaterial sharedMapMaterial = new PhongMaterial();
			        sharedMapMaterial.setDiffuseMap(diffuseMap);
			        sharedMapMaterial.setSpecularMap(specularMap);

			        meshView.setMaterial(sharedMapMaterial);
			 
				//meshView.setMaterial(material);

				meshView.setDrawMode(DrawMode.FILL);
				meshView.setCullFace(CullFace.BACK);
			}

			getChildren().add(meshView);
		}
	}

	
	private Image generateMap(WritableImage writableImage, Color color) {
		// Copy from source to destination pixel by pixel
		PixelWriter pixelWriter = writableImage.getPixelWriter();

		for (int y = 0; y < writableImage.getHeight(); y++) {
			for (int x = 0; x < writableImage.getWidth(); x++) {
				pixelWriter.setColor(x, y, color);
			}
		}
		return writableImage;
	}

	public void buildSkeleton(final BuildHelper buildHelper) {

		final ModelController controller = buildHelper.getController(instanceId);

		final Skeleton skeleton = buildHelper.getSkeleton(controller.getName());

		final List<Joint> joints = new ArrayList<>(skeleton.joints.values());

		joints.forEach(Joint::addMeshView);
		
		getChildren().add(skeleton);
	}

	public void buildGeometry(final BuildHelper buildHelper) {
		final List<TriangleMesh> meshes = buildHelper.getMeshes(instanceId);

		final List<Material> materials = buildHelper.getMaterials(instanceId);

		for (int i = 0; i < meshes.size(); i++) {
			final MeshView meshView = new MeshView(meshes.get(i));
			if (i < materials.size()) {
				meshView.setMaterial(materials.get(i));
			}
			addMeshViewAsChild(meshView);
		}
	}

	private void addMeshViewAsChild(final MeshView meshView) {
		FeatureToggle.onDisplayMeshsChange(bool -> {
			if (bool) {
				getChildren().add(meshView);
			} else if (getChildren().contains(meshView)) {
				getChildren().remove(meshView);
			}
		});
	}

	public Affine getAffine() {
		return affine;
	}

	public void setAffine(Affine affine) {
		this.affine = affine;
	}

	public List<Matrix4f> getTransformations() {
		return transformations;
	}

	public void setTransformations(List<Matrix4f> transformations) {
		this.transformations = transformations;
	}

	public void addTransformation(Matrix4f transformation) {
		transformations.add(transformation);
	}

	public void setTransformationsA(List<Affine> transformations) {
		this.transformationsA = transformations;
	}

	public void addTransformationA(Affine transformation) {
		transformationsA.add(transformation);
	}

	public List<Affine> getTransformationsA() {
		return transformationsA;
	}
	
	public List<Float> getTimeOfFrames() {
		return timeOfFrames;
	}

	public void setTimeOfFrames(List<Float> timeOfFrames) {
		this.timeOfFrames = timeOfFrames;
	}

	public ModelNode findByName(String targetName) {
		ModelNode result = null;
		if (this.name.equals(targetName)) {
			result = this;
		} else {
			for (Node child : getChildren()) {
				result = ((ModelNode)child).findByName(targetName);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}
	
	public List<ModelNode> getMeshes() {
		return meshes;
	}

	public void setMeshes(List<ModelNode> meshes) {
		this.meshes = meshes;
	}

	public void add(ModelNode mesh) {
		this.meshes.add(mesh);
	}
	
}