package com.garethevans.church.opensongtablet.core.music.tone;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link TonePitch} with English {@link ToneNameStyle}. This style is used in most countries where
 * English is spoken (e.g. UK and US). It uses ASCII for enharmonic signs (e.g. "C#" or "Cb").
 */
public class TonePitchEnglish extends TonePitch {

    /**
     * {@link #getNameStyle() Name style} for {@link TonePitchEnglish}.
     */
    public static final ToneNameStyleEnglish STYLE = new ToneNameStyleEnglish();

    private static final Map<String, TonePitchEnglish> NAME2PITCH_MAP = new HashMap<>();

    private static final Collection<TonePitchEnglish> PITCHES = Collections.unmodifiableCollection(NAME2PITCH_MAP.values());

    private static final TonePitchEnglish[] PITCHES_NORMAL = new TonePitchEnglish[12];

    private static final TonePitchEnglish[][] PITCHES_BY_TYPE_AND_STEP = new TonePitchEnglish[5][12];

    /**
     * {@code C} is the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getTonika() tonika}
     * of the common {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#C_MAJOR} key.
     */
    public static final TonePitchEnglish C = create("C", 0);

    /**
     * {@code C#} is one semitones higher than the pitch {@link #C}.
     */
    public static final TonePitchEnglish C_SHARP = create("C#", 1);

    /**
     * {@code D} is two semitones higher than the pitch {@link #C}.
     */
    public static final TonePitchEnglish D = create("D", 2);

    /**
     * {@code D#} is three semitones higher than the pitch {@link #C}.
     */
    public static final TonePitchEnglish D_SHARP = create("D#", 3);

    /**
     * {@code E} is four semitones higher than the pitch {@link #C}.
     */
    public static final TonePitchEnglish E = create("E", 4);

    /**
     * {@code F} is five semitones higher than the pitch {@link #C}.
     */
    public static final TonePitchEnglish F = create("F", 5);

    /**
     * {@code F#} is six semitones higher than the pitch {@link #C}.
     */
    public static final TonePitchEnglish F_SHARP = create("F#", 6);

    /**
     * {@code G} is seven semitones higher than the pitch {@link #C}.
     */
    public static final TonePitchEnglish G = create("G", 7);

    /**
     * {@code G#} is eight semitones higher than the pitch {@link #C}.
     */
    public static final TonePitchEnglish G_SHARP = create("G#", 8);

    /**
     * {@code A} is nine semitones higher than the pitch {@link #C}.
     * The middle a (a<sup>1</sup>, Concert A reference) is normalized to 440Hz.
     */
    public static final TonePitchEnglish A = create("A", 9);

    /**
     * {@code Bb} is ten semitones higher than the pitch {@link #C}.
     * @see TonePitchInternational#B_FLAT
     */
    public static final TonePitchEnglish B_FLAT = create("Bb", 10);

    /**
     * {@code B} is eleven semitones higher than the pitch {@link #C}.
     * @see TonePitchInternational#B_NEUTRAL
     */
    public static final TonePitchEnglish B = create("B", 11);

    // ------------------------------ enharmonic changes (single sharp) ------------------------------

    /**
     * {@code E#} is an enharmonic change of {@link #F}.
     */
    public static final TonePitchEnglish E_SHARP = create("E#", F);

    /**
     * {@code A#} is an enharmonic change of {@link #B_FLAT}.
     */
    public static final TonePitchEnglish A_SHARP = create("A#", B_FLAT);

    /**
     * {@code B#} is an enharmonic change of {@link #C}.
     */
    public static final TonePitchEnglish B_SHARP = create("B#", C);

    // ------------------------------ enharmonic changes (single flat) ------------------------------

    /**
     * {@code Cb} is an enharmonic change of {@link #B}.
     */
    public static final TonePitchEnglish C_FLAT = create("Cb", B);

    /**
     * {@code Db} is an enharmonic change of {@link #C_SHARP}.
     */
    public static final TonePitchEnglish D_FLAT = create("Db", C_SHARP);

    /**
     * {@code Eb} is an enharmonic change of {@link #D_SHARP}.
     */
    public static final TonePitchEnglish E_FLAT = create("Eb", D_SHARP);

    /**
     * {@code Fb} is an enharmonic change of {@link #E}.
     */
    public static final TonePitchEnglish F_FLAT = create("Fb", E);

    /**
     * {@code Gb} is an enharmonic change {@link #F_SHARP}.
     */
    public static final TonePitchEnglish G_FLAT = create("Gb", F_SHARP);

    /**
     * {@code Ab} is an enharmonic change {@link #G_SHARP}.
     */
    public static final TonePitchEnglish A_FLAT = create("Ab", G_SHARP);

    // ------------------------------ enharmonic changes (double flat) ------------------------------

    /**
     * {@code Cbb} is an enharmonic change {@link #B_FLAT}.
     */
    public static final TonePitchEnglish C_DOUBLE_FLAT = create("Cbb", B_FLAT);

    /**
     * {@code Dbb} is an enharmonic change {@link #C}.
     */
    public static final TonePitchEnglish D_DOUBLE_FLAT = create("Dbb", C);

