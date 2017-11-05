package edu.lwjgl_fx_01.ui.model;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

@SuppressWarnings({ "unused", "restriction" })
public class Model{
	
	final static Logger logger = LogManager.getLogger(Model.class);
	
	public void copyText(TextArea area, TextField field){
		area.setText(area.getText() + "\n" + field.getText());
	}
	
	
}