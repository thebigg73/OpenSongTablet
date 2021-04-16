package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.File;

public class NonOpenSongSQLiteHelper extends SQLiteOpenHelper {

    public NonOpenSongSQLiteHelper(Context context) {
        super(context, SQLite.NON_OS_DATABASE_NAME, null, DATABASE_VERSION);
        // Don't create the database here as we don't want to recreate on each call.
    }

    // Database Version
    private static final int DATABASE_VERSION = 2;  // THIS GETS
    private SQLiteDatabase getDB(Context c, MainActivityInterface mainActivityInterface) {
        // Make sure we have the persistent version ready
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface.getPreferences(),"Settings","", SQLite.NON_OS_DATABASE_NAME);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c,mainActivityInterface.getPreferences(),uri,null,"Settings","", SQLite.NON_OS_DATABASE_NAME);
        // The version we use has to be in local app storage unfortunately.  We can copy this though
        File f = new File(c.getExternalFilesDir("Database"), SQLite.NON_OS_DATABASE_NAME);
        SQLiteDatabase db2 = SQLiteDatabase.openOrCreateDatabase(f,null);
        if (db2.getVersion()!=DATABASE_VERSION) {
            // Check we have the columns we need!
            db2.setVersion(DATABASE_VERSION);
            mainActivityInterface.getCommonSQL().updateTable(db2);
        }
        return db2;
    }
    @Override
    public void onCreate(SQLiteDatabase db2) {
        // If the table doesn't exist, create it.
        db2.execSQL(SQLite.CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db2, int oldVersion, int newVersion) {
        // Do nothing here as we manually update the table to match
    }
    public void initialise(Context c, MainActivityInterface mainActivityInterface) {
        // If the database doesn't exist, create it
        try (SQLiteDatabase db2 = getDB(c,mainActivityInterface)) {
            onCreate(db2);
        }
    }



    // Create, delete and update entries
    public void createSong(Context c, MainActivityInterface mainActivityInterface, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db2 = getDB(c,mainActivityInterface)) {
            mainActivityInterface.getCommonSQL().createSong(c, mainActivityInterface, db2, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }
    public boolean deleteSong(Context c, MainActivityInterface mainActivityInterface, String folder, String filename) {
        int i;
        try (SQLiteDatabase db2 = getDB(c,mainActivityInterface)) {
            i = mainActivityInterface.getCommonSQL().deleteSong(db2,folder,filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return false;
        }
        return i > -1;
    }
    public void updateSong(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        try (SQLiteDatabase db2 = getDB(c,mainActivityInterface)) {
            mainActivityInterface.getCommonSQL().updateSong(db2,thisSong);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }



    // TODO MIGHT REMOVE AS THE CONTENTS OF THIS DATABASE ARE PULLED INTO THE MAIN ONE AT RUNTIME
    // Find specific song
    /*public Song getSpecificSong(Context c, MainActivityInterface mainActivityInterface,
                                String folder, String filename) {
        // This gets basic info from the normal temporary SQLite database
        // It then also adds in any extra stuff found in the NonOpenSongSQLite database
        Song thisSong = new Song();
        String songId = mainActivityInterface.getCommonSQL().getAnySongId(folder,filename);
        try (SQLiteDatabase db = mainActivityInterface.getSQLiteHelper().getDB(c)) {
            // Get the basics - error here returns the basic stuff as an exception
            thisSong = mainActivityInterface.getCommonSQL().getSpecificSong(db,folder,filename);

            // Now look to see if there is extra information in the saved NonOpenSongDatabase
            try (SQLiteDatabase db2 = getDB(c,mainActivityInterface)) {
                if (mainActivityInterface.getCommonSQL().songExists(db2,folder,filename)) {
                    // Get the more detailed values for the PDF/Image
                    thisSong = mainActivityInterface.getCommonSQL().getSpecificSong(db2,folder,filename);

                    // Update the values in the temporary main database (used for song menu and features)
                    mainActivityInterface.getCommonSQL().updateSong(db,thisSong);
                }
            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
                thisSong.setFolder(folder);
                thisSong.setFilename(filename);
                thisSong.setSongid(songId);
            }
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            thisSong.setFolder(folder);
            thisSong.setFilename(filename);
            thisSong.setSongid(songId);
        }
        return thisSong;
    }*/

    // TODO Flush entries that aren't in the filesystem, or alert the user to issues (perhaps asking to update the entry?

}
