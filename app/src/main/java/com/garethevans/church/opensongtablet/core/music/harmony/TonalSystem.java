/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.music.tone.ChromaticStep;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link TonalSystem} is a scale composed of seven distinct {@link TonePitch}es.
 * The {@link MusicalKey#getDiatonicScale() diatonic scale} includes five whole steps and two half steps for each octave,
 * in which the two half steps are separated from each other by either two or three whole steps,
 * depending on their position in the scale.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0
 */
public class TonalSystem {

    private static final Map<String, TonalSystem> NAME2SYSTEM_MAP = new HashMap<>();

    // DORIAN("dor", "dorian"),
    //
    // HYPODORIAN("hdor", "hypodorian"),
    //
    // PHRYGIAN("phr", "phrygian"),
    //
    // HYPOPHRYGIAN("hphr", "hypophrygian"),
    //
    // LYDIAN("lyd", "lydian"),
    //
    // HYPOLYDIAN("hlyd", "hypolydian"),
    //
    // MIXOLYDIAN("mix", "mixolydian"),
    //
    // HYPOMIXOLYDIAN("hmix", "hypomixolydian"),

    /**
     * {@link TonalSystem} of a major {@link MusicalKey key}. Its scale sequence has semitone intervals from the 3. to the
     * 4. tone as well as from the 7. to the 8. tone (1-1-½-1-1-1-½).
     */
    public static final TonalSystem MAJOR = create(Arrays.asList(TonePitchEnglish.C_FLAT, TonePitchEnglish.G_FLAT, TonePitchEnglish.D_FLAT, TonePitchEnglish.A_FLAT, TonePitchEnglish.E_FLAT, TonePitchEnglish.B_FLAT, TonePitchEnglish.F, TonePitchEnglish.C, TonePitchEnglish.G, TonePitchEnglish.D, TonePitchEnglish.A, TonePitchEnglish.E, TonePitchEnglish.B, TonePitchEnglish.F_SHARP, TonePitchEnglish.C_SHARP),
            2, 6, "major", "maj", "mj");

    /**
     * {@link TonalSystem} {@link #MAJOR} with {@link String#isEmpty() empty} {@link #getName() name}.
     */
    public static final TonalSystem MAJOR_EMPTY = MAJOR.empty;

    /**
     * {@link TonalSystem} of a minor {@link MusicalKey key}. Its scale sequence has semitone intervals from the 2. to the
     * 3. tone as well as from the 5. to the 6. tone (1-½-1-1-½-1-1).
     */
    public static final TonalSystem MINOR = create(Arrays.asList(TonePitchEnglish.A_FLAT, TonePitchEnglish.E_FLAT, TonePitchEnglish.B_FLAT, TonePitchEnglish.F, TonePitchEnglish.C, TonePitchEnglish.G, TonePitchEnglish.D, TonePitchEnglish.A, TonePitchEnglish.E, TonePitchEnglish.B, TonePitchEnglish.F_SHARP, TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.D_SHARP, TonePitchEnglish.A_SHARP),
            1, 4, "minor", "min", "m");

    /**
     * {@link TonalSystem} {@link #MINOR} with the short {@link #getName() name} "m".
     */
    public static final TonalSystem MINOR_M = NAME2SYSTEM_MAP.get("m");

    /**
     * {@link TonalSystem} {@link #MINOR} with {@link String#isEmpty() empty} {@link #getName() name}.
     */
    public static final TonalSystem MINOR_EMPTY = MINOR.empty;

    private final String name;

    private final int firstSemitone;

    private final int secondSemitone;

    private final TonalSystem reference;

    private final TonalSystem empty;

    private final List<TonePitchEnglish> signPitches;

    /**
     * The constructor.
     *
     * @param name      - see {@link #getName()}.
     * @param reference - see {@link #getReference()}.
     */
    private TonalSystem(String name, TonalSystem reference) {
        this(name, reference.firstSemitone, reference.secondSemitone, reference.signPitches, reference);
    }

    /**
     * The constructor.
     *
     * @param name        - see {@link #getName()}.
     * @param reference   - see {@link #getReference()}.
     * @param signPitches - see {@link #getSignPitches()}.
     */
    private TonalSystem(String name, int first, int second, List<TonePitchEnglish> signPitches, TonalSystem reference) {
        this.name = name;
        if (reference == null) {
            this.reference = this;
            this.empty = new TonalSystem("", first, second, signPitches, this);
        } else {
            this.reference = reference;
            if (reference.empty == null) {
                assert (name.isEmpty());
                this.empty = this;
            } else {
                this.empty = reference.empty;
            }
        }
        this.firstSemitone = first;
        this.secondSemitone = second;
        this.signPitches = signPitches;
        if (!name.isEmpty()) {
            TonalSystem duplicate = NAME2SYSTEM_MAP.put(name, this);
            if (duplicate != null) {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * @return the name (short and stable {@link #toString() name}).
     */
    public String getName() {
        return this.name;
    }

    public TonalSystem getReference() {
        return this.reference;
    }

    /**
     * @return the diatonic {@link List#get(int) zero-based index} of the first tone that is ony a
     * semitone higher than its predecessor (2 for {@link #MAJOR} and 1 for {@link #MINOR}).
     */
    public int getFirstSemitone() {
        return this.firstSemitone;
    }

    /**
     * @return the diatonic {@link java.util.List#get(int) zero-based index} of the second tone that
     * is only a semitone higher than its predecessor (6 for {@link #MAJOR} and 4 for {@link #MINOR}).
     */
    public int getSecondSemitone() {
        return this.secondSemitone;
    }

    /**
     * @return the {@link List} of {@link TonePitch}es in the order of their {@link #getEnharmonicSigns(TonePitch) enharmonic signs}
     * from {@code -7} to {@code +7}.
     */
    List<TonePitchEnglish> getSignPitches() {
        return this.signPitches;
    }

    /**
     * @param tonika the {@link MusicalKey#getTonika() tonika}.
     * @return {code 0} for no enharmonic signs, if positive (1-7) the number of sharp signs, and if negative (-1 to -7)
     * the number of flat signs.
     */
    public int getEnharmonicSigns(TonePitch tonika) {

        ChromaticStep step = tonika.getStep();
        int signs = -7;
        for (TonePitch pitch : this.signPitches) {
            if (pitch.getStep() == step) {
                if ((signs < 0) && (tonika.isFlat() || (tonika.isNormal() && pitch.isNormal()))) {
                    return signs;
                } else if ((signs > 0) && !tonika.isFlat()) {
                    return signs;
                } else if ((signs == 0) && tonika.isNormal()) {
                    return 0;
                }
            }
            signs++;
        }
        throw new IllegalArgumentException("" + tonika);
    }

    /**
     * @return {@code true} if {@link #MAJOR} or one of its aliases, {@code false} otherwise.
     */
    public boolean isMajor() {
        return this.reference == MAJOR;
    }

    /**
     * @return {@code true} if {@link #MINOR} or one of its aliases, {@code false} otherwise.
     */
    public boolean isMinor() {
        return this.reference == MINOR;
    }

    public int getDiatonicSteps(int chromaticSteps, boolean strict) {
        boolean negative = (chromaticSteps < 0);
        if (negative) {
            chromaticSteps = -chromaticSteps;
        }
        int mod = chromaticSteps % 12;
        int octaves = chromaticSteps / 12;
        int diatonicSteps = 0;
        int chromatic = 0;
        while (chromatic < mod) {
            if ((diatonicSteps == this.firstSemitone) && (diatonicSteps == this.secondSemitone)) {
                chromaticSteps++;
            } else {
                chromaticSteps = chromaticSteps + 2;
            }
            diatonicSteps++;
        }
        if ((chromatic != mod) && strict) {
            return Integer.MIN_VALUE;
        }
        diatonicSteps = diatonicSteps + (octaves * 7);
        if (negative) {
            diatonicSteps = -diatonicSteps;
        }
        return diatonicSteps;
    }

    public int getChromaticSteps(int diatonicSteps) {
        boolean negative = (diatonicSteps < 0);
        if (negative) {
            diatonicSteps = -diatonicSteps;
        }
        int mod = diatonicSteps % 7;
        int octaves = diatonicSteps / 7;
        int chromaticSteps = (octaves * 12);
        for (int i = 0; i < mod; i++) {
            if ((i == this.firstSemitone) && (i == this.secondSemitone)) {
                chromaticSteps++;
            } else {
                chromaticSteps = chromaticSteps + 2;
            }
        }
        if (negative) {
            chromaticSteps = -chromaticSteps;
        }
        return chromaticSteps;
    }
    public boolean isEqualTo(TonalSystem system) {
        if (system == null) {
            return false;
        }
        return (this.reference == system.reference);
    }

    @Override
    public String toString() {
        if (this.name.isEmpty()) {
            return "<" + this.reference.name + ">";
        }
        return this.name;
    }

    private static final TonalSystem create(List<TonePitchEnglish> signPitches, int first, int second, String name, String... altNames) {
        assert (signPitches.size() == 15);
        TonalSystem reference = new TonalSystem(name, first, second, Collections.unmodifiableList(signPitches), null);
        for (String alt : altNames) {
            new TonalSystem(alt, reference);
        }
        return reference;
    }

    /**
     * @param name the {@link #getName() name} of the requested {@link TonalSystem}.
     * @return the requested {@link TonalSystem} or {@code null} if no such {@link TonalSystem} exists.
     */
    public static TonalSystem of(String name) {

        return NAME2SYSTEM_MAP.get(name);
    }

}
