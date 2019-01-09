package com.garethevans.church.opensongtablet.core.music.rythm;

import com.garethevans.church.opensongtablet.core.ObjectHelper;
import com.garethevans.church.opensongtablet.core.filter.CharFilter;
import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.FormatConstants;
import com.garethevans.church.opensongtablet.core.music.MusicalUnicodeConstants;
import com.garethevans.church.opensongtablet.core.music.PeriodType;
import com.garethevans.church.opensongtablet.core.parser.AbstractFragmentParser;
import com.garethevans.church.opensongtablet.core.parser.CharStream;
import com.garethevans.church.opensongtablet.core.parser.FragmentParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Decoration of a {@link ValuedItem}.
 *
 * @see ValuedItem#getDecorations()
 */
public abstract class ValuedItemDecoration {

    private static final Map<String, ValuedItemDecoration> NAME_MAP = new HashMap<>();

    public static final MarkerDecoration CODA = MarkerDecoration.create("O", MusicalUnicodeConstants.CODA, "coda");

    public static final MarkerDecoration SEGNO = MarkerDecoration.create("S", MusicalUnicodeConstants.SEGNO,"segno");

    public static final MarkerDecoration DA_CAPO = MarkerDecoration.create("D.C.", MusicalUnicodeConstants.DA_CAPO,"dacapo");

    public static final MarkerDecoration DAL_SEGNO = MarkerDecoration.create("D.S.", MusicalUnicodeConstants.DAL_SEGNO,"dalsegno");

    public static final MarkerDecoration DA_CODA = MarkerDecoration.create("dacoda", "Da " + MusicalUnicodeConstants.CODA);

    public static final MarkerDecoration UP_BOW = MarkerDecoration.create("u", MusicalUnicodeConstants.COMBINING_UP_BOW, "upbow");

    public static final MarkerDecoration DOWN_BOW = MarkerDecoration.create("v", MusicalUnicodeConstants.COMBINING_DOWN_BOW, "downbow" );

    public static final MarkerDecoration FINE = MarkerDecoration.create("fine", "FINE");

    public static final AccentDecoration WEDGE = AccentDecoration.create("wedge", MusicalUnicodeConstants.ORNAMENT_STROKE_5);

    /** Left-hand pizzicato, or rasp for French horns. */
    public static final AccentDecoration PLUS = AccentDecoration.create("+", "+", "plus");

    /** cello thumb symbol. */
    public static final AccentDecoration THUMB = AccentDecoration.create("thumb", null);

    public static final AccentDecoration SNAP = AccentDecoration.create("snap", MusicalUnicodeConstants.COMBINING_SNAP_PIZZICATO);

    public static final AccentDecoration OPEN = AccentDecoration.create("open", MusicalUnicodeConstants.COMBINING_HARMONIC);

    public static final AccentDecoration BREATH = AccentDecoration.create("'", MusicalUnicodeConstants.BREATH_MARK, "breath");

    public static final AccentDecoration ROLL = AccentDecoration.create("~", "", "roll");

    public static final AccentDecoration ACCENT = AccentDecoration.create(">", MusicalUnicodeConstants.COMBINING_ACCENT, "accent", "emphasis", "L");

    public static final AccentDecoration FERMATA = AccentDecoration.create("H", MusicalUnicodeConstants.FERMATA, "fermata");

    public static final AccentDecoration INVERTED_FERMATA = AccentDecoration.create("invertedfermata", MusicalUnicodeConstants.FERMATA_BELOW);

    public static final AccentDecoration TENUTO = AccentDecoration.create("tenuto", MusicalUnicodeConstants.COMBINING_TENUTO);

    public static final AccentDecoration STACCATO = AccentDecoration.create(".", MusicalUnicodeConstants.COMBINING_STACCATO, "staccato");

    public static final TrillDecoration TRILL = TrillDecoration.create("T", null, MusicalUnicodeConstants.TR, "trill", "tr");

    public static final TrillDecoration TRILL_START = TrillDecoration.create("tr(", PeriodType.START, "", "trill(");

    public static final TrillDecoration TRILL_END = TrillDecoration.create("tr)", PeriodType.END, "", "trill)");

    public static final TrillDecoration LOWER_MORDENT = TrillDecoration.create("M", null, "", "mordent", "lowermordent");

    public static final TrillDecoration UPPER_MORDENT = TrillDecoration.create("P", null, "","pralltriller", "uppermordent");

