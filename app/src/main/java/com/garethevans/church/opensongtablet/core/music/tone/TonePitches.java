package com.garethevans.church.opensongtablet.core.music.tone;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Access to {@link #of(String) parse} a {@link TonePitch} by {@link TonePitch#getName() name} for
 * all available {@link ToneNameStyle}s.
 */
public class TonePitches {

    private final Map<String, TonePitch> name2pitchMap;

    private static final TonePitches INSTANCE = new TonePitches();

    private TonePitches() {
        super();
        this.name2pitchMap = new HashMap<>();
        register(TonePitchEnglish.STYLE);
        register(TonePitchInternational.STYLE);
        register(TonePitchGerman.STYLE);
        register(TonePitchDutch.STYLE);
        register(TonePitchNeoLatin.STYLE);
        register(TonePitchNeoLatinAsciiSigns.STYLE);
        register(TonePitchNeoLatinUnicodeSigns.STYLE);
    }

    private void register(ToneNameStyle<?> style) {
        for (TonePitch pitch : style.values()) {
            //this.name2pitchMap.putIfAbsent(pitch.getName(), pitch);
            String name = pitch.getName();
            if (!this.name2pitchMap.containsKey(name)) {
                this.name2pitchMap.put(name, pitch);
            }
        }
    }

    private TonePitch valueOf(String name) {
        return this.name2pitchMap.get(name);
    }

    public static TonePitch of(String name) {
        TonePitch pitch = INSTANCE.valueOf(name);
        if (pitch == null) {
            char first = name.charAt(0);
            String decapitalized = first + name.substring(1).toLowerCase(Locale.US);
            pitch = INSTANCE.valueOf(decapitalized);
        }
        return pitch;
    }
}
