package edu.lwjgl_fx_01.ui.view;

import jfxtras.labs.scene.control.window.Window;

public class MainWindow extends Window {
	private String Name = "Workspace";
	private int Id = 2;
	
	public MainWindow() {
		super();
		this.setTitle(Name);
		this.setPrefSize(774, 690);
	}
	
}
