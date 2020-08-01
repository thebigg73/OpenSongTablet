package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class NonOpenSongSQLiteHelper extends SQLiteOpenHelper {

    public NonOpenSongSQLiteHelper(Context context) {
        super(context, SQLite.NON_OS_DATABASE_NAME, null, DATABASE_VERSION);
        // Don't create the database here as we don't want to recreate on each call.
    }

    // Database Version
    private static final int DATABASE_VERSION = 2;  // THIS GETS
    private SQLiteDatabase getDB(Context c, CommonSQL commonSQL, StorageAccess storageAccess, Preferences preferences) {
        Uri uri = storageAccess.getUriForItem(c,preferences,"Settings","", SQLite.NON_OS_DATABASE_NAME);
        storageAccess.lollipopCreateFileForOutputStream(c,preferences,uri,null,"Settings","", SQLite.NON_OS_DATABASE_NAME);
        SQLiteDatabase db2 = SQLiteDatabase.openOrCreateDatabase(uri.getPath(),null);
        if (db2.getVersion()!=DATABASE_VERSION) {
            // Check we have the columns we need!
            commonSQL.updateTable(db2);
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
    public void initialise(Context c, CommonSQL commonSQL, StorageAccess storageAccess, Preferences preferences) {
        // If the database doesn't exist, create it
        try (SQLiteDatabase db2 = getDB(c,commonSQL,storageAccess,preferences)) {
            onCreate(db2);
        }
    }



    // Create, delete and update entries
    public void createSong(Context c, CommonSQL commonSQL, StorageAccess storageAccess,
                                Preferences preferences, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db2 = getDB(c,commonSQL,storageAccess,preferences)) {
            commonSQL.createSong(c, storageAccess, db2, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }
    public boolean deleteSong(Context c, CommonSQL commonSQL, StorageAccess storageAccess, Preferences preferences, String folder, String filename) {
        int i;
        try (SQLiteDatabase db2 = getDB(c,commonSQL,storageAccess,preferences)) {
            i = commonSQL.deleteSong(db2,folder,filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return false;
        }
        return i > -1;
    }
    public void updateSong(Context c, CommonSQL commonSQL, StorageAccess storageAccess,
                           Preferences preferences, SQLite sqLite) {
        try (SQLiteDatabase db2 = getDB(c,commonSQL,storageAccess,preferences)) {
            commonSQL.updateSong(db2, sqLite);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }



    // TODO MIGHT REMOVE AS THE CONTENTS OF THIS DATABASE ARE PULLED INTO THE MAIN ONE AT RUNTIME
    // Find specific song
    public SQLite getSpecificSong(Context c, SQLiteHelper sqLiteHelper, CommonSQL commonSQL,
                                  StorageAccess storageAccess, Preferences preferences,
                                  String folder, String filename) {
        // This gets basic info from the normal temporary SQLite database
        // It then also adds in any extra stuff found in the NonOpenSongSQLite database
        SQLite thisSong = new SQLite();
        String songId = commonSQL.getAnySongId(folder,filename);
        try (SQLiteDatabase db = sqLiteHelper.getDB(c)) {
            // Get the basics - error here returns the basic stuff as an exception
            thisSong = commonSQL.getSpecificSong(db,folder,filename);

            // Now look to see if there is extra information in the saved NonOpenSongDatabase
            try (SQLiteDatabase db2 = getDB(c,commonSQL,storageAccess,preferences)) {
                if (commonSQL.songExists(db2,folder,filename)) {
                    // Get the more detailed values for the PDF/Image
                    thisSong = commonSQL.getSpecificSong(db2,folder,filename);

                    // Update the values in the temporary main database (used for song menu and features)
                    commonSQL.updateSong(db,thisSong);
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
    }

    // TODO Flush entries that aren't in the filesystem, or alert the user to issues (perhaps asking to update the entry?

}
