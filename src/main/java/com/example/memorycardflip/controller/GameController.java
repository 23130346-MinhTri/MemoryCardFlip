package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Card;
import com.example.memorycardflip.model.CardType;
import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import com.example.memorycardflip.ui.CardFlipView;
import com.example.memorycardflip.ui.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller màn hình chơi game — game.fxml
 * UC-01 : Nhận Difficulty → khởi tạo bàn chơi
 * UC-02 : Lật thẻ — dùng CardFlipView + icon từ /assets/icons/
 * UC-03 : Kiểm tra cặp — khớp thì xóa, không khớp thì lật lại
 */
public class GameController implements Initializable {

    // ── FXML ──────────────────────────────────────────────────
    @FXML private GridPane    cardGrid;
    @FXML private StackPane   gridWrapper;
    @FXML private Label       lblDifficulty;
    @FXML private Label       lblPairs;
    @FXML private Label       lblMatched;
    @FXML private Label       lblTime;
    @FXML private ProgressBar timeProgressBar;
    @FXML private Label       lblStatus;
    @FXML private ProgressBar progressBar;
    @FXML private Button      btnRestart;

    // ── Layout constants ──────────────────────────────────────
    // HUD bar height (pref) + status bar height (pref)
    private static final double HUD_H    = 64.0;
    private static final double STATUS_H = 40.0;
    private static final double GRID_PAD = 14.0; // padding mỗi phía trong GridPane
    private static final double CARD_RATIO = 1.18; // height / width

    private static final double[] GAP     = { 10, 8, 6 };   // EASY, MEDIUM, HARD
    private static final double[] MAX_W   = { 115, 95, 75 }; // giới hạn trên

    // ── Icon pool ─────────────────────────────────────────────
    private final List<String> iconPool = new ArrayList<>();

    // ── Game state ────────────────────────────────────────────
    private Difficulty difficulty   = Difficulty.EASY;
    private GameState  gameState;

    private final Map<String, CardFlipView> viewMap = new HashMap<>();
    private Card    firstCard;
    private Card    secondCard;
    private boolean resolving    = false;
    private int     matchedPairs = 0;
    private int     totalPairs   = 0;
    private Timeline gameTimer;

    // ══════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameState = SceneManager.getInstance().getCurrentGameState();
        if (gameState != null) difficulty = gameState.getDifficulty();
        loadIconPool();

