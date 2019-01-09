package com.garethevans.church.opensongtablet.core.music.tone;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

public class ToneMapper extends AbstractMapper<Tone> {

    public static final ToneMapper INSTANCE = new ToneMapper();

    @Override
    public Tone parse(CharStream chars) {
        TonePitch pitch = TonePitchMapper.INSTANCE.parse(chars);
        if (pitch == null) {
            return null;
        }
        int octave = 2;
        if (pitch.isLowercase()) {
            octave = 3;
        }
        while (true) {
            char c = chars.peek();
            if (c == Tone.OCTAVE_UP) {
                octave++;
            } else if (c == Tone.OCTAVE_DOWN) {
                octave--;
            } else {
                break;
            }
            chars.next();
        }
        return Tone.of(pitch, octave);
    }

    @Override
    public void format(Tone tone, Appendable buffer, SongFormatOptions options) throws IOException {
        buffer.append(tone.getName());
    }
}
