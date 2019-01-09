package com.garethevans.church.opensongtablet.core.property.listener;

import com.garethevans.church.opensongtablet.core.ObjectHelper;
import com.garethevans.church.opensongtablet.core.property.Property;

/**
 * Implementation of {@link PropertyChangeListenerAdapter} for a single registered {@link PropertyChangeListener}.
 */
public class PropertyChangeListenerAdapterSingle<V> extends PropertyChangeListenerAdapter<V> {

    private final PropertyChangeListener<? super V> listener;

    private final Property<V> property;

    private V value;

    public PropertyChangeListenerAdapterSingle(PropertyChangeListener<? super V> listener, Property<V> property) {
        super();
        this.listener = listener;
        this.property = property;
        this.value = property.getValue();
    }

    @Override
    public PropertyChangeListenerAdapter<V> addListener(PropertyChangeListener<? super V> listener, Property<V> property) {
        return new PropertyChangeListenerAdapterMultiple<>(this.listener, listener, this.property);
    }

    @Override
    public PropertyChangeListenerAdapter<V> removeListener(PropertyChangeListener<? super V> listener) {
        if (listener.equals(this.listener)) {
            return PropertyChangeListenerAdapterEmpty.get();
        }
        return this;
    }

    public void fireChange() {

        final V oldValue = this.value;
        this.value = this.property.getValue();
        if (!ObjectHelper.equals(this.value, oldValue)) {
            try {
                this.listener.onChange(this.property, oldValue, this.value);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }
}
