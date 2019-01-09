package com.garethevans.church.opensongtablet.core.music.tone;


import com.garethevans.church.opensongtablet.core.music.harmony.Interval;
import com.garethevans.church.opensongtablet.core.music.harmony.TonalSystem;

public enum DiatonicStep implements Interval {

    /**
     * The first tone of the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getDiatonicScale() diatonic scale}.
     *
     * @see com.garethevans.church.opensongtablet.core.music.harmony.DiatonicInterval#UNISON
     * @see TonePitchEnglish#C
     */
    S0,

    /**
     * The second tone of the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getDiatonicScale() diatonic scale}
     * or one tone higher than {@link #S0}.
     *
     * @see com.garethevans.church.opensongtablet.core.music.harmony.DiatonicInterval#SECOND
     * @see TonePitchEnglish#D
     */
    S1,

    /**
     * The third tone of the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getDiatonicScale() diatonic scale}
     * or two tones higher than {@link #S0}.
     *
     * @see com.garethevans.church.opensongtablet.core.music.harmony.DiatonicInterval#THIRD
     * @see TonePitchEnglish#E
     */
    S2,

    /**
     * The fourth tone of the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getDiatonicScale() diatonic scale}
     * or three tones higher than {@link #S0}.
     *
     * @see com.garethevans.church.opensongtablet.core.music.harmony.DiatonicInterval#FOURTH
     * @see TonePitchEnglish#F
     */
    S3,

    /**
     * The fifth tone of the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getDiatonicScale() diatonic scale}
     * or four tones higher than {@link #S0}.
     *
     * @see com.garethevans.church.opensongtablet.core.music.harmony.DiatonicInterval#FIFTH
     * @see TonePitchEnglish#G
     */
    S4,

    /**
     * The sixth tone of the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getDiatonicScale() diatonic scale}
     * or five tones higher than {@link #S0}.
     *
     * @see com.garethevans.church.opensongtablet.core.music.harmony.DiatonicInterval#SIXTH
     * @see TonePitchEnglish#A
     */
    S5,

    /**
     * The seventh tone of the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getDiatonicScale() diatonic scale}
     * or six tones higher than {@link #S0}.
     *
     * @see com.garethevans.church.opensongtablet.core.music.harmony.DiatonicInterval#SEVENTH
     * @see TonePitchEnglish#B
     */
    S6;

    private static final DiatonicStep[] STEPS = values();

    /**
     * @return the number of diatonic steps.
     */
    public int get() {
        return ordinal();
    }

    public DiatonicStep next() {
        if (this == S6) {
            return S0;
        } else {
            return STEPS[get() + 1];
        }
    }

    public DiatonicStep previous() {
        if (this == S0) {
            return S6;
        } else {
            return STEPS[get() - 1];
        }
    }

    public DiatonicStep add(int diatonicSteps) {
        return of(get() + diatonicSteps);
    }

    public DiatonicStep add(DiatonicStep step) {
        return of(get() + step.get());
    }

    public DiatonicStep subtract(DiatonicStep step) {
        return of(get() + step.get());
    }

    public static DiatonicStep of(int diatonicStep) {
        int index = diatonicStep;
        if ((index < 0) || (index >= 7)) {
            index = index % 7;
            if (index < 0) {
                index = index + 7;
            }
        }
        return STEPS[index];
    }

    @Override
    public int getChromaticSteps(TonalSystem system) {
        if (system == null) {
            return Integer.MIN_VALUE;
        }
        return system.getChromaticSteps(get());
    }

    @Override
    public int getDiatonicSteps(TonalSystem system) {
        return get();
    }
}
