package com.garethevans.church.opensongtablet;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.mediarouter.app.MediaRouteActionProvider;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.animation.PageButtonFAB;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.appdata.FixLocale;
import com.garethevans.church.opensongtablet.databinding.ActivityMainBinding;
import com.garethevans.church.opensongtablet.databinding.AppBarMainBinding;
import com.garethevans.church.opensongtablet.interfaces.DrawerInterface;
import com.garethevans.church.opensongtablet.interfaces.LoadSongInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.ShowCaseInterface;
import com.garethevans.church.opensongtablet.performance.PerformanceFragment;
import com.garethevans.church.opensongtablet.presentation.PresentationFragment;
import com.garethevans.church.opensongtablet.screensetup.AppActionBar;
import com.garethevans.church.opensongtablet.screensetup.BatteryStatus;
import com.garethevans.church.opensongtablet.screensetup.VersionNumber;
import com.garethevans.church.opensongtablet.screensetup.WindowFlags;
import com.garethevans.church.opensongtablet.secondarydisplay.MediaRoute;
import com.garethevans.church.opensongtablet.secondarydisplay.MediaRouterCallback;
import com.garethevans.church.opensongtablet.songsandsets.SetMenuFragment;
import com.garethevans.church.opensongtablet.songsandsets.SongListBuildIndex;
import com.garethevans.church.opensongtablet.songsandsets.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoadSongInterface,
        SetMenuFragment.MyInterface, ShowCaseInterface, DrawerInterface,
        MainActivityInterface {

    private AppBarConfiguration mAppBarConfiguration;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private SQLiteHelper sqLiteHelper;
    private ChordProConvert chordProConvert;
    private OnSongConvert onSongConvert;
    private TextSongConvert textSongConvert;
    private UsrConvert usrConvert;
    private SongXML songXML;
    private ShowCase showCase;
    private WindowFlags windowFlags;
    private BatteryStatus batteryStatus;
    private AppActionBar appActionBar;
    private VersionNumber versionNumber;
    private FixLocale fixLocale;
    private PageButtonFAB pageButtonFAB;
    private boolean pageButtonActive = true;
    private MediaRoute mediaRoute;
    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private MediaRouterCallback mediaRouterCallback;
    private ShowCaseInterface showCaseInterface;

    FragmentManager manager;
    PerformanceFragment performanceFragment;
    PresentationFragment presentationFragment;

    NavController navController;

    ActionBar ab;
    DrawerLayout drawerLayout;

    MenuItem settingsButton;

    ActivityMainBinding activityMainBinding;
    AppBarMainBinding appBarMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        appBarMainBinding = activityMainBinding.appBarMain;

        showCaseInterface = this;

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

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_boot, R.id.nav_storage, R.id.nav_performance, R.id.nav_presentation)
                .setDrawerLayout(activityMainBinding.drawerLayout)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
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
    // Most of the set up only happens once the user has passed the BootUpFragment checks
    @Override
    public void initialiseActivity() {

        unlockDrawer();
        hideActionButton(false);
        hideActionBar(false);

        // Set the version in the menu
        versionNumber.updateMenuVersionNumber(this, activityMainBinding.menuTop.versionCode);

        // Initialise the fragment tags for the song menu (for onBackPressed())
        initialiseFragmentTags();

        // Set up page buttons
        setListeners();

        // Set up song / set menu tabs
        setUpSongMenuTabs();

        // Setup the CastContext
        mediaRouter = MediaRouter.getInstance(getApplicationContext());
        mediaRouteSelector = mediaRoute.getMediaRouteSelector();

        indexSongs();
    }


    @Override
    public void indexSongs() {
        new Thread(() -> {
            runOnUiThread(() -> {
                StaticVariables.myToastMessage = getString(R.string.search_index_start);
                ShowToast.showToast(MainActivity.this);
            });
            SongListBuildIndex songListBuildIndex = new SongListBuildIndex();
            songListBuildIndex.fullIndex(MainActivity.this,preferences,storageAccess,sqLiteHelper,songXML,
                    chordProConvert,onSongConvert,textSongConvert,usrConvert);
            runOnUiThread(() -> {
                StaticVariables.myToastMessage = getString(R.string.search_index_end);
                ShowToast.showToast(MainActivity.this);
            });
        }).start();
    }

    private void initialiseHelpers() {
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        sqLiteHelper = new SQLiteHelper(MainActivity.this);
        chordProConvert = new ChordProConvert();
        onSongConvert = new OnSongConvert();
        textSongConvert = new TextSongConvert();
        usrConvert = new UsrConvert();
        songXML = new SongXML();
        windowFlags = new WindowFlags(this.getWindow());
        appActionBar = new AppActionBar();
        versionNumber = new VersionNumber();
        fixLocale = new FixLocale();
        showCase = new ShowCase();
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

        // Set the locale
        fixLocale.fixLocale(this,preferences);
    }

    @Override
    public void updateToolbar() {
        windowFlags.setWindowFlags();
        appActionBar.setActionBar(activityMainBinding.appBarMain.myToolBarNew.songtitleAb,
                activityMainBinding.appBarMain.myToolBarNew.songauthorAb,
                activityMainBinding.appBarMain.myToolBarNew.songkeyAb);
    }

    @Override
    public void showTutorial(String what) {
        ArrayList<View> views = new ArrayList<>();
        ArrayList<String> infos = new ArrayList<>();
        ArrayList<String> dismisses = new ArrayList<>();
        if (settingsButton==null) {
            invalidateOptionsMenu();
        }
        switch (what) {
            case "actionButton":
                showCase.singleShowCase(this,activityMainBinding.pageButtonsRight.actionFAB,null,getString(R.string.info_actionButton)).build().show(this);
                break;
            case "songsetMenu":
                views.add(activityMainBinding.menuTop.tabs.getTabAt(0).view);
                views.add(activityMainBinding.menuTop.tabs.getTabAt(1).view);
                infos.add("Use this to view or filter the songs you have");
                infos.add("Use this to view or manage your sets");
                dismisses.add(null);
                dismisses.add(null);
                showCase.sequenceShowCase(this,views,dismisses,infos,"songMenu");
                // Now tell the menu fragment to show the other stuff it deals with
                showCaseInterface.runShowCase();
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        settingsButton = (MenuItem) menu.findItem(R.id.settings_menu_item).getActionView();
        return true;
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

        settingsButton = (MenuItem) menu.findItem(R.id.settings_menu_item).getActionView();

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

    private void setUpSongMenuTabs() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(),getLifecycle());
        adapter.createFragment(0);
        adapter.createFragment(1);
        ViewPager2 viewPager = activityMainBinding.viewpager;
        viewPager.setAdapter(adapter);
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
        activityMainBinding.menuTop.versionCode.setOnClickListener(v -> activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START));
    }

    private void initialiseFragmentTags() {
        if (manager==null) {
            manager = getSupportFragmentManager();
        }

        performanceFragment = (PerformanceFragment) manager.findFragmentById(R.id.nav_performance);
        presentationFragment = (PresentationFragment) manager.findFragmentById(R.id.nav_presentation);
    }

    @Override
    public void onBackPressed() {
        if (StaticVariables.homeFragment) {
            Log.d("d","In the home fragment, so deal with back pressed");
        } else {
            Log.d("d","Let Android deal with back press based on backstack");
            super.onBackPressed();
        }
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
                activityMainBinding.pageButtonsRight.actionFAB.hide();
                showTutorial("songsetMenu");
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                activityMainBinding.pageButtonsRight.actionFAB.show();
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
    public void loadSongFromSet() {
        StaticVariables.myToastMessage = "loadSongFromSet() called in MainActivity";
        ShowToast.showToast(this);
    }

    @Override
    public void confirmedAction() {
        StaticVariables.myToastMessage = "confirmedAction() called in MainActivity";
        ShowToast.showToast(this);
    }

    @Override
    public void refreshAll() {
        StaticVariables.myToastMessage = "refreshAll() called in MainActivity";
        ShowToast.showToast(this);
    }

    @Override
    public void doSongLoad() {
        Log.d("MainActivity","doSongLoad() called");
        NavHostFragment fragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (StaticVariables.whichMode.equals("Presentation")) {
            presentationFragment = (PresentationFragment)fragment.getChildFragmentManager().getFragments().get(0);
            presentationFragment.doSongLoad();
        } else {
            performanceFragment = (PerformanceFragment) fragment.getChildFragmentManager().getFragments().get(0);
            performanceFragment.doSongLoad();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void runShowCase() {

    }

    @Override
    public void lockDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void unlockDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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
}
