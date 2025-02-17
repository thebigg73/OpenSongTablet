package com.garethevans.church.opensongtablet.setprocessing;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SetObject {
    // This is used to convert between a set object and set xml
    @Nullable private String setName = null;
    @Nullable private String uuid = null;
    @Nullable private String lastModified = null;
    @Nullable private String notes = null;
    @Nullable private ArrayList<SetSlideGroupObject> slideGroups = null;

    @Nullable public String getSetName() {
        return setName;
    }
    @Nullable public String getUuid() {
        return uuid;
    }
    @Nullable public String getLastModified() {
        return lastModified;
    }
    @Nullable public String getNotes() {
        return notes;
    }
    @Nullable public ArrayList<SetSlideGroupObject> getSlideGroups() {
        return slideGroups;
    }

    public void setSetName(@Nullable String setName) {
        this.setName = setName;
    }
    public void setUuid(@Nullable String uuid) {
        this.uuid = uuid;
    }
    public void setLastModified(@Nullable String lastModified) {
        this.lastModified = lastModified;
    }
    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }
    public void setSlideGroups(@Nullable ArrayList<SetSlideGroupObject> slideGroups) {
        this.slideGroups = slideGroups;
    }
}
