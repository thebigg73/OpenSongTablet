/*
package com.garethevans.church.opensongtablet.OLD_TO_DELETE;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet._Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

// This file simply prepares the songFolders from the SQLite database

public class _SongFolders {

    public ArrayList<String> prepareSongFolders(Context c, _Preferences preferences) {
        // Use the database to get the available folders
        _SQLiteHelper sqLiteHelper = new _SQLiteHelper(c);
        ArrayList<String> folders = sqLiteHelper.getFolders(c);

        // Sort the list
        if (StaticVariables.locale==null) {
            _FixLocale.fixLocale(c,preferences);
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
*/
