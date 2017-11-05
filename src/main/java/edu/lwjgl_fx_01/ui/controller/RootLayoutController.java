package edu.lwjgl_fx_01.ui.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

@SuppressWarnings("restriction")
public class RootLayoutController extends BorderPane {

	@FXML
	private BorderPane borderPane;

	public RootLayoutController() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource("edu/lwjgl_fx_01/ui/view/RootLayout.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		// getStyleClass().add("main-window");

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@FXML
	private void handleNewWindow() {
	    	
	    	WavefrontObjDemo wod = new WavefrontObjDemo();
	    	
	    	wod.run();
	}

	@FXML
	private void handleExit() {
		System.exit(0);
	}

	public BorderPane getBorderPane() {
		return borderPane;
	}

}
