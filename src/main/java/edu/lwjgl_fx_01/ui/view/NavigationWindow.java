package edu.lwjgl_fx_01.ui.view;

import jfxtras.labs.scene.control.window.Window;

public class NavigationWindow extends Window{
	private String Name = "Navigation window";
	
	private int Id = 1;
	
	public NavigationWindow() {
		super();
		this.setTitle(Name);
		this.setPrefSize(250, 690);
	}
	
	

}
