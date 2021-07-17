package com.garethevans.church.opensongtablet;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.make;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.abcnotation.ABCNotation;
import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.appdata.AlertChecks;
import com.garethevans.church.opensongtablet.appdata.AlertInfoBottomSheet;
import com.garethevans.church.opensongtablet.appdata.CheckInternet;
import com.garethevans.church.opensongtablet.appdata.FixLocale;
import com.garethevans.church.opensongtablet.appdata.SetTypeFace;
import com.garethevans.church.opensongtablet.appdata.VersionNumber;
import com.garethevans.church.opensongtablet.autoscroll.AutoscrollActions;
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.ccli.SettingsCCLI;
import com.garethevans.church.opensongtablet.chords.Transpose;
import com.garethevans.church.opensongtablet.controls.Gestures;
import com.garethevans.church.opensongtablet.controls.PageButtons;
import com.garethevans.church.opensongtablet.controls.PedalActions;
import com.garethevans.church.opensongtablet.controls.PedalsFragment;
import com.garethevans.church.opensongtablet.controls.SwipeFragment;
import com.garethevans.church.opensongtablet.controls.Swipes;
import com.garethevans.church.opensongtablet.customviews.DrawNotes;
import com.garethevans.church.opensongtablet.databinding.ActivityMainBinding;
import com.garethevans.church.opensongtablet.export.ExportActions;
import com.garethevans.church.opensongtablet.export.MakePDF;
import com.garethevans.church.opensongtablet.export.PrepareFormats;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.filemanagement.ExportFiles;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.SaveSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.filemanagement.StorageManagementFragment;
import com.garethevans.church.opensongtablet.highlighter.HighlighterEditFragment;
import com.garethevans.church.opensongtablet.importsongs.ImportOnlineFragment;
import com.garethevans.church.opensongtablet.importsongs.OCR;
import com.garethevans.church.opensongtablet.importsongs.WebDownload;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.DialogReturnInterface;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.MidiAdapterInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyReturnActionsInterface;
import com.garethevans.church.opensongtablet.interfaces.ShowCaseInterface;
import com.garethevans.church.opensongtablet.interfaces.SwipeDrawingInterface;
import com.garethevans.church.opensongtablet.metronome.Metronome;
import com.garethevans.church.opensongtablet.midi.Midi;
import com.garethevans.church.opensongtablet.midi.MidiFragment;
import com.garethevans.church.opensongtablet.nearby.NearbyConnections;
import com.garethevans.church.opensongtablet.nearby.NearbyConnectionsFragment;
import com.garethevans.church.opensongtablet.pads.PadFunctions;
import com.garethevans.church.opensongtablet.performance.PerformanceFragment;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.ProfileActions;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.presentation.PresentationFragment;
import com.garethevans.church.opensongtablet.screensetup.ActivityGestureDetector;
import com.garethevans.church.opensongtablet.screensetup.AppActionBar;
import com.garethevans.church.opensongtablet.screensetup.BatteryStatus;
import com.garethevans.church.opensongtablet.screensetup.DoVibrate;
import com.garethevans.church.opensongtablet.screensetup.FontSetupFragment;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.screensetup.WindowFlags;
import com.garethevans.church.opensongtablet.secondarydisplay.ExternalDisplay;
import com.garethevans.church.opensongtablet.secondarydisplay.MediaRouterCallback;
import com.garethevans.church.opensongtablet.secondarydisplay.MySessionManagerListener;
import com.garethevans.church.opensongtablet.secondarydisplay.PresentationCommon;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.setprocessing.SetManageFragment;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ConvertTextSong;
import com.garethevans.church.opensongtablet.songprocessing.EditSongFragment;
import com.garethevans.church.opensongtablet.songprocessing.EditSongFragmentMain;
import com.garethevans.church.opensongtablet.songprocessing.PDFSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongSheetHeaders;
import com.garethevans.church.opensongtablet.songsandsetsmenu.SetMenuFragment;
import com.garethevans.church.opensongtablet.songsandsetsmenu.SongListBuildIndex;
import com.garethevans.church.opensongtablet.songsandsetsmenu.SongMenuFragment;
import com.garethevans.church.opensongtablet.songsandsetsmenu.ViewPagerAdapter;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

/*
This file is the Activity - the hub of the app.  Since V6, there is only one activity, which is
better for memory, lifecycle and passing values between fragments (how the app now works).

MainActivity is the main gatekeeper of class references and variables.
All fragments get access to these using getters and setters via the MainActivityInterface
This avoids loads of static variables

Fragments sometimes send info back to the MainActivity and ask this activity to send the details back
When this happens, the fragment has to be open.  Main activity keeps references and checks before doing!!
*/

//TODO Fix unused and local

