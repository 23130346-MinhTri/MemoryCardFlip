package com.memorycardflip.model;

import com.example.memorycardflip.model.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * [UC-07 — View Leaderboard] [UC-13 — Save High Score]
 * 
 * Unit tests cho ScoreStorage interface default methods.
 * 
 * <p>Bao gồm test cases cho:</p>
 * <ul>
 *   <li>getTopScores(difficulty, limit) - default method</li>
 *   <li>getHighScore(difficulty) - default method</li>
 *   <li>addRecord(record) - default method</li>
 *   <li>clear() - default method</li>
 * </ul>
 */
@DisplayName("ScoreStorage Interface Tests (UC-07 & UC-13)")
class ScoreStorageTest {

    private ScoreStorage storage;

    @BeforeEach
    void setUp() {
        storage = new JsonScoreStorage();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [UC-07 — 7.1.5 → 7.1.6] getTopScores(difficulty, limit) Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getTopScores(difficulty, limit)")
    class GetTopScoresTests {

        @Test
        @DisplayName("Trả về empty list khi không có scores")
        void shouldReturnEmptyWhenNoScores() throws Exception {
            List<ScoreRecord> topScores = storage.getTopScores(Difficulty.EASY, 10);
            
            assertTrue(topScores.isEmpty(), "Should return empty list");
        }

        @Test
        @DisplayName("Lọc theo difficulty đã chỉ định")
        void shouldFilterByDifficulty() throws Exception {
            // Save scores for different difficulties
            List<ScoreRecord> records = new ArrayList<>();
            records.add(ScoreRecord.of("EasyPlayer", Difficulty.EASY, 100, 5, 10L));
            records.add(ScoreRecord.of("HardPlayer", Difficulty.HARD, 200, 10, 20L));
            records.add(ScoreRecord.of("MediumPlayer", Difficulty.MEDIUM, 150, 7, 15L));
            records.add(ScoreRecord.of("EasyPlayer2", Difficulty.EASY, 120, 6, 12L));
            
            storage.save(records);
            
            List<ScoreRecord> easyScores = storage.getTopScores(Difficulty.EASY, 100);
            
            assertTrue(easyScores.stream().allMatch(r -> r.getDifficulty() == Difficulty.EASY),
                    "All returned scores should be EASY difficulty");
            assertEquals(2, easyScores.size(), "Should return exactly 2 EASY scores");
        }

        @Test
        @DisplayName("Sort scores descending theo score")
        void shouldSortDescendingByScore() throws Exception {
            List<ScoreRecord> records = new ArrayList<>();
            records.add(ScoreRecord.of("Player1", Difficulty.EASY, 100, 5, 10L));
            records.add(ScoreRecord.of("Player2", Difficulty.EASY, 300, 7, 15L));
            records.add(ScoreRecord.of("Player3", Difficulty.EASY, 200, 6, 12L));
            
            storage.save(records);
            
            List<ScoreRecord> topScores = storage.getTopScores(Difficulty.EASY, 100);
            
            assertEquals(3, topScores.size());
            assertEquals(300, topScores.get(0).getScore());
            assertEquals(200, topScores.get(1).getScore());
            assertEquals(100, topScores.get(2).getScore());
        }

        @Test
        @DisplayName("Giới hạn kết quả theo limit")
        void shouldLimitResults() throws Exception {
            List<ScoreRecord> records = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                records.add(ScoreRecord.of("Player" + i, Difficulty.EASY, (i + 1) * 100, 5 + i, 10L + i));
            }
            
            storage.save(records);
            
            List<ScoreRecord> top3 = storage.getTopScores(Difficulty.EASY, 3);
            
            assertEquals(3, top3.size(), "Should limit to 3 results");
            assertTrue(top3.get(0).getScore() >= top3.get(1).getScore(),
                    "Should be sorted descending");
        }

        @Test
        @DisplayName("Trả về ít hơn limit nếu không đủ dữ liệu")
        void shouldReturnLessThanLimitIfNotEnoughData() throws Exception {
            List<ScoreRecord> records = new ArrayList<>();
            records.add(ScoreRecord.of("Player1", Difficulty.EASY, 100, 5, 10L));
            records.add(ScoreRecord.of("Player2", Difficulty.EASY, 200, 7, 15L));
            
            storage.save(records);
            
            List<ScoreRecord> top10 = storage.getTopScores(Difficulty.EASY, 10);
            
            assertEquals(2, top10.size(), "Should return only 2 scores");
        }

        @Test
        @DisplayName("Trả về 0 scores khi limit = 0")
        void shouldReturnEmptyWhenLimitIsZero() throws Exception {
            List<ScoreRecord> records = new ArrayList<>();
            records.add(ScoreRecord.of("Player1", Difficulty.EASY, 100, 5, 10L));
            
            storage.save(records);
            
            List<ScoreRecord> top0 = storage.getTopScores(Difficulty.EASY, 0);
            
            assertTrue(top0.isEmpty(), "Should return empty when limit is 0");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // [UC-07 — 7.1.7] getHighScore(difficulty) Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getHighScore(difficulty)")
    class GetHighScoreTests {

        @Test
        @DisplayName("Trả về empty Optional khi không có scores")
        void shouldReturnEmptyWhenNoScores() throws Exception {
            Optional<ScoreRecord> highScore = storage.getHighScore(Difficulty.EASY);
            
            assertTrue(highScore.isEmpty(), "Should return empty Optional");
        }

        @Test
        @DisplayName("Trả về highest score cho difficulty")
        void shouldReturnHighestScore() throws Exception {
            List<ScoreRecord> records = new ArrayList<>();
            records.add(ScoreRecord.of("Player1", Difficulty.EASY, 100, 5, 10L));
            records.add(ScoreRecord.of("Player2", Difficulty.EASY, 300, 7, 15L));
            records.add(ScoreRecord.of("Player3", Difficulty.EASY, 200, 6, 12L));
            
            storage.save(records);
            
            Optional<ScoreRecord> highScore = storage.getHighScore(Difficulty.EASY);
            
            assertTrue(highScore.isPresent());
            assertEquals(300, highScore.get().getScore());
            assertEquals("Player2", highScore.get().getPlayerName());
        }

        @Test
        @DisplayName("Không ảnh hưởng bởi scores khác difficulty")
        void shouldNotBeAffectedByOtherDifficulties() throws Exception {
            List<ScoreRecord> records = new ArrayList<>();
            records.add(ScoreRecord.of("HardPlayer", Difficulty.HARD, 1000, 20, 5L));
            records.add(ScoreRecord.of("EasyPlayer", Difficulty.EASY, 100, 5, 10L));
            records.add(ScoreRecord.of("HardPlayer2", Difficulty.HARD, 800, 18, 8L));
            
            storage.save(records);
            
            Optional<ScoreRecord> easyHighScore = storage.getHighScore(Difficulty.EASY);
            
            assertTrue(easyHighScore.isPresent());
            assertEquals(100, easyHighScore.get().getScore());
            assertEquals(Difficulty.EASY, easyHighScore.get().getDifficulty());
        }

        @Test
        @DisplayName("Single score is also high score")
        void shouldReturnSingleScoreAsHighScore() throws Exception {
            List<ScoreRecord> records = new ArrayList<>();
            records.add(ScoreRecord.of("OnlyPlayer", Difficulty.MEDIUM, 250, 10, 20L));
            
            storage.save(records);
            
            Optional<ScoreRecord> highScore = storage.getHighScore(Difficulty.MEDIUM);
            
            assertTrue(highScore.isPresent());
            assertEquals("OnlyPlayer", highScore.get().getPlayerName());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // addRecord(record) Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addRecord(record)")
    class AddRecordTests {

        @Test
        @DisplayName("Thêm record vào empty storage")
        void shouldAddRecordToEmptyStorage() throws Exception {
            ScoreRecord record = ScoreRecord.of("NewPlayer", Difficulty.EASY, 150, 8, 25L);
            
            storage.addRecord(record);
            
            List<ScoreRecord> loaded = storage.load();
            assertEquals(1, loaded.size());
            assertEquals("NewPlayer", loaded.get(0).getPlayerName());
        }

        @Test
        @DisplayName("Thêm record vào existing records")
        void shouldAddRecordToExisting() throws Exception {
            // Pre-populate storage
            List<ScoreRecord> initial = new ArrayList<>();
            initial.add(ScoreRecord.of("Player1", Difficulty.EASY, 100, 5, 10L));
            storage.save(initial);
            
            // Add new record
            ScoreRecord newRecord = ScoreRecord.of("Player2", Difficulty.EASY, 200, 7, 15L);
            storage.addRecord(newRecord);
            
            List<ScoreRecord> loaded = storage.load();
            assertEquals(2, loaded.size());
        }

        @Test
        @DisplayName("Bảo toàn existing records khi add mới")
        void shouldPreserveExistingRecords() throws Exception {
            List<ScoreRecord> initial = new ArrayList<>();
            initial.add(ScoreRecord.of("Original", Difficulty.EASY, 500, 10, 30L));
            storage.save(initial);
            
            storage.addRecord(ScoreRecord.of("New", Difficulty.EASY, 300, 8, 20L));
            
            List<ScoreRecord> loaded = storage.load();
            assertTrue(loaded.stream().anyMatch(r -> "Original".equals(r.getPlayerName())),
                    "Original record should be preserved");
            assertTrue(loaded.stream().anyMatch(r -> "New".equals(r.getPlayerName())),
                    "New record should be added");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // clear() Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("clear()")
    class ClearTests {

        @Test
        @DisplayName("Xóa tất cả records")
        void shouldClearAllRecords() throws Exception {
            // Pre-populate storage
            List<ScoreRecord> records = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                records.add(ScoreRecord.of("Player" + i, Difficulty.EASY, (i + 1) * 100, 5 + i, 10L + i));
            }
            storage.save(records);
            
            storage.clear();
            
            List<ScoreRecord> loaded = storage.load();
            assertTrue(loaded.isEmpty(), "Storage should be empty after clear");
        }

        @Test
        @DisplayName("Clear empty storage không throw exception")
        void shouldClearEmptyStorageWithoutError() throws Exception {
            assertDoesNotThrow(() -> storage.clear(),
                    "Should not throw exception when clearing empty storage");
        }

        @Test
        @DisplayName("Clear các records từ tất cả difficulties")
        void shouldClearRecordsFromAllDifficulties() throws Exception {
            List<ScoreRecord> records = new ArrayList<>();
            records.add(ScoreRecord.of("EasyPlayer", Difficulty.EASY, 100, 5, 10L));
            records.add(ScoreRecord.of("MediumPlayer", Difficulty.MEDIUM, 150, 7, 15L));
            records.add(ScoreRecord.of("HardPlayer", Difficulty.HARD, 200, 10, 20L));
            
            storage.save(records);
            storage.clear();
            
            List<ScoreRecord> loaded = storage.load();
            assertTrue(loaded.isEmpty(), "All records should be cleared");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Integration Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Save → getTopScores → getHighScore chain")
        void shouldWorkThroughFullChain() throws Exception {
            // Setup
            List<ScoreRecord> records = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                records.add(ScoreRecord.of(
                        "Player" + i,
                        Difficulty.EASY,
                        i * 100,
                        5 * i,
                        10L * i
                ));
            }
            
            storage.save(records);
            
            // Test getTopScores
            List<ScoreRecord> top3 = storage.getTopScores(Difficulty.EASY, 3);
            assertEquals(3, top3.size());
            assertEquals(500, top3.get(0).getScore());
            
            // Test getHighScore
            Optional<ScoreRecord> highScore = storage.getHighScore(Difficulty.EASY);
            assertTrue(highScore.isPresent());
            assertEquals(500, highScore.get().getScore());
        }

        @Test
        @DisplayName("Add → Clear → Add cycle")
        void shouldHandleAddClearAddCycle() throws Exception {
            // Add initial
            storage.addRecord(ScoreRecord.of("First", Difficulty.EASY, 100, 5, 10L));
            List<ScoreRecord> loaded1 = storage.load();
            assertEquals(1, loaded1.size());
            
            // Clear
            storage.clear();
            List<ScoreRecord> loaded2 = storage.load();
            assertTrue(loaded2.isEmpty());
            
            // Add again
            storage.addRecord(ScoreRecord.of("Second", Difficulty.EASY, 200, 10, 20L));
            List<ScoreRecord> loaded3 = storage.load();
            assertEquals(1, loaded3.size());
            assertEquals("Second", loaded3.get(0).getPlayerName());
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        storage.clear();
    }
}
