package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class OpenSongFolderObject {
    // This is a json object that keeps our ownerID and a note of our folders/UUIDs
    @Nullable private String ownerID;
    @Nullable private ArrayList<OpenSongFolderRecordObject> openSongFolderRecordObjects;

    @Nullable public String getOwnerID() {
        return ownerID;
    }
    @Nullable public ArrayList<OpenSongFolderRecordObject> getOpenSongFolderRecordObjects() {
        return openSongFolderRecordObjects;
    }

    public void setOwnerID(@Nullable String ownerID) {
        this.ownerID = ownerID;
    }
    public void setOpenSongFolderRecordObjects(@Nullable ArrayList<OpenSongFolderRecordObject> openSongFolderRecordObjects) {
        this.openSongFolderRecordObjects = openSongFolderRecordObjects;
    }
}
