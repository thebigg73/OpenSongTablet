package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

// File created by James on 10/22/17.

/* final class - uninheritable -   Private member folderList accessible by getter
* getFolderlistasList which initialises the folderList if it is null, and then
* populates it with the folderList, which is parsed to remove the path prefix.
* */
final class SongFileList {
    private ArrayList<String>   folderList;
    private ArrayList<String>   currentFileList;
    Collator coll;

    // constructor
    SongFileList() {
        folderList = new ArrayList<>();
        currentFileList = new ArrayList<>();
    }

    String[] getSongFileListasArray(Context c, Preferences preferences, StorageAccess storageAccess) {
        fileList(c, preferences, storageAccess);
        //return currentFileList.toArray(new String[currentFileList.size()]).clone();
        return currentFileList.toArray(new String[0]).clone();
    }

    private void fileList(Context c, Preferences preferences, StorageAccess storageAccess) {
        currentFileList.clear();
        // Filter out items in this folder

        ArrayList<String> filesinfolder = storageAccess.listFilesInFolder(c,"Songs",FullscreenActivity.whichSongFolder);

        // Not liking the comparator sort.  Reverse folder sorting
        // Create two arrays: one for folders, one for songs
        ArrayList<String> folders_found = new ArrayList<>();
        ArrayList<String> songs_found = new ArrayList<>();

        for (String item:filesinfolder) {
            Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", FullscreenActivity.whichSongFolder, item);
            if (!storageAccess.uriIsFile(c,uri)) {
                folders_found.add(uri.getLastPathSegment());
            } else {
                songs_found.add(uri.getLastPathSegment());
            }
        }

        try {
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(folders_found, coll);
        } catch (Exception e) {
            // Error sorting
            Log.d("d","Error sorting");
        }

        // Now sort the songs
        try {
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(songs_found, coll);
        } catch (Exception e) {
            // Error sorting
            Log.d("d","Error sorting");
        }

        // Now join the two arrays back together
        currentFileList.addAll(folders_found);
        currentFileList.addAll(songs_found);
    }

    /*intialises the folderList variable*/
    private void initialiseFolderList(Context c, String folder, StorageAccess storageAccess) {
        folderList.clear();
        folderList = FullscreenActivity.folderIds;
        /*// List files at this location
        //ArrayList<String> items = storageAccess.listFilesInFolder(c,"Songs",folder);
        if (Fullscreen!=null && items.size()>0) {
            for (String item : items) {
                Uri uri = storageAccess.getUriForItem(c,"Songs","", item);
                if(!storageAccess.uriIsFile(c, uri)) {
                    folderList.add(uri.getPath());
                    initialiseFolderList(c,item,storageAccess);
                }
            }
        }*/
    }
}