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
    // [UC12] Số giây còn lại của ván chơi, được GameTimerService giảm mỗi tick.
    private final IntegerProperty timeRemaining;
    private final ObjectProperty<GameStatus> status;
    private final IntegerProperty wrongAttempts;

    // ── Combo ─────────────────────────────────────────────────
    private int comboCount;

    // ── Selection state ───────────────────────────────────────
    private Card firstSelectedCard;
    private boolean isResolving;

    // ── Hint state ────────────────────────────────────────────
    private int hintCount;
    private int hintPenalty;


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
        this.hintCount     = 3;
        this.hintPenalty   = 0;
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

    /**
     * [UC12 - Count down timer]
     * Giảm thời gian còn lại 1 giây nhưng không cho giá trị âm.
     * GameTimerService gọi hàm này sau mỗi KeyFrame 1 giây.
     */
    public void decrementTime() {
        int t = timeRemaining.get();
        if (t > 0) timeRemaining.set(t - 1);
    }

    public GameStatus getStatus()            { return status.get(); }
    public void setStatus(GameStatus s)      { status.set(s); }

    public int getWrongAttempts()            { return wrongAttempts.get(); }
    public void incrementWrongAttempts()     { wrongAttempts.set(wrongAttempts.get() + 1); }

    /**
     * [11.1.3] Trả về comboCount hiện tại sau khi hệ thống đã tăng combo.
     */
    public int getComboCount()               { return comboCount; }

    /**
     * [11.1.3] Hệ thống gọi khi ghép đúng một cặp thẻ để tăng comboCount lên 1.
     */
    public void incrementCombo()             { comboCount++; }

    /**
     * [11.2.1] Hệ thống gọi khi ghép sai cặp để reset comboCount về 0.
     */
    public void resetCombo()                 { comboCount = 0; }

    // ── Selection state (chỉ read, mutate qua GameLogicService) ──
    public Card getFirstSelectedCard()       { return firstSelectedCard; }
    public boolean isResolving()             { return isResolving; }

    // ── Hint state ────────────────────────────────────────────
    public int getHintCount()                { return hintCount; }
    public int getHintPenalty()              { return hintPenalty; }
    public void useHint() {
        if (hintCount > 0) {
            hintCount--;
            hintPenalty += 50;
        }
    }

    /**
     * [UC2 v2.0] Tìm cặp thẻ chưa ghép khớp đầu tiên trên bàn chơi.
     */
    public Card[] findFirstUnmatchedPair() {
        Card[] cardList = getCards();
        if (cardList == null || cardList.length == 0) return null;

        for (int i = 0; i < cardList.length; i++) {
            Card c1 = cardList[i];
            if (c1.isMatched()) continue;
            for (int j = i + 1; j < cardList.length; j++) {
                Card c2 = cardList[j];
                if (c2.isMatched()) continue;
                if (c1.getPairId().equals(c2.getPairId())) {
                    return new Card[] { c1, c2 };
                }
            }
        }
        return null;
    }


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

    /** 9.5.5
     * Reset trạng thái chọn thẻ sau mỗi lượt (match hoặc mismatch).
     * Phải gọi sau khi animation hoàn tất.
     */
    public void clearSelection() {
        firstSelectedCard = null;
        isResolving = false;
    }

    // ── Score ─────────────────────────────────────────────────
    /**
     * [11.1.4] Tính điểm thưởng combo theo công thức comboCount × 20.
     * Giá trị này được cộng vào điểm tổng khi GameController cập nhật HUD.
     */
    public int calculateScore() {
        if (matchedPairs.get() == 0) return 0;
        int base       = matchedPairs.get() * 100;
        int comboBonus = comboCount * 20;
        int movePenalty = moves.get() * 5;
        int timeBonus  = Math.min(timeRemaining.get(), 30) * 3;
        return Math.max(0, base + comboBonus - movePenalty + timeBonus - hintPenalty);
    }

    // ── State check ───────────────────────────────────────────
    public boolean isComplete() {
        return matchedPairs.get() == difficulty.totalPairs();
    }

    /**
     * [UC12 - Count down timer]
     * Kiểm tra điều kiện hết giờ để GameController chuyển sang trạng thái LOST.
     */
    public boolean isTimeUp() {
        return timeRemaining.get() <= 0;
    }

    // ── Reset ─────────────────────────────────────────────────
    /**
     * [UC4 - Play again]
     * Đưa toàn bộ trạng thái ván chơi về ban đầu khi người chơi bấm "Chơi lại":
     * số cặp, lượt, timer, combo, lỗi, thẻ đang chọn và trạng thái xử lý.
     */
    /**
     * [11.2.1] Khi bắt đầu ván mới hoặc chơi lại, toàn bộ trạng thái combo được reset.
     */
    public void reset() {
        matchedPairs.set(0);
        moves.set(0);
        // [UC4][UC12] Khi bắt đầu/chơi lại ván, timer quay về giới hạn của độ khó.
        timeRemaining.set(difficulty.getTimeLimit());
        status.set(GameStatus.IDLE);
        comboCount = 0;
        wrongAttempts.set(0);
        firstSelectedCard = null;
        isResolving = false;
        hintCount = 3;
        hintPenalty = 0;
    }

    public void setDifficulty(Difficulty difficulty) {
    }
}
