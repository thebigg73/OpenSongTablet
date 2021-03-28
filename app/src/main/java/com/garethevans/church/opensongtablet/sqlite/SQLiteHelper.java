package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.File;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    public SQLiteHelper(Context context) {
        super(context,  SQLite.DATABASE_NAME, null, DATABASE_VERSION);
        // Don't create the database here as we don't want to recreate on each call.
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        // If the table doesn't exist, create it.
        db.execSQL(SQLite.CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }



    // Create and reset the database
    public SQLiteDatabase getDB(Context c) {
        try {
            File f = new File(c.getExternalFilesDir("Database"), SQLite.DATABASE_NAME);
            return SQLiteDatabase.openOrCreateDatabase(f, null);
        } catch (OutOfMemoryError | Exception e) {
            return null;
        }
    }
    void emptyTable(SQLiteDatabase db) {
        // This drops the table if it exists (wipes it ready to start again)
        try {
            db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }
    public void resetDatabase(Context c) {
        Log.d("SQLiteHelper","resetDatabase");
        try (SQLiteDatabase db = getDB(c)) {
            emptyTable(db);
            Log.d("SQLiteHelper","emptyTable");
            onCreate(db);
            Log.d("SQLiteHelper","onCreate");
        }
    }



    // Create, delete and update entries
    public void insertFast(Context c, CommonSQL commonSQL, StorageAccess storageAccess) {
        SQLiteDatabase db = getDB(c);
        try {
            commonSQL.insertFast(c,db,storageAccess);
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (OutOfMemoryError | Exception e) {
            db.setTransactionSuccessful();
            db.endTransaction();
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
    public void createSong(Context c, StorageAccess storageAccess, CommonSQL commonSQL, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db = getDB(c)) {
            commonSQL.createSong(c, storageAccess, db, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            // Likely the song exists and we didn't check!
        }
    }
    public void updateSong(Context c, CommonSQL commonSQL, Song song) {
        try (SQLiteDatabase db = getDB(c)) {
            commonSQL.updateSong(db,song);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }
    public boolean deleteSong(Context c, CommonSQL commonSQL, String folder, String file) {
        try (SQLiteDatabase db = getDB(c)) {
            return commonSQL.deleteSong(db, folder, file) > -1;
        } catch (OutOfMemoryError | Exception e) {
            return false;
        }
    }




    // Search for entries in the database
    public ArrayList<String> getFolders(Context c, CommonSQL commonSQL) {
        // Get the database
        try (SQLiteDatabase db = getDB(c)) {
            return commonSQL.getFolders(db);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public boolean songExists(Context c, CommonSQL commonSQL, String folder, String filename) {
        try (SQLiteDatabase db = getDB(c)) {
            return commonSQL.songExists(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Song getSpecificSong(Context c, CommonSQL commonSQL, String folder, String filename) {
        try (SQLiteDatabase db = getDB(c)) {
            return commonSQL.getSpecificSong(db,folder,filename);
        } catch (OutOfMemoryError | Exception e) {
            Song thisSong = new Song();
            thisSong.setFolder(folder);
            thisSong.setFilename(filename);
            String songId = commonSQL.getAnySongId(folder,filename);
            thisSong.setSongid(songId);
            return thisSong;
        }
    }
    public ArrayList<Song> getSongsByFilters(Context c, CommonSQL commonSQL, boolean searchByFolder, boolean searchByArtist,
                                               boolean searchByKey, boolean searchByTag,
                                               boolean searchByFilter, String folderVal, String artistVal,
                                               String keyVal, String tagVal, String filterVal) {

        try (SQLiteDatabase db = getDB(c)) {
           return commonSQL.getSongsByFilters(c, db, searchByFolder, searchByArtist, searchByKey, searchByTag, searchByFilter,
                    folderVal, artistVal, keyVal, tagVal, filterVal);
        } catch (OutOfMemoryError | Exception e) {
            return new ArrayList<>();
        }
    }

}


