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
            db.insert(SQLite.TABLE_NAME, null, values);
        }
    }

    void insertFast(Context c, MainActivityInterface mainActivityInterface, SQLiteDatabase db) {
        // Insert new values or ignore rows that exist already
        String sql = "INSERT OR IGNORE INTO " + SQLite.TABLE_NAME + " ( songid, filename, folder ) VALUES ( ?, ?, ?)";
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

    Song getSpecificSong(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder, filename);
        String[] selectionArgs = new String[]{songId};
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "= ? ";
        Song thisSong = new Song();

        Cursor cursor = db.rawQuery(sql, selectionArgs);

        // Get the first item (the matching songID)
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
        }

        closeCursor(cursor);

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
            /*do {
                String s = cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER));
                if (s != null && !s.isEmpty()) {
                    folders.add(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER)));
                }
            } while (cursor.moveToNext());*/
        }
        closeCursor(cursor);
        return folders;
    }

    public boolean renameSong(SQLiteDatabase db, String oldFolder, String newFolder,
                           String oldName, String newName) {
        String oldId = getAnySongId(oldFolder,oldName);
        String newId = getAnySongId(newFolder,newName);

        // First change the folder/file againts the matching old songid
        String[] selectionArgs = new String[]{newId, newFolder, newName,oldId};
        String q = "UPDATE " + SQLite.TABLE_NAME + " SET " +
                SQLite.COLUMN_SONGID + " = ? ," +
                SQLite.COLUMN_FOLDER + " = ? ," +
                SQLite.COLUMN_FILENAME + " = ? WHERE " + SQLite.COLUMN_SONGID + " = ? ";
        Cursor cursor = db.rawQuery(q,selectionArgs);
        boolean success = cursor.getCount() > 0;
        closeCursor(cursor);
        return success;
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

        if (cursor.getColumnCount()>0 && cursor.getColumnIndex(SQLite.COLUMN_THEME)==0) {
            for (int x=0; x<cursor.getCount(); x++) {
                cursor.moveToPosition(x);
                String themes = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_THEME));
                if (themes.contains(";")) {
                    String[] themeBits = themes.split(";");
                    for (String bit:themeBits) {
                        if (!themeTags.contains(bit.trim()) && !bit.trim().isEmpty()) {
                            themeTags.add(bit.trim());
                        }
                    }
                } else if (!themeTags.contains(themes.trim()) && !themes.trim().isEmpty()) {
                    themeTags.add(themes.trim());
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




/*
package com.garethevans.church.opensongtablet;

        import android.content.ContentValues;
        import android.content.Context;
        import android.content.res.Configuration;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.database.sqlite.SQLiteStatement;
        import android.net.Uri;
        import android.util.Log;

        import java.io.File;
        import java.io.InputStream;
        import java.util.ArrayList;
        import java.util.Arrays;

class SQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    SQLiteHelper(Context context) {
        super(context,  SQLite.DATABASE_NAME, null, DATABASE_VERSION);
        // Don't create the database here as we don't want to recreate on each call.
    }

    SQLiteDatabase getDB (Context c) {
        try {
            File f = new File(c.getExternalFilesDir("Database"), SQLite.DATABASE_NAME);
            return SQLiteDatabase.openOrCreateDatabase(f, null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // If the table doesn't exist, create it.
        try {
            db.execSQL(SQLite.CREATE_TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    private ArrayList<String> getListOfSongs(Context c, StorageAccess storageAccess) {

        // Use the file created on app boot to get a list of songs and their locations
        File songIdsFile = new File(c.getExternalFilesDir("Database"), "SongIds.txt");

        // Add these to an arrayList
        InputStream inputStream = storageAccess.getInputStream(c, Uri.fromFile(songIdsFile));
        String filecontents = storageAccess.readTextFileToString(inputStream);
        String[] lines = filecontents.split("\n");
        return new ArrayList<>(Arrays.asList(lines));
    }

    private String escapedSQL(String s) {
        // Don't do this if already escaped
        if (s!=null) {
            s = s.replace("''", "^&*");
            s = s.replace("'", "''");
            s = s.replace("^&*", "''");
        }
        return s;
    }

    private String unescapedSQL(String s) {
        if (s!=null) {
            while (s.contains("''")) {
                s = s.replace("''","'");
            }
        }
        return s;
    }

    void createImportedSong(Context c, String folder, String filename, String title, String author,
                            String copyright, String key, String time_sig, String ccli, String lyrics) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db = getDB(c)) {
            filename = escapedSQL(filename);
            folder = escapedSQL(folder);
            String songid = escapedSQL(folder) + "/" + escapedSQL(filename);
            ContentValues values = new ContentValues();
            values.put(SQLite.COLUMN_SONGID, escapedSQL(songid));
            values.put(SQLite.COLUMN_FOLDER, escapedSQL(folder));
            values.put(SQLite.COLUMN_FILENAME, escapedSQL(filename));
            values.put(SQLite.COLUMN_TITLE, escapedSQL(title));
            values.put(SQLite.COLUMN_AUTHOR, escapedSQL(author));
            values.put(SQLite.COLUMN_COPYRIGHT, escapedSQL(copyright));
            values.put(SQLite.COLUMN_KEY, escapedSQL(key));
            values.put(SQLite.COLUMN_TIMESIG, escapedSQL(time_sig));
            values.put(SQLite.COLUMN_CCLI, escapedSQL(ccli));
            values.put(SQLite.COLUMN_LYRICS, escapedSQL(lyrics));

            // Insert the new row, returning the primary key value of the new row as long as it doesn't already exist
            String Query = "SELECT "+SQLite.COLUMN_SONGID+" FROM " + SQLite.TABLE_NAME+ " where " + SQLite.COLUMN_SONGID + " = '" + escapedSQL(songid) + "'";
            Cursor cursor = db.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                // Doesn't exist, so add it
                cursor.close();
                db.insert(SQLite.TABLE_NAME, null, values);

            } else {
                // Exists, so update it
                db.update(SQLite.TABLE_NAME, values,SQLite.COLUMN_SONGID + "=?",new String[]{escapedSQL(songid)});
                cursor.close();
            }
        }
    }

    void createSong(Context c, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db = getDB(c)) {
            if (folder == null || folder.isEmpty()) {
                folder = c.getString(R.string.mainfoldername);
            }
            filename = escapedSQL(filename);
            folder = escapedSQL(folder);
            String songid = escapedSQL(folder) + "/" + escapedSQL(filename);

            if (!songIdExists(db,songid)) {
                ContentValues values = new ContentValues();
                values.put(SQLite.COLUMN_SONGID, escapedSQL(songid));
                values.put(SQLite.COLUMN_FOLDER, escapedSQL(folder));
                values.put(SQLite.COLUMN_FILENAME, escapedSQL(filename));

                // Insert the new row, returning the primary key value of the new row
                try {
                    db.insert(SQLite.TABLE_NAME, null, values);
                } catch (Exception e) {
                    Log.d(TAG,songid + " already exists in the table, not able to create.");
                }
            }
        }
    }

    boolean songIdExists(SQLiteDatabase db, String songid) {
        String Query = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + " = \"" + escapedSQL(songid) + "\"";
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    SQLite setSong(SQLite sqLite) {
        sqLite.setTitle(StaticVariables.mTitle);
        sqLite.setAuthor(StaticVariables.mAuthor);
        sqLite.setCopyright(StaticVariables.mCopyright);
        // IV - print of pdfs need all lines in the db, previous 'shortenLyrics' logic no longer required
        sqLite.setLyrics(StaticVariables.mLyrics);
        sqLite.setHymn_num(StaticVariables.mHymnNumber);
        sqLite.setCcli(StaticVariables.mCCLI);
        sqLite.setTheme(StaticVariables.mTheme);
        sqLite.setAlttheme(StaticVariables.mAltTheme);
        sqLite.setUser1(StaticVariables.mUser1);
        sqLite.setUser2(StaticVariables.mUser2);
        sqLite.setUser3(StaticVariables.mUser3);
        sqLite.setKey(StaticVariables.mKey);
        sqLite.setTimesig(StaticVariables.mTimeSig);
        sqLite.setAka(StaticVariables.mAka);
        return sqLite;
    }

    void updateSong(Context c, SQLite sqLite) {

        try (SQLiteDatabase db = getDB(c)) {
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
            values.put(SQLite.COLUMN_TEMPO, escapedSQL(sqLite.getTempo()));
            values.put(SQLite.COLUMN_AKA, escapedSQL(sqLite.getAka()));

            long l = db.update(SQLite.TABLE_NAME, values, SQLite.COLUMN_ID + "=?", new String[]{String.valueOf(sqLite.getId())});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    SQLite getSong(Context c, String songid) {
        SQLiteDatabase db = getDB(c);
        try {
            Cursor cursor = db.query(SQLite.TABLE_NAME,
                    new String[]{SQLite.COLUMN_ID, SQLite.COLUMN_SONGID, SQLite.COLUMN_FILENAME, SQLite.COLUMN_FOLDER,
                            SQLite.COLUMN_TITLE, SQLite.COLUMN_AUTHOR, SQLite.COLUMN_COPYRIGHT, SQLite.COLUMN_LYRICS,
                            SQLite.COLUMN_HYMNNUM, SQLite.COLUMN_CCLI, SQLite.COLUMN_THEME,
                            SQLite.COLUMN_ALTTHEME, SQLite.COLUMN_USER1, SQLite.COLUMN_USER2,
                            SQLite.COLUMN_USER3, SQLite.COLUMN_KEY, SQLite.COLUMN_TIMESIG, SQLite.COLUMN_TEMPO, SQLite.COLUMN_AKA},
                    SQLite.COLUMN_SONGID + "=?",
                    new String[]{String.valueOf((songid))}, null, null, SQLite.COLUMN_FILENAME, null);

            if (cursor != null) {
                cursor.moveToFirst();

                try {
                    // prepare note object
                    SQLite sqLite = new SQLite(
                            cursor.getInt(cursor.getColumnIndex(SQLite.COLUMN_ID)),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_SONGID))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_TITLE))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_COPYRIGHT))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_LYRICS))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_HYMNNUM))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_CCLI))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_THEME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_ALTTHEME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER1))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER2))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER3))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_KEY))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_TIMESIG))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_TEMPO))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AKA))));

                    // close the db connection
                    cursor.close();
                    db.close();
                    return sqLite;
                } catch (Exception e) {
                    Log.d(TAG, "Song not found");
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

    void updateFolderName(Context c, String oldFolder, String newFolder) {
        // Select matching folder Query
        String selectQuery = "SELECT "+SQLite.COLUMN_SONGID + ", " +
                SQLite.COLUMN_FOLDER + ", " +
                SQLite.COLUMN_FILENAME + " " +
                "FROM " + SQLite.TABLE_NAME +
                " WHERE " + SQLite.COLUMN_SONGID + " LIKE '%" + escapedSQL(oldFolder) + "/%'" +
                " ORDER BY " + SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String currSongId = cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_SONGID));
                    String updatedId = currSongId.replace(oldFolder + "/", newFolder + "/");
                    ContentValues values = new ContentValues();
                    values.put(SQLite.COLUMN_SONGID, escapedSQL(updatedId));
                    values.put(SQLite.COLUMN_FOLDER, escapedSQL(newFolder));

                    db.update(SQLite.TABLE_NAME, values, SQLite.COLUMN_SONGID + "=?", new String[]{escapedSQL(currSongId)});

                } while (cursor.moveToNext());
            }

            try {
                cursor.close();
                // close db connection
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    void deleteSong(Context c, String songId) {
        try (SQLiteDatabase db = getDB(c)) {
            db.delete(SQLite.TABLE_NAME, SQLite.COLUMN_SONGID + " = ?",
                    new String[]{String.valueOf(escapedSQL(songId))});
        }
    }

    ArrayList<SQLite> getSongsInFolder(Context c, String whichSongFolder) {
        ArrayList<SQLite> songs = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();


        // Select matching folder Query
        String selectQuery = "SELECT "+SQLite.COLUMN_FILENAME + ", " +
                SQLite.COLUMN_AUTHOR + ", " +
                SQLite.COLUMN_KEY + " " +
                "FROM " + SQLite.TABLE_NAME +
                " WHERE " + SQLite.COLUMN_FOLDER + "='" + escapedSQL(whichSongFolder) + "'" +
                " ORDER BY " + SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    SQLite sqLite = new SQLite();
                    sqLite.setFilename(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME))));
                    sqLite.setAuthor(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR))));
                    sqLite.setKey(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_KEY))));
                    if (!sqLite.getFilename().equals("") && !files.contains("$__" + sqLite.getFolder() + "/" + sqLite.getFilename() + "__$")) {
                        // This avoids adding references to folders more than once
                        songs.add(sqLite);
                        files.add("$__" + sqLite.getFolder() + "/" + sqLite.getFilename() + "__$");
                    }
                } while (cursor.moveToNext());
            }

            // close db connection
            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // return songs in this folder
        return songs;
    }

    ArrayList<String> getFolders(Context c) {
        // Get the database
        try (SQLiteDatabase db = getDB(c)) {
            String q = "SELECT DISTINCT " + SQLite.COLUMN_FOLDER + " FROM " + SQLite.TABLE_NAME + " ORDER BY " +
                    SQLite.COLUMN_FOLDER + " ASC";
            ArrayList<String> folders = new ArrayList<>();

            Cursor cursor = db.rawQuery(q, null);
            cursor.moveToFirst();
            do {
                folders.add(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER))));
            } while (cursor.moveToNext());
            cursor.close();
            //db.close();
            return folders;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    ArrayList<SQLite> getChildFolders(Context c, String whichSongFolder) {
        // This skims through all of the folders and displays folders that are children of the current folder
        // These are added to the top of the song menu
        ArrayList<SQLite> childFolders = new ArrayList<>();
        ArrayList<String> allFolders = getFolders(c);

        StringBuilder folderadded = new StringBuilder();

        // Iterate through all the folders
        for (String f:allFolders) {

            if (!f.equals(c.getString(R.string.mainfoldername)) && !f.equals("MAIN") &&
                    (whichSongFolder.equals(c.getString(R.string.mainfoldername)) || whichSongFolder.equals("MAIN") ||
                            whichSongFolder.equals(""))) {
                // We are viewing items in the MAIN folder and the found folder isn't MAIN
                if (f.contains("/")) {
                    // Just show the first bit
                    f = f.substring(0,f.indexOf("/"));
                }
                if (f.startsWith("/")) {
                    f = f.replaceFirst("/","");
                }
                if (f.endsWith("/")) {
                    f = f.substring(0,f.length()-1);
                }
                if (!folderadded.toString().contains("$__"+f+"__$")) {
                    SQLite sqLite = new SQLite();
                    sqLite.setFilename(f);
                    sqLite.setAuthor("");
                    sqLite.setKey(c.getString(R.string.songsinfolder));
                    childFolders.add(sqLite);
                    folderadded.append("$__").append(f).append("__$");
                }

            } else if (!f.equals(c.getString(R.string.mainfoldername)) && !f.equals("MAIN") && !f.equals(whichSongFolder) &&
                    f.contains("/") && f.contains(whichSongFolder)) {
                // The found folder includes the current folder, but isn't just the current folder
                // Strip out the current folder prefix
                try {
                    f = f.substring(f.indexOf(StaticVariables.whichSongFolder) + StaticVariables.whichSongFolder.length() + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // If it still has a '/', only include the first direct child
                if (f.contains("/")) {
                    f = f.substring(0,f.indexOf("/"));
                }
                if (f.startsWith("/")) {
                    f = f.replaceFirst("/","");
                }
                if (f.endsWith("/")) {
                    f = f.substring(0,f.length()-1);
                }
                if (!folderadded.toString().contains("$__"+f+"__$")) {
                    SQLite sqLite = new SQLite();
                    sqLite.setFilename(f);
                    sqLite.setAuthor("");
                    sqLite.setKey(c.getString(R.string.songsinfolder));
                    childFolders.add(sqLite);
                    folderadded.append("$__").append(f).append("__$");
                }
            }
        }

        return childFolders;
    }

    ArrayList<SQLite> getAllSongs(Context c) {
        ArrayList<SQLite> songs = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + SQLite.TABLE_NAME + " ORDER BY " +
                SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    SQLite sqLite = new SQLite();
                    sqLite.setId(cursor.getInt(cursor.getColumnIndex(SQLite.COLUMN_ID)));
                    sqLite.setSongid(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_SONGID))));
                    sqLite.setFilename(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME))));
                    sqLite.setFolder(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER))));
                    sqLite.setTitle(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_TITLE))));
                    sqLite.setAuthor(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR))));
                    sqLite.setCopyright(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_COPYRIGHT))));
                    sqLite.setLyrics(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_LYRICS))));
                    sqLite.setHymn_num(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_HYMNNUM))));
                    sqLite.setCcli(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_CCLI))));
                    sqLite.setTheme(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_THEME))));
                    sqLite.setAlttheme(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_ALTTHEME))));
                    sqLite.setUser1(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER1))));
                    sqLite.setUser2(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER2))));
                    sqLite.setUser3(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER3))));
                    sqLite.setKey(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_KEY))));
                    sqLite.setAka(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AKA))));

                    if (sqLite.getFilename() != null && !sqLite.getFilename().isEmpty() &&
                            !sqLite.getFilename().equals(" ") && !sqLite.getFilename().equals("(invalid)")) {
                        songs.add(sqLite);
                    }
                } while (cursor.moveToNext());
            }

            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // close db connection
        // return notes list
        return songs;
    }

    int getSongsCount(Context c) {
        int count;
        try (SQLiteDatabase db = getDB(c)) {
            String countQuery = "SELECT  * FROM " + SQLite.TABLE_NAME;
            Cursor cursor = db.rawQuery(countQuery, null);
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    private void emptyTable(SQLiteDatabase db) {
        // This drops the table if it exists (wipes it ready to start again)
        try {
            db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void resetDatabase(Context c) {
        try (SQLiteDatabase db = getDB(c)) {
            emptyTable(db);
            onCreate(db);
        }
    }

    // insert data using transaction and prepared statement
    void insertFast(Context c, StorageAccess storageAccess) {
        SQLiteDatabase db = getDB(c);
        try {
            // Insert new values or ignore rows that exist already
            String sql = "INSERT OR IGNORE INTO " + SQLite.TABLE_NAME + " ( songid, filename, folder ) VALUES ( ?, ?, ?)";
            db.beginTransactionNonExclusive();
            SQLiteStatement stmt = db.compileStatement(sql);
            ArrayList<String> songIds = getListOfSongs(c, storageAccess);

            // Lookout for language changes messing with the mainfoldername
            Configuration configuration = new Configuration(c.getResources().getConfiguration());
            configuration.setLocale(StaticVariables.locale);
            String newmain = c.createConfigurationContext(configuration).getResources().getString(R.string.mainfoldername);

            for (String s : songIds) {

                String filename;
                String foldername;
                //if (!s.endsWith("/")) {
                // Only add song files, so if it ends with / this loop skips
                if (s.endsWith("/")) {
                    filename = "";
                    foldername = s.substring(0, s.lastIndexOf("/"));
                } else if (s.contains("/")) {
                    filename = s.substring(s.lastIndexOf("/"));
                    foldername = s.replace(filename, "");
                } else {
                    filename = s;
                    foldername = newmain;
                }

                filename = filename.replace("/", "");

                //stmt.bindString(1, escapedSQL(s));
                //stmt.bindString(2, escapedSQL(filename));
                //stmt.bindString(3, escapedSQL(foldername));

                stmt.bindString(1, s);
                stmt.bindString(2, filename);
                stmt.bindString(3, foldername);

                try {
                    stmt.execute();
                    stmt.clearBindings();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (Exception e) {
            db.setTransactionSuccessful();
            db.endTransaction();
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

}*/