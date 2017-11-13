/*
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package edu.lwjgl_fx_01.ui.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMatrix4x4;

import edu.lwjgl_fx_01.ui.model.engine.SceneFx;
import edu.lwjgl_fx_01.ui.model.engine.shape3d.SkinningMesh;
import edu.lwjgl_fx_01.ui.model.engine.graph.NodeFx;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.JointFx;
import edu.lwjgl_fx_01.ui.model.engine.graph.NodeFx;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.WritableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.scene.transform.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Utility methods for most of the ray tracing demos.
 * 
 * @author Kai Burjack
 */
@SuppressWarnings("restriction")
public class Utils {
    private static final int TIMER_MS_RATIO = 1000;
	private static final Vector3f VECTOR_MINUS_ONE = new Vector3f(-1.0f, -1.0f, -1.0f);
	private static final Vector3f VECTOR_PLUS_ONE = new Vector3f(1.0f, 1.0f, 1.0f);
	
	//counters
	
	public static int counter = 0;
	private static int countJoint = 0;
	
	
	/**
	 * Write the vertices (position and normal) of an axis-aligned unit box into the
	 * provided {@link FloatBuffer}.
	 * 
	 * @param fv
	 *            the {@link FloatBuffer} receiving the vertex position and normal
	 */
	public static void triangulateUnitBox(FloatBuffer fv) {
		triangulateBox(VECTOR_MINUS_ONE, VECTOR_PLUS_ONE, fv);
	}

	/**
	 * Write the vertices (position and normal) of an axis-aligned box with the
	 * given corner coordinates into the provided {@link FloatBuffer}.
	 * 
	 * @param min
	 *            the min corner
	 * @param max
	 *            the max corner
	 * @param fv
	 *            the {@link FloatBuffer} receiving the vertex position and normal
	 */
	public static void triangulateBox(Vector3f min, Vector3f max, FloatBuffer fv) {
		/* Front face */
		fv.put(min.x).put(min.y).put(max.z).put(0.0f).put(0.0f).put(1.0f);
		fv.put(max.x).put(min.y).put(max.z).put(0.0f).put(0.0f).put(1.0f);
		fv.put(max.x).put(max.y).put(max.z).put(0.0f).put(0.0f).put(1.0f);
		fv.put(max.x).put(max.y).put(max.z).put(0.0f).put(0.0f).put(1.0f);
		fv.put(min.x).put(max.y).put(max.z).put(0.0f).put(0.0f).put(1.0f);
		fv.put(min.x).put(min.y).put(max.z).put(0.0f).put(0.0f).put(1.0f);
		/* Back face */
		fv.put(max.x).put(min.y).put(min.z).put(0.0f).put(0.0f).put(-1.0f);
		fv.put(min.x).put(min.y).put(min.z).put(0.0f).put(0.0f).put(-1.0f);
		fv.put(min.x).put(max.y).put(min.z).put(0.0f).put(0.0f).put(-1.0f);
		fv.put(min.x).put(max.y).put(min.z).put(0.0f).put(0.0f).put(-1.0f);
		fv.put(max.x).put(max.y).put(min.z).put(0.0f).put(0.0f).put(-1.0f);
		fv.put(max.x).put(min.y).put(min.z).put(0.0f).put(0.0f).put(-1.0f);
		/* Left face */
		fv.put(min.x).put(min.y).put(min.z).put(-1.0f).put(0.0f).put(0.0f);
		fv.put(min.x).put(min.y).put(max.z).put(-1.0f).put(0.0f).put(0.0f);
		fv.put(min.x).put(max.y).put(max.z).put(-1.0f).put(0.0f).put(0.0f);
		fv.put(min.x).put(max.y).put(max.z).put(-1.0f).put(0.0f).put(0.0f);
		fv.put(min.x).put(max.y).put(min.z).put(-1.0f).put(0.0f).put(0.0f);
		fv.put(min.x).put(min.y).put(min.z).put(-1.0f).put(0.0f).put(0.0f);
		/* Right face */
		fv.put(max.x).put(min.y).put(max.z).put(1.0f).put(0.0f).put(0.0f);
		fv.put(max.x).put(min.y).put(min.z).put(1.0f).put(0.0f).put(0.0f);
		fv.put(max.x).put(max.y).put(min.z).put(1.0f).put(0.0f).put(0.0f);
		fv.put(max.x).put(max.y).put(min.z).put(1.0f).put(0.0f).put(0.0f);
		fv.put(max.x).put(max.y).put(max.z).put(1.0f).put(0.0f).put(0.0f);
		fv.put(max.x).put(min.y).put(max.z).put(1.0f).put(0.0f).put(0.0f);
		/* Top face */
		fv.put(min.x).put(max.y).put(max.z).put(0.0f).put(1.0f).put(0.0f);
		fv.put(max.x).put(max.y).put(max.z).put(0.0f).put(1.0f).put(0.0f);
		fv.put(max.x).put(max.y).put(min.z).put(0.0f).put(1.0f).put(0.0f);
		fv.put(max.x).put(max.y).put(min.z).put(0.0f).put(1.0f).put(0.0f);
		fv.put(min.x).put(max.y).put(min.z).put(0.0f).put(1.0f).put(0.0f);
		fv.put(min.x).put(max.y).put(max.z).put(0.0f).put(1.0f).put(0.0f);
		/* Bottom face */
		fv.put(min.x).put(min.y).put(min.z).put(0.0f).put(-1.0f).put(0.0f);
		fv.put(max.x).put(min.y).put(min.z).put(0.0f).put(-1.0f).put(0.0f);
		fv.put(max.x).put(min.y).put(max.z).put(0.0f).put(-1.0f).put(0.0f);
		fv.put(max.x).put(min.y).put(max.z).put(0.0f).put(-1.0f).put(0.0f);
		fv.put(min.x).put(min.y).put(max.z).put(0.0f).put(-1.0f).put(0.0f);
		fv.put(min.x).put(min.y).put(min.z).put(0.0f).put(-1.0f).put(0.0f);
	}

