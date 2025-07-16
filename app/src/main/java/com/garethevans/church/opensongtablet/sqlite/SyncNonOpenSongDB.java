package com.garethevans.church.opensongtablet.sqlite;

import static com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper.DATABASE_VERSION;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.File;
import java.util.ArrayList;

public class SyncNonOpenSongDB extends SQLiteOpenHelper {

    private File syncDBFile;
    private final String TAG = "SyncNonOpenSongDB";
    private final MainActivityInterface mainActivityInterface;
    private final Context c;

    public SyncNonOpenSongDB(Context c) {
        super(c, SQLite.NON_OS_DATABASE_NAME, null, DATABASE_VERSION);
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;

        // Get a reference to the database files/uris (app and user)
        getDatabaseUris();
        // Try to get the database
        Log.d(TAG,"Try to get the database for initialisation");
        getDB();
    }

    private void getDatabaseUris() {
        syncDBFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Sync","",SQLite.SYNC_NO_OS_DATABASE_NAME);
    }

    public SQLiteDatabase getDB() {
        Log.d(TAG,"Opening or creating the database");
        if (syncDBFile==null) {
            getDatabaseUris();
        }
        syncDBFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Sync","",SQLite.SYNC_NO_OS_DATABASE_NAME);
        SQLiteDatabase db3 = SQLiteDatabase.openOrCreateDatabase(syncDBFile,null);
        Log.d(TAG,"db3.getVersion():"+db3.getVersion());
        if (db3.getVersion()!=DATABASE_VERSION) {
            Log.d(TAG,"different versions, so update");
            // Check we have the columns we need!
            db3.setVersion(DATABASE_VERSION);
            mainActivityInterface.getCommonSQL().updateTable(db3);
        } else {
            Log.d(TAG, "Couldn't get database version, or it is the same");
        }
        return db3;
    }

    public void updateMyDBForMatchingSongIds(ArrayList<String> songIds) {
        // Go through each entry in this database and update
        // If the database doesn't exist, create it
        try (SQLiteDatabase db3 = getDB()) {
            onCreate(db3);
        }

        for (String songId:songIds) {
            // Get this song from the imported database
            String filename = songId.substring(songId.lastIndexOf("/")+1).replace("/","");
            String folder = songId.replace("/"+filename,"");
            Song importedSong = mainActivityInterface.getCommonSQL().getSpecificSong(getDB(),folder,filename);
            Song myNonOpenSong = mainActivityInterface.getNonOpenSongSQLiteHelper().getSpecificSong(folder,filename);
            Song myDatabaseSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(folder,filename);
            Log.d(TAG,"importedSong ("+folder+"/"+filename+"): "+importedSong.getUuid()+"  "+importedSong.getLastModified() + "  notes:"+importedSong.getNotes());
            Log.d(TAG,"myNonOpenSong ("+folder+"/"+filename+"): "+myNonOpenSong.getUuid()+"  "+myNonOpenSong.getLastModified() + "  notes:"+myNonOpenSong.getNotes());
            Log.d(TAG,"myDatabaseSong ("+folder+"/"+filename+"): "+myDatabaseSong.getUuid()+"  "+myDatabaseSong.getLastModified() + "  notes:"+myDatabaseSong.getNotes());

            // Now update the matching song in our NonOpenSong database and the main database
            // TODO reinstate
            mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(importedSong);
            mainActivityInterface.getSQLiteHelper().updateSong(importedSong);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db3) {
        // If the table doesn't exist, create it.
        db3.execSQL(SQLite.CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db3, int oldVersion, int newVersion) {
        // Do nothing here as we manually update the table to match
        Log.d(TAG,"onUpgrade path:"+db3.getPath());
        addMissingColumns(db3.getPath(),oldVersion);
    }

    private void addMissingColumns(String dbPath,int oldVersion) {
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
        if (oldVersion<7) {
            try (SQLiteDatabase tempDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null)) {
                Cursor cursor = tempDB.rawQuery("SELECT * FROM " + SQLite.TABLE_NAME + " LIMIT 0", null);
                if (cursor.getColumnIndex(SQLite.COLUMN_UUID) == -1) {
                    tempDB.execSQL("ALTER TABLE " + SQLite.TABLE_NAME + " ADD " + SQLite.COLUMN_UUID + " TEXT");
                }
                cursor.close();
            }
        }
        if (oldVersion<8) {
            try (SQLiteDatabase tempDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null)) {
                Cursor cursor = tempDB.rawQuery("SELECT * FROM " + SQLite.TABLE_NAME + " LIMIT 0", null);
                if (cursor.getColumnIndex(SQLite.COLUMN_LAST_MODIFIED) == -1) {
                    tempDB.execSQL("ALTER TABLE " + SQLite.TABLE_NAME + " ADD " + SQLite.COLUMN_LAST_MODIFIED + " TEXT");
                }
                cursor.close();
            }
        }
    }

}
