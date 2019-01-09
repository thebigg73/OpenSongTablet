/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.tone;

import com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey;
import com.garethevans.church.opensongtablet.core.music.stave.Clef;
import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

import java.util.Locale;

/**
 * A {@link Tone} is an absolute tone as the combination of a {@link TonePitch} with an {@link #getOctave() octave}.
 * While a {@link TonePitch} only identifies a tone within an octave a {@link Tone} can identify every
 * absolute tone from any octave. For string representation the Helmholtz pitch notation is used.
 *
 * @author hohwille
 */
public class Tone extends AbstractTransposable<Tone> implements Comparable<Tone> {

    public static final char OCTAVE_UP = '\'';

    public static final char OCTAVE_DOWN = ',';

    public static final Tone C0 = new Tone(TonePitchEnglish.C, 0);

    public static final Tone CS0 = new Tone(TonePitchEnglish.C_SHARP, 0);

    public static final Tone D0 = new Tone(TonePitchEnglish.D, 0);

    public static final Tone DS0 = new Tone(TonePitchEnglish.D_SHARP, 0);

    public static final Tone E0 = new Tone(TonePitchEnglish.E, 0);

    public static final Tone F0 = new Tone(TonePitchEnglish.F, 0);

    public static final Tone FS0 = new Tone(TonePitchEnglish.F_SHARP, 0);

    public static final Tone G0 = new Tone(TonePitchEnglish.G, 0);

    public static final Tone GS0 = new Tone(TonePitchEnglish.G_SHARP, 0);

    public static final Tone A0 = new Tone(TonePitchEnglish.A, 0);

    public static final Tone BF0 = new Tone(TonePitchEnglish.B_FLAT, 0);

    public static final Tone B0 = new Tone(TonePitchEnglish.B, 0);

    public static final Tone C1 = new Tone(TonePitchEnglish.C, 1);

    public static final Tone CS1 = new Tone(TonePitchEnglish.C_SHARP, 1);

    public static final Tone D1 = new Tone(TonePitchEnglish.D, 1);

    public static final Tone DS1 = new Tone(TonePitchEnglish.D_SHARP, 1);

    public static final Tone E1 = new Tone(TonePitchEnglish.E, 1);

    public static final Tone F1 = new Tone(TonePitchEnglish.F, 1);

    public static final Tone FS1 = new Tone(TonePitchEnglish.F_SHARP, 1);

    public static final Tone G1 = new Tone(TonePitchEnglish.G, 1);

    public static final Tone GS1 = new Tone(TonePitchEnglish.G_SHARP, 1);

    public static final Tone A1 = new Tone(TonePitchEnglish.A, 1);

    public static final Tone BF1 = new Tone(TonePitchEnglish.B_FLAT, 1);

    public static final Tone B1 = new Tone(TonePitchEnglish.B, 1);

    public static final Tone C2 = new Tone(TonePitchEnglish.C, 2);

    public static final Tone CS2 = new Tone(TonePitchEnglish.C_SHARP, 2);

    public static final Tone D2 = new Tone(TonePitchEnglish.D, 2);

    public static final Tone DS2 = new Tone(TonePitchEnglish.D_SHARP, 2);

    public static final Tone E2 = new Tone(TonePitchEnglish.E, 2);

    public static final Tone F2 = new Tone(TonePitchEnglish.F, 2);

    public static final Tone FS2 = new Tone(TonePitchEnglish.F_SHARP, 2);

    public static final Tone G2 = new Tone(TonePitchEnglish.G, 2);

    public static final Tone GS2 = new Tone(TonePitchEnglish.G_SHARP, 2);

    public static final Tone A2 = new Tone(TonePitchEnglish.A, 2);

    public static final Tone BF2 = new Tone(TonePitchEnglish.B_FLAT, 2);

    public static final Tone B2 = new Tone(TonePitchEnglish.B, 2);

    public static final Tone C3 = new Tone(TonePitchEnglish.C, 3);

    public static final Tone CS3 = new Tone(TonePitchEnglish.C_SHARP, 3);

    public static final Tone D3 = new Tone(TonePitchEnglish.D, 3);

    public static final Tone DS3 = new Tone(TonePitchEnglish.D_SHARP, 3);

