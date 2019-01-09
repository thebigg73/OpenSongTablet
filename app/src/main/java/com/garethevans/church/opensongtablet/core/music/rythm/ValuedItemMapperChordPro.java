package com.garethevans.church.opensongtablet.core.music.rythm;

import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

public class ValuedItemMapperChordPro extends ValuedItemMapper {

    public static final ValuedItemMapperChordPro INSTANCE = new ValuedItemMapperChordPro();

    @Override
    public ValuedItem<?> parse(CharStream chars) {
        ValuedItem<?> item = null;
        if (chars.expect(ITEM_START)) {
            item = super.parse(chars);
            chars.expect(ITEM_END, true);
        }
        return item;
    }

    @Override
    public void format(ValuedItem<?> item, Appendable buffer, SongFormatOptions options) throws IOException {
        if (item == null) {
            return;
        }
        buffer.append(ITEM_START);
        super.format(item, buffer, options);
        buffer.append(ITEM_END);
    }
}
