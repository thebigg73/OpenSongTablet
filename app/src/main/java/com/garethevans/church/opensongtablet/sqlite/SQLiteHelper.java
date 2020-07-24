package com.garethevans.church.opensongtablet.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    public SQLiteHelper(Context context) {
        super(context,  SQLite.DATABASE_NAME, null, DATABASE_VERSION);
        // Don't create the database here as we don't want to recreate on each call.
    }

    public SQLiteDatabase getDB(Context c) {
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
        db.execSQL(SQLite.CREATE_TABLE);
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
                values.put(SQLite.COLUMN_SONGID, escapedSQL(songid));
                values.put(SQLite.COLUMN_FOLDER, escapedSQL(folder));
                values.put(SQLite.COLUMN_FILENAME, escapedSQL(filename));

                // Insert the new row, returning the primary key value of the new row
                try {
                    db.insert(SQLite.TABLE_NAME, null, values);
                } catch (Exception e) {
                    Log.d("SQLiteHelper",songid + " already exists in the table, not able to create.");
                }
            }
        }
    }

    public boolean songIdExists(SQLiteDatabase db, String songid) {
        String Query = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + " = \"" + escapedSQL(songid) + "\"";
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void updateSong(Context c, SQLite sqLite) {

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
            values.put(SQLite.COLUMN_AKA, escapedSQL(sqLite.getAka()));

            long l = db.update(SQLite.TABLE_NAME, values, SQLite.COLUMN_ID + "=?", new String[]{String.valueOf(sqLite.getId())});
            Log.d("updateSong", "l=" + l);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SQLite getSong(Context c, String songid) {
        SQLiteDatabase db = getDB(c);
        try {
            Cursor cursor = db.query(SQLite.TABLE_NAME,
                    new String[]{SQLite.COLUMN_ID, SQLite.COLUMN_SONGID, SQLite.COLUMN_FILENAME, SQLite.COLUMN_FOLDER,
                            SQLite.COLUMN_TITLE, SQLite.COLUMN_AUTHOR, SQLite.COLUMN_COPYRIGHT, SQLite.COLUMN_LYRICS,
                            SQLite.COLUMN_HYMNNUM, SQLite.COLUMN_CCLI, SQLite.COLUMN_THEME,
                            SQLite.COLUMN_ALTTHEME, SQLite.COLUMN_USER1, SQLite.COLUMN_USER2,
                            SQLite.COLUMN_USER3, SQLite.COLUMN_KEY, SQLite.COLUMN_TIMESIG, SQLite.COLUMN_AKA},
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
                            unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AKA))));

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

    public void deleteSong(Context c, String songId) {
        try (SQLiteDatabase db = getDB(c)) {
            db.delete(SQLite.TABLE_NAME, SQLite.COLUMN_SONGID + " = ?",
                    new String[]{String.valueOf(escapedSQL(songId))});
        }
    }

    public ArrayList<String> getThemes(Context c, Preferences preferences, StorageAccess storageAccess) {
        ArrayList<String> themes = new ArrayList<>();

        // Select matching folder Query
        String selectQuery = "SELECT "+ SQLite.COLUMN_FILENAME + ", " +
                SQLite.COLUMN_THEME + " " +
                "FROM " + SQLite.TABLE_NAME +
                " ORDER BY " + SQLite.COLUMN_THEME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    SQLite sqLite = new SQLite();
                    sqLite.setTheme(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_THEME))));
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

    private String getBasicSQLQueryStart() {
        return "SELECT " + SQLite.COLUMN_FILENAME + ", " + SQLite.COLUMN_AUTHOR + ", " +
                SQLite.COLUMN_KEY + ", " + SQLite.COLUMN_FOLDER + ", " + SQLite.COLUMN_THEME + ", " +
                SQLite.COLUMN_ALTTHEME + ", " + SQLite.COLUMN_USER1 + ", " + SQLite.COLUMN_USER2 + ", " +
                SQLite.COLUMN_USER3 + ", " + SQLite.COLUMN_LYRICS + ", " + SQLite.COLUMN_HYMNNUM +
                " FROM " + SQLite.TABLE_NAME + " ";
    }
    private String getOrderBySQL() {
        return "ORDER BY " + SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";
    }

    public String getSongId() {
        return StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
    }

    public String getAnySongId(String folder, String filename) {
        return folder + "/" + filename;
    }

    private String getSetString(Context c, String folder, String filename) {
        if (folder == null || folder.equals(c.getString(R.string.mainfoldername)) || folder.isEmpty()) {
            return "$**_" + filename + "_**$";
        } else {
            return "$**_" + folder + "/" + filename + "_**$";
        }
    }

    public ArrayList<SQLite> getSongsByFilters(Context c, boolean searchByFolder, boolean searchByArtist,
                                                                                     boolean searchByKey, boolean searchByTag,
                                                                                     boolean searchByFilter, String folderVal, String artistVal,
                                                                                     String keyVal, String tagVal, String filterVal) {
        ArrayList<SQLite> songs = new ArrayList<>();
        StaticVariables.songsInList = new ArrayList<>();
        StaticVariables.songsInList.clear();

        String sqlMatch = "";
        if (searchByFolder && folderVal!=null && folderVal.length()>0) {
            sqlMatch += SQLite.COLUMN_FOLDER + "=\"" + folderVal + "\" AND ";
        }
        if (searchByArtist && artistVal!=null && artistVal.length()>0) {
            sqlMatch += SQLite.COLUMN_AUTHOR + " LIKE \"%" + artistVal + "%\" AND ";
        }
        if (searchByKey && keyVal!=null && keyVal.length()>0) {
            sqlMatch += SQLite.COLUMN_KEY + "=\"" + keyVal + "\" AND ";
        }
        if (searchByTag && tagVal!=null && tagVal.length()>0) {
            sqlMatch += "(" + SQLite.COLUMN_THEME + " LIKE \"%" + tagVal + "%\" OR ";
            sqlMatch += SQLite.COLUMN_ALTTHEME + " LIKE \"%" + tagVal + "%\") AND ";
        }
        if (searchByFilter && filterVal!=null && filterVal.length()>0) {
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
        String selectQuery = getBasicSQLQueryStart() + sqlMatch + " " + getOrderBySQL();

        try (SQLiteDatabase db = getDB(c)) {
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
                    String setString = getSetString(c,fo,fi);
                    StaticVariables.songsInList.add(setString);
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

    public SQLite getSpecificSong(Context c, String folder, String filename) {
        String id = getAnySongId(folder,filename);
        String sql = "SELECT * FROM " + SQLite.TABLE_NAME + " WHERE " + SQLite.COLUMN_SONGID + "=\"" + id + "\"";
        SQLite thisSong = new SQLite();

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(sql, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                String ti = unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_TITLE)));
                String au = unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR)));
                String co = unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_COPYRIGHT)));
                String ke = unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_KEY)));
                String ly = unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_LYRICS)));

                thisSong.setFilename(filename);
                thisSong.setTitle(ti);
                thisSong.setFolder(folder);
                thisSong.setSongid(id);
                thisSong.setAuthor(au);
                thisSong.setKey(ke);
                thisSong.setCopyright(co);
                thisSong.setLyrics(ly);
            }

            // close db connection
            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return thisSong;
    }

    public ArrayList<String> getFolders(Context c) {
        // Get the database
        ArrayList<String> folders = new ArrayList<>();
        try (SQLiteDatabase db = getDB(c)) {
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
            //db.close();
            return folders;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<String> getAuthors(Context c) {
        // Get the database
        ArrayList<String> authors = new ArrayList<>();
        authors.add("");
        try (SQLiteDatabase db = getDB(c)) {
            String q = "SELECT DISTINCT " + SQLite.COLUMN_AUTHOR + " FROM " + SQLite.TABLE_NAME + " ORDER BY " +
                    SQLite.COLUMN_AUTHOR + " ASC";
            Cursor cursor = db.rawQuery(q, null);
            cursor.moveToFirst();
            do {
                try {
                    String s = cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR));
                    if (s!=null && !s.isEmpty()) {
                        authors.add(unescapedSQL(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR))));
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

    private void emptyTable(SQLiteDatabase db) {
        // This drops the table if it exists (wipes it ready to start again)
        try {
            db.execSQL("DROP TABLE IF EXISTS " + SQLite.TABLE_NAME);
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
            String sql = "INSERT OR IGNORE INTO " + SQLite.TABLE_NAME + " ( songid, filename, folder ) VALUES ( ?, ?, ?)";
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

}