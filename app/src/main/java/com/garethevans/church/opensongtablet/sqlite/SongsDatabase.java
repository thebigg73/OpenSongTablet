package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public class SongsDatabase extends SQLiteOpenHelper {
    private static SongsDatabase instance;
    private static final int DATABASE_VERSION = 11;
    private static final String TAG = "SongsDatabase";
    private static final ReentrantLock lock = new ReentrantLock();

    // Use this to get the instance from anywhere in your app
    // Getter for the instance


    public static SongsDatabase getInstance(Context context) {
        MainActivityInterface mainActivityInterface = (MainActivityInterface) context;

        if (mainActivityInterface.getAlertChecks().appHasUpdated()) {
            lock.lock();
            try {
                Log.d(TAG, "App has updated - resetting database");

                // 1. Force close the existing connection if it exists
                if (instance != null) {
                    instance.getWritableDatabase().close();
                    instance.close();
                    instance = null;
                }

                // 2. Perform the physical file deletions
                File oldDbFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Database", "", SQLite.DATABASE_NAME);
                if (oldDbFile.exists()) {
                    Log.d(TAG, "Old DB file removed: " + oldDbFile.delete());
                }

                // 3. Clear from system database directory as well
                context.deleteDatabase(SQLite.DATABASE_NAME);

            } catch (Exception e) {
                Log.e(TAG, "Error during database reset", e);
            } finally {
                lock.unlock();
            }
        }

        // Now instantiate the new singleton safely
        if (instance == null) {
            synchronized (SongsDatabase.class) {
                if (instance == null) {
                    instance = new SongsDatabase(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    // Secondary getter that assumes the instance is already initialized
    public static SongsDatabase getInstance() {
        lock.lock();
        try {
            if (instance == null) {
                throw new IllegalStateException("SongsDatabase must be initialized with context and path first.");
            }
            return instance;
        } finally {
            lock.unlock();
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
        // Handle migrations here if you ever change your table schema
    }

    public static void nullifyInstance() {
        instance = null;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enables WAL mode - can read while writing
        db.enableWriteAheadLogging();
    }
}