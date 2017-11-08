package edu.lwjgl_fx_01.ui.controller;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import edu.lwjgl_fx_01.ui.model.engine.LwjglScene;
import edu.lwjgl_fx_01.ui.model.engine.ModelScene;
import edu.lwjgl_fx_01.ui.model.engine.graph.ModelNode;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.AnimMeshesLoader;
import edu.lwjgl_fx_01.ui.model.engine.loaders.assimp.StaticMeshesLoader;
import javafx.animation.Timeline;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Shows how to load models in Wavefront obj and mlt format with Assimp binding and render them with
 * OpenGL.
 *
 * @author Zhang Hai, Iha
 */
public class AssimpLoader {
    long window;
    int width = 1;
    int height = 1;
    int fbWidth = 1;
    int fbHeight = 1;
    float fov = 60;
    float rotation;

    int program;
    int vertexAttribute;
    int normalAttribute;
    int modelMatrixUniform;
    int viewProjectionMatrixUniform;
    int normalMatrixUniform;
    int lightPositionUniform;
    int viewPositionUniform;
    int ambientColorUniform;
    int diffuseColorUniform;
    int specularColorUniform;

    private LwjglScene scene;
    private LinkedList<ModelScene> sceneFx;
    
    AnimMeshesLoader model = new AnimMeshesLoader();
    AnimMeshesLoader model2 = new AnimMeshesLoader();
    StaticMeshesLoader model3 = new StaticMeshesLoader();

    Matrix4f modelMatrix = new Matrix4f().rotateY(0.5f * (float) Math.PI).scale(1.5f, 1.5f, 1.5f);
    Matrix4f viewMatrix = new Matrix4f();
    Matrix4f projectionMatrix = new Matrix4f();
    Matrix4f viewProjectionMatrix = new Matrix4f();
    Vector3f viewPosition = new Vector3f();
    Vector3f lightPosition = new Vector3f(-5f, 5f, 5f);

    GLCapabilities caps;
    GLFWKeyCallback keyCallback;
    GLFWFramebufferSizeCallback fbCallback;
    GLFWWindowSizeCallback wsCallback;
    GLFWCursorPosCallback cpCallback;
    GLFWScrollCallback sCallback;
    Callback debugProc;
    
    public AssimpLoader(String fileName) throws Exception {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(width, height,
                "Wavefront obj model loading with Assimp demo", NULL, NULL);
        if (window == NULL)
            throw new AssertionError("Failed to create the GLFW window");
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        
        glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);
        glfwSetCursorPos(window, width / 2, height / 2);

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize),
                memAddress(framebufferSize) + 4);
        fbWidth = framebufferSize.get(0);
        fbHeight = framebufferSize.get(1);

        caps = GL.createCapabilities();
        glfwHideWindow(window); //Hide windows
        if (!caps.GL_ARB_shader_objects) {
            throw new AssertionError("This demo requires the ARB_shader_objects extension.");
        }
        if (!caps.GL_ARB_vertex_shader) {
            throw new AssertionError("This demo requires the ARB_vertex_shader extension.");
        }
        if (!caps.GL_ARB_fragment_shader) {
            throw new AssertionError("This demo requires the ARB_fragment_shader extension.");
        }
        debugProc = GLUtil.setupDebugMessageCallback();

        glClearColor(0f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);

        if (fileName.matches("obj")) {
	        if (fileName.isEmpty()) {
	        	loadAnimationModel("./source/models/animated_cube.dae");
	        } else {
	        	loadAnimationModel(fileName);
	        }
        } else {
	        if (fileName.isEmpty()) {
	        	loadAnimationModel("./source/models/animated_cube.dae");
	        } else {
	        	loadAnimationModel(fileName);
	        }
	    }
    }
    
    @SuppressWarnings("static-access")
	void loadAnimationModel(String fileName) throws Exception {
/*    	String fileName = Thread.currentThread().getContextClassLoader()
                          .getResource("objects/magnet.obj").getFile();
*/       
        //source = model.loadAnimGameItem(fileName, "objects");
    	scene = model2.loadAnimGameItem(fileName, "");
    	sceneFx = model2.getScenesfx();
    }

	void loadModel(String fileName) throws Exception {
    	scene = model3.load(fileName, "");
    }
	
	public LwjglScene getScene() {
		return scene;
	}

	public LinkedList<ModelScene> getSceneFx() {
		return sceneFx;
	}
	
	public ModelNode getModelScene() {
		return model2.getModelNode();
	}
    
	public String getBoneName(int index) {
		return model2.getBoneName(index);
	}
	
	public int getQtyBones() {
		return model2.getQtyBones();
	}
  
	public Map<String, Timeline> getTimelines() {
		return model2.getTimelines();
	}

}