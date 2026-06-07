package com.memorycardflip.controller;

import com.example.memorycardflip.controller.GameController;
import com.example.memorycardflip.model.*;
import com.example.memorycardflip.service.AudioService;
import com.example.memorycardflip.service.GameLogicService;
import com.example.memorycardflip.ui.CardFlipView;
import com.example.memorycardflip.ui.SceneManager;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("GameController Audio Tests")
class GameControllerAudioTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX already initialized
        }
    }

    private GameController controller;
    private GameState gameState;

    @BeforeEach
    void setUp() throws Exception {
        controller = new GameController();

        gameState = new GameState(Difficulty.EASY);
        gameState.setStatus(GameStatus.PLAYING);

        // Inject GameState vào SceneManager
        Field currentGameStateField =
                SceneManager.class.getDeclaredField("currentGameState");
        currentGameStateField.setAccessible(true);
        currentGameStateField.set(
                SceneManager.getInstance(),
                gameState
        );

        // Inject các JavaFX controls cần thiết
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

        injectPrivateField("cardGrid", new GridPane());
        injectPrivateField("gridWrapper", new StackPane());

        // Inject game state
        injectPrivateField("gameState", gameState);

        // Inject viewMap
        Map<String, CardFlipView> viewMap = new HashMap<>();
        injectPrivateField("viewMap", viewMap);

        // Inject game logic service
        GameLogicService gameLogicService =
                new GameLogicService(gameState, 2);

        injectPrivateField(
                "gameLogicService",
                gameLogicService
        );

        AudioService.getInstance().clearTracking();
    }

    private void injectPrivateField(
            String fieldName,
            Object value
    ) throws Exception {

        Field field =
                GameController.class.getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(controller, value);
    }

    private Object getPrivateField(
            String fieldName
    ) throws Exception {

        Field field =
                GameController.class.getDeclaredField(fieldName);

        field.setAccessible(true);
        return field.get(controller);
    }

    private void triggerOnCardClick(
            Card card,
            CardFlipView view
    ) throws Exception {

        Method method =
                GameController.class.getDeclaredMethod(
                        "onCardClick",
                        Card.class,
                        CardFlipView.class
                );

        method.setAccessible(true);
        method.invoke(controller, card, view);
    }

    private void triggerOnHintClick() throws Exception {
        Method method =
                GameController.class.getDeclaredMethod(
                        "onHintClick"
                );

        method.setAccessible(true);
        method.invoke(controller);
    }

    @Test
    @DisplayName("Phát game-bonus.mp3 khi ghép đúng cặp")
    void testAudioOnMatch() throws Exception {

        Card c1 =
                new Card("c1", "pairA",
                        CardType.ANIMAL,
                        "🐶", 0);

        Card c2 =
                new Card("c2", "pairA",
                        CardType.ANIMAL,
                        "🐶", 1);

        gameState.setCards(new Card[]{c1, c2});

        @SuppressWarnings("unchecked")
        Map<String, CardFlipView> viewMap =
                (Map<String, CardFlipView>)
                        getPrivateField("viewMap");

        CardFlipView v1 = new CardFlipView();
        CardFlipView v2 = new CardFlipView();

        viewMap.put("c1", v1);
        viewMap.put("c2", v2);

        triggerOnCardClick(c1, v1);
        triggerOnCardClick(c2, v2);

        assertEquals(
                "/assets/sounds/game-bonus.mp3",
                AudioService.getInstance().getLastPlayedEffect()
        );
    }

    @Test
    @DisplayName("Phát resume.mp3 khi sử dụng Hint")
    void testAudioOnHintClick() throws Exception {

        Card c1 =
                new Card("c1", "pairA",
                        CardType.ANIMAL,
                        "🐶", 0);

        Card c2 =
                new Card("pairA", "pairA",
                        CardType.ANIMAL,
                        "🐶", 1);

        gameState.setCards(new Card[]{c1, c2});

        @SuppressWarnings("unchecked")
        Map<String, CardFlipView> viewMap =
                (Map<String, CardFlipView>)
                        getPrivateField("viewMap");

        CardFlipView v1 = new CardFlipView();
        CardFlipView v2 = new CardFlipView();

        viewMap.put("c1", v1);
        viewMap.put("pairA", v2);

        triggerOnHintClick();

        assertEquals(
                "/assets/sounds/resume.mp3",
                AudioService.getInstance().getLastPlayedEffect()
        );
    }

}
