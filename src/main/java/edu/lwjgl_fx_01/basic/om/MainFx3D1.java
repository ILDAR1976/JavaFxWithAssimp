package edu.lwjgl_fx_01.basic.om;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

@SuppressWarnings({ "unused", "restriction"})
public class MainFx3D1 extends Application {
	private Stage stage;
	final static Logger logger = LogManager.getLogger(MainFx3D1.class);
	private AssimpLoader assimpLoader;
	private final PerspectiveCamera camera = new PerspectiveCamera(true);
	private final Rotate cameraXRotate = new Rotate(-30, 0, 0, 0, Rotate.X_AXIS);
	// private final Rotate cameraXRotate = new Rotate(0,0,0,0,Rotate.X_AXIS);
	private final Rotate cameraYRotate = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
	// private final Rotate cameraYRotate = new Rotate(-50,0,0,0,Rotate.Y_AXIS);
	private final Rotate cameraLookXRotate = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
	private final Rotate cameraLookZRotate = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
	private final Translate cameraPosition = new Translate(0, 0, -10);

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

	public MainFx3D1() throws Exception {

		assimpLoader = new AssimpLoader("./models/guard.dae");
		
		loadedScene = assimpLoader.getScene();
		sFx = assimpLoader.getSceneFx();

	}

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

		//scene.setFill(Color.TRANSPARENT);
		//stage.initStyle(StageStyle.TRANSPARENT);
		scene.setFill(Color.BLACK);
		
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
		model = assimpLoader.getModelScene();
		
		Skeleton mySkl = new Skeleton("MySkeleton");
		Group test = new Group();
		
		Skeleton main ;
		
		main = sFx.getLast().skeletons.values().iterator().next();
		node = ihaScene.getRootNode();

		switch (1) {
		case 1:
			ihaScene.getMeshes().forEach((general) -> {
						meshes.getChildren().add(createSkin(general));
						System.out.println(">" + general.name);
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

			
			System.out.println("Quantity: " + boneCount);
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
			root3D.getChildren().addAll(model);
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
		timeline.play();

		stage.setScene(scene);
		stage.setOnCloseRequest(event -> myDialog.close());
		stage.show();
		
		GLSlider trX = new GLSlider(10d, 60d, "Translate X:", -1000d, 1000d, 2.7d);
		GLSlider trY = new GLSlider(10d, 100d, "Translate Y:", -1000d, 1000d, -124.3d);
		GLSlider trZ = new GLSlider(10d, 140d, "Translate Z:", -1000d, 1000d, -201d);
		GLSlider scl = new GLSlider(10d, 180d, "Scale figure:", 0d, 1.5d, 1.0d);

		camera.translateXProperty().bind(trX.getValue());
		camera.translateYProperty().bind(trY.getValue());
		camera.translateZProperty().bind(trZ.getValue());

		figure.scaleXProperty().bind(scl.getValue());
		figure.scaleYProperty().bind(scl.getValue());
		figure.scaleZProperty().bind(scl.getValue());

		meshes.scaleXProperty().bind(scl.getValue());
		meshes.scaleYProperty().bind(scl.getValue());
		meshes.scaleZProperty().bind(scl.getValue());

		main.scaleXProperty().bind(scl.getValue());
		main.scaleYProperty().bind(scl.getValue());
		main.scaleZProperty().bind(scl.getValue());
		
		if (ihaScene.getAnimations().size() > 0) {
			index = new GLSlider(10d, 20d, "Change element №1:      ", 0,
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
					Skeleton skln = (Skeleton) model.getChildren().stream()
					                                .filter(v->v instanceof Skeleton)
					                                .collect(Collectors.toList()).get(0);
					
					for (Map.Entry<String, Joint> jnt : skln.getJoints().entrySet()) {
						int qtyJoints = skln.getJoints().entrySet().size();
						Matrix4f mat = getTM(jnt.getValue().getId(), n.intValue());
						jnt.getValue().getTransforms().clear();
						jnt.getValue().getTransforms().add(adaptedMatrix(mat));
					}					
					break;
				}
				index.getValue().set(n.intValue());
			});
			grp.getChildren().addAll( index, trX, trY, trZ, scl);
		} else {
			grp.getChildren().addAll(  trX, trY, trZ, scl);
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

}
