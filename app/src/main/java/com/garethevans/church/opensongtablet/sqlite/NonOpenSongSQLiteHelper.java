package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
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

    private Uri appDB, userDB;
    private File appDBFile;
    private final String TAG = "NonOSSQLHelper";
    private final MainActivityInterface mainActivityInterface;
    private final Context c;

    public NonOpenSongSQLiteHelper(Context c) {
        super(c, SQLite.NON_OS_DATABASE_NAME, null, DATABASE_VERSION);
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;

        // Get a reference to the database files/uris (app and user)
        getDatabaseUris();

        // Check for a previous version in user storage
        // If it exists and isn't empty, copy it in to the appDB
        // If if doesn't exist, or is empty copy our appDB to the userDB
        importDatabase();
    }

    private void getDatabaseUris() {
        appDBFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Database","",SQLite.NON_OS_DATABASE_NAME);

        appDB = Uri.fromFile(appDBFile);
        userDB = mainActivityInterface.getStorageAccess().getUriForItem(
                "Settings", "", SQLite.NON_OS_DATABASE_NAME);
    }
    // Database Version
    private static final int DATABASE_VERSION = 6;

    public SQLiteDatabase getDB() {
        // The version we use has to be in local app storage unfortunately.  We can copy this though
        SQLiteDatabase db2 = SQLiteDatabase.openOrCreateDatabase(appDBFile,null);
        if (db2.getVersion()!=DATABASE_VERSION) {
            // Check we have the columns we need!
            db2.setVersion(DATABASE_VERSION);
            mainActivityInterface.getCommonSQL().updateTable(db2);
        }
        return db2;
    }

    private void importDatabase() {
        // This copies in the version in the settings folder if it exists and isn't empty
        boolean copied;
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
    }

    public boolean copyUserDatabase() {
        // This copies the app persistent database (app cache) into the user's OpenSong/Settings folder
        // GE It should only need done at app close, since it is never used directly

        // In case there was an issue and the Uris are null, get them again
        if (appDB==null || userDB==null) {
            getDatabaseUris();
        }

        if (mainActivityInterface.getStorageAccess().uriTreeValid(userDB)) {

            // Get an input stream for the app database so we can copy it
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(appDB);

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
        // If the table doesn't exist, create it.
        db2.execSQL(SQLite.CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db2, int oldVersion, int newVersion) {
        // Do nothing here as we manually update the table to match
        db2.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME + ";");
    }
    public void initialise() {
        // If the database doesn't exist, create it
        try (SQLiteDatabase db2 = getDB()) {
            onCreate(db2);
        }
    }


    // Create, delete and update entries
    public void createSong(String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db2 = getDB()) {
            mainActivityInterface.getCommonSQL().createSong(db2, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }
    public boolean deleteSong(String folder, String filename) {
        int i;
        try (SQLiteDatabase db2 = getDB()) {
            i = mainActivityInterface.getCommonSQL().deleteSong(db2,folder,filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return false;
        }
        return i > -1;
    }
    public void updateSong(Song thisSong) {
        try (SQLiteDatabase db2 = getDB()) {
            mainActivityInterface.getCommonSQL().updateSong(db2,thisSong);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }

    public boolean renameSong(String oldFolder, String newFolder, String oldName, String newName) {
        try (SQLiteDatabase db2 = getDB()) {
            return mainActivityInterface.getCommonSQL().renameSong(db2, oldFolder,newFolder,oldName,newName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getKey(String folder, String filename) {
        try (SQLiteDatabase db2 = getDB()) {
            return mainActivityInterface.getCommonSQL().getKey(db2, folder, filename);
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
            return "";
        }
    }

    // Check if a song exists
    public boolean songExists(String folder, String filename) {
        try (SQLiteDatabase db = getDB()) {
            return mainActivityInterface.getCommonSQL().songExists(db, folder, filename);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Find specific song
    public Song getSpecificSong(String folder, String filename) {
        // This gets basic info from the normal temporary SQLite database
        // It then also adds in any extra stuff found in the NonOpenSongSQLite database
        Song thisSong = new Song();
        String songId = mainActivityInterface.getCommonSQL().getAnySongId(folder,filename);
        try (SQLiteDatabase db = mainActivityInterface.getSQLiteHelper().getDB()) {
            // Get the basics - error here returns the basic stuff as an exception
            thisSong = mainActivityInterface.getCommonSQL().getSpecificSong(db,folder,filename);

            // Now look to see if there is extra information in the saved NonOpenSongDatabase
            try (SQLiteDatabase db2 = getDB()) {
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
    }

    public void importDB(String dbToImport, boolean overwrite) {
        // This is from the restore osb fragment to import non-opensongapp database
        SQLiteDatabase currentDB = getDB();
        addMissingColumns(dbToImport);
        currentDB.execSQL("ATTACH DATABASE '" + dbToImport + "' AS tempDb");
        if (overwrite) {
            currentDB.execSQL("REPLACE INTO main." + SQLite.TABLE_NAME +  " SELECT * FROM tempDb."+ SQLite.TABLE_NAME);
        } else {
            currentDB.execSQL("INSERT OR IGNORE INTO main." + SQLite.TABLE_NAME +  " SELECT * FROM tempDb."+ SQLite.TABLE_NAME);
        }
        currentDB.close();
    }

    private void addMissingColumns(String dbPath) {
        try (SQLiteDatabase tempDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null)) {
            Cursor cursor = tempDB.rawQuery("SELECT * FROM " + SQLite.TABLE_NAME + " LIMIT 0", null);
            if (cursor.getColumnIndex(SQLite.COLUMN_ABC_TRANSPOSE) == -1) {
                tempDB.execSQL("ALTER TABLE " + SQLite.TABLE_NAME + " ADD " + SQLite.COLUMN_ABC_TRANSPOSE + " TEXT");
            }
            cursor.close();
        }
        try (SQLiteDatabase tempDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null)) {
            Cursor cursor = tempDB.rawQuery("SELECT * FROM " + SQLite.TABLE_NAME + " LIMIT 0", null);
            if (cursor.getColumnIndex(SQLite.COLUMN_KEY_ORIGINAL) == -1) {
                tempDB.execSQL("ALTER TABLE " + SQLite.TABLE_NAME + " ADD " + SQLite.COLUMN_KEY_ORIGINAL + " TEXT");
            }
            cursor.close();
        }
        try (SQLiteDatabase tempDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null)) {
            Cursor cursor = tempDB.rawQuery("SELECT * FROM " + SQLite.TABLE_NAME + " LIMIT 0", null);
            if (cursor.getColumnIndex(SQLite.COLUMN_BEATBUDDY_SONG) == -1) {
                tempDB.execSQL("ALTER TABLE " + SQLite.TABLE_NAME + " ADD " + SQLite.COLUMN_BEATBUDDY_SONG + " TEXT");
            }
            cursor.close();
        }
        try (SQLiteDatabase tempDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null)) {
            Cursor cursor = tempDB.rawQuery("SELECT * FROM " + SQLite.TABLE_NAME + " LIMIT 0", null);
            if (cursor.getColumnIndex(SQLite.COLUMN_BEATBUDDY_KIT) == -1) {
                tempDB.execSQL("ALTER TABLE " + SQLite.TABLE_NAME + " ADD " + SQLite.COLUMN_BEATBUDDY_KIT + " TEXT");
            }
            cursor.close();
        }
        try (SQLiteDatabase tempDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null)) {
            Cursor cursor = tempDB.rawQuery("SELECT * FROM " + SQLite.TABLE_NAME + " LIMIT 0", null);
            if (cursor.getColumnIndex(SQLite.COLUMN_PREFERRED_INSTRUMENT) == -1) {
                tempDB.execSQL("ALTER TABLE " + SQLite.TABLE_NAME + " ADD " + SQLite.COLUMN_PREFERRED_INSTRUMENT + " TEXT");
            }
            cursor.close();
        }
    }

    public void exportDatabase() {
        // Export a csv version of the persistent database
        mainActivityInterface.getCommonSQL().exportDatabase(getDB(),"NonOpenSongSongs.csv");
        getDB().close();
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
        // This goes through the persistent database looking for references to files that don't exist
        // If the file has no useful information, it will just delete the entry
        ArrayList<Song> nonExistingSongs = mainActivityInterface.getCommonSQL().getNonExistingSongsInDB(getDB());

        // Now we remove the songs that have no information in them
        // Keep a note of the useful ones
        ArrayList<Song> uselessSongs = new ArrayList<>();
        ArrayList<Song> usefulSongs = new ArrayList<>();
        for (Song song:nonExistingSongs) {
            // Get the full information from the song
            SQLiteDatabase database = getDB();
            song = mainActivityInterface.getCommonSQL().getSpecificSong(database, song.getFolder(), song.getFilename());
            database.close();
            // Check if values exist (don't worry about some less important ones)
            boolean valuesExist = valueNotEmpty(song.getAuthor()) ||
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
                    valueNotEmpty(song.getTimesig()) ||
                    valueNotEmpty(song.getAka()) ||
                    valueNotEmpty(song.getAutoscrolldelay()) ||
                    valueNotEmpty(song.getAutoscrolllength()) ||
                    valueNotEmpty(song.getTempo()) ||
                    valueNotEmpty(song.getPadfile()) ||
                    valueNotEmpty(song.getPadloop()) ||
                    valueNotEmpty(song.getMidi()) ||
                    valueNotEmpty(song.getMidiindex()) ||
                    valueNotEmpty(song.getCapo()) ||
                    //valueNotEmpty(song.getCapoprint()) ||
                    valueNotEmpty(song.getCustomchords()) ||
                    valueNotEmpty(song.getNotes()) ||
                    valueNotEmpty(song.getAbc()) ||
                    //valueNotEmpty(song.getAbcTranspose()) ||
                    valueNotEmpty(song.getLinkyoutube()) ||
                    valueNotEmpty(song.getLinkweb()) ||
                    valueNotEmpty(song.getLinkaudio()) ||
                    valueNotEmpty(song.getLinkother()) ||
                    valueNotEmpty(song.getPresentationorder());
                    //valueNotEmpty(song.getFiletype());
            if (valuesExist) {
                usefulSongs.add(song);
            } else {
                uselessSongs.add(song);
            }
        }

        // Clear the original song list
        nonExistingSongs.clear();

        // Send the results back to the calling fragment
        if (databaseUtilitiesFragment!=null) {
            try {
                databaseUtilitiesFragment.showCleanDatabaseResults(uselessSongs,usefulSongs);
            } catch (Exception e) {
                mainActivityInterface.getShowToast().error();
            }
        }
    }

    private boolean valueNotEmpty(String value) {
        return value!=null && !value.isEmpty();
    }

    public void removeUselessEntries(ArrayList<Song> uselessSongs, boolean safeToDelete, CleanDatabaseBottomSheet cleanDatabaseBottomSheet) {
        // We will remove the entries from the database as they aren't used.
        // If this isn't safe, we will add a row to the removedNonOpenSongSongs.csv file

        if (uselessSongs!=null) {
            Uri removedFilesUri = null;
            if (!safeToDelete) {
                // Check the removedNonOpenSongSongs.csv file exists
                removedFilesUri = mainActivityInterface.getStorageAccess().getUriForItem("Settings","","removedNonOpenSongSongs.csv");
                if (!mainActivityInterface.getStorageAccess().uriExists(removedFilesUri)) {
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false,removedFilesUri,null,"Settings","",
                            "removedNonOpenSongSongs.csv");
                    // Write the table headings to the newly created file
                    StringBuilder headings = new StringBuilder();
                    mainActivityInterface.getCommonSQL().addCSVTableHeadings(headings);
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(removedFilesUri);
                    mainActivityInterface.getStorageAccess().writeFileFromString(headings.toString(),outputStream);
                }
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            mainActivityInterface.getShowToast().error();
        }
    }

    // TODO Flush entries that aren't in the filesystem, or alert the user to issues (perhaps asking to update the entry?
}
