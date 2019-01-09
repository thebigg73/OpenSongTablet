package com.garethevans.church.opensongtablet.core.property.listener;

import com.garethevans.church.opensongtablet.core.property.Property;

/**
 * Implementation of {@link PropertyChangeListenerAdapter} for no registered {@link PropertyChangeListener}.
 */
public class PropertyChangeListenerAdapterEmpty<V> extends PropertyChangeListenerAdapter<V> {

    private static final PropertyChangeListenerAdapterEmpty INSTANCE = new PropertyChangeListenerAdapterEmpty();

    @Override
    public PropertyChangeListenerAdapter<V> addListener(PropertyChangeListener<? super V> listener, Property<V> property) {
        return new PropertyChangeListenerAdapterSingle<>(listener, property);
    }

    @Override
    public PropertyChangeListenerAdapter<V> removeListener(PropertyChangeListener<? super V> listener) {
        return this;
    }

    @Override
    public void fireChange() {
        // nothing to do
    }

    /**
     * @param <V> the type of the value.
     * @return the singleton instance of {@link PropertyChangeListenerAdapterEmpty}.
     */
    public static <V> PropertyChangeListenerAdapterEmpty<V> get() {
        return INSTANCE;
    }
}
