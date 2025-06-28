package org.platform;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.platform.exception.MessageTooLargeException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.platform.error.ErrorSound.playErrorSound;
import static org.platform.error.ErrorAnimator.addErrorStatusStyle;

public class StegoApp extends Application {

    private File selectedImage;
    private Image originalImage;
    private Image encodedImage;
    private Image diffImage;
    private Image binaryDiffImage;

    private final Label statusLabel = new Label();
    private final ImageView displayView = new ImageView();
    private final ComboBox<String> imageSelector = new ComboBox<>();

    private double scaleFactor = 1.0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("LSB Steganography");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        Button chooseButton = new Button("Choose Image");
        chooseButton.setPrefWidth(150);
        chooseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select a PNG image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files", "*.png"));
            selectedImage = fileChooser.showOpenDialog(stage);
            if (selectedImage != null) {
                originalImage = new Image(selectedImage.toURI().toString());
                imageSelector.setValue("Original");
                updateDisplayedImage();
                statusLabel.setText("Selected: " + selectedImage.getName());
                removeErrorStatusStyle();
            }
        });

        imageSelector.getItems().addAll("Original", "Encoded", "Difference", "Binary Difference");
        imageSelector.setValue("Original");
        imageSelector.setOnAction(e -> updateDisplayedImage());

        displayView.setPreserveRatio(true);
        displayView.setSmooth(true);
        displayView.setFitWidth(600);

        Group imageGroup = new Group(displayView);
        ScrollPane imageScrollPane = new ScrollPane(imageGroup);
        imageScrollPane.setPannable(true);
        imageScrollPane.setFitToWidth(false);
        imageScrollPane.setFitToHeight(false);
        imageScrollPane.setPrefHeight(400);

        imageScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                if (e.getDeltaY() > 0) {
                    scaleFactor *= 1.1;
                } else {
                    scaleFactor /= 1.1;
                }
                applyZoom();
                e.consume();
            }
        });


        Button zoomInButton = new Button("Zoom In");
        Button zoomOutButton = new Button("Zoom Out");
        zoomInButton.setOnAction(e -> {
            scaleFactor *= 1.1;
            applyZoom();
        });
        zoomOutButton.setOnAction(e -> {
            scaleFactor /= 1.1;
            applyZoom();
        });

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter secret message here...");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(5);
        VBox.setVgrow(messageArea, Priority.ALWAYS);

        Button encodeButton = new Button("Encode");
        Button decodeButton = new Button("Decode");
        encodeButton.setPrefWidth(100);
        decodeButton.setPrefWidth(100);

        encodeButton.setOnAction(e -> {
            if (selectedImage == null || messageArea.getText().isEmpty()) {
                statusLabel.setText("Please choose an image and enter a message.");
                addErrorStatusStyle(statusLabel);
                return;
            }

            try {
                BufferedImage img = ImageIO.read(selectedImage);
                if (!LSBEncoder.canEncode(img, messageArea.getText())) {
                    statusLabel.setText("Message is too long to encode in this image.");
                    addErrorStatusStyle(statusLabel);
                    return;

                }
            } catch (IOException ex) {
                statusLabel.setText("Failed to read the image.");
                addErrorStatusStyle(statusLabel);
                ex.printStackTrace();
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save image with hidden message");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files", "*.png"));
            fileChooser.setInitialFileName("encoded.png");
            File output = fileChooser.showSaveDialog(stage);
            if (output != null) {
                try {
                    long seed = 1000 + new Random().nextInt(Integer.MAX_VALUE - 1000);
                    LSBEncoder.encode(selectedImage.getAbsolutePath(), output.getAbsolutePath(), messageArea.getText(), seed);
                    statusLabel.setText("Message successfully encoded into image.");
                    removeErrorStatusStyle();

                    BufferedImage original = ImageIO.read(selectedImage);
                    BufferedImage encoded = ImageIO.read(output);
                    BufferedImage diff = ImageDifferenceUtil.generateColoredDifferenceImage(original, encoded);
                    BufferedImage binaryDiff = ImageDifferenceUtil.generateBinaryDifferenceImage(original,encoded);

                    File tempEncoded = File.createTempFile("encoded_preview", ".png");
                    File tempDiff = File.createTempFile("diff_preview", ".png");
                    File tempBinaryDiff = File.createTempFile("binary_diff_preview", ".png");
                    tempEncoded.deleteOnExit();
                    tempDiff.deleteOnExit();


                    ImageIO.write(encoded, "png", tempEncoded);
                    ImageIO.write(diff, "png", tempDiff);
                    ImageIO.write(binaryDiff, "png", tempBinaryDiff);

                    encodedImage = new Image(tempEncoded.toURI().toString());
                    diffImage = new Image(tempDiff.toURI().toString());
                    binaryDiffImage = new Image(tempBinaryDiff.toURI().toString());


                    imageSelector.setValue("Encoded");
                    updateDisplayedImage();

                } catch (MessageTooLargeException ex){
                    statusLabel.setText("Message too long to encode in this image.");
                    addErrorStatusStyle(statusLabel);
                }
                catch (IOException ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    addErrorStatusStyle(statusLabel);
                    ex.printStackTrace();
                }
            }
        });

        decodeButton.setOnAction(e -> {
            if (selectedImage == null) {
                statusLabel.setText("Please select an image first.");
                addErrorStatusStyle(statusLabel);
                return;
            }
            try {
                String decoded = LSBDecoder.decode(selectedImage.getAbsolutePath());
                messageArea.setText(decoded);
                statusLabel.setText("Message successfully decoded.");
                removeErrorStatusStyle();
            } catch (IOException ex) {
                statusLabel.setText("Error while decoding the message.");
                ex.printStackTrace();
            }
        });

        VBox content = new VBox(15,
                new Label("Image View:"),
                new HBox(10, imageSelector, zoomInButton, zoomOutButton),
                imageScrollPane,
                chooseButton,
                new Label("Message:"),
                messageArea,
                new HBox(20, encodeButton, decodeButton),
                statusLabel
        );
        content.setStyle("-fx-background-color: #121212;");
        scrollPane.setStyle("-fx-background: #121212;");
        content.setPadding(new Insets(20));

        scrollPane.setContent(content);

        content.setOnDragOver(event -> {
            if (event.getGestureSource() != content && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        content.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (file.getName().toLowerCase().endsWith(".png")) {
                    selectedImage = file;
                    originalImage = new Image(file.toURI().toString());
                    imageSelector.setValue("Original");
                    updateDisplayedImage();
                    statusLabel.setText("Selected (dragged): " + selectedImage.getName());
                    removeErrorStatusStyle();
                    success = true;
                } else {
                    statusLabel.setText("Unsupported file type. Only PNG is allowed.");
                    playErrorSound();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        Scene scene = new Scene(scrollPane, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    private void updateDisplayedImage() {
        switch (imageSelector.getValue()) {
            case "Original" -> displayView.setImage(originalImage);
            case "Encoded" -> displayView.setImage(encodedImage);
            case "Difference" -> displayView.setImage(diffImage);
            case "Binary Difference" -> displayView.setImage(binaryDiffImage);
        }
        scaleFactor = 1.0;
        applyZoom();
    }

    private void applyZoom() {
        displayView.setScaleX(scaleFactor);
        displayView.setScaleY(scaleFactor);
    }

//    private void addErrorStatusStyle(){
//        if (!statusLabel.getStyleClass().contains("error")) {
//            statusLabel.getStyleClass().add("error");
//        }
//        playErrorSound();
//        FadeTransition blink = new FadeTransition(Duration.millis(100), statusLabel);
//        blink.setFromValue(1.0);
//        blink.setToValue(0.3);
//        blink.setCycleCount(4);
//        blink.setAutoReverse(true);
//
//        TranslateTransition bounce = new TranslateTransition(Duration.millis(100), statusLabel);
//        bounce.setByY(-10);
//        bounce.setAutoReverse(true);
//        bounce.setCycleCount(2);
//
//        ParallelTransition animation = new ParallelTransition(blink, bounce);
//        animation.play();
//    }

    private void removeErrorStatusStyle(){
        statusLabel.getStyleClass().removeAll("error");
    }


}
