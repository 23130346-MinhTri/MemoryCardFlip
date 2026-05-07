package com.example.memorycardflip.service;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class AudioService {

    private static AudioService instance;
    private MediaPlayer bgmPlayer;
    private boolean enabled = true;
    private double volume = 0.4;

    private AudioService() {}

    public static AudioService getInstance() {
        if (instance == null) instance = new AudioService();
        return instance;
    }

    // ── BGM ───────────────────────────────────────────────────

    public void playBGM(String resourcePath) {
        stopBGM();
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("Không tìm thấy file âm thanh: " + resourcePath);
                return;
            }
            Media media = new Media(url.toExternalForm());
            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setVolume(enabled ? volume : 0);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE); // lặp vô tận
            bgmPlayer.setOnError(() ->
                    System.err.println("Lỗi media: " + bgmPlayer.getError()));
            bgmPlayer.play();
        } catch (Exception e) {
            System.err.println("Không thể phát BGM: " + e.getMessage());
        }
    }

    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
    }

    public void pauseBGM() {
        if (bgmPlayer != null) bgmPlayer.pause();
    }

    public void resumeBGM() {
        if (bgmPlayer != null && enabled) bgmPlayer.play();
    }

    // ── Volume & Toggle ───────────────────────────────────────

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(enabled ? volume : 0);
        }
    }

    public boolean isEnabled() { return enabled; }

    public void setVolume(double volume) {
        this.volume = Math.max(0, Math.min(1, volume));
        if (bgmPlayer != null && enabled) {
            bgmPlayer.setVolume(this.volume);
        }
    }

    public double getVolume() { return volume; }
}