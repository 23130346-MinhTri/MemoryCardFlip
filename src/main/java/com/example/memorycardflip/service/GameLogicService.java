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

    public void reset(int totalPairs) {
        this.totalPairs = totalPairs;
    }

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
     * [UC-07] Xử lý lựa chọn thẻ thứ hai và kiểm tra cặp.
     *
     * <p>Use Case này xác định xem hai thẻ được chọn có cùng pairId hay không.</p>
     * <p>Postcondition: nếu khớp thì gọi onMatch; nếu không khớp thì gọi onMismatch.</p>
     */
    public void handleSelection(Card card, Listener listener) {
        // FIX 2a: Guard isResolving ngay đầu, trước mọi xử lý.
        // Trước đây guard này bị thiếu ở một số path → board bị lock.
        if (!canSelect(card)) return;

        Card first = gameState.getFirstSelectedCard();

        // Lần chọn đầu tiên
        if (first == null) {
            gameState.selectFirstCard(card);
            listener.onFirstCardSelected(card);
            return;
        }

        // Lần chọn thứ hai — khóa input ngay lập tức
        // FIX 2b: Chỉ gọi setResolving(true) ở ĐÂY, không để GameState.selectCard()
        // cũng gọi nó → trước đây gây double-call và double increment.
        gameState.setResolving(true);
        gameState.incrementMoves();

        if (first.isPairOf(card)) {
            // FIX 2c: Chỉ increment matchedPairs MỘT lần ở đây.
            // Trước đây GameState.selectCard() cũng gọi incrementMatchedPairs()
            // → count bị nhân đôi, win condition không bao giờ trigger đúng.
            gameState.incrementMatchedPairs();
            gameState.incrementCombo();

            listener.onMatch(
                    first, card,
                    gameState.getMatchedPairs(),
                    totalPairs,
                    gameState.getComboCount()
            );
        } else {
            gameState.incrementWrongAttempts();
            gameState.resetCombo();
            listener.onMismatch(first, card);
        }
    }

    /**
     * Gọi sau khi animation match/mismatch hoàn tất.
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