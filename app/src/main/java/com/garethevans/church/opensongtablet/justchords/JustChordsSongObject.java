package com.garethevans.church.opensongtablet.justchords;

import java.util.Map;

public class JustChordsSongObject {

    // This holds the compatible information for a JustChords song file

    String title;                   // The title of the song matching song.getTitle()
    String artist;                  // The artist of the song matching song.getAuthor()
    String duration;                // The song duration.  Similar to song.getAutoscrollLength() but formatted as m:ss instead of s
    String tempo;                   // The song tempo matching song.getTempo()
    String notes;                   // The song notes matching song.getNotes()
    String copyright;               // The song copyright matching song.getCopyright()
    String timeSignature;           // The song time signature matching song.getTimeSig()
    String ccli;                    // The song ccli matching song.getCcli()
    Map<String,Object> keyChord;    // The song key as a major key with a minor boolean based retrieved from song.getKey()
    String rawData;                 // The song lyrics based on song.getLyrics().  Formatted as ChordPro with heading lines ending with :
    String id;                      // A UUID field for the song

    // Unsupported (but required) JustChords fields
    String[] chordBeatsRaw;         // A non-used by me, but apparently required field

    // The setters
    public void setTitle(String title) {
        // Identical to OpenSong title
        this.title = title;
    }
    public void setArtist(String artist) {
        // Identical to OpenSong author
        this.artist = artist;
    }
    public void setDuration(String duration) {
        // Similar to OpenSong autoscrolllength but formatted as m:ss, not just s
        this.duration = duration;
    }
    public void setTempo(String tempo) {
        // Identical to OpenSong tempo in bpm
        this.tempo = tempo;
    }
    public void setNotes(String notes) {
        // Identical to OpenSong notes
        this.notes = notes;
    }
    public void setCopyright(String copyright) {
        // Identical to OpenSong copyright
        this.copyright = copyright;
    }
    public void setTimeSignature(String timeSignature) {
        // Identical to OpenSong timesig - might be escaped e.g. 4\/4
        // Must be set non-empty.  Default to 4/4
        if (timeSignature==null || timeSignature.isEmpty()) {
            timeSignature = "4/4";
        }
        this.timeSignature = timeSignature;
    }
    public void setCcli(String ccli) {
        // Identical to OpenSong ccli
        this.ccli = ccli;
    }
    public void setKeyChord(Map<String,Object> keyChord) {
        // A Map containing the keys "key" (a string) and "minor" (a boolean).  Note the "key" is the major key text and "minor" means change to minor
        this.keyChord = keyChord;
    }
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setChordBeatsRaw(String[] chordBeatsRaw) {
        this.chordBeatsRaw = emptyOrValues(chordBeatsRaw);
    }

    // The getters
    public String getTitle() {
        return emptyOrValue(title);
    }
    public String getArtist() {
        return emptyOrValue(artist);
    }
    public String getDuration() {
        return emptyOrValue(duration);
    }
    public String getTempo() {
        return emptyOrValue(tempo);
    }
    public String getNotes() {
        return emptyOrValue(notes);
    }
    public String getCopyright() {
        return emptyOrValue(copyright);
    }
    public String getTimeSignature() {
        // Must be set non-empty.  Default to 4/4
        return emptyOrValue(timeSignature).isEmpty() ? "4/4" : timeSignature;
    }
    public String getCcli() {
        return emptyOrValue(ccli);
    }
    public Map<String,Object> getKeyChord() {
        if (keyChord!=null) {
            if (!keyChord.containsKey("key")) {
                keyChord.put("key", "");
            }
            if (!keyChord.containsKey("minor")) {
                keyChord.put("minor", false);
            }
        }
        return keyChord;
    }
    public String getRawData() {
        return emptyOrValue(rawData);
    }
    public String getId() {
        return emptyOrValue(id);
    }
    public String[] getChordBeatsRaw() {
        return emptyOrValues(chordBeatsRaw);
    }

    private String emptyOrValue(String value) {
        return value == null ? "" : value;
    }
    private String[] emptyOrValues(String[] values) {
        return values == null ? new String[0]:values;
    }

}
