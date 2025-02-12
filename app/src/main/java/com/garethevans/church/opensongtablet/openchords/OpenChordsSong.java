package com.garethevans.church.opensongtablet.openchords;

public class OpenChordsSong {

    // This is the OpenChords song object
    // These are used in the obvious OpenChords class files
    // Also called in CommonSQL.openChordsSyncGetSongsFromFolder()

    // Matches v1.0.7
    private String id;              // UUID 4 id, e.g. 93E5D3EF-8B1E-4B35-851E-16A0859315E3
    private String title;           // The song title
    private String rawData;         // The actual ChordPro song formatted text
    private String artist;          // The song artist
    private String duration;        // The song duration in ordinal ISO 8601 format. e.g. 268 (seconds) and 4:28 (mins:secs) both work
    private int tempo;              // The tempo of the song in bpm
    private String timeSignature;   // The time signature of the song e.g. 4/4
    private String key;             // The key of the song e.g. A, G#, Ab
    private boolean keyIsMinor;     // If the key is a minor key
    private int capo;               // The capo fret to use
    private String transpose;       // Transposed to a different key e.g. A, G#, Ab
    private String notes;           // Notes to save with the song
    private String copyright;       // The copyright information
    private String ccli;            // The CCLI information
    private String lastUpdated;     // The last time the song was updated ISO 8601 format e.g. 2025-01-14T07:17:02Z
    private String[] tags;          // The tags for the song.  These are UUIDs that get looked up

    // The getters
    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getRawData() {
        return rawData;
    }
    public String getArtist() {
        return artist;
    }
    public String getDuration() {
        return duration;
    }
    public int getTempo() {
        return tempo;
    }
    public String getTimeSignature() {
        return timeSignature;
    }
    public String getKey() {
        return key;
    }
    public boolean isKeyIsMinor() {
        return keyIsMinor;
    }
    public int getCapo() {
        return capo;
    }
    public String getTranspose() {
        return transpose;
    }
    public String getNotes() {
        return notes;
    }
    public String getCopyright() {
        return copyright;
    }
    public String getCcli() {
        return ccli;
    }
    public String getLastUpdated() {
        return lastUpdated;
    }
    public String[] getTags() {
        return tags;
    }

    // The setters
    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }
    public void setTempo(int tempo) {
        this.tempo = tempo;
    }
    public void setTimeSignature(String timeSignature) {
        this.timeSignature = timeSignature;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setKeyIsMinor(boolean keyIsMinor) {
        this.keyIsMinor = keyIsMinor;
    }
    public void setCapo(int capo) {
        this.capo = capo;
    }
    public void setTranspose(String transpose) {
        this.transpose = transpose;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    public void setCcli(String ccli) {
        this.ccli = ccli;
    }
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public void setTags(String[] tags) {
        this.tags = getTags();
    }

}
