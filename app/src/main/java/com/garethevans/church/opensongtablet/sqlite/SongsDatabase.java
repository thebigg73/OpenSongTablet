package com.garethevans.church.opensongtablet.sqlite;

import static com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper.TAG;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;

public class SongsDatabase extends SQLiteOpenHelper {
    private static SongsDatabase instance;
    private static final int DATABASE_VERSION = 11;
    private static final String TAG = "SongsDatabase";

    // Use this to get the instance from anywhere in your app
    // Getter for the instance
    public static synchronized SongsDatabase getInstance(Context context) {
        // Cast context to your interface to access helper methods
        MainActivityInterface mainActivityInterface = (MainActivityInterface) context;

        // If the app has updated, we need to wipe and recreate the database
        // Also check for old ones in the custom location
        if (mainActivityInterface.getAlertChecks().appHasUpdated()) {
            Log.d(TAG,"App has updated - recreate Songs.db");
            // 1. Path check: Check if the old custom path database exists
            File oldDbFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Database","",SQLite.DATABASE_NAME);
            File oldDbJournal = mainActivityInterface.getStorageAccess().getAppSpecificFile("Database","","Songs.db-journal");
            if (oldDbFile.exists()) {
                try {
                    Log.d(TAG, "oldDbFile removed:" + oldDbFile.delete()); // Remove the legacy file to prevent conflicts
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (oldDbJournal.exists()) {
                try {
                    Log.d(TAG, "oldDbJournal removed:" + oldDbJournal.delete()); // Remove the legacy file to prevent conflicts
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                context.deleteDatabase(SQLite.DATABASE_NAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance == null) {
            instance = new SongsDatabase(context.getApplicationContext());
        }
        return instance;
    }

    // Secondary getter that assumes the instance is already initialized
    public static SongsDatabase getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SongsDatabase must be initialized with context and path first.");
        }
        return instance;
    }

    private SongsDatabase(Context context) {
        super(context, SQLite.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Use your predefined CREATE_TABLE string from your SQLite class
        db.execSQL(SQLite.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle migrations here if you ever change your table schema
    }

    public static void nullifyInstance() {
        instance = null;
    }
}