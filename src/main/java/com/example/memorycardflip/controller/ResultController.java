package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import com.example.memorycardflip.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ResultController implements Initializable {
    @FXML private Label lblResultTitle;
    @FXML private Label lblDifficultyValue;
    @FXML private Label lblScoreValue;
    @FXML private Label lblMovesValue;
    @FXML private Label lblTimeRemainingValue;  // Đổi tên từ lblTimeValue
    @FXML private Label lblPairsValue;
    @FXML private Label lblNewHighScore;  // Thêm mới
    @FXML private Label lblSummary;

    private GameState gameState;
    private final ScoreManager scoreManager = ScoreManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameState = SceneManager.getInstance().getCurrentGameState();
        loadUCRS01DisplayResult();
    }

    /**
     * [UC-RS-01] Hiển thị kết quả của ván vừa kết thúc.
     *
     * <p>Precondition: SceneManager đang giữ currentGameState của ván vừa xong.</p>
     * <p>Postcondition: UI kết quả hiển thị trạng thái WIN/LOSE và các thống kê chính.</p>
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

        // Định dạng thời gian MM:SS
        int remaining = gameState.getTimeRemaining();
        lblTimeRemainingValue.setText(String.format("%02d:%02d", remaining / 60, remaining % 60));

        int totalPairs = gameState.getDifficulty().totalPairs();
        lblPairsValue.setText(gameState.getMatchedPairs() + " / " + totalPairs);

        // ========== THÊM ĐOẠN CODE NÀY VÀO ĐÂY ==========
        System.out.println("===== SAVING SCORE =====");
        System.out.println("Matched pairs: " + gameState.getMatchedPairs());
        System.out.println("Total pairs: " + gameState.getDifficulty().totalPairs());
        String playerName = "Player";
        scoreManager.saveScore(gameState, playerName);  // ← BỎ if(won), gọi trực tiếp

// Kiểm tra high score (chỉ hiển thị khi thắng)
        if (won) {
            boolean isNewHighScore = scoreManager.isNewHighScore(
                    gameState.getDifficulty(),
                    gameState.calculateScore()
            );

            if (isNewHighScore) {
                lblNewHighScore.setText("🏆 NEW HIGH SCORE! 🏆");
                lblNewHighScore.setStyle("-fx-text-fill: #ffd700;");
            } else {
                lblNewHighScore.setText("---");
            }
        } else {
            lblNewHighScore.setText("---");
        }
        // ========== KẾT THÚC PHẦN THÊM ==========

        lblSummary.setText(won
                ? "Xuất sắc! Bạn đã hoàn thành " + totalPairs + " cặp thẻ."
                : "Bạn đã thua vì hết giờ. Cố gắng lần sau nhé!");
    }

    private boolean checkIfNewHighScore(int currentScore) {
        // TODO: Implement actual high score check from storage
        // Tạm thời trả về true nếu score > 100
        return currentScore > 100;
    }
    /**
     * [UC-RS-02] Chơi lại cùng độ khó hiện tại.
     *
     * <p>Postcondition: Màn chơi mới được mở với cùng difficulty.</p>
     */
    @FXML
    public void onUCRS02Replay() {
        SceneManager.getInstance().replayGame();
    }

    /**
     * [UC-RS-03] Quay lại menu chính.
     *
     * <p>Postcondition: Main menu hiển thị, game state hiện tại được dọn.</p>
     */
    @FXML
    public void onUCRS03BackToMenu() {
        SceneManager.getInstance().showMenu();
    }
}
