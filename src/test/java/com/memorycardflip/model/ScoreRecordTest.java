package com.memorycardflip.model;

import com.example.memorycardflip.model.*;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho ScoreRecord (Java 17 Record).
 */
@DisplayName("ScoreRecord Tests")
class ScoreRecordTest {
    
    // ── Constructor / Compact constructor ─────────────────────

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Tạo record hợp lệ thành công")
        void shouldCreateValidRecord() {
            ScoreRecord r = ScoreRecord.of("Minh", Difficulty.EASY, 500, 10, 25L);
            assertAll(
                    () -> assertEquals("Minh",         r.getPlayerName()),
                    () -> assertEquals(Difficulty.EASY, r.getDifficulty()),
                    () -> assertEquals(500,             r.getScore()),
                    () -> assertEquals(10,              r.getMoves()),
                    () -> assertEquals(25L,             r.getTimeUsed()),
                    () -> assertNotNull(r.getTimestamp())
            );
        }

        @Test
        @DisplayName("playerName null → mặc định 'Anonymous'")
        void nullPlayerNameShouldDefaultToAnonymous() {
            ScoreRecord r = ScoreRecord.of(null, Difficulty.EASY, 100, 5, 10L);
            assertEquals("Anonymous", r.getPlayerName());
        }

        @Test
        @DisplayName("playerName blank → mặc định 'Anonymous'")
        void blankPlayerNameShouldDefaultToAnonymous() {
            ScoreRecord r = ScoreRecord.of("   ", Difficulty.EASY, 100, 5, 10L);
            assertEquals("Anonymous", r.getPlayerName());
        }

        @Test
        @DisplayName("Score âm → ném IllegalArgumentException")
        void negativeScoreShouldThrow() {
            assertThrows(IllegalArgumentException.class,
                    () -> ScoreRecord.of("A", Difficulty.EASY, -1, 5, 10L));
        }

        @Test
        @DisplayName("Moves âm → ném IllegalArgumentException")
        void negativeMovesShouldThrow() {
            assertThrows(IllegalArgumentException.class,
                    () -> ScoreRecord.of("A", Difficulty.EASY, 100, -1, 10L));
        }

        @Test
        @DisplayName("TimeUsed âm → ném IllegalArgumentException")
        void negativeTimeShouldThrow() {
            assertThrows(IllegalArgumentException.class,
                    () -> ScoreRecord.of("A", Difficulty.EASY, 100, 5, -1L));
        }

        @Test
        @DisplayName("timestamp null → tự set Instant.now()")
        void nullTimestampShouldDefaultToNow() {
            ScoreRecord r = new ScoreRecord("A", Difficulty.EASY, 100, 5, 10L, null);
            assertNotNull(r.getTimestamp());
        }
    }

    // ── fromGameState() ───────────────────────────────────────

    @Nested
    @DisplayName("fromGameState()")
    class FromGameStateTests {

        @Test
        @DisplayName("Tạo đúng từ GameState hoàn chỉnh")
        void shouldCreateFromCompletedGameState() {
            GameState state = new GameState(Difficulty.EASY);
            for (int i = 0; i < Difficulty.EASY.totalPairs(); i++) {
                state.incrementMatchedPairs();
            }
            for (int i = 0; i < 15; i++) {
                state.incrementMoves();
            }

            state.setTimeRemaining(20); // 60 - 20 = 40s đã dùng

            ScoreRecord r = ScoreRecord.fromGameState("Nam", state);

            assertAll(
                    () -> assertEquals("Nam",          r.getPlayerName()),
                    () -> assertEquals(Difficulty.EASY, r.getDifficulty()),
                    () -> assertEquals(40L,             r.getTimeUsed()),
                    () -> assertEquals(15,              r.getMoves()),
                    () -> assertTrue(r.getScore() > 0)
            );
        }
    }

    // ── Display helpers ───────────────────────────────────────

    @Nested
    @DisplayName("Display helpers")
    class DisplayTests {

        @Test
        @DisplayName("formattedTimeUsed() đúng định dạng m:ss")
        void shouldFormatTimeCorrectly() {
            ScoreRecord r = ScoreRecord.of("A", Difficulty.EASY, 100, 5, 90L);
            assertEquals("1:30", r.formattedTimeUsed());
        }

        @Test
        @DisplayName("formattedTimeUsed() dưới 1 phút")
        void shouldFormatTimeBelowOneMinute() {
            ScoreRecord r = ScoreRecord.of("A", Difficulty.EASY, 100, 5, 45L);
            assertEquals("0:45", r.formattedTimeUsed());
        }

        @Test
        @DisplayName("formattedTimestamp() không null và không rỗng")
        void shouldFormatTimestampNotBlank() {
            ScoreRecord r = ScoreRecord.of("A", Difficulty.EASY, 100, 5, 30L);
            assertNotNull(r.formattedTimestamp());
            assertFalse(r.formattedTimestamp().isBlank());
        }
    }

    // ── Record equality ───────────────────────────────────────

    @Test
    @DisplayName("2 Record cùng giá trị → bằng nhau (Java Record)")
    void sameValuesShouldBeEqual() {
        Instant now = Instant.now();
        ScoreRecord r1 = new ScoreRecord("A", Difficulty.EASY, 100, 5, 30L, now);
        ScoreRecord r2 = new ScoreRecord("A", Difficulty.EASY, 100, 5, 30L, now);
        assertEquals(r1, r2);
    }
}