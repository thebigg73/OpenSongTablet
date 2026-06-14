package com.garethevans.church.opensongtablet.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;

public class AnalyticsSQLiteHelper extends SQLiteOpenHelper {
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AnalyticsSQLiteHelper";
    private static final String DB_NAME = "analytics.db";
    private static final int VERSION = 5;

    public AnalyticsSQLiteHelper(Context context) {
        // We calculate the exact file path so it lives in the same folder as songs.db
        super(context, getDatabasePath(context), null, VERSION);
    }

    static String getDatabasePath(Context context) {
        // This mirrors your main database location logic
        MainActivityInterface mainActivityInterface = (MainActivityInterface) context;
        File dbFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Database", "", DB_NAME);
        return dbFile.getAbsolutePath();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLite.CREATE_ANALYTICS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            // Add column SQLite_COLUMN_ADD_SET
            try {
                db.execSQL("ALTER TABLE " + SQLite.ANALYTICS_TABLE_NAME + " ADD COLUMN " + SQLite.COLUMN_SET_COUNT + " INTEGER DEFAULT 0;");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                db.execSQL("ALTER TABLE " + SQLite.ANALYTICS_TABLE_NAME + " ADD COLUMN " + SQLite.COLUMN_LAST_SET_DATE + " LONG DEFAULT 0;");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void incrementViewCount(String uuid) {
        if (uuid == null || uuid.isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();

        // 1. Try the UPDATE first (Atomic increment)
        String sql = "UPDATE " + SQLite.ANALYTICS_TABLE_NAME +
                " SET " + SQLite.COLUMN_VIEW_COUNT + " = " + SQLite.COLUMN_VIEW_COUNT + " + 1, " +
                SQLite.COLUMN_LAST_VIEWED + " = ? " +
                " WHERE " + SQLite.COLUMN_SONG_UUID + " = ?";

        android.database.sqlite.SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, now);
        stmt.bindString(2, uuid);
        int rows = stmt.executeUpdateDelete();
        stmt.close();

        // 2. If no rows were updated, the song record doesn't exist yet
        if (rows == 0) {
            ContentValues cv = new ContentValues();
            cv.put(SQLite.COLUMN_SONG_UUID, uuid);
            cv.put(SQLite.COLUMN_VIEW_COUNT, 1);
            cv.put(SQLite.COLUMN_LAST_VIEWED, now);
            // Ensure defaults for new rows
            cv.put(SQLite.COLUMN_LAST_SET_DATE, 0);
            cv.put(SQLite.COLUMN_LAST_CAST_DATE, 0);
            cv.put(SQLite.COLUMN_SET_COUNT, 0);

            db.insert(SQLite.ANALYTICS_TABLE_NAME, null, cv);
        }
    }

    public void incrementSetCount(String uuid) {
        if (uuid == null || uuid.isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();

        // 1. Atomic Update: Increment count and set the date
        String sql = "UPDATE " + SQLite.ANALYTICS_TABLE_NAME +
                " SET " + SQLite.COLUMN_SET_COUNT + " = " + SQLite.COLUMN_SET_COUNT + " + 1, " +
                SQLite.COLUMN_LAST_SET_DATE + " = ? " +
                " WHERE " + SQLite.COLUMN_SONG_UUID + " = ?";

        android.database.sqlite.SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, now);
        stmt.bindString(2, uuid);
        int rows = stmt.executeUpdateDelete();
        stmt.close();

        // 2. If no record existed, perform an INSERT
        if (rows == 0) {
            ContentValues cv = new ContentValues();
            cv.put(SQLite.COLUMN_SONG_UUID, uuid);
            cv.put(SQLite.COLUMN_SET_COUNT, 1);
            cv.put(SQLite.COLUMN_LAST_SET_DATE, now);
            // Ensure defaults for other columns
            cv.put(SQLite.COLUMN_VIEW_COUNT, 0);
            cv.put(SQLite.COLUMN_LAST_VIEWED, 0);
            cv.put(SQLite.COLUMN_LAST_CAST_DATE, 0);

            db.insert(SQLite.ANALYTICS_TABLE_NAME, null, cv);
        }
    }

    public void decrementSetCount(String uuid) {
        if (uuid == null || uuid.isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();

        // 1. Atomic Update: Decrement count using MAX(0, count - 1)
        // This ensures it never goes below 0.
        String sql = "UPDATE " + SQLite.ANALYTICS_TABLE_NAME +
                " SET " + SQLite.COLUMN_SET_COUNT + " = MAX(0, " + SQLite.COLUMN_SET_COUNT + " - 1), " +
                SQLite.COLUMN_LAST_SET_DATE + " = ? " +
                " WHERE " + SQLite.COLUMN_SONG_UUID + " = ?";

        android.database.sqlite.SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, now);
        stmt.bindString(2, uuid);
        int rows = stmt.executeUpdateDelete();
        stmt.close();
    }

    public void lastCastDate(String uuid) {
        if (uuid == null || uuid.isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();

        // 1. Attempt to update the existing row
        String sql = "UPDATE " + SQLite.ANALYTICS_TABLE_NAME +
                " SET " + SQLite.COLUMN_LAST_CAST_DATE + " = ? " +
                " WHERE " + SQLite.COLUMN_SONG_UUID + " = ?";

        android.database.sqlite.SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, now);
        stmt.bindString(2, uuid);
        int rows = stmt.executeUpdateDelete();
        stmt.close();

        // 2. If no record existed, perform an INSERT
        if (rows == 0) {
            ContentValues cv = new ContentValues();
            cv.put(SQLite.COLUMN_SONG_UUID, uuid);
            cv.put(SQLite.COLUMN_LAST_CAST_DATE, now);
            // Default other columns to 0
            cv.put(SQLite.COLUMN_VIEW_COUNT, 0);
            cv.put(SQLite.COLUMN_LAST_VIEWED, 0);
            cv.put(SQLite.COLUMN_LAST_SET_DATE, 0);
            cv.put(SQLite.COLUMN_SET_COUNT, 0);

            db.insert(SQLite.ANALYTICS_TABLE_NAME, null, cv);
        }
    }

    public void resetAnalytics() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Deletes all rows from the table
            db.execSQL("DELETE FROM " + SQLite.ANALYTICS_TABLE_NAME);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error resetting analytics database", e);
        } finally {
            db.endTransaction();
        }
    }
}