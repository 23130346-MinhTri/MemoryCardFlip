package com.memorycardflip.service;

import com.example.memorycardflip.model.Card;
import com.example.memorycardflip.model.CardType;
import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import com.example.memorycardflip.service.GameLogicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameLogicService Tests")
class GameLogicServiceTest {

    private GameState gameState;
    private GameLogicService logicService;
    private String lastEvent;
    private int capturedMatchedPairs;
    private int capturedComboCount;

    private final GameLogicService.Listener listener = new GameLogicService.Listener() {
        @Override
        public void onFirstCardSelected(Card firstCard) {
            lastEvent = "first";
        }

        @Override
        public void onMatch(Card firstCard, Card secondCard,
                            int matchedPairs, int totalPairs, int comboCount) {
            lastEvent = "match";
            capturedMatchedPairs = matchedPairs;
            capturedComboCount = comboCount;
        }

        @Override
        public void onMismatch(Card firstCard, Card secondCard) {
            lastEvent = "mismatch";
        }
    };

    @BeforeEach
    void setUp() {
        gameState = new GameState(Difficulty.EASY);
        gameState.setStatus(GameStatus.PLAYING);
        logicService = new GameLogicService(gameState, Difficulty.EASY.totalPairs());
        lastEvent = null;
        capturedMatchedPairs = -1;
        capturedComboCount = -1;
    }

    @Test
    @DisplayName("First card selection triggers first event")
    void firstCardSelectionTriggersFirstEvent() {
        Card first = new Card("c1", "p1", CardType.ANIMAL, "🐶", 0);

        logicService.handleSelection(first, listener);

        assertEquals("first", lastEvent);
        assertSame(first, gameState.getFirstSelectedCard());
        assertFalse(gameState.isResolving());
        assertEquals(0, gameState.getMoves());
    }

    @Test
    @DisplayName("Matching pair increases matched pairs, moves and combo")
    void matchingPairUpdatesGameState() {
        Card first = new Card("c1", "p1", CardType.ANIMAL, "🐶", 0);
        Card second = new Card("c2", "p1", CardType.ANIMAL, "🐶", 1);

        logicService.handleSelection(first, listener);
        logicService.handleSelection(second, listener);

        assertEquals("match", lastEvent);
        assertEquals(1, gameState.getMatchedPairs());
        assertEquals(1, gameState.getMoves());
        assertEquals(1, gameState.getComboCount());
        assertEquals(1, logicService.getMatchedPairs());
        assertTrue(gameState.getFirstSelectedCard() == null || gameState.isResolving());
    }

    @Test
    @DisplayName("Mismatching pair triggers mismatch and resets combo")
    void mismatchingPairResetsCombo() {
        Card first = new Card("c1", "p1", CardType.ANIMAL, "🐶", 0);
        Card second = new Card("c2", "p2", CardType.ANIMAL, "🐱", 1);

        logicService.handleSelection(first, listener);
        logicService.handleSelection(second, listener);

        assertEquals("mismatch", lastEvent);
        assertEquals(1, gameState.getMoves());
        assertEquals(1, gameState.getWrongAttempts());
        assertEquals(0, gameState.getComboCount());
        assertTrue(gameState.isResolving());
    }

    @Test
    @DisplayName("Cannot select the same card twice in a row")
    void cannotSelectSameCardTwice() {
        Card first = new Card("c1", "p1", CardType.ANIMAL, "🐶", 0);

        logicService.handleSelection(first, listener);
        lastEvent = null;
        logicService.handleSelection(first, listener);

        assertNull(lastEvent);
        assertEquals(0, gameState.getMoves());
    }

    @Test
    @DisplayName("Cannot select a card while resolving")
    void cannotSelectWhileResolving() {
        Card first = new Card("c1", "p1", CardType.ANIMAL, "🐶", 0);
        Card second = new Card("c2", "p2", CardType.ANIMAL, "🐱", 1);
        Card third = new Card("c3", "p3", CardType.ANIMAL, "🐰", 2);

        logicService.handleSelection(first, listener);
        logicService.handleSelection(second, listener);
        lastEvent = null;

        logicService.handleSelection(third, listener);

        assertNull(lastEvent);
        assertTrue(gameState.isResolving());
    }

    @Test
    @DisplayName("Board unlocks after clearSelection is called")
    void boardUnlocksAfterClearSelection() {
        Card first = new Card("c1", "p1", CardType.ANIMAL, "🐶", 0);
        Card second = new Card("c2", "p2", CardType.ANIMAL, "🐱", 1);
        Card next = new Card("c3", "p3", CardType.ANIMAL, "🐰", 2);

        logicService.handleSelection(first, listener);
        logicService.handleSelection(second, listener);
        assertTrue(gameState.isResolving());

        logicService.clearSelection();
        lastEvent = null;
        logicService.handleSelection(next, listener);

        assertEquals("first", lastEvent);
        assertFalse(gameState.isResolving());
        assertSame(next, gameState.getFirstSelectedCard());
    }

    @Test
    @DisplayName("Cannot select an already matched card")
    void cannotSelectAlreadyMatchedCard() {
        Card first = new Card("c1", "p1", CardType.ANIMAL, "🐶", 0);
        Card second = new Card("c2", "p1", CardType.ANIMAL, "🐶", 1);

        logicService.handleSelection(first, listener);
        logicService.handleSelection(second, listener);
        assertEquals("match", lastEvent);

        lastEvent = null;
        logicService.handleSelection(first, listener);

        assertNull(lastEvent);
        assertEquals(1, gameState.getMatchedPairs());
    }
}
