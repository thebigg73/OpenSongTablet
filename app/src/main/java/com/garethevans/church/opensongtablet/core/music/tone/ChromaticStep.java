package com.garethevans.church.opensongtablet.core.music.tone;

import com.garethevans.church.opensongtablet.core.music.harmony.Interval;
import com.garethevans.church.opensongtablet.core.music.harmony.TonalSystem;

/**
 * The {@link ChromaticStep} represents the number of semitones a {@link TonePitch} is higher than
 * {@link TonePitchEnglish#C}. Or in other words the index of a {@link TonePitch} in the
 * {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getChromaticScale() chromatic scale}
 * of {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#C_MAJOR}.
 *
 * @see TonePitch#getStep()
 */
public enum ChromaticStep implements Interval {

    /**
     * The first tone of {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#C_MAJOR}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#PERFECT_UNISON
     * @see TonePitchEnglish#C
     */
    S0,

    /**
     * One semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#MINOR_SECOND
     * @see TonePitchEnglish#C_SHARP
     */
    S1,

    /**
     * Two semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#MAJOR_SECOND
     * @see TonePitchEnglish#D
     */
    S2,

    /**
     * Three semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#MINOR_THIRD
     * @see TonePitchEnglish#D_SHARP
     */
    S3,

    /**
     * Four semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#MAJOR_THIRD
     * @see TonePitchEnglish#E
     */
    S4,

    /**
     * Five semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#PERFECT_FOURTH
     * @see TonePitchEnglish#F
     */
    S5,

    /**
     * Six semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#DIMINISHED_FIFTH
     * @see TonePitchEnglish#F_SHARP
     */
    S6,

    /**
     * Seven semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#PERFECT_FIFTH
     * @see TonePitchEnglish#G
     */
    S7,

    /**
     * Eight semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#MINOR_SIXT
     * @see TonePitchEnglish#G_SHARP
     */
    S8,

    /**
     * Nine semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#MAJOR_SIXT
     * @see TonePitchEnglish#A
     */
    S9,

    /**
     * Ten semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#MINOR_SEVENTH
     * @see TonePitchEnglish#B_FLAT
     */
    S10,

    /**
     * Eleven semiton higher than {@link #S0}.
     * @see com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval#MAJOR_SEVENTH
     * @see TonePitchEnglish#B
     */
    S11;

    private static final ChromaticStep[] STEPS = values();

    /**
     * @return the number of chromatic steps.
     */
    public int get() {
        return ordinal();
    }

    public ChromaticStep next() {
        if (this == S11) {
            return S0;
        } else {
            return STEPS[get() + 1];
        }
    }

    public ChromaticStep previous() {
        if (this == S0) {
            return S11;
        } else {
            return STEPS[get() - 1];
        }
    }

    public ChromaticStep add(int chromaticSteps) {
        return of(get() + chromaticSteps);
    }

    public ChromaticStep add(ChromaticStep step) {
        return of(get() + step.get());
    }

    public ChromaticStep subtract(ChromaticStep step) {
        return of(get() - step.get());
    }

    public static ChromaticStep of(int chromaticStep) {
        int index = chromaticStep;
        if ((index < 0) || (index >= 12)) {
            index = index % 12;
            if (index < 0) {
                index = index + 12;
            }
        }
        return STEPS[index];
    }

    @Override
    public int getChromaticSteps(TonalSystem system) {
        return get();
    }

    @Override
    public int getDiatonicSteps(TonalSystem system) {
        if (system == null) {
            return Integer.MIN_VALUE;
        }
        return system.getDiatonicSteps(get(), true);
    }

    @Override
    public String toString() {
        return Integer.toString(get());
    }
}
