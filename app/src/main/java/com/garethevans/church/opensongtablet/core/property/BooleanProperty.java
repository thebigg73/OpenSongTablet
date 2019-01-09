package com.garethevans.church.opensongtablet.core.property;

public class BooleanProperty extends AbstractProperty<Boolean> {

    private Boolean value;

    public BooleanProperty(String name) {
        this(name, null);
    }

    public BooleanProperty(String name, Boolean value) {
        super(name);
        this.value = value;
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    public Boolean getValueOrDefault() {
        if (this.value == null) {
            return Boolean.FALSE;
        }
        return this.value;
    }

    public boolean get() {
        if (this.value == null) {
            return false;
        } else {
            return this.value.booleanValue();
        }
    }


    public void set(boolean value) {
        setValue(Boolean.valueOf(value));
    }

    @Override
    protected void doSetValue(Boolean newValue) {
        this.value = newValue;
    }

    public void toggle() {
        set(!get());
    }

    @Override
    protected Boolean parseValue(String value) {
        return Boolean.parseBoolean(value);
    }

}
