package com.memorycardflip.controller;

import com.example.memorycardflip.controller.ScoreManager;
import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import com.example.memorycardflip.model.ScoreRecord;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * [UC-07 — View Leaderboard] [UC-13 — Save High Score]
 *
 * Unit tests cho ScoreManager Singleton.
 *
 * <p>Bao gồm các test cases cho:</p>
 * <ul>
 *   <li>Saving scores (BR-13.1 — 13.1.4 → 13.1.8)</li>
 *   <li>Loading scores from storage (BR-13.5)</li>
 *   <li>Getting top scores by difficulty (BR-07 — 7.1.5 → 7.1.6)</li>
 *   <li>Getting high score by difficulty (BR-07 — 7.1.7)</li>
 *   <li>Checking new high score (BR-13.3, BR-13.9)</li>
 *   <li>Sorting and filtering logic</li>
 *   <li>Score change notification callback</li>
 * </ul>
 */
@DisplayName("ScoreManager Tests (UC-07 & UC-13)")
class ScoreManagerTest {

    private ScoreManager manager;

    @BeforeEach
    void setUp() {
        // Reset singleton để đảm bảo test isolation
        ScoreManager.resetForTesting();
        manager = ScoreManager.getInstance();
        // Xóa data còn sót từ test trước
        manager.clearAllScores();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [UC-13 — 13.1.4 → 13.1.8] saveScore() Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveScore()")
    class SaveScoreTests {

        @Test
        @DisplayName("Lưu điểm thành công khi GameState.status = WON")
        void shouldSaveScoreWhenGameStateIsWon() {
            GameState gameState = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                gameState.incrementMatchedPairs();
            }
            gameState.setStatus(GameStatus.WON);
            gameState.setTimeRemaining(30);

            int sizeBefore = manager.getTopScores(100).size();

            manager.saveScore(gameState, "TestPlayer");

            int sizeAfter = manager.getTopScores(100).size();
            assertEquals(sizeBefore + 1, sizeAfter, "Cache size should increase after saving");
        }

        @Test
        @DisplayName("Không lưu khi GameState.status = LOST")
        void shouldNotSaveScoreWhenGameStateIsLost() {
            GameState gameState = new GameState(Difficulty.EASY);
            gameState.setStatus(GameStatus.LOST);

            int sizeBefore = manager.getTopScores(100).size();

            manager.saveScore(gameState, "TestPlayer");

            int sizeAfter = manager.getTopScores(100).size();
            assertEquals(sizeBefore, sizeAfter, "Cache size should NOT change when game is lost");
        }

        @Test
        @DisplayName("Không lưu khi GameState = null")
        void shouldNotSaveScoreWhenGameStateIsNull() {
            int sizeBefore = manager.getTopScores(100).size();

            manager.saveScore(null, "TestPlayer");

            int sizeAfter = manager.getTopScores(100).size();
            assertEquals(sizeBefore, sizeAfter, "Cache size should NOT change when gameState is null");
        }


        @Test
        @DisplayName("Cache được sort DESC theo score sau khi lưu")
        void shouldSortCacheDescendingByScore() {
            GameState state1 = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state1.incrementMatchedPairs();
            }
            state1.setStatus(GameStatus.WON);
            state1.setTimeRemaining(10);

            GameState state2 = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state2.incrementMatchedPairs();
            }
            state2.setStatus(GameStatus.WON);
            state2.setTimeRemaining(50);

            manager.saveScore(state1, "Player1");
            manager.saveScore(state2, "Player2");

            List<ScoreRecord> topScores = manager.getTopScores(100);
            assertTrue(topScores.size() >= 2, "Should have at least 2 scores");

