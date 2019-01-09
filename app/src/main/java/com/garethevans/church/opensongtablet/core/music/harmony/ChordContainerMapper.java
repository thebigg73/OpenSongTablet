package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.tone.ToneMapper;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchMapper;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link ChordContainer}.
 */
public class ChordContainerMapper extends AbstractMapper<ChordContainer> {

    public static final ChordContainerMapper INSTANCE = new ChordContainerMapper();

    private static final ListCharFilter START_FILTER = TonePitchMapper.FILTER_TONE_START.joinChars(ListCharFilter.NEWLINE).join(CHORD_START, CHORD_END, ITEM_START, ITEM_END);

    @Override
    public ChordContainer parse(CharStream chars) {
        Chord chord = ChordMapper.INSTANCE.parse(chars);
        String suffix = chars.readUntil(START_FILTER, true);
        if (suffix.isEmpty() && (chord == null)) {
            return null;
        }
        return new ChordContainer(chord, suffix);
    }

    @Override
    public void format(ChordContainer chordContainer, Appendable buffer, SongFormatOptions options) throws IOException {
        buffer.append(chordContainer.toString());
    }
}
