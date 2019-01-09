package com.garethevans.church.opensongtablet.core.property;

public class FloatProperty extends AbstractProperty<Float> {

    private Float value;

    public FloatProperty(String name) {
        this(name, null);
    }

    public FloatProperty(String name, Float value) {
        super(name);
        this.value = value;
    }

    @Override
    public Class<Float> getType() {
        return Float.class;
    }

    @Override
    public Float getValue() {
        return this.value;
    }

    @Override
    public Float getValueOrDefault() {
        if (this.value == null) {
            return Float.valueOf(0);
        }
        return this.value;
    }

    public float get() {
        if (this.value == null) {
            return 0;
        } else {
            return this.value.floatValue();
        }
    }

    @Override
    protected void doSetValue(Float newValue) {
        this.value = newValue;
    }

    public void set(float value) {
        setValue(Float.valueOf(value));
    }

    @Override
    protected Float parseValue(String value) {
        return Float.parseFloat(value);
    }

}
