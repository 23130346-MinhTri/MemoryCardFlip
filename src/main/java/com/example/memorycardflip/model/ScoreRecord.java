package com.example.memorycardflip.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Bản ghi điểm số bất biến (Java 17 Record).
 * Lưu vào ScoreStorage sau mỗi ván thắng.
 */
public record ScoreRecord(
        String playerName,
        Difficulty difficulty,
        int score,
        int moves,
        long timeUsed,       // Số giây đã dùng (= timeLimit - timeRemaining)
        Instant timestamp
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.systemDefault());

    // ── Compact constructor — validation ──────────────────────
    public ScoreRecord {
        if (playerName == null || playerName.isBlank()) playerName = "Anonymous";
        if (score    < 0) throw new IllegalArgumentException("Score cannot be negative");
        if (moves    < 0) throw new IllegalArgumentException("Moves cannot be negative");
        if (timeUsed < 0) throw new IllegalArgumentException("Time cannot be negative");
        if (timestamp == null) timestamp = Instant.now();
    }

    // ── Factory method tiện dùng ──────────────────────────────
    public static ScoreRecord of(String playerName, Difficulty difficulty,
                                 int score, int moves, long timeUsed) {
        return new ScoreRecord(playerName, difficulty, score, moves, timeUsed, Instant.now());
    }

    /** Tạo từ GameState sau khi game kết thúc */
    public static ScoreRecord fromGameState(String playerName, GameState state) {
        long used = state.getDifficulty().getTimeLimit() - state.getTimeRemaining();
        return ScoreRecord.of(
                playerName,
                state.getDifficulty(),
                state.calculateScore(),
                state.getMoves(),
                used
        );
    }

    // ── Display helpers ───────────────────────────────────────
    public String formattedTimestamp() {
        return FORMATTER.format(timestamp);
    }

    public String formattedTimeUsed() {
        return String.format("%d:%02d", timeUsed / 60, timeUsed % 60);
    }

    @Override
    public String toString() {
        return String.format("ScoreRecord{player='%s', difficulty=%s, score=%d, moves=%d, time=%s, at=%s}",
                playerName, difficulty.getDisplayName(), score, moves,
                formattedTimeUsed(), formattedTimestamp());
    }
}