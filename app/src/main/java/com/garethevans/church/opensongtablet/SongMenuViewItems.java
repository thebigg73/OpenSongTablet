package com.garethevans.church.opensongtablet;


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

    public String getFilename() {
        return filename;
    }
    /*public void setFilename(String filename) {
        this.filename = filename;
    }
*/
    public String getTitle() {
        return title;
    }
    /*public void setTitle(String title) {
        this.title = title;
    }
*/
    boolean getInSet() {
        return inset;
    }
    void setInSet(boolean inset) {this.inset = inset;}

    public String getAuthor() {
        return author;
    }
    /*public void setAuthor(String author) {
        this.author = author;
    }*/

    public String getKey() {
        return key;
    }
    /*public void setKey(String key) {
        this.key = key;
    }
*/
}