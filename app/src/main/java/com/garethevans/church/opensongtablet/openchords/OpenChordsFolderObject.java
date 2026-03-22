package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class OpenChordsFolderObject {

    // This is the shareable folder object that can hold songs, sets, tags, etc

    // Matches v1.0.7
    @Nullable private String title;                             // The name of the shared folder
    @Nullable private String ownerId;                           // The UUID that matches the user folder
    @Nullable private Boolean readonly;                         // Whether the folder is readonly or not
    @Nullable private Boolean isOwner;                          // Whether the user is the owner of the folder
    @Nullable private String type;                              // Default is 'folder'
    @Nullable private ArrayList<OpenChordsSong> songs;          // The songs array in the folder
    @Nullable private ArrayList<OpenChordsTag> tags;            // The tags array in the folder
    @Nullable private ArrayList<OpenChordsSetList> setlists;    // The sets array in the folder

    // The getters
    @Nullable public String getOwnerId() {
        return ownerId;
    }
    @Nullable public String getTitle() {
        return title;
    }
    @Nullable public Boolean getReadonly() {
        return readonly;
    }
    @Nullable public Boolean getIsOwner() {
        return isOwner;
    }
    @Nullable public ArrayList<OpenChordsSong> getSongs() {
        return songs;
    }
    @Nullable public ArrayList<OpenChordsSetList> getSetLists() {
        return setlists;
    }
    @Nullable public ArrayList<OpenChordsTag> getTags() {
        return tags;
    }

    // The setters
    public void setOwnerId(@Nullable String ownerId) {
        this.ownerId = ownerId;
    }
    public void setTitle(@Nullable String title) {
        this.title = title;
    }
    public void setReadonly(@Nullable Boolean readonly) {
        this.readonly = readonly;
    }
    public void setIsOwner(@Nullable Boolean isOwner) {
        this.isOwner = isOwner;
    }
    public void setSongs(@Nullable ArrayList<OpenChordsSong> songs) {
        this.songs = songs;
    }
    public void setSetLists(@Nullable ArrayList<OpenChordsSetList> setlists) {
        this.setlists = setlists;
    }
    public void setTags(@Nullable ArrayList<OpenChordsTag> tags) {
        this.tags = tags;
    }
}
