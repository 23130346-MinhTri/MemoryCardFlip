package com.example.memorycardflip.service;

import com.example.memorycardflip.model.GameState;
import com.example.memorycardflip.model.GameStatus;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * [UC12 - Count down timer]
 * Service phụ trách bộ đếm ngược của một ván chơi.
 *
 * Timeline chạy mỗi 1 giây, giảm timeRemaining trong GameState,
 * sau đó báo về GameController để cập nhật UI hoặc xử lý hết giờ.
 */
public class GameTimerService {

    public interface Listener {
        void onTick();
        void onTimeUp();
    }

    private GameState gameState;
    private final Listener listener;
    private Timeline timer;

    public GameTimerService(GameState gameState, Listener listener) {
        this.gameState = gameState;
        this.listener = listener;
    }
    public void updateGameState(GameState newState) {
        this.gameState = newState;
    }
    /**
     * Bắt đầu đếm ngược khi game đang ở trạng thái PLAYING.
     * Nếu timer chưa có thì tạo Timeline mới, nếu đã có thì chạy tiếp.
     */
    public void start() {
        if (gameState == null || gameState.getStatus() != GameStatus.PLAYING) {
            return;
        }

        if (timer == null) {
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
            timer.setCycleCount(Timeline.INDEFINITE);
        }

        if (timer.getStatus() != Timeline.Status.RUNNING) {
            timer.play();
        }
    }

    /**
     * Tạm dừng timer nhưng vẫn giữ Timeline để có thể chạy tiếp khi resume.
     */
    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Hủy timer hiện tại, dùng khi kết thúc ván hoặc tạo ván mới.
     */
    public void dispose() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    /**
     * Một nhịp đếm ngược:
     * - giảm thời gian còn lại 1 giây
     * - báo UI cập nhật nhãn thời gian/thanh tiến trình
     * - nếu hết giờ thì dừng timer và báo thua.
     */
    private void tick() {
        if (gameState == null || gameState.getStatus() != GameStatus.PLAYING) {
            return;
        }

        gameState.decrementTime();
        listener.onTick();

        if (gameState.isTimeUp()) {
            stop(); // ✅ FIX QUAN TRỌNG
            listener.onTimeUp();
        }
    }
}
