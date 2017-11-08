package edu.lwjgl_fx_01.ui.controller;

import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eclion, Iha
 */
@SuppressWarnings({ "unchecked", "rawtypes" ,"restriction"})
public final class ModelController {
    private final String name;

    private String skinId;
    private Affine bindShapeMatrix;
    private String[] jointNames;
    private float[][] vertexWeights;
    public List<Affine> bindPoses = new ArrayList<>();

    
    public ModelController(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

     public String getSkinId() {
        return skinId;
    }

    public void setSkinId(final String skinId) {
        this.skinId = skinId;
    }

    public Affine getBindShapeMatrix() {
        return bindShapeMatrix;
    }

    public void setBindShapeMatrix(final Affine bindShapeMatrix) {
        this.bindShapeMatrix = bindShapeMatrix;
    }

    public String[] getJointNames() {
        return jointNames;
    }

    public void setJointNames(final String[] jointNames) {
        this.jointNames = jointNames;
    }

    public float[][] getVertexWeights() {
        return vertexWeights;
    }

    public void setVertexWeights(final float[][] vertexWeights) {
        this.vertexWeights = vertexWeights;
    }

	public List<Affine> getBindPoses() {
		return bindPoses;
	}

	public void setBindPoses(List<Affine> bindPoses) {
		this.bindPoses = bindPoses;
	}
    
    
}
