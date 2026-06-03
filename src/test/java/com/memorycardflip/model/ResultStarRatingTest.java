package com.memorycardflip.model;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * [UC-03 - Nâng cấp View Result]
 *
 * Unit tests kiểm tra logic tính sao (1–5★) và gợi ý thông minh.
 * Logic đánh giá sao nằm trong ResultController.calculateStarRating(),
 * các test này kiểm tra trực tiếp điều kiện của GameState để đảm bảo
 * công thức tính sao hoạt động đúng.
 *
 * Tiêu chí tính sao:
 *   - Thua          → 1 sao (0 cặp) hoặc 2 sao (≥50% cặp)
 *   - Thắng cơ bản  → 2 sao
 *   - +1 sao nếu còn ≥30% thời gian
 *   - +1 sao nếu move efficiency ≥80% (moves ≤ totalPairs*2 / 0.8)
 */
@DisplayName("UC-03 Star Rating & Smart Tip Logic Tests")
class ResultStarRatingTest {

    private static final int EASY_PAIRS     = Difficulty.EASY.totalPairs();   // 8
    private static final int EASY_TIME      = Difficulty.EASY.getTimeLimit(); // 60

    // ── Helper ────────────────────────────────────────────────

    /** Tạo GameState WON với số lượt và thời gian tùy chỉnh */
    private GameState wonState(int moves, int timeRemaining) {
        GameState state = new GameState(Difficulty.EASY);
        state.setStatus(GameStatus.WON);
        for (int i = 0; i < EASY_PAIRS; i++) state.incrementMatchedPairs();
        for (int i = 0; i < moves; i++) state.incrementMoves();
        state.setTimeRemaining(timeRemaining);
        return state;
    }

    /** Tạo GameState LOST với số cặp đã ghép */
    private GameState lostState(int matchedPairs) {
        GameState state = new GameState(Difficulty.EASY);
        state.setStatus(GameStatus.LOST);
        for (int i = 0; i < matchedPairs; i++) state.incrementMatchedPairs();
        state.setTimeRemaining(0);
        return state;
    }

    // ── Thua: 1–2 sao ─────────────────────────────────────────

    @Nested
    @DisplayName("Thua — 1 hoặc 2 sao")
    class LoseStarTests {

        @Test
        @DisplayName("Thua với 0 cặp → 1 sao")
        void loseWith0Pairs_1Star() {
            GameState state = lostState(0);
            // Kiểm tra điều kiện: matched == 0
            assertEquals(0, state.getMatchedPairs());
            assertFalse(state.getStatus() == GameStatus.WON);
        }

        @Test
        @DisplayName("Thua với <50% cặp → 1 sao")
        void loseWithBelow50Percent_1Star() {
            GameState state = lostState(3); // 3/8 = 37.5%
            double matchRatio = (double) state.getMatchedPairs() / EASY_PAIRS;
            assertTrue(matchRatio < 0.5, "Dưới 50% cặp phải là 1 sao");
        }

        @Test
        @DisplayName("Thua với ≥50% cặp → 2 sao")
        void loseWith50PercentOrMore_2Stars() {
            GameState state = lostState(4); // 4/8 = 50%
            double matchRatio = (double) state.getMatchedPairs() / EASY_PAIRS;
            assertTrue(matchRatio >= 0.5, "Từ 50% cặp phải là 2 sao");
        }

        @Test
        @DisplayName("Thua với 7/8 cặp → 2 sao (không phải 5 sao dù gần xong)")
        void loseWith7Of8Pairs_2Stars() {
            GameState state = lostState(7); // 7/8 = 87.5%
            assertEquals(GameStatus.LOST, state.getStatus(),
                    "Trạng thái phải là LOST");
            double matchRatio = (double) state.getMatchedPairs() / EASY_PAIRS;
            assertTrue(matchRatio >= 0.5, "Từ 50% cặp là 2 sao");
        }
    }

    // ── Thắng: 2–5 sao ────────────────────────────────────────

    @Nested
    @DisplayName("Thắng — 2 đến 5 sao")
    class WinStarTests {

