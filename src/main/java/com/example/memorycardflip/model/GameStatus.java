package com.example.memorycardflip.model;

/**
 * Trạng thái vòng đời của một ván game.
 */
public enum GameStatus {
    IDLE,       // Chưa bắt đầu (màn hình menu)
    PLAYING,    // Đang chơi, nhận input từ người dùng
    CHECKING,   // Đang kiểm tra cặp thẻ (khóa input tạm)
    PAUSED,     // Tạm dừng
    WON,        // Thắng — ghép hết tất cả cặp
    LOST        // Thua — hết giờ
}