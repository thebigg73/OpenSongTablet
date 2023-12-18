package com.garethevans.church.opensongtablet.setprocessing;

import android.net.Uri;

public class FoundSet {
    private String filename;
    private String title;
    private Uri uri;
    private String tag;
    private String lastModifiedString;
    private long lastModifiedLong;

    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setUri(Uri uri) {
        this.uri = uri;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public void setLastModifiedString(String lastModifiedString) {
        this.lastModifiedString = lastModifiedString;
    }
    public void setLastModifiedLong(long lastModifiedLong) {
        this.lastModifiedLong = lastModifiedLong;
    }

    public String getFilename() {
        return filename;
    }
    public String getTitle() {
        return title;
    }
    public Uri getUri() {
        return uri;
    }
    public String getTag() {
        return tag;
    }
    public String getLastModifiedString() {
        return lastModifiedString;
    }
    public long getLastModifiedLong() {
        return lastModifiedLong;
    }
}
