package com.memorycardflip.controller;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * [UC-01: Select Difficulty] [UC-05: Pause Game] [UC-06: Resume Game]
 *
 * Unit tests cho Game Logic - CHỈ TEST BUSINESS LOGIC qua GameState.
 * Không phụ thuộc vào JavaFX UI.
 */
@DisplayName("Game Logic Unit Tests - Select Difficulty, Pause, Resume")
class GameControllerUnitTest {

    private GameState gameState;

    @BeforeEach
    void setUp() {
        gameState = new GameState(Difficulty.EASY);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // [UC-01: SELECT DIFFICULTY] Tests
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[UC-01] Select Difficulty")
    class SelectDifficultyTests {

        @Test
        @DisplayName("EASY: grid 4x4, 8 pairs, 60 seconds")
        void easyDifficultyValues() {
            assertEquals(4, Difficulty.EASY.getGridSize());
            assertEquals(8, Difficulty.EASY.totalPairs());
            assertEquals(60, Difficulty.EASY.getTimeLimit());
            assertEquals(16, Difficulty.EASY.totalCells());
        }

        @Test
        @DisplayName("MEDIUM: grid 6x6, 18 pairs, 90 seconds")
        void mediumDifficultyValues() {
            assertEquals(6, Difficulty.MEDIUM.getGridSize());
            assertEquals(18, Difficulty.MEDIUM.totalPairs());
            assertEquals(90, Difficulty.MEDIUM.getTimeLimit());
            assertEquals(36, Difficulty.MEDIUM.totalCells());
        }

        @Test
        @DisplayName("HARD: grid 8x8, 32 pairs, 120 seconds")
        void hardDifficultyValues() {
            assertEquals(8, Difficulty.HARD.getGridSize());
            assertEquals(32, Difficulty.HARD.totalPairs());
            assertEquals(120, Difficulty.HARD.getTimeLimit());
            assertEquals(64, Difficulty.HARD.totalCells());
        }

        @Test
        @DisplayName("GameState khởi tạo đúng với mỗi độ khó")
        void gameStateInitializesWithCorrectDifficulty() {
            GameState easyState = new GameState(Difficulty.EASY);
            assertEquals(Difficulty.EASY, easyState.getDifficulty());
            assertEquals(60, easyState.getTimeRemaining());

            GameState mediumState = new GameState(Difficulty.MEDIUM);
            assertEquals(Difficulty.MEDIUM, mediumState.getDifficulty());
            assertEquals(90, mediumState.getTimeRemaining());

            GameState hardState = new GameState(Difficulty.HARD);
            assertEquals(Difficulty.HARD, hardState.getDifficulty());
            assertEquals(120, hardState.getTimeRemaining());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // [UC-05: PAUSE GAME] Business Logic Tests
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[UC-05] Pause Game Logic")
    class PauseGameLogicTests {

        @Test
        @DisplayName("Game có thể chuyển từ PLAYING sang PAUSED")
        void gameCanTransitionFromPlayingToPaused() {
            gameState.setStatus(GameStatus.PLAYING);
            assertEquals(GameStatus.PLAYING, gameState.getStatus());

            gameState.setStatus(GameStatus.PAUSED);
            assertEquals(GameStatus.PAUSED, gameState.getStatus());
        }

        @Test
        @DisplayName("Khi PAUSED, không thể chọn thẻ (business rule)")
        void cannotSelectCardWhenPaused() {
            com.example.memorycardflip.model.Card card =
                    new com.example.memorycardflip.model.Card("c1", "p1",
                            com.example.memorycardflip.model.CardType.ANIMAL, "🐶", 0);

            gameState.setStatus(GameStatus.PLAYING);
            assertTrue(gameState.canSelectCard(card));

            gameState.setStatus(GameStatus.PAUSED);
            assertFalse(gameState.canSelectCard(card));
        }

        @Test
        @DisplayName("Pause không ảnh hưởng đến matchedPairs, moves")
        void pauseDoesNotAffectGameStats() {
            gameState.setStatus(GameStatus.PLAYING);

            for (int i = 0; i < 3; i++) {
                gameState.incrementMatchedPairs();
            }
            for (int i = 0; i < 5; i++) {
                gameState.incrementMoves();
            }
            gameState.incrementCombo();

            int matchedBefore = gameState.getMatchedPairs();
            int movesBefore = gameState.getMoves();
            int comboBefore = gameState.getComboCount();

            gameState.setStatus(GameStatus.PAUSED);

            assertEquals(matchedBefore, gameState.getMatchedPairs());
            assertEquals(movesBefore, gameState.getMoves());
            assertEquals(comboBefore, gameState.getComboCount());
        }

        @Test
        @DisplayName("Pause không ảnh hưởng đến timeRemaining")
        void pauseDoesNotAffectTimeRemaining() {
            gameState.setStatus(GameStatus.PLAYING);
            gameState.setTimeRemaining(45);
            int timeBefore = gameState.getTimeRemaining();

            gameState.setStatus(GameStatus.PAUSED);

            assertEquals(timeBefore, gameState.getTimeRemaining());
        }

        @Test
        @DisplayName("Có thể PAUSE khi game đang PLAYING (không có ràng buộc từ GameState)")
        void canPauseWhenGameIsPlaying() {
            gameState.setStatus(GameStatus.PLAYING);
            gameState.setStatus(GameStatus.PAUSED);
            assertEquals(GameStatus.PAUSED, gameState.getStatus());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // [UC-06: RESUME GAME] Business Logic Tests
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[UC-06] Resume Game Logic")
    class ResumeGameLogicTests {

        @Test
        @DisplayName("Game có thể chuyển từ PAUSED sang PLAYING")
        void gameCanTransitionFromPausedToPlaying() {
            gameState.setStatus(GameStatus.PAUSED);
            assertEquals(GameStatus.PAUSED, gameState.getStatus());

            gameState.setStatus(GameStatus.PLAYING);
            assertEquals(GameStatus.PLAYING, gameState.getStatus());
        }

        @Test
        @DisplayName("Khi resume, có thể chọn thẻ trở lại")
        void canSelectCardAfterResume() {
            com.example.memorycardflip.model.Card card =
                    new com.example.memorycardflip.model.Card("c1", "p1",
                            com.example.memorycardflip.model.CardType.ANIMAL, "🐶", 0);

            gameState.setStatus(GameStatus.PAUSED);
            assertFalse(gameState.canSelectCard(card));

            gameState.setStatus(GameStatus.PLAYING);
            assertTrue(gameState.canSelectCard(card));
        }

        @Test
        @DisplayName("Resume giữ nguyên toàn bộ stats")
        void resumePreservesAllStats() {
            gameState.setStatus(GameStatus.PLAYING);

            for (int i = 0; i < 3; i++) {
                gameState.incrementMatchedPairs();
            }
            for (int i = 0; i < 5; i++) {
                gameState.incrementMoves();
            }
            gameState.setTimeRemaining(30);
            gameState.incrementCombo();

            int matchedBefore = gameState.getMatchedPairs();
            int movesBefore = gameState.getMoves();
            int timeBefore = gameState.getTimeRemaining();
            int comboBefore = gameState.getComboCount();

            gameState.setStatus(GameStatus.PAUSED);
            gameState.setStatus(GameStatus.PLAYING);

            assertEquals(matchedBefore, gameState.getMatchedPairs());
            assertEquals(movesBefore, gameState.getMoves());
            assertEquals(timeBefore, gameState.getTimeRemaining());
            assertEquals(comboBefore, gameState.getComboCount());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Cross-Use Case Tests
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Cross Use Case: Pause ↔ Resume Cycle")
    class PauseResumeCycleTests {

        @Test
        @DisplayName("PLAYING → PAUSED → PLAYING hoạt động đúng")
        void playingToPausedToPlayingWorks() {
            gameState.setStatus(GameStatus.PLAYING);
            assertEquals(GameStatus.PLAYING, gameState.getStatus());

            gameState.setStatus(GameStatus.PAUSED);
            assertEquals(GameStatus.PAUSED, gameState.getStatus());

            gameState.setStatus(GameStatus.PLAYING);
            assertEquals(GameStatus.PLAYING, gameState.getStatus());
        }

        @Test
        @DisplayName("Nhiều lần pause/resume liên tiếp")
        void multiplePauseResumeCycles() {
            for (int i = 0; i < 5; i++) {
                gameState.setStatus(GameStatus.PLAYING);
                assertEquals(GameStatus.PLAYING, gameState.getStatus());

                gameState.setStatus(GameStatus.PAUSED);
                assertEquals(GameStatus.PAUSED, gameState.getStatus());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GameState Core Tests
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GameState Core Functionality")
    class GameStateCoreTests {

        @Test
        @DisplayName("reset() đưa về trạng thái ban đầu")
        void resetReturnsToInitialState() {
            for (int i = 0; i < 5; i++) {
                gameState.incrementMatchedPairs();
            }
            for (int i = 0; i < 10; i++) {
                gameState.incrementMoves();
            }
            gameState.setTimeRemaining(10);
            gameState.setStatus(GameStatus.PLAYING);
            gameState.incrementCombo();

            gameState.reset();

            assertEquals(0, gameState.getMatchedPairs());
            assertEquals(0, gameState.getMoves());
            assertEquals(60, gameState.getTimeRemaining());
            assertEquals(GameStatus.IDLE, gameState.getStatus());
            assertEquals(0, gameState.getComboCount());
        }

        @Test
        @DisplayName("calculateScore() - 3 matched pairs = base 300 + time bonus")
        void calculateScoreBasic() {
            for (int i = 0; i < 3; i++) {
                gameState.incrementMatchedPairs();
            }
            // timeRemaining default = 60, bonus = min(60,30)*3 = 90
            // base = 300, total = 390
            int score = gameState.calculateScore();
            assertEquals(390, score);
        }

        @Test
        @DisplayName("calculateScore() với combo bonus")
        void calculateScoreWithComboBonus() {
            for (int i = 0; i < 3; i++) {
                gameState.incrementMatchedPairs();
            }
            gameState.incrementCombo();
            gameState.incrementCombo(); // combo = 2, bonus = 40

            // base 300 + combo 40 + time bonus (60 -> 90) = 430
            int score = gameState.calculateScore();
            assertEquals(430, score);
        }

        @Test
        @DisplayName("calculateScore() với move penalty")
        void calculateScoreWithMovePenalty() {
            for (int i = 0; i < 3; i++) {
                gameState.incrementMatchedPairs();
            }
            for (int i = 0; i < 10; i++) {
                gameState.incrementMoves();
            }
            // base 300 - penalty 50 + time bonus 90 = 340
            int score = gameState.calculateScore();
            assertEquals(340, score);
        }

        @Test
        @DisplayName("canSelectCard() kiểm tra đúng điều kiện")
        void canSelectCardChecksConditions() {
            com.example.memorycardflip.model.Card card =
                    new com.example.memorycardflip.model.Card("c1", "p1",
                            com.example.memorycardflip.model.CardType.ANIMAL, "🐶", 0);

            gameState.setStatus(GameStatus.IDLE);
            assertFalse(gameState.canSelectCard(card));

            gameState.setStatus(GameStatus.PLAYING);
            assertTrue(gameState.canSelectCard(card));

            gameState.setStatus(GameStatus.PAUSED);
            assertFalse(gameState.canSelectCard(card));
        }

        @Test
        @DisplayName("decrementTime() giảm timeRemaining")
        void decrementTimeDecreasesTime() {
            gameState.setTimeRemaining(30);
            gameState.decrementTime();
            assertEquals(29, gameState.getTimeRemaining());
        }

        @Test
        @DisplayName("decrementTime() không xuống dưới 0")
        void decrementTimeDoesNotGoBelowZero() {
            gameState.setTimeRemaining(0);
            gameState.decrementTime();
            assertEquals(0, gameState.getTimeRemaining());
        }

        @Test
        @DisplayName("isTimeUp() trả về true khi timeRemaining <= 0")
        void isTimeUpReturnsTrueWhenTimeIsZero() {
            assertFalse(gameState.isTimeUp());
            gameState.setTimeRemaining(0);
            assertTrue(gameState.isTimeUp());
        }

        @Test
        @DisplayName("incrementCombo() và resetCombo() hoạt động đúng")
        void comboIncrementAndReset() {
            assertEquals(0, gameState.getComboCount());
            gameState.incrementCombo();
            assertEquals(1, gameState.getComboCount());
            gameState.incrementCombo();
            assertEquals(2, gameState.getComboCount());
            gameState.resetCombo();
            assertEquals(0, gameState.getComboCount());
        }
    }
}