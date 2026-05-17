package com.example.memorycardflip.model;

import javafx.beans.property.*;

/**
 * Trạng thái toàn bộ của một ván game.
 *
 * UC-10: Tính điểm (Calculate score)
 * UC-11: Streak bonus (Thưởng giữ đà)
 *
 * Chỉ giữ STATE — không xử lý logic phức tạp
 * (logic đã tách sang GameLogicService).
 *
 * FIX: Đã xóa selectCard() vì method đó tự gọi
 * incrementMatchedPairs(), incrementCombo(), resetCombo()
 * gây duplicate logic với GameLogicService.
 */
public class GameState {

    // ── Core data ─────────────────────────────────────────────
    private final Difficulty difficulty;
    private final ObjectProperty<Card[]> cards;

    // ── Stats ─────────────────────────────────────────────────
    private final IntegerProperty matchedPairs;
    private final IntegerProperty moves;

    /**
     * [UC-12]
     * Số giây còn lại của ván chơi.
     * Được GameTimerService giảm mỗi tick.
     */
    private final IntegerProperty timeRemaining;

    private final ObjectProperty<GameStatus> status;
    private final IntegerProperty wrongAttempts;

    // ── UC-11: Combo ──────────────────────────────────────────
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

        // [UC-11] Khởi tạo combo = 0
        this.comboCount    = 0;

        this.firstSelectedCard = null;
        this.isResolving   = false;
    }

    // ── Getters / Setters ─────────────────────────────────────
    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Card[] getCards() {
        return cards.get();
    }

    public void setCards(Card[] cards) {
        this.cards.set(cards);
    }

    public int getMatchedPairs() {
        return matchedPairs.get();
    }

    public void incrementMatchedPairs() {
        matchedPairs.set(matchedPairs.get() + 1);
    }

    public int getMoves() {
        return moves.get();
    }

    public void incrementMoves() {
        moves.set(moves.get() + 1);
    }

    public int getTimeRemaining() {
        return timeRemaining.get();
    }

    public void setTimeRemaining(int value) {
        timeRemaining.set(value);
    }

    /**
     * [UC-12 - Count down timer]
     * Giảm thời gian còn lại 1 giây nhưng không cho giá trị âm.
     * GameTimerService gọi hàm này sau mỗi KeyFrame 1 giây.
     */
    public void decrementTime() {
        int t = timeRemaining.get();

        if (t > 0) {
            timeRemaining.set(t - 1);
        }
    }

    public GameStatus getStatus() {
        return status.get();
    }

    public void setStatus(GameStatus status) {
        this.status.set(status);
    }

    public int getWrongAttempts() {
        return wrongAttempts.get();
    }

    public void incrementWrongAttempts() {
        wrongAttempts.set(wrongAttempts.get() + 1);
    }

    // ═══════════════════════════════════════════════════════════
    // UC-11: Streak bonus - Quản lý combo
    // ═══════════════════════════════════════════════════════════

    /**
     * [UC-11.1.3]
     * Lấy giá trị combo hiện tại.
     */
    public int getComboCount() {
        return comboCount;
    }

    /**
     * [UC-11.1.3]
     * Tăng combo lên 1 khi ghép đúng liên tiếp.
     */
    public void incrementCombo() {
        comboCount++;
    }

    /**
     * [UC-11.2.1]
     * Reset combo về 0 khi ghép sai.
     */
    public void resetCombo() {
        comboCount = 0;
    }

    // ── Selection state ───────────────────────────────────────

    public Card getFirstSelectedCard() {
        return firstSelectedCard;
    }

    public boolean isResolving() {
        return isResolving;
    }

    public void selectFirstCard(Card card) {
        this.firstSelectedCard = card;
    }

    public void setResolving(boolean resolving) {
        this.isResolving = resolving;
    }

    /**
     * [UC-09.5.5]
     * Reset trạng thái chọn thẻ sau mỗi lượt
     * (match hoặc mismatch).
     *
     * Phải gọi sau khi animation hoàn tất.
     */
    public void clearSelection() {
        firstSelectedCard = null;
        isResolving = false;
    }

    /**
     * FIX:
     * Chỉ dùng để CHECK khả năng chọn thẻ.
     * Không có side effects.
     *
     * Logic xử lý match/mismatch nằm ở
     * GameLogicService.handleSelection().
     */
    public boolean canSelectCard(Card card) {
        return !isResolving
                && !card.isMatched()
                && status.get() == GameStatus.PLAYING
                && (
                firstSelectedCard == null
                        || !firstSelectedCard.getId().equals(card.getId())
        );
    }

    // ═══════════════════════════════════════════════════════════
    // UC-10: Tính điểm (Calculate score)
    //
    // Công thức:
    // score =
    // (matchedPairs × 100)
    // + (comboCount × 20)
    // - (moves × 5)
    // + min(timeRemaining, 30) × 3
    // ═══════════════════════════════════════════════════════════

    /**
     * [UC-10.1.3 - UC-10.1.9]
     * Tính điểm số hiện tại.
     *
     * @return điểm số không âm
     */
    public int calculateScore() {

        // Nếu chưa ghép được cặp nào thì trả về 0
        if (matchedPairs.get() == 0) {
            return 0;
        }

        // [UC-10.1.4]
        // Điểm cơ bản: mỗi cặp đúng +100
        int baseScore = matchedPairs.get() * 100;

        // [UC-10.1.5]
        // Phạt lượt: mỗi lượt -5 điểm
        int movePenalty = moves.get() * 5;

        // [UC-10.1.6]
        // Thưởng thời gian:
        // mỗi giây còn lại +3 điểm
        // tối đa tính 30 giây
        int timeBonus = Math.min(timeRemaining.get(), 30) * 3;

        // [UC-10.1.7]
        // Thưởng combo:
        // mỗi cấp combo +20 điểm
        int comboBonus = comboCount * 20;

        // [UC-10.1.8]
        // Tổng hợp điểm
        int score =
                baseScore
                        + comboBonus
                        - movePenalty
                        + timeBonus;

        // [UC-10.1.9]
        // Đảm bảo điểm không âm
        return Math.max(0, score);
    }

    // ── State check ───────────────────────────────────────────

    /**
     * Kiểm tra đã hoàn thành toàn bộ cặp thẻ chưa.
     */
    public boolean isComplete() {
        return matchedPairs.get() == difficulty.totalPairs();
    }

    /**
     * [UC-12 - Count down timer]
     * Kiểm tra điều kiện hết giờ.
     * GameController sẽ chuyển sang trạng thái LOST.
     */
    public boolean isTimeUp() {
        return timeRemaining.get() <= 0;
    }

    // ── Reset (UC-04 Play again) ─────────────────────────────

    /**
     * [UC-04 - Play again]
     * Reset toàn bộ trạng thái game về ban đầu:
     *
     * - matched pairs
     * - số lượt
     * - timer
     * - combo
     * - số lần sai
     * - trạng thái chọn thẻ
     * - trạng thái xử lý
     */
    public void reset() {

        matchedPairs.set(0);

        moves.set(0);

        // [UC-04][UC-12]
        // Khi chơi lại:
        // timer quay về giới hạn của difficulty
        timeRemaining.set(difficulty.getTimeLimit());

        status.set(GameStatus.IDLE);

        // [UC-11]
        // Reset combo
        comboCount = 0;

        wrongAttempts.set(0);

        firstSelectedCard = null;

        isResolving = false;
    }
}