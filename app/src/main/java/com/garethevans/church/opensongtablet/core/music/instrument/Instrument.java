package com.garethevans.church.opensongtablet.core.music.instrument;

import com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval;
import com.garethevans.church.opensongtablet.core.music.harmony.Chord;
import com.garethevans.church.opensongtablet.core.music.tone.Tone;

public abstract class Instrument {

    public abstract String getName();

    public abstract Tone getLowestTone();

    public boolean isElectric() {
        return false;
    }

    public final boolean isAcoustic() {
        return !isElectric();
    }

    /**
     * @return the {@link ChromaticInterval} indicating the chromatic offset relative to the
     *         {@link com.garethevans.church.opensongtablet.core.music.partiture.Partiture} when playing
     *         {@link Chord}s. E.g. for a special
     *         {@link com.garethevans.church.opensongtablet.core.music.instrument.string.FrettedStringInstrument}
     *         this would be the Capo offset relative to the normal instrument. For instance a
     *         {@link com.garethevans.church.opensongtablet.core.music.instrument.string.Guitalele}
     *         has a chord offset of 7 chromatic steps higher than a regular
     *         {@link com.garethevans.church.opensongtablet.core.music.instrument.string.Guitar}.
     */
    public ChromaticInterval getChordOffset() {
        return ChromaticInterval.PERFECT_UNISON;
    }

    /**
     * @return the {@link ChromaticInterval} indicating the chromatic offset relative to the
     *         {@link com.garethevans.church.opensongtablet.core.music.partiture.Partiture} when playing
     *         {@link Chord}s. E.g.
     */
    public ChromaticInterval getToneOffset() {
        return ChromaticInterval.PERFECT_UNISON;
    }
}
