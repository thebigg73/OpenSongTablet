package com.garethevans.church.opensongtablet.core.music.stave;

import java.util.HashMap;
import java.util.Map;

public class BarType implements BarObject {

    private static final Map<String, BarType> SYMBOL_MAP = new HashMap<>();

    public static final BarType SINGLE = new BarType("|");

    public static final BarType DOUBLE = new BarType("||");

    public static final BarType THICK_THIN = new BarType("[|");

    public static final BarType THIN_THICK = new BarType("|]");

    public static final BarType START_REPEAT = new BarType("|:");

    public static final BarType END_REPEAT = new BarType(":|");

    public static final BarType START_END_REPEAT = new BarType("::");

    private final String symbol;

    static {
        SYMBOL_MAP.put(":||:", START_END_REPEAT);
        SYMBOL_MAP.put(":|:", START_END_REPEAT);
    }

    protected BarType(String symbol) {
        super();
        this.symbol = symbol;
        SYMBOL_MAP.put(symbol, this);
    }

    @Override
    public boolean isLeftBarOnly() {
        return (this == START_REPEAT);
    }

    @Override
    public boolean isRightBarOnly() {
        return (this == END_REPEAT);
    }

    /**
     * @return {@code true} for a repeat type ({@link #START_REPEAT}, {@link #END_REPEAT}, {@link #START_END_REPEAT}), {@code false} otherwise.
     */
    public boolean isRepeat() {
        return (this == START_REPEAT) || (this == END_REPEAT) || (this == START_END_REPEAT);
    }

    /**
     * @return the notation symbol if this bar type.
     */
    public String getSymbol() {
        return this.symbol;
    }

    @Override
    public String toString() {
        return this.symbol;
    }

    public static BarType of(String symbol) {
        return SYMBOL_MAP.get(symbol);
    }
}
