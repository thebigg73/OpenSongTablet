package com.garethevans.church.opensongtablet.core.music.rythm;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link MusicalValue}.
 */
public class MusicalValueMapper extends AbstractMapper<MusicalValue> {

    public static final MusicalValueMapper INSTANCE = new MusicalValueMapper();

    private static final String BEAT_SEPRATOR_STRING = Character.toString(BEAT_SEPARATOR);

    @Override
    public MusicalValue parse(CharStream chars) {
        Integer i = chars.readInteger(2, false);
        if (i == null) {
            return null;
        }
        int beat = 1;
        int fraction;
        if (chars.expect(BEAT_SEPRATOR_STRING, false)) {
            beat = i.intValue();
            i = chars.readInteger(3, false);
            if (i == null) {
                fraction = 1; // actually an error but lets be tolerant
            } else {
                fraction = i.intValue();
            }
        } else {
            fraction = i.intValue();
        }
        MusicalValueVariation variation = MusicalValueVariationMapper.INSTANCE.parse(chars);
        return new MusicalValue(beat, fraction, variation);
    }

    @Override
    public void format(MusicalValue value, Appendable buffer, SongFormatOptions options) throws IOException {
        if (value == null) {
            return;
        }
        int beats = value.getBeats();
        if (beats > 1) {
            buffer.append(Integer.toString(beats));
            buffer.append(BEAT_SEPARATOR);
        }
        buffer.append(Integer.toString(value.getFaction()));
        MusicalValueVariationMapper.INSTANCE.format(value.getVariation(), buffer, options);
    }
}
