package com.garethevans.church.opensongtablet.core.music.instrument.keyboard;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;

public class Piano extends KeyboardInstrument {

    private static final Tone LOWEST_TONE = Tone.of(TonePitchEnglish.A, -2);

    private static final Tone HIGHEST_TONE = Tone.of(TonePitchEnglish.C, 5);

    @Override
    public String getName() {
        return "Piano";
    }

    @Override
    public Tone getLowestTone() {
        return LOWEST_TONE;
    }

    public Tone getHighestTone() {
        return HIGHEST_TONE;
    }
}
