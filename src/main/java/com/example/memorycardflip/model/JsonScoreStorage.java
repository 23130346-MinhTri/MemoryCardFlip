package com.example.memorycardflip.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JsonScoreStorage implements ScoreStorage {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(
                    Instant.class,
                    (JsonSerializer<Instant>)
                            (src, type, ctx) ->
                                    new JsonPrimitive(src.toEpochMilli()))
            .registerTypeAdapter(
                    Instant.class,
                    (JsonDeserializer<Instant>)
                            (json, type, ctx) ->
                                    Instant.ofEpochMilli(json.getAsLong()))
            .create();

    private final Path saveFile;

    public JsonScoreStorage() {
        this(Path.of(
                System.getProperty("user.home"),
                ".memorycardflip",
                "scores.json"
        ));
    }

    public JsonScoreStorage(Path saveFile) {
        this.saveFile = saveFile;
    }

    @Override
    public void save(List<ScoreRecord> records) throws Exception {

        Files.createDirectories(saveFile.getParent());

        try (Writer writer = Files.newBufferedWriter(saveFile)) {
            GSON.toJson(records, writer);
        }
    }

    @Override
    public List<ScoreRecord> load() throws Exception {

        if (!Files.exists(saveFile)) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(saveFile)) {

            Type listType =
                    new TypeToken<List<ScoreRecord>>() {
                    }.getType();

            List<ScoreRecord> result =
                    GSON.fromJson(reader, listType);

            return result != null
                    ? result
                    : new ArrayList<>();
        }
    }
}