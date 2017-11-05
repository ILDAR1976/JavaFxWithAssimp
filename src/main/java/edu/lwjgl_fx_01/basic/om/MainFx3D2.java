package edu.lwjgl_fx_01.basic.om;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import edu.lwjgl_fx_01.ui.controller.AssimpLoader;
import edu.lwjgl_fx_01.ui.model.engine.LwjglScene;
import edu.lwjgl_fx_01.ui.model.engine.graph.LwjglMesh;
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

@SuppressWarnings({ "unused", "restriction" })
public class MainFx3D2 extends Application {
	private Stage stage;
	final static Logger logger = LogManager.getLogger(MainFx3D2.class);
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

	// visualition bones
	public List<Bone> bones0 = new ArrayList<>();
	public List<LwjglNode> bones1 = new ArrayList<>();

	public List<KeyFrame> keyFrames = new ArrayList<>();

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

	public static void main(String[] args) {
		try {

			logger.log(Level.INFO, "Application runnig ...");

			launch(args);

		} catch (ArithmeticException ex) {
			logger.error("Sorry, something wrong!", ex);
		}
	}

	public MainFx3D2() throws Exception {

		assimpLoader = new AssimpLoader("./models/guard.dae");

		loadedScene = assimpLoader.getScene();

	}

	@Override
	public void start(Stage stage) throws Exception {
		Group root3D = new Group();
		final Group grp = new Group();

		fullMatrixPool();

		getParameters().getRaw();
		Scene scene = new Scene(root3D, 1376, 768, true);
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

		camera.getTransforms().addAll(cameraXRotate, cameraYRotate, cameraPosition, cameraLookXRotate,
				cameraLookZRotate);
		camera.setNearClip(0.1);
		camera.setFarClip(1000);
		//camera.setTranslateZ(-80);

		scene.setCamera(camera);

		cameraRotate(root3D, scene);

		LwjglScene ihaScene = assimpLoader.getScene();
		Animation na = ihaScene.getAnimations().get("");

		Group figure = new Group();
		Group meshes = new Group();

		node = ihaScene.getRootNode();

		switch (1) {
		case 1:
			ihaScene.getMeshes().forEach((general) -> {
						meshes.getChildren().add(createSkin(general));
						System.out.println(general.name);
						int frameCount = 0;
						for (int j = 0; j < general.getBones().size(); j++) {
							Bone bone = (Bone) general.getBones().get(j);
							LwjglNode node = ihaScene.getRootNode().findByName(bone.getBoneName());
							figure.getChildren().add(createJoint(node, j, ihaScene, boneCount));
							bones1.add(node);
							boneCount++;
				}
			});

			System.out.println("Quantity: " + boneCount);
			// System.out.println("Fames: " + frameCount);
			break;
		}

		switch (0) {
		case 0:
			root3D.getChildren().addAll(meshes, figure); 
			break;
		case 1:	
			root3D.getChildren().addAll(meshes);
			break;
		}
		
		final Timeline timeline = new Timeline();
		timeline.getKeyFrames().addAll(keyFrames);
		timeline.setCycleCount(Timeline.INDEFINITE);
		//timeline.play();

		stage.setScene(scene);
		stage.setOnCloseRequest(event -> myDialog.close());
		stage.show();

		
		GLSlider trX = new GLSlider(10d, 60d, "Translate X:", -1000d, 1000d, 2.7d);
		GLSlider trY = new GLSlider(10d, 100d, "Translate Y:", -1000d, 1000d, -124.3d);
		GLSlider trZ = new GLSlider(10d, 140d, "Translate Z:", -1000d, 1000d, -201d);
		GLSlider scl = new GLSlider(10d, 180d, "Scale figure:", 0d, 2d, 1.0d);

		camera.translateXProperty().bind(trX.getValue());
		camera.translateYProperty().bind(trY.getValue());
		camera.translateZProperty().bind(trZ.getValue());

		figure.scaleXProperty().bind(scl.getValue());
		figure.scaleYProperty().bind(scl.getValue());
		figure.scaleZProperty().bind(scl.getValue());

		meshes.scaleXProperty().bind(scl.getValue());
		meshes.scaleYProperty().bind(scl.getValue());
		meshes.scaleZProperty().bind(scl.getValue());
		
		if (ihaScene.getAnimations().size() > 0) {
			index = new GLSlider(10d, 20d, "Change element №1:      ", 0,
					ihaScene.getAnimations().get("").getFrames().size() - 1, 0);

			index.getValue().addListener((e, o, n) -> {
				switch (0) {
				case 0:
	
					for (int i = 0; i < figure.getChildren().size(); i++) {
	
						figure.getChildren().get(i).getTransforms().clear();
	
						LwjglNode node = bones1.get(i);
	
						Matrix4f mat = ihaScene.getAnimations().get("").getFrames().get(n.intValue()).getJointMatrices()[i];
	
						figure.getChildren().get(i).getTransforms().addAll(adaptedMatrix(mat));
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

	public static void quaternionToAffine(Quaternionf q, Affine a) {
		double w = q.w(), x = q.x(), y = q.y(), z = q.z();
		a.setToIdentity();
		a.setMxx(w * w + x * x - y * y - z * z);
		a.setMxy(2.0 * (x * y - w * z));
		a.setMxz(2.0 * (w * y + x * z));
		a.setMyx(2.0 * (x * y + w * z));
		a.setMyy(w * w - x * x + y * y - z * z);
		a.setMyz(2.0 * (y * z - w * x));
		a.setMzx(2.0 * (x * z - w * y));
		a.setMzy(2.0 * (w * x + y * z));
		a.setMzz(w * w - x * x - y * y + z * z);
	}

	private class Matrix extends Affine {

		public Matrix(double[] m, MatrixType mt, int offset) {
			super(m, mt, offset);
		}

		public List<DoubleProperty> getProperties() {
			List<DoubleProperty> out = new ArrayList<>();

			out.add(this.mxxProperty());
			out.add(this.mxyProperty());
			out.add(this.mxzProperty());
			out.add(this.myxProperty());
			out.add(this.myyProperty());
			out.add(this.myzProperty());
			out.add(this.mzxProperty());
			out.add(this.mzyProperty());
			out.add(this.mzzProperty());
			out.add(this.txProperty());
			out.add(this.tyProperty());
			out.add(this.tzProperty());

			return out;
		}

		public List<Double> getValue() {
			List<Double> out = new ArrayList<>();

			out.add(this.getMxx());
			out.add(this.getMxy());
			out.add(this.getMxz());
			out.add(this.getMyx());
			out.add(this.getMyy());
			out.add(this.getMyz());
			out.add(this.getMzx());
			out.add(this.getMzy());
			out.add(this.getMzz());
			out.add(this.getTx());
			out.add(this.getTy());
			out.add(this.getTz());

			return out;
		}

	}

	private TriangleMesh createCubeMesh() {

		float width = 1.8f / 2f;
		float points[] = { -width, -width, -width, width, -width, -width, width, width, -width, -width, width, -width,
				-width, -width, width, width, -width, width, width, width, width, -width, width, width };

		float texCoords[] = { 0, 0, 1, 0, 1, 1, 0, 1 };

		int faceSmoothingGroups[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		int faces[] = { 0, 0, 2, 2, 1, 1, 2, 2, 0, 0, 3, 3, 1, 0, 6, 2, 5, 1, 6, 2, 1, 0, 2, 3, 5, 0, 7, 2, 4, 1, 7, 2,
				5, 0, 6, 3, 4, 0, 3, 2, 0, 1, 3, 2, 4, 0, 7, 3, 3, 0, 6, 2, 2, 1, 6, 2, 3, 0, 7, 3, 4, 0, 1, 2, 5, 1, 1,
				2, 4, 0, 0, 3, };

		TriangleMesh mesh = new TriangleMesh();
		mesh.getPoints().setAll(points);
		mesh.getTexCoords().setAll(texCoords);
		//mesh.getTexCoords().addAll(0, 0, 0, 0, 0, 0, 0, 0);
		mesh.getFaces().setAll(faces);
		mesh.getFaceSmoothingGroups().setAll(faceSmoothingGroups);

		return mesh;
	}

	private TriangleMesh createPaneMesh() {

		float width = 1.8f / 2f;
		float points[] = { -width, -width, -width, width, -width, -width, width, width, -width, -width, width, -width,
				-width, -width, width, width, -width, width, width, width, width, -width, width, width };

		float texCoords[] = { 0, 0, 1, 0, 1, 1, 0, 1 };

		int faceSmoothingGroups[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		int faces[] = { 0, 0, 2, 2, 1, 1, 2, 2, 0, 0, 3, 3, 1, 0, 6, 2, 5, 1, 6, 2, 1, 0, 2, 3, 5, 0, 7, 2, 4, 1, 7, 2,
				5, 0, 6, 3, 4, 0, 3, 2, 0, 1, 3, 2, 4, 0, 7, 3, 3, 0, 6, 2, 2, 1, 6, 2, 3, 0, 7, 3, 4, 0, 1, 2, 5, 1, 1,
				2, 4, 0, 0, 3, };

		TriangleMesh mesh = new TriangleMesh();
		mesh.getPoints().setAll(points);
		mesh.getTexCoords().setAll(texCoords);
		mesh.getFaces().setAll(faces);
		//mesh.getFaceSmoothingGroups().setAll(faceSmoothingGroups);

		return mesh;
	}

	private TriangleMesh createMesh(LwjglMesh inp) {
		TriangleMesh mesh = new TriangleMesh();
		mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
		mesh.getPoints().setAll(inp.getPointsFx());

		mesh.getTexCoords().setAll(inp.getTextureCoordianatesFx());
		if (inp.getTextureCoordianatesFx().length % 2 != 0 ) mesh.getTexCoords().addAll(0);
		mesh.getFaces().setAll(inp.getFaces3Fx());
		mesh.getNormals().setAll(inp.getNormalsFx());
	
		return mesh;
	}

	private double[] toDouble(float[] inp) {

		double[] out = new double[inp.length];

		for (int i = 0; i < inp.length; i++) {
			out[i] = inp[i];
		}

		return out;
	}

	private class GLSlider extends Group {

		private Slider slider;
		private Label label;
		private Label valueLabel;
		private double width = 200;
		private double height = 20;
		private double value = 1d;

		private NumberBinding sumlX;
		private NumberBinding sumlY;
		private NumberBinding sumvX;
		private NumberBinding sumvY;

		private SimpleDoubleProperty ldX = new SimpleDoubleProperty(0d);
		private SimpleDoubleProperty ldY = new SimpleDoubleProperty(-15d);

		private SimpleDoubleProperty vdX = new SimpleDoubleProperty(150d);
		private SimpleDoubleProperty vdY = new SimpleDoubleProperty(-15d);

		public GLSlider(double x, double y, String label, double min, double max, double value) {
			this(x, y, label);
			this.slider.setMin(min);
			this.slider.setMax(max);
			this.slider.setValue(value);
		}

		public GLSlider(double x, double y, String label) {
			slider = new Slider();
			slider.layoutXProperty().set(x);
			slider.layoutYProperty().set(y);
			slider.setMaxWidth(width);
			slider.setMaxHeight(height);
			slider.valueProperty().set(this.value);

			this.label = new Label(label);

			sumlX = slider.layoutXProperty().add(ldX);
			this.label.layoutXProperty().bind(sumlX);

			sumlY = slider.layoutYProperty().add(ldY);
			this.label.layoutYProperty().bind(sumlY);

			this.valueLabel = new Label();

			vdX.set(label.length() * 5d + 3d);
			sumvX = slider.layoutXProperty().add(vdX);
			this.valueLabel.layoutXProperty().bind(sumvX);

			sumvY = slider.layoutYProperty().add(vdY);
			this.valueLabel.layoutYProperty().bind(sumvY);

			this.valueLabel.textProperty().bind(slider.valueProperty().asString());

			getChildren().addAll(this.label, valueLabel, slider);
		}

		public DoubleProperty getValue() {
			return this.slider.valueProperty();
		}

	}

	public Affine adaptedMatrix(Matrix4f matrix) {
		// matrix.invert();
		Affine affine = new Affine();
		affine.setMxx(matrix.m00());
		affine.setMxy(matrix.m10());
		affine.setMxz(matrix.m20());
		affine.setTx(matrix.m30());
		affine.setMyx(matrix.m01());
		affine.setMyy(matrix.m11());
		affine.setMyz(matrix.m21());
		affine.setTy(matrix.m31());
		affine.setMzx(matrix.m02());
		affine.setMzy(matrix.m12());
		affine.setMzz(matrix.m22());
		affine.setTz(matrix.m32());
		return affine;
	}

	public Group createBone(LwjglNode node, int bone, LwjglScene is, int count) {

		Group out = new Group();

		float t = 0.0f;

		Matrix4f mat2 = is.getAnimations().get("").getFrames().get(0).getJointMatrices()[bone];

		// System.out.println(count + " " + node.name);

		Affine a = adaptedMatrix(mat2);
		TriangleMesh mesh = createCubeMesh();
		MeshView mv = new MeshView(mesh);
		out.getChildren().add(mv);
		out.getTransforms().add(node.getAffine());
	
		for (int i = 0; i < is.getAnimations().get("").getFrames().size(); i++) {

			Matrix4f mat = is.getAnimations().get("").getFrames().get(i).getJointMatrices()[bone];

			affine = adaptedMatrix(mat);

			keyFrames.add(convertToKeyFrame(node.getTimeOfFrames().get(i).floatValue() * 1000f, node.getAffine(),
					affine, Interpolator.LINEAR));
		}
		

		return out;
	}

	public Joint createJoint(LwjglNode node, int bone, LwjglScene is, int count) {

		Joint joint = new Joint();

		float t = 0.0f;

		Matrix4f mat2 = is.getAnimations().get("").getFrames().get(0).getJointMatrices()[bone];

		// System.out.println(count + " " + node.name);

		Affine a = adaptedMatrix(mat2);
		TriangleMesh mesh = joint.createCubeMesh();
		joint.addMeshView();
		joint.a = node.getAffine();
	
		for (int i = 0; i < is.getAnimations().get("").getFrames().size(); i++) {

			Matrix4f mat = is.getAnimations().get("").getFrames().get(i).getJointMatrices()[bone];

			affine = adaptedMatrix(mat);

			keyFrames.add(convertToKeyFrame(node.getTimeOfFrames().get(i).floatValue() * 1000f, joint.a,
					affine, Interpolator.LINEAR));
		}
		

		return joint;
	}

	public Group createSkin(LwjglMesh ms) {

		Group out = new Group();

		TriangleMesh mesh = createMesh(ms);
		//TriangleMesh mesh = createCubeMesh();
		MeshView mv = new MeshView(mesh);
		PhongMaterial material = new PhongMaterial(Color.BURLYWOOD);

		mv.setMaterial(material);
		
		mv.setDrawMode(DrawMode.FILL);;
		mv.setCullFace(CullFace.BACK);

		out.getChildren().add(mv);

		
		return out;
	}

	public void cameraRotate(Group root3D, Scene scene) {
		Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
		Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
		root3D.getTransforms().addAll(xRotate, yRotate);
		// Use Binding so your rotation doesn't have to be recreated
		xRotate.angleProperty().bind(angleX);
		yRotate.angleProperty().bind(angleY);
		// Start Tracking mouse movements only when a button is pressed
		scene.setOnMousePressed(event -> {
			event.getSceneX();
			event.getSceneY();
			angleX.get();
			angleY.get();
		});
		// Angle calculation will only change when the button has been pressed
		scene.setOnMouseDragged(event -> {
			angleX.set(anchorAngleX - (scenex - event.getSceneY()));
			angleY.set(anchorAngleY + sceney - event.getSceneX());
		});

	}

	public void fullMatrixPool() {
		Matrix4f m = new Matrix4f(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0.996825098991394f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f);
		matrixPool.add(m);

		m = new Matrix4f(0.999473512172699f, 0.0f, 0.03244609013199806f, 0.0f, 0.0f, 1.0f, 0.0f, 0.996825098991394f,
				-0.03244609013199806f, 0.0f, 0.999473512172699f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);

		matrixPool.add(m);

		m = new Matrix4f(-0.05675503984093666f, 0.0f, 0.9983881115913391f, 0.0f, 0.0f, 1.0f, 0.0f, 0.996825098991394f,
				-0.9983881115913391f, 0.0f, -0.05675503984093666f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);

		matrixPool.add(m);

		m = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.996825098991394f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f);

		matrixPool.add(m);

	}

	private KeyFrame convertToKeyFrame(final float t, final Affine jointAffine, final Affine keyAffine,
			final Interpolator interpolator) {
		final Duration duration = new Duration(t);
		final List<KeyValue> kvs = convertToKeyValues(jointAffine, keyAffine, interpolator);
		final KeyValue[] kvs2 = kvs.toArray(new KeyValue[kvs.size()]);
		return new KeyFrame(duration, kvs2);
	}

	private KeyValue convertToKeyValue(final WritableValue<Number> target, final Number endValue,
			final Interpolator interpolator) {
		return new KeyValue(target, endValue, interpolator);
	}

	private List<KeyValue> convertToKeyValues(final Affine jointAffine, final Affine keyAffine,
			final Interpolator interpolator) {
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

	private void CreateAnimation() {
/*	       final DaeController controller = buildHelper.getController(instanceId);

	        final DaeSkeleton skeleton = buildHelper.getSkeleton(controller.getName());

	        final String[] jointNames = controller.getJointNames();

	        final List<Joint> joints = Stream.of(jointNames).map(skeleton.joints::get).collect(Collectors.toList());
	        final Affine[] bindTransforms = controller.bindPoses.toArray(new Affine[joints.size()]);

	        final List<TriangleMesh> meshes = buildHelper.getMeshes(controller.getSkinId());
	        final List<Material> materials = buildHelper.getMaterials(controller.getSkinId());

	        for (int i = 0; i < meshes.size(); i++) {
	            final SkinningMesh skinningMesh = new SkinningMesh(
	                    meshes.get(i), controller.getVertexWeights(), bindTransforms,
	                    controller.getBindShapeMatrix(), joints, Arrays.asList(skeleton));

	            final MeshView meshView = new MeshView(skinningMesh);

	            final SkinningMeshTimer skinningMeshTimer = new SkinningMeshTimer(skinningMesh);
	            if (meshView.getScene() != null) {
	                skinningMeshTimer.start();
	            }
	            meshView.sceneProperty().addListener((observable, oldValue, newValue) -> {
	                if (newValue == null) {
	                    skinningMeshTimer.stop();
	                } else {
	                    skinningMeshTimer.start();
	                }
	            });

	            if (i < materials.size()) {
	                meshView.setMaterial(materials.get(i));
	            }
	            getChildren().add(meshView);
	        }
*/	}
}
