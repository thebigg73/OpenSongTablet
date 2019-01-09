package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link Clef}.
 */
public class ClefMapper extends AbstractMapper<Clef> {

    public static final ClefMapper INSTANCE = new ClefMapper();

    @Override
    public Clef parse(CharStream chars) {
        char c = chars.peek();
        Clef clef = Clef.valueOf("" + Character.toUpperCase(c));
        if (clef != null) {
            chars.next();
        }
        return clef;
    }

    @Override
    public void format(Clef clef, Appendable buffer, SongFormatOptions options) throws IOException {
        if (clef == null) {
            return;
        }
        buffer.append(clef.toString());
    }
}
