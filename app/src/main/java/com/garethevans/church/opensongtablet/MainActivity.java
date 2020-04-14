package com.garethevans.church.opensongtablet;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.mediarouter.app.MediaRouteActionProvider;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.garethevans.church.opensongtablet.appdata.FixLocale;
import com.garethevans.church.opensongtablet.databinding.ActivityMainBinding;
import com.garethevans.church.opensongtablet.databinding.AppBarMainBinding;
import com.garethevans.church.opensongtablet.databinding.NewtoolbarincludeBinding;
import com.garethevans.church.opensongtablet.performance.PerformanceFragment;
import com.garethevans.church.opensongtablet.presentation.PresentationFragment;
import com.garethevans.church.opensongtablet.screensetup.AppActionBar;
import com.garethevans.church.opensongtablet.screensetup.BatteryStatus;
import com.garethevans.church.opensongtablet.screensetup.VersionNumber;
import com.garethevans.church.opensongtablet.screensetup.WindowFlags;
import com.garethevans.church.opensongtablet.songlist.SongListBuildIndex;
import com.garethevans.church.opensongtablet.songlist.SongListFragment;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity  implements PerformanceFragment.MyInterface {

    private AppBarConfiguration mAppBarConfiguration;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private SQLiteHelper sqLiteHelper;
    private ChordProConvert chordProConvert;
    private OnSongConvert onSongConvert;
    private TextSongConvert textSongConvert;
    private UsrConvert usrConvert;
    private SongXML songXML;
    private WindowFlags windowFlags;
    private BatteryStatus batteryStatus;
    private AppActionBar appActionBar;
    private VersionNumber versionNumber;
    private FixLocale fixLocale;

    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;

    private int currentMenuItem;

    PerformanceFragment performanceFragment;

    FragmentManager manager;
    FragmentTransaction transaction;

    // The fragments for the drawerLayout (for back pressed actions)
    Fragment performanceFrag, presentationFrag, songlistFrag;

    ActionBar ab;
    DrawerLayout drawerLayout;

    ActivityMainBinding activityMainBinding;
    AppBarMainBinding appBarMainBinding;
    NewtoolbarincludeBinding newtoolbarincludeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        appBarMainBinding = activityMainBinding.appBarMain;
        newtoolbarincludeBinding = appBarMainBinding.myToolBarNew;
        View view = activityMainBinding.getRoot();
        setContentView(view);

        setSupportActionBar(newtoolbarincludeBinding.myToolBarNew);
        ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = activityMainBinding.drawerLayout;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_boot, R.id.nav_performance, R.id.nav_presentation, R.id.nav_songlist,
                R.id.nav_songlist_custom)
                .setDrawerLayout(activityMainBinding.drawerLayout)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(activityMainBinding.navView, navController);

        //setNavigationViewListener();

        // Initialise the helpers used for heavy lifting
        initialiseHelpers();

        // Set the fullscreen window flags
        windowFlags.setWindowFlags();

        // Set the version
        versionNumber = new VersionNumber();
        versionNumber.updateMenuVersionNumber(this, activityMainBinding.navView.getHeaderView(0).findViewById(R.id.versionCode));

        // Initialise the start variables we need
        initialiseStartVariables();

        // Initialise the fragment tags for the song menu (for onBackPressed())
        initialiseFragmentTags();

        // Setup the CastContext
        mediaRouter = MediaRouter.getInstance(getApplicationContext());
        mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast("4E2B0891"))
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .build();

        // Battery monitor
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        this.registerReceiver(batteryStatus, filter);

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
        performanceFragment = new PerformanceFragment();
        windowFlags = new WindowFlags(this.getWindow());
        appActionBar = new AppActionBar();
        versionNumber = new VersionNumber();
        fixLocale = new FixLocale();
    }

    private void initialiseStartVariables() {
        StaticVariables.mDisplayTheme = preferences.getMyPreferenceString(this, "appTheme", "dark");
        StaticVariables.whichMode = preferences.getMyPreferenceString(this, "whichMode", "Performance");

        // Song location
        StaticVariables.songfilename = preferences.getMyPreferenceString(this,"songfilename","Welcome to OpenSongApp");
        StaticVariables.whichSongFolder = preferences.getMyPreferenceString(this, "whichSongFolder", getString(R.string.mainfoldername));

        // Set the locale
        fixLocale.fixLocale(this,preferences);
    }

    @Override
    public void updateToolbar() {
        appActionBar.setActionBar(activityMainBinding.appBarMain.myToolBarNew.songtitleAb,
                activityMainBinding.appBarMain.myToolBarNew.songauthorAb,
                activityMainBinding.appBarMain.myToolBarNew.songkeyAb);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void initialiseFragmentTags() {
        if (manager==null) {
            manager = getSupportFragmentManager();
        }
        performanceFrag = manager.findFragmentByTag("Performance");
        presentationFrag = manager.findFragmentByTag("Presentation");
        songlistFrag = manager.findFragmentByTag("SongList");
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

    /*private void setNavigationViewListener() {
        NavigationView navigationView = activityMainBinding.navView;
        navigationView.setNavigationItemSelectedListener(this);
    }*/

    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == currentMenuItem){
            drawerLayout.closeDrawers();
            return false;
        }
        currentMenuItem = id;
        int i = item.getItemId();
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();

        Log.d("MainActivity","item="+item.toString());

        switch(i) {
            case R.id.nav_performance:
                StaticVariables.homeFragment = true;
                preferences.setMyPreferenceString(this,"whichMode","Performance");
                transaction.replace(R.id.nav_host_fragment,new PerformanceFragment(),"Performance");
                transaction.addToBackStack("Presentation");
                break;

            case (R.id.nav_stage):
                StaticVariables.homeFragment = true;
                preferences.setMyPreferenceString(this,"whichMode","Stage");
                transaction.replace(R.id.nav_host_fragment,new PerformanceFragment(),"Stage");
                transaction.addToBackStack("Stage");
                break;

            case (R.id.nav_presentation):
                StaticVariables.homeFragment = true;
                preferences.setMyPreferenceString(this,"whichMode","Presentation");
                transaction.replace(R.id.nav_host_fragment,new PresentationFragment(),"Presentation");
                transaction.addToBackStack("Presentation");
                break;

            case (R.id.nav_songlist):
                StaticVariables.homeFragment = false;
                transaction.replace(R.id.nav_host_fragment,new SongListFragment(),"SongList");
                transaction.addToBackStack("SongList");
                break;
        }
        transaction.commit();
        activityMainBinding.drawerLayout.closeDrawers();

        return false;
    }*/
}
