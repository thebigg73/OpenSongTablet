/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;

/**
 * Enum with the {@link #getChromaticSteps() chromatic} {@link Interval}s.
 *
 * @author hohwille
 */
public class ChromaticInterval implements Interval {

    /**
     * The "empty" interval.
     */
    public static final ChromaticInterval PERFECT_UNISON = new ChromaticInterval(0, 0);

    /**
     * One semitone step (one diatonic step).
     */
    public static final ChromaticInterval MINOR_SECOND = new ChromaticInterval(1, 1);

    /**
     * Two semitone steps (one diatonic step).
     */
    public static final ChromaticInterval MAJOR_SECOND = new ChromaticInterval(2, 1);

    /**
     * Three semitone steps (two diatonic steps). The third of a {@link TonalSystem#MINOR minor} {@link Chord} or {@link MusicalKey key}.
     */
    public static final ChromaticInterval MINOR_THIRD = new ChromaticInterval(3, 2);

    /**
     * Four semitone steps (two diatonic steps). The third of a {@link TonalSystem#MAJOR major} {@link Chord} or {@link MusicalKey key}.
     */
    public static final ChromaticInterval MAJOR_THIRD = new ChromaticInterval(4, 2);

    /**
     * Five semitone steps (three diatonic steps). Same as {@link DiatonicInterval#FOURTH} also called <em>quartum</em>.
     */
    public static final ChromaticInterval PERFECT_FOURTH = new ChromaticInterval(5, 3);

    /**
     * Six semitone steps (three diatonic steps).
     */
    public static final ChromaticInterval AUGMENTED_FOURTH = new ChromaticInterval(6, 3);

    /**
     * Six semitone steps (four diatonic steps).
     */
    public static final ChromaticInterval DIMINISHED_FIFTH = new ChromaticInterval(6, 4);

    /**
     * Seven semitone steps (four diatonic steps). Same as {@link DiatonicInterval#FIFTH} also called <em>quintium</em>, the total interval of a
     * regular {@link TonalSystem#MAJOR major} or {@link TonalSystem#MINOR minor} {@link Chord}.
     */
    public static final ChromaticInterval PERFECT_FIFTH = new ChromaticInterval(7, 4);

    /**
     * Seven semitone steps (five diatonic steps).
     */
    public static final ChromaticInterval DIMINISHED_SIXT = new ChromaticInterval(7, 5);

    /**
     * Eight semitone steps (five diatonic steps).
     */
    public static final ChromaticInterval MINOR_SIXT = new ChromaticInterval(8, 5);

    /**
     * Nine semitone steps (five diatonic steps).
     */
    public static final ChromaticInterval MAJOR_SIXT = new ChromaticInterval(9, 5);

    /**
     * Nine semitone steps (six diatonic steps).
     */
    public static final ChromaticInterval DIMINISHED_SEVENTH = new ChromaticInterval(9, 6);

    /**
     * Ten semitone steps (five diatonic steps).
     */
    public static final ChromaticInterval AUGMENTED_SIXT = new ChromaticInterval(10, 5);

    /**
     * Ten semitone steps (six diatonic steps). The regular {@link DiatonicInterval#SEVENTH seventh} used e.g. for {@link ChordExtension#_7}.
     * Especially important for dominant {@link Chord}s.
     */
    public static final ChromaticInterval MINOR_SEVENTH = new ChromaticInterval(10, 6);

    /**
     * Eigth semitone steps.
     */
    public static final ChromaticInterval MAJOR_SEVENTH = new ChromaticInterval(11, 6);

    /**
     * Ten semitone steps (five diatonic steps).
     */
    public static final ChromaticInterval AUGMENTED_SEVENTH = new ChromaticInterval(12, 6);

    /**
     * Twelve semitone steps. Results in the logically same {@link TonePitch} on a higher octave.
     */
    public static final ChromaticInterval PERFECT_OCTAVE = new ChromaticInterval(12, 7);

