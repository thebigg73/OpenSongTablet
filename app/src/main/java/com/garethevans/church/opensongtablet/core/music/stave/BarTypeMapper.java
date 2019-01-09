package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.AbstractSubParser;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link BarType}.
 */
public class BarTypeMapper extends AbstractMapper<BarType> {

    public static final BarTypeMapper INSTANCE = new BarTypeMapper();

    @Override
    public BarType parse(CharStream chars) {
        char c = chars.peek();
        if (c == '|') {
            chars.next();
            c = chars.peek();
            if (c == '|') {
                chars.next();
                return BarType.DOUBLE;
            } else if (c == ':') {
                chars.next();
                return BarType.START_REPEAT;
            } else if (c == ']') {
                chars.next();
                return BarType.THIN_THICK;
            } else {
                return BarType.SINGLE;
            }
        } else if (c == ':') {
            if (chars.expect("::", false) || chars.expect(":|:", false) || chars.expect(":||:", false)) {
                return BarType.START_END_REPEAT;
            } else if (chars.expect(":|", false)) {
                return BarType.END_REPEAT;
            }
        } else if (c == '[') {
            if (chars.expect("[|", false)) {
                return BarType.THICK_THIN;
            }
        }
        return null;
    }

    @Override
    public void format(BarType barType, Appendable buffer, SongFormatOptions options) throws IOException {
        if (barType == null) {
            return;
        }
        buffer.append(barType.getSymbol());
    }
}
