package com.garethevans.church.opensongtablet.core.music.tone;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link TonePitch} with German {@link ToneNameStyle}. This style is used in the countries with these codes:
 * AT, CZ, DE, SE, DK, EE, FI, HU, LV, NO, PL, RS, SK. It uses {@link #H} for {@link TonePitchEnglish#B}
 * and {@link #B} for {@link TonePitchEnglish#B_FLAT}. Enharmonic signs are represented textually with
 * e.g. "Cis" for "C#" or "Ces" for "Cb".
 */
public class TonePitchGerman extends TonePitch {

    /**
     * {@link #getNameStyle() Name style} for {@link TonePitchGerman}.
     */
    public static final ToneNameStyleGerman STYLE = new ToneNameStyleGerman();

    private static final Map<String, TonePitchGerman> NAME2PITCH_MAP = new HashMap<>();

    private static final Collection<TonePitchGerman> PITCHES = Collections.unmodifiableCollection(NAME2PITCH_MAP.values());

    private static final TonePitchGerman[] PITCHES_NORMAL = new TonePitchGerman[12];

    private static final TonePitchGerman[][] PITCHES_BY_TYPE_AND_STEP = new TonePitchGerman[5][12];

    /**
     * {@code C} is the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getTonika() tonika}
     * of the common {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#C_MAJOR} key.
     *
     * @see TonePitchEnglish#C
     */
    public static final TonePitchGerman C = create("C", 0);

    /**
     * {@code Cis} is one semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#C_SHARP
     */
    public static final TonePitchGerman CIS = create("Cis", 1);

    /**
     * {@code D} is two semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#D
     */
    public static final TonePitchGerman D = create("D", 2);

    /**
     * {@code Dis} is three semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#D_SHARP
     */
    public static final TonePitchGerman DIS = create("Dis", 3);

    /**
     * {@code E} is four semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#E
     */
    public static final TonePitchGerman E = create("E", 4);

    /**
     * {@code F} is five semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#F
     */
    public static final TonePitchGerman F = create("F", 5);

    /**
     * {@code Fis} is six semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#F_SHARP
     */
    public static final TonePitchGerman FIS = create("Fis", 6);

    /**
     * {@code G} is seven semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#G
     */
    public static final TonePitchGerman G = create("G", 7);

    /**
     * {@code Gis} is eight semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#G_SHARP
     */
    public static final TonePitchGerman GIS = create("Gis", 8);

    /**
     * {@code A} is nine semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#A
     */
    public static final TonePitchGerman A = create("A", 9);

    /**
     * {@code B} is ten semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#B_FLAT
     */
    public static final TonePitchGerman B = create("B", 10);

    /**
     * {@code H} is eleven semitones higher than the pitch {@link #C}.
     *
     * @see TonePitchEnglish#B
     */
    public static final TonePitchGerman H = create("H", 11);

    // ------------------------------ enharmonic changes (single sharp) ------------------------------

    /**
     * {@code Eis} is an enharmonic change of {@link #F}.
     *
     * @see TonePitchEnglish#E_SHARP
     */
    public static final TonePitchGerman EIS = create("Eis", F);

    /**
     * {@code Ais} is an enharmonic change of {@link #B}.
     *
     * @see TonePitchEnglish#A_SHARP
     */
    public static final TonePitchGerman AIS = create("Ais", B);

    /**
     * {@code His} is an enharmonic change of {@link #C}.
     *
     * @see TonePitchEnglish#B_SHARP
     */
    public static final TonePitchGerman HIS = create("His", C);

    // ------------------------------ enharmonic changes (single flat) ------------------------------

    /**
     * {@code Ces} is an enharmonic change of {@link #H}.
     *
     * @see TonePitchEnglish#C_FLAT
     */
    public static final TonePitchGerman CES = create("Ces", H);

    /**
     * {@code Des} is an enharmonic change of {@link #CIS}.
     *
     * @see TonePitchEnglish#D_FLAT
     */
    public static final TonePitchGerman DES = create("Des", CIS);

    /**
     * {@code Es} is an enharmonic change of {@link #DIS}.
     *
     * @see TonePitchEnglish#E_FLAT
     */
    public static final TonePitchGerman ES = create("Es", DIS);

    /**
     * {@code Fes} is an enharmonic change of {@link #E}.
     *
     * @see TonePitchEnglish#F_FLAT
     */
    public static final TonePitchGerman FES = create("Fes", E);

    /**
     * {@code Ges} is an enharmonic change of {@link #FIS}.
     *
     * @see TonePitchEnglish#G_FLAT
     */
    public static final TonePitchGerman GES = create("Ges", FIS);

    /**
     * {@code As} is an enharmonic change of {@link #GIS}.
     *
     * @see TonePitchEnglish#A_FLAT
     */
    public static final TonePitchGerman AS = create("As", GIS);

    // HES is B

    // ------------------------------ enharmonic changes (double flat) ------------------------------

    /**
     * {@code Ceses} is an enharmonic change of {@link #B}.
     *
     * @see TonePitchEnglish#C_DOUBLE_FLAT
     */
    public static final TonePitchGerman CESES = create("Ceses", B);

    /**
     * {@code Deses} is an enharmonic change of {@link #C}.
     *
     * @see TonePitchEnglish#D_DOUBLE_FLAT
     */
    public static final TonePitchGerman DESES = create("Deses", C);

    /**
     * {@code Eses} is an enharmonic change of {@link #D}.
     *
     * @see TonePitchEnglish#E_DOUBLE_FLAT
     */
    public static final TonePitchGerman ESES = create("Eses", D);

    /**
     * {@code Feses} is an enharmonic change of {@link #DIS}.
     *
     * @see TonePitchEnglish#F_DOUBLE_FLAT
     */
    public static final TonePitchGerman FESES = create("Feses", DIS);

    /**
     * {@code Geses} is an enharmonic change of {@link #F}.
     *
     * @see TonePitchEnglish#G_DOUBLE_FLAT
     */
    public static final TonePitchGerman GESES = create("Geses", F);

    /**
     * {@code Ases} is an enharmonic change of {@link #G}.
     *
     * @see TonePitchEnglish#A_DOUBLE_FLAT
     */
    public static final TonePitchGerman ASES = create("Ases", G);

    /**
     * {@code Heses} is an enharmonic change of {@link #A}.
     *
     * @see TonePitchEnglish#B_DOUBLE_FLAT
     */
    public static final TonePitchGerman HESES = create("Heses", A);

    // ------------------------------ enharmonic changes (double sharp) ------------------------------

    /**
     * {@code Cisis} is an enharmonic change of {@link #D}.
     *
     * @see TonePitchEnglish#C_DOUBLE_SHARP
     */
    public static final TonePitchGerman CISIS = create("Cisis", D);

    /**
     * {@code Disis} is an enharmonic change of {@link #E}.
     *
     * @see TonePitchEnglish#D_DOUBLE_SHARP
     */
    public static final TonePitchGerman DISIS = create("Disis", E);

    /**
     * {@code Eisis} is an enharmonic change of {@link #FIS}.
     *
     * @see TonePitchEnglish#E_DOUBLE_SHARP
     */
    public static final TonePitchGerman EISIS = create("Eisis", FIS);

    /**
     * {@code Fisis} is an enharmonic change of {@link #G}.
     *
     * @see TonePitchEnglish#F_DOUBLE_SHARP
     */
    public static final TonePitchGerman FISIS = create("Fisis", G);

    /**
     * {@code Gisis} is an enharmonic change of {@link #A}.
     *
     * @see TonePitchEnglish#G_DOUBLE_SHARP
     */
    public static final TonePitchGerman GISIS = create("Gisis", A);

    /**
     * {@code Aisis} is an enharmonic change of {@link #H}.
     *
     * @see TonePitchEnglish#A_DOUBLE_SHARP
     */
    public static final TonePitchGerman AISIS = create("Aisis", H);

    /**
     * {@code Hisis} is an enharmonic change of {@link #CIS}.
     *
     * @see TonePitchEnglish#B_DOUBLE_SHARP
     */
    public static final TonePitchGerman HISIS = create("Hisis", CIS);

    private final TonePitchGerman otherCase;

    private TonePitchGerman(String name, ChromaticStep step, EnharmonicType enharmonicType, TonePitchGerman otherCase) {
        super(name, step, (otherCase == null) ? ToneNameCase.CAPITAL_CASE : ToneNameCase.LOWER_CASE, enharmonicType);
        if (otherCase == null) {
            String lowercaseName = name.toLowerCase(Locale.US);
            assert (!lowercaseName.equals(name));
            this.otherCase = new TonePitchGerman(lowercaseName, step, enharmonicType, this);
        } else {
            this.otherCase = otherCase;
        }
        TonePitchGerman duplicate = NAME2PITCH_MAP.put(name, this);
        assert (duplicate == null);
    }

    @Override
    public ToneNameStyle getNameStyle() {
        return STYLE;
    }

    @Override
    public TonePitchGerman getReference() {
        return PITCHES_NORMAL[getStep().get()];
    }

    @Override
    public TonePitchGerman with(ToneNameCase nameCase) {
        if (this.nameCase == nameCase) {
            return this;
        }
        return this.otherCase;
    }

    private static TonePitchGerman create(String name, int step) {
        TonePitchGerman pitch = create(name, ChromaticStep.of(step));
        assert (PITCHES_NORMAL[step] == null);
        PITCHES_NORMAL[step] = pitch;
        return pitch;
    }

    private static TonePitchGerman create(String name, TonePitchGerman reference) {
        TonePitchGerman pitch = create(name, reference.step);
        return pitch;
    }

    private static TonePitchGerman create(String name, ChromaticStep step) {
        EnharmonicType type = STYLE.getType(name);
        TonePitchGerman pitch = new TonePitchGerman(name, step, type, null);
        int typeIndex = type.getSignOffset() + 2;
        assert (PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] == null);
        PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] = pitch;
        return pitch;
    }

    public static final class ToneNameStyleGerman extends ToneNameStyleEuropean<TonePitchGerman> {

        private ToneNameStyleGerman() {
            super();
        }

        @Override
        public String getName() {
            return "German";
        }

        @Override
        public EnharmonicType getType(String name) {
            if (name.equalsIgnoreCase("B")) {
                return EnharmonicType.SINGLE_FLAT;
            }
            return super.getType(name);
        }

        @Override
        public TonePitchGerman pitch(String name) {
            return NAME2PITCH_MAP.get(name);
        }

        @Override
        public Collection<TonePitchGerman> values() {
            return PITCHES;
        }

        @Override
        public TonePitchGerman pitch(ChromaticStep step, EnharmonicType type, ToneNameCase nameCase) {
            TonePitchGerman result;
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
