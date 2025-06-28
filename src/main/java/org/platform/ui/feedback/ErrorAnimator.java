package org.platform.ui.feedback;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

import static org.platform.ui.feedback.ErrorSound.playErrorSound;

public class ErrorAnimator {
    public static void addErrorStatusStyle(Label label){
        if (!label.getStyleClass().contains("error")) {
            label.getStyleClass().add("error");
        }
        playErrorSound();

        FadeTransition blink = new FadeTransition(Duration.millis(100), label);
        blink.setFromValue(1.0);
        blink.setToValue(0.3);
        blink.setCycleCount(4);
        blink.setAutoReverse(true);

        TranslateTransition bounce = new TranslateTransition(Duration.millis(100), label);
        bounce.setByY(-10);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);

        new ParallelTransition(blink, bounce).play();
    }


}
