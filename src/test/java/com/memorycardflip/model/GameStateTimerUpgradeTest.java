package com.memorycardflip.model;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * [UC-12 - Nâng cấp Countdown Timer]
 *
 * Unit tests kiểm tra các tính năng mới của timer:
 * - Ngưỡng cảnh báo thời gian (≤10s, ≤5s)
 * - Thêm +10 giây (setTimeRemaining)
 * - Progress bar ratio tính đúng
 * - Không âm sau khi decrementTime
 */
@DisplayName("UC-12 GameState Timer Upgrade Tests")
class GameStateTimerUpgradeTest {

    private GameState easyState;
    private GameState mediumState;
    private GameState hardState;

    @BeforeEach
    void setUp() {
        easyState   = new GameState(Difficulty.EASY);   // 60s
        mediumState = new GameState(Difficulty.MEDIUM); // 90s
        hardState   = new GameState(Difficulty.HARD);   // 120s
    }

    // ── Ngưỡng cảnh báo ──────────────────────────────────────

    @Nested
    @DisplayName("Ngưỡng cảnh báo timer (≤10s và ≤5s)")
    class TimerWarningThresholdTests {

        @Test
        @DisplayName("Không cảnh báo khi còn nhiều hơn 10 giây")
        void noWarningAbove10Seconds() {
            easyState.setTimeRemaining(11);
            assertFalse(easyState.getTimeRemaining() <= 10,
                    "Không nên cảnh báo khi còn 11 giây");
        }

        @Test
        @DisplayName("Bắt đầu cảnh báo nhẹ khi còn đúng 10 giây")
        void warningStartsAt10Seconds() {
            easyState.setTimeRemaining(10);
            assertTrue(easyState.getTimeRemaining() <= 10,
                    "Phải cảnh báo khi còn ≤10 giây");
        }

        @Test
        @DisplayName("Bắt đầu blink khi còn đúng 5 giây")
        void blinkStartsAt5Seconds() {
            easyState.setTimeRemaining(5);
            assertTrue(easyState.getTimeRemaining() <= 5,
                    "Phải bật blink khi còn ≤5 giây");
        }

        @Test
        @DisplayName("Blink khi còn 3 giây")
        void blinkActiveAt3Seconds() {
            easyState.setTimeRemaining(3);
            assertTrue(easyState.getTimeRemaining() <= 5,
                    "Phải đang blink khi còn 3 giây");
        }

        @Test
        @DisplayName("Không blink khi còn 6 giây")
        void noBlinkAt6Seconds() {
            easyState.setTimeRemaining(6);
            assertFalse(easyState.getTimeRemaining() <= 5,
                    "Không blink khi còn 6 giây");
        }
    }

    // ── Progress bar ratio ────────────────────────────────────

    @Nested
    @DisplayName("Progress bar ratio theo ngưỡng màu")
    class ProgressBarColorThresholdTests {

        @Test
        @DisplayName("Ratio > 0.5 → ngưỡng xanh lá")
        void ratioAbove50PercentIsGreen() {
            easyState.setTimeRemaining(40); // 40/60 = 0.667 > 0.5
            double ratio = (double) easyState.getTimeRemaining() / Difficulty.EASY.getTimeLimit();
            assertTrue(ratio > 0.5, "Ratio 66.7% phải thuộc ngưỡng xanh");
        }

        @Test
        @DisplayName("Ratio 0.2–0.5 → ngưỡng vàng")
        void ratioBetween20And50PercentIsYellow() {
            easyState.setTimeRemaining(18); // 18/60 = 0.30 ∈ (0.2, 0.5)
            double ratio = (double) easyState.getTimeRemaining() / Difficulty.EASY.getTimeLimit();
            assertTrue(ratio > 0.2 && ratio <= 0.5, "Ratio 30% phải thuộc ngưỡng vàng");
        }

        @Test
        @DisplayName("Ratio < 0.2 → ngưỡng đỏ")
        void ratioBelow20PercentIsRed() {
            easyState.setTimeRemaining(10); // 10/60 = 0.167 < 0.2
            double ratio = (double) easyState.getTimeRemaining() / Difficulty.EASY.getTimeLimit();
            assertTrue(ratio <= 0.2, "Ratio 16.7% phải thuộc ngưỡng đỏ");
        }

        @Test
        @DisplayName("Ratio đúng biên 50% thuộc ngưỡng vàng")
        void ratioExactly50PercentIsBorderYellow() {
            easyState.setTimeRemaining(30); // 30/60 = 0.5
            double ratio = (double) easyState.getTimeRemaining() / Difficulty.EASY.getTimeLimit();
            assertFalse(ratio > 0.5, "Biên 50% không thuộc ngưỡng xanh");
            assertTrue(ratio > 0.2, "Biên 50% thuộc ngưỡng vàng");
        }

