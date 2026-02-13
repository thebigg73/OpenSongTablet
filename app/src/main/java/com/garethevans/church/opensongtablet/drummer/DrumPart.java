package com.garethevans.church.opensongtablet.drummer;

public class DrumPart {

    // This object holds the information for each drum part (kick, snare, etc.)

    private final String partName;
    private final String partFileName;
    private float partVolume;
    private final String partVolPrefName;
    private final int partMidi;
    private final String partTranslation;

    /**
     * Builds a Drum kit part object that holds information about this part
     * @param partName The basic name of the drum part (for part identification in code)
     * @param partTranslation The translated string of the part name (for user view)
     * @param partFileName The filename of the drum part to use from the assets (part_samplerate.wav)
     * @param partVolPrefName The name of the volume preference
     * @param partVolume The volume of this drum part
     * @param partMidi The midi note for this part
     */
    public DrumPart(String partName, String partTranslation, String partFileName, String partVolPrefName,
                    float partVolume, int partMidi) {
        this.partName = partName;
        this.partTranslation = partTranslation;
        this.partFileName = partFileName;
        this.partVolPrefName = partVolPrefName;
        this.partVolume = partVolume;
        this.partMidi = partMidi;
    }

    // The setters
    public void setPartVolume(float partVolume) {
        this.partVolume = partVolume;
    }

    // The getters
    public String getPartName() {
        return partName;
    }
    public String getPartFileName() {
        return partFileName;
    }
    public float getPartVolume() {
        return partVolume;
    }
    public int getPartMidi() {
        return partMidi;
    }
    public String getPartVolPrefName() {
        return partVolPrefName;
    }
    public String getPartTranslation() {
        return partTranslation;
    }
}
