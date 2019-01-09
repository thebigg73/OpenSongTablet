package com.garethevans.church.opensongtablet.core.music.partiture.voice;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.stave.StaveBracketMapper;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link PartitureVoiceLine}.
 */
public abstract class PartitureVoiceLineMapper extends AbstractMapper<PartitureVoiceLine> {

    public static final PartitureVoiceLineMapper CHORD_PRO = PartitureVoiceLineMapperChordPro.INSTANCE;

    public static final PartitureVoiceLineMapper OPEN_SONG = PartitureVoiceLineMapperOpenSong.INSTANCE;

}
