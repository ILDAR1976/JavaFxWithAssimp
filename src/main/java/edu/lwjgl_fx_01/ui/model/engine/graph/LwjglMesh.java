package edu.lwjgl_fx_01.ui.model.engine.graph;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import static edu.lwjgl_fx_01.ui.utils.Utils.*;

import edu.lwjgl_fx_01.ui.model.engine.graph.LwjglMesh.ConvertPoints;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.Bone;

public class LwjglMesh {

    public static final int MAX_WEIGHTS = 4;
	public String name;
    //protected final int vaoId;
    protected final List<Integer> vboIdList;
    private final int vertexCount;
    private final int bonesCount;
    private LwjglMaterial material;
    private float boundingRadius;
    private List<Bone> bones = new ArrayList<>();
    private List<Vector3f> verteices = new ArrayList<>();
    private List<Vector3f> normals = new ArrayList<>();
	private List<Vector2f> textureCoordianates = new ArrayList<>();
	private List<Integer> indicies = new ArrayList<>();
	private List<Float> weights = new ArrayList<>();
	private List<Integer> jointIndices = new ArrayList<>();
	private List<Integer> vboIndices = new ArrayList<>();
	
    private float[] pointsFx;
    private float[] normalsFx;
	private float[] textureCoordianatesFx;
	private float[][] bonesVerteicesWeigth;
	private float[][] bonesPointsWeigth2Fx;
	private float[][] bonesPointsWeigth3Fx;
	
	private int[] faces3Fx;
	private int[] faces2Fx;

	// Working collections
	private Set<Vector3f> uniquePointsList = new LinkedHashSet<Vector3f>();
	private List<Vector3f> pointsList = new ArrayList<Vector3f>();

	private Set<Vector3f> uniqueNormalsList = new LinkedHashSet<Vector3f>();
	private List<Vector3f> normalsList = new ArrayList<Vector3f>();

	private Set<Vector2f> uniqueTexCoordinatesList = new LinkedHashSet<Vector2f>();
	private List<Vector2f> texCoordinatesList = new ArrayList<Vector2f>();

	
	private float[] mPositions;
	private float[] mTextureCoordinates;
	private float[] mNormals;
	private int[] mIndicies;
	
