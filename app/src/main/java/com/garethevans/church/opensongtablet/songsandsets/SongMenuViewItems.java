package com.garethevans.church.opensongtablet.songsandsets;

class SongMenuViewItems {

    private final String filename;
    private final String title;
    private final String author;
    private final String key;
    private boolean inset;

    SongMenuViewItems(String filename, String title, String author, String key, boolean inset) {
        this.filename = filename;
        this.title = title;
        this.author = author;
        this.key = key;
        this.inset = inset;
    }

    String getFilename() {
        return filename;
    }

    String getTitle() {
        return title;
    }

    boolean getInSet() {
        return inset;
    }
    void setInSet(boolean inset) {this.inset = inset;}

    String getAuthor() {
        return author;
    }

    String getKey() {
        return key;
    }
}
