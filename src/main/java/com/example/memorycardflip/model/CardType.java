package com.example.memorycardflip.model;

/**
 * Loại thẻ bài — mỗi loại có bộ emoji/symbol riêng.
 */
public enum CardType {

    ANIMAL("Động vật", new String[]{
            "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼",
            "🐨","🐯","🦁","🐮","🐸","🐵","🐔","🐧"
    }),
    FRUIT("Trái cây", new String[]{
            "🍎","🍊","🍋","🍇","🍓","🍑","🍒","🍍",
            "🥝","🍌","🥭","🍈","🍐","🫐","🍅","🥥"
    }),
    FLAG("Cờ các nước", new String[]{
            "🇻🇳","🇺🇸","🇯🇵","🇰🇷","🇨🇳","🇬🇧","🇫🇷","🇩🇪",
            "🇮🇹","🇧🇷","🇦🇺","🇨🇦","🇮🇳","🇷🇺","🇲🇽","🇪🇸"
    }),
    EMOJI("Biểu tượng", new String[]{
            "😀","😍","🤩","😎","🥳","😴","🤔","😱",
            "🎉","🔥","⭐","💎","🎯","🚀","🌈","💡"
    });

    private final String displayName;
    private final String[] symbols;

    CardType(String displayName, String[] symbols) {
        this.displayName = displayName;
        this.symbols     = symbols;
    }

    public String getDisplayName() { return displayName; }

    public String getSymbol(int index) {
        if (index < 0 || index >= symbols.length) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for type " + name()
                            + " (max " + (symbols.length - 1) + ")"
            );
        }
        return symbols[index];
    }

    public int symbolCount() { return symbols.length; }

    public String[] getSymbols() { return symbols.clone(); }
}