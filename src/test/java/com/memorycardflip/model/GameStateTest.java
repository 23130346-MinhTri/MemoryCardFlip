package com.memorycardflip.model;

import com.example.memorycardflip.model.*;
import com.example.memorycardflip.service.GameLogicService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameState Model Tests")
class GameStateTest {

    private GameState easyState;
    private GameState hardState;

    @BeforeEach
    void setUp() {
        easyState = new GameState(Difficulty.EASY);
        hardState = new GameState(Difficulty.HARD);
    }

    // ── INIT ─────────────────────────────────────────────

    @Nested
    class InitTests {

        @Test
        void easyDefaults() {
            assertAll(
                    () -> assertEquals(Difficulty.EASY, easyState.getDifficulty()),
                    () -> assertEquals(60,              easyState.getTimeRemaining()),
                    () -> assertEquals(0,               easyState.getMatchedPairs()),
                    () -> assertEquals(0,               easyState.getMoves()),
                    () -> assertEquals(GameStatus.IDLE, easyState.getStatus()),
                    () -> assertEquals(0,               easyState.getComboCount())
            );
        }

        @Test
        void hardTimeLimit() {
            assertEquals(120, hardState.getTimeRemaining());
        }
    }

    // ── SELECT CARD ─────────────────────────────────────
    //
    // GameState.selectCard() đã bị xóa (gây double-count với GameLogicService).
    // Các test selection dùng GameLogicService trực tiếp để kiểm tra
    // side-effects thực sự lên GameState — đúng với cách code production hoạt động.

    @Nested
    @DisplayName("Selection via GameLogicService")
    class SelectionTests {

        private GameLogicService service;

        // Capture kết quả callback từ listener
        private String lastEvent; // "first" | "match" | "mismatch"
        private int    capturedMatchedPairs;
        private int    capturedComboCount;

        private final GameLogicService.Listener listener = new GameLogicService.Listener() {
            @Override
            public void onFirstCardSelected(Card firstCard) {
                lastEvent = "first";
            }

            @Override
            public void onMatch(Card firstCard, Card secondCard,
                                int matchedPairs, int totalPairs, int comboCount) {
                lastEvent            = "match";
                capturedMatchedPairs = matchedPairs;
                capturedComboCount   = comboCount;
            }

            @Override
            public void onMismatch(Card firstCard, Card secondCard) {
                lastEvent = "mismatch";
            }
        };

        @BeforeEach
        void setUp() {
            easyState.setStatus(GameStatus.PLAYING);
            service   = new GameLogicService(easyState, Difficulty.EASY.totalPairs());
            lastEvent = null;
        }

        @Test
        @DisplayName("Chọn thẻ đầu tiên → onFirstCardSelected")
        void firstSelection() {
            Card c1 = new Card("1", "p1", CardType.ANIMAL, "🐶", 0);

            service.handleSelection(c1, listener);

            assertEquals("first", lastEvent);
        }

        @Test
        @DisplayName("Chọn đúng cặp → matchedPairs +1, moves +1, combo +1")
        void matchPair() {
            Card c1 = new Card("1", "p1", CardType.ANIMAL, "🐶", 0);
            Card c2 = new Card("2", "p1", CardType.ANIMAL, "🐶", 1);

            service.handleSelection(c1, listener);
            service.handleSelection(c2, listener);

            assertAll(
                    () -> assertEquals("match", lastEvent),
                    () -> assertEquals(1, easyState.getMatchedPairs()),
                    () -> assertEquals(1, easyState.getMoves()),
                    () -> assertEquals(1, easyState.getComboCount())
            );
        }

        @Test
        @DisplayName("Chọn sai cặp → mismatch, moves +1, wrongAttempts +1, combo reset")
        void mismatchPair() {
            Card c1 = new Card("1", "p1", CardType.ANIMAL, "🐶", 0);
            Card c2 = new Card("2", "p2", CardType.ANIMAL, "🐱", 1);

            service.handleSelection(c1, listener);
            service.handleSelection(c2, listener);

            assertAll(
                    () -> assertEquals("mismatch", lastEvent),
                    () -> assertEquals(1, easyState.getMoves()),
                    () -> assertEquals(1, easyState.getWrongAttempts()),
                    () -> assertEquals(0, easyState.getComboCount())
            );
        }

        @Test
        @DisplayName("Không thể chọn lại thẻ đầu tiên")
        void cannotSelectSameCardTwice() {
            Card c1 = new Card("1", "p1", CardType.ANIMAL, "🐶", 0);

            service.handleSelection(c1, listener);
            lastEvent = null;

            // Cố chọn lại c1 → canSelect() trả false → listener không được gọi
            service.handleSelection(c1, listener);

            assertNull(lastEvent, "Không được trigger event khi chọn lại thẻ cũ");
        }

        @Test
        @DisplayName("Không thể chọn thẻ khi đang resolving")
        void cannotSelectWhileResolving() {
            Card c1 = new Card("1", "p1", CardType.ANIMAL, "🐶", 0);
            Card c2 = new Card("2", "p2", CardType.ANIMAL, "🐱", 1);
            Card c3 = new Card("3", "p3", CardType.ANIMAL, "🐰", 2);

            service.handleSelection(c1, listener);
            service.handleSelection(c2, listener); // mismatch → isResolving = true
            lastEvent = null;

            // Board đang lock, c3 không được chọn
            service.handleSelection(c3, listener);

            assertNull(lastEvent, "Không được trigger event khi board đang lock");
        }

