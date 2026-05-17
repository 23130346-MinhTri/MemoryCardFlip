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

/**
 * UC-11: Hiển thị thông báo Streak/Combo
 *
 * Bước 11.1.6 - 11.1.8:
 * Animation hiển thị combo
 */
public class ComboNotificationController implements Initializable {

    // ── FXML Components ───────────────────────────────────────

    @FXML
    private StackPane rootPane;

    @FXML
    private StackPane bubblePane;

    @FXML
    private Label lblComboIcon;

    @FXML
    private Label lblComboText;

    @FXML
    private Label lblSubText;

    // ── Animation ─────────────────────────────────────────────

    private FadeTransition currentFadeOut;

    // ── Initialize ────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Ẩn notification khi khởi tạo
        if (rootPane != null) {

            rootPane.setOpacity(0);

            rootPane.setVisible(false);
        }
    }
    // UC-11: Hiển thị thông báo combo
    /**
     * [UC-11]
     * Hiển thị thông báo combo/streak.
     *
     * Bước 11.1.6:
     * Được gọi khi comboCount >= 2
     *
     * Bước 11.1.7:
     * Pop-in animation (200ms)
     *
     * Bước 11.1.8:
     * Hiển thị 1.5s → fade-out (300ms)
     *
     * Bước 11.2.4:
     * Phân cấp hiển thị theo comboCount
     *
     * Bước 11.2.5:
     * Xử lý chồng animation/effect
     */
    public void showCombo(int comboCount) {

        // [UC-11.2.5]
        // Dừng animation cũ nếu đang chạy
        if (currentFadeOut != null) {
            currentFadeOut.stop();
        }

        // [UC-11.1.6]
        // Chỉ hiển thị khi combo >= 2
        if (comboCount >= 2) {

            // Hiển thị số combo
            lblComboText.setText("x" + comboCount);


            // [UC-11.2.4]
            // Phân cấp hiển thị theo comboCount

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

            // Hiển thị root pane
            rootPane.setVisible(true);

            // ═══════════════════════════════════════════════════
            // [UC-11.1.7]
            // Pop-in animation (Scale + Fade)
            // ═══════════════════════════════════════════════════

            ScaleTransition popIn =
                    new ScaleTransition(
                            Duration.millis(200),
                            bubblePane
                    );

            popIn.setFromX(0);

            popIn.setFromY(0);

            popIn.setToX(1);

            popIn.setToY(1);

            FadeTransition fadeIn =
                    new FadeTransition(
                            Duration.millis(200),
                            rootPane
                    );

            fadeIn.setFromValue(0);

            fadeIn.setToValue(1);

            // Chạy animation
            fadeIn.play();

            popIn.play();

            // ═══════════════════════════════════════════════════
            // [UC-11.1.8]
            // Hiển thị 1.5s → fade-out (300ms)
            // ═══════════════════════════════════════════════════

            FadeTransition fadeOut =
                    new FadeTransition(
                            Duration.millis(300),
                            rootPane
                    );

            currentFadeOut = fadeOut;

            // Delay trước khi fade out
            fadeOut.setDelay(Duration.seconds(1.5));

            fadeOut.setToValue(0);

            fadeOut.setOnFinished(
                    e -> rootPane.setVisible(false)
            );

            fadeOut.play();
        }
    }

    // ── Hide notification ─────────────────────────────────────

    /**
     * Ẩn combo notification bằng fade-out animation.
     */
    public void hide() {

        if (rootPane != null && rootPane.isVisible()) {

            FadeTransition fadeOut =
                    new FadeTransition(
                            Duration.millis(200),
                            rootPane
                    );

            fadeOut.setToValue(0);

            fadeOut.setOnFinished(
                    e -> rootPane.setVisible(false)
            );

            fadeOut.play();
        }
    }
}