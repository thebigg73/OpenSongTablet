package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class ListSongFiles {

    // This is used to build the song index by matching the current folder with allSongDetails array
    // Filenames (including subfolders) are added to mSongFileNames

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

    void getAllSongFiles(Context c, Preferences preferences, StorageAccess storageAccess) {
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
            String folder = FullscreenActivity.whichSongFolder.replace("../", "");
            songs = storageAccess.listFilesInFolder(c, preferences, "", folder);
            String what = "";
            if (FullscreenActivity.whichSongFolder.startsWith("../Notes")) {
                what = c.getString(R.string.note);
            } else if (FullscreenActivity.whichSongFolder.startsWith("../Slides")) {
                what = c.getString(R.string.slide);
            } else if (FullscreenActivity.whichSongFolder.startsWith("../Variations")) {
                what = c.getString(R.string.variation);
            } else if (FullscreenActivity.whichSongFolder.startsWith("../Images")) {
                what = c.getString(R.string.image_slide);
            } else if (FullscreenActivity.whichSongFolder.startsWith("../Scripture")) {
                what = c.getString(R.string.scripture);
            } else if (FullscreenActivity.whichSongFolder.startsWith("../Received")) {
                what = c.getString(R.string.connected_device_song);
            }
            // Add author as custom and key as blank for each item in this folder
            for (int l = 0; l < songs.size(); l++) {
                authors.add(what);
                keys.add("");
            }

        } else {

            // Go through the values stored and add the ones required
            for (int w = 0; w < FullscreenActivity.allSongDetailsForMenu.length; w++) {
                boolean mainfolder = FullscreenActivity.whichSongFolder.equals("") ||
                        FullscreenActivity.whichSongFolder.equals(c.getString(R.string.mainfoldername));
                boolean songfolderisnotnull = FullscreenActivity.allSongDetailsForMenu[w] != null &&
                        FullscreenActivity.allSongDetailsForMenu[w][0] != null;
                boolean songisinmainfolder = songfolderisnotnull &&
                        (FullscreenActivity.allSongDetailsForMenu[w][0].replace("/", "").equals("") ||
                                FullscreenActivity.allSongDetailsForMenu[w][0].replace("/", "").equals(FullscreenActivity.mainfoldername));
                boolean songisincurrentfolder = songfolderisnotnull &&
                        (FullscreenActivity.allSongDetailsForMenu[w][0].replace("/", "").equals(FullscreenActivity.whichSongFolder));

                if (mainfolder && songisinmainfolder) {
                    songs.add(FullscreenActivity.allSongDetailsForMenu[w][1]);
                    authors.add(FullscreenActivity.allSongDetailsForMenu[w][2]);
                    keys.add(FullscreenActivity.allSongDetailsForMenu[w][3]);

                } else if (songisincurrentfolder && !FullscreenActivity.allSongDetailsForMenu[w][3].equals(c.getString(R.string.songsinfolder))) {
                    songs.add(FullscreenActivity.allSongDetailsForMenu[w][1]);
                    authors.add(FullscreenActivity.allSongDetailsForMenu[w][2]);
                    keys.add(FullscreenActivity.allSongDetailsForMenu[w][3]);
                }
            }


            // Go through the folders and add the root ones
            for (String f : FullscreenActivity.mSongFolderNames) {

                String thisfolder = FullscreenActivity.whichSongFolder;
                boolean containsfolder = f.contains(thisfolder + "/");
                String leftfolder = "";
                if (containsfolder) {
                    leftfolder = f.substring(f.lastIndexOf(thisfolder + "/") + thisfolder.length() + 1);
                }
                if ((thisfolder.equals(FullscreenActivity.mainfoldername) || thisfolder.equals("")) &&
                        !leftfolder.contains("/")) {
                    if (!f.equals(FullscreenActivity.mainfoldername) && !f.contains("/")) {
                        leftfolder = f;
                    }
                }

                // Don't add the folder if it is the main folder, or is a sub/sub folder or if it is the current whichSongFolder
                if (!leftfolder.equals("")) {
                    // This is a subfolder of the MAIN folder - add it.  This way we only show roots (not folders within folders)
                    subfolders.add("/" + leftfolder + "/");
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

        // Make the first song item the first one after the song folders
        FullscreenActivity.firstSongIndex = subfolders.size();

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
        // Set them all to the firstSongIndex (0 if no subfolders, but if not, the first song after the subfolders)
        FullscreenActivity.currentSongIndex = FullscreenActivity.firstSongIndex;
        FullscreenActivity.nextSongIndex = FullscreenActivity.firstSongIndex;
        FullscreenActivity.previousSongIndex = FullscreenActivity.firstSongIndex;

        // Go through the array
        try {
            if (FullscreenActivity.songDetails != null && FullscreenActivity.songfilename != null) {
                for (int s = 0; s < FullscreenActivity.songDetails.length; s++) {
                    if (FullscreenActivity.songDetails[s][0] != null && FullscreenActivity.songDetails[s][0].equals(FullscreenActivity.songfilename)) {
                        FullscreenActivity.currentSongIndex = s;
                        if (s > FullscreenActivity.firstSongIndex) {
                            FullscreenActivity.previousSongIndex = s - 1;
                        }
                        if (s < FullscreenActivity.songDetails.length - 1) {
                            FullscreenActivity.nextSongIndex = s + 1;
                        } else {
                            FullscreenActivity.nextSongIndex = s;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("ListSongFiles", "Some error with the song list");
        }
    }

}