package com.garethevans.church.opensongtablet.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.drummer.DrumCalculations;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.nearby.ShareableObject;
import com.garethevans.church.opensongtablet.openchords.OpenChordsTag;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongId;

import org.apache.commons.lang3.StringUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class CommonSQL {
    // This is used to perform common tasks for the SQL database and NonOpenSongSQL database.
    // Only the database itself is different, so as long as that is dealt with separately, we can proceed
    // When we return an SQLite object

    // Update the table.  Called for the NonOpenSong database that is persistent.
    // This is called if the db2 version is different to the version stated in NonOpenSongSQLiteHelper
    // This check we have the columns we need now

    private final String TAG = "CommonSQL";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    public CommonSQL(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    void updateTable(SQLiteDatabase db2) {
        // This is called if the database version changes.  It will attempt to add each column
        // It will throw an error if it already exists, but we will catch it
        String[] columnNames = {SQLite.COLUMN_ID, SQLite.COLUMN_SONGID, SQLite.COLUMN_FILENAME,
                SQLite.COLUMN_FOLDER, SQLite.COLUMN_TITLE, SQLite.COLUMN_AUTHOR,
                SQLite.COLUMN_COPYRIGHT, SQLite.COLUMN_LYRICS, SQLite.COLUMN_HYMNNUM,
                SQLite.COLUMN_CCLI, SQLite.COLUMN_THEME, SQLite.COLUMN_ALTTHEME, SQLite.COLUMN_USER1,
                SQLite.COLUMN_USER2, SQLite.COLUMN_USER3, SQLite.COLUMN_BEATBUDDY_SONG,
                SQLite.COLUMN_BEATBUDDY_KIT, SQLite.COLUMN_DRUMMER, SQLite.COLUMN_DRUMMER_KIT,
                SQLite.COLUMN_KEY, SQLite.COLUMN_KEY_ORIGINAL,
                SQLite.COLUMN_PREFERRED_INSTRUMENT, SQLite.COLUMN_UUID, SQLite.COLUMN_LAST_MODIFIED,
                SQLite.COLUMN_TIMESIG, SQLite.COLUMN_AKA, SQLite.COLUMN_AUTOSCROLL_DELAY,
                SQLite.COLUMN_AUTOSCROLL_LENGTH, SQLite.COLUMN_TEMPO, SQLite.COLUMN_PAD_FILE,
                SQLite.COLUMN_PAD_LOOP, SQLite.COLUMN_MIDI, SQLite.COLUMN_MIDI_INDEX, SQLite.COLUMN_CAPO,
                SQLite.COLUMN_CAPO_PRINT, SQLite.COLUMN_CUSTOM_CHORDS, SQLite.COLUMN_NOTES, SQLite.COLUMN_ABC,
                SQLite.COLUMN_ABC_TRANSPOSE, SQLite.COLUMN_LINK_YOUTUBE, SQLite.COLUMN_LINK_YOUTUBE,
                SQLite.COLUMN_LINK_WEB, SQLite.COLUMN_LINK_AUDIO, SQLite.COLUMN_LINK_OTHER,
                SQLite.COLUMN_PRESENTATIONORDER, SQLite.COLUMN_PREVIEWOVERRIDE, SQLite.COLUMN_FILETYPE};

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
        if (folder == null || folder.isEmpty()) {
            folder = c.getString(R.string.mainfoldername);
        }
        return folder + "/" + filename;
    }
    public ArrayList<String> getFolderFilenameFromId(String songId) {
        ArrayList<String> folderFilename = new ArrayList<>();
        String[] bits = songId.split("_");
        StringBuilder folderBits = new StringBuilder();
        String filename = "";
        for (int i=0; i<bits.length; i++) {
            if (i==bits.length-1) {
                filename = bits[i];
            } else {
                folderBits.append(bits[i]).append("/");
            }
        }
        folderFilename.add(folderBits.toString());
        folderFilename.add(filename);
        return folderFilename;
    }

    boolean songIdExists(SQLiteDatabase db, String songid) {
        String[] selectionArgs = new String[]{escape(songid)};
        String Query = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + " = ? ";
        Cursor cursor = db.rawQuery(Query, selectionArgs);
        boolean exists = cursor.getCount() > 0;
        closeCursor(cursor);
        return exists;
    }

    // Create, delete and update
    void createSong(SQLiteDatabase db, String folder, String filename) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        if (folder == null || folder.isEmpty()) {
            folder = c.getString(R.string.mainfoldername);
        }
        folder = mainActivityInterface.getStorageAccess().safeFilename(folder);
        filename = mainActivityInterface.getStorageAccess().safeFilename(filename);

        String songid = getAnySongId(folder, filename);

        // If it doesn't already exist, create it
        if (!songIdExists(db, songid)) {
            ContentValues values = new ContentValues();
            values.put(SQLite.COLUMN_SONGID, songid);
            values.put(SQLite.COLUMN_FOLDER, folder);
            values.put(SQLite.COLUMN_FILENAME, filename);
            values.put(SQLite.COLUMN_UUID, UUID.randomUUID().toString());

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
        String correctId = getAnySongId(thisSong.getFolder(), thisSong.getFilename());
        if (thisSong.getSongid() == null || thisSong.getSongid().isEmpty() || !thisSong.getSongid().equals(correctId)) {
            thisSong.setSongid(correctId);
        }
        String uuid = thisSong.getUuid();
        if (uuid == null || uuid.isEmpty()) {
            thisSong.setUuid(String.valueOf(UUID.randomUUID()));
        }
        String lastModified = thisSong.getLastModified();
        if (lastModified == null || lastModified.isEmpty()) {
            thisSong.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
        }

        ContentValues values = new ContentValues();
        values.put(SQLite.COLUMN_SONGID, thisSong.getSongid());
        values.put(SQLite.COLUMN_UUID, thisSong.getUuid());
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
        values.put(SQLite.COLUMN_BEATBUDDY_SONG, thisSong.getBeatbuddysong());
        values.put(SQLite.COLUMN_BEATBUDDY_KIT, thisSong.getBeatbuddykit());
        values.put(SQLite.COLUMN_DRUMMER, thisSong.getDrummer());
        values.put(SQLite.COLUMN_DRUMMER_KIT, thisSong.getDrummerKit());
        values.put(SQLite.COLUMN_KEY, thisSong.getKey());
        values.put(SQLite.COLUMN_KEY_ORIGINAL, thisSong.getKeyOriginal());
        values.put(SQLite.COLUMN_PREFERRED_INSTRUMENT, thisSong.getPreferredInstrument());
        values.put(SQLite.COLUMN_PREVIEWOVERRIDE, thisSong.getPreviewoverride());
        values.put(SQLite.COLUMN_TIMESIG, DrumCalculations.getFixedTimeSignatureString(thisSong.getTimesig(), false));
        values.put(SQLite.COLUMN_AKA, thisSong.getAka());
        values.put(SQLite.COLUMN_AUTOSCROLL_DELAY, thisSong.getAutoscrolldelay());
        values.put(SQLite.COLUMN_AUTOSCROLL_LENGTH, thisSong.getAutoscrolllength());
        //if (mainActivityInterface.getDrumViewModel().getBpm()>-1) {
        values.put(SQLite.COLUMN_TEMPO, DrumCalculations.getFixedTempoString(thisSong.getTempo(), false));
        /*} else {
            values.put(SQLite.COLUMN_TEMPO, "");
        }*/
        values.put(SQLite.COLUMN_PAD_FILE, thisSong.getPadfile());
        values.put(SQLite.COLUMN_PAD_LOOP, thisSong.getPadloop());
        values.put(SQLite.COLUMN_MIDI, thisSong.getMidi());
        values.put(SQLite.COLUMN_MIDI_INDEX, thisSong.getMidiindex());
        values.put(SQLite.COLUMN_CAPO, thisSong.getCapo());
        values.put(SQLite.COLUMN_CAPO_PRINT, thisSong.getCapoprint());
        values.put(SQLite.COLUMN_CUSTOM_CHORDS, thisSong.getCustomchords());
        values.put(SQLite.COLUMN_NOTES, thisSong.getNotes());
        values.put(SQLite.COLUMN_ABC, thisSong.getAbc());
        values.put(SQLite.COLUMN_ABC_TRANSPOSE, thisSong.getAbcTranspose());
        values.put(SQLite.COLUMN_LINK_YOUTUBE, thisSong.getLinkyoutube());
        values.put(SQLite.COLUMN_LINK_WEB, thisSong.getLinkweb());
        values.put(SQLite.COLUMN_LINK_AUDIO, thisSong.getLinkaudio());
        values.put(SQLite.COLUMN_LINK_OTHER, thisSong.getLinkother());
        values.put(SQLite.COLUMN_PRESENTATIONORDER, thisSong.getPresentationorder());
        values.put(SQLite.COLUMN_FILETYPE, thisSong.getFiletype());
        values.put(SQLite.COLUMN_LAST_MODIFIED, thisSong.getLastModified());

        int row = db.update(SQLite.TABLE_NAME, values, SQLite.COLUMN_SONGID + "=?",
                new String[]{String.valueOf(thisSong.getSongid())});
        if (row == 0) {
            try {
                db.insert(SQLite.TABLE_NAME, null, values);
            } catch (Exception e) {
                Log.e(TAG, "error:" + e);
            }
        }
    }

    public void removeOldSongs(SQLiteDatabase db, ArrayList<String> songIds) {
        // Remove entries in the database that aren't in the songIds
        StringBuilder inQuery = new StringBuilder();
        inQuery.append("(");
        boolean first = true;
        for (String item : songIds) {
            if (first) {
                first = false;
                inQuery.append("'").append(escape(item)).append("'");
            } else {
                inQuery.append(", '").append(escape(item)).append("'");
            }
        }
        inQuery.append(")");
        db.delete(SQLite.TABLE_NAME, SQLite.COLUMN_SONGID + " NOT IN " + inQuery, null);
    }

    public void insertFast(SQLiteDatabase db) {
        // Insert new values or ignore rows that exist already
        String sql = "INSERT OR IGNORE INTO " + SQLite.TABLE_NAME + " ( songid, filename, folder, title ) VALUES ( ?, ?, ?, ?)";
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        ArrayList<String> songIds = mainActivityInterface.getStorageAccess().getSongIDsFromFile();

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

    public String getValue(Cursor cursor, String index) {
        return cursor.getString(cursor.getColumnIndexOrThrow(index));
    }

    // Search for values in the table
    public ArrayList<Song> getSongsByFilters(SQLiteDatabase db, boolean searchByFolder,
                                             boolean searchByArtist, boolean searchByKey, boolean searchByTag,
                                             boolean searchByFilter, boolean searchByTitle, String folderVal,
                                             String artistVal, String keyVal, String tagVal,
                                             String filterVal, String titleVal, boolean songMenuSortTitles) {
        ArrayList<Song> songs = new ArrayList<>();

        // To avoid SQL injections, we need to build the args
        ArrayList<String> args = new ArrayList<>();
        String sqlMatch = "";
        if (searchByFolder && folderVal != null && !folderVal.isEmpty()) {
            if (folderVal.equals(mainActivityInterface.getMainfoldername()) ||
                    folderVal.equals("MAIN")) {
                sqlMatch += SQLite.COLUMN_FOLDER + "= ? AND ";
                args.add(folderVal);
            } else {
                // Allows the inclusion of subfolders contents
                sqlMatch += SQLite.COLUMN_FOLDER + " LIKE ? AND ";
                args.add(folderVal + "%");
            }
        }
        if (searchByArtist && artistVal != null && !artistVal.isEmpty()) {
            sqlMatch += SQLite.COLUMN_AUTHOR + " LIKE ? AND ";
            args.add("%" + artistVal + "%");
        }
        if (searchByKey && keyVal != null && !keyVal.isEmpty()) {
            sqlMatch += SQLite.COLUMN_KEY + "= ? AND ";
            args.add(keyVal);
        }
        if (searchByTag && tagVal != null && !tagVal.isEmpty()) {
            String[] tagArray = StringUtils.splitPreserveAllTokens(tagVal, ";");
            if (tagArray.length > 0) {
                sqlMatch += "(";
                StringBuilder tempSqlMatch = new StringBuilder();
                for (int i = 0, max = tagArray.length; i < max; i++) {
                    //sqlMatch += SQLite.COLUMN_THEME + " LIKE ? OR " + SQLite.COLUMN_ALTTHEME + " LIKE ?";
                    tempSqlMatch.append(SQLite.COLUMN_THEME).append(" LIKE ? OR ").append(SQLite.COLUMN_ALTTHEME).append(" LIKE ?");
                    if (i < max - 1) {
                        tempSqlMatch.append(" OR ");
                        //sqlMatch += " OR ";
                    }
                    args.add("%" + tagArray[i] + "%");
                    args.add("%" + tagArray[i] + "%");
                }
                sqlMatch += tempSqlMatch + ") AND ";
            }
        }
        if (searchByTitle && titleVal != null && !titleVal.isEmpty()) {
            sqlMatch += "(" + SQLite.COLUMN_TITLE + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_FILENAME + " LIKE ? ) AND ";
            args.add("%" + titleVal + "%");
            args.add("%" + titleVal + "%");
        }
        if (searchByFilter && filterVal != null && !filterVal.isEmpty()) {
            sqlMatch += "(" + SQLite.COLUMN_LYRICS + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_FILENAME + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_TITLE + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_COPYRIGHT + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_HYMNNUM + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_CCLI + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_USER1 + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_USER2 + " LIKE ? OR ";
            sqlMatch += SQLite.COLUMN_USER3 + " LIKE ? )";
            args.add("%" + filterVal + "%");
            args.add("%" + filterVal + "%");
            args.add("%" + filterVal + "%");
            args.add("%" + filterVal + "%");
            args.add("%" + filterVal + "%");
            args.add("%" + filterVal + "%");
            args.add("%" + filterVal + "%");
            args.add("%" + filterVal + "%");
            args.add("%" + filterVal + "%");
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

        /*
            Select matching folder Query
            Common strings for searching.  Don't need to grab everything here - we can get the rest later
        */

        String selectQuery = getSelectQuery(songMenuSortTitles, sqlMatch);

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
                String ti = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_TITLE));
                String th = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_THEME));
                String at = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_ALTTHEME));

                Song song = new Song();
                song.setFilename(fi);
                song.setFolder(fo);
                song.setAuthor(au);
                song.setKey(ke);
                song.setTitle(ti);
                song.setTheme(th);
                song.setAlttheme(at);

                songs.add(song);

                // Is this in the set?  This will add a tick for the songlist checkbox
                // String setString = getSetString(c, fo, fi);
            }
            while (cursor.moveToNext());
        }

        // close cursor connection
        closeCursor(cursor);

        // Because the song sorting from SQL ignores accented characters (non-English),
        // we need to set up a custom collator
        Comparator<Song> comparator = (o1, o2) -> {
            //Collator collator = Collator.getInstance(mainActivityInterface.getLocale());
            Collator collator = Collator.getInstance();
            collator.setStrength(Collator.SECONDARY);
            if (songMenuSortTitles) {
                return collator.compare(o1.getTitle(), o2.getTitle());
            } else {
                return collator.compare(o1.getFilename(), o2.getFilename());
            }
        };
        Collections.sort(songs, comparator);

        //Return the songs
        return songs;
    }

    // This checks the database for files that don't actually exist
    // A reference to the Song object (folder/filename only) is returned
    public ArrayList<Song> getNonExistingSongsInDB(SQLiteDatabase db) {
        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursor = db.rawQuery(getFolderFilenameQuery(), null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String fi = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILENAME));
                String fo = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER));

                // Check if the file exists
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", fo, fi);
                if (!mainActivityInterface.getStorageAccess().uriExists(uri)) {
                    // The file doesn't exist, so add it to the return list
                    Song song = new Song();
                    song.setFilename(fi);
                    song.setFolder(fo);
                    songs.add(song);
                }
            }
            while (cursor.moveToNext());
        }

        // close cursor connection
        try {
            closeCursor(cursor);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Return the songs that don't actually exist
        return songs;
    }

    private String getAllQuery() {
        return "SELECT * FROM " + SQLite.TABLE_NAME + " ORDER BY " + SQLite.COLUMN_FILENAME + " ASC";
    }

    private String getFolderFilenameQuery() {
        return "SELECT " + SQLite.COLUMN_FILENAME + ", " + SQLite.COLUMN_FOLDER + " FROM " + SQLite.TABLE_NAME;
    }

    private String getSelectQuery(boolean songMenuSortTitles, String sqlMatch) {
        String listname = SQLite.COLUMN_FILENAME;
        if (songMenuSortTitles) {
            listname = SQLite.COLUMN_TITLE;
        }

        String getOrderBySQL = " ORDER BY " + listname + " COLLATE NOCASE ASC";
        String getBasicSQLQueryStart = "SELECT " + SQLite.COLUMN_FILENAME + ", " + SQLite.COLUMN_AUTHOR +
                ", IFNULL(NULLIF(" + SQLite.COLUMN_TITLE + ",'')," + SQLite.COLUMN_FILENAME + ") AS " + SQLite.COLUMN_TITLE + ", " +
                SQLite.COLUMN_KEY + ", " + SQLite.COLUMN_FOLDER + ", " + SQLite.COLUMN_THEME + ", " +
                SQLite.COLUMN_ALTTHEME + ", " + SQLite.COLUMN_USER1 + ", " + SQLite.COLUMN_USER2 + ", " +
                SQLite.COLUMN_USER3 + ", " + SQLite.COLUMN_LYRICS + ", " + SQLite.COLUMN_HYMNNUM +
                " FROM " + SQLite.TABLE_NAME + " ";
        return getBasicSQLQueryStart + sqlMatch + " " + getOrderBySQL;
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

        if (key == null) {
            key = "";
        }

        closeCursor(cursor);
        return key;
    }

    public Song getSpecificSong(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder, filename);
        String[] selectionArgs = new String[]{songId};
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "= ? ";
        Song thisSong = new Song();

        Cursor cursor = db.rawQuery(sql, selectionArgs);

        // Get the first item (the matching songID)
        try {
            if (cursor.moveToFirst()) {
                setSongValues(thisSong, cursor);

            } else {
                // Song not found
                thisSong.setTitle(filename);
                thisSong.setFilename(filename);
                thisSong.setFolder(folder);
                thisSong.setLyrics("[" + folder + "/" + filename + "]\n" + c.getString(R.string.song_doesnt_exist));
            }

            closeCursor(cursor);
        } catch (Exception e) {
            Log.e(TAG, "error:" + e);
        }

        return thisSong;
    }

    public Song getSongFromUuid(SQLiteDatabase db, String uuid) {
        if (uuid != null && !uuid.isEmpty()) {
            String[] selectionArgs = new String[]{uuid};
            String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_UUID + "= ? ";
            Song thisSong = new Song();

            Cursor cursor = db.rawQuery(sql, selectionArgs);

            // Get the first item (the matching Uuid)
            try {
                if (cursor.moveToFirst()) {
                    setSongValues(thisSong, cursor);

                } else {
                    thisSong = null;
                }

                closeCursor(cursor);
            } catch (Exception e) {
                Log.e(TAG, "error:" + e);
            }
            return thisSong;
        } else {
            return null;
        }
    }

    private void setSongValues(Song thisSong, Cursor cursor) {
        thisSong.setId(cursor.getInt(cursor.getColumnIndexOrThrow(SQLite.COLUMN_ID)));
        thisSong.setFilename(getValue(cursor, SQLite.COLUMN_FILENAME));
        thisSong.setFolder(getValue(cursor, SQLite.COLUMN_FOLDER));
        thisSong.setSongid(getAnySongId(thisSong.getFolder(), thisSong.getFilename()));
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
        thisSong.setBeatbuddysong(getValue(cursor, SQLite.COLUMN_BEATBUDDY_SONG));
        thisSong.setBeatbuddykit(getValue(cursor, SQLite.COLUMN_BEATBUDDY_KIT));
        thisSong.setDrummer(getValue(cursor, SQLite.COLUMN_DRUMMER));
        thisSong.setDrummerKit(getValue(cursor, SQLite.COLUMN_DRUMMER_KIT));
        thisSong.setKey(getValue(cursor, SQLite.COLUMN_KEY));
        thisSong.setKeyOriginal(getValue(cursor, SQLite.COLUMN_KEY_ORIGINAL));
        thisSong.setPreferredInstrument(getValue(cursor, SQLite.COLUMN_PREFERRED_INSTRUMENT));
        thisSong.setPreviewoverride(getValue(cursor, SQLite.COLUMN_PREVIEWOVERRIDE));
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
        thisSong.setAbcTranspose(getValue(cursor, SQLite.COLUMN_ABC_TRANSPOSE));
        thisSong.setLinkyoutube(getValue(cursor, SQLite.COLUMN_LINK_YOUTUBE));
        thisSong.setLinkweb(getValue(cursor, SQLite.COLUMN_LINK_WEB));
        thisSong.setLinkaudio(getValue(cursor, SQLite.COLUMN_LINK_AUDIO));
        thisSong.setLinkother(getValue(cursor, SQLite.COLUMN_LINK_OTHER));
        thisSong.setPresentationorder(getValue(cursor, SQLite.COLUMN_PRESENTATIONORDER));
        thisSong.setFiletype(getValue(cursor, SQLite.COLUMN_FILETYPE));
        String uuid = getValue(cursor, SQLite.COLUMN_UUID);
        if (uuid == null || uuid.isEmpty()) {
            uuid = String.valueOf(UUID.randomUUID());
        }
        thisSong.setUuid(uuid);
        thisSong.setLastModified(getValue(cursor, SQLite.COLUMN_LAST_MODIFIED));
    }

    public Song getOpenChordsSong(SQLiteDatabase db, String folder, String uuid) {
        String[] selectionArgs = new String[]{uuid, folder};
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_UUID + "=? AND " + SQLite.COLUMN_FOLDER + "=?";
        Song thisSong = new Song();

        Cursor cursor = db.rawQuery(sql, selectionArgs);

        // Get the first item (the matching songID)
        try {
            if (cursor.moveToFirst()) {
                setSongValues(thisSong, cursor);
            }
            closeCursor(cursor);
        } catch (Exception e) {
            Log.e(TAG, "error:" + e);
        }
        return thisSong;
    }

    public boolean songExists(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder, filename);
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "= ? ";
        Cursor cursor = db.rawQuery(sql, new String[]{songId});
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

    public ArrayList<Song> openChordsSyncGetSongsFromFolder(SQLiteDatabase db, String folder) {
        ArrayList<Song> songs = new ArrayList<>();

        // To avoid SQL injections, we need to build the args
        String[] selectionArgs = new String[]{folder};
        String sqlQuery = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_FOLDER + "=? ORDER BY filename ASC";
        Cursor cursor = db.rawQuery(sqlQuery, selectionArgs);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String fi = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILENAME));
                String fo = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER));
                String ui = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_UUID));
                String lm = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LAST_MODIFIED));
                String au = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_AUTHOR));
                String ke = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_KEY));
                String ca = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_CAPO));
                String th = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_THEME));
                String at = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_ALTTHEME));
                String ly = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LYRICS));
                String du = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_AUTOSCROLL_LENGTH));
                String te = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_TEMPO));
                String ti = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_TIMESIG));
                String no = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_NOTES));
                String co = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_COPYRIGHT));
                String cc = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_CCLI));
                String po = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_PRESENTATIONORDER));

                Song song = new Song();
                song.setFilename(fi);
                song.setFolder(fo);
                song.setUuid(ui);
                song.setLastModified(lm);
                song.setAuthor(au);
                song.setKey(ke);
                song.setCapo(ca);
                song.setTitle(fi);
                song.setTheme(th);
                song.setAlttheme(at);
                song.setLyrics(ly);
                song.setAutoscrolllength(du);
                song.setTempo(te);
                song.setTimesig(ti);
                song.setNotes(no);
                song.setCopyright(co);
                song.setCcli(cc);
                song.setPresentationorder(po);

                songs.add(song);

            }
            while (cursor.moveToNext());
        }

        // close cursor connection
        closeCursor(cursor);

        // Because the song sorting from SQL ignores accented characters (non-English),
        // we need to set up a custom collator
        Comparator<Song> comparator = (o1, o2) -> {
            //Collator collator = Collator.getInstance(mainActivityInterface.getLocale());
            Collator collator = Collator.getInstance();
            collator.setStrength(Collator.SECONDARY);
            return collator.compare(o1.getFilename(), o2.getFilename());
        };
        Collections.sort(songs, comparator);

        //Return the songs
        return songs;
    }


    public Song getSongFromMidiIndex(SQLiteDatabase db, int midiIndex) {
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_MIDI_INDEX + "= ? ";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(midiIndex)});
        Song thisSong = new Song();

        // Get the first item (the matching songID)
        try {
            if (cursor.moveToFirst()) {
                thisSong.setFilename(getValue(cursor, SQLite.COLUMN_FILENAME));
                thisSong.setFolder(getValue(cursor, SQLite.COLUMN_FOLDER));
                cursor.close();
                return thisSong;
            }
        } catch (Exception e) {
            e.printStackTrace();
            cursor.close();
        }
        // No song found,
        return null;
    }

    public ArrayList<String> getFolders(SQLiteDatabase db) {
        ArrayList<String> folders = new ArrayList<>();
        String q = "SELECT DISTINCT " + SQLite.COLUMN_FOLDER + " FROM " + SQLite.TABLE_NAME + " ORDER BY " +
                SQLite.COLUMN_FOLDER + " ASC";

        // IV - Pre Lollipop use (where?) causes folder names starting 'MAIN/' - pragmatic clean up here
        Cursor cursor = db.rawQuery(q, null);
        cursor.moveToFirst();

        if (cursor.getColumnCount() > 0 && cursor.getColumnIndex(SQLite.COLUMN_FOLDER) == 0) {
            for (int x = 0; x < cursor.getCount(); x++) {
                cursor.moveToPosition(x);
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER))
                        .replace("MAIN/", c.getString(R.string.mainfoldername));
                folder = folder.replace(c.getString(R.string.mainfoldername) + "/", c.getString(R.string.mainfoldername));
                folders.add(folder);
            }
        }
        closeCursor(cursor);
        if (folders.isEmpty()) {
            folders.add(c.getString(R.string.mainfoldername));
        }

        // If we have custom folders (variations, etc.) listed, remove them
        if (folders.contains("../Variations/_cache") ||
                folders.contains("../" + c.getString(R.string.variation) + "/_cache") ||
                folders.contains("**Variations/_cache") ||
                folders.contains("**" + c.getString(R.string.variation) + "/_cache")) {
            folders.remove("../Variations/_cache");
            folders.remove("../" + c.getString(R.string.variation) + "/_cache");
            folders.remove("**Variations/_cache");
            folders.remove("**" + c.getString(R.string.variation) + "/_cache");
        }

        if (folders.contains("../Variations") ||
                folders.contains("../" + c.getString(R.string.variation)) ||
                folders.contains("**Variations") ||
                folders.contains("**" + c.getString(R.string.variation))) {
            folders.remove("../Variations");
            folders.remove("../" + c.getString(R.string.variation));
            folders.remove("**Variations");
            folders.remove("**" + c.getString(R.string.variation));
        }

        // We should also add in any folders that are empty - the database has no record of them
        ArrayList<String> songIds = mainActivityInterface.getStorageAccess().getSongFolders(mainActivityInterface.getStorageAccess().getSongIDsFromFile(), true, null);
        for (String songId : songIds) {
            if (!folders.contains(songId)) {
                folders.add(songId);
            }
        }

        Comparator<String> comparator = (o1, o2) -> {
            Collator collator = Collator.getInstance(mainActivityInterface.getLocale());
            collator.setStrength(Collator.SECONDARY);
            return collator.compare(o1, o2);
        };
        Collections.sort(folders, comparator);


        return folders;
    }

    public boolean renameSong(SQLiteDatabase db, String oldFolder, String newFolder,
                              String oldName, String newName) {
        String oldId = getAnySongId(oldFolder, oldName);
        String newId = getAnySongId(newFolder, newName);

        // First change the folder/file againts the matching old songid
        String[] whereClause = new String[]{oldId};
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLite.COLUMN_FOLDER, newFolder);
        contentValues.put(SQLite.COLUMN_FILENAME, newName);
        contentValues.put(SQLite.COLUMN_SONGID, newId);

        int val = db.update(SQLite.TABLE_NAME, contentValues, SQLite.COLUMN_SONGID + "=?", whereClause);

        return val > 0;
    }

    public void closeCursor(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (OutOfMemoryError | Exception e) {
                Log.e(TAG, "error:" + e);
            }
        }
    }

    public ArrayList<String> getUniqueThemeTags(SQLiteDatabase db) {
        ArrayList<String> themeTags = new ArrayList<>();

        String q = "SELECT DISTINCT " + SQLite.COLUMN_THEME + " FROM " + SQLite.TABLE_NAME + " ORDER BY " +
                SQLite.COLUMN_THEME + " ASC";

        Cursor cursor = db.rawQuery(q, null);
        cursor.moveToFirst();

        if (cursor.getColumnCount() > 0 && cursor.getColumnIndex(SQLite.COLUMN_THEME) == 0) {
            for (int x = 0; x < cursor.getCount(); x++) {
                cursor.moveToPosition(x);
                String themes = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_THEME));
                if (themes != null && themes.contains(";")) {
                    String[] themeBits = themes.split(";");
                    for (String bit : themeBits) {
                        if (!themeTags.contains(bit.trim()) && !bit.trim().isEmpty()) {
                            themeTags.add(bit.trim());
                        }
                    }
                } else if (themes != null && !themeTags.contains(themes.trim()) && !themes.trim().isEmpty()) {
                    themeTags.add(themes.trim());
                }
            }
        }
        closeCursor(cursor);
        Comparator<String> comparator = (o1, o2) -> {
            Collator collator = Collator.getInstance(mainActivityInterface.getLocale());
            collator.setStrength(Collator.SECONDARY);
            return collator.compare(o1, o2);
        };
        Collections.sort(themeTags, comparator);
        return themeTags;
    }

    public ArrayList<String> renameThemeTags(SQLiteDatabase db, SQLiteDatabase db2, String oldTag, String newTag) {
        String q = "SELECT " + SQLite.COLUMN_SONGID + ", " + SQLite.COLUMN_FOLDER + ", " +
                SQLite.COLUMN_FILENAME + ", " + SQLite.COLUMN_FILETYPE + ", " + SQLite.COLUMN_THEME +
                " FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_THEME + " LIKE ?";
        String[] arg = new String[]{"%" + oldTag + "%"};

        Cursor cursor = db.rawQuery(q, arg);

        if (cursor != null && cursor.getColumnCount() > 0) {
            cursor.moveToFirst();
            for (int x = 0; x < cursor.getCount(); x++) {
                cursor.moveToPosition(x);
                String songid = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_SONGID));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER));
                String filename = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILENAME));
                String filetype = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILETYPE));
                String themes = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_THEME));
                StringBuilder stringBuilder = new StringBuilder();
                if (themes != null && themes.contains(";")) {
                    String[] themeBits = themes.split(";");
                    for (String bit : themeBits) {
                        if (!bit.trim().equals(oldTag.trim()) && !bit.trim().isEmpty()) {
                            stringBuilder.append(bit).append(";");
                        }
                    }
                    // Add the new tag
                    stringBuilder.append(newTag);
                    themes = stringBuilder.toString();
                    themes = themes.replace(";;", ";");
                    themes = themes.replace("; ;", ";");
                } else if (themes != null && !themes.isEmpty() && themes.trim().equals(oldTag.trim())) {
                    themes = newTag;
                }
                // Put the fixed themes back into the database
                ContentValues contentValues = new ContentValues();
                contentValues.put(SQLite.COLUMN_THEME, themes);
                db.update(SQLite.TABLE_NAME, contentValues, SQLite.COLUMN_SONGID + "=?", new String[]{songid});

                // If this is a PDF or img, update the persistent database
                if (filetype != null && !filetype.isEmpty() &&
                        (filetype.equals("PDF") || filetype.equals("IMG"))) {
                    db2.update(SQLite.TABLE_NAME, contentValues, SQLite.COLUMN_SONGID + "=?", new String[]{songid});

                } else {
                    // Update the song file (don't do for PDF or IMG obviously
                    Song tempSong = getSpecificSong(db, folder, filename);
                    tempSong.setTheme(themes);
                    mainActivityInterface.getSaveSong().updateSong(tempSong, false);
                }
            }
        }
        closeCursor(cursor);

        // Now get the new unique tags
        return getUniqueThemeTags(db);
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
            text = text.substring(0, text.lastIndexOf(", "));
        }
        return text;
    }

    public String getFolderForSong(SQLiteDatabase db, String filename) {
        String folder = c.getString(R.string.mainfoldername);
        String selectQuery = "SELECT " + SQLite.COLUMN_FOLDER + " FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_FILENAME + " = ?;";
        String[] args = {filename};
        Cursor cursor = db.rawQuery(selectQuery, args);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            folder = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER));
        }
        cursor.close();
        return folder;
    }

    private String escape(String text) {
        // If the text contains ', escape it
        // First remove ''
        while (text.contains("''")) {
            text = text.replace("''", "'");
        }
        // Now escape by doubling
        text = text.replace("'", "''");
        return text;
    }

    public String[] getUuidFromFolderAndFile(SQLiteDatabase db, String folderAndFile) {
        String selectQuery = "SELECT " + SQLite.COLUMN_UUID + ", " + SQLite.COLUMN_TITLE + " FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + " = ?;";
        String[] args = {folderAndFile};
        String[] results = new String[2];
        results[0] = "";
        results[1] = "";
        Cursor cursor = db.rawQuery(selectQuery, args);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            results[0] = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_UUID));
            results[1] = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_TITLE));
        } else {
            results[0] = String.valueOf(UUID.randomUUID());
            results[1] = "";
        }
        cursor.close();
        return results;
    }

    public ArrayList<OpenChordsTag> getThemesFromFilesInFolder(SQLiteDatabase db, String folder) {
        String selectQuery = "SELECT " + SQLite.COLUMN_THEME + " FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_FOLDER + " = ?;";
        String[] args = {folder};
        Cursor cursor = db.rawQuery(selectQuery, args);
        ArrayList<OpenChordsTag> tags = new ArrayList<>();
        ArrayList<String> uniqueThemes = new ArrayList<>();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    String[] themes = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_THEME)).split("\n");
                    for (String theme : themes) {
                        if (!uniqueThemes.contains(theme)) {
                            uniqueThemes.add(theme);
                            OpenChordsTag openChordsTag = new OpenChordsTag();
                            openChordsTag.setTitle(theme);
                            tags.add(openChordsTag);
                        }
                    }
                } while (cursor.moveToNext());
            }
        }
        // close cursor connection
        closeCursor(cursor);

        return tags;
    }

    // This can be used to export the persistent database to a 'readable' csv table that can be imported into a spreadsheet
    public void exportDatabase(SQLiteDatabase db, String exportedFilename) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            StringBuilder stringBuilder = new StringBuilder();

            // Add the table headings - CODE MUST BE UPDATED IF COLUMNS CHANGE - USE SQLite file
            // Don't worry about ID or SONG_ID as they are created automatically based on entry / filenames / folders
            addCSVTableHeadings(stringBuilder);

            // Now we will query the database and add each item in turn
            Cursor cursor = db.rawQuery(getAllQuery(), null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    addCSVTableValue(stringBuilder, null, cursor);
                }
                while (cursor.moveToNext());
            }

            // close cursor connection
            closeCursor(cursor);

            // Now write the file
            if (mainActivityInterface.getStorageAccess().writeFileFromString("Export", "",
                    exportedFilename, stringBuilder.toString().replace("\"null\"", "\"\""))) {
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", exportedFilename);
                c.startActivity(Intent.createChooser(mainActivityInterface.getExportActions().setShareIntent(exportedFilename, "text/csv", uri, null), exportedFilename));
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.success));
            } else {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.error));
            }
        });
    }

    public void addCSVTableHeadings(StringBuilder stringBuilder) {
        // Add the table headings - CODE MUST BE UPDATED IF COLUMNS CHANGE - USE SQLite file
        // Don't worry about ID or SONG_ID as they are created automatically based on entry / filenames / folders
        stringBuilder.append("\"").append(SQLite.COLUMN_UUID).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_FILENAME).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_FOLDER).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_TITLE).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_AUTHOR).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_COPYRIGHT).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_LYRICS).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_HYMNNUM).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_CCLI).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_THEME).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_ALTTHEME).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_USER1).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_USER2).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_USER3).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_BEATBUDDY_SONG).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_BEATBUDDY_KIT).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_DRUMMER).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_DRUMMER_KIT).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_KEY).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_KEY_ORIGINAL).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_PREFERRED_INSTRUMENT).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_TIMESIG).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_AKA).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_AUTOSCROLL_DELAY).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_AUTOSCROLL_LENGTH).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_TEMPO).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_PAD_FILE).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_PAD_LOOP).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_MIDI).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_MIDI_INDEX).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_CAPO).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_CUSTOM_CHORDS).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_NOTES).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_ABC).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_ABC_TRANSPOSE).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_LINK_YOUTUBE).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_LINK_WEB).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_LINK_AUDIO).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_LINK_OTHER).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_PRESENTATIONORDER).append("\",");
        stringBuilder.append("\"").append(SQLite.COLUMN_FILETYPE).append("\"\n");
    }

    private String escaped(String string) {
        string = string == null ? "" : string;
        string = string.replace("\"", "\"\"");
        return string;
    }

    public void addCSVTableValue(StringBuilder stringBuilder, Song song, Cursor cursor) {
        // This can be called from a database cursor, or a song item that has already been retrieved
        //mainActivityInterface.getDrumViewModel().prepareSongValues(song);
        stringBuilder.append("\"").append(escaped(song != null ? song.getUuid() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_UUID)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getFilename() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILENAME)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getFolder() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getTitle() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_TITLE)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getAuthor() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_AUTHOR)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getCopyright() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_COPYRIGHT)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getLyrics() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LYRICS)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getHymnnum() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_HYMNNUM)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getCcli() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_CCLI)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getTheme() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_THEME)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getAlttheme() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_ALTTHEME)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getUser1() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_USER1)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getUser2() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_USER2)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getUser3() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_USER3)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getBeatbuddysong() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_BEATBUDDY_SONG)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getBeatbuddykit() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_BEATBUDDY_KIT)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getDrummer() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_DRUMMER)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getDrummerKit() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_DRUMMER_KIT)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getKey() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_KEY)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getKeyOriginal() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_KEY_ORIGINAL)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getPreferredInstrument() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_PREFERRED_INSTRUMENT)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? DrumCalculations.getFixedTimeSignatureString(song.getTimesig(), false) : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_TIMESIG)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getAka() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_AKA)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getAutoscrolldelay() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_AUTOSCROLL_DELAY)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getAutoscrolllength() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_AUTOSCROLL_LENGTH)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? DrumCalculations.getFixedTempoString(song.getTempo(), false) : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_TEMPO)))).append("\",");
        //stringBuilder.append("\"").append(escaped(song!=null ? song.getTempo() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_TEMPO)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getPadfile() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_PAD_FILE)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getPadloop() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_PAD_LOOP)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getMidi() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_MIDI)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getMidiindex() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_MIDI_INDEX)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getCapo() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_CAPO)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getCustomchords() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_CUSTOM_CHORDS)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getNotes() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_NOTES)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getAbc() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_ABC)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getAbcTranspose() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_ABC_TRANSPOSE)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getLinkyoutube() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LINK_YOUTUBE)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getLinkweb() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LINK_WEB)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getLinkaudio() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LINK_AUDIO)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getLinkother() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LINK_OTHER)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getPresentationorder() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_PRESENTATIONORDER)))).append("\",");
        stringBuilder.append("\"").append(escaped(song != null ? song.getFiletype() : cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILETYPE)))).append("\"\n");

    }

    public ArrayList<ShareableObject> getShareableSongs(SQLiteDatabase db) {
        ArrayList<ShareableObject> shareableSongs = new ArrayList<>();
        String selectQuery = "SELECT " + SQLite.COLUMN_FILENAME + ", " +
                SQLite.COLUMN_FOLDER + ", " +
                SQLite.COLUMN_TITLE + ", " +
                SQLite.COLUMN_LAST_MODIFIED + ", " +
                SQLite.COLUMN_UUID +
                " FROM " + SQLite.TABLE_NAME + ";";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ShareableObject shareableObject = new ShareableObject();
                shareableObject.setFilename(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILENAME)));
                shareableObject.setFolder(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER)));
                shareableObject.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_TITLE)));
                shareableObject.setLastModified(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LAST_MODIFIED)));
                shareableObject.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_UUID)));
                shareableSongs.add(shareableObject);
            }
            while (cursor.moveToNext());
        }

        // close cursor connection
        closeCursor(cursor);
        return shareableSongs;
    }

    public String[] getSongCreationInfo(SQLiteDatabase db, String folder, String filename) {
        String songId = getAnySongId(folder, filename);
        String[] selectionArgs = new String[]{songId};
        String sql = "SELECT " + SQLite.COLUMN_UUID + ", " + SQLite.COLUMN_LAST_MODIFIED + " FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "= ? ";
        String[] returnInfo = new String[]{"", "", "false"};

        Cursor cursor = db.rawQuery(sql, selectionArgs);

        // Get the first item (the matching songID)
        try {
            if (cursor.moveToFirst()) {
                returnInfo[0] = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_UUID));
                returnInfo[1] = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LAST_MODIFIED));
                returnInfo[2] = "true";  // We have this file
                returnInfo[0] = returnInfo[0] == null ? "" : returnInfo[0];
                returnInfo[1] = returnInfo[1] == null ? "" : returnInfo[1];
            }

            closeCursor(cursor);
        } catch (Exception e) {
            Log.e(TAG, "error:" + e);
        }

        return returnInfo;
    }

    public ArrayList<SongId> getSongIds(SQLiteDatabase db) {
        // Create an array of simple song details - used for the web server
        ArrayList<SongId> songIds = new ArrayList<>();
        String selectQuery = "SELECT " + SQLite.COLUMN_FILENAME + ", " +
                SQLite.COLUMN_FOLDER + ", " +
                SQLite.COLUMN_TITLE + ", " +
                // Wrap the reserved word 'key' in backticks
                "`" + SQLite.COLUMN_KEY + "`, " +
                SQLite.COLUMN_AUTHOR +
                " FROM " + SQLite.TABLE_NAME + ";";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                SongId songId = new SongId();
                songId.setFolder(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FOLDER)));
                songId.setFilename(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_FILENAME)));
                songId.setKey(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_KEY)));
                songId.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_AUTHOR)));
                songIds.add(songId);
            }
            while (cursor.moveToNext());
        }

        // close cursor connection
        closeCursor(cursor);
        return songIds;
    }

}
