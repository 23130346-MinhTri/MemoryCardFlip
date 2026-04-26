package com.memorycardflip.model;

import com.example.memorycardflip.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho Difficulty enum.
 */
@DisplayName("Difficulty Enum Tests")
class DifficultyTest {

    @Test
    @DisplayName("EASY: grid=4, time=60, pairs=8, cells=16")
    void easyShouldHaveCorrectValues() {
        assertAll(
                () -> assertEquals(4,  Difficulty.EASY.getGridSize()),
                () -> assertEquals(60, Difficulty.EASY.getTimeLimit()),
                () -> assertEquals(8,  Difficulty.EASY.totalPairs()),
                () -> assertEquals(16, Difficulty.EASY.totalCells())
        );
    }

    @Test
    @DisplayName("MEDIUM: grid=6, time=90, pairs=18, cells=36")
    void mediumShouldHaveCorrectValues() {
        assertAll(
                () -> assertEquals(6,  Difficulty.MEDIUM.getGridSize()),
                () -> assertEquals(90, Difficulty.MEDIUM.getTimeLimit()),
                () -> assertEquals(18, Difficulty.MEDIUM.totalPairs()),
                () -> assertEquals(36, Difficulty.MEDIUM.totalCells())
        );
    }

    @Test
    @DisplayName("HARD: grid=8, time=120, pairs=32, cells=64")
    void hardShouldHaveCorrectValues() {
        assertAll(
                () -> assertEquals(8,   Difficulty.HARD.getGridSize()),
                () -> assertEquals(120, Difficulty.HARD.getTimeLimit()),
                () -> assertEquals(32,  Difficulty.HARD.totalPairs()),
                () -> assertEquals(64,  Difficulty.HARD.totalCells())
        );
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    @DisplayName("totalCells() = gridSize * gridSize với mọi độ khó")
    void totalCellsShouldEqualGridSizeSquared(Difficulty d) {
        assertEquals(d.getGridSize() * d.getGridSize(), d.totalCells());
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    @DisplayName("totalPairs() = totalCells() / 2 với mọi độ khó")
    void totalPairsShouldBeHalfTotalCells(Difficulty d) {
        assertEquals(d.totalCells() / 2, d.totalPairs());
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    @DisplayName("displayName không null/rỗng")
    void displayNameShouldNotBeBlank(Difficulty d) {
        assertNotNull(d.getDisplayName());
        assertFalse(d.getDisplayName().isBlank());
    }
}