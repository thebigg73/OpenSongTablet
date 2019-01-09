package com.garethevans.church.opensongtablet.core.music.rythm;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link Beat}.
 */
public class BeatMapper extends AbstractMapper<Beat> {

    public static final BeatMapper INSTANCE = new BeatMapper();

    @Override
    public Beat parse(CharStream chars) {
        Integer beats = chars.readInteger(3, false);
        if (beats == null) {
            return null;
        }
        int fract = 4;
        if (chars.expect(BEAT_SEPARATOR)) {
            Integer fraction = chars.readInteger(3, false);
            if (fraction != null) {
                fract = fraction.intValue();
            }
        }
        return Beat.of(beats.intValue(), fract);
    }

    @Override
    public void format(Beat beat, Appendable buffer, SongFormatOptions options) throws IOException {
        if (beat == null) {
            return;
        }
        int beats = beat.getBeats();
        buffer.append(Integer.toString(beats));
        int fraction = beat.getFaction();
        if (fraction != 4) {
            buffer.append(BEAT_SEPARATOR);
            buffer.append(Integer.toString(fraction));
        }
    }
}
