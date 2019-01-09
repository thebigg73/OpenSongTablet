package com.garethevans.church.opensongtablet.core.property;

import com.garethevans.church.opensongtablet.core.property.listener.PropertyChangeListener;

/**
 * Generic container for a configuration value.
 *
 * @param <V> type of the contained value.
 */
public interface Property<V> {

    /**
     * @return the {@link Class} reflecting the type of the {@link Property} {@link #getValue() value}.
     */
    Class<V> getType();

    /**
     * @return the name of this property. May only contain alphanumeric ASCII characters and should be in CamlCase.
     */
    String getName();

    /**
     * @return the current value of this property.
     */
    V getValue();

    /**
     * @return the {@link #getValue() value} or a default value if not set.
     */
    V getValueOrDefault();

    /**
     * @param value the new value.
     */
    void setValue(V value);

    /**
     * @param value the new value.
     */
    void setValueAsString(String value);

    /**
     * @return {@code true} if this property is read-only and {@link #setValue(Object)} will fail with an exception,
     *         {@code false} otherwise.
     */
    boolean isReadOnly();

    /**
     * Adds a {@link PropertyChangeListener} which will be notified whenever the value of the {@code Property} changes.
     * If the same listener is added more than once, then it will be notified more than once.
     * Also the same {@link PropertyChangeListener} instance may be registered for different {@link Property properties}.
     * <p>
     * The {@code Property} stores a strong reference to the listener which will prevent the listener
     * from being garbage collected and may result in a memory leak. It is recommended to unregister
     * a listener by calling {@link #removeListener(PropertyChangeListener)} after use.
     *
     * @see #removeListener(PropertyChangeListener)
     *
     * @param listener the {@link PropertyChangeListener} to register.
     */
    void addListener(PropertyChangeListener<? super V> listener);

    /**
     * Removes the given {@link PropertyChangeListener} from the list of {@link #addListener(PropertyChangeListener)
     * registered} listeners.
     * <p>
     * If the given listener has not been previously {@link #addListener(PropertyChangeListener) registered}
     * this method will have no effect. Otherwise it will be removed. If it had been added more than once,
     * then only the first occurrence will be removed.
     *
     * @see #addListener(PropertyChangeListener)
     *
     * @param listener the {@link PropertyChangeListener} to remove.
     */
    void removeListener(PropertyChangeListener<? super V> listener);
}
