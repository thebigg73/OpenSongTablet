package com.garethevans.church.opensongtablet.core.music.partiture.voice;

import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordContainer;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordContainerMapper;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordContainerMapperChordPro;
import com.garethevans.church.opensongtablet.core.music.rythm.ValuedItem;
import com.garethevans.church.opensongtablet.core.music.rythm.ValuedItemMapper;
import com.garethevans.church.opensongtablet.core.music.rythm.ValuedItemMapperChordPro;
import com.garethevans.church.opensongtablet.core.music.stave.Bar;
import com.garethevans.church.opensongtablet.core.music.stave.BarMapper;
import com.garethevans.church.opensongtablet.core.music.stave.Stave;
import com.garethevans.church.opensongtablet.core.music.stave.StaveMapper;
import com.garethevans.church.opensongtablet.core.music.stave.StaveMapperChordPro;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link PartitureVoiceCellMapper} for {@link com.garethevans.church.opensongtablet.core.format.SongFormatChordPro ChordPro format}.
 */
public class PartitureVoiceCellMapperChordPro extends PartitureVoiceCellMapper {

    public static final PartitureVoiceCellMapperChordPro INSTANCE = new PartitureVoiceCellMapperChordPro();

    @Override
    public PartitureVoiceCell parse(CharStream chars) {
        PartitureVoiceCell currentCell = new PartitureVoiceCell();
        PartitureVoiceCell result = currentCell;
        Stave stave = StaveMapperChordPro.INSTANCE.parse(chars);
        currentCell.setStave(stave);

        ValuedItem<?> item = ValuedItemMapperChordPro.INSTANCE.parse(chars);
        ChordContainer chordContainer = ChordContainerMapperChordPro.INSTANCE.parse(chars);
        currentCell.setChordContainer(chordContainer);
        if ((item == null) && (chordContainer != null)) {
            item = ValuedItemMapperChordPro.INSTANCE.parse(chars);
        }
        currentCell.setItem(item);
        String lyric = chars.readUntil(STOP_FILTER, true);
        currentCell.setLyric(lyric);
        Bar bar = BarMapper.INSTANCE.parse(chars);
        currentCell.setBar(bar);
        return result;
    }

    @Override
    public void format(PartitureVoiceCell cell, Appendable buffer, SongFormatOptions options) throws IOException {
        if (cell == null) {
            return;
        }
        StaveMapperChordPro.INSTANCE.format(cell.getStave(), buffer, options);
        ValuedItemMapperChordPro.INSTANCE.format(cell.getItem(), buffer, options);
        ChordContainerMapperChordPro.INSTANCE.format(cell.getChordContainer(), buffer, options);
        buffer.append(cell.getLyric());
        BarMapper.INSTANCE.format(cell.getBar(), buffer, options);
    }
}
