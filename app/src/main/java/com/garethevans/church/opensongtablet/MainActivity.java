package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.mediarouter.app.MediaRouteActionProvider;
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
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.databinding.ActivityMainBinding;
import com.garethevans.church.opensongtablet.databinding.AppBarMainBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureDialogFragment;
import com.garethevans.church.opensongtablet.filemanagement.EditSongFragment;
import com.garethevans.church.opensongtablet.filemanagement.EditSongFragmentMain;
import com.garethevans.church.opensongtablet.filemanagement.ExportFiles;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.filemanagement.StorageManagementFragment;
import com.garethevans.church.opensongtablet.interfaces.LoadSongInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.ShowCaseInterface;
import com.garethevans.church.opensongtablet.performance.PerformanceFragment;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.presentation.PresentationFragment;
import com.garethevans.church.opensongtablet.screensetup.AppActionBar;
import com.garethevans.church.opensongtablet.screensetup.BatteryStatus;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.screensetup.VersionNumber;
import com.garethevans.church.opensongtablet.screensetup.WindowFlags;
import com.garethevans.church.opensongtablet.secondarydisplay.MediaRoute;
import com.garethevans.church.opensongtablet.secondarydisplay.MediaRouterCallback;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ConvertTextSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.garethevans.church.opensongtablet.songsandsets.SetMenuFragment;
import com.garethevans.church.opensongtablet.songsandsets.SongListBuildIndex;
import com.garethevans.church.opensongtablet.songsandsets.SongMenuFragment;
import com.garethevans.church.opensongtablet.songsandsets.ViewPagerAdapter;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLite;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoadSongInterface,
        ShowCaseInterface, MainActivityInterface,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private AppBarConfiguration mAppBarConfiguration;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private SQLite sqLite;
    private SQLiteHelper sqLiteHelper;
    private NonOpenSongSQLite nonOpenSongSQLite;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private ConvertChoPro convertChoPro;
    private ConvertOnSong convertOnSong;
    private ConvertTextSong convertTextSong;
    private ProcessSong processSong;
    private SongXML songXML;
    private ShowCase showCase;
    private ShowToast showToast;
    private WindowFlags windowFlags;
    private BatteryStatus batteryStatus;
    private AppActionBar appActionBar;
    private VersionNumber versionNumber;
    private FixLocale fixLocale;
    private CCLILog ccliLog;
    private ExportFiles exportFiles;
    private PageButtonFAB pageButtonFAB;
    private boolean pageButtonActive = true;
    private MediaRoute mediaRoute;
    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private MediaRouterCallback mediaRouterCallback;

    private ArrayList<View> targets;
    private ArrayList<String> infos, dismisses;
    private ArrayList<Boolean> rects;

    // Important fragments we get references to (for calling methods)
    PerformanceFragment performanceFragment;
    PresentationFragment presentationFragment;
    SongMenuFragment songMenuFragment;
    SetMenuFragment setMenuFragment;
    EditSongFragment editSongFragment;
    EditSongFragmentMain editSongFragmentMain;
    Fragment registeredFragment;

    NavController navController;

    ActionBar ab;
    DrawerLayout drawerLayout;

    MenuItem settingsButton;

    ViewPagerAdapter adapter;
    ViewPager2 viewPager;
    boolean showSetMenu;

    ActivityMainBinding activityMainBinding;
    AppBarMainBinding appBarMainBinding;

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
        //fragmentManager = getSupportFragmentManager();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

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
                navController.navigate(R.id.nav_performance);
                break;

            case "Presentation":
                navController.navigate(R.id.nav_presentation);
                break;
        }

        // Set up song / set menu tabs
        setUpSongMenuTabs();

        // Set the version in the menu
        versionNumber.updateMenuVersionNumber(this, activityMainBinding.menuTop.versionCode);

        // Set up page buttons
        setListeners();

        // Setup the CastContext
        mediaRouter = MediaRouter.getInstance(getApplicationContext());
        mediaRouteSelector = mediaRoute.getMediaRouteSelector();
    }
    private void initialiseHelpers() {
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        sqLite = new SQLite();
        sqLiteHelper = new SQLiteHelper(this);
        nonOpenSongSQLite = new NonOpenSongSQLite();
        nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(this);
        convertChoPro = new ConvertChoPro();
        convertOnSong = new ConvertOnSong();
        convertTextSong = new ConvertTextSong();
        songXML = new SongXML();
        processSong = new ProcessSong();
        windowFlags = new WindowFlags(this.getWindow());
        appActionBar = new AppActionBar();
        versionNumber = new VersionNumber();
        fixLocale = new FixLocale();
        ccliLog = new CCLILog();
        exportFiles = new ExportFiles();
        showCase = new ShowCase();
        showToast = new ShowToast();
        mediaRoute = new MediaRoute();
        mediaRouterCallback = new MediaRouterCallback();
        pageButtonFAB = new PageButtonFAB(activityMainBinding.pageButtonsRight.actionFAB, activityMainBinding.pageButtonsRight.custom1Button,
                activityMainBinding.pageButtonsRight.custom2Button,activityMainBinding.pageButtonsRight.custom3Button,
                activityMainBinding.pageButtonsRight.custom4Button,activityMainBinding.pageButtonsRight.custom5Button,
                activityMainBinding.pageButtonsRight.custom6Button);
        pageButtonFAB.animatePageButton(this,false);
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
        if (rotation==0) {
            pageButtonFAB.animatePageButton(this,true);
        } else {
            pageButtonFAB.animatePageButton(this,false);
        }
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
        if (view!=null) {
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
    protected void onResume() {
        super.onResume();
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
            hideDrawer();
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
            hideDrawer();
            doSongLoad();
        }
    }



    // The toolbar/menu items
    @Override
    public void updateToolbar(String title) {
        // Null titles are for the default song, author, etc.
        // Otherwise a new title is passed as a string
        windowFlags.setWindowFlags();
        appActionBar.setActionBar(activityMainBinding.appBarMain.myToolBarNew.songtitleAb,
                activityMainBinding.appBarMain.myToolBarNew.songauthorAb,
                activityMainBinding.appBarMain.myToolBarNew.songkeyAb,title);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        settingsButton = (MenuItem) menu.findItem(R.id.settings_menu_item).getActionView();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("d","item="+item);
        if (item!=null && item.toString().equals("Settings")) {
            hideActionButton(true);
            navController.navigate(R.id.nav_preference);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainactivitymenu, menu);

        // Setup the menu item for connecting to cast devices
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        View mr = menu.findItem(R.id.media_route_menu_item).getActionView();
        if (mr!=null) {
            mr.setFocusable(false);
            mr.setFocusableInTouchMode(false);
        }
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        if (mediaRouteSelector != null) {
            mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);
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
                        null,getString(R.string.info_actionButton),false,"pageActionFAB");
                break;
            case "songsetmenu":
                // Initialise the arraylists
                initialiseArrayLists();
                targets.add(activityMainBinding.menuTop.tabs.getTabAt(0).view);
                targets.add(activityMainBinding.menuTop.tabs.getTabAt(1).view);
                infos.add(getString(R.string.menu_song));
                infos.add(getString(R.string.menu_set));
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
                    songXML,processSong,convertChoPro,convertOnSong,convertTextSong,showToast);
            runOnUiThread(() -> {
                StaticVariables.indexRequired = false;
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
                    tab.setIcon(getResources().getDrawable(R.drawable.ic_music_note_white_36dp));
                    break;
                case 1:
                    tab.setText(getString(R.string.set));
                    tab.setIcon(getResources().getDrawable(R.drawable.ic_format_list_numbers_white_36dp));
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
        activityMainBinding.menuTop.versionCode.setOnClickListener(v -> hideDrawer());
    }
    private void hideDrawer() {
        activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
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
        hideDrawer();
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
        navController.navigate(caller.getId());
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
    public void updateKeyAndLyrics(String key, String lyrics) {
        // This is called from the transpose class once it has done its work on the edit song fragment
        editSongFragmentMain.updateKeyAndLyrics(key, lyrics);
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
        } else {
            activityMainBinding.pageButtonsRight.actionFAB.show();
        }
    }
    @Override
    public void hideActionBar(boolean hide) {
        if (hide) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }
    }
    @Override
    public void navigateToFragment(int id) {
        hideDrawer();  // Only the Performance and Presentation fragments allow this.  Switched on in these fragments
        hideActionButton(true);
        try {
            navController.navigate(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void openDialog(DialogFragment frag, String tag) {
        frag.show(getSupportFragmentManager(), tag);
    }
    @Override
    public void updateFragment(String what, Fragment callingFragment) {
        switch (what) {
            case "StorageManagementFragment":
                ((StorageManagementFragment)callingFragment).updateFragment();
                break;
        }
    }

    @Override
    public void returnToHome(Fragment fragment, Bundle bundle) {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(fragment.getId(), true)
                .build();
        if (StaticVariables.whichMode.equals("Presentation")) {
            NavHostFragment.findNavController(fragment)
                    .navigate(R.id.nav_presentation,bundle,navOptions);
        } else {
            NavHostFragment.findNavController(fragment)
                    .navigate(R.id.nav_performance,bundle,navOptions);
        }

    }
    @Override
    public void displayAreYouSure(String what, String action, ArrayList<String> arguments, String fragName, Fragment callingFragment) {
        AreYouSureDialogFragment dialogFragment = new AreYouSureDialogFragment(what,action,arguments,fragName,callingFragment);
        dialogFragment.show(this.getSupportFragmentManager(), "areYouSure");
    }
    @Override
    public void confirmedAction(boolean agree, String what, ArrayList<String> arguments, String fragName, Fragment callingFragment) {
        Log.d("d","agree="+agree+"  what="+what);
        if (agree) {
            boolean result = false;
            switch(what) {
                case "deleteSong":
                    result = storageAccess.doDeleteFile(this,preferences,"Songs",
                            StaticVariables.whichSongFolder, StaticVariables.songfilename);
                    // Now remove from the SQL database
                    if (StaticVariables.fileType.equals("PDF") || StaticVariables.fileType.equals("IMG")) {
                        nonOpenSongSQLiteHelper.deleteSong(this,storageAccess,preferences,
                                nonOpenSongSQLiteHelper.getSongId());
                    } else {
                        sqLiteHelper.deleteSong(this, sqLiteHelper.getSongId());
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
                    rebuildTheSongIndex();
                    //Update the fragment
                    updateFragment(fragName,callingFragment);
                    break;

            }
            if (result) {
                ShowToast.showToast(this,getResources().getString(R.string.success));
            } else {
                ShowToast.showToast(this, getResources().getString(R.string.error));
            }
        }
    }

    private void rebuildTheSongIndex() {
        // Get all of the files as an array list
        ArrayList<String> songIds = storageAccess.listSongs(this, preferences);
        // Write this to text file
        storageAccess.writeSongIDFile(this, preferences, songIds);
        // Try to create the basic databases
        SQLiteHelper sqLiteHelper = new SQLiteHelper(this);
        sqLiteHelper.resetDatabase(this);
        NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(this);
        nonOpenSongSQLiteHelper.initialise(this, storageAccess, preferences);
        // Add entries to the database that have songid, folder and filename fields
        // This is the minimum that we need for the song menu.
        // It can be upgraded asynchronously in StageMode/PresenterMode to include author/key
        // Also will later include all the stuff for the search index as well
        sqLiteHelper.insertFast(this, storageAccess);
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

}
