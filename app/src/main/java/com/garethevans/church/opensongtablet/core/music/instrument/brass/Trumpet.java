package com.garethevans.church.opensongtablet.core.music.instrument.brass;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;

public class Trumpet extends PitchOfBrassInstrument {

    @Override
    public String getName() {
        return "Trumpet";
    }

    @Override
    public Tone getLowestTone() {
        return Tone.BF3;
    }
}
