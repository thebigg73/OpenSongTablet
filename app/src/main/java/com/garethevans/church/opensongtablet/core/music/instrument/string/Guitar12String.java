package com.garethevans.church.opensongtablet.core.music.instrument.string;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;

public class Guitar12String extends Guitar {

    public static final Guitar12String GUITAR_12_STRING = new Guitar12String();

    public Guitar12String() {
        super(16, Tone.E2, Tone.E3, Tone.A2, Tone.A3, Tone.D3, Tone.D4, Tone.G3, Tone.G4, Tone.B3, Tone.B3, Tone.E4, Tone.E4);
    }

    @Override
    public String getName() {
        return "12-String Guitar";
    }
}
