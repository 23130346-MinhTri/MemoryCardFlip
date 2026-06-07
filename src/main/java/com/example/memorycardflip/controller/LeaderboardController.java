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
 * [UC-07 — View Leaderboard]
 * Controller màn hình bảng xếp hạng (leaderboard.fxml).
 *
 * <p><b>Precondition:</b>
 *   Màn hình Main Menu đang hiển thị; ScoreManager đã load dữ liệu từ file JSON vào cache.</p>
 *
 * <p><b>Postcondition:</b>
 *   Màn hình Leaderboard hiển thị top 50 điểm theo độ khó; ComboBox cho phép
 *   chuyển đổi EASY / MEDIUM / HARD; lblBestScore hiển thị điểm cao nhất.</p>
 *
 * <p><b>Sequence references:</b></p>
 * <ul>
 *   <li>7.1.3  — SceneManager tải leaderboard.fxml và khởi tạo lớp này</li>
 *   <li>7.2.4  — initialize() gọi setupTableColumns(), setupDifficultyCombo(), loadLeaderboard(EASY)</li>
 *   <li>7.1.5  — loadLeaderboard() gọi ScoreManager.getTopScores(difficulty, 50)</li>
 *   <li>7.1.6  — ScoreManager trả về List&lt;ScoreRecord&gt; từ cache</li>
 *   <li>7.1.7  — tableView.setItems() + cập nhật lblTotalScores, lblBestScore</li>
 *   <li>7.1.8  — Người chơi chọn độ khó mới → setOnAction gọi lại loadLeaderboard()</li>
 *   <li>7.1.9  — Bảng được làm mới với dữ liệu tương ứng</li>
 * </ul>
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
    // ════════════════════════════════════════════════════════════════════════
    // [UC-07 — Bước 7.2.4] initialize()
    // Được gọi tự động sau khi leaderboard.fxml được load bởi SceneManager (bước 7.1.3).
    // ════════════════════════════════════════════════════════════════════════

    /**
     * [UC-07 — 7.2.4] Khởi tạo controller bảng xếp hạng.
     *
     * <p>Thứ tự gọi phải đúng theo Sequence Diagram:</p>
     * <ol>
     *   <li>7.2.4a — setupTableColumns(): cấu hình các cột TableView</li>
     *   <li>7.2.4b — setupDifficultyCombo(): thiết lập bộ lọc độ khó (mặc định EASY)</li>
     *   <li>7.2.4c — loadLeaderboard(EASY): tải dữ liệu ban đầu</li>
     * </ol>
     *
     * <p>Đồng thời đăng ký callback onScoreChanged để Leaderboard tự cập nhật
     * khi UC-13 lưu điểm mới (bước 13.1.8).</p>
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // [7.2.4a] Thiết lập các cột của TableView
        setupTableColumns();
        // [7.2.4b] Thiết lập ComboBox lọc độ khó, mặc định EASY
        setupDifficultyCombo();
        // [7.2.4c] Tải dữ liệu bảng xếp hạng mặc định (EASY)
        loadLeaderboard(Difficulty.EASY);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        // Lắng nghe thay đổi điểm để cập nhật UI
        scoreManager.setOnScoreChanged(scores -> {
            javafx.application.Platform.runLater(() -> loadLeaderboard(comboDifficulty.getValue()));
        });
    }
    /**
     * [UC-07 — 7.2.4a] Cấu hình hiển thị từng cột của bảng xếp hạng.
     *
     * <p>Các cột: # (Hạng), Người chơi, Độ khó, Điểm, Số lượt, Thời gian dùng, Ngày chơi.</p>
     * <p>Postcondition: TableView sẵn sàng nhận dữ liệu ScoreRecord.</p>
     */
    private void setupTableColumns() {
        // [7.2.4a] Cột # — số thứ tự dựa trên vị trí trong danh sách (1-indexed)
        colRank.setCellValueFactory(cellData -> {
            int index = tableView.getItems().indexOf(cellData.getValue()) + 1;
            return new javafx.beans.property.SimpleStringProperty("#" + index);
        });
        // [7.2.4a] Cột Người chơi
        colPlayerName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPlayerName()));
        // [7.2.4a] Cột Độ khó (EASY / MEDIUM / HARD)
        colDifficulty.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDifficulty().getDisplayName()));

        // Cách đúng: dùng createObjectBinding
        // [7.2.4a] Cột Điểm — dùng createObjectBinding để trả về Integer
        colScore.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getScore()
                ));
        // [7.2.4a] Cột Số lượt — dùng createObjectBinding để trả về Integer
        colMoves.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getMoves()
                ));
        // [7.2.4a] Cột Thời gian dùng (MM:SS)
        colTime.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().formattedTimeUsed()));
        // [7.2.4a] Cột Ngày chơi (dd/MM/yyyy HH:mm)
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().formattedTimestamp()));
    }
    // ════════════════════════════════════════════════════════════════════════
    // [UC-07 — Bước 7.2.4b] setupDifficultyCombo()
    // ════════════════════════════════════════════════════════════════════════

    /**
     * [UC-07 — 7.2.4b] Thiết lập ComboBox lọc độ khó.
     *
     * <p>Gán giá trị EASY / MEDIUM / HARD vào comboDifficulty,
     * đặt mặc định EASY (bước 7.2.4 — comboDifficulty.setValue(EASY)),
     * và bắt sự kiện thay đổi để gọi lại loadLeaderboard() (bước 7.1.8).</p>
     */
    private void setupDifficultyCombo() {
        // [7.2.4b] Thêm tất cả độ khó vào ComboBox
        comboDifficulty.getItems().setAll(Difficulty.values());
        comboDifficulty.setValue(Difficulty.EASY);
        comboDifficulty.setOnAction(e -> loadLeaderboard(comboDifficulty.getValue()));
    }

    // ════════════════════════════════════════════════════════════════════════
    // [UC-07 — Bước 7.1.5 → 7.1.7] loadLeaderboard(Difficulty)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * [UC-07 — 7.1.5 → 7.1.7] Tải dữ liệu bảng xếp hạng và cập nhật UI.
     *
     * <p><b>Precondition:</b> ScoreManager đã sẵn sàng và cache đã được load.</p>
     * <p><b>Postcondition:</b></p>
     * <ul>
     *   <li>TableView hiển thị top 50 điểm theo {@code difficulty} giảm dần.</li>
     *   <li>lblTotalScores hiển thị tổng số lượt chơi.</li>
     *   <li>lblBestScore hiển thị điểm cao nhất hoặc "Chưa có điểm nào".</li>
     * </ul>
     *
     * @param difficulty Độ khó cần lọc (EASY / MEDIUM / HARD)
     */
    private void loadLeaderboard(Difficulty difficulty) {
        // [7.1.5] Gọi ScoreManager.getTopScores() lấy tối đa 50 bản ghi
        List<ScoreRecord> scores = scoreManager.getTopScores(difficulty, 50);
        // [7.1.6] ScoreManager trả về danh sách từ cache (đã filter + sort + limit)
        data.setAll(scores);
        // [7.1.7] Cập nhật TableView
        tableView.setItems(data);
        // [7.1.7] Cập nhật lblTotalScores — tổng số lượt chơi
        lblTotalScores.setText("Tổng số: " + scores.size() + " lượt chơi");
        // [7.1.7] Cập nhật lblBestScore — điểm cao nhất hoặc thông báo trống
        scoreManager.getHighScore(difficulty).ifPresentOrElse(
                best -> lblBestScore.setText("🏆 Cao nhất: " + best.getScore()),
                () -> lblBestScore.setText("🏆 Chưa có điểm nào")
        );
    }

    /**
     * [UC-07] Làm mới bảng xếp hạng với độ khó đang được chọn.
     *
     * <p>Postcondition: Dữ liệu được tải lại từ ScoreManager (đọc từ cache).</p>
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