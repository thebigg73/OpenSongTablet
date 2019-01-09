package com.garethevans.church.opensongtablet.core.music.instrument.string;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;

public class Guitar extends FrettedStringInstrument {

    public static final Guitar GUITAR = new Guitar(14);

    protected Guitar(int maxFret, Tone... tuning) {
        super(maxFret, tuning);
    }

    public Guitar(int maxFret) {
        super(maxFret, Tone.E2, Tone.A2, Tone.D3, Tone.G3, Tone.B3, Tone.E4);
    }

    @Override
    public String getName() {
        return "Guitar";
    }
}
