package com.garethevans.church.opensongtablet.core.property;

import com.garethevans.church.opensongtablet.core.format.SongFormat;
import com.garethevans.church.opensongtablet.core.format.SongFormatChordPro;

public class SongFormatProperty extends AbstractProperty<SongFormat> {

    private SongFormat value;

    public SongFormatProperty(String name) {

        this(name, SongFormatChordPro.INSTANCE);
    }

    public SongFormatProperty(String name, SongFormat value) {

        super(name);
        this.value = value;
    }

    @Override
    public Class<SongFormat> getType() {
        return SongFormat.class;
    }

    @Override
    public SongFormat getValue() {
        return this.value;
    }

    @Override
    protected void doSetValue(SongFormat newValue) {
        this.value = newValue;
    }

    @Override
    protected SongFormat parseValue(String value) {
        return SongFormat.get(value);
    }
}
