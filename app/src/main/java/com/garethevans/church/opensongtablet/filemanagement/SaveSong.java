package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

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
            String oldHighlighterFile_p = mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(),true,1);
            String oldHighlighterFile_c = mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(),true,2);
            String oldHighlighterFile_l = mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(),false,2);
            String newHighlighterFile_p = mainActivityInterface.getProcessSong().getHighlighterFilename(newSong,true,1);
            String newHighlighterFile_c = mainActivityInterface.getProcessSong().getHighlighterFilename(newSong,true,2);
            String newHighlighterFile_l = mainActivityInterface.getProcessSong().getHighlighterFilename(newSong,false,2);

            // The folder may not be in 'Songs'.  If this is the case, it starts with ../
            // This is most common if a user wants to save a received song (set/nearby)
            //ArrayList<String> oldLocation = mainActivityInterface.getStorageAccess().fixNonSongs(oldFolder);
            String[] oldLocation = mainActivityInterface.getStorageAccess().getActualFoldersFromNice(oldFolder);

            // Write the changes to the current Song object
            mainActivityInterface.setSong(newSong);

            if (folderChange || filenameChange) {
                // We need to rename the entry in the database
                mainActivityInterface.getSQLiteHelper().renameSong(oldFolder, newSong.getFolder(), oldFilename, newSong.getFilename());
                if (newSong.getFiletype().equals("PDF") || newSong.getFiletype().equals("IMG")) {
                    // If it isn't an XML file, also update the persistent database
                    mainActivityInterface.getNonOpenSongSQLiteHelper().renameSong(
                            oldLocation[0], newSong.getFolder(), oldLocation[1], newSong.getFilename());
                    // Copy the pdf/img file now
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doSave move "
                            +oldFolder+"/"+oldFilename+" to "+newSong.getFolder()+"/"+newSong.getFilename());
                    mainActivityInterface.getStorageAccess().copyFromTo(oldLocation[0],
                            oldLocation[1], oldFilename,"Songs",
                            newSong.getFolder(),newSong.getFilename());
                }

                // Now try to rename the highlighter files if they exist
                Uri pOld = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter","", oldHighlighterFile_p);
                Uri cOld = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter","", oldHighlighterFile_c);
                Uri lOld = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter","", oldHighlighterFile_l);
                if (mainActivityInterface.getStorageAccess().uriExists(pOld)) {
                    Uri pNew = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", newHighlighterFile_p);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doSave renameFileFromUri "+pOld+" to "+pNew);
                    mainActivityInterface.getStorageAccess().renameFileFromUri(pOld,pNew,"Highlighter","",newHighlighterFile_p);
                }
                if (mainActivityInterface.getStorageAccess().uriExists(cOld)) {
                    Uri cNew = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", newHighlighterFile_l);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doSave renameFileFromUri "+cOld+" to "+cNew);
                    mainActivityInterface.getStorageAccess().renameFileFromUri(cOld,cNew,"Highlighter", "", newHighlighterFile_c);
                }
                if (mainActivityInterface.getStorageAccess().uriExists(lOld)) {
                    Uri lNew = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", newHighlighterFile_l);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doSave renameFileFromUri "+lOld+" to "+lNew);
                    mainActivityInterface.getStorageAccess().renameFileFromUri(lOld,lNew,"Highlighter", "", newHighlighterFile_l);
                }
            }

            // Now save the new song
            boolean saveSuccessful = updateSong(newSong,true);

            // Now, if the save was successful and the folder/filename changes, delete the old stuff
            if ((folderChange || filenameChange) && saveSuccessful) {
                // If there wasn't an old song, don't try to delete it otherwise we delete the Songs folder!
                if (oldFilename!=null && !oldFilename.isEmpty() && oldLocation[0]!=null && oldLocation[1]!=null) {
                    Uri oldUri = mainActivityInterface.getStorageAccess().
                            getUriForItem(oldLocation[0], oldLocation[1], oldFilename);
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

            Log.d(TAG,"thisSong.getFolder():"+thisSong.getFolder());
            Log.d(TAG,"thisSong.getFilename():"+thisSong.getFilename());
            Log.d(TAG,"thisSong.getFiletype():"+thisSong.getFiletype());
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
