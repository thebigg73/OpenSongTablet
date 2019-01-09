package com.garethevans.church.opensongtablet.core.music.tone;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link TonePitch} with like {@link TonePitchNeoLatin} but with compact ascii symbols for
 * enharmonic signs (# and b).
 */
public class TonePitchNeoLatinAsciiSigns extends TonePitch {

    /**
     * {@link #getNameStyle() Name style} for {@link TonePitchNeoLatinAsciiSigns}.
     */
    public static final ToneNameStyleNeoLatin STYLE = new ToneNameStyleNeoLatin();

    private static final String SHARP = "#";

    private static final String DOUBLE_SHARP = "##";

    private static final String FLAT = "b";

    private static final String DOUBLE_FLAT = "bb";

    private static final Map<String, TonePitchNeoLatinAsciiSigns> NAME2PITCH_MAP = new HashMap<>();

    private static final Collection<TonePitchNeoLatinAsciiSigns> PITCHES = Collections.unmodifiableCollection(NAME2PITCH_MAP.values());

    private static final TonePitchNeoLatinAsciiSigns[] PITCHES_NORMAL = new TonePitchNeoLatinAsciiSigns[12];

    private static final TonePitchNeoLatinAsciiSigns[][] PITCHES_BY_TYPE_AND_STEP = new TonePitchNeoLatinAsciiSigns[5][12];

    /**
     * {@code Do} is the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getTonika() tonika}
     * of the common {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#C_MAJOR} key.
     *
     * @see TonePitchEnglish#C
     */
    public static final TonePitchNeoLatinAsciiSigns DO = create("Do", 0);

    /**
     * {@code Do#} is one semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#C_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns DO_SHARP = create("Do#", 1);

    /**
     * {@code Re} is two semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#D
     */
    public static final TonePitchNeoLatinAsciiSigns RE = create("Re", 2);

    /**
     * {@code Re#} is three semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#D_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns RE_SHARP = create("Re#", 3);

    /**
     * {@code Mi} is four semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#E
     */
    public static final TonePitchNeoLatinAsciiSigns MI = create("Mi", 4);

    /**
     * {@code Fa} is five semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#F
     */
    public static final TonePitchNeoLatinAsciiSigns FA = create("Fa", 5);

    /**
     * {@code Fa#} is six semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#F_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns FA_SHARP = create("Fa#", 6);

    /**
     * {@code Sol} is seven semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#G
     */
    public static final TonePitchNeoLatinAsciiSigns SOL = create("Sol", 7);

    /**
     * {@code Sol#} is eight semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#G_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns SOL_SHARP = create("Sol#", 8);

    /**
     * {@code La} is nine semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#A
     */
    public static final TonePitchNeoLatinAsciiSigns LA = create("La", 9);

    /**
     * {@code La#} is ten semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#B_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns LA_SHARP = create("La#", 10);

    /**
     * {@code Si} is eleven semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#B
     */
    public static final TonePitchNeoLatinAsciiSigns SI = create("Si", 11);

    // ------------------------------ enharmonic changes (single sharp) ------------------------------

    /**
     * {@code Mi#} is an enharmonic change of {@link #FA}.
     *
     * @see TonePitchEnglish#E_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns MI_SHARP = create("Mi#", FA);

    /**
     * {@code His} is an enharmonic change of {@link #DO}.
     *
     * @see TonePitchEnglish#B_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns SI_SHARP = create("Si#", DO);

    // ------------------------------ enharmonic changes (single flat) ------------------------------

    /**
     * {@code Dob} is an enharmonic change of {@link #SI}.
     *
     * @see TonePitchEnglish#C_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns DO_FLAT = create("Dob", SI);

    /**
     * {@code Reb} is an enharmonic change of {@link #DO_SHARP}.
     *
     * @see TonePitchEnglish#D_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns RE_FLAT = create("Reb", DO_SHARP);

    /**
     * {@code Mib} is an enharmonic change of {@link #RE_SHARP}.
     *
     * @see TonePitchEnglish#E_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns MI_FLAT = create("Mib", RE_SHARP);

    /**
     * {@code Fab} is an enharmonic change of {@link #MI}.
     *
     * @see TonePitchEnglish#F_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns FA_FLAT = create("Fab", MI);

    /**
     * {@code Solb} is an enharmonic change of {@link #FA_SHARP}.
     *
     * @see TonePitchEnglish#G_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns SOL_FLAT = create("Solb", FA_SHARP);

    /**
     * {@code Lab} is an enharmonic change of {@link #SOL_SHARP}.
     *
     * @see TonePitchEnglish#A_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns LA_FLAT = create("Lab", SOL_SHARP);

    /**
     * {@code Sib} is an enharmonic change of {@link #LA_SHARP}.
     *
     * @see TonePitchEnglish#A_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns SI_FLAT = create("Sib", LA_SHARP);

    // ------------------------------ enharmonic changes (double flat) ------------------------------

    /**
     * {@code Dobb} is an enharmonic change of {@link #SI_FLAT}.
     *
     * @see TonePitchEnglish#C_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns DO_DOUBLE_FLAT = create("Dobb", SI_FLAT);

    /**
     * {@code Rebb} is an enharmonic change of {@link #DO}.
     *
     * @see TonePitchEnglish#D_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns RE_DOUBLE_FLAT = create("Rebb", DO);

    /**
     * {@code Mibb} is an enharmonic change of {@link #RE}.
     *
     * @see TonePitchEnglish#E_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns MI_DOUBLE_FLAT = create("Mibb", RE);

    /**
     * {@code Fabb} is an enharmonic change of {@link #RE_SHARP}.
     *
     * @see TonePitchEnglish#F_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns FA_DOUBLE_FLAT = create("Fabb", RE_SHARP);

    /**
     * {@code Solbb} is an enharmonic change of {@link #FA}.
     *
     * @see TonePitchEnglish#G_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns SOL_DOUBLE_FLAT = create("Solbb", FA);

    /**
     * {@code Labb} is an enharmonic change of {@link #SOL}.
     *
     * @see TonePitchEnglish#A_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns LA_DOUBLE_FLAT = create("Labb", SOL);

    /**
     * {@code Sibb} is an enharmonic change of {@link #LA}.
     *
     * @see TonePitchEnglish#B_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinAsciiSigns SI_DOUBLE_FLAT = create("Sibb", LA);

    // ------------------------------ enharmonic changes (double sharp) ------------------------------

    /**
     * {@code Do##} is an enharmonic change of {@link #RE}.
     *
     * @see TonePitchEnglish#C_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns DO_DOUBLE_SHARP = create("Do##", RE);

    /**
     * {@code Re##} is an enharmonic change of {@link #MI}.
     *
     * @see TonePitchEnglish#D_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns RE_DOUBLE_SHARP = create("Re##", MI);

    /**
     * {@code Mi##} is an enharmonic change of {@link #FA_SHARP}.
     *
     * @see TonePitchEnglish#E_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns MI_DOUBLE_SHARP = create("Mi##", FA_SHARP);

    /**
     * {@code Fa##} is an enharmonic change of {@link #SOL}.
     *
     * @see TonePitchEnglish#E_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns FA_DOUBLE_SHARP = create("Fa##", SOL);

    /**
     * {@code Sol##} is an enharmonic change of {@link #LA}.
     *
     * @see TonePitchEnglish#G_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns SOL_DOUBLE_SHARP = create("Sol##", LA);

    /**
     * {@code La##} is an enharmonic change of {@link #SI}.
     *
     * @see TonePitchEnglish#A_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns LA_DOUBLE_SHARP = create("La##", SI);

    /**
     * {@code Si##} is an enharmonic change of {@link #DO_SHARP}.
     *
     * @see TonePitchEnglish#B_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinAsciiSigns SI_DOUBLE_SHARP = create("Si##", DO_SHARP);

    private final TonePitchNeoLatinAsciiSigns otherCase;

    private TonePitchNeoLatinAsciiSigns(String name, ChromaticStep step, EnharmonicType enharmonicType, TonePitchNeoLatinAsciiSigns otherCase) {
        super(name, step, (otherCase == null) ? ToneNameCase.CAPITAL_CASE : ToneNameCase.LOWER_CASE, enharmonicType);
        if (otherCase == null) {
            String lowercaseName = name.toLowerCase(Locale.US);
            assert (!lowercaseName.equals(name));
            this.otherCase = new TonePitchNeoLatinAsciiSigns(lowercaseName, step, enharmonicType, this);
        } else {
            this.otherCase = otherCase;
        }
        TonePitchNeoLatinAsciiSigns duplicate = NAME2PITCH_MAP.put(name, this);
        assert (duplicate == null);
    }

    @Override
    public ToneNameStyle getNameStyle() {
        return STYLE;
    }

    @Override
    public TonePitchNeoLatinAsciiSigns getReference() {
        return PITCHES_NORMAL[getStep().get()];
    }

    @Override
    public TonePitchNeoLatinAsciiSigns with(ToneNameCase nameCase) {
        if (this.nameCase == nameCase) {
            return this;
        }
        return this.otherCase;
    }

    private static TonePitchNeoLatinAsciiSigns create(String name, int step) {
        TonePitchNeoLatinAsciiSigns pitch = create(name, ChromaticStep.of(step));
        assert (PITCHES_NORMAL[step] == null);
        PITCHES_NORMAL[step] = pitch;
        return pitch;
    }

    private static TonePitchNeoLatinAsciiSigns create(String name, TonePitchNeoLatinAsciiSigns reference) {
        TonePitchNeoLatinAsciiSigns pitch = create(name, reference.step);
        return pitch;
    }

    private static TonePitchNeoLatinAsciiSigns create(String name, ChromaticStep step) {
        EnharmonicType type = STYLE.getType(name);
        TonePitchNeoLatinAsciiSigns pitch = new TonePitchNeoLatinAsciiSigns(name, step, type, null);
        int typeIndex = type.getSignOffset() + 2;
        assert (PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] == null);
        PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] = pitch;
        return pitch;
    }

    public static final class ToneNameStyleNeoLatin extends ToneNameStyleEuropean<TonePitchNeoLatinAsciiSigns> {

        private ToneNameStyleNeoLatin() {
            super();
        }

        @Override
        public String getName() {
            return "Neo-Latin-Ascii-Signs";
        }

        @Override
        public EnharmonicType getType(String name) {
            if (name.endsWith(DOUBLE_FLAT)) {
                return EnharmonicType.DOUBLE_FLAT;
            } else if (name.endsWith(DOUBLE_SHARP)) {
                return EnharmonicType.DOUBLE_SHARP;
            } else if (name.endsWith(SHARP)) {
                return EnharmonicType.SINGLE_SHARP;
            } else if (name.endsWith(FLAT)) {
                return EnharmonicType.SINGLE_FLAT;
            } else {
                return EnharmonicType.NORMAL;
            }
        }

        @Override
        public TonePitchNeoLatinAsciiSigns pitch(String name) {
            return NAME2PITCH_MAP.get(name);
        }

        @Override
        public Collection<TonePitchNeoLatinAsciiSigns> values() {
            return PITCHES;
        }

        @Override
        public TonePitchNeoLatinAsciiSigns pitch(ChromaticStep step, EnharmonicType type, ToneNameCase nameCase) {
            TonePitchNeoLatinAsciiSigns result;
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
