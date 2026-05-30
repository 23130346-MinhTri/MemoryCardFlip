package com.example.memorycardflip.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * [UC-13 — Save High Score]
 * Bản ghi điểm số bất biến — được tạo và lưu vào ScoreStorage sau mỗi ván thắng.
 *
 * <p>Dùng class thường (không phải Java record) để Gson có thể
 * serialize/deserialize qua reflection.</p>
 *
 * <p><b>UC-13 Sequence references:</b></p>
 * <ul>
 *   <li>13.1.5  — ScoreManager gọi {@link #fromGameState(String, GameState)}</li>
 *   <li>13.1.5  — Trong fromGameState(), điểm được tính theo công thức BR-13.2</li>
 *   <li>13.1.5  — ScoreRecord trả kết quả về ScoreManager (bước 10)</li>
 * </ul>
 */
public class ScoreRecord {

    private String     playerName;
    private Difficulty difficulty;
    private int        score;
    private int        moves;
    private long       timeUsed;   // giây đã dùng = timeLimit - timeRemaining
    private Instant    timestamp;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.systemDefault());

    // ── Constructor ───────────────────────────────────────────
    public ScoreRecord(String playerName, Difficulty difficulty,
                       int score, int moves, long timeUsed, Instant timestamp) {
        this.playerName = (playerName == null || playerName.isBlank()) ? "Anonymous" : playerName;
        if (score    < 0) throw new IllegalArgumentException("Score cannot be negative");
        if (moves    < 0) throw new IllegalArgumentException("Moves cannot be negative");
        if (timeUsed < 0) throw new IllegalArgumentException("Time cannot be negative");
        this.difficulty = difficulty;
        this.score      = score;
        this.moves      = moves;
        this.timeUsed   = timeUsed;
        this.timestamp  = (timestamp == null) ? Instant.now() : timestamp;
    }

    // ── Factory methods ───────────────────────────────────────
    public static ScoreRecord of(String playerName, Difficulty difficulty,
                                 int score, int moves, long timeUsed) {
        return new ScoreRecord(playerName, difficulty, score, moves, timeUsed, Instant.now());
    }

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

    // ── Getters ───────────────────────────────────────────────
    public String     getPlayerName() { return playerName; }
    public Difficulty getDifficulty() { return difficulty; }
    public int        getScore()      { return score;      }
    public int        getMoves()      { return moves;      }
    public long       getTimeUsed()   { return timeUsed;   }
    public Instant    getTimestamp()  { return timestamp;  }

    // ── Display helpers ───────────────────────────────────────
    public String formattedTimestamp() {
        return FORMATTER.format(timestamp);
    }

    public String formattedTimeUsed() {
        return String.format("%d:%02d", timeUsed / 60, timeUsed % 60);
    }

    @Override
    public String toString() {
        return String.format(
                "ScoreRecord{player='%s', difficulty=%s, score=%d, moves=%d, time=%s, at=%s}",
                playerName, difficulty.getDisplayName(), score, moves,
                formattedTimeUsed(), formattedTimestamp());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScoreRecord other)) return false;
        return score     == other.score
                && moves     == other.moves
                && timeUsed  == other.timeUsed
                && java.util.Objects.equals(playerName, other.playerName)
                && difficulty == other.difficulty
                && java.util.Objects.equals(timestamp,  other.timestamp);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(playerName, difficulty, score, moves, timeUsed, timestamp);
    }
}