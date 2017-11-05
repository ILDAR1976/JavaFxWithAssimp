package edu.lwjgl_fx_01.ui.model.engine.loaders.assimp;

import org.joml.Matrix4f;
import javafx.scene.transform.Affine;

@SuppressWarnings("restriction")
public class Bone {
	private int globalId;
	
	private int meshId;
	
    private final int boneId;

    private final String boneName;

    private Matrix4f offsetMatrix;

    private Affine channelMatrix;
    
    public Bone(int boneId, String boneName, Matrix4f offsetMatrix) {
        this.boneId = boneId;
        this.boneName = boneName;
        this.offsetMatrix = offsetMatrix;
    }

    public int getBoneId() {
        return boneId;
    }

    public String getBoneName() {
        return boneName;
    }

    public Matrix4f getOffsetMatrix() {
        return offsetMatrix;
    }

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public int getGlobalId() {
		return globalId;
	}

	public void setGlobalId(int globalId) {
		this.globalId = globalId;
	}

	public Affine getChannelMatrix() {
		return channelMatrix;
	}

	public void setChannelMatrix(Affine channelMatrix) {
		this.channelMatrix = channelMatrix;
	}

    
}
