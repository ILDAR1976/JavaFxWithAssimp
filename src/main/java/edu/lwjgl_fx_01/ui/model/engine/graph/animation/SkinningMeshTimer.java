package edu.lwjgl_fx_01.ui.model.engine.graph.animation;

import edu.lwjgl_fx_01.ui.model.engine.shape3d.SkinningMesh;
import javafx.animation.AnimationTimer;

/**
 * @author Eclion
 */
@SuppressWarnings("restriction")
public final class SkinningMeshTimer extends AnimationTimer {
    private final SkinningMesh mesh;

    public SkinningMeshTimer(final SkinningMesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public void handle(final long l) {
        mesh.update();
    }
}