    public static final TrillDecoration TURN = TrillDecoration.create("turn", null, MusicalUnicodeConstants.TURN);

    public static final TrillDecoration TURN_SLASH = TrillDecoration.create("turnx", null, MusicalUnicodeConstants.TURN_SLASH);

    public static final TrillDecoration INVERTED_TURN = TrillDecoration.create("invertedturn", null, MusicalUnicodeConstants.INVERTED_TURN);

    public static final TrillDecoration INVERTED_TURN_SLASH = TrillDecoration.create("invertedturnx", null, "");

    public static final DynamicDecoration CRESCENDO_START = DynamicDecoration.create("<(", PeriodType.START, MusicalUnicodeConstants.CRESCENDO, "crescendo(");

    public static final DynamicDecoration CRESCENDO_END = DynamicDecoration.create("<)", PeriodType.END, MusicalUnicodeConstants.CRESCENDO,"crescendo)");

    public static final DynamicDecoration DECRESCENDO_START = DynamicDecoration.create(">(", PeriodType.START, MusicalUnicodeConstants.DECRESCENDO, "decrescendo(", "diminuendo(");

    public static final DynamicDecoration DECRESCENDO_END = DynamicDecoration.create(">)", PeriodType.END, MusicalUnicodeConstants.DECRESCENDO, "decrescendo)", "diminuendo)");

    public static final VolumeDecoration PIANO_1 = new VolumeDecoration("p", MusicalUnicodeConstants.PIANO);

    public static final VolumeDecoration PIANO_2 = new VolumeDecoration("pp", MusicalUnicodeConstants.PIANO + MusicalUnicodeConstants.PIANO);

    public static final VolumeDecoration PIANO_3 = new VolumeDecoration("ppp", MusicalUnicodeConstants.PIANO + MusicalUnicodeConstants.PIANO + MusicalUnicodeConstants.PIANO);

    public static final VolumeDecoration PIANO_4 = new VolumeDecoration("pppp", MusicalUnicodeConstants.PIANO + MusicalUnicodeConstants.PIANO + MusicalUnicodeConstants.PIANO + MusicalUnicodeConstants.PIANO);

    public static final VolumeDecoration MEZZO_PIANO = new VolumeDecoration("mp", MusicalUnicodeConstants.MEZZO + MusicalUnicodeConstants.PIANO);

    public static final VolumeDecoration MEZZO_FORTE = new VolumeDecoration("mf", MusicalUnicodeConstants.MEZZO + MusicalUnicodeConstants.FORTE);

    public static final VolumeDecoration FORTE_1 = new VolumeDecoration("f", MusicalUnicodeConstants.FORTE);

    public static final VolumeDecoration FORTE_2 = new VolumeDecoration("ff", MusicalUnicodeConstants.FORTE + MusicalUnicodeConstants.FORTE);

    public static final VolumeDecoration FORTE_3 = new VolumeDecoration("fff", MusicalUnicodeConstants.FORTE + MusicalUnicodeConstants.FORTE + MusicalUnicodeConstants.FORTE);

    /** Sforzando (suddenly with force). */
    public static final VolumeDecoration SFORZANDO = new VolumeDecoration("sfz", MusicalUnicodeConstants.SUBITO + MusicalUnicodeConstants.FORTE + MusicalUnicodeConstants.Z);

    public static final PedalDecoration PEDAL_DOWN = PedalDecoration.create("ped,", PeriodType.START, MusicalUnicodeConstants.PEDAL_MARK, "pedal", "pedal(");

    public static final PedalDecoration PEDAL_UP = PedalDecoration.create("*", PeriodType.END, MusicalUnicodeConstants.PEDAL_UP_MARK, "pedal)");

    public static final PedalDecoration PEDAL_HALF_DOWN = PedalDecoration.create("pedalhalf", PeriodType.START, MusicalUnicodeConstants.HALF_PEDAL_MARK, "pedalhalf)");

    public static final SlurDecoration SLUR_START = SlurDecoration.create("(", PeriodType.START, "");

    public static final SlurDecoration SLUR_END = SlurDecoration.create(")", PeriodType.END, "");

    public static final TieDecoration TIE = TieDecoration.create("-");

    protected final String name;

    protected final String unicode;

    protected final ValuedItemPosition position;

    protected final PeriodType period;

    protected final ValuedItemDecoration reference;

    protected final int hash;