	int indexPoint;
	int indexNormal;
	int indexTexture;
	int indexFace2;
	int indexFace3;
	
	
    public LwjglMesh(float[] positions, float[] textCoords, float[] normals, int[] indicies, float[][] bonesVerteicesWeigth, List<Integer> currnetBone) {
        this(positions, textCoords, normals, indicies, bonesVerteicesWeigth, currnetBone, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), 
        	 createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0));
    }

    public LwjglMesh(float[] positions, float[] textCoords, float[] normals, int[] indicies, float[][] bonesVerteicesWeigth, List<Integer> currentBone, int[] jointIndices, float[] weights) {
         try {
            calculateBoundingRadius(positions);
            
            vertexCount = indicies.length;
            this.bonesCount = currentBone.get(0);
            
            vboIdList = new ArrayList();

        	mPositions = positions;
        	mTextureCoordinates = textCoords;
        	mNormals = normals;
        	mIndicies = indicies;
            
            verteices = getVector3fListFromFloatArray(positions);
            
            textureCoordianates = getVector2fListFromFloatArray(textCoords);
            
            this.normals = getVector3fListFromFloatArray(normals);

            this.weights = getFloatListFromFloatArray(weights);
            
            this.indicies = getIntegerListFromIntegerArray(indicies);
            
            this.jointIndices = getIntegerListFromIntegerArray(jointIndices);
            
            this.vboIndices = getIntegerListFromIntegerArray(indicies);

    		this.bonesVerteicesWeigth = bonesVerteicesWeigth;
    		
            calculateElementsFxDirect(); 
            
        } finally {
        
        }
    }
    
    private void calculateBoundingRadius(float positions[]) {
        int length = positions.length;
        boundingRadius = 0;
        for(int i=0; i< length; i++) {
            float pos = positions[i];
            boundingRadius = Math.max(Math.abs(pos), boundingRadius);
        }
    }

    public LwjglMaterial getMaterial() {
        return material;
    }

    public void setMaterial(LwjglMaterial material) {
        this.material = material;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public float getBoundingRadius() {
        return boundingRadius;
    }

    public void setBoundingRadius(float boundingRadius) {
        this.boundingRadius = boundingRadius;
    }
  
    protected static float[] createEmptyFloatArray(int length, float defaultValue) {
        float[] result = new float[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    protected static int[] createEmptyIntArray(int length, int defaultValue) {
        int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Bone> getBones() {
		return bones;
	}

	public void setBones(List<Bone> bones) {
		this.bones = bones;
	}

	public List<Vector3f> getVerteices() {
		return verteices;
	}

	public void setVerteices(List<Vector3f> verteices) {
		this.verteices = verteices;
	}

	public List<Vector3f> getNormals() {
		return normals;
	}

	public void setNormals(List<Vector3f> normals) {
		this.normals = normals;
	}

	public List<Vector2f> getTextureCoordianates() {
		return textureCoordianates;
	}

	public void setTextureCoordianates(List<Vector2f> textureCoordianates) {
		this.textureCoordianates = textureCoordianates;
	}

	public List<Integer> getIndicies() {
		return indicies;
	}

	public void setIndicies(List<Integer> indecies) {
		this.indicies = indecies;
	}

	public List<Float> getWeights() {
		return weights;
	}

	public void setWeights(List<Float> weights) {
		this.weights = weights;
	}

	public List<Integer> getJointIndices() {
		return jointIndices;
	}

	public void setJointIndices(List<Integer> jointIndices) {
		this.jointIndices = jointIndices;
	}

	public List<Integer> getVboIdList() {
		return vboIdList;
	}

	public float[] getmPositions() {
		return mPositions;
	}

	public void setmPositions(float[] mPositions) {
		this.mPositions = mPositions;
	}

	public float[] getmTextureCoordinates() {
		return mTextureCoordinates;
	}

	public void setmTextureCoordinates(float[] mTextureCoordinates) {
		this.mTextureCoordinates = mTextureCoordinates;
	}

	public float[] getmNormals() {
		return mNormals;
	}

	public void setmNormals(float[] mNormals) {
		this.mNormals = mNormals;
	}

	public int[] getmindicies() {
		return mIndicies;
	}

	public void setmIndicies(int[] mIndicies) {
		this.mIndicies = mIndicies;
	}

	public float[] getPointsFx() {
		return pointsFx;
	}

	public void setPointsFx(float[] pointsFx) {
		this.pointsFx = pointsFx;
	}

	public float[] getNormalsFx() {
		return normalsFx;
	}

	public void setNormalsFx(float[] normalsFx) {
		this.normalsFx = normalsFx;
	}

	public float[] getTextureCoordianatesFx() {
		return textureCoordianatesFx;
	}

	public void setTextureCoordianatesFx(float[] textureCoordianatesFx) {
		this.textureCoordianatesFx = textureCoordianatesFx;
	}

	public int[] getFaces3Fx() {
		return faces3Fx;
	}

	public void setFaces3Fx(int[] faces3Fx) {
		this.faces3Fx = faces3Fx;
	}

	public int[] getFaces2Fx() {
		return faces2Fx;
	}

	public void setFaces2Fx(int[] faces2Fx) {
		this.faces2Fx = faces2Fx;
	}

	private void calculateElementsFxDirect() {

		List<Vector3f> _pl = new ArrayList<>();
		List<Vector3f> _nl = new ArrayList<>();
		List<Vector2f> _tcl = new ArrayList<>();
		
		float pvx = 0f, pvy = 0f, pvz = 0f;
		float nvx = 0f, nvy = 0f, nvz = 0f;
		float tvx = 0f, tvy = 0f;		
				
		int pointsQty = verteices.size();
		
		for (int i = 0; i < pointsQty; i++) {
				if (verteices.size() == pointsQty) pvx = verteices.get(i).x(); else pvx = 0;
				if (normals.size() == pointsQty) nvx = normals.get(i).x(); else nvx = 0;
				if (verteices.size() == pointsQty) pvy = verteices.get(i).y(); else pvy = 0;
				if (normals.size() == pointsQty) nvy = normals.get(i).y(); else nvy = 0;
				if (verteices.size() == pointsQty) pvz = verteices.get(i).z(); else pvz = 0;
				if (normals.size() == pointsQty) nvz = normals.get(i).z(); else nvz = 0;

				_pl.add(new Vector3f(toSFN(pvx), toSFN(pvy), toSFN(pvz)));
				_nl.add(new Vector3f(toSFN(nvx), toSFN(nvy), toSFN(nvz)));
		}

		if (textureCoordianates == null) textureCoordianates = new ArrayList<Vector2f>();
		
		int textureQty = textureCoordianates.size();
		
		for (int i = 0; i < textureQty; i++) {
				if (textureCoordianates.size() == textureQty) tvx = textureCoordianates.get(i).x(); else tvx = 0;
				if (textureCoordianates.size() == textureQty) tvy = textureCoordianates.get(i).y(); else tvy = 0;
				_tcl.add(new Vector2f(toSFN(tvx), toSFN(tvy)));
		}

		this.pointsFx = new float[_pl.size() * 3 ];
		this.normalsFx = new float[_nl.size() * 3 ];
		if (this.textureCoordianates != null) this.textureCoordianatesFx = new float[_tcl.size() * 3 ];

		
		Object[] pl = _pl.toArray();
		Object[] nl = _nl.toArray();
		Object[] tcl = _tcl.toArray();


		this.faces2Fx = new int[this.indicies.size() * 2];
		this.faces3Fx = new int[this.indicies.size() * 3];
		
		int index = 0;
		
		bonesPointsWeigth2Fx = new float[bonesCount][_pl.size()];
		bonesPointsWeigth3Fx = new float[bonesCount][_pl.size()];
		
		int[] ind = indicies.stream().mapToInt(v->v).toArray();
		
				
		for (int i : indicies) {
			//System.out.println(i);
			this.faces3Fx[index++] = i;
			this.faces3Fx[index++] = i;
			this.faces3Fx[index++] = (tcl.length != 0) ? i : 0;
			
			for (int j = 0; j < bonesCount; j++) {
				bonesPointsWeigth3Fx[j][i] = bonesVerteicesWeigth[j][i];
			}
		}
		
		index = 0;
		
		for (int i : indicies) {
			this.faces2Fx[index++] = i;
			this.faces2Fx[index++] = (tcl.length != 0) ? i : 0;
			
			for (int j = 0; j < bonesCount; j++) {
				bonesPointsWeigth2Fx[j][i] = bonesVerteicesWeigth[j][i];
			}
		}
		
		this.pointsFx = new float[pl.length * 3];
		this.normalsFx = new float[nl.length * 3];
		if (this.textureCoordianates != null) this.textureCoordianatesFx = new float[tcl.length * 3];
		
		index = 0;
		
		for (int i=0; i<pl.length; i++) {
			this.pointsFx[index++] = ((Vector3f)pl[i]).x;
			this.pointsFx[index++] = ((Vector3f)pl[i]).y;
			this.pointsFx[index++] = ((Vector3f)pl[i]).z;
		}

		index = 0;
		
		for (int i=0; i<nl.length; i++) {
			this.normalsFx[index++] = ((Vector3f)nl[i]).x;
			this.normalsFx[index++] = ((Vector3f)nl[i]).y;
			this.normalsFx[index++] = ((Vector3f)nl[i]).z;
		}

		index = 0;
		 
		for (int i=0; i<tcl.length; i++) {
			this.textureCoordianatesFx[index++] = ((Vector2f)tcl[i]).x;
			this.textureCoordianatesFx[index++] = ((Vector2f)tcl[i]).y;
		}
	}

	public float[][] getBonesPointsWeigth2Fx() {
		return bonesPointsWeigth2Fx;
	}
	
	public void setBonesPointsWeigth2Fx(float[][] bonesPointsWeigthFx) {
		this.bonesPointsWeigth2Fx = bonesPointsWeigthFx;
	}
	
	public float[][] getBonesPointsWeigth3Fx() {
		return bonesPointsWeigth3Fx;
	}
	
	public void setBonesPointsWeigth3Fx(float[][] bonesPointsWeigthFx) {
		this.bonesPointsWeigth3Fx = bonesPointsWeigthFx;
	}

	public class ConvertPoints {
		public Vector3f point;
		public Vector3f normal;
		public Vector2f texture;
		
		public int uniqueIndex;
		public int oldIndex;
	}
}
