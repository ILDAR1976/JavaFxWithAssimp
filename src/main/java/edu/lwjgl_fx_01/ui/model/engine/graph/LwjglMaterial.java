package edu.lwjgl_fx_01.ui.model.engine.graph;

import org.joml.Vector4f;

public class LwjglMaterial {

    public static final Vector4f DEFAULT_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private Vector4f ambientColour;

    private Vector4f diffuseColour;

    private Vector4f specularColour;
    
    private float shininess;

    private float reflectance;

    private LwjglTexture texture;
    
    private LwjglTexture normalMap;

    public LwjglMaterial() {
        this.ambientColour = DEFAULT_COLOUR;
        this.diffuseColour = DEFAULT_COLOUR;
        this.specularColour = DEFAULT_COLOUR;
        this.texture = null;
        this.reflectance = 0;
    }

    public LwjglMaterial(Vector4f colour, float reflectance) {
        this(colour, colour, colour, null, reflectance);
    }

    public LwjglMaterial(LwjglTexture texture) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, 0);
    }

    public LwjglMaterial(LwjglTexture texture, float reflectance) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, reflectance);
    }

    public LwjglMaterial(Vector4f ambientColour, Vector4f diffuseColour, Vector4f specularColour, float reflectance) {
        this(ambientColour, diffuseColour, specularColour, null, reflectance);
    }

    public LwjglMaterial(Vector4f ambientColour, Vector4f diffuseColour, Vector4f specularColour, LwjglTexture texture, float reflectance) {
        this.ambientColour = ambientColour;
        this.diffuseColour = diffuseColour;
        this.specularColour = specularColour;
        this.texture = texture;
        this.reflectance = reflectance;
    }

    public Vector4f getAmbientColour() {
        return ambientColour;
    }

    public void setAmbientColour(Vector4f ambientColour) {
        this.ambientColour = ambientColour;
    }

    public Vector4f getDiffuseColour() {
        return diffuseColour;
    }

    public void setDiffuseColour(Vector4f diffuseColour) {
        this.diffuseColour = diffuseColour;
    }

    public Vector4f getSpecularColour() {
        return specularColour;
    }

    public void setSpecularColour(Vector4f specularColour) {
        this.specularColour = specularColour;
    }

    public float getReflectance() {
        return reflectance;
    }

    public void setReflectance(float reflectance) {
        this.reflectance = reflectance;
    }

    public boolean isTextured() {
        return this.texture != null;
    }

    public LwjglTexture getTexture() {
        return texture;
    }

    public void setTexture(LwjglTexture texture) {
        this.texture = texture;
    }
    
    public boolean hasNormalMap() {
        return this.normalMap != null;
    }

    public LwjglTexture getNormalMap() {
        return normalMap;
    }

    public void setNormalMap(LwjglTexture normalMap) {
        this.normalMap = normalMap;
    }
}