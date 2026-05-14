package com.garethevans.church.opensongtablet.webserver;

public class SongIdentifier {

    // A simple class that can be received from the Webserver that identifies a song
    // It has two values only: folder and filename
    private String folder;
    private String filename;

    public SongIdentifier(String folder, String filename) {
        this.folder = folder;
        this.filename = filename;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFolder() {
        return folder;
    }
    public String getFilename() {
        return filename;
    }

}
