package com.memorycardflip.model;

import com.example.memorycardflip.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonScoreStorage Tests (UC-07 & UC-13)")
class JsonScoreStorageTest {

    @TempDir
    Path tempDir;

    private JsonScoreStorage storage;
    private Path scoreFile;

    @BeforeEach
    void setUp() {
        scoreFile = tempDir.resolve("scores.json");
        storage = new JsonScoreStorage(scoreFile);
    }

    // ==========================================================
    // save()
    // ==========================================================

    @Nested
    @DisplayName("save()")
    class SaveTests {

        @Test
        void shouldSaveEmptyList() {
            List<ScoreRecord> records = new ArrayList<>();

            assertDoesNotThrow(() -> storage.save(records));
        }

        @Test
        void shouldSaveSingleScore() throws Exception {

            List<ScoreRecord> records = new ArrayList<>();
            records.add(
                    ScoreRecord.of(
                            "TestPlayer",
                            Difficulty.EASY,
                            500,
                            10,
                            30L
                    )
            );

            storage.save(records);

            List<ScoreRecord> loaded = storage.load();

            assertEquals(1, loaded.size());
            assertEquals("TestPlayer",
                    loaded.get(0).getPlayerName());
            assertEquals(Difficulty.EASY,
                    loaded.get(0).getDifficulty());
            assertEquals(500,
                    loaded.get(0).getScore());
        }

        @Test
        void shouldSaveMultipleScores() throws Exception {

            List<ScoreRecord> records = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                records.add(
                        ScoreRecord.of(
                                "Player" + i,
                                Difficulty.EASY,
                                i * 100,
                                i,
                                i
                        )
                );
            }

            storage.save(records);

            List<ScoreRecord> loaded = storage.load();

            assertEquals(5, loaded.size());
        }

        @Test
        void shouldOverwriteOldFile() throws Exception {

            List<ScoreRecord> first = new ArrayList<>();
            first.add(
                    ScoreRecord.of(
                            "Player1",
                            Difficulty.EASY,
                            100,
                            5,
                            10L
                    )
            );

            storage.save(first);

            List<ScoreRecord> second = new ArrayList<>();
            second.add(
                    ScoreRecord.of(
                            "Player2",
                            Difficulty.HARD,
                            200,
                            10,
                            20L
                    )
            );

            storage.save(second);

            List<ScoreRecord> loaded = storage.load();

            assertEquals(1, loaded.size());
            assertEquals(
                    "Player2",
                    loaded.get(0).getPlayerName()
            );
        }

