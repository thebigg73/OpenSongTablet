package com.garethevans.church.opensongtablet;

// This class is used to build the song database and then query it for searches
class NonOpenSongSQLite {

    static final String DATABASE_NAME = "NonOpenSongSongs.db";
    static final String TABLE_NAME = "pdfsongs";
    static final String COLUMN_ID = "id";
    static final String COLUMN_SONGID = "songid";
    static final String COLUMN_FILENAME = "filename";
    static final String COLUMN_FOLDER = "folder";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_AUTHOR = "author";
    static final String COLUMN_COPYRIGHT = "copyright";
    static final String COLUMN_LYRICS = "lyrics";
    static final String COLUMN_HYMNNUM = "hymn_num";
    static final String COLUMN_CCLI = "ccli";
    static final String COLUMN_THEME = "theme";
    static final String COLUMN_ALTTHEME = "alttheme";
    static final String COLUMN_USER1 = "user1";
    static final String COLUMN_USER2 = "user2";
    static final String COLUMN_USER3 = "user3";
    static final String COLUMN_KEY = "key";
    static final String COLUMN_AKA = "aka";
    static final String COLUMN_AUTOSCROLL_DELAY = "autoscrolldelay";
    static final String COLUMN_AUTOSCROLL_LENGTH = "autoscrolllength";
    static final String COLUMN_METRONOME_BPM = "metronomebpm";
    static final String COLUMN_METRONOME_SIG = "metronomesig";
    static final String COLUMN_PAD_FILE = "padfile";
    static final String COLUMN_MIDI = "midi";
    static final String COLUMN_MIDI_INDEX = "midiindex";
    static final String COLUMN_CAPO = "capo";
    static final String COLUMN_NOTES = "notes";
    static final String COLUMN_ABC = "abc";
    static final String COLUMN_LINK_YOUTUBE = "linkyoutube";
    static final String COLUMN_LINK_WEB = "linkweb";
    static final String COLUMN_LINK_AUDIO = "linkaudio";
    static final String COLUMN_LINK_OTHER = "linkother";
    static final String COLUMN_PRESENTATIONORDER = "presentationorder";

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
    private String aka;
    private String autoscrolldelay;
    private String autoscrolllength;
    private String metronomebpm;
    private String metronomesig;
    private String padfile;
    private String midi;
    private String midiindex;
    private String capo;
    private String notes;
    private String abc;
    private String linkyoutube;
    private String linkweb;
    private String linkaudio;
    private String linkother;
    private String presentationorder;

    NonOpenSongSQLite() {}

    NonOpenSongSQLite(int id, String songid, String filename, String folder, String title, String author,
                      String copyright, String lyrics, String hymn_num, String ccli, String theme,
                      String alttheme, String user1, String user2, String user3, String key, String aka,
                      String autoscrolldelay, String autoscrolllength, String metronomebpm, String metronomesig,
                      String padfile, String midi, String midiindex, String capo, String notes, String abc,
                      String linkyoutube, String linkweb, String linkaudio, String linkother, String presentationorder) {
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
        this.autoscrolldelay = autoscrolldelay;
        this.autoscrolllength = autoscrolllength;
        this.metronomebpm = metronomebpm;
        this.metronomesig = metronomesig;
        this.padfile = padfile;
        this.midi = midi;
        this.midiindex = midiindex;
        this.capo = capo;
        this.notes = notes;
        this.abc = abc;
        this.linkyoutube = linkyoutube;
        this.linkweb = linkweb;
        this.linkaudio = linkaudio;
        this.linkother = linkother;
        this.presentationorder = presentationorder;
    }

    int getId() {
        return id;
    }
    String getSongid() {
        return songid;
    }
    String getFilename() {
        return filename;
    }
    String getFolder() {
        return folder;
    }
    String getTitle() {
        return title;
    }
    String getAuthor() {
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
    String getKey() {
        return key;
    }
    String getAka() {
        return aka;
    }
    String getAutoscrolldelay() {
        return autoscrolldelay;
    }
    String getAutoscrollLength() {
        return autoscrolllength;
    }
    String getMetronomebpm() {
        return metronomebpm;
    }
    String getMetronomeSig() {
        return metronomesig;
    }
    String getPadfile() {return padfile;}
    String getMidi() {return midi;}
    String getMidiindex() {return midiindex;}
    String getCapo() {return capo;}
    String getNotes() {return notes;}
    String getAbc() {return abc;}
    String getLinkyoutube() {return linkyoutube;}
    String getLinkweb() {return linkweb;}
    String getLinkaudio() {return linkaudio;}
    String getLinkother() {return linkother;}
    String getPresentationorder() {return presentationorder;}

    void setId(int id) {
        this.id = id;
    }
    void setSongid(String songid) {
        this.songid = songid;
    }
    void setFilename(String filename) {
        this.filename = filename;
    }
    void setFolder(String folder) {
        this.folder = folder;
    }
    void setTitle(String title) {
        this.title = title;
    }
    void setAuthor(String author) {
        this.author = author;
    }
    void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    void setLyrics(String lyrics) {
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
    void setAka(String aka) {
        this.aka = aka;
    }
    void setAutoscrolldelay(String autoscrolldelay) {
        this.autoscrolldelay = autoscrolldelay;
    }
    void setAutoscrolllength(String autoscrolllength) {
        this.autoscrolllength = autoscrolllength;
    }
    void setMetronomebpm(String metronomebpm) {
        this.metronomebpm = metronomebpm;
    }
    void setMetronomeSig(String metronomesig) {
        this.metronomesig = metronomesig;
    }
    void setPadfile(String padfile) {this.padfile = padfile;}
    void setMidi(String midi) {this.midi = midi;}
    void setMidiindex(String midiindex) {this.midiindex=midiindex;}
    void setCapo(String capo) {this.capo=capo;}
    void setNotes(String notes) {this.notes=notes;}
    void setAbc(String abc) {this.abc=abc;}
    void setLinkyoutube(String linkyoutube) {this.linkyoutube=linkyoutube;}
    void setLinkweb(String linkweb) {this.linkweb=linkweb;}
    void setLinkaudio(String linkaudio) {this.linkaudio=linkaudio;}
    void setLinkother(String linkother) {this.linkother=linkother;}
    void setPresentationorder(String presentationorder) {this.presentationorder=presentationorder;}

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
                    + COLUMN_AKA + " TEXT,"
                    + COLUMN_AUTOSCROLL_DELAY + " TEXT,"
                    + COLUMN_AUTOSCROLL_LENGTH + " TEXT,"
                    + COLUMN_METRONOME_BPM + " TEXT,"
                    + COLUMN_METRONOME_SIG + " TEXT,"
                    + COLUMN_PAD_FILE + " TEXT,"
                    + COLUMN_MIDI + " TEXT,"
                    + COLUMN_MIDI_INDEX + " TEXT,"
                    + COLUMN_CAPO + " TEXT,"
                    + COLUMN_NOTES + " TEXT,"
                    + COLUMN_ABC + " TEXT,"
                    + COLUMN_LINK_YOUTUBE + " TEXT,"
                    + COLUMN_LINK_WEB + " TEXT,"
                    + COLUMN_LINK_AUDIO + " TEXT,"
                    + COLUMN_LINK_OTHER + " TEXT,"
                    + COLUMN_PRESENTATIONORDER
                    + ");";

}