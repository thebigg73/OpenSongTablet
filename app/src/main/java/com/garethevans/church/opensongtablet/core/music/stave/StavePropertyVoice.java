package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.music.rythm.Beat;

import java.util.List;

class StavePropertyVoice extends StaveProperty<StaveVoice> {

    public static final StavePropertyVoice INSTANCE = new StavePropertyVoice();

    public StavePropertyVoice() {
        super("voice");
    }

    @Override
    public StaveVoice get(Stave stave) {
        if (stave == null) {
            return null;
        }
        List<StaveVoice> voices = stave.getVoices();
        int size = voices.size();
        if (size == 0) {
            return null;
        }
        return voices.get(size - 1);
    }

    @Override
    public void set(Stave stave, StaveVoice voice) {
        if (stave != null) {
            stave.addVoice(voice);
        }
    }
}
