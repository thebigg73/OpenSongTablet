package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

public class SongXML {

    /*public void initialiseSongTags() {
        StaticVariables.mTitle = StaticVariables.songfilename;
        StaticVariables.mAuthor = "";
        StaticVariables.mCopyright = "";
        StaticVariables.mPresentation = "";
        StaticVariables.mHymnNumber = "";
        StaticVariables.mCapo = "";
        StaticVariables.mCapoPrint = "false";
        StaticVariables.mCustomChords = "";
        StaticVariables.mTempo = "";
        StaticVariables.mTimeSig = "";
        StaticVariables.mDuration = "";
        StaticVariables.mPreDelay = "";
        StaticVariables.mCCLI = "";
        StaticVariables.mTheme = "";
        StaticVariables.mAltTheme = "";
        StaticVariables.mUser1 = "";
        StaticVariables.mUser2 = "";
        StaticVariables.mUser3 = "";
        StaticVariables.mKey = "";
        StaticVariables.mAka = "";
        StaticVariables.mMidi = "";
        StaticVariables.mMidiIndex = "";
        StaticVariables.mNotes = "";
        StaticVariables.mLyrics = "";
        StaticVariables.mNotation = "";
        StaticVariables.mPadFile = "";
        StaticVariables.mCustomChords = "";
        StaticVariables.mLinkYouTube = "";
        StaticVariables.mLinkWeb = "";
        StaticVariables.mLinkAudio = "";
        StaticVariables.mLoopAudio = "false";
        StaticVariables.mLinkOther = "";
        StaticVariables.mExtraStuff1 = "";
        StaticVariables.mExtraStuff2 = "";
        StaticVariables.mEncoding = "UTF-8";
        StaticVariables.mFileType = "XML";
    }*/

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
