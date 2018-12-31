package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;

public class ListSongFiles {

    // This is used to build the song index by matching the current folder with allSongDetails array
    // Filenames (including subfolders) are added to mSongFileNames

    Collator coll;

    // This is what will work with SAF eventually....
    void songUrisInFolder(Context c, Preferences preferences) {
        // All of the songs are in the FullscreenActivity.songIds
        // This is only updated when the app boots up, or the user forces a database rebuild
        // Individual songs are just appended to this list

        // Get the song folder document id
        StorageAccess storageAccess = new StorageAccess();
        Uri songFolderUri = storageAccess.getUriForItem(c, preferences, "Songs", FullscreenActivity.whichSongFolder, "");
        String songfolderid;
        if (storageAccess.lollipopOrLater()) {
            songfolderid = storageAccess.getDocumentsContractId(songFolderUri);
        } else {
            songfolderid = songFolderUri.getPath();
        }

        // Now extract the ones in the current folder from the songIds
        FullscreenActivity.songsInFolderUris = new ArrayList<>();
        if (songfolderid!=null) {
            for (String id : FullscreenActivity.songIds) {
                String totest = id.replace(songfolderid, "");
                // If we are in the mainfolder, we shouldn't have any '/'
                if ((FullscreenActivity.whichSongFolder.equals(c.getString(R.string.mainfoldername)) ||
                        FullscreenActivity.whichSongFolder.equals("")) && !totest.contains("/")) {
                    Uri uri = storageAccess.documentUriFromId(id);
                    FullscreenActivity.songsInFolderUris.add(uri);

                } else if (!totest.replace(FullscreenActivity.whichSongFolder + "/", "").contains("/")) {
                    // If we are in the current folder, we shouldn't have any / after this
                    FullscreenActivity.songsInFolderUris.add(storageAccess.documentUriFromId(id));
                }
            }
        }

    }

    void getAllSongFiles(Context c, StorageAccess storageAccess) {
        // This will list all of the songs and subfolders in the current folder
        // They are extracted from the FullscreenActivity.allSongsForMenu array

        ArrayList<String> songs = new ArrayList<>();
        songs.clear();
        ArrayList<String> subfolders = new ArrayList<>();
        subfolders.clear();
        ArrayList<String> authors = new ArrayList<>();
        authors.clear();
        ArrayList<String> authorsfolder = new ArrayList<>();
        authorsfolder.clear();
        ArrayList<String> keys = new ArrayList<>();
        keys.clear();
        ArrayList<String> keysfolder = new ArrayList<>();
        keysfolder.clear();

        if (FullscreenActivity.whichSongFolder.startsWith("../")) {
            // This is one of the custom slides/notes/images
            songs = storageAccess.listFilesInFolder(c,"",FullscreenActivity.whichSongFolder);

        } else {

            // Go through the values stored and add the ones required
            for (int w = 0; w < FullscreenActivity.allSongDetailsForMenu.length; w++) {

                if ((FullscreenActivity.whichSongFolder.equals("") ||
                        FullscreenActivity.whichSongFolder.equals(c.getString(R.string.mainfoldername))) &&
                        (FullscreenActivity.allSongDetailsForMenu[w][0].equals("") ||
                                FullscreenActivity.allSongDetailsForMenu[w][0].equals(c.getString(R.string.mainfoldername)))) {
                    songs.add(FullscreenActivity.allSongDetailsForMenu[w][1]);
                    authors.add(FullscreenActivity.allSongDetailsForMenu[w][2]);
                    keys.add(FullscreenActivity.allSongDetailsForMenu[w][3]);

                } else if (FullscreenActivity.allSongDetailsForMenu[w][0].equals(FullscreenActivity.whichSongFolder)) {
                    songs.add(FullscreenActivity.allSongDetailsForMenu[w][1]);
                    authors.add(FullscreenActivity.allSongDetailsForMenu[w][2]);
                    keys.add(FullscreenActivity.allSongDetailsForMenu[w][3]);
                }
            }

            // Go through the folders and add the root ones
            for (String f : FullscreenActivity.mSongFolderNames) {
                Log.d("d", "Folders=" + f);

                // Don't add the folder if it is the main folder, or is a sub/sub folder or if it is the current whichSongFolder
                if (!f.equals(c.getString(R.string.mainfoldername)) && !f.contains("/") &&
                        !f.equals(FullscreenActivity.whichSongFolder)) {
                    // This is a subfolder of the MAIN folder - add it.  This way we only show roots (not folders within folders)
                    subfolders.add("/" + f + "/");
                    authorsfolder.add("");
                    keysfolder.add(c.getString(R.string.songsinfolder));
                }
            }
        }

        // Add them together (folders at the start)
        ArrayList<String> item_files = new ArrayList<>();
        ArrayList<String> item_authors = new ArrayList<>();
        ArrayList<String> item_keys = new ArrayList<>();
        item_files.addAll(subfolders);
        item_files.addAll(songs);
        item_authors.addAll(authorsfolder);
        item_authors.addAll(authors);
        item_keys.addAll(keysfolder);
        item_keys.addAll(keys);

        FullscreenActivity.mSongFileNames = item_files.toArray(new String[0]).clone();
        FullscreenActivity.songDetails = new String[FullscreenActivity.mSongFileNames.length][3];
        for (int y = 0; y < FullscreenActivity.mSongFileNames.length; y++) {
            FullscreenActivity.songDetails[y][0] = item_files.get(y);
            FullscreenActivity.songDetails[y][1] = item_authors.get(y);
            FullscreenActivity.songDetails[y][2] = item_keys.get(y);
        }
    }

    void getCurrentSongIndex() {
        // Find the current song index from the song filename
        // Set them all to 0
        FullscreenActivity.currentSongIndex = 0;
        FullscreenActivity.nextSongIndex = 0;
        FullscreenActivity.previousSongIndex = 0;

        // Go through the array
        try {
            if (FullscreenActivity.mSongFileNames != null && FullscreenActivity.songfilename != null) {
                for (int s = 0; s < FullscreenActivity.mSongFileNames.length; s++) {
                    if (FullscreenActivity.mSongFileNames != null &&
                            FullscreenActivity.mSongFileNames[s] != null &&
                            FullscreenActivity.mSongFileNames[s].equals(FullscreenActivity.songfilename)) {
                        FullscreenActivity.currentSongIndex = s;
                        if (s > 0) {
                            FullscreenActivity.previousSongIndex = s - 1;
                        } else {
                            FullscreenActivity.previousSongIndex = s;
                        }
                        if (s < FullscreenActivity.mSongFileNames.length - 1) {
                            FullscreenActivity.nextSongIndex = s + 1;
                        } else {
                            FullscreenActivity.nextSongIndex = s;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(e.getMessage(),"Some error with the song list");
        }
    }

}