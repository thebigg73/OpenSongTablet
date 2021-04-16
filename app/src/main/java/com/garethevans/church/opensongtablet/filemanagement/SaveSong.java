package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
import java.io.OutputStream;

public class SaveSong {

    public boolean doSave(Context c, MainActivityInterface mainActivityInterface,
                          Song newSong, Song originalSong, boolean fromEditFragment, boolean imgOrPDF) {

        boolean saveOK = false;

        String where = mainActivityInterface.getStorageAccess().
                safeFilename(mainActivityInterface.getProcessSong().getLocation(newSong.getFolder()));
        String folder = mainActivityInterface.getStorageAccess().
                safeFilename(newSong.getFolder()).replace(".."+where+"/","");
        String filename = mainActivityInterface.getStorageAccess().
                safeFilename(newSong.getFilename());

        // If we are editing as chordpro, convert the lyrics back to back to OpenSong first
        // This is only checked if we've come from the song edit fragment.
        if (fromEditFragment && mainActivityInterface.getPreferences().
                getMyPreferenceBoolean(c, "editAsChordPro", false)) {
            newSong.setLyrics(mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(newSong.getLyrics()));
        }

        // Decide if we need to remove the original after writing the new song
        // This happens if the user has changed the filename or folder
        boolean removeOriginal = false;
        Uri oldUri = null;
        String oldwhere, oldfolder, oldfilename;
        if (originalSong!=null) {
            oldwhere = mainActivityInterface.getStorageAccess().
                    safeFilename(mainActivityInterface.getProcessSong().getLocation(mainActivityInterface.getSong().getFolder()));
            oldfolder = mainActivityInterface.getStorageAccess().
                    safeFilename(originalSong.getFolder()).replace(".." + where + "/", "");
            oldfilename = mainActivityInterface.getStorageAccess().
                    safeFilename(originalSong.getFilename());

            removeOriginal = !oldfolder.isEmpty() && oldfilename != null && !oldfilename.isEmpty() &&
                    (!oldwhere.equals(where) || !oldfolder.equals(folder) || !oldfilename.equals(filename));

            oldUri = mainActivityInterface.getStorageAccess().
                    getUriForItem(c, mainActivityInterface.getPreferences(), oldwhere, oldfolder, oldfilename);

        }
        Uri newUri = mainActivityInterface.getStorageAccess().
                getUriForItem(c, mainActivityInterface.getPreferences(), where, folder, filename);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c,mainActivityInterface.getPreferences(),
                newUri,null,where,folder,filename);

        if (!imgOrPDF || (removeOriginal && oldUri!=null)) {
            // Updated the song name, folder or contents, so write to the new/original file as appropriate
            // Check the uri exists for the outputstream to be valid
            mainActivityInterface.getStorageAccess().
                    lollipopCreateFileForOutputStream(c, mainActivityInterface.getPreferences(), newUri, null,
                    where, folder, filename);

            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, newUri);
            if (imgOrPDF && removeOriginal) {
                // PDFs and images don't store the data - this goes into the nonOpenSongSQL database
                // Just move the file by copying it to the new location.  Removal comes later
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c,oldUri);
                saveOK = mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Prepare a new XML version of the song from the statics (OpenSong song only)
                String newXML = mainActivityInterface.getProcessSong().getXML(c,mainActivityInterface,newSong);
                saveOK = mainActivityInterface.getStorageAccess().writeFileFromString(newXML, outputStream);
            }
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Update the default non persistent database for all songs (keeps menus and search up to date
        // Only update the song id/filename/folder if we:
        // Flagged to remove the original and save of the new file was successful
        if (!removeOriginal || !saveOK) {
            mainActivityInterface.setSong(newSong);
        }
        mainActivityInterface.getSQLiteHelper().updateSong(c,mainActivityInterface,mainActivityInterface.getSong());

        // If it was a PDF/IMG, update the persistent database as well
        // If save wasn't successful, we sorted the folder/filename/id already
        if (imgOrPDF) {
            // If update fails (due to no existing row, a new one is created)
            mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(c,mainActivityInterface,
                    mainActivityInterface.getSong());
        }

        // If we need to remove the original for non-pdfand the new file was successfully created, make the change
        if (saveOK && removeOriginal) {
            mainActivityInterface.getStorageAccess().deleteFile(c,oldUri);
        }

        // If we are autologging CCLI information
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"ccliAutomaticLogging",false)) {
            mainActivityInterface.getCCLILog().addEntry(c,mainActivityInterface,"3"); // 3=edited
        }
        return saveOK;
    }

}