    public static final Tone E3 = new Tone(TonePitchEnglish.E, 3);

    public static final Tone F3 = new Tone(TonePitchEnglish.F, 3);

    public static final Tone FS3 = new Tone(TonePitchEnglish.F_SHARP, 3);

    public static final Tone G3 = new Tone(TonePitchEnglish.G, 3);

    public static final Tone GS3 = new Tone(TonePitchEnglish.G_SHARP, 3);

    public static final Tone A3 = new Tone(TonePitchEnglish.A, 3);

    public static final Tone BF3 = new Tone(TonePitchEnglish.B_FLAT, 3);

    public static final Tone B3 = new Tone(TonePitchEnglish.B, 3);

    public static final Tone C4 = new Tone(TonePitchEnglish.C, 4);

    public static final Tone CS4 = new Tone(TonePitchEnglish.C_SHARP, 4);

    public static final Tone D4 = new Tone(TonePitchEnglish.D, 4);

    public static final Tone DS4 = new Tone(TonePitchEnglish.D_SHARP, 4);

    public static final Tone E4 = new Tone(TonePitchEnglish.E, 4);

    public static final Tone F4 = new Tone(TonePitchEnglish.F, 4);

    public static final Tone FS4 = new Tone(TonePitchEnglish.F_SHARP, 4);

    public static final Tone G4 = new Tone(TonePitchEnglish.G, 4);

    public static final Tone GS4 = new Tone(TonePitchEnglish.G_SHARP, 4);

    public static final Tone A4 = new Tone(TonePitchEnglish.A, 4);

    public static final Tone BF4 = new Tone(TonePitchEnglish.B_FLAT, 4);

    public static final Tone B4 = new Tone(TonePitchEnglish.B, 4);

    public static final Tone C5 = new Tone(TonePitchEnglish.C, 5);

    public static final Tone CS5 = new Tone(TonePitchEnglish.C_SHARP, 5);

    public static final Tone D5 = new Tone(TonePitchEnglish.D, 5);

    public static final Tone DS5 = new Tone(TonePitchEnglish.D_SHARP, 5);

    public static final Tone E5 = new Tone(TonePitchEnglish.E, 5);

    public static final Tone F5 = new Tone(TonePitchEnglish.F, 5);

    public static final Tone FS5 = new Tone(TonePitchEnglish.F_SHARP, 5);

    public static final Tone G5 = new Tone(TonePitchEnglish.G, 5);

    public static final Tone GS5 = new Tone(TonePitchEnglish.G_SHARP, 5);

    public static final Tone A5 = new Tone(TonePitchEnglish.A, 5);

    public static final Tone BF5 = new Tone(TonePitchEnglish.B_FLAT, 5);

    public static final Tone B5 = new Tone(TonePitchEnglish.B, 5);

    public static final Tone C6 = new Tone(TonePitchEnglish.C, 6);

    public static final Tone CS6 = new Tone(TonePitchEnglish.C_SHARP, 6);

    public static final Tone D6 = new Tone(TonePitchEnglish.D, 6);

    public static final Tone DS6 = new Tone(TonePitchEnglish.D_SHARP, 6);

    public static final Tone E6 = new Tone(TonePitchEnglish.E, 6);

    public static final Tone F6 = new Tone(TonePitchEnglish.F, 6);

    public static final Tone FS6 = new Tone(TonePitchEnglish.F_SHARP, 6);

    public static final Tone G6 = new Tone(TonePitchEnglish.G, 6);

    public static final Tone GS6 = new Tone(TonePitchEnglish.G_SHARP, 6);

    public static final Tone A6 = new Tone(TonePitchEnglish.A, 6);

    public static final Tone BF6 = new Tone(TonePitchEnglish.B_FLAT, 6);

    public static final Tone B6 = new Tone(TonePitchEnglish.B, 6);

    public static final Tone C7 = new Tone(TonePitchEnglish.C, 7);

    public static final Tone CS7 = new Tone(TonePitchEnglish.C_SHARP, 7);

    public static final Tone D7 = new Tone(TonePitchEnglish.D, 7);

    public static final Tone DS7 = new Tone(TonePitchEnglish.D_SHARP, 7);

    public static final Tone E7 = new Tone(TonePitchEnglish.E, 7);

