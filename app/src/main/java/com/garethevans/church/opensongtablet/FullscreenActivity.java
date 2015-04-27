package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("deprecation")
@SuppressLint({ "DefaultLocale", "InlinedApi", "RtlHardcoded", "NewApi", "InflateParams", "SdCardPath" })
public class FullscreenActivity extends Activity {
    /** First up, declare all of the variables needed by this application **/

    // Identify the chord images
    Drawable f1;
    Drawable f2;
    Drawable f3;
    Drawable f4;
    Drawable f5;
    Drawable f6;
    Drawable f7;
    Drawable f8;
    Drawable f9;
    Drawable lx;
    Drawable l0;
    Drawable l1;
    Drawable l2;
    Drawable l3;
    Drawable l4;
    Drawable l5;
    Drawable mx;
    Drawable m0;
    Drawable m1;
    Drawable m2;
    Drawable m3;
    Drawable m4;
    Drawable m5;
    Drawable rx;
    Drawable r0;
    Drawable r1;
    Drawable r2;
    Drawable r3;
    Drawable r4;
    Drawable r5;
    TableLayout chordimageshere;
    static String chordInstrument = "g";

    static String allchords = "";
    static String allchordscapo = "";
    static String[] allchords_array = null;
    static ArrayList<String> unique_chords = null;
    public static String chordnotes = "";
    static String capoDisplay;
    static String languageToLoad;
    static String tempLanguage;
    SQLiteDatabase db;
    String[] backUpFiles;
    String backupchosen = "";

    static int currentapiVersion;

    static String emailtext;

    ScrollView popupChord;
    Spinner popupChord_Instrument;

    ScrollView popupPad;
    static String popupPad_stoporstart = "stop";
    Spinner popupPad_key;
    SeekBar popupPad_volume;
    TextView popupPad_volume_text;
    SeekBar popupPad_pan;
    TextView popupPad_pan_text;
    Button popupPad_startstopbutton ;

    ScrollView popupAutoscroll;
    static String popupAutoscroll_stoporstart = "stop";
    static int popupAutoscroll_tempduration;
    SeekBar popupAutoscroll_delay;
    TextView popupAutoscroll_delay_text;
    TextView popupAutoscroll_duration;
    Button popupAutoscroll_startstopbutton;
    static int newPos;

    ScrollView popupMetronome;
    static String popupMetronome_stoporstart = "stop";
    Spinner popupMetronome_timesig;
    SeekBar popupMetronome_tempo;
    TextView popupMetronome_tempo_text;
    SeekBar popupMetronome_volume;
    TextView popupMetronome_volume_text;
    SeekBar popupMetronome_pan;
    TextView popupMetronome_pan_text;
    Button popupMetronome_startstopbutton ;
    int beatoffcolour = 0xff121222;
    String whichbeat = "a";
    static boolean visualmetronome = false;
    ToggleButton popupMetronome_visualmetronometoggle;

