package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

public class OpenChordsFolderPermissionsObject {

    @Nullable private String userID;
    @Nullable private Boolean readonly;

    @Nullable
    public String getUserID() {
        return userID;
    }
    public void setUserID(@Nullable String userID) {
        this.userID = userID;
    }
    @Nullable
    public Boolean getReadonly() {
        return readonly;
    }
    public void setReadonly(@Nullable Boolean readonly) {
        this.readonly = readonly;
    }

}
