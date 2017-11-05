package edu.lwjgl_fx_01.basic.om;


import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.lwjgl_fx_01.ui.model.Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import jfxtras.labs.scene.control.window.CloseIcon;
import jfxtras.labs.scene.control.window.MinimizeIcon;
import jfxtras.labs.scene.control.window.Window;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;


import edu.lwjgl_fx_01.ui.controller.Controller;
import edu.lwjgl_fx_01.ui.controller.RootLayoutController;
import edu.lwjgl_fx_01.ui.controller.UiFacade;


@SuppressWarnings({ "unused", "restriction" })
public class MainFx extends Application
{
	private Stage stage;
	final static Logger logger = LogManager.getLogger(MainFx.class);
	
	public static void main( String[] args )
    {
		try{
			
			logger.log(Level.INFO, "Application runnig ...");

			launch(args);
		
		}catch(ArithmeticException ex){
			logger.error("Sorry, something wrong!", ex);
		}
    }
    
	@Override
	public void start(Stage stage) throws Exception {
	    
		this.stage = stage;
	    
	    UiFacade uf = new UiFacade(this.stage);
	   
	    uf.show();
   }
		



	
}
