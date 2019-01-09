package com.garethevans.church.opensongtablet.core.music.tone;

import com.garethevans.church.opensongtablet.core.music.MusicalUnicodeConstants;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link TonePitch} with like {@link TonePitchNeoLatin} but with compact unicode symbols for
 * enharmonic signs (&#9839;, &#9837;, &#119082;, and &#119083;).
 */
public class TonePitchNeoLatinUnicodeSigns extends TonePitch {

    /**
     * {@link #getNameStyle() Name style} for {@link TonePitchNeoLatinUnicodeSigns}.
     */
    public static final ToneNameStyleNeoLatin STYLE = new ToneNameStyleNeoLatin();

    private static final String SINGLE_SHARP = MusicalUnicodeConstants.SINGLE_SHARP;

    private static final String DOUBLE_SHARP = MusicalUnicodeConstants.DOUBLE_SHARP;

    private static final String SINGLE_FLAT = MusicalUnicodeConstants.SINGLE_FLAT;

    private static final String DOUBLE_FLAT = MusicalUnicodeConstants.DOUBLE_FLAT;

    private static final Map<String, TonePitchNeoLatinUnicodeSigns> NAME2PITCH_MAP = new HashMap<>();

    private static final Collection<TonePitchNeoLatinUnicodeSigns> PITCHES = Collections.unmodifiableCollection(NAME2PITCH_MAP.values());

    private static final TonePitchNeoLatinUnicodeSigns[] PITCHES_NORMAL = new TonePitchNeoLatinUnicodeSigns[12];

    private static final TonePitchNeoLatinUnicodeSigns[][] PITCHES_BY_TYPE_AND_STEP = new TonePitchNeoLatinUnicodeSigns[5][12];

    /**
     * {@code Do} is the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getTonika() tonika}
     * of the common {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#C_MAJOR} key.
     *
     * @see TonePitchEnglish#C
     */
    public static final TonePitchNeoLatinUnicodeSigns DO = create("Do", 0);

    /**
     * {@code Do&#9839;} is one semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#C_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns DO_SHARP = create("Do" + MusicalUnicodeConstants.SINGLE_SHARP, 1);

    /**
     * {@code Re} is two semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#D
     */
    public static final TonePitchNeoLatinUnicodeSigns RE = create("Re", 2);

    /**
     * {@code Re&#9839;} is three semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#D_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns RE_SHARP = create("Re" + MusicalUnicodeConstants.SINGLE_SHARP, 3);

    /**
     * {@code Mi} is four semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#E
     */
    public static final TonePitchNeoLatinUnicodeSigns MI = create("Mi", 4);

    /**
     * {@code Fa} is five semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#F
     */
    public static final TonePitchNeoLatinUnicodeSigns FA = create("Fa", 5);

    /**
     * {@code Fa&#9839;} is six semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#F_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns FA_SHARP = create("Fa" + MusicalUnicodeConstants.SINGLE_SHARP, 6);

    /**
     * {@code Sol} is seven semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#G
     */
    public static final TonePitchNeoLatinUnicodeSigns SOL = create("Sol", 7);

    /**
     * {@code Sol&#9839;} is eight semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#G_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns SOL_SHARP = create("Sol" + MusicalUnicodeConstants.SINGLE_SHARP, 8);

    /**
     * {@code La} is nine semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#A
     */
    public static final TonePitchNeoLatinUnicodeSigns LA = create("La", 9);

    /**
     * {@code La&#9839;} is ten semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#B_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns LA_SHARP = create("La" + MusicalUnicodeConstants.SINGLE_SHARP, 10);

    /**
     * {@code Si} is eleven semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#B
     */
    public static final TonePitchNeoLatinUnicodeSigns SI = create("Si", 11);

    // ------------------------------ enharmonic changes (single sharp) ------------------------------

    /**
     * {@code Mi&#9839;} is an enharmonic change of {@link #FA}.
     *
     * @see TonePitchEnglish#E_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns MI_SHARP = create("Mi" + MusicalUnicodeConstants.SINGLE_SHARP, FA);

    /**
     * {@code His} is an enharmonic change of {@link #DO}.
     *
     * @see TonePitchEnglish#B_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns SI_SHARP = create("Si" + MusicalUnicodeConstants.SINGLE_SHARP, DO);

    // ------------------------------ enharmonic changes (single flat) ------------------------------

    /**
     * {@code Do&#9837;} is an enharmonic change of {@link #SI}.
     *
     * @see TonePitchEnglish#C_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns DO_FLAT = create("Do" + MusicalUnicodeConstants.SINGLE_FLAT, SI);

    /**
     * {@code Re&#9837;} is an enharmonic change of {@link #DO_SHARP}.
     *
     * @see TonePitchEnglish#D_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns RE_FLAT = create("Re" + MusicalUnicodeConstants.SINGLE_FLAT, DO_SHARP);

    /**
     * {@code Mi&#9837;} is an enharmonic change of {@link #RE_SHARP}.
     *
     * @see TonePitchEnglish#E_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns MI_FLAT = create("Mi" + MusicalUnicodeConstants.SINGLE_FLAT, RE_SHARP);

    /**
     * {@code Fa&#9837;} is an enharmonic change of {@link #MI}.
     *
     * @see TonePitchEnglish#F_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns FA_FLAT = create("Fa" + MusicalUnicodeConstants.SINGLE_FLAT, MI);

    /**
     * {@code Sol&#9837;} is an enharmonic change of {@link #FA_SHARP}.
     *
     * @see TonePitchEnglish#G_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns SOL_FLAT = create("Sol" + MusicalUnicodeConstants.SINGLE_FLAT, FA_SHARP);

    /**
     * {@code La&#9837;} is an enharmonic change of {@link #SOL_SHARP}.
     *
     * @see TonePitchEnglish#A_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns LA_FLAT = create("La" + MusicalUnicodeConstants.SINGLE_FLAT, SOL_SHARP);

    /**
     * {@code Si&#9837;} is an enharmonic change of {@link #LA_SHARP}.
     *
     * @see TonePitchEnglish#A_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns SI_FLAT = create("Si" + MusicalUnicodeConstants.SINGLE_FLAT, LA_SHARP);

    // ------------------------------ enharmonic changes (double flat) ------------------------------

    /**
     * {@code Do&#119083;} is an enharmonic change of {@link #SI_FLAT}.
     *
     * @see TonePitchEnglish#C_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns DO_DOUBLE_FLAT = create("Do" + MusicalUnicodeConstants.DOUBLE_FLAT, SI_FLAT);

    /**
     * {@code Re&#119083;} is an enharmonic change of {@link #DO}.
     *
     * @see TonePitchEnglish#D_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns RE_DOUBLE_FLAT = create("Re" + MusicalUnicodeConstants.DOUBLE_FLAT, DO);

    /**
     * {@code Mi&#119083;} is an enharmonic change of {@link #RE}.
     *
     * @see TonePitchEnglish#E_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns MI_DOUBLE_FLAT = create("Mi" + MusicalUnicodeConstants.DOUBLE_FLAT, RE);

    /**
     * {@code Fa&#119083;} is an enharmonic change of {@link #RE_SHARP}.
     *
     * @see TonePitchEnglish#F_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns FA_DOUBLE_FLAT = create("Fa" + MusicalUnicodeConstants.DOUBLE_FLAT, RE_SHARP);

    /**
     * {@code Sol&#119083;} is an enharmonic change of {@link #FA}.
     *
     * @see TonePitchEnglish#G_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns SOL_DOUBLE_FLAT = create("Sol" + MusicalUnicodeConstants.DOUBLE_FLAT, FA);

    /**
     * {@code La&#119083;} is an enharmonic change of {@link #SOL}.
     *
     * @see TonePitchEnglish#A_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns LA_DOUBLE_FLAT = create("La" + MusicalUnicodeConstants.DOUBLE_FLAT, SOL);

    /**
     * {@code Si&#119083;} is an enharmonic change of {@link #LA}.
     *
     * @see TonePitchEnglish#B_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatinUnicodeSigns SI_DOUBLE_FLAT = create("Si" + MusicalUnicodeConstants.DOUBLE_FLAT, LA);

    // ------------------------------ enharmonic changes (double sharp) ------------------------------

    /**
     * {@code Do&#119082;} is an enharmonic change of {@link #RE}.
     *
     * @see TonePitchEnglish#C_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns DO_DOUBLE_SHARP = create("Do" + MusicalUnicodeConstants.DOUBLE_SHARP, RE);

    /**
     * {@code Re&#119082;} is an enharmonic change of {@link #MI}.
     *
     * @see TonePitchEnglish#D_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns RE_DOUBLE_SHARP = create("Re" + MusicalUnicodeConstants.DOUBLE_SHARP, MI);

    /**
     * {@code Mi&#119082;} is an enharmonic change of {@link #FA_SHARP}.
     *
     * @see TonePitchEnglish#E_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns MI_DOUBLE_SHARP = create("Mi" + MusicalUnicodeConstants.DOUBLE_SHARP, FA_SHARP);

    /**
     * {@code Fa&#119082;} is an enharmonic change of {@link #SOL}.
     *
     * @see TonePitchEnglish#E_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns FA_DOUBLE_SHARP = create("Fa" + MusicalUnicodeConstants.DOUBLE_SHARP, SOL);

    /**
     * {@code Sol&#119082;} is an enharmonic change of {@link #LA}.
     *
     * @see TonePitchEnglish#G_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns SOL_DOUBLE_SHARP = create("Sol" + MusicalUnicodeConstants.DOUBLE_SHARP, LA);

    /**
     * {@code La&#119082;} is an enharmonic change of {@link #SI}.
     *
     * @see TonePitchEnglish#A_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns LA_DOUBLE_SHARP = create("La" + MusicalUnicodeConstants.DOUBLE_SHARP, SI);

    /**
     * {@code Si&#119082;} is an enharmonic change of {@link #DO_SHARP}.
     *
     * @see TonePitchEnglish#B_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatinUnicodeSigns SI_DOUBLE_SHARP = create("Si" + MusicalUnicodeConstants.DOUBLE_SHARP, DO_SHARP);

    private final TonePitchNeoLatinUnicodeSigns otherCase;

    private TonePitchNeoLatinUnicodeSigns(String name, ChromaticStep step, EnharmonicType enharmonicType, TonePitchNeoLatinUnicodeSigns otherCase) {
        super(name, step, (otherCase == null) ? ToneNameCase.CAPITAL_CASE : ToneNameCase.LOWER_CASE, enharmonicType);
        if (otherCase == null) {
            String lowercaseName = name.toLowerCase(Locale.US);
            assert (!lowercaseName.equals(name));
            this.otherCase = new TonePitchNeoLatinUnicodeSigns(lowercaseName, step, enharmonicType, this);
        } else {
            this.otherCase = otherCase;
        }
        TonePitchNeoLatinUnicodeSigns duplicate = NAME2PITCH_MAP.put(name, this);
        assert (duplicate == null);
    }

    @Override
    public ToneNameStyle getNameStyle() {
        return STYLE;
    }

    @Override
    public TonePitchNeoLatinUnicodeSigns getReference() {
        return PITCHES_NORMAL[getStep().get()];
    }

    @Override
    public TonePitchNeoLatinUnicodeSigns with(ToneNameCase nameCase) {
        if (this.nameCase == nameCase) {
            return this;
        }
        return this.otherCase;
    }

    private static TonePitchNeoLatinUnicodeSigns create(String name, int step) {
        TonePitchNeoLatinUnicodeSigns pitch = create(name, ChromaticStep.of(step));
        assert (PITCHES_NORMAL[step] == null);
        PITCHES_NORMAL[step] = pitch;
        return pitch;
    }

    private static TonePitchNeoLatinUnicodeSigns create(String name, TonePitchNeoLatinUnicodeSigns reference) {
        TonePitchNeoLatinUnicodeSigns pitch = create(name, reference.step);
        return pitch;
    }

    private static TonePitchNeoLatinUnicodeSigns create(String name, ChromaticStep step) {
        EnharmonicType type = STYLE.getType(name);
        TonePitchNeoLatinUnicodeSigns pitch = new TonePitchNeoLatinUnicodeSigns(name, step, type, null);
        int typeIndex = type.getSignOffset() + 2;
        assert (PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] == null);
        PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] = pitch;
        return pitch;
    }

    public static final class ToneNameStyleNeoLatin extends ToneNameStyleEuropean<TonePitchNeoLatinUnicodeSigns> {

        private ToneNameStyleNeoLatin() {
            super();
        }

        @Override
        public String getName() {
            return "Neo-Latin";
        }

        @Override
        public EnharmonicType getType(String name) {
            if (name.endsWith(DOUBLE_FLAT)) {
                return EnharmonicType.DOUBLE_FLAT;
            } else if (name.endsWith(DOUBLE_SHARP)) {
                return EnharmonicType.DOUBLE_SHARP;
            } else if (name.endsWith(SINGLE_SHARP)) {
                return EnharmonicType.SINGLE_SHARP;
            } else if (name.endsWith(SINGLE_FLAT)) {
                return EnharmonicType.SINGLE_FLAT;
            } else {
                return EnharmonicType.NORMAL;
            }
        }

        @Override
        public TonePitchNeoLatinUnicodeSigns pitch(String name) {
            return NAME2PITCH_MAP.get(name);
        }

        @Override
        public Collection<TonePitchNeoLatinUnicodeSigns> values() {
            return PITCHES;
        }

        @Override
        public TonePitchNeoLatinUnicodeSigns pitch(ChromaticStep step, EnharmonicType type, ToneNameCase nameCase) {
            TonePitchNeoLatinUnicodeSigns result;
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
