package com.garethevans.church.opensongtablet.core.music.tone;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link TonePitch} with neo-latin {@link ToneNameStyle}. It is originated from <a href="https://en.wikipedia.org/wiki/Solf%C3%A8ge">Solfège</a>
 * that evolved in romance languages and especially from Italy. This style is used in countries like
 * Italy, France, Spain, Portugal, Romania, Israel, Turkey, Greece, Bulgaria, Russia, and in Latin America.
 * It uses the syllables {@link #DO}, {@link #RE}, {@link #MI}, {@link #FA}, {@link #SOL}, {@link #LA}, {@link #SI}
 * for the letters {@link TonePitchEnglish#C}, {@link TonePitchEnglish#D}, {@link TonePitchEnglish#E},
 * {@link TonePitchEnglish#F}, {@link TonePitchEnglish#G}, {@link TonePitchEnglish#A}, {@link TonePitchEnglish#B}.
 * Enharmonic signs are represented textually in Italian language (diesis for #, bemolle for b, and
 * doppio for double).
 */
public class TonePitchNeoLatin extends TonePitch {

    /**
     * {@link #getNameStyle() Name style} for {@link TonePitchNeoLatin}.
     */
    public static final ToneNameStyleNeoLatin STYLE = new ToneNameStyleNeoLatin();

    private static final String SHARP = "diesis";

    private static final String DOUBLE_SHARP = "doppio diesis";

    private static final String FLAT = "bemolle";

    private static final String DOUBLE_FLAT = "doppio bemolle";

    private static final Map<String, TonePitchNeoLatin> NAME2PITCH_MAP = new HashMap<>();

    private static final Collection<TonePitchNeoLatin> PITCHES = Collections.unmodifiableCollection(NAME2PITCH_MAP.values());

    private static final TonePitchNeoLatin[] PITCHES_NORMAL = new TonePitchNeoLatin[12];

    private static final TonePitchNeoLatin[][] PITCHES_BY_TYPE_AND_STEP = new TonePitchNeoLatin[5][12];

    /**
     * {@code Do} is the {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#getTonika() tonika}
     * of the common {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey#C_MAJOR} key.
     *
     * @see TonePitchEnglish#C
     */
    public static final TonePitchNeoLatin DO = create("Do", 0);

    /**
     * {@code Do diesis} is one semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#C_SHARP
     */
    public static final TonePitchNeoLatin DO_DIESIS = create("Do diesis", 1);

    /**
     * {@code Re} is two semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#D
     */
    public static final TonePitchNeoLatin RE = create("Re", 2);

    /**
     * {@code Re diesis} is three semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#D_SHARP
     */
    public static final TonePitchNeoLatin RE_DIESIS = create("Re diesis", 3);

    /**
     * {@code Mi} is four semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#E
     */
    public static final TonePitchNeoLatin MI = create("Mi", 4);

    /**
     * {@code Fa} is five semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#F
     */
    public static final TonePitchNeoLatin FA = create("Fa", 5);

    /**
     * {@code Fa diesis} is six semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#F_SHARP
     */
    public static final TonePitchNeoLatin FA_DIESIS = create("Fa diesis", 6);

    /**
     * {@code Sol} is seven semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#G
     */
    public static final TonePitchNeoLatin SOL = create("Sol", 7);

    /**
     * {@code Sol diesis} is eight semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#G_SHARP
     */
    public static final TonePitchNeoLatin SOL_DIESIS = create("Sol diesis", 8);

    /**
     * {@code La} is nine semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#A
     */
    public static final TonePitchNeoLatin LA = create("La", 9);

    /**
     * {@code La diesis} is ten semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#B_FLAT
     */
    public static final TonePitchNeoLatin LA_DIESIS = create("La diesis", 10);

    /**
     * {@code Si} is eleven semitones higher than the pitch {@link #DO}.
     *
     * @see TonePitchEnglish#B
     */
    public static final TonePitchNeoLatin SI = create("Si", 11);

    // ------------------------------ enharmonic changes (single sharp) ------------------------------

    /**
     * {@code Mi diesis} is an enharmonic change of {@link #FA}.
     *
     * @see TonePitchEnglish#E_SHARP
     */
    public static final TonePitchNeoLatin MI_DIESIS = create("Mi diesis", FA);

    /**
     * {@code His} is an enharmonic change of {@link #DO}.
     *
     * @see TonePitchEnglish#B_SHARP
     */
    public static final TonePitchNeoLatin SI_DIESIS = create("Si diesis", DO);

    // ------------------------------ enharmonic changes (single flat) ------------------------------

    /**
     * {@code Do bemolle} is an enharmonic change of {@link #SI}.
     *
     * @see TonePitchEnglish#C_FLAT
     */
    public static final TonePitchNeoLatin DO_BEMOLLE = create("Do bemolle", SI);

    /**
     * {@code Re bemolle} is an enharmonic change of {@link #DO_DIESIS}.
     *
     * @see TonePitchEnglish#D_FLAT
     */
    public static final TonePitchNeoLatin RE_BEMOLLE = create("Re bemolle", DO_DIESIS);

    /**
     * {@code Mi bemolle} is an enharmonic change of {@link #RE_DIESIS}.
     *
     * @see TonePitchEnglish#E_FLAT
     */
    public static final TonePitchNeoLatin MI_BEMOLLE = create("Mi bemolle", RE_DIESIS);

    /**
     * {@code Fa bemolle} is an enharmonic change of {@link #MI}.
     *
     * @see TonePitchEnglish#F_FLAT
     */
    public static final TonePitchNeoLatin FA_BEMOLLE = create("Fa bemolle", MI);

    /**
     * {@code Sol bemolle} is an enharmonic change of {@link #FA_DIESIS}.
     *
     * @see TonePitchEnglish#G_FLAT
     */
    public static final TonePitchNeoLatin SOL_BEMOLLE = create("Sol bemolle", FA_DIESIS);

    /**
     * {@code La bemolle} is an enharmonic change of {@link #SOL_DIESIS}.
     *
     * @see TonePitchEnglish#A_FLAT
     */
    public static final TonePitchNeoLatin LA_BEMOLLE = create("La bemolle", SOL_DIESIS);

    /**
     * {@code Si bemolle} is an enharmonic change of {@link #LA_DIESIS}.
     *
     * @see TonePitchEnglish#A_FLAT
     */
    public static final TonePitchNeoLatin SI_BEMOLLE = create("Si bemolle", LA_DIESIS);

    // ------------------------------ enharmonic changes (double flat) ------------------------------

    /**
     * {@code Do doppio bemolle} is an enharmonic change of {@link #SI_BEMOLLE}.
     *
     * @see TonePitchEnglish#C_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatin DO_DOPPIO_BEMOLLE = create("Do doppio bemolle", SI_BEMOLLE);

    /**
     * {@code Re doppio bemolle} is an enharmonic change of {@link #DO}.
     *
     * @see TonePitchEnglish#D_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatin RE_DOPPIO_BEMOLLE = create("Re doppio bemolle", DO);

    /**
     * {@code Mi doppio bemolle} is an enharmonic change of {@link #RE}.
     *
     * @see TonePitchEnglish#E_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatin MI_DOPPIO_BEMOLLE = create("Mi doppio bemolle", RE);

    /**
     * {@code Fa doppio bemolle} is an enharmonic change of {@link #RE_DIESIS}.
     *
     * @see TonePitchEnglish#F_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatin FA_DOPPIO_BEMOLLE = create("Fa doppio bemolle", RE_DIESIS);

    /**
     * {@code Sol doppio bemolle} is an enharmonic change of {@link #FA}.
     *
     * @see TonePitchEnglish#G_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatin SOL_DOPPIO_BEMOLLE = create("Sol doppio bemolle", FA);

    /**
     * {@code La doppio bemolle} is an enharmonic change of {@link #SOL}.
     *
     * @see TonePitchEnglish#A_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatin LA_DOPPIO_BEMOLLE = create("La doppio bemolle", SOL);

    /**
     * {@code Si doppio bemolle} is an enharmonic change of {@link #LA}.
     *
     * @see TonePitchEnglish#B_DOUBLE_FLAT
     */
    public static final TonePitchNeoLatin SI_DOPPIO_BEMOLLE = create("Si doppio bemolle", LA);

    // ------------------------------ enharmonic changes (double sharp) ------------------------------

    /**
     * {@code Do doppio diesis} is an enharmonic change of {@link #RE}.
     *
     * @see TonePitchEnglish#C_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatin DO_DOPPIO_DIESIS = create("Do doppio diesis", RE);

    /**
     * {@code Re doppio diesis} is an enharmonic change of {@link #MI}.
     *
     * @see TonePitchEnglish#D_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatin RE_DOPPIO_DIESIS = create("Re doppio diesis", MI);

    /**
     * {@code Mi doppio diesis} is an enharmonic change of {@link #FA_DIESIS}.
     *
     * @see TonePitchEnglish#E_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatin MI_DOPPIO_DIESIS = create("Mi doppio diesis", FA_DIESIS);

    /**
     * {@code Fa doppio diesis} is an enharmonic change of {@link #SOL}.
     *
     * @see TonePitchEnglish#E_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatin FA_DOPPIO_DIESIS = create("Fa doppio diesis", SOL);

    /**
     * {@code Sol doppio diesis} is an enharmonic change of {@link #LA}.
     *
     * @see TonePitchEnglish#G_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatin SOL_DOPPIO_DIESIS = create("Sol doppio diesis", LA);

    /**
     * {@code La doppio diesis} is an enharmonic change of {@link #SI}.
     *
     * @see TonePitchEnglish#A_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatin LA_DOPPIO_DIESIS = create("La doppio diesis", SI);

    /**
     * {@code Si doppio diesis} is an enharmonic change of {@link #DO_DIESIS}.
     *
     * @see TonePitchEnglish#B_DOUBLE_SHARP
     */
    public static final TonePitchNeoLatin SI_DOPPIO_DIESIS = create("Si doppio diesis", DO_DIESIS);

    private final TonePitchNeoLatin otherCase;

    private TonePitchNeoLatin(String name, ChromaticStep step, EnharmonicType enharmonicType, TonePitchNeoLatin otherCase) {
        super(name, step, (otherCase == null) ? ToneNameCase.CAPITAL_CASE : ToneNameCase.LOWER_CASE, enharmonicType);
        if (otherCase == null) {
            String lowercaseName = name.toLowerCase(Locale.US);
            assert (!lowercaseName.equals(name));
            this.otherCase = new TonePitchNeoLatin(lowercaseName, step, enharmonicType, this);
        } else {
            this.otherCase = otherCase;
        }
        TonePitchNeoLatin duplicate = NAME2PITCH_MAP.put(name, this);
        assert (duplicate == null);
    }

    @Override
    public ToneNameStyle getNameStyle() {
        return STYLE;
    }

    @Override
    public TonePitchNeoLatin getReference() {
        return PITCHES_NORMAL[getStep().get()];
    }

    @Override
    public TonePitchNeoLatin with(ToneNameCase nameCase) {
        if (this.nameCase == nameCase) {
            return this;
        }
        return this.otherCase;
    }

    private static TonePitchNeoLatin create(String name, int step) {
        TonePitchNeoLatin pitch = create(name, ChromaticStep.of(step));
        assert (PITCHES_NORMAL[step] == null);
        PITCHES_NORMAL[step] = pitch;
        return pitch;
    }

    private static TonePitchNeoLatin create(String name, TonePitchNeoLatin reference) {
        TonePitchNeoLatin pitch = create(name, reference.step);
        return pitch;
    }

    private static TonePitchNeoLatin create(String name, ChromaticStep step) {
        EnharmonicType type = STYLE.getType(name);
        TonePitchNeoLatin pitch = new TonePitchNeoLatin(name, step, type, null);
        int typeIndex = type.getSignOffset() + 2;
        assert (PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] == null);
        PITCHES_BY_TYPE_AND_STEP[typeIndex][step.get()] = pitch;
        return pitch;
    }

    public static final class ToneNameStyleNeoLatin extends ToneNameStyleEuropean<TonePitchNeoLatin> {

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
            } else if (name.endsWith(SHARP)) {
                return EnharmonicType.SINGLE_SHARP;
            } else if (name.endsWith(FLAT)) {
                return EnharmonicType.SINGLE_FLAT;
            } else {
                return EnharmonicType.NORMAL;
            }
        }

        @Override
        public TonePitchNeoLatin pitch(String name) {
            return NAME2PITCH_MAP.get(name);
        }

        @Override
        public Collection<TonePitchNeoLatin> values() {
            return PITCHES;
        }

        @Override
        public TonePitchNeoLatin pitch(ChromaticStep step, EnharmonicType type, ToneNameCase nameCase) {
            TonePitchNeoLatin result;
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
