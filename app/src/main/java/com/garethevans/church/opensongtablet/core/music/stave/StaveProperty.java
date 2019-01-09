package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.property.PropertyAccessor;

abstract class StaveProperty<T> implements PropertyAccessor<Stave, T> {

    private final String name;

    public StaveProperty(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
