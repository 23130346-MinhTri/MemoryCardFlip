package com.example.memorycardflip.ui;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class CardFlipView extends StackPane {

    private static final Duration FLIP_HALF_DURATION = Duration.millis(90);
    private static final String CARD_BACK_IMAGE = "/assets/icons/logogame.png";
    private static final String BACK_TEXT = "?";

    @FXML private Label contentLabel;
    @FXML private ImageView backImageView;
    @FXML private ImageView frontImageView;
    private boolean faceUp;
    private boolean matched;
    private Runnable flipRequestedHandler;

    public CardFlipView() {
        loadLayout();
        Image backImage = loadResourceImage(CARD_BACK_IMAGE);
        if (backImage != null) {
            backImageView.setImage(backImage);
        }
        faceUp = false;
        frontImageView.setVisible(false);
        backImageView.setVisible(true);
        contentLabel.setText(BACK_TEXT);

        setBackTextStyle();
        getStyleClass().add("card-back");

        // FIX 1: Gắn mouse handler vào chính StackPane này.
        // Trước đây setOnFlipRequested chỉ lưu Runnable nhưng không bao giờ
        // gọi setOnMouseClicked → click hoàn toàn bị bỏ qua.
        setOnMouseClicked(event -> {
            if (flipRequestedHandler != null && !matched && !isDisabled()) {
                flipRequestedHandler.run();
            }
        });
    }
    // 2.3.1
    public void setOnFlipRequested(Runnable flipRequestedHandler) {
        this.flipRequestedHandler = flipRequestedHandler;
    }

    public void setCardSize(double width, double height) {
        setMinSize(width, height);
        setPrefSize(width, height);
        setMaxSize(width, height);
    }
/*
*2.3.5 Hiển thị mặt trước thẻ kèm hiệu ứng lật khi người chơi chọn thẻ.
*/
    public void showFront(String symbol, String imageUrl) {
        if (matched) return;
        if (faceUp) return;
        animateFlip(() -> {
            faceUp = true;
            if (imageUrl != null && !imageUrl.isBlank()) {
                Image frontImage = loadResourceImage(imageUrl);
                if (frontImage != null) {
                    frontImageView.setImage(frontImage);
                    frontImageView.setVisible(true);
                    contentLabel.setText("");
                } else {
                    frontImageView.setVisible(false);
                    contentLabel.setText(symbol);
                    setFrontTextStyle();
                }
            } else {
                frontImageView.setVisible(false);
                contentLabel.setText(symbol);
                setFrontTextStyle();
            }
            backImageView.setVisible(false);
            refreshStyle();
        });
    }
/*
*9.5.3 Ẩn mặt trước và quay lại mặt sau khi không ghép được cặp, cũng với hiệu ứng lật.
*/
    public void showBack() {
        if (matched) return;
        if (!faceUp) return;
        animateFlip(() -> {
            faceUp = false;
            frontImageView.setVisible(false);
            contentLabel.setText(BACK_TEXT);
            setBackTextStyle();
            backImageView.setVisible(true);
            refreshStyle();
        });
    }
// 9.4.3 Đánh dấu thẻ đã được ghép cặp thành công, giữ nguyên mặt trước và vô hiệu hóa tương tác.
    public void setMatched(boolean matched) {
        this.matched = matched;
        if (matched) {
            getStyleClass().add("card-matched");
            setDisable(true);
            setVisible(true);
            setManaged(true);
            setMouseTransparent(true);
        } else {
            getStyleClass().remove("card-matched");
            setDisable(false);
            setVisible(true);
            setManaged(true);
            setMouseTransparent(false);
        }
    }

    private void refreshStyle() {
        getStyleClass().removeAll("card-back", "card-front");
        getStyleClass().add(faceUp ? "card-front" : "card-back");
    }

    private void animateFlip(Runnable swapFaceAction) {
        ScaleTransition close = new ScaleTransition(FLIP_HALF_DURATION, this);
        close.setFromX(1.0);
        close.setToX(0.0);

        ScaleTransition open = new ScaleTransition(FLIP_HALF_DURATION, this);
        open.setFromX(0.0);
        open.setToX(1.0);

        close.setOnFinished(event -> {
            swapFaceAction.run();
            open.play();
        });

        close.play();
    }

    private Image loadResourceImage(String resourcePath) {
        URL resourceUrl = getClass().getResource(resourcePath);
        if (resourceUrl == null) {
            return null;
        }
        return new Image(resourceUrl.toExternalForm());
    }

    private void loadLayout() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/card-flip-view.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            setBackTextStyle();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load card-flip-view.fxml", exception);
        }
    }

    private void setFrontTextStyle() {
        contentLabel.getStyleClass().remove("card-content-label-back");
        if (!contentLabel.getStyleClass().contains("card-content-label-front")) {
            contentLabel.getStyleClass().add("card-content-label-front");
        }
    }

    private void setBackTextStyle() {
        contentLabel.getStyleClass().remove("card-content-label-front");
        if (!contentLabel.getStyleClass().contains("card-content-label-back")) {
            contentLabel.getStyleClass().add("card-content-label-back");
        }
    }
}