package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Card;
import com.example.memorycardflip.model.CardType;
import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import com.example.memorycardflip.service.AnimationService;
import com.example.memorycardflip.service.AudioService;
import com.example.memorycardflip.service.GameLogicService;
import com.example.memorycardflip.service.GameTimerService;
import com.example.memorycardflip.ui.CardFlipView;
import com.example.memorycardflip.ui.SceneManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller màn hình chơi game — game.fxml
 * UC-01 : Nhận Difficulty → khởi tạo bàn chơi
 * UC-02 : Lật thẻ — dùng CardFlipView + icon từ /assets/icons/
 * UC-07 : Kiểm tra cặp thẻ — khớp thì xóa, không khớp thì lật lại
 * UC-08 : Tính điểm và streak bonus
 * UC-09 : Đếm ngược thời gian
 */
public class GameController implements Initializable {
    // ── FXML ──────────────────────────────────────────────────
    @FXML private GridPane    cardGrid;
    @FXML private StackPane   gridWrapper;
    @FXML private Label       lblDifficulty;
    @FXML private Label       lblPairs;
    @FXML private Label       lblTime;
    @FXML private ProgressBar timeProgressBar;
    @FXML private Label       lblStatus;
    @FXML private Button      btnPause;
    // =============== THÊM CÁC FXML BINDING MỚI ===============
    @FXML private Label lblScore;      // Hiển thị điểm số
    @FXML private Label lblMoves;      // Hiển thị số lượt di chuyển
    @FXML private Label lblRemaining;  // Hiển thị số cặp còn lại
    @FXML private Label lblCombo;      // Hiển thị combo (giữ đà ghép đúng)
    // ── Layout constants ──────────────────────────────────────
    private boolean isRendering = false;
    private static final double CARD_RATIO = 1.18; // height / width
    private static final double[] GAP     = { 10, 8, 6 };   // EASY, MEDIUM, HARD
    private static final double[] MAX_W   = { 115, 95, 75 }; // giới hạn trên
    // ── Icon pool ─────────────────────────────────────────────
    private final List<String> iconPool = new ArrayList<>();
    // ── Game state ────────────────────────────────────────────
    private Difficulty difficulty   = Difficulty.EASY;
    private GameState  gameState;
    private ResourceBundle messages;

