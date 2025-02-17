package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

public class OpenChordsTag {
    // This gets the tags from the folder object

    // Matches v1.0.7
    @Nullable private String id;
    @Nullable private String title;
    @Nullable private String color;

    @Nullable public String getId() {
        return id;
    }
    @Nullable public String getTitle() {
        return title;
    }
    @Nullable public String getColor() {
        return color;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }
    public void setTitle(@Nullable String title) {
        this.title = title;
    }
    public void setColor(@Nullable String color) {
        this.color = color;
    }
}
