package com.memorycardflip.model;

import com.example.memorycardflip.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho class Card.
 * Kiểm tra toàn bộ trạng thái, business logic và edge cases.
 */
@DisplayName("Card Model Tests")
class CardTest {

    private Card card;
    private Card pairCard;

    @BeforeEach
    void setUp() {
        card     = new Card("c1", "pair-A", CardType.ANIMAL, "🐶", 0);
        pairCard = new Card("c2", "pair-A", CardType.ANIMAL, "🐶", 1);
    }

    // ── Constructor & Getters ─────────────────────────────────

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Khởi tạo đúng giá trị mặc định")
        void shouldInitializeWithDefaultValues() {
            assertAll(
                    () -> assertEquals("c1",      card.getId()),
                    () -> assertEquals("pair-A",  card.getPairId()),
                    () -> assertEquals(CardType.ANIMAL, card.getType()),
                    () -> assertEquals("🐶",      card.getSymbol()),
                    () -> assertEquals(0,          card.getPosition()),
                    () -> assertFalse(card.isFlipped()),
                    () -> assertFalse(card.isMatched())
            );
        }

        @Test
        @DisplayName("Ném NullPointerException khi id null")
        void shouldThrowWhenIdIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new Card(null, "pair-A", CardType.ANIMAL, "🐶", 0));
        }

        @Test
        @DisplayName("Ném NullPointerException khi pairId null")
        void shouldThrowWhenPairIdIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new Card("c1", null, CardType.ANIMAL, "🐶", 0));
        }

        @Test
        @DisplayName("Ném NullPointerException khi type null")
        void shouldThrowWhenTypeIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new Card("c1", "pair-A", null, "🐶", 0));
        }
    }

    // ── flip() ────────────────────────────────────────────────

    @Nested
    @DisplayName("flip()")
    class FlipTests {

        @Test
        @DisplayName("Lật thẻ từ úp → ngửa")
        void shouldFlipFaceUp() {
            card.flip();
            assertTrue(card.isFlipped());
        }

        @Test
        @DisplayName("Lật thẻ 2 lần → về úp")
        void shouldFlipBackFaceDown() {
            card.flip();
            card.flip();
            assertFalse(card.isFlipped());
        }

        @Test
        @DisplayName("Không thể lật thẻ đã matched")
        void shouldNotFlipMatchedCard() {
            card.match();
            card.flip(); // Không có hiệu lực
            assertFalse(card.isFlipped());
        }

        @Test
        @DisplayName("JavaFX Property phản ánh đúng trạng thái")
        void flippedPropertyShouldReflectState() {
            assertFalse(card.flippedProperty().get());
            card.flip();
            assertTrue(card.flippedProperty().get());
        }
    }

    // ── faceDown() ────────────────────────────────────────────

    @Nested
    @DisplayName("faceDown()")
    class FaceDownTests {

        @Test
        @DisplayName("Úp thẻ đang ngửa về lại")
        void shouldFaceDownFlippedCard() {
            card.flip();
            card.faceDown();
            assertFalse(card.isFlipped());
        }

        @Test
        @DisplayName("Thẻ đã matched không bị úp")
        void shouldNotFaceDownMatchedCard() {
            card.flip();
            card.match();
            card.faceDown();
            // matched card không thay đổi trạng thái flipped
            assertFalse(card.isMatched() && card.isFlipped()); // vẫn matched, không flipped
        }
    }

    // ── match() ───────────────────────────────────────────────

    @Nested
    @DisplayName("match()")
    class MatchTests {

        @Test
        @DisplayName("Match thành công → isMatched = true")
        void shouldSetMatchedTrue() {
            card.match();
            assertTrue(card.isMatched());
        }

        @Test
        @DisplayName("Sau match → isFlipped = false")
        void shouldSetFlippedFalseAfterMatch() {
            card.flip();
            card.match();
            assertFalse(card.isFlipped());
        }

        @Test
        @DisplayName("JavaFX matchedProperty phản ánh đúng")
        void matchedPropertyShouldReflectState() {
            assertFalse(card.matchedProperty().get());
            card.match();
            assertTrue(card.matchedProperty().get());
        }
    }

    // ── reset() ───────────────────────────────────────────────

    @Nested
    @DisplayName("reset()")
    class ResetTests {

        @Test
        @DisplayName("Reset sau khi flip → về FACE_DOWN")
        void shouldResetFlippedCard() {
            card.flip();
            card.reset();
            assertFalse(card.isFlipped());
        }

        @Test
        @DisplayName("Reset sau khi match → về trạng thái ban đầu")
        void shouldResetMatchedCard() {
            card.flip();
            card.match();
            card.reset();
            assertAll(
                    () -> assertFalse(card.isFlipped()),
                    () -> assertFalse(card.isMatched())
            );
        }
    }

    // ── isPairOf() ────────────────────────────────────────────

    @Nested
    @DisplayName("isPairOf()")
    class IsPairOfTests {

        @Test
        @DisplayName("2 thẻ cùng pairId, khác id → là cặp")
        void shouldReturnTrueForMatchingPair() {
            assertTrue(card.isPairOf(pairCard));
        }

        @Test
        @DisplayName("2 thẻ cùng id → không phải cặp (cùng object)")
        void shouldReturnFalseForSameCard() {
            assertFalse(card.isPairOf(card));
        }

        @Test
        @DisplayName("2 thẻ khác pairId → không phải cặp")
        void shouldReturnFalseForDifferentPairId() {
            Card other = new Card("c3", "pair-B", CardType.ANIMAL, "🐱", 2);
            assertFalse(card.isPairOf(other));
        }

        @Test
        @DisplayName("isPairOf(null) → false, không throw")
        void shouldReturnFalseForNull() {
            assertFalse(card.isPairOf(null));
        }
    }

    // ── isClickable() ─────────────────────────────────────────

    @Nested
    @DisplayName("isClickable()")
    class IsClickableTests {

        @Test
        @DisplayName("Thẻ mới → có thể click")
        void newCardShouldBeClickable() {
            assertTrue(card.isClickable());
        }

        @Test
        @DisplayName("Thẻ đang ngửa → không thể click")
        void flippedCardShouldNotBeClickable() {
            card.flip();
            assertFalse(card.isClickable());
        }

        @Test
        @DisplayName("Thẻ đã matched → không thể click")
        void matchedCardShouldNotBeClickable() {
            card.match();
            assertFalse(card.isClickable());
        }
    }

    // ── equals / hashCode ─────────────────────────────────────

    @Nested
    @DisplayName("equals() và hashCode()")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Cùng id → bằng nhau")
        void shouldBeEqualWhenSameId() {
            Card duplicate = new Card("c1", "pair-A", CardType.ANIMAL, "🐶", 0);
            assertEquals(card, duplicate);
        }

        @Test
        @DisplayName("Khác id → không bằng nhau")
        void shouldNotBeEqualWhenDifferentId() {
            assertNotEquals(card, pairCard);
        }

        @Test
        @DisplayName("hashCode nhất quán với equals")
        void hashCodeShouldBeConsistentWithEquals() {
            Card duplicate = new Card("c1", "pair-A", CardType.ANIMAL, "🐶", 0);
            assertEquals(card.hashCode(), duplicate.hashCode());
        }
    }
}