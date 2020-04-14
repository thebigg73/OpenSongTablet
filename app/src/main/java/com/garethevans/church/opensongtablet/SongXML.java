package com.garethevans.church.opensongtablet;

public class SongXML {
    // This class is used to build or initialise the song XML

    void initialiseSongTags() {
        StaticVariables.mTitle = StaticVariables.songfilename;
        StaticVariables.mAuthor = "";
        StaticVariables.mCopyright = "";
        StaticVariables.mPresentation = "";
        StaticVariables.mHymnNumber = "";
        StaticVariables.mCapo = "";
        StaticVariables.mCapoPrint = "false";
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
        StaticVariables.mKeyLine = "";
        StaticVariables.mBooks = "";
        StaticVariables.mMidi = "";
        StaticVariables.mMidiIndex = "";
        StaticVariables.mPitch = "";
        StaticVariables.mRestrictions = "";
        StaticVariables.mNotes = "";
        StaticVariables.mLyrics = "";
        StaticVariables.mStyle = "";
        StaticVariables.mLinkedSongs = "";
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
    }

    String prepareBlankSongXML() {
        // Prepare the new XML file
        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + StaticVariables.songfilename + "</title>\n";
        myNEWXML += "  <author></author>\n";
        myNEWXML += "  <copyright></copyright>\n";
        myNEWXML += "  <presentation></presentation>\n";
        myNEWXML += "  <hymn_number></hymn_number>\n";
        myNEWXML += "  <capo print=\"\"></capo>\n";
        myNEWXML += "  <tempo></tempo>\n";
        myNEWXML += "  <time_sig></time_sig>\n";
        myNEWXML += "  <duration></duration>\n";
        myNEWXML += "  <predelay></predelay>\n";
        myNEWXML += "  <ccli></ccli>\n";
        myNEWXML += "  <theme></theme>\n";
        myNEWXML += "  <alttheme></alttheme>\n";
        myNEWXML += "  <user1></user1>\n";
        myNEWXML += "  <user2></user2>\n";
        myNEWXML += "  <user3></user3>\n";
        myNEWXML += "  <key></key>\n";
        myNEWXML += "  <aka></aka>\n";
        myNEWXML += "  <key_line></key_line>\n";
        myNEWXML += "  <books></books>\n";
        myNEWXML += "  <midi></midi>\n";
        myNEWXML += "  <midi_index></midi_index>\n";
        myNEWXML += "  <pitch></pitch>\n";
        myNEWXML += "  <restrictions></restrictions>\n";
        myNEWXML += "  <notes></notes>\n";
        myNEWXML += "  <lyrics>[V]\n</lyrics>\n";
        myNEWXML += "  <linked_songs></linked_songs>\n";
        myNEWXML += "  <pad_file></pad_file>\n";
        myNEWXML += "  <custom_chords></custom_chords>\n";
        myNEWXML += "  <link_youtube></link_youtube>\n";
        myNEWXML += "  <link_web></link_web>\n";
        myNEWXML += "  <link_audio></link_audio>\n";
        myNEWXML += "  <loop_audio>false</loop_audio>\n";
        myNEWXML += "  <link_other></link_other>\n";
        myNEWXML += "  <abcnotation></abcnotation>\n";
        myNEWXML += "</song>";
        return myNEWXML;
    }

