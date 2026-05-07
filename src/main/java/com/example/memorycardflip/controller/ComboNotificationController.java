package com.example.memorycardflip.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;

public class ComboNotificationController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private StackPane bubblePane;
    @FXML private Label lblComboIcon;
    @FXML private Label lblComboText;
    @FXML private Label lblSubText;
    private FadeTransition currentFadeOut;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (rootPane != null) {
            rootPane.setOpacity(0);
            rootPane.setVisible(false);
        }
    }
    public void showCombo(int comboCount) {
        if (currentFadeOut != null) currentFadeOut.stop();

        if (comboCount >= 2) {
            lblComboText.setText("x" + comboCount);

            if (comboCount >= 5) {
                lblComboIcon.setText("🔥🔥🔥");
                lblSubText.setText("MEGA STREAK!!!");
            } else if (comboCount >= 4) {
                lblComboIcon.setText("🔥🔥");
                lblSubText.setText("SUPER STREAK!!");
            } else if (comboCount >= 3) {
                lblComboIcon.setText("🔥🔥");
                lblSubText.setText("HOT STREAK!");
            } else {
                lblComboIcon.setText("🔥");
                lblSubText.setText("STREAK!");
            }

            rootPane.setVisible(true);

            ScaleTransition popIn = new ScaleTransition(Duration.millis(200), bubblePane);
            popIn.setFromX(0);
            popIn.setFromY(0);
            popIn.setToX(1);
            popIn.setToY(1);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), rootPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            fadeIn.play();
            popIn.play();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), rootPane);
            currentFadeOut = fadeOut;
            fadeOut.setDelay(Duration.seconds(1.5));
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> rootPane.setVisible(false));
            fadeOut.play();
        }
    }

    public void hide() {
        if (rootPane != null && rootPane.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), rootPane);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> rootPane.setVisible(false));
            fadeOut.play();
        }
    }
}