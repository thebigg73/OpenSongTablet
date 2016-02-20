package com.garethevans.church.opensongtablet;

public class SearchViewItems {

    String filename;
    String title;
    String folder;
    String author;
    String key;
    String theme;
    String lyrics;
    String hymnnum;

    SearchViewItems(String filename, String title, String folder, String author, String key, String theme, String lyrics, String hymnnum) {
        this.filename = filename;
        this.title = title;
        this.folder = folder;
        this.author = author;
        this.key = key;
        this.theme = theme;
        this.lyrics = lyrics;
        this.hymnnum = hymnnum;
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

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getHymnnum() {
        return hymnnum;
    }

    public void setHymnnum(String hymnnum) {
        this.hymnnum = hymnnum;
    }

}