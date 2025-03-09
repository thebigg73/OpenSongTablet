package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

public class OpenChordsSetListSongItem {

    // This holds the basic information required for a song in the set list

    // Matches v1.0.7
    @Nullable private String songId;
    //@Nullable private Integer capo;
    @Nullable private String transpose;

    @Nullable public String getSongId() {
        return songId;
    }
    /*@Nullable public Integer getCapo() {
        return capo;
    }*/
    @Nullable public String getTranspose() {
        return transpose;
    }

    public void setSongId(@Nullable String songId) {
        this.songId = songId;
    }
    //public void setCapo(@Nullable Integer capo) {
    //    this.capo = capo;
    //}
    public void setTranspose(@Nullable String transpose) {
        this.transpose = transpose;
    }
}
