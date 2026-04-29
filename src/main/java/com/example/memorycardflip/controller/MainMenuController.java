package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Difficulty;
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
 *   <li>[UC-01] Chọn cấp độ & Bắt đầu game</li>
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
    }
    // UC-01 — Chọn cấp độ & Bắt đầu game
    /**
     * [UC-01] Thiết lập sự kiện cho 3 nút chọn độ khó.
     * Mặc định chọn EASY khi mở menu.
     */
    private void setupUC01DifficultyButtons() {
        btnEasy.setOnAction(e   -> onUCMM01EasyClick());
        btnMedium.setOnAction(e -> onUCMM02MediumClick());
        btnHard.setOnAction(e   -> onUCMM03HardClick());

        // Mặc định chọn EASY
        handleUC01SelectDifficulty(Difficulty.EASY);
    }
    /**
     * [UC-01] Xử lý chọn một độ khó — cập nhật UI selected state.
     *
     * <p>Precondition:  Màn hình menu đang hiển thị.</p>
     * <p>Postcondition: selectedDifficulty được set, nút tương ứng highlight.</p>
     *
     * @param difficulty độ khó người dùng chọn
     */
    private void handleUC01SelectDifficulty(Difficulty difficulty) {
        selectedDifficulty = difficulty;

        diffButtonMap.forEach((diff, btn) -> {
            boolean isSelected = diff == difficulty;
            btn.pseudoClassStateChanged(SELECTED, isSelected);
            renderUC01ButtonScale(btn, isSelected);
        });
    }
    /**
     * [UC-01] Scale animation cho nút được chọn / bỏ chọn.
     */
    private void renderUC01ButtonScale(Button btn, boolean isSelected) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setToX(isSelected ? 1.04 : 1.0);
        st.setToY(isSelected ? 1.04 : 1.0);
        st.playFromStart();
    }

    /**
     * [UC-01] Bắt đầu game — chuyển sang GameScene với độ khó đã chọn.
     *
     * <p>Precondition:  selectedDifficulty != null.</p>
     * <p>Postcondition: GameScene được load, GameState được khởi tạo.</p>
     */
    @FXML
    public void onUC01StartGame() {
        if (selectedDifficulty == null) {
            handleUC01SelectDifficulty(Difficulty.EASY);
        }
        SceneManager.getInstance().showGame(selectedDifficulty);
    }

    /**
     * [UC-01 / UC-MM-01] Click nút Easy.
     * Lần 1 → chọn Easy. Lần 2 (đã chọn rồi) → start game luôn.
     */
    @FXML
    public void onUCMM01EasyClick() {
        if (selectedDifficulty == Difficulty.EASY) {
            onUC01StartGame();
        } else {
            handleUC01SelectDifficulty(Difficulty.EASY);
        }
    }

    /**
     * [UC-01 / UC-MM-02] Click nút Medium.
     * Lần 1 → chọn Medium. Lần 2 → start game luôn.
     */
    @FXML
    public void onUCMM02MediumClick() {
        if (selectedDifficulty == Difficulty.MEDIUM) {
            onUC01StartGame();
        } else {
            handleUC01SelectDifficulty(Difficulty.MEDIUM);
        }
    }

    /**
     * [UC-01 / UC-MM-03] Click nút Hard.
     * Lần 1 → chọn Hard. Lần 2 → start game luôn.
     */
    @FXML
    public void onUCMM03HardClick() {
        if (selectedDifficulty == Difficulty.HARD) {
            onUC01StartGame();
        } else {
            handleUC01SelectDifficulty(Difficulty.HARD);
        }
    }
    // ── Event Handlers ────────────────────────────────────────
    /** Toggle âm thanh on/off */
    @FXML
    public void onSoundToggle() {
        soundEnabled = !soundEnabled;
        btnSound.setText(soundEnabled ? "🔊  Âm thanh" : "🔇  Tắt tiếng");
        // TODO: AudioService.getInstance().setEnabled(soundEnabled);
    }
    /** Nút Về game — hiển thị dialog thông tin */
    @FXML
    public void onAbout() {
        // TODO: SceneManager.getInstance().showAboutDialog();
    }
    /** Nút Lịch sử — hiển thị bảng điểm */
    @FXML
    public void onHistory() {
        // TODO: SceneManager.getInstance().showHistoryDialog();
    }

    @FXML
    public void onStartEasy() {
        openGameScene(Difficulty.EASY);
    }

    @FXML
    public void onStartMedium() {
        openGameScene(Difficulty.MEDIUM);
    }

    @FXML
    public void onStartHard() {
        openGameScene(Difficulty.HARD);
    }

    private void openGameScene(Difficulty difficulty) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            Parent root = loader.load();

            GameController gameController = loader.getController();
            gameController.setDifficulty(difficulty);

            Stage stage = (Stage) btnEasy.getScene().getWindow();
            Scene scene = new Scene(root, 900, 700);
            stage.setTitle("Memory Card Flip - " + difficulty.getDisplayName());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot open game scene", exception);
        }
    }
}