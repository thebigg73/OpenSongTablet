package com.garethevans.church.opensongtablet.preferences;

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

import com.garethevans.church.opensongtablet.performance.PerformanceFragment;
import com.garethevans.church.opensongtablet.presentation.PresentationFragment;

import java.util.ArrayList;
import java.util.Locale;

public class StaticVariables {

    // The song fields
    public static String mTitle = "Welcome to OpenSongApp", mAuthor = "Gareth Evans", mCopyright = "",
            mLyrics = "", mCCLI = "", mAltTheme = "", mPresentation = "", mHymnNumber = "",
            mUser1 = "", mUser2 = "", mUser3 = "", mKey = "", mAka = "", mKeyLine = "",
            mStyle = "", mCapo = "", mCapoPrint = "", mTempo = "", mTimeSig = "", mTheme = "",
            mDuration = "", mPreDelay = "", mBooks = "", mMidi = "", mMidiIndex = "",
            mPitch = "", mRestrictions = "", mNotes = "", mLinkedSongs = "", mExtraStuff1 = "",
            mExtraStuff2 = "", mPadFile = "", mCustomChords = "", mLinkYouTube = "", mLinkWeb = "",
            mLinkAudio = "", mLoopAudio = "false", mLinkOther = "", mNotation = "", mEncoding = "UTF-8",
            fileType = "";

    // For moving through songs in list with swipe
    public static ArrayList<String> songsInList = new ArrayList<>();

    public static PerformanceFragment performanceFragment;
    public static PresentationFragment presentationFragment;

    // Set stuff
    public static String previousSongInSet = "";
    public static String nextSongInSet = "";
    public static String myNewXML;
    public static boolean homeFragment;
    public static boolean sortAlphabetically = true;
    public static String whichDirection = "R2L";
    public static String whattodo = "";
    public static int pdfPageCurrent = 0;
    public static int pdfPageCount = 0;
    public static String linkclicked = "";
    public static int currentSongIndex;
    public static int previousSongIndex;
    public static int nextSongIndex;
    // Views and bits on the pages
    public static int currentScreenOrientation;
    public static boolean orientationChanged;
    public static boolean indexRequired = true;
    public static boolean needtorefreshsongmenu;
    static String nextSongKeyInSet = "";
    public static String whatsongforsetwork = "";
    public static String newSetContents = "";
    public static String settoload = "";
    public static String setMoveDirection = "";
    public static String setnamechosen = "";
    public static String[] mSet;
    public static String[] mSetList;
    public static boolean setView;
    public static boolean doneshuffle = false;
    public static boolean setchanged = false;
    public static int setSize;
    public static int indexSongInSet;
    public static ArrayList<String> mTempSetList;
    public static String currentSet;
    
    // Storage variables
    static Uri uriTree;

    // The important starting up ones
    public static Locale locale;

    // Default text sizes
    static final float infoBarLargeTextSize = 20.0f;
    static final float infoBarSmallTextSize = 14.0f;

    // Song locations.  These are also saved as preferences if the song loads correctly (without crashing midway)
    public static String whichSongFolder = "";
    public static String songfilename = "";

    // The action bar size (used to work out song space available)
    public static int ab_height;

    // The song secions.  Used when paring songs into bits
    public static String[] songSections;
    static String[] songSectionsLabels;
    static String[] songSectionsTypes;
    static String[][] sectionContents, sectionLineTypes, projectedContents, projectedLineTypes;
    static String songSection_holder; // This carries on section types after line breaks
    static float[] sectionScaleValue;
    public static int currentSection;

    // The fonts used.  They are preloaded in SetTypeFace
    public static Typeface typefaceLyrics, typefaceChords, typefaceSticky, typefacePreso, typefacePresoInfo, typefaceCustom, typefaceMono;


    // Song scaling used for scaling overrides if appropriate (matches to user preference)
    static String thisSongScale = "W";

    // The theme
    public static String mDisplayTheme = "dark";

    // The app mode
    public static String whichMode = "";

