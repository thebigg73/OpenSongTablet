package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLite;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.io.OutputStream;

public class SaveSong {

    public boolean doSave(Context c, Preferences preferences, StorageAccess storageAccess,
                          EditContent editContent, SongXML songXML, ConvertChoPro convertChoPro,
                          ProcessSong processSong, SQLite sqLite, SQLiteHelper sqLiteHelper,
                          NonOpenSongSQLite nonOpenSongSQLite, NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper,
                          CCLILog ccliLog, boolean imgOrPDF) {

        boolean saveOK = false;
        String songid = StaticVariables.whichSongFolder.replace("'", "''") + "/" +
                StaticVariables.songfilename.replace("'", "''"); // make it SQL friendly

        // If we are editing as chordpro, convert to OpenSong
        if (preferences.getMyPreferenceBoolean(c, "editAsChordPro", false)) {
            editContent.setLyrics(convertChoPro.fromChordProToOpenSong(editContent.getLyrics()));
        }
        // Set the static variables to match (new song will be created from these)
        editContent.updateStatics();

        String newXML = songXML.getXML(processSong);

        // Makes sure all & are replaced with &amp;
        newXML = newXML.replace("&amp;", "&");
        newXML = newXML.replace("&", "&amp;");

        String where = songXML.getLocation(editContent.getFolder());  // Either songs or a custom
        String folder = songXML.getLocation(editContent.getFolder()); // Removes the ../ from customs

        if (!imgOrPDF) {
            // For proper songs (not images or PDFs)
            Uri uri = storageAccess.getUriForItem(c, preferences, where, folder, editContent.getTitle());
            Log.d("EditSongFragment", "where=" + where + "\nfolder=" + folder + "\nsongfilename=" + StaticVariables.songfilename);
            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null,
                    where, folder, StaticVariables.songfilename);

            OutputStream outputStream = storageAccess.getOutputStream(c, uri);
            saveOK = storageAccess.writeFileFromString(newXML, outputStream);
        }

        // If this isn't a song (a set item, variation, etc, it won't be in the database, so the following will fail
        // That's ok though!!  We don't want to update search indexes or song menus
        try {
            if (imgOrPDF) {
                // Image or PDF
                if (nonOpenSongSQLiteHelper != null) {
                    nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(c, storageAccess, preferences, songid);
                } else if (nonOpenSongSQLiteHelper == null) {
                    nonOpenSongSQLiteHelper.createBasicSong(c, storageAccess, preferences,
                            StaticVariables.whichSongFolder, StaticVariables.songfilename);
                    nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(c, storageAccess, preferences,
                            StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename);
                }

                if (nonOpenSongSQLite != null && nonOpenSongSQLite.getId() > -1) {
                    Log.d("d", "id=" + nonOpenSongSQLite.getId());
                    nonOpenSongSQLite.setId(nonOpenSongSQLite.getId());
                    nonOpenSongSQLite.setFolder(StaticVariables.whichSongFolder);
                    nonOpenSongSQLite.setFilename(StaticVariables.songfilename);
                    nonOpenSongSQLite.setSongid(songid);
                    nonOpenSongSQLiteHelper.updateSong(c, storageAccess, preferences, nonOpenSongSQLite);
                }


            } else {
                // Proper songs
                if (sqLiteHelper != null) {
                    sqLiteHelper.getSong(c, songid);
                }
                if (sqLite != null && sqLite.getId() > -1) {
                    Log.d("d", "id=" + sqLite.getId());
                    sqLite.setId(sqLite.getId());
                    sqLite.setFolder(StaticVariables.whichSongFolder);
                    sqLite.setFilename(StaticVariables.songfilename);
                    sqLite.setSongid(StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename);
                    sqLite.setTitle(StaticVariables.mTitle);
                    sqLite.setAuthor(StaticVariables.mAuthor);
                    sqLite.setCopyright(StaticVariables.mCopyright);
                    sqLite.setLyrics(StaticVariables.mLyrics);
                    sqLite.setHymn_num(StaticVariables.mHymnNumber);
                    sqLite.setCcli(StaticVariables.mCCLI);
                    sqLite.setTheme(StaticVariables.mTheme);
                    sqLite.setAlttheme(StaticVariables.mAltTheme);
                    sqLite.setUser1(StaticVariables.mUser1);
                    sqLite.setUser2(StaticVariables.mUser2);
                    sqLite.setUser3(StaticVariables.mUser3);
                    sqLite.setKey(StaticVariables.mKey);
                    sqLite.setTimesig(StaticVariables.mTimeSig);
                    sqLite.setAka(StaticVariables.mAka);
                    sqLiteHelper.updateSong(c, sqLite);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If we are autologging CCLI information
        if (preferences.getMyPreferenceBoolean(c,"ccliAutomaticLogging",false)) {
            ccliLog.addEntry(c,preferences,storageAccess,"3"); // 3=edited
        }
        return saveOK;
    }
}