    /**
     * {@code Ebb} is an enharmonic change {@link #D}.
     */
    public static final TonePitchEnglish E_DOUBLE_FLAT = create("Ebb", D);

    /**
     * {@code Fbb} is an enharmonic change {@link #D_SHARP}.
     */
    public static final TonePitchEnglish F_DOUBLE_FLAT = create("Fbb", D_SHARP);

    /**
     * {@code Gbb} is an enharmonic change {@link #F}.
     */
    public static final TonePitchEnglish G_DOUBLE_FLAT = create("Gbb", F);

    /**
     * {@code Abb} is an enharmonic change {@link #G}.
     */
    public static final TonePitchEnglish A_DOUBLE_FLAT = create("Abb", G);

    /**
     * {@code Bbb} is an enharmonic change {@link #A}.
     */
    public static final TonePitchEnglish B_DOUBLE_FLAT = create("Bbb", A);

    // ------------------------------ enharmonic changes (double sharp) ------------------------------

    /**
     * {@code C##} is an enharmonic change {@link #D}.
     */
    public static final TonePitchEnglish C_DOUBLE_SHARP = create("C##", D);

    /**
     * {@code D##} is an enharmonic change {@link #E}.
     */
    public static final TonePitchEnglish D_DOUBLE_SHARP = create("D##", E);

    /**
     * {@code E##} is an enharmonic change {@link #F_SHARP}.
     */
    public static final TonePitchEnglish E_DOUBLE_SHARP = create("E##", F_SHARP);

    /**
     * {@code F##} is an enharmonic change {@link #G}.
     */
    public static final TonePitchEnglish F_DOUBLE_SHARP = create("F##", G);

    /**
     * {@code G##} is an enharmonic change {@link #A}.
     */
    public static final TonePitchEnglish G_DOUBLE_SHARP = create("G##", A);

    /**
     * {@code A##} is an enharmonic change {@link #B}.
     */
    public static final TonePitchEnglish A_DOUBLE_SHARP = create("A##", B);

    /**
     * {@code B##} is an enharmonic change {@link #C_SHARP}.
     */
    public static final TonePitchEnglish B_DOUBLE_SHARP = create("B##", C_SHARP);

    private final TonePitchEnglish otherCase;

    private TonePitchEnglish(String name, ChromaticStep step, EnharmonicType enharmonicType, TonePitchEnglish otherCase) {
        super(name, step, (otherCase == null) ? ToneNameCase.CAPITAL_CASE : ToneNameCase.LOWER_CASE, enharmonicType);
        if (otherCase == null) {
            String lowercaseName = name.toLowerCase(Locale.US);
            assert (!lowercaseName.equals(name));
            this.otherCase = new TonePitchEnglish(lowercaseName, step, enharmonicType, this);
        } else {
            this.otherCase = otherCase;
        }
        TonePitchEnglish duplicate = NAME2PITCH_MAP.put(name, this);
        assert (duplicate == null);
    }

    @Override
    public ToneNameStyleEnglish getNameStyle() {
        return STYLE;
    }

    @Override
    public TonePitchEnglish getReference() {
        return PITCHES_NORMAL[getStep().get()];
    }

    @Override
    public TonePitchEnglish with(ToneNameCase nameCase) {
        if (this.nameCase == nameCase) {
            return this;
        }
        return this.otherCase;
    }

    private static TonePitchEnglish create(String name, int step) {
        TonePitchEnglish pitch = create(name, ChromaticStep.of(step));
        assert (PITCHES_NORMAL[step] == null);
        PITCHES_NORMAL[step] = pitch;
        return pitch;
    }

    private static TonePitchEnglish create(String name, TonePitchEnglish reference) {
        TonePitchEnglish pitch = create(name, reference.getStep());
        return pitch;
    }

    private static TonePitchEnglish create(String name, ChromaticStep step) {
        EnharmonicType type = STYLE.getType(name);
        TonePitchEnglish pitch = new TonePitchEnglish(name, step, type, null);
        int typeIndex = type.getSignOffset() + 2;
        assert (PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] == null);
        PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] = pitch;
        return pitch;
    }

    public static final class ToneNameStyleEnglish extends ToneNameStyle<TonePitchEnglish> {

        private ToneNameStyleEnglish() {
            super();
        }

        @Override
        public String getName() {
            return "English";
        }

        @Override
        public String getSingleSharpSign() {
            return "#";
        }

        @Override
        public String getDoubleSharpSign() {
            return "##";
        }

        @Override
        public String getSingleFlatSign() {
            return "b";
        }

        @Override
        public String getDoubleFlatSign() {
            return "bb";
        }

        public String getNeutralSign() {
            return "";
        }

        @Override
        public TonePitchEnglish pitch(String name) {
            return NAME2PITCH_MAP.get(name);
        }

        @Override
        public Collection<TonePitchEnglish> values() {
            return PITCHES;
        }

        @Override
        public TonePitchEnglish pitch(ChromaticStep step, EnharmonicType type, ToneNameCase nameCase) {
            TonePitchEnglish result;
            if (type == null) {
                result = PITCHES_NORMAL[step.get()];
            } else {
                int typeIndex = type.getSignOffset() + 2;
                result = PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()];
            }
            if (result == null) {
                return null;
            }
            return result.with(nameCase);
        }

    }

}