    private final Map<String, CardFlipView> viewMap = new HashMap<>();
    private int     totalPairs   = 0;
    private GameTimerService timerManager;
    private GameLogicService gameLogicService;
    private ComboNotificationController comboNotification;
    private StackPane notificationOverlay;
    // ══════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.messages = resources != null
                ? resources
                : ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

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
        loadComboNotification();
        gridWrapper.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (Math.abs(newVal.doubleValue() - oldVal.doubleValue()) > 1.0) {
                rerender();
            }
        });
        gridWrapper.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (Math.abs(newVal.doubleValue() - oldVal.doubleValue()) > 1.0) {
                rerender();
            }
        });
    }
    private void loadComboNotification() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/combo_notification.fxml"));
            notificationOverlay = loader.load();
            comboNotification = loader.getController();

            // Đợi scene sẵn sàng rồi mới thêm
            if (gridWrapper.getScene() != null) {
                addNotificationToScene();
            } else {
                gridWrapper.sceneProperty().addListener((obs, old, newScene) -> {
                    if (newScene != null) {
                        addNotificationToScene();
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Không thể load combo notification: " + e.getMessage());
            comboNotification = null;
        }
    }
    private void addNotificationToScene() {
        if (notificationOverlay == null || comboNotification == null) return;

        if (notificationOverlay.getParent() != null) {
            ((StackPane) notificationOverlay.getParent()).getChildren().remove(notificationOverlay);
        }

        if (gridWrapper.getScene().getRoot() instanceof StackPane) {
            StackPane root = (StackPane) gridWrapper.getScene().getRoot();
            if (!root.getChildren().contains(notificationOverlay)) {
                StackPane.setAlignment(notificationOverlay, javafx.geometry.Pos.CENTER);
                root.getChildren().add(notificationOverlay);
            }
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
        AudioService.getInstance().playBGM("/assets/sounds/game.mp3");
        stopTimer();
        viewMap.clear();
        totalPairs   = difficulty.totalPairs();
        // Ẩn combo notification khi restart game
        if (comboNotification != null) {
            comboNotification.hide();
        }
        int gs = difficulty.getGridSize();
        lblDifficulty.setText(difficulty.getDisplayName() + "  " + gs + "×" + gs);
        // KHỞI TẠO UI HIỂN THỊ BAN ĐẦU
        if (lblTime != null) {
            lblTime.setText(tr("default.time"));
            lblTime.setStyle("");
        }
        if (lblScore != null) {
            lblScore.setText(tr("default.score"));
        }
        if (lblMoves != null) {
            lblMoves.setText(tr("default.moves"));
        }
        if (lblPairs != null) {
            lblPairs.setText("0 / " + totalPairs);
        }
        if (lblRemaining != null) {
            lblRemaining.setText(String.valueOf(totalPairs));
        }
        if (timeProgressBar != null) {
            timeProgressBar.setProgress(1.0);
            timeProgressBar.getStyleClass().remove("time-progress-warning");
        }
        if (btnPause != null) {
            btnPause.setText(tr("button.pause"));
            btnPause.setDisable(false);
        }
        if (lblCombo != null) {
            lblCombo.setText("x0");
        }
        lblStatus.setText(tr("status.start"));
        List<Card> deck = buildDeck();
        renderGrid(deck);
        if (gameState != null) {
            gameState.reset();
            gameState.setCards(deck.toArray(new Card[0]));
            gameState.setStatus(GameStatus.PLAYING);
            if (gameLogicService == null) {
                gameLogicService = new GameLogicService(gameState, totalPairs);
            } else {
                gameLogicService.reset(totalPairs);
            }
            if (timerManager != null) {
                timerManager.dispose();
            }
            timerManager = new GameTimerService(gameState, new GameTimerService.Listener() {
                @Override
                public void onTick() {
                    handleTimerTick();
                }
                @Override
                public void onTimeUp() {
                    handleUCGM09LoseGame();
                }
            });
        }
        renderUCGM06Time();
        startTimer();
    }
    /**
     * Cập nhật hiển thị điểm số
     */
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
        double gap = GAP[idx()];
        // ── Tính available space ──────────────────────────────
        double availW = gridWrapper.getWidth();
        double availH = gridWrapper.getHeight();   // gridWrapper đã là CENTER, KHÔNG trừ topBox/bottomBox
        if (availW <= 0) availW = 480;
        if (availH <= 0) availH = 480;
        Insets padding = cardGrid.getPadding();
        double usableW = availW - padding.getLeft() - padding.getRight();
        double usableH = availH - padding.getTop()  - padding.getBottom();
        // ── Tính kích thước thẻ ──────────────────────────────
        double cardByW = (usableW - gap * (gs - 1)) / gs;
        double cardByH = (usableH - gap * (gs - 1)) / gs;
        double cardW = Math.min(cardByW, cardByH / CARD_RATIO);
        cardW = Math.min(cardW, MAX_W[idx()]);      // giới hạn max width
        double cardH = cardW * CARD_RATIO;
// Sau khi tính xong cardW, cardH — khóa kích thước grid lại
        double totalGridW = gs * cardW + (gs - 1) * gap + padding.getLeft() + padding.getRight();
        double totalGridH = gs * cardH + (gs - 1) * gap + padding.getTop()  + padding.getBottom();
        cardGrid.setMaxSize(totalGridW, totalGridH);
        cardGrid.setMinSize(totalGridW, totalGridH);
        cardGrid.setPrefSize(totalGridW, totalGridH);
        cardGrid.setHgap(gap);
        cardGrid.setVgap(gap);
        for (int i = 0; i < deck.size(); i++) {
            Card         card = deck.get(i);
            CardFlipView view = new CardFlipView();
            view.setCardSize(cardW, cardH);
            view.showBack();
            view.setOnFlipRequested(() -> onCardClick(card, view));
            cardGrid.add(view, i % gs, i / gs);
            viewMap.put(card.getId(), view);
        }
    }
    // ══════════════════════════════════════════════════════════
    // [UC-02] Lật thẻ
    // ══════════════════════════════════════════════════════════
    /**
     * [UC-02 / UC-07] Xử lý thao tác lật thẻ và kiểm tra cặp.
     *
     * <p>Use Case này bắt đầu khi người chơi nhấn vào một thẻ trên bảng.</p>
     * <p>Postcondition: thẻ được lật lên, trạng thái chọn thẻ được cập nhật, và nếu là cặp thứ hai thì tiến hành kiểm tra khớp.</p>
     */
    private void onCardClick(Card card, CardFlipView view) {
        if (gameLogicService == null || !gameLogicService.canSelect(card)) {
            return;
        }
        // 🛑 HARD BLOCK chống spam click
        if (gameState.isResolving()) return;
        card.flip();
        view.showFront(card.getSymbol(), card.getImageURL());
        // [UC-07] Kiểm tra cặp thẻ sau khi người chơi chọn thẻ thứ hai.
        gameLogicService.handleSelection(card, new GameLogicService.Listener() {
            @Override
            public void onFirstCardSelected(Card firstCard) {
                lblStatus.setText(tr("status.chooseSecond"));
            }
            @Override
            public void onMatch(Card firstCard, Card secondCard,
                                int matchedPairs, int totalPairs, int comboCount) {
                CardFlipView v1 = viewMap.get(firstCard.getId());
                CardFlipView v2 = viewMap.get(secondCard.getId());
                firstCard.match();
                secondCard.match();
                updateHUD();
                lblStatus.setText(tr("status.match", matchedPairs, totalPairs));
                // 🎯 HIỆU ỨNG
                AnimationService.playMatchEffect(v1);
                AnimationService.playMatchEffect(v2);
                // 🎉 COMBO EFFECT
                if (comboNotification != null && comboCount > 1) {
                    comboNotification.showCombo(comboCount);
                }
                PauseTransition pause = new PauseTransition(Duration.millis(280));
                pause.setOnFinished(e -> {
                    v1.setMatched(true);
                    v2.setMatched(true);
                    gameLogicService.clearSelection(); // ✅ chỉ gọi cái này
                    if (matchedPairs == totalPairs) {
                        onWin();
                    }
                });
                pause.play();
            }
            @Override
            public void onMismatch(Card firstCard, Card secondCard) {
                CardFlipView v1 = viewMap.get(firstCard.getId());
                CardFlipView v2 = viewMap.get(secondCard.getId());
                if (v1 == null || v2 == null) {
                    gameLogicService.clearSelection();
                    return;
                }
                updateHUD();
                AnimationService.playShakeAnimation(v1);
                AnimationService.playShakeAnimation(v2);
                lblStatus.setText(tr("status.mismatch"));
                PauseTransition pause = new PauseTransition(Duration.millis(750));
                pause.setOnFinished(e -> {
                    firstCard.faceDown();
                    secondCard.faceDown();
                    v1.showBack();
                    v2.showBack();
                    gameLogicService.clearSelection(); // ✅ xử lý toàn bộ state
                    lblStatus.setText(tr("status.continueFlipping"));
                });
                pause.play();
            }
        });
    }
    /**
     * [UC-03] Xử lý khi người chơi hoàn thành toàn bộ cặp bài.
     *
     * <p>Use Case này kết thúc phiên chơi và chuyển sang màn hình kết quả.</p>
     * <p>Postcondition: gameState chuyển sang trạng thái WON, timer dừng, và ResultScene được mở.</p>
     */
    private void onWin() {
        stopTimer();
        disposeTimer();
        lblStatus.setText(tr("status.win", totalPairs));
        if (gameState != null) gameState.setStatus(GameStatus.WON);
        PauseTransition p = new PauseTransition(Duration.millis(500));
        p.setOnFinished(e -> SceneManager.getInstance().showResult());
        p.play();
    }
    // ── Helpers ───────────────────────────────────────────────
    /**
     * [UC-08] Cập nhật HUD điểm, lượt và streak sau mỗi hành động.
     *
     * <p>Use Case này hiển thị điểm số, số lượt, số cặp còn lại và combo hiện tại.</p>
     * <p>Postcondition: các nhãn trên HUD phản ánh trạng thái gameState mới nhất.</p>
     */
    private void updateHUD() {
        if (lblPairs != null && gameLogicService != null) {
            lblPairs.setText(gameLogicService.getMatchedPairs() + " / " + totalPairs);
        }
        if (lblRemaining != null && gameLogicService != null) {
            lblRemaining.setText(String.valueOf(totalPairs - gameLogicService.getMatchedPairs()));
        }
        if (lblScore != null && gameState != null) {
            lblScore.setText(String.valueOf(gameState.calculateScore()));
        }
        if (lblMoves != null && gameState != null) {
            lblMoves.setText(String.valueOf(gameState.getMoves()));
        }
        // ✅ THÊM COMBO
        if (lblCombo != null && gameState != null) {
            lblCombo.setText("x" + gameState.getComboCount());
        }
    }
    private String tr(String key, Object... args) {
        if (messages == null || !messages.containsKey(key)) {
            return MessageFormat.format(key, args);
        }
        return MessageFormat.format(messages.getString(key), args);
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
            int minutes = remaining / 60;
            int seconds = remaining % 60;
            lblTime.setText(String.format("%02d:%02d", minutes, seconds));
        }
        if (timeProgressBar != null) {
            double progress = difficulty.getTimeLimit() == 0 ? 0 : (double) remaining / difficulty.getTimeLimit();
            timeProgressBar.setProgress(progress);
        }
    }
    /**
     * [UC-GM-06] Khởi động bộ đếm ngược cho ván hiện tại.
     *
     * <p>Precondition: gameState != null và trạng thái đang PLAYING.</p>
     * <p>Postcondition: Mỗi giây sẽ gọi xử lý UC-GM-06.</p>
     */
    private void startTimer() {
        if (timerManager != null) {
            timerManager.start();
        }
    }
    /**
     * [UC-GM-06] Dừng bộ đếm ngược hiện tại.
     *
     * <p>Postcondition: Không còn nhịp timer nào chạy cho ván hiện tại.</p>
     */
    private void stopTimer() {
        if (timerManager != null) {
            timerManager.stop();
        }
    }
    private void disposeTimer() {
        if (timerManager != null) {
            timerManager.dispose();
            timerManager = null;
        }
    }
    /**
     * [UC-GM-06] Giảm thời gian còn lại sau mỗi giây.
     *
     * <p>Precondition: GameStatus == PLAYING.</p>
     * <p>Postcondition: timeRemaining giảm 1; gần hết giờ gọi UC-GM-07; hết giờ gọi UC-GM-09.</p>
     */
    /**
     * [UC-09] Xử lý mỗi nhịp đếm ngược.
     *
     * <p>Use Case này cập nhật thời gian mỗi giây và kiểm tra khi còn dưới 10 giây.</p>
     * <p>Postcondition: thời gian hiển thị được cập nhật, điểm hiển thị được refresh, và cảnh báo gần hết giờ được kích hoạt.</p>
     */
    private void handleTimerTick() {
        renderUCGM06Time();
        if (lblScore != null && gameState != null) {
            lblScore.setText(String.valueOf(gameState.calculateScore()));
        }
        if (gameState != null && gameState.getTimeRemaining() <= 10) {
            handleUCGM07TimerWarning();
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
            lblStatus.setText(tr("timer.warning", gameState.getTimeRemaining()));
        }
    }
    /**
     * [UC-GM-09] Xử lý thua game khi hết giờ.
     *
     * <p>Precondition: timeRemaining <= 0.</p>
     * <p>Postcondition: Dừng timer, khóa input bằng LOST state và cập nhật HUD.</p>
     */
    private void handleUCGM09LoseGame() {
        stopTimer();
        disposeTimer();
        if (gameState != null) {
            gameState.setTimeRemaining(0);
            gameState.setStatus(GameStatus.LOST);
        }
        renderUCGM06Time();
        lblStatus.setText(tr("timer.expired"));
        PauseTransition p = new PauseTransition(Duration.millis(500));
        p.setOnFinished(e -> SceneManager.getInstance().showResult());
        p.play();
    }
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
    /**
     * [UC-04] Toggles giữa tạm dừng và tiếp tục.
     *
     * <p>Use Case này cho phép người chơi tạm dừng hoặc tiếp tục ván hiện tại.</p>
     * <p>Postcondition: nếu đang chơi thì chuyển sang PAUSED; nếu đang tạm dừng thì chuyển sang PLAYING.</p>
     */
    @FXML
    private void onTogglePause() {
        if (gameState == null) return;
        if (gameState.getStatus() == GameStatus.PLAYING) {
            pauseGame();
        } else if (gameState.getStatus() == GameStatus.PAUSED) {
            resumeGame();
        }
    }
    /**
     * [UC-04] Tạm dừng ván chơi.
     *
     * <p>Postcondition: timer bị dừng, status hiển thị Pause, và âm thanh nền dừng lại.</p>
     */
    private void pauseGame() {
        if (gameState == null || gameState.getStatus() != GameStatus.PLAYING) return;
        stopTimer();
        gameState.setStatus(GameStatus.PAUSED);
        if (lblStatus != null) {
            lblStatus.setText(tr("status.pause"));
        }
        if (btnPause != null) {
            btnPause.setText(tr("button.resume"));
        }
        AudioService.getInstance().pauseBGM();
    }
    /**
     * [UC-04] Tiếp tục ván chơi sau khi đã tạm dừng.
     *
     * <p>Postcondition: timer tiếp tục chạy, trạng thái trở lại PLAYING, và âm thanh nền được tiếp tục.</p>
     */
    private void resumeGame() {
        if (gameState == null || gameState.getStatus() != GameStatus.PAUSED) return;
        gameState.setStatus(GameStatus.PLAYING);
        startTimer();
        if (lblStatus != null) {
            lblStatus.setText(tr("status.resume"));
        }
        if (btnPause != null) {
            btnPause.setText(tr("button.pause"));
        }
        AudioService.getInstance().resumeBGM();
    }
    @FXML
    private void handleUC01Restart() {
        javafx.application.Platform.runLater(this::startBoard);
    }
    @FXML
    public void onBackToMenu() {
        stopTimer();
        disposeTimer();
        SceneManager.getInstance().showMenu();
    }
    private void rerender() {
        if (isRendering) return;
        isRendering = true;
        javafx.application.Platform.runLater(() -> {
            if (gameState != null && gameState.getCards() != null) {
                renderGrid(List.of(gameState.getCards()));
            }
            isRendering = false;
        });
    }
}