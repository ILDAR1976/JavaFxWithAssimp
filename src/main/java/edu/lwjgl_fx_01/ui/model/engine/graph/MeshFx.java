package edu.lwjgl_fx_01.ui.model.engine.graph;


import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import static edu.lwjgl_fx_01.ui.utils.Utils.*;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.JointFx;
import javafx.scene.transform.Affine;
import javafx.scene.Group;

import static edu.lwjgl_fx_01.ui.utils.Utils.*;

public class MeshFx extends Group{
	private float[] points;
	private float[] normals;
	private float[] texCoords;
	private int[]  faces;
	private final float[][] jointsPointsWeights; 
	private final Affine baseTransformMatrix;
	private final Affine[] bindPosList;
	private final List<JointFx> jointList;
	
	public MeshFx( 		
			final float[] points,
    		final float[] normals,
    		final float[] texCoords,
    		final int[]  faces,
    		final float[][] jointsPointsWeights,
			final Affine baseTransformMatrix,
    		final Affine[] bindPosList,
    		final List<JointFx> jointList
    ) {
		
		this.points = points;
		this.normals = normals;
		this.texCoords = texCoords;
		this.faces = faces;
		this.jointsPointsWeights = jointsPointsWeights;
		this.baseTransformMatrix = baseTransformMatrix;
		this.bindPosList = bindPosList;
		this.jointList = jointList;
		
		calculateElementsFxDirect(); 
	}

	private void calculateElementsFxDirect() {

		List<Vector3f> _pl = new ArrayList<>();
		List<Vector3f> _nl = new ArrayList<>();
		List<Vector2f> _tcl = new ArrayList<>();
		List<Integer> _fl = new ArrayList<>();
		
		float pvx = 0f, pvy = 0f, pvz = 0f;
		float nvx = 0f, nvy = 0f, nvz = 0f;
		float tvx = 0f, tvy = 0f;		
				
		int pointsQty = points.length;
		int texCoordsQty = texCoords.length;
		
		for (int i = 0; i < pointsQty; i++) {
				if (i % 3 == 0) {
					pvx = points[i];
					nvx = normals[i];
				} else if (i % 3 == 1) {
					pvy = points[i];
					nvy = normals[i];
				} else if (i % 3 == 2) {
					pvz = points[i];
					nvz = normals[i];

					_pl.add(new Vector3f(toSFN(pvx), toSFN(pvy), toSFN(pvz)));
					_nl.add(new Vector3f(toSFN(nvx), toSFN(nvy), toSFN(nvz)));
				}
		}
		
		for (int i = 0; i < texCoordsQty; i++) {
			if (i % 2 == 0) {
				tvx = texCoords[i];
			} else if (i % 2 == 1) {
				tvy = texCoords[i];
				_tcl.add(new Vector2f(toSFN(tvx), toSFN(tvy)));
			}	
		}

		for (int i = 0; i < faces.length; i++) {
			_fl.add(faces[i]);
		}
		
		this.points = new float[_pl.size() * 3 ];
		this.normals = new float[_nl.size() * 3 ];
		if (texCoordsQty != 0) this.texCoords = new float[_tcl.size() * 3 ];
		
		Object[] pl = _pl.toArray();
		Object[] nl = _nl.toArray();
		Object[] tcl = _tcl.toArray();

		this.faces = new int[this.faces.length * 3];
		int index = 0;

		for (int i : _fl) {
			//System.out.println(i);
			this.faces[index++] = i;
			this.faces[index++] = i;
			this.faces[index++] = (tcl.length != 0) ? i : 0;
			
		}
		
		index = 0;
		
		for (int i=0; i<pl.length; i++) {
			this.points[index++] = ((Vector3f)pl[i]).x;
			this.points[index++] = ((Vector3f)pl[i]).y;
			this.points[index++] = ((Vector3f)pl[i]).z;
		}

		index = 0;
		
		for (int i=0; i<nl.length; i++) {
			this.normals[index++] = ((Vector3f)nl[i]).x;
			this.normals[index++] = ((Vector3f)nl[i]).y;
			this.normals[index++] = ((Vector3f)nl[i]).z;
		}

		index = 0;
		 
		for (int i=0; i<tcl.length; i++) {
			this.texCoords[index++] = ((Vector2f)tcl[i]).x;
			this.texCoords[index++] = ((Vector2f)tcl[i]).y;
		}
	}

	public float[] getPoints() {
		return points;
	}

	public void setPoints(float[] points) {
		this.points = points;
	}

	public float[] getNormals() {
		return normals;
	}

	public void setNormals(float[] normals) {
		this.normals = normals;
	}

	public float[] getTexCoords() {
		return texCoords;
	}

	public void setTexCoords(float[] texCoords) {
		this.texCoords = texCoords;
	}

	public int[] getFaces() {
		return faces;
	}

	public void setFaces(int[] faces) {
		this.faces = faces;
	}

	public float[][] getJointsPointsWeights() {
		return jointsPointsWeights;
	}

	public Affine[] getBindPosList() {
		return bindPosList;
	}

	public List<JointFx> getJointList() {
		return jointList;
	}

	public Affine getBaseTransformMatrix() {
		return baseTransformMatrix;
	}
}
