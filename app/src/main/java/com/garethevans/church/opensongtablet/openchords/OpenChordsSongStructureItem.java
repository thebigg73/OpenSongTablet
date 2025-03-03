package com.garethevans.church.opensongtablet.openchords;

public class OpenChordsSongStructureItem {

    // This contains information about the song structure (Presentation order)

    private String instructionBefore;   // Unlikely to use in OpenSongApp
    private String instructionAfter;    // Unlikely to use in OpenSongApp
    private String sectionName;         // This will be like 'Verse 1'

    public String getInstructionBefore() {
        return instructionBefore;
    }
    public String getInstructionAfter() {
        return instructionAfter;
    }
    public String getSectionName() {
        return sectionName;
    }
    public void setInstructionBefore(String instructionBefore) {
        this.instructionBefore = instructionBefore;
    }
    public void setInstructionAfter(String instructionAfter) {
        this.instructionAfter = instructionAfter;
    }
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
}
