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
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.stage.Stage;
import com.example.memorycardflip.ui.NotificationManager;

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
 *
 * <p>UseCase phụ trách:</p>
 * <ul>
 *   <li>[1. Select Difficulty] Nhận Difficulty → khởi tạo bàn chơi</li>
 *   <li>[2. Flip Card] Lật thẻ — dùng CardFlipView + icon từ /assets/icons/</li>
 *   <li>[3. Match Pair] Kiểm tra cặp thẻ — khớp thì xóa, không khớp thì lật lại</li>
 *   <li>[4. Calculate Score] Tính điểm và streak bonus</li>
 *   <li>[5. Pause Game] Tạm dừng ván chơi</li>
 *   <li>[6. Resume Game] Tiếp tục ván chơi sau khi tạm dừng</li>
 * </ul>
 */
public class GameController implements Initializable {
    // ── FXML ──────────────────────────────────────────────────
    @FXML private GridPane    cardGrid;
    @FXML private StackPane   gridWrapper;
    @FXML private Label       lblDifficulty;
    @FXML private Label       lblPairs;
    @FXML private Label       lblTime;          // [UC12] Hiển thị thời gian còn lại theo định dạng MM:SS
    @FXML private ProgressBar timeProgressBar;  // [UC12] Thanh tiến trình giảm dần theo timeRemaining
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
    // [UC12] Quản lý Timeline đếm ngược, start/stop/dispose theo vòng đời ván chơi.
    private GameTimerService timerManager;
    private GameLogicService gameLogicService;
    private ComboNotificationController comboNotification;
    private StackPane notificationOverlay;
    private StackPane pausedOverlay;  // Overlay hiển thị khi tạm dừng
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

    /**
     * [11.1.6] Gắn overlay thông báo combo vào vùng chơi để nó xuất hiện ngay trên game board.
     * Nếu root scene không sẵn sàng, overlay sẽ được gắn sau khi scene được tạo.
     */
    private void addNotificationToScene() {
        if (notificationOverlay == null || comboNotification == null) return;

        if (notificationOverlay.getParent() != null) {
            ((StackPane) notificationOverlay.getParent()).getChildren().remove(notificationOverlay);
        }

        if (!gridWrapper.getChildren().contains(notificationOverlay)) {
            notificationOverlay.setMouseTransparent(true);
            notificationOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            StackPane.setAlignment(notificationOverlay, javafx.geometry.Pos.CENTER);
            gridWrapper.getChildren().add(notificationOverlay);
        }
    }
    /**
     * [1. Select Difficulty] Đặt độ khó và khởi tạo GameState.
     * Được gọi từ SceneManager.showGame() hoặc khi reload game.
     * FIX: Thêm kiểm tra gameState != null trước khi set difficulty
     */
    public void setDifficulty(Difficulty d) {
        if (d == null) {
            System.err.println("[UC-01] Difficulty cannot be null, defaulting to EASY");
            d = Difficulty.EASY;
        }
        difficulty = d;
        // [UC-01] Kiểm tra gameState hiện tại - nếu null thì tạo mới
        if (gameState == null) {
            gameState = new GameState(difficulty);
        } else {
            // [UC-01] Nếu gameState đã tồn tại, cập nhật difficulty và reset
            gameState.setStatus(GameStatus.IDLE);
        }
        loadIconPool();
        javafx.application.Platform.runLater(this::startBoard);
    }
    // ══════════════════════════════════════════════════════════
    // 1. Select Difficulty — Khởi tạo bàn chơi
    // ══════════════════════════════════════════════════════════

