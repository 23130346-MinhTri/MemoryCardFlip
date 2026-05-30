package com.example.memorycardflip.model;

/**
 * Card face categories. Each category owns a pool of symbols used by the deck.
 */
public enum CardType {

    UI_ICONS("UI Icons", new String[]{
            "\u2699", "\u2705", "\u274C", "\uD83D\uDD14",
            "\uD83D\uDD0D", "\u2302", "\uD83D\uDCBE", "\uD83D\uDDD1",
            "\uD83D\uDD17", "\uD83D\uDCCB", "\uD83D\uDD16", "\uD83D\uDCE4",
            "\u2B05", "\u27A1", "\uD83D\uDD10", "\u26A1"
    }),

    DEVICES("Devices", new String[]{
            "\uD83D\uDCBB", "\uD83D\uDDA5", "\uD83D\uDCF1", "\u2328",
            "\uD83D\uDDB1", "\uD83D\uDDA8", "\uD83D\uDCF7", "\uD83D\uDCF9",
            "\uD83C\uDFAE", "\uD83D\uDD79", "\uD83D\uDCE1", "\uD83D\uDD0B",
            "\uD83D\uDCFA", "\uD83C\uDFA7", "\uD83C\uDF99", "\uD83D\uDD0C"
    }),

    PROGRAMMING("Programming", new String[]{
            "\uD83E\uDD16", "\uD83D\uDC7E", "\uD83E\uDDE0", "\uD83D\uDCA1",
            "\uD83D\uDD27", "\uD83D\uDEE0", "\uD83C\uDFD7", "\uD83D\uDD29",
            "\uD83D\uDCD0", "\uD83D\uDCCF", "\uD83E\uDDEA", "\uD83D\uDD2C",
            "\uD83D\uDD2D", "\uD83D\uDDDC", "\u2697", "\uD83E\uDDF2"
    }),

    CLOUD("Cloud & Network", new String[]{
            "\uD83C\uDF10", "\u2601", "\uD83D\uDCF6", "\uD83D\uDEF0",
            "\uD83D\uDD78", "\uD83D\uDCE8", "\uD83D\uDCAC", "\uD83D\uDDC4",
            "\uD83D\uDCCA", "\uD83D\uDCC8", "\uD83D\uDCC9", "\uD83D\uDDC2",
            "\uD83D\uDCC1", "\uD83D\uDD22", "\uD83E\uDDEE", "\uD83D\uDD11"
    }),

    ANIMAL("Animal", new String[]{
            "\uD83D\uDC36", "\uD83D\uDC31", "\uD83D\uDC2D", "\uD83D\uDC39",
            "\uD83D\uDC30", "\uD83E\uDD8A", "\uD83D\uDC3B", "\uD83D\uDC3C",
            "\uD83D\uDC28", "\uD83D\uDC2F", "\uD83E\uDD81", "\uD83D\uDC2E",
            "\uD83D\uDC38", "\uD83D\uDC35", "\uD83D\uDC14", "\uD83D\uDC27"
    }),

    FRUIT("Fruit", new String[]{
            "\uD83C\uDF4E", "\uD83C\uDF4A", "\uD83C\uDF4B", "\uD83C\uDF47",
            "\uD83C\uDF53", "\uD83C\uDF51", "\uD83C\uDF52", "\uD83C\uDF4D",
            "\uD83E\uDD5D", "\uD83C\uDF4C", "\uD83E\uDD6D", "\uD83C\uDF48",
            "\uD83C\uDF50", "\uD83C\uDF45", "\uD83E\uDD65", "\uD83E\uDD51"
    });

    private final String displayName;
    private final String[] symbols;

    CardType(String displayName, String[] symbols) {
        this.displayName = displayName;
        this.symbols = symbols;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol(int index) {
        if (index < 0 || index >= symbols.length) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for type " + name()
                            + " (max " + (symbols.length - 1) + ")"
            );
        }
        return symbols[index];
    }

    public int symbolCount() {
        return symbols.length;
    }

    public String[] getSymbols() {
        return symbols.clone();
    }
}