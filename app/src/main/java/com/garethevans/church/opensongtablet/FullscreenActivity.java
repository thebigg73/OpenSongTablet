package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutServiceData;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("deprecation")
@SuppressLint({"DefaultLocale", "RtlHardcoded", "InflateParams", "SdCardPath"})
public class FullscreenActivity extends AppCompatActivity implements PopUpImportExportOSBFragment.MyInterface,
        PopUpImportExternalFile.MyInterface {

    //First up, declare all of the variables needed by this application

    public static boolean sortAlphabetically = true;

    // PopUp window size and alpha
    public static float popupAlpha_Set = 0.6f;
    public static float popupDim_Set = 0.7f;
    public static float popupScale_Set = 0.8f;
    public static String popupPosition_Set = "c";
    public static float popupAlpha_All = 0.6f;
    public static float popupDim_All = 0.7f;
    public static float popupScale_All = 0.8f;
    public static String popupPosition_All = "c";
    public static float pageButtonAlpha = 0.4f;

    // Song menu
    public static String indexlog = "";
    public static ArrayList<String> searchFileName = new ArrayList<>();
    public static ArrayList<String> searchFolder = new ArrayList<>();
    public static ArrayList<String> searchTitle = new ArrayList<>();
    public static ArrayList<String> searchAuthor = new ArrayList<>();
    public static ArrayList<String> searchShortLyrics = new ArrayList<>();
    public static ArrayList<String> searchTheme = new ArrayList<>();
    public static ArrayList<String> searchKey = new ArrayList<>();
    public static ArrayList<String> searchHymnNumber = new ArrayList<>();
    //public static ArrayList<SearchViewItems> searchlist = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    public static SearchViewAdapter sva;
    //ArrayAdapter<String> lva;
    //public static boolean menuscroll = true;

    // Custom QuickLaunch buttons
    public static String quickLaunchButton_1 = "";
    public static String quickLaunchButton_2 = "";
    public static String quickLaunchButton_3 = "";
    public static int fabSize = FloatingActionButton.SIZE_MINI;
    public static boolean page_set_visible;
    public static boolean page_pad_visible;
    public static boolean page_metronome_visible;
    public static boolean page_autoscroll_visible;
    public static boolean page_extra_visible;
    public static boolean page_custom_visible;
    public static boolean page_chord_visible;
    public static boolean page_links_visible;
    public static boolean page_sticky_visible;
    public static boolean page_pages_visible;
    public static boolean page_custom1_visible;
    public static boolean page_custom2_visible;
    public static boolean page_custom3_visible;
    public static boolean page_extra_grouped;
    public static boolean page_custom_grouped;

    // Long and short key presses
    public static boolean longKeyPress = false;
    public static boolean shortKeyPress = false;

    // Option menu
    public static String whichOptionMenu = "MAIN";

    // Checks for metronome, pad, autoscroll
    public static boolean metronomeok;
    public static boolean padok;
    public static boolean autoscrollok;

    // Updated scaled view stuff
    public static int[] viewwidth;
    public static int[] viewheight;
    public static int padding = 18;
    // Song sections
    public static LinearLayout[] sectionviews;
    public static boolean[] sectionrendered;
    public static Bitmap[] sectionbitmaps;

    // Playback progress
    static long time_start;
    public static int audiolength = -1;
    public static boolean pad1Playing;
    public static boolean pad2Playing;
    public static boolean pad1Fading;
    public static boolean pad2Fading;
    public static int fadeWhichPad;

    //LinearLayout playbackProgress;
    //TextView padcurrentTime_TextView;
    //TextView padtotalTime_TextView;
    //long padtime_start;
    //public static int padaudiolength = -1;

    public static String filetoselect = "";
    public static String pagebutton_scale;
    public static String profile;

    // Transpose preferences
    public static String prefChord_Aflat_Gsharp = "";
    public static String prefChord_Bflat_Asharp = "";
    public static String prefChord_Dflat_Csharp = "";
    public static String prefChord_Eflat_Dsharp = "";
    public static String prefChord_Gflat_Fsharp = "";
    public static String prefChord_Aflatm_Gsharpm = "";
    public static String prefChord_Bflatm_Asharpm = "";
    public static String prefChord_Dflatm_Csharpm = "";
    public static String prefChord_Eflatm_Dsharpm = "";
    public static String prefChord_Gflatm_Fsharpm = "";
    public static boolean switchsharpsflats = false;

    // The toolbar
    public Toolbar toolbar;
    public ActionBar ab;
    public static int ab_height;
    //public RelativeLayout songandauthor;
    //public TextView songtitle_ab;
    //public TextView songkey_ab;
    //public TextView songauthor_ab;

    // Immersive mode stuff

    // This is for trying to automatically open songs via intent
    public static Intent incomingfile;
    public static String file_name = "";
    public static String file_location = "";
    public static String file_type = "";
    public static Uri file_uri;
    public static String file_contents = "";

    // Screencapure variables
    public static Bitmap bmScreen;
    public static boolean abort = false;

    // Custom note/slide variables
    public static String noteorslide = "";
    public static String customslide_title = "";
    public static String customslide_content = "";
    public static String customimage_list = "";
    public static String customimage_loop = "";
    public static String customimage_time = "";
    public static boolean customreusable = false;
    public static String customreusabletoload = "";
    public static boolean isImageSection = false;
    public static String imagetext="";
    public static int checkscroll_time = 1600;
    public static int delayswipe_time = 800;
    public static int crossFadeTime = 8000;
    public static String toggleScrollArrows = "";

    public static boolean converting = false;
    public static String phrasetosearchfor;

    public static int myWidthAvail;
    public static int myHeightAvail;

    public static Typeface typeface0;
    public static Typeface typeface1;
    public static Typeface typeface2;
    public static Typeface typeface3;
    public static Typeface typeface4;
    public static Typeface typeface4i;
    public static Typeface typeface5;
    public static Typeface typeface5i;
    public static Typeface typeface6;
    public static Typeface typeface7;
    public static Typeface typeface7i;
    public static Typeface typeface8;
    public static Typeface typeface8i;
    public static Typeface typeface9;
    public static Typeface typeface9i;
    public static Typeface typeface10;
    public static Typeface typeface10i;
    public static Typeface typeface11;
    public static Typeface typeface11i;
    public static Typeface typeface12;
    public static Typeface typeface12i;

    public static ArrayList<String> exportsetfilenames = new ArrayList<>();
    public static ArrayList<String> exportsetfilenames_ost = new ArrayList<>();
    public static String lastSetName = "";
    public static String chordInstrument = "g";
    public static String showNextInSet = "top";
    public static String allchords = "";
    public static String chordnotes = "";
    public static String capoDisplay = "";
    public static String languageToLoad = "";
    //private static String tempLanguage = "";
    //private String[] backUpFiles;
    //private String backupchosen = "";

    // Stuff to deal with the splash screen/version
    public static int version = 0;
    public static int showSplashVersion;


    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    public static boolean receiveHostFiles;

    public static int currentapiVersion;

    public static String mediaStore = "";

    public static String emailtext = "";

    public static int maxvolrange;

    public static String whattodo = "";

    public static boolean pressing_button = false;

    public static String popupAutoscroll_stoporstart = "stop";
    public static int scrollpageHeight;
    public static boolean autostartautoscroll;

    public DialogFragment newFragment;

    public static long time_passed = 0;
    public static int padtime_length = 0;
    public static int beatoffcolour = 0xf232333;
    public static String whichbeat = "a";
    public static boolean visualmetronome = false;

    @SuppressWarnings("unused")
    public static boolean fadeout1 = false;
    @SuppressWarnings("unused")
    public static boolean fadeout2 = false;
    public static final short minBpm = 40;
    public static final short maxBpm = 199;
    //private short bpm = 100;
    public static short noteValue = 4;
    public static short beats = 4;
    public static int currentBeat = 1;
    @SuppressWarnings("unused")
    public static float metrovol;
    @SuppressWarnings("unused")
    public short initialVolume;
    public static double beatSound = 1200;
    public static double sound = 1600;


    public static String metronomeonoff = "off";
    public static String metronomepan = "both";
    public static float metronomevol = 1.0f;
    public static float padvol = 1.0f;
    public static String padpan = "both";
    public static int timesigindex;
    public static boolean mTimeSigValid = false;
    public static String[] timesigs;
    //public static int temposlider;
    static boolean usingdefaults = false;
    public static final int autoscroll_pause_time = 500; // specified in ms
    public static int default_autoscroll_songlength;
    public static int default_autoscroll_predelay;
    public static String autoscroll_default_or_prompt = "";
    public static boolean pauseautoscroll = true;
    public static boolean autoscrollispaused = false;
    public static boolean isautoscrolling = false;
    public static float autoscroll_pixels = 0.0f;
    public static float newPosFloat = 0.0f;
    public static int total_pixels_to_scroll = 0;
    public static String autoscrollonoff = "false";
    public static String pad_filename = "null";
    //public static boolean isfading1 = false;
    //public static boolean isfading2 = false;
    public static boolean padson = false;
    public static boolean needtorefreshsongmenu = false;
    public static MediaPlayer mPlayer1 = new MediaPlayer();
    public static MediaPlayer mPlayer2 = new MediaPlayer();
    public static boolean orientationchanged = false;
    //private static ImageButton uselinkaudiolength_ImageButton;
    //private static int alreadyshowingpage;
    public static int keyindex;

    public static final int REQUEST_CAMERA_CODE = 1973;
    public static String mCurrentPhotoPath;
    public static final int REQUEST_MICROPHONE_CODE = 1974;

    public static int pdfPageCurrent = 0;
    public static int pdfPageCount = 0;
    public static boolean isPDF = false;
    public static boolean isImage = false;
    public static boolean isSong = false;
    public static boolean isSlide = false;
    public static boolean isScripture = false;
    public static boolean isImageSlide = false;
    public static boolean isVideo = false;

    public static String toggleAutoSticky = "";
    public static boolean hideActionBar;
    public static String setnamechosen = "";
    public static boolean addingtoset = false;

    // Swipe
    public static int SWIPE_MIN_DISTANCE = 300;
    public static int SWIPE_MAX_OFF_PATH = 200;
    public static int SWIPE_THRESHOLD_VELOCITY = 1000;
    public static boolean swipeForMenus;
    public static boolean swipeForSongs;

    public static String whichMode = "";

    // Views and bits on the pages
    public static int mScreenOrientation;
    public static boolean scrollbutton = false;
    public static boolean actionbarbutton = false;

    // Font sizes (relative)
    public static int linespacing;

    // Colours
    public static int dark_lyricsTextColor;
    public static int dark_lyricsBackgroundColor;
    public static int dark_lyricsVerseColor;
    public static int dark_lyricsChorusColor;
    public static int dark_lyricsBridgeColor;
    public static int dark_lyricsCommentColor;
    public static int dark_lyricsPreChorusColor;
    public static int dark_lyricsTagColor;
    public static int dark_lyricsChordsColor;
    public static int dark_lyricsCustomColor;
    public static int dark_lyricsCapoColor;
    public static int dark_presoFont;
    public static int dark_presoInfoFont;
    public static int dark_presoAlertFont;
    public static int dark_presoShadow;
    public static int dark_metronome;
    public static int dark_pagebuttons;

    public static int light_lyricsTextColor;
    public static int light_lyricsBackgroundColor;
    public static int light_lyricsVerseColor;
    public static int light_lyricsChorusColor;
    public static int light_lyricsBridgeColor;
    public static int light_lyricsCommentColor;
    public static int light_lyricsPreChorusColor;
    public static int light_lyricsTagColor;
    public static int light_lyricsChordsColor;
    public static int light_lyricsCustomColor;
    public static int light_lyricsCapoColor;
    public static int light_presoFont;
    public static int light_presoInfoFont;
    public static int light_presoAlertFont;
    public static int light_presoShadow;
    public static int light_metronome;
    public static int light_pagebuttons;

    public static int custom1_lyricsTextColor;
    public static int custom1_lyricsBackgroundColor;
    public static int custom1_lyricsVerseColor;
    public static int custom1_lyricsChorusColor;
    public static int custom1_lyricsBridgeColor;
    public static int custom1_lyricsCommentColor;
    public static int custom1_lyricsPreChorusColor;
    public static int custom1_lyricsTagColor;
    public static int custom1_lyricsChordsColor;
    public static int custom1_lyricsCustomColor;
    public static int custom1_lyricsCapoColor;
    public static int custom1_presoFont;
    public static int custom1_presoInfoFont;
    public static int custom1_presoAlertFont;
    public static int custom1_presoShadow;
    public static int custom1_metronome;
    public static int custom1_pagebuttons;

    public static int custom2_lyricsTextColor;
    public static int custom2_lyricsBackgroundColor;
    public static int custom2_lyricsVerseColor;
    public static int custom2_lyricsChorusColor;
    public static int custom2_lyricsBridgeColor;
    public static int custom2_lyricsCommentColor;
    public static int custom2_lyricsPreChorusColor;
    public static int custom2_lyricsTagColor;
    public static int custom2_lyricsChordsColor;
    public static int custom2_lyricsCustomColor;
    public static int custom2_lyricsCapoColor;
    public static int custom2_presoFont;
    public static int custom2_presoInfoFont;
    public static int custom2_presoAlertFont;
    public static int custom2_presoShadow;
    public static int custom2_metronome;
    public static int custom2_pagebuttons;

    public static int lyricsBoxColor;
    public static int lyricsTextColor;
    public static int lyricsBackgroundColor;
    public static int lyricsChorusColor;
    public static int lyricsVerseColor;
    public static int lyricsBridgeColor;
    public static int lyricsCommentColor;
    public static int lyricsPreChorusColor;
    public static int lyricsTagColor;
    public static int lyricsChordsColor;
    public static int lyricsCustomColor;
    public static int lyricsCapoColor;
    public static int metronomeColor;
    public static int pagebuttonsColor;
    public static int presoAlertFontColor;
    public static int presoFontColor;
    public static int presoInfoFontColor;
    public static int presoShadowColor;
    public static float commentfontscalesize;
    public static float headingfontscalesize;
    public static float chordfontscalesize;

    // Page turner
    public static int pageturner_NEXT;
    public static int pageturner_PREVIOUS;
    public static int pageturner_UP;
    public static int pageturner_DOWN;
    public static int pageturner_PAD;
    public static int pageturner_AUTOSCROLL;
    public static int pageturner_METRONOME;
    public static String toggleScrollBeforeSwipe = "";
    public static String togglePageButtons = "";

    // Set variables
    public static int autoScrollDelay;
    public static int autoScrollDuration;
    public static String prefStorage = "";
    public static String customStorage = "";
    public static boolean wasscrolling = false;
    public static boolean isManualDragging = false;
    public static String alwaysPreferredChordFormat = "";
    public static String gesture_doubletap = "";
    public static String gesture_longpress = "";
    public static String longpressdownpedalgesture = "";
    public static String longpressuppedalgesture = "";
    public static String longpresspreviouspedalgesture = "";
    public static String longpressnextpedalgesture = "";
    public static String bibleFile = "";
    public static String chordFormat = "";
    public static String oldchordformat = "";
    public static String presenterChords = "";
    public static String swipeDrawer = "";
    public static String swipeSet = "";
    public static String tempswipeSet = "enable";
    public static String whichDirection = "R2L";
    public static int indexSongInSet;
    public static String previousSongInSet = "";
    public static String nextSongInSet = "";
    public static String nextSongKeyInSet = "";
    public static String mTheme = "";
    public static String mDisplayTheme = "Theme.Holo";
    public static boolean setView;
    public static int setSize;
    public static boolean showingSetsToLoad = false;
    public static String whatsongforsetwork = "";
    public static String mySet = "";
    public static String newSetContents = "";
    public static String[] mSet;
    public static String[] mSetList;
    public static ArrayList<String> mTempSetList;
    public static boolean doneshuffle = false;

    public static float menuSize;
    public static boolean showSetTickBoxInSongMenu;

    // Song filenames, folders, set filenames, folders
    public static String currentFolder = "";
    public static String newFolder = "";
    public static String whichSongFolder = "";
    public static String[] mySetsFileNames;
    public static File[] mySetsFiles;
    public static String whichSetCategory;
    public static String[] mySetsFolderNames;
    public static File[] mySetsDirectories;
    public static File filechosen;
    public static File file;
    public static File setfile;
    public static String settoload = "";
    public static String[] mSongFileNames;
    public static String[] mSongFolderNames;
    public static String[][] songDetails;
    public static Map<String, Integer> mapIndex;

    public static String setMoveDirection = "";
    public static ArrayList<String> allfilesforsearch = new ArrayList<>();
    //public static ArrayList<String> allfilesforsearch_folder = new ArrayList<>();
    //public static ArrayList<String> allfilesforsearch_song = new ArrayList<>();
    public static ArrayList<String> search_database = new ArrayList<>();
    public static boolean safetosearch = false;

    public static int currentSongIndex;
    public static int previousSongIndex;
    public static int nextSongIndex;

    // Presentation mode variables
    public static boolean presoAutoScale;
    public static boolean presoShowChords;
    public static int presoFontSize;
    public static int presoMaxFontSize;
    public static float presoTitleSize;
    public static float presoAuthorSize;
    public static float presoCopyrightSize;
    public static float presoAlertSize;
    public static int presoLyricsAlign;
    public static int presoInfoAlign;
    public static Typeface presoInfoFont;

    public static String customLogo = "";
    public static float customLogoSize;
    public static float presoAlpha;
    public static String myAlert = "";
    public static boolean dualDisplayCapable;
    public static String backgroundImage1 = "";
    public static String backgroundImage2 = "";
    public static String backgroundVideo1 = "";
    public static String backgroundVideo2 = "";
    public static String backgroundToUse = "";
    public static String backgroundTypeToUse = "";
    public static int xmargin_presentation;
    public static int ymargin_presentation;
    public static boolean usePresentationOrder = false;
    public static int presoTransitionTime = 800;

    // Song xml data
    public static ArrayList<String> foundSongSections_heading = new ArrayList<>();
    //public static ArrayList<String> foundSongSections_content = new ArrayList<>();

    public static CharSequence mTitle = "";
    public static CharSequence mAuthor = "Gareth Evans";
    //private static String mTempAuthor = "";
    public static CharSequence mCopyright = "";
    public static String mLyrics = "";
    public static String mCCLI = "";
    public static String mAltTheme = "";
    public static String mPresentation = "";
    public static String mHymnNumber = "";
    public static String mUser1 = "";
    public static String mUser2 = "";
    public static String mUser3 = "";
    public static String mKey = "";
    public static String mAka = "";
    public static String mKeyLine = "";
    public static String mStyle = "";
    public static String mCapo = "";
    public static String mCapoPrint = "";
    public static String mTempo = "";
    public static String mTimeSig = "";
    public static String mDuration = "";
    public static String mPreDelay = "";
    public static String mBooks = "";
    public static String mMidi = "";
    public static String mMidiIndex = "";
    public static String mPitch = "";
    public static String mRestrictions = "";
    public static String mNotes = "";
    public static String temptranspChords = "";
    public static String mLinkedSongs = "";
    public static String mExtraStuff1 = "";
    public static String mExtraStuff2 = "";
    public static String mPadFile = "";
    public static String mCustomChords = "";
    public static String mLinkYouTube = "";
    public static String mLinkWeb = "";
    public static String mLinkAudio = "";
    public static String mLoopAudio = "false";
    public static String mLinkOther = "";

    public static String capokey = null;

    public static boolean isPresenting;
    public static int scalingDensity = 240;

    // Info for the lyrics table
    public static boolean scalingfiguredout = false;
    public static boolean botherwithcolumns;
    public static int splitpoint;
    public static int thirdsplitpoint;
    public static int twothirdsplitpoint;
    public static int halfsplit_section;
    public static int thirdsplit_section;
    public static int twothirdsplit_section;
    public static String mStorage = "";
    public static String myLyrics = "";
    public static float mFontSize;
    public static int mMaxFontSize;
    public static int mMinFontSize;
    public static boolean override_fullscale;
    public static boolean override_widthscale;
    public static String toggleYScale = "";
    public static String thissong_scale;
    public static String mySetXML = "";
    public static String[] myParsedSet;
    public static String myXML = "";
    public static String mynewXML = "";
    public static String[] myParsedLyrics;
    public static String[] myTransposedLyrics;
    public static String songfilename = "";
    public static String linkclicked = "";
    public static SharedPreferences myPreferences;
    public static int numrowstowrite;
    public static String transposeDirection = "0";
    public static int transposeTimes = 1;
    public static String transposeStyle = "sharps";
    public static String transposedLyrics = "";
    public static boolean showChords;
    public static boolean showLyrics;

    public static String myToastMessage = "";
    public static boolean showCapo;
    public static boolean showCapoChords;
    public static boolean showNativeAndCapoChords;

    public static String mScripture = "";
    public static String incoming_text = "";
    public static String scripture_title = "";
    public static String scripture_verse = "";
    public static String mainfoldername = "";
    public static int mylyricsfontnum;
    public static int mychordsfontnum;
    public static int mypresofontnum;
    public static int mypresoinfofontnum;
    public static Typeface lyricsfont;
    public static Typeface commentfont;
    public static Typeface chordsfont;
    public static Typeface presofont;

    public static int whichPad = 0;

    public static String[] songSections;
    public static String[] songSectionsLabels;
    public static String[][] sectionContents;
    public static String[][] sectionLineTypes;
    public static String[] songSectionsTypes;
    public static String songSection_holder; // This carries on section types after line breaks
    public static float[] sectionScaleValue;
    public static int currentSection;
    public static String[][] projectedContents;
    public static String[][] projectedLineTypes;


    public static String pagebutton_position = "right";
    public static boolean grouppagebuttons = false;

    public static File root = Environment.getExternalStorageDirectory();
    public static File homedir = new File(root.getAbsolutePath() + "/documents/OpenSong");
    public static File dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs");
    public static File dirsettings = new File(root.getAbsolutePath() + "/documents/OpenSong/Settings");
    public static File dironsong = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs/OnSong");
    public static File dirsets = new File(root.getAbsolutePath() + "/documents/OpenSong/Sets");
    public static File dirPads = new File(root.getAbsolutePath() + "/documents/OpenSong/Pads");
    public static File dirbackgrounds = new File(root.getAbsolutePath() + "/documents/OpenSong/Backgrounds");
    public static File dirbibles = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture");
    public static File dirbibleverses = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture/_cache");
    public static File dirscripture = new File(root.getAbsolutePath() + "/documents/OpenSong/Scripture/");
    public static File dirscriptureverses = new File(root.getAbsolutePath() + "/documents/OpenSong/Scripture/_cache");
    public static File dircustomslides = new File(root.getAbsolutePath() + "/documents/OpenSong/Slides/_cache");
    public static File dircustomnotes = new File(root.getAbsolutePath() + "/documents/OpenSong/Notes/_cache");
    public static File dircustomimages = new File(root.getAbsolutePath() + "/documents/OpenSong/Images/_cache");
    public static File dirvariations = new File(root.getAbsolutePath() + "/documents/OpenSong/Variations");
    public static File dirprofiles = new File(root.getAbsolutePath() + "/documents/OpenSong/Profiles");

    public static Locale locale;

    public static boolean firstload = true;

    //public static String[][][] bibleVerse; // bibleVerse[book][chapter#][verse#]

    //public static ArrayList<String> deviceList;
    //public static WifiP2pDeviceList wifiP2PDeviceList;
    //public static boolean isHost = true;
    //public static WifiP2pManager mManager;
    //public static WifiP2pManager.Channel mChannel;
    //public static BroadcastReceiver mReceiver;
    //public static IntentFilter mIntentFilter;
    //public static WifiP2pManager.PeerListListener mWifiP2PListener;
    //public static NsdServiceInfo serviceInfo;
    //public static String deviceConnectedTo = "";

    @SuppressLint("StaticFieldLeak")
    public static Button hostButton;
    public static String hostButtonText="";
    @SuppressLint("StaticFieldLeak")
    public static Button clientButton;
    public static String clientButtonText="";
    @SuppressLint("StaticFieldLeak")
    public static TextView connectionsLog;
    public static String salutLog="";
    @SuppressLint("StaticFieldLeak")
    public static SalutDataReceiver dataReceiver;
    public static SalutServiceData serviceData;
    public static Salut network;
    public static BluetoothAdapter mBluetoothAdapter;
    public static String mBluetoothName;
    public static boolean firstSendingOfSalut = true;
    public static boolean firstSendingOfSalutXML = true;
    public static boolean firstReceivingOfSalut = true;
    public static boolean firstReceivingOfSalutXML = true;
    public static String mySalutXML = "";

    static NfcAdapter mNfcAdapter;
    // Flag to indicate that Android Beam is available
    public static boolean mAndroidBeamAvailable  = false;

    // Just for the popups - let StageMode or PresenterMode try to deal with them
    // @Override
    public void refreshAll() {
        Log.d("d","refreshAll() called from FullscreenActivity");
    }

    @Override
    public void onSongImportDone(String message) {
        Log.d("d","onSongImportDone() called from FullscreenActivity");
    }

    @Override
    public void backupInstall(String message) {
        Log.d("d","backupInstall() called from FullscreenActivity");
    }

    @Override
    public void openFragment() {
        Log.d("d","openFragment() called from FullscreenActivity");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myPreferences = getPreferences(MODE_PRIVATE);
        Preferences.loadPreferences();

        mainfoldername = getResources().getString(R.string.mainfoldername);

        // If the song was loaded last time correctly, then we are good to continue
        // If it didn't load, then reset the starting song and folder
        if (!Preferences.wasSongLoaded()) {
            whichSongFolder = "";
            songfilename = "Welcome to OpenSongApp";
        }

        // If whichSongFolder is empty, reset to main
        if (whichSongFolder == null || whichSongFolder.isEmpty() || whichSongFolder.equals("")) {
            whichSongFolder = mainfoldername;
            Preferences.savePreferences();
        }
        locale = Locale.getDefault();
        if  (locale==null) {
            locale = new Locale(Locale.getDefault().getDisplayLanguage());
        }

        if (!locale.toString().equals("af") && !locale.toString().equals("cz") && !locale.toString().equals("de") &&
                !locale.toString().equals("el") && !locale.toString().equals("es") && !locale.toString().equals("fr") &&
                !locale.toString().equals("hu") && !locale.toString().equals("it") && !locale.toString().equals("ja") &&
                !locale.toString().equals("pl") && !locale.toString().equals("pt") && !locale.toString().equals("ru") &&
                !locale.toString().equals("sr") && !locale.toString().equals("zh")) {
            locale = new Locale("en");
        }

        // Get the song folders
        ListSongFiles.getAllSongFolders();

        // Try language locale change
        if (!languageToLoad.isEmpty()) {
            locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        //gestureDetector = new GestureDetector(new SwipeDetector());

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Load up the translations
        //variation_edit = getResources().getString(R.string.variation_edit);
        //set_loading = getResources().getString(R.string.set_loading);
        //set_processing = getResources().getString(R.string.set_processing);
        //set = getResources().getString(R.string.options_set);
        //song = getResources().getString(R.string.options_song);
        //slide = getResources().getString(R.string.slide);
        //scripture = getResources().getString(R.string.scripture);
        //note = getResources().getString(R.string.note);
        //image = getResources().getString(R.string.image);
        //variation = getResources().getString(R.string.variation);

        timesigs = getResources().getStringArray(R.array.timesig);

        //tag_verse = getResources().getString(R.string.tag_verse);
        //tag_chorus = getResources().getString(R.string.tag_chorus);
        //tag_prechorus = getResources().getString(R.string.tag_prechorus);
        //tag_bridge = getResources().getString(R.string.tag_bridge);
        //tag_tag = getResources().getString(R.string.tag_tag);

        //edit_song_presentation = getResources().getString(R.string.edit_song_presentation);
        //error_notset = getResources().getString(R.string.error_notset);
        //error_missingsection = getResources().getString(R.string.error_missingsection);

        //toastmessage_maxfont = getResources().getString(R.string.toastmessage_maxfont);
        //toastmessage_minfont = getResources().getString(R.string.toastmessage_minfont);
        //backtooptions = getResources().getString(R.string.options_backtooptions);
        //savethisset = getResources().getString(R.string.options_savethisset);
        //clearthisset = getResources().getString(R.string.options_clearthisset);

        //set_edit = getResources().getString(R.string.set_edit);
        //set_save = getResources().getString(R.string.set_save);
        //set_load = getResources().getString(R.string.set_load);
        //set_clear = getResources().getString(R.string.set_clear);
        //set_export = getResources().getString(R.string.set_export);

        //sethasbeendeleted = getResources().getString(R.string.sethasbeendeleted);
        //deleteerror_start = getResources().getString(R.string.deleteerror_start);
        //deleteerror_end_song = getResources().getString(R.string.deleteerror_end_song);
        //deleteerror_end_sets = getResources().getString(R.string.deleteerror_end_sets);
        //songdoesntexist = getResources().getString(R.string.songdoesntexist);
        //exportcurrentsong = getResources().getString(R.string.exportcurrentsong);
        //importnewsong = getResources().getString(R.string.importnewsong);
        //exportsavedset = getResources().getString(R.string.exportsavedset);
        //importnewset = getResources().getString(R.string.importnewset);
        mainfoldername = getResources().getString(R.string.mainfoldername);


        // Test for NFC capability
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mAndroidBeamAvailable = false;
        } else {
            mAndroidBeamAvailable = true;
            try {
                mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            } catch (Exception e) {
                mAndroidBeamAvailable = false;
            }
            if (mNfcAdapter==null) {
                mAndroidBeamAvailable = false;
            }
        }
        Log.d("d","NFC available="+mAndroidBeamAvailable);
        Log.d("d","NFC adapter="+mNfcAdapter);

        whattodo = "";
        try {
            incomingfile = getIntent();
            if (incomingfile != null) {
                file_location = incomingfile.getData().getPath();
                filechosen = new File(incomingfile.getData().getPath());
                file_name = incomingfile.getData().getLastPathSegment();
                file_uri = incomingfile.getData();
                if (file_name.endsWith(".osb")) {
                    whattodo = "processimportosb";
                } else {
                    whattodo = "doimport";
                }
            }
        } catch (Exception e) {
            // No file
            //needtoimport = false;
        }

        // Initialise api
        currentapiVersion = Build.VERSION.SDK_INT;

        // Capable of dual head presentations
        dualDisplayCapable = currentapiVersion >= 17;

        // Set up the available typefaces
        // Initialise the typefaces available
        typeface0 = Typeface.DEFAULT;
        typeface1 = Typeface.MONOSPACE;
        typeface2 = Typeface.SANS_SERIF;
        typeface3 = Typeface.SERIF;
        typeface4 = Typeface.createFromAsset(getAssets(), "fonts/FiraSansOT-Light.otf");
        typeface4i = Typeface.createFromAsset(getAssets(), "fonts/FiraSans-LightItalic.otf");
        typeface5 = Typeface.createFromAsset(getAssets(), "fonts/FiraSansOT-Regular.otf");
        typeface5i = Typeface.createFromAsset(getAssets(), "fonts/FiraSans-Italic.otf");
        typeface6 = Typeface.createFromAsset(getAssets(), "fonts/KaushanScript-Regular.otf");
        typeface7 = Typeface.createFromAsset(getAssets(), "fonts/Lato-Lig.ttf");
        typeface7i = Typeface.createFromAsset(getAssets(), "fonts/Lato-LigIta.ttf");
        typeface8 = Typeface.createFromAsset(getAssets(), "fonts/Lato-Reg.ttf");
        typeface8i = Typeface.createFromAsset(getAssets(), "fonts/Lato-RegIta.ttf");
        typeface9 = Typeface.createFromAsset(getAssets(), "fonts/LeagueGothic-Regular.otf");
        typeface9i = Typeface.createFromAsset(getAssets(), "fonts/LeagueGothic-Italic.otf");
        typeface10 = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        typeface10i = Typeface.createFromAsset(getAssets(), "fonts/Roboto-LightItalic.ttf");
        typeface11 = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        typeface11i = Typeface.createFromAsset(getAssets(), "fonts/Roboto-ThinItalic.ttf");
        typeface12 = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        typeface12i = Typeface.createFromAsset(getAssets(), "fonts/Roboto-MediumItalic.ttf");

        // Set up the user preferences for page colours and fonts
        SetUpColours.colours();
        SetTypeFace.setTypeface();

        // Copy assets (background images)
        PopUpStorageFragment.copyAssets(FullscreenActivity.this);

        // Prepare import varaibale
        // Copy the default files into the image folder (from assets)


        // If whichMode is Presentation, open that app instead
        switch (whichMode) {
            case "Presentation":
                Intent performmode = new Intent();
                performmode.setClass(FullscreenActivity.this, PresenterMode.class);
                startActivity(performmode);
                finish();
                break;
            case "Stage": {
                Intent stagemode = new Intent();
                stagemode.setClass(FullscreenActivity.this, StageMode.class);
                startActivity(stagemode);
                finish();
                break;
            }
            case "Performance":
            default: {
                Intent stagemode = new Intent();
                stagemode.setClass(FullscreenActivity.this, StageMode.class);
                startActivity(stagemode);
                finish();
                break;
            }
        }

        finish();
    }

}