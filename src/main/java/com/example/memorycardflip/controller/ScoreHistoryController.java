package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.model.ScoreRecord;
import com.example.memorycardflip.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ScoreHistoryController implements Initializable {

    @FXML private TableView<ScoreRecord> tableView;
    @FXML private TableColumn<ScoreRecord, String> colDate;
    @FXML private TableColumn<ScoreRecord, String> colDifficulty;
    @FXML private TableColumn<ScoreRecord, Integer> colScore;
    @FXML private TableColumn<ScoreRecord, Integer> colMoves;
    @FXML private TableColumn<ScoreRecord, String> colTimeUsed;
    @FXML private ComboBox<Difficulty> comboDifficulty;
    @FXML private ComboBox<String> comboFilter;
    @FXML private Label lblTotalGames;

    private final ScoreManager scoreManager = ScoreManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFilter();
        loadAllHistory();
    }

    private void setupTableColumns() {
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().formattedTimestamp()));

        colDifficulty.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDifficulty().getDisplayName()));

        colScore.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getScore()
                ));

        colMoves.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getMoves()
                ));

        colTimeUsed.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().formattedTimeUsed()));
    }

    private void setupFilter() {
        comboFilter.getItems().addAll("Tất cả", "EASY", "MEDIUM", "HARD");
        comboFilter.setValue("Tất cả");
        comboFilter.setOnAction(e -> filterHistory());
    }

    /**
     * [UC-06] Tải toàn bộ lịch sử điểm từ ScoreManager.
     *
     * <p>Postcondition: bảng lịch sử được điền đầy đủ dữ liệu và tổng số ván được cập nhật.</p>
     */
    private void loadAllHistory() {
        List<ScoreRecord> allScores = scoreManager.getAllScores();
        tableView.getItems().setAll(allScores);
        lblTotalGames.setText("Tổng số ván: " + allScores.size());
    }

    private void filterHistory() {
        String filter = comboFilter.getValue();
        List<ScoreRecord> allScores = scoreManager.getAllScores();

        if (filter == null || filter.equals("Tất cả")) {
            tableView.getItems().setAll(allScores);
        } else {
            Difficulty diff = Difficulty.valueOf(filter);
            List<ScoreRecord> filtered = allScores.stream()
                    .filter(r -> r.getDifficulty() == diff)
                    .toList();
            tableView.getItems().setAll(filtered);
        }
        lblTotalGames.setText("Tổng số ván: " + tableView.getItems().size());
    }
    /**
     * [UC-06] Xác nhận và xóa toàn bộ lịch sử điểm.
     *
     * <p>Use Case này yêu cầu người chơi xác nhận trước khi xóa điểm vĩnh viễn.</p>
     */
    @FXML
    public void onClearAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa điểm");
        alert.setHeaderText("Xóa tất cả điểm số?");
        alert.setContentText("Hành động này không thể hoàn tác!");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                scoreManager.clearAllScores();
                loadAllHistory();
            }
        });
    }
    @FXML
    public void onBackToMenu() {
        SceneManager.getInstance().showMenu();
    }
}