package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class SaveSong {

    public boolean doSave(Context c, MainActivityInterface mainActivityInterface, Song newSong) {
        // This is called from the EditSong fragment where we check for file/folder changes too
        // Because we haven't written the changes, we receive the 'newSong' object to compare with the current song

        // Only if we aren't messing with the welcome song!
        if (checkNotWelcomeSong(mainActivityInterface)) {
            // Check for folders
            String oldFolder = mainActivityInterface.getSong().getFolder();
            String oldFilename = mainActivityInterface.getSong().getFilename();
            boolean folderChange = !newSong.getFolder().equals(oldFolder);
            boolean filenameChange = !newSong.getFilename().equals(oldFilename);

            // The folder may not be in 'Songs'.  If this is the case, it starts with ../
            // This is most common if a user wants to save a received song (set/nearby)
            ArrayList<String> oldLocation = mainActivityInterface.getStorageAccess().fixNonSongs(oldFolder);

            // Write the changes to the current Song object
            mainActivityInterface.setSong(newSong);

            if (folderChange || filenameChange) {
                // We need to create a new entry in the database
                mainActivityInterface.getSQLiteHelper().createSong(c, mainActivityInterface,
                        newSong.getFolder(), newSong.getFilename());
                if (!newSong.getFiletype().equals("XML")) {
                    // If it isn't an XML file, also update the persistent database
                    mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(c, mainActivityInterface,
                            newSong.getFolder(), newSong.getFilename());
                }
            }

            // Now save the new song
            boolean saveSuccessful = updateSong(c, mainActivityInterface);

            // Now, if the save was successful and the folder/filename changes, delete the old stuff
            if ((folderChange || filenameChange) && saveSuccessful) {
                mainActivityInterface.getSQLiteHelper().deleteSong(c,mainActivityInterface,oldFolder,oldFilename);
                if (!newSong.getFiletype().equals("XML")) {
                    // If it isn't an XML file, also update the persistent database
                    mainActivityInterface.getNonOpenSongSQLiteHelper().deleteSong(c,mainActivityInterface,oldFolder,oldFilename);
                }

                Uri oldUri = mainActivityInterface.getStorageAccess().
                        getUriForItem(c, mainActivityInterface, oldLocation.get(0), oldLocation.get(1), oldFilename);
                mainActivityInterface.getStorageAccess().deleteFile(c, oldUri);
            }

            return saveSuccessful;
        } else {
            mainActivityInterface.getShowToast().doIt(c,c.getString(R.string.error_song_not_saved));
            return false;
        }
    }

    // This updates the current song
    public boolean updateSong(Context c, MainActivityInterface mainActivityInterface) {
        // This is called if we just want to save the current song updates stored in the current song
        // This only works is the folder and filename haven't changed (done in the step above from edit song instead)

        // Won't do anything if this is the 'Welcome' song
        if (checkNotWelcomeSong(mainActivityInterface)) {
            // First update the song database
            mainActivityInterface.getSQLiteHelper().updateSong(c, mainActivityInterface, mainActivityInterface.getSong());

            // If this is a non-OpenSong song (PDF, IMG), update the persistent database
            if (!mainActivityInterface.getSong().getFiletype().equals("XML")) {
                // If update fails (due to no existing row, a new one is created)
                mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(c, mainActivityInterface,
                        mainActivityInterface.getSong());
            }

            // Update the CCLI log if required
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "ccliAutomaticLogging", false)) {
                mainActivityInterface.getCCLILog().addEntry(c, mainActivityInterface,
                        mainActivityInterface.getSong(), "3"); // 3=edited
            }

            // Now save the song file and return the success!
            return mainActivityInterface.getStorageAccess().saveSongFile(c, mainActivityInterface);

        } else {
            mainActivityInterface.getShowToast().doIt(c,c.getString(R.string.error_song_not_saved));
            return false; //Welcome song, so no saving!
        }
    }

    private boolean checkNotWelcomeSong(MainActivityInterface mainActivityInterface) {
        return (!mainActivityInterface.getSong().getFilename().equals("Welcome to OpenSongApp") &&
                !mainActivityInterface.getSong().getFilename().equals("Welcome to OpenSongApp"));
    }
}
