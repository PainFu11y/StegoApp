package org.platform.ui;

import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class UIFactory {

    public static Button createButton(String text, double width) {
        Button button = new Button(text);
        if (width > 0) {
            button.setPrefWidth(width);
        } else {
            button.setMinWidth(100);
        }
        return button;
    }

    public static TextArea createMessageArea() {
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter secret message here...");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(5);
        VBox.setVgrow(messageArea, Priority.ALWAYS);
        return messageArea;
    }

    public static ScrollPane createImageScrollPane(Group imageGroup) {
        ScrollPane scrollPane = new ScrollPane(imageGroup);
        scrollPane.setPannable(true);
        scrollPane.setPrefHeight(400);
        return scrollPane;
    }

    public static ComboBox<String> createImageSelector() {
        ComboBox<String> selector = new ComboBox<>();
        selector.getItems().addAll("Original", "Encoded", "Difference", "Binary Difference");
        selector.setValue("Original");
        return selector;
    }

    public static Label createStatusLabel() {
        return new Label();
    }

    public static ImageView createImageView() {
        ImageView view = new ImageView();
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitWidth(600);
        return view;
    }
}
