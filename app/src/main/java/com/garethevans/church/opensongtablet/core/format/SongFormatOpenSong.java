package com.garethevans.church.opensongtablet.core.format;

import com.garethevans.church.opensongtablet.core.music.partiture.PartitureMapper;

public class SongFormatOpenSong extends SongFormat {

    public static final SongFormatOpenSong INSTANCE = new SongFormatOpenSong();

    private SongFormatOpenSong() {
        super();
    }

    @Override
    public String getName() {
        return "OpenSong";
    }

    @Override
    protected PartitureMapper getPartitureMapper() {
        return PartitureMapper.OPEN_SONG;
    }

}
