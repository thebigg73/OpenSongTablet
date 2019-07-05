package com.garethevans.church.opensongtablet;

import android.content.ContentValues;
import android.content.Context;
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

public class SQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    SQLiteHelper(Context context) {
        super(context,  SQLite.DATABASE_NAME, null, DATABASE_VERSION);
        // Don't create the database here as we don't want to recreate on each call.
    }

    SQLiteDatabase getDB (Context c) {
        File f = new File(c.getExternalFilesDir("Database"), SQLite.DATABASE_NAME);
        return SQLiteDatabase.openOrCreateDatabase(f,null);
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
        return s.replace("'","''");
    }

    void createImportedSong(Context c, String folder, String filename, String title, String author,
                            String copyright, String key, String time_sig, String ccli, String lyrics) {
        // Creates a basic song entry to the database (id, songid, folder, file)
        try (SQLiteDatabase db = getDB(c)) {
            filename = escapedSQL(filename);
            folder = escapedSQL(folder);
            String songid = folder + "/" + filename;
            ContentValues values = new ContentValues();
            values.put(SQLite.COLUMN_SONGID, songid);
            values.put(SQLite.COLUMN_FOLDER, folder);
            values.put(SQLite.COLUMN_FILENAME, filename);
            values.put(SQLite.COLUMN_TITLE, title);
            values.put(SQLite.COLUMN_AUTHOR, author);
            values.put(SQLite.COLUMN_COPYRIGHT, copyright);
            values.put(SQLite.COLUMN_KEY, key);
            values.put(SQLite.COLUMN_TIMESIG, time_sig);
            values.put(SQLite.COLUMN_CCLI, ccli);
            values.put(SQLite.COLUMN_LYRICS, lyrics);

            // Insert the new row, returning the primary key value of the new row
            db.insert(SQLite.TABLE_NAME, null, values);
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
            String songid = folder + "/" + filename;
            ContentValues values = new ContentValues();
            values.put(SQLite.COLUMN_SONGID, songid);
            values.put(SQLite.COLUMN_FOLDER, folder);
            values.put(SQLite.COLUMN_FILENAME, filename);

            // Insert the new row, returning the primary key value of the new row
            try {
                db.insert(SQLite.TABLE_NAME, null, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    SQLite setSong(SQLite sqLite) {
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

    void updateSong(Context c, SQLite sqLite) {

        try (SQLiteDatabase db = getDB(c)) {
            ContentValues values = new ContentValues();
            values.put(SQLite.COLUMN_ID, sqLite.getId());
            values.put(SQLite.COLUMN_SONGID, sqLite.getSongid());
            values.put(SQLite.COLUMN_FILENAME, sqLite.getFilename());
            values.put(SQLite.COLUMN_FOLDER, sqLite.getFolder());
            values.put(SQLite.COLUMN_TITLE, sqLite.getTitle());
            values.put(SQLite.COLUMN_AUTHOR, sqLite.getAuthor());
            values.put(SQLite.COLUMN_COPYRIGHT, sqLite.getCopyright());
            values.put(SQLite.COLUMN_LYRICS, sqLite.getLyrics());
            values.put(SQLite.COLUMN_HYMNNUM, sqLite.getHymn_num());
            values.put(SQLite.COLUMN_CCLI, sqLite.getCcli());
            values.put(SQLite.COLUMN_THEME, sqLite.getTheme());
            values.put(SQLite.COLUMN_ALTTHEME, sqLite.getAlttheme());
            values.put(SQLite.COLUMN_USER1, sqLite.getUser1());
            values.put(SQLite.COLUMN_USER2, sqLite.getUser2());
            values.put(SQLite.COLUMN_USER3, sqLite.getUser3());
            values.put(SQLite.COLUMN_KEY, sqLite.getKey());
            values.put(SQLite.COLUMN_TIMESIG, sqLite.getTimesig());
            values.put(SQLite.COLUMN_AKA, sqLite.getAka());

            long l = db.update(SQLite.TABLE_NAME, values, SQLite.COLUMN_ID + "=?", new String[]{String.valueOf(sqLite.getId())});
            Log.d("updateSong", "l=" + l);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    SQLite getSong(Context c, String songid) {
        SQLiteDatabase db = getDB(c);
        Cursor cursor = db.query(SQLite.TABLE_NAME,
                new String[]{SQLite.COLUMN_ID,SQLite.COLUMN_SONGID, SQLite.COLUMN_FILENAME, SQLite.COLUMN_FOLDER,
                SQLite.COLUMN_TITLE, SQLite.COLUMN_AUTHOR, SQLite.COLUMN_COPYRIGHT, SQLite.COLUMN_LYRICS,
                        SQLite.COLUMN_HYMNNUM, SQLite.COLUMN_CCLI, SQLite.COLUMN_THEME,
                        SQLite.COLUMN_ALTTHEME, SQLite.COLUMN_USER1, SQLite.COLUMN_USER2,
                        SQLite.COLUMN_USER3, SQLite.COLUMN_KEY, SQLite.COLUMN_TIMESIG, SQLite.COLUMN_AKA},
                SQLite.COLUMN_SONGID + "=?",
                new String[]{String.valueOf(songid)}, null, null, SQLite.COLUMN_FILENAME, null);

        if (cursor != null) {
            cursor.moveToFirst();

            try {
                // prepare note object
                SQLite sqLite = new SQLite(
                        cursor.getInt(cursor.getColumnIndex(SQLite.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_SONGID)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_COPYRIGHT)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_LYRICS)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_HYMNNUM)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_CCLI)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_THEME)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_ALTTHEME)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER1)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER2)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER3)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_KEY)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_TIMESIG)),
                        cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AKA)));

                // close the db connection
                cursor.close();
                db.close();
                return sqLite;
            } catch (Exception e) {
                Log.d("SQLiteHelper","Song not found");
                return null;
            } finally {
                db.close();
            }
        } else {
            return null;
        }


    }

    void updateFolderName(Context c, String oldFolder, String newFolder) {
        // Select matching folder Query
        String selectQuery = "SELECT "+SQLite.COLUMN_SONGID + ", " +
                SQLite.COLUMN_FOLDER + ", " +
                SQLite.COLUMN_FILENAME + " " +
                "FROM " + SQLite.TABLE_NAME +
                " WHERE " + SQLite.COLUMN_SONGID + " LIKE '%" + oldFolder + "/%'" +
                " ORDER BY " + SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String currSongId = cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_SONGID));
                    String updatedId = currSongId.replace(oldFolder + "/", newFolder + "/");
                    ContentValues values = new ContentValues();
                    values.put(SQLite.COLUMN_SONGID, updatedId);
                    values.put(SQLite.COLUMN_FOLDER, newFolder);

                    db.update(SQLite.TABLE_NAME, values, SQLite.COLUMN_SONGID + "=?", new String[]{currSongId});

                } while (cursor.moveToNext());
            }

            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // close db connection
    }

    void deleteSong(Context c, String songId) {
        try (SQLiteDatabase db = getDB(c)) {
            db.delete(SQLite.TABLE_NAME, SQLite.COLUMN_SONGID + " = ?",
                    new String[]{String.valueOf(songId)});
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
                " WHERE " + SQLite.COLUMN_FOLDER + "='" + whichSongFolder + "'" +
                " ORDER BY " + SQLite.COLUMN_FILENAME + " COLLATE NOCASE ASC";

        try (SQLiteDatabase db = getDB(c)) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    SQLite sqLite = new SQLite();
                    sqLite.setFilename(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME)));
                    sqLite.setAuthor(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR)));
                    sqLite.setKey(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_KEY)));
                    if (!sqLite.getFilename().equals("") && !files.contains("$__" + sqLite.getFolder() + "/" + sqLite.getFilename() + "__$")) {
                        // This avoids adding references to folders more than once
                        songs.add(sqLite);
                        files.add("$__" + sqLite.getFolder() + "/" + sqLite.getFilename() + "__$");
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
                folders.add(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER)));
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

            if (!f.equals(c.getString(R.string.mainfoldername)) &&
                    (whichSongFolder.equals(c.getString(R.string.mainfoldername)) ||
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

            } else if (!f.equals(c.getString(R.string.mainfoldername)) && !f.equals(whichSongFolder) &&
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

        /*// Sort alphabetically
        Collator collator = Collator.getInstance(StaticVariables.locale);
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(childFolders, collator);*/
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
                    sqLite.setSongid(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_SONGID)));
                    sqLite.setFilename(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME)));
                    sqLite.setFolder(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER)));
                    sqLite.setTitle(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_TITLE)));
                    sqLite.setAuthor(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AUTHOR)));
                    sqLite.setCopyright(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_COPYRIGHT)));
                    sqLite.setLyrics(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_LYRICS)));
                    sqLite.setHymn_num(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_HYMNNUM)));
                    sqLite.setCcli(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_CCLI)));
                    sqLite.setTheme(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_THEME)));
                    sqLite.setAlttheme(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_ALTTHEME)));
                    sqLite.setUser1(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER1)));
                    sqLite.setUser2(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER2)));
                    sqLite.setUser3(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_USER3)));
                    sqLite.setKey(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_KEY)));
                    sqLite.setAka(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_AKA)));

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
            String countQuery = "SELECT  * FROM " + SQLite.TABLE_NAME;
            Cursor cursor = db.rawQuery(countQuery, null);
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    private void emptyTable(SQLiteDatabase db) {
        // This drops the table if it exists (wipes it ready to start again)
        db.execSQL("DROP TABLE IF EXISTS "+SQLite.TABLE_NAME);
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

            for (String s : songIds) {

                String filename;
                String foldername;
                //if (!s.endsWith("/")) {
                    // Only add song files, so if it ends with / this loop skips
                    if (s.endsWith("/")) {
                        filename = "";
                        foldername = s.substring(0,s.lastIndexOf("/"));
                    } else if (s.contains("/")) {
                            filename = s.substring(s.lastIndexOf("/"));
                            foldername = s.replace(filename, "");
                    } else {
                            filename = s;
                            foldername = c.getString(R.string.mainfoldername);
                        }
                    //}
                    filename = filename.replace("/", "");

                    stmt.bindString(1, s);
                    stmt.bindString(2, filename);
                    stmt.bindString(3, foldername);

                    stmt.execute();
                    stmt.clearBindings();
                //}
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