    private ValuedItemDecoration(String name, ValuedItemPosition position, PeriodType period, String unicode, ValuedItemDecoration reference) {
        super();
        this.name = name;
        this.position = position;
        this.period = period;
        this.unicode = unicode;
        if (reference == null) {
            this.reference = this;
            this.hash = this.name.hashCode();
        } else {
            this.reference = reference;
            this.hash = reference.hash;
        }
        ValuedItemDecoration duplicate = NAME_MAP.put(getKey(name), this);
        if (duplicate != null) {
            throw new IllegalStateException(name);
        }
    }

    /**
     * @return the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the {@link ValuedItemPosition position} of this decoration relative to the {@link ValuedItem#getDecorations() owning}
     * {@link ValuedItem item}.
     */
    public ValuedItemPosition getPosition() {
        return this.position;
    }

    /**
     * @return the {@link PeriodType} or {@code null} for none.
     */
    public PeriodType getPeriod() {
        return this.period;
    }

    /**
     * @return the unicode text representation or {@code null} if not available.
     */
    public String getUnicode() {

        if (this.unicode.isEmpty()) {
            return null;
        }
        return this.unicode;
    }

    /**
     * @return {@code true} if this decoration is used as suffix after the actual value, {@code false} otherwise (for prefix).
     * By default decorations have to be used as prefix. Only special decorations like {@link #SLUR_END} are
     * used as suffix.
     */
    public boolean isItemSuffix() {
        return (this == SLUR_END);
    }

    /**
     * @return the {@link ValuedItemDecoration} with the short {@link #getName() name} form. There can
     * be multiple {@link ValuedItemDecoration decorations} that are semantically equal but has different
     * {@link #getName() names}. The will all share the same reference with the short {@link #getName() name}.
     * All other {@link #getName() names} are called alias.
     */
    public ValuedItemDecoration getReference() {
        return this.reference;
    }

    /**
     * @param position the new {@link #getPosition() position}.
     * @return a copy of this decoration with the given {@link ValuedItemPosition position}.
     */
    public ValuedItemDecoration with(ValuedItemPosition position) {
        if (this.position == position) {
            return this;
        }
        return create(this.name, position, this.period);
    }

    protected void alias(String... altNames) {
        for (String alias : altNames) {
            alias(alias);
        }
    }

    protected ValuedItemDecoration alias(String alias) {
        ValuedItemDecoration decoration = create(alias, this.position, this.period);
        return decoration;
    }

