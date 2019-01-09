package com.garethevans.church.opensongtablet.core.property.listener;

import com.garethevans.church.opensongtablet.core.ObjectHelper;
import com.garethevans.church.opensongtablet.core.property.Property;

import java.util.Arrays;

/**
 * Implementation of {@link PropertyChangeListenerAdapter} for multiple registered {@link PropertyChangeListener}s.
 */
public class PropertyChangeListenerAdapterMultiple<V> extends PropertyChangeListenerAdapter<V> {

    private final Property<V> property;

    private PropertyChangeListener<? super V>[] listeners;

    private int listenerSize;

    private boolean locked;

    private V value;

    public PropertyChangeListenerAdapterMultiple(PropertyChangeListener<? super V> listener1, PropertyChangeListener<? super V> listener2, Property<V> property) {
        super();
        this.listeners = new PropertyChangeListener[]{listener1, listener2};
        this.listenerSize = 2;
        this.property = property;
        this.value = property.getValue();
    }

    @Override
    public PropertyChangeListenerAdapter<V> addListener(PropertyChangeListener<? super V> listener, Property<V> property) {
        final int oldCapacity = this.listeners.length;
        if ((this.locked) || (this.listenerSize == oldCapacity)) {
            final int newCapacity = newCapacity(this.listenerSize, this.listeners.length);
            this.listeners = Arrays.copyOf(this.listeners, newCapacity);
        }
        this.listeners[this.listenerSize++] = listener;
        return this;
    }

    private int newCapacity(int size, int oldCapacity) {
        return (size < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
    }

    @Override
    public PropertyChangeListenerAdapter<V> removeListener(PropertyChangeListener<? super V> listener) {
        for (int i = 0; i < this.listenerSize; i++) {
            if (listener.equals(this.listeners[i])) {
                return removeListener(i);
            }
        }
        return this;
    }

    private PropertyChangeListenerAdapter<V> removeListener(int index) {

        if (this.listenerSize == 2) {
            return new PropertyChangeListenerAdapterSingle<>(this.listeners[1 - index], this.property);
        } else {
            final int moveCount = this.listenerSize - index - 1;
            final PropertyChangeListener<? super V>[] oldListeners = this.listeners;
            if (this.locked) {
                this.listeners = new PropertyChangeListener[this.listeners.length];
                System.arraycopy(oldListeners, 0, this.listeners, 0, index);
            }
            if (moveCount > 0) {
                System.arraycopy(oldListeners, index + 1, this.listeners, index, moveCount);
            }
            this.listenerSize--;
            if (!this.locked) {
                this.listeners[this.listenerSize] = null;
            }
        }
        return this;
    }

    public void fireChange() {

        final int currentListenerCount = this.listenerSize;
        if (currentListenerCount == 0) {
            return;
        }
        final PropertyChangeListener<? super V>[] currentListeners = this.listeners;
        try {
            this.locked = true;
            final V oldValue = this.value;
            this.value = this.property.getValue();
            if (!ObjectHelper.equals(this.value, oldValue)) {
                for (int i = 0; i < currentListenerCount; i++) {
                    try {
                        currentListeners[i].onChange(this.property, oldValue, this.value);
                    } catch (Exception e) {
                        Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                    }
                }
            }
        } finally {
            this.locked = false;
        }
    }
}
