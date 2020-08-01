package com.garethevans.church.opensongtablet.sqlite;

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
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_TIMESIG = "timesig";
    public static final String COLUMN_AKA = "aka";
    public static final String COLUMN_AUTOSCROLL_DELAY = "autoscrolldelay";
    public static final String COLUMN_AUTOSCROLL_LENGTH = "autoscrolllength";
    public static final String COLUMN_METRONOME_BPM = "metronomebpm";
    public static final String COLUMN_PAD_FILE = "padfile";
    public static final String COLUMN_PAD_LOOP = "padloop";
    public static final String COLUMN_MIDI = "midi";
    public static final String COLUMN_MIDI_INDEX = "midiindex";
    public static final String COLUMN_CAPO = "capo";
    public static final String COLUMN_CUSTOM_CHORDS = "customchords";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_ABC = "abc";
    public static final String COLUMN_LINK_YOUTUBE = "linkyoutube";
    public static final String COLUMN_LINK_WEB = "linkweb";
    public static final String COLUMN_LINK_AUDIO = "linkaudio";
    public static final String COLUMN_LINK_OTHER = "linkother";
    public static final String COLUMN_PRESENTATIONORDER = "presentationorder";
    public static final String COLUMN_FILETYPE = "filetype";




    // The holders
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
    private String autoscrolldelay;
    private String autoscrolllength;
    private String metronomebpm;
    private String padfile;
    private String padloop;
    private String midi;
    private String midiindex;
    private String capo;
    private String customchords;
    private String notes;
    private String abc;
    private String linkyoutube;
    private String linkweb;
    private String linkaudio;
    private String linkother;
    private String presentationorder;
    private String filetype;


    // The getters
    public int getId() {
        return id;
    }
    public String getSongid() {
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
    public String getCopyright() {
        return copyright;
    }
    public String getLyrics() {
        return lyrics;
    }
    public String getHymn_num() {
        return hymn_num;
    }
    public String getCcli() {
        return ccli;
    }
    public String getTheme() {
        return theme;
    }
    public String getAlttheme() {
        return alttheme;
    }
    public String getUser1() {
        return user1;
    }
    public String getUser2() {
        return user2;
    }
    public String getUser3() {
        return user3;
    }
    public String getKey() {
        return key;
    }
    public String getTimesig() {return timesig;}
    public String getAka() {
        return aka;
    }
    public String getAutoscrolldelay() {return autoscrolldelay;}
    public String getAutoscrolllength() {return autoscrolllength;}
    public String getMetronomebpm() {return metronomebpm;}
    public String getPadfile() {return padfile;}
    public String getPadloop() {return padloop;}
    public String getMidi() {return midi;}
    public String getMidiindex() {return midiindex;}
    public String getCapo() {return capo;}
    public String getCustomchords() {return customchords;}
    public String getNotes() {return notes;}
    public String getAbc() {return abc;}
    public String getLinkyoutube() {return linkyoutube;}
    public String getLinkWeb() {return linkweb;}
    public String getLinkaudio() {return linkaudio;}
    public String getLinkother() {return linkother;}
    public String getPresentationorder() {return presentationorder;}
    public String getFiletype() {return filetype;}



    // The setters
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
    public void setAutoscrolldelay(String autoscrolldelay) {this.autoscrolldelay = autoscrolldelay;}
    public void setAutoscrolllength(String autoscrolllength) {this.autoscrolllength = autoscrolllength;}
    public void setMetronomebpm(String metronomebpm) {this.metronomebpm  = metronomebpm;}
    public void setPadfile(String padfile) {this.padfile = padfile;}
    public void setPadloop(String padloop) {this.padloop = padloop;}
    public void setMidi(String midi) {this.midi = midi;}
    public void setMidiindex(String midiindex) {this.midiindex = midiindex;}
    public void setCapo(String capo) {this.capo = capo;}
    public void setCustomChords(String customchords) {this.customchords = customchords;}
    public void setNotes(String notes) {this.notes = notes;}
    public void setAbc(String abc) {this.abc = abc;}
    public void setLinkyoutube(String linkyoutube) {this.linkyoutube = linkyoutube;}
    public void setLinkWeb(String linkweb) {this.linkweb = linkweb;}
    public void setLinkaudio(String linkaudio) {this.linkaudio = linkaudio;}
    public void setLinkother(String linkother) {this.linkother = linkother;}
    public void setPresentationorder(String presentationorder) {this.presentationorder = presentationorder;}
    public void setFiletype(String filetype) {this.filetype = filetype;}


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
                    + COLUMN_KEY + " TEXT,"
                    + COLUMN_TIMESIG + " TEXT,"
                    + COLUMN_AKA + " TEXT,"
                    + COLUMN_AUTOSCROLL_DELAY + " TEXT,"
                    + COLUMN_AUTOSCROLL_LENGTH + " TEXT,"
                    + COLUMN_METRONOME_BPM + " TEXT,"
                    + COLUMN_METRONOME_BPM + " TEXT,"
                    + COLUMN_PAD_FILE + " TEXT,"
                    + COLUMN_PAD_LOOP + " TEXT,"
                    + COLUMN_MIDI + " TEXT,"
                    + COLUMN_MIDI_INDEX + " TEXT,"
                    + COLUMN_CAPO + " TEXT,"
                    + COLUMN_CUSTOM_CHORDS + " TEXT,"
                    + COLUMN_NOTES + " TEXT,"
                    + COLUMN_ABC + " TEXT,"
                    + COLUMN_LINK_YOUTUBE + " TEXT,"
                    + COLUMN_LINK_WEB + " TEXT,"
                    + COLUMN_LINK_AUDIO + " TEXT,"
                    + COLUMN_LINK_OTHER + " TEXT,"
                    + COLUMN_PRESENTATIONORDER + " TEXT,"
                    + COLUMN_FILETYPE + " TEXT"
                    + ");";
}
