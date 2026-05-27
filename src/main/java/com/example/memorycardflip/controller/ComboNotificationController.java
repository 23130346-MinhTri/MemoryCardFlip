package com.example.memorycardflip.controller;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;

public class ComboNotificationController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private VBox bubblePane;
    @FXML private Label lblComboIcon;
    @FXML private Label lblComboText;
    @FXML private Label lblSubText;
    @FXML private Label lblTagline;
    private FadeTransition currentFadeOut;

    /**
     * [11.1.6] Chuẩn bị trạng thái ẩn ban đầu cho thông báo combo.
     * View chỉ xuất hiện khi showCombo() được gọi sau khi match thành công.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (rootPane != null) {
            rootPane.setOpacity(0);
            rootPane.setVisible(false);
        }
    }

    /**
     * [11.1.6-11.1.8] Hiển thị thông báo combo khi comboCount >= 2.
     *
     * Thứ tự xử lý:
     * - [11.1.6] Chọn kiểu hiển thị theo comboCount, Mega Streak từ comboCount >= 5
     * - [11.1.7] Chạy pop-in animation
     * - [11.1.8] Giữ hiển thị rồi fade-out
     */
    public void showCombo(int comboCount) {
        if (currentFadeOut != null) currentFadeOut.stop();
        bubblePane.getStyleClass().removeAll("level-2", "level-3", "level-4", "level-5");
        rootPane.toFront();
        bubblePane.toFront();

        if (comboCount >= 2) {
            lblComboText.setText("x" + comboCount);

            if (comboCount >= 5) {
                lblComboIcon.setText("⚡⚡⚡");
                lblSubText.setText("MEGA STREAK!!!");
                lblTagline.setText("Cực nóng, thưởng lớn đang kích hoạt");
                bubblePane.getStyleClass().add("level-5");
            } else if (comboCount >= 4) {
                lblComboIcon.setText("⚡⚡");
                lblSubText.setText("SUPER STREAK!!");
                lblTagline.setText("Chuỗi đúng liên tiếp đang bùng nổ");
                bubblePane.getStyleClass().add("level-4");
            } else if (comboCount >= 3) {
                lblComboIcon.setText("🔥⚡");
                lblSubText.setText("HOT STREAK!");
                lblTagline.setText("Bonus đang sáng rực trên màn hình");
                bubblePane.getStyleClass().add("level-3");
            } else {
                lblComboIcon.setText("🔥");
                lblSubText.setText("STREAK!");
                lblTagline.setText("Tiếp tục giữ nhịp để nhận bonus");
                bubblePane.getStyleClass().add("level-2");
            }

            rootPane.setVisible(true);
            bubblePane.setScaleX(0.7);
            bubblePane.setScaleY(0.7);

            ScaleTransition popIn = new ScaleTransition(Duration.millis(200), bubblePane);
            popIn.setFromX(0.7);
            popIn.setFromY(0.7);
            popIn.setToX(1);
            popIn.setToY(1);

            ScaleTransition pulse = new ScaleTransition(Duration.millis(160), bubblePane);
            pulse.setFromX(1);
            pulse.setFromY(1);
            pulse.setToX(1.06);
            pulse.setToY(1.06);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);

            RotateTransition rotate = new RotateTransition(Duration.millis(220), bubblePane);
            rotate.setFromAngle(-2);
            rotate.setToAngle(2);
            rotate.setAutoReverse(true);
            rotate.setCycleCount(2);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), rootPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            fadeIn.play();
            popIn.play();
            pulse.play();
            rotate.play();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), rootPane);
            currentFadeOut = fadeOut;
            fadeOut.setDelay(comboCount >= 4 ? Duration.seconds(2.3) : Duration.seconds(1.8));
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                rootPane.setVisible(false);
                bubblePane.getStyleClass().removeAll("level-2", "level-3", "level-4", "level-5");
            });
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