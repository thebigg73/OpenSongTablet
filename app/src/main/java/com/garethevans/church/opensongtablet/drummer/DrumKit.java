package com.garethevans.church.opensongtablet.drummer;

import java.util.ArrayList;

public class DrumKit {

    // This object holds what we need for the entire kit
    private final String kitName;
    private ArrayList<DrumPart> drumParts;

    /**
     * Builds a Drum kit object that holds information about each part in an array
     * @param kitName The name of the drum kit.  Setting this initialises the object
     */
    public DrumKit(String kitName) {
        this.kitName = kitName;
        drumParts = new ArrayList<>();
    }

    // The setters
    /**
     * @param drumParts An array of DrumPart objects.
     */
    public void setDrumParts(ArrayList<DrumPart> drumParts) {
        this.drumParts = drumParts;
    }

    // The getters
    public String getKitName() {
        return kitName;
    }
    public ArrayList<DrumPart> getDrumParts() {
        return drumParts;
    }
}
