package edu.lwjgl_fx_01.ui.model.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lwjgl_fx_01.ui.model.engine.graph.BuildHelper;
import edu.lwjgl_fx_01.ui.model.engine.graph.LwjglMesh;
import edu.lwjgl_fx_01.ui.model.engine.graph.ModelNode;
import edu.lwjgl_fx_01.ui.model.engine.graph.anim.Animation;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.LwjglNode;

public class LwjglScene {
	private LwjglNode rootNode;
	private List<LwjglMesh> meshes = new ArrayList<>();
    private Map<String, Animation> animations = new HashMap<>();
    private BuildHelper builder = new BuildHelper();	
    
    public LwjglScene() {
    }

	public LwjglNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(LwjglNode rootNode) {
		this.rootNode = rootNode;
	}

	public List<LwjglMesh> getMeshes() {
		return meshes;
	}

	public void setMeshes(List<LwjglMesh> meshes) {
		this.meshes = meshes;
	}

	public Map<String, Animation> getAnimations() {
		return animations;
	}

	public void setAnimations(Map<String, Animation> animations) {
		this.animations = animations;
	}

	public BuildHelper getBuilder() {
		return builder;
	}

	public void setBuilder(BuildHelper builder) {
		this.builder = builder;
	}

	
 }
