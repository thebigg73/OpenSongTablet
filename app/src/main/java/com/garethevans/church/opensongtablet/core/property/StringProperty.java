package com.garethevans.church.opensongtablet.core.property;

public class StringProperty extends AbstractProperty<String> {

    private String value;

    public StringProperty(String name) {

        this(name, "");
    }

    public StringProperty(String name, String value) {

        super(name);
        this.value = value;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getValueOrDefault() {
        if (this.value == null) {
            return "";
        }
        return this.value;
    }

    @Override
    protected void doSetValue(String newValue) {
        this.value = newValue;
    }

    @Override
    protected String parseValue(String value) {
        return value;
    }
}
