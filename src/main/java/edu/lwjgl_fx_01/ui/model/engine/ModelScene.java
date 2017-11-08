package edu.lwjgl_fx_01.ui.model.engine;

import edu.lwjgl_fx_01.ui.model.engine.graph.BuildHelper;
import edu.lwjgl_fx_01.ui.model.engine.graph.Skeleton;
import edu.lwjgl_fx_01.ui.utils.Utils;
import javafx.scene.Group;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eclion, Iha
 */
public final class ModelScene extends Group {
    public Map<String, Skeleton> skeletons = new HashMap<>();

    public ModelScene(final String id) {
        this.setId(id);
    }

    public void build(final BuildHelper buildHelper) {
        Utils.getModelNodeChildStream(this).
                forEach(child -> child.build(buildHelper));
    }

	public Map<String, Skeleton> getSkeletons() {
		return skeletons;
	}

	public void setSkeletons(Map<String, Skeleton> skeletons) {
		this.skeletons = skeletons;
	}
    
    
}
