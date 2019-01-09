package com.garethevans.church.opensongtablet.core.music.rythm;

import com.garethevans.church.opensongtablet.core.filter.CharFilter;
import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link ValuedItemDecoration}.
 */
public class ValuedItemDecorationMapper extends AbstractMapper<ValuedItemDecoration> {

    public static final ValuedItemDecorationMapper INSTANCE = new ValuedItemDecorationMapper();

    private static final CharFilter DECORATION_SYMBOL_FILTER = new ListCharFilter('h', 'w', true).join('~', '.').or(new ListCharFilter('H', 'W', true));

    private static final ListCharFilter DECORATION_STOP_FILTER = ListCharFilter.NEWLINE.join(DECORATION_END, ITEM_END, CHORD_END);

    @Override
    public ValuedItemDecoration parse(CharStream chars) {
        char c = chars.peek();
        String name = null;
        if (c == DECORATION_START) {
            chars.next();
            name = chars.readUntil(DECORATION_STOP_FILTER, false);
            c = chars.next();
            if (c != DECORATION_END) {
                // actually an error, but be tolerant
                // return null;
            }
        } else if (DECORATION_SYMBOL_FILTER.accept(c)) {
            c = chars.next();
            name = Character.toString(c);
        }
        return ValuedItemDecoration.of(name);
    }

    @Override
    public void format(ValuedItemDecoration decoration, Appendable buffer, SongFormatOptions options) throws IOException {
        if (decoration == null) {
            return;
        }
        if (options.isNormalizeItemDecorations()) {
            decoration = decoration.getReference();
        }
        String name = decoration.getName();
        if (name.length() > 1) {
            buffer.append(DECORATION_START);
            buffer.append(name);
            buffer.append(DECORATION_END);
        } else {
            buffer.append(name);
        }
    }
}