	/**
	 * Create a shader object from the given classpath resource.
	 *
	 * @param resource
	 *            the class path
	 * @param type
	 *            the shader type
	 *
	 * @return the shader object id
	 *
	 * @throws IOException
	 */
	public static int createShader(String resource, int type) throws IOException {
		return createShader(resource, type, null);
	}

	/**
	 * Create a shader object from the given classpath resource.
	 *
	 * @param resource
	 *            the class path
	 * @param type
	 *            the shader type
	 * @param version
	 *            the GLSL version to prepend to the shader source, or null
	 *
	 * @return the shader object id
	 *
	 * @throws IOException
	 */
	public static int createShader(String resource, int type, String version) throws IOException {
		int shader = glCreateShader(type);

		ByteBuffer source = ioResourceToByteBuffer(resource, 8192);

		if (version == null) {
			PointerBuffer strings = BufferUtils.createPointerBuffer(1);
			IntBuffer lengths = BufferUtils.createIntBuffer(1);

			strings.put(0, source);
			lengths.put(0, source.remaining());

			glShaderSource(shader, strings, lengths);
		} else {
			PointerBuffer strings = BufferUtils.createPointerBuffer(2);
			IntBuffer lengths = BufferUtils.createIntBuffer(2);

			ByteBuffer preamble = memUTF8("#version " + version + "\n", false);

			strings.put(0, preamble);
			lengths.put(0, preamble.remaining());

			strings.put(1, source);
			lengths.put(1, source.remaining());

			glShaderSource(shader, strings, lengths);
		}

		glCompileShader(shader);
		int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
		String shaderLog = glGetShaderInfoLog(shader);
		if (shaderLog != null && shaderLog.trim().length() > 0) {
			System.err.println(shaderLog);
		}
		if (compiled == 0) {
			throw new AssertionError("Could not compile shader");
		}
		return shader;
	}

