package com.garethevans.church.opensongtablet.drummer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class BBSQLite extends SQLiteOpenHelper {

    // This holds the default song list searchable from the app
    public final String DATABASE_NAME = "BeatBuddy.db";
    public final String TABLE_NAME = "songs";
    public final String COLUMN_ID = "id";
    public final String COLUMN_FOLDER = "folder";
    public final String COLUMN_SONG = "song";
    public final String COLUMN_NAME = "name";
    public final String COLUMN_SIGNATURE = "signature";
    public final String COLUMN_KIT = "kit";

    // Create table SQL query.
    public final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_FOLDER + " INTEGER,"
                    + COLUMN_SONG + " INTEGER,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_SIGNATURE + " TEXT,"
                    + COLUMN_KIT + " INTEGER"
                    + ");";

    // CSV file created will give 1,10000 Reasons,37611B1D,1,BB Worship
    // Format is SONG_NUM,SONG_NAME,FOLDER_CODE,FOLDER_NUM,FOLDER_NAME
    
    private final Context c;
    private static final int DATABASE_VERSION = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "BBLite";

    private final String[][] defaultKits = new String[][]{
            {"1", "Dance"},
            {"2", "Ethereal"},
            {"3", "Jazz"},
            {"4", "Latin"},
            {"5", "Metal"},
            {"6", "Percussion"},
            {"7", "Rock"},
            {"8", "Standard"},
            {"9", "Voice"},
            {"10", "Brushes"}
    };

    private final String[][] defaultFolders = new String[][]{
            {"1", "Ballad-mini"},
            {"2", "Blues"},
            {"3", "Brazillian"},
            {"4", "Brushes Beats"},
            {"5", "Country"},
            {"6", "Drum & Bass"},
            {"7", "Funk"},
            {"8", "Hand Percussion BB Mini"},
            {"9", "Hip Hop"},
            {"10", "Jazz"},
            {"11", "Latin"},
            {"12", "Marching"},
            {"13", "Metal"},
            {"14", "Odd Time"},
            {"15", "Oldies"},
            {"16", "Pop"},
            {"17", "Punk"},
            {"18", "R&B"},
            {"19", "Rock"},
            {"20", "Reggae"},
            {"21", "Techno"},
            {"22", "Voice-Beatbox"},
            {"23", "World"},
            {"24", "Metronome"},
            {"25", "David's Beats"},
            {"26", "Test Beats"},
    };

    private ArrayList<BBSong> bbSongs;


    // The initialisers
    public BBSQLite(Context c) {
        // Don't create the database here as we don't want to recreate on each call.
        super(c,  "BeatBuddy.db", null, DATABASE_VERSION);
        this.c = c;
    }

    // Database Version
    @Override
    public void onCreate(SQLiteDatabase db) {
        // If the table doesn't exist, create it.
        if (db!=null) {
            try {
                db.execSQL(CREATE_TABLE);
                buildDefaultDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");

        // Create tables again
        onCreate(db);
    }



    // Create and reset the database
    public SQLiteDatabase getDB() {
        try {
            File f = new File(c.getExternalFilesDir("Database"), DATABASE_NAME);
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(f, null);
            // Check if the table exists
            db.execSQL(CREATE_TABLE);
            return db;
        } catch (OutOfMemoryError | Exception e) {
            return null;
        }
    }
    void emptyTable(SQLiteDatabase db) {
        // This drops the table if it exists (wipes it ready to start again)
        if (db!=null) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void resetDatabase() {
        try (SQLiteDatabase db = getDB()) {
            emptyTable(db);
            onCreate(db);
        }
    }
    public void closeCursor(Cursor cursor) {
        if (cursor!=null) {
            try {
                cursor.close();
            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
            }
        }
    }


    // Create the database for first use
    private void getDefaultSongs() {
        // Go through each entry and add the values
        bbSongs = new ArrayList<>();
        // 1. Ballad-mini
        bbSongs.add(addNewItem(1,1,"Ballad 2","4/4",7));
        bbSongs.add(addNewItem(1,2,"Ballad 3","4/4",7));
        bbSongs.add(addNewItem(1,3,"Ballad 6","4/4",7));
        bbSongs.add(addNewItem(1,4,"Ballad 8","4/4",7));
        bbSongs.add(addNewItem(1,5,"Ballad 13","4/4",7));

        // 2. Blues
        bbSongs.add(addNewItem(2,1,"Blues 1","4/4",8));
        bbSongs.add(addNewItem(2,2,"Blues 2","4/4",8));
        bbSongs.add(addNewItem(2,3,"Blues 3","6/8",8));
        bbSongs.add(addNewItem(2,4,"Blues 4","6/8",8));
        bbSongs.add(addNewItem(2,5,"Blues 5","6/8",8));
        bbSongs.add(addNewItem(2,6,"Blues 6","6/8",8));
        bbSongs.add(addNewItem(2,7,"Blues 7","6/8",8));
        bbSongs.add(addNewItem(2,8,"Blues 8","12/8",8));
        bbSongs.add(addNewItem(2,9,"Blues 9","12/8",8));
        bbSongs.add(addNewItem(2,10,"Blues 10","12/8",8));

        // 3. Brazillian
        bbSongs.add(addNewItem(3,1,"Bossa Nova","4/4",8));
        bbSongs.add(addNewItem(3,2,"Bossa Samba","4/4",8));
        bbSongs.add(addNewItem(3,3,"Bossa Nova 2","4/4",8));
        bbSongs.add(addNewItem(3,4,"Samba","4/4",8));
        bbSongs.add(addNewItem(3,5,"Samba Alt Kick","4/4",8));
        bbSongs.add(addNewItem(3,6,"Partido Alto","4/4",8));
        bbSongs.add(addNewItem(3,7,"Batukada","4/4",8));
        bbSongs.add(addNewItem(3,8,"Samba Funk","4/4",8));
        bbSongs.add(addNewItem(3,9,"Samba Reggae","4/4",8));
        bbSongs.add(addNewItem(3,10,"Afoxe","4/4",8));

        // 4. Brushes Beats
        bbSongs.add(addNewItem(4,1,"Brushes 1- Str 8","4/4",10));
        bbSongs.add(addNewItem(4,2,"Brushes 2- Str 16ths","4/4",10));
        bbSongs.add(addNewItem(4,3,"Brushes 3- Half time shuffle","4/4",10));
        bbSongs.add(addNewItem(4,4,"Brushes 4- 3/4 Shuffle Waltz","3/4",10));
        bbSongs.add(addNewItem(4,5,"Brushes 5- 3/4 Half time Shuffle Pop","3/4",10));
        bbSongs.add(addNewItem(4,6,"Brushes 6- 5/4 simple","5/4",10));
        bbSongs.add(addNewItem(4,7,"Brushes 7- 7/8 simple","7/8",10));
        bbSongs.add(addNewItem(4,8,"Brushes 8- Str 8","4/4",10));
        bbSongs.add(addNewItem(4,9,"Brushes 9- Bossa Nova brushes","4/4",10));
        bbSongs.add(addNewItem(4,10,"Brushes 10- Spanish Rumba","4/4",10));

        // 5. Country
        bbSongs.add(addNewItem(5,1,"Country 1- Shuffle","4/4",8));
        bbSongs.add(addNewItem(5,2,"Country 2- Train Shuffle","4/4",8));
        bbSongs.add(addNewItem(5,3,"Country 3- 2nd line beat","4/4",8));
        bbSongs.add(addNewItem(5,4,"Country 4- Shuffle","4/4",8));
        bbSongs.add(addNewItem(5,5,"Country 5- Brushes Train","4/4",10));
        bbSongs.add(addNewItem(5,6,"Country 6- Str 8","4/4",8));
        bbSongs.add(addNewItem(5,7,"Country 7- Brushes Shuffle","4/4",10));
        bbSongs.add(addNewItem(5,8,"Country 8- Str 8","4/4",8));

        // 6. Drum & Bass
        bbSongs.add(addNewItem(6,1,"Drum&Bass 1","4/4",1));
        bbSongs.add(addNewItem(6,2,"Drum&Bass 2","4/4",1));
        bbSongs.add(addNewItem(6,3,"Drum&Bass 3","4/4",1));
        bbSongs.add(addNewItem(6,4,"Drum&Bass 4","4/4",1));
        bbSongs.add(addNewItem(6,5,"Drum&Bass 5","4/4",1));
        bbSongs.add(addNewItem(6,6,"Drum&Bass 6","4/4",1));
        bbSongs.add(addNewItem(6,7,"Drum&Bass 7","4/4",1));
        bbSongs.add(addNewItem(6,8,"Drum&Bass 8","4/4",1));
        bbSongs.add(addNewItem(6,9,"Drum&Bass 9","4/4",1));
        bbSongs.add(addNewItem(6,10,"Drum&Bass 10","4/4",1));

        // 7. Funk
        bbSongs.add(addNewItem(7,1,"Funk 1- str 16ths","4/4",8));
        bbSongs.add(addNewItem(7,2,"Funk 2- str 16ths","4/4",8));
        bbSongs.add(addNewItem(7,3,"Funk 3- str 16ths","4/4",8));
        bbSongs.add(addNewItem(7,4,"Funk 4- str 16ths","4/4",8));
        bbSongs.add(addNewItem(7,5,"Funk 5- str 16ths","4/4",8));
        bbSongs.add(addNewItem(7,6,"Funk 6- swung","4/4",8));
        bbSongs.add(addNewItem(7,7,"Funk 7- swung","4/4",8));
        bbSongs.add(addNewItem(7,8,"Funk 8- swung","4/4",8));
        bbSongs.add(addNewItem(7,9,"Funk 9- swung","4/4",8));
        bbSongs.add(addNewItem(7,10,"Funk 10- swung","4/4",8));

        // 8. Hand Percussion BB Mini
        bbSongs.add(addNewItem(8,1,"Straight 8th","4/4",6));
        bbSongs.add(addNewItem(8,2,"Straight 16th","4/4",6));
        bbSongs.add(addNewItem(8,3,"Ballad","4/4",6));
        bbSongs.add(addNewItem(8,4,"Shuffle Funk","4/4",6));
        bbSongs.add(addNewItem(8,5,"Shuffle","4/4",6));

        // 9. Hip Hop
        bbSongs.add(addNewItem(9,1,"Hip Hop 1","4/4",1));
        bbSongs.add(addNewItem(9,2,"Hip Hop 2","4/4",1));
        bbSongs.add(addNewItem(9,3,"Hip Hop 3","4/4",1));
        bbSongs.add(addNewItem(9,4,"Hip Hop 4","4/4",1));
        bbSongs.add(addNewItem(9,5,"Hip Hop 5","4/4",1));
        bbSongs.add(addNewItem(9,6,"Hip Hop 6- swung","4/4",1));
        bbSongs.add(addNewItem(9,7,"Hip Hop 7- swung","4/4",1));
        bbSongs.add(addNewItem(9,8,"Hip Hop 8- swung","4/4",1));
        bbSongs.add(addNewItem(9,9,"Hip Hop 9- swung","4/4",1));
        bbSongs.add(addNewItem(9,10,"Hip Hop 10- swung","4/4",1));

        // 10. Jazz
        bbSongs.add(addNewItem(10,1,"Jazz 1- med swing","4/4",3));
        bbSongs.add(addNewItem(10,1,"Jazz 2- med Basie swing","4/4",3));
        bbSongs.add(addNewItem(10,1,"Jazz 3- med swing","4/4",3));
        bbSongs.add(addNewItem(10,1,"Jazz 4- med swing walk","4/4",3));
        bbSongs.add(addNewItem(10,1,"Jazz 5- med up swing","4/4",3));
        bbSongs.add(addNewItem(10,1,"Jazz 6- med up complex","4/4",3));
        bbSongs.add(addNewItem(10,1,"Jazz 7- up tempo Bebop","4/4",3));
        bbSongs.add(addNewItem(10,1,"Jazz 8- modal open swing","4/4",3));
        bbSongs.add(addNewItem(10,1,"Jazz 9- 3/4 swing","3/4",3));
        bbSongs.add(addNewItem(10,1,"Jazz 10- Jazz eight","4/4",3));

        // 11. Latin.
        bbSongs.add(addNewItem(11,1,"Bolero","4/4",4));
        bbSongs.add(addNewItem(11,2,"Bomba","4/4",4));
        bbSongs.add(addNewItem(11,3,"Calypso","4/4",4));
        bbSongs.add(addNewItem(11,4,"Cha Cha (2-3)","4/4",4));
        bbSongs.add(addNewItem(11,5,"Clave 2-3","4/4",4));
        bbSongs.add(addNewItem(11,6,"Clave 3-2","4/4",4));
        bbSongs.add(addNewItem(11,7,"Clave (Rumba) 2-3","4/4",4));
        bbSongs.add(addNewItem(11,8,"Clave (Rumba) 3-2","4/4",4));
        bbSongs.add(addNewItem(11,9,"Danzon (2-3)","4/4",4));
        bbSongs.add(addNewItem(11,10,"Mambo (2-3)","4/4",4));
        bbSongs.add(addNewItem(11,11,"Merengue (3-2)","4/4",4));
        bbSongs.add(addNewItem(11,12,"Plena","4/4",4));
        bbSongs.add(addNewItem(11,13,"Rumba Columbia (3-2)","4/4",4));
        bbSongs.add(addNewItem(11,14,"Rumba Guaguanco (3-2)","4/4",4));
        bbSongs.add(addNewItem(11,15,"Son (2-3)","4/4",4));
        bbSongs.add(addNewItem(11,16,"Son Montuno (2-3)","4/4",4));

        // 12. Marching
        bbSongs.add(addNewItem(12,1,"Marching 1","4/4",8));
        bbSongs.add(addNewItem(12,2,"Marching 2","4/4",8));
        bbSongs.add(addNewItem(12,3,"Marching 3","4/4",8));
        bbSongs.add(addNewItem(12,4,"Marching 4","4/4",8));
        bbSongs.add(addNewItem(12,5,"Marching 5","4/4",8));
        bbSongs.add(addNewItem(12,6,"Marching 6","4/4",8));
        bbSongs.add(addNewItem(12,7,"Marching 7","4/4",8));
        bbSongs.add(addNewItem(12,8,"Marching 8","4/4",8));
        bbSongs.add(addNewItem(12,9,"Marching 9","4/4",8));

        // 13. Metal
        bbSongs.add(addNewItem(13,1,"Metal 1","4/4",5));
        bbSongs.add(addNewItem(13,2,"Metal 2","4/4",5));
        bbSongs.add(addNewItem(13,3,"Metal 3","4/4",5));
        bbSongs.add(addNewItem(13,4,"Metal 4","4/4",5));
        bbSongs.add(addNewItem(13,5,"Metal 5","4/4",5));
        bbSongs.add(addNewItem(13,6,"Metal 6","4/4",5));
        bbSongs.add(addNewItem(13,7,"Metal 7","4/4",5));
        bbSongs.add(addNewItem(13,8,"Metal 8","4/4",5));
        bbSongs.add(addNewItem(13,9,"Metal 9","4/4",5));
        bbSongs.add(addNewItem(13,10,"Metal 10","4/4",5));
        bbSongs.add(addNewItem(13,11,"Metal 11","4/4",5));
        bbSongs.add(addNewItem(13,12,"Metal 12","4/4",5));

        // 14. Odd Time
        bbSongs.add(addNewItem(14,1,"Odd Time 1","5/4",8));
        bbSongs.add(addNewItem(14,2,"Odd Time 2","5/8",8));
        bbSongs.add(addNewItem(14,3,"Odd Time 3","6/8",8));
        bbSongs.add(addNewItem(14,4,"Odd Time 4","6/8",8));
        bbSongs.add(addNewItem(14,5,"Odd Time 5","7/4",8));
        bbSongs.add(addNewItem(14,6,"Odd Time 6","7/8",8));
        bbSongs.add(addNewItem(14,7,"Odd Time 7","9/4",8));
        bbSongs.add(addNewItem(14,8,"Odd Time 8","9/8",8));
        bbSongs.add(addNewItem(14,9,"Odd Time 9","9/4",8));
        bbSongs.add(addNewItem(14,10,"Odd Time 10","7/8",8));

        // 15. Oldies
        bbSongs.add(addNewItem(15,1,"Oldie 1- 4 on snare","4/4",8));
        bbSongs.add(addNewItem(15,2,"Oldie 2- 4 on snare","4/4",8));
        bbSongs.add(addNewItem(15,3,"Oldie 3- 4 on snare","4/4",8));
        bbSongs.add(addNewItem(15,4,"Oldie Song 4- Bum cha ka","4/4",8));
        bbSongs.add(addNewItem(15,5,"Oldie 5- Bum cha ka more","4/4",8));
        bbSongs.add(addNewItem(15,6,"Oldie 6","4/4",8));

        // 16. Pop
        bbSongs.add(addNewItem(16,1,"Pop 1- 16ths","4/4",8));
        bbSongs.add(addNewItem(16,2,"Pop 2- 16ths","4/4",8));
        bbSongs.add(addNewItem(16,3,"Pop 3- 16ths","4/4",8));
        bbSongs.add(addNewItem(16,4,"Pop 4- 16ths","4/4",8));
        bbSongs.add(addNewItem(16,5,"Pop 5- 16ths","4/4",8));
        bbSongs.add(addNewItem(16,6,"Pop 6- 8ths","4/4",8));
        bbSongs.add(addNewItem(16,7,"Pop 7- anticipated 1","4/4",8));
        bbSongs.add(addNewItem(16,8,"Pop 8- 8ths","4/4",8));
        bbSongs.add(addNewItem(16,9,"Pop 9- 8ths","4/4",8));
        bbSongs.add(addNewItem(16,10,"Pop 10- 8ths","4/4",8));
        bbSongs.add(addNewItem(16,11,"Pop 11- 16ths","4/4",8));
        bbSongs.add(addNewItem(16,12,"Pop 12- 8ths","4/4",8));

        // 17. Punk
        bbSongs.add(addNewItem(17,1,"Punk 1","4/4",7));
        bbSongs.add(addNewItem(17,2,"Punk 2","4/4",7));
        bbSongs.add(addNewItem(17,3,"Punk 3","4/4",7));
        bbSongs.add(addNewItem(17,4,"Punk 4","4/4",7));
        bbSongs.add(addNewItem(17,5,"Punk 5","4/4",7));

        // 18. R&B
        bbSongs.add(addNewItem(18,1,"R&B 1","4/4",1));
        bbSongs.add(addNewItem(18,2,"R&B 2","4/4",1));
        bbSongs.add(addNewItem(18,3,"R&B 3","4/4",1));
        bbSongs.add(addNewItem(18,4,"R&B 4","4/4",1));
        bbSongs.add(addNewItem(18,5,"R&B 5","4/4",1));
        bbSongs.add(addNewItem(18,6,"R&B 6","4/4",1));
        bbSongs.add(addNewItem(18,7,"R&B 7","4/4",1));
        bbSongs.add(addNewItem(18,8,"R&B 8","4/4",1));
        bbSongs.add(addNewItem(18,9,"R&B 9","4/4",1));
        bbSongs.add(addNewItem(18,10,"R&B 10","4/4",1));

        // 19. Rock
        bbSongs.add(addNewItem(19,1,"Rock 1","4/4",7));
        bbSongs.add(addNewItem(19,2,"Rock 2","4/4",7));
        bbSongs.add(addNewItem(19,3,"Rock 3","4/4",7));
        bbSongs.add(addNewItem(19,4,"Rock 4","4/4",7));
        bbSongs.add(addNewItem(19,5,"Rock 5- Jungle Toms","4/4",7));
        bbSongs.add(addNewItem(19,6,"Rock 6- Jungle Toms","4/4",7));
        bbSongs.add(addNewItem(19,7,"Rock 7","4/4",7));
        bbSongs.add(addNewItem(19,8,"Rock 8","4/4",7));
        bbSongs.add(addNewItem(19,9,"Rock 9","4/4",7));
        bbSongs.add(addNewItem(19,10,"Rock 10","4/4",7));
        bbSongs.add(addNewItem(19,11,"Rock 11","4/4",7));
        bbSongs.add(addNewItem(19,12,"Rock 12","4/4",7));
        bbSongs.add(addNewItem(19,13,"Rock 13","4/4",7));
        bbSongs.add(addNewItem(19,14,"Rock 14- Jungle Toms","4/4",7));

        // 20. Reggae
        bbSongs.add(addNewItem(20,1,"Reggae 1- backbeat","4/4",8));
        bbSongs.add(addNewItem(20,2,"Reggae 2- one drop str","4/4",8));
        bbSongs.add(addNewItem(20,3,"Reggae 3- one drop str","4/4",8));
        bbSongs.add(addNewItem(20,4,"Reggae 4- one drop str","4/4",8));
        bbSongs.add(addNewItem(20,5,"Reggae 5- Basic one drop str","4/4",8));
        bbSongs.add(addNewItem(20,6,"Reggae 6- stomp","4/4",8));
        bbSongs.add(addNewItem(20,7,"Reggae 7- swung one drop","4/4",8));
        bbSongs.add(addNewItem(20,8,"Reggae 8- swung one drop","4/4",8));
        bbSongs.add(addNewItem(20,9,"Reggae 9- swung stomp","4/4",8));
        bbSongs.add(addNewItem(20,10,"Reggae 10- Basic swung","4/4",8));

        // 21. Techno
        bbSongs.add(addNewItem(21,1,"Techno 1","4/4",1));
        bbSongs.add(addNewItem(21,2,"Techno 2","4/4",1));
        bbSongs.add(addNewItem(21,3,"Techno 3","4/4",1));
        bbSongs.add(addNewItem(21,4,"Techno 4","4/4",1));
        bbSongs.add(addNewItem(21,5,"Techno 5","4/4",1));
        bbSongs.add(addNewItem(21,6,"Techno 6","4/4",1));

        // 22. Voice-Beatbox
        bbSongs.add(addNewItem(22,1,"Beatbox 1","4/4",9));
        bbSongs.add(addNewItem(22,2,"Beatbox 2","4/4",9));
        bbSongs.add(addNewItem(22,3,"Beatbox 3","4/4",9));
        bbSongs.add(addNewItem(22,4,"Beatbox 4","4/4",9));
        bbSongs.add(addNewItem(22,5,"Beatbox 5","4/4",9));
        bbSongs.add(addNewItem(22,6,"Beatbox 6- swung","4/4",9));
        bbSongs.add(addNewItem(22,7,"Beatbox 7- swung","4/4",9));
        bbSongs.add(addNewItem(22,8,"Beatbox 8- swung","4/4",9));
        bbSongs.add(addNewItem(22,9,"Beatbox 9- swung","4/4",9));
        bbSongs.add(addNewItem(22,10,"Beatbox 10- swung","4/4",9));

        // 23. World
        bbSongs.add(addNewItem(23,1,"Tango","4/4",8));
        bbSongs.add(addNewItem(23,2,"Piazzolla","4/4",8));
        bbSongs.add(addNewItem(23,3,"Polka","2/4",8));
        bbSongs.add(addNewItem(23,4,"Polka var","2/4",8));
        bbSongs.add(addNewItem(23,5,"Serbian Ethno","4/4",8));
        bbSongs.add(addNewItem(23,6,"Waltz","3/4",8));
        bbSongs.add(addNewItem(23,7,"Spanish Buleria","4/4",8));
        bbSongs.add(addNewItem(23,8,"Spanish Rumba","4/4",8));
        bbSongs.add(addNewItem(23,9,"Buleria w brushes","4/4",10));
        bbSongs.add(addNewItem(23,10,"Spanish 3/4 beat- brushes","3/4",10));

        // 24. Metronome
        bbSongs.add(addNewItem(24,1,"Metronome- Quarters- no accent","1/4",6));
        bbSongs.add(addNewItem(24,2,"Metronome- 2/4","2/4",6));
        bbSongs.add(addNewItem(24,3,"Metronome- 3/4","3/4",6));
        bbSongs.add(addNewItem(24,4,"Metronome- 4/4","4/4",6));
        bbSongs.add(addNewItem(24,5,"Metronome- 5/4","5/4",6));
        bbSongs.add(addNewItem(24,6,"Metronome- 6/4","6/4",6));
        bbSongs.add(addNewItem(24,7,"Metronome- 6/8","6/8",6));
        bbSongs.add(addNewItem(24,8,"Metronome- 7/4","7/4",6));
        bbSongs.add(addNewItem(24,9,"Metronome- 7/8","7/8",6));
        bbSongs.add(addNewItem(24,10,"Metronome- 9/8","9/8",6));
        bbSongs.add(addNewItem(24,11,"Metronome- 11/8","11/8",6));

        // 25. David's Beats
        bbSongs.add(addNewItem(25,1,"Ska","4/4",8));
        bbSongs.add(addNewItem(25,2,"In my place","4/4",8));
        bbSongs.add(addNewItem(25,3,"Shuffle Funk","4/4",8));
        bbSongs.add(addNewItem(25,4,"Pop-Rock Str8","4/4",8));
        bbSongs.add(addNewItem(25,5,"16ths Funk","4/4",8));
        bbSongs.add(addNewItem(25,6,"Shuffle Beat","4/4",8));
        bbSongs.add(addNewItem(25,7,"Wonderful World","4/4",8));
        bbSongs.add(addNewItem(25,8,"Times Change","4/4",8));
        bbSongs.add(addNewItem(25,9,"Singular Sound video","4/4",8));

        // 26. Test Beats
        bbSongs.add(addNewItem(26,1,"Test Drumset Components Volumes","4/4",11));

    }

    private BBSong addNewItem(int folder, int song, String name, String signature, int kit) {
        BBSong bbSong = new BBSong();
        bbSong.folder = folder;
        bbSong.song = song;
        bbSong.name = name;
        bbSong.signature = signature;
        bbSong.kit = kit;
        bbSong.foldername = getFolderNameForNumber(folder);
        bbSong.kitname = getDrumKitForNumber(kit);
        return bbSong;
    }

    public void buildDefaultDatabase() {
        // If we haven't built the bbSongs, do that first
        if (bbSongs==null || bbSongs.isEmpty()) {
            getDefaultSongs();
        }

        // If the database doesn't exist, create it
        SQLiteDatabase db = getDB();

        // Insert new values or ignore rows that exist already
        for (BBSong bbSong : bbSongs) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_FOLDER, bbSong.folder);
            values.put(COLUMN_SONG, bbSong.song);
            values.put(COLUMN_NAME, bbSong.name);
            values.put(COLUMN_SIGNATURE, bbSong.signature);
            values.put(COLUMN_KIT, bbSong.kit);

            // Insert the new row
            try {
                db.insert(TABLE_NAME, null, values);
            } catch (Exception e) {
                Log.d(TAG, bbSong.name + " already exists in the table, not able to create.");
            }
        }

        db.close();
    }

    public ArrayList<String> searchUniqueValues(String what) {
        SQLiteDatabase db = getDB();
        // Check we have populated the database
        long numberOfRows = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        if (numberOfRows==0) {
            buildDefaultDatabase();
        }

        ArrayList<String> values = new ArrayList<>();
        String q = "SELECT DISTINCT " + what + " FROM " + TABLE_NAME + " ORDER BY " +
                what + " ASC";

        Cursor cursor = db.rawQuery(q, null);
        cursor.moveToFirst();
        if (cursor.getColumnCount()>0 && cursor.getColumnIndex(what)==0) {
            for (int x=0; x<cursor.getCount(); x++) {
                cursor.moveToPosition(x);
                String value;
                int resultInt;
                switch (what) {
                    case COLUMN_FOLDER:
                        resultInt = cursor.getInt(cursor.getColumnIndexOrThrow(what));
                        // Get the name from this number
                        value = getFolderNameForNumber(resultInt);
                        break;
                    case COLUMN_SIGNATURE:
                        value = cursor.getString(cursor.getColumnIndexOrThrow(what));
                        break;
                    case COLUMN_KIT:
                        resultInt = cursor.getInt(cursor.getColumnIndexOrThrow(what));
                        // Get the name from this number
                        value = getDrumKitForNumber(resultInt);
                        break;
                    default:
                        value = "";
                }
                values.add(value);
            }
        }
        closeCursor(cursor);
        values.add(0,"");
        db.close();
        return values;
    }

    public ArrayList<BBSong> getSongsByFilters(String folderVal, String timeSigVal, String kitVal) {
        try (SQLiteDatabase db = getDB()) {
            ArrayList<BBSong> bbSongsFound = new ArrayList<>();
            // To avoid SQL injections, we need to build the args
            ArrayList<String> args = new ArrayList<>();
            String sqlMatch = "";
            if (folderVal != null && !folderVal.isEmpty()) {
                sqlMatch += COLUMN_FOLDER + "= ? AND ";
                args.add(""+getNumberFromFolder(folderVal));
            }
            if (timeSigVal != null && !timeSigVal.isEmpty()) {
                sqlMatch += COLUMN_SIGNATURE + "= ? AND ";
                args.add(timeSigVal);
            }
            if (kitVal != null && !kitVal.isEmpty()) {
                sqlMatch += COLUMN_KIT + "= ?";
                args.add(""+getNumberFromKit(kitVal));
            }

            if (!sqlMatch.isEmpty()) {
                sqlMatch = "WHERE " + sqlMatch;
                if (sqlMatch.trim().endsWith("AND")) {
                    sqlMatch = sqlMatch.substring(0, sqlMatch.lastIndexOf("AND ")).trim();
                }
            }

            String getOrderBySQL = "ORDER BY " + COLUMN_FOLDER + " COLLATE NOCASE ASC," +
                    COLUMN_SONG + " ASC";
            String getBasicSQLQueryStart = "SELECT " + COLUMN_FOLDER + ", " +
                    COLUMN_SONG + ", " + COLUMN_NAME + ", " +
                    COLUMN_SIGNATURE + ", " + COLUMN_KIT +
                    " FROM " + TABLE_NAME + " ";
            String selectQuery = getBasicSQLQueryStart.trim() + " " + sqlMatch.trim() + " " + getOrderBySQL.trim();
            String[] selectionArgs = new String[args.size()];
            selectionArgs = args.toArray(selectionArgs);

            Cursor cursor = db.rawQuery(selectQuery, selectionArgs);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    int folder = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER));
                    int song = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SONG));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    String signature = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIGNATURE));
                    int kit = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_KIT));

                    BBSong bbSong = new BBSong();
                    bbSong.folder = folder;
                    bbSong.song = song;
                    bbSong.name = name;
                    bbSong.signature = signature;
                    bbSong.kit = kit;
                    bbSong.foldername = getFolderNameForNumber(folder);
                    bbSong.kitname = getDrumKitForNumber(kit);

                    bbSongsFound.add(bbSong);
                }
                while (cursor.moveToNext());
            }

            // close cursor connection
            closeCursor(cursor);

            //Return the songs
            return bbSongsFound;
        } catch (OutOfMemoryError | Exception e) {
            Log.d(TAG,"Table/database error");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String getFolderNameForNumber(int number) {
        for (String[] folder:defaultFolders) {
            if (folder[0].equals(""+number)) {
                return folder[1];
            }
        }
        return "";
    }
    public int getNumberFromFolder(String foldername) {
        for (String[] folder:defaultFolders) {
            if (folder[1].equals(foldername)) {
                return Integer.parseInt(folder[0]);
            }
        }
        return -1;
    }

    public int getNumberFromKit(String kitname) {
        for (String[] kit:defaultKits) {
            if (kit[1].equals(kitname)) {
                return Integer.parseInt(kit[0]);
            }
        }
        return -1;
    }

    public String getDrumKitForNumber(int number) {
        for (String[] kit:defaultKits) {
            if (kit[0].equals(""+number)) {
                return kit[1];
            }
        }
        return "";
    }

}
