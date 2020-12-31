package com.garethevans.church.opensongtablet.songprocessing;

// This is the song object that links the XML file to the database
// Used whenever the app queries or works with a lyrics, key, author, etc.


import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;

import java.util.ArrayList;

public class Song {

    // The holders
    private int id;
    private String songid="";
    private String filename="";
    private String folder="";
    private String title="";
    private String author="";
    private String copyright="";
    private String lyrics="";
    private String hymnnum="";
    private String ccli="";
    private String theme="";
    private String alttheme="";
    private String user1="";
    private String user2="";
    private String user3="";
    private String key="";
    private String timesig="";
    private String aka="";
    private String autoscrolldelay="";
    private String autoscrolllength="";
    private String metronomebpm="";
    private String padfile="";
    private String padloop="";
    private String midi="";
    private String midiindex="";
    private String capo="";
    private String capoprint="";
    private String customchords="";
    private String notes="";
    private String abc="";
    private String linkyoutube="";
    private String linkweb="";
    private String linkaudio="";
    private String linkother="";
    private String presentationorder="";
    private String filetype="";

    private ArrayList<String> songSections;
    private ArrayList<String> songSectionTypes;
    private int currentSection;

    private boolean isSong;
    private boolean isPDF;
    private boolean isImage;
    private String nextDirection = "R2L";
    public int pdfPageCurrent;

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
    public String getHymnnum() {
        return hymnnum;
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
    public String getCapoprint() {return capoprint;}
    public String getCustomchords() {return customchords;}
    public String getNotes() {return notes;}
    public String getAbc() {return abc;}
    public String getLinkyoutube() {return linkyoutube;}
    public String getLinkweb() {return linkweb;}
    public String getLinkaudio() {return linkaudio;}
    public String getLinkother() {return linkother;}
    public String getPresentationorder() {return presentationorder;}
    public String getFiletype() {return filetype;}
    public ArrayList<String> getSongSections() {
        if (songSections!=null) {
            return songSections;
        } else {
            return new ArrayList<>();
        }
    }
    public ArrayList<String> getSongSectionTypes() {
        if (songSectionTypes!=null) {
            return songSectionTypes;
        } else {
            return new ArrayList<>();
        }
    }
    public boolean getIsSong() {return isSong;}
    public boolean getIsPDF() {return isPDF;}
    public boolean getIsImage() {return isImage;}
    public String getNextDirection() {return nextDirection;}
    public int getPdfPageCurrent() {return pdfPageCurrent;}
    public int getCurrentSection() {return currentSection;}

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
    public void setHymnnum(String hymnnum) {
        this.hymnnum = hymnnum;
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
    public void setCapoprint(String capoprint) {this.capoprint = capoprint;}
    public void setCustomChords(String customchords) {this.customchords = customchords;}
    public void setNotes(String notes) {this.notes = notes;}
    public void setAbc(String abc) {this.abc = abc;}
    public void setLinkyoutube(String linkyoutube) {this.linkyoutube = linkyoutube;}
    public void setLinkweb(String linkweb) {this.linkweb = linkweb;}
    public void setLinkaudio(String linkaudio) {this.linkaudio = linkaudio;}
    public void setLinkother(String linkother) {this.linkother = linkother;}
    public void setPresentationorder(String presentationorder) {this.presentationorder = presentationorder;}
    public void setFiletype(String filetype) {this.filetype = filetype;}
    public void setSongSections(ArrayList<String> songSections) {
        this.songSections = songSections;
    }
    public void setSongSectionTypes(ArrayList<String> songSectionTypes) {
        this.songSectionTypes = songSectionTypes;
    }
    public void setIsSong(boolean isSong) {this.isSong = isSong;}
    public void setIsPDF(boolean isPDF) {this.isPDF = isPDF;}
    public void setIsImage(boolean isImage) {this.isSong = isImage;}
    public void setNextDirection(String nextDirection) {this.nextDirection = nextDirection;}
    public void setPdfPageCurrent(int pdfPageCurrent) {this.pdfPageCurrent = pdfPageCurrent;}
    public void setCurrentSection(int currentSection) {this.currentSection = currentSection;}

    public Song initialiseSong(CommonSQL commonSQL) {
        Song song = new Song();
        song.setFilename(StaticVariables.songfilename);
        song.setFolder(StaticVariables.whichSongFolder);
        song.setSongid(commonSQL.getAnySongId(StaticVariables.whichSongFolder,StaticVariables.songfilename));
        return song;
    }

    public Song() {}

    // The copy constructor (for creating a copy of the song
    public Song(Song toCopy) {
        this.id = toCopy.id;
        this.songid = toCopy.songid;
        this.filename = toCopy.filename;
        this.folder = toCopy.folder;
        this.title = toCopy.title;
        this.author = toCopy.author;
        this.copyright = toCopy.copyright;
        this.lyrics = toCopy.lyrics;
        this.hymnnum = toCopy.hymnnum;
        this.ccli = toCopy.ccli;
        this.theme = toCopy.theme;
        this.alttheme = toCopy.alttheme;
        this.user1 = toCopy.user1;
        this.user2 = toCopy.user2;
        this.user3 = toCopy.user3;
        this.key = toCopy.key;
        this.timesig = toCopy.timesig;
        this.aka = toCopy.aka;
        this.autoscrolldelay = toCopy.autoscrolldelay;
        this.autoscrolllength = toCopy.autoscrolllength;
        this.metronomebpm = toCopy.metronomebpm;
        this.padfile = toCopy.padfile;
        this.padloop = toCopy.padloop;
        this.midi = toCopy.midi;
        this.midiindex = toCopy.midiindex;
        this.capo = toCopy.capo;
        this.capoprint = toCopy.capoprint;
        this.customchords = toCopy.customchords;
        this.notes = toCopy.notes;
        this.abc = toCopy.abc;
        this.linkyoutube = toCopy.linkyoutube;
        this.linkweb = toCopy.linkweb;
        this.linkaudio = toCopy.linkaudio;
        this.linkother = toCopy.linkother;
        this.presentationorder = toCopy.presentationorder;
        this.filetype = toCopy.filetype;
        this.isSong = toCopy.isSong;
        this.isPDF = toCopy.isPDF;
        this.isImage = toCopy.isImage;
        this.pdfPageCurrent = toCopy.pdfPageCurrent;
        this.nextDirection = toCopy.nextDirection;
        this.songSections = toCopy.songSections;
        this.songSectionTypes = toCopy.songSectionTypes;
        this.currentSection = toCopy.currentSection;
    }

    // This is used when comparing song objects for changes (when editing a song)
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false; // Different object types or null
        } else {
            Song song = (Song) obj;
            return id == song.id && songid.equals(song.songid) && filename.equals(song.filename) &&
                    folder.equals(song.folder) && title.equals(song.title) && author.equals(song.author) &&
                    copyright.equals(song.copyright) && lyrics.equals(song.lyrics) && hymnnum.equals(song.hymnnum) &&
                    ccli.equals(song.ccli) && theme.equals(song.theme) && alttheme.equals(song.alttheme) &&
                    user1.equals(song.user1) && user2.equals(song.user2) && user3.equals(song.user3) &&
                    key.equals(song.key) && timesig.equals(song.timesig) && aka.equals(song.aka) &&
                    autoscrolldelay.equals(song.autoscrolldelay) && autoscrolllength.equals(song.autoscrolllength) &&
                    metronomebpm.equals(song.metronomebpm) && padfile.equals(song.padfile) &&
                    padloop.equals(song.padloop) && midi.equals(song.midi) && midiindex.equals(song.midiindex) &&
                    capo.equals(song.capo) && capoprint.equals(song.capoprint) && customchords.equals(song.customchords) &&
                    notes.equals(song.notes) && abc.equals(song.abc) && linkyoutube.equals(song.linkyoutube) &&
                    linkweb.equals(song.linkweb) && linkaudio.equals(song.linkaudio) &&
                    linkother.equals(song.linkother) && presentationorder.equals(song.presentationorder) &&
                    filetype.equals(song.filetype);
        }
    }

