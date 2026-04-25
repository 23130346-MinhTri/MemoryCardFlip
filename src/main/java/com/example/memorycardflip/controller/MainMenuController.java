package com.example.memorycardflip.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {
    @FXML private Label scoreEasy;
    @FXML private Label scoreMedium;
    @FXML private Label scoreHard;

    @FXML private Button btnEasy;
    @FXML private Button btnMedium;
    @FXML private Button btnHard;
    @FXML private Button btnSound;

    private boolean soundEnabled = true;
    public MainMenuController() {
    }
    // ── Lifecycle ─────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addKeyboardShortcuts();
    }

    private void addKeyboardShortcuts() {
        btnEasy.getScene();
        // Gọi sau khi scene đã ready (dùng listener)
        btnEasy.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        default -> { }
                    }
                });
            }
        });
    }
    // ── Event Handlers ────────────────────────────────────────
    /** Toggle âm thanh on/off */
    @FXML
    public void onSoundToggle() {
        soundEnabled = !soundEnabled;
        btnSound.setText(soundEnabled ? "🔊  Âm thanh" : "🔇  Tắt tiếng");
        // TODO: AudioService.getInstance().setEnabled(soundEnabled);
    }
    /** Nút Về game — hiển thị dialog thông tin */
    @FXML
    public void onAbout() {
        // TODO: SceneManager.getInstance().showAboutDialog();
    }
    /** Nút Lịch sử — hiển thị bảng điểm */
    @FXML
    public void onHistory() {
        // TODO: SceneManager.getInstance().showHistoryDialog();
    }
}