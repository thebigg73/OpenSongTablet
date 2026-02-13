package com.garethevans.church.opensongtablet.drummer;

import java.util.HashMap;
import java.util.Map;

public class DrumPattern {
    public String name = "New Pattern";
    public int steps;
    // We make this public for Gson, but provide a helper for the logic
    public Map<String, boolean[]> tracks = new HashMap<>();

    // Default constructor for Gson
    public DrumPattern() {}

    public DrumPattern(int steps) {
        this.steps = steps;
        initializeTracks();
    }

    public void initializeTracks() {
        if (!tracks.containsKey("Kick")) tracks.put("Kick", new boolean[steps]);
        if (!tracks.containsKey("Snare")) tracks.put("Snare", new boolean[steps]);
        if (!tracks.containsKey("HiHat")) tracks.put("HiHat", new boolean[steps]);
    }

    /**
     * Safety helper to get a track. If it doesn't exist, it creates it
     * to avoid NullPointerExceptions in the ViewModel.
     */
    public boolean[] getTrack(String name) {
        if (!tracks.containsKey(name)) {
            tracks.put(name, new boolean[steps]);
        }
        return tracks.get(name);
    }

    public int getSteps() {
        return steps;
    }
}