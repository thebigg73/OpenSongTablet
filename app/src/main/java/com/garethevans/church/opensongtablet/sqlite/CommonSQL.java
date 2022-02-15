package com.garethevans.church.opensongtablet.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.Collections;

public class CommonSQL {
    // This is used to perform common tasks for the SQL database and NonOpenSongSQL database.
    // Only the database itself is different, so as long as that is dealt with separately, we can proceed
    // When we return an SQLite object

    // Update the table.  Called for the NonOpenSong database that is persistent.
    // This is called if the db2 version is different to the version stated in NonOpenSongSQLiteHelper
    // This check we have the columns we need now

    private final String TAG = "CommonSQL";

    void updateTable(SQLiteDatabase db2) {
        // This is called if the database version changes.  It will attempt to add each column
        // It will throw an error if it already exists, but we will catch it
        String[] columnNames = {SQLite.COLUMN_ID, SQLite.COLUMN_SONGID, SQLite.COLUMN_FILENAME, SQLite.COLUMN_FOLDER,
                SQLite.COLUMN_TITLE, SQLite.COLUMN_AUTHOR, SQLite.COLUMN_COPYRIGHT, SQLite.COLUMN_LYRICS,
                SQLite.COLUMN_HYMNNUM, SQLite.COLUMN_CCLI, SQLite.COLUMN_THEME, SQLite.COLUMN_ALTTHEME,
                SQLite.COLUMN_USER1, SQLite.COLUMN_USER2, SQLite.COLUMN_USER3, SQLite.COLUMN_KEY,
                SQLite.COLUMN_TIMESIG, SQLite.COLUMN_AKA, SQLite.COLUMN_AUTOSCROLL_DELAY,
                SQLite.COLUMN_AUTOSCROLL_LENGTH, SQLite.COLUMN_TEMPO, SQLite.COLUMN_PAD_FILE,
                SQLite.COLUMN_PAD_LOOP, SQLite.COLUMN_MIDI, SQLite.COLUMN_MIDI_INDEX, SQLite.COLUMN_CAPO,
                SQLite.COLUMN_CAPO_PRINT, SQLite.COLUMN_CUSTOM_CHORDS, SQLite.COLUMN_NOTES, SQLite.COLUMN_ABC,
                SQLite.COLUMN_LINK_YOUTUBE, SQLite.COLUMN_LINK_YOUTUBE, SQLite.COLUMN_LINK_WEB,
                SQLite.COLUMN_LINK_AUDIO, SQLite.COLUMN_LINK_OTHER, SQLite.COLUMN_PRESENTATIONORDER,
                SQLite.COLUMN_FILETYPE};

        String mainQuery = "ALTER TABLE " + SQLite.TABLE_NAME + " ADD COLUMN ";
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
                Log.d(TAG, "Attempting to add " + column + " but it already exists.");
            }
        }
    }

    // Song ID tasks and checks for values
    public String getAnySongId(String folder, String filename) {
        // Double '' to make SQL safe
        return folder + "/" + filename;
    }

    boolean songIdExists(SQLiteDatabase db, String songid) {
        String[] selectionArgs = new String[]{songid};
        String Query = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + " = ? ";

        Cursor cursor = db.rawQuery(Query, selectionArgs);
        boolean exists = cursor.getCount() > 0;
        closeCursor(cursor);
        return exists;
    }

    // Create, delete and update
    void createSong(Context c, MainActivityInterface mainActivityInterface, SQLiteDatabase db, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        if (folder == null || folder.isEmpty()) {
            folder = c.getString(R.string.mainfoldername);
        }
        folder = mainActivityInterface.getStorageAccess().safeFilename(folder);
        filename = mainActivityInterface.getStorageAccess().safeFilename(filename);

        String songid = getAnySongId(folder, filename);

        // If it doens't already exist, create it
        if (!songIdExists(db, songid)) {
            ContentValues values = new ContentValues();
            values.put(SQLite.COLUMN_SONGID, songid);
            values.put(SQLite.COLUMN_FOLDER, folder);
            values.put(SQLite.COLUMN_FILENAME, filename);

            // Insert the new row
            try {
                db.insert(SQLite.TABLE_NAME, null, values);
            } catch (Exception e) {
                Log.d(TAG, songid + " already exists in the table, not able to create.");
            }
        }
    }

    int deleteSong(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder, filename);
        return db.delete(SQLite.TABLE_NAME, SQLite.COLUMN_SONGID + " = ?",
                new String[]{String.valueOf(songId)});
    }

    public void updateSong(SQLiteDatabase db, Song thisSong) {
        // Values have already been set to sqLite, just need updated in the table
        // We use an object reference to song as this could be from indexingSong or actual song
        String correctId = getAnySongId(thisSong.getFolder(),thisSong.getFilename());
        if (thisSong.getSongid()==null || thisSong.getSongid().isEmpty() || !thisSong.getSongid().equals(correctId)) {
            thisSong.setSongid(correctId);
        }
        Log.d(TAG,"updatingSong: "+thisSong.getSongid()+" - "+thisSong.getFolder()+"/"+thisSong.getFilename());
        ContentValues values = new ContentValues();
        values.put(SQLite.COLUMN_SONGID, thisSong.getSongid());
        values.put(SQLite.COLUMN_FILENAME, thisSong.getFilename());
        values.put(SQLite.COLUMN_FOLDER, thisSong.getFolder());
        values.put(SQLite.COLUMN_TITLE, thisSong.getTitle());
        values.put(SQLite.COLUMN_AUTHOR, thisSong.getAuthor());
        values.put(SQLite.COLUMN_COPYRIGHT, thisSong.getCopyright());
        values.put(SQLite.COLUMN_LYRICS, thisSong.getLyrics());
        values.put(SQLite.COLUMN_HYMNNUM, thisSong.getHymnnum());
        values.put(SQLite.COLUMN_CCLI, thisSong.getCcli());
        values.put(SQLite.COLUMN_THEME, thisSong.getTheme());
        values.put(SQLite.COLUMN_ALTTHEME, thisSong.getAlttheme());
        values.put(SQLite.COLUMN_USER1, thisSong.getUser1());
        values.put(SQLite.COLUMN_USER2, thisSong.getUser2());
        values.put(SQLite.COLUMN_USER3, thisSong.getUser3());
        values.put(SQLite.COLUMN_KEY, thisSong.getKey());
        values.put(SQLite.COLUMN_TIMESIG, thisSong.getTimesig());
        values.put(SQLite.COLUMN_AKA, thisSong.getAka());
        values.put(SQLite.COLUMN_AUTOSCROLL_DELAY, thisSong.getAutoscrolldelay());
        values.put(SQLite.COLUMN_AUTOSCROLL_LENGTH, thisSong.getAutoscrolllength());
        values.put(SQLite.COLUMN_TEMPO, thisSong.getTempo());
        values.put(SQLite.COLUMN_PAD_FILE, thisSong.getPadfile());
        values.put(SQLite.COLUMN_PAD_LOOP, thisSong.getPadloop());
        values.put(SQLite.COLUMN_MIDI, thisSong.getMidi());
        values.put(SQLite.COLUMN_MIDI_INDEX, thisSong.getMidiindex());
        values.put(SQLite.COLUMN_CAPO, thisSong.getCapo());
        values.put(SQLite.COLUMN_CAPO_PRINT, thisSong.getCapoprint());
        values.put(SQLite.COLUMN_CUSTOM_CHORDS, thisSong.getCustomchords());
        values.put(SQLite.COLUMN_NOTES, thisSong.getNotes());
        values.put(SQLite.COLUMN_ABC, thisSong.getAbc());
        values.put(SQLite.COLUMN_LINK_YOUTUBE, thisSong.getLinkyoutube());
        values.put(SQLite.COLUMN_LINK_WEB, thisSong.getLinkweb());
        values.put(SQLite.COLUMN_LINK_AUDIO, thisSong.getLinkaudio());
        values.put(SQLite.COLUMN_LINK_OTHER, thisSong.getLinkother());
        values.put(SQLite.COLUMN_PRESENTATIONORDER, thisSong.getPresentationorder());
        values.put(SQLite.COLUMN_FILETYPE, thisSong.getFiletype());

        int row = db.update(SQLite.TABLE_NAME, values, SQLite.COLUMN_SONGID + "=?",
                new String[]{String.valueOf(thisSong.getSongid())});
        if (row == 0) {
            Log.d(TAG,"inserting="+thisSong.getSongid());
            db.insert(SQLite.TABLE_NAME, null, values);
        }
    }

    void insertFast(Context c, MainActivityInterface mainActivityInterface, SQLiteDatabase db) {
        // Insert new values or ignore rows that exist already
        String sql = "INSERT OR IGNORE INTO " + SQLite.TABLE_NAME + " ( songid, filename, folder, title ) VALUES ( ?, ?, ?, ?)";
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        ArrayList<String> songIds = mainActivityInterface.getStorageAccess().getSongIDsFromFile(c);

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

            if (!filename.isEmpty()) {
                stmt.bindString(1, s);
                stmt.bindString(2, filename);
                stmt.bindString(3, foldername);
                // Temp title for now
                // During full indexing this will be replaced
                stmt.bindString(4, filename);

                stmt.execute();
                stmt.clearBindings();
            }
        }
    }

    String getValue(Cursor cursor, String index) {
        return cursor.getString(cursor.getColumnIndexOrThrow(index));
    }

    // Search for values in the table
    ArrayList<Song> getSongsByFilters(SQLiteDatabase db, boolean searchByFolder,
                                      boolean searchByArtist, boolean searchByKey, boolean searchByTag,
                                      boolean searchByFilter, String folderVal, String artistVal,
                                      String keyVal, String tagVal, String filterVal) {
        ArrayList<Song> songs = new ArrayList<>();

        // To avoid SQL injections, we need to build the args
        ArrayList<String> args = new ArrayList<>();
        String sqlMatch = "";
        if (searchByFolder && folderVal != null && folderVal.length() > 0) {
            sqlMatch += SQLite.COLUMN_FOLDER + "= ? AND ";
            args.add(folderVal);
        }
        if (searchByArtist && artistVal != null && artistVal.length() > 0) {
            sqlMatch += SQLite.COLUMN_AUTHOR + " LIKE ? AND ";
            args.add("%"+artistVal+"%");
        }
        if (searchByKey && keyVal != null && keyVal.length() > 0) {
            sqlMatch += SQLite.COLUMN_KEY + "= ? AND ";
            args.add(keyVal);
        }
        if (searchByTag && tagVal != null && tagVal.length() > 0) {
            sqlMatch += "(" + SQLite.COLUMN_THEME + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_ALTTHEME + " LIKE ? ) AND ";
            args.add("%"+tagVal+"%");
            args.add("%"+tagVal+"%");
        }
        if (searchByFilter && filterVal != null && filterVal.length() > 0) {
            sqlMatch += "(" + SQLite.COLUMN_LYRICS + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_FILENAME + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_TITLE + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_COPYRIGHT + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_HYMNNUM + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_CCLI + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_USER1 + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_USER2 + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_USER3 + " LIKE ? )";
            args.add("%"+filterVal+"%");
            args.add("%"+filterVal+"%");
            args.add("%"+filterVal+"%");
            args.add("%"+filterVal+"%");
            args.add("%"+filterVal+"%");
            args.add("%"+filterVal+"%");
            args.add("%"+filterVal+"%");
            args.add("%"+filterVal+"%");
            args.add("%"+filterVal+"%");

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
        String getOrderBySQL = " ORDER BY listname COLLATE NOCASE ASC";
        String getBasicSQLQueryStart = "SELECT " + SQLite.COLUMN_FILENAME + ", " + SQLite.COLUMN_AUTHOR +
                ", IFNULL(NULLIF(" +  SQLite.COLUMN_TITLE  + ",'')," +  SQLite.COLUMN_FILENAME + ") as listname, " +
                SQLite.COLUMN_KEY + ", " + SQLite.COLUMN_FOLDER + ", " + SQLite.COLUMN_THEME + ", " +
                SQLite.COLUMN_ALTTHEME + ", " + SQLite.COLUMN_USER1 + ", " + SQLite.COLUMN_USER2 + ", " +
                SQLite.COLUMN_USER3 + ", " + SQLite.COLUMN_LYRICS + ", " + SQLite.COLUMN_HYMNNUM +
                " FROM " + SQLite.TABLE_NAME + " ";
        String selectQuery = getBasicSQLQueryStart + sqlMatch + " " + getOrderBySQL;

        String[] selectionArgs = new String[args.size()];
        selectionArgs = args.toArray(selectionArgs);

        Cursor cursor = db.rawQuery(selectQuery, selectionArgs);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String fi = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILENAME));
                String fo = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER));
                String au = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_AUTHOR));
                String ke = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_KEY));
                String ti = cursor.getString(cursor.getColumnIndexOrThrow("listname"));

                Song song = new Song();
                song.setFilename(fi);
                song.setFolder(fo);
                song.setAuthor(au);
                song.setKey(ke);
                song.setTitle(ti);

                songs.add(song);

                // Is this in the set?  This will add a tick for the songlist checkbox
                // String setString = getSetString(c, fo, fi);
            }
            while (cursor.moveToNext());
        }

        // close cursor connection
        closeCursor(cursor);

        //Return the songs
        return songs;
    }

    public String getKey(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder, filename);
        String[] selectionArgs = new String[]{songId};
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "= ? ";

        Cursor cursor = db.rawQuery(sql, selectionArgs);

        String key = "";
        // Get the first item (the matching songID)
        if (cursor.moveToFirst()) {
            key = getValue(cursor, SQLite.COLUMN_KEY);
        }

        if (key==null) {
            key = "";
        }

        closeCursor(cursor);
        return key;
    }

    public Song getSpecificSong(Context c, SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder, filename);
        String[] selectionArgs = new String[]{songId};
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "= ? ";
        Song thisSong = new Song();

        Cursor cursor = db.rawQuery(sql, selectionArgs);

        // Get the first item (the matching songID)
        try {
            if (cursor.moveToFirst()) {
                thisSong.setId(cursor.getInt(cursor.getColumnIndexOrThrow(SQLite.COLUMN_ID)));
                thisSong.setSongid(songId);
                thisSong.setFilename(getValue(cursor, SQLite.COLUMN_FILENAME));
                thisSong.setFolder(getValue(cursor, SQLite.COLUMN_FOLDER));
                thisSong.setTitle(getValue(cursor, SQLite.COLUMN_TITLE));
                thisSong.setAuthor(getValue(cursor, SQLite.COLUMN_AUTHOR));
                thisSong.setCopyright(getValue(cursor, SQLite.COLUMN_COPYRIGHT));
                thisSong.setLyrics(getValue(cursor, SQLite.COLUMN_LYRICS));
                thisSong.setHymnnum(getValue(cursor, SQLite.COLUMN_HYMNNUM));
                thisSong.setCcli(getValue(cursor, SQLite.COLUMN_CCLI));
                thisSong.setTheme(getValue(cursor, SQLite.COLUMN_THEME));
                thisSong.setAlttheme(getValue(cursor, SQLite.COLUMN_ALTTHEME));
                thisSong.setUser1(getValue(cursor, SQLite.COLUMN_USER1));
                thisSong.setUser2(getValue(cursor, SQLite.COLUMN_USER2));
                thisSong.setUser3(getValue(cursor, SQLite.COLUMN_USER3));
                thisSong.setKey(getValue(cursor, SQLite.COLUMN_KEY));
                thisSong.setTimesig(getValue(cursor, SQLite.COLUMN_TIMESIG));
                thisSong.setAka(getValue(cursor, SQLite.COLUMN_AKA));
                thisSong.setAutoscrolldelay(getValue(cursor, SQLite.COLUMN_AUTOSCROLL_DELAY));
                thisSong.setAutoscrolllength(getValue(cursor, SQLite.COLUMN_AUTOSCROLL_LENGTH));
                thisSong.setTempo(getValue(cursor, SQLite.COLUMN_TEMPO));
                thisSong.setPadfile(getValue(cursor, SQLite.COLUMN_PAD_FILE));
                thisSong.setPadloop(getValue(cursor, SQLite.COLUMN_PAD_LOOP));
                thisSong.setMidi(getValue(cursor, SQLite.COLUMN_MIDI));
                thisSong.setMidiindex(getValue(cursor, SQLite.COLUMN_MIDI_INDEX));
                thisSong.setCapo(getValue(cursor, SQLite.COLUMN_CAPO));
                thisSong.setCapoprint(getValue(cursor, SQLite.COLUMN_CAPO_PRINT));
                thisSong.setCustomChords((getValue(cursor, SQLite.COLUMN_CUSTOM_CHORDS)));
                thisSong.setNotes(getValue(cursor, SQLite.COLUMN_NOTES));
                thisSong.setAbc(getValue(cursor, SQLite.COLUMN_ABC));
                thisSong.setLinkyoutube(getValue(cursor, SQLite.COLUMN_LINK_YOUTUBE));
                thisSong.setLinkweb(getValue(cursor, SQLite.COLUMN_LINK_WEB));
                thisSong.setLinkaudio(getValue(cursor, SQLite.COLUMN_LINK_AUDIO));
                thisSong.setLinkother(getValue(cursor, SQLite.COLUMN_LINK_OTHER));
                thisSong.setPresentationorder(getValue(cursor, SQLite.COLUMN_PRESENTATIONORDER));
                thisSong.setFiletype(getValue(cursor, SQLite.COLUMN_FILETYPE));
            } else {
                // Song not found
                thisSong.setTitle(filename);
                thisSong.setFilename(filename);
                thisSong.setFolder(folder);
                thisSong.setLyrics("[" + folder + "/" + filename + "]\n" + c.getString(R.string.song_doesnt_exist));
            }

            closeCursor(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return thisSong;
    }

    public boolean songExists(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder, filename);
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "=\"" + songId + "\"";
        Cursor cursor = db.rawQuery(sql, null);
        int count;
        if (cursor == null) {
            // Error, so not found
            return false;
        } else {
            count = cursor.getCount();
        }
        closeCursor(cursor);
        return count > 0;
    }

    public ArrayList<String> getFolders(SQLiteDatabase db) {
        ArrayList<String> folders = new ArrayList<>();
        String q = "SELECT DISTINCT " + SQLite.COLUMN_FOLDER + " FROM " + SQLite.TABLE_NAME + " ORDER BY " +
                SQLite.COLUMN_FOLDER + " ASC";

        Cursor cursor = db.rawQuery(q, null);
        cursor.moveToFirst();

        if (cursor.getColumnCount()>0 && cursor.getColumnIndex(SQLite.COLUMN_FOLDER)==0) {
            for (int x=0; x<cursor.getCount(); x++) {
                cursor.moveToPosition(x);
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER));
                folders.add(folder);
            }
        }
        closeCursor(cursor);
        return folders;
    }

    public boolean renameSong(SQLiteDatabase db, String oldFolder, String newFolder,
                              String oldName, String newName) {
        String oldId = getAnySongId(oldFolder,oldName);
        String newId = getAnySongId(newFolder,newName);

        Log.d(TAG,"oldId: "+oldId+"  newId: "+newId);
        // First change the folder/file againts the matching old songid
        String[] whereClause = new String[]{oldId};
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLite.COLUMN_FOLDER,newFolder);
        contentValues.put(SQLite.COLUMN_FILENAME,newName);
        contentValues.put(SQLite.COLUMN_SONGID,newId);

        int val = db.update(SQLite.TABLE_NAME,contentValues,SQLite.COLUMN_SONGID+"=?",whereClause);

        Log.d(TAG,"val="+val);
        return val>0;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor!=null) {
            try {
                cursor.close();
            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getUniqueThemeTags(SQLiteDatabase db) {
        ArrayList<String> themeTags = new ArrayList<>();
        String q = "SELECT DISTINCT " + SQLite.COLUMN_THEME + " FROM " + SQLite.TABLE_NAME + " ORDER BY " +
                SQLite.COLUMN_THEME + " ASC";

        Cursor cursor = db.rawQuery(q, null);
        cursor.moveToFirst();

        Log.d(TAG,"cursor.getCount()="+cursor.getCount());
        Log.d(TAG,"cursor.getColumnCount()="+cursor.getColumnCount());
        Log.d(TAG,"ursor.getColumnIndex(SQLite.COLUMN_THEME)="+cursor.getColumnIndex(SQLite.COLUMN_THEME));

        if (cursor.getColumnCount()>0 && cursor.getColumnIndex(SQLite.COLUMN_THEME)==0) {
            for (int x=0; x<cursor.getCount(); x++) {
                cursor.moveToPosition(x);
                String themes = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_THEME));
                Log.d(TAG,"themes: "+themes);
                if (themes!=null && themes.contains(";")) {
                    String[] themeBits = themes.split(";");
                    for (String bit:themeBits) {
                        if (!themeTags.contains(bit.trim()) && !bit.trim().isEmpty()) {
                            themeTags.add(bit.trim());
                            Log.d(TAG,"adding: "+bit.trim());
                        }
                    }
                } else if (themes!=null && !themeTags.contains(themes.trim()) && !themes.trim().isEmpty()) {
                    themeTags.add(themes.trim());
                    Log.d(TAG,"adding: "+themes.trim());
                }
            }
        }
        closeCursor(cursor);
        Collections.sort(themeTags);
        return themeTags;
    }

    public String getSongsWithThemeTag(SQLiteDatabase db, String tag) {
        StringBuilder songsFound = new StringBuilder();
        String selectQuery = "SELECT " + SQLite.COLUMN_FILENAME + ", " + SQLite.COLUMN_FOLDER +
                " FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_THEME + " LIKE ?;";

        String[] args = {tag};
        Cursor cursor = db.rawQuery(selectQuery, args);

        if (cursor.moveToFirst()) {
            do {
                songsFound.append(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER))).
                        append("/").
                        append(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILENAME))).
                        append(", ");
            }
            while (cursor.moveToNext());
        }

        // close cursor connection
        closeCursor(cursor);

        // Remove the end comma
        String text = songsFound.toString();
        if (text.endsWith(", ")) {
            text = text.substring(0,text.lastIndexOf(", "));
        }
        return text;
    }

    // TODO go through the non-opensong database and populate that data
    // TODO delete stuff from the non-opensong database where the file has gone
}
