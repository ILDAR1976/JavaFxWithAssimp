package edu.lwjgl_fx_01.ui.model.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Group;

import edu.lwjgl_fx_01.ui.model.engine.graph.NodeFx;
import edu.lwjgl_fx_01.ui.model.engine.graph.animation.Animation;
import edu.lwjgl_fx_01.ui.model.engine.shape3d.SkinningMesh;

@SuppressWarnings("restriction")
public class SceneFx extends Group  {
	private NodeFx rootNode;
	private String name;
	private List<SkinningMesh> meshes = new ArrayList<>();
    private Map<String, Animation> animations = new HashMap<>();
    
    public SceneFx(String name) {
    	this.name = name;
    }

	public NodeFx getRootNode() {
		return rootNode;
	}

	public void setRootNode(NodeFx rootNode) {
		this.rootNode = rootNode;
	}

	public List<SkinningMesh> getMeshes() {
		return meshes;
	}

	public void setMeshes(List<SkinningMesh> meshes) {
		this.meshes = meshes;
	}

	public Map<String, Animation> getAnimations() {
		return animations;
	}

	public void setAnimations(Map<String, Animation> animations) {
		this.animations = animations;
	}

 }
