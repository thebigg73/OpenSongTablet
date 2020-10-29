package com.garethevans.church.opensongtablet;

// This class is used to store the static variables the app needs
// Most used to be in FullscreenActivity, but have been moved here
// Many have been removed as they can just be loaded from preferences as required
// These are the ones that are frequently accessed, so easier just to be static

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

class StaticVariables {

    public static boolean isHost = false, isConnected = false, usingNearby = false,
            receiveHostFiles = false, keepHostFiles=false;
    public static String connectionLog, deviceName, randomId = UUID.randomUUID().toString();

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
    static String previousSongInSet = "";
    static String nextSongInSet = "";
    static String nextSongKeyInSet = "";
    static String whatsongforsetwork = "";
    static String newSetContents = "";
    static String settoload = "";
    static String setMoveDirection = "";
    static String setnamechosen = "";
    static String[] mSet;
    static String[] mSetList;
    static boolean setView;
    static boolean doneshuffle = false;
    static boolean setchanged = false;
    static int setSize, indexSongInSet;
    static ArrayList<String> mTempSetList;
    
    // Storage variables
    static Uri uriTree;

    // The important starting up ones
    static Locale locale;

    // Default text sizes
    static final float infoBarLargeTextSize = 20.0f;
    static final float infoBarSmallTextSize = 14.0f;

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
    static Typeface typefaceLyrics, typefaceChords, typefaceSticky, typefacePreso, typefacePresoInfo, typefaceCustom, typefaceMono;


    // Song scaling used for scaling overrides if appropriate (matches to user preference)
    static String thisSongScale = "W";

    // The theme
    static String mDisplayTheme = "dark";

    // The app mode
    static String whichMode = "";

    // Default colours
    static final int darkblue = 0xff0000dd;
    static final int vdarkblue = 0xff000022;
    static final int purplyblue = 0xff452277;
    static final int vlightcyan = 0xffeeffff;
    static final int vlightblue = 0xffeeeeff;
    static final int blue = 0xff0000ff;
    static final int black = 0xff000000;
    static final int white = 0xffffffff;
    static final int grey = 0xff666666;
    static final int lightgrey = 0xff222222;
    static final int lightyellow = 0xffddaa00;
    static final int yellow = 0xffffff00;
    static final int vdarkyellow = 0xff111100;
    static final int red = 0xffff0000;
    static final int vdarkred = 0xff220000;
    static final int darkishred = 0xffaa1212;
    static final int transparent = 0x00000000;
    static final int vdarkgreen = 0xff002200;
    static final int darkishgreen = 0xff112211;
    static final int lightgreen = 0xffeeddee;
    static final int vlightgreen = 0xffeeffee;
    static final int green = 0xff00ff00;
    static final int darkpurple = 0xff220022;
    static final int vlightpurple = 0xffffeeff;
    static final int lightishcyan = 0xffddeeff;
    static final int highlighterblack = 0x66000000;
    static final int highlighterwhite = 0x66ffffff;
    static final int highlighterblue = 0x660000ff;
    static final int highlighterred = 0x66ff0000;
    static final int highlightergreen = 0x6600ff00;
    static final int highlighteryellow = 0x66ffff00;

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
    static boolean metronomeok;
    static boolean autoscrollok;
    static boolean pad1Playing;
    static boolean pad2Playing;
    static boolean pad1Fading = false;
    static boolean pad2Fading = false;
    static boolean clickedOnAutoScrollStart = false;
    static boolean pauseautoscroll = true;
    static boolean autoscrollispaused = false;
    static boolean isautoscrolling = false;
    static boolean mTimeSigValid = false;
    static boolean usingdefaults = false;
    static boolean learnPreDelay = false;
    static boolean learnSongLength = false;
    static boolean clickedOnMetronomeStart = false;
    static boolean clickedOnPadStart = false;
    static boolean pedalPreviousAndNextNeedsConfirm = true;
    static boolean pedalPreviousAndNextIgnore = false;
    static boolean reloadOfSong = false;
    static boolean showCapoInChordsFragment = false;
    static boolean canGoToNext = false;
    static boolean canGoToPrevious = false;
    static boolean doVibrateActive = true;
    static int scrollpageHeight;
    static int total_pixels_to_scroll = 0;
    static final int autoscroll_pause_time = 500;
    static int autoScrollDelay;
    static int autoScrollDuration;
    static int audiolength = -1;
    static int padtime_length = 0;
    static int autoscroll_modifier = 0;
    static int padInQuickFade = 0;
    static float pad1FadeVolume;
    static float pad2FadeVolume;
    static String whichbeat = "a";
    static String metronomeonoff = "off";
    static String pad_filename = "null";

    // PDF stuff
    static boolean showstartofpdf = true;

    // Used for the chord image display
    static String allchords = "", chordnotes = "";
    static String temptranspChords = "";

    // Request codes for callbacks
    static final int REQUEST_CAMERA_CODE = 1973, REQUEST_MICROPHONE_CODE = 1974, REQUEST_PDF_CODE = 1975,
            LINK_AUDIO = 1000, LINK_OTHER = 1001, REQUEST_IMAGE_CODE = 1111,
            REQUEST_BACKGROUND_IMAGE1 = 1544, REQUEST_BACKGROUND_IMAGE2 = 1555,
            REQUEST_BACKGROUND_VIDEO1 = 1556, REQUEST_BACKGROUND_VIDEO2 = 1557,
            REQUEST_CUSTOM_LOGO = 1558, REQUEST_FILE_CHOOSER = 7789,
            REQUEST_PROFILE_LOAD = 4567, REQUEST_PROFILE_SAVE = 5678;

    // PresentationService(HDMI) variables
    static int cast_padding, cast_screenWidth, cast_availableScreenWidth, cast_screenHeight, cast_availableScreenHeight,
        cast_availableWidth_1col, cast_availableWidth_2col, cast_availableWidth_3col,
            cast_lyricsCapoColor, cast_lyricsChordsColor, cast_presoFontColor, cast_lyricsBackgroundColor,
            cast_lyricsTextColor, cast_presoInfoColor, cast_presoAlertColor, cast_presoShadowColor, cast_lyricsVerseColor,
            cast_lyricsChorusColor, cast_lyricsPreChorusColor, cast_lyricsBridgeColor, cast_lyricsTagColor,
            cast_lyricsCommentColor, cast_lyricsCustomColor;
    static Drawable cast_defimage;
    static boolean forcecastupdate;
    static boolean panicRequired;
    static boolean infoBarChangeRequired;
    static Uri cast_vidUri;
    static MediaPlayer cast_mediaPlayer;
    static Activity activity;

}
