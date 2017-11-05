package edu.lwjgl_fx_01.ui.model.engine.loaders.assimp;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

@SuppressWarnings("restriction")
public class LwjglNode {

    private String name;
    private List<Matrix4f> transformations;
    private LwjglNode parent;
    private List<LwjglNode> children;
    private List<Double> timeOfFrames = new ArrayList<>();
    private Affine affine = new Affine(); 
    
    public LwjglNode(String name, LwjglNode parent) {
        this.name = name;
        this.parent = parent;
        this.transformations = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public static Matrix4f getParentTransforms(LwjglNode node, int framePos) {
        if (node == null) {
            return new Matrix4f();
        } else {
            Matrix4f parentTransform = new Matrix4f(getParentTransforms(node.getParent(), framePos));
            List<Matrix4f> transformations = node.getTransformations();
            Matrix4f nodeTransform;
            if (framePos < transformations.size()) {
                nodeTransform = transformations.get(framePos);
            } else {
                nodeTransform = new Matrix4f();
            }
            return parentTransform.mul(nodeTransform);
        }
    }

    public void addChild(LwjglNode node) {
        this.children.add(node);
    }

    public void addTransformation(Matrix4f transformation) {
        transformations.add(transformation);
    }

    public LwjglNode findByName(String targetName) {
    	LwjglNode result = null;
        if (this.name.equals(targetName)) {
            result = this;
        } else {
            for (LwjglNode child : children) {
                result = child.findByName(targetName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    public int getAnimationFrames() {
        int numFrames = this.transformations.size();
        for (LwjglNode child : children) {
            int childFrame = child.getAnimationFrames();
            numFrames = Math.max(numFrames, childFrame);
        }
        return numFrames;
    }

    public List<LwjglNode> getChildren() {
        return children;
    }

    public List<Matrix4f> getTransformations() {
        return transformations;
    }

    public String getName() {
        return name;
    }

    public LwjglNode getParent() {
        return parent;
    }
	
    public List<Double> getTimeOfFrames() {
		return timeOfFrames;
	}
	
    public void setTimeOfFrames(List<Double> timeOfFrames) {
		this.timeOfFrames = timeOfFrames;
	}

	public Affine getAffine() {
		return affine;
	}

	public void setAffine(Affine affine) {
		try {
			affine.invert();
		} catch (NonInvertibleTransformException e) {
			e.printStackTrace();
		}
		this.affine = affine;
	}

    
}
