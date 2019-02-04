package com.garethevans.church.opensongtablet;

class SongXML {
    // This class is used to build or initialise the song XML

    void initialiseSongTags() {
        FullscreenActivity.mTitle = FullscreenActivity.songfilename;
        FullscreenActivity.mAuthor = "";
        FullscreenActivity.mCopyright = "";
        FullscreenActivity.mPresentation = "";
        FullscreenActivity.mHymnNumber = "";
        FullscreenActivity.mCapo = "";
        FullscreenActivity.mCapoPrint = "false";
        FullscreenActivity.mTempo = "";
        FullscreenActivity.mTimeSig = "";
        FullscreenActivity.mDuration = "";
        FullscreenActivity.mPreDelay = "";
        FullscreenActivity.mCCLI = "";
        FullscreenActivity.mTheme = "";
        FullscreenActivity.mAltTheme = "";
        FullscreenActivity.mUser1 = "";
        FullscreenActivity.mUser2 = "";
        FullscreenActivity.mUser3 = "";
        FullscreenActivity.mKey = "";
        FullscreenActivity.mAka = "";
        FullscreenActivity.mKeyLine = "";
        FullscreenActivity.mBooks = "";
        FullscreenActivity.mMidi = "";
        FullscreenActivity.mMidiIndex = "";
        FullscreenActivity.mPitch = "";
        FullscreenActivity.mRestrictions = "";
        FullscreenActivity.mNotes = "";
        FullscreenActivity.mLyrics = "";
        FullscreenActivity.mStyle = "";
        FullscreenActivity.mLinkedSongs = "";
        FullscreenActivity.mNotation = "";
        FullscreenActivity.mPadFile = "";
        FullscreenActivity.mCustomChords = "";
        FullscreenActivity.mLinkYouTube = "";
        FullscreenActivity.mLinkWeb = "";
        FullscreenActivity.mLinkAudio = "";
        FullscreenActivity.mLoopAudio = "false";
        FullscreenActivity.mLinkOther = "";
        FullscreenActivity.mExtraStuff1 = "";
        FullscreenActivity.mExtraStuff2 = "";
        FullscreenActivity.mEncoding = "UTF-8";
    }