        @Test
        void shouldPreserveTimestamp() throws Exception {

            Instant now = Instant.now();

            List<ScoreRecord> records = new ArrayList<>();
            records.add(
                    new ScoreRecord(
                            "Player",
                            Difficulty.EASY,
                            100,
                            5,
                            10L,
                            now
                    )
            );

            storage.save(records);

            List<ScoreRecord> loaded = storage.load();

            assertEquals(1, loaded.size());

            long diff =
                    Math.abs(
                            loaded.get(0)
                                    .getTimestamp()
                                    .toEpochMilli()
                                    - now.toEpochMilli()
                    );

            assertTrue(diff < 100);
        }
    }

    // ==========================================================
    // load()
    // ==========================================================

    @Nested
    @DisplayName("load()")
    class LoadTests {

        @Test
        void shouldReturnEmptyListWhenFileNotExists() throws Exception {

            List<ScoreRecord> loaded = storage.load();

            assertNotNull(loaded);
            assertTrue(loaded.isEmpty());
        }

        @Test
        void shouldLoadEmptyList() throws Exception {

            storage.save(new ArrayList<>());

            List<ScoreRecord> loaded = storage.load();

            assertNotNull(loaded);
            assertTrue(loaded.isEmpty());
        }

        @Test
        void shouldLoadAndVerifyDataIntegrity() throws Exception {

            List<ScoreRecord> original = new ArrayList<>();

            original.add(
                    ScoreRecord.of(
                            "Player1",
                            Difficulty.EASY,
                            500,
                            10,
                            20L
                    )
            );

            original.add(
                    ScoreRecord.of(
                            "Player2",
                            Difficulty.HARD,
                            300,
                            15,
                            30L
                    )
            );

            storage.save(original);

            List<ScoreRecord> loaded = storage.load();

            assertEquals(
                    original.size(),
                    loaded.size()
            );

            for (int i = 0; i < original.size(); i++) {

                assertEquals(
                        original.get(i).getPlayerName(),
                        loaded.get(i).getPlayerName()
                );

                assertEquals(
                        original.get(i).getDifficulty(),
                        loaded.get(i).getDifficulty()
                );

                assertEquals(
                        original.get(i).getScore(),
                        loaded.get(i).getScore()
                );
            }
        }

        @Test
        void shouldLoadMultipleRecordsInOrder() throws Exception {

            List<ScoreRecord> records =
                    new ArrayList<>();

            for (int i = 0; i < 10; i++) {

                records.add(
                        ScoreRecord.of(
                                "Player" + i,
                                Difficulty.EASY,
                                i,
                                i,
                                i
                        )
                );
            }

            storage.save(records);

            List<ScoreRecord> loaded =
                    storage.load();

            assertEquals(10, loaded.size());

            for (int i = 0; i < 10; i++) {

                assertEquals(
                        "Player" + i,
                        loaded.get(i).getPlayerName()
                );
            }
        }

        @Test
        void shouldHandleNullListOnLoad() {

            assertDoesNotThrow(
                    () -> storage.load()
            );
        }
    }

    // ==========================================================
    // File I/O
    // ==========================================================

    @Nested
    class FileIOTests {

        @Test
        void shouldCreateParentDirectoriesIfNotExist()
                throws Exception {

            List<ScoreRecord> records =
                    new ArrayList<>();

            records.add(
                    ScoreRecord.of(
                            "Player",
                            Difficulty.EASY,
                            100,
                            5,
                            10L
                    )
            );

            storage.save(records);

            assertTrue(
                    Files.exists(scoreFile.getParent())
            );
        }

        @Test
        void shouldSaveAndLoadFromSamePath()
                throws Exception {

            JsonScoreStorage storage1 =
                    new JsonScoreStorage(scoreFile);

            JsonScoreStorage storage2 =
                    new JsonScoreStorage(scoreFile);

            List<ScoreRecord> records =
                    new ArrayList<>();

            records.add(
                    ScoreRecord.of(
                            "StorageTest",
                            Difficulty.EASY,
                            250,
                            8,
                            15L
                    )
            );

            storage1.save(records);

            List<ScoreRecord> loaded =
                    storage2.load();

            assertEquals(1, loaded.size());

            assertEquals(
                    "StorageTest",
                    loaded.get(0).getPlayerName()
            );
        }
    }

    // ==========================================================
    // JSON format
    // ==========================================================

    @Nested
    class JsonFormatTests {

        @Test
        void shouldCreateJsonFileAtCorrectLocation()
                throws Exception {

            List<ScoreRecord> records =
                    new ArrayList<>();

            records.add(
                    ScoreRecord.of(
                            "Player",
                            Difficulty.EASY,
                            100,
                            5,
                            10L
                    )
            );

            storage.save(records);

            assertTrue(
                    Files.exists(scoreFile)
            );
        }

        @Test
        void shouldCreateValidJsonFile()
                throws Exception {

            List<ScoreRecord> records =
                    new ArrayList<>();

            records.add(
                    ScoreRecord.of(
                            "Player",
                            Difficulty.EASY,
                            100,
                            5,
                            10L
                    )
            );

            storage.save(records);

            String content =
                    Files.readString(scoreFile);

            assertTrue(content.contains("["));
            assertTrue(content.contains("]"));
            assertTrue(content.contains("Player"));
        }
    }

    // ==========================================================
    // Boundary
    // ==========================================================

    @Nested
    class BoundaryTests {

        @Test
        void shouldHandleMaxIntScore()
                throws Exception {

            List<ScoreRecord> records =
                    new ArrayList<>();

            records.add(
                    ScoreRecord.of(
                            "Max",
                            Difficulty.EASY,
                            Integer.MAX_VALUE,
                            1,
                            1L
                    )
            );

            storage.save(records);

            List<ScoreRecord> loaded =
                    storage.load();

            assertEquals(
                    Integer.MAX_VALUE,
                    loaded.get(0).getScore()
            );
        }

        @Test
        void shouldHandleZeroScore()
                throws Exception {

            List<ScoreRecord> records =
                    new ArrayList<>();

            records.add(
                    ScoreRecord.of(
                            "Zero",
                            Difficulty.EASY,
                            0,
                            0,
                            0L
                    )
            );

            storage.save(records);

            List<ScoreRecord> loaded =
                    storage.load();

            assertEquals(
                    0,
                    loaded.get(0).getScore()
            );
        }
    }
}