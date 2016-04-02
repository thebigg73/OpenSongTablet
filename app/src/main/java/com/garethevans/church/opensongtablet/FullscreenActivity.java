package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("deprecation")
@SuppressLint({"DefaultLocale", "RtlHardcoded", "InflateParams", "SdCardPath"})
public class FullscreenActivity extends AppCompatActivity implements PopUpListSetsFragment.MyInterface,
        PopUpAreYouSureFragment.MyInterface,
        PopUpTransposeFragment.MyInterface, PopUpEditSongFragment.MyInterface,
        PopUpSongDetailsFragment.MyInterface, PopUpSongRenameFragment.MyInterface,
        PopUpSongCreateFragment.MyInterface, PopUpFontsFragment.MyInterface,
        PopUpEditStickyFragment.MyInterface, PopUpCustomSlideFragment.MyInterface,
        PopUpSetViewNew.MyInterface, PopUpImportExternalFile.MyInterface,
        PopUpFileChooseFragment.MyInterface, PopUpDirectoryChooserFragment.MyInterface {

    //First up, declare all of the variables needed by this application

    public static boolean sortAlphabetically = true;

    // Playback progress
    LinearLayout playbackProgress;
    TextView currentTime_TextView;
    TextView totalTime_TextView;
    long time_start;
    long end_time;

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
    public TextView songandauthor;

    // Immersive mode stuff
    private Handler mRestoreImmersiveModeHandler = new Handler();
    private Runnable restoreImmersiveModeRunnable = new Runnable() {
        public void run() {
            Log.d("d","restoreImmersiveModeRunnable");
            restoreTransparentBars();
        }
    };

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
    public static String imagetext = "";
    public static String text_slide = "";
    public static String text_scripture = "";
    public static String text_note = "";
    public int slideout_time = 500;
    public int checkscroll_time = 1000;
    public int delayswipe_time = 1800;
    public static int crossFadeTime = 8000;
    public static String toggleScrollArrows = "";

    public static boolean converting = false;

    @SuppressWarnings("unused")
    // Identify the chord images
    private Drawable f1;
    private Drawable f2;
    private Drawable f3;
    private Drawable f4;
    private Drawable f5;
    private Drawable f6;
    private Drawable f7;
    private Drawable f8;
    private Drawable f9;
    private Drawable lx;
    private Drawable l0;
    private Drawable l1;
    private Drawable l2;
    private Drawable l3;
    private Drawable l4;
    private Drawable l5;
    private Drawable mx;
    private Drawable m0;
    private Drawable m1;
    private Drawable m2;
    private Drawable m3;
    private Drawable m4;
    private Drawable m5;
    private Drawable rx;
    private Drawable r0;
    private Drawable r1;
    private Drawable r2;
    private Drawable r3;
    private Drawable r4;
    private Drawable r5;

    public static Typeface typeface0;
    public static Typeface typeface1;
    public static Typeface typeface2;
    public static Typeface typeface3;
    public static Typeface typeface4;
    public static Typeface typeface5;
    public static Typeface typeface4i;
    public static Typeface typeface5i;
    public static Typeface typeface6;
    public static Typeface typeface7;
    public static Typeface typeface8;
    public static Typeface typeface9;
    public static Typeface typeface7i;
    public static Typeface typeface8i;
    public static Typeface typeface9i;

    public static ArrayList<String> exportsetfilenames = new ArrayList<>();
    public static ArrayList<String> exportsetfilenames_ost = new ArrayList<>();
    public static String lastSetName = "";
    private TableLayout chordimageshere;
    public static String chordInstrument = "g";
    public static String showNextInSet = "top";
    private Typeface lyrics_useThisFont;
    private static String allchords = "";
    private static String allchordscapo = "";
    public static String chordnotes = "";
    public static String capoDisplay = "";
    public static String languageToLoad = "";
    private static String tempLanguage = "";
    private String[] backUpFiles;
    private String backupchosen = "";

    public static int currentapiVersion;

    public static String mediaStore = "";

    public static String emailtext = "";

    public static int maxvolrange;

    public ScrollView pagebuttons;
    public ImageView setlisticon;

    public static String whattodo = "";
    private ScrollView popupChord;
    private ScrollView popupPad;
    private static String popupPad_stoporstart = "stop";
    private Spinner popupPad_key;
    private Spinner popupPad_file;
    private SeekBar popupPad_volume;
    private TextView popupPad_volume_text;
    private SeekBar popupPad_pan;
    private TextView popupPad_pan_text;
    private Button popupPad_startstopbutton;

    private ScrollView popupAutoscroll;
    private static String popupAutoscroll_stoporstart = "stop";
    private SeekBar popupAutoscroll_delay;
    private TextView popupAutoscroll_delay_text;
    private TextView popupAutoscroll_duration;
    private Button popupAutoscroll_startstopbutton;
    private static int newPos;
    private static int scrollpageHeight;
    public static boolean autostartautoscroll;
    public boolean autoscrollactivated = false;

    public DialogFragment newFragment;

    ScrollView popupMetronome;
    public static int total_calc_bpm;
    int total_counts = 0;
    public static int av_bpm;
    public static long new_time = 0;
    public static long time_passed = 0;
    public static long old_time = 0;
    public static int calc_bpm;
    public static String popupMetronome_stoporstart = "stop";
    Spinner popupMetronome_timesig;
    public SeekBar popupMetronome_tempo;
    TextView popupMetronome_tempo_text;
    public SeekBar popupMetronome_volume;
    Button increaseTempo_Button;
    Button decreaseTempo_Button;
    TextView popupMetronome_volume_text;
    SeekBar popupMetronome_pan;
    TextView popupMetronome_pan_text;
    Button popupMetronome_startstopbutton;
    private static int beatoffcolour = 0xf232333;
    private String whichbeat = "a";
    public static boolean visualmetronome = false;
    private ToggleButton popupMetronome_visualmetronometoggle;

    @SuppressWarnings("unused")
    public boolean fadeout1 = false;
    @SuppressWarnings("unused")
    public boolean fadeout2 = false;
    private final short minBpm = 40;
    private final short maxBpm = 199;
    private short bpm = 100;
    public static short noteValue = 4;
    public static short beats = 4;
    public int currentBeat = 1;
    @SuppressWarnings("unused")
    private float metrovol;
    @SuppressWarnings("unused")
    public short initialVolume;
    public double beatSound = 1200;
    public double sound = 1600;
    private AudioManager audio;
    private MetronomeAsyncTask metroTask;
    private Handler mHandler;

    // P2P/WiFi
    private ServerSocket mServerSocket;
    private int mLocalPort;
    private Object mNsdManager;

    @SuppressLint("HandlerLeak")
    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (visualmetronome) {
                    if (whichbeat.equals("a")) {
                        whichbeat = "b";
                        if (ab != null) {
                            ab.setBackgroundDrawable(new ColorDrawable(beatoffcolour));
                        }
                    } else {
                        whichbeat = "a";
                        if (ab != null) {
                            ab.setBackgroundDrawable(new ColorDrawable(metronomeColor));
                        }
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
    public static int timesigindex;
    public static boolean mTimeSigValid = false;
    public static int temposlider;
    boolean usingdefaults = false;
    private final int autoscroll_pause_time = 500; // specified in ms
    public static int default_autoscroll_songlength;
    public static int default_autoscroll_predelay;
    public static String autoscroll_default_or_prompt = "";
    private boolean pauseautoscroll = true;
    private boolean autoscrollispaused = false;
    private static boolean isautoscrolling = false;
    private static float autoscroll_pixels = 0.0f;
    private static float newPosFloat = 0.0f;
    private static int total_pixels_to_scroll = 0;
    private static String autoscrollonoff = "false";
    public static String pad_filename = "null";
    private static boolean killfadeout1 = false;
    private static boolean killfadeout2 = false;
    private static boolean isfading1 = false;
    private static boolean isfading2 = false;
    private static boolean padson = false;
    public static boolean needtorefreshsongmenu = false;
    private static MediaPlayer mPlayer1 = null;
    private static MediaPlayer mPlayer2 = null;
    private static ImageView padButton;
    private static ImageView linkButton;
    private static ImageView chordButton;
    private static ImageView autoscrollButton;
    private static ImageView metronomeButton;
    private static boolean orientationchanged = false;
    private static int wasshowing_pdfselectpage;
    private static int wasshowing_stickynotes;
    private static int alreadyshowingpage;
    public static int keyindex;

    public static int pdfPageCurrent = 0;
    private static int pdfPageCount = 0;
    public static boolean isPDF = false;
    private static boolean isSong = false;
    private static ImageView pdf_selectpage;
    private static ImageView stickynotes;
    private static TextView mySticky;
    public static String toggleAutoSticky = "";
    private static ScrollView scrollstickyholder;
    public static String hideactionbaronoff = "";
    private int lastExpandedGroupPositionOption;
    private int lastExpandedGroupPositionSong;
    public static String[][] childSongs;
    public static String setnamechosen = "";
    private static boolean addingtoset = false;
    private static boolean removingfromset = false;
    private static int fontsizeseekar;
    private static int pageseekbarpos;

    private static Handler delayautoscroll;

    // Swipe
    private static final int SWIPE_MIN_DISTANCE = 250;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 800;
    private GestureDetector gestureDetector;

    public static String whichMode = "";

    // Views and bits on the pages
    private static int mScreenOrientation;
    private static int columnTest = 1;
    private static float onecolfontsize;
    private static float twocolfontsize;
    private static float threecolfontsize;
    private ScrollView scrollpage;
    private ScrollView scrollpage_pdf;
    private ScrollView scrollpage_onecol;
    private ScrollView scrollpage_twocol;
    private ScrollView scrollpage_threecol;
    private static View view;
    public static int maxcharsinline;
    private static ActionBarDrawerToggle actionBarDrawerToggle;
    private static Handler delayactionBarHide;
    private static Handler delaycheckscroll;
    private static Handler doautoScroll;
    private static Handler doProgressTime;

    private static boolean scrollbutton = false;
    private static boolean actionbarbutton = false;

    // Font sizes (relative)
    private boolean alreadyloaded = false;
    private final float mainfontsize = 16;
    private static float sectionfontsize = 10;
    private static float commentfontsize = 10;
    public static int linespacing;
    private float tempfontsize;
    private float tempsectionsize;
    private boolean doScaling = false;
    private float scaleX;
    private float scaleY;
    private float pageWidth;
    private float pageHeight;
    private boolean needtoredraw = true;
    private boolean doanimate = false;

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
    public static int dark_metronome;

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
    public static int light_metronome;

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
    public static int custom1_metronome;

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
    public static int custom2_metronome;

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
    public static int chords_useThisBGColor;
    public static int capo_useThisBGColor;
    public static int lyrics_useThisBGColor;
    public static float lyrics_useThisTextSize;
    public static float chords_useThisTextSize;
    public static int temp_useThisBGColor;
    public static float commentfontscalesize;
    public static float headingfontscalesize;

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
    private static boolean wasscrolling = false;
    public static String alwaysPreferredChordFormat = "";
    public static String gesture_doubletap = "";
    public static String gesture_longpress = "";
    public static String bibleFile = "";
    public static boolean bibleLoaded = false;
    public static String bibleFileContents = "";
    public static String chordFormat = "";
    private static String chord_converting = "N";
    public static String oldchordformat = "";
    public static String presenterChords = "";
    public static String swipeDrawer = "";
    public static String swipeSet = "";
    private static String tempswipeSet = "enable";
    private static String whichDirection = "R2L";
    public static int indexSongInSet;
    public static String previousSongInSet = "";
    public static String nextSongInSet = "";
    public static String mTheme = "";
    public static String mDisplayTheme = "Theme.Holo";
    public static String setView = "N";
    private MenuItem presentationMode;
    public static int setSize;
    public static boolean showingSetsToLoad = false;
    public static String whatsongforsetwork = "";
    public static String mySet = "";
    public static String newSetContents = "";
    public static String[] mSet;
    public static String[] mSetList;
    public static ArrayList<String> mTempSetList;
    public static boolean doneshuffle = false;

    // Song filenames, folders, set filenames, folders
    public static String currentFolder = "";
    public static String newFolder = "";
    private static int next_song;
    private static int prev_song;
    public static String whichSongFolder = "";
    public static String[] mySetsFileNames;
    public static File[] mySetsFiles;
    public static String[] mySetsFolderNames;
    public static File[] mySetsDirectories;
    public static File filechosen;
    public static File file;
    public static File setfile;
    public static String settoload = "";
    public static String settodelete = "";
    public static String[] mSongFileNames;
    public static String[] mSongFolderNames;

    public static ArrayList<String> allfilesforsearch = new ArrayList<>();
    public static ArrayList<String> allfilesforsearch_folder = new ArrayList<>();
    public static ArrayList<String> allfilesforsearch_song = new ArrayList<>();
    public static ArrayList<String> search_database = new ArrayList<>();
    public static boolean safetosearch = false;

    public static int currentSongIndex;
    public static int previousSongIndex;
    public static int nextSongIndex;

    // Presentation mode variables
    public static boolean presoAutoScale;
    public static boolean presoShowChords;
    public static int presoFontSize;
    public static int presoTitleSize;
    public static int presoAuthorSize;
    public static int presoCopyrightSize;
    public static int presoAlertSize;

    public static float presoAlpha;
    public static String myAlert = "";
    private static String dualDisplayCapable = "N";
    public static String backgroundImage1 = "";
    public static String backgroundImage2 = "";
    public static String backgroundVideo1 = "";
    public static String backgroundVideo2 = "";
    public static String backgroundToUse = "";
    public static String backgroundTypeToUse = "";
    public static int xmargin_presentation;
    public static int ymargin_presentation;
    public static boolean usePresentationOrder = false;

    // Song xml data
    public static ArrayList<String> foundSongSections_heading = new ArrayList<>();
    public static ArrayList<String> foundSongSections_content = new ArrayList<>();

    public static CharSequence mTitle = "";
    public static CharSequence mAuthor = "Gareth Evans";
    private static String mTempAuthor = "";
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
    private static String tempChords = "";
    public static String mLinkedSongs = "";
    public static String mExtraStuff1 = "";
    public static String mExtraStuff2 = "";
    public static String mPadFile = "";
    public static String mCustomChords = "";
    public static String mLinkYouTube = "";
    public static String mLinkWeb = "";
    public static String mLinkAudio = "";
    public static String mLinkOther = "";

    public static String capokey = null;

    // Info for the lyrics table
    private static float mScaleFactor = 1.0f;
    private ScaleGestureDetector scaleGestureDetector;
    public static boolean botherwithcolumns;
    public static int splitpoint;
    public static int thirdsplitpoint;
    public static int twothirdsplitpoint;
    public static String[] whatisthisblock;
    public static String[] whatisthisline;
    public static String mStorage = "";
    public static String myLyrics = "";
    public static float mFontSize;
    public static int mMaxFontSize;
    public static String toggleYScale = "";
    public static String mySetXML = "";
    public static String[] myParsedSet;
    public static String myXML = "";
    public static String mynewXML = "";
    public static String[] myParsedLyrics;
    public static String[] myTransposedLyrics;
    public static String songfilename = "";
    private DrawerLayout mDrawerLayout;
    private Menu menu;
    public static String linkclicked = "";
    private static int myOptionListClickedItem;
    public static SharedPreferences myPreferences;
    public static int numrowstowrite;
    public static String transposeDirection = "0";
    public static int transposeTimes = 1;
    public static String transposeStyle = "sharps";
    public static String transposedLyrics = "";
    public static String showChords = "";
    private TableLayout lyricstable_onecolview;
    private TableLayout lyricstable_twocolview;
    private TableLayout lyricstable2_twocolview;
    private TableLayout lyricstable_threecolview;
    private TableLayout lyricstable2_threecolview;
    private TableLayout lyricstable3_threecolview;
    private static ImageView uparrow_top;
    private static ImageView downarrow_top;
    private static ImageView uparrow_bottom;
    private static ImageView downarrow_bottom;
    public static String myToastMessage = "";
    private static boolean showCapo;

    // The following get in translation texts
    private static String edit_song_presentation = "";
    private static String error_notset = "";
    public static String error_missingsection = "";
    public static String set_loading = "";
    public static String set_processing = "";

    public static String tag_verse = "";
    public static String tag_chorus = "";
    public static String tag_prechorus = "";
    public static String tag_bridge = "";
    public static String tag_tag = "";
    private static String set = "";
    public static String song = "";
    public static String slide = "";
    public static String note = "";
    public static String scripture = "";
    public static String incoming_text = "";
    public static String scripture_title = "";
    public static String scripture_verse = "";
    public static String image = "";
    private static String toastmessage_maxfont = "";
    private static String toastmessage_minfont = "";
    public static String set_menutitle = "";
    public static String backtooptions = "";
    public static String savethisset = "";
    public static String clearthisset = "";
    public static String set_edit = "";
    public static String set_save = "";
    public static String set_load = "";
    public static String set_clear = "";
    public static String set_export = "";
    public static String menu_menutitle = "";
    public static String sethasbeendeleted = "";
    public static String deleteerror_start = "";
    private static String deleteerror_end_song = "";
    public static String deleteerror_end_sets = "";
    public static String songdoesntexist = "";
    public static String exportcurrentsong = "";
    private static String importnewsong = "";
    public static String exportsavedset = "";
    private static String importnewset = "";
    public static String mainfoldername = "";
    public static int mylyricsfontnum;
    public static int mychordsfontnum;
    public static int mypresofontnum;
    public static Typeface lyricsfont;
    public static Typeface commentfont;
    public static Typeface chordsfont;
    public static Typeface presofont;

    private static View main_page;
    private static View songLoadingProgressBar;
    private static View onsongImportProgressBar;
    private static TextView songTitleHolder;

    private static Runnable hideActionBarRunnable;
    private static Runnable checkScrollPosition;
    private static Runnable autoScrollRunnable;
    private static Runnable progressTimeRunnable;
    public static boolean keepgoing = false;

    private List<String> listDataHeaderOption;
    private HashMap<String, List<String>> listDataChildOption;
    private ExpandableListAdapter listAdapterSong;
    private ExpandableListAdapterOptions listAdapterOption;
    private ExpandableListView expListViewSong;
    private ExpandableListView expListViewOption;
    private List<String> listDataHeaderSong;
    private HashMap<String, List<String>> listDataChildSong;
    private FadeOutMusic1 mtask_fadeout_music1;
    private FadeOutMusic2 mtask_fadeout_music2;
    private AutoScrollMusic mtask_autoscroll_music;
    private ProgressTime mtask_updatetimeprogress;
    private static int whichtofadeout;

    // Try to determine internal or external storage
    private String secStorage = System.getenv("SECONDARY_STORAGE");
    private final String defStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final String[] secStorageOptions = {"/mnt/emmc/",
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

    private boolean extStorageExists = false;
    private boolean defStorageExists = false;

    private static File extStorCheck;
    private static File intStorCheck;

    private boolean storageIsValid = true;

    public static String pagebutton_position = "right";

    // P2P Wifi

    public static File root = Environment.getExternalStorageDirectory();
    public static File homedir = new File(root.getAbsolutePath() + "/documents/OpenSong");
    public static File dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs");
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

    public static Locale locale;

    public static String[][][] bibleVerse; // bibleVerse[book][chapter#][verse#]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load up the user preferences
        myPreferences = getPreferences(MODE_PRIVATE);
        Preferences.loadPreferences();

        mainfoldername = getResources().getString(R.string.mainfoldername);

        // If whichSongFolder is empty, reset to main
        if (whichSongFolder == null || whichSongFolder.isEmpty() || whichSongFolder.equals("")) {
            whichSongFolder = mainfoldername;
            Preferences.savePreferences();
        }
        locale = Locale.getDefault();

        // Try language locale change
        if (!languageToLoad.isEmpty()) {
            locale = null;
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
        set_loading = getResources().getString(R.string.set_loading);
        set_processing = getResources().getString(R.string.set_processing);
        set = getResources().getString(R.string.options_set);
        song = getResources().getString(R.string.options_song);
        slide = getResources().getString(R.string.slide);
        scripture = getResources().getString(R.string.scripture);
        note = getResources().getString(R.string.note);
        image = getResources().getString(R.string.image);

        tag_verse = getResources().getString(R.string.tag_verse);
        tag_chorus = getResources().getString(R.string.tag_chorus);
        tag_prechorus = getResources().getString(R.string.tag_prechorus);
        tag_bridge = getResources().getString(R.string.tag_bridge);
        tag_tag = getResources().getString(R.string.tag_tag);

        edit_song_presentation = getResources().getString(R.string.edit_song_presentation);
        error_notset = getResources().getString(R.string.error_notset);
        error_missingsection = getResources().getString(R.string.error_missingsection);

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

        text_slide = slide;
        text_scripture = scripture;
        text_note = note;

        // If we have opened the app by an intent (clicking on an ost or osts file)
        // Get the popup
        boolean needtoimport = false;

        try {
            incomingfile = getIntent();
            if (incomingfile != null) {
                file_location = incomingfile.getData().getPath();
                file_name = incomingfile.getData().getLastPathSegment();
                file_uri = incomingfile.getData();
                needtoimport = true;
            }
        } catch (Exception e) {
            // No file
            e.printStackTrace();
            needtoimport = false;
        }

        // Initialise api
        currentapiVersion = Build.VERSION.SDK_INT;

        if (currentapiVersion >= 17) {
            // Capable of dual head presentations
            dualDisplayCapable = "Y";
        }

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

        // Set up the user preferences for page colours and fonts
        SetUpColours.colours();
        SetTypeFace.setTypeface();

        // If the user hasn't set the preferred storage location
        // Or the default storage location isn't available, ask them!
        checkDirectories();

        // If whichMode is Presentation, open that app instead
        if (whichMode.equals("Presentation") && dualDisplayCapable.equals("Y") && !needtoimport) {
            Intent performmode = new Intent();
            performmode.setClass(FullscreenActivity.this, PresenterMode.class);
            startActivity(performmode);
            finish();
        }

        // Load the songs
        ListSongFiles.listSongs();

        // Get the song indexes
        ListSongFiles.getCurrentSongIndex();

        // Load the page
        setContentView(R.layout.activity_fullscreen_table);

        if (needtoimport) {
            newFragment = PopUpImportExternalFile.newInstance();
            newFragment.show(getFragmentManager(), "dialog");
        } else {
            Log.d("d", "No incoming file - continue normally");
        }

        // Set up the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        songandauthor = (TextView) findViewById(R.id.songandauthor);
        ab = getSupportActionBar();
        beatoffcolour = (int) getResources().getColor(R.color.toolbar);

        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        songandauthor.setText("OpenSong\\nGareth Evans");

        // Set up runnable used to hide the actionbar
        delayactionBarHide = new Handler();
        delaycheckscroll = new Handler();
        doautoScroll = new Handler();

        hideActionBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (ab != null && ab.isShowing()) {
                    ab.hide();
                }
            }
        };

        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                scrollpage.smoothScrollTo(0, (int) newPosFloat);
            }
        };

        // The task to update the progress time
        // Identify the playback progress view and hide it for now
        playbackProgress = (LinearLayout) findViewById(R.id.playbackProgress);
        currentTime_TextView = (TextView) findViewById(R.id.currentTime_TextView);
        totalTime_TextView = (TextView) findViewById(R.id.totalTime_TextView);
        playbackProgress.setVisibility(View.INVISIBLE);

        doProgressTime = new Handler();
        progressTimeRunnable = new Runnable() {
            @Override
            public void run() {
                time_passed = System.currentTimeMillis();
                int currtimesecs = (int) ((time_passed - time_start)/1000);
                String text = timeFormatFixer(currtimesecs);
                if (currtimesecs<autoScrollDelay) {
                    currentTime_TextView.setTextColor(0xffff0000);
                    currentTime_TextView.setText(text);
                } else {
                    currentTime_TextView.setTextColor(0xffffffff);
                    currentTime_TextView.setText(text);
                }
            }
        };

        // If key is set
        String keytext = "";
        if (!mKey.isEmpty() && !mKey.equals("")) {
            keytext = " (" + mKey + ")";
        }

        songandauthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newFragment = PopUpSongDetailsFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
            }
        });
        String text = songfilename + keytext + "\n" + mAuthor;
        songandauthor.setText(text);
        main_page = findViewById(R.id.main_page);
        // Set a listener for the main_page.
        // If a popup is open, clicking on the main page will hide it.
        songTitleHolder = (TextView) findViewById(R.id.songTitleHolder);
        songLoadingProgressBar = findViewById(R.id.songLoadingProgressBar);
        onsongImportProgressBar = findViewById(R.id.mainProgressBar);
        scrollpage_pdf = (ScrollView) findViewById(R.id.scrollpage_pdf);

        // get the expandablelistview
        expListViewOption = (ExpandableListView) findViewById(R.id.option_list_ex);
        expListViewSong = (ExpandableListView) findViewById(R.id.song_list_ex);

        // Set up the navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        prepareSongMenu();
        prepareOptionMenu();

        // Decide if the user want the page buttons on the right or the bottom
        setupPageButtons();


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
        f1 = getResources().getDrawable(R.drawable.chord_f1);
        f2 = getResources().getDrawable(R.drawable.chord_f2);
        f3 = getResources().getDrawable(R.drawable.chord_f3);
        f4 = getResources().getDrawable(R.drawable.chord_f4);
        f5 = getResources().getDrawable(R.drawable.chord_f5);
        f6 = getResources().getDrawable(R.drawable.chord_f6);
        f7 = getResources().getDrawable(R.drawable.chord_f7);
        f8 = getResources().getDrawable(R.drawable.chord_f8);
        f9 = getResources().getDrawable(R.drawable.chord_f9);
        lx = getResources().getDrawable(R.drawable.chord_l_x);
        l0 = getResources().getDrawable(R.drawable.chord_l_0);
        l1 = getResources().getDrawable(R.drawable.chord_l_1);
        l2 = getResources().getDrawable(R.drawable.chord_l_2);
        l3 = getResources().getDrawable(R.drawable.chord_l_3);
        l4 = getResources().getDrawable(R.drawable.chord_l_4);
        l5 = getResources().getDrawable(R.drawable.chord_l_5);
        mx = getResources().getDrawable(R.drawable.chord_m_x);
        m0 = getResources().getDrawable(R.drawable.chord_m_0);
        m1 = getResources().getDrawable(R.drawable.chord_m_1);
        m2 = getResources().getDrawable(R.drawable.chord_m_2);
        m3 = getResources().getDrawable(R.drawable.chord_m_3);
        m4 = getResources().getDrawable(R.drawable.chord_m_4);
        m5 = getResources().getDrawable(R.drawable.chord_m_5);
        rx = getResources().getDrawable(R.drawable.chord_r_x);
        r0 = getResources().getDrawable(R.drawable.chord_r_0);
        r1 = getResources().getDrawable(R.drawable.chord_r_1);
        r2 = getResources().getDrawable(R.drawable.chord_r_2);
        r3 = getResources().getDrawable(R.drawable.chord_r_3);
        r4 = getResources().getDrawable(R.drawable.chord_r_4);
        r5 = getResources().getDrawable(R.drawable.chord_r_5);

        chordimageshere = (TableLayout) findViewById(R.id.chordimageshere);
        popupChord = (ScrollView) findViewById(R.id.popupchords);
        Spinner popupChord_Instrument = (Spinner) findViewById(R.id.popupchord_instrument);
        String[] instrument_choice = new String[6];
        instrument_choice[0] = getResources().getString(R.string.guitar);
        instrument_choice[1] = getResources().getString(R.string.ukulele);
        instrument_choice[2] = getResources().getString(R.string.mandolin);
        instrument_choice[3] = getResources().getString(R.string.cavaquinho);
        instrument_choice[4] = getResources().getString(R.string.banjo4);
        instrument_choice[5] = getResources().getString(R.string.banjo5);
        ArrayAdapter<String> adapter_instrument = new ArrayAdapter<>(this, R.layout.my_spinner, instrument_choice);
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
        popupPad_file = (Spinner) findViewById(R.id.popuppad_file);
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(getResources().getString(R.string.pad_auto));
        padfiles.add(getResources().getString(R.string.link_audio));
        padfiles.add(getResources().getString(R.string.off));
        ArrayAdapter<String> adapter_file = new ArrayAdapter<>(this, R.layout.my_spinner, padfiles);
        adapter_file.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupPad_file.setAdapter(adapter_file);
        popupPad_file.setOnItemSelectedListener(new popupPad_fileListener());
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
        popupAutoscroll_delay_text = (TextView) findViewById(R.id.popupautoscroll_delay_text);
        popupAutoscroll_duration = (TextView) findViewById(R.id.popupautoscroll_duration);
        popupAutoscroll_duration.addTextChangedListener(new textWatcher());
        popupAutoscroll_startstopbutton = (Button) findViewById(R.id.popupautoscroll_startstopbutton);

        popupMetronome = (ScrollView) findViewById(R.id.popupmetronome);
        popupMetronome_tempo = (SeekBar) findViewById(R.id.popupmetronome_tempo);
        popupMetronome_tempo.setOnSeekBarChangeListener(new popupMetronome_tempoListener());
        popupMetronome_tempo_text = (TextView) findViewById(R.id.popupmetronome_tempo_text);
        increaseTempo_Button = (Button) findViewById(R.id.increaseTempo_Button);
        decreaseTempo_Button = (Button) findViewById(R.id.decreaseTempo_Button);
        increaseTempo_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseTempo();
            }
        });
        decreaseTempo_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseTempo();
            }
        });
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
        //stickynotes.setVisibility(View.INVISIBLE);
        mySticky.setVisibility(View.INVISIBLE);
        //padButton.setVisibility(View.INVISIBLE);
        //linkButton.setVisibility(View.INVISIBLE);
        //chordButton.setVisibility(View.INVISIBLE);
        //metronomeButton.setVisibility(View.INVISIBLE);
        //autoscrollButton.setVisibility(View.INVISIBLE);
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

        uparrow_top = (ImageView) findViewById(R.id.uparrow_top);
        uparrow_bottom = (ImageView) findViewById(R.id.uparrow_bottom);
        downarrow_top = (ImageView) findViewById(R.id.downarrow_top);
        downarrow_bottom = (ImageView) findViewById(R.id.downarrow_bottom);

        checkScrollPosition = new Runnable() {
            @Override
            public void run() {
                int height = scrollpage.getChildAt(0).getMeasuredHeight() - scrollpage.getHeight();

                // Decide if the down arrow should be displayed.
                if (height > scrollpage.getScrollY() && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerOpen(expListViewSong)) {
                    if (toggleScrollArrows.equals("D")) {
                        downarrow_top.setVisibility(View.VISIBLE);
                    } else {
                        downarrow_top.setVisibility(View.GONE);
                    }
                    downarrow_bottom.setVisibility(View.VISIBLE);
                } else {
                    if (toggleScrollArrows.equals("D")) {
                        downarrow_top.setVisibility(View.INVISIBLE);
                    } else {
                        downarrow_top.setVisibility(View.GONE);
                    }
                    downarrow_bottom.setVisibility(View.INVISIBLE);
                }
                // Decide if the up arrow should be displayed.
                if (scrollpage.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerOpen(expListViewSong)) {
                    uparrow_top.setVisibility(View.VISIBLE);
                    if (toggleScrollArrows.equals("D")) {
                        uparrow_bottom.setVisibility(View.VISIBLE);
                    } else {
                        uparrow_bottom.setVisibility(View.GONE);
                    }
                } else {
                    uparrow_top.setVisibility(View.INVISIBLE);
                    if (toggleScrollArrows.equals("D")) {
                        uparrow_bottom.setVisibility(View.INVISIBLE);
                    } else {
                        uparrow_bottom.setVisibility(View.GONE);
                    }
                }
            }
        };

        // What happens when the navigation drawers are opened
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            // Called when a drawer has settled in a completely closed state.
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                toggleActionBar();
                setupPageButtons();
                showpagebuttons();
            }

            // Called when a drawer has settled in a completely open state.
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                wasscrolling = false;
                scrollbutton = false;
                setupPageButtons();
                toggleActionBar();
                hidepagebuttons();
                hidepopupPad();
                hidepopupChord();
                hidepopupAutoscroll();
                hidepopupMetronome();
                hidepopupSticky();

                if (!ab.isShowing()) {
                    ab.show();
                }
            }
        };

        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        if (swipeDrawer.equals("N")) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }

        mDrawerLayout.setScrimColor(0x33000000);

        // Before redrawing the lyrics table, split the song into sections
        redrawTheLyricsTable(main_page);

        fixSetActionButtons();

        main_page = findViewById(R.id.main_page);
        mDrawerLayout.closeDrawer(expListViewOption);
        mDrawerLayout.closeDrawer(expListViewSong);

        // As the app opens, swipe in and out the drawer menus so that people know they are there
        // Do it with a delay
        Handler delayopensongdrawer = new Handler();
        Handler delayopenoptiondrawer = new Handler();
        Handler delayclosesongdrawer = new Handler();
        Handler delaycloseoptiondrawer = new Handler();
        Handler delaypageicons = new Handler();
        delayopensongdrawer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.openDrawer(expListViewSong);
            }
        }, 800);
        delayopenoptiondrawer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.openDrawer(expListViewOption);
            }
        }, 1600);
        delayclosesongdrawer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(expListViewSong);
            }
        }, 1600);
        delaycloseoptiondrawer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(expListViewOption);
            }
        }, 2400);

        tempfontsize = mainfontsize;
        twocolfontsize = mainfontsize;
        threecolfontsize = mainfontsize;
        tempsectionsize = sectionfontsize;

        setWindowFlags();
        setWindowFlagsAdvanced();
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                Log.d("d","setOnSystemUiVisibilityChangeListener_1361");
                restoreTransparentBars();
            }
        });


        decorView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                restoreTransparentBars();
            }
        });


        scaleGestureDetector = new ScaleGestureDetector(this, new simpleOnScaleGestureListener());

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        short volume = (short) metronomevol;
        metroTask = new MetronomeAsyncTask();
        Runtime.getRuntime().gc();

        resizeDrawers();
        SharedPreferences indexSongPreferences = getSharedPreferences("indexsongs",MODE_PRIVATE);
        boolean buildSearchIndex = indexSongPreferences.getBoolean("buildSearchIndex",true);
        Log.d("d","buildSearchIndex="+buildSearchIndex);
        // Start to index all the songs
        if (buildSearchIndex) {
            try {
                IndexSongs.ListAllSongs();
                Log.d("d","number of songs found="+allfilesforsearch.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AsyncTask indexmysongs;
        indexmysongs = new AsyncTask<Object,Void,String>() {
            @Override
            protected String doInBackground(Object[] params) {
                String val;
                try {
                    indexMySongs();
                    val = "ok";

                } catch (Exception e) {
                    e.printStackTrace();
                    val = "error";
                }
                return val;
            }

            @Override
            public void onPreExecute() {
                myToastMessage = getString(R.string.search_index_start);
                ShowToast.showToast(FullscreenActivity.this);
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals("error")) {
                    myToastMessage = getString(R.string.search_index_error)+"\n"+getString(R.string.search_log);
                    ShowToast.showToast(getApplicationContext());
                    FullscreenActivity.safetosearch = true;
                    SharedPreferences indexSongPreferences = getSharedPreferences("indexsongs",MODE_PRIVATE);
                    SharedPreferences.Editor editor_index = indexSongPreferences.edit();
                    editor_index.putBoolean("buildSearchIndex", true);
                    editor_index.apply();
                } else {
                    myToastMessage = getString(R.string.search_index_end);
                    ShowToast.showToast(FullscreenActivity.this);
                }
            }
        };

        if (buildSearchIndex) {
            SharedPreferences.Editor editor_index = indexSongPreferences.edit();
            editor_index.putBoolean("buildSearchIndex", false);
            editor_index.apply();
            indexmysongs.execute();
        }

        // WiFi Direct / P2P

    }

    public String timeFormatFixer(int secstime) {
        double mins_float = (float)secstime/60.0;
        int mins = (int) mins_float;
        int secs = secstime - (mins*60);
        String time;
        if (secs<10) {
            time = mins + ":0" + secs;
        } else {
            time = mins + ":" + secs;
        }
        return time;
    }
/*
    public void initializeServerSocket() {
        mServerSocket = new ServerSocket(0);
        mLocalPort = mServerSocket.getLocalPort();
    }

    public void registerService() {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("OpenSongApp");
        serviceInfo.setServiceType("_ost.tcp.");
        serviceInfo.setPort(port);

        mNsdManager = Context.getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD,mRegistrationListener);
    }
*/

    public void fixActionButtons() {

    }

    public void setupPageButtons() {
        // Decide if we are using buttons at the bottom of the page or the right
        if (pagebutton_position.equals("right")) {
            // Make the right bar visible
            pagebuttons = (ScrollView) findViewById(R.id.rightbar);
            pagebuttons.setVisibility(View.VISIBLE);

            // Hide the default setlist button
            setlisticon = (ImageView) findViewById(R.id.setbutton);
            setlisticon.setVisibility(View.GONE);

            // Assign the buttons
            padButton = (ImageView) findViewById(R.id.padbutton_right);
            autoscrollButton = (ImageView) findViewById(R.id.autoscroll_right);
            metronomeButton = (ImageView) findViewById(R.id.metronomebutton_right);
            stickynotes = (ImageView) findViewById(R.id.stickynotes_right);
            pdf_selectpage = (ImageView) findViewById(R.id.pageselect_right);
            chordButton = (ImageView) findViewById(R.id.chordbutton_right);
            linkButton = (ImageView) findViewById(R.id.linkbutton_right);

            //Show the bottombar for scroll down
            findViewById(R.id.bottombar).setVisibility(View.VISIBLE);

            // Hide the bottom icons
            findViewById(R.id.padbutton).setVisibility(View.INVISIBLE);
            findViewById(R.id.autoscroll).setVisibility(View.INVISIBLE);
            findViewById(R.id.metronomebutton).setVisibility(View.INVISIBLE);
            findViewById(R.id.stickynotes).setVisibility(View.INVISIBLE);
            findViewById(R.id.pageselect).setVisibility(View.INVISIBLE);
            findViewById(R.id.chordbutton).setVisibility(View.INVISIBLE);
            findViewById(R.id.linkbutton).setVisibility(View.INVISIBLE);

            // Decide if the icons should be shown (pdf or song)
            showpagebuttons();

        } else if (pagebutton_position.equals("bottom")){
            // Make the right bar invisible
            pagebuttons = (ScrollView) findViewById(R.id.rightbar);
            pagebuttons.setVisibility(View.GONE);

            // Make sure the bottom bar is visible
            findViewById(R.id.bottombar).setVisibility(View.VISIBLE);

            // Enable the default setlist button
            setlisticon = (ImageView) findViewById(R.id.setbutton);
            setlisticon.setVisibility(View.VISIBLE);

            // Assign the buttons
            padButton = (ImageView) findViewById(R.id.padbutton);
            autoscrollButton = (ImageView) findViewById(R.id.autoscroll);
            metronomeButton = (ImageView) findViewById(R.id.metronomebutton);
            pdf_selectpage = (ImageView) findViewById(R.id.pageselect);
            stickynotes = (ImageView) findViewById(R.id.stickynotes);
            chordButton = (ImageView) findViewById(R.id.chordbutton);
            linkButton = (ImageView) findViewById(R.id.linkbutton);

            // Make the buttons visible
            padButton.setVisibility(View.VISIBLE);
            autoscrollButton.setVisibility(View.VISIBLE);
            metronomeButton.setVisibility(View.VISIBLE);
            pdf_selectpage.setVisibility(View.VISIBLE);
            stickynotes.setVisibility(View.VISIBLE);
            chordButton.setVisibility(View.VISIBLE);
            linkButton.setVisibility(View.VISIBLE);

            // Decide if the icons should be shown (pdf or song)
            showpagebuttons();

        } else {
            // All page buttons should be hidden
            pagebuttons = (ScrollView) findViewById(R.id.rightbar);
            pagebuttons.setVisibility(View.GONE);
            setlisticon = (ImageView) findViewById(R.id.setbutton);
            setlisticon.setVisibility(View.GONE);
            findViewById(R.id.padbutton).setVisibility(View.GONE);
            findViewById(R.id.autoscroll).setVisibility(View.GONE);
            findViewById(R.id.metronomebutton).setVisibility(View.GONE);
            findViewById(R.id.stickynotes).setVisibility(View.GONE);
            findViewById(R.id.pageselect).setVisibility(View.GONE);
            findViewById(R.id.chordbutton).setVisibility(View.GONE);
            findViewById(R.id.linkbutton).setVisibility(View.GONE);
        }
    }

    public void increaseTempo() {
        // Get the current tempo
        int currtempo;
        try {
            currtempo = popupMetronome_tempo.getProgress() + 39;
        } catch (Exception e) {
            currtempo = 39;
        }
        if (currtempo<199) {
            // Add 1 then subtract 39;
            currtempo -= 38;
            popupMetronome_tempo.setProgress(currtempo);
        }
    }

    public void decreaseTempo() {
        // Get the current tempo
        int currtempo;
        try {
            currtempo = popupMetronome_tempo.getProgress() + 39;
        } catch (Exception e) {
            currtempo = 39;
        }
        if (currtempo>40) {
            // Subtract 1 then subtract 39;
            currtempo -= 40;
            popupMetronome_tempo.setProgress(currtempo);
        }
    }

    public void resizeDrawers() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels/2;
        DrawerLayout.LayoutParams paramsSong = (android.support.v4.widget.DrawerLayout.LayoutParams) expListViewSong.getLayoutParams();
        DrawerLayout.LayoutParams paramsOption = (android.support.v4.widget.DrawerLayout.LayoutParams) expListViewOption.getLayoutParams();
        paramsSong.width = width;
        paramsOption.width = width;
        expListViewSong.setLayoutParams(paramsSong);
        expListViewOption.setLayoutParams(paramsOption);
    }

    public void restoreTranslucentBarsDelayed() {
        // we restore it now and after 500 ms!
        Log.d("d", "restoreTranslucentBarsDelayed");
        restoreTransparentBars();
        mRestoreImmersiveModeHandler.postDelayed(restoreImmersiveModeRunnable, 500);
    }

    public void setWindowFlags() {
        View v = getWindow().getDecorView();
        v.setOnSystemUiVisibilityChangeListener(null);
        v.setOnFocusChangeListener(null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    public void setWindowFlagsAdvanced() {
        View v = getWindow().getDecorView();
        v.setOnSystemUiVisibilityChangeListener(null);
        v.setOnFocusChangeListener(null);

        if (currentapiVersion >= 16) {
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        if (currentapiVersion >= 19) {
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        Runnable testnavbar = new Runnable() {
            @Override
            public void run() {
                getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        Log.d("d","setOnSystemUiVisibilityChangeListener_1563");
                        restoreTransparentBars();
                    }
                });

                getWindow().getDecorView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        restoreTransparentBars();
                    }
                });
            }
        };

        Handler waitandtest = new Handler();
        waitandtest.postDelayed(testnavbar,1000);
    }

    public void restoreTransparentBars() {
        // Set runnable
        Runnable delhide = new Runnable() {
            @Override
            public void run() {
                if (View.SYSTEM_UI_FLAG_FULLSCREEN!=0) {
                    // Hide them
                    Log.d("d", "hide them");
                    main_page.requestFocus();
                    setWindowFlags();
                    setWindowFlagsAdvanced();
                }
            }
        };

        // Wait for 1000ms then check for Navigation bar visibility
        // If it is there, hide it
        Handler delayhidehandler = new Handler();
        delayhidehandler.postDelayed(delhide, 1000);

    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void hidepopupPad() {
        if (popupPad.getVisibility() == View.VISIBLE) {
            popupPad.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupPad.setVisibility(View.GONE);
                }
            }, 500);
        }
        if (popupPad_stoporstart.equals("stop")) {
            padButton.setAlpha(0.4f);
        } else {
            padButton.setAlpha(0.7f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    private void hidepopupAutoscroll() {
        if (popupAutoscroll.getVisibility() == View.VISIBLE) {
            popupAutoscroll.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupAutoscroll.setVisibility(View.GONE);
                }
            }, 500);
        }
        if (popupAutoscroll_stoporstart.equals("stop")) {
            autoscrollButton.setAlpha(0.4f);
        } else {
            autoscrollButton.setAlpha(0.7f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    private void hidepopupMetronome() {
        if (popupMetronome.getVisibility() == View.VISIBLE) {
            popupMetronome.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupMetronome.setVisibility(View.GONE);
                }
            }, 500);
        }
        if (popupMetronome_stoporstart.equals("stop")) {
            metronomeButton.setAlpha(0.4f);
        } else {
            metronomeButton.setAlpha(0.7f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    private void hidepopupSticky() {
        if (scrollstickyholder.getVisibility() == View.VISIBLE) {
            scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollstickyholder.setVisibility(View.GONE);
                }
            }, 500);
            stickynotes.setAlpha(0.4f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    public void popupLink(View view) {
        hidepopupPad();
        hidepopupAutoscroll();
        hidepopupMetronome();
        hidepopupSticky();
        hidepopupChord();
        newFragment = PopUpLinks.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void hidepopupChord() {
        if (popupChord.getVisibility() == View.VISIBLE) {
            popupChord.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupChord = new Handler();
            delayhidepopupChord.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupChord.setVisibility(View.GONE);
                }
            }, 500);
            chordButton.setAlpha(0.4f);
        }
        hideKeyboard();
        scrollpage.requestFocus();
    }

    public void popupChords_toggle(View view) {
        // Hide the other popups
        hidepopupSticky();
        hidepopupAutoscroll();
        hidepopupMetronome();
        hidepopupPad();
        if (popupChord.getVisibility() == View.VISIBLE) {
            // If popupChord is visible, it is about to be hidden
            // Also fade the chordButton
            chordButton.setAlpha(0.4f);
            popupChord.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupChord = new Handler();
            delayhidepopupChord.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupChord.setVisibility(View.GONE);
                }
            }, 500);
        } else {
            chordButton.setAlpha(0.7f);
            popupChord.setVisibility(View.VISIBLE);
            popupChord.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom));
            popupChord.requestFocus();
        }
    }

    public void popupPad_toggle(View view) {
        // Hide the other popups
        hidepopupSticky();
        hidepopupAutoscroll();
        hidepopupMetronome();
        hidepopupChord();
        scrollpage.requestFocus();
        if (popupPad.getVisibility() == View.VISIBLE) {
            // If popupPad is visible, it is about to be hidden
            // Also fade the padButton if the pad isn't playing, otherwise keep it bright
            if (popupPad_stoporstart.equals("stop")) {
                padButton.setAlpha(0.4f);
            } else {
                padButton.setAlpha(0.7f);
            }
            popupPad.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupPad = new Handler();
            delayhidepopupPad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupPad.setVisibility(View.GONE);
                }
            }, 500);
        } else {
            padButton.setAlpha(0.7f);
            popupPad.setVisibility(View.VISIBLE);
            popupPad.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom));
            popupPad.requestFocus();
        }
    }

    public void customChordEdit(View view) {
        Intent docustomchord = new Intent();
        docustomchord.setClass(FullscreenActivity.this, CustomChord.class);
        tryKillPads();
        tryKillMetronome();
        startActivity(docustomchord);
        FullscreenActivity.this.finish();
    }

    public void popupPad_startstop(View view) {
        // This is called when the start/stop button has been pressed
        if (popupPad_stoporstart.equals("start")) {
            // user now wants to stop
            // Turn off the button for now.  This will be reset after fadeout or 10secs
            popupPad_stoporstart = "stop";
            popupPad_startstopbutton.setText(getResources().getString(R.string.wait));
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_button));
            popupPad_startstopbutton.setEnabled(false);
            // Which pads are playing and need faded?
            fadeout1 = false;
            fadeout2 = false;
            if (mPlayer1 != null) {
                if (mPlayer1.isPlaying()) {
                    whichtofadeout = 1;
                    fadeout1 = true;
                }
            }
            if (mPlayer2 != null) {
                if (mPlayer2.isPlaying()) {
                    whichtofadeout = 2;
                    fadeout2 = true;
                }
            }

        } else {
            // user now wants to start
            popupPad_stoporstart = "start";
            popupPad_startstopbutton.setText(getResources().getString(R.string.stop));
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            popupPad_startstopbutton.setEnabled(true);
            hidepopupPad();
        }
        mKey = popupPad_key.getItemAtPosition(popupPad_key.getSelectedItemPosition()).toString();
        if (popupPad.getVisibility() == View.VISIBLE) {
            try {
                togglePlayPads(view);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stickyNotesUpdate() {
        mySticky.setText(mNotes);
    }

    @Override
    public void addSlideToSet() {
        String filename;
        String reusablefilename;
        String templocator;

        switch (noteorslide) {
            case "note":
                filename = dircustomnotes + "/" + customslide_title;
                reusablefilename = homedir + "/Notes/" + customslide_title;
                templocator = note;
                customimage_list = "";
                break;
            case "slide":
                filename = dircustomslides + "/" + customslide_title;
                reusablefilename = homedir + "/Slides/" + customslide_title;
                templocator = slide;
                customimage_list = "";
                break;
            case "scripture":
                filename = dirscriptureverses + "/" + customslide_title;
                reusablefilename = dirscripture + "/" + customslide_title;
                templocator = text_scripture;
                customreusable = false;
                customimage_list = "";
                break;
            default:
                filename = dircustomimages + "/" + customslide_title;
                reusablefilename = homedir + "/Images/" + customslide_title;
                templocator = image;
                break;
        }

        // If slide content is empty - put the title in
        if (customslide_content.isEmpty() && !noteorslide.equals("image")) {
            customslide_content = customslide_title;
        }

        // Prepare the custom slide so it can be viewed in the app
        // When exporting/saving the set, the contents get grabbed from this
        mynewXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        mynewXML += "<song>\n";
        mynewXML += "  <title>" + customslide_title + "</title>\n";
        mynewXML += "  <author> </author>\n";
        mynewXML += "  <user1>" + customimage_time + "</user1>\n";  // This is used for auto advance time
        mynewXML += "  <user2>" + customimage_loop + "</user2>\n";  // This is used for loop on or off
        mynewXML += "  <user3>" + customimage_list + "</user3>\n";  // This is used as links to a background images
        mynewXML += "  <aka></aka>\n";
        mynewXML += "  <key_line></key_line>\n";
        mynewXML += "  <hymn_number></hymn_number>\n";
        mynewXML += "  <lyrics>" + customslide_content + "</lyrics>\n";
        mynewXML += "</song>";

        mynewXML = mynewXML.replace("&amp;", "&");
        mynewXML = mynewXML.replace("&", "&amp;");

        // Now write the modified song
        FileOutputStream overWrite;
        try {
            overWrite = new FileOutputStream(filename, false);
            overWrite.write(mynewXML.getBytes());
            overWrite.flush();
            overWrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If this is to be a reusable custom slide
        if (customreusable) {
            // Now write the modified song
            FileOutputStream overWriteResuable;
            try {
                overWriteResuable = new FileOutputStream(reusablefilename, false);
                overWriteResuable.write(mynewXML.getBytes());
                overWriteResuable.flush();
                overWriteResuable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            customreusable = false;
        }

        // Add to set
        whatsongforsetwork = "$**_" + templocator + "/" + customslide_title + "_**$";

        // Allow the song to be added, even if it is already there
        mySet = mySet + whatsongforsetwork;

        // Tell the user that the song has been added.
        myToastMessage = "\"" + customslide_title + "\" " + getResources().getString(R.string.addedtoset);
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
        }, 1000);

    }

    @Override
    public void loadSongFromSet() {
        Preferences.savePreferences();

        try {
            newFragment.dismiss();
        } catch (Exception e) {
            Log.d("d","Fragment already closed");
        }
        redrawTheLyricsTable(view);
    }

    @Override
    public void shuffleSongsInSet() {
        newFragment = PopUpSetViewNew.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void loadCustomReusable() {
        // This is called from the file chooser fragment.
        // Load in the custom reusable, then reopen the custom slide editor
        // Put the old myXML and song fields into temporary memory while we load in the new ones
        LoadXML.prepareLoadCustomReusable(customreusabletoload);
        // This reopens the choose backgrounds popupFragment
        newFragment = PopUpCustomSlideFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void updateCustomStorage() {
        // Reopen the appropriate dialog fragment
        if (myToastMessage.equals("link_other")) {
            mLinkOther = filechosen.toString();
            newFragment = PopUpLinks.newInstance();
            newFragment.show(getFragmentManager(), "dialog");
        } else if (myToastMessage.equals("link_audio")) {
            mLinkAudio = filechosen.toString();
            newFragment = PopUpLinks.newInstance();
            newFragment.show(getFragmentManager(), "dialog");
        }
    }

    private class popupChord_InstrumentListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            switch (position) {
                case 0:
                    chordInstrument = "g";
                    break;
                case 1:
                    chordInstrument = "u";
                    break;
                case 2:
                    chordInstrument = "m";
                    break;
                case 3:
                    chordInstrument = "c";
                    break;
                case 4:
                    chordInstrument = "b";
                    break;
                case 5:
                    chordInstrument = "B";
                    break;
            }
            //Save preferences and redraw the chords
            Preferences.savePreferences();
            prepareChords();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e("popupChord", "Nothing selected");
        }

    }

    private class popupPad_keyListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            mKey = popupPad_key.getItemAtPosition(popupPad_key.getSelectedItemPosition()).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e("popupPad", "Nothing selected");
        }
    }

    private class popupPad_fileListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            if (position == 1) {
                if (mLinkAudio.isEmpty() || mLinkAudio.equals("")) {
                    mPadFile = getResources().getString(R.string.pad_auto);
                    popupPad_file.setSelection(0);
                    myToastMessage = getResources().getString(R.string.notset);
                    ShowToast.showToast(FullscreenActivity.this);
                }
            }
            FullscreenActivity.mPadFile = popupPad_file.getItemAtPosition(popupPad_file.getSelectedItemPosition()).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e("popupFile", "Nothing selected");
        }

    }

    private class popupPad_volumeListener implements OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            popupPad_volume_text.setText(popupPad_volume.getProgress() + " %");
            float temp_padvol = (float) popupPad_volume.getProgress() / 100.0f;
            String temp_padpan = "both";
            if (popupPad_pan.getProgress() == 0) {
                temp_padpan = "left";
                popupPad_pan_text.setText("L");
            } else if (popupPad_pan.getProgress() == 2) {
                temp_padpan = "right";
                popupPad_pan_text.setText("R");
            } else {
                popupPad_pan_text.setText("C");
            }
            if (mPlayer1 != null) {
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
            if (mPlayer2 != null) {
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

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            int temp_padvol = popupPad_volume.getProgress();
            padvol = temp_padvol / 100;
            if (popupPad_pan.getProgress() == 0) {
                padpan = "left";
            } else if (popupPad_pan.getProgress() == 2) {
                padpan = "right";
            } else {
                padpan = "both";
            }

            // Save preferences
            Preferences.savePreferences();
        }
    }

    public void popupMetronome_toggle(View view) {
        // Hide the other popups
        hidepopupSticky();
        hidepopupAutoscroll();
        hidepopupPad();
        hidepopupChord();
        scrollpage.requestFocus();
        if (popupMetronome.getVisibility() == View.VISIBLE) {
            // If user has clicked on stop, fade button
            if (popupMetronome_stoporstart.equals("stop")) {
                metronomeButton.setAlpha(0.4f);
            } else {
                metronomeButton.setAlpha(0.7f);
            }
            popupMetronome.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupMetronome = new Handler();
            delayhidepopupMetronome.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupMetronome.setVisibility(View.GONE);
                }
            }, 500);
        } else {
            metronomeButton.setAlpha(0.7f);
            popupMetronome.setVisibility(View.VISIBLE);
            popupMetronome.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom));
        }
    }

    public void popupMetronome_startstop(View view) {
        if (popupMetronome_stoporstart.equals("start")) {
            // user now wants to stop
            popupMetronome_stoporstart = "stop";

            if (ab != null) {
                ab.setBackgroundDrawable(new ColorDrawable(beatoffcolour));
            }

            if (popupMetronome.getVisibility() != View.VISIBLE) {
                metronomeButton.setAlpha(0.4f);
            }
            popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
        } else {
            if (!isPDF) {
                // user now wants to start
                popupMetronome_stoporstart = "start";
                metronomeButton.setAlpha(0.7f);
                popupMetronome_startstopbutton.setText(getResources().getString(R.string.stop));
            }
        }
        mTimeSig = popupMetronome_timesig.getItemAtPosition(popupMetronome_timesig.getSelectedItemPosition()).toString();
        int valoftempobar = popupMetronome_tempo.getProgress() + 39;
        if (valoftempobar > 39) {
            mTempo = "" + valoftempobar;
        } else {
            mTempo = "";
        }
        if (!isPDF && popupMetronome.getVisibility() == View.VISIBLE) {
            metronomeToggle(view);
        }
    }

    private class popupMetronome_volumeListener implements OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            popupMetronome_volume_text.setText(popupMetronome_volume.getProgress() + " %");
            metronomevol = (float) popupMetronome_volume.getProgress() / 100.0f;
            if (fromUser) {
                if (popupMetronome_pan.getProgress() == 0) {
                    metronomepan = "left";
                    popupMetronome_pan_text.setText("L");
                } else if (popupMetronome_pan.getProgress() == 2) {
                    metronomepan = "right";
                    popupMetronome_pan_text.setText("R");
                } else {
                    popupMetronome_pan_text.setText("C");
                    metronomepan = "both";
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            // Save preferences
            Preferences.savePreferences();
        }
    }

    private class popupMetronome_tempoListener implements OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            temposlider = popupMetronome_tempo.getProgress() + 39;
            short newbpm = (short) temposlider;
            if (temposlider > 39) {
                popupMetronome_tempo_text.setText(""+temposlider);
            } else {
                popupMetronome_tempo_text.setText(getResources().getString(R.string.notset));
            }
            if (newbpm < 40) {
                mTempo = "";
                bpm = 100;
                if (metroTask != null) {
                    metroTask.cancel(true);
                    metroTask.stop();
                }
            } else {
                try {
                    metroTask.setBpm(newbpm);
                    metroTask.setBeat(beats);
                    metroTask.setNoteValue(noteValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            int valoftempobar = popupMetronome_tempo.getProgress() + 39;
            if (valoftempobar > 39) {
                mTempo = "" + valoftempobar;
            } else {
                mTempo = "";
            }
        }
    }

    private class popupMetronome_timesigListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            // Hold mTimeSig as a temp value
            String temp_sig = mTimeSig;
            mTimeSig = popupMetronome_timesig.getItemAtPosition(popupMetronome_timesig.getSelectedItemPosition()).toString();
            ProcessSong.processTimeSig();

            if (metroTask != null) {
                metroTask.setNoteValue(noteValue);
                metroTask.setBeat(beats);
                metroTask.setBpm(bpm);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e("metronome", "Nothing selected");
        }

    }

    // Backwards compatible recreate().
    @Override
    public void recreate() {
        super.recreate();
    }

    public void saveSong(View view) {
        PopUpEditSongFragment.prepareSongXML();

        // Now write the modified song
        try {
            PopUpEditSongFragment.justSaveSongXML();
        } catch (IOException e) {
            e.printStackTrace();
        }

        myToastMessage = getResources().getString(R.string.savesong) + " - " + getResources().getString(R.string.ok);
        ShowToast.showToast(FullscreenActivity.this);
    }

    private void checkDirectories() {
        if (secStorage != null) {
            if (secStorage.contains(":")) {
                secStorage = secStorage.substring(0, secStorage.indexOf(":"));
            }
        } else {
            // Lets look for alternative secondary storage positions
            for (String secStorageOption : secStorageOptions) {
                File testaltsecstorage = new File(secStorageOption);
                if (testaltsecstorage.exists() && testaltsecstorage.canWrite()) {
                    secStorage = secStorageOption;
                }
            }
        }

        // If secondary and default storage are the same thing, hide secStorage
        if (defStorage.equals(secStorage)) {
            secStorage = null;
        }

        // I want folders to be ready on internal and external storage (if available)
        intStorCheck = new File(defStorage);
        if (intStorCheck.exists() && intStorCheck.canWrite()) {
            defStorageExists = true;
        }

        root = Environment.getExternalStorageDirectory();
        homedir = new File(root.getAbsolutePath() + "/documents/OpenSong");
        dir = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs");
        dironsong = new File(root.getAbsolutePath() + "/documents/OpenSong/Songs/OnSong");
        dirsets = new File(root.getAbsolutePath() + "/documents/OpenSong/Sets");
        dirPads = new File(root.getAbsolutePath() + "/documents/OpenSong/Pads");
        dirbackgrounds = new File(root.getAbsolutePath() + "/documents/OpenSong/Backgrounds");
        dirbibles = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture");
        dirbibleverses = new File(root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture/_cache");
        dirscripture = new File(root.getAbsolutePath() + "/documents/OpenSong/Scripture");
        dirscriptureverses = new File(root.getAbsolutePath() + "/documents/OpenSong/Scripture/_cache");
        dircustomslides = new File(root.getAbsolutePath() + "/documents/OpenSong/Slides/_cache");
        dircustomnotes = new File(root.getAbsolutePath() + "/documents/OpenSong/Notes/_cache");

        if (secStorage != null) {
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
                    dirscripture = new File(root.getAbsolutePath() + "/documents/OpenSong/Scripture");
                    dirscriptureverses = new File(root.getAbsolutePath() + "/documents/OpenSong/Scripture/_cache");
                    dircustomslides = new File(root.getAbsolutePath() + "/documents/OpenSong/Slides/_cache");
                    dircustomnotes = new File(root.getAbsolutePath() + "/documents/OpenSong/Notes/_cache");
                }
            }
        }

        File customStorCheck;
        boolean customStorageExists = false;
        if (customStorage != null) {
            customStorCheck = new File(customStorage);
            if (customStorCheck.exists() && customStorCheck.canWrite()) {
                customStorageExists = true;
                if (prefStorage.equals("other")) {
                    root = customStorCheck;
                    homedir = new File(root.getAbsolutePath() + "/OpenSong");
                    dir = new File(root.getAbsolutePath() + "/OpenSong/Songs");
                    dironsong = new File(root.getAbsolutePath() + "/OpenSong/Songs/OnSong");
                    dirsets = new File(root.getAbsolutePath() + "/OpenSong/Sets");
                    dirPads = new File(root.getAbsolutePath() + "/OpenSong/Pads");
                    dirbackgrounds = new File(root.getAbsolutePath() + "/OpenSong/Backgrounds");
                    dirbibles = new File(root.getAbsolutePath() + "/OpenSong/OpenSong Scripture");
                    dirbibleverses = new File(root.getAbsolutePath() + "/OpenSong/OpenSong Scripture/_cache");
                    dirscripture = new File(root.getAbsolutePath() + "/OpenSong/Scripture");
                    dirscriptureverses = new File(root.getAbsolutePath() + "/OpenSong/Scripture/_cache");
                    dircustomslides = new File(root.getAbsolutePath() + "/OpenSong/Slides/_cache");
                    dircustomnotes = new File(root.getAbsolutePath() + "/OpenSong/Notes/_cache");
                }
            }
        }

        if (prefStorage.equals("int") && !defStorageExists) {
            storageIsValid = false;
        }

        if (prefStorage.equals("ext") && !extStorageExists) {
            storageIsValid = false;
        }

        if (prefStorage.equals("other") && !customStorageExists) {
            storageIsValid = false;
        }

        if (prefStorage.isEmpty() || !storageIsValid) {
            intStorCheck = null;
            extStorCheck = null;
            customStorCheck = null;
            // Not set, or not valid - open the storage activity.
            Intent intent_stop = new Intent();
            intent_stop.setClass(this, StorageChooser.class);
            startActivity(intent_stop);
            finish();
        } else {
            // Create the folders if they don't exist
            StorageChooser.createDirectory(homedir);
            StorageChooser.createDirectory(dir);
            StorageChooser.createDirectory(dirsets);
            StorageChooser.createDirectory(dirPads);
            StorageChooser.createDirectory(dirbackgrounds);
            StorageChooser.createDirectory(dirbibles);
            StorageChooser.createDirectory(dirbibleverses);
            StorageChooser.createDirectory(dirscripture);
            StorageChooser.createDirectory(dirscriptureverses);
            StorageChooser.createDirectory(dircustomimages);
            StorageChooser.createDirectory(dircustomnotes);
            StorageChooser.createDirectory(dircustomslides);

            // Make sure the default files are there
            copyDefaultFiles();
        }
    }

    private void promptTempo() {
        if (popupMetronome.getVisibility() != View.VISIBLE) {
            metronomeButton.setAlpha(0.4f);
        }
        popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
        popupMetronome_stoporstart = "stop";
        metronomeonoff = "off";
        myToastMessage = getResources().getString(R.string.temponotset);
        ShowToast.showToast(FullscreenActivity.this);
    }

    public void popUpSetList(View view) {
        // Show set
        newFragment = PopUpSetViewNew.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void promptTimeSig() {
        if (popupMetronome.getVisibility() != View.VISIBLE) {
            metronomeButton.setAlpha(0.4f);
        }
        popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
        popupMetronome_stoporstart = "stop";
        metronomeonoff = "off";
        myToastMessage = getResources().getString(R.string.timesignotset);
        ShowToast.showToast(FullscreenActivity.this);
    }

    private void metronomeToggle(View view) {
        if (metronomeonoff.equals("on")) {
            metronomeonoff = "off";
            if (metroTask != null) {
                metroTask.cancel(true);
                metroTask.stop();
            }

        } else {
            mTimeSigValid = false;
            if (mTimeSig.isEmpty() || mTimeSig.equals("")) {
                promptTimeSig();
            } else {
                ProcessSong.processTimeSig();
            }

            boolean mTempoValid;
            if (mTempo.isEmpty()) {
                promptTempo();
            } else {
                ProcessSong.processTempo();
                String temp_tempo = mTempo;
                temp_tempo = temp_tempo.replace("Very Fast", "140");
                temp_tempo = temp_tempo.replace("Fast", "120");
                temp_tempo = temp_tempo.replace("Moderate", "100");
                temp_tempo = temp_tempo.replace("Slow", "80");
                temp_tempo = temp_tempo.replace("Very Slow", "60");
                temp_tempo = temp_tempo.replaceAll("[\\D]", "");
                try {
                    bpm = (short) Integer.parseInt(temp_tempo);
                } catch (NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                    bpm = 39;
                }
            }
            mTempoValid = bpm >= minBpm && bpm <= maxBpm;

            if (mTempo.equals("") || mTempo == null) {
                mTempoValid = false;
            }

            if (mTimeSigValid && mTempoValid) {
                metroTask = new MetronomeAsyncTask();
                Runtime.getRuntime().gc();
                metronomeonoff = "on";
                metroTask.execute();
            }
        }
    }

    private class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {
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
            if (metronome != null) {
                metronome.stop();
                metronome = null;
            }
            popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
            popupMetronome_stoporstart = "stop";
        }

        public void setBpm(short bpm) {
            if (metronome != null && bpm >= minBpm && bpm <= maxBpm && noteValue > 0) {
                metronome.setBpm(bpm);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void setNoteValue(short noteVal) {
            if (metronome != null && bpm >= minBpm && bpm <= maxBpm && noteVal > 0) {
                metronome.setNoteValue(noteVal);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void setBeat(short beat) {
            if (metronome != null) {
                metronome.setBeat(beat);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void setCurrentBeat(int currentBeat) {
            if (metronome != null)
                metronome.setCurrentBeat(currentBeat);
            try {
                metronome.calcSilence();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("unused")
        public void setVolume(float metrovol) {
            if (metronome != null)
                metronome.setVolume(metrovol);
        }
    }

    private class textWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mDuration = popupAutoscroll_duration.getText().toString();
            try {
                autoScrollDuration = Integer.parseInt(mDuration.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                autoScrollDuration = -1;
            }
        }
    }

    private class popupAutoscroll_delayListener implements OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            progress = progress - 1;
            String text;
            if (progress < 0) {
                text = "";
            } else {
                text = progress + " s";
            }
            popupAutoscroll_delay_text.setText(text);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            autoScrollDelay = seekBar.getProgress() - 1;
            mPreDelay = "" + autoScrollDelay;
        }
    }

    public void popupAutoscroll_toggle(View view) {
        // Hide other popup windows
        hidepopupPad();
        hidepopupSticky();
        hidepopupMetronome();

        if (popupAutoscroll.getVisibility() == View.VISIBLE) {
            // If user has clicked on stop, fade button
            if (popupAutoscroll_stoporstart.equals("stop")) {
                autoscrollButton.setAlpha(0.4f);
            } else {
                autoscrollButton.setAlpha(0.7f);
            }
            popupAutoscroll.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidepopupAutoscroll = new Handler();
            delayhidepopupAutoscroll.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupAutoscroll.setVisibility(View.GONE);
                }
            }, 500);
        } else {
            autoscrollButton.setAlpha(0.7f);
            popupAutoscroll.setVisibility(View.VISIBLE);
            popupAutoscroll.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom));
        }
    }

    public void popupAutoscroll_startstop(View view) {
        if (popupAutoscroll_stoporstart.equals("start")) {
            // user now wants to stop
            popupAutoscroll_stoporstart = "stop";
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
            autoscrollactivated = false;
            // Hide the autoscroll playback progress
            playbackProgress.setVisibility(View.INVISIBLE);
        } else {
            // user now wants to start
            popupAutoscroll_stoporstart = "start";
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.stop));
            autoscrollactivated = true;
            // Show the autoscroll playback progress
            String text = timeFormatFixer((autoScrollDuration));
            currentTime_TextView.setText("0:00");
            totalTime_TextView.setText(text);
            playbackProgress.setVisibility(View.VISIBLE);

        }

        autoScrollDelay = popupAutoscroll_delay.getProgress() - 1;
        mDuration = popupAutoscroll_duration.getText().toString();

        if (popupAutoscroll.getVisibility() == View.VISIBLE) {
            popupAutoscroll_toggle(view);
        }
        autoScroll(view);
    }

    private void autoScroll(View view) {
        if (!autoscrollispaused) {
            // If we haven't paused the autoscroll part way down a page, reset to the top
            // This is important if we have autoscrolled to the end and want to repeat.
            scrollpage.smoothScrollTo(0, 0);
        }

        if (autoscrollonoff.equals("on")) {
            // We want to stop scrolling
            autoscrollonoff = "off";
            isautoscrolling = false;
            pauseautoscroll = true;
            autoscrollispaused = true;
            myToastMessage = getResources().getString(R.string.autoscroll) + " - " + getResources().getString(R.string.off);
            ShowToast.showToast(FullscreenActivity.this);
            // Turn the button off
            if (popupAutoscroll.getVisibility() != View.VISIBLE) {
                autoscrollButton.setAlpha(0.4f);
            }
            // Hide the autoscroll playback progress
            playbackProgress.setVisibility(View.INVISIBLE);

        } else {
            // We want to start
            boolean usingdefaults = false;
            boolean showtheprompt = false;
            if (mDuration.isEmpty() && autoscroll_default_or_prompt.equals("prompt")) {
                showtheprompt = true;
            } else if (mDuration.isEmpty() && autoscroll_default_or_prompt.equals("default")) {
                // Use the song defaults
                autoScrollDuration = default_autoscroll_songlength;
                usingdefaults = true;
            }

            if (mPreDelay.isEmpty() && autoscroll_default_or_prompt.equals("prompt")) {
                showtheprompt = true;
            } else if (mPreDelay.isEmpty() && autoscroll_default_or_prompt.equals("default")) {
                // Use the song defaults
                autoScrollDelay = default_autoscroll_predelay;
                usingdefaults = true;
            }

            if (usingdefaults) {
                myToastMessage = getString(R.string.default_autoscroll);
                ShowToast.showToast(FullscreenActivity.this);
            }

            if (showtheprompt && isSong) {
                // Show the prompt
                playbackProgress.setVisibility(View.INVISIBLE);
                promptSongDuration();
            } else if (showtheprompt && !isSong) {
                // Do nothing as it isn't a song!
                playbackProgress.setVisibility(View.INVISIBLE);
            } else {
                if (autoScrollDuration > 0 && autoScrollDelay > -1) {
                    autoscrollonoff = "on";
                    pauseautoscroll = true;
                    boolean dontdelay = false;
                    if (autoscrollispaused) {
                        dontdelay = true;
                    }
                    getAutoScrollValues();
                    if (dontdelay) {
                        autoScrollDelay = 0;
                    }

                    // Show the autoscroll progress
                    //String text = timeFormatFixer(autoScrollDelay);
                    playbackProgress.setVisibility(View.VISIBLE);

                    autoscrollispaused = false;
                    myToastMessage = getResources().getString(R.string.autoscroll) + " - " + getResources().getString(R.string.on);
                    ShowToast.showToast(FullscreenActivity.this);
                    // Turn the button on
                    autoscrollButton.setAlpha(0.7f);
                    isautoscrolling = true;

                    // After the autoScrollDelay, postexecute the task
                    time_start = System.currentTimeMillis();
                    delayautoscroll = new Handler();
                    delayautoscroll.post(new Runnable() {
                        @Override
                        public void run() {
                            mtask_autoscroll_music = (AutoScrollMusic) new AutoScrollMusic().execute();
                        }
                    });
                }
            }
        }
    }

    private void promptSongDuration() {
        // Create a dialogue and get the song duration and predelay
        // Close the menus
        autoscrollButton.setAlpha(0.4f);
        mDrawerLayout.closeDrawer(expListViewOption);
        mDrawerLayout.closeDrawer(expListViewOption);

        // Set up the Alert Dialogue
        // This bit gives the user a prompt to create a new song
        AlertDialog.Builder songdurationprompt = new AlertDialog.Builder(this);
        songdurationprompt.setTitle(getResources().getString(R.string.edit_song_duration));

        final LinearLayout autoscroll_vals = new LinearLayout(this);
        autoscroll_vals.setOrientation(LinearLayout.VERTICAL);

        final TextView durationtext = new TextView(this);
        durationtext.setText(getResources().getString(R.string.edit_song_duration));

        // Set an EditText view to get user input
        final EditText newduration = new EditText(this);
        if (mDuration.isEmpty()) {
            mDuration = "" + default_autoscroll_songlength;
        }
        newduration.setText(mDuration);
        newduration.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        // Set an SeekBar view to get the predelay
        final TextView predelaytext = new TextView(this);
        predelaytext.setText(getString(R.string.autoscroll_time));
        final SeekBar newpredelay = new SeekBar(this);
        final TextView newpredelaytext = new TextView(this);
        String text = default_autoscroll_predelay + " s";
        newpredelaytext.setText(text);
        newpredelay.setMax(30);
        newpredelay.setProgress(default_autoscroll_predelay);
        newpredelay.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + " s";
                newpredelaytext.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(newduration, InputMethodManager.SHOW_IMPLICIT);
        newduration.requestFocus();
        songdurationprompt.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mDuration = newduration.getText().toString();
                        mPreDelay = "" + newpredelay.getProgress();
                        PopUpEditSongFragment.prepareSongXML();
                        try {
                            PopUpEditSongFragment.justSaveSongXML();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            LoadXML.loadXML();
                        } catch (XmlPullParserException | IOException e) {
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

        autoscroll_vals.addView(durationtext);
        autoscroll_vals.addView(newduration);
        autoscroll_vals.addView(predelaytext);
        autoscroll_vals.addView(newpredelay);
        autoscroll_vals.addView(newpredelaytext);
        songdurationprompt.setView(autoscroll_vals);
        songdurationprompt.show();
    }

    private void getAutoScrollValues() {
        // Get the autoScrollDuration;
        if (mDuration.isEmpty() && autoscroll_default_or_prompt.equals("default")) {
            autoScrollDuration = default_autoscroll_songlength;
        } else if (mDuration.isEmpty() && autoscroll_default_or_prompt.equals("prompt")) {
            autoScrollDuration = -1;
        } else {
            try {
                autoScrollDuration = Integer.parseInt(mDuration.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                autoScrollDuration = 0;
            }
        }

        // Get the autoScrollDelay;
        if (mPreDelay.isEmpty() && autoscroll_default_or_prompt.equals("default")) {
            autoScrollDelay = default_autoscroll_predelay;
        } else if (mDuration.isEmpty() && autoscroll_default_or_prompt.equals("prompt")) {
            autoScrollDelay = -1;
        } else {
            try {
                autoScrollDelay = Integer.parseInt(mPreDelay.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                autoScrollDelay = 0;
            }
        }

        if (autoScrollDuration > -1 && autoScrollDelay > -1) {
            // If it duration is less than the predelay, stop!
            if (autoScrollDuration < autoScrollDelay) {
                return;
            } else {
                // Remove the autoScrollDelay
                autoScrollDuration = autoScrollDuration - autoScrollDelay;
            }

            // Ok figure out the size of amount of scrolling needed
            int height = (scrollpage.getChildAt(0).getMeasuredHeight()) - main_page.getHeight();
            if (height >= scrollpage.getScrollY()) {
                total_pixels_to_scroll = height;
            } else {
                total_pixels_to_scroll = 0;
            }

            // Ok how many pixels per 500ms - autoscroll_pause_time
            autoscroll_pixels = ((float) total_pixels_to_scroll / ((float) autoScrollDuration * 1000 / (float) autoscroll_pause_time));

            newPosFloat=0.0f;
            if (autoscroll_pixels>0) {
                playbackProgress.setVisibility(View.VISIBLE);
            }
            autoScrollDuration = autoScrollDuration + autoScrollDelay;
        }
    }

    private class ProgressTime extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            time_start = System.currentTimeMillis();
        }

        @Override
        protected String doInBackground(String... args) {
            time_start = System.currentTimeMillis();
            time_passed = System.currentTimeMillis();
            while ((time_passed - time_start)/1000 < autoScrollDelay) {
                if ((time_passed-time_start)>1000) {
                    doProgressTime.post(progressTimeRunnable);
                }
                time_passed = System.currentTimeMillis();
            }
            return null;
        }
    }

    private class AutoScrollMusic extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            scrollpageHeight = scrollpage.getChildAt(0).getMeasuredHeight() - scrollpage.getHeight();
            time_start = System.currentTimeMillis();
        }

        @Override
        protected String doInBackground(String... args) {
            while (isautoscrolling) {
                long starttime = System.currentTimeMillis();
                time_passed = System.currentTimeMillis();
                boolean doscroll = ((time_passed - time_start) / 1000) >= autoScrollDelay;
                if (doscroll) {
                    publishProgress(1);
                }
                // don't scroll first time
                if (!pauseautoscroll) {
                    doProgressTime.post(progressTimeRunnable);
                    if (doscroll) {
                        doautoScroll.post(autoScrollRunnable);
                    }
                } else {
                    pauseautoscroll = false;
                }
                if (doscroll) {
                    if (newPosFloat >= scrollpageHeight) {
                        //if (newPos >= scrollpageHeight) {
                        autoscrollispaused = false;
                        isautoscrolling = false;
                    }
                }

                long currtime = System.currentTimeMillis();
                while ((currtime - starttime) < autoscroll_pause_time) {
                    currtime = System.currentTimeMillis();
                }

            }
            return "dummy";
        }

        @Override
        protected void onProgressUpdate(Integer... intg) {
            newPosFloat = newPosFloat + autoscroll_pixels;
            newPos = (int) (scrollpage.getScrollY() + autoscroll_pixels);
        }

        @Override
        protected void onPostExecute(String dummy) {
            if (!isautoscrolling) {
                pauseautoscroll = false;
            } else {
                isautoscrolling = false;
                pauseautoscroll = true;
            }
            if (popupAutoscroll.getVisibility() != View.VISIBLE) {
                autoscrollButton.setAlpha(0.4f);
            }
            popupAutoscroll_stoporstart = "stop";
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
            autoscrollonoff = "off";
            if (mtask_autoscroll_music != null) {
                mtask_autoscroll_music.cancel(true);
                mtask_autoscroll_music = null;
            }
            playbackProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCancelled() {
            isautoscrolling = false;
            pauseautoscroll = true;
            if (popupAutoscroll.getVisibility() != View.VISIBLE) {
                autoscrollButton.setAlpha(0.4f);
            }
            popupAutoscroll_stoporstart = "stop";
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
            autoscrollonoff = "off";
            if (mtask_autoscroll_music != null) {
                mtask_autoscroll_music.cancel(true);
            }
        }
    }

    public void visualMetronomeToggle(View view) {
        if (popupMetronome_visualmetronometoggle.isChecked()) {
            visualmetronome = true;
        } else {
            visualmetronome = false;
            ab.setBackgroundDrawable(new ColorDrawable(beatoffcolour));
        }
        Preferences.savePreferences();
    }

    private void tryKillMetronome() {
        if (metronomeonoff.equals("on")) {
            metronomeonoff = "off";
            if (metroTask != null) {
                metroTask.cancel(true);
                metroTask.stop();
            }
        }
        metronomeButton.setAlpha(0.4f);
    }

    private void tryKillPads() {
        // Hard stop all pads - leaving
        killfadeout1 = true;
        killfadeout2 = true;
        isfading1 = false;
        isfading2 = false;
        if (mtask_fadeout_music1 != null) {
            mtask_fadeout_music1 = null;
        }
        if (mtask_fadeout_music2 != null) {
            mtask_fadeout_music2 = null;
        }
        try {
            killPad1(padButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            killPad2(padButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
        padson = false;
        if (popupPad.getVisibility() != View.VISIBLE) {
            padButton.setAlpha(0.4f);
        }
    }

    private void killPad1(View view) {
        // This releases mPlayer1 if it has finished fading out only
        if (mPlayer1 != null) {
            if (mPlayer1.isPlaying()) {
                mPlayer1.stop();
            }
            mPlayer1.reset();
            mPlayer1.release();
        }
        mPlayer1 = null;
        fadeout1 = false;

        isfading1 = false;

        if (mtask_fadeout_music1 != null) {
            mtask_fadeout_music1 = null;
        }
        if (mPlayer2 == null) {
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            popupPad_startstopbutton.setEnabled(true);
            popupPad_stoporstart = "stop";
            padson = false;
        }
    }

    private void killPad2(View view) {
        // This releases mPlayer2 if it has finished fading out only
        if (mPlayer2 != null) {
            if (mPlayer2.isPlaying()) {
                mPlayer2.stop();
            }
            mPlayer2.reset();
            mPlayer2.release();
        }
        mPlayer2 = null;
        fadeout2 = false;

        isfading1 = false;

        if (mtask_fadeout_music2 != null) {
            mtask_fadeout_music2 = null;
        }
        if (mPlayer1 == null) {
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            popupPad_startstopbutton.setEnabled(true);
            popupPad_stoporstart = "stop";
            padson = false;
        }
    }

    private void togglePlayPads(View view) throws IllegalStateException {
        // This is called when the user clicks on the padButton start/stop button.
        // If padson is true, user has tried to stop playing (fade out)
        // If padson is false, user has tried to start playing (only after any previous fade out)

        padButton.setAlpha(0.7f);
        if (padson) {
            if (popupPad.getVisibility() != View.VISIBLE) {
                padButton.setAlpha(0.4f);
            }
            // If currently fading out, kill the fade out in progress
            if (isfading1) {
                killfadeout1 = true;
            } else {
                killfadeout1 = false;
                // Do a fadeout
                if (mPlayer1 != null) {
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
                if (mPlayer2 != null) {
                    whichtofadeout = 2;
                    fadeout2 = true;
                    fadeOutBackgroundMusic2();
                }
            }
            myToastMessage = getResources().getString(R.string.pad_stopped);
            ShowToast.showToast(FullscreenActivity.this);
            popupPad_startstopbutton.setEnabled(false);
            popupPad_startstopbutton.setText(getResources().getString(R.string.wait));
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_button));
            popupPad_stoporstart = "stop";


            // In case of errors, check the mPlayer states again in 10 secs and fix the buttons if need be
            Handler checkmPlayerStates = new Handler();
            checkmPlayerStates.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((mPlayer1 == null && mPlayer2 == null) || ((mPlayer1 != null && !mPlayer1.isPlaying()) && (mPlayer2 != null && !mPlayer2.isPlaying()))) {
                        popupPad_stoporstart = "stop";
                        padson = false;
                        popupPad_startstopbutton.setEnabled(true);
                        popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                        popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                        fadeout1 = false;
                        fadeout2 = false;
                        killfadeout1 = false;
                        killfadeout2 = false;
                        if (mtask_fadeout_music1 != null) {
                            mtask_fadeout_music1 = null;
                        }
                        if (mtask_fadeout_music2 != null) {
                            mtask_fadeout_music2 = null;
                        }
                        if (popupPad.getVisibility() != View.VISIBLE) {
                            padButton.setAlpha(0.4f);
                        }
                        mPlayer1 = null;
                        mPlayer2 = null;
                    }
                }
            }, (crossFadeTime + 2000));

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

            // test linkaudio file with mediaplayer
            MediaPlayer testMPlayer = new MediaPlayer();
            boolean testfile = true;
            try {
                testMPlayer.setDataSource(this, Uri.parse(mLinkAudio));
                testMPlayer.prepareAsync();
            } catch (Exception e) {
                testfile = false;
            }


            if ((!mKey.isEmpty() && !mKey.equals("") && !mPadFile.equals(getResources().getString(R.string.off)) && !mPadFile.equals(getResources().getString(R.string.link_audio)))) {
                // So far so good, change the button (valid key is checked later).
                // This will be an auto pad backing track
                padson = true;
                padButton.setAlpha(0.7f);
                popupPad_startstopbutton.setText(getResources().getString(R.string.stop));
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                popupPad_startstopbutton.setEnabled(true);
                popupPad_stoporstart = "start";

                myToastMessage = getResources().getString(R.string.pad_playing);
                ShowToast.showToast(FullscreenActivity.this);
                playPads(view);

            } else if (mPadFile.equals(getResources().getString(R.string.link_audio)) && testfile && !mLinkAudio.isEmpty() && !mLinkAudio.equals("")) {
                // This is a valid audio link file
                padson = true;
                padButton.setAlpha(0.7f);
                popupPad_startstopbutton.setText(getResources().getString(R.string.stop));
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                popupPad_startstopbutton.setEnabled(true);
                popupPad_stoporstart = "start";

                myToastMessage = getResources().getString(R.string.pad_playing);
                ShowToast.showToast(FullscreenActivity.this);
                playPads(view);

            } else if (mPadFile.equals(getResources().getString(R.string.link_audio)) && (!testfile || mLinkAudio.isEmpty() || mLinkAudio.equals(""))) {
                // Tried to use a link file, but it is empty or not valid
                myToastMessage = getResources().getString(R.string.link_audio) + " - " + getResources().getString(R.string.file_type_unknown);
                ShowToast.showToast(FullscreenActivity.this);
                // Do nothing until key is specified
                if (popupPad.getVisibility() != View.VISIBLE) {
                    // User has probably pressed the foot pedal - don't show prompt
                    padButton.setAlpha(0.4f);
                }
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                popupPad_startstopbutton.setEnabled(true);
                popupPad_stoporstart = "stop";
                padson = false;

            } else {
                // Key isn't set / is empty
                myToastMessage = getResources().getString(R.string.pad_error);
                ShowToast.showToast(FullscreenActivity.this);
                // Do nothing until key is specified
                if (popupPad.getVisibility() != View.VISIBLE) {
                    // User has probably pressed the foot pedal - don't show prompt
                    padButton.setAlpha(0.4f);
                }
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                popupPad_startstopbutton.setEnabled(true);
                popupPad_stoporstart = "stop";
                padson = false;
            }
        }
    }

    private void playPads(View view) throws IllegalStateException {
        pad_filename = "null";

        // Determine the key of the song and therefore which auto pad track to use
        ProcessSong.processKey();

        int path = -1;

        // validlinkaudio is true for valid link files and mPadFile=link, always false otherwise
        boolean validlinkaudio = false;
        MediaPlayer testMPlayer = new MediaPlayer();
        if (mPadFile.equals(getResources().getString(R.string.link_audio)) && !mLinkAudio.isEmpty() && !mLinkAudio.equals("")) {
            try {
                testMPlayer.setDataSource(this, Uri.parse(mLinkAudio));
                testMPlayer.prepare();
                validlinkaudio = true;
            } catch (Exception e) {
                validlinkaudio = false;
            }
        }
        testMPlayer.reset();
        testMPlayer.release();

        // off
        if (mPadFile.equals(getResources().getString(R.string.off)) && padson) {
            // Pad shouldn't play
            popupPad_stoporstart = "stop";
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            popupPad_startstopbutton.setEnabled(true);
            padson = false;
        }

        // linkaudio + notvalid
        if (mPadFile.equals(getResources().getString(R.string.link_audio)) && !validlinkaudio && padson) {
            // Problem with link audio so don't use it
            myToastMessage = getResources().getString(R.string.link_audio) + " - " + getResources().getString(R.string.file_type_unknown);
            ShowToast.showToast(FullscreenActivity.this);
            popupPad_stoporstart = "stop";
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            popupPad_startstopbutton.setEnabled(true);
            padson = false;
        }

        // pads + notvalid
        if (!mPadFile.equals(getResources().getString(R.string.off)) &&
                !mPadFile.equals(getResources().getString(R.string.link_audio)) &&
                pad_filename.equals("null") && padson) {
            // No key specified in the song - play nothing
            myToastMessage = getResources().getString(R.string.pad_error);
            ShowToast.showToast(FullscreenActivity.this);
            popupPad_stoporstart = "stop";
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            popupPad_startstopbutton.setEnabled(true);
            padson = false;
        }

        // pads + valid
        if (!mPadFile.equals(getResources().getString(R.string.off)) &&
                !mPadFile.equals(getResources().getString(R.string.link_audio)) &&
                !pad_filename.equals("null") && padson) {
            path = getResources().getIdentifier(pad_filename, "raw", getPackageName());
        }

        // If both players are playing - currently in the process of a fade out of both players!
        if (mPlayer1 != null && mPlayer2 != null && padson) {
            if (mPlayer1.isPlaying()) {
                if (isfading1) {
                    killfadeout1 = true;
                    if (mtask_fadeout_music1 != null) {
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
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
            popupPad_startstopbutton.setEnabled(true);
            popupPad_stoporstart = "start";

            if (mPadFile.equals(getResources().getString(R.string.link_audio)) && validlinkaudio) {
                try {
                    mPlayer1 = new MediaPlayer();
                    mPlayer1.setDataSource(this, Uri.parse(mLinkAudio));
                    mPlayer1.prepareAsync();
                } catch (Exception e) {
                }
            } else {
                try {
                    mPlayer1 = MediaPlayer.create(this, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mPlayer1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer1.setLooping(true);
                    switch (padpan) {
                        case "left":
                            mPlayer1.setVolume(padvol, 0.0f);
                            break;
                        case "right":
                            mPlayer1.setVolume(0.0f, padvol);
                            break;
                        default:
                            mPlayer1.setVolume(padvol, padvol);
                            break;
                    }
                    mPlayer1.start();
                    padson = true;
                }
            });

            // Now kill the 2nd player if still needed
            if (mPlayer2 != null && mPlayer2.isPlaying()) {
                if (isfading2) {
                    killfadeout2 = true;
                    if (mtask_fadeout_music2 != null) {
                        mtask_fadeout_music2 = null;
                    }
                    killPad2(padButton);
                }
            }
            isfading2 = false;
            fadeout2 = false;
            killfadeout2 = false;

        } else if (mPlayer1 != null && padson) {
            // We need to fade out mPlayer1 and start playing mPlayer2
            if (mPlayer1.isPlaying()) {
                // We need to fade this out over the next 8 seconds, then load and start mPlayer2
                // After 10seconds, release mPlayer1
                whichtofadeout = 1;
                fadeout1 = true;
                fadeOutBackgroundMusic1();
            }
            if (mPadFile.equals(getResources().getString(R.string.link_audio)) && validlinkaudio) {
                try {
                    mPlayer2 = new MediaPlayer();
                    mPlayer2.setDataSource(this, Uri.parse(mLinkAudio));
                    mPlayer2.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    mPlayer2 = MediaPlayer.create(this, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mPlayer2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer2.setLooping(true);
                    switch (padpan) {
                        case "left":
                            mPlayer2.setVolume(padvol, 0.0f);
                            break;
                        case "right":
                            mPlayer2.setVolume(0.0f, padvol);
                            break;
                        default:
                            mPlayer2.setVolume(padvol, padvol);
                            break;
                    }
                    mPlayer2.start();
                    padson = true;
                }
            });


        } else if (mPlayer2 != null && padson) {
            if (mPlayer2.isPlaying()) {
                // We need to fade this out over the next 8 seconds, then load and start mPlayer1
                // After 10seconds, release mPlayer1
                whichtofadeout = 2;
                fadeout2 = true;
                fadeOutBackgroundMusic2();
            }

            if (mPadFile.equals(getResources().getString(R.string.link_audio)) && validlinkaudio) {
                try {
                    mPlayer1 = new MediaPlayer();
                    mPlayer1.setDataSource(this, Uri.parse(mLinkAudio));
                    mPlayer1.prepareAsync();

                } catch (Exception e) {
                }
            } else {
                try {
                    mPlayer1 = MediaPlayer.create(this, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mPlayer1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer1.setLooping(true);
                    switch (padpan) {
                        case "left":
                            mPlayer1.setVolume(padvol, 0.0f);
                            break;
                        case "right":
                            mPlayer1.setVolume(0.0f, padvol);
                            break;
                        default:
                            mPlayer1.setVolume(padvol, padvol);
                            break;
                    }
                    mPlayer1.start();
                    padson = true;
                }
            });

        } else {
            // Nothing was playing already, so start mPlayer 1

            if (mPadFile.equals(getResources().getString(R.string.link_audio)) && validlinkaudio && padson) {
                try {
                    mPlayer1 = new MediaPlayer();
                    mPlayer1.setDataSource(this, Uri.parse(mLinkAudio));
                    mPlayer1.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    mPlayer1 = MediaPlayer.create(this, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mPlayer1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer1.setLooping(true);
                    switch (padpan) {
                        case "left":
                            mPlayer1.setVolume(padvol, 0.0f);
                            break;
                        case "right":
                            mPlayer1.setVolume(0.0f, padvol);
                            break;
                        default:
                            mPlayer1.setVolume(padvol, padvol);
                            break;
                    }
                    mPlayer1.start();
                    padson = true;
                }
            });
        }
    }

    private void fadeOutBackgroundMusic1() {
        killfadeout1 = false;
        isfading1 = true;
        mtask_fadeout_music1 = (FadeOutMusic1) new FadeOutMusic1().execute();
    }

    private void fadeOutBackgroundMusic2() {
        killfadeout2 = false;
        isfading2 = true;
        mtask_fadeout_music2 = (FadeOutMusic2) new FadeOutMusic2().execute();
    }

    private class FadeOutMusic1 extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... args) {
            float level1 = padvol;
            int i = 1;
            isfading1 = true;
            while (i < 50 && !orientationchanged && !killfadeout1) {
                i++;
                level1 = level1 * 0.9f;
                float leftVol1 = level1;
                float rightVol1 = level1;
                if (padpan.equals("left")) {
                    rightVol1 = 0.0f;
                } else if (padpan.equals("right")) {
                    leftVol1 = 0.0f;
                }
                try {
                    if (mPlayer1 != null) {
                        mPlayer1.setVolume(leftVol1, rightVol1);
                    }
                } catch (Exception e) {
                    // Problem!
                    e.printStackTrace();
                }

                // Pause before next fade increment
                long nowtime = System.currentTimeMillis();
                long thentime = nowtime + crossFadeTime / 50;
                while (System.currentTimeMillis() < thentime) {
                    // Do nothing......
                }
            }
            return "dummy";
        }

        @Override
        @SuppressWarnings("deprecation")
        protected void onPostExecute(String dummy) {
            isfading1 = false;
            fadeout1 = false;
            try {
                killPad1(view);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mtask_fadeout_music1 != null) {
                mtask_fadeout_music1 = null;
            }

            if (mPlayer1 == null && mPlayer2 == null) {
                popupPad_stoporstart = "stop";
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                popupPad_stoporstart = "stop";
                padson = false;
            }
        }

        @Override
        public void onCancelled() {
            fadeout1 = false;
            try {
                killPad1(padButton);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mtask_fadeout_music1 != null) {
                mtask_fadeout_music1 = null;
            }
            if (mPlayer1 == null && mPlayer2 == null) {
                popupPad_stoporstart = "stop";
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                popupPad_stoporstart = "stop";
                padson = false;

            }
        }
    }

    private class FadeOutMusic2 extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... args) {
            float level2 = padvol;
            int i = 1;
            isfading2 = true;
            while (i < 50 && !orientationchanged && !killfadeout2) {
                i++;
                level2 = level2 * 0.9f;
                float leftVol2 = level2;
                float rightVol2 = level2;
                if (padpan.equals("left")) {
                    rightVol2 = 0.0f;
                } else if (padpan.equals("right")) {
                    leftVol2 = 0.0f;
                }
                try {
                    if (mPlayer2 != null) {
                        mPlayer2.setVolume(leftVol2, rightVol2);
                    }
                } catch (Exception e) {
                    // Problem!
                    e.printStackTrace();
                }

                // Pause before next fade increment
                long nowtime = System.currentTimeMillis();
                long thentime = nowtime + crossFadeTime / 50;
                while (System.currentTimeMillis() < thentime) {
                    // Do nothing......
                }
            }
            return "dummy";
        }

        @Override
        protected void onPostExecute(String dummy) {
            isfading2 = false;
            fadeout2 = false;
            try {
                killPad2(view);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mtask_fadeout_music2 != null) {
                mtask_fadeout_music2 = null;
            }

            if (mPlayer1 == null && mPlayer2 == null) {
                popupPad_stoporstart = "stop";
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                padson = false;
                popupPad_stoporstart = "stop";
            }
        }

        @Override
        public void onCancelled() {
            fadeout2 = false;
            try {
                killPad2(padButton);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mtask_fadeout_music2 != null) {
                mtask_fadeout_music2 = null;
            }
            if (mPlayer1 == null && mPlayer2 == null) {
                popupPad_stoporstart = "stop";
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                padson = false;
                popupPad_stoporstart = "stop";
            }
        }
    }

    public void editNotes(View view) {
        // This calls the edit song sticky notes dialogue
        // Once user clicks OK, the notes are saved to the song.

        whattodo = "editnotes";
        newFragment = PopUpEditStickyFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void tapTempo(View view) {
        // This function checks the previous tap_tempo time and calculates the bpm
        new_time = System.currentTimeMillis();
        time_passed = new_time - old_time;
        calc_bpm = Math.round((1 / ((float) time_passed / 1000)) * 60);
        if (time_passed < 2000) {
            total_calc_bpm += calc_bpm;
            total_counts++;
        } else {
            // Waited too long, reset count
            total_calc_bpm = 0;
            total_counts = 0;
        }

        av_bpm = Math.round((float) total_calc_bpm / (float) total_counts);

        if (av_bpm < 200 && av_bpm >= 39) {
            String text = "" + av_bpm;
            popupMetronome_tempo_text.setText(text);
            mTempo = "" + av_bpm;
            popupMetronome_tempo.setProgress(av_bpm - 39);
        }
        old_time = new_time;
    }

    private void hidepagebuttons() {
        delaycheckscroll.removeCallbacks(checkScrollPosition);
        wasshowing_pdfselectpage = pdf_selectpage.getVisibility();
        wasshowing_stickynotes = stickynotes.getVisibility();
        scrollstickyholder.setVisibility(View.GONE);

        findViewById(R.id.bottombar).setVisibility(View.GONE);
        findViewById(R.id.rightbar).setVisibility(View.GONE);
        uparrow_top.setVisibility(View.GONE);
        uparrow_bottom.setVisibility(View.GONE);
        downarrow_bottom.setVisibility(View.GONE);
        downarrow_top.setVisibility(View.GONE);
        playbackProgress.setVisibility(View.GONE);
    }

    private void showpagebuttons() {
        // if PDF and page count is bigger than 1, show pdf button
        if (isPDF && pdfPageCount > 1 && togglePageButtons.equals("Y")) {
            pdf_selectpage.setVisibility(View.VISIBLE);
        } else {
            pdf_selectpage.setVisibility(View.INVISIBLE);
        }
        // If song and sticky note exists, show it
        if (!isPDF && isSong && togglePageButtons.equals("Y") && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerOpen(expListViewSong)) {
            stickynotes.setVisibility(View.VISIBLE);
            autoscrollButton.setVisibility(View.VISIBLE);
            metronomeButton.setVisibility(View.VISIBLE);
            padButton.setVisibility(View.VISIBLE);
            linkButton.setVisibility(View.VISIBLE);
            chordButton.setVisibility(View.VISIBLE);
            if (autoscrollactivated) {
                playbackProgress.setVisibility(View.VISIBLE);
            }
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
    }

    public void prepareSongMenu() {
        // Initialise Songs menu
        listDataHeaderSong = new ArrayList<>();
        listDataChildSong = new HashMap<>();

        // Get song folders
        ListSongFiles.listSongFolders();
        listDataHeaderSong.add(getResources().getString(R.string.mainfoldername));
        if (mSongFolderNames == null) {
            mSongFolderNames = new String[1];
            mSongFolderNames[0] = "";
        }
        if (childSongs == null) {
            childSongs = new String[1][1];
            childSongs[0][0] = "";
        }
        listDataHeaderSong.addAll(Arrays.asList(mSongFolderNames).subList(0, mSongFolderNames.length - 1));

        for (int s = 0; s < mSongFolderNames.length; s++) {
            List<String> song_folders = new ArrayList<>();
            Collections.addAll(song_folders, childSongs[s]);
            listDataChildSong.put(listDataHeaderSong.get(s), song_folders);
        }

        listAdapterSong = new ExpandableListAdapter(expListViewSong, FullscreenActivity.this, listDataHeaderSong, listDataChildSong);
        expListViewSong.setAdapter(listAdapterSong);

        listAdapterSong.notifyDataSetInvalidated();
        listAdapterSong.notifyDataSetChanged();

        // Listen for song folders being opened/expanded
        expListViewSong.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != lastExpandedGroupPositionSong) {
                    expListViewSong.collapseGroup(lastExpandedGroupPositionSong);
                }
                lastExpandedGroupPositionSong = groupPosition;
                listAdapterSong.notifyDataSetInvalidated();
                listAdapterSong.notifyDataSetChanged();
                // If the options menu is open, close it
                if (mDrawerLayout.isDrawerOpen(expListViewOption)) {
                    mDrawerLayout.closeDrawer(expListViewOption);
                }
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

                    // If the song is in .pro, .onsong, .txt format, tell the user to convert it first
                    // This is done by viewing it (avoids issues with file extension renames)

                    // Just in case users running older than lollipop, we don't want to open the file
                    // In this case, store the current song as a string so we can go back to it
                    String currentsong = songfilename;
                    String currentfolder = whichSongFolder;

                    songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

                    // If the song is in .pro, .onsong, .txt format, tell the user to convert it first
                    // This is done by viewing it (avoids issues with file extension renames)
                    if (songfilename.toLowerCase(locale).endsWith(".pro") ||
                            songfilename.toLowerCase(locale).endsWith(".chopro") ||
                            songfilename.toLowerCase(locale).endsWith(".cho") ||
                            songfilename.toLowerCase(locale).endsWith(".chordpro") ||
                            songfilename.toLowerCase(locale).endsWith(".onsong") ||
                            songfilename.toLowerCase(locale).endsWith(".txt")) {

                        // Don't add song yet, but tell the user
                        myToastMessage = getResources().getString(R.string.convert_song);
                        ShowToast.showToast(FullscreenActivity.this);
                    } else {

                        if (listDataHeaderSong.get(groupPosition).equals(mainfoldername)) {
                            whichSongFolder = mainfoldername;
                            whatsongforsetwork = "$**_" + songfilename + "_**$";
                        } else {
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

                        // If the user isn't running lollipop and they've added a pdf - don't open it
                        if (currentapiVersion < Build.VERSION_CODES.LOLLIPOP) {
                            songfilename = currentsong;
                            whichSongFolder = currentfolder;
                        }

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
                        }, 1000);
                    }
                }
                return false;
            }
        });

        // Try to open the appropriate Song folder on the left menu
        expListViewSong.expandGroup(0);
        for (int z = 0; z < listDataHeaderSong.size() - 1; z++) {
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

                // If the options menu is open, close it
                if (mDrawerLayout.isDrawerOpen(expListViewOption)) {
                    mDrawerLayout.closeDrawer(expListViewOption);
                }

                if (!addingtoset) {
                    // Set the appropriate folder name

                    if (listDataHeaderSong.get(groupPosition).equals(mainfoldername)) {
                        whichSongFolder = mainfoldername;
                    } else {
                        whichSongFolder = listDataHeaderSong.get(groupPosition);
                    }
                    // Set the appropriate song filename
                    songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

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

    private void prepareOptionMenu() {
        // preparing list data
        listDataHeaderOption = new ArrayList<>();
        listDataChildOption = new HashMap<>();

        // Adding headers for option menu data
        listDataHeaderOption.add(getResources().getString(R.string.options_set).toUpperCase(locale));
        listDataHeaderOption.add(getResources().getString(R.string.options_song).toUpperCase(locale));
        listDataHeaderOption.add(getResources().getString(R.string.chords).toUpperCase(locale));
        listDataHeaderOption.add(getResources().getString(R.string.options_display).toUpperCase(locale));
        listDataHeaderOption.add(getResources().getString(R.string.options_gesturesandmenus).toUpperCase(locale));
        listDataHeaderOption.add(getResources().getString(R.string.options_storage).toUpperCase(locale));
        listDataHeaderOption.add(getResources().getString(R.string.pad).toUpperCase(locale));
        listDataHeaderOption.add(getResources().getString(R.string.autoscroll).toUpperCase(locale));
        listDataHeaderOption.add(getResources().getString(R.string.options_other).toUpperCase(locale));

        // Adding child data
        List<String> options_set = new ArrayList<>();
        options_set.add(getResources().getString(R.string.options_set_load));
        options_set.add(getResources().getString(R.string.options_set_save));
        options_set.add(getResources().getString(R.string.options_set_clear));
        options_set.add(getResources().getString(R.string.options_set_delete));
        options_set.add(getResources().getString(R.string.options_set_export));
        options_set.add(getResources().getString(R.string.add_custom_slide).toUpperCase(locale));
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

        for (int r = 0; r < mSetList.length; r++) {
            mSetList[r] = mSetList[r].replace("$**_", "");
            mSetList[r] = mSetList[r].replace("_**$", "");
            if (!mSetList[r].isEmpty()) {
                options_set.add(mSetList[r]);
            }
        }

        List<String> options_song = new ArrayList<>();
        options_song.add(getResources().getString(R.string.options_song_edit));
        options_song.add(getResources().getString(R.string.options_song_stickynotes));
        options_song.add(getResources().getString(R.string.options_song_rename));
        options_song.add(getResources().getString(R.string.options_song_delete));
        options_song.add(getResources().getString(R.string.options_song_new));
        options_song.add(getResources().getString(R.string.options_song_export));

        List<String> options_chords = new ArrayList<>();
        options_chords.add(getResources().getString(R.string.options_song_transpose));
        options_chords.add(getResources().getString(R.string.capo_toggle));
        options_chords.add(getResources().getString(R.string.options_song_sharp));
        options_chords.add(getResources().getString(R.string.options_song_flat));
        options_chords.add(getResources().getString(R.string.options_options_chordformat));
        options_chords.add(getResources().getString(R.string.options_song_convert));
        options_chords.add(getResources().getString(R.string.options_options_showchords));

        List<String> options_display = new ArrayList<>();
        options_display.add(getResources().getString(R.string.options_options_theme));
        options_display.add(getResources().getString(R.string.options_options_scale));
        options_display.add(getResources().getString(R.string.options_options_fontsize));
        options_display.add(getResources().getString(R.string.options_options_colors));
        options_display.add(getResources().getString(R.string.options_options_fonts));
        options_display.add(getResources().getString(R.string.shownextinset));
        options_song.add(getResources().getString(R.string.edit_song_presentation));
        options_display.add(getResources().getString(R.string.scrollbuttons_toggle));
        options_display.add(getResources().getString(R.string.songbuttons_toggle));
        options_display.add(getResources().getString(R.string.toggle_autoshow_stickynotes));
        options_display.add(getResources().getString(R.string.options_options_hidebar));

        List<String> options_gesturesandmenus = new ArrayList<>();
        options_gesturesandmenus.add(getResources().getString(R.string.options_options_pedal));
        options_gesturesandmenus.add(getResources().getString(R.string.options_options_gestures));
        options_gesturesandmenus.add(getResources().getString(R.string.options_options_menuswipe));
        options_gesturesandmenus.add(getResources().getString(R.string.options_options_songswipe));

        List<String> options_storage = new ArrayList<>();
        options_storage.add(getResources().getString(R.string.options_song_newfolder));
        options_storage.add(getResources().getString(R.string.options_song_editfolder));
        options_storage.add(getResources().getString(R.string.storage_choose));
        options_storage.add(getResources().getString(R.string.import_onsong_choose));
        options_storage.add(getResources().getString(R.string.refreshsongs));
        options_storage.add(getResources().getString(R.string.search_rebuild));
        options_storage.add(getResources().getString(R.string.search_log));

        List<String> options_pad = new ArrayList<>();
        options_pad.add(getResources().getString(R.string.crossfade_time));

        List<String> options_autoscroll = new ArrayList<>();
        options_autoscroll.add(getResources().getString(R.string.default_autoscroll));
        options_autoscroll.add(getResources().getString(R.string.options_options_autostartscroll));

        List<String> options_other = new ArrayList<>();
        options_other.add(getResources().getString(R.string.options_options_help));
        options_other.add("@OpenSongApp");
        options_other.add(getResources().getString(R.string.language));
        options_other.add(getResources().getString(R.string.options_options_start));
        options_other.add(getResources().getString(R.string.rate));

        listDataChildOption.put(listDataHeaderOption.get(0), options_set); // Header, Child data
        listDataChildOption.put(listDataHeaderOption.get(1), options_song);
        listDataChildOption.put(listDataHeaderOption.get(2), options_chords);
        listDataChildOption.put(listDataHeaderOption.get(3), options_display);
        listDataChildOption.put(listDataHeaderOption.get(4), options_gesturesandmenus);
        listDataChildOption.put(listDataHeaderOption.get(5), options_storage);
        listDataChildOption.put(listDataHeaderOption.get(6), options_pad);
        listDataChildOption.put(listDataHeaderOption.get(7), options_autoscroll);
        listDataChildOption.put(listDataHeaderOption.get(8), options_other);

        listAdapterOption = new ExpandableListAdapterOptions(expListViewOption, this, listDataHeaderOption, listDataChildOption);

        // setting list adapter
        expListViewOption.setAdapter(listAdapterOption);

        // Listen for options menus being expanded (close the others and keep a note that this one is open)
        expListViewOption.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != lastExpandedGroupPositionOption) {
                    expListViewOption.collapseGroup(lastExpandedGroupPositionOption);
                }
                lastExpandedGroupPositionOption = groupPosition;
                // If the songs menu is open, close it
                if (mDrawerLayout.isDrawerOpen(expListViewSong)) {
                    mDrawerLayout.closeDrawer(expListViewSong);
                }
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

                    if (myOptionListClickedItem > 7 && groupPosition == 0) {
                        // Long clicking on the 8th or later options will remove the
                        // song from the set (all occurrences)
                        // Remove this song from the set. Remember it has tags at the start and end
                        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vb.vibrate(25);

                        // Take away the menu items (8)
                        String tempSong = mSetList[childPosition - 8];
                        mSetList[childPosition - 8] = "";

                        if (indexSongInSet == (childPosition - 8)) {
                            setView = "N";
                        }

                        mySet = "";
                        for (String aMSetList : mSetList) {
                            if (!aMSetList.isEmpty()) {
                                mySet = mySet + "$**_" + aMSetList + "_**$";
                            }
                        }

                        // Tell the user that the song has been removed.
                        myToastMessage = "\"" + tempSong + "\" "
                                + getResources().getString(R.string.removedfromset);
                        ShowToast.showToast(FullscreenActivity.this);

                        //Check to see if our set list is still valid
                        SetActions.prepareSetList();
                        showCurrentSet(view);
                        fixSetActionButtons();
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

                // If the songs menu is open, close it
                if (mDrawerLayout.isDrawerOpen(expListViewSong)) {
                    mDrawerLayout.closeDrawer(expListViewSong);
                }

                if (!removingfromset) {
                    String chosenMenu = listDataHeaderOption.get(groupPosition);

                    // Build a dialogue window and related bits that get modified/shown if needed
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FullscreenActivity.this);
                    LinearLayout titleLayout = new LinearLayout(FullscreenActivity.this);
                    titleLayout.setOrientation(LinearLayout.VERTICAL);
                    TextView m_titleView = new TextView(FullscreenActivity.this);
                    m_titleView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    m_titleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
                    m_titleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white));
                    m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                    if (chosenMenu.equals(getResources().getString(R.string.options_set).toUpperCase(locale))) {

                        // Load up a list of saved sets as it will likely be needed
                        SetActions.updateOptionListSets();
                        Arrays.sort(mySetsFiles);
                        Arrays.sort(mySetsDirectories);
                        Arrays.sort(mySetsFileNames);
                        Arrays.sort(mySetsFolderNames);

                        mDrawerLayout.closeDrawer(expListViewOption);

                        // First up check for set options clicks
                        if (childPosition == 0) {
                            // Load a set
                            whattodo = "loadset";
                            newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 1) {
                            // Save current set
                            whattodo = "saveset";
                            newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 2) {
                            // Clear current set
                            whattodo = "clearset";
                            String message = getResources().getString(R.string.options_clearthisset);
                            newFragment = PopUpAreYouSureFragment.newInstance(message);
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 3) {
                            // Delete saved set
                            whattodo = "deleteset";
                            newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 4) {
                            // Export current set
                            whattodo = "exportset";
                            newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 5) {
                            // Add a custom slide
                            newFragment = PopUpCustomSlideFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 6) {
                            // Edit current set
                            mDrawerLayout.closeDrawer(expListViewOption);
                            FullscreenActivity.whattodo = "editset";
                            newFragment = PopUpSetViewNew.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition > 7) {
                            // Load song in set
                            setView = "Y";
                            pdfPageCurrent = 0;
                            // Set item is 8 less than childPosition
                            indexSongInSet = childPosition - 8;
                            String linkclicked = mSetList[indexSongInSet];
                            if (linkclicked == null) {
                                linkclicked = "";
                            }
                            if (indexSongInSet == 0) {
                                // Already first item
                                previousSongInSet = "";
                            } else {
                                previousSongInSet = mSetList[indexSongInSet - 1];
                            }

                            if (indexSongInSet == (setSize - 1)) {
                                // Last item
                                nextSongInSet = "";
                            } else {
                                nextSongInSet = mSetList[indexSongInSet + 1];
                            }
                            whichDirection = "R2L";
                            invalidateOptionsMenu();

                            if (!linkclicked.contains("/")) {
                                // Right it doesn't, so add the /
                                linkclicked = "/" + linkclicked;
                            }

                            // Now split the linkclicked into two song parts 0=folder 1=file
                            String[] songpart = linkclicked.split("/");

                            if (songpart.length < 2) {
                                songpart = new String[2];
                                songpart[0] = "";
                                songpart[1] = "";
                            }


                            // If the folder length isn't 0, it is a folder
                            if (songpart[0].length() > 0 && !songpart[0].contains(text_scripture) && !songpart[0].contains(image) && !songpart[0].contains(text_slide) && !songpart[0].contains(text_note)) {
                                whichSongFolder = songpart[0];

                            } else if (songpart[0].length() > 0 && songpart[0].contains(text_scripture)) {
                                whichSongFolder = "../Scripture/_cache";
                                songpart[0] = "../Scripture/_cache";

                            } else if (songpart[0].length() > 0 && songpart[0].contains(text_slide)) {
                                whichSongFolder = "../Slides/_cache";
                                songpart[0] = "../Slides/_cache";

                            } else if (songpart[0].length() > 0 && songpart[0].contains(text_note)) {
                                whichSongFolder = "../Notes/_cache";
                                songpart[0] = "../Notes/_cache";

                            } else if (songpart[0].length() > 0 && songpart[0].contains(image)) {
                                whichSongFolder = "../Images/_cache";
                                songpart[0] = "../Images/_cache";

                            } else {
                                whichSongFolder = mainfoldername;
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

                    } else if (chosenMenu.equals(getResources().getString(R.string.options_song).toUpperCase(locale))) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);
                        mDrawerLayout.closeDrawer(expListViewOption);
                        // Now check for song options clicks

                        // 0 = Edit song
                        // 1 = Edit sticky notes
                        // 2 = Rename song
                        // 3 = Delete song
                        // 4 = New song
                        // 5 = Export song
                        // 6 = Presentation order

                        if (childPosition == 0) {//Edit song
                            if (isPDF) {// Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else if (!isSong) {// Editing a slide / note / scripture / image
                                myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                whattodo = "editsong";
                                newFragment = PopUpEditSongFragment.newInstance();
                                newFragment.show(getFragmentManager(), "dialog");
                            }

                        } else if (childPosition == 1) {// Edit sticky notes
                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else if (!isSong) {
                                // Editing a slide / note / scripture / image
                                myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                whattodo = "editnotes";
                                newFragment = PopUpEditStickyFragment.newInstance();
                                newFragment.show(getFragmentManager(), "dialog");
                            }

                        } else if (childPosition == 2) {// Rename song
                            if (!isPDF && !isSong) {
                                // Editing a slide / note / scripture / image
                                myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                whattodo = "renamesong";
                                newFragment = PopUpSongRenameFragment.newInstance();
                                newFragment.show(getFragmentManager(), "dialog");
                            }


                        } else if (childPosition == 3) {// Delete song
                            if (!isPDF && !isSong) {
                                // Editing a slide / note / scripture / image
                                myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                // Delete
                                // Give the user an are you sure prompt!
                                whattodo = "deletesong";
                                String message = getResources().getString(R.string.options_song_delete) +
                                        " \"" + songfilename + "\"?";
                                newFragment = PopUpAreYouSureFragment.newInstance(message);
                                newFragment.show(getFragmentManager(), "dialog");
                            }


                        } else if (childPosition == 4) {// New song
                            whattodo = "createsong";
                            newFragment = PopUpSongCreateFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");


                        } else if (childPosition == 5) {// Export song
                            shareSong();

                        } else if (childPosition == 6) {// Presentation order
                            if (usePresentationOrder) {
                                usePresentationOrder = false;
                                myToastMessage = getResources().getString(R.string.edit_song_presentation) + " - "
                                        + getResources().getString(R.string.off);
                            } else {
                                usePresentationOrder = true;
                                myToastMessage = getResources().getString(R.string.edit_song_presentation) + " - "
                                        + getResources().getString(R.string.on);
                            }
                            ShowToast.showToast(FullscreenActivity.this);
                            Preferences.savePreferences();
                            redrawTheLyricsTable(main_page);
                        }

                    } else if (chosenMenu.equals(getResources().getString(R.string.chords).toUpperCase(locale))) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);
                        mDrawerLayout.closeDrawer(expListViewOption);
                        // Now check for chord options clicks

                        // 0 = Transpose
                        // 1 = Capo toggle
                        // 2 = Use # chords
                        // 3 = Use b chords
                        // 4 = Choose chord format
                        // 5 = Convert to preferred chord format
                        // 6 = Show/hide chords

                        if (childPosition == 0) {// Transpose
                            // First ask the user how many steps to transpose by
                            // Default is +1

                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else if (!isSong) {
                                // Editing a slide / note / scripture / image
                                myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                transposeDirection = "+1";
                                transposeTimes = 1;
                                Transpose.checkChordFormat();
                                whattodo = "transpose";
                                newFragment = PopUpTransposeFragment.newInstance();
                                newFragment.show(getFragmentManager(), "dialog");
                            }


                        } else if (childPosition == 1) {// Capo toggle
                            switch (capoDisplay) {
                                case "both":
                                    capoDisplay = "capoonly";
                                    myToastMessage = getResources().getString(R.string.capo_toggle_onlycapo);
                                    break;
                                case "capoonly":
                                    capoDisplay = "native";
                                    myToastMessage = getResources().getString(R.string.capo_toggle_native);
                                    break;
                                default:
                                    capoDisplay = "both";
                                    myToastMessage = getResources().getString(R.string.capo_toggle_bothcapo);
                                    break;
                            }
                            ShowToast.showToast(FullscreenActivity.this);
                            Preferences.savePreferences();
                            redrawTheLyricsTable(main_page);


                        } else if (childPosition == 2) {// Use # chords
                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else if (!isSong) {
                                // Editing a slide / note / scripture / image
                                myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                transposeStyle = "sharps";
                                transposeDirection = "0";
                                switchsharpsflats = true;
                                Transpose.checkChordFormat();
                                try {
                                    Transpose.doTranspose();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                switchsharpsflats = false;
                                redrawTheLyricsTable(main_page);
                            }

                        } else if (childPosition == 3) {// Use b chords
                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else if (!isSong) {
                                // Editing a slide / note / scripture / image
                                myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                transposeStyle = "flats";
                                transposeDirection = "0";
                                switchsharpsflats = true;
                                Transpose.checkChordFormat();
                                try {
                                    Transpose.doTranspose();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                switchsharpsflats = false;
                                redrawTheLyricsTable(main_page);
                            }

                        } else if (childPosition == 4) {// Choose chord format
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, ChordFormat.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();

                        } else if (childPosition == 5) {// Convert to preferred chord format
                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else if (!isSong) {
                                // Editing a slide / note / scripture / image
                                myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                transposeDirection = "0";
                                chord_converting = "Y";
                                Transpose.checkChordFormat();
                                try {
                                    Transpose.doTranspose();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                redrawTheLyricsTable(main_page);
                            }


                        } else if (childPosition == 6) {// Show/hide chords
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
                        }

                    } else if (chosenMenu.equals(getResources().getString(R.string.options_display).toUpperCase(locale))) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);
                        mDrawerLayout.closeDrawer(expListViewOption);
                        // Now check for display options clicks

                        // 0 = Change theme
                        // 1 = Toggle autoscale
                        // 2 = Change font size
                        // 3 = Colour
                        // 4 = Fonts
                        // 5 = Show next
                        // 6 = Toggle scroll buttons
                        // 7 = Toggle song buttons
                        // 8 = Toggle autosticky
                        // 9 = Show/hide menu bar

                        if (childPosition == 0) {// Change theme
                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {

                                myToastMessage = "";
                                switch (mDisplayTheme) {
                                    case "Theme_Holo_Light":
                                    case "Theme.Holo.Light":
                                        mDisplayTheme = "Theme_Holo";
                                        myToastMessage = getResources().getString(R.string.dark_theme);
                                        Preferences.savePreferences();
                                        break;
                                    case "Theme_Holo":
                                    case "Theme.Holo":
                                        mDisplayTheme = "custom1";
                                         myToastMessage = getResources().getString(R.string.custom1_theme);
                                        Preferences.savePreferences();
                                        break;
                                    case "custom1":
                                        mDisplayTheme = "custom2";
                                        myToastMessage = getResources().getString(R.string.custom2_theme);
                                        Preferences.savePreferences();
                                        break;
                                    case "custom2":
                                        mDisplayTheme = "Theme_Holo_Light";
                                        myToastMessage = getResources().getString(R.string.light_theme);
                                        Preferences.savePreferences();
                                        break;
                                }
                                SetUpColours.colours();
                                ShowToast.showToast(FullscreenActivity.this);
                                main_page = findViewById(R.id.main_page);
                                mDrawerLayout.openDrawer(expListViewOption);
                                redrawTheLyricsTable(view);
                            }


                        } else if (childPosition == 1) {// Toggle autoscale
                            switch (toggleYScale) {
                                case "Y":
                                    toggleYScale = "W";
                                    myToastMessage = getResources().getString(R.string.scaleY)
                                            + " " + getResources().getString(R.string.on_width);
                                    ShowToast.showToast(FullscreenActivity.this);
                                    break;
                                case "W":
                                    toggleYScale = "N";
                                    myToastMessage = getResources().getString(R.string.scaleY)
                                            + " " + getResources().getString(R.string.off);
                                    ShowToast.showToast(FullscreenActivity.this);
                                    break;
                                default:
                                    toggleYScale = "Y";
                                    myToastMessage = getResources().getString(R.string.scaleY)
                                            + " " + getResources().getString(R.string.on);
                                    ShowToast.showToast(FullscreenActivity.this);
                                    break;
                            }
                            Preferences.savePreferences();
                            redrawTheLyricsTable(view);


                        } else if (childPosition == 2) {// Change font size
                            if (isPDF) {
                                // Can't do this action on a pdf!
                                myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(FullscreenActivity.this);
                            } else {
                                final float storefontsize = mFontSize;
                                // Decide where the font size progress bar should be
                                if (mFontSize > 80) {
                                    mFontSize = 42;
                                }
                                fontsizeseekar = Math.round((((mFontSize) / 80) * 100));

                                dialogBuilder.setTitle(getResources().getText(R.string.options_options_fontsize).toString());

                                LinearLayout scalefont = new LinearLayout(FullscreenActivity.this);
                                scalefont.setOrientation(LinearLayout.VERTICAL);
                                final TextView text_size = new TextView(FullscreenActivity.this);
                                String text = "" + mFontSize;
                                text_size.setText(text);
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
                                        val = val / 100;
                                        val = val * 76;
                                        val = val * 10;
                                        val = Math.round(val);
                                        val = val / 10;
                                        mFontSize = 4 + val;
                                        String text = "" + mFontSize;
                                        text_size.setText(text);
                                        redrawTheLyricsTable(view);

                                    }

                                    public void onStartTrackingTouch(SeekBar seekBar) {
                                        // Do nothing
                                    }

                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        // Basic font size is 42sp - this is to be 50%
                                        // Min is 4sp

                                        fontsizeseekar = seekBar.getProgress();
                                        float val = fontsizeseekar;
                                        val = val / 100;
                                        val = val * 76;
                                        val = val * 10;
                                        val = Math.round(val);
                                        val = val / 10;
                                        mFontSize = 4 + val;
                                        String text = "" + mFontSize;
                                        text_size.setText(text);
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


                        } else if (childPosition == 3) {// Colour
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this,
                                    ChangeDisplayPreferences.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();


                        } else if (childPosition == 4) {// Fonts
                            whattodo = "changefonts";
                            newFragment = PopUpFontsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");


                        } else if (childPosition == 5) {// Show next
                            // Options are top, bottom, off

                            switch (showNextInSet) {
                                case "top":
                                    showNextInSet = "bottom";
                                    myToastMessage = getResources().getString(R.string.shownextinset)
                                            + " - " + getResources().getString(R.string.on)
                                            + " (" + getResources().getString(R.string.bottom).toUpperCase(locale) + ")";
                                    ShowToast.showToast(FullscreenActivity.this);
                                    break;
                                case "bottom":
                                    showNextInSet = "off";
                                    myToastMessage = getResources().getString(R.string.shownextinset)
                                            + " - " + getResources().getString(R.string.off);
                                    ShowToast.showToast(FullscreenActivity.this);
                                    break;
                                default:
                                    showNextInSet = "top";
                                    myToastMessage = getResources().getString(R.string.shownextinset)
                                            + " - " + getResources().getString(R.string.on)
                                            + " (" + getResources().getString(R.string.top).toUpperCase(locale) + ")";
                                    ShowToast.showToast(FullscreenActivity.this);
                                    break;
                            }
                            Preferences.savePreferences();
                            redrawTheLyricsTable(main_page);


                        } else if (childPosition == 6) {// Toggle scroll buttons
                            if (toggleScrollArrows.equals("S")) {
                                toggleScrollArrows = "D";
                                Preferences.savePreferences();
                                myToastMessage = getResources().getString(R.string.scrollbuttons_toggle) + " - " + getResources().getString(R.string.scrollbuttons_double);
                                ShowToast.showToast(FullscreenActivity.this);
                                redrawTheLyricsTable(view);
                            } else {
                                toggleScrollArrows = "S";
                                Preferences.savePreferences();
                                myToastMessage = getResources().getString(R.string.scrollbuttons_toggle) + " - " + getResources().getString(R.string.scrollbuttons_single);
                                ShowToast.showToast(FullscreenActivity.this);
                                redrawTheLyricsTable(view);
                            }


                        } else if (childPosition == 7) {// Toggle song buttons

                            if (pagebutton_position.equals("off")) {
                                // Turn them on and put them on the right
                                togglePageButtons = "Y";
                                pagebutton_position = "right";
                                Preferences.savePreferences();
                                String message = getResources().getString(R.string.songbuttons_toggle) +
                                        " - " + getResources().getString(R.string.right);
                                myToastMessage = message;

                            } else if (pagebutton_position.equals("right")) {
                                // Turn them on and put them on the bottom
                                togglePageButtons = "Y";
                                pagebutton_position = "bottom";
                                Preferences.savePreferences();
                                String message = getResources().getString(R.string.songbuttons_toggle) +
                                        " - " + getResources().getString(R.string.bottom);
                                myToastMessage = message;

                            } else {
                                // Turn them off
                                togglePageButtons = "N";
                                pagebutton_position = "off";
                                Preferences.savePreferences();
                                String message = getResources().getString(R.string.songbuttons_toggle) +
                                        " - " + getResources().getString(R.string.off);
                                myToastMessage = message;

                            }
                            ShowToast.showToast(FullscreenActivity.this);
                            setupPageButtons();

                        } else if (childPosition == 8) {// Toggle autosticky
                            switch (toggleAutoSticky) {
                                case "N":
                                    toggleAutoSticky = "Y";
                                    myToastMessage = getResources().getString(R.string.toggle_autoshow_stickynotes) + " - " + getResources().getString(R.string.on);
                                    break;
                                case "Y":
                                    toggleAutoSticky = "T";
                                    myToastMessage = getResources().getString(R.string.toggle_autoshow_stickynotes) + " - " + getResources().getString(R.string.top);
                                    break;
                                case "T":
                                    toggleAutoSticky = "B";
                                    myToastMessage = getResources().getString(R.string.toggle_autoshow_stickynotes) + " - " + getResources().getString(R.string.bottom);
                                    break;
                                default:
                                    // Keep sticky notes hidden in normal sticky note view
                                    toggleAutoSticky = "N";
                                    myToastMessage = getResources().getString(R.string.toggle_autoshow_stickynotes) + " - " + getResources().getString(R.string.off);
                                    break;
                            }
                            // Save preferences
                            Preferences.savePreferences();
                            ShowToast.showToast(FullscreenActivity.this);
                            redrawTheLyricsTable(view);


                        } else if (childPosition == 9) {// Show/hide menu bar
                            if (hideactionbaronoff.equals("Y")) {
                                hideactionbaronoff = "N";
                                myToastMessage = getResources()
                                        .getString(R.string.options_options_hidebar)
                                        + " - "
                                        + getResources().getString(R.string.off);
                                ShowToast.showToast(FullscreenActivity.this);

                                if (ab != null) {
                                    ab.show();
                                }

                            } else {
                                hideactionbaronoff = "Y";
                                myToastMessage = getResources()
                                        .getString(R.string.options_options_hidebar)
                                        + " - "
                                        + getResources().getString(R.string.on);
                                ShowToast.showToast(FullscreenActivity.this);

                                if (ab != null) {
                                    ab.hide();
                                }
                            }

                            Preferences.savePreferences();
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, FullscreenActivity.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();
                        }


                    } else if (chosenMenu.equals(getResources().getString(R.string.options_gesturesandmenus).toUpperCase(locale))) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);
                        mDrawerLayout.closeDrawer(expListViewOption);
                        // Now check for gestures and menus options clicks

                        // 0 = Assign pedals
                        // 1 = Custom gestures
                        // 2 = Toggle menu swipe on/off
                        // 3 = Toggle song swipe on/off

                        if (childPosition == 0) {// Assign pedals
                            whattodo = "footpedal";
                            newFragment = PopUpPedalsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");


                        } else if (childPosition == 1) {// Custom gestures
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, GestureOptions.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();


                        } else if (childPosition == 2) {// Toggle menu swipe on/off
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


                        } else if (childPosition == 3) {// Toggle song swipe on/off
                            switch (swipeSet) {
                                case "Y":
                                    swipeSet = "S";
                                    Preferences.savePreferences();
                                    myToastMessage = getResources()
                                            .getString(R.string.swipeSet)
                                            + " "
                                            + getResources().getString(R.string.on_set);
                                    ShowToast.showToast(FullscreenActivity.this);
                                    break;
                                case "S":
                                    swipeSet = "N";
                                    Preferences.savePreferences();
                                    myToastMessage = getResources()
                                            .getString(R.string.swipeSet)
                                            + " "
                                            + getResources().getString(R.string.off);
                                    ShowToast.showToast(FullscreenActivity.this);
                                    break;
                                default:
                                    swipeSet = "Y";
                                    Preferences.savePreferences();
                                    myToastMessage = getResources()
                                            .getString(R.string.swipeSet)
                                            + " "
                                            + getResources().getString(R.string.on);
                                    ShowToast.showToast(FullscreenActivity.this);
                                    break;
                            }


                        }


                    } else if (chosenMenu.equals(getResources().getString(R.string.options_storage).toUpperCase(locale))) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);
                        mDrawerLayout.closeDrawer(expListViewOption);
                        // Now check for storage options clicks

                        // 0 = Create new song folder
                        // 1 = Edit song folder name
                        // 2 = Manage storage
                        // 3 = Import OnSong
                        // 4 = Refresh songs menu
                        // 5 = Rebuild search index
                        // 6 = View search error log

                        if (childPosition == 0) {// Create new song folder
                            promptNewFolder();


                        } else if (childPosition == 1) {// Edit song folder name
                            editFolderName();


                        } else if (childPosition == 2) {// Manage storage
                            tryKillPads();
                            tryKillMetronome();
                            Intent intent_stop = new Intent();
                            intent_stop.setClass(FullscreenActivity.this, StorageChooser.class);
                            startActivity(intent_stop);
                            finish();


                        } else if (childPosition == 3) {// Import OnSong
                            onSongImport();


                        } else if (childPosition == 4) {// Refresh songs menu
                            prepareSongMenu();
                            mDrawerLayout.closeDrawer(expListViewOption);
                            mDrawerLayout.openDrawer(expListViewSong);


                        } else if (childPosition == 5) {// Rebuild song index
                            safetosearch = false;
                            SharedPreferences indexSongPreferences = getSharedPreferences("indexsongs",MODE_PRIVATE);
                            SharedPreferences.Editor editor_index = indexSongPreferences.edit();
                            editor_index.putBoolean("buildSearchIndex", true);
                            editor_index.apply();
                            Intent intentmain = new Intent();
                            intentmain.setClass(FullscreenActivity.this, FullscreenActivity.class);
                            startActivity(intentmain);
                            finish();

                        } else if (childPosition == 6) {// View search error log
                            whattodo = "errorlog";
                            newFragment = PopUpWebViewFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");


                        }


                    } else if (chosenMenu.equals(getResources().getString(R.string.pad).toUpperCase(locale))) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);
                        mDrawerLayout.closeDrawer(expListViewOption);
                        // Now check for pad options clicks

                        // 0 = Cross fade time

                        if (childPosition == 0) {// Crossfade time
                            whattodo = "crossfade";
                            newFragment = PopUpCrossFadeFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");


                        }


                    } else if (chosenMenu.equals(getResources().getString(R.string.autoscroll).toUpperCase(locale))) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);
                        mDrawerLayout.closeDrawer(expListViewOption);
                        // Now check for autoscroll options clicks

                        // 0 = Autoscroll defaults
                        // 1 = Autostart autoscroll

                        if (childPosition == 0) {// Autoscroll delay time
                            newFragment = PopUpAutoScrollDefaultsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");


                        } else if (childPosition == 1) {// Autostart autoscroll
                            if (autostartautoscroll) {
                                autostartautoscroll = false;
                                Preferences.savePreferences();
                                myToastMessage = getResources().getString(R.string.options_options_autostartscroll) + " - " + getResources().getString(R.string.off);
                                ShowToast.showToast(FullscreenActivity.this);
                                redrawTheLyricsTable(view);
                            } else {
                                autostartautoscroll = true;
                                Preferences.savePreferences();
                                myToastMessage = getResources().getString(R.string.options_options_autostartscroll) + " - " + getResources().getString(R.string.on);
                                ShowToast.showToast(FullscreenActivity.this);
                                redrawTheLyricsTable(view);
                            }


                        }


                    } else if (chosenMenu.equals(getResources().getString(R.string.options_other).toUpperCase(locale))) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);
                        mDrawerLayout.closeDrawer(expListViewOption);
                        // Now check for other options clicks

                        // 0 = Help
                        // 1 = Twitter
                        // 2 = Language
                        // 3 = Start/splash screen
                        // 4 = Rate this app

                        if (childPosition == 0) {// Help
                            String url = "https://sites.google.com/site/opensongtabletmusicviewer/home";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);


                        } else if (childPosition == 1) {// Twitter
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=opensongapp")));
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/opensongapp")));
                            }

                        } else if (childPosition == 2) {// Language
                            int positionselected = -1;

                            mDrawerLayout.closeDrawer(expListViewOption);
                            mDrawerLayout.closeDrawer(expListViewSong);
                            if (!languageToLoad.isEmpty()) {
                                switch (languageToLoad) {
                                    case "af":
                                        positionselected = 0;
                                        break;
                                    case "cz":
                                        positionselected = 1;
                                        break;
                                    case "de":
                                        positionselected = 2;
                                        break;
                                    case "en":
                                        positionselected = 3;
                                        break;
                                    case "es":
                                        positionselected = 4;
                                        break;
                                    case "fr":
                                        positionselected = 5;
                                        break;
                                    case "hu":
                                        positionselected = 6;
                                        break;
                                    case "it":
                                        positionselected = 7;
                                        break;
                                    case "ja":
                                        positionselected = 8;
                                        break;
                                    case "pl":
                                        positionselected = 9;
                                        break;
                                    case "pt":
                                        positionselected = 10;
                                        break;
                                    case "ru":
                                        positionselected = 11;
                                        break;
                                    case "sr":
                                        positionselected = 12;
                                        break;
                                    case "zh":
                                        positionselected = 13;
                                        break;
                                }
                            }
                            AlertDialog.Builder languageDialog = new AlertDialog.Builder(FullscreenActivity.this);
                            languageDialog.setTitle(getResources().getString(R.string.language))
                                    .setSingleChoiceItems(getResources().getStringArray(R.array.languagelist), positionselected, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            if (arg1 == 0) { //af
                                                tempLanguage = "af";
                                            } else if (arg1 == 1) { //cz
                                                tempLanguage = "cz";
                                            } else if (arg1 == 2) { //de
                                                tempLanguage = "de";
                                            } else if (arg1 == 3) { //en
                                                tempLanguage = "en";
                                            } else if (arg1 == 4) { //es
                                                tempLanguage = "es";
                                            } else if (arg1 == 5) { //fr
                                                tempLanguage = "fr";
                                            } else if (arg1 == 6) { //hu
                                                tempLanguage = "hu";
                                            } else if (arg1 == 7) { //it
                                                tempLanguage = "it";
                                            } else if (arg1 == 8) { //ja
                                                tempLanguage = "ja";
                                            } else if (arg1 == 9) { //pl
                                                tempLanguage = "pl";
                                            } else if (arg1 == 10) { //pt
                                                tempLanguage = "pt";
                                            } else if (arg1 == 11) { //ru
                                                tempLanguage = "ru";
                                            } else if (arg1 == 12) { //sr
                                                tempLanguage = "sr";
                                            } else if (arg1 == 13) { //zh
                                                tempLanguage = "zh";
                                            }
                                        }
                                    });

                            languageDialog.setPositiveButton(getResources().getString(R.string.docontinue),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            languageToLoad = tempLanguage;
                                            Preferences.savePreferences();
                                            // Unfortunately this means the MAIN folder name isn't right!
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
                                        }
                                    });

                            languageDialog.show();

                        } else if (childPosition == 3) {// Start/splash screen
                            // First though, set the preference to show the current version
                            // Otherwise it won't show the splash screen
                            SharedPreferences settings = getSharedPreferences("mysettings",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt("showSplashVersion", 0);
                            editor.apply();
                            Intent intent = new Intent();
                            intent.setClass(FullscreenActivity.this, SettingsActivity.class);
                            tryKillPads();
                            tryKillMetronome();
                            startActivity(intent);
                            finish();

                        } else if (childPosition == 4) {// Rate this app
                            Context context = getApplicationContext();
                            String appPackage = context.getPackageName();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                            startActivity(intent);
                        }
                    }
                }
                return false;
            }
        });
    }

    private void gesture1() {
        // Collapse both menus
        int count = listAdapterSong.getGroupCount();
        for (int i = 0; i < count; i++) {
            expListViewSong.collapseGroup(i);
        }
        int count2 = listAdapterOption.getGroupCount();
        for (int i = 0; i < count2; i++) {
            expListViewOption.collapseGroup(i);
        }
        // Open both drawers
        mDrawerLayout.openDrawer(expListViewSong);
        mDrawerLayout.openDrawer(expListViewOption);
        wasscrolling = false;
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
    }

    private void gesture2() {
        // Edit the song
        whattodo = "editsong";
        newFragment = PopUpEditSongFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void gesture3() {
        // Add to set
        if (isSong) {
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
            }, 1000);
        }
    }

    private void gesture4() {
        // Redraw the lyrics page
        redrawTheLyricsTable(main_page);
    }

    private void gesture5() {
        // Stop or start autoscroll
        popupAutoscroll_startstop(autoscrollButton);
    }

    private void gesture6() {
        // Stop or start pad
        popupPad_startstop(padButton);
    }

    private void gesture7() {
        // Start or stop metronome
        popupMetronome_startstop(metronomeButton);
    }

    private void toggleActionBar() {

        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        if (ab != null) {
            if (wasscrolling || scrollbutton) {
                if (hideactionbaronoff.equals("Y") && !expListViewSong.isFocused() && !expListViewSong.isShown() && !expListViewOption.isFocused() && !expListViewOption.isShown()) {
                    ab.hide();
                }
            } else if (!expListViewSong.isFocused() && !expListViewSong.isShown() && !expListViewOption.isFocused() && !expListViewOption.isShown()) {
                if (ab.isShowing() && hideactionbaronoff.equals("Y")) {
                    delayactionBarHide.postDelayed(hideActionBarRunnable, 500);
                    actionbarbutton = false;
                } else {
                    ab.show();
                    // Set a runnable to hide it after 3 seconds
                    if (hideactionbaronoff.equals("Y")) {
                        delayactionBarHide.postDelayed(hideActionBarRunnable, 3000);
                    }
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (currentapiVersion >= 17) {
                // Capable of dual head presentations
                dualDisplayCapable = "Y";
            }
            setWindowFlags();
            setWindowFlagsAdvanced();
            restoreTranslucentBarsDelayed();
        }
    }

    // This bit listens for long key presses (disables the menu long press action)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            // Open up the song search intent instead of bringing up the keyboard
            Log.d("d", "menu long presses");
            return true;
        }
        return false;
    }

    // This bit listens for key presses (for page turn and scroll)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        // Reset immersive mode
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            restoreTranslucentBarsDelayed();
        }

        // Set a runnable to reset swipe back to original value after 1 second

        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.isLongPress()) {
                // Open up the song search intent instead of bringing up the keyboard
                return true;
            } else {
                // User wants the menu opened/closed
                if (mDrawerLayout.isDrawerOpen(expListViewOption)) {
                    mDrawerLayout.closeDrawer(expListViewOption);
                }
                if (mDrawerLayout.isDrawerOpen(expListViewSong)) {
                    mDrawerLayout.closeDrawer(expListViewSong);
                }
            }
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (scrollstickyholder.getVisibility() == View.VISIBLE) {
                // Hide the sticky
                scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
                // After 500ms, make them invisible
                Handler delayhidenote = new Handler();
                delayhidenote.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mySticky.setVisibility(View.INVISIBLE);
                        scrollstickyholder.setVisibility(View.GONE);
                    }
                }, 500);
                stickynotes.setAlpha(0.4f);

            } else {
                // Ask the user if they want to exit.  Give them an are you sure prompt
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                // Yes button clicked
                                killPad1(padButton);
                                killPad2(padButton);
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
            }, delayswipe_time); // 1800ms delay

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

                int height = scrollpage.getChildAt(0).getMeasuredHeight() - scrollpage.getHeight();

                if (height > scrollpage.getScrollY()) {
                    // Need to scroll down first
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    scrollpage.smoothScrollBy(0, (int) (0.9 * metrics.heightPixels));

                    // Set a runnable to check the scroll position after 1 second
                    delaycheckscroll.postDelayed(checkScrollPosition, checkscroll_time);
                    return false;
                }
            }

            // OK can't scroll, so move to the next song if we can

            if (isPDF && pdfPageCurrent < (pdfPageCount - 1)) {
                pdfPageCurrent = pdfPageCurrent + 1;
                redrawTheLyricsTable(main_page);
                return false;
            } else {
                pdfPageCurrent = 0;
            }

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
                } catch (XmlPullParserException | IOException e) {
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
            }, delayswipe_time);

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

                if (scrollpage.getScrollY() > 0) {
                    // Need to scroll up first
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    scrollpage.smoothScrollBy(0, (int) (-0.9 * metrics.heightPixels));

                    // Set a runnable to check the scroll position after 1 second
                    delaycheckscroll.postDelayed(checkScrollPosition, checkscroll_time);
                    return false;
                }
            }

            // OK can't scroll, so move to the previous song if we can
            if (isPDF && pdfPageCurrent > 0) {
                pdfPageCurrent = pdfPageCurrent - 1;
                redrawTheLyricsTable(main_page);
                return false;
            } else {
                pdfPageCurrent = 0;
            }

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
                } catch (XmlPullParserException | IOException e) {
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
            }, delayswipe_time);

            // Scroll the screen up
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            scrollpage.smoothScrollBy(0, (int) (-0.9 * metrics.heightPixels));
            // Set a runnable to check the scroll position after 1 second
            delaycheckscroll.postDelayed(checkScrollPosition, checkscroll_time);

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
            }, delayswipe_time);

            // Scroll the screen up
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            scrollpage.smoothScrollBy(0, (int) (0.9 * metrics.heightPixels));
            // Set a runnable to check the scroll position after 1 second
            delaycheckscroll.postDelayed(checkScrollPosition, checkscroll_time);

        } else if (keyCode == pageturner_PAD) {
            popupPad_startstop(padButton);

        } else if (keyCode == pageturner_AUTOSCROLL) {
            if (popupAutoscroll_stoporstart.equals("start")) {
                // user now wants to stop
                popupAutoscroll_stoporstart = "stop";
                popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
                autoscrollactivated = false;
            } else {
                // user now wants to start
                popupAutoscroll_stoporstart = "start";
                popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.stop));
                autoscrollactivated = true;
            }
            autoScroll(autoscrollButton);

        } else if (keyCode == pageturner_METRONOME) {
            popupMetronome_startstop(metronomeButton);

        }
        return false;
    }

    public void stickyNotes(View view) {
        if (mySticky.getVisibility() == View.VISIBLE) {
            // Hide the sticky
            scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            // After 500ms, make them invisible
            Handler delayhidenote = new Handler();
            delayhidenote.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mySticky.setVisibility(View.INVISIBLE);
                    scrollstickyholder.setVisibility(View.GONE);
                }
            }, 500);
            stickynotes.setAlpha(0.4f);
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
            scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom));
            stickynotes.setAlpha(0.7f);
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
        m_titleView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        m_titleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        m_titleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white));
        m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        dialogBuilder.setTitle(getResources().getText(R.string.pdf_selectpage).toString());
        LinearLayout pickpage = new LinearLayout(FullscreenActivity.this);
        pickpage.setOrientation(LinearLayout.VERTICAL);
        final TextView page_num = new TextView(FullscreenActivity.this);
        String text = (1 + pdfPageCurrent) + " / " + pdfPageCount;
        page_num.setText(text);
        page_num.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Medium);
        page_num.setGravity(1);
        final SeekBar pageseekbar = new SeekBar(FullscreenActivity.this);
        pickpage.addView(page_num);
        pickpage.addView(pageseekbar);
        dialogBuilder.setView(pickpage);
        pageseekbar.setProgress(pageseekbarpos);
        pageseekbar.setMax(pdfPageCount - 1);
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

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pdfPageCurrent = seekBar.getProgress();
                String text = (1 + pdfPageCurrent) + " / " + (pdfPageCount);
                page_num.setText(text);
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
        scrollpage.smoothScrollBy(0, (int) (-0.9 * metrics.heightPixels));
        // Set a runnable to check the scroll position after 1 second
        delaycheckscroll.postDelayed(checkScrollPosition, checkscroll_time);
        toggleActionBar();
    }

    public void doScrollDown(View view) {
        // Scroll the screen down
        wasscrolling = true;
        scrollbutton = true;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        scrollpage.smoothScrollBy(0, (int) (+0.9 * metrics.heightPixels));
        // Set a runnable to check the scroll position after 1 second
        delaycheckscroll.postDelayed(checkScrollPosition, checkscroll_time);
        toggleActionBar();
    }

    private void fixSetActionButtons() {
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
        // Only if there is only one occurence of the song in the set
        int count = 0;
        for (String aMSet : mSet) {
            if (aMSet.equals(whatsongforsetwork)) {
                count++;
            }
        }

        if (count < 2) {
            SetActions.indexSongInSet();
        }

        if (indexSongInSet < 0) {
            setView = "N";
        }
        if (setSize >= 1) {
            showCurrentSet(view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu = this.menu;

        ab = getSupportActionBar();
        if (currentapiVersion >= 14 && ab != null) {
            ab.setHomeButtonEnabled(false); // disable the button
            ab.setDisplayHomeAsUpEnabled(false); // remove the left caret
            ab.setDisplayShowHomeEnabled(false); // remove the icon
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        ab = getSupportActionBar();
        if (currentapiVersion >= 14 && ab != null) {
            ab.setHomeButtonEnabled(false); // disable the button
            ab.setDisplayHomeAsUpEnabled(false); // remove the left caret
            ab.setDisplayShowHomeEnabled(false); // remove the icon
        }

        // Force overflow icon to show, even if hardware key is present
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        // Force icons to show in overflow menu
        if (ab != null && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                    Log.e("menu", "onMenuOpened", e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (menu != null) {
            menu.clear();
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
        this.menu = menu;

        MenuItem set_back = null;
        MenuItem set_forward = null;
        if (menu != null) {
            set_back = menu.findItem(R.id.set_back);
            set_forward = menu.findItem(R.id.set_forward);
        }

        if (setSize > 0 && setView.equals("Y")) {
            if (set_back != null) {
                set_back.setVisible(true);
                set_back.getIcon().setAlpha(255);
            }
            if (set_forward != null) {
                set_forward.setVisible(true);
                set_forward.getIcon().setAlpha(255);
            }

        } else {
            if (set_back != null) {
                set_back.setVisible(false);
            }
            if (set_forward != null) {
                set_forward.setVisible(false);
            }
        }
        // Now decide if the song being viewed has a song before it.  Otherwise disable the back button
        if (indexSongInSet < 1) {
            if (set_back != null) {
                set_back.setEnabled(false);
                set_back.getIcon().setAlpha(30);
            }
        }
        // Now decide if the song being viewed has a song after it.  Otherwise disable the forward button
        // Also need to check if last item in set is a pdf, does it have more pages?
        if (indexSongInSet >= (setSize - 1) && (!isPDF || (pdfPageCurrent >= (pdfPageCount - 1)))) {
            if (set_forward != null) {
                set_forward.setEnabled(false);
                set_forward.getIcon().setAlpha(30);
            }
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
        super.onPause();
        killPad1(padButton);
        killPad2(padButton);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        setWindowFlags();
        setWindowFlagsAdvanced();
        restoreTranslucentBarsDelayed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // This bit draws the lyrics stored in the variable to the page.
    private void showLyrics(View view) throws IOException, IllegalStateException {

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
        if (mtask_autoscroll_music != null) {
            mtask_autoscroll_music.cancel(true);
        }

        // Turn off the autoscroll if it was on
        autoscrollispaused = false;
        scrollpage.smoothScrollTo(0, 0);
        popupAutoscroll_stoporstart = "stop";
        popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
        autoscrollonoff = "off";
        isautoscrolling = false;

        if (popupAutoscroll.getVisibility() != View.VISIBLE) {
            autoscrollButton.setAlpha(0.4f);
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

            // Find out if the song is in the current set
            // Only if there is only one occurence of the song in the set
            int count = 0;
            for (String aMSet : mSet) {
                if (aMSet.equals(whatsongforsetwork)) {
                    count++;
                }
            }

            if (count < 2) {
                if (SetActions.isSongInSet()) {
                    invalidateOptionsMenu();
                }
            }

            // If there isn't a key specified, or the user wants the pad off, or an invalid linkAudio,
            // make sure the padButton is turned off
            // User will be prompted to specify a key if they press the button again
            boolean stoppad = false;
            boolean badlinkaudio = false;
            // Test the audio file is there by assigning it to a temp mediaplayer
            MediaPlayer testMPlayer = new MediaPlayer();
            try {
                testMPlayer.setDataSource(this, Uri.parse(mLinkAudio));
                testMPlayer.prepare();
                testMPlayer.reset();
            } catch (Exception e) {
                // Problem with link audio so don't use it
                badlinkaudio = true;
            }
            testMPlayer.release();

            if (!mPadFile.equals(getResources().getString(R.string.off)) &&
                    !mPadFile.equals(getResources().getString(R.string.link_audio)) &&
                    (mKey.isEmpty() || mKey.equals("")) && padson) {
                // Trying to play a pad and the key isn't set
                myToastMessage = getResources().getString(R.string.pad_error);
                stoppad = true;

            } else if (mPadFile.equals(getResources().getString(R.string.off)) && padson) {
                // Pad has been turned off for this song
                myToastMessage = getResources().getString(R.string.pad) + " - " + getResources().getString(R.string.off);
                stoppad = true;

            } else if (mPadFile.equals(getResources().getString(R.string.link_audio)) && (mLinkAudio.isEmpty() || badlinkaudio) && padson) {
                // Want a link audio, but the audio link is either empty or invalid audio
                myToastMessage = getResources().getString(R.string.link_audio) + " - " + getResources().getString(R.string.file_type_unknown);
                stoppad = true;

            }

            if (stoppad) {
                if (!myToastMessage.isEmpty()) {
                    ShowToast.showToast(FullscreenActivity.this);
                }
                padson = false;
                if (mPlayer1 != null) {
                    if (mPlayer1.isPlaying()) {
                        whichtofadeout = 1;
                        fadeout1 = true;
                        popupPad_startstopbutton.setText(getResources().getString(R.string.wait));
                        popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_button));
                        popupPad_startstopbutton.setEnabled(false);
                        isfading1 = false;
                        fadeOutBackgroundMusic1();
                    }
                }
                if (mPlayer2 != null) {
                    if (mPlayer2.isPlaying()) {
                        whichtofadeout = 2;
                        fadeout2 = true;
                        popupPad_startstopbutton.setText(getResources().getString(R.string.wait));
                        popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_button));
                        popupPad_startstopbutton.setEnabled(false);
                        isfading2 = false;
                        fadeOutBackgroundMusic2();
                    }
                }
                if (popupPad.getVisibility() != View.VISIBLE) {
                    padButton.setAlpha(0.4f);
                }
            }

            if ((mPlayer1 == null && mPlayer2 == null) || (mPlayer1 != null && !mPlayer1.isPlaying()) && mPlayer2 != null && !mPlayer2.isPlaying()) {
                popupPad_stoporstart = "stop";
                padson = false;
                popupPad_startstopbutton.setEnabled(true);
                popupPad_startstopbutton.setText(getResources().getString(R.string.start));
                popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
                if (popupPad.getVisibility() != View.VISIBLE) {
                    padButton.setAlpha(0.4f);
                }
                fadeout1 = false;
                fadeout2 = false;
            }

            // Set song key
            ProcessSong.processKey();
            popupPad_key.setSelection(keyindex);

            // Set the pad / backing track
            if (mPadFile.equals(getResources().getString(R.string.off))) {
                popupPad_file.setSelection(2);
            } else if (mPadFile.equals(getResources().getString(R.string.link_audio)) && !mLinkAudio.isEmpty() && !mLinkAudio.equals("")) {
                popupPad_file.setSelection(1);
            } else {
                popupPad_file.setSelection(0);
            }

            // Set the pad volume and pan
            int temp_padvol = (int) (100 * padvol);
            popupPad_volume.setProgress(temp_padvol);
            popupPad_volume_text.setText(temp_padvol + " %");
            switch (padpan) {
                case "left":
                    popupPad_pan_text.setText("L");
                    popupPad_pan.setProgress(0);
                    break;
                case "right":
                    popupPad_pan_text.setText("R");
                    popupPad_pan.setProgress(2);
                    break;
                default:
                    popupPad_pan_text.setText("C");
                    popupPad_pan.setProgress(1);
                    break;
            }

            // Set the autoscroll values
            try {
                autoScrollDuration = Integer.parseInt(mDuration.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                autoScrollDuration = -1;
            }

            try {
                autoScrollDelay = Integer.parseInt(mPreDelay.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                autoScrollDelay = -1;
            }
            usingdefaults = false;
            if (mDuration.isEmpty() && autoscroll_default_or_prompt.equals("default")) {
                autoScrollDuration = default_autoscroll_songlength;
                usingdefaults = true;
            }

            if (mPreDelay.isEmpty() && autoscroll_default_or_prompt.equals("default")) {
                autoScrollDelay = default_autoscroll_predelay;
                usingdefaults = true;
            }

            String text;
            if (autoScrollDelay < 0) {
                popupAutoscroll_delay.setProgress(0);
                text = "";
            } else {
                popupAutoscroll_delay.setProgress(autoScrollDelay + 1);
                text = autoScrollDelay + " s";
            }
            popupAutoscroll_delay_text.setText(text);
            String timetext = timeFormatFixer((autoScrollDuration));
            currentTime_TextView.setText("0:00");
            totalTime_TextView.setText(timetext);
            playbackProgress.setVisibility(View.INVISIBLE);

            if (autoScrollDuration < 0) {
                text = "";
            } else {
                text = autoScrollDuration + "";
            }
            popupAutoscroll_duration.setText(text);

            // Set time signature
            timesigindex = 0;
            ProcessSong.processTimeSig();
            popupMetronome_timesig.setSelection(timesigindex);

            // Process tempo
            ProcessSong.processTempo();

            if (temposlider < 1) {
                temposlider = 0;
                popupMetronome_tempo_text.setText(getResources().getString(R.string.notset));
            } else {
                popupMetronome_tempo_text.setText(mTempo);
            }
            popupMetronome_tempo.setProgress(temposlider);

            // Set the metronome volume and pan
            int temp_metronomevol = (int) (100 * metronomevol);
            popupMetronome_volume.setProgress(temp_metronomevol);
            popupMetronome_volume_text.setText(temp_metronomevol + " %");
            switch (metronomepan) {
                case "left":
                    popupMetronome_pan_text.setText("L");
                    popupMetronome_pan.setProgress(0);
                    break;
                case "right":
                    popupMetronome_pan_text.setText("R");
                    popupMetronome_pan.setProgress(2);
                    break;
                default:
                    popupMetronome_pan_text.setText("C");
                    popupMetronome_pan.setProgress(1);
                    break;
            }
            // Alert the user for no timesig/tempo
            if (timesigindex == 0) {
                if (metroTask != null) {
                    metroTask.cancel(true);
                    metroTask.stop();
                    if (metronomeonoff.equals("on")) {
                        promptTimeSig();
                    }
                }
            } else if (temposlider == 0) {
                if (metroTask != null) {
                    metroTask.cancel(true);
                    metroTask.stop();
                    if (metronomeonoff.equals("on")) {
                        promptTempo();
                    }
                }
            }

            // If metronome is playing and tempo and timesig are good,
            // Stop the original metronome and start a new one
            if (metroTask != null) {
                metroTask.cancel(true);
                metroTask.stop();
            }
            if (metronomeonoff.equals("on") && temposlider != 0 && timesigindex != 0) {
                ProcessSong.processTempo();
                bpm = (short) ((short) temposlider + 39);
                metroTask = new MetronomeAsyncTask();
                Runtime.getRuntime().gc();
                metronomeonoff = "on";
                popupMetronome_stoporstart = "start";
                popupMetronome_startstopbutton.setText(getResources().getString(R.string.stop));
                metroTask.execute();
            }

            // Stop restarting the pads if changing portrait/landscape
            // Only play if this isn't called by an orientation change
            if (!orientationchanged && padson) {
                playPads(view);
            }

            // Now, reset the orientation.
            orientationchanged = false;

            // Make the page buttons visible for a song
            // The whole group can still be hidden if the user specified it
            if (!isPDF && isSong && togglePageButtons.equals("Y")) {
                stickynotes.setVisibility(View.VISIBLE);
                autoscrollButton.setVisibility(View.VISIBLE);
                metronomeButton.setVisibility(View.VISIBLE);
                padButton.setVisibility(View.VISIBLE);
                linkButton.setVisibility(View.VISIBLE);
                chordButton.setVisibility(View.VISIBLE);
            } else {
                stickynotes.setVisibility(View.INVISIBLE);
                autoscrollButton.setVisibility(View.INVISIBLE);
                metronomeButton.setVisibility(View.INVISIBLE);
                padButton.setVisibility(View.INVISIBLE);
                linkButton.setVisibility(View.INVISIBLE);
                chordButton.setVisibility(View.INVISIBLE);
            }

            // Refresh the song list to the current song.
            findSongInFolder();

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

            if (toggleScrollArrows.equals("D")) {
                uparrow_bottom.setVisibility(View.INVISIBLE);
                downarrow_top.setVisibility(View.INVISIBLE);
            } else {
                uparrow_bottom.setVisibility(View.GONE);
                downarrow_top.setVisibility(View.GONE);
            }
            uparrow_top.setVisibility(View.INVISIBLE);
            downarrow_bottom.setVisibility(View.INVISIBLE);
        }

        // If the song is a PDF, turn stuff off that won't work
        if (isPDF) {
            hidepopupAutoscroll();
            hidepopupMetronome();
            hidepopupPad();
            hidepopupChord();
            hidepopupSticky();

            padButton.setAlpha(0.4f);
            metronomeButton.setAlpha(0.4f);
            autoscrollButton.setAlpha(0.4f);

            //Stop Autoscroll
            if (mtask_autoscroll_music != null) {
                mtask_autoscroll_music.cancel(true);
            }

            //Stop Metronome
            if (metroTask != null) {
                metroTask.cancel(true);
                metroTask.stop();
            }
            padson = false;
            popupMetronome_stoporstart = "stop";
            metronomeonoff = "off";

            // Fix buttons
            popupPad_startstopbutton.setText(getResources().getString(R.string.start));
            popupMetronome_startstopbutton.setText(getResources().getString(R.string.start));
            popupAutoscroll_startstopbutton.setText(getResources().getString(R.string.start));
            popupPad_startstopbutton.setEnabled(true);
            popupMetronome_startstopbutton.setEnabled(true);
            popupAutoscroll_startstopbutton.setEnabled(true);
            popupPad_startstopbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button));
        }

        if (isPDF && padson) {
            mKey = "";
            padson = false;
            if (mtask_fadeout_music1 != null) {
                mtask_fadeout_music1 = null;
            }
            if (mtask_fadeout_music2 != null) {
                mtask_fadeout_music2 = null;
            }
            fadeout1 = false;
            fadeout2 = false;
            if (mPlayer1 != null) {
                // We need to fade out mPlayer1
                if (mPlayer1.isPlaying()) {
                    // We need to fade this out over the next 8 seconds
                    whichtofadeout = 1;
                    fadeout1 = true;
                    fadeOutBackgroundMusic1();
                }
            }
            if (mPlayer2 != null) {
                // We need to fade out mPlayer2
                if (mPlayer2.isPlaying()) {
                    // We need to fade this out over the next 8 seconds
                    whichtofadeout = 2;
                    fadeout2 = true;
                    fadeOutBackgroundMusic2();
                }
            }
            if (popupPad.getVisibility() != View.VISIBLE) {
                padButton.setAlpha(0.4f);
            }
        }

        // Now, decide which column we are writing to
        if (columnTest == 1) {
            scrollpage = scrollpage_onecol;
        } else if (columnTest == 2) {
            scrollpage = scrollpage_twocol;
        } else if (columnTest == 3) {
            scrollpage = scrollpage_threecol;
        } else {
            scrollpage = scrollpage_onecol;
        }

        if (isPDF) {
            scrollpage = scrollpage_pdf;
            needtoredraw = false;
            columnTest = 99;
        }

        // Set the default view!  Set the theme colours
        SetUpColours.colours();

        main_page.setBackgroundColor(lyricsBackgroundColor);
        scrollpage.setBackgroundColor(lyricsBackgroundColor);

        // Set up a listener to wait for the tables to draw
        ViewTreeObserver vto = scrollpage.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("SuspiciousNameCombination")
            @Override
            public void onGlobalLayout() {
                scrollpage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width;
                int height;
                pageWidth = main_page.getMeasuredWidth();
                pageHeight = main_page.getMeasuredHeight();

                // Get the scale for 1 col view
                width = lyricstable_onecolview.getMeasuredWidth() + 8;
                height = lyricstable_onecolview.getMeasuredHeight() + 8;
                scaleX = pageWidth / width;
                scaleY = pageHeight / height;
                if (toggleYScale.equals("Y")) {
                    if (scaleX > scaleY) {
                        //noinspection SuspiciousNameCombination
                        scaleX = scaleY;
                    } else if (toggleYScale.equals("W")) {
                        scaleY = scaleX;
                    }
                }
                onecolfontsize = scaleX;

                // Get the scale for 2 col view
                width = lyricstable_twocolview.getMeasuredWidth() + lyricstable2_twocolview.getMeasuredWidth() + 16;
                height = lyricstable_twocolview.getMeasuredHeight() + 8;
                int height2 = lyricstable2_twocolview.getMeasuredHeight() + 8;
                if (height2 > height) {
                    height = height2;
                }
                scaleX = pageWidth / width;
                scaleY = pageHeight / height;
                if (toggleYScale.equals("Y")) {
                    if (scaleX > scaleY) {
                        scaleX = pageHeight / height;
                    } else if (toggleYScale.equals("W")) {
                        scaleY = pageWidth / width;
                    }
                }
                twocolfontsize = scaleX;

                // Get the scale for 3 col view
                width = lyricstable_threecolview.getMeasuredWidth() + lyricstable2_threecolview.getMeasuredWidth() + lyricstable3_threecolview.getMeasuredWidth() + 36;
                height = lyricstable_threecolview.getMeasuredHeight() + 12;
                height2 = lyricstable2_threecolview.getMeasuredHeight() + 12;
                int height3 = lyricstable3_threecolview.getMeasuredHeight() + 12;
                if (height2 > height) {
                    height = height2;
                }
                if (height3 > height) {
                    height = height3;
                }
                scaleX = pageWidth / width;
                scaleY = pageHeight / height;
                if (toggleYScale.equals("Y")) {
                    if (scaleX > scaleY) {
                        scaleX = pageHeight / height;
                    } else if (toggleYScale.equals("W")) {
                        scaleY = pageWidth / width;
                    }
                }
                threecolfontsize = scaleX;

                // Which view ia the best one then?  Save the font scale size
                if (threecolfontsize > onecolfontsize && threecolfontsize > twocolfontsize) {
                    // 3 columns is best
                    tempfontsize = mainfontsize * threecolfontsize - 0.6f;
                    tempsectionsize = sectionfontsize * threecolfontsize - 0.6f;
                    scrollpage = scrollpage_threecol;
                    columnTest = 3;
                } else if (twocolfontsize > onecolfontsize && twocolfontsize > threecolfontsize) {
                    tempfontsize = mainfontsize * twocolfontsize - 0.6f;
                    tempsectionsize = sectionfontsize * twocolfontsize - 0.6f;
                    scrollpage = scrollpage_twocol;
                    columnTest = 2;
                } else {
                    tempfontsize = mainfontsize * onecolfontsize - 0.6f;
                    tempsectionsize = sectionfontsize * onecolfontsize - 0.6f;
                    scrollpage = scrollpage_onecol;
                    columnTest = 1;
                }

                // Make sure font sizes don't exceed the max specified by the user
                if (mMaxFontSize < 20) {
                    mMaxFontSize = 20;
                }
                if (tempfontsize > mMaxFontSize) {
                    tempfontsize = mMaxFontSize;
                }
                if (tempsectionsize > mMaxFontSize) {
                    tempsectionsize = mMaxFontSize * 0.7f;
                }

                if (toggleYScale.equals("N")) {
                    tempfontsize = mFontSize;
                    tempsectionsize = mFontSize * 0.7f;
                    columnTest = 1;
                }

                needtoredraw = false;
                if (!isPDF) {
                    writeSongToPage();
                } else {
                    columnTest = 99;
                    scrollpage = scrollpage_pdf;
                }

                tempfontsize = mainfontsize;
                tempsectionsize = sectionfontsize;
                songTitleHolder.setTextColor(lyricsTextColor);
                songTitleHolder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout));
                songLoadingProgressBar.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout));
                songLoadingProgressBar.setVisibility(View.INVISIBLE);
                songTitleHolder.setVisibility(View.INVISIBLE);
                if (whichDirection.equals("L2R")) {
                    scrollpage.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
                } else {
                    scrollpage.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
                }
                if (columnTest == 1) {
                    scrollpage_onecol.setVisibility(View.VISIBLE);
                    scrollpage_pdf.setVisibility(View.GONE);
                    scrollpage_twocol.setVisibility(View.GONE);
                    scrollpage_threecol.setVisibility(View.GONE);
                } else if (columnTest == 2) {
                    scrollpage_twocol.setVisibility(View.VISIBLE);
                    scrollpage_pdf.setVisibility(View.GONE);
                    scrollpage_onecol.setVisibility(View.GONE);
                    scrollpage_threecol.setVisibility(View.GONE);
                } else if (columnTest == 3) {
                    scrollpage_threecol.setVisibility(View.VISIBLE);
                    scrollpage_pdf.setVisibility(View.GONE);
                    scrollpage_onecol.setVisibility(View.GONE);
                    scrollpage_twocol.setVisibility(View.GONE);
                } else {
                    scrollpage_pdf.setVisibility(View.VISIBLE);
                    scrollpage_onecol.setVisibility(View.GONE);
                    scrollpage_twocol.setVisibility(View.GONE);
                    scrollpage_threecol.setVisibility(View.GONE);
                }

                // If autoStickyNotes is set to yes and they aren't empty, display them
                if (toggleAutoSticky.equals("Y") && !mNotes.isEmpty() && !mNotes.equals("")) {
                    // Opening a sticky note!
                    // Hide other popups
                    hidepopupPad();
                    hidepopupChord();
                    hidepopupAutoscroll();
                    hidepopupMetronome();

                    mySticky.setText(mNotes);
                    mySticky.setVisibility(View.VISIBLE);
                    scrollstickyholder.setVisibility(View.VISIBLE);
                    scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom));
                    stickynotes.setAlpha(0.7f);
                }

                // Restart the autoscroll if it was already on and the song has a duration
                // Get the autoscroll info initialised
                if (autostartautoscroll && autoscrollactivated) {

                    if (usingdefaults) {
                        myToastMessage = getString(R.string.default_autoscroll);
                        ShowToast.showToast(FullscreenActivity.this);
                    }

                    if (mtask_autoscroll_music != null) {
                        mtask_autoscroll_music.cancel(true);
                    }
                    delayautoscroll.removeCallbacks(autoScrollRunnable);
                    autoScrollDelay = popupAutoscroll_delay.getProgress() - 1;
                    mDuration = popupAutoscroll_duration.getText().toString();
                    getAutoScrollValues();

                    // Wait 2 seconds before starting autoscroll (time for page to draw)
                    Handler prepareAutoscroll = new Handler();
                    prepareAutoscroll.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            popupAutoscroll_startstop(autoscrollButton);
                            Log.d("d","attempting to start autoscroll");
                        }
                    }, 2000);
                }
            }
        });


        if (!isPDF) {
            // Set a variable to decide if capo chords should be shown
            showCapo = !capoDisplay.equals("native") &&
                    (mCapo.equals("1") || mCapo.equals("2") || mCapo.equals("3") ||
                            mCapo.equals("4") || mCapo.equals("5") || mCapo.equals("6") ||
                            mCapo.equals("7") || mCapo.equals("8") || mCapo.equals("9") ||
                            mCapo.equals("10") || mCapo.equals("11"));

            // Decide on the font being used
            SetTypeFace.setTypeface();


            // Write the song to the tables
            columnTest = 0;
            writeSongToPage();

            songandauthor.setText("");

            // Decide on best title
            String songtitleorfilename;
            if (!mTitle.equals("")) {
                songtitleorfilename = mTitle.toString();
            } else {
                songtitleorfilename = songfilename;
            }

            // If key is set
            String keytext = "";
            if (!mKey.isEmpty() && !mKey.equals("")) {
                keytext = " (" + mKey + ")";
            }
            // Get best author
            if (mAuthor.equals("")) {
                mTempAuthor = "";
            } else {
                mTempAuthor = mAuthor.toString();
            }

            // Put the title together
            String text = songtitleorfilename + keytext + "\n" + mTempAuthor;
            songandauthor.setText(text);

        } else {
            Preferences.savePreferences();
            // is a pdf file
            // if we are able, render pdf to image
            // filePath represent path of Pdf document on storage
            File file;
            if (whichSongFolder.equals(mainfoldername)) {
                file = new File(dir + "/" + songfilename);
            } else {
                file = new File(dir + "/" + whichSongFolder + "/" + songfilename);
            }
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Capable of pdf rendering
                // PdfRenderer enables rendering a PDF document
                padButton.setVisibility(View.INVISIBLE);
                stickynotes.setVisibility(View.INVISIBLE);
                metronomeButton.setVisibility(View.INVISIBLE);
                autoscrollButton.setVisibility(View.INVISIBLE);
                chordButton.setVisibility(View.INVISIBLE);
                scrollstickyholder.setVisibility(View.GONE);

                PdfRenderer mPdfRenderer = null;
                try {
                    mPdfRenderer = new PdfRenderer(mFileDescriptor);
                    pdfPageCount = mPdfRenderer.getPageCount();
                } catch (IOException e) {
                    e.printStackTrace();
                    pdfPageCount = 0;
                }

                if (pdfPageCurrent > pdfPageCount) {
                    pdfPageCurrent = 0;
                }

                String text = tempsongtitle + "\n" + (pdfPageCurrent + 1) + "/" + pdfPageCount;
                songandauthor.setText(text);

                if (pdfPageCount > 1 && togglePageButtons.equals("Y")) {
                    // Show page select button
                    pdf_selectpage.setVisibility(View.VISIBLE);
                } else {
                    pdf_selectpage.setVisibility(View.INVISIBLE);
                }

                stickynotes.setVisibility(View.INVISIBLE);
                scrollstickyholder.setVisibility(View.GONE);
                stickynotes.setAlpha(0.4f);
                // Open page 0
                PdfRenderer.Page mCurrentPage = null;
                if (mPdfRenderer != null) {
                    //noinspection AndroidLintNewApi
                    mCurrentPage = mPdfRenderer.openPage(pdfPageCurrent);
                }

                // Get pdf size from page
                int pdfwidth = 1;
                int pdfheight = 1;
                pdfwidth = mCurrentPage.getWidth();
                pdfheight = mCurrentPage.getHeight();

                int holderwidth = pdfwidth;
                int holderheight = pdfheight;

                int pagewidth = main_page.getWidth();
                int pageheight = main_page.getHeight();

                switch (toggleYScale) {
                    case "Y":
                        float xscale = (float) pagewidth / (float) pdfwidth;
                        float yscale = (float) pageheight / (float) pdfheight;
                        if (xscale > yscale) {
                            xscale = yscale;
                        } else {
                            yscale = xscale;
                        }
                        pdfheight = (int) ((float) pdfheight * yscale);
                        pdfwidth = (int) ((float) pdfwidth * xscale);
                        holderheight = pageheight;
                        holderwidth = pagewidth;

                        break;
                    case "W":
                        pdfheight = (int) (((float) pagewidth / (float) pdfwidth) * (float) pdfheight);
                        pdfwidth = pagewidth;
                        holderheight = pdfheight;
                        holderwidth = pdfwidth;
                        break;
                    default:
                        // This means pdf will never be bigger than needed (even if scale is off)
                        // This avoids massive files calling out of memory error
                        if (pdfwidth > pagewidth) {
                            pdfheight = (int) (((float) pagewidth / (float) pdfwidth) * (float) pdfheight);
                            pdfwidth = pagewidth;
                            holderheight = pdfheight;
                            holderwidth = pdfwidth;
                        }
                        break;
                }
                if (pdfwidth == 0) {
                    pdfwidth = 1;
                }
                if (pdfheight == 0) {
                    pdfheight = 1;
                }
                Bitmap bitmap = Bitmap.createBitmap(pdfwidth, pdfheight, Bitmap.Config.ARGB_8888);

                // Pdf page is rendered on Bitmap
                mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                // Set rendered bitmap to ImageView (pdfView in my case)
                ImageView pdfView = (ImageView) findViewById(R.id.pdfView);

                pdfView.setImageBitmap(bitmap);
                pdfView.getLayoutParams().height = holderheight;
                pdfView.getLayoutParams().width = holderwidth;

                mCurrentPage.close();
                mPdfRenderer.close();

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
                String text = tempsongtitle + "\n" + getResources().getString(R.string.nothighenoughapi);
                songandauthor.setText(text);
                Preferences.savePreferences();
                invalidateOptionsMenu();

                // Run an intent to try to show the pdf externally
                Intent target = new Intent(Intent.ACTION_VIEW);
                File pdffile;
                if (whichSongFolder.equals(mainfoldername)) {
                    pdffile = new File(dir + "/" + songfilename);
                } else {
                    pdffile = new File(dir + "/" + whichSongFolder + "/" + songfilename);
                }
                target.setDataAndType(Uri.fromFile(pdffile), "application/pdf");
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                try {
                    startActivity(target);
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.pdfviewer")));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.pdfviewer")));
                    }
                }
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

        // If we have just converted a song from .pro or .onsong, we need to fix the song menu
        if (converting) {
            converting = false;
            prepareSongMenu();
        }
    }

    public void shareSong() {
        if (!isSong) {
            // Editing a slide / note / scripture / image
            myToastMessage = getResources().getString(R.string.not_allowed);
            ShowToast.showToast(FullscreenActivity.this);
        } else {
            // Export
            // The current song is the songfile
            // Believe it or not, it works!!!!!
            // Take a screenshot as a bitmap
            scrollpage.destroyDrawingCache();
            scrollpage.setDrawingCacheEnabled(true);
            bmScreen = null;
            bmScreen = scrollpage.getDrawingCache();
            File saved_image_file = new File(
                    homedir + "/Images/_cache/" + songfilename + ".png");
            if (saved_image_file.exists())
                saved_image_file.delete();
            try {
                FileOutputStream out = new FileOutputStream(saved_image_file);
                bmScreen.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Run the script that generates the email text which has the set details in it.
            try {
                ExportPreparer.songParser();
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_TITLE, songfilename);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, songfilename);
            emailIntent.putExtra(Intent.EXTRA_TEXT, emailtext);
            emailtext = "";
            String songlocation = dir + "/";
            String tolocation = homedir + "/Notes/_cache/";
            Uri uri;
            if (!dir.toString().contains("/" + whichSongFolder + "/")
                    && !whichSongFolder.equals(mainfoldername)) {
                uri = Uri.fromFile(new File(dir + "/" + whichSongFolder + "/" + songfilename));
                songlocation = songlocation + whichSongFolder + "/" + songfilename;
                tolocation = tolocation + "/" + songfilename + ".ost";
            } else {
                uri = Uri.fromFile(new File(dir + "/" + songfilename));
                songlocation = songlocation + songfilename;
                tolocation = tolocation + "/" + songfilename + ".ost";
            }

            Uri uri2 = Uri.fromFile(saved_image_file);
            Uri uri3 = null;
            // Also add an .ost version of the file
            try {
                FileInputStream in = new FileInputStream(new File(songlocation));
                FileOutputStream out = new FileOutputStream(new File(tolocation));

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;

                // write the output file (You have now copied the file)
                out.flush();
                out.close();
                out = null;

                uri3 = Uri.fromFile(new File(tolocation));

            } catch (Exception e) {
                // Error
                e.printStackTrace();
            }
            ArrayList<Uri> uris = new ArrayList<>();
            if (uri != null) {
                uris.add(uri);
            }
            if (uri2 != null) {
                uris.add(uri2);
            }
            if (uri3 != null) {
                uris.add(uri3);
            }
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            startActivityForResult(Intent.createChooser(emailIntent, exportcurrentsong), 12345);

            // These .ost and .png files will be removed when a user loads a new set
        }
    }

    private void prepareChords() {
        // Read in my custom chords
        if (mCustomChords == null) {
            mCustomChords = "";
        }
        while (mCustomChords.contains("  ")) {
            mCustomChords = mCustomChords.replace("  ", " ");
        }

        String tempallchords = allchords;
        mCustomChords = mCustomChords.trim();
        String[] tempCustomChordsArray = mCustomChords.split(" ");
        String tempCustomChordsToAdd = "";
        int numcustomchords;
        if (tempCustomChordsArray.length > 0) {
            numcustomchords = tempCustomChordsArray.length;
            for (int q = 0; q < numcustomchords; q++) {
                if ((chordInstrument.equals("u") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_u_")) ||
                        (chordInstrument.equals("m") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_m_")) ||
                        (chordInstrument.equals("c") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_c_")) ||
                        (chordInstrument.equals("b") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_b_")) ||
                        (chordInstrument.equals("B") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_B_")) ||
                        (chordInstrument.equals("g") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_g_"))) {
                    tempCustomChordsToAdd = tempCustomChordsToAdd + " $$$" + tempCustomChordsArray[q];
                }
            }
        }

        // Remove all whitespace between chords
        if (allchords == null) {
            allchords = "";
        }
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

        // Add the identified custom chords (start with $$$) to the allchords
        tempallchords = tempCustomChordsToAdd + " " + tempallchords;

        ArrayList<String> unique_chords = new ArrayList<>();
        tempallchords = tempallchords.trim();
        String[] allchords_array = tempallchords.split(" ");
        if (allchords_array.length > 0) {
            for (String anAllchords_array : allchords_array) {
                if (!unique_chords.contains(anAllchords_array)) {
                    unique_chords.add(anAllchords_array);
                }
            }
        }

        chordimageshere.removeAllViews();
        // Send the unique chords off to get the string layout
        // This will eventually be if guitar/ukulele/mandolin/piano/other
        // Custom chords don't get sent for retrieval as they are already defined
        for (int l = 0; l < unique_chords.size(); l++) {
            if (chordInstrument.equals("u") && unique_chords != null && !unique_chords.get(l).contains("$$$")) {
                ChordDirectory.ukuleleChords(unique_chords.get(l));
            } else if (chordInstrument.equals("m") && unique_chords != null && !unique_chords.get(l).contains("$$$")) {
                ChordDirectory.mandolinChords(unique_chords.get(l));
            } else if (chordInstrument.equals("g") && unique_chords != null && !unique_chords.get(l).contains("$$$")) {
                ChordDirectory.guitarChords(unique_chords.get(l));
            } else if (chordInstrument.equals("c") && unique_chords != null && !unique_chords.get(l).contains("$$$")) {
                ChordDirectory.cavaquinhoChords(unique_chords.get(l));
            } else if (chordInstrument.equals("b") && unique_chords != null && !unique_chords.get(l).contains("$$$")) {
                ChordDirectory.banjo4stringChords(unique_chords.get(l));
            } else if (chordInstrument.equals("B") && unique_chords != null && !unique_chords.get(l).contains("$$$")) {
                ChordDirectory.banjo5stringChords(unique_chords.get(l));
            }

            // If chord is custom, prepare this prefix to the name
            String iscustom = "";
            if (unique_chords != null && unique_chords.get(l).contains("$$$")) {
                iscustom = "\n" + getResources().getString(R.string.custom) + "";
                chordnotes = unique_chords.get(l);
                chordnotes = chordnotes.replace("$$$", "");
                unique_chords.set(l, unique_chords.get(l).replace("$$$", ""));
                int startposcname = unique_chords.get(l).lastIndexOf("_");
                if (startposcname != -1) {
                    unique_chords.set(l, unique_chords.get(l).substring(startposcname + 1));
                }
            }

            // Prepare a new Horizontal Linear Layout for each chord
            TableRow chordview = new TableRow(this);
            TableLayout.LayoutParams tableRowParams =
                    new TableLayout.LayoutParams
                            (TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

            int leftMargin = 10;
            int topMargin = 10;
            int rightMargin = 10;
            int bottomMargin = 10;

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

            // Initialise 6 strings and frets
            String string_6 = "";
            String string_5 = "";
            String string_4 = "";
            String string_3 = "";
            String string_2 = "";
            String string_1 = "";
            String fret = "";

            if (chordInstrument.equals("g")) {

                if (chordnotes.length() > 0) {
                    string_6 = chordnotes.substring(0, 1);
                }
                if (chordnotes.length() > 1) {
                    string_5 = chordnotes.substring(1, 2);
                }
                if (chordnotes.length() > 2) {
                    string_4 = chordnotes.substring(2, 3);
                }
                if (chordnotes.length() > 3) {
                    string_3 = chordnotes.substring(3, 4);
                }
                if (chordnotes.length() > 4) {
                    string_2 = chordnotes.substring(4, 5);
                }
                if (chordnotes.length() > 5) {
                    string_1 = chordnotes.substring(5, 6);
                }
                if (chordnotes.length() > 7) {
                    fret = chordnotes.substring(7, 8);
                }

                // Prepare string_6
                switch (string_6) {
                    case "0":
                        image6.setImageDrawable(l0);
                        break;
                    case "1":
                        image6.setImageDrawable(l1);
                        break;
                    case "2":
                        image6.setImageDrawable(l2);
                        break;
                    case "3":
                        image6.setImageDrawable(l3);
                        break;
                    case "4":
                        image6.setImageDrawable(l4);
                        break;
                    case "5":
                        image6.setImageDrawable(l5);
                        break;
                    default:
                        image6.setImageDrawable(lx);
                        break;
                }

                // Prepare string_5
                switch (string_5) {
                    case "0":
                        image5.setImageDrawable(m0);
                        break;
                    case "1":
                        image5.setImageDrawable(m1);
                        break;
                    case "2":
                        image5.setImageDrawable(m2);
                        break;
                    case "3":
                        image5.setImageDrawable(m3);
                        break;
                    case "4":
                        image5.setImageDrawable(m4);
                        break;
                    case "5":
                        image5.setImageDrawable(m5);
                        break;
                    default:
                        image5.setImageDrawable(mx);
                        break;
                }

                // Prepare string_4
                switch (string_4) {
                    case "0":
                        image4.setImageDrawable(m0);
                        break;
                    case "1":
                        image4.setImageDrawable(m1);
                        break;
                    case "2":
                        image4.setImageDrawable(m2);
                        break;
                    case "3":
                        image4.setImageDrawable(m3);
                        break;
                    case "4":
                        image4.setImageDrawable(m4);
                        break;
                    case "5":
                        image4.setImageDrawable(m5);
                        break;
                    default:
                        image4.setImageDrawable(mx);
                        break;
                }

                // Prepare string_3
                switch (string_3) {
                    case "0":
                        image3.setImageDrawable(m0);
                        break;
                    case "1":
                        image3.setImageDrawable(m1);
                        break;
                    case "2":
                        image3.setImageDrawable(m2);
                        break;
                    case "3":
                        image3.setImageDrawable(m3);
                        break;
                    case "4":
                        image3.setImageDrawable(m4);
                        break;
                    case "5":
                        image3.setImageDrawable(m5);
                        break;
                    default:
                        image3.setImageDrawable(mx);
                        break;
                }

                // Prepare string_2
                switch (string_2) {
                    case "0":
                        image2.setImageDrawable(m0);
                        break;
                    case "1":
                        image2.setImageDrawable(m1);
                        break;
                    case "2":
                        image2.setImageDrawable(m2);
                        break;
                    case "3":
                        image2.setImageDrawable(m3);
                        break;
                    case "4":
                        image2.setImageDrawable(m4);
                        break;
                    case "5":
                        image2.setImageDrawable(m5);
                        break;
                    default:
                        image2.setImageDrawable(mx);
                        break;
                }

                // Prepare string_1
                switch (string_1) {
                    case "0":
                        image1.setImageDrawable(r0);
                        break;
                    case "1":
                        image1.setImageDrawable(r1);
                        break;
                    case "2":
                        image1.setImageDrawable(r2);
                        break;
                    case "3":
                        image1.setImageDrawable(r3);
                        break;
                    case "4":
                        image1.setImageDrawable(r4);
                        break;
                    case "5":
                        image1.setImageDrawable(r5);
                        break;
                    default:
                        image1.setImageDrawable(rx);
                        break;
                }

                // Prepare fret
                switch (fret) {
                    case "1":
                        image0.setImageDrawable(f1);
                        break;
                    case "2":
                        image0.setImageDrawable(f2);
                        break;
                    case "3":
                        image0.setImageDrawable(f3);
                        break;
                    case "4":
                        image0.setImageDrawable(f4);
                        break;
                    case "5":
                        image0.setImageDrawable(f5);
                        break;
                    case "6":
                        image0.setImageDrawable(f6);
                        break;
                    case "7":
                        image0.setImageDrawable(f7);
                        break;
                    case "8":
                        image0.setImageDrawable(f8);
                        break;
                    case "9":
                        image0.setImageDrawable(f9);
                        break;
                    default:
                        image0 = null;
                        break;
                }

                chordview.addView(chordname);
                if (image0 != null) {
                    chordview.addView(image0);
                }
                chordview.addView(image6);
                chordview.addView(image5);
                chordview.addView(image4);
                chordview.addView(image3);
                chordview.addView(image2);
                chordview.addView(image1);

            } else if (chordInstrument.equals("B")) {

                if (chordnotes.length() > 0) {
                    string_5 = chordnotes.substring(0, 1);
                }
                if (chordnotes.length() > 1) {
                    string_4 = chordnotes.substring(1, 2);
                }
                if (chordnotes.length() > 2) {
                    string_3 = chordnotes.substring(2, 3);
                }
                if (chordnotes.length() > 3) {
                    string_2 = chordnotes.substring(3, 4);
                }
                if (chordnotes.length() > 4) {
                    string_1 = chordnotes.substring(4, 5);
                }
                if (chordnotes.length() > 6) {
                    fret = chordnotes.substring(6, 7);
                }

                // Prepare string_5
                switch (string_5) {
                    case "0":
                        image5.setImageDrawable(l0);
                        break;
                    case "1":
                        image5.setImageDrawable(l1);
                        break;
                    case "2":
                        image5.setImageDrawable(l2);
                        break;
                    case "3":
                        image5.setImageDrawable(l3);
                        break;
                    case "4":
                        image5.setImageDrawable(l4);
                        break;
                    case "5":
                        image5.setImageDrawable(l5);
                        break;
                    default:
                        image5.setImageDrawable(lx);
                        break;
                }

                // Prepare string_4
                switch (string_4) {
                    case "0":
                        image4.setImageDrawable(m0);
                        break;
                    case "1":
                        image4.setImageDrawable(m1);
                        break;
                    case "2":
                        image4.setImageDrawable(m2);
                        break;
                    case "3":
                        image4.setImageDrawable(m3);
                        break;
                    case "4":
                        image4.setImageDrawable(m4);
                        break;
                    case "5":
                        image4.setImageDrawable(m5);
                        break;
                    default:
                        image4.setImageDrawable(mx);
                        break;
                }

                // Prepare string_3
                switch (string_3) {
                    case "0":
                        image3.setImageDrawable(m0);
                        break;
                    case "1":
                        image3.setImageDrawable(m1);
                        break;
                    case "2":
                        image3.setImageDrawable(m2);
                        break;
                    case "3":
                        image3.setImageDrawable(m3);
                        break;
                    case "4":
                        image3.setImageDrawable(m4);
                        break;
                    case "5":
                        image3.setImageDrawable(m5);
                        break;
                    default:
                        image3.setImageDrawable(mx);
                        break;
                }

                // Prepare string_2
                switch (string_2) {
                    case "0":
                        image2.setImageDrawable(m0);
                        break;
                    case "1":
                        image2.setImageDrawable(m1);
                        break;
                    case "2":
                        image2.setImageDrawable(m2);
                        break;
                    case "3":
                        image2.setImageDrawable(m3);
                        break;
                    case "4":
                        image2.setImageDrawable(m4);
                        break;
                    case "5":
                        image2.setImageDrawable(m5);
                        break;
                    default:
                        image2.setImageDrawable(mx);
                        break;
                }

                // Prepare string_1
                switch (string_1) {
                    case "0":
                        image1.setImageDrawable(r0);
                        break;
                    case "1":
                        image1.setImageDrawable(r1);
                        break;
                    case "2":
                        image1.setImageDrawable(r2);
                        break;
                    case "3":
                        image1.setImageDrawable(r3);
                        break;
                    case "4":
                        image1.setImageDrawable(r4);
                        break;
                    case "5":
                        image1.setImageDrawable(r5);
                        break;
                    default:
                        image1.setImageDrawable(rx);
                        break;
                }

                // Prepare fret
                switch (fret) {
                    case "1":
                        image0.setImageDrawable(f1);
                        break;
                    case "2":
                        image0.setImageDrawable(f2);
                        break;
                    case "3":
                        image0.setImageDrawable(f3);
                        break;
                    case "4":
                        image0.setImageDrawable(f4);
                        break;
                    case "5":
                        image0.setImageDrawable(f5);
                        break;
                    case "6":
                        image0.setImageDrawable(f6);
                        break;
                    case "7":
                        image0.setImageDrawable(f7);
                        break;
                    case "8":
                        image0.setImageDrawable(f8);
                        break;
                    case "9":
                        image0.setImageDrawable(f9);
                        break;
                    default:
                        image0 = null;
                        break;
                }

                chordview.addView(chordname);
                if (image0 != null) {
                    chordview.addView(image0);
                }
                chordview.addView(image5);
                chordview.addView(image4);
                chordview.addView(image3);
                chordview.addView(image2);
                chordview.addView(image1);

            } else if (chordInstrument.equals("u") || chordInstrument.equals("m") || chordInstrument.equals("c") || chordInstrument.equals("b")) {
                if (chordnotes.length() > 0) {
                    string_4 = chordnotes.substring(0, 1);
                }
                if (chordnotes.length() > 1) {
                    string_3 = chordnotes.substring(1, 2);
                }
                if (chordnotes.length() > 2) {
                    string_2 = chordnotes.substring(2, 3);
                }
                if (chordnotes.length() > 3) {
                    string_1 = chordnotes.substring(3, 4);
                }
                if (chordnotes.length() > 5) {
                    fret = chordnotes.substring(5, 6);
                }

                // Prepare string_4
                switch (string_4) {
                    case "0":
                        image4.setImageDrawable(l0);
                        break;
                    case "1":
                        image4.setImageDrawable(l1);
                        break;
                    case "2":
                        image4.setImageDrawable(l2);
                        break;
                    case "3":
                        image4.setImageDrawable(l3);
                        break;
                    case "4":
                        image4.setImageDrawable(l4);
                        break;
                    default:
                        image4.setImageDrawable(lx);
                        break;
                }

                // Prepare string_3
                switch (string_3) {
                    case "0":
                        image3.setImageDrawable(m0);
                        break;
                    case "1":
                        image3.setImageDrawable(m1);
                        break;
                    case "2":
                        image3.setImageDrawable(m2);
                        break;
                    case "3":
                        image3.setImageDrawable(m3);
                        break;
                    case "4":
                        image3.setImageDrawable(m4);
                        break;
                    case "5":
                        image3.setImageDrawable(m5);
                        break;
                    default:
                        image3.setImageDrawable(mx);
                        break;
                }

                // Prepare string_2
                switch (string_2) {
                    case "0":
                        image2.setImageDrawable(m0);
                        break;
                    case "1":
                        image2.setImageDrawable(m1);
                        break;
                    case "2":
                        image2.setImageDrawable(m2);
                        break;
                    case "3":
                        image2.setImageDrawable(m3);
                        break;
                    case "4":
                        image2.setImageDrawable(m4);
                        break;
                    case "5":
                        image2.setImageDrawable(m5);
                        break;
                    default:
                        image2.setImageDrawable(mx);
                        break;
                }

                // Prepare string_1
                switch (string_1) {
                    case "0":
                        image1.setImageDrawable(r0);
                        break;
                    case "1":
                        image1.setImageDrawable(r1);
                        break;
                    case "2":
                        image1.setImageDrawable(r2);
                        break;
                    case "3":
                        image1.setImageDrawable(r3);
                        break;
                    case "4":
                        image1.setImageDrawable(r4);
                        break;
                    case "5":
                        image1.setImageDrawable(r5);
                        break;
                    default:
                        image1.setImageDrawable(rx);
                        break;
                }

                // Prepare fret
                switch (fret) {
                    case "1":
                        image0.setImageDrawable(f1);
                        break;
                    case "2":
                        image0.setImageDrawable(f2);
                        break;
                    case "3":
                        image0.setImageDrawable(f3);
                        break;
                    case "4":
                        image0.setImageDrawable(f4);
                        break;
                    case "5":
                        image0.setImageDrawable(f5);
                        break;
                    case "6":
                        image0.setImageDrawable(f6);
                        break;
                    case "7":
                        image0.setImageDrawable(f7);
                        break;
                    case "8":
                        image0.setImageDrawable(f8);
                        break;
                    case "9":
                        image0.setImageDrawable(f9);
                        break;
                    default:
                        image0 = null;
                        break;
                }

                chordview.addView(chordname);
                if (image0 != null) {
                    chordview.addView(image0);
                }
                chordview.addView(image4);
                chordview.addView(image3);
                chordview.addView(image2);
                chordview.addView(image1);
            }

            if (chordnotes != null && !chordnotes.contains("xxxx_") && !chordnotes.contains("xxxxxx_")) {
                chordimageshere.addView(chordview);
                String text = unique_chords.get(l) + iscustom;
                chordname.setText(text);
                chordname.setTextColor(0xff000000);
                chordname.setTextSize(20);
            }
        }
    }

    private void promptNewFolder() {
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
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(newfoldername, InputMethodManager.SHOW_IMPLICIT);

        newfolderprompt.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Get the text for the new sticky note
                        final String tempnewfoldername;
                        if (newfoldername.getText().toString() == null) {
                            tempnewfoldername = "";
                        } else {
                            tempnewfoldername = newfoldername.getText().toString();
                        }
                        if (tempnewfoldername.length() > 0 && !tempnewfoldername.contains("/") && !tempnewfoldername.contains(".")) {

                            // Make the folder
                            File newsongdir = new File(dir.getAbsolutePath() + "/" + tempnewfoldername);
                            if (!newsongdir.exists()) {
                                // Tell the user we're creating the Songs directory
                                if (newsongdir.mkdirs()) {
                                    myToastMessage = getResources().getString(R.string.songfoldercreate) + " - " + tempnewfoldername;
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

    private void editFolderName() {
        // First set the browsing directory back to the main one
        currentFolder = whichSongFolder;
        newFolder = whichSongFolder;
        whichSongFolder = mainfoldername;
        ListSongFiles.listSongs();

        // Build a dialogue window and related bits that get modified/shown if needed
        AlertDialog.Builder folderdialogBuilder = new AlertDialog.Builder(FullscreenActivity.this);
        LinearLayout foldertitleLayout = new LinearLayout(FullscreenActivity.this);
        foldertitleLayout.setOrientation(LinearLayout.VERTICAL);
        TextView f_titleView = new TextView(FullscreenActivity.this);
        f_titleView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        f_titleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        f_titleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white));
        f_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        f_titleView.setText(getResources().getString(R.string.options_song_editfolder));
        foldertitleLayout.addView(f_titleView);
        folderdialogBuilder.setCustomTitle(foldertitleLayout);
        final EditText f_editbox = new EditText(FullscreenActivity.this);
        f_editbox.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        f_editbox.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        f_editbox.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white));
        f_editbox.setBackgroundColor(FullscreenActivity.this.getResources().getColor(android.R.color.darker_gray));
        f_editbox.setHint(getResources().getString(R.string.newfoldername));
        f_editbox.requestFocus();
        folderdialogBuilder.setView(f_editbox);
        folderdialogBuilder.setSingleChoiceItems(mSongFolderNames, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (arg1 == 0) {
                    myToastMessage = getResources().getString(R.string.not_allowed);
                    ShowToast.showToast(FullscreenActivity.this);
                    currentFolder = "";
                } else {
                    currentFolder = mSongFolderNames[arg1];
                }
                f_editbox.setText(mSongFolderNames[arg1]);
            }
        });

        folderdialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        String newFolderTitle = f_editbox.getText().toString();
                        if (currentFolder.length() > 0) {
                            // Isn't main folder, so allow rename
                            File from = new File(dir + "/" + currentFolder);
                            File to = new File(dir + "/" + newFolderTitle);
                            from.renameTo(to);

                            // Load the songs
                            ListSongFiles.listSongs();
                            prepareSongMenu();
                            mDrawerLayout.closeDrawer(expListViewOption);
                            mDrawerLayout.openDrawer(expListViewSong);
                        } else {
                            myToastMessage = getResources().getString(R.string.not_allowed);
                            ShowToast.showToast(FullscreenActivity.this);
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

    public void showCurrentSet(View view) {
        // Use the stored mySet variable to make a new list for the Options menu.
        // Set the main variable to say we are viewing a set - setView = "Y";
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

    private void redrawTheLyricsTable(View view) {

        isPDF = false;
        File checkfile;
        if (whichSongFolder.equals(mainfoldername)) {
            checkfile = new File(dir + "/" + songfilename);
        } else {
            checkfile = new File(dir + "/" + whichSongFolder + "/" + songfilename);
        }

        if (songfilename != null && (songfilename.contains(".pdf") || songfilename.contains(".PDF")) && checkfile.exists()) {
            // File is pdf
            isPDF = true;
            isSong = false;
        } else if (whichSongFolder.equals("../Scripture/_cache") || whichSongFolder.equals("../Images/_cache") || whichSongFolder.equals("../Slides/_cache") || whichSongFolder.equals("../Notes/_cache")) {
            // File is slide, note, image or scripture
            isPDF = false;
            isSong = false;
        } else {
            isPDF = false;
            isSong = true;
        }
        wasshowing_pdfselectpage = View.GONE;
        wasshowing_stickynotes = View.INVISIBLE;

        // Show the ActionBar
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);

        if (ab != null) {
            ab.show();
        }

        if (hideactionbaronoff.equals("Y")) {
            delayactionBarHide.postDelayed(hideActionBarRunnable, 1000);
        }
        needtoredraw = true;
        alreadyloaded = false;
        mScreenOrientation = getResources().getConfiguration().orientation;
        columnTest = 1;

        // Set up and down arrows to invisible
        if (toggleScrollArrows.equals("D")) {
            uparrow_bottom.setVisibility(View.INVISIBLE);
            downarrow_top.setVisibility(View.INVISIBLE);
        } else {
            uparrow_bottom.setVisibility(View.GONE);
            downarrow_top.setVisibility(View.GONE);
        }
        uparrow_top.setVisibility(View.INVISIBLE);
        downarrow_bottom.setVisibility(View.INVISIBLE);
        songLoadingProgressBar.setVisibility(View.VISIBLE);
        songTitleHolder.setVisibility(View.VISIBLE);

        // Make sure the window is scrolled to the top for the new song
        scrollpage.smoothScrollTo(0, 0);

        songTitleHolder.setText(songfilename);
        songTitleHolder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein));
        songLoadingProgressBar.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein));

        // Get the song index numbers
        ListSongFiles.listSongs();
        ListSongFiles.getCurrentSongIndex();

        if (whichDirection.equals("L2R")) {
            if (scrollpage_pdf.getVisibility() == View.VISIBLE) {
                scrollpage_pdf.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
            } else if (scrollpage_onecol.getVisibility() == View.VISIBLE) {
                scrollpage_onecol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
            } else if (scrollpage_twocol.getVisibility() == View.VISIBLE) {
                scrollpage_twocol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
            } else if (scrollpage_threecol.getVisibility() == View.VISIBLE) {
                scrollpage_threecol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
            }
        } else {
            if (scrollpage_pdf.getVisibility() == View.VISIBLE) {
                scrollpage_pdf.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
            } else if (scrollpage_onecol.getVisibility() == View.VISIBLE) {
                scrollpage_onecol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
            } else if (scrollpage_twocol.getVisibility() == View.VISIBLE) {
                scrollpage_twocol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
            } else if (scrollpage_threecol.getVisibility() == View.VISIBLE) {
                scrollpage_threecol.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
            }
        }
        // Wait 500ms before clearing the table and loading the new lyrics
        Handler delayfadeinredraw = new Handler();
        delayfadeinredraw.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (toggleScrollArrows.equals("D")) {
                    uparrow_bottom.setVisibility(View.INVISIBLE);
                    downarrow_top.setVisibility(View.INVISIBLE);
                } else {
                    uparrow_bottom.setVisibility(View.GONE);
                    downarrow_top.setVisibility(View.GONE);
                }
                uparrow_top.setVisibility(View.INVISIBLE);
                downarrow_bottom.setVisibility(View.INVISIBLE);

                myXML = null;
                myXML = "";

                scrollpage_onecol.setVisibility(View.INVISIBLE);
                scrollpage_twocol.setVisibility(View.INVISIBLE);
                scrollpage_threecol.setVisibility(View.INVISIBLE);
                lyricstable_onecolview.setVisibility(View.INVISIBLE);
                lyricstable_twocolview.setVisibility(View.INVISIBLE);
                lyricstable_threecolview.setVisibility(View.INVISIBLE);

                lyricstable_onecolview.removeAllViews();
                lyricstable_twocolview.removeAllViews();
                lyricstable2_twocolview.removeAllViews();
                lyricstable_threecolview.removeAllViews();
                lyricstable2_threecolview.removeAllViews();
                lyricstable3_threecolview.removeAllViews();
                chordimageshere.removeAllViews();

                try {
                    showLyrics(main_page);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, slideout_time);

        doScaling = false;

        // Set a runnable to check the scroll position after 1 second
        delaycheckscroll.postDelayed(checkScrollPosition, checkscroll_time);

        // Try to open the appropriate Song folder on the left menu
        for (int z = 0; z < listDataHeaderSong.size() - 1; z++) {
            if (listDataHeaderSong.get(z).equals(whichSongFolder)) {
                expListViewSong.expandGroup(z);
            }
        }

        if (mySticky.getVisibility() == View.VISIBLE) {
            scrollstickyholder.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
            stickynotes.setAlpha(0.4f);
            // After 500ms, make them invisible
            Handler delayhidenote = new Handler();
            delayhidenote.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mySticky.setVisibility(View.INVISIBLE);
                    scrollstickyholder.setVisibility(View.GONE);
                }
            }, 500);
        }
    }

    private void searchYouTube() {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=" + mTitle + "+" + mAuthor)));
    }

    public void doMoveInSet() {
        invalidateOptionsMenu();
        linkclicked = mSetList[indexSongInSet];
        if (linkclicked == null) {
            linkclicked = "";
        }
        pdfPageCurrent = 0;
        if (!linkclicked.contains("/")) {
            // Right it doesn't, so add the /
            linkclicked = "/" + linkclicked;
        }

        // Now split the linkclicked into two song parts 0=folder 1=file
        String[] songpart = linkclicked.split("/");
        // If the folder length isn't 0, it is a folder
        if (songpart[0].length() > 0 && !songpart[0].contains(text_scripture) && !songpart[0].contains(image) && !songpart[0].contains(text_slide) && !songpart[0].contains(text_note)) {
            whichSongFolder = songpart[0];

        } else if (songpart[0].length() > 0 && songpart[0].contains(text_scripture) && !songpart[0].contains(image) && !songpart[0].contains(text_slide) && !songpart[0].contains(text_note)) {
            whichSongFolder = "../Scripture/_cache";
            songpart[0] = "../Scripture/_cache";

        } else if (songpart[0].length() > 0 && songpart[0].contains(text_slide) && !songpart[0].contains(image) && !songpart[0].contains(text_note) && !songpart[0].contains(text_scripture)) {
            whichSongFolder = "../Slides/_cache";
            songpart[0] = "../Slides/_cache";

        } else if (songpart[0].length() > 0 && !songpart[0].contains(text_slide) && !songpart[0].contains(image) && songpart[0].contains(text_note) && !songpart[0].contains(text_scripture)) {
            whichSongFolder = "../Notes/_cache";
            songpart[0] = "../Notes/_cache";

        } else if (songpart[0].length() > 0 && !songpart[0].contains(text_slide) && songpart[0].contains(image) && !songpart[0].contains(text_note) && !songpart[0].contains(text_scripture)) {
            whichSongFolder = "../Images/_cache";
            songpart[0] = "../Images/_cache";

        } else {
            whichSongFolder = mainfoldername;
        }

        // Save the preferences
        Preferences.savePreferences();

        // Match the song folder
        ListSongFiles.listSongs();
        mDrawerLayout.closeDrawer(expListViewSong);
        // Redraw the Lyrics View
        songfilename = null;
        songfilename = "";
        songfilename = songpart[1];
        redrawTheLyricsTable(view);
    }

    private void onSongImport() {
        // Give an alert box that asks the user to specify the backup file (must be in OpenSong/folder)
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FullscreenActivity.this);
        LinearLayout titleLayout = new LinearLayout(FullscreenActivity.this);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        TextView m_titleView = new TextView(FullscreenActivity.this);
        TextView m_subtitleView = new TextView(FullscreenActivity.this);
        m_titleView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        m_subtitleView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        m_titleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Large);
        m_subtitleView.setTextAppearance(FullscreenActivity.this, android.R.style.TextAppearance_Medium);
        m_titleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white));
        m_subtitleView.setTextColor(FullscreenActivity.this.getResources().getColor(android.R.color.white));
        m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        m_subtitleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        m_titleView.setText(getResources().getString(R.string.import_onsong_choose));
        m_subtitleView.setText(getResources().getString(R.string.onsonglocation));
        titleLayout.addView(m_titleView);
        titleLayout.addView(m_subtitleView);
        dialogBuilder.setCustomTitle(titleLayout);
        // List files ending with .backup in homedir
        ArrayList<String> backups = new ArrayList<>();
        File[] backupfilecheck = homedir.listFiles();

        if (backupfilecheck!=null) {
            for (File aBackupfilecheck : backupfilecheck) {
                if (aBackupfilecheck.isFile() && aBackupfilecheck.getPath().endsWith(".backup")) {
                    backups.add(aBackupfilecheck.getName());
                }
            }
        }
        if (backups.size() > 0) {
            backUpFiles = new String[backups.size()];
            for (int r = 0; r < backups.size(); r++) {
                backUpFiles[r] = backups.get(r);
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
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Load the backup as an async task
                if (backupchosen!=null && !backupchosen.isEmpty()) {
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
                StorageChooser.createDirectory(dironsong);
            }
            InputStream is;
            ZipInputStream zis;
            try {
                String filename;
                is = new FileInputStream(homedir + "/" + backupchosen);
                Log.d("backup", "is=" + is);
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;
                byte[] buffer = new byte[1024];
                int count;

                while ((ze = zis.getNextEntry()) != null) {
                    filename = ze.getName();
                    FileOutputStream fout;
                    if (filename.equals("OnSong.Backup.sqlite3")) {
                        fout = new FileOutputStream(homedir + "/" + filename);
                    } else {
                        fout = new FileOutputStream(dironsong + "/" + filename);
                    }
                    while ((count = zis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                    Log.d("onsongbackup", "filename=" + ze);
                    fout.close();
                    zis.closeEntry();
                }
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
                return backupchosen;
            }

            File dbfile = new File(homedir + "/OnSong.Backup.sqlite3");
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
            // Go through each row and read in the content field
            // Save the files with the .onsong extension

            String query = "SELECT * FROM Song";

            //Cursor points to a location in your results
            Cursor cursor = db.rawQuery(query, null);

            // Move to first row
            cursor.moveToFirst();

            String str_title;
            String str_content;

            while (cursor.moveToNext()) {
                // Extract data.
                str_title = cursor.getString(cursor.getColumnIndex("title"));
                // Make sure title doesn't have /
                str_title = str_title.replace("/", "_");
                str_title = TextUtils.htmlEncode(str_title);
                str_content = cursor.getString(cursor.getColumnIndex("content"));

                try {
                    // Now write the modified song
                    FileOutputStream overWrite = new FileOutputStream(dironsong + "/" + str_title + ".onsong", false);
                    overWrite.write(str_content.getBytes());
                    overWrite.flush();
                    overWrite.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            return "doneit";

        }

        @Override
        protected void onPostExecute(String doneit) {
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

        orientationchanged = mScreenOrientation != newConfig.orientation;
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
        mDrawerLayout.closeDrawer(expListViewSong);
        mDrawerLayout.closeDrawer(expListViewOption);
        if (orientationchanged) {
            invalidateOptionsMenu();
            mDrawerLayout.closeDrawers();
            resizeDrawers();
            redrawTheLyricsTable(main_page);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        actionbarbutton = true;
        // call ActionBarDrawerToggle.onOptionsItemSelected(), if it returns true
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
                                PresenterMode.class);
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

                case R.id.song_share:
                    shareSong();
                    return true;

                case R.id.action_fullsearch:
                    if (!safetosearch) {
                        myToastMessage = getResources().getString(R.string.search_index_start)+"\n\n"+getResources().getString(R.string.wait);
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    Intent intent = new Intent();
                    intent.setClass(FullscreenActivity.this, SearchViewFilterModeNew.class);
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
                    if (isSong || isPDF) {
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

                        fixSetActionButtons();

                        // Hide the menus - 1 second after opening the Option menu,
                        // close it (1000ms total)
                        Handler optionMenuFlickClosed = new Handler();
                        optionMenuFlickClosed.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDrawerLayout.closeDrawer(expListViewOption);
                            }
                        }, 1000);
                        return true;
                    }

                case R.id.pad_menu_button:
                    // Run the pad start/stop, but only if song isn't a pdf!
                    if (!isPDF && isSong) {
                        try {
                            togglePlayPads(padButton);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    } else {
                        myToastMessage = getResources().getString(R.string.not_allowed);
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;

                case R.id.link_menu_button:
                    // Run the link menu popup page
                    popupLink(linkButton);
                    return true;

                case R.id.autoscroll_menu_button:
                    // Run the autoscroll start/stop, but only if song isn't a pdf!
                    if (!isPDF && isSong) {
                        autoScroll(autoscrollButton);
                    } else {
                        myToastMessage = getResources().getString(R.string.not_allowed);
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;

                case R.id.metronome_menu_button:
                    // Run the metronome start/stop, but only if the song insn't a pdf!
                    if (!isPDF && isSong) {
                        metronomeToggle(metronomeButton);
                    } else {
                        myToastMessage = getResources().getString(R.string.not_allowed);
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;

                case R.id.stickynotes_menu_button:
                    // Hide/show the sticky notes, but only if song isn't a pdf!
                    if (!isPDF && isSong) {
                        stickyNotes(stickynotes);
                    } else {
                        myToastMessage = getResources().getString(R.string.not_allowed);
                        ShowToast.showToast(FullscreenActivity.this);
                    }
                    return true;

                case R.id.chords_menu_button:
                    // Hide/show the chords, but only if song isn't a pdf!
                    if (!isPDF && isSong) {
                        popupChords_toggle(chordButton);
                    } else {
                        myToastMessage = getResources().getString(R.string.not_allowed);
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
                        }, delayswipe_time);

                        // Set the swipe direction to right to left
                        whichDirection = "L2R";
                        if (isPDF && pdfPageCurrent > 0) {
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
                        }, delayswipe_time);

                        // Set the swipe direction to right to left
                        whichDirection = "R2L";
                        if (isPDF && pdfPageCurrent < (pdfPageCount - 1)) {
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
            // 5 = start/stop autoscroll
            // 6 = start/stop pad
            // 7 = start/stop metronome
            // 0/else = off (highest menu item)


            // First test conditions
            if (!padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow_top.isFocused() && !downarrow_bottom.isFocused() && !uparrow_bottom.isFocused() && !uparrow_top.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility() != View.VISIBLE && popupAutoscroll.getVisibility() != View.VISIBLE
                    && popupMetronome.getVisibility() != View.VISIBLE && scrollstickyholder.getVisibility() != View.VISIBLE
                    && popupChord.getVisibility() != View.VISIBLE) {

                // Now find out which gesture we've gone for
                if (gesture_doubletap.equals("1")) {
                    gesture1();

                } else if (gesture_doubletap.equals("2")) {
                    if (isPDF) {
                        // Can't do this action on a pdf!
                        myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                        ShowToast.showToast(FullscreenActivity.this);
                    } else if (!isSong) {
                        // Editing a slide / note / scripture / image
                        myToastMessage = getResources().getString(R.string.not_allowed);
                        ShowToast.showToast(FullscreenActivity.this);
                    } else {
                        gesture2();
                    }

                } else if (gesture_doubletap.equals("3")) {
                    gesture3();

                } else if (gesture_doubletap.equals("4")) {
                    gesture4();

                } else if (gesture_doubletap.equals("5")) {
                    gesture5();

                } else if (gesture_doubletap.equals("6")) {
                    gesture6();

                } else if (gesture_doubletap.equals("7")) {
                    gesture7();
                }

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Decide what the long press action is
            // 1 = both menus
            // 2 = edit song
            // 3 = add to set
            // 4 = redraw
            // 5 = start/stop autoscroll
            // 6 = start/stop pad
            // 7 = start/stop metronome
            // 0/else = off (highest menu item)


            // First test conditions
            if (!padButton.isFocused() && !linkButton.isFocused() && !autoscrollButton.isFocused()
                    && !pdf_selectpage.isFocused() && !metronomeButton.isFocused() && !stickynotes.isFocused()
                    && !chordButton.isFocused() && !downarrow_top.isFocused() && !downarrow_bottom.isFocused() && !uparrow_bottom.isFocused() && !uparrow_top.isFocused()
                    && !mDrawerLayout.isDrawerOpen(expListViewSong) && !mDrawerLayout.isDrawerVisible(expListViewSong)
                    && !mDrawerLayout.isDrawerOpen(expListViewOption) && !mDrawerLayout.isDrawerVisible(expListViewOption)
                    && popupPad.getVisibility() != View.VISIBLE && popupAutoscroll.getVisibility() != View.VISIBLE
                    && popupMetronome.getVisibility() != View.VISIBLE && scrollstickyholder.getVisibility() != View.VISIBLE
                    && popupChord.getVisibility() != View.VISIBLE) {

                // Now find out which gesture we've gone for
                switch (gesture_longpress) {
                    case "1":
                        gesture1();

                        break;
                    case "2":
                        if (isPDF) {
                            // Can't do this action on a pdf!
                            myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                            ShowToast.showToast(FullscreenActivity.this);
                        } else if (!isSong) {
                            // Editing a slide / note / scripture / image
                            myToastMessage = getResources().getString(R.string.not_allowed);
                            ShowToast.showToast(FullscreenActivity.this);
                        } else {
                            gesture2();
                        }

                        break;
                    case "3":
                        gesture3();

                        break;
                    case "4":
                        gesture4();

                        break;
                    case "5":
                        gesture5();

                        break;
                    case "6":
                        gesture6();

                        break;
                    case "7":
                        gesture7();
                        break;
                }
            }
            super.onLongPress(e);
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
                return false; // Currently disabled swiping to let screen finish drawing.
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

                if (isPDF && pdfPageCurrent < (pdfPageCount - 1)) {
                    pdfPageCurrent = pdfPageCurrent + 1;
                    redrawTheLyricsTable(main_page);
                    return false;
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
                    }, delayswipe_time);

                }
                // If not in a set, see if we can move to the next song in the list
                // Only if swipeSet is Y (not S)
                if (setView.equals("N") && swipeSet.equals("Y") && !whichSongFolder.equals("../Scripture/_cache") && !whichSongFolder.equals("../Images/_cache") && !whichSongFolder.equals("../Slides/_cache") && !whichSongFolder.equals("../Notes/_cache")) {
                    if (nextSongIndex < mSongFileNames.length && nextSongIndex != -1 && !songfilename.equals(mSongFileNames[nextSongIndex])) {
                        // Move to the next song
                        // temporarily disable swipe
                        tempswipeSet = "disable";

                        songfilename = mSongFileNames[nextSongIndex];
                        redrawTheLyricsTable(view);

                        // Set a runnable to reset swipe back to original value after 1 second
                        Handler delayfadeinredraw = new Handler();
                        delayfadeinredraw.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tempswipeSet = "enable";
                            }
                        }, delayswipe_time);
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

                if (isPDF && pdfPageCurrent > 0) {
                    pdfPageCurrent = pdfPageCurrent - 1;
                    redrawTheLyricsTable(main_page);
                    return false;
                } else {
                    pdfPageCurrent = 0;
                }

                // If we are viewing a set, move to the previous song.
                if (setSize > 2 && setView.equals("Y") && indexSongInSet >= 1) {
                    // temporarily disable swipe
                    tempswipeSet = "disable";

                    indexSongInSet -= 1;
                    doMoveInSet();
                    // Set a runnable to reset swipe back to original value after 1 second
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tempswipeSet = "enable";
                        }
                    }, delayswipe_time);
                }
                // If not in a set, see if we can move to the previous song in the list
                // Only if swipeSet is Y (not S)
                if (setView.equals("N") && swipeSet.equals("Y") && !whichSongFolder.equals("../Scripture/_cache") && !whichSongFolder.equals("../Images/_cache") && !whichSongFolder.equals("../Slides/_cache") && !whichSongFolder.equals("../Notes/_cache")) {

                    if (previousSongIndex < mSongFileNames.length && previousSongIndex != -1 && !songfilename.equals(mSongFileNames[previousSongIndex])) {
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
                        }, delayswipe_time);
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class simpleOnScaleGestureListener extends
            SimpleOnScaleGestureListener {

    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        // WOULD BE BETTER IF THIS WAS CALLED ON SOME KIND OF ONSCROLL LISTENER
        scaleGestureDetector.onTouchEvent(ev);
        switch (action) {

            case (MotionEvent.ACTION_MOVE):
                // Set a runnable to check the scroll position
                newPosFloat = (float) scrollpage.getScrollY();
                delaycheckscroll.post(checkScrollPosition);

                // Set a runnable to check the scroll position after 1 second
                delaycheckscroll.postDelayed(checkScrollPosition, checkscroll_time);
        }

        // TouchEvent dispatcher.
        if (gestureDetector != null) {
            if (!gestureDetector.onTouchEvent(ev)) {
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

    public void findSongInFolder() {
        // Try to open the appropriate Song folder on the left menu
        expListViewSong.setFastScrollEnabled(false);
        for (int z = 0; z < listDataHeaderSong.size() - 1; z++) {
            if (listDataHeaderSong.get(z).equals(whichSongFolder)) {
                expListViewSong.expandGroup(z);
            }
        }
        expListViewSong.setSelection(currentSongIndex);
    }

    public void writeSongToPage() {

        // Try to set the capokey to null so it is figured out
        capokey = null;

        lyricstable_onecolview.removeAllViews();
        lyricstable_twocolview.removeAllViews();
        lyricstable2_twocolview.removeAllViews();
        lyricstable_threecolview.removeAllViews();
        lyricstable2_threecolview.removeAllViews();
        lyricstable3_threecolview.removeAllViews();

        // columnTest lets us know what we are doing
        // if columTest = 0 or 1, prepare 1 column view
        // if columTest = 0 or 2, prepare 1 column view
        // if columTest = 0 or 3, prepare 1 column view

        if (whichSongFolder == null) {
            whichSongFolder = "";
        }
        if (whichSongFolder.contains("../Images/")) {
            columnTest = 1;
        }

        int startatline = 0;
        int endatline = numrowstowrite;
        for (int x = startatline; x < endatline; x++) {
            int n;
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
            // x = current line
            // n = previous line (or x if at the beginning)
            // m = next line (or x if at the end)

            // Decide where the written stuff can go
            boolean writetocol1_1 = true;
            boolean writetocol1_2 = false;
            boolean writetocol2_2 = false;
            boolean writetocol1_3 = false;
            boolean writetocol2_3 = false;
            boolean writetocol3_3 = false;

            if (x < splitpoint) {
                writetocol1_2 = true;
            } else {
                writetocol2_2 = true;
            }
            if (x < thirdsplitpoint) {
                writetocol1_3 = true;
            } else if (x >= thirdsplitpoint && x < twothirdsplitpoint) {
                writetocol2_3 = true;
            } else {
                writetocol3_3 = true;
            }

            chords_useThisTextSize = tempfontsize;
            lyrics_useThisTextSize = tempfontsize;
            lyrics_useThisFont = lyricsfont;

            if (x == 0) {
                // This is the first line.
                // If in a set view and option is on to show at top, show the title of the next song
                // If we are showing the last song already, say this instead
                if (setView.equals("Y") && showNextInSet.equals("top")) {
                    // Get next title in set
                    String next_title = getResources().getString(R.string.lastsong);
                    if (setSize >= 2 && indexSongInSet >= 0 && indexSongInSet < (setSize - 1)) {
                        next_title = getResources().getString(R.string.next) + ": " + mSetList[indexSongInSet + 1];
                    }
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (writetocol1_1 || columnTest == 1 || columnTest == 0) {
                        RelativeLayout nextInSetBox1_1 = new RelativeLayout(this);
                        nextInSetBox1_1.setLayoutParams(lp);
                        nextInSetBox1_1.setHorizontalGravity(Gravity.RIGHT);
                        TextView nextSongText1_1 = new TextView(this);
                        nextSongText1_1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        nextSongText1_1.setText(next_title);
                        nextSongText1_1.setGravity(Gravity.RIGHT);
                        nextSongText1_1.setTypeface(lyrics_useThisFont);
                        nextSongText1_1.setBackgroundColor(lyricsCommentColor);
                        nextInSetBox1_1.setBackgroundColor(lyricsCommentColor);
                        nextSongText1_1.setTextColor(lyricsTextColor);
                        nextSongText1_1.setTextSize(tempfontsize * commentfontscalesize);
                        nextInSetBox1_1.addView(nextSongText1_1);
                        lyricstable_onecolview.addView(nextInSetBox1_1);
                    }

                    if (writetocol1_2 || columnTest == 2 || columnTest == 0) {
                        RelativeLayout nextInSetBox1_2 = new RelativeLayout(this);
                        nextInSetBox1_2.setLayoutParams(lp);
                        nextInSetBox1_2.setHorizontalGravity(Gravity.RIGHT);
                        TextView nextSongText1_2 = new TextView(this);
                        nextSongText1_2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        nextSongText1_2.setText(next_title);
                        nextSongText1_2.setGravity(Gravity.RIGHT);
                        nextSongText1_2.setTypeface(lyrics_useThisFont);
                        nextSongText1_2.setBackgroundColor(lyricsCommentColor);
                        nextInSetBox1_2.setBackgroundColor(lyricsCommentColor);
                        nextSongText1_2.setTextColor(lyricsTextColor);
                        nextSongText1_2.setTextSize(tempfontsize * commentfontscalesize);
                        nextInSetBox1_2.addView(nextSongText1_2);
                        lyricstable_twocolview.addView(nextInSetBox1_2);
                    }

                    if (writetocol1_1 || columnTest == 3 || columnTest == 0) {
                        RelativeLayout nextInSetBox1_3 = new RelativeLayout(this);
                        nextInSetBox1_3.setLayoutParams(lp);
                        nextInSetBox1_3.setHorizontalGravity(Gravity.RIGHT);
                        TextView nextSongText1_3 = new TextView(this);
                        nextSongText1_3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        nextSongText1_3.setText(next_title);
                        nextSongText1_3.setGravity(Gravity.RIGHT);
                        nextSongText1_3.setTypeface(lyrics_useThisFont);
                        nextSongText1_3.setBackgroundColor(lyricsCommentColor);
                        nextInSetBox1_3.setBackgroundColor(lyricsCommentColor);
                        nextSongText1_3.setTextColor(lyricsTextColor);
                        nextSongText1_3.setTextSize(tempfontsize *commentfontscalesize);
                        nextInSetBox1_3.addView(nextSongText1_3);
                        lyricstable_threecolview.addView(nextInSetBox1_3);
                    }
                }

                // If the user wants to show sticky notes at the top of the page
                if (toggleAutoSticky.equals("T") && !mNotes.isEmpty() && !mNotes.equals("")) {
                    String notetoadd = getResources().getString(R.string.stickynotes) + ":\n" + mNotes + "\n\n";
                    RelativeLayout stickyNotes1_1 = new RelativeLayout(this);
                    RelativeLayout stickyNotes1_2 = new RelativeLayout(this);
                    RelativeLayout stickyNotes1_3 = new RelativeLayout(this);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    stickyNotes1_1.setLayoutParams(lp);
                    stickyNotes1_1.setHorizontalGravity(Gravity.LEFT);
                    stickyNotes1_2.setLayoutParams(lp);
                    stickyNotes1_2.setHorizontalGravity(Gravity.LEFT);
                    stickyNotes1_3.setLayoutParams(lp);
                    stickyNotes1_3.setHorizontalGravity(Gravity.LEFT);
                    TextView nextSongText1_1 = new TextView(this);
                    nextSongText1_1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    nextSongText1_1.setText(notetoadd);
                    nextSongText1_1.setGravity(Gravity.LEFT);
                    nextSongText1_1.setTypeface(lyrics_useThisFont);
                    nextSongText1_1.setBackgroundColor(lyricsCommentColor);
                    nextSongText1_1.setTextColor(lyricsTextColor);
                    nextSongText1_1.setTextSize(tempfontsize);
                    stickyNotes1_1.addView(nextSongText1_1);
                    TextView nextSongText1_2 = new TextView(this);
                    nextSongText1_2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    nextSongText1_2.setText(notetoadd);
                    nextSongText1_2.setGravity(Gravity.LEFT);
                    nextSongText1_2.setTypeface(lyrics_useThisFont);
                    nextSongText1_2.setBackgroundColor(lyricsCommentColor);
                    nextSongText1_2.setTextColor(lyricsTextColor);
                    nextSongText1_2.setTextSize(tempfontsize);
                    stickyNotes1_2.addView(nextSongText1_2);
                    TextView nextSongText1_3 = new TextView(this);
                    nextSongText1_3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    nextSongText1_3.setText(notetoadd);
                    nextSongText1_3.setGravity(Gravity.LEFT);
                    nextSongText1_3.setTypeface(lyrics_useThisFont);
                    nextSongText1_3.setBackgroundColor(lyricsCommentColor);
                    nextSongText1_3.setTextColor(lyricsTextColor);
                    nextSongText1_3.setTextSize(tempfontsize);
                    stickyNotes1_3.addView(nextSongText1_3);

                    // Prepare this view
                    lyricstable_onecolview.addView(stickyNotes1_1);
                    lyricstable_twocolview.addView(stickyNotes1_2);
                    lyricstable_threecolview.addView(stickyNotes1_3);
                }

                // If showCapo is true, add a comment line with the capo information
                if (showCapo && showChords.equals("Y")) {

                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    String capocustomtext = "";
                    capocustomtext = capocustomtext + getResources().getString(R.string.edit_song_capo) + " " + mCapo;
                    if (!mKey.isEmpty() && !mKey.equals("")) {
                        // set key to transpose
                        temptranspChords = mKey;
                        Transpose.capoTranspose();
                        capocustomtext = capocustomtext + " (" + temptranspChords + ")";
                    }

                    if (writetocol1_1 || columnTest == 1 || columnTest == 0) {
                        RelativeLayout myCapoBox1_1 = new RelativeLayout(this);
                        myCapoBox1_1.setLayoutParams(lp);
                        myCapoBox1_1.setHorizontalGravity(Gravity.LEFT);
                        TextView tCapoBox1_1 = new TextView(this);
                        tCapoBox1_1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        tCapoBox1_1.setText(capocustomtext);
                        tCapoBox1_1.setGravity(Gravity.LEFT);
                        tCapoBox1_1.setTypeface(lyrics_useThisFont);
                        tCapoBox1_1.setBackgroundColor(lyricsBackgroundColor);
                        tCapoBox1_1.setTextColor(lyricsCapoColor);
                        tCapoBox1_1.setTextSize(tempfontsize * commentfontscalesize);
                        myCapoBox1_1.addView(tCapoBox1_1);
                        lyricstable_onecolview.addView(myCapoBox1_1);
                    }

                    if (writetocol1_2 || columnTest == 2 || columnTest == 0) {
                        RelativeLayout myCapoBox1_2 = new RelativeLayout(this);
                        myCapoBox1_2.setLayoutParams(lp);
                        myCapoBox1_2.setHorizontalGravity(Gravity.LEFT);
                        TextView tCapoBox1_2 = new TextView(this);
                        tCapoBox1_2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        tCapoBox1_2.setText(capocustomtext);
                        tCapoBox1_2.setGravity(Gravity.LEFT);
                        tCapoBox1_2.setTypeface(lyrics_useThisFont);
                        tCapoBox1_2.setBackgroundColor(lyricsBackgroundColor);
                        tCapoBox1_2.setTextColor(lyricsCapoColor);
                        tCapoBox1_2.setTextSize(tempfontsize * commentfontscalesize);
                        myCapoBox1_2.addView(tCapoBox1_2);
                        lyricstable_twocolview.addView(myCapoBox1_2);
                    }

                    if (writetocol1_3 || columnTest == 3 || columnTest == 0) {
                        RelativeLayout myCapoBox1_3 = new RelativeLayout(this);
                        myCapoBox1_3.setLayoutParams(lp);
                        myCapoBox1_3.setHorizontalGravity(Gravity.LEFT);
                        TextView tCapoBox1_3 = new TextView(this);
                        tCapoBox1_3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        tCapoBox1_3.setText(capocustomtext);
                        tCapoBox1_3.setGravity(Gravity.LEFT);
                        tCapoBox1_3.setTypeface(lyrics_useThisFont);
                        tCapoBox1_3.setBackgroundColor(lyricsBackgroundColor);
                        tCapoBox1_3.setTextColor(lyricsCapoColor);
                        tCapoBox1_3.setTextSize(tempfontsize * commentfontscalesize);
                        myCapoBox1_3.addView(tCapoBox1_3);
                        lyricstable_threecolview.addView(myCapoBox1_3);
                    }
                }
            }

            // Decide if this line is a chord line followed by a lyric line.
            // If so, we need to split it up so the spacing is right.
            if (whatisthisline[x].equals("chords") && (whatisthisline[m].equals("lyrics") || whatisthisline[m].equals("comment"))) {

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

                // Get the colours for the block
                getBlock(whatisthisblock[x]);

                // Right, we need to create a new table inside this row to hold 2 rows - chords and lyrics
                TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT);

                TableLayout chords_n_lyrics1_1 = new TableLayout(this);
                TableLayout chords_n_lyrics1_2 = new TableLayout(this);
                TableLayout chords_n_lyrics2_2 = new TableLayout(this);
                TableLayout chords_n_lyrics1_3 = new TableLayout(this);
                TableLayout chords_n_lyrics2_3 = new TableLayout(this);
                TableLayout chords_n_lyrics3_3 = new TableLayout(this);
                TableRow capo_chords_row1_1 = new TableRow(this);
                TableRow capo_chords_row1_2 = new TableRow(this);
                TableRow capo_chords_row2_2 = new TableRow(this);
                TableRow capo_chords_row1_3 = new TableRow(this);
                TableRow capo_chords_row2_3 = new TableRow(this);
                TableRow capo_chords_row3_3 = new TableRow(this);
                TableRow chords_row1_1 = new TableRow(this);
                TableRow chords_row1_2 = new TableRow(this);
                TableRow chords_row2_2 = new TableRow(this);
                TableRow chords_row1_3 = new TableRow(this);
                TableRow chords_row2_3 = new TableRow(this);
                TableRow chords_row3_3 = new TableRow(this);

                if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                    capo_chords_row1_1.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row1_1.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row1_1.setLayoutParams(lp);
                    capo_chords_row1_1.setLayoutParams(lp);
                }

                if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                    capo_chords_row1_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row1_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row1_2.setLayoutParams(lp);
                    capo_chords_row1_2.setLayoutParams(lp);
                }

                if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                    capo_chords_row2_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row2_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row2_2.setLayoutParams(lp);
                    capo_chords_row2_2.setLayoutParams(lp);
                }

                if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                    capo_chords_row1_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row1_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row1_3.setLayoutParams(lp);
                    capo_chords_row1_3.setLayoutParams(lp);
                }

                if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                    capo_chords_row2_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row2_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row2_3.setLayoutParams(lp);
                    capo_chords_row2_3.setLayoutParams(lp);
                }

                if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                    capo_chords_row3_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row3_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                    chords_row3_3.setLayoutParams(lp);
                    capo_chords_row3_3.setLayoutParams(lp);
                }

                for (int i = 0; i < chordnum; i++) {
                    String chord;
                    tempChords = "";
                    temptranspChords = "";

                    if (i == 0) {
                        chord = myParsedLyrics[x].substring(0, chord_pos[1]);
                        tempChords = chord;
                        if (showCapo) {
                            temptranspChords = chord;
                            Transpose.capoTranspose();
                            allchordscapo = allchordscapo + " " + temptranspChords;
                        }

                    } else {
                        try {
                            chord = myParsedLyrics[x].substring(chord_pos[i], chord_pos[i + 1]);
                        } catch (Exception e) {
                            chord = " ";
                        }
                        tempChords = chord;
                        if (showCapo) {
                            temptranspChords = chord;
                            Transpose.capoTranspose();
                            allchordscapo = allchordscapo + " " + temptranspChords;
                        }
                    }

                    if (i == chordnum) {
                        chord = chord.trim();
                        temptranspChords = temptranspChords.trim();
                    }

                    // Set the appropriate formats for the chord line
                    // create a new TextView for each chord
                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        TextView t1_1 = new TextView(this);
                        TextView tcapo1_1 = new TextView(this);
                        t1_1.setText(chord);
                        t1_1.setTypeface(chordsfont);
                        t1_1.setTextSize(chords_useThisTextSize);
                        t1_1.setBackgroundColor(chords_useThisBGColor);
                        t1_1.setTextColor(lyricsChordsColor);
                        tcapo1_1.setText(temptranspChords);
                        tcapo1_1.setTypeface(chordsfont);
                        tcapo1_1.setTextSize(chords_useThisTextSize);
                        tcapo1_1.setBackgroundColor(capo_useThisBGColor);
                        tcapo1_1.setTextColor(lyricsCapoColor);
                        if (showCapo && showChords.equals("Y")) {
                            capo_chords_row1_1.addView(tcapo1_1);
                        }
                        chords_row1_1.addView(t1_1);
                    }

                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        TextView t1_2 = new TextView(this);
                        TextView tcapo1_2 = new TextView(this);
                        t1_2.setText(chord);
                        t1_2.setTypeface(chordsfont);
                        t1_2.setTextSize(chords_useThisTextSize);
                        t1_2.setBackgroundColor(chords_useThisBGColor);
                        t1_2.setTextColor(lyricsChordsColor);
                        tcapo1_2.setText(temptranspChords);
                        tcapo1_2.setTypeface(chordsfont);
                        tcapo1_2.setTextSize(chords_useThisTextSize);
                        tcapo1_2.setBackgroundColor(capo_useThisBGColor);
                        tcapo1_2.setTextColor(lyricsCapoColor);
                        if (showCapo && showChords.equals("Y")) {
                            capo_chords_row1_2.addView(tcapo1_2);
                        }
                        chords_row1_2.addView(t1_2);
                    }

                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        TextView t2_2 = new TextView(this);
                        TextView tcapo2_2 = new TextView(this);
                        t2_2.setText(chord);
                        t2_2.setTypeface(chordsfont);
                        t2_2.setTextSize(chords_useThisTextSize);
                        t2_2.setBackgroundColor(chords_useThisBGColor);
                        t2_2.setTextColor(lyricsChordsColor);
                        tcapo2_2.setText(temptranspChords);
                        tcapo2_2.setTypeface(chordsfont);
                        tcapo2_2.setTextSize(chords_useThisTextSize);
                        tcapo2_2.setBackgroundColor(capo_useThisBGColor);
                        tcapo2_2.setTextColor(lyricsCapoColor);
                        if (showCapo && showChords.equals("Y")) {
                            capo_chords_row2_2.addView(tcapo2_2);
                        }
                        chords_row2_2.addView(t2_2);
                    }

                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        TextView t1_3 = new TextView(this);
                        TextView tcapo1_3 = new TextView(this);
                        t1_3.setText(chord);
                        t1_3.setTypeface(chordsfont);
                        t1_3.setTextSize(chords_useThisTextSize);
                        t1_3.setBackgroundColor(chords_useThisBGColor);
                        t1_3.setTextColor(lyricsChordsColor);
                        tcapo1_3.setText(temptranspChords);
                        tcapo1_3.setTypeface(chordsfont);
                        tcapo1_3.setTextSize(chords_useThisTextSize);
                        tcapo1_3.setBackgroundColor(capo_useThisBGColor);
                        tcapo1_3.setTextColor(lyricsCapoColor);
                        if (showCapo && showChords.equals("Y")) {
                            capo_chords_row1_3.addView(tcapo1_3);
                        }
                        chords_row1_3.addView(t1_3);
                    }

                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        TextView t2_3 = new TextView(this);
                        TextView tcapo2_3 = new TextView(this);
                        t2_3.setText(chord);
                        t2_3.setTypeface(chordsfont);
                        t2_3.setTextSize(chords_useThisTextSize);
                        t2_3.setBackgroundColor(chords_useThisBGColor);
                        t2_3.setTextColor(lyricsChordsColor);
                        tcapo2_3.setText(temptranspChords);
                        tcapo2_3.setTypeface(chordsfont);
                        tcapo2_3.setTextSize(chords_useThisTextSize);
                        tcapo2_3.setBackgroundColor(capo_useThisBGColor);
                        tcapo2_3.setTextColor(lyricsCapoColor);
                        if (showCapo && showChords.equals("Y")) {
                            capo_chords_row2_3.addView(tcapo2_3);
                        }
                        chords_row2_3.addView(t2_3);
                    }

                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        TextView t3_3 = new TextView(this);
                        TextView tcapo3_3 = new TextView(this);
                        t3_3.setText(chord);
                        t3_3.setTypeface(chordsfont);
                        t3_3.setTextSize(chords_useThisTextSize);
                        t3_3.setBackgroundColor(chords_useThisBGColor);
                        t3_3.setTextColor(lyricsChordsColor);
                        tcapo3_3.setText(temptranspChords);
                        tcapo3_3.setTypeface(chordsfont);
                        tcapo3_3.setTextSize(chords_useThisTextSize);
                        tcapo3_3.setBackgroundColor(capo_useThisBGColor);
                        tcapo3_3.setTextColor(lyricsCapoColor);
                        if (showCapo && showChords.equals("Y")) {
                            capo_chords_row3_3.addView(tcapo3_3);
                        }
                        chords_row3_3.addView(t3_3);
                    }
                }

                if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                    chords_row1_1.setBackgroundColor(chords_useThisBGColor);
                    capo_chords_row1_1.setBackgroundColor(capo_useThisBGColor);
                }

                if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                    chords_row1_2.setBackgroundColor(chords_useThisBGColor);
                    capo_chords_row1_2.setBackgroundColor(capo_useThisBGColor);
                }

                if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                    chords_row2_2.setBackgroundColor(chords_useThisBGColor);
                    capo_chords_row2_2.setBackgroundColor(capo_useThisBGColor);
                }

                if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                    chords_row1_3.setBackgroundColor(chords_useThisBGColor);
                    capo_chords_row1_3.setBackgroundColor(capo_useThisBGColor);
                }

                if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                    chords_row2_3.setBackgroundColor(chords_useThisBGColor);
                    capo_chords_row2_3.setBackgroundColor(capo_useThisBGColor);
                }

                if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                    chords_row3_3.setBackgroundColor(chords_useThisBGColor);
                    capo_chords_row3_3.setBackgroundColor(capo_useThisBGColor);
                }

                // What should we be showing?
                // First option is that there are no capo chords, but the user wants chords
                // This displays the normal chords only
                if (showChords.equals("Y") && !showCapo) {
                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        chords_n_lyrics1_1.addView(chords_row1_1, 0);
                    }

                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics1_2.addView(chords_row1_2, 0);
                    }

                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics2_2.addView(chords_row2_2, 0);
                    }

                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics1_3.addView(chords_row1_3, 0);
                    }

                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics2_3.addView(chords_row2_3, 0);
                    }

                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics3_3.addView(chords_row3_3, 0);
                    }

                    // Add this line of chords to the combined string
                    allchords = allchords + " " + myParsedLyrics[x];

                } else if (showChords.equals("Y") && showCapo && capoDisplay.equals("both")) {
                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        chords_n_lyrics1_1.addView(chords_row1_1, 0);
                        chords_n_lyrics1_1.addView(capo_chords_row1_1, 1);
                    }
                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics1_2.addView(chords_row1_2, 0);
                        chords_n_lyrics1_2.addView(capo_chords_row1_2, 1);
                    }
                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics2_2.addView(chords_row2_2, 0);
                        chords_n_lyrics2_2.addView(capo_chords_row2_2, 1);
                    }
                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics1_3.addView(chords_row1_3, 0);
                        chords_n_lyrics1_3.addView(capo_chords_row1_3, 1);
                    }
                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics2_3.addView(chords_row2_3, 0);
                        chords_n_lyrics2_3.addView(capo_chords_row2_3, 1);
                    }
                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics3_3.addView(chords_row3_3, 0);
                        chords_n_lyrics3_3.addView(capo_chords_row3_3, 1);
                    }

                    // Add this line of chords to the combined string
                    allchords = allchords + " " + myParsedLyrics[x];
                    allchords = allchords + " " + allchordscapo;

                } else if (showChords.equals("Y") && showCapo && capoDisplay.equals("capoonly")) {
                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        chords_n_lyrics1_1.addView(capo_chords_row1_1, 0);
                    }
                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics1_2.addView(capo_chords_row1_2, 0);
                    }
                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics2_2.addView(capo_chords_row2_2, 0);
                    }
                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics1_3.addView(capo_chords_row1_3, 0);
                    }
                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics2_3.addView(capo_chords_row2_3, 0);
                    }
                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics3_3.addView(capo_chords_row3_3, 0);
                    }
                    // Add this line of chords to the combined string
                    allchords = allchords + " " + allchordscapo;
                }

                TableRow lyrics_row1_1 = new TableRow(this);
                TableRow lyrics_row1_2 = new TableRow(this);
                TableRow lyrics_row2_2 = new TableRow(this);
                TableRow lyrics_row1_3 = new TableRow(this);
                TableRow lyrics_row2_3 = new TableRow(this);
                TableRow lyrics_row3_3 = new TableRow(this);

                String temp_lyricstext;

                // Now we have the positions, split the words into substrings and write each
                // substring as a textview within the row
                // If this is a multiline verse, we need to sort the next verse lines as well

                for (int i = 0; i < chordnum; i++) {
                    // create a new TextView
                    TextView t21_1 = new TextView(this);
                    TextView t21_2 = new TextView(this);
                    TextView t22_2 = new TextView(this);
                    TextView t21_3 = new TextView(this);
                    TextView t22_3 = new TextView(this);
                    TextView t23_3 = new TextView(this);

                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        t21_1.setTextColor(lyricsTextColor);
                        t21_1.setTypeface(lyrics_useThisFont);
                        t21_1.setTextSize(lyrics_useThisTextSize);
                    }

                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        t21_2.setTextColor(lyricsTextColor);
                        t21_2.setTypeface(lyrics_useThisFont);
                        t21_2.setTextSize(lyrics_useThisTextSize);
                    }

                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        t22_2.setTextColor(lyricsTextColor);
                        t22_2.setTypeface(lyrics_useThisFont);
                        t22_2.setTextSize(lyrics_useThisTextSize);
                    }

                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        t21_3.setTextColor(lyricsTextColor);
                        t21_3.setTypeface(lyrics_useThisFont);
                        t21_3.setTextSize(lyrics_useThisTextSize);
                    }

                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        t22_3.setTextColor(lyricsTextColor);
                        t22_3.setTypeface(lyrics_useThisFont);
                        t22_3.setTextSize(lyrics_useThisTextSize);
                    }

                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        t23_3.setTextColor(lyricsTextColor);
                        t23_3.setTypeface(lyrics_useThisFont);
                        t23_3.setTextSize(lyrics_useThisTextSize);
                    }

                    // Get the block colours
                    getBlock(whatisthisblock[x + 1]);

                    if (whatisthisblock[x + 1].equals("comment")) {
                        if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                            t21_1.setTypeface(commentfont);
                            //t21_1.setTextSize(tempsectionsize);
                            t21_1.setTextSize(tempfontsize * commentfontscalesize);
                        }
                        if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                            t21_2.setTypeface(commentfont);
                            //t21_2.setTextSize(tempsectionsize);
                            t21_2.setTextSize(tempfontsize * commentfontscalesize);
                        }
                        if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                            t22_2.setTypeface(commentfont);
                            //t22_2.setTextSize(tempsectionsize);
                            t22_2.setTextSize(tempfontsize * commentfontscalesize);

                        }
                        if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                            t21_3.setTypeface(commentfont);
                            //t21_3.setTextSize(tempsectionsize);
                            t21_3.setTextSize(tempfontsize * commentfontscalesize);
                        }
                        if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                            t22_3.setTypeface(commentfont);
                            //t22_3.setTextSize(tempsectionsize);
                            t22_3.setTextSize(tempfontsize * commentfontscalesize);
                        }
                        if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                            t23_3.setTypeface(commentfont);
                            //t23_3.setTextSize(tempsectionsize);
                            t23_3.setTextSize(tempfontsize * commentfontscalesize);
                        }
                    }

                    if (i == 0) {
                        temp_lyricstext = myParsedLyrics[x + 1].substring(0, chord_pos[1]);
                        // Multilines
                        if (myParsedLyrics[x + 1].indexOf("1") == 0) {
                            if ((x + 2) < numrowstowrite) {
                                if (myParsedLyrics[x + 2].indexOf("2") == 0) {
                                    temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 2].substring(0, chord_pos[1]);
                                }
                            }
                            if ((x + 3) < numrowstowrite) {
                                if (myParsedLyrics[x + 3].indexOf("3") == 0) {
                                    temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 3].substring(0, chord_pos[1]);
                                }
                            }
                            if ((x + 4) < numrowstowrite) {
                                if (myParsedLyrics[x + 4].indexOf("4") == 0) {
                                    temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 4].substring(0, chord_pos[1]);
                                }
                            }
                            if ((x + 5) < numrowstowrite) {
                                if (myParsedLyrics[x + 5].indexOf("5") == 0) {
                                    temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 5].substring(0, chord_pos[1]);
                                }
                            }
                            if ((x + 6) < numrowstowrite) {
                                if (myParsedLyrics[x + 6].indexOf("6") == 0) {
                                    temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 6].substring(0, chord_pos[1]);
                                }
                            }
                            if ((x + 7) < numrowstowrite) {
                                if (myParsedLyrics[x + 7].indexOf("7") == 0) {
                                    temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 7].substring(0, chord_pos[1]);
                                }
                            }
                            if ((x + 8) < numrowstowrite) {
                                if (myParsedLyrics[x + 8].indexOf("8") == 0) {
                                    temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 8].substring(0, chord_pos[1]);
                                }
                            }
                            if ((x + 9) < numrowstowrite) {
                                if (myParsedLyrics[x + 9].indexOf("9") == 0) {
                                    temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 9].substring(0, chord_pos[1]);
                                }
                            }
                        }
                    } else {
                        try {
                            temp_lyricstext = myParsedLyrics[x + 1].substring(chord_pos[i], chord_pos[i + 1]);
                        } catch (Exception e) {
                            temp_lyricstext = " ";
                        }

                        // Multilines
                        if (myParsedLyrics[x + 1].indexOf("1") == 0) {
                            if ((x + 2) < numrowstowrite) {
                                if (myParsedLyrics[x + 2].indexOf("2") == 0) {
                                    try {
                                        temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 2].substring(chord_pos[i], chord_pos[i + 1]);
                                    } catch (Exception e) {
                                        temp_lyricstext = temp_lyricstext + "\n" + " ";
                                    }
                                }
                            }
                            if ((x + 3) < numrowstowrite) {
                                if (myParsedLyrics[x + 3].indexOf("3") == 0) {
                                    try {
                                        temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 3].substring(chord_pos[i], chord_pos[i + 1]);
                                    } catch (Exception e) {
                                        temp_lyricstext = temp_lyricstext + "\n" + " ";
                                    }
                                }
                            }
                            if ((x + 4) < numrowstowrite) {
                                if (myParsedLyrics[x + 4].indexOf("4") == 0) {
                                    try {
                                        temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 4].substring(chord_pos[i], chord_pos[i + 1]);
                                    } catch (Exception e) {
                                        temp_lyricstext = temp_lyricstext + "\n" + " ";
                                    }
                                }
                            }
                            if ((x + 5) < numrowstowrite) {
                                if (myParsedLyrics[x + 5].indexOf("5") == 0) {
                                    try {
                                        temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 5].substring(chord_pos[i], chord_pos[i + 1]);
                                    } catch (Exception e) {
                                        temp_lyricstext = temp_lyricstext + "\n" + " ";
                                    }
                                }
                            }
                            if ((x + 6) < numrowstowrite) {
                                if (myParsedLyrics[x + 6].indexOf("6") == 0) {
                                    try {
                                        temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 6].substring(chord_pos[i], chord_pos[i + 1]);
                                    } catch (Exception e) {
                                        temp_lyricstext = temp_lyricstext + "\n" + " ";
                                    }
                                }
                            }
                            if ((x + 7) < numrowstowrite) {
                                if (myParsedLyrics[x + 7].indexOf("7") == 0) {
                                    try {
                                        temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 7].substring(chord_pos[i], chord_pos[i + 1]);
                                    } catch (Exception e) {
                                        temp_lyricstext = temp_lyricstext + "\n" + " ";
                                    }
                                }
                            }
                            if ((x + 8) < numrowstowrite) {
                                if (myParsedLyrics[x + 8].indexOf("8") == 0) {
                                    try {
                                        temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 8].substring(chord_pos[i], chord_pos[i + 1]);
                                    } catch (Exception e) {
                                        temp_lyricstext = temp_lyricstext + "\n" + " ";
                                    }
                                }
                            }
                            if ((x + 9) < numrowstowrite) {
                                if (myParsedLyrics[x + 9].indexOf("9") == 0) {
                                    try {
                                        temp_lyricstext = temp_lyricstext + "\n" + myParsedLyrics[x + 9].substring(chord_pos[i], chord_pos[i + 1]);
                                    } catch (Exception e) {
                                        temp_lyricstext = temp_lyricstext + "\n" + " ";
                                    }
                                }
                            }
                        }
                    }

                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        t21_1.setText(temp_lyricstext);
                        t21_1.setBackgroundColor(temp_useThisBGColor);
                        lyrics_row1_1.addView(t21_1);
                    }
                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        t21_2.setText(temp_lyricstext);
                        t21_2.setBackgroundColor(temp_useThisBGColor);
                        lyrics_row1_2.addView(t21_2);
                    }
                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        t22_2.setText(temp_lyricstext);
                        t22_2.setBackgroundColor(temp_useThisBGColor);
                        lyrics_row2_2.addView(t22_2);
                    }
                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        t21_3.setText(temp_lyricstext);
                        t21_3.setBackgroundColor(temp_useThisBGColor);
                        lyrics_row1_3.addView(t21_3);
                    }
                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        t22_3.setText(temp_lyricstext);
                        t22_3.setBackgroundColor(temp_useThisBGColor);
                        lyrics_row2_3.addView(t22_3);
                    }
                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        t23_3.setText(temp_lyricstext);
                        t23_3.setBackgroundColor(temp_useThisBGColor);
                        lyrics_row3_3.addView(t23_3);
                    }
                }

                // Decide on the lyrics row background colour
                getBlock(whatisthisblock[x + 1]);

                if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                    lyrics_row1_1.setBackgroundColor(lyrics_useThisBGColor);
                    lyrics_row1_1.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                }
                if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                    lyrics_row1_2.setBackgroundColor(lyrics_useThisBGColor);
                    lyrics_row1_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                }
                if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                    lyrics_row2_2.setBackgroundColor(lyrics_useThisBGColor);
                    lyrics_row2_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                }
                if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                    lyrics_row1_3.setBackgroundColor(lyrics_useThisBGColor);
                    lyrics_row1_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                }
                if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                    lyrics_row2_3.setBackgroundColor(lyrics_useThisBGColor);
                    lyrics_row2_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                }
                if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                    lyrics_row3_3.setBackgroundColor(lyrics_useThisBGColor);
                    lyrics_row3_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                }

                // What should we be showing?
                // First option is that there are no capo chords, but the user wants chords
                // This displays the normal chords only
                if (showChords.equals("Y") && !showCapo) {
                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        chords_n_lyrics1_1.addView(lyrics_row1_1, 1);
                    }
                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics1_2.addView(lyrics_row1_2, 1);
                    }
                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics2_2.addView(lyrics_row2_2, 1);
                    }
                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics1_3.addView(lyrics_row1_3, 1);
                    }
                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics2_3.addView(lyrics_row2_3, 1);
                    }
                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics3_3.addView(lyrics_row3_3, 1);
                    }
                } else if (showChords.equals("Y") && showCapo && capoDisplay.equals("both")) {
                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        chords_n_lyrics1_1.addView(lyrics_row1_1, 2);
                    }
                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics1_2.addView(lyrics_row1_2, 2);
                    }
                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics2_2.addView(lyrics_row2_2, 2);
                    }
                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics1_3.addView(lyrics_row1_3, 2);
                    }
                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics2_3.addView(lyrics_row2_3, 2);
                    }
                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics3_3.addView(lyrics_row3_3, 2);
                    }
                } else if (showChords.equals("Y") && showCapo && capoDisplay.equals("capoonly")) {
                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        chords_n_lyrics1_1.addView(lyrics_row1_1, 1);
                    }
                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics1_2.addView(lyrics_row1_2, 1);
                    }
                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics2_2.addView(lyrics_row2_2, 1);
                    }
                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics1_3.addView(lyrics_row1_3, 1);
                    }
                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics2_3.addView(lyrics_row2_3, 1);
                    }
                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics3_3.addView(lyrics_row3_3, 1);
                    }
                } else {
                    if (writetocol1_1 && (columnTest == 1 || columnTest == 0)) {
                        chords_n_lyrics1_1.addView(lyrics_row1_1, 0);
                    }
                    if (writetocol1_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics1_2.addView(lyrics_row1_2, 0);
                    }
                    if (writetocol2_2 && (columnTest == 2 || columnTest == 0)) {
                        chords_n_lyrics2_2.addView(lyrics_row2_2, 0);
                    }
                    if (writetocol1_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics1_3.addView(lyrics_row1_3, 0);
                    }
                    if (writetocol2_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics2_3.addView(lyrics_row2_3, 0);
                    }
                    if (writetocol3_3 && (columnTest == 3 || columnTest == 0)) {
                        chords_n_lyrics3_3.addView(lyrics_row3_3, 0);
                    }
                }

                // Prepare this view
                if (writetocol1_1) {
                    lyricstable_onecolview.addView(chords_n_lyrics1_1);
                }
                if (writetocol1_2) {
                    lyricstable_twocolview.addView(chords_n_lyrics1_2);
                } else if (writetocol2_2) {
                    lyricstable2_twocolview.addView(chords_n_lyrics2_2);
                }
                if (writetocol1_3) {
                    lyricstable_threecolview.addView(chords_n_lyrics1_3);
                } else if (writetocol2_3) {
                    lyricstable2_threecolview.addView(chords_n_lyrics2_3);
                } else if (writetocol3_3) {
                    lyricstable3_threecolview.addView(chords_n_lyrics3_3);
                }

            } else if (whatisthisline[x].equals("chords") && !whatisthisline[m].equals("lyrics")) {
                // No blocking is needed, just add the entire row as one bit
                TableLayout basicline1_1 = new TableLayout(this);
                TableLayout basicline1_2 = new TableLayout(this);
                TableLayout basicline2_2 = new TableLayout(this);
                TableLayout basicline1_3 = new TableLayout(this);
                TableLayout basicline2_3 = new TableLayout(this);
                TableLayout basicline3_3 = new TableLayout(this);

                basicline1_1.setPadding(0, 0, 0, 0);
                basicline1_2.setPadding(0, 0, 0, 0);
                basicline2_2.setPadding(0, 0, 0, 0);
                basicline1_3.setPadding(0, 0, 0, 0);
                basicline2_3.setPadding(0, 0, 0, 0);
                basicline3_3.setPadding(0, 0, 0, 0);

                // create a new TableRow
                TableRow normalrow1_1 = new TableRow(this);
                TableRow normalrow1_2 = new TableRow(this);
                TableRow normalrow2_2 = new TableRow(this);
                TableRow normalrow1_3 = new TableRow(this);
                TableRow normalrow2_3 = new TableRow(this);
                TableRow normalrow3_3 = new TableRow(this);
                TableRow caponormalrow1_1 = new TableRow(this);
                TableRow caponormalrow1_2 = new TableRow(this);
                TableRow caponormalrow2_2 = new TableRow(this);
                TableRow caponormalrow1_3 = new TableRow(this);
                TableRow caponormalrow2_3 = new TableRow(this);
                TableRow caponormalrow3_3 = new TableRow(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 0);
                normalrow1_1.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                normalrow1_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                normalrow2_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                normalrow1_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                normalrow2_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                normalrow3_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                caponormalrow1_1.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                caponormalrow1_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                caponormalrow2_2.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                caponormalrow1_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                caponormalrow2_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);
                caponormalrow3_3.setPadding(0, -(int) ((float) linespacing / 3.0f), 0, 0);

                // create a new TextView
                TextView tbasic1_1 = new TextView(this);
                TextView tbasic1_2 = new TextView(this);
                TextView tbasic2_2 = new TextView(this);
                TextView tbasic1_3 = new TextView(this);
                TextView tbasic2_3 = new TextView(this);
                TextView tbasic3_3 = new TextView(this);
                TextView capotbasic1_1 = new TextView(this);
                TextView capotbasic1_2 = new TextView(this);
                TextView capotbasic2_2 = new TextView(this);
                TextView capotbasic1_3 = new TextView(this);
                TextView capotbasic2_3 = new TextView(this);
                TextView capotbasic3_3 = new TextView(this);
                tbasic1_1.setLayoutParams(lp);
                tbasic1_2.setLayoutParams(lp);
                tbasic2_2.setLayoutParams(lp);
                tbasic1_3.setLayoutParams(lp);
                tbasic2_3.setLayoutParams(lp);
                tbasic3_3.setLayoutParams(lp);
                capotbasic1_1.setLayoutParams(lp);
                capotbasic1_2.setLayoutParams(lp);
                capotbasic2_2.setLayoutParams(lp);
                capotbasic1_3.setLayoutParams(lp);
                capotbasic2_3.setLayoutParams(lp);
                capotbasic3_3.setLayoutParams(lp);
                tbasic1_1.setText(myParsedLyrics[x]);
                tbasic1_1.setTextColor(lyricsChordsColor);
                tbasic1_1.setTypeface(chordsfont);
                tbasic1_1.setTextSize(tempfontsize);
                tbasic1_1.setPadding(0, 0, 0, 0);
                tbasic1_2.setText(myParsedLyrics[x]);
                tbasic1_2.setTextColor(lyricsChordsColor);
                tbasic1_2.setTypeface(chordsfont);
                tbasic1_2.setTextSize(tempfontsize);
                tbasic1_2.setPadding(0, 0, 0, 0);
                tbasic2_2.setText(myParsedLyrics[x]);
                tbasic2_2.setTextColor(lyricsChordsColor);
                tbasic2_2.setTypeface(chordsfont);
                tbasic2_2.setTextSize(tempfontsize);
                tbasic2_2.setPadding(0, 0, 0, 0);
                tbasic1_3.setText(myParsedLyrics[x]);
                tbasic1_3.setTextColor(lyricsChordsColor);
                tbasic1_3.setTypeface(chordsfont);
                tbasic1_3.setTextSize(tempfontsize);
                tbasic1_3.setPadding(0, 0, 0, 0);
                tbasic2_3.setText(myParsedLyrics[x]);
                tbasic2_3.setTextColor(lyricsChordsColor);
                tbasic2_3.setTypeface(chordsfont);
                tbasic2_3.setTextSize(tempfontsize);
                tbasic2_3.setPadding(0, 0, 0, 0);
                tbasic3_3.setText(myParsedLyrics[x]);
                tbasic3_3.setTextColor(lyricsChordsColor);
                tbasic3_3.setTypeface(chordsfont);
                tbasic3_3.setTextSize(tempfontsize);
                tbasic3_3.setPadding(0, 0, 0, 0);

                if (showCapo) {
                    temptranspChords = myParsedLyrics[x];
                    Transpose.capoTranspose();
                    allchordscapo = allchordscapo + " " + temptranspChords;
                    capotbasic1_1.setText(temptranspChords);
                    capotbasic1_1.setTextColor(lyricsCapoColor);
                    capotbasic1_1.setTypeface(chordsfont);
                    capotbasic1_1.setTextSize(tempfontsize);
                    capotbasic1_1.setPadding(0, 0, 0, 0);
                    capotbasic1_2.setText(temptranspChords);
                    capotbasic1_2.setTextColor(lyricsCapoColor);
                    capotbasic1_2.setTypeface(chordsfont);
                    capotbasic1_2.setTextSize(tempfontsize);
                    capotbasic1_2.setPadding(0, 0, 0, 0);
                    capotbasic2_2.setText(temptranspChords);
                    capotbasic2_2.setTextColor(lyricsCapoColor);
                    capotbasic2_2.setTypeface(chordsfont);
                    capotbasic2_2.setTextSize(tempfontsize);
                    capotbasic2_2.setPadding(0, 0, 0, 0);
                    capotbasic1_3.setText(temptranspChords);
                    capotbasic1_3.setTextColor(lyricsCapoColor);
                    capotbasic1_3.setTypeface(chordsfont);
                    capotbasic1_3.setTextSize(tempfontsize);
                    capotbasic1_3.setPadding(0, 0, 0, 0);
                    capotbasic2_3.setText(temptranspChords);
                    capotbasic2_3.setTextColor(lyricsCapoColor);
                    capotbasic2_3.setTypeface(chordsfont);
                    capotbasic2_3.setTextSize(tempfontsize);
                    capotbasic2_3.setPadding(0, 0, 0, 0);
                    capotbasic3_3.setText(temptranspChords);
                    capotbasic3_3.setTextColor(lyricsCapoColor);
                    capotbasic3_3.setTypeface(chordsfont);
                    capotbasic3_3.setTextSize(tempfontsize);
                    capotbasic3_3.setPadding(0, 0, 0, 0);
                }

                // If line is a title, need to make the text size smaller
                if (whatisthisline[x].equals("versetitle")
                        || whatisthisline[x].equals("chorustitle")
                        || whatisthisline[x].equals("prechorustitle")
                        || whatisthisline[x].equals("bridgetitle")
                        || whatisthisline[x].equals("tagtitle")
                        || whatisthisline[x].equals("imagetitle")
                        || whatisthisline[x].equals("customtitle")) {
                    tbasic1_1.setTextColor(lyricsTextColor);
                    tbasic1_1.setAlpha(0.8f);
                    //tbasic1_1.setTextSize(tempsectionsize);
                    tbasic1_1.setTextSize(tempfontsize * headingfontscalesize);
                    tbasic1_2.setTextColor(lyricsTextColor);
                    tbasic1_2.setAlpha(0.8f);
                    //tbasic1_2.setTextSize(tempsectionsize);
                    tbasic1_2.setTextSize(tempfontsize * headingfontscalesize);
                    tbasic2_2.setTextColor(lyricsTextColor);
                    tbasic2_2.setAlpha(0.8f);
                    //tbasic2_2.setTextSize(tempsectionsize);
                    tbasic2_2.setTextSize(tempfontsize * headingfontscalesize);
                    tbasic1_3.setTextColor(lyricsTextColor);
                    tbasic1_3.setAlpha(0.8f);
                    //tbasic1_3.setTextSize(tempsectionsize);
                    tbasic1_3.setTextSize(tempfontsize * headingfontscalesize);
                    tbasic2_3.setTextColor(lyricsTextColor);
                    tbasic2_3.setAlpha(0.8f);
                    //tbasic2_3.setTextSize(tempsectionsize);
                    tbasic2_3.setTextSize(tempfontsize * headingfontscalesize);
                    tbasic3_3.setTextColor(lyricsTextColor);
                    tbasic3_3.setAlpha(0.8f);
                    //tbasic3_3.setTextSize(tempsectionsize);
                    tbasic3_3.setTextSize(tempfontsize * headingfontscalesize);
                }
                // add the TextView new TableRow
                normalrow1_1.addView(tbasic1_1);
                normalrow1_2.addView(tbasic1_2);
                normalrow2_2.addView(tbasic2_2);
                normalrow1_3.addView(tbasic1_3);
                normalrow2_3.addView(tbasic2_3);
                normalrow3_3.addView(tbasic3_3);
                if (showCapo && showChords.equals("Y")) {
                    caponormalrow1_1.addView(capotbasic1_1);
                    caponormalrow1_2.addView(capotbasic1_2);
                    caponormalrow2_2.addView(capotbasic2_2);
                    caponormalrow1_3.addView(capotbasic1_3);
                    caponormalrow2_3.addView(capotbasic2_3);
                    caponormalrow3_3.addView(capotbasic3_3);
                }

                lyrics_useThisFont = lyricsfont;
                lyrics_useThisTextSize = tempfontsize;

                // Decide on the block of text
                getBlock(whatisthisblock[x]);

                tbasic1_1.setBackgroundColor(lyrics_useThisBGColor);
                tbasic1_2.setBackgroundColor(lyrics_useThisBGColor);
                tbasic2_2.setBackgroundColor(lyrics_useThisBGColor);
                tbasic1_3.setBackgroundColor(lyrics_useThisBGColor);
                tbasic2_3.setBackgroundColor(lyrics_useThisBGColor);
                tbasic3_3.setBackgroundColor(lyrics_useThisBGColor);
                normalrow1_1.setBackgroundColor(lyrics_useThisBGColor);
                normalrow1_2.setBackgroundColor(lyrics_useThisBGColor);
                normalrow2_2.setBackgroundColor(lyrics_useThisBGColor);
                normalrow1_3.setBackgroundColor(lyrics_useThisBGColor);
                normalrow2_3.setBackgroundColor(lyrics_useThisBGColor);
                normalrow3_3.setBackgroundColor(lyrics_useThisBGColor);
                capotbasic1_1.setBackgroundColor(lyrics_useThisBGColor);
                capotbasic1_2.setBackgroundColor(lyrics_useThisBGColor);
                capotbasic2_2.setBackgroundColor(lyrics_useThisBGColor);
                capotbasic1_3.setBackgroundColor(lyrics_useThisBGColor);
                capotbasic2_3.setBackgroundColor(lyrics_useThisBGColor);
                capotbasic3_3.setBackgroundColor(lyrics_useThisBGColor);
                caponormalrow1_1.setBackgroundColor(lyrics_useThisBGColor);
                caponormalrow1_2.setBackgroundColor(lyrics_useThisBGColor);
                caponormalrow2_2.setBackgroundColor(lyrics_useThisBGColor);
                caponormalrow1_3.setBackgroundColor(lyrics_useThisBGColor);
                caponormalrow2_3.setBackgroundColor(lyrics_useThisBGColor);
                caponormalrow3_3.setBackgroundColor(lyrics_useThisBGColor);
                tbasic1_1.setTextSize(lyrics_useThisTextSize);
                tbasic1_2.setTextSize(lyrics_useThisTextSize);
                tbasic2_2.setTextSize(lyrics_useThisTextSize);
                tbasic1_3.setTextSize(lyrics_useThisTextSize);
                tbasic2_3.setTextSize(lyrics_useThisTextSize);
                tbasic3_3.setTextSize(lyrics_useThisTextSize);
                capotbasic1_1.setTextSize(lyrics_useThisTextSize);
                capotbasic1_2.setTextSize(lyrics_useThisTextSize);
                capotbasic2_2.setTextSize(lyrics_useThisTextSize);
                capotbasic1_3.setTextSize(lyrics_useThisTextSize);
                capotbasic2_3.setTextSize(lyrics_useThisTextSize);
                capotbasic3_3.setTextSize(lyrics_useThisTextSize);

                if (!whatisthisline[x].equals("chords")) {
                    basicline1_1.addView(normalrow1_1);
                    basicline1_2.addView(normalrow1_2);
                    basicline2_2.addView(normalrow2_2);
                    basicline1_3.addView(normalrow1_3);
                    basicline2_3.addView(normalrow2_3);
                    basicline3_3.addView(normalrow3_3);
                } else if (whatisthisline[x].equals("chords") && showChords.equals("Y")) {
                    if (!showCapo) {
                        // Add this line of chords to the combined string
                        allchords = allchords + " " + myParsedLyrics[x];
                        basicline1_1.addView(normalrow1_1);
                        basicline1_2.addView(normalrow1_2);
                        basicline2_2.addView(normalrow2_2);
                        basicline1_3.addView(normalrow1_3);
                        basicline2_3.addView(normalrow2_3);
                        basicline3_3.addView(normalrow3_3);
                    } else if (showCapo && capoDisplay.equals("both")) {
                        // Add this line of chords to the combined string
                        allchords = allchords + " " + myParsedLyrics[x];
                        allchords = allchords + " " + allchordscapo;
                        basicline1_1.addView(normalrow1_1);
                        basicline1_2.addView(normalrow1_2);
                        basicline2_2.addView(normalrow2_2);
                        basicline1_3.addView(normalrow1_3);
                        basicline2_3.addView(normalrow2_3);
                        basicline3_3.addView(normalrow3_3);
                        basicline1_1.addView(caponormalrow1_1);
                        basicline1_2.addView(caponormalrow1_2);
                        basicline2_2.addView(caponormalrow2_2);
                        basicline1_3.addView(caponormalrow1_3);
                        basicline2_3.addView(caponormalrow2_3);
                        basicline3_3.addView(caponormalrow3_3);
                    } else if (showCapo && capoDisplay.equals("capoonly")) {
                        // Add this line of chords to the combined string
                        allchords = allchords + " " + allchordscapo;
                        basicline1_1.addView(caponormalrow1_1);
                        basicline1_2.addView(caponormalrow1_2);
                        basicline2_2.addView(caponormalrow2_2);
                        basicline1_3.addView(caponormalrow1_3);
                        basicline2_3.addView(caponormalrow2_3);
                        basicline3_3.addView(caponormalrow3_3);
                    }
                }
                // Prepare this view
                if (writetocol1_1) {
                    lyricstable_onecolview.addView(basicline1_1);
                }
                if (writetocol1_2) {
                    lyricstable_twocolview.addView(basicline1_2);
                } else if (writetocol2_2) {
                    lyricstable2_twocolview.addView(basicline2_2);
                }
                if (writetocol1_3) {
                    lyricstable_threecolview.addView(basicline1_3);
                } else if (writetocol2_3) {
                    lyricstable2_threecolview.addView(basicline2_3);
                } else if (writetocol3_3) {
                    lyricstable3_threecolview.addView(basicline3_3);
                }


            } else if (whatisthisline[n] != null && !whatisthisline[n].equals("chords") || (whatisthisline[x] != null && whatisthisline[x].contains("title"))) {
                // No blocking is needed, just add the entire row as one bit
                TableLayout basicline1_1 = new TableLayout(this);
                TableLayout basicline1_2 = new TableLayout(this);
                TableLayout basicline2_2 = new TableLayout(this);
                TableLayout basicline1_3 = new TableLayout(this);
                TableLayout basicline2_3 = new TableLayout(this);
                TableLayout basicline3_3 = new TableLayout(this);
                basicline1_1.setPadding(0, 0, 0, 0);
                basicline1_2.setPadding(0, 0, 0, 0);
                basicline2_2.setPadding(0, 0, 0, 0);
                basicline1_3.setPadding(0, 0, 0, 0);
                basicline2_3.setPadding(0, 0, 0, 0);
                basicline3_3.setPadding(0, 0, 0, 0);

                // create a new TableRow
                TableRow normalrow1_1 = new TableRow(this);
                TableRow normalrow1_2 = new TableRow(this);
                TableRow normalrow2_2 = new TableRow(this);
                TableRow normalrow1_3 = new TableRow(this);
                TableRow normalrow2_3 = new TableRow(this);
                TableRow normalrow3_3 = new TableRow(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 0);

                if (!whatisthisline[x].contains("title")) {
                    myParsedLyrics[x] = myParsedLyrics[x];
                }

                // create a new TextView
                TextView tbasic1_1 = new TextView(this);
                tbasic1_1.setLayoutParams(lp);
                tbasic1_1.setText(myParsedLyrics[x]);
                tbasic1_1.setTextColor(lyricsTextColor);
                tbasic1_1.setTypeface(lyricsfont);
                tbasic1_1.setTextSize(tempfontsize);
                tbasic1_1.setPadding(0, 0, 0, 0);
                TextView tbasic1_2 = new TextView(this);
                tbasic1_2.setLayoutParams(lp);
                tbasic1_2.setText(myParsedLyrics[x]);
                tbasic1_2.setTextColor(lyricsTextColor);
                tbasic1_2.setTypeface(lyricsfont);
                tbasic1_2.setTextSize(tempfontsize);
                tbasic1_2.setPadding(0, 0, 0, 0);
                TextView tbasic2_2 = new TextView(this);
                tbasic2_2.setLayoutParams(lp);
                tbasic2_2.setText(myParsedLyrics[x]);
                tbasic2_2.setTextColor(lyricsTextColor);
                tbasic2_2.setTypeface(lyricsfont);
                tbasic2_2.setTextSize(tempfontsize);
                tbasic2_2.setPadding(0, 0, 0, 0);
                TextView tbasic1_3 = new TextView(this);
                tbasic1_3.setLayoutParams(lp);
                tbasic1_3.setText(myParsedLyrics[x]);
                tbasic1_3.setTextColor(lyricsTextColor);
                tbasic1_3.setTypeface(lyricsfont);
                tbasic1_3.setTextSize(tempfontsize);
                tbasic1_3.setPadding(0, 0, 0, 0);
                TextView tbasic2_3 = new TextView(this);
                tbasic2_3.setLayoutParams(lp);
                tbasic2_3.setText(myParsedLyrics[x]);
                tbasic2_3.setTextColor(lyricsTextColor);
                tbasic2_3.setTypeface(lyricsfont);
                tbasic2_3.setTextSize(tempfontsize);
                tbasic2_3.setPadding(0, 0, 0, 0);
                TextView tbasic3_3 = new TextView(this);
                tbasic3_3.setLayoutParams(lp);
                tbasic3_3.setText(myParsedLyrics[x]);
                tbasic3_3.setTextColor(lyricsTextColor);
                tbasic3_3.setTypeface(lyricsfont);
                tbasic3_3.setTextSize(tempfontsize);
                tbasic3_3.setPadding(0, 0, 0, 0);

                // If line is a title, need to make the text size smaller
                if (whatisthisline[x].equals("versetitle")
                        || whatisthisline[x].equals("chorustitle")
                        || whatisthisline[x].equals("prechorustitle")
                        || whatisthisline[x].equals("bridgetitle")
                        || whatisthisline[x].equals("tagtitle")
                        || whatisthisline[x].equals("imagetitle")
                        || whatisthisline[x].equals("customtitle")) {
                    tbasic1_1.setTextColor(lyricsTextColor);
                    tbasic1_1.setAlpha(0.8f);
                    tbasic1_1.setTextSize(tempfontsize * headingfontscalesize);
                   // tbasic1_1.setTextSize(tempfontsize * 0.6f);
                    tbasic1_1.setPaintFlags(tbasic1_1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    tbasic1_2.setTextColor(lyricsTextColor);
                    tbasic1_2.setAlpha(0.8f);
                    tbasic1_2.setTextSize(tempfontsize * headingfontscalesize);
                    //tbasic1_2.setTextSize(tempfontsize * 0.6f);
                    tbasic1_2.setPaintFlags(tbasic1_1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    tbasic2_2.setTextColor(lyricsTextColor);
                    tbasic2_2.setAlpha(0.8f);
                    tbasic2_2.setTextSize(tempfontsize * headingfontscalesize);
                    //tbasic2_2.setTextSize(tempfontsize * 0.6f);
                    tbasic2_2.setPaintFlags(tbasic1_1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    tbasic1_3.setTextColor(lyricsTextColor);
                    tbasic1_3.setAlpha(0.8f);
                    tbasic1_3.setTextSize(tempfontsize * headingfontscalesize);
                    //tbasic1_3.setTextSize(tempfontsize * 0.6f);
                    tbasic1_3.setPaintFlags(tbasic1_1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    tbasic2_3.setTextColor(lyricsTextColor);
                    tbasic2_3.setAlpha(0.8f);
                    tbasic2_3.setTextSize(tempfontsize * headingfontscalesize);
                    //tbasic2_3.setTextSize(tempfontsize * 0.6f);
                    tbasic2_3.setPaintFlags(tbasic1_1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    tbasic3_3.setTextColor(lyricsTextColor);
                    tbasic3_3.setAlpha(0.8f);
                    tbasic3_3.setTextSize(tempfontsize * headingfontscalesize);
                    //tbasic3_3.setTextSize(tempfontsize * 0.6f);
                    tbasic3_3.setPaintFlags(tbasic1_1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                    if (myParsedLyrics[x] != null && myParsedLyrics[x].contains(image + "_")) {
                        isImageSection = true;
                        if ((x + 1) < myParsedLyrics.length) {
                            imagetext = myParsedLyrics[x + 1];
                        } else {
                            imagetext = "";
                        }
                    }
                }

                // add the TextView new TableRow
                // As long as it isn't a multiline 2-9 beginning
                if (myParsedLyrics[x].indexOf("1") != 0 && myParsedLyrics[x].indexOf("2") != 0 && myParsedLyrics[x].indexOf("3") != 0 &&
                        myParsedLyrics[x].indexOf("4") != 0 && myParsedLyrics[x].indexOf("5") != 0 &&
                        myParsedLyrics[x].indexOf("6") != 0 && myParsedLyrics[x].indexOf("7") != 0 &&
                        myParsedLyrics[x].indexOf("8") != 0 && myParsedLyrics[x].indexOf("9") != 0) {
                    normalrow1_1.addView(tbasic1_1);
                    normalrow1_2.addView(tbasic1_2);
                    normalrow2_2.addView(tbasic2_2);
                    normalrow1_3.addView(tbasic1_3);
                    normalrow2_3.addView(tbasic2_3);
                    normalrow3_3.addView(tbasic3_3);

                } else if (myParsedLyrics[x].indexOf("1") == 0) {
                    // Since we have a multiline section, add the remaining lines needed
                    String newtext = myParsedLyrics[x];
                    if ((x + 1) < numrowstowrite) {
                        if (myParsedLyrics[x + 1].indexOf("2") == 0) {
                            newtext = newtext + "\n" + myParsedLyrics[x + 1];
                        }
                    }
                    if ((x + 2) < numrowstowrite) {
                        if (myParsedLyrics[x + 2].indexOf("3") == 0) {
                            newtext = newtext + "\n" + myParsedLyrics[x + 2];
                        }
                    }
                    if ((x + 3) < numrowstowrite) {
                        if (myParsedLyrics[x + 3].indexOf("4") == 0) {
                            newtext = newtext + "\n" + myParsedLyrics[x + 3];
                        }
                    }
                    if ((x + 4) < numrowstowrite) {
                        if (myParsedLyrics[x + 4].indexOf("5") == 0) {
                            newtext = newtext + "\n" + myParsedLyrics[x + 4];
                        }
                    }
                    if ((x + 5) < numrowstowrite) {
                        if (myParsedLyrics[x + 5].indexOf("6") == 0) {
                            newtext = newtext + "\n" + myParsedLyrics[x + 5];
                        }
                    }
                    if ((x + 6) < numrowstowrite) {
                        if (myParsedLyrics[x + 6].indexOf("7") == 0) {
                            newtext = newtext + "\n" + myParsedLyrics[x + 6];
                        }
                    }
                    if ((x + 7) < numrowstowrite) {
                        if (myParsedLyrics[x + 7].indexOf("8") == 0) {
                            newtext = newtext + "\n" + myParsedLyrics[x + 7];
                        }
                    }
                    if ((x + 8) < numrowstowrite) {
                        if (myParsedLyrics[x + 8].indexOf("9") == 0) {
                            newtext = newtext + "\n" + myParsedLyrics[x + 8];
                        }
                    }

                    tbasic1_1.setText(newtext);
                    tbasic1_2.setText(newtext);
                    tbasic2_2.setText(newtext);
                    tbasic1_3.setText(newtext);
                    tbasic2_3.setText(newtext);
                    tbasic3_3.setText(newtext);
                    normalrow1_1.addView(tbasic1_1);
                    normalrow1_2.addView(tbasic1_2);
                    normalrow2_2.addView(tbasic2_2);
                    normalrow1_3.addView(tbasic1_3);
                    normalrow2_3.addView(tbasic2_3);
                    normalrow3_3.addView(tbasic3_3);
                }

                lyrics_useThisFont = lyricsfont;

                // Decide on the block of text
                getBlock(whatisthisblock[x]);

                tbasic1_1.setBackgroundColor(lyrics_useThisBGColor);
                tbasic1_2.setBackgroundColor(lyrics_useThisBGColor);
                tbasic2_2.setBackgroundColor(lyrics_useThisBGColor);
                tbasic1_3.setBackgroundColor(lyrics_useThisBGColor);
                tbasic2_3.setBackgroundColor(lyrics_useThisBGColor);
                tbasic3_3.setBackgroundColor(lyrics_useThisBGColor);

                if (whatisthisline[x].equals("comment")) {
                    //tbasic1_1.setTextSize(tempsectionsize);
                    //tbasic1_2.setTextSize(tempsectionsize);
                    //tbasic2_2.setTextSize(tempsectionsize);
                    //tbasic1_3.setTextSize(tempsectionsize);
                    //tbasic2_3.setTextSize(tempsectionsize);
                    //tbasic3_3.setTextSize(tempsectionsize);
                    tbasic1_1.setTextSize(tempfontsize * commentfontscalesize);
                    tbasic1_2.setTextSize(tempfontsize * headingfontscalesize);
                    tbasic2_2.setTextSize(tempfontsize * headingfontscalesize);
                    tbasic1_3.setTextSize(tempfontsize * headingfontscalesize);
                    tbasic2_3.setTextSize(tempfontsize * headingfontscalesize);
                    tbasic3_3.setTextSize(tempfontsize * headingfontscalesize);
                }
                normalrow1_1.setBackgroundColor(lyrics_useThisBGColor);
                normalrow1_2.setBackgroundColor(lyrics_useThisBGColor);
                normalrow2_2.setBackgroundColor(lyrics_useThisBGColor);
                normalrow1_3.setBackgroundColor(lyrics_useThisBGColor);
                normalrow2_3.setBackgroundColor(lyrics_useThisBGColor);
                normalrow3_3.setBackgroundColor(lyrics_useThisBGColor);

                if (!whatisthisline[x].equals("chords")) {
                    basicline1_1.addView(normalrow1_1);
                    basicline1_2.addView(normalrow1_2);
                    basicline2_2.addView(normalrow2_2);
                    basicline1_3.addView(normalrow1_3);
                    basicline2_3.addView(normalrow2_3);
                    basicline3_3.addView(normalrow3_3);
                } else if (whatisthisline[x].equals("chords") && showChords.equals("Y")) {
                    basicline1_1.addView(normalrow1_1);
                    basicline1_2.addView(normalrow1_2);
                    basicline2_2.addView(normalrow2_2);
                    basicline1_3.addView(normalrow1_3);
                    basicline2_3.addView(normalrow2_3);
                    basicline3_3.addView(normalrow3_3);
                }

                // Prepare this view
                if (writetocol1_1) {
                    lyricstable_onecolview.addView(basicline1_1);
                }
                if (writetocol1_2) {
                    lyricstable_twocolview.addView(basicline1_2);
                } else if (writetocol2_2) {
                    lyricstable2_twocolview.addView(basicline2_2);
                }
                if (writetocol1_3) {
                    lyricstable_threecolview.addView(basicline1_3);
                } else if (writetocol2_3) {
                    lyricstable2_threecolview.addView(basicline2_3);
                } else if (writetocol3_3) {
                    lyricstable3_threecolview.addView(basicline3_3);
                }
            }

            if (isImageSection) {
                isImageSection = false;
                TableRow rowimage1_1 = new TableRow(this);

                ImageView img1_1 = new ImageView(this);

                // By default, the image should be the not found one
                Drawable drw = getResources().getDrawable(R.drawable.notfound);

                img1_1.setMaxWidth(320);
                img1_1.setMaxHeight(240);

                File checkfile = new File(imagetext);
                if (checkfile.exists()) {
                    try {
                        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagetext), 240, 180);
                        Resources res = getResources();
                        BitmapDrawable bd = new BitmapDrawable(res, ThumbImage);
                        img1_1.setImageDrawable(bd);

                    } catch (Exception e) {
                        // Didn't work
                        img1_1.setImageDrawable(drw);
                    }
                } else {
                    img1_1.setImageDrawable(drw);
                    TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(240, 180);
                    img1_1.setLayoutParams(layoutParams);
                    img1_1.requestLayout();
                }
                TableRow.LayoutParams layoutImage = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                layoutImage.width = 320;
                layoutImage.height = 240;
                img1_1.setLayoutParams(layoutImage);
                rowimage1_1.setGravity(Gravity.CENTER_HORIZONTAL);
                rowimage1_1.addView(img1_1);
                lyricstable_onecolview.addView(rowimage1_1);
                imagetext = "";
            }
        }

        // If the user wants to show sticky notes at the bottom of the page
        if (toggleAutoSticky.equals("B") && !mNotes.isEmpty() && !mNotes.equals("")) {
            String notetoadd = "\n\n" + getResources().getString(R.string.stickynotes) + ":\n" + mNotes;
            RelativeLayout stickyNotes1_1 = new RelativeLayout(this);
            RelativeLayout stickyNotes2_2 = new RelativeLayout(this);
            RelativeLayout stickyNotes3_3 = new RelativeLayout(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            stickyNotes1_1.setLayoutParams(lp);
            stickyNotes1_1.setHorizontalGravity(Gravity.LEFT);
            stickyNotes2_2.setLayoutParams(lp);
            stickyNotes2_2.setHorizontalGravity(Gravity.LEFT);
            stickyNotes3_3.setLayoutParams(lp);
            stickyNotes3_3.setHorizontalGravity(Gravity.LEFT);
            TextView nextSongText1_1 = new TextView(this);
            nextSongText1_1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            nextSongText1_1.setText(notetoadd);
            nextSongText1_1.setGravity(Gravity.LEFT);
            nextSongText1_1.setTypeface(lyrics_useThisFont);
            nextSongText1_1.setBackgroundColor(lyricsCommentColor);
            nextSongText1_1.setTextColor(lyricsTextColor);
            nextSongText1_1.setTextSize(tempfontsize);
            stickyNotes1_1.addView(nextSongText1_1);
            TextView nextSongText2_2 = new TextView(this);
            nextSongText2_2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            nextSongText2_2.setText(notetoadd);
            nextSongText2_2.setGravity(Gravity.LEFT);
            nextSongText2_2.setTypeface(lyrics_useThisFont);
            nextSongText2_2.setBackgroundColor(lyricsCommentColor);
            nextSongText2_2.setTextColor(lyricsTextColor);
            nextSongText2_2.setTextSize(tempfontsize);
            stickyNotes2_2.addView(nextSongText2_2);
            TextView nextSongText3_3 = new TextView(this);
            nextSongText3_3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            nextSongText3_3.setText(notetoadd);
            nextSongText3_3.setGravity(Gravity.LEFT);
            nextSongText3_3.setTypeface(lyrics_useThisFont);
            nextSongText3_3.setBackgroundColor(lyricsCommentColor);
            nextSongText3_3.setTextColor(lyricsTextColor);
            nextSongText3_3.setTextSize(tempfontsize);
            stickyNotes3_3.addView(nextSongText3_3);

            // Prepare this view
            lyricstable_onecolview.addView(stickyNotes1_1);
            lyricstable2_twocolview.addView(stickyNotes2_2);
            lyricstable3_threecolview.addView(stickyNotes3_3);
        }

        // If in a set view and option is on to show at bottom, show the title of the next song
        // If we are showing the last song already, say this instead
        if (setView.equals("Y") && showNextInSet.equals("bottom")) {
            // Get next title in set
            String next_title = getResources().getString(R.string.next) + ": " + getResources().getString(R.string.lastsong);
            if (setView.equals("Y") && showNextInSet.equals("bottom")) {
                // Get next title in set
                next_title = getResources().getString(R.string.lastsong);
                if (setSize >= 2 && indexSongInSet >= 0 && indexSongInSet < (setSize - 1)) {
                    next_title = getResources().getString(R.string.next) + ": " + mSetList[indexSongInSet + 1];
                }
                RelativeLayout nextInSetBox1_1 = new RelativeLayout(this);
                RelativeLayout nextInSetBox2_2 = new RelativeLayout(this);
                RelativeLayout nextInSetBox3_3 = new RelativeLayout(this);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                nextInSetBox1_1.setLayoutParams(lp);
                nextInSetBox1_1.setHorizontalGravity(Gravity.RIGHT);
                nextInSetBox2_2.setLayoutParams(lp);
                nextInSetBox2_2.setHorizontalGravity(Gravity.RIGHT);
                nextInSetBox3_3.setLayoutParams(lp);
                nextInSetBox3_3.setHorizontalGravity(Gravity.RIGHT);
                TextView nextSongText1_1 = new TextView(this);
                nextSongText1_1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                nextSongText1_1.setText(next_title);
                nextSongText1_1.setGravity(Gravity.RIGHT);
                nextSongText1_1.setTypeface(lyrics_useThisFont);
                nextSongText1_1.setBackgroundColor(lyricsCommentColor);
                nextSongText1_1.setTextColor(lyricsTextColor);
                nextSongText1_1.setTextSize(tempfontsize * commentfontscalesize);
                //nextSongText1_1.setTextSize(tempfontsize * 0.7f);
                nextInSetBox1_1.addView(nextSongText1_1);
                TextView nextSongText2_2 = new TextView(this);
                nextSongText2_2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                nextSongText2_2.setText(next_title);
                nextSongText2_2.setGravity(Gravity.RIGHT);
                nextSongText2_2.setTypeface(lyrics_useThisFont);
                nextSongText2_2.setBackgroundColor(lyricsCommentColor);
                nextSongText2_2.setTextColor(lyricsTextColor);
                nextSongText2_2.setTextSize(tempfontsize * commentfontscalesize);
                //nextSongText2_2.setTextSize(tempfontsize * 0.7f);
                nextInSetBox2_2.addView(nextSongText2_2);
                TextView nextSongText3_3 = new TextView(this);
                nextSongText3_3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                nextSongText3_3.setText(next_title);
                nextSongText3_3.setGravity(Gravity.RIGHT);
                nextSongText3_3.setTypeface(lyrics_useThisFont);
                nextSongText3_3.setBackgroundColor(lyricsCommentColor);
                nextSongText3_3.setTextColor(lyricsTextColor);
                nextSongText3_3.setTextSize(tempfontsize * commentfontscalesize);
                //nextSongText3_3.setTextSize(tempfontsize * 0.7f);
                nextInSetBox3_3.addView(nextSongText3_3);

                // Prepare this view
                lyricstable_onecolview.addView(nextInSetBox1_1);
                lyricstable2_twocolview.addView(nextInSetBox2_2);
                lyricstable3_threecolview.addView(nextInSetBox3_3);
            }
        }
        // Try to set the capokey to null so it is figured out next time
        capokey = null;
    }

    @Override
    public void openSongEdit() {
        // This is called from the create a new song popup
        ListSongFiles.listSongFolders();
        // Now load the appropriate song folder
        ListSongFiles.listSongs();
        invalidateOptionsMenu();
        try {
            LoadXML.loadXML();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        redrawTheLyricsTable(main_page);

        whattodo = "editsong";
        newFragment = PopUpEditSongFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void getBlock(String what) {
        switch (what) {
            case "image":
                chords_useThisBGColor = lyricsVerseColor;
                capo_useThisBGColor = lyricsVerseColor;
                lyrics_useThisBGColor = lyricsVerseColor;
                temp_useThisBGColor = lyricsVerseColor;
                break;
            case "chorus":
                chords_useThisBGColor = lyricsChorusColor;
                capo_useThisBGColor = lyricsChorusColor;
                lyrics_useThisBGColor = lyricsChorusColor;
                temp_useThisBGColor = lyricsChorusColor;
                break;
            case "verse":
                chords_useThisBGColor = lyricsVerseColor;
                capo_useThisBGColor = lyricsVerseColor;
                lyrics_useThisBGColor = lyricsVerseColor;
                temp_useThisBGColor = lyricsVerseColor;
                break;
            case "prechorus":
                chords_useThisBGColor = lyricsPreChorusColor;
                capo_useThisBGColor = lyricsPreChorusColor;
                lyrics_useThisBGColor = lyricsPreChorusColor;
                temp_useThisBGColor = lyricsPreChorusColor;
                break;
            case "bridge":
                chords_useThisBGColor = lyricsBridgeColor;
                capo_useThisBGColor = lyricsBridgeColor;
                lyrics_useThisBGColor = lyricsBridgeColor;
                temp_useThisBGColor = lyricsBridgeColor;
                break;
            case "tag":
                chords_useThisBGColor = lyricsTagColor;
                capo_useThisBGColor = lyricsTagColor;
                lyrics_useThisBGColor = lyricsTagColor;
                temp_useThisBGColor = lyricsTagColor;
                break;
            case "comment":
                chords_useThisBGColor = lyricsCommentColor;
                capo_useThisBGColor = lyricsCommentColor;
                lyrics_useThisBGColor = lyricsCommentColor;
                temp_useThisBGColor = lyricsCommentColor;
                lyrics_useThisFont = commentfont;
                lyrics_useThisTextSize = tempsectionsize;
                break;
            case "custom":
                chords_useThisBGColor = lyricsCustomColor;
                capo_useThisBGColor = lyricsCustomColor;
                lyrics_useThisBGColor = lyricsCustomColor;
                temp_useThisBGColor = lyricsCustomColor;
                break;
            default:
                chords_useThisBGColor = lyricsVerseColor;
                capo_useThisBGColor = lyricsVerseColor;
                lyrics_useThisBGColor = lyricsVerseColor;
                temp_useThisBGColor = lyricsVerseColor;
                break;
        }
    }

    @Override
    public void doEdit() {
        whattodo = "editsong";
        newFragment = PopUpEditSongFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void refreshAll() {
        // Show the toast
        if (!myToastMessage.isEmpty()) {
            ShowToast.showToast(FullscreenActivity.this);
        }
        SetActions.prepareSetList();
        prepareSongMenu();
        prepareOptionMenu();
        SetActions.indexSongInSet();
        invalidateOptionsMenu();
        redrawTheLyricsTable(main_page);

        // Reopen the set or song menu if something has changed here
        if (whattodo.equals("loadset") || whattodo.equals("clearset")) {
            expListViewOption.expandGroup(0);
            mDrawerLayout.openDrawer(expListViewOption);
        }

        if (whattodo.equals("renamesong") || whattodo.equals("createsong")) {
            findSongInFolder();
            mDrawerLayout.openDrawer(expListViewSong);
        }

        // If menus are open, close them after 1 second
        Handler closeMenus = new Handler();
        closeMenus.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(expListViewSong);
                mDrawerLayout.closeDrawer(expListViewOption);
            }
        }, 1000);

    }

    @Override
    public void onSongInstall() {
        // Called when a user clicks on a .backup file and intent runs app
        onSongImport();
    }

    @Override
    public void confirmedAction() {
        switch (whattodo) {
            case "saveset":
                whattodo = "";
                if (mSetList!=null && mSetList.length>0) {
                    CreateNewSet.doCreation();
                }
                if (myToastMessage.equals("yes")) {
                    myToastMessage = getResources().getString(R.string.set_save)
                            + " - " + getResources().getString(R.string.ok);
                } else {
                    myToastMessage = getResources().getString(R.string.set_save)
                            + " - " + getResources().getString(R.string.no);
                }
                mDrawerLayout.openDrawer(expListViewOption);
                refreshAll();
                break;

            case "clearset":
                // Clear the set
                mySet = "";
                mSetList = null;
                setView = "N";

                // Save the new, empty, set
                Preferences.savePreferences();

                myToastMessage = getResources().getString(R.string.options_set_clear) + " " + getResources().getString(R.string.ok);

                // Refresh all stuff needed
                refreshAll();
                break;

            case "deletesong":
                // Delete current song
                setView = "N";
                String setFileLocation;
                if (whichSongFolder.equals(mainfoldername)) {
                    setFileLocation = dir + "/" + songfilename;
                } else {
                    setFileLocation = dir + "/" + whichSongFolder + "/" + songfilename;
                }
                File filetoremove = new File(setFileLocation);
                if (filetoremove.delete()) {
                    myToastMessage = "\"" + songfilename + "\" "
                            + getResources().getString(R.string.songhasbeendeleted);
                } else {
                    myToastMessage = getResources().getString(R.string.deleteerror_start)
                            + " \"" + songfilename + "\" "
                            + getResources().getString(R.string.deleteerror_end_song);
                }
                invalidateOptionsMenu();

                // Save the new, empty, set
                Preferences.savePreferences();

                // Refresh all stuff needed
                refreshAll();
                break;

            case "deleteset":
                // Load the set up
                // Sets can be multiple sets
                String[] tempsets = FullscreenActivity.setnamechosen.split("%_%");
                myToastMessage = "";
                for (String tempfile:tempsets) {
                    if (tempfile!=null && !tempfile.equals("") && !tempfile.isEmpty()) {
                        File settodelete = new File(dirsets + "/" + tempfile);
                        if (settodelete.delete()) {
                            myToastMessage = myToastMessage + tempfile + ", ";

                        }
                    }
                }
                myToastMessage = myToastMessage.substring(0,myToastMessage.length()-2);
                myToastMessage = myToastMessage + " " + getResources().getString(R.string.sethasbeendeleted);

                // Refresh all stuff needed
                refreshAll();
                break;
        }
    }

    public void indexMySongs() {
        // Build an array containing the song fields (all of the searchable ones)
        // search_database has 8 columns for each song
        // Columns are folder, filename, title, author, lyrics, theme, key, hymn number
        try {
            IndexSongs.doIndex(this);
        } catch (Exception e) {
            e.printStackTrace();
            myToastMessage = getString(R.string.search_index_error)+"\n"+getString(R.string.search_log);
            ShowToast.showToast(FullscreenActivity.this);
            safetosearch = true;
            SharedPreferences indexSongPreferences = getSharedPreferences("indexsongs",MODE_PRIVATE);
            SharedPreferences.Editor editor_index = indexSongPreferences.edit();
            editor_index.putBoolean("buildSearchIndex", true);
            editor_index.apply();
        }
    }

    public void copyDefaultFiles() {
        // Look for default background image, logo and Love Everlasting
        AssetManager assetManager_bg = getAssets();
        InputStream in;
        OutputStream out;

        String filename;
        try {
            // Default background image
            filename = "ost_bg.png";
            in = assetManager_bg.open("backgrounds" + File.separator + filename);
            File outFile = new File(FullscreenActivity.dirbackgrounds, filename);
            if (!outFile.exists()) {
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                out.flush();
                out.close();
            }
            in.close();

        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file: " + "ost_bg.png", e);
        }

        try {
            // Default logo image
            filename = "ost_logo.png";
            in = assetManager_bg.open("backgrounds" + File.separator + filename);
            File outFile = new File(FullscreenActivity.dirbackgrounds, filename);
            if (!outFile.exists()) {
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                out.flush();
                out.close();
            }
            in.close();
        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file: " + "ost_logo.png", e);
        }

        try {
            // Default song Love Everlasting
            filename = "Love everlasting";
            in = assetManager_bg.open("Songs" + File.separator + filename);
            File outFile = new File(FullscreenActivity.dir, filename);
            if (!outFile.exists()) {
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                out.flush();
                out.close();
            }
            in.close();
        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file: " + "Love Everlasting", e);
        }
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}