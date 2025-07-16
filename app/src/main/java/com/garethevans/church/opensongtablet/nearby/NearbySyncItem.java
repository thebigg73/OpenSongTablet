package com.garethevans.church.opensongtablet.nearby;

public class NearbySyncItem {

    private String filename;
    private String title;
    private String folder;
    private String uuid;
    private String lastModified;
    private String comparisonText;
    private boolean selected;

    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    public void setComparisonText(String comparisonText) {
        this.comparisonText = comparisonText;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getFilename() {
        return filename;
    }
    public String getTitle() {
        return title;
    }
    public String getFolder() {
        return folder;
    }
    public String getUuid() {
        return uuid;
    }
    public String getLastModified() {
        return lastModified;
    }
    public String getComparisonText() {
        return comparisonText;
    }
    public boolean getSelected() {
        return selected;
    }
}
