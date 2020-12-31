package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.animation.PageButtonFAB;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.appdata.FixLocale;
import com.garethevans.church.opensongtablet.appdata.SetTypeFace;
import com.garethevans.church.opensongtablet.appdata.VersionNumber;
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.ccli.SettingsCCLI;
import com.garethevans.church.opensongtablet.controls.PedalActions;
import com.garethevans.church.opensongtablet.controls.PedalsFragment;
import com.garethevans.church.opensongtablet.databinding.ActivityMainBinding;
import com.garethevans.church.opensongtablet.databinding.AppBarMainBinding;
import com.garethevans.church.opensongtablet.export.ExportActions;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureDialogFragment;
import com.garethevans.church.opensongtablet.filemanagement.EditSongFragment;
import com.garethevans.church.opensongtablet.filemanagement.EditSongFragmentMain;
import com.garethevans.church.opensongtablet.filemanagement.ExportFiles;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.filemanagement.StorageManagementFragment;
import com.garethevans.church.opensongtablet.interfaces.DialogReturnInterface;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.LoadSongInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.MidiAdapterInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyInterface;
import com.garethevans.church.opensongtablet.interfaces.ShowCaseInterface;
import com.garethevans.church.opensongtablet.midi.Midi;
import com.garethevans.church.opensongtablet.midi.MidiFragment;
import com.garethevans.church.opensongtablet.nearby.NearbyConnections;
import com.garethevans.church.opensongtablet.nearby.NearbyConnectionsFragment;
import com.garethevans.church.opensongtablet.performance.PerformanceFragment;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.presentation.PresentationFragment;
import com.garethevans.church.opensongtablet.screensetup.AppActionBar;
import com.garethevans.church.opensongtablet.screensetup.BatteryStatus;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.screensetup.WindowFlags;
import com.garethevans.church.opensongtablet.secondarydisplay.MediaRouterCallback;
import com.garethevans.church.opensongtablet.secondarydisplay.MySessionManagerListener;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ConvertTextSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songsandsets.SetMenuFragment;
import com.garethevans.church.opensongtablet.songsandsets.SongListBuildIndex;
import com.garethevans.church.opensongtablet.songsandsets.SongMenuFragment;
import com.garethevans.church.opensongtablet.songsandsets.ViewPagerAdapter;
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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Objects;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.make;

//TODO Fix unused and local

