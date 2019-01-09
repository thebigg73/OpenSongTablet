package com.garethevans.church.opensongtablet.core.music.note;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.rythm.AbstractValuedItemMapper;
import com.garethevans.church.opensongtablet.core.music.rythm.MusicalValue;
import com.garethevans.church.opensongtablet.core.music.rythm.MusicalValueMapper;
import com.garethevans.church.opensongtablet.core.music.rythm.ValuedItemDecoration;
import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.tone.ToneMapper;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;
import java.util.List;

/**
 * {@link AbstractMapper Mapper} for {@link Note}.
 */
public class NoteMapper extends AbstractValuedItemMapper<Note> {

    public static final NoteMapper INSTANCE = new NoteMapper();

    @Override
    protected Note parseContent(CharStream chars, List<ValuedItemDecoration> decorations) {
        Tone tone = ToneMapper.INSTANCE.parse(chars);
        if (tone == null) {
            return null;
        }
        MusicalValue value = MusicalValueMapper.INSTANCE.parse(chars);
        if ((value == null) || (value == MusicalValue._1_1)) {
            value = MusicalValue._4_4;
        }
        return new Note(tone, value, decorations);
    }

    @Override
    protected void formatContent(Note note, Appendable buffer, SongFormatOptions options) throws IOException {
        ToneMapper.INSTANCE.format(note.getTone(), buffer, options);
    }
}
