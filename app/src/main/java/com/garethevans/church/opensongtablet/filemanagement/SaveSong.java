package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.io.InputStream;
import java.io.OutputStream;

public class SaveSong {

    public boolean doSave(Context c, Preferences preferences, StorageAccess storageAccess,
                          SongXML songXML, ConvertChoPro convertChoPro, ProcessSong processSong,
                          Song song, SQLiteHelper sqLiteHelper,
                          NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper, CommonSQL commonSQL,
                          CCLILog ccliLog, boolean imgOrPDF) {

        boolean saveOK = false;

        String where = storageAccess.safeFilename(songXML.getLocation(song.getFolder()));  // Either songs or a custom
        String folder = storageAccess.safeFilename(songXML.getLocation(song.getFolder())); // Removes the ../ from customs
        String filename = storageAccess.safeFilename(song.getFilename());
        String oldfolder = StaticVariables.whichSongFolder;
        String oldfilename = StaticVariables.songfilename;

        // If we are editing as chordpro, convert the lyrics back to back to OpenSong first
        if (preferences.getMyPreferenceBoolean(c, "editAsChordPro", false)) {
            song.setLyrics(convertChoPro.fromChordProToOpenSong(song.getLyrics()));
        }

        // Decide if we need to remove the original after writing the new song
        // This happens if the user has changed the filename or folder
        boolean removeOriginal = (!oldfolder.equals(folder) || !oldfilename.equals(filename)) &&
                (oldfolder!=null && !oldfolder.isEmpty() && oldfilename!=null && !oldfilename.isEmpty());

        Uri oldUri = storageAccess.getUriForItem(c,preferences,where,oldfolder,oldfilename);
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
                String newXML = songXML.getXML(song,processSong);
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

        // Update the folder and filename
        StaticVariables.whichSongFolder = folder;
        StaticVariables.songfilename = filename;

        // If we are autologging CCLI information
        if (preferences.getMyPreferenceBoolean(c,"ccliAutomaticLogging",false)) {
            ccliLog.addEntry(c,preferences,storageAccess,"3"); // 3=edited
        }
        return saveOK;
    }

}
