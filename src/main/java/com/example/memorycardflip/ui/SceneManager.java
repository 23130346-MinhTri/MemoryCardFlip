package com.example.memorycardflip.ui;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton quản lý toàn bộ việc chuyển đổi Scene trong ứng dụng.
 *
 * <p>UseCase phụ trách:</p>
 * <ul>
 *   <li>[1. Select Difficulty] Chuyển sang GameScene sau khi chọn độ khó</li>
 *   <li>[UC-03] Kết thúc game → chuyển sang ResultScene</li>
 *   <li>[UC-10] Replay → reset GameScene</li>
 *   <li>[UC-09] Quay về MainMenu</li>
 * </ul>
 *
 * <p>Cách dùng:</p>
 * <pre>
 *   // Khởi tạo 1 lần duy nhất trong Main.java
 *   SceneManager.getInstance().init(primaryStage);
 *
 *   // Từ bất kỳ Controller nào
 *   SceneManager.getInstance().showGame(Difficulty.EASY);
 * </pre>
 */
public class SceneManager {

    private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());

    // ── Đường dẫn FXML ───────────────────────────────────────
    private static final String FXML_MENU   = "/fxml/menu.fxml";
    private static final String FXML_GAME   = "/fxml/game.fxml"; // [UC4] Màn chơi được load lại khi Play again
    private static final String FXML_RESULT = "/fxml/result.fxml"; // [UC3] FXML màn hình View result

    // ── Kích thước cửa sổ ────────────────────────────────────
    private static final double WIDTH  = 540;
    private static final double HEIGHT = 700;

    // ── Singleton instance ────────────────────────────────────
    private static SceneManager instance;

    // ── State ─────────────────────────────────────────────────
    private Stage primaryStage;
    // [UC4] Giữ GameState hiện tại để biết difficulty khi người chơi bấm Play again.
    private GameState currentGameState;

    // ── Private constructor ───────────────────────────────────
    private SceneManager() {}

    /**
     * Lấy instance duy nhất — thread-safe double-checked locking.
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            synchronized (SceneManager.class) {
                if (instance == null) {
                    instance = new SceneManager();
                }
            }
        }
        return instance;
    }

// ── Khởi tạo ─────────────────────────────────────────────

/**
 * Khởi tạo với Stage chính. Gọi 1 lần trong {@code Main.start()}.
 *
 * @param stage Stage chính của ứng dụng JavaFX
 */
public void init(Stage stage) {
    this.primaryStage = stage;
    primaryStage.setTitle("Memory Card Flip");
    primaryStage.setResizable(false);
    primaryStage.setWidth(WIDTH);
    primaryStage.setHeight(HEIGHT);
}

    // ══════════════════════════════════════════════════════════
    // 1. Select Difficulty — Chuyển sang GameScene
    // ══════════════════════════════════════════════════════════

    /**
     * [1.1.7 - 1.1.9] Chuyển sang màn hình chơi game với độ khó đã chọn.
     *
     * <p>Bước 1.1.7: MainMenuController gọi SceneManager.showGame(difficulty).
     *              SceneManager load game.fxml và tạo GameController mới.</p>
     * <p>Bước 1.1.8: GameController.init(GameState) được gọi:
     *              tạo GameState với Difficulty đã chọn, khởi tạo card grid,
     *              reset timer và score.</p>
     * <p>Bước 1.1.9: GameScene được hiển thị. Timer bắt đầu đếm ngược.
     *              AudioService phát BGM.</p>
     *
     * <p>Precondition:  difficulty != null, primaryStage đã init.</p>
     * <p>Postcondition: GameScene hiển thị, GameState mới được tạo.</p>
     *
     * @param difficulty độ khó người dùng chọn ở bước 1.1.2
     */
    public void showGame(Difficulty difficulty) {
        currentGameState = new GameState(difficulty);
        switchScene(FXML_GAME);
    }

    // ══════════════════════════════════════════════════════════
    // UC-03 — Kết thúc game → ResultScene
    // ══════════════════════════════════════════════════════════

    /**
     * [UC-03] Chuyển sang màn hình kết quả (WIN hoặc LOSE).
     * GameState giữ nguyên để ResultController đọc thống kê.
     *
     * <p>Precondition:  currentGameState != null, game đã kết thúc.</p>
     * <p>Postcondition: ResultScene hiển thị với đúng thống kê.</p>
     */
    public void showResult() {
        // [UC3] Giữ nguyên currentGameState để ResultController đọc và hiển thị thống kê cuối ván.
        switchScene(FXML_RESULT);
    }

    // ══════════════════════════════════════════════════════════
    // UC4 — Play again / Replay
    // ══════════════════════════════════════════════════════════

    /**
     * [UC4 - Play again] Chơi lại cùng độ khó — tạo GameState mới, load lại GameScene.
     *
     * <p>Precondition:  currentGameState != null.</p>
     * <p>Postcondition: GameScene mới, GameState reset về 0.</p>
     */
    public void replayGame() {
        if (currentGameState == null) {
            // [UC4] Fallback an toàn nếu người chơi replay khi chưa có ván trước đó.
            LOGGER.warning("[UC-10] currentGameState null → fallback EASY");
            showGame(Difficulty.EASY);
            return;
        }
        // [UC4] Lấy lại difficulty của ván vừa xong để tạo ván mới cùng cấp độ.
        showGame(currentGameState.getDifficulty());
    }
// ══════════════════════════════════════════════════════════
    // Quay về Menu
    // ══════════════════════════════════════════════════════════

    /**
     * Quay về MainMenu, xoá GameState hiện tại.
     *
     * <p>Postcondition: MainMenuScene hiển thị, currentGameState = null.</p>
     */
    public void showMenu() {
        currentGameState = null;
        switchScene(FXML_MENU);
    }

    // ── Getter ────────────────────────────────────────────────

    /**
     * [UC3 - View result] Lấy GameState ván vừa kết thúc.
     * ResultController dùng dữ liệu này để hiển thị thống kê trên màn kết quả.
     */
    public GameState getCurrentGameState() {
        return currentGameState;
    }

    // ── Internal ─────────────────────────────────────────────

    /**
     * Load FXML và set Scene lên primaryStage.
     *
     * @param fxmlPath đường dẫn FXML trong classpath
     */
    private void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );
            Parent root  = loader.load();
            Scene  scene = new Scene(root, WIDTH, HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.show();

            LOGGER.info("Switched to: " + fxmlPath);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML: " + fxmlPath, e);
            throw new RuntimeException("Cannot load scene: " + fxmlPath, e);
        }
    }
    /**
     * [UC-06] Hiển thị màn hình Lịch sử điểm (tất cả các ván đã chơi).
     *
     * <p>Postcondition: màn hình score_history được load lên primaryStage.</p>
     */
    public void showScoreHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/score_history.fxml"));
            Parent root = loader.load();
            Stage stage = getPrimaryStage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Lịch sử điểm - Memory Card Flip");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * [UC-05] Hiển thị màn hình Bảng xếp hạng (top điểm cao).
     *
     * <p>Postcondition: màn hình leaderboard được load lên primaryStage.</p>
     */
    public void showLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/leaderboard.fxml"));
            Parent root = loader.load();
            Stage stage = getPrimaryStage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Bảng xếp hạng - Memory Card Flip");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stage getPrimaryStage() {
        // Bạn cần lưu primaryStage khi khởi động app
        // Hoặc dùng: (Stage) Stage.getWindows().get(0)
        return (Stage) javafx.stage.Stage.getWindows().get(0);
    }


}
