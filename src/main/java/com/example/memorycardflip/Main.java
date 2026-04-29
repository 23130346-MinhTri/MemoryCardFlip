package com.example.memorycardflip;

import com.example.memorycardflip.ui.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point của ứng dụng Memory Card Flip.
 * Khởi tạo SceneManager và load màn hình chính.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Khởi tạo SceneManager 1 lần duyF nhất
        SceneManager.getInstance().init(primaryStage);

        // Load màn hình chính
        SceneManager.getInstance().showMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}