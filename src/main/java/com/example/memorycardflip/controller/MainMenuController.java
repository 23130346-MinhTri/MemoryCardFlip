package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.service.AudioService;
import com.example.memorycardflip.ui.SceneManager;
import javafx.animation.ScaleTransition;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình chính (menu.fxml).
 *
 * <p>UseCase phụ trách:</p>
 * <ul>
 *   <li>[1. Select Difficulty] Chọn cấp độ khó & Bắt đầu game</li>
 *   <li>[UC-05] Xem Bảng xếp hạng</li>
 *   <li>[UC-06] Xem Lịch sử điểm</li>
 *   <li>[UC-09] Xem Leaderboard (high score bar)</li>
 * </ul>
 */
public class MainMenuController implements Initializable {
    private static final PseudoClass SELECTED =
            PseudoClass.getPseudoClass("selected");

    @FXML private Label scoreEasy;
    @FXML private Label scoreMedium;
    @FXML private Label scoreHard;

    @FXML private Button btnEasy;
    @FXML private Button btnMedium;
    @FXML private Button btnHard;
    @FXML private Button btnSound;
    private Difficulty selectedDifficulty = null;
    private final Map<Difficulty, Button> diffButtonMap = new EnumMap<>(Difficulty.class);
    private boolean soundEnabled = true;
    public MainMenuController() {
    }
    // ── Lifecycle ─────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diffButtonMap.put(Difficulty.EASY,   btnEasy);
        diffButtonMap.put(Difficulty.MEDIUM, btnMedium);
        diffButtonMap.put(Difficulty.HARD,   btnHard);

