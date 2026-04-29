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
    @FXML private Label lblTimeValue;
    @FXML private Label lblPairsValue;
    @FXML private Label lblSummary;

    private GameState gameState;

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
            lblResultTitle.setText("NO RESULT");
            lblResultTitle.getStyleClass().setAll("result-lose");
            lblDifficultyValue.setText("-");
            lblScoreValue.setText("0");
            lblMovesValue.setText("0");
            lblTimeValue.setText("0s");
            lblPairsValue.setText("0 / 0");
            lblSummary.setText("Khong tim thay du lieu van choi.");
            return;
        }

        boolean won = gameState.getStatus() == GameStatus.WON;
        lblResultTitle.setText(won ? "YOU WIN" : "GAME OVER");
        lblResultTitle.getStyleClass().setAll(won ? "result-win" : "result-lose");

        lblDifficultyValue.setText(gameState.getDifficulty().toString());
        lblScoreValue.setText(String.valueOf(gameState.calculateScore()));
        lblMovesValue.setText(String.valueOf(gameState.getMoves()));
        lblTimeValue.setText(gameState.getTimeRemaining() + "s");
        lblPairsValue.setText(gameState.getMatchedPairs() + " / " + gameState.getDifficulty().totalPairs());
        lblSummary.setText(won
                ? "Ban da hoan thanh tat ca cap the. Choi lai de cai thien diem so."
                : "Ban da het thoi gian. Thu lai de vuot qua moc hien tai.");
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
