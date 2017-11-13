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
import edu.lwjgl_fx_01.ui.model.engine.SceneFx;
import edu.lwjgl_fx_01.ui.model.engine.graph.NodeFx;
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
 * Shows how to load models in with Assimp binding and render them with OpenGL.
 *
 * @author Zhang Hai, Iha
 */
public class AssimpLoader {
    long window;
    int width = 1;
    int height = 1;
    
    private SceneFx scene;
    StaticMeshesLoader staticModel = new StaticMeshesLoader();
    AnimMeshesLoader animationModel = new AnimMeshesLoader();

    GLCapabilities caps;
    Callback debugProc;
    
    public AssimpLoader(String fileName) throws Exception {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(width, height,"", NULL, NULL);
        if (window == NULL)
            throw new AssertionError("Failed to create the GLFW window");
        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize),
                memAddress(framebufferSize) + 4);
        glfwHideWindow(window); //Hide windows
        caps = GL.createCapabilities();
        
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
	        	loadAnimationModel("./models/guard.dae");
	        } else {
	        	loadAnimationModel(fileName);
	        }
        } else {
	        if (fileName.isEmpty()) {
	        	loadAnimationModel("./models/guard.dae");
	        } else {
	        	loadAnimationModel(fileName);
	        }
	    }
    }
    
    @SuppressWarnings("static-access")
	void loadAnimationModel(String fileName) throws Exception {
		scene = animationModel.loadAnimGameItem(fileName, "");
    }

	void loadModel(String fileName) throws Exception {
    	scene = staticModel.load(fileName, "");
    }
	
	public SceneFx getScene() {
		return scene;
	}

	public Map<String, Timeline> getTimelines() {
		return animationModel.getTimelines();
	}

}