package com.garethevans.church.opensongtablet.multitrack;

import java.util.ArrayList;

public class MultiTrackValues {

    // This is an object that holds the values for the multitrack player for this song
    // Each track is saved as an item in an arrayList
    // It gets saved when closing the player

    private ArrayList<AudioTrackValues> audioTrackValues = new ArrayList<>();

    public void setAudioTrackValues(ArrayList<AudioTrackValues> audioTrackValues) {
        this.audioTrackValues = audioTrackValues;
    }

    public ArrayList<AudioTrackValues> getAudioTrackValues() {
        return audioTrackValues;
    }
}
