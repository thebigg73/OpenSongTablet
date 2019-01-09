package com.garethevans.church.opensongtablet.core.music.tone;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link TonePitch} with Dutch {@link ToneNameStyle}. This style is used in the Netherlands and Indonesia
 * as well as sometimes in scandinavia. It uses {@link #BES} for {@link TonePitchEnglish#B_FLAT}.
 * Enharmonic signs are represented textually with e.g. "Cis" for "C#" or "Ces" for "Cb".
 */
public class TonePitchDutch extends TonePitch {

    /**
     * {@link #getNameStyle() Name style} for {@link TonePitchDutch}.
     */
    public static final ToneNameStyleDutch STYLE = new ToneNameStyleDutch();

    private static final Map<String, TonePitchDutch> NAME2PITCH_MAP = new HashMap<>();

    private static final Collection<TonePitchDutch> PITCHES = Collections.unmodifiableCollection(NAME2PITCH_MAP.values());

    private static final TonePitchDutch[] PITCHES_NORMAL = new TonePitchDutch[12];

    private static final TonePitchDutch[][] PITCHES_BY_TYPE_AND_STEP = new TonePitchDutch[5][12];

    /**
     * {@code C} is the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getTonika() tonika}
     * of the common {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#C_MAJOR} key.
     *
     * @see TonePitchEnglish#C
     */
    public static final TonePitchDutch C = create("C", 0);

    /**
     * {@code Cis} is one semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#C_SHARP
     */
    public static final TonePitchDutch CIS = create("Cis", 1);

    /**
     * {@code D} is two semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#D
     */
    public static final TonePitchDutch D = create("D", 2);

    /**
     * {@code Dis} is three semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#D_SHARP
     */
    public static final TonePitchDutch DIS = create("Dis", 3);

    /**
     * {@code E} is four semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#E
     */
    public static final TonePitchDutch E = create("E", 4);

    /**
     * {@code F} is five semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#F
     */
    public static final TonePitchDutch F = create("F", 5);

    /**
     * {@code Fis} is six semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#F_SHARP
     */
    public static final TonePitchDutch FIS = create("Fis", 6);

    /**
     * {@code G} is seven semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#G
     */
    public static final TonePitchDutch G = create("G", 7);

    /**
     * {@code Gis} is eight semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#G_SHARP
     */
    public static final TonePitchDutch GIS = create("Gis", 8);

    /**
     * {@code A} is nine semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#A
     */
    public static final TonePitchDutch A = create("A", 9);

    /**
     * {@code Bes} is ten semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#B_FLAT
     */
    public static final TonePitchDutch BES = create("Bes", 10);

    /**
     * {@code B} is eleven semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#B
     */
    public static final TonePitchDutch B = create("B", 11);

    // ------------------------------ enharmonic changes (single sharp) ------------------------------

    /**
     * {@code Eis} is an enharmonic change of {@link #F}.
     *
     * @see TonePitchEnglish#E_SHARP
     */
    public static final TonePitchDutch EIS = create("Eis", F);

    /**
     * {@code Ais} is an enharmonic change of {@link #BES}.
     *
     * @see TonePitchEnglish#A_SHARP
     */
    public static final TonePitchDutch AIS = create("Ais", BES);

    /**
     * {@code Bis} is an enharmonic change of {@link #C}.
     *
     * @see TonePitchEnglish#B_SHARP
     */
    public static final TonePitchDutch BIS = create("Bis", C);

    // ------------------------------ enharmonic changes (single flat) ------------------------------

    /**
     * {@code Ces} is an enharmonic change of {@link #B}.
     *
     * @see TonePitchEnglish#C_FLAT
     */
    public static final TonePitchDutch CES = create("Ces", B);

    /**
     * {@code Des} is an enharmonic change of {@link #CIS}.
     *
     * @see TonePitchEnglish#D_FLAT
     */
    public static final TonePitchDutch DES = create("Des", CIS);

    /**
     * {@code Es} is an enharmonic change of {@link #DIS}.
     *
     * @see TonePitchEnglish#E_FLAT
     */
    public static final TonePitchDutch ES = create("Es", DIS);

    /**
     * {@code Fes} is an enharmonic change of {@link #E}.
     *
     * @see TonePitchEnglish#F_FLAT
     */
    public static final TonePitchDutch FES = create("Fes", E);

    /**
     * {@code Ges} is an enharmonic change of {@link #FIS}.
     *
     * @see TonePitchEnglish#G_FLAT
     */
    public static final TonePitchDutch GES = create("Ges", FIS);

    /**
     * {@code As} is an enharmonic change of {@link #GIS}.
     *
     * @see TonePitchEnglish#A_FLAT
     */
    public static final TonePitchDutch AS = create("As", GIS);

    // HES is B

    // ------------------------------ enharmonic changes (double flat) ------------------------------

    /**
     * {@code Ceses} is an enharmonic change of {@link #BES}.
     *
     * @see TonePitchEnglish#C_DOUBLE_FLAT
     */
    public static final TonePitchDutch CESES = create("Ceses", BES);

    /**
     * {@code Deses} is an enharmonic change of {@link #C}.
     *
     * @see TonePitchEnglish#D_DOUBLE_FLAT
     */
    public static final TonePitchDutch DESES = create("Deses", C);

    /**
     * {@code Eses} is an enharmonic change of {@link #D}.
     *
     * @see TonePitchEnglish#E_DOUBLE_FLAT
     */
    public static final TonePitchDutch ESES = create("Eses", D);

    /**
     * {@code Feses} is an enharmonic change of {@link #DIS}.
     *
     * @see TonePitchEnglish#F_DOUBLE_FLAT
     */
    public static final TonePitchDutch FESES = create("Feses", DIS);

    /**
     * {@code Geses} is an enharmonic change of {@link #F}.
     *
     * @see TonePitchEnglish#G_DOUBLE_FLAT
     */
    public static final TonePitchDutch GESES = create("Geses", F);

    /**
     * {@code Ases} is an enharmonic change of {@link #G}.
     *
     * @see TonePitchEnglish#A_DOUBLE_FLAT
     */
    public static final TonePitchDutch ASES = create("Ases", G);

    /**
     * {@code Beses} is an enharmonic change of {@link #A}.
     *
     * @see TonePitchEnglish#B_DOUBLE_FLAT
     */
    public static final TonePitchDutch BESES = create("Beses", A);

    // ------------------------------ enharmonic changes (double sharp) ------------------------------

    /**
     * {@code Cisis} is an enharmonic change of {@link #D}.
     *
     * @see TonePitchEnglish#C_DOUBLE_SHARP
     */
    public static final TonePitchDutch CISIS = create("Cisis", D);

    /**
     * {@code Disis} is an enharmonic change of {@link #E}.
     *
     * @see TonePitchEnglish#D_DOUBLE_SHARP
     */
    public static final TonePitchDutch DISIS = create("Disis", E);

    /**
     * {@code Eisis} is an enharmonic change of {@link #FIS}.
     *
     * @see TonePitchEnglish#E_DOUBLE_SHARP
     */
    public static final TonePitchDutch EISIS = create("Eisis", FIS);

    /**
     * {@code Fisis} is an enharmonic change of {@link #G}.
     *
     * @see TonePitchEnglish#F_DOUBLE_SHARP
     */
    public static final TonePitchDutch FISIS = create("Fisis", G);

    /**
     * {@code Gisis} is an enharmonic change of {@link #A}.
     *
     * @see TonePitchEnglish#G_DOUBLE_SHARP
     */
    public static final TonePitchDutch GISIS = create("Gisis", A);

    /**
     * {@code Aisis} is an enharmonic change of {@link #B}.
     *
     * @see TonePitchEnglish#A_DOUBLE_SHARP
     */
    public static final TonePitchDutch AISIS = create("Aisis", B);

    /**
     * {@code Bisis} is an enharmonic change of {@link #CIS}.
     *
     * @see TonePitchEnglish#B_DOUBLE_SHARP
     */
    public static final TonePitchDutch BISIS = create("Bisis", CIS);

    private final TonePitchDutch otherCase;

    private TonePitchDutch(String name, ChromaticStep step, EnharmonicType enharmonicType, TonePitchDutch otherCase) {
        super(name, step, (otherCase == null) ? ToneNameCase.CAPITAL_CASE : ToneNameCase.LOWER_CASE, enharmonicType);
        if (otherCase == null) {
            String lowercaseName = name.toLowerCase(Locale.US);
            assert (!lowercaseName.equals(name));
            this.otherCase = new TonePitchDutch(lowercaseName, step, enharmonicType, this);
        } else {
            this.otherCase = otherCase;
        }
        TonePitchDutch duplicate = NAME2PITCH_MAP.put(name, this);
        assert (duplicate == null);
    }

    @Override
    public ToneNameStyle getNameStyle() {
        return STYLE;
    }

    @Override
    public TonePitchDutch getReference() {
        return PITCHES_NORMAL[getStep().get()];
    }

    @Override
    public TonePitchDutch with(ToneNameCase nameCase) {
        if (this.nameCase == nameCase) {
            return this;
        }
        return this.otherCase;
    }

    private static TonePitchDutch create(String name, int step) {
        TonePitchDutch pitch = create(name, ChromaticStep.of(step));
        assert (PITCHES_NORMAL[step] == null);
        PITCHES_NORMAL[step] = pitch;
        return pitch;
    }

    private static TonePitchDutch create(String name, TonePitchDutch reference) {
        TonePitchDutch pitch = create(name, reference.step);
        return pitch;
    }

    private static TonePitchDutch create(String name, ChromaticStep step) {
        EnharmonicType type = STYLE.getType(name);
        TonePitchDutch pitch = new TonePitchDutch(name, step, type, null);
        int typeIndex = type.getSignOffset() + 2;
        assert (PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] == null) : pitch;
        PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] = pitch;
        return pitch;
    }

    public static final class ToneNameStyleDutch extends ToneNameStyleEuropean<TonePitchDutch> {

        private ToneNameStyleDutch() {
            super();
        }

        @Override
        public String getName() {
            return "Dutch";
        }

        @Override
        public TonePitchDutch pitch(String name) {
            return NAME2PITCH_MAP.get(name);
        }

        @Override
        public Collection<TonePitchDutch> values() {
            return PITCHES;
        }

        @Override
        public TonePitchDutch pitch(ChromaticStep step, EnharmonicType type, ToneNameCase nameCase) {
            TonePitchDutch result;
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
