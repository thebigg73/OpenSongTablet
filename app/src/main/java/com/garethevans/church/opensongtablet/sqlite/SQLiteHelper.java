package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.File;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 3;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "SQLiteHelper";

    public SQLiteHelper(Context c) {
        // Don't create the database here as we don't want to recreate on each call.
        super(c,  SQLite.DATABASE_NAME, null, DATABASE_VERSION);
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // If the table doesn't exist, create it.
        if (db!=null) {
            try {
                db.execSQL(SQLite.CREATE_TABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME + ";");

        // Create tables again
        onCreate(db);
    }



    // Create and reset the database
    public SQLiteDatabase getDB() {
        try {
            File f = new File(c.getExternalFilesDir("Database"), SQLite.DATABASE_NAME);
            return SQLiteDatabase.openOrCreateDatabase(f, null);
        } catch (OutOfMemoryError | Exception e) {
            return null;
        }
    }
    void emptyTable(SQLiteDatabase db) {
        // This drops the table if it exists (wipes it ready to start again)
        if (db!=null) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME + ";");
            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void resetDatabase() {
        try (SQLiteDatabase db = getDB()) {
            emptyTable(db);
            onCreate(db);
        }
    }

    // Create, delete and update entries
    public void insertFast() {
        SQLiteDatabase db = getDB();
        try {
            mainActivityInterface.getCommonSQL().insertFast(db);
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
    public void createSong(String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db = getDB()) {
            mainActivityInterface.getCommonSQL().createSong(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            // Likely the song exists and we didn't check!
        }
    }
    public void updateSong(Song thisSong) {
        try (SQLiteDatabase db = getDB()) {
            mainActivityInterface.getCommonSQL().updateSong(db,thisSong);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }
    public boolean deleteSong(String folder, String file) {
        try (SQLiteDatabase db = getDB()) {
            return mainActivityInterface.getCommonSQL().deleteSong(db, folder, file) > -1;
        } catch (OutOfMemoryError | Exception e) {
            return false;
        }
    }
    public boolean renameSong(String oldFolder, String newFolder, String oldName, String newName) {
        try (SQLiteDatabase db = getDB()) {
            return mainActivityInterface.getCommonSQL().renameSong(db, oldFolder,newFolder,oldName,newName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    // Search for entries in the database
    public ArrayList<String> getFolders() {
        // Get the database
        try (SQLiteDatabase db = getDB()) {
            return mainActivityInterface.getCommonSQL().getFolders(db);
        } catch (OutOfMemoryError | Exception e) {
            Log.d(TAG,"SQLite error - likely DB doesn't exist yet");
            //e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public boolean songExists(String folder, String filename) {
        try (SQLiteDatabase db = getDB()) {
            return mainActivityInterface.getCommonSQL().songExists(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Song getSpecificSong(String folder, String filename) {
        try (SQLiteDatabase db = getDB()) {
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
    public ArrayList<Song> getSongsByFilters(boolean searchByFolder, boolean searchByArtist,
                                             boolean searchByKey, boolean searchByTag,
                                             boolean searchByFilter, boolean searchByTitle,
                                             String folderVal, String artistVal, String keyVal,
                                             String tagVal, String filterVal, String titleVal,
                                             boolean songMenuSortTitles) {

        try (SQLiteDatabase db = getDB()) {
           return mainActivityInterface.getCommonSQL().getSongsByFilters(db, searchByFolder,
                   searchByArtist, searchByKey, searchByTag, searchByFilter, searchByTitle,
                    folderVal, artistVal, keyVal, tagVal, filterVal, titleVal, songMenuSortTitles);
        } catch (OutOfMemoryError | Exception e) {
            Log.d(TAG,"Table doesn't exist");
            return new ArrayList<>();
        }
    }
    public String getKey(String folder, String filename) {
        try (SQLiteDatabase db = getDB()) {
            return mainActivityInterface.getCommonSQL().getKey(db, folder, filename);
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
            return "";
        }
    }
    public ArrayList<String> getThemeTags() {
        // Get unique theme tags
        try (SQLiteDatabase db = getDB()) {
            return mainActivityInterface.getCommonSQL().getUniqueThemeTags(db);
        } catch (OutOfMemoryError | Exception e) {
            return new ArrayList<>();
        }
    }
    public ArrayList<String> renameThemeTags(String oldTag, String newTag) {
        // Rename matching tags if found and don't already exist
        try (SQLiteDatabase db = getDB(); SQLiteDatabase db2 = mainActivityInterface.getNonOpenSongSQLiteHelper().getDB()) {
            return mainActivityInterface.getCommonSQL().renameThemeTags(db, db2, oldTag, newTag);
        } catch (OutOfMemoryError | Exception e) {
            return new ArrayList<>();
        }
    }
    public String songsWithThemeTags(String tag) {
        try (SQLiteDatabase db = getDB()) {
            return mainActivityInterface.getCommonSQL().getSongsWithThemeTag(db, tag);
        } catch (OutOfMemoryError | Exception e) {
            return "";
        }
    }

    public String getFolderForSong(String filename) {
        // Set the default folder
        try (SQLiteDatabase db = getDB()) {
            return mainActivityInterface.getCommonSQL().getFolderForSong(db, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return c.getString(R.string.mainfoldername);
        }
    }
}