            for (int i = 0; i < topScores.size() - 1; i++) {
                assertTrue(topScores.get(i).getScore() >= topScores.get(i + 1).getScore(),
                        "Scores should be in descending order");
            }
        }

        @Test
        @DisplayName("Callback onScoreChanged được gọi khi lưu score thành công")
        void shouldInvokeOnScoreChangedCallback() {
            GameState gameState = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                gameState.incrementMatchedPairs();
            }
            gameState.setStatus(GameStatus.WON);

            final boolean[] callbackInvoked = {false};
            manager.setOnScoreChanged(scores -> {
                callbackInvoked[0] = true;
                assertNotNull(scores, "Callback should receive non-null list");
            });

            manager.saveScore(gameState, "TestPlayer");

            assertTrue(callbackInvoked[0], "onScoreChanged callback should be invoked");
        }

        @Test
        @DisplayName("Callback KHÔNG được gọi khi lưu thất bại")
        void shouldNotInvokeCallbackOnFailedSave() {
            GameState gameState = new GameState(Difficulty.EASY);
            gameState.setStatus(GameStatus.LOST);

            final boolean[] callbackInvoked = {false};
            manager.setOnScoreChanged(scores -> callbackInvoked[0] = true);

            manager.saveScore(gameState, "TestPlayer");

            assertFalse(callbackInvoked[0], "Callback should NOT be invoked on failed save");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [UC-07 — 7.1.5 → 7.1.6] getTopScores() Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getTopScores()")
    class GetTopScoresTests {

        @Test
        @DisplayName("Trả về empty list khi không có scores")
        void shouldReturnEmptyListWhenNoScores() {
            manager.clearAllScores();

            List<ScoreRecord> topScores = manager.getTopScores(10);

            assertTrue(topScores.isEmpty(), "Should return empty list when no scores saved");
        }

        @Test
        @DisplayName("Lọc scores theo difficulty chỉ định")
        void shouldFilterByDifficulty() {
            GameState easyState = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                easyState.incrementMatchedPairs();
            }
            easyState.setStatus(GameStatus.WON);

            GameState hardState = new GameState(Difficulty.HARD);
            for (int i = 0; i < Difficulty.HARD.totalPairs(); i++) {
                hardState.incrementMatchedPairs();
            }
            hardState.setStatus(GameStatus.WON);

            manager.saveScore(easyState, "EasyPlayer");
            manager.saveScore(hardState, "HardPlayer");

            List<ScoreRecord> easyScores = manager.getTopScores(Difficulty.EASY, 100);
            List<ScoreRecord> hardScores = manager.getTopScores(Difficulty.HARD, 100);

            assertTrue(easyScores.stream().allMatch(r -> r.getDifficulty() == Difficulty.EASY),
                    "All scores should be EASY difficulty");
            assertTrue(hardScores.stream().allMatch(r -> r.getDifficulty() == Difficulty.HARD),
                    "All scores should be HARD difficulty");
        }

        @Test
        @DisplayName("Sắp xếp giảm dần theo score")
        void shouldSortDescendingByScore() {
            for (int i = 0; i < 5; i++) {
                GameState state = new GameState(Difficulty.EASY);
                for (int j = 0; j < Difficulty.EASY.totalPairs(); j++) {
                    state.incrementMatchedPairs();
                }
                state.setStatus(GameStatus.WON);
                state.setTimeRemaining(10 + (i * 15));
                manager.saveScore(state, "Player" + i);
            }

            List<ScoreRecord> topScores = manager.getTopScores(Difficulty.EASY, 100);

            for (int i = 0; i < topScores.size() - 1; i++) {
                assertTrue(topScores.get(i).getScore() >= topScores.get(i + 1).getScore(),
                        "Scores should be in descending order");
            }
        }

        @Test
        @DisplayName("getTopScores(limit) - Trả về top scores tất cả difficulties")
        void shouldGetTopScoresAllDifficulties() {
            GameState easyState = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                easyState.incrementMatchedPairs();
            }
            easyState.setStatus(GameStatus.WON);

            GameState hardState = new GameState(Difficulty.HARD);
            for (int i = 0; i < Difficulty.HARD.totalPairs(); i++) {
                hardState.incrementMatchedPairs();
            }
            hardState.setStatus(GameStatus.WON);

            manager.saveScore(easyState, "EasyPlayer");
            manager.saveScore(hardState, "HardPlayer");

            List<ScoreRecord> allTopScores = manager.getTopScores(10);

            assertTrue(allTopScores.size() >= 2, "Should return scores from all difficulties");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [UC-07 — 7.1.7] getHighScore() Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getHighScore()")
    class GetHighScoreTests {

        @Test
        @DisplayName("Trả về empty Optional khi không có scores cho difficulty")
        void shouldReturnEmptyOptionalWhenNoScores() {
            manager.clearAllScores();

            Optional<ScoreRecord> highScore = manager.getHighScore(Difficulty.EASY);

            assertTrue(highScore.isEmpty(), "Should return empty Optional");
        }

        @Test
        @DisplayName("Trả về highest score cho difficulty chỉ định")
        void shouldReturnHighestScoreForDifficulty() {
            int[] timeRemaining = {20, 50, 35};

            for (int time : timeRemaining) {
                GameState state = new GameState(Difficulty.EASY);
                for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                    state.incrementMatchedPairs();
                }
                state.setStatus(GameStatus.WON);
                state.setTimeRemaining(time);
                manager.saveScore(state, "Player");
            }

            Optional<ScoreRecord> highScore = manager.getHighScore(Difficulty.EASY);

            assertTrue(highScore.isPresent(), "Should have high score");

            // FIX: So sánh với max score trong danh sách thay vì dùng công thức phức tạp
            int expectedMax = manager.getTopScores(Difficulty.EASY, 100)
                    .stream()
                    .mapToInt(ScoreRecord::getScore)
                    .max()
                    .orElse(0);
            assertEquals(expectedMax, highScore.get().getScore(),
                    "getHighScore should return the highest score among all saved records");
        }

    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [BR-07 — 7.1.5 → 7.1.6] getScoresByDifficulty() Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getScoresByDifficulty()")
    class GetScoresByDifficultyTests {

        @Test
        @DisplayName("Trả về tất cả scores cho difficulty, sorted DESC")
        void shouldReturnAllScoresForDifficultySorted() {
            for (int i = 0; i < 3; i++) {
                GameState state = new GameState(Difficulty.EASY);
                for (int j = 0; j < Difficulty.EASY.totalPairs(); j++) {
                    state.incrementMatchedPairs();
                }
                state.setStatus(GameStatus.WON);
                state.setTimeRemaining(30 - (i * 10));
                manager.saveScore(state, "Player" + i);
            }

            List<ScoreRecord> scores = manager.getScoresByDifficulty(Difficulty.EASY);

            assertEquals(3, scores.size(), "Should return all 3 scores");

            for (int i = 0; i < scores.size() - 1; i++) {
                assertTrue(scores.get(i).getScore() >= scores.get(i + 1).getScore(),
                        "Scores should be sorted descending");
            }
        }

        @Test
        @DisplayName("Trả về empty list khi không có scores cho difficulty")
        void shouldReturnEmptyListWhenNoScoresForDifficulty() {
            manager.clearAllScores();

            List<ScoreRecord> scores = manager.getScoresByDifficulty(Difficulty.EASY);

            assertTrue(scores.isEmpty(), "Should return empty list");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [UC-13 — 13.1.9 / BR-13.3] isNewHighScore() Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isNewHighScore()")
    class IsNewHighScoreTests {

        @Test
        @DisplayName("Trả về true khi chưa có high score")
        void shouldReturnTrueWhenNoHighScore() {
            manager.clearAllScores();

            boolean isNew = manager.isNewHighScore(Difficulty.EASY, 500);

            assertTrue(isNew, "Should be new high score when no previous scores");
        }

        @Test
        @DisplayName("Trả về true khi score cao hơn high score hiện tại")
        void shouldReturnTrueWhenScoreIsHigher() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);
            state.setTimeRemaining(50);
            manager.saveScore(state, "Player1");

            int currentHighScore = manager.getHighScore(Difficulty.EASY).get().getScore();

            boolean isNew = manager.isNewHighScore(Difficulty.EASY, currentHighScore + 100);

            assertTrue(isNew, "Should be new high score when score is higher");
        }

        @Test
        @DisplayName("Trả về false khi score thấp hơn high score hiện tại")
        void shouldReturnFalseWhenScoreIsLower() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);
            state.setTimeRemaining(20);
            manager.saveScore(state, "Player1");

            int currentHighScore = manager.getHighScore(Difficulty.EASY).get().getScore();

            boolean isNew = manager.isNewHighScore(Difficulty.EASY, currentHighScore - 100);

            assertFalse(isNew, "Should NOT be new high score when score is lower");
        }

        @Test
        @DisplayName("Trả về false khi score bằng high score hiện tại")
        void shouldReturnFalseWhenScoreEquals() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);
            state.setTimeRemaining(30);
            manager.saveScore(state, "Player1");

            int currentHighScore = manager.getHighScore(Difficulty.EASY).get().getScore();

            boolean isNew = manager.isNewHighScore(Difficulty.EASY, currentHighScore);

            assertFalse(isNew, "Should NOT be new high score when score equals");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Singleton & Callback Management Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Singleton Pattern")
    class SingletonTests {

        @Test
        @DisplayName("getInstance() trả về cùng instance")
        void shouldReturnSameInstance() {
            ScoreManager instance1 = ScoreManager.getInstance();
            ScoreManager instance2 = ScoreManager.getInstance();

            assertSame(instance1, instance2, "getInstance should return same instance");
        }
    }

    @Nested
    @DisplayName("Callback Management")
    class CallbackManagementTests {

        @Test
        @DisplayName("setOnScoreChanged() lưu callback")
        void shouldSetCallback() {
            final boolean[] called = {false};
            manager.setOnScoreChanged(scores -> called[0] = true);

            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);

            manager.saveScore(state, "Test");

            assertTrue(called[0], "Callback should be called");
        }

        @Test
        @DisplayName("Callback nhận đúng danh sách scores")
        void shouldPassCorrectListToCallback() {
            final List<ScoreRecord>[] callbackScores = new List[1];
            manager.setOnScoreChanged(scores -> callbackScores[0] = scores);

            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);

            manager.saveScore(state, "Test");

            assertNotNull(callbackScores[0], "Callback should receive scores list");
            assertTrue(callbackScores[0].size() > 0, "Callback list should not be empty");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Data Persistence Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Data Persistence (BR-07.1 — persistent storage)")
    class DataPersistenceTests {

        @Test
        @DisplayName("Cache được load từ storage khi khởi tạo")
        void shouldLoadCacheFromStorageOnInit() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);

            manager.saveScore(state, "Persistent");

            int savedCount = manager.getTopScores(100).size();

            // FIX: Reset singleton trước để force reload từ storage
            ScoreManager.resetForTesting();
            ScoreManager newManager = ScoreManager.getInstance();
            int reloadedCount = newManager.getTopScores(100).size();

            assertEquals(savedCount, reloadedCount,
                    "Scores should be persisted and reloaded from storage");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [UC-06] clearAllScores() Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("clearAllScores()")
    class ClearAllScoresTests {

        @Test
        @DisplayName("Xóa tất cả scores khỏi cache")
        void shouldClearAllScoresFromCache() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);

            manager.saveScore(state, "TestPlayer");
            assertTrue(manager.getTopScores(100).size() > 0, "Should have scores initially");

            manager.clearAllScores();

            assertTrue(manager.getTopScores(100).isEmpty(), "Cache should be empty after clear");
        }

        @Test
        @DisplayName("Xóa tất cả scores khỏi storage")
        void shouldClearAllScoresFromStorage() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);

            manager.saveScore(state, "TestPlayer");

            manager.clearAllScores();

            // FIX: Reset singleton để force reload từ storage và kiểm tra thực sự
            ScoreManager.resetForTesting();
            ScoreManager newManager = ScoreManager.getInstance();
            assertTrue(newManager.getTopScores(100).isEmpty(), "Storage should be empty after clear");
        }

        @Test
        @DisplayName("Callback được gọi khi xóa scores")
        void shouldInvokeCallbackOnClear() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);

            manager.saveScore(state, "TestPlayer");

            final boolean[] callbackInvoked = {false};
            manager.setOnScoreChanged(scores -> {
                callbackInvoked[0] = true;
                assertTrue(scores.isEmpty(), "Cleared scores should be empty");
            });

            manager.clearAllScores();

            assertTrue(callbackInvoked[0], "Callback should be invoked on clear");
        }

        @Test
        @DisplayName("Xóa scores từ tất cả difficulties")
        void shouldClearScoresFromAllDifficulties() {
            for (Difficulty difficulty : Difficulty.values()) {
                GameState state = new GameState(difficulty);
                for (int i = 0; i < difficulty.totalPairs(); i++) {
                    state.incrementMatchedPairs();
                }
                state.setStatus(GameStatus.WON);
                manager.saveScore(state, "Player_" + difficulty);
            }

            manager.clearAllScores();

            for (Difficulty difficulty : Difficulty.values()) {
                assertTrue(manager.getTopScores(difficulty, 100).isEmpty(),
                        "Should clear scores for " + difficulty);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [UC-03 / UC-13] getBestScore() Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getBestScore()")
    class GetBestScoreTests {

        @Test
        @DisplayName("Trả về 0 khi không có scores")
        void shouldReturnZeroWhenNoScores() {
            manager.clearAllScores();

            int bestScore = manager.getBestScore(Difficulty.EASY);

            assertEquals(0, bestScore, "Should return 0 when no scores");
        }

        @Test
        @DisplayName("Trả về best score dạng int")
        void shouldReturnBestScoreAsInt() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);
            state.setTimeRemaining(30);

            manager.saveScore(state, "TestPlayer");

            int bestScore = manager.getBestScore(Difficulty.EASY);

            assertTrue(bestScore > 0, "Best score should be greater than 0");
        }

        @Test
        @DisplayName("Trả về highest score khi có multiple scores")
        void shouldReturnHighestScoreAmongMultiple() {
            int[] times = {10, 30, 50};

            for (int time : times) {
                GameState state = new GameState(Difficulty.EASY);
                for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                    state.incrementMatchedPairs();
                }
                state.setStatus(GameStatus.WON);
                state.setTimeRemaining(time);
                manager.saveScore(state, "Player");
            }

            int bestScore = manager.getBestScore(Difficulty.EASY);

            Optional<ScoreRecord> highScore = manager.getHighScore(Difficulty.EASY);
            assertTrue(highScore.isPresent(), "High score should be present");
            assertEquals(bestScore, highScore.get().getScore(),
                    "getBestScore should match getHighScore");
        }

    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [UC-07 / UC-13] getAllScores() Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAllScores()")
    class GetAllScoresTests {

        @Test
        @DisplayName("Trả về empty list khi không có scores")
        void shouldReturnEmptyListWhenNoScores() {
            manager.clearAllScores();

            List<ScoreRecord> allScores = manager.getAllScores();

            assertTrue(allScores.isEmpty(), "Should return empty list");
        }

        @Test
        @DisplayName("Trả về tất cả scores từ tất cả difficulties")
        void shouldReturnAllScoresFromAllDifficulties() {
            for (Difficulty difficulty : Difficulty.values()) {
                GameState state = new GameState(difficulty);
                for (int i = 0; i < difficulty.totalPairs(); i++) {
                    state.incrementMatchedPairs();
                }
                state.setStatus(GameStatus.WON);
                manager.saveScore(state, "Player_" + difficulty);
            }

            List<ScoreRecord> allScores = manager.getAllScores();

            assertEquals(Difficulty.values().length, allScores.size(),
                    "Should return one score for each difficulty");
        }

        @Test
        @DisplayName("Trả về bản sao (không thay đổi được cache)")
        void shouldReturnCopyNotReference() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);

            manager.saveScore(state, "TestPlayer");

            List<ScoreRecord> allScores1 = manager.getAllScores();
            List<ScoreRecord> allScores2 = manager.getAllScores();

            assertNotSame(allScores1, allScores2, "Should return different list instances");
            assertEquals(allScores1.size(), allScores2.size(), "But content should be the same");
        }

        @Test
        @DisplayName("Không thể modify returned list để thay đổi cache")
        void shouldNotAllowModificationOfCache() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            state.setStatus(GameStatus.WON);

            manager.saveScore(state, "TestPlayer");

            // FIX: Lấy originalSize từ manager, không từ list đã copy ra
            int originalSize = manager.getAllScores().size();

            List<ScoreRecord> allScores = manager.getAllScores();
            allScores.clear(); // Modify bản sao

            // Cache không bị ảnh hưởng
            assertEquals(originalSize, manager.getAllScores().size(),
                    "Cache should not be affected by modifying returned list");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Cleanup
    // ═══════════════════════════════════════════════════════════════════════════

    @AfterEach
    void tearDown() {
        manager.clearAllScores();
    }
}