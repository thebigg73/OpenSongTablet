package com.garethevans.church.opensongtablet.core.music.tone;

import java.util.Map;

public abstract class ToneNameStyleEuropean<P extends TonePitch> extends ToneNameStyle<P> {

    @Override
    public String getSingleSharpSign() {
        return "is";
    }

    @Override
    public String getDoubleSharpSign() {
        return "isis";
    }

    @Override
    public String getSingleFlatSign() {
        return "es";
    }

    @Override
    public String getDoubleFlatSign() {
        return "eses";
    }

    public String getNeutralSign() {
        return "";
    }

    @Override
    protected Map<String, EnharmonicType> createSignsMap() {
        Map<String, EnharmonicType> map = super.createSignsMap();
        map.put("s", EnharmonicType.SINGLE_FLAT);
        map.put("ses", EnharmonicType.DOUBLE_FLAT);
        return map;
    }
}
