package com.garethevans.church.opensongtablet.sqlite;

// This class is used to build the song database and then query it for searches
public class SQLite {

    public static final String DATABASE_NAME = "Songs.db";
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
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_TIMESIG = "timesig";
    public static final String COLUMN_AKA = "aka";

    private int id;
    private String songid;
    private String filename;
    private String folder;
    private String title;
    private String author;
    private String copyright;
    private String lyrics;
    private String hymn_num;
    private String ccli;
    private String theme;
    private String alttheme;
    private String user1;
    private String user2;
    private String user3;
    private String key;
    private String timesig;
    private String aka;

    public SQLite() {

    }

    SQLite(int id, String songid, String filename, String folder, String title, String author,
           String copyright, String lyrics, String hymn_num, String ccli, String theme,
           String alttheme, String user1, String user2, String user3, String key, String timesig,
           String aka) {
        this.id = id;
        this.songid = songid;
        this.filename = filename;
        this.folder = folder;
        this.title = title;
        this.author = author;
        this.copyright = copyright;
        this.lyrics = lyrics;
        this.hymn_num = hymn_num;
        this.ccli = ccli;
        this.theme = theme;
        this.alttheme = alttheme;
        this.user1 = user1;
        this.user2 = user2;
        this.user3 = user3;
        this.key = key;
        this.aka = aka;
        this.timesig = timesig;
    }

    public int getId() {
        return id;
    }
    String getSongid() {
        return songid;
    }
    public String getFilename() {
        return filename;
    }
    public String getFolder() {
        return folder;
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    String getCopyright() {
        return copyright;
    }
    String getLyrics() {
        return lyrics;
    }
    String getHymn_num() {
        return hymn_num;
    }
    String getCcli() {
        return ccli;
    }
    String getTheme() {
        return theme;
    }
    String getAlttheme() {
        return alttheme;
    }
    String getUser1() {
        return user1;
    }
    String getUser2() {
        return user2;
    }
    String getUser3() {
        return user3;
    }
    public String getKey() {
        return key;
    }
    String getTimesig() {return timesig;}
    String getAka() {
        return aka;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setSongid(String songid) {
        this.songid = songid;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }
    public void setHymn_num(String hymn_num) {
        this.hymn_num = hymn_num;
    }
    public void setCcli(String ccli) {
        this.ccli = ccli;
    }
    public void setTheme(String theme) {
        this.theme = theme;
    }
    public void setAlttheme(String alttheme) {
        this.alttheme = alttheme;
    }
    public void setUser1(String user1) {
        this.user1 = user1;
    }
    public void setUser2(String user2) {
        this.user2 = user2;
    }
    public void setUser3(String user3) {
        this.user3 = user3;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setTimesig(String timesig) {this.timesig = timesig;}
    public void setAka(String aka) {
        this.aka = aka;
    }

    // Create table SQL query - only including fields which are searchable or used in the song index
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
                    + COLUMN_KEY + " TEXT,"
                    + COLUMN_TIMESIG + " TEXT,"
                    + COLUMN_AKA + " TEXT"
                    + ");";
}
