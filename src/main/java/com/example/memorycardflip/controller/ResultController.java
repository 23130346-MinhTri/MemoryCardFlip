package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import com.example.memorycardflip.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * [UC3 - View result]
 * Controller của màn hình kết quả sau khi người chơi thắng hoặc thua.
 *
 * Nâng cấp:
 * - Đánh giá sao 1–5★ dựa trên thời gian còn lại + độ chính xác
 * - Gợi ý cải thiện thông minh dựa trên stats ván vừa chơi
 */
public class ResultController implements Initializable {
    @FXML private Label lblResultTitle;          // [UC3] Tiêu đề kết quả: YOU WIN hoặc GAME OVER
    @FXML private Label lblDifficultyValue;      // [UC3] Độ khó của ván vừa kết thúc
    @FXML private Label lblScoreValue;           // [UC3] Điểm cuối cùng được tính từ GameState
    @FXML private Label lblMovesValue;           // [UC3] Tổng số lượt lật/di chuyển
    @FXML private Label lblTimeRemainingValue;   // [UC3][UC12] Thời gian còn lại khi ván kết thúc
    @FXML private Label lblPairsValue;           // [UC3] Số cặp đã ghép / tổng số cặp
    @FXML private Label lblNewHighScore;         // [UC3] Hiển thị kỷ lục hoặc điểm cao hiện tại
    @FXML private Label lblSummary;              // [UC3] Câu tổng kết theo kết quả thắng/thua
    @FXML private Label lblStarRating;           // [UC3 - nâng cấp] Đánh giá sao 1–5★
    @FXML private Label lblTip;                  // [UC3 - nâng cấp] Gợi ý cải thiện thông minh
    @FXML private javafx.scene.control.Button btnNextLevel; // [UC4 - nâng cấp] Nút lên level tiếp theo

