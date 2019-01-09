package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

/**
 * A simple container for a {@link #getChord() chord} in combination with the {@link #getString()} parsed
 * string}. This is useful to preserve the original source information.
 *
 * @see #getChord()
 * @see #getSuffix()
 */
public class ChordContainer extends AbstractTransposable<ChordContainer> {

    static final ChordContainer EMPTY = new ChordContainer("");

    private final Chord chord;

    private final String suffix;

    private ChordContainer next;

    public ChordContainer(String suffix) {
        this(null, suffix);
    }

    public ChordContainer(Chord chord) {
        this(chord, "");
    }

    public ChordContainer(Chord chord, String suffix) {
        super();
        this.chord = chord;
        this.suffix = suffix;
    }

    /**
     * @return the parsed {@link Chord}. May be {@code null} if the parsed chord information did not
     * contain a valid {@link Chord}. E.g. "N.C." or "tacet" may pragmatically be provided as "chord" but can not
     * be represented by the class {@link Chord} as these examples are no valid chords.
     * @see #getSuffix()
     */
    public Chord getChord() {
        return this.chord;
    }

    /**
     * @return the prefix of the {@link Chord}. This may contain additional information
     * (whitespaces, clutter) to preserve the full original representation from the source format.
     * Further, this allows to preserve invalid chord information that can not be parsed as {@link Chord}
     * such as "D.C." or "X". In the latter case the {@link #getChord() chord} would be {@code null}.
     * @see #getChord()
     */
    public String getSuffix() {
        return this.suffix;
    }

    public ChordContainer getNext() {
        return this.next;
    }

    void setNext(ChordContainer next) {
        this.next = next;
    }

    @Override
    public ChordContainer transpose(int steps, boolean diatonic, TransposeContext context) {
        if (this.chord == null) {
            return this;
        }
        Chord transposedChord = this.chord.transpose(steps, diatonic, context);
        return new ChordContainer(transposedChord, this.suffix);
    }

    @Override
    public String toString() {
        if (this.chord == null) {
            return this.suffix;
        } else {
            return this.chord + this.suffix;
        }
    }

}
