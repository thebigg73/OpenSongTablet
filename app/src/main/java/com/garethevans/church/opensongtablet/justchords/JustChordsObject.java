package com.garethevans.church.opensongtablet.justchords;

public class JustChordsObject {

    // This is an object for a JustChords object (set/songs)

    // Supported JustChords fields
    String[] playlists;             // TODO likely set info?
    String dataVersion;             // TODO likely the set UUID?
    String identity;                // TODO likely the original id of the song?
    JustChordsSongObject[] songs;   // TODO likely the array of songs in the set?
    String[] tags;                  // TODO likely tags for the set?

    // The getters
    public String[] getPlaylists() {
        return emptyOrValues(playlists);
    }
    public String getDataVersion() {
        return emptyOrValue(dataVersion);
    }
    public String getIdentity() {
        return emptyOrValue(identity);
    }
    public JustChordsSongObject[] getSongs() {
        return emptyOrSongValues(songs);
    }
    public String[] getTags() {
        return emptyOrValues(tags);
    }

    // The setters
    public void setPlaylists(String[] playlists) {
        this.playlists = emptyOrValues(playlists);
    }
    public void setDataVersion(String dataVersion) {
        this.dataVersion = emptyOrValue(dataVersion);
    }
    public void setIdentity(String identity) {
        this.identity = emptyOrValue(identity);
    }
    public void setSongs(JustChordsSongObject[] songs) {
        this.songs = new JustChordsSongObject[songs.length];
        System.arraycopy(songs, 0, this.songs, 0, songs.length);
        this.songs = emptyOrSongValues(songs);
    }
    public void setTags(String[] tags) {
        this.tags = emptyOrValues(tags);
    }


    public String emptyOrValue(String value) {
        return value==null ? "":value;
    }

    public String[] emptyOrValues(String[] values) {
        return values==null ? new String[0]: values;
    }

    public JustChordsSongObject[] emptyOrSongValues(JustChordsSongObject[] values) {
        return values==null ? new JustChordsSongObject[0]: values;
    }
}