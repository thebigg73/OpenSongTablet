package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.StaticVariables;

public class SongXML {

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

    String getXML(ProcessSong processSong) {
        if (StaticVariables.mEncoding==null || StaticVariables.mEncoding.equals("")) {
            StaticVariables.mEncoding = "UTF-8";
        }
        String myNEWXML = "<?xml version=\"1.0\" encoding=\""+ StaticVariables.mEncoding+"\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + processSong.parseToHTMLEntities(StaticVariables.mTitle) + "</title>\n";
        myNEWXML += "  <author>" + processSong.parseToHTMLEntities(StaticVariables.mAuthor) + "</author>\n";
        myNEWXML += "  <copyright>" + processSong.parseToHTMLEntities(StaticVariables.mCopyright) + "</copyright>\n";
        myNEWXML += "  <presentation>" + processSong.parseToHTMLEntities(StaticVariables.mPresentation) + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + processSong.parseToHTMLEntities(StaticVariables.mHymnNumber) + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + processSong.parseToHTMLEntities(StaticVariables.mCapoPrint) + "\">" +
                processSong.parseToHTMLEntities(StaticVariables.mCapo) + "</capo>\n";
        myNEWXML += "  <tempo>" + processSong.parseToHTMLEntities(StaticVariables.mTempo) + "</tempo>\n";
        myNEWXML += "  <time_sig>" + processSong.parseToHTMLEntities(StaticVariables.mTimeSig) + "</time_sig>\n";
        myNEWXML += "  <duration>" + processSong.parseToHTMLEntities(StaticVariables.mDuration) + "</duration>\n";
        myNEWXML += "  <predelay>" + processSong.parseToHTMLEntities(StaticVariables.mPreDelay) + "</predelay>\n";
        myNEWXML += "  <ccli>" + processSong.parseToHTMLEntities(StaticVariables.mCCLI) + "</ccli>\n";
        myNEWXML += "  <theme>" + processSong.parseToHTMLEntities(StaticVariables.mTheme) + "</theme>\n";
        myNEWXML += "  <alttheme>" + processSong.parseToHTMLEntities(StaticVariables.mAltTheme) + "</alttheme>\n";
        myNEWXML += "  <user1>" + processSong.parseToHTMLEntities(StaticVariables.mUser1) + "</user1>\n";
        myNEWXML += "  <user2>" + processSong.parseToHTMLEntities(StaticVariables.mUser2) + "</user2>\n";
        myNEWXML += "  <user3>" + processSong.parseToHTMLEntities(StaticVariables.mUser3) + "</user3>\n";
        myNEWXML += "  <key>" + processSong.parseToHTMLEntities(StaticVariables.mKey) + "</key>\n";
        myNEWXML += "  <aka>" + processSong.parseToHTMLEntities(StaticVariables.mAka) + "</aka>\n";
        myNEWXML += "  <key_line>" + processSong.parseToHTMLEntities(StaticVariables.mKeyLine) + "</key_line>\n";
        myNEWXML += "  <books>" + processSong.parseToHTMLEntities(StaticVariables.mBooks) + "</books>\n";
        myNEWXML += "  <midi>" + processSong.parseToHTMLEntities(StaticVariables.mMidi) + "</midi>\n";
        myNEWXML += "  <midi_index>" + processSong.parseToHTMLEntities(StaticVariables.mMidiIndex) + "</midi_index>\n";
        myNEWXML += "  <pitch>" + processSong.parseToHTMLEntities(StaticVariables.mPitch) + "</pitch>\n";
        myNEWXML += "  <restrictions>" + processSong.parseToHTMLEntities(StaticVariables.mRestrictions) + "</restrictions>\n";
        myNEWXML += "  <notes>" + processSong.parseToHTMLEntities(StaticVariables.mNotes) + "</notes>\n";
        myNEWXML += "  <lyrics>" + processSong.parseToHTMLEntities(StaticVariables.mLyrics) + "</lyrics>\n";
        myNEWXML += "  <linked_songs>" + processSong.parseToHTMLEntities(StaticVariables.mLinkedSongs) + "</linked_songs>\n";
        myNEWXML += "  <pad_file>" + processSong.parseToHTMLEntities(StaticVariables.mPadFile) + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + processSong.parseToHTMLEntities(StaticVariables.mCustomChords) + "</custom_chords>\n";
        myNEWXML += "  <link_youtube>" + processSong.parseToHTMLEntities(StaticVariables.mLinkYouTube) + "</link_youtube>\n";
        myNEWXML += "  <link_web>" + processSong.parseToHTMLEntities(StaticVariables.mLinkWeb) + "</link_web>\n";
        myNEWXML += "  <link_audio>" + processSong.parseToHTMLEntities(StaticVariables.mLinkAudio) + "</link_audio>\n";
        myNEWXML += "  <loop_audio>" + processSong.parseToHTMLEntities(StaticVariables.mLoopAudio) + "</loop_audio>\n";
        myNEWXML += "  <link_other>" + processSong.parseToHTMLEntities(StaticVariables.mLinkOther) + "</link_other>\n";
        myNEWXML += "  <abcnotation>" + processSong.parseToHTMLEntities(StaticVariables.mNotation) + "</abcnotation>\n";

        if (!StaticVariables.mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + StaticVariables.mExtraStuff1 + "\n";
        }
        if (!StaticVariables.mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + StaticVariables.mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

        StaticVariables.myNewXML = myNEWXML;

        return myNEWXML;
    }

    void showWelcomeSong(Context c) {
        StaticVariables.songfilename = "Welcome to OpenSongApp";
        StaticVariables.mTitle = "Welcome to OpenSongApp";
        StaticVariables.mLyrics = c.getString(R.string.user_guide_lyrics);
        StaticVariables.mAuthor = "Gareth Evans";
        StaticVariables.mLinkWeb = "https://www.opensongapp.com";
    }
}
