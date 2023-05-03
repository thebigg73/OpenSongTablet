package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class NonOpenSongSQLiteHelper extends SQLiteOpenHelper {

    private final Uri appDB, userDB;
    private final File appDBFile;
    private final String TAG = "NonOSSQLHelper";
    private final MainActivityInterface mainActivityInterface;

    public NonOpenSongSQLiteHelper(Context c) {
        super(c, SQLite.NON_OS_DATABASE_NAME, null, DATABASE_VERSION);
        mainActivityInterface = (MainActivityInterface) c;
        appDBFile = new File(c.getExternalFilesDir("Database"), SQLite.NON_OS_DATABASE_NAME);
        appDB = Uri.fromFile(appDBFile);
        userDB = mainActivityInterface.getStorageAccess().getUriForItem(
                "Settings", "", SQLite.NON_OS_DATABASE_NAME);

        // Check for a previous version in user storage
        // If it exists and isn't empty, copy it in to the appDB
        // If if doesn't exist, or is empty copy our appDB to the userDb
        importDatabase();
    }

    // Database Version
    private static final int DATABASE_VERSION = 4;

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
        if (mainActivityInterface.getStorageAccess().uriExists(userDB) &&
        mainActivityInterface.getStorageAccess().getFileSizeFromUri(userDB)>0) {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(userDB);
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(appDB);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" importDatabase copyFile from "+userDB+" to "+appDB);
            Log.d(TAG,"Initialise database: User database copied in from "+userDB+" to "+ appDB + " to appCache: "+mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream));
        } else {
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" importDatabse Create Settings/"+SQLite.NON_OS_DATABASE_NAME+" deleteOld=false");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, userDB,null,"Settings","",
                    SQLite.NON_OS_DATABASE_NAME);
            Log.d(TAG,"Create new "+SQLite.NON_OS_DATABASE_NAME+" at OpenSong/Settings/ and copy to appCache - success: "+copyUserDatabase());
        }
    }

    public boolean copyUserDatabase() {
        // This copies the app persistent database (app cache) into the user's OpenSong/Settings folder
        // GE It should only need done at app close, since it is never used directly
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(appDB);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(userDB);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" copyNonOpenSongAppDB copyFile from "+appDB+" to "+userDB);
        boolean copied = mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);
        Log.d(TAG,"Shut down calls "+SQLite.NON_OS_DATABASE_NAME+" from "+appDB+" to "+userDB+" - success:"+copied);
        return copied;
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
        };
        try (SQLiteDatabase tempDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null)) {
            Cursor cursor = tempDB.rawQuery("SELECT * FROM " + SQLite.TABLE_NAME + " LIMIT 0", null);
            if (cursor.getColumnIndex(SQLite.COLUMN_KEY_ORIGINAL) == -1) {
                tempDB.execSQL("ALTER TABLE " + SQLite.TABLE_NAME + " ADD " + SQLite.COLUMN_KEY_ORIGINAL + " TEXT");
            }
            cursor.close();
        };
    }

    // TODO Flush entries that aren't in the filesystem, or alert the user to issues (perhaps asking to update the entry?
}
