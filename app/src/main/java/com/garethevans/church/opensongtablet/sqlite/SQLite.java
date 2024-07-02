package com.garethevans.church.opensongtablet.sqlite;

// This file holds the column and table names
// It also has the build SQL query for the table.

// This class is used to build the song database and then query it for searches
// If the database is good to go (after full index) we get songs from here = quicker
// If the database isn't fully indexed, we load the song from the file
// If we create, update, delete songs we do that to both the SQL database and the file
// Non OpenSong songs (PDF, Images) have their details stored in a persistent database
// This is stored in the Settings folder to allow syncing between devices
// After indexing songs, PDFs and images will just have the songid, folder and filename to begin with
// We then update their records in the SQL database using the persistent NonSQLDatabase entries
// Updates to PDFs and images therefore require edits in the SQL database and the NonSQLDatabase
// We only need to touch the PDF/image if we delete it

public class SQLite {

    // The initialisers
    public SQLite() {}

/*
    The values of the database and Song object should be in alphabetical order...
    (Values in brackets are the xml file fields if they are different)

    abc (abcnotation)
    abctranspose
    aka
    alttheme
    author
    autoscrolldelay
    autoscrolllength
    beatbuddysong
    beatbuddykit
    capo
    capoprint
    ccli
    copyright
    customchords
    filename
    filetype
    folder
    hymnnum
    id
    key
    keyoriginal
    linkaudio
    linkother
    linkweb
    linkyoutube
    lyrics
    midi
    midiindex
    notes
    padfile
    padloop
    preferredinstrument (preferred_instrument)
    presentationorder (presentation)
    songid
    theme
    timesig
    title
    user1
    user2
    user3

*/

    // The table columns
    public static final String DATABASE_NAME = "Songs.db";
    public static final String NON_OS_DATABASE_NAME = "NonOpenSongSongs.db";
    public static final String TABLE_NAME = "songs";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SONGID = "songid";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_FOLDER = "folder";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_COPYRIGHT = "copyright";
    public static final String COLUMN_LYRICS = "lyrics";
    public static final String COLUMN_HYMNNUM = "hymn_num";
    public static final String COLUMN_CCLI = "ccli";
    public static final String COLUMN_THEME = "theme";
    public static final String COLUMN_ALTTHEME = "alttheme";
    public static final String COLUMN_USER1 = "user1";
    public static final String COLUMN_USER2 = "user2";
    public static final String COLUMN_USER3 = "user3";
    public static final String COLUMN_BEATBUDDY_SONG = "beatbuddysong";
    public static final String COLUMN_BEATBUDDY_KIT = "beatbuddykit";
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_KEY_ORIGINAL = "keyoriginal";
    public static final String COLUMN_PREFERRED_INSTRUMENT = "preferredinstrument";
    public static final String COLUMN_TIMESIG = "timesig";
    public static final String COLUMN_AKA = "aka";
    public static final String COLUMN_AUTOSCROLL_DELAY = "autoscrolldelay";
    public static final String COLUMN_AUTOSCROLL_LENGTH = "autoscrolllength";
    public static final String COLUMN_TEMPO = "tempo";
    public static final String COLUMN_PAD_FILE = "padfile";
    public static final String COLUMN_PAD_LOOP = "padloop";
    public static final String COLUMN_MIDI = "midi";
    public static final String COLUMN_MIDI_INDEX = "midiindex";
    public static final String COLUMN_CAPO = "capo";
    public static final String COLUMN_CAPO_PRINT = "capoprint";
    public static final String COLUMN_CUSTOM_CHORDS = "customchords";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_ABC = "abc";
    public static final String COLUMN_ABC_TRANSPOSE = "abctranspose";
    public static final String COLUMN_LINK_YOUTUBE = "linkyoutube";
    public static final String COLUMN_LINK_WEB = "linkweb";
    public static final String COLUMN_LINK_AUDIO = "linkaudio";
    public static final String COLUMN_LINK_OTHER = "linkother";
    public static final String COLUMN_PRESENTATIONORDER = "presentationorder";
    public static final String COLUMN_FILETYPE = "filetype";

    // Create table SQL query.  Because this will have non OpenSong stuff too, include all useable fields
    static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_SONGID + " TEXT UNIQUE,"
                    + COLUMN_FILENAME + " TEXT,"
                    + COLUMN_FOLDER + " TEXT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_AUTHOR + " TEXT,"
                    + COLUMN_COPYRIGHT + " TEXT,"
                    + COLUMN_LYRICS + " TEXT,"
                    + COLUMN_HYMNNUM + " TEXT,"
                    + COLUMN_CCLI + " TEXT,"
                    + COLUMN_THEME + " TEXT,"
                    + COLUMN_ALTTHEME + " TEXT,"
                    + COLUMN_USER1 + " TEXT,"
                    + COLUMN_USER2 + " TEXT,"
                    + COLUMN_USER3 + " TEXT,"
                    + COLUMN_BEATBUDDY_SONG + " TEXT,"
                    + COLUMN_BEATBUDDY_KIT + " TEXT,"
                    + COLUMN_KEY + " TEXT,"
                    + COLUMN_KEY_ORIGINAL + " TEXT,"
                    + COLUMN_PREFERRED_INSTRUMENT + " TEXT,"
                    + COLUMN_TIMESIG + " TEXT,"
                    + COLUMN_AKA + " TEXT,"
                    + COLUMN_AUTOSCROLL_DELAY + " TEXT,"
                    + COLUMN_AUTOSCROLL_LENGTH + " TEXT,"
                    + COLUMN_TEMPO + " TEXT,"
                    + COLUMN_PAD_FILE + " TEXT,"
                    + COLUMN_PAD_LOOP + " TEXT,"
                    + COLUMN_MIDI + " TEXT,"
                    + COLUMN_MIDI_INDEX + " TEXT,"
                    + COLUMN_CAPO + " TEXT,"
                    + COLUMN_CAPO_PRINT + " TEXT,"
                    + COLUMN_CUSTOM_CHORDS + " TEXT,"
                    + COLUMN_NOTES + " TEXT,"
                    + COLUMN_ABC + " TEXT,"
                    + COLUMN_ABC_TRANSPOSE + " TEXT,"
                    + COLUMN_LINK_YOUTUBE + " TEXT,"
                    + COLUMN_LINK_WEB + " TEXT,"
                    + COLUMN_LINK_AUDIO + " TEXT,"
                    + COLUMN_LINK_OTHER + " TEXT,"
                    + COLUMN_PRESENTATIONORDER + " TEXT,"
                    + COLUMN_FILETYPE + " TEXT"
                    + ");";
}