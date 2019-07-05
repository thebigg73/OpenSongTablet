package com.garethevans.church.opensongtablet;

// This class is used to store the static variables the app needs
// Most used to be in FullscreenActivity, but have been moved here
// Many have been removed as they can just be loaded from preferences as required
// These are the ones that are frequently accessed, so easier just to be static

import android.graphics.Typeface;
import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Locale;

class StaticVariables {

    static String temptranspChords = "";
    static String pad_filename = "null";


    // The song fields
    static String mTitle = "Welcome to OpenSongApp", mAuthor = "Gareth Evans", mCopyright = "",
            mLyrics = "", mCCLI = "", mAltTheme = "", mPresentation = "", mHymnNumber = "",
            mUser1 = "", mUser2 = "", mUser3 = "", mKey = "", mAka = "", mKeyLine = "",
            mStyle = "", mCapo = "", mCapoPrint = "", mTempo = "", mTimeSig = "", mTheme = "",
            mDuration = "", mPreDelay = "", mBooks = "", mMidi = "", mMidiIndex = "",
            mPitch = "", mRestrictions = "", mNotes = "", mLinkedSongs = "", mExtraStuff1 = "",
            mExtraStuff2 = "", mPadFile = "", mCustomChords = "", mLinkYouTube = "", mLinkWeb = "",
            mLinkAudio = "", mLoopAudio = "false", mLinkOther = "", mNotation = "", mEncoding = "UTF-8";

    // Set stuff
    static String previousSongInSet = "", nextSongInSet = "", nextSongKeyInSet = "",
            whatsongforsetwork = "", newSetContents = "", settoload = "", setMoveDirection = "",
            mySetXML = "", setnamechosen = "";
    static String[] mSet, mSetList, myParsedSet;
    static boolean setView, doneshuffle = false, addingtoset = false, setchanged = false;
    static int setSize, indexSongInSet;
    static ArrayList<String> mTempSetList;
    
    // Storage variables
    static Uri uriTree;

    // The important starting up ones
    static Locale locale;

    // Default text sizes
    static float infoBarLargeTextSize = 20.0f, infoBarSmallTextSize = 14.0f;

    // Song locations.  These are also saved as preferences if the song loads correctly (without crashing midway)
    static String whichSongFolder = "", songfilename = "";

    // The action bar size (used to work out song space available)
    static int ab_height;

    // The song secions.  Used when paring songs into bits
    static String[] songSections, songSectionsLabels, songSectionsTypes;
    static String[][] sectionContents, sectionLineTypes, projectedContents, projectedLineTypes;
    static String songSection_holder; // This carries on section types after line breaks
    static float[] sectionScaleValue;
    static int currentSection;

    // The fonts used.  They are preloaded in SetTypeFace
    static Typeface typefaceLyrics, typefaceChords, typefacePreso, typefacePresoInfo, typefaceCustom, typefaceMono;


    // Song scaling used for scaling overrides if appropriate (matches to user preference)
    static String thisSongScale = "W";

    // The theme
    static String mDisplayTheme = "dark";

    // The app mode
    static String whichMode = "";

    // Default colours
    static int darkblue = 0xff0000dd, vdarkblue = 0xff000022, purplyblue = 0xff452277, vlightcyan = 0xffeeffff,
            vlightblue = 0xffeeeeff, blue = 0xff0000ff;
    static int black = 0xff000000, white = 0xffffffff, grey = 0xff666666, lightgrey = 0xff222222;
    static int lightyellow = 0xffddaa00, yellow = 0xffffff00, vdarkyellow = 0xff111100;
    static int red = 0xffff0000, vdarkred = 0xff220000, darkishred = 0xffaa1212, transparent = 0x00000000;
    static int vdarkgreen = 0xff002200, darkishgreen = 0xff112211, lightgreen = 0xffeeddee, vlightgreen = 0xffeeffee, green = 0xff00ff00;
    static int darkpurple = 0xff220022, vlightpurple = 0xffffeeff, lightishcyan = 0xffddeeff;
    static int highlighterblack = 0x66000000, highlighterwhite = 0x66ffffff, highlighterblue = 0x660000ff,
            highlighterred = 0x66ff0000, highlightergreen = 0x6600ff00, highighteryellow = 0x66ffff00;

    // Option menu defines which menu we are in
    static String whichOptionMenu = "MAIN";

    // MIDI stuff
    static MidiDevice midiDevice;
    static MidiManager midiManager;
    static MidiInputPort midiInputPort;
    static MidiOutputPort midiOutputPort;
    static String midiDeviceName = "", midiDeviceAddress = "";
    static MidiReceiver loggingReceiver;

    // Stuff for transposing songs
    static int detectedChordFormat = 1, transposeTimes = 1;
    static String transposeDirection = "0", transposedLyrics = "";

    // The toast message - sometimes used to identify next steps for the app, sometimes just to display
    static String myToastMessage = "";

    // Uri of external files (image from image file) clicked to present
    static Uri uriToLoad;

    // Metronome, pad and autoscroll stuff
    static boolean metronomeok, padok, autoscrollok, pad1Playing, pad2Playing, pad1Fading,
            pad2Fading, padson = false, clickedOnAutoScrollStart = false,
            pauseautoscroll = true, autoscrollispaused = false, isautoscrolling = false,
            mTimeSigValid = false, usingdefaults = false, learnPreDelay = false, learnSongLength = false,
            clickedOnMetronomeStart = false, clickedOnPadStart = false;
    static int scrollpageHeight,  total_pixels_to_scroll = 0, autoscroll_pause_time = 500,
            autoScrollDelay, autoScrollDuration, audiolength = -1, fadeWhichPad,
            padtime_length = 0, autoscroll_modifier = 0;
    static String popupAutoscroll_stoporstart = "stop", whichbeat = "a", metronomeonoff = "off";

    // PDF stuff
    static boolean showstartofpdf = true;

    // Used for the chord image display
    static String allchords = "", chordnotes = "";

    // Request codes for callbacks
    static final int REQUEST_CAMERA_CODE = 1973, REQUEST_MICROPHONE_CODE = 1974, REQUEST_PDF_CODE = 1975,
            LINK_AUDIO = 1000, LINK_OTHER = 1001, REQUEST_IMAGE_CODE = 1111;
}
