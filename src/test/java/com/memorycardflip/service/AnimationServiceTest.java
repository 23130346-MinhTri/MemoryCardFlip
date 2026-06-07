package com.memorycardflip.service;

import com.example.memorycardflip.service.AnimationService;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnimationService Tests")
class AnimationServiceTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already started
        }
    }

    private Pane parentPane;

    @BeforeEach
    void setUp() {
        parentPane = new Pane();
    }

    @Test
    @DisplayName("Ghép đúng bình thường (combo < 2) -> Sinh đúng 15 hạt tròn tại tọa độ truyền vào")
    void testSpawnParticlesForNormalMatch() {
        double testX = 150.0;
        double testY = 200.0;

        AnimationService.spawnSparkleParticles(parentPane, testX, testY, 1);

        // Đảm bảo sinh đúng 15 hạt
        assertEquals(15, parentPane.getChildren().size());

        // Kiểm tra từng hạt
        for (var node : parentPane.getChildren()) {
            assertTrue(node instanceof Circle);
            Circle c = (Circle) node;

            // Xác định hạt sinh ra tại tọa độ (x, y)
            assertEquals(testX, c.getLayoutX());
            assertEquals(testY, c.getLayoutY());

            // Kiểm tra màu sắc thuộc tập màu ghép đúng thường: Gold, Light Yellow, Deep Sky Blue, White
            Color fill = (Color) c.getFill();
            assertNotNull(fill);

            boolean isValidColor = isColorMatch(fill, "#FFD700")  // Gold
                    || isColorMatch(fill, "#FFFFE0")              // Light Yellow
                    || isColorMatch(fill, "#00BFFF")              // Deep Sky Blue
                    || fill.equals(Color.WHITE);

            assertTrue(isValidColor, "Màu hạt không thuộc danh sách ghép đúng thường: " + fill);
        }
    }

    @Test
    @DisplayName("Ghép combo cao (combo >= 2) -> Sinh gấp đôi số hạt (30 hạt) với màu sắc rực rỡ")
    void testSpawnParticlesForHighCombo() {
        double testX = 100.0;
        double testY = 100.0;

        AnimationService.spawnSparkleParticles(parentPane, testX, testY, 3);

        // Đảm bảo sinh đúng 30 hạt
        assertEquals(30, parentPane.getChildren().size());

        // Kiểm tra từng hạt
        for (var node : parentPane.getChildren()) {
            assertTrue(node instanceof Circle);
            Circle c = (Circle) node;

            assertEquals(testX, c.getLayoutX());
            assertEquals(testY, c.getLayoutY());

            // Kiểm tra màu sắc thuộc tập màu combo: Orange Red, Deep Pink, Dark Violet, Gold
            Color fill = (Color) c.getFill();
            assertNotNull(fill);

            boolean isValidColor = isColorMatch(fill, "#FF4500")  // Orange Red
                    || isColorMatch(fill, "#FF1493")              // Deep Pink
                    || isColorMatch(fill, "#9400D3")              // Dark Violet
                    || isColorMatch(fill, "#FFD700");             // Gold

            assertTrue(isValidColor, "Màu hạt không thuộc danh sách combo: " + fill);
        }
    }

    @Test
    @DisplayName("Truyền Pane null -> không gây lỗi sập ứng dụng")
    void testSpawnParticlesWithNullPane() {
        assertDoesNotThrow(() -> {
            AnimationService.spawnSparkleParticles(null, 100.0, 100.0, 1);
        });
    }

    private boolean isColorMatch(Color color, String hex) {
        Color expected = Color.web(hex);
        return Math.abs(color.getRed() - expected.getRed()) < 0.01
                && Math.abs(color.getGreen() - expected.getGreen()) < 0.01
                && Math.abs(color.getBlue() - expected.getBlue()) < 0.01;
    }
}
