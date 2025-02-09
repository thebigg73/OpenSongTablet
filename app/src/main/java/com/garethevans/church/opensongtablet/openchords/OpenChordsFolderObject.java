package com.garethevans.church.opensongtablet.openchords;

import java.util.ArrayList;

public class OpenChordsFolderObject {

    // This is the shareable folder object that can hold songs, sets, tags, etc

    private String title;                   // The name of the shared folder
    private String ownerId;                 // The UUID that matches the user folder
    private ArrayList<OpenChordsSong> songs;     // The songs array in the folder
    private ArrayList<OpenChordsTag> tags;       // The tags array in the folder
    // TODO add sets, tags, etc.
    private ArrayList<OpenChordsSetList> setlists;       // The sets array in the folder

    // The getters
    public String getOwnerId() {
        return ownerId;
    }
    public String getTitle() {
        return title;
    }
    public ArrayList<OpenChordsSong> getSongs() {
        return songs;
    }
    public ArrayList<OpenChordsSetList> getSetLists() {
        return setlists;
    }
    public ArrayList<OpenChordsTag> getTags() {
        return tags;
    }

    // The setters
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setSongs(ArrayList<OpenChordsSong> songs) {
        this.songs = songs;
    }
    public void setSetLists(ArrayList<OpenChordsSetList> setlists) {
        this.setlists = setlists;
    }
    public void setTags(ArrayList<OpenChordsTag> tags) {
        this.tags = tags;
    }
}
