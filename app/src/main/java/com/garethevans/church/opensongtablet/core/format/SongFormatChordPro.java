package com.garethevans.church.opensongtablet.core.format;

import com.garethevans.church.opensongtablet.core.music.partiture.PartitureMapper;

/**
 * {@link SongFormat} ChordPro which puts chords between the lyrics in square brackets.
 */
public class SongFormatChordPro extends SongFormat {

    public static final SongFormatChordPro INSTANCE = new SongFormatChordPro();

    private SongFormatChordPro() {
        super();
    }

    @Override
    public String getName() {
        return "ChordPro";
    }

    @Override
    protected PartitureMapper getPartitureMapper() {
        return PartitureMapper.CHORD_PRO;
    }

}
