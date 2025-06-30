package org.platform.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import org.platform.controller.StegoController;

public class StegoApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("LSB Steganography");


        ScrollPane rootPane = new ScrollPane();
        rootPane.setFitToWidth(true);
        rootPane.setStyle("-fx-background: #121212;");


        StegoController controller = new StegoController();
        controller.initializeUI(rootPane, stage);


        Scene scene = new Scene(rootPane, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());

        stage.setScene(scene);//sfa
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
