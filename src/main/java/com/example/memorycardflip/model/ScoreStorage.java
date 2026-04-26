package com.example.memorycardflip.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Interface lưu trữ điểm số.
 * Có 2 implementation: JsonScoreStorage và SQLiteScoreStorage.
 */
public interface ScoreStorage {

    /** Lưu toàn bộ danh sách records */
    void save(List<ScoreRecord> records) throws Exception;

    /** Tải toàn bộ danh sách records */
    List<ScoreRecord> load() throws Exception;

    // ── Default methods (không cần override) ─────────────────

    /** Lấy top N điểm cao nhất theo difficulty */
    default List<ScoreRecord> getTopScores(Difficulty difficulty, int limit) throws Exception {
        return load().stream()
                .filter(r -> r.difficulty() == difficulty)
                .sorted(Comparator.comparingInt(ScoreRecord::score).reversed())
                .limit(limit)
                .toList();
    }

    /** Lấy điểm cao nhất của một độ khó */
    default Optional<ScoreRecord> getHighScore(Difficulty difficulty) throws Exception {
        return load().stream()
                .filter(r -> r.difficulty() == difficulty)
                .max(Comparator.comparingInt(ScoreRecord::score));
    }

    /** Thêm một record mới rồi lưu lại */
    default void addRecord(ScoreRecord record) throws Exception {
        List<ScoreRecord> all = new java.util.ArrayList<>(load());
        all.add(record);
        save(all);
    }

    /** Xoá toàn bộ dữ liệu */
    default void clear() throws Exception {
        save(List.of());
    }
}