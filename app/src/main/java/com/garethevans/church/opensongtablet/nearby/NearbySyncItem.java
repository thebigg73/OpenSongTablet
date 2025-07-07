package com.garethevans.church.opensongtablet.nearby;

public class NearbySyncItem {

    private String filename;
    private String title;
    private String folder;
    //private String subfolder;
    private String uuid;
    private String lastModified;
    private String comparisonText;
    //private boolean newer;
    //private boolean different;
    private boolean selected;
    //private boolean exists;
    //private boolean same;

    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }
    /*public void setSubfolder(String subfolder) {
        this.subfolder = subfolder;
    }*/
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
    /*public void setNewer(boolean newer) {
        this.newer = newer;
    }
    public void setDifferent(boolean different) {
        this.different = different;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
    public void setSame(boolean same) {
        this.same = same;
    }*/

    public String getFilename() {
        return filename;
    }
    public String getTitle() {
        return title;
    }
    public String getFolder() {
        return folder;
    }
    /*public String getSubfolder() {
        return subfolder;
    }*/
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
    /*public boolean getNewer() {
        return newer;
    }

    public boolean getDifferent() {
        return different;
    }
    public boolean getExists() {
        return exists;
    }
    public boolean getSame() {
        return same;
    }*/
}