    protected abstract ValuedItemDecoration create(String name, ValuedItemPosition position, PeriodType period);

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj == null) || !(obj instanceof ValuedItemDecoration)) {
            return false;
        }
        ValuedItemDecoration other = (ValuedItemDecoration) obj;
        if (this.reference != other.reference) {
            return false;
        }
        if (ObjectHelper.equals(this.period, other.period)) {
            return false;
        }
        if (ObjectHelper.equals(this.position, other.position)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (this.name.length() == 1) {
            return this.name;
        }
        return FormatConstants.DECORATION_START + this.name + FormatConstants.DECORATION_END;
    }

    public static ValuedItemDecoration of(String name) {
        if ((name == null) || name.isEmpty()) {
            return null;
        }
        return NAME_MAP.get(getKey(name));
    }

    private static String getKey(String name) {
        if (name.length() > 1) {
            name = name.toLowerCase(Locale.US);
        }
        return name;
    }

    public static class DynamicDecoration extends ValuedItemDecoration {

        public DynamicDecoration(String name, ValuedItemPosition position, PeriodType period, String unicode, ValuedItemDecoration reference) {
            super(name, position, period, unicode, reference);
        }

        @Override
        protected DynamicDecoration create(String name, ValuedItemPosition position, PeriodType period) {
            return new DynamicDecoration(name, position, period, this.unicode, this.reference);
        }

        private static DynamicDecoration create(String name, PeriodType period, String unicode, String... altNames) {
            DynamicDecoration decoration = new DynamicDecoration(name, ValuedItemPosition.BOTTOM, period, unicode, null);
            decoration.alias(altNames);
            return decoration;
        }
    }

    public static class VolumeDecoration extends ValuedItemDecoration {

        public VolumeDecoration(String name, String unicode) {
            this(name, ValuedItemPosition.BOTTOM, null, unicode, null);
        }

        public VolumeDecoration(String name, ValuedItemPosition position, PeriodType period, String unicode, ValuedItemDecoration reference) {
            super(name, position, period, unicode, reference);
        }

        @Override
        protected VolumeDecoration create(String name, ValuedItemPosition position, PeriodType period) {
            return new VolumeDecoration(name, position, period, this.unicode, this.reference);
        }
    }

    public static class TrillDecoration extends ValuedItemDecoration {

        public TrillDecoration(String name, ValuedItemPosition position, PeriodType period, String unicode, ValuedItemDecoration reference) {
            super(name, position, period, unicode, reference);
        }

        @Override
        protected TrillDecoration create(String name, ValuedItemPosition position, PeriodType period) {
            return new TrillDecoration(name, position, period, this.unicode, this.reference);
        }

        private static TrillDecoration create(String name, PeriodType period, String unicode, String... altNames) {
            TrillDecoration decoration = new TrillDecoration(name, ValuedItemPosition.TOP, period, unicode, null);
            decoration.alias(altNames);
            return decoration;
        }
    }

    public static class PedalDecoration extends ValuedItemDecoration {

        public PedalDecoration(String name, ValuedItemPosition position, PeriodType period, String unicode, ValuedItemDecoration reference) {
            super(name, position, period, unicode, reference);
        }

        @Override
        protected PedalDecoration create(String name, ValuedItemPosition position, PeriodType period) {
            return new PedalDecoration(name, position, period, this.unicode, this.reference);
        }

        private static PedalDecoration create(String name, PeriodType period, String unicode, String... altNames) {
            PedalDecoration decoration = new PedalDecoration(name, ValuedItemPosition.TOP, period, unicode, null);
            decoration.alias(altNames);
            return decoration;
        }
    }

    public static class AccentDecoration extends ValuedItemDecoration {

        public AccentDecoration(String name, ValuedItemPosition position, PeriodType period, String unicode, ValuedItemDecoration reference) {
            super(name, position, period, unicode, reference);
        }

        @Override
        protected AccentDecoration create(String name, ValuedItemPosition position, PeriodType period) {
            return new AccentDecoration(name, position, period, this.unicode, this.reference);
        }

        private static AccentDecoration create(String name, String unicode, String... altNames) {
            return create(name, ValuedItemPosition.TOP, unicode, altNames);
        }

        private static AccentDecoration create(String name, ValuedItemPosition position, String unicode, String... altNames) {
            AccentDecoration decoration = new AccentDecoration(name, position, null, unicode, null);
            decoration.alias(altNames);
            return decoration;
        }
    }

    public static class MarkerDecoration extends ValuedItemDecoration {

        public MarkerDecoration(String name, ValuedItemPosition position, PeriodType period, String unicode, ValuedItemDecoration reference) {
            super(name, position, period, unicode, reference);
        }

        @Override
        protected MarkerDecoration create(String name, ValuedItemPosition position, PeriodType period) {
            return new MarkerDecoration(name, position, period, this.unicode, this.reference);
        }

        private static MarkerDecoration create(String name, String unicode, String... altNames) {
            return create(name, ValuedItemPosition.TOP, unicode, altNames);
        }

        private static MarkerDecoration create(String name, ValuedItemPosition position, String unicode, String... altNames) {
            MarkerDecoration decoration = new MarkerDecoration(name, position, null, unicode, null);
            decoration.alias(altNames);
            return decoration;
        }
    }

    public static class SlurDecoration extends ValuedItemDecoration {

        public SlurDecoration(String name, ValuedItemPosition position, PeriodType period, String unicode, ValuedItemDecoration reference) {
            super(name, position, period, unicode, reference);
        }

        @Override
        protected SlurDecoration create(String name, ValuedItemPosition position, PeriodType period) {
            return new SlurDecoration(name, position, period, this.unicode, this.reference);
        }

        private static SlurDecoration create(String name, PeriodType period, String unicode, String... altNames) {
            SlurDecoration decoration = new SlurDecoration(name, ValuedItemPosition.ABOVE, period, unicode, null);
            decoration.alias(altNames);
            return decoration;
        }
    }

    public static class TieDecoration extends ValuedItemDecoration {

        public TieDecoration(String name, ValuedItemPosition position, PeriodType period, String unicode, ValuedItemDecoration reference) {
            super(name, position, period, unicode, reference);
        }

        @Override
        protected TieDecoration create(String name, ValuedItemPosition position, PeriodType period) {
            return new TieDecoration(name, position, period, this.unicode, this.reference);
        }

        private static TieDecoration create(String name, String... altNames) {
            TieDecoration decoration = new TieDecoration(name, ValuedItemPosition.BELOW, null, null, null);
            decoration.alias(altNames);
            return decoration;
        }
    }
}