    public static final Tone F7 = new Tone(TonePitchEnglish.F, 7);

    public static final Tone FS7 = new Tone(TonePitchEnglish.F_SHARP, 7);

    public static final Tone G7 = new Tone(TonePitchEnglish.G, 7);

    public static final Tone GS7 = new Tone(TonePitchEnglish.G_SHARP, 7);

    public static final Tone A7 = new Tone(TonePitchEnglish.A, 7);

    public static final Tone BF7 = new Tone(TonePitchEnglish.B_FLAT, 7);

    public static final Tone B7 = new Tone(TonePitchEnglish.B, 7);

    public static final Tone C8 = new Tone(TonePitchEnglish.C, 8);

    public static final Tone CS8 = new Tone(TonePitchEnglish.C_SHARP, 8);

    public static final Tone D8 = new Tone(TonePitchEnglish.D, 8);

    public static final Tone DS8 = new Tone(TonePitchEnglish.D_SHARP, 8);

    public static final Tone E8 = new Tone(TonePitchEnglish.E, 8);

    public static final Tone F8 = new Tone(TonePitchEnglish.F, 8);

    public static final Tone FS8 = new Tone(TonePitchEnglish.F_SHARP, 8);

    public static final Tone G8 = new Tone(TonePitchEnglish.G, 8);

    public static final Tone GS8 = new Tone(TonePitchEnglish.G_SHARP, 8);

    public static final Tone A8 = new Tone(TonePitchEnglish.A, 8);

    public static final Tone BF8 = new Tone(TonePitchEnglish.B_FLAT, 8);

    public static final Tone B8 = new Tone(TonePitchEnglish.B, 8);

    private static final Tone[] TONES = new Tone[]{
            C0, CS0, D0, DS0, E0, F0, FS0, G0, GS0, A0, BF0, B0,
            C1, CS1, D1, DS1, E1, F1, FS1, G1, GS1, A1, BF1, B1,
            C2, CS2, D2, DS2, E2, F2, FS2, G2, GS2, A2, BF2, B2,
            C3, CS3, D3, DS3, E3, F3, FS3, G3, GS3, A3, BF3, B3,
            C4, CS4, D4, DS4, E4, F4, FS4, G4, GS4, A4, BF4, B4,
            C5, CS5, D5, DS5, E5, F5, FS5, G5, GS5, A5, BF5, B5,
            C6, CS6, D6, DS6, E6, F6, FS6, G6, GS6, A6, BF6, B6,
            C7, CS7, D7, DS7, E7, F7, FS7, G7, GS7, A7, BF7, B7,
            C8, CS8, D8, DS8, E8, F8, FS8, G8, GS8, A8, BF8, B8};

    private final TonePitch pitch;

    private final int octave;

    private final String name;

    /**
     * The constructor.
     *
     * @param pitch  - see {@link #getPitch()}.
     * @param octave - see {@link #getOctave()}.
     */
    private Tone(TonePitch pitch, int octave) {
        super();
        this.pitch = pitch;
        this.octave = octave;
        this.name = getName(TonePitchEnglish.STYLE);
    }

    /**
     * @return the {@link TonePitch} within the {@link #getOctave() octave}.
     */
    public TonePitch getPitch() {
        return this.pitch;
    }

    /**
     * @return the octave the {@link #getPitch() pitch} is located. A value of {@code 0} is the lowest
     * octave on a piano (starting with {@link #A0}). The regular octave in {@link Clef#G violin-clef}
     * is {@code 2} starting with the {@link #C2 low C} (below the scale) and ending with
     * {@link #B2 B♯2} in the middle of the scale. A higher value is used to go up an octave to
     * higher pitches and a lower value is used to go down (e.g. {@code 1} and {@code 0} for
     * {@link Clef#F bass-clef}).
     */
    public int getOctave() {
        return this.octave;
    }

    /**
     * @return the number of semitone steps upwards from {@code C0}.
     * @see TonePitch#getStep()
     */
    public int getStep() {
        return (this.octave * 12) + this.pitch.getStep().get();
    }

    @Override
    public int compareTo(Tone tone) {
        return getStep() - tone.getStep();
    }