    String prepareBlankSongXML() {
        // Prepare the new XML file
        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + FullscreenActivity.songfilename + "</title>\n";
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
        if (FullscreenActivity.mEncoding==null || FullscreenActivity.mEncoding.equals("")) {
            FullscreenActivity.mEncoding = "UTF-8";
        }
        String myNEWXML = "<?xml version=\"1.0\" encoding=\""+FullscreenActivity.mEncoding+"\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + parseToHTMLEntities(FullscreenActivity.mTitle.toString()) + "</title>\n";
        myNEWXML += "  <author>" + parseToHTMLEntities(FullscreenActivity.mAuthor.toString()) + "</author>\n";
        myNEWXML += "  <copyright>" + parseToHTMLEntities(FullscreenActivity.mCopyright.toString()) + "</copyright>\n";
        myNEWXML += "  <presentation>" + parseToHTMLEntities(FullscreenActivity.mPresentation) + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + parseToHTMLEntities(FullscreenActivity.mHymnNumber) + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + parseToHTMLEntities(FullscreenActivity.mCapoPrint) + "\">" + parseToHTMLEntities(FullscreenActivity.mCapo) + "</capo>\n";
        myNEWXML += "  <tempo>" + parseToHTMLEntities(FullscreenActivity.mTempo) + "</tempo>\n";
        myNEWXML += "  <time_sig>" + parseToHTMLEntities(FullscreenActivity.mTimeSig) + "</time_sig>\n";
        myNEWXML += "  <duration>" + parseToHTMLEntities(FullscreenActivity.mDuration) + "</duration>\n";
        myNEWXML += "  <predelay>" + parseToHTMLEntities(FullscreenActivity.mPreDelay) + "</predelay>\n";
        myNEWXML += "  <ccli>" + parseToHTMLEntities(FullscreenActivity.mCCLI) + "</ccli>\n";
        myNEWXML += "  <theme>" + parseToHTMLEntities(FullscreenActivity.mTheme) + "</theme>\n";
        myNEWXML += "  <alttheme>" + parseToHTMLEntities(FullscreenActivity.mAltTheme) + "</alttheme>\n";
        myNEWXML += "  <user1>" + parseToHTMLEntities(FullscreenActivity.mUser1) + "</user1>\n";
        myNEWXML += "  <user2>" + parseToHTMLEntities(FullscreenActivity.mUser2) + "</user2>\n";
        myNEWXML += "  <user3>" + parseToHTMLEntities(FullscreenActivity.mUser3) + "</user3>\n";
        myNEWXML += "  <key>" + parseToHTMLEntities(FullscreenActivity.mKey) + "</key>\n";
        myNEWXML += "  <aka>" + parseToHTMLEntities(FullscreenActivity.mAka) + "</aka>\n";
        myNEWXML += "  <key_line>" + parseToHTMLEntities(FullscreenActivity.mKeyLine) + "</key_line>\n";
        myNEWXML += "  <books>" + parseToHTMLEntities(FullscreenActivity.mBooks) + "</books>\n";
        myNEWXML += "  <midi>" + parseToHTMLEntities(FullscreenActivity.mMidi) + "</midi>\n";
        myNEWXML += "  <midi_index>" + parseToHTMLEntities(FullscreenActivity.mMidiIndex) + "</midi_index>\n";
        myNEWXML += "  <pitch>" + parseToHTMLEntities(FullscreenActivity.mPitch) + "</pitch>\n";
        myNEWXML += "  <restrictions>" + parseToHTMLEntities(FullscreenActivity.mRestrictions) + "</restrictions>\n";
        myNEWXML += "  <notes>" + parseToHTMLEntities(FullscreenActivity.mNotes) + "</notes>\n";
        myNEWXML += "  <lyrics>" + parseToHTMLEntities(FullscreenActivity.mLyrics) + "</lyrics>\n";
        myNEWXML += "  <linked_songs>" + parseToHTMLEntities(FullscreenActivity.mLinkedSongs) + "</linked_songs>\n";
        myNEWXML += "  <pad_file>" + parseToHTMLEntities(FullscreenActivity.mPadFile) + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + parseToHTMLEntities(FullscreenActivity.mCustomChords) + "</custom_chords>\n";
        myNEWXML += "  <link_youtube>" + parseToHTMLEntities(FullscreenActivity.mLinkYouTube) + "</link_youtube>\n";
        myNEWXML += "  <link_web>" + parseToHTMLEntities(FullscreenActivity.mLinkWeb) + "</link_web>\n";
        myNEWXML += "  <link_audio>" + parseToHTMLEntities(FullscreenActivity.mLinkAudio) + "</link_audio>\n";
        myNEWXML += "  <loop_audio>" + parseToHTMLEntities(FullscreenActivity.mLoopAudio) + "</loop_audio>\n";
        myNEWXML += "  <link_other>" + parseToHTMLEntities(FullscreenActivity.mLinkOther) + "</link_other>\n";
        myNEWXML += "  <abcnotation>" + parseToHTMLEntities(FullscreenActivity.mNotation) + "</abcnotation>\n";

        if (!FullscreenActivity.mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff1 + "\n";
        }
        if (!FullscreenActivity.mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

        FullscreenActivity.mynewXML = myNEWXML;

        return myNEWXML;
    }

    String parseToHTMLEntities(String val) {
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
        val = val.replace("\'","'");

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
        val = val.replace("\'","'");
        val = val.replace("Õ","'");
        val = val.replace("Ó","'");
        val = val.replace("Ò","'");

        val = val.replace("&#39;", "'");
        val = val.replace("&#145", "'");
        val = val.replace("&#146;", "'");
        val = val.replace("&#147;", "'");
        val = val.replace("&#148;", "'");
        val = val.replace("тАЩ", "'");
        val = val.replace("\u0027", "'");
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