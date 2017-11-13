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
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.JointFx;
import edu.lwjgl_fx_01.ui.controller.AssimpLoader;
import edu.lwjgl_fx_01.ui.model.engine.SceneFx;
import edu.lwjgl_fx_01.ui.model.engine.graph.NodeFx;
import edu.lwjgl_fx_01.ui.model.engine.graph.animation.Animation;
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
public class MainFx3D extends Application {
	private Stage stage;
	final static Logger logger = LogManager.getLogger(MainFx3D.class);
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

	public SceneFx loadedScene;


	// visualization bones
	

	public List<KeyFrame> keyFrames = new ArrayList<>();
	
	public NodeFx model;

	public Map<Integer,List<Affine>> affineKeyFramesOnBone;
	public Map<JointFx,List<Affine>> affineKeyFramesOnJoint;
	
	private double scenex, sceney = 0;
	private final DoubleProperty angleX = new SimpleDoubleProperty(0);
	private final DoubleProperty angleY = new SimpleDoubleProperty(0);
	private double anchorAngleX = 0;
	private double anchorAngleY = 0;

	final Translate t = new Translate();

	public GLSlider index;
	
	public List<Matrix4f> matrixPool = new ArrayList<>();

	public int boneCount = 0;

	public SceneFx ihaScene;
	
	public static void main(String[] args) {
		try {
			logger.log(Level.INFO, "Application runnig ...");
			launch(args);
		} catch (ArithmeticException ex) {
			logger.error("Sorry, something wrong!", ex);
		}
	}

	public MainFx3D() throws Exception {

		assimpLoader = new AssimpLoader("./models/boblamp.md5mesh");
		//assimpLoader = new AssimpLoader("./models/monster.md5mesh");
		loadedScene = assimpLoader.getScene();

	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage stage) throws Exception {
		Group root3D = new Group();
		final Group grp = new Group();

		
		getParameters().getRaw();
		Scene scene = new Scene(root3D, 600, 600, true);
		final Scene scene2 = new Scene(grp, 200, 440, true);

		Stage myDialog = new Stage();

		myDialog.initModality(Modality.APPLICATION_MODAL);
		myDialog.setWidth(250);
		myDialog.setHeight(440);

		myDialog.setX(1000);
		myDialog.setY(40);

		myDialog.setScene(scene2);
		myDialog.show();

		scene.setFill(Color.TRANSPARENT);
		stage.initStyle(StageStyle.TRANSPARENT);
		scene.setFill(Color.TRANSPARENT);
		
		camera.getTransforms().addAll(cameraXRotate, cameraYRotate, cameraPosition, cameraLookXRotate,
				cameraLookZRotate);
		camera.setNearClip(0.1);
		camera.setFarClip(1000);
		//camera.setTranslateZ(-80);

		scene.setCamera(camera);

		cameraRotate(root3D, scene, angleX,  angleY,
				     anchorAngleX, anchorAngleY, scenex, sceney);

		ihaScene = assimpLoader.getScene();
		Animation na = ihaScene.getAnimations().get("");

		Group figure = new Group();
		Group meshes = new Group();
		//model = assimpLoader.getModelScene();

		Group test = new Group();

		switch (0) {
		case 0:
			root3D.getChildren().addAll(ihaScene); 
			break;
		case 1:	
			root3D.getChildren().addAll(meshes);
			break;

		case 4:
			root3D.getChildren().addAll(figure); 
			break;
		}
		
		final Timeline timeline = new Timeline();
		timeline.getKeyFrames().addAll(keyFrames);
		timeline.setCycleCount(Timeline.INDEFINITE);
		//assimpLoader.getTimelines().values().forEach(tl -> timeline.getKeyFrames().addAll(tl.getKeyFrames()));
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

		dur.getValue().addListener((e, o, n) -> {
			//timeline.pause();
			
			timeline.jumpTo(Duration.millis(n.doubleValue()));
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
			grp.getChildren().addAll( trX, trY, trZ, scl, dur, start, pause, tq);
		} else {
			grp.getChildren().addAll(  trX, trY, trZ, scl, dur, start, pause, tq);
		}
	}
}
