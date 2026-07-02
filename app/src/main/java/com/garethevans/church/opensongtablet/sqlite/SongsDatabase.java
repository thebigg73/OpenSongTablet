package com.garethevans.church.opensongtablet.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SongsDatabase extends SQLiteOpenHelper {
    private static SongsDatabase instance;
    private static final int DATABASE_VERSION = 11;

    // Use this to get the instance from anywhere in your app
    // Getter for the instance
    public static synchronized SongsDatabase getInstance(Context context) {
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