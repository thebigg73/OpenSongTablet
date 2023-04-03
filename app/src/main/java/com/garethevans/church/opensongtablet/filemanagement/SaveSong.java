package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class SaveSong {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "SaveSong";

    public SaveSong(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    public boolean doSave(Song newSong) {
        // This is called from the EditSong fragment where we check for file/folder changes too
        // Because we haven't written the changes, we receive the 'newSong' object to compare with the current song

        Log.d(TAG,"filename:"+newSong.getFilename()+"  title:"+newSong.getTitle());
        // Only if we aren't messing with the welcome song!
        if (checkNotWelcomeSong(newSong)) {
            // Check for folders
            String oldFolder = mainActivityInterface.getSong().getFolder();
            String oldFilename = mainActivityInterface.getSong().getFilename();
            boolean folderChange = !newSong.getFolder().equals(oldFolder);
            boolean filenameChange = !newSong.getFilename().equals(oldFilename);
            String oldHighlighterFile_p = mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(),true);
            String oldHighlighterFile_l = mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(),false);
            String newHighlighterFile_p = mainActivityInterface.getProcessSong().getHighlighterFilename(newSong,true);
            String newHighlighterFile_l = mainActivityInterface.getProcessSong().getHighlighterFilename(newSong,false);

            // The folder may not be in 'Songs'.  If this is the case, it starts with ../
            // This is most common if a user wants to save a received song (set/nearby)
            ArrayList<String> oldLocation = mainActivityInterface.getStorageAccess().fixNonSongs(oldFolder);

            // Write the changes to the current Song object
            mainActivityInterface.setSong(newSong);

            if (folderChange || filenameChange) {
                // We need to rename the entry in the database
                mainActivityInterface.getSQLiteHelper().renameSong(oldFolder, newSong.getFolder(), oldFilename, newSong.getFilename());
                if (newSong.getFiletype().equals("PDF") || newSong.getFiletype().equals("IMG")) {
                    // If it isn't an XML file, also update the persistent database
                    mainActivityInterface.getNonOpenSongSQLiteHelper().renameSong(
                            oldFolder, newSong.getFolder(), oldFilename, newSong.getFilename());
                }

                // Now try to rename the highlighter files if they exist
                Uri portraitOld = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter","", oldHighlighterFile_p);
                Uri landscapeOld = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter","", oldHighlighterFile_l);
                if (mainActivityInterface.getStorageAccess().uriExists(portraitOld)) {
                    Uri portraitNew = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", newHighlighterFile_p);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doSave renameFileFromUri "+portraitOld+" to "+portraitNew);
                    mainActivityInterface.getStorageAccess().renameFileFromUri(portraitOld,portraitNew,"Highlighter","",newHighlighterFile_p);
                }
                if (mainActivityInterface.getStorageAccess().uriExists(landscapeOld)) {
                    Uri landscapeNew = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", newHighlighterFile_l);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doSave renameFileFromUri "+landscapeOld+" to "+landscapeNew);
                    mainActivityInterface.getStorageAccess().renameFileFromUri(landscapeOld,landscapeNew,"Highlighter", "", newHighlighterFile_l);
                }
            }

            // Now save the new song
            boolean saveSuccessful = updateSong(newSong,true);

            // Now, if the save was successful and the folder/filename changes, delete the old stuff
            if ((folderChange || filenameChange) && saveSuccessful) {
                // If there wasn't an old song, don't try to delete it otherwise we delete the Songs folder!
                if (oldFilename!=null && !oldFilename.isEmpty() && oldLocation.get(0)!=null && oldLocation.get(1)!=null) {
                    Uri oldUri = mainActivityInterface.getStorageAccess().
                            getUriForItem(oldLocation.get(0), oldLocation.get(1), oldFilename);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doSave deleteFile "+oldUri);
                    mainActivityInterface.getStorageAccess().deleteFile(oldUri);
                }
            }

            return saveSuccessful;

        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.error_song_not_saved));
            return false;
        }
    }

    // This updates the current song
    public boolean updateSong(Song thisSong, boolean updateSongMenu) {
        // This is called if we just want to save the current song updates stored in the current song
        // This only works is the folder and filename haven't changed (done in the step above from edit song instead)

        // Won't do anything if this is the 'Welcome' song
        if (checkNotWelcomeSong(thisSong)) {
            // First update the song database
            mainActivityInterface.getSQLiteHelper().updateSong(thisSong);

            // If this is a non-OpenSong song (PDF, IMG), update the persistent database
            if (!thisSong.getFiletype().equals("XML")) {
                // If update fails (due to no existing row, a new one is created)
                mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(thisSong);
            }

            // Update the CCLI log if required
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("ccliAutomaticLogging", false)) {
                mainActivityInterface.getCCLILog().addEntry(thisSong, "3"); // 3=edited
            }

            // Update the song menu
            if (updateSongMenu) {
                mainActivityInterface.updateSongMenu(thisSong);
            }

            mainActivityInterface.getSong().setFilename(thisSong.getFilename());
            mainActivityInterface.getSong().setFolder(thisSong.getFolder());
            mainActivityInterface.getPreferences().setMyPreferenceString("songFolder",thisSong.getFolder());
            mainActivityInterface.getPreferences().setMyPreferenceString("songFilename",thisSong.getFilename());

            // Update the song xml ready for saving
            mainActivityInterface.getSong().setSongXML(mainActivityInterface.getProcessSong().getXML(thisSong));

            // Now save the song file and return the success!
            if (thisSong.getFiletype().equals("XML")) {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" updateSong Songs/"+thisSong.getFolder()+"/"+thisSong.getFilename());
                return mainActivityInterface.getStorageAccess().saveThisSongFile(thisSong);
            } else {
                return true;
            }

        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.error_song_not_saved));
            return false; //Welcome song, so no saving!
        }
    }

    public boolean checkNotWelcomeSong(Song thisSong) {
        return (!thisSong.getFilename().equals("Welcome to OpenSongApp"));
    }
}
