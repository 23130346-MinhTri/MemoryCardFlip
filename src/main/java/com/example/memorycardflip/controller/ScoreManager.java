package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * [UC-07 — View Leaderboard] [UC-13 — Save High Score]
 * Service Singleton quản lý toàn bộ hoạt động lưu trữ và truy xuất điểm số.
 *
 * <p>Singleton được khởi tạo một lần duy nhất khi ứng dụng mở; cache được load
 * từ file JSON ngay tại hàm tạo để mọi truy vấn đều phục vụ từ bộ nhớ (O(1)/O(n)).</p>
 *
 * <p><b>UC-07 Sequence references:</b></p>
 * <ul>
 *   <li>7.1.5  — LeaderboardController gọi getTopScores(difficulty, 50)</li>
 *   <li>7.1.6  — filter by difficulty → sort by score DESC → limit 50 → return topList</li>
 *   <li>7.1.7  — LeaderboardController gọi getHighScore(difficulty) lấy điểm cao nhất</li>
 * </ul>
 *
 * <p><b>UC-13 Sequence references:</b></p>
 * <ul>
 *   <li>13.1.4  — ResultController gọi saveScore(gameState, "Player")</li>
 *   <li>13.1.5  — saveScore() tạo ScoreRecord từ GameState</li>
 *   <li>13.1.6  — cache.add(record) + sort giảm dần</li>
 *   <li>13.1.7  — storage.save(cache) → JsonScoreStorage ghi file JSON</li>
 *   <li>13.1.8  — onScoreChanged callback cập nhật LeaderboardController (nếu đang mở)</li>
 * </ul>
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
    // ════════════════════════════════════════════════════════════════════════
    // [UC-13 — Bước 13.1.4 → 13.1.8] saveScore()
    // ════════════════════════════════════════════════════════════════════════

    /**
     * [UC-13 — 13.1.4 → 13.1.8] Lưu kết quả ván chơi vào cache và file JSON.
     *
     * <p><b>Precondition:</b> gameState != null; gameState.getStatus() là WON
     *   (BR-13.1 mọi ván đều lưu lịch sử; BR-13.3 high score chỉ xét ván thắng).</p>
     * <p><b>Postcondition:</b></p>
     * <ul>
     *   <li>ScoreRecord mới được thêm vào cache (bước 13.1.6).</li>
     *   <li>Cache được sắp xếp theo điểm giảm dần (bước 13.1.6).</li>
     *   <li>storage.save() ghi toàn bộ cache xuống file JSON (bước 13.1.7).</li>
     *   <li>onScoreChanged callback được kích hoạt nếu đã đăng ký (bước 13.1.8).</li>
     * </ul>
     *
     * @param gameState  Trạng thái ván chơi vừa kết thúc
     * @param playerName Tên người chơi (mặc định "Player")
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


    // ════════════════════════════════════════════════════════════════════════
    // [UC-07 — Bước 7.1.5 → 7.1.6] getTopScores()
    // ════════════════════════════════════════════════════════════════════════

    /**
     * [UC-07 — 7.1.5 → 7.1.6] Lấy top N điểm cao nhất trên tất cả độ khó.
     *
     * <p>Sequence step 12-14: filter by difficulty → sort DESC → limit N.</p>
     *
     * @param limit Số bản ghi tối đa trả về
     * @return Danh sách điểm giảm dần, tối đa {@code limit} phần tử
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
     * [UC-07 — 7.1.5 → 7.1.6] Lấy top N điểm theo độ khó.
     *
     * <p><b>Sequence diagram bước 12:</b> filter by difficulty</p>
     * <p><b>Sequence diagram bước 13:</b> sort by score DESC (cache đã được sort khi saveScore)</p>
     * <p><b>Sequence diagram bước 14:</b> limit {@code limit}</p>
     * <p><b>Sequence diagram bước 15:</b> return topList → LeaderboardController</p>
     *
     * @param difficulty Độ khó cần lọc
     * @return Top {@code limit} bản ghi điểm theo {@code difficulty}
     */
    public Optional<ScoreRecord> getHighScore(Difficulty difficulty) {
        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .findFirst();
    }

    // ════════════════════════════════════════════════════════════════════════
    // [UC-07 — Bước 7.1.7] getHighScore()
    // ════════════════════════════════════════════════════════════════════════

    /**
     * [UC-07 — 7.1.7 (bước 19-20)] Lấy bản ghi điểm cao nhất theo độ khó.
     *
     * <p>Cache đã sort DESC nên phần tử đầu tiên sau khi filter là cao nhất.</p>
     *
     * @param difficulty Độ khó cần tra
     * @return Optional chứa bản ghi điểm cao nhất, hoặc empty nếu chưa có dữ liệu
     */
    public List<ScoreRecord> getScoresByDifficulty(Difficulty difficulty) {
        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .toList();
    }

    // ════════════════════════════════════════════════════════════════════════
    // [UC-13 — Bước 13.1.9] isNewHighScore()
    // ════════════════════════════════════════════════════════════════════════

    /**
     * [UC-13 — 13.1.9] Kiểm tra xem điểm có phải kỷ lục mới không.
     *
     * <p>ResultController dùng để quyết định hiển thị "🏆 Kỷ lục mới!" trên màn kết quả.</p>
     * <p>BR-13.3: Chỉ xét kỷ lục cho ván thắng.</p>
     *
     * @param difficulty Độ khó của ván vừa chơi
     * @param score      Điểm của ván vừa chơi
     * @return {@code true} nếu chưa có điểm nào hoặc điểm mới cao hơn kỷ lục hiện tại
     */
    public boolean isNewHighScore(Difficulty difficulty, int score) {
        // [UC3] ResultController dùng để quyết định hiển thị "Kỷ lục mới!" trên màn kết quả.
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
     * [UC-13 — 13.1.8] Đăng ký callback nhận thông báo khi có điểm mới được lưu.
     *
     * <p>LeaderboardController gọi hàm này trong initialize() để tự động làm mới
     * TableView khi saveScore() hoàn thành.</p>
     *
     * @param callback Hàm nhận List&lt;ScoreRecord&gt; cập nhật mới nhất
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
     * [UC-03][UC-13 — 13.1.9] Lấy điểm cao nhất dạng int theo độ khó.
     *
     * <p>ResultController dùng để hiển thị lblNewHighScore khi thắng.</p>
     *
     * @param difficulty Độ khó cần tra
     * @return Điểm cao nhất, hoặc 0 nếu chưa có dữ liệu
     */

    public int getBestScore(Difficulty difficulty) {
        // [UC3] Lấy điểm cao nhất để hiển thị ở màn View result sau khi ván kết thúc.
        return cache.stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .mapToInt(ScoreRecord::getScore)
                .max()
                .orElse(0);
    }
    /**
     * [UC-07][UC-13] Lấy tất cả bản ghi điểm (bản sao để tránh side-effect).
     *
     * @return Bản sao danh sách toàn bộ lịch sử điểm
     */
    public List<ScoreRecord> getAllScores() {
        return new java.util.ArrayList<>(cache);
    }
}
