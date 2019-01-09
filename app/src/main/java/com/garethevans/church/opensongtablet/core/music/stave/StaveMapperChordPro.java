package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

public class StaveMapperChordPro extends StaveMapper {

    public static final StaveMapperChordPro INSTANCE = new StaveMapperChordPro();

    private static final ListCharFilter STOP_FILTER = ListCharFilter.NEWLINE.join(STAVE_END);

    @Override
    public Stave parse(CharStream chars) {
        Stave stave = null;
        if (chars.expect(STAVE_START)) {
            stave = super.parse(chars);
            if (!chars.expect(STAVE_END, true)) {
                String gabarge = chars.readUntil(STOP_FILTER, true);
                // log gabarge
                chars.expect(STAVE_END, true);
            }
        }
        return stave;
    }

    @Override
    public void format(Stave stave, Appendable buffer, SongFormatOptions options) throws IOException {
        if (stave == null) {
            return;
        }
        buffer.append(STAVE_START);
        super.format(stave, buffer, options);
        buffer.append(STAVE_END);
    }
}
