package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchMapper;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link MusicalKey}.
 */
public class MusicalKeyMapper extends AbstractMapper<MusicalKey> {

    public static final MusicalKeyMapper INSTANCE = new MusicalKeyMapper();

    @Override
    public MusicalKey parse(CharStream chars) {
        TonePitch tonika = TonePitchMapper.INSTANCE.parse(chars);
        if (tonika == null) {
            return null;
        }
        chars.skipWhile(' '); // for ABC compatibility
        // detect tonal system (maj/min)...
        TonalSystem tonalSystem = TonalSystemMapper.INSTANCE.parse(chars);
        if (tonalSystem == null) {
            if (tonika.isLowercase()) {
                tonalSystem = TonalSystem.MINOR;
            } else {
                tonalSystem = TonalSystem.MAJOR;
            }
        }
        return MusicalKey.of(tonika, tonalSystem);
    }

    @Override
    public void format(MusicalKey key, Appendable buffer, SongFormatOptions options) throws IOException {
        if (key == null) {
            return;
        }
        TonePitch tonika = key.getTonika();
        if (options.isNormalizeMusicalKeys()) {
            tonika = tonika.with(options.getToneNameStyle());
        }
        TonePitchMapper.INSTANCE.format(tonika, buffer, options);
        TonalSystemMapper.INSTANCE.format(key.getTonalSystem(), buffer, options);
    }
}
