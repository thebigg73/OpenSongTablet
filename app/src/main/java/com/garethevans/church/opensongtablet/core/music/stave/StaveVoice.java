package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.ObjectHelper;
import com.garethevans.church.opensongtablet.core.format.FormatConstants;
import com.garethevans.church.opensongtablet.core.music.instrument.Instrument;

public class StaveVoice {

    public static final StaveVoice SOPRANO = new StaveVoice("Soprano", "S");

    public static final StaveVoice ALTO = new StaveVoice("Alto", "A");

    public static final StaveVoice TENOR = new StaveVoice("Tenor", "T");

    public static final StaveVoice BASS = new StaveVoice("Bass", "B");

    private final Instrument instrument;

    private final String name;

    private final String abbreviation;

    public StaveVoice(String name) {
        this(name, null, null);
    }

    public StaveVoice(String name, String abbreviation) {
        this(name, abbreviation, null);
    }

    public StaveVoice(String name, String abbreviation, Instrument instrument) {
        super();
        this.name = name;
        this.abbreviation = abbreviation;
        this.instrument = instrument;
    }

    public String getName() {
        return this.name;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    /**
     * @return the {@link Instrument} used to play this {@link StaveVoice}. May be {@code null} for undefined
     *         or human singing voice.
     */
    public Instrument getInstrument() {
        return this.instrument;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if ((obj == null) || (obj.getClass() != getClass())) {
            return false;
        }
        StaveVoice other = (StaveVoice) obj;
        if (!ObjectHelper.equals(this.name, other.name)) {
            return false;
        }
        if (!ObjectHelper.equals(this.abbreviation, other.abbreviation)) {
            return false;
        }
        if (!ObjectHelper.equals(this.instrument, other.instrument)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.name);
        if (this.abbreviation != null) {
            sb.append(FormatConstants.VOICE_SEPARATOR);
            sb.append(this.abbreviation);
        }
        if (this.instrument != null) {
            if (this.abbreviation == null) {
                sb.append(FormatConstants.VOICE_SEPARATOR);
            }
            sb.append(FormatConstants.VOICE_SEPARATOR);
            sb.append(this.instrument);
        }
        return sb.toString();
    }
}