	public static String loadFromJar(String name) throws IOException {

		String path = name;

		String[] parts = path.split("/");
		String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

		String prefix = "";
		String suffix = null;
		if (filename != null) {
			parts = filename.split("\\.", 2);
			prefix = parts[0];
			suffix = (parts.length > 1) ? "." + parts[parts.length - 1] : null;
		}

		if (filename == null || prefix.length() < 3) {
			throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
		}

		File temp = File.createTempFile(prefix, suffix);
		temp.deleteOnExit();

		if (!temp.exists()) {
			throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
		}

		byte[] buffer = new byte[1024];
		int readBytes;

		InputStream is = Utils.class.getResourceAsStream(path);
		if (is == null) {
			throw new FileNotFoundException("File " + path + " was not found inside JAR.");
		}

		OutputStream os = new FileOutputStream(temp);
		try {
			while ((readBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}
		} finally {
			os.close();
			is.close();
		}

		return temp.getAbsolutePath();

	}

	public static String loadResource(String sfileName) throws Exception {
		String result;

		// System.out.println(Utils.loadFromJar(sfileName));

		File initialFile = new File(Utils.loadFromJar(sfileName));
		InputStream in = new FileInputStream(initialFile);

		// try (InputStream in =
		// Utils.class.getClass().getResourceAsStream(Utils.loadFromJar(sfileName));
		try (Scanner scanner = new Scanner(in, "UTF-8")) {
			result = scanner.useDelimiter("\\A").next();
		}
		return result;
	}

	public static List<String> readAllLines(String fileName) throws Exception {
		List<String> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(Utils.class.getClass().getResourceAsStream(fileName)))) {
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		}
		return list;
	}

	public static int[] listIntToArray(List<Integer> list) {
		int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
		return result;
	}

	public static float[] listToArray(List<Float> list) {
		int size = list != null ? list.size() : 0;
		float[] floatArr = new float[size];
		for (int i = 0; i < size; i++) {
			floatArr[i] = list.get(i);
		}
		return floatArr;
	}

	public static Affine[] listAffineToArray(List<Affine> list) {
		int size = list != null ? list.size() : 0;
		Affine[] affineArr = new Affine[size];
		for (int i = 0; i < size; i++) {
			affineArr[i] = list.get(i);
		}
		return affineArr;
	}

	public static double[] listToArrayDouble(List<Double> list) {
		int size = list != null ? list.size() : 0;
		double[] doubleArr = new double[size];
		for (int i = 0; i < size; i++) {
			doubleArr[i] = list.get(i);
		}
		return doubleArr;
	}

	public static List<Vector3f> getVector3fListFromFloatArray(float[] inp) {
		if (inp.length > 0) {
			List<Vector3f> out = new ArrayList<>();
			Vector3f vec = null;
			for (int i = 0; i < inp.length; i++) {
				if (i % 3 == 0) {
					vec = new Vector3f();
					vec.x = inp[i];
				} else if (i % 3 == 1) {
					vec.y = inp[i];
				} else if (i % 3 == 2) {
					vec.z = inp[i];
					out.add(vec);
				}

			}
			return out;
		} else {
			return null;
		}
	}

	public static List<Vector2f> getVector2fListFromFloatArray(float[] inp) {
		if (inp.length > 0) {
			List<Vector2f> out = new ArrayList<>();
			Vector2f vec = null;
			for (int i = 0; i < inp.length; i++) {
				if (i % 2 == 0) {
					vec = new Vector2f();
					vec.x = inp[i];
				} else if (i % 2 == 1) {
					vec.y = inp[i];
					out.add(vec);
				}
			}
			return out;
		} else {
			return null;
		}
	}

	public static List<Float> getFloatListFromFloatArray(float[] inp) {
		if (inp.length > 0) {
			List<Float> out = new ArrayList<>();
			for (int i = 0; i < inp.length; i++) {
				out.add(inp[i]);
			}
			return out;
		} else {
			return null;
		}
	}

	public static List<Integer> getIntegerListFromIntegerArray(int[] inp) {
		if (inp.length > 0) {
			List<Integer> out = new ArrayList<>();
			for (int i = 0; i < inp.length; i++) {
				out.add(inp[i]);
			}
			return out;
		} else {
			return null;
		}
	}

	public static boolean existsResourceFile(String fileName) {
		boolean result;
		try (InputStream is = Utils.class.getResourceAsStream(fileName)) {
			result = is != null;
		} catch (Exception excp) {
			result = false;
		}
		return result;
	}

	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
		ByteBuffer buffer;

		resource = "./source/" + resource;

		Path path = Paths.get(resource);
		if (Files.isReadable(path)) {
			try (SeekableByteChannel fc = Files.newByteChannel(path)) {
				buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
				while (fc.read(buffer) != -1)
					;
			}
		} else {
			try (InputStream source = Utils.class.getResourceAsStream(resource);
					ReadableByteChannel rbc = Channels.newChannel(source)) {
				buffer = createByteBuffer(bufferSize);

				while (true) {
					int bytes = rbc.read(buffer);
					if (bytes == -1) {
						break;
					}
					if (buffer.remaining() == 0) {
						buffer = resizeBuffer(buffer, buffer.capacity() * 2);
					}
				}
			}
		}

		buffer.flip();
		return buffer;
	}

	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}


	public static List<Vector2f> getVector2fListFromFloatBuffer(FloatBuffer inp) {
		List<Vector2f> out = new ArrayList<>();

		while (inp.hasRemaining()) {
			Vector2f vec = new Vector2f();
			if (inp.position() % 2 == 0) {
				vec.x = inp.get();
			} else if (inp.position() % 2 == 1) {
				vec.y = inp.get();
				out.add(vec);
			}
		}

		return out;
	}

	public static List<Vector3f> getVector3fListFromFloatBuffer(FloatBuffer inp) {
		List<Vector3f> out = new ArrayList<>();

		while (inp.hasRemaining()) {
			Vector3f vec = new Vector3f();
			if (inp.position() % 3 == 0) {
				vec.x = inp.get();
			} else if (inp.position() % 3 == 1) {
				vec.y = inp.get();
				out.add(vec);
			} else if (inp.position() % 3 == 1) {
				vec.z = inp.get();
				out.add(vec);
			}

		}

		return out;
	}

	public static List<Float> getFloatListFromFloatBuffer(FloatBuffer inp) {
		List<Float> out = new ArrayList<>();

		while (inp.hasRemaining()) {
			out.add((float) inp.get());
		}

		return out;
	}

	public static List<Integer> getIntegerListFromIntegerBuffer(IntBuffer inp) {
		List<Integer> out = new ArrayList<>();

		while (inp.hasRemaining()) {
			out.add((int) inp.get());
		}

		return out;
	}

	public static void cameraRotate(Group root3D, Scene scene, DoubleProperty angleX, DoubleProperty angleY,
			double anchorAngleX, double anchorAngleY, double scenex, double sceney) {
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

	public static KeyFrame convertToKeyFrame(final float t, final Affine jointAffine, final Affine keyAffine,
			final Interpolator interpolator) {
		final Duration duration = new Duration(t);
		final List<KeyValue> kvs = convertToKeyValues(jointAffine, keyAffine, interpolator);
		final KeyValue[] kvs2 = kvs.toArray(new KeyValue[kvs.size()]);
		return new KeyFrame(duration, kvs2);
	}

	public static KeyValue convertToKeyValue(final WritableValue<Number> target, final Number endValue,
			final Interpolator interpolator) {
		return new KeyValue(target, endValue, interpolator);
	}

	public static List<KeyValue> convertToKeyValues(final Affine jointAffine, final Affine keyAffine,
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

	public static TriangleMesh createCubeMesh(float cubeSide) {

		float width = cubeSide / 2f;
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
		mesh.getFaceSmoothingGroups().setAll(faceSmoothingGroups);

		return mesh;
	}

	public static class GLSlider extends Group {

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

	public static float[] extractFloatArray(final String content) {
		final String[] numbers = content.split("\\s+");
		final float[] array = new float[numbers.length];
		for (int i = 0; i < numbers.length; i++) {
			array[i] = Float.parseFloat(numbers[i].trim());
		}
		return array;
	}

	public static double[] extractDoubleArray(final String content) {
		final float[] floatArray = extractFloatArray(content);
		return IntStream.range(0, floatArray.length).mapToDouble(i -> floatArray[i]).toArray();
	}

	public static int[] extractIntArray(final String content) {
		final float[] floatArray = extractFloatArray(content);
		final int[] intArray = new int[floatArray.length];
		for (int i = 0; i < floatArray.length; ++i) {
			intArray[i] = (int) floatArray[i];
		}
		return intArray;
	}

	public static Stream<NodeFx> getNodeFxChildStream(final Group group) {
		return group.getChildren().stream().filter(child -> child instanceof NodeFx).map(child -> (NodeFx) child);
	}

	public static float toSFN(float inp) {
		String job = String.format("%.10e", inp);
		return Float.parseFloat(job.replace(',', '.'));
	}

	public static String read(String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
          BufferedReader in= new BufferedReader(new FileReader(
            new File(fileName).getAbsoluteFile()));
          try {
            String s;
            while((s = in.readLine()) != null) {
              sb.append(s);
              sb.append("\n");
            }
          } finally {
            in.close();
          }
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
        return sb.toString();
      }
      // Write a single file in one method call:
    
	public static void write(String fileName, String text) {
        try {
          PrintWriter out = new PrintWriter(
            new File(fileName).getAbsoluteFile());
          try {
            out.print(text);
          } finally {
            out.close();
          }
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }

	@SuppressWarnings("unchecked")
	public static double[] AffineListToDouble(List<Affine> inputList) {
		double[] out = new double[inputList.size() * 16];
		
		List<double[]> job = (List<double[]>) inputList.stream().map(v -> (double[])v.toArray(MatrixType.MT_3D_4x4)).collect(Collectors.toList());
		
		int index = 0;
		
		for (double[] dm : job) {
			for (int i = 0; i < dm.length; i++) {
				out[index++] = dm[i];
			}
		}
		
		return out;
	}
	
	public static void printJoints(NodeFx inp) {
		
		if (inp.isJoint()) {
			System.out.println("node: "  + (++countJoint) + ". " + inp.name);
		}
		
		for (Node item : inp.getChildren()) {
			printJoints((NodeFx)item);
		}
	}

	public static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {
		Matrix4f result = new Matrix4f();
		result.m00(aiMatrix4x4.a1());
		result.m10(aiMatrix4x4.a2());
		result.m20(aiMatrix4x4.a3());
		result.m30(aiMatrix4x4.a4());
		result.m01(aiMatrix4x4.b1());
		result.m11(aiMatrix4x4.b2());
		result.m21(aiMatrix4x4.b3());
		result.m31(aiMatrix4x4.b4());
		result.m02(aiMatrix4x4.c1());
		result.m12(aiMatrix4x4.c2());
		result.m22(aiMatrix4x4.c3());
		result.m32(aiMatrix4x4.c4());
		result.m03(aiMatrix4x4.d1());
		result.m13(aiMatrix4x4.d2());
		result.m23(aiMatrix4x4.d3());
		result.m33(aiMatrix4x4.d4());

		return result;
	}

	public static Affine adaptedMatrix(Matrix4f matrix) {
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

	
}
