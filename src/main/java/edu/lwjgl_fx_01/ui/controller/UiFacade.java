package edu.lwjgl_fx_01.ui.controller;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jfxtras.labs.scene.control.window.CloseIcon;
import jfxtras.labs.scene.control.window.MinimizeIcon;
import jfxtras.labs.scene.control.window.Window;

@SuppressWarnings("restriction")
public class UiFacade extends Group {
	
	private Stage stage;

	public UiFacade(Stage primaryStage) {

		this.stage = primaryStage;

		this.stage.setTitle("Game modeling system");

		Group root = new Group();

		RootLayoutController rlController = new RootLayoutController();

		rlController.getBorderPane().prefWidthProperty().bind(stage.widthProperty());

		Controller controller = new Controller();
		
		rlController.setCenter(controller.createMainWindow());

		rlController.setLeft(controller.createNavigationWindow());
		
		root.getChildren().addAll(rlController);

		Scene scene = new Scene(root, 1040, 770);

		scene.getStylesheets()
				.add(getClass().getClassLoader().getResource("edu/lwjgl_fx_01/ui/view/Main.css").toExternalForm());

		root.getStyleClass().add("modal-dialog-content");

		stage.setScene(scene);

		stage.getIcons().add(new Image(getClass().getClassLoader().getResource("image/engine.png").toString()));

		stage.setResizable(true);

		stage.centerOnScreen();

	}

	public void show() {
		stage.show();
	}

	private Pane createShadowPane() {
		Pane shadowPane = new Pane();
		/*
		 * int shadowSize = 3; // a "real" app would do this in a CSS stylesheet.
		 * shadowPane.setStyle( "-fx-background-color: white;" +
		 * "-fx-effect: dropshadow(gaussian, red, " + shadowSize + ", 0, 0, 0);" +
		 * "-fx-background-insets: " + shadowSize + ";" );
		 * 
		 * Rectangle innerRect = new Rectangle(); Rectangle outerRect = new Rectangle();
		 * shadowPane.layoutBoundsProperty().addListener( (observable, oldBounds,
		 * newBounds) -> { innerRect.relocate( newBounds.getMinX() + shadowSize,
		 * newBounds.getMinY() + shadowSize ); innerRect.setWidth(newBounds.getWidth() -
		 * shadowSize * 2); innerRect.setHeight(newBounds.getHeight() - shadowSize * 2);
		 * 
		 * outerRect.setWidth(newBounds.getWidth());
		 * outerRect.setHeight(newBounds.getHeight());
		 * 
		 * Shape clip = Shape.subtract(outerRect, innerRect); shadowPane.setClip(clip);
		 * } );
		 */
		Window w = new Window("My MDI Window");
		w.getLeftIcons().add(new CloseIcon(w));
		w.getRightIcons().add(new MinimizeIcon(w));
		w.getContentPane().getChildren().add(new Label("Hello world!"));
		shadowPane.getChildren().add(w);
		return shadowPane;
	}

}