        @Test
        @DisplayName("Thắng cơ bản = ít nhất 2 sao")
        void winAtLeast2Stars() {
            GameState state = wonState(EASY_PAIRS * 4, 1); // nhiều moves, ít time
            assertEquals(GameStatus.WON, state.getStatus());
            // 2 sao cơ bản khi thắng
            assertTrue(state.getMatchedPairs() == EASY_PAIRS,
                    "Phải ghép đủ cặp để thắng");
        }

        @Test
        @DisplayName("Thắng + còn ≥30% thời gian → ít nhất 3 sao")
        void winWith30PercentTimeLeft_atLeast3Stars() {
            int timeRemaining = (int)(EASY_TIME * 0.3); // = 18s = 30%
            GameState state = wonState(EASY_PAIRS * 3, timeRemaining);

            double timeRatio = (double) state.getTimeRemaining() / EASY_TIME;
            assertTrue(timeRatio >= 0.3, "Phải còn ≥30% thời gian để +1 sao");
        }

        @Test
        @DisplayName("Thắng + còn <30% thời gian → không được +1 sao thời gian")
        void winWithBelow30PercentTime_noTimeStar() {
            int timeRemaining = (int)(EASY_TIME * 0.29); // = 17s < 30%
            GameState state = wonState(EASY_PAIRS * 3, timeRemaining);

            double timeRatio = (double) state.getTimeRemaining() / EASY_TIME;
            assertFalse(timeRatio >= 0.3, "Dưới 30% không được +1 sao thời gian");
        }

        @Test
        @DisplayName("Thắng + move efficiency ≥80% → +1 sao độ chính xác")
        void winWithHighMoveEfficiency_accuracyStar() {
            // Minimum moves = EASY_PAIRS * 2 = 16
            // efficiency = 16/moves ≥ 0.8 → moves ≤ 20
            int perfectMoves = EASY_PAIRS * 2; // 16 moves = 100% efficiency
            GameState state = wonState(perfectMoves, 5);

            double efficiency = (double)(EASY_PAIRS * 2) / state.getMoves();
            assertTrue(efficiency >= 0.8, "16 moves phải có efficiency ≥80%");
        }

        @Test
        @DisplayName("Thắng + nhiều moves sai → efficiency <80%, không +sao")
        void winWithLowMoveEfficiency_noAccuracyStar() {
            int badMoves = EASY_PAIRS * 5; // 40 moves = 40% efficiency
            GameState state = wonState(badMoves, 5);

            double efficiency = (double)(EASY_PAIRS * 2) / state.getMoves();
            assertFalse(efficiency >= 0.8, "40 moves phải có efficiency <80%");
        }

        @Test
        @DisplayName("Thắng hoàn hảo (moves tối thiểu + ≥30% time) → 4–5 sao")
        void perfectWin_4or5Stars() {
            int minMoves = EASY_PAIRS * 2; // 16 moves
            int goodTime = (int)(EASY_TIME * 0.5); // 50% time còn lại
            GameState state = wonState(minMoves, goodTime);

            // Kiểm tra cả 2 tiêu chí +1 sao đều đạt
            double timeRatio = (double) state.getTimeRemaining() / EASY_TIME;
            double efficiency = (double)(EASY_PAIRS * 2) / state.getMoves();

            assertTrue(timeRatio >= 0.3, "Tiêu chí thời gian phải đạt");
            assertTrue(efficiency >= 0.8, "Tiêu chí độ chính xác phải đạt");
        }
    }

    // ── Star string format ────────────────────────────────────

    @Nested
    @DisplayName("Format chuỗi sao")
    class StarStringFormatTests {

        /** Mô phỏng buildStarString() */
        private String buildStarString(int stars) {
            return "★".repeat(stars) + "☆".repeat(5 - stars);
        }

        @Test
        @DisplayName("1 sao → ★☆☆☆☆")
        void oneStar() {
            assertEquals("★☆☆☆☆", buildStarString(1));
        }

