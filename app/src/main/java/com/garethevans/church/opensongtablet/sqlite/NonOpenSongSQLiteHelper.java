package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.drummer.DrumCalculations;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.utilities.CleanDatabaseBottomSheet;
import com.garethevans.church.opensongtablet.utilities.DatabaseUtilitiesFragment;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NonOpenSongSQLiteHelper extends SQLiteOpenHelper {

    private Uri appDB, userDB; // appDB is app hidden (but useable), userDB is one in OpenSong/Settings
    private File appDBFile;
    public static final String TAG = "NonOSSQLHelper";
    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    private boolean initialiseUserDB = false;

    // Database Version
    private static final int DATABASE_VERSION = 11;

    private static String getDatabasePath(Context c) {
        MainActivityInterface mai = (MainActivityInterface) c;
        File dbFile = mai.getStorageAccess().getAppSpecificFile("Database", "", SQLite.NON_OS_DATABASE_NAME);
        return dbFile.getAbsolutePath();
    }

    public NonOpenSongSQLiteHelper(Context c) {
        super(c, getDatabasePath(c), null, DATABASE_VERSION);
        this.mainActivityInterface = (MainActivityInterface) c;
        this.c = c;

        // Get a reference to the database files/uris (app and user)
        getDatabaseUris();

        // Ensure the database is imported/synced from user storage before use
        // Check for a previous version in user storage
        // If it exists and isn't empty, copy it in to the appDB
        // If it doesn't exist, or is empty copy our appDB to the userDB
        importDatabase();
    }

    private void getDatabaseUris() {
        appDBFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Database","",SQLite.NON_OS_DATABASE_NAME);

        appDB = Uri.fromFile(appDBFile);
        Log.d(TAG,"starting trying to get appDB local");
        userDB = mainActivityInterface.getStorageAccess().getUriForItem(
                "Settings", "", SQLite.NON_OS_DATABASE_NAME);
        Log.d(TAG,"finished trying to get appDB local");

        // If the userDB uri doesn't exist, copy the appDB now it is ready
        if (!mainActivityInterface.getStorageAccess().uriExists(userDB)) {
            initialiseUserDB = true;
            copyUserDatabase();
        }
    }

    private void importDatabase() {
        // This copies in the version in the settings folder if it exists and isn't empty
        boolean copied;
        if (appDB==null || userDB==null || appDBFile==null) {
            getDatabaseUris();
        }

        if (mainActivityInterface.getStorageAccess().uriTreeValid(userDB) && mainActivityInterface.getStorageAccess().uriExists(userDB) &&
                mainActivityInterface.getStorageAccess().getFileSizeFromUri(userDB)>0) {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(userDB);
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(appDB);
            copied = mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" importDatabase copyFile from "+userDB+" to "+appDB+": "+copied);
        } else if (mainActivityInterface.getStorageAccess().uriTreeValid(userDB)){
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" importDatabse Create Settings/"+SQLite.NON_OS_DATABASE_NAME+" deleteOld=false");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, userDB,null,"Settings","",
                    SQLite.NON_OS_DATABASE_NAME);
            copied = copyUserDatabase();
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+": Create new "+SQLite.NON_OS_DATABASE_NAME+" at OpenSong/Settings/ and copy to appCache - success: "+copied);
        }

        // Check we have the columns we need (match to the latest version)!

        // After the file is copied into appDBFile, we simply open it.
        // getWritableDatabase() will automatically compare the version of the file
        // you just copied with DATABASE_VERSION.
        // If they differ, it triggers onUpgrade() automatically.
        try {
            Log.d(TAG, "Database imported and verified version: " + getWritableDatabase().getVersion());
        } catch (Exception e) {
            Log.e(TAG, "Error verifying imported database", e);
        }
    }

    public void importDB(String dbToImport, boolean overwrite) {
        // 1. Force external file upgrade (this is fine as a standalone, short-lived helper)
        SQLiteOpenHelper tempHelper = new SQLiteOpenHelper(c, dbToImport, null, DATABASE_VERSION) {
            @Override public void onCreate(SQLiteDatabase db) {}
            @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                NonOpenSongSQLiteHelper.applySurgicalUpgrade(db, oldVersion);
            }
        };
        tempHelper.getWritableDatabase().close(); // Safe to close here because it's a specific, temp helper
        tempHelper.close();

        // 2. Perform the merge using the application's persistent database reference
        // Defensive check
        SQLiteDatabase currentDB = getWritableDatabase();
        if (currentDB == null) {
            Log.e(TAG, "Could not get writable database for import");
            return;
        }

        currentDB.beginTransaction();

        try {
            currentDB.execSQL("ATTACH DATABASE '" + dbToImport + "' AS tempDb");

            String sql = (overwrite)
                    ? "REPLACE INTO main." + SQLite.TABLE_NAME + " SELECT * FROM tempDb." + SQLite.TABLE_NAME
                    : "INSERT OR IGNORE INTO main." + SQLite.TABLE_NAME + " SELECT * FROM tempDb." + SQLite.TABLE_NAME;

            currentDB.execSQL(sql);
            currentDB.execSQL("DETACH DATABASE tempDb");

            currentDB.setTransactionSuccessful();
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Database import failed", e);
        } finally {
            currentDB.endTransaction();
            // Do NOT call currentDB.close()!
        }
    }


    public boolean copyUserDatabase() {
        // This copies the app persistent database (app cache) into the user's OpenSong/Settings folder
        // GE It should only need done at app close, since it is never used directly

        // In case there was an issue and the Uris are null, get them again
        if ((appDB==null || userDB==null) && !initialiseUserDB) {
            getDatabaseUris();
        }

        Log.d(TAG,"copyUserDatabase() check uriTreeValid for userDB");
        if (mainActivityInterface.getStorageAccess().uriTreeValid(userDB) ||
                !mainActivityInterface.getStorageAccess().uriExists(userDB)) {

            Log.d(TAG,"getInputStream");
            // Get an input stream for the app database so we can copy it
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(appDB);

            Log.d(TAG,"check if exists getInputStream");

            // Make sure the userDB file exists if it isn't there - may not have been used before
            if (!mainActivityInterface.getStorageAccess().uriExists(userDB)) {
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                        false, userDB, null, "Settings", "",
                        SQLite.NON_OS_DATABASE_NAME);
            }

            // Get an output stream for the userDB to copy into
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(userDB);

            // If all is well, attempt the copy
            boolean copied;
            if (inputStream != null && outputStream != null) {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " copyNonOpenSongAppDB copyFile from " + appDB + " to " + userDB);
                copied = mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
                Log.d(TAG, "Copy user database " + SQLite.NON_OS_DATABASE_NAME + " from " + appDB + " to " + userDB + " - success:" + copied);
            } else {
                copied = false;
            }
            return copied;
        }
        return false;
    }

    @Override
    public void onCreate(SQLiteDatabase db2) {
        db2.execSQL(SQLite.CREATE_TABLE);
    }

    public void initialise() {
        try {
            // Just calling this triggers the SQLiteOpenHelper lifecycle.
            // Do NOT use 'try (...)', because that calls close() automatically.
            getWritableDatabase();
            Log.d(TAG, "Database initialized successfully.");
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            // Keep the catch block for hardware/IO errors.
            Log.e(TAG, "Database initialization failed", e);
        }
    }


    public static void applySurgicalUpgrade(SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 4) addColumnIfMissing(db, SQLite.COLUMN_KEY_ORIGINAL, "TEXT");
        if (oldVersion < 6) {
            addColumnIfMissing(db, SQLite.COLUMN_BEATBUDDY_SONG, "TEXT");
            addColumnIfMissing(db, SQLite.COLUMN_BEATBUDDY_KIT, "TEXT");
            addColumnIfMissing(db, SQLite.COLUMN_ABC_TRANSPOSE, "TEXT");
            addColumnIfMissing(db, SQLite.COLUMN_PREFERRED_INSTRUMENT, "TEXT");
        }
        if (oldVersion < 7) addColumnIfMissing(db, SQLite.COLUMN_UUID, "TEXT");
        if (oldVersion < 8) addColumnIfMissing(db, SQLite.COLUMN_LAST_MODIFIED, "TEXT");
        if (oldVersion < 9) addColumnIfMissing(db, SQLite.COLUMN_PREVIEWOVERRIDE, "TEXT");
        if (oldVersion < 11) {
            addColumnIfMissing(db, SQLite.COLUMN_DRUMMER, "TEXT");
            addColumnIfMissing(db, SQLite.COLUMN_DRUMMER_KIT, "TEXT");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading from " + oldVersion + " to " + newVersion);
        applySurgicalUpgrade(db, oldVersion);
    }

    public static void addColumnIfMissing(SQLiteDatabase db, String columnName, String columnType) {
        // PRAGMA table_info returns one row for each column in the table
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + SQLite.TABLE_NAME + ")", null)) {
            boolean columnExists = false;
            int nameIndex = cursor.getColumnIndex("name");

            while (cursor.moveToNext()) {
                if (nameIndex != -1 && columnName.equals(cursor.getString(nameIndex))) {
                    columnExists = true;
                    break;
                }
            }

            if (!columnExists) {
                db.execSQL("ALTER TABLE " + SQLite.TABLE_NAME + " ADD COLUMN " + columnName + " " + columnType);
            }
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error checking/adding column: " + columnName, e);
        }
    }

    // Create, delete and update entries
    public void createSong(String folder, String filename) {
        // Creates a basic song entry to the database
        try {
            // Retrieve the persistent instance instead of a resource to be closed
            SQLiteDatabase db = getWritableDatabase();
            mainActivityInterface.getCommonSQL().createSong(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            // Keep the catch block for hardware/IO errors
            Log.e(TAG, "Error creating song", e);
        }
    }

    public boolean deleteSong(String folder, String filename) {
        try {
            // Retrieve the database instance without closing it
            SQLiteDatabase db = getWritableDatabase();
            int rowsAffected = mainActivityInterface.getCommonSQL().deleteSong(db, folder, filename);
            return rowsAffected > 0; // Standard check: > 0 means a row was actually deleted
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error deleting song", e);
            return false;
        }
    }

    public void updateSong(Song thisSong) {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getWritableDatabase();
            mainActivityInterface.getCommonSQL().updateSong(db, thisSong);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            // Catching Exception is sufficient; OutOfMemoryError is likely
            // a symptom of a deeper issue elsewhere in the app.
            Log.e(TAG, "Error updating song", e);
        }
    }
    public boolean renameSong(String oldFolder, String newFolder, String oldName, String newName) {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getWritableDatabase();
            return mainActivityInterface.getCommonSQL().renameSong(db, oldFolder, newFolder, oldName, newName);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error renaming song", e);
            return false;
        }
    }

    // Get song information
    public String getKey(String folder, String filename) {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().getKey(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            // Logging the error is sufficient.
            Log.e(TAG, "Error getting key", e);
            return "";
        }
    }
    public boolean songExists(String folder, String filename) {
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            return mainActivityInterface.getCommonSQL().songExists(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error checking if song exists", e);
            return false;
        }
    }

    // Find specific song
    public Song getSpecificSong(String folder, String filename) {
        Song thisSong = new Song();
        String songId = mainActivityInterface.getCommonSQL().getAnySongId(folder, filename);

        // Retrieve references without using try-with-resources
        SQLiteDatabase db = mainActivityInterface.getSQLiteHelper().getReadableDatabase();
        SQLiteDatabase db2 = getReadableDatabase();

        try {
            // Get the basics from the main DB
            thisSong = mainActivityInterface.getCommonSQL().getSpecificSong(db, folder, filename);

            // Check for extra info in the NonOpenSongDatabase
            if (mainActivityInterface.getCommonSQL().songExists(db2, folder, filename)) {
                // Get detailed values
                thisSong = mainActivityInterface.getCommonSQL().getSpecificSong(db2, folder, filename);

                // Update values in the temporary main database
                mainActivityInterface.getCommonSQL().updateSong(db, thisSong);
            }
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error fetching specific song", e);
            thisSong.setFolder(folder);
            thisSong.setFilename(filename);
            thisSong.setSongid(songId);
        }
        return thisSong;
    }


    // Datagbase actions
    public void exportDatabase() {
        // Export a CSV version of the persistent database
        try {
            // Retrieve the persistent instance managed by SQLiteOpenHelper
            SQLiteDatabase db = getReadableDatabase();
            mainActivityInterface.getCommonSQL().exportDatabase(db, "NonOpenSongSongs.csv");
        } catch (OutOfMemoryError | Exception e) { // Keep both here
            Log.e(TAG, "Error exporting database to CSV", e);
        }
    }
    public void backupPersistentDatabase() {
        // This copies the appDB file to a backup file
        if (appDB==null) {
            getDatabaseUris();
        }
        // Get the date to append to the backup file
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String backupFileName = SQLite.NON_OS_DATABASE_NAME.replace(".db","_backup_"+date+".db");
        Uri backupUri = mainActivityInterface.getStorageAccess().getUriForItem("Backups","",backupFileName);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,backupUri,null,"Backups","",backupFileName);
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(appDB);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(backupUri);
        if (inputStream!=null && outputStream!=null) {
            if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                c.startActivity(Intent.createChooser(mainActivityInterface.getExportActions().setShareIntent(backupFileName, "application/vnd.sqlite3", backupUri, null), backupFileName));
            } else {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.error));
            }
        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.error));
        }
    }
    public void importDatabaseBackup() {
        String returnlog;
        String temp_backup_filename = "persistent_temp_backup.db";
        // The app has already copied the appDB to the userDB before this step - our backup plan in case of issue!
        // Copy this file into the Settings folder
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(appDB);
        Uri tempFileUri = mainActivityInterface.getStorageAccess().getUriForItem("Backups","",temp_backup_filename);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,tempFileUri,null,"Backups","",temp_backup_filename);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(tempFileUri);

        // Only proceed if this step works
        if (mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream)) {
            // Now we have some confidence that we can continue as we have a backup
            // Overwrite the userDB with the imported uri
            inputStream = mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri());
            outputStream = mainActivityInterface.getStorageAccess().getOutputStream(userDB);

            // Only proceed if this step works
            if (mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream)) {
                // Now we overwrite the appDB file
                inputStream = mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri());
                outputStream = mainActivityInterface.getStorageAccess().getOutputStream(appDB);

                // Only proceed if this step works
                if (mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream)) {
                    // It has worked, so now we can delete the temporary file
                    mainActivityInterface.getStorageAccess().deleteFile(tempFileUri);
                    // Now rebuild the song index
                    mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
                    mainActivityInterface.getSongListBuildIndex().setFullIndexRequired(true);
                    mainActivityInterface.updateSongMenu(null,null,null);
                    returnlog = "success";

                } else {
                    returnlog = "overwriteappDBerror";
                }
            } else {
                returnlog = "overwriteuserDBerror";
            }
        } else {
            returnlog = "backuperror";
        }
        if (returnlog.equals("success")) {
            mainActivityInterface.getShowToast().success();
        } else {
            mainActivityInterface.getShowToast().error();
        }
    }

    public void cleanDatabase(DatabaseUtilitiesFragment databaseUtilitiesFragment) {
        // 1. Open the database using the helper lifecycle
        ArrayList<Song> uselessSongs = new ArrayList<>();
        ArrayList<Song> usefulSongs = new ArrayList<>();

        // Use try-with-resources to ensure the DB connection is handled automatically
        try {
            // Retrieve the reference safely from your helper
            SQLiteDatabase db = getReadableDatabase();

            ArrayList<Song> nonExistingSongs = mainActivityInterface.getCommonSQL().getNonExistingSongsInDB(db);

            for (Song song : nonExistingSongs) {
                Song fullSong = mainActivityInterface.getCommonSQL().getSpecificSong(
                        db, song.getFolder(), song.getFilename()
                );

                if (fullSong == null) continue; // Skip if song not found

                if (hasUsefulValues(fullSong)) {
                    usefulSongs.add(fullSong);
                } else {
                    uselessSongs.add(fullSong);
                }
            }
        } catch (android.database.sqlite.SQLiteException e) {
            // Catch specific DB errors, not just a generic Exception
            Log.e(TAG, "Database operation failed: ", e);
            mainActivityInterface.getShowToast().error();
        }

        // Send the results back
        if (databaseUtilitiesFragment != null) {
            try {
                databaseUtilitiesFragment.showCleanDatabaseResults(uselessSongs, usefulSongs);
            } catch (OutOfMemoryError | Exception e) { // Keep both here
                mainActivityInterface.getShowToast().error();
            }
        }
    }

    // Helper to keep cleanDatabase() readable
    private boolean hasUsefulValues(Song song) {
        return valueNotEmpty(song.getAuthor()) ||
                valueNotEmpty(song.getCopyright()) ||
                valueNotEmpty(song.getLyrics()) ||
                valueNotEmpty(song.getHymnnum()) ||
                valueNotEmpty(song.getCcli()) ||
                valueNotEmpty(song.getTheme()) ||
                valueNotEmpty(song.getAlttheme()) ||
                valueNotEmpty(song.getUser1()) ||
                valueNotEmpty(song.getUser2()) ||
                valueNotEmpty(song.getUser3()) ||
                valueNotEmpty(song.getBeatbuddysong()) ||
                valueNotEmpty(song.getBeatbuddykit()) ||
                valueNotEmpty(song.getKey()) ||
                valueNotEmpty(song.getKeyOriginal()) ||
                valueNotEmpty(song.getPreferredInstrument()) ||
                valueNotEmpty(DrumCalculations.getFixedTimeSignatureString(song.getTimesig(), false)) ||
                valueNotEmpty(song.getAka()) ||
                valueNotEmpty(song.getAutoscrolldelay()) ||
                valueNotEmpty(song.getAutoscrolllength()) ||
                valueNotEmpty(DrumCalculations.getFixedTempoString(song.getTempo(), false)) ||
                valueNotEmpty(song.getPadfile()) ||
                valueNotEmpty(song.getPadloop()) ||
                valueNotEmpty(song.getMidi()) ||
                valueNotEmpty(song.getMidiindex()) ||
                valueNotEmpty(song.getCapo()) ||
                valueNotEmpty(song.getCustomchords()) ||
                valueNotEmpty(song.getNotes()) ||
                valueNotEmpty(song.getAbc()) ||
                valueNotEmpty(song.getLinkyoutube()) ||
                valueNotEmpty(song.getLinkweb()) ||
                valueNotEmpty(song.getLinkaudio()) ||
                valueNotEmpty(song.getLinkother()) ||
                valueNotEmpty(song.getPresentationorder());
    }
    private boolean valueNotEmpty(String value) {
        return value!=null && !value.isEmpty();
    }

    public void removeUselessEntries(ArrayList<Song> uselessSongs, boolean safeToDelete, CleanDatabaseBottomSheet cleanDatabaseBottomSheet) {
        // We will remove the entries from the database as they aren't used.
        // If this isn't safe, we will add a row to the removedNonOpenSongSongs.csv file

        if (uselessSongs!=null) {
            //Uri removedFilesUri = null;
            if (!safeToDelete) {
                StringBuilder headings = new StringBuilder();
                mainActivityInterface.getCommonSQL().addCSVTableHeadings(headings);
                mainActivityInterface.getStorageAccess().writeFileFromString("Settings","","removedNonOpenSongSongs.csv",headings.toString(), false);

            }

            StringBuilder lineForRemovedFile = new StringBuilder();
            for (Song uselessSong:uselessSongs) {
                deleteSong(uselessSong.getFolder(),uselessSong.getFilename());
                if (!safeToDelete) {
                    // Add the table headings - CODE MUST BE UPDATED IF COLUMNS CHANGE - USE SQLite file
                    // Don't worry about ID or SONG_ID as they are created automatically based on entry / filenames / folders
                    mainActivityInterface.getCommonSQL().addCSVTableValue(lineForRemovedFile,uselessSong,null);
                }
            }

            if (!safeToDelete) {
                // Add the info to the table
                mainActivityInterface.getStorageAccess().updateRemoveDBFile(lineForRemovedFile.toString());
            }

            // Update the fragement
            if (cleanDatabaseBottomSheet!=null) {
                try {
                    if (safeToDelete) {
                        cleanDatabaseBottomSheet.clearUseless();
                    } else {
                        cleanDatabaseBottomSheet.clearUseful();
                    }
                } catch (OutOfMemoryError | Exception e) { // Keep both here
                    e.printStackTrace();
                }
            }

        } else {
            mainActivityInterface.getShowToast().error();
        }
    }
}
