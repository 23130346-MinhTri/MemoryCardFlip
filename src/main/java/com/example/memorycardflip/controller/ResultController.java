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
 * Controller lấy GameState hiện tại từ SceneManager, đọc các thống kê cuối ván
 * và hiển thị lên result.fxml: trạng thái WIN/LOSE, độ khó, điểm, số lượt,
 * thời gian còn lại, số cặp đã ghép và thông tin high score.
 */
/**
 * <p><b>UC-13 Sequence references:</b></p>
        * <ul>
 *   <li>13.1.2  — SceneManager tải result.fxml, tạo ResultController và truyền GameState</li>
        *   <li>13.1.3  — initialize() lấy GameState và gọi loadUCRS01DisplayResult()</li>
        *   <li>13.1.4  — loadUCRS01DisplayResult() gọi ScoreManager.saveScore(gameState, "Player")</li>
        *   <li>13.1.9  — Kiểm tra isNewHighScore() và hiển thị "🏆 NEW HIGH SCORE!" nếu phá kỷ lục</li>
        *   <li>13.1.10 — Màn hình Result hiển thị đầy đủ điểm, lượt, thời gian còn lại</li>
        * </ul>
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

    private GameState gameState;
    private final ScoreManager scoreManager = ScoreManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // [UC3] Nhận lại GameState của ván vừa kết thúc trước khi render màn kết quả.
        gameState = SceneManager.getInstance().getCurrentGameState();
        loadUCRS01DisplayResult();
    }

    /**
     * [UC-03] Hiển thị kết quả của ván vừa kết thúc.
     *
     * <p>Use Case này trình bày kết quả cho người chơi khi ván đã WIN hoặc LOSE.</p>
     * <p>Precondition: SceneManager đang giữ currentGameState của ván vừa xong.</p>
     * <p>Postcondition: UI kết quả hiển thị trạng thái WIN/LOSE, điểm số, thời gian và số lượt.</p>
     */
    /**
     * [UC-13 — 13.1.4 → 13.1.10] kết quả điểm và lưu điểm.
     *
     * <p><b>Precondition:</b> gameState != null (hoặc null → hiển thị fallback).</p>
     * <p><b>Postcondition:</b></p>
     * <ul>
     *   <li>UI kết quả hiển thị WIN/LOSE, điểm số, số lượt, thời gian, số cặp.</li>
     *   <li>Điểm đã được lưu vào ScoreManager cache và file JSON.</li>
     *   <li>Nếu thắng và là kỷ lục mới → lblNewHighScore hiển thị "🏆 Kỷ lục mới!".</li>
     * </ul>
     */
    private void loadUCRS01DisplayResult() {
        if (gameState == null) {
            // [UC3] Trường hợp phòng vệ: không có GameState thì hiển thị kết quả rỗng.
            lblResultTitle.setText("KẾT QUẢ");
            lblResultTitle.getStyleClass().setAll("result-title", "result-lose");
            lblDifficultyValue.setText("-");
            lblScoreValue.setText("0");
            lblMovesValue.setText("0");
            lblTimeRemainingValue.setText("00:00");
            lblPairsValue.setText("0 / 0");
            lblNewHighScore.setText("---");
            lblSummary.setText("Không tìm thấy dữ liệu ván chơi.");
            return;
        }

        boolean won = gameState.getStatus() == GameStatus.WON;
        // [UC3] Chọn tiêu đề và màu style theo trạng thái cuối ván.
        lblResultTitle.setText(won ? "🎉 YOU WIN! 🎉" : "💀 GAME OVER 💀");
        lblResultTitle.getStyleClass().setAll("result-title", won ? "result-win" : "result-lose");

        // [UC3] Hiển thị độ khó kèm kích thước lưới để người chơi biết ván vừa chơi.
        lblDifficultyValue.setText(gameState.getDifficulty().getDisplayName() +
                " (" + gameState.getDifficulty().getGridSize() + "×" +
                gameState.getDifficulty().getGridSize() + ")");

        // [UC3] Lấy thống kê chính từ GameState và đổ lên các label trong result.fxml.
        int score = gameState.calculateScore();
        lblScoreValue.setText(String.valueOf(score));
        lblMovesValue.setText(String.valueOf(gameState.getMoves()));

        // [UC12] Định dạng thời gian còn lại theo MM:SS để người chơi xem lại kết quả.
        int remaining = gameState.getTimeRemaining();
        lblTimeRemainingValue.setText(String.format("%02d:%02d", remaining / 60, remaining % 60));

        int totalPairs = gameState.getDifficulty().totalPairs();
        lblPairsValue.setText(gameState.getMatchedPairs() + " / " + totalPairs);

        // [UC3] Sau khi hiển thị thống kê, chỉ lưu điểm khi ván thắng.
        if (won) {
            System.out.println("===== SAVING SCORE =====");
            System.out.println("Matched pairs: " + gameState.getMatchedPairs());
            System.out.println("Total pairs: " + gameState.getDifficulty().totalPairs());
            String playerName = "Player";
            scoreManager.saveScore(gameState, playerName);
        }

        // [UC3] Chỉ hiển thị phần kỷ lục khi người chơi thắng.
        if (won) {
            int bestScore = scoreManager.getBestScore(gameState.getDifficulty());
            boolean isNewHighScore = scoreManager.isNewHighScore(
                    gameState.getDifficulty(),
                    gameState.calculateScore()
            );
            if (isNewHighScore) {
                lblNewHighScore.setText("🏆 " + gameState.calculateScore() + " (Kỷ lục mới!)");
                lblNewHighScore.setStyle("-fx-text-fill: #ffd700;");
            } else {
                lblNewHighScore.setText( ""+ bestScore);
                lblNewHighScore.setStyle("-fx-text-fill: #7ec8e3;");
            }
        } else {
            // [13.2.2] Người chơi thua → không hiển thị New High Score
            lblNewHighScore.setText("---");
        }

        // [UC3] Câu tổng kết cuối màn hình giúp người chơi hiểu kết quả ván.
        lblSummary.setText(won
                ? "Xuất sắc! Bạn đã hoàn thành " + totalPairs + " cặp thẻ."
                : "Bạn đã thua vì hết giờ. Cố gắng lần sau nhé!");
    }

    /**
     * [UC4 - Play again] Chơi lại cùng độ khó hiện tại từ màn kết quả.
     *
     * <p>Postcondition: SceneManager tạo GameState mới và mở lại GameScene với cùng difficulty.</p>
     */
    @FXML
    public void onUCRS02Replay() {
        // [UC4] Người chơi bấm "CHƠI LẠI" sau khi xem kết quả.
        SceneManager.getInstance().replayGame();
    }

    /**
     * [UC-RS-03] Quay lại menu chính.
     *
     * <p>Postcondition: Main menu hiển thị, game state hiện tại được dọn.</p>
     */
    @FXML
    public void onUCRS03BackToMenu() {
        // [UC3] Rời màn kết quả và quay lại menu chính.
        SceneManager.getInstance().showMenu();
    }
}
