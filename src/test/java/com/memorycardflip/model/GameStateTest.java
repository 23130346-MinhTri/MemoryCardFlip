package com.memorycardflip.model;

import com.example.memorycardflip.model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho class GameState.
 */
@DisplayName("GameState Model Tests")
class GameStateTest {

    private GameState easyState;
    private GameState hardState;

    @BeforeEach
    void setUp() {
        easyState = new GameState(Difficulty.EASY);
        hardState = new GameState(Difficulty.HARD);
    }

    // ── Khởi tạo ─────────────────────────────────────────────

    @Nested
    @DisplayName("Khởi tạo")
    class InitTests {

        @Test
        @DisplayName("EASY: timeRemaining = 60, totalPairs = 8")
        void easyStateShouldHaveCorrectDefaults() {
            assertAll(
                    () -> assertEquals(Difficulty.EASY, easyState.getDifficulty()),
                    () -> assertEquals(60, easyState.getTimeRemaining()),
                    () -> assertEquals(0,  easyState.getMatchedPairs()),
                    () -> assertEquals(0,  easyState.getMoves()),
                    () -> assertEquals(GameStatus.IDLE, easyState.getStatus()),
                    () -> assertEquals(0,  easyState.getComboCount())
            );
        }

        @Test
        @DisplayName("HARD: timeRemaining = 120, totalPairs = 32")
        void hardStateShouldHaveCorrectTimeLimit() {
            assertEquals(120, hardState.getTimeRemaining());
        }
    }

    // ── FlippedCards ──────────────────────────────────────────

    @Nested
    @DisplayName("FlippedCards management")
    class FlippedCardsTests {

        @Test
        @DisplayName("Thêm 2 thẻ → hasTwoFlipped() = true")
        void shouldHaveTwoFlippedAfterAddingTwo() {
            Card c1 = new Card("c1","p1", CardType.ANIMAL,"🐶",0);
            Card c2 = new Card("c2","p1", CardType.ANIMAL,"🐶",1);
            easyState.addFlippedCard(c1);
            easyState.addFlippedCard(c2);
            assertTrue(easyState.hasTwoFlipped());
        }

        @Test
        @DisplayName("Không thêm cùng thẻ 2 lần")
        void shouldNotAddDuplicateCard() {
            Card c1 = new Card("c1","p1", CardType.ANIMAL,"🐶",0);
            easyState.addFlippedCard(c1);
            easyState.addFlippedCard(c1);
            assertEquals(1, easyState.getFlippedCards().size());
        }

        @Test
        @DisplayName("Không thêm quá 2 thẻ")
        void shouldNotAddMoreThanTwoCards() {
            for (int i = 0; i < 5; i++) {
                easyState.addFlippedCard(
                        new Card("c"+i,"p"+i, CardType.ANIMAL,"🐶",i));
            }
            assertEquals(2, easyState.getFlippedCards().size());
        }

        @Test
        @DisplayName("clearFlippedCards() → danh sách rỗng")
        void shouldClearFlippedCards() {
            easyState.addFlippedCard(new Card("c1","p1",CardType.ANIMAL,"🐶",0));
            easyState.clearFlippedCards();
            assertTrue(easyState.getFlippedCards().isEmpty());
        }
    }

    // ── isComplete() ─────────────────────────────────────────

    @Nested
    @DisplayName("isComplete()")
    class IsCompleteTests {

        @Test
        @DisplayName("Mới khởi tạo → chưa hoàn thành")
        void shouldNotBeCompleteInitially() {
            assertFalse(easyState.isComplete());
        }

        @Test
        @DisplayName("Ghép đủ 8 cặp EASY → hoàn thành")
        void shouldBeCompleteWhenAllPairsMatched() {
            easyState.setMatchedPairs(Difficulty.EASY.totalPairs()); // 8
            assertTrue(easyState.isComplete());
        }
    }

    // ── isTimeUp() ────────────────────────────────────────────

    @Nested
    @DisplayName("isTimeUp()")
    class TimeUpTests {

        @Test
        @DisplayName("timeRemaining > 0 → chưa hết giờ")
        void shouldNotBeTimeUpWithTimeRemaining() {
            assertFalse(easyState.isTimeUp());
        }

        @Test
        @DisplayName("timeRemaining = 0 → hết giờ")
        void shouldBeTimeUpWhenZero() {
            easyState.setTimeRemaining(0);
            assertTrue(easyState.isTimeUp());
        }

        @Test
        @DisplayName("decrementTime() giảm đúng 1 giây")
        void shouldDecrementByOne() {
            int before = easyState.getTimeRemaining();
            easyState.decrementTime();
            assertEquals(before - 1, easyState.getTimeRemaining());
        }

