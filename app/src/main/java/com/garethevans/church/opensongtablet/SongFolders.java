package com.garethevans.church.opensongtablet;

import android.content.Context;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// This file simply prepares the songFolders

class SongFolders {

    void prepareSongFolders(Context c, StorageAccess storageAccess, Preferences preferences) {
        // Use the folderIds to get the song folders found
        // This is used for the folder chooser popup

        ArrayList<String> folders = new ArrayList<>();
        FullscreenActivity.mSongFolderNames = null;
        String bittoremove = storageAccess.getUriForItem(c, preferences, "Songs", "", "").getPath() + "/";
        for (String s : FullscreenActivity.folderIds) {
            s = s.replace(bittoremove, "");
            folders.add(s);
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
        folders.add(0, c.getString(R.string.mainfoldername));
        FullscreenActivity.mSongFolderNames = folders.toArray(new String[0]).clone();
    }
}
