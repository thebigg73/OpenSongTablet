package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.filter.CharFilter;
import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.instrument.Instrument;
import com.garethevans.church.opensongtablet.core.music.instrument.InstrumentMapper;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link StaveVoice}.
 */
public class StaveVoiceMapper extends AbstractMapper<StaveVoice> {

    private static final CharFilter STOP_FILTER = ListCharFilter.NEWLINE.join(PROPERTIES_KEY_VALUE_SEPARATOR, PROPERTIES_SEPARATOR, VOICE_SEPARATOR, STAVE_END);

    public static final StaveVoiceMapper INSTANCE = new StaveVoiceMapper();

    @Override
    public StaveVoice parse(CharStream chars) {
        String name = chars.readUntil(STOP_FILTER, true);
        String shortcut = null;
        Instrument instrument = null;
        if (chars.expect(VOICE_SEPARATOR, false)) {
            shortcut = chars.readUntil(STOP_FILTER, true);
            if (chars.expect(VOICE_SEPARATOR, false)) {
                instrument = InstrumentMapper.INSTANCE.parse(chars);
            }
        }
        return new StaveVoice(name, shortcut, instrument);
    }

    @Override
    public void format(StaveVoice voice, Appendable buffer, SongFormatOptions options) throws IOException {
        if (voice == null) {
            return;
        }
        buffer.append(voice.getName());
        String abbreviation = voice.getAbbreviation();
        if (abbreviation != null) {
            buffer.append(VOICE_SEPARATOR);
            buffer.append(abbreviation);
        }
        Instrument instrument = voice.getInstrument();
        if (instrument != null) {
            if (abbreviation == null) {
                buffer.append(VOICE_SEPARATOR);
            }
            buffer.append(VOICE_SEPARATOR);
            InstrumentMapper.INSTANCE.format(instrument, buffer, options);
        }
    }
}