    /**
     * Thirteen semitone steps.
     */
    public static final ChromaticInterval MINOR_NINTH = new ChromaticInterval(13, 8);

    /**
     * Fourteen semitone steps.
     */
    public static final ChromaticInterval MAJOR_NINTH = new ChromaticInterval(14, 8);

    /**
     * Fifteen semitone steps.
     */
    public static final ChromaticInterval MINOR_TENTH = new ChromaticInterval(15, 9);

    /**
     * Sixteen semitone steps.
     */
    public static final ChromaticInterval MAJOR_TENTH = new ChromaticInterval(16, 9);

    /**
     * Seventeen semitone steps.
     */
    public static final ChromaticInterval PERFECT_ELEVENTH = new ChromaticInterval(17, 10);

    /**
     * Eighteen semitone steps.
     */
    public static final ChromaticInterval DIMINISHED_TWELVE = new ChromaticInterval(18, 11);

    /**
     * Nineteen semitone steps.
     */
    public static final ChromaticInterval PERFECT_TWELVE = new ChromaticInterval(19, 11);

    /**
     * Twenty semitone steps.
     */
    public static final ChromaticInterval MINOR_THIRTEENTH = new ChromaticInterval(20, 12);

    /**
     * Twenty-one semitone steps.
     */
    public static final ChromaticInterval MAJOR_THIRTEENTH = new ChromaticInterval(21, 12);

    private static final ChromaticInterval[] INTERVALS = new ChromaticInterval[]{
            PERFECT_UNISON, MAJOR_SECOND, MINOR_THIRD, MAJOR_THIRD, PERFECT_FOURTH, DIMINISHED_FIFTH, PERFECT_FIFTH,
            MINOR_SIXT, MAJOR_SIXT, MINOR_SEVENTH, MAJOR_SEVENTH, PERFECT_OCTAVE, MINOR_NINTH, MAJOR_NINTH, MINOR_NINTH,
            MAJOR_TENTH, PERFECT_ELEVENTH, DIMINISHED_TWELVE, PERFECT_TWELVE, MINOR_THIRTEENTH, MAJOR_THIRTEENTH
    };

    private final int chromaticSteps;

    private final int diatonicSteps;

    private ChromaticInterval(int chromatic, int diatonic) {

        this.chromaticSteps = chromatic;
        this.diatonicSteps = diatonic;
    }

    /**
     * @return the number of semitone steps.
     * @see #getChromaticSteps(TonalSystem)
     */
    public int getChromaticSteps() {

        return this.chromaticSteps;
    }

    /**
     * @return the number of diatonic steps.
     * @see #getDiatonicSteps(TonalSystem)
     */
    public int getDiatonicSteps() {

        return this.diatonicSteps;
    }

    @Override
    public int getChromaticSteps(TonalSystem system) {

        return this.chromaticSteps;
    }

    @Override
    public int getDiatonicSteps(TonalSystem system) {

        return this.diatonicSteps;
    }

    /**
     * @param chromaticSteps the number of {@link #getChromaticSteps() chromatic steps}.
     * @return the corresponding {@link ChromaticInterval} or <code>null</code> if no such {@link ChromaticInterval}
     * exists (given value is negative or too high).
     */
    public static ChromaticInterval of(int chromaticSteps) {

        int diatonicSteps;
        if (chromaticSteps >= 0) {
            if (chromaticSteps < 22) {
                return INTERVALS[chromaticSteps];
            }
            int modulo = chromaticSteps % 12;
            ChromaticInterval mod = INTERVALS[modulo];
            int octaves = (chromaticSteps / 12);
            diatonicSteps = (octaves * 7) + mod.diatonicSteps;
        } else {
            int negation = -chromaticSteps;
            int modulo = negation % 12;
            ChromaticInterval mod = INTERVALS[modulo];
            int octaves = (negation / 12);
            diatonicSteps = -((octaves * 7) + mod.diatonicSteps);
        }
        return new ChromaticInterval(chromaticSteps, diatonicSteps);
    }

}
