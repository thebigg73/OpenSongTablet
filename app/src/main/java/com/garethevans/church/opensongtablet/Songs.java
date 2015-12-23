package com.garethevans.church.opensongtablet;

public class Songs {

    private int id;
    private String fileid;
    private String title;
    private String author;
    private String lyrics;

    public Songs() {
    }

    public Songs(String fileid, String title, String author, String lyrics) {
        super();
        this.fileid = fileid;
        this.title = title;
        this.author = author;
        this.lyrics = lyrics;
    }

    //getters & setters
    @Override
    public String toString() {
        return "Songs [id=" + id + ", fileid=" + fileid + ", title=" + title + ", author=" + author + ",lyrics=" + lyrics
                + "]";
    }

    public int getId() {
        return id;
    }

    public String getFileId() {
        return fileid;
    }

    public void setFileId(String fileid) {
        this.fileid = fileid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getLyrics() {
        return lyrics;
    }

}