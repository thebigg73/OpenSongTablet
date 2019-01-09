package com.garethevans.church.opensongtablet.core.music.rythm;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.note.Note;
import com.garethevans.church.opensongtablet.core.music.note.NoteMapper;
import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.tone.ToneMapper;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link ValuedItem}.
 */
public class ValuedItemMapper extends AbstractMapper<ValuedItem<?>> {

    public static final ValuedItemMapper INSTANCE = new ValuedItemMapper();

    @Override
    public ValuedItem<?> parse(CharStream chars) {
        Note note = NoteMapper.INSTANCE.parse(chars);
        if (note != null) {
            return note;
        }
        return RestMapper.INSTANCE.parse(chars);
    }

    @Override
    public void format(ValuedItem<?> item, Appendable buffer, SongFormatOptions options) throws IOException {
        if (item instanceof Note) {
            NoteMapper.INSTANCE.format((Note) item, buffer, options);
        } else if (item instanceof Rest) {
            RestMapper.INSTANCE.format((Rest) item, buffer, options);
        }
    }
}
