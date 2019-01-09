package com.garethevans.church.opensongtablet.core.music.rythm;

/**
 * A {@link MusicalValueVariation} is a {@link Fraction} that modifies a {@link MusicalValue}. There are particular symbols to
 * visualize a {@link MusicalValueVariation} in a partiture.
 */
public enum MusicalValueVariation implements Fraction {

    /**
     * No variation.
     */
    NONE(1, 1, ""),

    /**
     * Increases the {@link MusicalValue} by adding half of its value. Visualized as a single dot right to the musical
     * symbol.
     */
    PUNCTURED(3, 2, "."),

    /**
     * Like {@link #PUNCTURED} but additionally adding a quarter of the value. Visualized as a double dot right to the
     * musical symbol.
     */
    DOUBLE_PUNCTURED(7, 4, ".."),

    /**
     * Increases the {@link MusicalValue} such that three tones of that value actually last as long as two {@link #NONE
     * regular} ones. Visualized as a small three ({@code 3} centered below or on top of the tones. Typically used with
     * barred tones otherwise a bar-bracket is added to group the tones.
     */
    TRIPLET(2, 3, "(3");

    private final int beats;

    private final int fraction;

    private final String text;

    private MusicalValueVariation(int beats, int fraction, String text) {
        this.beats = beats;
        this.fraction = fraction;
        this.text = text;
    }

    @Override
    public int getBeats() {
        return this.beats;
    }

    @Override
    public int getFaction() {
        return this.fraction;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
