package com.garethevans.church.opensongtablet;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.make;

import android.Manifest;
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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;

import com.bumptech.glide.Glide;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lib.folderpicker.FolderPicker;

public class PresenterMode extends AppCompatActivity implements MenuHandlers.MyInterface,
        SongMenuListeners.MyInterface, PopUpChooseFolderFragment.MyInterface,
        PopUpSongDetailsFragment.MyInterface, PopUpEditSongFragment.MyInterface,
        SetActions.MyInterface, PopUpPresentationOrderFragment.MyInterface,
        PopUpSetViewNew.MyInterface, IndexSongs.MyInterface, SearchView.OnQueryTextListener,
        OptionMenuListeners.MyInterface, PopUpFullSearchFragment.MyInterface,
        PopUpListSetsFragment.MyInterface,
        PopUpLongSongPressFragment.MyInterface, PopUpSwipeSettingsFragment.MyInterface,
        PopUpFileChooseFragment.MyInterface, PopUpBackupPromptFragment.MyInterface,
        PopUpSongFolderRenameFragment.MyInterface, PopUpSongCreateFragment.MyInterface,
        PopUpSongRenameFragment.MyInterface, PopUpImportExportOSBFragment.MyInterface,
        PopUpImportExternalFile.MyInterface, PopUpCustomSlideFragment.MyInterface,
        PopUpFindNewSongsFragment.MyInterface,
        PopUpThemeChooserFragment.MyInterface, PopUpGroupedPageButtonsFragment.MyInterface,
        PopUpQuickLaunchSetup.MyInterface, PopUpPagesFragment.MyInterface,
        PopUpExtraInfoFragment.MyInterface, PopUpPageButtonsFragment.MyInterface,
        PopUpScalingFragment.MyInterface, PopUpFontsFragment.MyInterface,
        PopUpChordsFragment.MyInterface, PopUpCustomChordsFragment.MyInterface,
        PopUpTransposeFragment.MyInterface, PopUpEditStickyFragment.MyInterface,
        PopUpPadFragment.MyInterface, PopUpAutoscrollFragment.MyInterface,
        PopUpMetronomeFragment.MyInterface, PopUpStickyFragment.MyInterface,
        PopUpLinks.MyInterface, PopUpAreYouSureFragment.MyInterface,
        SongMenuAdapter.MyInterface, BatteryMonitor.MyInterface,
        PopUpMenuSettingsFragment.MyInterface, PopUpAlertFragment.MyInterface,
        PopUpLayoutFragment.MyInterface, DownloadTask.MyInterface,
        PopUpExportFragment.MyInterface, PopUpActionBarInfoFragment.MyInterface,
        PopUpCreateDrawingFragment.MyInterface,
        PopUpRandomSongFragment.MyInterface,
        PopUpConnectFragment.MyInterface,
        PopUpCCLIFragment.MyInterface, NearbyReturnActionsInterface, NearbyInterface,
        PopUpBibleXMLFragment.MyInterface, PopUpShowMidiMessageFragment.MyInterface,
        PopUpChordFormatFragment.MyInterface {
    private DialogFragment newFragment;

    private final String TAG = "PresenterMode";
    // Helper classes
    private SetActions setActions;
    private ExportPreparer exportPreparer;
    private StorageAccess storageAccess;
    private IndexSongs indexSongs;
    private Preferences preferences;
    private SongXML songXML;
    private ChordProConvert chordProConvert;
    private OnSongConvert onSongConvert;
    private UsrConvert usrConvert;
    private TextSongConvert textSongConvert;
    private SQLiteHelper sqLiteHelper;
    private SQLite sqLite;  // The song details from SQLite.  Used for menu and searches.
    private NonOpenSongSQLite nonOpenSongSQLite; // For pdf and image files
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private ProcessSong processSong;
    private ProfileActions profileActions;
    private NearbyConnections nearbyConnections;
    private MakePDF makePDF;

    // MIDI
    private Midi midi;

    // Casting
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private final MyMediaRouterCallback mMediaRouterCallback = new MyMediaRouterCallback();
    private CastDevice mSelectedDevice;

    // The toolbar and menu
    private Toolbar ab_toolbar;
    private static ActionBar ab;
    private RelativeLayout songandauthor;
    private TextView digitalclock;
    private TextView songtitle_ab;
    private TextView songkey_ab;
    private TextView songcapo_ab;
    private TextView songauthor_ab;
    private TextView batterycharge;
    private ImageView batteryimage;
    private RelativeLayout batteryholder;

    // AsyncTasks
    private AsyncTask<Object, Void, String> preparesongmenu_async;
    private AsyncTask<Object, Void, String> prepareoptionmenu_async;
    private AsyncTask<Object, Void, String> autoslideshowtask;
    private AsyncTask<Object, Void, String> sharesong_async;
    private AsyncTask<Object, Void, String> shareset_async;
    private AsyncTask<Object, Void, String> load_customreusable;
    private AsyncTask<Object, Void, String> add_slidetoset;
    private AsyncTask<Object, Void, String> resize_drawers;
    private AsyncTask<Object, Void, String> do_moveinset;
    private AsyncTask<Object, Void, String> shareactivitylog_async;
    private AsyncTask<Object, Void, String> check_storage;
    private AsyncTask<String, Integer, String> do_download;
    private LoadSong loadsong_async;

    // The views
    private LinearLayout mLayout;
    private LinearLayout pres_details;
    private LinearLayout presenter_song_buttonsListView;
    private LinearLayout presenter_set_buttonsListView;
    private LinearLayout loopandtimeLinearLayout;
    private TextView presenter_songtitle;
    private TextView presenter_author;
    private TextView presenter_copyright;
    private CheckBox presenter_order_text;
    private CheckBox loopCheckBox;
    private Button presenter_order_button;
    private FloatingActionButton set_view_fab;
    private FloatingActionButton startstopSlideShow;
    private EditText presenter_lyrics;
    private EditText timeEditText;
    private ImageView presenter_lyrics_image;
    private ScrollView presenter_songbuttons;
    private ScrollView preso_action_buttons_scroll;
    private ScrollView presenter_setbuttons;

    // Quick nav buttons
    private FloatingActionButton nav_prevsong;
    private FloatingActionButton nav_nextsong;
    private FloatingActionButton nav_prevsection;
    private FloatingActionButton nav_nextsection;
    private boolean autoproject = false;
    private boolean pedalsenabled = true;

    // The buttons
    private TextView presenter_project_group;
    private TextView presenter_logo_group;
    private TextView presenter_background_group;
    private TextView presenter_blank_group;
    private TextView presenter_alert_group;
    private TextView presenter_audio_group;
    private TextView presenter_dB_group;
    private TextView presenter_slide_group;
    private TextView presenter_scripture_group;
    private TextView presenter_display_group;

    // The song and option menu stuff
    private DrawerLayout mDrawerLayout;
    private LinearLayout songmenu;
    private LinearLayout optionmenu;
    private TextView menuFolder_TextView;
    private TextView menuCount_TextView;
    private ListView song_list_view;
    private boolean firstrun_option = true;
    private boolean firstrun_song = true;
    private boolean newsongloaded = false;

    // The media player
    public static MediaPlayer mp;
    public static String mpTitle = "";

    // Variables used by the popups
    static String whatBackgroundLoaded;

    // General variables
    private String[] imagelocs;

    // Which Actions buttons are selected
    private boolean projectButton_isSelected = false;
    private boolean blankButton_isSelected = false;
    static boolean logoButton_isSelected = false;
    static boolean backgroundButton_isSelected = false;
    static String alert_on = "N";
    // IV - Support a half highlight of buttons during transitions
    private boolean buttonInTransition = false;

    // Auto slideshow
    private boolean isplayingautoslideshow = false;
    private boolean autoslideloop = false;
    private int autoslidetime = 0;

    // Battery
    private BroadcastReceiver br;

    // IV - Called after delay to enable and set correct highlights of action buttons
    private final Handler enableActionButtonsHandler = new Handler();
    private final Runnable enableActionButtonsRunnable = () -> {
        if (blankButton_isSelected) {
            highlightButtonClicked(presenter_blank_group);
        } else {
            unhighlightButtonClicked(presenter_blank_group);
        }
        if (logoButton_isSelected) {
            highlightButtonClicked(presenter_logo_group);
        } else {
            unhighlightButtonClicked(presenter_logo_group);
        }
        if (backgroundButton_isSelected) {
            highlightButtonClicked(presenter_background_group);
        } else {
            unhighlightButtonClicked(presenter_background_group);
        }
        if (projectButton_isSelected) {
            highlightButtonClicked(presenter_project_group);
        } else {
            unhighlightButtonClicked(presenter_project_group);
        }
        presenter_project_group.setEnabled(true);
        presenter_logo_group.setEnabled(true);
        presenter_background_group.setEnabled(true);
        presenter_blank_group.setEnabled(true);
        buttonInTransition = false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Welcome to Presentation Mode");

        StaticVariables.activity = PresenterMode.this;
        FullscreenActivity.mContext = PresenterMode.this;
        FullscreenActivity.appRunning = true;

        // Initialise the helpers
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
        sqLiteHelper = new SQLiteHelper(PresenterMode.this);
        nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(PresenterMode.this);
        processSong = new ProcessSong();
        profileActions = new ProfileActions();
        OptionMenuListeners optionMenuListeners = new OptionMenuListeners(this);
        nearbyConnections = new NearbyConnections(this,preferences,storageAccess,processSong, optionMenuListeners,sqLiteHelper);
        makePDF = new MakePDF();

        // IV - Index at start of session
        if (FullscreenActivity.doonetimeactions) {
            new Thread(() -> {
                runOnUiThread(() -> {
                    StaticVariables.myToastMessage = getString(R.string.search_index_start);
                    ShowToast.showToast(PresenterMode.this);
                });
                indexSongs.fullIndex(PresenterMode.this,preferences,storageAccess,sqLiteHelper,songXML,
                        chordProConvert,onSongConvert,textSongConvert,usrConvert);
                runOnUiThread(() -> {
                    StaticVariables.myToastMessage = getString(R.string.search_index_end);
                    ShowToast.showToast(PresenterMode.this);
                    // Now instruct the song menu to be built again.
                    prepareSongMenu();
                });
            }).start();
        }

        // Get the language
        FixLocale.fixLocale(PresenterMode.this,preferences);

        checkStorage();

        mp = new MediaPlayer();

        // Load the layout and set the title
        setContentView(R.layout.presenter_mode);

        // In order to quickly start, load the minimum variables we need
        loadStartUpVariables();
        setActions.prepareSetList(PresenterMode.this,preferences);

        // Set the fullscreen window flags
        runOnUiThread(() -> {
            setWindowFlags();
            setWindowFlagsAdvanced();
        });

        // Battery monitor
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        try {
            br = new BatteryMonitor();
            PresenterMode.this.registerReceiver(br, filter);
        } catch (Exception e) {
            Log.d(TAG, "Didn't register battery");
        }

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

        // Since this mode has just been opened, force an update to the cast screen
        StaticVariables.forcecastupdate = true;

        // Set up the toolbar and views
        runOnUiThread(() -> {
            ab_toolbar = findViewById(R.id.mytoolbar); // Attaching the layout to the toolbar object
            setSupportActionBar(ab_toolbar);                     // Setting toolbar as the ActionBar with setSupportActionBar() call
            ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayShowHomeEnabled(false); // show or hide the default home button
                ab.setDisplayHomeAsUpEnabled(false);
                ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
                ab.setDisplayShowTitleEnabled(false);
            }

            // Identify the views
            initialiseTheViews();
            screenClickListeners();

            // Make the drawers match half the width of the screen
            resizeDrawers();

            // IV - refreshAll call later will perform setupButtons, prepareOptionsMenu and setupSongButtons

            // Set up the Wifi service
            getBluetoothName();
            nearbyConnections.getUserNickname();

            // Initialise the ab info
            adjustABInfo();

            // Setup the options drawer reset on close
            setupOptionDrawerReset();
            // IV - Force display of top level of option menu - needed after mode change
            closeMyDrawers("song");

            // IV - refreshAll includes load of the song
            // Click on the first item in the set (which calls refreshAll)
            if (presenter_set_buttonsListView.getChildCount() > 0) {
                presenter_set_buttonsListView.getChildAt(0).performClick();
            } else {
                refreshAll();
            }

            // Deal with any intents from external files/intents
            dealWithIntent();
        });

        // IV - Check backups at start of session
        if (FullscreenActivity.doonetimeactions) {
            // Check if we need to remind the user to backup their songs
            checkBackupState();
        }

        setDummyFocus();

        // Establish a known state for Nearby
        nearbyConnections.turnOffNearby();

        // IV -  One time actions will have been completed
        FullscreenActivity.doonetimeactions = false;
    }

    // Handlers for main page on/off/etc. and window flags
    @Override
    public void onStart() {
        super.onStart();
        StaticVariables.activity = PresenterMode.this;
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
    protected void onStop() {
        super.onStop();
        try {
            FullscreenActivity.appRunning = false;
            mMediaRouter.removeCallback(mMediaRouterCallback);
        } catch (Exception e) {
            Log.d(TAG, "Problem removing mediaroutercallback");
        }

        if (br!=null) {
            try {
                PresenterMode.this.unregisterReceiver(br);
            } catch (Exception e2) {
                Log.d(TAG, "Battery receiver not registerd, so no need to unregister");
            }
        }
    }
    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();
        StaticVariables.activity = PresenterMode.this;
        FullscreenActivity.appRunning = true;
        resizeDrawers();
        // Fix the page flags
        windowFlags();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Battery monitor
        if (br!=null) {
            try {
                PresenterMode.this.unregisterReceiver(br);
            } catch (Exception e) {
                Log.d(TAG, "Battery monitor not registered anymore");
            }
        }
        tryCancelAsyncTasks();

        //Second screen
        try {
            CastRemoteDisplayLocalService.stopService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (FullscreenActivity.hdmi !=null) {
                FullscreenActivity.hdmi.dismiss();
                FullscreenActivity.hdmi = null;
            }
        } catch (Exception e) {
            // Ooops
            e.printStackTrace();
        }
        nearbyConnections.turnOffNearby();
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Get the language
        FixLocale.fixLocale(PresenterMode.this,preferences);

        FullscreenActivity.orientationchanged = FullscreenActivity.mScreenOrientation != newConfig.orientation;
        if (FullscreenActivity.orientationchanged) {
            if (newFragment != null && newFragment.getDialog() != null) {
                PopUpSizeAndAlpha.decoratePopUp(PresenterMode.this, newFragment.getDialog(), preferences);
            }
            // Now, reset the orientation.
            FullscreenActivity.orientationchanged = false;

            // Get the current orientation
            FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

            //invalidateOptionsMenu();
            closeMyDrawers("both");
            resizeDrawers();
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            windowFlags();
        }
    }
    public void windowFlags() {
        setWindowFlags();
        setWindowFlagsAdvanced();
    }
    private void setWindowFlags() {
        try {
            View v = getWindow().getDecorView();
            v.setOnSystemUiVisibilityChangeListener(null);
            v.setOnFocusChangeListener(null);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setWindowFlagsAdvanced() {
        try {
            View v = getWindow().getDecorView();
            v.setOnSystemUiVisibilityChangeListener(i -> {
                if (i!=0) {
                    setWindowFlags();
                    setWindowFlagsAdvanced();
                }
            });
            v.setOnFocusChangeListener(null);

            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE);

            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void tryCancelAsyncTasks() {
        doCancelAsyncTask(loadsong_async);
        doCancelAsyncTask(preparesongmenu_async);
        doCancelAsyncTask(prepareoptionmenu_async);
        doCancelAsyncTask(sharesong_async);
        doCancelAsyncTask(shareset_async);
        doCancelAsyncTask(shareactivitylog_async);
        doCancelAsyncTask(load_customreusable);
        doCancelAsyncTask(resize_drawers);
        doCancelAsyncTask(do_moveinset);
        doCancelAsyncTask(add_slidetoset);
        doCancelAsyncTask(autoslideshowtask);
        doCancelAsyncTask(do_download);
        doCancelAsyncTask(check_storage);

    }
    private void doCancelAsyncTask(AsyncTask<?,?,?> ast) {
        if (ast != null) {
            try {
                ast.cancel(true);
            } catch (Exception e) {
                // OOps
            }
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

    // Load the variables we need
    private void loadStartUpVariables() {

        StaticVariables.mDisplayTheme = preferences.getMyPreferenceString(PresenterMode.this,"appTheme","dark");

        // The mode we are in
        StaticVariables.whichMode = preferences.getMyPreferenceString(PresenterMode.this, "whichMode", "Performance");

        // Locale
        try {
            StaticVariables.locale = new Locale(preferences.getMyPreferenceString(PresenterMode.this, "locale", "en"));
            Locale.setDefault(StaticVariables.locale);
            Configuration config = new Configuration();
            config.setLocale(StaticVariables.locale);
            this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Song location
        loadFileLocation();
    }

    private void setDummyFocus() {
        try {
            // This simply makes all of the blank space focusable
            // This is to allow removing the view from the edittext to hide the keyboard
            findViewById(R.id.coordinator_layout).setOnClickListener(new RemoveFocus());
            findViewById(R.id.pres_col1).setOnClickListener(new RemoveFocus());
            findViewById(R.id.pres_col2).setOnClickListener(new RemoveFocus());
            findViewById(R.id.pres_col3).setOnClickListener(new RemoveFocus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class RemoveFocus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                presenter_lyrics.clearFocus();
                InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm!=null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                setWindowFlags();
                setWindowFlagsAdvanced();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void adjustABInfo() {
        boolean inuse = false;
        // Change the visibilities
        if (preferences.getMyPreferenceBoolean(PresenterMode.this,"batteryDialOn",true)) {
            batteryimage.setVisibility(View.VISIBLE);
            inuse = true;
        } else {
            batteryimage.setVisibility(View.INVISIBLE);
        }
        if (preferences.getMyPreferenceBoolean(PresenterMode.this,"batteryTextOn",true)) {
            batterycharge.setVisibility(View.VISIBLE);
            inuse = true;
        } else {
            batterycharge.setVisibility(View.GONE);
        }
        if (preferences.getMyPreferenceBoolean(PresenterMode.this,"clockOn",true)) {
            digitalclock.setVisibility(View.VISIBLE);
            inuse = true;
        } else {
            digitalclock.setVisibility(View.GONE);
        }

        if (inuse) {
            // Set the text sizes
            batterycharge.setTextSize(preferences.getMyPreferenceFloat(PresenterMode.this, "batteryTextSize", 9.0f));
            digitalclock.setTextSize(preferences.getMyPreferenceFloat(PresenterMode.this, "clockTextSize", 9.0f));
            songtitle_ab.setTextSize(preferences.getMyPreferenceFloat(PresenterMode.this, "songTitleSize", 13.0f));
            songcapo_ab.setTextSize(preferences.getMyPreferenceFloat(PresenterMode.this, "songTitleSize", 13.0f));
            songauthor_ab.setTextSize(preferences.getMyPreferenceFloat(PresenterMode.this, "songAuthorSize", 11.0f));
            songkey_ab.setTextSize(preferences.getMyPreferenceFloat(PresenterMode.this, "songTitleSize", 13.0f));

            // Set the time format
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df;
            if (preferences.getMyPreferenceBoolean(PresenterMode.this, "clock24hFormat", true)) {
                df = new SimpleDateFormat("HH:mm", StaticVariables.locale);
            } else {
                df = new SimpleDateFormat("h:mm", StaticVariables.locale);
            }
            String formattedTime = df.format(c.getTime());
            digitalclock.setText(formattedTime);
            batteryholder.setVisibility(View.VISIBLE);
        }else {
            batteryholder.setVisibility(View.GONE);
        }
    }

    @Override
    // The navigation drawers
    public void prepareSongMenu() {
        try {
            if (song_list_view==null) {
                song_list_view = findViewById(R.id.song_list_view);
            }
            doCancelAsyncTask(preparesongmenu_async);
            song_list_view.setFastScrollEnabled(false);
            song_list_view.setScrollingCacheEnabled(false);
            preparesongmenu_async = new PrepareSongMenu();
            preparesongmenu_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkBackupState() {
        // Check for the number of times the app has run without the user backing up his songs
        // If this is 10 (or more) show the backup prompt window.
        int runssincebackup = preferences.getMyPreferenceInt(PresenterMode.this, "runssincebackup", 0) + 1;

        // Save the new value
        preferences.setMyPreferenceInt(PresenterMode.this, "runssincebackup", runssincebackup);
        if (runssincebackup >= 10) {
            FullscreenActivity.whattodo = "promptbackup";
            openFragment();
        }
    }

    public void loadSong() {
        // IV - Prevent slide shows continuing to be active on change of song(!)
        prepareStopAutoSlideShow();

        try {
            // Only do this once - if we are in the process of loading a song already, don't try to do it again!
            if (!FullscreenActivity.alreadyloading) {
                // It will get set back to false in the post execute of the async task
                FullscreenActivity.alreadyloading = true;

                // Remove any capokey
                FullscreenActivity.capokey = "";

                // Declare we have loaded a new song (for the ccli log).
                // This stops us reporting projecting every section
                newsongloaded = true;

                // We may transpose to Nashville and need to init
                StaticVariables.fromchordnumsnash = null;
                StaticVariables.tochordnumsnash = null;

                doCancelAsyncTask(loadsong_async);
                loadsong_async = new LoadSong();
                try {
                    loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (Exception e) {
                    // Error loading the song
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void findSongInFolders() {
        //scroll to the song in the song menu
        try {
            song_list_view.setSelection(FullscreenActivity.currentSongIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        StaticVariables.songfilename = preferences.getMyPreferenceString(PresenterMode.this,"songfilename","Welcome to OpenSongApp");
        StaticVariables.whichSongFolder = preferences.getMyPreferenceString(PresenterMode.this, "whichSongFolder", getString(R.string.mainfoldername));
    }
    private void saveFileLocation(String loc_name, String loc_folder) {
        StaticVariables.songfilename = loc_name;
        StaticVariables.whichSongFolder = loc_folder;
        preferences.setMyPreferenceString(PresenterMode.this, "songfilename", loc_name);
        preferences.setMyPreferenceString(PresenterMode.this, "whichSongFolder", loc_folder);


    }

    @Override
    public void songLongClick() {
        // Rebuild the set list as we've just added a song
        setActions.prepareSetList(PresenterMode.this,preferences);
        prepareOptionMenu();
        fixSet();
        closeMyDrawers("song");
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

    private void loadPDFPagePreview() {
        presenter_lyrics.setVisibility(View.GONE);
        // IV - Make sure it starts clear
        presenter_lyrics_image.setImageBitmap(null);

        Bitmap bmp = processSong.createPDFPage(PresenterMode.this, preferences, storageAccess, 800, 800, "Y");
        if (bmp != null) {
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(bmp.getWidth(), bmp.getHeight());
            presenter_lyrics_image.setLayoutParams(llp);
            // Set the image to the view
            presenter_lyrics_image.setBackgroundColor(StaticVariables.white);
            presenter_lyrics_image.setImageBitmap(bmp);

        } else {
            // Set the image to the unhappy android
            Drawable myDrawable = ResourcesCompat.getDrawable(getResources(),R.drawable.unhappy_android,null);
            presenter_lyrics_image.setImageDrawable(myDrawable);

            // Set an intent to try and open the pdf with an appropriate application
            Intent target = new Intent(Intent.ACTION_VIEW);
            // Run an intent to try to show the pdf externally
            target.setDataAndType(StaticVariables.uriToLoad, "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            callIntent("openpdf", target);
        }

        presenter_lyrics_image.setVisibility(View.VISIBLE);

        if (autoproject || preferences.getMyPreferenceBoolean(PresenterMode.this,"presoAutoUpdateProjector",true)) {
            autoproject = false;
            presenter_project_group.performClick();
        }
    }
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
                width = preferences.getMyPreferenceInt(PresenterMode.this,"menuSize",250);
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
                if (!cancelled) {
                    songmenu.setLayoutParams(DrawerTweaks.resizeMenu(songmenu, width));
                    optionmenu.setLayoutParams(DrawerTweaks.resizeMenu(optionmenu, width));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void openMyDrawers(String which) {
        new Thread(() -> runOnUiThread(() -> DrawerTweaks.openMyDrawers(mDrawerLayout, songmenu, optionmenu, which))).start();
    }

    @Override
    public void closeMyDrawers(String which) {
        new Thread(() -> runOnUiThread(() -> DrawerTweaks.closeMyDrawers(mDrawerLayout, songmenu, optionmenu, which))).start();
    }

    // The overflow menu and actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.presenter_actions, menu);

        // Setup the menu item for connecting to cast devices
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        }

        // Force overflow icon to show, even if hardware key is present
        MenuHandlers.forceOverFlow(PresenterMode.this, ab, menu);

        // Set up battery monitor
        setUpBatteryMonitor();

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        MenuHandlers.actOnClicks(PresenterMode.this, preferences,item.getItemId());
        return super.onOptionsItemSelected(item);
    }
    public void setUpBatteryMonitor() {
        // Get clock
        try {
            boolean inuse = false;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df;
            if (preferences.getMyPreferenceBoolean(PresenterMode.this,"clock24hFormat",true)) {
                df = new SimpleDateFormat("HH:mm", StaticVariables.locale);
            } else {
                df = new SimpleDateFormat("h:mm", StaticVariables.locale);
            }
            String formattedTime = df.format(c.getTime());
            if (preferences.getMyPreferenceBoolean(PresenterMode.this,"clockOn",true)) {
                digitalclock.setVisibility(View.VISIBLE);
                inuse = true;
            } else {
                digitalclock.setVisibility(View.GONE);
            }
            digitalclock.setTextSize(preferences.getMyPreferenceFloat(PresenterMode.this,"clockTextSize",9.0f));
            digitalclock.setText(formattedTime);

            // Get battery
            int i = (int) (BatteryMonitor.getBatteryStatus(PresenterMode.this) * 100.0f);
            String charge = i + "%";
            if (preferences.getMyPreferenceBoolean(PresenterMode.this,"batteryTextOn",true)) {
                batterycharge.setVisibility(View.VISIBLE);
                inuse = true;
            } else {
                batterycharge.setVisibility(View.GONE);
            }
            batterycharge.setTextSize(preferences.getMyPreferenceFloat(PresenterMode.this, "batteryTextSize",9.0f));
            batterycharge.setText(charge);
            int abh = ab.getHeight();
            StaticVariables.ab_height = abh;
            if (preferences.getMyPreferenceBoolean(PresenterMode.this,"batteryDialOn",true)) {
                batteryimage.setVisibility(View.VISIBLE);
                inuse = true;
            } else {
                batteryimage.setVisibility(View.INVISIBLE);
            }
            if (ab != null && abh > 0) {
                BitmapDrawable bmp = BatteryMonitor.batteryImage(PresenterMode.this, preferences, i, abh);
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
    @Override
    public void refreshActionBar() {
        invalidateOptionsMenu();
    }
    @Override
    public void showActionBar() {
        // Do nothing as we don't allow this in Presentation Mode
    }
    @Override
    public void hideActionBar() {
        // Do nothing as we don't allow this in Presentation Mode
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
    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class CheckStorage extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (ActivityCompat.checkSelfPermission(PresenterMode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Prepare the stuff we need
    private void initialiseTheViews() {

        // The main views
        mLayout = findViewById(R.id.pagepresentermode);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        pres_details = findViewById(R.id.pres_details);
        presenter_songtitle = findViewById(R.id.presenter_songtitle);
        presenter_author = findViewById(R.id.presenter_author);
        presenter_copyright = findViewById(R.id.presenter_copyright);
        presenter_order_text = findViewById(R.id.presenter_order_text);
        presenter_order_button = findViewById(R.id.presenter_order_button);
        set_view_fab = findViewById(R.id.set_view_fab);
        TextView presenter_set = findViewById(R.id.presenter_set);
        presenter_set_buttonsListView = findViewById(R.id.presenter_set_buttonsListView);
        presenter_lyrics = findViewById(R.id.presenter_lyrics);
        presenter_lyrics_image = findViewById(R.id.presenter_lyrics_image);
        loopandtimeLinearLayout = findViewById(R.id.loopandtimeLinearLayout);
        loopCheckBox = findViewById(R.id.loopCheckBox);
        timeEditText = findViewById(R.id.timeEditText);
        startstopSlideShow = findViewById(R.id.startstopSlideShow);
        presenter_songbuttons = findViewById(R.id.presenter_songbuttons);
        preso_action_buttons_scroll = findViewById(R.id.preso_action_buttons_scroll);
        presenter_setbuttons = findViewById(R.id.presenter_setbuttons);
        presenter_song_buttonsListView = findViewById(R.id.presenter_song_buttonsListView);
        SwitchCompat autoProject = findViewById(R.id.autoProject);

        // Quick nav buttons
        nav_prevsong = findViewById(R.id.nav_prevsong);
        nav_nextsong = findViewById(R.id.nav_nextsong);
        nav_prevsection = findViewById(R.id.nav_prevsection);
        nav_nextsection = findViewById(R.id.nav_nextsection);
        enabledisableButton(nav_prevsong, false);
        enabledisableButton(nav_nextsong, false);
        enabledisableButton(nav_prevsection, false);
        enabledisableButton(nav_nextsection, false);

        // The buttons
        presenter_project_group = findViewById(R.id.presenter_project_group);
        presenter_logo_group = findViewById(R.id.presenter_logo_group);
        presenter_background_group = findViewById(R.id.presenter_background_group);
        presenter_blank_group = findViewById(R.id.presenter_blank_group);
        presenter_alert_group = findViewById(R.id.presenter_alert_group);
        presenter_audio_group = findViewById(R.id.presenter_audio_group);
        presenter_dB_group = findViewById(R.id.presenter_dB_group);
        presenter_slide_group = findViewById(R.id.presenter_slide_group);
        presenter_scripture_group = findViewById(R.id.presenter_scripture_group);
        presenter_display_group = findViewById(R.id.presenter_display_group);

        // The toolbar
        songandauthor = findViewById(R.id.songandauthor);
        digitalclock = findViewById(R.id.digitalclock);
        songtitle_ab = findViewById(R.id.songtitle_ab);
        songkey_ab = findViewById(R.id.songkey_ab);
        songcapo_ab = findViewById(R.id.songcapo_ab);
        songauthor_ab = findViewById(R.id.songauthor_ab);
        songtitle_ab.setText(getResources().getString(R.string.presentermode));
        songkey_ab.setText("");
        songcapo_ab.setText("");
        songauthor_ab.setText("");
        batterycharge = findViewById(R.id.batterycharge);
        batteryimage = findViewById(R.id.batteryimage);
        batteryholder = findViewById(R.id.batteryholder);
        ab_toolbar = findViewById(R.id.mytoolbar);

        // The song menu
        songmenu = findViewById(R.id.songmenu);
        menuFolder_TextView = findViewById(R.id.menuFolder_TextView);
        menuCount_TextView = findViewById(R.id.menuCount_TextView);
        song_list_view = findViewById(R.id.song_list_view);
        FloatingActionButton closeSongsFAB = findViewById(R.id.closeSongsFAB);
        closeSongsFAB.setOnClickListener(view -> closeMyDrawers("song"));


        // The option menu
        optionmenu = findViewById(R.id.optionmenu);
        menuFolder_TextView = findViewById(R.id.menuFolder_TextView);
        menuFolder_TextView.setText(getString(R.string.wait));
        LinearLayout changefolder_LinearLayout = findViewById(R.id.changefolder_LinearLayout);
        changefolder_LinearLayout.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "choosefolder";
            openFragment();
        });

        // Make views focusable
        presenter_songtitle.isFocusable();
        presenter_songtitle.requestFocus();

        autoProject.setChecked(preferences.getMyPreferenceBoolean(PresenterMode.this,"presoAutoUpdateProjector",true));

        // Set the button listeners
        presenter_set.setOnClickListener(v -> {
            // Edit current set
            //mDrawerLayout.closeDrawer(expListViewOption);
            FullscreenActivity.whattodo = "editset";
            openFragment();
        });
        set_view_fab.setOnClickListener(v -> {
            // Edit current set
            //mDrawerLayout.closeDrawer(expListViewOption);
            CustomAnimations.animateFAB(set_view_fab,PresenterMode.this);
            FullscreenActivity.whattodo = "editset";
            openFragment();
        });
        startstopSlideShow.setOnClickListener(v -> {
            CustomAnimations.animateFAB(startstopSlideShow, PresenterMode.this);
            if (isplayingautoslideshow) {
                prepareStopAutoSlideShow();
            } else {
                prepareStartAutoSlideShow();
            }
        });
        autoProject.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(PresenterMode.this,"presoAutoUpdateProjector",b));

        // Scrollbars
        presenter_set_buttonsListView.setScrollbarFadingEnabled(false);
        presenter_songbuttons.setScrollbarFadingEnabled(false);
        preso_action_buttons_scroll.setScrollbarFadingEnabled(false);

        // Hide some stuff
        presenter_lyrics.setVisibility(View.VISIBLE);
        presenter_lyrics_image.setVisibility(View.GONE);

        // Disable the views until a screen is connected
        noSecondScreen();
    }
    private void screenClickListeners() {
        songandauthor.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "songdetails";
            openFragment();
        });
        batteryholder.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "actionbarinfo";
            openFragment();
        });
        pres_details.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "songdetails";
            openFragment();
        });
        presenter_order_text.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(PresenterMode.this,"usePresentationOrder",isChecked);
            refreshAll();
        });
        presenter_order_button.setOnClickListener(view -> {
            if (FullscreenActivity.isPDF) {
                StaticVariables.myToastMessage = getString(R.string.pdf_functionnotavailable);
                ShowToast.showToast(PresenterMode.this);
            } else if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "editsong";
                openFragment();
            }
        });
        nav_prevsong.setOnClickListener(view -> {
            CustomAnimations.animateFAB(nav_prevsong, PresenterMode.this);
            tryClickPreviousSongInSet();
        });
        nav_nextsong.setOnClickListener(view -> {
            CustomAnimations.animateFAB(nav_nextsong, PresenterMode.this);
            tryClickNextSongInSet();
        });
        nav_prevsection.setOnClickListener(view -> {
            CustomAnimations.animateFAB(nav_prevsection, PresenterMode.this);
            tryClickPreviousSection();
        });
        nav_nextsection.setOnClickListener(view -> {
            CustomAnimations.animateFAB(nav_nextsection, PresenterMode.this);
            tryClickNextSection();
        });
        presenter_project_group.setOnClickListener(view -> projectButtonClick());
        presenter_logo_group.setOnClickListener(view -> logoButtonClick());
        presenter_background_group.setOnClickListener(view -> backgroundButtonClick());
        presenter_blank_group.setOnClickListener(view -> blankButtonClick());
        presenter_alert_group.setOnClickListener(view -> alertButtonClick());
        presenter_audio_group.setOnClickListener(view -> audioButtonClick());
        presenter_dB_group.setOnClickListener(view -> dBButtonClick());
        presenter_slide_group.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "customreusable_slide";
            openFragment();
        });
        presenter_scripture_group.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "customreusable_scripture";
            openFragment();
        });
        presenter_display_group.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "connecteddisplay";
            openFragment();
        });
    }
    private void showCorrectViews() {
        // IV - Make sure it starts clear
        presenter_lyrics_image.setImageBitmap(null);
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            // Image and PDF files replace the slide text with an image preview
            presenter_lyrics_image.setVisibility(View.VISIBLE);
            presenter_lyrics.setVisibility(View.GONE);
            loopandtimeLinearLayout.setVisibility(View.GONE);
        } else if (FullscreenActivity.isSong) {
            presenter_lyrics_image.setVisibility(View.GONE);
            presenter_lyrics.setVisibility(View.VISIBLE);
            loopandtimeLinearLayout.setVisibility(View.GONE);
        }
        if (FullscreenActivity.isImageSlide) {
            presenter_lyrics_image.setVisibility(View.VISIBLE);
            presenter_lyrics.setVisibility(View.GONE);
            loopandtimeLinearLayout.setVisibility(View.VISIBLE);
        }
        if (FullscreenActivity.isSlide) {
            presenter_lyrics_image.setVisibility(View.GONE);
            presenter_lyrics.setVisibility(View.VISIBLE);
            loopandtimeLinearLayout.setVisibility(View.VISIBLE);
        }
    }
    private void setupSetButtons() {
        // Create a new button for each song in the Set
        setActions.prepareSetList(PresenterMode.this,preferences);
        presenter_set_buttonsListView.removeAllViews();
        try {
            if (StaticVariables.mSet != null && StaticVariables.mSet.length > 0) {
                for (int x = 0; x < StaticVariables.mSet.length; x++) {
                    // Button for the song and set
                    Button newSetButton = processSong.makePresenterSetButton(x, PresenterMode.this);
                    newSetButton.setOnClickListener(new SetButtonClickListener(x));
                    presenter_set_buttonsListView.addView(newSetButton);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class SetButtonClickListener implements View.OnClickListener {
        int which = 0;

        SetButtonClickListener(int i) {
            if (i > 0) {
                which = i;
            }
        }

        @Override
        public void onClick(View view) {
            // Make sure we are now in set view
            StaticVariables.setView = true;
            StaticVariables.indexSongInSet = which;

            // Scroll this song to the top of the list
            presenter_setbuttons.smoothScrollTo(0, view.getTop());

            // We will use the first section in the new song
            StaticVariables.currentSection = 0;

            // Unhightlight all of the items in the set button list except this one
            for (int v = 0; v < presenter_set_buttonsListView.getChildCount(); v++) {
                if (v != which) {
                    // Change the background colour of this button to show it is active
                    processSong.unhighlightPresenterSetButton((Button) presenter_set_buttonsListView.getChildAt(v));
                } else {
                    // Change the background colour of this button to show it is active
                    processSong.highlightPresenterSetButton((Button) presenter_set_buttonsListView.getChildAt(v));
                }
            }

            // Identify our new position in the set
            StaticVariables.indexSongInSet = which;
            if (StaticVariables.mSetList != null && StaticVariables.mSetList.length > which) {
                StaticVariables.whatsongforsetwork = StaticVariables.mSetList[which];
                FullscreenActivity.linkclicked = StaticVariables.mSetList[which];
                if (which < 1) {
                    StaticVariables.previousSongInSet = "";
                } else {
                    StaticVariables.previousSongInSet = StaticVariables.mSetList[which - 1];
                }
                if (which == (StaticVariables.setSize - 1)) {
                    StaticVariables.nextSongInSet = "";
                } else {
                    StaticVariables.previousSongInSet = StaticVariables.mSetList[which + 1];
                }

                // Call the script to get the song location.
                setActions.getSongFileAndFolder(PresenterMode.this);
                findSongInFolders();
                prepareSongMenu();

                // Close the drawers in case they are open
                closeMyDrawers("both");

                // Load the song
                loadSong();
            }
        }
    }
    private void setupSongButtons() {
        // Create a new button for each songSection
        // If the 'song' is custom images, set them as the background
        presenter_song_buttonsListView.removeAllViews();
        // IV - When there is no song title use filename
        if (StaticVariables.mTitle.isEmpty()) {
            presenter_songtitle.setText(StaticVariables.songfilename);
        } else {
            presenter_songtitle.setText(StaticVariables.mTitle);
        }
        presenter_author.setText(StaticVariables.mAuthor);
        presenter_copyright.setText(StaticVariables.mCopyright);
        // IV - PDF files will have null mPresentation
        if ((StaticVariables.mPresentation == null) || (StaticVariables.mPresentation.isEmpty())) {
            presenter_order_text.setText(getResources().getString(R.string.notset));
        } else {
            presenter_order_text.setText(StaticVariables.mPresentation);
        }
        // Need to decide if checkbox is on or off
        presenter_order_text.setChecked(preferences.getMyPreferenceBoolean(PresenterMode.this,"usePresentationOrder",false));

        imagelocs = null;

        // Song and set button variables
        LinearLayout newSongSectionGroup;
        Button newSongButton;
        TextView newSongSectionText;
        if (FullscreenActivity.isPDF) {
            int pages = FullscreenActivity.pdfPageCount;
            if (pages > 0) {
                for (int p = 0; p < pages; p++) {
                    String sectionText = (p + 1) + "";
                    // IV - Make the buttons bigger (fudge!)
                    String buttonText = "▼\n" + getResources().getString(R.string.pdf_selectpage) + " " + (p + 1) + "\n▲";
                    newSongSectionGroup = processSong.makePresenterSongButtonLayout(PresenterMode.this);
                    newSongSectionText = processSong.makePresenterSongButtonSection(PresenterMode.this, sectionText);
                    newSongButton = processSong.makePresenterSongButtonContent(PresenterMode.this, buttonText);
                    newSongButton.setOnClickListener(new SectionButtonClickListener(p));
                    newSongSectionGroup.addView(newSongSectionText);
                    newSongSectionGroup.addView(newSongButton);
                    presenter_song_buttonsListView.addView(newSongSectionGroup);
                }
            }
        } else if (FullscreenActivity.isImage) {
            StaticVariables.uriToLoad = storageAccess.getUriForItem(PresenterMode.this, preferences, "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);
            if (storageAccess.uriExists(PresenterMode.this,StaticVariables.uriToLoad)) {
                String sectionText = getResources().getString(R.string.image);
                String buttonText = StaticVariables.songfilename;
                newSongSectionGroup = processSong.makePresenterSongButtonLayout(PresenterMode.this);
                newSongSectionText = processSong.makePresenterSongButtonSection(PresenterMode.this, sectionText);
                newSongButton = processSong.makePresenterSongButtonContent(PresenterMode.this, buttonText);
                newSongButton.setOnClickListener(new SectionButtonClickListener(0));
                newSongSectionGroup.addView(newSongSectionText);
                newSongSectionGroup.addView(newSongButton);
                presenter_song_buttonsListView.addView(newSongSectionGroup);
            }
        } else {
            if (StaticVariables.whichSongFolder.contains("../Images")) {
                // Custom images so split the mUser3 field by newline.  Each value is image location
                imagelocs = StaticVariables.mUser3.split("\n");
            }

            if (StaticVariables.songSections != null && StaticVariables.songSections.length > 0) {
                int numsectionbuttons = StaticVariables.songSections.length;
                for (int x = 0; x < numsectionbuttons; x++) {

                    // Get the image locations if they exist
                    String thisloc = null;
                    if (imagelocs != null && imagelocs[x] != null) {
                        thisloc = imagelocs[x];
                    }

                    StringBuilder buttonText;
                    if (StaticVariables.songSections!=null && StaticVariables.songSections.length>x) {
                        buttonText = new StringBuilder(StaticVariables.songSections[x]);
                    } else {
                        buttonText = new StringBuilder();
                    }
                    // Get the text for the button
                    if (FullscreenActivity.isImageSlide) {
                        if (thisloc == null) {
                            thisloc = "";
                        }
                        buttonText = new StringBuilder(thisloc);
                        // Try to remove everything except the name
                        if (buttonText.toString().contains("/") && buttonText.lastIndexOf("/") < buttonText.length() - 1) {
                            buttonText = new StringBuilder(buttonText.substring(buttonText.lastIndexOf("/") + 1));
                        }
                    }

                    // If we aren't showing the chords, strip them out
                    if (!preferences.getMyPreferenceBoolean(PresenterMode.this,"presoShowChords",false)) {
                        String[] l;
                        l = buttonText.toString().split("\n");

                        buttonText = new StringBuilder();
                        // Add the lines back in, but removing the ones starting with .
                        for (String eachline : l) {
                            if (!eachline.startsWith(".")) {
                                buttonText.append(eachline.trim()).append("\n");
                            }
                        }
                        buttonText = new StringBuilder(buttonText.toString().trim());
                    }

                    // Get the button information (type of section)
                    String sectionText;
                    try {
                        sectionText = StaticVariables.songSectionsLabels[x];
                    } catch (Exception e) {
                        sectionText = "";
                    }

                    newSongSectionGroup = processSong.makePresenterSongButtonLayout(PresenterMode.this);
                    newSongSectionText = processSong.makePresenterSongButtonSection(PresenterMode.this, sectionText.replace("_", " "));
                    newSongButton = processSong.makePresenterSongButtonContent(PresenterMode.this, buttonText.toString());

                    if (FullscreenActivity.isImageSlide || FullscreenActivity.isSlide) {
                        // Make sure the time, loop and autoslideshow buttons are visible
                        loopandtimeLinearLayout.setVisibility(View.VISIBLE);
                        enabledisableButton(startstopSlideShow, true);
                        // Just in case we were playing a slide show, stop it
                        prepareStopAutoSlideShow();
                        // Set the appropiate values
                        if (StaticVariables.mUser1 != null) {
                            timeEditText.setText(StaticVariables.mUser1);
                        }
                        loopCheckBox.setChecked(StaticVariables.mUser2 != null && StaticVariables.mUser2.equals("true"));

                    } else {
                        // Otherwise, hide them
                        loopandtimeLinearLayout.setVisibility(View.GONE);
                    }
                    newSongButton.setOnClickListener(new SectionButtonClickListener(x));
                    newSongSectionGroup.addView(newSongSectionText);
                    newSongSectionGroup.addView(newSongButton);
                    presenter_song_buttonsListView.addView(newSongSectionGroup);
                }
            }
        }
        // Select the current button if we can
        selectSectionButtonInSong(StaticVariables.currentSection);
    }

    @Override
    public void selectSection(int which) {
        selectSectionButtonInSong(which);
    }
    private void selectSectionButtonInSong(int which) {

        try {
            StaticVariables.currentSection = which;
            // IV - Changed to move through displayed buttons without reference to songSections which do not exists for PDF and Img
            if (presenter_song_buttonsListView.getChildCount() > 0) {
                // if which=-1 then we want to pick the first section of the previous song in set
                // Otherwise, move to the next one.
                // If we are at the end, move to the nextsonginset

                if (StaticVariables.currentSection < 0 && !isplayingautoslideshow) {
                    StaticVariables.currentSection = 0;
                    tryClickPreviousSongInSet();
                } else if (StaticVariables.currentSection >= presenter_song_buttonsListView.getChildCount() && !isplayingautoslideshow) {
                    StaticVariables.currentSection = 0;
                    tryClickNextSongInSet();
                } else if (StaticVariables.currentSection < 0 || StaticVariables.currentSection >= presenter_song_buttonsListView.getChildCount()) {
                    StaticVariables.currentSection = 0;
                }

                // enable or disable the quick nav buttons
                fixNavButtons();

                LinearLayout row = (LinearLayout) presenter_song_buttonsListView.getChildAt(StaticVariables.currentSection);
                Button thisbutton = (Button) row.getChildAt(1);
                thisbutton.performClick();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void unhighlightAllSetButtons() {
        // Unhighlighting all buttons
        int numbuttons = presenter_set_buttonsListView.getChildCount();
        for (int z = 0; z < numbuttons; z++) {
            processSong.unhighlightPresenterSetButton((Button) presenter_set_buttonsListView.getChildAt(z));
        }
    }
    private void fixNavButtons() {
        // By default disable them all!
        //enabledisableButton(nav_prevsection,false);
        //enabledisableButton(nav_nextsection,false);
        enabledisableButton(nav_prevsong, false);
        enabledisableButton(nav_nextsong, false);

        // Show the previous section button if we currently showing a section higher than 0
        enabledisableButton(nav_prevsection, StaticVariables.currentSection > 0);

        // Show the next section button if we are currently in a section lower than the count by 1
        int sectionsavailable = presenter_song_buttonsListView.getChildCount();
        enabledisableButton(nav_nextsection, StaticVariables.currentSection < sectionsavailable - 1);

        // Enable the previous set button if we are in set view and indexSongInSet is >0 (but less than set size)
        int numsongsinset = 0;
        if (StaticVariables.mSetList != null) {
            numsongsinset = StaticVariables.mSetList.length;
        }
        enabledisableButton(nav_prevsong, StaticVariables.setView && StaticVariables.indexSongInSet > 0 && StaticVariables.indexSongInSet < numsongsinset);

        // Enable the next set button if we are in set view and index SongInSet is < set size -1
        enabledisableButton(nav_nextsong, StaticVariables.setView && StaticVariables.indexSongInSet > -1 && StaticVariables.indexSongInSet < numsongsinset - 1);

        /*if (FullscreenActivity.songSections!=null && FullscreenActivity.currentSection>=FullscreenActivity.songSections.length) {
            enabledisableButton(nav_nextsection,false);
        } else if (FullscreenActivity.songSections!=null){
            enabledisableButton(nav_nextsection,true);
        } else {
            enabledisableButton(nav_nextsection,false);
        }
        // Initially disable the set buttons
        enabledisableButton(nav_prevsong,false);
        enabledisableButton(nav_nextsong,false);

        if (FullscreenActivity.setView && FullscreenActivity.indexSongInSet>0) {
            enabledisableButton(nav_prevsong,true);
        }
        if (FullscreenActivity.setView && FullscreenActivity.indexSongInSet>=0 &&
                FullscreenActivity.indexSongInSet<FullscreenActivity.mSetList.length-1) {
            enabledisableButton(nav_nextsong, true);
        }*/
    }
    @Override
    public void goToPreviousItem() {
        // Called from nearby connection
        tryClickPreviousSection();
    }
    @Override
    public void goToNextItem() {
        // Called from nearby connection
        tryClickPreviousSection();
    }
    private void tryClickNextSection() {
        if (StaticVariables.currentSection < presenter_song_buttonsListView.getChildCount() - 1) {
            StaticVariables.currentSection += 1;
            autoproject = true;
            preso_action_buttons_scroll.smoothScrollTo(0, presenter_project_group.getTop());
            selectSectionButtonInSong(StaticVariables.currentSection);
        }
    }
    private void tryClickPreviousSection() {
        if (StaticVariables.currentSection > 0) {
            StaticVariables.currentSection -= 1;
            autoproject = true;
            preso_action_buttons_scroll.smoothScrollTo(0, presenter_project_group.getTop());
            selectSectionButtonInSong(StaticVariables.currentSection);
        }
    }
    private void tryClickNextSongInSet() {
        if (StaticVariables.mSetList != null && StaticVariables.indexSongInSet>-1 && StaticVariables.indexSongInSet < StaticVariables.mSetList.length - 1) {
            StaticVariables.indexSongInSet += 1;
            StaticVariables.currentSection = 0;
            autoproject = true;
            doMoveInSet();
        }
    }
    private void tryClickPreviousSongInSet() {
        if (StaticVariables.mSetList != null && StaticVariables.mSetList.length > StaticVariables.indexSongInSet &&
                StaticVariables.indexSongInSet > 0) {
            StaticVariables.indexSongInSet -= 1;
            StaticVariables.currentSection = 0;
            autoproject = true;
            doMoveInSet();
        }
    }
    private void enabledisableButton(FloatingActionButton fab, boolean enable) {
        if (fab!=null) {
            fab.setEnabled(enable);
            if (enable) {
                fab.setAlpha(1.0f);
            } else {
                fab.setAlpha(0.5f);
            }
        }
    }
    @Override
    public void doMoveSection() {
        switch (StaticVariables.setMoveDirection) {
            case "forward":
                StaticVariables.currentSection += 1;
                selectSectionButtonInSong(StaticVariables.currentSection);
                break;
            case "back":
                StaticVariables.currentSection -= 1;
                selectSectionButtonInSong(StaticVariables.currentSection);
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
    public void confirmedAction() {
        switch (FullscreenActivity.whattodo) {
            case "exit":
                this.finish();
                break;

            case "saveset":
                // Save the set
                setActions.saveSetMessage(PresenterMode.this, preferences, storageAccess, processSong);
                refreshAll();
                break;

            case "clearset":
                // Clear the set
                setActions.clearSet(PresenterMode.this,preferences);
                refreshAll();
                break;

            case "deletesong":
                // Delete current song
                Uri uri = storageAccess.getUriForItem(PresenterMode.this, preferences, "Songs", StaticVariables.whichSongFolder,
                        StaticVariables.songfilename);
                storageAccess.deleteFile(PresenterMode.this, uri);
                // If we are autologging CCLI information
                if (preferences.getMyPreferenceBoolean(PresenterMode.this,"ccliAutomaticLogging",false)) {
                    PopUpCCLIFragment.addUsageEntryToLog(PresenterMode.this, preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                            StaticVariables.mTitle, StaticVariables.mAuthor,
                            StaticVariables.mCopyright, StaticVariables.mCCLI, "2"); // Deleted
                }

                // Remove the item from the SQL database
                if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                    nonOpenSongSQLiteHelper.deleteSong(PresenterMode.this, storageAccess, preferences, nonOpenSongSQLite.getSongid());
                }
                if (sqLite!=null && sqLite.getSongid()!=null) {
                    sqLiteHelper.deleteSong(PresenterMode.this, sqLite.getSongid());
                }
                prepareSongMenu();
                // IV - Load song to display as deleted
                loadSong();
                break;

            case "deleteset":
                // Delete set
                setActions.deleteSet(PresenterMode.this, preferences, storageAccess);
                refreshAll();
                break;

            case "wipeallsongs":
                // Wipe all songs - Getting rid of this!!!!!
                Log.d(TAG, "Trying wipe songs folder - ignoring");
                /*storageAccess.wipeFolder(PresenterMode.this, preferences, "Songs", "");
                // Rebuild the song list
                storageAccess.listSongs(PresenterMode.this, preferences);
                listSongFiles.songUrisInFolder(PresenterMode.this, preferences);
                refreshAll();*/
                break;

            /*case "resetcolours":
                // Reset the theme colours
                PopUpThemeChooserFragment.getDefaultColours();
                Preferences.savePreferences();
                refreshAll();
                FullscreenActivity.whattodo = "changetheme";
                openFragment();
                break;*/
        }
    }

    private void loadImagePreview() {
        // Make the appropriate bits visible
        presenter_lyrics.setVisibility(View.GONE);

        // Process the image location into an URI, then get the sizes
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //Returns null, sizes are in the options variable
        InputStream inputStream = storageAccess.getInputStream(PresenterMode.this, StaticVariables.uriToLoad);
        if (inputStream != null) {
            BitmapFactory.decodeStream(inputStream, null, options);
            int imgwidth = options.outWidth;
            int imgheight = options.outHeight;
            int widthavail = 800;
            int heightavail = 800;
            float xscale = (float) widthavail / (float) imgwidth;
            float yscale = (float) heightavail / (float) imgheight;
            // Now decide on the scaling required....
            if (xscale > yscale) {
                xscale = yscale;
            }
            int glidewidth = (int) ((float) imgwidth * xscale);
            int glideheight = (int) ((float) imgheight * xscale);

            // Draw the image to the preview window
            presenter_lyrics_image.setBackgroundColor(0x00000000);
            RequestOptions myOptions = new RequestOptions()
                    .override(glidewidth, glideheight);
            Glide.with(PresenterMode.this).load(StaticVariables.uriToLoad).apply(myOptions).into(presenter_lyrics_image);
        }

        presenter_lyrics_image.setVisibility(View.VISIBLE);

        if (autoproject || preferences.getMyPreferenceBoolean(PresenterMode.this,"presoAutoUpdateProjector",true)) {
            autoproject = false;
            presenter_project_group.performClick();
        }
    }

    private void sendSongSectionToConnected() {
        // IV - Do not send section 0 payload when loading a song
        if (!FullscreenActivity.alreadyloading) {
            String infoPayload = "___section___" + StaticVariables.currentSection;
            nearbyConnections.doSendPayloadBytes(infoPayload);
        }
    }

    private void getBluetoothName() {
        try {
            if (FullscreenActivity.mBluetoothAdapter == null) {
                FullscreenActivity.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            FullscreenActivity.mBluetoothName = FullscreenActivity.mBluetoothAdapter.getName();
            if (FullscreenActivity.mBluetoothName == null) {
                FullscreenActivity.mBluetoothName = "Unknown";
            }
        } catch (Exception e) {
            FullscreenActivity.mBluetoothName = "Unknown";
        }
    }


    // Loading the song
    @Override
    public void loadSongFromSet() {
        // Redraw the set buttons as the user may have changed the order
        refreshAll();

        closePopUps();

        StaticVariables.setView = true;
        // Specify which songinset button
        StaticVariables.currentSection = 0;

        // Select it
        if (presenter_set_buttonsListView.getChildCount() > StaticVariables.indexSongInSet) {
            Button which_song_to_click = (Button) presenter_set_buttonsListView.getChildAt(StaticVariables.indexSongInSet);
            which_song_to_click.performClick();
        }
    }

    // The right hand column buttons
    private void projectButtonClick() {
        // IV - Button clicks can be called from other button clicks as a subroutine
        // IV - Only the first button sets the highlights, subroutine calls do not
        if (!buttonInTransition) {
            highlightButtonClickedHalf(presenter_project_group);
            unhighlightButtonClicked(presenter_logo_group);
            unhighlightButtonClicked(presenter_background_group);
            unhighlightButtonClicked(presenter_blank_group);
        }
        // IV - Disable action buttons here
        presenter_project_group.setEnabled(false);
        presenter_logo_group.setEnabled(false);
        presenter_background_group.setEnabled(false);
        presenter_blank_group.setEnabled(false);

        try {
            projectButton_isSelected = !projectButton_isSelected;
            if (presenter_lyrics.getText()==null) {
                presenter_lyrics.setText("");
            }
            if (!FullscreenActivity.isPDF && !FullscreenActivity.isImage && !FullscreenActivity.isImageSlide) {
                StaticVariables.projectedContents[StaticVariables.currentSection] = presenter_lyrics.getText().toString().split("\n");
                int linesnow = StaticVariables.projectedContents[StaticVariables.currentSection].length;
                StaticVariables.projectedLineTypes[StaticVariables.currentSection] = new String[linesnow];
                for (int i = 0; i < linesnow; i++) {
                    StaticVariables.projectedLineTypes[StaticVariables.currentSection][i] =
                        processSong.determineLineTypes(StaticVariables.projectedContents[StaticVariables.currentSection][i], PresenterMode.this);
                    if (StaticVariables.projectedContents[StaticVariables.currentSection][i] != null &&
                            StaticVariables.projectedContents[StaticVariables.currentSection][i].length() > 0 && (StaticVariables.projectedContents[StaticVariables.currentSection][i].startsWith(" ") ||
                            StaticVariables.projectedContents[StaticVariables.currentSection][i].startsWith(".") || StaticVariables.projectedContents[StaticVariables.currentSection][i].startsWith(";"))) {
                        StaticVariables.projectedContents[StaticVariables.currentSection][i] = StaticVariables.projectedContents[StaticVariables.currentSection][i].replaceAll("\\s+$", "");
                    }
                }
            }

            // Turn off the other actions buttons as we are now projecting!
            if (logoButton_isSelected) {
                presenter_logo_group.performClick();  // This turns off the logo
            }
            if (backgroundButton_isSelected) {
                presenter_background_group.performClick();  // This turns off background only
            }
            if (blankButton_isSelected) {
                presenter_blank_group.performClick();
            }

            // Update the projector
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.doUpdate();

                    // If we are autologging CCLI information
                    if (newsongloaded && preferences.getMyPreferenceBoolean(PresenterMode.this,"ccliAutomaticLogging",false)) {
                        PopUpCCLIFragment.addUsageEntryToLog(PresenterMode.this, preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                                StaticVariables.mTitle, StaticVariables.mAuthor,
                                StaticVariables.mCopyright, StaticVariables.mCCLI, "5"); // Presented
                        newsongloaded = false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.doUpdate();
                    // If we are autologging CCLI information
                    if (newsongloaded && preferences.getMyPreferenceBoolean(PresenterMode.this,"ccliAutomaticLogging",false)) {
                        PopUpCCLIFragment.addUsageEntryToLog(PresenterMode.this, preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                                StaticVariables.mTitle, StaticVariables.mAuthor,
                                StaticVariables.mCopyright, StaticVariables.mCCLI, "5"); // Presented
                        newsongloaded = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            projectButton_isSelected = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // IV - After a delay enable and set correct highlights of action buttons
        enableActionButtonsHandler.removeCallbacks(enableActionButtonsRunnable);
        enableActionButtonsHandler.postDelayed(enableActionButtonsRunnable, (long) (3.2 * preferences.getMyPreferenceInt(this, "presoTransitionTime",800)));
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
            ArrayList<String> songIds = storageAccess.listSongs(PresenterMode.this, preferences);
            storageAccess.writeSongIDFile(PresenterMode.this, preferences, songIds);

            // Try to create the basic database
            sqLiteHelper.resetDatabase(PresenterMode.this);
            sqLiteHelper.insertFast(PresenterMode.this,storageAccess);

            // Build the full index
            indexSongs.fullIndex(PresenterMode.this,preferences,storageAccess, sqLiteHelper, songXML,
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
            try {
                // This is for the File Chooser returning a file uri
                String filelocation;
                if (data.getExtras() != null) {
                    // This is from the FolderPicker.class
                    filelocation = data.getExtras().getString("data");
                } else {
                    // This is the built in file picker
                    filelocation = data.getDataString();
                }

                String filename = storageAccess.getActualFilename(PresenterMode.this,filelocation);

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
                            FullscreenActivity.file_uri = FileProvider.getUriForFile(PresenterMode.this,
                                    "OpenSongAppFiles", f);
                        }

                        // Get persistent permissions
                        try {
                            final int takeFlags = data.getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            // Check for the freshest data.
                            getContentResolver().takePersistableUriPermission(FullscreenActivity.file_uri, takeFlags);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        openFragment();
                    } else {
                        StaticVariables.myToastMessage = getString(R.string.file_type_unknown);
                        ShowToast.showToast(PresenterMode.this);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == StaticVariables.REQUEST_PROFILE_LOAD && data!=null && data.getData()!=null) {
            // Loading in a profile
            new Thread(() -> {
                boolean success = profileActions.doLoadProfile(PresenterMode.this,preferences,storageAccess,data.getData());
                if (success) {
                    StaticVariables.myToastMessage = getString(R.string.success);
                } else {
                    StaticVariables.myToastMessage = getString(R.string.error);
                }
                // Once done, reload everything
                runOnUiThread(() -> {
                    ShowToast.showToast(PresenterMode.this);
                    loadStartUpVariables();
                    refreshAll();
                }
                );
            }).start();

        } else if (requestCode == StaticVariables.REQUEST_PROFILE_SAVE && data!=null && data.getData()!=null) {
            // Saving a profile
            new Thread(() -> {
                boolean success = profileActions.doSaveProfile(PresenterMode.this,preferences,storageAccess,data.getData());
                if (success) {
                    StaticVariables.myToastMessage = getString(R.string.success);
                } else {
                    StaticVariables.myToastMessage = getString(R.string.error);
                }
                // Once done, say so
                runOnUiThread(() -> ShowToast.showToast(PresenterMode.this)
                );
            }).start();
        }
    }

    private class SectionButtonClickListener implements View.OnClickListener {
        int which = 0;

        SectionButtonClickListener(int i) {
            if (i > 0) {
                which = i;
            }
        }

        @Override
        public void onClick(View view) {

            // We will use this section for the song
            StaticVariables.currentSection = which;

            // Fix the nav buttons
            fixNavButtons();

            // Send section to other devices
            if (StaticVariables.isHost && StaticVariables.isConnected) {
                sendSongSectionToConnected();
            }


            // Scroll this section to the top of the list
            // Have to do this manually - add the height of the buttons before the one wanted + margin
            int totalheight = 0;
            for (int d = 0; d < which; d++) {
                totalheight += presenter_song_buttonsListView.getChildAt(d).getHeight();
                totalheight += 10;
            }
            presenter_songbuttons.smoothScrollTo(0, totalheight);


            // Unhightlight all of the items in the song button list except this one
            for (int v = 0; v < presenter_song_buttonsListView.getChildCount(); v++) {
                LinearLayout row = (LinearLayout) presenter_song_buttonsListView.getChildAt(v);
                if (v != which) {
                    processSong.unhighlightPresenterSongButton((Button) row.getChildAt(1));
                } else {
                    // Change the background colour of this button to show it is active
                    processSong.highlightPresenterSongButton((Button) row.getChildAt(1));
                }
            }

            // If this is an image, hide the text, show the image, otherwise show the text in the slide window
            if (FullscreenActivity.isPDF) {
                FullscreenActivity.pdfPageCurrent = which;
                StaticVariables.currentSection = which;
                loadPDFPagePreview();
            } else if (FullscreenActivity.isImage) {
                StaticVariables.uriToLoad = storageAccess.getUriForItem(PresenterMode.this, preferences, "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);
                loadImagePreview();
            } else if (FullscreenActivity.isImageSlide) {
                // Get the image location from the projectedSongSection
                if (imagelocs[StaticVariables.currentSection] != null) {
                    String loc = imagelocs[StaticVariables.currentSection];
                    Log.d(TAG,"image uri="+loc);
                    //StaticVariables.uriToLoad = Uri.parse(imagelocs[StaticVariables.currentSection]);
                    StaticVariables.uriToLoad = storageAccess.fixLocalisedUri(PresenterMode.this,preferences,loc);
                    loadImagePreview();
                }
            // IV - Handle a song section with content as Image Slide
            } else {
                String bit = "";
                try {
                    bit = String.valueOf(StaticVariables.sectionContents[StaticVariables.currentSection][0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (((bit.toLowerCase(Locale.ROOT).endsWith(".png") || bit.toLowerCase(Locale.ROOT).endsWith(".jpg") || bit.toLowerCase(Locale.ROOT).endsWith(".gif")) ||
                        (bit.toLowerCase(Locale.ROOT).contains("content://") || bit.toLowerCase(Locale.ROOT).contains("file://")))) {
                    StaticVariables.uriToLoad = storageAccess.fixLocalisedUri(PresenterMode.this, preferences, bit);
                    FullscreenActivity.isImage = true;
                    loadImagePreview();
                    FullscreenActivity.isImage = false;
                } else {
                    loadSongPreview();
                }
            }

            projectButton_isSelected = false;
        }
    }
    private void loadSongPreview() {
        // Set the appropriate views to visible
        presenter_lyrics_image.setVisibility(View.GONE);
        presenter_lyrics.setVisibility(View.VISIBLE);

        // Prepare the text to go in the view
        StringBuilder s = new StringBuilder();
        try {
            if (StaticVariables.projectedContents!=null && StaticVariables.projectedContents[StaticVariables.currentSection]!=null) {
                for (int w = 0; w < StaticVariables.projectedContents[StaticVariables.currentSection].length; w++) {
                    if (preferences.getMyPreferenceBoolean(PresenterMode.this,"presoShowChords",false)) {
                        s.append(StaticVariables.projectedContents[StaticVariables.currentSection][w]).append("\n");
                    } else {
                        s.append(StaticVariables.projectedContents[StaticVariables.currentSection][w].trim()).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        s = new StringBuilder(s.toString().trim());

        // And write it
        presenter_lyrics.setText(s.toString());

        if (autoproject || preferences.getMyPreferenceBoolean(PresenterMode.this,"presoAutoUpdateProjector",true)) {
            autoproject = false;
            presenter_project_group.performClick();
        }
    }

    // Interface listeners for PopUpPages
    @Override
    public void backupInstall() {
        // Songs have been imported, so update the song menu and rebuild the search index
        rebuildSearchIndex();
    }

    @Override
    public void fixSet() {
        closeMyDrawers("song");
        setupSetButtons();
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
                    ShowToast.showToast(PresenterMode.this);
                }
                break;

            case "activity":
                startActivity(i);
                finish();
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
                    Intent i = profileActions.openProfile(PresenterMode.this,preferences,storageAccess);
                    this.startActivityForResult(i, StaticVariables.REQUEST_PROFILE_LOAD);
                    refreshAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "save":
                try {
                    Intent i = profileActions.saveProfile(PresenterMode.this,preferences,storageAccess);
                    this.startActivityForResult(i, StaticVariables.REQUEST_PROFILE_SAVE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
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
    public void doSendPayloadBytes(String infoPayload) {
        nearbyConnections.doSendPayloadBytes(infoPayload);
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class PrepareOptionMenu extends AsyncTask<Object, Void, String> {

        public void onPreExecute() {
            try {
                optionmenu = findViewById(R.id.optionmenu);
                optionmenu.removeAllViews();
                optionmenu.addView(OptionMenuListeners.prepareOptionMenu(PresenterMode.this, getSupportFragmentManager()));
                if (optionmenu != null) {
                    OptionMenuListeners.optionListeners(optionmenu, PresenterMode.this, preferences, storageAccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Get the current set list
            try {
                setActions.prepareSetList(PresenterMode.this,preferences);
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
            OptionMenuListeners.updateMenuVersionNumber(PresenterMode.this, findViewById(R.id.menu_version_bottom));
        }

    }
    @Override
    public void doEdit() {
        // IV - Was "extractPDF" for PDF which crashed(!), all now get editsong
        FullscreenActivity.whattodo = "editsong";
        openFragment();
    }
    @Override
    public void openFragment() {
        // Load the whichSongFolder in case we were browsing elsewhere
        StaticVariables.whichSongFolder = preferences.getMyPreferenceString(PresenterMode.this,
                "whichSongFolder",getString(R.string.mainfoldername));

        // Initialise the newFragment
        newFragment = OpenFragment.openFragment(PresenterMode.this);
        String message = OpenFragment.getMessage(PresenterMode.this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(newFragment,message);


        if (newFragment != null && !this.isFinishing()) {
            try {
                ft.commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onSongImportDone() {
        rebuildSearchIndex();
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
    public void loadCustomReusable() {
        doCancelAsyncTask(load_customreusable);
        load_customreusable = new LoadCustomReusable();
        try {
            load_customreusable.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class ShareSong extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                String title = getString(R.string.exportcurrentsong);
                Intent emailIntent = exportPreparer.exportSong(PresenterMode.this, preferences,
                        FullscreenActivity.bmScreen, storageAccess, processSong,makePDF,sqLiteHelper);
                Intent chooser = Intent.createChooser(emailIntent, title);
                startActivity(chooser);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
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

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class ShareActivityLog extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                Intent emailIntent = exportPreparer.exportActivityLog(PresenterMode.this, preferences, storageAccess);
                startActivityForResult(Intent.createChooser(emailIntent, "ActivityLog.xml"), 2222);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
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
    public void showToastMessage(String message) {
        if (message != null && !message.isEmpty()) {
            StaticVariables.myToastMessage = message;
            ShowToast.showToast(PresenterMode.this);
        }
    }
    @Override
    public void shuffleSongsInSet() {
        setActions.indexSongInSet();
        fixSet();
        newFragment = PopUpSetViewNew.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(newFragment,"dialog");

        if (newFragment != null && !PresenterMode.this.isFinishing()) {
            try {
                ft.commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //newFragment = PopUpSetViewNew.newInstance();
        //newFragment.show(getSupportFragmentManager(), "dialog");
    }
    @Override
    public void splashScreen() {
        Intent intent = new Intent();
        intent.putExtra("showsplash",true);
        intent.setClass(PresenterMode.this, BootUpCheck.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void toggleDrawerSwipe() {
        if (preferences.getMyPreferenceBoolean(PresenterMode.this,"swipeForMenus",true)) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        closeMyDrawers("both");
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

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class ShareSet extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                String title = getString(R.string.exportsavedset);
                ExportPreparer exportPreparer = new ExportPreparer();
                Intent emailIntent = exportPreparer.exportSet(PresenterMode.this, preferences, storageAccess, processSong, makePDF, sqLiteHelper);
                Intent chooser = Intent.createChooser(emailIntent, title);
                startActivity(chooser);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
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

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class LoadCustomReusable extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... obj) {
            try {
                LoadXML.prepareLoadCustomReusable(PresenterMode.this, preferences, storageAccess,
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

                    if (newFragment != null && !PresenterMode.this.isFinishing()) {
                        try {
                            ft.commitAllowingStateLoss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // This reopens the choose backgrounds popupFragment
                    //newFragment = PopUpCustomSlideFragment.newInstance();
                    //newFragment.show(getSupportFragmentManager(), "dialog");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void removeSongFromSet(int val) {
        // Vibrate to let the user know something happened
        DoVibrate.vibrate(PresenterMode.this, 50);

        // Take away the menu item
        String tempSong = StaticVariables.mSetList[val];
        StaticVariables.mSetList[val] = "";

        StringBuilder sb = new StringBuilder();
        for (String aMSetList : StaticVariables.mSetList) {
            if (!aMSetList.isEmpty()) {
                sb.append("$**_").append(aMSetList).append("_**$");
            }
        }

        preferences.setMyPreferenceString(PresenterMode.this,"setCurrent",sb.toString());

        // Tell the user that the song has been removed.
        showToastMessage("\"" + tempSong + "\" "
                + getResources().getString(R.string.removedfromset));

        //Check to see if our set list is still valid
        setActions.prepareSetList(PresenterMode.this,preferences);
        prepareOptionMenu();
        fixSet();

        closeMyDrawers("option");
    }
    @Override
    public void changePDFPage(int page, String direction) {
        FullscreenActivity.whichDirection = direction;
        FullscreenActivity.pdfPageCurrent = page;
        StaticVariables.currentSection = page;
        if (presenter_song_buttonsListView.getChildCount()>page) {
            LinearLayout row = (LinearLayout) presenter_song_buttonsListView.getChildAt(page);
            Button thisbutton = (Button) row.getChildAt(1);
            thisbutton.performClick();
        }
    }
    @Override
    public void doDownload(String filename) {
        if (do_download!=null) {
            doCancelAsyncTask(do_download);
        }
        do_download = new DownloadTask(PresenterMode.this,filename);
        try {
            do_download.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void sendMidi() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI) &&
                preferences.getMyPreferenceBoolean(PresenterMode.this,"midiSendAuto",false) &&
                StaticVariables.midiDevice!=null &&
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
                    for (String ml : midilines) {
                        Log.d(TAG, "Sending "+ml);
                        if (midi!=null) {
                            midi.sendMidi(midi.returnBytesFromHexText(ml));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // The song index
    private void displayIndex(ArrayList<SongMenuViewItems> songMenuViewItems,
                              SongMenuAdapter songMenuAdapter) {
        LinearLayout indexLayout = findViewById(R.id.side_index);
        // IV - Always displayed for layout consistency - only populate if in use.
        indexLayout.removeAllViews();
        if (preferences.getMyPreferenceBoolean(PresenterMode.this,"songMenuAlphaIndexShow",true)) {
            TextView textView;
            final Map<String,Integer> map = songMenuAdapter.getAlphaIndex(PresenterMode.this,songMenuViewItems);
            Set<String> setString = map.keySet();
            List<String> indexList = new ArrayList<>(setString);
            for (String index : indexList) {
                textView = (TextView) View.inflate(PresenterMode.this,R.layout.leftmenu, null);
                textView.setTextSize(preferences.getMyPreferenceFloat(PresenterMode.this,"songMenuAlphaIndexSize", 4.0f));
                int i = (int) preferences.getMyPreferenceFloat(PresenterMode.this,"songMenuAlphaIndexSize",14.0f) *2;
                textView.setPadding(i,i,i,i);
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
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                indexLayout.addView(textView);
            }
        }
    }

    private void startCamera() {
        closeMyDrawers("option");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            Uri photoUri = getImageUri();
            Log.d(TAG, "photoUri=" + photoUri);
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

            Uri imageUri = FileProvider.getUriForFile(PresenterMode.this, "OpenSongAppFiles", image);
            // Save a file: path for use with ACTION_VIEW intents
            FullscreenActivity.mCurrentPhotoPath = imageUri.toString();
            return imageUri;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public boolean onQueryTextChange(String newText) {
        // Replace unwanted symbols
        newText = processSong.removeUnwantedSymbolsAndSpaces(PresenterMode.this, preferences, newText);
        // TODO Not sure if this does anything as FullscreenActivity.sva is never assigned anything!
        if (FullscreenActivity.sva != null) {
            FullscreenActivity.sva.getFilter().filter(newText);
        }
        return false;
    }

    // The stuff to deal with the overall views
    @Override
    public void refreshAll() {
        // Clear the set and song section buttons
        presenter_set_buttonsListView.removeAllViews();
        presenter_song_buttonsListView.removeAllViews();
        presenter_lyrics.setText("");
        setupSetButtons();

        prepareOptionMenu();
        prepareSongMenu();
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
    @Override
    public void closePopUps() {
        try {
            if (newFragment != null) {
                newFragment.dismiss();
            }
        } catch (Exception e) {
            // Oops
        }
    }
    @Override
    public void updatePresentationOrder() {
        doEdit();
    }

    // The stuff to deal with the slideshow
    private void prepareStopAutoSlideShow() {
        if (autoslideshowtask != null) {
            try {
                autoslideshowtask.cancel(true);
                autoslideshowtask = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isplayingautoslideshow = false;
        startstopSlideShow.setImageResource(R.drawable.ic_play_white_36dp);
        enabledisableButton(startstopSlideShow, true);
    }
    private void prepareStartAutoSlideShow() {
        // Stop the slideshow if it already happening
        prepareStopAutoSlideShow();

        try {
            autoslidetime = Integer.parseInt(timeEditText.getText().toString());
        } catch (Exception e) {
            autoslidetime = 0;
        }
        autoslideloop = loopCheckBox.isChecked();

        if (autoslidetime > 0) {
            // Start asynctask that recalls every autoslidetime
            // Once we have reached the end of the slide group we either
            // Start again (if autoslideloop)
            // Or we exit autoslideshow
            projectButtonClick();
            isplayingautoslideshow = true;
            startstopSlideShow.setImageResource(R.drawable.ic_stop_white_36dp);
            doCancelAsyncTask(autoslideshowtask);
            autoslideshowtask = new AutoSlideShow();
            autoslideshowtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            showToastMessage(getResources().getString(R.string.bad_time));
        }
        enabledisableButton(startstopSlideShow, true);

    }
    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class AutoSlideShow extends AsyncTask<Object, Void, String> {

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                if (!cancelled) {
                    // Check if we can move to the next section in the song
                    if (StaticVariables.currentSection < StaticVariables.songSections.length - 1 && isplayingautoslideshow) {
                        // Move to next song section
                        StaticVariables.currentSection++;
                        selectSectionButtonInSong(StaticVariables.currentSection);
                        prepareStopAutoSlideShow();
                        prepareStartAutoSlideShow();
                    } else if (autoslideloop && StaticVariables.currentSection >= (StaticVariables.songSections.length - 1) && isplayingautoslideshow) {
                        // Go back to first song section
                        StaticVariables.currentSection = 0;
                        selectSectionButtonInSong(StaticVariables.currentSection);
                        prepareStopAutoSlideShow();
                        prepareStartAutoSlideShow();
                    } else {
                        // Stop autoplay
                        prepareStopAutoSlideShow();
                    }
                }
            } catch (Exception e) {
                //  Oops
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                // Get clock time
                long start = System.currentTimeMillis();
                long end = start;
                while (end < (start + (autoslidetime * 1000L)) && isplayingautoslideshow) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    end = System.currentTimeMillis();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
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
                customSlide.addCustomSlide(PresenterMode.this, preferences);
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
                    DoVibrate.vibrate(PresenterMode.this, 50);

                    //invalidateOptionsMenu();
                    prepareOptionMenu();
                    fixSet();
                    closeMyDrawers("option_delayed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void logoButtonClick() {
        if (!buttonInTransition) {
            unhighlightButtonClicked(presenter_project_group);
            highlightButtonClickedHalf(presenter_logo_group);
            unhighlightButtonClicked(presenter_background_group);
            unhighlightButtonClicked(presenter_blank_group);
        }
        presenter_project_group.setEnabled(false);
        presenter_logo_group.setEnabled(false);
        presenter_background_group.setEnabled(false);
        presenter_blank_group.setEnabled(false);

        logoButton_isSelected = !logoButton_isSelected;

        if (logoButton_isSelected) {
            backgroundButton_isSelected = false;
            // IV - If coming from a blank screen do fade quicker
            long tDelay;
            if (blankButton_isSelected) {
                tDelay = 0;
            } else {
                tDelay = preferences.getMyPreferenceInt(this, "presoTransitionTime",800);
            }
            // Fade in the logo
            if (mSelectedDevice != null) {
                PresentationService.ExternalDisplay.showLogoPrep();
                Handler h = new Handler();
                h.postDelayed(() -> {
                    try {
                        PresentationService.ExternalDisplay.showLogo();
                        PresentationService.ExternalDisplay.wipeProjectedLayout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, tDelay);
                if (blankButton_isSelected) {
                    PresentationService.ExternalDisplay.blankUnblankDisplay(true);
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                PresentationServiceHDMI.showLogoPrep();
                Handler h = new Handler();
                h.postDelayed(() -> {
                    try {
                        PresentationServiceHDMI.showLogo();
                        PresentationServiceHDMI.wipeProjectedLayout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },tDelay);
                if (blankButton_isSelected) {
                    PresentationServiceHDMI.blankUnblankDisplay(true);
                }
            }
            blankButton_isSelected = false;
        } else {
            // Fade out the logo
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.wipeProjectedLayout();
                    PresentationService.ExternalDisplay.hideLogo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.wipeProjectedLayout();
                    PresentationServiceHDMI.hideLogo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // IV - If we end up not 'blank' and not 'logo' and not 'background' then a 'project' is needed
        if (!blankButton_isSelected && !logoButton_isSelected && !backgroundButton_isSelected && !projectButton_isSelected) {
            presenter_project_group.performClick();
        }

        enableActionButtonsHandler.removeCallbacks(enableActionButtonsRunnable);
        enableActionButtonsHandler.postDelayed(enableActionButtonsRunnable, (long) (3.2 * preferences.getMyPreferenceInt(this, "presoTransitionTime",800)));
    }

    private void backgroundButtonClick() {
        if (!buttonInTransition) {
            unhighlightButtonClicked(presenter_project_group);
            unhighlightButtonClicked(presenter_logo_group);
            highlightButtonClickedHalf(presenter_background_group);
            unhighlightButtonClicked(presenter_blank_group);
        }
        presenter_project_group.setEnabled(false);
        presenter_logo_group.setEnabled(false);
        presenter_background_group.setEnabled(false);
        presenter_blank_group.setEnabled(false);

        backgroundButton_isSelected = !backgroundButton_isSelected;

        if (backgroundButton_isSelected) {
            logoButton_isSelected = false;
            // IV - If coming from a blank screen do fade quicker
            long tDelay;
            if (blankButton_isSelected) {
                tDelay = 0;
            } else {
                tDelay = preferences.getMyPreferenceInt(this, "presoTransitionTime",800);
            }
            // Fade to the background
            if (mSelectedDevice != null) {
                PresentationService.ExternalDisplay.showBackgroundPrep();
                Handler h = new Handler();
                h.postDelayed(() -> {
                    try {
                        PresentationService.ExternalDisplay.showLogo();
                        PresentationService.ExternalDisplay.wipeProjectedLayout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, tDelay);
                if (blankButton_isSelected) {
                    PresentationService.ExternalDisplay.blankUnblankDisplay(true);
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                PresentationServiceHDMI.showBackgroundPrep();
                Handler h = new Handler();
                h.postDelayed(() -> {
                    try {
                        PresentationServiceHDMI.showLogo();
                        PresentationServiceHDMI.wipeProjectedLayout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },tDelay);
                if (blankButton_isSelected) {
                    PresentationServiceHDMI.blankUnblankDisplay(true);
                }
            }
            blankButton_isSelected = false;
        } else {
            // Fade out the logo
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.wipeProjectedLayout();
                    PresentationService.ExternalDisplay.hideLogo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.wipeProjectedLayout();
                    PresentationServiceHDMI.hideLogo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // IV - If we end up not 'blank' and not 'logo' and not 'background' then a 'project' is needed
        if (!blankButton_isSelected && !logoButton_isSelected && !backgroundButton_isSelected && !projectButton_isSelected) {
            presenter_project_group.performClick();
        }

        enableActionButtonsHandler.removeCallbacks(enableActionButtonsRunnable);
        enableActionButtonsHandler.postDelayed(enableActionButtonsRunnable, (long) (3.2 * preferences.getMyPreferenceInt(this, "presoTransitionTime",800)));
    }
    private void blankButtonClick() {
        if (!buttonInTransition) {
            unhighlightButtonClicked(presenter_project_group);
            unhighlightButtonClicked(presenter_logo_group);
            unhighlightButtonClicked(presenter_background_group);
            highlightButtonClickedHalf(presenter_blank_group);
        }

        presenter_project_group.setEnabled(false);
        presenter_logo_group.setEnabled(false);
        presenter_background_group.setEnabled(false);
        presenter_blank_group.setEnabled(false);

        blankButton_isSelected = !blankButton_isSelected;

        if (blankButton_isSelected) {
            // Fade out everything after highlighting the button and disabling
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.blankUnblankDisplay(false);
                    PresentationService.ExternalDisplay.wipeProjectedLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.blankUnblankDisplay(false);
                    PresentationServiceHDMI.wipeProjectedLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (logoButton_isSelected) {
                presenter_logo_group.performClick();
             }
            if (backgroundButton_isSelected) {
                presenter_background_group.performClick();
            }
        } else {
             // Fade back everything
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.wipeProjectedLayout();
                    PresentationService.ExternalDisplay.blankUnblankDisplay(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.wipeProjectedLayout();
                    PresentationServiceHDMI.blankUnblankDisplay(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (!blankButton_isSelected && !logoButton_isSelected && !backgroundButton_isSelected && !projectButton_isSelected) {
            presenter_project_group.performClick();
        }
        enableActionButtonsHandler.removeCallbacks(enableActionButtonsRunnable);
        enableActionButtonsHandler.postDelayed(enableActionButtonsRunnable, (long) (3.2 * preferences.getMyPreferenceInt(this, "presoTransitionTime",800)));
    }
    private void alertButtonClick() {
        highlightButtonClicked(presenter_alert_group);
        FullscreenActivity.whattodo = "alert";
        openFragment();

        // After a short time, turn off the button
        Handler delay = new Handler();
        delay.postDelayed(() -> unhighlightButtonClicked(presenter_alert_group), 500);
    }
    private void audioButtonClick() {
        highlightButtonClicked(presenter_audio_group);
        FullscreenActivity.whattodo = "presenter_audio";
        openFragment();

        // After a short time, turn off the button
        Handler delay = new Handler();
        delay.postDelayed(() -> unhighlightButtonClicked(presenter_audio_group), 500);
    }
    private void dBButtonClick() {
        // Check audio record is allowed
        if (ActivityCompat.checkSelfPermission(PresenterMode.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Snackbar.make(mLayout, R.string.microphone_rationale, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, view -> ActivityCompat.requestPermissions(PresenterMode.this, new String[]{Manifest.permission.RECORD_AUDIO}, StaticVariables.REQUEST_MICROPHONE_CODE)).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                        StaticVariables.REQUEST_MICROPHONE_CODE);
            }

        } else {
            highlightButtonClicked(presenter_dB_group);
            FullscreenActivity.whattodo = "presenter_db";
            openFragment();

            // After a short time, turn off the button
            Handler delay = new Handler();
            delay.postDelayed(() -> unhighlightButtonClicked(presenter_dB_group), 500);
        }
    }

    // Highlight or unhighlight the presenter (col 3) buttons
    private void highlightButtonClicked(View v) {
        v.setBackground(ContextCompat.getDrawable(PresenterMode.this, R.drawable.presenter_box_blue_active));
    }
    private void unhighlightButtonClicked(View v) {
        v.setBackground(null);
    }
    private void highlightButtonClickedHalf(View v) {
        buttonInTransition = true;
        v.setBackground(ContextCompat.getDrawable(PresenterMode.this, R.drawable.presenter_box_blue_half_active));
    }

    // Enable or disable the buttons in the final column
    private void noSecondScreen() {
        unhighlightButtonClicked(presenter_project_group);
        unhighlightButtonClicked(presenter_logo_group);
        unhighlightButtonClicked(presenter_background_group);
        unhighlightButtonClicked(presenter_blank_group);
        unhighlightButtonClicked(presenter_alert_group);
        unhighlightButtonClicked(presenter_audio_group);
        unhighlightButtonClicked(presenter_dB_group);
        unhighlightButtonClicked(presenter_slide_group);
        unhighlightButtonClicked(presenter_scripture_group);
        unhighlightButtonClicked(presenter_display_group);
        presenter_project_group.setEnabled(true);
        presenter_logo_group.setEnabled(true);
        presenter_background_group.setEnabled(true);
        presenter_blank_group.setEnabled(true);
        presenter_alert_group.setEnabled(true);
        presenter_audio_group.setEnabled(true);
        presenter_dB_group.setEnabled(true);
        presenter_slide_group.setEnabled(true);
        presenter_scripture_group.setEnabled(true);
        presenter_display_group.setEnabled((true));
        projectButton_isSelected = false;
        logoButton_isSelected = false;
        blankButton_isSelected = false;
    }
    private void isSecondScreen() {
        presenter_project_group.setEnabled(true);
        presenter_logo_group.setEnabled(true);
        presenter_background_group.setEnabled(true);
        presenter_blank_group.setEnabled(true);
        presenter_alert_group.setEnabled(true);
        presenter_audio_group.setEnabled(true);
        presenter_dB_group.setEnabled(true);
        presenter_slide_group.setEnabled(true);
        presenter_scripture_group.setEnabled(true);
        presenter_display_group.setEnabled(true);
        projectButton_isSelected = false;
        logoButton_isSelected = false;
        blankButton_isSelected = false;
    }

    // Google Nearby
    @Override
    public boolean requestNearbyPermissions() {
        Log.d(TAG, "Requesting nearby permissions");
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            try {
                make(findViewById(R.id.mypage), R.string.location_rationale,
                        LENGTH_INDEFINITE).setAction(R.string.ok, view -> ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 404)).show();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},404);
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

    // The camera permissions and stuff
    @Override
    public void useCamera() {
        if (ContextCompat.checkSelfPermission(PresenterMode.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(PresenterMode.this, new String[]{Manifest.permission.CAMERA},
                    StaticVariables.REQUEST_CAMERA_CODE);
        } else {
            startCamera();
        }
    }
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
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class LoadSong extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                StaticVariables.panicRequired = false;
                StaticVariables.infoBarChangeRequired = true;
                // Load up the song
                try {
                    LoadXML.loadXML(PresenterMode.this, preferences, storageAccess, processSong);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!StaticVariables.reloadOfSong) {
                    // Send Nearby song intent
                    if (StaticVariables.isHost && StaticVariables.isConnected && !FullscreenActivity.orientationchanged) {
                        // IV - The send is always called by the 'if' and will return true if a large file has been sent
                        if (nearbyConnections.sendSongPayload()) {
                            StaticVariables.myToastMessage = (getString(R.string.nearby_large_file));
                            Handler h = new Handler();
                            h.post(() -> ShowToast.showToast(PresenterMode.this));
                        }
                    }
                    // IV - Set current values
                    if (StaticVariables.currentSection >= 0) {
                        StaticVariables.currentSection = 0;
                    } else {
                        // IV - Consume any pending client section change received from Host (-ve value)
                        StaticVariables.currentSection = -(1 + StaticVariables.currentSection);
                    }
                    FullscreenActivity.pdfPageCurrent = StaticVariables.currentSection;
                }

                // Clear the old headings (presention order looks for these)
                FullscreenActivity.foundSongSections_heading = new ArrayList<>();

                if (StaticVariables.mLyrics != null) {
                    FullscreenActivity.myLyrics = StaticVariables.mLyrics;
                } else {
                    FullscreenActivity.myLyrics = "";
                }

                // CheckChordFormat returns the detected current format to both StaticVariables detectedChordFormat and newChordFormat
                try {
                    Transpose.checkChordFormat();
                    if (preferences.getMyPreferenceInt(PresenterMode.this,"chordFormat",1) != 0) {
                        // Override output format to preference
                        StaticVariables.newChordFormat = preferences.getMyPreferenceInt(PresenterMode.this, "chordFormat", 1);

                        if (preferences.getMyPreferenceBoolean(PresenterMode.this,"chordFormatUsePreferred",true)) {
                            StaticVariables.detectedChordFormat = StaticVariables.newChordFormat;
                        }
                    } else {
                        StaticVariables.newChordFormat = StaticVariables.detectedChordFormat;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error checking the chord format");
                }

                // Don't process images or image slide details here.  No need.  Only do songs
                if (FullscreenActivity.isSong || FullscreenActivity.isSlide || FullscreenActivity.isScripture) {
                    // IV - PrepareSongSections prepares
                    // FullscreenActivity.myLyrics,
                    // StaticVariables.songSections, StaticVariables.songSectionsLabels, StaticVariables.songSectionsTypes,
                    // StaticVariables.sectionContents, StaticVariables.sectionLineTypes
                    // StaticVariables.projectedContents, StaticVariables.projectedLineTypes
                    processSong.prepareSongSections(PresenterMode.this, preferences, storageAccess);
                 }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "done";
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            FullscreenActivity.alreadyloading = false;
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Now, reset the orientation.
                    // Keep for check
                    boolean orientationChanged = FullscreenActivity.orientationchanged;
                    FullscreenActivity.orientationchanged = false;

                    // Get the current orientation
                    FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

                    setActions.indexSongInSet();
                    if (!StaticVariables.setView) {
                        // Unhighlight the set buttons
                        unhighlightAllSetButtons();
                    }
                    showCorrectViews();

                    // IV - Consume any later pending client section change received from Host (-ve value)
                    if (StaticVariables.currentSection < 0) {
                        StaticVariables.currentSection = -(1 + StaticVariables.currentSection);
                    }

                    if (FullscreenActivity.isPDF) {
                        LoadXML.getPDFPageCount(PresenterMode.this, preferences, storageAccess);
                    }
                    setupSongButtons();
                    findSongInFolders();

                    // Send the midi data if we can
                    if (!orientationChanged) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            sendMidi();
                        }
                    }

                    // If the user has shown the 'Welcome to OpenSongApp' file, and their song lists are empty,
                    // open the find new songs menu
                    if (StaticVariables.mTitle.equals("Welcome to OpenSongApp") &&
                            sqLiteHelper.getSongsCount(PresenterMode.this)<1) {
                        StaticVariables.whichOptionMenu = "FIND";
                        prepareOptionMenu();
                        Handler find = new Handler();
                        find.postDelayed(() -> openMyDrawers("option"), 2000);
                    }
                    // If we have created, or converted a song format (e.g from OnSong or ChordPro), rebuild the database
                    // or pull up the edit screen
                    if (FullscreenActivity.needtoeditsong) {
                        FullscreenActivity.whattodo = "editsong";
                        FullscreenActivity.alreadyloading = false;
                        FullscreenActivity.needtorefreshsongmenu = true;
                    } else if (FullscreenActivity.needtorefreshsongmenu) {
                        FullscreenActivity.needtorefreshsongmenu = false;
                        prepareSongMenu();
                    }

                    // Get the SQLite stuff if the song exists.  Otherwise throws an exception (which is ok)
                    if (!StaticVariables.whichSongFolder.startsWith("..")) {
                        String songId = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;

                        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                            nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(PresenterMode.this,storageAccess,preferences,songId);
                        } else {
                            // If this song isn't indexed, set its details
                            sqLite = sqLiteHelper.getSong(PresenterMode.this, songId);
                            if (sqLite.getLyrics()==null || sqLite.getLyrics().equals("")) {
                                sqLite = sqLiteHelper.setSong(sqLite);
                                sqLiteHelper.updateSong(PresenterMode.this,sqLite);
                            }
                        }
                    } else {
                        // Not a song in the database (likley a variation, slide, etc.)
                        sqLite.setSongid("");
                        sqLite.setId(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            FullscreenActivity.alreadyloading = false;
        }
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class PrepareSongMenu extends AsyncTask<Object, Void, String> {

        ArrayList<SQLite> songsInFolder;

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
                // Get a list of the songs in the current folder
                // TODO
                songsInFolder = sqLiteHelper.getSongsInFolder(PresenterMode.this, StaticVariables.whichSongFolder);

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

                    // Go through the found songs in folder and prepare the menu
                    ArrayList<SongMenuViewItems> songmenulist = new ArrayList<>();

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
                        whattolookfor = setActions.whatToLookFor(PresenterMode.this, StaticVariables.whichSongFolder, foundsongfilename);

                        // Fix for variations, etc
                        whattolookfor = setActions.fixIsInSetSearch(whattolookfor);

                        boolean isinset = preferences.getMyPreferenceString(PresenterMode.this,"setCurrent","").contains(whattolookfor);

                        SongMenuViewItems song = new SongMenuViewItems(foundsongfilename,
                                foundsongfilename, foundsongauthor, foundsongkey, isinset);
                        songmenulist.add(song);
                    }

                    SongMenuAdapter lva = new SongMenuAdapter(PresenterMode.this, preferences, songmenulist);
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

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class DoMoveInSet extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Get the appropriate song
            try {
                FullscreenActivity.linkclicked = StaticVariables.mSetList[StaticVariables.indexSongInSet];
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
                    // Get the next set positions and song
                    FullscreenActivity.linkclicked = StaticVariables.mSetList[StaticVariables.indexSongInSet];
                    StaticVariables.whatsongforsetwork = FullscreenActivity.linkclicked;
                    StaticVariables.setMoveDirection = ""; // Expects back or forward for Stage/Performance, but not here
                    setActions.doMoveInSet(PresenterMode.this, preferences);

                    // Set indexSongInSet position has moved
                    //invalidateOptionsMenu();

                    // Click the item in the set list
                    if (presenter_set_buttonsListView.getChildAt(StaticVariables.indexSongInSet) != null) {
                        presenter_set_buttonsListView.getChildAt(StaticVariables.indexSongInSet).performClick();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        // IV - Need to refresh all when in this mode
        refreshAll();
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo info, int reason) {
            super.onRouteSelected(router,info,reason);
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            isSecondScreen();
            logoButton_isSelected = true;
            highlightButtonClicked(presenter_logo_group);
            updateDisplays();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info, int reason) {
            super.onRouteUnselected(router,info,reason);
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
                if (FullscreenActivity.hdmi!=null) {
                    FullscreenActivity.hdmi.dismiss();
                    FullscreenActivity.hdmi = null;
                }
            } catch (Exception e) {
                // Ooops
                e.printStackTrace();
            }
            logoButton_isSelected = false;
            noSecondScreen();
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
    private void updateDisplays() {
        // This is called when display devices are changed (connected, disconnected, etc.)
        StaticVariables.activity = PresenterMode.this;

        Intent intent = new Intent(PresenterMode.this,
                PresenterMode.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                PresenterMode.this, 0, intent, 0);

        CastRemoteDisplayLocalService.NotificationSettings settings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(notificationPendingIntent).build();

        if (mSelectedDevice!=null) {
            CastRemoteDisplayLocalService.startService(
                    getApplicationContext(),
                    PresentationService.class, getString(R.string.app_id),
                    mSelectedDevice, settings,
                    new CastRemoteDisplayLocalService.Callbacks() {
                        @Override
                        public void onServiceCreated(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {
                            Log.d(TAG, "onServiceCreated()");
                            Log.d(TAG, "castRemoteDisplayLocalService=" + castRemoteDisplayLocalService);
                        }

                        @Override
                        public void onRemoteDisplaySessionStarted(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {
                            Log.d(TAG, "onRemoteDisplaySessionStarted()");
                            Log.d(TAG, "castRemoteDisplayLocalService=" + castRemoteDisplayLocalService);
                        }

                        @Override
                        public void onRemoteDisplaySessionError(Status status) {
                            Log.d(TAG, "onRemoteDisplaySessionError()");
                            Log.d(TAG, "status=" + status);
                        }

                        @Override
                        public void onRemoteDisplaySessionEnded(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {
                            Log.d(TAG, "onRemoteDisplaySessionEnded()");
                            Log.d(TAG, "castRemoteDisplayLocalService=" + castRemoteDisplayLocalService);
                        }

                        // IV - After a rebase against upstream - cast update makes this not needed and adds onRemoteDisplayMuteStateChanged
                        //@Override
                        //public void zza() {
                        //    Log.d(TAG,"zza()");
                        //}

                        @Override
                        public void onRemoteDisplayMuteStateChanged(boolean b) {
                            Log.d(TAG, "onRemoteDisplayMuteStateChanged()");
                            Log.d(TAG, "b=" + b);
                        }
                    });
        } else {
            // Might be a hdmi connection
            /*try {
                Display mDisplay = mMediaRouter.getSelectedRoute().getPresentationDisplay();
                if (mDisplay != null) {
                    hdmi = new PresentationServiceHDMI(PresenterMode.this, mDisplay, processSong);
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
                    Log.d(TAG,"dm=" + dm);

                    // If a Chromebook HDMI, need to do this
                    Display[] displays = dm.getDisplays();
                    for (Display mDisplay : displays) {
                        if (mDisplay.getDisplayId() > 0) {
                            if (FullscreenActivity.hdmi == null) FullscreenActivity.hdmi = new PresentationServiceHDMI(PresenterMode.this, mDisplay, processSong);
                            FullscreenActivity.hdmi.show();
                            FullscreenActivity.isHDMIConnected = true;
                        }
                    }

                    if (!FullscreenActivity.isHDMIConnected) {
                        // For non-Chromebooks
                        displays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
                        for (Display mDisplay : displays) {
                            if (FullscreenActivity.hdmi == null) FullscreenActivity.hdmi = new PresentationServiceHDMI(PresenterMode.this, mDisplay, processSong);
                            FullscreenActivity.hdmi.show();
                            FullscreenActivity.isHDMIConnected = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"Error" + e);
            }
        }
    }
    @Override
    public void refreshSecondaryDisplay(String which) {
        try {
            switch (which) {
                case "logo":
                    if (FullscreenActivity.isPresenting && !FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationService.ExternalDisplay.setUpLogo();
                            // IV - Do a full showLogo if needed
                            if (PresenterMode.logoButton_isSelected) {
                                PresentationService.ExternalDisplay.showLogoPrep();
                                PresentationService.ExternalDisplay.showLogo();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (FullscreenActivity.isHDMIConnected) {
                        try {
                            PresentationServiceHDMI.setUpLogo();
                            if (PresenterMode.logoButton_isSelected) {
                                PresentationServiceHDMI.showLogoPrep();
                                PresentationServiceHDMI.showLogo();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

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
    public void updateAlert(boolean ison) {
        boolean displayAllowed = !(logoButton_isSelected || blankButton_isSelected);
        if (FullscreenActivity.isPresenting && !FullscreenActivity.isHDMIConnected) {
            try {
                PresentationService.ExternalDisplay.updateAlert(ison, displayAllowed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (FullscreenActivity.isHDMIConnected) {
            try {
                PresentationServiceHDMI.updateAlert(ison, displayAllowed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Listeners for key or pedal presses
    @Override
    public void onBackPressed() {
        if (mp.isPlaying()) {
            // Stop the media player
            mp.stop();
            mp.reset();
            mpTitle = "";
        }

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
        ft.add(newFragment,message);

        if (newFragment != null && !PresenterMode.this.isFinishing()) {
            try {
                ft.commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //newFragment = PopUpAreYouSureFragment.newInstance(message);
        //newFragment.show(getSupportFragmentManager(), "dialog");
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        // To stop repeated pressing too quickly, set a handler to wait for 500ms before reenabling
        if (event.getAction() == KeyEvent.ACTION_UP && pedalsenabled) {
            if (keyCode == preferences.getMyPreferenceInt(PresenterMode.this,"pedal1Code",21)) {
                doPedalAction(preferences.getMyPreferenceString(PresenterMode.this,"pedal1ShortPressAction","prev"));
            } else if (keyCode == preferences.getMyPreferenceInt(PresenterMode.this,"pedal2Code",22)) {
                doPedalAction(preferences.getMyPreferenceString(PresenterMode.this,"pedal2ShortPressAction","next"));
            } else if (keyCode == preferences.getMyPreferenceInt(PresenterMode.this,"pedal3Code",19)) {
                doPedalAction(preferences.getMyPreferenceString(PresenterMode.this,"pedal3ShortPressAction","prev"));
            } else if (keyCode == preferences.getMyPreferenceInt(PresenterMode.this,"pedal4Code",20)) {
                doPedalAction(preferences.getMyPreferenceString(PresenterMode.this,"pedal4ShortPressAction","next"));
            } else if (keyCode == preferences.getMyPreferenceInt(PresenterMode.this,"pedal5Code",92)) {
                doPedalAction(preferences.getMyPreferenceString(PresenterMode.this,"pedal15hortPressAction","prev"));
            } else if (keyCode == preferences.getMyPreferenceInt(PresenterMode.this,"pedal6Code",93)) {
                doPedalAction(preferences.getMyPreferenceString(PresenterMode.this,"pedal6ShortPressAction","next"));
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void doPedalAction(String action) {
        switch (action) {
            case "prev":
            case "down":
                pausePedalUse();
                tryClickPreviousSection();
                break;

            case "next":
            case "up":
                pausePedalUse();
                tryClickNextSection();
                break;
        }
    }
    private void pausePedalUse() {
        pedalsenabled = false;
        // Close both drawers
        closeMyDrawers("both");

        Handler reenablepedal = new Handler();
        reenablepedal.postDelayed(() -> pedalsenabled = true, 500);
    }
    @Override
    public void gesture5() {
        // Stop or start autoscroll - Does nothing in presentation mode
    }
    @Override
    public void gesture6() {
        // Stop or start pad - Does nothing in presentation mode
    }
    @Override
    public void gesture7() {
        // Start or stop the metronome - Does nothing in presentation mode
    }


    // Page buttons not officially used in PresenterMode, although some features are
    @Override
    public void setupPageButtons() {
        // Not using page buttons as FABs on the screen, so do nothing
    }@Override
    public void setupPageButtonsColors() {
        // Not using page buttons as FABs on the screen, so do nothing
    }
    @Override
    public void pageButtonAlpha(String s) {
        // Do nothing as this override is for StageMode
    }
    @Override
    public void setupQuickLaunchButtons() {
        // Do nothing as this override is for StageMode
    }
    // IV - Various void removed / added to match with changes
    @Override
    public void onScrollAction() {}

    @Override
    public void prepareLearnAutoScroll() {}

    @Override
    public void stopLearnAutoScroll() {}

    @Override
    public void updateExtraInfoColorsAndSizes(String what) {}

    @Override
    public void takeScreenShot() {
        // Do nothing - this is only for Performance mode
    }
    @Override
    public void displayHighlight(boolean fromautoshow) {
        // Do nothing - this is only for Performance mode
    }

    @Override
    public void selectAFileUri(String s) {
        // Replace the FolderPicker class for Lollopop+
        Intent intent;
        // Start location
        Uri uri = storageAccess.getUriForItem(PresenterMode.this,preferences,"","","");
        Log.d(TAG,"Start uri=" + uri);
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

    // IV - Make option menu return to MAIN after use - same as for other modes
    private void setupOptionDrawerReset() {
        // Called when a drawer has settled in a completely open state.
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(PresenterMode.this, mDrawerLayout, ab_toolbar, R.string.drawer_open, R.string.drawer_close) {
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
                // Song index use will set this false.  Set to true again on drawer close.
                song_list_view.setFastScrollEnabled(true);
            }
        };
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
    }
}