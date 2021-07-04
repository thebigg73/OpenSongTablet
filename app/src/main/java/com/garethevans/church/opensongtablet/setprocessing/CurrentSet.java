package com.garethevans.church.opensongtablet.setprocessing;

// This is the (current) set object
// All actions related to building/processing are in the SetActions class

import java.util.ArrayList;

public class CurrentSet {

    private String initialSetString;
    private String currentSetString;
    private ArrayList<String> setItems;           // The set item $$_folder/filename_**key**__$$
    private ArrayList<String> setFolders;    // The folder only
    private ArrayList<String> setFilenames;  // The filename only
    private ArrayList<String> setKeys;       // The key only
    private int indexSongInSet;
    private String previousSongInSet;
    private String nextSongInSet;
    private String setName;
    private String setFile;
    private String currentSetXML;

    // The current set is a combination of array lists
    // They are built on app start by parsing the set string from preferences
    public void initialiseTheSet() {
        // Clears ALL arraylists and values
        setItems = new ArrayList<>();
        currentSetString = "";
        initialSetString = "";
        setName = "";
        setFile = "";
        initialiseTheSpecifics();
    }
    public void initialiseTheSpecifics() {
        // Kept separate as when shuffling, we only call this not the initialiseTheSet()
        setFilenames = new ArrayList<>();
        setFolders = new ArrayList<>();
        setKeys = new ArrayList<>();
    }

    // Add items to the set
    public void addSetItem(String item) {
        setItems.add(item);
    }
    public void addSetValues(String folder, String filename, String key) {
        setFolders.add(folder);
        setFilenames.add(filename);
        setKeys.add(key);
    }

    // Get items from the set
    public ArrayList<String> getSetItems() {
        return setItems;
    }
    public String getItem(int position) {
        return getValueAtPosition("item",position);
    }
    public String getFolder(int position) {
        return getValueAtPosition("folder",position);
    }
    public String getFilename(int position) {
        return getValueAtPosition("filename",position);
    }
    public String getKey(int position) {
        return getValueAtPosition("key",position);
    }
    private String getValueAtPosition(String what, int position) {
        String value = "";
        try {
            switch (what) {
                case "item":
                    value = setItems.get(position);
                    break;
                case "folder":
                    value = setFolders.get(position);
                    break;
                case "filename":
                    value = setFilenames.get(position);
                    break;
                case "key":
                    value = setKeys.get(position);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    // Set or update items in the set
    public void setItem(int position, String value) {
        setItems.set(position, value);
    }
    public void setFolder(int position, String value) {
        setFolders.set(position, value);
    }
    public void setFilename(int position, String value) {
        setFilenames.set(position, value);
    }
    public void setKey(int position, String value) {
        setKeys.set(position, value);
    }

    // Set and get the initial and current set string (as saved in preferences)
    // These can be compared to show if a save is required due to changes
    public void setInitialSetString(String initialSetString) {
        this.initialSetString = initialSetString;
    }
    public void setCurrentSetString(String currentSetString) {
        this.currentSetString = currentSetString;
    }
    public String getInitialSetString() {
        return initialSetString;
    }
    public String getCurrentSetString() {
        return currentSetString;
    }

    public void updateSetItem(int position, String item) {
        setItems.set(position,item);
    }

    // TODO MIGHT REMOVE THE STUFF BELOW
    // The getters


    public int getIndexSongInSet() {
        return indexSongInSet;
    }
    public String getPreviousSongInSet() {
        return previousSongInSet;
    }
    public String getNextSongInSet() {
        return nextSongInSet;
    }
    public ArrayList<String> getSetFolders() {
        return setFolders;
    }
    public ArrayList<String> getSetFilenames() {
        return setFilenames;
    }
    public ArrayList<String> getSetKeys() {
        return setKeys;
    }
    public String getSetName() {
        return setName;
    }
    public String getCurrentSetXML() {
        return currentSetXML;
    }
    public String getSetFile() {
        return setFile;
    }



    public void addToCurrentSetString(String item) {
        currentSetString = currentSetString + item;
    }
    public void setSetItems(ArrayList<String> setItems) {
        this.setItems = setItems;
    }
    public void addToCurrentSet(String item) {
        setItems.add(item);
    }
    public int removeFromCurrentSet(int pos, String item) {
        if (pos==-1) {
            // Don't know, so look for it
            pos = setItems.indexOf(item);
        }
        if (pos!=-1) {
            setItems.remove(pos);
        }
        return pos;
    }
    public void setSetFolders(ArrayList<String> setFolders) {
        this.setFolders = setFolders;
    }
    public void addToCurrentSet_Folder(String item) {
        setFolders.add(item);
    }
    public void removeFromCurrentSet_Folder(int position) {
        setFolders.remove(position);
    }
    public void setSetFilenames(ArrayList<String> setFilenames) {
        this.setFilenames = setFilenames;
    }
    public void addToCurrentSet_Filename(String item) {
        setFilenames.add(item);
    }
    public void removeFromCurrentSet_Filename(int position) {
        setFilenames.remove(position);
    }
    public void setIndexSongInSet(int indexSongInSet) {
        this.indexSongInSet = indexSongInSet;
    }
    public void setNextSongInSet(String nextSongInSet) {
        this.nextSongInSet = nextSongInSet;
    }
    public void setPreviousSongInSet(String previousSongInSet) {
        this.previousSongInSet = previousSongInSet;
    }
    public void setSetName(String setName) {
        this.setName = setName;
    }
    public void setSetKeys(ArrayList<String> setKeys) {
        this.setKeys = setKeys;
    }
    public void addToCurrentSet_Key(String item) {
        setKeys.add(item);
    }
    public void removeFromCurrentSet_Key(int position) {
        setKeys.remove(position);
    }
    public void setCurrentSetXML(String currentSetXML) {
        this.currentSetXML = currentSetXML;
    }
    public void setSetFle(String setFile) {
        this.setFile = setFile;
    }
}
