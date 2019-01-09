package com.garethevans.church.opensongtablet.core.music.instrument.string;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;

public abstract class FrettedStringInstrument extends StringInstrument {

    private final int maxFret;

    public FrettedStringInstrument(int maxFret, Tone... tuning) {
        super(tuning);
        this.maxFret = maxFret;
    }

    /**
     * @return the maximum fret number that can be played (comfortably) on this instrument. Will depend
     *         e.g. on {@link #isElectric() electric} vs. {@link #isAcoustic() acoustic} instruments
     *         as well as on features like <em>cutaway</em>. Every reasonable {@link FrettedStringInstrument}
     *         should at least support 12 frets (one octave).
     */
    public int getMaxFret() {
        return this.maxFret;
    }
}
