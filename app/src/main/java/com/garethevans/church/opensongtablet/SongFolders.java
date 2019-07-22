package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

// This file simply prepares the songFolders from the SQLite database

class SongFolders {

    ArrayList<String> prepareSongFolders(Context c, Preferences preferences) {
        // Use the database to get the available folders
        SQLiteHelper sqLiteHelper = new SQLiteHelper(c);
        ArrayList<String> folders = sqLiteHelper.getFolders(c);

        // Sort the list
        if (StaticVariables.locale==null) {
            FixLocale.fixLocale(c,preferences);
        }
        Collator collator = Collator.getInstance(StaticVariables.locale);
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(folders, collator);

        // Add the main folder to the top if it isn't already there
        int pos = folders.indexOf(c.getString(R.string.mainfoldername));
        if (pos<0) {
            // It isn't there, so add it
            folders.add(0,c.getString(R.string.mainfoldername));

        } else if (pos>0) {
            // It's there, but not at the top - remove it
            folders.remove(pos);
            // Add it to the top position
            folders.add(0,c.getString(R.string.mainfoldername));
        }

        for (String folder:folders) {
            Log.d("SongFolders", "folder=" + folder);
        }

        return folders;
    }
}
