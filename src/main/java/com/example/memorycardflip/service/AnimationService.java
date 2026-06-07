package com.example.memorycardflip.service;

import com.example.memorycardflip.ui.CardFlipView;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.Random;

public final class AnimationService {

    private AnimationService() {
        // utility class
    }

    public static void playMatchEffect(CardFlipView view) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), view);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        Glow glow = new Glow(0.7);
        view.setEffect(glow);

        scale.setOnFinished(e -> view.setEffect(null));
        scale.play();
    }

    public static void playShakeAnimation(CardFlipView view) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), view);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> view.setTranslateX(0));
        shake.play();
    }

    /**
     * [UC2 v2.0] Hiển thị hiệu ứng hạt lấp lánh (sparkle particles) khi ghép cặp thành công.
     * Nếu comboCount >= 2, hiệu ứng sẽ nhiều hạt hơn và có màu sắc sặc sỡ khác biệt.
     */
    public static void spawnSparkleParticles(Pane parentPane, double x, double y, int comboCount) {
        if (parentPane == null) return;

        Random rand = new Random();
        int count = comboCount >= 2 ? 30 : 15; // Tăng gấp đôi số hạt nếu combo >= 2

        for (int i = 0; i < count; i++) {
            double radius = rand.nextDouble() * 3 + 2; // Bán kính từ 2px đến 5px
            Circle p = new Circle(radius);

            // Quyết định màu sắc dựa trên combo
            if (comboCount >= 2) {
                // Combo lớn: Cam lửa, hồng neon, tím sáng, vàng tươi
                double cRand = rand.nextDouble();
                if (cRand < 0.3) {
                    p.setFill(Color.web("#FF4500")); // Orange Red
                } else if (cRand < 0.6) {
                    p.setFill(Color.web("#FF1493")); // Deep Pink
                } else if (cRand < 0.8) {
                    p.setFill(Color.web("#9400D3")); // Dark Violet
                } else {
                    p.setFill(Color.web("#FFD700")); // Gold
                }
            } else {
                // Match thường: Vàng gold, vàng sáng, xanh da trời nhạt, trắng
                double cRand = rand.nextDouble();
                if (cRand < 0.4) {
                    p.setFill(Color.web("#FFD700")); // Gold
                } else if (cRand < 0.7) {
                    p.setFill(Color.web("#FFFFE0")); // Light Yellow
                } else if (cRand < 0.9) {
                    p.setFill(Color.web("#00BFFF")); // Deep Sky Blue
                } else {
                    p.setFill(Color.WHITE);
                }
            }

            p.setLayoutX(x);
            p.setLayoutY(y);
            parentPane.getChildren().add(p);

            // Hướng bay ngẫu nhiên (góc ngẫu nhiên 360 độ và khoảng cách ngẫu nhiên)
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = rand.nextDouble() * (comboCount >= 2 ? 80 : 50) + 20;
            double targetX = Math.cos(angle) * distance;
            double targetY = Math.sin(angle) * distance;

            // Hiệu ứng bay
            TranslateTransition translate = new TranslateTransition(Duration.millis(rand.nextInt(300) + 500), p);
            translate.setByX(targetX);
            translate.setByY(targetY);

            // Hiệu ứng biến mất dần
            FadeTransition fade = new FadeTransition(Duration.millis(translate.getDuration().toMillis()), p);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            // Chạy song song
            ParallelTransition parallel = new ParallelTransition(translate, fade);
            parallel.setOnFinished(evt -> parentPane.getChildren().remove(p));
            parallel.play();
        }
    }
}
