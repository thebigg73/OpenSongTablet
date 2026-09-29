package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.SongProvider; // Make sure this matches your interface name

public class SongProviderFactory {
    public static SongProvider getProvider(String providerName, Context context) {
        switch (providerName) {
            case "UltimateGuitar": return new UltimateGuitarProvider(context);
            case "Chordie": return new ChordieProvider(context);
            case "UkuTabs": return new UkuTabsProvider(context);
            case "eChords": return new EChordsProvider(context);
            case "HolyChords": return new HolyChordsProvider(context);
            case "La Boîte à chansons": return new BoiteachansonsProvider(context);
            case "WorshipTogether": return new WorshipTogetherProvider(context);
            default: throw new IllegalArgumentException("Unknown provider: " + providerName);
        }
    }
}