package edu.lwjgl_fx_01.ui.model.engine.graph.animation;

import java.util.Arrays;

import org.joml.Matrix4f;
import javafx.scene.transform.Affine;

@SuppressWarnings({ "unused", "restriction" })
public class AnimatedFrame {

    private static final Affine IDENTITY_MATRIX = new Affine();

    public static final int MAX_JOINTS = 600;

    private final Affine[] jointMatrices;

    public AnimatedFrame() {
        jointMatrices = new Affine[MAX_JOINTS];
        Arrays.fill(jointMatrices, IDENTITY_MATRIX);
    }

    public Affine[] getJointMatrices() {
        return jointMatrices;
    }

    public void setMatrix(int pos,Affine jointMatrix) {
        jointMatrices[pos] = jointMatrix;
    }
}

