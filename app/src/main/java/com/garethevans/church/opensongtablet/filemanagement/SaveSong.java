package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.io.InputStream;
import java.io.OutputStream;

public class SaveSong {

    public boolean doSave(Context c, Preferences preferences, StorageAccess storageAccess,
                          ConvertChoPro convertChoPro, ProcessSong processSong,
                          Song song, Song originalSong, SQLiteHelper sqLiteHelper,
                          NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper, CommonSQL commonSQL,
                          CCLILog ccliLog, boolean imgOrPDF) {

        boolean saveOK = false;

        String where = storageAccess.safeFilename(processSong.getLocation(song.getFolder()));
        String folder = storageAccess.safeFilename(song.getFolder()).replace(".."+where+"/","");
        String filename = storageAccess.safeFilename(song.getFilename());

        String oldwhere = storageAccess.safeFilename(processSong.getLocation(song.getFolder()));
        String oldfolder = storageAccess.safeFilename(originalSong.getFolder()).replace(".."+where+"/","");
        String oldfilename = storageAccess.safeFilename(originalSong.getFilename());

        // If we are editing as chordpro, convert the lyrics back to back to OpenSong first
        if (preferences.getMyPreferenceBoolean(c, "editAsChordPro", false)) {
            song.setLyrics(convertChoPro.fromChordProToOpenSong(song.getLyrics()));
        }

        // Decide if we need to remove the original after writing the new song
        // This happens if the user has changed the filename or folder
        boolean removeOriginal = !oldfolder.isEmpty() && oldfilename != null && !oldfilename.isEmpty() &&
                (!oldwhere.equals(where) || !oldfolder.equals(folder) || !oldfilename.equals(filename));

        Uri oldUri = storageAccess.getUriForItem(c, preferences, oldwhere, oldfolder, oldfilename);
        Uri newUri = storageAccess.getUriForItem(c, preferences, where, folder, filename);

        if (!imgOrPDF || removeOriginal) {
            // Updated the song name, folder or contents, so write to the new/original file as appropriate
            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, newUri, null,
                    where, folder, filename);

            OutputStream outputStream = storageAccess.getOutputStream(c, newUri);
            if (imgOrPDF) {
                // PDFs and images don't store the data - this goes into the nonOpenSongSQL database
                // Just move the file by copying it to the new location.  Removal comes later
                InputStream inputStream = storageAccess.getInputStream(c,oldUri);
                saveOK = storageAccess.copyFile(inputStream,outputStream);
            } else {
                // Prepare a new XML version of the song from the statics (OpenSong song only)
                String newXML = processSong.getXML(song);
                saveOK = storageAccess.writeFileFromString(newXML, outputStream);
            }
        }

        // Update the default non persistent database for all songs (keeps menus and search up to date
        // Only update the song id/filename/folder if we:
        // Flagged to remove the original and save of the new file was successful
        if (!removeOriginal || !saveOK) {
            song.setFolder(oldfolder);
            song.setFilename(oldfilename);
            song.setSongid(commonSQL.getAnySongId(oldfolder, oldfilename));
        }
        sqLiteHelper.updateSong(c,commonSQL,song);

        // If it was a PDF/IMG, update the persistent database as well
        // If save wasn't successful, we sorted the folder/filename/id already
        if (imgOrPDF) {
            // If update fails (due to no existing row, a new one is created)
            nonOpenSongSQLiteHelper.updateSong(c,commonSQL,storageAccess,preferences,song);
        }

        // If we need to remove the original for non-pdfand the new file was successfully created, make the change
        if (saveOK && removeOriginal) {
            storageAccess.deleteFile(c,oldUri);
        }

        // If we are autologging CCLI information
        if (preferences.getMyPreferenceBoolean(c,"ccliAutomaticLogging",false)) {
            ccliLog.addEntry(c,preferences,storageAccess,song,"3"); // 3=edited
        }
        return saveOK;
    }

}
