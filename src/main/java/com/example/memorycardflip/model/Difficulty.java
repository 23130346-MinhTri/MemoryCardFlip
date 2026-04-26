package com.example.memorycardflip.model;

/**
 * Độ khó của game — xác định kích thước lưới và thời gian.
 *
 * <pre>
 * EASY   → lưới 4×4 = 16 ô = 8 cặp  → 60 giây
 * MEDIUM → lưới 6×6 = 36 ô = 18 cặp → 90 giây
 * HARD   → lưới 8×8 = 64 ô = 32 cặp → 120 giây
 * </pre>
 */
public enum Difficulty {

    EASY  ("Dễ",  4, 60),
    MEDIUM("Vừa", 6, 90),
    HARD  ("Khó", 8, 120);

    private final String displayName;
    private final int gridSize;     // Số cột = số hàng
    private final int timeLimit;    // Giây

    Difficulty(String displayName, int gridSize, int timeLimit) {
        this.displayName = displayName;
        this.gridSize    = gridSize;
        this.timeLimit   = timeLimit;
    }

    public String getDisplayName() { return displayName; }
    public int getGridSize()       { return gridSize; }
    public int getTimeLimit()      { return timeLimit; }

    /** Tổng số ô trong lưới */
    public int totalCells()  { return gridSize * gridSize; }

    /** Tổng số cặp cần ghép */
    public int totalPairs()  { return totalCells() / 2; }

    @Override
    public String toString() {
        return String.format("%s (%dx%d, %ds)", displayName, gridSize, gridSize, timeLimit);
    }
}