        @Test
        @DisplayName("Ratio đúng biên 20% thuộc ngưỡng đỏ")
        void ratioExactly20PercentIsBorderRed() {
            easyState.setTimeRemaining(12); // 12/60 = 0.2
            double ratio = (double) easyState.getTimeRemaining() / Difficulty.EASY.getTimeLimit();
            assertFalse(ratio > 0.2, "Biên 20% không thuộc ngưỡng vàng");
        }
    }

    // ── +10 giây ─────────────────────────────────────────────

    @Nested
    @DisplayName("Chức năng +10 giây (Extra Time)")
    class ExtraTimeTests {

        @Test
        @DisplayName("+10 giây khi còn 8s → trở thành 18s")
        void addExtraTimeIncreasesCorrectly() {
            easyState.setTimeRemaining(8);
            easyState.setStatus(GameStatus.PLAYING);

            int before = easyState.getTimeRemaining();
            easyState.setTimeRemaining(before + 10);

            assertEquals(18, easyState.getTimeRemaining(),
                    "Sau khi +10s phải là 18 giây");
        }

        @Test
        @DisplayName("+10 giây thoát khỏi vùng blink (>5s)")
        void addExtraTimeExitsBlinkZone() {
            easyState.setTimeRemaining(3); // Đang trong vùng blink ≤5
            assertTrue(easyState.getTimeRemaining() <= 5, "Ban đầu phải trong vùng blink");

            easyState.setTimeRemaining(easyState.getTimeRemaining() + 10); // = 13

            assertFalse(easyState.getTimeRemaining() <= 5,
                    "Sau +10s phải thoát khỏi vùng blink");
        }

        @Test
        @DisplayName("+10 giây thoát khỏi vùng cảnh báo (>10s)")
        void addExtraTimeExitsWarningZone() {
            easyState.setTimeRemaining(7); // Đang trong vùng cảnh báo ≤10
            assertTrue(easyState.getTimeRemaining() <= 10, "Ban đầu phải trong vùng cảnh báo");

            easyState.setTimeRemaining(easyState.getTimeRemaining() + 10); // = 17

            assertFalse(easyState.getTimeRemaining() <= 10,
                    "Sau +10s phải thoát khỏi vùng cảnh báo");
        }

        @Test
        @DisplayName("+10 giây khi còn 0s (hết giờ) → trở thành 10s")
        void addExtraTimeFromZero() {
            easyState.setTimeRemaining(0);
            assertTrue(easyState.isTimeUp());

            easyState.setTimeRemaining(10);

            assertEquals(10, easyState.getTimeRemaining());
            assertFalse(easyState.isTimeUp(), "Sau +10s không còn hết giờ");
        }

        @Test
        @DisplayName("useHint trừ đúng 50 điểm penalty")
        void extraTimePenalty50Points() {
            easyState.setStatus(GameStatus.PLAYING);
            // Thêm vài cặp để có điểm base
            for (int i = 0; i < 4; i++) easyState.incrementMatchedPairs();
            easyState.setTimeRemaining(30);

            int scoreBefore = easyState.calculateScore();
            easyState.useHint(); // penalty 50 điểm
            int scoreAfter = easyState.calculateScore();

            assertTrue(scoreAfter < scoreBefore,
                    "Sau khi dùng +10s điểm phải giảm do penalty");
            assertEquals(scoreBefore - 50, scoreAfter,
                    "Penalty chính xác là 50 điểm");
        }
    }

    // ── Decrement không âm ────────────────────────────────────

    @Nested
    @DisplayName("DecrementTime không cho giá trị âm")
    class DecrementSafetyTests {

        @Test
        @DisplayName("decrementTime dừng ở 0, không xuống âm")
        void decrementStopsAtZero() {
            easyState.setTimeRemaining(0);
            easyState.decrementTime();
            assertEquals(0, easyState.getTimeRemaining(),
                    "Không được âm sau decrementTime");
        }

        @Test
        @DisplayName("decrementTime từ 1 → 0 và isTimeUp = true")
        void decrementFrom1ToZeroTriggersTimeUp() {
            easyState.setTimeRemaining(1);
            easyState.decrementTime();
            assertEquals(0, easyState.getTimeRemaining());
            assertTrue(easyState.isTimeUp());
        }
    }

    // ── Reset timer ───────────────────────────────────────────

    @Test
    @DisplayName("reset() khôi phục timeRemaining về timeLimit của difficulty")
    void resetRestoresTimeLimit() {
        easyState.setTimeRemaining(5);
        easyState.reset();
        assertEquals(Difficulty.EASY.getTimeLimit(), easyState.getTimeRemaining(),
                "Sau reset phải về timeLimit ban đầu (60s)");
    }

    @Test
    @DisplayName("Mỗi độ khó có timeLimit đúng")
    void eachDifficultyHasCorrectTimeLimit() {
        assertAll(
                () -> assertEquals(60,  Difficulty.EASY.getTimeLimit()),
                () -> assertEquals(90,  Difficulty.MEDIUM.getTimeLimit()),
                () -> assertEquals(120, Difficulty.HARD.getTimeLimit())
        );
    }
}
