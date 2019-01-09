package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;

public class Bar implements BarObject {

    private final BarType type;

    private final int ending;

    public Bar(BarType type) {
        this(type, 0);
    }

    public Bar(BarType type, int ending) {
        super();
        this.type = type;
        this.ending = ending;
        assert (ending == 0) || !type.isRightBarOnly();
    }

    public BarType getType() {
        return this.type;
    }

    public boolean isEnding() {
        return this.ending > 0;
    }

    public int getEnding() {
        return this.ending;
    }

    @Override
    public boolean isLeftBarOnly() {
        if (isEnding()) {
            return true;
        }
        return this.type.isLeftBarOnly();
    }

    @Override
    public boolean isRightBarOnly() {
        return this.type.isRightBarOnly();
    }

    @Override
    public String toString() {
        if (this.ending > 0) {
            return this.type.getSymbol() + this.ending;
        }
        return this.type.getSymbol();
    }

    public static Bar parse(String string) {
        return parse(string, 0);
    }

    public static Bar parse(String string, int index) {

        int len = string.length();
        int max = index + 4;
        if (max >= len) {
            max = len - 1;
        }
        BarType type = null;
        int end;
        for (end = max; end > index; end--) {
            String key = string.substring(index, end);
            type = BarType.of(key);
            if (type != null) {
                break;
            }
        }
        if (type == null) {
            return null;
        }
        int ending = 0;
        if (end < len) {
            char c = string.charAt(end);
            if (ListCharFilter.DIGITS.accept(c)) {
                ending = c - '0';
            }
        }
        return new Bar(type, ending);
    }
}
