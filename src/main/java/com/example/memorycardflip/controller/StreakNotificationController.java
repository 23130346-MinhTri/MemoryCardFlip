package com.example.memorycardflip.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StreakNotificationController {
    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Label iconLabel;

    public void setTitle(String title) {
        if (titleLabel != null) titleLabel.setText(title);
    }

    public void setMessage(String message) {
        if (messageLabel != null) messageLabel.setText(message);
    }

    public void setIconText(String text) {
        if (iconLabel != null && text != null) iconLabel.setText(text);
    }
}
