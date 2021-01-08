package com.garethevans.church.opensongtablet.setprocessing;

// This is the (current) set object

import java.util.ArrayList;

public class CurrentSet {

    private String currentSetString;
    private ArrayList<String> currentSet;
    private ArrayList<String> currentSet_Folder;
    private ArrayList<String> currentSet_Filename;
    private ArrayList<String> currentSet_Key;
    private int indexSongInSet;
    private String previousSongInSet;
    private String nextSongInSet;
    private String setName;
    private String setFile;
    private String currentSetXML;

    // The getters
    public String getCurrentSetString() {
        return currentSetString;
    }
    public ArrayList<String> getCurrentSet() {
        return currentSet;
    }
    public int getIndexSongInSet() {
        return indexSongInSet;
    }
    public String getPreviousSongInSet() {
        return previousSongInSet;
    }
    public String getNextSongInSet() {
        return nextSongInSet;
    }
    public ArrayList<String> getCurrentSet_Folder() {
        return currentSet_Folder;
    }
    public ArrayList<String> getCurrentSet_Filename() {
        return currentSet_Filename;
    }
    public ArrayList<String> getCurrentSet_Key() {
        return currentSet_Key;
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


    // The setters
    public void setCurrentSetString(String currentSetString) {
        this.currentSetString = currentSetString;
    }
    public void addToCurrentSetString(String item) {
        currentSetString = currentSetString + item;
    }
    public void setCurrentSet(ArrayList<String> currentSet) {
        this.currentSet = currentSet;
    }
    public void addToCurrentSet(String item) {
        currentSet.add(item);
    }
    public int removeFromCurrentSet(int pos, String item) {
        if (pos==-1) {
            // Don't know, so look for it
            pos = currentSet.indexOf(item);
        }
        if (pos!=-1) {
            currentSet.remove(pos);
        }
        return pos;
    }
    public void setCurrentSet_Folder(ArrayList<String> currentSet_Folder) {
        this.currentSet_Folder = currentSet_Folder;
    }
    public void addToCurrentSet_Folder(String item) {
        currentSet_Folder.add(item);
    }
    public void removeFromCurrentSet_Folder(int position) {
        currentSet_Folder.remove(position);
    }
    public void setCurrentSet_Filename(ArrayList<String> currentSet_Filename) {
        this.currentSet_Filename = currentSet_Filename;
    }
    public void addToCurrentSet_Filename(String item) {
        currentSet_Filename.add(item);
    }
    public void removeFromCurrentSet_Filename(int position) {
        currentSet_Filename.remove(position);
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
    public void setCurrentSet_Key(ArrayList<String> currentSet_Key) {
        this.currentSet_Key = currentSet_Key;
    }
    public void addToCurrentSet_Key(String item) {
        currentSet_Key.add(item);
    }
    public void removeFromCurrentSet_Key(int position) {
        currentSet_Key.remove(position);
    }
    public void setCurrentSetXML(String currentSetXML) {
        this.currentSetXML = currentSetXML;
    }
    public void setSetFle(String setFile) {
        this.setFile = setFile;
    }
}
