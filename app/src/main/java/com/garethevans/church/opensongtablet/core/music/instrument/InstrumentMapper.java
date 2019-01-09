package com.garethevans.church.opensongtablet.core.music.instrument;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

public class InstrumentMapper extends AbstractMapper<Instrument> {

    public static final InstrumentMapper INSTANCE = new InstrumentMapper();

    @Override
    public Instrument parse(CharStream chars) {
        // TODO
        return null;
    }

    @Override
    public void format(Instrument instrument, Appendable buffer, SongFormatOptions options) throws IOException {
        if (instrument == null) {
            return;
        }
        // TODO
    }
}
