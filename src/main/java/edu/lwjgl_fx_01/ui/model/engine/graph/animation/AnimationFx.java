package edu.lwjgl_fx_01.ui.model.engine.graph.animation;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.value.WritableValue;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.Skeleton;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.JointFx;

/**
 * @author Eclion
 */
@SuppressWarnings({ "restriction", "unused" })
public final class AnimationFx {

    //ratio set for the animation used for testing.
    private static final int TIMER_MS_RATIO = 1000;

    public final String id;
    private float[] input;
    private double[] output;
    private Interpolator[] interpolators;
    private String target; 
    private final List<AnimationFx> childAnimations = new ArrayList<>();
    
    public AnimationFx(final String id) {
        this.id = id;
    }

    public List<KeyFrame> calculateAnimation(final Skeleton skeleton) {
        final List<KeyFrame> keyFrames = new ArrayList<>();
       
        Optional.ofNullable(this.target).
		        map(t -> t).
		        map(skeleton.joints::get).
		        map(this::calculateJointAnimation).
		        ifPresent(keyFrames::addAll);

        this.childAnimations.stream().
                map(animation -> animation.calculateAnimation(skeleton)).
                forEach(keyFrames::addAll);

        return keyFrames;
    }

    @SuppressWarnings("restriction")
	private List<KeyFrame> calculateJointAnimation(JointFx joint) {
        final List<KeyFrame> keyFrames = new ArrayList<>();
        int a = 0;
        for (int i = 0; i < this.input.length; i++) {
            final Affine keyAffine = new Affine(this.output, MatrixType.MT_3D_4x4, i * 16);
            keyFrames.add(this.convertToKeyFrame(this.input[i] * TIMER_MS_RATIO, joint.getAffine(), keyAffine, Interpolator.LINEAR));
            a = 0;
        }
        return keyFrames;
    }

    private KeyFrame convertToKeyFrame(final float t, final Affine jointAffine, final Affine keyAffine, final Interpolator interpolator) {
        final Duration duration = new Duration(t);
        final List<KeyValue> kvs = convertToKeyValues(jointAffine, keyAffine, interpolator);
        final KeyValue[] kvs2 = kvs.toArray(new KeyValue[kvs.size()]);
        return new KeyFrame(duration, kvs2);
    }

    private KeyValue convertToKeyValue(final WritableValue<Number> target, final Number endValue, final Interpolator interpolator) {
        return new KeyValue(target, endValue, interpolator);
    }

    @SuppressWarnings("restriction")
	private List<KeyValue> convertToKeyValues(final Affine jointAffine, final Affine keyAffine, final Interpolator interpolator) {
        final List<KeyValue> keyValues = new ArrayList<>();
        keyValues.add(convertToKeyValue(jointAffine.mxxProperty(), keyAffine.getMxx(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.mxyProperty(), keyAffine.getMxy(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.mxzProperty(), keyAffine.getMxz(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.myxProperty(), keyAffine.getMyx(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.myyProperty(), keyAffine.getMyy(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.myzProperty(), keyAffine.getMyz(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.mzxProperty(), keyAffine.getMzx(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.mzyProperty(), keyAffine.getMzy(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.mzzProperty(), keyAffine.getMzz(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.txProperty(), keyAffine.getTx(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.tyProperty(), keyAffine.getTy(), interpolator));
        keyValues.add(convertToKeyValue(jointAffine.tzProperty(), keyAffine.getTz(), interpolator));
        return keyValues;
    }

    public void setInterpolations(final String[] interpolations) {
        this.interpolators = new Interpolator[interpolations.length];
        for (int i = 0; i < interpolations.length; ++i) {
            interpolators[i] = Interpolator.LINEAR;
        }
    }

    public void addChild(final AnimationFx animation) {
        childAnimations.add(animation);
    }

    public void setInput(final float[] input) {
        this.input = input;
    }

    public float[] getInput() {
        return input;
    }

    public void setOutput(final double[] output) {
        this.output = output;
    }

    public double[] getOutput() {
        return output;
    }

    public void setTarget(final String target) {
        this.target = target;
    }
    
    public String getTarget() {
        return target;
    }

}