    /**
     * [1.1.8 - 1.1.10] Khởi tạo bàn chơi game.
     * [UC4 - Play again] Hàm này cũng được gọi lại khi người chơi bấm "Chơi lại".
     * [UC-01] Đảm bảo difficulty được áp dụng đúng khi khởi tạo board mới.
     * <p>Bước 1.1.8: GameController.startBoard() được gọi:
     *              tạo GameState với Difficulty đã chọn, khởi tạo card grid,
     *              reset timer, score, moves, và tất cả các labels HUD.</p>
     * <p>Bước 1.1.9: GameScene được hiển thị. Timer bắt đầu đếm ngược.
     *              AudioService phát BGM.</p>
     * <p>Bước 1.1.10: Người chơi có thể bắt đầu lật thẻ bằng cách click vào bất kỳ thẻ nào.</p>
     *
     * <p>Postcondition:</p>
     * <ul>
     *   <li>Card grid được render đầy đủ theo độ khó đã chọn</li>
     *   <li>Timer reset và bắt đầu đếm ngược</li>
     *   <li>HUD labels được cập nhật (score, moves, pairs, time, combo)</li>
     *   <li>BGM bắt đầu phát</li>
     *   <li>Người chơi có thể click vào thẻ để lật</li>
     * </ul>
     * FIX: Đảm bảo tất cả UI components được khởi tạo đúng trước khi render
     */
    private void startBoard() {
        // [UC-01] Đảm bảo difficulty không bị null trước khi tính totalPairs
        if (difficulty == null) {
            difficulty = Difficulty.EASY;
            if (gameState != null) gameState.setDifficulty(difficulty);
        }
        // [UC4] Bắt đầu lại toàn bộ ván: nhạc, timer, grid, HUD và trạng thái chơi.
        // Phát nhạc nền (BGM)
        AudioService.getInstance().playBGM("/assets/sounds/game.mp3");

        // [UC4] Dừng timer cũ để ván mới không còn tick từ ván trước.
        stopTimer();

        // [UC4] Xóa map view cũ trước khi render bộ bài mới.
        viewMap.clear();

        // Tính toán số cặp thẻ dựa trên độ khó
        totalPairs   = difficulty.totalPairs();

        
            // [11.2.1] Khi chơi lại, ẩn combo notification để không giữ trạng thái cũ.
        if (comboNotification != null) {
            comboNotification.hide();
        }

        // Cập nhật nhãn độ khó (vd: "Easy  4×4")
        int gs = difficulty.getGridSize();
        lblDifficulty.setText(difficulty.getDisplayName() + "  " + gs + "×" + gs);

        // ── Khởi tạo UI HUD (Heads-Up Display) ──────────────────────
        // Đặt lại tất cả các labels về trạng thái ban đầu

        if (lblTime != null) {
            lblTime.setText(tr("default.time"));  // Vd: "01:00"
            lblTime.setStyle("");  // Clear màu warning nếu có
        }

        if (lblScore != null) {
            lblScore.setText(tr("default.score"));  // Vd: "0"
        }

        if (lblMoves != null) {
            lblMoves.setText(tr("default.moves"));  // Vd: "0"
        }

        if (lblPairs != null) {
            lblPairs.setText("0 / " + totalPairs);  // Vd: "0 / 8" (EASY)
        }

        if (lblRemaining != null) {
            lblRemaining.setText(String.valueOf(totalPairs));  // Số cặp còn lại
        }

        if (timeProgressBar != null) {
            timeProgressBar.setProgress(1.0);  // Progress bar đầy 100%
            timeProgressBar.getStyleClass().remove("time-progress-warning");  // Xóa style warning
        }

        if (btnPause != null) {
            btnPause.setText(tr("button.pause"));  // Nút Pause
            btnPause.setDisable(false);  // Kích hoạt nút
        }

        if (lblCombo != null) {
            lblCombo.setText("x0");  // Combo counter bắt đầu từ 0
        }

        lblStatus.setText(tr("status.start"));  // Vd: "Game started!"

        // ── Xây dựng bộ bài ──────────────────────────────────────────
        // Tạo các thẻ theo độ khó, shuffle, gắn vào card grid
        List<Card> deck = buildDeck();
        renderGrid(deck);

        // ── Khởi tạo GameState ────────────────────────────────────────
        // Lưu deck vào GameState để game logic có thể truy cập
        if (gameState != null) {
            // [UC4] Reset toàn bộ trạng thái (score, moves, combo, timer, selection...).
            gameState.reset();
            gameState.setCards(deck.toArray(new Card[0]));  // Gán deck
            gameState.setStatus(GameStatus.PLAYING);  // Đánh dấu game đang chơi

            // [UC4] Reset logic ghép cặp để lượt chơi mới không còn selection/matchedPairs cũ.
            if (gameLogicService == null) {
                gameLogicService = new GameLogicService(gameState, totalPairs);
            } else {
                gameLogicService.reset(totalPairs);
            }

            // [UC4] Hủy timer cũ và tạo timer mới cho lượt chơi lại.
            if (timerManager != null) {
                timerManager.dispose();  // Dispose timer cũ
            }
            // [UC12] Khởi tạo timer với callback: mỗi tick cập nhật UI, hết giờ thì xử lý thua.
            timerManager = new GameTimerService(gameState, new GameTimerService.Listener() {
                @Override
                public void onTick() {
                    handleTimerTick();  // Cập nhật UI mỗi tick
                }
                @Override
                public void onTimeUp() {
                    handleUCGM09LoseGame();  // Thua cuộc nếu hết thời gian
                }
            });
        }

        // [UC12] Render thời gian ban đầu trước khi timer bắt đầu chạy.
        // ── Cập nhật hiển thị thời gian ──────────────────────────────
        renderUCGM06Time();

        // [UC12] Bắt đầu bộ đếm ngược cho ván hiện tại.
        // ── Bắt đầu timer đếm ngược ──────────────────────────────────
        startTimer();

        // ══════════════════════════════════════════════════════════
        // Bây giờ người chơi có thể bắt đầu lật thẻ!
        // ══════════════════════════════════════════════════════════
    }
    /**
     * Cập nhật hiển thị điểm số
     */
    private List<Card> buildDeck() {
        List<CardFace> faces = buildCardFacePool();
        List<Card>   deck    = new ArrayList<>();
        for (int i = 0; i < totalPairs; i++) {
            String pairId   = "pair-" + i;
            CardFace face   = faces.get(i % faces.size());
            String symbol   = face.symbol();
            // String imageUrl = iconPool.isEmpty() ? null : iconPool.get(i % iconPool.size());
            String imageUrl = null;
            deck.add(new Card("c" + (i * 2),     pairId, face.type(), symbol, imageUrl, i * 2));
            deck.add(new Card("c" + (i * 2 + 1), pairId, face.type(), symbol, imageUrl, i * 2 + 1));
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
    // [2] Lật thẻ
    // ══════════════════════════════════════════════════════════
    /**
     * [UC-02, UC-09] Xử lý khi người chơi click vào một thẻ.
     *
     * <p>Precondition:</p>
     * <ul>
     *   <li>gameLogicService != null</li>
     *   <li>Thẻ chưa matched và board không đang resolving</li>
     *   <li>GameState ở trạng thái PLAYING</li>
     * </ul>
     *
     * <p>Flow xử lý:</p>
     * <ul>
     *   <li>2.1.1 Người chơi click vào thẻ</li>
     *   <li>2.1.2 CardFlipView gọi onCardClick(card, view)</li>
     *   <li>2.1.3 Kiểm tra trạng thái game</li>
     *   <li>2.1.4 Kiểm tra resolving</li>
     *   <li>2.1.6 Kiểm tra card.isClickable()</li>
     *   <li>2.1.7 Gọi card.flip()</li>
     *   <li>2.1.8 Gọi view.showFront()</li>
     *   <li>2.1.10 Kiểm tra firstCard == null</li>
     *   <li>2.1.11 Gán firstCard</li>
     *   <li>2.1.12 Hiển thị "Chọn thẻ thứ hai..."</li>
     *   <li>2.1.13 Click thẻ thứ hai</li>
     *   <li>2.1.14 Gán secondCard</li>
     *   <li>2.1.15 Reset combo</li>
     *   <li>2.1.16 Tăng số lượt</li>
     *   <li>2.1.17 Kiểm tra cặp thẻ</li>
     *   <li>2.1.18 Chuyển sang nhánh khớp hoặc không khớp</li>
     * </ul>
     */
    private void onCardClick(Card card, CardFlipView view) {
        // 2.1.2: CardFlipView gửi yêu cầu onCardClick
        // 2.1.3: Kiểm tra trạng thái game
        if (gameLogicService == null || !gameLogicService.canSelect(card)) {
            return;  // 2.2.0: Nếu không hợp lệ, bỏ qua
        }
        // 2.1.4: Kiểm tra biến resolving
        //   HARD BLOCK chống spam click
        if (gameState.isResolving()) return;  // 2.2.1: Nếu resolving == true, khóa
        // 2.1.7: Flip thẻ (đảo trạng thái)
        card.flip();
        // 2.1.8: Hiển thị mặt trước
        view.showFront(card.getSymbol(), card.getImageURL());
        // 9.1.1: checkMatch() - core logic của UC09
        gameLogicService.handleSelection(card, new GameLogicService.Listener() {
            // 2.1.10: Kiểm tra firstCard == null → đây là lựa chọn đầu tiên
            // 2.1.11: Gán firstCard
            // 2.1.12: Cập nhật "Chọn thẻ thứ hai..."
            @Override
            public void onFirstCardSelected(Card firstCard) {
                lblStatus.setText(tr("status.chooseSecond"));
                // [UC2 v2.0] Bật highlight xanh để báo hiệu đang chờ thẻ thứ hai
                view.showHighlight(true);
            }
            // 9.2.0 - 9.2.12: Nhánh hai thẻ khớp nhau
            @Override
            public void onMatch(Card firstCard, Card secondCard,
                                int matchedPairs, int totalPairs, int comboCount) {
                // Luồng chính: hai thẻ khớp -> cập nhật điểm/streak, hiệu ứng thắng cặp, kiểm tra điều kiện win.
                // 9.1.2: Lấy CardFlipView từ viewMap
                CardFlipView v1 = viewMap.get(firstCard.getId());
                CardFlipView v2 = viewMap.get(secondCard.getId());

                // [UC2 v2.0] Tắt highlight thẻ đầu tiên khi đã khớp cặp
                if (v1 != null) v1.showHighlight(false);

                // 9.2.1: Đánh dấu hai thẻ đã matched
                firstCard.match();
                secondCard.match();


                
                // 9.2.5: [11.1.9-11.1.10]  Cập nhật HUD (điểm, combo, moves)
                updateHUD();

                // 9.2.6: Cập nhật thông báo "Khớp!"
                lblStatus.setText(tr("status.match", matchedPairs, totalPairs));

                //   HIỆU ỨNG
                AnimationService.playMatchEffect(v1);
                AnimationService.playMatchEffect(v2);
                // [11.1.6] Chỉ hiển thị thông báo combo khi comboCount >= 2.
                if (comboNotification != null && comboCount > 1) {
                    comboNotification.showCombo(comboCount);
                }

                // [11.1.6 / 11.2.4] Nếu comboCount đủ cao, showCombo() đã đổi icon/text sang HOT/MEGA STREAK.
                if (comboCount >= 3 && gridWrapper.getScene() != null) {
                    try {
                        Stage owner = (Stage) gridWrapper.getScene().getWindow();
                        NotificationManager.showStreakNotification(owner,
                                "Streak x" + comboCount,
                                "Bạn đang streak " + comboCount + " lần!",
                                "★");
                    } catch (Exception ignored) {
                    }
                }
                

                // 9.2.7: PauseTransition(280ms) để hiển thị hiệu ứng matched
                PauseTransition pause = new PauseTransition(Duration.millis(280));
                pause.setOnFinished(e -> {
                    // 9.2.8: Gọi setMatched(true) để đánh dấu thẻ trên UI
                    v1.setMatched(true);
                    v2.setMatched(true);

                    // 9.2.9: Gọi clearSelection() để reset firstCard/secondCard
                    gameLogicService.clearSelection();

                    // 9.2.11: Kiểm tra điều kiện chiến thắng (matchedPairs == totalPairs)
                    if (matchedPairs == totalPairs) {
                        // [UC3] Khi ghép đủ cặp, chuyển sang luồng View result.
                        onWin();
                    }
                });
                pause.play();
            }
            // 9.3.0 - 9.3.9: Nhánh hai thẻ không khớp
            @Override
            public void onMismatch(Card firstCard, Card secondCard) {
                // Luồng phụ: hai thẻ không khớp -> combo đã reset ở service, UI phát hiệu ứng sai và lật lại thẻ.
                // 9.1.2: Lấy CardFlipView từ viewMap
                CardFlipView v1 = viewMap.get(firstCard.getId());
                CardFlipView v2 = viewMap.get(secondCard.getId());

                // 9.3.1: Kiểm tra views hợp lệ
                if (v1 == null || v2 == null) {
                    gameLogicService.clearSelection();
                    return;
                }

                // [UC2 v2.0] Tắt highlight thẻ đầu tiên khi bị mismatch
                v1.showHighlight(false);

                // 9.2.5: Cập nhật HUD
                // [11.2.6] Nếu lblCombo == null thì không thể hiển thị HUD combo, nhưng combo vẫn được tính trong model.
                updateHUD();

                // Hiệu ứng shake để báo không khớp
                AnimationService.playShakeAnimation(v1);
                AnimationService.playShakeAnimation(v2);

                // 9.3.2: Cập nhật thông báo "Không khớp!"
                lblStatus.setText(tr("status.mismatch"));

                // 9.3.3: PauseTransition(750ms) để người chơi quan sát
                PauseTransition pause = new PauseTransition(Duration.millis(750));
                pause.setOnFinished(e -> {
                    // 9.3.4-9.3.5: Lật úp hai thẻ (faceDown)
                    firstCard.faceDown();
                    secondCard.faceDown();

                    // 9.3.6-9.3.7: Hiển thị mặt sau (showBack)
                    v1.showBack();
                    v2.showBack();

                    // 9.3.8: Gọi clearSelection() để reset firstCard/secondCard
                    gameLogicService.clearSelection();

                    // 9.3.9: resolving = false được gọi inside clearSelection()
                    lblStatus.setText(tr("status.continueFlipping"));
                });
                pause.play();
            }
        });
    }
    /**
     * [UC3 - View result] Xử lý khi người chơi hoàn thành toàn bộ cặp bài.
     *
     * <p>Use Case này kết thúc phiên chơi và chuyển sang màn hình kết quả.</p>
     * <p>Postcondition: gameState chuyển sang trạng thái WON, timer dừng, và ResultScene được mở.</p>
     * <p>UC-09: 9.2.12 gọi onWin() và 9.3.6 ghi log nếu phát sinh exception.</p>
     */
    private void onWin() {
        try {
            // [UC3] Khóa trạng thái ván trước khi mở ResultScene để kết quả không còn thay đổi.
            stopTimer();
            disposeTimer();
            lblStatus.setText(tr("status.win", totalPairs));
            if (gameState != null) gameState.setStatus(GameStatus.WON);
            PauseTransition p = new PauseTransition(Duration.millis(500));
            // [UC3] Sau hiệu ứng ngắn, chuyển sang màn hình View result.
            p.setOnFinished(e -> SceneManager.getInstance().showResult());
            p.play();
        } catch (Exception ex) {
            // 9.3.6: Xử lý exception trong onWin()
            System.err.println("Error in onWin(): " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    // ── Helpers ───────────────────────────────────────────────
    /**
     * [UC-08] Cập nhật HUD điểm, lượt và streak sau mỗi hành động.
     *
     * <p>Use Case này hiển thị điểm số, số lượt, số cặp còn lại và combo hiện tại.</p>
     * <p>Postcondition: các nhãn trên HUD phản ánh trạng thái gameState mới nhất.</p>
     */
    private void updateHUD() {
        // Luồng chính + phụ đều đi qua đây để đồng bộ HUD sau mỗi lần xử lý match/mismatch.
        // [11.1.9-11.1.10] Đồng bộ comboCount từ model sang HUD: lblCombo.setText("x" + comboCount).
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
     * [UC12 - Count down timer] Cập nhật hiển thị thời gian còn lại trên HUD.
     *
     * <p>Precondition: Difficulty đã được chọn cho ván hiện tại.</p>
     * <p>Postcondition: Nhãn thời gian hiển thị đúng MM:SS, progress bar phản ánh tỷ lệ thời gian còn lại.</p>
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
     * [UC12 - Count down timer] Khởi động bộ đếm ngược cho ván hiện tại.
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
     * [UC12 - Count down timer] Dừng bộ đếm ngược hiện tại.
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
     * [UC12 - Count down timer] Giảm thời gian còn lại sau mỗi giây.
     *
     * <p>Precondition: GameStatus == PLAYING.</p>
     * <p>Postcondition: timeRemaining giảm 1; gần hết giờ gọi UC-GM-07; hết giờ gọi UC-GM-09.</p>
     */
    /**
     * [UC12 - Count down timer] Xử lý mỗi nhịp đếm ngược.
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
     * [UC12 - Count down timer] Cảnh báo người chơi khi sắp hết giờ.
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
     * [UC12 - Count down timer] Xử lý thua game khi hết giờ.
     *
     * <p>Precondition: timeRemaining <= 0.</p>
     * <p>Postcondition: Dừng timer, khóa input bằng LOST state và cập nhật HUD.</p>
     */
    private void handleUCGM09LoseGame() {
        stopTimer();
        disposeTimer();
        if (gameState != null) {
            gameState.setTimeRemaining(0);
            // [UC3] Đánh dấu LOST để ResultController hiển thị GAME OVER.
            gameState.setStatus(GameStatus.LOST);
        }
        renderUCGM06Time();
        lblStatus.setText(tr("timer.expired"));
        PauseTransition p = new PauseTransition(Duration.millis(500));
        // [UC3] Hết giờ cũng đi tới View result với trạng thái thua.
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
    private List<CardFace> buildCardFacePool() {
        List<CardFace> faces = new ArrayList<>();
        // [UC2 v2.0] Lấy symbol từ TECH trước, sau đó các loại khác
        // Shuffle để mỗi ván có thể xuất hiện ký hiệu khác nhau
        for (CardType type : List.of(CardType.UI_ICONS, CardType.DEVICES, CardType.PROGRAMMING, CardType.CLOUD)) {
            for (String symbol : type.getSymbols()) {
                faces.add(new CardFace(type, symbol));
            }
        }
        Collections.shuffle(faces);
        return faces;
    }

    private record CardFace(CardType type, String symbol) {}
    // ── FXML handlers ─────────────────────────────────────────
    /**
     * [UC-05,6] Toggles giữa tạm dừng và tiếp tục.
     *
     * <p>Use Case này cho phép người chơi tạm dừng hoặc tiếp tục ván hiện tại.</p>
     * <p>Postcondition: nếu đang chơi thì chuyển sang PAUSED; nếu đang tạm dừng thì chuyển sang PLAYING.</p>
     */
    // ══════════════════════════════════════════════════════════
    // 5. Pause Game & 6. Resume Game — Tạm dừng & Tiếp tục
    // ══════════════════════════════════════════════════════════

    /**
     * [5.1.2 - 5.1.6] Xử lý click nút Pause — toggle trạng thái game.
     *
     * <p>Bước 5.1.0: Người chơi đang trong ván chơi.</p>
     * <p>Bước 5.1.1: GameStatus = PLAYING, timer đang chạy.</p>
     * <p>Bước 5.1.2: Người chơi click vào nút btnPause (icon ⏸).</p>
     * <p>Bước 5.1.3: game.fxml kích hoạt sự kiện. GameController.onTogglePause() được gọi.</p>
     * <p>Bước 5.1.4: Controller kiểm tra gameState != null và GameStatus hợp lệ.</p>
     * <p>Bước 5.1.5: Phân nhánh theo trạng thái hiện tại:</p>
     * <ul>
     *   <li>Nếu status = PLAYING → gọi pauseGame() (Usecase 5)</li>
     *   <li>Nếu status = PAUSED → gọi resumeGame() (Usecase 6)</li>
     * </ul>
     * <p>Bước 5.1.6: UI được cập nhật: lblStatus đổi text, btnPause đổi trạng thái.</p>
     *
     * <p>Precondition: gameState != null, game đang chơi hoặc đã tạm dừng.</p>
     * <p>Postcondition: Trạng thái game được toggle (PLAYING ↔ PAUSED).</p>
     */
    @FXML
    private void onTogglePause() {
        // Bước 5.1.4: Kiểm tra gameState hợp lệ
        if (gameState == null) return;

        // Bước 5.1.5: Phân nhánh theo GameStatus hiện tại
        if (gameState.getStatus() == GameStatus.PLAYING) {
            // Chuyển sang PAUSED (Usecase 5: Pause Game)
            pauseGame();
        } else if (gameState.getStatus() == GameStatus.PAUSED) {
            // Chuyển sang PLAYING (Usecase 6: Resume Game)
            resumeGame();
        }
    }
    /**
     * [5. Pause Game] Tạm dừng ván chơi.
     *
     * <p>Được gọi từ onTogglePause() khi GameStatus = PLAYING.</p>
     *
     * <p>Hành động chi tiết:</p>
     * <ul>
     *   <li>Dừng timer (stopTimer()) — thời gian ngừng đếm ngược</li>
     *   <li>Đặt GameStatus = PAUSED</li>
     *   <li>Cập nhật UI:</li>
     *     <ul>
     *       <li>lblStatus: Đổi thành "Paused" (từ i18n)</li>
     *       <li>btnPause: Đổi icon/text thành "Resume" (để người chơi click tiếp tục)</li>
     *     </ul>
     *   <li>Dừng âm thanh nền (pauseBGM())</li>
     * </ul>
     *
     * <p>Precondition: gameState != null, GameStatus = PLAYING.</p>
     * <p>Postcondition: GameStatus = PAUSED, timer dừng, UI cập nhật, âm thanh tạm dừng.</p>
     *Fix: Khi pause game, tất cả thẻ sẽ bị khóa không thể click.
     *          Điều này ngăn người chơi tiếp tục lật thẻ khi game đang tạm dừng.
     */
    private void pauseGame() {
        if (gameState == null || gameState.getStatus() != GameStatus.PLAYING) return;

        // [UC-05] Khóa tất cả thẻ
        disableAllCards(true);
        // [UC-05] Tạo và hiển thị overlay nếu chưa có
        if (pausedOverlay == null) {
            createPausedOverlay();
        }
        if (pausedOverlay.getParent() == null) {
            gridWrapper.getChildren().add(pausedOverlay);
        }
        pausedOverlay.setVisible(true);

        stopTimer();
        gameState.setStatus(GameStatus.PAUSED);
        if (lblStatus != null) lblStatus.setText(tr("status.pause"));
        if (btnPause != null) btnPause.setText(tr("button.resume"));
        AudioService.getInstance().pauseBGM();
        // [UC-05] Phát âm thanh pause (tùy chọn)
        AudioService.getInstance().playEffect("/assets/sounds/pause.mp3");
    }

    /**
     * [UC-05][UC-06] Helper method để khóa/mở khóa tất cả thẻ trên bàn chơi.
     *
     * @param disabled true nếu muốn khóa thẻ (disable click), false nếu mở khóa
     */
    private void disableAllCards(boolean disabled) {
        if (cardGrid == null) return;
        for (javafx.scene.Node node : cardGrid.getChildren()) {
            if (node instanceof CardFlipView) {
                ((CardFlipView) node).setDisable(disabled);
            }
        }
    }
    /**
     * [6. Resume Game] Tiếp tục ván chơi sau khi đã tạm dừng.
     * FIX: Khi resume game, tất cả thẻ sẽ được mở khóa trở lại.
     *            Chỉ những thẻ chưa matched mới có thể click được.
     * <p>Được gọi từ onTogglePause() khi GameStatus = PAUSED.</p>
     *
     * <p>Hành động chi tiết:</p>
     * <ul>
     *   <li>Đặt GameStatus = PLAYING</li>
     *   <li>Khởi động lại timer (startTimer()) — thời gian tiếp tục đếm ngược từ giá trị tạm dừng</li>
     *   <li>Cập nhật UI:</li>
     *     <ul>
     *       <li>lblStatus: Đổi thành "Resumed" hoặc "Game On" (từ i18n)</li>
     *       <li>btnPause: Đổi icon/text lại thành "Pause" (để người chơi có thể tạm dừng lại)</li>
     *     </ul>
     *   <li>Tiếp tục phát âm thanh nền (resumeBGM())</li>
     * </ul>
     *
     * <p>Precondition: gameState != null, GameStatus = PAUSED.</p>
     * <p>Postcondition: GameStatus = PLAYING, timer chạy, UI cập nhật, âm thanh phát lại.</p>
     */
    private void resumeGame() {
        if (gameState == null || gameState.getStatus() != GameStatus.PAUSED) return;

        // [UC-06] Mở khóa tất cả thẻ
        disableAllCards(false);
        // [UC-06] Ẩn overlay
        if (pausedOverlay != null) {
            pausedOverlay.setVisible(false);
        }

        gameState.setStatus(GameStatus.PLAYING);
        startTimer();
        if (lblStatus != null) lblStatus.setText(tr("status.resume"));
        if (btnPause != null) btnPause.setText(tr("button.pause"));
        AudioService.getInstance().resumeBGM();
        AudioService.getInstance().playEffect("/assets/sounds/resume.mp3");
    }
    /**
     * Tạo overlay tạm dừng với nút Resume bên trong.
     */
    private void createPausedOverlay() {
        pausedOverlay = new StackPane();
        pausedOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");
        pausedOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pausedOverlay.setMouseTransparent(false); // bắt sự kiện click

        VBox content = new VBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        Label pauseLabel = new Label("⏸ GAME PAUSED");
        pauseLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #f5c842;");
        Button resumeBtn = new Button("▶ RESUME");
        resumeBtn.setStyle("-fx-background-color: #2a6f8f; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 20;");
        resumeBtn.setOnAction(e -> onTogglePause()); // gọi toggle để resume
        content.getChildren().addAll(pauseLabel, resumeBtn);
        pausedOverlay.getChildren().add(content);
        StackPane.setAlignment(content, javafx.geometry.Pos.CENTER);
    }
    @FXML
    private void handleUC01Restart() {
        // [UC4] Nút "Chơi lại" ngay trong GameScene: dựng lại bàn chơi hiện tại.
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
