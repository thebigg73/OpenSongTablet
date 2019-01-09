package com.garethevans.church.opensongtablet.core.music.tone;

import java.util.Locale;

public enum ToneNameCase {

    LOWER_CASE,

    CAPITAL_CASE;

    public String convert(String name) {
        if ((name == null) || name.isEmpty())  {
            return name;
        }
        switch (this) {
            case LOWER_CASE:
                return name.toLowerCase(Locale.US);
            case CAPITAL_CASE:
                return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase(Locale.US);
        }
        return name;
    }
}
