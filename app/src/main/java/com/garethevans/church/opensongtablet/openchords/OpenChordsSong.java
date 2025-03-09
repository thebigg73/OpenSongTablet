package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class OpenChordsSong {

    // This is the OpenChords song object
    // These are used in the obvious OpenChords class files
    // Also called in CommonSQL.openChordsSyncGetSongsFromFolder()

    // Matches v1.0.7
    @Nullable private String id;              // UUID 4 id, e.g. 93E5D3EF-8B1E-4B35-851E-16A0859315E3
    @Nullable private String title;           // The song title
    @Nullable private String rawData;         // The actual ChordPro song formatted text
    @Nullable private String artist;          // The song artist
    @Nullable private String duration;        // The song duration in ordinal ISO 8601 format. e.g. 268 (seconds) and 4:28 (mins:secs) both work
    @Nullable private Integer tempo;          // The tempo of the song in bpm
    @Nullable private String timeSignature;   // The time signature of the song e.g. 4/4
    @Nullable private String key;             // The key of the song e.g. A, G#, Ab
    @Nullable private Boolean keyIsMinor;     // If the key is a minor key
    //@Nullable private Integer capo;               // The capo fret to use
    @Nullable private String transpose;       // Transposed to a different key e.g. A, G#, Ab
    @Nullable private String notes;           // Notes to save with the song
    @Nullable private String copyright;       // The copyright information
    @Nullable private String ccli;            // The CCLI information
    @Nullable private String lastUpdated;     // The last time the song was updated ISO 8601 format e.g. 2025-01-14T07:17:02Z
    @Nullable private String[] tags;          // The tags for the song.  These are UUIDs that get looked up
    @Nullable private ArrayList<OpenChordsSongStructureItem> structure;     // Any song structure/presentation order in OpenSongApp

    // The getters
    @Nullable public String getId() {
        return id;
    }
    @Nullable public String getTitle() {
        return title;
    }
    @Nullable public String getRawData() {
        return rawData;
    }
    @Nullable public String getArtist() {
        return artist;
    }
    @Nullable public String getDuration() {
        return duration;
    }
    @Nullable public Integer getTempo() {
        return tempo;
    }
    @Nullable public String getTimeSignature() {
        return timeSignature;
    }
    @Nullable public String getKey() {
        return key;
    }
    @Nullable public Boolean isKeyIsMinor() {
        return keyIsMinor;
    }
    //@Nullable public Integer getCapo() {
    //    return capo;
    //}
    @Nullable public String getTranspose() {
        return transpose;
    }
    @Nullable public String getNotes() {
        return notes;
    }
    @Nullable public String getCopyright() {
        return copyright;
    }
    @Nullable public String getCcli() {
        return ccli;
    }
    @Nullable public String getLastUpdated() {
        return lastUpdated;
    }
    @Nullable public String[] getTags() {
        return tags;
    }
    @Nullable public ArrayList<OpenChordsSongStructureItem> getStructure() {
        return structure;
    }

    // The setters
    public void setId(@Nullable String id) {
        this.id = id;
    }
    public void setTitle(@Nullable String title) {
        this.title = title;
    }
    public void setRawData(@Nullable String rawData) {
        this.rawData = rawData;
    }
    public void setArtist(@Nullable String artist) {
        this.artist = artist;
    }
    public void setDuration(@Nullable String duration) {
        this.duration = duration;
    }
    public void setTempo(@Nullable Integer tempo) {
        this.tempo = tempo;
    }
    public void setTimeSignature(@Nullable String timeSignature) {
        this.timeSignature = timeSignature;
    }
    public void setKey(@Nullable String key) {
        this.key = key;
    }
    public void setKeyIsMinor(@Nullable Boolean keyIsMinor) {
        this.keyIsMinor = keyIsMinor;
    }
    //public void setCapo(@Nullable Integer capo) {
    //    this.capo = capo;
    //}
    public void setTranspose(@Nullable String transpose) {
        this.transpose = transpose;
    }
    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }
    public void setCopyright(@Nullable String copyright) {
        this.copyright = copyright;
    }
    public void setCcli(@Nullable String ccli) {
        this.ccli = ccli;
    }
    public void setLastUpdated(@Nullable String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public void setTags(@Nullable String[] tags) {
        this.tags = getTags();
    }
    public void setStructure(@Nullable ArrayList<OpenChordsSongStructureItem> structure) {
        this.structure = structure;
    }
}
