package edu.lwjgl_fx_01.basic.om;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static edu.lwjgl_fx_01.ui.utils.Utils.*;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.Joint;
import edu.lwjgl_fx_01.ui.controller.AssimpLoader;
import edu.lwjgl_fx_01.ui.model.engine.LwjglScene;
import edu.lwjgl_fx_01.ui.model.engine.ModelScene;
import edu.lwjgl_fx_01.ui.model.engine.graph.LwjglMesh;
import edu.lwjgl_fx_01.ui.model.engine.graph.ModelNode;
import edu.lwjgl_fx_01.ui.model.engine.graph.Skeleton;
import edu.lwjgl_fx_01.ui.model.engine.graph.anim.Animation;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.Bone;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.Joint;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.LwjglNode;
import javafx.application.Application;
import javafx.beans.binding.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.transform.*;
import javafx.animation.Interpolator;
import javafx.beans.value.WritableValue;
import javafx.geometry.Point3D;
import javafx.util.Duration;
import javafx.scene.control.Button;

@SuppressWarnings({ "unused", "restriction"})
public class MainFx3D3 extends Application {
	private Stage stage;
	final static Logger logger = LogManager.getLogger(MainFx3D3.class);
	private AssimpLoader assimpLoader;
	private final PerspectiveCamera camera = new PerspectiveCamera(true);
	private final Rotate cameraXRotate = new Rotate(-30, 0, 0, 0, Rotate.X_AXIS);
	// private final Rotate cameraXRotate = new Rotate(0,0,0,0,Rotate.X_AXIS);
	private final Rotate cameraYRotate = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
	// private final Rotate cameraYRotate = new Rotate(-50,0,0,0,Rotate.Y_AXIS);
	private final Rotate cameraLookXRotate = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
	private final Rotate cameraLookZRotate = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
	private final Translate cameraPosition = new Translate(0, 0, -10);
	private final DoubleProperty tempo = new SimpleDoubleProperty(100);
    
	public Affine affine = new Affine();
	public Scale scale = new Scale();
	public Translate translate = new Translate();
	public Affine rotate = new Affine();
	public Vector3f position = new Vector3f();
	public Quaternionf quaternion = new Quaternionf();
	public Vector3f scaling = new Vector3f();
	public Matrix4f matrix;
	public Rotate xRotate;
	public Rotate yRotate;

	public LwjglScene loadedScene;
	public LinkedList<ModelScene> sFx;

	// visualization bones
	
	public List<Bone> bones0 = new ArrayList<>();
	public List<LwjglNode> bones1 = new ArrayList<>();

	public List<KeyFrame> keyFrames = new ArrayList<>();
	
	public ModelNode model;
	public Skeleton skl;
	public Map<Integer,List<Affine>> affineKeyFramesOnBone;
	public Map<Joint,List<Affine>> affineKeyFramesOnJoint;
	
	private double scenex, sceney = 0;
	private final DoubleProperty angleX = new SimpleDoubleProperty(0);
	private final DoubleProperty angleY = new SimpleDoubleProperty(0);
	private double anchorAngleX = 0;
	private double anchorAngleY = 0;

	final Translate t = new Translate();

	public GLSlider index;
	
	public List<Matrix4f> matrixPool = new ArrayList<>();
	public LwjglNode node;
	public int boneCount = 0;

	public LwjglScene ihaScene;
	
	public static void main(String[] args) {
		try {
			logger.log(Level.INFO, "Application runnig ...");
			launch(args);
		} catch (ArithmeticException ex) {
			logger.error("Sorry, something wrong!", ex);
		}
	}

