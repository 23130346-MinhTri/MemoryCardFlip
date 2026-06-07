package com.memorycardflip.ui;

import com.example.memorycardflip.ui.CardFlipView;
import javafx.application.Platform;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CardFlipView Highlight Tests")
class CardFlipViewTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already started
        }
    }

    private CardFlipView cardFlipView;

    @BeforeEach
    void setUp() {
        cardFlipView = new CardFlipView();
    }

    @Test
    @DisplayName("showHighlight(true) -> Kích hoạt viền phát sáng Glow dropshadow xanh dương")
    void testShowHighlightTrue() {
        cardFlipView.showHighlight(true);
        assertEquals("-fx-effect: dropshadow(gaussian, #2563EB, 18, 0.75, 0, 0);", cardFlipView.getStyle());
    }

    @Test
    @DisplayName("showHighlight(false) -> Tắt hiệu ứng phát sáng, khôi phục lại style trống")
    void testShowHighlightFalse() {
        cardFlipView.showHighlight(true); // turn on first
        cardFlipView.showHighlight(false); // turn off
        assertEquals("", cardFlipView.getStyle());
    }
}
