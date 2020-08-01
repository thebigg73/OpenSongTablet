package com.garethevans.church.opensongtablet.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.util.ArrayList;

public class CommonSQL {
    // This is used to perform common tasks for the SQL database and NonOpenSongSQL database.
    // Only the database itself is different, so as long as that is dealt with separately, we can proceed
    // When we return an SQLite object


    // Basic string parsing for SQL safety
    public String escapedSQL(String s) {
        // Don't do this if already escaped
        if (s != null) {
            s = s.replace("''", "^&*");
            s = s.replace("'", "''");
            s = s.replace("^&*", "''");
        }
        return s;
    }
    public String unescapedSQL(String s) {
        if (s != null) {
            while (s.contains("''")) {
                s = s.replace("''", "'");
            }
        }
        return s;
    }

    // Update the table.  Called for the NonOpenSong database that is persistent.
    // This is called if the db2 version is different to the version stated in NonOpenSongSQLiteHelper
    // This check we have the columns we need now
    void updateTable(SQLiteDatabase db2) {
        // This is called if the database version changes.  It will attempt to add each column
        // It will throw an error if it already exists, but we will catch it
        String[] columnNames = {SQLite.COLUMN_ID, SQLite.COLUMN_ID, SQLite.COLUMN_FILENAME, SQLite.COLUMN_FOLDER,
                SQLite.COLUMN_TITLE, SQLite.COLUMN_AUTHOR, SQLite.COLUMN_COPYRIGHT, SQLite.COLUMN_LYRICS,
                SQLite.COLUMN_HYMNNUM, SQLite.COLUMN_CCLI, SQLite.COLUMN_THEME, SQLite.COLUMN_ALTTHEME,
                SQLite.COLUMN_USER1, SQLite.COLUMN_USER2, SQLite.COLUMN_USER3, SQLite.COLUMN_KEY,
                SQLite.COLUMN_TIMESIG, SQLite.COLUMN_AKA, SQLite.COLUMN_AUTOSCROLL_DELAY,
                SQLite.COLUMN_AUTOSCROLL_LENGTH, SQLite.COLUMN_METRONOME_BPM, SQLite.COLUMN_PAD_FILE,
                SQLite.COLUMN_PAD_LOOP, SQLite.COLUMN_MIDI, SQLite.COLUMN_MIDI_INDEX, SQLite.COLUMN_CAPO,
                SQLite.COLUMN_CUSTOM_CHORDS, SQLite.COLUMN_NOTES, SQLite.COLUMN_ABC,
                SQLite.COLUMN_LINK_YOUTUBE, SQLite.COLUMN_LINK_YOUTUBE, SQLite.COLUMN_LINK_WEB,
                SQLite.COLUMN_LINK_AUDIO, SQLite.COLUMN_LINK_OTHER, SQLite.COLUMN_PRESENTATIONORDER,
                SQLite.COLUMN_FILETYPE};

        String mainQuery = "ALTER " + SQLite.TABLE_NAME + " ADD COLUMN ";
        String thisQuery;
        String type;
        for (String column : columnNames) {
            if (column.equals(SQLite.COLUMN_ID)) {
                type = " INTEGER PRIMARY KEY AUTOINCREMENT";
            } else if (column.equals(SQLite.COLUMN_SONGID)) {
                type = " TEXT UNIQUE";
            } else {
                type = " TEXT";
            }
            thisQuery = mainQuery + column + type;
            try {
                db2.execSQL(thisQuery);
            } catch (Exception e) {
                Log.d("CommonSQL", "Attempting to add " + column + " but it already exists.");
            }
        }
    }

