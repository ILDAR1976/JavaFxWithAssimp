package edu.lwjgl_fx_01.ui.model.engine.graph;

import javafx.scene.Camera;
import javafx.scene.paint.Material;
import javafx.scene.shape.TriangleMesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.lwjgl_fx_01.ui.controller.ModelController;

/**
 * Created by Eclion.
 */
@SuppressWarnings("restriction")
public final class BuildHelper {
    private final Map<String, Material> materialMap = new HashMap<>();
    private final Map<String, List<TriangleMesh>> meshes = new HashMap<>();
    private final Map<String, List<String>> meshMaterialIds = new HashMap<>();
    private final Map<String, ModelController> controllers = new HashMap<>();
    private final Map<String, Skeleton> skeletons = new HashMap<>();
    private final Map<String, Camera> cameras = new HashMap<>();

    public BuildHelper withMeshes(final Map<String, List<TriangleMesh>> meshes) {
        this.meshes.putAll(meshes);
        return this;
    }

    public BuildHelper withMeshMaterialIds(final Map<String, List<String>> meshMaterialIds) {
        this.meshMaterialIds.putAll(meshMaterialIds);
        return this;
    }

    public BuildHelper withMaterialMap(final Map<String, Material> materialMap) {
        this.materialMap.putAll(materialMap);
        return this;
    }

    public BuildHelper withControllers(final Map<String, ModelController> controllers) {
        this.controllers.putAll(controllers);
        return this;
    }

    public BuildHelper withSkeletons(final Map<String, Skeleton> skeletons) {
        this.skeletons.putAll(skeletons);
        return this;
    }

    List<TriangleMesh> getMeshes(final String geometryId) {
        return meshes.get(geometryId);
    }

    List<Material> getMaterials(final String geometryId) {
        return meshMaterialIds.getOrDefault(geometryId, new ArrayList<>()).
                stream().
                map(materialMap::get).
                collect(Collectors.toList());
    }

    ModelController getController(final String controllerId) {
        return controllers.get(controllerId);
    }

    Skeleton getSkeleton(final String skeletonId) {
        return skeletons.get(skeletonId);
    }

    public BuildHelper withCameras(final Map<String, Camera> cameras) {
        this.cameras.putAll(cameras);
        return this;
    }

    Camera getCamera(final String instanceCameraId) {
        return cameras.get(instanceCameraId);
    }
}
