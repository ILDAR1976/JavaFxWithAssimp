/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.lwjgl_fx_01.ui.model.engine.loaders.assimp;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Joint -  A Joint is equivalent to a Maya Joint Node
 * <p/>
 * If you are post-multiplying matrices, To transform a point p from object-space to world-space you would need to
 * post-multiply by the worldMatrix. (p' = p * wm) matrix = [S][SO][R][JO][IS][T] where R = [RX][RY][RZ]  (Note: order
 * is determined by rotateOrder)
 * <p/>
 * If you are pre-multiplying matrices, to transform a point p from object-space to world-space you would need to
 * pre-multiply by the worldMatrix. (p' = wm * p) matrix = [T][IS][JO][R][SO][S] where R = [RZ][RY][RX]  (Note: order is
 * determined by rotateOrder) Of these sub-matrices we can set [SO] to identity, so matrix = [T][IS][JO][R][S]
 */
@SuppressWarnings("restriction")
public final class Joint extends Group {
    private int globalId;
	final Translate t = new Translate();

    final Rotate jox = new Rotate();
    final Rotate joy = new Rotate();
    final Rotate joz = new Rotate();

    final Rotate rx = new Rotate();
    final Rotate ry = new Rotate();
    final Rotate rz = new Rotate();

    final Scale s = new Scale();
    final Scale is = new Scale();
    // should bind "is" to be in the inverse of the parent's "s"

    public Affine a = new Affine();
	
    
    //experiment fields
    
    private List<Matrix4f> transformations = new ArrayList<>();
    private Matrix4f offset;
    
	public Joint() {
        super();

        this.joz.setAxis(Rotate.Z_AXIS);
        this.joy.setAxis(Rotate.Y_AXIS);
        this.jox.setAxis(Rotate.X_AXIS);

        this.rx.setAxis(Rotate.X_AXIS);
        this.ry.setAxis(Rotate.Y_AXIS);
        this.rz.setAxis(Rotate.Z_AXIS);

        this.getTransforms().addAll(t, is, joz, joy, jox, rz, ry, rx, s, a);
        

    }

	private Image generateMap(WritableImage writableImage, Color color) {
		// Copy from source to destination pixel by pixel
		PixelWriter pixelWriter = writableImage.getPixelWriter();

		for (int y = 0; y < writableImage.getHeight(); y++) {
			for (int x = 0; x < writableImage.getWidth(); x++) {
				pixelWriter.setColor(x, y, color);
			}
		}
		return writableImage;
	}

    public TriangleMesh createCubeMesh() {

        float width = 4.1f / 2f;
        float points[] = {
                -width, -width, -width,
                width, -width, -width,
                width, width, -width,
                -width, width, -width,
                -width, -width, width,
                width, -width, width,
                width, width, width,
                -width, width, width};

        float texCoords[] = {0, 0, 1, 0, 1, 1, 0, 1};

        int faceSmoothingGroups[] = {
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };

        int faces[] = {
                0, 0, 2, 2, 1, 1,
                2, 2, 0, 0, 3, 3,
                1, 0, 6, 2, 5, 1,
                6, 2, 1, 0, 2, 3,
                5, 0, 7, 2, 4, 1,
                7, 2, 5, 0, 6, 3,
                4, 0, 3, 2, 0, 1,
                3, 2, 4, 0, 7, 3,
                3, 0, 6, 2, 2, 1,
                6, 2, 3, 0, 7, 3,
                4, 0, 1, 2, 5, 1,
                1, 2, 4, 0, 0, 3,
        };

        final TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(points);
        mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(faces);
        mesh.getFaceSmoothingGroups().setAll(faceSmoothingGroups);

        return mesh;
    }

    public void addMeshView() {
	    int width = 100;
	    int height = 100;
    	final WritableImage diffuseMap = new WritableImage(width, height);
        final WritableImage specularMap = new WritableImage(width, height);
        final WritableImage selfIllumMap = new WritableImage(width, height);
        generateMap(diffuseMap, Color.GRAY);
        generateMap(specularMap, Color.ANTIQUEWHITE);
        final PhongMaterial sharedMaterial = new PhongMaterial();
        final PhongMaterial sharedMapMaterial = new PhongMaterial();
        sharedMapMaterial.setDiffuseMap(diffuseMap);
        sharedMapMaterial.setSpecularMap(specularMap);
        final MeshView meshView = new MeshView(createCubeMesh());
        meshView.setMaterial(sharedMapMaterial);
        getChildren().add(meshView);
    }

	public int getGlobalId() {
		return globalId;
	}

	public void setGlobalId(int id) {
		this.globalId = id;
	}

	public List<Matrix4f> getTransformations() {
		return transformations;
	}

	public void setTransformations(List<Matrix4f> transformations) {
		this.transformations = transformations;
	}

	public Matrix4f getOffset() {
		return offset;
	}

	public void setOffset(Matrix4f offset) {
		this.offset = offset;
	}

	
}
