package com.example.memorycardflip.service;

import com.example.memorycardflip.model.Card;
import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;

/**
 * Service xử lý logic ghép cặp thẻ trong ván chơi.
 *
 * UC-07: Kiểm tra cặp thẻ (Check card pair)
 * UC-11: Streak bonus (Thưởng giữ đà) - quản lý combo
 *
 * UC-10: Tính điểm (Calculate score)
 */
public class GameLogicService {

    public interface Listener {

        void onFirstCardSelected(Card firstCard);

        void onMatch(
                Card firstCard,
                Card secondCard,
                int matchedPairs,
                int totalPairs,
                int comboCount
        );

        void onMismatch(Card firstCard, Card secondCard);
    }

    private final GameState gameState;
    private int totalPairs;

    // ── Constructor ───────────────────────────────────────────
    public GameLogicService(GameState gameState, int totalPairs) {
        this.gameState = gameState;
        this.totalPairs = totalPairs;
    }

    /**
     * [UC-04 - Play again]
     * Cập nhật lại tổng số cặp khi ván mới được tạo
     * hoặc bàn chơi được dựng lại.
     */
    public void reset(int totalPairs) {
        this.totalPairs = totalPairs;
    }

    // ═══════════════════════════════════════════════════════════
    // UC-07.1: Kiểm tra thẻ có thể được chọn hay không
    // ═══════════════════════════════════════════════════════════

    /**
     * [UC-07.1]
     * Kiểm tra một thẻ có thể được chọn hay không.
     */
    public boolean canSelect(Card card) {

        // Đã matched thì không được chọn
        if (card.isMatched()) {
            return false;
        }

        // Đang xử lý animation thì khóa chọn
        if (gameState.isResolving()) {
            return false;
        }

        // Không cho chọn lại chính thẻ đầu tiên
        Card first = gameState.getFirstSelectedCard();

        if (first != null && first.getId().equals(card.getId())) {
            return false;
        }

        // Chỉ cho chọn khi game đang PLAYING
        return gameState.getStatus() == GameStatus.PLAYING;
    }

    // ═══════════════════════════════════════════════════════════
    // UC-07.2: Xử lý chọn thẻ và kiểm tra cặp
    // UC-11 : Combo / Streak bonus
    // ═══════════════════════════════════════════════════════════

    /**
     * [UC-07.2]
     * Xử lý lựa chọn thẻ và kiểm tra cặp.
     *
     * [UC-11]
     * Tăng/reset combo khi match hoặc mismatch.
     *
     * @param card     thẻ được chọn
     * @param listener callback nhận kết quả xử lý
     */
    public void handleSelection(Card card, Listener listener) {

        // Không hợp lệ thì bỏ qua
        if (!canSelect(card)) {
            return;
        }

        Card first = gameState.getFirstSelectedCard();

        // ──────────────────────────────────────────────────────
        // [UC-07.2.1]
        // Chưa có thẻ nào -> đây là thẻ đầu tiên
        // ──────────────────────────────────────────────────────
        if (first == null) {

            gameState.selectFirstCard(card);

            listener.onFirstCardSelected(card);

            return;
        }

        // ──────────────────────────────────────────────────────
        // [UC-07.2.2]
        // Đã có thẻ đầu -> xử lý thẻ thứ hai
        // ──────────────────────────────────────────────────────
        gameState.setResolving(true);

        // Tăng số lượt chơi
        // (ảnh hưởng UC-10 calculate score)
        gameState.incrementMoves();

        // ──────────────────────────────────────────────────────
        // [UC-07.2.3]
        // So sánh pairId của hai thẻ
        // ──────────────────────────────────────────────────────
        if (first.isPairOf(card)) {

            // ═══════════════════════════════════════════════════
            // UC-11: LUỒNG CHÍNH - GHÉP ĐÚNG
            // ═══════════════════════════════════════════════════

            // [UC-11.1.2]
            // Xác định cặp match thành công

            // [UC-11.1.3]
            // Tăng combo
            gameState.incrementCombo();

            // [UC-07.2.4]
            // Tăng số cặp ghép đúng
            gameState.incrementMatchedPairs();

            // [UC-11.1.4]
            // Combo bonus sẽ được tính trong:
            // GameState.calculateScore()
            //
            // comboBonus = comboCount × 20

            // [UC-11.1.5]
            // Trả kết quả về listener
            listener.onMatch(
                    first,
                    card,
                    gameState.getMatchedPairs(),
                    totalPairs,
                    gameState.getComboCount()
            );

        } else {

            // ═══════════════════════════════════════════════════
            // UC-11: LUỒNG THAY THẾ - GHÉP SAI
            // ═══════════════════════════════════════════════════

            // [UC-11.2.1]
            // Reset combo về 0
            gameState.resetCombo();

            // Tăng số lần ghép sai
            gameState.incrementWrongAttempts();

            // Không có thưởng combo
            listener.onMismatch(first, card);
        }
    }

    // ── Clear selection ───────────────────────────────────────

    /**
     * Gọi sau khi animation match/mismatch hoàn tất.
     *
     * Reset:
     * - firstSelectedCard
     * - isResolving
     *
     * để board có thể tiếp tục nhận click.
     */
    public void clearSelection() {

        // FIX:
        // clearSelection phải luôn reset cả:
        // - firstSelectedCard
        // - isResolving
        //
        // Nếu resolving bị kẹt true
        // board sẽ bị khóa vĩnh viễn.
        gameState.clearSelection();
    }

    // ── Getter ────────────────────────────────────────────────

    public int getMatchedPairs() {
        return gameState.getMatchedPairs();
    }

    public int getTotalPairs() {
        return totalPairs;
    }
}