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

    public void playEffect(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                // Try with leading slash
                url = getClass().getResource("/" + resourcePath);
            }
            if (url == null) {
                // If caller passed a source path like "src/main/resources/...", convert to classpath
                int idx = resourcePath.indexOf("src/main/resources");
                if (idx >= 0) {
                    String rp = resourcePath.substring(idx + "src/main/resources".length());
                    if (!rp.startsWith("/")) rp = "/" + rp;
                    url = getClass().getResource(rp);
                }
            }
            if (url == null) {
                // Fall back to filesystem path
                java.io.File f = new java.io.File(resourcePath);
                if (f.exists()) url = f.toURI().toURL();
            }
            if (url == null) {
                System.err.println("Không tìm thấy file âm thanh: " + resourcePath);
                return;
            }

            System.out.println("AudioService.playEffect -> resolved URL: " + url.toExternalForm());
            try {
                MediaPlayer effectPlayer = new MediaPlayer(new Media(url.toExternalForm()));
                effectPlayer.setVolume(enabled ? volume : 0);
                effectPlayer.setOnError(() ->
                        System.err.println("Lỗi media effect: " + effectPlayer.getError()));
                effectPlayer.setOnEndOfMedia(() -> {
                    effectPlayer.stop();
                    effectPlayer.dispose();
                });
                effectPlayer.play();
                return;
            } catch (Exception me) {
                System.err.println("MediaPlayer failed: " + me.getMessage());
                // fallback to AudioClip for short effects
                try {
                    javafx.scene.media.AudioClip clip = new javafx.scene.media.AudioClip(url.toExternalForm());
                    clip.setVolume(enabled ? volume : 0);
                    clip.play();
                    return;
                } catch (Exception ac) {
                    System.err.println("AudioClip fallback failed: " + ac.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể phát hiệu ứng âm thanh: " + e.getMessage());
        }
    }

    // ── BGM ───────────────────────────────────────────────────

    public void playBGM(String resourcePath) {
        stopBGM();
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) url = getClass().getResource("/" + resourcePath);
            if (url == null) {
                int idx = resourcePath.indexOf("src/main/resources");
                if (idx >= 0) {
                    String rp = resourcePath.substring(idx + "src/main/resources".length());
                    if (!rp.startsWith("/")) rp = "/" + rp;
                    url = getClass().getResource(rp);
                }
            }
            if (url == null) {
                java.io.File f = new java.io.File(resourcePath);
                if (f.exists()) url = f.toURI().toURL();
            }
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