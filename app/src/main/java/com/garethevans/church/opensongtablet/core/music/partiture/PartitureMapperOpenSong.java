package com.garethevans.church.opensongtablet.core.music.partiture;

import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSectionNameMapper;

/**
 * {@link PartitureMapper} for {@link com.garethevans.church.opensongtablet.core.format.SongFormatChordPro ChordPro format}.
 */
public class PartitureMapperOpenSong extends PartitureMapper {

    public static final PartitureMapperOpenSong INSTANCE = new PartitureMapperOpenSong();

    @Override
    protected PartitureLineMapper getLineMapper() {
        return PartitureLineMapper.OPEN_SONG;
    }

    @Override
    protected PartitureSectionNameMapper getSectionNameMapper() {
        return PartitureSectionNameMapper.OPEN_SONG;
    }
}
