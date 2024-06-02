package com.garethevans.church.opensongtablet.nearby;

public class HostItem {

    private String filename;
    private String title;
    private String folder;
    private String subfolder;
    private String category;
    private String identifier;
    private String tag;
    private boolean exists;
    private boolean checked;

    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setSubfolder(String subfolder) {
        this.subfolder = subfolder;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public void setExists(boolean exists) {
        this.exists = exists;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
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
    public String getSubfolder() {
        return subfolder;
    }
    public String getCategory() {
        return category;
    }
    public String getTag() {
        return tag;
    }
    public String getIdentifier() {
        return identifier;
    }
    public boolean getExists() {
        return exists;
    }
    public boolean getChecked() {
        return checked;
    }
}
