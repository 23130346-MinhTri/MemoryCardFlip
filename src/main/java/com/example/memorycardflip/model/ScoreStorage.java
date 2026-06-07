package com.example.memorycardflip.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface ScoreStorage {

    void save(List<ScoreRecord> records) throws Exception;

    List<ScoreRecord> load() throws Exception;

    // ── Default methods ───────────────────────────────────────

    default List<ScoreRecord> getTopScores(Difficulty difficulty, int limit) throws Exception {
        return load().stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .sorted(Comparator.comparingInt(ScoreRecord::getScore).reversed())
                .limit(limit)
                .toList();
    }

    default Optional<ScoreRecord> getHighScore(Difficulty difficulty) throws Exception {
        return load().stream()
                .filter(r -> r.getDifficulty() == difficulty)
                .max(Comparator.comparingInt(ScoreRecord::getScore));
    }

    default void addRecord(ScoreRecord record) throws Exception {
        List<ScoreRecord> all = new ArrayList<>(load());
        all.add(record);
        save(all);
    }

    default void clear() throws Exception {
        save(List.of());
    }
}