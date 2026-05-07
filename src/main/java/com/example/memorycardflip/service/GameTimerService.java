package com.example.memorycardflip.service;

import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class GameTimerService {

    public interface Listener {
        void onTick();
        void onTimeUp();
    }

    private GameState gameState;
    private final Listener listener;
    private Timeline timer;

    public GameTimerService(GameState gameState, Listener listener) {
        this.gameState = gameState;
        this.listener = listener;
    }
    public void updateGameState(GameState newState) {
        this.gameState = newState;
    }
    public void start() {
        if (gameState == null || gameState.getStatus() != GameStatus.PLAYING) {
            return;
        }

        if (timer == null) {
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
            timer.setCycleCount(Timeline.INDEFINITE);
        }

        if (timer.getStatus() != Timeline.Status.RUNNING) {
            timer.play();
        }
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void dispose() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void tick() {
        if (gameState == null || gameState.getStatus() != GameStatus.PLAYING) {
            return;
        }

        gameState.decrementTime();
        listener.onTick();

        if (gameState.isTimeUp()) {
            stop(); // ✅ FIX QUAN TRỌNG
            listener.onTimeUp();
        }
    }
}
