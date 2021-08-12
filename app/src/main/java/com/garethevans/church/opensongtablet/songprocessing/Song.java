package com.garethevans.church.opensongtablet.songprocessing;

// This is the song object that links the XML file to the database
// Used whenever the app queries or works with a lyrics, key, author, etc.

import android.content.Context;

import com.garethevans.church.opensongtablet.R;

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
    private String tempo="";
    private String aka="";
    private String autoscrolldelay="";
    private String autoscrolllength="";
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
    private boolean hasExtraStuff;
    private String filetype="XML";
    private int detectedChordFormat=1;
    private String encoding="UTF-8";
    private ArrayList<String> songSections;
    private ArrayList<String> songSectionHeadings;
    private ArrayList<String> songSectionTypes;
    private int currentSection;
    private boolean isImageSlide;
    private String nextDirection = "R2L";
    private int pdfPageCurrent;
    private String songXML;
    private int halfSplit;
    private int thirdSplit;
    private int twoThirdSplit;
    private boolean scalingFiguredOut = false;

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
    public String getTempo() {
        return tempo;
    }
    public String getAka() {
        return aka;
    }
    public String getAutoscrolldelay() {return autoscrolldelay;}
    public String getAutoscrolllength() {return autoscrolllength;}
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
    public ArrayList<String> getSongSectionHeadings() {
        if (songSectionHeadings!=null) {
            return songSectionHeadings;
        } else {
            return new ArrayList<>();
        }
    }
    public boolean getIsImageSlide() {return isImageSlide;}
    public String getNextDirection() {return nextDirection;}
    public int getPdfPageCurrent() {return pdfPageCurrent;}
    public int getCurrentSection() {return currentSection;}
    public int getDetectedChordFormat() {
        return detectedChordFormat;
    }
    public String getEncoding() {
        return encoding;
    }
    public String getSongXML() {
        return songXML;
    }
    public boolean getHasExtraStuff() {
        return hasExtraStuff;
    }
    public int getHalfSplit() {
        return halfSplit;
    }
    public int getThirdSplit() {
        return thirdSplit;
    }
    public int getTwoThirdSplit() {
        return twoThirdSplit;
    }
    public boolean getScalingFiguredOut() {
        return scalingFiguredOut;
    }

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
    public void setTempo(String tempo) {
        this.tempo = tempo;
    }
    public void setAka(String aka) {
        this.aka = aka;
    }
    public void setAutoscrolldelay(String autoscrolldelay) {this.autoscrolldelay = autoscrolldelay;}
    public void setAutoscrolllength(String autoscrolllength) {this.autoscrolllength = autoscrolllength;}
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
    public void setHasExtraStuff(boolean hasExtraStuff) {
        this.hasExtraStuff = hasExtraStuff;
    }
    public void setFiletype(String filetype) {this.filetype = filetype;}
    public void setSongSections(ArrayList<String> songSections) {
        this.songSections = songSections;
    }
    public void setSongSectionTypes(ArrayList<String> songSectionTypes) {
        this.songSectionTypes = songSectionTypes;
    }
    public void setSongSectionHeadings(ArrayList<String> songSectionHeadings) {
        this.songSectionHeadings = songSectionHeadings;
    }
    public void setIsImageSlide(boolean isImageSlide) {this.isImageSlide = isImageSlide;}
    public void setNextDirection(String nextDirection) {this.nextDirection = nextDirection;}
    public void setPdfPageCurrent(int pdfPageCurrent) {this.pdfPageCurrent = pdfPageCurrent;}
    public void setCurrentSection(int currentSection) {this.currentSection = currentSection;}
    public void setDetectedChordFormat(int detectedChordFormat) {
        this.detectedChordFormat = detectedChordFormat;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public void setSongXML(String songXML) {
        this.songXML = songXML;
    }
    public void setHalfSplit(int halfSplit) {
        this.halfSplit = halfSplit;
    }
    public void setThirdSplit(int thirdSplit) {
        this.thirdSplit = thirdSplit;
    }
    public void setTwoThirdSplit(int twoThirdSplit) {
        this.twoThirdSplit = twoThirdSplit;
    }
    public void setScalingFiguredOut(boolean scalingFiguredOut) {
        this.scalingFiguredOut = scalingFiguredOut;
    }


    // The constructor to create a new song object
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
        this.tempo = toCopy.tempo;
        this.aka = toCopy.aka;
        this.autoscrolldelay = toCopy.autoscrolldelay;
        this.autoscrolllength = toCopy.autoscrolllength;
        this.tempo = toCopy.tempo;
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

        this.hasExtraStuff = toCopy.hasExtraStuff;
        this.filetype = toCopy.filetype;
        this.detectedChordFormat = toCopy.detectedChordFormat;
        this.encoding = toCopy.encoding;
        this.songSections = toCopy.songSections;
        this.songSectionHeadings = toCopy.songSectionHeadings;
        this.songSectionTypes = toCopy.songSectionTypes;
        this.currentSection = toCopy.currentSection;
        this.isImageSlide = toCopy.isImageSlide;
        this.nextDirection = toCopy.nextDirection;
        this.pdfPageCurrent = toCopy.pdfPageCurrent;
        this.presentationorder = toCopy.presentationorder;
        this.songXML = toCopy.songXML;
        this.halfSplit = toCopy.halfSplit;
        this.thirdSplit = toCopy.thirdSplit;
        this.twoThirdSplit = toCopy.twoThirdSplit;
        this.scalingFiguredOut = toCopy.scalingFiguredOut;
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
                    tempo.equals(song.tempo) && padfile.equals(song.padfile) &&
                    padloop.equals(song.padloop) && midi.equals(song.midi) && midiindex.equals(song.midiindex) &&
                    capo.equals(song.capo) && capoprint.equals(song.capoprint) && customchords.equals(song.customchords) &&
                    notes.equals(song.notes) && abc.equals(song.abc) && linkyoutube.equals(song.linkyoutube) &&
                    linkweb.equals(song.linkweb) && linkaudio.equals(song.linkaudio) &&
                    linkother.equals(song.linkother) && presentationorder.equals(song.presentationorder) &&
                    filetype.equals(song.filetype);
        }
    }


    // The welcome song if there is a problem
    public Song showWelcomeSong(Context c, Song thisSong) {
        thisSong.setFilename("Welcome to OpenSongApp");
        thisSong.setTitle(c.getString(R.string.welcome));
        thisSong.setLyrics(c.getString(R.string.user_guide_lyrics));
        thisSong.setAuthor("Gareth Evans");
        thisSong.setKey("G");
        thisSong.setLinkweb("https://www.opensongapp.com");
        thisSong.setFiletype("XML");
        return thisSong;
    }

    public String getFolderNamePair() {
        return folder+"/"+filename;
    }

}