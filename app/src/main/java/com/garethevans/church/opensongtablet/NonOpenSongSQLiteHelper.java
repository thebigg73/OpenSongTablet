package com.garethevans.church.opensongtablet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class NonOpenSongSQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    public NonOpenSongSQLiteHelper(Context context) {
        super(context, NonOpenSongSQLite.DATABASE_NAME, null, DATABASE_VERSION);
        // Don't create the database here as we don't want to recreate on each call.
    }

    public void initialise(Context c, StorageAccess storageAccess, Preferences preferences) {
         SQLiteDatabase db = getDB(c,storageAccess,preferences);
        if (db != null) {
            onCreate(db);
            db.close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // If the table doesn't exist, create it.
        db.execSQL(NonOpenSongSQLite.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + NonOpenSongSQLite.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    private SQLiteDatabase getDB(Context c, StorageAccess storageAccess, Preferences preferences) {
        // The app uses a local app folder for the database, but copies a persistent version to the OpenSong/Settings folder
        Uri uri = storageAccess.getUriForItem(c,preferences,"Settings","",NonOpenSongSQLite.DATABASE_NAME);
        if (!storageAccess.uriExists(c,uri)) {
            storageAccess.lollipopCreateFileForOutputStream(c,preferences,uri,null,"Settings","",NonOpenSongSQLite.DATABASE_NAME);
        }

        try {
            File f = new File(c.getExternalFilesDir("Database"), NonOpenSongSQLite.DATABASE_NAME);
            if (!f.exists() && storageAccess.uriExists(c,uri)) {
                // Maybe a reinstall?  Copy the user NonOpenSong database
                InputStream inputStream = storageAccess.getInputStream(c,uri);
                OutputStream outputStream = new FileOutputStream(f);
                storageAccess.copyFile(inputStream,outputStream);
            }
            return SQLiteDatabase.openOrCreateDatabase(f, null);
        } catch (Exception e) {
            return null;
        }

    }

    private String escapedSQL(String s) {
        // Don't do this if already escaped
        if (s!=null) {
            s = s.replace("''", "^&*");
            s = s.replace("'", "''");
            s = s.replace("^&*", "''");
            return s;
        } else {
            return s;
        }
    }

    private String unescapedSQL(String s) {
        if (s!=null) {
            while (s.contains("''")) {
                s = s.replace("''","'");
            }
            return s;
        } else {
            return s;
        }
    }

    void createBasicSong(Context c, StorageAccess storageAccess, Preferences preferences, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db = getDB(c,storageAccess,preferences)) {
            if (folder == null || folder.isEmpty()) {
                folder = c.getString(R.string.mainfoldername);
            }
            filename = escapedSQL(filename);
            folder = escapedSQL(folder);
            String songid = escapedSQL(folder) + "/" + escapedSQL(filename);
            ContentValues values = new ContentValues();
            values.put(NonOpenSongSQLite.COLUMN_SONGID, escapedSQL(songid));
            values.put(NonOpenSongSQLite.COLUMN_FOLDER, escapedSQL(folder));
            values.put(NonOpenSongSQLite.COLUMN_FILENAME, escapedSQL(filename));
            values.put(NonOpenSongSQLite.COLUMN_METRONOME_SIG, "");
            values.put(NonOpenSongSQLite.COLUMN_METRONOME_BPM, "");
            values.put(NonOpenSongSQLite.COLUMN_AUTOSCROLL_LENGTH, "");
            values.put(NonOpenSongSQLite.COLUMN_AUTOSCROLL_DELAY, "");
            values.put(NonOpenSongSQLite.COLUMN_KEY, "");
            values.put(NonOpenSongSQLite.COLUMN_AKA, "");
            values.put(NonOpenSongSQLite.COLUMN_ALTTHEME, "");
            values.put(NonOpenSongSQLite.COLUMN_AUTHOR, "");
            values.put(NonOpenSongSQLite.COLUMN_CCLI, "");
            values.put(NonOpenSongSQLite.COLUMN_COPYRIGHT, "");
            values.put(NonOpenSongSQLite.COLUMN_HYMNNUM, "");
            values.put(NonOpenSongSQLite.COLUMN_LYRICS, "");
            values.put(NonOpenSongSQLite.COLUMN_THEME, "");
            values.put(NonOpenSongSQLite.COLUMN_TITLE, "");
            values.put(NonOpenSongSQLite.COLUMN_USER1, "");
            values.put(NonOpenSongSQLite.COLUMN_USER2, "");
            values.put(NonOpenSongSQLite.COLUMN_USER3, "");
            values.put(NonOpenSongSQLite.COLUMN_PAD_FILE, "");
            values.put(NonOpenSongSQLite.COLUMN_MIDI, "");
            values.put(NonOpenSongSQLite.COLUMN_MIDI_INDEX, "");
            values.put(NonOpenSongSQLite.COLUMN_CAPO, "");
            values.put(NonOpenSongSQLite.COLUMN_NOTES, "");
            values.put(NonOpenSongSQLite.COLUMN_ABC, "");
            values.put(NonOpenSongSQLite.COLUMN_LINK_YOUTUBE, "");
            values.put(NonOpenSongSQLite.COLUMN_LINK_WEB,"");
            values.put(NonOpenSongSQLite.COLUMN_LINK_AUDIO,"");
            values.put(NonOpenSongSQLite.COLUMN_LINK_OTHER,"");

            try {
                if (db != null) {
                    db.insert(NonOpenSongSQLite.TABLE_NAME, null, values);
                    db.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void updateSong(Context c, StorageAccess storageAccess, Preferences preferences, NonOpenSongSQLite sqLite) {

        try (SQLiteDatabase db = getDB(c,storageAccess,preferences)) {
            ContentValues values = new ContentValues();
            values.put(NonOpenSongSQLite.COLUMN_ID, sqLite.getId());
            values.put(NonOpenSongSQLite.COLUMN_SONGID, escapedSQL(sqLite.getSongid()));
            values.put(NonOpenSongSQLite.COLUMN_FILENAME, escapedSQL(sqLite.getFilename()));
            values.put(NonOpenSongSQLite.COLUMN_FOLDER, escapedSQL(sqLite.getFolder()));
            values.put(NonOpenSongSQLite.COLUMN_TITLE, escapedSQL(StaticVariables.mTitle));
            values.put(NonOpenSongSQLite.COLUMN_AUTHOR, escapedSQL(StaticVariables.mAuthor));
            values.put(NonOpenSongSQLite.COLUMN_COPYRIGHT, escapedSQL(StaticVariables.mCopyright));
            values.put(NonOpenSongSQLite.COLUMN_LYRICS, escapedSQL(StaticVariables.mLyrics));
            values.put(NonOpenSongSQLite.COLUMN_HYMNNUM, escapedSQL(StaticVariables.mHymnNumber));
            values.put(NonOpenSongSQLite.COLUMN_CCLI, escapedSQL(StaticVariables.mCCLI));
            values.put(NonOpenSongSQLite.COLUMN_THEME, escapedSQL(StaticVariables.mTheme));
            values.put(NonOpenSongSQLite.COLUMN_ALTTHEME, escapedSQL(StaticVariables.mAltTheme));
            values.put(NonOpenSongSQLite.COLUMN_USER1, escapedSQL(StaticVariables.mUser1));
            values.put(NonOpenSongSQLite.COLUMN_USER2, escapedSQL(StaticVariables.mUser2));
            values.put(NonOpenSongSQLite.COLUMN_USER3, escapedSQL(StaticVariables.mUser3));
            values.put(NonOpenSongSQLite.COLUMN_KEY, escapedSQL(StaticVariables.mKey));
            values.put(NonOpenSongSQLite.COLUMN_AKA, escapedSQL(StaticVariables.mAka));
            values.put(NonOpenSongSQLite.COLUMN_AUTOSCROLL_DELAY, escapedSQL(StaticVariables.mPreDelay));
            values.put(NonOpenSongSQLite.COLUMN_AUTOSCROLL_LENGTH, escapedSQL(StaticVariables.mDuration));
            values.put(NonOpenSongSQLite.COLUMN_METRONOME_BPM, escapedSQL(StaticVariables.mTempo));
            values.put(NonOpenSongSQLite.COLUMN_METRONOME_SIG, escapedSQL(StaticVariables.mTimeSig));
            values.put(NonOpenSongSQLite.COLUMN_PAD_FILE, escapedSQL(StaticVariables.mPadFile));
            values.put(NonOpenSongSQLite.COLUMN_MIDI, escapedSQL(StaticVariables.mMidi));
            values.put(NonOpenSongSQLite.COLUMN_MIDI_INDEX, escapedSQL(StaticVariables.mMidiIndex));
            values.put(NonOpenSongSQLite.COLUMN_CAPO, escapedSQL(StaticVariables.mCapo));
            values.put(NonOpenSongSQLite.COLUMN_NOTES, escapedSQL(StaticVariables.mNotes));
            values.put(NonOpenSongSQLite.COLUMN_ABC, escapedSQL(StaticVariables.mNotation));
            values.put(NonOpenSongSQLite.COLUMN_LINK_YOUTUBE, escapedSQL(StaticVariables.mLinkYouTube));
            values.put(NonOpenSongSQLite.COLUMN_LINK_WEB, escapedSQL(StaticVariables.mLinkWeb));
            values.put(NonOpenSongSQLite.COLUMN_LINK_AUDIO, escapedSQL(StaticVariables.mLinkAudio));
            values.put(NonOpenSongSQLite.COLUMN_LINK_OTHER, escapedSQL(StaticVariables.mLinkOther));
            values.put(NonOpenSongSQLite.COLUMN_PRESENTATIONORDER, escapedSQL(StaticVariables.mPresentation));

            if (db != null) {
                db.update(NonOpenSongSQLite.TABLE_NAME, values, NonOpenSongSQLite.COLUMN_ID + "=?", new String[]{String.valueOf(sqLite.getId())});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    NonOpenSongSQLite getSong(Context c, StorageAccess storageAccess, Preferences preferences, String songid) {
        SQLiteDatabase db = getDB(c,storageAccess,preferences);
        try {
            Cursor cursor = null;
            if (db != null) {
                cursor = db.query(NonOpenSongSQLite.TABLE_NAME,
                        new String[]{NonOpenSongSQLite.COLUMN_ID, NonOpenSongSQLite.COLUMN_SONGID,
                                NonOpenSongSQLite.COLUMN_FILENAME, NonOpenSongSQLite.COLUMN_FOLDER,
                                NonOpenSongSQLite.COLUMN_TITLE, NonOpenSongSQLite.COLUMN_AUTHOR,
                                NonOpenSongSQLite.COLUMN_COPYRIGHT, NonOpenSongSQLite.COLUMN_LYRICS,
                                NonOpenSongSQLite.COLUMN_HYMNNUM, NonOpenSongSQLite.COLUMN_CCLI,
                                NonOpenSongSQLite.COLUMN_THEME, NonOpenSongSQLite.COLUMN_ALTTHEME,
                                NonOpenSongSQLite.COLUMN_USER1, NonOpenSongSQLite.COLUMN_USER2,
                                NonOpenSongSQLite.COLUMN_USER3, NonOpenSongSQLite.COLUMN_KEY,
                                NonOpenSongSQLite.COLUMN_AKA, NonOpenSongSQLite.COLUMN_AUTOSCROLL_DELAY,
                                NonOpenSongSQLite.COLUMN_AUTOSCROLL_LENGTH, NonOpenSongSQLite.COLUMN_METRONOME_BPM,
                                NonOpenSongSQLite.COLUMN_METRONOME_SIG, NonOpenSongSQLite.COLUMN_PAD_FILE,
                                NonOpenSongSQLite.COLUMN_MIDI, NonOpenSongSQLite.COLUMN_MIDI_INDEX,
                                NonOpenSongSQLite.COLUMN_CAPO, NonOpenSongSQLite.COLUMN_NOTES, NonOpenSongSQLite.COLUMN_ABC,
                        NonOpenSongSQLite.COLUMN_LINK_YOUTUBE, NonOpenSongSQLite.COLUMN_LINK_WEB,
                        NonOpenSongSQLite.COLUMN_LINK_AUDIO, NonOpenSongSQLite.COLUMN_LINK_OTHER,
                        NonOpenSongSQLite.COLUMN_PRESENTATIONORDER},
                        NonOpenSongSQLite.COLUMN_SONGID + "=?",
                        new String[]{String.valueOf((songid))}, null, null, NonOpenSongSQLite.COLUMN_FILENAME, null);
            }

            if (cursor != null) {
                cursor.moveToFirst();

                try {
                    // prepare note object
                    NonOpenSongSQLite sqLite = new NonOpenSongSQLite(
                            cursor.getInt(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_ID)),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_SONGID))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_FILENAME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_FOLDER))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_TITLE))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_AUTHOR))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_COPYRIGHT))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_LYRICS))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_HYMNNUM))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_CCLI))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_THEME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_ALTTHEME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_USER1))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_USER2))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_USER3))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_KEY))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_AKA))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_AUTOSCROLL_DELAY))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_AUTOSCROLL_LENGTH))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_METRONOME_BPM))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_METRONOME_SIG))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_PAD_FILE))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_MIDI))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_MIDI_INDEX))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_CAPO))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_NOTES))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_ABC))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_LINK_YOUTUBE))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_LINK_WEB))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_LINK_AUDIO))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_LINK_OTHER))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_PRESENTATIONORDER))));

                    // close the db connection
                    cursor.close();
                    db.close();
                    return sqLite;
                } catch (Exception e) {
                    Log.d("SQLiteHelper", "Song not found");
                    return null;
                } finally {
                    db.close();
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    String getSongId() {
        return StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
    }

    // TODO still to add this
    void updateFolderName(Context c, StorageAccess storageAccess, Preferences preferences, String oldFolder, String newFolder) {
        // Select matching folder Query
        String selectQuery = "SELECT "+NonOpenSongSQLite.COLUMN_SONGID + ", " +
                NonOpenSongSQLite.COLUMN_FOLDER + ", " +
                NonOpenSongSQLite.COLUMN_FILENAME + " " +
                "FROM " + NonOpenSongSQLite.TABLE_NAME +
                " WHERE " + NonOpenSongSQLite.COLUMN_SONGID + " LIKE '%" + escapedSQL(oldFolder) + "/%'" +
                " ORDER BY " + NonOpenSongSQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c,storageAccess,preferences)) {
            Cursor cursor;
            if (db != null) {
                cursor = db.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        String currSongId = cursor.getString(cursor.getColumnIndex(NonOpenSongSQLite.COLUMN_SONGID));
                        String updatedId = currSongId.replace(oldFolder + "/", newFolder + "/");
                        ContentValues values = new ContentValues();
                        values.put(NonOpenSongSQLite.COLUMN_SONGID, escapedSQL(updatedId));
                        values.put(NonOpenSongSQLite.COLUMN_FOLDER, escapedSQL(newFolder));

                        db.update(NonOpenSongSQLite.TABLE_NAME, values, NonOpenSongSQLite.COLUMN_SONGID + "=?", new String[]{escapedSQL(currSongId)});

                    } while (cursor.moveToNext());
                }

                try {
                    cursor.close();
                    // close db connection
                    closeAndCopyDBToStorage(c,storageAccess,preferences,db);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }

    void deleteSong(Context c, StorageAccess storageAccess, Preferences preferences, String songId) {
        try (SQLiteDatabase db = getDB(c,storageAccess,preferences)) {
            if (db!=null) {
                db.delete(NonOpenSongSQLite.TABLE_NAME, NonOpenSongSQLite.COLUMN_SONGID + " = ?",
                        new String[]{String.valueOf(escapedSQL(songId))});
            }
        }
    }

    private void closeAndCopyDBToStorage(Context c, StorageAccess storageAccess, Preferences preferences, SQLiteDatabase db) {
        db.close();
        File f = new File(c.getExternalFilesDir("Database"), NonOpenSongSQLite.DATABASE_NAME);
        try {
            InputStream inputStream = new FileInputStream(f);
            Uri out = storageAccess.getUriForItem(c,preferences,"Settings","",NonOpenSongSQLite.DATABASE_NAME);
            OutputStream outputStream = storageAccess.getOutputStream(c,out);
            storageAccess.copyFile(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
