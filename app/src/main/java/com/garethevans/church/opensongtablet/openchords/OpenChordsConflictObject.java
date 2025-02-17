package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class OpenChordsConflictObject {

    // This is a json object that we can get some info on
    @Nullable
    private String uuid;
    @Nullable
    private String lastQuery;
    @Nullable
    private String lastUploadNewSongs;
    @Nullable
    private String lastUploadNewSets;
    @Nullable
    private String lastUploadSongChanges;
    @Nullable
    private String lastUploadSetChanges;
    @Nullable
    private String lastDownloadNewSongs;
    @Nullable
    private String lastDownloadNewSets;
    @Nullable
    private String lastDownloadSongChanges;
    @Nullable
    private String lastDownloadSetChanges;
    @Nullable
    private String lastForcePush;
    @Nullable
    private String lastForcePull;
    @Nullable
    private ArrayList<OpenChordsConflictItemObject> items;

    @Nullable
    public String getUuid() {
        return uuid;
    }

    @Nullable
    public String getLastQuery() {
        return lastQuery;
    }

    @Nullable
    public String getLastUploadNewSongs() {
        return lastUploadNewSongs;
    }

    @Nullable
    public String getLastUploadNewSets() {
        return lastUploadNewSets;
    }

    @Nullable
    public String getLastUploadSongChanges() {
        return lastUploadSongChanges;
    }

    @Nullable
    public String getLastUploadSetChanges() {
        return lastUploadSetChanges;
    }

    @Nullable
    public String getLastDownloadNewSongs() {
        return lastDownloadNewSongs;
    }

    @Nullable
    public String getLastDownloadNewSets() {
        return lastDownloadNewSets;
    }

    @Nullable
    public String getLastDownloadSongChanges() {
        return lastDownloadSongChanges;
    }

    @Nullable
    public String getLastDownloadSetChanges() {
        return lastDownloadSetChanges;
    }

    @Nullable
    public String getLastForcePush() {
        return lastForcePush;
    }

    @Nullable
    public String getLastForcePull() {
        return lastForcePull;
    }

    @Nullable
    public ArrayList<OpenChordsConflictItemObject> getItems() {
        return items;
    }

    public void setUuid(@Nullable String uuid) {
        this.uuid = uuid;
    }

    public void setLastQuery(@Nullable String lastQuery) {
        this.lastQuery = lastQuery;
    }

    public void setLastUploadNewSongs(@Nullable String lastUploadNewSongs) {
        this.lastUploadNewSongs = lastUploadNewSongs;
    }

    public void setLastUploadNewSets(@Nullable String lastUploadNewSets) {
        this.lastUploadNewSets = lastUploadNewSets;
    }

    public void setLastUploadSongChanges(@Nullable String lastUploadSongChanges) {
        this.lastUploadSongChanges = lastUploadSongChanges;
    }

    public void setLastUploadSetChanges(@Nullable String lastUploadSetChanges) {
        this.lastUploadSetChanges = lastUploadSetChanges;
    }

    public void setLastDownloadNewSongs(@Nullable String lastDownloadNewSongs) {
        this.lastDownloadNewSongs = lastDownloadNewSongs;
    }

    public void setLastDownloadNewSets(@Nullable String lastDownloadNewSets) {
        this.lastDownloadNewSets = lastDownloadNewSets;
    }

    public void setLastDownloadSongChanges(@Nullable String lastDownloadSongChanges) {
        this.lastDownloadSongChanges = lastDownloadSongChanges;
    }

    public void setLastDownloadSetChanges(@Nullable String lastDownloadSetChanges) {
        this.lastDownloadSetChanges = lastDownloadSetChanges;
    }

    public void setLastForcePush(@Nullable String lastForcePush) {
        this.lastForcePush = lastForcePush;
    }

    public void setLastForcePull(@Nullable String lastForcePull) {
        this.lastForcePull = lastForcePull;
    }

    public void setItems(@Nullable ArrayList<OpenChordsConflictItemObject> items) {
        this.items = items;
    }

}