    // Song ID tasks and checks for values
    public String getAnySongId(String folder, String filename) {
        // Double '' to make SQL safe
        return escapedSQL(folder) + "/" + escapedSQL(filename);
    }
    boolean songIdExists(SQLiteDatabase db, String songid) {
        String Query = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + " = \"" + escapedSQL(songid) + "\"";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
    private String getSetString(Context c, String folder, String filename) {
        if (folder == null || folder.equals(c.getString(R.string.mainfoldername)) || folder.isEmpty()) {
            return "$**_" + filename + "_**$";
        } else {
            return "$**_" + folder + "/" + filename + "_**$";
        }
    }

    // Create, delete and update
    void createSong(Context c, StorageAccess storageAccess, SQLiteDatabase db, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        if (folder == null || folder.isEmpty()) {
            folder = c.getString(R.string.mainfoldername);
        }
        folder = storageAccess.safeFilename(folder);
        filename = storageAccess.safeFilename(filename);

        String songid = getAnySongId(folder, filename);

        // If it doens't already exist, create it
        if (!songIdExists(db, songid)) {
            ContentValues values = new ContentValues();
            values.put(SQLite.COLUMN_SONGID, escapedSQL(songid));
            values.put(SQLite.COLUMN_FOLDER, escapedSQL(folder));
            values.put(SQLite.COLUMN_FILENAME, escapedSQL(filename));

            // Insert the new row
            try {
                db.insert(SQLite.TABLE_NAME, null, values);
            } catch (Exception e) {
                Log.d("CommonSQL", songid + " already exists in the table, not able to create.");
            }
        }
    }
    int deleteSong(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder, filename);
        return db.delete(SQLite.TABLE_NAME, SQLite.COLUMN_SONGID + " = ?",
                new String[]{String.valueOf(escapedSQL(songId))});
    }
    public void updateSong(SQLiteDatabase db, SQLite sqLite) {
        // Values have already been set to sqLite, just need updated in the table
        ContentValues values = new ContentValues();
        values.put(SQLite.COLUMN_ID, sqLite.getId());
        values.put(SQLite.COLUMN_SONGID, escapedSQL(sqLite.getSongid()));
        values.put(SQLite.COLUMN_FILENAME, escapedSQL(sqLite.getFilename()));
        values.put(SQLite.COLUMN_FOLDER, escapedSQL(sqLite.getFolder()));
        values.put(SQLite.COLUMN_TITLE, escapedSQL(sqLite.getTitle()));
        values.put(SQLite.COLUMN_AUTHOR, escapedSQL(sqLite.getAuthor()));
        values.put(SQLite.COLUMN_COPYRIGHT, escapedSQL(sqLite.getCopyright()));
        values.put(SQLite.COLUMN_LYRICS, escapedSQL(sqLite.getLyrics()));
        values.put(SQLite.COLUMN_HYMNNUM, escapedSQL(sqLite.getHymn_num()));
        values.put(SQLite.COLUMN_CCLI, escapedSQL(sqLite.getCcli()));
        values.put(SQLite.COLUMN_THEME, escapedSQL(sqLite.getTheme()));
        values.put(SQLite.COLUMN_ALTTHEME, escapedSQL(sqLite.getAlttheme()));
        values.put(SQLite.COLUMN_USER1, escapedSQL(sqLite.getUser1()));
        values.put(SQLite.COLUMN_USER2, escapedSQL(sqLite.getUser2()));
        values.put(SQLite.COLUMN_USER3, escapedSQL(sqLite.getUser3()));
        values.put(SQLite.COLUMN_KEY, escapedSQL(sqLite.getKey()));
        values.put(SQLite.COLUMN_TIMESIG, escapedSQL(sqLite.getTimesig()));
        values.put(SQLite.COLUMN_AKA, escapedSQL(sqLite.getAka()));
        values.put(SQLite.COLUMN_AUTOSCROLL_DELAY, escapedSQL(sqLite.getAutoscrolldelay()));
        values.put(SQLite.COLUMN_AUTOSCROLL_LENGTH, escapedSQL(sqLite.getAutoscrolllength()));
        values.put(SQLite.COLUMN_METRONOME_BPM, escapedSQL(sqLite.getMetronomebpm()));
        values.put(SQLite.COLUMN_PAD_FILE, escapedSQL(sqLite.getPadfile()));
        values.put(SQLite.COLUMN_PAD_LOOP, escapedSQL(sqLite.getPadloop()));
        values.put(SQLite.COLUMN_MIDI, escapedSQL(sqLite.getMidi()));
        values.put(SQLite.COLUMN_MIDI_INDEX, escapedSQL(sqLite.getMidiindex()));
        values.put(SQLite.COLUMN_CAPO, escapedSQL(sqLite.getCapo()));
        values.put(SQLite.COLUMN_CUSTOM_CHORDS, escapedSQL(sqLite.getCustomchords()));
        values.put(SQLite.COLUMN_NOTES, escapedSQL(sqLite.getNotes()));
        values.put(SQLite.COLUMN_ABC, escapedSQL(sqLite.getAbc()));
        values.put(SQLite.COLUMN_LINK_YOUTUBE, escapedSQL(sqLite.getLinkyoutube()));
        values.put(SQLite.COLUMN_LINK_WEB, escapedSQL(sqLite.getLinkWeb()));
        values.put(SQLite.COLUMN_LINK_AUDIO, escapedSQL(sqLite.getLinkaudio()));
        values.put(SQLite.COLUMN_LINK_OTHER, escapedSQL(sqLite.getLinkother()));
        values.put(SQLite.COLUMN_PRESENTATIONORDER, escapedSQL(sqLite.getPresentationorder()));
        values.put(SQLite.COLUMN_FILETYPE, escapedSQL(sqLite.getFiletype()));

        if (db.update(SQLite.TABLE_NAME, values, SQLite.COLUMN_ID + "=?", new String[]{String.valueOf(sqLite.getId())}) == 0) {
            // Row didn't exist, so add it
            db.insert(SQLite.TABLE_NAME, null, values);
        }
    }
    void insertFast(Context c, SQLiteDatabase db, StorageAccess storageAccess){
        // Insert new values or ignore rows that exist already
        String sql = "INSERT OR IGNORE INTO " + SQLite.TABLE_NAME + " ( songid, filename, folder ) VALUES ( ?, ?, ?)";
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        ArrayList<String> songIds = storageAccess.getSongIDsFromFile(c);

        for (String s : songIds) {
            String filename;
            String foldername;
            // Only add song files, so if it ends with / this loop skips
            if (s.endsWith("/")) {
                filename = "";
                foldername = s.substring(0, s.lastIndexOf("/"));
            } else if (s.contains("/")) {
                filename = s.substring(s.lastIndexOf("/"));
                foldername = s.replace(filename, "");
            } else {
                filename = s;
                foldername = c.getString(R.string.mainfoldername);
            }

            filename = filename.replace("/", "");

            stmt.bindString(1, s);
            stmt.bindString(2, filename);
            stmt.bindString(3, foldername);

            stmt.execute();
            stmt.clearBindings();
        }
    }
    String getValue(Cursor cursor,String index) {
        return unescapedSQL(cursor.getString(cursor.getColumnIndex(index)));
    }
    public SQLite updateSQLiteFromStatics(StorageAccess storageAccess, SQLite sqLite, String folder, String filename) {
        // This sets the sqLite values to match the Statics.  Called when editing/saving songs
        // COUMN_ID is not changed as it is the primary key
        sqLite.setSongid(getAnySongId(folder,filename));
        sqLite.setFolder(escapedSQL(storageAccess.safeFilename(folder)));
        sqLite.setFilename(escapedSQL(storageAccess.safeFilename(filename)));
        sqLite.setTitle(escapedSQL(StaticVariables.mTitle));
        sqLite.setAuthor(escapedSQL(StaticVariables.mAuthor));
        sqLite.setCopyright(escapedSQL(StaticVariables.mCopyright));
        sqLite.setLyrics(escapedSQL(StaticVariables.mLyrics));
        sqLite.setCcli(escapedSQL(StaticVariables.mCCLI));
        sqLite.setPresentationorder(escapedSQL(StaticVariables.mPresentation));
        sqLite.setHymn_num(escapedSQL(StaticVariables.mHymnNumber));
        sqLite.setUser1(escapedSQL(StaticVariables.mUser1));
        sqLite.setUser2(escapedSQL(StaticVariables.mUser2));
        sqLite.setUser3(escapedSQL(StaticVariables.mUser3));
        sqLite.setKey(escapedSQL(StaticVariables.mKey));
        sqLite.setAka(escapedSQL(StaticVariables.mAka));
        sqLite.setCapo(escapedSQL(StaticVariables.mCapo));
        sqLite.setMetronomebpm(escapedSQL(StaticVariables.mTempo));
        sqLite.setTimesig(escapedSQL(StaticVariables.mTimeSig));
        sqLite.setTheme(escapedSQL(StaticVariables.mTheme));
        sqLite.setAlttheme(escapedSQL(StaticVariables.mAltTheme));
        sqLite.setAutoscrolllength(escapedSQL(StaticVariables.mDuration));
        sqLite.setAutoscrolldelay(escapedSQL(StaticVariables.mPreDelay));
        sqLite.setMidi(escapedSQL(StaticVariables.mMidi));
        sqLite.setMidiindex(escapedSQL(StaticVariables.mMidiIndex));
        sqLite.setNotes(escapedSQL(StaticVariables.mNotes));
        sqLite.setAbc(escapedSQL(StaticVariables.mNotation));
        sqLite.setPadfile(escapedSQL(StaticVariables.mPadFile));
        sqLite.setCustomChords(escapedSQL(StaticVariables.mCustomChords));
        sqLite.setLinkyoutube(escapedSQL(StaticVariables.mLinkYouTube));
        sqLite.setLinkWeb(escapedSQL(StaticVariables.mLinkWeb));
        sqLite.setLinkaudio(escapedSQL(StaticVariables.mLinkAudio));
        sqLite.setPadloop(escapedSQL(StaticVariables.mLoopAudio));
        sqLite.setLinkother(escapedSQL(StaticVariables.mLinkOther));
        sqLite.setFiletype(escapedSQL(StaticVariables.mFileType));

        return sqLite;
    }

