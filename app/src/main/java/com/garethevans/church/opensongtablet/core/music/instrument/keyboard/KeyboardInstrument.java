package com.garethevans.church.opensongtablet.core.music.instrument.keyboard;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.instrument.Instrument;

public abstract class KeyboardInstrument extends Instrument {

    public abstract Tone getHighestTone();

}
