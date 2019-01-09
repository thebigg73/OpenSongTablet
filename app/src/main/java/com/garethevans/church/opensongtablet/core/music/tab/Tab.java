package com.garethevans.church.opensongtablet.core.music.tab;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.instrument.string.FrettedStringInstrument;
import com.garethevans.church.opensongtablet.core.music.instrument.string.StringInstrument;
import com.garethevans.church.opensongtablet.core.music.stave.Stave;
import com.garethevans.church.opensongtablet.core.music.tone.ToneNameStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A tablature for a string based instrument like Guitar or Ukulele. Unlike a {@link Stave} that is
 * univeral for each kind of instrument, a {@link Tab} shows a line for each {@link TabString string}
 * with {@link TabNote}s showing the numbers of the {@link TabNote#getFret() fret} to play on the
 * according string.
 */
public class Tab {

    private final int maxFret;

    private final List<TabString> strings;

    private Tab(int maxFret) {
        super();
        this.maxFret = maxFret;
        this.strings = new ArrayList<>();
    }

    private Tab(int maxFret, List<TabString> strings) {
        super();
        this.maxFret = maxFret;
        this.strings = strings;
    }

    public List<TabString> getStrings() {
        return this.strings;
    }

    /**
     * @return the maximum fret number.
     * @see FrettedStringInstrument#getMaxFret()
     */
    public int getMaxFret() {
        return this.maxFret;
    }

    public void format(StringBuilder sb, ToneNameStyle style) {

    }

    public static Tab of(StringInstrument instrument) {
        List<Tone> tuning = instrument.getTuning();
        List<TabString> strings = new ArrayList<>(tuning.size());
        for (Tone tone : tuning) {
            TabString string = new TabString(tone);
            strings.add(string);
        }
        int maxFret = 16;
        if (instrument instanceof FrettedStringInstrument) {
            maxFret = ((FrettedStringInstrument) instrument).getMaxFret();
        }
        return new Tab(maxFret, Collections.unmodifiableList(strings));
    }

}
