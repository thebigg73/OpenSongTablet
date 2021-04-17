package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
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
        try (SQLiteDatabase db = getDB(c)) {
            emptyTable(db);
            onCreate(db);
        }
    }



    // Create, delete and update entries
    public void insertFast(Context c, MainActivityInterface mainActivityInterface) {
        SQLiteDatabase db = getDB(c);
        try {
            mainActivityInterface.getCommonSQL().insertFast(c,mainActivityInterface,db);
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
    public void createSong(Context c, MainActivityInterface mainActivityInterface, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db = getDB(c)) {
            mainActivityInterface.getCommonSQL().createSong(c, mainActivityInterface, db, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            // Likely the song exists and we didn't check!
        }
    }
    public void updateSong(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        try (SQLiteDatabase db = getDB(c)) {
            mainActivityInterface.getCommonSQL().updateSong(db,thisSong);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }
    public boolean deleteSong(Context c, MainActivityInterface mainActivityInterface, String folder, String file) {
        try (SQLiteDatabase db = getDB(c)) {
            return mainActivityInterface.getCommonSQL().deleteSong(db, folder, file) > -1;
        } catch (OutOfMemoryError | Exception e) {
            return false;
        }
    }
    public boolean renameSong(Context c, MainActivityInterface mainActivityInterface,
                              String oldFolder, String newFolder, String oldName, String newName) {
        try (SQLiteDatabase db = getDB(c)) {
            return mainActivityInterface.getCommonSQL().renameSong(db,oldFolder,newFolder,oldName,newName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    // Search for entries in the database
    public ArrayList<String> getFolders(Context c, MainActivityInterface mainActivityInterface) {
        // Get the database
        try (SQLiteDatabase db = getDB(c)) {
            return mainActivityInterface.getCommonSQL().getFolders(db);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public boolean songExists(Context c, MainActivityInterface mainActivityInterface, String folder, String filename) {
        try (SQLiteDatabase db = getDB(c)) {
            return mainActivityInterface.getCommonSQL().songExists(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Song getSpecificSong(Context c, MainActivityInterface mainActivityInterface, String folder, String filename) {
        try (SQLiteDatabase db = getDB(c)) {
            return mainActivityInterface.getCommonSQL().getSpecificSong(db,folder,filename);
        } catch (OutOfMemoryError | Exception e) {
            Song thisSong = new Song();
            thisSong.setFolder(folder);
            thisSong.setFilename(filename);
            String songId = mainActivityInterface.getCommonSQL().getAnySongId(folder,filename);
            thisSong.setSongid(songId);
            return thisSong;
        }
    }
    public ArrayList<Song> getSongsByFilters(Context c, MainActivityInterface mainActivityInterface,
                                             boolean searchByFolder, boolean searchByArtist,
                                             boolean searchByKey, boolean searchByTag,
                                             boolean searchByFilter, String folderVal, String artistVal,
                                             String keyVal, String tagVal, String filterVal) {

        try (SQLiteDatabase db = getDB(c)) {
           return mainActivityInterface.getCommonSQL().getSongsByFilters(c, db, searchByFolder, searchByArtist, searchByKey, searchByTag, searchByFilter,
                    folderVal, artistVal, keyVal, tagVal, filterVal);
        } catch (OutOfMemoryError | Exception e) {
            return new ArrayList<>();
        }
    }

}


