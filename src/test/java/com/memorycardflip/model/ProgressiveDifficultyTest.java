package com.memorycardflip.model;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * [UC-04 - Nâng cấp Play Again: Progressive Difficulty]
 *
 * Unit tests kiểm tra logic tăng độ khó tự động:
 * - EASY → MEDIUM → HARD → null (đã max)
 * - Nút "Lên Level" chỉ hiện khi thắng
 * - Thua hoặc HARD → không tăng độ khó
 * - GameState mới được tạo đúng với difficulty tiếp theo
 */
@DisplayName("UC-04 Progressive Difficulty Tests")
class ProgressiveDifficultyTest {

    // ── Helper: mô phỏng getNextDifficulty() của SceneManager ────

    private Difficulty getNextDifficulty(Difficulty current) {
        return switch (current) {
            case EASY   -> Difficulty.MEDIUM;
            case MEDIUM -> Difficulty.HARD;
            case HARD   -> null;
        };
    }

    // ── Chuỗi tăng độ khó ────────────────────────────────────────

    @Nested
    @DisplayName("Chuỗi tăng độ khó EASY → MEDIUM → HARD → null")
    class DifficultyProgressionChainTests {

        @Test
        @DisplayName("EASY → MEDIUM")
        void easyToMedium() {
            assertEquals(Difficulty.MEDIUM, getNextDifficulty(Difficulty.EASY));
        }

        @Test
        @DisplayName("MEDIUM → HARD")
        void mediumToHard() {
            assertEquals(Difficulty.HARD, getNextDifficulty(Difficulty.MEDIUM));
        }

        @Test
        @DisplayName("HARD → null (đã ở mức cao nhất)")
        void hardToNull() {
            assertNull(getNextDifficulty(Difficulty.HARD),
                    "HARD không có độ khó tiếp theo");
        }

        @Test
        @DisplayName("Chuỗi đầy đủ: EASY → MEDIUM → HARD → null")
        void fullProgression() {
            Difficulty current = Difficulty.EASY;
            current = getNextDifficulty(current);
            assertEquals(Difficulty.MEDIUM, current);

            current = getNextDifficulty(current);
            assertEquals(Difficulty.HARD, current);

            current = getNextDifficulty(current);
            assertNull(current, "Sau HARD không còn cấp độ nào");
        }
    }

    // ── Điều kiện hiển thị nút "Lên Level" ───────────────────────

    @Nested
    @DisplayName("Điều kiện hiển thị nút 'Lên Level'")
    class NextLevelButtonVisibilityTests {

        @Test
        @DisplayName("Thắng + EASY → hiện nút Lên Level (MEDIUM)")
        void showButtonWhenWinEasy() {
            GameState state = new GameState(Difficulty.EASY);
            state.setStatus(GameStatus.WON);

            boolean won = state.getStatus() == GameStatus.WON;
            Difficulty next = getNextDifficulty(state.getDifficulty());

            assertTrue(won && next != null,
                    "Phải hiển thị nút khi thắng EASY");
            assertEquals(Difficulty.MEDIUM, next);
        }

        @Test
        @DisplayName("Thắng + MEDIUM → hiện nút Lên Level (HARD)")
        void showButtonWhenWinMedium() {
            GameState state = new GameState(Difficulty.MEDIUM);
            state.setStatus(GameStatus.WON);

            boolean won = state.getStatus() == GameStatus.WON;
            Difficulty next = getNextDifficulty(state.getDifficulty());

            assertTrue(won && next != null,
                    "Phải hiển thị nút khi thắng MEDIUM");
            assertEquals(Difficulty.HARD, next);
        }

        @Test
        @DisplayName("Thắng + HARD → ẩn nút (đã max)")
        void hideButtonWhenWinHard() {
            GameState state = new GameState(Difficulty.HARD);
            state.setStatus(GameStatus.WON);

            Difficulty next = getNextDifficulty(state.getDifficulty());

            assertNull(next, "Không hiển thị nút khi đã HARD");
        }

        @Test
        @DisplayName("Thua + EASY → ẩn nút")
        void hideButtonWhenLoseEasy() {
            GameState state = new GameState(Difficulty.EASY);
            state.setStatus(GameStatus.LOST);

            boolean won = state.getStatus() == GameStatus.WON;

            assertFalse(won, "Không hiển thị nút khi thua");
        }

        @Test
        @DisplayName("Thua + MEDIUM → ẩn nút dù có cấp độ tiếp theo")
        void hideButtonWhenLoseMedium() {
            GameState state = new GameState(Difficulty.MEDIUM);
            state.setStatus(GameStatus.LOST);

            boolean won = state.getStatus() == GameStatus.WON;
            Difficulty next = getNextDifficulty(state.getDifficulty());

            // Dù next != null nhưng phải ẩn vì thua
            assertFalse(won, "Phải ẩn nút khi thua dù có next level");
            assertNotNull(next, "MEDIUM có next level nhưng không hiển thị vì thua");
        }
    }