    public boolean fadeout1 = false;
    public boolean fadeout2 = false;
    private final short minBpm = 40;
    private final short maxBpm = 199;
    private short bpm = 100;
    private short noteValue = 4;
    private short beats = 4;
    public int currentBeat = 1;
    @SuppressWarnings("unused")
    private short volume;
    private float metrovol;
    @SuppressWarnings("unused")
    private short initialVolume;
    private double beatSound = 1200; //1200
    private double sound = 1600; //1600
    @SuppressWarnings("unused")
    private AudioManager audio;
    private MetronomeAsyncTask metroTask;
    private Handler mHandler;
    @SuppressLint("HandlerLeak")
    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //Sring message = (String)msg.obj;
                //metronomebeat.setText(message);
                if (visualmetronome) {
                    if (whichbeat.equals("a")) {
                        whichbeat = "b";
                        //metronomebeat.setBackgroundDrawable(beat_a);
                        getActionBar().setBackgroundDrawable(new ColorDrawable(beatoffcolour));
                        getActionBar().setDisplayShowTitleEnabled(false);
                        getActionBar().setDisplayShowTitleEnabled(true);
                    } else {
                        whichbeat = "a";
                        //metronomebeat.setBackgroundDrawable(beat_b);					
                        getActionBar().setBackgroundDrawable(new ColorDrawable(metronomeColor));
                        getActionBar().setDisplayShowTitleEnabled(false);
                        getActionBar().setDisplayShowTitleEnabled(true);

                    }
                }
            }
        };
    }

    public static String metronomeonoff = "off";
    public static String metronomepan = "both";
    public static float metronomevol = 1.0f;
    public static float padvol = 1.0f;
    public static String padpan = "both";

    public int autoscroll_pause_time = 2000;
    public boolean pauseautoscroll = true;
    public boolean autoscrollispaused = false;
    public static boolean isautoscrolling = false;
    public static float autoscroll_pixels = 0.0f;
    public static int song_duration = 0;
    public static int total_pixels_to_scroll = 0;
    public static String autoscrollonoff = "false";
    public static String pad_filename = "null";
    public static boolean killfadeout1 = false;
    public static boolean killfadeout2 = false;
    public static boolean isfading1 = false;
    public static boolean isfading2 = false;
    public static boolean padson = false;
    public static boolean needtorefreshsongmenu = false;
    public static MediaPlayer mPlayer1 = null;
    public static MediaPlayer mPlayer2 = null;
    public static ImageView padButton;
    public static ImageView linkButton;
    public static ImageView chordButton;
    public static ImageView autoscrollButton;
    public static ImageView metronomeButton;
    public static boolean padAcrossSet = false;
    public static boolean padPlayingToggle = false;
    public static boolean orientationchanged = false;
    public static int wasshowing_uparrow;
    public static int wasshowing_downarrow;
    public static int wasshowing_pdfselectpage;
    public static int wasshowing_stickynotes;
    public static int alreadyshowingpage;

    public static int pdfPageCurrent = 0;
    public static int pdfPageCount = 0;
    public static boolean isPDF = false;
    public static boolean isSong = false;
    public static ImageView pdf_selectpage;
    public static ImageView stickynotes;
    public static TextView mySticky;
    public static ScrollView scrollstickyholder;
    public static String hideactionbaronoff;
    public int lastExpandedGroupPositionOption;
    public int lastExpandedGroupPositionSong;
    public static String[][] childSongs;
    public static String setnamechosen;
    public static boolean addingtoset = false;
    public static boolean removingfromset = false;
    static int fontsizeseekar;
    static int pageseekbarpos;

    public View presseditem;

    static Handler delayautoscroll;

    // Swipe
    private static final int SWIPE_MIN_DISTANCE = 250;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 800;
    private GestureDetector gestureDetector;

    static String whichMode;

    // Views and bits on the pages
    static int mScreenOrientation;
    static int columnTest = 1;
    static float onecolfontsize;
    static float twocolfontsize;
    static float threecolfontsize;
    ScrollView scrollpage;
    ScrollView scrollpage_pdf;
    ScrollView scrollpage_onecol;
    ScrollView scrollpage_twocol;
    ScrollView scrollpage_threecol;
    static View view;
    static int maxcharsinline;
    static ActionBarDrawerToggle actionBarDrawerToggle;
    static Handler delayactionBarHide;
    static Handler delaycheckscroll;
    static Handler doautoScroll;

    static boolean scrollbutton = false;
    static boolean actionbarbutton = false;

    // Font sizes (relative)
    boolean alreadyloaded = false;
    float mainfontsize = 16;
    float sectionfontsize = 10;
    static int linespacing;
    float tempfontsize;
    float oldtempfontsize;
    float tempsectionsize;
    float oldtempsectionsize;
    boolean doScaling = false;
    float scaleX;
    float scaleY;
    float bestfontsize;
    float bestcolumnnumber;
    float pageWidth;
    float pageHeight;
    boolean needtoredraw = true;
    boolean doanimate = false;

    // Colours
    static int dark_background;
    static int dark_lyricsTextColor;
    static int dark_lyricsBackgroundColor;
    static int dark_lyricsVerseColor;
    static int dark_lyricsChorusColor;
    static int dark_lyricsBridgeColor;
    static int dark_lyricsCommentColor;
    static int dark_lyricsPreChorusColor;
    static int dark_lyricsTagColor;
    static int dark_lyricsChordsColor;
    static int dark_lyricsCustomColor;
    static int dark_lyricsCapoColor;
    static int dark_metronome;

    static int light_background;
    static int light_lyricsTextColor;
    static int light_lyricsBackgroundColor;
    static int light_lyricsVerseColor;
    static int light_lyricsChorusColor;
    static int light_lyricsBridgeColor;
    static int light_lyricsCommentColor;
    static int light_lyricsPreChorusColor;
    static int light_lyricsTagColor;
    static int light_lyricsChordsColor;
    static int light_lyricsCustomColor;
    static int light_lyricsCapoColor;
    static int light_metronome;

    static int custom1_background;
    static int custom1_lyricsTextColor;
    static int custom1_lyricsBackgroundColor;
    static int custom1_lyricsVerseColor;
    static int custom1_lyricsChorusColor;
    static int custom1_lyricsBridgeColor;
    static int custom1_lyricsCommentColor;
    static int custom1_lyricsPreChorusColor;
    static int custom1_lyricsTagColor;
    static int custom1_lyricsChordsColor;
    static int custom1_lyricsCustomColor;
    static int custom1_lyricsCapoColor;
    static int custom1_metronome;

    static int custom2_background;
    static int custom2_lyricsTextColor;
    static int custom2_lyricsBackgroundColor;
    static int custom2_lyricsVerseColor;
    static int custom2_lyricsChorusColor;
    static int custom2_lyricsBridgeColor;
    static int custom2_lyricsCommentColor;
    static int custom2_lyricsPreChorusColor;
    static int custom2_lyricsTagColor;
    static int custom2_lyricsChordsColor;
    static int custom2_lyricsCustomColor;
    static int custom2_lyricsCapoColor;
    static int custom2_metronome;


    static int lyricsBoxColor;
    static int lyricsTextColor;
    static int lyricsBackgroundColor;
    static int lyricsChorusColor;
    static int lyricsVerseColor;
    static int lyricsBridgeColor;
    static int lyricsCommentColor;
    static int lyricsPreChorusColor;
    static int lyricsTagColor;
    static int lyricsChordsColor;
    static int lyricsCustomColor;
    static int lyricsCapoColor;
    static int newbgColor;
    static int metronomeColor;

    // Page turner
    static int pageturner_NEXT;
    static int pageturner_PREVIOUS;
    static int pageturner_UP;
    static int pageturner_DOWN;
    static int pageturner_PAD;
    static int pageturner_AUTOSCROLL;
    static int pageturner_METRONOME;
    static String toggleScrollBeforeSwipe;
    static String togglePageButtons;

    // Set variables
    static int autoScrollDelay;
    static int temp_autoScrollDelay = 0;
    static int make_temp_autoScrollDelay = 0;
    public static String prefStorage;
    static boolean wasscrolling = false;
    static String alwaysPreferredChordFormat;
    static String gesture_doubletap;
    static String gesture_longpress;
    static String bibleFile;
    static boolean bibleLoaded = false;
    static String bibleFileContents;
    static String[] bibleBooks;
    static String chordFormat;
    static String chord_converting = "N";
    static String oldchordformat;
    static String presenterChords;
    static String toggleColumns;
    static String swipeDrawer;
    static String swipeSet;
    static String tempswipeSet = "enable";
    static String whichDirection = "R2L";
    public static int editSetSelectedIndex;
    public static String editSetSelectedValue;
    public static String editSetPreviousValue;
    public static String editSetNextValue;
    public static String[] tempSet;
    static int indexSongInSet;
    static String previousSongInSet;
    static String nextSongInSet;
    static String mTheme;
    public static String mDisplayTheme = "Theme.Holo";
    static String setView = "N";
    MenuItem set_back;
    MenuItem set_forward;
    MenuItem presentationMode;
    static int setSize;
    static boolean showingSetsToLoad = false;
    static String whatsongforsetwork = "";
    static String mySet;
    static String newSetContents;
    static String[] mSet;
    static String[] mSetList;
    static View currentView;

    // Song filenames, folders, set filenames, folders
    static String currentFolder;
    static String newFolder;
    static int next_song;
    static int prev_song;
    static String whichSongFolder;
    static String textWhichSongFolder;
    static String[] mySetsFileNames;
    static File[] mySetsFiles;
    static String[] mySetsFolderNames;
    static File[] mySetsDirectories;
    static File[] myFiles;
    static File[] myDirectories;
    static File file;
    static File setfile;
    static String settoload;
    static String settodelete;
    static String[] mSongTitles;
    static String[] mSongTitlesForSorting;
    static String[] mSongFileNames;
    static String[] mSongFolderNames;
    static String[] search_songid;
    static String[] search_content;

    static String[] mTempFolderNames;

    static int currentSongIndex;
    static int previousSongIndex;
    static int nextSongIndex;

    // Presentation mode variables
    static String dualDisplayCapable = "N";
    static int numdisplays;
    static String backgroundImage1;
    static String backgroundImage2;
    static String backgroundVideo1;
    static String backgroundVideo2;
    static String backgroundToUse;
    static String backgroundTypeToUse;
    static int xmargin_presentation;
    static int ymargin_presentation;

    // Song xml data
    static CharSequence mTitle = "";
    static CharSequence mTempTitle = "";
    static CharSequence mAuthor = "Gareth Evans";
    static String mTempAuthor = "";
    static CharSequence mCopyright = "";
    static String mLyrics = "";
    static String mCCLI = "";
    static String mAltTheme = "";
    static String mPresentation = "";
    static String mHymnNumber = "";
    static String mUser1 = "";
    static String mUser2 = "";
    static String mUser3 = "";
    static String mKey = "";
    static String mAka = "";
    static String mKeyLine = "";
    static String mStyle = "";
    static String mStyleIndex = "";
    static String mCapo = "";
    static String mCapoPrint = "";
    static String mTempo = "";
    static String mTimeSig = "";
    static String mDuration = "";
    static String mBooks = "";
    static String mMidi = "";
    static String mMidiIndex = "";
    static String mPitch = "";
    static String mRestrictions = "";
    static String mNotes = "";
    static String temptranspChords = "";
    static String tempChords = "";
    static String mLinkedSongs = "";
    static String mExtraStuff1 = "";
    static String mExtraStuff2 = "";
    static String mPadFile = "";

    // Info for the lyrics table
    static float mScaleFactor = 1.0f;
    ScaleGestureDetector scaleGestureDetector;
    static BitmapDrawable[] songSectionBitmap;
    static float scaleonecolview;
    static float scaletwocolview;
    static float scalethreecolview;
    static int onecolview_width;
    static int onecolview_height;
    static int twocolview_width;
    static int twocolview_height;
    static int threecolview_width;
    static int threecolview_height;
    static boolean botherwithcolumns;
    static int numcolstouse = 1;
    static int splitpoint;
    static int thirdsplitpoint;
    static int twothirdsplitpoint;
    static String whatvieworientation;
    static String[] whatisthisblock;
    static String[] whatisthisline;
    static String mFileToLoad;
    static String mStorage;
    static String myLyrics;
    static float mFontSize;
    static String toggleYScale;
    static String mySetXML;
    static String[] myParsedSet;
    static String myXML;
    static String myTempXML;
    static String mynewXML;
    static String[] myParsedLyrics;
    static String[] myTransposedLyrics;
    static String songfilename;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ListView mOptionList;
    ActionBarDrawerToggle mDrawerToggle;
    private Menu menu;
    static String linkclicked;
    static int myOptionListClickedItem;
    static String whichOptionsView = "options_main";
    static String changeWhichOptionsView;
    static SharedPreferences myPreferences;
    static int lyricsstartpos;
    static int lyricsendpos;
    static int numrowstowrite;
    static int authstartpos;
    static int authendpos;
    static int copystartpos;
    static int copyendpos;
    static int titlestartpos;
    static int titleendpos;
    static int temptitlestartpos;
    static int temptitleendpos;
    static String transposeDirection = "0";
    static String[] transposeSteps = {"-6","-5","-4","-3","-2","-1","+1","+2","+3","+4","+5","+6"};
    static String[] keyChoice = {"A","A#","Bb","B","C","C#","Db","D","D#","Eb","E","F","F#","Gb","G","G#","Ab","Am","A#m","Bbm","Bm","Cm","C#m","Dbm","Dm","D#m","Ebm","Em","Fm","F#m","Gbm","Gm","G#m","Abm"};
    static int transposeTimes = 1;
    static String transposeStyle = "sharps";
    static String transposedLyrics;
    static String showChords;
    TableLayout testTable;
    LinearLayout lyricstableall;
    LinearLayout lyricstableall_onecol;
    LinearLayout lyricstableall_twocol;
    LinearLayout lyricstableall_threecol;
    TableLayout lyricstable_onecolview;
    TableLayout lyricstable_twocolview;
    TableLayout lyricstable2_twocolview;
    TableLayout lyricstable_threecolview;
    TableLayout lyricstable2_threecolview;
    TableLayout lyricstable3_threecolview;
    static ImageView uparrow;
    static ImageView downarrow;
    static String myToastMessage;
    static boolean showCapo;
    static TextView top_songtitle;

    // Search function
    static String mySearchText;

    // The following get in translation texts
    static String set;
    static String song;
    static String slide;
    static String scripture;
    static String unknown_format;
    static String toastmessage_maxfont;
    static String toastmessage_minfont;
    static String set_menutitle;
    static String setload_menutitle;
    static String setexport_menutitle;
    static String song_menutitle;
    static String options_menutitle;
    static String backtooptions;
    static String savethisset;
    static String clearthisset;
    static String set_edit;
    static String set_save;
    static String set_load;
    static String set_clear;
    static String set_export;
    static String menu_menutitle;
    static String sethasbeendeleted;
    static String deleteerror_start;
    static String deleteerror_end_song;
    static String deleteerror_end_sets;
    static String songdoesntexist;
    static String exportcurrentsong;
    static String importnewsong;
    static String exportsavedset;
    static String importnewset;
    static String mainfoldername;
    static int mylyricsfontnum;
    static int mychordsfontnum;
    Typeface lyricsfont;
    Typeface commentfont;
    Typeface chordsfont;
    static Animation animationFadeIn;
    static Animation animationFadeOut;
    static View main_page;
    static View main_lyrics;
    static View songLoadingProgressBar;
    static View onsongImportProgressBar;
    static TextView songTitleHolder;

    static Runnable hideActionBarRunnable;
    static Runnable hideButtonRunnable;
    static Runnable checkScrollPosition;
    static Runnable autoScrollRunnable;

    ExpandableListAdapter listAdapterOption;
    ExpandableListView expListViewOption;
    List<String> listDataHeaderOption;
    HashMap<String, List<String>> listDataChildOption;
    ExpandableListAdapter listAdapterSong;
    ExpandableListView expListViewSong;
    List<String> listDataHeaderSong;
    HashMap<String, List<String>> listDataChildSong;
    private FadeOutMusic1 mtask_fadeout_music1;
    private FadeOutMusic2 mtask_fadeout_music2;
    private AutoScrollMusic mtask_autoscroll_music;
    public static int whichtofadeout;

    // Try to determine internal or external storage
    String secStorage = System.getenv("SECONDARY_STORAGE");
    String defStorage = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
    String[] secStorageOptions = {"/mnt/emmc/",
            "/FAT",
            "/Removable/MicroSD",
            "/Removable/SD",
            "/data/sdext2",
            "/sdcard/sd",
            "/mnt/flash",
            "/mnt/sdcard/tflash",
            "/mnt/nand",
            "/mnt/external1",
            "/mnt/sdcard-ext",
            "/mnt/extsd",
            "/mnt/sdcard2",
            "/mnt/sdcard/sdcard1",
            "/mnt/sdcard/sdcard2",
            "/mnt/sdcard/ext_sd",
            "/mnt/sdcard/_ExternalSD",
            "/mnt/sdcard/external_sd",
            "/mnt/sdcard/SD_CARD",
            "/mnt/sdcard/removable_sdcard",
            "/mnt/sdcard/external_sdcard",
            "/mnt/sdcard/extStorages/SdCard",
            "/mnt/ext_card",
            "/mnt/extern_sd",
            "/mnt/ext_sdcard",
            "/mnt/ext_sd",
            "/mnt/external_sd",
            "/mnt/external_sdcard",
            "/mnt/extSdCard"};

    boolean extStorageExists = false;
    boolean defStorageExists = false;

    public static File extStorCheck;
    public static File intStorCheck;

    boolean storageIsValid = true;

    static File root = android.os.Environment.getExternalStorageDirectory();
    static File homedir = new File(root.getAbsolutePath() + "/documents/OpenSong");
    static File dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs");
    static File dironsong = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs/OnSong");
    static File dirsets = new File(root.getAbsolutePath() + "/documents/OpenSong/Sets");
    static File dirPads = new File(root.getAbsolutePath() + "/documents/OpenSong/Pads");
    static File dirbackgrounds = new File(root.getAbsolutePath() + "/documents/OpenSong/Backgrounds");
    static File dirbibles = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture");
    static File dirbibleverses = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture/_cache");
    static File dircustomslides = new File(root.getAbsolutePath() + "/documents/OpenSong/Slides/_cache");


    static String[][][] bibleVerse; // bibleVerse[book][chapter#][verse#]

    /** Now on to the code! **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load up the user preferences
        myPreferences = getPreferences(MODE_PRIVATE);
        Preferences.loadPreferences();

        // Try language locale change
        if (!languageToLoad.isEmpty()) {
            Locale locale = null;
            locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }


        gestureDetector = new GestureDetector(new SwipeDetector());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Load up the translations
        set = getResources().getString(R.string.options_set);
        song = getResources().getString(R.string.options_song);
        slide = getResources().getString(R.string.slide);
        scripture = getResources().getString(R.string.scripture);

        toastmessage_maxfont = getResources().getString(R.string.toastmessage_maxfont);
        toastmessage_minfont = getResources().getString(R.string.toastmessage_minfont);
        backtooptions = getResources().getString(R.string.options_backtooptions);
        savethisset = getResources().getString(R.string.options_savethisset);
        clearthisset = getResources().getString(R.string.options_clearthisset);

        set_edit = getResources().getString(R.string.set_edit);
        set_save = getResources().getString(R.string.set_save);
        set_load = getResources().getString(R.string.set_load);
        set_clear = getResources().getString(R.string.set_clear);
        set_export = getResources().getString(R.string.set_export);

        sethasbeendeleted = getResources().getString(R.string.sethasbeendeleted);
        deleteerror_start = getResources().getString(R.string.deleteerror_start);
        deleteerror_end_song = getResources().getString(R.string.deleteerror_end_song);
        deleteerror_end_sets = getResources().getString(R.string.deleteerror_end_sets);
        songdoesntexist = getResources().getString(R.string.songdoesntexist);
        exportcurrentsong = getResources().getString(R.string.exportcurrentsong);
        importnewsong = getResources().getString(R.string.importnewsong);
        exportsavedset = getResources().getString(R.string.exportsavedset);
        importnewset = getResources().getString(R.string.importnewset);
        mainfoldername = getResources().getString(R.string.mainfoldername);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion >= 17) {
            // Capable of dual head presentations
            dualDisplayCapable = "Y";
        }

        // If the user hasn't set the preferred storage location
        // Or the default storage location isn't available, ask them!
        checkDirectories();

        // If whichMode is Presentation, open that app instead
        if (whichMode.equals("Presentation") && dualDisplayCapable.equals("Y")) {
            Intent performmode = new Intent();
            performmode.setClass(FullscreenActivity.this, PresentMode.class);
            startActivity(performmode);
            finish();
        }

        // Load the songs
        ListSongFiles.listSongs();

        // Get the song indexes
        ListSongFiles.getCurrentSongIndex();

        // Load the page
        setContentView(R.layout.activity_fullscreen_table);

        // Make the ActionBar translucent
        ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        mActionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(200, 20, 20, 40)));

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar, null);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        top_songtitle = (TextView) findViewById(R.id.songtitle_top);
        top_songtitle.setText("OpenSong\nGareth Evans");
        // Set up runnable used to hide the actionbar
        delayactionBarHide = new Handler();
        delaycheckscroll = new Handler();
        doautoScroll = new Handler();


        hideActionBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (getActionBar().isShowing()) {
                    getActionBar().hide();
                }
            }};

        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                scrollpage.smoothScrollBy(0,(int) autoscroll_pixels);
            }};


        if (mAuthor.equals("")) {
            mTempAuthor = "Unknown";
        } else {
            mTempAuthor = mAuthor.toString();
        }

        //getActionBar().setTitle(songfilename + " by " + mAuthor);
        top_songtitle.setText(songfilename + "\n" + mAuthor);
        main_page = (RelativeLayout) findViewById(R.id.main_page);
        // Set a listener for the main_page.
        // If a popup is open, clicking on the main page will hide it.
        songTitleHolder = (TextView) findViewById(R.id.songTitleHolder);
        songLoadingProgressBar = (ProgressBar) findViewById(R.id.songLoadingProgressBar);
        onsongImportProgressBar = (ProgressBar) findViewById(R.id.mainProgressBar);
        scrollpage_pdf = (ScrollView) findViewById(R.id.scrollpage_pdf);
        pdf_selectpage = (ImageView) findViewById(R.id.pageselect);
        stickynotes = (ImageView) findViewById(R.id.stickynotes);
        padButton = (ImageView) findViewById(R.id.padbutton);
        metronomeButton = (ImageView) findViewById(R.id.metronomebutton);
        linkButton = (ImageView) findViewById(R.id.linkbutton);
        autoscrollButton = (ImageView) findViewById(R.id.autoscroll);
        scrollstickyholder = (ScrollView) findViewById(R.id.scrollstickyholder);
        mySticky = (TextView) findViewById(R.id.mySticky);
        scrollpage_onecol = (ScrollView) findViewById(R.id.scrollpage_onecol);
        scrollpage_twocol = (ScrollView) findViewById(R.id.scrollpage_twocol);
        scrollpage_threecol = (ScrollView) findViewById(R.id.scrollpage_threecol);
        lyricstable_onecolview = (TableLayout) findViewById(R.id.LyricDisplay_onecoldisplay);
        lyricstable_twocolview = (TableLayout) findViewById(R.id.LyricDisplay_twocoldisplay);
        lyricstable2_twocolview = (TableLayout) findViewById(R.id.LyricDisplayCol2_twocoldisplay);
        lyricstable_threecolview = (TableLayout) findViewById(R.id.LyricDisplay_threecoldisplay);
        lyricstable2_threecolview = (TableLayout) findViewById(R.id.LyricDisplayCol2_threecoldisplay);
        lyricstable3_threecolview = (TableLayout) findViewById(R.id.LyricDisplayCol3_threecoldisplay);

        // Identify the chord images
        f1 = (Drawable) getResources().getDrawable(R.drawable.chord_f1);
        f2 = (Drawable) getResources().getDrawable(R.drawable.chord_f2);
        f3 = (Drawable) getResources().getDrawable(R.drawable.chord_f3);
        f4 = (Drawable) getResources().getDrawable(R.drawable.chord_f4);
        f5 = (Drawable) getResources().getDrawable(R.drawable.chord_f5);
        f6 = (Drawable) getResources().getDrawable(R.drawable.chord_f6);
        f7 = (Drawable) getResources().getDrawable(R.drawable.chord_f7);
        f8 = (Drawable) getResources().getDrawable(R.drawable.chord_f8);
        f9 = (Drawable) getResources().getDrawable(R.drawable.chord_f9);
        lx = (Drawable) getResources().getDrawable(R.drawable.chord_l_x);
        l0 = (Drawable) getResources().getDrawable(R.drawable.chord_l_0);
        l1 = (Drawable) getResources().getDrawable(R.drawable.chord_l_1);
        l2 = (Drawable) getResources().getDrawable(R.drawable.chord_l_2);
        l3 = (Drawable) getResources().getDrawable(R.drawable.chord_l_3);
        l4 = (Drawable) getResources().getDrawable(R.drawable.chord_l_4);
        l5 = (Drawable) getResources().getDrawable(R.drawable.chord_l_5);
        mx = (Drawable) getResources().getDrawable(R.drawable.chord_m_x);
        m0 = (Drawable) getResources().getDrawable(R.drawable.chord_m_0);
        m1 = (Drawable) getResources().getDrawable(R.drawable.chord_m_1);
        m2 = (Drawable) getResources().getDrawable(R.drawable.chord_m_2);
        m3 = (Drawable) getResources().getDrawable(R.drawable.chord_m_3);
        m4 = (Drawable) getResources().getDrawable(R.drawable.chord_m_4);
        m5 = (Drawable) getResources().getDrawable(R.drawable.chord_m_5);
        rx = (Drawable) getResources().getDrawable(R.drawable.chord_r_x);
        r0 = (Drawable) getResources().getDrawable(R.drawable.chord_r_0);
        r1 = (Drawable) getResources().getDrawable(R.drawable.chord_r_1);
        r2 = (Drawable) getResources().getDrawable(R.drawable.chord_r_2);
        r3 = (Drawable) getResources().getDrawable(R.drawable.chord_r_3);
        r4 = (Drawable) getResources().getDrawable(R.drawable.chord_r_4);
        r5 = (Drawable) getResources().getDrawable(R.drawable.chord_r_5);

        chordimageshere = (TableLayout) findViewById(R.id.chordimageshere);
        popupChord = (ScrollView) findViewById(R.id.popupchords);
        chordButton = (ImageView) findViewById(R.id.chordbutton);
        popupChord_Instrument = (Spinner) findViewById(R.id.popupchord_instrument);
        String[] instrument_choice = new String[3];
        instrument_choice[0] = getResources().getString(R.string.guitar);
        instrument_choice[1] = getResources().getString(R.string.ukulele);
        instrument_choice[2] = getResources().getString(R.string.mandolin);
        ArrayAdapter<String> adapter_instrument = new ArrayAdapter<String>(this, R.layout.my_spinner, instrument_choice);
        adapter_instrument.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupChord_Instrument.setAdapter(adapter_instrument);
        popupChord_Instrument.setOnItemSelectedListener(new popupChord_InstrumentListener());

        popupPad = (ScrollView) findViewById(R.id.popuppad);
        popupPad_key = (Spinner) findViewById(R.id.popuppad_key);
        ArrayAdapter<CharSequence> adapter_key = ArrayAdapter.createFromResource(this,
                R.array.key_choice,
                R.layout.my_spinner);
        adapter_key.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupPad_key.setAdapter(adapter_key);
        popupPad_key.setOnItemSelectedListener(new popupPad_keyListener());
        popupPad_volume = (SeekBar) findViewById(R.id.popuppad_volume);
        popupPad_volume.setOnSeekBarChangeListener(new popupPad_volumeListener());
        popupPad_volume_text = (TextView) findViewById(R.id.popuppad_volume_text);
        popupPad_pan = (SeekBar) findViewById(R.id.popuppad_pan);
        popupPad_pan.setOnSeekBarChangeListener(new popupPad_volumeListener());
        popupPad_pan_text = (TextView) findViewById(R.id.popuppad_pan_text);
        popupPad_startstopbutton = (Button) findViewById(R.id.popuppad_startstopbutton);

        popupAutoscroll = (ScrollView) findViewById(R.id.popupautoscroll);
        popupAutoscroll_delay = (SeekBar) findViewById(R.id.popupautoscroll_delay);
        popupAutoscroll_delay.setOnSeekBarChangeListener(new popupAutoscroll_delayListener());
        popupAutoscroll_delay_text = (TextView)  findViewById(R.id.popupautoscroll_delay_text);
        popupAutoscroll_duration = (TextView) findViewById(R.id.popupautoscroll_duration);
        popupAutoscroll_duration.addTextChangedListener(new textWatcher());
        popupAutoscroll_startstopbutton = (Button) findViewById(R.id.popupautoscroll_startstopbutton);

        popupMetronome = (ScrollView) findViewById(R.id.popupmetronome);
        popupMetronome_tempo = (SeekBar) findViewById(R.id.popupmetronome_tempo);
        popupMetronome_tempo.setOnSeekBarChangeListener(new popupMetronome_tempoListener());
        popupMetronome_tempo_text = (TextView) findViewById(R.id.popupmetronome_tempo_text);
        popupMetronome_timesig = (Spinner) findViewById(R.id.popupmetronome_timesig);
        ArrayAdapter<CharSequence> adapter_timesig = ArrayAdapter.createFromResource(this,
                R.array.timesig,
                R.layout.my_spinner);
        adapter_timesig.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupMetronome_timesig.setAdapter(adapter_timesig);
        popupMetronome_timesig.setOnItemSelectedListener(new popupMetronome_timesigListener());
        popupMetronome_volume = (SeekBar) findViewById(R.id.popupmetronome_volume);
        popupMetronome_volume.setOnSeekBarChangeListener(new popupMetronome_volumeListener());
        popupMetronome_volume_text = (TextView) findViewById(R.id.popupmetronome_volume_text);
        popupMetronome_pan = (SeekBar) findViewById(R.id.popupmetronome_pan);
        popupMetronome_pan.setOnSeekBarChangeListener(new popupMetronome_volumeListener());
        popupMetronome_pan_text = (TextView) findViewById(R.id.popupmetronome_pan_text);
        popupMetronome_startstopbutton = (Button) findViewById(R.id.popupmetronome_startstopbutton);
        popupMetronome_visualmetronometoggle = (ToggleButton) findViewById(R.id.visualmetronome);
        popupMetronome_visualmetronometoggle.setChecked(visualmetronome);

        //Set default view as 1 col
        scrollpage = scrollpage_onecol;

        //Get the available screen height and width
        scrollpage_pdf.setVisibility(View.INVISIBLE);
        pdf_selectpage.setVisibility(View.INVISIBLE);
        stickynotes.setVisibility(View.INVISIBLE);
        mySticky.setVisibility(View.INVISIBLE);
        padButton.setVisibility(View.INVISIBLE);
        linkButton.setVisibility(View.INVISIBLE);
        chordButton.setVisibility(View.INVISIBLE);
        metronomeButton.setVisibility(View.INVISIBLE);
        autoscrollButton.setVisibility(View.INVISIBLE);
        scrollstickyholder.setVisibility(View.GONE);
        scrollpage_onecol.setVisibility(View.INVISIBLE);
        scrollpage_twocol.setVisibility(View.INVISIBLE);
        scrollpage_threecol.setVisibility(View.INVISIBLE);
        lyricstable_onecolview.setVisibility(View.INVISIBLE);
        lyricstable_twocolview.setVisibility(View.INVISIBLE);
        lyricstable2_twocolview.setVisibility(View.INVISIBLE);
        lyricstable_threecolview.setVisibility(View.INVISIBLE);
        lyricstable2_threecolview.setVisibility(View.INVISIBLE);
        lyricstable3_threecolview.setVisibility(View.INVISIBLE);
        popupPad.setVisibility(View.GONE);
        popupAutoscroll.setVisibility(View.GONE);
        popupMetronome.setVisibility(View.GONE);
        popupChord.setVisibility(View.GONE);


        uparrow = (ImageView) findViewById(R.id.uparrow);
        downarrow = (ImageView) findViewById(R.id.downarrow);

        checkScrollPosition = new Runnable() {
            @Override
            public void run() {
                int height = ((ViewGroup) scrollpage).getChildAt(0).getMeasuredHeight() - scrollpage.getHeight();

                // Decide if the down arrow should be displayed.
                if (height > scrollpage.getScrollY()) {
                    downarrow.setVisibility(View.VISIBLE);
                } else {
                    downarrow.setVisibility(View.INVISIBLE);
                }
                // Decide if the up arrow should be displayed.
                if (scrollpage.getScrollY() > 0) {
                    uparrow.setVisibility(View.VISIBLE);
                } else {
                    uparrow.setVisibility(View.INVISIBLE);
                }
            }
        };

        // get the expandablelistview
        expListViewOption = (ExpandableListView) findViewById(R.id.option_list_ex);
        expListViewSong = (ExpandableListView) findViewById(R.id.song_list_ex);

        // Set up the navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        prepareSongMenu();
        prepareOptionMenu();

        // What happens when the navigation drawers are opened
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,R.string.drawer_close) {
            // Called when a drawer has settled in a completely closed state.
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                toggleActionBar();
                showpagebuttons();
            }


            // Called when a drawer has settled in a completely open state.
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                wasscrolling = false;
                scrollbutton = false;
                toggleActionBar();
                hidepagebuttons();
                hidepopupPad();
                hidepopupChord();
                hidepopupAutoscroll();
                hidepopupMetronome();
                hidepopupSticky();

                if (!getActionBar().isShowing()) {
                    getActionBar().show();
                }
            }
        };

        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (swipeDrawer.equals("N")) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }

        mDrawerLayout.setScrimColor(0x33000000);

        // Before redrawing the lyrics table, split the song into sections
        redrawTheLyricsTable(main_page);

        if (setView.equals("Y")) {
            fixSetActionButtons(menu);
        }

        invalidateOptionsMenu();

        fixSetActionButtons(menu);


        main_page = (View) findViewById(R.id.main_page);
        mDrawerLayout.closeDrawer(expListViewOption);
        mDrawerLayout.closeDrawer(expListViewSong);

        tempfontsize = mainfontsize;
        twocolfontsize = mainfontsize;
        threecolfontsize = mainfontsize;
        tempsectionsize = sectionfontsize;

        View v = getWindow().getDecorView();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        // Prepare the two media players (for crossfading)
        //mPlayer1 = new MediaPlayer();		
        //mPlayer2 = new MediaPlayer();			
        //mPlayer1 = MediaPlayer.create(FullscreenActivity.this, R.raw.c);
        //mPlayer2 = MediaPlayer.create(FullscreenActivity.this, R.raw.c);

        scaleGestureDetector = new ScaleGestureDetector(this, new simpleOnScaleGestureListener());

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //initialVolume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume = (short) metronomevol;
        //volume = initialVolume;
        metroTask = new MetronomeAsyncTask();
        Runtime.getRuntime().gc();

    }


    public void exportPDF() {
    }


    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void hidepopupPad() {
        if (popupPad.getVisibility()==View.VISIBLE) {
            popupPad.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupPad.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
        }
        if (popupPad_stoporstart.equals("stop")) {
            padButton.setAlpha(0.3f);
        } else {
            padButton.setAlpha(0.5f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    public void hidepopupAutoscroll() {
        if (popupAutoscroll.getVisibility()==View.VISIBLE) {
            popupAutoscroll.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupAutoscroll.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
        }
        if (popupAutoscroll_stoporstart.equals("stop")) {
            autoscrollButton.setAlpha(0.3f);
        } else {
            autoscrollButton.setAlpha(0.5f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    public void hidepopupMetronome() {
        if (popupMetronome.getVisibility()==View.VISIBLE) {
            popupMetronome.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupMetronome.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
        }
        if (popupMetronome_stoporstart.equals("stop")) {
            metronomeButton.setAlpha(0.3f);
        } else {
            metronomeButton.setAlpha(0.5f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    public void hidepopupSticky() {
        if (scrollstickyholder.getVisibility()==View.VISIBLE) {
            scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollstickyholder.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
            stickynotes.setAlpha(0.3f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    public void hidepopupChord() {
        if (popupChord.getVisibility()==View.VISIBLE) {
            popupChord.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupChord = new Handler();
            delayhidepopupChord.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupChord.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
            chordButton.setAlpha(0.3f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    public void popupChords_toggle (View view) {
        // Hide the other popups
        hidepopupSticky();
        hidepopupAutoscroll();
        hidepopupMetronome();
        hidepopupPad();
        if (popupChord.getVisibility()==View.VISIBLE) {
            // If popupChord is visible, it is about to be hidden
            // Also fade the chordButton
            chordButton.setAlpha(0.3f);
            popupChord.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupChord = new Handler();
            delayhidepopupChord.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupChord.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
        } else {
            chordButton.setAlpha(0.5f);
            popupChord.setVisibility(View.VISIBLE);
            popupChord.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_bottom));
            popupChord.requestFocus();
        }
    }

    public void popupPad_toggle (View view) {
        // Hide the other popups
        hidepopupSticky();
        hidepopupAutoscroll();
        hidepopupMetronome();
        scrollpage.requestFocus();
        if (popupPad.getVisibility()==View.VISIBLE) {
            // If popupPad is visible, it is about to be hidden
            // Also fade the padButton if the pad isn't playing, otherwise keep it bright 
            if (popupPad_stoporstart.equals("stop")) {
                padButton.setAlpha(0.3f);
            } else {
                padButton.setAlpha(0.5f);
            }
            popupPad.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupPad.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
        } else {
            padButton.setAlpha(0.5f);
            popupPad.setVisibility(View.VISIBLE);
            popupPad.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_bottom));
            popupPad.requestFocus();
        }
    }


    public void popupPad_startstop (View view) {
        // This is called when the start/stop button has been pressed
        if (popupPad_stoporstart.equals("start")) {
            // user now wants to stop
            // Turn off the button for now.  This will be reset after fadeout or 10secs
            popupPad_stoporstart = "stop";
            popupPad_startstopbutton.setText(getResources().getString(R.string.wait));
            if (currentapiVersion>=16) {
                popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.grey_button));
            } else {
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_button));
            }
            popupPad_startstopbutton.setEnabled(false);
            // Which pads are playing and need faded?
            fadeout1=false;
            fadeout2=false;
            if (mPlayer1!=null) {
                if (mPlayer1.isPlaying()) {
                    whichtofadeout=1;
                    fadeout1 = true;
                }
            }
            if (mPlayer2!=null) {
                if (mPlayer2.isPlaying()) {
                    whichtofadeout=2;
                    fadeout2 = true;
                }
            }

        } else {
            // user now wants to start
            popupPad_stoporstart = "start";
            popupPad_startstopbutton.setText(getResources().getString(R.string.stop));
            if (currentapiVersion>=16) {
                popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
            } else {
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            }
            popupPad_startstopbutton.setEnabled(true);
            hidepopupPad();
        }
        mKey = popupPad_key.getItemAtPosition(popupPad_key.getSelectedItemPosition()).toString();
        try {
            togglePlayPads(view);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class popupChord_InstrumentListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            if (position==0) {
                chordInstrument = "g";
            } else if (position==1) {
                chordInstrument = "u";
            } else if (position==2) {
                chordInstrument = "m";
            }
            //Save preferences and redraw the chords
            Preferences.savePreferences();
            prepareChords();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e ("popupChord", "Nothing selected");
        }

    }

    private class popupPad_keyListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            String temp_key = popupPad_key.getItemAtPosition(popupPad_key.getSelectedItemPosition()).toString();
            mKey = temp_key;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e ("popupPad", "Nothing selected");
        }

    }

    private class popupPad_volumeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            popupPad_volume_text.setText(popupPad_volume.getProgress()+" %");
            float temp_padvol = (float) ((float) popupPad_volume.getProgress() / 100);
            String temp_padpan = "both";
            if (popupPad_pan.getProgress()==0) {
                temp_padpan = "left";
                popupPad_pan_text.setText("L");
            } else if (popupPad_pan.getProgress()==2) {
                temp_padpan = "right";
                popupPad_pan_text.setText("R");
            } else {
                popupPad_pan_text.setText("C");
            }
            if (mPlayer1!=null) {
                if (mPlayer1.isPlaying() && !isfading1) {
                    float leftVolume = temp_padvol;
                    float rightVolume = temp_padvol;
                    if (temp_padpan.equals("left")) {
                        leftVolume = temp_padvol;
                        rightVolume = 0.0f;
                    } else if (temp_padpan.equals("right")) {
                        leftVolume = 0.0f;
                        rightVolume = temp_padvol;
                    }
                    try {
                        mPlayer1.setVolume(leftVolume, rightVolume);
                    } catch (Exception e) {
                        // This will catch any exception, because they are all descended from Exception
                    }
                }
            }
            if (mPlayer2!=null) {
                if (mPlayer2.isPlaying() && !isfading2) {
                    float leftVolume = temp_padvol;
                    float rightVolume = temp_padvol;
                    if (temp_padpan.equals("left")) {
                        leftVolume = temp_padvol;
                        rightVolume = 0.0f;
                    } else if (temp_padpan.equals("right")) {
                        leftVolume = 0.0f;
                        rightVolume = temp_padvol;
                    }
                    try {
                        mPlayer2.setVolume(leftVolume, rightVolume);
                    } catch (Exception e) {
                        // This will catch any exception, because they are all descended from Exception
                    }
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            int temp_padvol = popupPad_volume.getProgress();
            padvol = (float) ((float) temp_padvol/100);
            if (popupPad_pan.getProgress()==0) {
                padpan = "left";
            } else if (popupPad_pan.getProgress()==2) {
                padpan = "right";
            } else {
                padpan = "both";
            }

            // Save preferences
            Preferences.savePreferences();
        }
    }

    public void popupMetronome_toggle (View view) {
        // Hide the other popups
        hidepopupSticky();
        hidepopupAutoscroll();
        hidepopupPad();
        scrollpage.requestFocus();
        if (popupMetronome.getVisibility()==View.VISIBLE) {
            // If user has clicked on stop, fade button
            if (popupMetronome_stoporstart.equals("stop")) {
                metronomeButton.setAlpha(0.3f);
            } else {
                metronomeButton.setAlpha(0.5f);
            }
            popupMetronome.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupMetronome = new Handler();
            delayhidepopupMetronome.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupMetronome.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
        } else {
            metronomeButton.setAlpha(0.5f);
            popupMetronome.setVisibility(View.VISIBLE);
            popupMetronome.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_bottom));
        }
    }

    public void popupMetronome_startstop (View view) {
        if (popupMetronome_stoporstart.equals("start")) {
            // user now wants to stop
            popupMetronome_stoporstart = "stop";
            getActionBar().setBackgroundDrawable(new ColorDrawable(beatoffcolour));
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);

            if (popupMetronome.getVisibility()!=View.VISIBLE) {
                metronomeButton.setAlpha(0.3f);
            }
            popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
        } else {
            if (!isPDF) {
                // user now wants to start
                popupMetronome_stoporstart = "start";
                metronomeButton.setAlpha(0.5f);
                popupMetronome_startstopbutton.setText(getResources().getString(R.string.stop));
            }
        }
        mTimeSig = popupMetronome_timesig.getItemAtPosition(popupMetronome_timesig.getSelectedItemPosition()).toString();
        int valoftempobar = popupMetronome_tempo.getProgress() + 39;
        if (valoftempobar>39) {
            mTempo = ""+valoftempobar;
        } else {
            mTempo = "";
        }
        if (!isPDF) {
            metronomeToggle(view);
        }
    }

    private class popupMetronome_volumeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            popupMetronome_volume_text.setText(popupMetronome_volume.getProgress()+" %");
            metronomevol = (float) ((float) popupMetronome_volume.getProgress() / 100);
            if (fromUser) {
                if (popupMetronome_pan.getProgress()==0) {
                    metronomepan = "left";
                    popupMetronome_pan_text.setText("L");
                } else if (popupMetronome_pan.getProgress()==2) {
                    metronomepan = "right";
                    popupMetronome_pan_text.setText("R");
                } else {
                    popupMetronome_pan_text.setText("C");
                    metronomepan = "both";
                }
            }

        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            // Save preferences
            Preferences.savePreferences();
        }
    }

    private class popupMetronome_tempoListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int temposlider = popupMetronome_tempo.getProgress()+39;
            short newbpm = (short) temposlider;
            if (temposlider>39) {
                popupMetronome_tempo_text.setText(temposlider+" "+getResources().getString(R.string.bpm));
            } else {
                popupMetronome_tempo_text.setText(getResources().getString(R.string.notset));
            }
            if (newbpm<40) {
                mTempo="";
                bpm=100;
                if (metroTask!=null) {
                    metroTask.cancel(true);
                    metroTask.stop();
                }
            } else {
                try {
                    metroTask.setBpm(newbpm);
                    metroTask.setBeat(beats);
                    metroTask.setNoteValue(noteValue);
                    metroTask.setCurrentBeat(currentBeat);
                } catch (Exception e) {
                    // Catch error here
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            int valoftempobar = popupMetronome_tempo.getProgress() + 39;
            if (valoftempobar>39) {
                mTempo = ""+valoftempobar;
            } else {
                mTempo = "";
            }
        }
    }

    private class popupMetronome_timesigListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            String temp_sig = popupMetronome_timesig.getItemAtPosition(popupMetronome_timesig.getSelectedItemPosition()).toString();
            if (temp_sig.equals("2/4")) {
                noteValue = 4;
                beats = 2;
            } else if (temp_sig.equals("3/4")) {
                noteValue = 4;
                beats = 3;
            } else if (temp_sig.equals("3/8")) {
                noteValue = 8;
                beats = 3;
            } else if (temp_sig.equals("4/4")) {
                noteValue = 4;
                beats = 4;
            } else if (temp_sig.equals("5/4")) {
                noteValue = 4;
                beats = 5;
            } else if (temp_sig.equals("5/8")) {
                noteValue = 8;
                beats = 5;
            } else if (temp_sig.equals("6/4")) {
                noteValue = 4;
                beats = 6;
            } else if (temp_sig.equals("6/8")) {
                noteValue = 8;
                beats = 6;
            } else if (temp_sig.equals("7/4")) {
                noteValue = 4;
                beats = 7;
            } else if (temp_sig.equals("7/8")) {
                noteValue = 8;
                beats = 7;
            } else {
                noteValue = 4;
                beats = 4;
                if (metroTask!=null) {
                    metroTask.cancel(true);
                    metroTask.stop();
                }
            }

            mTimeSig = temp_sig;
            metroTask.setNoteValue(noteValue);
            metroTask.setBeat(beats);
            metroTask.setBpm(bpm);
            Log.d("metronome","getting here");

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e ("metronome", "Nothing selected");
        }

    }

    // Backwards compatible recreate().
    @Override
    public void recreate() {
        super.recreate();
    }

    public void saveSong(View view) {
        try {
            prepareToSave();
        } catch (Exception e) {
            e.printStackTrace();
        }
        myToastMessage = getResources().getString(R.string.savesong) + " - " + getResources().getString(R.string.ok);
        ShowToast.showToast(FullscreenActivity.this);
    }

    public void checkDirectories() {
        if (secStorage!=null) {
            if (secStorage.contains(":")) {
                secStorage = secStorage.substring(0,secStorage.indexOf(":"));
            }
            if (secStorage.contains("storage")) {
                // Valid external SD card directory
            }
        } else {
            // Lets look for alternative secondary storage positions
            for (int z=0;z<secStorageOptions.length;z++) {
                File testaltsecstorage = new File(secStorageOptions[z]);
                if (testaltsecstorage.exists() && testaltsecstorage.canWrite()) {
                    secStorage = secStorageOptions[z];
                }
            }
        }

        // If secondary and default storage are the same thing, hide secStorage
        if (defStorage.equals(secStorage)) {
            secStorage = null;
        }


        // I want folders to be ready on internal and external storage (if available)
        intStorCheck = new File(defStorage);
        if (intStorCheck.exists()  && intStorCheck.canWrite()) {
            defStorageExists = true;
        }
        root = android.os.Environment.getExternalStorageDirectory();
        homedir = new File(root.getAbsolutePath() + "/documents/OpenSong");
        dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs");
        dironsong = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs/OnSong");
        dirsets = new File(root.getAbsolutePath() + "/documents/OpenSong/Sets");
        dirPads = new File(root.getAbsolutePath() + "/documents/OpenSong/Pads");
        dirbackgrounds = new File(root.getAbsolutePath() + "/documents/OpenSong/Backgrounds");
        dirbibles = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture");
        dirbibleverses = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture/_cache");
        dircustomslides = new File(root.getAbsolutePath() + "/documents/OpenSong/Slides/_cache");

        if (secStorage!=null) {
            extStorCheck = new File(secStorage);
            if (extStorCheck.exists() && extStorCheck.canWrite()) {
                extStorageExists = true;
                if (prefStorage.equals("ext")) {
                    root = extStorCheck;
                    homedir = new File(root.getAbsolutePath() + "/documents/OpenSong");
                    dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs");
                    dironsong = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs/OnSong");
                    dirsets = new File(root.getAbsolutePath() + "/documents/OpenSong/Sets");
                    dirPads = new File(root.getAbsolutePath() + "/documents/OpenSong/Pads");
                    dirbackgrounds = new File(root.getAbsolutePath() + "/documents/OpenSong/Backgrounds");
                    dirbibles = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture");
                    dirbibleverses = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture/_cache");
                    dircustomslides = new File(root.getAbsolutePath() + "/documents/OpenSong/Slides/_cache");
                }
            }
        }

        if (prefStorage.equals("int") && !defStorageExists) {
            storageIsValid = false;
        }

        if (prefStorage.equals("ext") && !extStorageExists) {
            storageIsValid = false;
        }

        if (prefStorage.isEmpty() || !storageIsValid) {
            intStorCheck = null;
            extStorCheck = null;
            // Not set, or not valid - open the storage activity.
            Intent intent_stop = new Intent();
            intent_stop.setClass(this, StorageChooser.class);
            startActivity(intent_stop);
            finish();
        }

    }

    public void promptTempo() {
        if (popupMetronome.getVisibility()!=View.VISIBLE) {
            metronomeButton.setAlpha(0.3f);
        }
        popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
        popupMetronome_stoporstart = "stop";
        metronomeonoff="off";
        myToastMessage = getResources().getString(R.string.temponotset);
        ShowToast.showToast(FullscreenActivity.this);

    }

    public void promptTimeSig() {
        if (popupMetronome.getVisibility()!=View.VISIBLE) {
            metronomeButton.setAlpha(0.3f);
        }
        popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
        popupMetronome_stoporstart = "stop";
        metronomeonoff="off";
        myToastMessage = getResources().getString(R.string.timesignotset);
        ShowToast.showToast(FullscreenActivity.this);


    }

    public void metronomeToggle (View view) {
        if (metronomeonoff.equals("on")) {
            metronomeonoff = "off";
            //metronomeButton.setAlpha(0.3f);
            if (metroTask!=null) {
                metroTask.cancel(true);
                metroTask.stop();
            }
            //metroTask = new MetronomeAsyncTask();
            //Runtime.getRuntime().gc();
        } else {
            boolean mTimeSigValid = false;
            if (mTimeSig.isEmpty() || mTimeSig.equals("")) {
                promptTimeSig();
            } else {
                if (mTimeSig.equals("1/4")) {
                    //mPeriod = 1;
                    beats = 1;
                    noteValue = 4;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("2/4")) {
                    //mPeriod = 2;
                    beats = 2;
                    noteValue = 4;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("3/4")) {
                    //mPeriod = 3;
                    beats = 3;
                    noteValue = 4;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("3/8")) {
                    //mPeriod = 3;
                    beats = 3;
                    noteValue = 8;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("4/4")) {
                    //mPeriod = 4;
                    beats = 4;
                    noteValue = 4;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("5/4")) {
                    //mPeriod = 5;
                    beats = 5;
                    noteValue = 4;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("5/8")) {
                    //mPeriod = 5;
                    //mNoteVal = mNoteVal*2;
                    beats = 5;
                    noteValue = 8;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("6/4")) {
                    //mPeriod = 6;
                    beats = 6;
                    noteValue = 4;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("6/8")) {
                    //mPeriod = 6;
                    //mNoteVal = mNoteVal*2;
                    beats = 6;
                    noteValue = 8;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("7/4")) {
                    //mPeriod = 7;
                    beats = 7;
                    noteValue = 4;
                    mTimeSigValid = true;
                } else if (mTimeSig.equals("7/8")) {
                    //mPeriod = 7;
                    //mNoteVal = mNoteVal*2;
                    beats = 7;
                    noteValue = 8;
                    mTimeSigValid = true;
                }
            }


            boolean mTempoValid = false;
            if (mTempo.isEmpty()) {
                promptTempo();
            } else {
                String temp_tempo = mTempo;
                temp_tempo = temp_tempo.replace("Very Fast", "140");
                temp_tempo = temp_tempo.replace("Fast", "120");
                temp_tempo = temp_tempo.replace("Moderate", "100");
                temp_tempo = temp_tempo.replace("Slow", "80");
                temp_tempo = temp_tempo.replace("Very Slow", "60");
                temp_tempo = temp_tempo.replaceAll("[\\D]", "");
                try {
                    bpm = (short)Integer.parseInt(temp_tempo);
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                    bpm = 39;
                }
            }
            if (bpm>minBpm && bpm<maxBpm) {
                mTempoValid = true;
            } else {
                mTempoValid = false;
            }

            if (mTempo.equals("") || mTempo == null) {
                mTempoValid = false;
            }


            if (mTimeSigValid && mTempoValid) {
                metroTask = new MetronomeAsyncTask();
                Runtime.getRuntime().gc();
                metronomeonoff = "on";
                //metronomeButton.setAlpha(0.5f);
                metroTask.execute();
            }
        }
    }



    private class MetronomeAsyncTask extends AsyncTask<Void,Void,String> {
        Metronome metronome;

        MetronomeAsyncTask() {
            mHandler = getHandler();
            metronome = new Metronome(mHandler);
        }

        protected String doInBackground(Void... params) {
            metronome.setBeat(beats);
            metronome.setNoteValue(noteValue);
            metronome.setBpm(bpm);
            metronome.setBeatSound(beatSound);
            metronome.setSound(sound);
            metronome.setVolume(metrovol);
            metronome.setCurrentBeat(currentBeat);
            metronome.play();

            return null;
        }

        public void stop() {
            if (metronome!=null) {
                metronome.stop();
                metronome = null;
            }
            popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
            popupMetronome_stoporstart = "stop";
        }

        public void setBpm(short bpm) {
            if (metronome!=null && bpm>=minBpm && bpm<=maxBpm && noteValue>0) {
                metronome.setBpm(bpm);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {}
            }
        }

        public void setNoteValue(short noteVal) {
            if (metronome!=null && bpm>=minBpm && bpm<=maxBpm && noteValue>0) {
                metronome.setNoteValue(noteVal);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {}
            }
        }

        public void setBeat(short beat) {
            if(metronome != null)
                metronome.setBeat(beat);
            try {
                metronome.calcSilence();
            } catch (Exception e) {}
        }

        public void setCurrentBeat(int currentBeat) {
            if(metronome != null)
                metronome.setCurrentBeat(currentBeat);
            try {
                metronome.calcSilence();
            } catch (Exception e) {}
        }

        @SuppressWarnings("unused")
        public void setVolume(float metrovol) {
            if(metronome != null)
                metronome.setVolume(metrovol);
        }

    }

    private class textWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            mDuration = popupAutoscroll_duration.getText().toString();
        }
    }

    private class popupAutoscroll_delayListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            popupAutoscroll_delay_text.setText(progress+" s");
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            autoScrollDelay = seekBar.getProgress();
            // Save preferences
            Preferences.savePreferences();
        }
    }

    public void popupAutoscroll_toggle (View view) {
        // Hide other popup windows
        hidepopupPad();
        hidepopupSticky();
        hidepopupMetronome();

        if (popupAutoscroll.getVisibility()==View.VISIBLE) {
            // If user has clicked on stop, fade button
            if (popupAutoscroll_stoporstart.equals("stop")) {
                autoscrollButton.setAlpha(0.3f);
            } else {
                autoscrollButton.setAlpha(0.5f);
            }
            popupAutoscroll.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupAutoscroll = new Handler();
            delayhidepopupAutoscroll.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupAutoscroll.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
        } else {
            autoscrollButton.setAlpha(0.5f);
            popupAutoscroll.setVisibility(View.VISIBLE);
            popupAutoscroll.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_bottom));
        }
    }


    public void popupAutoscroll_startstop (View view) {
        if (popupAutoscroll_stoporstart.equals("start")) {
            // user now wants to stop
            popupAutoscroll_stoporstart = "stop";
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
        } else {
            // user now wants to start
            popupAutoscroll_stoporstart = "start";
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.stop));
        }

        autoScrollDelay = popupAutoscroll_delay.getProgress();

        mDuration = popupAutoscroll_duration.getText().toString();
        popupAutoscroll_toggle(view);
        autoScroll(view);
    }





    public void autoScroll (View view) {
        if (autoscrollonoff.equals("on")) {
            autoscrollonoff = "off";
            isautoscrolling = false;
            pauseautoscroll = true;
            autoscrollispaused = true;
            myToastMessage = getResources().getString(R.string.autoscroll) + " - " + getResources().getString(R.string.off);
            ShowToast.showToast(FullscreenActivity.this);
            // Turn the button off
            if (popupAutoscroll.getVisibility()!=View.VISIBLE) {
                autoscrollButton.setAlpha(0.3f);
            }
        } else {
            if (mDuration.isEmpty()) {
                // Prompt
                try {
                    promptSongDuration();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                autoscrollonoff = "on";
                pauseautoscroll = true;
                getAutoScrollValues();
                if (!autoscrollispaused) {
                    scrollpage.smoothScrollTo(0,0);
                    make_temp_autoScrollDelay = temp_autoScrollDelay;
                } else {
                    make_temp_autoScrollDelay = 0;
                }
                autoscrollispaused = false;
                myToastMessage = getResources().getString(R.string.autoscroll) + " - " + getResources().getString(R.string.on);
                ShowToast.showToast(FullscreenActivity.this);
                // Turn the button on
                autoscrollButton.setAlpha(0.5f);

                isautoscrolling = true;


                // After the temp_autoScrollDelay, postexecute the task
                delayautoscroll = new Handler();
                delayautoscroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mtask_autoscroll_music = (AutoScrollMusic) new AutoScrollMusic().execute();
                    }
                }, make_temp_autoScrollDelay*1000); // temp_AutoScrollDelay ms delay

            }
        }
    }

    public void promptSongDuration() throws IOException {
        // Create a dialogue and get the song duration
        // Close the menus
        autoscrollButton.setAlpha(0.3f);
        mDrawerLayout.closeDrawer(expListViewOption);
        mDrawerLayout.closeDrawer(expListViewOption);

        // Set up the Alert Dialogue
        // This bit gives the user a prompt to create a new song
        AlertDialog.Builder songdurationprompt = new AlertDialog.Builder(this);

        songdurationprompt.setTitle(getResources().getString(R.string.edit_song_duration));
        // Set an EditText view to get user input
        final EditText newduration = new EditText(this);
        newduration.setText(mDuration);
        newduration.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        songdurationprompt.setView(newduration);
        InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(newduration, InputMethodManager.SHOW_IMPLICIT);
        newduration.requestFocus();

        songdurationprompt.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mDuration = newduration.getText().toString();
                        try {
                            prepareToSave();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            LoadXML.loadXML();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (needtorefreshsongmenu) {
                            prepareSongMenu();
                            needtorefreshsongmenu = false;
                        }
                        redrawTheLyricsTable(view);
                    }

                });

        songdurationprompt.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancelled.
                    }
                });

        songdurationprompt.show();

    }



    public void getAutoScrollValues () {
        if (mDuration.isEmpty()) {
            // Duration not set.  Ask the user to add a time
        } else {
            // Get the numerical version of mDuration
            try {
                song_duration = Integer.parseInt(mDuration);
            } catch(NumberFormatException nfe) {
                // stop and ask user to set the tempo
                return;
            }

            // If it equates to 0, stop!
            if (song_duration<(autoscroll_pause_time/1000)) {
                return;
            } else {
                // Remove the autoScrollDelay
                if (song_duration>autoScrollDelay) {
                    song_duration = song_duration - autoScrollDelay;
                    temp_autoScrollDelay = autoScrollDelay;
                } else {
                    // Song is too short, set the temp_autoscroll_pause_time to 0
                    temp_autoScrollDelay = 0;
                }
                song_duration = (song_duration * 1000) - autoscroll_pause_time; // convert to milliseconds
            }
            // Ok figure out the size of amount of scrolling needed
            total_pixels_to_scroll = scrollpage.getHeight() - main_page.getHeight();
            int height = ((ViewGroup) scrollpage).getChildAt(0).getMeasuredHeight() - scrollpage.getHeight();
            if (height>=scrollpage.getScrollY()) {
                total_pixels_to_scroll = height;
            } else {
                total_pixels_to_scroll = 0;
            }
            // Ok how many pixels per 200ms - autoscroll_pause_time
            autoscroll_pixels = (float)total_pixels_to_scroll / ((float)song_duration / (float)autoscroll_pause_time);

        }

    }




    public class AutoScrollMusic extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... args) {
            int height = ((ViewGroup) scrollpage).getChildAt(0).getMeasuredHeight() - scrollpage.getHeight();
            while(isautoscrolling){
                long starttime = System.currentTimeMillis();
                newPos = (int) (scrollpage.getScrollY()+autoscroll_pixels);
                // don't scroll first time
                if (!pauseautoscroll) {
                    doautoScroll.post(autoScrollRunnable);
                    //scrollpage.smoothScrollBy(0,(int) autoscroll_pixels);
                } else {
                    pauseautoscroll = false;
                }
                if (newPos>=height) {
                    autoscrollispaused = false;
                    isautoscrolling = false;
                }
                long currtime = System.currentTimeMillis();
                while ((currtime-starttime)<autoscroll_pause_time) {
                    currtime = System.currentTimeMillis();
                }
            }
            return "dummy";
        }


        @Override
        protected void onPostExecute( String dummy ) {
            isautoscrolling = false;
            pauseautoscroll = true;
            if(popupAutoscroll.getVisibility()!=View.VISIBLE) {
                autoscrollButton.setAlpha(0.3f);
            }
            popupAutoscroll_stoporstart="stop";
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
            autoscrollonoff = "off";
            if(mtask_autoscroll_music!=null){
                //mtask_autoscroll_music = null;
                mtask_autoscroll_music.cancel(true);
            }
        }

        @Override
        public void onCancelled() {
            isautoscrolling = false;
            pauseautoscroll = true;
            if(popupAutoscroll.getVisibility()!=View.VISIBLE) {
                autoscrollButton.setAlpha(0.3f);
            }
            popupAutoscroll_stoporstart="stop";
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
            autoscrollonoff = "off";
            if(mtask_autoscroll_music!=null){
                //mtask_autoscroll_music = null;
                mtask_autoscroll_music.cancel(true);
            }
        }
    }

    public void visualMetronomeToggle (View view) {
        if (popupMetronome_visualmetronometoggle.isChecked()) {
            visualmetronome = true;
        } else {
            visualmetronome = false;
            getActionBar().setBackgroundDrawable(new ColorDrawable(beatoffcolour));
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);
        }
        Preferences.savePreferences();
    }


    public void tryKillMetronome() {
        if (metronomeonoff.equals("on")) {
            metronomeonoff="off";
            if (metroTask!=null) {
                metroTask.cancel(true);
                metroTask.stop();
            }
        }
        metronomeButton.setAlpha(0.3f);
    }

    public void tryKillPads() {
        // Hard stop all pads - leaving
        killfadeout1 = true;
        killfadeout2 = true;
        isfading1 = false;
        isfading2 = false;
        if (mtask_fadeout_music1!=null) {
            mtask_fadeout_music1 = null;
        }
        if (mtask_fadeout_music2!=null) {
            mtask_fadeout_music2 = null;
        }
        try {
            killPad1(padButton);
        } catch (Exception e) {}
        try {
            killPad2(padButton);
        } catch (Exception e) {}
        padson=false;
        if (popupPad.getVisibility()!=View.VISIBLE) {
            padButton.setAlpha(0.3f);
        }
    }


    public void killPad1 (View view) throws InterruptedException {
        // This releases mPlayer1 if it has finished fading out only
        if (mPlayer1!=null) {
            if (mPlayer1.isPlaying()) {
                mPlayer1.stop();
            }
            mPlayer1.reset();
            mPlayer1.release();
        }
        mPlayer1 = null;
        fadeout1 = false;

        isfading1 = false;

        if (mtask_fadeout_music1!=null) {
            mtask_fadeout_music1 = null;
        }
        if (mPlayer1==null & mPlayer2==null) {
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            if (currentapiVersion>=16) {
                popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
            } else {
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            }
            popupPad_startstopbutton.setEnabled(true);
            popupPad_stoporstart="stop";
            padson = false;

        }
    }

    public void killPad2 (View view) throws InterruptedException {
        // This releases mPlayer2 if it has finished fading out only
        if (mPlayer2!=null) {
            if (mPlayer2.isPlaying()) {
                mPlayer2.stop();
            }
            mPlayer2.reset();
            mPlayer2.release();
        }
        mPlayer2 = null;
        fadeout2 = false;

        isfading1 = false;

        if (mtask_fadeout_music2!=null) {
            mtask_fadeout_music2 = null;
        }
        if (mPlayer1==null & mPlayer2==null) {
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            if (currentapiVersion>=16) {
                popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
            } else {
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            }
            popupPad_startstopbutton.setEnabled(true);
            popupPad_stoporstart="stop";
            padson = false;

        }
    }

    public void togglePlayPads (View view) throws InterruptedException, IllegalStateException, IOException {
        // This is called when the user clicks on the padButton start/stop button.
        // If padson is true, user has tried to stop playing (fade out)
        // If padson is false, user has tried to start playing (only after any previous fade out)

        padButton.setAlpha(0.5f);
        if (padson) {
            if (popupPad.getVisibility()!=View.VISIBLE) {
                padButton.setAlpha(0.3f);
            }
            // If currently fading out, kill the fade out in progress
            if (isfading1) {
                killfadeout1 = true;
            } else {
                killfadeout1 = false;
                // Do a fadeout
                if (mPlayer1!=null) {
                    whichtofadeout = 1;
                    fadeout1 = true;
                    fadeOutBackgroundMusic1();
                }
            }
            if (isfading2) {
                killfadeout2 = true;
            } else {
                killfadeout2 = false;
                // Do a fadeout
                if (mPlayer2!=null) {
                    whichtofadeout = 2;
                    fadeout2 = true;
                    fadeOutBackgroundMusic2();
                }
            }
            padPlayingToggle = false;
            myToastMessage = getResources().getString(R.string.pad_stopped);
            ShowToast.showToast(FullscreenActivity.this);
            popupPad_startstopbutton.setEnabled(false);
            popupPad_startstopbutton.setText(getResources().getString(R.string.wait));
            if (currentapiVersion>=16) {
                popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.grey_button));
            } else {
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_button));
            }
            popupPad_stoporstart="stop";


            // In case of errors, check the mPlayer states again in 10 secs and fix the buttons if need be
            Handler checkmPlayerStates = new Handler();
            checkmPlayerStates.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((mPlayer1==null && mPlayer2==null) || ((mPlayer1!=null && !mPlayer1.isPlaying()) && (mPlayer2!=null && !mPlayer2.isPlaying()))) {
                        popupPad_stoporstart="stop";
                        padson=false;
                        popupPad_startstopbutton.setEnabled(true);
                        popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                        if (currentapiVersion>=16) {
                            popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
                        } else {
                            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                        }
                        fadeout1 = false;
                        fadeout2 = false;
                        killfadeout1 = false;
                        killfadeout2 = false;
                        if (mtask_fadeout_music1!=null) {
                            mtask_fadeout_music1 = null;
                        }
                        if (mtask_fadeout_music2!=null) {
                            mtask_fadeout_music2 = null;
                        }
                        if (popupPad.getVisibility()!=View.VISIBLE) {
                            padButton.setAlpha(0.3f);
                        }
                        mPlayer1=null;
                        mPlayer2=null;
                    }
                }
            }, 10000); // 1000ms delay




        } else {
            // User wants to play the pad
            // Check fade out isn't still happening - If it is, kill the fade out
            if (isfading1) {
                killfadeout1 = true;
                isfading1 = false;
                // Kill any pads that are playing for good measure
                killPad1(view);
            } else {
                killfadeout1 = false;
            }

            if (isfading2) {
                killfadeout2 = true;
                isfading2 = false;
                // Kill any pads that are playing for good measure
                killPad2(view);
            } else {
                killfadeout2 = false;
            }

            if (!mKey.isEmpty() && !mKey.equals("")) {
                // So far so good, change the button (valid key is checked later).
                padson = true;
                padPlayingToggle = true;
                padButton.setAlpha(0.5f);
                popupPad_startstopbutton.setText(getResources().getString(R.string.stop));
                if (currentapiVersion>=16) {
                    popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
                } else {
                    popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                }
                popupPad_startstopbutton.setEnabled(true);
                popupPad_stoporstart="start";

                myToastMessage = getResources().getString(R.string.pad_playing);
                ShowToast.showToast(FullscreenActivity.this);
                playPads(view);
            } else {
                // Key isn't set / is empty
                myToastMessage = getResources().getString(R.string.pad_error);
                ShowToast.showToast(FullscreenActivity.this);
                // Do nothing until key is specified
                if (popupPad.getVisibility()!=View.VISIBLE) {
                    // User has probably pressed the foot pedal - don't show prompt
                    padButton.setAlpha(0.3f);
                }
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                if (currentapiVersion>=16) {
                    popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
                } else {
                    popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                }
                popupPad_startstopbutton.setEnabled(true);
                popupPad_stoporstart="stop";
                padPlayingToggle = false;
                padson=false;
            }
        }
    }

    public void playPads(View view) throws IllegalStateException, IOException, InterruptedException {
        pad_filename = "null";

        // Determine the key of the song and therefore which auto pad track to use
        if (mKey.equals("A")) {
            pad_filename = "a";
        } else if (mKey.equals("A#") || mKey.equals("Bb")) {
            pad_filename = "asharp";
        } else if (mKey.equals("B")) {
            pad_filename = "b";
        } else if (mKey.equals("C")) {
            pad_filename = "c";
        } else if (mKey.equals("C#") || mKey.equals("Db")) {
            pad_filename = "csharp";
        } else if (mKey.equals("D")) {
            pad_filename = "d";
        } else if (mKey.equals("D#") || mKey.equals("Eb")) {
            pad_filename = "dsharp";
        } else if (mKey.equals("E")) {
            pad_filename = "e";
        } else if (mKey.equals("F")) {
            pad_filename = "f";
        } else if (mKey.equals("F#") || mKey.equals("Gb")) {
            pad_filename = "fsharp";
        } else if (mKey.equals("G")) {
            pad_filename = "g";
        } else if (mKey.equals("G#") || mKey.equals("Ab")) {
            pad_filename = "gsharp";
        } else if (mKey.equals("Am")) {
            pad_filename = "am";
        } else if (mKey.equals("A#m") || mKey.equals("Bbm")) {
            pad_filename = "asharpm";
        } else if (mKey.equals("Bm")) {
            pad_filename = "bm";
        } else if (mKey.equals("Cm")) {
            pad_filename = "cm";
        } else if (mKey.equals("C#m") || mKey.equals("Dbm")) {
            pad_filename = "csharpm";
        } else if (mKey.equals("Dm")) {
            pad_filename = "dm";
        } else if (mKey.equals("D#m") || mKey.equals("Ebm")) {
            pad_filename = "dsharpm";
        } else if (mKey.equals("Em")) {
            pad_filename = "em";
        } else if (mKey.equals("Fm")) {
            pad_filename = "fm";
        } else if (mKey.equals("F#m") || mKey.equals("Gbm")) {
            pad_filename = "fsharpm";
        } else if (mKey.equals("Gm")) {
            pad_filename = "gm";
        } else if (mKey.equals("G#m") || mKey.equals("Abm")) {
            pad_filename = "gsharpm";
        }

        int path = -1;

        if (pad_filename.equals("null") && padson) {
            // No key specified in the song - play nothing
            myToastMessage = getResources().getString(R.string.pad_error);
            ShowToast.showToast(FullscreenActivity.this);
            popupPad_stoporstart = "stop";
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            if (currentapiVersion>=16) {
                popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
            } else {
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            }
            popupPad_startstopbutton.setEnabled(true);
            padson = false;


        } else {
            path = getResources().getIdentifier(pad_filename,"raw", getPackageName());
        }

        // If both players are playing - currently in the process of a fade out of both players!
        if (mPlayer1!=null && mPlayer2!=null) {
            if (mPlayer1!=null && mPlayer1.isPlaying()) {
                if (isfading1) {
                    killfadeout1 = true;
                    if(mtask_fadeout_music1!=null) {
                        mtask_fadeout_music1 = null;
                    }
                    killPad1(padButton);
                }
            }

            // Now player 1 is released - load it up and get it going.
            // After that we can check player 2 progress and kill it if need be
            isfading1 = false;
            fadeout1 = false;
            killfadeout1 = false;
            // Fix the button
            padson = true;
            popupPad_startstopbutton.setText(getResources().getString(R.string.stop));
            if (currentapiVersion>=16) {
                popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
            } else {
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            }
            popupPad_startstopbutton.setEnabled(true);
            popupPad_stoporstart="start";

            mPlayer1 = MediaPlayer.create(FullscreenActivity.this,path);
            mPlayer1.setLooping(true);
            if (padpan.equals("left")) {
                mPlayer1.setVolume(padvol, 0.0f);
            } else if (padpan.equals("right")) {
                mPlayer1.setVolume(0.0f, padvol);
            } else {
                mPlayer1.setVolume(padvol, padvol);
            }
            mPlayer1.start();
            Log.d("mPlayer", "starting player 1 - both mPlayers were live - killed them");


            // Now kill the 2nd player if still needed
            if (mPlayer2!=null && mPlayer2.isPlaying()) {
                if (isfading2) {
                    killfadeout2 = true;
                    if(mtask_fadeout_music2!=null) {
                        mtask_fadeout_music2 = null;
                    }
                    killPad2(padButton);
                }
            }
            isfading2 = false;
            fadeout2 = false;
            killfadeout2 = false;



        } else if (mPlayer1!=null && mPlayer2==null) {
            // We need to fade out mPlayer1 and start playing mPlayer2
            if (mPlayer1.isPlaying()) {
                // We need to fade this out over the next 8 seconds, then load and start mPlayer2
                // After 10seconds, release mPlayer1
                whichtofadeout=1;
                fadeout1 = true;
                fadeOutBackgroundMusic1();
            }
            if (!pad_filename.equals("null") && padson) {
                mPlayer2 = MediaPlayer.create(FullscreenActivity.this,path);
                mPlayer2.setLooping(true);
                if (padpan.equals("left")) {
                    mPlayer2.setVolume(padvol, 0.0f);
                } else if (padpan.equals("right")) {
                    mPlayer2.setVolume(0.0f, padvol);
                } else {
                    mPlayer2.setVolume(padvol, padvol);
                }
                mPlayer2.start();
                padson = true;
                Log.d("mPlayer", "starting player 2 - mPlayer 1 was already live");
            }
        } else if (mPlayer2!=null && mPlayer1==null) {
            if (mPlayer2.isPlaying()) {
                // We need to fade this out over the next 8 seconds, then load and start mPlayer1
                // After 10seconds, release mPlayer1
                whichtofadeout=2;
                fadeout2 = true;
                fadeOutBackgroundMusic2();
            }
            if (!pad_filename.equals("null") && padson) {
                mPlayer1 = MediaPlayer.create(FullscreenActivity.this,path);
                mPlayer1.setLooping(true);
                if (padpan.equals("left")) {
                    mPlayer1.setVolume(padvol, 0.0f);
                } else if (padpan.equals("right")) {
                    mPlayer1.setVolume(0.0f, padvol);
                } else {
                    mPlayer1.setVolume(padvol, padvol);
                }
                mPlayer1.start();
                padson = true;
                Log.d("mPlayer", "starting player 1 - mPlayer 2 was already live");

            }
        } else if (mPlayer1==null && mPlayer2==null){
            // Nothing was playing already, so start mPlayer 1
            mPlayer1 = MediaPlayer.create(FullscreenActivity.this,path);
            mPlayer1.setLooping(true);
            if (padpan.equals("left")) {
                mPlayer1.setVolume(padvol, 0.0f);
            } else if (padpan.equals("right")) {
                mPlayer1.setVolume(0.0f, padvol);
            } else {
                mPlayer1.setVolume(padvol, padvol);
            }
            mPlayer1.start();
            padson = true;
            Log.d("mPlayer", "starting player 1 - no mPlayers were live");
        }
    }


    public void fadeOutBackgroundMusic1(){
        killfadeout1 = false;
        isfading1 = true;
        mtask_fadeout_music1 = (FadeOutMusic1) new FadeOutMusic1().execute();
    }

    public void fadeOutBackgroundMusic2(){
        killfadeout2 = false;
        isfading2 = true;
        mtask_fadeout_music2 = (FadeOutMusic2) new FadeOutMusic2().execute();
    }


    public class FadeOutMusic1 extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... args) {
            float level1 = padvol;
            int i = 1;
            isfading1 = true;
            Log.d("fade", "fading mPlayer 1");
            while(i<50 && !orientationchanged && !killfadeout1){
                i++;
                level1=level1*0.9f;
                float leftVol1 = level1;
                float rightVol1 = level1;
                if (padpan.equals("left")) {
                    rightVol1=0.0f;
                } else if (padpan.equals("right")) {
                    leftVol1=0.0f;
                }
                try {
                    if (mPlayer1!=null) {
                        mPlayer1.setVolume(leftVol1, rightVol1);
                    }
                } catch (Exception e) {
                    // Problem!
                    Log.d("d","Struggling to set volume of mPlayer1");
                }

                // Pause before next fade increment
                long nowtime = System.currentTimeMillis();
                long thentime = nowtime + 150;
                while (System.currentTimeMillis()<thentime) {
                    // Do nothing......
                }
            }
            return "dummy";
        }

        @Override
        protected void onPostExecute( String dummy ) {
            isfading1 = false;
            fadeout1 = false;
            try {
                killPad1(view);
            } catch (Exception e) {}

            if(mtask_fadeout_music1!=null){
                mtask_fadeout_music1 = null;
            }

            if (mPlayer1==null && mPlayer2==null) {
                popupPad_stoporstart="stop";
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                if (currentapiVersion>=16) {
                    popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
                } else {
                    popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                }
                popupPad_stoporstart="stop";
                padson = false;
            }
        }

        @Override
        public void onCancelled() {
            fadeout1=false;
            try {
                killPad1(padButton);
            } catch (Exception e) {}
            if(mtask_fadeout_music1!=null){
                mtask_fadeout_music1 = null;
            }
            if (mPlayer1==null && mPlayer2==null) {
                popupPad_stoporstart="stop";
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                if (currentapiVersion>=16) {
                    popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
                } else {
                    popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                }
                popupPad_stoporstart="stop";
                padson = false;

            }
        }
    }



    public class FadeOutMusic2 extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... args) {
            float level2 = padvol;
            int i = 1;
            isfading2 = true;
            Log.d("fade", "fading mPlayer 2");
            while(i<50 && !orientationchanged && !killfadeout2){
                i++;
                level2=level2*0.9f;
                float leftVol2 = level2;
                float rightVol2 = level2;
                if (padpan.equals("left")) {
                    rightVol2=0.0f;
                } else if (padpan.equals("right")) {
                    leftVol2=0.0f;
                }
                try {
                    if (mPlayer2!=null) {
                        mPlayer2.setVolume(leftVol2, rightVol2);
                    }
                } catch (Exception e) {
                    // Problem!
                    Log.d("d","Struggling to set volume of mPlayer2");
                }

                // Pause before next fade increment
                long nowtime = System.currentTimeMillis();
                long thentime = nowtime + 150;
                while (System.currentTimeMillis()<thentime) {
                    // Do nothing......
                }
            }
            return "dummy";
        }

        @Override
        protected void onPostExecute( String dummy ) {
            isfading2 = false;
            fadeout2 = false;
            try {
                killPad2(view);
            } catch (Exception e) {}

            if(mtask_fadeout_music2!=null){
                mtask_fadeout_music2 = null;
            }

            if (mPlayer1==null && mPlayer2==null) {
                popupPad_stoporstart="stop";
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                if (currentapiVersion>=16) {
                    popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
                } else {
                    popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                }
                padson = false;
                popupPad_stoporstart="stop";

            }
        }

        @Override
        public void onCancelled() {
            fadeout2=false;
            try {
                killPad2(padButton);
            } catch (Exception e) {}

            if(mtask_fadeout_music2!=null){
                mtask_fadeout_music2 = null;
            }
            if (mPlayer1==null && mPlayer2==null) {
                popupPad_stoporstart="stop";
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                if (currentapiVersion>=16) {
                    popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
                } else {
                    popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                }
                padson = false;
                popupPad_stoporstart="stop";

            }
        }
    }



    public void editNotes(View view) {
        // This calls the edit song sticky notes dialogue
        // Once user clicks OK, the notes are saved to the song.

        // Close the menus
        mDrawerLayout.closeDrawer(expListViewOption);
        mDrawerLayout.closeDrawer(expListViewOption);

        // Hide the note if it is currently showing
        mySticky.setVisibility(View.INVISIBLE);
        scrollstickyholder.setVisibility(View.GONE);

        // Return the alpha to the note button
        stickynotes.setAlpha(0.3f);

        // Set up the Alert Dialogue
        // This bit gives the user a prompt to create a new song
        AlertDialog.Builder editmynotes = new AlertDialog.Builder(this);

        editmynotes.setTitle(getResources().getString(R.string.options_song_stickynotes));
        // Set an EditText view to get user input
        final EditText editnotesinput = new EditText(this);
        editnotesinput.setText(mNotes);
        editnotesinput.setTextColor(0xff000000);
        editnotesinput.setBackgroundColor(0xfffcf0ad);
        editmynotes.setView(editnotesinput);
        editnotesinput.requestFocus();
        InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(editnotesinput, InputMethodManager.SHOW_IMPLICIT);

        editmynotes.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Get the text for the new sticky note
                        mNotes = editnotesinput.getText().toString();

                        // Save the file
                        try {
                            prepareToSave();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Open the sticky back up if it isn't empty
                        if (mNotes==null) {
                            mNotes="";
                        }
                        if (mNotes.length()>0) {
                            stickynotes.setVisibility(View.VISIBLE);
                            stickynotes.setAlpha(0.5f);
                            mySticky.setText(mNotes);
                            mySticky.setVisibility(View.VISIBLE);
                            scrollstickyholder.setVisibility(View.VISIBLE);
                            scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_bottom));
                        } else {
                            stickynotes.setVisibility(View.INVISIBLE);
                            stickynotes.setAlpha(0.3f);
                            mySticky.setText(mNotes);
                            mySticky.setVisibility(View.INVISIBLE);
                            scrollstickyholder.setVisibility(View.GONE);
                        }
                    }
                });

        editmynotes.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancelled.
                    }
                });

        editmynotes.show();



    }
    public void hidepagebuttons() {
        delaycheckscroll.removeCallbacks(checkScrollPosition);
        wasshowing_pdfselectpage = pdf_selectpage.getVisibility();
        wasshowing_stickynotes = stickynotes.getVisibility();
        scrollstickyholder.setVisibility(View.GONE);

        //downarrow.setVisibility(View.GONE);
        findViewById(R.id.bottombar).setVisibility(View.GONE);
        uparrow.setVisibility(View.GONE);
    }

    public void showpagebuttons() {
        findViewById(R.id.bottombar).setVisibility(View.VISIBLE);
        // if PDF and page count is bigger than 1, show pdf button
        if (isPDF && pdfPageCount>1 && togglePageButtons.equals("Y")) {
            pdf_selectpage.setVisibility(View.VISIBLE);
        }
        // If song and sticky note exists, show it
        //if (!isPDF && mNotes.length()>0) {
        if (!isPDF && togglePageButtons.equals("Y") && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerOpen(expListViewSong)) {
            stickynotes.setVisibility(View.VISIBLE);
            autoscrollButton.setVisibility(View.VISIBLE);
            metronomeButton.setVisibility(View.VISIBLE);
            padButton.setVisibility(View.VISIBLE);
            linkButton.setVisibility(View.INVISIBLE);
            chordButton.setVisibility(View.VISIBLE);
        } else {
            stickynotes.setVisibility(View.INVISIBLE);
            autoscrollButton.setVisibility(View.INVISIBLE);
            metronomeButton.setVisibility(View.INVISIBLE);
            padButton.setVisibility(View.INVISIBLE);
            linkButton.setVisibility(View.INVISIBLE);
            chordButton.setVisibility(View.INVISIBLE);
        }

        // Reshow the scroll arrows if needed
        delaycheckscroll.post(checkScrollPosition);
        //padButton.setVisibility(View.VISIBLE);
    }

    public void prepareSongMenu() {
        // Initialise Songs menu
        listDataHeaderSong = new ArrayList<String>();
        listDataChildSong = new HashMap<String, List<String>>();

        // Get song folders
        ListSongFiles.listSongFolders();
        listDataHeaderSong.add(getResources().getString(R.string.mainfoldername));
        if (mSongFolderNames==null) {
            mSongFolderNames= new String[1];
            mSongFolderNames[0] = "";
        }
        if (childSongs==null) {
            childSongs= new String[1][1];
            childSongs[0][0] = "";
        }
        for (int w=0;w<mSongFolderNames.length-1;w++) {
            listDataHeaderSong.add(mSongFolderNames[w]);
        }

        for (int s=0;s<mSongFolderNames.length;s++) {
            List<String> song_folders = new ArrayList<String>();
            for (int t=0;t<childSongs[s].length;t++) {
                song_folders.add(childSongs[s][t]);
            }
            listDataChildSong.put(listDataHeaderSong.get(s), song_folders);
        }

        listAdapterSong = new ExpandableListAdapter(this, listDataHeaderSong, listDataChildSong);
        expListViewSong.setAdapter(listAdapterSong);
        expListViewSong.setFastScrollEnabled(true);


        // Listen for song folders being opened/expanded
        expListViewSong.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != lastExpandedGroupPositionSong){
                    expListViewSong.collapseGroup(lastExpandedGroupPositionSong);
                }
                lastExpandedGroupPositionSong = groupPosition;
            }
        });

        // Listen for long clicks in the song menu (songs only, not folders) - ADD TO SET!!!!
        expListViewSong.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                addingtoset = true;
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    // Vibrate to indicate something has happened
                    Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vb.vibrate(25);

                    songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

                    if (listDataHeaderSong.get(groupPosition)==mainfoldername) {
                        dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs");
                        whichSongFolder = mainfoldername;
                        whatsongforsetwork = "$**_" + songfilename + "_**$";
                    } else {
                        dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs/" + listDataHeaderSong.get(groupPosition));
                        whichSongFolder = listDataHeaderSong.get(groupPosition);
                        whatsongforsetwork = "$**_" + whichSongFolder + "/" + songfilename + "_**$";
                    }
                    // Set the appropriate song filename
                    songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

                    // Allow the song to be added, even if it is already there
                    mySet = mySet + whatsongforsetwork;

                    // Tell the user that the song has been added.
                    myToastMessage = "\"" + songfilename + "\" " + getResources().getString(R.string.addedtoset);
                    ShowToast.showToast(FullscreenActivity.this);

                    // Save the set and other preferences
                    Preferences.savePreferences();

                    // Show the current set
                    SetActions.prepareSetList();
                    invalidateOptionsMenu();
                    prepareOptionMenu();
                    mDrawerLayout.openDrawer(expListViewOption);
                    expListViewOption.expandGroup(0);

                    // Hide the menus - 1 second after opening the Option menu,
                    // close it (1000ms total)
                    Handler optionMenuFlickClosed = new Handler();
                    optionMenuFlickClosed.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.closeDrawer(expListViewOption);
                            addingtoset = false;
                        }
                    }, 1000); // 1000ms delay
                }
                return false;
            }
        });

        // Try to open the appropriate Song folder on the left menu
        expListViewSong.expandGroup(0);
        for (int z=0;z<listDataHeaderSong.size()-1;z++) {
            if (listDataHeaderSong.get(z).equals(whichSongFolder)) {
                expListViewSong.expandGroup(z);
            }
        }

        // Listen for short clicks in the song menu (songs only, not folders) - OPEN SONG!!!!
        expListViewSong.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                pdfPageCurrent = 0;
                if (!addingtoset) {
                    // Set the appropriate folder name

                    if (listDataHeaderSong.get(groupPosition)==mainfoldername) {
                        dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs");
                        whichSongFolder = mainfoldername;
                    } else {
                        dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs/" + listDataHeaderSong.get(groupPosition));
                        whichSongFolder = listDataHeaderSong.get(groupPosition);
                    }
                    // Set the appropriate song filename
                    songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

                    if (setView.equals("Y") && setSize >= 8) {
                        // Ok look for the song in the set.
                        if (whichSongFolder.equals(mainfoldername)) {
                            whatsongforsetwork = songfilename;
                        } else {
                            whatsongforsetwork = whichSongFolder + "/"
                                    + linkclicked;
                        }

                        if (mySet.indexOf(whatsongforsetwork) >= 0) {
                            // Song is in current set.  Find the song position in the current set and load it (and next/prev)
                            // The first song has an index of 6 (the 7th item as the rest are menu items)

                            previousSongInSet = "";
                            nextSongInSet = "";
                            showCurrentSet(view);

                            for (int x = 7; x < setSize; x++) {
                                if (mSet[x].equals(whatsongforsetwork)) {
                                    indexSongInSet = x;
                                    previousSongInSet = mSet[x - 1];
                                    if (x == setSize - 1) {
                                        nextSongInSet = "";
                                    } else {
                                        nextSongInSet = mSet[x + 1];
                                    }
                                }
                            }

                        } else {
                            // Song isn't in the set, so just show the song
                            // Switch off the set view (buttons in action bar)
                            setView = "N";
                        }
                    } else {
                        // User wasn't in set view, or the set was empty
                        // Switch off the set view (buttons in action bar)
                        setView = "N";
                    }

                    // Set the swipe direction to right to left
                    whichDirection = "R2L";

                    // Now save the preferences
                    Preferences.savePreferences();

                    invalidateOptionsMenu();

                    // Redraw the Lyrics View
                    redrawTheLyricsTable(view);
                    mDrawerLayout.closeDrawer(expListViewSong);
                    mDrawerLayout.closeDrawer(expListViewOption);

                } else {
                    addingtoset = false;
                }
                return false;
            }

        });

    }

    public void prepareOptionMenu() {
        // preparing list data
        listDataHeaderOption = new ArrayList<String>();
        listDataChildOption = new HashMap<String, List<String>>();

        // Adding headers for option menu data
        listDataHeaderOption.add(getResources().getString(R.string.options_set));
        listDataHeaderOption.add(getResources().getString(R.string.options_song));
        listDataHeaderOption.add(getResources().getString(R.string.options_options));

        // Adding child data
        List<String> options_set = new ArrayList<String>();
        options_set.add(getResources().getString(R.string.options_set_load));
        options_set.add(getResources().getString(R.string.options_set_save));
        options_set.add(getResources().getString(R.string.options_set_clear));
        options_set.add(getResources().getString(R.string.options_set_delete));
        options_set.add(getResources().getString(R.string.options_set_export));
        options_set.add(getResources().getString(R.string.options_set_edit));
        options_set.add("");

        // Parse the saved set
        mySet = mySet.replace("_**$$**_", "_**$%%%$**_");
        // Break the saved set up into a new String[]
        mSetList = mySet.split("%%%");
        // Restore the set back to what it was
        mySet = mySet.replace("_**$%%%$**_", "_**$$**_");
        setSize = mSetList.length;
        invalidateOptionsMenu();

        for (int r=0;r<mSetList.length;r++) {
            mSetList[r] = mSetList[r].replace("$**_","");
            mSetList[r] = mSetList[r].replace("_**$","");
            if (!mSetList[r].isEmpty()) {
                options_set.add(mSetList[r]);
            }
        }


        List<String> options_song = new ArrayList<String>();
        options_song.add(getResources().getString(R.string.options_song_transpose));
        options_song.add(getResources().getString(R.string.capo_toggle));
        options_song.add(getResources().getString(R.string.options_song_convert));
        options_song.add(getResources().getString(R.string.options_song_sharp));
        options_song.add(getResources().getString(R.string.options_song_flat));
        options_song.add(getResources().getString(R.string.options_song_edit));
        options_song.add(getResources().getString(R.string.options_song_stickynotes));
        options_song.add(getResources().getString(R.string.options_song_rename));
        options_song.add(getResources().getString(R.string.options_song_delete));
        options_song.add(getResources().getString(R.string.options_song_new));
        options_song.add(getResources().getString(R.string.options_song_export));
        options_song.add(getResources().getString(R.string.options_song_newfolder));
        options_song.add(getResources().getString(R.string.options_song_editfolder));

        List<String> options_options = new ArrayList<String>();
        options_options.add(getResources().getString(R.string.options_options_theme));
        options_options.add(getResources().getString(R.string.songbuttons_toggle));
        options_options.add(getResources().getString(R.string.autoscroll_time));
        options_options.add(getResources().getString(R.string.metronomepadsettings));
        options_options.add(getResources().getString(R.string.options_options_scale));
        options_options.add(getResources().getString(R.string.options_options_fontsize));
        options_options.add(getResources().getString(R.string.options_options_chordformat));
        options_options.add(getResources().getString(R.string.options_options_showchords));
        options_options.add(getResources().getString(R.string.options_options_menuswipe));
        options_options.add(getResources().getString(R.string.options_options_songswipe));
        options_options.add(getResources().getString(R.string.options_options_gestures));
        options_options.add(getResources().getString(R.string.options_options_hidebar));
        options_options.add(getResources().getString(R.string.options_options_colors));
        options_options.add(getResources().getString(R.string.options_options_fonts));
        options_options.add(getResources().getString(R.string.options_options_pedal));
        options_options.add(getResources().getString(R.string.options_options_help));
        options_options.add(getResources().getString(R.string.storage_choose));
        options_options.add(getResources().getString(R.string.import_onsong_choose));
        options_options.add(getResources().getString(R.string.language));
        options_options.add(getResources().getString(R.string.refreshsongs));
        options_options.add(getResources().getString(R.string.options_options_start));

        listDataChildOption.put(listDataHeaderOption.get(0), options_set); // Header, Child data
        listDataChildOption.put(listDataHeaderOption.get(1), options_song);
        listDataChildOption.put(listDataHeaderOption.get(2), options_options);

        listAdapterOption = new ExpandableListAdapter(this, listDataHeaderOption, listDataChildOption);

        // setting list adapter
        expListViewOption.setAdapter(listAdapterOption);

        // Listen for options menus being expanded (close the others and keep a note that this one is open)
        expListViewOption.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != lastExpandedGroupPositionOption){
                    expListViewOption.collapseGroup(lastExpandedGroupPositionOption);
                }
                lastExpandedGroupPositionOption = groupPosition;
            }
        });


        // Listen for long clicks on songs in current set to remove them
        expListViewOption.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removingfromset = true;
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    myOptionListClickedItem = position;

                    if (myOptionListClickedItem > 6 && groupPosition==0) {
                        // Long clicking on the 7th or later options will remove the
                        // song from the set (all occurrences)
                        // Remove this song from the set. Remember it has tags at the start and end
                        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vb.vibrate(25);

                        // Take away the menu items (7)
                        String tempSong = mSetList[childPosition-7];
                        mSetList[childPosition-7] = "";

                        if (indexSongInSet == (childPosition-7)) {
                            setView = "N";
                        }

                        mySet = "";
                        for (int w=0;w<mSetList.length;w++) {
                            if (!mSetList[w].isEmpty()) {
                                mySet = mySet + "$**_" + mSetList[w] + "_**$";
                            }
                        }


                        // Tell the user that the song has been removed.
                        myToastMessage = "\"" + tempSong + "\" "
                                + getResources().getString(R.string.removedfromset);
                        ShowToast.showToast(FullscreenActivity.this);


                        //Check to see if our set list is still valid
                        SetActions.prepareSetList();
                        showCurrentSet(view);
                        fixSetActionButtons(menu);
                        prepareOptionMenu();
                        invalidateOptionsMenu();
                        expListViewOption.expandGroup(0);

                        // Save set
                        Preferences.savePreferences();

                        // Close the drawers again so accidents don't happen!
                        mDrawerLayout.closeDrawer(expListViewSong);
                        mDrawerLayout.closeDrawer(expListViewOption);
                    }
                }
                removingfromset = false;

                return false;
            }
        });

        // Listview on child click listener
        expListViewOption.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (!removingfromset) {
                    // Make sure the song menu is closed
                    mDrawerLayout.closeDrawer(expListViewSong);

                    String chosenMenu = listDataHeaderOption.get(groupPosition);

                    // Build a dialogue window and related bits that get modified/shown if needed
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FullscreenActivity.this);
                    LinearLayout titleLayout = new LinearLayout(FullscreenActivity.this);
                    titleLayout.setOrientation(LinearLayout.VERTICAL);
                    TextView m_titleView = new TextView(FullscreenActivity.this);
                    m_titleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
                    m_titleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
                    m_titleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white) );
                    m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                    if (chosenMenu==getResources().getString(R.string.options_set)) {

                        // Load up a list of saved sets as it will likely be needed
                        SetActions.updateOptionListSets(FullscreenActivity.this, view);
                        Arrays.sort(mySetsFiles);
                        Arrays.sort(mySetsDirectories);
                        Arrays.sort(mySetsFileNames);
                        Arrays.sort(mySetsFolderNames);

                        // First up check for set options clicks
                        if (childPosition==0) {
                            // Load a set
                            // Pull up a dialogue with a list of all saved sets available
                            m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_load));
                            titleLayout.addView(m_titleView);
                            dialogBuilder.setCustomTitle(titleLayout);
                            dialogBuilder.setSingleChoiceItems(mySetsFileNames, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    setnamechosen = mySetsFileNames[arg1];
                                }
                            });
                            dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }
                            });
                            dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // Load the set up
                                    settoload = null;
                                    settoload = setnamechosen;
                                    try {
                                        SetActions.loadASet(view);
                                    } catch (XmlPullParserException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    // Save the new set to the preferences
                                    Preferences.savePreferences();

                                    // Reset the options menu
                                    prepareOptionMenu();
                                    // Expand set group
                                    expListViewOption.expandGroup(0);
                                }
                            });

                            dialogBuilder.show();


                        } else if (childPosition==1) {
                            // Save current set
                            m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_save));
                            titleLayout.addView(m_titleView);
                            dialogBuilder.setCustomTitle(titleLayout);
                            final EditText m_editbox = new EditText(FullscreenActivity.this);
                            m_editbox.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
                            m_editbox.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
                            m_editbox.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white) );
                            m_editbox.setBackgroundColor(FullscreenActivity.this.getResources().getColor(android.R.color.darker_gray));
                            m_editbox.setHint(getResources().getString(R.string.options_set_nameofset));
                            m_editbox.requestFocus();
                            dialogBuilder.setView(m_editbox);
                            dialogBuilder.setSingleChoiceItems(mySetsFileNames, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    setnamechosen = mySetsFileNames[arg1];
                                    m_editbox.setText(setnamechosen);
                                }
                            });
                            dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }
                            });
                            dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // Save the set
                                    settoload = m_editbox.getText().toString();
                                    try {
                                        promptNewSet();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    myToastMessage = getResources().getString(R.string.options_set_save) + " " + settoload + " - " + getResources().getString(R.string.ok);
                                    ShowToast.showToast(FullscreenActivity.this);

                                    // Reset the options menu
                                    prepareOptionMenu();
                                    // Expand set group
                                    expListViewOption.expandGroup(0);
                                }
                            });

                            dialogBuilder.show();


                        } else if (childPosition==2) {
                            // Clear current set
                            m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_clear));
                            titleLayout.addView(m_titleView);
                            dialogBuilder.setCustomTitle(titleLayout);
                            dialogBuilder.setMessage(getResources().getString(R.string.areyousure));

                            dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }
                            });
                            dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // Clear the set
                                    mySet = "";
                                    mSetList = null;
                                    setView = "N";
                                    invalidateOptionsMenu();

                                    // Save the new, empty, set
                                    Preferences.savePreferences();

                                    myToastMessage = getResources().getString(R.string.options_set_clear) + " " + getResources().getString(R.string.ok);
                                    ShowToast.showToast(FullscreenActivity.this);

                                    // Reset the options menu
                                    prepareOptionMenu();
                                    // Expand set group
                                    expListViewOption.expandGroup(0);
                                }
                            });

                            dialogBuilder.show();



                        } else if (childPosition==3) {
                            // Delete saved set
                            // Pull up a dialogue with a list of all saved sets available
                            m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_delete));
                            titleLayout.addView(m_titleView);
                            dialogBuilder.setCustomTitle(titleLayout);
                            dialogBuilder.setSingleChoiceItems(mySetsFileNames, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    setnamechosen = mySetsFileNames[arg1];
                                }
                            });
                            dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }
                            });
                            dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // Load the set up
                                    settoload = null;
                                    settoload = setnamechosen;

                                    File settodelete = new File(dirsets+"/"+settoload);
                                    if (settodelete.delete()) {
                                        myToastMessage = settoload + getResources().getString(R.string.sethasbeendeleted);
                                        ShowToast.showToast(FullscreenActivity.this);
                                    }

                                    // Reset the options menu
                                    prepareOptionMenu();
                                    // Expand set group
                                    expListViewOption.expandGroup(0);
                                }
                            });

                            dialogBuilder.show();





                        } else if (childPosition==4) {
                            // Export current set
                            // Pull up a dialogue with a list of all saved sets available
                            m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_export));
                            titleLayout.addView(m_titleView);
                            dialogBuilder.setCustomTitle(titleLayout);
                            dialogBuilder.setSingleChoiceItems(mySetsFileNames, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    setnamechosen = mySetsFileNames[arg1];
                                }
                            });
                            dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }
                            });
                            dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // Export the set
                                    settoload = null;
                                    settoload = setnamechosen;

                                    // Run the script that generates the email text which has the set details in it.
                                    try {
                                        ExportPreparer.setParser();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (XmlPullParserException e) {
                                        e.printStackTrace();
                                    }

                                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                    emailIntent.setType("text/plain");
                                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, settoload);
                                    emailIntent.putExtra(Intent.EXTRA_TITLE, settoload);
                                    emailIntent.putExtra(Intent.EXTRA_TEXT, settoload + "\n\n" + emailtext);
                                    emailtext = "";
                                    File file = new File(dirsets + "/" + settoload);
                                    if (!file.exists() || !file.canRead()) {
                                        return;
                                    }
                                    Uri uri = Uri.fromFile(file);
                                    emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    startActivity(Intent.createChooser(emailIntent, exportsavedset));
                                }

                            });

                            dialogBuilder.show();



                        } else if (childPosition==5) {
                            // Edit current set
                            // Only works for ICS or above

                            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                            mDrawerLayout.closeDrawer(expListViewOption);
                            if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                Intent editset = new Intent();
                                editset.setClass(FullscreenActivity.this, ListViewDraggingAnimation.class);
                                tryKillPads();
                                tryKillMetronome();
                                startActivity(editset);
                                FullscreenActivity.this.finish();
                            } else {
                                myToastMessage = getResources().getText(R.string.nothighenoughapi).toString();
                                ShowToast.showToast(FullscreenActivity.this);
                            }

                        } else if (childPosition==6) {
                            // Blank entry

                        } else {
                            // Load song in set
                            setView = "Y";
                            pdfPageCurrent = 0;
                            // Set item is 7 less than childPosition
                            indexSongInSet = childPosition - 7;
                            String linkclicked = mSetList[indexSongInSet];
                            if (indexSongInSet==0) {
                                // Already first item
                                previousSongInSet = "";
                            } else {
                                previousSongInSet = mSetList[indexSongInSet-1];
                            }

                            if (indexSongInSet==(setSize-1)) {
                                // Last item
                                nextSongInSet = "";
                            } else {
                                nextSongInSet = mSetList[indexSongInSet+1];
                            }
                            whichDirection = "R2L";
                            invalidateOptionsMenu();
                            if (linkclicked.indexOf("/") >= 0) {
                                // Ok so it does!
                            } else {
                                // Right it doesn't, so add the /
                                linkclicked = "/" + linkclicked;
                            }

                            // Now split the linkclicked into two song parts 0=folder 1=file
                            String[] songpart = linkclicked.split("/");

                            // If the folder length isn't 0, it is a folder
                            if (songpart[0].length() > 0 && !songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
                                whichSongFolder = songpart[0];
                                FullscreenActivity.dir = new File(
                                        FullscreenActivity.root.getAbsolutePath()
                                                + "/documents/OpenSong/Songs/"
                                                + songpart[0]);

                            } else if (songpart[0].length() > 0 && songpart[0].contains("Scripture")) {
                                whichSongFolder = "../OpenSong Scripture/_cache";
                                songpart[0] = "../OpenSong Scripture/_cache";
                                //FullscreenActivity.dir = FullscreenActivity.dirbibleverses;

                            } else if (songpart[0].length() > 0 && songpart[0].contains("Slide")) {
                                whichSongFolder = "../Slides/_cache";
                                songpart[0] = "../Slides/_cache";
                                //FullscreenActivity.dir = FullscreenActivity.dircustomslides;

                            } else {
                                whichSongFolder = mainfoldername;
                                FullscreenActivity.dir = new File(
                                        FullscreenActivity.root.getAbsolutePath()
                                                + "/documents/OpenSong/Songs");
                            }

                            songfilename = null;
                            songfilename = "";
                            songfilename = songpart[1];

                            // Save the preferences
                            Preferences.savePreferences();

                            redrawTheLyricsTable(view);

                            //Close the drawer
                            mDrawerLayout.closeDrawer(expListViewOption);
                        }




                    } else if (chosenMenu==getResources().getString(R.string.options_song)) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for song options clicks
                        if (childPosition==0) {
                            // Transpose
                            // First ask the user how many steps to transpose by
                            // Default is +1

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {

                                transposeDirection = "+1";
                                transposeTimes = 1;

                                AlertDialog.Builder transposeStepsDialog = new AlertDialog.Builder(FullscreenActivity.this);
                                transposeStepsDialog.setTitle(getResources().getString(R.string.transpose))
                                        .setSingleChoiceItems(transposeSteps, 6, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                if (arg1==0) { //-6
                                                    transposeTimes = 6;
                                                    transposeDirection = "-1";
                                                } else if (arg1==1) { //-5
                                                    transposeTimes = 5;
                                                    transposeDirection = "-1";
                                                } else if (arg1==2) { //-4
                                                    transposeTimes = 4;
                                                    transposeDirection = "-1";
                                                } else if (arg1==3) { //-3
                                                    transposeTimes = 3;
                                                    transposeDirection = "-1";
                                                } else if (arg1==4) { //-2
                                                    transposeTimes = 2;
                                                    transposeDirection = "-1";
                                                } else if (arg1==5) { //-1
                                                    transposeTimes = 1;
                                                    transposeDirection = "-1";
                                                } else if (arg1==6) { //+1
                                                    transposeTimes = 1;
                                                    transposeDirection = "+1";
                                                } else if (arg1==7) { //+2
                                                    transposeTimes = 2;
                                                    transposeDirection = "+1";
                                                } else if (arg1==8) { //+3
                                                    transposeTimes = 3;
                                                    transposeDirection = "+1";
                                                } else if (arg1==9) { //+4
                                                    transposeTimes = 4;
                                                    transposeDirection = "+1";
                                                } else if (arg1==10) { //+5
                                                    transposeTimes = 5;
                                                    transposeDirection = "+1";
                                                } else if (arg1==11) { //+6
                                                    transposeTimes = 6;
                                                    transposeDirection = "+1";
                                                }
                                            }
                                        });

                                transposeStepsDialog.setPositiveButton(getResources().getString(R.string.docontinue).toString(),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                mDrawerLayout.closeDrawer(expListViewOption);
                                                checkChordFormat();
                                            }
                                        });



                                transposeStepsDialog.setNegativeButton(
                                        getResources().getString(R.string.cancel),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                                // Cancelled.
                                                return;
                                            }
                                        });

                                transposeStepsDialog.show();
                            }

                        } else if (childPosition==1) {
                            // Capo display toggle

                            if (capoDisplay.equals("both")) {
                                capoDisplay = "capoonly";
                                myToastMessage = getResources().getString(R.string.capo_toggle_onlycapo);
                            } else {
                                capoDisplay = "both";
                                myToastMessage = getResources().getString(R.string.capo_toggle_bothcapo);
                            }
                            ShowToast.showToast(FullscreenActivity.this);
                            Preferences.savePreferences();
                            redrawTheLyricsTable(main_page);

                        } else if (childPosition==2) {
                            // Convert to preferred chord format

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {

                                transposeDirection = "0";
                                chord_converting = "Y";
                                checkChordFormat();
                            }

                        } else if (childPosition==3) {

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {

                                // Use # chords
                                transposeStyle = "sharps";
                                transposeDirection = "0";
                                checkChordFormat();
                            }

                        } else if (childPosition==4) {
                            // Use b chords

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {

                                transposeStyle = "flats";
                                transposeDirection = "0";
                                checkChordFormat();
                            }

                        } else if (childPosition==5) {
                            // Edit

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {

                                openEditSong();
                            }

                        } else if (childPosition==6) {
                            // Edit sticky notes

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                editNotes(view);
                            }

                        } else if (childPosition==7) {
                            // Rename
                            // This bit gives the user a prompt to change the song name
                            // First set the browsing directory back to the main one
                            dir = new File(root.getAbsolutePath()+"/documents/OpenSong/Songs");
                            currentFolder = whichSongFolder;
                            newFolder = whichSongFolder;
                            whichSongFolder = mainfoldername;
                            ListSongFiles.listSongs();

                            Log.d("rename","whichSongFolder="+whichSongFolder);
                            Log.d("rename","currentFolder="+currentFolder);
                            // This bit gives the user a prompt to create a new song
                            AlertDialog.Builder alert = new AlertDialog.Builder(
                                    FullscreenActivity.this);

                            m_titleView.setText(getResources().getString(R.string.renametitle));
                            titleLayout.addView(m_titleView);
                            alert.setCustomTitle(titleLayout);

                            // Get current folder
                            int numfolders = mSongFolderNames.length;
                            //By default the folder is set to the main one
                            int folderposition = 0;
                            for (int z=0;z<numfolders;z++) {
                                if (mSongFolderNames[z].toString().equals(currentFolder)) {
                                    // Set this as the folder
                                    folderposition = z;
                                    mSongFolderNames[z] = currentFolder;
                                }
                            }

                            alert.setSingleChoiceItems(mSongFolderNames, folderposition,  new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    newFolder = mSongFolderNames[arg1];
                                }
                            });

                            // Set an EditText view to get user input
                            final LinearLayout renameSong = new LinearLayout(FullscreenActivity.this);
                            renameSong.setOrientation(LinearLayout.VERTICAL);
                            final TextView blurb = new TextView(FullscreenActivity.this);
                            blurb.setText("\n"+ getResources().getString(R.string.rename));
                            blurb.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Medium);
                            blurb.setTextColor(lyricsChordsColor);
                            final EditText inputnewsongname = new EditText(FullscreenActivity.this);
                            inputnewsongname.setText(songfilename);
                            renameSong.addView(blurb);
                            renameSong.addView(inputnewsongname);
                            alert.setView(renameSong);

                            alert.setPositiveButton(getResources().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            String newSongTitle = inputnewsongname.getText().toString();
                                            // Rename
                                            String tempCurrentFolder = currentFolder + "/";
                                            String tempNewFolder = newFolder + "/";

                                            if (newFolder.equals(mainfoldername) || newFolder.equals("("+mainfoldername+")")) {
                                                tempNewFolder = "";
                                                whichSongFolder = "";
                                                newFolder = "";
                                            } else {
                                                whichSongFolder = newFolder;
                                            }

                                            if (currentFolder.equals(mainfoldername)) {
                                                tempCurrentFolder = "";
                                            }

                                            whichSongFolder = newFolder;

                                            if (isPDF && (!newSongTitle.contains(".pdf") || !newSongTitle.contains(".PDF"))) {
                                                newSongTitle = newSongTitle + ".pdf";
                                            }

                                            File from = new File(dir + "/" + tempCurrentFolder + songfilename);
                                            File to = new File(dir + "/" + tempNewFolder + newSongTitle);
                                            from.renameTo(to);
                                            songfilename = newSongTitle;

                                            // Load the songs
                                            ListSongFiles.listSongs();

                                            // Get the song indexes
                                            ListSongFiles.getCurrentSongIndex();
                                            prepareSongMenu();
                                            redrawTheLyricsTable(null);
                                            mDrawerLayout.closeDrawer(expListViewOption);
                                        }
                                    });

                            alert.setNegativeButton(
                                    getResources().getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            // Cancelled.
                                        }
                                    });

                            alert.show();


                        } else if (childPosition==8) {
                            // Delete
                            // Give the user an are you sure prompt!
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            // Yes button clicked
                                            // Exit set mode
                                            setView = "N";
                                            String setFileLocation = dir + "/" + songfilename;
                                            File filetoremove = new File(setFileLocation);
                                            // Don't allow the user to delete Love Everlasting
                                            // This is to stop there being no song to show after
                                            // deleting the currently viewed one
                                            if (songfilename.equals("Love everlasting")) {
                                                myToastMessage = "\"Love everlasting\" "
                                                        + getResources().getString(
                                                        R.string.shouldnotbedeleted);
                                                ShowToast.showToast(FullscreenActivity.this);
                                            } else {
                                                boolean deleted = filetoremove.delete();
                                                if (deleted) {
                                                    myToastMessage = "\""
                                                            + FullscreenActivity.songfilename
                                                            + "\" "
                                                            + getResources()
                                                            .getString(
                                                                    R.string.songhasbeendeleted);
                                                } else {
                                                    myToastMessage = getResources().getString(
                                                            R.string.deleteerror_start)
                                                            + " \""
                                                            + FullscreenActivity.songfilename
                                                            + "\" "
                                                            + getResources()
                                                            .getString(
                                                                    R.string.deleteerror_end_song);
                                                }
                                                ShowToast.showToast(FullscreenActivity.this);
                                            }

                                            // Now save the preferences
                                            Preferences.savePreferences();
                                            redrawTheLyricsTable(null);

                                            // Need to reload the song list
                                            // Match the song folder
                                            ListSongFiles.listSongs();
                                            prepareSongMenu();
                                            mDrawerLayout.closeDrawer(expListViewOption);
                                            mDrawerLayout.openDrawer(expListViewSong);
                                            // 1000ms second after opening the Songs menu, close it
                                            Handler songMenuFlickClosed = new Handler();
                                            songMenuFlickClosed.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mDrawerLayout.closeDrawer(expListViewSong);
                                                }
                                            }, 1000); // 1000ms delay

                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // No button clicked
                                            break;
                                    }
                                }
                            };

                            if (myLyrics.equals("ERROR!")) {
                                // Tell the user they can't edit a song with an error!
                                myToastMessage = songdoesntexist;
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        FullscreenActivity.this);
                                builder.setMessage(
                                        getResources().getString(R.string.areyousure))
                                        .setPositiveButton(
                                                getResources().getString(R.string.yes),
                                                dialogClickListener)
                                        .setNegativeButton(
                                                getResources().getString(R.string.no),
                                                dialogClickListener).show();
                            }

                        } else if (childPosition==9) {
                            // New
                            try {
                                promptNew();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        } else if (childPosition==10) {
                            // Export
                            // The current song is the songfile
                            // Believe it or not, it works!!!!!
                            if (myLyrics.equals("ERROR!")) {
                                // Tell the user they can't edit a song with an error!
                                myToastMessage = songdoesntexist;
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
								/*								 Intent intent = new Intent(Intent.ACTION_SEND);
								 intent.setType("application/xml");
								 intent.putExtra(Intent.EXTRA_TITLE, songfilename);
								 intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
								 startActivity(Intent.createChooser(intent,
										 exportcurrentsong));

								 */
                                // Run the script that generates the email text which has the set details in it.
                                try {
                                    ExportPreparer.songParser();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (XmlPullParserException e) {
                                    e.printStackTrace();
                                }

                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                emailIntent.setType("text/plain");
                                emailIntent.putExtra(Intent.EXTRA_TITLE, songfilename);
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, songfilename);
                                emailIntent.putExtra(Intent.EXTRA_TEXT, emailtext);
                                emailtext = "";
                                File file = new File(dir+"/" + songfilename);
                                Uri uri = Uri.fromFile(file);
                                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                startActivity(Intent.createChooser(emailIntent,exportcurrentsong));


                            }

                        } else if (childPosition==11) {
                            // Create a new song folder
                            try {
                                promptNewFolder();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if (childPosition==12) {
                            // Edit the name of a song folder
                            try {
                                editFolderName();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }




                    } else if (chosenMenu==getResources().getString(R.string.options_options)) {
                        // Now check for option options clicks
                        if (childPosition==0) {
                            // Switch theme

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {

                                myToastMessage = "";
                                if (mDisplayTheme.equals("Theme_Holo_Light") || mDisplayTheme.equals("Theme.Holo.Light")) {
                                    mDisplayTheme = "Theme_Holo";
                                    newbgColor = dark_lyricsBackgroundColor;
                                    myToastMessage = getResources().getString(R.string.dark_theme);
                                    Preferences.savePreferences();
                                } else if (mDisplayTheme.equals("Theme_Holo") || mDisplayTheme.equals("Theme.Holo")) {
                                    mDisplayTheme = "custom1";
                                    newbgColor = custom1_lyricsBackgroundColor;
                                    myToastMessage = getResources().getString(R.string.custom1_theme);
                                    Preferences.savePreferences();
                                } else if (mDisplayTheme.equals("custom1")) {
                                    mDisplayTheme = "custom2";
                                    newbgColor = custom2_lyricsBackgroundColor;
                                    myToastMessage = getResources().getString(R.string.custom2_theme);
                                    Preferences.savePreferences();
                                } else if (mDisplayTheme.equals("custom2")) {
                                    mDisplayTheme = "Theme_Holo_Light";
                                    newbgColor = light_lyricsBackgroundColor;
                                    myToastMessage = getResources().getString(R.string.light_theme);
                                    Preferences.savePreferences();
                                }
                                ShowToast.showToast(FullscreenActivity.this);
                                main_page = findViewById(R.id.main_page);
                                main_lyrics = findViewById(R.id.LyricDisplay);
                                mDrawerLayout.openDrawer(expListViewOption);
                                redrawTheLyricsTable(view);
                            }

                        } else if (childPosition==1) {
                            // Toggle song buttons on page on/off
                            if (togglePageButtons.equals("Y")) {
                                togglePageButtons = "N";
                                Preferences.savePreferences();
                                myToastMessage = getResources().getString(R.string.songbuttons_toggle) + " - " + getResources().getString(R.string.off);
                                ShowToast.showToast(FullscreenActivity.this);
                                redrawTheLyricsTable(view);
                            } else {
                                togglePageButtons = "Y";
                                Preferences.savePreferences();
                                myToastMessage = getResources().getString(R.string.songbuttons_toggle) + " - " + getResources().getString(R.string.on);
                                ShowToast.showToast(FullscreenActivity.this);
                                redrawTheLyricsTable(view);
                            }


                        } else if (childPosition==2) {
                            // Set autoscroll delay time (0-30 seconds)
                            final int storeautoscrolldelay = autoScrollDelay;

                            dialogBuilder.setTitle(getResources().getText(R.string.autoscroll_time).toString());

                            LinearLayout setastime = new LinearLayout(FullscreenActivity.this);
                            setastime.setOrientation(LinearLayout.VERTICAL);
                            final TextView text_time = new TextView(FullscreenActivity.this);
                            text_time.setText(autoScrollDelay+ " s");
                            text_time.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Medium);
                            text_time.setGravity(1);
                            final SeekBar autoscrollpauseseekbar = new SeekBar(FullscreenActivity.this);
                            autoscrollpauseseekbar.setMax(30);
                            setastime.addView(text_time);
                            setastime.addView(autoscrollpauseseekbar);
                            dialogBuilder.setView(setastime);
                            autoscrollpauseseekbar.setProgress(autoScrollDelay);

                            autoscrollpauseseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                    autoScrollDelay = seekBar.getProgress();
                                    text_time.setText(autoScrollDelay+ " s");
                                }
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                    // Do nothing
                                }
                                public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                                    autoScrollDelay = seekBar.getProgress();
                                    text_time.setText(autoScrollDelay+ " s");
                                }
                            });
                            dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    autoScrollDelay = storeautoscrolldelay;
                                }
                            });
                            dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // Save the preferences
                                    Preferences.savePreferences();
                                    mDrawerLayout.closeDrawer(expListViewOption);
                                }
                            });
                            dialogBuilder.show();


                        } else if (childPosition==3) {
                            // Open Metronome and Pad settings page
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, MetronomePadSettings.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();



                        } else if (childPosition==4) {
                            // Toggle autoscale
                            if (toggleYScale.equals("Y")) {
                                toggleYScale = "W";
                                myToastMessage = getResources().getString(R.string.scaleY)
                                        + " " + getResources().getString(R.string.on_width);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else if (toggleYScale.equals("W")) {
                                toggleYScale = "N";
                                myToastMessage = getResources().getString(R.string.scaleY)
                                        + " " + getResources().getString(R.string.off);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                toggleYScale = "Y";
                                myToastMessage = getResources().getString(R.string.scaleY)
                                        + " " + getResources().getString(R.string.on);
                                ShowToast.showToast(FullscreenActivity.this);
                            }
                            Preferences.savePreferences();
                            redrawTheLyricsTable(view);



                        } else if (childPosition==5) {
                            // Choose font size

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {


                                final float storefontsize = mFontSize;

                                // Decide where the font size progress bar should be
                                if (mFontSize>80) {
                                    mFontSize = 42;
                                }
                                fontsizeseekar = Math.round((((mFontSize)/80)*100));

                                dialogBuilder.setTitle(getResources().getText(R.string.options_options_fontsize).toString());

                                LinearLayout scalefont = new LinearLayout(FullscreenActivity.this);
                                scalefont.setOrientation(LinearLayout.VERTICAL);
                                final TextView text_size = new TextView(FullscreenActivity.this);
                                text_size.setText(""+mFontSize);
                                text_size.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Medium);
                                text_size.setGravity(1);
                                final SeekBar fontseekbar = new SeekBar(FullscreenActivity.this);
                                scalefont.addView(text_size);
                                scalefont.addView(fontseekbar);
                                dialogBuilder.setView(scalefont);
                                fontseekbar.setProgress(fontsizeseekar);

                                fontseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                                    public void onStopTrackingTouch(SeekBar seekBar) {
                                        // Basic font size is 42sp - this is to be 50%
                                        // Min is 4sp

                                        fontsizeseekar = seekBar.getProgress();
                                        float val = fontsizeseekar;
                                        val = val/100;
                                        val = val * 76;
                                        val = val*10;
                                        val = Math.round(val);
                                        val = val/10;
                                        mFontSize = 4 + val;
                                        text_size.setText(""+mFontSize);
                                        redrawTheLyricsTable(view);

                                    }
                                    public void onStartTrackingTouch(SeekBar seekBar) {
                                        // Do nothing
                                    }
                                    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                                        // Basic font size is 42sp - this is to be 50%
                                        // Min is 4sp

                                        fontsizeseekar = seekBar.getProgress();
                                        float val = fontsizeseekar;
                                        val = val/100;
                                        val = val * 76;
                                        val = val*10;
                                        val = Math.round(val);
                                        val = val/10;
                                        mFontSize = 4 + val;
                                        text_size.setText(""+mFontSize);
                                    }
                                });
                                dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        mFontSize = storefontsize;
                                        redrawTheLyricsTable(view);
                                    }
                                });
                                dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // Save the preferences
                                        Preferences.savePreferences();

                                        redrawTheLyricsTable(null);

                                        mDrawerLayout.closeDrawer(expListViewOption);
                                    }
                                });
                                dialogBuilder.show();

                            }


                        } else if (childPosition==6) {
                            // Choose chord format
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, ChordFormat.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();


                        } else if (childPosition==7) {
                            // Show/Hide chords

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {


                                if (showChords.equals("Y")) {
                                    showChords = "N";
                                } else {
                                    showChords = "Y";
                                }
                                // Save the preferences
                                Preferences.savePreferences();
                                redrawTheLyricsTable(view);
                            }

                        } else if (childPosition==8) {
                            // Toggle menu swipe on/off
                            if (swipeDrawer.equals("Y")) {
                                swipeDrawer = "N";
                                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                                myToastMessage = getResources().getString(
                                        R.string.drawerswipe)
                                        + " " + getResources().getString(R.string.off);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                swipeDrawer = "Y";
                                mDrawerLayout
                                        .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                myToastMessage = getResources().getString(
                                        R.string.drawerswipe)
                                        + " " + getResources().getString(R.string.on);
                                ShowToast.showToast(FullscreenActivity.this);
                            }
                            Preferences.savePreferences();


                        } else if (childPosition==9) {
                            // Toggle song move swipe
                            if (swipeSet.equals("Y")) {
                                swipeSet = "S";
                                Preferences.savePreferences();
                                myToastMessage = getResources()
                                        .getString(R.string.swipeSet)
                                        + " "
                                        + getResources().getString(R.string.on_set);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else if (swipeSet.equals("S")) {
                                swipeSet = "N";
                                Preferences.savePreferences();
                                myToastMessage = getResources()
                                        .getString(R.string.swipeSet)
                                        + " "
                                        + getResources().getString(R.string.off);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                swipeSet = "Y";
                                Preferences.savePreferences();
                                myToastMessage = getResources()
                                        .getString(R.string.swipeSet)
                                        + " "
                                        + getResources().getString(R.string.on);
                                ShowToast.showToast(FullscreenActivity.this);
                            }


                        } else if (childPosition==10) {
                            // Assign custom gestures
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, GestureOptions.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();

                        } else if (childPosition==11) {
                            // Hide top menu bar on/off
                            if (hideactionbaronoff.equals("Y")) {
                                hideactionbaronoff = "N";
                                myToastMessage = getResources()
                                        .getString(R.string.options_options_hidebar)
                                        + " - "
                                        + getResources().getString(R.string.off);
                                ShowToast.showToast(FullscreenActivity.this);
                                getActionBar().show();
                            } else {
                                hideactionbaronoff = "Y";
                                myToastMessage = getResources()
                                        .getString(R.string.options_options_hidebar)
                                        + " - "
                                        + getResources().getString(R.string.on);
                                ShowToast.showToast(FullscreenActivity.this);
                                getActionBar().hide();
                            }
                            Preferences.savePreferences();
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, FullscreenActivity.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();



                        } else if (childPosition==12) {
                            // Change colours
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this,
                                    ChangeDisplayPreferences.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();


                        } else if (childPosition==13) {
                            // Change fonts
                            Intent intent2 = new Intent();
                            intent2.setClass(FullscreenActivity.this, ChangeFonts.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent2);
                            finish();


                        } else if (childPosition==14) {
                            // Assign foot pedal
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, setPageTurns.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();

                        } else if (childPosition==15) {
                            // Help (online)
                            String url = "https://sites.google.com/site/opensongtabletmusicviewer/home";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);


                        } else if (childPosition==16) {
                            // Manage OpenSong storage
                            tryKillPads();
                            tryKillMetronome();
                            Intent intent_stop = new Intent();
                            intent_stop.setClass(FullscreenActivity.this, StorageChooser.class);
                            startActivity(intent_stop);
                            finish();

                        } else if (childPosition==17) {
                            // Import OnSong backup
                            onSongImport();

                        } else if (childPosition==18) {
                            // Change language

                            int positionselected = -1;

                            mDrawerLayout.closeDrawer(expListViewOption);
                            mDrawerLayout.closeDrawer(expListViewSong);
                            if (!languageToLoad.isEmpty()) {
                                if (languageToLoad.equals("af")) {positionselected = 0;}
                                else if (languageToLoad.equals("cz")) {positionselected = 1;}
                                else if (languageToLoad.equals("de")) {positionselected = 2;}
                                else if (languageToLoad.equals("en")) {positionselected = 3;}
                                else if (languageToLoad.equals("es")) {positionselected = 4;}
                                else if (languageToLoad.equals("fr")) {positionselected = 5;}
                                else if (languageToLoad.equals("hu")) {positionselected = 6;}
                                else if (languageToLoad.equals("it")) {positionselected = 7;}
                                else if (languageToLoad.equals("ja")) {positionselected = 8;}
                                else if (languageToLoad.equals("pl")) {positionselected = 9;}
                                else if (languageToLoad.equals("pt")) {positionselected = 10;}
                                else if (languageToLoad.equals("ru")) {positionselected = 11;}
                                else if (languageToLoad.equals("zh")) {positionselected = 12;}
                            }
                            AlertDialog.Builder languageDialog = new AlertDialog.Builder(FullscreenActivity.this);
                            languageDialog.setTitle(getResources().getString(R.string.language))
                                    .setSingleChoiceItems(getResources().getStringArray(R.array.languagelist), positionselected, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            if (arg1==0) { //af
                                                tempLanguage = "af";
                                            } else if (arg1==1) { //cz
                                                tempLanguage = "cz";
                                            } else if (arg1==2) { //de
                                                tempLanguage = "de";
                                            } else if (arg1==3) { //en
                                                tempLanguage = "en";
                                            } else if (arg1==4) { //es
                                                tempLanguage = "es";
                                            } else if (arg1==5) { //fr
                                                tempLanguage = "fr";
                                            } else if (arg1==6) { //hu
                                                tempLanguage = "hu";
                                            } else if (arg1==7) { //it
                                                tempLanguage = "it";
                                            } else if (arg1==8) { //ja
                                                tempLanguage = "ja";
                                            } else if (arg1==9) { //pl
                                                tempLanguage = "pl";
                                            } else if (arg1==10) { //pt
                                                tempLanguage = "pt";
                                            } else if (arg1==11) { //ru
                                                tempLanguage = "ru";
                                            } else if (arg1==12) { //zh
                                                tempLanguage = "zh";
                                            }
                                        }
                                    });

                            languageDialog.setPositiveButton(getResources().getString(R.string.docontinue).toString(),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            languageToLoad = tempLanguage;
                                            Preferences.savePreferences();
                                            FullscreenActivity.this.recreate();
                                        }
                                    });



                            languageDialog.setNegativeButton(
                                    getResources().getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            // Cancelled.
                                            return;
                                        }
                                    });

                            languageDialog.show();

                        } else if (childPosition==19) {
                            // Refresh Songs folders
                            prepareSongMenu();
                            mDrawerLayout.closeDrawer(expListViewOption);
                            mDrawerLayout.openDrawer(expListViewSong);



                        } else if (childPosition==20) {
                            // Splash screen
                            // First though, set the preference to show the current version
                            // Otherwise it won't show the splash screen
                            SharedPreferences settings = getSharedPreferences("mysettings",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt("showSplashVersion", 0);
                            editor.commit();
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, SettingsActivity.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();

                        }
                    }
                }
                return false;
            }
        });
    }

    public void gesture1() {
        mDrawerLayout.openDrawer(expListViewSong);
        mDrawerLayout.openDrawer(expListViewOption);
        wasscrolling = false;
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
    }

    public void gesture3() {
        if (whichSongFolder.equals(mainfoldername)) {
            whatsongforsetwork = "$**_" + songfilename + "_**$";
        } else {
            whatsongforsetwork = "$**_" + whichSongFolder + "/"
                    + songfilename + "_**$";
        }

        // Allow the song to be added, even if it is already there
        mySet = mySet + whatsongforsetwork;
        // Tell the user that the song has been added.
        myToastMessage = "\"" + songfilename + "\" "
                + getResources().getString(R.string.addedtoset);
        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vb.vibrate(25);
        ShowToast.showToast(FullscreenActivity.this);

        // Save the set and other preferences
        Preferences.savePreferences();

        SetActions.prepareSetList();
        invalidateOptionsMenu();
        prepareOptionMenu();

        // Show the current set
        showCurrentSet(view);

        // Hide the menus - 1 second after opening the Option menu,
        // close it (1000ms total)
        Handler optionMenuFlickClosed = new Handler();
        optionMenuFlickClosed.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(expListViewOption);
            }
        }, 1000); // 1000ms delay

    }

    public void gesture4() {
        redrawTheLyricsTable(main_page);
    }



    private void toggleActionBar() {
        ActionBar actionBar = getActionBar();
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        if(actionBar != null) {
            if (wasscrolling || scrollbutton) {
                if (hideactionbaronoff.equals("Y") && !expListViewSong.isFocused() && !expListViewSong.isShown() && !expListViewOption.isFocused() && !expListViewOption.isShown()){
                    actionBar.hide();
                }
            } else if (!expListViewSong.isFocused() && !expListViewSong.isShown() && !expListViewOption.isFocused() && !expListViewOption.isShown()) {
                if (actionBar.isShowing() && hideactionbaronoff.equals("Y")) {
                    delayactionBarHide.postDelayed(hideActionBarRunnable, 500);
                    actionbarbutton = false;
                } else {
                    actionBar.show();
                    // Set a runnable to hide it after 2 seconds
                    if (hideactionbaronoff.equals("Y")) {
                        delayactionBarHide.postDelayed(hideActionBarRunnable, 3000); // 3000ms delay
                    }
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            // Determine the correct thing to do
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            View v = getWindow().getDecorView();

            int currentapiVersion = android.os.Build.VERSION.SDK_INT;

            if (currentapiVersion >= 17) {
                // Capable of dual head presentations
                dualDisplayCapable = "Y";
            }

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        }
    }

    // This bit listens for key presses (for page turn and scroll)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Set a runnable to reset swipe back to original value after 1 second

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // User wants the menu
            if (mDrawerLayout.isDrawerOpen(expListViewOption)) {
                mDrawerLayout.closeDrawer(expListViewOption);
            }
            if (mDrawerLayout.isDrawerOpen(expListViewSong)) {
                mDrawerLayout.closeDrawer(expListViewSong);
            }

        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (scrollstickyholder.getVisibility() == View.VISIBLE) {
                // Hide the sticky
                scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
                // After 500ms, make them invisible
                Handler delayhidenote = new Handler();
                delayhidenote.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mySticky.setVisibility(View.INVISIBLE);
                        scrollstickyholder.setVisibility(View.GONE);
                    }
                }, 500); // 500ms delay
                stickynotes.setAlpha(0.3f);

            } else {
                // Ask the user if they want to exit.  Give them an are you sure prompt
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                // Yes button clicked
                                try {
                                    killPad1(padButton);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    killPad2(padButton);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                // No button clickedvisibility
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                builder.setMessage(getResources().getString(R.string.exit))
                        .setPositiveButton(getResources().getString(R.string.yes),
                                dialogClickListener)
                        .setNegativeButton(getResources().getString(R.string.no),
                                dialogClickListener).show();
                return false;
            }
        } else if (keyCode == pageturner_NEXT) {

            if (tempswipeSet.equals("disable")) {
                return false; // Currently disabled swiping to let screen finish drawing.
            }
            tempswipeSet = "disable"; // Temporarily suspend swiping or next song			
            // Set a runnable to re-enable swipe
            Handler allowswipe = new Handler();
            allowswipe.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tempswipeSet = "enable"; // enable swipe after short delay
                }
            }, 1800); // 1800ms delay


            // If we are viewing a set, move to the next song.
            // If we are not in a set, check for the next song. If it doesn't exist, do nothing
            if (setView.equals("N")) {
                // Currently not in set mode.  Get song position in list
                next_song = currentSongIndex + 1;
                if (next_song > mSongFileNames.length - 1) {
                    // No next song!
                    next_song = currentSongIndex;
                }
            }
            // Check if we can scroll first
            if (toggleScrollBeforeSwipe.equals("Y")) {

                int height = ((ViewGroup) scrollpage).getChildAt(0).getMeasuredHeight() - scrollpage.getHeight();

                if (height > scrollpage.getScrollY()) {
                    // Need to scroll down first
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    scrollpage.smoothScrollBy(0,(int) (0.9 * metrics.heightPixels));

                    // Set a runnable to check the scroll position after 1 second
                    delaycheckscroll.postDelayed(checkScrollPosition, 1000);
                    return false;
                }
            }

            // OK can't scroll, so move to the next song if we can

            if (isPDF && pdfPageCurrent<(pdfPageCount-1)) {
                pdfPageCurrent = pdfPageCurrent + 1;
                redrawTheLyricsTable(main_page);
                return false;
            } else {
                pdfPageCurrent = 0;
            }

            //main_page.requestFocus();
            whichDirection = "R2L";
            if (setSize >= 2 && setView.equals("Y") && indexSongInSet >= 0 && indexSongInSet < (setSize - 1)) {
                indexSongInSet += 1;
                doMoveInSet();
                expListViewOption.expandGroup(0);
                expListViewOption.setSelection(indexSongInSet);
                return true;
            } else {
                if (setView.equals("Y") || currentSongIndex == next_song) {
                    // Already at the end!
                    myToastMessage = getResources().getText(R.string.lastsong).toString();
                    ShowToast.showToast(FullscreenActivity.this);
                    return false;
                }
                // Not in set mode, so go to the next song
                songfilename = mSongFileNames[next_song];
                invalidateOptionsMenu();
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (needtorefreshsongmenu) {
                    prepareSongMenu();
                    needtorefreshsongmenu = false;
                }
                redrawTheLyricsTable(main_page);
            }

        } else if (keyCode == pageturner_PREVIOUS) {

            if (tempswipeSet.equals("disable")) {
                return false; // Currently disabled swiping to let screen finish drawing.
            }
            tempswipeSet = "disable"; // Temporarily suspend swiping or next song			
            // Set a runnable to re-enable swipe
            Handler allowswipe = new Handler();
            allowswipe.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tempswipeSet = "enable"; // enable swipe after short delay
                }
            }, 1800); // 1800ms delay

            // If we are viewing a set, move to the next song.
            // If we are not in a set, check for the next song. If it doesn't exist, do nothing
            if (setView.equals("N")) {
                // Currently not in set mode.  Get song position in list
                expListViewOption.expandGroup(1);
                prev_song = currentSongIndex - 1;
                if (prev_song < 1) {
                    // No next song!
                    prev_song = currentSongIndex;
                }
            }
            // Check if we can scroll first
            if (toggleScrollBeforeSwipe.equals("Y")) {
                //main_page.requestFocus();

                if (scrollpage.getScrollY() > 0) {
                    // Need to scroll up first
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    scrollpage.smoothScrollBy(0, (int) (-0.9 * metrics.heightPixels));

                    // Set a runnable to check the scroll position after 1 second
                    delaycheckscroll.postDelayed(checkScrollPosition, 1000);
                    return false;
                }
            }

            // OK can't scroll, so move to the previous song if we can
            if (isPDF && pdfPageCurrent>0) {
                pdfPageCurrent = pdfPageCurrent - 1;
                redrawTheLyricsTable(main_page);
                return false;
            } else {
                pdfPageCurrent = 0;
            }

            //main_page.requestFocus();
            whichDirection = "L2R";
            if (setSize >= 2 && setView.equals("Y") && indexSongInSet >= 1) {
                indexSongInSet -= 1;
                doMoveInSet();
                expListViewOption.expandGroup(0);
                expListViewOption.setSelection(indexSongInSet);
                return true;
            } else {
                if (setView.equals("Y") || currentSongIndex == prev_song) {
                    // Already at the start!
                    myToastMessage = getResources().getText(R.string.firstsong).toString();
                    ShowToast.showToast(FullscreenActivity.this);
                    return false;
                }
                // Not in set mode, so go to the next song
                songfilename = mSongFileNames[prev_song];
                invalidateOptionsMenu();
                expListViewOption.expandGroup(1);
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (needtorefreshsongmenu) {
                    prepareSongMenu();
                    needtorefreshsongmenu = false;
                }
                redrawTheLyricsTable(main_page);
            }

        } else if (keyCode == pageturner_UP) {
            if (tempswipeSet.equals("disable")) {
                return false; // Currently disabled swiping to let screen finish drawing.
            }
            tempswipeSet = "disable"; // Temporarily suspend swiping or next song			
            // Set a runnable to re-enable swipe
            Handler allowswipe = new Handler();
            allowswipe.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tempswipeSet = "enable"; // enable swipe after short delay
                }
            }, 1800); // 1800ms delay

            //main_page.requestFocus();
            // Scroll the screen up
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            scrollpage.smoothScrollBy(0, (int) (-0.9 * metrics.heightPixels));
            // Set a runnable to check the scroll position after 1 second
            delaycheckscroll.postDelayed(checkScrollPosition, 1000);

        } else if (keyCode == pageturner_DOWN) {
            if (tempswipeSet.equals("disable")) {
                return false; // Currently disabled swiping to let screen finish drawing.
            }
            tempswipeSet = "disable"; // Temporarily suspend swiping or next song			
            // Set a runnable to re-enable swipe
            Handler allowswipe = new Handler();
            allowswipe.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tempswipeSet = "enable"; // enable swipe after short delay
                }
            }, 1800); // 1800ms delay

            // Scroll the screen up
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            scrollpage.smoothScrollBy(0, (int) (0.9 * metrics.heightPixels));
            // Set a runnable to check the scroll position after 1 second
            delaycheckscroll.postDelayed(checkScrollPosition, 1000);

        } else if (keyCode == pageturner_PAD) {
            popupPad_startstop(padButton);

        } else if (keyCode == pageturner_AUTOSCROLL) {
            if (popupAutoscroll_stoporstart.equals("start")) {
                // user now wants to stop
                popupAutoscroll_stoporstart = "stop";
                popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
            } else {
                // user now wants to start
                popupAutoscroll_stoporstart = "start";
                popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.stop));
            }
            autoScroll(autoscrollButton);

        } else if (keyCode == pageturner_METRONOME) {
            popupMetronome_startstop(metronomeButton);

        }
        return false;
    }

    public void stickyNotes(View view) {
        if (view==mySticky) {
            // mySticky will be hidden
        }
        if (mySticky.getVisibility()==View.VISIBLE) {
            // Hide the sticky
            scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidenote = new Handler();
            delayhidenote.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mySticky.setVisibility(View.INVISIBLE);
                    scrollstickyholder.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
            stickynotes.setAlpha(0.3f);
        } else {
            // Opening a sticky note!
            // Hide other popups
            hidepopupPad();
            hidepopupChord();
            hidepopupAutoscroll();
            hidepopupMetronome();

            mySticky.setText(mNotes);
            mySticky.setVisibility(View.VISIBLE);
            scrollstickyholder.setVisibility(View.VISIBLE);
            scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_bottom));
            stickynotes.setAlpha(0.5f);
        }
    }

    public void pdfSelectPage(View view) {
        final int storecurrpage = pdfPageCurrent;
        pageseekbarpos = pdfPageCurrent;
        alreadyshowingpage = pdfPageCurrent;

        // Build a dialogue window and related bits that get modified/shown if needed
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FullscreenActivity.this);
        LinearLayout titleLayout = new LinearLayout(FullscreenActivity.this);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        TextView m_titleView = new TextView(FullscreenActivity.this);
        m_titleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        m_titleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        m_titleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white) );
        m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        dialogBuilder.setTitle(getResources().getText(R.string.pdf_selectpage).toString());
        LinearLayout pickpage = new LinearLayout(FullscreenActivity.this);
        pickpage.setOrientation(LinearLayout.VERTICAL);
        final TextView page_num = new TextView(FullscreenActivity.this);
        page_num.setText((1+pdfPageCurrent) + " / " + pdfPageCount);
        page_num.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Medium);
        page_num.setGravity(1);
        final SeekBar pageseekbar = new SeekBar(FullscreenActivity.this);
        pickpage.addView(page_num);
        pickpage.addView(pageseekbar);
        dialogBuilder.setView(pickpage);
        pageseekbar.setProgress(pageseekbarpos);
        pageseekbar.setMax(pdfPageCount-1);
        pageseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {

                pageseekbarpos = seekBar.getProgress();
                pdfPageCurrent = pageseekbarpos;
                if (pdfPageCurrent != alreadyshowingpage) {
                    alreadyshowingpage = pdfPageCurrent;
                    redrawTheLyricsTable(main_page);
                }

            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                pdfPageCurrent = seekBar.getProgress();
                page_num.setText((1+pdfPageCurrent) + " / " + (pdfPageCount));
            }
        });
        dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,
                                int which) {
                if (pdfPageCurrent != storecurrpage) {
                    pdfPageCurrent = storecurrpage;
                    redrawTheLyricsTable(main_page);
                }
            }
        });
        dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,
                                int which) {

                mDrawerLayout.closeDrawer(expListViewOption);
            }
        });
        dialogBuilder.show();


    }




    public void doScrollUp(View view) {
        // Scroll the screen up
        wasscrolling = true;
        scrollbutton = true;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //scrollpage.requestFocus();
        scrollpage.smoothScrollBy(0, (int) (-0.9 * metrics.heightPixels));
        // Set a runnable to check the scroll position after 1 second
        delaycheckscroll.postDelayed(checkScrollPosition, 1000);
        toggleActionBar();
    }

    public void doScrollDown(View view) {
        // Scroll the screen down
        wasscrolling = true;
        scrollbutton = true;

        //main_page.requestFocus();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //scrollpage.requestFocus();
        scrollpage.smoothScrollBy(0, (int) (+0.9 * metrics.heightPixels));
        // Set a runnable to check the scroll position after 1 second
        delaycheckscroll.postDelayed(checkScrollPosition, 1000);
        toggleActionBar();

    }

    public void fixSetActionButtons(Menu menu) {
        // If the saved preference to showSet is Y and the size of the current set isn't 0 
        // display the appropriate arrows in the title bar. 
        // If not, hide them (since they are there by default.
        // Use the stored mySet variable to make a new list for the Options menu.
        // Add a delimiter between songs
        mySet = mySet.replace("_**$$**_", "_**$%%%$**_");
        // Break the saved set up into a new String[]
        mSet = mySet.split("%%%");
        mSetList = mSet;
        // Restore the set back to what it was
        mySet = mySet.replace("_**$%%%$**_", "_**$$**_");
        // Purge the set so it only shows songfilenames
        setSize = mSetList.length;
        invalidateOptionsMenu();

        // Get the index in the set in case it has changed
        for (int s=0;s<setSize;s++) {
            if (mSetList[s].contains(songfilename) && (mSetList[s].contains(whichSongFolder)||whichSongFolder.equals(getResources().getString(R.string.mainfoldername)))) {
                indexSongInSet = s;
            }
        }

        if (indexSongInSet<0) {
            setView = "N";
        }
        if (setSize >= 1) {
            showCurrentSet(view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion>=14 && actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        if (currentapiVersion>=14 && actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
        this.menu = menu;
        MenuItem set_back = menu.findItem(R.id.set_back);
        MenuItem set_forward = menu.findItem(R.id.set_forward);
        if (setSize > 0 && setView.equals("Y")) {
            set_back.setVisible(true);
            set_forward.setVisible(true);
            set_back.getIcon().setAlpha(255);
            set_forward.getIcon().setAlpha(255);

        } else {
            set_back.setVisible(false);
            set_forward.setVisible(false);
        }
        // Now decide if the song being viewed has a song before it.  Otherwise disable the back button
        if (indexSongInSet < 1) {
            set_back.setEnabled(false);
            set_back.getIcon().setAlpha(30);
        }
        // Now decide if the song being viewed has a song after it.  Otherwise disable the forward button
        // Also need to check if last item in set is a pdf, does it have more pages?
        if ((isPDF && pdfPageCurrent<(pdfPageCount-1)) && indexSongInSet <= (setSize - 1)) {
            // LEAVE
        } else if (indexSongInSet >= (setSize - 1) && (!isPDF || (isPDF && pdfPageCurrent>=(pdfPageCount-1)))) {
            set_forward.setEnabled(false);
            set_forward.getIcon().setAlpha(30);
        }

        // Decide if presenter button is greyed out or not
        presentationMode = menu.findItem(R.id.present_mode);
        if (dualDisplayCapable.equals("N")) {
            presentationMode.setEnabled(true);
            presentationMode.getIcon().setAlpha(30);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPause() {
		/*		mPlayer1.stop();
		mPlayer1.release();
		mPlayer2.stop();
		mPlayer2.release();
		 */
        try {
            killPad1(padButton);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            killPad2(padButton);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();

    }


    @Override
    public void onResume() {
        //mPlayer.release();
        View v = getWindow().getDecorView();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        super.onResume();
    }










    // This bit draws the lyrics stored in the variable to the page.
    public void showLyrics(View view) throws IOException, IllegalStateException, InterruptedException {

        chordimageshere.removeAllViews();
        allchords = "";
        allchordscapo = "";

        mScaleFactor = 1.0f;
        findViewById(R.id.LyricDisplay_onecoldisplay).setScaleX(1.0f);
        findViewById(R.id.LyricDisplay_onecoldisplay).setScaleY(1.0f);

        if (toggleYScale.equals("N")) {
            needtoredraw = false;
            columnTest = 1;
            tempfontsize = mFontSize;
        }

        // Get the autoscroll info initialised
        if (mtask_autoscroll_music!=null) {
            mtask_autoscroll_music.cancel(true);
            //mtask_autoscroll_music = null;
        }
        autoscrollonoff = "off";
        autoscrollispaused = false;
        isautoscrolling = false;
        scrollpage.smoothScrollTo(0, 0);
        if (popupAutoscroll.getVisibility()!=View.VISIBLE) {
            autoscrollButton.setAlpha(0.3f);
        }


        if (!alreadyloaded && !isPDF) {
            // Load up the song
            pdfPageCurrent = 0;
            pdfPageCount = 0;

            try {
                LoadXML.loadXML();
                alreadyloaded = true;
            } catch (XmlPullParserException e1) {
                e1.printStackTrace();
            }
            if (needtorefreshsongmenu) {
                prepareSongMenu();
                needtorefreshsongmenu = false;
            }

            // If there isn't a key specified, make sure the padButton is turned off
            // User will be prompted to specify a key if they press the button again
            if ((mKey.isEmpty() || mKey.equals("")) && padson) {
                myToastMessage = getResources().getString(R.string.pad_error);
                ShowToast.showToast(FullscreenActivity.this);
                padPlayingToggle = false;
                padson = false;
                if (mPlayer1!=null) {
                    if (mPlayer1.isPlaying()) {
                        whichtofadeout = 1;
                        fadeout1 = true;
                        popupPad_startstopbutton.setText(getResources().getString(R.string.wait));
                        if (currentapiVersion>=16) {
                            popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.grey_button));
                        } else {
                            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_button));
                        }
                        popupPad_startstopbutton.setEnabled(false);
                        isfading1=false;
                        fadeOutBackgroundMusic1();
                    }
                }
                if (mPlayer2!=null) {
                    if (mPlayer2.isPlaying()) {
                        whichtofadeout = 2;
                        fadeout2=true;
                        popupPad_startstopbutton.setText(getResources().getString(R.string.wait));
                        if (currentapiVersion>=16) {
                            popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.grey_button));
                        } else {
                            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_button));
                        }
                        popupPad_startstopbutton.setEnabled(false);
                        isfading2=false;
                        fadeOutBackgroundMusic2();
                    }
                }
                if (popupPad.getVisibility()!=View.VISIBLE) {
                    padButton.setAlpha(0.3f);
                }
            }

            if ((mPlayer1==null && mPlayer2==null) || (mPlayer1!=null && !mPlayer1.isPlaying()) && mPlayer2!=null && !mPlayer2.isPlaying()) {
                popupPad_stoporstart="stop";
                padson=false;
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                if (currentapiVersion>=16) {
                    popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
                } else {
                    popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                }
                if (popupPad.getVisibility()!=View.VISIBLE) {
                    padButton.setAlpha(0.3f);
                }
                fadeout1 = false;
                fadeout2 = false;
            }

            // Set chord instrument
            int instrumentindex = 0;
            if (chordInstrument.equals("u")) {
                instrumentindex=1;
            } else if (chordInstrument.equals("m")) {
                instrumentindex=2;
            } else {
                instrumentindex=0;
            }

            // Set song key
            int keyindex=0;
            if (mKey.equals("A")) {keyindex=1;}
            else if (mKey.equals("A#")) {keyindex=2;}
            else if (mKey.equals("Bb")) {keyindex=3;}
            else if (mKey.equals("B")) {keyindex=4;}
            else if (mKey.equals("C")) {keyindex=5;}
            else if (mKey.equals("C#")) {keyindex=6;}
            else if (mKey.equals("Db")) {keyindex=7;}
            else if (mKey.equals("D")) {keyindex=8;}
            else if (mKey.equals("D#")) {keyindex=9;}
            else if (mKey.equals("Eb")) {keyindex=10;}
            else if (mKey.equals("E")) {keyindex=11;}
            else if (mKey.equals("F")) {keyindex=12;}
            else if (mKey.equals("F#")) {keyindex=13;}
            else if (mKey.equals("Gb")) {keyindex=14;}
            else if (mKey.equals("G")) {keyindex=15;}
            else if (mKey.equals("G#")) {keyindex=16;}
            else if (mKey.equals("Ab")) {keyindex=17;}
            else if (mKey.equals("Am")) {keyindex=18;}
            else if (mKey.equals("A#b")) {keyindex=19;}
            else if (mKey.equals("Bbm")) {keyindex=20;}
            else if (mKey.equals("Bm")) {keyindex=21;}
            else if (mKey.equals("Cm")) {keyindex=22;}
            else if (mKey.equals("C#m")) {keyindex=23;}
            else if (mKey.equals("Dbm")) {keyindex=24;}
            else if (mKey.equals("Dm")) {keyindex=25;}
            else if (mKey.equals("D#m")) {keyindex=26;}
            else if (mKey.equals("Ebm")) {keyindex=27;}
            else if (mKey.equals("Em")) {keyindex=28;}
            else if (mKey.equals("Fm")) {keyindex=29;}
            else if (mKey.equals("F#m")) {keyindex=30;}
            else if (mKey.equals("Gbm")) {keyindex=31;}
            else if (mKey.equals("Gm")) {keyindex=32;}
            else if (mKey.equals("G#m")) {keyindex=33;}
            else if (mKey.equals("Abm")) {keyindex=34;}
            popupPad_key.setSelection(keyindex);

            // Set the pad volume and pan
            int temp_padvol = (int) (100*padvol);
            popupPad_volume.setProgress(temp_padvol);
            popupPad_volume_text.setText(temp_padvol+" %");
            if (padpan.equals("left")) {
                popupPad_pan_text.setText("L");
                popupPad_pan.setProgress(0);
            } else if (padpan.equals("right")) {
                popupPad_pan_text.setText("R");
                popupPad_pan.setProgress(2);
            } else {
                popupPad_pan_text.setText("C");
                popupPad_pan.setProgress(1);
            }


            // Set the autoscroll values
            popupAutoscroll_delay.setProgress(autoScrollDelay);
            popupAutoscroll_delay_text.setText(autoScrollDelay+" s");
            popupAutoscroll_duration.setText(mDuration.replaceAll("[\\D]", ""));


            // Set time signature
            int timesigindex = 0;
            if (mTimeSig.equals("2/4")) {
                timesigindex = 1;
                beats = 2;
                noteValue = 4;
            } else if (mTimeSig.equals("3/4")) {
                timesigindex = 2;
                beats = 3;
                noteValue = 4;
            } else if (mTimeSig.equals("3/8")) {
                timesigindex = 3;
                beats = 3;
                noteValue = 8;
            } else if (mTimeSig.equals("4/4")) {
                timesigindex = 4;
                beats = 4;
                noteValue = 4;
            } else if (mTimeSig.equals("5/4")) {
                timesigindex = 5;
                beats = 5;
                noteValue = 4;
            } else if (mTimeSig.equals("5/8")) {
                timesigindex = 6;
                beats = 5;
                noteValue = 8;
            } else if (mTimeSig.equals("6/4")) {
                timesigindex = 7;
                beats = 6;
                noteValue = 4;
            } else if (mTimeSig.equals("6/8")) {
                timesigindex = 8;
                beats = 6;
                noteValue = 8;
            } else if (mTimeSig.equals("7/4")) {
                timesigindex = 9;
                beats = 7;
                noteValue = 4;
            } else if (mTimeSig.equals("7/8")) {
                timesigindex = 10;
                beats = 7;
                noteValue = 8;
            }
            popupMetronome_timesig.setSelection(timesigindex);
            int temposlider;
            try {
                temposlider = (int) Integer.parseInt(mTempo.replaceAll("[\\D]",""));
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
                temposlider = 39;
            }
            temposlider = temposlider - 39;

            if (temposlider<1) {
                temposlider=0;
                popupMetronome_tempo_text.setText(getResources().getString(R.string.notset));
            } else {
                popupMetronome_tempo_text.setText(mTempo+" "+getResources().getString(R.string.bpm));
            }
            popupMetronome_tempo.setProgress(temposlider);

            // Set the metronome volume and pan
            int temp_metronomevol = (int) (100*metronomevol);
            popupMetronome_volume.setProgress(temp_metronomevol);
            popupMetronome_volume_text.setText(temp_metronomevol+" %");
            if (metronomepan.equals("left")) {
                popupMetronome_pan_text.setText("L");
                popupMetronome_pan.setProgress(0);
            } else if (metronomepan.equals("right")) {
                popupMetronome_pan_text.setText("R");
                popupMetronome_pan.setProgress(2);
            } else {
                popupMetronome_pan_text.setText("C");
                popupMetronome_pan.setProgress(1);
            }
            // Alert the user for no timesig/tempo
            if (timesigindex==0) {
                if (metroTask!=null) {
                    metroTask.cancel(true);
                    metroTask.stop();
                    if (metronomeonoff.equals("on")) {
                        promptTimeSig();
                    }
                }

            } else if (temposlider==0) {
                if (metroTask!=null) {
                    metroTask.cancel(true);
                    metroTask.stop();
                    if (metronomeonoff.equals("on")) {
                        promptTempo();
                    }
                }
            }





            // If metronome is playing and tempo and timesig are good,
            // Stop the original metronome and start a new one
            if (metroTask!=null) {
                metroTask.cancel(true);
                metroTask.stop();
            }
            if (metronomeonoff.equals("on") && temposlider!=0 && timesigindex!=0) {
                bpm = (short) ((short)temposlider+39);
                metroTask = new MetronomeAsyncTask();
                Runtime.getRuntime().gc();
                metronomeonoff = "on";
                popupMetronome_stoporstart = "start";
                popupMetronome_startstopbutton.setText(getResources().getString(R.string.stop));
                metroTask.execute();
            }



            // Stop restarting the pads if changing portrait/landscape
            if (!orientationchanged && padson) {
                playPads(view);
                Log.d("showLyrics","playPads command");
            }


            // Now, reset the orientation.
            orientationchanged = false;


            // If song and sticky note exists, show it
            if (!isPDF && togglePageButtons.equals("Y")) {
                stickynotes.setVisibility(View.VISIBLE);
                autoscrollButton.setVisibility(View.VISIBLE);
                metronomeButton.setVisibility(View.VISIBLE);
                padButton.setVisibility(View.VISIBLE);
                linkButton.setVisibility(View.INVISIBLE);
                chordButton.setVisibility(View.VISIBLE);
            } else {
                stickynotes.setVisibility(View.INVISIBLE);
                autoscrollButton.setVisibility(View.INVISIBLE);
                metronomeButton.setVisibility(View.INVISIBLE);
                padButton.setVisibility(View.INVISIBLE);
                linkButton.setVisibility(View.INVISIBLE);
                chordButton.setVisibility(View.INVISIBLE);
            }


            // Refresh the song list
            expListViewSong.setSelection(currentSongIndex);

            // Strip out the lyrics, author, etc.
            LyricsDisplay.parseLyrics();

            // Split the song up into the bits we need
            LyricsDisplay.lookForSplitPoints();

            // Beautify the lyrics tags
            LyricsDisplay.replaceLyricsCode();

            // save preferences (current song being viewed)
            Preferences.savePreferences();

            invalidateOptionsMenu();

            // Make sure the tables are invisible while their content is prepared.
            lyricstable_onecolview.setVisibility(View.VISIBLE);
            lyricstable_twocolview.setVisibility(View.VISIBLE);
            lyricstable2_twocolview.setVisibility(View.VISIBLE);
            lyricstable_threecolview.setVisibility(View.VISIBLE);
            lyricstable2_threecolview.setVisibility(View.VISIBLE);
            lyricstable3_threecolview.setVisibility(View.VISIBLE);
            scrollpage_pdf.setVisibility(View.INVISIBLE);
            pdf_selectpage.setVisibility(View.INVISIBLE);
            scrollpage_onecol.setVisibility(View.INVISIBLE);
            scrollpage_twocol.setVisibility(View.INVISIBLE);
            scrollpage_threecol.setVisibility(View.INVISIBLE);

            uparrow.setVisibility(View.INVISIBLE);
            downarrow.setVisibility(View.INVISIBLE);
        }

        if (isPDF) {
            hidepopupAutoscroll();
            hidepopupMetronome();
            hidepopupPad();
            hidepopupChord();
            hidepopupSticky();

            padButton.setAlpha(0.3f);
            metronomeButton.setAlpha(0.3f);
            autoscrollButton.setAlpha(0.3f);

            //Stop Autoscroll
            if(mtask_autoscroll_music!=null){
                //mtask_autoscroll_music = null;
                mtask_autoscroll_music.cancel(true);
            }

            //Stop Metronome
            if (metroTask!=null) {
                metroTask.cancel(true);
                metroTask.stop();
            }
            padson = false;
            popupMetronome_stoporstart="stop";
            metronomeonoff="off";
            popupAutoscroll_stoporstart="stop";

            // Fix buttons
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
            popupPad_startstopbutton.setEnabled(true);
            popupMetronome_startstopbutton.setEnabled(true);
            popupAutoscroll_startstopbutton.setEnabled(true);
            if (currentapiVersion>=16) {
                popupPad_startstopbutton.setBackground(getResources().getDrawable(R.drawable.blue_button));
            } else {
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            }

        }

        if (isPDF && padson) {
            mKey="";
            padson = false;
            padPlayingToggle = false;
            if (mtask_fadeout_music1!=null) {
                mtask_fadeout_music1 = null;
            }
            if (mtask_fadeout_music2!=null) {
                mtask_fadeout_music2 = null;
            }
            fadeout1=false;
            fadeout2=false;
            if (mPlayer1!=null) {
                // We need to fade out mPlayer1
                if (mPlayer1.isPlaying()) {
                    // We need to fade this out over the next 8 seconds
                    whichtofadeout=1;
                    fadeout1=true;
                    fadeOutBackgroundMusic1();
                }
            }
            if (mPlayer2!=null) {
                // We need to fade out mPlayer2
                if (mPlayer2.isPlaying()) {
                    // We need to fade this out over the next 8 seconds
                    whichtofadeout=2;
                    fadeout2=true;
                    fadeOutBackgroundMusic2();
                }
            }
            if (popupPad.getVisibility()!=View.VISIBLE) {
                padButton.setAlpha(0.3f);
            }
        }

        if (columnTest == 1) {
            scrollpage = scrollpage_onecol;
        } else if (columnTest == 2) {
            scrollpage = scrollpage_twocol;
        } else if (columnTest == 3) {
            scrollpage = scrollpage_threecol;
        }

        if (isPDF) {
            scrollpage = scrollpage_pdf;
            needtoredraw = false;
            columnTest = 99;
        }



        // Set up a listener to wait for the tables to draw - one for each of the tables!

        ViewTreeObserver vto = scrollpage.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollpage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (needtoredraw && columnTest != 0) {
                    int width = 0;
                    int height = 0;
                    pageWidth = main_page.getMeasuredWidth();
                    pageHeight = main_page.getMeasuredHeight();

                    if (columnTest==1) {
                        width  = lyricstable_onecolview.getMeasuredWidth() + 8;
                        height = lyricstable_onecolview.getMeasuredHeight() + 8;
                        scaleX = pageWidth/width;
                        scaleY = pageHeight/height;
                        if (toggleYScale.equals("Y")) {
                            if (scaleX>scaleY) {
                                scaleX=scaleY;
                            } else if (toggleYScale.equals("W")) {
                                scaleY=scaleX;
                            }
                        }
                        onecolfontsize = scaleX;

                    } else if (columnTest==2) {
                        width  = lyricstable_twocolview.getMeasuredWidth() + lyricstable2_twocolview.getMeasuredWidth() + 16;
                        height = lyricstable_twocolview.getMeasuredHeight() + 8;
                        int height2 = lyricstable2_twocolview.getMeasuredHeight() + 8;
                        if (height2>height) {
                            height=height2;
                        }
                        scaleX = pageWidth/width;
                        scaleY = pageHeight/height;
                        if (toggleYScale.equals("Y")) {
                            if (scaleX>scaleY) {
                                scaleX=scaleY;
                            } else if (toggleYScale.equals("W")) {
                                scaleY=scaleX;
                            }
                        }
                        twocolfontsize = scaleX;

                    } else {
                        width  = lyricstable_threecolview.getMeasuredWidth() + lyricstable2_threecolview.getMeasuredWidth() + lyricstable3_threecolview.getMeasuredWidth() + 36;
                        height = lyricstable_threecolview.getMeasuredHeight() + 12;
                        int height2 = lyricstable2_threecolview.getMeasuredHeight() + 12;
                        int height3 = lyricstable3_threecolview.getMeasuredHeight() + 12;
                        if (height2>height) {
                            height = height2;
                        }
                        if (height3>height) {
                            height = height3;
                        }
                        scaleX = pageWidth/width;
                        scaleY = pageHeight/height;
                        if (toggleYScale.equals("Y")) {
                            if (scaleX>scaleY) {
                                scaleX=scaleY;
                            } else if (toggleYScale.equals("W")){
                                scaleY=scaleX;
                            }
                        }
                        threecolfontsize = scaleX;
                    }
                    if (columnTest==1 && toggleYScale.equals("Y")) {
                        needtoredraw = true;
                        columnTest=2;
                    } else if (columnTest==1 && !toggleYScale.equals("Y")) {
                        columnTest=1;
                        needtoredraw = false;
                        tempfontsize = mainfontsize*onecolfontsize - 0.4f;
                        tempsectionsize = sectionfontsize*onecolfontsize - 0.4f;
                        scrollpage = scrollpage_onecol;
                        try {
                            showLyrics(main_page);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (columnTest==2) {
                        needtoredraw = true;
                        columnTest=3;
                    } else if (columnTest==3) {
                        needtoredraw = true;
                        columnTest=0;
                    } else if (columnTest==0){
                        needtoredraw = true;
                    }
                    doanimate = false;
                    try {
                        showLyrics(main_page);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (needtoredraw && columnTest==0) {

                    if (threecolfontsize>onecolfontsize && threecolfontsize>twocolfontsize) {
                        tempfontsize = mainfontsize*threecolfontsize - 0.4f;
                        tempsectionsize = sectionfontsize*threecolfontsize - 0.4f;
                        scrollpage = scrollpage_threecol;
                        columnTest = 3;
                    } else if (twocolfontsize>onecolfontsize && twocolfontsize>threecolfontsize) {
                        tempfontsize = mainfontsize*twocolfontsize - 0.4f;
                        tempsectionsize = sectionfontsize*twocolfontsize - 0.4f;
                        scrollpage = scrollpage_twocol;
                        columnTest = 2;
                    } else {
                        tempfontsize = mainfontsize*onecolfontsize - 0.4f;
                        tempsectionsize = sectionfontsize*onecolfontsize - 0.4f;
                        scrollpage = scrollpage_onecol;
                        columnTest = 1;
                    }

                    needtoredraw = false;
                    try {
                        showLyrics(main_page);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    tempfontsize = mainfontsize;
                    tempsectionsize = sectionfontsize;
                    songTitleHolder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadeout));
                    songLoadingProgressBar.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadeout));
                    songLoadingProgressBar.setVisibility(View.INVISIBLE);
                    songTitleHolder.setVisibility(View.INVISIBLE);
                    if (columnTest == 1) {
                        scrollpage_onecol.setVisibility(View.VISIBLE);
                        scrollpage_pdf.setVisibility(View.GONE);
                        //pdf_selectpage.setVisibility(View.GONE);
                        scrollpage_twocol.setVisibility(View.GONE);
                        scrollpage_threecol.setVisibility(View.GONE);
                    } else if (columnTest == 2) {
                        scrollpage_twocol.setVisibility(View.VISIBLE);
                        scrollpage_pdf.setVisibility(View.GONE);
                        //pdf_selectpage.setVisibility(View.GONE);
                        scrollpage_onecol.setVisibility(View.GONE);
                        scrollpage_threecol.setVisibility(View.GONE);
                    } else if (columnTest == 3) {
                        scrollpage_threecol.setVisibility(View.VISIBLE);
                        scrollpage_pdf.setVisibility(View.GONE);
                        //pdf_selectpage.setVisibility(View.GONE);
                        scrollpage_onecol.setVisibility(View.GONE);
                        scrollpage_twocol.setVisibility(View.GONE);
                    } else {
                        scrollpage_pdf.setVisibility(View.VISIBLE);
                        //pdf_selectpage.setVisibility(View.VISIBLE);
                        scrollpage_onecol.setVisibility(View.GONE);
                        scrollpage_twocol.setVisibility(View.GONE);
                        scrollpage_threecol.setVisibility(View.GONE);

                    }
                    if (whichDirection.equals("L2R")) {
                        scrollpage.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_left));
                    } else {
                        scrollpage.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_right));
                    }
                }
            }
        });

        // Set the default view!  Set the theme colours
        if (mDisplayTheme.equals("Theme_Holo_Light") || mDisplayTheme.equals("Theme.Holo.Light")) {
            lyricsTextColor = light_lyricsTextColor;
            lyricsCapoColor = light_lyricsCapoColor;
            lyricsBackgroundColor = light_lyricsBackgroundColor;
            lyricsVerseColor = light_lyricsVerseColor;
            lyricsChorusColor = light_lyricsChorusColor;
            lyricsBridgeColor = light_lyricsBridgeColor;
            lyricsCommentColor = light_lyricsCommentColor;
            lyricsPreChorusColor = light_lyricsPreChorusColor;
            lyricsTagColor = light_lyricsTagColor;
            lyricsChordsColor = light_lyricsChordsColor;
            lyricsCustomColor = light_lyricsCustomColor;
            lyricsBoxColor = light_lyricsTextColor;
            metronomeColor = light_metronome;
        } else if (mDisplayTheme.equals("Theme_Holo") || mDisplayTheme.equals("Theme.Holo")) {
            lyricsTextColor = dark_lyricsTextColor;
            lyricsCapoColor = dark_lyricsCapoColor;
            lyricsBackgroundColor = dark_lyricsBackgroundColor;
            lyricsVerseColor = dark_lyricsVerseColor;
            lyricsChorusColor = dark_lyricsChorusColor;
            lyricsBridgeColor = dark_lyricsBridgeColor;
            lyricsCommentColor = dark_lyricsCommentColor;
            lyricsPreChorusColor = dark_lyricsPreChorusColor;
            lyricsTagColor = dark_lyricsTagColor;
            lyricsChordsColor = dark_lyricsChordsColor;
            lyricsCustomColor = dark_lyricsCustomColor;
            lyricsBoxColor = dark_lyricsTextColor;
            metronomeColor = dark_metronome;
        } else if (mDisplayTheme.equals("custom1")) {
            lyricsTextColor = custom1_lyricsTextColor;
            lyricsCapoColor = custom1_lyricsCapoColor;
            lyricsBackgroundColor = custom1_lyricsBackgroundColor;
            lyricsVerseColor = custom1_lyricsVerseColor;
            lyricsChorusColor = custom1_lyricsChorusColor;
            lyricsBridgeColor = custom1_lyricsBridgeColor;
            lyricsCommentColor = custom1_lyricsCommentColor;
            lyricsPreChorusColor = custom1_lyricsPreChorusColor;
            lyricsTagColor = custom1_lyricsTagColor;
            lyricsChordsColor = custom1_lyricsChordsColor;
            lyricsCustomColor = custom1_lyricsCustomColor;
            lyricsBoxColor = custom1_lyricsTextColor;
            metronomeColor = custom1_metronome;
        } else if (mDisplayTheme.equals("custom2")) {
            lyricsTextColor = custom2_lyricsTextColor;
            lyricsCapoColor = custom2_lyricsCapoColor;
            lyricsBackgroundColor = custom2_lyricsBackgroundColor;
            lyricsVerseColor = custom2_lyricsVerseColor;
            lyricsChorusColor = custom2_lyricsChorusColor;
            lyricsBridgeColor = custom2_lyricsBridgeColor;
            lyricsCommentColor = custom2_lyricsCommentColor;
            lyricsPreChorusColor = custom2_lyricsPreChorusColor;
            lyricsTagColor = custom2_lyricsTagColor;
            lyricsChordsColor = custom2_lyricsChordsColor;
            lyricsCustomColor = custom2_lyricsCustomColor;
            lyricsBoxColor = custom2_lyricsTextColor;
            metronomeColor = custom2_metronome;
        }

        findViewById(R.id.main_page).setBackgroundColor(lyricsBackgroundColor);
        scrollpage.setBackgroundColor(lyricsBackgroundColor);

        if (!isPDF) {
            // Set a variable to decide if capo chords should be shown
            showCapo = false;
            if (mCapoPrint.equals("true") &&
                    (mCapo.equals("1") || mCapo.equals("2") || mCapo.equals("3") ||
                            mCapo.equals("4") || mCapo.equals("5") || mCapo.equals("6") ||
                            mCapo.equals("7") || mCapo.equals("8") || mCapo.equals("9") ||
                            mCapo.equals("10") || mCapo.equals("11"))) {
                showCapo = true;
            }

            // Decide on the font being used
            if (mylyricsfontnum == 1) {
                lyricsfont = Typeface.MONOSPACE;
                commentfont = Typeface.MONOSPACE;
            } else if (mylyricsfontnum == 2) {
                lyricsfont = Typeface.SANS_SERIF;
                commentfont = Typeface.SANS_SERIF;
            } else if (mylyricsfontnum == 3) {
                lyricsfont = Typeface.SERIF;
                commentfont = Typeface.SERIF;
            } else if (mylyricsfontnum == 4) {
                lyricsfont = Typeface.createFromAsset(getAssets(),"fonts/FiraSansOT-Light.otf");
                commentfont = Typeface.createFromAsset(getAssets(),"fonts/FiraSans-LightItalic.otf");
            } else if (mylyricsfontnum == 5) {
                lyricsfont = Typeface.createFromAsset(getAssets(),"fonts/FiraSansOT-Regular.otf");
                commentfont = Typeface.createFromAsset(getAssets(),"fonts/FiraSans-Italic.otf");
            } else if (mylyricsfontnum == 6) {
                lyricsfont = Typeface.createFromAsset(getAssets(),"fonts/KaushanScript-Regular.otf");
                commentfont = Typeface.createFromAsset(getAssets(),"fonts/KaushanScript-Regular.otf");
            } else if (mylyricsfontnum == 7) {
                lyricsfont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Lig.ttf");
                commentfont = Typeface.createFromAsset(getAssets(),"fonts/Lato-LigIta.ttf");
            } else if (mylyricsfontnum == 8) {
                lyricsfont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Reg.ttf");
                commentfont = Typeface.createFromAsset(getAssets(),"fonts/Lato-RegIta.ttf");
            } else {
                lyricsfont = Typeface.DEFAULT;
                commentfont = Typeface.DEFAULT;
            }
            if (mychordsfontnum == 1) {
                chordsfont = Typeface.MONOSPACE;
            } else if (mychordsfontnum == 2) {
                chordsfont = Typeface.SANS_SERIF;
            } else if (mychordsfontnum == 3) {
                chordsfont = Typeface.SERIF;
            } else if (mychordsfontnum == 4) {
                chordsfont = Typeface.createFromAsset(getAssets(),"fonts/FiraSansOT-Light.otf");
            } else if (mychordsfontnum == 5) {
                chordsfont = Typeface.createFromAsset(getAssets(),"fonts/FiraSansOT-Regular.otf");
            } else if (mychordsfontnum == 6) {
                chordsfont = Typeface.createFromAsset(getAssets(),"fonts/KaushanScript-Regular.otf");
            } else if (mychordsfontnum == 7) {
                chordsfont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Lig.ttf");
            } else if (mychordsfontnum == 8) {
                chordsfont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Reg.ttf");
            } else {
                chordsfont = Typeface.DEFAULT;
            }


            // Go through each section with start and end lines
            // Split points are dealt with inline
            int startatline = 0;
            int endatline = numrowstowrite;

            lyricstable_onecolview.removeAllViews();
            lyricstable_twocolview.removeAllViews();
            lyricstable2_twocolview.removeAllViews();
            lyricstable_threecolview.removeAllViews();
            lyricstable2_threecolview.removeAllViews();
            lyricstable3_threecolview.removeAllViews();
            chordimageshere.removeAllViews();

            // This bit gets repeated for each colyricstable_onecolviewlumn (in 1, 2 and three col view)

            for (int x = startatline; x < endatline; x++) {
                int n = 0;
                if (x == 0) {
                    n = 0;
                } else {
                    n = x - 1;
                }
                // This is to avoid errors when there isn't a line+1
                int m = x + 1;
                if (x + 1 >= endatline) {
                    m = x;
                }

                TableLayout thistable = lyricstable_onecolview;
                // Specify the correct table to be used in drawing the stuff below....
                if (columnTest==1 || mScreenOrientation==Configuration.ORIENTATION_PORTRAIT || !botherwithcolumns) {
                    thistable = lyricstable_onecolview;
                }
                if (columnTest==2 && x<splitpoint) {
                    thistable = lyricstable_twocolview;
                }
                if (columnTest==2 && x>=splitpoint) {
                    thistable = lyricstable2_twocolview;
                }
                if (columnTest==3 && x<thirdsplitpoint) {
                    thistable = lyricstable_threecolview;
                }
                if (columnTest==3 && x>=thirdsplitpoint && x<twothirdsplitpoint) {
                    thistable = lyricstable2_threecolview;
                }
                if (columnTest==3 && x>=twothirdsplitpoint) {
                    thistable = lyricstable3_threecolview;
                }

                int lyrics_useThisBGColor = lyricsTextColor;
                int chords_useThisBGColor = lyricsVerseColor;
                int capo_useThisBGColor = lyricsVerseColor;
                float chords_useThisTextSize = tempfontsize;
                float lyrics_useThisTextSize = tempfontsize;
                Typeface lyrics_useThisFont = lyricsfont;

                if (x == 0) {
                    // This is the first line.  If showCapo is true, add a comment line with the capo information
                    if (showCapo && showChords.equals("Y")) {
                        TableLayout myCapoBox = new TableLayout(this);
                        TableRow myCapoBoxRow = new TableRow(this);
                        TableRow.LayoutParams lp = new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                        myCapoBoxRow.setLayoutParams(lp);
                        TextView tCapoBox = new TextView(this);
                        tCapoBox.setText(getResources().getString(R.string.edit_song_capo).toString() + " " + mCapo);
                        tCapoBox.setBackgroundColor(lyricsBackgroundColor);
                        tCapoBox.setTextColor(lyricsCapoColor);
                        tCapoBox.setTextSize(tempfontsize);
                        myCapoBoxRow.addView(tCapoBox);
                        myCapoBox.addView(myCapoBoxRow);
                        // Write the word CAPO if capo chords are being shown
                        thistable.addView(myCapoBox);

                    } else {
                        // No capo, do nothing
                    }
                }

                // Decide if this line is a chord line followed by a lyric line.
                // If so, we need to split it up so the spacing is right.
                if (whatisthisline[x].equals("chords") && (whatisthisline[m].equals("lyrics") || whatisthisline[m].equals("comment"))) {
                    // Right, we need to create a new table inside this row to hold 2 rows - chords and lyrics
                    TableLayout chords_n_lyrics = new TableLayout(this);

/*					// Add this line of chords to the combined string
					allchords = allchords + " " + myParsedLyrics[x];
*/
                    // Prepare this view
                    //testTable.addView(chords_n_lyrics);
                    thistable.addView(chords_n_lyrics);

                    // Ok, so we have a chord line first. Let's break it into an array each 1 character big
                    char[] chars = (myParsedLyrics[x]).toCharArray();
                    // Make it into an array
                    String[] chord_chars = new String[chars.length + 1]; // Added 1 to check 2nd last char (last is empty)
                    chord_chars[chord_chars.length - 1] = " "; // Set last one as empty
                    // chordnum 0 is the start of the line
                    int chordnum = 1;

                    // Go through the chord_chars array and count the chords
                    for (int i = 0; i < chord_chars.length - 2; i++) {
                        chord_chars[i] = String.valueOf(chars[i]);
                        chord_chars[i + 1] = String.valueOf(chars[i + 1]);
                        if (!chord_chars[i].equals(" ") && i == 0) {
                            // First char is a chord, so set the start chordnum to 1
                            chordnum = 1;

                        } else if (chord_chars[i].equals(" ") && !chord_chars[i + 1].equals(" ") && i != 0) {
                            // Found another chord (space then non-space) not at the start.
                            chordnum += 1;
                        }
                    }

                    // Set up the chord_chunk array size based on the number of chords
                    // Also, set up the character position of each chord
                    int[] chord_pos = new int[chordnum + 2];
                    chord_pos[0] = 0;
                    chord_pos[chordnum] = maxcharsinline;
                    int z = 1;
                    // Go back through the array and identify the start positions
                    for (int i = 1; i < chord_chars.length - 1; i++) {
                        if (!chord_chars[i].equals(" ") && i == 0) {
                            // First char is a chord, so set the start chord_pos to 0
                            chord_pos[z] = i;
                            z++;

                        } else if (chord_chars[i].equals(" ") && !chord_chars[i + 1].equals(" ") && i != 0) {
                            // Found another chord (space then non-space). Chord pos is i+1
                            chord_pos[z] = i + 1;
                            z++;
                        }
                    }

                    TableRow capo_chords_row = new TableRow(this);
                    TableRow chords_row = new TableRow(this);
                    capo_chords_row.setPadding(0, -(int) ((float)linespacing/3.0f), 0, 0);
                    chords_row.setPadding(0, -(int) ((float)linespacing/3.0f), 0, 0);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

                    chords_row.setLayoutParams(lp);
                    capo_chords_row.setLayoutParams(lp);

                    if (whatisthisblock[x].equals("chorus")) {
                        chords_useThisBGColor = lyricsChorusColor;
                        capo_useThisBGColor = lyricsChorusColor;
                    } else if (whatisthisblock[x].equals("verse")) {
                        chords_useThisBGColor = lyricsVerseColor;
                        capo_useThisBGColor = lyricsVerseColor;
                    } else if (whatisthisblock[x].equals("prechorus")) {
                        chords_useThisBGColor = lyricsPreChorusColor;
                        capo_useThisBGColor = lyricsPreChorusColor;
                    } else if (whatisthisblock[x].equals("bridge")) {
                        chords_useThisBGColor = lyricsBridgeColor;
                        capo_useThisBGColor = lyricsBridgeColor;
                    } else if (whatisthisblock[x].equals("tag")) {
                        chords_useThisBGColor = lyricsTagColor;
                        capo_useThisBGColor = lyricsTagColor;
                    } else if (whatisthisblock[x].equals("comment")) {
                        chords_useThisBGColor = lyricsCommentColor;
                        capo_useThisBGColor = lyricsCommentColor;
                    } else if (whatisthisblock[x].equals("custom")) {
                        chords_useThisBGColor = lyricsCustomColor;
                        capo_useThisBGColor = lyricsCustomColor;
                    } else {
                        chords_useThisBGColor = lyricsVerseColor;
                        capo_useThisBGColor = lyricsVerseColor;
                    }

                    for (int i = 0; i < chordnum; i++) {
                        // create a new TextView for each chord
                        TextView t = new TextView(this);
                        TextView tcapo = new TextView(this);

                        String chord = "";
                        tempChords = "";
                        temptranspChords = "";

                        if (i == 0) {
                            chord = myParsedLyrics[x].substring(0,chord_pos[1]).toString();
                            tempChords = chord;
                            if (showCapo) {
                                temptranspChords = chord;
                                Transpose.capoTranspose();
                                allchordscapo = allchordscapo + " " + temptranspChords;
                            }

                        } else {
                            chord = myParsedLyrics[x].substring(chord_pos[i], chord_pos[i+1]).toString();
                            tempChords = chord;
                            if (showCapo) {
                                temptranspChords = chord;
                                Transpose.capoTranspose();
                                allchordscapo = allchordscapo + " " + temptranspChords;
                            }
                        }

                        // Set the appropriate formats for the chord line

                        t.setText(chord);
                        tcapo.setText(temptranspChords);
                        t.setTypeface(chordsfont);
                        tcapo.setTypeface(chordsfont);
                        t.setTextSize(chords_useThisTextSize);
                        tcapo.setTextSize(chords_useThisTextSize);
                        t.setBackgroundColor(chords_useThisBGColor);
                        tcapo.setBackgroundColor(capo_useThisBGColor);
                        t.setTextColor(lyricsChordsColor);
                        tcapo.setTextColor(lyricsCapoColor);

                        if (showCapo && showChords.equals("Y")) {
                            capo_chords_row.addView(tcapo);
                        }
                        chords_row.addView(t);
                    }

                    chords_row.setBackgroundColor(chords_useThisBGColor);
                    capo_chords_row.setBackgroundColor(capo_useThisBGColor);

                    // What should we be showing?
                    // First option is that there are no capo chords, but the user wants chords
                    // This displays the normal chords only
                    if (showChords.equals("Y") && !showCapo) {
                        chords_n_lyrics.addView(chords_row, 0);
                        // Add this line of chords to the combined string
                        allchords = allchords + " " + myParsedLyrics[x];

                    } else if (showChords.equals("Y") && showCapo && capoDisplay.equals("both")) {
                        chords_n_lyrics.addView(chords_row, 0);
                        chords_n_lyrics.addView(capo_chords_row, 1);
                        // Add this line of chords to the combined string
                        allchords = allchords + " " + myParsedLyrics[x];
                        allchords = allchords + " " + allchordscapo;

                    } else if (showChords.equals("Y") && showCapo && capoDisplay.equals("capoonly")) {
                        chords_n_lyrics.addView(capo_chords_row, 0);
                        // Add this line of chords to the combined string
                        allchords = allchords + " " + allchordscapo;
                    }
					
					
/*					chords_n_lyrics.addView(chords_row, 0);

					if (showChords.equals("Y")) {
						if (showCapo) {
							chords_n_lyrics.addView(capo_chords_row, 1);
						}
					}
*/
                    TableRow lyrics_row = new TableRow(this);

                    // Now we have the positions, split the words into substrings and write each 
                    // substring as a textview within the row
                    // If this is a multiline verse, we need to sort the next verse lines as well
                    for (int i = 0; i < chordnum; i++) {
                        // create a new TextView
                        TextView t2 = new TextView(this);

                        t2.setTextColor(lyricsTextColor);
                        t2.setTypeface(lyrics_useThisFont);
                        t2.setTextSize(lyrics_useThisTextSize);
                        int temp_useThisBGColor = lyrics_useThisBGColor;

                        if (whatisthisblock[x + 1].equals("chorus")) {
                            temp_useThisBGColor = lyricsChorusColor;
                        } else if (whatisthisblock[x + 1].equals("verse")) {
                            temp_useThisBGColor = lyricsVerseColor;
                        } else if (whatisthisblock[x + 1].equals("prechorus")) {
                            temp_useThisBGColor = lyricsPreChorusColor;
                        } else if (whatisthisblock[x + 1].equals("bridge")) {
                            temp_useThisBGColor = lyricsBridgeColor;
                        } else if (whatisthisblock[x + 1].equals("tag")) {
                            temp_useThisBGColor = lyricsTagColor;
                        } else if (whatisthisblock[x + 1].equals("comment")) {
                            temp_useThisBGColor = lyricsCommentColor;
                            t2.setTypeface(commentfont);
                            t2.setTextSize(tempsectionsize);
                        } else if (whatisthisblock[x + 1].equals("custom")) {
                            temp_useThisBGColor = lyricsCustomColor;
                        } else {
                            temp_useThisBGColor = lyricsVerseColor;
                        }

                        t2.setBackgroundColor(temp_useThisBGColor);

                        String  temp_lyricstext = "";
                        if (i == 0) {
                            temp_lyricstext = myParsedLyrics[x + 1].substring(0,chord_pos[1]);
                            // Multilines
                            if (myParsedLyrics[x+1].indexOf("1")==0) {
                                if ((x+2)<numrowstowrite) {
                                    if (myParsedLyrics[x+2].indexOf("2")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 2].substring(0,chord_pos[1]);
                                    }
                                }
                                if ((x+3)<numrowstowrite) {
                                    if (myParsedLyrics[x+3].indexOf("3")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 3].substring(0,chord_pos[1]);
                                    }
                                }
                                if ((x+4)<numrowstowrite) {
                                    if (myParsedLyrics[x+4].indexOf("4")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 4].substring(0,chord_pos[1]);
                                    }
                                }
                                if ((x+5)<numrowstowrite) {
                                    if (myParsedLyrics[x+5].indexOf("5")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 5].substring(0,chord_pos[1]);
                                    }
                                }
                                if ((x+6)<numrowstowrite) {
                                    if (myParsedLyrics[x+6].indexOf("6")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 6].substring(0,chord_pos[1]);
                                    }
                                }
                                if ((x+7)<numrowstowrite) {
                                    if (myParsedLyrics[x+7].indexOf("7")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 7].substring(0,chord_pos[1]);
                                    }
                                }
                                if ((x+8)<numrowstowrite) {
                                    if (myParsedLyrics[x+8].indexOf("8")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 8].substring(0,chord_pos[1]);
                                    }
                                }
                                if ((x+9)<numrowstowrite) {
                                    if (myParsedLyrics[x+9].indexOf("9")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 9].substring(0,chord_pos[1]);
                                    }
                                }
                            }
                        } else {
                            temp_lyricstext = myParsedLyrics[x + 1].substring(chord_pos[i], chord_pos[i+1]);
                            // Multilines
                            if (myParsedLyrics[x+1].indexOf("1")==0) {
                                if ((x+2)<numrowstowrite) {
                                    if (myParsedLyrics[x+2].indexOf("2")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 2].substring(chord_pos[i], chord_pos[i+1]);
                                    }
                                }
                                if ((x+3)<numrowstowrite) {
                                    if (myParsedLyrics[x+3].indexOf("3")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 3].substring(chord_pos[i], chord_pos[i+1]);
                                    }
                                }
                                if ((x+4)<numrowstowrite) {
                                    if (myParsedLyrics[x+4].indexOf("4")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 4].substring(chord_pos[i], chord_pos[i+1]);
                                    }
                                }
                                if ((x+5)<numrowstowrite) {
                                    if (myParsedLyrics[x+5].indexOf("5")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 5].substring(chord_pos[i], chord_pos[i+1]);
                                    }
                                }
                                if ((x+6)<numrowstowrite) {
                                    if (myParsedLyrics[x+6].indexOf("6")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 6].substring(chord_pos[i], chord_pos[i+1]);
                                    }
                                }
                                if ((x+7)<numrowstowrite) {
                                    if (myParsedLyrics[x+7].indexOf("7")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 7].substring(chord_pos[i], chord_pos[i+1]);
                                    }
                                }
                                if ((x+8)<numrowstowrite) {
                                    if (myParsedLyrics[x+8].indexOf("8")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 8].substring(chord_pos[i], chord_pos[i+1]);
                                    }
                                }
                                if ((x+9)<numrowstowrite) {
                                    if (myParsedLyrics[x+9].indexOf("9")==0) {
                                        temp_lyricstext = temp_lyricstext + "\n"+ myParsedLyrics[x + 9].substring(chord_pos[i], chord_pos[i+1]);
                                    }
                                }
                            }
                        }

                        t2.setText(temp_lyricstext);
                        lyrics_row.addView(t2);
                    }
                    lyrics_useThisBGColor = lyricsVerseColor;

                    // Decide on the lyrics row background colour
                    if (whatisthisblock[x + 1].equals("chorus")) {
                        lyrics_useThisBGColor = lyricsChorusColor;
                    } else if (whatisthisblock[x + 1].equals("verse")) {
                        lyrics_useThisBGColor = lyricsVerseColor;
                    } else if (whatisthisblock[x + 1].equals("prechorus")) {
                        lyrics_useThisBGColor = lyricsPreChorusColor;
                    } else if (whatisthisblock[x + 1].equals("bridge")) {
                        lyrics_useThisBGColor = lyricsBridgeColor;
                    } else if (whatisthisblock[x + 1].equals("tag")) {
                        lyrics_useThisBGColor = lyricsTagColor;
                    } else if (whatisthisblock[x + 1].equals("comment")) {
                        lyrics_useThisBGColor = lyricsCommentColor;
                    } else if (whatisthisblock[x + 1].equals("custom")) {
                        lyrics_useThisBGColor = lyricsCustomColor;
                    } else {
                        lyrics_useThisBGColor = lyricsVerseColor;
                    }

                    lyrics_row.setBackgroundColor(lyrics_useThisBGColor);
                    lyrics_row.setPadding(0, -(int) ((float)linespacing/3.0f), 0, 0);


                    // What should we be showing?
                    // First option is that there are no capo chords, but the user wants chords
                    // This displays the normal chords only
                    if (showChords.equals("Y") && !showCapo) {
                        chords_n_lyrics.addView(lyrics_row, 1);
                    } else if (showChords.equals("Y") && showCapo && capoDisplay.equals("both")) {
                        chords_n_lyrics.addView(lyrics_row, 2);
                    } else if (showChords.equals("Y") && showCapo && capoDisplay.equals("capoonly")) {
                        chords_n_lyrics.addView(lyrics_row, 1);
                    } else {
                        chords_n_lyrics.addView(lyrics_row, 0);
                    }

					
					