        setupUC01DifficultyButtons();
        AudioService.getInstance().playBGM("/assets/sounds/game.mp3");
        loadHighScores(); // ← thêm dòng này
    }

    private void loadHighScores() {
        try {
            ScoreManager sm = ScoreManager.getInstance();
            if (scoreEasy != null) scoreEasy.setText(String.valueOf(sm.getBestScore(Difficulty.EASY)));
            if (scoreMedium != null) scoreMedium.setText(String.valueOf(sm.getBestScore(Difficulty.MEDIUM)));
            if (scoreHard != null) scoreHard.setText(String.valueOf(sm.getBestScore(Difficulty.HARD)));
        } catch (Exception e) {
            System.err.println("Không thể load high score: " + e.getMessage());
        }
    }
    // ══════════════════════════════════════════════════════════
    // 1. Select Difficulty — Chọn cấp độ khó & Bắt đầu game
    // ══════════════════════════════════════════════════════════

    /**
     * [1.1.1 - 1.1.2] Thiết lập sự kiện cho 3 nút chọn độ khó.
     *
     * <p>Bước 1.1.1: Hệ thống load menu.fxml và hiển thị Main Menu</p>
     * <p>Bước 1.1.2: Người chơi nhìn thấy ba nút (btnEasy, btnMedium, btnHard)
     *              và click vào một nút để chọn độ khó.</p>
     * <p>Mặc định chọn EASY khi mở menu.</p>
     */
    private void setupUC01DifficultyButtons() {
        // Gán sự kiện click cho từng nút
        btnEasy.setOnAction(e   -> onSelectDifficultyEasy());
        btnMedium.setOnAction(e -> onSelectDifficultyMedium());
        btnHard.setOnAction(e   -> onSelectDifficultyHard());
        // Thêm tooltip
        Tooltip easyTooltip = new Tooltip("4×4 lưới · 8 cặp thẻ · 60 giây");
        Tooltip mediumTooltip = new Tooltip("6×6 lưới · 18 cặp thẻ · 90 giây");
        Tooltip hardTooltip = new Tooltip("8×8 lưới · 32 cặp thẻ · 120 giây");

        Tooltip.install(btnEasy, easyTooltip);
        Tooltip.install(btnMedium, mediumTooltip);
        Tooltip.install(btnHard, hardTooltip);

        // Mặc định chọn EASY (optional)
        handleSelectDifficulty(Difficulty.EASY);
    }
    /**
     * [1.1.3 - 1.1.4] Xử lý chọn một độ khó — cập nhật UI selected state.
     *
     * <p>Bước 1.1.3: Hệ thống nhận sự kiện onSelectDifficulty*() từ FXML binding.
     *              MainMenuController.handleSelectDifficulty(Difficulty) được gọi.</p>
     * <p>Bước 1.1.4: Nút được chọn được highlight (đổi màu, scale animation).
     *              Trạng thái selectedDifficulty trong controller được cập nhật.</p>
     *
     * <p>Precondition:  Màn hình menu đang hiển thị.</p>
     * <p>Postcondition: selectedDifficulty được set, nút tương ứng highlight.</p>
     *
     * @param difficulty độ khó người dùng chọn (EASY, MEDIUM, HARD)
     */
    private void handleSelectDifficulty(Difficulty difficulty) {
        selectedDifficulty = difficulty;

        // Cập nhật pseudo-class selected cho tất cả nút
        diffButtonMap.forEach((diff, btn) -> {
            boolean isSelected = diff == difficulty;
            btn.pseudoClassStateChanged(SELECTED, isSelected);
            renderButtonScaleAnimation(btn, isSelected);
        });
    }

    /**
     * Hỗ trợ [1.1.4]: Tạo scale animation cho nút được chọn / bỏ chọn.
     *
     * <p>Khi nút được chọn: scale up 1.04x. Khi bỏ chọn: scale down về 1.0x.</p>
     *
     * @param btn nút cần animate
     * @param isSelected true nếu nút được chọn, false nếu bỏ chọn
     */
    private void renderButtonScaleAnimation(Button btn, boolean isSelected) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setToX(isSelected ? 1.04 : 1.0);
        st.setToY(isSelected ? 1.04 : 1.0);
        st.playFromStart();
    }

    /**
     * [1.1.5 - 1.1.7] Bắt đầu game — chuyển sang GameScene với độ khó đã chọn.
     *
     * <p>Bước 1.1.5: Người chơi click lại vào cùng nút đó (hoặc nhấn Start).</p>
     * <p>Bước 1.1.6: Hệ thống gọi onStartGame().</p>
     * <p>Bước 1.1.7: MainMenuController gọi SceneManager.showGame(difficulty).
     *              SceneManager load game.fxml và tạo GameController mới.</p>
     *
     * <p>Precondition:  selectedDifficulty != null.</p>
     * <p>Postcondition: GameScene được load, GameState được khởi tạo.</p>
     */
    @FXML
    public void onStartGame() {
        if (selectedDifficulty == null) {
            handleSelectDifficulty(Difficulty.EASY);
        }
        SceneManager.getInstance().showGame(selectedDifficulty);
    }

    /**
     * [1.1.2] Click nút Easy (4×4 · 8 cặp · 60s).
     * <p>Lần 1 → chọn Easy (highlight).
     * Lần 2 (nút đã được chọn rồi) → gọi onStartGame() để bắt đầu game.</p>
     *
     * <p>Phương thức hỗ trợ:
     * - handleSelectDifficulty(Difficulty.EASY): cập nhật trạng thái highlight
     * - onStartGame(): chuyển sang GameScene</p>
     */
    @FXML
    public void onSelectDifficultyEasy() {
        if (selectedDifficulty == Difficulty.EASY) {
            // Nút Easy đã được chọn → click lần 2 → start game
            onStartGame();
        } else {
            // Click lần 1 → chỉ chọn Easy, highlight nút
            handleSelectDifficulty(Difficulty.EASY);
        }
    }

    /**
     * [1.1.2] Click nút Medium (6×6 · 18 cặp · 90s).
     * <p>Lần 1 → chọn Medium (highlight).
     * Lần 2 (nút đã được chọn rồi) → gọi onStartGame() để bắt đầu game.</p>
     *
     * <p>Phương thức hỗ trợ:
     * - handleSelectDifficulty(Difficulty.MEDIUM): cập nhật trạng thái highlight
     * - onStartGame(): chuyển sang GameScene</p>
     */
    @FXML
    public void onSelectDifficultyMedium() {
        if (selectedDifficulty == Difficulty.MEDIUM) {
            // Nút Medium đã được chọn → click lần 2 → start game
            onStartGame();
        } else {
            // Click lần 1 → chỉ chọn Medium, highlight nút
            handleSelectDifficulty(Difficulty.MEDIUM);
        }
    }

    /**
     * [1.1.2] Click nút Hard (8×8 · 32 cặp · 120s).
     * <p>Lần 1 → chọn Hard (highlight).
     * Lần 2 (nút đã được chọn rồi) → gọi onStartGame() để bắt đầu game.</p>
     *
     * <p>Phương thức hỗ trợ:
     * - handleSelectDifficulty(Difficulty.HARD): cập nhật trạng thái highlight
     * - onStartGame(): chuyển sang GameScene</p>
     */
    @FXML
    public void onSelectDifficultyHard() {
        if (selectedDifficulty == Difficulty.HARD) {
            // Nút Hard đã được chọn → click lần 2 → start game
            onStartGame();
        } else {
            // Click lần 1 → chỉ chọn Hard, highlight nút
            handleSelectDifficulty(Difficulty.HARD);
        }
    }
    // ── Event Handlers ────────────────────────────────────────
    /** Toggle âm thanh on/off */
    @FXML
    public void onSoundToggle() {
        soundEnabled = !soundEnabled;
        AudioService.getInstance().setEnabled(soundEnabled);
        btnSound.setText(soundEnabled ? "🔊  Âm thanh" : "🔇  Tắt tiếng");
        // TODO: AudioService.getInstance().setEnabled(soundEnabled);
    }
    /** Nút Về game — hiển thị dialog thông tin */
    @FXML
    public void onAbout() {
        // TODO: SceneManager.getInstance().showAboutDialog();
    }
    /** Nút Lịch sử — hiển thị bảng điểm */
    /**
     * [UC-06] Mở màn hình Lịch sử điểm.
     *
     * <p>Use Case này cho phép người chơi xem lại toàn bộ lịch sử điểm đã lưu.</p>
     * <p>Postcondition: ScoreHistoryScene được hiển thị.</p>
     */
    @FXML
    public void onHistory() {
        SceneManager.getInstance().showScoreHistory();   // ← Đổi thành showScoreHistory()
    }

    /**
     * [UC-05] Mở màn hình Bảng xếp hạng.
     *
     * <p>Use Case này cho phép người chơi xem danh sách điểm cao nhất theo độ khó.</p>
     * <p>Postcondition: LeaderboardScene được hiển thị.</p>
     */
    @FXML
    public void onLeaderboard() {
        SceneManager.getInstance().showLeaderboard();   // Mở BẢNG XẾP HẠNG
    }

}