package com.garethevans.church.opensongtablet.core.music.rythm;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link AbstractMapper} for {@link ValuedItem}.
 */
public abstract class AbstractValuedItemMapper<I extends ValuedItem<?>> extends AbstractMapper<I> {

    @Override
    public I parse(CharStream chars) {
        List<ValuedItemDecoration> decorations = new ArrayList<>();
        while (true) {
            ValuedItemDecoration decoration = ValuedItemDecorationMapper.INSTANCE.parse(chars);
            if (decoration == null) {
                break;
            }
            assert (!decoration.isItemSuffix());
            decorations.add(decoration);
        }
        I item = parseContent(chars, decorations);
        while (true) {
            ValuedItemDecoration decoration = ValuedItemDecorationMapper.INSTANCE.parse(chars);
            if (decoration == null) {
                break;
            }
            assert (decoration.isItemSuffix());
            decorations.add(decoration);
        }
        return item;
    }

    protected abstract I parseContent(CharStream chars, List<ValuedItemDecoration> decorations);

    @Override
    public void format(I item, Appendable buffer, SongFormatOptions options) throws IOException {
        if (item == null) {
            return;
        }
        // decoration prefixes
        ValuedItemDecoration suffix = null;
        int suffixCount = 0;
        for (ValuedItemDecoration decoration : item.getDecorations()) {
            if (decoration.isItemSuffix()) {
                suffix = decoration;
                suffixCount++;
            } else {
                ValuedItemDecorationMapper.INSTANCE.format(decoration, buffer, options);
            }
        }
        formatContent(item, buffer, options);
        MusicalValueMapper.INSTANCE.format(item.getValue(), buffer, options);
        // decoration suffixes
        if (suffixCount == 1) {
            ValuedItemDecorationMapper.INSTANCE.format(suffix, buffer, options);
        } else if (suffixCount > 1) {
            for (ValuedItemDecoration decoration : item.getDecorations()) {
                if (decoration.isItemSuffix()) {
                    ValuedItemDecorationMapper.INSTANCE.format(decoration, buffer, options);
                }
            }
        }
    }

    protected abstract void formatContent(I item, Appendable buffer, SongFormatOptions options) throws IOException;

}