    private GameState gameState;
    private final ScoreManager scoreManager = ScoreManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameState = SceneManager.getInstance().getCurrentGameState();
        loadUCRS01DisplayResult();
    }

    /**
     * [UC-03 / UC-13] Hiển thị kết quả của ván vừa kết thúc.
     * Nâng cấp: thêm đánh giá sao và gợi ý thông minh.
     */
    private void loadUCRS01DisplayResult() {
        if (gameState == null) {
            lblResultTitle.setText("KẾT QUẢ");
            lblResultTitle.getStyleClass().setAll("result-title", "result-lose");
            lblDifficultyValue.setText("-");
            lblScoreValue.setText("0");
            lblMovesValue.setText("0");
            lblTimeRemainingValue.setText("00:00");
            lblPairsValue.setText("0 / 0");
            lblNewHighScore.setText("---");
            lblSummary.setText("Không tìm thấy dữ liệu ván chơi.");
            if (lblStarRating != null) lblStarRating.setText("☆☆☆☆☆");
            if (lblTip != null) lblTip.setText("");
            return;
        }

        boolean won = gameState.getStatus() == GameStatus.WON;

        lblResultTitle.setText(won ? "🎉 YOU WIN! 🎉" : "💀 GAME OVER 💀");
        lblResultTitle.getStyleClass().setAll("result-title", won ? "result-win" : "result-lose");

        lblDifficultyValue.setText(gameState.getDifficulty().getDisplayName() +
                " (" + gameState.getDifficulty().getGridSize() + "×" +
                gameState.getDifficulty().getGridSize() + ")");

        int score = gameState.calculateScore();
        lblScoreValue.setText(String.valueOf(score));
        lblMovesValue.setText(String.valueOf(gameState.getMoves()));

        int remaining = gameState.getTimeRemaining();
        lblTimeRemainingValue.setText(String.format("%02d:%02d", remaining / 60, remaining % 60));

        int totalPairs = gameState.getDifficulty().totalPairs();
        lblPairsValue.setText(gameState.getMatchedPairs() + " / " + totalPairs);

        if (won) {
            scoreManager.saveScore(gameState, "Player");
        }

        // [UC3] High score
        if (won) {
            int bestScore = scoreManager.getBestScore(gameState.getDifficulty());
            boolean isNewHighScore = scoreManager.isNewHighScore(
                    gameState.getDifficulty(), gameState.calculateScore());
            if (isNewHighScore) {
                lblNewHighScore.setText("🏆 " + gameState.calculateScore() + " (Kỷ lục mới!)");
                lblNewHighScore.setStyle("-fx-text-fill: #ffd700;");
            } else {
                lblNewHighScore.setText("" + bestScore);
                lblNewHighScore.setStyle("-fx-text-fill: #7ec8e3;");
            }
        } else {
            lblNewHighScore.setText("---");
        }

        // [UC3 - nâng cấp] Đánh giá sao
        if (lblStarRating != null) {
            int stars = calculateStarRating(won, totalPairs);
            lblStarRating.setText(buildStarString(stars));
            lblStarRating.setStyle(getStarStyle(stars));
        }

        // [UC3 - nâng cấp] Gợi ý thông minh
        if (lblTip != null) {
            lblTip.setText(buildSmartTip(won, totalPairs));
        }

        // [UC4 - nâng cấp] Hiện/ẩn nút "Lên Level" dựa trên kết quả và độ khó
        if (btnNextLevel != null) {
            com.example.memorycardflip.model.Difficulty nextDiff =
                    SceneManager.getInstance().getNextDifficulty(gameState.getDifficulty());
            if (won && nextDiff != null) {
                btnNextLevel.setVisible(true);
                btnNextLevel.setText("⬆ " + nextDiff.getDisplayName().toUpperCase());
            } else {
                // Thua, hoặc đã ở HARD — ẩn nút
                btnNextLevel.setVisible(false);
                btnNextLevel.setManaged(false);
            }
        }

        // [UC3] Summary
        lblSummary.setText(won
                ? "Xuất sắc! Bạn đã hoàn thành " + totalPairs + " cặp thẻ."
                : "Bạn đã thua vì hết giờ. Cố gắng lần sau nhé!");
    }

    /**
     * [UC3 - nâng cấp] Tính số sao (1–5) dựa trên:
     * - Thắng/thua
     * - Tỷ lệ thời gian còn lại (time efficiency)
     * - Tỷ lệ đúng/sai (move accuracy)
     */
    private int calculateStarRating(boolean won, int totalPairs) {
        if (!won) {
            // Thua: tối đa 2 sao dựa trên số cặp đã ghép được
            int matched = gameState.getMatchedPairs();
            if (matched == 0) return 1;
            double matchRatio = (double) matched / totalPairs;
            return matchRatio >= 0.5 ? 2 : 1;
        }

        int stars = 0;

        // Tiêu chí 1: Thắng = 2 sao cơ bản
        stars += 2;

        // Tiêu chí 2: Thời gian còn lại ≥ 30% = +1 sao
        int timeLimit = gameState.getDifficulty().getTimeLimit();
        double timeRatio = timeLimit > 0 ? (double) gameState.getTimeRemaining() / timeLimit : 0;
        if (timeRatio >= 0.3) stars++;

        // Tiêu chí 3: Độ chính xác (moves tối thiểu = totalPairs*2)
        // moves ≤ totalPairs * 2.5 = +1 sao (ít lật lại thừa)
        int moves = gameState.getMoves();
        double moveEfficiency = totalPairs > 0 ? (double) (totalPairs * 2) / moves : 0;
        if (moveEfficiency >= 0.8) stars++;

        return Math.min(5, stars);
    }

    /** Xây dựng chuỗi sao hiển thị */
    private String buildStarString(int stars) {
        return "★".repeat(stars) + "☆".repeat(5 - stars);
    }

    /** Màu sắc theo số sao */
    private String getStarStyle(int stars) {
        return switch (stars) {
            case 5 -> "-fx-text-fill: #ffd700; -fx-font-size: 28px;";
            case 4 -> "-fx-text-fill: #f5c842; -fx-font-size: 28px;";
            case 3 -> "-fx-text-fill: #f59e0b; -fx-font-size: 28px;";
            case 2 -> "-fx-text-fill: #94a3b8; -fx-font-size: 28px;";
            default -> "-fx-text-fill: #64748b; -fx-font-size: 28px;";
        };
    }

    /**
     * [UC3 - nâng cấp] Gợi ý cải thiện thông minh dựa trên stats thực tế.
     * Phân tích điểm yếu cụ thể thay vì text cố định.
     */
    private String buildSmartTip(boolean won, int totalPairs) {
        if (!won) {
            int matched = gameState.getMatchedPairs();
            int remaining = totalPairs - matched;
            if (matched == 0) {
                return "💡 Còn " + remaining + " cặp chưa ghép. Hãy thử độ khó dễ hơn trước!";
            }
            return "💡 Bạn đã ghép được " + matched + "/" + totalPairs
                    + " cặp. Thêm " + remaining + " cặp nữa là xong — tăng thêm thời gian nào!";
        }

        // Phân tích từng chỉ số
        int moves = gameState.getMoves();
        int minMoves = totalPairs * 2;
        int wrongMoves = moves - minMoves;

        int timeLimit = gameState.getDifficulty().getTimeLimit();
        int timeUsed = timeLimit - gameState.getTimeRemaining();
        double timeRatio = timeLimit > 0 ? (double) timeUsed / timeLimit : 1;

        int combo = gameState.getComboCount();

        // Ưu tiên gợi ý theo vấn đề nổi bật nhất
        if (wrongMoves >= totalPairs) {
            return "💡 Bạn lật sai " + wrongMoves + " lần! Hãy ghi nhớ vị trí thẻ tốt hơn.";
        }
        if (timeRatio > 0.85) {
            return "💡 Bạn dùng " + (int)(timeRatio * 100) + "% thời gian. Thử lật nhanh hơn nhé!";
        }
        if (combo < 3) {
            return "💡 Combo cao nhất của bạn là x" + combo + ". Ghép liên tiếp để nhân điểm!";
        }
        // Nếu chơi tốt
        if (wrongMoves <= 2 && timeRatio < 0.5) {
            return "🏆 Hoàn hảo! Chỉ " + wrongMoves + " lần sai và còn " +
                    (int)((1 - timeRatio) * 100) + "% thời gian. Thử khó hơn đi!";
        }
        return "✨ Chơi tốt lắm! Còn " + gameState.getTimeRemaining() + " giây dư.";
    }

    /**
     * [UC4 - nâng cấp] Chơi lại với độ khó tiếp theo (progressive mode).
     * Chỉ hiển thị khi người chơi vừa thắng và chưa ở độ khó HARD.
     */
    @FXML
    public void onUCRS04NextLevel() {
        if (gameState == null) return;
        com.example.memorycardflip.model.Difficulty nextDiff =
                SceneManager.getInstance().getNextDifficulty(gameState.getDifficulty());
        if (nextDiff != null) {
            SceneManager.getInstance().replayWithDifficulty(nextDiff);
        }
    }

    /**
     * [UC4 - Play again] Chơi lại cùng độ khó hiện tại từ màn kết quả.
     */
    @FXML
    public void onUCRS02Replay() {
        SceneManager.getInstance().replayGame();
    }

    /**
     * [UC-RS-03] Quay lại menu chính.
     */
    @FXML
    public void onUCRS03BackToMenu() {
        SceneManager.getInstance().showMenu();
    }
}
