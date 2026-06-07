package com.memorycardflip.controller;

import com.example.memorycardflip.controller.GameController;
import com.example.memorycardflip.model.*;
import com.example.memorycardflip.service.AudioService;
import com.example.memorycardflip.service.GameLogicService;
import com.example.memorycardflip.ui.CardFlipView;
import com.example.memorycardflip.ui.SceneManager;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameController Audio Tests")
class GameControllerAudioTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already started
        }
    }

    private GameController controller;
    private GameState gameState;

    @BeforeEach
    void setUp() throws Exception {
        controller = new GameController();
        gameState = new GameState(Difficulty.EASY);
        gameState.setStatus(GameStatus.PLAYING);

        // Inject GameState into SceneManager (since controller.initialize gets it from there)
        Field currentGameStateField = SceneManager.class.getDeclaredField("currentGameState");
        currentGameStateField.setAccessible(true);
        currentGameStateField.set(SceneManager.getInstance(), gameState);

        // Mock / Inject required JavaFX Nodes to prevent NullPointerExceptions
        injectPrivateField("lblScore", new Label());
        injectPrivateField("lblMoves", new Label());
        injectPrivateField("lblTime", new Label());
        injectPrivateField("lblCombo", new Label());
        injectPrivateField("lblDifficulty", new Label());
        injectPrivateField("lblStatus", new Label());
        injectPrivateField("lblPairs", new Label());
        injectPrivateField("lblRemaining", new Label());
        injectPrivateField("btnPause", new Button());
        injectPrivateField("btnHint", new Button());
        injectPrivateField("particlePane", new Pane());
        injectPrivateField("cardGrid", new GridPane());
        injectPrivateField("gridWrapper", new StackPane());

        // Associate gameState
        injectPrivateField("gameState", gameState);

        // Create viewMap and populate it
        Map<String, CardFlipView> viewMap = new HashMap<>();
        injectPrivateField("viewMap", viewMap);

        // Instantiate GameLogicService and inject
        GameLogicService gameLogicService = new GameLogicService(gameState, 2);
        injectPrivateField("gameLogicService", gameLogicService);

        AudioService.getInstance().clearTracking();
    }

    private void injectPrivateField(String fieldName, Object value) throws Exception {
        Field field = GameController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    @DisplayName("Phát âm thanh chúc mừng game-bonus.mp3 khi ghép đúng cặp")
    void testAudioOnMatch() throws Exception {
        // Chuẩn bị 2 thẻ thuộc cùng một cặp
        Card c1 = new Card("c1", "pairA", CardType.ANIMAL, "🐶", 0);
        Card c2 = new Card("c2", "pairA", CardType.ANIMAL, "🐶", 1);
        Card[] cards = {c1, c2};
        gameState.setCards(cards);

        // Đăng ký views vào viewMap để tránh NullPointerException khi onMatch cập nhật view
        Map<String, CardFlipView> viewMap = (Map<String, CardFlipView>) getPrivateField("viewMap");
        CardFlipView v1 = new CardFlipView();
        CardFlipView v2 = new CardFlipView();
        viewMap.put("c1", v1);
        viewMap.put("c2", v2);

        // Giả lập click thẻ thứ nhất
        triggerOnCardClick(c1, v1);

        // Giả lập click thẻ thứ hai (cùng cặp -> ghép đúng)
        triggerOnCardClick(c2, v2);

        // Kiểm tra xem âm thanh ghép đúng (/assets/sounds/game-bonus.mp3) có được gọi phát hay không
        assertEquals("/assets/sounds/game-bonus.mp3", AudioService.getInstance().getLastPlayedEffect());
    }

    @Test
    @DisplayName("Phát âm thanh resume.mp3 khi bấm nút Gợi ý (Hint)")
    void testAudioOnHintClick() throws Exception {
        // Chuẩn bị cặp thẻ chưa ghép khớp
        Card c1 = new Card("c1", "pairA", CardType.ANIMAL, "🐶", 0);
        Card c2 = new Card("pairA", "pairA", CardType.ANIMAL, "🐶", 1);
        Card[] cards = {c1, c2};
        gameState.setCards(cards);

        // Đăng ký views vào viewMap để tránh NullPointerException khi hint lật và highlight thẻ
        Map<String, CardFlipView> viewMap = (Map<String, CardFlipView>) getPrivateField("viewMap");
        CardFlipView v1 = new CardFlipView();
        CardFlipView v2 = new CardFlipView();
        viewMap.put("c1", v1);
        viewMap.put("pairA", v2);

        // Kích hoạt click gợi ý
        triggerOnHintClick();

        // Kiểm tra xem âm thanh gợi ý (/assets/sounds/resume.mp3) có được gọi phát hay không
        assertEquals("/assets/sounds/resume.mp3", AudioService.getInstance().getLastPlayedEffect());
    }

    private Object getPrivateField(String fieldName) throws Exception {
        Field field = GameController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(controller);
    }

    private void triggerOnCardClick(Card card, CardFlipView view) throws Exception {
        java.lang.reflect.Method method = GameController.class.getDeclaredMethod("onCardClick", Card.class, CardFlipView.class);
        method.setAccessible(true);
        method.invoke(controller, card, view);
    }

    private void triggerOnHintClick() throws Exception {
        java.lang.reflect.Method method = GameController.class.getDeclaredMethod("onHintClick");
        method.setAccessible(true);
        method.invoke(controller);
    }
}
