package com.example.memorycardflip.service;

import com.example.memorycardflip.model.Card;
import com.example.memorycardflip.model.GameState;

/**
 * Service xử lý logic ghép cặp thẻ trong ván chơi.
 *
 * <p>Use Case phụ trách:</p>
 * <ul>
 *   <li>[UC-07] Kiểm tra cặp thẻ</li>
 * </ul>
 */
public class GameLogicService {

    public interface Listener {
        void onFirstCardSelected(Card firstCard);
        void onMatch(Card firstCard, Card secondCard, int matchedPairs, int totalPairs, int comboCount);
        void onMismatch(Card firstCard, Card secondCard);
    }

    private final GameState gameState;
    private int totalPairs;

    public GameLogicService(GameState gameState, int totalPairs) {
        this.gameState = gameState;
        this.totalPairs = totalPairs;
    }

    /**
     * [UC4 - Play again]
     * Cập nhật lại tổng số cặp khi ván mới được tạo hoặc bàn chơi được dựng lại.
     */
    public void reset(int totalPairs) {
        this.totalPairs = totalPairs;
    }

    /**
     * [11.1.0] Kiểm tra điều kiện chọn thẻ ở Game Scene.
     * Chỉ cho chọn khi game đang PLAYING, thẻ chưa matched và board không resolving.
     */
    public boolean canSelect(Card card) {
        // Không cho chọn thẻ đã matched
        if (card.isMatched()) return false;
        // Không cho chọn khi đang xử lý animation
        if (gameState.isResolving()) return false;
        // Không cho chọn lại thẻ đầu tiên đã chọn
        Card first = gameState.getFirstSelectedCard();
        if (first != null && first.getId().equals(card.getId())) return false;
        // Chỉ cho chọn khi game đang PLAYING
        return gameState.getStatus() == com.example.memorycardflip.model.GameStatus.PLAYING;
    }

    /**
     * [11.1.1-11.2.2] Xử lý người chơi ghép cặp và quyết định match/mismatch.
     *
     * Luồng chính (match):
     * - [11.1.2] xác định đây là cặp match
     * - [11.1.3] incrementCombo()
     * - [11.1.4] lấy comboCount để tính thưởng
     * - [11.1.5] trả comboCount về cho UI/UC-10
     *
     * Luồng phụ (mismatch):
     * - [11.2.1] resetCombo()
     */
    public void handleSelection(Card card, Listener listener) {
        if (!canSelect(card)) return;

        Card first = gameState.getFirstSelectedCard();

        if (first == null) {
            gameState.selectFirstCard(card);
            listener.onFirstCardSelected(card);
            return;
        }

        gameState.setResolving(true);
        gameState.incrementMoves();

        // Luồng chính: ghép đúng -> tăng matchedPairs + combo để UC-10 tính điểm và UC-11 hiển thị streak.
        if (first.isPairOf(card)) {
            gameState.incrementMatchedPairs();
            gameState.incrementCombo();

            listener.onMatch(
                    first, card,
                    gameState.getMatchedPairs(),
                    totalPairs,
                    gameState.getComboCount()
            );
        } else {
            // Luồng phụ: ghép sai -> reset combo (cắt streak) và chuyển nhánh mismatch cho UI xử lý.
            gameState.incrementWrongAttempts();
            gameState.resetCombo();
            listener.onMismatch(first, card);
        }
    }

    /**
     * [11.1.7-11.1.8] Gọi sau khi animation match/mismatch hoàn tất.
     * Reset firstSelectedCard và isResolving để board có thể nhận click tiếp theo.
     */
    public void clearSelection() {
        // FIX 2d: clearSelection phải luôn reset cả firstCard lẫn resolving.
        // Trước đây nếu có early-return trong handleSelection,
        // isResolving bị kẹt true → board bị lock vĩnh viễn.
        gameState.clearSelection();
    }

    public int getMatchedPairs() {
        return gameState.getMatchedPairs();
    }

    public int getTotalPairs() {
        return totalPairs;
    }
}