        // Chờ scene gắn vào stage để BorderPane phân bổ kích thước xong
        gridWrapper.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(this::startBoard);
            }
        });
        // Fallback nếu scene đã có sẵn (trường hợp reload)
        if (gridWrapper.getScene() != null) {
            javafx.application.Platform.runLater(this::startBoard);
        }
    }

    /** Dùng khi MainMenuController.openGameScene() truyền difficulty trực tiếp */
    public void setDifficulty(Difficulty d) {
        if (d == null) return;
        difficulty = d;
        if (gameState == null) gameState = new GameState(difficulty);
        loadIconPool();
        javafx.application.Platform.runLater(this::startBoard);
    }

    // ══════════════════════════════════════════════════════════
    // [UC-01] Khởi tạo bàn chơi
    // ══════════════════════════════════════════════════════════

    private void startBoard() {
        stopUCGM06Timer();
        viewMap.clear();
        firstCard    = null;
        secondCard   = null;
        resolving    = false;
        matchedPairs = 0;
        totalPairs   = difficulty.totalPairs();

        int gs = difficulty.getGridSize();
        lblDifficulty.setText(difficulty.getDisplayName() + "  " + gs + "×" + gs);
        if (lblTime != null) {
            lblTime.setStyle("");
        }
        if (timeProgressBar != null) {
            timeProgressBar.setProgress(1.0);
            timeProgressBar.getStyleClass().remove("time-progress-warning");
        }
        updateHUD();
        lblStatus.setText("Lật thẻ để bắt đầu!");
        progressBar.setProgress(0);

        List<Card> deck = buildDeck();
        renderGrid(deck);

        if (gameState != null) {
            gameState.reset();
            gameState.setCards(deck.toArray(new Card[0]));
            gameState.setStatus(GameStatus.PLAYING);
        }
        renderUCGM06Time();
        startUCGM06Timer();
    }

    private List<Card> buildDeck() {
        List<String> symbols = buildSymbolPool();
        List<Card>   deck    = new ArrayList<>();
        for (int i = 0; i < totalPairs; i++) {
            String pairId   = "pair-" + i;
            String symbol   = symbols.get(i % symbols.size());
            // String imageUrl = iconPool.isEmpty() ? null : iconPool.get(i % iconPool.size());
            String imageUrl = null;
            deck.add(new Card("c" + (i * 2),     pairId, CardType.EMOJI, symbol, imageUrl, i * 2));
            deck.add(new Card("c" + (i * 2 + 1), pairId, CardType.EMOJI, symbol, imageUrl, i * 2 + 1));
        }
        Collections.shuffle(deck);
        return deck;
    }

    /**
     * [UC-01] Tính card size động để lưới luôn vừa khít vùng center.
     *
     * Chiều cao available = scene height - HUD - status bar - grid padding
     * Chiều rộng available = gridWrapper width - grid padding
     * cardW = min(byWidth, byHeight/ratio), giới hạn max.
     */
    private void renderGrid(List<Card> deck) {
        cardGrid.getChildren().clear();
        cardGrid.getColumnConstraints().clear();
        cardGrid.getRowConstraints().clear();

        int    gs  = difficulty.getGridSize();
        int    idx = idx();
        double gap = GAP[idx];

        // ── Tính available space ──────────────────────────────
        // Chiều rộng: lấy từ gridWrapper (đã layout xong nhờ Platform.runLater)
        double availW = gridWrapper.getWidth();
        if (availW <= 0) {
            // Backup: lấy từ scene nếu gridWrapper chưa có width
            availW = gridWrapper.getScene() != null
                    ? gridWrapper.getScene().getWidth()
                    : 540;
        }

        // Chiều cao: scene height trừ HUD và status bar (luôn chính xác)
        double sceneH = gridWrapper.getScene() != null
                ? gridWrapper.getScene().getHeight()
                : 700;
        double availH = sceneH - HUD_H - STATUS_H;

        double usableW = availW  - GRID_PAD * 2;
        double usableH = availH  - GRID_PAD * 2;

        // ── Tính kích thước thẻ ──────────────────────────────
        double cardByW = (usableW - gap * (gs - 1)) / gs;
        double cardByH = (usableH - gap * (gs - 1)) / gs;

        // Chọn chiều nhỏ hơn → thẻ không tràn theo cả ngang lẫn dọc
        double cardW = Math.min(cardByW, cardByH / CARD_RATIO);
        cardW = Math.min(cardW, MAX_W[idx]);
        cardW = Math.max(cardW, 32); // tối thiểu 32px

        double cardH = cardW * CARD_RATIO;

        cardGrid.setHgap(gap);
        cardGrid.setVgap(gap);

        for (int i = 0; i < deck.size(); i++) {
            Card         card = deck.get(i);
            CardFlipView view = new CardFlipView();
            view.setCardSize(cardW, cardH);
            view.setOnFlipRequested(() -> onCardClick(card, view));
            cardGrid.add(view, i % gs, i / gs);
            viewMap.put(card.getId(), view);
        }
    }

    // ══════════════════════════════════════════════════════════
    // [UC-02] Lật thẻ
    // ══════════════════════════════════════════════════════════

    private void onCardClick(Card card, CardFlipView view) {
        if (gameState != null && gameState.getStatus() != GameStatus.PLAYING) return;
        if (resolving || !card.isClickable()) return;
        if (firstCard != null && firstCard.getId().equals(card.getId())) return;

        card.flip();
        view.showFront(card.getSymbol(), card.getImageURL());

        if (firstCard == null) {
            firstCard = card;
            lblStatus.setText("Chọn thẻ thứ hai...");
        } else {
            if (gameState != null) {
                gameState.resetCombo();
            }
            secondCard = card;
            if (gameState != null) {
                gameState.incrementMoves();
            }
            checkMatch();
        }
    }

    // ══════════════════════════════════════════════════════════
    // [UC-03] Kiểm tra cặp thẻ
    // ══════════════════════════════════════════════════════════

    private void checkMatch() {
        if (firstCard == null || secondCard == null) return;

        CardFlipView v1 = viewMap.get(firstCard.getId());
        CardFlipView v2 = viewMap.get(secondCard.getId());
        if (v1 == null || v2 == null) { clearSel(); return; }

        resolving = true;

        if (firstCard.isPairOf(secondCard)) {
            firstCard.match();
            secondCard.match();
            matchedPairs++;
            if (gameState != null) {
                gameState.incrementMatchedPairs();
                gameState.incrementCombo();
            }
            updateHUD();
            lblStatus.setText("✅  Khớp rồi! " + matchedPairs + "/" + totalPairs);

            PauseTransition p = new PauseTransition(Duration.millis(280));
            p.setOnFinished(e -> {
                v1.setMatched(true);
                v2.setMatched(true);
                clearSel();
                resolving = false;
                if (matchedPairs == totalPairs) onWin();
            });
            p.play();

        } else {
            lblStatus.setText("❌  Không khớp, thử lại...");
            PauseTransition p = new PauseTransition(Duration.millis(750));
            p.setOnFinished(e -> {
                firstCard.faceDown();
                secondCard.faceDown();
                v1.showBack();
                v2.showBack();
                clearSel();
                resolving = false;
                lblStatus.setText("Tiếp tục lật thẻ...");
            });
            p.play();
        }
    }

    private void onWin() {
        stopUCGM06Timer();
        lblStatus.setText("🎉  Bạn đã thắng! Tìm hết " + totalPairs + " cặp!");
        if (gameState != null) gameState.setStatus(GameStatus.WON);

        PauseTransition p = new PauseTransition(Duration.millis(500));
        p.setOnFinished(e -> SceneManager.getInstance().showResult());
        p.play();
    }

    // ── Helpers ───────────────────────────────────────────────

    private void updateHUD() {
        lblPairs.setText(matchedPairs + " / " + totalPairs);
        lblMatched.setText("Còn: " + (totalPairs - matchedPairs));
        progressBar.setProgress(totalPairs == 0 ? 0 : (double) matchedPairs / totalPairs);
    }

    /**
     * [UC-GM-06] Cập nhật hiển thị thời gian còn lại trên HUD.
     *
     * <p>Precondition: Difficulty đã được chọn cho ván hiện tại.</p>
     * <p>Postcondition: Nhãn thời gian hiển thị đúng số giây còn lại.</p>
     */
    private void renderUCGM06Time() {
        int remaining = difficulty.getTimeLimit();
        if (gameState != null) {
            remaining = gameState.getTimeRemaining();
        }
        if (lblTime != null) {
            lblTime.setText(remaining + "s");
        }
        if (timeProgressBar != null) {
            double progress = difficulty.getTimeLimit() == 0
                    ? 0
                    : (double) remaining / difficulty.getTimeLimit();
            timeProgressBar.setProgress(progress);
        }

    }

    /**
     * [UC-GM-06] Khởi động bộ đếm ngược cho ván hiện tại.
     *
     * <p>Precondition: gameState != null và trạng thái đang PLAYING.</p>
     * <p>Postcondition: Mỗi giây sẽ gọi xử lý UC-GM-06.</p>
     */
    private void startUCGM06Timer() {
        if (gameState == null) {
            return;
        }

        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> handleUCGM06TimerTick()));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    /**
     * [UC-GM-06] Dừng bộ đếm ngược hiện tại.
     *
     * <p>Postcondition: Không còn nhịp timer nào chạy cho ván hiện tại.</p>
     */
    private void stopUCGM06Timer() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
    }

    /**
     * [UC-GM-06] Giảm thời gian còn lại sau mỗi giây.
     *
     * <p>Precondition: GameStatus == PLAYING.</p>
     * <p>Postcondition: timeRemaining giảm 1; gần hết giờ gọi UC-GM-07; hết giờ gọi UC-GM-09.</p>
     */
    private void handleUCGM06TimerTick() {
        if (gameState == null || gameState.getStatus() != GameStatus.PLAYING) {
            stopUCGM06Timer();
            return;
        }

        gameState.decrementTime();
        renderUCGM06Time();

        if (gameState.getTimeRemaining() <= 10) {
            handleUCGM07TimerWarning();
        }

        if (gameState.isTimeUp()) {
            handleUCGM09LoseGame();
        }
    }

    /**
     * [UC-GM-07] Cảnh báo người chơi khi sắp hết giờ.
     *
     * <p>Precondition: timeRemaining <= 10.</p>
     * <p>Postcondition: Nhãn thời gian chuyển đỏ, đậm và status hiển thị cảnh báo.</p>
     */
    private void handleUCGM07TimerWarning() {
        if (lblTime != null) {
            lblTime.setStyle("-fx-text-fill: #ffeb3b; -fx-font-weight: bold;"); // Đổi màu chữ vàng sáng cho thời gian cảnh báo
        }
        if (timeProgressBar != null && !timeProgressBar.getStyleClass().contains("time-progress-warning")) {
            timeProgressBar.getStyleClass().add("time-progress-warning");
        }

        if (gameState != null && !gameState.isTimeUp()) {
            lblStatus.setText("Sap het gio! Con " + gameState.getTimeRemaining() + " giay.");
        }
    }

    /**
     * [UC-GM-09] Xử lý thua game khi hết giờ.
     *
     * <p>Precondition: timeRemaining <= 0.</p>
     * <p>Postcondition: Dừng timer, khóa input bằng LOST state và cập nhật HUD.</p>
     */
    private void handleUCGM09LoseGame() {
        stopUCGM06Timer();

        if (gameState != null) {
            gameState.setTimeRemaining(0);
            gameState.setStatus(GameStatus.LOST);
        }

        renderUCGM06Time();
        lblStatus.setText("Het gio! Ban da thua van nay.");

        PauseTransition p = new PauseTransition(Duration.millis(500));
        p.setOnFinished(e -> SceneManager.getInstance().showResult());
        p.play();
    }

    private void clearSel() { firstCard = null; secondCard = null; }

    private int idx() {
        return switch (difficulty) { case EASY -> 0; case MEDIUM -> 1; case HARD -> 2; };
    }

    private void loadIconPool() {
        iconPool.clear();
        try {
            URL dir = getClass().getResource("/assets/icons");
            if (dir == null || !"file".equalsIgnoreCase(dir.getProtocol())) return;
            Path directory = Path.of(dir.toURI());
            try (var s = Files.list(directory)) {
                s.filter(Files::isRegularFile)
                        .filter(p -> isImage(p.getFileName().toString()))
                        .filter(p -> !p.getFileName().toString().toLowerCase().startsWith("logogame"))
                        .sorted()
                        .map(p -> "/assets/icons/" + p.getFileName())
                        .forEach(iconPool::add);
            }
        } catch (IOException | URISyntaxException ignored) {}
    }

    private boolean isImage(String n) {
        String l = n.toLowerCase();
        return l.endsWith(".png") || l.endsWith(".jpg") || l.endsWith(".jpeg")
                || l.endsWith(".gif") || l.endsWith(".webp");
    }

    private List<String> buildSymbolPool() {
        List<String> s = new ArrayList<>();
        for (CardType t : CardType.values()) Collections.addAll(s, t.getSymbols());
        return s;
    }

    // ── FXML handlers ─────────────────────────────────────────

    @FXML
    private void handleUC01Restart() {
        javafx.application.Platform.runLater(this::startBoard);
    }

    @FXML
    public void onBackToMenu() {
        stopUCGM06Timer();
        SceneManager.getInstance().showMenu();
    }
}