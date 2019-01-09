package com.garethevans.church.opensongtablet.core.music.instrument.brass;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;

public class TubaEs extends Tuba {

    @Override
    public Tone getLowestTone() {
        return Tone.of(TonePitchEnglish.E_FLAT, 2);
    }
}
