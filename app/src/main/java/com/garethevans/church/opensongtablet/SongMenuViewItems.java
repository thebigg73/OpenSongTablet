package com.garethevans.church.opensongtablet;


class SongMenuViewItems {

    String filename;
    String title;
    String author;
    String key;
    boolean inset;

    SongMenuViewItems(String filename, String title, String author, String key, boolean inset) {
        this.filename = filename;
        this.title = title;
        this.author = author;
        this.key = key;
        this.inset = inset;
    }

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getInSet() {
        return inset;
    }
    public void setInSet(boolean inset) {this.inset = inset;}

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

}