/*
package com.garethevans.church.opensongtablet.OLD_TO_DELETE;

// This class is used to build the song database and then query it for searches
public class _SQLite {

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
    public static final String COLUMN_INSET = "inset";

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
    private String inset;

    public _SQLite() {

    }

    _SQLite(int id, String songid, String filename, String folder, String title, String author,
            String copyright, String lyrics, String hymn_num, String ccli, String theme,
            String alttheme, String user1, String user2, String user3, String key, String timesig,
            String aka, String inset) {
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
        this.inset = inset;
    }

    int getId() {
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
    public String getInSet() {return inset;}

    void setId(int id) {
        this.id = id;
    }
    void setSongid(String songid) {
        this.songid = songid;
    }
    void setFilename(String filename) {
        this.filename = filename;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    void setAuthor(String author) {
        this.author = author;
    }
    void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }
    void setHymn_num(String hymn_num) {
        this.hymn_num = hymn_num;
    }
    void setCcli(String ccli) {
        this.ccli = ccli;
    }
    void setTheme(String theme) {
        this.theme = theme;
    }
    void setAlttheme(String alttheme) {
        this.alttheme = alttheme;
    }
    void setUser1(String user1) {
        this.user1 = user1;
    }
    void setUser2(String user2) {
        this.user2 = user2;
    }
    void setUser3(String user3) {
        this.user3 = user3;
    }
    void setKey(String key) {
        this.key = key;
    }
    void setTimesig(String timesig) {this.timesig = timesig;}
    void setAka(String aka) {
        this.aka = aka;
    }
    public void setInSet(String inset) {
        this.inset = inset;
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
                    + COLUMN_AKA + " TEXT,"
                    + COLUMN_INSET + " TEXT"
                    + ");";


}
*/
