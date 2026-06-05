package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.service.AudioService;
import com.example.memorycardflip.ui.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
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
     * FIX: Thêm kiểm tra null cho các button và tooltip
     */
    private void setupUC01DifficultyButtons() {
        // Gán sự kiện click cho từng nút (kiểm tra null)
        if (btnEasy != null) {
            btnEasy.setOnAction(e -> onSelectDifficultyEasy());
            Tooltip.install(btnEasy, new Tooltip("4×4 lưới · 8 cặp thẻ · 60 giây"));
        }

        if (btnMedium != null) {
            btnMedium.setOnAction(e -> onSelectDifficultyMedium());
            Tooltip.install(btnMedium, new Tooltip("6×6 lưới · 18 cặp thẻ · 90 giây"));
        }

        if (btnHard != null) {
            btnHard.setOnAction(e -> onSelectDifficultyHard());
            Tooltip.install(btnHard, new Tooltip("8×8 lưới · 32 cặp thẻ · 120 giây"));
        }

        // Mặc định chọn EASY khi mở menu
        handleSelectDifficulty(Difficulty.EASY);
    }
    /**
     *
     * [1.1.3 - 1.1.4] Xử lý chọn một độ khó — cập nhật UI selected state.
     *
     * <p>Bước 1.1.3: Hệ thống nhận sự kiện onSelectDifficulty*() từ FXML binding.
     *              MainMenuController.handleSelectDifficulty(Difficulty) được gọi.</p>
     * <p>Bước 1.1.4: Nút được chọn được highlight (đổi màu, scale animation).
     *              Trạng thái selectedDifficulty trong controller được cập nhật.</p>
     *
     * <p>Precondition:  Màn hình menu đang hiển thị.</p>
     * <p>Postcondition: selectedDifficulty được set, nút tương ứng highlight.</p>
     *FIX: toast + ripple.
     * @param difficulty độ khó người dùng chọn (EASY, MEDIUM, HARD)
     *
     */
    private void handleSelectDifficulty(Difficulty difficulty) {
        if (difficulty == null) return;
        selectedDifficulty = difficulty;

        // Cập nhật pseudo-class và animation scale
        for (Map.Entry<Difficulty, Button> entry : diffButtonMap.entrySet()) {
            Button btn = entry.getValue();
            boolean isSelected = entry.getKey() == difficulty;
            btn.pseudoClassStateChanged(SELECTED, isSelected);
            renderButtonScaleAnimation(btn, isSelected);
            // Thêm hiệu ứng ripple (xoay nhẹ icon)
            if (isSelected) {
                Node icon = btn.lookup(".btn-icon");
                if (icon != null) {
                    RotateTransition rt = new RotateTransition(Duration.millis(200), icon);
                    rt.setByAngle(360);
                    rt.setCycleCount(1);
                    rt.play();
                }
            }
        }

        // [UC-01] Hiển thị toast thông báo
        showToast("Đã chọn độ khó: " + difficulty.getDisplayName());

        // [UC-01] Phát âm thanh khi chọn (nếu có file)
        AudioService.getInstance().playEffect("/assets/sounds/select.mp3");
    }
    /**
     * Hiển thị thông báo tạm thời (toast) trên menu.
     */
    private void showToast(String message) {
        Platform.runLater(() -> {
            if (btnEasy == null || btnEasy.getScene() == null) return;
            Label toast = new Label(message);
            toast.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 20;");
            toast.setOpacity(0);
            StackPane root = (StackPane) btnEasy.getScene().getRoot();
            root.getChildren().add(toast);
            StackPane.setAlignment(toast, javafx.geometry.Pos.TOP_CENTER);
            StackPane.setMargin(toast, new Insets(20, 0, 0, 0));
            FadeTransition ft = new FadeTransition(Duration.seconds(0.3), toast);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
            PauseTransition pt = new PauseTransition(Duration.seconds(1.5));
            pt.setOnFinished(e -> {
                FadeTransition out = new FadeTransition(Duration.seconds(0.3), toast);
                out.setFromValue(1);
                out.setToValue(0);
                out.setOnFinished(ev -> root.getChildren().remove(toast));
                out.play();
            });
            pt.play();
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
     * FIX: Thêm kiểm tra selectedDifficulty != null và AudioService khởi tạo đúng
     */
    @FXML
    public void onStartGame() {
        // Nếu chưa chọn độ khó nào, mặc định chọn EASY
        if (selectedDifficulty == null) {
            handleSelectDifficulty(Difficulty.EASY);
        }

        // Dừng BGM menu trước khi chuyển scene (tránh 2 BGM chồng nhau)
        AudioService.getInstance().stopBGM();

        // Chuyển sang GameScene với độ khó đã chọn
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

    // ════════════════════════════════════════════════════════════════════════
    // [UC-07 — Bước 7.1.1 → 7.1.3] onLeaderboard()
    // ════════════════════════════════════════════════════════════════════════

    /**
     * [UC-07 — 7.1.1 → 7.1.3] Xử lý click nút "Bảng xếp hạng".
     *
     * <p>Sequence diagram bước 2: MainMenuController.onLeaderboard() được gọi.</p>
     * <p>Sequence diagram bước 3: Gọi SceneManager.showLeaderboard() để
     * chuyển scene và tải leaderboard.fxml.</p>
     *
     * <p><b>Postcondition:</b> SceneManager tải leaderboard.fxml (bước 7.1.3),
     * khởi tạo LeaderboardController và hiển thị màn hình bảng xếp hạng.</p>
     */
    @FXML
    public void onLeaderboard() {
        // [7.1.2] Xử lý sự kiện click nút Leaderboard
        // [7.1.3] Yêu cầu SceneManager chuyển sang màn hình bảng xếp hạng
        SceneManager.getInstance().showLeaderboard();   // Mở BẢNG XẾP HẠNG
    }

}