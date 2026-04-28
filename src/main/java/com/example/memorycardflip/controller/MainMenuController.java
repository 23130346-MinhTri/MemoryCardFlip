package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Difficulty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
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

    @FXML
    public void onStartEasy() {
        openGameScene(Difficulty.EASY);
    }

    @FXML
    public void onStartMedium() {
        openGameScene(Difficulty.MEDIUM);
    }

    @FXML
    public void onStartHard() {
        openGameScene(Difficulty.HARD);
    }

    private void openGameScene(Difficulty difficulty) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            Parent root = loader.load();

            GameController gameController = loader.getController();
            gameController.setDifficulty(difficulty);

            Stage stage = (Stage) btnEasy.getScene().getWindow();
            Scene scene = new Scene(root, 900, 700);
            stage.setTitle("Memory Card Flip - " + difficulty.getDisplayName());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot open game scene", exception);
        }
    }
}