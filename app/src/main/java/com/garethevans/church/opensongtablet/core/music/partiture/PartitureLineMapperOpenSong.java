package com.garethevans.church.opensongtablet.core.music.partiture;

import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLineMapper;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLineMapperChordPro;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLineMapperOpenSong;

/**
 * {@link PartitureLineMapper} for {@link com.garethevans.church.opensongtablet.core.format.SongFormatOpenSong OpenSong format}.
 */
public class PartitureLineMapperOpenSong extends PartitureLineMapper {

    public static final PartitureLineMapperOpenSong INSTANCE = new PartitureLineMapperOpenSong();

    @Override
    protected PartitureVoiceLineMapper getVoiceLineMapper() {
        return PartitureVoiceLineMapperOpenSong.INSTANCE;
    }
}
