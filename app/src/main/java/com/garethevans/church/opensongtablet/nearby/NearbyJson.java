package com.garethevans.church.opensongtablet.nearby;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class NearbyJson {

    // Any information sent by a connected device will be either sent as a string of this JSON object,
    // Or for files, this will preceed the file being sent (so the receiver can wait and process it when it arrives)
    private Long id;                // A reference to a file payload that is to come
    private String what;            // What this payload is (one of the nearbyAction.[identifier strings]
    private String folder;          // The folder this file belongs in on the sending device
    private String filename;        // The filename of this file on the sending device
    private String swipeDirection;  // If this is a song, what direction should the swipe action take
    private String key;             // If this is a song, the key on the sending device
    private String xml;             // If this is a song and is XML, the file content (if less than 30kb)
    private Float scrollProportion; // If we send scrollBy or scrollTo, this is the proportion of the scroll
    private String deviceSending;   // The id of the device that is sending the file
    private String deviceToAction;  // The id of the device that should be acting on this
    private Integer section;        // The section to show
    private String message;         // The nearbyMessage to show
    private Song song;              // If we are sharing a pdf/image, also share the extra info from the database
    private ArrayList<ShareableObject> shareableSongObjects;        // If we are sharing song objects
    private ArrayList<ShareableObject> shareableSetObjects;         // If we are sharing set objects
    private ArrayList<ShareableObject> shareableProfileObjects;     // If we are sharing profile objects

    public @Nullable Long getId() {
        return id;
    }
    public @Nullable String getWhat() {
        return what;
    }
    public @Nullable String getFolder() {
        return folder;
    }
    public @Nullable String getFilename() {
        return filename;
    }
    public @Nullable String getSwipeDirection() {
        return swipeDirection;
    }
    public @Nullable String getKey() {
        return key;
    }
    public @Nullable String getXml() {
        return xml;
    }
    public @Nullable Float getScrollProportion() {
        return scrollProportion;
    }
    public @Nullable String getDeviceSending() {
        return deviceSending;
    }
    public @Nullable String getDeviceToAction() {
        return deviceToAction;
    }
    public @Nullable Integer getSection() {
        return section;
    }
    public @Nullable String getMessage() {
        return message;
    }
    public @Nullable Song getSong() {
        return song;
    }
    public @Nullable ArrayList<ShareableObject> getShareableSongObjects() {
        return shareableSongObjects;
    }
    public @Nullable ArrayList<ShareableObject> getShareableSetObjects() {
        return shareableSetObjects;
    }
    public @Nullable ArrayList<ShareableObject> getShareableProfileObjects() {
        return shareableProfileObjects;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setWhat(String what) {
        this.what = what;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setSwipeDirection(String R2L_L2R) {
        this.swipeDirection = R2L_L2R;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setXml(String xml) {
        this.xml = xml;
    }
    public void setScrollProportion(Float scrollProportion) {
        this.scrollProportion = scrollProportion;
    }
    public void setDeviceSending(String deviceSending) {
        this.deviceSending = deviceSending;
    }
    public void setDeviceToAction(String deviceToAction) {
        this.deviceToAction = deviceToAction;
    }
    public void setSection(Integer section) {
        this.section = section;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setSong(Song song) {
        this.song = song;
    }
    public void setShareableSongObjects(ArrayList<ShareableObject> shareableSongObjects) {
        this.shareableSongObjects = shareableSongObjects;
    }
    public void setShareableSetObjects(ArrayList<ShareableObject> shareableSetObjects) {
        this.shareableSetObjects = shareableSetObjects;
    }
    public void setShareableProfileObjects(ArrayList<ShareableObject> shareableProfileObjects) {
        this.shareableProfileObjects = shareableProfileObjects;
    }
}