public class MainActivity extends AppCompatActivity implements //LoadSongInterface,
        ShowCaseInterface, MainActivityInterface, MidiAdapterInterface, EditSongFragmentInterface,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, DialogReturnInterface,
        NearbyInterface, NearbyReturnActionsInterface, ActionInterface, SwipeDrawingInterface {

    private final String TAG = "MainActivity";
    private ActivityMainBinding activityMainBinding;

    private AppBarConfiguration mAppBarConfiguration;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private ThemeColors themeColors;
    private DrawNotes drawNotes;
    private SetTypeFace setTypeFace;
    private SQLiteHelper sqLiteHelper;
    private CommonSQL commonSQL;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private ConvertChoPro convertChoPro;
    private ConvertOnSong convertOnSong;
    private ConvertTextSong convertTextSong;
    private ProcessSong processSong;
    private LoadSong loadSong;
    private Song song, indexingSong, tempSong;
    private CurrentSet currentSet;
    private SetActions setActions;
    private SongListBuildIndex songListBuildIndex;
    private ShowCase showCase;
    private ShowToast showToast;
    private WindowFlags windowFlags;
    private BatteryStatus batteryStatus;
    private AppActionBar appActionBar;
    private VersionNumber versionNumber;
    private FixLocale fixLocale;
    private CCLILog ccliLog;
    private Midi midi;
    private ExportFiles exportFiles;
    private boolean pageButtonActive = true;
    private PageButtons pageButtons;
    private PedalActions pedalActions;
    private Swipes swipes;
    private Gestures gestures;
    private ExportActions exportActions;
    private WebDownload webDownload;
    private AutoscrollActions autoscrollActions;
    private DoVibrate doVibrate;
    private SaveSong saveSong;
    private PadFunctions padFunctions;
    private Metronome metronome;
    private CustomAnimation customAnimation;
    private PDFSong pdfSong;
    private PrepareFormats prepareFormats;
    private MakePDF makePDF;
    private OCR ocr;
    private Transpose transpose;
    private GestureDetector gestureDetector;
    private ActivityGestureDetector activityGestureDetector;
    private ABCNotation abcNotation;
    private ProfileActions profileActions;
    private CheckInternet checkInternet;
    private SongSheetHeaders songSheetHeaders;

    private ArrayList<View> targets;
    private ArrayList<String> infos, dismisses;
    private ArrayList<Boolean> rects;

    public MediaPlayer mediaPlayer1, mediaPlayer2;

    private MainActivityInterface mainActivityInterface;

    // Important fragments we get references to (for calling methods)
    PerformanceFragment performanceFragment;
    PresentationFragment presentationFragment;
    SongMenuFragment songMenuFragment;
    SetMenuFragment setMenuFragment;
    EditSongFragment editSongFragment;
    EditSongFragmentMain editSongFragmentMain;
    NearbyConnectionsFragment nearbyConnectionsFragment;
    SwipeFragment swipeFragment;
    Fragment registeredFragment;
    int fragmentOpen;
    boolean fullIndexRequired = true;
    String whattodo = "";
    private Bitmap screenShot;

    NavHostFragment navHostFragment;
    NavController navController;

    // Actionbar
    ActionBar ab;
    AlertChecks alertChecks;

    MenuItem settingsButton;
    private boolean settingsOpen;

    // Network discovery / connections
    NearbyConnections nearbyConnections;
    private boolean nearbyOpen;

    // Casting
    private CastContext castContext;
    private MySessionManagerListener sessionManagerListener;
    private CastSession castSession;
    private CastStateListener castStateListener;
    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private MediaRouterCallback mediaRouterCallback;
    private CastDevice castDevice;
    private SessionManager sessionManager;
    private Display display;
    private ExternalDisplay externalDisplay;
    private PresentationCommon presentationCommon;
    //private PresentationServiceHDMI hdmi;

    ViewPagerAdapter adapter;
    ViewPager2 viewPager;
    boolean showSetMenu;

    Locale locale;

    // The song views.
    // Stored here so they can be accessed via the different modes and classes
    private ArrayList<View> sectionViews;
    private LinearLayout songSheetTitleLayout;
    private ArrayList<Integer> sectionWidths, sectionHeights, songSheetTitleLayoutSize;


    // Importing/exporting
    private String importFilename;
    private Uri importUri;

    private String whichMode;

    // Pads


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise the views for the activity
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);

        Log.d(TAG, "STARTING MAIN ACTIVITY: ");
        // Initialise the most important stuff
        initialiseHelpers1();

        // Initialise the remaining helpers needed before doing anything else
        initialiseHelpers2();

        // Hide the page button to begin with
        activityMainBinding.pageButtonRight.actionFAB.setVisibility(View.GONE);

        // Prepare the actionbar
        setupActionbar();

        // Now initialise the remaining helpers
        initialiseHelpers3();

        // Set the fullscreen window flags
        setWindowFlags();

        // Fragment work
        setupNav();

        // Only do the following if we haven't got a saved state
        if (savedInstanceState==null) {



            /*view.setOnSystemUiVisibilityChangeListener(
                visibility -> {
                    Log.d(TAG,"UiVisibility changed");
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        appActionBar.showActionBar(settingsOpen);
                        setWindowFlags();
                    }
                });*/


        }

        // Initialise the start variables we need
        initialiseStartVariables();

        // Set up the page buttons
        updatePageButtonLayout();

        // Battery monitor
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        this.registerReceiver(batteryStatus, filter);

        // Set up the Nearby connection service
        nearbyConnections.getUserNickname(this, mainActivityInterface);

        // Initialise the CastContext
        setUpCast();
    }

    // Set up the helpers
    private void initialiseHelpers1() {
        // The first two are used in other helpers often, so they get done first!
        storageAccess = new StorageAccess();
        preferences = new Preferences();

        mainActivityInterface = this;
        songListBuildIndex = new SongListBuildIndex();
    }
    private void initialiseHelpers2() {

        // The app setup
        versionNumber = new VersionNumber();
        fixLocale = new FixLocale();
        checkInternet = new CheckInternet();
        nearbyConnections = new NearbyConnections(this,mainActivityInterface);
        mediaRouterCallback = new MediaRouterCallback();
        doVibrate = new DoVibrate();
        customAnimation = new CustomAnimation();
        presentationCommon = new PresentationCommon();
        webDownload = new WebDownload();
        alertChecks = new AlertChecks();

        // For user preferences
        setTypeFace = new SetTypeFace();
        themeColors = new ThemeColors();
        profileActions = new ProfileActions();

        // The databases
        sqLiteHelper = new SQLiteHelper(this);
        nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(this);
        commonSQL = new CommonSQL();

        // Converting song formats and processing song content
        convertChoPro = new ConvertChoPro();
        convertOnSong = new ConvertOnSong();
        convertTextSong = new ConvertTextSong();
        processSong = new ProcessSong();
        prepareFormats = new PrepareFormats();
        pdfSong = new PDFSong();
        makePDF = new MakePDF();
        songSheetHeaders = new SongSheetHeaders();
        ocr = new OCR();
        transpose = new Transpose();
        abcNotation = new ABCNotation();
        song = new Song();

        // Loading up songs and the indexing
        loadSong = new LoadSong();
        saveSong = new SaveSong();

        // Sets
        currentSet = new CurrentSet();
        setActions = new SetActions();

        // Displaying info to the user
        showCase = new ShowCase();
        showToast = new ShowToast();

        // Song actions/features
        pageButtons = new PageButtons(this,preferences);
        midi = new Midi();
        pedalActions = new PedalActions();
        padFunctions = new PadFunctions();
        autoscrollActions = new AutoscrollActions();
        metronome = new Metronome();
        gestures = new Gestures(this,mainActivityInterface);
        gestureDetector = new GestureDetector(this,new ActivityGestureDetector());
        swipes = new Swipes(this,mainActivityInterface);

        // Other file actions
        ccliLog = new CCLILog();
        exportFiles = new ExportFiles();
        exportActions = new ExportActions();
    }
    private void initialiseHelpers3() {
        windowFlags = new WindowFlags(this.getWindow());
        appActionBar = new AppActionBar(ab,activityMainBinding.toolBar.actionBarBackground,
                batteryStatus,activityMainBinding.toolBar.songtitleAb,
                activityMainBinding.toolBar.songauthorAb, activityMainBinding.toolBar.songkeyAb,
                activityMainBinding.toolBar.songcapoAb,activityMainBinding.toolBar.batteryimage,
                activityMainBinding.toolBar.batterycharge,activityMainBinding.toolBar.digitalclock,
                preferences.getMyPreferenceBoolean(this,"hideActionBar",false));
        pageButtons.setMainFABS(activityMainBinding.pageButtonRight.actionFAB, activityMainBinding.pageButtonRight.custom1Button,
                activityMainBinding.pageButtonRight.custom2Button,activityMainBinding.pageButtonRight.custom3Button,
                activityMainBinding.pageButtonRight.custom4Button,activityMainBinding.pageButtonRight.custom5Button,
                activityMainBinding.pageButtonRight.custom6Button,activityMainBinding.pageButtonRight.pageButtonsLayout,
                themeColors.getPageButtonsColor());
        pageButtons.animatePageButton(this,false);
    }

    // The actionbar
    private void setupActionbar() {
        setSupportActionBar(activityMainBinding.toolBar.getRoot());
        ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public void changeActionBarVisible(boolean wasScrolling, boolean scrollButton) {
        if (!whichMode.equals("Presentation") && preferences.getMyPreferenceBoolean(this, "hideActionBar", false)) {
            // If we are are in performance or stage mode and want to hide the actionbar, then move the views up to the top
            activityMainBinding.fragmentView.setTop(0);
        } else {
            // Otherwise move the content below it
            activityMainBinding.fragmentView.setTop(ab.getHeight());
        }
        appActionBar.toggleActionBar(wasScrolling,scrollButton,activityMainBinding.drawerLayout.isOpen());
    }

    @Override
    public void updatePageButtonLayout() {
        // We have changed something about the page buttons (or initialising them
        if (activityMainBinding.pageButtonRight.actionFAB.getRotation()!=0) {
            pageButtons.animatePageButton(this,false);
        }
        activityMainBinding.pageButtonRight.actionFAB.setBackgroundTintList(ColorStateList.valueOf(themeColors.getPageButtonsColor()));
        activityMainBinding.pageButtonRight.actionFAB.setAlpha(pageButtons.getPageButtonAlpha());
        for (int x=0; x<6; x++) {
            pageButtons.setPageButton(this,pageButtons.getFAB(x), themeColors.getPageButtonsColor(), x, false);
        }
    }

    // The navigation actions
    public void setupNav() {
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment!=null) {
            navController = navHostFragment.getNavController();
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.bootUpFragment,
                R.id.performanceFragment, R.id.presentationFragment)
                .setOpenableLayout(activityMainBinding.drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    }

    // Initialise the MainActivity components
    // Some of the set up only happens once the user has passed the BootUpFragment checks
    @Override
    public void initialiseActivity() {
        // Set up song / set menu tabs
        setUpSongMenuTabs();

        // Set the version in the menu
        versionNumber.updateMenuVersionNumber(this, activityMainBinding.menuTop.versionCode);

        // Set up page buttons
        setListeners();
    }

    private void setUpCast() {
        try {
            castContext = CastContext.getSharedInstance(this);
            sessionManager = castContext.getSessionManager();
            castSession = castContext.getSessionManager().getCurrentCastSession();
            sessionManagerListener = new MySessionManagerListener(this);

        } catch (Exception e) {
            // No Google Service available
            // Do nothing as the user will see a warning in the settings menu
            Log.d(TAG,"No Google Services");
        }
    }

    @Override
    public void setDisplay(Display display) {
        this.display = display;
    }
    @Override
    public Display getDisplay() {
        return display;
    }
    @Override
    public ExternalDisplay getExternalDisplay() {
        if (externalDisplay!=null && display!=null) {
            externalDisplay = new ExternalDisplay(this,display);
        }
        return externalDisplay;
    }
    private void recoverCastState() {
        castSession = sessionManager.getCurrentCastSession();
        sessionManager.addSessionManagerListener(new MySessionManagerListener(this));
    }
    private void endCastState() {
        sessionManager.removeSessionManagerListener(sessionManagerListener);
        castSession = null;
    }
    @Override
    public PresentationCommon getPresentationCommon() {
        return presentationCommon;
    }
    private void initialiseStartVariables() {
        themeColors.setThemeName(preferences.getMyPreferenceString(this, "appTheme", "dark"));
        whichMode = preferences.getMyPreferenceString(this, "whichMode", "Performance");

        // Song location
        song.setFilename(preferences.getMyPreferenceString(this,"songfilename","Welcome to OpenSongApp"));
        song.setFolder(preferences.getMyPreferenceString(this, "whichSongFolder", getString(R.string.mainfoldername)));

        // Set
        // TODO remove the temp made up set
        preferences.setMyPreferenceString(this, "setCurrent",
                "$**_MAIN/Abba Father_***G***__**$$**_MAIN/All I need_**$$**_Band/500 miles_***E***__**$");
        setActions.preferenceStringToArrays(this,mainActivityInterface);

        // Set the locale
        fixLocale.setLocale(this,mainActivityInterface);
        locale = fixLocale.getLocale();

        // MediaPlayer
        mediaPlayer1 = new MediaPlayer();
        mediaPlayer2 = new MediaPlayer();

        // ThemeColors
        themeColors.getDefaultColors(this,mainActivityInterface);
        pageButtons.setPageButtonAlpha(preferences.getMyPreferenceFloat(this,"pageButtonAlpha",0.6f));

        // Typefaces
        setTypeFace.setUpAppFonts(this,mainActivityInterface,new Handler(),new Handler(),new Handler(),new Handler(),new Handler());
    }
    private void initialiseArrayLists() {
        targets = new ArrayList<>();
        dismisses = new ArrayList<>();
        infos = new ArrayList<>();
        rects = new ArrayList<>();
    }

    private void setListeners() {
        activityMainBinding.pageButtonRight.actionFAB.setOnClickListener(v  -> {
            if (pageButtonActive) {
                pageButtonActive = false;
                Handler h = new Handler();
                h.postDelayed(() -> pageButtonActive = true,600);
                animatePageButtons();
            }
        });

        activityMainBinding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            float initialVal = -1.0f;
            boolean decided;

            private void resetVals() {
                decided = false;
                initialVal = -1;
            }

            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (!decided && initialVal==-1.0f) {
                    // Just started, so set the inital value
                    initialVal = slideOffset;
                } else if (!decided && initialVal!=-0.0f) {
                    // We have our first value, so now compare.
                    // If we are getting bigger = opening, if smaller, closing
                    hideActionButton(slideOffset > initialVal);
                    decided = true;
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                hideActionButton(true);
                setWindowFlags();
                showTutorial("songsetmenu");
                if (setSongMenuFragment()) {
                    showTutorial("songsetMenu");
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (activityMainBinding.drawerLayout.getDrawerLockMode(GravityCompat.START) ==
                        DrawerLayout.LOCK_MODE_UNLOCKED) {
                    hideActionButton(false);
                } else {
                    hideActionButton(true);
                }
                hideKeyboard();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Reset the check vals
                resetVals();
            }
        });
    }
    private void animatePageButtons() {
        float rotation = activityMainBinding.pageButtonRight.actionFAB.getRotation();
        pageButtons.animatePageButton(this,rotation == 0);
    }

    void setWindowFlags() {
        // Fix the page flags
        if (windowFlags==null) {
            windowFlags = new WindowFlags(this.getWindow());
        }
        windowFlags.setWindowFlags();
    }

    // Deal with the song menu
    @Override
    public void quickSongMenuBuild() {
        fullIndexRequired = true;
        ArrayList<String> songIds = new ArrayList<>();
        try {
            songIds = storageAccess.listSongs(this, mainActivityInterface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Write a crude text file (line separated) with the song Ids (folder/file)
        storageAccess.writeSongIDFile(this, mainActivityInterface, songIds);

        // Try to create the basic databases
        // Non persistent, created from storage at boot (to keep updated) used to references ALL files
        sqLiteHelper.resetDatabase(this);
        // Persistent containing details of PDF/Image files only.  Pull in to main database at boot
        // Updated each time a file is created, deleted, moved.
        // Also updated when feature data (pad, autoscroll, metronome, etc.) is updated for these files
        nonOpenSongSQLiteHelper.initialise(this, mainActivityInterface);

        // Add entries to the database that have songid, folder and filename fields
        // This is the minimum that we need for the song menu.
        // It can be upgraded asynchronously in StageMode/PresenterMode to include author/key
        // Also will later include all the stuff for the search index as well
        sqLiteHelper.insertFast(this, mainActivityInterface);
    }

    @Override
    public void fullIndex() {
        Log.d(TAG,"fullIndex() called");
        if (fullIndexRequired) {
            showToast.doIt(this,getString(R.string.search_index_start));
            new Thread(() -> {
                String outcome = songListBuildIndex.fullIndex(MainActivity.this,mainActivityInterface);
                Log.d(TAG,"index done");
                if (songMenuFragment!=null) {
                    try {
                        songMenuFragment.updateSongMenu(song);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(() -> {
                    if (!outcome.isEmpty()) {
                        showToast.doIt(this,outcome.trim());
                    }
                    updateFragment("set_updateKeys",null,null);
                });

            }).start();
        }
    }
    @Override
    public void setFullIndexRequired(boolean fullIndexRequired) {
        this.fullIndexRequired = fullIndexRequired;
    }

    private boolean setSongMenuFragment() {
        runOnUiThread(() -> {
            if (songMenuFragment!=null) {
                //songMenuFragment.showActionButton(true);
                if (showSetMenu) {
                    viewPager.setCurrentItem(1);
                } else {
                    viewPager.setCurrentItem(0);
                }
            }
        });
        return songMenuFragment != null;
    }
    @Override
    public void chooseMenu(boolean showSetMenu) {
        this.showSetMenu = showSetMenu;
        setSongMenuFragment();
        closeDrawer(activityMainBinding.drawerLayout.isOpen());
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view!=null && imm!=null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // MainActivity standard Overrides
    @Override
    public void onStart() {
        super.onStart();
        // Deal with the Cast logic
        if (mediaRouter != null && mediaRouteSelector != null) {
            try {
                mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,
                        MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Fix the page flags
        setWindowFlags();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Set the fullscreen window flags]
        if (hasFocus) {
            setWindowFlags();
            //appActionBar.showActionBar(settingsOpen);
        }
    }
    /*@Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        gestureDetector.onTouchEvent(ev); // Dealt with in ActivityGestureDetector
        return false;

    }*/



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            mediaRouter.removeCallback(mediaRouterCallback);
        } catch (Exception e) {
            Log.d("StageMode", "Problem removing mediaroutercallback");
        }
        if (batteryStatus!=null) {
            try {
                this.unregisterReceiver(batteryStatus);
            } catch (Exception e) {
                Log.d(TAG, "Battery receiver not registered, so no need to unregister");
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d(TAG,"Configuration changed");
        // Get the language
        fixLocale.setLocale(this,mainActivityInterface);

        // Save a static variable that we have rotated the screen.
        // The media player will look for this.  If found, it won't restart when the song loads
        padFunctions.setOrientationChanged(padFunctions.getCurrentOrientation()!=newConfig.orientation);
        // If orientation has changed, we need to reload the song to get it resized.
        if (padFunctions.getOrientationChanged()) {
            // Set the current orientation
            padFunctions.setCurrentOrientation(newConfig.orientation);
            closeDrawer(true);
            doSongLoad(song.getFolder(),song.getFilename());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        endCastState();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //setUpCast();

        // Fix the page flags
        setWindowFlags();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nearbyConnections.turnOffNearby(this);
    }

    // The toolbar/menu items
    @Override
    public void updateToolbar(String title) {
        // Null titles are for the default song, author, etc.
        // Otherwise a new title is passed as a string
        windowFlags.setWindowFlags();
        appActionBar.setActionBar(this,mainActivityInterface, title);

        if (title!=null || !preferences.getMyPreferenceBoolean(this,"hideActionBar",false)) {
            // Make sure the content shows below the action bar
            activityMainBinding.fragmentView.setTop(ab.getHeight());
        }
    }
    @Override
    public void updateActionBarSettings(String prefName, int intval, float floatval, boolean isvisible) {
        // If the user changes settings from the ActionBarSettingsFragment, they get sent here to deal with
        // So let's pass them on to the AppActionBar helper
        appActionBar.updateActionBarSettings(this,mainActivityInterface,prefName,intval,floatval,isvisible);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        settingsButton = menu.findItem(R.id.settings_menu_item);
        MenuItem alertButton = menu.findItem(R.id.alert_info_item);

        // Decide if an alert should be shown
        if (alertChecks.showBackup(this,
                preferences.getMyPreferenceInt(this,"runssincebackup",0)) ||
                alertChecks.showPlayServicesAlert(this) ||
                alertChecks.showUpdateInfo(this,versionNumber.getVersionCode(),
                        preferences.getMyPreferenceInt(this,"lastUsedVersion",0))) {
            alertButton.setVisible(true);
        } else if (alertButton!=null){
            alertButton.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG,item.toString());
        switch (item.toString()) {
            case "Settings":
                if (settingsOpen) {
                    navHome();
                } else {
                    navigateToFragment("opensongapp://preferences",0);
                }
                break;

            case "Information":
                BottomSheetDialogFragment df = new AlertInfoBottomSheet();
                openDialog(df,"Alerts");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void refreshMenuItems() {
        invalidateOptionsMenu();
    }
    @Override
    public void navHome() {
        lockDrawer(false);
        whichMode = preferences.getMyPreferenceString(this,"whichMode","Performance");
        if (navController.getCurrentDestination()!=null) {
            navController.popBackStack(navController.getCurrentDestination().getId(), true);
        }
        if (whichMode.equals("Presentation")) {
            navigateToFragment("opensongapp://presentation",0);
        } else {
            navigateToFragment("opensongapp://performance",0);
        }
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return castContext.onDispatchVolumeKeyEventBeforeJellyBean(event)
                || super.dispatchKeyEvent(event);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainactivitymenu, menu);

        // Setup the menu item for connecting to cast devices

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            MenuItem mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        } else {
            Log.d(TAG, "Google Play Services Not Available");
            // TODO
            // Alert the user about the Google Play issues and give them an option to fix it
            // Add it to the menu alerts
        }

        // Set up battery monitor
        batteryStatus = new BatteryStatus();
        batteryStatus.setUpBatteryMonitor(this,mainActivityInterface,activityMainBinding.toolBar.digitalclock,
                activityMainBinding.toolBar.batterycharge,
                activityMainBinding.toolBar.batteryimage,ab);

        return true;
    }
    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    // Using the MaterialShowCase library
    @Override
    public void showTutorial(String what) {
        if (settingsButton==null) {
            invalidateOptionsMenu();
        }
        switch (what) {
            case "performanceView":
                showCase.singleShowCase(this,activityMainBinding.pageButtonRight.actionFAB,
                        null,getString(R.string.action_button_info),false,"pageActionFAB");
                break;
            case "songsetmenu":
                // Initialise the arraylists
                initialiseArrayLists();
                if (activityMainBinding != null) {
                    targets.add(Objects.requireNonNull(activityMainBinding.menuTop.tabs.getTabAt(0)).view);
                    targets.add(Objects.requireNonNull(activityMainBinding.menuTop.tabs.getTabAt(1)).view);
                    targets.add(Objects.requireNonNull(activityMainBinding.viewpager.findViewById(R.id.actionFAB)));
                }
                infos.add(getString(R.string.menu_song_info));
                infos.add(getString(R.string.menu_set_info));
                infos.add (getString(R.string.add_songs)+" / "+getString(R.string.song_actions));
                dismisses.add(null);
                dismisses.add(null);
                dismisses.add(null);
                rects.add(true);
                rects.add(true);
                rects.add(true);
                showCase.sequenceShowCase(this,targets,dismisses,infos,rects,"songSetMenu");
        }
    }
    @Override
    public void runShowCase() {

    }

    // Deal with stuff in the song menu
    @Override
    public void moveToSongInSongMenu() {
        if (setSongMenuFragment()) {
            if (songMenuFragment!=null) {
                try {
                    songMenuFragment.moveToSongInMenu(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.d(TAG, "songMenuFragment not available");
        }
    }
    @Override
    public void indexSongs() {
        Log.d(TAG,"indexSong() called");
        new Thread(() -> {
            runOnUiThread(() -> showToast.doIt(this,getString(R.string.search_index_start)));
            songListBuildIndex.setIndexComplete(false);
            songListBuildIndex.fullIndex(MainActivity.this,mainActivityInterface);
            runOnUiThread(() -> {
                songListBuildIndex.setIndexRequired(false);
                songListBuildIndex.setIndexComplete(true);
                showToast.doIt(this,getString(R.string.search_index_end));
                updateSongMenu(song);
                updateFragment("set_updateKeys",null,null);
            });
        }).start();
    }
    @Override
    public void updateSongMenu(Song song) {
        // This only asks for an update from the database
        songListBuildIndex.setIndexComplete(true);
        songListBuildIndex.setIndexRequired(false);
        if (setSongMenuFragment() && songMenuFragment!=null) {
            songMenuFragment.updateSongMenu(song);
        } else {
            Log.d(TAG, "songMenuFragment not available");
        }
    }

    private void setUpSongMenuTabs() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager(),MainActivity.this.getLifecycle());
        adapter.createFragment(0);
        songMenuFragment = (SongMenuFragment)adapter.menuFragments[0];
        setMenuFragment = (SetMenuFragment) adapter.createFragment(1);
        viewPager = activityMainBinding.viewpager;
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        // Disable the swiping gesture
        viewPager.setUserInputEnabled(false);
        TabLayout tabLayout = activityMainBinding.menuTop.tabs;
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.song));
                    tab.setIcon(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_music_note_white_36dp,null));
                    //tab.setIcon(getResources().getDrawable(R.drawable.ic_music_note_white_36dp));
                    break;
                case 1:
                    tab.setText(getString(R.string.set));
                    tab.setIcon(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_format_list_numbers_white_36dp,null));
                    //tab.setIcon(getResources().getDrawable(R.drawable.ic_format_list_numbers_white_36dp));
                    break;
            }
        }).attach();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                showSetMenu = position != 0;
                super.onPageSelected(position);
            }
        });
        activityMainBinding.menuTop.versionCode.setOnClickListener(v -> closeDrawer(true));
    }
    @Override
    public void closeDrawer(boolean close) {
        if (close) {
            activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            activityMainBinding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }
    @Override
    public DrawerLayout getDrawer() {
        return activityMainBinding.drawerLayout;
    }
    @Override
    public ActionBar getAb() {
        return getSupportActionBar();
    }

    // Overrides called from fragments (Performance and Presentation) using interfaces
    @Override
    public void loadSongFromSet(int position) {
        // Get the key of the set item
        String setKey = currentSet.getKey(position);
        String setFolder = currentSet.getFolder(position);
        String setFilename = currentSet.getFilename(position);
        String songKey;
        // Get the song key (from the database)
        if (storageAccess.isSpecificFileExtension("imageorpdf",currentSet.getFilename(position))) {
            songKey = nonOpenSongSQLiteHelper.getKey(this,mainActivityInterface,setFolder,setFilename);
        } else {
            songKey = sqLiteHelper.getKey(this,mainActivityInterface,setFolder,setFilename);
        }
        Log.d(TAG,"setKey="+setKey+"  songKey="+songKey);
        Log.d(TAG,"loadSongFromSet() called");
        doSongLoad(setFolder,setFilename);
    }
    @Override
    public void refreshAll() {
        Log.d(TAG,"refreshAll() called");
    }
    @Override
    public void doSongLoad(String folder, String filename) {
        if (whichMode.equals("Presentation")) {
            if (presentationFragment!=null && presentationFragment.isAdded()) {
                presentationFragment.doSongLoad(folder,filename);
            } else {
                navigateToFragment(null,R.id.performanceFragment);
            }
        } else {
            if (performanceFragment!=null && performanceFragment.isAdded()) {
                performanceFragment.doSongLoad(folder,filename);
            } else {
                navigateToFragment(null,R.id.presentationFragment);
            }
        }
        closeDrawer(true);
    }


    // For all fragments that need to listen for method calls from MainActivity;
    @Override
    public void registerFragment(Fragment frag, String what) {
        switch (what) {
            case "Performance":
                performanceFragment = (PerformanceFragment) frag;
                break;
            case "Presentation":
                presentationFragment = (PresentationFragment) frag;
                break;
            case "EditSongFragment":
                editSongFragment = (EditSongFragment) frag;
                break;
            case "EditSongFragmentMain":
                editSongFragmentMain = (EditSongFragmentMain) frag;
                break;
            case "NearbyConnectionsFragment":
                nearbyConnectionsFragment = (NearbyConnectionsFragment) frag;
                break;
            case "SwipeFragment":
                swipeFragment = (SwipeFragment) frag;
                break;
        }
        registeredFragment = frag;
    }
    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        //fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        navigateToFragment(null,caller.getId());
        //navController.navigate(caller.getId());
        /*fragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();*/
        return true;
    }



    // To run in the song/set menu
    @Override
    public void songMenuActionButtonShow(boolean show) {
        if (songMenuFragment!=null) {
            //songMenuFragment.showActionButton(show);
        }
    }
    @Override
    public void refreshSetList() {
        if (setMenuFragment!=null) {
            setMenuFragment.prepareCurrentSet();
        }
    }


    // To run in the edit song fragment
    @Override
    public void updateKeyAndLyrics(Song song) {
        // This is called from the transpose class once it has done its work on the edit song fragment
        editSongFragmentMain.updateKeyAndLyrics(song);
    }
    @Override
    public void updateSong(Song song) {
        editSongFragment.updateSong(song);
    }
    @Override
    public Song getSong() {
        return song;
    }
    @Override
    public Song getIndexingSong() {
        return indexingSong;
    }
    @Override
    public Song getTempSong() {
        return tempSong;
    }
    @Override
    public void setSong(Song song) {
        this.song = song;
    }
    @Override
    public void setIndexingSong(Song indexingSong) {
        this.indexingSong = indexingSong;
    }
    @Override
    public void setTempSong(Song tempSong) {
        this.tempSong = tempSong;
    }
    @Override
    public void setOriginalSong(Song originalSong) {
        editSongFragment.setOriginalSong(originalSong);
    }
    @Override
    public Song getOriginalSong() {
        return editSongFragment.getOriginalSong();
    }
    @Override
    public boolean songChanged() {
        return editSongFragment.songChanged();
    }
    @Override
    public void editSongSaveButtonAnimation(boolean pulse) {
        if (editSongFragment!=null) {
            editSongFragment.pulseSaveChanges(pulse);
        }
    }


    // Run actions from MainActivity (called from fragments)
    @Override
    public void lockDrawer(boolean lock) {
        // This is done whenever we have a settings window open
        if (activityMainBinding != null) {
            if (lock) {
                settingsOpen = true;
                activityMainBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                settingsOpen = false;
                activityMainBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        }
    }
    @Override
    public void hideActionButton(boolean hide) {
        if (activityMainBinding != null) {
            if (hide) {
                if (activityMainBinding.pageButtonRight.actionFAB.getRotation() != 0) {
                    activityMainBinding.pageButtonRight.actionFAB.performClick();
                }
                activityMainBinding.pageButtonRight.actionFAB.hide();
                activityMainBinding.pageButtonRight.bottomButtons.setVisibility(View.GONE);
            } else {
                activityMainBinding.pageButtonRight.actionFAB.show();
                activityMainBinding.pageButtonRight.bottomButtons.setVisibility(View.VISIBLE);
                // Do this with a delay
                customAnimation.fadeActionButton(activityMainBinding.pageButtonRight.actionFAB, pageButtons.getPageButtonAlpha());
            }
        }
    }
    @Override
    public void hideActionBar(boolean hide) {
        if (hide) {
            if (getSupportActionBar()!=null) {
                getSupportActionBar().hide();
            }
        } else {
            if (getSupportActionBar()!=null) {
                getSupportActionBar().show();
            }
        }
    }
    @Override
    public void navigateToFragment(String deepLink, int id) {
        // Either sent a deeplink string, or a fragment id
        lockDrawer(true);
        closeDrawer(true);  // Only the Performance and Presentation fragments allow this.  Switched on in these fragments
        hideActionButton(true);
        try {
            if (deepLink!=null) {
                navController.navigate(Uri.parse(deepLink));
            } else {
                navController.navigate(id);
                fragmentOpen = id;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void popTheBackStack(int id, boolean inclusive) {
        navController.popBackStack(id,inclusive);
    }
    @Override
    public void openDialog(BottomSheetDialogFragment frag, String tag) {
        try {
            frag.show(getSupportFragmentManager(), tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void updateFragment(String fragName, Fragment callingFragment, ArrayList<String> arguments) {
        if (fragName!=null) {
            // The fragName can also be text that hints at a fragment
            switch (fragName) {
                case "StorageManagementFragment":
                    ((StorageManagementFragment)callingFragment).updateFragment();
                    break;

                case "createNewSong":
                    // User was in song menu dialog, clicked on create, then entered a new file name
                    // Check this was successful (saved as arguments)
                    if (arguments!=null && arguments.size()>0 && arguments.get(0).equals("success")) {
                        // Write a blank xml file with the song name in it
                        // TODO
                        song = processSong.initialiseSong(mainActivityInterface,song.getFolder(),"NEWSONGFILENAME");
                        String newSongText = processSong.getXML(this,mainActivityInterface,song);
                        if (storageAccess.doStringWriteToFile(this,mainActivityInterface,"Songs",song.getFolder(), song.getFilename(),newSongText)) {
                            navigateToFragment(null,R.id.editSongFragment);
                        } else {
                            ShowToast.showToast(this,getString(R.string.error));
                        }
                    }
                    break;

                case "duplicateSong":
                    // User was in song menu dialog, clicked on create, then entered a new file name
                    // Check this was successful (saved as arguments)
                    if (arguments!=null && arguments.size()>1 && arguments.get(0).equals("success")) {
                        // We now need to copy the original file.  It's contents are saved in arguments.get(1)
                        if (storageAccess.doStringWriteToFile(this,mainActivityInterface,"Songs",song.getFolder(),song.getFilename(),arguments.get(1))) {
                            doSongLoad(song.getFolder(),song.getFilename());
                        } else {
                            ShowToast.showToast(this,getString(R.string.error));
                        }
                    }
                    break;

                case "set_updateKeys":
                case "set_updateView":
                case "set_updateItem":
                    // User has the set menu open and wants to shuffle the set
                    if (setMenuFragment!=null) {
                        if (fragName.equals("set_updateView")) {
                            setMenuFragment.updateSet();
                        } else if (fragName.equals("set_updateKeys")){
                            setMenuFragment.updateKeys();
                        } else if (arguments!=null && arguments.size()>0){
                            setMenuFragment.updateItem(Integer.parseInt(arguments.get(0)));
                        }
                    }
                    break;
            }
        }
    }
    @Override
    public void displayAreYouSure(String what, String action, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song) {
        AreYouSureBottomSheet dialogFragment = new AreYouSureBottomSheet(what,action,arguments,fragName,callingFragment,song);
        dialogFragment.show(this.getSupportFragmentManager(), "areYouSure");
    }
    @Override
    public void confirmedAction(boolean agree, String what, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song) {
        if (agree) {
            boolean result = false;
            boolean allowToast = true;
            switch(what) {
                case "deleteSong":
                    result = storageAccess.doDeleteFile(this,mainActivityInterface,"Songs",
                            song.getFolder(), song.getFilename());
                    // Now remove from the SQL database
                    if (song.getFiletype().equals("PDF") || song.getFiletype().equals("IMG")) {
                        nonOpenSongSQLiteHelper.deleteSong(this,mainActivityInterface,song.getFolder(),song.getFilename());
                    } else {
                        sqLiteHelper.deleteSong(this, mainActivityInterface, song.getFolder(),song.getFilename());
                    }
                    // TODO
                    // Send a call to reindex?
                    break;

                case "ccliDelete":
                    Uri uri = storageAccess.getUriForItem(this,mainActivityInterface,"Settings","","ActivityLog.xml");
                    result = ccliLog.createBlankXML(this,mainActivityInterface,uri);
                    break;

                case "deleteItem":
                    // Folder and subfolder are passed in the arguments.  Blank arguments.get(2) /filenames mean folders
                    result = storageAccess.doDeleteFile(this,mainActivityInterface,arguments.get(0),arguments.get(1),arguments.get(2));
                    if (arguments.get(2).isEmpty() && arguments.get(0).equals("Songs") && (arguments.get(1).isEmpty()||arguments.get(1)==null)) {
                        // Emptying the entire songs foler, so need to recreate it on finish
                        storageAccess.createFolder(this,mainActivityInterface,"Songs","","");
                    }
                    //Rebuild the song index
                    updateSongMenu(fragName, callingFragment, arguments); // Passing the fragment allows an update to be sent to the calling fragment
                    break;

                case "deleteHighlighter":
                    // Try to send the info back to the highlighter edit fragment
                    try {
                        ((HighlighterEditFragment)callingFragment).doDelete(agree);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    allowToast = false;
                    break;

                case "exit":
                    // Close the app.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();
                    } else {
                        finishAffinity();
                    }
                    allowToast = false;
                    break;

                case "newSet":
                    // Clear the current set
                    currentSet.initialiseTheSet();
                    updateFragment("set_updateView",null,null);
                    result = true;
                    break;

            }
            if (result && allowToast) {
                // Don't show toast for exit, but other successful actions
                showToast.doIt(this,getString(R.string.success));
            } else if (allowToast){
                showToast.doIt(this,getString(R.string.error));
            }
        }
    }

    @Override
    public void updateSongMenu(String fragName, Fragment callingFragment, ArrayList<String> arguments) {
        // This is a full rebuild
        // If sent called from another fragment the fragName and callingFragment are used to run an update listener
        songListBuildIndex.setIndexComplete(false);
        // Get all of the files as an array list
        ArrayList<String> songIds = storageAccess.listSongs(this, mainActivityInterface);
        // Write this to text file
        storageAccess.writeSongIDFile(this, mainActivityInterface, songIds);
        // Try to create the basic databases
        sqLiteHelper.resetDatabase(this);
        nonOpenSongSQLiteHelper.initialise(this, mainActivityInterface);
        // Add entries to the database that have songid, folder and filename fields
        // This is the minimum that we need for the song menu.
        // It can be upgraded asynchronously in StageMode/PresenterMode to include author/key
        // Also will later include all the stuff for the search index as well
        sqLiteHelper.insertFast(this, mainActivityInterface);
        if (fragName!=null) {
            //Update the fragment
            updateFragment(fragName,callingFragment,arguments);
        }
        // Now build it properly
        indexSongs();
    }
    @Override
    public void doExport(String what) {
        Intent intent;
        switch (what) {
            case "ccliLog":
                intent = exportFiles.exportActivityLog(this, mainActivityInterface);
                startActivityForResult(Intent.createChooser(intent, "ActivityLog.xml"), 2222);
        }
    }
    @Override
    public void updateSetList() {
        Log.d(TAG,"Update set list");
    }
    @Override
    public void startAutoscroll (){
        Log.d(TAG,"Start auto scroll");
    }
    @Override
    public void stopAutoscroll (){
        Log.d(TAG,"Stop auto scroll");
    }
    @Override
    public void fadeoutPad() {
        Log.d(TAG,"fadeoutPad()");
    }
    @Override
    public void playPad() {
        Log.d(TAG,"playPad()");
    }
    @Override
    public void fixOptionsMenu() {invalidateOptionsMenu();}
    @Override
    public void songSelectDownloadPDF(Fragment fragment, int fragId, Uri uri) {
        if (fragment!=null && fragId==R.id.importOnlineFragment) {
            try {
                ((ImportOnlineFragment)fragment).finishedDownloadPDF(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode==12345) {
            // We initiated an OpenSongApp backup
            preferences.setMyPreferenceInt(this,"runssincebackup",0);

        } else if (intent!=null) {
            importUri = intent.getData();
            boolean filetypeerror = false;
            if (intent.getDataString()!=null) {
                importFilename = storageAccess.getActualFilename(this,intent.getDataString());
            }
            if (requestCode == preferences.getFinalInt("REQUEST_FILE_CHOOSER")) {
                Log.d(TAG,"File chosen: "+intent.getData());
            } else if (requestCode == preferences.getFinalInt("REQUEST_OSB_FILE")) {
                if (importFilename!=null && importUri!=null &&
                        importFilename.toLowerCase(fixLocale.getLocale()).endsWith(".osb")) {
                    // OSB file detected
                    navigateToFragment(null,R.id.importOSBFragment);

                } else {
                    filetypeerror = true;
                }
            }

            if (filetypeerror) {
                showToast.doIt(this,getString(R.string.file_type) + " - " + getString(R.string.unknown));
            }
        }


    }

    // Get references to the objects set in MainActivity
    @Override
    public void setMainActivityInterface(MainActivityInterface mainActivityInterface) {
        Log.d(TAG,"setMainActivityInterface("+mainActivityInterface+")");
        if (mainActivityInterface!=null) {
            this.mainActivityInterface = mainActivityInterface;
        }
    }
    @Override
    public MainActivityInterface getMainActivityInterface() {
        return mainActivityInterface;
    }
    @Override
    public Activity getActivity() {
        return this;
    }
    @Override
    public MediaPlayer getMediaPlayer(int i) {
        // Keeping mediaPlayers in the MainActivity so they persist across fragments
        if (i==1) {
            return mediaPlayer1;
        } else {
            return mediaPlayer2;
        }
    }
    @Override
    public SetTypeFace getMyFonts() {
        return setTypeFace;
    }
    @Override
    public ThemeColors getMyThemeColors() {
        return themeColors;
    }
    @Override
    public void setDrawNotes(DrawNotes view) {
        drawNotes = view;
    }
    @Override
    public DrawNotes getDrawNotes() {
        return drawNotes;
    }
    @Override
    public StorageAccess getStorageAccess() {
        return storageAccess;
    }
    @Override
    public Preferences getPreferences() {
        return preferences;
    }
    @Override
    public ExportActions getExportActions() {
        return exportActions;
    }
    @Override
    public ConvertChoPro getConvertChoPro() {
        return convertChoPro;
    }
    @Override
    public ConvertTextSong getConvertTextSong() {
        return convertTextSong;
    }
    @Override
    public ProcessSong getProcessSong() {
        return processSong;
    }
    @Override
    public SQLiteHelper getSQLiteHelper() {
        return sqLiteHelper;
    }
    @Override
    public NonOpenSongSQLiteHelper getNonOpenSongSQLiteHelper() {
        return nonOpenSongSQLiteHelper;
    }
    @Override
    public CommonSQL getCommonSQL() {
        return commonSQL;
    }
    @Override
    public CCLILog getCCLILog() {
        return ccliLog;
    }
    @Override
    public Midi getMidi(MainActivityInterface mainActivityInterface) {
        // First update the mainActivityInterface used in midi
        midi.setMainActivityInterface(mainActivityInterface);
        // Return a reference to midi
        return midi;
    }
    @Override
    public Midi getMidi() {
        return midi;
    }
    @Override
    public void updateValue(Fragment fragment, String fragname, String which, String value) {
        // This takes the info from the TextInputBottomSheet and passes back to the calling fragment
        Log.d(TAG, "fragment: "+fragment);
        Log.d(TAG, "fragname: "+fragname);
        Log.d(TAG, "value: "+value);
        if (fragment!=null) {
            switch (fragname) {
                case "SettingsCCLI":
                    ((SettingsCCLI)fragment).updateValue(which,value);
                    break;
                case "NearbyConnectionsFragment":
                    ((NearbyConnectionsFragment)fragment).updateValue(which,value);
                    break;
                case "SetManageFragment":
                    ((SetManageFragment)fragment).updateValue(which,value);
                    break;
            }
        }
    }
    @Override
    public String getImportFilename() {
        return importFilename;
    }
    @Override
    public Uri getImportUri() {
        return importUri;
    }
    @Override
    public void setImportFilename(String importFilename) {
        this.importFilename = importFilename;
    }
    @Override
    public void setImportUri(Uri importUri) {
        this.importUri = importUri;
    }
    @Override
    public ShowToast getShowToast() {
        return showToast;
    }
    @Override
    public Locale getLocale() {
        return locale;
    }
    @Override
    public String getWhattodo() {
        return whattodo;
    }
    @Override
    public void setWhattodo(String whattodo) {
        this.whattodo = whattodo;
    }
    @Override
    public String getMode() {
        return whichMode;
    }
    @Override
    public void setMode(String whichMode) {
        this.whichMode = whichMode;
    }
    @Override
    public SetActions getSetActions() {
        return setActions;
    }
    @Override
    public CurrentSet getCurrentSet() {
        return currentSet;
    }
    @Override
    public LoadSong getLoadSong() {
        return loadSong;
    }
    @Override
    public void setNearbyOpen(boolean nearbyOpen) {
        this.nearbyOpen = nearbyOpen;
    }
    @Override
    public AutoscrollActions getAutoscrollActions() {
        return autoscrollActions;
    }
    @Override
    public WebDownload getWebDownload() {
        return webDownload;
    }
    @Override
    public ConvertOnSong getConvertOnSong() {
        return convertOnSong;
    }
    @Override
    public PedalActions getPedalActions() {
        return pedalActions;
    }
    @Override
    public PageButtons getPageButtons() {
        return pageButtons;
    }
    @Override
    public Swipes getSwipes() {
        return swipes;
    }
    @Override
    public DoVibrate getDoVibrate() {
        return doVibrate;
    }
    @Override
    public SaveSong getSaveSong() {
        return saveSong;
    }
    @Override
    public PadFunctions getPadFunctions() {
        return padFunctions;
    }
    @Override
    public Metronome getMetronome() {
        return metronome;
    }
    @Override
    public SongListBuildIndex getSongListBuildIndex() {
        return songListBuildIndex;
    }
    @Override
    public CustomAnimation getCustomAnimation() {
        return customAnimation;
    }
    @Override
    public PDFSong getPDFSong() {
        return pdfSong;
    }
    @Override
    public OCR getOCR() {
        return ocr;
    }
    @Override
    public ShowCase getShowCase() {
        return showCase;
    }
    @Override
    public MakePDF getMakePDF() {
        return makePDF;
    }
    @Override
    public SongSheetHeaders getSongSheetHeaders() {
        return songSheetHeaders;
    }
    @Override
    public PrepareFormats getPrepareFormats() {
        return prepareFormats;
    }
    @Override
    public VersionNumber getVersionNumber() {
        return versionNumber;
    }
    @Override
    public Transpose getTranspose() {
        return transpose;
    }
    @Override
    public AppActionBar getAppActionBar() {
        return appActionBar;
    }
    @Override
    public int getFragmentOpen() {
        return fragmentOpen;
    }
    @Override
    public Gestures getGestures() {
        return gestures;
    }
    @Override
    public void setScreenshot(Bitmap bitmap) {
        screenShot = Bitmap.createBitmap(bitmap);
        bitmap.recycle();
    }
    @Override
    public Bitmap getScreenshot() {
        return screenShot;
    }
    @Override
    public ABCNotation getAbcNotation() {
        return abcNotation;
    }
    @Override
    public AlertChecks getAlertChecks() {
        return alertChecks;
    }
    @Override
    public ProfileActions getProfileActions() {
        return profileActions;
    }
    @Override
    public CheckInternet getCheckInternet() {
        return checkInternet;
    }


    // The song views
    @Override
    public ArrayList<View> getSectionViews() {
        return sectionViews;
    }
    @Override
    public void setSectionViews(ArrayList<View> views) {
        if (views==null) {
            // Reset the views and their sizes
            sectionViews = null;
            sectionViews = new ArrayList<>();
            sectionWidths = null;
            sectionWidths = new ArrayList<>();
            sectionHeights = null;
            sectionHeights = new ArrayList<>();
        } else {
            sectionViews = views;
        }
    }
    @Override
    public ArrayList<Integer> getSectionWidths() {
        return sectionWidths;
    }
    @Override
    public ArrayList<Integer> getSectionHeights() {
        return sectionHeights;
    }
    @Override
    public void addSectionSize(int width, int height) {
        if (sectionWidths==null) {
            sectionWidths = new ArrayList<>();
        }
        if (sectionHeights==null) {
            sectionHeights = new ArrayList<>();
        }
        sectionWidths.add(width);
        sectionHeights.add(height);
    }


    // The song sheet title.  Can be displayed in Performance mode and PDF creation
    @Override
    public LinearLayout getSongSheetTitleLayout() {
        if (songSheetTitleLayout==null) {
            songSheetTitleLayout = new LinearLayout(this);
        }
        return songSheetTitleLayout;
    }
    @Override
    public void setSongSheetTitleLayout(LinearLayout linearLayout) {
        if (songSheetTitleLayout==null) {
            songSheetTitleLayout = new LinearLayout(this);
        }
        if (linearLayout==null) {
            // Remove the views
            songSheetTitleLayout.removeAllViews();
            songSheetTitleLayoutSize = new ArrayList<>();
        } else {
            songSheetTitleLayout.addView(linearLayout);
        }
    }
    @Override
    public ArrayList<Integer> getSongSheetTitleLayoutSize() {
        return songSheetTitleLayoutSize;
    }
    @Override
    public void setSongSheetTitleLayoutSize(ArrayList<Integer> sizes) {
        songSheetTitleLayoutSize = sizes;
    }




    // Nearby
    @Override
    public void startDiscovery(Context c, MainActivityInterface mainActivityInterface) {
        nearbyConnections.startDiscovery(c,mainActivityInterface);
    }
    @Override
    public void startAdvertising(Context c, MainActivityInterface mainActivityInterface) {
        nearbyConnections.startAdvertising(c,mainActivityInterface);
    }
    @Override
    public void stopDiscovery(Context c) {
        nearbyConnections.stopDiscovery(c);
    }
    @Override
    public void stopAdvertising(Context c) {
        nearbyConnections.stopAdvertising(c);
    }
    @Override
    public void turnOffNearby(Context c) {
        nearbyConnections.turnOffNearby(c);
    }
    @Override
    public void doSendPayloadBytes(Context c,String infoPayload) {
        // TODO - IV addition to check if needed (obs no FullscreenActivity anymore!
        // // IV - Do not send section 0 payload when loading a song
        //        if (!FullscreenActivity.alreadyloading) {
        nearbyConnections.doSendPayloadBytes(c,infoPayload);
        // }
    }
    @Override
    public boolean requestNearbyPermissions() {
        // Only do this if the user has Google APIs installed, otherwise, there is no point
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            return requestCoarseLocationPermissions() && requestFineLocationPermissions();
        } else {
            installPlayServices();
            // Not allowed on this device
            return false;
        }
    }
    @Override
    public boolean requestCoarseLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            try {
                make(findViewById(R.id.fragmentView), R.string.location_rationale,
                        LENGTH_INDEFINITE).setAction(android.R.string.ok, view -> ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 403)).show();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 403);
            return false;
        }
    }
    @Override
    public boolean requestFineLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            try {
                make(findViewById(R.id.coordinator), R.string.location_rationale,
                        LENGTH_INDEFINITE).setAction(android.R.string.ok, view -> ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 404)).show();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 404);
            return false;
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
    public void updateConnectionsLog() {
        // Send the command to the Nearby Connections fragment (if it exists!)
        try {
            if (nearbyConnectionsFragment!=null && nearbyOpen) {
                try {
                    nearbyConnectionsFragment.updateConnectionsLog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public NearbyConnections getNearbyConnections(MainActivityInterface mainActivityInterface) {
        // First update the mainActivityInterface used in nearby connections
        nearbyConnections.setMainActivityInterface(mainActivityInterface);
        // Return a reference to nearbyConnections
        return nearbyConnections;
    }
    @Override
    public NearbyConnections getNearbyConnections() {
        return nearbyConnections;
    }
    

    // Get permissions request callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            switch (requestCode) {
                case StaticVariables.REQUEST_CAMERA_CODE:
                    //startCamera();
                    break;

                case 404:
                case 403:
                    // Access fine location, so can open the menu at 'Connect devices'
                    if (whattodo.equals("nearby")) {
                        openNearbyFragment();
                    }
                    Log.d("d", "LOCATION granted!");
                    break;
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void sendMidiFromList(int item) {
        if (fragmentOpen==R.id.midiFragment) {
            ((MidiFragment)registeredFragment).sendMidiFromList(item);
        }
    }
    @Override
    public void deleteMidiFromList(int item) {
        if (fragmentOpen==R.id.midiFragment) {
            ((MidiFragment)registeredFragment).deleteMidiFromList(item);
        }
    }


    // Pedal listeners either dealt with in PedalActions or sent to PedalsFragment for setup
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        // If pedalsFragment is open, send the keyCode and event there
        if (fragmentOpen==R.id.pedalsFragment && ((PedalsFragment)registeredFragment).isListening()) {
            ((PedalsFragment)registeredFragment).keyDownListener(keyCode);
            return false;
        } else {
            pedalActions.commonEventDown(keyCode, null);
        }
        return super.onKeyDown(keyCode, keyEvent);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
        // If pedalsFragment is open, send the keyCode and event there
        if (fragmentOpen==R.id.pedalsFragment && ((PedalsFragment)registeredFragment).isListening()) {
            ((PedalsFragment)registeredFragment).commonEventUp();
            return true;
        } else {
            pedalActions.commonEventUp(keyCode,null);
        }
        return super.onKeyUp(keyCode, keyEvent);
    }
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent keyEvent) {
        // If pedalsFragment is open, send the keyCode and event there
        if (fragmentOpen==R.id.pedalsFragment && ((PedalsFragment)registeredFragment).isListening()) {
            ((PedalsFragment)registeredFragment).commonEventLong();
            return true;
        } else {
            pedalActions.commonEventLong(keyCode,null);
        }
        return super.onKeyLongPress(keyCode, keyEvent);
    }
    @Override
    public void registerMidiAction(boolean actionDown, boolean actionUp, boolean actionLong, String note) {
        // If pedalsFragment is open, send the midiNote and event there
        if (fragmentOpen==R.id.pedalsFragment && ((PedalsFragment)registeredFragment).isListening()) {
            if (actionDown) {
                ((PedalsFragment) registeredFragment).midiDownListener(note);
            } else if (actionUp) {
                ((PedalsFragment) registeredFragment).commonEventUp();
            } else if (actionLong) {
                ((PedalsFragment) registeredFragment).commonEventLong();
            }
        } else {
            if (actionDown) {
                pedalActions.commonEventDown(-1,note);
            } else if (actionUp) {
                pedalActions.commonEventUp(-1,note);
            } else if (actionLong) {
                pedalActions.commonEventLong(-1,note);
            }
        }
    }


    // The return actions from the NearbyReturnActionsInterface
    private void openNearbyFragment() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            navigateToFragment("opensongapp://settings/nearby", 0);
        }
    }
    @Override
    public void gesture5() {
        // TODO
    }
    @Override
    public void selectSection(int i) {
        // TODO
    }
    @Override
    public void prepareSongMenu() {
        // TODO
    }
    @Override
    public void loadSong() {
        // TODO
    }
    @Override
    public void goToPreviousItem() {
        // TODO
    }
    @Override
    public void goToNextItem() {
        // TODO
    }
    @Override
    public void showSticky() {
        // Try to show the sticky note
        Log.d(TAG,"showSticky");
        if (!whichMode.equals("Presentation") && performanceFragment!=null) {
            try {
                performanceFragment.dealWithStickyNotes(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void metronomeToggle() {
        Log.d(TAG, mainActivityInterface.getSong().getTitle());
        Log.d(TAG, "metronomeToggle()  isRunning="+metronome.getIsRunning());
        if (!metronome.getIsRunning()) {
            metronome.startMetronome(this,this,mainActivityInterface);
        } else {
            metronome.stopMetronome();
        }
    }


    // Backpress to exit the app
    @Override
    public void onBackPressed() {
        if (navController.getCurrentDestination()!=null &&
                navController.getCurrentDestination().getId()==R.id.performanceFragment) {
            displayAreYouSure("exit", getString(R.string.exit_confirm), null,
                    navController.getCurrentDestination().getNavigatorName(),
                    navHostFragment, null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void getSwipeValues(int minDistance, int minHeight, int minTime) {
        if (swipeFragment!=null) {
            try {
                swipeFragment.getSwipeValues(minDistance, minHeight, minTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void isWebConnected(Fragment fragment, int fragId, boolean connected) {
        // This is the result of an internet connection check
        if (fragment!=null) {
            try {
                if (fragId==R.id.fontSetupFragment) {
                    ((FontSetupFragment) fragment).isConnected(connected);
                } else if (fragId==R.id.importOnlineFragment) {
                    ((ImportOnlineFragment) fragment).isConnected(connected);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void openWebPage(String local, String location) {
        // I could pass the address in as a location string,
        // However, for the user-guide to avoid having to change loads of files
        // I keep them listed here.
        if (local!=null && !local.isEmpty()) {
            switch (local) {
                case "mode":
                    location = "https://www.opensongapp.com/user-guide/the-app-modes";
                    break;
                case "storage":
                    location = "https://www.opensongapp.com/user-guide/setting-up-and-using-opensongapp/setting-up-opensong-tablet";
                    break;
            }
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(location));
            startActivity(intent);
        } catch (Exception e) {
            // Probably no browser installed or no internet permission given.
            e.printStackTrace();
        }
    }
}
