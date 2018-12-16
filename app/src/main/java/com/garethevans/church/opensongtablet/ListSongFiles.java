package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ListSongFiles {

    static Collator coll;

    // This is what will work with SAF eventually....
    void songUrisInFolder(Context c) {
        // All of the songs are in the FullscreenActivity.songIds

        // Get the song folder document id
        StorageAccess storageAccess = new StorageAccess();
        Uri songFolderUri = storageAccess.getUriForItem(c,"Songs",FullscreenActivity.whichSongFolder,"");
        String songfolderid;
        if (storageAccess.lollipopOrLater()) {
            songfolderid = storageAccess.getDocumentsContractId(songFolderUri);
        } else {
            songfolderid = songFolderUri.getPath();
        }

        // Now extract the ones in the current folder
        FullscreenActivity.songsInFolderUris = new ArrayList<>();
        if (songfolderid!=null) {
            for (String id : FullscreenActivity.songIds) {
                String totest = id.replace(songfolderid, "");
                // If we are in the mainfolder, we shouldn't have any '/'
                if ((FullscreenActivity.whichSongFolder.equals(c.getString(R.string.mainfoldername)) ||
                        FullscreenActivity.whichSongFolder.equals("")) && !totest.contains("/")) {
                    Uri uri = storageAccess.documentUriFromId(id);
                    FullscreenActivity.songsInFolderUris.add(uri);

                    // If we are in the current folder, we shouldn't have any / after this
                } else if (!totest.replace(FullscreenActivity.whichSongFolder + "/", "").contains("/")) {
                    FullscreenActivity.songsInFolderUris.add(storageAccess.documentUriFromId(id));
                }
            }
        }

    }

    public static void getAllSongFolders(Context c, StorageAccess storageAccess) {
        //FullscreenActivity.allfilesforsearch.clear();
        //FullscreenActivity.mSongFolderNames = FullscreenActivity.songfilelist.getFolderList(c, storageAccess);
        // Use the folderIds to get the song folders found
        ArrayList<String> folders = new ArrayList<>();
        FullscreenActivity.mSongFolderNames = null;
        String bittoremove = storageAccess.getUriForItem(c,"Songs","","").getPath() + "/";
        for (String s:FullscreenActivity.folderIds) {
            folders.add(s.replace(bittoremove,""));
        }

        // Remove any duplicates
        Set<String> hs = new HashSet<>(folders);
        folders.clear();
        folders.addAll(hs);

        // Sort the list
        Collator collator = Collator.getInstance(FullscreenActivity.locale);
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(folders, collator);

        // Add the main folder to the top
        folders.add(0,c.getString(R.string.mainfoldername));
        //FullscreenActivity.mSongFolderNames = folders.toArray(new String[folders.size()]).clone();
        FullscreenActivity.mSongFolderNames = folders.toArray(new String[0]).clone();
    }

    static void getAllSongFiles(Context c, StorageAccess storageAccess) {
        // This will list all of the songs and subfolders in the current folder
        ArrayList<String> songs = new ArrayList<>();
        ArrayList<String> subfolders = new ArrayList<>();
        if (FullscreenActivity.whichSongFolder.startsWith("../")) {
            // This is one of the custom slides/notes/images
            songs = storageAccess.listFilesInFolder(c,"",FullscreenActivity.whichSongFolder);
        } else {
            String bittoremove = storageAccess.getUriForItem(c, "Songs", FullscreenActivity.whichSongFolder, "").getPath() + "/";
            if (storageAccess.lollipopOrLater() && FullscreenActivity.songIds!=null && FullscreenActivity.songIds.get(0)!=null) {

                // Get the start section to remove
                String[] bits = FullscreenActivity.songIds.get(0).split("OpenSong/Songs/");
                if (bits.length==2) {
                    bittoremove = bits[0] + "OpenSong/Songs/";
                } else {
                    bittoremove = FullscreenActivity.songIds.get(0);
                }

                if (FullscreenActivity.whichSongFolder!=null &&
                        !FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                    bittoremove = bittoremove + FullscreenActivity.whichSongFolder + "/";
                }

            }

            for (String s : FullscreenActivity.songIds) {

                if (s.startsWith(bittoremove)) {
                    // We are in the folder!
                    s = s.replace(bittoremove, "");


                    if (s.contains("/")) {
                        // This is a subfolder.  Get the root only
                        s = s.substring(0, s.indexOf("/"));
                        if (!subfolders.contains("/" + s + "/")) {
                            subfolders.add("/" + s + "/");
                        }
                    } else {
                        // This is a song
                        songs.add(s);
                    }
                }
            }
        }

        // Sort the songs and the folders
        Collator collator = Collator.getInstance(FullscreenActivity.locale);
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(songs, collator);
        Collections.sort(subfolders, collator);

        // Add them together
        ArrayList<String> items = new ArrayList<>();
        items.addAll(subfolders);
        items.addAll(songs);

        //FullscreenActivity.mSongFileNames = items.toArray(new String[items.size()]).clone();
        FullscreenActivity.mSongFileNames = items.toArray(new String[0]).clone();
    }

    /*TODO why use a multidimensional array, when you could use an xml object?
    I've been reading about performance and I guess its because of performance
    limitations?  Is maintaining an object in memory expensive
    in terms of performance?  So, the class I created is essentially worse
    than reading directly from the file system?  I don't think so personally,
    as I don't think the garbage collector will be dereference either of the objects
    internal to the songfilelist class, and the songfilelist class persists for the
    lifetime of the app, so there shouldn't be any extra work, and the memory overhead
    is low and speed of access of cached variable is faster than file access, at
    least I guess.
     */
    static void getSongDetails(final Context c, StorageAccess storageAccess, LoadXML loadXML) {
        // Go through each song in the current folder and extract the title, key and author
        // If not a valid song, just return the file name
        try {
            FullscreenActivity.songDetails = new String[FullscreenActivity.mSongFileNames.length][3];
            boolean fileextensionok;
            String utf;
            for (int r = 0; r < FullscreenActivity.mSongFileNames.length; r++) {
                String s = FullscreenActivity.mSongFileNames[r];
                boolean isdir = s.startsWith("/") && s.endsWith("/");
                String[] vals = new String[3];
                Uri uri;
                if (isdir) {
                    s = s.substring(1,s.length()-1);
                    uri = storageAccess.getUriForItem(c,"Songs",s,"");
                } else {
                    uri = storageAccess.getUriForItem(c, "Songs", FullscreenActivity.whichSongFolder,
                            FullscreenActivity.mSongFileNames[r]);
                }
                if (storageAccess.uriExists(c, uri)) {
                    if (isdir) {
                        // This is a directory
                        vals[0] = s;
                        vals[1] = "";
                        vals[2] = c.getString(R.string.songsinfolder);
                    } else {
                        fileextensionok = storageAccess.checkFileExtensionValid(uri);
                        utf = storageAccess.getUTFEncoding(c, uri);
                        if (fileextensionok) {
                            vals = loadXML.getSongDetails(c, uri, utf, s,storageAccess);
                        } else {
                            // Not an opensong file
                            vals[0] = s;
                            vals[1] = "";
                            vals[2] = "";
                        }
                    }
                }

                try {
                    FullscreenActivity.songDetails[r][0] = vals[0];
                    FullscreenActivity.songDetails[r][1] = vals[1];
                    FullscreenActivity.songDetails[r][2] = vals[2];
                } catch (Exception e) {
                    // Error trying to get song details
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        FullscreenActivity.numDirs = 0;
        try {
            while (FullscreenActivity.songDetails!=null &&
                    FullscreenActivity.songDetails.length>FullscreenActivity.numDirs &&
                    FullscreenActivity.songDetails[FullscreenActivity.numDirs]!=null &&
                    FullscreenActivity.songDetails[FullscreenActivity.numDirs][2] != null &&
                    FullscreenActivity.songDetails[FullscreenActivity.numDirs][2].equals(c.getString(R.string.songsinfolder))) {
                FullscreenActivity.numDirs++;
            }
        } catch (Exception e){
            e.printStackTrace();
            Log.d("d","Error building a valid index - it's empty");
        }
        //numDirs is zerobased index >> horrible hack.
        if (FullscreenActivity.numDirs > 0) {
            FullscreenActivity.numDirs += 1;
        }
    }

    static void getCurrentSongIndex() {
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