package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.abcnotation.ABCNotation;
import com.garethevans.church.opensongtablet.aeros.Aeros;
import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.appdata.AlertChecks;
import com.garethevans.church.opensongtablet.appdata.BootUpFragment;
import com.garethevans.church.opensongtablet.appdata.CheckInternet;
import com.garethevans.church.opensongtablet.appdata.FixLocale;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
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
import com.garethevans.church.opensongtablet.customviews.MyExtendedFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.customviews.MyToolbar;
import com.garethevans.church.opensongtablet.databinding.ActivityBinding;
import com.garethevans.church.opensongtablet.drummer.Drummer;
import com.garethevans.church.opensongtablet.export.ExportActions;
import com.garethevans.church.opensongtablet.export.OpenSongSetBundle;
import com.garethevans.church.opensongtablet.export.PrepareFormats;
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
import com.garethevans.church.opensongtablet.justchords.ConvertJustChords;
import com.garethevans.church.opensongtablet.justchords.JustChordsObject;
import com.garethevans.church.opensongtablet.links.LinksFragment;
import com.garethevans.church.opensongtablet.metronome.Metronome;
import com.garethevans.church.opensongtablet.midi.Midi;
import com.garethevans.church.opensongtablet.midi.MidiActionBottomSheet;
import com.garethevans.church.opensongtablet.multitrack.MultiTrackPlayer;
import com.garethevans.church.opensongtablet.multitrack.MultiTrackPopUp;
import com.garethevans.church.opensongtablet.nearby.NearbyActions;
import com.garethevans.church.opensongtablet.nearby.NearbyConnectionsFragment;
import com.garethevans.church.opensongtablet.nearby.SyncNearbyFragment;
import com.garethevans.church.opensongtablet.openchords.OpenChordsAPI;
import com.garethevans.church.opensongtablet.openchords.OpenChordsFragment;
import com.garethevans.church.opensongtablet.pads.Pad;
import com.garethevans.church.opensongtablet.pdf.MakePDF;
import com.garethevans.church.opensongtablet.pdf.OCR;
import com.garethevans.church.opensongtablet.performance.DisplayPrevNext;
import com.garethevans.church.opensongtablet.performance.PerformanceFragment;
import com.garethevans.church.opensongtablet.performance.PerformanceGestures;
import com.garethevans.church.opensongtablet.preferences.AppPermissions;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.ProfileActions;
import com.garethevans.church.opensongtablet.presenter.PresenterFragment;
import com.garethevans.church.opensongtablet.presenter.PresenterSettings;
import com.garethevans.church.opensongtablet.presenter.SongSectionsFragment;
import com.garethevans.church.opensongtablet.screensetup.BatteryStatus;
import com.garethevans.church.opensongtablet.screensetup.FontSetupFragment;
import com.garethevans.church.opensongtablet.screensetup.Palette;
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
import com.garethevans.church.opensongtablet.utilities.AudioPlayerBottomSheet;
import com.garethevans.church.opensongtablet.utilities.AudioRecorderPopUp;
import com.garethevans.church.opensongtablet.utilities.DatabaseUtilitiesFragment;
import com.garethevans.church.opensongtablet.utilities.ForumFragment;
import com.garethevans.church.opensongtablet.utilities.TimeTools;
import com.garethevans.church.opensongtablet.variations.Variations;
import com.garethevans.church.opensongtablet.voicelive.VoiceLive;
import com.garethevans.church.opensongtablet.webserver.LocalWiFiHost;
import com.garethevans.church.opensongtablet.webserver.WebServer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
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

    public static final Gson gson = new Gson();

    // Initialise the Executors and main handlers for async tasks
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() ,               // Initial pool size
            (Runtime.getRuntime().availableProcessors() * 8),          // Max pool size (including queued)
            1000,                                                      // Time for idle thread to remain
            TimeUnit.MILLISECONDS,                                     // Unit
            new ArrayBlockingQueue<>(10)                       // Blocking queue
    );

    // The helpers sorted alphabetically
    private ABCNotation abcNotation;
    private Aeros aeros;
    private AlertChecks alertChecks;
    private AppPermissions appPermissions;
    private Autoscroll autoscroll;
    private BeatBuddy beatBuddy;
    private Bible bible;
    private CCLILog ccliLog;
    private CheckInternet checkInternet;
    private ChordDirectory chordDirectory;
    private ChordDisplayProcessing chordDisplayProcessing;
    private CommonControls commonControls;
    private CommonSQL commonSQL;
    private ConvertJustChords convertJustChords;
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
    private LocalWiFiHost localWiFiHost;
    private MakePDF makePDF;
    private Metronome metronome;
    private Midi midi;
    private MultiTrackPlayer multiTrackPlayer;
    private NearbyActions nearbyActions;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private OCR ocr;
    private OpenChordsAPI openChordsAPI;
    private OpenSongSetBundle openSongSetBundle;
    private Pad pad;
    private PageButtons pageButtons;
    private Palette palette;
    private PedalActions pedalActions;
    private PerformanceGestures performanceGestures;
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
    private Variations variations;
    private VersionNumber versionNumber;
    private VoiceLive voiceLive;
    private WebDownload webDownload;
    private WebServer webServer;

    // The audio recorder permissions
    private ActivityResultLauncher<String> audioPermissionLauncher;
    private ActivityResultLauncher<Intent> selectFileLauncher;
    private ActivityResultLauncher<Intent> selectFolderLauncher;
    private boolean requireAudioRecorder = false;
    private AudioRecorderPopUp audioRecorderPopUp;
    private MultiTrackPopUp multiTrackPopUp;

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
    private final Handler mainLooper = new Handler(Looper.getMainLooper());
    private final Handler updatingToolbarHandler = new Handler(Looper.getMainLooper());
    private Runnable updatingToolbarRunnable;

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
    private boolean settingsOpen = false, showSetMenu, forceReload,
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
            website_menu_set = "", website_menu_song = "", exit_confirm = "", deeplink_set_bundle = "",
            error = "", deeplink_presenter = "", deeplink_performance = "", extra_settings = "",
            action_button_info = "", song_sections = "", logo_info = "", blank_screen_info = "",
            black_screen_info = "", project_panic = "", song_title = "", long_press = "", edit_song = "",
            song_sections_project = "", menu_song_info = "", menu_set_info = "", add_songs = "",
            song_actions = "", deeplink_preferences = "", song_string = "", set_string = "",
            search_index_start = "", search_index_end = "", deeplink_metronome = "",
            mode_presenter = "", mode_performance = "", mode_stage = "", success = "", okay = "", pad_playback_info = "",
            no_suitable_application = "", indexing_string = "", deeplink_edit = "", cast_info_string = "",
            menu_showcase_info ="";

    private MenuItem menuScreenMirror, menuScreenHelp, menuSearch, menuSettings;
    private String webHelpAddress = null;

    private Drawable searchIcon, helpIcon, settingsIcon, closeIcon, castIconOff, castIconOn,
            navIconBack, navIconMenu;

    // Used if implementing Oboe using C++ injection
    /* static {System.loadLibrary("lowlatencyaudio");} */

    // Set up the activity
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Use a manual dark / light theme (Android's breaks when using WebView!)
        getPalette();
        // Get current theme mode from SharedPreferences or wherever you store it
        getWindow().setBackgroundDrawable(new ColorDrawable(getPalette().background));

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        // Set up crash collector
        setUpCrashCollector();

        // Set up the audioPermission launcher
        audioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                requireAudioRecorder = true;
                // Navigate home (performance/presenter) and then this fragment will attempt to show the popup
                navHome();
            } else {
                // notify user
                InformationBottomSheet informationBottomSheet = new InformationBottomSheet(getString(R.string.microphone),
                        getString(R.string.permissions_refused), getString(R.string.settings), "appPrefs");
                informationBottomSheet.show(getMyFragmentManager(), "InformationBottomSheet");
            }
        });

        // Updating toolbar runnable
        updatingToolbarRunnable = () -> {
            updatingToolbarHelp = true;
            if (menuScreenHelp != null) {
                menuScreenHelp.setVisible(webHelpAddress != null && !webHelpAddress.isEmpty());
                if (menuScreenHelp.isVisible() && !isCurrentFragment(R.id.setStorageLocationFragment)) {
                    showCase.singleShowCase(MainActivity.this, menuScreenHelp.getActionView(), null, getString(R.string.help), false, "webHelp");
                }
            }
            updatingToolbarHelp = false;
        };

        // The file picker launcher
        selectFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        if (data.getData()!=null) {
                            setImportUri(data.getData());
                            if (whattodo.equals("audioplayer")) {
                                whattodo="";
                                AudioPlayerBottomSheet audioPlayerBottomSheet = new AudioPlayerBottomSheet();
                                audioPlayerBottomSheet.show(getMyFragmentManager(), "audioPlayerBottomSheet");
                            }
                        } else {
                            getShowToast().error();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    getShowToast().error();
                }
            }
        });
        selectFolderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (multiTrackPopUp!=null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                multiTrackPopUp.processAlternativeFolderUri(result.getData());
                            }
                        }
                    }
                });



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
                TooLargeTool.startLogging(this.getApplication());
                WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            });

            if (savedInstanceState != null) {
                Log.d(TAG,"TooLargeTool is logging:"+TooLargeTool.bundleBreakdown(savedInstanceState));
                bootUpCompleted = savedInstanceState.getBoolean("bootUpCompleted", false);
                rebooted = true;
                getSongListBuildIndex();

                songListBuildIndex.setIndexComplete(savedInstanceState.getBoolean("indexComplete", false));
                songListBuildIndex.setFullIndexRequired(!songListBuildIndex.getIndexComplete());
                nearbyActions = getNearbyActions();
                nearbyActions.getNearbyConnectionManagement().setIsHost(savedInstanceState.getBoolean("isHost", false));
                nearbyActions.getNearbyConnectionManagement().setUsingNearby(savedInstanceState.getBoolean("usingNearby", false));
                nearbyActions.getNearbyConnectionManagement().setDiscoveredEndpoints(savedInstanceState.getStringArrayList("discoveredEndpoints"));
                nearbyActions.getNearbyConnectionManagement().setConnectedEndpoints(savedInstanceState.getStringArrayList("connectedEndpoints"));
                // If we were using Nearby, try to start it again
                if (nearbyActions.getNearbyConnectionManagement().getUsingNearby() &&
                        nearbyActions.getNearbyConnectionManagement().getIsHost()) {
                    nearbyActions.getNearbyConnectionManagement().doTempAdvertise();
                } else if (nearbyActions.getNearbyConnectionManagement().getUsingNearby() &&
                        !nearbyActions.getNearbyConnectionManagement().getIsHost()) {
                    nearbyActions.getNearbyConnectionManagement().doTempDiscover();
                }

                // Make sure the song title is there
                updateToolbar(null);

                // Clear the saved instance state - we've finished with everything we need.
                savedInstanceState.clear();

            } else {
                rebooted = false;
            }

            // Make sure the song title is there
            updateToolbar(null);

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
    public Palette getPalette() {
        if (palette == null) {
            palette = new Palette(this);
        }
        return palette;
    }

    @Override
    public boolean getWaitingOnBootUpFragment() {
        return waitingOnBootUpFragment;
    }

    private void setUpCrashCollector() {
        // Set up a default crash capture, but keep a reference to the original handler
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            // Get the stack trace.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            // Write a crash log file
            getStorageAccess().updateCrashLog(sw.toString());

            // Reset the unhandled exception handler
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

            // Now turn off the app while alerting the user
            try {
                Toast.makeText(this, this.getString(R.string.crash_alert), Toast.LENGTH_LONG).show();
                throw e;
            } catch (Throwable ex) {
                this.finish();
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

    @Override
    public void prepareStrings() {
        // To avoid null context for long tasks throwing error when getting strings
        if (getApplicationContext() != null) {

            // Fix the user locale preference
            fixLocale = getFixLocale();
            fixLocale.setLocale();
            locale = fixLocale.getLocale();
            locale = fixLocale.getLocale();

            deeplink_import_osb = getString(R.string.deeplink_import_osb);
            deeplink_sets_backup_restore = getString(R.string.deeplink_sets_backup_restore);
            deeplink_onsong = getString(R.string.deeplink_onsong);
            deeplink_import_file = getString(R.string.deeplink_import_file);
            deeplink_set_bundle = getString(R.string.deeplink_set_bundle);
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
        getVariations().updateStrings(this);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        fileOpenIntent = intent;
        Log.d(TAG,"new intent:"+intent+"  "+intent.getData());
        // Send the action to be called from the opening fragment to fix backstack!
        if (settingsOpen) {
            if (whichMode.equals(mode_presenter)) {
                dealWithIntent(R.id.presenterFragment);
            } else if (whichMode.equals(mode_performance)) {
                dealWithIntent(R.id.performanceFragment);
            }
        } else if (presenterValid()) {
            presenterFragment.tryToImportIntent();
        } else if (performanceValid()) {
            Log.d(TAG,"performance valid - sending there");
            performanceFragment.tryToImportIntent();
        }
        super.onNewIntent(intent);
    }

    @Override
    public void dealWithIntent(int navigationId) {
        boolean dealtWith = getPreferences().getMyPreferenceBoolean("intentAlreadyDealtWith",false);
        Log.d(TAG,"intentAlreadyDealtWith:"+dealtWith);
        getThreadPoolExecutor().execute(() -> {
            if (fileOpenIntent != null && fileOpenIntent.getDataString()!=null && fileOpenIntent.getDataString().startsWith(getOpenChordsAPI().getAppFolderTrigger())) {
                // This should trigger the GET request to sync OpenChords
                Log.d(TAG,"openchords link received\n"+ fileOpenIntent.getData());
                try {
                    getPreferences().setMyPreferenceBoolean("intentAlreadyDealtWith", true);
                    String uuid = fileOpenIntent.getData().toString().replace(getOpenChordsAPI().getAppFolderTrigger(), "");
                    Log.d(TAG,"uuid:"+uuid);
                    getOpenChordsAPI().setOpenChordsFolderUuid(uuid);
                    getOpenChordsAPI().setReceivedFolderLink(true);
                    setWhattodo("openchordsintent");
                    getMainHandler().post(() -> navigateToFragment(getString(R.string.deeplink_openchords), 0));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (!dealtWith && fileOpenIntent != null && (fileOpenIntent.getDataString() != null || fileOpenIntent.getData()!=null)) {
                if (getStorageAccess().getFileSizeFromUri(fileOpenIntent.getData()) > 0) {
                    getPreferences().setMyPreferenceBoolean("intentAlreadyDealtWith", true);
                    importUri = fileOpenIntent.getData();
                    Log.d(TAG, "intent received:" + importUri);

                    getMainHandler().post(() -> navController.popBackStack(navigationId, false));

                    // We need to copy this file to our temp storage for now to have later permission
                    InputStream inputStream;
                    try {
                        inputStream = getContentResolver().openInputStream(importUri);
                        importFilename = getStorageAccess().getFileNameFromUri(importUri);
                        if (inputStream != null) {
                            File tempFile = getStorageAccess().getAppSpecificFile("Import", "", importFilename);
                            FileOutputStream outputStream = new FileOutputStream(tempFile);
                            getStorageAccess().updateFileActivityLog(TAG + " dealWithIntent CopyFile " + importUri + " to " + tempFile);
                            getStorageAccess().copyFile(inputStream, outputStream);
                            importUri = Uri.fromFile(tempFile);
                            openFragmentBasedOnFileImport();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        getPreferences().setMyPreferenceBoolean("intentAlreadyDealtWith",false);
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
        getNearbyActions();
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
        convertJustChords = getConvertJustChords();
        convertChoPro = getConvertChoPro();
        convertOnSong = getConvertOnSong();
        convertTextSong = getConvertTextSong();
        processSong = getProcessSong();
        prepareFormats = getPrepareFormats();
        songSheetHeaders = getSongSheetHeaders();
        ocr = getOCR();
        openChordsAPI = getOpenChordsAPI();
        makePDF = getMakePDF();
        transpose = getTranspose();
        abcNotation = getAbcNotation();

        song = getSong();
        variations = getVariations();

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
        voiceLive = getVoiceLive();
        drummer = getDrummer();
        pedalActions = getPedalActions();
        pad = getPad();
        autoscroll = getAutoscroll();
        metronome = getMetronome();
        gestures = getGestures();
        swipes = getSwipes();
        timeTools = getTimeTools();
        displayPrevNext = getDisplayPrevNext();
        multiTrackPlayer = getMultiTrackPlayer();

        // Other file actions
        ccliLog = getCCLILog();
        exportActions = getExportActions();
        bible = getBible();
        customSlide = getCustomSlide();
        presenterSettings = getPresenterSettings();

        // Webserver (for displaying song over html server)
        webServer = getWebServer();
        localWiFiHost = getLocalWiFiHost();
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
        if (getSupportActionBar()==null) {
            setupActionbar();
        }
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!disable);
        }
        if (disable) {
            hideActionBar();
        }

        if (menuSearch!=null) {
            menuSearch.setVisible(!disable);
        }
        if (menuSettings!=null) {
            menuSettings.setVisible(!disable);
        }
        if (menuScreenHelp!=null) {
            return (ImageView) menuScreenHelp.getActionView();
        } else {
            return null;
        }
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
        int bottomOfToolbar = myView.myAppBarLayout.getBottom();
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

        if (getSupportActionBar()!=null) {
            myView.myToolbar.initialiseToolbar(this, getSupportActionBar());
        }
        initialisePageButtons();
        tintDrawerLayout();

        View root = myView.getRoot();
        root.setFocusableInTouchMode(true);
        root.requestFocus();
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

    @Override
    public void initialiseStartVariables() {
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

    private void tintDrawerLayout() {
        myView.menuTop.getRoot().setBackgroundColor(getPalette().background);
        myView.menuTop.menuHelp.setColorFilter(getPalette().onPrimary, PorterDuff.Mode.SRC_IN);
        myView.menuTop.backButton.setColorFilter(getPalette().onPrimary, PorterDuff.Mode.SRC_IN);
        if (songMenuFragment!=null) {
            songMenuFragment.updateTheme();
        }
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

                // Hide the abc notes if required
                showAbc(false,true);

                // Hide the sticky notes if required
                showSticky(false,true);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                menuOpen = false;
                if (!whichMode.equals(presenter)) {
                    hideActionButton(myView.drawerLayout.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_UNLOCKED);
                }
                // Hide the keyboard
                windowFlags.hideKeyboard();

                // Show the abc notes if required
                if (performanceValid() && getAbcNotation().getAutoshowMusicScore() &&
                        getSong().getAbc()!=null && !getSong().getAbc().isEmpty()) {
                    showAbc(true,false);
                }

                // Show the sticky notes if required
                if (performanceValid()) {
                    showSticky(false, false);
                }
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


    private void checkMenuIconsAreNotNull() {
        if (closeIcon==null) {
            Drawable tempCloseIcon = AppCompatResources.getDrawable(this, R.drawable.close);
            if (tempCloseIcon != null) {
                closeIcon = DrawableCompat.wrap(tempCloseIcon);
                DrawableCompat.setTint(closeIcon, getPalette().textColor);
            }
        }

        if (settingsIcon==null) {
            Drawable tempSettingsIcon = AppCompatResources.getDrawable(this, R.drawable.settings_outline);
            if (tempSettingsIcon != null) {
                settingsIcon = DrawableCompat.wrap(tempSettingsIcon);
                DrawableCompat.setTint(settingsIcon, getPalette().textColor);
            }
        }

        if (castIconOff==null) {
            Drawable tempCastIconOff = AppCompatResources.getDrawable(this, R.drawable.cast);
            if (tempCastIconOff != null) {
                castIconOff = DrawableCompat.wrap(tempCastIconOff);
                DrawableCompat.setTint(castIconOff, getPalette().textColor);
            }
        }

        if (castIconOn==null) {
            Drawable tempCastIconOn = AppCompatResources.getDrawable(this, R.drawable.cast_connected);
            if (tempCastIconOn != null) {
                castIconOn = DrawableCompat.wrap(tempCastIconOn);
                DrawableCompat.setTint(castIconOn, getPalette().textColor);
            }
        }

        if (searchIcon==null) {
            Drawable tempSearchIcon = AppCompatResources.getDrawable(this, R.drawable.search);
            if (tempSearchIcon != null) {
                searchIcon = DrawableCompat.wrap(tempSearchIcon);
                DrawableCompat.setTint(searchIcon, getPalette().textColor);
            }
        }

        if (helpIcon==null) {
            Drawable tempHelpIcon = AppCompatResources.getDrawable(this, R.drawable.help_outline);
            if (tempHelpIcon != null) {
                helpIcon = DrawableCompat.wrap(tempHelpIcon);
                DrawableCompat.setTint(helpIcon, getPalette().textColor);
            }
        }

        if (navIconBack==null) {
            Drawable tempNavIconBack = AppCompatResources.getDrawable(this, R.drawable.arrow_left);
            if (tempNavIconBack != null) {
                navIconBack = DrawableCompat.wrap(tempNavIconBack);
                DrawableCompat.setTint(navIconBack, getPalette().textColor);
            }
        }

        if (navIconMenu==null) {
            Drawable tempNavIconMenu = AppCompatResources.getDrawable(this,R.drawable.menu);
            if (tempNavIconMenu!=null) {
                navIconMenu = DrawableCompat.wrap(tempNavIconMenu);
                DrawableCompat.setTint(navIconMenu, getPalette().textColor);
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

            try {
                TooltipCompat.setTooltipText(myView.drawerLayout, null);
                int size = myView.myToolbar.getChildCount();
                for (int i = 0; i < size; i++) {
                    View child = myView.myToolbar.getChildAt(i);
                    if (child != null) {
                        TooltipCompat.setTooltipText(child, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

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
                    if (menuScreenMirror!=null) {
                        menuScreenMirror.setVisible(false);
                    }
                    if (menuScreenHelp!=null) {
                        menuScreenHelp.setVisible(false);
                    }

                    // IV - We set settingsOpen based on the new navDestination
                    settingsOpen = !((navDestination.getId() == R.id.performanceFragment ||
                            navDestination.getId() == R.id.presenterFragment));

                    // IV - To smooth build, we add elements right to left

                    checkOptionsMenu();

                    checkMenuIconsAreNotNull();

                    myView.myToolbar.setNavigationIcon(settingsOpen ? navIconBack:navIconMenu);

                    if (settingsOpen && menuSettings!=null) {
                        menuSettings.setIcon(closeIcon);

                        // IV - Other elements are added by the called fragment
                    } else if (!settingsOpen) {
                        // IV - Top level of menu - song details are added by song load
                        menuSettings.setIcon(settingsIcon);
                        updateCastIcon();
                        if (getPreferences().getMyPreferenceBoolean("clockOn", true) ||
                                getPreferences().getMyPreferenceBoolean("batteryTextOn", true) ||
                                getPreferences().getMyPreferenceBoolean("batteryDialOn", true)) {
                            if (myView != null) {
                                myView.myToolbar.batteryholderVisibility(true, true);
                            }
                            if (getBatteryStatus() != null) {
                                batteryStatus.showBatteryStuff(true);
                            }
                        }
                        // IV - Song details are added by song load
                        // GE onResuming (open cast and return), not called, so quick check is worthwhile
                        if (!whichMode.equals(getString(R.string.mode_presenter))) {
                            updateToolbar(null);
                        }
                    }
                    myView.myToolbar.setNavigationOnClickListener(view -> {
                        TooltipCompat.setTooltipText(view,null);
                        int aboutToGoTo = -1;
                        if (navController.getPreviousBackStackEntry()!=null) {
                            aboutToGoTo = navController.getPreviousBackStackEntry().getDestination().getId();
                        }
                        if (navController.getCurrentDestination()!=null &&
                            (navController.getCurrentDestination().getId()==R.id.performanceFragment ||
                                    navController.getCurrentDestination().getId()==R.id.presenterFragment)) {
                            closeDrawer(myView.drawerLayout.isDrawerOpen(GravityCompat.START));

                        } else if (aboutToGoTo == R.id.presenterFragment || aboutToGoTo == R.id.performanceFragment ||
                               (settingsOpen && navController.getCurrentDestination()!=null &&
                                navController.getCurrentDestination().getId()==R.id.preferencesFragment) ||
                                (navHostFragment.getChildFragmentManager().getBackStackEntryCount()==1 &&
                                        navController.getCurrentDestination()!=null &&
                                        navController.getCurrentDestination().getId()!=R.id.performanceFragment &&
                                        navController.getCurrentDestination().getId()==R.id.presenterFragment)) {
                            navHome();
                        } else {
                            navController.navigateUp();
                        }
                    });

                    myView.myToolbar.requestLayout();
                    myView.myToolbar.setContentInsetStartWithNavigation(0);
                }
            });
        }
    }

    // If we navigate to any fragment, the forceReload (for song display) is set to true
    // The Performance/Stage/Presenter modes remove this flag after loading
    @Override
    public void setForceReload(boolean forceReload) {
        this.forceReload = forceReload;
    }
    @Override
    public boolean getForceReload() {
        return forceReload;
    }

    @Override
    public void navigateToFragment(String deepLink, int id) {
        // Hide the abc notes if required
        showAbc(false,true);

        // Hide the sticky notes if required
        showSticky(false,true);

        // If we are currently on the song window (performanceFragment or presenterFragment)
        // Make sure the backstack is clear as we are at the root page before going elsewhere
        int aboutToGoTo = -1;
        if (navController!=null && navController.getPreviousBackStackEntry()!=null) {
            aboutToGoTo = navController.getPreviousBackStackEntry().getDestination().getId();
        }
        if ((id == R.id.performanceFragment || id == R.id.presenterFragment) &&
                (aboutToGoTo == R.id.presenterFragment || aboutToGoTo == R.id.performanceFragment)) {
            navHome();
        } else {
            // Set the force reload flag as we want the song to reload when needed
            // Set the force reload flag
            forceReload = true;
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
            if (autoscroll != null) {
                autoscroll.stopAutoscroll();
            }

            if (deepLink != null && deepLink.equals(deeplink_edit) && songListBuildIndex.getCurrentlyIndexing()) {
                String progressText = "";
                if (songMenuFragment != null) {
                    MyMaterialSimpleTextView progressView = songMenuFragment.getProgressText();
                    if (progressView != null && progressView.getText() != null) {
                        progressText = " " + progressView.getText().toString();
                    }
                }

                getShowToast().doIt(indexing_string + progressText);
                hideActionButton(false);

            } else {
                runOnUiThread(() -> {
                    try {
                        if (navController == null) {
                            setupActionbar();
                            setupNavigation();
                        }
                        if (deepLink != null && navController != null) {
                            navController.navigate(Uri.parse(deepLink));
                        } else if (navController != null) {
                            navController.navigate(id);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            showActionBar();
        }

        checkToolbarMenuIcons();
    }

    @Override
    public void popTheBackStack(int id, boolean inclusive) {
        try {
            navController.popBackStack(id, inclusive);
        } catch (Exception e) {
            Log.d(TAG,"FragmentManager busy...");
        }
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
                        if (getStorageAccess().doStringWriteToFile("Songs", song.getFolder(), song.getFilename(), newSongText)) {
                            navigateToFragment(null, R.id.editSongFragment);
                        } else {
                            getShowToast().doIt(error);
                        }
                    }
                    break;

                case "sortSet":
                case "shuffleSet":
                case "rebuildSet":
                    getThreadPoolExecutor().execute(() -> {
                        if (setMenuFragment!=null) {
                            // Firstly hide the set
                            setMenuFragment.changeVisibility(false);
                            // Sort or shuffle the set as required
                            if (fragName.equals("sortSet")) {
                                getSetActions().sortSet();
                            } else if (fragName.equals("shuffleSet")) {
                                getSetActions().shuffleSet();
                            } else {
                                getCurrentSet().loadCurrentSet();
                            }

                            // Show the set
                            setMenuFragment.changeVisibility(true);
                            try {
                                setMenuFragment.notifyItemRangeChanged(0, getCurrentSet().getCurrentSetSize());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

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
            whichMode = getPreferences().getMyPreferenceString("whichMode", performance);
            if (navController.getCurrentDestination() != null) {
                try {
                    navController.popBackStack(Objects.requireNonNull(navController.getCurrentDestination()).getId(), true);
                } catch (Exception e) {
                    Log.d(TAG,"Can't pop the backstack");
                    e.printStackTrace();
                }
            }
            if (whichMode.equals(mode_presenter)) {
                navigateToFragment(deeplink_presenter, 0);

            } else {
                navigateToFragment(deeplink_performance, 0);
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
        if (nearbyActions!=null) {
            nearbyActions.getNearbyConnectionManagement().getUserNickname();

            // Establish a known state for Nearby
            nearbyActions.getNearbyConnectionManagement().turnOffNearby();
        }
    }

    @Override
    public NearbyActions getNearbyActions() {
        // Return a reference to nearbyActions
        if (nearbyActions == null) {
            nearbyActions = new NearbyActions(this,this);
        }
        return nearbyActions;
    }

    @Override
    public void nearbyEnableConnectionButtons() {
        if (settingsOpen && getNearbyActions().getNearbyConnectionManagement().getConnectionsOpen()
                && nearbyConnectionsFragment != null) {
            try {
                nearbyConnectionsFragment.enableConnectionButtons();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void nearbyUpdateCountdownText(boolean advertise, MyMaterialButton materialButton) {
        if (settingsOpen && getNearbyActions().getNearbyConnectionManagement().getConnectionsOpen() &&
                nearbyConnectionsFragment != null) {
            try {
                nearbyConnectionsFragment.updateCountdownText(advertise, materialButton);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void startDiscovery() {
        getNearbyActions().getNearbyConnectionManagement().startDiscovery();
    }

    @Override
    public void startAdvertising() {
        getNearbyActions().getNearbyConnectionManagement().startAdvertising();
    }

    @Override
    public void stopDiscovery() {
        getNearbyActions().getNearbyConnectionManagement().stopDiscovery();
    }

    @Override
    public void stopAdvertising() {
        getNearbyActions().getNearbyConnectionManagement().stopAdvertising();
    }

    @Override
    public void turnOffNearby() {
        getNearbyActions().getNearbyConnectionManagement().turnOffNearby();
    }

    @Override
    public void updateConnectionsLog() {
        // Send the command to the Nearby Connections fragment (if it exists!)
        try {
            if (nearbyConnectionsFragment != null &&
                    nearbyActions.getNearbyConnectionManagement().getConnectionsOpen()) {
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
    public void showNearbyAlertPopUp(String message) {
        if (performanceValid()) {
            performanceFragment.showNearbyAlertPopUp(message);
        }
    }


    // For audio recording
    public void setRequireAudioRecorder() {
        if (audioRecorderPopUp!=null) {
            try {
                audioRecorderPopUp.destroyPopup();
            } catch (Exception e) {
                e.printStackTrace();
            }
            removeAudioRecorderPopUp();
        } else {
            if (getAppPermissions().hasAudioPermissions() && !requireAudioRecorder) {
                requireAudioRecorder = true;
            } else {
                requireAudioRecorder = false;
                audioPermissionLauncher.launch(getAppPermissions().getAudioPermissions());
            }
        }
    }

    @Override
    public void removeAudioRecorderPopUp() {
        audioRecorderPopUp = null;
        requireAudioRecorder = false;
    }

    @Override
    public void displayAudioRecorder() {
        if (requireAudioRecorder) {
            requireAudioRecorder = false;
            if (myView!=null) {
                if (audioRecorderPopUp!=null) {
                    try {
                        audioRecorderPopUp.destroyPopup();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                audioRecorderPopUp = new AudioRecorderPopUp(this);
                audioRecorderPopUp.floatRecorder(myView.fragmentView);
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void displayMultiTrack() {
        // Only allow for API 21 and above
        getMainHandler().post(() -> {
            // Note that we need to decide on the audio encoding
            getMultiTrackPlayer().setAudioInfoSetForSong(false);

            if (myView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (multiTrackPopUp != null) {
                    // Close the popup
                    try {
                        multiTrackPopUp.destroyPopup();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    multiTrackPopUp = null;

                } else {
                    // Create and open the popup
                    multiTrackPopUp = new MultiTrackPopUp(this);
                    multiTrackPopUp.floatMultiTrack(myView.fragmentView);
                }
            }
        });
    }
    @Override
    public void nullMultitrackPopUp() {
        multiTrackPopUp = null;
    }
    @Override
    public MultiTrackPlayer getMultiTrackPlayer() {
        if (multiTrackPlayer==null) {
            multiTrackPlayer = new MultiTrackPlayer(this);
        }
        return multiTrackPlayer;
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
                        getCustomAnimation().fadeActionButton(myView.actionFAB, getMyThemeColors().getPageButtonAlpha());
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
        if (getSupportActionBar()==null) {
            setupActionbar();
        }
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
                if (myView!=null) {
                    myView.myToolbar.setActionBar(this, what);
                    if (whattodo!=null && whattodo.equals("storageBad")) {
                        myView.fragmentView.setTop(0);
                    } else {
                        myView.fragmentView.setTop(myView.myToolbar.getActionBarHeight(settingsOpen || menuOpen));
                    }
                }
            });
        }
    }
    private boolean updatingToolbarHelp = false;
    @Override
    public void updateToolbarHelp(String webHelpAddress) {
        // Only proceed if it has changed
        if ((this.webHelpAddress==null && webHelpAddress!=null) ||
                (this.webHelpAddress!=null && webHelpAddress==null) ||
                (!Objects.equals(this.webHelpAddress, webHelpAddress))) {
            // If a webAddress is supplied, setup and reveal the help button
            // or for a null or empty web address,hide the help button
            // Only allow this to happen after 200ms and only once (false repeats)
            // There is another check 800ms after opening the fragment
            if (!updatingToolbarHelp) {
                this.webHelpAddress = webHelpAddress;
                updatingToolbarHelp = true;
                updatingToolbarHandler.removeCallbacks(updatingToolbarRunnable);
                // For stability, run this on a delayed handler
                updatingToolbarHandler.postDelayed(updatingToolbarRunnable, 200);
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
        checkOptionsMenu();

        initialiseArrayLists();

        String whichShowcase;
        if (!getAlertChecks().getIsShowing()) {
            switch (what) {
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

    private void checkOptionsMenu() {
        if (menuSettings==null || menuSearch==null || menuScreenHelp==null || menuScreenMirror==null) {
            invalidateOptionsMenu();
        }
    }

    private void checkToolbarMenuIcons() {
        // To be run 800ms after the fragment has opened.
        // The fragment should have already set a webHelpAddress 200ms after opening if required
        // This is a secondary check as the fragment won't send null/empty values.
        updatingToolbarHandler.removeCallbacks(updatingToolbarRunnable);
        updatingToolbarHelp = false;
        updatingToolbarHandler.postDelayed(updatingToolbarRunnable, 800);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // GE had to add onResume string update otherwise this call failed if user changed languages
        if (item.getItemId()==R.id.settings_menu_item) {
            // Either open or close the settings menu
            if (settingsOpen) {
                if (navController.getCurrentDestination()!=null &&
                        navController.getCurrentDestination().getId()==R.id.preferencesFragment) {
                    popTheBackStack(R.id.preferencesFragment,true);
                }
                navHome();
            } else {
                navigateToFragment(deeplink_preferences, 0);
            }
            return true;

        } else if (item.getItemId()==R.id.help_menu_item) {
            if (webHelpAddress!=null && !webHelpAddress.isEmpty()) {
                openDocument(webHelpAddress);
            }
            return true;

        } else if (item.getItemId()==R.id.search_menu_item) {
            navigateToFragment(getString(R.string.deeplink_search_menu), 0);
            return true;

        } else if (item.getItemId()==R.id.mirror_menu_item) {
            if (!getShowCase().singleShowCase(this, menuScreenMirror.getActionView(), null, cast_info_string, true, "castInfo")) {
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
                            getShowToast().doIt(error);
                        }
                    }
                }
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void refreshMenuItems() {
        invalidateOptionsMenu();
        tintDrawerLayout();
    }

    private void tintMenuIcons(Menu menu, @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getIcon() != null) {
                Drawable icon = DrawableCompat.wrap(item.getIcon());
                DrawableCompat.setTint(icon, color);
                item.setIcon(icon);
            }
        }
    }

    @Override
    public void refreshToolbarMenu() {
        // This is called when we change the theme when running the app
        getWindow().setBackgroundDrawable(new ColorDrawable(getPalette().background));
        // Rebuild the menu icons
        navIconMenu = null;
        navIconBack = null;
        settingsIcon = null;
        closeIcon = null;
        searchIcon = null;
        helpIcon = null;
        castIconOff = null;
        castIconOn = null;
        checkMenuIconsAreNotNull();
        invalidateOptionsMenu();
        tintDrawerLayout();
        updatePageButtonLayout();
        myView.actionFAB.setPalette(getPalette());
        getDisplayPrevNext().updateColors();
        setUpSongMenuTabs();
    }

    @SuppressLint("PrivateResource")
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            globalMenuItem = menu;
            getMenuInflater().inflate(R.menu.mainactivitymenu, menu);
            int tint = getPalette().onPrimary;

            checkMenuIconsAreNotNull();

            menuSearch = menu.findItem(R.id.search_menu_item);
            menuScreenHelp = menu.findItem(R.id.help_menu_item);
            menuScreenHelp.setVisible(false);

            menuScreenMirror = menu.findItem(R.id.mirror_menu_item);
            menuSettings = menu.findItem(R.id.settings_menu_item);

            if (settingsOpen && closeIcon!=null) {
                menuSettings.setIcon(closeIcon);

            } else if (settingsIcon!=null){
                menuSettings.setIcon(settingsIcon);
            }
            if (searchIcon!=null) {
                menuSearch.setIcon(searchIcon);
            }
            if (helpIcon!=null) {
                menuScreenHelp.setIcon(helpIcon);
            }
            if (castIconOff!=null) {
                menuScreenMirror.setIcon(castIconOff);
            }

            menuScreenHelp.setVisible(!settingsOpen);
            menuScreenMirror.setVisible(!settingsOpen);

            tintMenuIcons(menu, tint);

            myView.myToolbar.setNavigationIcon(settingsOpen ? navIconBack : navIconMenu);
            updateCastIcon();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
            tabLayout.setTabTextColors(getPalette().textColor, getPalette().textColor);
            tabLayout.setTabIconTint(ColorStateList.valueOf(getPalette().textColor));
            tabLayout.setSelectedTabIndicatorColor(getPalette().secondary);
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
                // "removing" tooltip
                TooltipCompat.setTooltipText(tab.view, null);
            }).attach();

            // Still try to remove tooltips
            for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab == null) {
                    continue;
                }

                tab.view.addOnLayoutChangeListener((view, i0, i1, i2, i3, i4, i5, i6, i7) -> {
                    view.setContentDescription(null);
                    TooltipCompat.setTooltipText(view, null);
                });
            }
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
                    if (search_index_start != null) {
                        getShowToast().doIt(search_index_start);
                    }
                });
                if (songListBuildIndex != null && songMenuFragment != null && songMenuFragment.getProgressText() != null) {
                    songListBuildIndex.setIndexComplete(false);
                    songListBuildIndex.fullIndex(songMenuFragment.getProgressText(),null);
                } else {
                    // Try again in a short while
                    mainLooper.postDelayed(() -> {
                        if (songListBuildIndex!=null && songMenuFragment != null && songMenuFragment.getProgressText() != null) {
                            songListBuildIndex.setIndexComplete(false);
                            songListBuildIndex.fullIndex(songMenuFragment.getProgressText(),null);
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
                    if (search_index_end != null) {
                        getShowToast().doIt(search_index_end);
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
        getSetActions().indexSongInSet(song);
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
            ArrayList<String> songIds = getStorageAccess().listSongs(false);
            // Write this to text file
            getStorageAccess().writeSongIDFile(songIds);
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
            songMenuFragment.updateSongMenu();
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
        return performanceFragment != null && !whichMode.equals(presenter) && !settingsOpen;
    }

    @Override
    public boolean getPerformanceValid() {
        return performanceValid();
    }

    @Override
    public PerformanceFragment getPerformanceFragment() {
        return performanceFragment;
    }

    private boolean presenterValid() {
        return presenterFragment != null && whichMode.equals(presenter) && !settingsOpen;
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
    public SetMenuFragment getSetMenuFragment() {
        return setMenuFragment;
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
    public void setHighlightChangeAllowed(boolean highlightChangeAllowed) {
        if (setMenuFragment!=null) {
            setMenuFragment.setHighlightChangeAllowed(highlightChangeAllowed);
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
                if (pageButtons.getFAB(x)!=null) {
                    pageButtons.setPageButton(pageButtons.getFAB(x), x, false);
                }
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
    public void registerMidiPedalAction(boolean actionDown, boolean actionUp, boolean actionLong, String note) {
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
    public Midi getMidi() {
        if (midi == null) {
            midi = new Midi(this,this);
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
    public VoiceLive getVoiceLive() {
        if (voiceLive==null) {
            voiceLive = new VoiceLive(this);
        }
        return voiceLive;
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
    public MyExtendedFloatingActionButton getSaveButton() {
        if (editSongFragment != null) {
            return editSongFragment.getSaveButton();
        } else {
            return null;
        }
    }

    // The getters for references to the helper classes also needed in fragments
    @Override
    public void selectFile(Intent intent) {
        if (selectFileLauncher!=null) {
            selectFileLauncher.launch(intent);
        }
    }
    @Override
    public void selectFolder(Intent intent) {
        if (selectFolderLauncher!=null) {
            selectFolderLauncher.launch(intent);
        }
    }

    @Override
    public StorageAccess getStorageAccess() {
        if (storageAccess == null) {
            storageAccess = new StorageAccess(this);
        }
        return storageAccess;
    }

    @Override
    public void openFragmentBasedOnFileImport() {
        if (importUri!=null && importFilename != null && !importFilename.isEmpty()) {
            String dealingWithIntent = null;
            if (importFilename.toLowerCase(Locale.ROOT).endsWith(".osb")) {
                // OpenSongApp backup file
                dealingWithIntent = deeplink_import_osb;
            } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".osbs")) {
                // OpenSongApp sets backup file
                setWhattodo("restoresets");
                dealingWithIntent = deeplink_sets_backup_restore;
            } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".ost")) {
                // OpenSong song
                setWhattodo("intentlaunch");
                dealingWithIntent = deeplink_import_file;
            } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".osts") ||
                    importFilename.toLowerCase(Locale.ROOT).endsWith(".html")) {
                // OpenSong (or OnSong) set
                setWhattodo("importset");
                dealingWithIntent = deeplink_import_file;
            } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".backup")) {
                // OnSong backup file
                dealingWithIntent = deeplink_onsong;
            } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".ossb")) {
                // OpenSongApp set bundle
                setWhattodo("ossb");
                dealingWithIntent = deeplink_set_bundle;
            } else if (importFilename.toLowerCase(Locale.ROOT).endsWith(".justchords")) {
                JustChordsObject justChordsObject = getConvertJustChords().getJustChordsObjectFromImportUri();
                if (justChordsObject!=null && justChordsObject.getSongs()!=null) {
                    if (justChordsObject.getSongs().length > 1) {
                        // This is a set/bundle
                        setWhattodo("justchordsset");
                        dealingWithIntent = deeplink_set_bundle;
                    } else if (justChordsObject.getSongs().length == 1) {
                        // This is a song file
                        setWhattodo("justchordssong");
                        dealingWithIntent = deeplink_import_file;
                    }
                } else {
                    dealingWithIntent = "";
                    setWhattodo("");
                }
            } else if (getStorageAccess().isSpecificFileExtension("imageorpdf", importFilename) ||
                    getStorageAccess().isSpecificFileExtension("chordpro", importFilename) ||
                    getStorageAccess().isSpecificFileExtension("text", importFilename) ||
                    getStorageAccess().isSpecificFileExtension("onsong", importFilename) ||
                    importFilename.toLowerCase(Locale.ROOT).endsWith(".docx")) {
                setWhattodo("");
                // Set, song, pdf or image files are initially sent to the import file
                dealingWithIntent = deeplink_import_file;
            } else {
                // Might be an opensong file (with no extension)
                // If the file size is small enough (<200kB), read it as text and look for </song> and </lyrics> or </set> and </slide_groups>
                boolean isOpenSong = false;
                boolean isOpenSongSet = false;
                String content = "";
                try {
                    InputStream inputStream = getContentResolver().openInputStream(importUri);
                    if (!importFilename.contains(".") && getStorageAccess().getFileSizeFromUri(importUri) < 200) {
                        content = getStorageAccess().readTextFileToString(inputStream);
                    }
                    if (content != null && content.contains("</song>") && content.contains("</lyrics>")) {
                        isOpenSong = true;
                    }
                    if (content != null && content.contains("</set>") && content.contains("</slide_groups>")) {
                        isOpenSongSet = true;
                    }
                    if (isOpenSongSet) {
                        setWhattodo("importset");
                    } else if (isOpenSong) {
                        setWhattodo("importsong");
                    }
                    if (isOpenSong || isOpenSongSet) {
                        dealingWithIntent = deeplink_import_file;
                    } else {
                        // Can't handle the file, so delete it
                        File tempFileFolder = getStorageAccess().getAppSpecificFile("Import","","");
                        getStorageAccess().emptyFileFolder(tempFileFolder);
                        setWhattodo("");
                        dealingWithIntent = "";
                        getShowToast().doIt(unknown);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (dealingWithIntent != null && !dealingWithIntent.isEmpty()) {
                String deeplink = dealingWithIntent;
                getMainHandler().post(() -> navigateToFragment(deeplink, 0));
                // Reset the flag to allow dealing with a new intent as we have handled this one
                getPreferences().setMyPreferenceBoolean("intentAlreadyDealtWith", false);
            }
        } else {
            setWhattodo("");
            getShowToast().doIt(unknown);
        }
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
    public OpenChordsAPI getOpenChordsAPI() {
        if (openChordsAPI==null) {
            openChordsAPI = new OpenChordsAPI(this);
        }
        return openChordsAPI;
    }
    @Override
    public ConvertJustChords getConvertJustChords() {
        if (convertJustChords == null) {
            convertJustChords = new ConvertJustChords(this);
        }
        return convertJustChords;
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
    public Variations getVariations() {
        if (variations==null) {
            variations = new Variations(this);
        }
        return variations;
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
            timeTools = new TimeTools(this);
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

        // Check if the song is in the set
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
                    setMenuFragment.removeHighlight();
                }

                // Get the set item
                SetItemInfo setItemInfo = getCurrentSet().getSetItemInfo(position);
                String setFolder = setItemInfo.songfolder;
                String setFilename = setItemInfo.songfilename;
                String setKey = setItemInfo.songkey;
                Uri setUri = getStorageAccess().getUriForItem("Songs",setFolder,setFilename);

                if (setItemInfo.songfilename.equals(getSetActions().getDividerIdentifier())) {
                    // Exit here!
                    return;
                }
                // If we are viewing a set item with a temp key change, we will need these variables
                String[] bits = getVariations().getPreVariationInfo(setItemInfo);
                String originalFolder = bits[0];
                String originalFilename = bits[1];
                String originalKey = bits[2];
                Uri originalUri = getStorageAccess().getUriForItem("Songs",originalFolder,originalFilename);

                // Determine if this is a variation file based on the filename
                boolean isNormalVariation = getVariations().getIsNormalVariation(setFolder, setFilename);

                // Create a null/empty song object in case we need to load it to get the key or transpose
                Song quickSong = null;

                // Get the key of the song from the file
                if (getStorageAccess().isSpecificFileExtension("imageorpdf",setFilename)) {
                    // This is a pdf, we query the persistent database
                    originalKey = nonOpenSongSQLiteHelper.getKey(setFolder,setFilename);
                } else if (isNormalVariation) {
                    if (getStorageAccess().uriExists(setUri)) {
                        // We are a variation and the file already exists.
                        // We can get the key from the variation file
                        quickSong = new Song();
                        quickSong.setFolder(setFolder);
                        quickSong.setFilename(setFilename);
                    } else if (getStorageAccess().uriExists(originalUri)) {
                        // The variation file doesn't exist yet
                        // We can get the original file
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
                    originalKey = sqLiteHelper.getKey(setFolder, setFilename);
                }

                boolean isKeyVariation = setKey!=null && originalKey!=null && !setKey.isEmpty() &&
                                !originalKey.isEmpty() && !setKey.equals(originalKey);

                if (isKeyVariation) {
                    // Could be just a key variation, or a standard variation needing adjusted

                    boolean needToTranspose = false;
                    Uri targetUri;
                    String targetFilename;

                    if (isNormalVariation) {
                        // We must already have the variation file, so we can edit directly
                        needToTranspose = true;

                    } else {
                        // Look for an already created key Variation file so we don't need to do it again
                        targetFilename = getVariations().getKeyVariationFilename(originalFolder,originalFilename,setKey);
                        targetUri = getVariations().getKeyVariationUri(targetFilename);

                        if (!getStorageAccess().uriExists(targetUri)) {
                            needToTranspose = true;
                        }

                        // We adjust the folder and filename on a temporary basis
                        // This isn't used in the set, just in the loading process
                        setFolder = getVariations().getKeyVariationsFolder();
                        setFilename = targetFilename;
                    }

                    if (needToTranspose) {
                        // The set has specified a key that is different from our song
                        if (quickSong == null) {
                            // This was a straightforward song (i.e. not a standard variation)
                            // Get the song object from the database
                            if (getStorageAccess().isSpecificFileExtension("imageorpdf", setFilename)) {
                                quickSong = nonOpenSongSQLiteHelper.getSpecificSong(originalFolder, originalFilename);
                            } else {
                                quickSong = sqLiteHelper.getSpecificSong(originalFolder, originalFilename);
                            }
                        } else if (quickSong.getLyrics() == null || quickSong.getLyrics().isEmpty()) {
                            quickSong = getLoadSong().doLoadSong(quickSong, false);
                        }
                        getVariations().makeKeyVariation(quickSong,setKey,false, !isNormalVariation);

                    } else if (!getVariations().getIsNormalOrKeyVariation(setFolder,setFilename)) {
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
            setMenuFragment.updateItem(setPosition);
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
                case "syncNearbyZip":
                    // If we are about to import files from a zip file and overwrite our files
                    if (arguments!=null && arguments.size()==2) {
                        Uri zipUri = Uri.parse(arguments.get(0));
                        String which = arguments.get(1);
                        ((SyncNearbyFragment) callingFragment).doExtractFromZip(zipUri, which);
                    }
                    allowToast = false;
                    break;
                case "addUUIDLastMod":
                    // This checks songs for missing UUID or lastModified values
                    // If the aren't found, they get added and the song saved again
                    getSongListBuildIndex().setCheckForUUIDLastMod(true);
                    getSongListBuildIndex().setFullIndexRequired(true);
                    getSongListBuildIndex().setIndexRequired(true);
                    getSongListBuildIndex().buildBasicFromFiles();
                    indexSongs();
                    navHome();
                    break;

                case "deleteSong":
                    getStorageAccess().updateFileActivityLog(TAG + " confirmedAction deleteFile Songs/" + song.getFolder() + "/" + song.getFilename());
                    result = getStorageAccess().doDeleteFile("Songs",
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
                    result = getStorageAccess().doDeleteFile(arguments.get(0), arguments.get(1), arguments.get(2));
                    if (arguments.get(2)!=null && arguments.get(2).isEmpty() && arguments.get(0)!=null && arguments.get(0).equals("Songs") &&
                            (arguments.get(1)==null || arguments.get(1).isEmpty())) {
                        // Emptying the entire songs foler, so need to recreate it on finish.
                        getStorageAccess().createFolder("Songs", "", "", false);
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
                    getSetActions().setUseThisLastModifiedDate(getTimeTools().getNowIsoTime());
                    String xml = getSetActions().createSetXML(getCurrentSet());
                    getSetActions().setUseThisLastModifiedDate(null);

                    String setString = getSetActions().getSetAsPreferenceString();
                    result = getStorageAccess().doStringWriteToFile("Sets", "", currentSet.getSetCurrentLastName(), xml);
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
                    // We are renaming a set or
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

                case "restorePersistentDatabase":
                    if (callingFragment!=null) {
                        try {
                            ((DatabaseUtilitiesFragment) callingFragment).doImportDatabaseBackup();
                            result = true;
                            allowToast = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "NearbyAdvertise":
                    // We have accepted our advertise settings, so continue
                    getNearbyActions().getNearbyConnectionManagement().setUsingNearby(true);
                    if (getNearbyActions().getNearbyConnectionManagement().getNearbyTemporaryAdvertise()) {
                        getNearbyActions().getNearbyConnectionManagement().doTempAdvertise();
                    } else {
                        getNearbyActions().getNearbyConnectionManagement().startAdvertising();
                    }
                    allowToast = false;
                    break;

                case "NearbyDiscover":
                    // We have accepted our discover settings, so continue
                    getNearbyActions().getNearbyConnectionManagement().setUsingNearby(true);
                    getNearbyActions().getNearbyConnectionManagement().doTempDiscover();
                    allowToast = false;
                    break;

                case "openChordsForcePush":
                case "openChordsForcePull":
                    if (callingFragment!=null) {
                        try {
                            ((OpenChordsFragment)callingFragment).doForceChanges(what);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    allowToast = false;
                    break;

                case "addallsongstoset":
                    if (callingFragment!=null && songMenuFragment!=null) {
                        try {
                            songMenuFragment.addAllSongsToSet();
                            result = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }

            if (allowToast && result && getResources() != null) {
                // Don't show toast for exit, but other successful actions
                getShowToast().doIt(success);
            } else if (allowToast && getResources() != null) {
                getShowToast().doIt(error);
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
    public void fullIndex(String specificFolder) {
        if (songListBuildIndex.getIndexRequired() && !songListBuildIndex.getCurrentlyIndexing()) {
            getShowToast().doIt(search_index_start);
            getThreadPoolExecutor().execute(() -> {
                String outcome = songListBuildIndex.fullIndex(songMenuFragment.getProgressText(),specificFolder);
                if (songMenuFragment != null && !songMenuFragment.isDetached()) {
                    try {
                        songMenuFragment.updateSongMenu();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mainLooper.post(() -> {
                    if (outcome!=null && !outcome.isEmpty()) {
                        getShowToast().doIt(outcome.trim());
                    }
                    updateFragment("set_updateKeys", null, null);
                });
            });
        }
    }

    @Override
    public void quickSongMenuBuild() {
        if (getStorageAccess()!=null && sqLiteHelper!=null && nonOpenSongSQLiteHelper!=null) {
            ArrayList<String> songIds = new ArrayList<>();
            try {
                songIds = getStorageAccess().listSongs(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Write a crude text file (line separated) with the song Ids (folder/file)
            getStorageAccess().writeSongIDFile(songIds);

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
        // Remove any existing toasts
        if (showToast!=null) {
            showToast.kill();
        }
        // Check we have a toast helper
        if (showToast == null) {
            showToast = new ShowToast(this, myView.toastBox);
        }
        // Sent the helper reference
        return showToast;
    }

    @Override
    public String getMode() {
        if (whichMode == null) {
            whichMode = getPreferences().getMyPreferenceString("whichMode", performance);
        }
        return whichMode;
    }

    @Override
    public void setMode(String whichMode) {
        this.whichMode = whichMode;
    }

    @Override
    public FixLocale getFixLocale() {
        if (fixLocale == null) {
            fixLocale = new FixLocale(this);
        }
        return fixLocale;
    }

    @Override
    public Locale getLocale() {
        if (locale == null && fixLocale!=null) {
            fixLocale.setLocale();
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
    public OpenSongSetBundle getOpenSongSetBundle() {
        if (openSongSetBundle==null) {
            openSongSetBundle = new OpenSongSetBundle(this);
        }
        return openSongSetBundle;
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
            autoscroll = new Autoscroll(this, myView.onScreenInfo, myView.onScreenInfo.getAutoscrollTime(),
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
        if (bitmap==null && screenshotFile!=null) {
            Log.d(TAG,"Deleting old screenshot:"+screenshotFile.delete());
        } else if (screenshotFile!=null) {
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
    public LocalWiFiHost getLocalWiFiHost() {
        if (localWiFiHost == null && Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            localWiFiHost = new LocalWiFiHost(this);
        }
        return localWiFiHost;
    }

    @Override
    public void openDocument(String location) {
        // Most locations are passed in from the string.xml file.  They are listed under website_xxx
        // Otherwise they are created on the fly (for link files, importing songs, etc).
        if (location != null) {
            try {
                CustomTabsIntent customTabsIntent = null;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (location.startsWith("http")) {
                    if (!location.contains("https://www.google.com/search?q=")) {
                        // Not searching, so just display the webpage in the default browser
                        Bitmap myCustomCloseIcon = null;
                        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.arrow_left);
                        if (drawable!=null) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                drawable = (DrawableCompat.wrap(drawable)).mutate();
                                drawable.setColorFilter(getPalette().onPrimary, PorterDuff.Mode.SRC_IN);
                            } else {
                                drawable.setTint(getPalette().onPrimary);
                            }

                            myCustomCloseIcon = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(myCustomCloseIcon);
                            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                            drawable.draw(canvas);
                        }
                        if (myCustomCloseIcon!=null) {
                            customTabsIntent = new CustomTabsIntent.Builder().setDefaultColorSchemeParams(new CustomTabColorSchemeParams.Builder()
                                            .setToolbarColor(getPalette().primary).build()).setShowTitle(true).
                                    setCloseButtonIcon(myCustomCloseIcon).setUrlBarHidingEnabled(true).build();
                        } else {
                            customTabsIntent = new CustomTabsIntent.Builder().setDefaultColorSchemeParams(new CustomTabColorSchemeParams.Builder()
                                            .setToolbarColor(getPalette().primary).build()).setShowTitle(true).
                                    setUrlBarHidingEnabled(true).build();
                        }
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse(location));

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
                if (customTabsIntent==null) {
                    startActivity(intent);
                }
            } catch (ActivityNotFoundException nf) {
                // No suitable application to open the document
                getShowToast().doIt(no_suitable_application);
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
            updateDisplay("showSection");
        } else {
            getNearbyActions().getNearbyReceivePayloads().setPendingSection(i);
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
                getFixLocale().setLocale();

                forceReload = true;

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
        // GE - remove view states as these should be recreated anyway
        // Start with a new bundle and only store relevant variables
        outState.clear();
        outState = new Bundle();
        outState.putBoolean("bootUpCompleted", bootUpCompleted);
        if (songListBuildIndex!=null) {
            outState.putBoolean("indexComplete", songListBuildIndex.getIndexComplete());
        } else {
            outState.putBoolean("indexComplete",false);
        }

        // If we were using nearby, keep a reference of known devices and a call to restart it
        if (getNearbyActions().getNearbyConnectionManagement().getUsingNearby() &&
                getNearbyActions().getNearbyConnectionManagement().hasValidConnections()) {
            // Note we were using
            outState.putBoolean("usingNearby", getNearbyActions().getNearbyConnectionManagement().getUsingNearby());
            // Are we a host or client?
            outState.putBoolean("isHost", getNearbyActions().getNearbyConnectionManagement().getIsHost());
            // What connections did we have
            outState.putStringArrayList("discoveredEndpoints", getNearbyActions().getNearbyConnectionManagement().getBundleDiscoveredDevices());
            outState.putStringArrayList("connectedEndpoints", getNearbyActions().getNearbyConnectionManagement().getBundleConnectedDevices());
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
        }

        if (bootUpCompleted) {
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
        File[] files = tempLoc.listFiles();
        if (files != null) {
            for (File file : files) {
                Log.d(TAG, "Deleted temp import file " + file + ":" + file.delete());
            }
        }
        getShowToast().kill();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // If we were running a local WiFi host, turn it off
        getLocalWiFiHost().stopLocalWifi();

        // If we were running a local webServer, turn it off
        getWebServer().stop();

        // Clear any toasts
        getShowToast().kill();

        // Turn off nearby
        getNearbyActions().getNearbyConnectionManagement().turnOffNearby();

        // Stop and clear the metronome
        getMetronome().releaseSoundPool();

        // Reset the dealt with intent
        try {
            getPreferences().setMyPreferenceBoolean("intentAlreadyDealtWith", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If we had a bluetooth MIDI device, cancel the connection and unpair
        // Also stop any MIDI clock
        getMidi().stopMidiClock();
        getMidi().tryDisconnectBluetoothLE();

        getMultiTrackPlayer().closeMultitrack();

        // Clear out the export and received folders
        getStorageAccess().wipeFolder("Export","");
        getStorageAccess().wipeFolder("Received","");

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

    private boolean updatingIcon = false;
    private final Runnable doCastUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!updatingIcon) {
                updatingIcon = true;
                if (menuScreenMirror != null) {
                    if (settingsOpen || !getAlertChecks().getHasPlayServices()) {
                        menuScreenMirror.setVisible(false);
                    } else {
                        menuScreenMirror.setIcon(secondaryDisplays != null && connectedDisplays.length > 0 ? castIconOn:castIconOff);
                        menuScreenMirror.setVisible(true);
                    }
                }
            }
        }
    };
    private final Runnable allowCastUpdateRunnable = () -> updatingIcon = false;
    private final Handler doCastUpdateHandler = new Handler(Looper.getMainLooper());
    private void updateCastIcon() {
        // Clear previous actions
        doCastUpdateHandler.removeCallbacks(doCastUpdateRunnable);
        doCastUpdateHandler.removeCallbacks(allowCastUpdateRunnable);

        // Update the action (will check if not already doing this within 1 sec)
        doCastUpdateHandler.post(doCastUpdateRunnable);
        doCastUpdateHandler.postDelayed(allowCastUpdateRunnable, 1000);
    }

    private void setupDisplays() {
        // Go through each connected display and create the secondaryDisplay Presentation class
        // Check there aren't any already connected, if there are, dismiss them
        if (secondaryDisplays != null) {
            for (SecondaryDisplay secondaryDisplay : secondaryDisplays) {
                if (secondaryDisplay != null && secondaryDisplay.isShowing()) {
                    try {
                        getMainHandler().post(secondaryDisplay::dismiss);
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
            getMainHandler().post(() -> {
                for (int c = 0; c < connectedDisplays.length; c++) {
                    secondaryDisplays[c] = new SecondaryDisplay(this, connectedDisplays[c]);
                    secondaryDisplays[c].show();

                }
            });
        }

        // Update cast icon
        updateCastIcon();
    }

    @Override
    public boolean getIsSecondaryDisplaying() {
        return secondaryDisplays != null && secondaryDisplays.length > 0;
    }

    @Override
    public SecondaryDisplay[] getSecondaryDisplays() {
        return secondaryDisplays;
    }

    @Override
    public void updateDisplay(String what) {

        // Make sure everything here happens on the main UI thread
        getMainHandler().post(() -> {
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
                                    if (song.getFiletype() != null && song.getFiletype().equals("PDF")) {
                                        secondaryDisplay.showSection(song.getPdfPageCurrent());
                                    } else {
                                        secondaryDisplay.showSection(song.getCurrentSection());
                                    }
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
        });
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
                        getMainHandler().post(() -> secondaryDisplay.showSection(position));
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