/*
package com.garethevans.church.opensongtablet.OLD_TO_DELETE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet._Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class _NonOpenSongSQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    public _NonOpenSongSQLiteHelper(Context context) {
        super(context, _NonOpenSongSQLite.DATABASE_NAME, null, DATABASE_VERSION);
        // Don't create the database here as we don't want to recreate on each call.
    }

    public void initialise(Context c, StorageAccess storageAccess, _Preferences preferences) {
         SQLiteDatabase db = getDB(c,storageAccess,preferences);
        if (db != null) {
            onCreate(db);
            db.close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // If the table doesn't exist, create it.
        db.execSQL(_NonOpenSongSQLite.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + _NonOpenSongSQLite.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    private SQLiteDatabase getDB(Context c, StorageAccess storageAccess, _Preferences preferences) {
        // The app uses a local app folder for the database, but copies a persistent version to the OpenSong/Settings folder
        Uri uri = storageAccess.getUriForItem(c,preferences,"Settings","", _NonOpenSongSQLite.DATABASE_NAME);
        if (!storageAccess.uriExists(c,uri)) {
            storageAccess.lollipopCreateFileForOutputStream(c,preferences,uri,null,"Settings","", _NonOpenSongSQLite.DATABASE_NAME);
        }

        try {
            File f = new File(c.getExternalFilesDir("Database"), _NonOpenSongSQLite.DATABASE_NAME);
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

    void createBasicSong(Context c, StorageAccess storageAccess, _Preferences preferences, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db = getDB(c,storageAccess,preferences)) {
            if (folder == null || folder.isEmpty()) {
                folder = c.getString(R.string.mainfoldername);
            }
            filename = escapedSQL(filename);
            folder = escapedSQL(folder);
            String songid = escapedSQL(folder) + "/" + escapedSQL(filename);
            ContentValues values = new ContentValues();
            values.put(_NonOpenSongSQLite.COLUMN_SONGID, escapedSQL(songid));
            values.put(_NonOpenSongSQLite.COLUMN_FOLDER, escapedSQL(folder));
            values.put(_NonOpenSongSQLite.COLUMN_FILENAME, escapedSQL(filename));
            values.put(_NonOpenSongSQLite.COLUMN_METRONOME_SIG, "");
            values.put(_NonOpenSongSQLite.COLUMN_METRONOME_BPM, "");
            values.put(_NonOpenSongSQLite.COLUMN_AUTOSCROLL_LENGTH, "");
            values.put(_NonOpenSongSQLite.COLUMN_AUTOSCROLL_DELAY, "");
            values.put(_NonOpenSongSQLite.COLUMN_KEY, "");
            values.put(_NonOpenSongSQLite.COLUMN_AKA, "");
            values.put(_NonOpenSongSQLite.COLUMN_ALTTHEME, "");
            values.put(_NonOpenSongSQLite.COLUMN_AUTHOR, "");
            values.put(_NonOpenSongSQLite.COLUMN_CCLI, "");
            values.put(_NonOpenSongSQLite.COLUMN_COPYRIGHT, "");
            values.put(_NonOpenSongSQLite.COLUMN_HYMNNUM, "");
            values.put(_NonOpenSongSQLite.COLUMN_LYRICS, "");
            values.put(_NonOpenSongSQLite.COLUMN_THEME, "");
            values.put(_NonOpenSongSQLite.COLUMN_TITLE, "");
            values.put(_NonOpenSongSQLite.COLUMN_USER1, "");
            values.put(_NonOpenSongSQLite.COLUMN_USER2, "");
            values.put(_NonOpenSongSQLite.COLUMN_USER3, "");
            values.put(_NonOpenSongSQLite.COLUMN_PAD_FILE, "");
            values.put(_NonOpenSongSQLite.COLUMN_MIDI, "");
            values.put(_NonOpenSongSQLite.COLUMN_MIDI_INDEX, "");
            values.put(_NonOpenSongSQLite.COLUMN_CAPO, "");
            values.put(_NonOpenSongSQLite.COLUMN_NOTES, "");
            values.put(_NonOpenSongSQLite.COLUMN_ABC, "");
            values.put(_NonOpenSongSQLite.COLUMN_LINK_YOUTUBE, "");
            values.put(_NonOpenSongSQLite.COLUMN_LINK_WEB,"");
            values.put(_NonOpenSongSQLite.COLUMN_LINK_AUDIO,"");
            values.put(_NonOpenSongSQLite.COLUMN_LINK_OTHER,"");

            try {
                if (db != null) {
                    db.insert(_NonOpenSongSQLite.TABLE_NAME, null, values);
                    db.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void updateSong(Context c, StorageAccess storageAccess, _Preferences preferences, _NonOpenSongSQLite sqLite) {

        try (SQLiteDatabase db = getDB(c,storageAccess,preferences)) {
            ContentValues values = new ContentValues();
            values.put(_NonOpenSongSQLite.COLUMN_ID, sqLite.getId());
            values.put(_NonOpenSongSQLite.COLUMN_SONGID, escapedSQL(sqLite.getSongid()));
            values.put(_NonOpenSongSQLite.COLUMN_FILENAME, escapedSQL(sqLite.getFilename()));
            values.put(_NonOpenSongSQLite.COLUMN_FOLDER, escapedSQL(sqLite.getFolder()));
            values.put(_NonOpenSongSQLite.COLUMN_TITLE, escapedSQL(StaticVariables.mTitle));
            values.put(_NonOpenSongSQLite.COLUMN_AUTHOR, escapedSQL(StaticVariables.mAuthor));
            values.put(_NonOpenSongSQLite.COLUMN_COPYRIGHT, escapedSQL(StaticVariables.mCopyright));
            values.put(_NonOpenSongSQLite.COLUMN_LYRICS, escapedSQL(StaticVariables.mLyrics));
            values.put(_NonOpenSongSQLite.COLUMN_HYMNNUM, escapedSQL(StaticVariables.mHymnNumber));
            values.put(_NonOpenSongSQLite.COLUMN_CCLI, escapedSQL(StaticVariables.mCCLI));
            values.put(_NonOpenSongSQLite.COLUMN_THEME, escapedSQL(StaticVariables.mTheme));
            values.put(_NonOpenSongSQLite.COLUMN_ALTTHEME, escapedSQL(StaticVariables.mAltTheme));
            values.put(_NonOpenSongSQLite.COLUMN_USER1, escapedSQL(StaticVariables.mUser1));
            values.put(_NonOpenSongSQLite.COLUMN_USER2, escapedSQL(StaticVariables.mUser2));
            values.put(_NonOpenSongSQLite.COLUMN_USER3, escapedSQL(StaticVariables.mUser3));
            values.put(_NonOpenSongSQLite.COLUMN_KEY, escapedSQL(StaticVariables.mKey));
            values.put(_NonOpenSongSQLite.COLUMN_AKA, escapedSQL(StaticVariables.mAka));
            values.put(_NonOpenSongSQLite.COLUMN_AUTOSCROLL_DELAY, escapedSQL(StaticVariables.mPreDelay));
            values.put(_NonOpenSongSQLite.COLUMN_AUTOSCROLL_LENGTH, escapedSQL(StaticVariables.mDuration));
            values.put(_NonOpenSongSQLite.COLUMN_METRONOME_BPM, escapedSQL(StaticVariables.mTempo));
            values.put(_NonOpenSongSQLite.COLUMN_METRONOME_SIG, escapedSQL(StaticVariables.mTimeSig));
            values.put(_NonOpenSongSQLite.COLUMN_PAD_FILE, escapedSQL(StaticVariables.mPadFile));
            values.put(_NonOpenSongSQLite.COLUMN_MIDI, escapedSQL(StaticVariables.mMidi));
            values.put(_NonOpenSongSQLite.COLUMN_MIDI_INDEX, escapedSQL(StaticVariables.mMidiIndex));
            values.put(_NonOpenSongSQLite.COLUMN_CAPO, escapedSQL(StaticVariables.mCapo));
            values.put(_NonOpenSongSQLite.COLUMN_NOTES, escapedSQL(StaticVariables.mNotes));
            values.put(_NonOpenSongSQLite.COLUMN_ABC, escapedSQL(StaticVariables.mNotation));
            values.put(_NonOpenSongSQLite.COLUMN_LINK_YOUTUBE, escapedSQL(StaticVariables.mLinkYouTube));
            values.put(_NonOpenSongSQLite.COLUMN_LINK_WEB, escapedSQL(StaticVariables.mLinkWeb));
            values.put(_NonOpenSongSQLite.COLUMN_LINK_AUDIO, escapedSQL(StaticVariables.mLinkAudio));
            values.put(_NonOpenSongSQLite.COLUMN_LINK_OTHER, escapedSQL(StaticVariables.mLinkOther));
            values.put(_NonOpenSongSQLite.COLUMN_PRESENTATIONORDER, escapedSQL(StaticVariables.mPresentation));

            if (db != null) {
                db.update(_NonOpenSongSQLite.TABLE_NAME, values, _NonOpenSongSQLite.COLUMN_ID + "=?", new String[]{String.valueOf(sqLite.getId())});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    _NonOpenSongSQLite getSong(Context c, StorageAccess storageAccess, _Preferences preferences, String songid) {
        SQLiteDatabase db = getDB(c,storageAccess,preferences);
        try {
            Cursor cursor = null;
            if (db != null) {
                cursor = db.query(_NonOpenSongSQLite.TABLE_NAME,
                        new String[]{_NonOpenSongSQLite.COLUMN_ID, _NonOpenSongSQLite.COLUMN_SONGID,
                                _NonOpenSongSQLite.COLUMN_FILENAME, _NonOpenSongSQLite.COLUMN_FOLDER,
                                _NonOpenSongSQLite.COLUMN_TITLE, _NonOpenSongSQLite.COLUMN_AUTHOR,
                                _NonOpenSongSQLite.COLUMN_COPYRIGHT, _NonOpenSongSQLite.COLUMN_LYRICS,
                                _NonOpenSongSQLite.COLUMN_HYMNNUM, _NonOpenSongSQLite.COLUMN_CCLI,
                                _NonOpenSongSQLite.COLUMN_THEME, _NonOpenSongSQLite.COLUMN_ALTTHEME,
                                _NonOpenSongSQLite.COLUMN_USER1, _NonOpenSongSQLite.COLUMN_USER2,
                                _NonOpenSongSQLite.COLUMN_USER3, _NonOpenSongSQLite.COLUMN_KEY,
                                _NonOpenSongSQLite.COLUMN_AKA, _NonOpenSongSQLite.COLUMN_AUTOSCROLL_DELAY,
                                _NonOpenSongSQLite.COLUMN_AUTOSCROLL_LENGTH, _NonOpenSongSQLite.COLUMN_METRONOME_BPM,
                                _NonOpenSongSQLite.COLUMN_METRONOME_SIG, _NonOpenSongSQLite.COLUMN_PAD_FILE,
                                _NonOpenSongSQLite.COLUMN_MIDI, _NonOpenSongSQLite.COLUMN_MIDI_INDEX,
                                _NonOpenSongSQLite.COLUMN_CAPO, _NonOpenSongSQLite.COLUMN_NOTES, _NonOpenSongSQLite.COLUMN_ABC,
                        _NonOpenSongSQLite.COLUMN_LINK_YOUTUBE, _NonOpenSongSQLite.COLUMN_LINK_WEB,
                        _NonOpenSongSQLite.COLUMN_LINK_AUDIO, _NonOpenSongSQLite.COLUMN_LINK_OTHER,
                        _NonOpenSongSQLite.COLUMN_PRESENTATIONORDER},
                        _NonOpenSongSQLite.COLUMN_SONGID + "=?",
                        new String[]{String.valueOf((songid))}, null, null, _NonOpenSongSQLite.COLUMN_FILENAME, null);
            }

            if (cursor != null) {
                cursor.moveToFirst();

                try {
                    // prepare note object
                    _NonOpenSongSQLite sqLite = new _NonOpenSongSQLite(
                            cursor.getInt(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_ID)),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_SONGID))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_FILENAME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_FOLDER))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_TITLE))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_AUTHOR))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_COPYRIGHT))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_LYRICS))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_HYMNNUM))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_CCLI))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_THEME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_ALTTHEME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_USER1))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_USER2))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_USER3))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_KEY))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_AKA))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_AUTOSCROLL_DELAY))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_AUTOSCROLL_LENGTH))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_METRONOME_BPM))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_METRONOME_SIG))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_PAD_FILE))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_MIDI))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_MIDI_INDEX))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_CAPO))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_NOTES))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_ABC))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_LINK_YOUTUBE))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_LINK_WEB))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_LINK_AUDIO))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_LINK_OTHER))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_PRESENTATIONORDER))));

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
    void updateFolderName(Context c, StorageAccess storageAccess, _Preferences preferences, String oldFolder, String newFolder) {
        // Select matching folder Query
        String selectQuery = "SELECT "+ _NonOpenSongSQLite.COLUMN_SONGID + ", " +
                _NonOpenSongSQLite.COLUMN_FOLDER + ", " +
                _NonOpenSongSQLite.COLUMN_FILENAME + " " +
                "FROM " + _NonOpenSongSQLite.TABLE_NAME +
                " WHERE " + _NonOpenSongSQLite.COLUMN_SONGID + " LIKE '%" + escapedSQL(oldFolder) + "/%'" +
                " ORDER BY " + _NonOpenSongSQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c,storageAccess,preferences)) {
            Cursor cursor;
            if (db != null) {
                cursor = db.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        String currSongId = cursor.getString(cursor.getColumnIndex(_NonOpenSongSQLite.COLUMN_SONGID));
                        String updatedId = currSongId.replace(oldFolder + "/", newFolder + "/");
                        ContentValues values = new ContentValues();
                        values.put(_NonOpenSongSQLite.COLUMN_SONGID, escapedSQL(updatedId));
                        values.put(_NonOpenSongSQLite.COLUMN_FOLDER, escapedSQL(newFolder));

                        db.update(_NonOpenSongSQLite.TABLE_NAME, values, _NonOpenSongSQLite.COLUMN_SONGID + "=?", new String[]{escapedSQL(currSongId)});

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

    void deleteSong(Context c, StorageAccess storageAccess, _Preferences preferences, String songId) {
        try (SQLiteDatabase db = getDB(c,storageAccess,preferences)) {
            if (db!=null) {
                db.delete(_NonOpenSongSQLite.TABLE_NAME, _NonOpenSongSQLite.COLUMN_SONGID + " = ?",
                        new String[]{String.valueOf(escapedSQL(songId))});
            }
        }
    }

    private void closeAndCopyDBToStorage(Context c, StorageAccess storageAccess, _Preferences preferences, SQLiteDatabase db) {
        db.close();
        File f = new File(c.getExternalFilesDir("Database"), _NonOpenSongSQLite.DATABASE_NAME);
        try {
            InputStream inputStream = new FileInputStream(f);
            Uri out = storageAccess.getUriForItem(c,preferences,"Settings","", _NonOpenSongSQLite.DATABASE_NAME);
            OutputStream outputStream = storageAccess.getOutputStream(c,out);
            storageAccess.copyFile(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/