        @Test
        @DisplayName("Sau clearSelection board nhận click bình thường trở lại")
        void boardUnlocksAfterClear() {
            Card c1 = new Card("1", "p1", CardType.ANIMAL, "🐶", 0);
            Card c2 = new Card("2", "p2", CardType.ANIMAL, "🐱", 1);
            Card c3 = new Card("3", "p3", CardType.ANIMAL, "🐰", 2);

            service.handleSelection(c1, listener);
            service.handleSelection(c2, listener); // mismatch → lock

            service.clearSelection();              // giả lập animation xong
            lastEvent = null;

            service.handleSelection(c3, listener);

            assertEquals("first", lastEvent, "Board phải nhận click sau khi clearSelection");
        }
    }

    // ── COMPLETE ─────────────────────────────────────────

    @Test
    @DisplayName("isComplete đúng khi đủ cặp")
    void shouldBeComplete() {
        for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
            easyState.incrementMatchedPairs();
        }
        assertTrue(easyState.isComplete());
    }

    // ── TIME ─────────────────────────────────────────────

    @Nested
    class TimeTests {

        @Test
        void decrementTime() {
            int before = easyState.getTimeRemaining();
            easyState.decrementTime();
            assertEquals(before - 1, easyState.getTimeRemaining());
        }

        @Test
        void notBelowZero() {
            easyState.setTimeRemaining(0);
            easyState.decrementTime();
            assertEquals(0, easyState.getTimeRemaining());
        }

        @Test
        void isTimeUp() {
            easyState.setTimeRemaining(0);
            assertTrue(easyState.isTimeUp());
        }
    }

    // ── SCORE ───────────────────────────────────────────

    /**
     * Score calculation tests.
     *
     * These tests verify:
     * - base score when no matches
     * - score increases with matched pairs
     * - combo/streak bonuses (combo count increases score)
     * - time bonus (more time remaining increases score)
     *
     * Keeping these checks in the model tests makes the scoring rules
     * explicit and easy for other developers to find and modify.
     */
    @Nested
    class ScoreTests {

        @Test
        void zeroScoreWhenNoMatch() {
            assertEquals(0, easyState.calculateScore());
        }

        @Test
        void scoreIncrease() {
            for (int i = 0; i < 4; i++) {
                easyState.incrementMatchedPairs();
            }
            assertTrue(easyState.calculateScore() > 0);
        }

        @Test
        // Combo/streak bonus: increasing combo should increase the calculated score
        void comboBonus() {
            for (int i = 0; i < 4; i++) {
                easyState.incrementMatchedPairs();
            }
            for (int i = 0; i < 10; i++) {
                easyState.incrementMoves();
            }
            int base = easyState.calculateScore();

            easyState.incrementCombo();
            easyState.incrementCombo();

            assertTrue(easyState.calculateScore() > base);
        }
 // Test time bonus: higher remaining time should increase the calculated score
        @Test
        @DisplayName("Streak bonus: consecutive combos increase score progressively")
        void streakBonus() {
            for (int i = 0; i < 4; i++) {
                easyState.incrementMatchedPairs();
            }
            for (int i = 0; i < 10; i++) {
                easyState.incrementMoves();
            }

            int base = easyState.calculateScore();

            easyState.incrementCombo();
            int afterOneCombo = easyState.calculateScore();

            easyState.incrementCombo();
            int afterTwoCombos = easyState.calculateScore();

            assertTrue(afterOneCombo > base, "One combo should increase score");
            assertTrue(afterTwoCombos > afterOneCombo, "Additional combo should further increase score");
        }

        @Test
        // Time bonus: higher remaining time increases the calculated score
        void timeBonus() {
            for (int i = 0; i < 4; i++) {
                easyState.incrementMatchedPairs();
            }
            for (int i = 0; i < 10; i++) {
                easyState.incrementMoves();
            }

            easyState.setTimeRemaining(0);
            int noTime = easyState.calculateScore();

            easyState.setTimeRemaining(60);
            int withTime = easyState.calculateScore();

            assertTrue(withTime > noTime);
        }
    }

    // ── RESET ───────────────────────────────────────────

    @Test
    @DisplayName("reset() xóa sạch toàn bộ state")
    void resetShouldClearState() {
        for (int i = 0; i < 5;  i++) easyState.incrementMatchedPairs();
        for (int i = 0; i < 20; i++) easyState.incrementMoves();
        easyState.setTimeRemaining(10);
        easyState.setStatus(GameStatus.PLAYING);
        easyState.incrementCombo();

        easyState.reset();

        assertAll(
                () -> assertEquals(0,               easyState.getMatchedPairs()),
                () -> assertEquals(0,               easyState.getMoves()),
                () -> assertEquals(60,              easyState.getTimeRemaining()),
                () -> assertEquals(GameStatus.IDLE, easyState.getStatus()),
                () -> assertEquals(0,               easyState.getComboCount())
        );
    }
}