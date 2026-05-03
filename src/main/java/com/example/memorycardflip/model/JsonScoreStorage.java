package com.example.memorycardflip.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation lưu điểm dưới dạng file JSON.
 */
public class JsonScoreStorage implements ScoreStorage {

    private static final String DATA_DIR = ".memorycardflip";
    private static final String FILE_NAME = "scores.json";

    private final Path filePath;
    private final Gson gson;

    public JsonScoreStorage() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        String userHome = System.getProperty("user.home");
        Path dataDir = Paths.get(userHome, DATA_DIR);

        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            this.filePath = dataDir.resolve(FILE_NAME);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục dữ liệu: " + dataDir, e);
        }
    }

    @Override
    public void save(List<ScoreRecord> records) throws Exception {
        try (Writer writer = new FileWriter(filePath.toFile())) {
            gson.toJson(records, writer);
        }
    }

    @Override
    public List<ScoreRecord> load() throws Exception {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(filePath.toFile())) {
            Type listType = new TypeToken<List<ScoreRecord>>() {}.getType();
            List<ScoreRecord> records = gson.fromJson(reader, listType);
            return records != null ? records : new ArrayList<>();
        }
    }

    public Path getFilePath() {
        return filePath;
    }
}