package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.UnaryOperator;

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

    /*getters and setters*/
    /*getFolderList - package private, returns Array of String
    * creates list of folders and caches it in private class variable
    * which it then returns*/
    /*@NonNull
    String[] getFolderList(Context c, StorageAccess storageAccess) {
        folderList.clear();
        initialiseFolderList(c, "", storageAccess);
        postprocessListPath(c, storageAccess);
        try {
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(folderList, coll);
        } catch (Exception e) {
            // Error sorting
            Log.d("d", "Error sorting");
        }
        // Add the main folder to the top
        folderList.add(0, FullscreenActivity.mainfoldername);

        FullscreenActivity.mSongFolderNames = null;
        return folderList.toArray(new String[folderList.size()]).clone();
    }
*/
    /*this function simply strips the leading prefix from the file path*/
    /*private void postprocessListPath(Context c, StorageAccess storageAccess) {

        //replaceAll(unaryComp) is only available for newer versions of Android.
        // Added a check and alternative for older versions

        final Uri uri = storageAccess.getUriForItem(c,"Songs","","");
        *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            UnaryOperator<String> unaryComp = new UnaryOperator<String>() {
                @Override
                public String apply(String i) {
                    return i.substring(uri.getPath().length() + 1);
                }
            };
            folderList.replaceAll(unaryComp);

        } else {*//*
            for (int z=0;z<folderList.size();z++) {
                Log.d("d","folderList.get("+z+")="+folderList.get(z));
                String new_string = folderList.get(z);
                String bittoremove = uri.getPath();
                new_string = new_string.replace(bittoremove + "/","");
                folderList.set(z,new_string);
            }
        //}
        //
    }*/

    /*getSongFileList() - package private, returns array of String
    * returns an array of the file names of the currently chosen folder
    * */
    String[] getSongFileListasArray(Context c, StorageAccess storageAccess) {
        fileList(c, storageAccess);
        return currentFileList.toArray(new String[currentFileList.size()]).clone();
    }
/*

    */
/* a getter to return a list, should it be required. *//*

    List<String> getSongFileListasList(Context c, StorageAccess storageAccess) {
        // datastructure to encapsulate currentFileList and include invalidate
        //code, perhaps event handling?
        fileList(c, storageAccess);
        */
/*//*
/ Sort the file list
        try {
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(currentFileList, coll);
        } catch (Exception e) {
            // Error sorting
            Log.d("d","Error sorting");
        }*//*

        return currentFileList;
    }
*/
    /*private function to modify currentFileList by scanning the currently selected
    * folder
    * */
    private void fileList(Context c, StorageAccess storageAccess) {
        currentFileList.clear();
        // Filter out items in this folder

        ArrayList<String> filesinfolder = storageAccess.listFilesInFolder(c,"Songs",FullscreenActivity.whichSongFolder);

        // Not liking the comparator sort.  Reverse folder sorting
        // Create two arrays: one for folders, one for songs
        ArrayList<String> folders_found = new ArrayList<>();
        ArrayList<String> songs_found = new ArrayList<>();

        for (String item:filesinfolder) {
            Uri uri = storageAccess.getUriForItem(c, "Songs",FullscreenActivity.whichSongFolder,item);
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