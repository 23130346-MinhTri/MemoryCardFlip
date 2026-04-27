package com.example.memorycardflip.controller;

import com.example.memorycardflip.model.Card;
import com.example.memorycardflip.model.CardType;
import com.example.memorycardflip.model.Difficulty;
import com.example.memorycardflip.ui.CardFlipView;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameController {

    @FXML private GridPane cardGrid;

    @FXML private Button btnRestart;

    private Difficulty difficulty = Difficulty.EASY;
    private final List<Card> cards = new ArrayList<>();
    private final Map<String, CardFlipView> viewsByCardId = new HashMap<>();

    private Card firstCard;
    private Card secondCard;
    private boolean resolvingTurn;

    @FXML
    public void initialize() {
        btnRestart.setOnAction(event -> startNewGame());
        startNewGame();
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty == null) {
            return;
        }
        this.difficulty = difficulty;
        startNewGame();
    }

    private void startNewGame() {
        cards.clear();
        viewsByCardId.clear();
        firstCard = null;
        secondCard = null;
        resolvingTurn = false;

        buildDeck();
        renderBoard();
    }

    private void buildDeck() {
        List<String> symbolPool = buildSymbolPool();
        List<String> imagePool = loadFrontFaceImages();
        int totalPairs = difficulty.totalPairs();

        int position = 0;
        for (int pairIndex = 0; pairIndex < totalPairs; pairIndex++) {
            String pairId = "pair-" + pairIndex;
            String symbol = symbolPool.get(pairIndex);
            String imageUrl = imagePool.isEmpty() ? null : imagePool.get(pairIndex % imagePool.size());

            cards.add(new Card("card-" + position, pairId, CardType.EMOJI, symbol, imageUrl, position));
            position++;
            cards.add(new Card("card-" + position, pairId, CardType.EMOJI, symbol, imageUrl, position));
            position++;
        }

        Collections.shuffle(cards);
    }

    private List<String> buildSymbolPool() {
        List<String> symbols = new ArrayList<>();
        for (CardType type : CardType.values()) {
            Collections.addAll(symbols, type.getSymbols());
        }
        return symbols;
    }

    private List<String> loadFrontFaceImages() {
        try {
            URL directoryUrl = getClass().getResource("/assets/icons");
            if (directoryUrl == null || !"file".equalsIgnoreCase(directoryUrl.getProtocol())) {
                return List.of();
            }

            Path directory = Path.of(directoryUrl.toURI());
            try (var pathStream = Files.list(directory)) {
                return pathStream
                        .filter(Files::isRegularFile)
                        .filter(path -> isSupportedImage(path.getFileName().toString()))
                        .filter(path -> !path.getFileName().toString().toLowerCase().startsWith("logogame"))
                        .sorted()
                        .map(path -> "/assets/icons/" + path.getFileName())
                        .collect(Collectors.toList());
            }
        } catch (IOException | URISyntaxException exception) {
            return List.of();
        }
    }

    private boolean isSupportedImage(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".webp");
    }

    private void renderBoard() {
        cardGrid.getChildren().clear();
        cardGrid.getColumnConstraints().clear();
        cardGrid.getRowConstraints().clear();

        int gridSize = difficulty.getGridSize();
        double cardWidth = switch (difficulty) {
            case EASY -> 120;
            case MEDIUM -> 82;
            case HARD -> 58;
        };
        double cardHeight = switch (difficulty) {
            case EASY -> 140;
            case MEDIUM -> 98;
            case HARD -> 70;
        };
        double gap = switch (difficulty) {
            case EASY -> 10;
            case MEDIUM -> 8;
            case HARD -> 6;
        };

        cardGrid.setHgap(gap);
        cardGrid.setVgap(gap);
        cardGrid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            CardFlipView view = new CardFlipView();
            view.setCardSize(cardWidth, cardHeight);
            view.setOnFlipRequested(() -> onCardClicked(card, view));

            int row = i / gridSize;
            int col = i % gridSize;
            cardGrid.add(view, col, row);

            viewsByCardId.put(card.getId(), view);
        }
    }

    private void onCardClicked(Card card, CardFlipView view) {
        if (resolvingTurn || !card.isClickable()) {
            return;
        }

        card.flip();
        view.showFront(card.getSymbol(), card.getImageURL());

        if (firstCard == null) {
            firstCard = card;
            return;
        }

        secondCard = card;
        resolveTurn();
    }

    private void resolveTurn() {
        if (firstCard == null || secondCard == null) {
            return;
        }

        CardFlipView firstView = viewsByCardId.get(firstCard.getId());
        CardFlipView secondView = viewsByCardId.get(secondCard.getId());
        if (firstView == null || secondView == null) {
            clearSelection();
            return;
        }

        if (firstCard.isPairOf(secondCard)) {
            firstCard.match();
            secondCard.match();
            resolvingTurn = true;
            PauseTransition pause = new PauseTransition(Duration.millis(220));
            pause.setOnFinished(event -> {
                firstView.setMatched(true);
                secondView.setMatched(true);
                clearSelection();
                resolvingTurn = false;
            });
            pause.play();
            return;
        }

        resolvingTurn = true;
        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(event -> {
            firstCard.faceDown();
            secondCard.faceDown();
            firstView.showBack();
            secondView.showBack();
            clearSelection();
            resolvingTurn = false;
        });
        pause.play();
    }

    private void clearSelection() {
        firstCard = null;
        secondCard = null;
    }
}
