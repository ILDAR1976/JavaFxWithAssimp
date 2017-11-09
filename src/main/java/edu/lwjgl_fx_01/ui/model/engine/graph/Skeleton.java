package edu.lwjgl_fx_01.ui.model.engine.graph;

import javafx.scene.Parent;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.lwjgl_fx_01.ui.utils.Utils.*;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.Joint;

/**
 * @author Eclion
 */
@SuppressWarnings({ "restriction", "unused" })
public final class Skeleton extends Parent {

    public Map<String, Joint> joints = new LinkedHashMap<>();
    private final Map<String, Affine> bindTransforms = new LinkedHashMap<>();
    private static int c;
    public Skeleton(final String id) {
        setId(id);
    }

    public static Skeleton fromModelNode(final ModelNode rootNode) {
        final Skeleton skeleton = new Skeleton(rootNode.getId());

        // should the rootNode transforms be local to the parent or global?
        skeleton.getTransforms().addAll(rootNode.getTransforms());

        final List<ModelNode> rootModelNodes = new ArrayList<>();
        
        rootModelNodes.addAll(rootNode.getModelNodeChildStream().
                filter(ModelNode::isJoint).
                collect(Collectors.toList()));
        
        skeleton.getChildren().addAll(buildBone(rootModelNodes, skeleton.joints, skeleton.bindTransforms));
       
        return skeleton;
    }

    private static List<Joint> buildBone(final List<ModelNode> modelNodes, final Map<String, Joint> joints, final Map<String, Affine> bindTransforms) {
    	return modelNodes.stream().
                map(node -> {
                    final Joint joint = createJointFromNode(node);
                    joints.put(joint.getId(), joint);
                    bindTransforms.put(joint.getId(), joint.a);
                    final List<ModelNode> children = node.getModelNodeChildStream().collect(Collectors.toList());
                    joint.getChildren().addAll(buildBone(children, joints, bindTransforms));
                    return joint;
                }).
                collect(Collectors.toList());
    }

    private static Joint createJointFromNode(final ModelNode node) {
        final Joint joint = new Joint();
        //System.out.println("joint create: " + node.name);
        joint.setId(node.getId().trim());
        joint.setTransformations(node.getTransformations());
        node.getTransforms().stream().
                filter(transform -> transform instanceof Affine).
                findFirst().
                ifPresent(joint.a::setToTransform);

        return joint;
    }

	public Map<String, Joint> getJoints() {
		return joints;
	}

	public Map<String, Affine> getBindTransforms() {
		return bindTransforms;
	}
	
	public void setJoints(Map<String, Joint> joints) {
		this.joints = joints;
	}
	
	
	
}
