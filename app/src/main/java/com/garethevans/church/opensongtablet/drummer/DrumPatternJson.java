package com.garethevans.church.opensongtablet.drummer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DrumPatternJson {
    private String name;
    private int beats;
    private int divisions;
    // Key: Drum Name (e.g. "Kick"), Value: array of 16th note triggers
    // The three variations for this specific beat
    private Map<String, int[]> mainPattern = new HashMap<>();
    private Map<String, int[]> variationPattern = new HashMap<>();
    private Map<String, int[]> fillMainPattern = new HashMap<>();
    private Map<String, int[]> fillVariationPattern = new HashMap<>();

    public DrumPatternJson(ArrayList<DrumPart> drumParts, int steps, int beats, int divisions) {
        this.beats = beats;
        this.divisions = divisions;
        initialiseTracks(drumParts, mainPattern, steps);
        initialiseTracks(drumParts, variationPattern, steps);
        initialiseTracks(drumParts, fillMainPattern, steps);
        initialiseTracks(drumParts, fillVariationPattern, steps);
    }

    private void initialiseTracks(ArrayList<DrumPart> drumParts, Map<String,int[]> map, int steps) {
        for (DrumPart drumPart : drumParts) {
            map.put(drumPart.getPartName(), new int[steps]); // Initialized to 0
        }
    }

    // The getters and setters for importing/exporting json via Gson
    public Map<String, int[]> getMainPattern() {
        return mainPattern;
    }
    public Map<String, int[]> getVariationPattern() {
        return variationPattern;
    }
    public Map<String, int[]> getFillMainPattern() {
        return fillMainPattern;
    }
    public Map<String, int[]> getFillVariationPattern() {
        return fillVariationPattern;
    }
    public void setMainPattern(Map<String, int[]> mainPattern) {
        this.mainPattern = mainPattern;
    }
    public void setVariationPattern(Map<String, int[]> variationPattern) {
        this.variationPattern = variationPattern;
    }
    public void setFillMainPattern(Map<String, int[]> fillMainPattern) {
        this.fillMainPattern = fillMainPattern;
    }
    public void setFillVariationPattern(Map<String, int[]> fillVariationPattern) {
        this.fillVariationPattern = fillVariationPattern;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setBeats(int beats) {
        this.beats = beats;
    }
    public int getBeats() {
        return beats;
    }
    public void setDivisions(int divisions) {
        this.divisions = divisions;
    }
    public int getDivisions() {
        return divisions;
    }

}