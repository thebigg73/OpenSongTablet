package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.nearby.ShareableObject;
import com.garethevans.church.opensongtablet.openchords.OpenChordsTag;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongId;

import java.io.File;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 11;
    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "SQLiteHelper";

    // Static helper to resolve the path BEFORE super() is called
    private static String getDatabasePath(Context c) {
        MainActivityInterface mai = (MainActivityInterface) c;
        File dbFile = mai.getStorageAccess().getAppSpecificFile("Database", "", SQLite.DATABASE_NAME);
        return dbFile.getAbsolutePath();
    }

    public SQLiteHelper(Context c) {
        // Now super is the first statement, satisfying Java requirements
        super(c, getDatabasePath(c), null, DATABASE_VERSION);
        this.mainActivityInterface = (MainActivityInterface) c;
        this.c = c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db != null) {
            try {
                db.execSQL(SQLite.CREATE_TABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Completely resets the song database by dropping and recreating the table.
     * Ideal for a full re-index from local files.
     */
    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME + ";");
            onCreate(db);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error resetting database", e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since songs.db is rebuilt from local files, this is safe
        db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME + ";");
        onCreate(db);
    }

    // Create, delete and update entries
    public void removeOldSongs(ArrayList<String> songIds) {
        try {
            mainActivityInterface.getCommonSQL().removeOldSongs(getWritableDatabase(), songIds);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error removing old songs", e);
        }
    }
    public void insertFast() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            mainActivityInterface.getCommonSQL().insertFast(db);
            db.setTransactionSuccessful();
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error performing fast insert", e);
        } finally {
            db.endTransaction();
        }
    }
    public void createSong(String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try {
            mainActivityInterface.getCommonSQL().createSong(getWritableDatabase(), folder, filename);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error creating song", e);
        }
    }
    public void updateSong(Song thisSong) {
        try {
            mainActivityInterface.getCommonSQL().updateSong(getWritableDatabase(), thisSong);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error updating song", e);
        }
    }
    public boolean deleteSong(String folder, String filename) {
        try {
            return mainActivityInterface.getCommonSQL().deleteSong(getWritableDatabase(), folder, filename) > -1;
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error deleting song", e);
            return false;
        }
    }

    public void renameSong(String oldFolder, String newFolder, String oldName, String newName) {
        try {
            mainActivityInterface.getCommonSQL().renameSong(getWritableDatabase(), oldFolder, newFolder, oldName, newName);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error renaming song", e);
        }
    }



    // Search for entries in the database
    public ArrayList<String> getFolders() {
        try {
            return mainActivityInterface.getCommonSQL().getFolders(getReadableDatabase());
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            return new ArrayList<>();
        }
    }
    public boolean songExists(String folder, String filename) {
        try {
            return mainActivityInterface.getCommonSQL().songExists(getReadableDatabase(), folder, filename);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            return false;
        }
    }
    public Song getSpecificSong(String folder, String filename) {
        try {
            return mainActivityInterface.getCommonSQL().getSpecificSong(getReadableDatabase(), folder, filename);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Song thisSong = new Song();
            thisSong.setFolder(folder);
            thisSong.setFilename(filename);
            thisSong.setSongid(mainActivityInterface.getCommonSQL().getAnySongId(folder, filename));
            return thisSong;
        }
    }
    public Song getSongByUuid(String uuid) {
        try {
            return mainActivityInterface.getCommonSQL().getSongFromUuid(getReadableDatabase(), uuid);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            return null;
        }
    }

    public ArrayList<Song> getSongsByFilters(boolean searchByFolder, boolean searchByArtist,
                                             boolean searchByKey, boolean searchByTag,
                                             boolean searchByFilter, boolean searchByTitle,
                                             String folderVal, String artistVal, String keyVal,
                                             String tagVal, String filterVal, String titleVal,
                                             boolean songMenuSortTitles) {

        try {
           return mainActivityInterface.getCommonSQL().getSongsByFilters(getReadableDatabase(), searchByFolder,
                   searchByArtist, searchByKey, searchByTag, searchByFilter, searchByTitle,
                    folderVal, artistVal, keyVal, tagVal, filterVal, titleVal, songMenuSortTitles);
        } catch (OutOfMemoryError | Exception e) {
            Log.d(TAG,"Table doesn't exist");
            resetDatabase();
            return new ArrayList<>();
        }
    }
    public ArrayList<Song> openChordsSyncGetSongsFromFolder(String folder) {
        try {
            // Retrieve the persistent instance without using try-with-resources
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().openChordsSyncGetSongsFromFolder(db, folder);
        } catch (Exception | OutOfMemoryError e) {
            Log.e(TAG, "Error syncing songs from folder: " + folder, e);
            return new ArrayList<>();
        }
    }
    public Song getOpenChordsSong(String folder, String uuid) {
        try {
            // Retrieve the persistent instance without using try-with-resources
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getOpenChordsSong(db, folder, uuid);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error fetching OpenChords song: " + folder + "/" + uuid, e);
            return null;
        }
    }

    public String[] getUuidFromFolderAndFile(String folderAndFile) {
        try {
            // Retrieve the persistent instance without using try-with-resources
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getUuidFromFolderAndFile(db, folderAndFile);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error getting UUID for: " + folderAndFile, e);
            return null;
        }
    }

    public ArrayList<OpenChordsTag> getThemesFromFilesInFolder(String folder) {
        try {
            // Retrieve the persistent instance without using try-with-resources
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getThemesFromFilesInFolder(db, folder);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error fetching themes from folder: " + folder, e);
            return new ArrayList<>();
        }
    }

    public String getKey(String folder, String filename) {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getKey(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error getting key for song: " + folder + "/" + filename, e);
            return "";
        }
    }
    public ArrayList<String> getThemeTags() {
        try {
            // Retrieve the persistent instance without using try-with-resources
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getUniqueThemeTags(db);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error fetching unique theme tags", e);
            return new ArrayList<>();
        }
    }


    public ArrayList<String> renameThemeTags(String oldTag, String newTag) {
        // Retrieve helpers
        NonOpenSongSQLiteHelper nonOsHelper = mainActivityInterface.getNonOpenSongSQLiteHelper();

        // Retrieve persistent database instances
        SQLiteDatabase db = getWritableDatabase();
        SQLiteDatabase db2 = nonOsHelper.getWritableDatabase();

        // Wrap in a transaction because we are modifying two different files
        db.beginTransaction();
        db2.beginTransaction();

        try {
            ArrayList<String> result = mainActivityInterface.getCommonSQL().renameThemeTags(db, db2, oldTag, newTag);
            db.setTransactionSuccessful();
            db2.setTransactionSuccessful();
            return result;
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error renaming theme tags", e);
            return new ArrayList<>();
        } finally {
            db.endTransaction();
            db2.endTransaction();
            // Do NOT call close() here
        }
    }


    public String songsWithThemeTags(String tag) {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getSongsWithThemeTag(db, tag);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error fetching songs with theme tag: " + tag, e);
            return "";
        }
    }

    public String getFolderForSong(String filename) {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getFolderForSong(db, filename);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error getting folder for song: " + filename, e);
            return c.getString(R.string.mainfoldername);
        }
    }

    public Song getSongFromMidiIndex(int midiIndex) {
        try {
            // Retrieve the persistent instance without using try-with-resources
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getSongFromMidiIndex(db, midiIndex);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error fetching song by MIDI index: " + midiIndex, e);
            return null;
        }
    }
    public void exportDatabase() {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            mainActivityInterface.getCommonSQL().exportDatabase(db, "SongDatabase.csv");
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error exporting database to CSV", e);
        }
    }

    public ArrayList<ShareableObject> getShareableSongs() {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getShareableSongs(db);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error fetching shareable songs", e);
            return new ArrayList<>();
        }
    }
    public String[] getSongCreationInfo(String folder, String filename) {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getSongCreationInfo(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error getting creation info for: " + filename, e);
            return new String[] {"", "", "false"};
        }
    }

    public ArrayList<SongId> getSongIds() {
        // Create an array of simple song details - used for the web server
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getSongIds(db);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error fetching song IDs", e);
            return new ArrayList<>();
        }
    }
}
