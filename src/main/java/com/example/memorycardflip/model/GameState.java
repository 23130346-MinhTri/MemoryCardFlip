package com.example.memorycardflip.model;

import com.example.memorycardflip.model.GameStatus;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Trạng thái toàn bộ của một ván game.
 * Dùng JavaFX Properties để Controller/UI bind trực tiếp,
 * không cần polling thủ công.
 */
public class GameState {

    // ── Core data ─────────────────────────────────────────────
    private final Difficulty difficulty;
    private final ObjectProperty<Card[]> cards;
    private final List<Card> flippedCards;      // Tối đa 2 thẻ đang lật

    // ── Stats (bindable) ──────────────────────────────────────
    private final IntegerProperty matchedPairs;
    private final IntegerProperty moves;
    private final IntegerProperty timeRemaining; // Đếm ngược từ difficulty.timeLimit
    private final ObjectProperty<GameStatus> status;

    // ── Score multiplier ──────────────────────────────────────
    private int comboCount;    // Số cặp đúng liên tiếp (cho combo bonus)

    // ── Constructor ───────────────────────────────────────────
    public GameState(Difficulty difficulty) {
        this.difficulty     = difficulty;
        this.cards          = new SimpleObjectProperty<>(new Card[0]);
        this.flippedCards   = new ArrayList<>(2);
        this.matchedPairs   = new SimpleIntegerProperty(0);
        this.moves          = new SimpleIntegerProperty(0);
        this.timeRemaining  = new SimpleIntegerProperty(difficulty.getTimeLimit());
        this.status         = new SimpleObjectProperty<>(GameStatus.IDLE);
        this.comboCount     = 0;
    }

    // ── Getters / Setters ─────────────────────────────────────
    public Difficulty getDifficulty()   { return difficulty; }

    public Card[] getCards()            { return cards.get(); }
    public ObjectProperty<Card[]> cardsProperty() { return cards; }
    public void setCards(Card[] cards)  { this.cards.set(cards); }

    public List<Card> getFlippedCards() { return flippedCards; }

    public int getMatchedPairs()        { return matchedPairs.get(); }
    public IntegerProperty matchedPairsProperty() { return matchedPairs; }
    public void setMatchedPairs(int v)  { matchedPairs.set(v); }
    public void incrementMatchedPairs() { matchedPairs.set(matchedPairs.get() + 1); }

    public int getMoves()               { return moves.get(); }
    public IntegerProperty movesProperty() { return moves; }
    public void setMoves(int v)         { moves.set(v); }
    public void incrementMoves()        { moves.set(moves.get() + 1); }

    public int getTimeRemaining()       { return timeRemaining.get(); }
    public IntegerProperty timeRemainingProperty() { return timeRemaining; }
    public void setTimeRemaining(int v) { timeRemaining.set(v); }
    public void decrementTime()         {
        int t = timeRemaining.get();
        if (t > 0) timeRemaining.set(t - 1);
    }

    public GameStatus getStatus()       { return status.get(); }
    public ObjectProperty<GameStatus> statusProperty() { return status; }
    public void setStatus(GameStatus s) { status.set(s); }

    public int getComboCount()          { return comboCount; }
    public void incrementCombo()        { comboCount++; }
    public void resetCombo()            { comboCount = 0; }

    // ── Business Methods ──────────────────────────────────────

    /** Game hoàn thành khi tất cả cặp đã ghép */
    public boolean isComplete() {
        return matchedPairs.get() == difficulty.totalPairs();
    }

    /** Hết giờ */
    public boolean isTimeUp() {
        return timeRemaining.get() <= 0;
    }

    /** Thêm thẻ vào danh sách đang lật (tối đa 2) */
    public void addFlippedCard(Card card) {
        if (flippedCards.size() < 2 && !flippedCards.contains(card)) {
            flippedCards.add(card);
        }
    }

    public void clearFlippedCards() {
        flippedCards.clear();
    }

    public boolean hasTwoFlipped() {
        return flippedCards.size() == 2;
    }

    /**
     * Tính điểm theo công thức:
     * base = matchedPairs × 100
     * combo bonus = comboCount × 20
     * move penalty = moves × 5
     * time bonus = timeRemaining × 3
     */
    public int calculateScore() {
        if (matchedPairs.get() == 0) return 0;  // ← thêm dòng này
        int base        = matchedPairs.get() * 100;
        int comboBonus  = comboCount * 20;
        int movePenalty = moves.get() * 5;
        int timeBonus   = timeRemaining.get() * 3;
        return Math.max(0, base + comboBonus - movePenalty + timeBonus);
    }

    /** Reset toàn bộ về trạng thái ban đầu cho ván mới */
    public void reset() {
        flippedCards.clear();
        matchedPairs.set(0);
        moves.set(0);
        timeRemaining.set(difficulty.getTimeLimit());
        status.set(GameStatus.IDLE);
        comboCount = 0;
    }
    @Override
    public String toString() {
        return String.format(
                "GameState{difficulty=%s, matched=%d/%d, moves=%d, time=%ds, status=%s}",
                difficulty.getDisplayName(),
                matchedPairs.get(), difficulty.totalPairs(),
                moves.get(), timeRemaining.get(), status.get()
        );
    }
}