    // This deals with the song XML file
    public String getXML(Song song, ProcessSong processSong) {
        if (StaticVariables.mEncoding==null || StaticVariables.mEncoding.equals("")) {
            StaticVariables.mEncoding = "UTF-8";
        }
        String myNEWXML = "<?xml version=\"1.0\" encoding=\""+ StaticVariables.mEncoding+"\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + processSong.parseToHTMLEntities(song.getTitle()) + "</title>\n";
        myNEWXML += "  <author>" + processSong.parseToHTMLEntities(song.getAuthor()) + "</author>\n";
        myNEWXML += "  <copyright>" + processSong.parseToHTMLEntities(song.getCopyright()) + "</copyright>\n";
        myNEWXML += "  <presentation>" + processSong.parseToHTMLEntities(song.getPresentationorder()) + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + processSong.parseToHTMLEntities(song.getHymnnum()) + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + processSong.parseToHTMLEntities(song.getCapoprint()) + "\">" +
                processSong.parseToHTMLEntities(song.getCapo()) + "</capo>\n";
        myNEWXML += "  <tempo>" + processSong.parseToHTMLEntities(song.getMetronomebpm()) + "</tempo>\n";
        myNEWXML += "  <time_sig>" + processSong.parseToHTMLEntities(song.getTimesig()) + "</time_sig>\n";
        myNEWXML += "  <duration>" + processSong.parseToHTMLEntities(song.getAutoscrolllength()) + "</duration>\n";
        myNEWXML += "  <predelay>" + processSong.parseToHTMLEntities(song.getAutoscrolldelay()) + "</predelay>\n";
        myNEWXML += "  <ccli>" + processSong.parseToHTMLEntities(song.getCcli()) + "</ccli>\n";
        myNEWXML += "  <theme>" + processSong.parseToHTMLEntities(song.getTheme()) + "</theme>\n";
        myNEWXML += "  <alttheme>" + processSong.parseToHTMLEntities(song.getAlttheme()) + "</alttheme>\n";
        myNEWXML += "  <user1>" + processSong.parseToHTMLEntities(song.getUser1()) + "</user1>\n";
        myNEWXML += "  <user2>" + processSong.parseToHTMLEntities(song.getUser2()) + "</user2>\n";
        myNEWXML += "  <user3>" + processSong.parseToHTMLEntities(song.getUser3()) + "</user3>\n";
        myNEWXML += "  <key>" + processSong.parseToHTMLEntities(song.getKey()) + "</key>\n";
        myNEWXML += "  <aka>" + processSong.parseToHTMLEntities(song.getAka()) + "</aka>\n";
        myNEWXML += "  <midi>" + processSong.parseToHTMLEntities(song.getMidi()) + "</midi>\n";
        myNEWXML += "  <midi_index>" + processSong.parseToHTMLEntities(song.getMidiindex()) + "</midi_index>\n";
        myNEWXML += "  <notes>" + processSong.parseToHTMLEntities(song.getNotes()) + "</notes>\n";
        myNEWXML += "  <lyrics>" + processSong.parseToHTMLEntities(song.getLyrics()) + "</lyrics>\n";
        myNEWXML += "  <pad_file>" + processSong.parseToHTMLEntities(song.getPadfile()) + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + processSong.parseToHTMLEntities(song.getCustomchords()) + "</custom_chords>\n";
        myNEWXML += "  <link_youtube>" + processSong.parseToHTMLEntities(song.getLinkyoutube()) + "</link_youtube>\n";
        myNEWXML += "  <link_web>" + processSong.parseToHTMLEntities(song.getLinkweb()) + "</link_web>\n";
        myNEWXML += "  <link_audio>" + processSong.parseToHTMLEntities(song.getLinkaudio()) + "</link_audio>\n";
        myNEWXML += "  <loop_audio>" + processSong.parseToHTMLEntities(song.getPadloop()) + "</loop_audio>\n";
        myNEWXML += "  <link_other>" + processSong.parseToHTMLEntities(song.getLinkother()) + "</link_other>\n";
        myNEWXML += "  <abcnotation>" + processSong.parseToHTMLEntities(song.getAbc()) + "</abcnotation>\n";

        if (!StaticVariables.mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + StaticVariables.mExtraStuff1 + "\n";
        }
        if (!StaticVariables.mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + StaticVariables.mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

        return myNEWXML;
    }

    // The welcome song if there is a problem
    public Song showWelcomeSong(Context c) {
        Song song = new Song();
        StaticVariables.songfilename = "Welcome to OpenSongApp";
        song.setFilename("Welcome to OpenSongApp");
        song.setTitle("Welcome to OpenSongApp");
        song.setLyrics(c.getString(R.string.user_guide_lyrics));
        song.setAuthor("Gareth Evans");
        song.setKey("G");
        song.setLinkweb("https://www.opensongapp.com");
        return song;
    }

    // These are to deal with custom files (scriptures, etc.)
    public String getLocation (String string) {
        if (string.startsWith("../")) {
            return string.replace("../", "");
        } else {
            return "Songs";
        }
    }
    private static String getFolder(String string) {
        if (string.startsWith("../")) {
            return "";
        } else {
            return string;
        }
    }
}