package com.garethevans.church.opensongtablet.core.property;

import com.garethevans.church.opensongtablet.core.bean.Bean;

public class BeanProperty<V extends Bean> extends AbstractProperty<V> {

    private final Class<V> type;

    private final V value;

    @SuppressWarnings("unchecked")
    public BeanProperty(String name, V value) {
        this(name, (Class) value.getClass(), value);
    }

    public BeanProperty(String name, Class<V> type, V value) {
        super(name);
        this.value = value;
        this.type = type;
    }

    @Override
    public Class<V> getType() {
        return this.type;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected void doSetValue(V newValue) {
        throw new IllegalStateException();
    }

    @Override
    protected V parseValue(String string) {
        throw new IllegalStateException();
    }
}