public class MainActivity extends AppCompatActivity implements LoadSongInterface,
        ShowCaseInterface, MainActivityInterface, MidiAdapterInterface, EditSongFragmentInterface,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, DialogReturnInterface,
        NearbyInterface {

    ActivityMainBinding activityMainBinding;
    AppBarMainBinding appBarMainBinding;

    private AppBarConfiguration mAppBarConfiguration;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private ThemeColors themeColors;
    private SetTypeFace setTypeFace;
    private SQLiteHelper sqLiteHelper;
    private CommonSQL commonSQL;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private ConvertChoPro convertChoPro;
    private ConvertOnSong convertOnSong;
    private ConvertTextSong convertTextSong;
    private ProcessSong processSong;
    private LoadSong loadSong;
    private Song song;
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
    private PageButtonFAB pageButtonFAB;
    private boolean pageButtonActive = true;
    private PedalActions pedalActions;
    private ExportActions exportActions;

    private ArrayList<View> targets;
    private ArrayList<String> infos, dismisses;
    private ArrayList<Boolean> rects;

    public MediaPlayer mediaPlayer1, mediaPlayer2;

    // Important fragments we get references to (for calling methods)
    PerformanceFragment performanceFragment;
    PresentationFragment presentationFragment;
    SongMenuFragment songMenuFragment;
    SetMenuFragment setMenuFragment;
    EditSongFragment editSongFragment;
    EditSongFragmentMain editSongFragmentMain;
    NearbyConnectionsFragment nearbyConnectionsFragment;
    Fragment registeredFragment;
    int fragmentOpen;

    NavController navController;

    ActionBar ab;
    DrawerLayout drawerLayout;

    MenuItem settingsButton;

    // Network discovery / connections
    NearbyConnections nearbyConnections;

    // Casting
    private CastContext castContext;
    private MySessionManagerListener sessionManagerListener;
    private CastSession castSession;
    private MenuItem mediaRouteMenuItem;
    private CastStateListener castStateListener;
    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private MediaRouterCallback mediaRouterCallback;
    private CastDevice castDevice;
    private SessionManager sessionManager;
    //private PresentationServiceHDMI hdmi;

    ViewPagerAdapter adapter;
    ViewPager2 viewPager;
    boolean showSetMenu;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        appBarMainBinding = activityMainBinding.appBarMain;

        View view = activityMainBinding.getRoot();
        view.setOnSystemUiVisibilityChangeListener(
                visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        setWindowFlags();
                    }
                });

        setContentView(view);
        setSupportActionBar(activityMainBinding.appBarMain.myToolBarNew.myToolBarNew);
        ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = activityMainBinding.drawerLayout;

        // Fragment work
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_boot, R.id.nav_performance, R.id.nav_presentation)
                .setOpenableLayout(activityMainBinding.drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(activityMainBinding.navView, navController);


        // Initialise the helpers used for heavy lifting
        initialiseHelpers();

        // Set the fullscreen window flags
        setWindowFlags();

        // Get the version
        versionNumber = new VersionNumber();

        // Initialise the start variables we need
        initialiseStartVariables();

        // Battery monitor
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        this.registerReceiver(batteryStatus, filter);

        // Set up the Nearby connection service
        nearbyConnections.getUserNickname();
    }

    // Initialise the MainActivity components
    // Some of the set up only happens once the user has passed the BootUpFragment checks
    @Override
    public void initialiseActivity() {

        lockDrawer(false);
        hideActionButton(false);
        hideActionBar(false);

        // We don't want the back button to bring us back to the splash screen, so pop it from the backstack
        navController.popBackStack(R.id.nav_boot,true);

        switch (StaticVariables.whichMode) {
            case "Performance":
            case "Stage":
            default:
                navigateToFragment(R.id.nav_performance);
                break;

            case "Presentation":
                navigateToFragment(R.id.nav_presentation);
                break;
        }

        // Set up song / set menu tabs
        setUpSongMenuTabs();

        // Set the version in the menu
        versionNumber.updateMenuVersionNumber(this, activityMainBinding.menuTop.versionCode);

        // Set up page buttons
        setListeners();

        // Initialise the CastContext
        setUpCast();

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
            Log.d("MainActivity","No Google Services");
        }
    }
    private void recoverCastState() {
        castSession = sessionManager.getCurrentCastSession();
        sessionManager.addSessionManagerListener(new MySessionManagerListener(this));
    }
    private void endCastState() {
        sessionManager.removeSessionManagerListener(sessionManagerListener);
        castSession = null;
    }
    private void initialiseHelpers() {
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        setTypeFace = new SetTypeFace();
        themeColors = new ThemeColors();
        sqLiteHelper = new SQLiteHelper(this);
        nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(this);
        commonSQL = new CommonSQL();
        convertChoPro = new ConvertChoPro();
        convertOnSong = new ConvertOnSong();
        convertTextSong = new ConvertTextSong();
        processSong = new ProcessSong();
        loadSong = new LoadSong();
        windowFlags = new WindowFlags(this.getWindow());
        appActionBar = new AppActionBar();
        versionNumber = new VersionNumber();
        fixLocale = new FixLocale();
        ccliLog = new CCLILog();
        exportFiles = new ExportFiles();
        showCase = new ShowCase();
        showToast = new ShowToast();
        pageButtonFAB = new PageButtonFAB(activityMainBinding.pageButtonsRight.actionFAB, activityMainBinding.pageButtonsRight.custom1Button,
                activityMainBinding.pageButtonsRight.custom2Button,activityMainBinding.pageButtonsRight.custom3Button,
                activityMainBinding.pageButtonsRight.custom4Button,activityMainBinding.pageButtonsRight.custom5Button,
                activityMainBinding.pageButtonsRight.custom6Button);
        pageButtonFAB.animatePageButton(this,false);
        nearbyConnections = new NearbyConnections(this,preferences,storageAccess,processSong,sqLiteHelper,commonSQL,song);
        midi = new Midi(this,preferences);
        pedalActions = new PedalActions(this,preferences);
        exportActions = new ExportActions();
        song = new Song();
        mediaRouterCallback = new MediaRouterCallback();
    }
    private void initialiseStartVariables() {
        StaticVariables.mDisplayTheme = preferences.getMyPreferenceString(this, "appTheme", "dark");
        StaticVariables.whichMode = preferences.getMyPreferenceString(this, "whichMode", "Performance");

        // Song location
        StaticVariables.songfilename = preferences.getMyPreferenceString(this,"songfilename","Welcome to OpenSongApp");
        StaticVariables.whichSongFolder = preferences.getMyPreferenceString(this, "whichSongFolder", getString(R.string.mainfoldername));

        // Set
        StaticVariables.currentSet = preferences.getMyPreferenceString(this,"setCurrent","");
        // TODO
        StaticVariables.currentSet = "$**_Abba Father_**$$**_Above all_**$";

        // Set the locale
        fixLocale.fixLocale(this,preferences);

        // MediaPlayer
        mediaPlayer1 = new MediaPlayer();
        mediaPlayer2 = new MediaPlayer();

        // ThemeColors
        themeColors.getDefaultColors(this,preferences);

        // Typefaces
        setTypeFace.setUpAppFonts(this,preferences,new Handler(),new Handler(),new Handler(),new Handler(),new Handler());
    }
    private void initialiseArrayLists() {
        targets = new ArrayList<>();
        dismisses = new ArrayList<>();
        infos = new ArrayList<>();
        rects = new ArrayList<>();
    }
    private void setListeners() {
        activityMainBinding.pageButtonsRight.actionFAB.setOnClickListener(v  -> {
            if (pageButtonActive) {
                pageButtonActive = false;
                Handler h = new Handler();
                h.postDelayed(() -> pageButtonActive = true,600);
                animatePageButtons();
            }
        });
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                showTutorial("songsetmenu");
                activityMainBinding.pageButtonsRight.actionFAB.hide();
                if (setSongMenuFragment()) {
                    showTutorial("songsetMenu");
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (performanceFragment!=null && performanceFragment.isVisible()) {
                    activityMainBinding.pageButtonsRight.actionFAB.show();
                }
                hideKeyboard();
            }

            @Override
            public void onDrawerStateChanged(int newState) {}
        });
    }
    private void animatePageButtons() {
        float rotation = activityMainBinding.pageButtonsRight.actionFAB.getRotation();
        pageButtonFAB.animatePageButton(this, rotation == 0);
    }
    void setWindowFlags() {
        // Fix the page flags
        if (windowFlags==null) {
            windowFlags = new WindowFlags(this.getWindow());
        }
        windowFlags.setWindowFlags();
    }

    private boolean setSongMenuFragment() {
        runOnUiThread(() -> {
            if (songMenuFragment!=null) {
                songMenuFragment.showActionButton(true);
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
        }
    }

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
                Log.d("MainActivity", "No need to close battery monitor");
            }
        }
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer(true);
        } else if (StaticVariables.homeFragment) {
            Log.d("d","In the home fragment, so deal with back pressed");
        } else {
            Log.d("d","Let Android deal with back press based on backstack");
            super.onBackPressed();
        }
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Get the language
        fixLocale.fixLocale(this,preferences);

        // Save a static variable that we have rotated the screen.
        // The media player will look for this.  If found, it won't restart when the song loads
        StaticVariables.orientationChanged = StaticVariables.currentScreenOrientation!=newConfig.orientation;

        // If orientation has changed, we need to reload the song to get it resized.
        if (StaticVariables.orientationChanged) {
            // Set the current orientation
            StaticVariables.currentScreenOrientation = newConfig.orientation; // Set the current orientation
            closeDrawer(true);
            doSongLoad();
        }
    }

    @Override
    protected void onPause() {
        endCastState();
        super.onPause();
    }
    @Override
    protected void onResume() {
        setUpCast();

        // Fix the page flags
        setWindowFlags();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nearbyConnections.turnOffNearby();
    }

    // The toolbar/menu items
    @Override
    public void updateToolbar(Song song, String title) {
        // Null titles are for the default song, author, etc.  A song is sent instead
        // Otherwise a new title is passed as a string and a null song is sent
        windowFlags.setWindowFlags();
        appActionBar.setActionBar(activityMainBinding.appBarMain.myToolBarNew.songtitleAb,
                activityMainBinding.appBarMain.myToolBarNew.songauthorAb,
                activityMainBinding.appBarMain.myToolBarNew.songkeyAb,song,title);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        settingsButton = (MenuItem) menu.findItem(R.id.settings_menu_item).getActionView();
        MenuItem nearbyConnectButton = (MenuItem) menu.findItem(R.id.nearby_menu_item).getActionView();

        // Decide if we should hide the Google Nearby button
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            nearbyConnectButton.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("d","item="+item);
        switch (item.toString()) {
            case "Settings":
                if (fragmentOpen != R.id.nav_preference) {
                    //hideActionButton(true);
                    navigateToFragment(R.id.nav_preference);
                    //showingsettings = true;
                } else {
                    navHome();
                }
                break;

            case "Nearby":
                if (requestNearbyPermissions() && fragmentOpen!=R.id.nearbyConnectionsFragment) {
                    navigateToFragment(R.id.nearbyConnectionsFragment);
                } else if (fragmentOpen==R.id.nearbyConnectionsFragment){
                    navHome();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navHome() {
        if (StaticVariables.whichMode.equals("Presentation")) {
            navigateToFragment(R.id.nav_presentation);
            //hideActionButton(true);
        } else {
            navigateToFragment(R.id.nav_performance);
            //hideActionButton(false);
        }
        //showingsettings = false;
        StaticVariables.whattodo = "";
        //getSupportFragmentManager().popBackStack();
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
            Log.d("MainActivity","Google Play Services Available");
            mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        } else {
            Log.d("MainActivity","Google Play Services Available");
        }

        // Set up battery monitor
        batteryStatus = new BatteryStatus();
        batteryStatus.setUpBatteryMonitor(this,preferences,activityMainBinding.appBarMain.myToolBarNew.digitalclock,
                activityMainBinding.appBarMain.myToolBarNew.batterycharge,
                activityMainBinding.appBarMain.myToolBarNew.batteryimage,ab);

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
                showCase.singleShowCase(this,activityMainBinding.pageButtonsRight.actionFAB,
                        null,getString(R.string.action_button_info),false,"pageActionFAB");
                break;
            case "songsetmenu":
                // Initialise the arraylists
                initialiseArrayLists();
                if (activityMainBinding != null) {
                    targets.add(Objects.requireNonNull(activityMainBinding.menuTop.tabs.getTabAt(0)).view);
                    targets.add(Objects.requireNonNull(activityMainBinding.menuTop.tabs.getTabAt(1)).view);
                }
                infos.add(getString(R.string.menu_song_info));
                infos.add(getString(R.string.menu_set_info));
                dismisses.add(null);
                dismisses.add(null);
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
            songMenuFragment.moveToSongInMenu();
        } else {
            Log.d("MainActivity", "songMenuFragment not available");
        }
    }
    @Override
    public void indexSongs() {
        new Thread(() -> {
            runOnUiThread(() -> showToast.doIt(this,getString(R.string.search_index_start)));
            SongListBuildIndex songListBuildIndex = new SongListBuildIndex();
            songListBuildIndex.fullIndex(MainActivity.this,preferences,storageAccess,sqLiteHelper,
                    nonOpenSongSQLiteHelper,commonSQL,processSong,convertChoPro,convertOnSong,convertTextSong,showToast,loadSong);
            runOnUiThread(() -> {
                StaticVariables.indexRequired = false;
                StaticVariables.indexComplete = true;
                showToast.doIt(this,getString(R.string.search_index_end));
                if (setSongMenuFragment()) {
                    songMenuFragment.updateSongMenu();
                } else {
                    Log.d("MainActivity", "songMenuFragment not available");
                }
            });
        }).start();
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
    public void loadSongFromSet() {
        Log.d("MainActivity","loadSongFromSet() called");
    }
    @Override
    public void refreshAll() {
        Log.d("MainActivity","refreshAll() called");
    }
    @Override
    public void doSongLoad() {
        Log.d("MainActivity","doSongLoad() called");

        if (StaticVariables.whichMode.equals("Presentation")) {
            if (presentationFragment!=null && presentationFragment.isAdded()) {
                presentationFragment.doSongLoad();
            } else {
                Log.d("MainActivity", "presentationFragment not available");
            }
        } else {
            if (performanceFragment!=null && performanceFragment.isAdded()) {
                performanceFragment.doSongLoad();
            } else {
                Log.d("MainActivity", "performanceFragment not available");
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
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        navigateToFragment(caller.getId());
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
            songMenuFragment.showActionButton(show);
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
        if (lock) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }
    @Override
    public void hideActionButton(boolean hide) {
        if (hide) {
            activityMainBinding.pageButtonsRight.actionFAB.hide();
            activityMainBinding.pageButtonsRight.bottomButtons.setVisibility(View.GONE);
        } else {
            activityMainBinding.pageButtonsRight.actionFAB.show();
            activityMainBinding.pageButtonsRight.bottomButtons.setVisibility(View.VISIBLE);
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
    public void navigateToFragment(int id) {
        closeDrawer(true);  // Only the Performance and Presentation fragments allow this.  Switched on in these fragments
        lockDrawer(true);
        hideActionButton(true);

        try {
            navController.navigate(id);
            Fragment f = getSupportFragmentManager().findFragmentById(id);
            Log.d("MainActivity","f="+f);
            fragmentOpen = id;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void openDialog(DialogFragment frag, String tag) {
        frag.show(getSupportFragmentManager(), tag);
    }
    @Override
    public void updateFragment(String fragName, Fragment callingFragment, ArrayList<String> arguments) {
        if (fragName!=null) {
            switch (fragName) {
                case "StorageManagementFragment":
                    ((StorageManagementFragment)callingFragment).updateFragment();
                    break;

                case "createNewSong":
                    // User was in song menu dialog, clicked on create, then entered a new file name
                    // Check this was successful (saved as arguments)
                    if (arguments!=null && arguments.size()>0 && arguments.get(0).equals("success")) {
                        // Write a blank xml file with the song name in it
                        Song song = new Song();
                        song = song.initialiseSong(commonSQL);
                        String newSongText = song.getXML(song,processSong);
                        if (storageAccess.doStringWriteToFile(this,preferences,"Songs",StaticVariables.whichSongFolder, StaticVariables.songfilename,newSongText)) {
                            navigateToFragment(R.id.nav_editSong);
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
                        if (storageAccess.doStringWriteToFile(this,preferences,"Songs",StaticVariables.whichSongFolder,StaticVariables.songfilename,arguments.get(1))) {
                            doSongLoad();
                        } else {
                            ShowToast.showToast(this,getString(R.string.error));
                        }
                    }
            }
        }
    }
    @Override
    public void returnToHome(Fragment fragment, Bundle bundle) {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(fragment.getId(), true)
                .build();
        if (StaticVariables.whichMode.equals("Presentation")) {
            fragmentOpen = R.id.nav_presentation;
            NavHostFragment.findNavController(fragment)
                    .navigate(R.id.nav_presentation,bundle,navOptions);
        } else {
            fragmentOpen = R.id.nav_performance;
            NavHostFragment.findNavController(fragment)
                    .navigate(R.id.nav_performance,bundle,navOptions);
        }

    }
    @Override
    public void displayAreYouSure(String what, String action, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song) {
        AreYouSureDialogFragment dialogFragment = new AreYouSureDialogFragment(what,action,arguments,fragName,callingFragment,song);
        dialogFragment.show(this.getSupportFragmentManager(), "areYouSure");
    }
    @Override
    public void confirmedAction(boolean agree, String what, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song) {
        Log.d("d","agree="+agree+"  what="+what);
        if (agree) {
            boolean result = false;
            switch(what) {
                case "deleteSong":
                    result = storageAccess.doDeleteFile(this,preferences,"Songs",
                            StaticVariables.whichSongFolder, StaticVariables.songfilename);
                    // Now remove from the SQL database
                    if (song.getFiletype().equals("PDF") || song.getFiletype().equals("IMG")) {
                        nonOpenSongSQLiteHelper.deleteSong(this,commonSQL,storageAccess,preferences,song.getFolder(),song.getFilename());
                    } else {
                        sqLiteHelper.deleteSong(this, commonSQL, song.getFolder(),song.getFilename());
                    }
                    // TODO
                    // Send a call to reindex?
                    break;

                case "ccliDelete":
                    Uri uri = storageAccess.getUriForItem(this,preferences,"Settings","","ActivityLog.xml");
                    result = ccliLog.createBlankXML(this,preferences,storageAccess,uri);
                    break;

                case "deleteItem":
                    // Folder and subfolder are passed in the arguments.  Blank arguments.get(2) /filenames mean folders
                    result = storageAccess.doDeleteFile(this,preferences,arguments.get(0),arguments.get(1),arguments.get(2));
                    //Rebuild the song index
                    updateSongMenu(fragName, callingFragment, arguments); // Passing the fragment allows an update to be sent to the calling fragment
                    break;

            }
            if (result) {
                ShowToast.showToast(this,getResources().getString(R.string.success));
            } else {
                ShowToast.showToast(this, getResources().getString(R.string.error));
            }
        }
    }
    @Override
    public void updateSongMenu(String fragName, Fragment callingFragment, ArrayList<String> arguments) {
        // If sent called from another fragment the fragName and callingFragment are used to run an update listener
        StaticVariables.indexComplete = false;
        // Get all of the files as an array list
        ArrayList<String> songIds = storageAccess.listSongs(this, preferences);
        // Write this to text file
        storageAccess.writeSongIDFile(this, preferences, songIds);
        // Try to create the basic databases
        sqLiteHelper.resetDatabase(this);
        nonOpenSongSQLiteHelper.initialise(this, commonSQL, storageAccess, preferences);
        // Add entries to the database that have songid, folder and filename fields
        // This is the minimum that we need for the song menu.
        // It can be upgraded asynchronously in StageMode/PresenterMode to include author/key
        // Also will later include all the stuff for the search index as well
        sqLiteHelper.insertFast(this, commonSQL, storageAccess);
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
                intent = exportFiles.exportActivityLog(this, preferences, storageAccess, ccliLog);
                startActivityForResult(Intent.createChooser(intent, "ActivityLog.xml"), 2222);
        }
    }
    @Override
    public void updateSetList() {
        Log.d("MainActivity","Update set list");
    }
    @Override
    public void startAutoscroll (){
        Log.d("MainActivity","Start auto scroll");
    }
    @Override
    public void stopAutoscroll (){
        Log.d("MainActivity","Stop auto scroll");
    }
    @Override
    public void fadeoutPad() {
        Log.d("MainActivity","fadeoutPad()");
    }
    @Override
    public void playPad() {
        Log.d("MainActivity","playPad()");
    }
    @Override
    public void fixOptionsMenu() {invalidateOptionsMenu();}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (intent!=null) {
            if (requestCode == StaticVariables.REQUEST_FILE_CHOOSER) {
                Log.d("MainActivity","File chosen: "+intent.getData());
            }
        }

    }

    // Get references to the objects set in MainActivity
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
    public ConvertChoPro getConvertChoPro() {
        return convertChoPro;
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
    public PedalActions getPedalActions(MainActivityInterface mainActivityInterface) {
        if (mainActivityInterface!=null) {
            pedalActions.setInterface(mainActivityInterface);
        }
        return pedalActions;
    }
    @Override
    public void updateValue(Fragment fragment, String fragname, String which, String value) {
        if (fragment!=null) {
            switch (fragname) {
                case "SettingsCCLI":
                    ((SettingsCCLI)fragment).updateValue(which,value);
                    break;
                case "NearbyConnectionsFragment":
                    ((NearbyConnectionsFragment)fragment).updateValue(which,value);
                    break;
            }
        }
    }


    // Nearby
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
    public void doSendPayloadBytes(String infoPayload) {
        nearbyConnections.doSendPayloadBytes(infoPayload);
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

                make(findViewById(R.id.navView), R.string.location_rationale,
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
                make(findViewById(R.id.mypage), R.string.location_rationale,
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
            if (nearbyConnectionsFragment!=null) {
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
    

    // Get permissions request callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            switch (requestCode) {
                case StaticVariables.REQUEST_CAMERA_CODE:
                    //startCamera();
                    break;

                case 404:
                case 403:
                    // Access fine location, so can open the menu at 'Connect devices'
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

}
