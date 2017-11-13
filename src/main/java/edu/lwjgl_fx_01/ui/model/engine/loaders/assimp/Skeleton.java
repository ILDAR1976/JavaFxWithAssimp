package edu.lwjgl_fx_01.ui.model.engine.loaders.assimp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.lwjgl_fx_01.ui.model.engine.graph.NodeFx;
import static edu.lwjgl_fx_01.ui.utils.Utils.*;

import javafx.scene.Parent;
import javafx.scene.transform.Affine;

@SuppressWarnings("restriction")
public class Skeleton extends Parent {
    public Map<String, JointFx> joints = new LinkedHashMap<>();
    private final Map<String, Affine> bindTransforms = new LinkedHashMap<>();
	
	public static Skeleton SetChild(final NodeFx rootNode, List<JointFx> jointList) {
        final Skeleton skeleton = new Skeleton();
        skeleton.getTransforms().addAll(rootNode.getTransforms());
        skeleton.getChildren().addAll(rootNode.getChildren());
        return skeleton;
    }
    
    public static Skeleton fromNodeFx(final NodeFx rootNode, Map<String, JointFx> jointsMap) {
        final Skeleton skeleton = new Skeleton();

        // should the rootNode transforms be local to the parent or global?
        skeleton.getTransforms().addAll(rootNode.getTransforms());

        final List<NodeFx> rootModelNodes = new ArrayList<>();
        
        rootModelNodes.addAll(rootNode.getNodeFxChildStream().
                filter(NodeFx::isJoint).
                collect(Collectors.toList()));
        
        skeleton.getChildren().addAll(buildJoint(rootModelNodes, skeleton.joints, skeleton.bindTransforms, jointsMap));
       
        return skeleton;
    }

    private static List<JointFx> buildJoint(final List<NodeFx> modelNodes, final Map<String, JointFx> joints, final Map<String, Affine> bindTransforms, Map<String, JointFx> jointsMap) {
    	return modelNodes.stream().
                map(node -> {
                    final JointFx joint = createJointFromNode(node, jointsMap);
                    joints.put(joint.getId().trim(), joint);
                    bindTransforms.put(joint.getId().trim(), joint.getAffine());
                    final List<NodeFx> children = node.getNodeFxChildStream().collect(Collectors.toList());
                    joint.getChildren().addAll(buildJoint(children, joints, bindTransforms, jointsMap));
                    return joint;
                }).
                collect(Collectors.toList());
    }

    private static JointFx createJointFromNode(final NodeFx node, Map<String, JointFx> jointsMap) {
        JointFx joint = jointsMap.get(node.getId().trim());
        
        //joint.createCubeMesh();
        //joint.addMeshView();
        
        joint.setId(node.getId().trim());
        joint.setTransformations(node.getTransformations());
        
        node.getTransforms().stream().
                filter(transform -> transform instanceof Affine).
                findFirst().
                ifPresent(joint.affine::setToTransform);

        return joint;
    }

}
