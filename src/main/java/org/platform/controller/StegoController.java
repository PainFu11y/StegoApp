package org.platform.controller;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.platform.exception.MessageTooLargeException;
import org.platform.logic.ImageDifferenceUtil;
import org.platform.logic.LSBDecoder;
import org.platform.logic.LSBEncoder;
import org.platform.ui.UIFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.platform.ui.feedback.ErrorSound.playErrorSound;
import static org.platform.ui.feedback.ErrorAnimator.addErrorStatusStyle;

public class StegoController {

    private File selectedImage;
    private Image originalImage;
    private Image encodedImage;
    private Image diffImage;
    private Image binaryDiffImage;

    private final Label statusLabel = UIFactory.createStatusLabel();
    private final ImageView displayView = UIFactory.createImageView();
    private final ComboBox<String> imageSelector = UIFactory.createImageSelector();
    private double scaleFactor = 1.0;

    public void initializeUI(ScrollPane rootPane, Stage stage) {
        Button chooseButton = UIFactory.createButton("Choose Image", 150);
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

        imageSelector.setOnAction(e -> updateDisplayedImage());

        Group imageGroup = new Group(displayView);
        ScrollPane imageScrollPane = UIFactory.createImageScrollPane(imageGroup);
        imageScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                scaleFactor *= e.getDeltaY() > 0 ? 1.1 : 1 / 1.1;
                applyZoom();
                e.consume();
            }
        });

        Button zoomInButton = UIFactory.createButton("Zoom In", 0);
        Button zoomOutButton = UIFactory.createButton("Zoom Out", 0);
        zoomInButton.setOnAction(e -> {
            scaleFactor *= 1.1;
            applyZoom();
        });
        zoomOutButton.setOnAction(e -> {
            scaleFactor /= 1.1;
            applyZoom();
        });

        TextArea messageArea = UIFactory.createMessageArea();

        Button encodeButton = UIFactory.createButton("Encode", 100);
        Button decodeButton = UIFactory.createButton("Decode", 100);

        encodeButton.setOnAction(e -> encodeMessage(messageArea, stage));
        decodeButton.setOnAction(e -> decodeMessage(messageArea));

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
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #121212;");

        rootPane.setContent(content);
        rootPane.setStyle("-fx-background: #121212;");

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
    }

    private void encodeMessage(TextArea messageArea, Stage stage) {
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
                BufferedImage binaryDiff = ImageDifferenceUtil.generateBinaryDifferenceImage(original, encoded);

                File tempEncoded = File.createTempFile("encoded_preview", ".png");
                File tempDiff = File.createTempFile("diff_preview", ".png");
                File tempBinaryDiff = File.createTempFile("binary_diff_preview", ".png");

                tempEncoded.deleteOnExit();
                tempDiff.deleteOnExit();
                tempBinaryDiff.deleteOnExit();

                ImageIO.write(encoded, "png", tempEncoded);
                ImageIO.write(diff, "png", tempDiff);
                ImageIO.write(binaryDiff, "png", tempBinaryDiff);

                encodedImage = new Image(tempEncoded.toURI().toString());
                diffImage = new Image(tempDiff.toURI().toString());
                binaryDiffImage = new Image(tempBinaryDiff.toURI().toString());

                imageSelector.setValue("Encoded");
                updateDisplayedImage();

            } catch (MessageTooLargeException | IOException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                addErrorStatusStyle(statusLabel);
                ex.printStackTrace();
            }
        }
    }

    private void decodeMessage(TextArea messageArea) {
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

    private void removeErrorStatusStyle() {
        statusLabel.getStyleClass().removeAll("error");
    }
}