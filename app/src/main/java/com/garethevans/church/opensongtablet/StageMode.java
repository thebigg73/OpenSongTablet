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
import android.graphics.Canvas;
import android.graphics.Point;
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
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lib.folderpicker.FolderPicker;

// This includes all recent version pulls from IV and GE

public class StageMode extends AppCompatActivity implements
        PopUpAreYouSureFragment.MyInterface, PopUpPagesFragment.MyInterface,
        PopUpEditSongFragment.MyInterface, PopUpSongDetailsFragment.MyInterface,
        PopUpPresentationOrderFragment.MyInterface, PopUpListSetsFragment.MyInterface,
        SongMenuListeners.MyInterface, OptionMenuListeners.MyInterface, MenuHandlers.MyInterface,
        SetActions.MyInterface, PopUpFullSearchFragment.MyInterface, IndexSongs.MyInterface,
        SearchView.OnQueryTextListener, PopUpSetViewNew.MyInterface,
        PopUpChooseFolderFragment.MyInterface, PopUpCustomSlideFragment.MyInterface,
        PopUpImportExternalFile.MyInterface, PopUpRandomSongFragment.MyInterface,
        PopUpBackupPromptFragment.MyInterface, PopUpSwipeSettingsFragment.MyInterface,
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
        PopUpImportExportOSBFragment.MyInterface, SongMenuAdapter.MyInterface,
        BatteryMonitor.MyInterface, PopUpMenuSettingsFragment.MyInterface,
        PopUpLayoutFragment.MyInterface, DownloadTask.MyInterface,
        PopUpExportFragment.MyInterface, PopUpActionBarInfoFragment.MyInterface,
        PopUpCreateDrawingFragment.MyInterface,
        PopUpConnectFragment.MyInterface,
        PopUpCCLIFragment.MyInterface, NearbyReturnActionsInterface, NearbyInterface,
        PopUpBibleXMLFragment.MyInterface, PopUpShowMidiMessageFragment.MyInterface,
        PopUpChordFormatFragment.MyInterface {

    private final String TAG = "StageMode";
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
    private RelativeLayout batteryholder;

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
    private boolean blockKeyAction;
    private boolean blockActionOnKeyUp;
    private boolean drawerOrFragmentActive = false;

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
    private RelativeLayout setButtonLayout;
    private RelativeLayout padButtonLayout;
    private RelativeLayout autoscrollButtonLayout;
    private RelativeLayout metronomeButtonLayout;
    private RelativeLayout extraButtonLayout;
    private RelativeLayout chordButtonLayout;
    private RelativeLayout stickyButtonLayout;
    private RelativeLayout notationButtonLayout;
    private RelativeLayout highlightButtonLayout;
    private RelativeLayout pageselectButtonLayout;
    private RelativeLayout linkButtonLayout;
    private RelativeLayout chordButton_ungroupedLayout;
    private RelativeLayout stickyButton_ungroupedLayout;
    private RelativeLayout notationButton_ungroupedLayout;
    private RelativeLayout highlightButton_ungroupedLayout;
    private RelativeLayout pageselectButton_ungroupedLayout;
    private RelativeLayout linkButton_ungroupedLayout;
    private RelativeLayout customButtonLayout;
    private RelativeLayout custom1ButtonLayout;
    private RelativeLayout custom2ButtonLayout;
    private RelativeLayout custom3ButtonLayout;
    private RelativeLayout custom4ButtonLayout;
    private RelativeLayout custom1Button_ungroupedLayout;
    private RelativeLayout custom2Button_ungroupedLayout;
    private RelativeLayout custom3Button_ungroupedLayout;
    private RelativeLayout custom4Button_ungroupedLayout;
    private RelativeLayout scrollDownButtonLayout;
    private RelativeLayout scrollUpButtonLayout;
    private RelativeLayout setBackButtonLayout;
    private RelativeLayout setForwardButtonLayout;
    private ScrollView mainbuttons;
    private ScrollView extrabuttons;
    private ScrollView extrabuttons2;
    private int keyRepeatCount = 0;
    private int sendSongDelay = 0;

    // MIDI
    private Midi midi;

    // Casting
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private final MyMediaRouterCallback mMediaRouterCallback = new MyMediaRouterCallback();
    private CastDevice mSelectedDevice;
    private boolean newsongloaded = false;
    private boolean songUriExists;


    // Dialogue fragments and stuff
    private DialogFragment newFragment;

    // Gestures
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // Permissions
    private Permissions permissions;

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
    private AsyncTask<Void, Void, Integer> play_pads;
    private AsyncTask<String, Integer, String> do_download;
    private AsyncTask<Object, Integer, String> get_scrollheight;

    // Allow the menus to flash open to show where they are on first run
    private boolean firstrun_option = true;
    private boolean firstrun_song = true;

    // Handlers and Runnables
    private final Handler dopadProgressTime = new Handler();
    private final Runnable padprogressTimeRunnable = this::getPadProgress;
    private final Runnable onEverySecond = this::preparePadProgress;
    private Handler delaycheckscroll;
    private Runnable checkScrollPosition;
    private final Handler restoreImmersiveModeHandler = new Handler();
    private final Runnable restoreImmersiveModeRunnable = this::restoreTransparentBars;
    private final Handler delayactionBarHide = new Handler();
    private final Runnable hideActionBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (ab != null && ab.isShowing()) {
                ab.hide();
            }
        }
    };
    // IV - Handlers for confirmation of page change when using pedal
    private final Handler pedalPreviousAndNextNeedsConfirmHandler = new Handler();
    private final Runnable pedalPreviousAndNextNeedsConfirmRunnable = () -> StaticVariables.pedalPreviousAndNextNeedsConfirm = true;
    private final Handler pedalPreviousAndNextIgnoreHandler = new Handler();
    private final Runnable pedalPreviousAndNextIgnoreRunnable = () -> StaticVariables.pedalPreviousAndNextIgnore = false;
    // Handlers for activity after settled on a song
    private final Handler startAutoscrollHandler = new Handler();
    private final Runnable startAutoscrollRunnable = this::gesture5;
    private final Handler showStickyHandler = new Handler();
    private final Runnable showStickyRunnable = this::showSticky;
    private final Handler startCapoAnimationHandler = new Handler();
    private final Runnable startCapoAnimationRunnable = () -> CustomAnimations.highlightAction(capoInfo, StageMode.this);
    private final Handler startMetronomeHandler = new Handler();
    private final Runnable startMetronomeRunnable = this::gesture7;

    // Handler for temporary pause of autoscroll
    private final Handler endManualDraggingHandler = new Handler();
    private final Runnable endManualDraggingRunnable = () -> {
        FullscreenActivity.isManualDragging = false;
        // IV - Use current position (it may have been moved) for autoscroll resume
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY();
        } else {
            FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
        }
    };
    // Handler for stop of autoscroll
    private final Handler endAutoScrollHandler = new Handler();
    private final Runnable endAutoScrollRunnable = () -> {
        // If we have not needed to scroll or are still at the end of the page then stop
        if (StaticVariables.isautoscrolling && (StaticVariables.scrollpageHeight <= 0.0f || FullscreenActivity.newPosFloat >= StaticVariables.scrollpageHeight)) {
            StaticVariables.autoscrollispaused = false;
            StaticVariables.isautoscrolling = false;
        }
    };
    // Handlers to support nearby send of first and last song only for a sequence of rapid song changes
    // This reduces stress on clients
    private final Handler sendSongAfterDelayHandler = new Handler();
    private final Runnable sendSongAfterDelayRunnable = () -> {
        sendSongToConnected();
        sendSongDelay = 3000;
    };
    private final Handler playPadHandler = new Handler();
    private final Runnable playPadRunnable = this::playPad;
    private final Handler resetSendSongAfterDelayHandler = new Handler();
    private final Runnable resetSendSongAfterDelayRunnable = () -> sendSongDelay = 0;

    // Handlers for fonts
    private Handler lyrichandler;
    private Handler chordhandler;
    private Handler stickyhandler;
    private Handler presohandler;
    private Handler presoinfohandler;
    private Handler customhandler;

    // Network discovery / connections
    NearbyConnections nearbyConnections;

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
    private ProfileActions profileActions;
    private MakePDF makePDF;

    private boolean pdfCanContinueScrolling;
    private boolean dealtwithaspdf;

    private long songTransitionStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Welcome to Stage Mode");

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
        profileActions = new ProfileActions();
        makePDF = new MakePDF();
        OptionMenuListeners optionMenuListeners = new OptionMenuListeners(this);
        nearbyConnections = new NearbyConnections(this, preferences, storageAccess, processSong, optionMenuListeners, sqLiteHelper);

        // IV - Index at start of session
        if (FullscreenActivity.doonetimeactions) {
            new Thread(() -> {
                runOnUiThread(() -> {
                    StaticVariables.myToastMessage = getString(R.string.search_index_start);
                    ShowToast.showToast(StageMode.this);
                });
                indexSongs.fullIndex(StageMode.this, preferences, storageAccess, sqLiteHelper, songXML,
                        chordProConvert, onSongConvert, textSongConvert, usrConvert);
                runOnUiThread(() -> {
                    StaticVariables.myToastMessage = getString(R.string.search_index_end);
                    ShowToast.showToast(StageMode.this);
                    // Now instruct the song menu to be built again.
                    prepareSongMenu();
                });
            }).start();
        }

        // Get the language
        FixLocale.fixLocale(StageMode.this, preferences);

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
        MediaRouteButton mediaRouteButton = findViewById(R.id.media_route_menu_item);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
        try {
            CastContext.getSharedInstance(this);
        } catch (Exception e) {
            Log.d(TAG, "No Google Services");
        }
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast("4E2B0891"))
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .build();

        new Thread(() -> {
            // Set the fullscreen window flags
            runOnUiThread(() -> {
                setWindowFlags();
                setWindowFlagsAdvanced();
            });

            // Since this mode has just been opened, force an update to the cast screen
            StaticVariables.forcecastupdate = true;

            runOnUiThread(() -> {
                // Set up the gesture detector
                scaleGestureDetector = new ScaleGestureDetector(StageMode.this, new simpleOnScaleGestureListener());
                gestureDetector = new GestureDetector(StageMode.this, new SwipeDetector());

                // Set up the toolbar and views
                setUpToolbar();
                setUpViews();
            });

            // Battery monitor
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            br = new BatteryMonitor();
            StageMode.this.registerReceiver(br, filter);

            runOnUiThread(() -> {
                // Make the drawers match half the width of the screen
                resizeDrawers();

                // Restore Drawer Swipe preference
                toggleDrawerSwipe();

                // IV - RefreshAll calls setupButtons, prepareOptionsMenu and setupSongButtons
                // Start with MAIN option menu
                StaticVariables.whichOptionMenu = "MAIN";

                // Load the song and get started
                refreshAll();

                // Prepare abhide listener
                setupAbHide();
                // IV - Force display of top level of option menu - needed after mode change
                closeMyDrawers("song");
            });

            // Set up the Nearby connection service
            permissions = new Permissions();
            permissions.setNearbyPermissionsString();
            getBluetoothName();
            nearbyConnections.getUserNickname();

            dealWithIntent();

            // Set up stuff for NFC transfer (if allowed)
            if (FullscreenActivity.mAndroidBeamAvailable) {
                FullscreenActivity.mNfcAdapter = NfcAdapter.getDefaultAdapter(StageMode.this);
                mFileUriCallback = new FileUriCallback();
                // Set the dynamic callback for URI requests.
                FullscreenActivity.mNfcAdapter.setBeamPushUrisCallback(mFileUriCallback, StageMode.this);
            }

            // Initialise the ab info
            runOnUiThread(this::adjustABInfo);

        }).start();

        // IV - Check backups at start of session
        if (FullscreenActivity.doonetimeactions) {
            // Check if we need to remind the user to backup their songs
            checkBackupState();
        }

        // IV - Setup handling of scroll and set dynamic buttons once
        scrollButtons();

        // Establish a known state for Nearby
        nearbyConnections.turnOffNearby();

        // IV -  One time actions will have been completed
        FullscreenActivity.doonetimeactions = false;

        prepareSongMenu();
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
        setSupportActionBar(ab_toolbar); // Setting toolbar as the ActionBar with setSupportActionBar() call
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
        defmetronomecolor = preferences.getMyPreferenceInt(StageMode.this, "light_metronomeColor", StaticVariables.darkishred);
        defpagebuttoncolor = preferences.getMyPreferenceInt(StageMode.this, "light_pageButtonsColor", StaticVariables.purplyblue);
        defstickytextcolor = preferences.getMyPreferenceInt(StageMode.this, "light_stickyTextColor", StaticVariables.black);
        defstickybgcolor = preferences.getMyPreferenceInt(StageMode.this, "light_stickyBackgroundColor", StaticVariables.lightyellow);
        defextrainfobgcolor = preferences.getMyPreferenceInt(StageMode.this, "light_extraInfoBgColor", StaticVariables.grey);
        defextrainfotextcolor = preferences.getMyPreferenceInt(StageMode.this, "light_extraInfoTextColor", StaticVariables.white);
        lyricsTextColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsTextColor", StaticVariables.black);
        lyricsCapoColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsCapoColor", StaticVariables.red);
        lyricsBackgroundColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsBackgoundColour", StaticVariables.white);
        lyricsVerseColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsVerseColor", StaticVariables.white);
        lyricsChorusColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsChorusColor", StaticVariables.vlightpurple);
        lyricsBridgeColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsBridgeColor", StaticVariables.vlightcyan);
        lyricsCommentColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsCommentColor", StaticVariables.vlightblue);
        lyricsPreChorusColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsPreChorusColor", StaticVariables.lightgreen);
        lyricsTagColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsTagColor", StaticVariables.vlightgreen);
        lyricsChordsColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsChordsColor", StaticVariables.darkblue);
        lyricsCustomColor = preferences.getMyPreferenceInt(StageMode.this, "light_lyricsCustomColor", StaticVariables.lightishcyan);
        presoFontColor = preferences.getMyPreferenceInt(StageMode.this, "light_presoFontColor", StaticVariables.white);
    }

    private void setThemeCustom1() {
        defmetronomecolor = preferences.getMyPreferenceInt(StageMode.this, "custom1_metronomeColor", StaticVariables.darkishred);
        defpagebuttoncolor = preferences.getMyPreferenceInt(StageMode.this, "custom1_pageButtonsColor", StaticVariables.purplyblue);
        defstickytextcolor = preferences.getMyPreferenceInt(StageMode.this, "custom1_stickyTextColor", StaticVariables.black);
        defstickybgcolor = preferences.getMyPreferenceInt(StageMode.this, "custom1_stickyBackgroundColor", StaticVariables.lightyellow);
        defextrainfobgcolor = preferences.getMyPreferenceInt(StageMode.this, "custom1_extraInfoBgColor", StaticVariables.grey);
        defextrainfotextcolor = preferences.getMyPreferenceInt(StageMode.this, "custom1_extraInfoTextColor", StaticVariables.white);
        lyricsTextColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsTextColor", StaticVariables.white);
        lyricsCapoColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsCapoColor", StaticVariables.red);
        lyricsBackgroundColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsBackgoundColour", StaticVariables.black);
        lyricsVerseColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsVerseColor", StaticVariables.black);
        lyricsChorusColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsChorusColor", StaticVariables.black);
        lyricsBridgeColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsBridgeColor", StaticVariables.black);
        lyricsCommentColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsCommentColor", StaticVariables.black);
        lyricsPreChorusColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsPreChorusColor", StaticVariables.black);
        lyricsTagColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsTagColor", StaticVariables.black);
        lyricsChordsColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsChordsColor", StaticVariables.yellow);
        lyricsCustomColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_lyricsCustomColor", StaticVariables.black);
        presoFontColor = preferences.getMyPreferenceInt(StageMode.this, "custom1_presoFontColor", StaticVariables.white);
    }

    private void setThemeCustom2() {
        defmetronomecolor = preferences.getMyPreferenceInt(StageMode.this, "custom2_metronomeColor", StaticVariables.darkishred);
        defpagebuttoncolor = preferences.getMyPreferenceInt(StageMode.this, "custom2_pageButtonsColor", StaticVariables.purplyblue);
        defstickytextcolor = preferences.getMyPreferenceInt(StageMode.this, "custom2_stickyTextColor", StaticVariables.black);
        defstickybgcolor = preferences.getMyPreferenceInt(StageMode.this, "custom2_stickyBackgroundColor", StaticVariables.lightyellow);
        defextrainfobgcolor = preferences.getMyPreferenceInt(StageMode.this, "custom2_extraInfoBgColor", StaticVariables.grey);
        defextrainfotextcolor = preferences.getMyPreferenceInt(StageMode.this, "custom2_extraInfoTextColor", StaticVariables.white);
        lyricsTextColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsTextColor", StaticVariables.black);
        lyricsCapoColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsCapoColor", StaticVariables.red);
        lyricsBackgroundColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsBackgoundColour", StaticVariables.white);
        lyricsVerseColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsVerseColor", StaticVariables.white);
        lyricsChorusColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsChorusColor", StaticVariables.white);
        lyricsBridgeColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsBridgeColor", StaticVariables.white);
        lyricsCommentColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsCommentColor", StaticVariables.white);
        lyricsPreChorusColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsPreChorusColor", StaticVariables.white);
        lyricsTagColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsTagColor", StaticVariables.white);
        lyricsChordsColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsChordsColor", StaticVariables.darkblue);
        lyricsCustomColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_lyricsCustomColor", StaticVariables.white);
        presoFontColor = preferences.getMyPreferenceInt(StageMode.this, "custom2_presoFontColor", StaticVariables.white);
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
        batteryholder = findViewById(R.id.batteryholder);
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
        backingtrackProgress.setOnClickListener(v -> PadFunctions.pauseOrResumePad());
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

        // Enable the song and author section to link to song details
        songandauthor.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "songdetails";
            openFragment();
        });
        // Enable the song and author section to link to edit song
        songandauthor.setOnLongClickListener(view -> {
            doEdit();
            return true;
        });
        batteryholder.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "actionbarinfo";
            openFragment();
        });
        // Give batteryholder long click the same action
        batteryholder.setOnLongClickListener(view -> {
            FullscreenActivity.whattodo = "actionbarinfo";
            openFragment();
            return true;
        });

        // Set up the navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        songmenu = findViewById(R.id.songmenu);
        optionmenu = findViewById(R.id.optionmenu);
        song_list_view = findViewById(R.id.song_list_view);
        FloatingActionButton closeSongsFAB = findViewById(R.id.closeSongsFAB);
        menuFolder_TextView = findViewById(R.id.menuFolder_TextView);
        menuFolder_TextView.setText(getString(R.string.wait));
        menuCount_TextView = findViewById(R.id.menuCount_TextView);
        LinearLayout changefolder_LinearLayout = findViewById(R.id.changefolder_LinearLayout);
        RelativeLayout fullSearchFABLayout = findViewById(R.id.fullSearchFABLayout);
        FloatingActionButton fullSearchFAB = findViewById(R.id.fullSearchFAB);
        RelativeLayout editSetFABLayout = findViewById(R.id.editSetFABLayout);
        FloatingActionButton editSetFAB = findViewById(R.id.editSetFAB);
        closeSongsFAB.setOnClickListener(view -> closeMyDrawers("song"));
        changefolder_LinearLayout.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "choosefolder";
            openFragment();
        });
        fullSearchFABLayout.setOnClickListener(view -> fullSearchFAB.performClick());
        fullSearchFAB.setOnClickListener(view -> {
            closeMyDrawers("song");
            FullscreenActivity.whattodo = "fullsearch";
            openFragment();
        });
        fullSearchFABLayout.setOnLongClickListener(view -> fullSearchFAB.performLongClick());
        fullSearchFAB.setOnLongClickListener(view -> {
            closeMyDrawers("song");
            FullscreenActivity.whattodo = "fullsearch";
            openFragment();
            return true;
        });
        editSetFABLayout.setOnClickListener(view -> editSetFAB.performClick());
        editSetFAB.setOnClickListener(view -> {
            closeMyDrawers("song");
            FullscreenActivity.whattodo = "editset";
            openFragment();
        });
        editSetFABLayout.setOnLongClickListener(view -> editSetFAB.performLongClick());
        editSetFAB.setOnLongClickListener(view -> {
            closeMyDrawers("song");
            FullscreenActivity.whattodo = "loadset";
            openFragment();
            return true;
        });
    }

    private void getBluetoothName() {
        // Only do this if we have the correct permissions
        if (permissions.hasNearbyPermissions(this)) {
            try {
                if (FullscreenActivity.mBluetoothAdapter == null) {
                    FullscreenActivity.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!permissions.checkForPermission(this,Manifest.permission.BLUETOOTH_CONNECT)) {
                        permissions.requestForPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 488);
                        return;
                    } else {
                        FullscreenActivity.mBluetoothName = FullscreenActivity.mBluetoothAdapter.getName();
                    }

                } else {
                    FullscreenActivity.mBluetoothName = FullscreenActivity.mBluetoothAdapter.getName();
                }
                if (FullscreenActivity.mBluetoothName == null) {
                    FullscreenActivity.mBluetoothName = "Unknown";
                }
            } catch (Exception e) {
                FullscreenActivity.mBluetoothName = "Unknown";
            }
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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

        Runnable testnavbar = () -> {
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> restoreTransparentBars());

            getWindow().getDecorView().setOnFocusChangeListener((v1, hasFocus) -> restoreTransparentBars());
        };

        Handler waitandtest = new Handler();
        waitandtest.postDelayed(testnavbar, 1000);
    }
    private void restoreTranslucentBarsDelayed() {
        // we restore it now and after 500 ms!
        restoreTransparentBars();
        restoreImmersiveModeHandler.postDelayed(restoreImmersiveModeRunnable, 500);
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
        Runnable delhide = () -> {
            // Hide them
            setWindowFlags();
            setWindowFlagsAdvanced();
            View rf = getCurrentFocus();
            if (rf!=null) {
                rf.clearFocus();
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
                // Set a runnable to return option menu to MAIN, This ensures 'Activated/Running' in sub menus work properly
                Handler resetoptionmenu = new Handler();
                resetoptionmenu.postDelayed(() -> {
                    StaticVariables.whichOptionMenu = "MAIN";
                    prepareOptionMenu();
                }, 100);
                // Set a runnable to re-enable swipe
                Handler allowswipe = new Handler();
                allowswipe.postDelayed(() -> {
                    FullscreenActivity.tempswipeSet = "enable"; // enable swipe after short delay
                }, FullscreenActivity.delayswipe_time); // 1800ms delay
                // Song index use will set this false.  Set true on drawer close.
                song_list_view.setFastScrollEnabled(true);
                hideActionBar();
                // Make sure all dynamic (scroll and set) buttons display
                onScrollAction();
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
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> restoreTransparentBars());

        decorView.setOnFocusChangeListener((v, hasFocus) -> restoreTransparentBars());

    }
    @Override
    public void adjustABInfo() {
        boolean inuse = false;
        try {
            // Change the visibilities
            if (preferences.getMyPreferenceBoolean(StageMode.this, "batteryDialOn", true)) {
                batteryimage.setVisibility(View.VISIBLE);
                inuse = true;
            } else {
                batteryimage.setVisibility(View.INVISIBLE);
            }
            if (preferences.getMyPreferenceBoolean(StageMode.this, "batteryTextOn", true)) {
                batterycharge.setVisibility(View.VISIBLE);
                inuse = true;
            } else {
                batterycharge.setVisibility(View.GONE);
            }
            if (preferences.getMyPreferenceBoolean(StageMode.this, "clockOn", true)) {
                digitalclock.setVisibility(View.VISIBLE);
                inuse = true;
            } else {
                digitalclock.setVisibility(View.GONE);
            }

            if (inuse) {
                // Set the text sizes
                batterycharge.setTextSize(preferences.getMyPreferenceFloat(StageMode.this, "batteryTextSize", 9.0f));
                digitalclock.setTextSize(preferences.getMyPreferenceFloat(StageMode.this, "clockTextSize", 9.0f));
                songtitle_ab.setTextSize(preferences.getMyPreferenceFloat(StageMode.this, "songTitleSize", 13.0f));
                songtitle_ab.setSingleLine(true);
                songcapo_ab.setTextSize(preferences.getMyPreferenceFloat(StageMode.this, "songTitleSize", 13.0f));
                songcapo_ab.setSingleLine(true);
                songauthor_ab.setTextSize(preferences.getMyPreferenceFloat(StageMode.this, "songAuthorSize", 11.0f));
                songauthor_ab.setSingleLine(true);
                songkey_ab.setTextSize(preferences.getMyPreferenceFloat(StageMode.this, "songTitleSize", 13.0f));
                songkey_ab.setSingleLine(true);

                // Set the time format
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df;
                if (preferences.getMyPreferenceBoolean(StageMode.this, "clock24hFormat", true)) {
                    df = new SimpleDateFormat("HH:mm", StaticVariables.locale);
                } else {
                    df = new SimpleDateFormat("h:mm", StaticVariables.locale);
                }
                String formattedTime = df.format(c.getTime());
                digitalclock.setText(formattedTime);
                batteryholder.setVisibility(View.VISIBLE);
            } else {
                batteryholder.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUpBatteryMonitor() {
        // Get clock
        try {
            boolean inuse = false;
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
                inuse = true;
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
                inuse = true;
            } else {
                batterycharge.setVisibility(View.GONE);
            }
            batterycharge.setTextSize(preferences.getMyPreferenceFloat(StageMode.this, "batteryTextSize",9.0f));
            batterycharge.setText(charge);
            int abh = ab.getHeight();
            StaticVariables.ab_height = abh;
            if (preferences.getMyPreferenceBoolean(StageMode.this,"batteryDialOn",true)) {
                batteryimage.setVisibility(View.VISIBLE);
                inuse = true;
            } else {
                batteryimage.setVisibility(View.INVISIBLE);
            }
            if (ab != null && abh > 0) {
                BitmapDrawable bmp = BatteryMonitor.batteryImage(StageMode.this, preferences,i, abh);
                batteryimage.setImageDrawable(bmp);
            }
            if (inuse) {
                batteryholder.setVisibility(View.VISIBLE);
            } else {
                batteryholder.setVisibility(View.GONE);
            }

            // Ask the app to check again in 60s
            Handler batterycheck = new Handler();
            batterycheck.postDelayed(this::setUpBatteryMonitor, 60000);
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
        super.onNewIntent(intent);
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


    // Nearby
    // These are dealt with in NearbyConnections.  Pulled in from interface to listen from optionmenulistener
    @Override
    public void startDiscovery() {
        nearbyConnections.startDiscovery();
    }
    @Override
    public void startAdvertising() {
        nearbyConnections.startAdvertising();
    }
    @Override
    public void stopDiscovery() {
        nearbyConnections.stopDiscovery();
    }
    @Override
    public void stopAdvertising() {
        nearbyConnections.stopAdvertising();
    }
    @Override
    public void turnOffNearby() {
        nearbyConnections.turnOffNearby();
    }

    @Override
    public String getUserNickname() {
        return nearbyConnections.getUserNickname();
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class CheckStorage extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (permissions!=null && Build.VERSION.SDK_INT<=Build.VERSION_CODES.P &&
                        !permissions.checkForPermission(StageMode.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        }

        // Add long press actions
        menuButtonLongPressActions();

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
    }

    @Override
    public void onStart() {
        super.onStart();
        StaticVariables.activity = StageMode.this;
        FullscreenActivity.appRunning = true;
        if (mMediaRouter != null && mMediaRouteSelector != null) {
            try {
                StaticVariables.infoBarChangeRequired = true;
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
            Log.d(TAG, "Problem removing mediaroutercallback");
        }

        if (br!=null) {
            try {
                StageMode.this.unregisterReceiver(br);
            } catch (Exception e) {
                Log.d(TAG, "Battery receiver not registered, so no need to unregister");
            }
        }
        tryCancelAsyncTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticVariables.activity = StageMode.this;
        FullscreenActivity.appRunning = true;
        resizeDrawers();
        // Fix the page flags
        setWindowFlags();
        setWindowFlagsAdvanced();
        FullscreenActivity.needtorefreshsongmenu = true;
        prepareSongMenu();
        // Add action bar menu long press actions
        // menuButtonLongPressActions is too quick when resuming
        // We are not sure when it becomes stable - so keep trying
        new Handler().postDelayed(this::menuButtonLongPressActions, 200);
        new Handler().postDelayed(this::menuButtonLongPressActions, 400);
        new Handler().postDelayed(this::menuButtonLongPressActions, 600);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            FullscreenActivity.whichPad = 0;
            // IV - Stop autoscroll as it breaks on pause
            if (StaticVariables.isautoscrolling) {
                stopAutoScroll();
            }
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
                Log.d(TAG, "Battery monitor not registered anymore");
            }
        }
        tryCancelAsyncTasks();
        if (songscrollview !=null) {
            songscrollview.removeAllViews();
        }

        // Second screen
        try {
            CastRemoteDisplayLocalService.stopService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (FullscreenActivity.hdmi!=null) {
                FullscreenActivity.hdmi.dismiss();
                FullscreenActivity.hdmi = null;
            }
        } catch (Exception e) {
            // Ooops
            e.printStackTrace();
        }
        nearbyConnections.turnOffNearby();
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
        doCancelAsyncTask(resize_drawers);
        doCancelAsyncTask(do_moveinset);
        doCancelAsyncTask(add_slidetoset);
        doCancelAsyncTask(dualscreenwork_async);
        doCancelAsyncTask(play_pads);
        doCancelAsyncTask(do_download);
        doCancelAsyncTask(show_sticky);
        doCancelAsyncTask(show_highlight);
        doCancelAsyncTask(get_scrollheight);
    }
    private void doCancelAsyncTask(AsyncTask<?,?,?> ast) {
        try {
            if (ast!=null) {
                ast.cancel(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void installPlayServices() {
        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.play_services_error,
                BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.play_services_how, v -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_services_help)));
            startActivity(i);
        })
                .show();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
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

            invalidateOptionsMenu();
            closeMyDrawers("both");
            resizeDrawers();
            loadSong();
        }
    }

    private void sendAutoscrollTriggerToConnected() {
        String infoPayload = "autoscroll_";

        if (StaticVariables.isautoscrolling) {
            infoPayload += "start";
        } else {
            infoPayload += "stop";
        }
        nearbyConnections.doSendPayloadBytes(infoPayload);
    }

    // Needed to support send activty from within runnable
    private void sendSongToConnected () {
        // IV - The send is always called by the 'if' and will return true if a large file has been sent
        if (nearbyConnections.sendSongPayload()) {
            StaticVariables.myToastMessage = (getString(R.string.nearby_large_file));
            Handler h = new Handler();
            h.post(() -> ShowToast.showToast(StageMode.this));
        }
    }

    private void sendSongSectionToConnected() {
        // IV - Do not send section 0 payload when loading a song
        if (!FullscreenActivity.alreadyloading) {
            String infoPayload = "___section___" + StaticVariables.currentSection;
            nearbyConnections.doSendPayloadBytes(infoPayload);
        }
    }

    private void sendSongSectionForPendingToConnected() {
        // IV - Send a pending section change to the client (-ve offset by 1)
        String infoPayload = "___section___-" + (1 + StaticVariables.currentSection);
        nearbyConnections.doSendPayloadBytes(infoPayload);
    }

    @Override
    public void shareSong() {
        doCancelAsyncTask(sharesong_async);
        sharesong_async = new ShareSong();
        try {
            sharesong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
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
        setupPageButtons();

        // IV - Update second screen theme
        if (FullscreenActivity.isPresenting && !FullscreenActivity.isHDMIConnected) {
            PresentationService.ExternalDisplay.presenterThemeSetUp();
        }
        if (FullscreenActivity.isHDMIConnected) {
            PresentationServiceHDMI.presenterThemeSetUp();
        }
        // Load the song
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
        FullscreenActivity.needtorefreshsongmenu = false;
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

    // IV - Expanded to handle both scroll and set buttons
    private void scrollButtons() {
        delaycheckscroll = new Handler();
        checkScrollPosition = () -> {
            if (!StaticVariables.isautoscrolling) { // GE Added as this was breaking the autoscroll - grabbing the rounded pixel value
                FullscreenActivity.newPosFloat = songscrollview.getScrollY();
            }

            // IV - Added handling for multi-page PDF
            // Use checkCanScroll results
            // IV - Made transparent so that they remain active click areas on the screen
            if (!preferences.getMyPreferenceBoolean(StageMode.this, "pageButtonShowScroll", true)) {
                scrollDownButton.setAlpha(0.0f);
                scrollUpButton.setAlpha(0.0f);
            } else {
                // Get the default alpha value
                float val = preferences.getMyPreferenceFloat(StageMode.this, "pageButtonAlpha", 0.5f);
                scrollDownButton.setAlpha(val);
                scrollUpButton.setAlpha(val);
            }
            if (checkCanScrollDown() || (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent < (FullscreenActivity.pdfPageCount - 1))) {
                scrollDownButton.setVisibility(View.VISIBLE);
            } else {
                scrollDownButton.setVisibility(View.GONE);
            }
            if (checkCanScrollUp() || (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0)) {
                scrollUpButton.setVisibility(View.VISIBLE);
            } else {
                scrollUpButton.setVisibility(View.GONE);
            }

            if (preferences.getMyPreferenceBoolean(StageMode.this, "pageButtonShowSetMove", true) && StaticVariables.setView ) {
                // Use checkCanGoTo results
                showFAB(setBackButton, StaticVariables.canGoToPrevious);
                showFAB(setForwardButton, StaticVariables.canGoToNext);
            } else {
                showFAB(setBackButton, false);
                showFAB(setForwardButton, false);
            }
        };
    }

    //@Override
    public void setupPageButtons() {
        runOnUiThread(() -> {
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
            mainbuttons = findViewById(R.id.mainbuttons);
            mainbuttons.setVisibility(View.GONE);
            extrabuttons = findViewById(R.id.extrabuttons);
            extrabuttons.setVisibility(View.GONE);
            extrabuttons2 = findViewById(R.id.extrabuttons2);
            extrabuttons2.setVisibility(View.GONE);
            scrollDownButton = findViewById(R.id.scrollDownButton);
            scrollUpButton = findViewById(R.id.scrollUpButton);
            setBackButton = findViewById(R.id.setBackButton);
            setForwardButton = findViewById(R.id.setForwardButton);
            setButtonLayout = findViewById(R.id.setButtonLayout);
            padButtonLayout = findViewById(R.id.padButtonLayout);
            autoscrollButtonLayout = findViewById(R.id.autoscrollButtonLayout);
            metronomeButtonLayout = findViewById(R.id.metronomeButtonLayout);
            extraButtonLayout = findViewById(R.id.extraButtonLayout);
            chordButtonLayout = findViewById(R.id.chordButtonLayout);
            stickyButtonLayout = findViewById(R.id.stickyButtonLayout);
            notationButtonLayout = findViewById(R.id.notationButtonLayout);
            highlightButtonLayout = findViewById(R.id.highlightButtonLayout);
            pageselectButtonLayout = findViewById(R.id.pageselectButtonLayout);
            linkButtonLayout = findViewById(R.id.linkButtonLayout);
            chordButton_ungroupedLayout = findViewById(R.id.chordButton_ungroupedLayout);
            stickyButton_ungroupedLayout = findViewById(R.id.stickyButton_ungroupedLayout);
            notationButton_ungroupedLayout = findViewById(R.id.notationButton_ungroupedLayout);
            highlightButton_ungroupedLayout = findViewById(R.id.highlightButton_ungroupedLayout);
            pageselectButton_ungroupedLayout = findViewById(R.id.pageselectButton_ungroupedLayout);
            linkButton_ungroupedLayout = findViewById(R.id.linkButton_ungroupedLayout);
            customButtonLayout = findViewById(R.id.customButtonLayout);
            custom1ButtonLayout = findViewById(R.id.custom1ButtonLayout);
            custom2ButtonLayout = findViewById(R.id.custom2ButtonLayout);
            custom3ButtonLayout = findViewById(R.id.custom3ButtonLayout);
            custom4ButtonLayout = findViewById(R.id.custom4ButtonLayout);
            custom1Button_ungroupedLayout = findViewById(R.id.custom1Button_ungroupedLayout);
            custom2Button_ungroupedLayout = findViewById(R.id.custom2Button_ungroupedLayout);
            custom3Button_ungroupedLayout = findViewById(R.id.custom3Button_ungroupedLayout);
            custom4Button_ungroupedLayout = findViewById(R.id.custom4Button_ungroupedLayout);
            scrollDownButtonLayout = findViewById(R.id.scrollDownButtonLayout);
            scrollUpButtonLayout = findViewById(R.id.scrollUpButtonLayout);
            setBackButtonLayout = findViewById(R.id.setBackButtonLayout);
            setForwardButtonLayout = findViewById(R.id.setForwardButtonLayout);
            setupPageButtonsColors();
            setupQuickLaunchButtons();
        });

        // Decide if we are grouping / tidying page buttons
        groupPageButtons();

        // Set the sizes and the alphas
        pageButtonAlpha("");

        // IV - No animations for grouped buttons as the collapse of the group will 'animate' the click
        // Set the listeners
        setButtonLayout.setOnClickListener(view -> setButton.performClick());
        setButton.setOnClickListener(view -> {
            CustomAnimations.animateFAB(setButton,StageMode.this);
            FullscreenActivity.whattodo = "editset";
            openFragment();
        });
        setButtonLayout.setOnLongClickListener(view -> setButton.performLongClick());
        setButton.setOnLongClickListener(view -> {
            FullscreenActivity.whattodo = "setitemvariation";
            openFragment();
            return true;
        });
        padButtonLayout.setOnClickListener(view -> padButton.performClick());
        padButton.setOnClickListener(view -> {
            CustomAnimations.animateFAB(padButton,StageMode.this);
            FullscreenActivity.whattodo = "page_pad";
            openFragment();
        });
        padButtonLayout.setOnLongClickListener(view -> padButton.performLongClick());
        padButton.setOnLongClickListener(view -> {
            // Vibrate to let the user know something happened
            DoVibrate.vibrate(StageMode.this, 50);
            CustomAnimations.animateFABLong(padButton,StageMode.this);
            // IV - Indicate a fade with just the pad icon to give immediate feedback
            if (backingtrackProgress.getVisibility() == View.VISIBLE) {
                padtotalTime_TextView.setText("");
                padTimeSeparator_TextView.setText("");
                padcurrentTime_TextView.setText("");
            }
            gesture6();
            return true;
        });
        autoscrollButtonLayout.setOnClickListener(view -> autoscrollButton.performClick());
        autoscrollButton.setOnClickListener(view -> {
            CustomAnimations.animateFAB(autoscrollButton,StageMode.this);
            FullscreenActivity.whattodo = "page_autoscroll";
            openFragment();
        });
        autoscrollButtonLayout.setOnLongClickListener(view -> autoscrollButton.performLongClick());
        autoscrollButton.setOnLongClickListener(view -> {
            // Vibrate to let the user know something happened
            DoVibrate.vibrate(StageMode.this, 50);
            CustomAnimations.animateFABLong(autoscrollButton,StageMode.this);
            gesture5();
            return true;
        });
        metronomeButtonLayout.setOnClickListener(view -> metronomeButton.performClick());
        metronomeButton.setOnClickListener(view -> {
            CustomAnimations.animateFAB(metronomeButton,StageMode.this);
            FullscreenActivity.whattodo = "page_metronome";
            openFragment();
        });
        metronomeButtonLayout.setOnLongClickListener(view -> metronomeButton.performLongClick());
        metronomeButton.setOnLongClickListener(view -> {
            // Vibrate to let the user know something happened
            DoVibrate.vibrate(StageMode.this, 50);
            CustomAnimations.animateFABLong(metronomeButton,StageMode.this);
            gesture7();
            return true;
        });
        highlightButtonLayout.setOnClickListener(view -> highlightButton.performClick());
        highlightButton.setOnClickListener(view -> {
            FullscreenActivity.highlightOn = !FullscreenActivity.highlightOn;
            FullscreenActivity.whattodo = "page_highlight";
            displayHighlight(false);
        });
        highlightButtonLayout.setOnLongClickListener(view -> highlightButton.performLongClick());
        highlightButton.setOnLongClickListener(view -> {
            // Vibrate to let the user know something happened
            DoVibrate.vibrate(StageMode.this, 50);
            takeScreenShot();
            if (FullscreenActivity.bmScreen!=null) {
                FullscreenActivity.whattodo = "drawnotes";
                openFragment();
            } else {
                Log.d(TAG, "screenshot is null");
            }
            return true;
        });
        highlightButton_ungroupedLayout.setOnClickListener(view -> highlightButton_ungrouped.performClick());
        highlightButton_ungrouped.setOnClickListener(view -> {
            CustomAnimations.animateFAB(highlightButton_ungrouped,StageMode.this);
            FullscreenActivity.whattodo = "page_highlight";
            FullscreenActivity.highlightOn = !FullscreenActivity.highlightOn;
            displayHighlight(false);
        });
        highlightButton_ungroupedLayout.setOnLongClickListener(view -> highlightButton_ungrouped.performLongClick());
        highlightButton_ungrouped.setOnLongClickListener(view -> {
            CustomAnimations.animateFABLong(highlightButton_ungrouped,StageMode.this);
            // Vibrate to let the user know something happened
            DoVibrate.vibrate(StageMode.this, 50);
            takeScreenShot();
            if (FullscreenActivity.bmScreen!=null) {
                FullscreenActivity.whattodo = "drawnotes";
                openFragment();
            } else {
                Log.d(TAG, "screenshot is null");
            }
            return true;
        });
        extraButtonLayout.setOnClickListener(view -> extraButton.performClick());
        extraButton.setOnClickListener(view -> {
            CustomAnimations.animateFAB(extraButton,StageMode.this);
            if (extrabuttons!=null && extrabuttons.getVisibility() == View.GONE) {
                pageButtonAlpha("extra");
            } else {
                pageButtonAlpha("");
            }
        });
        // Button groups - extra group long click action to switch to all group mode (collapse of the buttons displayed)
        extraButtonLayout.setOnLongClickListener(view -> extraButton.performLongClick());
        extraButton.setOnLongClickListener(view -> {
            CustomAnimations.animateFAB(extraButton,StageMode.this);
            preferences.setMyPreferenceBoolean(StageMode.this, "pageButtonGroupMain", !(preferences.getMyPreferenceBoolean(StageMode.this, "pageButtonGroupMain", false)));
            // Need to redo OnClick button action
            setupPageButtons();
            groupPageButtons();
            // Make sure all dynamic (scroll and set) buttons display
            onScrollAction();
            return true;
        });
        chordButtonLayout.setOnClickListener(view -> chordButton.performClick());
        chordButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "page_chords";
            openFragment();
        });
        chordButton_ungroupedLayout.setOnClickListener(view -> chordButton_ungrouped.performClick());
        chordButton_ungrouped.setOnClickListener(view -> {
            CustomAnimations.animateFAB(chordButton_ungrouped,StageMode.this);
            FullscreenActivity.whattodo = "page_chords";
            openFragment();
        });
        linkButtonLayout.setOnClickListener(view -> linkButton.performClick());
        linkButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "page_links";
            openFragment();
        });
        linkButton_ungroupedLayout.setOnClickListener(view -> linkButton_ungrouped.performClick());
        linkButton_ungrouped.setOnClickListener(view -> {
            CustomAnimations.animateFAB(linkButton_ungrouped,StageMode.this);
            FullscreenActivity.whattodo = "page_links";
            openFragment();
        });
        stickyButtonLayout.setOnClickListener(view -> stickyButton.performClick());
        stickyButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "page_sticky";
            displaySticky(false);
        });
        stickyButtonLayout.setOnLongClickListener(view -> stickyButton.performLongClick());
        stickyButton.setOnLongClickListener(view -> {
            FullscreenActivity.whattodo = "page_sticky";
            openFragment();
            return true;
        });
        stickyButton_ungroupedLayout.setOnClickListener(view -> stickyButton_ungrouped.performClick());
        stickyButton_ungrouped.setOnClickListener(view -> {
            CustomAnimations.animateFAB(stickyButton_ungrouped,StageMode.this);
            FullscreenActivity.whattodo = "page_sticky";
            displaySticky(false);
        });
        stickyButton_ungroupedLayout.setOnLongClickListener(view -> stickyButton_ungrouped.performLongClick());
        stickyButton_ungrouped.setOnLongClickListener(view -> {
            CustomAnimations.animateFAB(stickyButton,StageMode.this);
            FullscreenActivity.whattodo = "page_sticky";
            openFragment();
            return true;
        });
        notationButtonLayout.setOnClickListener(view -> notationButton.performClick());
        notationButton.setOnClickListener(view -> {
            if (StaticVariables.mNotation.equals("")) {
                FullscreenActivity.whattodo = "abcnotation_edit";
            } else {
                FullscreenActivity.whattodo = "abcnotation";
            }
            openFragment();
        });
        notationButtonLayout.setOnLongClickListener(view -> notationButton.performLongClick());
        notationButton.setOnLongClickListener(view -> {
            FullscreenActivity.whattodo = "abcnotation_edit";
            openFragment();
            return true;
        });
        notationButton_ungroupedLayout.setOnClickListener(view -> notationButton_ungrouped.performClick());
        notationButton_ungrouped.setOnClickListener(view -> {
            CustomAnimations.animateFAB(notationButton_ungrouped,StageMode.this);
            if (StaticVariables.mNotation.equals("")) {
                FullscreenActivity.whattodo = "abcnotation_edit";
            } else {
                FullscreenActivity.whattodo = "abcnotation";
            }
            openFragment();
        });
        notationButton_ungroupedLayout.setOnLongClickListener(view -> notationButton_ungrouped.performLongClick());
        notationButton_ungrouped.setOnLongClickListener(view -> {
            CustomAnimations.animateFAB(notationButton_ungrouped,StageMode.this);
            FullscreenActivity.whattodo = "abcnotation_edit";
            openFragment();
            return true;
        });
        pageselectButtonLayout.setOnClickListener(view -> pageselectButton.performClick());
        pageselectButton.setOnClickListener(view -> {
            if (FullscreenActivity.isPDF) {
                FullscreenActivity.whattodo = "page_pageselect";
                openFragment();
            } else {
                StaticVariables.myToastMessage = getResources().getString(R.string.not_allowed);
                ShowToast.showToast(StageMode.this);
            }
        });
        pageselectButton_ungroupedLayout.setOnClickListener(view -> pageselectButton_ungrouped.performClick());
        pageselectButton_ungrouped.setOnClickListener(view -> {
            if (FullscreenActivity.isPDF) {
                FullscreenActivity.whattodo = "page_pageselect";
                openFragment();
            } else {
                StaticVariables.myToastMessage = getResources().getString(R.string.not_allowed);
                ShowToast.showToast(StageMode.this);
            }
        });
        customButtonLayout.setOnClickListener(view -> customButton.performClick());
        if (preferences.getMyPreferenceBoolean(StageMode.this,"pageButtonGroupMain",false)) {
            customButton.setOnClickListener(view -> {
                CustomAnimations.animateFAB(customButton,StageMode.this);
                FullscreenActivity.whattodo = "groupedpagebuttons";
                openFragment();
            });
        } else {
            customButton.setOnClickListener(view -> {
                CustomAnimations.animateFAB(customButton,StageMode.this);
                if (extrabuttons2!=null && extrabuttons2.getVisibility() == View.GONE) {
                    pageButtonAlpha("custom");
                } else {
                    pageButtonAlpha("");
                }
            });
        }
        // Button groups - custom Long press toggles all group mode - allows expansion and collapse of buttons display
        customButtonLayout.setOnLongClickListener(view -> customButton.performLongClick());
        customButton.setOnLongClickListener(view -> {
            // This provides the ability to expand and collapse the number of buttons displayed.
            // If extra or custom groupings are 'hidden' by an all grouping support long press to switch into and out of all grouping
            if ((preferences.getMyPreferenceBoolean(StageMode.this, "pageButtonGroupExtra", false) || preferences.getMyPreferenceBoolean(StageMode.this, "pageButtonGroupCustom", false))) {
                preferences.setMyPreferenceBoolean(StageMode.this, "pageButtonGroupMain", !(preferences.getMyPreferenceBoolean(StageMode.this, "pageButtonGroupMain", false)));
                // Need to redo OnClick button action
                setupPageButtons();
                groupPageButtons();
                // Make sure all dynamic (scroll and set) buttons display
                onScrollAction();
            } else {
                CustomAnimations.animateFAB(customButton, StageMode.this);
                FullscreenActivity.whattodo = "groupedpagebuttons";
                openFragment();
            }
            return true;
        });
        scrollUpButtonLayout.setOnClickListener(view -> scrollUpButton.performClick());
        scrollUpButton.setOnClickListener(view -> {
            CustomAnimations.animateFAB(scrollUpButton,StageMode.this);
            doScrollUp();
        });
        scrollDownButtonLayout.setOnClickListener(view -> scrollDownButton.performClick());
        scrollDownButton.setOnClickListener(view -> {
            CustomAnimations.animateFAB(scrollDownButton,StageMode.this);
            doScrollDown();
        });
        setForwardButtonLayout.setOnClickListener(view -> setForwardButton.performClick());
        setForwardButton.setOnClickListener(view -> {
            // Animate but not if called by R2L swipe
            if (!(StaticVariables.setMoveDirection.equals("swipe"))) {
                CustomAnimations.animateFAB(setForwardButton, StageMode.this);
            }
            StaticVariables.setMoveDirection = "forward";
            FullscreenActivity.whichDirection = "R2L";
            goToNextItem();
        });
        setBackButtonLayout.setOnClickListener(view -> setBackButton.performClick());
        setBackButton.setOnClickListener(view -> {
            // Animate but not if called by  a L2R swipe
            if (!(StaticVariables.setMoveDirection.equals("swipe"))) {
                CustomAnimations.animateFAB(setBackButton, StageMode.this);
            }
            StaticVariables.setMoveDirection = "back";
            FullscreenActivity.whichDirection = "L2R";
            goToPreviousItem();
        });
    }

    @Override
    public void setupPageButtonsColors() {
        // Set the colors
        autoscrollButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        chordButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        chordButton_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom1Button.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom1Button_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom2Button.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom2Button_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom3Button.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom3Button_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom4Button.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        custom4Button_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        customButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        extraButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        highlightButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        highlightButton_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        linkButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        linkButton_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        metronomeButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        notationButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        notationButton_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        padButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        pageselectButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        pageselectButton_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        scrollDownButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        scrollUpButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        setBackButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        setButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        setForwardButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        stickyButton.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
        stickyButton_ungrouped.setBackgroundTintList(ColorStateList.valueOf(defpagebuttoncolor));
    }

    // IV - This is called wherever we need to make sure scroll and set dynamic buttons are displayed
    public void onScrollAction() {
        // Display the scroll and set dynamic buttons as needed
        checkCanGoTo();
        delaycheckscroll.post(checkScrollPosition);
    }

    private void doScrollUp() {
        // Temporarily pause any running autoscroll
        pauseAutoscroll();

        dealtwithaspdf = false;

        if (!FullscreenActivity.alreadyloading) {
            if (FullscreenActivity.isPDF && !checkCanScrollUp() && (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0)) {
                FullscreenActivity.pdfPageCurrent = FullscreenActivity.pdfPageCurrent - 1;
                StaticVariables.currentSection = FullscreenActivity.pdfPageCurrent;
                // Send page number as 'section' to other devices
                if (StaticVariables.whichMode.equals("Stage") && StaticVariables.isHost && StaticVariables.isConnected) {
                    sendSongSectionToConnected();
                }
                // IV - Indicate reload which does not impact running pad etc.
                StaticVariables.reloadOfSong = true;
                loadSong();
                dealtwithaspdf = true;
            }
        }

        // Scroll the screen up
        if (!dealtwithaspdf) {
            if (StaticVariables.whichMode.equals("Stage") && FullscreenActivity.isSong) {
                try {
                    StaticVariables.currentSection -= 1;
                    selectSection(StaticVariables.currentSection);
                } catch (Exception e) {
                    StaticVariables.currentSection += 1;
                    e.printStackTrace();
                }
                // Make sure all dynamic (scroll and set) buttons display
                onScrollAction();
            } else {
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
                            (int) (preferences.getMyPreferenceFloat(StageMode.this, "scrollDistance", 0.7f) * (
                                    metrics.heightPixels - barheight));
                    animator = ObjectAnimator.ofInt(glideimage_ScrollView, "scrollY", glideimage_ScrollView.getScrollY(), (int) FullscreenActivity.newPosFloat);
                } else {
                    FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() -
                            (int) (preferences.getMyPreferenceFloat(StageMode.this, "scrollDistance", 0.7f) *
                                    (metrics.heightPixels - barheight));
                    animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
                }

                Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
                animator.setInterpolator(customInterpolator);
                animator.setDuration(preferences.getMyPreferenceInt(StageMode.this, "scrollSpeed", 1500));
                animator.start();

                // Set a runnable to check the scroll position after 1 second
                delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
                hideActionBar();
            }
        }
    }

    private void doScrollDown() {
        // Temporarily pause any running autoscroll
        pauseAutoscroll();

        dealtwithaspdf = false;

        if (!FullscreenActivity.alreadyloading) {
            if (FullscreenActivity.isPDF && !checkCanScrollDown() && (FullscreenActivity.pdfPageCurrent < (FullscreenActivity.pdfPageCount - 1))) {
                FullscreenActivity.pdfPageCurrent = FullscreenActivity.pdfPageCurrent + 1;
                StaticVariables.currentSection = FullscreenActivity.pdfPageCurrent;
                // Send page number as 'section' to other devices
                if (StaticVariables.whichMode.equals("Stage") && StaticVariables.isHost && StaticVariables.isConnected) {
                    sendSongSectionToConnected();
                }
                // IV - Indicate reload which does not impact running pad etc.
                StaticVariables.reloadOfSong = true;
                loadSong();
                dealtwithaspdf = true;
            }
        }

        if (!dealtwithaspdf) {
            if (StaticVariables.whichMode.equals("Stage") && FullscreenActivity.isSong) {
                if (StaticVariables.currentSection == StaticVariables.songSections.length - 1) {
                    // We are at the end of the song
                    Log.d(TAG, "End of the song");
                } else {
                    try {
                        StaticVariables.currentSection += 1;
                        selectSection(StaticVariables.currentSection);
                    } catch (Exception e) {
                        StaticVariables.currentSection -= 1;
                    }
                }
                // Make sure all dynamic (scroll and set) buttons display
                onScrollAction ();
            } else {
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
                            (int) (preferences.getMyPreferenceFloat(StageMode.this, "scrollDistance", 0.7f) *
                                    (metrics.heightPixels - barheight));
                    animator = ObjectAnimator.ofInt(glideimage_ScrollView, "scrollY", glideimage_ScrollView.getScrollY(), (int) FullscreenActivity.newPosFloat);
                } else {
                    FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() +
                            (int) (preferences.getMyPreferenceFloat(StageMode.this, "scrollDistance", 0.7f) *
                                    (metrics.heightPixels - barheight));
                    animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
                }

                Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
                animator.setInterpolator(customInterpolator);
                animator.setDuration(preferences.getMyPreferenceInt(StageMode.this, "scrollSpeed", 1500));
                animator.start();

                // Set a runnable to check the scroll position after 1 second
                delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
                hideActionBar();
            }
        }
    }

    private void showFAB(final View fab, boolean show) {
        // Could use the default show() and hide() but the default animation keeps getting fired
        // Treating them as a generic view fixes that!
        if (show) {
            fab.setAlpha(preferences.getMyPreferenceFloat(StageMode.this,"pageButtonAlpha",0.5f));
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);

        }
    }
    public void groupPageButtons() {
        // Hide activity buttons to begin with
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

            // Set the on value (if a popup is running)
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

            mainbuttons.setVisibility(View.VISIBLE);
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
        custom1ButtonLayout.setOnClickListener(view -> custom1Button.performClick());
        custom1Button.setOnClickListener(view -> customButtonAction(b1ac));
        custom2ButtonLayout.setOnClickListener(view -> custom2Button.performClick());
        custom2Button.setOnClickListener(view -> customButtonAction(b2ac));
        custom3ButtonLayout.setOnClickListener(view -> custom3Button.performClick());
        custom3Button.setOnClickListener(view -> customButtonAction(b3ac));
        custom4ButtonLayout.setOnClickListener(view -> custom4Button.performClick());
        custom4Button.setOnClickListener(view -> customButtonAction(b4ac));
        custom1Button_ungroupedLayout.setOnClickListener(view -> custom1Button_ungrouped.performClick());
        custom1Button_ungrouped.setOnClickListener(view -> {
            CustomAnimations.animateFAB(custom1Button_ungrouped,StageMode.this);
            customButtonAction(b1ac);
        });
        custom2Button_ungroupedLayout.setOnClickListener(view -> custom2Button_ungrouped.performClick());
        custom2Button_ungrouped.setOnClickListener(view -> {
            CustomAnimations.animateFAB(custom2Button_ungrouped,StageMode.this);
            customButtonAction(b2ac);
        });
        custom3Button_ungroupedLayout.setOnClickListener(view -> custom3Button_ungrouped.performClick());
        custom3Button_ungrouped.setOnClickListener(view -> {
            CustomAnimations.animateFAB(custom3Button_ungrouped,StageMode.this);
            customButtonAction(b3ac);
        });
        custom4Button_ungroupedLayout.setOnClickListener(view -> custom4Button_ungrouped.performClick());
        custom4Button_ungrouped.setOnClickListener(view -> {
            CustomAnimations.animateFAB(custom4Button_ungrouped,StageMode.this);
            customButtonAction(b4ac);
        });

        // Support for custom button LongClick actions
        custom1ButtonLayout.setOnLongClickListener(view -> custom1Button.performLongClick());
        custom1Button.setOnLongClickListener(view -> {
            customButtonLongPressAction(b1ac);
            return true;
        });
        custom2ButtonLayout.setOnLongClickListener(view -> custom2Button.performLongClick());
        custom2Button.setOnLongClickListener(view -> {
            customButtonLongPressAction(b2ac);
            return true;
        });
        custom3ButtonLayout.setOnLongClickListener(view -> custom3Button.performLongClick());
        custom3Button.setOnLongClickListener(view -> {
            customButtonLongPressAction(b3ac);
            return true;
        });
        custom4ButtonLayout.setOnLongClickListener(view -> custom4Button.performLongClick());
        custom4Button.setOnLongClickListener(view -> {
            customButtonLongPressAction(b4ac);
            return true;
        });
        custom1Button_ungroupedLayout.setOnLongClickListener(view -> custom1Button_ungrouped.performLongClick());
        custom1Button_ungrouped.setOnLongClickListener(view -> {
            CustomAnimations.animateFAB(custom1Button_ungrouped, StageMode.this);
            customButtonLongPressAction(b1ac);
            return true;
        });
        custom2Button_ungroupedLayout.setOnLongClickListener(view -> custom2Button_ungrouped.performLongClick());
        custom2Button_ungrouped.setOnLongClickListener(view -> {
            CustomAnimations.animateFAB(custom2Button_ungrouped, StageMode.this);
            customButtonLongPressAction(b2ac);
            return true;
        });
        custom3Button_ungroupedLayout.setOnLongClickListener(view -> custom3Button_ungrouped.performLongClick());
        custom3Button_ungrouped.setOnLongClickListener(view -> {
            CustomAnimations.animateFAB(custom3Button_ungrouped, StageMode.this);
            customButtonLongPressAction(b3ac);
            return true;
        });
        custom4Button_ungroupedLayout.setOnLongClickListener(view -> custom4Button_ungrouped.performLongClick());
        custom4Button_ungrouped.setOnLongClickListener(view -> {
            CustomAnimations.animateFAB(custom4Button_ungrouped, StageMode.this);
            customButtonLongPressAction(b4ac);
            return true;
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
                FullscreenActivity.whattodo = s;
                openFragment();
                break;

            case "showchords":
                // Button cycle is: on -> off -> on
                val = preferences.getMyPreferenceBoolean(StageMode.this,"displayChords",true);
                preferences.setMyPreferenceBoolean(StageMode.this,"displayChords",!val);
                // Flag for a reload
                StaticVariables.reloadOfSong = true;
                loadSong();
                break;

            case "showcapo":
                // Button cycle is: Native on and Capo off -> Capo else Native on -> both Native and Capo on  -> Native on and Capo off
                if (preferences.getMyPreferenceBoolean(StageMode.this, "displayChords", true)) {
                    if (preferences.getMyPreferenceBoolean(StageMode.this, "displayCapoChords", true)) {
                        if (preferences.getMyPreferenceBoolean(StageMode.this, "displayCapoAndNativeChords", true)) {
                            preferences.setMyPreferenceBoolean(StageMode.this, "displayCapoAndNativeChords", false);
                            preferences.setMyPreferenceBoolean(StageMode.this, "displayCapoChords", false);
                        } else {
                            preferences.setMyPreferenceBoolean(StageMode.this, "displayCapoAndNativeChords", true);
                        }
                    } else {
                        preferences.setMyPreferenceBoolean(StageMode.this, "displayCapoChords", true);
                    }
                    // Flag for a reload
                    StaticVariables.reloadOfSong = true;
                    loadSong();
                } else {
                    StaticVariables.myToastMessage = getResources().getString(R.string.showchords) + " - " + getResources().getString(R.string.notset) + "!";
                    ShowToast.showToast(StageMode.this);
                }
                break;

            case "showlyrics":
                val = preferences.getMyPreferenceBoolean(StageMode.this,"displayLyrics",true);
                preferences.setMyPreferenceBoolean(StageMode.this,"displayLyrics",!val);
                // Flag for a reload
                StaticVariables.reloadOfSong = true;
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

        pageButtonAlpha("");
    }

    private void customButtonLongPressAction(String s) {
        switch (s) {
            case "":
            default:
                FullscreenActivity.whattodo = "quicklaunch";
                openFragment();
                break;

            case "editsong":
            case "editsongpdf":
            case "changetheme":
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
            case "showlyrics":
            case "inc_autoscroll_speed":
            case "dec_autoscroll_speed":
            case "toggle_autoscroll_pause":
                FullscreenActivity.whattodo = s;
                openFragment();
                break;

            case "autoscale":
                // IV - Take the current value, lookup in string and set to the next value in the string
                preferences.setMyPreferenceString(StageMode.this,"songAutoScale", "" + "YWNY".charAt("YWNY".indexOf(preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W")) + 1));
                // Flag for a reload
                StaticVariables.reloadOfSong = true;
                loadSong();
                break;

            case "showchords":
                // Display the native chords
                StaticVariables.showCapoInChordsFragment = false;
                FullscreenActivity.whattodo = "page_chords";
                openFragment();
                break;

            case "showcapo":
                // Display the Capo chords
                if (StaticVariables.mCapo.equals("")) {
                    StaticVariables.myToastMessage = getString(R.string.edit_song_capo) + " - " + getString(R.string.notset);
                    ShowToast.showToast(StageMode.this);
                } else {
                    StaticVariables.showCapoInChordsFragment = true;
                    FullscreenActivity.whattodo = "page_chords";
                    openFragment();
                }
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
        pageButtonAlpha("");
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
        FullscreenActivity.whattodo = "editsong";
        if (FullscreenActivity.myXML.contains("<aka>ERROR!</aka>")) {
            StaticVariables.myToastMessage = getResources().getString(R.string.not_allowed);
            ShowToast.showToast(StageMode.this);
        } else {
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
        new Thread(() -> runOnUiThread(() -> DrawerTweaks.openMyDrawers(mDrawerLayout, songmenu, optionmenu, which))).start();
    }

    @Override
    public void closeMyDrawers(String which) {
        new Thread(() -> runOnUiThread(() -> DrawerTweaks.closeMyDrawers(mDrawerLayout, songmenu, optionmenu, which))).start();
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
    @SuppressWarnings("deprecation")
    private class ResizeDrawers extends AsyncTask<Object, Void, String> {
        int width;

        @Override
        protected String doInBackground(Object... o) {
            try {
                width = preferences.getMyPreferenceInt(StageMode.this, "menuSize", 250);
                // IV- Needs to be a minimum of 3 buttons wide
                if (width <= 200) {
                    width = 168;
                }
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
        if (!fromautoshow) {
            // IV - Stop any auto hide
            doCancelAsyncTask(show_highlight);
        }
        highlightNotes.setVisibility(View.GONE);
        // IV - A fade out may have occurred so set Alpha to 1
        highlightNotes.setAlpha(1.0f);
        if (!StaticVariables.whichMode.equals("Performance")) {
            FullscreenActivity.highlightOn = false;
            if (!fromautoshow) {
                // Don't show the warning just because the app tries to autoshow it
                StaticVariables.myToastMessage = getString(R.string.switchtoperformmode);
                ShowToast.showToast(StageMode.this);
            }
        } else {
            if (StaticVariables.thisSongScale==null) {
                StaticVariables.thisSongScale = preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W");
            }

            // TODO - Fix scaling and positioning of highlightNotes when song is scaled
            if ((!FullscreenActivity.highlightOn && !fromautoshow) ) {
                // IV - If a manual click of highlight button to hide the highlight then hide - do nothing as already done
            } else if (StaticVariables.thisSongScale.equals("Y")) {
                // IV - If the song has been scaled then reload to display without scale
                if (highlightNotes.getScaleX() != 1.0f) {
                    // Flag for a reload
                    StaticVariables.reloadOfSong = true;
                    loadSong();
                } else {
                    String hname = processSong.getHighlighterName(StageMode.this);
                    Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Highlighter", "", hname);
                    if (storageAccess.uriExists(StageMode.this, uri)) {
                        // Load the image in if it exists and then show it
                        try {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            InputStream inputStream = storageAccess.getInputStream(StageMode.this, uri);
                            highlightNotes.setImageBitmap(BitmapFactory.decodeStream(inputStream, null, options).copy(Bitmap.Config.ARGB_8888, true));
                            final int firstguesswidth;
                            final int firstguessheight;
                            if (FullscreenActivity.isSong) {
                                firstguesswidth = songscrollview.getMeasuredWidth();
                                firstguessheight = songscrollview.getMeasuredHeight();
                            } else {
                                firstguesswidth = glideimage_ScrollView.getMeasuredWidth();
                                firstguessheight = glideimage_ScrollView.getMeasuredHeight();
                            }
                            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(firstguesswidth, firstguessheight);
                            if (preferences.getMyPreferenceBoolean(StageMode.this, "hideActionBar", false)) {
                                rlp.addRule(RelativeLayout.BELOW, 0);
                            } else {
                                rlp.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
                            }
                            highlightNotes.setLayoutParams(rlp);
                            highlightNotes.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            // Set a runnable to check the height/width after a couple of seconds to redraw the image position
                            // Only if it has changed though
                            new Thread(() -> {
                                try {
                                    Thread.sleep(1000);
                                    runOnUiThread(() -> {
                                        int secondguessheight;
                                        int secondguesswidth;
                                        if (FullscreenActivity.isSong) {
                                            secondguesswidth = songscrollview.getMeasuredWidth();
                                            secondguessheight = songscrollview.getMeasuredHeight();
                                        } else {
                                            secondguesswidth = glideimage_ScrollView.getMeasuredWidth();
                                            secondguessheight = glideimage_ScrollView.getMeasuredHeight();
                                        }
                                        if (secondguessheight != firstguessheight || secondguesswidth != firstguesswidth) {
                                            // Set the parameters again
                                            RelativeLayout.LayoutParams rlp2 = new RelativeLayout.LayoutParams(secondguesswidth, secondguessheight);
                                            if (preferences.getMyPreferenceBoolean(StageMode.this, "hideActionBar", false)) {
                                                rlp2.addRule(RelativeLayout.BELOW, 0);
                                            } else {
                                                rlp2.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
                                            }
                                            highlightNotes.setLayoutParams(rlp2);
                                            highlightNotes.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                        }
                                    });
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                            if (!fromautoshow) {
                                // If user manually wanted to show, otherwise song load animates it in
                                highlightNotes.setVisibility(View.VISIBLE);
                            } else if (FullscreenActivity.isSong) {
                                highlightNotes.setVisibility(View.VISIBLE);
                                if (FullscreenActivity.whichDirection.equals("L2R")) {
                                    highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
                                } else {
                                    highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
                                }
                            } else {
                                // IV - Fade in for PDF and Image songs
                                highlightNotes.setAlpha(0.0f);
                                // IV - Might have been scrolled
                                highlightNotes.setX(0.0f);
                                CustomAnimations.faderAnimation(highlightNotes, 200, true);
                            }
                        } catch (OutOfMemoryError | Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Oops - error, ran out of memory for an image!");
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
                                Log.d(TAG, "screenshot is null");
                            }
                        }
                        FullscreenActivity.highlightOn = false;
                    }
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
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Replace unwanted symbols
        newText = processSong.removeUnwantedSymbolsAndSpaces(StageMode.this,preferences,newText);
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
    public void confirmedAction() {
        switch (FullscreenActivity.whattodo) {
            case "exit":
                try {
                    android.os.Process.killProcess(android.os.Process.myPid());
                } catch (Exception e) {
                    Log.d(TAG, "Couldn't close the application!");
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
                            StaticVariables.mTitle, StaticVariables.mAuthor,
                            StaticVariables.mCopyright, StaticVariables.mCCLI, "2"); // Deleted
                }
                // Remove the item from the SQL database
                if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                    nonOpenSongSQLiteHelper.deleteSong(StageMode.this, storageAccess,preferences,nonOpenSongSQLite.getSongid());
                }
                if (sqLite!=null && sqLite.getSongid()!=null) {
                    sqLiteHelper.deleteSong(StageMode.this, sqLite.getSongid());
                }

                // IV - Force a song menu refresh as we have deleted a song
                prepareSongMenu();

                // IV - Load previous song to keep place in list
                goToPreviousItem();

                // IV - A backstop loadsong() to display song as deleted if there is no previous song (if previous is running loadsong() this second call is abandoned)
                loadSong();
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
        // Set the ab title to include the song info if available
        if (StaticVariables.mTitle.equals("")) {
            StaticVariables.mTitle = StaticVariables.songfilename.replaceAll("\\.[^.]*$", "");
        }
        if (StaticVariables.whichSongFolder.startsWith("../Received")) {
            String text = "▾" + StaticVariables.mTitle;
            songtitle_ab.setText(text);
        } else if (StaticVariables.whichSongFolder.startsWith("../")) {
            String text = "≡" + StaticVariables.mTitle;
            songtitle_ab.setText(text);
        } else {
            songtitle_ab.setText(StaticVariables.mTitle);
        }
        if (StaticVariables.mKey.isEmpty()) {
            songkey_ab.setText("");
        } else {
            String s = " ("+StaticVariables.mKey+")";
            songkey_ab.setText(s);
        }
        // Process the image location into an URI
        Uri imageUri = storageAccess.getUriForItem(StageMode.this, preferences, "Songs",
                StaticVariables.whichSongFolder, StaticVariables.songfilename);

        glideimage_ScrollView.setVisibility(View.VISIBLE);
        glideimage_HorizontalScrollView.setVisibility(View.VISIBLE);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Returns null, sizes are in the options variable
        InputStream inputStream = storageAccess.getInputStream(StageMode.this, imageUri);

        if (inputStream != null) {
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
            if (preferences.getMyPreferenceString(StageMode.this, "songAutoScale", "W").equals("Y")) {
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
        } else {
            // IV - Handle when image does not exist
            songauthor_ab.setText(getResources().getString(R.string.songdoesntexist));
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

        // Set the ab title to include the song info if available
        if (StaticVariables.whichSongFolder.startsWith("../Received")) {
            String text = "▾" + StaticVariables.mTitle;
            songtitle_ab.setText(text);
        } else if (StaticVariables.whichSongFolder.startsWith("../")) {
            String text = "≡" + StaticVariables.mTitle;
            songtitle_ab.setText(text);
        } else {
            songtitle_ab.setText(StaticVariables.mTitle);
        }
        if (StaticVariables.mKey.isEmpty()) {
            songkey_ab.setText("");
        } else {
            String s = " ("+StaticVariables.mKey+")";
            songkey_ab.setText(s);
        }
        if (bmp != null) {
            if (FullscreenActivity.pdfPageCount > 0) {
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
                songauthor_ab.setText(getResources().getString(R.string.songdoesntexist));
            }
        } else {
            songauthor_ab.setText(getResources().getString(R.string.nothighenoughapi));

            // Set the image to the unhappy android
            Drawable myDrawable = ResourcesCompat.getDrawable(getResources(),R.drawable.unhappy_android,null);
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

    private void checkCanGoTo() {
        // Set default state
        StaticVariables.canGoToPrevious = false;
        StaticVariables.canGoToNext = false;

        StaticVariables.setView = setActions.isSongInSet(StageMode.this, preferences);
        if (StaticVariables.setView) {
            // Now get the position in the set and decide on the set move buttons
            if (StaticVariables.indexSongInSet < 0) {
                // We weren't in set mode, so find the first instance of this song.
                setActions.indexSongInSet();
            }
            // If we aren't at the beginning indicate a set back button
            StaticVariables.canGoToPrevious = StaticVariables.indexSongInSet > 0;

            // If we aren't at the end of the set indicate a setForwardButton
            if (StaticVariables.mSetList==null) {
                StaticVariables.mSetList = new String[0];
            }
            StaticVariables.canGoToNext = StaticVariables.indexSongInSet < StaticVariables.mSetList.length - 1;
        } else {
            StaticVariables.canGoToPrevious = (FullscreenActivity.currentSongIndex > FullscreenActivity.previousSongIndex); // i.e there is a song before in the list/menu
            StaticVariables.canGoToNext = (FullscreenActivity.currentSongIndex < FullscreenActivity.nextSongIndex); // i.e there is a song after in the list/menu
            StaticVariables.indexSongInSet = -1;
        }
    }

    private void getPadProgress() {
        // IV - Pad time display logic is concentrated here
        String text = "";

        if (StaticVariables.clickedOnPadStart) {
            // Decide which player and get time
            if (PadFunctions.getPad1Status() && !StaticVariables.pad1Fading) {
                text = TimeTools.timeFormatFixer((int) (FullscreenActivity.mPlayer1.getCurrentPosition() / 1000.0f));
            } else if (PadFunctions.getPad2Status() && !StaticVariables.pad2Fading) {
                text = TimeTools.timeFormatFixer((int) (FullscreenActivity.mPlayer2.getCurrentPosition() / 1000.0f));
            }
        }

        if (!text.equals(padcurrentTime_TextView.toString())) {
            updateExtraInfoColorsAndSizes("pad");
            if (!text.equals("")) {
                // When 0:00 we get the pad total time and make Pad progress visible
                if (text.equals("0:00")) {
                    backingtrackProgress.setVisibility(View.GONE);
                    padcurrentTime_TextView.setText(text);
                    padTimeSeparator_TextView.setText("/");
                    padtotalTime_TextView.setText(TimeTools.timeFormatFixer(StaticVariables.padtime_length));
                    backingtrackProgress.setVisibility(View.VISIBLE);
                } else {
                    padcurrentTime_TextView.setText(text);
                }
            }
            // IV - If we have only fading pads - Indicate fade with just the pad icon
            if (!StaticVariables.clickedOnPadStart) {
                    padtotalTime_TextView.setText("");
                    padTimeSeparator_TextView.setText("");
                    padcurrentTime_TextView.setText("");
            }
        }
    }

    @Override
    public void goToNextItem() {
        if (!FullscreenActivity.alreadyloading) {
            FullscreenActivity.whichDirection = "R2L";
            // IV - PDF page move handling moved to doscrollDown
            if (StaticVariables.setView) {
                // Is there another song in the set?  If so move, if not, do nothing
                if ((StaticVariables.indexSongInSet < StaticVariables.mSetList.length - 1)) {
                    // Stop the metronome task now as it is high drain and breaks async starts!
                    Metronome.stopMetronomeTask();
                    StaticVariables.setMoveDirection = "forward";
                    doMoveInSet();
                } else {
                    showToastMessage(getResources().getString(R.string.lastsong));
                }
            } else {
                // Try to move to the next song alphabetically
                // However, only do this if the previous item isn't a subfolder!
                boolean isfolder = false;
                try {
                    if (FullscreenActivity.nextSongIndex < filenamesSongsInFolder.size() && FullscreenActivity.nextSongIndex > -1) {

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
                        FullscreenActivity.needtorefreshsongmenu = false;
                        loadSong();

                        // Set a runnable to reset swipe back to original value after 1 second
                        Handler delayfadeinredraw = new Handler();
                        delayfadeinredraw.postDelayed(() -> FullscreenActivity.tempswipeSet = "enable", FullscreenActivity.delayswipe_time);
                    } else {
                        showToastMessage(getResources().getString(R.string.lastsong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void sendMidi() {
        if ((Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)
                && StaticVariables.midiDevice!=null &&
                StaticVariables.midiInputPort!=null && StaticVariables.mMidi!=null &&
                !StaticVariables.mMidi.isEmpty()) && !StaticVariables.mMidi.trim().equals("")) {
            // Declare the midi code
            Handler mh = new Handler();
            mh.post(() -> {
                try {
                    if (midi==null) {
                        midi = new Midi();
                    }
                    // Split the midi messages by line, after changing , into new line
                    StaticVariables.mMidi = StaticVariables.mMidi.replace(",", "\n");
                    StaticVariables.mMidi = StaticVariables.mMidi.replace("\n\n", "\n");
                    String[] midilines = StaticVariables.mMidi.trim().split("\n");
                    int midiDelay = preferences.getMyPreferenceInt(this,"midiDelay",100);
                    int thisDelay = midiDelay;
                    for (String ml : midilines) {
                        // Send each bit of code with a buffer delay (user pref)
                        new Handler().postDelayed(() -> {
                            if (midi != null) {
                                Log.d(TAG, "Sending message: " + ml);
                                midi.sendMidi(midi.returnBytesFromHexText(ml));
                            }
                        }, thisDelay);
                        // Prepare the delay for the next message
                        thisDelay = thisDelay + midiDelay;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            FullscreenActivity.bmScreen = null;
            if (StaticVariables.thisSongScale == null || !StaticVariables.thisSongScale.equals("Y")) {
                StaticVariables.myToastMessage = getString(R.string.highlight_notallowed);
                ShowToast.showToast(StageMode.this);
            } else {
                boolean vis = highlightNotes != null && highlightNotes.getVisibility() == View.VISIBLE;

                if (vis) {
                    highlightNotes.setVisibility(View.GONE);
                }
                if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                    glideimage_ScrollView.destroyDrawingCache();
                    glideimage_ScrollView.setDrawingCacheEnabled(true);
                    glideimage_ScrollView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
                    glideimage_ScrollView.setDrawingCacheBackgroundColor(lyricsBackgroundColor);
                    try {
                        FullscreenActivity.bmScreen = glideimage_ScrollView.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
                    } catch (Exception e) {
                        Log.d(TAG, "error getting the screenshot!");
                    } catch (OutOfMemoryError e) {
                        Log.d(TAG, "not enough memory");
                    }

                } else {
                    songscrollview.destroyDrawingCache();
                    songscrollview.setDrawingCacheEnabled(true);
                    songscrollview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
                    songscrollview.setDrawingCacheBackgroundColor(lyricsBackgroundColor);
                    try {
                        FullscreenActivity.bmScreen = songscrollview.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
                    } catch (Exception e) {
                        Log.d(TAG, "error getting the screenshot!");
                    } catch (OutOfMemoryError o) {
                        Log.d(TAG, "Out of memory");
                    }

                }
                if (vis) {
                    // IV - A fade may have occurred so set Alpha to 1
                    highlightNotes.setAlpha(1.0f);
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
    @SuppressWarnings("deprecation")
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

    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == StaticVariables.LINK_AUDIO || requestCode == StaticVariables.LINK_OTHER) {
            // This has been called from the popuplinks fragment
            try {
                newFragment.onActivityResult(requestCode, resultCode, data);
            } catch (Exception e) {
                Log.d(TAG, "Error sending activity result to fragment");
            }

        } else if (requestCode==StaticVariables.REQUEST_IMAGE_CODE) {
            // This has been called from the custom slides fragment
            try {
                newFragment.onActivityResult(requestCode, resultCode, data);
            } catch (Exception e) {
                Log.d(TAG, "Error sending activity result to fragment");
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
                Log.d(TAG, "Error sending activity result to fragment");
            }

        } else if (requestCode == StaticVariables.REQUEST_CAMERA_CODE && resultCode == Activity.RESULT_OK) {
            FullscreenActivity.whattodo = "savecameraimage";
            openFragment();

        } else if (requestCode == StaticVariables.REQUEST_PDF_CODE) {
            // PDF sent back, so reload it
            loadSong();

        } else if (requestCode == StaticVariables.REQUEST_FILE_CHOOSER && data != null) {
            String filelocation;
            try {
                // This is for the File Chooser returning a file uri
                if (data.getExtras() != null) {
                    // This is from the FolderPicker.class
                    filelocation = data.getExtras().getString("data");
                } else {
                    // This is the built in file picker
                    filelocation = data.getDataString();
                }

                String filename = storageAccess.getActualFilename(StageMode.this,filelocation);

                if (filelocation != null) {
                    boolean validfiletype = (FullscreenActivity.whattodo.equals("processimportosb") && filename.endsWith(".osb")) ||
                            (FullscreenActivity.whattodo.equals("importos") && filename.endsWith(".backup")) ||
                            FullscreenActivity.whattodo.equals("doimport") ||
                            FullscreenActivity.whattodo.equals("doimportset");

                    if (validfiletype) {
                        if (filelocation.startsWith("content")) {
                            // Already safe to continue
                            FullscreenActivity.file_uri = Uri.parse(filelocation);
                        } else {
                            // Non secure (from Folder Picker class, need to convert to FileProvider content
                            File f = new File(filelocation);
                            FullscreenActivity.file_uri = FileProvider.getUriForFile(StageMode.this,
                                    "OpenSongAppFiles", f);
                        }
                        // Get persistent permissions
                        try {
                            final int takeFlags = data.getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            // Check for the freshest data.
                            getContentResolver().takePersistableUriPermission(FullscreenActivity.file_uri,
                                    takeFlags);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

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
            new Thread(() -> {
                boolean success = profileActions.doLoadProfile(StageMode.this,preferences,storageAccess,data.getData());
                if (success) {
                    StaticVariables.myToastMessage = getString(R.string.success);
                } else {
                    StaticVariables.myToastMessage = getString(R.string.error);
                }
                // Once done, reload everything
                runOnUiThread(() -> {
                    ShowToast.showToast(StageMode.this);
                    loadStartUpVariables();
                    if (preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
                        hideActionBar();
                    } else {
                        showActionBar();
                    }
                    refreshAll();
                }
                );
            }).start();

        } else if (requestCode == StaticVariables.REQUEST_PROFILE_SAVE && data!=null && data.getData()!=null) {
            // Saving a profile
            new Thread(() -> {
                boolean success = profileActions.doSaveProfile(StageMode.this, storageAccess,data.getData());
                if (success) {
                    StaticVariables.myToastMessage = getString(R.string.success);
                } else {
                    StaticVariables.myToastMessage = getString(R.string.error);
                }
                // Once done, say so
                runOnUiThread(() -> ShowToast.showToast(StageMode.this)
                );
            }).start();
        }
    }

    private void animateInSong() {
        // End any current autoscroll
        if (StaticVariables.isautoscrolling) {
            stopAutoScroll();
        }
        // Indicate manual drag to cause any still active scrolling tasks to stop.
        FullscreenActivity.isManualDragging = true;

        // If autoshowing highlighter notes
        if (preferences.getMyPreferenceBoolean(StageMode.this,"drawingAutoDisplay",true)) {
            showHighlight();
        }
        // Now display the song via an animation
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            songscrollview.setVisibility(View.GONE);
            glideimage_ScrollView.setAlpha(0.0f);
            // IV - Fade in for PDF and Image songs
            CustomAnimations.faderAnimation(glideimage_ScrollView, 100, true);
        } else {
            glideimage_ScrollView.setVisibility(View.GONE);
            glideimage_HorizontalScrollView.setVisibility(View.GONE);
            songscrollview.setVisibility(View.VISIBLE);
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
            } else {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
            }
            // Enable the scroll bar as animate out will have disabled
            songscrollview.setVerticalScrollBarEnabled(true);
        }

        // IV - Same duration as animate in
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // IV - Consume any later pending client section change received from Host (-ve value)
        if (StaticVariables.currentSection < 0) {
            StaticVariables.currentSection = -(1 + StaticVariables.currentSection);
        }

        // Check for dual screen presentation
        if (StaticVariables.whichMode.equals("Performance")) {
            dualScreenWork();
        } else {
            if (!sectionpresented) { // So it isn't called for each section.
                sectionpresented = true;
                dualScreenWork();
            }
        }

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
            final int proposedCurrentSection = StaticVariables.currentSection;
            final LinearLayout songbit = (LinearLayout) songscrollview.getChildAt(0);
            songbit.postDelayed(() -> {
                songwidth = songbit.getMeasuredWidth();
                songheight = songbit.getMeasuredHeight();
                songbit.setScaleX(1.0f);
                songbit.setScaleY(1.0f);
                highlightNotes.setScaleX(1.0f);
                highlightNotes.setScaleY(1.0f);

               if (StaticVariables.whichMode.equals("Stage")) {
                    // Smooth scroll to show this view at the top of the page unless we are autoscrolling
                    try {
                        FullscreenActivity.sectionviews[StaticVariables.currentSection].setAlpha(1.0f);
                        if (!StaticVariables.isautoscrolling &&
                                (proposedCurrentSection == StaticVariables.currentSection)) {
                            songscrollview.smoothScrollTo(0, FullscreenActivity.sectionviews[StaticVariables.currentSection].getTop() - (int) (getAvailableHeight() * (1.0f - preferences.getMyPreferenceFloat(StageMode.this, "scrollDistance", 0.7f))));
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Section not found");
                    }
                }
            }, 1000);
        }

        glideimage_ScrollView.scrollTo(0,0);
        FullscreenActivity.newPosFloat = 0.0f;

        // Do not touch on a reload
        if (!StaticVariables.reloadOfSong) {
            // If autoshowing sticky notes as a popup
            if (preferences.getMyPreferenceString(StageMode.this, "stickyAutoDisplay", "F").equals("F") && !StaticVariables.mNotes.equals("")) {
                // IV - Empty then add to queue (known state)
                showStickyHandler.removeCallbacks(showStickyRunnable);
                showStickyHandler.postDelayed(showStickyRunnable, 2000);
            }

            // Do the pad fade and play here after song animated in. This ensures a good cross-fade after the song is displayed
            if (!StaticVariables.reloadOfSong && StaticVariables.clickedOnPadStart) {
                // If pads were already playing (previous song), start them up again if wanted
                // Don't do this if the orientation has changed (causing a reload)
                // A delay supports cross-fade for latest song only when rapidly changing songs
                if (preferences.getMyPreferenceBoolean(StageMode.this, "padAutoStart", false) &&
                        !FullscreenActivity.orientationchanged) {
                    playPadHandler.removeCallbacks(playPadRunnable);
                    playPadHandler.postDelayed(playPadRunnable, 3000);
                } else {
                    fadeoutPad();
                }
            }
        }

        // Dealt with display so can move on to other things...

        // Decide if the metronome is good to go
        StaticVariables.metronomeok = Metronome.isMetronomeValid();

        // IV - If StaticVariables.metronomeonoff == "on" this is a reload with the Metronome left running
        // For all other case loadSong has stopped any running Metronome task == "off"
        if (StaticVariables.metronomeonoff.equals("off")) {
            // If we were running and need to autostart the metronome for the new song...
            if ((StaticVariables.clickedOnMetronomeStart) &&
                    preferences.getMyPreferenceBoolean(StageMode.this, "metronomeAutoStart", false)) {
                // Start it
                startMetronomeHandler.removeCallbacks(startMetronomeRunnable);
                startMetronomeHandler.postDelayed(startMetronomeRunnable, 2000);
            } else {
                StaticVariables.clickedOnMetronomeStart = false;
            }
        }

        // Decide if the autoscroll is good to go
        StaticVariables.autoscrollok = processSong.isAutoScrollValid(StageMode.this,preferences);

        // Automatically start the autoscroll
        if (StaticVariables.clickedOnAutoScrollStart && (preferences.getMyPreferenceBoolean(StageMode.this, "autoscrollAutoStart", false) || pdfCanContinueScrolling)) {
            // IV - Using a runnable to autostart if needed - proceeds only after settled on an item
            // IV - Avoids unneeded work during quick song changes
            // IV - Longer delay to start autoscroll when acting as a nearby host - to give time for remote song to render and (hopefully) to have starts in sync
            startAutoscrollHandler.removeCallbacks(startAutoscrollRunnable);
            if (StaticVariables.isHost && StaticVariables.isConnected) {
                startAutoscrollHandler.postDelayed(startAutoscrollRunnable, 8000);
                // There will be a wait, display autoscroll icon only to indicate pending
                currentTime_TextView.setText("");
                timeSeparator_TextView.setText("");
                totalTime_TextView.setText("");
                playbackProgress.setVisibility(View.VISIBLE);
            } else {
                startAutoscrollHandler.postDelayed(startAutoscrollRunnable, 2000);
            }
        }

        // Now, reset the orientation changed flag
        FullscreenActivity.orientationchanged = false;

        setUpCapoInfo();

        // Make sure all dynamic (scroll and set) buttons display
        onScrollAction();

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
            allcapodetails.append(" (").append(capokey).append(")");
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
            // IV - Using a runnable to start capo animation - proceeds only after settled on an item
            // IV - This avoids mis-display during rapid song changes
            // IV - Empty then add to queue (known state)
            startCapoAnimationHandler.removeCallbacks(startCapoAnimationRunnable);
            startCapoAnimationHandler.postDelayed(startCapoAnimationRunnable, 4000);
        }

        // Add the capo information for the action bar
        String s = allcapodetails.toString();
        String capotext;
        if (s.isEmpty()) {
            capotext = "";
        } else {
            capotext = " ["+s+"]";
        }
        songcapo_ab.setText(capotext);
    }

    @Override
    public void goToPreviousItem() {
        // IV - Stops errors on rapid song changes
        if (!FullscreenActivity.alreadyloading) {
            FullscreenActivity.whichDirection = "L2R";
            // IV - PDF page move handling moved to doscrollUp
            if (StaticVariables.setView) {
                // Is there another song in the set?  If so move, if not, do nothing
                if (StaticVariables.mSetList!=null && (StaticVariables.indexSongInSet > 0 && StaticVariables.mSetList.length > 0)) {
                    // Stop the metronome task now as it is high drain and breaks async starts!
                    Metronome.stopMetronomeTask();
                    StaticVariables.setMoveDirection = "back";
                    doMoveInSet();
                } else {
                    showToastMessage(getResources().getString(R.string.firstsong));
                }
            } else {
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
                    if (FullscreenActivity.previousSongIndex >= 0 && filenamesSongsInFolder.size()>FullscreenActivity.previousSongIndex
                            && !StaticVariables.songfilename.equals(filenamesSongsInFolder.get(FullscreenActivity.previousSongIndex))
                            && !isfolder) {
                        FullscreenActivity.tempswipeSet = "disable";

                        StaticVariables.songfilename = filenamesSongsInFolder.get(FullscreenActivity.previousSongIndex);
                        FullscreenActivity.needtorefreshsongmenu = false;
                        loadSong();

                        // Set a runnable to reset swipe back to original value after 1 second
                        Handler delayfadeinredraw = new Handler();
                        delayfadeinredraw.postDelayed(() -> FullscreenActivity.tempswipeSet = "enable", FullscreenActivity.delayswipe_time);
                    } else {
                        showToastMessage(getResources().getString(R.string.firstsong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private static class DualScreenWork extends AsyncTask<Object, Void, String> {
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

    @Override
    public void selectSection(int whichone) {
        //Log.d("StageMode","selectSection " + whichone);
        if (FullscreenActivity.isPDF) {
            // IV - 'Section' moves for PDF will arrive from presenter mode and Stage mode PDF page moves
            // IV - A connected host may request an invalid section, if it does show section 0
            if (whichone >= FullscreenActivity.pdfPageCount) {
                whichone = 0;
            }

            if (whichone < 0) {
                whichone = 0;
            }

            StaticVariables.currentSection = whichone;
            FullscreenActivity.pdfPageCurrent = whichone;

            // Send section to other devices
            if (StaticVariables.isHost && StaticVariables.isConnected) {
                sendSongSectionToConnected();
            }

            // Indicate reload which does not impact running pad etc.
            StaticVariables.reloadOfSong = true;
            loadSong();
        } else {
            // IV - A connected host may request an invalid section, if it does show section 0
            if (whichone >= FullscreenActivity.sectionviews.length) {
                whichone = 0;
            }

            if (whichone < 0) {
                whichone = 0;
            }

            if (StaticVariables.whichMode.equals("Stage")) {
                // Smooth scroll to show this view at the top of the page unless we are autoscrolling
                try {
                    if (FullscreenActivity.sectionviews[whichone] == null) {
                        Log.d("StageMode", "Had to reset section to 0");
                        whichone = 0;
                    }
                    if (!StaticVariables.isautoscrolling) {
                        songscrollview.smoothScrollTo(0, FullscreenActivity.sectionviews[whichone].getTop() - (int) (getAvailableHeight() * (1.0f - preferences.getMyPreferenceFloat(StageMode.this, "scrollDistance", 0.7f))));
                    }
                } catch (Exception e) {
                    whichone = 0;
                    Log.d(TAG, "Section not found");
                }
            }

            try {
                if (FullscreenActivity.sectionviews[whichone] != null) {
                    // Go through each of the views and set the alpha (deliberatly all are set)
                    for (int x = 0; x < FullscreenActivity.sectionviews.length; x++) {
                        if (x == whichone) {
                            FullscreenActivity.sectionviews[x].setAlpha(1.0f);
                        } else {
                            FullscreenActivity.sectionviews[x].setAlpha(0.5f);
                        }
                    }
                    FullscreenActivity.tempswipeSet = "enable";
                    StaticVariables.setMoveDirection = "";
                    StaticVariables.currentSection = whichone;
                    FullscreenActivity.pdfPageCurrent = whichone;
                    // Send section to other devices
                    if (StaticVariables.isHost && StaticVariables.isConnected && FullscreenActivity.isSong) {
                        sendSongSectionToConnected();
                    }
                    dualScreenWork();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Make sure all dynamic (scroll and set) buttons display
            onScrollAction();
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
    @SuppressWarnings("deprecation")
    private class ResizeStageView extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            try {
                // Remove the views from the test pane if any!
                testpane.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Decide on the best scaling
                FullscreenActivity.padding = getPixelsFromDpi(16);
                int availablewidth_1col = getAvailableWidth() - FullscreenActivity.padding;
                int availableheight = (int) (preferences.getMyPreferenceFloat(StageMode.this,"stageModeScale", 0.8f) *
                        getAvailableHeight()) - FullscreenActivity.padding;

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
    @SuppressWarnings("deprecation")
    private class ResizePerformanceView extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
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
            try {
                // Decide on the best scaling
                FullscreenActivity.padding = getPixelsFromDpi(6);
                int availablewidth_1col = getAvailableWidth() - FullscreenActivity.padding;
                float myscale_1_1_col_x = (float) availablewidth_1col / (float) FullscreenActivity.viewwidth[0];
                width_scale = myscale_1_1_col_x;
                int availableheight = getAvailableHeight() - getPixelsFromDpi(12);
                float myscale_1_1_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[0];
                StaticVariables.sectionScaleValue[0] = processSong.getScaleValue(StageMode.this, preferences, myscale_1_1_col_x, myscale_1_1_col_y);

                // IV - If we are going for full scaling
                if (StaticVariables.thisSongScale.equals("Y")) {
                    int availablewidth_2col = (int) (getAvailableWidth() / 2.0f) - getPixelsFromDpi(12 + 4);
                    int availablewidth_3col = (int) (getAvailableWidth() / 3.0f) - getPixelsFromDpi(18 + 4 + 4);

                    float myscale_1_2_col_x = (float) availablewidth_2col / (float) FullscreenActivity.viewwidth[1];
                    float myscale_2_2_col_x = (float) availablewidth_2col / (float) FullscreenActivity.viewwidth[2];
                    float myscale_1_3_col_x = (float) availablewidth_3col / (float) FullscreenActivity.viewwidth[3];
                    float myscale_2_3_col_x = (float) availablewidth_3col / (float) FullscreenActivity.viewwidth[4];
                    float myscale_3_3_col_x = (float) availablewidth_3col / (float) FullscreenActivity.viewwidth[5];
                    float myscale_1_2_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[1];
                    float myscale_2_2_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[2];
                    float myscale_1_3_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[3];
                    float myscale_2_3_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[4];
                    float myscale_3_3_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[5];

                    // If users don't want to scale columns independently
                    if (!preferences.getMyPreferenceBoolean(StageMode.this,"songAutoScaleColumnMaximise",true)) {
                        // Two columns
                        myscale_1_2_col_x = Math.min(myscale_1_2_col_x,myscale_2_2_col_x);
                        myscale_1_2_col_y = Math.min(myscale_1_2_col_y,myscale_2_2_col_y);
                        myscale_2_2_col_x = myscale_1_2_col_x;
                        myscale_2_2_col_y = myscale_1_2_col_y;

                        // Three columns
                        myscale_1_3_col_x = Math.min(Math.min(myscale_1_3_col_x,myscale_2_3_col_x),myscale_3_3_col_x);
                        myscale_1_3_col_y = Math.min(Math.min(myscale_1_3_col_y,myscale_2_3_col_y),myscale_3_3_col_y);
                        myscale_2_3_col_x = myscale_1_3_col_x;
                        myscale_2_3_col_y = myscale_1_3_col_y;
                        myscale_3_3_col_x = myscale_1_3_col_x;
                        myscale_3_3_col_y = myscale_1_3_col_y;
                    }

                    StaticVariables.sectionScaleValue[1] = processSong.getScaleValue(StageMode.this, preferences, myscale_1_2_col_x, myscale_1_2_col_y);
                    StaticVariables.sectionScaleValue[2] = processSong.getScaleValue(StageMode.this, preferences, myscale_2_2_col_x, myscale_2_2_col_y);
                    StaticVariables.sectionScaleValue[3] = processSong.getScaleValue(StageMode.this, preferences, myscale_1_3_col_x, myscale_1_3_col_y);
                    StaticVariables.sectionScaleValue[4] = processSong.getScaleValue(StageMode.this, preferences, myscale_2_3_col_x, myscale_2_3_col_y);
                    StaticVariables.sectionScaleValue[5] = processSong.getScaleValue(StageMode.this, preferences, myscale_3_3_col_x, myscale_3_3_col_y);
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
    @SuppressWarnings("deprecation")
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
                displaySticky(true);

                // Get the current time
                stickycurrtime = System.currentTimeMillis();

                // Set the time to close the sticky note
                stickytimetohide = stickycurrtime + (preferences.getMyPreferenceInt(StageMode.this,"timeToDisplaySticky",5) * 1000L);

                if (preferences.getMyPreferenceInt(StageMode.this,"timeToDisplaySticky",5) == 0) {
                    stickydonthide = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (!stickydonthide && !StaticVariables.mNotes.equals("")) {
                    Thread.sleep(stickytimetohide - System.currentTimeMillis() + 10);
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
    private void displaySticky(boolean fromautoshow) {
        if (!fromautoshow) {
            // IV - Stop any auto hide
            doCancelAsyncTask(show_sticky);
        }

        if (stickyPopUpWindow!=null && stickyPopUpWindow.isShowing()) {
            try {
                stickyPopUpWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (StaticVariables.mNotes != null && !StaticVariables.mNotes.isEmpty()) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (layoutInflater!=null) {
                    final View popupView = layoutInflater.inflate(R.layout.popup_float_sticky, new LinearLayout(this),false);
                    // Decide on the popup position
                    int hp = preferences.getMyPreferenceInt(StageMode.this, "stickyXPosition", -1);
                    int vp = preferences.getMyPreferenceInt(StageMode.this, "stickyYPosition", -1);
                    int sw = getAvailableWidth();
                    int sh = getAvailableHeight();
                    int stickywidth = preferences.getMyPreferenceInt(StageMode.this, "stickyWidth", 400);
                    if (hp == -1 || hp > sw) {
                        hp = sw - stickywidth - (int) ((float) setButton.getMeasuredWidth() * 1.2f);
                    }
                    if (hp < 0) {
                        hp = 0;
                    }
                    if (vp == -1 || hp > sh) {
                        vp = (int) ((float) ab_toolbar.getMeasuredHeight() * 1.2f);
                    }
                    if (vp < 0) {
                        vp = 0;
                    }
                    preferences.setMyPreferenceInt(StageMode.this, "stickyXPosition", hp);
                    preferences.setMyPreferenceInt(StageMode.this, "stickyYPosition", vp);
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
                    if (preferences.getMyPreferenceBoolean(StageMode.this, "stickyLargeFont", true)) {
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
                    stickyfloat.setAlpha(preferences.getMyPreferenceFloat(StageMode.this, "stickyOpacity", 0.8f));
                    closeStickyFloat.setOnClickListener(view -> {
                        // If there is a sticky note showing, remove it
                        if (stickyPopUpWindow != null && stickyPopUpWindow.isShowing()) {
                            try {
                                stickyPopUpWindow.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
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
                                    preferences.setMyPreferenceInt(StageMode.this, "stickyXPosition", offsetX);
                                    preferences.setMyPreferenceInt(StageMode.this, "stickyYPosition", offsetY);
                            }
                            return true;
                        }
                    });
                }
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
        PendingIntent notificationPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            notificationPendingIntent = PendingIntent.getActivity(
                    StageMode.this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            notificationPendingIntent = PendingIntent.getActivity(
                    StageMode.this, 0, intent, 0);
        }


        CastRemoteDisplayLocalService.NotificationSettings settings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(notificationPendingIntent).build();

        if (mSelectedDevice != null) {
            CastRemoteDisplayLocalService.startService(getApplicationContext(),
                    PresentationService.class,
                    getString(R.string.app_id),
                    mSelectedDevice,
                    settings,
                    new CastRemoteDisplayLocalService.Callbacks() {
                        @Override
                        public void onServiceCreated(@NonNull CastRemoteDisplayLocalService castRemoteDisplayLocalService) {
                            Log.d(TAG,"onServiceCreated()");
                            Log.d(TAG,"castRemoteDisplayLocalService="+castRemoteDisplayLocalService);
                        }

                        @Override
                        public void onRemoteDisplaySessionStarted(@NonNull CastRemoteDisplayLocalService castRemoteDisplayLocalService) {
                            Log.d(TAG,"onRemoteDisplaySessionStarted()");
                            Log.d(TAG,"castRemoteDisplayLocalService="+castRemoteDisplayLocalService);
                        }

                        @Override
                        public void onRemoteDisplaySessionError(@NonNull Status status) {
                            Log.d(TAG,"onRemoteDisplaySessionError()");
                            Log.d(TAG,"status="+status);
                        }

                        @Override
                        public void onRemoteDisplaySessionEnded(@NonNull CastRemoteDisplayLocalService castRemoteDisplayLocalService) {
                            Log.d(TAG,"onRemoteDisplaySessionEnded()");
                            Log.d(TAG,"castRemoteDisplayLocalService="+castRemoteDisplayLocalService);
                        }

                        public void onRemoteDisplayMuteStateChanged(boolean b) {
                            Log.d(TAG, "onRemoteDisplayMuteStateChanged()");
                            Log.d(TAG, "b=" + b);
                        }
                    });

        } else {
            // Might be a hdmi connection
            try {
                DisplayManager dm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
                if (dm!=null) {

                    // If a Chromebook HDMI, need to do this
                    Display[] displays = dm.getDisplays();
                    for (Display mDisplay : displays) {
                        if (mDisplay.getDisplayId() > 0) {
                            Point size = new Point();
                            mDisplay.getRealSize(size);
                            if (FullscreenActivity.hdmi == null) FullscreenActivity.hdmi = new PresentationServiceHDMI(StageMode.this, mDisplay, processSong);
                            FullscreenActivity.hdmi.show();
                            FullscreenActivity.isHDMIConnected = true;
                        }
                    }

                    if (!FullscreenActivity.isHDMIConnected) {
                        // For non-Chromebooks
                        displays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
                        for (Display mDisplay : displays) {
                            if (FullscreenActivity.hdmi == null) FullscreenActivity.hdmi = new PresentationServiceHDMI(StageMode.this, mDisplay, processSong);
                            FullscreenActivity.hdmi.show();
                            FullscreenActivity.isHDMIConnected = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doPedalAction(String action) {
        if (action==null) {
            action = "";
        }

        try {
            drawerOrFragmentActive = (mDrawerLayout.isDrawerOpen(songmenu) || mDrawerLayout.isDrawerOpen(optionmenu)) &&
                    !action.equals("songmenu") && !action.equals("optionmenu");
            // IV - If in a drawer or fragment restrict to move actions
            // GE - and open and CLOSE drawer
            if (drawerOrFragmentActive) {
                switch (action) {
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
                }
            } else {
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
                        val = preferences.getMyPreferenceBoolean(StageMode.this, "displayChords", true);
                        preferences.setMyPreferenceBoolean(StageMode.this, "displayChords", !val);
                        refreshAll();
                        break;

                    case "showcapo":
                        val = preferences.getMyPreferenceBoolean(StageMode.this, "displayCapoChords", true);
                        preferences.setMyPreferenceBoolean(StageMode.this, "displayCapoChords", !val);
                        refreshAll();
                        break;

                    case "showlyrics":
                        val = preferences.getMyPreferenceBoolean(StageMode.this, "displayLyrics", true);
                        preferences.setMyPreferenceBoolean(StageMode.this, "displayLyrics", !val);
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
                                displaySticky(false);
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
                        gesture1();
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
        } catch (Exception e) {
            e.printStackTrace();
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
    @SuppressWarnings("deprecation")
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
                    highlighttimetohide = highlightcurrtime + (time * 1000L);

                    if (time == 0) {
                        highlightdonthide = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (shouldHighlightsBeShown() && !highlightdonthide) {
                    while (System.currentTimeMillis() < highlighttimetohide) {
                        Thread.sleep(highlighttimetohide - System.currentTimeMillis() + 10);
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
                        FullscreenActivity.highlightOn = false;
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
        // IV - If playing pads then fade to stop
        if ((StaticVariables.pad1Playing && !StaticVariables.pad1Fading)  || (StaticVariables.pad2Playing && !StaticVariables.pad2Fading)) {
            fadeoutPad();
        } else {
            if (PadFunctions.isPadValid(StageMode.this, preferences)) {
                playPad();
            } else {
                // We inform the user - 'Not set' which can be valid
                showToastMessage(getResources().getString(R.string.pad) + " - " +
                        getResources().getString(R.string.notset));
            }
        }
    }

    @Override
    public void prepareSongMenu() {
        doCancelAsyncTask(preparesongmenu_async);
        // If we have changed folders, redraw the song menu
        if (song_list_view != null) {
            try {
                if (menuFolder_TextView.getText() != null) {
                    // IV - FullscreenActivity.needtorefreshsongmenu can be set false before a call to try to use the existing song menu
                    if (!FullscreenActivity.needtorefreshsongmenu && menuFolder_TextView.getText().toString().equals(StaticVariables.whichSongFolder)) {
                        findSongInFolders();
                    } else {
                        song_list_view.setFastScrollEnabled(false);
                        song_list_view.setScrollingCacheEnabled(false);
                        preparesongmenu_async = new StageMode.PrepareSongMenu();
                        preparesongmenu_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // IV - Reset to ensure the default behaviour is true
        FullscreenActivity.needtorefreshsongmenu = true;
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class ShareSong extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute() {
            if (FullscreenActivity.isSong) {
                try {
                    // If the song height is bigger than the screen height (scrollable), scale it down for memory
                    int childheight = songscrollview.getChildAt(0).getHeight();
                    int scrollheight = songscrollview.getHeight();
                    float scale = 1.0f;
                    if (childheight > scrollheight) {
                        scale = (float) scrollheight / (float) childheight;
                    }
                    FullscreenActivity.bmScreen = null;
                    FullscreenActivity.bmScreen = Bitmap.createBitmap((int) (songscrollview.getChildAt(0).getWidth() * scale),
                            (int) (songscrollview.getChildAt(0).getHeight() * scale), Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(FullscreenActivity.bmScreen);
                    canvas.scale(scale, scale);
                    songscrollview.getChildAt(0).draw(canvas);
                    songscrollview.destroyDrawingCache();
                    songscrollview.setDrawingCacheEnabled(true);
                    songscrollview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
                    songscrollview.setDrawingCacheBackgroundColor(lyricsBackgroundColor);
                    try {
                        FullscreenActivity.bmScreen = songscrollview.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
                    } catch (Exception e) {
                        Log.d(TAG, "ShareSong error getting the screenshot!");
                    } catch (OutOfMemoryError o) {
                        Log.d(TAG, "ShareSong Out of memory");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                String title = getString(R.string.exportcurrentsong);
                Intent emailIntent = exportPreparer.exportSong(StageMode.this, preferences,
                        FullscreenActivity.bmScreen, storageAccess, processSong,makePDF,sqLiteHelper);
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
        // IV - Always displayed for layout consistency - only populate if in use.
        indexLayout.removeAllViews();
        if (preferences.getMyPreferenceBoolean(StageMode.this,"songMenuAlphaIndexShow",true)) {
            TextView textView;
            final Map<String, Integer> map = songMenuAdapter.getAlphaIndex(StageMode.this,songMenuViewItems);
            Set<String> setString = map.keySet();
            List<String> indexList = new ArrayList<>(setString);
            for (String index : indexList) {
                textView = (TextView) View.inflate(StageMode.this,R.layout.leftmenu, null);
                textView.setTextSize(preferences.getMyPreferenceFloat(StageMode.this,"songMenuAlphaIndexSize",14.0f));
                int i = (int) ((int) preferences.getMyPreferenceFloat(StageMode.this,"songMenuAlphaIndexSize",14.0f) * 2.0f);
                textView.setPadding(i+4,i,i-12,i);
                textView.setMinimumWidth(48);
                textView.setMinimumHeight(48);
                textView.setText(index);
                textView.setOnClickListener(view -> {
                    TextView selectedIndex = (TextView) view;
                    try {
                        if (selectedIndex.getText() != null) {
                            String myval = selectedIndex.getText().toString();
                            Object obj = map.get(myval);
                            if (obj!=null) {
                                // Using index so turn off fast scroll to have a clean display behaviour for index use
                                song_list_view.setFastScrollEnabled(false);
                                song_list_view.setSelection((int) obj);
                            }
                            /*
                            int i = map.get(myval);
                            song_list_view.setSelection(i);*/
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                indexLayout.addView(textView);
            }
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
    @SuppressWarnings("deprecation")
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
        closeMyDrawers("song");
        // IV - prepareOptionMenu also prepares the set list
        prepareOptionMenu();
        prepareSongMenu();
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

        // Allow drawer close animation time to cleanly complete
        Handler delayloadsong = new Handler();
        delayloadsong.postDelayed(() -> {
            FullscreenActivity.needtorefreshsongmenu = false;
            loadSong();

            FullscreenActivity.currentSongIndex = i;
            // Scroll to this song in the song menu
            song_list_view.smoothScrollToPosition(i);

            // Initialise the previous and next songs
            findSongInFolders();
        }, 300);
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

        // IV - prepareOptionMenu also prepares the set list
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
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, 0);
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
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
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
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, 0);
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
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
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
    @SuppressWarnings("deprecation")
    private class CreatePerformanceView1Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_1 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_1 = new RelativeLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            try {
                // We know how many columns we are using, so lets go for it.
                column1_1 = processSong.preparePerformanceColumnView(StageMode.this);
                // IV - If doing song block add a padding
                if (StaticVariables.whichMode.equals("Performance") && preferences.getMyPreferenceBoolean(StageMode.this,"stickyBlockInfo",false)) {
                    column1_1.setPadding(0, getPixelsFromDpi(12), 0, 0);
                }
                songbit = processSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
                boxbit1_1 = processSong.preparePerformanceBoxView(StageMode.this, lyricsTextColor, FullscreenActivity.padding);

                // Add the song sections...
                for (int x = 0; x < StaticVariables.songSections.length; x++) {
                    final LinearLayout sectionview = processSong.songSectionView(StageMode.this, x, processSong.setScaledFontSize(0),
                            storageAccess, preferences,
                            lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                            lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor, lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor);
                    column1_1.addView(sectionview);
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
                songTransition_QOS();
                if (!cancelled) {
                    songscrollview.removeAllViews();
                    LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    if (StaticVariables.thisSongScale.equals("Y")) {
                        llp1_1 = new LinearLayout.LayoutParams(getAvailableWidth(), getAvailableHeight());
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
                if (i != null && i.toString().contains("StageMode")) {
                    StageMode.this.recreate();
                } else {
                    finish();
                    startActivity(i);
                }
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
        closeMyDrawers("option");
        switch (s) {
            case "load":
                try {
                    Intent i = profileActions.openProfile(StageMode.this,preferences,storageAccess);
                    this.startActivityForResult(i, StaticVariables.REQUEST_PROFILE_LOAD);
                    refreshAll();
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

    @Override
    public boolean requestNearbyPermissions() {
        // Determine if there is an issue with any of the permissions
        return permissions.requestNearbyPermissions(this,403);
    }

    @Override
    public boolean hasNearbyPermissions() {
        // Determine if there is an issue with any of the permissions
        return permissions.hasNearbyPermissions(this);
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class ShareSet extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                String title = getString(R.string.exportsavedset);
                Intent emailIntent = exportPreparer.exportSet(StageMode.this, preferences, storageAccess, processSong, makePDF, sqLiteHelper);
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
        // IV - Block false short key press if fragment used during long press
        blockActionOnKeyUp = true;
        Handler resetBlockActionOnKeyUp = new Handler();
        resetBlockActionOnKeyUp.postDelayed(() -> blockActionOnKeyUp = false, 300);

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
        if (!permissions.checkForPermission(StageMode.this,Manifest.permission.CAMERA)) {
           permissions.requestForPermissions(StageMode.this,new String[]{Manifest.permission.CAMERA},
                   StaticVariables.REQUEST_CAMERA_CODE);
        } else {
            startCamera();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            switch (requestCode) {
                case StaticVariables.REQUEST_CAMERA_CODE:
                    startCamera();
                    break;

                case 404:
                    // Access fine location, so can open the menu at 'Connect devices'
                    Log.d(TAG, "FINE LOCATION granted!");
                    break;

                case 488:
                    // Android S - allow Bluetooth name
                    Log.d(TAG, "Bluetooth connect allowed");
                    FullscreenActivity.mBluetoothName = FullscreenActivity.mBluetoothAdapter.getName();
                    break;
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
    @SuppressWarnings("deprecation")
    private class CreatePerformanceView2Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_2 = new LinearLayout(StageMode.this);
        LinearLayout column2_2 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_2 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit2_2 = new RelativeLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            try {
                // We know how many columns we are using, so lets go for it.
                column1_2 = processSong.preparePerformanceColumnView(StageMode.this);
                column2_2 = processSong.preparePerformanceColumnView(StageMode.this);
                // IV - If doing song block add a padding
                if (StaticVariables.whichMode.equals("Performance") && preferences.getMyPreferenceBoolean(StageMode.this,"stickyBlockInfo",false)) {
                    column1_2.setPadding(0,getPixelsFromDpi(12), 0, 0);
                    column2_2.setPadding(0,getPixelsFromDpi(12), 0, 0);
                }
                songbit = processSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
                boxbit1_2 = processSong.preparePerformanceBoxView(StageMode.this, lyricsTextColor, FullscreenActivity.padding);
                boxbit2_2 = processSong.preparePerformanceBoxView(StageMode.this, lyricsTextColor, FullscreenActivity.padding);

                // Add the song sections...
                for (int x = 0; x < StaticVariables.songSections.length; x++) {

                    if (x < FullscreenActivity.halfsplit_section) {
                        float fontsize = processSong.setScaledFontSize(1);
                        final LinearLayout sectionview = processSong.songSectionView(StageMode.this, x, fontsize,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor);
                        sectionview.setPadding(0, 0, 0, 0);
                        column1_2.addView(sectionview);

                    } else {
                        float fontsize = processSong.setScaledFontSize(2);
                        final LinearLayout sectionview2 = processSong.songSectionView(StageMode.this, x, fontsize,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor);
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
                songTransition_QOS();
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
    @SuppressWarnings("deprecation")
    private class CreatePerformanceView3Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_3 = new LinearLayout(StageMode.this);
        LinearLayout column2_3 = new LinearLayout(StageMode.this);
        LinearLayout column3_3 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_3 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit2_3 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit3_3 = new RelativeLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            try {
                // We know how many columns we are using, so lets go for it.
                column1_3 = processSong.preparePerformanceColumnView(StageMode.this);
                column2_3 = processSong.preparePerformanceColumnView(StageMode.this);
                column3_3 = processSong.preparePerformanceColumnView(StageMode.this);
                // IV - If doing song block add a padding
                if (StaticVariables.whichMode.equals("Performance") && preferences.getMyPreferenceBoolean(StageMode.this,"stickyBlockInfo",false)) {
                    column1_3.setPadding(0, getPixelsFromDpi(12), 0, 0);
                    column2_3.setPadding(0, getPixelsFromDpi(12), 0, 0);
                    column3_3.setPadding(0, getPixelsFromDpi(12), 0, 0);
                }
                songbit = processSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
                boxbit1_3 = processSong.preparePerformanceBoxView(StageMode.this, lyricsTextColor, FullscreenActivity.padding);
                boxbit2_3 = processSong.preparePerformanceBoxView(StageMode.this, lyricsTextColor, FullscreenActivity.padding);
                boxbit3_3 = processSong.preparePerformanceBoxView(StageMode.this, lyricsTextColor, FullscreenActivity.padding);

                // Add the song sections...
                for (int x = 0; x < StaticVariables.songSections.length; x++) {
                    if (x < FullscreenActivity.thirdsplit_section) {
                        float fontsize = processSong.setScaledFontSize(3);
                        final LinearLayout sectionview = processSong.songSectionView(StageMode.this, x, fontsize,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor);
                        sectionview.setPadding(0, 0, 0, 0);
                        column1_3.addView(sectionview);

                    } else if (x < FullscreenActivity.twothirdsplit_section) {
                        float fontsize = processSong.setScaledFontSize(4);
                        final LinearLayout sectionview2 = processSong.songSectionView(StageMode.this, x, fontsize,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor);
                        sectionview2.setPadding(0, 0, 0, 0);
                        column2_3.addView(sectionview2);

                    } else {
                        float fontsize = processSong.setScaledFontSize(5);
                        final LinearLayout sectionview3 = processSong.songSectionView(StageMode.this, x, fontsize,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor);
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
                songTransition_QOS();
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
    @SuppressWarnings("deprecation")
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class PrepareSongView extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            try {
                mypage.setBackgroundColor(lyricsBackgroundColor);
                songscrollview.setBackgroundColor(lyricsBackgroundColor);
                width_scale = 0f;
                testpane.removeAllViews();
                if (StaticVariables.whichMode.equals("Performance") && !(preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W").equals("W"))) {
                    testpane1_2.removeAllViews();
                    testpane2_2.removeAllViews();
                    testpane1_3.removeAllViews();
                    testpane2_3.removeAllViews();
                    testpane3_3.removeAllViews();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
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
            try {
                if (!cancelled) {
                    // For stage mode, each section gets its own box
                    // For performance mode, all the sections get added into the one box

                    if (column1_1 != null) {
                        column1_1.removeAllViews();
                    }

                    // IV - Always do 1 column activity. Only do 2 and 3 if needed.
                    column1_1 = processSong.createLinearLayout(StageMode.this);

                    if (StaticVariables.whichMode.equals("Performance") && !(preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W").equals("W"))) {
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
                        column1_2 = processSong.createLinearLayout(StageMode.this);
                        column2_2 = processSong.createLinearLayout(StageMode.this);
                        column1_3 = processSong.createLinearLayout(StageMode.this);
                        column2_3 = processSong.createLinearLayout(StageMode.this);
                        column3_3 = processSong.createLinearLayout(StageMode.this);
                    }

                    // Go through each section
                    for (int x = 0; x < StaticVariables.songSections.length; x++) {

                        // The single stage mode view
                        final LinearLayout section = processSong.songSectionView(StageMode.this, x, 12.0f,
                                storageAccess, preferences,
                                lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                                lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor);

                        column1_1.addView(section);

                        section.setClipChildren(false);
                        section.setClipToPadding(false);
                        section.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        FullscreenActivity.viewwidth[x] = section.getMeasuredWidth();
                        FullscreenActivity.viewheight[x] = section.getMeasuredHeight();

                        // IV - Only do split 1 2 and 3 if needed.
                        if (StaticVariables.whichMode.equals("Performance") && !(preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W").equals("W"))) {

                            if (x < FullscreenActivity.halfsplit_section) {
                                column1_2.addView(processSong.songSectionView(StageMode.this, x, 12.0f,
                                        storageAccess, preferences,
                                        lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor, lyricsCommentColor, lyricsCustomColor,
                                        lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                        lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor));
                            } else {
                                column2_2.addView(processSong.songSectionView(StageMode.this, x, 12.0f,
                                        storageAccess, preferences,
                                        lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor, lyricsCommentColor, lyricsCustomColor,
                                        lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                        lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor));
                            }

                            if (x < FullscreenActivity.thirdsplit_section) {
                                column1_3.addView(processSong.songSectionView(StageMode.this, x, 12.0f,
                                        storageAccess, preferences,
                                        lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor, lyricsCommentColor, lyricsCustomColor,
                                        lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                        lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor));
                            } else if (x < FullscreenActivity.twothirdsplit_section) {
                                column2_3.addView(processSong.songSectionView(StageMode.this, x, 12.0f,
                                        storageAccess, preferences,
                                        lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor, lyricsCommentColor, lyricsCustomColor,
                                        lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                        lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor));
                            } else {
                                column3_3.addView(processSong.songSectionView(StageMode.this, x, 12.0f,
                                        storageAccess, preferences,
                                        lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor, lyricsCommentColor, lyricsCustomColor,
                                        lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                                        lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor));
                            }
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
                        testpane.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane.setVisibility(View.INVISIBLE);

                        if (!(preferences.getMyPreferenceString(StageMode.this,"songAutoScale","W").equals("W"))) {
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

                            testpane1_2.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            testpane2_2.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            testpane1_3.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            testpane2_3.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            testpane3_3.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                            testpane1_2.setVisibility(View.INVISIBLE);
                            testpane2_2.setVisibility(View.INVISIBLE);
                            testpane1_3.setVisibility(View.INVISIBLE);
                            testpane2_3.setVisibility(View.INVISIBLE);
                            testpane3_3.setVisibility(View.INVISIBLE);
                        }
                        resizePerformanceView();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class CreateStageView1Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_1 = new LinearLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            try {
                // Only 1 column, but many sections
                column1_1 = processSong.preparePerformanceColumnView(StageMode.this);
                // IV - If doing song block add a padding
                if (StaticVariables.whichMode.equals("Performance") && preferences.getMyPreferenceBoolean(StageMode.this,"stickyBlockInfo",false)) {
                    column1_1.setPadding(0, getPixelsFromDpi(12), 0, 0);
                }
                songbit = processSong.prepareStageSongBitView(StageMode.this);

                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(getAvailableWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(0, 0, 0, getPixelsFromDpi(4));

                // Add the song sections...
                for (int x = 0; x < StaticVariables.songSections.length; x++) {
                    float fontsize = processSong.setScaledFontSize(x);
                    final LinearLayout sectionview = processSong.songSectionView(StageMode.this, x, fontsize,
                            storageAccess, preferences,
                            lyricsTextColor, lyricsBackgroundColor, lyricsChordsColor,  lyricsCommentColor, lyricsCustomColor,
                            lyricsCapoColor, presoFontColor, lyricsVerseColor, lyricsChorusColor,
                            lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor);
                    LinearLayout boxbit = processSong.prepareStageBoxView(StageMode.this, lyricsTextColor, FullscreenActivity.padding);
                    boxbit.setLayoutParams(llp);
                    boxbit.addView(sectionview);
                    column1_1.addView(boxbit);
                    boxbit.setAlpha(0.5f);
                    FullscreenActivity.sectionviews[x] = boxbit;
                    final int finalX = x;
                    FullscreenActivity.sectionviews[x].setOnClickListener(v -> selectSection(finalX));
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
            songTransition_QOS();
            try {
                if (!cancelled) {
                    songscrollview.removeAllViews();
                    songbit.addView(column1_1);
                    songscrollview.addView(songbit);
                    if (FullscreenActivity.sectionviews[StaticVariables.currentSection] != null) {
                        // Make the current section active (full alpha)
                        try {
                            FullscreenActivity.sectionviews[StaticVariables.currentSection].setAlpha(1.0f);
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

    private class Player1Prepared implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            String padpan = preferences.getMyPreferenceString(StageMode.this,"padPan","C");
            float padvol = preferences.getMyPreferenceFloat(StageMode.this,"padVol",1.0f);
            StaticVariables.padtime_length = (int) (FullscreenActivity.mPlayer1.getDuration() / 1000.0f);
            FullscreenActivity.mPlayer1.setLooping(PadFunctions.getLoop());
            FullscreenActivity.mPlayer1.setVolume(PadFunctions.getVol(padpan,padvol,0), PadFunctions.getVol(padpan,padvol,1));
            FullscreenActivity.mPlayer1.setOnCompletionListener(mp -> {
                if (!PadFunctions.getLoop()) {
                    Log.d(TAG, "Reached end and not looping");

                } else {
                    Log.d(TAG, "Reached end but looping");
                }
            });
            FullscreenActivity.mPlayer1.start();
            dopadProgressTime.removeCallbacks((onEverySecond));
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
        // If we need to display pad time do runnables otherwise stop display
        PadFunctions.getPad1Status();
        PadFunctions.getPad2Status();
        if (StaticVariables.pad1Playing || StaticVariables.pad2Playing) {
            dopadProgressTime.post(padprogressTimeRunnable);
            dopadProgressTime.postDelayed(onEverySecond, 1000);
        } else {
            backingtrackProgress.setVisibility(View.GONE);
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
            FullscreenActivity.mPlayer2.setOnCompletionListener(mp -> {
                if (!PadFunctions.getLoop()) {
                    Log.d(TAG, "Reached end and not looping");

                } else {
                    Log.d(TAG, "Reached end but looping");
                }
            });
            FullscreenActivity.mPlayer2.start();
            dopadProgressTime.removeCallbacks((onEverySecond));
            dopadProgressTime.post(onEverySecond);
        }
    }
    private void fadeoutPad() {

        // IV - Remove any outstanding cross-fade playPad requests
        playPadHandler.removeCallbacks(playPadRunnable);

        // Put the quick fade mechanism into a known state
        StaticVariables.padInQuickFade = 0;

        // Set false as all pads will fade. preparePad sets this true if a pad is played
        StaticVariables.clickedOnPadStart = false;

        String padpan = preferences.getMyPreferenceString(StageMode.this, "padPan", "C");
        float padvol = preferences.getMyPreferenceFloat(StageMode.this, "padVol", 1.0f);

        PadFunctions.getPad1Status();
        PadFunctions.getPad2Status();

        if (StaticVariables.pad1Playing && !StaticVariables.pad1Fading) {
            fadeout_media1 = new FadeoutMediaPlayer(padpan, padvol, 1, preferences.getMyPreferenceInt(StageMode.this, "padCrossFadeTime", 8000));
            fadeout_media1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if (StaticVariables.pad2Playing && !StaticVariables.pad2Fading) {
            fadeout_media2 = new FadeoutMediaPlayer(padpan, padvol, 2, preferences.getMyPreferenceInt(StageMode.this, "padCrossFadeTime", 8000));
            fadeout_media2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        while (StaticVariables.pad1Playing && StaticVariables.pad2Playing) {
            // Sleep until a pad is free for use
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        StaticVariables.padInQuickFade = 0;
    }

    private void killPad() {
        // Fade rather than kill - avoids an abrupt stop if the app is accidentally switched away from
        fadeoutPad();
    }

    @Override
    public void prepareLearnAutoScroll() {
        StaticVariables.learnPreDelay = false;
        StaticVariables.learnSongLength = false;
        updateExtraInfoColorsAndSizes("autoscroll");
        learnAutoScroll.setVisibility(View.VISIBLE);
        learnAutoScroll.setOnClickListener(view -> startLearnAutoScroll());
        String s = getString(R.string.autoscroll_time) + "\n" + getString(R.string.start);
        learnAutoScroll_TextView.setText(s);
        learnAutoScrollTime_TextView.setText(TimeTools.timeFormatFixer(0));
    }

    private void startLearnAutoScroll() {
        StaticVariables.learnPreDelay = true;
        StaticVariables.learnSongLength = false;
        learnAutoScroll.setOnClickListener(view -> getLearnedPreDelayValue());
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
        learnAutoScroll.setOnClickListener(view -> getLearnedSongLengthValue());
    }
    // IV - Used by autoscroll setting page - if learn is running it can be abandoned by reopen of autoscroll settings page
    @Override
    public void stopLearnAutoScroll() {
        learnAutoScroll.setVisibility(View.GONE);
        StaticVariables.learnPreDelay = false;
        StaticVariables.learnSongLength = false;
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
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

    private void startAutoScroll() {
        // IV - clickedOnAutoScrollStart is being used to indicate being active (autoscroll may be active but not running)
        // IV - it is set elsewhere
        updateExtraInfoColorsAndSizes("autoscroll");
        doCancelAsyncTask(mtask_autoscroll_music);
        doCancelAsyncTask(get_scrollheight);
        endAutoScrollHandler.removeCallbacks(endAutoScrollRunnable);
        AutoScrollFunctions.getAutoScrollActiveTimes(StageMode.this, preferences);
        if (StaticVariables.autoScrollDuration > -1) {
            currentTime_TextView.setText(R.string.time_zero);
            // Display the '/' as now active
            timeSeparator_TextView.setText("/");
            totalTime_TextView.setText(TimeTools.timeFormatFixer(StaticVariables.autoScrollDuration));
            playbackProgress.setVisibility(View.VISIBLE);
            StaticVariables.isautoscrolling = true;
            StaticVariables.pauseautoscroll = true;
            FullscreenActivity.isManualDragging = false;
            FullscreenActivity.wasscrolling = false;
            get_scrollheight = new GetScrollHeight();
            try {
                get_scrollheight.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
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
                        ready = true;
                        cancelled = true;
                    }
                    // IV - We show progress for all cases
                    viewdrawn = true;
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
    @SuppressWarnings("deprecation")
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
            // Send Nearby autoscroll payload
            if (StaticVariables.isHost && StaticVariables.isConnected) {
                sendAutoscrollTriggerToConnected();
            }
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                // IV - Ensure a 'panic' end should there be no scroll
                endAutoScrollHandler.removeCallbacks(endAutoScrollRunnable);
                endAutoScrollHandler.postDelayed(endAutoScrollRunnable, ((StaticVariables.autoScrollDelay * 1000L) + 4000));
                while (StaticVariables.isautoscrolling) {
                    // IV - update the scroll buttons as we go
                    FullscreenActivity.time_passed = System.currentTimeMillis();
                    delaycheckscroll.post(checkScrollPosition);
                    boolean doscroll = ((((FullscreenActivity.time_passed - FullscreenActivity.time_start) / 1000) >= StaticVariables.autoScrollDelay) || (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0));
                    if (doscroll) {
                        publishProgress(1);
                        // We set a runnable to end scroll after 4s - renewed if we are not at the end of the page.
                        // IV - Helps manual drag during autoscroll - a user can drag temporarily to the end and back up without an immediate autoscroll stop
                        if ((FullscreenActivity.newPosFloat < StaticVariables.scrollpageHeight) || !currentTime_TextView.getText().equals(totalTime_TextView.getText())) {
                            endAutoScrollHandler.removeCallbacks(endAutoScrollRunnable);
                            endAutoScrollHandler.postDelayed(endAutoScrollRunnable, 4000);
                        }
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
                        doScrollDown();
                    } else {
                        pdfCanContinueScrolling = false;
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
    private void stopAutoScroll() {
        try {
            updateExtraInfoColorsAndSizes("autoscroll");
            playbackProgress.setVisibility(View.GONE);
            doCancelAsyncTask(mtask_autoscroll_music);
            doCancelAsyncTask(get_scrollheight);
            StaticVariables.isautoscrolling = false;
            currentTime_TextView.setText(R.string.time_zero);
            // Send Nearby autoscroll payload
            if (StaticVariables.isHost && StaticVariables.isConnected) {
                sendAutoscrollTriggerToConnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // IV - New function to restart autoscroll after a manual drag or scroll
    private void pauseAutoscroll () {
        if (StaticVariables.isautoscrolling) {
            FullscreenActivity.isManualDragging = true;
            endManualDraggingHandler.removeCallbacks(endManualDraggingRunnable);
            endManualDraggingHandler.postDelayed(endManualDraggingRunnable, 2000);
        }
    }
    private boolean checkCanScrollDown() {
        boolean showscrolldown = false;
        // IV - Handling by song type
        int height;
        if (FullscreenActivity.isSong) {
            if (StaticVariables.whichMode!=null && StaticVariables.whichMode.equals("Stage") &&
                StaticVariables.songSections != null && StaticVariables.currentSection > -1) {
                    showscrolldown = StaticVariables.currentSection < StaticVariables.songSections.length - 1;
            } else {
                try {
                    height = songscrollview.getChildAt(0).getMeasuredHeight() - songscrollview.getHeight();
                } catch (Exception e) {
                    height = 0;
                }
                if (songscrollview != null) {
                    if (!StaticVariables.isautoscrolling) { // GE Added as this was breaking the autoscroll - grabbing the rounded pixel value
                        FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
                    }
                    showscrolldown = height > songscrollview.getScrollY() && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            }
        } else { // PDF and Image
            try {
                height = glideimage_ScrollView.getChildAt(0).getMeasuredHeight() - glideimage_ScrollView.getHeight();
            } catch (Exception e) {
                height = 0;
            }
            if (glideimage_ScrollView!=null) {
                if (!StaticVariables.isautoscrolling) {  // GE Added as this was breaking the autoscroll - grabbing the rounded pixel value
                    FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY();
                }
                showscrolldown = height > glideimage_ScrollView.getScrollY() && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
            }
        }
        return showscrolldown;
    }

    private boolean checkCanScrollUp() {
        // IV - Handling by song type
        boolean showscrollup = false;
        if (FullscreenActivity.isSong) {
            if (StaticVariables.whichMode!=null && StaticVariables.whichMode.equals("Stage") &&
                StaticVariables.songSections != null && StaticVariables.currentSection > -1) {
                showscrollup = StaticVariables.currentSection > 0;
            } else {
                if (songscrollview != null) {
                    if (!StaticVariables.isautoscrolling) { // GE Added as this was breaking the autoscroll - grabbing the rounded pixel value
                        FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
                    }
                    showscrollup = songscrollview.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            }
        } else { // PDF and Image
            if (glideimage_ScrollView!=null) {
                if (!StaticVariables.isautoscrolling) { // GE Added as this was breaking the autoscroll - grabbing the rounded pixel value
                    FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY();
                }
                showscrollup = glideimage_ScrollView.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
            }
        }
        return showscrollup;
    }

    private void scrollMenu(String direction) {
        if (direction.equals("up")) {
            song_list_view.smoothScrollBy((int) (-0.8f * songmenu.getHeight()), 1600);
        } else {
            song_list_view.smoothScrollBy((int) (0.8f * songmenu.getHeight()), 1600);
        }
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class DoMoveInSet extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Get the appropriate song
            try {
                if (StaticVariables.mSetList != null && StaticVariables.indexSongInSet > -1 &&
                        StaticVariables.mSetList.length > StaticVariables.indexSongInSet) {
                    FullscreenActivity.linkclicked = StaticVariables.mSetList[StaticVariables.indexSongInSet];
                } else {
                    FullscreenActivity.linkclicked = "";
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
                    setActions.doMoveInSet(StageMode.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // This bit listens for long key presses (disables the menu long press action)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // IV - With this call, pause(if running) autoscroll so a user using pedal long press for scroll can hold autoscroll at the top of a song with it starting with pedal release
        // Temporarily pause any running autoscroll
        pauseAutoscroll();

        // IV - Used by all methods
        keyRepeatCount++;

        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            // Open up the song search intent instead of bringing up the keyboard
            return true;
        }

        // IV - Further process pedal keys only
        if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal1Code", 21) ||
            keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal2Code", 22) ||
            keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal3Code", 19) ||
            keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal4Code", 20) ||
            keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal5Code", 92) ||
            keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal6Code", 93)) {

            // AirTurn pedals don't do long press, but instead send repeated signals (onKeyDown then onKeyUp).  To deal with, count onKeyDown
            // If the app detects more than a set number (reset when onKeyUp/onLongPress) it calls doLongKeyPressAction

            if (preferences.getMyPreferenceBoolean(StageMode.this, "airTurnMode", false)) {
                if (keyRepeatCount > preferences.getMyPreferenceInt(StageMode.this, "keyRepeatCount", 20)) {
                    if (!blockKeyAction) {
                        doLongKeyPressAction(keyCode);
                    }
                    return true;
                }
            } else {
                // IV - Some devices don't do long press straight after fragment use! Provide a backstop doLongKeyPressAction
                if (!blockKeyAction && (keyRepeatCount > 6)) {
                    doLongKeyPressAction(keyCode);
                }
                event.startTracking();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void changePDFPage(int page, String direction) {
        FullscreenActivity.whichDirection = direction;
        FullscreenActivity.pdfPageCurrent = page;
        StaticVariables.currentSection = FullscreenActivity.pdfPageCurrent;
        StaticVariables.reloadOfSong = true;
        loadSong();
    }

    @Override
    public void loadSong() {
        try {
            // Only do this once - if we are in the process of loading a song already, don't try to do it again!
            if (!FullscreenActivity.alreadyloading) {
                // It will get set back to false in the post execute of the async task
                FullscreenActivity.alreadyloading = true;

                // Clear any queued activity - we are moving to a new song
                startCapoAnimationHandler.removeCallbacks(startCapoAnimationRunnable);
                startAutoscrollHandler.removeCallbacks(startAutoscrollRunnable);
                startMetronomeHandler.removeCallbacks(startMetronomeRunnable);
                showStickyHandler.removeCallbacks(showStickyRunnable);
                playPadHandler.removeCallbacks(playPadRunnable);
                doCancelAsyncTask(loadsong_async);
                doCancelAsyncTask(resizestage_async);
                doCancelAsyncTask(resizeperformance_async);
                doCancelAsyncTask(createperformanceview1col_async);
                doCancelAsyncTask(createperformanceview2col_async);
                doCancelAsyncTask(createperformanceview3col_async);

                // We may transpose to Nashville and need to init variables
                StaticVariables.fromchordnumsnash = null;
                StaticVariables.tochordnumsnash = null;

                // For a load
                if (!StaticVariables.reloadOfSong) {
                    // Stop the metronome now as it is high drain!
                    Metronome.stopMetronomeTask();
                    // IV - Set current section
                    if (StaticVariables.currentSection >= 0) {
                        StaticVariables.currentSection = 0;
                    } else {
                        // IV - Consume any pending client section change received from Host (-ve value)
                        StaticVariables.currentSection = -(1 + StaticVariables.currentSection);
                    }
                    // IV - Clear sendSong Handlers and send a pending section change for the current section
                    if (StaticVariables.isHost && StaticVariables.isConnected) {
                        sendSongAfterDelayHandler.removeCallbacks(sendSongAfterDelayRunnable);
                        resetSendSongAfterDelayHandler.removeCallbacks(resetSendSongAfterDelayRunnable);
                        if (StaticVariables.whichMode.equals("Stage")) {
                            sendSongSectionForPendingToConnected();
                        }
                    }
                } else {
                    // IV - For a reload, load the stored whichSongFolder in case we were browsing elsewhere
                    StaticVariables.whichSongFolder = preferences.getMyPreferenceString(StageMode.this,"whichSongFolder", getString(R.string.mainfoldername));
                    FullscreenActivity.needtorefreshsongmenu = false;
                }

                FullscreenActivity.pdfPageCurrent = StaticVariables.currentSection;

                // If there is a sticky note showing, remove it early
                if (stickyPopUpWindow != null && stickyPopUpWindow.isShowing()) {
                    try {
                        stickyPopUpWindow.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Remove any capokey
                FullscreenActivity.capokey = "";
                // IV - Clear capo info and animation - prevents disturbance with display of new song
                capoInfo.setVisibility(View.GONE);
                capoinfonewkey.setVisibility(View.GONE);
                capoInfo.clearAnimation();

                // End any current autoscroll
                if (StaticVariables.isautoscrolling) {
                    stopAutoScroll();
                }

                // Check for set song (isSongInSet will index)
                StaticVariables.whatsongforsetwork = setActions.getSongForSetWork(StageMode.this);
                StaticVariables.setView = setActions.isSongInSet(StageMode.this, preferences);

                // Sort the text size and colour of the info stuff
                updateExtraInfoColorsAndSizes("capo");
                updateExtraInfoColorsAndSizes("pad");
                updateExtraInfoColorsAndSizes("metronome");

                String where = "Songs";
                String folder = StaticVariables.whichSongFolder;

                // Watch out for custom items
                if (folder.startsWith("../")) {
                    where = "";
                    folder = folder.replace("../", "");
                }

                Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, where, folder,
                        StaticVariables.songfilename);

                songUriExists = storageAccess.uriExists(StageMode.this, uri);

                newsongloaded = true;

                songTransitionStart = System.currentTimeMillis();
                // IV - Animate out only when isSong
                if (FullscreenActivity.isSong) {
                    // Animate out the current song without a scrollbar line - so it looks nice!
                    songscrollview.setVerticalScrollBarEnabled(false);
                    if (FullscreenActivity.whichDirection.equals("L2R")) {
                        if (songscrollview != null) {
                            songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
                        }
                    } else {
                        if (songscrollview != null) {
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
                } else {
                    // If there were highlight notes showing, remove them
                    CustomAnimations.faderAnimation(highlightNotes, 100, false);
                }

                // After animate out, load the song
                Handler h = new Handler();
                h.postDelayed(() -> {
                    try {
                        glideimage_HorizontalScrollView.setVisibility(View.GONE);
                        glideimage_ScrollView.setVisibility(View.GONE);
                        songscrollview.setVisibility(View.GONE);
                        highlightNotes.setVisibility(View.GONE);
                        highlightNotes.setScaleX(1.0f);
                        highlightNotes.setScaleY(1.0f);
                        FullscreenActivity.highlightOn = false;
                        glideimage_ScrollView.scrollTo(0, 0);
                        songscrollview.scrollTo(0, 0);

                        // Hide the image, cause we might be loading a proper song!
                        glideimage.setBackgroundColor(StaticVariables.transparent);
                        glideimage.setImageDrawable(null);

                        // Load the song
                        loadsong_async = new LoadSongAsync();
                        loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    } catch (Exception e) {
                        Log.d(TAG, "error updating the views");
                    }
                    // IV - After a small delay
                }, 20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class LoadSongAsync extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            StaticVariables.myToastMessage = "";
            try {
                StaticVariables.panicRequired = false;
                StaticVariables.infoBarChangeRequired = true;
                FullscreenActivity.scalingfiguredout = false;
                sectionpresented = false;
                try {
                    LoadXML.loadXML(StageMode.this, preferences, storageAccess, processSong);
                } catch (Exception e) {
                    Log.d(TAG, "Error loading song:" + StaticVariables.songfilename);
                }
                if (!StaticVariables.reloadOfSong) {
                    // Send Nearby song intent
                    if (StaticVariables.isConnected && StaticVariables.isHost && !FullscreenActivity.orientationchanged) {
                        // Only the first (with no delay) and last (with delay) of a long sequence of song changes is actually sent
                        // sendSongDelay will be 0 for the first song
                        // IV - Always empty then add to queue (known state)
                        sendSongAfterDelayHandler.removeCallbacks(sendSongAfterDelayRunnable);
                        sendSongAfterDelayHandler.postDelayed(sendSongAfterDelayRunnable, sendSongDelay);
                        // IV - Always empty then add to queue (known state)
                        resetSendSongAfterDelayHandler.removeCallbacks(resetSendSongAfterDelayRunnable);
                        resetSendSongAfterDelayHandler.postDelayed(resetSendSongAfterDelayRunnable, 3500);
                    }
                }

                if (StaticVariables.mLyrics != null) {
                    FullscreenActivity.myLyrics = StaticVariables.mLyrics;
                } else {
                    FullscreenActivity.myLyrics = "";
                }

                // Clear the old headings (presention order looks for these)
                FullscreenActivity.foundSongSections_heading = new ArrayList<>();

                if (FullscreenActivity.isSong) {
                    // Determine formats to be used for capo / transpose
                    // Note: If chordformat = 0 (detect) then chordFormatUsePreferred is false
                    try {
                        if (preferences.getMyPreferenceBoolean(StageMode.this,"chordFormatUsePreferred",true)) {
                            // Set StaticVariables.detectedChordFormat to the preferred format
                            StaticVariables.detectedChordFormat = preferences.getMyPreferenceInt(StageMode.this,"chordFormat",1);
                        } else {
                            // Sets StaticVariables.detectedChordFormat to the detected format
                            Transpose.checkChordFormat();
                        }
                        // Set the newChordFormat default to be the same
                        StaticVariables.newChordFormat = StaticVariables.detectedChordFormat;
                    } catch (Exception e) {
                        Log.d(TAG, "Error checking the chord format");
                    }

                    // IV - PrepareSongSections prepares
                    // FullscreenActivity.myLyrics,
                    // StaticVariables.songSections, StaticVariables.songSectionsLabels, StaticVariables.songSectionsTypes,
                    // StaticVariables.sectionContents, StaticVariables.sectionLineTypes
                    processSong.prepareSongSections(StageMode.this, preferences, storageAccess);
                    if (StaticVariables.whichMode.equals("Performance")) {
                        // Put the song back together for checking for splitpoints
                        processSong.rebuildParsedLyrics(StaticVariables.songSections.length);
                        FullscreenActivity.numrowstowrite = FullscreenActivity.myParsedLyrics.length;

                        // Look for song split points if the lyrics are long enough
                        if (FullscreenActivity.numrowstowrite > 1) {
                            try {
                                processSong.lookForSplitPoints();
                            } catch (Exception e) {
                                Log.d(TAG, "Split point not worth it");
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
                    // Activities are kept to a minimum before content load
                    // Fix the page flags
                    setWindowFlags();
                    setWindowFlagsAdvanced();

                    // Put the title of the song in the taskbar
                    if (StaticVariables.whichSongFolder.startsWith("../Received")) {
                        String text = "▾" + processSong.getSongTitle();
                        songtitle_ab.setText(text);
                    } else if (StaticVariables.whichSongFolder.startsWith("../")) {
                        String text = "≡" + processSong.getSongTitle();
                        songtitle_ab.setText(text);
                    } else {
                        songtitle_ab.setText(processSong.getSongTitle());
                    }
                    songkey_ab.setText(processSong.getSongKey());
                    songauthor_ab.setText(processSong.getSongAuthor());
                    songcapo_ab.setText("");

                    // Any errors to show?
                    if (!StaticVariables.myToastMessage.equals("")) {
                        ShowToast.showToast(StageMode.this);
                    }

                    // Get the current orientation
                    FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

                    // Determine file type
                    storageAccess.determineFileTypeByExtension();

                    // IV - File does not exist - the loadXML 'not found' song needs to be revealed
                    if (!songUriExists) {
                        FullscreenActivity.isSong = true;
                        FullscreenActivity.isPDF = false;
                        FullscreenActivity.isImage = false;
                    }

                    // IV - Background colour set to white for PDF and Image
                    if (FullscreenActivity.isPDF) {
                        mypage.setBackgroundColor(StaticVariables.white);
                        loadPDF();

                    } else if (FullscreenActivity.isImage) {
                        mypage.setBackgroundColor(StaticVariables.white);
                        loadImage();

                    } else if (FullscreenActivity.isSong) {
                        mypage.setBackgroundColor(lyricsBackgroundColor);
                        //Prepare the song views
                        prepareView();
                    }

                    // If the user is shown the 'Welcome to OpenSongApp' file and their song lists are empty,
                    // open the find new songs menu
                    if (StaticVariables.mTitle.equals("Welcome to OpenSongApp") &&
                            sqLiteHelper.getSongsCount(StageMode.this)<1) {
                        StaticVariables.whichOptionMenu = "FIND";
                        prepareOptionMenu();
                        Handler find = new Handler();
                        find.postDelayed(() -> openMyDrawers("option"), 2000);
                    }
                    // Send the midi data if we can
                    if (preferences.getMyPreferenceBoolean(StageMode.this,"midiSendAuto",false) &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        sendMidi();
                    }

                    // If we have created, or converted a song format (e.g from OnSong or ChordPro), rebuild the database
                    // or pull up the edit screen
                    if (FullscreenActivity.needtoeditsong) {
                        FullscreenActivity.whattodo = "editsong";
                        FullscreenActivity.alreadyloading = false;
                        openFragment();
                    } else if (FullscreenActivity.needtorefreshsongmenu) {
                        if (sqLite!=null && sqLite.getSongid()!=null) {
                            sqLite = sqLiteHelper.getSong(StageMode.this, sqLite.getSongid());
                            sqLiteHelper.updateSong(StageMode.this, sqLite);
                        }
                    }

                    delayactionBarHide.removeCallbacks(hideActionBarRunnable);
                    if (preferences.getMyPreferenceBoolean(StageMode.this,"hideActionBar",false)) {
                        delayactionBarHide.postDelayed(hideActionBarRunnable, 1000);
                    }

                    if (ab != null) {
                        ab.show();
                    }

                    // Get the SQLite stuff
                    if (!StaticVariables.whichSongFolder.startsWith("..")) {
                        String songId = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;

                        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                            nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(StageMode.this, storageAccess, preferences, songId);
                        }
                        sqLite = sqLiteHelper.getSong(StageMode.this, songId);

                        if (!StaticVariables.mTitle.equals("Welcome to OpenSongApp")) {
                            // IV - Backstop, if the song is not found add a basic song entry. Handles Nearby 'imported' songs
                            if (sqLite==null) {
                                sqLiteHelper.createImportedSong(StageMode.this, StaticVariables.whichSongFolder, StaticVariables.songfilename, StaticVariables.songfilename, "", "", "", "", "", "");
                                sqLite = sqLiteHelper.getSong(StageMode.this, songId);
                            }

                            // If this song isn't indexed, set its details
                            if (sqLite!=null && (sqLite.getLyrics()==null || sqLite.getLyrics().equals(""))) {
                                sqLite = sqLiteHelper.setSong(sqLite);
                                sqLiteHelper.updateSong(StageMode.this,sqLite);
                            }
                        }
                    }

                    // IV - After any sqLite update has occurred
                    prepareSongMenu();

                    // Make sure all dynamic (scroll and set) buttons display
                    onScrollAction();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // IV - Store song details
            preferences.setMyPreferenceString(StageMode.this, "songfilename",StaticVariables.songfilename);
            preferences.setMyPreferenceString(StageMode.this,"whichSongFolder",StaticVariables.whichSongFolder);

            // IV - True if a reload, this sets loadsong back to standard mode
            StaticVariables.reloadOfSong = false;
            FullscreenActivity.alreadyloading = false;
        }
    }

    // This bit listens for key presses (for page turn and scroll)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        keyRepeatCount++;
        // If we are using an AirTurn pedal it will send onKeyDown then onKeyUp and quickly repeat for long press
        // Set a listener for the keyRepeatTime + 100ms  to detect the last (no change in keyRepeatCount) onKeyUp and do a short press
        if (preferences.getMyPreferenceBoolean(StageMode.this, "airTurnMode", false)) {
            int keyRepeatTime = preferences.getMyPreferenceInt(StageMode.this,"keyRepeatTime",400) + 100;
            final int initialAirTurnCount = keyRepeatCount;
            // Check again after the keyRepeatTime + 100ms to see if the count has increased.  If it hasn't, short press action should be called.
            new Handler().postDelayed(() -> {
                if (initialAirTurnCount==keyRepeatCount) {
                    doShortPressAction(keyCode, event);
                }
            }, keyRepeatTime);
            return false;
        } else {
            // IV - If a false short press event when long press is active (fragment use will do this) correct
            if (blockActionOnKeyUp) {
                blockActionOnKeyUp = false;
                blockKeyAction = false;
                return false;
            } else {
                doShortPressAction(keyCode, event);
                return true;
            }
        }
    }

    private void doShortPressAction(int keyCode, KeyEvent event) {
        keyRepeatCount = 0;
        event.startTracking();
        View rf = getCurrentFocus();
        if (rf != null) {
            rf.clearFocus();
        }

        if (!blockKeyAction) {
            // Reset immersive mode
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                restoreTranslucentBarsDelayed();
            }

            // Eat the long press event so the keyboard doesn't come up.
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (!event.isLongPress()) {
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

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal1Code", 21)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal1ShortPressAction", "prev"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal2Code", 22)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal2ShortPressAction", "next"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal3Code", 19)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal3ShortPressAction", "prev"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal4Code", 20)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal4ShortPressAction", "next"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal5Code", 92)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal5ShortPressAction", "prev"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal6Code", 93)) {
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal6ShortPressAction", "next"));
            }
        } else {
            blockKeyAction = false;
        }
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class PrepareSongMenu extends AsyncTask<Object, Void, String> {

        ArrayList<SQLite> songsInFolder;
        ArrayList<SQLite> childFolders;

        @Override
        protected void onPreExecute() {
            menuCount_TextView.setText("");
            menuCount_TextView.setVisibility(View.GONE);
            // IV - Recently fixed to work but disabled as it is too slow!
            //menuFolder_TextView.setText(getString(R.string.wait));
            song_list_view.setAdapter(null);
            LinearLayout indexLayout = findViewById(R.id.side_index);
            indexLayout.removeAllViews();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                songsInFolder = sqLiteHelper.getSongsInFolder(StageMode.this, StaticVariables.whichSongFolder);
                // IV - Recently fixed to work but disabled as it is too slow!
                // Remove any that aren't there (due to updating something) - permanently fixed on reboot
                //for (SQLite s:songsInFolder) {
                //    if (s!=null && s.getFolder()==null && s.getFilename()!=null) {
                //        Uri u = storageAccess.getUriForItem(StageMode.this, preferences, "Songs", StaticVariables.whichSongFolder, s.getFilename());
                //        if (!storageAccess.uriExists(StageMode.this, u)) {
                //            songsInFolder.remove(s);
                //            // IV - We have a DB entry for a missing song - so delete it
                //            sqLiteHelper.deleteSong(StageMode.this, StaticVariables.whichSongFolder + "/" + s.getFilename());
                //        }
                //    }
                //}
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
                        String foundsongtitle = songsInFolder.get(i).getTitle();
                        String foundsongauthor = songsInFolder.get(i).getAuthor();
                        String foundsongkey = songsInFolder.get(i).getKey();

                        if (foundsongfilename == null) {
                            foundsongfilename = getString(R.string.error);
                        }
                        if (foundsongtitle == null || foundsongtitle.equals("")) {
                            foundsongtitle = foundsongfilename;
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
                                //TODO GE commit changes to display of title  however SQL does not yet order by title. Both filename and title order, user choice, are needed.
                                //foundsongtitle, foundsongauthor, foundsongkey, isinset);
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

                    findSongInFolders();

                    // Flick the song drawer open once it is ready
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

    // IV - code supporting intentional page turns when using pedal for next/previous.
    // IV - 'Are you sure?' is displayed and the user must stop, wait and can repeat the action to continue after 2 seconds (an intentional action)
    // IV - After continue there is a 10s grace period where further pedal use is not tested.  Any pedal 'page' or 'scroll' use extends a further 10s grace period.
    private void pedalPreviousAndNextConfirm() {
         // If we confirm a move then we will ignore the move
        if (StaticVariables.pedalPreviousAndNextNeedsConfirm) {
            StaticVariables.myToastMessage = getString(R.string.pedal) + " - " + getString(R.string.areyousure);
            ShowToast.showToast(StageMode.this);
            StaticVariables.pedalPreviousAndNextNeedsConfirm = false;
            pedalPreviousAndNextNeedsConfirmHandler.postDelayed(pedalPreviousAndNextNeedsConfirmRunnable, 10000);
            StaticVariables.pedalPreviousAndNextIgnore = true;
            // Use a runnable to end the ignore period.
            pedalPreviousAndNextIgnoreHandler.postDelayed(pedalPreviousAndNextIgnoreRunnable, 2000);
         }
    }

    private void PedalNeedsConfirmTrueAfterDelay() {
        // After a NeedsConfirm prompt the user can use prev/next without prompt during a grace period.
        // This is extended on any use of a pedal prev/next/up/down.
        if (!StaticVariables.pedalPreviousAndNextNeedsConfirm) {
            pedalPreviousAndNextNeedsConfirmHandler.removeCallbacks(pedalPreviousAndNextNeedsConfirmRunnable);
            pedalPreviousAndNextNeedsConfirmHandler.postDelayed(pedalPreviousAndNextNeedsConfirmRunnable, 10000);
        }
    }

    private void pedalPrevious() {
        // This can be cancelled
        boolean goToItemRequired = true;

        if (preferences.getMyPreferenceBoolean(StageMode.this,"pedalScrollBeforeMove",true)) {
            if (mDrawerLayout.isDrawerOpen(songmenu)) {
                // Scroll the song menu up
                scrollMenu("up");
            } else {
                // IV - Added handling for PDF pages
                if (!drawerOrFragmentActive && (checkCanScrollUp() || (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0))) {
                    if (scrollUpButton != null && scrollUpButton.getVisibility() == View.VISIBLE) {
                        CustomAnimations.animateFAB(scrollUpButton, StageMode.this);
                    }
                    doScrollUp();
                    PedalNeedsConfirmTrueAfterDelay();
                    goToItemRequired = false;
                }
            }
        }

        // If pedal used again in the ignore period (which starts with song change 'are you sure' warning)  - extend the ignore period
        if (StaticVariables.pedalPreviousAndNextIgnore) {
            pedalPreviousAndNextIgnoreHandler.removeCallbacks(pedalPreviousAndNextIgnoreRunnable);
            pedalPreviousAndNextIgnoreHandler.postDelayed(pedalPreviousAndNextIgnoreRunnable, 2000);
        }

        // Ignore the move if in the ignore period or a drawer is open
        goToItemRequired = goToItemRequired && !StaticVariables.pedalPreviousAndNextIgnore && !drawerOrFragmentActive;

        if (goToItemRequired) {
            // Consider a song change warning
            if (preferences.getMyPreferenceBoolean(StageMode.this, "pedalShowWarningBeforeMove", false)) {
                // IV - We warn only if we can successfully move
                if (StaticVariables.setView) {
                    checkCanGoTo();
                    // If in a set and able to move
                    if (StaticVariables.canGoToPrevious) {
                        pedalPreviousAndNextConfirm();
                    }
                } else {
                    // If in a folder and able to move
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
                        try {
                            if (!StaticVariables.songfilename.equals(filenamesSongsInFolder.get(FullscreenActivity.previousSongIndex)) && !isfolder) {
                                pedalPreviousAndNextConfirm();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Ignore the move if in the ignore period
                goToItemRequired = !StaticVariables.pedalPreviousAndNextIgnore;
            }

            if (goToItemRequired) {
                if (setBackButton!=null && setBackButton.getVisibility() == View.VISIBLE) {
                    CustomAnimations.animateFAB(setBackButton, StageMode.this);
                }
                goToPreviousItem();
                if (preferences.getMyPreferenceBoolean(StageMode.this, "pedalShowWarningBeforeMove", false)) {
                    PedalNeedsConfirmTrueAfterDelay();
                }
            }
        }
    }

    private void pedalNext() {
        // This can be cancelled
        boolean goToItemRequired = true;

        if (preferences.getMyPreferenceBoolean(StageMode.this,"pedalScrollBeforeMove",true)) {
            if (mDrawerLayout.isDrawerOpen(songmenu)) {
                // Scroll the song menu down
                scrollMenu("down");
            } else {
                // IV - Added handling for PDF pages
                if (!drawerOrFragmentActive && (checkCanScrollDown() || (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent < (FullscreenActivity.pdfPageCount - 1)))) {
                    if (scrollDownButton != null && scrollDownButton.getVisibility() == View.VISIBLE) {
                        CustomAnimations.animateFAB(scrollDownButton, StageMode.this);
                    }
                    doScrollDown();
                    PedalNeedsConfirmTrueAfterDelay();
                    // we have done a scroll so cancel the item move
                    goToItemRequired = false;
                }
            }
        }

        // If pedal used again in the ignore period (which starts with song change 'are you sure' warning)  - extend the ignore period
        if (StaticVariables.pedalPreviousAndNextIgnore) {
            pedalPreviousAndNextIgnoreHandler.removeCallbacks(pedalPreviousAndNextIgnoreRunnable);
            pedalPreviousAndNextIgnoreHandler.postDelayed(pedalPreviousAndNextIgnoreRunnable, 2000);
        }
        // Ignore the move if in the ignore period
        goToItemRequired = goToItemRequired && !StaticVariables.pedalPreviousAndNextIgnore && !drawerOrFragmentActive;

        if (goToItemRequired) {
            // Consider a song change warning
            if (preferences.getMyPreferenceBoolean(StageMode.this, "pedalShowWarningBeforeMove", false)) {
                // IV - We warn only if we can successfully move
                if (StaticVariables.setView) {
                    checkCanGoTo();
                    // If in a set and able to move
                    if (StaticVariables.canGoToNext) {
                        pedalPreviousAndNextConfirm();
                    }
                } else {
                    // If in a folder and able to move
                    boolean isfolder = false;
                    try {
                        if (FullscreenActivity.nextSongIndex < filenamesSongsInFolder.size()) {
                            if (FullscreenActivity.nextSongIndex > -1) {
                                Uri uri = storageAccess.getUriForItem(StageMode.this, preferences, "Songs", "",
                                        filenamesSongsInFolder.get(FullscreenActivity.nextSongIndex));
                                if (storageAccess.uriExists(StageMode.this, uri) && !storageAccess.uriIsFile(StageMode.this, uri)) {
                                    isfolder = true;
                                }
                            }
                            if (FullscreenActivity.nextSongIndex != -1 &&
                                    !StaticVariables.songfilename.equals(filenamesSongsInFolder.get(FullscreenActivity.nextSongIndex)) &&
                                    !isfolder) {
                                pedalPreviousAndNextConfirm();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Ignore the move if in the ignore period
                goToItemRequired = !StaticVariables.pedalPreviousAndNextIgnore;
            }

            if (goToItemRequired) {
                if (setForwardButton!=null && setForwardButton.getVisibility() == View.VISIBLE) {
                    CustomAnimations.animateFAB(setForwardButton, StageMode.this);
                }
                goToNextItem();
                if (preferences.getMyPreferenceBoolean(StageMode.this, "pedalShowWarningBeforeMove", false)) {
                    PedalNeedsConfirmTrueAfterDelay();
                }
            }
        }
    }
    private void pedalUp() {
        if (mDrawerLayout.isDrawerOpen(songmenu)) {
            // Scroll the song menu up
            scrollMenu("up");
        } else {
            if (!drawerOrFragmentActive) {
                if (StaticVariables.whichMode.equals("Stage") && preferences.getMyPreferenceBoolean(StageMode.this, "pedalScrollBeforeMove", true) && !checkCanScrollUp()) {
                    pedalPrevious();
                // IV - Added handling for PDF pages
                } else if (checkCanScrollUp() || (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0)) {
                    CustomAnimations.animateFAB(scrollUpButton, StageMode.this);
                    doScrollUp();
                }
                if (preferences.getMyPreferenceBoolean(StageMode.this, "pedalShowWarningBeforeMove", false)) {
                    PedalNeedsConfirmTrueAfterDelay();
                }
            }
        }
    }
    private void pedalDown() {
        if (mDrawerLayout.isDrawerOpen(songmenu)) {
            // Scroll the song menu down
            scrollMenu("down");
        } else {
            if (!drawerOrFragmentActive) {
                if (StaticVariables.whichMode.equals("Stage") && preferences.getMyPreferenceBoolean(StageMode.this, "pedalScrollBeforeMove", true) && !checkCanScrollDown()) {
                    pedalNext();
                // IV - Added handling for PDF pages
                } else if (checkCanScrollDown() || (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent < (FullscreenActivity.pdfPageCount - 1))) {
                    CustomAnimations.animateFAB(scrollDownButton, StageMode.this);
                    doScrollDown();
                }
                if (preferences.getMyPreferenceBoolean(StageMode.this, "pedalShowWarningBeforeMove", false)) {
                    PedalNeedsConfirmTrueAfterDelay();
                }
            }
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        boolean actionrecognised = doLongKeyPressAction(keyCode);

        if (actionrecognised) {
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private boolean doLongKeyPressAction(int keyCode) {
        keyRepeatCount = 0;
        boolean actionrecognised = false;

        if (!blockKeyAction) {
            if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal1Code", 21)) {
                actionrecognised = true;
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal1LongPressAction", "songmenu"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal2Code", 22)) {
                actionrecognised = true;
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal2LongPressAction", "editset"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal3Code", 19)) {
                actionrecognised = true;
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal3LongPressAction", "songmenu"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal4Code", 20)) {
                actionrecognised = true;
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal4LongPressAction", "editset"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal5Code", 92)) {
                actionrecognised = true;
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal5LongPressAction", "songmenu"));

            } else if (keyCode == preferences.getMyPreferenceInt(StageMode.this, "pedal6Code", 93)) {
                actionrecognised = true;
                doPedalAction(preferences.getMyPreferenceString(StageMode.this, "pedal6LongPressAction", "editset"));
            }
        }

        // IV - After the first long press action block further action.  Sequences will now end with a blocked short press which only sets blockKeyAction false
        blockKeyAction = true;

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
            // IV - HighlightNotes are not (yet) correctly scaling/positioning on scale so remove!
            highlightNotes.setVisibility(View.GONE);
            FullscreenActivity.highlightOn = false;
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
                glideimage_FrameLayout.post(() -> {
                    glideimage_ScrollView.scrollTo(0, 0);
                    glideimage_HorizontalScrollView.scrollTo(0, 0);
                    glideimage.setScaleX(1.0f);
                    glideimage.setScaleY(1.0f);

                    // If the width of the song is smaller than the screen width, make the scrollview the screen width
                    // Otherwise make it expand to fit the song
                    hsvlp.width = Math.max(newwidth, screenwidth);

                    //Keep the scrollview the height of the page
                    hsvlp.height = screenheight;

                    glideimage_ScrollView.setLayoutParams(hsvlp);
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
                songscrollview.post(() -> {
                    songscrollview.scrollTo(0, 0);
                    horizontalscrollview.scrollTo(0, 0);

                    // If the width of the song is smaller than the screen width, make the scrollview the screen width
                    // Otherwise make it expand to fit the song
                    hsvlp.width = Math.max(newwidth, screenwidth);

                    //Keep the scrollview the height of the page
                    hsvlp.height = screenheight;

                    songscrollview.setLayoutParams(hsvlp);
                    songscrollview.requestLayout();

                });
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        try {
            int action = ev.getAction();
            // WOULD BE BETTER IF THIS WAS CALLED ON SOME KIND OF ONSCROLL LISTENER
            // IV - Do not do pinch and zoom when autoscrolling - autoscroll code is not compatible with it
            if (!StaticVariables.isautoscrolling) {
                scaleGestureDetector.onTouchEvent(ev);
            }
            if (action == MotionEvent.ACTION_MOVE) {
                // Set a runnable to check the scroll position
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Temporarily pause any running autoscroll
        pauseAutoscroll();
        return super.dispatchTouchEvent(ev);
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class PrepareOptionMenu extends AsyncTask<Object, Void, String> {

        public void onPreExecute() {
            try {
                optionmenu = findViewById(R.id.optionmenu);
                try {
                    optionmenu.removeAllViews();
                } catch (Exception e) {
                    Log.d(TAG, "Error removing view");
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
            OptionMenuListeners.updateMenuVersionNumber(StageMode.this, findViewById(R.id.menu_version_bottom));
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
        doEdit();
    }

    // Add to set
    private void gesture3() {
        // Add to end of set
        String newval = preferences.getMyPreferenceString(StageMode.this,"setCurrent","") + StaticVariables.whatsongforsetwork;
        preferences.setMyPreferenceString(StageMode.this,"setCurrent",newval);
        // Tell the user that the song has been added.
        showToastMessage("\"" + StaticVariables.songfilename + "\" "
                + getResources().getString(R.string.addedtoset));
        // Vibrate to indicate something has happened
        DoVibrate.vibrate(StageMode.this, 50);

        // IV - prepareOptionMenu also prepares the set list
        prepareOptionMenu();
    }

    // Redraw the lyrics page
    private void gesture4() {
        StaticVariables.reloadOfSong = true;
        FullscreenActivity.pdfPageCurrent = 0;
        StaticVariables.currentSection = 0;
        // Send section to other devices
        if (StaticVariables.isHost && StaticVariables.isConnected) {
            sendSongSectionToConnected();
        }
        loadSong();
    }

    @Override
    // Stop or start autoscroll
    public void gesture5() {
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

            // IV - Proceed if drawers closed...
            if (!mDrawerLayout.isDrawerOpen(songmenu) && !mDrawerLayout.isDrawerVisible(songmenu) &&
                    !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerVisible(optionmenu)) {
                // IV - ... and in centre of screen
                // GE reduced threshold to 20% - on a smaller screen 33% touch zone too small, 60% better.
                // larger screen 33% dead space too large, 20% better
                int width = horizontalscrollview.getRight();
                int height = horizontalscrollview.getBottom();
                int x = (int) e.getX();
                int y = (int) e.getY();
                if ((x > (width * 0.2)) && (x < (width * 0.8)) && (y > height * 0.2) && (y < (height * 0.8))) {
                    // Now find out which gesture we've gone for
                    switch (preferences.getMyPreferenceInt(StageMode.this, "gestureScreenDoubleTap", 2)) {
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

            // IV - Proceed if drawers closed...
            if (!mDrawerLayout.isDrawerOpen(songmenu) && !mDrawerLayout.isDrawerVisible(songmenu) &&
                    !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerVisible(optionmenu)) {
                // IV - ... and in centre of screen
                int width = horizontalscrollview.getRight();
                int height = horizontalscrollview.getBottom();
                int x = (int) e.getX();
                int y = (int) e.getY();
                if ((x > (width * 0.2)) && (x < (width * 0.8)) && (y > height * 0.2) && (y < (height * 0.8))) {
                    // Now find out which gesture we've gone for
                    switch (preferences.getMyPreferenceInt(StageMode.this, "gestureScreenLongPress", 0)) {
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
                    super.onLongPress(e);
                }
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            // Temporarily pause any running autoscroll
            pauseAutoscroll();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (FullscreenActivity.tempswipeSet.equals("disable")  || !preferences.getMyPreferenceBoolean(StageMode.this, "swipeForSongs", true)) {
                // Currently disabled swiping to let screen finish drawing or not required
                return false;
            }

            // We accept a fling if it is a certain velocity (swipeMinimumVelocity)
            // and essentially in the Y or X axis for the direction of swipe (swipeMaxDistanceYError)
            // and exceeds a certain distance (swipeMinimumDistance)
            // and does not start on the relevant mypage edges

            try {
                // Check for an acceptable Y-axis fling
                if (Math.abs(velocityX) > preferences.getMyPreferenceInt(StageMode.this, "swipeMinimumVelocity", 600) &&
                        Math.abs(e1.getY() - e2.getY()) < preferences.getMyPreferenceInt(StageMode.this,"swipeMaxDistanceYError",200) &&
                        Math.abs(e1.getX() - e2.getX()) > preferences.getMyPreferenceInt(StageMode.this, "swipeMinimumDistance", 250) &&
                        e1.getX() > 40 &&
                        e1.getX() < (mypage.getWidth() - 40)) {

                    // IV - Flag this as a swipe
                    StaticVariables.setMoveDirection = "swipe";

                    if (e1.getX() > e2.getX()) {
                        setForwardButton.performClick();
                    } else {
                        setBackButton.performClick();
                    }
                    return true;
                }

                 // For PDF page scroll - check for an acceptable Y-axis fling
                if (FullscreenActivity.isPDF &&
                        Math.abs(velocityY) > preferences.getMyPreferenceInt(StageMode.this, "swipeMinimumVelocity", 600) &&
                        Math.abs(e1.getX() - e2.getX()) < preferences.getMyPreferenceInt(StageMode.this, "swipeMaxDistanceYError", 200) &&
                        Math.abs(e1.getY() - e2.getY()) > preferences.getMyPreferenceInt(StageMode.this, "swipeMinimumDistance", 250) &&
                        e1.getY() > 40 &&
                        e1.getY() < (mypage.getHeight() - 40)) {

                    // IV - Only act if not able to scroll within a page
                    if (e1.getY() > e2.getY()) {
                        if (!checkCanScrollDown()) {
                            scrollDownButton.performClick();
                        }
                    } else {
                        if (!checkCanScrollUp()) {
                            scrollUpButton.performClick();
                        }
                    }
                    return true;
                }
                return false;

            } catch (Exception e) {
                Log.d(TAG, "error");
            }
            return false;
        }
    }

    // Start or stop the metronome
    @Override
    public void gesture7() {
        StaticVariables.metronomeok = Metronome.isMetronomeValid();
        if (StaticVariables.metronomeok || StaticVariables.clickedOnMetronomeStart) {
            // IV - clickedOnMetronomeStart is set elsewhere (Metronome class)
            Metronome.startstopMetronome(StageMode.this,
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
        StaticVariables.panicRequired = false;
        StaticVariables.infoBarChangeRequired = true;
        StaticVariables.forcecastupdate = true;
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        updateDisplays();
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo info, int reason) {
            menuButtonLongPressActions();
            super.onRouteSelected(router,info,reason);
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            try {
                updateDisplays();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRouteUnselected(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo info, int reason) {
            menuButtonLongPressActions();
            super.onRouteUnselected(router,info,reason);
            teardown();
            mSelectedDevice = null;
            FullscreenActivity.isPresenting = false;
            FullscreenActivity.isHDMIConnected = false;
        }

        void teardown() {
            menuButtonLongPressActions();
            try {
                CastRemoteDisplayLocalService.stopService();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (FullscreenActivity.hdmi != null) {
                    FullscreenActivity.hdmi.dismiss();
                    FullscreenActivity.hdmi = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRouteAdded(@NonNull MediaRouter mediaRouter, @NonNull MediaRouter.RouteInfo routeInfo) {
            menuButtonLongPressActions();
        }

        @Override
        public void onRouteRemoved(@NonNull MediaRouter mediaRouter, @NonNull MediaRouter.RouteInfo routeInfo) {
            menuButtonLongPressActions();
        }

        @Override
        public void onRouteChanged(@NonNull MediaRouter mediaRouter, @NonNull MediaRouter.RouteInfo routeInfo) {
            menuButtonLongPressActions();
        }

        @Override
        public void onRouteVolumeChanged(@NonNull MediaRouter mediaRouter, @NonNull MediaRouter.RouteInfo routeInfo) {
            menuButtonLongPressActions();
        }
    }

    private void playPad() {
        doCancelAsyncTask(play_pads);
        play_pads = new PlayPads();
        try {
            play_pads.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class PlayPads extends AsyncTask<Void, Void, Integer> {
        int which;
        int path;
        boolean validlinkaudio;
        boolean error;

        PlayPads() {}

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
            // Makes sure pads are in a known state
            fadeoutPad();

            // Default to 0, overridden if a pad is prepareAsync'd
            FullscreenActivity.whichPad = 0;
            try {
                if (!cancelled) {
                    if (StaticVariables.mPadFile.equals(getResources().getString(R.string.pad_auto)) ||
                            StaticVariables.mPadFile.equals("")) {
                        boolean custompad;
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
                                // IV - These can be slow to prepare. we may need to display just the pad icon to indicate a pending start
                                if (backingtrackProgress.getVisibility() == View.VISIBLE) {
                                    padcurrentTime_TextView.setText("");
                                    padTimeSeparator_TextView.setText("");
                                    padtotalTime_TextView.setText("");
                                    // Note that if start completes within 1s then the settings above will have been overwritten
                                    Handler h = new Handler();
                                    h.postDelayed(() -> backingtrackProgress.setVisibility(View.VISIBLE),1000);
                                }
                                path = getResources().getIdentifier(StaticVariables.pad_filename, "raw", getPackageName());
                                try {
                                    afd = getResources().openRawResourceFd(path);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            // IV - Do work IF we have a pad to start
                            if (padpath != null || afd != null) {
                                try {
                                    PadFunctions.getPad1Status();
                                    PadFunctions.getPad2Status();
                                    if (!StaticVariables.pad1Playing || StaticVariables.pad2Playing) {
                                        FullscreenActivity.whichPad = 1;
                                        FullscreenActivity.mPlayer1.stop();
                                        FullscreenActivity.mPlayer1.reset();
                                        StaticVariables.pad1Fading = false;
                                        FullscreenActivity.mPlayer1.setOnPreparedListener(new Player1Prepared());
                                        if (custompad) {
                                            FullscreenActivity.mPlayer1.setDataSource(StageMode.this, Uri.parse(padpath));
                                        } else {
                                            FullscreenActivity.mPlayer1.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                            afd.close();
                                        }
                                        FullscreenActivity.mPlayer1.prepareAsync();
                                    } else {
                                        FullscreenActivity.whichPad = 2;
                                        FullscreenActivity.mPlayer2.stop();
                                        FullscreenActivity.mPlayer2.reset();
                                        StaticVariables.pad2Fading = false;
                                        FullscreenActivity.mPlayer2.setOnPreparedListener(new Player2Prepared());
                                        if (custompad) {
                                            FullscreenActivity.mPlayer2.setDataSource(StageMode.this, Uri.parse(padpath));
                                        } else {
                                            FullscreenActivity.mPlayer2.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                            afd.close();
                                        }
                                        FullscreenActivity.mPlayer2.prepareAsync();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // Prepare the link audio file
                    if (StaticVariables.mPadFile.equals(getResources().getString(R.string.link_audio))) {
                        try {
                            StorageAccess storageAccess = new StorageAccess();
                            Uri uri = storageAccess.fixLocalisedUri(StageMode.this, preferences, StaticVariables.mLinkAudio);
                            if (!StaticVariables.pad1Playing || StaticVariables.pad2Playing) {
                                FullscreenActivity.whichPad = 1;
                                FullscreenActivity.mPlayer1.stop();
                                FullscreenActivity.mPlayer1.reset();
                                StaticVariables.pad1Fading = false;
                                FullscreenActivity.mPlayer1.setOnPreparedListener(new Player1Prepared());
                                FullscreenActivity.mPlayer1.setDataSource(StageMode.this, uri);
                                FullscreenActivity.mPlayer1.prepareAsync();
                            } else {
                                FullscreenActivity.whichPad = 2;
                                FullscreenActivity.mPlayer2.stop();
                                FullscreenActivity.mPlayer2.reset();
                                StaticVariables.pad2Fading = false;
                                FullscreenActivity.mPlayer2.setOnPreparedListener(new Player2Prepared());
                                FullscreenActivity.mPlayer2.setDataSource(StageMode.this, uri);
                                FullscreenActivity.mPlayer2.prepareAsync();
                            }
                            validlinkaudio = true;
                        } catch (Exception e) {
                            validlinkaudio = false;
                            Log.d(TAG, "Something went wrong with the media");
                        }

                        if (!validlinkaudio) {
                            // Problem with link audio so don't use it
                            StaticVariables.myToastMessage = getResources().getString(R.string.link_audio) + " - " +
                                    getResources().getString(R.string.file_type_unknown);
                            error = true;
                        }
                    }
                    if (error) {
                        ShowToast.showToast(StageMode.this);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            StaticVariables.clickedOnPadStart = !(FullscreenActivity.whichPad == 0);
            which = FullscreenActivity.whichPad;
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
        // Replace the FolderPicker class for Lollipop+
        Intent intent;
        // Start location
        Uri uri = storageAccess.getUriForItem(StageMode.this,preferences,"","","");
        if (storageAccess.lollipopOrLater()) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            }

        } else {
            intent = new Intent(this, FolderPicker.class);
            intent.putExtra("title", s);
            intent.putExtra("pickFiles", true);
            if (StaticVariables.uriTree!=null) {
                intent.putExtra("location", StaticVariables.uriTree.getPath());
            }
        }
        startActivityForResult(intent, StaticVariables.REQUEST_FILE_CHOOSER);
    }

    private void  songTransition_QOS () {
        int songTransition_QOS_time = 400;
        // To give a more consistent speed of song change sleep when render is quick!
        // This is followed by animateout
        long diff = (System.currentTimeMillis() - songTransitionStart);
        if (diff < songTransition_QOS_time) {
            try {
                Thread.sleep(songTransition_QOS_time - diff);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void menuButtonLongPressActions () {
        // IV - Called on resume and on Cast state change events as the dynamic icon causes a redraw of the menu without trigger of normal menu change calls!
        new Handler().postDelayed(() -> {
            // IV - Support long press of song icon to enter Transpose
            final View view = findViewById(R.id.action_search);

            if (view == null) {
                // Not there yet! Try again.
                menuButtonLongPressActions();
            } else {
                view.setOnLongClickListener(v -> {
                    if (FullscreenActivity.isSong) {
                        FullscreenActivity.whattodo = "transpose";
                        openFragment();
                    } else {
                        StaticVariables.myToastMessage = getResources().getString(R.string.not_allowed);
                        ShowToast.showToast(StageMode.this);
                    }
                    return true;
                });

                // IV - Support long press of '+' icon to directly add the song to the end of the set as a variation
                final View view2 = findViewById(R.id.set_add);

                if (view2 != null) {
                    view2.setOnLongClickListener(v -> {
                        if (!StaticVariables.whichSongFolder.startsWith("..") ||
                                (StaticVariables.whichSongFolder.equals("../Received") && !StaticVariables.receivedSongfilename.equals("") && !StaticVariables.receivedSongfilename.equals("ReceivedSong"))) {
                            PopUpLongSongPressFragment.addtoSet(StageMode.this, preferences);
                            setActions.prepareSetList(StageMode.this, preferences);
                            StaticVariables.indexSongInSet = StaticVariables.mSetList.length - 1;
                            PopUpSetViewNew.makeVariation(StageMode.this, preferences);
                            loadSong();
                        } else {
                            // Not a song
                            StaticVariables.myToastMessage = getResources().getString(R.string.not_allowed);
                            ShowToast.showToast(StageMode.this);
                        }
                        return true;
                    });
                }
            }
        }, 200);
    }
}