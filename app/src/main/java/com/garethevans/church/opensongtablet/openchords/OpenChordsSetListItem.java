package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

public class OpenChordsSetListItem {

    // This lists the items in the setlist

    // Matches 1.0.7
    @Nullable private String id;
    @Nullable private String title;
    @Nullable private String type;
    @Nullable private OpenChordsSetListSongItem songItem;
    @Nullable private String notes;
    @Nullable private String customData;
    @Nullable private String lastUpdated;

    @Nullable public String getId() {
        return id;
    }
    @Nullable public String getTitle() {
        return title;
    }
    @Nullable public String getType() {
        return type;
    }
    @Nullable public OpenChordsSetListSongItem getSongItem() {
        return songItem;
    }
    @Nullable public String getNotes() {
        return notes;
    }
    @Nullable public String getCustomData() {
        return customData;
    }
    @Nullable public String getLastUpdated() {
        return lastUpdated;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }
    public void setTitle(@Nullable String title) {
        this.title = title;
    }
    public void setType(@Nullable String type) {
        this.type = type;
    }
    public void setSongItem(@Nullable OpenChordsSetListSongItem songItem) {
        this.songItem = songItem;
    }
    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }
    public void setCustomData(@Nullable String customData) {
        this.customData = customData;
    }
    public void setLastUpdated(@Nullable String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