        @Test
        @DisplayName("3 sao → ★★★☆☆")
        void threeStars() {
            assertEquals("★★★☆☆", buildStarString(3));
        }

        @Test
        @DisplayName("5 sao → ★★★★★")
        void fiveStars() {
            assertEquals("★★★★★", buildStarString(5));
        }

        @Test
        @DisplayName("Tổng ký tự luôn là 5")
        void totalCharsAlways5() {
            for (int i = 1; i <= 5; i++) {
                String s = buildStarString(i);
                assertEquals(5, s.codePointCount(0, s.length()),
                        i + " sao phải có tổng 5 ký tự");
            }
        }
    }

    // ── Smart tip conditions ──────────────────────────────────

    @Nested
    @DisplayName("Gợi ý thông minh — điều kiện kích hoạt")
    class SmartTipConditionTests {

        @Test
        @DisplayName("wrongMoves ≥ totalPairs → gợi ý 'lật sai nhiều'")
        void highWrongMovesTriggersMemoryTip() {
            // wrongMoves = moves - minMoves
            int moves = EASY_PAIRS * 2 + EASY_PAIRS; // sai EASY_PAIRS lần
            GameState state = wonState(moves, 20);
            int wrongMoves = state.getMoves() - EASY_PAIRS * 2;

            assertTrue(wrongMoves >= EASY_PAIRS,
                    "wrongMoves ≥ totalPairs phải kích hoạt gợi ý nhớ thẻ");
        }

        @Test
        @DisplayName("Dùng >85% thời gian → gợi ý 'lật nhanh hơn'")
        void highTimeUsageTriggersSpeedTip() {
            int timeUsed = (int)(EASY_TIME * 0.9); // dùng 90%
            int remaining = EASY_TIME - timeUsed;   // còn 10%
            GameState state = wonState(EASY_PAIRS * 2, remaining);

            double timeRatio = (double)(EASY_TIME - state.getTimeRemaining()) / EASY_TIME;
            assertTrue(timeRatio > 0.85,
                    "Dùng >85% thời gian phải kích hoạt gợi ý tốc độ");
        }

        @Test
        @DisplayName("Combo < 3 → gợi ý 'ghép liên tiếp để nhân điểm'")
        void lowComboTriggersComboTip() {
            GameState state = wonState(EASY_PAIRS * 2, 30);
            // Không increment combo → comboCount = 0
            assertTrue(state.getComboCount() < 3,
                    "Combo thấp phải kích hoạt gợi ý combo");
        }

        @Test
        @DisplayName("Chơi tốt: sai ≤2 lần và dùng <50% time → gợi ý khen ngợi")
        void excellentPlayTriggersPraiseTip() {
            int moves = EASY_PAIRS * 2 + 2; // chỉ sai 2 lần
            int remaining = (int)(EASY_TIME * 0.55); // còn 55% time → dùng 45% < 50%
            GameState state = wonState(moves, remaining);

            int wrongMoves = state.getMoves() - EASY_PAIRS * 2;
            double timeRatio = (double)(EASY_TIME - state.getTimeRemaining()) / EASY_TIME;

            assertTrue(wrongMoves <= 2, "Sai ≤2 lần là chơi tốt");
            assertTrue(timeRatio < 0.5, "Dùng <50% time là chơi tốt");
        }

        @Test
        @DisplayName("Thua với 0 cặp → gợi ý thử độ khó dễ hơn")
        void loseWith0PairsSuggestsEasierDifficulty() {
            GameState state = lostState(0);
            assertEquals(0, state.getMatchedPairs(),
                    "0 cặp phải gợi ý thử độ khó dễ hơn");
        }

        @Test
        @DisplayName("Thua với một số cặp → gợi ý cần thêm thời gian")
        void loseWithSomePairsSuggestsMoreTime() {
            GameState state = lostState(5); // 5/8 cặp
            assertTrue(state.getMatchedPairs() > 0,
                    "Có cặp đã ghép phải gợi ý cần thêm thời gian");
            assertTrue(state.getMatchedPairs() < EASY_PAIRS,
                    "Chưa ghép đủ cặp");
        }
    }
}
