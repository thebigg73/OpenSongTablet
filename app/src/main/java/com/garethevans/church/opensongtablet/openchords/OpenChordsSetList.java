package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class OpenChordsSetList {
    // This gets the set from the folder object

    // Matches v1.0.7
    @Nullable
    private String id;
    @Nullable private String title;
    @Nullable private String notes;
    @Nullable private ArrayList<OpenChordsSetListItem> items;
    @Nullable private ArrayList<OpenChordsSongStructureItem> structure;
    @Nullable private String lastUpdated;

    @Nullable public String getId() {
        return id;
    }
    @Nullable public String getTitle() {
        return title;
    }
    @Nullable public String getNotes() {
        return notes;
    }
    @Nullable public ArrayList<OpenChordsSetListItem> getItems() {
        return items;
    }
    @Nullable public String getLastUpdated() {
        return lastUpdated;
    }
    @Nullable public ArrayList<OpenChordsSongStructureItem> getStructure() {
        return structure;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }
    public void setTitle(@Nullable String title) {
        this.title = title;
    }
    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }
    public void setItems(@Nullable ArrayList<OpenChordsSetListItem> items) {
        this.items = items;
    }
    public void setLastUpdated(@Nullable String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public void setStructure(@Nullable ArrayList<OpenChordsSongStructureItem> structure) {
        this.structure = structure;
    }
}