	public MainFx3D3() throws Exception {

		//assimpLoader = new AssimpLoader("./models/guard.dae");
		assimpLoader = new AssimpLoader("./models/monster.md5mesh");
		loadedScene = assimpLoader.getScene();
		sFx = assimpLoader.getSceneFx();

	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage stage) throws Exception {
		Group root3D = new Group();
		final Group grp = new Group();

		
		getParameters().getRaw();
		Scene scene = new Scene(root3D, 1024, 768, true);
		final Scene scene2 = new Scene(grp, 200, 440, true);

		Stage myDialog = new Stage();

		myDialog.initModality(Modality.APPLICATION_MODAL);
		myDialog.setWidth(250);
		myDialog.setHeight(440);

		myDialog.setX(1000);
		myDialog.setY(40);

		myDialog.setScene(scene2);
		myDialog.show();

		scene.setFill(Color.BLACK);
		//stage.initStyle(StageStyle.TRANSPARENT);
		//scene.setFill(Color.TRANSPARENT);
		
		camera.getTransforms().addAll(cameraXRotate, cameraYRotate, cameraPosition, cameraLookXRotate,
				cameraLookZRotate);
		camera.setNearClip(0.1);
		camera.setFarClip(1000);
		
		Cylinder cl = new Cylinder(200,200,200);

		scene.setCamera(camera);

		cameraRotate(root3D, scene, angleX,  angleY,
				     anchorAngleX, anchorAngleY, scenex, sceney);

		ihaScene = assimpLoader.getScene();
		Animation na = ihaScene.getAnimations().get("");

		Group figure = new Group();
		Group meshes = new Group();
		model = assimpLoader.getModelScene();
		
		Skeleton mySkl = new Skeleton("MySkeleton");
		Group test = new Group();
		
		Skeleton main ;
		
		main = sFx.getLast().skeletons.values().iterator().next();
		node = ihaScene.getRootNode();
		
		switch (0) {
		case 1:
			ihaScene.getMeshes().forEach((general) -> {
						meshes.getChildren().add(createSkin(general));
						//System.out.println(">" + general.name);
						int frameCount = 0;
						boneCount = 0 ;
						for (int j = 0; j < general.getBones().size(); j++) {
							Bone bone = (Bone) general.getBones().get(j);
							LwjglNode node = ihaScene.getRootNode().findByName(bone.getBoneName());
							figure.getChildren().add(createJoint(node, j, ihaScene, boneCount, affine, keyFrames));
							bones1.add(node);
							boneCount++;
							//System.out.println(boneCount + " " +bone.getBoneName());
						}
			});
			//System.out.println("Quantity: " + boneCount);
			break;
		}
		switch (3) {
		case 0:
			root3D.getChildren().addAll(meshes, figure); 
			break;
		case 1:	
			root3D.getChildren().addAll(meshes);
			break;
		case 2:
			root3D.getChildren().addAll(main);
			break;
		case 3:
			root3D.getChildren().addAll(sFx.getLast(),cl);
			break;
		case 4:
			root3D.getChildren().addAll(figure); 
			break;
		case 5:
			root3D.getChildren().addAll(figure, main); 
			break;
		case 6:
			root3D.getChildren().addAll(assimpLoader.getModelScene(),figure);
			break;
		}
		
		final Timeline timeline = new Timeline();
		timeline.getKeyFrames().addAll(keyFrames);
		timeline.setCycleCount(Timeline.INDEFINITE);
		assimpLoader.getTimelines().values().forEach(tl -> timeline.getKeyFrames().addAll(tl.getKeyFrames()));
		timeline.play();

		stage.setScene(scene);
		stage.setOnCloseRequest(event -> myDialog.close());
		stage.show();
		
		GLSlider trX = new GLSlider(10d, 60d, "Translate X:", -1000d, 1000d, 2.7d);
		GLSlider trY = new GLSlider(10d, 100d, "Translate Y:", -1000d, 1000d, -124.3d);
		GLSlider trZ = new GLSlider(10d, 140d, "Translate Z:", -1000d, 1000d, -201d);
		GLSlider scl = new GLSlider(10d, 180d, "Scale figure:", 0d, 1.5d, 1.0d);
		GLSlider dur = new GLSlider(10d, 220d, "Timeline position:  ", 0d, timeline.getCycleDuration().toMillis(), 0d);
		GLSlider tq = new GLSlider(10d, 260d, "Time quatum:   ", 0d, 200d, 25d);

		Button start = new Button("Start");
		start.setLayoutX(10d);
		start.setLayoutY(300d);
		
		Button pause = new Button("Pause");
		pause.setLayoutX(60d);
		pause.setLayoutY(300d);
		
		camera.translateXProperty().bind(trX.getValue());
		camera.translateYProperty().bind(trY.getValue());
		camera.translateZProperty().bind(trZ.getValue());
		
		timeline.rateProperty().bind(tq.getValue());

		figure.scaleXProperty().bind(scl.getValue());
		figure.scaleYProperty().bind(scl.getValue());
		figure.scaleZProperty().bind(scl.getValue());

		meshes.scaleXProperty().bind(scl.getValue());
		meshes.scaleYProperty().bind(scl.getValue());
		meshes.scaleZProperty().bind(scl.getValue());

		main.scaleXProperty().bind(scl.getValue());
		main.scaleYProperty().bind(scl.getValue());
		main.scaleZProperty().bind(scl.getValue());

		dur.getValue().addListener((e, o, n) -> {
			//timeline.pause();
			
			timeline.jumpTo(Duration.millis(n.doubleValue()));
		});

		tq.getValue().addListener((e, o, n) -> {
			assimpLoader.setTimeQuantum(n.floatValue());
		});
		
		timeline.currentTimeProperty().addListener((e, o, n) -> {
			dur.getValue().set(n.toMillis());
		});
		
		start.setOnAction(event -> {
			timeline.play();
		});

		pause.setOnAction(event -> {
			timeline.pause();
		});

		
		if (ihaScene.getAnimations().size() > 0) {
	/*		index = new GLSlider(10d, 20d, "Change element №1:      ", 0,
					ihaScene.getAnimations().get("").getFrames().size() - 1, 0);

			index.getValue().addListener((e, o, n) -> {
				switch (1) {
				case 0:
					for (int i = 0; i < figure.getChildren().size(); i++) {
						figure.getChildren().get(i).getTransforms().clear();
						LwjglNode node = bones1.get(i);
						Matrix4f mat = ihaScene.getAnimations().get("").getFrames().get(n.intValue()).getJointMatrices()[i];
						figure.getChildren().get(i).getTransforms().addAll(adaptedMatrix(mat));
					}
	
					break;
				case 1:
					Skeleton skln = sFx.getFirst().getSkeletons().get("MD5");
					for (Map.Entry<String, Joint> jnt : skln.getJoints().entrySet()) {
						int qtyJoints = skln.getJoints().entrySet().size();
						Matrix4f mat = getTM(jnt.getValue().getId(), n.intValue());
						//Affine af = affineKeyFramesOnJoint.get(jnt.getValue()).get(n.intValue());
						//System.out.println(" diff: " + (af.getMxx() - adaptedMatrix(mat).getMxx()));
						jnt.getValue().getTransforms().clear();
						jnt.getValue().getTransforms().addAll(adaptedMatrix(mat));
					}					
					break;
				}
				index.getValue().set(n.intValue());
			});
			grp.getChildren().addAll( index, trX, trY, trZ, scl, dur, start, pause);
		*/
			grp.getChildren().addAll( trX, trY, trZ, scl, dur, start, pause, tq);
		} else {
			grp.getChildren().addAll(  trX, trY, trZ, scl, dur, start, pause, tq);
		}
	}

