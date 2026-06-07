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
                    () -> assertEquals(60, easyState.getTimeRemaining()),
                    () -> assertEquals(0, easyState.getMatchedPairs()),
                    () -> assertEquals(0, easyState.getMoves()),
                    () -> assertEquals(GameStatus.IDLE, easyState.getStatus()),
                    () -> assertEquals(0, easyState.getComboCount()),
                    () -> assertEquals(3, easyState.getHintCount()),
                    () -> assertEquals(0, easyState.getHintPenalty())
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

        @Test
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

    // ── HINT SYSTEM ─────────────────────────────────────

    @Nested
    @DisplayName("Hint System Tests")
    class HintSystemTests {

        @Test
        @DisplayName("Mặc định ban đầu có 3 gợi ý và phạt 0 điểm")
        void hintDefaults() {
            assertEquals(3, easyState.getHintCount());
            assertEquals(0, easyState.getHintPenalty());
        }

        @Test
        @DisplayName("Sử dụng gợi ý -> giảm số lượt gợi ý và tăng điểm phạt")
        void useHintDecrementsCountAndIncreasesPenalty() {
            easyState.useHint();
            assertEquals(2, easyState.getHintCount());
            assertEquals(50, easyState.getHintPenalty());

            easyState.useHint();
            assertEquals(1, easyState.getHintCount());
            assertEquals(100, easyState.getHintPenalty());

            easyState.useHint();
            assertEquals(0, easyState.getHintCount());
            assertEquals(150, easyState.getHintPenalty());
        }

        @Test
        @DisplayName("Sử dụng gợi ý khi đã hết lượt -> không giảm thêm và không phạt thêm")
        void useHintWhenEmpty() {
            easyState.useHint(); // 3 -> 2
            easyState.useHint(); // 2 -> 1
            easyState.useHint(); // 1 -> 0

            easyState.useHint(); // 0 -> 0 (no-op)
            assertEquals(0, easyState.getHintCount());
            assertEquals(150, easyState.getHintPenalty());
        }

        @Test
        @DisplayName("Tính điểm số có trừ điểm phạt của gợi ý")
        void calculateScoreWithHintPenalty() {
            // Thiết lập trạng thái có 1 cặp ghép đúng (100 điểm)
            easyState.incrementMatchedPairs();
            int scoreBeforeHint = easyState.calculateScore();
            assertTrue(scoreBeforeHint > 0);

            // Dùng 1 gợi ý (phạt 50 điểm)
            easyState.useHint();
            int scoreAfterHint = easyState.calculateScore();
            assertEquals(scoreBeforeHint - 50, scoreAfterHint);
        }

        @Test
        @DisplayName("Tìm cặp thẻ chưa khớp đầu tiên trên bàn chơi")
        void testFindFirstUnmatchedPair() {
            // Chuẩn bị 4 thẻ (2 cặp)
            Card c1 = new Card("1", "pairA", CardType.ANIMAL, "🐶", 0);
            Card c2 = new Card("2", "pairB", CardType.ANIMAL, "🐱", 1);
            Card c3 = new Card("3", "pairA", CardType.ANIMAL, "🐶", 2);
            Card c4 = new Card("4", "pairB", CardType.ANIMAL, "🐱", 3);

            Card[] cards = {c1, c2, c3, c4};
            easyState.setCards(cards);

            // Chưa có cặp nào matched -> Phải trả về cặp pairA đầu tiên (c1 và c3)
            Card[] unmatchedPair = easyState.findFirstUnmatchedPair();
            assertNotNull(unmatchedPair);
            assertEquals(2, unmatchedPair.length);
            assertEquals("1", unmatchedPair[0].getId());
            assertEquals("3", unmatchedPair[1].getId());

            // Đánh dấu c1 và c3 đã match
            c1.match();
            c3.match();

            // Cặp tiếp theo chưa matched là pairB (c2 và c4)
            unmatchedPair = easyState.findFirstUnmatchedPair();
            assertNotNull(unmatchedPair);
            assertEquals(2, unmatchedPair.length);
            assertEquals("2", unmatchedPair[0].getId());
            assertEquals("4", unmatchedPair[1].getId());

            // Đánh dấu nốt c2 và c4 đã match
            c2.match();
            c4.match();

            // Không còn cặp nào chưa matched -> Phải trả về null
            assertNull(easyState.findFirstUnmatchedPair());
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
        easyState.useHint(); // Tiêu tốn 1 gợi ý

        easyState.reset();

        assertAll(
                () -> assertEquals(0, easyState.getMatchedPairs()),
                () -> assertEquals(0, easyState.getMoves()),
                () -> assertEquals(60, easyState.getTimeRemaining()),
                () -> assertEquals(GameStatus.IDLE, easyState.getStatus()),
                () -> assertEquals(0, easyState.getComboCount()),
                () -> assertEquals(3, easyState.getHintCount()),
                () -> assertEquals(0, easyState.getHintPenalty())
        );
    }
}