    /**
     * @param other the {@link Tone} to compare with.
     * @return {@code true} if this {@link Tone} is lower than the other given {@link Tone}.
     */
    public boolean isLower(Tone other) {
        if (this.octave < other.octave) {
            return true;
        } else if (this.octave == other.octave) {
            return this.pitch.getStep().get() < other.pitch.getStep().get();
        } else {
            return false;
        }
    }

    /**
     * @param other the {@link Tone} to compare with.
     * @return {@code true} if this {@link Tone} is lower than or equal to the other given {@link Tone}.
     */
    public boolean isLowerOrEqual(Tone other) {
        if (this.octave < other.octave) {
            return true;
        } else if (this.octave == other.octave) {
            return this.pitch.getStep().get() <= other.pitch.getStep().get();
        } else {
            return false;
        }
    }

    /**
     * @param other the {@link Tone} to compare with.
     * @return {@code true} if this {@link Tone} is higher than the other given {@link Tone}.
     */
    public boolean isHigher(Tone other) {
        if (this.octave > other.octave) {
            return true;
        } else if (this.octave == other.octave) {
            return this.pitch.getStep().get() > other.pitch.getStep().get();
        } else {
            return false;
        }
    }

    /**
     * @param other the {@link Tone} to compare with.
     * @return {@code true} if this {@link Tone} is higher than or equal to the other given {@link Tone}.
     */
    public boolean isHigherOrEqual(Tone other) {
        if (this.octave > other.octave) {
            return true;
        } else if (this.octave == other.octave) {
            return this.pitch.getStep().get() >= other.pitch.getStep().get();
        } else {
            return false;
        }
    }

    @Override
    public Tone transpose(int steps, boolean diatonic, TransposeContext context) {
        TonePitch transposedPitch = this.pitch.transpose(steps, diatonic, context);
        MusicalKey key = context.getKey();
        if (diatonic) {
            if (key == null) {
                int chromaticSteps = getChromaticSteps(this.pitch, transposedPitch, steps);
                return transposeOctaveChromatic(transposedPitch, chromaticSteps);
            } else {
                // TODO this is so deadly wrong
                int targetStep = (this.pitch.getStep().get() + steps - key.getTonika().getStep().get()) % 8;
                int octaveStep = targetStep / 8;
                int resultOctave = this.octave + octaveStep;
                return new Tone(transposedPitch, resultOctave);
            }
        } else {
            return transposeOctaveChromatic(transposedPitch, steps);
        }
    }

    private Tone transposeOctaveChromatic(TonePitch resultPitch, int chromaticSteps) {
        int pitchSteps = resultPitch.getStep().get() - this.pitch.getStep().get();
        int octaveSteps;
        if (chromaticSteps < 0) {
            octaveSteps = chromaticSteps / 12;
            if (pitchSteps > 0) {
                // extra octave step when stepping below C
                octaveSteps--;
            }
        } else {
            octaveSteps = chromaticSteps / 12;
            if (pitchSteps < 0) {
                // extra octave step when stepping above B/H
                octaveSteps++;
            }
        }
        int resultOctave = this.octave + octaveSteps;
        return new Tone(resultPitch, resultOctave);
    }

    public String getName(ToneNameStyle style) {
        String result = this.pitch.with(style).getName();
        if (this.octave > 2) {
            result = result.toLowerCase(Locale.US);
            for (int i = this.octave; i > 3; i--) {
                result = result + OCTAVE_UP;
            }
        } else {
            for (int i = this.octave; i < 2; i++) {
                result = result + OCTAVE_DOWN;
            }
        }
        return result;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.octave;
        result = prime * result + ((this.pitch == null) ? 0 : this.pitch.hashCode());
        return result;
    }

    public boolean isEqualTo(Tone other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        }
        if (this.octave != other.octave) {
            return false;
        }
        if (!this.pitch.isEqualTo(other.pitch)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tone other = (Tone) obj;
        if (this.octave != other.octave) {
            return false;
        }
        if (this.pitch != other.pitch) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static Tone of(TonePitch pitch, int octave) {
        if ((octave >= 0) && (octave <= 8) && pitch.isNormal()) {
            int step = (octave * 12) + pitch.getStep().get();
            return TONES[step];
        }
        return new Tone(pitch, octave);
    }

}
