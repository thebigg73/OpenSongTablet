package com.garethevans.church.opensongtablet.core.music.instrument.brass;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;

public class PicoloTrumpet extends Trumpet {

    @Override
    public String getName() {
        return "Picolo Trumpet";
    }

    @Override
    public Tone getLowestTone() {
        return Tone.BF4;
    }
}
