package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.abcnotation.ABCNotation;
import com.garethevans.church.opensongtablet.aeros.Aeros;
import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.appdata.AlertChecks;
import com.garethevans.church.opensongtablet.appdata.BootUpFragment;
import com.garethevans.church.opensongtablet.appdata.CheckInternet;
import com.garethevans.church.opensongtablet.appdata.FixLocale;
import com.garethevans.church.opensongtablet.appdata.MyFonts;
import com.garethevans.church.opensongtablet.appdata.VersionNumber;
import com.garethevans.church.opensongtablet.autoscroll.Autoscroll;
import com.garethevans.church.opensongtablet.beatbuddy.BBOptionsFragment;
import com.garethevans.church.opensongtablet.beatbuddy.BeatBuddy;
import com.garethevans.church.opensongtablet.bible.Bible;
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.ccli.SettingsCCLI;
import com.garethevans.church.opensongtablet.chords.ChordDirectory;
import com.garethevans.church.opensongtablet.chords.ChordDisplayProcessing;
import com.garethevans.church.opensongtablet.chords.CustomChordsFragment;
import com.garethevans.church.opensongtablet.chords.Transpose;
import com.garethevans.church.opensongtablet.controls.CommonControls;
import com.garethevans.church.opensongtablet.controls.Gestures;
import com.garethevans.church.opensongtablet.controls.HotZones;
import com.garethevans.church.opensongtablet.controls.PageButtons;
import com.garethevans.church.opensongtablet.controls.PedalActions;
import com.garethevans.church.opensongtablet.controls.PedalsFragment;
import com.garethevans.church.opensongtablet.controls.SwipeFragment;
import com.garethevans.church.opensongtablet.controls.Swipes;
import com.garethevans.church.opensongtablet.customslides.CustomSlide;
import com.garethevans.church.opensongtablet.customslides.CustomSlideFragment;
import com.garethevans.church.opensongtablet.customviews.DrawNotes;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.MyToolbar;
import com.garethevans.church.opensongtablet.databinding.ActivityBinding;
import com.garethevans.church.opensongtablet.drummer.Drummer;
import com.garethevans.church.opensongtablet.export.ExportActions;
import com.garethevans.church.opensongtablet.export.PrepareFormats;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.SaveSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.filemanagement.StorageManagementFragment;
import com.garethevans.church.opensongtablet.highlighter.HighlighterEditFragment;
import com.garethevans.church.opensongtablet.importsongs.ImportFileFragment;
import com.garethevans.church.opensongtablet.importsongs.ImportOnlineFragment;
import com.garethevans.church.opensongtablet.importsongs.ImportOptionsFragment;
import com.garethevans.church.opensongtablet.importsongs.WebDownload;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.DialogReturnInterface;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyReturnActionsInterface;
import com.garethevans.church.opensongtablet.interfaces.SwipeDrawingInterface;
import com.garethevans.church.opensongtablet.links.LinksFragment;
import com.garethevans.church.opensongtablet.metronome.Metronome;
import com.garethevans.church.opensongtablet.midi.Midi;
import com.garethevans.church.opensongtablet.midi.MidiActionBottomSheet;
import com.garethevans.church.opensongtablet.nearby.NearbyConnections;
import com.garethevans.church.opensongtablet.nearby.NearbyConnectionsFragment;
import com.garethevans.church.opensongtablet.pads.Pad;
import com.garethevans.church.opensongtablet.pdf.MakePDF;
import com.garethevans.church.opensongtablet.pdf.OCR;
import com.garethevans.church.opensongtablet.performance.DisplayPrevNext;
import com.garethevans.church.opensongtablet.performance.PerformanceFragment;
import com.garethevans.church.opensongtablet.performance.PerformanceGestures;
import com.garethevans.church.opensongtablet.preferences.AppPermissions;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.ProfileActions;
import com.garethevans.church.opensongtablet.presenter.PresenterFragment;
import com.garethevans.church.opensongtablet.presenter.PresenterSettings;
import com.garethevans.church.opensongtablet.presenter.SongSectionsFragment;
import com.garethevans.church.opensongtablet.screensetup.BatteryStatus;
import com.garethevans.church.opensongtablet.screensetup.FontSetupFragment;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.screensetup.ThemeSetupFragment;
import com.garethevans.church.opensongtablet.screensetup.WindowFlags;
import com.garethevans.church.opensongtablet.secondarydisplay.SecondaryDisplay;
import com.garethevans.church.opensongtablet.secondarydisplay.SecondaryDisplaySettingsFragment;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.setmenu.SetMenuFragment;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.setprocessing.SetManageFragment;
import com.garethevans.church.opensongtablet.songmenu.SongListBuildIndex;
import com.garethevans.church.opensongtablet.songmenu.SongMenuFragment;
import com.garethevans.church.opensongtablet.songmenu.ViewPagerAdapter;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ConvertTextSong;
import com.garethevans.church.opensongtablet.songprocessing.ConvertWord;
import com.garethevans.church.opensongtablet.songprocessing.EditSongFragment;
import com.garethevans.church.opensongtablet.songprocessing.EditSongFragmentMain;
import com.garethevans.church.opensongtablet.songprocessing.EditSongFragmentTags;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongActionsMenuFragment;
import com.garethevans.church.opensongtablet.songprocessing.SongSheetHeaders;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.garethevans.church.opensongtablet.tags.BulkTagAssignFragment;
import com.garethevans.church.opensongtablet.utilities.ForumFragment;
import com.garethevans.church.opensongtablet.utilities.TimeTools;
import com.garethevans.church.opensongtablet.webserver.WebServer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textview.MaterialTextView;
import com.gu.toolargetool.TooLargeTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MainActivityInterface,
        ActionInterface, NearbyInterface, NearbyReturnActionsInterface, DialogReturnInterface,
        SwipeDrawingInterface, BatteryStatus.MyInterface,
        DisplayInterface, EditSongFragmentInterface {

    private ActivityBinding myView;
    private boolean bootUpCompleted = false;
    private boolean rebooted = false, alreadyBackPressed = false;

    // Initialise the Executors and main handlers for async tasks
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() ,               // Initial pool size
            (Runtime.getRuntime().availableProcessors() * 8),          // Max pool size (including queued)
            1000,                                                      // Time for idle thread to remain
            TimeUnit.MILLISECONDS,                                     // Unit
            new ArrayBlockingQueue<>(10)                     // Blocking queue
    );

    // The helpers sorted alphabetically
    private ABCNotation abcNotation;
    private Aeros aeros;
    private AlertChecks alertChecks;
    //private CustomToolBar customToolBar;
    private Autoscroll autoscroll;
    private BeatBuddy beatBuddy;
    private Bible bible;
    private CCLILog ccliLog;
    private CheckInternet checkInternet;
    private ChordDirectory chordDirectory;
    private ChordDisplayProcessing chordDisplayProcessing;
    private CommonControls commonControls;
    private CommonSQL commonSQL;
    private ConvertChoPro convertChoPro;
    private ConvertOnSong convertOnSong;
    private ConvertTextSong convertTextSong;
    private ConvertWord convertWord;
    private CurrentSet currentSet;
    private CustomAnimation customAnimation;
    private CustomSlide customSlide;
    private DisplayPrevNext displayPrevNext;
    private DrawNotes drawNotes;
    private Drummer drummer;
    private ExportActions exportActions;
    private FixLocale fixLocale;
    private Gestures gestures;
    private HotZones hotZones;
    private LoadSong loadSong;
    private MakePDF makePDF;
    private Metronome metronome;
    private Midi midi;
    private NearbyConnections nearbyConnections;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private OCR ocr;
    private Pad pad;
    private PageButtons pageButtons;
    private PedalActions pedalActions;
    private PerformanceGestures performanceGestures;
    private AppPermissions appPermissions;
    private Preferences preferences;
    private PrepareFormats prepareFormats;
    private PresenterSettings presenterSettings;
    private ProcessSong processSong;
    private ProfileActions profileActions;
    private SaveSong saveSong;
    private SetActions setActions;
    private MyFonts myFonts;
    private ShowCase showCase;
    private ShowToast showToast;
    private Song song, tempSong, indexingSong;
    private SongListBuildIndex songListBuildIndex;
    private SongSheetHeaders songSheetHeaders;
    private SQLiteHelper sqLiteHelper;
    private StorageAccess storageAccess;
    private Swipes swipes;
    private ThemeColors themeColors;
    private TimeTools timeTools;
    private Transpose transpose;
    private VersionNumber versionNumber;
    private WebDownload webDownload;
    private WebServer webServer;

    // The navigation controls
    private NavHostFragment navHostFragment;
    private NavController navController;

    // Other views/listeners/helpers
    private WindowFlags windowFlags;
    private BatteryStatus batteryStatus;
    private ViewPagerAdapter viewPagerAdapter;
    private SongMenuFragment songMenuFragment;
    private SetMenuFragment setMenuFragment;
    private PerformanceFragment performanceFragment;
    private PresenterFragment presenterFragment;
    private EditSongFragment editSongFragment;
    private NearbyConnectionsFragment nearbyConnectionsFragment;
    private PedalsFragment pedalsFragment;
    private BootUpFragment bootUpFragment;
    private boolean waitingOnBootUpFragment = false;
    private ViewPager2 viewPager;
    private AppBarConfiguration appBarConfiguration;
    private SecondaryDisplay[] secondaryDisplays;
    private Display[] connectedDisplays;
    private int prevNumConnectedDisplays = 0;
    private ImageView screenHelp;
    private final Handler mainLooper = new Handler(Looper.getMainLooper());

    // Variables used
    private ArrayList<View> targets;
    private ArrayList<String> infos;
    private ArrayList<Boolean> rects;
    private ArrayList<View> sectionViews;
    private LinearLayout songSheetTitleLayout;
    private ArrayList<Integer> sectionWidths, sectionHeights, sectionColors;
    private String whichMode, whattodo, importFilename;
    private final String presenter = "Presenter", performance = "Performance";
    private Uri importUri;
    private boolean settingsOpen = false, showSetMenu,
            pageButtonActive = true, menuOpen, firstRun = true;
    private final String TAG = "MainActivity";
    private Menu globalMenuItem;
    private Locale locale;
    private File screenshotFile;
    private Runnable hideActionButtonRunnable;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    private Intent fileOpenIntent;
    private int availableWidth=-1, availableHeight=-1;

    private String deeplink_import_osb = "", deeplink_sets_backup_restore = "", deeplink_onsong = "",
            deeplink_import_file = "", unknown = "", mainfoldername = "MAIN", deeplink_page_buttons = "",
            website_menu_set = "", website_menu_song = "", exit_confirm = "",
            error = "", deeplink_presenter = "", deeplink_performance = "", extra_settings = "",
            action_button_info = "", song_sections = "", logo_info = "", blank_screen_info = "",
            black_screen_info = "", project_panic = "", song_title = "", long_press = "", edit_song = "",
            song_sections_project = "", menu_song_info = "", menu_set_info = "", add_songs = "",
            song_actions = "", settings = "", deeplink_preferences = "", song_string = "", set_string = "",
            search_index_start = "", search_index_end = "", deeplink_metronome = "",
            mode_presenter = "", mode_performance = "", mode_stage = "", success = "", okay = "", pad_playback_info = "",
            no_suitable_application = "", indexing_string = "", deeplink_edit = "", cast_info_string = "",
            menu_showcase_info ="";

    // ViewPager2 messes up id on restarts causing issues on restoreinstancestate
    //public static final String KEY_GENERATED_VIEW_ID = "generated_view_id";
    //private static final String KEY_PAGER_ID = "pager_id";
    //private Bundle savedInstanceState;

    // Used if implementing Oboe using C++ injection
    /* static {System.loadLibrary("lowlatencyaudio");} */

    // Set up the activity
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up crash collector
        setUpCrashCollector();

        // Set up the onBackPressed intercepter as onBackPressed is deprecated
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                interceptBackPressed();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        if (myView == null) {
            myView = ActivityBinding.inflate(getLayoutInflater());
            try {
                setContentView(myView.getRoot());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Get the user locale and prepare the strings
        prepareStrings();

        // Attempt stuff using the threadPooleExecutor
        getThreadPoolExecutor().execute(() -> {

            // Set the hardware acceleration
            setHardwareAcceleration();

            mainLooper.post(() -> {
                // TODO can remove once we track down TransactionTooLarge crash
                TooLargeTool.startLogging(this.getApplication());
                WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            });

            if (savedInstanceState != null) {
                //ensureGeneratedViewIdGreaterThan(savedInstanceState.getInt(KEY_GENERATED_VIEW_ID, 0));
                Log.d(TAG,"TooLargeTool is logging:"+TooLargeTool.bundleBreakdown(savedInstanceState));
                bootUpCompleted = savedInstanceState.getBoolean("bootUpCompleted", false);
                rebooted = true;
                getSongListBuildIndex();

                songListBuildIndex.setIndexComplete(savedInstanceState.getBoolean("indexComplete", false));
                songListBuildIndex.setFullIndexRequired(!songListBuildIndex.getIndexComplete());

                nearbyConnections = getNearbyConnections();
                nearbyConnections.setIsHost(savedInstanceState.getBoolean("isHost", false));
                nearbyConnections.setUsingNearby(savedInstanceState.getBoolean("usingNearby", false));
                nearbyConnections.setDiscoveredEndpoints(savedInstanceState.getStringArrayList("discoveredEndpoints"));
                nearbyConnections.setConnectedEndpoints(savedInstanceState.getStringArrayList("connectedEndpoints"));
                // If we were using Nearby, try to start it again
                if (nearbyConnections.getUsingNearby() && nearbyConnections.getIsHost()) {
                    nearbyConnections.doTempAdvertise();
                } else if (nearbyConnections.getUsingNearby() && !nearbyConnections.getIsHost()) {
                    nearbyConnections.doTempDiscover();
                }

                // Make sure the song title is there
                updateToolbar(null);

                // Clear the saved instance state - we've finished with everything we need.
                savedInstanceState.clear();

            } else {
                rebooted = false;
            }

            // Did we receive an intent (user clicked on an openable file)?
            fileOpenIntent = getIntent();
            onNewIntent(fileOpenIntent);

            mainLooper.post(() -> {
                setContentView(myView.getRoot());

                // Set up the helpers
                setupHelpers();

                // Set up the action bar
                setupActionbar();

                // Set up views
                setupViews();

                // Now if we are showing the bootup fragment, proceed with that
                waitingOnBootUpFragment = true;

                if (bootUpFragment!=null) {
                    try {
                        hideActionBar();
                        bootUpFragment.startOrSetUp();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    @Override
    public boolean getWaitingOnBootUpFragment() {
        return waitingOnBootUpFragment;
    }


    /**
     * ViewCompat.generateViewId stores the current ID in a static variable.
     * When the process is killed, the variable gets reset.
     * This makes sure that we do not get ID collisions
     * and therefore errors when trying to restore state from another view.

    //@SuppressWarnings("StatementWithEmptyBody")
    private void ensureGeneratedViewIdGreaterThan(int minimum) {
        while (ViewCompat.generateViewId() <= minimum) {
            // Generate new IDs
        }
    }*/

    private void setUpCrashCollector() {
        // Set up a default crash capture, but keep a reference to the original handler
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            // Get the stack trace.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            // Write a crash log file
            storageAccess.updateCrashLog(sw.toString());

            // Switch off hardware acceleration if crash happens to try to alleviate the issue
            // Decided for now to leave this off
            //getPreferences().setMyPreferenceBoolean("hardwareAcceleration",false);

            // Reset the unhandled exception handler
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

            // Now turn off the app
            try {
                throw e;
            } catch (Throwable ex) {
                System.exit(1);
            }
        });
    }

    private void setHardwareAcceleration() {
        if (getPreferences().getMyPreferenceBoolean("hardwareAcceleration",true)) {
            try {
                if (getWindow()!=null) {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (getWindow()!=null) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        }
    }

    @Override
    public void recreateActivity() {
        navHome();
        try {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.recreate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View v = getCurrentFocus();
        if (v instanceof EditText || v instanceof ExposedDropDown) {
            int[] scrcoords = new int[2];
            v.getLocationOnScreen(scrcoords);
            // calculate the relative position of the clicking position against the position of the view
            float x = event.getRawX() - scrcoords[0];
            float y = event.getRawY() - scrcoords[1];

            // check whether action is up and the clicking position is outside of the view
            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < 0 || x > v.getRight() - v.getLeft()
                    || y < 0 || y > v.getBottom() - v.getTop())) {
                if (v.getOnFocusChangeListener() != null) {
                    v.getOnFocusChangeListener().onFocusChange(v, false);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public Handler getMainHandler() {
        return mainLooper;
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    private void prepareStrings() {
        // To avoid null context for long tasks throwing error when getting strings
        if (getApplicationContext() != null) {

            // Fix the user locale preference
            getFixLocale().setLocale(this, this);
            locale = fixLocale.getLocale();

            deeplink_import_osb = getString(R.string.deeplink_import_osb);
            deeplink_sets_backup_restore = getString(R.string.deeplink_sets_backup_restore);
            deeplink_onsong = getString(R.string.deeplink_onsong);
            deeplink_import_file = getString(R.string.deeplink_import_file);
            deeplink_edit = getString(R.string.deeplink_edit);
            unknown = getString(R.string.unknown);
            mainfoldername = getString(R.string.mainfoldername);
            deeplink_page_buttons = getString(R.string.deeplink_page_buttons);
            website_menu_set = getString(R.string.website_menu_set);
            website_menu_song = getString(R.string.website_menu_song);
            exit_confirm = getString(R.string.exit_confirm);
            error = getString(R.string.error);
            deeplink_presenter = getString(R.string.deeplink_presenter);
            deeplink_performance = getString(R.string.deeplink_performance);
            extra_settings = getString(R.string.extra_settings);
            action_button_info = getString(R.string.action_button_info);
            song_sections = getString(R.string.song_sections);
            logo_info = getString(R.string.logo_info);
            blank_screen_info = getString(R.string.blank_screen_info);
            black_screen_info = getString(R.string.black_screen_info);
            project_panic = getString(R.string.project_panic);
            song_title = getString(R.string.song_title);
            long_press = getString(R.string.long_press);
            edit_song = getString(R.string.edit_song);
            song_sections_project = getString(R.string.song_sections_project);
            menu_song_info = getString(R.string.menu_song_info);
            menu_set_info = getString(R.string.menu_set_info);
            add_songs = getString(R.string.add_songs);
            song_actions = getString(R.string.song_actions);
            settings = getString(R.string.settings);
            deeplink_preferences = getString(R.string.deeplink_preferences);
            song_string = getString(R.string.song);
            set_string = getString(R.string.set);
            search_index_start = getString(R.string.index_songs_start);
            search_index_end = getString(R.string.index_songs_end);
            deeplink_metronome = getString(R.string.deeplink_metronome);
            mode_presenter = getString(R.string.mode_presenter);
            mode_performance = getString(R.string.mode_performance);
            mode_stage = getString(R.string.mode_stage);
            success = getString(R.string.success);
            okay = getString(R.string.okay);
            pad_playback_info = getString(R.string.pad_playback_info);
            no_suitable_application = getString(R.string.no_suitable_application);
            indexing_string = getString(R.string.index_songs_wait);
            cast_info_string = getString(R.string.cast_info_string);
            menu_showcase_info = getString(R.string.menu_showcase_info);
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        fileOpenIntent = intent;
        // Send the action to be called from the opening fragment to fix backstack!
        if (presenterValid()) {
            presenterFragment.tryToImportIntent();
        } else if (performanceValid()) {
            performanceFragment.tryToImportIntent();
        }
        super.onNewIntent(intent);
    }

    @Override
    public void dealWithIntent(int navigationId) {
        if (storageAccess == null) {
            storageAccess = new StorageAccess(this);
        }
        getThreadPoolExecutor().execute(() -> {
            if (!preferences.getMyPreferenceBoolean("intentAlreadyDealtWith",false) &&
                fileOpenIntent != null && fileOpenIntent.getData() != null && storageAccess.getFileSizeFromUri(fileOpenIntent.getData())>0) {
            preferences.setMyPreferenceBoolean("intentAlreadyDealtWith",true);
            importUri = fileOpenIntent.getData();
            getMainHandler().post(() -> navController.popBackStack(navigationId, false));

            // We need to copy this file to our temp storage for now to have later permission
            InputStream inputStream;
            try {
                inputStream = getContentResolver().openInputStream(importUri);
                importFilename = storageAccess.getFileNameFromUri(importUri);
                if (inputStream != null) {
                    File tempFile = getStorageAccess().getAppSpecificFile("Import","",importFilename);
                    //File tempFile = new File(getExternalFilesDir("Import"), importFilename);
                    FileOutputStream outputStream = new FileOutputStream(tempFile);
                    storageAccess.updateFileActivityLog(TAG + " dealWithIntent CopyFile " + importUri + " to " + tempFile);
                    storageAccess.copyFile(inputStream, outputStream);
                    importUri = Uri.fromFile(tempFile);
                    String dealingWithIntent;
                    if (importFilename.toLowerCase(Locale.ROOT).endsWith(".osb")) {
                        // OpenSongApp backup file
                        dealingWithIntent = deeplink_import_osb;
                    } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".osbs")) {
                        // OpenSongApp sets backup file
                        setWhattodo("intentlaunch");
                        dealingWithIntent = deeplink_sets_backup_restore;
                    } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".ost")) {
                        // OpenSong song
                        setWhattodo("intentlaunch");
                        dealingWithIntent = deeplink_import_file;
                    } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".osts")) {
                        // OpenSong set
                        setWhattodo("intentlaunch");
                        dealingWithIntent = deeplink_import_file;
                    } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".backup")) {
                        // OnSong backup file
                        dealingWithIntent = deeplink_onsong;
                    } else if (getStorageAccess().isSpecificFileExtension("imageorpdf", importFilename) ||
                            getStorageAccess().isSpecificFileExtension("chordpro", importFilename) ||
                            getStorageAccess().isSpecificFileExtension("text", importFilename) ||
                            getStorageAccess().isSpecificFileExtension("onsong", importFilename)) {
                        // Set, song, pdf or image files are initially sent to the import file
                        dealingWithIntent = deeplink_import_file;
                    } else {
                        // Might be an opensong file (with no extension)
                        // If the file size is small enough (<200kB), read it as text and look for </song> and </lyrics> or </set> and </slide_groups>
                        boolean isOpenSong = false;
                        boolean isOpenSongSet = false;
                        String content = "";
                        inputStream = getContentResolver().openInputStream(importUri);
                        if (!importFilename.contains(".") && getStorageAccess().getFileSizeFromUri(importUri)<200) {
                            content = getStorageAccess().readTextFileToString(inputStream);
                        }
                        if (content!=null && content.contains("</song>") && content.contains("</lyrics>")) {
                            isOpenSong = true;
                        }
                        if (content!=null && content.contains("</set>") && content.contains("</slide_groups>")) {
                            isOpenSongSet = true;
                        }
                        if (isOpenSong || isOpenSongSet) {
                            setWhattodo("intentlaunch");
                            dealingWithIntent = deeplink_import_file;
                        } else {
                            // Can't handle the file, so delete it
                            if (showToast != null) {
                                showToast.doIt(unknown);
                            }
                            if (tempFile.delete()) {
                                Log.d(TAG, tempFile + " has been deleted");
                            }
                            setWhattodo("");
                            dealingWithIntent = "";
                        }
                    }
                    if (dealingWithIntent != null && !dealingWithIntent.isEmpty()) {
                        getMainHandler().post(() -> navigateToFragment(dealingWithIntent, 0));
                        // Reset the flag to allow dealing with a new intent as we have handled this one
                        preferences.setMyPreferenceBoolean("intentAlreadyDealtWith",false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Try to clear the intent
        fileOpenIntent = null;
        try {
            getIntent().setData(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        });
    }

    private void setupHelpers() {
        // The get methods check for null and if so, create new instances
        getStorageAccess();
        getPreferences();

        // The song stuff may have been initialised in savedInstanceState
        songListBuildIndex = getSongListBuildIndex();

        // The screen display stuff
        customAnimation = getCustomAnimation();
        showCase = getShowCase();
        getShowToast();

        // The app setup
        versionNumber = getVersionNumber();

        // Connections and alerts
        checkInternet = getCheckInternet();
        getNearbyConnections();
        webDownload = getWebDownload();
        alertChecks = getAlertChecks();
        alertChecks.setAlreadySeen(rebooted);

        // For user preferences
        myFonts = getMyFonts();
        themeColors = getMyThemeColors();
        profileActions = getProfileActions();
        appPermissions = getAppPermissions();

        // The databases
        sqLiteHelper = getSQLiteHelper();
        nonOpenSongSQLiteHelper = getNonOpenSongSQLiteHelper();
        commonSQL = getCommonSQL();

        // Converting song formats and processing song content
        chordDisplayProcessing = getChordDisplayProcessing();
        chordDirectory = getChordDirectory();
        convertChoPro = getConvertChoPro();
        convertOnSong = getConvertOnSong();
        convertTextSong = getConvertTextSong();
        processSong = getProcessSong();
        prepareFormats = getPrepareFormats();
        songSheetHeaders = getSongSheetHeaders();
        ocr = getOCR();
        makePDF = getMakePDF();
        transpose = getTranspose();
        abcNotation = getAbcNotation();
        song = getSong();

        // Loading up songs and the indexing
        loadSong = getLoadSong();
        saveSong = getSaveSong();

        // Sets
        currentSet = getCurrentSet();
        setActions = getSetActions();

        // Song actions/features
        hotZones = getHotZones();
        commonControls = getCommonControls();
        performanceGestures = getPerformanceGestures();
        pageButtons = getPageButtons();
        midi = getMidi();
        aeros = getAeros();
        beatBuddy = getBeatBuddy();
        drummer = getDrummer();
        pedalActions = getPedalActions();
        pad = getPad();
        autoscroll = getAutoscroll();
        metronome = getMetronome();
        gestures = getGestures();
        swipes = getSwipes();
        timeTools = getTimeTools();
        displayPrevNext = getDisplayPrevNext();

        // Other file actions
        ccliLog = getCCLILog();
        exportActions = getExportActions();
        bible = getBible();
        customSlide = getCustomSlide();
        presenterSettings = getPresenterSettings();

        // Webserver (for displaying song over html server)
        webServer = getWebServer();
    }

    @Override
    public String getMainfoldername() {
        return mainfoldername;
    }

    @Override
    public BatteryStatus getBatteryStatus() {
        if (batteryStatus == null && myView!=null) {
            batteryStatus = new BatteryStatus(this, myView.myToolbar.getBatteryimage(),
                    myView.myToolbar.getBatterycharge(), myView.myToolbar.getActionBarHeight(true));
        }
        return batteryStatus;
    }

    private void setupActionbar() {
        setSupportActionBar(myView.myToolbar);
    }

    @Override
    public ImageView disableActionBarStuff(boolean disable) {
        // Called from storage selection
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!disable);
        }
        if (globalMenuItem!=null) {
            globalMenuItem.findItem(R.id.settings_menu_item).setVisible(!disable);
        }
        return screenHelp;
    }

    @Override
    public void showActionBar() {
        if (myView!=null) {
            myView.myToolbar.showActionBar(settingsOpen);
            updateMargins();
        }
    }

    @Override
    public void updateMargins() {
        if (myView!=null && windowFlags!=null) {
            mainLooper.post(() -> {
                if (settingsOpen || whichMode.equals(mode_presenter)) {
                    myView.fragmentView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    myView.fragmentView.setBackgroundColor(themeColors.getLyricsBackgroundColor());
                }

                // Get the user margins (additional)
                int[] margins = windowFlags.getMargins();

                // Work out top padding for status bar if shown
                int additionalTop = getAdditionalTop();

                // Set the toolbar paddings
                if (myView != null) {
                    myView.myToolbar.setAdditionalTopPadding(additionalTop);
                    myView.myToolbar.setPadding(margins[0] + windowFlags.getMarginToolbarLeft(),
                            margins[1] + additionalTop,
                            margins[2] + windowFlags.getMarginToolbarRight(),
                            0);
                }

                // Now set the paddings to the content page, the song menu and the page button
                // If we are showing the status in the cutout
                int statusPadding = 0;
                int topPadding = 0;
                if (myView != null) {
                    if (settingsOpen) {
                        topPadding = myView.myToolbar.getActionBarHeight(true);
                    } else {
                        topPadding = myView.myToolbar.getActionBarHeight(!myView.myToolbar.getHideActionBar());
                    }
                }
                if (windowFlags.getShowStatusInCutout() && !windowFlags.getIgnoreCutouts()) {
                    statusPadding += windowFlags.getCurrentTopCutoutHeight();
                } else if (windowFlags.getShowStatus()) {
                    statusPadding += windowFlags.getStatusHeight();
                }

                if (topPadding == 0 && windowFlags.getShowStatusInCutout() && windowFlags.getCurrentTopCutoutHeight() > 0) {
                    // We need to add in the statusBar
                    topPadding += statusPadding + margins[1];
                }

                int bottomOfToolbar = getBottomOfToolbar();

                if (myView != null) {
                    myView.fragmentView.setPadding(margins[0], Math.max(margins[1] + additionalTop, Math.max(topPadding, bottomOfToolbar)), margins[2], margins[3]);
                    myView.songMenuLayout.setPadding(margins[0], margins[1] + additionalTop, 0, margins[3]);
                    myView.songMenuLayout.findViewById(R.id.menu_top).setPadding(windowFlags.getMarginToolbarLeft(), 0, 0, 0);
                }
            });
        }
    }

    private int getBottomOfToolbar() {
        int bottomOfToolbar = myView.myAppBarLayout.getBottom() + 1;
        if (myView.myToolbar.getHideActionBar()) {
            if (windowFlags.getShowStatus()) {
                bottomOfToolbar = windowFlags.getStatusHeight();
            } else if (windowFlags.getShowStatusInCutout()) {
                bottomOfToolbar = windowFlags.getCurrentTopCutoutHeight();
            } else {
                bottomOfToolbar = 0;
            }
        }
        return bottomOfToolbar;
    }

    private int getAdditionalTop() {
        int additionalTop = 0;
        if (!windowFlags.getImmersiveMode()) {
            additionalTop += Math.max(windowFlags.getCurrentTopCutoutHeight(), windowFlags.getStatusHeight());
        } else if (windowFlags.getShowStatusInCutout() && !windowFlags.getIgnoreCutouts()) {
            int topCutout = windowFlags.getCurrentTopCutoutHeight();
            int status = windowFlags.getStatusHeight();
            if (topCutout > 0) {
                topCutout = Math.max(topCutout, status);
            }
            additionalTop += topCutout;
        } else if (windowFlags.getShowStatus()) {
            additionalTop += windowFlags.getStatusHeight();
        }

        // Now work out any rounded corner inserts
        if (windowFlags.getHasRoundedCorners() && !windowFlags.getIgnoreRoundedCorners()) {
            additionalTop += windowFlags.getCurrentRoundedTop();
        }
        return additionalTop;
    }

    public int[] getViewMargins() {
        int left = myView.fragmentView.getPaddingLeft();
        int right = myView.fragmentView.getPaddingRight();
        int top = myView.fragmentView.getPaddingTop();
        int bottom = myView.fragmentView.getPaddingBottom();
        return new int[]{left, right, top, bottom};
    }


    private void setupViews() {
        windowFlags = new WindowFlags(this, this.getWindow());
        ViewCompat.setOnApplyWindowInsetsListener(myView.getRoot(), (v, insets) -> {

            // On first call, we get a reference to the windowinsetscompat
            // We need this in the windowFlags class, so set it if it is null
            // Also set the initial screen rotation
            if (windowFlags.getInsetsCompat() == null) {
                windowFlags.setInsetsCompat(insets);
                windowFlags.setCurrentRotation(this.getWindow().getDecorView().getDisplay().getRotation());
            }

            // If we have opened the soft keyboard we can get the height
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (imeVisible) {
                windowFlags.setSoftKeyboardHeight(insets.getInsets(WindowInsetsCompat.Type.ime()).bottom);
            }

            // Moves the view to above the soft keyboard height if required
            if (imeVisible) {
                v.getRootView().setPadding(0, 0, 0, windowFlags.getSoftKeyboardHeight());
            } else {
                v.getRootView().setPadding(0, 0, 0, 0);
            }

            // If the keyboard isn't visible, hide the other flags after a short delay
            // This makes the mode immersive/sticky
            if (!imeVisible) {
                mainLooper.postDelayed(() -> windowFlags.hideOrShowSystemBars(), 1000);
            }
            return insets;
        });

        myView.myToolbar.initialiseToolbar(this, getSupportActionBar());
        initialisePageButtons();
    }

    @Override
    public void initialisePageButtons() {
        pageButtons.setMainFABS(
                myView.actionFAB, myView.pageButtonRight.custom1Button,
                myView.pageButtonRight.custom2Button, myView.pageButtonRight.custom3Button,
                myView.pageButtonRight.custom4Button, myView.pageButtonRight.custom5Button,
                myView.pageButtonRight.custom6Button, myView.pageButtonRight.custom7Button,
                myView.pageButtonRight.custom8Button, myView.pageButtonRight.bottomButtons);
        pageButtons.animatePageButton(false);
    }
    @Override
    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    @Override
    public boolean getFirstRun() {
        return firstRun;
    }

    @Override
    public void initialiseActivity() {
        // This is called after successfully passing BootUpFragment

        // Set up song / set menu tabs
        setUpSongMenuTabs();

        // Set up page buttons
        setListeners();

        // Get the start variables needed for the app
        initialiseStartVariables();

        // Set up battery status
        setUpBatteryMonitor();

        // Set up the page buttons
        updatePageButtonLayout();

        // Set up nearby
        setupNearby();

        // Tell the second screen we are ready
        bootUpCompleted = true;

        mainLooper.post(() -> myView.myAppBarLayout.setVisibility(View.VISIBLE));

    }

    private void initialiseStartVariables() {
            getMyThemeColors().setThemeName(getPreferences().getMyPreferenceString("appTheme", "dark"));
            whichMode = getPreferences().getMyPreferenceString("whichMode", performance);
            // Fix old mode from old profile
            if (whichMode.equals("Presentation")) {
                whichMode = presenter;
            }

            // Song location
            song.setFilename(getPreferences().getMyPreferenceString("songFilename", "Welcome to OpenSongApp"));
            song.setFolder(getPreferences().getMyPreferenceString("songFolder", mainfoldername));

            // ThemeColors
            getMyThemeColors().getDefaultColors();

            // Typefaces
            getMyFonts().setUpAppFonts(mainLooper,mainLooper,mainLooper,mainLooper,mainLooper);
    }

    private void setListeners() {
        myView.actionFAB.setOnClickListener(v -> {
            if (hideActionButtonRunnable == null) {
                setHideActionButtonRunnable();
            }
            myView.actionFAB.removeCallbacks(hideActionButtonRunnable);
            if (pageButtonActive) {
                pageButtonActive = false;
                // Reenable the page button after the animation time
                mainLooper.postDelayed(() -> pageButtonActive = true, pageButtons.getAnimationTime());
                animatePageButtons();
            }
            if (!pageButtonActive && pageButtons.getPageButtonHide() && !whichMode.equals(mode_stage)) {
                myView.actionFAB.postDelayed(hideActionButtonRunnable, 3000);
            }
        });
        myView.actionFAB.setOnLongClickListener(view -> {
            navigateToFragment(deeplink_page_buttons, 0);
            return true;
        });

        // The menu help
        myView.menuTop.menuHelp.setOnClickListener(v -> {
            if (showSetMenu) {
                openDocument(website_menu_set);
            } else {
                openDocument(website_menu_song);
            }
        });
        myView.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            float initialVal = -1.0f;
            boolean decided;

            private void resetVals() {
                decided = false;
                initialVal = -1;
            }

            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (!decided && initialVal == -1.0f) {
                    // Just started, so set the inital value
                    initialVal = slideOffset;
                } else if (!decided && initialVal != -0.0f) {
                    // We have our first value, so now compare.
                    // If we are getting bigger = opening, if smaller, closing
                    if (!whichMode.equals(presenter)) {
                        hideActionButton(slideOffset > initialVal);
                    }
                    menuOpen = slideOffset > initialVal;
                    decided = true;
                }
                // Hide the keyboard
                windowFlags.hideKeyboard();
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                menuOpen = true;
                hideActionButton(true);
                if (setSongMenuFragment() && !songMenuFragment.getHasShownMenuShowcase()) {
                    songMenuFragment.setHasShownMenuShowcase(true);
                    showTutorial("songsetMenu", null);
                }
                // Hide the keyboard
                windowFlags.hideKeyboard();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                menuOpen = false;
                if (!whichMode.equals(presenter)) {
                    hideActionButton(myView.drawerLayout.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_UNLOCKED);
                }
                // Hide the keyboard
                windowFlags.hideKeyboard();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Reset the check vals
                resetVals();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        // If pedalsFragment is open, send the keyCode and event there
        if (pedalsFragment != null && pedalsFragment.isListening()) {
            pedalsFragment.keyDownListener(keyCode);
            return true;
        } else if (pedalsFragment !=null) {
            pedalsFragment.backgroundKeyDown(keyCode, keyEvent);
            return true;
        } else {
            getPedalActions().commonEventDown(keyCode, null);
            if (pedalActions.getButtonNumber(keyCode, null) > 0) {
                return true;
            } else {
                return super.onKeyDown(keyCode, keyEvent);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
        // If pedalsFragment is open, send the keyCode and event there
        if (pedalsFragment != null && pedalsFragment.isListening()) {
            pedalsFragment.commonEventUp();
        } else if (pedalsFragment!=null) {
            pedalsFragment.backgroundKeyUp(keyCode, keyEvent);
        } else if (!settingsOpen) {
            pedalActions.commonEventUp(keyCode, null);
        }
        return super.onKeyUp(keyCode, keyEvent);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent keyEvent) {
        // If pedalsFragment is open, send the keyCode and event there
        if (pedalsFragment != null && pedalsFragment.isListening()) {
            pedalsFragment.commonEventLong();
            return true;
        } else if (pedalsFragment !=null) {
            pedalsFragment.backgroundKeyLongPress(keyCode, keyEvent);
            return true;
        } else if (!settingsOpen) {
            pedalActions.commonEventLong(keyCode, null);
        }
        return super.onKeyLongPress(keyCode, keyEvent);
    }

    @Override
    public void setAlreadyBackPressed(boolean alreadyBackPressed) {
        this.alreadyBackPressed = alreadyBackPressed;
    }

    @SuppressWarnings("deprecation")
    public void interceptBackPressed() {
        if (alreadyBackPressed && !settingsOpen) {
            // Close the app
            confirmedAction(true, "exit", null, null, null, null);
        } else if (settingsOpen) {
            navController.navigateUp();
        } else if (navController != null && navController.getCurrentDestination() != null) {
            alreadyBackPressed = true;
            try {
                int id = Objects.requireNonNull(navController.getCurrentDestination()).getId();
                if (id == R.id.performanceFragment || id == R.id.presenterFragment || id == R.id.setStorageLocationFragment) {
                    displayAreYouSure("exit", exit_confirm, null,
                            Objects.requireNonNull(navController.getCurrentDestination()).getNavigatorName(),
                            navHostFragment, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // This is deprecated, but a last ditch effort!
                super.onBackPressed();
            }
        }
    }


    // Navigation logic
    private void setupNavigation() {
        if (navHostFragment==null || navController==null) {
            navHostFragment =
                    (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                navController = navHostFragment.getNavController();
            }
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            appBarConfiguration = new AppBarConfiguration.Builder(R.id.bootUpFragment,
                    R.id.performanceFragment, R.id.presenterFragment)
                    .setOpenableLayout(myView.drawerLayout)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(myView.myToolbar, navController, appBarConfiguration);
            navController.addOnDestinationChangedListener((navController, navDestination, bundle) -> {
                // IV - We are changing so adjust option menu elements
                if (globalMenuItem != null) {
                    // IV - To smooth teardown, we clear performance mode song detail elements left to right
                    if (!settingsOpen && whichMode.equals(mode_performance)) {
                        myView.myToolbar.hideSongDetails(true);
                    }
                    myView.myToolbar.batteryholderVisibility(false, false);
                    if (getBatteryStatus() != null) {
                        batteryStatus.showBatteryStuff(false);
                    }
                    updateToolbarHelp(null);
                    globalMenuItem.findItem(R.id.mirror_menu_item).setVisible(false);

                    // IV - We set settingsOpen based on the new navDestination
                    settingsOpen = !((navDestination.getId() == R.id.performanceFragment ||
                            navDestination.getId() == R.id.presenterFragment));

                    // IV - To smooth build, we add elements right to left
                    if (settingsOpen) {
                        globalMenuItem.findItem(R.id.settings_menu_item).setIcon(R.drawable.close);
                        // IV - Other elements are added by the called fragment
                    } else {
                        // IV - Top level of menu - song details are added by song load
                        globalMenuItem.findItem(R.id.settings_menu_item).setIcon(R.drawable.settings_outline);
                        updateCastIcon();
                        if (getPreferences().getMyPreferenceBoolean("clockOn", true) ||
                                getPreferences().getMyPreferenceBoolean("batteryTextOn", true) ||
                                getPreferences().getMyPreferenceBoolean("batteryDialOn", true)) {
                            if (myView!=null) {
                                myView.myToolbar.batteryholderVisibility(true, true);
                            }
                            if (getBatteryStatus()!=null) {
                                batteryStatus.showBatteryStuff(true);
                            }
                        }
                        // IV - Song details are added by song load
                        // GE onResuming (open cast and return), not called, so quick check is worthwhile
                        if (!whichMode.equals(getString(R.string.mode_presenter))) {
                            updateToolbar(null);
                        }
                    }
                    myView.myToolbar.requestLayout();
                    myView.myToolbar.setContentInsetStartWithNavigation(0);
                }
            });
        }
    }

    @Override
    public void navigateToFragment(String deepLink, int id) {
        try {
            if (Thread.currentThread() != getMainHandler().getLooper().getThread()) {
                getMainHandler().post(super::onPostResume);
            } else {
                super.onPostResume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Either sent a deeplink string, or a fragment id
        lockDrawer(true);
        closeDrawer(true);  // Only the Performance and Presenter fragments allow this.  Switched on in these fragments
        hideActionButton(true);
        // Stop the autoscroll if running
        if (autoscroll!=null) {
            autoscroll.stopAutoscroll();
        }

        if (deepLink != null && deepLink.equals(deeplink_edit) && songListBuildIndex.getCurrentlyIndexing()) {
            String progressText = "";
            if (songMenuFragment!=null) {
                MaterialTextView progressView = songMenuFragment.getProgressText();
                if (progressView!=null && progressView.getText()!=null) {
                    progressText = " " + progressView.getText().toString();
                }
            }
            if (showToast!=null) {
                showToast.doIt(indexing_string + progressText);
                hideActionButton(false);
            }
        } else {
            runOnUiThread(() -> {
                try {
                    if (navController==null) {
                        setupActionbar();
                        setupNavigation();
                    }
                    if (deepLink != null && navController!=null) {
                        navController.navigate(Uri.parse(deepLink));
                    } else if (navController!=null) {
                        navController.navigate(id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        showActionBar();
    }

    @Override
    public void popTheBackStack(int id, boolean inclusive) {
        navController.popBackStack(id, inclusive);
    }

    @Override
    public void updateFragment(String fragName, Fragment callingFragment, ArrayList<String> arguments) {
        if (fragName != null) {
            // The fragName can also be text that hints at a fragment
            switch (fragName) {
                case "StorageManagementFragment":
                    ((StorageManagementFragment) callingFragment).updateFragment();
                    break;

                case "createNewSong":
                    // User was in song menu dialog, clicked on create, then entered a new file name
                    // Check this was successful (saved as arguments)
                    if (arguments != null && !arguments.isEmpty() && arguments.get(0).equals("success")) {
                        // Write a blank xml file with the song name in it
                        song = processSong.initialiseSong(song.getFolder(), "NEWSONGFILENAME");
                        String newSongText = processSong.getXML(song);
                        // Save the song.  This also calls lollipopCreateFile with 'true' to deleting old
                        getStorageAccess().updateFileActivityLog(TAG + " updateFragment doStringWriteToFile Songs/" + song.getFolder() + "/" + song.getFilename() + " with: " + newSongText);
                        if (storageAccess.doStringWriteToFile("Songs", song.getFolder(), song.getFilename(), newSongText)) {
                            navigateToFragment(null, R.id.editSongFragment);
                        } else {
                            showToast.doIt(error);
                        }
                    }
                    break;

                case "sortSet":
                case "shuffleSet":
                    getThreadPoolExecutor().execute(() -> {
                        if (setMenuFragment!=null) {
                            // Firstly hide the set
                            setMenuFragment.changeVisibility(false);

                            // Sort the set as required
                            if (fragName.equals("sortSet")) {
                                getSetActions().sortSet();
                            } else {
                                getSetActions().shuffleSet();
                            }

                            // Update the set items
                            setMenuFragment.notifyItemRangeChanged(0,getCurrentSet().getCurrentSetSize());

                            // Show the set
                            setMenuFragment.changeVisibility(true);
                        }
                    });
                    break;

                case "set_updateKeys":
                case "set_updateView":
                case "set_updateItem":
                    // User has the set menu open and wants to do something
                    if (setMenuFragment != null) {
                        if (fragName.equals("set_updateView")) {
                            getCurrentSet().updateSetTitleView();
                        } else if (fragName.equals("set_updateKeys")) {
                            setMenuFragment.updateKeys();
                        } else if (arguments != null && !arguments.isEmpty()) {
                            setMenuFragment.updateItem(Integer.parseInt(arguments.get(0)));
                        }
                    }
                    break;

                case "setSelectedSetItem":
                    // We are in the setManageFragment and have clicked on an item, so need to update a view
                    ((SetManageFragment) callingFragment).updateSelectedSet(arguments);
                    break;

                case "linksFragment":
                    // Update the values in the links
                    if (callingFragment != null && callingFragment.isVisible()) {
                        try {
                            ((LinksFragment) callingFragment).setupViews();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "EditSongFragmentTags":
                    ((EditSongFragmentTags) callingFragment).updateValue();
                    break;

                case "confirmed_EditSongFragmentTags":
                    ((EditSongFragmentTags) callingFragment).removeTags(arguments);
                    break;

                case "presenterFragment_showCase":
                    if (presenterValid()) {
                        presenterFragment.showTutorial();
                    }
                    break;

                case "presenterFragment_loadSong":
                    ((PresenterFragment) callingFragment).doSongLoad(getSong().getFolder(), getSong().getFilename());
                    break;

                case "presenterFragmentSettings":
                    ((SecondaryDisplaySettingsFragment) callingFragment).updateBackground();
                    ((SecondaryDisplaySettingsFragment) callingFragment).updateInfoBackground();
                    ((SecondaryDisplaySettingsFragment) callingFragment).updateLogo();
                    break;

                case "presenterFragmentSongSections":
                    if (presenterValid()) {
                        processSong.processSongIntoSections(song, true);
                        presenterFragment.getSongViews();
                        presenterFragment.updateButtons();
                    }
                    break;

                case "themeSetupFragment":
                    ((ThemeSetupFragment) callingFragment).updateColors();
                    ((ThemeSetupFragment) callingFragment).updateButtons();
                    break;

                case "setManageFragment":
                    ((SetManageFragment) callingFragment).doRename();
                    break;

                case "importOnlineFragment":
                    ((ImportOnlineFragment) callingFragment).continueSaving();
                    break;

                case "ImportFileFragment_Set":
                    ((ImportFileFragment) callingFragment).finishImportSet();
                    break;

                case "toggleScale":
                    if (performanceValid()) {
                        performanceFragment.toggleScale();
                    }
                    break;

                case "updateSongMenuSortTitles":
                    if (songMenuFragment != null) {
                        songMenuFragment.updateSongMenuSortTitles();
                    }
                    if (performanceValid()) {
                        performanceFragment.updateInlineSetSortTitles();
                    } else if (presenterValid()) {
                        presenterFragment.updateInlineSetSortTitles();
                    }
                    break;

            }
        }
    }

    public void toggleScale() {
        updateFragment("toggleScale", null, null);
    }

    @Override
    public void navHome() {
        lockDrawer(false);
        if (navController==null) {
            try {
                setupActionbar();
                setupNavigation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (navController!=null && myView!=null) {
            whichMode = preferences.getMyPreferenceString("whichMode", performance);
            if (navController.getCurrentDestination() != null) {
                navController.popBackStack(Objects.requireNonNull(navController.getCurrentDestination()).getId(), true);
            }
            if (whichMode.equals(mode_presenter)) {
                navigateToFragment(deeplink_presenter, 0);
                myView.fragmentView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            } else {
                navigateToFragment(deeplink_performance, 0);
                myView.fragmentView.setBackgroundColor(themeColors.getLyricsBackgroundColor());
            }
        }
    }

    @Override
    public void allowNavigationUp(boolean allow) {
        if (getSupportActionBar() != null) {
            mainLooper.post(() -> {
                getSupportActionBar().setDisplayHomeAsUpEnabled(allow);
                getSupportActionBar().setHomeButtonEnabled(allow);
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public Fragment getCurrentFragment() {
        return navHostFragment.getChildFragmentManager().getFragments().get(0);
    }

    private boolean isCurrentFragment(int fragId) {
        runOnUiThread(() -> {
            try {
                getSupportFragmentManager().executePendingTransactions();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        int currFrag = -1;
        if (navController != null && navController.getCurrentDestination() != null) {
            currFrag = navController.getCurrentDestination().getId();
        }
        return currFrag == fragId;
    }

    // Nearby stuff
    private void setupNearby() {
        // Set up the Nearby connection service
        if (nearbyConnections!=null) {
            nearbyConnections.getUserNickname();

            // Establish a known state for Nearby
            nearbyConnections.turnOffNearby();
        }
    }

    @Override
    public NearbyConnections getNearbyConnections(MainActivityInterface mainActivityInterface) {
        // Return a reference to nearbyConnections
        if (nearbyConnections == null) {
            nearbyConnections = new NearbyConnections(this);
        }
        return nearbyConnections;
    }

    @Override
    public NearbyConnections getNearbyConnections() {
        if (nearbyConnections == null) {
            nearbyConnections = new NearbyConnections(this);
        }
        return nearbyConnections;
    }

    @Override
    public void nearbyEnableConnectionButtons() {
        if (settingsOpen && nearbyConnections.getConnectionsOpen() && nearbyConnectionsFragment != null) {
            try {
                nearbyConnectionsFragment.enableConnectionButtons();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void nearbyUpdateCountdownText(boolean advertise, MaterialButton materialButton) {
        if (settingsOpen && nearbyConnections.getConnectionsOpen() && nearbyConnectionsFragment != null) {
            try {
                nearbyConnectionsFragment.updateCountdownText(advertise, materialButton);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


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
    public void updateConnectionsLog() {
        // Send the command to the Nearby Connections fragment (if it exists!)
        try {
            if (nearbyConnectionsFragment != null && nearbyConnections.getConnectionsOpen()) {
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
    public void doSendPayloadBytes(String infoPayload, boolean clientSend) {
        nearbyConnections.doSendPayloadBytes(infoPayload, clientSend);
    }
    @Override
    public void showNearbyAlertPopUp(String message) {
        if (performanceValid()) {
            performanceFragment.showNearbyAlertPopUp(message);
        }
    }


    // Instructions sent from fragments for MainActivity to deal with
    @Override
    public void hideActionButton(boolean hide) {
        runOnUiThread(() -> {
            if (hide) {
                if (hideActionButtonRunnable != null) {
                    if (myView!=null) {
                        try {
                            myView.actionFAB.removeCallbacks(hideActionButtonRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (myView!=null) {
                    myView.actionFAB.hide();
                    myView.pageButtonRight.bottomButtons.setVisibility(View.GONE);
                    myView.onScreenInfo.getInfo().setVisibility(View.GONE);
                    myView.nextPrevInfo.nextPrevInfoLayout.setVisibility(View.GONE);
                    myView.nextPrevInfo.nextPrevInfoFABLayout.setVisibility(View.GONE);
                }

            } else {
                if (hideActionButtonRunnable == null) {
                    setHideActionButtonRunnable();
                }
                if (getPageButtons().getPageButtonHide() && pageButtons.getPageButtonActivated()) {
                    myView.actionFAB.postDelayed(hideActionButtonRunnable, 3000);
                }
                if (myView!=null) {
                    myView.actionFAB.show();
                    myView.pageButtonRight.bottomButtons.setVisibility(View.VISIBLE);

                    myView.onScreenInfo.getInfo().setVisibility(View.VISIBLE);
                }
                if (displayPrevNext!=null && displayPrevNext.getTextButtons() && (displayPrevNext.getShowPrev() || displayPrevNext.getShowNext())) {
                    myView.nextPrevInfo.nextPrevInfoLayout.setVisibility(View.VISIBLE);
                }
                if (displayPrevNext!=null && !displayPrevNext.getTextButtons() && (displayPrevNext.getShowPrev() || displayPrevNext.getShowNext())) {
                    myView.nextPrevInfo.nextPrevInfoFABLayout.setVisibility(View.VISIBLE);
                }
                // Do this with a delay
                if (myView!=null) {
                    try {
                        getCustomAnimation().fadeActionButton(myView.actionFAB, getMyThemeColors().getPageButtonsSplitAlpha());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void miniPageButton(boolean mini) {
        if (mini) {
            myView.actionFAB.setSize(FloatingActionButton.SIZE_MINI);
        } else {
            myView.actionFAB.setSize(FloatingActionButton.SIZE_NORMAL);
        }
    }

    @Override
    public void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    @Override
    public void updateToolbar(String what) {
        // Null titles are for the default song, author, etc.
        // Otherwise a new title is passed as a string (in a settings menu)
        if (myView != null) {
            mainLooper.post(() -> {
                myView.myToolbar.setActionBar(this, what);
                myView.fragmentView.setTop(myView.myToolbar.getActionBarHeight(settingsOpen || menuOpen));
            });
        }
    }

    @Override
    public void updateToolbarHelp(String webAddress) {
        // If a webAddress is supplied, setup and reveal the help button
        // or for a null or empty web address,hide the help button
        if (globalMenuItem != null) {
            if (!(webAddress == null || webAddress.isEmpty())) {
                // IV - Post the icon and click action after a short delay - testing shows the delay is needed to ensure stability
                globalMenuItem.findItem(R.id.help_menu_item).setVisible(true);
                mainLooper.postDelayed(() -> {
                    if (globalMenuItem != null) {
                        screenHelp = (ImageView) globalMenuItem.findItem(R.id.help_menu_item).getActionView();
                        screenHelp.setOnClickListener(v -> openDocument(webAddress));
                        globalMenuItem.findItem(R.id.help_menu_item).setVisible(true);
                        // For the first run, show the showcase as well
                        if (!isCurrentFragment(R.id.setStorageLocationFragment)) {
                            showCase.singleShowCase(this, screenHelp, null, getString(R.string.help), false, "webHelp");
                        }
                    }
                }, 50);
            } else {
                globalMenuItem.findItem(R.id.help_menu_item).setVisible(false);
            }
        }
    }

    @Override
    public void updateActionBarSettings(String prefName, float floatval, boolean isvisible) {
        // If the user changes settings from the ActionBarSettingsFragment, they get sent here to deal with
        // So let's pass them on to the AppActionBar helper
        myView.myToolbar.updateActionBarSettings(prefName, floatval, isvisible);
    }

    @Override
    public void showTutorial(String what, ArrayList<View> viewsToHighlight) {
        if (globalMenuItem == null) {
            invalidateOptionsMenu();
        }
        initialiseArrayLists();

        String whichShowcase;
        if (!getAlertChecks().getIsShowing()) {
            switch (what) {
                case "performanceView":
                default:
                    whichShowcase = "performanceMode";
                    // Get the hamburger icon and settings if shown
                    View hamburgerView = null;
                    View settingsView = null;
                    for (int z = 0; z < myView.myToolbar.getChildCount(); z++) {
                        if (hamburgerView == null && myView.myToolbar.getChildAt(z).getClass().toString().contains("ImageButton")) {
                            hamburgerView = myView.myToolbar.getChildAt(z);
                        }
                        if (settingsView == null && myView.myToolbar.getChildAt(z).getClass().toString().contains("ActionMenu")) {
                            settingsView = myView.myToolbar.getChildAt(z);
                        }
                    }

                    if (hamburgerView != null) {
                        targets.add(hamburgerView);
                        infos.add(menu_showcase_info);
                        rects.add(false);
                    }
                    if (settingsView != null) {
                        targets.add(settingsView);
                        infos.add(extra_settings);
                        rects.add(false);
                    }

                    // The page button
                    targets.add(myView.actionFAB);
                    infos.add(action_button_info);
                    rects.add(false);
                    break;

                case "presenterSongs":
                    whichShowcase = "presenterSongs";
                    // The hamburger (song/set menu)
                    if (myView.myToolbar.getChildCount() > 2) {
                        final View view = myView.myToolbar.getChildAt(2);
                        targets.add(view);
                        infos.add("Open the menu to view and manage your songs and sets");
                    } else {
                        for (int i = 0; i < myView.myToolbar.getChildCount(); ++i) {
                            final View child = myView.myToolbar.getChildAt(i);
                            if (child != null && child.getClass().toString().contains("ImageView")) {
                                targets.add(child);
                                infos.add("Open the menu to view and manage your songs and sets");
                            }
                        }
                    }
                    targets.add(findViewById(R.id.menuSettings));
                    infos.add(extra_settings);
                    rects.add(false);
                    rects.add(false);
                    // This relies on views having been sent
                    if (viewsToHighlight != null && viewsToHighlight.size() > 6) {
                        targets.add(viewsToHighlight.get(0));
                        infos.add(song_sections);
                        rects.add(true);
                        targets.add(viewsToHighlight.get(1));
                        infos.add(logo_info);
                        rects.add(true);
                        targets.add(viewsToHighlight.get(2));
                        infos.add(blank_screen_info);
                        rects.add(true);
                        targets.add(viewsToHighlight.get(3));
                        infos.add(black_screen_info);
                        rects.add(true);
                        targets.add(viewsToHighlight.get(4));
                        infos.add(project_panic);
                        rects.add(true);
                        targets.add(viewsToHighlight.get(5));
                        infos.add(song_title + "\n" + long_press + " = " + edit_song);
                        rects.add(true);
                        targets.add(viewsToHighlight.get(6));
                        infos.add(song_sections_project);
                        rects.add(true);
                    }
                    break;
                case "songsetMenu":
                    // Initialise the arraylists
                    whichShowcase = "songsetMenu";
                    initialiseArrayLists();
                    targets.add(Objects.requireNonNull(myView.menuTop.tabs.getTabAt(0)).view);
                    targets.add(Objects.requireNonNull(myView.menuTop.tabs.getTabAt(1)).view);
                    targets.add(Objects.requireNonNull(myView.viewpager.findViewById(R.id.actionFAB)));
                    infos.add(menu_song_info);
                    infos.add(menu_set_info);
                    infos.add(add_songs + " / " + song_actions);
                    rects.add(true);
                    rects.add(true);
                    rects.add(false);
                    break;

            }
            getMainHandler().postDelayed(() -> showCase.sequenceShowCase(this, targets, null, infos, rects, whichShowcase), 200);
        }
    }

    private void initialiseArrayLists() {
        targets = new ArrayList<>();
        infos = new ArrayList<>();
        rects = new ArrayList<>();
    }


    // Settings and options menus
    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // GE had to add onResume string update otherwise this call failed if user changed languages
        if (settings.equals(item.toString())) {
            if (settingsOpen) {
                navHome();
            } else {
                navigateToFragment(deeplink_preferences, 0);
                myView.fragmentView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshMenuItems() {
        invalidateOptionsMenu();
    }

    @SuppressLint("PrivateResource")
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.mainactivitymenu, menu);
            ImageView screenMirror = (ImageView) menu.findItem(R.id.mirror_menu_item).getActionView();
            screenMirror.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.cast, getTheme()));
            screenMirror.setOnClickListener(view -> {
                if (!getShowCase().singleShowCase(this, screenMirror, null, cast_info_string, true, "castInfo")) {
                    try {
                        startActivity(new Intent("android.settings.WIFI_DISPLAY_SETTINGS"));
                    } catch (ActivityNotFoundException e) {
                        Log.d(TAG, "android.settings.WIFI_DISPLAY_SETTINGS not an option");
                        try {
                            startActivity(new Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG"));
                        } catch (Exception e2) {
                            Log.d(TAG, "com.samsung.wfd.LAUNCH_WFD_PICKER_DLG not an option");
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    startActivity(new Intent(Settings.ACTION_CAST_SETTINGS));
                                } else {
                                    startActivity(new Intent("android.settings.CAST_SETTINGS"));
                                }
                            } catch (Exception e3) {
                                showToast.doIt(error);
                            }
                        }
                    }
                }
            });
            screenHelp = (ImageView) menu.findItem(R.id.help_menu_item).getActionView();
            screenHelp.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.help_outline, getTheme()));
            globalMenuItem = menu;
            // IV - Set 'settings'/'close' icon is set depending on settingOpen
            globalMenuItem.findItem(R.id.settings_menu_item).setIcon(settingsOpen ? R.drawable.close : R.drawable.settings_outline);
            updateCastIcon();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // The drawers and actionbars
    @Override
    public void lockDrawer(boolean lock) {
        // This is done whenever we have a settings window open
        if (myView != null) {
            myView.drawerLayout.post(()-> {
                if (lock) {
                    myView.drawerLayout.requestDisallowInterceptTouchEvent(true);
                    myView.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                } else {
                    myView.drawerLayout.requestDisallowInterceptTouchEvent(false);
                    myView.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            });
        }
    }

    @Override
    public void closeDrawer(boolean close) {
        if (close) {
            myView.drawerLayout.post(() -> myView.drawerLayout.closeDrawer(GravityCompat.START));
            menuOpen = false;
        } else {
            myView.drawerLayout.post(() -> myView.drawerLayout.openDrawer(GravityCompat.START));
            menuOpen = true;
        }
        // Hide the keyboard
        getWindowFlags().hideKeyboard();

        // Check if we need to hide the actionbar
        showActionBar();
    }

    @Override
    public boolean getMenuOpen() {
        return menuOpen;
    }

    @Override
    public boolean getSettingsOpen() {
        return settingsOpen;
    }

    @Override
    public void setSettingsOpen(boolean settingsOpen) {
        this.settingsOpen = settingsOpen;
    }

    @Override
    public boolean needActionBar() {
        return menuOpen || settingsOpen;
    }

    @Override
    public void scrollOpenMenu(boolean scrollDown) {
        int height = Math.round(getDisplayMetrics()[1] * 0.5f);
        if (!scrollDown) {
            height = -height;
        }
        if (showSetMenu) {
            // Scroll the set menu
            setMenuFragment.scrollMenu(height);
        } else {
            songMenuFragment.scrollMenu(height);
        }
    }

    // The song and set menu
    private void setUpSongMenuTabs() {
        getMainHandler().post(() -> {
            if (viewPagerAdapter == null) {
                viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this.getLifecycle());
                viewPagerAdapter.createFragment(0);
            }
            if (songMenuFragment == null) {
                songMenuFragment = (SongMenuFragment) viewPagerAdapter.menuFragments[0];
            }
            if (setMenuFragment == null) {
                setMenuFragment = (SetMenuFragment) viewPagerAdapter.createFragment(1);
            }
            viewPager = myView.viewpager;
            viewPager.setAdapter(viewPagerAdapter);
            viewPager.setOffscreenPageLimit(1);
            // Disable the swiping gesture
            viewPager.setUserInputEnabled(false);
            TabLayout tabLayout = myView.menuTop.tabs;
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText(song_string);
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.music_note, null));
                        break;
                    case 1:
                        tab.setText(set_string);
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.list_number, null));
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
            myView.menuTop.backButton.setOnClickListener(v -> closeDrawer(true));
        });
    }

    @Override
    public boolean getShowSetMenu() {
        return showSetMenu;
    }

    private boolean setSongMenuFragment() {
        runOnUiThread(() -> {
            if (songMenuFragment != null && viewPager != null) {
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
    public SongMenuFragment getSongMenuFragment() {
        return songMenuFragment;
    }

    @Override
    public int[] getAvailableSizes() {
        if (availableWidth>-1 && availableHeight>-1) {
            return new int[]{availableWidth, availableHeight};
        } else {
            return null;
        }
    }
    @Override
    public void setAvailableSizes(int availableWidth, int availableHeight) {
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
    }

    @Override
    public void chooseMenu(boolean showSetMenu) {
        this.showSetMenu = showSetMenu;
        setSongMenuFragment();
        closeDrawer(myView.drawerLayout.isOpen());
    }

    @Override
    public void indexSongs() {
        getThreadPoolExecutor().execute(() -> {
            try {
                mainLooper.post(() -> {
                    if (showToast != null && search_index_start != null) {
                        showToast.doIt(search_index_start);
                    }
                });
                if (songListBuildIndex != null && songMenuFragment != null && songMenuFragment.getProgressText() != null) {
                    songListBuildIndex.setIndexComplete(false);
                    songListBuildIndex.fullIndex(songMenuFragment.getProgressText());
                } else {
                    // Try again in a short while
                    mainLooper.postDelayed(() -> {
                        if (songListBuildIndex!=null && songMenuFragment != null && songMenuFragment.getProgressText() != null) {
                            songListBuildIndex.setIndexComplete(false);
                            songListBuildIndex.fullIndex(songMenuFragment.getProgressText());
                        }
                    }, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mainLooper.post(() -> {
                try {
                    if (songListBuildIndex != null) {
                        songListBuildIndex.setIndexRequired(false);
                        songListBuildIndex.setIndexComplete(true);
                    }
                    if (showToast != null && search_index_end != null) {
                        showToast.doIt(search_index_end);
                    }
                    updateSongMenu(song);
                    updateFragment("set_updateKeys", null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    public void refreshSong() {
        // Only called after indexing is completed
        if (performanceValid()) {
            performanceFragment.doSongLoad(song.getFolder(), song.getFilename());
        } else if (presenterValid()) {
            presenterFragment.doSongLoad(song.getFolder(), song.getFilename());
        }
    }

    @Override
    public void moveToSongInSongMenu() {
        if (songMenuFragment != null) {
            try {
                songMenuFragment.moveToSongInMenu(song);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateSongMenu(String fragName, Fragment callingFragment, ArrayList<String> arguments) {
        // If the fragName is menuSettingsFragment, we just want to change the alpha index view or sizes
        if (fragName != null && fragName.equals("menuSettingsFragment")) {
            if (songMenuFragment != null) {
                songMenuFragment.changeAlphabeticalLayout();
            }
            if (setMenuFragment !=null) {
                setMenuFragment.notifyItemRangeChanged(0,getCurrentSet().getCurrentSetSize());
            }
        } else if ((rebooted && bootUpCompleted && songMenuFragment != null) || (bootUpCompleted && fragName!=null && fragName.equals("menuSettingsFrag"))) {
            // We have resumed from stale state or changed between title/filename, build the index but from the database
            if (songMenuFragment!=null) {
                songMenuFragment.prepareSearch();
            }
            if (performanceValid()) {
                performanceFragment.updateInlineSetSortTitles();
            } else if (presenterValid()) {
                presenterFragment.updateInlineSetSortTitles();
            }

        } else if (songListBuildIndex != null && songMenuFragment != null) {
            // This is a full rebuild
            // If sent called from another fragment the fragName and callingFragment are used to run an update listener
            songListBuildIndex.setIndexComplete(false);
            // Get all of the files as an array list
            ArrayList<String> songIds = storageAccess.listSongs(false);
            // Write this to text file
            storageAccess.writeSongIDFile(songIds);
            // Try to create the basic databases
            sqLiteHelper.resetDatabase();
            nonOpenSongSQLiteHelper.initialise();
            // Add entries to the database that have songid, folder and filename fields
            // This is the minimum that we need for the song menu.
            // It can be upgraded asynchronously in StageMode/PresenterMode to include author/key
            // Also will later include all the stuff for the search index as well
            sqLiteHelper.insertFast();
            if (fragName != null) {
                //Update the fragment
                updateFragment(fragName, callingFragment, arguments);
            }
            // Now build it properly
            indexSongs();
        }
    }


    // Called after indexing the songs
    @Override
    public void updateSongMenu(Song song) {
        // This only asks for an update from the database
        if (songListBuildIndex.getIndexComplete()) {
            songListBuildIndex.setIndexComplete(true);
            songListBuildIndex.setIndexRequired(false);
        }
        if (setSongMenuFragment() && songMenuFragment != null) {
            songMenuFragment.updateSongMenu(song);
        }
    }

    @Override
    public int getPositionOfSongInMenu() {
        if (songMenuFragment != null) {
            return songMenuFragment.getPositionInSongMenu(song);
        } else {
            return 0;
        }
    }

    @Override
    public Song getSongInMenu(int position) {
        if (position > -1 && songMenuFragment != null && songMenuFragment.getSongsFound() != null && songMenuFragment.getSongsFound().size() > position) {
            return songMenuFragment.getSongsFound().get(position);
        }
        return song;
    }

    @Override
    public ArrayList<Song> getSongsInMenu() {
        if (songMenuFragment != null) {
            return songMenuFragment.getSongsFound();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public WindowFlags getWindowFlags() {
        if (windowFlags==null) {
            windowFlags = new WindowFlags(this,getWindow());
        }
        return windowFlags;
    }

    private boolean performanceValid() {
        return performanceFragment != null && !whichMode.equals(presenter);
    }

    private boolean presenterValid() {
        return presenterFragment != null && whichMode.equals(presenter);
    }

    @Override
    public void notifySetFragment(String what, int position) {
        if (setMenuFragment!=null) {
            switch (what) {
                case "setItemRemoved":
                    setMenuFragment.notifyItemRemoved(position);
                    break;
                case "setItemInserted":
                    setMenuFragment.notifyItemInserted();
                    break;
                case "scrollTo":
                    setMenuFragment.scrollToItem();
                    break;
                case "highlight":
                    setMenuFragment.updateHighlight();
                    break;
                case "clear":
                    setMenuFragment.notifyItemRangeRemoved(0,position);
                    break;
                case "changed":
                    setMenuFragment.notifyItemChanged(position);
                    break;
            }
        }
    }
    @Override
    public void toggleInlineSet() {
        if (performanceValid()) {
            performanceFragment.toggleInlineSet();
        } else if (presenterValid()) {
            presenterFragment.toggleInlineSet();
        }
        loadSong(false);
    }
    @Override
    public void updateInlineSetVisibility() {
        if (performanceValid()) {
            performanceFragment.updateInlineSetVisibility();
        } else if (presenterValid()) {
            presenterFragment.updateInlineSetVisibility();
        }
    }
    @Override
    public void notifyInlineSetInserted() {
        if (performanceValid()) {
            performanceFragment.notifyInlineSetInserted();
        } else if (presenterValid()) {
            presenterFragment.notifyInlineSetInserted();
        }
    }
    @Override
    public void notifyInlineSetInserted(int position) {
        if (performanceValid()) {
            performanceFragment.notifyInlineSetInserted(position);
        } else if (presenterValid()) {
            presenterFragment.notifyInlineSetInserted(position);
        }
    }
    @Override
    public void notifyInlineSetRemoved(int position) {
        if (performanceValid()) {
            performanceFragment.notifyInlineSetRemoved(position);
        } else if (presenterValid()) {
            presenterFragment.notifyInlineSetRemoved(position);
        }
    }
    @Override
    public void notifyToClearInlineSet(int from, int count) {
        if (performanceValid()) {
            performanceFragment.notifyToClearInlineSet(from,count);
        } else if (presenterValid()) {
            presenterFragment.notifyToClearInlineSet(from,count);
        }
    }
    @Override
    public void notifyToInsertAllInlineSet() {
        if (performanceValid()) {
            performanceFragment.notifyToInsertAllInlineSet();
        } else if (presenterValid()) {
            presenterFragment.notifyToInsertAllInlineSet();
        }
    }
    @Override
    public void notifyInlineSetMove(int from, int to) {
        if (performanceValid()) {
            performanceFragment.notifyInlineSetMove(from, to);
        } else if (presenterValid()) {
            presenterFragment.notifyInlineSetMove(from, to);
        }
    }

    @Override
    public void notifyInlineSetChanged(int position) {
        if (performanceValid()) {
            performanceFragment.notifyInlineSetChanged(position);
        } else if (presenterValid()) {
            presenterFragment.notifyInlineSetChanged(position);
        }
    }

    @Override
    public void notifyInlineSetRangeChanged(int from, int count) {
        if (performanceValid()) {
            performanceFragment.notifyInlineSetRangeChanged(from,count);
        } else if (presenterValid()) {
            presenterFragment.notifyInlineSetRangeChanged(from,count);
        }
    }

    @Override
    public void notifyInlineSetHighlight() {
        if (performanceValid()) {
            performanceFragment.notifyInlineSetHighlight();
        } else if (presenterValid()) {
            presenterFragment.notifyInlineSetHighlight();
        }
    }

    @Override
    public void notifyInlineSetScrollToItem() {
        if (performanceValid()) {
            performanceFragment.notifyInlineSetScrollToItem();
        } else if (presenterValid()) {
            presenterFragment.notifyInlineSetScrollToItem();
        }
    }

    @Override
    public int getSongWidth() {
        if (performanceValid()) {
            return performanceFragment.getSongWidth();
        } else {
            return 0;
        }
    }

    // Page buttons
    private void animatePageButtons() {
        float rotation = myView.actionFAB.getRotation();
        pageButtons.animatePageButton(rotation == 0);
    }

    @Override
    public void updatePageButtonLayout() {
        if (myView!=null && pageButtons!=null) {
            // We have changed something about the page buttons (or initialising them
            if (myView.actionFAB.getRotation() != 0) {
                pageButtons.animatePageButton(false);
            }
            pageButtons.updateColors();
            pageButtons.setPageButton(myView.actionFAB, -1, false);
            for (int x = 0; x < pageButtons.getPageButtonNum(); x++) {
                pageButtons.setPageButton(pageButtons.getFAB(x), x, false);
            }
        }
    }

    // Databases
    @Override
    public SQLiteHelper getSQLiteHelper() {
        if (sqLiteHelper == null) {
            sqLiteHelper = new SQLiteHelper(this);
        }
        return sqLiteHelper;
    }

    @Override
    public NonOpenSongSQLiteHelper getNonOpenSongSQLiteHelper() {
        if (nonOpenSongSQLiteHelper == null) {
            nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(this);
        }
        return nonOpenSongSQLiteHelper;
    }

    @Override
    public CommonSQL getCommonSQL() {
        if (commonSQL == null) {
            commonSQL = new CommonSQL(this);
        }
        return commonSQL;
    }


    // Song actions
    @Override
    public void registerMidiAction(boolean actionDown, boolean actionUp, boolean actionLong, String note) {
        // If pedalsFragment is open, send the midiNote and event there
        if (isCurrentFragment(R.id.pedalsFragment) && ((PedalsFragment) getCurrentFragment()).isListening()) {
            if (actionDown) {
                ((PedalsFragment) getCurrentFragment()).midiDownListener(note);
            } else if (actionUp) {
                ((PedalsFragment) getCurrentFragment()).commonEventUp();
            } else if (actionLong) {
                ((PedalsFragment) getCurrentFragment()).commonEventLong();
            }
        } else {
            if (actionDown && !settingsOpen) {
                pedalActions.commonEventDown(-1, note);
            } else if (actionUp && !settingsOpen) {
                pedalActions.commonEventUp(-1, note);
            } else if (actionLong && !settingsOpen) {
                pedalActions.commonEventLong(-1, note);
            }
        }
    }

    @Override
    public void midiStartStopReceived(boolean start) {
        if (beatBuddy.getMetronomeSyncWithBeatBuddy() && midi.getMidiDevice()!=null) {
            if (start && metronome.metronomeValid() && !metronome.getIsRunning()) {
                metronome.startMetronome();
            } else if (!start) {
                metronome.stopMetronome();
            }
        }
    }

    @Override
    public Midi getMidi() {
        if (midi == null) {
            midi = new Midi(this);
        }
        return midi;
    }

    @Override
    public Aeros getAeros() {
        if (aeros == null) {
            aeros = new Aeros(this);
        }
        return aeros;
    }

    @Override
    public BeatBuddy getBeatBuddy() {
        if (beatBuddy == null) {
            beatBuddy = new BeatBuddy(this);
        }
        return beatBuddy;
    }

    @Override
    public Drummer getDrummer() {
        if (drummer == null) {
            drummer = new Drummer(this);
        }
        return drummer;
    }

    // Sticky notes
    @Override
    public void showSticky(boolean forceshow, boolean hide) {
        // Try to show the sticky note
        if (performanceValid()) {
            performanceFragment.dealWithStickyNotes(forceshow, hide);
        }
    }

    public void showAbc(boolean forceShow, boolean hide) {
        // Try to show the abc score
        if (performanceValid()) {
            performanceFragment.dealWithAbc(forceShow, hide);
        }
    }

    // Highlighter
    @Override
    public void toggleHighlighter() {
        // Try to show the highlighter
        if (performanceValid()) {
            performanceFragment.toggleHighlighter();
        }
    }

    // Metronome
    @Override
    public void metronomeToggle() {
        // Get the song values for the metronome if any
        metronome.setSongValues();
        if (metronome.metronomeValid()) {
            // Toggle the start or stop
            if (!metronome.getIsRunning()) {
                metronome.startMetronome();
            } else {
                metronome.stopMetronome();
            }
        } else {
            // Open up the metronome settings
            navigateToFragment(deeplink_metronome, 0);
        }
    }


    // CCLI
    @Override
    public CCLILog getCCLILog() {
        if (ccliLog == null) {
            ccliLog = new CCLILog(this);
        }
        return ccliLog;
    }

    // Capo
    @Override
    public void dealWithCapo() {
        // This checks for song capo and if capo chords are shown
        myView.onScreenInfo.dealWithCapo(this, this);
    }

    @Override
    public void updateOnScreenInfo(String what) {
        switch (what) {
            case "alpha":
                myView.onScreenInfo.updateAlpha(this, this);
                break;
            case "showhide":
                if (hideActionButtonRunnable == null) {
                    setHideActionButtonRunnable();
                }
                myView.actionFAB.removeCallbacks(hideActionButtonRunnable);
                if (whichMode.equals(mode_stage) && !settingsOpen) {
                    myView.actionFAB.show();
                } else {
                    myView.onScreenInfo.showHideViews(this);
                    if (pageButtons.getPageButtonHide() && !pageButtons.getPageButtonActivated()) {
                        myView.actionFAB.postDelayed(() -> myView.actionFAB.show(), 50);
                        myView.actionFAB.postDelayed(hideActionButtonRunnable, 3000);
                    }
                }
                break;
            case "setpreferences":
                myView.onScreenInfo.setPreferences(this, this);
                break;
            case "capoHide":
                myView.onScreenInfo.showCapo(false);
                break;
            case "setblankScreenUnChecked":
                if (presenterFragment != null) {
                    presenterFragment.setBlankScreenUnChecked();
                }
                getPresenterSettings().setBlankscreenOn(false);
                break;
        }
    }

    private void setHideActionButtonRunnable() {
        hideActionButtonRunnable = () -> {
            if (!pageButtons.getPageButtonActivated()) {
                myView.actionFAB.hide();
            }
        };
    }

    // Song processing
    @Override
    public ExtendedFloatingActionButton getSaveButton() {
        if (editSongFragment != null) {
            return editSongFragment.getSaveButton();
        } else {
            return null;
        }
    }

    // The getters for references to the helper classes also needed in fragments
    @Override
    public StorageAccess getStorageAccess() {
        if (storageAccess == null) {
            storageAccess = new StorageAccess(this);
        }
        return storageAccess;
    }

    @Override
    public Preferences getPreferences() {
        if (preferences == null) {
            preferences = new Preferences(this);
        }
        return preferences;
    }

    @Override
    public AppPermissions getAppPermissions() {
        if (appPermissions == null) {
            appPermissions = new AppPermissions(this);
        }
        return appPermissions;
    }

    @Override
    public MyFonts getMyFonts() {
        if (myFonts == null) {
            myFonts = new MyFonts(this);
        }
        return myFonts;
    }

    @Override
    public ThemeColors getMyThemeColors() {
        if (themeColors == null) {
            themeColors = new ThemeColors(this);
        }
        return themeColors;
    }

    @Override
    public ExportActions getExportActions() {
        if (exportActions == null) {
            exportActions = new ExportActions(this);
        }
        return exportActions;
    }

    @Override
    public ChordDisplayProcessing getChordDisplayProcessing() {
        if (chordDisplayProcessing == null) {
            chordDisplayProcessing = new ChordDisplayProcessing(this);
        }
        return chordDisplayProcessing;
    }

    @Override
    public ChordDirectory getChordDirectory() {
        if (chordDirectory == null) {
            chordDirectory = new ChordDirectory();
        }
        return chordDirectory;
    }

    @Override
    public ConvertChoPro getConvertChoPro() {
        if (convertChoPro == null) {
            convertChoPro = new ConvertChoPro(this);
        }
        return convertChoPro;
    }

    @Override
    public ConvertOnSong getConvertOnSong() {
        if (convertOnSong == null) {
            convertOnSong = new ConvertOnSong(this);
        }
        return convertOnSong;
    }
    @Override
    public ConvertWord getConvertWord() {
        if (convertWord == null) {
            convertWord = new ConvertWord(this);
        }
        return convertWord;
    }

    @Override
    public ConvertTextSong getConvertTextSong() {
        if (convertTextSong == null) {
            convertTextSong = new ConvertTextSong(this);
        }
        return convertTextSong;
    }

    @Override
    public ProcessSong getProcessSong() {
        if (processSong == null) {
            processSong = new ProcessSong(this);
        }
        return processSong;
    }

    @Override
    public Song getSong() {
        if (song == null) {
            song = new Song();
        }
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
    public PrepareFormats getPrepareFormats() {
        if (prepareFormats == null) {
            prepareFormats = new PrepareFormats(this);
        }
        return prepareFormats;
    }

    @Override
    public TimeTools getTimeTools() {
        if (timeTools == null) {
            timeTools = new TimeTools();
        }
        return timeTools;
    }

    @Override
    public DisplayPrevNext getDisplayPrevNext() {
        if (displayPrevNext == null) {
            displayPrevNext = new DisplayPrevNext(this, myView.nextPrevInfo.nextPrev,
                    myView.nextPrevInfo.prevButton, myView.nextPrevInfo.nextButton,
                    myView.nextPrevInfo.prevButtonFAB, myView.nextPrevInfo.nextButtonFAB);
        }
        return displayPrevNext;
    }

    @Override
    public FragmentManager getMyFragmentManager() {
        return getSupportFragmentManager();
    }

    @Override
    public Bible getBible() {
        if (bible == null) {
            bible = new Bible(this);
        }
        return bible;
    }

    @Override
    public CustomSlide getCustomSlide() {
        if (customSlide == null) {
            customSlide = new CustomSlide(this);
        }
        return customSlide;
    }

    @Override
    public PresenterSettings getPresenterSettings() {
        if (presenterSettings == null) {
            presenterSettings = new PresenterSettings(this);
        }
        return presenterSettings;
    }

    @Override
    public void doSongLoad(String folder, String filename, boolean closeDrawer) {
        // IV - Close an open drawer and start song load after animate out
        int delay = 0;
        if (getMenuOpen() && closeDrawer) {
            closeDrawer(true);
            delay = 100;
        }
        mainLooper.postDelayed(() -> {
            if (whichMode.equals(presenter)) {
                if (presenterValid()) {
                    presenterFragment.doSongLoad(folder, filename);
                } else {
                    navigateToFragment(null, R.id.presenterFragment);
                }
            } else {
                if (performanceValid()) {
                    performanceFragment.doSongLoad(folder, filename);
                } else {
                    navigateToFragment(null, R.id.performanceFragment);
                }
            }
        }, delay);
    }

    @Override
    public void loadSongFromSet(int position) {
        getThreadPoolExecutor().execute(() -> {
            if (position >= currentSet.getIndexSongInSet()) {
                displayPrevNext.setSwipeDirection("R2L");
            } else {
                displayPrevNext.setSwipeDirection("L2R");
            }

            if (getCurrentSet().getCurrentSetSize() > position) {
                // Update the index in the set
                // Remove highlighting from the old position
                currentSet.setIndexSongInSet(position);
                if (setMenuFragment!=null) {
                    setMenuFragment.updateHighlight();
                }

                // Get the set item
                SetItemInfo setItemInfo = getCurrentSet().getSetItemInfo(position);
                String setFolder = setItemInfo.songfolder;
                String setFilename = setItemInfo.songfilename;
                String setKey = setItemInfo.songkey;
                Uri setUri = getStorageAccess().getUriForItem("Songs",setFolder,setFilename);

                // If we are viewing a set item with a temp key change, we will need these variables
                String[] bits = getSetActions().getPreVariationFolderFilename(setItemInfo);
                String originalFolder = bits[0];
                String originalFilename = bits[1];
                String originalKey = null;
                Uri originalUri = getStorageAccess().getUriForItem("Songs",originalFolder,originalFilename);

                // Determine if this is a variation file based on the filename
                boolean isNormalVariation = getSetActions().getIsNormalVariation(setFolder, setFilename);

                // Create a null/empty song object in case we need to load it to get the key or transpose
                Song quickSong = null;

                // Get the key of the song from the file
                if (storageAccess.isSpecificFileExtension("imageorpdf",setFilename)) {
                    // This is a pdf, we query the persistent database
                    originalKey = nonOpenSongSQLiteHelper.getKey(setFolder,setFilename);
                } else if (isNormalVariation) {
                    if (storageAccess.uriExists(setUri)) {
                        // We are a variation and the file already exists.
                        // We can get the key from the variation file
                        Log.d(TAG,"Variation file already exists, so load that at:"+setFolder+" / "+setFilename);
                        quickSong = new Song();
                        quickSong.setFolder(setFolder);
                        quickSong.setFilename(setFilename);
                    } else if (storageAccess.uriExists(originalUri)) {
                        // The variation file doesn't exist yet
                        // We can get the original file
                        Log.d(TAG,"variation file doesn't exist, so load the original:"+setFolder+" / "+setFilename);
                        quickSong = new Song();
                        quickSong.setFolder(originalFolder);
                        quickSong.setFilename(originalFilename);
                    }
                    if (quickSong!=null && quickSong.getFilename()!=null &&
                            !quickSong.getFilename().isEmpty()) {
                        quickSong = getLoadSong().doLoadSong(quickSong,false);
                        originalKey = quickSong.getKey();
                    }
                } else {
                    Log.d(TAG,"Just a normal song (or a key variation), get from the database");
                    originalKey = sqLiteHelper.getKey(setFolder, setFilename);
                }

                boolean isKeyVariation = setKey!=null && originalKey!=null && !setKey.isEmpty() &&
                                !originalKey.isEmpty() && !setKey.equals(originalKey);

                if (isKeyVariation) {
                    // Could be just a key variation, or a standard variation needing adjusted

                    boolean needToTranspose = false;
                    Uri targetUri;
                    String targetFolder = "Variations";
                    String targetSubfolder = "";
                    String targetFilename;

                    if (isNormalVariation) {
                        // We must already have the variation file, so we can edit directly
                        Log.d(TAG, "Normal variation that needs to be transposed (file already exists)");
                        needToTranspose = true;
                        targetFilename = setFilename;
                        targetUri = getStorageAccess().getUriForItem("Variations", "", setFilename);

                    } else {
                        // Look for an already created key Variation file so we don't need to do it again
                        targetFilename = originalFolder + "/" + originalFilename + getSetActions().getKeyTextInFilename() + setKey;
                        targetFilename = targetFilename.replace("//", "/").replace("/", "_");
                        targetUri = getStorageAccess().getUriForItem("Variations", "_cache", targetFilename);
                        targetSubfolder = "_cache";

                        if (!getStorageAccess().uriExists(targetUri)) {
                            needToTranspose = true;
                        }

                        // We adjust the folder and filename on a temporary basis
                        // This isn't used in the set, just in the loading process
                        setFolder = "../Variations/_cache";
                        setFilename = targetFilename;
                    }

                    if (needToTranspose) {
                        // The set has specified a key that is different from our song
                        Log.d(TAG, "Need to transpose...");

                        if (quickSong == null) {
                            // This was a straightforward song (i.e. not a standard variation)
                            // Get the song object from the database
                            if (storageAccess.isSpecificFileExtension("imageorpdf", setFilename)) {
                                quickSong = nonOpenSongSQLiteHelper.getSpecificSong(originalFolder, originalFilename);
                            } else {
                                quickSong = sqLiteHelper.getSpecificSong(originalFolder, originalFilename);
                            }
                        } else if (quickSong.getLyrics() == null || quickSong.getLyrics().isEmpty()) {
                            Log.d(TAG, "Likely a variation file or empty lyrics, import it");
                            quickSong = getLoadSong().doLoadSong(quickSong, false);
                        }

                        // Transpose the lyrics in the song file
                        // Get the number of transpose times
                        int transposeTimes = transpose.getTransposeTimes(originalKey, setKey);

                        // Why transpose up 11 times, when you can just transpose down once.
                        // Giving the option as it makes it easier for the user to select new key
                        if (transposeTimes > 6) {
                            // 7>-5  8>-4 9>-3 10>-2 11>-1 12>0
                            transposeTimes = transposeTimes - 12;
                        } else if (transposeTimes < -6) {
                            // -7>5 -8>4 -9>3 -10>2 -11>1 -12>0
                            transposeTimes = 12 + transposeTimes;
                        }

                        String transposeDirection;
                        if (transposeTimes >= 0) {
                            transposeDirection = "+1";
                        } else {
                            transposeDirection = "-1";
                        }

                        transposeTimes = Math.abs(transposeTimes);

                        quickSong.setKey(originalKey); // This will be transposed in the following...
                        quickSong.setLyrics(transpose.doTranspose(quickSong,
                                transposeDirection, transposeTimes, quickSong.getDetectedChordFormat(),
                                quickSong.getDesiredChordFormat()).getLyrics());
                        // Get the song XML
                        String songXML = processSong.getXML(quickSong);

                        // Now we need to write the transposed file
                        // If this is a standard variation (not a key variation)
                        if (!getStorageAccess().uriExists(targetUri)) {
                            // We need to create the new key variation file for writing to
                            getStorageAccess().lollipopCreateFileForOutputStream(false,
                                    targetUri, null, targetFolder,
                                    targetSubfolder, targetFilename);
                        }

                        if (targetUri != null) {
                            // Write the transposed file
                            OutputStream outputStream = getStorageAccess().getOutputStream(targetUri);
                            storageAccess.writeFileFromString(songXML, outputStream);
                        }
                    } else if (!getSetActions().getIsNormalOrKeyVariation(setFolder,setFilename)) {
                        // Load the song in the original key
                        setFolder = originalFolder;
                        setFilename = originalFilename;
                        setItemInfo.songfolder = originalFolder;
                        setItemInfo.songfilename = originalFilename;
                        setItemInfo.songfoldernice = originalFolder;
                    }
                }

                // Now update the song menu filters (remove all but folder)
                if (songMenuFragment!=null) {
                    songMenuFragment.removeFiltersFromLoadSong();
                }

                doSongLoad(setFolder, setFilename, true);
            }
        });
    }

    @Override
    public void checkSetMenuItemHighlighted(int setPosition) {
        // See if we need to force the highlighting of the setItem in the set menu
        // This is called from the MyToolbar
        // Will only do something if the set item isn't already highlighted - normally on boot
        if (setPosition > -1 && setMenuFragment!=null) {
            setMenuFragment.updateHighlight();
        }
    }


    @Override
    public void registerFragment(Fragment frag, String what) {
        if (whichMode != null) {
            switch (what) {
                case "Performance":
                    performanceFragment = (PerformanceFragment) frag;
                    presenterFragment = null;
                    if (whichMode.equals(mode_presenter)) {
                        whichMode = mode_performance;
                    }
                    break;
                case "Presenter":
                    presenterFragment = (PresenterFragment) frag;
                    performanceFragment = null;
                    whichMode = mode_presenter;
                    break;
                case "EditSongFragment":
                    editSongFragment = (EditSongFragment) frag;
                    break;
                case "NearbyConnectionsFragment":
                    nearbyConnectionsFragment = (NearbyConnectionsFragment) frag;
                    break;
                case "PedalsFragment":
                    pedalsFragment = (PedalsFragment) frag;
                    break;
                case "SongMenuFragment":
                    songMenuFragment = (SongMenuFragment) frag;
                    break;
                case "SetMenuFragment":
                    setMenuFragment = (SetMenuFragment) frag;
                    break;
                case "BootUpFragment":
                    bootUpFragment = (BootUpFragment) frag;
                    break;
            }
        } else if (what.equals("BootUpFragment")) {
            bootUpFragment = (BootUpFragment) frag;
        }
    }

    @Override
    public void displayAreYouSure(String what, String action, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song) {
        AreYouSureBottomSheet dialogFragment = new AreYouSureBottomSheet(what, action, arguments, fragName, callingFragment, song);
        dialogFragment.show(this.getSupportFragmentManager(), "areYouSure");
    }

    @Override
    public void confirmedAction(boolean agree, String what, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song) {
        if (agree) {
            boolean result = false;
            boolean allowToast = true;

            switch (what) {
                case "deleteSong":
                    getStorageAccess().updateFileActivityLog(TAG + " confirmedAction deleteFile Songs/" + song.getFolder() + "/" + song.getFilename());
                    result = storageAccess.doDeleteFile("Songs",
                            song.getFolder(), song.getFilename());
                    // Now remove from the SQL database
                    if (song.getFiletype()!=null && (song.getFiletype().equals("PDF") || song.getFiletype().equals("IMG"))) {
                        boolean deleted = nonOpenSongSQLiteHelper.deleteSong(song.getFolder(), song.getFilename());
                        Log.d(TAG, "deleted:" + deleted);
                    }
                    sqLiteHelper.deleteSong(song.getFolder(), song.getFilename());
                    // Set the welcome song
                    song.setFilename("Welcome to OpenSongApp");
                    song.setFolder(mainfoldername);
                    updateSongMenu(song);
                    navHome();
                    break;

                case "ccliDelete":
                    result = ccliLog.createBlankXML();
                    break;

                case "deleteItem":
                    // Folder and subfolder are passed in the arguments.  Blank arguments.get(2) /filenames mean folders
                    getStorageAccess().updateFileActivityLog(TAG + " confirmedAction deleteFile " + arguments.get(0) + "/" + arguments.get(1) + "/" + arguments.get(2));
                    result = storageAccess.doDeleteFile(arguments.get(0), arguments.get(1), arguments.get(2));
                    if (arguments.get(2)!=null && arguments.get(2).isEmpty() && arguments.get(0)!=null && arguments.get(0).equals("Songs") &&
                            (arguments.get(1)==null || arguments.get(1).isEmpty())) {
                        // Emptying the entire songs foler, so need to recreate it on finish.
                        storageAccess.createFolder("Songs", "", "", false);
                    }
                    //Rebuild the song index
                    updateSongMenu(fragName, callingFragment, arguments); // Passing the fragment allows an update to be sent to the calling fragment
                    break;

                case "deleteHighlighter":
                    // Try to send the info back to the highlighter edit fragment
                    try {
                        ((HighlighterEditFragment) callingFragment).doDelete(true);
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
                    getThreadPoolExecutor().execute(() -> {
                        if (setMenuFragment!=null) {
                            // Firstly hide the set
                            setMenuFragment.changeVisibility(false);

                            int count = getCurrentSet().getCurrentSetSize();

                            // Now clear the current set and preferences
                            getSetActions().clearCurrentSet();

                            // Untick the songs in the song menu
                            updateSongList();

                            // Notify the set that we removed items
                            setMenuFragment.notifyItemRangeRemoved(0, count);
                            notifyToClearInlineSet(0,count);

                            // Update the set title
                            getCurrentSet().updateSetTitleView();

                            // Show the set
                            setMenuFragment.changeVisibility(true);
                        }
                    });
                    result = true;
                    break;

                case "saveset":
                    // Overwriting the last loaded set with the current one via bottom sheet
                    // This is only called if we are editing a previously saved set
                    String xml = getSetActions().createSetXML();
                    String setString = getSetActions().getSetAsPreferenceString();
                    result = storageAccess.doStringWriteToFile("Sets", "", currentSet.getSetCurrentLastName(), xml);
                    if (result) {
                        // Update the last edited version (current set already has this)
                        currentSet.setSetCurrentBeforeEdits(setString);
                    }
                    // Update the set title
                    currentSet.updateSetTitleView();
                    break;

                case "removeThemeTag":
                    // We are about to remove tags from songs.  This is done in the EditSong fragment
                    updateFragment("confirmed_" + fragName, callingFragment, arguments);
                    allowToast = false;
                    break;

                case "resetColors":
                    // We will reset the chosen theme colours to app defaults
                    themeColors.resetTheme();
                    themeColors.getDefaultColors();
                    updateFragment(fragName, callingFragment, null);
                    allowToast = false;
                    break;

                case "renameSet":
                case "onlineSongOverwrite":
                case "importSetIntent":
                    // We are renaming a set    or
                    // We extracted an online song, but one with the same name exists already
                    updateFragment(fragName, callingFragment, null);
                    allowToast = false;
                    break;

                case "resetBeatBuddyDatabase":
                    // Reset the BeatBuddy database
                    if (callingFragment != null && callingFragment.isAdded()) {
                        ((BBOptionsFragment) callingFragment).resetDatabase();
                        allowToast = false;
                        break;
                    }
                    break;
                case "cropImage":
                    Uri tempUri = getStorageAccess().getUriForItem("Export","",song.getFilename());
                    Uri songUri = getStorageAccess().getUriForItem("Songs",song.getFolder(),song.getFilename());
                    InputStream inputStream = getStorageAccess().getInputStream(tempUri);
                    OutputStream outputStream = getStorageAccess().getOutputStream(songUri);
                    boolean copied = getStorageAccess().copyFile(inputStream,outputStream);
                    // Copy the cropped image to the original one
                    if (copied) {
                        // Copy was successful, so delete the temp file
                        getStorageAccess().deleteFile(tempUri);
                        result = true;
                        navHome();
                    }
                    break;
            }
            if (allowToast && result && showToast != null && getResources() != null) {
                // Don't show toast for exit, but other successful actions
                showToast.doIt(success);
            } else if (allowToast && showToast != null && getResources() != null) {
                showToast.doIt(error);
            }
        }
    }

    @Override
    public void updateSetList() {
        updateFragment("set_updateView", null, null);
    }

    @Override
    public void updateSongList() {
        // This uses the existing database objects
        if (songMenuFragment != null) {
            try {
                songMenuFragment.setFolders();
                songMenuFragment.refreshSongList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateCheckForThisSong(Song thisSong) {
        songMenuFragment.updateCheckForThisSong(thisSong);
    }

    @Override
    public void toggleAutoscroll() {
        if (autoscroll.getIsPaused()) {
            // This sets to the opposite, so un-paused
            autoscroll.pauseAutoscroll();
        } else if (autoscroll.getIsAutoscrolling()) {
            autoscroll.stopAutoscroll();
        } else {
            if (song.getAutoscrolllength() == null || song.getAutoscrolllength().isEmpty() &&
                    !(getPreferences().getMyPreferenceBoolean("autoscrollUseDefaultTime", true))) {
                performanceGestures.autoscrollSettings();
            } else {
                autoscroll.startAutoscroll();
            }
        }
    }

    @Override
    public void doScrollByProportion(float scrollProportion) {
        if (performanceValid()) {
            performanceFragment.doNearbyScrollBy(scrollProportion);
        }
    }

    @Override
    public void doScrollToProportion(float scrollProportion) {
        if (performanceValid()) {
            performanceFragment.doNearbyScrollTo(scrollProportion);
        }
    }

    @Override
    public Pad getPad() {
        if (pad == null) {
            pad = new Pad(this, myView.onScreenInfo.getPad());
        }
        return pad;
    }

    @Override
    public boolean playPad() {
        // If the pad is playing, stop else start
        if (pad.isPadPlaying()) {
            pad.stopPad();
            return false;
        } else {
            pad.startPad();
            // Showcase if required
            showCase.singleShowCase(this, myView.onScreenInfo.getPad(), okay, pad_playback_info, true, "padPlayback");
            return true;
        }
    }


    @Override
    public void fullIndex() {
        if (songListBuildIndex.getIndexRequired()) {
            if (showToast == null) {
                showToast = new ShowToast(this, myView.getRoot());
            }
            showToast.doIt(search_index_start);
            getThreadPoolExecutor().execute(() -> {
                String outcome = songListBuildIndex.fullIndex(songMenuFragment.getProgressText());
                if (songMenuFragment != null && !songMenuFragment.isDetached()) {
                    try {
                        songMenuFragment.updateSongMenu(song);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mainLooper.post(() -> {
                    if (showToast != null && outcome!=null && !outcome.isEmpty()) {
                        showToast.doIt(outcome.trim());
                    }
                    updateFragment("set_updateKeys", null, null);
                });
            });
        }
    }

    @Override
    public void quickSongMenuBuild() {
        if (storageAccess!=null && sqLiteHelper!=null && nonOpenSongSQLiteHelper!=null) {
            ArrayList<String> songIds = new ArrayList<>();
            try {
                songIds = storageAccess.listSongs(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Write a crude text file (line separated) with the song Ids (folder/file)
            storageAccess.writeSongIDFile(songIds);

            // Non persistent, created from storage at boot (to keep updated) used to references ALL files
            if (songListBuildIndex.getFullIndexRequired()) {
                sqLiteHelper.resetDatabase();
                sqLiteHelper.insertFast();
            } else {
                // Remove existing items that don't match the new songIds
                // If this throws an error, the database is reset
                sqLiteHelper.removeOldSongs(songIds);
            }

            // Persistent containing details of PDF/Image files only.  Pull in to main database at boot
            // Updated each time a file is created, deleted, moved.
            // Also updated when feature data (pad, autoscroll, metronome, etc.) is updated for these files
            nonOpenSongSQLiteHelper.initialise();

            // Add entries to the database that have songid, folder and filename fields
            // This is the minimum that we need for the song menu.
            // It can be upgraded asynchronously in StageMode/PresenterMode to include author/key
            // Also will later include all the stuff for the search index as well

        }
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
    public CommonControls getCommonControls() {
        if (commonControls == null) {
            commonControls = new CommonControls(this);
        }
        return commonControls;
    }
    @Override
    public HotZones getHotZones() {
        if (hotZones == null) {
            hotZones = new HotZones(this);
        }
        return hotZones;
    }
    @Override
    public PedalActions getPedalActions() {
        if (pedalActions == null) {
            pedalActions = new PedalActions(this);
        }
        return pedalActions;
    }

    @Override
    public Gestures getGestures() {
        if (gestures == null) {
            gestures = new Gestures(this);
        }
        return gestures;
    }

    @Override
    public PerformanceGestures getPerformanceGestures() {
        if (performanceGestures == null) {
            performanceGestures = new PerformanceGestures(this);
        }
        return performanceGestures;
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
    public WebDownload getWebDownload() {
        if (webDownload == null) {
            webDownload = new WebDownload();
        }
        return webDownload;
    }

    @Override
    public void chordinatorResult(ImportOnlineFragment importOnlineFragment, String songText) {
        if (importOnlineFragment != null && importOnlineFragment.isAdded()) {
            try {
                if (songText == null) {
                    songText = "";
                }
                songText = convertTextSong.convertText(songText);
                importOnlineFragment.setClipboardText(songText);
                importOnlineFragment.doShowSaveButton(!songText.isEmpty());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ShowToast getShowToast() {
        if (showToast == null) {
            showToast = new ShowToast(this, myView.getRoot());
        }
        return showToast;
    }

    @Override
    public String getMode() {
        if (whichMode == null) {
            whichMode = preferences.getMyPreferenceString("whichMode", performance);
        }
        return whichMode;
    }

    @Override
    public void setMode(String whichMode) {
        this.whichMode = whichMode;
    }


    public FixLocale getFixLocale() {
        if (fixLocale == null) {
            fixLocale = new FixLocale();
        }
        return fixLocale;
    }

    @Override
    public Locale getLocale() {
        if (locale == null && fixLocale!=null) {
            fixLocale.setLocale(this, this);
            locale = fixLocale.getLocale();
        }
        if (locale == null) {
            return Locale.getDefault();
        } else {
            return locale;
        }
    }

    @Override
    public CurrentSet getCurrentSet() {
        if (currentSet == null) {
            currentSet = new CurrentSet(this);
        }
        return currentSet;
    }

    @Override
    public SetActions getSetActions() {
        if (setActions == null) {
            setActions = new SetActions(this);
        }
        return setActions;
    }

    @Override
    public LoadSong getLoadSong() {
        if (loadSong == null) {
            loadSong = new LoadSong(this);
        }
        return loadSong;
    }

    @Override
    public SaveSong getSaveSong() {
        if (saveSong == null) {
            saveSong = new SaveSong(this);
        }
        return saveSong;
    }

    @Override
    public String getWhattodo() {
        if (whattodo == null) {
            whattodo = "";
        }
        return whattodo;
    }

    @Override
    public void setWhattodo(String whattodo) {
        this.whattodo = whattodo;
    }

    @Override
    public PageButtons getPageButtons() {
        if (pageButtons == null) {
            pageButtons = new PageButtons(this);
        }
        return pageButtons;
    }

    @Override
    public Autoscroll getAutoscroll() {
        if (autoscroll == null) {
            autoscroll = new Autoscroll(this, myView.onScreenInfo.getAutoscrollTime(),
                    myView.onScreenInfo.getAutoscrollTotalTime(), myView.onScreenInfo.getAutoscroll());
        }
        return autoscroll;
    }


    @Override
    public Metronome getMetronome() {
        if (metronome == null) {
            metronome = new Metronome(this);
            metronome.initialiseMetronome();
        }
        return metronome;
    }

    @Override
    public SongListBuildIndex getSongListBuildIndex() {
        if (songListBuildIndex == null) {
            songListBuildIndex = new SongListBuildIndex(this);
        }
        return songListBuildIndex;
    }

    @Override
    public CustomAnimation getCustomAnimation() {
        if (customAnimation == null) {
            customAnimation = new CustomAnimation();
        }
        return customAnimation;
    }

    @Override
    public void pdfScrollToPage(int pageNumber) {
        performanceShowSection(pageNumber);
    }

    @Override
    public ShowCase getShowCase() {
        if (showCase == null) {
            showCase = new ShowCase(this);
        }
        return showCase;
    }

    @Override
    public OCR getOCR() {
        if (ocr == null) {
            ocr = new OCR(this);
        }
        return ocr;
    }

    @Override
    public MakePDF getMakePDF() {
        if (makePDF == null) {
            makePDF = new MakePDF(this);
        }
        return makePDF;
    }

    @Override
    public VersionNumber getVersionNumber() {
        if (versionNumber == null) {
            versionNumber = new VersionNumber(this);
        }
        return versionNumber;
    }

    @Override
    public Transpose getTranspose() {
        if (transpose == null) {
            transpose = new Transpose(this);
        }
        return transpose;
    }

    @Override
    public MyToolbar getToolbar() {
        return myView.myToolbar;
    }

    @Override
    public Swipes getSwipes() {
        if (swipes == null) {
            swipes = new Swipes(this);
        }
        return swipes;
    }

    @Override
    public void setScreenshotFile(Bitmap bitmap) {
        if (screenshotFile==null) {
            getScreenshotFile();
        }
        if (bitmap==null) {
            Log.d(TAG,"Deleting old screenshot:"+screenshotFile.delete());
        } else {
            try {
                FileOutputStream out = new FileOutputStream(screenshotFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public File getScreenshotFile() {
        if (screenshotFile==null) {
            screenshotFile = getStorageAccess().getAppSpecificFile("", "", "screenshot.png");
        }
        return screenshotFile;
    }

    @Override
    public boolean validScreenShotFile() {
        if (screenshotFile==null) {
            getScreenshotFile();
        }
        return screenshotFile.exists() && screenshotFile.length()>0;
    }

    @Override
    public ABCNotation getAbcNotation() {
        if (abcNotation == null) {
            abcNotation = new ABCNotation(this);
        }
        return abcNotation;
    }

    @Override
    public AlertChecks getAlertChecks() {
        if (alertChecks == null) {
            alertChecks = new AlertChecks(this);
        }
        return alertChecks;
    }

    @Override
    public DrawNotes getDrawNotes() {
        return drawNotes;
    }

    @Override
    public void setDrawNotes(DrawNotes view) {
        drawNotes = view;
    }

    @Override
    public ProfileActions getProfileActions() {
        if (profileActions == null) {
            profileActions = new ProfileActions(this);
        }
        return profileActions;
    }

    @Override
    public CheckInternet getCheckInternet() {
        if (checkInternet == null) {
            checkInternet = new CheckInternet();
        }
        return checkInternet;
    }

    @Override
    public void isWebConnected(Fragment fragment, int fragId, boolean isConnected) {
        // This is the result of an internet connection check
        if (fragment != null) {
            try {
                if (fragId == R.id.fontSetupFragment) {
                    ((FontSetupFragment) fragment).isConnected(isConnected);
                } else if (fragId == R.id.importOnlineFragment) {
                    ((ImportOnlineFragment) fragment).isConnected(isConnected);
                } else if (fragId == R.id.importOSBFragment) {
                    ((ImportOptionsFragment) fragment).isConnected(isConnected);
                } else if (fragId == R.id.forumFragment) {
                    ((ForumFragment) fragment).isConnected(isConnected);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void songSelectDownload(Fragment fragment, int fragId, Uri uri, String filename) {
        if (fragment != null && fragId == R.id.importOnlineFragment) {
            try {
                ((ImportOnlineFragment) fragment).finishedDownload(uri, filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public WebServer getWebServer() {
        if (webServer==null) {
            webServer = new WebServer();
            webServer.initialiseVariables(this);
        }
        return webServer;
    }

    @Override
    public void openDocument(String location) {
        // Most locations are passed in from the string.xml file.  They are listed under website_xxx
        // Otherwise they are created on the fly (for link files, importing songs, etc).
        if (location != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (location.startsWith("http")) {
                    if (!location.contains("https://www.google.com/search?q=")) {
                        // Not searching, so just display the webpage in the default browser
                        intent.setData(Uri.parse(location));
                    } else {
                        // Searching.  May not be using Google/Chrome, so use default search engine
                        // Replace the location with the search phrase (strip out the google.com/search?q= bit)
                        intent = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, location.replace("https://www.google.com/search?q=",""));
                    }
                } else {
                    String mimeType = null;
                    if (location.contains(".")) {
                        String extension = location.substring(location.lastIndexOf(".") + 1);
                        MimeTypeMap myMime = MimeTypeMap.getSingleton();
                        mimeType = myMime.getMimeTypeFromExtension(extension);
                    }
                    if (mimeType == null) {
                        mimeType = "*/*";
                    }
                    Uri uri = Uri.parse(location);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, mimeType);
                }

                startActivity(intent);
            } catch (ActivityNotFoundException nf) {
                // No suitable application to open the document
                showToast.doIt(no_suitable_application);
                nf.printStackTrace();

            } catch (Exception e) {
                // Probably no browser installed or no internet permission given.
                e.printStackTrace();
            }
        }
    }


    @Override
    public void setSectionViews(ArrayList<View> views) {
        if (views == null) {
            // Reset the views and their sizes
            sectionViews = null;
            sectionViews = new ArrayList<>();
            sectionWidths = null;
            sectionWidths = new ArrayList<>();
            sectionHeights = null;
            sectionHeights = new ArrayList<>();
            sectionColors = null;
            sectionColors = new ArrayList<>();
        } else {
            sectionViews = views;
        }
    }

    @Override
    public ArrayList<View> getSectionViews() {
        return sectionViews;
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
    public ArrayList<Integer> getSectionColors() {
        return sectionColors;
    }

    @Override
    public void addSectionSize(int position, int width, int height) {
        if (sectionWidths == null) {
            sectionWidths = new ArrayList<>();
        }
        if (sectionHeights == null) {
            sectionHeights = new ArrayList<>();
        }
        sectionWidths.add(position, width);
        sectionHeights.add(position, height);
    }

    @Override
    public void setSectionColors(ArrayList<Integer> colors) {
        sectionColors = colors;
    }

    @Override
    public void setSongSheetTitleLayout(LinearLayout linearLayout) {
        if (songSheetTitleLayout == null) {
            initialiseSongSheetTitleLayout();
        }
        if (linearLayout == null) {
            // Remove the views
            songSheetTitleLayout.removeAllViews();
        } else {
            songSheetTitleLayout.addView(linearLayout);
        }
    }

    @Override
    public LinearLayout getSongSheetTitleLayout() {
        if (songSheetTitleLayout == null) {
            initialiseSongSheetTitleLayout();
        }
        return songSheetTitleLayout;
    }

    private void initialiseSongSheetTitleLayout() {
        songSheetTitleLayout = new LinearLayout(this);
        songSheetTitleLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        songSheetTitleLayout.setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    public SongSheetHeaders getSongSheetHeaders() {
        if (songSheetHeaders == null) {
            songSheetHeaders = new SongSheetHeaders(this);
        }
        return songSheetHeaders;
    }

    @Override
    public void enableSwipe(String which, boolean canSwipe) {
        if (which.equals("edit") && editSongFragment != null) {
            editSongFragment.enableSwipe(canSwipe);
        }
    }

    @Override
    public ArrayList<Song> getSongsFound(String whichMenu) {
        switch (whichMenu) {
            case "song":
                if (songMenuFragment != null) {
                    try {
                        return songMenuFragment.getSongsFound();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "set":
                try {
                    return currentSet.getSetSongObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return new ArrayList<>();
    }

    @Override
    public void updateSizes(int width, int height) {
        if (performanceValid()) {
            performanceFragment.updateSizes(width, height);
        }
    }

    @Override
    public void selectSection(int i) {
        // Only do this if we are not in a settings fragment
        if (!settingsOpen) {
            if (presenterValid()) {
                presenterFragment.selectSection(i);
            } else if (performanceValid()) {
                performanceFragment.selectSection(i);
                performanceShowSection(i);
            }
        } else {
            nearbyConnections.setPendingSection(i);
        }
    }

    @Override
    public void loadSong(boolean updateSongMenu) {
        // If we are not in a settings window, load the song
        // Otherwise it will happen when the user closes the settings fragments
        if (!settingsOpen) {
            doSongLoad(song.getFolder(), song.getFilename(), true);
            // Update the song menu filters to match the incoming song if required
            if (songMenuFragment!=null && updateSongMenu) {
                songMenuFragment.removeFiltersFromLoadSong();
            }
            if (setMenuFragment!=null && updateSongMenu) {
                setMenuFragment.updateHighlight();
            }
        }
    }

    @Override
    public void goToPreviousPage() {
        // Received from nearbyAction
        if (song.getFiletype().equals("PDF") && song.getPdfPageCurrent() > 0) {
            if (presenterValid()) {
                presenterFragment.selectSection(song.getPdfPageCurrent() - 1);
            } else if (performanceValid()) {
                performanceFragment.selectSection(song.getPdfPageCount() - 1);
            }
        }
    }

    @Override
    public void goToNextPage() {
        // Received from nearbyAction
        if (song.getFiletype().equals("PDF") && song.getPdfPageCount() > 0 &&
                song.getPdfPageCurrent() < song.getPdfPageCount() - 1) {
            if (presenterValid()) {
                presenterFragment.selectSection(song.getPdfPageCurrent() + 1);
            } else if (performanceValid()) {
                performanceFragment.selectSection(song.getPdfPageCount() + 1);
            }
        }
    }

    // Sent from bottom sheet and requires an update in calling fragment
    @Override
    public void updateValue(Fragment fragment, String fragname, String which, String value) {
        // This takes the info from the TextInputBottomSheet and passes back to the calling fragment
        if (fragment != null) {
            try {
                switch (fragname) {
                    case "SettingsCCLI":
                        ((SettingsCCLI) fragment).updateValue(which, value);
                        break;
                    case "NearbyConnectionsFragment":
                        ((NearbyConnectionsFragment) fragment).updateValue(which, value);
                        break;
                    case "SetManageFragment":
                        ((SetManageFragment) fragment).updateValue(value);
                        break;
                    case "EditSongFragmentMain":
                        ((EditSongFragmentMain) fragment).updateValue(value);
                        break;
                    case "CustomChordsFragment":
                        ((CustomChordsFragment) fragment).updateValue(value);
                        break;
                    case "SongSectionsFragment":
                        ((SongSectionsFragment) fragment).updateValue(value);
                        break;
                    case "songActionsMenuFragment":
                        ((SongActionsMenuFragment) fragment).doDuplicate(value);
                        break;
                    case "CustomSlideFragment":
                        ((CustomSlideFragment) fragment).getReusable(value);
                        break;
                    case "StorageManagementFragment":
                        if (whattodo.equals("newfolder")) {
                            ((StorageManagementFragment) fragment).createNewFolder(value);
                        } else if (whattodo.equals("renamefolder")) {
                            ((StorageManagementFragment) fragment).renameFolder(value);
                        }
                        break;
                    case "BulkTagAssignFragment":
                        ((BulkTagAssignFragment) fragment).addNewTag(value);
                        break;
                    case "BulkTagAssignFragmentRename":
                        ((BulkTagAssignFragment) fragment).renameTag(value);
                        break;
                    case "NearbyMessages":
                        ((NearbyConnectionsFragment) fragment).updateMessage(value);
                        break;
                    case "MidiActionBS":
                        ((MidiActionBottomSheet) fragment).updateMessage(value);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getSwipeValues(int minDistance, int minHeight, int minTime) {
        if (isCurrentFragment(R.id.swipeFragment)) {
            try {
                ((SwipeFragment) getCurrentFragment()).getSwipeValues(minDistance, minHeight, minTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (bootUpCompleted) {
            try {
                // Get the language
                fixLocale.setLocale(this, this);

                // Save a variable that we have rotated the screen.
                // The media player will look for this.  If found, it won't restart when the song loads
                pad.setOrientationChanged(pad.getCurrentOrientation() != newConfig.orientation);
                // If orientation has changed, we need to reload the song to get it resized.
                // Only do this if we are not in a settings menu though!
                if (!settingsOpen && pad.getOrientationChanged()) {
                    // Set the current orientation
                    pad.setCurrentOrientation(newConfig.orientation);
                    pageButtons.requestLayout();
                    // IV - After a short delay - to allow screen layout to stabilise
                    mainLooper.postDelayed(() -> {
                        // IV - Following testing - Margins update requires 2 calls on orientation change!
                        updateMargins();
                        doSongLoad(song.getFolder(), song.getFilename(), true);
                    }, 50);
                }
                if (!settingsOpen) {
                    if (performanceValid()) {
                        performanceFragment.orientationInlineSet(newConfig.orientation);
                    } else if (presenterValid()) {
                        presenterFragment.orientationInlineSet(newConfig.orientation);
                    }
                }
                windowFlags.setCurrentRotation(this.getWindow().getDecorView().getDisplay().getRotation());
            } catch (Exception e) {
                e.printStackTrace();
            }
            closeDrawer(true);
            // IV - Following testing - Margins update requires 2 calls on orientation change!
            updateMargins();
        }
    }

    @Override
    public int getOrientation() {
        try {
            return getResources().getConfiguration().orientation;
        } catch (Exception e) {
            e.printStackTrace();
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("bootUpCompleted", bootUpCompleted);
        //outState.putInt(KEY_GENERATED_VIEW_ID, ViewCompat.generateViewId());
        if (songListBuildIndex!=null) {
            outState.putBoolean("indexComplete", songListBuildIndex.getIndexComplete());
        } else {
            outState.putBoolean("indexComplete",false);
        }

        // If we were using nearby, keep a reference of known devices and a call to restart it
        if (getNearbyConnections().getUsingNearby() && getNearbyConnections().hasValidConnections()) {
            // Note we were using
            outState.putBoolean("usingNearby", nearbyConnections.getUsingNearby());
            // Are we a host or client?
            outState.putBoolean("isHost", nearbyConnections.getIsHost());
            // What connections did we have
            outState.putStringArrayList("discoveredEndpoints", nearbyConnections.getDiscoveredEndpoints());
            outState.putStringArrayList("connectedEndpoints", nearbyConnections.getConnectedEndpoints());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myView == null) {
            // Something is wrong - restart the app
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        } else if (bootUpCompleted) {
            // Just check the actionbar and navigation work
            // Set up the action bar
            setupActionbar();

            // Set up navigation
            setupNavigation();

            // Check displays
            checkDisplays();

            // Prepare the themes
            getMyThemeColors();

            // Prepapre the metronome
            getMetronome();

            if (metronome!=null) {
                metronome.initialiseMetronome();
            }
        }
        try {
            // If the user changed language, the strings need updated
            prepareStrings();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    @Override
    protected void onPause() {
        super.onPause();
        // Copy the persistent database from app storage to user storage
        if (nonOpenSongSQLiteHelper!=null) {
            nonOpenSongSQLiteHelper.copyUserDatabase();
        }
        if (autoscroll != null) {
            autoscroll.stopTimers();
        }
        if (metronome != null) {
            metronome.stopTimers(true);
        }
    }

    @Override
    protected void onStop() {
        // Stop pad timers
        if (pad != null) {
            pad.stopPad();
        }
        // Stop metronome timers
        if (metronome != null) {
            metronome.stopTimers(true);
        }
        // Stop autoscroll timers
        if (autoscroll != null) {
            autoscroll.stopTimers();
        }

        // Clear out any temporarily copied intent files
        File tempLoc = getStorageAccess().getAppSpecificFile("Import","","");
        //File tempLoc = new File(getExternalFilesDir("Import"), "Intent");
        File[] files = tempLoc.listFiles();
        if (files != null) {
            for (File file : files) {
                Log.d(TAG, "Deleted temp import file " + file + ":" + file.delete());
            }
        }

        if (showToast != null) {
            showToast.kill();
            showToast = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (webServer!=null) {
            webServer.stop();
        }
        if (showToast != null) {
            showToast.kill();
        }

        // Turn off nearby
        if (nearbyConnections!=null) {
            nearbyConnections.turnOffNearby();
        }

        if (metronome!=null) {
            metronome.releaseSoundPool();
        }

        // Reset the dealt with intent
        try {
            if (preferences!=null) {
                preferences.setMyPreferenceBoolean("intentAlreadyDealtWith", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Keep a reference to connections if needed as bundle

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && getWindowFlags()!=null) {
            getWindowFlags().hideKeyboard();
        }
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && navController != null && navController.getCurrentDestination() != null) {
            if (Objects.requireNonNull(navController.getCurrentDestination()).getId() != R.id.setStorageLocationFragment) {
                showActionBar();
            }
        }
    }

    @Override
    public void setUpBatteryMonitor() {
        if (batteryStatus == null) {
            batteryStatus = new BatteryStatus(this, myView.myToolbar.getBatteryimage(),
                    myView.myToolbar.getBatterycharge(), myView.myToolbar.getActionBarHeight(true));
        }
        batteryStatus.setUpBatteryMonitor();
    }

    @Override
    public int[] getDisplayMetrics() {
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int[] displayMetrics = new int[3];
        displayMetrics[0] = getWindow().getDecorView().getWidth();
        displayMetrics[1] = getWindow().getDecorView().getHeight();
        displayMetrics[2] = metrics.densityDpi;
        return displayMetrics;
    }

    @Override
    public float getDisplayDensity() {
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        return metrics.density;
    }

    // The secondary displays (HDMI or Mirroring/Casting)
    @SuppressLint("PrivateResource")
    @Override
    public void checkDisplays() {
        // This checks for connected displays and adjusts the menu item if connected
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (bootUpCompleted && displayManager != null) {
            connectedDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);

            // If we have changed the number of connected displays, set them up
            if (connectedDisplays.length > prevNumConnectedDisplays) {
                prevNumConnectedDisplays = connectedDisplays.length;
                setupDisplays();
            }
        } else {
            connectedDisplays = null;
            secondaryDisplays = null;
        }

        updateCastIcon();
    }

    private void updateCastIcon() {
        if (globalMenuItem != null) {
            if (settingsOpen || !getAlertChecks().getHasPlayServices()) {
                globalMenuItem.findItem(R.id.mirror_menu_item).setVisible(false);
            } else {
                Drawable drawable;
                if (secondaryDisplays != null && connectedDisplays.length > 0) {
                    drawable = VectorDrawableCompat.create(getResources(), R.drawable.cast_connected, getTheme());
                } else {
                    drawable = VectorDrawableCompat.create(getResources(), R.drawable.cast, getTheme());
                }
                globalMenuItem.findItem(R.id.mirror_menu_item).setIcon(drawable);
                globalMenuItem.findItem(R.id.mirror_menu_item).setVisible(true);
            }
        }
    }

    private void setupDisplays() {
        // Go through each connected display and create the secondaryDisplay Presentation class
        // Check there aren't any already connected, if there are, dismiss them
        if (secondaryDisplays != null) {
            for (SecondaryDisplay secondaryDisplay : secondaryDisplays) {
                if (secondaryDisplay != null && secondaryDisplay.isShowing()) {
                    try {
                        secondaryDisplay.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Now reset the secondaryDisplays
        secondaryDisplays = null;
        if (connectedDisplays.length > 0) {
            secondaryDisplays = new SecondaryDisplay[connectedDisplays.length];
            for (int c = 0; c < connectedDisplays.length; c++) {
                secondaryDisplays[c] = new SecondaryDisplay(this, connectedDisplays[c]);
                secondaryDisplays[c].show();
            }
        }

        // Update cast icon
        updateCastIcon();
    }

    @Override
    public boolean getIsSecondaryDisplaying() {
        return secondaryDisplays != null && secondaryDisplays.length > 0;
    }

    @Override
    public void updateDisplay(String what) {
        // Update cast icon
        updateCastIcon();
        if (secondaryDisplays != null) {
            for (SecondaryDisplay secondaryDisplay : secondaryDisplays) {
                if (secondaryDisplay != null && secondaryDisplay.isShowing()) {
                    try {
                        switch (what) {
                            // The song info bar
                            case "initialiseInfoBarRequired":
                                secondaryDisplay.initialiseInfoBarRequired();
                                break;
                            case "setSongInfo":
                                secondaryDisplay.setSongInfo();
                                break;
                            case "setInfoStyles":
                                secondaryDisplay.setInfoStyles();
                                break;
                            case "changeInfoAlignment":
                                secondaryDisplay.changeInfoAlignment();
                                break;
                            case "checkSongInfoShowHide":
                                secondaryDisplay.checkSongInfoShowHide();
                                break;

                            // Song content
                            case "setSongContent":
                                secondaryDisplay.setSongContent();
                                break;

                            case "contentAlignment":
                            case "showSection":
                                secondaryDisplay.showSection(song.getCurrentSection());
                                break;
                            case "editView":
                                secondaryDisplay.editView();
                                break;
                            case "newSongLoaded":
                                secondaryDisplay.setIsNewSong();
                                break;

                            // The alert bar
                            case "showAlert":
                                secondaryDisplay.showAlert();
                                break;
                            case "updateAlert":
                                secondaryDisplay.updateAlert();
                                break;

                            // The screen setup
                            case "measureAvailableSizes":
                                secondaryDisplay.measureAvailableSizes();
                                //secondaryDisplay.viewsAreReady();
                                break;
                            case "setScreenSizes":
                                secondaryDisplay.setScreenSizes();
                                break;
                            case "changeBackground":
                                secondaryDisplay.changeBackground();
                                break;
                            case "changeRotation":
                                secondaryDisplay.changeRotation();
                                break;
                            case "setSongContentPrefs":
                                secondaryDisplay.setSongContentPrefs();
                                break;

                            // The logo
                            case "changeLogo":
                                secondaryDisplay.changeLogo();
                                break;
                            case "showLogo":
                                secondaryDisplay.showLogo(presenterSettings.getLogoOn(), false);
                                break;

                            // Black and blank screen
                            case "showBlackscreen":
                                secondaryDisplay.showBlackScreen();
                                break;
                            case "showBlankscreen":
                                secondaryDisplay.showBlankScreen();
                                break;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void presenterShowSection(int position) {
        int sections;
        if (song.getFiletype().equals("PDF") || song.getFolder().contains("**Image")) {
            sections = song.getPdfPageCount();
            if (position < sections) {
                song.setPdfPageCurrent(position);
            }
        } else if (song.getFiletype().equals("IMG")) {
            sections = 1;
            song.setPdfPageCount(0);
            song.setCurrentSection(0);
        } else {
            sections = song.getPresoOrderSongSections().size();
            if (position < sections) {
                song.setCurrentSection(position);
            }
        }

        if (secondaryDisplays != null) {
            for (SecondaryDisplay secondaryDisplay : secondaryDisplays) {
                if (secondaryDisplay != null && secondaryDisplay.isShowing() &&
                        position < sections) {
                    try {
                        secondaryDisplay.showSection(position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void performanceShowSection(int position) {
        // This gets a section from from the user selecting either a PDF page or a Stage Mode section
        // Send it back to Performance Mode to deal with the outcome (scroll to, update display, etc)
        if (performanceValid()) {
            performanceFragment.performanceShowSection(position);
        }
    }


}