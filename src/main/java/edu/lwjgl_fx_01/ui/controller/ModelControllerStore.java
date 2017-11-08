package edu.lwjgl_fx_01.ui.controller;

import javafx.scene.transform.Affine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eclion, Iha
 */
@SuppressWarnings({ "unchecked", "rawtypes" ,"restriction"})
public final class ModelControllerStore {
    private final String name;

    private String skinId;
	private Map<Integer, Affine> bindShapeMatrix = new HashMap();
	private Map<Integer, String[]> jointNames = new HashMap();
    private Map<Integer, float[][]> vertexWeights = new HashMap();
    public Map<Integer, List<Affine>> bindPoses = new HashMap();

    
    public ModelControllerStore(final String name) {
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

    public Affine getBindShapeMatrix(int meshId) {
        return bindShapeMatrix.get(meshId);
    }

    public void setBindShapeMatrix(final int meshId, final Affine bindShapeMatrix) {
        this.bindShapeMatrix.put(meshId, bindShapeMatrix);
    }

    public String[] getJointNames(int meshId) {
        return jointNames.get(meshId);
    }

    public void setJointNames(final int meshId, final String[] jointNames) {
        this.jointNames.put(meshId, jointNames);
    }

    public float[][] getVertexWeights(int meshId) {
        return vertexWeights.get(meshId);
    }

    public void setVertexWeights(final int meshId, final float[][] vertexWeights) {
        this.vertexWeights.put(meshId,vertexWeights);
    }

	public List<Affine> getBindPoses(int meshId) {
		return bindPoses.get(meshId);
	}

    
}
