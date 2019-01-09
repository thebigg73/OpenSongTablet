package com.garethevans.church.opensongtablet.core.music.tone;

import com.garethevans.church.opensongtablet.core.music.harmony.EnharmonicStyle;

/**
 * The {@link EnharmonicType} specifies the enharmonic {@link #getStyle() style} and {@link #getSignCount() sign count}
 * of a {@link TonePitch}.
 *
 * @see TonePitch#getEnharmonicType()
 */
public enum EnharmonicType {

    NORMAL("0", EnharmonicStyle.NORMAL, 0),

    SINGLE_FLAT("1b", EnharmonicStyle.FLAT, 1),

    DOUBLE_FLAT("2b", EnharmonicStyle.FLAT, 2),

    SINGLE_SHARP("1#", EnharmonicStyle.SHARP, 1),

    DOUBLE_SHARP("2#", EnharmonicStyle.SHARP, 2);

    private final String title;

    private final EnharmonicStyle style;

    private final int signCount;

    EnharmonicType(String title, EnharmonicStyle enharmonicStyle, int signCount) {
        this.title = title;
        this.style = enharmonicStyle;
        this.signCount = signCount;
    }

    /**
     * @return the {@link EnharmonicStyle}.
     */
    public EnharmonicStyle getStyle() {
        return this.style;
    }

    public EnharmonicType sharpen() {
        switch (this) {
            case DOUBLE_FLAT:
                return SINGLE_FLAT;
            case SINGLE_FLAT:
                return NORMAL;
            case NORMAL:
                return SINGLE_SHARP;
            case SINGLE_SHARP:
                return DOUBLE_SHARP;
        }
        return null;
    }

    public EnharmonicType flatten() {
        switch (this) {
            case DOUBLE_SHARP:
                return SINGLE_SHARP;
            case SINGLE_SHARP:
                return NORMAL;
            case NORMAL:
                return SINGLE_FLAT;
            case SINGLE_FLAT:
                return DOUBLE_FLAT;
        }
        return null;
    }

    /**
     * @return the number of enharmonic signs with {@code 0} for {@link EnharmonicStyle#NORMAL none},
     * {@code 1} for a {@link #SINGLE_FLAT} or {@link #SINGLE_SHARP},
     * and {@code 2} for {@link #DOUBLE_FLAT} or {@link #DOUBLE_FLAT}.
     */
    public int getSignCount() {
        return this.signCount;
    }

    /**
     * @return {@code -2} for {@link #DOUBLE_FLAT}, {@code -1} for {@link #SINGLE_FLAT}, {@code 0}
     * for {@link #NORMAL}, {@code 1} for {@link #SINGLE_SHARP}, and {@code 2} for {@link #DOUBLE_SHARP}.
     */
    public int getSignOffset() {
        if (this.style.isFlat()) {
            return -this.signCount;
        }
        return this.signCount;
    }

    @Override
    public String toString() {
        return this.title;
    }
}
