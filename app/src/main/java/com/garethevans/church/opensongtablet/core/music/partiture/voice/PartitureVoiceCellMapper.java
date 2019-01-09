package com.garethevans.church.opensongtablet.core.music.partiture.voice;

import com.garethevans.church.opensongtablet.core.filter.CharFilter;
import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.AbstractMapper;

/**
 * {@link AbstractMapper Mapper} for {@link PartitureVoiceCell}.
 */
public abstract class PartitureVoiceCellMapper extends AbstractMapper<PartitureVoiceCell> {

    static final CharFilter STOP_FILTER = ListCharFilter.NEWLINE.join(CHORD_START, ITEM_START, STAVE_START, BAR_SINGLE, BAR_REPEAT);

    public static final PartitureVoiceCellMapper CHORD_PRO = PartitureVoiceCellMapperChordPro.INSTANCE;

}
