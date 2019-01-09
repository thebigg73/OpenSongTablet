package com.garethevans.church.opensongtablet.core.property;

public interface PropertyAccessor<B, T> {

    /**
     * @param bean the Java bean containing the requested property.
     * @return the value of the property. May be {@code null}.
     */
    T get(B bean);

    /**
     * @param bean the Java bean containing the property to set.
     * @param value the new value of the property. May be {@code null}.
     */
    void set(B bean, T value);

    /**
     * @return the name of the property.
     */
    @Override
    String toString();

}
