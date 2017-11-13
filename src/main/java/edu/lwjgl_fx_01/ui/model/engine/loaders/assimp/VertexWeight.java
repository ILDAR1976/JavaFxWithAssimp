package edu.lwjgl_fx_01.ui.model.engine.loaders.assimp;

public class VertexWeight {

    private int jointId;

    private int vertexId;

    private float weight;

    public VertexWeight(int jointId, int vertexId, float weight) {
        this.jointId = jointId;
        this.vertexId = vertexId;
        this.weight = weight;
    }

    public int getJointId() {
        return jointId;
    }

    public int getVertexId() {
        return vertexId;
    }

    public float getWeight() {
        return weight;
    }

    public void setVertexId(int vertexId) {
        this.vertexId = vertexId;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
