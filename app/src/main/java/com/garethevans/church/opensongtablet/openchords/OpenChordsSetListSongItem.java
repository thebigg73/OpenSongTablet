package com.garethevans.church.opensongtablet.openchords;

public class OpenChordsSetListSongItem {

    // This holds the basic information required for a song in the set list
    private String songId;
    private int capo;
    private String transpose;

    public String getSongId() {
        return songId;
    }

    public int getCapo() {
        return capo;
    }

    public String getTranspose() {
        return transpose;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public void setCapo(int capo) {
        this.capo = capo;
    }

    public void setTranspose(String transpose) {
        this.transpose = transpose;
    }
}
