package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.ScoreRecord;
import com.example.memorycardflip.model.ScoreStorage;
import com.example.memorycardflip.model.JsonScoreStorage;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service quản lý điểm số, dùng Singleton.
 * Nằm trong package service (cần tạo mới nếu chưa có)
 */
public class ScoreManager {

    private static ScoreManager instance;
    private final ScoreStorage storage;
    private List<ScoreRecord> cache;
    private Consumer<List<ScoreRecord>> onScoreChanged;

    private ScoreManager() {
        // Có thể đổi sang SQLiteScoreStorage nếu muốn
        this.storage = new JsonScoreStorage();
        loadScores();
    }

    public static ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();
        }
        return instance;
    }

    /**
     * Tải điểm từ storage vào cache
     */
    private void loadScores() {
        try {
            cache = storage.load();
        } catch (Exception e) {
            System.err.println("Không thể tải điểm: " + e.getMessage());
            cache = new java.util.ArrayList<>();
        }
    }

    /**
     * Lưu điểm hiện tại
     */
    /**
     * Lưu điểm cho tất cả các ván (cả thắng và thua)
     */
    /**
     * Lưu điểm hiện tại
     */
    public void saveScore(GameState gameState, String playerName) {
        if (gameState == null ) {
            return; // Chỉ lưu khi thắng
        }

        ScoreRecord record = ScoreRecord.fromGameState(playerName, gameState);
        cache.add(record);

        // Sắp xếp theo điểm giảm dần
        cache.sort((a, b) -> Integer.compare(b.score(), a.score()));

        try {
            storage.save(cache);
            if (onScoreChanged != null) {
                onScoreChanged.accept(cache);
            }
        } catch (Exception e) {
            System.err.println("Không thể lưu điểm: " + e.getMessage());
        }
    }


    /**
     * Lấy top N điểm cao nhất (tất cả độ khó)
     */
    public List<ScoreRecord> getTopScores(int limit) {
        return cache.stream()
                .limit(limit)
                .toList();
    }

    /**
     * Lấy top N điểm theo độ khó
     */
    public List<ScoreRecord> getTopScores(Difficulty difficulty, int limit) {
        return cache.stream()
                .filter(r -> r.difficulty() == difficulty)
                .limit(limit)
                .toList();
    }

    /**
     * Lấy điểm cao nhất theo độ khó
     */
    public Optional<ScoreRecord> getHighScore(Difficulty difficulty) {
        return cache.stream()
                .filter(r -> r.difficulty() == difficulty)
                .findFirst();
    }

    /**
     * Lấy tất cả điểm theo độ khó
     */
    public List<ScoreRecord> getScoresByDifficulty(Difficulty difficulty) {
        return cache.stream()
                .filter(r -> r.difficulty() == difficulty)
                .toList();
    }

    /**
     * Kiểm tra xem có phải điểm cao nhất mới không
     */
    public boolean isNewHighScore(Difficulty difficulty, int score) {
        Optional<ScoreRecord> highScore = getHighScore(difficulty);
        return highScore.isEmpty() || score > highScore.get().score();
    }

    /**
     * Xóa tất cả điểm
     */
    public void clearAllScores() {
        cache.clear();
        try {
            storage.clear();
            if (onScoreChanged != null) {
                onScoreChanged.accept(cache);
            }
        } catch (Exception e) {
            System.err.println("Không thể xóa điểm: " + e.getMessage());
        }
    }

    /**
     * Đăng ký callback khi điểm thay đổi
     */
    public void setOnScoreChanged(Consumer<List<ScoreRecord>> callback) {
        this.onScoreChanged = callback;
    }

    /**
     * Lấy storage hiện tại
     */
    public ScoreStorage getStorage() {
        return storage;
    }
    /**
     * Lấy TẤT CẢ các bản ghi điểm (không giới hạn, không lọc theo điểm cao)
     * Dùng cho màn hình Lịch sử
     */
    public List<ScoreRecord> getAllScores() {
        return new java.util.ArrayList<>(cache);
    }
}