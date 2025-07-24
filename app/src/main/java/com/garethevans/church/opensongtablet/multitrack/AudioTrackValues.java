package com.garethevans.church.opensongtablet.multitrack;

import androidx.annotation.Nullable;

public class AudioTrackValues {

    // This is an object to hold the relevant information for each track
    private Integer trackVolume;
    private Boolean trackMute, trackSolo;
    private String trackName, trackPan;
    private String trackUri;

    public void setTrackVolume(@Nullable Integer trackVolume) {
        this.trackVolume = trackVolume;
    }
    public void setTrackPan(@Nullable String trackPan) {
        this.trackPan = trackPan;
    }
    public void setTrackMute(@Nullable Boolean trackMute) {
        this.trackMute = trackMute;
    }
    public void setTrackSolo(@Nullable Boolean trackSolo) {
        this.trackSolo = trackSolo;
    }
    public void setTrackName(@Nullable String trackName) {
        this.trackName = trackName;
    }
    public void setTrackUri(@Nullable String trackUri) {
        this.trackUri = trackUri;
    }

    public @Nullable Integer getTrackVolume() {
        return trackVolume;
    }
    public @Nullable String getTrackPan() {
        return trackPan;
    }
    public @Nullable Boolean getTrackMute() {
        return trackMute;
    }
    public @Nullable Boolean getTrackSolo() {
        return trackSolo;
    }
    public @Nullable String getTrackName() {
        return trackName;
    }
    public @Nullable String getTrackUri() {
        return trackUri;
    }
}
