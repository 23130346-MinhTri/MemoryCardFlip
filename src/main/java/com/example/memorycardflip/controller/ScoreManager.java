package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.*;

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

    public static synchronized ScoreManager getInstance() {
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
     * [UC-10] Lưu điểm ván chơi khi người chơi thắng.
     *
     * <p>Use Case này lưu trữ điểm cao nhất vào storage để hiển thị leaderboard và lịch sử.</p>
     */
    public void saveScore(GameState gameState, String playerName) {
        if (gameState == null) return;

        // Chỉ lưu khi thắng
        if (gameState.getStatus() != GameStatus.WON) {
            System.out.println("Không lưu — trạng thái: " + gameState.getStatus());
            return;
        }

        try {
            ScoreRecord record = ScoreRecord.fromGameState(playerName, gameState);
            cache.add(record);
            cache.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
            storage.save(cache); // ← lưu xuống file
            System.out.println("Đã lưu: " + record);

            if (onScoreChanged != null) onScoreChanged.accept(cache);

        } catch (Exception e) {
            System.err.println("Không thể lưu điểm: " + e.getMessage());
            e.printStackTrace(); // ← xem stack trace đầy đủ
        }
    }


    /**
     * [UC-05] Lấy top N điểm cao nhất.
     *
     * <p>Use Case này dùng để hiển thị bảng xếp hạng toàn cục hoặc theo độ khó.</p>
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
                .filter(r -> r.getDifficulty() == difficulty)
                .limit(limit)
                .toList();
    }

    /**
     * Lấy điểm cao nhất theo độ khó
     */
    public Optional<ScoreRecord> getHighScore(Difficulty difficulty) {
        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .findFirst();
    }

    /**
     * Lấy tất cả điểm theo độ khó
     */
    public List<ScoreRecord> getScoresByDifficulty(Difficulty difficulty) {
        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .toList();
    }

    /**
     * Kiểm tra xem có phải điểm cao nhất mới không
     */
    public boolean isNewHighScore(Difficulty difficulty, int score) {
        Optional<ScoreRecord> highScore = getHighScore(difficulty);
        return highScore.isEmpty() || score > highScore.get().getScore();
    }

    /**
     * [UC-06] Xóa toàn bộ lịch sử điểm.
     *
     * <p>Use Case này cho phép người chơi xoá dữ liệu lịch sử đã lưu trên thiết bị.</p>
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
     * [UC-05] Lấy tất cả các bản ghi điểm để hiển thị trên lịch sử hoặc leaderboard.
     */
    public int getBestScore(Difficulty difficulty) {
        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .mapToInt(ScoreRecord::getScore)
                .max()
                .orElse(0);
    }
    public List<ScoreRecord> getAllScores() {
        return new java.util.ArrayList<>(cache);
    }
}