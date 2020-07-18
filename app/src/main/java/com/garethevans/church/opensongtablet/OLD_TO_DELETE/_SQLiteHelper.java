/*
package com.garethevans.church.opensongtablet.OLD_TO_DELETE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet._Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class _SQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    public _SQLiteHelper(Context context) {
        super(context,  _SQLite.DATABASE_NAME, null, DATABASE_VERSION);
        // Don't create the database here as we don't want to recreate on each call.
    }

    public SQLiteDatabase getDB(Context c) {
        try {
            File f = new File(c.getExternalFilesDir("Database"), _SQLite.DATABASE_NAME);
            return SQLiteDatabase.openOrCreateDatabase(f, null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // If the table doesn't exist, create it.
        db.execSQL(_SQLite.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + _SQLite.TABLE_NAME);

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

    void createImportedSong(Context c, String folder, String filename, String title, String author,
                            String copyright, String key, String time_sig, String ccli, String lyrics) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db = getDB(c)) {
            filename = escapedSQL(filename);
            folder = escapedSQL(folder);
            String songid = escapedSQL(folder) + "/" + escapedSQL(filename);
            ContentValues values = new ContentValues();
            values.put(_SQLite.COLUMN_SONGID, escapedSQL(songid));
            values.put(_SQLite.COLUMN_FOLDER, escapedSQL(folder));
            values.put(_SQLite.COLUMN_FILENAME, escapedSQL(filename));
            values.put(_SQLite.COLUMN_TITLE, escapedSQL(title));
            values.put(_SQLite.COLUMN_AUTHOR, escapedSQL(author));
            values.put(_SQLite.COLUMN_COPYRIGHT, escapedSQL(copyright));
            values.put(_SQLite.COLUMN_KEY, escapedSQL(key));
            values.put(_SQLite.COLUMN_TIMESIG, escapedSQL(time_sig));
            values.put(_SQLite.COLUMN_CCLI, escapedSQL(ccli));
            values.put(_SQLite.COLUMN_LYRICS, escapedSQL(lyrics));

            // Insert the new row, returning the primary key value of the new row
            db.insert(_SQLite.TABLE_NAME, null, values);

        }
    }

    public void createSong(Context c, String folder, String filename) {
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
                values.put(_SQLite.COLUMN_SONGID, escapedSQL(songid));
                values.put(_SQLite.COLUMN_FOLDER, escapedSQL(folder));
                values.put(_SQLite.COLUMN_FILENAME, escapedSQL(filename));

                // Insert the new row, returning the primary key value of the new row
                try {
                    db.insert(_SQLite.TABLE_NAME, null, values);
                } catch (Exception e) {
                    Log.d("SQLiteHelper",songid + " already exists in the table, not able to create.");
                }
            }
        }
    }

    public boolean songIdExists(SQLiteDatabase db, String songid) {
        String Query = "SELECT * FROM " + _SQLite.TABLE_NAME + " WHERE " + _SQLite.COLUMN_SONGID + " = \"" + escapedSQL(songid) + "\"";
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private String shortenLyrics(String lyrics) {
        // This strips out chord lines from the lyrics ready for the sql database
        String[] lines = lyrics.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line:lines) {
            if (!line.startsWith(".") && !line.startsWith("[")) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    _SQLite setSong(_SQLite sqLite) {
        sqLite.setTitle(StaticVariables.mTitle);
        sqLite.setAuthor(StaticVariables.mAuthor);
        sqLite.setCopyright(StaticVariables.mCopyright);
        sqLite.setLyrics(shortenLyrics(StaticVariables.mLyrics));
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

    public void updateSong(Context c, _SQLite sqLite) {

        try (SQLiteDatabase db = getDB(c)) {
            ContentValues values = new ContentValues();

            values.put(_SQLite.COLUMN_ID, sqLite.getId());
            values.put(_SQLite.COLUMN_SONGID, escapedSQL(sqLite.getSongid()));
            values.put(_SQLite.COLUMN_FILENAME, escapedSQL(sqLite.getFilename()));
            values.put(_SQLite.COLUMN_FOLDER, escapedSQL(sqLite.getFolder()));
            values.put(_SQLite.COLUMN_TITLE, escapedSQL(sqLite.getTitle()));
            values.put(_SQLite.COLUMN_AUTHOR, escapedSQL(sqLite.getAuthor()));
            values.put(_SQLite.COLUMN_COPYRIGHT, escapedSQL(sqLite.getCopyright()));
            values.put(_SQLite.COLUMN_LYRICS, escapedSQL(sqLite.getLyrics()));
            values.put(_SQLite.COLUMN_HYMNNUM, escapedSQL(sqLite.getHymn_num()));
            values.put(_SQLite.COLUMN_CCLI, escapedSQL(sqLite.getCcli()));
            values.put(_SQLite.COLUMN_THEME, escapedSQL(sqLite.getTheme()));
            values.put(_SQLite.COLUMN_ALTTHEME, escapedSQL(sqLite.getAlttheme()));
            values.put(_SQLite.COLUMN_USER1, escapedSQL(sqLite.getUser1()));
            values.put(_SQLite.COLUMN_USER2, escapedSQL(sqLite.getUser2()));
            values.put(_SQLite.COLUMN_USER3, escapedSQL(sqLite.getUser3()));
            values.put(_SQLite.COLUMN_KEY, escapedSQL(sqLite.getKey()));
            values.put(_SQLite.COLUMN_TIMESIG, escapedSQL(sqLite.getTimesig()));
            values.put(_SQLite.COLUMN_AKA, escapedSQL(sqLite.getAka()));

            long l = db.update(_SQLite.TABLE_NAME, values, _SQLite.COLUMN_ID + "=?", new String[]{String.valueOf(sqLite.getId())});
            Log.d("updateSong", "l=" + l);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public _SQLite getSong(Context c, String songid) {
        SQLiteDatabase db = getDB(c);
        try {
            Cursor cursor = db.query(_SQLite.TABLE_NAME,
                    new String[]{_SQLite.COLUMN_ID, _SQLite.COLUMN_SONGID, _SQLite.COLUMN_FILENAME, _SQLite.COLUMN_FOLDER,
                            _SQLite.COLUMN_TITLE, _SQLite.COLUMN_AUTHOR, _SQLite.COLUMN_COPYRIGHT, _SQLite.COLUMN_LYRICS,
                            _SQLite.COLUMN_HYMNNUM, _SQLite.COLUMN_CCLI, _SQLite.COLUMN_THEME,
                            _SQLite.COLUMN_ALTTHEME, _SQLite.COLUMN_USER1, _SQLite.COLUMN_USER2,
                            _SQLite.COLUMN_USER3, _SQLite.COLUMN_KEY, _SQLite.COLUMN_TIMESIG, _SQLite.COLUMN_AKA},
                    _SQLite.COLUMN_SONGID + "=?",
                    new String[]{String.valueOf((songid))}, null, null, _SQLite.COLUMN_FILENAME, null);

            if (cursor != null) {
                cursor.moveToFirst();

                try {
                    // prepare note object
                    _SQLite sqLite = new _SQLite(
                            cursor.getInt(cursor.getColumnIndex(_SQLite.COLUMN_ID)),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_SONGID))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FILENAME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FOLDER))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_TITLE))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AUTHOR))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_COPYRIGHT))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_LYRICS))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_HYMNNUM))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_CCLI))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_THEME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_ALTTHEME))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_USER1))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_USER2))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_USER3))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_KEY))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_TIMESIG))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AKA))),
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_INSET))));

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

    void updateFolderName(Context c, String oldFolder, String newFolder) {
        // Select matching folder Query
        String selectQuery = "SELECT "+ _SQLite.COLUMN_SONGID + ", " +
                _SQLite.COLUMN_FOLDER + ", " +
                _SQLite.COLUMN_FILENAME + " " +
                "FROM " + _SQLite.TABLE_NAME +
                " WHERE " + _SQLite.COLUMN_SONGID + " LIKE '%" + escapedSQL(oldFolder) + "/%'" +
                " ORDER BY " + _SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String currSongId = cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_SONGID));
                    String updatedId = currSongId.replace(oldFolder + "/", newFolder + "/");
                    ContentValues values = new ContentValues();
                    values.put(_SQLite.COLUMN_SONGID, escapedSQL(updatedId));
                    values.put(_SQLite.COLUMN_FOLDER, escapedSQL(newFolder));

                    db.update(_SQLite.TABLE_NAME, values, _SQLite.COLUMN_SONGID + "=?", new String[]{escapedSQL(currSongId)});

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

    public void deleteSong(Context c, String songId) {
        try (SQLiteDatabase db = getDB(c)) {
            db.delete(_SQLite.TABLE_NAME, _SQLite.COLUMN_SONGID + " = ?",
                    new String[]{String.valueOf(escapedSQL(songId))});
        }
    }

    public ArrayList<String> getThemes(Context c, _Preferences preferences, StorageAccess storageAccess) {
        ArrayList<String> themes = new ArrayList<>();

        // Select matching folder Query
        String selectQuery = "SELECT "+ _SQLite.COLUMN_FILENAME + ", " +
                _SQLite.COLUMN_THEME + " " +
                "FROM " + _SQLite.TABLE_NAME +
                " ORDER BY " + _SQLite.COLUMN_THEME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    _SQLite sqLite = new _SQLite();
                    sqLite.setTheme(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_THEME))));
                    String t = sqLite.getTheme();
                    if (t==null) {
                        t="";
                    }
                    // Song may have multiple themes, so split them by new lines (preferred), semicolons, commas
                    t = t.replace("\n\n","__SPLIT__");
                    t = t.replace("\n","__SPLIT__");
                    t = t.replace(";","__SPLIT__");
                    t = t.replace(",","__SPLIT__");
                    String[] split = t.split("__SPLIT__");
                    for (String t_split:split) {
                        if (!t_split.trim().equals("") && !themes.contains("$__" + t_split.trim() + "__$")) {
                            // This avoids adding references to themes more than once
                            // Enclosing in $__ __$ so that 'Church' isn't seen as 'not Church'
                            themes.add("$__" + t_split.trim() + "__$");
                            Log.d("SQLiteHelper","Song:"+sqLite.getFilename()+" Theme:"+sqLite.getTheme());
                        }
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

        // Now add in any themes saved in the settings directory
        Uri uri = storageAccess.getUriForItem(c,preferences,"Settings","","userthemes.txt");
        InputStream is = storageAccess.getInputStream(c,uri);
        String savedthemes = storageAccess.readTextFileToString(is);
        if (savedthemes==null) {
            savedthemes = "";
        }
        String[] st = savedthemes.split("\n");
        for (String savedtheme : st) {
            if (!themes.contains("$__" + savedtheme + "__$")) {
                // This avoids adding references to themes more than once
                themes.add("$__" + savedtheme + "__$");
            }
        }

        // Sort the array
        Collections.sort(themes, String.CASE_INSENSITIVE_ORDER);

        // If we have new ones, update the saved file
        String fs = arrayListToString(themes);
        fs = fs.replace("$__","");
        fs = fs.replace("__$","");
        if (!fs.equals(savedthemes)) {
            OutputStream os = storageAccess.getOutputStream(c,uri);
            storageAccess.writeFileFromString(fs,os);
        }

        // return songs in this folder without the $__ __$
        for (int i=0; i<themes.size();i++) {
            themes.set(i,themes.get(i).replace("$__","").replace("__$",""));
        }
        return themes;
    }

    private String arrayListToString(ArrayList<String> arrayList) {
        if (arrayList!=null && arrayList.size()>0) {
            StringBuilder sb = new StringBuilder();
            for (String line : arrayList) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    void updateThemes(Context c, _Preferences preferences, StorageAccess storageAccess, ArrayList<String> themes) {
        String fs = arrayListToString(themes);
        fs = fs.replace("$__","");
        fs = fs.replace("__$","");
        Uri uri = storageAccess.getUriForItem(c, preferences,"Settings", "", "userthemes.txt");
        OutputStream os = storageAccess.getOutputStream(c,uri);
        storageAccess.writeFileFromString(fs,os);
    }

    int countWithTheme(Context c, String theme) {
        String selectQuery = "SELECT "+ _SQLite.COLUMN_FILENAME+", " +
                "FROM " + _SQLite.TABLE_NAME +
                " WHERE " + _SQLite.COLUMN_THEME + " CONTAINS ['" + escapedSQL(theme) + "']";

        int i;
        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor==null) {
                i = 0;
            } else {
                i = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return i;
    }

    private String getBasicSQLQueryStart() {
        return "SELECT " + _SQLite.COLUMN_FILENAME + ", " + _SQLite.COLUMN_AUTHOR + ", " +
                _SQLite.COLUMN_KEY + ", " + _SQLite.COLUMN_FOLDER + ", " + _SQLite.COLUMN_THEME + ", " +
                _SQLite.COLUMN_ALTTHEME + ", " + _SQLite.COLUMN_USER1 + ", " + _SQLite.COLUMN_USER2 + ", " +
                _SQLite.COLUMN_USER3 + ", " + _SQLite.COLUMN_LYRICS + " FROM " + _SQLite.TABLE_NAME + " ";
    }
    private String getOrderBySQL() {
        return "ORDER BY " + _SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";
    }

    public ArrayList<_SQLite> getSongsByArtist(Context c, String whichArtist, String filter) {
        ArrayList<_SQLite> songs = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();
        StaticVariables.songsInList = new ArrayList<>();
        StaticVariables.songsInList.clear();
        String where;
        if (whichArtist==null || whichArtist.isEmpty()) {
            where = "";
        } else {
            where = _SQLite.COLUMN_AUTHOR + " LIKE '%" + escapedSQL(whichArtist) + "%'";
        }

        // Now for the filter AND matches
        where = getFilterSearch(where, filter);

        if (!where.isEmpty()) {
            where = "WHERE " + where + " AND ";
        } else {
            where = "WHERE ";
        }

        where += _SQLite.COLUMN_FILENAME + " !=''";

        String selectQuery = getBasicSQLQueryStart() + where + " " + getOrderBySQL();

        Log.d("getSongByArtists","selectQuery="+selectQuery);

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String fi = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FILENAME)));
                    String fo = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FOLDER)));
                    String au = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AUTHOR)));
                    String ke = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_KEY)));

                    _SQLite sqLite = new _SQLite();
                    sqLite.setFilename(fi);
                    sqLite.setFolder(fo);
                    sqLite.setAuthor(au);
                    sqLite.setKey(ke);

                    songs.add(sqLite);

                    String setString = getSetString(c,fo,fi);
                    sqLite.setInSet(isItInSet(setString,StaticVariables.currentSet));

                    StaticVariables.songsInList.add(setString);
                    if (!fi.equals("") && !files.contains("$__" + fo + "/" + fi + "__$")) {
                        // This avoids adding references to folders more than once
                        files.add("$__" + fo + "/" + fi + "__$");
                    }
                }
                while (cursor.moveToNext());
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

    public ArrayList<_SQLite> getSongsInFolder(Context c, String whichSongFolder, String filter) {
        ArrayList<_SQLite> songs = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();
        StaticVariables.songsInList = new ArrayList<>();
        StaticVariables.songsInList.clear();

        String where = _SQLite.COLUMN_FOLDER + "='" + escapedSQL(whichSongFolder) + "'";

        // Now for the filter AND matches
        where = getFilterSearch(where, filter);
        where = "WHERE " + where + " AND ";

        where += _SQLite.COLUMN_FILENAME + "!=''";

        // Select matching folder Query
        String selectQuery = getBasicSQLQueryStart() + where + getOrderBySQL();

        Log.d("getSongByFolder","selectQuery="+selectQuery);

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String fi = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FILENAME)));
                    String fo = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FOLDER)));
                    String au = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AUTHOR)));
                    String ke = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_KEY)));

                    _SQLite sqLite = new _SQLite();
                    sqLite.setFilename(fi);
                    sqLite.setFolder(fo);
                    sqLite.setAuthor(au);
                    sqLite.setKey(ke);

                    songs.add(sqLite);

                    // Is this in the set?  This will add a tick for the songlist checkbox
                    String setString = getSetString(c,fo,fi);
                    StaticVariables.songsInList.add(setString);
                    if (StaticVariables.currentSet==null) {
                        StaticVariables.currentSet="";
                    }
                    sqLite.setInSet(isItInSet(setString,StaticVariables.currentSet));

                    // Add it to the files list (for swiping).  This ignores filtered songs
                    if (!fi.equals("") && !files.contains("$__" + fo + "/" + fi + "__$")) {
                        // This avoids adding references to folders more than once
                        files.add("$__" + fo + "/" + fi + "__$");
                    }
                }
                while (cursor.moveToNext());
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

    private String getSetString(Context c, String folder, String filename) {
        if (folder == null || folder.equals(c.getString(R.string.mainfoldername))) {
            return "$**_" + filename + "_**$";
        } else {
            return "$**_" + folder + "/" + filename + "_**$";
        }
    }

    private String isItInSet(String setString, String currentSet) {
        if (currentSet.contains(setString)) {
            return "true";
        } else {
            return "false";
        }
    }

    public ArrayList<_SQLite> getSongsByCustom(Context c, boolean searchFolder, boolean searchAuthor, boolean searchKey,
                                               boolean searchTheme, boolean searchOther, String folderSearch,
                                               String authorSearch, String keySearch, String themeSearch,
                                               String otherSearch, String filter) {

        // This is used for either a custom search or updating the current found list by filter text
        ArrayList<_SQLite> songs = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();
        StaticVariables.songsInList = new ArrayList<>();
        StaticVariables.songsInList.clear();

        // Get the search phrase
        // These will be split by "S:title S:folder S:lyrics S:author S:key S:theme S:user1 S:user2 S:user3 PHRASE:XXX"

        String searchMatches = "";
        if (searchFolder && folderSearch!=null && !folderSearch.equals("")) {
            searchMatches += _SQLite.COLUMN_FOLDER + "='" + escapedSQL(folderSearch) + "' AND ";
        }
        if (searchAuthor && authorSearch!=null && !authorSearch.equals("")) {
            searchMatches += _SQLite.COLUMN_AUTHOR + "='" + escapedSQL(authorSearch) + "' AND ";
        }
        if (searchKey && keySearch!=null && !keySearch.equals("")) {
            searchMatches += _SQLite.COLUMN_KEY+ "='" + escapedSQL(keySearch) + "' AND ";
        }
        if (searchTheme && themeSearch!=null && !themeSearch.equals("")) {
            searchMatches += "(" + _SQLite.COLUMN_THEME+ " LIKE '%" + escapedSQL(themeSearch) + "%' OR ";
            searchMatches += _SQLite.COLUMN_ALTTHEME+ " LIKE '%" + escapedSQL(themeSearch) + "%')";
            searchMatches = searchMatches.replace("((","("); // If we are only searching themes
            searchMatches = searchMatches.replace("))",")"); // Remove the double (( )) that is created
        }
        // Group and fix the AND searches
        if (!searchMatches.isEmpty()) {
            if (searchMatches.endsWith(" AND ")) {
                searchMatches = searchMatches.substring(0,searchMatches.lastIndexOf(" AND "));
            }
            searchMatches = "(" + searchMatches + ")";
        }

        // Now for the OR matches
        if (searchOther && otherSearch!=null && !otherSearch.equals("")) {
            if (!searchMatches.isEmpty()) {
                searchMatches += " AND " + "(";
            } else {
                searchMatches += "(";
            }
            searchMatches += _SQLite.COLUMN_LYRICS + " LIKE '%" + escapedSQL(otherSearch) + "%' OR ";
            searchMatches += _SQLite.COLUMN_USER1 + " LIKE '%" + escapedSQL(otherSearch) + "%' OR ";
            searchMatches += _SQLite.COLUMN_USER2 + " LIKE '%" + escapedSQL(otherSearch) + "%' OR ";
            searchMatches += _SQLite.COLUMN_USER3 + " LIKE '%" + escapedSQL(otherSearch) + "%' OR ";
            searchMatches += _SQLite.COLUMN_HYMNNUM + " LIKE '%" + escapedSQL(otherSearch) + "%'";
            searchMatches += ")";
        }

        // Now for the filter AND matches
        searchMatches = getFilterSearch(searchMatches, filter);

        if (!searchMatches.isEmpty()) {
            searchMatches = "WHERE " + searchMatches + " AND ";
        } else {
            searchMatches = "WHERE ";
        }

        searchMatches += _SQLite.COLUMN_FILENAME + " !=''";

        // Select matching folder Query
        String selectQuery = getBasicSQLQueryStart() + searchMatches + " " + getOrderBySQL();

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String fi = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FILENAME)));
                    String fo = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FOLDER)));
                    String au = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AUTHOR)));
                    String ke = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_KEY)));

                    _SQLite sqLite = new _SQLite();
                    sqLite.setFilename(fi);
                    sqLite.setFolder(fo);
                    sqLite.setAuthor(au);
                    sqLite.setKey(ke);

                    songs.add(sqLite);

                    // Is this in the set?  This will add a tick for the songlist checkbox
                    String setString = getSetString(c,fo,fi);
                    StaticVariables.songsInList.add(setString);
                    sqLite.setInSet(isItInSet(setString,StaticVariables.currentSet));

                    // Add it to the files list (for swiping).  This ignores filtered songs
                    if (!fi.equals("") && !files.contains("$__" + fo + "/" + fi + "__$")) {
                        // This avoids adding references to folders more than once
                        files.add("$__" + fo + "/" + fi + "__$");
                    }

                }
                while (cursor.moveToNext());
            }

            // close db connection
            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Return the songs
        return songs;
    }

    public ArrayList<_SQLite> getSongsByFilters(Context c, boolean searchByFolder, boolean searchByArtist,
                                                boolean searchByKey, boolean searchByTag,
                                                boolean searchByFilter, String folderVal, String artistVal,
                                                String keyVal, String tagVal, String filterVal) {
        ArrayList<_SQLite> songs = new ArrayList<>();
        StaticVariables.songsInList = new ArrayList<>();
        StaticVariables.songsInList.clear();

        String sqlMatch = "";
        if (searchByFolder && folderVal!=null && folderVal.length()>0) {
            sqlMatch += _SQLite.COLUMN_FOLDER + "=\"" + folderVal + "\" AND ";
        }
        if (searchByArtist && artistVal!=null && artistVal.length()>0) {
            sqlMatch += _SQLite.COLUMN_AUTHOR + " LIKE \"%" + artistVal + "%\" AND ";
        }
        if (searchByKey && keyVal!=null && keyVal.length()>0) {
            sqlMatch += _SQLite.COLUMN_KEY + "=\"" + keyVal + "\" AND ";
        }
        if (searchByTag && tagVal!=null && tagVal.length()>0) {
            sqlMatch += _SQLite.COLUMN_THEME + "=\"" + tagVal + "\" AND ";
        }
        if (searchByFilter && filterVal!=null && filterVal.length()>0) {
            sqlMatch += "(" + _SQLite.COLUMN_LYRICS + " LIKE \"%" + filterVal + "%\" OR ";
            sqlMatch += _SQLite.COLUMN_FILENAME + " LIKE \"%" + filterVal + "%\" OR ";
            sqlMatch += _SQLite.COLUMN_TITLE + " LIKE \"%" + filterVal + "%\")";
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
        sqlMatch += _SQLite.COLUMN_FILENAME + " !=''";

        // Select matching folder Query
        String selectQuery = getBasicSQLQueryStart() + sqlMatch + " " + getOrderBySQL();

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String fi = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FILENAME)));
                    String fo = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FOLDER)));
                    String au = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AUTHOR)));
                    String ke = unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_KEY)));

                    _SQLite sqLite = new _SQLite();
                    sqLite.setFilename(fi);
                    sqLite.setFolder(fo);
                    sqLite.setAuthor(au);
                    sqLite.setKey(ke);

                    songs.add(sqLite);

                    // Is this in the set?  This will add a tick for the songlist checkbox
                    String setString = getSetString(c,fo,fi);
                    StaticVariables.songsInList.add(setString);

                    if (StaticVariables.currentSet==null) {
                        StaticVariables.currentSet="";
                    }
                    sqLite.setInSet(isItInSet(setString,StaticVariables.currentSet));

                }
                while (cursor.moveToNext());
            }

            // close db connection
            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Return the songs
        Log.d("SQLiteHelper","QUERY:  "+selectQuery);
        return songs;
    }

    public ArrayList<String> getFolders(Context c) {
        // Get the database
        ArrayList<String> folders = new ArrayList<>();
        try (SQLiteDatabase db = getDB(c)) {
            String q = "SELECT DISTINCT " + _SQLite.COLUMN_FOLDER + " FROM " + _SQLite.TABLE_NAME + " ORDER BY " +
                    _SQLite.COLUMN_FOLDER + " ASC";

            Cursor cursor = db.rawQuery(q, null);
            cursor.moveToFirst();
            do {
                String s = cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FOLDER));
                if (s!=null && !s.isEmpty()) {
                    folders.add(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FOLDER))));
                }
            } while (cursor.moveToNext());
            cursor.close();
            //db.close();
            return folders;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String getFilterSearch(String searchMatches, String filter) {
        if (filter!=null && !filter.isEmpty()) {
            if (!searchMatches.isEmpty()) {
                searchMatches += " AND (";
            } else {
                searchMatches += " (";
            }
            searchMatches += _SQLite.COLUMN_FILENAME + " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_TITLE + " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_FOLDER + " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_AUTHOR+ " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_KEY+ " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_THEME+ " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_ALTTHEME+ " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_HYMNNUM+ " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_LYRICS + " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_USER1 + " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_USER2 + " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_USER3 + " LIKE '%" + escapedSQL(filter) + "%' OR ";
            searchMatches += _SQLite.COLUMN_HYMNNUM+ " LIKE '%" + escapedSQL(filter) + "%')";
        }
        return searchMatches;
    }

    public ArrayList<String> getAuthors(Context c) {
        // Get the database
        ArrayList<String> authors = new ArrayList<>();
        authors.add("");
        try (SQLiteDatabase db = getDB(c)) {
            String q = "SELECT DISTINCT " + _SQLite.COLUMN_AUTHOR + " FROM " + _SQLite.TABLE_NAME + " ORDER BY " +
                    _SQLite.COLUMN_AUTHOR + " ASC";
            Cursor cursor = db.rawQuery(q, null);
            cursor.moveToFirst();
            do {
                try {
                    String s = cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AUTHOR));
                    if (s!=null && !s.isEmpty()) {
                        authors.add(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AUTHOR))));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
            cursor.close();
            return authors;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<_SQLite> getChildFolders(Context c, String whichSongFolder) {
        // This skims through all of the folders and displays folders that are children of the current folder
        // These are added to the top of the song menu
        ArrayList<_SQLite> childFolders = new ArrayList<>();
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
                    _SQLite sqLite = new _SQLite();
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
                    _SQLite sqLite = new _SQLite();
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

    ArrayList<_SQLite> getAllSongs(Context c) {
        ArrayList<_SQLite> songs = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + _SQLite.TABLE_NAME + " ORDER BY " +
                _SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    _SQLite sqLite = new _SQLite();
                    sqLite.setId(cursor.getInt(cursor.getColumnIndex(_SQLite.COLUMN_ID)));
                    sqLite.setSongid(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_SONGID))));
                    sqLite.setFilename(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FILENAME))));
                    sqLite.setFolder(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_FOLDER))));
                    sqLite.setTitle(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_TITLE))));
                    sqLite.setAuthor(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AUTHOR))));
                    sqLite.setCopyright(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_COPYRIGHT))));
                    sqLite.setLyrics(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_LYRICS))));
                    sqLite.setHymn_num(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_HYMNNUM))));
                    sqLite.setCcli(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_CCLI))));
                    sqLite.setTheme(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_THEME))));
                    sqLite.setAlttheme(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_ALTTHEME))));
                    sqLite.setUser1(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_USER1))));
                    sqLite.setUser2(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_USER2))));
                    sqLite.setUser3(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_USER3))));
                    sqLite.setKey(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_KEY))));
                    sqLite.setAka(unescapedSQL(cursor.getString(cursor.getColumnIndex(_SQLite.COLUMN_AKA))));

                    songs.add(sqLite);
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
            String countQuery = "SELECT  * FROM " + _SQLite.TABLE_NAME;
            Cursor cursor = db.rawQuery(countQuery, null);
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    private void emptyTable(SQLiteDatabase db) {
        // This drops the table if it exists (wipes it ready to start again)
        try {
            db.execSQL("DROP TABLE IF EXISTS " + _SQLite.TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetDatabase(Context c) {
        try (SQLiteDatabase db = getDB(c)) {
            emptyTable(db);
            onCreate(db);
        }
    }

    // insert data using transaction and prepared statement
    public void insertFast(Context c, StorageAccess storageAccess) {
        SQLiteDatabase db = getDB(c);
        try {
            // Insert new values or ignore rows that exist already
            String sql = "INSERT OR IGNORE INTO " + _SQLite.TABLE_NAME + " ( songid, filename, folder ) VALUES ( ?, ?, ?)";
            db.beginTransactionNonExclusive();
            SQLiteStatement stmt = db.compileStatement(sql);
            ArrayList<String> songIds = getListOfSongs(c, storageAccess);

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
                    foldername = c.getString(R.string.mainfoldername);
                }

                filename = filename.replace("/", "");

                //stmt.bindString(1, escapedSQL(s));
                //stmt.bindString(2, escapedSQL(filename));
                //stmt.bindString(3, escapedSQL(foldername));

                stmt.bindString(1, s);
                stmt.bindString(2, filename);
                stmt.bindString(3, foldername);

                stmt.execute();
                stmt.clearBindings();
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

    // Comparing strings (case insensitive .contains().  Much faster than comparing toLowerCase()
    private static boolean containsIgnoreCase(String src, String what) {
        if (src == null || what == null) { // Shouldn't be null, but if so, don't use this
            return true;
        } else {
            final int length = what.length();
            if (length == 0)
                return true; // Empty string is contained

            final char firstLo = Character.toLowerCase(what.charAt(0));
            final char firstUp = Character.toUpperCase(what.charAt(0));

            for (int i = src.length() - length; i >= 0; i--) {
                // Quick check before calling the more expensive regionMatches() method:
                final char ch = src.charAt(i);
                if (ch != firstLo && ch != firstUp)
                    continue;

                if (src.regionMatches(true, i, what, 0, length))
                    return true;
            }

            return false;
        }
    }
}*/
