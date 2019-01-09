package com.garethevans.church.opensongtablet.core.format;

import com.garethevans.church.opensongtablet.core.music.harmony.Chord;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceCell;
import com.garethevans.church.opensongtablet.core.music.rythm.MusicalValue;
import com.garethevans.church.opensongtablet.core.music.rythm.ValuedItem;
import com.garethevans.church.opensongtablet.core.music.stave.Stave;

import org.assertj.core.api.Assertions;

public abstract class SongFormatTest extends Assertions {

    protected void checkCell(PartitureVoiceCell cell, ValuedItem<?> item) {
        checkCell(cell, "", null, item);
    }

    protected void checkCell(PartitureVoiceCell cell, String lyric, ValuedItem<?> item) {
        checkCell(cell, lyric, null, item);
    }

    protected void checkCell(PartitureVoiceCell cell, String lyric, Chord chord) {
        checkCell(cell, lyric, chord, null);
    }

    protected void checkCell(PartitureVoiceCell cell, String lyric, Chord chord, ValuedItem<?> item) {
        checkCell(cell, lyric, chord, item, null);
    }

    protected void checkCell(PartitureVoiceCell cell, String lyric, Chord chord, ValuedItem<?> item, Stave stave) {
        assertThat(cell).isNotNull();
        assertThat(cell.getLyric()).isEqualTo(lyric);
        assertThat(cell.getChord()).isEqualTo(chord);
        assertThat(cell.getItem()).isEqualTo(item);
        assertThat(cell.getStave()).isEqualTo(stave);
    }
}