    String getXML() {
        if (StaticVariables.mEncoding==null || StaticVariables.mEncoding.equals("")) {
            StaticVariables.mEncoding = "UTF-8";
        }
        String myNEWXML = "<?xml version=\"1.0\" encoding=\""+ StaticVariables.mEncoding+"\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + parseToHTMLEntities(StaticVariables.mTitle) + "</title>\n";
        myNEWXML += "  <author>" + parseToHTMLEntities(StaticVariables.mAuthor) + "</author>\n";
        myNEWXML += "  <copyright>" + parseToHTMLEntities(StaticVariables.mCopyright) + "</copyright>\n";
        myNEWXML += "  <presentation>" + parseToHTMLEntities(StaticVariables.mPresentation) + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + parseToHTMLEntities(StaticVariables.mHymnNumber) + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + parseToHTMLEntities(StaticVariables.mCapoPrint) + "\">" + parseToHTMLEntities(StaticVariables.mCapo) + "</capo>\n";
        myNEWXML += "  <tempo>" + parseToHTMLEntities(StaticVariables.mTempo) + "</tempo>\n";
        myNEWXML += "  <time_sig>" + parseToHTMLEntities(StaticVariables.mTimeSig) + "</time_sig>\n";
        myNEWXML += "  <duration>" + parseToHTMLEntities(StaticVariables.mDuration) + "</duration>\n";
        myNEWXML += "  <predelay>" + parseToHTMLEntities(StaticVariables.mPreDelay) + "</predelay>\n";
        myNEWXML += "  <ccli>" + parseToHTMLEntities(StaticVariables.mCCLI) + "</ccli>\n";
        myNEWXML += "  <theme>" + parseToHTMLEntities(StaticVariables.mTheme) + "</theme>\n";
        myNEWXML += "  <alttheme>" + parseToHTMLEntities(StaticVariables.mAltTheme) + "</alttheme>\n";
        myNEWXML += "  <user1>" + parseToHTMLEntities(StaticVariables.mUser1) + "</user1>\n";
        myNEWXML += "  <user2>" + parseToHTMLEntities(StaticVariables.mUser2) + "</user2>\n";
        myNEWXML += "  <user3>" + parseToHTMLEntities(StaticVariables.mUser3) + "</user3>\n";
        myNEWXML += "  <key>" + parseToHTMLEntities(StaticVariables.mKey) + "</key>\n";
        myNEWXML += "  <aka>" + parseToHTMLEntities(StaticVariables.mAka) + "</aka>\n";
        myNEWXML += "  <key_line>" + parseToHTMLEntities(StaticVariables.mKeyLine) + "</key_line>\n";
        myNEWXML += "  <books>" + parseToHTMLEntities(StaticVariables.mBooks) + "</books>\n";
        myNEWXML += "  <midi>" + parseToHTMLEntities(StaticVariables.mMidi) + "</midi>\n";
        myNEWXML += "  <midi_index>" + parseToHTMLEntities(StaticVariables.mMidiIndex) + "</midi_index>\n";
        myNEWXML += "  <pitch>" + parseToHTMLEntities(StaticVariables.mPitch) + "</pitch>\n";
        myNEWXML += "  <restrictions>" + parseToHTMLEntities(StaticVariables.mRestrictions) + "</restrictions>\n";
        myNEWXML += "  <notes>" + parseToHTMLEntities(StaticVariables.mNotes) + "</notes>\n";
        myNEWXML += "  <lyrics>" + parseToHTMLEntities(StaticVariables.mLyrics) + "</lyrics>\n";
        myNEWXML += "  <linked_songs>" + parseToHTMLEntities(StaticVariables.mLinkedSongs) + "</linked_songs>\n";
        myNEWXML += "  <pad_file>" + parseToHTMLEntities(StaticVariables.mPadFile) + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + parseToHTMLEntities(StaticVariables.mCustomChords) + "</custom_chords>\n";
        myNEWXML += "  <link_youtube>" + parseToHTMLEntities(StaticVariables.mLinkYouTube) + "</link_youtube>\n";
        myNEWXML += "  <link_web>" + parseToHTMLEntities(StaticVariables.mLinkWeb) + "</link_web>\n";
        myNEWXML += "  <link_audio>" + parseToHTMLEntities(StaticVariables.mLinkAudio) + "</link_audio>\n";
        myNEWXML += "  <loop_audio>" + parseToHTMLEntities(StaticVariables.mLoopAudio) + "</loop_audio>\n";
        myNEWXML += "  <link_other>" + parseToHTMLEntities(StaticVariables.mLinkOther) + "</link_other>\n";
        myNEWXML += "  <abcnotation>" + parseToHTMLEntities(StaticVariables.mNotation) + "</abcnotation>\n";

        if (!StaticVariables.mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + StaticVariables.mExtraStuff1 + "\n";
        }
        if (!StaticVariables.mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + StaticVariables.mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

        FullscreenActivity.mynewXML = myNEWXML;

        return myNEWXML;
    }

    private String parseToHTMLEntities(String val) {
        if (val==null) {
            val = "";
        }
        // Make sure all vals are unencoded to start with
        // Now HTML encode everything that needs encoded
        // Protected are < > &
        // Change < to __lt;  We'll later replace the __ with &.  Do this to deal with &amp; separately
        val = val.replace("<","__lt;");
        val = val.replace("&lt;","__lt;");

        // Change > to __gt;  We'll later replace the __ with &.  Do this to deal with &amp; separately
        val = val.replace(">","__gt;");
        val = val.replace("&gt;","__gt;");

        // Change &apos; to ' as they don't need encoding in this format - also makes it compatible with desktop
        val = val.replace("&apos;","'");

        // Change " to __quot;  We'll later replace the __ with &.  Do this to deal with &amp; separately
        val = val.replace("\"","__quot;");
        val = val.replace("&quot;","__quot;");

        // Now deal with the remaining ampersands
        val = val.replace("&amp;","&");  // Reset any that already encoded - all need encoded now
        val = val.replace("&&","&");     // Just in case we have wrongly encoded old ones e.g. &amp;&quot;
        val = val.replace("&","&amp;");  // Reencode all remaining ampersands

        // Now replace the other protected encoded entities back with their leading ampersands
        val = val.replace("__lt;","&lt;");
        val = val.replace("__gt;","&gt;");
        val = val.replace("__quot;","&quot;");

        // Replace other weird characters
        val = val.replace("Õ","'");
        val = val.replace("Ó","'");
        val = val.replace("Ò","'");

        val = val.replace("&#39;", "'");
        val = val.replace("&#145", "'");
        val = val.replace("&#146;", "'");
        val = val.replace("&#147;", "'");
        val = val.replace("&#148;", "'");
        val = val.replace("тАЩ", "'");
        val = val.replace("\u0028", "'");
        val = val.replace("\u0029", "'");
        val = val.replace("\u0060", "'");
        val = val.replace("\u00B4", "'");
        val = val.replace("\u2018", "'");
        val = val.replace("\u2019", "'");
        val = val.replace("\u0211", "'");
        val = val.replace("\u0212", "'");
        val = val.replace("\u0213", "'");
        val = val.replace("\u00D5", "'");
        val = val.replace("\u0442\u0410\u0429", "'");
        val = val.replace("\u0442", "");
        val = val.replace("\u0410", "");
        val = val.replace("\u0429", "'");
        
        return val;
    }

}