        @Test
        @DisplayName("decrementTime() không xuống dưới 0")
        void shouldNotDecrementBelowZero() {
            easyState.setTimeRemaining(0);
            easyState.decrementTime();
            assertEquals(0, easyState.getTimeRemaining());
        }
    }

    // ── calculateScore() ─────────────────────────────────────

    @Nested
    @DisplayName("calculateScore()")
    class ScoreTests {

        @Test
        @DisplayName("Score = 0 khi chưa ghép cặp nào")
        void scoreShouldBeZeroWithNoPairs() {
            assertEquals(0, easyState.calculateScore());
        }

        @Test
        @DisplayName("Score tăng khi ghép cặp")
        void scoreShouldIncreaseWithMatches() {
            easyState.setMatchedPairs(4);
            assertTrue(easyState.calculateScore() > 0);
        }

        @Test
        @DisplayName("Score không âm dù nhiều moves")
        void scoreShouldNeverBeNegative() {
            easyState.setMatchedPairs(1);
            easyState.setMoves(9999);
            assertTrue(easyState.calculateScore() >= 0);
        }

        @Test
        @DisplayName("Combo tăng điểm")
        void comboShouldIncreaseScore() {
            easyState.setMatchedPairs(4);
            easyState.setMoves(10);
            int scoreNoCombo = easyState.calculateScore();

            easyState.incrementCombo();
            easyState.incrementCombo();
            int scoreWithCombo = easyState.calculateScore();

            assertTrue(scoreWithCombo > scoreNoCombo);
        }

        @Test
        @DisplayName("Time bonus khi còn nhiều thời gian")
        void timeBonusShouldIncreaseScore() {
            easyState.setMatchedPairs(4);
            easyState.setMoves(10);
            easyState.setTimeRemaining(0);
            int scoreNoTime = easyState.calculateScore();

            easyState.setTimeRemaining(60);
            int scoreWithTime = easyState.calculateScore();

            assertTrue(scoreWithTime > scoreNoTime);
        }
    }

    // ── Combo ────────────────────────────────────────────────

    @Nested
    @DisplayName("Combo counter")
    class ComboTests {

        @Test
        @DisplayName("incrementCombo() tăng đúng")
        void shouldIncrementCombo() {
            easyState.incrementCombo();
            easyState.incrementCombo();
            assertEquals(2, easyState.getComboCount());
        }

        @Test
        @DisplayName("resetCombo() về 0")
        void shouldResetCombo() {
            easyState.incrementCombo();
            easyState.resetCombo();
            assertEquals(0, easyState.getComboCount());
        }
    }

    // ── reset() ───────────────────────────────────────────────

    @Nested
    @DisplayName("reset()")
    class ResetTests {

        @Test
        @DisplayName("reset() khôi phục toàn bộ về trạng thái ban đầu")
        void shouldResetAllFieldsToInitial() {
            // Setup state đã chơi
            easyState.setMatchedPairs(5);
            easyState.setMoves(20);
            easyState.setTimeRemaining(10);
            easyState.setStatus(GameStatus.PLAYING);
            easyState.incrementCombo();
            easyState.addFlippedCard(new Card("c1","p1",CardType.ANIMAL,"🐶",0));

            easyState.reset();

            assertAll(
                    () -> assertEquals(0, easyState.getMatchedPairs()),
                    () -> assertEquals(0, easyState.getMoves()),
                    () -> assertEquals(60, easyState.getTimeRemaining()),
                    () -> assertEquals(GameStatus.IDLE, easyState.getStatus()),
                    () -> assertEquals(0, easyState.getComboCount()),
                    () -> assertTrue(easyState.getFlippedCards().isEmpty())
            );
        }
    }

    // ── JavaFX Properties binding ─────────────────────────────

    @Nested
    @DisplayName("JavaFX Properties")
    class PropertyTests {

        @Test
        @DisplayName("matchedPairsProperty() phản ánh đúng giá trị")
        void matchedPairsPropertyShouldReflectValue() {
            easyState.setMatchedPairs(3);
            assertEquals(3, easyState.matchedPairsProperty().get());
        }

        @Test
        @DisplayName("statusProperty() phản ánh đúng trạng thái")
        void statusPropertyShouldReflectValue() {
            easyState.setStatus(GameStatus.PLAYING);
            assertEquals(GameStatus.PLAYING, easyState.statusProperty().get());
        }

        @Test
        @DisplayName("timeRemainingProperty() phản ánh đúng")
        void timeRemainingPropertyShouldReflectValue() {
            easyState.setTimeRemaining(45);
            assertEquals(45, easyState.timeRemainingProperty().get());
        }
    }
}