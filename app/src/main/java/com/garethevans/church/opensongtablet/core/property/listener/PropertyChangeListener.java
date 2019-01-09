package com.garethevans.church.opensongtablet.core.property.listener;

import com.garethevans.church.opensongtablet.core.property.Property;

public interface PropertyChangeListener<V> {

    /**
     * If the {@link Property#getValue() value} of a {@link Property} changed, this method is called for
     * all {@link Property#addListener(PropertyChangeListener) registered} {@link PropertyChangeListener}s.
     * <p>
     * In general is is considered bad practice to modify the observed value in this method.
     *
     * @param property the {@link Property} that changed its {@link Property#getValue() value}.
     * @param oldValue the old value.
     * @param newValue the new value.
     */
    void onChange(Property<? extends V> property, V oldValue, V newValue);
}
