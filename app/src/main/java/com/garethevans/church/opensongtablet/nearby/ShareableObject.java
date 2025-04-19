package com.garethevans.church.opensongtablet.nearby;

public class ShareableObject {

    // This is used to populate a json object with a note of the song folder, title, filename, last modified, uuid
    // This can also be used to list sets.

    private String title;
    private String filename;
    private String folder;
    private String lastModified;
    private String uuid;

    public String getTitle() {
        return title;
    }
    public String getFilename() {
        return filename;
    }
    public String getFolder() {
        return folder;
    }
    public String getLastModified() {
        return lastModified;
    }
    public String getUuid() {
        return uuid;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}