	private Matrix4f getTM(String nameJoint, int frame) {
		Matrix4f mat = null;
		int j;
		for (j = 0; j < assimpLoader.getQtyBones(); j++) {
			if (assimpLoader.getBoneName(j).equals(nameJoint)) break;
		}		
		if (j == 32) return new Matrix4f();
		//System.out.println(j + " - " + assimpLoader.getBoneName(j) + " jnt: " + nameJoint);
		mat = ihaScene.getAnimations().get("").getFrames().get(frame).getJointMatrices()[j];
		return mat;
	}
	
	private List<Transform> getTMA(String nameJoint, int frame, int qtyJoints, Group figure) {
		Matrix4f mat = null;
		int j;
		for (j = 0; j < 35; j++) {
			if (assimpLoader.getBoneName(j).equals(nameJoint)) break;
		}		
		if (j == 32) return new ArrayList<Transform>();
		//System.out.println(j + " - " + assimpLoader.getBoneName(j) + " jnt: " + nameJoint);
		
		//System.out.println(figure.getChildren().get(j));
		//mat.invert();
		return (List<Transform>) (figure.getChildren().get(j).getTransforms());
	}
	
	@SuppressWarnings("unchecked")
	private Map<Integer,List<Affine>> getFramesFromFileOnBoneList(String inp, List<Bone> bones){
		Map<Integer,List<Affine>> out = new HashMap();
		Map<String,double[]> framesMap = new HashMap();
		
		String input;
		input = read(inp);
		input.replaceAll("\r", "");
		String[] job = input.split("\n");
		double[] _work;
		String id = "";
		String[] str;
		int matSize = 0;
		int qtyValue = 0;
		
		for (int i = 0; i < job.length; i++) {
			if (job[i].contains("id:")) {
				String[] work = job[i].split(":");
				work = work[1].split(";");
				id = work[0];
				matSize = Integer.parseInt(work[1]);
				qtyValue = Integer.parseInt(work[2]);
			} else {
				String[] work = job[i].split(";");
				if (work[0].equals(id)) {
					_work = new double[qtyValue];
					for (int j = 1; j < work.length; j++) {
						_work[j - 1] = Double.parseDouble(work[j]);
					}
					
					str = id.split("_");
					String resId = "";
					
					for (int k = 1 ; k < str.length; k++) {
						if (!"pose".equals(str[k])  && !"matrix".equals(str[k])) {
							resId += str[k] + "_"; 
						}
					}
					
					resId = resId.substring(0, resId.length() - 1);
					
					framesMap.put(resId,_work);
				}
			}
		}
		
		out = (Map<Integer, List<Affine>>) bones.stream()
                .collect(Collectors.toMap(k -> ((Bone)k).getGlobalId(), v->{
        			List<Affine> af = new ArrayList<>();
    				double[] work = framesMap.get(((Bone)v).getBoneName());
    				
    				for (int i = 0; i < work.length/16; i++) {
    					af.add(new Affine(work, MatrixType.MT_3D_4x4, i * 16));
    				}
    				return af;
               }));
		
		return out;
	}

