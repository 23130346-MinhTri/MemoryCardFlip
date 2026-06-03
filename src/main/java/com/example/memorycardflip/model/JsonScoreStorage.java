package com.example.memorycardflip.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * [UC-13 — Save High Score]
 * Implementation của {@link ScoreStorage} dùng file JSON cục bộ.
 *
 * <p>File lưu tại: {@code ~/.memorycardflip/scores.json} (BR-13.4).</p>
 *
 * <p><b>UC-13 Sequence references:</b></p>
 * <ul>
 *   <li>13.1.7  — ScoreManager gọi save(cache)</li>
 *   <li>13.1.7  — JsonFile.write() ghi xuống ~/.memorycardflip/scores.json (bước 14)</li>
 *   <li>13.1.7  — JsonFile trả về "Ghi thành công" → ScoreStorage (bước 15)</li>
 *   <li>13.1.7  — ScoreStorage trả về "return success" → ScoreManager (bước 16)</li>
 *   <li>BR-13.5 — load() được gọi khi ScoreManager khởi tạo để điền cache</li>
 * </ul>
 */
public class JsonScoreStorage implements ScoreStorage {

    private static final Path SAVE_FILE = Path.of(
            System.getProperty("user.home"), ".memorycardflip", "scores.json"
    );

    // Gson với TypeAdapter cho Instant
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class,
                    (com.google.gson.JsonSerializer<Instant>)
                            (src, type, ctx) -> new com.google.gson.JsonPrimitive(src.toEpochMilli()))
            .registerTypeAdapter(Instant.class,
                    (com.google.gson.JsonDeserializer<Instant>)
                            (json, type, ctx) -> Instant.ofEpochMilli(json.getAsLong()))
            .create();

    @Override
    public void save(List<ScoreRecord> records) throws Exception {
        Files.createDirectories(SAVE_FILE.getParent());
        try (Writer w = Files.newBufferedWriter(SAVE_FILE)) {
            GSON.toJson(records, w);
        }
    }

    @Override
    public List<ScoreRecord> load() throws Exception {
        if (!Files.exists(SAVE_FILE)) return new ArrayList<>();
        try (Reader r = Files.newBufferedReader(SAVE_FILE)) {
            Type listType = new TypeToken<List<ScoreRecord>>(){}.getType();
            List<ScoreRecord> result = GSON.fromJson(r, listType);
            return result != null ? result : new ArrayList<>();
        }
    }
}