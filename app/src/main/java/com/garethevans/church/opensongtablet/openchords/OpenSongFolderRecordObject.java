package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

public class OpenSongFolderRecordObject {
    // This is a json objec that we simply save the name of the folder and a UUID
    @Nullable private String folderName;      // e.g MAIN or Band
    @Nullable private String folderUuid;      // e.g aaaa-bbbb-cccc-ddddd
    @Nullable private String folderOwnerUuid; // e.g aaaa-bbbb-cccc-ddddd (currently not used but might be needed for authorisation with OpenChords

    @Nullable public String getFolderName() {
        return folderName;
    }
    @Nullable public String getFolderUuid() {
        return folderUuid;
    }
    @Nullable public String getFolderOwnerUuid() {
        return folderOwnerUuid;
    }

    public void setFolderName(@Nullable String folderName) {
        this.folderName = folderName;
    }
    public void setFolderUuid(@Nullable String folderUuid) {
        this.folderUuid = folderUuid;
    }
    public void setFolderOwnerUuid(@Nullable String folderOwnerUuid) {
        this.folderOwnerUuid = folderOwnerUuid;
    }
}
