package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

public class OpenChordsCompareObject {

    @Nullable private String title;
    @Nullable private String uuid;
    @Nullable private String lastModified;
    @Nullable private String type;


    @Nullable public String getTitle() {
        return title;
    }
    @Nullable public String getUuid() {
        return uuid;
    }
    @Nullable public String getLastModified() {
        return lastModified;
    }
    @Nullable public String getType() {
        return type;
    }
    public void setTitle(@Nullable String title) {
        this.title = title;
    }
    public void setUuid(@Nullable String uuid) {
        this.uuid = uuid;
    }
    public void setLastModified(@Nullable String lastModified) {
        this.lastModified = lastModified;
    }
    public void setType(@Nullable String type) {
        this.type = type;
    }
}
