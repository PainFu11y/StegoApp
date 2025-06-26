package org.platform;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class StegoApp extends Application {

    private File selectedImage;
    private final Label statusLabel = new Label();

    public static void main(String[] args) {
        System.out.println("Main started!");
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        System.out.println("Program starts!");
        stage.setTitle("LSB Steganography");


        Button chooseButton = new Button(" Choose Image");
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);

        chooseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select a PNG image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG files", "*.png")
            );
            selectedImage = fileChooser.showOpenDialog(stage);
            if (selectedImage != null) {
                imageView.setImage(new Image(selectedImage.toURI().toString()));
                statusLabel.setText("Selected: " + selectedImage.getName());
            }
        });


        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter secret message here...");
        messageArea.setWrapText(true);


        Button encodeButton = new Button(" Encode");
        encodeButton.setOnAction(e -> {
            if (selectedImage == null || messageArea.getText().isEmpty()) {
                statusLabel.setText(" Please choose an image and enter a message.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save image with hidden message");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG files", "*.png")
            );
            fileChooser.setInitialFileName("encoded.png");
            File output = fileChooser.showSaveDialog(stage);
            if (output != null) {
                try {
                    LSBEncoder.encode(selectedImage.getAbsolutePath(), output.getAbsolutePath(), messageArea.getText());
                    statusLabel.setText(" Message successfully encoded into image.");
                } catch (IOException ex) {
                    statusLabel.setText(" Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });


        Button decodeButton = new Button(" Decode");
        decodeButton.setOnAction(e -> {
            if (selectedImage == null) {
                statusLabel.setText(" Please select an image first.");
                return;
            }
            try {
                String decoded = LSBDecoder.decode(selectedImage.getAbsolutePath());
                messageArea.setText(decoded);
                statusLabel.setText(" Message successfully decoded.");
            } catch (IOException ex) {
                statusLabel.setText(" Error while decoding the message.");
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(15,
                chooseButton, imageView,
                new Label("Message:"),
                messageArea,
                new HBox(20, encodeButton, decodeButton),
                statusLabel
        );
        root.setPadding(new Insets(20));
        root.getStyleClass().add("vbox");

        chooseButton.setPrefWidth(150);
        encodeButton.setPrefWidth(100);
        decodeButton.setPrefWidth(100);

        HBox buttonsBox = new HBox(20, encodeButton, decodeButton);
        buttonsBox.getStyleClass().add("hbox");

        root.getChildren().set(4, buttonsBox);

        root.setOnDragOver(event -> {
            if (event.getGestureSource() != root && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        root.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (file.getName().toLowerCase().endsWith(".png")) {
                    selectedImage = file;
                    imageView.setImage(new Image(selectedImage.toURI().toString()));
                    statusLabel.setText("Selected (dragged): " + selectedImage.getName());
                    success = true;
                } else {
                    statusLabel.setText(" Unsupported file type. Only PNG is allowed.");
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        Scene scene = new Scene(root, 450, 600);
        stage.setScene(scene);

        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

    }
}