    // Search for values in the table
    ArrayList<SQLite> getSongsByFilters(Context c, SQLiteDatabase db, boolean searchByFolder,
                                        boolean searchByArtist, boolean searchByKey, boolean searchByTag,
                                        boolean searchByFilter, String folderVal, String artistVal,
                                        String keyVal, String tagVal, String filterVal) {
        ArrayList<SQLite> songs = new ArrayList<>();
        StaticVariables.songsInList = new ArrayList<>();
        StaticVariables.songsInList.clear();

        String sqlMatch = "";
        if (searchByFolder && folderVal != null && folderVal.length() > 0) {
            sqlMatch += SQLite.COLUMN_FOLDER + "=\"" + folderVal + "\" AND ";
        }
        if (searchByArtist && artistVal != null && artistVal.length() > 0) {
            sqlMatch += SQLite.COLUMN_AUTHOR + " LIKE \"%" + artistVal + "%\" AND ";
        }
        if (searchByKey && keyVal != null && keyVal.length() > 0) {
            sqlMatch += SQLite.COLUMN_KEY + "=\"" + keyVal + "\" AND ";
        }
        if (searchByTag && tagVal != null && tagVal.length() > 0) {
            sqlMatch += "(" + SQLite.COLUMN_THEME + " LIKE \"%" + tagVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_ALTTHEME + " LIKE \"%" + tagVal + "%\") AND ";
        }
        if (searchByFilter && filterVal != null && filterVal.length() > 0) {
            sqlMatch += "(" + SQLite.COLUMN_LYRICS + " LIKE \"%" + filterVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_FILENAME + " LIKE \"%" + filterVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_TITLE + " LIKE \"%" + filterVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_COPYRIGHT + " LIKE \"&" + filterVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_HYMNNUM + " LIKE \"%" + filterVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_CCLI + " LIKE \"%" + filterVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_USER1 + " LIKE \"%" + filterVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_USER2 + " LIKE \"%" + filterVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_USER3 + " LIKE \"%" + filterVal + "%\")";
        }

        if (!sqlMatch.isEmpty()) {
            if (sqlMatch.endsWith(" AND ")) {
                sqlMatch = "WHERE " + sqlMatch;
            } else {
                sqlMatch = "WHERE " + sqlMatch + " AND ";
            }
        } else {
            sqlMatch = "WHERE ";
        }
        sqlMatch += SQLite.COLUMN_FILENAME + " !=''";

        // Select matching folder Query
        // Common strings for searching.  Don't need to grab everything here - we can get the rest later
        String getOrderBySQL = "ORDER BY " + SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";
        String getBasicSQLQueryStart = "SELECT " + SQLite.COLUMN_FILENAME + ", " + SQLite.COLUMN_AUTHOR + ", " +
                SQLite.COLUMN_KEY + ", " + SQLite.COLUMN_FOLDER + ", " + SQLite.COLUMN_THEME + ", " +
                SQLite.COLUMN_ALTTHEME + ", " + SQLite.COLUMN_USER1 + ", " + SQLite.COLUMN_USER2 + ", " +
                SQLite.COLUMN_USER3 + ", " + SQLite.COLUMN_LYRICS + ", " + SQLite.COLUMN_HYMNNUM +
                " FROM " + SQLite.TABLE_NAME + " ";
        String selectQuery = getBasicSQLQueryStart + sqlMatch + " " + getOrderBySQL;


        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String fi = unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME)));
                String fo = unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER)));
                String au = unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR)));
                String ke = unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_KEY)));

                SQLite sqLite = new SQLite();
                sqLite.setFilename(fi);
                sqLite.setFolder(fo);
                sqLite.setAuthor(au);
                sqLite.setKey(ke);

                songs.add(sqLite);

                // Is this in the set?  This will add a tick for the songlist checkbox
                String setString = getSetString(c, fo, fi);
                StaticVariables.songsInList.add(setString);
            }
            while (cursor.moveToNext());
        }

        // close cursor connection
        try {
            cursor.close();
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }

        //Return the songs
        Log.d("SQLiteHelper", "QUERY:  " + selectQuery);
        return songs;
    }
    SQLite getSpecificSong(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder,filename);
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "=\"" + songId + "\"";
        SQLite thisSong = new SQLite();

        Cursor cursor = db.rawQuery(sql, null);

        // Get the first item (the matching songID)
        if (cursor.moveToFirst()) {
            thisSong.setId(cursor.getInt(cursor.getColumnIndex(SQLite.COLUMN_ID)));
            thisSong.setSongid(unescapedSQL(songId));
            thisSong.setFilename(getValue(cursor,SQLite.COLUMN_FILENAME));
            thisSong.setFolder(getValue(cursor,SQLite.COLUMN_FOLDER));
            thisSong.setTitle(getValue(cursor,SQLite.COLUMN_TITLE));
            thisSong.setAuthor(getValue(cursor,SQLite.COLUMN_AUTHOR));
            thisSong.setCopyright(getValue(cursor,SQLite.COLUMN_COPYRIGHT));
            thisSong.setLyrics(getValue(cursor,SQLite.COLUMN_LYRICS));
            thisSong.setHymn_num(getValue(cursor,SQLite.COLUMN_HYMNNUM));
            thisSong.setCcli(getValue(cursor,SQLite.COLUMN_CCLI));
            thisSong.setTheme(getValue(cursor,SQLite.COLUMN_THEME));
            thisSong.setAlttheme(getValue(cursor,SQLite.COLUMN_ALTTHEME));
            thisSong.setUser1(getValue(cursor,SQLite.COLUMN_USER1));
            thisSong.setUser2(getValue(cursor,SQLite.COLUMN_USER2));
            thisSong.setUser3(getValue(cursor,SQLite.COLUMN_USER3));
            thisSong.setKey(getValue(cursor,SQLite.COLUMN_KEY));
            thisSong.setTimesig(getValue(cursor,SQLite.COLUMN_TIMESIG));
            thisSong.setAka(getValue(cursor,SQLite.COLUMN_AKA));
            thisSong.setAutoscrolldelay(getValue(cursor,SQLite.COLUMN_AUTOSCROLL_DELAY));
            thisSong.setAutoscrolllength(getValue(cursor,SQLite.COLUMN_AUTOSCROLL_LENGTH));
            thisSong.setMetronomebpm(getValue(cursor,SQLite.COLUMN_METRONOME_BPM));
            thisSong.setPadfile(getValue(cursor,SQLite.COLUMN_PAD_FILE));
            thisSong.setPadloop(getValue(cursor,SQLite.COLUMN_PAD_LOOP));
            thisSong.setMidi(getValue(cursor,SQLite.COLUMN_MIDI));
            thisSong.setMidiindex(getValue(cursor,SQLite.COLUMN_MIDI_INDEX));
            thisSong.setCapo(getValue(cursor,SQLite.COLUMN_CAPO));
            thisSong.setCustomChords((getValue(cursor,SQLite.COLUMN_CUSTOM_CHORDS)));
            thisSong.setNotes(getValue(cursor,SQLite.COLUMN_NOTES));
            thisSong.setAbc(getValue(cursor,SQLite.COLUMN_ABC));
            thisSong.setLinkyoutube(getValue(cursor,SQLite.COLUMN_LINK_YOUTUBE));
            thisSong.setLinkWeb(getValue(cursor,SQLite.COLUMN_LINK_WEB));
            thisSong.setLinkaudio(getValue(cursor,SQLite.COLUMN_LINK_AUDIO));
            thisSong.setLinkother(getValue(cursor,SQLite.COLUMN_LINK_OTHER));
            thisSong.setPresentationorder(getValue(cursor,SQLite.COLUMN_PRESENTATIONORDER));
            thisSong.setFiletype(getValue(cursor,SQLite.COLUMN_FILETYPE));
        }

        try {
            cursor.close();
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }

        return thisSong;
    }
    public boolean songExists(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder,filename);
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "=\"" + songId + "\"";
        Cursor cursor = db.rawQuery(sql, null);
        int count;
        if (cursor!=null) {
            // Error, so not found
            return false;
        } else {
            count = cursor.getCount();
        }
        cursor.close();
        return count > 0;
    }
    ArrayList<String> getFolders (SQLiteDatabase db) {
        ArrayList<String> folders = new ArrayList<>();
        String q = "SELECT DISTINCT " + SQLite.COLUMN_FOLDER + " FROM " + SQLite.TABLE_NAME + " ORDER BY " +
                SQLite.COLUMN_FOLDER + " ASC";

        Cursor cursor = db.rawQuery(q, null);
        cursor.moveToFirst();
        do {
            String s = cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER));
            if (s!=null && !s.isEmpty()) {
                folders.add(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER))));
            }
        } while (cursor.moveToNext());
        cursor.close();
        return folders;
    }
}
