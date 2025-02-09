package com.garethevans.church.opensongtablet.openchords;

public class OpenChordsSetListItem {

    // This lists the items in the setlist
    private String id;
    private String title;
    private String type;
    private OpenChordsSetListSongItem songItem;
    private String notes;
    private String customData;
    private String lastUpdated;

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getType() {
        return type;
    }
    public OpenChordsSetListSongItem getSongItem() {
        return songItem;
    }
    public String getNotes() {
        return notes;
    }
    public String getCustomData() {
        return customData;
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
    public void setType(String type) {
        this.type = type;
    }
    public void setSongItem(OpenChordsSetListSongItem songItem) {
        this.songItem = songItem;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public void setCustomData(String customData) {
        this.customData = customData;
    }
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
