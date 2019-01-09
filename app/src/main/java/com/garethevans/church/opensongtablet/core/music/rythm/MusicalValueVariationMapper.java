package com.garethevans.church.opensongtablet.core.music.rythm;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link MusicalValueVariation}.
 */
public class MusicalValueVariationMapper extends AbstractMapper<MusicalValueVariation> {

    public static final MusicalValueVariationMapper INSTANCE = new MusicalValueVariationMapper();

    @Override
    public MusicalValueVariation parse(CharStream chars) {
        if (chars.expect(MusicalValueVariation.DOUBLE_PUNCTURED.toString(), false)) {
            return MusicalValueVariation.DOUBLE_PUNCTURED;
        } else if (chars.expect(MusicalValueVariation.PUNCTURED.toString(), false)) {
            return MusicalValueVariation.PUNCTURED;
        } else if (chars.expect(MusicalValueVariation.TRIPLET.toString(), false)) {
            return MusicalValueVariation.TRIPLET;
        }
        return MusicalValueVariation.NONE;
    }

    @Override
    public void format(MusicalValueVariation variation, Appendable buffer, SongFormatOptions options) throws IOException {
        if (variation == null) {
            return;
        }
        buffer.append(variation.toString());
    }
}
