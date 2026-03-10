package com.garethevans.church.opensongtablet.songprocessing;

public class SongId {

    // A simple object that holds the song folder and filename
    // Used for the web server to prepare song menus

    private String folder="";
    private String filename="";
    private String key = "";
    private String author = "";

    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getFolder() {
        return folder;
    }
    public String getFilename() {
        return filename;
    }
    public String getKey() {
        return key;
    }
    public String getAuthor() {
        return author;
    }
}
