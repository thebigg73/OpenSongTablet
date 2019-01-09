package com.garethevans.church.opensongtablet.core.music.partiture;

import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSectionNameMapper;

/**
 * {@link PartitureMapper} for {@link com.garethevans.church.opensongtablet.core.format.SongFormatChordPro ChordPro format}.
 */
public class PartitureMapperChordPro extends PartitureMapper {

    public static final PartitureMapperChordPro INSTANCE = new PartitureMapperChordPro();

    @Override
    protected PartitureLineMapper getLineMapper() {
        return PartitureLineMapper.CHORD_PRO;
    }

    @Override
    protected PartitureSectionNameMapper getSectionNameMapper() {
        return PartitureSectionNameMapper.CHORD_PRO;
    }
}
