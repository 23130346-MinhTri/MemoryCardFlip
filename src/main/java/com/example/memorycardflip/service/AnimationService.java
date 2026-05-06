package com.example.memorycardflip.service;

import com.example.memorycardflip.ui.CardFlipView;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.Glow;
import javafx.util.Duration;

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
}
