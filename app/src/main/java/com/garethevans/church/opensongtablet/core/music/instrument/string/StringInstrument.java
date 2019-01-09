package com.garethevans.church.opensongtablet.core.music.instrument.string;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.instrument.Instrument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class StringInstrument extends Instrument {

    private final List<Tone> tuning;

    private final Tone lowestTone;

    public StringInstrument(Tone... tuning) {
        super();
        this.tuning = Collections.unmodifiableList(Arrays.asList(tuning));
        Tone lowest = null;
        for (Tone tone : tuning) {
            if (tone.isLower(lowest)) {
                lowest = tone;
            }
        }
        this.lowestTone = lowest;
    }

    public List<Tone> getTuning() {
        return this.tuning;
    }

    @Override
    public Tone getLowestTone() {
        return this.lowestTone;
    }
}