	@SuppressWarnings("unchecked")
	private Map<Joint,List<Affine>> getFramesFromFileOnJointList(String inp, Map<String, Joint> joints){
		Map<Joint,List<Affine>> out = new HashMap();
		Map<String,double[]> framesMap = new HashMap();
		
		String input;
		input = read(inp);
		input.replaceAll("\r", "");
		String[] job = input.split("\n");
		double[] _work;
		String id = "";
		String[] str;
		int matSize = 0;
		int qtyValue = 0;
		
		for (int i = 0; i < job.length; i++) {
			if (job[i].contains("id:")) {
				String[] work = job[i].split(":");
				work = work[1].split(";");
				id = work[0];
				matSize = Integer.parseInt(work[1]);
				qtyValue = Integer.parseInt(work[2]);
			} else {
				String[] work = job[i].split(";");
				if (work[0].equals(id)) {
					_work = new double[qtyValue];
					for (int j = 1; j < work.length; j++) {
						_work[j - 1] = Double.parseDouble(work[j]);
					}
					
					str = id.split("_");
					String resId = "";
					
					for (int k = 1 ; k < str.length; k++) {
						if (!"pose".equals(str[k])  && !"matrix".equals(str[k])) {
							resId += str[k] + "_"; 
						}
					}
					
					resId = resId.substring(0, resId.length() - 1);
					
					framesMap.put(resId,_work);
				}
			}
		}
		
		
		for (Map.Entry<String, Joint> jnt : joints.entrySet()) {
  			List<Affine> af = new ArrayList<>();
			double[] work = framesMap.get(jnt.getKey());
			
			for (int i = 0; i < work.length/16; i++) {
				af.add(new Affine(work, MatrixType.MT_3D_4x4, i * 16));
			}

			out.put(jnt.getValue(), af);
		}
		
		return out;
	}
}
