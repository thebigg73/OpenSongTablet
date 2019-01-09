package com.garethevans.church.opensongtablet.core.music.partiture.voice;

import com.garethevans.church.opensongtablet.core.music.harmony.Chord;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordContainer;
import com.garethevans.church.opensongtablet.core.music.partiture.PartitureCell;
import com.garethevans.church.opensongtablet.core.music.rythm.ValuedItem;
import com.garethevans.church.opensongtablet.core.music.stave.Bar;
import com.garethevans.church.opensongtablet.core.music.stave.Stave;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

public class PartitureVoiceCell extends PartitureCell<PartitureVoiceCell> {

    private ValuedItem<?> item;

    private String lyric;

    private Bar bar;

    private Stave stave;

    public PartitureVoiceCell() {
        super();
    }

    public PartitureVoiceCell(String lyric) {
        this(null, null, lyric, null);
    }

    public PartitureVoiceCell(Chord chord, String lyric) {
        this(new ChordContainer(chord), null, lyric, null);
    }

    public PartitureVoiceCell(ChordContainer chordContainer) {
        this(chordContainer, null, null, null);
    }

    public PartitureVoiceCell(ChordContainer chordContainer, String lyric) {
        this(chordContainer, null, lyric, null);
    }

    public PartitureVoiceCell(ChordContainer chordContainer, ValuedItem<?> item, String lyric, Bar bar) {
        super();
        this.chordContainer = chordContainer;
        this.item = item;
        this.lyric = lyric;
        this.bar = bar;
    }

    /**
     * @return the optional {@link Stave} to change the current stave (e.g. its {@link Stave#getKey() key}
     * or {@link Stave#getBeat() beat}) at the beginning of this cell. Otherwise {@code null}.
     */
    public Stave getStave() {
        return this.stave;
    }

    public void setStave(Stave stave) {
        this.stave = stave;
    }

    /**
     * @return the lyric (phrase) to be sung at this cell.
     */
    public String getLyric() {
        if (this.lyric == null) {
            return "";
        }
        return this.lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    /**
     * @return the {@link ValuedItem} of this cell that is the {@link com.garethevans.church.opensongtablet.core.music.tone.Tone}
     * or {@link com.garethevans.church.opensongtablet.core.music.rythm.Rest} to be played at this cell. May be {@code null} for none.
     */
    public ValuedItem<?> getItem() {
        return this.item;
    }

    public void setItem(ValuedItem<?> item) {
        this.item = item;
    }

    /**
     * @return the optional {@link Bar} of this cell that will be displayed to the right. May be {@code null} for none.
     * There is no left {@link Bar} as this would be the right {@link Bar} of the previous cell.
     */
    public Bar getBar() {
        return this.bar;
    }

    public void setBar(Bar bar) {
        this.bar = bar;
    }

    @Override
    public PartitureVoiceCell transpose(int steps, boolean diatonic, TransposeContext context) {
        PartitureVoiceCell transposed = new PartitureVoiceCell();
        transposed.bar = this.bar;
        transposed.lyric = this.lyric;
        if (this.chordContainer != null) {
            transposed.chordContainer = this.chordContainer.transpose(steps, diatonic, context);
        }
        if (this.item != null) {
            transposed.item = this.item.transpose(steps, diatonic, context);
        }
        return transposed;
    }

}
