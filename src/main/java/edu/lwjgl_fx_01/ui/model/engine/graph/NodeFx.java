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

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.Parent;
import org.joml.Matrix4f;

import edu.lwjgl_fx_01.ui.model.engine.graph.animation.SkinningMeshTimer;
import edu.lwjgl_fx_01.ui.model.engine.items.FeatureToggle;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.JointFx;
import edu.lwjgl_fx_01.ui.model.engine.shape3d.SkinningMesh;
import edu.lwjgl_fx_01.ui.utils.Utils;

/**
 * @author Iha
 */

@SuppressWarnings({ "restriction", "unused" })
public final class NodeFx extends Group {
	public final String name;
	public final String type;
	private Category instanceCategory = Category.NONE;
	private String instanceId;
	private Affine affine;
	private List<Affine> transformations = new ArrayList<>();
	private List<NodeFx> meshes = new ArrayList<>();
	private List<Float> timeOfFrames = new ArrayList<>();
	
	private enum Category {
		CAMERA, GEOMETRY, LIGHT, NONE
	}

	public NodeFx(final String id, final String name, final String type) {
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

	public void setInstanceLightId(final String instanceLightId) {
		instanceId = instanceLightId;
		instanceCategory = Category.LIGHT;
	}

	public boolean hasJoints() {
		return getNodeFxChildStream().anyMatch(NodeFx::isJoint);
	}

	public boolean isJoint() {
		return "JOINT".equalsIgnoreCase(type);
	}

	@Override
	public String toString() {
		return "NodeFx {" + "id='" + this.getId() + '\'' + ", name='" + this.name + '\'' + ", instance="
				+ this.instanceId + ", instance_category=" + this.instanceCategory.toString().toLowerCase() + ", type=" + this.type + '}';
	}

	public Stream<NodeFx> getNodeFxChildStream() {
		return Utils.getNodeFxChildStream(this);
	}

	public void setTimeOfFrames(List<Float> timeOfFrames) {
		this.timeOfFrames = timeOfFrames;
	}

	public NodeFx findByName(String targetName) {
		NodeFx result = null;
		if (this.name.equals(targetName)) {
			result = this;
		} else {
			for (Node child : getChildren()) {
				result = ((NodeFx)child).findByName(targetName);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

	public NodeFx findByNameWithRootNode(String targetName, NodeFx rootNode) {
		NodeFx result = null;
		
		if (rootNode.name.equals(targetName)) {
			result = rootNode;
		} else {
			for (Node child : rootNode.getChildren()) {
				result = ((NodeFx)child).findByNameWithRootNode(targetName, (NodeFx)child);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

    public int getAnimationFrames() {
        int numFrames = this.transformations.size();
        for (Node child : this.getChildren()) {
            int childFrame = ((NodeFx) child).getAnimationFrames();
            numFrames = Math.max(numFrames, childFrame);
        }
        return numFrames;
    }
	
	public List<NodeFx> getMeshes() {
		return meshes;
	}

	public void setMeshes(List<NodeFx> meshes) {
		this.meshes = meshes;
	}

	public void add(NodeFx mesh) {
		this.meshes.add(mesh);
	}
	
	public List<Affine> getTransformations() {
		return transformations;
	}

	public void setTransformations(List<Affine> transformations) {
		this.transformations = transformations;
	}
	
	public void addTransformations(Affine transformations) {
		this.transformations.add(transformations);
	}

	public List<Float> getTimeOfFrames() {
		return timeOfFrames;
	}
	
	
}