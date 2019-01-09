package com.garethevans.church.opensongtablet.core.music.partiture;

import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLineMapper;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLineMapperChordPro;

/**
 * {@link PartitureLineMapper} for {@link com.garethevans.church.opensongtablet.core.format.SongFormatChordPro ChordPro format}.
 */
public class PartitureLineMapperChordPro extends PartitureLineMapper {

    public static final PartitureLineMapperChordPro INSTANCE = new PartitureLineMapperChordPro();

    @Override
    protected PartitureVoiceLineMapper getVoiceLineMapper() {
        return PartitureVoiceLineMapperChordPro.INSTANCE;
    }
}
