/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.stave;


import com.garethevans.church.opensongtablet.core.ObjectHelper;
import com.garethevans.church.opensongtablet.core.format.FormatConstants;
import com.garethevans.church.opensongtablet.core.music.rythm.Beat;
import com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey;
import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the configuration and intro of a musical <em>stave</em>, what in general refers to the
 * five lines used in classic music notation. Here a {@link Stave} contains the {@link #getClef() clef},
 * {@link #getKey() key} and {@link #getBeat() beat} as well as the {@link #getVoices() voices} related
 * to the stave. It is used for the initial definition and configuration of a stave as well as for
 * changes to a stave within the line (to change the key or beat).<br>
 * <br>
 * <em>Stave</em> is the British term that is called <em>staff</em> in US English. As <em>staff</em>
 * is more ambiguous and the plural form is always <em>staves</em> the Brithish name was preferred even
 * though <em>staff</em> is used quite commonly.
 */
public final class Stave extends AbstractTransposable<Stave> {

    private static final String SEPARATOR = "" + FormatConstants.PROPERTIES_SEPARATOR;

    private StaveBracket bracket;

    private Clef clef;

    private int clefShift;

    private MusicalKey key;

    private Beat beat;

    private List<StaveVoice> voices;

    /**
     * The constructor.
     */
    public Stave() {
        this(null);
    }

    /**
     * The constructor.
     *
     * @param clef - see {@link #getClef()}.
     */
    public Stave(Clef clef) {
        this(clef, null);
    }

    /**
     * The constructor.
     *
     * @param clef - see {@link #getClef()}.
     * @param key  - see {@link #getKey()}.
     */
    public Stave(Clef clef, MusicalKey key) {
        this(clef, key, null);
    }

    /**
     * The constructor.
     *
     * @param clef - see {@link #getClef()}.
     * @param key  - see {@link #getKey()}.
     * @param beat - see {@link #getBeat()}.
     */
    public Stave(Clef clef, MusicalKey key, Beat beat) {
        super();
        this.clef = clef;
        this.key = key;
        this.beat = beat;
        this.voices = new ArrayList<>();
    }

    /**
     * @return the optional {@link StaveBracket bracket} of this stave. May be {@code null} for none.
     * In case one or more {@link com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLine lines}
     * in a {@link com.garethevans.church.opensongtablet.core.music.partiture.PartitureRow row} start with
     * the same {@link StaveBracket bracket}, they will be visually grouped together by a bracket of
     * this type. Otherwise the {@link StaveBracket bracket} is only displayed for its single
     * {@link com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLine line}.
     * In case a {@link Stave} is not at the beginning of a {@link com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLine line},
     * it will have no visual effect (unless a dynamic reformatting would insert a line break at this place,
     * what would then overrule the defaults from the start of the line).
     */
    public StaveBracket getBracket() {
        return this.bracket;
    }

    public void setBracket(StaveBracket bracket) {
        this.bracket = bracket;
    }

    /**
     * @return the optional {@link Clef} or {@code null} if undefined.
     */
    public Clef getClef() {
        return this.clef;
    }

    /**
     * @param clef the new value of {@link #getClef()}.
     */
    public void setClef(Clef clef) {
        this.clef = clef;
    }

    /**
     * @return the shift of the clef. By default {@code 0} for none. May e.g. be {@code 8} to shift a
     * {@link Clef#G G-clef} by one octave ("tenor clef").
     */
    public int getClefShift() {
        return this.clefShift;
    }

    /**
     * @param clefShift the new value of {@link #getClefShift()}.
     */
    public void setClefShift(int clefShift) {
        this.clefShift = clefShift;
    }

    /**
     * @return the optional {@link MusicalKey}. If {@code null} or the same {@link MusicalKey}
     * as the previous {@link Stave} on the same line, no enharmonic signs will be displayed (e.g. the
     * new {@link Stave} might only change the {@link Beat}).
     */
    public MusicalKey getKey() {
        return this.key;
    }

    /**
     * @param key the new value of {@link #getKey()}.
     */
    public void setKey(MusicalKey key) {
        this.key = key;
    }

    /**
     * @return the optional {@link Beat}. If {@code null} or the same {@link Beat}
     * as the previous {@link Stave} on the same line, no beat will be displayed (e.g. the
     * new {@link Stave} might only change the {@link Clef}).
     * If this is the first {@link Stave} of a {@link com.garethevans.church.opensongtablet.core.music.partiture.Partiture}
     * and no {@link Beat} is defined then {@link Beat#_4_4 4/4} is assumed (but not displayed).
     */
    public Beat getBeat() {
        return this.beat;
    }

    /**
     * @param beat the new value of {@link #getBeat()}.
     */
    public void setBeat(Beat beat) {
        this.beat = beat;
    }

    /**
     * @return the {@link List} of {@link StaveVoice}s.
     */
    public List<StaveVoice> getVoices() {
        return this.voices;
    }

    /**
     * @param voice the {@link StaveVoice} to add.
     * @see #getVoices()
     */
    public void addVoice(StaveVoice voice) {
        this.voices.add(voice);
    }

    @Override
    public Stave transpose(int steps, boolean diatonic, TransposeContext context) {
        if ((context.isKeepKey()) || (this.key == null)) {
            return this;
        }
        Stave transposed = new Stave();
        transposed.key = this.key.transpose(steps, diatonic, context);
        transposed.clefShift = this.clefShift;
        transposed.beat = this.beat;
        transposed.voices.addAll(this.voices);
        return transposed;
    }

    public void join(Stave otherStave, boolean joinVoices) {
        if (this.clef == null) {
            this.clef = otherStave.clef;
        }
        if (this.key == null) {
            this.key = otherStave.key;
        }
        if (this.clefShift == 0) {
            this.clefShift = otherStave.clefShift;
        }
        if (this.beat == null) {
            this.beat = otherStave.beat;
        }
        if (this.bracket == null) {
            this.bracket = otherStave.bracket;
        }
        if (joinVoices) {
            for (StaveVoice voice : otherStave.voices) {
                if (!this.voices.contains(voice)) {
                    this.voices.add(voice);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return ObjectHelper.hash(this.clef, this.key, this.beat);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        Stave other = (Stave) obj;
        if (this.clefShift != other.clefShift) {
            return false;
        } else if (!ObjectHelper.equals(this.clef, other.clef)) {
            return false;
        } else if (!ObjectHelper.equals(this.key, other.key)) {
            return false;
        } else if (!ObjectHelper.equals(this.beat, other.beat)) {
            return false;
        } else if (!ObjectHelper.equals(this.bracket, other.bracket)) {
            return false;
        } else if (!ObjectHelper.equals(this.voices, other.voices)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.bracket != null) {
            sb.append(this.bracket.getSyntax());
        }
        String separator = "";
        if (this.clef != null) {
            sb.append(FormatConstants.PROPERTY_CLEV);
            sb.append(FormatConstants.PROPERTIES_KEY_VALUE_SEPARATOR);
            sb.append(this.clef);
            if (this.clefShift != 0) {
                if (this.clefShift > 0) {
                    sb.append('+');
                }
                sb.append(this.clefShift);
            }
            separator = SEPARATOR;
        }
        if (this.key != null) {
            sb.append(separator);
            sb.append(FormatConstants.PROPERTY_KEY);
            sb.append(FormatConstants.PROPERTIES_KEY_VALUE_SEPARATOR);
            sb.append(this.key);
            separator = SEPARATOR;
        }
        if (this.beat != null) {
            sb.append(separator);
            sb.append(FormatConstants.PROPERTY_BEAT);
            sb.append(FormatConstants.PROPERTIES_KEY_VALUE_SEPARATOR);
            sb.append(this.beat);
            separator = SEPARATOR;
        }
        for (StaveVoice voice : this.voices) {
            sb.append(separator);
            sb.append(FormatConstants.PROPERTY_VOICE);
            sb.append(FormatConstants.PROPERTIES_KEY_VALUE_SEPARATOR);
            sb.append(voice);
            separator = SEPARATOR;
        }
        return sb.toString();
    }
}
