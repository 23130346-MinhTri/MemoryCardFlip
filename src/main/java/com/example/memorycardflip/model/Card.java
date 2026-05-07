package com.example.memorycardflip.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.util.Objects;

/**
 * Model đại diện cho một thẻ bài trong game.
 * Dùng JavaFX Properties để hỗ trợ binding trực tiếp lên UI.
 */
public class Card {

    private final String id;
    private final String pairId;       // ID chung của cặp (2 thẻ cùng pairId = match)
    private final CardType type;
    private final String symbol;       // Emoji/ký tự hiển thị mặt trước
    private final String imageURL;     // Đường dẫn ảnh (null nếu dùng symbol)
    private final int position;        // Vị trí trong lưới (0-based index)

    private final BooleanProperty isFlipped;
    private final BooleanProperty isMatched;

    // ── Constructor đầy đủ ───────────────────────────────────
    public Card(String id, String pairId, CardType type,
                String symbol, String imageURL, int position) {
        this.id       = Objects.requireNonNull(id, "id must not be null");
        this.pairId   = Objects.requireNonNull(pairId, "pairId must not be null");
        this.type     = Objects.requireNonNull(type, "type must not be null");
        this.symbol   = symbol;
        this.imageURL = imageURL;
        this.position = position;
        this.isFlipped = new SimpleBooleanProperty(false);
        this.isMatched = new SimpleBooleanProperty(false);
    }

    // ── Constructor tiện dùng (không có imageURL) ────────────
    public Card(String id, String pairId, CardType type, String symbol, int position) {
        this(id, pairId, type, symbol, null, position);
    }

    // ── Getters ───────────────────────────────────────────────
    public String getId()       { return id; }
    public String getPairId()   { return pairId; }
    public CardType getType()   { return type; }
    public String getSymbol()   { return symbol; }
    public String getImageURL() { return imageURL; }
    public int getPosition()    { return position; }

    public boolean isFlipped()              { return isFlipped.get(); }
    public BooleanProperty flippedProperty(){ return isFlipped; }

    public boolean isMatched()              { return isMatched.get(); }
    public BooleanProperty matchedProperty(){ return isMatched; }

    // ── Business Methods ──────────────────────────────────────

    /**
     * Lật thẻ — chỉ cho phép khi chưa matched.
     */
    public void flip() {
        if (!isMatched.get()) {
            isFlipped.set(!isFlipped.get());
        }
    }

    /**
     * Úp thẻ về (khi không ghép được cặp).
     */
    public void faceDown() {
        if (!isMatched.get()) {
            isFlipped.set(false);
        }
    }

    /**
     * Đánh dấu thẻ đã được ghép cặp thành công.
     */
    public void match() {
        isMatched.set(true);
        isFlipped.set(false); // ← đổi true → false
    }
    /**
     * Reset thẻ về trạng thái ban đầu (dùng khi restart game).
     */
    public void reset() {
        isFlipped.set(false);
        isMatched.set(false);
    }

    /**
     * Kiểm tra 2 thẻ có phải cặp không (cùng pairId, khác id).
     */
    public boolean isPairOf(Card other) {
        if (other == null) return false;
        return this.pairId.equals(other.pairId) && !this.id.equals(other.id);
    }

    /**
     * Thẻ có thể click không (chưa matched và chưa lật).
     */
    public boolean isClickable() {
        return !isMatched.get() && !isFlipped.get(); // ← chỉ chặn khi đã matched
    }

    // ── equals / hashCode dựa trên id ─────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return id.equals(card.id);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Card{id='%s', pairId='%s', symbol='%s', flipped=%s, matched=%s}",
                id, pairId, symbol, isFlipped.get(), isMatched.get());
    }
}