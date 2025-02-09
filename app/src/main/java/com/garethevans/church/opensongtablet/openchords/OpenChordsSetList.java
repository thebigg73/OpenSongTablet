package com.garethevans.church.opensongtablet.openchords;

import java.util.ArrayList;

public class OpenChordsSetList {
    // This gets the set from the folder object
    private String id;
    private String title;
    private String notes;
    private ArrayList<OpenChordsSetListItem> items;
    private String lastUpdated;

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getNotes() {
        return notes;
    }
    public ArrayList<OpenChordsSetListItem> getItems() {
        return items;
    }
    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public void setItems(ArrayList<OpenChordsSetListItem> items) {
        this.items = items;
    }
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}
