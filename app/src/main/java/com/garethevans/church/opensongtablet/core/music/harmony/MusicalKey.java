/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.music.tone.ToneNameCase;
import com.garethevans.church.opensongtablet.core.music.tone.ToneNameStyle;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;
import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This enum represents a musical key. It represents a {@link #getDiatonicScale() diatonic scale} based on a fundamental
 * tone (called {@link #getTonika() tonika}) and a {@link #getTonalSystem() tonal system}.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0
 */
public final class MusicalKey extends AbstractTransposable<MusicalKey> {

    private static final Map<String, MusicalKey> NAME2KEY_MAP = new HashMap<>();

    private static final Collection<MusicalKey> VALUES = Collections.unmodifiableCollection(NAME2KEY_MAP.values());

    /**
     * The {@link MusicalKey} Cb-{@link TonalSystem#MAJOR major} (with 7 &#9837; signs).
     */
    public static final MusicalKey C_FLAT_MAJOR = create(TonePitchEnglish.C_FLAT, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} ab-{@link TonalSystem#MINOR minor} (with 7 &#9837; signs).
     */
    public static final MusicalKey A_FLAT_MINOR = create(TonePitchEnglish.A_FLAT, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} Gb-{@link TonalSystem#MAJOR major} (with 6 &#9837; signs).
     */
    public static final MusicalKey G_FLAT_MAJOR = create(TonePitchEnglish.G_FLAT, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} eb-{@link TonalSystem#MINOR minor} (with 6 &#9837; signs).
     */
    public static final MusicalKey E_FLAT_MINOR = create(TonePitchEnglish.E_FLAT, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} Db-{@link TonalSystem#MAJOR major} (with 5 &#9837; signs).
     */
    public static final MusicalKey D_FLAT_MAJOR = create(TonePitchEnglish.D_FLAT, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} bb-{@link TonalSystem#MINOR minor} (with 5 &#9837; signs).
     */
    public static final MusicalKey B_FLAT_MINOR = create(TonePitchEnglish.B_FLAT, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} Ab-{@link TonalSystem#MAJOR major} (with 4 &#9837; signs).
     */
    public static final MusicalKey A_FLAT_MAJOR = create(TonePitchEnglish.A_FLAT, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} f-{@link TonalSystem#MINOR minor} (with 4 &#9837; signs).
     */
    public static final MusicalKey F_MINOR = create(TonePitchEnglish.F, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} Eb-{@link TonalSystem#MAJOR major} (with 3 &#9837; signs).
     */
    public static final MusicalKey E_FLAT_MAJOR = create(TonePitchEnglish.E_FLAT, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} c-{@link TonalSystem#MINOR minor} (with 3 &#9837; signs).
     */
    public static final MusicalKey C_MINOR = create(TonePitchEnglish.C, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} Bb-{@link TonalSystem#MAJOR major} (with 2 &#9837; signs).
     */
    public static final MusicalKey B_FLAT_MAJOR = create(TonePitchEnglish.B_FLAT, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} g-{@link TonalSystem#MINOR minor} (with 2 &#9837; signs).
     */
    public static final MusicalKey G_MINOR = create(TonePitchEnglish.G, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} F-{@link TonalSystem#MAJOR major} (with 1 &#9837; signs).
     */
    public static final MusicalKey F_MAJOR = create(TonePitchEnglish.F, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} d-{@link TonalSystem#MINOR minor} (with 1 &#9837; signs).
     */
    public static final MusicalKey D_MINOR = create(TonePitchEnglish.D, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} C-{@link TonalSystem#MAJOR major} (without any signs).
     */
    public static final MusicalKey C_MAJOR = create(TonePitchEnglish.C, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} a-{@link TonalSystem#MINOR minor} (without any signs).
     */
    public static final MusicalKey A_MINOR = create(TonePitchEnglish.A, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} G-{@link TonalSystem#MAJOR major} (with 1 &#9839; signs).
     */
    public static final MusicalKey G_MAJOR = create(TonePitchEnglish.G, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} e-{@link TonalSystem#MINOR minor} (with 1 &#9839; signs).
     */
    public static final MusicalKey E_MINOR = create(TonePitchEnglish.E, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} D-{@link TonalSystem#MAJOR major} (with 2 &#9839; signs).
     */
    public static final MusicalKey D_MAJOR = create(TonePitchEnglish.D, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} b-{@link TonalSystem#MINOR minor} (with 2 &#9839; signs).
     */
    public static final MusicalKey B_MINOR = create(TonePitchEnglish.B, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} A-{@link TonalSystem#MAJOR major} (with 3 &#9839; signs).
     */
    public static final MusicalKey A_MAJOR = create(TonePitchEnglish.A, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} f#-{@link TonalSystem#MINOR minor} (with 3 &#9839; signs).
     */
    public static final MusicalKey F_SHARP_MINOR = create(TonePitchEnglish.F_SHARP, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} E-{@link TonalSystem#MAJOR major} (with 4 &#9839; signs).
     */
    public static final MusicalKey E_MAJOR = create(TonePitchEnglish.E, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} c#-{@link TonalSystem#MINOR minor} (with 4 &#9839; signs).
     */
    public static final MusicalKey C_SHARP_MINOR = create(TonePitchEnglish.C_SHARP, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} B-{@link TonalSystem#MAJOR major} (with 5 &#9839; signs).
     */
    public static final MusicalKey B_MAJOR = create(TonePitchEnglish.B, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} g#-{@link TonalSystem#MINOR minor} (with 5 &#9839; signs).
     */
    public static final MusicalKey G_SHARP_MINOR = create(TonePitchEnglish.G_SHARP, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} F#-{@link TonalSystem#MAJOR major} (with 6 &#9839; signs).
     */
    public static final MusicalKey F_SHARP_MAJOR = create(TonePitchEnglish.F_SHARP, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} d#-{@link TonalSystem#MINOR minor} (with 6 &#9839; signs).
     */
    public static final MusicalKey D_SHARP_MINOR = create(TonePitchEnglish.D_SHARP, TonalSystem.MINOR);

    /**
     * The {@link MusicalKey} C#-{@link TonalSystem#MAJOR major} (with 7 &#9839; signs). The enharmonic identical
     * {@link MusicalKey key} is {@link #D_FLAT_MAJOR}.
     */
    public static final MusicalKey C_SHARP_MAJOR = create(TonePitchEnglish.C_SHARP, TonalSystem.MAJOR);

    /**
     * The {@link MusicalKey} A#-{@link TonalSystem#MAJOR major} (with 7 &#9839; signs). The enharmonic identical
     * {@link MusicalKey key} is {@link #B_FLAT_MINOR}.
     */
    public static final MusicalKey A_SHARP_MINOR = create(TonePitchEnglish.A_SHARP, TonalSystem.MINOR);

    private final TonePitch tonika;

    private final TonalSystem tonalSystem;

    private final String name;

    private final EnharmonicStyle enharmonicStyle;

    private final List<TonePitch> tonesDiatonic;

    private final List<TonePitch> tonesChromatic;

    private final List<TonePitch> chromaticSignTones;

    /**
     * The constructor.
     *
     * @param tonika      - see {@link #getTonika()}.
     * @param tonalSystem - see {@link #getTonalSystem()}.
     * @param name        - see {@link #getName()}.
     */
    private MusicalKey(TonePitch tonika, TonalSystem tonalSystem, String name) {

        this.tonika = tonika;
        this.tonalSystem = tonalSystem;
        this.name = name;

        // determine chromaticSignTones
        EnharmonicStyle style;
        List<TonePitch> chromaticSigns = new ArrayList<>(7);
        int enharmonicSigns = tonalSystem.getEnharmonicSigns(tonika);
        ToneNameStyle nameStyle = tonika.getNameStyle();
        if (enharmonicSigns > 0) {
            style = EnharmonicStyle.SHARP;
            for (int i = 1; i <= enharmonicSigns; i++) {
                chromaticSigns.add(nameStyle.pitchByKeySign(i));
            }
        } else if (enharmonicSigns < 0) {
            style = EnharmonicStyle.FLAT;
            for (int i = -1; i >= enharmonicSigns; i--) {
                chromaticSigns.add(nameStyle.pitchByKeySign(i));
            }
        } else {
            style = EnharmonicStyle.NORMAL;
        }
        this.enharmonicStyle = style;
        this.chromaticSignTones = Collections.unmodifiableList(chromaticSigns);

        // determine tonesDiatoic and tonesChromatic
        List<TonePitch> diatonic = new ArrayList<>(7);
        List<TonePitch> chromatic = new ArrayList<>(12);
        TonePitch pitch = tonika;
        boolean addDiatonic = true;
        int firstSemitone = tonalSystem.getFirstSemitone();
        int secondSemitone = tonalSystem.getSecondSemitone();
        for (int i = 0; i < 12; i++) {
            if (addDiatonic) {
                diatonic.add(pitch);
            }
            chromatic.add(pitch);
            TonePitch nextPitch = pitch.transposeChromatic(1, style, false);
            pitch = getEnharmonicChange(nextPitch);
            int size = diatonic.size() - 1;
            if (addDiatonic) {
                if ((size != firstSemitone) && (size != secondSemitone)) {
                    addDiatonic = false;
                }
            } else {
                addDiatonic = true;
            }
        }
        assert (chromatic.size() == 12);
        this.tonesChromatic = Collections.unmodifiableList(chromatic);
        assert (diatonic.size() == 7);
        this.tonesDiatonic = Collections.unmodifiableList(diatonic);
        NAME2KEY_MAP.put(name, this);
    }

    private TonePitch getEnharmonicChange(TonePitch pitch) {
        for (TonePitch signPitch : this.chromaticSignTones) {
            if (signPitch.getStep() == pitch.getStep()) {
                return signPitch;
            }
        }
        return pitch;
    }

    /**
     * @return the textual representation in short form (Capitalized for {@link TonalSystem#MAJOR major} and lower case
     * for {@link TonalSystem#MINOR minor}).
     */
    public String getName() {

        return this.name;
    }

    /**
     * This method gets the {@link TonalSystem} of this key.
     *
     * @return the {@link TonalSystem}.
     */
    public TonalSystem getTonalSystem() {

        return this.tonalSystem;
    }

    /**
     * This method gets the {@link TonePitch} of the tonika (base-tone) for this key.
     *
     * @return the {@link TonePitch}.
     */
    public TonePitch getTonika() {

        return this.tonika;
    }

    /**
     * @return the {@link List} with the seven {@link TonePitch}es of the {@link TonalSystem} for this {@link MusicalKey}
     * starting from {@link #getTonika() tonika}.
     */
    public List<TonePitch> getDiatonicScale() {

        return this.tonesDiatonic;
    }

    /**
     * @return the {@link List} with twelve {@link TonePitch}es of the chromatic scale for this {@link MusicalKey}
     * starting from {@link #getTonika() tonika}.
     */
    public List<TonePitch> getChromaticScale() {

        return this.tonesChromatic;
    }

    /**
     * @return the {@link List} with the {@link TonePitch tones} for the chromatic signs of this {@link MusicalKey}. Will
     * be empty for {@link #C_MAJOR} or {@link #A_MINOR}. Will contain only {@link TonePitch#isSharp() sharp} or
     * only {@link TonePitch#isFlat() flat} {@link TonePitch tones}. E.g. for {@link #D_MAJOR} it will contain
     * {@link TonePitchEnglish#F_SHARP} and {@link TonePitchEnglish#C_SHARP} and for {@link #C_MINOR} it will contain
     * {@link TonePitchEnglish#B_FLAT}, {@link TonePitchEnglish#E_FLAT}, {@link TonePitchEnglish#A_FLAT}.
     */
    public List<TonePitch> getChromaticSignTones() {

        return this.chromaticSignTones;
    }

    /**
     * @return the {@link EnharmonicStyle}. {@link EnharmonicStyle#NORMAL} for {@link #C_MAJOR} and {@link #A_MINOR}.
     * {@link EnharmonicStyle#SHARP} if the {@link #getChromaticSignTones() chromatic sign tones} are
     * {@link TonePitch#isSharp() sharp}. {@link EnharmonicStyle#FLAT} if the {@link #getChromaticSignTones()
     * chromatic sign tones} are {@link TonePitch#isFlat() flat}.
     */
    public EnharmonicStyle getEnharmonicStyle() {

        return this.enharmonicStyle;
    }


    @Override
    public MusicalKey transpose(int steps, boolean diatonic, TransposeContext context) {
        if (context.isKeepKey()) {
            return this;
        }
        if (diatonic) {
            return transposeDiatonic(steps);
        } else {
            return transposeChromatic(steps);
        }
    }

    @Override
    public MusicalKey transposeChromatic(int semitoneSteps) {

        TonePitch newTonika = this.tonika.transposeChromatic(semitoneSteps, this.enharmonicStyle);
        return of(newTonika, this.tonalSystem);
    }

    @Override
    public MusicalKey transposeDiatonic(int steps) {
        TonePitch newTonika = this.tonika.transposeDiatonic(steps, this);
        return of(newTonika, this.tonalSystem);
    }
    @Override
    public MusicalKey transpose(Interval interval, TransposeContext context) {
        Integer steps = interval.getChromaticSteps(getTonalSystem());
        if (steps != null) {
            return transposeChromatic(steps.intValue());
        } else {
            steps = interval.getDiatonicSteps(getTonalSystem());
            if (steps == null) {
                throw new IllegalStateException("Can not transpose by " + interval);
            }
            return transposeDiatonic(steps.intValue());
        }
    }

    /**
     * Returns the tone for the given {@link Interval}.
     *
     * @param interval is the {@link Interval} such as e.g. {@link Solmization#MI}, {@link ChromaticInterval#MAJOR_THIRD},
     *                 or {@link DiatonicInterval#THIRD}.
     * @return the requested {@link TonePitch} of this {@link MusicalKey}.
     */
    public TonePitch getTone(Interval interval) {

        Integer chromaticSteps = interval.getChromaticSteps(this.tonalSystem);
        if (chromaticSteps != null) {
            int index = chromaticSteps.intValue() % 12;
            if (index < 0) {
                index = index + 12;
            }
            return this.tonesChromatic.get(index);
        }
        Integer diatonicSteps = interval.getDiatonicSteps(this.tonalSystem);
        if (diatonicSteps != null) {
            int index = diatonicSteps.intValue() % 8;
            if (index < 0) {
                index = index + 8;
            }
            return this.tonesDiatonic.get(index);
        }
        throw new IllegalArgumentException(interval.toString());
    }

    @Override
    public String toString() {

        return getName() + "-" + this.tonalSystem.toString();
    }

    private static MusicalKey create(TonePitch tonika, TonalSystem tonalSystem) {
        String name;
        if (tonalSystem.isMinor()) {
            name = tonika.with(ToneNameCase.LOWER_CASE).getName();
        } else {
            name = tonika.getName();
        }
        return new MusicalKey(tonika, tonalSystem, name);
    }

    /**
     * This method gets the {@link MusicalKey} for the given <code>value</code>.
     *
     * @param name is the {@link #getName() name} of the requested {@link MusicalKey}.
     * @return the requested {@link MusicalKey} or {@code null} if no such {@link MusicalKey} exists.
     */
    public static MusicalKey fromName(String name) {
        return NAME2KEY_MAP.get(name);
    }

    /**
     * @param tonika      the {@link #getTonika() tonika}.
     * @param tonalSystem the {@link #getTonalSystem() tonal system}.
     * @return the {@link MusicalKey} for the given parameters.
     */
    public static MusicalKey of(TonePitch tonika, TonalSystem tonalSystem) {
        for (MusicalKey key : NAME2KEY_MAP.values()) {
            if (key.tonalSystem == tonalSystem) {
                if (key.tonika == tonika) {
                    return key;
                }
            }
        }
        // throw new IllegalStateException();
        return null;
    }

    public static Collection<MusicalKey> values() {
        return VALUES;
    }
}
