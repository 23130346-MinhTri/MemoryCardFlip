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

    // Kiểm tra thẻ có thể được chọn.
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
     * Xử lý lựa chọn thẻ thứ hai và kiểm tra cặp.
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