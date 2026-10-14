package com.garethevans.church.opensongtablet.importsongs;

public class SongMatch {
    private final String title;
    private final String artist;
    private final String url;
    private double rating = -1;
    private int votes = -1;
    private int version = 0;
    private final String providerName;

    public SongMatch(String title, String artist, String url, String providerName) {
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.providerName = providerName;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    public void setVotes(int votes) {
        this.votes = votes;
    }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getUrl() { return url; }
    public double getRating() {
        return rating;
    }
    public int getVotes() {
        return votes;
    }
    public String getVersion() {
        if (version>1) {
            return " (v"+version+")";
        } else {
            return "";
        }
    }
    public String getProviderName() { return providerName; }
}