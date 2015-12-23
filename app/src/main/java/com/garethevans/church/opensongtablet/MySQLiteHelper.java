package com.garethevans.church.opensongtablet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class MySQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "SongDB";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create song table
        String CREATE_SONG_TABLE = "CREATE TABLE songs ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fileid TEXT, "+
                "title TEXT, "+
                "author TEXT, "+
                "lyrics TEXT)";

        // create sont table
        db.execSQL(CREATE_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older songs table if existed
        db.execSQL("DROP TABLE IF EXISTS songs");

        // create fresh songs table
        this.onCreate(db);
    }
    //---------------------------------------------------------------------

    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */

    // Songs table name
    private static final String TABLE_SONGS = "songs";

    // Books Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_FILEID = "fileid";
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_LYRICS = "lyrics";

    private static final String[] COLUMNS = {KEY_ID,KEY_FILEID,KEY_TITLE,KEY_AUTHOR,KEY_LYRICS};

    public void addSong(Songs song){
        Log.d("addSong", song.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_FILEID, song.getFileId()); // get fileid
        values.put(KEY_TITLE, song.getTitle()); // get title
        values.put(KEY_AUTHOR, song.getAuthor()); // get author
        values.put(KEY_LYRICS, song.getLyrics()); // get lyrics

        // 3. insert
        db.insert(TABLE_SONGS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Songs getSong(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_SONGS, // a. table
                        COLUMNS, // b. column names
                        " fileid = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build song object
        Songs song = new Songs();
        song.setId(Integer.parseInt(cursor.getString(0)));
        song.setFileId(cursor.getString(1));
        song.setTitle(cursor.getString(2));
        song.setAuthor(cursor.getString(3));
        song.setLyrics(cursor.getString(4));

        Log.d("getSong("+id+")", song.toString());

        // 5. return song
        return song;
    }

    // Get All Songs
    public List<Songs> getAllSongs() {
        List<Songs> songs = new LinkedList<Songs>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_SONGS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build song and add it to list
        Songs song = null;
        if (cursor.moveToFirst()) {
            do {
                song = new Songs();
                song.setId(Integer.parseInt(cursor.getString(0)));
                song.setFileId(cursor.getString(1));
                song.setTitle(cursor.getString(2));
                song.setAuthor(cursor.getString(3));
                song.setLyrics(cursor.getString(4));

                // Add song to songs
                songs.add(song);
            } while (cursor.moveToNext());
        }

        Log.d("getAllSongs()", songs.toString());

        return songs;
    }

    // Updating single song
    public int updateSongs(Songs song) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("fileid", song.getFileId()); // get fileid
        values.put("title", song.getTitle()); // get title
        values.put("author", song.getAuthor()); // get author
        values.put("lyrics", song.getLyrics()); // get lyrics

        // 3. updating row
        int i = db.update(TABLE_SONGS, //table
                values, // column/value
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf(song.getId()) }); //selection args

        // 4. close
        db.close();

        return i;

    }

    // Deleting single song
    public void deleteSong(Songs song) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_SONGS,
                KEY_ID+" = ?",
                new String[] { String.valueOf(song.getId()) });

        // 3. close
        db.close();

        Log.d("deleteSong", song.toString());

    }
 }