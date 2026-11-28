package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public class SongsDatabase extends SQLiteOpenHelper {
    private static volatile SongsDatabase instance;
    private static final int DATABASE_VERSION = 11;
    private static final String TAG = "SongsDatabase";
    private static final ReentrantLock lock = new ReentrantLock();

    // Use this to get the instance from anywhere in your app
    // Getter for the instance


    public static SongsDatabase getInstance(Context context) {
        synchronized (SongsDatabase.class) {
            MainActivityInterface mainActivityInterface = (MainActivityInterface) context;

            if (instance == null) {
                // Only check for an app update when we're creating the database (i.e. at startup),
                // never while it's in use
                if (mainActivityInterface.getAlertChecks().appHasUpdated()) {
                    Log.d(TAG, "App has updated - resetting database");
                    try {
                        File oldDbFile = mainActivityInterface.getStorageAccess()
                                .getAppSpecificFile("Database", "", SQLite.DATABASE_NAME);
                        if (oldDbFile.exists()) {
                            Log.d(TAG, "Old DB file removed: " + oldDbFile.delete());
                        }
                        context.deleteDatabase(SQLite.DATABASE_NAME);
                    } catch (Exception e) {
                        Log.e(TAG, "Error during database reset", e);
                    }
                }
                instance = new SongsDatabase(context.getApplicationContext());
            }
            return instance;
        }
    }
    public static SongsDatabase getInstance() {
        synchronized (SongsDatabase.class) {
            if (instance == null) {
                throw new IllegalStateException("SongsDatabase must be initialized with context first.");
            }
            return instance;
        }
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
        db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enables WAL mode - can read while writing
        db.enableWriteAheadLogging();
    }

    /*public synchronized void resetDatabase(Context context) {
        Log.d(TAG, "resetDatabase() called");

        // 1. Get the current writable database connection and close it if open
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }

        // 2. Completely delete the physical database file and its WAL/journal files
        context.deleteDatabase(SQLite.DATABASE_NAME);

        // 3. Reinitialize a fresh database file via the helper
        SQLiteDatabase freshDb = this.getWritableDatabase();
    }*/
    public synchronized void resetDatabase(Context context) {
        Log.d(TAG, "resetDatabase() called");
        close();                                  // closes the helper's connection pool
        context.deleteDatabase(SQLite.DATABASE_NAME); // also removes -wal, -shm, -journal
        getWritableDatabase();                    // recreate with onCreate()
    }
}