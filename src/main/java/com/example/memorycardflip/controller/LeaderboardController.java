package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.ScoreRecord;
import com.example.memorycardflip.controller.ScoreManager;
import com.example.memorycardflip.ui.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.binding.Bindings;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Leaderboard.
 * File FXML tương ứng: leaderboard.fxml
 *
 * <p>Use Case phụ trách: [UC-05] Xem bảng xếp hạng.</p>
 */
public class LeaderboardController implements Initializable {

    @FXML private TableView<ScoreRecord> tableView;
    @FXML private TableColumn<ScoreRecord, String> colRank;
    @FXML private TableColumn<ScoreRecord, String> colPlayerName;
    @FXML private TableColumn<ScoreRecord, String> colDifficulty;
    @FXML private TableColumn<ScoreRecord, Integer> colScore;
    @FXML private TableColumn<ScoreRecord, Integer> colMoves;
    @FXML private TableColumn<ScoreRecord, String> colTime;
    @FXML private TableColumn<ScoreRecord, String> colDate;
    @FXML private ComboBox<Difficulty> comboDifficulty;
    @FXML private Label lblTotalScores;
    @FXML private Label lblBestScore;

    private final ScoreManager scoreManager = ScoreManager.getInstance();
    private ObservableList<ScoreRecord> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupDifficultyCombo();
        loadLeaderboard(Difficulty.EASY);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        // Lắng nghe thay đổi điểm để cập nhật UI
        scoreManager.setOnScoreChanged(scores -> {
            javafx.application.Platform.runLater(() -> loadLeaderboard(comboDifficulty.getValue()));
        });
    }

    private void setupTableColumns() {
        colRank.setCellValueFactory(cellData -> {
            int index = tableView.getItems().indexOf(cellData.getValue()) + 1;
            return new javafx.beans.property.SimpleStringProperty("#" + index);
        });

        colPlayerName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPlayerName()));

        colDifficulty.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDifficulty().getDisplayName()));

        // Cách đúng: dùng createObjectBinding
        colScore.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getScore()
                ));

        colMoves.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getMoves()
                ));

        colTime.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().formattedTimeUsed()));

        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().formattedTimestamp()));
    }
    private void setupDifficultyCombo() {
        comboDifficulty.getItems().setAll(Difficulty.values());
        comboDifficulty.setValue(Difficulty.EASY);
        comboDifficulty.setOnAction(e -> loadLeaderboard(comboDifficulty.getValue()));
    }

    /**
     * [UC-05] Tải dữ liệu bảng xếp hạng theo độ khó.
     *
     * <p>Postcondition: bảng xếp hạng được cập nhật và điểm cao nhất hiển thị.</p>
     */
    private void loadLeaderboard(Difficulty difficulty) {
        List<ScoreRecord> scores = scoreManager.getTopScores(difficulty, 50);
        data.setAll(scores);
        tableView.setItems(data);

        lblTotalScores.setText("Tổng số: " + scores.size() + " lượt chơi");

        scoreManager.getHighScore(difficulty).ifPresentOrElse(
                best -> lblBestScore.setText("🏆 Cao nhất: " + best.getScore()),
                () -> lblBestScore.setText("🏆 Chưa có điểm nào")
        );
    }

    /**
     * [UC-05] Làm mới dữ liệu bảng xếp hạng hiện tại.
     */
    @FXML
    public void onRefresh() {
        loadLeaderboard(comboDifficulty.getValue());
    }


    @FXML
    public void onBackToMenu() {
        SceneManager.getInstance().showMenu();
    }
}