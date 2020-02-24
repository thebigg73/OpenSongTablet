package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.mediarouter.app.MediaRouteActionProvider;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutServiceData;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lib.folderpicker.FolderPicker;

public class StageMode extends AppCompatActivity implements
        PopUpAreYouSureFragment.MyInterface, PopUpPagesFragment.MyInterface,
        PopUpEditSongFragment.MyInterface, PopUpSongDetailsFragment.MyInterface,
        PopUpPresentationOrderFragment.MyInterface, PopUpListSetsFragment.MyInterface,
        SongMenuListeners.MyInterface, OptionMenuListeners.MyInterface, MenuHandlers.MyInterface,
        SetActions.MyInterface, PopUpFullSearchFragment.MyInterface, IndexSongs.MyInterface,
        SearchView.OnQueryTextListener, PopUpSetViewNew.MyInterface,
        PopUpChooseFolderFragment.MyInterface, PopUpCustomSlideFragment.MyInterface,
        PopUpImportExternalFile.MyInterface,
        PopUpBackupPromptFragment.MyInterface,
        PopUpSongFolderRenameFragment.MyInterface, PopUpThemeChooserFragment.MyInterface,
        PopUpExtraInfoFragment.MyInterface,
        PopUpPageButtonsFragment.MyInterface, PopUpScalingFragment.MyInterface,
        PopUpFontsFragment.MyInterface, PopUpTransposeFragment.MyInterface,
        PopUpEditStickyFragment.MyInterface, PopUpSongRenameFragment.MyInterface,
        PopUpSongCreateFragment.MyInterface, PopUpFileChooseFragment.MyInterface,
        PopUpPadFragment.MyInterface, PopUpAutoscrollFragment.MyInterface,
        PopUpMetronomeFragment.MyInterface, PopUpChordsFragment.MyInterface,
        PopUpStickyFragment.MyInterface, PopUpLinks.MyInterface, PopUpCustomChordsFragment.MyInterface,
        PopUpQuickLaunchSetup.MyInterface, PopUpLongSongPressFragment.MyInterface,
        PopUpFindNewSongsFragment.MyInterface, PopUpGroupedPageButtonsFragment.MyInterface,
        PopUpImportExportOSBFragment.MyInterface, SalutDataCallback, SongMenuAdapter.MyInterface,
        BatteryMonitor.MyInterface, PopUpMenuSettingsFragment.MyInterface,
        PopUpLayoutFragment.MyInterface, DownloadTask.MyInterface,
        PopUpExportFragment.MyInterface, PopUpActionBarInfoFragment.MyInterface,
        PopUpCreateDrawingFragment.MyInterface,
        PopUpPDFToTextFragment.MyInterface, PopUpRandomSongFragment.MyInterface,
        PopUpCCLIFragment.MyInterface,
        PopUpBibleXMLFragment.MyInterface, PopUpShowMidiMessageFragment.MyInterface {

    // The toolbar and menu
    private Toolbar ab_toolbar;
    public static ActionBar ab;
    private TextView digitalclock;
    private TextView songtitle_ab;
    private TextView songkey_ab;
    private TextView songcapo_ab;
    private TextView songauthor_ab;
    private TextView batterycharge;
    private ImageView batteryimage;

    // The popup window (sticky)
    private PopupWindow stickyPopUpWindow;

    // The left and right menu
    private DrawerLayout mDrawerLayout;
    private TextView menuFolder_TextView;
    private TextView menuCount_TextView;
    private LinearLayout songmenu;
    private LinearLayout optionmenu;
    private ListView song_list_view;

    // Song sections view
    private RelativeLayout mypage;
    private int songwidth = 0;
    private int songheight = 0;
    private HorizontalScrollView horizontalscrollview;
    private HorizontalScrollView glideimage_HorizontalScrollView;
    private RelativeLayout testpane;
    private RelativeLayout testpane1_2;
    private RelativeLayout testpane2_2;
    private RelativeLayout testpane1_3;
    private RelativeLayout testpane2_3;
    private RelativeLayout testpane3_3;
    private LinearLayout column1_1;
    private LinearLayout column1_2;
    private LinearLayout column2_2;
    private LinearLayout column1_3;
    private LinearLayout column2_3;
    private LinearLayout column3_3;
    private ScrollView songscrollview;
    private ScrollView glideimage_ScrollView;
    private FrameLayout glideimage_FrameLayout;
    private ImageView glideimage;
    private ImageView highlightNotes;
    private LinearLayout backingtrackProgress;
    private LinearLayout playbackProgress;
    private LinearLayout capoInfo;
    private LinearLayout learnAutoScroll;
    private TextView padcurrentTime_TextView;
    private TextView padTimeSeparator_TextView;
    private TextView padtotalTime_TextView;
    private TextView currentTime_TextView;
    private TextView timeSeparator_TextView;
    private TextView totalTime_TextView;
    private TextView capoinfo;
    private TextView capoinfonewkey;
    private TextView learnAutoScroll_TextView;
    private TextView learnAutoScrollTime_TextView;
    private float width_scale = 0f;
    private boolean sectionpresented = false;

    private int coltouse = 1;
    private boolean longKeyPress = false;
    private boolean shortKeyPress = false;

    // Page buttons
    private FloatingActionButton setButton;
    private FloatingActionButton padButton;
    private FloatingActionButton autoscrollButton;
    private FloatingActionButton metronomeButton;
    private FloatingActionButton extraButton;
    private FloatingActionButton chordButton;
    private FloatingActionButton stickyButton;
    private FloatingActionButton notationButton;
    private FloatingActionButton highlightButton;
    private FloatingActionButton pageselectButton;
    private FloatingActionButton linkButton;
    private FloatingActionButton chordButton_ungrouped;
    private FloatingActionButton stickyButton_ungrouped;
    private FloatingActionButton notationButton_ungrouped;
    private FloatingActionButton highlightButton_ungrouped;
    private FloatingActionButton pageselectButton_ungrouped;
    private FloatingActionButton linkButton_ungrouped;
    private FloatingActionButton customButton;
    private FloatingActionButton custom1Button;
    private FloatingActionButton custom2Button;
    private FloatingActionButton custom3Button;
    private FloatingActionButton custom4Button;
    private FloatingActionButton custom1Button_ungrouped;
    private FloatingActionButton custom2Button_ungrouped;
    private FloatingActionButton custom3Button_ungrouped;
    private FloatingActionButton custom4Button_ungrouped;
    private FloatingActionButton scrollDownButton;
    private FloatingActionButton scrollUpButton;
    private FloatingActionButton setBackButton;
    private FloatingActionButton setForwardButton;
    private ScrollView extrabuttons;
    private ScrollView extrabuttons2;
    private int keyRepeatCount = 0;

    //private CoordinatorLayout coordinator_layout;

    // MIDI
    private Midi midi;

    // Casting
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private final MyMediaRouterCallback mMediaRouterCallback = new MyMediaRouterCallback();
    private CastDevice mSelectedDevice;
    private PresentationServiceHDMI hdmi;
    private boolean newsongloaded = false;

    // Dialogue fragments and stuff
    private DialogFragment newFragment;

    // Gestures
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // ASyncTask stuff
    private AsyncTask<Object, Void, String> loadsong_async;
    private AsyncTask<Object, Void, String> preparesongview_async;
    private AsyncTask<Object, Void, String> createperformanceview1col_async;
    private AsyncTask<Object, Void, String> createperformanceview2col_async;
    private AsyncTask<Object, Void, String> createperformanceview3col_async;
    private AsyncTask<Object, Void, String> preparesongmenu_async;
    private AsyncTask<Object, Void, String> prepareoptionmenu_async;
    private AsyncTask<Object, Void, String> createstageview1col_async;
    private AsyncTask<Object, Void, String> fadeout_media1;
    private AsyncTask<Object, Void, String> fadeout_media2;
    private AsyncTask<Object, Void, String> check_storage;
    private AsyncTask<Object, Void, String> sharesong_async;
    private AsyncTask<Object, Void, String> shareset_async;
    private AsyncTask<Object, Void, String> load_customreusable;
    private AsyncTask<Object, Void, String> open_drawers;
    private AsyncTask<Object, Void, String> close_drawers;
    private AsyncTask<Object, Void, String> resize_drawers;
    private AsyncTask<Object, Void, String> do_moveinset;
    private AsyncTask<Object, Void, String> add_slidetoset;
    private AsyncTask<Object, Void, String> dualscreenwork_async;
    private AsyncTask<Object, Void, String> show_sticky;
    private AsyncTask<Object, Void, String> show_highlight;
    private AsyncTask<Object, Void, String> shareactivitylog_async;
    private AsyncTask<Void, Void, String> resizeperformance_async;
    private AsyncTask<Void, Void, String> resizestage_async;
    private AsyncTask<String, Integer, String> mtask_autoscroll_music;
    private AsyncTask<Void, Void, Integer> prepare_pad;
    private AsyncTask<Void, Void, Integer> play_pads;
    private AsyncTask<String, Integer, String> do_download;
    private AsyncTask<Object, Integer, String> get_scrollheight;

    // Allow the menus to flash open to show where they are on first run
    private boolean firstrun_option = true;
    private boolean firstrun_song = true;

    // Handlers and Runnables
    private final Runnable padoncheck = new Runnable() {
        @Override
        public void run() {
            getPadsOnStatus();
        }
    };
    private final Handler handle = new Handler();
    private final Handler dopadProgressTime = new Handler();
    private final Runnable padprogressTimeRunnable = new Runnable() {
        @Override
        public void run() {
            getPadProgress();
        }
    };
    private final Runnable onEverySecond = new Runnable() {
        @Override
        public void run() {
            preparePadProgress();
        }
    };
    private Handler delaycheckscroll;
    private Runnable checkScrollPosition;
    private final Handler mRestoreImmersiveModeHandler = new Handler();
    private final Runnable restoreImmersiveModeRunnable = new Runnable() {
        public void run() {
            restoreTransparentBars();
        }
    };
    private final Handler delayactionBarHide = new Handler();
    private final Runnable hideActionBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (ab != null && ab.isShowing()) {
                ab.hide();
            }
        }
    };
    // Handlers for fonts
    private Handler lyrichandler;
    private Handler chordhandler;
    private Handler stickyhandler;
    private Handler presohandler;
    private Handler presoinfohandler;
    private Handler customhandler;

    // Network discovery / connections
    private static final String TAG = "StageMode";
    private SalutMessage myMessage;
    private SalutMessage mySongMessage;
    private SalutMessage mySectionMessage;

    // NFC
    private FileUriCallback mFileUriCallback;

    // Battery
    private BroadcastReceiver br;

    // Songs in this folder for swiping
    private ArrayList<String> filenamesSongsInFolder;

    // Helper classes
    private ExportPreparer exportPreparer;
    private SetActions setActions;
    private StorageAccess storageAccess;
    private IndexSongs indexSongs;
    private Preferences preferences;
    private SongXML songXML;
    private ChordProConvert chordProConvert;
    private OnSongConvert onSongConvert;
    private UsrConvert usrConvert;
    private TextSongConvert textSongConvert;
    private SetTypeFace setTypeFace;
    private SQLiteHelper sqLiteHelper;
    private ProcessSong processSong;
    private SQLite sqLite;  // This is the song values for the sqlite database, search and menus
    private NonOpenSongSQLite nonOpenSongSQLite; // For the pdf and image songs
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private Transpose transpose;
    private ProfileActions profileActions;

    private boolean pdfCanContinueScrolling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("StageMode", "Welcome to Stage Mode");
        StaticVariables.activity = StageMode.this;
        FullscreenActivity.mContext = StageMode.this;
        FullscreenActivity.appRunning = true;

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setActions = new SetActions();
        exportPreparer = new ExportPreparer();
        storageAccess = new StorageAccess();
        indexSongs = new IndexSongs();
        preferences = new Preferences();
        songXML = new SongXML();
        chordProConvert = new ChordProConvert();
        onSongConvert = new OnSongConvert();
        usrConvert = new UsrConvert();
        textSongConvert = new TextSongConvert();
        setTypeFace = new SetTypeFace();
        sqLiteHelper = new SQLiteHelper(StageMode.this);
        nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(StageMode.this);
        processSong = new ProcessSong();
        transpose = new Transpose();
        profileActions = new ProfileActions();

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StaticVariables.myToastMessage = getString(R.string.search_index_start);
                        ShowToast.showToast(StageMode.this);
                    }
                });
                indexSongs.fullIndex(StageMode.this,preferences,storageAccess,sqLiteHelper,songXML,
                        chordProConvert,onSongConvert,textSongConvert,usrConvert);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StaticVariables.myToastMessage = getString(R.string.search_index_end);
                        ShowToast.showToast(StageMode.this);
                        // Now instruct the song menu to be built again.
                        prepareSongMenu();
                    }
                });

            }
        }).start();

        // Get the language
        FixLocale.fixLocale(StageMode.this,preferences);

        // Initialise the font handlers
        lyrichandler = new Handler();
        chordhandler = new Handler();
        stickyhandler = new Handler();
        presohandler = new Handler();
        presoinfohandler = new Handler();
        customhandler = new Handler();

        // Check we have permission to use the storage
        checkStorage();

        // Load the layout and set the title
        setContentView(R.layout.stage_mode);

        // In order to quickly start, load the minimum variables we need
        loadStartUpVariables();

        // Set up the fonts
        setTypeFace.setUpAppFonts(StageMode.this, preferences, lyrichandler, chordhandler, stickyhandler,
                presohandler, presoinfohandler, customhandler);

        // Setup the CastContext
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast("4E2B0891"))
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Set the fullscreen window flags
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setWindowFlags();
                        setWindowFlagsAdvanced();
                    }
                });

                // Since this mode has just been opened, force an update to the cast screen
                StaticVariables.forcecastupdate = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Set up the gesture detector
                        scaleGestureDetector = new ScaleGestureDetector(StageMode.this, new simpleOnScaleGestureListener());
                        gestureDetector = new GestureDetector(StageMode.this,new SwipeDetector());

                        // Set up the toolbar and views
                        setUpToolbar();
                        setUpViews();
                    }
                });

                // Battery monitor
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                br = new BatteryMonitor();
                StageMode.this.registerReceiver(br, filter);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Make the drawers match half the width of the screen
                        resizeDrawers();

                        // Prepare the song menu
                        prepareSongMenu();

                        // Prepare the option menu
                        prepareOptionMenu();

                        // Set up the page buttons
                        setupPageButtons();

                        // Load the song and get started
                        loadSong();

                        // Prepare the scrollbuttons
                        scrollButtons();

                        // Prepare abhide listener
                        setupAbHide();
                    }
                });

                // Set up the Salut service
                getBluetoothName();
                startRegistration();

                dealWithIntent();

                // Set up stuff for NFC transfer (if allowed)
                if (FullscreenActivity.mAndroidBeamAvailable) {
                    FullscreenActivity.mNfcAdapter = NfcAdapter.getDefaultAdapter(StageMode.this);
                    mFileUriCallback = new FileUriCallback();
                    // Set the dynamic callback for URI requests.
                    FullscreenActivity.mNfcAdapter.setBeamPushUrisCallback(mFileUriCallback, StageMode.this);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Initialise the ab info
                        adjustABInfo();
                    }
                });

            }
        }).start();

        // Check if we need to remind the user to backup their songs
        checkBackupState();
    }

    // Load the variables we need
    private void loadStartUpVariables() {

        StaticVariables.mDisplayTheme = preferences.getMyPreferenceString(StageMode.this, "appTheme", "dark");

        // The mode we are in
        StaticVariables.whichMode = preferences.getMyPreferenceString(StageMode.this, "whichMode", "Performance");

        // Song location
        loadFileLocation();
    }

    private void setUpToolbar() {
        ab_toolbar = findViewById(R.id.mytoolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(ab_toolbar);                     // Setting toolbar as the ActionBar with setSupportActionBar() call
        ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(false); // show or hide the default home button
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
            ab.setDisplayShowTitleEnabled(false);
        }
    }

    // Set the colours from preferences
    private int lyricsTextColor;
    private int lyricsBackgroundColor;
    private int lyricsCapoColor;
    private int lyricsVerseColor;
    private int lyricsChorusColor;
    private int lyricsBridgeColor;
    private int lyricsCommentColor;
    private int lyricsPreChorusColor;
    private int lyricsTagColor;
    private int lyricsChordsColor;
    private int lyricsCustomColor;
    private int presoFontColor;
    private int defmetronomecolor;
    private int defpagebuttoncolor;
    private int defstickytextcolor;
    private int defstickybgcolor;
    private int defextrainfobgcolor;
    private int defextrainfotextcolor;
    private void getDefaultColors() {
        switch (StaticVariables.mDisplayTheme) {
            case "dark":
            default:
                setThemeDark();
                break;
            case "light":
                setThemeLight();
                break;
            case "custom1":
                setThemeCustom1();
                break;
            case "custom2":
                setThemeCustom2();
                break;
        }
    }
    private void setThemeDark() {
        defmetronomecolor = preferences.getMyPreferenceInt(StageMode.this, "dark_metronomeColor", StaticVariables.darkishred);
        defpagebuttoncolor = preferences.getMyPreferenceInt(StageMode.this, "dark_pageButtonsColor", StaticVariables.purplyblue);
        defstickytextcolor = preferences.getMyPreferenceInt(StageMode.this, "dark_stickyTextColor", StaticVariables.black);
        defstickybgcolor = preferences.getMyPreferenceInt(StageMode.this, "dark_stickyBackgroundColor", StaticVariables.lightyellow);
        defextrainfobgcolor = preferences.getMyPreferenceInt(StageMode.this, "dark_extraInfoBgColor", StaticVariables.grey);
        defextrainfotextcolor = preferences.getMyPreferenceInt(StageMode.this, "dark_extraInfoTextColor", StaticVariables.white);
        lyricsTextColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsTextColor", StaticVariables.white);
        lyricsCapoColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsCapoColor", StaticVariables.red);
        lyricsBackgroundColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsBackgoundColour", StaticVariables.black);
        lyricsVerseColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsVerseColor", StaticVariables.black);
        lyricsChorusColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsChorusColor", StaticVariables.vdarkblue);
        lyricsBridgeColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsBridgeColor", StaticVariables.vdarkred);
        lyricsCommentColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsCommentColor", StaticVariables.vdarkgreen);
        lyricsPreChorusColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsPreChorusColor", StaticVariables.darkishgreen);
        lyricsTagColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsTagColor", StaticVariables.darkpurple);
        lyricsChordsColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsChordsColor", StaticVariables.yellow);
        lyricsCustomColor = preferences.getMyPreferenceInt(StageMode.this, "dark_lyricsCustomColor", StaticVariables.vdarkyellow);
        presoFontColor = preferences.getMyPreferenceInt(StageMode.this, "dark_presoFontColor", StaticVariables.white);
//      presoShadowColor = preferences.getMyPreferenceInt(StageMode.this, "dark_presoShadowColor", StaticVariables.black);
    }
    private void setThemeLight() {
        defmetronomecolor       = preferences.getMyPreferenceInt(StageMode.this,"light_metronomeColor",         StaticVariables.darkishred);
        defpagebuttoncolor      = preferences.getMyPreferenceInt(StageMode.this,"light_pageButtonsColor",       StaticVariables.purplyblue);
        defstickytextcolor      = preferences.getMyPreferenceInt(StageMode.this,"light_stickyTextColor",        StaticVariables.black);
        defstickybgcolor        = preferences.getMyPreferenceInt(StageMode.this,"light_stickyBackgroundColor",  StaticVariables.lightyellow);
        defextrainfobgcolor     = preferences.getMyPreferenceInt(StageMode.this,"light_extraInfoBgColor",       StaticVariables.grey);
        defextrainfotextcolor   = preferences.getMyPreferenceInt(StageMode.this,"light_extraInfoTextColor",     StaticVariables.white);
        lyricsTextColor         = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsTextColor",        StaticVariables.black);
        lyricsCapoColor         = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsCapoColor",        StaticVariables.red);
        lyricsBackgroundColor   = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsBackgoundColour",  StaticVariables.white);
        lyricsVerseColor        = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsVerseColor",       StaticVariables.white);
        lyricsChorusColor       = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsChorusColor",      StaticVariables.vlightpurple);
        lyricsBridgeColor       = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsBridgeColor",      StaticVariables.vlightcyan);
        lyricsCommentColor      = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsCommentColor",     StaticVariables.vlightblue);
        lyricsPreChorusColor    = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsPreChorusColor",   StaticVariables.lightgreen);
        lyricsTagColor          = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsTagColor",         StaticVariables.vlightgreen);
        lyricsChordsColor       = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsChordsColor",      StaticVariables.darkblue);
        lyricsCustomColor       = preferences.getMyPreferenceInt(StageMode.this,"light_lyricsCustomColor",      StaticVariables.lightishcyan);
        presoFontColor          = preferences.getMyPreferenceInt(StageMode.this,"light_presoFontColor",         StaticVariables.white);
//      presoShadowColor        = preferences.getMyPreferenceInt(StageMode.this,"light_presoShadowColor",       StaticVariables.black);
    }
    private void setThemeCustom1() {
        defmetronomecolor       = preferences.getMyPreferenceInt(StageMode.this,"custom1_metronomeColor",       StaticVariables.darkishred);
        defpagebuttoncolor      = preferences.getMyPreferenceInt(StageMode.this,"custom1_pageButtonsColor",     StaticVariables.purplyblue);
        defstickytextcolor      = preferences.getMyPreferenceInt(StageMode.this,"custom1_stickyTextColor",      StaticVariables.black);
        defstickybgcolor        = preferences.getMyPreferenceInt(StageMode.this,"custom1_stickyBackgroundColor",StaticVariables.lightyellow);
        defextrainfobgcolor     = preferences.getMyPreferenceInt(StageMode.this,"custom1_extraInfoBgColor",     StaticVariables.grey);
        defextrainfotextcolor   = preferences.getMyPreferenceInt(StageMode.this, "custom1_extraInfoTextColor",  StaticVariables.white);
        lyricsTextColor         = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsTextColor",      StaticVariables.white);
        lyricsCapoColor         = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsCapoColor",      StaticVariables.red);
        lyricsBackgroundColor   = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsBackgoundColour",StaticVariables.black);
        lyricsVerseColor        = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsVerseColor",     StaticVariables.black);
        lyricsChorusColor       = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsChorusColor",    StaticVariables.black);
        lyricsBridgeColor       = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsBridgeColor",    StaticVariables.black);
        lyricsCommentColor      = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsCommentColor",   StaticVariables.black);
        lyricsPreChorusColor    = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsPreChorusColor", StaticVariables.black);
        lyricsTagColor          = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsTagColor",       StaticVariables.black);
        lyricsChordsColor       = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsChordsColor",    StaticVariables.yellow);
        lyricsCustomColor       = preferences.getMyPreferenceInt(StageMode.this,"custom1_lyricsCustomColor",    StaticVariables.black);
        presoFontColor          = preferences.getMyPreferenceInt(StageMode.this,"custom1_presoFontColor",       StaticVariables.white);
//      presoShadowColor        = preferences.getMyPreferenceInt(StageMode.this,"custom1_presoShadowColor",     StaticVariables.black);
    }
    private void setThemeCustom2() {
        defmetronomecolor       = preferences.getMyPreferenceInt(StageMode.this,"custom2_metronomeColor",       StaticVariables.darkishred);
        defpagebuttoncolor      = preferences.getMyPreferenceInt(StageMode.this,"custom2_pageButtonsColor",     StaticVariables.purplyblue);
        defstickytextcolor      = preferences.getMyPreferenceInt(StageMode.this,"custom2_stickyTextColor",      StaticVariables.black);
        defstickybgcolor        = preferences.getMyPreferenceInt(StageMode.this,"custom2_stickyBackgroundColor",StaticVariables.lightyellow);
        defextrainfobgcolor     = preferences.getMyPreferenceInt(StageMode.this,"custom2_extraInfoBgColor",     StaticVariables.grey);
        defextrainfotextcolor   = preferences.getMyPreferenceInt(StageMode.this,"custom2_extraInfoTextColor",   StaticVariables.white);
        lyricsTextColor         = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsTextColor",      StaticVariables.black);
        lyricsCapoColor         = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsCapoColor",      StaticVariables.red);
        lyricsBackgroundColor   = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsBackgoundColour",StaticVariables.white);
        lyricsVerseColor        = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsVerseColor",     StaticVariables.white);
        lyricsChorusColor       = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsChorusColor",    StaticVariables.white);
        lyricsBridgeColor       = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsBridgeColor",    StaticVariables.white);
        lyricsCommentColor      = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsCommentColor",   StaticVariables.white);
        lyricsPreChorusColor    = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsPreChorusColor", StaticVariables.white);
        lyricsTagColor          = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsTagColor",       StaticVariables.white);
        lyricsChordsColor       = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsChordsColor",    StaticVariables.darkblue);
        lyricsCustomColor       = preferences.getMyPreferenceInt(StageMode.this,"custom2_lyricsCustomColor",    StaticVariables.white);
        presoFontColor          = preferences.getMyPreferenceInt(StageMode.this,"custom2_presoFontColor",       StaticVariables.white);
//      presoShadowColor        = preferences.getMyPreferenceInt(StageMode.this,"custom2_presoShadowColor",     StaticVariables.black);
    }

    private void setUpViews() {
        getDefaultColors();
        RelativeLayout songandauthor = findViewById(R.id.songandauthor);
        digitalclock = findViewById(R.id.digitalclock);
        songtitle_ab = findViewById(R.id.songtitle_ab);
        songkey_ab = findViewById(R.id.songkey_ab);
        songcapo_ab = findViewById(R.id.songcapo_ab);
        songauthor_ab = findViewById(R.id.songauthor_ab);
        batterycharge = findViewById(R.id.batterycharge);
        batteryimage = findViewById(R.id.batteryimage);
        RelativeLayout batteryholder = findViewById(R.id.batteryholder);
        mypage = findViewById(R.id.mypage);
        //mypage.init(StageMode.this);
        mypage.setBackgroundColor(lyricsBackgroundColor);

        // Set up the pad and autoscroll timing display
        capoinfo = findViewById(R.id.capoinfo);
        capoinfonewkey = findViewById(R.id.capoinfonewkey);
        learnAutoScroll = findViewById(R.id.learnAutoScroll);
        learnAutoScroll.setVisibility(View.GONE);
        learnAutoScroll_TextView = findViewById(R.id.learnAutoScroll_TextView);
        learnAutoScrollTime_TextView = findViewById(R.id.learnAutoScrollTime_TextView);
        backingtrackProgress = findViewById(R.id.backingtrackProgress);
        backingtrackProgress.setVisibility(View.GONE);
        padcurrentTime_TextView = findViewById(R.id.padcurrentTime_TextView);
        // Allow the user to pause/resume playback
        backingtrackProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PadFunctions.pauseOrResumePad();
            }
        });
        padTimeSeparator_TextView = findViewById(R.id.padTimeSeparator_TextView);
        padtotalTime_TextView = findViewById(R.id.padtotalTime_TextView);
        playbackProgress = findViewById(R.id.playbackProgress);
        playbackProgress.setVisibility(View.GONE);
        capoInfo = findViewById(R.id.capoInfo);
        capoInfo.setVisibility(View.GONE);
        currentTime_TextView = findViewById(R.id.currentTime_TextView);
        timeSeparator_TextView = findViewById(R.id.timeSeparator_TextView);
        totalTime_TextView = findViewById(R.id.totalTime_TextView);

        // Identify the views being used
        songscrollview = findViewById(R.id.songscrollview);
        horizontalscrollview = findViewById(R.id.horizontalscrollview);

        glideimage_ScrollView = findViewById(R.id.glideimage_ScrollView);
        glideimage_HorizontalScrollView = findViewById(R.id.glideimage_HorizontalScrollView);
        glideimage_FrameLayout = findViewById(R.id.glideimage_FrameLayout);
        glideimage = findViewById(R.id.glideimage);
        testpane = findViewById(R.id.testpane);
        testpane1_2 = findViewById(R.id.testpane1_2);
        testpane2_2 = findViewById(R.id.testpane2_2);
        testpane1_3 = findViewById(R.id.testpane1_3);
        testpane2_3 = findViewById(R.id.testpane2_3);
        testpane3_3 = findViewById(R.id.testpane3_3);
        highlightNotes = findViewById(R.id.highlightNotes);

        songscrollview.setBackgroundColor(lyricsBackgroundColor);
        //songscrollview.setBackgroundColor(0xff0000ff);

        // Enable the song and author section to link to edit song
        songandauthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FullscreenActivity.isPDF) {
                    FullscreenActivity.whattodo = "extractPDF";
                    openFragment();
                } else if (FullscreenActivity.isSong) {
                    FullscreenActivity.whattodo = "songdetails";
                    openFragment();
                }
            }
        });
        batteryholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "actionbarinfo";
                openFragment();
            }
        });

        // Set up the navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        songmenu = findViewById(R.id.songmenu);
        optionmenu = findViewById(R.id.optionmenu);
        song_list_view = findViewById(R.id.song_list_view);
        FloatingActionButton closeSongFAB = findViewById(R.id.closeSongsFAB);
        menuFolder_TextView = findViewById(R.id.menuFolder_TextView);
        menuFolder_TextView.setText(getString(R.string.wait));
        menuCount_TextView = findViewById(R.id.menuCount_TextView);
        LinearLayout changefolder_LinearLayout = findViewById(R.id.changefolder_LinearLayout);
        changefolder_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "choosefolder";
                openFragment();
            }
        });
        closeSongFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMyDrawers("song");
            }
        });
    }

    private void getBluetoothName() {
        try {
            if(FullscreenActivity.mBluetoothAdapter == null){
                FullscreenActivity.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            FullscreenActivity.mBluetoothName = FullscreenActivity.mBluetoothAdapter.getName();
            if(FullscreenActivity.mBluetoothName == null){
                FullscreenActivity.mBluetoothName = "Unknown";
            }
        } catch (Exception e) {
            FullscreenActivity.mBluetoothName = "Unknown";
        }
    }

    private void startRegistration() {
        try {
            FullscreenActivity.dataReceiver = new SalutDataReceiver(StageMode.this, StageMode.this);
            FullscreenActivity.serviceData = new SalutServiceData("OpenSongApp", 60606,
                    FullscreenActivity.mBluetoothName);

            FullscreenActivity.network = new Salut(FullscreenActivity.dataReceiver, FullscreenActivity.serviceData, new SalutCallback() {
                @Override
                public void call() {
                    FullscreenActivity.salutLog += "\n" + getResources().getString(R.string.nowifidirect);
                }
            });

        } catch (Exception e) {
            FullscreenActivity.salutLog += "\n" + getResources().getString(R.string.nowifidirect);
            e.printStackTrace();
        }
    }

    // Window decoration
    @Override
    public void windowFlags() {
        setWindowFlags();
        setWindowFlagsAdvanced();
    }
    private void setWindowFlags() {
        View v = getWindow().getDecorView();
        v.setOnSystemUiVisibilityChangeListener(null);
        v.setOnFocusChangeListener(null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    private void setWindowFlagsAdvanced() {
        View v = getWindow().getDecorView();
        v.setOnSystemUiVisibilityChangeListener(null);
        v.setOnFocusChangeListener(null);

        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Runnable testnavbar = new Runnable() {
            @Override
            public void run() {
                getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
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
        waitandtest.postDelayed(testnavbar, 1000);
    }
    private void restoreTranslucentBarsDelayed() {
        // we restore it now and after 500 ms!
        restoreTransparentBars();
        mRestoreImmersiveModeHandler.postDelayed(restoreImmersiveModeRunnable, 500);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            setWindowFlags();
            setWindowFlagsAdvanced();
            restoreTranslucentBarsDelayed();
        }
    }
    private void restoreTransparentBars() {
        // Set runnable
        Runnable delhide = new Runnable() {
            @Override
            public void run() {
                // Hide them
                setWindowFlags();
                setWindowFlagsAdvanced();
                View rf = getCurrentFocus();
                if (rf!=null) {
                    rf.clearFocus();
                }
            }
        };

        // Wait for 1000ms then check for Navigation bar visibility
        // If it is there, hide it
        Handler delayhidehandler = new Handler();
        delayhidehandler.postDelayed(delhide, 1000);
    }

    // Action bar stuff
    private void toggleActionBar() {
        try {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ab != null) {
            if (FullscreenActivity.wasscrolling || FullscreenActivity.scrollbutton) {
                if (preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false) &&
                        !songmenu.isFocused() && !songmenu.isShown() && !optionmenu.isFocused() && !optionmenu.isShown()) {
                    ab.hide();
                }
            } else if (!songmenu.isFocused() && !songmenu.isShown() && !optionmenu.isFocused() && !optionmenu.isShown()) {
                if (ab.isShowing() && preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
                    delayactionBarHide.postDelayed(hideActionBarRunnable, 500);
                } else {
                    ab.show();
                    // Set a runnable to hide it after 3 seconds
                    if (preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
                        delayactionBarHide.postDelayed(hideActionBarRunnable, 3000);
                    }
                }
            }
        }
    }
    private void setupAbHide() {
        // What happens when the navigation drawers are opened
        // Called when a drawer has settled in a completely closed state.
        // Set a runnable to re-enable swipe
        // enable swipe after short delay
        // 1800ms delay
        // Called when a drawer has settled in a completely open state.
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(StageMode.this, mDrawerLayout, ab_toolbar, R.string.drawer_open, R.string.drawer_close) {
            // Called when a drawer has settled in a completely closed state.
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // Set a runnable to re-enable swipe
                Handler allowswipe = new Handler();
                allowswipe.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.tempswipeSet = "enable"; // enable swipe after short delay
                    }
                }, FullscreenActivity.delayswipe_time); // 1800ms delay
                hideActionBar();
                setupPageButtons();
            }

            // Called when a drawer has settled in a completely open state.
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                FullscreenActivity.tempswipeSet = "disable";
                FullscreenActivity.wasscrolling = false;
                FullscreenActivity.scrollbutton = false;
                hideActionBar();

                if (!ab.isShowing()) {
                    ab.show();
                }
            }
        };

        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                restoreTransparentBars();
            }
        });

        decorView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                restoreTransparentBars();
            }
        });

    }
    @Override
    public void adjustABInfo() {
        // Change the visibilities
        if (preferences.getMyPreferenceBoolean(StageMode.this,"batteryDialOn",true)) {
            batteryimage.setVisibility(View.VISIBLE);
        } else {
            batteryimage.setVisibility(View.INVISIBLE);
        }
        if (preferences.getMyPreferenceBoolean(StageMode.this,"batteryTextOn",true)) {
            batterycharge.setVisibility(View.VISIBLE);
        } else {
            batterycharge.setVisibility(View.GONE);
        }
        if (preferences.getMyPreferenceBoolean(StageMode.this,"clockOn",true)) {
            digitalclock.setVisibility(View.VISIBLE);
        } else {
            digitalclock.setVisibility(View.GONE);
        }

        // Set the text sizes
        batterycharge.setTextSize(preferences.getMyPreferenceFloat(StageMode.this, "batteryTextSize",9.0f));
        digitalclock.setTextSize(preferences.getMyPreferenceFloat(StageMode.this,"clockTextSize",9.0f));
        songtitle_ab.setTextSize(preferences.getMyPreferenceFloat(StageMode.this,"songTitleSize",13.0f));
        songtitle_ab.setSingleLine(true);
        songcapo_ab.setTextSize(preferences.getMyPreferenceFloat(StageMode.this,"songTitleSize",13.0f));
        songcapo_ab.setSingleLine(true);
        songauthor_ab.setTextSize(preferences.getMyPreferenceFloat(StageMode.this,"songAuthorSize",11.0f));
        songauthor_ab.setSingleLine(true);
        songkey_ab.setTextSize(preferences.getMyPreferenceFloat(StageMode.this,"songTitleSize",13.0f));
        songkey_ab.setSingleLine(true);

        // Set the time format
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df;
        if (preferences.getMyPreferenceBoolean(StageMode.this,"clock24hFormat",true)) {
            df = new SimpleDateFormat("HH:mm", StaticVariables.locale);
        } else {
            df = new SimpleDateFormat("h:mm", StaticVariables.locale);
        }
        String formattedTime = df.format(c.getTime());
        digitalclock.setText(formattedTime);
    }


    @Override
    public void setUpBatteryMonitor() {
        // Get clock
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df;
            if (preferences.getMyPreferenceBoolean(StageMode.this,"clock24hFormat",true)) {
                df = new SimpleDateFormat("HH:mm", StaticVariables.locale);
            } else {
                df = new SimpleDateFormat("h:mm", StaticVariables.locale);
            }
            String formattedTime = df.format(c.getTime());
            if (preferences.getMyPreferenceBoolean(StageMode.this,"clockOn",true)) {
                digitalclock.setVisibility(View.VISIBLE);
            } else {
                digitalclock.setVisibility(View.GONE);
            }
            digitalclock.setTextSize(preferences.getMyPreferenceFloat(StageMode.this,"clockTextSize",9.0f));
            digitalclock.setText(formattedTime);

            // Get battery
            int i = (int) (BatteryMonitor.getBatteryStatus(StageMode.this) * 100.0f);
            String charge = i + "%";
            if (preferences.getMyPreferenceBoolean(StageMode.this,"batteryTextOn",true)) {
                batterycharge.setVisibility(View.VISIBLE);
            } else {
                batterycharge.setVisibility(View.GONE);
            }
            batterycharge.setTextSize(preferences.getMyPreferenceFloat(StageMode.this, "batteryTextSize",9.0f));
            batterycharge.setText(charge);
            int abh = ab.getHeight();
            StaticVariables.ab_height = abh;
            if (preferences.getMyPreferenceBoolean(StageMode.this,"batteryDialOn",true)) {
                batteryimage.setVisibility(View.VISIBLE);
            } else {
                batteryimage.setVisibility(View.INVISIBLE);
            }
            if (ab != null && abh > 0) {
                BitmapDrawable bmp = BatteryMonitor.batteryImage(StageMode.this, preferences,i, abh);
                batteryimage.setImageDrawable(bmp);
            }

            // Ask the app to check again in 60s
            Handler batterycheck = new Handler();
            batterycheck.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setUpBatteryMonitor();
                }
            }, 60000);
        } catch (Exception e) {
            // Ooops
        }
    }

    private void checkStorage() {
        if (check_storage!=null) {
            check_storage.cancel(true);
        }
        check_storage = new CheckStorage();
        try {
            check_storage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent (Intent intent) {
        //super.onNewIntent(intent);
        dealWithIntent();
    }
    private void dealWithIntent() {
        if (FullscreenActivity.whattodo!=null) {
            try {
                switch (FullscreenActivity.whattodo) {
                    case "importfile_customreusable_scripture":
                        // Receiving scripture text
                        FullscreenActivity.whattodo = "customreusable_scripture";
                        openFragment();
                        break;
                    case "importfile_newsong_text":
                        // Receiving song (maybe) text
                        FullscreenActivity.whattodo = "createsong";
                        openFragment();
                        break;
                    case "importfile_processimportosb":
                        // Receiving an OpenSongApp backup file
                        FullscreenActivity.whattodo = "processimportosb";
                        openFragment();
                        break;
                    case "importfile_doimport":
                        // Receiving another file
                        FullscreenActivity.whattodo = "doimport";
                        openFragment();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDataReceived(Object data) {
        // Attempt to extract the song details
        if (data!=null) {
            try {
                SalutMessage newMessage = LoganSquare.parse((String)data,SalutMessage.class);
                Log.d("StageMode","Salut Message = "+newMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (data.toString().contains("_____") || data.toString().contains("<lyrics>") || data.toString().contains("___section___") || data.toString().contains("autoscroll_")) {

                // Look for a section being sent
                int mysection = processSong.getSalutReceivedSection(data.toString());
                if (mysection >= 0) {
                    // Choose a section
                    holdBeforeLoadingSection(mysection);

                    // Listen for autoscroll start being sent
                } else if (data.toString().contains("autoscroll_start")) {
                    // Trigger the autoscroll
                    StaticVariables.isautoscrolling = false;
                    holdBeforeAutoscrolling();

                    // Listen for autoscroll stop being sent
                } else if (data.toString().contains("autoscroll_stop")) {
                    // Trigger the autoscroll stop
                    StaticVariables.isautoscrolling = true;
                    holdBeforeAutoscrolling();

                    // Must be receiving a song or a song location
                } else {
                    // Load the song
                    String action = processSong.getSalutReceivedLocation(data.toString(), StageMode.this, preferences, storageAccess);
                    switch (action) {
                        case "Location":
                            holdBeforeLoading();
                            break;
                        case "HostFile":
                            holdBeforeLoadingXML();
                            break;
                    }
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckStorage extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (ActivityCompat.checkSelfPermission(StageMode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stage_actions, menu);

        // Setup the menu item for connecting to cast devices
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        View mr = menu.findItem(R.id.media_route_menu_item).getActionView();
        if (mr!=null) {
            mr.setFocusable(false);
            mr.setFocusableInTouchMode(false);
        }
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        if (mMediaRouteSelector != null) {
            mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        }

        // Force overflow icon to show, even if hardware key is present
        MenuHandlers.forceOverFlow(StageMode.this, ab, menu);

        // Set up battery monitor
        setUpBatteryMonitor();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        MenuHandlers.actOnClicks(StageMode.this, preferences, item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        try {
            if (mDrawerLayout.isDrawerOpen(songmenu)) {
                mDrawerLayout.closeDrawer(songmenu);
                return;
            }
            if (mDrawerLayout.isDrawerOpen(optionmenu)) {
                mDrawerLayout.closeDrawer(optionmenu);
                return;
            }

            String message = getResources().getString(R.string.exit);
            FullscreenActivity.whattodo = "exit";

            newFragment = PopUpAreYouSureFragment.newInstance(message);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(newFragment, message);

            if (newFragment != null && !StageMode.this.isFinishing()) {
                try {
                    ft.commitAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //newFragment = PopUpAreYouSureFragment.newInstance(message);
        //newFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onStart() {
        super.onStart();
        StaticVariables.activity = StageMode.this;
        FullscreenActivity.appRunning = true;
        if (mMediaRouter != null && mMediaRouteSelector != null) {
            try {
                mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                        MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Fix the page flags
        windowFlags();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            FullscreenActivity.appRunning = false;
            mMediaRouter.removeCallback(mMediaRouterCallback);
        } catch (Exception e) {
            Log.d("StageMode", "Problem removing mediaroutercallback");
        }
        if (br!=null) {
            try {
                StageMode.this.unregisterReceiver(br);
            } catch (Exception e) {
                Log.d("StageMode", "No need to close battery monitor");
            }
        }

        tryCancelAsyncTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticVariables.activity = StageMode.this;
        FullscreenActivity.appRunning = true;
        // Make the drawers match half the width of the screen
        resizeDrawers();
        // Fix the page flags
        setWindowFlags();
        setWindowFlagsAdvanced();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            FullscreenActivity.whichPad = 0;
            killPad();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Battery monitor
        if (br!=null) {
            try {
                StageMode.this.unregisterReceiver(br);
            } catch (Exception e) {
                Log.d("StageMode", "Battery monitor not registered anymore");
            }
        }
        tryCancelAsyncTasks();
        if (songscrollview !=null) {
            songscrollview.removeAllViews();
        }

        if (FullscreenActivity.network!=null && FullscreenActivity.network.isRunningAsHost) {
            try {
                FullscreenActivity.network.stopNetworkService(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (FullscreenActivity.network!=null) {
            try {
            FullscreenActivity.network.unregisterClient(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Second screen
        try {
            CastRemoteDisplayLocalService.stopService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (hdmi!=null) {
                hdmi.dismiss();
            }
        } catch (Exception e) {
            // Ooops
            e.printStackTrace();
        }
    }

    private void tryCancelAsyncTasks() {
        doCancelAsyncTask(loadsong_async);
        doCancelAsyncTask(preparesongview_async);
        doCancelAsyncTask(createperformanceview1col_async);
        doCancelAsyncTask(createperformanceview2col_async);
        doCancelAsyncTask(createperformanceview3col_async);
        doCancelAsyncTask(preparesongmenu_async);
        doCancelAsyncTask(prepareoptionmenu_async);
        doCancelAsyncTask(resizeperformance_async);
        doCancelAsyncTask(resizestage_async);
        doCancelAsyncTask(createstageview1col_async);
        doCancelAsyncTask(fadeout_media1);
        doCancelAsyncTask(fadeout_media2);
        doCancelAsyncTask(mtask_autoscroll_music);
        doCancelAsyncTask(check_storage);
        doCancelAsyncTask(sharesong_async);
        doCancelAsyncTask(shareset_async);
        doCancelAsyncTask(shareactivitylog_async);
        doCancelAsyncTask(load_customreusable);
        doCancelAsyncTask(open_drawers);
        doCancelAsyncTask(close_drawers);
        doCancelAsyncTask(resize_drawers);
        doCancelAsyncTask(do_moveinset);
        doCancelAsyncTask(add_slidetoset);
        doCancelAsyncTask(dualscreenwork_async);
        doCancelAsyncTask(prepare_pad);
        doCancelAsyncTask(play_pads);
        doCancelAsyncTask(do_download);
        doCancelAsyncTask(show_sticky);
        doCancelAsyncTask(show_highlight);
        doCancelAsyncTask(get_scrollheight);
    }
    private void doCancelAsyncTask(AsyncTask ast) {
        try {
            if (ast!=null) {
                ast.cancel(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Get the language
        FixLocale.fixLocale(StageMode.this,preferences);

        FullscreenActivity.orientationchanged = FullscreenActivity.mScreenOrientation != newConfig.orientation;

        if (FullscreenActivity.orientationchanged) {
            if (newFragment!=null && newFragment.getDialog()!=null) {
                PopUpSizeAndAlpha.decoratePopUp(StageMode.this,newFragment.getDialog(), preferences);
            }

            // Get the current orientation
            FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

            closeMyDrawers("both");
            resizeDrawers();
            loadSong();
        }
    }

    private void sendSongLocationToConnected() {
        String messageString = StaticVariables.whichSongFolder + "_____" +
                StaticVariables.songfilename + "_____" +
                FullscreenActivity.whichDirection;

        myMessage = new SalutMessage();
        myMessage.description = messageString;
        holdBeforeSending();
    }

    private void sendAutoscrollTriggerToConnected() {
        String messageString = "autoscroll_";

        if (StaticVariables.isautoscrolling) {
            messageString += "start";
        } else {
            messageString += "stop";
        }
        SalutMessage sm = new SalutMessage();
        sm.description = messageString;
        holdBeforeSendingAutoscroll(sm);
    }

    private void sendSongXMLToConnected() {
        String myXML;
        if (FullscreenActivity.isSong && FullscreenActivity.myXML!=null) {
            myXML = FullscreenActivity.myXML;
        } else {
            myXML = "";
        }
        mySongMessage = new SalutMessage();
        mySongMessage.description = myXML;
        holdBeforeSendingXML();
    }

    private void sendSongSectionToConnected() {
        int sectionval;
        if (StaticVariables.whichMode.equals("Stage") || StaticVariables.whichMode.equals("Presentation")) {
            sectionval = StaticVariables.currentSection;
            mySectionMessage = new SalutMessage();
            mySectionMessage.description = "___section___" + sectionval;
            holdBeforeSendingSection();
        }
    }

    @Override
    public void shareSong() {
        if (justSong(StageMode.this)) {
        // Export - Take a screenshot as a bitmap
            doCancelAsyncTask(sharesong_async);
            sharesong_async = new ShareSong();
            try {
                sharesong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void refreshAll() {
        // Show the toast if the message isn't blank
        if (!StaticVariables.myToastMessage.equals("")) {
            ShowToast.showToast(StageMode.this);
        }
        lyrichandler = new Handler();
        chordhandler = new Handler();
        stickyhandler = new Handler();
        presohandler = new Handler();
        presoinfohandler = new Handler();
        customhandler = new Handler();

        getDefaultColors();
        mypage.setBackgroundColor(lyricsBackgroundColor);
        songscrollview.setBackgroundColor(lyricsBackgroundColor);
        setTypeFace.setUpAppFonts(StageMode.this, preferences, lyrichandler, chordhandler, stickyhandler,
                presohandler, presoinfohandler, customhandler);

        prepareOptionMenu();
        prepareSongMenu();
        loadSong();
    }

    private void shareActivityLog() {
        doCancelAsyncTask(shareactivitylog_async);
        shareactivitylog_async = new ShareActivityLog();
        try {
            shareactivitylog_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkBackupState() {
        // Check for the number of times the app has run without the user backing up his songs
        // If this is 10 (or more) show the backup prompt window.
        int runssincebackup = preferences.getMyPreferenceInt(StageMode.this, "runssincebackup", 0) + 1;

        // Save the new value
        preferences.setMyPreferenceInt(StageMode.this, "runssincebackup", runssincebackup);
        if (runssincebackup >= 10) {
            FullscreenActivity.whattodo = "promptbackup";
            openFragment();
        }
    }

    private void indexOfSongInMenu() {
        int position = filenamesSongsInFolder.indexOf(StaticVariables.songfilename);
        if (position<=0) {
            FullscreenActivity.currentSongIndex = 0;
            FullscreenActivity.previousSongIndex = 0;
        } else {
            FullscreenActivity.currentSongIndex = position;
            FullscreenActivity.previousSongIndex = position-1;
        }
        if (position<filenamesSongsInFolder.size()-1) {
            FullscreenActivity.nextSongIndex = position+1;
        } else {
            FullscreenActivity.nextSongIndex = position;
        }
    }

    @Override
    public void loadSong() {
        try {
            // Only do this once - if we are the process of loading a song already, don't try to do it again!
            if (!FullscreenActivity.alreadyloading) {
                FullscreenActivity.alreadyloading = true;
                // It will get set back to false in the post execute of the async task

                // Check for set song
                StaticVariables.setView = setActions.isSongInSet(StageMode.this, preferences);

                // Sort the text size and colour of the info stuff
                updateExtraInfoColorsAndSizes("capo");
                updateExtraInfoColorsAndSizes("pad");
                updateExtraInfoColorsAndSizes("metronome");

                // Set the focus
                // Don't do this for a blacklisted filetype (application, video, audio)
                String where = "Songs";
                String folder = StaticVariables.whichSongFolder;

                // Watch out for custom items
                if (folder.startsWith("../")) {
                    where = "";
                    folder = folder.replace("../", "");
                }

                Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, where, folder,
                        StaticVariables.songfilename);

                if (!storageAccess.checkFileExtensionValid(uri) && !storageAccess.determineFileTypeByExtension()) {
                    StaticVariables.myToastMessage = getResources().getString(R.string.file_type_unknown);
                    ShowToast.showToast(StageMode.this);
                } else {
                    // Declare this as a new song for CCLI autologging
                    newsongloaded = true;

                    // Send WiFiP2P intent
                    if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
                        try {
                            sendSongLocationToConnected();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // If there is a sticky note showing, remove it
                    if (stickyPopUpWindow != null && stickyPopUpWindow.isShowing()) {
                        try {
                            stickyPopUpWindow.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Animate out the current song
                    if (FullscreenActivity.whichDirection.equals("L2R")) {
                        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                            glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
                        } else if (songscrollview != null) {
                            songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
                        }
                    } else {
                        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                            glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
                        } else if (songscrollview != null) {
                            songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
                        }
                    }
                    // If there were highlight notes showing, move them away
                    if (StaticVariables.whichMode.equals("Performance") && highlightNotes != null && highlightNotes.getVisibility() == View.VISIBLE) {
                        if (FullscreenActivity.whichDirection.equals("L2R")) {
                            highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
                        } else if (highlightNotes != null) {
                            highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
                        }
                    }

                    // Remove any capokey
                    FullscreenActivity.capokey = "";

                    // End any current autscrolling
                    stopAutoScroll();

                    if ((StaticVariables.pad1Playing || StaticVariables.pad2Playing)) {
                        if (!FullscreenActivity.isPDF && !FullscreenActivity.isImage ||
                                (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent==0)) {
                            StaticVariables.fadeWhichPad = 0; // Fade both pads if required
                            fadeoutPad();
                        }
                    }
                    updateExtraInfoColorsAndSizes("pad");
                    padcurrentTime_TextView.setText(getString(R.string.zerotime));
                    backingtrackProgress.setVisibility(View.GONE);

                    // After animate out, load the song
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                glideimage_HorizontalScrollView.setVisibility(View.GONE);
                                glideimage_ScrollView.setVisibility(View.GONE);
                                songscrollview.setVisibility(View.GONE);
                                highlightNotes.setVisibility(View.GONE);
                                FullscreenActivity.highlightOn = false;
                                glideimage_ScrollView.scrollTo(0, 0);
                                songscrollview.scrollTo(0, 0);

                                // Hide the image, cause we might be loading a proper song!
                                glideimage.setBackgroundColor(StaticVariables.transparent);
                                glideimage.setImageDrawable(null);

                            } catch (Exception e) {
                                Log.d("StageMode", "error updating the views");
                            }
                            // Load the song
                            doCancelAsyncTask(loadsong_async);
                            loadsong_async = new LoadSongAsync();
                            try {
                                loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 300);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shareSet() {
        doCancelAsyncTask(shareset_async);
        shareset_async = new ShareSet();
        try {
            shareset_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doExport() {
        // This is called after the user has specified what should be exported.
        switch (FullscreenActivity.whattodo) {
            case "customise_exportsong":
                shareSong();
                break;
            case "ccli_export":
                shareActivityLog();
                break;
            default:
                shareSet();
                break;
        }
    }

    @Override
    public void loadCustomReusable() {
        if (load_customreusable!=null) {
            load_customreusable.cancel(true);
        }

        load_customreusable = new LoadCustomReusable();
        try {
            load_customreusable.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dualScreenWork() {
        if (FullscreenActivity.isPresenting || FullscreenActivity.isHDMIConnected) {
            // If we are autologging CCLI information
            if (newsongloaded && preferences.getMyPreferenceBoolean(StageMode.this,"ccliAutomaticLogging",false)) {
                PopUpCCLIFragment.addUsageEntryToLog(StageMode.this, preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                        StaticVariables.mTitle, StaticVariables.mAuthor,
                        StaticVariables.mCopyright, StaticVariables.mCCLI, "5"); // Presented
                newsongloaded = false;
            }
            try {
                doCancelAsyncTask(dualscreenwork_async);
                dualscreenwork_async = new DualScreenWork();
                dualscreenwork_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadSongFromSet() {
        loadSong();
    }

    @Override
    public void shuffleSongsInSet() {
        setActions.indexSongInSet();
        newFragment = PopUpSetViewNew.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(newFragment,"dialog");

        if (newFragment != null && !StageMode.this.isFinishing()) {
            try {
                ft.commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //newFragment = PopUpSetViewNew.newInstance();
        //newFragment.show(getSupportFragmentManager(), "dialog");
    }

    private boolean shouldHighlightsBeShown() {
        // This is the check before animating in the highlight notes.
        String highlightfilename = processSong.getHighlighterName(StageMode.this);
        String where = "Highlighter";
        Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, where, "", highlightfilename);
        return (FullscreenActivity.highlightOn || preferences.getMyPreferenceBoolean(StageMode.this,"drawingAutoDisplay",true)) &&
                storageAccess.uriExists(StageMode.this, uri) && StaticVariables.whichMode.equals("Performance");
    }

    @Override
    public void updateExtraInfoColorsAndSizes(String what) {

        try {
            switch (what) {
                case "capo":
                    capoInfo.setBackgroundColor(defextrainfobgcolor);
                    capoinfo.setTextColor(defextrainfotextcolor);
                    capoinfonewkey.setTextColor(defextrainfotextcolor);
                    if (preferences.getMyPreferenceBoolean(StageMode.this, "capoLargeFontInfoBar", true)) {
                        capoinfo.setTextSize(StaticVariables.infoBarLargeTextSize);
                        capoinfonewkey.setTextSize(StaticVariables.infoBarLargeTextSize);
                    } else {
                        capoinfo.setTextSize(StaticVariables.infoBarSmallTextSize);
                        capoinfonewkey.setTextSize(StaticVariables.infoBarSmallTextSize);
                    }
                    break;
                case "autoscroll":
                    playbackProgress.setBackgroundColor(defextrainfobgcolor);
                    currentTime_TextView.setTextColor(defextrainfotextcolor);
                    timeSeparator_TextView.setTextColor(defextrainfotextcolor);
                    totalTime_TextView.setTextColor(defextrainfotextcolor);
                    learnAutoScroll_TextView.setTextColor(defextrainfotextcolor);
                    learnAutoScrollTime_TextView.setTextColor(defextrainfotextcolor);
                    if (preferences.getMyPreferenceBoolean(StageMode.this, "autoscrollLargeFontInfoBar", true)) {
                        currentTime_TextView.setTextSize(StaticVariables.infoBarLargeTextSize);
                        timeSeparator_TextView.setTextSize(StaticVariables.infoBarLargeTextSize);
                        totalTime_TextView.setTextSize(StaticVariables.infoBarLargeTextSize);
                        learnAutoScroll_TextView.setTextSize(StaticVariables.infoBarLargeTextSize);
                        learnAutoScrollTime_TextView.setTextSize(StaticVariables.infoBarLargeTextSize);
                    } else {
                        currentTime_TextView.setTextSize(StaticVariables.infoBarSmallTextSize);
                        timeSeparator_TextView.setTextSize(StaticVariables.infoBarSmallTextSize);
                        totalTime_TextView.setTextSize(StaticVariables.infoBarSmallTextSize);
                        learnAutoScroll_TextView.setTextSize(StaticVariables.infoBarSmallTextSize);
                        learnAutoScrollTime_TextView.setTextSize(StaticVariables.infoBarSmallTextSize);
                    }
                    break;
                case "pad":
                    backingtrackProgress.setBackgroundColor(defextrainfobgcolor);
                    padcurrentTime_TextView.setTextColor(defextrainfotextcolor);
                    padTimeSeparator_TextView.setTextColor(defextrainfotextcolor);
                    padtotalTime_TextView.setTextColor(defextrainfotextcolor);
                    if (preferences.getMyPreferenceBoolean(StageMode.this, "padLargeFontInfoBar", true)) {
                        padcurrentTime_TextView.setTextSize(StaticVariables.infoBarLargeTextSize);
                        padTimeSeparator_TextView.setTextSize(StaticVariables.infoBarLargeTextSize);
                        padtotalTime_TextView.setTextSize(StaticVariables.infoBarLargeTextSize);
                    } else {
                        padcurrentTime_TextView.setTextSize(StaticVariables.infoBarSmallTextSize);
                        padTimeSeparator_TextView.setTextSize(StaticVariables.infoBarSmallTextSize);
                        padtotalTime_TextView.setTextSize(StaticVariables.infoBarSmallTextSize);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closePopUps() {
        if (newFragment != null && newFragment.isVisible()) {
            try {
                newFragment.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void scrollButtons() {
        delaycheckscroll = new Handler();
        checkScrollPosition = new Runnable() {
            @Override
            public void run() {
                FullscreenActivity.newPosFloat = songscrollview.getScrollY();

                if (checkCanScrollDown()) {
                    showFAB(scrollDownButton,true);
                } else {
                    showFAB(scrollDownButton,false);
                }

                if (checkCanScrollUp()) {
                    showFAB(scrollUpButton,true);
                } else {
                    showFAB(scrollUpButton,false);
                }
            }
        };
    }

    //@Override
    private void setupPageButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //coordinator_layout = findViewById(R.id.coordinator_layout);

                setButton = findViewById(R.id.setButton);
                padButton = findViewById(R.id.padButton);
                autoscrollButton = findViewById(R.id.autoscrollButton);
                metronomeButton = findViewById(R.id.metronomeButton);
                extraButton = findViewById(R.id.extraButton);
                chordButton = findViewById(R.id.chordButton);
                stickyButton = findViewById(R.id.stickyButton);
                notationButton = findViewById(R.id.notationButton);
                highlightButton = findViewById(R.id.highlightButton);
                pageselectButton = findViewById(R.id.pageselectButton);
                linkButton = findViewById(R.id.linkButton);
                chordButton_ungrouped = findViewById(R.id.chordButton_ungrouped);
                stickyButton_ungrouped = findViewById(R.id.stickyButton_ungrouped);
                notationButton_ungrouped = findViewById(R.id.notationButton_ungrouped);
                highlightButton_ungrouped = findViewById(R.id.highlightButton_ungrouped);
                pageselectButton_ungrouped = findViewById(R.id.pageselectButton_ungrouped);
                linkButton_ungrouped = findViewById(R.id.linkButton_ungrouped);
                customButton = findViewById(R.id.customButton);
                custom1Button = findViewById(R.id.custom1Button);
                custom2Button = findViewById(R.id.custom2Button);
                custom3Button = findViewById(R.id.custom3Button);
                custom4Button = findViewById(R.id.custom4Button);
                custom1Button_ungrouped = findViewById(R.id.custom1Button_ungrouped);
                custom2Button_ungrouped = findViewById(R.id.custom2Button_ungrouped);
                custom3Button_ungrouped = findViewById(R.id.custom3Button_ungrouped);
                custom4Button_ungrouped = findViewById(R.id.custom4Button_ungrouped);
                extrabuttons = findViewById(R.id.extrabuttons);
                extrabuttons.setVisibility(View.GONE);
                extrabuttons2 = findViewById(R.id.extrabuttons2);
                extrabuttons2.setVisibility(View.GONE);
                scrollDownButton = findViewById(R.id.scrollDownButton);
                scrollUpButton = findViewById(R.id.scrollUpButton);
                setBackButton = findViewById(R.id.setBackButton);
                setForwardButton = findViewById(R.id.setForwardButton);
                setUpPageButtonsColors();
                setupQuickLaunchButtons();
            }
        });





        // Decide if we are grouping / tidying page buttons
        groupPageButtons();

        // Set the sizes and the alphas
        pageButtonAlpha("");

        // Set the listeners
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(setButton,StageMode.this);
                FullscreenActivity.whattodo = "editset";
                openFragment();
            }
        });
        padButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(padButton,StageMode.this);
                FullscreenActivity.whattodo = "page_pad";
                openFragment();
            }
        });
        padButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(padButton,StageMode.this);
                gesture6();
                return true;
            }
        });
        autoscrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(autoscrollButton,StageMode.this);
                FullscreenActivity.whattodo = "page_autoscroll";
                openFragment();
            }
        });
        autoscrollButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(autoscrollButton,StageMode.this);
                gesture5();
                return true;
            }
        });
        metronomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(metronomeButton,StageMode.this);
                FullscreenActivity.whattodo = "page_metronome";
                openFragment();
            }
        });
        metronomeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(metronomeButton,StageMode.this);
                gesture7();
                return true;
            }
        });
        highlightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(highlightButton,StageMode.this);
                FullscreenActivity.highlightOn = !FullscreenActivity.highlightOn;
                FullscreenActivity.whattodo = "page_highlight";
                displayHighlight(false);
            }
        });
        highlightButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(highlightButton,StageMode.this);
                // Vibrate to let the user know something happened
                DoVibrate.vibrate(StageMode.this, 50);
                takeScreenShot();
                if (FullscreenActivity.bmScreen!=null) {
                    FullscreenActivity.whattodo = "drawnotes";
                    openFragment();
                } else {
                    Log.d("StageMode", "screenshot is null");
                }
                return true;
            }
        });
        highlightButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(highlightButton_ungrouped,StageMode.this);
                FullscreenActivity.whattodo = "page_highlight";
                FullscreenActivity.highlightOn = !FullscreenActivity.highlightOn;
                displayHighlight(false);
            }
        });
        highlightButton_ungrouped.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(highlightButton_ungrouped,StageMode.this);
                // Vibrate to let the user know something happened
                DoVibrate.vibrate(StageMode.this, 50);
                takeScreenShot();
                if (FullscreenActivity.bmScreen!=null) {
                    FullscreenActivity.whattodo = "drawnotes";
                    openFragment();
                } else {
                    Log.d("StageMode", "screenshot is null");
                }
                return true;
            }
        });
        extraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(extraButton,StageMode.this);
                if (extrabuttons!=null && extrabuttons.getVisibility() == View.GONE) {
                    pageButtonAlpha("extra");
                } else {
                    pageButtonAlpha("");
                }
            }
        });
        chordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(chordButton,StageMode.this);
                FullscreenActivity.whattodo = "page_chords";
                openFragment();
            }
        });
        chordButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(chordButton_ungrouped,StageMode.this);
                FullscreenActivity.whattodo = "page_chords";
                openFragment();
            }
        });
        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(linkButton,StageMode.this);
                FullscreenActivity.whattodo = "page_links";
                openFragment();
            }
        });
        linkButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(linkButton_ungrouped,StageMode.this);
                FullscreenActivity.whattodo = "page_links";
                openFragment();
            }
        });
        stickyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(stickyButton,StageMode.this);
                FullscreenActivity.whattodo = "page_sticky";
                displaySticky();
            }
        });
        stickyButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFAB(stickyButton,StageMode.this);
                FullscreenActivity.whattodo = "page_sticky";
                openFragment();
                return true;
            }
        });
        stickyButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(stickyButton_ungrouped,StageMode.this);
                FullscreenActivity.whattodo = "page_sticky";
                displaySticky();
            }
        });
        stickyButton_ungrouped.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFAB(stickyButton,StageMode.this);
                FullscreenActivity.whattodo = "page_sticky";
                openFragment();
                return true;
            }
        });
        notationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(notationButton,StageMode.this);
                if (StaticVariables.mNotation.equals("")) {
                    FullscreenActivity.whattodo = "abcnotation_edit";
                } else {
                    FullscreenActivity.whattodo = "abcnotation";
                }
                openFragment();
            }
        });
        notationButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFAB(notationButton,StageMode.this);
                FullscreenActivity.whattodo = "abcnotation_edit";
                openFragment();
                return true;
            }
        });
        notationButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(notationButton_ungrouped,StageMode.this);
                if (StaticVariables.mNotation.equals("")) {
                    FullscreenActivity.whattodo = "abcnotation_edit";
                } else {
                    FullscreenActivity.whattodo = "abcnotation";
                }
                openFragment();
            }
        });
        notationButton_ungrouped.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFAB(notationButton_ungrouped,StageMode.this);
                FullscreenActivity.whattodo = "abcnotation_edit";
                openFragment();
                return true;
            }
        });
        pageselectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(pageselectButton,StageMode.this);
                if (FullscreenActivity.isPDF) {
                    FullscreenActivity.whattodo = "page_pageselect";
                    openFragment();
                } else {
                    StaticVariables.myToastMessage = getResources().getString(R.string.not_allowed);
                    ShowToast.showToast(StageMode.this);
                }
            }
        });
        pageselectButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(pageselectButton_ungrouped,StageMode.this);
                if (FullscreenActivity.isPDF) {
                    FullscreenActivity.whattodo = "page_pageselect";
                    openFragment();
                } else {
                    StaticVariables.myToastMessage = getResources().getString(R.string.not_allowed);
                    ShowToast.showToast(StageMode.this);
                }
            }
        });
        if (preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonGroupMain",false)) {
            customButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(customButton,StageMode.this);
                    FullscreenActivity.whattodo = "groupedpagebuttons";
                    openFragment();
                }
            });
        } else {
            customButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(customButton,StageMode.this);
                    if (extrabuttons2!=null && extrabuttons2.getVisibility() == View.GONE) {
                        pageButtonAlpha("custom");
                    } else {
                        pageButtonAlpha("");
                    }
                    }
            });
        }
        scrollUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(scrollUpButton,StageMode.this);
                doScrollUp();
            }
        });
        scrollDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(scrollDownButton,StageMode.this);
                doScrollDown();
            }
        });
        setForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(setForwardButton,StageMode.this);
                StaticVariables.setMoveDirection = "forward";
                FullscreenActivity.whichDirection = "R2L";
                goToNextItem();
            }
        });
        setBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(setBackButton,StageMode.this);
                StaticVariables.setMoveDirection = "back";
                FullscreenActivity.whichDirection = "L2R";
                goToPreviousItem();
            }
        });
    }

    @Override
    public void setUpPageButtonsColors() {
        // Set the colors
        setButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        padButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        autoscrollButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        metronomeButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        extraButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        chordButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        linkButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        pageselectButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        customButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom1Button.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom2Button.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom3Button.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom4Button.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        scrollDownButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        scrollUpButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        setBackButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        setForwardButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
    }

    private void onScrollAction() {
        // Reshow the scroll arrows if needed
        scrollButtons();
        delaycheckscroll.post(checkScrollPosition);
    }

    private void doScrollUp() {
        // Scroll the screen up

        if (StaticVariables.whichMode.equals("Stage")) {
            try {
                StaticVariables.currentSection -= 1;
                selectSection(StaticVariables.currentSection);
            } catch (Exception e) {
                StaticVariables.currentSection += 1;
                e.printStackTrace();
            }
            scrollButtons();
        } else {
            FullscreenActivity.wasscrolling = true;
            FullscreenActivity.scrollbutton = true;

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int barheight = 0;
            if (ab != null) {
                if (ab.isShowing()) {
                    barheight = ab.getHeight();
                }
            }

            ObjectAnimator animator;

            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY() -
                        (int) (preferences.getMyPreferenceFloat(StageMode.this,"scrollDistance", 0.7f) * (
                                metrics.heightPixels - barheight));
                animator = ObjectAnimator.ofInt(glideimage_ScrollView, "scrollY", glideimage_ScrollView.getScrollY(), (int) FullscreenActivity.newPosFloat);
            } else {
                FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() -
                        (int) (preferences.getMyPreferenceFloat(StageMode.this,"scrollDistance", 0.7f) *
                                (metrics.heightPixels - barheight));
                animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
            }

            Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
            animator.setInterpolator(customInterpolator);
            animator.setDuration(preferences.getMyPreferenceInt(StageMode.this,"scrollSpeed",1500));
            animator.start();

            // Set a runnable to check the scroll position after 1 second
            delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
            hideActionBar();
        }
    }

    private void doScrollDown() {

        if (StaticVariables.whichMode.equals("Stage")) {
            try {
                StaticVariables.currentSection += 1;
                selectSection(StaticVariables.currentSection);

            } catch (Exception e) {
                StaticVariables.currentSection -= 1;
            }
            scrollButtons();
        } else {
            // Scroll the screen down
            FullscreenActivity.wasscrolling = true;
            FullscreenActivity.scrollbutton = true;

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);


            ObjectAnimator animator;

            int barheight = 0;
            if (ab != null) {
                if (ab.isShowing()) {
                    barheight = ab.getHeight();
                }
            }

            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY() +
                        (int) (preferences.getMyPreferenceFloat(StageMode.this,"scrollDistance", 0.7f) *
                                (metrics.heightPixels - barheight));
                animator = ObjectAnimator.ofInt(glideimage_ScrollView, "scrollY", glideimage_ScrollView.getScrollY(), (int) FullscreenActivity.newPosFloat);
            } else {
                FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() +
                        (int) (preferences.getMyPreferenceFloat(StageMode.this,"scrollDistance", 0.7f) *
                                (metrics.heightPixels - barheight));
                animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
            }

            Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
            animator.setInterpolator(customInterpolator);
            animator.setDuration(preferences.getMyPreferenceInt(StageMode.this,"scrollSpeed",1500));
            animator.start();

            // Set a runnable to check the scroll position after 1 second
            delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
            hideActionBar();
        }
    }

    private void showFAB(final View fab, boolean show) {
        // Could use the default show() and hide() but the default animation keeps getting fired
        // Treating them as a generic view fixes that!
        if (show) {
            fab.setAlpha(preferences.getMyPreferenceFloat(StageMode.this,"pageButtonAlpha",0.5f));
            fab.setVisibility(View.VISIBLE);
        } else {
            //fab.hide();
            fab.setVisibility(View.GONE);

        }
    }
    private void hideAllFABs() {
        showFAB(setButton, false);
        showFAB(padButton, false);
        showFAB(autoscrollButton, false);
        showFAB(metronomeButton, false);
        showFAB(extraButton, false);
        extrabuttons.setVisibility(View.GONE);
        extrabuttons2.setVisibility(View.GONE);
        showFAB(chordButton_ungrouped, false);
        showFAB(stickyButton_ungrouped, false);
        showFAB(notationButton_ungrouped, false);
        showFAB(highlightButton_ungrouped, false);
        showFAB(pageselectButton_ungrouped, false);
        showFAB(linkButton_ungrouped, false);
        showFAB(customButton,false);
        showFAB(custom1Button_ungrouped, false);
        showFAB(custom2Button_ungrouped, false);
        showFAB(custom3Button_ungrouped, false);
        showFAB(custom4Button_ungrouped, false);
        showFAB(setBackButton, false);
        showFAB(setForwardButton, false);
        showFAB(scrollDownButton, false);
        showFAB(scrollUpButton, false);
    }

    @Override
    public void groupPageButtons() {
        // Hide everything to begin with
        hideAllFABs();

        // Get the preference of what should show
        boolean showsetbutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowSet", true);
        boolean showpadbutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowPad", true);
        boolean showautoscrollbutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowAutoscroll", true);
        boolean showmetronomebutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowMetronome", false);
        boolean showchordsbutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowChords", false);
        boolean showlinkbutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowLinks", false);
        boolean showstickybutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowSticky", false);
        boolean shownotationbutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowNotation", false);
        boolean showhighlighterbutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowHighlighter", false);
        boolean showpageselectbutton = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowPageSelect", false);
        boolean showcustom1button = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowCustom1", true);
        boolean showcustom2button = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowCustom2", true);
        boolean showcustom3button = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowCustom3", true);
        boolean showcustom4button = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowCustom4", true);
        boolean groupmain = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonGroupMain",false);
        boolean groupextra = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonGroupExtra",false);
        boolean groupcustom = preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonGroupCustom",false);

        // By default, hide them all and we'll only show the ones we want
        if (groupmain) {
            // All buttons are already hidden, so just show the custom group all button
            showFAB(customButton,true);
        } else {
            // The following buttons don't belong in a group
            showFAB(setButton,showsetbutton);
            showFAB(padButton,showpadbutton);
            showFAB(autoscrollButton,showautoscrollbutton);
            showFAB(metronomeButton,showmetronomebutton);

            // Decide if we've grouped the extra buttons, the show the appropriate extra buttons
            if (groupextra) {
                // Only show the extrabutton if some of the contents are visible
                if (showchordsbutton || showlinkbutton || showstickybutton || shownotationbutton ||
                    showhighlighterbutton || showpageselectbutton) {
                    showFAB(extraButton, true);
                    showFAB(chordButton, showchordsbutton);
                    showFAB(linkButton, showlinkbutton);
                    showFAB(stickyButton, showstickybutton);
                    showFAB(notationButton, shownotationbutton);
                    showFAB(highlightButton, showhighlighterbutton);
                    showFAB(pageselectButton, showpageselectbutton);
                }
            } else {
                // Not grouping the extra buttons, so show what we need
                showFAB(chordButton_ungrouped,showchordsbutton);
                showFAB(linkButton_ungrouped,showlinkbutton);
                showFAB(stickyButton_ungrouped,showstickybutton);
                showFAB(notationButton_ungrouped,shownotationbutton);
                showFAB(highlightButton_ungrouped,showhighlighterbutton);
                showFAB(pageselectButton_ungrouped,showpageselectbutton);
            }

            // Decide if we've grouped the custom buttons, the show the appropriate extra buttons
            if (groupcustom) {
                // Only show the custombutton if some of the contents are visible (otherwise, there's no point)
                if (showcustom1button || showcustom2button || showcustom3button || showcustom4button) {
                    showFAB(customButton, true);
                    showFAB(custom1Button, showcustom1button);
                    showFAB(custom2Button, showcustom2button);
                    showFAB(custom3Button, showcustom3button);
                    showFAB(custom4Button, showcustom4button);
                }
            } else {
                // Not grouping the custom buttons, so show what we need
                showFAB(custom1Button_ungrouped,showcustom1button);
                showFAB(custom2Button_ungrouped,showcustom2button);
                showFAB(custom3Button_ungrouped,showcustom3button);
                showFAB(custom4Button_ungrouped,showcustom4button);
            }
        }
    }

    @Override
    public void pageButtonAlpha(final String s) {
        try {
            // This is called at run, if the user changes a preference and if action is running

            // Get the default alpha value
            float val = preferences.getMyPreferenceFloat(StageMode.this, "pageButtonAlpha", 0.5f);

            // Set the on vaule (if a popup is running)
            float onval = val + 0.3f;
            if (onval > 1.0f) {
                onval = 1.0f;
            }

            // Prepare the default values
            float setAlpha = val;
            float padAlpha = val;
            float autoscrollAlpha = val;
            float metronomeAlpha = val;
            float extraAlpha = val;
            float chordsAlpha = val;
            float stickyAlpha = val;
            float highlightAlpha = val;
            float pageselectAlpha = val;
            float linkAlpha = val;
            float customAlpha = val;
            float custom1Alpha = val;
            float custom2Alpha = val;
            float custom3Alpha = val;
            float custom4Alpha = val;

            // Check the extrabuttons and custombuttons views are hidden to start with
            extrabuttons.setVisibility(View.GONE);
            extrabuttons2.setVisibility(View.GONE);

            // If we have sent a button 'on' state, set the alpha to match
            if (s != null && !s.isEmpty()) {
                switch (s) {
                    case "set":
                        setAlpha = onval;
                        break;
                    case "pad":
                        padAlpha = onval;
                        break;
                    case "autoscroll":
                        autoscrollAlpha = onval;
                        break;
                    case "metronome":
                        metronomeAlpha = onval;
                        break;
                    case "extra":
                        extraAlpha = onval;
                        extrabuttons.setVisibility(View.VISIBLE);
                        break;
                    case "chord":
                        extraAlpha = onval;
                        extrabuttons.setVisibility(View.VISIBLE);
                        chordsAlpha = onval;
                        break;
                    case "sticky":
                        extraAlpha = onval;
                        extrabuttons.setVisibility(View.VISIBLE);
                        stickyAlpha = onval;
                        break;
                    case "highlight":
                        extraAlpha = onval;
                        extrabuttons.setVisibility(View.VISIBLE);
                        highlightAlpha = onval;
                        break;
                    case "pageselect":
                        extraAlpha = onval;
                        extrabuttons.setVisibility(View.VISIBLE);
                        pageselectAlpha = onval;
                        break;
                    case "link":
                        extraAlpha = onval;
                        extrabuttons.setVisibility(View.VISIBLE);
                        linkAlpha = onval;
                        break;
                    case "custom":
                        customAlpha = onval;
                        extrabuttons2.setVisibility(View.VISIBLE);
                        break;
                    case "custom1":
                        customAlpha = onval;
                        custom1Alpha = onval;
                        extrabuttons2.setVisibility(View.VISIBLE);
                        break;
                    case "custom2":
                        customAlpha = onval;
                        custom2Alpha = onval;
                        extrabuttons2.setVisibility(View.VISIBLE);
                        break;
                    case "custom3":
                        customAlpha = onval;
                        custom3Alpha = onval;
                        extrabuttons2.setVisibility(View.VISIBLE);
                        break;
                    case "custom4":
                        customAlpha = onval;
                        custom4Alpha = onval;
                        extrabuttons2.setVisibility(View.VISIBLE);
                        break;
                }
            }

            // Set the button alphas as required
            setButton.setAlpha(setAlpha);
            padButton.setAlpha(padAlpha);
            autoscrollButton.setAlpha(autoscrollAlpha);
            metronomeButton.setAlpha(metronomeAlpha);
            extraButton.setAlpha(extraAlpha);
            chordButton.setAlpha(chordsAlpha);
            stickyButton.setAlpha(stickyAlpha);
            notationButton.setAlpha(stickyAlpha);
            highlightButton.setAlpha(highlightAlpha);
            pageselectButton.setAlpha(pageselectAlpha);
            linkButton.setAlpha(linkAlpha);
            chordButton_ungrouped.setAlpha(chordsAlpha);
            stickyButton_ungrouped.setAlpha(stickyAlpha);
            notationButton_ungrouped.setAlpha(stickyAlpha);
            highlightButton_ungrouped.setAlpha(highlightAlpha);
            pageselectButton_ungrouped.setAlpha(pageselectAlpha);
            linkButton_ungrouped.setAlpha(linkAlpha);
            customButton.setAlpha(customAlpha);
            custom1Button.setAlpha(custom1Alpha);
            custom2Button.setAlpha(custom2Alpha);
            custom3Button.setAlpha(custom3Alpha);
            custom4Button.setAlpha(custom4Alpha);
            custom1Button_ungrouped.setAlpha(custom1Alpha);
            custom2Button_ungrouped.setAlpha(custom2Alpha);
            custom3Button_ungrouped.setAlpha(custom3Alpha);
            custom4Button_ungrouped.setAlpha(custom4Alpha);
            scrollDownButton.setAlpha(val);
            scrollUpButton.setAlpha(val);
            setBackButton.setAlpha(val);
            setForwardButton.setAlpha(val);

            // Now set the sizes of the FAB buttons
            int fabSize = preferences.getMyPreferenceInt(StageMode.this, "pageButtonSize", FloatingActionButton.SIZE_NORMAL);
            setButton.setSize(fabSize);
            padButton.setSize(fabSize);
            autoscrollButton.setSize(fabSize);
            metronomeButton.setSize(fabSize);
            extraButton.setSize(fabSize);
            chordButton.setSize(fabSize);
            stickyButton.setSize(fabSize);
            notationButton.setSize(fabSize);
            highlightButton.setSize(fabSize);
            pageselectButton.setSize(fabSize);
            linkButton.setSize(fabSize);
            chordButton_ungrouped.setSize(fabSize);
            stickyButton_ungrouped.setSize(fabSize);
            notationButton_ungrouped.setSize(fabSize);
            highlightButton_ungrouped.setSize(fabSize);
            pageselectButton_ungrouped.setSize(fabSize);
            linkButton_ungrouped.setSize(fabSize);
            customButton.setSize(fabSize);
            custom1Button.setSize(fabSize);
            custom2Button.setSize(fabSize);
            custom3Button.setSize(fabSize);
            custom4Button.setSize(fabSize);
            custom1Button_ungrouped.setSize(fabSize);
            custom2Button_ungrouped.setSize(fabSize);
            custom3Button_ungrouped.setSize(fabSize);
            custom4Button_ungrouped.setSize(fabSize);
            scrollDownButton.setSize(fabSize);
            scrollUpButton.setSize(fabSize);
            setBackButton.setSize(fabSize);
            setForwardButton.setSize(fabSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupQuickLaunchButtons() {
        // Based on the user's choices for the custom quicklaunch buttons,
        // set the appropriate icons and onClick listeners
        final String b1ac = preferences.getMyPreferenceString(StageMode.this,"pageButtonCustom1Action","transpose");
        final String b2ac = preferences.getMyPreferenceString(StageMode.this,"pageButtonCustom2Action","");
        final String b3ac = preferences.getMyPreferenceString(StageMode.this,"pageButtonCustom3Action","");
        final String b4ac = preferences.getMyPreferenceString(StageMode.this,"pageButtonCustom4Action","");
        custom1Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, b1ac));
        custom2Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, b2ac));
        custom3Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, b3ac));
        custom4Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, b4ac));
        custom1Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, b1ac));
        custom2Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, b2ac));
        custom3Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, b3ac));
        custom4Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, b4ac));
        custom1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom1Button,StageMode.this);
                customButtonAction(b1ac);
            }
        });
        custom2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom2Button,StageMode.this);
                customButtonAction(b2ac);
            }
        });
        custom3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom3Button,StageMode.this);
                customButtonAction(b3ac);
            }
        });
        custom4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom4Button,StageMode.this);
                customButtonAction(b4ac);
            }
        });
        custom1Button_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom1Button_ungrouped,StageMode.this);
                customButtonAction(b1ac);
            }
        });
        custom2Button_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom2Button_ungrouped,StageMode.this);
                customButtonAction(b2ac);
            }
        });
        custom3Button_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom3Button_ungrouped,StageMode.this);
                customButtonAction(b3ac);
            }
        });
        custom4Button_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom4Button_ungrouped,StageMode.this);
                customButtonAction(b4ac);
            }
        });
    }

    private void customButtonAction(String s) {
        boolean val;
        switch (s) {
            case "":
            default:
                FullscreenActivity.whattodo = "quicklaunch";
                openFragment();
                break;

            case "editsong":
            case "editsongpdf":
            case "changetheme":
            case "autoscale":
            case "changefonts":
            case "profiles":
            case "gestures":
            case "footpedal":
            case "transpose":
            case "fullsearch":
            case "randomsong":
            case "abcnotation_edit":
            case "abcnotation":
            case "abcnotation_editsong":
            case "showmidicommands":
                if (s.equals("editsong") && !justSong(StageMode.this) && !FullscreenActivity.isPDF) {
                    ShowToast.showToast(StageMode.this);
                } else {
                    if (FullscreenActivity.isPDF && s.equals("editsong")) {
                        s = "extractPDF";
                    }
                    FullscreenActivity.whattodo = s;
                    openFragment();
                }
                break;

            case "showchords":
                val = preferences.getMyPreferenceBoolean(StageMode.this,"displayChords",true);
                preferences.setMyPreferenceBoolean(StageMode.this,"displayChords",!val);
                loadSong();
                break;

            case "showcapo":
                val = preferences.getMyPreferenceBoolean(StageMode.this,"displayCapoChords",true);
                preferences.setMyPreferenceBoolean(StageMode.this,"displayCapoChords",!val);
                loadSong();
                break;

            case "showlyrics":
                val = preferences.getMyPreferenceBoolean(StageMode.this,"displayLyrics",true);
                preferences.setMyPreferenceBoolean(StageMode.this,"displayLyrics",!val);
                loadSong();
                break;

            case "inc_autoscroll_speed":
                increaseAutoScrollSpeed();
                break;

            case "dec_autoscroll_speed":
                decreaseAutoScrollSpeed();
                break;

            case "toggle_autoscroll_pause":
                StaticVariables.autoscrollispaused = !StaticVariables.autoscrollispaused;
                break;

            case "exit":
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();
                    } else {
                        this.finishAffinity();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void increaseAutoScrollSpeed() {
        if (StaticVariables.autoscrollispaused) {
            StaticVariables.autoscrollispaused = false;
            StaticVariables.autoscroll_modifier = 0;
        } else {
            StaticVariables.autoscroll_modifier = StaticVariables.autoscroll_modifier + 4;
        }
    }

    private void decreaseAutoScrollSpeed() {
        if (FullscreenActivity.autoscroll_pixels + StaticVariables.autoscroll_modifier >= 4) {
            StaticVariables.autoscroll_modifier = StaticVariables.autoscroll_modifier - 4;
        }
        if (FullscreenActivity.autoscroll_pixels + StaticVariables.autoscroll_modifier <= 4) {
            StaticVariables.autoscrollispaused = true;
        }
    }

    @Override
    public void onSongImportDone() {
        rebuildSearchIndex();
    }

    @Override
    public void backupInstall() {
        // Songs have been imported, so update the song menu and rebuild the search index
        rebuildSearchIndex();
    }

    @Override
    public void doEdit() {
        if (FullscreenActivity.isPDF) {
            FullscreenActivity.whattodo = "extractPDF";
            openFragment();
        } else if (FullscreenActivity.isSong){
            FullscreenActivity.whattodo = "editsong";
            openFragment();
        }
    }

    @Override
    public void updatePresentationOrder() {
        // User has changed the presentation order
        doEdit();
    }

    @Override
    public void openMyDrawers(String which) {
        doCancelAsyncTask(open_drawers);
        open_drawers = new OpenMyDrawers(which);
        try {
            open_drawers.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class OpenMyDrawers extends AsyncTask<Object, Void, String> {

        final String which;

        OpenMyDrawers(String w) {
            which = w;
        }

        @Override
        protected String doInBackground(Object... obj) {
            return null;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    DrawerTweaks.openMyDrawers(mDrawerLayout, songmenu, optionmenu, which);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeMyDrawers(String which) {
        doCancelAsyncTask(close_drawers);
        close_drawers = new CloseMyDrawers(which);
        try {
            close_drawers.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class CloseMyDrawers extends AsyncTask<Object, Void, String> {

        final String which;

        CloseMyDrawers(String w) {
            which = w;
        }

        @Override
        protected String doInBackground(Object... obj) {
            return null;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    DrawerTweaks.closeMyDrawers(mDrawerLayout, songmenu, optionmenu, which);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void findSongInFolders() {
        //scroll to the song in the song menu
        try {
            indexOfSongInMenu();
            song_list_view.setSelection(FullscreenActivity.currentSongIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resizeDrawers() {
        doCancelAsyncTask(resize_drawers);
        resize_drawers = new ResizeDrawers();
        try {
            resize_drawers.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ResizeDrawers extends AsyncTask<Object, Void, String> {
        int width;

        @Override
        protected String doInBackground(Object... o) {
            try {
                width = preferences.getMyPreferenceInt(StageMode.this,"menuSize",250);
                float density = getResources().getDisplayMetrics().density;
                width = Math.round((float) width * density);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled && songmenu!=null && optionmenu!=null) {
                    songmenu.setLayoutParams(DrawerTweaks.resizeMenu(songmenu, width));
                    optionmenu.setLayoutParams(DrawerTweaks.resizeMenu(optionmenu, width));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void doMoveSection() {
        switch (StaticVariables.setMoveDirection) {
            case "forward":
                StaticVariables.currentSection += 1;
                selectSection(StaticVariables.currentSection);
                break;
            case "back":
                StaticVariables.currentSection -= 1;
                selectSection(StaticVariables.currentSection);
                break;
        }
    }

    public void doMoveInSet() {
        doCancelAsyncTask(do_moveinset);
        do_moveinset = new DoMoveInSet();
        try {
            do_moveinset.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displayHighlight(boolean fromautoshow) {

        if (!StaticVariables.whichMode.equals("Performance")) {
            FullscreenActivity.highlightOn = false;
            highlightNotes.setVisibility(View.GONE);
            if (!fromautoshow) {
                // Don't show the warning just because the app tries to autoshow it
                StaticVariables.myToastMessage = getString(R.string.switchtoperformmode);
                ShowToast.showToast(StageMode.this);
            }
        } else {
            // If we are trying to show notes, but they are already open, close them
            // This is only if a manual click on the hightlight button happened
            // Are the notes visible?
            if (StaticVariables.thisSongScale==null) {
                StaticVariables.thisSongScale = preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W");
            }

            if (highlightNotes.getVisibility() == View.VISIBLE && !fromautoshow) {
                // Hide it
                FullscreenActivity.highlightOn = false;
                highlightNotes.setVisibility(View.GONE);
            } else if (StaticVariables.thisSongScale.equals("Y")) {
                String hname = processSong.getHighlighterName(StageMode.this);
                Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Highlighter", "", hname);
                if (storageAccess.uriExists(StageMode.this, uri)) {
                    // Load the image in if it exists and then show it
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    try {
                        InputStream inputStream = storageAccess.getInputStream(StageMode.this, uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        if (bitmap != null) {
                            Bitmap canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                            bitmap.recycle();
                            RelativeLayout.LayoutParams rlp;
                            final int firstguesswidth;
                            final int firstguessheight;
                            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                                firstguesswidth = glideimage_ScrollView.getMeasuredWidth();
                                firstguessheight = glideimage_ScrollView.getMeasuredHeight();
                            } else {
                                firstguesswidth = songscrollview.getMeasuredWidth();
                                firstguessheight = songscrollview.getMeasuredHeight();
                            }
                            rlp = new RelativeLayout.LayoutParams(firstguesswidth, firstguessheight);
                            if (preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
                                rlp.addRule(RelativeLayout.BELOW, 0);
                            } else {
                                rlp.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
                            }

                            // Set a runnable to check the height/width after a couple of seconds to redraw the image position
                            // Only if it has changed though
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1000);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                int secondguessheight;
                                                int secondguesswidth;
                                                if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                                                    secondguesswidth = glideimage_ScrollView.getMeasuredWidth();
                                                    secondguessheight = glideimage_ScrollView.getMeasuredHeight();
                                                } else {
                                                    secondguesswidth = songscrollview.getMeasuredWidth();
                                                    secondguessheight = songscrollview.getMeasuredHeight();
                                                }
                                                if (secondguessheight != firstguessheight || secondguesswidth != firstguesswidth) {
                                                    // Set the parameters again
                                                    RelativeLayout.LayoutParams rlp2 =
                                                            new RelativeLayout.LayoutParams(secondguesswidth, secondguessheight);
                                                    if (preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
                                                        rlp2.addRule(RelativeLayout.BELOW, 0);
                                                    } else {
                                                        rlp2.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
                                                    }
                                                    highlightNotes.setLayoutParams(rlp2);
                                                    highlightNotes.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                                }
                                            }
                                        });
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).start();
                            highlightNotes.setImageBitmap(canvasBitmap);
                            highlightNotes.setLayoutParams(rlp);
                            highlightNotes.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            if (!fromautoshow) {
                                // If user manually wanted to show, otherwise song load animates it in
                                highlightNotes.setVisibility(View.VISIBLE);
                            } else {
                                highlightNotes.setVisibility(View.VISIBLE);
                                if (FullscreenActivity.whichDirection.equals("L2R")) {
                                    highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
                                } else {
                                    highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
                                }
                            }
                        }

                    } catch (OutOfMemoryError | Exception e) {
                        e.printStackTrace();
                        Log.d("StageMode", "Oops - error, likely too big an image!");
                    }
                    FullscreenActivity.highlightOn = true;
                } else {
                    if (!fromautoshow) {
                        // If the user has clicked the icon and no drawing exists, create one
                        takeScreenShot();
                        if (FullscreenActivity.bmScreen != null) {
                            FullscreenActivity.whattodo = "drawnotes";
                            openFragment();
                        } else {
                            Log.d("StageMode", "screenshot is null");
                        }
                    }
                    FullscreenActivity.highlightOn = false;
                }
            } else {
                if (!fromautoshow) {
                    // Don't show the warning just because the app tries to autoshow it
                    StaticVariables.myToastMessage = getString(R.string.highlight_notallowed);
                    ShowToast.showToast(StageMode.this);
                }
                FullscreenActivity.highlightOn = false;
            }
        }
    }

    @Override
    public void refreshActionBar() {
        invalidateOptionsMenu();
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        // TODO Not sure if this does anything as FullscreenActivity.sva is never assigned anything!
        SearchViewItems item = (SearchViewItems) FullscreenActivity.sva.getItem(0);
        StaticVariables.songfilename = item.getFilename();
        StaticVariables.whichSongFolder = item.getFolder();
        StaticVariables.setView = false;
        StaticVariables.myToastMessage = StaticVariables.songfilename;

        loadSong();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Replace unwanted symbols
        newText = processSong.removeUnwantedSymbolsAndSpaces(StageMode.this,preferences,newText);
        // TODO Not sure if this does anything as FullscreenActivity.sva is never assigned anything!
        if (FullscreenActivity.sva != null) {
            FullscreenActivity.sva.getFilter().filter(newText);
        }
        return false;
    }

    @Override
    public void addSlideToSet() {
        doCancelAsyncTask(add_slidetoset);
        add_slidetoset = new AddSlideToSet();
        try {
            add_slidetoset.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void allowPDFEditViaExternal() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Songs", StaticVariables.whichSongFolder,
                StaticVariables.songfilename);
        intent.setDataAndType(uri, "application/pdf");
        // Always use string resources for UI text.
        String title = getResources().getString(R.string.editpdf);
        // Create intent to show chooser
        Intent chooser = Intent.createChooser(intent, title);

        // Verify the intent will resolve to at least one activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, StaticVariables.REQUEST_PDF_CODE);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.adobe.reader")));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.adobe.reader")));
            }
        }
    }


    // Salut (Connected devices logic)
    private void holdBeforeLoading() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we receive if, check this is the first time
        if (FullscreenActivity.firstReceivingOfSalut) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalut = false;
            // Decide if the file exists on this device first
            Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Songs",
                    StaticVariables.whichSongFolder, StaticVariables.songfilename);
            if (storageAccess.uriExists(StageMode.this, uri)) {
                loadSong();
            }

            // After a delay of 800 milliseconds, reset the firstReceivingOfSalut;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalut = true;
                }
            }, 800);
        }
    }
    private void holdBeforeSending() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we send it, check this is the first time
        if (FullscreenActivity.firstSendingOfSalut) {
            // Now turn it off
            FullscreenActivity.firstSendingOfSalut = false;
            if (FullscreenActivity.network != null) {
                if (FullscreenActivity.network.isRunningAsHost) {
                    try {
                        FullscreenActivity.network.sendToAllDevices(myMessage, new SalutCallback() {
                            @Override
                            public void call() {
                                Log.e(TAG, "Oh no! The data failed to send.");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // After a delay of 800 milliseconds, reset the firstSendingOfSalut;
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.firstSendingOfSalut = true;
                    }
                }, 800);
            }
        }
    }
    private void holdBeforeSendingAutoscroll(SalutMessage sm) {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we send it, check this is the first time
        if (FullscreenActivity.firstSendingOfSalutAutoscroll) {
            // Now turn it off
            FullscreenActivity.firstSendingOfSalutAutoscroll = false;
            if (FullscreenActivity.network != null) {
                if (FullscreenActivity.network.isRunningAsHost) {
                    try {
                        FullscreenActivity.network.sendToAllDevices(sm, new SalutCallback() {
                            @Override
                            public void call() {
                                Log.e(TAG, "Oh no! The data failed to send.");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // After a delay of 800 milliseconds, reset the firstSendingOfSalut;
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.firstSendingOfSalutAutoscroll = true;
                    }
                }, 800);
            }
        }
    }
    private void holdBeforeSendingXML() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we send it, check this is the first time
        if (FullscreenActivity.firstSendingOfSalutXML) {
            // Now turn it off
            FullscreenActivity.firstSendingOfSalutXML = false;
            if (FullscreenActivity.network != null) {
                if (FullscreenActivity.network.isRunningAsHost) {
                    try {
                        FullscreenActivity.network.sendToAllDevices(mySongMessage, new SalutCallback() {
                            @Override
                            public void call() {
                                Log.e(TAG, "Oh no! The data failed to send.");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After a delay of 800 milliseconds, reset the firstSendingOfSalut;
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.firstSendingOfSalutXML = true;
                    }
                }, 800);
            }
        }
    }
    private void holdBeforeSendingSection() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we send it, check this is the first time
        if (FullscreenActivity.firstSendingOfSalutSection) {
            // Now turn it off
            FullscreenActivity.firstSendingOfSalutSection = false;
            if (FullscreenActivity.network != null) {
                if (FullscreenActivity.network.isRunningAsHost) {
                    try {
                        FullscreenActivity.network.sendToAllDevices(mySectionMessage, new SalutCallback() {
                            @Override
                            public void call() {
                                Log.e(TAG, "Oh no! The data failed to send.");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After a delay of 800 milliseconds, reset the firstSendingOfSalutSection;
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.firstSendingOfSalutSection = true;
                    }
                }, 800);
            }
        }
    }
    private void holdBeforeLoadingXML() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we receive if, check this is the first time
        if (FullscreenActivity.firstReceivingOfSalutXML) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalutXML = false;


                FullscreenActivity.mySalutXML = FullscreenActivity.mySalutXML.replace("\\n", "$$__$$");
                FullscreenActivity.mySalutXML = FullscreenActivity.mySalutXML.replace("\\", "");
                FullscreenActivity.mySalutXML = FullscreenActivity.mySalutXML.replace("$$__$$", "\n");

                // Create the temp song file
                Uri receivedUri = storageAccess.getUriForItem(StageMode.this, preferences,
                        "Received", "", "ReceivedSong");
                storageAccess.lollipopCreateFileForOutputStream(StageMode.this, preferences, receivedUri,
                        null, "Received", "", "ReceivedSong");
                OutputStream outputStream = storageAccess.getOutputStream(StageMode.this, receivedUri);
                storageAccess.writeFileFromString(FullscreenActivity.mySalutXML, outputStream);

                StaticVariables.songfilename = "ReceivedSong";
                StaticVariables.whichSongFolder = "../Received";

                loadSong();


            // After a delay of 800 milliseconds, reset the firstReceivingOfSalut;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalutXML = true;
                }
            },800);
        }
    }
    private void holdBeforeLoadingSection(int s) {
        if (FullscreenActivity.firstReceivingOfSalutSection) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalutSection = false;
            selectSection(s);

            // After a delay of 800 milliseconds, reset the firstReceivingOfSalutSection;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalutSection = true;
                }
            }, 800);
        }
    }
    private void holdBeforeAutoscrolling() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we receive if, check this is the first time
        if (FullscreenActivity.firstReceivingOfSalutAutoscroll) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalutAutoscroll = false;
            gesture5();

            // After a delay of 800 milliseconds, reset the firstReceivingOfSalut;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalutAutoscroll = true;
                }
            }, 800);
        }
    }

    @Override
    public void confirmedAction() {
        switch (FullscreenActivity.whattodo) {
            case "exit":
                try {
                    android.os.Process.killProcess(android.os.Process.myPid());
                } catch (Exception e) {
                    Log.d("StageMode", "Couldn't close the application!");
                }
                break;

            case "saveset":
                // Save the set
                setActions.saveSetMessage(StageMode.this, preferences, storageAccess, processSong);
                refreshAll();
                break;

            case "clearset":
                // Clear the set
                setActions.clearSet(StageMode.this,preferences);
                refreshAll();
                break;

            case "deletesong":
                // Delete current song
                Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Songs", StaticVariables.whichSongFolder,
                        StaticVariables.songfilename);
                storageAccess.deleteFile(StageMode.this, uri);
                // If we are autologging CCLI information
                if (preferences.getMyPreferenceBoolean(StageMode.this,"ccliAutomaticLogging",false)) {
                    PopUpCCLIFragment.addUsageEntryToLog(StageMode.this, preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                            "", "",
                            "", "", "2"); // Deleted
                }
                // Remove the item from the SQL database
                if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                    nonOpenSongSQLiteHelper.deleteSong(StageMode.this, storageAccess,preferences,nonOpenSongSQLite.getSongid());
                }
                if (sqLite!=null && sqLite.getSongid()!=null) {
                    sqLiteHelper.deleteSong(StageMode.this, sqLite.getSongid());
                }
                prepareSongMenu();

                break;

            case "deleteset":
                // Delete set
                setActions.deleteSet(StageMode.this, preferences, storageAccess);
                refreshAll();
                break;

            case "wipeallsongs":
                // Not needed anymore
                break;

        }
    }

    private void resetImageViewSizes() {
        glideimage_HorizontalScrollView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
        glideimage_HorizontalScrollView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
        glideimage_ScrollView.getLayoutParams().width = HorizontalScrollView.LayoutParams.WRAP_CONTENT;
        glideimage_ScrollView.getLayoutParams().height = HorizontalScrollView.LayoutParams.MATCH_PARENT;
        glideimage_FrameLayout.getLayoutParams().width = ScrollView.LayoutParams.MATCH_PARENT;
        glideimage_FrameLayout.getLayoutParams().height = ScrollView.LayoutParams.WRAP_CONTENT;
        glideimage.setPivotX(0.0f);
        glideimage.setPivotY(0.0f);
        glideimage.setScaleX(1.0f);
        glideimage.setScaleY(1.0f);
        glideimage.setTop(0);
        glideimage.setLeft(0);
        glideimage.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        glideimage.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
    }

    private void loadImage() {
        // Process the image location into an URI
        Uri imageUri = storageAccess.getUriForItem(StageMode.this, preferences, "Songs",
                StaticVariables.whichSongFolder, StaticVariables.songfilename);

        glideimage_ScrollView.setVisibility(View.VISIBLE);
        glideimage_HorizontalScrollView.setVisibility(View.VISIBLE);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Returns null, sizes are in the options variable
        InputStream inputStream = storageAccess.getInputStream(StageMode.this, imageUri);
        BitmapFactory.decodeStream(inputStream, null, options);

        int imgwidth = options.outWidth;
        int imgheight = options.outHeight;

        int widthavail = getAvailableWidth();
        int heightavail = getAvailableHeight();

        glideimage.setScaleX(1.0f);
        glideimage.setScaleY(1.0f);
        highlightNotes.setScaleX(1.0f);
        highlightNotes.setScaleY(1.0f);
        glideimage.setBackgroundColor(StaticVariables.transparent);
        songwidth = widthavail;
        songheight = heightavail;

        // Reset the imageview
        resetImageViewSizes();

        // Decide on the image size to use
        if (preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W").equals("Y")) {
            // Glide sorts the width vs height (keeps the image in the space available using fitCenter
            RequestOptions myOptions = new RequestOptions()
                    .fitCenter()
                    .override(widthavail, heightavail);
            GlideApp.with(StageMode.this).load(imageUri).apply(myOptions).into(glideimage);
        } else {
            // Now decide on the scaling required....
            float xscale = (float) widthavail / (float) imgwidth;
            int glideheight = (int) ((float) imgheight * xscale);
            RequestOptions myOptions = new RequestOptions()
                    .override(widthavail, glideheight);
            GlideApp.with(StageMode.this).load(imageUri).apply(myOptions).into(glideimage);

        }

        songscrollview.removeAllViews();

        // Animate the view in after a delay (waiting for slide out animation to complete
        animateInSong();

        // Check for scroll position
        delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);

        preferences.setMyPreferenceBoolean(StageMode.this,"songLoadSuccess",true);
    }

    private void loadPDF() {

        Bitmap bmp = processSong.createPDFPage(StageMode.this, preferences, storageAccess,
                getAvailableWidth(), getAvailableHeight(),
                preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W"));

        glideimage_ScrollView.setVisibility(View.VISIBLE);
        glideimage_HorizontalScrollView.setVisibility(View.VISIBLE);

        // Set the ab title to include the page info if available
        songtitle_ab.setText(StaticVariables.mTitle);
        if (StaticVariables.mKey.isEmpty()) {
            songkey_ab.setText("");
        } else {
            String s = "("+StaticVariables.mKey+")";
            songkey_ab.setText(s);
        }
        if (bmp != null) {
            int widthavail = getAvailableWidth();
            int heightavail = getAvailableHeight();

            glideimage.setScaleX(1.0f);
            glideimage.setScaleY(1.0f);
            highlightNotes.setScaleX(1.0f);
            highlightNotes.setScaleY(1.0f);
            glideimage.setBackgroundColor(StaticVariables.transparent);
            songwidth = widthavail;
            songheight = heightavail;

            // Reset the imageview
            resetImageViewSizes();

            String text = (FullscreenActivity.pdfPageCurrent + 1) + "/" + FullscreenActivity.pdfPageCount;

            songauthor_ab.setText(text);

            glideimage.setBackgroundColor(0xffffffff);

            // Decide on the image size to use
            if (preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W").equals("Y")) {
                // Glide sorts the width vs height (keeps the image in the space available using fitCenter
                RequestOptions myOptions = new RequestOptions()
                        .fitCenter()
                        .override(widthavail, heightavail);
                GlideApp.with(StageMode.this).load(bmp).apply(myOptions).into(glideimage);
            } else {
                // Now decide on the scaling required....
                float xscale = (float) widthavail / (float) bmp.getWidth();
                int glideheight = (int) ((float) bmp.getHeight() * xscale);
                RequestOptions myOptions = new RequestOptions()
                        .override(widthavail, glideheight);
                GlideApp.with(StageMode.this).load(bmp).apply(myOptions).into(glideimage);

            }

        } else {
            songauthor_ab.setText(getResources().getString(R.string.nothighenoughapi));

            // Set the image to the unhappy android
            Drawable myDrawable = getResources().getDrawable(R.drawable.unhappy_android);
            glideimage.setImageDrawable(myDrawable);

            // Set an intent to try and open the pdf with an appropriate application
            Intent target = new Intent(Intent.ACTION_VIEW);
            // Run an intent to try to show the pdf externally
            Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Songs",
                    StaticVariables.whichSongFolder, StaticVariables.songfilename);
            target.setDataAndType(uri, "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            callIntent("openpdf", target);
        }

        songscrollview.removeAllViews();

        // Animate the view in after a delay (waiting for slide out animation to complete
        animateInSong();

        // Check for scroll position
        delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);

        preferences.setMyPreferenceBoolean(StageMode.this,"songLoadSuccess",true);
    }


    private void getLearnedSongLengthValue() {
        int time = (int) (FullscreenActivity.time_passed - FullscreenActivity.time_start) / 1000;
        if (time < 0) {
            time = 0;
        }
        StaticVariables.mDuration = time + "";
        StaticVariables.learnPreDelay = false;
        StaticVariables.learnSongLength = false;

        // Save the learned values to the song
        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(StageMode.this,storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
            nonOpenSongSQLiteHelper.updateSong(StageMode.this,storageAccess,preferences,nonOpenSongSQLite);
        } else {
            PopUpEditSongFragment.justSaveSongXML(StageMode.this, preferences);
        }

        FullscreenActivity.whattodo = "page_autoscroll";
        openFragment();
    }

    private void fixSetActionButtons() {
        StaticVariables.setView = setActions.isSongInSet(StageMode.this,preferences);
        if (StaticVariables.setView) {
            // Now get the position in the set and decide on the set move buttons
            if (StaticVariables.indexSongInSet < 0) {
                // We weren't in set mode, so find the first instance of this song.
                setActions.indexSongInSet();
            }
            // If we aren't at the beginning or have pdf pages before this, enable the setBackButton
            if ((StaticVariables.indexSongInSet > 0) ||
                    (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent>0)) {
                showFAB(setBackButton,preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowSetMove",true));
            } else {
                showFAB(setBackButton,false);
            }

            // If we aren't at the end of the set or inside a multipage pdf, enable the setForwardButton
            if ((StaticVariables.indexSongInSet < StaticVariables.mSetList.length - 1) ||
                    (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent<FullscreenActivity.pdfPageCount - 1)) {
                showFAB(setForwardButton,preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonShowSetMove",true));
            } else {
                showFAB(setForwardButton,false);
            }

        } else {
            StaticVariables.indexSongInSet = -1;
            showFAB(setBackButton,false);
            showFAB(setForwardButton,false);
        }
    }

    private void getPadProgress() {
        int pos;
        boolean pad1status = PadFunctions.getPad1Status();
        boolean pad2status = PadFunctions.getPad2Status();

        boolean display1 = (pad1status && !StaticVariables.pad1Fading);
        boolean display2 = (pad2status && !StaticVariables.pad2Fading);
        // Decide which player we should be getting the status of
        if (display1 || FullscreenActivity.mPlayer1Paused) {
            pos = (int) (FullscreenActivity.mPlayer1.getCurrentPosition() / 1000.0f);
        } else if (display2 || FullscreenActivity.mPlayer2Paused) {
            pos = (int) (FullscreenActivity.mPlayer2.getCurrentPosition() / 1000.0f);
        } else {
            pos = 0;
        }

        String text = TimeTools.timeFormatFixer(pos);
        updateExtraInfoColorsAndSizes("pad");
        padcurrentTime_TextView.setText(text);

    }

    private void goToNextItem() {
        FullscreenActivity.whichDirection = "R2L";
        boolean dealtwithaspdf = false;
        StaticVariables.showstartofpdf = true;

        // If this is a PDF, check we can't move pages
        if (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent < (FullscreenActivity.pdfPageCount - 1)) {
            FullscreenActivity.pdfPageCurrent = FullscreenActivity.pdfPageCurrent + 1;

            // Load the next pdf page
            dealtwithaspdf = true;
            loadSong();

        } else {
            FullscreenActivity.pdfPageCurrent = 0;
        }

        // If this hasn't been dealt with
        if (!dealtwithaspdf && StaticVariables.setView) {
            // Is there another song in the set?  If so move, if not, do nothing
            if (StaticVariables.indexSongInSet < StaticVariables.mSetList.length - 1) {
                //FullscreenActivity.indexSongInSet += 1;
                StaticVariables.setMoveDirection = "forward";
                doMoveInSet();
            }
        } else if (!dealtwithaspdf) {
            // Try to move to the next song alphabetically
            // However, only do this if the previous item isn't a subfolder!
            boolean isfolder = false;
            try {
                if (FullscreenActivity.nextSongIndex < filenamesSongsInFolder.size() && FullscreenActivity.nextSongIndex>-1) {

                    Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Songs", "",
                            filenamesSongsInFolder.get(FullscreenActivity.nextSongIndex));
                    if (storageAccess.uriExists(StageMode.this, uri) && !storageAccess.uriIsFile(StageMode.this, uri)) {
                        isfolder = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (FullscreenActivity.nextSongIndex < filenamesSongsInFolder.size()
                        && FullscreenActivity.nextSongIndex != -1
                        && !StaticVariables.songfilename.equals(filenamesSongsInFolder.get(FullscreenActivity.nextSongIndex)) &&
                        !isfolder) {
                    FullscreenActivity.tempswipeSet = "disable";
                    StaticVariables.songfilename = filenamesSongsInFolder.get(FullscreenActivity.nextSongIndex);
                    loadSong();

                    // Set a runnable to reset swipe back to original value after 1 second
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FullscreenActivity.tempswipeSet = "enable";
                        }
                    }, FullscreenActivity.delayswipe_time);
                } else {
                    showToastMessage(getResources().getString(R.string.lastsong));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendMidi() {
        if ((Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)
                && StaticVariables.midiDevice!=null &&
                StaticVariables.midiInputPort!=null && StaticVariables.mMidi!=null &&
                !StaticVariables.mMidi.isEmpty()) && !StaticVariables.mMidi.trim().equals("")) {
            // Declare the midi code
            Handler mh = new Handler();
            mh.post(new Runnable() {
                @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    try {
                        if (midi==null) {
                            midi = new Midi();
                        }
                        // Split the midi messages by line, after changing , into new line
                        StaticVariables.mMidi = StaticVariables.mMidi.replace(",", "\n");
                        StaticVariables.mMidi = StaticVariables.mMidi.replace("\n\n", "\n");
                        String[] midilines = StaticVariables.mMidi.trim().split("\n");
                        for (String ml : midilines) {
                            if (midi!=null) {
                                midi.sendMidi(midi.returnBytesFromHexText(ml));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void doDownload(String filename) {
        if (do_download!=null) {
            doCancelAsyncTask(do_download);
        }
        do_download = new DownloadTask(StageMode.this,filename);

        // Only do this if we have a valid internet connection.

        try {
            do_download.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPerformanceView1col() {
        doCancelAsyncTask(createperformanceview1col_async);
        createperformanceview1col_async = new CreatePerformanceView1Col();
        try {
            createperformanceview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void takeScreenShot() {
        // This option is hidden (Song Menu/Highlight) unless we are in Performance Mode
        if (!StaticVariables.whichMode.equals("Performance")) {
            FullscreenActivity.highlightOn = false;
            highlightNotes.setVisibility(View.GONE);
            StaticVariables.myToastMessage = getString(R.string.switchtoperformmode);
            ShowToast.showToast(StageMode.this);
        } else {
            if (StaticVariables.thisSongScale == null || !StaticVariables.thisSongScale.equals("Y")) {
                StaticVariables.myToastMessage = getString(R.string.highlight_notallowed);
                ShowToast.showToast(StageMode.this);
                FullscreenActivity.bmScreen = null;
            } else {
                boolean vis = false;
                if (highlightNotes != null && highlightNotes.getVisibility() == View.VISIBLE) {
                    vis = true;
                }

                if (vis) {
                    highlightNotes.setVisibility(View.GONE);
                }
                if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                    glideimage_ScrollView.destroyDrawingCache();
                    glideimage_ScrollView.setDrawingCacheEnabled(true);
                    glideimage_ScrollView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
                    FullscreenActivity.bmScreen = null;
                    try {
                        FullscreenActivity.bmScreen = glideimage_ScrollView.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
                    } catch (Exception e) {
                        Log.d("StageMode", "error getting the screenshot!");
                    } catch (OutOfMemoryError e) {
                        Log.d("StageMode", "not enough memory");
                    }

                } else {
                    songscrollview.destroyDrawingCache();
                    songscrollview.setDrawingCacheEnabled(true);
                    songscrollview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
                    FullscreenActivity.bmScreen = null;
                    try {
                        FullscreenActivity.bmScreen = songscrollview.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
                    } catch (Exception e) {
                        Log.d("StageMode", "error getting the screenshot!");
                    } catch (OutOfMemoryError o) {
                        Log.d("StageMode", "Out of memory");
                    }

                }
                if (vis) {
                    highlightNotes.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void createPerformanceView2col() {
        doCancelAsyncTask(createperformanceview2col_async);
        createperformanceview2col_async = new CreatePerformanceView2Col();
        try {
            createperformanceview2col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rebuildSearchIndex() {
        showToastMessage(getString(R.string.search_rebuild));
        RebuildSearchIndex doRebuildSearchIndex = new RebuildSearchIndex();
        doRebuildSearchIndex.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    @SuppressLint("StaticFieldLeak")
    private class RebuildSearchIndex extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Write a crude text file (line separated) with the song Ids (folder/file)
            ArrayList<String> songIds = storageAccess.listSongs(StageMode.this, preferences);
            storageAccess.writeSongIDFile(StageMode.this, preferences, songIds);

            // Try to create the basic database
            sqLiteHelper.resetDatabase(StageMode.this);
            sqLiteHelper.insertFast(StageMode.this,storageAccess);

            // Build the full index
            indexSongs.fullIndex(StageMode.this,preferences,storageAccess, sqLiteHelper, songXML,
                    chordProConvert, onSongConvert, textSongConvert, usrConvert);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            showToastMessage(getString(R.string.search_index_end));
            // Update the song menu
            prepareSongMenu();
        }
    }

    private void createPerformanceView3col() {
        doCancelAsyncTask(createperformanceview3col_async);
        createperformanceview3col_async = new CreatePerformanceView3Col();
        try {
            createperformanceview3col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == StaticVariables.LINK_AUDIO || requestCode == StaticVariables.LINK_OTHER) {
            // This has been called from the popuplinks fragment
            try {
                newFragment.onActivityResult(requestCode, resultCode, data);
            } catch (Exception e) {
                Log.d("StageMode","Error sending activity result to fragment");
            }

        } else if (requestCode==StaticVariables.REQUEST_IMAGE_CODE) {
            // This has been called from the custom slides fragment
            try {
                newFragment.onActivityResult(requestCode, resultCode, data);
            } catch (Exception e) {
                Log.d("StageMode", "Error sending activity result to fragment");
            }

        } else if (requestCode==StaticVariables.REQUEST_BACKGROUND_IMAGE1 ||
                requestCode==StaticVariables.REQUEST_BACKGROUND_IMAGE2 ||
                requestCode==StaticVariables.REQUEST_BACKGROUND_VIDEO1 ||
                requestCode==StaticVariables.REQUEST_BACKGROUND_VIDEO2 ||
                requestCode==StaticVariables.REQUEST_CUSTOM_LOGO) {
            // This has been called from the layout dialog.  Send the info back there
            try {
                newFragment.onActivityResult(requestCode, resultCode, data);
            } catch (Exception e) {
                Log.d("StageMode", "Error sending activity result to fragment");
            }

        } else if (requestCode == StaticVariables.REQUEST_CAMERA_CODE && resultCode == Activity.RESULT_OK) {
            FullscreenActivity.whattodo = "savecameraimage";
            openFragment();

        } else if (requestCode == StaticVariables.REQUEST_PDF_CODE) {
            // PDF sent back, so reload it
            loadSong();

        } else if (requestCode == StaticVariables.REQUEST_FILE_CHOOSER && data != null && data.getExtras() != null) {
            try {
                // This is for the File Chooser returning a file uri
                String filelocation = data.getExtras().getString("data");
                if (filelocation != null) {
                    boolean validfiletype = (FullscreenActivity.whattodo.equals("processimportosb") && filelocation.endsWith(".osb")) ||
                            (FullscreenActivity.whattodo.equals("importos") && filelocation.endsWith(".backup")) ||
                            FullscreenActivity.whattodo.equals("doimport") ||
                            FullscreenActivity.whattodo.equals("doimportset");

                    if (validfiletype) {
                        File f = new File(filelocation);
                        FullscreenActivity.file_uri = FileProvider.getUriForFile(StageMode.this,
                                "OpenSongAppFiles", f);
                        openFragment();
                    } else {
                        StaticVariables.myToastMessage = getString(R.string.file_type_unknown);
                        ShowToast.showToast(StageMode.this);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == StaticVariables.REQUEST_PROFILE_LOAD && data!=null && data.getData()!=null) {
            // Loading in a profile
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean success = profileActions.doLoadProfile(StageMode.this,preferences,storageAccess,data.getData());
                    if (success) {
                        StaticVariables.myToastMessage = getString(R.string.success);
                    } else {
                        StaticVariables.myToastMessage = getString(R.string.error);
                    }
                    // Once done, reload everything
                    runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          ShowToast.showToast(StageMode.this);
                                          loadStartUpVariables();
                                          refreshAll();
                                      }
                                  }
                            );
                }
            }).start();

        } else if (requestCode == StaticVariables.REQUEST_PROFILE_SAVE && data!=null && data.getData()!=null) {
            // Saving a profile
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean success = profileActions.doSaveProfile(StageMode.this,preferences,storageAccess,data.getData());
                    if (success) {
                        StaticVariables.myToastMessage = getString(R.string.success);
                    } else {
                        StaticVariables.myToastMessage = getString(R.string.error);
                    }
                    // Once done, say so
                    runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          ShowToast.showToast(StageMode.this);
                                      }
                                  }
                    );
                }
            }).start();
        }
    }

    private void animateInSong() {
        // If autoshowing highlighter notes
        if (preferences.getMyPreferenceBoolean(StageMode.this,"drawingAutoDisplay",true)) {
            showHighlight();
        }
        // Now scroll in the song via an animation
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            songscrollview.setVisibility(View.GONE);
            glideimage_ScrollView.setVisibility(View.VISIBLE);
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
            } else {
                glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
            }
        } else {
            glideimage_ScrollView.setVisibility(View.GONE);
            glideimage_HorizontalScrollView.setVisibility(View.GONE);
            songscrollview.setVisibility(View.VISIBLE);
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
            } else {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
            }
        }

        // Set the overrides back

        // Check for dual screen presentation
        if (StaticVariables.whichMode.equals("Performance")) {
            dualScreenWork();
        } else {
            if (!sectionpresented) { // So it isn't called for each section.
                sectionpresented = true;
                dualScreenWork();
            }
        }

        // Check the scroll buttons
        onScrollAction();

        // Keep a note of the content size in case we pinch zoom

        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            ViewTreeObserver.OnGlobalLayoutListener vto = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    songwidth = glideimage.getMeasuredWidth();
                    songheight = glideimage.getMeasuredHeight();
                    if (songwidth>0 && songheight>0) {
                        highlightNotes.setScaleX(1.0f);
                        highlightNotes.setScaleY(1.0f);
                        glideimage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            };

            glideimage.getViewTreeObserver().addOnGlobalLayoutListener(vto);

        } else {
            final LinearLayout songbit = (LinearLayout) songscrollview.getChildAt(0);
            songbit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    songwidth = songbit.getMeasuredWidth();
                    songheight = songbit.getMeasuredHeight();
                    songbit.setScaleX(1.0f);
                    songbit.setScaleY(1.0f);
                    highlightNotes.setScaleX(1.0f);
                    highlightNotes.setScaleY(1.0f);
                }
            }, 1000);
        }

        songscrollview.scrollTo(0,0);
        glideimage_ScrollView.scrollTo(0,0);
        FullscreenActivity.newPosFloat = 0.0f;
        // Automatically start the autoscroll
        if ((preferences.getMyPreferenceBoolean(StageMode.this,"autoscrollAutoStart",false) &&
                StaticVariables.clickedOnAutoScrollStart) || pdfCanContinueScrolling) {
            StaticVariables.autoscrollok = processSong.isAutoScrollValid(StageMode.this,preferences);
            if ((FullscreenActivity.isPDF || FullscreenActivity.isImage) && StaticVariables.autoscrollok) {
                glideimage_ScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        startAutoScroll();
                    }
                });
            } else if (!FullscreenActivity.isPDF && !FullscreenActivity.isImage && StaticVariables.autoscrollok) {
                songscrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        startAutoScroll();
                    }
                });
            }
        }

        setUpCapoInfo();

        // Could add CCLI autologging here, but excessive as may just be browsing.  So only log when
        // a song is added to a set.
    }


    private void setUpCapoInfo() {
        updateExtraInfoColorsAndSizes("capo");
        boolean bothempty = true;
        StringBuilder allcapodetails = new StringBuilder();
        // If we are showing capo chords, show this info
        if (capoinfo!=null && !StaticVariables.mCapo.equals("") && !StaticVariables.mCapo.equals("0")) {
            String t = processSong.getCapoInfo(StageMode.this, preferences);
            allcapodetails.append(t);
            capoinfo.setText(t);
            capoinfo.setVisibility(View.VISIBLE);
            bothempty = false;
        } else if (capoinfo!=null){
            capoinfo.setVisibility(View.GONE);
        }

        String capokey = processSong.getCapoNewKey();
        if (!capokey.equals("")) {
            allcapodetails.append(" - ").append(capokey);
            String t = " (" + capokey + ")";
            capoinfonewkey.setText(t);
            capoinfonewkey.setVisibility(View.VISIBLE);
            bothempty = false;
        } else {
            capoinfonewkey.setVisibility(View.GONE);
        }

        if (bothempty || !preferences.getMyPreferenceBoolean(StageMode.this,"displayCapoChords",true) ||
                !preferences.getMyPreferenceBoolean(StageMode.this,"displayChords",true)) {
            capoInfo.setVisibility(View.GONE);
        } else {
            capoInfo.setVisibility(View.VISIBLE);
            // Highlight the capoInfo to draw attention to it
            CustomAnimations.highlightAction(capoInfo,StageMode.this);
        }

        // Add the capo information for theactionbar
        String s = allcapodetails.toString();
        String capotext;
        if (s.isEmpty()) {
            capotext = "";
        } else {
            capotext = " ["+s+"]";
        }
        songcapo_ab.setText(capotext);
    }

    private void goToPreviousItem() {
        FullscreenActivity.whichDirection = "L2R";
        boolean dealtwithaspdf = false;
        StaticVariables.showstartofpdf = true; // Default value - change later if need be

        // If this is a PDF, check we can't move pages
        if (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0) {
            FullscreenActivity.pdfPageCurrent = FullscreenActivity.pdfPageCurrent - 1;
            dealtwithaspdf = true;
            loadSong();
        } else {
            FullscreenActivity.pdfPageCurrent = 0;
        }

        // If this hasn't been dealt with
        if (!dealtwithaspdf && StaticVariables.setView) {
            StaticVariables.showstartofpdf = false; // Moving backwards, so start at end of pdf
            // Is there another song in the set?  If so move, if not, do nothing
            if (StaticVariables.indexSongInSet > 0 && StaticVariables.mSetList.length > 0) {
                //FullscreenActivity.indexSongInSet -= 1;
                StaticVariables.setMoveDirection = "back";
                doMoveInSet();
            }
        } else if (!dealtwithaspdf) {
            // Try to move to the previous song alphabetically
            // However, only do this if the previous item isn't a subfolder!
            boolean isfolder = false;
            if (FullscreenActivity.previousSongIndex >= 0) {
                try {
                    Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Songs", "",
                            filenamesSongsInFolder.get(FullscreenActivity.previousSongIndex));
                    if (storageAccess.uriExists(StageMode.this, uri) && !storageAccess.uriIsFile(StageMode.this, uri)) {
                        isfolder = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                if (FullscreenActivity.previousSongIndex >= 0
                        && !StaticVariables.songfilename.equals(filenamesSongsInFolder.get(FullscreenActivity.previousSongIndex))
                        && !isfolder) {
                    FullscreenActivity.tempswipeSet = "disable";

                    StaticVariables.songfilename = filenamesSongsInFolder.get(FullscreenActivity.previousSongIndex);
                    loadSong();

                    // Set a runnable to reset swipe back to original value after 1 second
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FullscreenActivity.tempswipeSet = "enable";
                        }
                    }, FullscreenActivity.delayswipe_time);
                } else {
                    showToastMessage(getResources().getString(R.string.firstsong));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DualScreenWork extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            return null;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    if (FullscreenActivity.isPresenting && !FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationService.ExternalDisplay.doUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationServiceHDMI.doUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareView() {
        doCancelAsyncTask(preparesongview_async);
        preparesongview_async = new PrepareSongView();
        try {
            preparesongview_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectSection(int whichone) {
        if (whichone < 0) {
            whichone = 0;
        }

        StaticVariables.currentSection = whichone;

        // Send section to other devices (checks we are in stage or presentation mode in called method
        if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
            try {
                sendSongSectionToConnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Set this sections alpha to 1.0f;
        try {
            FullscreenActivity.sectionviews[whichone].setAlpha(1.0f);
        } catch (Exception e) {
            Log.d("StageMode", "Section not found");
        }

        // Smooth scroll to show this view at the top of the page unless we are autoscrolling
        try {
            if (!StaticVariables.isautoscrolling) {
                songscrollview.smoothScrollTo(0, FullscreenActivity.sectionviews[whichone].getTop());
            }
        } catch (Exception e) {
            Log.d("StageMode", "Section not found");
        }

        try {
            // Go through each of the views and set the alpha of the others to 0.5f;
            for (int x = 0; x < FullscreenActivity.sectionviews.length; x++) {
                if (x != whichone) {
                    FullscreenActivity.sectionviews[x].setAlpha(0.5f);
                }
            }
            FullscreenActivity.tempswipeSet = "enable";
            StaticVariables.setMoveDirection = "";

            dualScreenWork();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resizeStageView() {
        doCancelAsyncTask(resizestage_async);
        resizestage_async = new ResizeStageView();
        try {
            resizestage_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ResizeStageView extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            try {
                // Remove the views from the test panes if there was any!
                testpane.removeAllViews();
                testpane1_2.removeAllViews();
                testpane2_2.removeAllViews();
                testpane1_3.removeAllViews();
                testpane2_3.removeAllViews();
                testpane3_3.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                // Decide on the best scaling
                FullscreenActivity.padding = getPixelsFromDpi(16);
                int availablewidth_1col = getAvailableWidth() - getPixelsFromDpi(16);
                int availableheight = (int) (preferences.getMyPreferenceFloat(StageMode.this,"stageModeScale", 0.8f) *
                        getAvailableHeight()) - getPixelsFromDpi(16);

                for (int f = 0; f < FullscreenActivity.sectionviews.length; f++) {
                    float myscale_1_1_col_x = availablewidth_1col / (float) FullscreenActivity.viewwidth[f];
                    float myscale_1_1_col_y = availableheight / (float) FullscreenActivity.viewheight[f];
                    StaticVariables.sectionScaleValue[f] = processSong.getStageScaleValue(myscale_1_1_col_x, myscale_1_1_col_y);
                    float maxscale = preferences.getMyPreferenceFloat(StageMode.this,"fontSizeMax",50.0f) / 12.0f;
                    if (StaticVariables.sectionScaleValue[f] > maxscale) {
                        StaticVariables.sectionScaleValue[f] = maxscale;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        public void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    FullscreenActivity.scalingfiguredout = true;

                    // Now render the scaled song!
                    createStageView1col();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createStageView1col() {
        doCancelAsyncTask(createstageview1col_async);
        createstageview1col_async = new CreateStageView1Col();
        try {
            createstageview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPadsOnStatus() {
        StaticVariables.padson = StaticVariables.pad1Playing || StaticVariables.pad2Playing;
        if (!StaticVariables.padson) {
            try {
                backingtrackProgress.setVisibility(View.GONE);
                updateExtraInfoColorsAndSizes("pad");
            } catch (Exception e) {
                Log.d("StageMode", "Can't touch the view - " + e);
                // This will happen if killPads was called from an async task
            }
        }
    }

    private void resizePerformanceView() {
        doCancelAsyncTask(resizeperformance_async);
        resizeperformance_async = new ResizePerformanceView();
        try {
            resizeperformance_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ResizePerformanceView extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                // Set the view widths and heights
                FullscreenActivity.viewwidth[0] = testpane.getMeasuredWidth();
                FullscreenActivity.viewwidth[1] = testpane1_2.getMeasuredWidth();
                FullscreenActivity.viewwidth[2] = testpane2_2.getMeasuredWidth();
                FullscreenActivity.viewwidth[3] = testpane1_3.getMeasuredWidth();
                FullscreenActivity.viewwidth[4] = testpane2_3.getMeasuredWidth();
                FullscreenActivity.viewwidth[5] = testpane3_3.getMeasuredWidth();
                FullscreenActivity.viewheight[0] = testpane.getMeasuredHeight();
                FullscreenActivity.viewheight[1] = testpane1_2.getMeasuredHeight();
                FullscreenActivity.viewheight[2] = testpane2_2.getMeasuredHeight();
                FullscreenActivity.viewheight[3] = testpane1_3.getMeasuredHeight();
                FullscreenActivity.viewheight[4] = testpane2_3.getMeasuredHeight();
                FullscreenActivity.viewheight[5] = testpane3_3.getMeasuredHeight();

                // Remove the views from the test panes
                testpane.removeAllViews();
                testpane1_2.removeAllViews();
                testpane2_2.removeAllViews();
                testpane1_3.removeAllViews();
                testpane2_3.removeAllViews();
                testpane3_3.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                // Decide on the best scaling
                FullscreenActivity.padding = getPixelsFromDpi(6);
                int availablewidth_1col = getAvailableWidth() - FullscreenActivity.padding;
                int availablewidth_2col = (int) (getAvailableWidth() / 2.0f) - getPixelsFromDpi(12 + 4);
                int availablewidth_3col = (int) (getAvailableWidth() / 3.0f) - getPixelsFromDpi(18 + 4 + 4);
                int availableheight = getAvailableHeight() - getPixelsFromDpi(12);

                float myscale_1_1_col_x = (float) availablewidth_1col / (float) FullscreenActivity.viewwidth[0];
                width_scale = myscale_1_1_col_x;
                float myscale_1_2_col_x = (float) availablewidth_2col / (float) FullscreenActivity.viewwidth[1];
                float myscale_2_2_col_x = (float) availablewidth_2col / (float) FullscreenActivity.viewwidth[2];
                float myscale_1_3_col_x = (float) availablewidth_3col / (float) FullscreenActivity.viewwidth[3];
                float myscale_2_3_col_x = (float) availablewidth_3col / (float) FullscreenActivity.viewwidth[4];
                float myscale_3_3_col_x = (float) availablewidth_3col / (float) FullscreenActivity.viewwidth[5];
                float myscale_1_1_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[0];
                float myscale_1_2_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[1];
                float myscale_2_2_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[2];
                float myscale_1_3_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[3];
                float myscale_2_3_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[4];
                float myscale_3_3_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[5];

                // If users don't want to maximise the size of every column
                if (preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W").equals("Y") &&
                        !preferences.getMyPreferenceBoolean(StageMode.this,"songAutoScaleColumnMaximise",true)) {
                    // Two columns
                    if (myscale_1_2_col_x < myscale_2_2_col_x) {
                        myscale_2_2_col_x = myscale_1_2_col_x;
                        myscale_2_2_col_y = myscale_1_2_col_y;
                    } else {
                        myscale_1_2_col_x = myscale_2_2_col_x;
                        myscale_1_2_col_y = myscale_2_2_col_y;
                    }

                    // Three columns
                    if (myscale_1_3_col_x < myscale_2_3_col_x) {
                        myscale_2_3_col_x = myscale_1_3_col_x;
                        myscale_2_3_col_y = myscale_1_3_col_y;
                    } else {
                        myscale_1_3_col_x = myscale_2_3_col_x;
                        myscale_1_3_col_y = myscale_2_3_col_y;
                    }
                    if (myscale_1_3_col_x < myscale_3_3_col_x) {
                        myscale_3_3_col_x = myscale_1_3_col_x;
                        myscale_3_3_col_y = myscale_1_3_col_y;
                    } else {
                        myscale_1_3_col_x = myscale_3_3_col_x;
                        myscale_2_3_col_x = myscale_3_3_col_x;
                        myscale_1_3_col_y = myscale_3_3_col_y;
                        myscale_2_3_col_y = myscale_3_3_col_y;
                    }
                }

                StaticVariables.sectionScaleValue[0] = processSong.getScaleValue(StageMode.this,
                        preferences, myscale_1_1_col_x, myscale_1_1_col_y, 12.0f);
                StaticVariables.sectionScaleValue[1] = processSong.getScaleValue(StageMode.this,
                        preferences, myscale_1_2_col_x, myscale_1_2_col_y, 12.0f);
                StaticVariables.sectionScaleValue[2] = processSong.getScaleValue(StageMode.this,
                        preferences, myscale_2_2_col_x, myscale_2_2_col_y, 12.0f);
                StaticVariables.sectionScaleValue[3] = processSong.getScaleValue(StageMode.this,
                        preferences, myscale_1_3_col_x, myscale_1_3_col_y, 12.0f);
                StaticVariables.sectionScaleValue[4] = processSong.getScaleValue(StageMode.this,
                        preferences, myscale_2_3_col_x, myscale_2_3_col_y, 12.0f);
                StaticVariables.sectionScaleValue[5] = processSong.getScaleValue(StageMode.this,
                        preferences, myscale_3_3_col_x, myscale_3_3_col_y, 12.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        public void onPostExecute(String s) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                if (!cancelled) {
                    float myscale;
                    float minscale = preferences.getMyPreferenceFloat(StageMode.this,"fontSizeMin",8.0f) / 12.0f;
                    float maxscale = preferences.getMyPreferenceFloat(StageMode.this,"fontSizeMax",50.0f) / 12.0f;
                    float nonscaled = preferences.getMyPreferenceFloat(StageMode.this,"fontSize",42.0f) / 12.0f;

                    float minscale1col = StaticVariables.sectionScaleValue[0];

                    // Now we need to decide on the scale values to use and which view style we are going for.
                    // First up, if we are going for full scaling..

                    if (StaticVariables.thisSongScale.equals("Y")) {
                        float minscale2col = StaticVariables.sectionScaleValue[1];
                        float minscale3col = StaticVariables.sectionScaleValue[3];

                        // Decide if the other columns are smaller
                        if (StaticVariables.sectionScaleValue[2] < minscale2col) {
                            minscale2col = StaticVariables.sectionScaleValue[2];
                        }

                        if (StaticVariables.sectionScaleValue[4] < minscale3col) {
                            minscale3col = StaticVariables.sectionScaleValue[4];
                        }
                        if (StaticVariables.sectionScaleValue[5] < minscale3col) {
                            minscale3col = StaticVariables.sectionScaleValue[5];
                        }

                        // We will prefer the view with the biggest scaling
                        StaticVariables.myToastMessage = "";
                        myscale = minscale1col;
                        coltouse = 1;
                        if (minscale2col > myscale) {
                            myscale = minscale2col;
                            coltouse = 2;
                        }
                        if (minscale3col > myscale) {
                            myscale = minscale3col;
                            coltouse = 3;
                        }

                        // All is good unless we have exceeded the prefered max scale
                        if (myscale > maxscale) {
                            myscale = maxscale;
                        }

                        // All is good, unless myscale is below the minimum size and overriding is on!
                        if (myscale < minscale && preferences.getMyPreferenceBoolean(StageMode.this,"songAutoScaleOverrideFull",true)) {
                            //Set to width only
                            StaticVariables.thisSongScale = "W";
                            StaticVariables.myToastMessage = getString(R.string.override_fullautoscale);
                            StaticVariables.sectionScaleValue[0] = width_scale;
                            coltouse = 1;
                        }

                        if (myscale < minscale &&
                                preferences.getMyPreferenceBoolean(StageMode.this, "songAutoScaleOverrideWidth", false) &&
                                !preferences.getMyPreferenceBoolean(StageMode.this,"songAutoScaleOverrideFull",true)) {
                            //Set to non scaled
                            StaticVariables.thisSongScale = "N";
                            StaticVariables.myToastMessage = getString(R.string.override_widthautoscale);
                            StaticVariables.sectionScaleValue[0] = nonscaled;
                            coltouse = 1;
                        }
                    }

                    // If we are autoscaling to width only...
                    if (StaticVariables.thisSongScale.equals("W")) {
                        myscale = width_scale;

                        // Check we haven't exceeded the max scale preference
                        if (myscale > maxscale) {
                            myscale = maxscale;
                        }

                        StaticVariables.sectionScaleValue[0] = myscale;
                        coltouse = 1;

                        // All is good, unless myscale is below the minimum size and overriding is on!
                        if (myscale < minscale &&
                                preferences.getMyPreferenceBoolean(StageMode.this, "songAutoScaleOverrideWidth", false)) {
                            //Set to scaling off
                            StaticVariables.thisSongScale = "N";
                            StaticVariables.myToastMessage = getString(R.string.override_widthautoscale);
                            StaticVariables.sectionScaleValue[0] = nonscaled;
                            coltouse = 1;
                        }
                    }

                    // If autoscaling is off...
                    if (StaticVariables.thisSongScale.equals("N")) {
                        coltouse = 1;
                        StaticVariables.sectionScaleValue[0] = nonscaled;
                    }

                    FullscreenActivity.scalingfiguredout = true;
                    ShowToast.showToast(StageMode.this);

                    // Now render the scaled song!
                    if (coltouse == 1) {
                        createPerformanceView1col();
                    } else if (coltouse == 2) {
                        createPerformanceView2col();
                    } else {
                        createPerformanceView3col();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private int getAvailableWidth() {
        int val;
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();

        try {
            display.getRealMetrics(metrics);
            val = metrics.widthPixels;

        } catch (Exception e) {
            e.printStackTrace();
            val = mypage.getWidth();
        }

        FullscreenActivity.padding = getPixelsFromDpi(6);
        FullscreenActivity.myWidthAvail = val;
        return val;
    }

    private int getAvailableHeight() {
        int val;
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();

        try {
            display.getRealMetrics(metrics);
            val = metrics.heightPixels;

        } catch (Exception e) {
            e.printStackTrace();
            val = mypage.getHeight();
        }

        if (!preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
            //StaticVariables.ab_height = ab.getHeight();
            val = val - ab.getHeight();
        }

        return val;
    }

    private int getPixelsFromDpi(int dps) {
        return dps * (int) (getResources().getDisplayMetrics().densityDpi / 160f);
    }

    @Override
    public void showToastMessage(String message) {
        if (message != null && !message.isEmpty()) {
            StaticVariables.myToastMessage = message;
            ShowToast.showToast(StageMode.this);
        }
    }

    private void showSticky() {
        doCancelAsyncTask(show_sticky);
        show_sticky = new ShowSticky();
        try {
            show_sticky.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ShowSticky extends AsyncTask<Object, Void, String> {

        long stickycurrtime;
        long stickytimetohide;
        boolean stickydonthide;

        @Override
        protected void onPreExecute() {
            try {
                // If the sticky notes were already showing, close them
                if (stickyPopUpWindow != null && stickyPopUpWindow.isShowing()) {
                    try {
                        stickyPopUpWindow.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Open the sticky note window up again
                displaySticky();

                // Get the current time
                stickycurrtime = System.currentTimeMillis();

                // Set the time to close the sticky note
                stickytimetohide = stickycurrtime + (preferences.getMyPreferenceInt(StageMode.this,"timeToDisplaySticky",5) * 1000);

                if (preferences.getMyPreferenceInt(StageMode.this,"timeToDisplaySticky",5) == 0) {
                    stickydonthide = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (!stickydonthide && !StaticVariables.mNotes.equals("")) {
                    while (System.currentTimeMillis() < stickytimetohide) {
                        // Do nothing
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    if (!stickydonthide) {
                        // If there is a sticky note showing, remove it
                        if (stickyPopUpWindow != null && stickyPopUpWindow.isShowing()) {
                            try {
                                stickyPopUpWindow.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void displaySticky() {
        if (stickyPopUpWindow!=null && stickyPopUpWindow.isShowing()) {
            try {
                stickyPopUpWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (StaticVariables.mNotes != null && !StaticVariables.mNotes.isEmpty()) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                assert layoutInflater != null;
                @SuppressLint("InflateParams") final View popupView = layoutInflater.inflate(R.layout.popup_float_sticky, null);
                // Decide on the popup position
                int hp = preferences.getMyPreferenceInt(StageMode.this,"stickyXPosition",-1);
                int vp = preferences.getMyPreferenceInt(StageMode.this,"stickyYPosition",-1);
                int sw = getAvailableWidth();
                int sh = getAvailableHeight();
                int stickywidth = preferences.getMyPreferenceInt(StageMode.this,"stickyWidth",400);
                if (hp==-1 || hp>sw) {
                    hp = sw - stickywidth - (int) ((float) setButton.getMeasuredWidth() * 1.2f);
                }
                if (hp < 0) {
                    hp = 0;
                }
                if (vp==-1 || hp>sh) {
                    vp = (int) ((float) ab_toolbar.getMeasuredHeight() * 1.2f);
                }
                if (vp < 0) {
                    vp = 0;
                }
                preferences.setMyPreferenceInt(StageMode.this,"stickyXPosition",hp);
                preferences.setMyPreferenceInt(StageMode.this,"stickyYPosition",vp);
                stickyPopUpWindow = new PopupWindow(popupView);
                stickyPopUpWindow.setFocusable(false);
                stickyPopUpWindow.setWidth(stickywidth);
                stickyPopUpWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                stickyPopUpWindow.setContentView(popupView);
                FloatingActionButton closeStickyFloat = popupView.findViewById(R.id.closeMe);
                LinearLayout myTitle = popupView.findViewById(R.id.myTitle);
                TextView mySticky = popupView.findViewById(R.id.mySticky);
                mySticky.setTextColor(defstickytextcolor);
                float sts;
                if (preferences.getMyPreferenceBoolean(StageMode.this,"stickyLargeFont",true)) {
                    sts = StaticVariables.infoBarLargeTextSize;
                } else {
                    sts = StaticVariables.infoBarSmallTextSize;
                }
                mySticky.setTextSize(sts);
                mySticky.setTypeface(StaticVariables.typefaceSticky);
                mySticky.setText(StaticVariables.mNotes);
                popupView.setBackgroundResource(R.drawable.popup_sticky);
                GradientDrawable drawable = (GradientDrawable) popupView.getBackground();
                drawable.setColor(defstickybgcolor);
                popupView.setPadding(10, 10, 10, 10);
                stickyPopUpWindow.showAtLocation(mypage, Gravity.TOP | Gravity.START, hp, vp);
                RelativeLayout stickyfloat = popupView.findViewById(R.id.stickyfloat);
                stickyfloat.setAlpha(preferences.getMyPreferenceFloat(StageMode.this,"stickyOpacity",0.8f));
                closeStickyFloat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // If there is a sticky note showing, remove it
                        if (stickyPopUpWindow != null && stickyPopUpWindow.isShowing()) {
                            try {
                                stickyPopUpWindow.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                myTitle.setOnTouchListener(new View.OnTouchListener() {
                    int orgX, orgY;
                    int offsetX, offsetY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                orgX = (int) event.getX();
                                orgY = (int) event.getY();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                offsetX = (int) event.getRawX() - orgX;
                                offsetY = (int) event.getRawY() - orgY;
                                stickyPopUpWindow.update(offsetX, offsetY, -1, -1, true);
                                break;
                            case MotionEvent.ACTION_UP:
                                preferences.setMyPreferenceInt(StageMode.this,"stickyXPosition",offsetX);
                                preferences.setMyPreferenceInt(StageMode.this,"stickyYPosition",offsetY);
                        }
                        return true;
                    }
                });
            } else {
                // No sticky note, so show the edit window
                FullscreenActivity.whattodo = "page_sticky";
                openFragment();
            }
        }
    }

    //@SuppressLint("ServiceCast")
    private void updateDisplays() {
        // This is called when display devices are changed (connected, disconnected, etc.)
        StaticVariables.activity = StageMode.this;
        Intent intent = new Intent(StageMode.this,
                StageMode.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                StageMode.this, 0, intent, 0);

        CastRemoteDisplayLocalService.NotificationSettings settings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(notificationPendingIntent).build();

        if (mSelectedDevice != null) {
            CastRemoteDisplayLocalService.startService(
                    getApplicationContext(),
                    PresentationService.class, getString(R.string.app_id),
                    mSelectedDevice, settings,
                    new CastRemoteDisplayLocalService.Callbacks() {
                        @Override
                        public void onServiceCreated(
                                CastRemoteDisplayLocalService service) {
                        }

                        @Override
                        public void onRemoteDisplaySessionStarted(
                                CastRemoteDisplayLocalService service) {
                        }

                        @Override
                        public void onRemoteDisplaySessionError(Status status) {
                            Log.d("StageMode", "onRemoteDisplaySessionError status=" + status);
                        }

                        @Override
                        public void onRemoteDisplaySessionEnded(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {
                            Log.d("StageMode", "onRemoteDisplaySessionEnded");
                        }
                    });
        } else {
            // Might be a hdmi connection
            /*try {
                Display mDisplay = mMediaRouter.getSelectedRoute().getPresentationDisplay();
                if (mDisplay != null) {
                    hdmi = new PresentationServiceHDMI(StageMode.this, mDisplay, processSong);
                    hdmi.show();
                    FullscreenActivity.isHDMIConnected = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            // Try this code (Alternative to use HDMI as Chromebooks not coping with above
            try {
                DisplayManager dm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
                if (dm!=null) {
                    Display[] displays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
                    for (Display mDisplay : displays) {
                        hdmi = new PresentationServiceHDMI(StageMode.this, mDisplay, processSong);
                        hdmi.show();
                        FullscreenActivity.isHDMIConnected = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("d","Error"+e);
            }
        }
    }

    private void doPedalAction(String action) {
        boolean val;
        switch (action) {
            default:
                StaticVariables.myToastMessage = getString(R.string.pedal) + " - " + getString(R.string.notset);
                ShowToast.showToast(StageMode.this);
                break;

            case "prev":
                pedalPrevious();
                break;

            case "next":
                pedalNext();
                break;

            case "up":
                pedalUp();
                break;

            case "down":
                pedalDown();
                break;

            case "autoscroll":
                gesture5();
                break;

            case "pad":
                gesture6();
                break;

            case "metronome":
                gesture7();
                break;

            case "pad_autoscroll":
                gesture6();
                gesture5();
                break;

            case "pad_metronome":
                gesture6();
                gesture7();
                break;

            case "autoscroll_metronome":
                gesture5();
                gesture7();
                break;

            case "pad_autoscroll_metronome":
                gesture5();
                gesture6();
                gesture7();
                break;

            case "editsong":
            case "editsongpdf":
            case "changetheme":
            case "autoscale":
            case "transpose":
            case "fullsearch":
            case "randomsong":
            case "abcnotation":
            case "editset":
                FullscreenActivity.whattodo = action;
                openFragment();
                break;

            case "showchords":
                val = preferences.getMyPreferenceBoolean(StageMode.this,"displayChords",true);
                preferences.setMyPreferenceBoolean(StageMode.this,"displayChords",!val);
                refreshAll();
                break;

            case "showcapo":
                val =  preferences.getMyPreferenceBoolean(StageMode.this,"displayCapoChords",true);
                preferences.setMyPreferenceBoolean(StageMode.this,"displayCapoChords",!val);
                refreshAll();
                break;

            case "showlyrics":
                val = preferences.getMyPreferenceBoolean(StageMode.this,"displayLyrics",true);
                preferences.setMyPreferenceBoolean(StageMode.this,"displayLyrics",!val);
                refreshAll();
                break;

            case "highlight":
                displayHighlight(false);
                break;

            case "sticky":
                if (stickyPopUpWindow != null && stickyPopUpWindow.isShowing()) {
                    try {
                        stickyPopUpWindow.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (StaticVariables.mNotes == null || StaticVariables.mNotes.equals("")) {
                        StaticVariables.myToastMessage = getString(R.string.stickynotes) + " - " + getString(R.string.notset);
                        ShowToast.showToast(StageMode.this);
                    } else {
                        displaySticky();
                    }
                }
                break;

            case "speedup":
                increaseAutoScrollSpeed();
                break;

            case "slowdown":
                decreaseAutoScrollSpeed();
                break;

            case "pause":
                StaticVariables.autoscrollispaused = !StaticVariables.autoscrollispaused;
                break;

            case "songmenu":
                if (mDrawerLayout.isDrawerOpen(songmenu)) {
                    closeMyDrawers("song");
                } else {
                    openMyDrawers("song");
                }
                break;

            case "optionmenu":
                if (mDrawerLayout.isDrawerOpen(optionmenu)) {
                    closeMyDrawers("option");
                } else {
                    openMyDrawers("option");
                }
                break;

            case "refreshsong":
                refreshAll();
                break;

            case "addsongtoset":
                PopUpLongSongPressFragment.addtoSet(StageMode.this, preferences);
                break;
        }
    }
    private void showHighlight() {
        doCancelAsyncTask(show_highlight);
        show_highlight = new ShowHighlight();
        try {
            show_highlight.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ShowHighlight extends AsyncTask<Object, Void, String> {

        long highlightcurrtime;
        long highlighttimetohide;
        boolean highlightdonthide;
        final boolean fromautoshow;

        ShowHighlight() {
            fromautoshow = true;
        }

        @Override
        protected void onPreExecute() {
            try {
                if (shouldHighlightsBeShown()) {
                    // Open the highlight note window up again
                    displayHighlight(fromautoshow);

                    // Get the current time
                    highlightcurrtime = System.currentTimeMillis();

                    // Set the time to close the highlighter note
                    int time = preferences.getMyPreferenceInt(StageMode.this,"timeToDisplayHighlighter",0);
                    highlighttimetohide = highlightcurrtime + (time * 1000);

                    if (time == 0) {
                        highlightdonthide = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (shouldHighlightsBeShown() && !highlightdonthide) {
                    while (System.currentTimeMillis() < highlighttimetohide) {
                        // Do nothing
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    if (!highlightdonthide && highlightNotes.getVisibility() == View.VISIBLE) {
                        // If there is a highlight note showing, remove it
                        highlightNotes.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void gesture6() {
        StaticVariables.pad1Playing = PadFunctions.getPad1Status();
        StaticVariables.pad2Playing = PadFunctions.getPad2Status();

        DoVibrate.vibrate(StageMode.this, 50);
        if (StaticVariables.pad1Playing || StaticVariables.pad2Playing) {
            StaticVariables.clickedOnPadStart = false;
            fadeoutPad();
            StaticVariables.padson = false;
        } else if (PadFunctions.isPadValid(StageMode.this, preferences)) {
            StaticVariables.clickedOnPadStart = true;
            preparePad();
        } else {
            FullscreenActivity.whattodo = "page_pad";
            openFragment();
        }
    }

    @Override
    public void prepareSongMenu() {
        doCancelAsyncTask(preparesongmenu_async);
        if (song_list_view!=null) {
            try {
                song_list_view.setFastScrollEnabled(false);
                song_list_view.setScrollingCacheEnabled(false);
                preparesongmenu_async = new PrepareSongMenu();
                preparesongmenu_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ShareSong extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute() {
            try {
                songscrollview.destroyDrawingCache();
                songscrollview.setDrawingCacheEnabled(true);
                songscrollview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
                FullscreenActivity.bmScreen = null;
                FullscreenActivity.bmScreen = songscrollview.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                String title = getString(R.string.exportcurrentsong);
                Intent emailIntent = exportPreparer.exportSong(StageMode.this, preferences,
                        FullscreenActivity.bmScreen, storageAccess, processSong);
                Intent chooser = Intent.createChooser(emailIntent, title);
                startActivity(chooser);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void displayIndex(ArrayList<SongMenuViewItems> songMenuViewItems,
                              SongMenuAdapter songMenuAdapter) {
        LinearLayout indexLayout = findViewById(R.id.side_index);
        if (preferences.getMyPreferenceBoolean(StageMode.this,"songMenuAlphaIndexShow",true)) {
            indexLayout.setVisibility(View.VISIBLE);
        } else {
            indexLayout.setVisibility(View.GONE);
        }
        indexLayout.removeAllViews();
        TextView textView;
        final Map<String,Integer> map = songMenuAdapter.getAlphaIndex(StageMode.this,songMenuViewItems);
        Set<String> setString = map.keySet();
        List<String> indexList = new ArrayList<>(setString);
        for (String index : indexList) {
            textView = (TextView) View.inflate(StageMode.this,
                    R.layout.leftmenu, null);

            textView.setTextSize(preferences.getMyPreferenceFloat(StageMode.this,"songMenuAlphaIndexSize",14.0f));
            int i = (int) preferences.getMyPreferenceFloat(StageMode.this,"songMenuAlphaIndexSize",14.0f) *2;
            textView.setPadding(i,i,i,i);
            textView.setText(index);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView selectedIndex = (TextView) view;
                    try {
                        if (selectedIndex.getText() != null) {
                            String myval = selectedIndex.getText().toString();
                            Object obj = map.get(myval);
                            if (obj!=null) {
                                song_list_view.setSelection((int)obj);
                            }
                            /*
                            int i = map.get(myval);
                            song_list_view.setSelection(i);*/
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            indexLayout.addView(textView);
        }
    }

    @Override
    public void prepareOptionMenu() {
        doCancelAsyncTask(prepareoptionmenu_async);
        prepareoptionmenu_async = new PrepareOptionMenu();
        try {
            prepareoptionmenu_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ShareActivityLog extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                Intent emailIntent = exportPreparer.exportActivityLog(StageMode.this, preferences, storageAccess);
                startActivityForResult(Intent.createChooser(emailIntent, "ActivityLog.xml"), 2222);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void songLongClick() {
        // Rebuild the set list as we've just added a song
        setActions.prepareSetList(StageMode.this,preferences);
        prepareOptionMenu();
        prepareSongMenu();
        closeMyDrawers("song");
    }

    @Override
    public void fixSet() {
        // This is only used for the PresenterMode
    }

    @Override
    public void songShortClick(String clickedfile, String clickedfolder, int i) {
        // Close both drawers
        closeMyDrawers("both");

        // Save our preferences
        saveFileLocation(clickedfile,clickedfolder);

        // Load the song
        loadSong();

        FullscreenActivity.currentSongIndex = i;

        // Scroll to this song in the song menu
        song_list_view.smoothScrollToPosition(i);

        // Initialise the previous and next songs
        findSongInFolders();
    }

    @Override
    public void openSongLongClickAction(String clickedfile, String clickedfolder,int i) {
        // Set the values
        FullscreenActivity.whattodo = "songlongpress";
        StaticVariables.songfilename = clickedfile;
        StaticVariables.whichSongFolder = clickedfolder;
        // Short click the song as well!
        songShortClick(clickedfile,clickedfolder,i);
        openFragment();
    }

    private void loadFileLocation() {
        StaticVariables.songfilename = preferences.getMyPreferenceString(StageMode.this,"songfilename","Welcome to OpenSongApp");
        StaticVariables.whichSongFolder = preferences.getMyPreferenceString(StageMode.this, "whichSongFolder", getString(R.string.mainfoldername));
    }
    private void saveFileLocation(String loc_name, String loc_folder) {
        StaticVariables.songfilename = loc_name;
        StaticVariables.whichSongFolder = loc_folder;
        preferences.setMyPreferenceString(StageMode.this, "songfilename", loc_name);
        preferences.setMyPreferenceString(StageMode.this, "whichSongFolder", loc_folder);
    }

    @Override
    public void removeSongFromSet(int val) {
        // Vibrate to let the user know something happened
        DoVibrate.vibrate(StageMode.this, 50);

        // Take away the menu item
        String tempSong = StaticVariables.mSetList[val];
        StaticVariables.mSetList[val] = "";

        StringBuilder sb = new StringBuilder();
        for (String aMSetList : StaticVariables.mSetList) {
            if (!aMSetList.isEmpty()) {
                sb.append("$**_").append(aMSetList).append("_**$");
            }
        }

        preferences.setMyPreferenceString(StageMode.this,"setCurrent",sb.toString());

        // Tell the user that the song has been removed.
        showToastMessage("\"" + tempSong + "\" "
                + getResources().getString(R.string.removedfromset));

        //Check to see if our set list is still valid
        setActions.prepareSetList(StageMode.this,preferences);
        prepareOptionMenu();

        closeMyDrawers("option");
    }

    @Override
    public void showActionBar() {
        if (preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
            // Make the songscrollview not sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.horizontalscrollview).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) findViewById(R.id.glideimage_HorizontalScrollView).getLayoutParams();
            lp2.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.glideimage_HorizontalScrollView).setLayoutParams(lp2);
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) findViewById(R.id.highlightNotes).getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.highlightNotes).setLayoutParams(lp3);
            //RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.pagebuttons).getLayoutParams();
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, 0);
            //findViewById(R.id.pagebuttons).setLayoutParams(lp4);
            findViewById(R.id.capoInfo).setLayoutParams(lp4);
        } else {
            // Make the songscrollview sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.horizontalscrollview).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) findViewById(R.id.glideimage_HorizontalScrollView).getLayoutParams();
            lp2.addRule(RelativeLayout.BELOW,  ab_toolbar.getId());
            findViewById(R.id.glideimage_HorizontalScrollView).setLayoutParams(lp2);
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) findViewById(R.id.highlightNotes).getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW,  ab_toolbar.getId());
            findViewById(R.id.highlightNotes).setLayoutParams(lp3);
            //RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.pagebuttons).getLayoutParams();
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            //findViewById(R.id.pagebuttons).setLayoutParams(lp4);
            findViewById(R.id.capoInfo).setLayoutParams(lp4);
        }
        toggleActionBar();
    }

    @Override
    public void hideActionBar() {
        if (preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
            // Make the songscrollview not sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.horizontalscrollview).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) findViewById(R.id.glideimage_HorizontalScrollView).getLayoutParams();
            lp2.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.glideimage_HorizontalScrollView).setLayoutParams(lp2);
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) findViewById(R.id.highlightNotes).getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.highlightNotes).setLayoutParams(lp3);
            //RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.pagebuttons).getLayoutParams();
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, 0);
            //findViewById(R.id.pagebuttons).setLayoutParams(lp4);
            findViewById(R.id.capoInfo).setLayoutParams(lp4);
        } else {
            // Make the songscrollview sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.horizontalscrollview).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) findViewById(R.id.glideimage_HorizontalScrollView).getLayoutParams();
            lp2.addRule(RelativeLayout.BELOW,  ab_toolbar.getId());
            findViewById(R.id.glideimage_HorizontalScrollView).setLayoutParams(lp2);
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) findViewById(R.id.highlightNotes).getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW,  ab_toolbar.getId());
            findViewById(R.id.highlightNotes).setLayoutParams(lp3);
            //RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.pagebuttons).getLayoutParams();
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            //findViewById(R.id.pagebuttons).setLayoutParams(lp4);
            findViewById(R.id.capoInfo).setLayoutParams(lp4);
        }
        toggleActionBar();
    }

    @Override
    public void toggleDrawerSwipe() {
        if (preferences.getMyPreferenceBoolean(StageMode.this,"swipeForMenus",true)) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        closeMyDrawers("both");
    }

    @SuppressLint("StaticFieldLeak")
    private class CreatePerformanceView1Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_1 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_1 = new RelativeLayout(StageMode.this);
        long start;

        @Override
        protected void onPreExecute() {
            start = System.currentTimeMillis();
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                // We know how many columns we are using, so lets go for it.
                column1_1 = processSong.preparePerformanceColumnView(StageMode.this);
                songbit = processSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
                boxbit1_1 = processSong.preparePerformanceBoxView(StageMode.this, preferences,lyricsTextColor,
                        lyricsBackgroundColor, FullscreenActivity.padding);

                // Add the song sections...
                for (int x = 0; x < StaticVariables.songSections.length; x++) {
                    float fontsize = processSong.setScaledFontSize(0);
                    LinearLayout sectionview = processSong.songSectionView(StageMode.this, x, fontsize, false,
                            storageAccess, preferences,
                            lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                            lyricsCapoColor, presoFontColor);
                    sectionview.setPadding(0, 0, 0, 0);
                    sectionview.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],lyricsVerseColor,
                            lyricsChorusColor, lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor,lyricsCommentColor,lyricsCustomColor));
                    column1_1.addView(sectionview);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                if (!cancelled) {
                    songscrollview.removeAllViews();
                    LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    if (StaticVariables.thisSongScale.equals("Y")) {
                        llp1_1 = new LinearLayout.LayoutParams(getAvailableWidth(), getAvailableHeight());
                        //llp1_1 = new LinearLayout.LayoutParams(getAvailableWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
                    } else if (StaticVariables.thisSongScale.equals("W")) {
                        llp1_1 = new LinearLayout.LayoutParams(getAvailableWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
                    }
                    llp1_1.setMargins(0, 0, 0, 0);
                    boxbit1_1.setLayoutParams(llp1_1);
                    boxbit1_1.addView(column1_1);
                    songbit.addView(boxbit1_1);
                    songscrollview.addView(songbit);
                    animateInSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void callIntent(String what, Intent i) {
        switch (what) {
            case "web":
                startActivity(i);
                break;
            case "twitter":
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=opensongapp")));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/opensongapp")));
                }
                break;

            case "forum":
                String mailto = "mailto:opensongapp@googlegroups.com";
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));
                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    StaticVariables.myToastMessage = getString(R.string.error);
                    ShowToast.showToast(StageMode.this);
                }
                break;

            case "activity":
                finish();
                startActivity(i);
                break;

            case "openpdf":
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.pdfviewer")));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.pdfviewer")));
                    }
                }
                break;

        }
    }

    @Override
    public void profileWork(String s) {
        switch (s) {
            case "load":
                try {
                    Intent i = profileActions.openProfile(StageMode.this,preferences,storageAccess);
                    this.startActivityForResult(i, StaticVariables.REQUEST_PROFILE_LOAD);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "save":
                try {
                    Intent i = profileActions.saveProfile(StageMode.this,preferences,storageAccess);
                    this.startActivityForResult(i, StaticVariables.REQUEST_PROFILE_SAVE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ShareSet extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                String title = getString(R.string.exportsavedset);
                Intent emailIntent = exportPreparer.exportSet(StageMode.this, preferences, storageAccess);
                Intent chooser = Intent.createChooser(emailIntent, title);
                startActivity(chooser);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void openFragment() {
        // Load the whichSongFolder in case we were browsing elsewhere
        StaticVariables.whichSongFolder = preferences.getMyPreferenceString(StageMode.this,"whichSongFolder",getString(R.string.mainfoldername));

        // Initialise the newFragment
        newFragment = OpenFragment.openFragment(StageMode.this);
        String message = OpenFragment.getMessage(StageMode.this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (newFragment != null && !this.isFinishing()) {
            try {
                ft.add(newFragment,message);
                ft.commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void useCamera() {
        if (ContextCompat.checkSelfPermission(StageMode.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(StageMode.this, new String[]{Manifest.permission.CAMERA},
                    StaticVariables.REQUEST_CAMERA_CODE);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == StaticVariables.REQUEST_CAMERA_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Success, go for it
                startCamera();
            }
        }
    }

    private void startCamera() {
        closeMyDrawers("option");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            Uri photoUri = getImageUri();
            // Continue only if the File was successfully created
            if (photoUri != null) {
                try {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, StaticVariables.REQUEST_CAMERA_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Uri getImageUri() {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", StaticVariables.locale).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            Uri imageUri = FileProvider.getUriForFile(StageMode.this, "OpenSongAppFiles", image);
            // Save a file: path for use with ACTION_VIEW intents
            FullscreenActivity.mCurrentPhotoPath = imageUri.toString();
            return imageUri;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CreatePerformanceView2Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_2 = new LinearLayout(StageMode.this);
        LinearLayout column2_2 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_2 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit2_2 = new RelativeLayout(StageMode.this);

        long start;

        @Override
        protected void onPreExecute() {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            start = System.currentTimeMillis();

            try {
                // We know how many columns we are using, so lets go for it.
                column1_2 = processSong.preparePerformanceColumnView(StageMode.this);
                column2_2 = processSong.preparePerformanceColumnView(StageMode.this);
                songbit = processSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
                boxbit1_2 = processSong.preparePerformanceBoxView(StageMode.this, preferences, lyricsTextColor,
                        lyricsBackgroundColor, FullscreenActivity.padding);
                boxbit2_2 = processSong.preparePerformanceBoxView(StageMode.this, preferences, lyricsTextColor,
                        lyricsBackgroundColor, FullscreenActivity.padding);

                // Add the song sections...
                for (int x = 0; x < StaticVariables.songSections.length; x++) {

                    if (x < FullscreenActivity.halfsplit_section) {
                        float fontsize = processSong.setScaledFontSize(1);
                        LinearLayout sectionview = processSong.songSectionView(StageMode.this, x, fontsize, false,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor);
                        sectionview.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],lyricsVerseColor,
                                lyricsChorusColor, lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor,lyricsCommentColor,lyricsCustomColor));
                        sectionview.setPadding(0, 0, 0, 0);
                        column1_2.addView(sectionview);

                    } else {
                        float fontsize = processSong.setScaledFontSize(2);
                        LinearLayout sectionview2 = processSong.songSectionView(StageMode.this, x, fontsize, false,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor);
                        sectionview2.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],lyricsVerseColor,
                                lyricsChorusColor, lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor,lyricsCommentColor,lyricsCustomColor));
                        sectionview2.setPadding(0, 0, 0, 0);
                        column2_2.addView(sectionview2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                if (!cancelled) {
                    songscrollview.removeAllViews();
                    LinearLayout.LayoutParams llp1_2 = new LinearLayout.LayoutParams((int) (getAvailableWidth() / 2.0f) - getPixelsFromDpi(2), LinearLayout.LayoutParams.WRAP_CONTENT);
                    LinearLayout.LayoutParams llp2_2 = new LinearLayout.LayoutParams((int) (getAvailableWidth() / 2.0f) - getPixelsFromDpi(2), LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_2.setMargins(0, 0, getPixelsFromDpi(4), 0);
                    llp2_2.setMargins(0, 0, 0, 0);
                    boxbit1_2.setLayoutParams(llp1_2);
                    boxbit2_2.setLayoutParams(llp2_2);
                    boxbit1_2.addView(column1_2);
                    boxbit2_2.addView(column2_2);
                    songbit.addView(boxbit1_2);
                    songbit.addView(boxbit2_2);
                    songscrollview.addView(songbit);
                    animateInSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CreatePerformanceView3Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_3 = new LinearLayout(StageMode.this);
        LinearLayout column2_3 = new LinearLayout(StageMode.this);
        LinearLayout column3_3 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_3 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit2_3 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit3_3 = new RelativeLayout(StageMode.this);
        long start;

        @Override
        protected void onPreExecute() {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            start = System.currentTimeMillis();

            try {
                // We know how many columns we are using, so lets go for it.
                column1_3 = processSong.preparePerformanceColumnView(StageMode.this);
                column2_3 = processSong.preparePerformanceColumnView(StageMode.this);
                column3_3 = processSong.preparePerformanceColumnView(StageMode.this);
                songbit = processSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
                boxbit1_3 = processSong.preparePerformanceBoxView(StageMode.this, preferences,lyricsTextColor,
                        lyricsBackgroundColor, FullscreenActivity.padding);
                boxbit2_3 = processSong.preparePerformanceBoxView(StageMode.this, preferences, lyricsTextColor,
                        lyricsBackgroundColor, FullscreenActivity.padding);
                boxbit3_3 = processSong.preparePerformanceBoxView(StageMode.this, preferences, lyricsTextColor,
                        lyricsBackgroundColor, FullscreenActivity.padding);

                // Add the song sections...
                for (int x = 0; x < StaticVariables.songSections.length; x++) {
                    if (x < FullscreenActivity.thirdsplit_section) {
                        float fontsize = processSong.setScaledFontSize(3);
                        LinearLayout sectionview = processSong.songSectionView(StageMode.this, x, fontsize, false,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor);
                        sectionview.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],lyricsVerseColor,
                                lyricsChorusColor, lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor,lyricsCommentColor,lyricsCustomColor));
                        sectionview.setPadding(0, 0, 0, 0);
                        column1_3.addView(sectionview);

                    } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                        float fontsize = processSong.setScaledFontSize(4);
                        LinearLayout sectionview2 = processSong.songSectionView(StageMode.this, x, fontsize, false,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor);
                        sectionview2.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],lyricsVerseColor,
                                lyricsChorusColor, lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor,lyricsCommentColor,lyricsCustomColor));
                        sectionview2.setPadding(0, 0, 0, 0);
                        column2_3.addView(sectionview2);

                    } else {
                        float fontsize = processSong.setScaledFontSize(5);
                        LinearLayout sectionview3 = processSong.songSectionView(StageMode.this, x, fontsize, false,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor);
                        sectionview3.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],lyricsVerseColor,
                                lyricsChorusColor, lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor,lyricsCommentColor,lyricsCustomColor));
                        sectionview3.setPadding(0, 0, 0, 0);
                        column3_3.addView(sectionview3);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                if (!cancelled) {
                    songscrollview.removeAllViews();
                    LinearLayout.LayoutParams llp1_3 = new LinearLayout.LayoutParams((int) (getAvailableWidth() / 3.0f) - getPixelsFromDpi(3), LinearLayout.LayoutParams.WRAP_CONTENT);
                    LinearLayout.LayoutParams llp3_3 = new LinearLayout.LayoutParams((int) (getAvailableWidth() / 3.0f) - getPixelsFromDpi(3), LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_3.setMargins(0, 0, getPixelsFromDpi(4), 0);
                    llp3_3.setMargins(0, 0, 0, 0);
                    boxbit1_3.setLayoutParams(llp1_3);
                    boxbit2_3.setLayoutParams(llp1_3);
                    boxbit3_3.setLayoutParams(llp3_3);
                    boxbit1_3.addView(column1_3);
                    boxbit2_3.addView(column2_3);
                    boxbit3_3.addView(column3_3);
                    songbit.addView(boxbit1_3);
                    songbit.addView(boxbit2_3);
                    songbit.addView(boxbit3_3);
                    songscrollview.addView(songbit);
                    animateInSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void splashScreen() {
        Intent intent = new Intent();
        intent.putExtra("showsplash",true);
        intent.setClass(StageMode.this, BootUpCheck.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadCustomReusable extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... obj) {
            try {
                LoadXML.prepareLoadCustomReusable(StageMode.this, preferences, storageAccess,
                        processSong, FullscreenActivity.customreusabletoload);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // This reopens the choose backgrounds popupFragment
                    // Initialise the newFragment
                    newFragment = PopUpCustomSlideFragment.newInstance();
                    String message = "dialog";
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.add(newFragment,message);

                    if (newFragment != null && !StageMode.this.isFinishing()) {
                        try {
                            ft.commitAllowingStateLoss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //newFragment = PopUpCustomSlideFragment.newInstance();
                    //newFragment.show(getSupportFragmentManager(), "dialog");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void preparePad() {
        backingtrackProgress.setVisibility(View.VISIBLE);
        updateExtraInfoColorsAndSizes("pad");
        doCancelAsyncTask(prepare_pad);
        prepare_pad = new PreparePad();
        try {
            prepare_pad.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class PreparePad extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                StaticVariables.padson = true;
                PadFunctions.getPad1Status();
                PadFunctions.getPad2Status();

                FullscreenActivity.mPlayer1Paused = false;
                FullscreenActivity.mPlayer2Paused = false;

                if (StaticVariables.pad1Playing && !StaticVariables.pad1Fading) {
                    // If mPlayer1 is already playing, set this to fade out and start mPlayer2
                    StaticVariables.pad1Fading = true;
                    StaticVariables.pad2Fading = false;
                    FullscreenActivity.whichPad = 2;
                    StaticVariables.padson = true;

                } else if (StaticVariables.pad2Playing && !StaticVariables.pad2Fading) {
                    // If mPlayer2 is already playing, set this to fade out and start mPlayer1
                    StaticVariables.pad1Fading = false;
                    StaticVariables.pad2Fading = true;
                    StaticVariables.padson = true;
                    FullscreenActivity.whichPad = 1;

                } else {
                    // Else nothing, was playing, so start mPlayer1
                    StaticVariables.pad1Fading = false;
                    StaticVariables.pad2Fading = false;
                    FullscreenActivity.whichPad = 1;
                    StaticVariables.padson = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return FullscreenActivity.whichPad;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(Integer i) {
            try {
                if (!cancelled) {
                    if (StaticVariables.pad1Fading) {
                        StaticVariables.fadeWhichPad = 1;
                        fadeoutPad();
                    }
                    if (StaticVariables.pad2Fading) {
                        StaticVariables.fadeWhichPad = 2;
                        fadeoutPad();
                    }
                    try {
                        playPads(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class PrepareSongView extends AsyncTask<Object, Void, String> {

        long start;
        @Override
        protected void onPreExecute() {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            start = System.currentTimeMillis();
            try {
                mypage.setBackgroundColor(lyricsBackgroundColor);
                songscrollview.setBackgroundColor(lyricsBackgroundColor);

                width_scale = 0f;

                StaticVariables.currentSection = 0;

                testpane.removeAllViews();
                testpane1_2.removeAllViews();
                testpane2_2.removeAllViews();
                testpane1_3.removeAllViews();
                testpane2_3.removeAllViews();
                testpane3_3.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            // Set up the songviews
            try {
                StaticVariables.songSectionsTypes = new String[StaticVariables.songSections.length];
                FullscreenActivity.sectionviews = new LinearLayout[StaticVariables.songSections.length];
                StaticVariables.sectionScaleValue = new float[StaticVariables.songSections.length];
                FullscreenActivity.viewwidth = new int[StaticVariables.songSections.length];
                FullscreenActivity.viewheight = new int[StaticVariables.songSections.length];
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        protected void onPostExecute(String s) {
            //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                if (!cancelled) {
                    // For stage mode, each section gets its own box
                    // For performance mode, all the sections get added into the one box

                    if (column1_1 != null) {
                        column1_1.removeAllViews();
                    }
                    if (column1_2 != null) {
                        column1_2.removeAllViews();
                    }
                    if (column2_2 != null) {
                        column2_2.removeAllViews();
                    }
                    if (column1_3 != null) {
                        column1_3.removeAllViews();
                    }
                    if (column2_3 != null) {
                        column2_3.removeAllViews();
                    }
                    if (column3_3 != null) {
                        column3_3.removeAllViews();
                    }

                    column1_1 = processSong.createLinearLayout(StageMode.this);
                    column1_2 = processSong.createLinearLayout(StageMode.this);
                    column2_2 = processSong.createLinearLayout(StageMode.this);
                    column1_3 = processSong.createLinearLayout(StageMode.this);
                    column2_3 = processSong.createLinearLayout(StageMode.this);
                    column3_3 = processSong.createLinearLayout(StageMode.this);

                    LinearLayout section1_1;
                    LinearLayout section1_2;
                    LinearLayout section2_2;
                    LinearLayout section1_3;
                    LinearLayout section2_3;
                    LinearLayout section3_3;

                    // Go through each section
                    for (int x = 0; x < StaticVariables.songSections.length; x++) {

                        // The single stage mode view
                        final LinearLayout section = processSong.songSectionView(StageMode.this, x, 12.0f, false,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor);
                        section.setClipChildren(false);
                        section.setClipToPadding(false);
                        section.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        FullscreenActivity.viewwidth[x] = section.getMeasuredWidth();
                        FullscreenActivity.viewheight[x] = section.getMeasuredHeight();

                        // The other views for 2 or 3 column mode
                        section1_1 = processSong.songSectionView(StageMode.this, x, 12.0f, false,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor);
                        column1_1.addView(section1_1);

                        if (x < FullscreenActivity.halfsplit_section) {
                            section1_2 = processSong.songSectionView(StageMode.this, x, 12.0f, false,
                                    storageAccess, preferences,
                                    lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                    lyricsCapoColor, presoFontColor);
                            column1_2.addView(section1_2);
                        } else {
                            section2_2 = processSong.songSectionView(StageMode.this, x, 12.0f, false,
                                    storageAccess, preferences,
                                    lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                    lyricsCapoColor, presoFontColor);
                            column2_2.addView(section2_2);
                        }

                        if (x < FullscreenActivity.thirdsplit_section) {
                            section1_3 = processSong.songSectionView(StageMode.this, x, 12.0f, false,
                                    storageAccess, preferences,
                                    lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                    lyricsCapoColor, presoFontColor);
                            column1_3.addView(section1_3);
                        } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                            section2_3 = processSong.songSectionView(StageMode.this, x, 12.0f, false,
                                    storageAccess, preferences,
                                    lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                    lyricsCapoColor, presoFontColor);
                            column2_3.addView(section2_3);
                        } else {
                            section3_3 = processSong.songSectionView(StageMode.this, x, 12.0f, false,
                                    storageAccess, preferences,
                                    lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                    lyricsCapoColor, presoFontColor);
                            column3_3.addView(section3_3);
                        }

                        if (StaticVariables.whichMode.equals("Stage")) {
                            // Stage Mode
                            resizeStageView();
                        }
                    }

                    if (StaticVariables.whichMode.equals("Performance")) {
                        StaticVariables.sectionScaleValue = new float[6];
                        FullscreenActivity.viewwidth = new int[6];
                        FullscreenActivity.viewheight = new int[6];

                        // Performance Mode
                        testpane.setClipChildren(false);
                        testpane.setClipToPadding(false);
                        testpane.addView(column1_1);
                        testpane1_2.setClipChildren(false);
                        testpane1_2.setClipToPadding(false);
                        testpane1_2.addView(column1_2);
                        testpane2_2.setClipChildren(false);
                        testpane2_2.setClipToPadding(false);
                        testpane2_2.addView(column2_2);
                        testpane1_3.setClipChildren(false);
                        testpane1_3.setClipToPadding(false);
                        testpane1_3.addView(column1_3);
                        testpane2_3.setClipChildren(false);
                        testpane2_3.setClipToPadding(false);
                        testpane2_3.addView(column2_3);
                        testpane3_3.setClipChildren(false);
                        testpane3_3.setClipToPadding(false);
                        testpane3_3.addView(column3_3);

                        testpane.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane1_2.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane2_2.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane1_3.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane2_3.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane3_3.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                        testpane.setVisibility(View.INVISIBLE);
                        testpane1_2.setVisibility(View.INVISIBLE);
                        testpane2_2.setVisibility(View.INVISIBLE);
                        testpane1_3.setVisibility(View.INVISIBLE);
                        testpane2_3.setVisibility(View.INVISIBLE);
                        testpane3_3.setVisibility(View.INVISIBLE);
                        resizePerformanceView();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CreateStageView1Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_1 = new LinearLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            try {
                // Only 1 column, but many sections
                column1_1 = processSong.preparePerformanceColumnView(StageMode.this);
                songbit = processSong.prepareStageSongBitView(StageMode.this);

                // Add the song sections...
                for (int x = 0; x < StaticVariables.songSections.length; x++) {
                    float fontsize = processSong.setScaledFontSize(x);
                    LinearLayout sectionview = processSong.songSectionView(StageMode.this, x, fontsize, false,
                            storageAccess, preferences,
                            lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                            lyricsCapoColor, presoFontColor);
                    sectionview.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],lyricsVerseColor,
                            lyricsChorusColor, lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor,lyricsCommentColor,lyricsCustomColor));
                    LinearLayout boxbit = processSong.prepareStageBoxView(StageMode.this, preferences, lyricsTextColor,
                            lyricsBackgroundColor, 0, FullscreenActivity.padding);
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(getAvailableWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp.setMargins(0, 0, 0, getPixelsFromDpi(4));
                    boxbit.setLayoutParams(llp);
                    boxbit.addView(sectionview);
                    column1_1.addView(boxbit);
                    boxbit.setAlpha(0.5f);
                    FullscreenActivity.sectionviews[x] = boxbit;
                    final int finalX = x;
                    FullscreenActivity.sectionviews[x].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectSection(finalX);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    songscrollview.removeAllViews();
                    songbit.addView(column1_1);
                    songscrollview.addView(songbit);
                    if (FullscreenActivity.sectionviews[0] != null) {
                        // Make the first section active (full alpha)
                        try {
                            FullscreenActivity.sectionviews[0].setAlpha(1.0f);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    animateInSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void playPads(int which) {
        doCancelAsyncTask(play_pads);
        play_pads = new PlayPads(which);
        try {
            play_pads.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Player1Prepared implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            String padpan = preferences.getMyPreferenceString(StageMode.this,"padPan","C");
            float padvol = preferences.getMyPreferenceFloat(StageMode.this,"padVol",1.0f);
            StaticVariables.padtime_length = (int) (FullscreenActivity.mPlayer1.getDuration() / 1000.0f);
            FullscreenActivity.mPlayer1.setLooping(PadFunctions.getLoop());
            FullscreenActivity.mPlayer1.setVolume(PadFunctions.getVol(padpan,padvol,0), PadFunctions.getVol(padpan,padvol,1));
            FullscreenActivity.mPlayer1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!PadFunctions.getLoop()) {
                        Log.d("StageMode", "Reached end and not looping");

                    } else {
                        Log.d("StageMode", "Reached end but looping");
                    }
                }
            });
            String text = TimeTools.timeFormatFixer(StaticVariables.padtime_length);
            try {
                updateExtraInfoColorsAndSizes("pad");
                padcurrentTime_TextView.setText(getString(R.string.zerotime));
                padtotalTime_TextView.setText(text);

            } catch (Exception e) {
                e.printStackTrace(); // If called from doInBackground()
            }
            FullscreenActivity.mPlayer1.start();
            StaticVariables.padson = true;
            dopadProgressTime.post(onEverySecond);
        }
    }

    // Uri to provide to Android Beam
    //@SuppressLint("NewApi")
    private class FileUriCallback implements NfcAdapter.CreateBeamUrisCallback {
        FileUriCallback() {
        }

        private final Uri[] mFileUris = new Uri[1]; // Send one at a time

        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            mFileUris[0] = storageAccess.getUriForItem(StageMode.this, preferences, "Songs",
                    StaticVariables.whichSongFolder, StaticVariables.songfilename);
            return mFileUris;
        }
    }

    private void preparePadProgress() {
        if (StaticVariables.padson) {
            dopadProgressTime.post(padprogressTimeRunnable);
            dopadProgressTime.postDelayed(onEverySecond, 1000);
        }
    }

    private class Player2Prepared implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            StaticVariables.padtime_length = (int) (FullscreenActivity.mPlayer2.getDuration() / 1000.0f);
            String padpan = preferences.getMyPreferenceString(StageMode.this,"padPan","C");
            float padvol = preferences.getMyPreferenceFloat(StageMode.this,"padVol",1.0f);
            FullscreenActivity.mPlayer2.setLooping(PadFunctions.getLoop());
            FullscreenActivity.mPlayer2.setVolume(PadFunctions.getVol(padpan,padvol,0), PadFunctions.getVol(padpan,padvol,1));
            FullscreenActivity.mPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!PadFunctions.getLoop()) {
                        Log.d("StageMode", "Reached end and not looping");

                    } else {
                        Log.d("StageMode", "Reached end but looping");
                    }
                }
            });
            String text = TimeTools.timeFormatFixer(StaticVariables.padtime_length);
            try {
                updateExtraInfoColorsAndSizes("pad");
                padcurrentTime_TextView.setText(getString(R.string.zerotime));
                padtotalTime_TextView.setText(text);
            } catch (Exception e) {
                e.printStackTrace(); // If called from doInBackground()
            }
            FullscreenActivity.mPlayer2.start();
            StaticVariables.padson = true;
            dopadProgressTime.post(onEverySecond);
        }
    }

    @Override
    public void fadeoutPad() {
        if ((StaticVariables.pad1Playing && !StaticVariables.pad1Fading) && !StaticVariables.pad2Playing) {
            // Pad 1 is the one that will be faded
            StaticVariables.fadeWhichPad = 1;
        } else if ((StaticVariables.pad2Playing && !StaticVariables.pad2Fading) && !StaticVariables.pad1Playing) {
            // Pad 2 is the one that will be faded
            StaticVariables.fadeWhichPad = 2;
        } else if (StaticVariables.pad1Playing && StaticVariables.pad1Fading &&
                StaticVariables.pad2Playing && StaticVariables.pad2Fading) {
            // Both pads are being faded, so do nothing
            StaticVariables.fadeWhichPad = -1;
        } else {
            // Both pads need faded, or it doesn't matter either way
            StaticVariables.fadeWhichPad = 0;
        }
        String padpan = preferences.getMyPreferenceString(StageMode.this,"padPan","C");
        float padvol = preferences.getMyPreferenceFloat(StageMode.this,"padVol",1.0f);

        switch (StaticVariables.fadeWhichPad) {

            case 1:
                // mPlayer1 is playing, so fade it out.
                doCancelAsyncTask(fadeout_media1);
                updateExtraInfoColorsAndSizes("pad");
                padcurrentTime_TextView.setText(getString(R.string.zerotime));
                fadeout_media1 = new FadeoutMediaPlayer(padpan, padvol,1,preferences.getMyPreferenceInt(StageMode.this,"padCrossFadeTime",8000));
                fadeout_media1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;

            case 2:
                // mPlayer2 is playing, so fade it out.
                doCancelAsyncTask(fadeout_media2);
                updateExtraInfoColorsAndSizes("pad");
                padcurrentTime_TextView.setText(getString(R.string.zerotime));
                fadeout_media2 = new FadeoutMediaPlayer(padpan, padvol, 2,preferences.getMyPreferenceInt(StageMode.this,"padCrossFadeTime",8000));
                fadeout_media2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;

            case 0:
                // Fade both pads
                if (StaticVariables.pad1Playing) {
                    // mPlayer1 is playing, so fade it out.
                    doCancelAsyncTask(fadeout_media1);
                    updateExtraInfoColorsAndSizes("pad");
                    padcurrentTime_TextView.setText(getString(R.string.zerotime));
                    fadeout_media1 = new FadeoutMediaPlayer(padpan,padvol, 1,preferences.getMyPreferenceInt(StageMode.this,"padCrossFadeTime",8000));
                    fadeout_media1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if (StaticVariables.pad2Playing) {
                    // mPlayer2 is playing, so fade it out.
                    doCancelAsyncTask(fadeout_media2);
                    updateExtraInfoColorsAndSizes("pad");
                    padcurrentTime_TextView.setText(getString(R.string.zerotime));
                    fadeout_media2 = new FadeoutMediaPlayer(padpan,padvol, 2, preferences.getMyPreferenceInt(StageMode.this,"padCrossFadeTime",8000));
                    fadeout_media2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
        }

        // Set a runnable to check for the pads on or off to hide the player progress
        handle.postDelayed(padoncheck, 13000); // Cross fade has finished
    }

    @Override
    public void killPad() {

        PadFunctions.getPad1Status();
        PadFunctions.getPad2Status();

        switch (FullscreenActivity.whichPad) {
            case 1:
                if (StaticVariables.pad1Playing) {
                    try {
                        FullscreenActivity.mPlayer1.stop();
                        FullscreenActivity.mPlayer1.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 2:
                if (StaticVariables.pad2Playing) {
                    try {
                        FullscreenActivity.mPlayer2.stop();
                        FullscreenActivity.mPlayer2.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 0:
            default:
                if (StaticVariables.pad1Playing) {
                    try {
                        FullscreenActivity.mPlayer1.stop();
                        FullscreenActivity.mPlayer1.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (StaticVariables.pad2Playing) {
                    try {
                        FullscreenActivity.mPlayer2.stop();
                        FullscreenActivity.mPlayer2.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        PadFunctions.getPad1Status();
        PadFunctions.getPad2Status();
        getPadsOnStatus();
        updateExtraInfoColorsAndSizes("pad");
        padcurrentTime_TextView.setText(getString(R.string.zerotime));
    }

    @Override
    public void prepareLearnAutoScroll() {
        StaticVariables.learnPreDelay = false;
        StaticVariables.learnSongLength = false;
        updateExtraInfoColorsAndSizes("autoscroll");
        learnAutoScroll.setVisibility(View.VISIBLE);
        learnAutoScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLearnAutoScroll();
            }
        });
        String s = getString(R.string.autoscroll_time) + "\n" + getString(R.string.start);
        //learnAutoScroll_TextView.setTextSize(10.0f);
        learnAutoScroll_TextView.setText(s);
        learnAutoScrollTime_TextView.setText(TimeTools.timeFormatFixer(0));
    }

    private void startLearnAutoScroll() {
        StaticVariables.learnPreDelay = true;
        StaticVariables.learnSongLength = false;
        learnAutoScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLearnedPreDelayValue();
            }
        });
        String s = getString(R.string.autoscroll_time) + "\n" + getString(R.string.save);
        learnAutoScroll_TextView.setText(s);
        LearnAutoScroll mtask_learnautoscroll = new LearnAutoScroll();
        mtask_learnautoscroll.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private void getLearnedPreDelayValue() {
        int time = (int) (FullscreenActivity.time_passed - FullscreenActivity.time_start)/1000;
        if (time<0) {
            time = 0;
        }
        StaticVariables.learnSongLength = true;
        StaticVariables.learnPreDelay = false;
        StaticVariables.mPreDelay = time+"";
        String s = getString(R.string.edit_song_duration) + "\n" + getString(R.string.save);
        learnAutoScroll_TextView.setText(s);
        learnAutoScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLearnedSongLengthValue();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class AddSlideToSet extends AsyncTask<Object, Void, String> {
        CustomSlide customSlide;

        @Override
        protected void onPreExecute() {
            customSlide = new CustomSlide();
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Add the slide
            try {
                customSlide.addCustomSlide(StageMode.this, preferences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Tell the user that the song has been added.
                    showToastMessage("\"" + FullscreenActivity.customslide_title + "\" " + getResources().getString(R.string.addedtoset));

                    // Vibrate to let the user know something happened
                    DoVibrate.vibrate(StageMode.this, 50);

                    prepareOptionMenu();
                    closeMyDrawers("option_delayed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class LearnAutoScroll extends AsyncTask<String, Integer, String> {

        int time;

        @Override
        protected void onPreExecute() {
            try {
                FullscreenActivity.time_start = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                while (StaticVariables.learnPreDelay || StaticVariables.learnSongLength) {
                    FullscreenActivity.time_passed = System.currentTimeMillis();

                    long starttime = System.currentTimeMillis();
                    long currtime = System.currentTimeMillis();
                    while ((currtime - starttime) < 1000) {
                        currtime = System.currentTimeMillis();
                    }
                    time = (int) (FullscreenActivity.time_passed - FullscreenActivity.time_start)/1000;
                    if (time>28 && StaticVariables.learnPreDelay) {
                        publishProgress(-100);
                    } else {
                        publishProgress(time);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "dummy";
        }

        @Override
        protected void onProgressUpdate(Integer... intg) {
            if (time==-100) {
                // We've exceed the allowed predelay length, so save this value as it is max
                getLearnedPreDelayValue();
            } else {
                // Update the timer
                AutoScrollFunctions.LearnTimeRunnable runnable = new AutoScrollFunctions.LearnTimeRunnable(learnAutoScrollTime_TextView);
                AutoScrollFunctions.doautoScrollLearn.post(runnable);
            }
        }

        @Override
        protected void onPostExecute(String dummy) {
            try {
                if (!cancelled) {
                    StaticVariables.learnPreDelay = false;
                    StaticVariables.learnSongLength = false;
                    learnAutoScroll.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean cancelled = false;
        @Override
        public void onCancelled() {
            StaticVariables.learnPreDelay = false;
            StaticVariables.learnSongLength = false;
            cancelled = true;
        }
    }

    @Override
    public void startAutoScroll() {
        StaticVariables.clickedOnAutoScrollStart = true;
        updateExtraInfoColorsAndSizes("autoscroll");
        currentTime_TextView.setText(getString(R.string.zerotime));
        AutoScrollFunctions.getMultiPagePDFValues();  // This splits the time for multiple pages
        totalTime_TextView.setText(TimeTools.timeFormatFixer(StaticVariables.autoScrollDuration));
        playbackProgress.setVisibility(View.VISIBLE);
        doCancelAsyncTask(mtask_autoscroll_music);
        doCancelAsyncTask(get_scrollheight);
        StaticVariables.isautoscrolling = true;
        StaticVariables.pauseautoscroll = true;
        FullscreenActivity.isManualDragging = false;
        FullscreenActivity.wasscrolling = false;
        get_scrollheight = new GetScrollHeight();
        //FullscreenActivity.refWatcher.watch(get_scrollheight);
        try {
            get_scrollheight.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class GetScrollHeight extends AsyncTask<Object, Integer, String> {
        boolean viewdrawn = false;
        boolean ready = false;

        boolean cancelled = false;
        @Override
        public void onCancelled() {
            cancelled = true;
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (!cancelled) {
                    StaticVariables.scrollpageHeight = 0;
                    FullscreenActivity.newPosFloat = 0.0f;
                    long t1 = System.currentTimeMillis();
                    long t2 = t1 + 5000;

                    while (!ready) {
                        long ct = System.currentTimeMillis();
                        publishProgress(1);
                        if (ct > t2 || viewdrawn) {
                            ready = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... intg) {
            try {
                if (!cancelled) {
                    if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                        StaticVariables.scrollpageHeight = glideimage_ScrollView.getChildAt(0).getMeasuredHeight() -
                                glideimage_ScrollView.getHeight();

                    } else {
                        if (songscrollview.getChildAt(0) != null) {
                            StaticVariables.scrollpageHeight = songscrollview.getChildAt(0).getMeasuredHeight() -
                                    songscrollview.getHeight();
                        }

                    }
                    if (StaticVariables.scrollpageHeight > 0) {
                        viewdrawn = true;
                        ready = true;
                        cancelled = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                if (viewdrawn) {
                    if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                        AutoScrollFunctions.getAutoScrollValues(StageMode.this, preferences, glideimage_ScrollView, mypage, ab_toolbar);
                    } else {
                        AutoScrollFunctions.getAutoScrollValues(StageMode.this, preferences, songscrollview, mypage, ab_toolbar);
                    }
                    mtask_autoscroll_music = new AutoScrollMusic();
                    try {
                        mtask_autoscroll_music.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class AutoScrollMusic extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            try {
                StaticVariables.autoscroll_modifier = 0;
                StaticVariables.autoscrollispaused = false;
                FullscreenActivity.time_start = System.currentTimeMillis();
                if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                    glideimage_ScrollView.scrollTo(0,0);
                } else {
                    songscrollview.scrollTo(0, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Send WiFiP2P intent
            if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
                try {
                    sendAutoscrollTriggerToConnected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                while (StaticVariables.isautoscrolling) {
                    FullscreenActivity.time_passed = System.currentTimeMillis();
                    boolean doscroll = ((FullscreenActivity.time_passed - FullscreenActivity.time_start) / 1000) >= StaticVariables.autoScrollDelay;
                    if (doscroll) {
                        publishProgress(1);
                    }
                    // don't scroll first time
                    if (!StaticVariables.pauseautoscroll) {
                        AutoScrollFunctions.ProgressTimeRunnable runnable = new AutoScrollFunctions.ProgressTimeRunnable(StageMode.this,preferences,currentTime_TextView, totalTime_TextView, timeSeparator_TextView);
                        AutoScrollFunctions.doProgressTime.post(runnable);
                        if (doscroll) {
                            AutoScrollFunctions.AutoScrollRunnable runnable2;
                            if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                                runnable2 = new AutoScrollFunctions.AutoScrollRunnable(glideimage_ScrollView);
                            } else {
                                runnable2 = new AutoScrollFunctions.AutoScrollRunnable(songscrollview);
                            }
                            AutoScrollFunctions.doautoScroll.post(runnable2);
                        }
                    } else {
                        StaticVariables.pauseautoscroll = false;
                    }
                    if (doscroll) {
                        if (FullscreenActivity.newPosFloat >= StaticVariables.scrollpageHeight) {
                            StaticVariables.autoscrollispaused = false;
                            StaticVariables.isautoscrolling = false;
                        }
                    }
                    long starttime = System.currentTimeMillis();
                    long currtime = System.currentTimeMillis();
                    while ((currtime - starttime) < StaticVariables.autoscroll_pause_time) {
                        currtime = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "dummy";
        }

        @Override
        protected void onProgressUpdate(Integer... intg) {
            try {
                if (!FullscreenActivity.wasscrolling && !StaticVariables.autoscrollispaused) {
                    if (FullscreenActivity.newPosFloat + FullscreenActivity.autoscroll_pixels + StaticVariables.autoscroll_modifier > 0) {
                        FullscreenActivity.newPosFloat = FullscreenActivity.newPosFloat + FullscreenActivity.autoscroll_pixels + StaticVariables.autoscroll_modifier;
                    } else {
                        FullscreenActivity.newPosFloat = FullscreenActivity.newPosFloat + FullscreenActivity.autoscroll_pixels;
                    }
                } else {
                    if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                        FullscreenActivity.newPosFloat = glideimage_ScrollView.getChildAt(0).getScrollY();
                    } else {
                        FullscreenActivity.newPosFloat = songscrollview.getScrollY();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String dummy) {
            try {
                if (!cancelled) {
                    if (!StaticVariables.isautoscrolling) {
                        StaticVariables.pauseautoscroll = false;
                    } else {
                        StaticVariables.isautoscrolling = false;
                        StaticVariables.pauseautoscroll = true;
                    }
                    doCancelAsyncTask(mtask_autoscroll_music);
                    if (FullscreenActivity.isPDF && (FullscreenActivity.pdfPageCurrent+1)<FullscreenActivity.pdfPageCount) {
                        pdfCanContinueScrolling = true;
                        goToNextItem();
                    } else {
                        pdfCanContinueScrolling = false;
                        updateExtraInfoColorsAndSizes("autoscroll");
                        playbackProgress.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean cancelled = false;
        @Override
        public void onCancelled() {
            StaticVariables.isautoscrolling = false;
            StaticVariables.pauseautoscroll = true;
            cancelled = true;
            doCancelAsyncTask(mtask_autoscroll_music);
        }
    }

    @Override
    public void stopAutoScroll() {
        try {
            updateExtraInfoColorsAndSizes("autoscroll");
            playbackProgress.setVisibility(View.GONE);
            doCancelAsyncTask(mtask_autoscroll_music);
            StaticVariables.isautoscrolling = false;
            currentTime_TextView.setText(getString(R.string.zerotime));
            // Send WiFiP2P intent
            if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
                try {
                    sendAutoscrollTriggerToConnected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopMetronome() {
        if (StaticVariables.metronomeonoff.equals("on")) {
            // Stop it
            Metronome.startstopMetronome(StageMode.this, StageMode.this,
                    preferences.getMyPreferenceBoolean(StageMode.this,"metronomeShowVisual",false),
                    defmetronomecolor, preferences.getMyPreferenceString(StageMode.this,"metronomePan","C"),
                    preferences.getMyPreferenceFloat(StageMode.this,"metronomeVol",0.5f),
                    preferences.getMyPreferenceInt(StageMode.this,"metronomeLength",0));
        }
    }

    private boolean checkCanScrollDown() {
        boolean showscrolldown = false;
        if (StaticVariables.whichMode!=null && StaticVariables.whichMode.equals("Stage")) {
            if (StaticVariables.currentSection>-1 && StaticVariables.songSections!=null) {
                showscrolldown = StaticVariables.currentSection < StaticVariables.songSections.length - 1;
            }
        } else {
            int height;
            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                try {
                    height = glideimage_ScrollView.getChildAt(0).getMeasuredHeight() - glideimage_ScrollView.getHeight();
                } catch (Exception e) {
                    height = 0;
                }
                if (glideimage_ScrollView!=null) {
                    FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY();
                    showscrolldown = height > glideimage_ScrollView.getScrollY() && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            } else {
                try {
                    height = songscrollview.getChildAt(0).getMeasuredHeight() - songscrollview.getHeight();
                } catch (Exception e) {
                    height = 0;
                }
                if (songscrollview != null) {
                    FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
                    showscrolldown = height > songscrollview.getScrollY() && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            }
        }
        // Decide if the down arrow should be displayed.
        return showscrolldown && preferences.getMyPreferenceBoolean(StageMode.this,"PageButtonShowScroll",true);
    }

    private boolean checkCanScrollUp() {
        boolean showscrollup = false;
        if (StaticVariables.whichMode!=null && StaticVariables.whichMode.equals("Stage")) {
            showscrollup = StaticVariables.currentSection > 0;
        } else {
            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                if (glideimage_ScrollView!=null) {
                    FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY();
                    showscrollup = glideimage_ScrollView.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            } else {
                if (songscrollview != null) {
                    FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
                    showscrollup = songscrollview.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            }
        }
        // Decide if the up arrow should be displayed.
        return showscrollup && preferences.getMyPreferenceBoolean(StageMode.this,"PageButtonShowScroll",true);
    }

    private void scrollMenu(String direction) {
        if (direction.equals("up")) {
            song_list_view.smoothScrollBy((int) (-0.8f * songmenu.getHeight()), 1600);
        } else {
            song_list_view.smoothScrollBy((int) (+0.8f * songmenu.getHeight()), 1600);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DoMoveInSet extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Get the appropriate song
            try {
                if (StaticVariables.mSetList != null && StaticVariables.indexSongInSet > -1 &&
                        StaticVariables.mSetList.length > StaticVariables.indexSongInSet) {
                    FullscreenActivity.linkclicked = StaticVariables.mSetList[StaticVariables.indexSongInSet];
                    StaticVariables.whatsongforsetwork = FullscreenActivity.linkclicked;
                } else {
                    FullscreenActivity.linkclicked = "";
                    StaticVariables.whatsongforsetwork = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                // Get the next set positions and song
                if (!cancelled) {
                    setActions.doMoveInSet(StageMode.this, preferences);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // This bit listens for long key presses (disables the menu long press action)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // AirTurn pedals don't do long press, but instead autorepeat.  To deal with, count onKeyDown
        // If the app detects more than a set number (reset when onKeyUp/onLongPress) it triggers onLongPress

        keyRepeatCount++;
        if (preferences.getMyPreferenceBoolean(StageMode.this,"airTurnMode",false) && keyRepeatCount>preferences.getMyPreferenceInt(StageMode.this,"keyRepeatCount",20)) {
            keyRepeatCount = 0;
            shortKeyPress = false;
            longKeyPress = true;
            doLongKeyPressAction(keyCode);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            // Open up the song search intent instead of bringing up the keyboard
            shortKeyPress = !longKeyPress;
            return true;
        }

        if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal1Code",21) ||
                keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal2Code",22) ||
                keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal3Code",19) ||
                keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal4Code",20) ||
                keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal5Code",92) ||
                keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal6Code",93)) {
            event.startTracking();
            shortKeyPress = !longKeyPress;

            return true;
        }
        return super.onKeyDown(keyCode, event);
        //return false;
    }

    private boolean justSong(Context c) {
        boolean isallowed = true;
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF || !FullscreenActivity.isSong) {
            showToastMessage(c.getResources().getString(R.string.not_allowed));
            isallowed = false;
        }
        return isallowed;
    }

    @Override
    public void changePDFPage(int page, String direction) {
        FullscreenActivity.whichDirection = direction;
        FullscreenActivity.pdfPageCurrent = page;
        loadSong();
    }

    // The page action gestures stuff is below
    private boolean oktoregistergesture() {

        boolean oktogo = false;

        if (!FullscreenActivity.pressing_button  // Button pressing
                && !setButton.isPressed() && !padButton.isPressed() && !autoscrollButton.isPressed()
                && !metronomeButton.isPressed() && !extraButton.isPressed() && !chordButton.isPressed()
                && !linkButton.isPressed() && !stickyButton.isPressed() && !notationButton.isPressed()
                && !highlightButton.isPressed() && !pageselectButton.isPressed()
                && !customButton.isPressed() && !custom1Button.isPressed() && !custom2Button.isPressed()
                && !custom3Button.isPressed() && !custom4Button.isPressed() && !linkButton_ungrouped.isPressed()
                && !chordButton_ungrouped.isPressed() && !pageselectButton_ungrouped.isPressed()
                && !stickyButton_ungrouped.isPressed() && !notationButton_ungrouped.isPressed()
                && !highlightButton_ungrouped.isPressed()
                && !custom1Button_ungrouped.isPressed() && !custom2Button_ungrouped.isPressed()
                && !custom3Button_ungrouped.isPressed() && !custom4Button_ungrouped.isPressed()
                && !scrollDownButton.isPressed() && !scrollUpButton.isPressed()
                && !mDrawerLayout.isDrawerOpen(songmenu) && !mDrawerLayout.isDrawerVisible(songmenu)
                && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerVisible(optionmenu)) {
            oktogo = true;
        }

        return oktogo;
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadSongAsync extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            StaticVariables.myToastMessage = "";
            try {
                FullscreenActivity.scalingfiguredout = false;
                sectionpresented = false;

                try {
                    LoadXML.loadXML(StageMode.this, preferences, storageAccess, processSong);
                } catch (Exception e) {
                    Log.d("StageMode", "Error loading song:" + StaticVariables.songfilename);
                }

                // If we are in a set, try to get the appropriate indexes

                setActions.getSongForSetWork(StageMode.this);
                setActions.indexSongInSet();

                if (StaticVariables.mLyrics != null) {
                    FullscreenActivity.myLyrics = StaticVariables.mLyrics;
                } else {
                    FullscreenActivity.myLyrics = "";
                }

                // Clear the old headings (presention order looks for these)
                FullscreenActivity.foundSongSections_heading = new ArrayList<>();

                if (FullscreenActivity.isSong) {

                    // Check the chord format
                    try {
                        transpose.checkChordFormat(StageMode.this,preferences);
                        if (preferences.getMyPreferenceBoolean(StageMode.this,"chordFormatUsePreferred",true)) {
                            StaticVariables.detectedChordFormat = preferences.getMyPreferenceInt(StageMode.this,"chordFormat",1);
                        }
                    } catch (Exception e) {
                        Log.d("StageMode", "Error checking the chord format");
                    }

                    // Sort song formatting
                    // 1. Sort multiline verse/chord formats
                    FullscreenActivity.myLyrics = processSong.fixMultiLineFormat(StageMode.this, preferences, FullscreenActivity.myLyrics);

                    // If we want info on the next song in the set, add it as a comment line
                    processSong.addExtraInfo(StageMode.this, storageAccess, preferences);

                    // Decide if the pad, metronome and autoscroll are good to go
                    //StaticVariables.padok = PadFunctions.isPadValid(StageMode.this, preferences);
                    StaticVariables.metronomeok = Metronome.isMetronomeValid();
                    StaticVariables.autoscrollok = processSong.isAutoScrollValid(StageMode.this,preferences);

                    // 2. Split the song into sections
                    StaticVariables.songSections = processSong.splitSongIntoSections(StageMode.this, preferences, FullscreenActivity.myLyrics);

                    // 3. Put the song into presentation order if required
                    if (preferences.getMyPreferenceBoolean(StageMode.this,"usePresentationOrder",false) &&
                            !StaticVariables.mPresentation.equals("")) {
                        StaticVariables.songSections = processSong.matchPresentationOrder(StageMode.this, preferences, StaticVariables.songSections);
                    }

                    StaticVariables.songSections = processSong.splitLaterSplits(StageMode.this, preferences, StaticVariables.songSections);

                    // 4. Get the section headings/types (may have changed after presentationorder
                    StaticVariables.songSectionsLabels = new String[StaticVariables.songSections.length];
                    StaticVariables.songSectionsTypes = new String[StaticVariables.songSections.length];
                    for (int sl = 0; sl < StaticVariables.songSections.length; sl++) {
                        StaticVariables.songSectionsLabels[sl] = processSong.getSectionHeadings(StaticVariables.songSections[sl]);
                    }

                    // We need to split each section into string arrays by line
                    StaticVariables.sectionContents = new String[StaticVariables.songSections.length][];
                    for (int x = 0; x < StaticVariables.songSections.length; x++) {
                        StaticVariables.sectionContents[x] = StaticVariables.songSections[x].split("\n");
                    }

                    // Determine what each line type is
                    // Copy the array of sectionContents into sectionLineTypes
                    // Then we'll replace the content with the line type
                    // This keeps the array sizes the same simply
                    StaticVariables.sectionLineTypes = new String[StaticVariables.sectionContents.length][];
                    for (int x = 0; x < StaticVariables.sectionLineTypes.length; x++) {
                        StaticVariables.sectionLineTypes[x] = new String[StaticVariables.sectionContents[x].length];
                        for (int y = 0; y < StaticVariables.sectionLineTypes[x].length; y++) {
                            StaticVariables.sectionLineTypes[x][y] = processSong.determineLineTypes(StaticVariables.sectionContents[x][y], StageMode.this);
                            if (StaticVariables.sectionContents[x][y].length() > 0 && (StaticVariables.sectionContents[x][y].indexOf(" ") == 0 ||
                                    StaticVariables.sectionContents[x][y].indexOf(".") == 0 || StaticVariables.sectionContents[x][y].indexOf(";") == 0)) {
                                StaticVariables.sectionContents[x][y] = StaticVariables.sectionContents[x][y].substring(1);
                            }
                        }
                    }

                    if (StaticVariables.whichMode.equals("Performance")) {
                        // Put the song back together for checking for splitpoints
                        processSong.rebuildParsedLyrics(StaticVariables.songSections.length);
                        FullscreenActivity.numrowstowrite = FullscreenActivity.myParsedLyrics.length;

                        // Look for song split points if the lyrics are long enough
                        if (FullscreenActivity.numrowstowrite > 1) {
                            try {
                                processSong.lookForSplitPoints();
                            } catch (Exception e) {
                                Log.d("StageMode","Split point not worth it");
                            }
                        } else {
                            FullscreenActivity.splitpoint = 0;
                            FullscreenActivity.halfsplit_section = 0;
                            FullscreenActivity.thirdsplitpoint = 0;
                            FullscreenActivity.thirdsplit_section = 0;
                            FullscreenActivity.twothirdsplitpoint = 0;
                            FullscreenActivity.twothirdsplit_section = 0;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "done";
        }

        private boolean cancelled = false;

        @Override
        protected void onCancelled() {
            FullscreenActivity.alreadyloading = false;
            cancelled = true;
        }

        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // If we have changed folders, redraw the song menu
                    if (menuFolder_TextView.getText() != null) {
                        if (menuFolder_TextView.getText().toString().equals(StaticVariables.whichSongFolder)) {
                            // Just move to the correct song
                            indexOfSongInMenu();

                        } else {
                            prepareSongMenu();
                        }
                    }

                    // Fix the page flags
                    setWindowFlags();
                    setWindowFlagsAdvanced();

                    // Show the ActionBar
                    try {
                        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (ab != null) {
                        ab.show();
                    }

                    if (preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
                        try {
                            delayactionBarHide.postDelayed(hideActionBarRunnable, 1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Any errors to show?
                    if (!StaticVariables.myToastMessage.equals("")) {
                        ShowToast.showToast(StageMode.this);
                    }

                    // If pads were already playing (previous song), start them up again if wanted
                    // Don't redo this if the orientation has changed (causing a reload)
                    // Stop restarting the pads if changing portrait/landscape
                    // Only play if this isn't called by an orientation change
                    if (!FullscreenActivity.orientationchanged && FullscreenActivity.isSong &&
                            (StaticVariables.padson ||
                                    (preferences.getMyPreferenceBoolean(StageMode.this,"padAutoStart",false) &&
                                            StaticVariables.clickedOnPadStart))) {
                        preparePad();
                    }

                    // Now, reset the orientation.
                    FullscreenActivity.orientationchanged = false;

                    // Get the current orientation
                    FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

                    // Put the title of the song in the taskbar
                    songtitle_ab.setText(processSong.getSongTitle());
                    songkey_ab.setText(processSong.getSongKey());
                    songauthor_ab.setText(processSong.getSongAuthor());
                    songcapo_ab.setText("");

                    //Determine file type
                    storageAccess.determineFileTypeByExtension();

                    if (FullscreenActivity.isPDF) {
                        loadPDF();

                    } else if (FullscreenActivity.isImage) {
                        loadImage();

                    } else if (FullscreenActivity.isSong) {
                        //Prepare the song views
                        prepareView();
                    }

                    // Automatically stop the metronome if it is still on
                    if (StaticVariables.metronomeonoff.equals("on")) {
                        // Stop it
                        Metronome.startstopMetronome(StageMode.this, StageMode.this,
                                preferences.getMyPreferenceBoolean(StageMode.this,"metronomeShowVisual",false),
                                defmetronomecolor, preferences.getMyPreferenceString(StageMode.this,"metronomePan","C"),
                                preferences.getMyPreferenceFloat(StageMode.this,"metronomeVol",0.5f),
                                preferences.getMyPreferenceInt(StageMode.this,"metronomeLength",0));
                    }
                    // Start it again with the new values if we chose to
                    if (preferences.getMyPreferenceBoolean(StageMode.this,"metronomeAutoStart",false) &&
                            StaticVariables.clickedOnMetronomeStart &&
                            !FullscreenActivity.isPDF) {
                        gesture7();
                    }

                    // Decide if we have loaded a song in the current set
                    fixSetActionButtons();

                    // If the user has shown the 'Welcome to OpenSongApp' file, and their song lists are empty,
                    // open the find new songs menu
                    if (StaticVariables.mTitle.equals("Welcome to OpenSongApp") &&
                            sqLiteHelper.getSongsCount(StageMode.this)<1) {
                        StaticVariables.whichOptionMenu = "FIND";
                        prepareOptionMenu();
                        Handler find = new Handler();
                        find.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                openMyDrawers("option");
                            }
                        }, 2000);
                    }

                    // Send the midi data if we can
                    if (preferences.getMyPreferenceBoolean(StageMode.this,"midiSendAuto",false)) {
                        sendMidi();
                    }

                    // Send WiFiP2P intent
                    if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
                        try {
                            sendSongXMLToConnected();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // If autoshowing sticky notes as a popup
                    if (preferences.getMyPreferenceString(StageMode.this,"stickyAutoDisplay","F").equals("F") && !StaticVariables.mNotes.equals("")) {
                        try {
                            showSticky();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // If we have created, or converted a song format (e.g from OnSong or ChordPro), rebuild the database
                    // or pull up the edit screen
                    if (FullscreenActivity.needtoeditsong) {

                        // This line below is now reset in the edit song window
                        //FullscreenActivity.needtoeditsong = false;

                        FullscreenActivity.whattodo = "editsong";
                        FullscreenActivity.alreadyloading = false;
                        FullscreenActivity.needtorefreshsongmenu = true;
                        openFragment();
                    } else if (FullscreenActivity.needtorefreshsongmenu) {
                        FullscreenActivity.needtorefreshsongmenu = false;
                        if (sqLite!=null && sqLite.getSongid()!=null) {
                            sqLite = sqLiteHelper.getSong(StageMode.this, sqLite.getSongid());
                            sqLiteHelper.updateSong(StageMode.this, sqLite);
                        }
                        prepareSongMenu();
                    }

                    // Get the SQLite stuff
                    if (!StaticVariables.whichSongFolder.startsWith("..")) {
                        String songId = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
                        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                            nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(StageMode.this, storageAccess, preferences, songId);
                        }
                        sqLite = sqLiteHelper.getSong(StageMode.this, songId);

                        // If this song isn't indexed, set its details
                        if (sqLite!=null && (sqLite.getLyrics()==null || sqLite.getLyrics().equals(""))) {
                            sqLite = sqLiteHelper.setSong(sqLite);
                            sqLiteHelper.updateSong(StageMode.this,sqLite);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            FullscreenActivity.alreadyloading = false;

        }
    }

    // This bit listens for key presses (for page turn and scroll)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        keyRepeatCount = 0;
        event.startTracking();
        View rf = getCurrentFocus();
        if (rf!=null) {
            rf.clearFocus();
        }
        if (shortKeyPress) {
            FullscreenActivity.pressing_button = false;

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
                    if (mDrawerLayout.isDrawerOpen(optionmenu)) {
                        mDrawerLayout.closeDrawer(optionmenu);
                    }
                    if (mDrawerLayout.isDrawerOpen(songmenu)) {
                        mDrawerLayout.closeDrawer(songmenu);
                    }
                }
            }

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed();

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal1Code",21)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal1ShortPressAction","prev"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal2Code",22)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal2ShortPressAction","next"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal3Code",19)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal3ShortPressAction","prev"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal4Code",20)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal4ShortPressAction","next"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal5Code",92)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal5ShortPressAction","prev"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal6Code",93)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal6ShortPressAction","next"));
            }
        }
        shortKeyPress = true;
        longKeyPress = false;
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class PrepareSongMenu extends AsyncTask<Object, Void, String> {

        ArrayList<SQLite> songsInFolder;
        ArrayList<SQLite> childFolders;

        @Override
        protected void onPreExecute() {
            menuCount_TextView.setText("");
            menuCount_TextView.setVisibility(View.GONE);
            menuFolder_TextView.setText(getString(R.string.wait));
            song_list_view.setAdapter(null);
            LinearLayout indexLayout = findViewById(R.id.side_index);
            indexLayout.removeAllViews();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                songsInFolder = sqLiteHelper.getSongsInFolder(StageMode.this, StaticVariables.whichSongFolder);
                // Remove any that aren't there (due to updating something) - permanently fixed on reboot
                for (SQLite s:songsInFolder) {
                    if (s!=null && s.getFolder()!=null && s.getFilename()!=null) {
                        Uri u = storageAccess.getUriForItem(StageMode.this, preferences, "Songs", s.getFolder(), s.getFilename());
                        if (!storageAccess.uriExists(StageMode.this, u)) {
                            Log.d("StageMode", s.getFolder() + "/" + s.getFilename() + " doesn't exist - remove it");
                            songsInFolder.remove(s);
                        }
                    }
                }
                // Get a list of the child folders
                childFolders = sqLiteHelper.getChildFolders(StageMode.this, StaticVariables.whichSongFolder);
                songsInFolder.addAll(0,childFolders);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Set the name of the current folder
                    menuFolder_TextView.setText(StaticVariables.whichSongFolder);

                    // Prepare the array list of items in this folder for swiping
                    filenamesSongsInFolder = new ArrayList<>();

                    // Go through the found songs in folder and prepare the menu
                    ArrayList<SongMenuViewItems> songmenulist = new ArrayList<>();

                    String setcurrent = preferences.getMyPreferenceString(StageMode.this,"setCurrent","");

                    for (int i=0; i<songsInFolder.size(); i++) {
                        String foundsongfilename = songsInFolder.get(i).getFilename();
                        String foundsongauthor = songsInFolder.get(i).getAuthor();
                        String foundsongkey = songsInFolder.get(i).getKey();

                        if (foundsongfilename == null) {
                            foundsongfilename = getString(R.string.error);
                        }
                        if (foundsongauthor == null) {
                            foundsongauthor = "";
                        }
                        if (foundsongkey == null) {
                            foundsongkey = "";
                        }

                        String whattolookfor; // not going to find this by accident...
                        whattolookfor = setActions.whatToLookFor(StageMode.this, StaticVariables.whichSongFolder, foundsongfilename);

                        // Fix for variations, etc
                        whattolookfor = setActions.fixIsInSetSearch(whattolookfor);

                        boolean isinset = setcurrent.contains(whattolookfor);

                        SongMenuViewItems song = new SongMenuViewItems(foundsongfilename,
                                foundsongfilename, foundsongauthor, foundsongkey, isinset);
                        songmenulist.add(song);
                        filenamesSongsInFolder.add(foundsongfilename);
                    }

                    SongMenuAdapter lva = new SongMenuAdapter(StageMode.this, preferences, songmenulist);
                    song_list_view.setAdapter(lva);
                    song_list_view.setFastScrollEnabled(true);
                    song_list_view.setScrollingCacheEnabled(true);
                    lva.notifyDataSetChanged();

                    // Set the secondary alphabetical side bar
                    displayIndex(songmenulist, lva);

                    // Flick the song drawer open once it is ready
                    findSongInFolders();
                    if (firstrun_song) {
                        openMyDrawers("song");
                        closeMyDrawers("song_delayed");
                        firstrun_song = false;
                    }

                    String menusize = "" + (songmenulist.size());
                    if (menuCount_TextView != null) {
                        menuCount_TextView.setText(menusize);
                        menuCount_TextView.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void pedalPrevious() {
        if (preferences.getMyPreferenceBoolean(StageMode.this,"pedalScrollBeforeMove",true)) {
            if (mDrawerLayout.isDrawerOpen(songmenu)) {
                // Scroll the song menu up
                scrollMenu("up");
            } else {
                if (checkCanScrollUp()) {
                    CustomAnimations.animateFAB(scrollUpButton,StageMode.this);
                    doScrollUp();
                } else {
                    if (setBackButton!=null && setBackButton.getVisibility() == View.VISIBLE) {
                        CustomAnimations.animateFAB(setBackButton,StageMode.this);
                    }
                    goToPreviousItem();
                }
            }
        } else {
            goToPreviousItem();
        }
    }
    private void pedalNext() {
        if (preferences.getMyPreferenceBoolean(StageMode.this,"pedalScrollBeforeMove",true)) {
            if (mDrawerLayout.isDrawerOpen(songmenu)) {
                // Scroll the song menu down
                scrollMenu("down");
            } else {
                if (checkCanScrollDown()) {
                    CustomAnimations.animateFAB(scrollDownButton,StageMode.this);
                    doScrollDown();
                } else {
                    if (setForwardButton!=null && setForwardButton.getVisibility() == View.VISIBLE) {
                        CustomAnimations.animateFAB(setForwardButton,StageMode.this);
                    }
                    goToNextItem();
                }
            }
        } else {
            goToNextItem();
        }
    }
    private void pedalUp() {
        if (mDrawerLayout.isDrawerOpen(songmenu)) {
            // Scroll the song menu up
            scrollMenu("up");
        } else {
            if (checkCanScrollUp()) {
                CustomAnimations.animateFAB(scrollUpButton,StageMode.this);
                doScrollUp();
            }
        }
    }
    private void pedalDown() {
        if (mDrawerLayout.isDrawerOpen(songmenu)) {
            // Scroll the song menu down
            scrollMenu("down");
        } else {
            if (checkCanScrollDown()) {
                CustomAnimations.animateFAB(scrollDownButton,StageMode.this);
                doScrollDown();
            }
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        boolean actionrecognised = doLongKeyPressAction(keyCode);

        if (actionrecognised) {
            shortKeyPress = false;
            longKeyPress = true;
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private boolean doLongKeyPressAction(int keyCode) {
        keyRepeatCount = 0;
        boolean actionrecognised = false;
        if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal1Code",21)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal1LongPressAction","songmenu"));

        } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal2Code",22)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal2LongPressAction","editset"));

        } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal3Code",19)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal3LongPressAction","songmenu"));

        } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal4Code",20)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal4LongPressAction","editset"));

        } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal5Code",92)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal5LongPressAction","songmenu"));

        } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this,"pedal6Code",93)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(StageMode.this,"pedal6LongPressAction","editset"));
        }
        return actionrecognised;
    }

    private class simpleOnScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        float scaleFactor;
        LinearLayout songbit;

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            scaleFactor = scaleGestureDetector.getScaleFactor();
            highlightNotes.setScaleX(scaleFactor);
            highlightNotes.setScaleY(scaleFactor);

            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                glideimage.setScaleX(scaleFactor);
                glideimage.setScaleY(scaleFactor);
            } else {
                songbit.setScaleX(scaleFactor);
                songbit.setScaleY(scaleFactor);
            }
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            highlightNotes.setPivotX(glideimage.getLeft());
            highlightNotes.setPivotY(glideimage.getTop());
            highlightNotes.setScaleX(1.0f);
            highlightNotes.setScaleY(1.0f);

            if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                glideimage.getLayoutParams().width = songwidth;
                glideimage.getLayoutParams().height = songheight;
                glideimage.setScaleX(1.0f);
                glideimage.setScaleY(1.0f);
                glideimage.setPivotX(glideimage.getLeft());
                glideimage.setPivotY(glideimage.getTop());
                resetImageViewSizes();
            } else {
                songbit = (LinearLayout) songscrollview.getChildAt(0);
                songbit.setPivotX(songbit.getLeft());
                songbit.setPivotY(songbit.getTop());
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            scaleFactor = scaleGestureDetector.getScaleFactor();

            final int newwidth = (int) (songwidth * scaleFactor);
            final int newheight = (int) (songheight * scaleFactor);
            final int screenwidth = getAvailableWidth();
            final int screenheight = getAvailableHeight();

            if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {

                final HorizontalScrollView.LayoutParams hsvlp = (HorizontalScrollView.LayoutParams) glideimage_ScrollView.getLayoutParams();
                glideimage_FrameLayout.getLayoutParams().width = newwidth;
                glideimage_FrameLayout.getLayoutParams().height = newheight;

                glideimage.setAdjustViewBounds(true);
                glideimage.getLayoutParams().width = newwidth;
                glideimage.getLayoutParams().height = newheight;
                glideimage_FrameLayout.requestLayout();
                glideimage_FrameLayout.post(new Runnable() {

                    @Override
                    public void run() {
                        glideimage_ScrollView.scrollTo(0, 0);
                        glideimage_HorizontalScrollView.scrollTo(0, 0);
                        glideimage.setScaleX(1.0f);
                        glideimage.setScaleY(1.0f);

                        // If the width of the song is smaller than the screen width, make the scrollview the screen width
                        // Otherwise make it expand to fit the song
                        if (newwidth <= screenwidth) {
                            hsvlp.width = screenwidth;

                        } else {
                            hsvlp.width = newwidth;
                        }

                        //Keep the scrollview the height of the page
                        hsvlp.height = screenheight;

                        glideimage_ScrollView.setLayoutParams(hsvlp);
                    }
                });

            } else {
                final HorizontalScrollView.LayoutParams hsvlp = (HorizontalScrollView.LayoutParams) songscrollview.getLayoutParams();
                final ScrollView.LayoutParams lllp = new ScrollView.LayoutParams(newwidth, newheight);
                lllp.width = newwidth;
                lllp.height = newheight;
                if (newheight<songheight) {
                    // Resizing the height below the original size doesn't work in a linear layout!
                    // Use a negative padding instead
                    songbit.setPaddingRelative(0,0,0,newheight-songheight);
                } else {
                    songbit.setPadding(0,0,0,0);
                }
                songbit.setLayoutParams(lllp);

                // The minimum height is for adding height to the bottom if it gets bigger
                songbit.setMinimumHeight(newheight);
                songscrollview.requestLayout();
                songscrollview.post(new Runnable() {

                    @Override
                    public void run() {
                        songscrollview.scrollTo(0, 0);
                        horizontalscrollview.scrollTo(0, 0);

                        // If the width of the song is smaller than the screen width, make the scrollview the screen width
                        // Otherwise make it expand to fit the song
                        if (newwidth <= screenwidth) {
                            hsvlp.width = screenwidth;
                        } else {
                            hsvlp.width = newwidth;
                        }

                        //Keep the scrollview the height of the page
                        hsvlp.height = screenheight;

                        songscrollview.setLayoutParams(hsvlp);
                        songscrollview.requestLayout();

                    }
                });
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        try {
            int action = ev.getAction();
            // WOULD BE BETTER IF THIS WAS CALLED ON SOME KIND OF ONSCROLL LISTENER
            scaleGestureDetector.onTouchEvent(ev);
            if (action == MotionEvent.ACTION_MOVE) {// Set a runnable to check the scroll position
                FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
                delaycheckscroll.post(checkScrollPosition);

                // Set a runnable to check the scroll position after 1 second
                delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
            }

            // TouchEvent dispatcher.
            if (gestureDetector != null) {
                if (!gestureDetector.onTouchEvent(ev)) {
                    if (action == MotionEvent.ACTION_UP && !FullscreenActivity.scrollbutton) {
                        toggleActionBar();
                        FullscreenActivity.wasscrolling = false;

                    } else {
                        try {
                            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    FullscreenActivity.wasscrolling = false;
                    FullscreenActivity.scrollbutton = false;
                    FullscreenActivity.isManualDragging = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.dispatchTouchEvent(ev);
    }

    @SuppressLint("StaticFieldLeak")
    private class PrepareOptionMenu extends AsyncTask<Object, Void, String> {

        public void onPreExecute() {
            try {
                optionmenu = findViewById(R.id.optionmenu);
                try {
                    optionmenu.removeAllViews();
                } catch (Exception e) {
                    Log.d("StageMode", "Error removing view");
                }
                optionmenu.addView(OptionMenuListeners.prepareOptionMenu(StageMode.this,getSupportFragmentManager()));
                if (optionmenu != null) {
                    OptionMenuListeners.optionListeners(optionmenu, StageMode.this, preferences, storageAccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Get the current set list
            try {
                setActions.prepareSetList(StageMode.this,preferences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    if (firstrun_option) {
                        openMyDrawers("option");
                        closeMyDrawers("option_delayed");
                        firstrun_option = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            OptionMenuListeners.updateMenuVersionNumber(StageMode.this, (TextView) findViewById(R.id.menu_version_bottom));
        }

    }

    // Open/close the drawers
    private void gesture1() {
        if (mDrawerLayout.isDrawerOpen(songmenu)) {
            closeMyDrawers("song");
        } else {
            openMyDrawers("song");
        }
        FullscreenActivity.wasscrolling = false;
        try {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Edit song
    private void gesture2() {
        if (FullscreenActivity.whattodo!=null && FullscreenActivity.whattodo.equals("editsongpdf")) {
            openFragment();
        } else if (FullscreenActivity.isPDF) {
            FullscreenActivity.whattodo = "extractPDF";
            openFragment();
        } else if (justSong(StageMode.this)) {
            // Edit the song
            FullscreenActivity.whattodo = "editsong";
            openFragment();
        }
    }

    // Add to set
    private void gesture3() {
        if (StaticVariables.whichSongFolder.equals(getString(R.string.mainfoldername)) || StaticVariables.whichSongFolder.equals("MAIN") ||
                StaticVariables.whichSongFolder.equals("")) {
            StaticVariables.whatsongforsetwork = "$**_" + StaticVariables.songfilename + "_**$";
        } else {
            StaticVariables.whatsongforsetwork = "$**_" + StaticVariables.whichSongFolder + "/"
                    + StaticVariables.songfilename + "_**$";
        }

        // Allow the song to be added, even if it is already there
        String val = preferences.getMyPreferenceString(StageMode.this,"setCurrent","") + StaticVariables.whatsongforsetwork;
        preferences.setMyPreferenceString(StageMode.this,"setCurrent",val);
        // Tell the user that the song has been added.
        showToastMessage("\"" + StaticVariables.songfilename + "\" "
                + getResources().getString(R.string.addedtoset));
        // Vibrate to let the user know something happened
        DoVibrate.vibrate(StageMode.this, 50);

        setActions.prepareSetList(StageMode.this,preferences);
        prepareOptionMenu();

        // Show the current set
        openMyDrawers("option");

        // Hide the menus - 1 second after opening the Option menu,
        // close it (1000ms total)
        Handler optionMenuFlickClosed = new Handler();
        optionMenuFlickClosed.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(optionmenu);
            }
        }, 1000);
    }

    // Redraw the lyrics page
    private void gesture4() {
        loadSong();
    }

    @Override
    // Stop or start autoscroll
    public void gesture5() {
        DoVibrate.vibrate(StageMode.this, 50);
        if (StaticVariables.isautoscrolling) {
            stopAutoScroll();
            StaticVariables.clickedOnAutoScrollStart = false;

        } else {
            if (StaticVariables.autoscrollok || preferences.getMyPreferenceBoolean(StageMode.this, "autoscrollUseDefaultTime", true)) {
                StaticVariables.clickedOnAutoScrollStart = true;
                startAutoScroll();
            } else {
                showToastMessage(getResources().getString(R.string.autoscroll) + " - " +
                        getResources().getString(R.string.notset));
            }
        }
    }

    private class SwipeDetector extends GestureDetector.SimpleOnGestureListener {

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
            // 8 = start/stop autoscroll + pad
            // 9 = start/stop autoscroll + metronome
            //10 = start/stop autoscroll + pad + metronome
            //11 = start/stop autoscroll + pad + metronome
            // 0/else = off (highest menu item)

            // First test conditions
            if (oktoregistergesture()) {

                // Now find out which gesture we've gone for
                switch (preferences.getMyPreferenceInt(StageMode.this,"gestureScreenDoubleTap",2)) {
                    case 1:
                        gesture1();  // Open/close the drawers
                        break;
                    case 2:
                        if (FullscreenActivity.isSong) {
                            gesture2();
                        } else {
                            FullscreenActivity.whattodo = "extractPDF";
                            openFragment();
                        }
                        break;
                    case 3:
                        gesture3();  // Add the song to the set
                        break;
                    case 4:
                        gesture4();  // Refresh the current song
                        break;
                    case 5:
                        gesture5();  // Stop/start autoscroll
                        break;
                    case 6:
                        gesture6();  // Stop/start pad
                        break;
                    case 7:
                        gesture7();  // Stop/start metronome
                        break;
                    case 8:
                        gesture5();  // Stop/start autoscroll
                        gesture6();  // Stop/start pad
                        break;
                    case 9:
                        gesture5();  // Stop/start autoscroll
                        gesture7();  // Stop/start metronome
                        break;
                    case 10:
                        gesture6();  // Stop/start pad
                        gesture7();  // Stop/start metronome
                        break;
                    case 11:
                        gesture5();  // Stop/start autoscroll
                        gesture6();  // Stop/start pad
                        gesture7();  // Stop/start metronome
                        break;
                    default:
                        // Do nothing
                        break;
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
            if (oktoregistergesture()) {

                // Now find out which gesture we've gone for
                switch (preferences.getMyPreferenceInt(StageMode.this,"gestureScreenLongPress",3)) {
                    case 1:
                        gesture1();  // Open/close the drawers
                        break;
                    case 2:
                        gesture2();  // Edit the song
                        break;
                    case 3:
                        gesture3();  // Add the song to the set
                        break;
                    case 4:
                        gesture4();  // Refresh the current song
                        break;
                    case 5:
                        gesture5();  // Stop/start autoscroll
                        break;
                    case 6:
                        gesture6();  // Stop/start pad
                        break;
                    case 7:
                        gesture7();  // Stop/start metronome
                        break;
                    case 8:
                        gesture5();  // Stop/start autoscroll
                        gesture6();  // Stop/start pad
                        break;
                    case 9:
                        gesture5();  // Stop/start autoscroll
                        gesture7();  // Stop/start metronome
                        break;
                    case 10:
                        gesture6();  // Stop/start pad
                        gesture7();  // Stop/start metronome
                        break;
                    case 11:
                        gesture5();  // Stop/start autoscroll
                        gesture6();  // Stop/start pad
                        gesture7();  // Stop/start metronome
                        break;
                    default:
                        // Do nothing
                        break;
                }
            }
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            FullscreenActivity.wasscrolling = true;
            FullscreenActivity.isManualDragging = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            try {
                // Check movement along the Y-axis. If it exceeds
                // SWIPE_MAX_OFF_PATH, then dismiss the swipe.
                int screenwidth = mypage.getWidth();
                int leftmargin = 40;
                int rightmargin = screenwidth - 40;
                if (Math.abs(e1.getY() - e2.getY()) > preferences.getMyPreferenceInt(StageMode.this,"swipeMaxDistanceYError",200)) {
                    return false;
                }

                if (FullscreenActivity.tempswipeSet.equals("disable")) {
                    return false; // Currently disabled swiping to let screen finish drawing.
                }

                // Swipe from right to left.
                // The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE)
                // and a certain velocity (SWIPE_THRESHOLD_VELOCITY).
                if (e1.getX() - e2.getX() > preferences.getMyPreferenceInt(StageMode.this,"swipeMinimumDistance",250)
                        && e1.getX() < rightmargin
                        && Math.abs(velocityX) > preferences.getMyPreferenceInt(StageMode.this,"swipeMinimumVelocity",600)
                        && preferences.getMyPreferenceBoolean(StageMode.this,"swipeForSongs",true)) {

                    // Trying to move to the next item
                    try {
                        setForwardButton.performClick();
                        //goToNextItem();
                    } catch (Exception e) {
                        // No song after
                    }
                    return true;
                }

                // Swipe from left to right.
                // The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE)
                // and a certain velocity (SWIPE_THRESHOLD_VELOCITY).
                if (e2.getX() - e1.getX() > preferences.getMyPreferenceInt(StageMode.this,"swipeMinimumDistance",250)
                        && e1.getX() > leftmargin
                        && Math.abs(velocityX) > preferences.getMyPreferenceInt(StageMode.this,"swipeMinimumVelocity",600)
                        && preferences.getMyPreferenceBoolean(StageMode.this,"swipeForSongs",true)) {

                    // Go to previous item
                    try {
                        setBackButton.performClick();
                        //goToPreviousItem();
                    } catch (Exception e) {
                        // No song before
                    }
                    return true;
                }
                return false;

            } catch (Exception e) {
                Log.d("StageMode", "error");
            }
            return false;
        }
    }

    @Override
    public void gesture7() {
        DoVibrate.vibrate(StageMode.this, 50);
        StaticVariables.metronomeok = Metronome.isMetronomeValid();
        if (StaticVariables.metronomeok) {
            StaticVariables.clickedOnMetronomeStart = !StaticVariables.metronomeonoff.equals("on");
            Metronome.startstopMetronome(StageMode.this, StageMode.this,
                    preferences.getMyPreferenceBoolean(StageMode.this, "metronomeShowVisual", false),
                    defmetronomecolor, preferences.getMyPreferenceString(StageMode.this, "metronomePan", "C"),
                    preferences.getMyPreferenceFloat(StageMode.this, "metronomeVol", 0.5f),
                    preferences.getMyPreferenceInt(StageMode.this, "metronomeLength", 0));
        } else {
            showToastMessage(getResources().getString(R.string.metronome) + " - " +
                    getResources().getString(R.string.notset));
        }
    }

    // The stuff to deal with the second screen
    @Override
    public void connectHDMI() {
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        updateDisplays();
    }

    //@SuppressLint("NewApi")
    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            try {
                updateDisplays();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            teardown();
            mSelectedDevice = null;
            FullscreenActivity.isPresenting = false;
            FullscreenActivity.isHDMIConnected = false;
        }

        void teardown() {
            try {
                CastRemoteDisplayLocalService.stopService();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (hdmi != null) {
                    hdmi.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRouteAdded(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
        }

        @Override
        public void onRouteRemoved(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
        }

        @Override
        public void onRouteChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
        }

        @Override
        public void onRouteVolumeChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class PlayPads extends AsyncTask<Void, Void, Integer> {
        int which;
        int path;
        boolean validlinkaudio;
        boolean error;

        PlayPads(Integer w) {
            which = w;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                processSong.processKey(StageMode.this, preferences, storageAccess);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return which;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(Integer i) {
            try {
                if (!cancelled) {
                    if (StaticVariables.mPadFile.equals(getResources().getString(R.string.pad_auto)) ||
                            StaticVariables.mPadFile.equals("")) {

                        boolean custompad;
                        StaticVariables.padson = true;
                        if (StaticVariables.pad_filename != null && StaticVariables.mKey != null) {
                            custompad = !StaticVariables.pad_filename.endsWith("null") && StaticVariables.pad_filename.startsWith("custom_");
                            AssetFileDescriptor afd = null;
                            String padpath = null;
                            if (custompad) {
                                // Prepare the custom pad
                                Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Pads", "",
                                        StaticVariables.pad_filename.replace("custom_", ""));
                                padpath = uri.toString();

                            } else {
                                // Prepare the default auto pad
                                path = getResources().getIdentifier(StaticVariables.pad_filename, "raw", getPackageName());
                                try {
                                    afd = getResources().openRawResourceFd(path);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    which = 0;
                                }
                            }

                            if (which == 1) {
                                try {
                                    PadFunctions.getPad1Status();
                                    if (StaticVariables.pad1Playing) {
                                        FullscreenActivity.mPlayer1.stop();
                                    }
                                    FullscreenActivity.mPlayer1.reset();
                                    FullscreenActivity.mPlayer1.setOnPreparedListener(new Player1Prepared());
                                    if (custompad) {
                                        FullscreenActivity.mPlayer1.setDataSource(StageMode.this,Uri.parse(padpath));
                                    } else {
                                        FullscreenActivity.mPlayer1.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                        afd.close();
                                    }

                                    FullscreenActivity.mPlayer1.prepareAsync();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    StaticVariables.fadeWhichPad = 0;
                                    FullscreenActivity.whichPad = 0;
                                    killPad();
                                }
                            } else if (which == 2) {
                                try {
                                    PadFunctions.getPad2Status();
                                    if (StaticVariables.pad2Playing) {
                                        FullscreenActivity.mPlayer2.stop();
                                    }
                                    FullscreenActivity.mPlayer2.reset();
                                    FullscreenActivity.mPlayer2.setOnPreparedListener(new Player2Prepared());
                                    if (custompad) {
                                        FullscreenActivity.mPlayer2.setDataSource(StageMode.this,Uri.parse(padpath));
                                    } else {
                                        FullscreenActivity.mPlayer2.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                        afd.close();
                                    }
                                    FullscreenActivity.mPlayer2.prepareAsync();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    StaticVariables.fadeWhichPad = 0;
                                    FullscreenActivity.whichPad = 0;
                                    killPad();
                                }
                            }

                        } else {
                            // No key specified in the song - play nothing
                            StaticVariables.myToastMessage = getResources().getString(R.string.pad_error);
                            StaticVariables.padson = false;
                            error = true;
                            StaticVariables.fadeWhichPad = 0;
                            FullscreenActivity.whichPad = 0;
                            killPad();
                        }
                    }

                    // Prepare the link audio file
                    if (StaticVariables.mPadFile.equals(getResources().getString(R.string.link_audio))) {
                        try {
                            StorageAccess storageAccess = new StorageAccess();
                            Uri uri = storageAccess.fixLocalisedUri(StageMode.this, preferences, StaticVariables.mLinkAudio);
                            if (which == 1) {
                                PadFunctions.getPad1Status();
                                if (StaticVariables.pad1Playing) {
                                    FullscreenActivity.mPlayer1.stop();
                                }
                                FullscreenActivity.mPlayer1.reset();
                                FullscreenActivity.mPlayer1.setOnPreparedListener(new Player1Prepared());
                                FullscreenActivity.mPlayer1.setDataSource(StageMode.this, uri);
                                FullscreenActivity.mPlayer1.prepareAsync();
                            } else if (which == 2) {
                                PadFunctions.getPad2Status();
                                if (StaticVariables.pad2Playing) {
                                    FullscreenActivity.mPlayer2.stop();
                                }
                                FullscreenActivity.mPlayer2.reset();
                                FullscreenActivity.mPlayer2.setOnPreparedListener(new Player2Prepared());
                                FullscreenActivity.mPlayer2.setDataSource(StageMode.this, uri);
                                FullscreenActivity.mPlayer2.prepareAsync();
                            }
                            validlinkaudio = true;
                        } catch (Exception e) {
                            validlinkaudio = false;
                            Log.d("StageMode", "Something went wrong with the media");
                        }

                        if (!validlinkaudio) {
                            // Problem with link audio so don't use it
                            StaticVariables.myToastMessage = getResources().getString(R.string.link_audio) + " - " +
                                    getResources().getString(R.string.file_type_unknown);
                            StaticVariables.padson = false;
                            error = true;
                        }
                    }

                    // No pads wanted
                    if (StaticVariables.mPadFile.equals(getResources().getString(R.string.off)) && StaticVariables.padson) {
                        // Pad shouldn't play
                        StaticVariables.padson = false;
                        StaticVariables.fadeWhichPad = 0;
                        FullscreenActivity.whichPad = 0;
                        killPad();
                    }

                    if (error) {
                        ShowToast.showToast(StageMode.this);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void refreshSecondaryDisplay(String which) {
        try {
            switch (which) {
                case "all":
                case "chords":
                case "autoscale":
                case "maxfontsize":
                case "manualfontsize":
                default:
                    if (FullscreenActivity.isPresenting && !FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationService.ExternalDisplay.doUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationServiceHDMI.doUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "info":
                    if (FullscreenActivity.isPresenting && !FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationService.ExternalDisplay.updateFonts();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationServiceHDMI.updateFonts();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "backgrounds":
                    if (FullscreenActivity.isPresenting && !FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationService.ExternalDisplay.fixBackground();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationServiceHDMI.fixBackground();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "margins":
                    if (FullscreenActivity.isPresenting && !FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationService.ExternalDisplay.changeMargins();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationServiceHDMI.changeMargins();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void selectAFileUri(String s) {
        Intent intent = new Intent(this, FolderPicker.class);
        intent.putExtra("title", s);
        intent.putExtra("pickFiles", true);
        if (StaticVariables.uriTree!=null) {
            intent.putExtra("location", StaticVariables.uriTree.getPath());
        }
        startActivityForResult(intent, StaticVariables.REQUEST_FILE_CHOOSER);
    }
}