/*					if (showChords.equals("Y")) {
						if (showCapo) {
							chords_n_lyrics.addView(lyrics_row, 2);
						} else {
							chords_n_lyrics.addView(lyrics_row, 1);
						}
					} else {
						chords_n_lyrics.addView(lyrics_row, 0);
					}
*/
                } else if (whatisthisline[x].equals("chords") && !whatisthisline[m].equals("lyrics")) {
                    // No blocking is needed, just add the entire row as one bit
                    TableLayout basicline = new TableLayout(this);
                    thistable.addView(basicline);
                    basicline.setPadding(0, 0, 0, 0);

/*					// Add this line of chords to the combined string
					allchords = allchords + " " + myParsedLyrics[x];
*/
                    // create a new TableRow
                    TableRow normalrow = new TableRow(this);
                    TableRow caponormalrow = new TableRow(this);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, 0);
                    normalrow.setPadding(0, -(int) ((float)linespacing/3.0f), 0, 0);
                    caponormalrow.setPadding(0, -(int) ((float)linespacing/3.0f), 0, 0);


                    // create a new TextView
                    TextView tbasic = new TextView(this);
                    TextView capotbasic = new TextView(this);
                    tbasic.setLayoutParams(lp);
                    capotbasic.setLayoutParams(lp);
                    tbasic.setText(myParsedLyrics[x]);
                    tbasic.setTextColor(lyricsChordsColor);
                    tbasic.setTypeface(chordsfont);
                    tbasic.setTextSize(tempfontsize);
                    tbasic.setPadding(0, 0, 0, 0);

                    if (showCapo) {
                        temptranspChords = myParsedLyrics[x];
                        Transpose.capoTranspose();
                        allchordscapo = allchordscapo + " " + temptranspChords;
                        capotbasic.setText(temptranspChords);
                        capotbasic.setTextColor(lyricsCapoColor);
                        capotbasic.setTypeface(chordsfont);
                        capotbasic.setTextSize(tempfontsize);
                        capotbasic.setPadding(0, 0, 0, 0);
                    }

                    // If line is a title, need to make the text size smaller
                    if (whatisthisline[x].equals("versetitle")
                            || whatisthisline[x].equals("chorustitle")
                            || whatisthisline[x].equals("prechorustitle")
                            || whatisthisline[x].equals("bridgetitle")
                            || whatisthisline[x].equals("tagtitle")
                            || whatisthisline[x].equals("customtitle")) {
                        tbasic.setTextColor(lyricsTextColor);
                        tbasic.setAlpha(0.8f);
                        tbasic.setTextSize(tempsectionsize);
                    }
                    // add the TextView new TableRow
                    normalrow.addView(tbasic);
                    if (showCapo && showChords.equals("Y")) {
                        caponormalrow.addView(capotbasic);
                    }

                    lyrics_useThisBGColor = lyricsVerseColor;
                    lyrics_useThisFont = lyricsfont;
                    lyrics_useThisTextSize = tempfontsize;

                    // Decide on the block of text
                    if (whatisthisblock[x].equals("verse")) {
                        lyrics_useThisBGColor = lyricsVerseColor;

                    } else if (whatisthisblock[x].equals("prechorus")) {
                        lyrics_useThisBGColor = lyricsPreChorusColor;

                    } else if (whatisthisblock[x].equals("chorus")) {
                        lyrics_useThisBGColor = lyricsPreChorusColor;

                    } else if (whatisthisblock[x].equals("bridge")) {
                        lyrics_useThisBGColor = lyricsBridgeColor;

                    } else if (whatisthisblock[x].equals("tag")) {
                        lyrics_useThisBGColor = lyricsTagColor;

                    } else if (whatisthisblock[x].equals("comment")) {
                        lyrics_useThisBGColor = lyricsCommentColor;
                        lyrics_useThisFont = commentfont;
                        lyrics_useThisTextSize = tempsectionsize;

                    } else if (whatisthisblock[x].equals("custom")) {
                        lyrics_useThisBGColor = lyricsCustomColor;

                    } else {
                        lyrics_useThisBGColor = lyricsVerseColor;

                    }

                    tbasic.setBackgroundColor(lyrics_useThisBGColor);
                    normalrow.setBackgroundColor(lyrics_useThisBGColor);
                    capotbasic.setBackgroundColor(lyrics_useThisBGColor);
                    caponormalrow.setBackgroundColor(lyrics_useThisBGColor);
                    //tbasic.setTypeface(lyrics_useThisFont);
                    tbasic.setTextSize(lyrics_useThisTextSize);
                    //capotbasic.setTypeface(lyrics_useThisFont);
                    capotbasic.setTextSize(lyrics_useThisTextSize);

                    if (!whatisthisline[x].equals("chords")) {
                        basicline.addView(normalrow);
                    } else if (whatisthisline[x].equals("chords") && showChords.equals("Y")) {
                        if (!showCapo) {
                            // Add this line of chords to the combined string
                            allchords = allchords + " " + myParsedLyrics[x];
                            basicline.addView(normalrow);
                        } else if (showCapo && capoDisplay.equals("both")) {
                            // Add this line of chords to the combined string
                            allchords = allchords + " " + myParsedLyrics[x];
                            allchords = allchords + " " + allchordscapo;
                            basicline.addView(normalrow);
                            basicline.addView(caponormalrow);
                        } else if (showCapo && capoDisplay.equals("capoonly")) {
                            // Add this line of chords to the combined string
                            allchords = allchords + " " + allchordscapo;
                            basicline.addView(caponormalrow);
                        }
                    }


                } else if (!whatisthisline[n].equals("chords")||whatisthisline[x].contains("title")) {
                    // No blocking is needed, just add the entire row as one bit
                    TableLayout basicline = new TableLayout(this);
                    thistable.addView(basicline);
                    basicline.setPadding(0, 0, 0, 0);

                    // create a new TableRow
                    TableRow normalrow = new TableRow(this);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, 0);
                    //normalrow.setPadding(0, -(int) ((float)tempfontsize/3.0f), 0, 0);

                    // create a new TextView
                    TextView tbasic = new TextView(this);
                    tbasic.setLayoutParams(lp);
                    tbasic.setText(myParsedLyrics[x]);
                    tbasic.setTextColor(lyricsTextColor);
                    tbasic.setTypeface(lyricsfont);
                    tbasic.setTextSize(tempfontsize);
                    tbasic.setPadding(0, 0, 0, 0);

                    // If line is a title, need to make the text size smaller
                    if (whatisthisline[x].equals("versetitle")
                            || whatisthisline[x].equals("chorustitle")
                            || whatisthisline[x].equals("prechorustitle")
                            || whatisthisline[x].equals("bridgetitle")
                            || whatisthisline[x].equals("tagtitle")
                            || whatisthisline[x].equals("customtitle")) {
                        tbasic.setTextColor(lyricsTextColor);
                        tbasic.setAlpha(0.8f);
                        tbasic.setTextSize(tempfontsize*0.6f);
                        tbasic.setPaintFlags(tbasic.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    }
                    // add the TextView new TableRow
                    // As long as it isn't a multiline 2-9 beginning
                    if (myParsedLyrics[x].indexOf("1")!=0 && myParsedLyrics[x].indexOf("2")!=0 && myParsedLyrics[x].indexOf("3")!=0 &&
                            myParsedLyrics[x].indexOf("4")!=0 && myParsedLyrics[x].indexOf("5")!=0 &&
                            myParsedLyrics[x].indexOf("6")!=0 && myParsedLyrics[x].indexOf("7")!=0 &&
                            myParsedLyrics[x].indexOf("8")!=0 && myParsedLyrics[x].indexOf("9")!=0) {
                        normalrow.addView(tbasic);

                    } else if (myParsedLyrics[x].indexOf("1")==0) {
                        // Since we have a multiline section, add the remaining lines needed
                        String newtext = myParsedLyrics[x];
                        if ((x+1)<numrowstowrite) {
                            if (myParsedLyrics[x+1].indexOf("2")==0) {
                                newtext = newtext + "\n"+ myParsedLyrics[x + 1];
                            }
                        }
                        if ((x+2)<numrowstowrite) {
                            if (myParsedLyrics[x+2].indexOf("3")==0) {
                                newtext = newtext + "\n"+ myParsedLyrics[x + 2];
                            }
                        }
                        if ((x+3)<numrowstowrite) {
                            if (myParsedLyrics[x+3].indexOf("4")==0) {
                                newtext = newtext + "\n"+ myParsedLyrics[x + 3];
                            }
                        }
                        if ((x+4)<numrowstowrite) {
                            if (myParsedLyrics[x+4].indexOf("5")==0) {
                                newtext = newtext + "\n"+ myParsedLyrics[x + 4];
                            }
                        }
                        if ((x+5)<numrowstowrite) {
                            if (myParsedLyrics[x+5].indexOf("6")==0) {
                                newtext = newtext + "\n"+ myParsedLyrics[x + 5];
                            }
                        }
                        if ((x+6)<numrowstowrite) {
                            if (myParsedLyrics[x+6].indexOf("7")==0) {
                                newtext = newtext + "\n"+ myParsedLyrics[x + 6];
                            }
                        }
                        if ((x+7)<numrowstowrite) {
                            if (myParsedLyrics[x+7].indexOf("8")==0) {
                                newtext = newtext + "\n"+ myParsedLyrics[x + 7];
                            }
                        }
                        if ((x+8)<numrowstowrite) {
                            if (myParsedLyrics[x+8].indexOf("9")==0) {
                                newtext = newtext + "\n"+ myParsedLyrics[x + 8];
                            }
                        }
                        tbasic.setText(newtext);
                        normalrow.addView(tbasic);

                    }

                    lyrics_useThisBGColor = lyricsVerseColor;
                    lyrics_useThisTextSize = tempfontsize;
                    lyrics_useThisFont = lyricsfont;

                    // Decide on the block of text
                    if (whatisthisblock[x].equals("verse")) {
                        lyrics_useThisBGColor = lyricsVerseColor;

                    } else if (whatisthisblock[x].equals("prechorus")) {
                        lyrics_useThisBGColor = lyricsPreChorusColor;

                    } else if (whatisthisblock[x].equals("chorus")) {
                        lyrics_useThisBGColor = lyricsChorusColor;

                    } else if (whatisthisblock[x].equals("bridge")) {
                        lyrics_useThisBGColor = lyricsBridgeColor;

                    } else if (whatisthisblock[x].equals("tag")) {
                        lyrics_useThisBGColor = lyricsTagColor;

                    } else if (whatisthisblock[x].equals("comment")) {
                        lyrics_useThisBGColor = lyricsCommentColor;
                        lyrics_useThisFont = commentfont;
                        lyrics_useThisTextSize = tempsectionsize;

                    } else if (whatisthisblock[x].equals("custom")) {
                        lyrics_useThisBGColor = lyricsCustomColor;

                    } else {
                        lyrics_useThisBGColor = lyricsVerseColor;

                    }

                    tbasic.setBackgroundColor(lyrics_useThisBGColor);
                    if (whatisthisline[x].equals("comment")) {
                        tbasic.setTextSize(tempsectionsize);
                    }
                    normalrow.setBackgroundColor(lyrics_useThisBGColor);

                    if (!whatisthisline[x].equals("chords")) {
                        basicline.addView(normalrow);
                    } else if (whatisthisline[x].equals("chords") && showChords.equals("Y")) {
                        basicline.addView(normalrow);
                    }
                }
            }


            top_songtitle.setText("");

            if (mAuthor.equals("")) {
                top_songtitle.setText(songfilename + "\n");
                mTempAuthor = "Unknown";
            } else {
                mTempAuthor = mAuthor.toString();
                top_songtitle.setText(songfilename + "\n" + mTempAuthor);
            }

        } else {
            Preferences.savePreferences();
            // is a pdf file
            // if we are able, render pdf to image
            // filePath represent path of Pdf document on storage
            File file = new File(dir+"/" + songfilename);
            String tempsongtitle = songfilename.replace(".pdf", "");
            tempsongtitle = tempsongtitle.replace(".PDF", "");
            mTitle = tempsongtitle;
            mAuthor = "";

            // FileDescriptor for file, it allows you to close file when you are
            // done with it
            ParcelFileDescriptor mFileDescriptor = null;
            try {
                mFileDescriptor = ParcelFileDescriptor.open(file,
                        ParcelFileDescriptor.MODE_READ_ONLY);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= 21) {
                // Capable of pdf rendering
                // PdfRenderer enables rendering a PDF document
                padButton.setVisibility(View.INVISIBLE);
                stickynotes.setVisibility(View.INVISIBLE);
                metronomeButton.setVisibility(View.INVISIBLE);
                autoscrollButton.setVisibility(View.INVISIBLE);
                chordButton.setVisibility(View.INVISIBLE);
                scrollstickyholder.setVisibility(View.GONE);

                PdfRenderer mPdfRenderer = null;
                if (currentapiVersion>=21) {
                    try {
                        mPdfRenderer = new PdfRenderer(mFileDescriptor);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                pdfPageCount = 0;
                if (currentapiVersion>=21) {
                    pdfPageCount = mPdfRenderer.getPageCount();
                }

                if (pdfPageCurrent>pdfPageCount) {
                    pdfPageCurrent = 0;
                }
                //				getActionBar().setTitle(tempsongtitle + "   " + (pdfPageCurrent+1)+"/"+pdfPageCount);
                top_songtitle.setText(tempsongtitle + "\n" + (pdfPageCurrent+1)+"/"+pdfPageCount);

                if (pdfPageCount>1 && togglePageButtons.equals("Y")) {
                    // Show page select button
                    pdf_selectpage.setVisibility(View.VISIBLE);
                } else {
                    pdf_selectpage.setVisibility(View.INVISIBLE);
                }

                stickynotes.setVisibility(View.INVISIBLE);
                scrollstickyholder.setVisibility(View.GONE);
                stickynotes.setAlpha(0.3f);
                // Open page 0
                PdfRenderer.Page mCurrentPage = null;;
                if (currentapiVersion>=21) {
                    mCurrentPage = mPdfRenderer.openPage(pdfPageCurrent);
                }

                // Get pdf size from page
                int pdfwidth = 1;
                int pdfheight = 1;
                if (currentapiVersion>=21) {
                    pdfwidth = mCurrentPage.getWidth();
                    pdfheight = mCurrentPage.getHeight();
                }
                int holderwidth = pdfwidth;
                int holderheight = pdfheight;

                int pagewidth = main_page.getWidth();
                int pageheight = main_page.getHeight();

                if (toggleYScale.equals("Y")) {
                    float xscale = (float)pagewidth/(float)pdfwidth;
                    float yscale = (float)pageheight/(float)pdfheight;
                    if (xscale>yscale) {
                        xscale = yscale;
                    } else {
                        yscale = xscale;
                    }
                    pdfheight = (int) ((float)pdfheight*(float)yscale);
                    pdfwidth = (int) ((float)pdfwidth*(float)xscale);
                    holderheight = pageheight;
                    holderwidth = pagewidth;

                } else if (toggleYScale.equals("W")){
                    pdfheight = (int) (((float)pagewidth/(float)pdfwidth) * (float)pdfheight);
                    pdfwidth = pagewidth;
                    holderheight = pdfheight;
                    holderwidth = pdfwidth;
                } else {
                    // This means pdf will never be bigger than needed (even if scale is off)
                    // This avoids massive files calling out of memory error
                    if (pdfwidth > pagewidth) {
                        pdfheight = (int) (((float)pagewidth/(float)pdfwidth) * (float)pdfheight);
                        pdfwidth = pagewidth;
                        holderheight = pdfheight;
                        holderwidth = pdfwidth;
                    }
                }
                if (pdfwidth==0) {
                    pdfwidth=1;
                }
                if (pdfheight==0) {
                    pdfheight=1;
                }
                //Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(), Bitmap.Config.ARGB_8888);
                Bitmap bitmap = Bitmap.createBitmap(pdfwidth, pdfheight, Bitmap.Config.ARGB_8888);

                // Pdf page is rendered on Bitmap
                if (currentapiVersion>=21) {
                    mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                }

                // Set rendered bitmap to ImageView (pdfView in my case)
                ImageView pdfView = (ImageView) findViewById(R.id.pdfView);

                pdfView.setImageBitmap(bitmap);
                pdfView.getLayoutParams().height = holderheight;
                pdfView.getLayoutParams().width = holderwidth;


                if (currentapiVersion>=21) {
                    mCurrentPage.close();
                    mPdfRenderer.close();
                }

                try {
                    mFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                scrollpage_pdf.scrollTo(0, 0);
                invalidateOptionsMenu();
            } else {
                // Not capable of pdf rendering
                myToastMessage = getResources().getString(R.string.nothighenoughapi);
                ShowToast.showToast(FullscreenActivity.this);
                pdf_selectpage.setVisibility(View.INVISIBLE);
                stickynotes.setVisibility(View.INVISIBLE);
                padButton.setVisibility(View.INVISIBLE);
                chordButton.setVisibility(View.INVISIBLE);
                stickynotes.setVisibility(View.INVISIBLE);
                metronomeButton.setVisibility(View.INVISIBLE);
                autoscrollButton.setVisibility(View.INVISIBLE);
                scrollstickyholder.setVisibility(View.GONE);
                ImageView pdfView = (ImageView) findViewById(R.id.pdfView);
                Drawable myDrawable = getResources().getDrawable(R.drawable.unhappy_android);
                pdfView.setImageDrawable(myDrawable);
                pdfView.getLayoutParams().height = main_page.getHeight();
                pdfView.getLayoutParams().width = main_page.getWidth();
                //				getActionBar().setTitle(tempsongtitle + " - " + getResources().getString(R.string.nothighenoughapi));
                top_songtitle.setText(tempsongtitle + "\n" + getResources().getString(R.string.nothighenoughapi));
                Preferences.savePreferences();
                invalidateOptionsMenu();


            }





        }

        FrameLayout.LayoutParams params_fix = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        if (toggleYScale.equals("N")) {
            params_fix.gravity = Gravity.LEFT;
            findViewById(R.id.scrollpage_onecol).scrollTo(0, 0);
            findViewById(R.id.horizontalScrollView1_onecolview).scrollTo(0, 0);
        } else {
            params_fix.gravity = Gravity.CENTER_HORIZONTAL;
        }
        findViewById(R.id.linearLayout2_onecolview).setLayoutParams(params_fix);


        // Organise the chords.
        // Only do this if needtoredraw = false;
        if (!needtoredraw) {
            prepareChords();
        }
    }

    public void prepareChords() {
        // Remove all whitespace between chords
        while (allchords.contains("  ")) {
            allchords = allchords.replaceAll("  ", " ");
        }

        // Get rid of other bits that shouldn't be there
        allchords = allchords.replace("(", "");
        allchords = allchords.replace(")", "");
        allchords = allchords.replace("*", "");
        allchords = allchords.replace("!", "");
        allchords = allchords.replace(";", "");
        allchords = allchords.replace(":", "");
        allchords = allchords.replace("*", "");

        unique_chords = new ArrayList<String>();
        allchords = allchords.trim();
        allchords_array = allchords.split(" ");
        if (allchords_array.length>0) {
            for (int f=0;f<allchords_array.length;f++) {
                if (!unique_chords.contains(allchords_array[f])) {
                    unique_chords.add(allchords_array[f]);
                }
            }
        }

        chordimageshere.removeAllViews();
        // Send the unique chords off to get the string layout
        // This will eventually be if guitar/ukelele/mandolin/piano/other
        for (int l=0;l<unique_chords.size();l++) {
            if (chordInstrument.equals("u")) {
                ChordDirectory.ukuleleChords(unique_chords.get(l));
            } else if (chordInstrument.equals("m")) {
                ChordDirectory.mandolinChords(unique_chords.get(l));
            } else {
                ChordDirectory.guitarChords(unique_chords.get(l));
            }


            // Prepare a new Horizontal Linear Layout for each chord
            TableRow chordview = new TableRow(this);
            TableLayout.LayoutParams tableRowParams=
                    new TableLayout.LayoutParams
                            (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);

            int leftMargin=10;
            int topMargin=10;
            int rightMargin=10;
            int bottomMargin=10;

            tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

            chordview.setLayoutParams(tableRowParams);
            TextView chordname = new TextView(this);
            ImageView image1 = new ImageView(this);
            ImageView image2 = new ImageView(this);
            ImageView image3 = new ImageView(this);
            ImageView image4 = new ImageView(this);
            ImageView image5 = new ImageView(this);
            ImageView image6 = new ImageView(this);
            ImageView image0 = new ImageView(this);

            if (chordInstrument.equals("g")) {
                // Initialise guitar strings
                String e6 = "";
                String a5 = "";
                String d4 = "";
                String g3 = "";
                String b2 = "";
                String e1 = "";
                String fret = "";

                if (chordnotes.length() > 0) {
                    e6 = chordnotes.substring(0, 1);
                }
                if (chordnotes.length() > 1) {
                    a5 = chordnotes.substring(1, 2);
                }
                if (chordnotes.length() > 2) {
                    d4 = chordnotes.substring(2, 3);
                }
                if (chordnotes.length() > 3) {
                    g3 = chordnotes.substring(3, 4);
                }
                if (chordnotes.length() > 4) {
                    b2 = chordnotes.substring(4, 5);
                }
                if (chordnotes.length() > 5) {
                    e1 = chordnotes.substring(5, 6);
                }
                if (chordnotes.length() > 7) {
                    fret = chordnotes.substring(7, 8);
                }

                // Prepare string e6
                if (e6.equals("0")) {
                    image6.setImageDrawable(l0);
                } else if (e6.equals("1")) {
                    image6.setImageDrawable(l1);
                } else if (e6.equals("2")) {
                    image6.setImageDrawable(l2);
                } else if (e6.equals("3")) {
                    image6.setImageDrawable(l3);
                } else if (e6.equals("4")) {
                    image6.setImageDrawable(l4);
                } else if (e6.equals("5")) {
                    image6.setImageDrawable(l5);
                } else {
                    image6.setImageDrawable(lx);
                }

                // Prepare string a5
                if (a5.equals("0")) {
                    image5.setImageDrawable(m0);
                } else if (a5.equals("1")) {
                    image5.setImageDrawable(m1);
                } else if (a5.equals("2")) {
                    image5.setImageDrawable(m2);
                } else if (a5.equals("3")) {
                    image5.setImageDrawable(m3);
                } else if (a5.equals("4")) {
                    image5.setImageDrawable(m4);
                } else if (a5.equals("5")) {
                    image5.setImageDrawable(m5);
                } else {
                    image5.setImageDrawable(mx);
                }

                // Prepare string d4
                if (d4.equals("0")) {
                    image4.setImageDrawable(m0);
                } else if (d4.equals("1")) {
                    image4.setImageDrawable(m1);
                } else if (d4.equals("2")) {
                    image4.setImageDrawable(m2);
                } else if (d4.equals("3")) {
                    image4.setImageDrawable(m3);
                } else if (d4.equals("4")) {
                    image4.setImageDrawable(m4);
                } else if (d4.equals("5")) {
                    image4.setImageDrawable(m5);
                } else {
                    image4.setImageDrawable(mx);
                }

                // Prepare string g3
                if (g3.equals("0")) {
                    image3.setImageDrawable(m0);
                } else if (g3.equals("1")) {
                    image3.setImageDrawable(m1);
                } else if (g3.equals("2")) {
                    image3.setImageDrawable(m2);
                } else if (g3.equals("3")) {
                    image3.setImageDrawable(m3);
                } else if (g3.equals("4")) {
                    image3.setImageDrawable(m4);
                } else if (g3.equals("5")) {
                    image3.setImageDrawable(m5);
                } else {
                    image3.setImageDrawable(mx);
                }

                // Prepare string b2
                if (b2.equals("0")) {
                    image2.setImageDrawable(m0);
                } else if (b2.equals("1")) {
                    image2.setImageDrawable(m1);
                } else if (b2.equals("2")) {
                    image2.setImageDrawable(m2);
                } else if (b2.equals("3")) {
                    image2.setImageDrawable(m3);
                } else if (b2.equals("4")) {
                    image2.setImageDrawable(m4);
                } else if (b2.equals("5")) {
                    image2.setImageDrawable(m5);
                } else {
                    image2.setImageDrawable(mx);
                }

                // Prepare string e1
                if (e1.equals("0")) {
                    image1.setImageDrawable(r0);
                } else if (e1.equals("1")) {
                    image1.setImageDrawable(r1);
                } else if (e1.equals("2")) {
                    image1.setImageDrawable(r2);
                } else if (e1.equals("3")) {
                    image1.setImageDrawable(r3);
                } else if (e1.equals("4")) {
                    image1.setImageDrawable(r4);
                } else if (e1.equals("5")) {
                    image1.setImageDrawable(r5);
                } else {
                    image1.setImageDrawable(rx);
                }

                // Prepare string e1
                if (fret.equals("1")) {
                    image0.setImageDrawable(f1);
                } else if (fret.equals("2")) {
                    image0.setImageDrawable(f2);
                } else if (fret.equals("3")) {
                    image0.setImageDrawable(f3);
                } else if (fret.equals("4")) {
                    image0.setImageDrawable(f4);
                } else if (fret.equals("5")) {
                    image0.setImageDrawable(f5);
                } else if (fret.equals("6")) {
                    image0.setImageDrawable(f6);
                } else if (fret.equals("7")) {
                    image0.setImageDrawable(f7);
                } else if (fret.equals("8")) {
                    image0.setImageDrawable(f8);
                } else if (fret.equals("9")) {
                    image0.setImageDrawable(f9);
                } else {
                    image0 = null;
                }

                chordview.addView(chordname);
                if (image0 != null) {
                    chordview.addView(image0);
                }
                if (image6 != null) {
                    chordview.addView(image6);
                }
                if (image5 != null) {
                    chordview.addView(image5);
                }
                if (image4 != null) {
                    chordview.addView(image4);
                }
                if (image3 != null) {
                    chordview.addView(image3);
                }
                if (image2 != null) {
                    chordview.addView(image2);
                }
                if (image1 != null) {
                    chordview.addView(image1);
                }


            } else if (chordInstrument.equals("u")) {
                // Initialise ukulele strings
                String g4 = "";
                String c3 = "";
                String e2 = "";
                String a1 = "";
                String fret = "";

                if (chordnotes.length() > 0) {
                    g4 = chordnotes.substring(0, 1);
                }
                if (chordnotes.length() > 1) {
                    c3 = chordnotes.substring(1, 2);
                }
                if (chordnotes.length() > 2) {
                    e2 = chordnotes.substring(2, 3);
                }
                if (chordnotes.length() > 3) {
                    a1 = chordnotes.substring(3, 4);
                }
                if (chordnotes.length() > 5) {
                    fret = chordnotes.substring(5, 6);
                }

                // Prepare string g4
                if (g4.equals("0")) {
                    image4.setImageDrawable(l0);
                } else if (g4.equals("1")) {
                    image4.setImageDrawable(l1);
                } else if (g4.equals("2")) {
                    image4.setImageDrawable(l2);
                } else if (g4.equals("3")) {
                    image4.setImageDrawable(l3);
                } else if (g4.equals("4")) {
                    image4.setImageDrawable(l4);
                } else {
                    image4.setImageDrawable(lx);
                }

                // Prepare string c3
                if (c3.equals("0")) {
                    image3.setImageDrawable(m0);
                } else if (c3.equals("1")) {
                    image3.setImageDrawable(m1);
                } else if (c3.equals("2")) {
                    image3.setImageDrawable(m2);
                } else if (c3.equals("3")) {
                    image3.setImageDrawable(m3);
                } else if (c3.equals("4")) {
                    image3.setImageDrawable(m4);
                } else if (c3.equals("5")) {
                    image3.setImageDrawable(m5);
                } else {
                    image3.setImageDrawable(mx);
                }

                // Prepare string e2
                if (e2.equals("0")) {
                    image2.setImageDrawable(m0);
                } else if (e2.equals("1")) {
                    image2.setImageDrawable(m1);
                } else if (e2.equals("2")) {
                    image2.setImageDrawable(m2);
                } else if (e2.equals("3")) {
                    image2.setImageDrawable(m3);
                } else if (e2.equals("4")) {
                    image2.setImageDrawable(m4);
                } else if (e2.equals("5")) {
                    image2.setImageDrawable(m5);
                } else {
                    image2.setImageDrawable(mx);
                }

                // Prepare string a1
                if (a1.equals("0")) {
                    image1.setImageDrawable(r0);
                } else if (a1.equals("1")) {
                    image1.setImageDrawable(r1);
                } else if (a1.equals("2")) {
                    image1.setImageDrawable(r2);
                } else if (a1.equals("3")) {
                    image1.setImageDrawable(r3);
                } else if (a1.equals("4")) {
                    image1.setImageDrawable(r4);
                } else if (a1.equals("5")) {
                    image1.setImageDrawable(r5);
                } else {
                    image1.setImageDrawable(rx);
                }

                // Prepare fret
                if (fret.equals("1")) {
                    image0.setImageDrawable(f1);
                } else if (fret.equals("2")) {
                    image0.setImageDrawable(f2);
                } else if (fret.equals("3")) {
                    image0.setImageDrawable(f3);
                } else if (fret.equals("4")) {
                    image0.setImageDrawable(f4);
                } else if (fret.equals("5")) {
                    image0.setImageDrawable(f5);
                } else if (fret.equals("6")) {
                    image0.setImageDrawable(f6);
                } else if (fret.equals("7")) {
                    image0.setImageDrawable(f7);
                } else if (fret.equals("8")) {
                    image0.setImageDrawable(f8);
                } else if (fret.equals("9")) {
                    image0.setImageDrawable(f9);
                } else {
                    image0 = null;
                }

                chordview.addView(chordname);
                if (image0 != null) {
                    chordview.addView(image0);
                }
                if (image4 != null) {
                    chordview.addView(image4);
                }
                if (image3 != null) {
                    chordview.addView(image3);
                }
                if (image2 != null) {
                    chordview.addView(image2);
                }
                if (image1 != null) {
                    chordview.addView(image1);
                }

            } else if (chordInstrument.equals("m")) {
                // Initialise ukulele strings
                String g4 = "";
                String d3 = "";
                String a2 = "";
                String e1 = "";
                String fret = "";

                if (chordnotes.length() > 0) {
                    g4 = chordnotes.substring(0, 1);
                }
                if (chordnotes.length() > 1) {
                    d3 = chordnotes.substring(1, 2);
                }
                if (chordnotes.length() > 2) {
                    a2 = chordnotes.substring(2, 3);
                }
                if (chordnotes.length() > 3) {
                    e1 = chordnotes.substring(3, 4);
                }
                if (chordnotes.length() > 5) {
                    fret = chordnotes.substring(5, 6);
                }

                // Prepare string g4
                if (g4.equals("0")) {
                    image4.setImageDrawable(l0);
                } else if (g4.equals("1")) {
                    image4.setImageDrawable(l1);
                } else if (g4.equals("2")) {
                    image4.setImageDrawable(l2);
                } else if (g4.equals("3")) {
                    image4.setImageDrawable(l3);
                } else if (g4.equals("4")) {
                    image4.setImageDrawable(l4);
                } else if (g4.equals("5")) {
                    image4.setImageDrawable(l5);
                } else {
                    image4.setImageDrawable(lx);
                }

                // Prepare string d3
                if (d3.equals("0")) {
                    image3.setImageDrawable(m0);
                } else if (d3.equals("1")) {
                    image3.setImageDrawable(m1);
                } else if (d3.equals("2")) {
                    image3.setImageDrawable(m2);
                } else if (d3.equals("3")) {
                    image3.setImageDrawable(m3);
                } else if (d3.equals("4")) {
                    image3.setImageDrawable(m4);
                } else if (d3.equals("5")) {
                    image3.setImageDrawable(m5);
                } else {
                    image3.setImageDrawable(mx);
                }

                // Prepare string a2
                if (a2.equals("0")) {
                    image2.setImageDrawable(m0);
                } else if (a2.equals("1")) {
                    image2.setImageDrawable(m1);
                } else if (a2.equals("2")) {
                    image2.setImageDrawable(m2);
                } else if (a2.equals("3")) {
                    image2.setImageDrawable(m3);
                } else if (a2.equals("4")) {
                    image2.setImageDrawable(m4);
                } else if (a2.equals("5")) {
                    image2.setImageDrawable(m5);
                } else {
                    image2.setImageDrawable(mx);
                }

                // Prepare string e1
                if (e1.equals("0")) {
                    image1.setImageDrawable(r0);
                } else if (e1.equals("1")) {
                    image1.setImageDrawable(r1);
                } else if (e1.equals("2")) {
                    image1.setImageDrawable(r2);
                } else if (e1.equals("3")) {
                    image1.setImageDrawable(r3);
                } else if (e1.equals("4")) {
                    image1.setImageDrawable(r4);
                } else if (e1.equals("5")) {
                    image1.setImageDrawable(r5);
                } else {
                    image1.setImageDrawable(rx);
                }

                // Prepare fret
                if (fret.equals("1")) {
                    image0.setImageDrawable(f1);
                } else if (fret.equals("2")) {
                    image0.setImageDrawable(f2);
                } else if (fret.equals("3")) {
                    image0.setImageDrawable(f3);
                } else if (fret.equals("4")) {
                    image0.setImageDrawable(f4);
                } else if (fret.equals("5")) {
                    image0.setImageDrawable(f5);
                } else if (fret.equals("6")) {
                    image0.setImageDrawable(f6);
                } else if (fret.equals("7")) {
                    image0.setImageDrawable(f7);
                } else if (fret.equals("8")) {
                    image0.setImageDrawable(f8);
                } else if (fret.equals("9")) {
                    image0.setImageDrawable(f9);
                } else {
                    image0 = null;
                }

                chordview.addView(chordname);
                if (image0 != null) {
                    chordview.addView(image0);
                }
                if (image4 != null) {
                    chordview.addView(image4);
                }
                if (image3 != null) {
                    chordview.addView(image3);
                }
                if (image2 != null) {
                    chordview.addView(image2);
                }
                if (image1 != null) {
                    chordview.addView(image1);
                }

            }
            if (!chordnotes.contains("xxxx_") && !chordnotes.contains("xxxxxx_")) {
                chordimageshere.addView(chordview);
                chordname.setText(unique_chords.get(l));
                chordname.setTextColor(0xff000000);
                chordname.setTextSize(20);
            }

        }

    }




    public void openEditSong() {
        Intent editsong = new Intent(this, EditSong.class);
        tryKillPads();
        tryKillMetronome();
        startActivity(editsong);
        finish();
    }

    public void prepareToSave() throws Exception {
        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + mTitle.toString() + "</title>\n";
        myNEWXML += "  <author>" + mAuthor + "</author>\n";
        myNEWXML += "  <copyright>" + mCopyright + "</copyright>\n";
        myNEWXML += "  <presentation>" + mPresentation + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + mHymnNumber + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + mCapoPrint + "\">" + mCapo + "</capo>\n";
        myNEWXML += "  <tempo>" + mTempo + "</tempo>\n";
        myNEWXML += "  <time_sig>" + mTimeSig + "</time_sig>\n";
        myNEWXML += "  <duration>" + mDuration + "</duration>\n";
        myNEWXML += "  <ccli>" + mCCLI + "</ccli>\n";
        myNEWXML += "  <theme>" + mTheme + "</theme>\n";
        myNEWXML += "  <alttheme>" + mAltTheme + "</alttheme>\n";
        myNEWXML += "  <user1>" + mUser1 + "</user1>\n";
        myNEWXML += "  <user2>" + mUser2 + "</user2>\n";
        myNEWXML += "  <user3>" + mUser3 + "</user3>\n";
        myNEWXML += "  <key>" + mKey + "</key>\n";
        myNEWXML += "  <aka>" + mAka + "</aka>\n";
        myNEWXML += "  <key_line>" + mKeyLine + "</key_line>\n";
        myNEWXML += "  <books>" + mBooks + "</books>\n";
        myNEWXML += "  <midi>" + mMidi + "</midi>\n";
        myNEWXML += "  <midi_index>" + mMidiIndex + "</midi_index>\n";
        myNEWXML += "  <pitch>" + mPitch + "</pitch>\n";
        myNEWXML += "  <restrictions>" + mRestrictions + "</restrictions>\n";
        myNEWXML += "  <notes>" + mNotes + "</notes>\n";
        myNEWXML += "  <lyrics>" + mLyrics + "</lyrics>\n";
		/*if (!mStyle.equals("") && !mStyleIndex.equals("")) {
			myNEWXML += "  <style index=\"" + mStyleIndex + "\">" + mStyle + "</style>\n";			
		} else if (!mStyle.equals("") && mStyleIndex.equals("")){
			myNEWXML += "  <style>" + mStyle + "</style>\n";	
		}*/
        myNEWXML += "  <linked_songs>" + mLinkedSongs + "</linked_songs>\n";
        myNEWXML += "  <pad_file></pad_file>\n";
        if (!mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + mExtraStuff1 + "\n";
        }
        myNEWXML += "  " + mExtraStuff1 + "\n";
        if (!mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

        mynewXML = myNEWXML;

        // Makes sure all & are replaced with &amp;
        mynewXML = mynewXML.replace("&amp;","&");
        mynewXML = mynewXML.replace("&","&amp;");

        // Now write the modified song
        FileOutputStream overWrite = new FileOutputStream(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename,false);
        overWrite.write(mynewXML.getBytes());
        overWrite.flush();
        overWrite.close();
    }


    public void promptNewFolder() throws IOException {
        // Create a dialogue and get the users new song folder name
        // Close the menus
        mDrawerLayout.closeDrawer(expListViewOption);
        mDrawerLayout.closeDrawer(expListViewOption);

        // Set up the Alert Dialogue
        // This bit gives the user a prompt to create a new song
        AlertDialog.Builder newfolderprompt = new AlertDialog.Builder(this);

        newfolderprompt.setTitle(getResources().getString(R.string.options_song_newfolder));
        // Set an EditText view to get user input
        final EditText newfoldername = new EditText(this);
        newfoldername.setHint(getResources().getString(R.string.newfolder));
        newfolderprompt.setView(newfoldername);
        newfoldername.requestFocus();
        InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(newfoldername, InputMethodManager.SHOW_IMPLICIT);

        newfolderprompt.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Get the text for the new sticky note
                        final String tempnewfoldername = newfoldername.getText().toString();

                        if (tempnewfoldername.length()>0 && !tempnewfoldername.contains("/") && !tempnewfoldername.contains(".")) {

                            // Make the folder
                            File newsongdir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs/"+tempnewfoldername);
                            if (newsongdir.exists()) {
                                // Song folder exists, do nothing
                            } else {
                                // Tell the user we're creating the Songs directory
                                if (newsongdir.mkdirs()) {
                                    myToastMessage = getResources().getString(R.string.songfoldercreate)+" - "+tempnewfoldername;
                                    ListSongFiles.listSongFolders();
                                    ListSongFiles.listSongs();
                                    prepareSongMenu();
                                } else {
                                    myToastMessage = getResources().getString(R.string.no);
                                }
                                ShowToast.showToast(FullscreenActivity.this);
                            }

                            // Refresh the song folders
                            mDrawerLayout.openDrawer(expListViewSong);
                        }

                    }
                });

        newfolderprompt.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancelled.
                    }
                });

        newfolderprompt.show();

    }


    public void editFolderName() throws IOException {
        // First set the browsing directory back to the main one
        dir = new File(root.getAbsolutePath()+"/documents/OpenSong/Songs");
        currentFolder = whichSongFolder;
        newFolder = whichSongFolder;
        whichSongFolder = mainfoldername;
        ListSongFiles.listSongs();

        // Build a dialogue window and related bits that get modified/shown if needed
        AlertDialog.Builder folderdialogBuilder = new AlertDialog.Builder(FullscreenActivity.this);
        LinearLayout foldertitleLayout = new LinearLayout(FullscreenActivity.this);
        foldertitleLayout.setOrientation(LinearLayout.VERTICAL);
        TextView f_titleView = new TextView(FullscreenActivity.this);
        f_titleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        f_titleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        f_titleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white) );
        f_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        f_titleView.setText(getResources().getString(R.string.options_song_editfolder));
        foldertitleLayout.addView(f_titleView);
        folderdialogBuilder.setCustomTitle(foldertitleLayout);
        final EditText f_editbox = new EditText(FullscreenActivity.this);
        f_editbox.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        f_editbox.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        f_editbox.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white) );
        f_editbox.setBackgroundColor(FullscreenActivity.this.getResources().getColor(android.R.color.darker_gray));
        f_editbox.setHint(getResources().getString(R.string.newfoldername));
        f_editbox.requestFocus();
        folderdialogBuilder.setView(f_editbox);
        folderdialogBuilder.setSingleChoiceItems(mSongFolderNames, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (arg1==0) {
                    // Main folder, so unselect it
                    currentFolder = "";
                } else {
                    currentFolder = mSongFolderNames[arg1].toString();
                }
                f_editbox.setText(mSongFolderNames[arg1].toString());
            }
        });

        folderdialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        String newFolderTitle = f_editbox.getText().toString();
                        if (currentFolder.length()>0) {
                            // Isn't main folder, so allow rename
                            File from = new File(dir + "/" + currentFolder);
                            File to = new File(dir + "/" + newFolderTitle);
                            from.renameTo(to);

                            // Load the songs
                            ListSongFiles.listSongs();
                            prepareSongMenu();
                            mDrawerLayout.closeDrawer(expListViewOption);
                            mDrawerLayout.openDrawer(expListViewSong);
                        }
                    }
                });

        folderdialogBuilder.setNegativeButton(
                getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        // Cancelled.
                    }
                });

        folderdialogBuilder.show();

    }


    public void promptNew() throws IOException {
        // First set the browsing directory back to the main one
        dir = new File(root.getAbsolutePath()+"/documents/OpenSong/Songs");
        newFolder = whichSongFolder;
        whichSongFolder = mainfoldername;
        currentFolder = mainfoldername;
        ListSongFiles.listSongs();

        // Build a dialogue window and related bits that get modified/shown if needed
        AlertDialog.Builder newsongdialogBuilder = new AlertDialog.Builder(FullscreenActivity.this);
        LinearLayout newsongtitleLayout = new LinearLayout(FullscreenActivity.this);
        newsongtitleLayout.setOrientation(LinearLayout.VERTICAL);
        TextView s_titleView = new TextView(FullscreenActivity.this);
        s_titleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        s_titleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        s_titleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white) );
        s_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        s_titleView.setText(getResources().getString(R.string.createanewsong));
        newsongtitleLayout.addView(s_titleView);
        newsongdialogBuilder.setCustomTitle(newsongtitleLayout);
        final EditText s_editbox = new EditText(FullscreenActivity.this);
        s_editbox.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        s_editbox.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        s_editbox.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white) );
        s_editbox.setBackgroundColor(FullscreenActivity.this.getResources().getColor(android.R.color.darker_gray));
        s_editbox.setHint(getResources().getString(R.string.newsongtitleprompt));
        s_editbox.requestFocus();
        newsongdialogBuilder.setView(s_editbox);
        newsongdialogBuilder.setSingleChoiceItems(mSongFolderNames, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (arg1==0) {
                    currentFolder = mainfoldername;
                } else {
                    currentFolder = mSongFolderNames[arg1].toString();
                }
            }
        });

        newsongdialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        String newSongTitle = s_editbox.getText().toString();
                        // Create the file with the song title
                        final String basicSong = new String(
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                                        + "<song>\r\n <title>"
                                        + newSongTitle
                                        + "</title>\r\n"
                                        + "  <author></author>\r\n"
                                        + "  <copyright></copyright>\r\n"
                                        + "  <presentation></presentation>\r\n"
                                        + "  <hymn_number></hymn_number>\r\n"
                                        + "  <capo print=\"false\"></capo>\r\n"
                                        + "  <tempo></tempo>\r\n"
                                        + "  <time_sig></time_sig>\r\n"
                                        + "  <duration></duration>\r\n"
                                        + "  <ccli></ccli>\r\n"
                                        + "  <theme></theme>\r\n"
                                        + "  <alttheme></alttheme>\r\n"
                                        + "  <user1></user1>\r\n"
                                        + "  <user2></user2>\r\n"
                                        + "  <user3></user3>\r\n"
                                        + "  <key></key>\n"
                                        + "  <aka></aka>\r\n"
                                        + "  <key_line></key_line>\r\n"
                                        + "  <books></books>\r\n"
                                        + "  <midi></midi>\r\n"
                                        + "  <midi_index></midi_index>\r\n"
                                        + "  <pitch></pitch>\r\n"
                                        + "  <restrictions></restrictions>\r\n"
                                        + "  <notes></notes>\r\n"
                                        + "  <lyrics>[V1]\n.C     F G\n Verse 1 lyrics\n\n[C]\n.C      G/B    Am\n Chorus lyrics here\n\n;This is a comment line</lyrics>\r\n"
                                        + "  <linked_songs></linkedsongs>"
                                        + "  <pad_file></pad_file>"
                                        + "</song>");

                        // If song already exists - tell the user and do nothing
                        boolean isitanokfilename = true;
                        if (isitanokfilename) {
                            // Write the string to the file
                            FileOutputStream newFile;

                            if (currentFolder.equals(mainfoldername)) {
                                dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs");
                            } else {
                                dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs/" + currentFolder);
                            }

                            File checkfile = new File(dir+"/"+newSongTitle);

                            if (checkfile.exists()) {
                                // Song is already there.  Sorry!
                                myToastMessage = getResources().getText(R.string.songnamealreadytaken).toString();
                                ShowToast.showToast(FullscreenActivity.this);
                                myToastMessage = "";
                            } else {
                                try {
                                    newFile = new FileOutputStream(dir + "/" + newSongTitle, false);
                                    newFile.write(basicSong.getBytes());
                                    newFile.flush();
                                    newFile.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // Exit set mode
                                setView = "N";
                                songfilename = newSongTitle;
                                whichSongFolder = currentFolder;
                                myLyrics = "[V1]\n.C     F G\n Verse 1 lyrics\n\n[C]\n.C      G/B    Am\n Chorus lyrics here\n\n;This is a comment line";
                                redrawTheLyricsTable(null);
                                Intent editsong = new Intent(FullscreenActivity.this, EditSong.class);
                                Bundle newextras = new Bundle();
                                newextras.putString("songfilename", newSongTitle);
                                editsong.putExtras(newextras);
                                tryKillPads();
                                tryKillMetronome();
                                startActivity(editsong);
                                finish();
                            }
                        }
                    }
                });

        newsongdialogBuilder.setNegativeButton(
                getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        // Cancelled.
                    }
                });

        newsongdialogBuilder.show();
    }

    public void promptNewSet() throws IOException {
        String newSetTitle = settoload;
        // Create the file with the song title
        // Now create the song set file
        // Have to include the file path
        // If the song name includes a / it is in a sub-folder
        // If not, add a / to the start to signify no sub-folder
        // Split the string into two bits - sub-folder and
        // songname
        newSetContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "<set name=\""
                + newSetTitle
                + "\">\r\n<slide_groups>\r\n";

        for (int x = 0; x < mSetList.length; x++) {
            // Only add the lines that aren't back to options,
            // save this set, clear this set, load set, edit set, export set or blank line
            if (mSetList[x].equals(getResources().getString(R.string.options_backtooptions)) == false &&
                    mSetList[x].equals(getResources().getString(R.string.options_savethisset)) == false &&
                    mSetList[x].equals(getResources().getString(R.string.options_clearthisset)) == false &&
                    mSetList[x].equals(getResources().getString(R.string.set_edit)) == false &&
                    //mSetList[x].equals(getResources().getString(R.string.set_menutitle)) == false && 
                    //mSetList[x].equals(getResources().getString(R.string.menu_menutitle)) == false && 
                    mSetList[x].equals(getResources().getString(R.string.set_save)) == false &&
                    mSetList[x].equals(getResources().getString(R.string.set_clear)) == false &&
                    mSetList[x].equals(getResources().getString(R.string.set_export)) == false &&
                    mSetList[x].equals(getResources().getString(R.string.set_load)) == false &&
                    mSetList[x].length() > 0) {

                // Check if song is in subfolder
                int issonginsubfolder = mSetList[x]
                        .indexOf("/");
                if (issonginsubfolder >= 0) {
                    // Already has / for subfolder
                } else {
                    mSetList[x] = "/" + mSetList[x];
                }
                // Split the string into two
                String[] songparts = null;
                songparts = mSetList[x].split("/");
                // If the path isn't empty, add a forward slash
                // to the end
                if (songparts[0].length() > 0) {
                    songparts[0] = songparts[0] + "/";
                }
                if (!songparts[0].contains("Scripture") && !songparts[0].contains("Slide")) {
                    // Adding a song
                    newSetContents = newSetContents
                            + "  <slide_group name=\""
                            + songparts[1]
                            + "\" type=\"song\" presentation=\"\" path=\""
                            + songparts[0] + "\"/>\n";
                } else if (songparts[0].contains("Scripture")  & !songparts[0].contains("Slide")) {
                    // Adding a scripture
                    // Load the scripture file up
                    // Keep the songfile as a temp
                    String tempsongfilename = songfilename;
                    File tempdir = dir;
                    dir = dirbibleverses;
                    songfilename = songparts[1];
                    try {
                        LoadXML.loadXML();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (needtorefreshsongmenu) {
                        prepareSongMenu();
                        needtorefreshsongmenu = false;
                    }

                    String scripture_lyrics = mLyrics;

                    // Parse the lyrics into individual slides;
                    scripture_lyrics = scripture_lyrics.replace("[]","_SPLITHERE_");
                    scripture_lyrics = scripture_lyrics.replace("\\n "," ");
                    scripture_lyrics = scripture_lyrics.replace("\n "," ");
                    scripture_lyrics = scripture_lyrics.replace("\n"," ");
                    scripture_lyrics = scripture_lyrics.replace("\\n"," ");
                    scripture_lyrics = scripture_lyrics.replace("  "," ");
                    scripture_lyrics = scripture_lyrics.replace(". ",". ");

                    String[] mySlides = scripture_lyrics.split("_SPLITHERE_");

                    newSetContents = newSetContents
                            + "  <slide_group type=\"scripture\" name=\""
                            + songparts[1] + "|" + mAuthor
                            + "\" print=\"true\">\r\n"
                            + "  <title>" + songparts[1] + "</title>\r\n"
                            + "  <slides>\r\n";

                    for (int w=1;w<mySlides.length;w++) {
                        if (mySlides[w]!=null && mySlides[w].length()>0){
                            newSetContents = newSetContents
                                    + "  <slide>\r\n"
                                    + "  <body>"+mySlides[w].trim()+"</body>\r\n"
                                    + "  </slide>\r\n";
                        }
                    }
                    newSetContents = newSetContents + "</slides>\r\n"
                            + "  <subtitle>" + "</subtitle>\r\n"
                            + "  <notes />\r\n"
                            + "</slide_group>\r\n";
                    //Put the original songfilename back
                    songfilename = tempsongfilename;
                    dir = tempdir;
                    try {
                        LoadXML.loadXML();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (needtorefreshsongmenu) {
                        prepareSongMenu();
                        needtorefreshsongmenu = false;
                    }

                } else if (songparts[0].contains("Slide") && !songparts[0].contains("Scripture")) {
                    // Adding a custom slide
                    // Load the slide file up
                    // Keep the songfile as a temp
                    String tempsongfilename = songfilename;
                    File tempdir = dir;
                    dir = dircustomslides;
                    songfilename = songparts[1];
                    try {
                        LoadXML.loadXML();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (needtorefreshsongmenu) {
                        prepareSongMenu();
                        needtorefreshsongmenu = false;
                    }

                    String slide_lyrics = mLyrics;

                    // Parse the lyrics into individual slides;
                    slide_lyrics = slide_lyrics.replace("[]","_SPLITHERE_");
                    slide_lyrics = slide_lyrics.replace("\\n "," ");
                    slide_lyrics = slide_lyrics.replace("\n "," ");
                    slide_lyrics = slide_lyrics.replace("\n"," ");
                    slide_lyrics = slide_lyrics.replace("\\n"," ");
                    slide_lyrics = slide_lyrics.replace("  "," ");
                    slide_lyrics = slide_lyrics.replace(". ",".  ");

                    String[] mySlides = slide_lyrics.split("_SPLITHERE_");

                    newSetContents = newSetContents
                            + "  <slide_group name=\"" + songparts[1]
                            + "\" type=\"custom\" print=\"true\""
                            + " seconds=\"" + mUser1 + "\""
                            + " loop=\"" + mUser2 + "\""
                            + " transition=\"" + mUser3 + "\">\r\n"
                            + "<title>"+mTitle+"</title>\r\n"
                            + "<subtitle>" + mCopyright + "</subtitle>\r\n"
                            + "<notes>" + mKeyLine + "</notes>\r\n"
                            + "<slides>\r\n";

                    for (int w=1;w<mySlides.length;w++) {
                        if (mySlides[w]!=null && mySlides[w].length()>0){
                            newSetContents = newSetContents
                                    + "  <slide>\r\n"
                                    + "  <body>"+mySlides[w].trim()+"</body>\r\n"
                                    + "  </slide>\r\n";
                        }
                    }
                    newSetContents = newSetContents + "</slides>\r\n"
                            + "</slide_group>\r\n";
                    //Put the original songfilename back
                    songfilename = tempsongfilename;
                    dir = tempdir;
                    try {
                        LoadXML.loadXML();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (needtorefreshsongmenu) {
                        prepareSongMenu();
                        needtorefreshsongmenu = false;
                    }

                }
            }
        }
        newSetContents = newSetContents
                + "</slide_groups>\r\n</set>";

        // Write the string to the file
        FileOutputStream newFile;
        try {
            newFile = new FileOutputStream(dirsets + "/"
                    + newSetTitle, false);
            newFile.write(newSetContents.getBytes());
            newFile.flush();
            newFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void showSongMenu() {

    }

    public void showCurrentSet(View view) {
        // Use the stored mySet variable to make a new list for the Options
        // menu.
        // Set the main variable to say we are viewing a set
        // setView = "Y";
        invalidateOptionsMenu();
        // Add a delimiter between songs
        mySet = mySet.replace("_**$$**_", "_**$%%%$**_");
        // Break the saved set up into a new String[]
        mSet = mySet.split("%%%");
        mSetList = mSet;
        // Restore the set back to what it was
        mySet = mySet.replace("_**$%%%$**_", "_**$$**_");

        // Purge the set so it only shows songfilenames
        setSize = mSetList.length;
        invalidateOptionsMenu();
        for (int x = 0; x < mSetList.length; x++) {
            mSetList[x] = mSetList[x].replace("$**_", "");
            mSetList[x] = mSetList[x].replace("_**$", "");
        }

        // Open the Optionlist drawer up again so we can see the set
        mDrawerLayout.openDrawer(expListViewOption);
        expListViewOption.expandGroup(0);

    }

    public void redrawTheLyricsTable(View view) {
        isPDF = false;
        File checkfile = new File(dir+"/"+songfilename);
        if ((songfilename.contains(".pdf") || songfilename.contains(".PDF")) && checkfile.exists()) {
            // File is pdf
            isPDF = true;
            isSong = false;
        } else {
            isPDF = false;
            isSong = true;
        }
        wasshowing_pdfselectpage = View.GONE;
        wasshowing_stickynotes = View.INVISIBLE;

        // Show the ActionBar
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        getActionBar().show();
        if (hideactionbaronoff.equals("Y")) {
            delayactionBarHide.postDelayed(hideActionBarRunnable, 1000); // 1000ms delay
        }
        //toggleActionBar();
        needtoredraw = true;
        alreadyloaded = false;
        mScreenOrientation = getResources().getConfiguration().orientation;
        columnTest = 1;

        // Set up and down arrows to invisible
        uparrow.setVisibility(View.INVISIBLE);
        downarrow.setVisibility(View.INVISIBLE);
        songLoadingProgressBar.setVisibility(View.VISIBLE);
        songTitleHolder.setVisibility(View.VISIBLE);

        // Make sure the window is scrolled to the top for the new song
        scrollpage.smoothScrollTo(0, 0);

        songTitleHolder.setText(songfilename);
        songTitleHolder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein));
        songLoadingProgressBar.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein));

        // Get the song index numbers
        ListSongFiles.listSongs();
        ListSongFiles.getCurrentSongIndex();

        if (whichDirection.equals("L2R")) {
            if (scrollpage_pdf.getVisibility() == View.VISIBLE) {
                scrollpage_pdf.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_right));
            } else if (scrollpage_onecol.getVisibility() == View.VISIBLE) {
                scrollpage_onecol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_right));
            } else if (scrollpage_twocol.getVisibility() == View.VISIBLE) {
                scrollpage_twocol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_right));
            } else if (scrollpage_threecol.getVisibility() == View.VISIBLE) {
                scrollpage_threecol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_right));
            }
        } else {
            if (scrollpage_pdf.getVisibility() == View.VISIBLE) {
                scrollpage_pdf.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_left));
            } else if (scrollpage_onecol.getVisibility() == View.VISIBLE) {
                scrollpage_onecol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_left));
            } else if (scrollpage_twocol.getVisibility() == View.VISIBLE) {
                scrollpage_twocol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_left));
            } else if (scrollpage_threecol.getVisibility() == View.VISIBLE) {
                scrollpage_threecol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_left));
            }
        }
        // Wait 500ms before clearing the table and loading the new lyrics
        Handler delayfadeinredraw = new Handler();
        delayfadeinredraw.postDelayed(new Runnable() {
            @Override
            public void run() {

                uparrow.setVisibility(View.INVISIBLE);
                downarrow.setVisibility(View.INVISIBLE);

                myXML = null;
                myXML = "";

                try {
                    showLyrics(main_page);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 500); // 500ms delay

        doScaling = false;


        // Set a runnable to check the scroll position after 1 second
        delaycheckscroll.postDelayed(checkScrollPosition, 1000); // 1000ms delay


        // Try to open the appropriate Song folder on the left menu
        for (int z=0;z<listDataHeaderSong.size()-1;z++) {
            if (listDataHeaderSong.get(z).equals(whichSongFolder)) {
                expListViewSong.expandGroup(z);
            }
        }

        if (mySticky.getVisibility() == View.VISIBLE) {
            scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_top));
            stickynotes.setAlpha(0.3f);
            // After 500ms, make them invisible
            Handler delayhidenote = new Handler();
            delayhidenote.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mySticky.setVisibility(View.INVISIBLE);
                    scrollstickyholder.setVisibility(View.GONE);
                }
            }, 500); // 500ms delay
        }

    }

    public void searchYouTube() {


        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query="+mTitle+"+"+mAuthor)));
    }

    public void doMoveInSet() {
        invalidateOptionsMenu();
        linkclicked = mSetList[indexSongInSet];
        pdfPageCurrent = 0;
        if (linkclicked.indexOf("/") >= 0) {
            // Ok so it does!
        } else {
            // Right it doesn't, so add the /
            linkclicked = "/" + linkclicked;
        }

        // Now split the linkclicked into two song parts 0=folder 1=file
        String[] songpart = linkclicked.split("/");

        // If the folder length isn't 0, it is a folder
        if (songpart[0].length() > 0 && !songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
            whichSongFolder = songpart[0];
            FullscreenActivity.dir = new File(
                    FullscreenActivity.root.getAbsolutePath()
                            + "/documents/OpenSong/Songs/"
                            + songpart[0]);

        } else if (songpart[0].length() > 0 && songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
            whichSongFolder = "../OpenSong Scripture/_cache";
            songpart[0] = "../OpenSong Scripture/_cache";
            //FullscreenActivity.dir = FullscreenActivity.dirbibleverses;

        } else if (songpart[0].length() > 0 && songpart[0].contains("Slide") && !songpart[0].contains("Scripture")) {
            whichSongFolder = "../Slides/_cache";
            songpart[0] = "../Slides/_cache";
            //FullscreenActivity.dir = FullscreenActivity.dircustomslides;

        } else {
            whichSongFolder = mainfoldername;
            FullscreenActivity.dir = new File(
                    FullscreenActivity.root.getAbsolutePath()
                            + "/documents/OpenSong/Songs");
        }

        // Save the preferences
        Preferences.savePreferences();

        // Match the song folder
        ListSongFiles.listSongs();
        showSongMenu();
        // Actually - changed my mind, close it immediately
        mDrawerLayout.closeDrawer(expListViewSong);
        // Redraw the Lyrics View
        songfilename = null;
        songfilename = "";
        songfilename = songpart[1];
        redrawTheLyricsTable(view);
        return;

    }

    public void listSavedSets(View view) {
        // update the optionList to show the sets stored in the sets folder
        SetActions.updateOptionListSets(FullscreenActivity.this, view);
        Arrays.sort(mySetsFiles);
        Arrays.sort(mySetsDirectories);
        Arrays.sort(mySetsFileNames);
        Arrays.sort(mySetsFolderNames);

        // Open the Optionlist drawer up again so we can see the set
        mDrawerLayout.openDrawer(expListViewOption);

    }

    public void checkChordFormat() {
        transposedLyrics = null;
        transposedLyrics = "";
        myTransposedLyrics = null;
        myTransposedLyrics = myLyrics.split("\n");

        oldchordformat = chordFormat;
        //boolean warn=false;

        boolean contains_es_is = false;
        boolean contains_H = false;
        boolean contains_do = false;

        // Check if the user is using the same chord format as the song
        // Go through the chord lines and look for clues
        for (int x = 0; x < numrowstowrite; x++) {
            if (myTransposedLyrics[x].indexOf(".")==0) {
                // Chord line
                if (myTransposedLyrics[x].contains("es") || myTransposedLyrics[x].contains("is") ||
                        myTransposedLyrics[x].contains(" a") || myTransposedLyrics[x].contains(".a") ||
                        myTransposedLyrics[x].contains(" b") || myTransposedLyrics[x].contains(".b") ||
                        myTransposedLyrics[x].contains(" h") || myTransposedLyrics[x].contains(".h") ||
                        myTransposedLyrics[x].contains(" c") || myTransposedLyrics[x].contains(".c") ||
                        myTransposedLyrics[x].contains(" d") || myTransposedLyrics[x].contains(".d") ||
                        myTransposedLyrics[x].contains(" e") || myTransposedLyrics[x].contains(".e") ||
                        myTransposedLyrics[x].contains(" f") || myTransposedLyrics[x].contains(".f") ||
                        myTransposedLyrics[x].contains(" g") || myTransposedLyrics[x].contains(".g"))	{
                    contains_es_is = true;
                } else if (myTransposedLyrics[x].contains("H")) {
                    contains_H = true;
                } else if (myTransposedLyrics[x].contains("Do") || myTransposedLyrics[x].contains("Re") ||
                        myTransposedLyrics[x].contains("Me") || myTransposedLyrics[x].contains("Fa") ||
                        myTransposedLyrics[x].contains("Sol") || myTransposedLyrics[x].contains("La") ||
                        myTransposedLyrics[x].contains("Si")) {
                    contains_do = true;
                }
            }
        }

        int detected = 0;
        // Set the chord style detected
        if (contains_do) {
            oldchordformat="4";
            detected = 3;
        } else if (contains_H && !contains_es_is) {
            oldchordformat="2";
            detected = 1;
        } else if (contains_H || contains_es_is) {
            oldchordformat="3";
            detected = 2;
        } else {
            oldchordformat="1";
            detected = 0;
        }

        // Ok so the user chord format may not quite match the song - it might though!
        // Prompt the user to confirm and list his current chord format

        //Prepare oldchordformat options list and newchordformat options lists
        final String[] old_format = new String [4];
        old_format[0] = getResources().getText(R.string.chordFormat1).toString();
        old_format[1] = getResources().getText(R.string.chordFormat2).toString();
        old_format[2] = getResources().getText(R.string.chordFormat3).toString();
        old_format[3] = getResources().getText(R.string.chordFormat4).toString();


        // If the user has specified to check chord format each time, display the dialogue below
        // If not, just go straight to the transpose function

        if (alwaysPreferredChordFormat.equals("N") || chord_converting.equals("Y")) {

            String alwayscheck = getResources().getString(R.string.docontinue).toString() + "\n" + getResources().getString(R.string.chordformat_check_short).toString();

            String alwaysdefault = getResources().getString(R.string.docontinue).toString() + "\n" + getResources().getString(R.string.chordformat_default_short).toString();

            AlertDialog.Builder dotranspose = new AlertDialog.Builder(this);
            dotranspose.setTitle(getResources().getString(R.string.oldchordformat))
                    .setSingleChoiceItems(old_format, detected, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            int detected = arg1+1;
                            oldchordformat = ""+detected;
                        }
                    });

            if (chord_converting!="Y") {
                dotranspose.setPositiveButton(alwaysdefault,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                alwaysPreferredChordFormat = "Y";
                                chord_converting = "N";
                                Preferences.savePreferences();
                                try {
                                    Transpose.doTranspose();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                redrawTheLyricsTable(main_page);

                            }
                        });
            }


            dotranspose.setNeutralButton(alwayscheck,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            chord_converting = "N";
                            try {
                                Transpose.doTranspose();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            redrawTheLyricsTable(main_page);

                        }
                    });

            dotranspose.setNegativeButton(
                    getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            // Cancelled.
                        }
                    });

            dotranspose.show();

        } else {
            // Just transpose.
            // Must be using default chord format
            oldchordformat = chordFormat;
            try {
                Transpose.doTranspose();
            } catch (IOException e) {
                e.printStackTrace();
            }
            redrawTheLyricsTable(main_page);


        }

    }

    public void onSongImport() {
        // Give an alert box that asks the user to specify the backup file (must be in OpenSong/folder)
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FullscreenActivity.this);
        LinearLayout titleLayout = new LinearLayout(FullscreenActivity.this);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        TextView m_titleView = new TextView(FullscreenActivity.this);
        TextView m_subtitleView = new TextView(FullscreenActivity.this);
        m_titleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        m_subtitleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        m_titleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        m_subtitleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Medium);
        m_titleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white) );
        m_subtitleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white) );
        m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        m_subtitleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        m_titleView.setText(getResources().getString(R.string.import_onsong_choose));
        m_subtitleView.setText(getResources().getString(R.string.onsonglocation));
        titleLayout.addView(m_titleView);
        titleLayout.addView(m_subtitleView);
        dialogBuilder.setCustomTitle(titleLayout);
        //dialogBuilder.setMessage(getResources().getString(R.string.onsonglocation));
        // List files ending with .backup in homedir
        ArrayList<String> backups = new ArrayList<String>();
        File[] backupfilecheck = homedir.listFiles();

        for (int i=0; i<backupfilecheck.length; i++) {
            if (backupfilecheck[i].isFile() && backupfilecheck[i].getPath().endsWith(".backup")) {
                backups.add(backupfilecheck[i].getName());
            }
        }
		/*		 for (File f : backupfilecheck) {
			 if (f.isFile() && f.getPath().endsWith(".backup")) {
				 backups.add(f.getName());
			 }
		 }
		 */		 if (backups.size()>0) {
            backUpFiles = new String[backups.size()];
            for (int r=0;r<backups.size();r++) {
                backUpFiles[r] = backups.get(r).toString();
            }
        }
        dialogBuilder.setSingleChoiceItems(backUpFiles, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                backupchosen = backUpFiles[arg1];
            }
        });

        dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,
                                int which) {
            }
        });

        dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,	int which) {
                // Load the backup as an async task
                if (!backupchosen.isEmpty()) {
                    // Let the user know something is happening
                    onsongImportProgressBar.setVisibility(View.VISIBLE);

                    // Do the stuff async to stop the app slowing down
                    ImportOnSongBackup task2 = new ImportOnSongBackup();
                    task2.execute();
                } else {
                    myToastMessage = getResources().getString(R.string.import_onsong_error);
                    ShowToast.showToast(FullscreenActivity.this);
                }
            }
        });

        dialogBuilder.show();
    }

    private class ImportOnSongBackup extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            if (!dironsong.exists()) {
                // No OnSong folder exists - make it
                dironsong.mkdirs();
            }
            InputStream is;
            ZipInputStream zis;
            try {
                String filename;
                is = new FileInputStream(homedir + "/" + backupchosen);
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;
                byte[] buffer = new byte[1024];
                int count;

                while ((ze = zis.getNextEntry()) != null) {
                    filename = ze.getName();

                    if (filename.equals("OnSong.Backup.sqlite3")) {
                        FileOutputStream fout = new FileOutputStream(homedir +"/" + filename);

                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                        fout.close();
                        zis.closeEntry();
                    }
                }
                zis.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return backupchosen;
            }

            File dbfile = new File(homedir + "/OnSong.Backup.sqlite3" );
            db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
            int numRows = (int) DatabaseUtils.queryNumEntries(db, "Song");
            // Go through each row and read in the content field
            // Save the files with the .onsong extension

            String query = "SELECT * FROM Song";

            //Cursor points to a location in your results
            Cursor cursor = db.rawQuery(query, null);

            // Move to first row
            cursor.moveToFirst();

            String str_title = "";
            String str_content = "";

            while (cursor.moveToNext()) {
                // Extract data.
                str_title = cursor.getString(cursor.getColumnIndex("title"));
                // Make sure title doesn't have /
                str_title = str_title.replace("/","_");
                str_content = cursor.getString(cursor.getColumnIndex("content"));

                try {
                    // Now write the modified song
                    FileOutputStream overWrite = new FileOutputStream(dironsong + "/" + str_title + ".onsong",false);
                    overWrite.write(str_content.getBytes());
                    overWrite.flush();
                    overWrite.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "doneit";
        }
        @Override
        protected void onPostExecute( String doneit) {
            onsongImportProgressBar.setVisibility(View.INVISIBLE);
            myToastMessage = getResources().getString(R.string.import_onsong_done);
            ShowToast.showToast(FullscreenActivity.this);
            prepareSongMenu();
        }
    }





    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mScreenOrientation != newConfig.orientation) {
            orientationchanged = true;
        } else {
            orientationchanged = false;
        }
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
        mDrawerLayout.closeDrawer(expListViewSong);
        mDrawerLayout.closeDrawer(expListViewOption);
        if (orientationchanged ) {
            redrawTheLyricsTable(main_page);
        }
    }


    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        if (!getActionBar().isShowing()) {
            //getActionBar().show();
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        if (getActionBar().isShowing()) {
            //getActionBar().hide();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        actionbarbutton = true;
        // call ActionBarDrawerToggle.onOptionsItemSelected(), if it returns
        // true
        // then it has handled the app icon touch event

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
            return true;

        } else {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
            switch (item.getItemId()) {

                case R.id.present_mode:
                    // Switch to presentation mode
                    if (dualDisplayCapable.equals("Y")) {
                        whichMode = "Presentation";
                        Preferences.savePreferences();
                        Intent presentmode = new Intent();
                        presentmode.setClass(FullscreenActivity.this,
                                PresentMode.class);
                        tryKillPads();
                        tryKillMetronome();
                        startActivity(presentmode);
                        finish();
                    } else {
                        myToastMessage = getResources().getText(
                                R.string.notcompatible).toString();
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;

                case R.id.action_search:
                    if (mDrawerLayout.isDrawerOpen(expListViewSong)) {
                        mDrawerLayout.closeDrawer(expListViewSong);
                        return true;
                    } else {
                        mDrawerLayout.openDrawer(expListViewSong);
                        return true;
                    }
                case R.id.action_fullsearch:
                    Intent intent = new Intent();
                    intent.setClass(FullscreenActivity.this,
                            SearchViewFilterMode.class);
                    tryKillPads();
                    tryKillMetronome();
                    startActivity(intent);
                    finish();
                    return true;

                case R.id.youtube_websearch:
                    // Open a youtube search for the current song
                    searchYouTube();
                    return true;

                case R.id.chordie_websearch:
                    // Prompt the user for a string to search for

                    AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setTitle(getResources().getString(R.string.chordiesearch));
                    alert.setMessage(getResources()
                            .getString(R.string.phrasesearch));
                    // Set an EditText view to get user input
                    final EditText inputsearch = new EditText(this);
                    alert.setView(inputsearch);

                    alert.setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    Intent chordiesearch = new Intent();
                                    chordiesearch.setClass(FullscreenActivity.this,
                                            Chordie.class);
                                    chordiesearch.putExtra("thissearch",
                                            inputsearch.getText().toString());
                                    chordiesearch.putExtra("engine", "chordie");
                                    tryKillPads();
                                    tryKillMetronome();
                                    startActivity(chordiesearch);
                                    finish();
                                }
                            });

                    alert.setNegativeButton(
                            getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    // Cancelled.
                                }
                            });

                    alert.show();
                    return true;

                case R.id.ultimateguitar_websearch:
                    // Prompt the user for a string to search for

                    AlertDialog.Builder alert_ug = new AlertDialog.Builder(this);

                    alert_ug.setTitle(getResources().getString(
                            R.string.ultimateguitarsearch));
                    alert_ug.setMessage(getResources().getString(
                            R.string.phrasesearch));
                    // Set an EditText view to get user input
                    final EditText inputsearch2 = new EditText(this);
                    alert_ug.setView(inputsearch2);

                    alert_ug.setPositiveButton(getResources()
                                    .getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    Intent ultimateguitarsearch = new Intent();
                                    ultimateguitarsearch.setClass(
                                            FullscreenActivity.this, Chordie.class);
                                    ultimateguitarsearch.putExtra("thissearch",
                                            inputsearch2.getText().toString());
                                    ultimateguitarsearch.putExtra("engine",
                                            "ultimate-guitar");
                                    tryKillPads();
                                    tryKillMetronome();
                                    startActivity(ultimateguitarsearch);
                                    finish();
                                }
                            });

                    alert_ug.setNegativeButton(
                            getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    // Cancelled.
                                }
                            });

                    alert_ug.show();
                    return true;

                case R.id.action_settings:
                    if (mDrawerLayout.isDrawerOpen(expListViewOption)) {
                        mDrawerLayout.closeDrawer(expListViewOption);
                        return true;
                    } else {
                        mDrawerLayout.openDrawer(expListViewOption);
                        return true;
                    }

                case R.id.set_add:
                    if (whichSongFolder.equals(mainfoldername)) {
                        whatsongforsetwork = "$**_" + songfilename + "_**$";
                    } else {
                        whatsongforsetwork = "$**_" + whichSongFolder + "/"
                                + songfilename + "_**$";
                    }
                    // Allow the song to be added, even if it is already there
                    mySet = mySet + whatsongforsetwork;
                    // Tell the user that the song has been added.
                    myToastMessage = "\"" + songfilename + "\" "
                            + getResources().getString(R.string.addedtoset);
                    ShowToast.showToast(FullscreenActivity.this);
                    // Vibrate to indicate something has happened
                    Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vb.vibrate(25);

                    SetActions.prepareSetList();
                    invalidateOptionsMenu();
                    prepareOptionMenu();

                    // Save the set and other preferences
                    Preferences.savePreferences();

                    // Show the current set
                    showCurrentSet(view);

                    fixSetActionButtons(menu);

                    // Hide the menus - 1 second after opening the Option menu,
                    // close it (1000ms total)
                    Handler optionMenuFlickClosed = new Handler();
                    optionMenuFlickClosed.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.closeDrawer(expListViewOption);
                        }
                    }, 1000); // 1000ms delay
                    return true;

                case R.id.pad_menu_button:
                    // Run the pad start/stop, but only if song isn't a pdf!
                    if (!isPDF) {
                        try {
                            togglePlayPads(padButton);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;


                case R.id.autoscroll_menu_button:
                    // Run the autoscroll start/stop, but only if song isn't a pdf!
                    if (!isPDF) {
                        autoScroll(autoscrollButton);
                    } else {
                        myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;

                case R.id.stickynotes_menu_button:
                    // Hide/show the sticky notes, but only if song isn't a pdf!
                    if (!isPDF) {
                        stickyNotes(stickynotes);
                    } else {
                        myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;

                case R.id.chords_menu_button:
                    // Hide/show the chords, but only if song isn't a pdf!
                    if (!isPDF) {
                        popupChords_toggle(chordButton);
                    } else {
                        myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                            ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;

                case R.id.pageselect_menu_button:
                    // Hide/show the page select, but only if song is a pdf!
                    if (isPDF) {
                        pdfSelectPage(pdf_selectpage);
                    } else {
                        myToastMessage = getResources().getString(R.string.song_functionnotavailable);
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;


                case R.id.set_back:
                    if (!tempswipeSet.equals("disable")) {
                        tempswipeSet = "disable";
                        // reset the tempswipeset after 1sec
                        Handler delayfadeinredraw = new Handler();
                        delayfadeinredraw.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tempswipeSet = "enable";
                            }
                        }, 1800); // 1800ms delay

                        // Set the swipe direction to right to left
                        whichDirection = "L2R";
                        if (isPDF && pdfPageCurrent>0) {
                            pdfPageCurrent = pdfPageCurrent - 1;
                            redrawTheLyricsTable(main_page);
                            return false;
                        } else {
                            pdfPageCurrent = 0;
                        }
                        indexSongInSet -= 1;
                        doMoveInSet();

                    }
                    return true;


                case R.id.set_forward:
                    if (!tempswipeSet.equals("disable")) {
                        tempswipeSet = "disable";
                        // reset the tempswipeset after 1sec
                        Handler delayfadeinredraw = new Handler();
                        delayfadeinredraw.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tempswipeSet = "enable";
                            }
                        }, 1800); // 1800ms delay

                        // Set the swipe direction to right to left
                        whichDirection = "R2L";
                        if (isPDF && pdfPageCurrent<(pdfPageCount-1)) {
                            pdfPageCurrent = pdfPageCurrent + 1;
                            redrawTheLyricsTable(main_page);
                            return false;
                        } else {
                            pdfPageCurrent = 0;
                        }
                        indexSongInSet += 1;
                        doMoveInSet();

                    }
                    return true;

            }
            return super.onOptionsItemSelected(item);
        }

    }

    private class SwipeDetector extends SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Decide what the double tap action is
            // 1 = both menus
            // 2 = edit song
            // 3 = add to set
            // 4 = redraw
            // 5 = off
            if (gesture_doubletap.equals("1")
                    && !padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow.isFocused() && !uparrow.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility()!=View.VISIBLE && popupAutoscroll.getVisibility()!=View.VISIBLE
                    && popupMetronome.getVisibility()!=View.VISIBLE && scrollstickyholder.getVisibility()!=View.VISIBLE
                    && popupChord.getVisibility()!=View.VISIBLE) {
                gesture1();
            } else if (gesture_doubletap.equals("2")
                    && !padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow.isFocused() && !uparrow.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility()!=View.VISIBLE && popupAutoscroll.getVisibility()!=View.VISIBLE
                    && popupMetronome.getVisibility()!=View.VISIBLE && scrollstickyholder.getVisibility()!=View.VISIBLE
                    && popupChord.getVisibility()!=View.VISIBLE) {
                if (isPDF) {
                    // Can't do this action on a pdf!
                    myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                    ShowToast.showToast(FullscreenActivity.this);
                } else {
                    openEditSong();
                }

            } else if (gesture_doubletap.equals("3")
                    && !padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow.isFocused() && !uparrow.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility()!=View.VISIBLE && popupAutoscroll.getVisibility()!=View.VISIBLE
                    && popupMetronome.getVisibility()!=View.VISIBLE && scrollstickyholder.getVisibility()!=View.VISIBLE
                    && popupChord.getVisibility()!=View.VISIBLE) {
                gesture3();
            } else if (gesture_doubletap.equals("4")
                    && !padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow.isFocused() && !uparrow.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility()!=View.VISIBLE && popupAutoscroll.getVisibility()!=View.VISIBLE
                    && popupMetronome.getVisibility()!=View.VISIBLE && scrollstickyholder.getVisibility()!=View.VISIBLE
                    && popupChord.getVisibility()!=View.VISIBLE) {
                gesture4();
            } else {
                // Gesture is off!
                // Do nothing
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Decide what the long press action is
            // 1 = both menus
            // 2 = edit song
            // 3 = add to set
            // 4 = redraw song
            if (gesture_longpress.equals("1")
                    && !padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow.isFocused() && !uparrow.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility()!=View.VISIBLE && popupAutoscroll.getVisibility()!=View.VISIBLE
                    && popupMetronome.getVisibility()!=View.VISIBLE && scrollstickyholder.getVisibility()!=View.VISIBLE
                    && popupChord.getVisibility()!=View.VISIBLE) {
                gesture1();
            } else if (gesture_longpress.equals("2")
                    && !padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow.isFocused() && !uparrow.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility()!=View.VISIBLE && popupAutoscroll.getVisibility()!=View.VISIBLE
                    && popupMetronome.getVisibility()!=View.VISIBLE && scrollstickyholder.getVisibility()!=View.VISIBLE
                    && popupChord.getVisibility()!=View.VISIBLE) {
                if (isPDF) {
                    // Can't do this action on a pdf!
                    myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                    ShowToast.showToast(FullscreenActivity.this);
                } else {
                    openEditSong();
                }
            } else if (gesture_longpress.equals("3")
                    && !padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow.isFocused() && !uparrow.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility()!=View.VISIBLE && popupAutoscroll.getVisibility()!=View.VISIBLE
                    && popupMetronome.getVisibility()!=View.VISIBLE && scrollstickyholder.getVisibility()!=View.VISIBLE
                    && popupChord.getVisibility()!=View.VISIBLE) {
                gesture3();
            } else if (gesture_longpress.equals("4")
                    && !padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow.isFocused() && !uparrow.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility()!=View.VISIBLE && popupAutoscroll.getVisibility()!=View.VISIBLE
                    && popupMetronome.getVisibility()!=View.VISIBLE && scrollstickyholder.getVisibility()!=View.VISIBLE
                    && popupChord.getVisibility()!=View.VISIBLE) {
                gesture4();
            } else {// Gesture is off
                // Do nothing
            }
            super.onLongPress(e);;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            wasscrolling = true;
            return true;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {

            // Check movement along the Y-axis. If it exceeds
            // SWIPE_MAX_OFF_PATH, then dismiss the swipe.
            int screenwidth = findViewById(R.id.main_page).getWidth();
            int leftmargin = 40;
            int rightmargin = screenwidth - 40;
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }


            if (tempswipeSet.equals("disable")) {
                return false; // Currently disabled swiping to let screen finish
                // drawing.
            }

            // Swipe from right to left.
            // The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE)
            // and a certain velocity (SWIPE_THRESHOLD_VELOCITY).
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && e1.getX() < rightmargin
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
                    && (swipeSet.equals("Y") || swipeSet.equals("S"))) {
                // Set the direction as right to left
                whichDirection = "R2L";
                // If we are viewing a set, move to the next song.
                //main_page.requestFocus();

                //scrollpage.requestFocus();
                // Get height of screen

                if (isPDF && pdfPageCurrent<(pdfPageCount-1)) {
                    pdfPageCurrent = pdfPageCurrent + 1;
                    redrawTheLyricsTable(main_page);
                    return false;
                } else {
                    //pdfPageCurrent = 0;
                }

                if (setSize > 1 && setView.equals("Y") && indexSongInSet >= 0
                        && indexSongInSet < (setSize - 1)) {
                    // temporarily disable swipe
                    tempswipeSet = "disable";
                    indexSongInSet += 1;
                    doMoveInSet();
                    // Set a runnable to reset swipe back to original value
                    // after 1 second
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tempswipeSet = "enable";
                        }
                    }, 1800); // 1800ms delay

                }
                // If not in a set, see if we can move to the next song in the
                // list
                // Only if swipeSet is Y (not S)
                if (setView.equals("N") && swipeSet.equals("Y")) {
                    if (songfilename.equals(mSongFileNames[nextSongIndex])) {
                        // Already on the last song, do nothing
                    } else {
                        // Move to the next song
                        // temporarily disable swipe
                        tempswipeSet = "disable";

                        songfilename = mSongFileNames[nextSongIndex];
                        redrawTheLyricsTable(view);

                        // Set a runnable to reset swipe back to original value
                        // after 1 second
                        Handler delayfadeinredraw = new Handler();
                        delayfadeinredraw.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tempswipeSet = "enable";
                            }
                        }, 1800); // 1800ms delay
                    }
                }
                return true;
            }

            // Swipe from left to right.
            // The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE)
            // and a certain velocity (SWIPE_THRESHOLD_VELOCITY).
            if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && e1.getX() > leftmargin
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
                    && (swipeSet.equals("Y") || swipeSet.equals("S"))) {
                // Set the direction as left to right
                whichDirection = "L2R";

                if (isPDF && pdfPageCurrent>0) {
                    pdfPageCurrent = pdfPageCurrent - 1;
                    redrawTheLyricsTable(main_page);
                    return false;
                } else {
                    pdfPageCurrent = 0;
                }

                //main_page.requestFocus();
                // If we are viewing a set, move to the previous song.
                if (setSize > 2 && setView.equals("Y") && indexSongInSet >= 1) {
                    // temporarily disable swipe
                    tempswipeSet = "disable";

                    indexSongInSet -= 1;
                    doMoveInSet();
                    // Set a runnable to reset swipe back to original value
                    // after 1 second
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tempswipeSet = "enable";
                        }
                    }, 1800); // 1800ms delay
                }
                // If not in a set, see if we can move to the previous song in
                // the list
                // Only if swipeSet is Y (not S)
                if (setView.equals("N") && swipeSet.equals("Y")) {
                    if (songfilename.equals(mSongFileNames[previousSongIndex])) {
                        // Already on the first song, do nothing
                    } else {
                        // temporarily disable swipe
                        tempswipeSet = "disable";

                        // Move to the previous song
                        songfilename = mSongFileNames[previousSongIndex];
                        redrawTheLyricsTable(view);

                        // Set a runnable to reset swipe back to original value
                        // after 1 second
                        Handler delayfadeinredraw = new Handler();
                        delayfadeinredraw.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tempswipeSet = "enable";
                            }
                        }, 1800); // 1800ms delay
                    }
                }
                return true;
            }

            return false;
        }
    }

    public void onScrollChanged() {
        // Hide the actionbar because we're just scrolling

        //View mainpage = findViewById(R.id.main_page);
        //mainpage.requestFocus();
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        if (getActionBar().isShowing()) {
            //getActionBar().hide();		
        }
        // Set a runnable to check the scroll position
        delaycheckscroll.post(checkScrollPosition);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    public class simpleOnScaleGestureListener extends
            SimpleOnScaleGestureListener {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        // WOULD BE BETTER IF THIS WAS CALLED ON SOME KIND OF ONSCROLL LISTENER
        scaleGestureDetector.onTouchEvent(ev);
        switch (action) {

            case (MotionEvent.ACTION_MOVE):
                //View mainpage = findViewById(R.id.main_page);
                //mainpage.requestFocus();

                // Set a runnable to check the scroll position
                delaycheckscroll.post(checkScrollPosition);

                // Set a runnable to check the scroll position after 1 second
                delaycheckscroll.postDelayed(checkScrollPosition, 1000); // 1000ms delay
        }

        // TouchEvent dispatcher.
        if (gestureDetector != null) {
            if (gestureDetector.onTouchEvent(ev)) {
                // If the gestureDetector handles the event, a swipe has been
                // executed and no more needs to be done.
            } else {
                if (action == MotionEvent.ACTION_UP && !scrollbutton) {
                    toggleActionBar();

                } else {
                    delayactionBarHide.removeCallbacks(hideActionBarRunnable);
                }
                wasscrolling = false;
                scrollbutton = false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}