    // ── GameState mới với difficulty tiếp theo ────────────────────

    @Nested
    @DisplayName("Khởi tạo GameState với difficulty tiếp theo")
    class NewGameStateWithNextDifficultyTests {

        @Test
        @DisplayName("GameState mới với MEDIUM có đúng config")
        void newMediumGameState() {
            Difficulty next = getNextDifficulty(Difficulty.EASY);
            assertNotNull(next);

            GameState newState = new GameState(next);

            assertAll(
                    () -> assertEquals(Difficulty.MEDIUM, newState.getDifficulty()),
                    () -> assertEquals(90, newState.getTimeRemaining()),
                    () -> assertEquals(6,  newState.getDifficulty().getGridSize()),
                    () -> assertEquals(18, newState.getDifficulty().totalPairs()),
                    () -> assertEquals(0,  newState.getMatchedPairs()),
                    () -> assertEquals(0,  newState.getMoves()),
                    () -> assertEquals(GameStatus.IDLE, newState.getStatus())
            );
        }

        @Test
        @DisplayName("GameState mới với HARD có đúng config")
        void newHardGameState() {
            Difficulty next = getNextDifficulty(Difficulty.MEDIUM);
            assertNotNull(next);

            GameState newState = new GameState(next);

            assertAll(
                    () -> assertEquals(Difficulty.HARD, newState.getDifficulty()),
                    () -> assertEquals(120, newState.getTimeRemaining()),
                    () -> assertEquals(8,   newState.getDifficulty().getGridSize()),
                    () -> assertEquals(32,  newState.getDifficulty().totalPairs()),
                    () -> assertEquals(0,   newState.getMatchedPairs())
            );
        }

        @Test
        @DisplayName("GameState mới reset toàn bộ stats từ ván cũ")
        void newGameStateHasFreshStats() {
            // Giả lập ván cũ đã chơi xong
            GameState oldState = new GameState(Difficulty.EASY);
            oldState.setStatus(GameStatus.WON);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) oldState.incrementMatchedPairs();
            for (int i = 0; i < 20; i++) oldState.incrementMoves();
            oldState.incrementCombo();

            // Tạo GameState mới cho ván tiếp theo
            Difficulty next = getNextDifficulty(oldState.getDifficulty());
            GameState newState = new GameState(next);

            assertAll(
                    () -> assertEquals(0, newState.getMatchedPairs(), "matchedPairs phải = 0"),
                    () -> assertEquals(0, newState.getMoves(),         "moves phải = 0"),
                    () -> assertEquals(0, newState.getComboCount(),    "combo phải = 0"),
                    () -> assertEquals(GameStatus.IDLE, newState.getStatus())
            );
        }
    }

    // ── Replay cùng độ khó ────────────────────────────────────────

    @Nested
    @DisplayName("Chơi lại cùng độ khó (Play Again thường)")
    class ReplayWithSameDifficultyTests {

        @Test
        @DisplayName("Replay EASY → tạo GameState EASY mới")
        void replaySameDifficultyEasy() {
            GameState current = new GameState(Difficulty.EASY);
            current.setStatus(GameStatus.WON);

            // Replay cùng độ khó (không tăng)
            GameState replayed = new GameState(current.getDifficulty());

            assertEquals(Difficulty.EASY, replayed.getDifficulty());
            assertEquals(0, replayed.getMatchedPairs());
            assertEquals(GameStatus.IDLE, replayed.getStatus());
        }

        @Test
        @DisplayName("Replay HARD → tạo GameState HARD mới (không lên cao hơn)")
        void replaySameDifficultyHard() {
            GameState current = new GameState(Difficulty.HARD);
            current.setStatus(GameStatus.LOST);

            GameState replayed = new GameState(current.getDifficulty());

            assertEquals(Difficulty.HARD, replayed.getDifficulty());
            assertEquals(120, replayed.getTimeRemaining());
        }
    }

    // ── Difficulty config không đổi ───────────────────────────────

    @Test
    @DisplayName("Difficulty giữ nguyên displayName đúng")
    void difficultyDisplayNames() {
        assertAll(
                () -> assertEquals("Dễ",  Difficulty.EASY.getDisplayName()),
                () -> assertEquals("Vừa", Difficulty.MEDIUM.getDisplayName()),
                () -> assertEquals("Khó", Difficulty.HARD.getDisplayName())
        );
    }

    @Test
    @DisplayName("totalPairs tăng theo độ khó")
    void totalPairsIncreaseWithDifficulty() {
        assertTrue(Difficulty.EASY.totalPairs() < Difficulty.MEDIUM.totalPairs(),
                "MEDIUM phải có nhiều cặp hơn EASY");
        assertTrue(Difficulty.MEDIUM.totalPairs() < Difficulty.HARD.totalPairs(),
                "HARD phải có nhiều cặp hơn MEDIUM");
    }
}
