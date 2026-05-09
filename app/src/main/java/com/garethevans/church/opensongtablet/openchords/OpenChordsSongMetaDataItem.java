package com.garethevans.church.opensongtablet.openchords;

public class OpenChordsSongMetaDataItem {

    // This contains metadata for link
    // Only used if they start with http (don't match local files)

    private String youtubeUrl;  // Matched to linkyoutube
    private String audioUrl;    // Matched to linkaudio
    private String spotifyId;   // Matched to linkother

    public String getYoutubeUrl() {
        return youtubeUrl;
    }
    public String getAudioUrl() {
        return audioUrl;
    }
    public String getSpotifyId() {
        return spotifyId;
    }
    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    public void setSpotifyId(String sectionName) {
        this.spotifyId = spotifyId;
    }

}
