package com.garethevans.church.opensongtablet.nearby;

import java.util.ArrayList;

public class ShareableObjects {

    // This is the link for a json object with all shareable items
    private ArrayList<ShareableObject> shareableSongObjects;
    private ArrayList<ShareableObject> shareableSetObjects;

    public ArrayList<ShareableObject> getShareableSongObjects() {
        return shareableSongObjects;
    }
    public ArrayList<ShareableObject> getShareableSetObjects() {
        return shareableSetObjects;
    }

    public void setShareableSongObjects(ArrayList<ShareableObject> shareableSongObjects) {
        this.shareableSongObjects = shareableSongObjects;
    }
    public void setShareableSetObjects(ArrayList<ShareableObject> shareableSetObjects) {
        this.shareableSetObjects = shareableSetObjects;
    }
}
