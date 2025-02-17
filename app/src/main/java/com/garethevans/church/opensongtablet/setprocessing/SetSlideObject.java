package com.garethevans.church.opensongtablet.setprocessing;

import androidx.annotation.Nullable;

public class SetSlideObject {

    // An object used to hold individual slide information
    @Nullable private String body = null;
    @Nullable private String image = null;
    @Nullable private String description = null;
    @Nullable private String filename = null;
    @Nullable private String background = null;

    @Nullable public String getBody() {
        return body;
    }
    @Nullable public String getImage() {
        return image;
    }
    @Nullable public String getDescription() {
        return description;
    }
    @Nullable public String getFilename() {
        return filename;
    }
    @Nullable public String getBackground() {
        return background;
    }

    public void setBody(@Nullable String body) {
        this.body = body;
    }
    public void setImage(@Nullable String image) {
        this.image = image;
    }
    public void setDescription(@Nullable String description) {
        this.description = description;
    }
    public void setFilename(@Nullable String filename) {
        this.filename = filename;
    }
    public void setBackground(@Nullable String background) {
        this.background = background;
    }
}
