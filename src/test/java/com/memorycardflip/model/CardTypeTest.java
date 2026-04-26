package com.memorycardflip.model;

import com.example.memorycardflip.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho CardType enum.
 */
@DisplayName("CardType Enum Tests")
class CardTypeTest {

    @ParameterizedTest
    @EnumSource(CardType.class)
    @DisplayName("Mỗi loại có ít nhất 16 symbol")
    void shouldHaveAtLeast16Symbols(CardType type) {
        assertTrue(type.symbolCount() >= 16);
    }

    @ParameterizedTest
    @EnumSource(CardType.class)
    @DisplayName("displayName không null/rỗng")
    void displayNameShouldNotBeBlank(CardType type) {
        assertNotNull(type.getDisplayName());
        assertFalse(type.getDisplayName().isBlank());
    }

    @Test
    @DisplayName("getSymbol(0) trả về symbol đầu tiên")
    void shouldReturnFirstSymbol() {
        assertNotNull(CardType.ANIMAL.getSymbol(0));
    }

    @Test
    @DisplayName("getSymbol(index âm) → ném IndexOutOfBoundsException")
    void negativeIndexShouldThrow() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> CardType.ANIMAL.getSymbol(-1));
    }

    @Test
    @DisplayName("getSymbol(index vượt bound) → ném IndexOutOfBoundsException")
    void outOfBoundsIndexShouldThrow() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> CardType.ANIMAL.getSymbol(999));
    }

    @Test
    @DisplayName("getSymbols() trả về bản sao (không ảnh hưởng bản gốc)")
    void getSymbolsShouldReturnCopy() {
        String[] symbols = CardType.FRUIT.getSymbols();
        symbols[0] = "MODIFIED";
        assertNotEquals("MODIFIED", CardType.FRUIT.getSymbol(0));
    }
}