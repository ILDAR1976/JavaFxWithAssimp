package edu.lwjgl_fx_01.ui.model.engine.graph;

import org.joml.Vector4f;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.AmbientLight;
import javafx.scene.image.Image;

@SuppressWarnings("restriction")
public class MaterialFx {

    public static final Vector4f DEFAULT_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private Vector4f ambientColour;
    private Vector4f diffuseColour;
    private Vector4f specularColour;
    private float shininess;
    private float reflectance;
    private TextureFx texture;
    private TextureFx normalMap;
    private PhongMaterial phongMaterial;
    private AmbientLight ambientLight;
    

    public MaterialFx() {
        this.ambientColour = DEFAULT_COLOUR;
        this.diffuseColour = DEFAULT_COLOUR;
        this.specularColour = DEFAULT_COLOUR;
        this.texture = null;
        this.reflectance = 0;
        
        this.ambientLight = new AmbientLight(new Color(this.ambientColour.x, this.ambientColour.y, 
                									   this.ambientColour.z, this.ambientColour.w));
        this.phongMaterial = new PhongMaterial();
        this.phongMaterial.setDiffuseColor(new Color(this.diffuseColour.x, this.diffuseColour.y, 
        		                                     this.diffuseColour.z, this.diffuseColour.w));
        this.phongMaterial.setSpecularColor(new Color(this.specularColour.x, this.specularColour.y, 
                									  this.specularColour.z, this.specularColour.w));

    }

    public MaterialFx(Vector4f colour, float reflectance) {
        this(colour, colour, colour, null, reflectance);
    }

    public MaterialFx(TextureFx texture) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, 0);
    }

    public MaterialFx(TextureFx texture, float reflectance) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, reflectance);
    }

    public MaterialFx(Vector4f ambientColour, Vector4f diffuseColour, Vector4f specularColour, float reflectance) {
        this(ambientColour, diffuseColour, specularColour, null, reflectance);
    }

    public MaterialFx(Vector4f ambientColour, Vector4f diffuseColour, Vector4f specularColour, TextureFx texture, float reflectance) {
        this.ambientColour = ambientColour;
        this.diffuseColour = diffuseColour;
        this.specularColour = specularColour;
        this.texture = texture;
        this.reflectance = reflectance;
        this.ambientLight = new AmbientLight(new Color(this.ambientColour.x, this.ambientColour.y, 
        											   this.ambientColour.z, this.ambientColour.w));
		this.phongMaterial = new PhongMaterial();
		if (texture != null) this.phongMaterial.setDiffuseMap(texture.getImage());
		this.phongMaterial.setDiffuseColor(new Color(this.diffuseColour.x, this.diffuseColour.y, 
													 this.diffuseColour.z, this.diffuseColour.w));
		this.phongMaterial.setSpecularColor(new Color(this.specularColour.x, this.specularColour.y, 
						                              this.specularColour.z, this.specularColour.w));
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

    public TextureFx getTexture() {
        return texture;
    }

    public void setTexture(TextureFx texture) {
        this.texture = texture;
    }
    
    public boolean hasNormalMap() {
        return this.normalMap != null;
    }

    public TextureFx getNormalMap() {
        return normalMap;
    }

    public void setNormalMap(TextureFx normalMap) {
        this.normalMap = normalMap;
    }

	public PhongMaterial getPhongMaterial() {
		return phongMaterial;
	}

	public AmbientLight getAmbientLight() {
		return ambientLight;
	}
    
    
}