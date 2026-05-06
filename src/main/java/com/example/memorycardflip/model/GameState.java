package com.example.memorycardflip.model;

import javafx.beans.property.*;

/**
 * Trạng thái toàn bộ của một ván game.
 * Chỉ giữ STATE — không xử lý logic phức tạp (đã tách sang GameLogicService).
 *
 * FIX: Đã xóa selectCard() vì method đó tự gọi incrementMatchedPairs(),
 * incrementCombo(), resetCombo() — duplicate hoàn toàn với GameLogicService
 * và gây double-count khi cả hai cùng chạy.
 */
public class GameState {

    // ── Core data ─────────────────────────────────────────────
    private final Difficulty difficulty;
    private final ObjectProperty<Card[]> cards;

    // ── Stats ─────────────────────────────────────────────────
    private final IntegerProperty matchedPairs;
    private final IntegerProperty moves;
    private final IntegerProperty timeRemaining;
    private final ObjectProperty<GameStatus> status;
    private final IntegerProperty wrongAttempts;

    // ── Combo ─────────────────────────────────────────────────
    private int comboCount;

    // ── Selection state ───────────────────────────────────────
    private Card firstSelectedCard;
    private boolean isResolving;

    // ── Constructor ───────────────────────────────────────────
    public GameState(Difficulty difficulty) {
        this.difficulty    = difficulty;
        this.cards         = new SimpleObjectProperty<>(new Card[0]);
        this.matchedPairs  = new SimpleIntegerProperty(0);
        this.moves         = new SimpleIntegerProperty(0);
        this.timeRemaining = new SimpleIntegerProperty(difficulty.getTimeLimit());
        this.status        = new SimpleObjectProperty<>(GameStatus.IDLE);
        this.wrongAttempts = new SimpleIntegerProperty(0);
        this.comboCount    = 0;
        this.firstSelectedCard = null;
        this.isResolving   = false;
    }

    // ── Getter / Setter ───────────────────────────────────────
    public Difficulty getDifficulty()  { return difficulty; }

    public Card[] getCards()           { return cards.get(); }
    public void setCards(Card[] cards) { this.cards.set(cards); }

    public int getMatchedPairs()             { return matchedPairs.get(); }
    public void incrementMatchedPairs()      { matchedPairs.set(matchedPairs.get() + 1); }

    public int getMoves()                    { return moves.get(); }
    public void incrementMoves()             { moves.set(moves.get() + 1); }

    public int getTimeRemaining()            { return timeRemaining.get(); }
    public void setTimeRemaining(int v)      { timeRemaining.set(v); }
    public void decrementTime() {
        int t = timeRemaining.get();
        if (t > 0) timeRemaining.set(t - 1);
    }

    public GameStatus getStatus()            { return status.get(); }
    public void setStatus(GameStatus s)      { status.set(s); }

    public int getWrongAttempts()            { return wrongAttempts.get(); }
    public void incrementWrongAttempts()     { wrongAttempts.set(wrongAttempts.get() + 1); }

    public int getComboCount()               { return comboCount; }
    public void incrementCombo()             { comboCount++; }
    public void resetCombo()                 { comboCount = 0; }

    // ── Selection state (chỉ read, mutate qua GameLogicService) ──
    public Card getFirstSelectedCard()       { return firstSelectedCard; }
    public boolean isResolving()             { return isResolving; }

    public void selectFirstCard(Card card) {
        this.firstSelectedCard = card;
    }

    /**
     * FIX: Chỉ dùng để CHECK — không có side effects.
     * Logic xử lý match/mismatch ở GameLogicService.handleSelection().
     */
    public boolean canSelectCard(Card card) {
        return !isResolving
                && !card.isMatched()
                && status.get() == GameStatus.PLAYING
                && (firstSelectedCard == null || !firstSelectedCard.getId().equals(card.getId()));
    }

    public void setResolving(boolean resolving) {
        this.isResolving = resolving;
    }

    /**
     * Reset trạng thái chọn thẻ sau mỗi lượt (match hoặc mismatch).
     * Phải gọi sau khi animation hoàn tất.
     */
    public void clearSelection() {
        firstSelectedCard = null;
        isResolving = false;
    }

    // ── Score ─────────────────────────────────────────────────
    public int calculateScore() {
        if (matchedPairs.get() == 0) return 0;
        int base       = matchedPairs.get() * 100;
        int comboBonus = comboCount * 20;
        int movePenalty = moves.get() * 5;
        int timeBonus  = Math.min(timeRemaining.get(), 30) * 3;
        return Math.max(0, base + comboBonus - movePenalty + timeBonus);
    }

    // ── State check ───────────────────────────────────────────
    public boolean isComplete() {
        return matchedPairs.get() == difficulty.totalPairs();
    }

    public boolean isTimeUp() {
        return timeRemaining.get() <= 0;
    }

    // ── Reset ─────────────────────────────────────────────────
    public void reset() {
        matchedPairs.set(0);
        moves.set(0);
        timeRemaining.set(difficulty.getTimeLimit());
        status.set(GameStatus.IDLE);
        comboCount = 0;
        wrongAttempts.set(0);
        firstSelectedCard = null;
        isResolving = false;
    }
}