    // Default colours
    public static final int darkblue = 0xff0000dd;
    public static final int vdarkblue = 0xff000022;
    public static final int purplyblue = 0xff452277;
    public static final int vlightcyan = 0xffeeffff;
    public static final int vlightblue = 0xffeeeeff;
    public static final int blue = 0xff0000ff;
    public static final int black = 0xff000000;
    public static final int white = 0xffffffff;
    public static final int grey = 0xff666666;
    public static final int lightgrey = 0xff222222;
    public static final int lightyellow = 0xffddaa00;
    public static final int yellow = 0xffffff00;
    public static final int vdarkyellow = 0xff111100;
    public static final int red = 0xffff0000;
    public static final int vdarkred = 0xff220000;
    public static final int darkishred = 0xffaa1212;
    public static final int transparent = 0x00000000;
    public static final int vdarkgreen = 0xff002200;
    public static final int darkishgreen = 0xff112211;
    public static final int lightgreen = 0xffeeddee;
    public static final int vlightgreen = 0xffeeffee;
    public static final int green = 0xff00ff00;
    public static final int darkpurple = 0xff220022;
    public static final int vlightpurple = 0xffffeeff;
    public static final int lightishcyan = 0xffddeeff;
    public static final int highlighterblack = 0x66000000;
    public static final int highlighterwhite = 0x66ffffff;
    public static final int highlighterblue = 0x660000ff;
    public static final int highlighterred = 0x66ff0000;
    public static final int highlightergreen = 0x6600ff00;
    public static final int highighteryellow = 0x66ffff00;

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
    public static int detectedChordFormat = 1;
    public static int transposeTimes = 1;
    public static String transposeDirection = "0";
    public static String transposedLyrics = "";

    // The toast message - sometimes used to identify next steps for the app, sometimes just to display
    public static String myToastMessage = "";

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
    static boolean ignoreGesture = false;
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
    public static String pad_filename = "null";

    // PDF stuff
    public static boolean showstartofpdf = true;

    // Used for the chord image display
    static String allchords = "";
    public static String chordnotes = "";
    public static String temptranspChords = "";

    // Request codes for callbacks
    static final int REQUEST_CAMERA_CODE = 1973;
    static final int REQUEST_MICROPHONE_CODE = 1974;
    static final int REQUEST_PDF_CODE = 1975;
    static final int LINK_AUDIO = 1000;
    static final int LINK_OTHER = 1001;
    static final int REQUEST_IMAGE_CODE = 1111;
    public static final int REQUEST_BACKGROUND_IMAGE1 = 1544;
    public static final int REQUEST_BACKGROUND_IMAGE2 = 1555;
    public static final int REQUEST_BACKGROUND_VIDEO1 = 1556;
    public static final int REQUEST_BACKGROUND_VIDEO2 = 1557;
    public static final int REQUEST_CUSTOM_LOGO = 1558;
    static final int REQUEST_FILE_CHOOSER = 7789;
    static final int REQUEST_PROFILE_LOAD = 4567;
    static final int REQUEST_PROFILE_SAVE = 5678;

    // PresentationService(HDMI) variables
    static int cast_padding, cast_screenWidth, cast_availableScreenWidth, cast_screenHeight, cast_availableScreenHeight,
        cast_availableWidth_1col, cast_availableWidth_2col, cast_availableWidth_3col,
            cast_lyricsCapoColor, cast_lyricsChordsColor, cast_presoFontColor, cast_lyricsBackgroundColor,
            cast_lyricsTextColor, cast_presoInfoColor, cast_presoAlertColor, cast_presoShadowColor, cast_lyricsVerseColor,
            cast_lyricsChorusColor, cast_lyricsPreChorusColor, cast_lyricsBridgeColor, cast_lyricsTagColor,
            cast_lyricsCommentColor, cast_lyricsCustomColor;
    static Drawable cast_defimage;
    static boolean forcecastupdate;
    static Uri cast_vidUri;
    static MediaPlayer cast_mediaPlayer;
    public static Activity activity;

}
