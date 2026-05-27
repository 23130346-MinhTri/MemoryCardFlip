package com.example.memorycardflip.ui;

import com.example.memorycardflip.controller.StreakNotificationController;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class NotificationManager {
    public static void showStreakNotification(Stage owner, String title, String message, String iconText) {
        try {
            FXMLLoader loader = new FXMLLoader(NotificationManager.class.getResource("/fxml/components/streak_notification.fxml"));
            Parent root = loader.load();
            StreakNotificationController controller = loader.getController();
            controller.setTitle(title);
            controller.setMessage(message);
            controller.setIconText(iconText == null ? "★" : iconText);

            Stage stage = new Stage(StageStyle.TRANSPARENT);
            if (owner != null) stage.initOwner(owner);
            Scene scene = new Scene(root);
            scene.setFill(null);
            // Add notification stylesheet
            try {
                String css = NotificationManager.class.getResource("/styles/notification.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ignored) {
            }
            stage.setScene(scene);

            // Position at bottom-right of primary screen with small margin
            Screen screen = Screen.getPrimary();
            stage.setOnShown(e -> {
                double x = screen.getVisualBounds().getMaxX() - stage.getWidth() - 20;
                double y = screen.getVisualBounds().getMaxY() - stage.getHeight() - 80;
                stage.setX(x);
                stage.setY(y);
            });

            stage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            PauseTransition wait = new PauseTransition(Duration.seconds(3.5));
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            wait.setOnFinished(evt -> {
                fadeOut.play();
                fadeOut.setOnFinished(ev -> stage.close());
            });
            wait.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
