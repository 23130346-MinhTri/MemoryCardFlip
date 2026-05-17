package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service quản lý điểm số, dùng Singleton.
 *
 * UC-10: Tính điểm (Calculate score) - lưu và truy xuất điểm
 * UC-05: Xem bảng xếp hạng
 * UC-06: Xóa lịch sử điểm
 */
public class ScoreManager {

    private static ScoreManager instance;

    private final ScoreStorage storage;

    private List<ScoreRecord> cache;

    private Consumer<List<ScoreRecord>> onScoreChanged;

    // ── Constructor ───────────────────────────────────────────
    private ScoreManager() {

        // Có thể đổi sang SQLiteScoreStorage nếu muốn
        this.storage = new JsonScoreStorage();

        loadScores();
    }

    // ── Singleton ─────────────────────────────────────────────
    public static synchronized ScoreManager getInstance() {

        if (instance == null) {
            instance = new ScoreManager();
        }

        return instance;
    }

    // ── Load score ────────────────────────────────────────────

    /**
     * Tải danh sách điểm từ storage vào cache.
     */
    private void loadScores() {

        try {

            cache = storage.load();

        } catch (Exception e) {

            System.err.println("Không thể tải điểm: " + e.getMessage());

            cache = new ArrayList<>();
        }
    }

    // UC-10: Lưu điểm
    /**
     * [UC-10]
     * Lưu điểm ván chơi khi người chơi thắng.
     *
     * Bước 10.2.5:
     * Kiểm tra GameState != null
     *
     * Bước 10.1.3 - 10.1.9:
     * Điểm đã được tính trong:
     * GameState.calculateScore()
     */
    public void saveScore(GameState gameState, String playerName) {

        // [UC-10.2.5]
        // Kiểm tra GameState null
        if (gameState == null) {

            System.err.println("GameState is null, cannot calculate score");

            return;
        }

        // Chỉ lưu khi thắng
        if (gameState.getStatus() != GameStatus.WON) {

            System.out.println(
                    "Không lưu — trạng thái: "
                            + gameState.getStatus()
            );

            return;
        }

        try {

            // Tạo ScoreRecord từ GameState
            // Điểm đã được tính theo UC-10
            ScoreRecord record =
                    ScoreRecord.fromGameState(playerName, gameState);

            cache.add(record);

            // Sắp xếp điểm giảm dần
            // Điểm cao nhất lên đầu
            cache.sort(
                    (a, b) ->
                            Integer.compare(
                                    b.getScore(),
                                    a.getScore()
                            )
            );

            // Lưu xuống storage
            storage.save(cache);

            System.out.println("Đã lưu: " + record);

            // Callback khi score thay đổi
            if (onScoreChanged != null) {
                onScoreChanged.accept(cache);
            }

        } catch (Exception e) {

            System.err.println(
                    "Không thể lưu điểm: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
    // UC-10: Truy xuất điểm

    /**
     * [UC-10]
     * Lấy điểm cao nhất theo độ khó.
     *
     * Được dùng trong ResultController
     * để hiển thị kỷ lục.
     */
    public int getBestScore(Difficulty difficulty) {

        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .mapToInt(ScoreRecord::getScore)
                .max()
                .orElse(0);
    }

    /**
     * [UC-10]
     * Kiểm tra xem có phải điểm cao nhất mới không.
     */
    public boolean isNewHighScore(
            Difficulty difficulty,
            int score
    ) {

        return score > getBestScore(difficulty);
    }
    // UC-05: Leaderboard / Ranking


    /**
     * [UC-05]
     * Lấy top N điểm cao nhất toàn cục.
     */
    public List<ScoreRecord> getTopScores(int limit) {

        return cache.stream()
                .limit(limit)
                .toList();
    }

    /**
     * [UC-05]
     * Lấy top N điểm theo độ khó.
     */
    public List<ScoreRecord> getTopScores(
            Difficulty difficulty,
            int limit
    ) {

        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .limit(limit)
                .toList();
    }

    /**
     * [UC-05]
     * Lấy điểm cao nhất theo độ khó.
     */
    public Optional<ScoreRecord> getHighScore(
            Difficulty difficulty
    ) {

        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .findFirst();
    }

    /**
     * [UC-05]
     * Lấy toàn bộ điểm theo độ khó.
     */
    public List<ScoreRecord> getScoresByDifficulty(
            Difficulty difficulty
    ) {

        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .toList();
    }

    /**
     * [UC-05]
     * Lấy toàn bộ các bản ghi điểm
     * để hiển thị leaderboard hoặc lịch sử.
     */
    public List<ScoreRecord> getAllScores() {

        return new ArrayList<>(cache);
    }

    // ═══════════════════════════════════════════════════════════
    // UC-06: Xóa lịch sử điểm
    // ═══════════════════════════════════════════════════════════

    /**
     * [UC-06]
     * Xóa toàn bộ lịch sử điểm.
     */
    public void clearAllScores() {

        cache.clear();

        try {

            storage.clear();

            if (onScoreChanged != null) {
                onScoreChanged.accept(cache);
            }

        } catch (Exception e) {

            System.err.println(
                    "Không thể xóa điểm: "
                            + e.getMessage()
            );
        }
    }

    // ── Callback ──────────────────────────────────────────────

    /**
     * Đăng ký callback khi danh sách điểm thay đổi.
     */
    public void setOnScoreChanged(
            Consumer<List<ScoreRecord>> callback
    ) {

        this.onScoreChanged = callback;
    }

    // ── Getter ────────────────────────────────────────────────

    /**
     * Lấy storage hiện tại.
     */
    public ScoreStorage getStorage() {

        return storage;
    }
}