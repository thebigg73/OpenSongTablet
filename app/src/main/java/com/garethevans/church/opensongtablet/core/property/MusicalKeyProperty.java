package com.garethevans.church.opensongtablet.core.property;

import com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey;

public class MusicalKeyProperty extends AbstractProperty<MusicalKey> {

    private MusicalKey value;

    public MusicalKeyProperty(String name) {

        this(name, null);
    }

    public MusicalKeyProperty(String name, MusicalKey value) {

        super(name);
        this.value = value;
    }

    @Override
    public Class<MusicalKey> getType() {
        return MusicalKey.class;
    }

    @Override
    public MusicalKey getValue() {
        return this.value;
    }

    @Override
    protected void doSetValue(MusicalKey newValue) {
        this.value = newValue;
    }

    @Override
    protected MusicalKey parseValue(String value) {
        return MusicalKey.fromName(value);
    }
}
