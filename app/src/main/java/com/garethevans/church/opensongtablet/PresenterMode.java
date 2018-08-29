package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)

public class PresenterMode extends AppCompatActivity implements MenuHandlers.MyInterface,
        SongMenuListeners.MyInterface, PopUpChooseFolderFragment.MyInterface,
        PopUpSongDetailsFragment.MyInterface, PopUpEditSongFragment.MyInterface,
        SetActions.MyInterface, PopUpPresentationOrderFragment.MyInterface,
        PopUpSetViewNew.MyInterface, IndexSongs.MyInterface, SearchView.OnQueryTextListener,
        OptionMenuListeners.MyInterface, PopUpFullSearchFragment.MyInterface,
        PopUpListSetsFragment.MyInterface, PopUpDirectoryChooserFragment.MyInterface,
        PopUpLongSongPressFragment.MyInterface, PopUpProfileFragment.MyInterface,
        PopUpStorageFragment.MyInterface, PopUpFileChooseFragment.MyInterface,
        PopUpSongFolderRenameFragment.MyInterface, PopUpSongCreateFragment.MyInterface,
        PopUpSongRenameFragment.MyInterface, PopUpImportExportOSBFragment.MyInterface,
        PopUpImportExternalFile.MyInterface, PopUpCustomSlideFragment.MyInterface,
        OnSongConvert.MyInterface, PopUpFindNewSongsFragment.MyInterface,
        PopUpThemeChooserFragment.MyInterface, PopUpGroupedPageButtonsFragment.MyInterface,
        PopUpQuickLaunchSetup.MyInterface, PopUpPagesFragment.MyInterface,
        PopUpExtraInfoFragment.MyInterface, PopUpPageButtonsFragment.MyInterface,
        PopUpScalingFragment.MyInterface, PopUpFontsFragment.MyInterface,
        PopUpChordsFragment.MyInterface, PopUpCustomChordsFragment.MyInterface,
        PopUpTransposeFragment.MyInterface, PopUpEditStickyFragment.MyInterface,
        PopUpPadFragment.MyInterface, PopUpAutoscrollFragment.MyInterface,
        PopUpMetronomeFragment.MyInterface, PopUpStickyFragment.MyInterface,
        PopUpLinks.MyInterface, PopUpAreYouSureFragment.MyInterface,
        SongMenuAdapter.MyInterface, BatteryMonitor.MyInterface, SalutDataCallback,
        PopUpMenuSettingsFragment.MyInterface, PopUpAlertFragment.MyInterface,
        PopUpLayoutFragment.MyInterface, DownloadTask.MyInterface,
        PopUpExportFragment.MyInterface, PopUpActionBarInfoFragment.MyInterface,
        PopUpCreateDrawingFragment.MyInterface, PopUpABCNotationFragment.MyInterface,
        PopUpPDFToTextFragment.MyInterface, PopUpRandomSongFragment.MyInterface,
        PopUpFindStorageLocationFragment.MyInterface, PopUpCCLIFragment.MyInterface,
        PopUpBibleXMLFragment.MyInterface, PopUpShowMidiMessageFragment.MyInterface {

    DialogFragment newFragment;

    // MIDI
    Midi midi;

    // Casting
    MediaRouter mMediaRouter;
    MediaRouteSelector mMediaRouteSelector;
    MyMediaRouterCallback mMediaRouterCallback = new MyMediaRouterCallback();
    CastDevice mSelectedDevice;
    PresentationServiceHDMI hdmi;

    // The toolbar and menu
    public Toolbar ab_toolbar;
    public static ActionBar ab;
    public RelativeLayout songandauthor;
    public TextView digitalclock, songtitle_ab, songkey_ab, songcapo_ab, songauthor_ab, batterycharge;
    public ImageView batteryimage;
    RelativeLayout batteryholder;
    Menu menu;

    // AsyncTasks
    AsyncTask<Object, Void, String> preparesongmenu_async, prepareoptionmenu_async, autoslideshowtask,
            sharesong_async, shareset_async, load_customreusable, add_slidetoset, indexing_done,
            open_drawers, close_drawers, resize_drawers, do_moveinset, shareactivitylog_async;
    AsyncTask<String, Integer, String> do_download;
    LoadSong loadsong_async;
    IndexSongs.IndexMySongs indexsongs_task;

    // The views
    LinearLayout mLayout, pres_details, presenter_song_buttonsListView, preso_Action_buttons,
            presenter_set_buttonsListView, loopandtimeLinearLayout;
    TextView presenter_songtitle, presenter_author, presenter_copyright, presenter_set;
    CheckBox presenter_order_text, loopCheckBox;
    Button presenter_order_button;
    FloatingActionButton set_view_fab, startstopSlideShow;
    EditText presenter_lyrics, timeEditText;
    ImageView presenter_lyrics_image;
    ScrollView presenter_songbuttons, preso_action_buttons_scroll, presenter_setbuttons;
    RelativeLayout col1_layout, col2_layout, col3_layout;

    // Quick nav buttons
    FloatingActionButton nav_prevsong, nav_nextsong, nav_prevsection, nav_nextsection;
    boolean autoproject = false, pedalsenabled = true;

    // Button for the song and set
    Button newSetButton;

    // The buttons
    TextView presenter_project_group, presenter_logo_group, presenter_blank_group,
            presenter_alert_group, presenter_audio_group, presenter_dB_group, presenter_slide_group,
            presenter_scripture_group, presenter_display_group;

    // The song and option menu stuff
    DrawerLayout mDrawerLayout;
    LinearLayout songmenu, optionmenu, side_index, changefolder_LinearLayout;
    TextView menuFolder_TextView, menuCount_TextView;
    FloatingActionButton closeSongsFAB;
    ListView song_list_view;
    ScrollView optionsdisplayscrollview;
    boolean firstrun_option = true, firstrun_song = true, newsongloaded = false;
    SwitchCompat autoProject;

    // The media player
    public static MediaPlayer mp;
    public static String mpTitle = "";

    // Song and set button variables
    LinearLayout newSongSectionGroup;
    Button newSongButton;
    TextView newSongSectionText;
    int numsectionbuttons;

    // Variables used by the popups
    static String whatBackgroundLoaded;

    // General variables
    String[] imagelocs;

    // Which Actions buttons are selected
    boolean projectButton_isSelected = false, blankButton_isSelected = false,
            scriptureButton_isSelected = false, slideButton_isSelected = false,
            alertButton_isSelected = false, displayButton_isSelected = false,
            audioButton_isSelected = false, dBButton_isSelected = false;

    static boolean logoButton_isSelected = false;
    static String alert_on = "N";

    // Auto slideshow
    boolean isplayingautoslideshow = false, autoslideloop = false;
    int autoslidetime = 0;

    // Network discovery / connections
    public static final String TAG = "StageMode";
    SalutMessage myMessage, mySongMessage, mySectionMessage;

    // Battery
    BroadcastReceiver br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("d", "Welcome to Presentation Mode");

        FullscreenActivity.mContext = PresenterMode.this;

        mp = new MediaPlayer();

        // Load up the user preferences
        Preferences.loadPreferences();

        PopUpStorageFragment.setUpStoragePreferences();
        Preferences.savePreferences();

        // Load the layout and set the title
        setContentView(R.layout.presenter_mode);

        // Set the fullscreen window flags
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                setWindowFlags();
                setWindowFlagsAdvanced();

                // Try language locale change
                SetLocale.setLocale(PresenterMode.this);
            }
        });

        // Setup the CastContext
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast("4E2B0891"))
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .build();

        // Since this mode has just been opened, force an update to the cast screen
        FullscreenActivity.forcecastupdate = true;

        // Set up the toolbar and views
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

                // Battery monitor
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                br = new BatteryMonitor();
                PresenterMode.this.registerReceiver(br, filter);

                // Make the drawers match half the width of the screen
                resizeDrawers();

                // Set up the page buttons
                setupSetButtons();

                // Set up the menus
                prepareSongMenu();
                prepareOptionMenu();

                // Set up the song buttons
                setupSongButtons();

                // Redraw the menu
                //invalidateOptionsMenu();

                // If we have started for the first time (not redrawn)
                if (FullscreenActivity.firstload) {
                    FullscreenActivity.firstload = false;
                    rebuildSearchIndex();
                }

                // Set up the Salut service
                getBluetoothName();
                startRegistration();

                // Initialise the ab info
                adjustABInfo();

                // Click on the first item in the set
                if (presenter_set_buttonsListView.getChildCount() > 0) {
                    presenter_set_buttonsListView.getChildAt(0).performClick();

                } else {
                    // Load the song
                    loadSong();
                }

                // Deal with any intents from external files/intents
                dealWithIntent();
            }
        });

    }

    // Handlers for main page on/off/etc. and window flags
    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                    MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        }
        // Fix the page flags
        windowFlags();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                mMediaRouter.removeCallback(mMediaRouterCallback);
            } catch (Exception e) {
                Log.d("d","Problem removing mediaroutercallback");
            }
        }
        if (br!=null) {
            try {
                PresenterMode.this.unregisterReceiver(br);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
    @Override
    protected void onResume() {
        resizeDrawers();
        // Fix the page flags
        windowFlags();
        // Be sure to call the super class.
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Battery monitor
        if (br!=null) {
            try {
                PresenterMode.this.unregisterReceiver(br);
            } catch (Exception e) {
                Log.d("d","Error closing battery monitor");
            }
        }
        tryCancelAsyncTasks();

        if (FullscreenActivity.network!=null && FullscreenActivity.network.isRunningAsHost) {
            try {
                FullscreenActivity.network.stopNetworkService(false);
            } catch (Exception e) {
                Log.d("d","Error closing network service");
            }
        } else if (FullscreenActivity.network!=null) {
            try {
                FullscreenActivity.network.unregisterClient(false);
            } catch (Exception e) {
                Log.d("d","Error closing network service");
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FullscreenActivity.orientationchanged = FullscreenActivity.mScreenOrientation != newConfig.orientation;
        if (FullscreenActivity.orientationchanged) {
            if (newFragment != null && newFragment.getDialog() != null) {
                PopUpSizeAndAlpha.decoratePopUp(PresenterMode.this, newFragment.getDialog());
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
            // Capable of dual head presentations
            FullscreenActivity.dualDisplayCapable = FullscreenActivity.currentapiVersion >= 17;
            windowFlags();
        }
    }
    public void windowFlags() {
        setWindowFlags();
        setWindowFlagsAdvanced();
    }
    public void setWindowFlags() {
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
    public void setWindowFlagsAdvanced() {
        View v = getWindow().getDecorView();
        v.setOnSystemUiVisibilityChangeListener(null);
        v.setOnFocusChangeListener(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
    public void tryCancelAsyncTasks() {
        doCancelAsyncTask(loadsong_async);
        doCancelAsyncTask(loadsong_async);
        doCancelAsyncTask(preparesongmenu_async);
        doCancelAsyncTask(prepareoptionmenu_async);
        doCancelAsyncTask(sharesong_async);
        doCancelAsyncTask(shareset_async);
        doCancelAsyncTask(shareactivitylog_async);
        doCancelAsyncTask(load_customreusable);
        doCancelAsyncTask(open_drawers);
        doCancelAsyncTask(close_drawers);
        doCancelAsyncTask(resize_drawers);
        doCancelAsyncTask(do_moveinset);
        doCancelAsyncTask(indexing_done);
        doCancelAsyncTask(add_slidetoset);
        doCancelAsyncTask(indexsongs_task);
        doCancelAsyncTask(autoslideshowtask);
        doCancelAsyncTask(do_download);
    }
    public void doCancelAsyncTask(AsyncTask ast) {
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
    public void dealWithIntent() {
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

    @Override
    public void adjustABInfo() {
        // Change the visibilities
        if (FullscreenActivity.batteryDialOn) {
            batteryimage.setVisibility(View.VISIBLE);
        } else {
            batteryimage.setVisibility(View.INVISIBLE);
        }
        if (FullscreenActivity.batteryOn) {
            batterycharge.setVisibility(View.VISIBLE);
        } else {
            batterycharge.setVisibility(View.GONE);
        }
        if (FullscreenActivity.timeOn) {
            digitalclock.setVisibility(View.VISIBLE);
        } else {
            digitalclock.setVisibility(View.GONE);
        }

        // Set the text sizes
        batterycharge.setTextSize(FullscreenActivity.batterySize);
        digitalclock.setTextSize(FullscreenActivity.timeSize);
        songtitle_ab.setTextSize(FullscreenActivity.ab_titleSize);
        songauthor_ab.setTextSize(FullscreenActivity.ab_authorSize);
        songkey_ab.setTextSize(FullscreenActivity.ab_titleSize);

        // Set the time format
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df;
        if (FullscreenActivity.timeFormat24h) {
            df = new SimpleDateFormat("HH:mm", FullscreenActivity.locale);
        } else {
            df = new SimpleDateFormat("h:mm", FullscreenActivity.locale);
        }
        String formattedTime = df.format(c.getTime());
        digitalclock.setText(formattedTime);
    }

    @Override
    // The navigation drawers
    public void prepareSongMenu() {
        doCancelAsyncTask(preparesongmenu_async);
        song_list_view.setFastScrollEnabled(false);
        song_list_view.setScrollingCacheEnabled(false);
        preparesongmenu_async = new PrepareSongMenu();
        try {
            preparesongmenu_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class PrepareSongMenu extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            try {
                closeSongsFAB = findViewById(R.id.closeSongsFAB);
                closeSongsFAB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeMyDrawers("song");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            menuCount_TextView.setText("");
            menuCount_TextView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                // List all of the songs in the current folder
                ListSongFiles.getAllSongFolders();
                ListSongFiles.getAllSongFiles();
                ListSongFiles.getSongDetails(PresenterMode.this);
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
                    menuFolder_TextView.setText(FullscreenActivity.whichSongFolder);

                    // Get the song indexes
                    ListSongFiles.getCurrentSongIndex();

                    ArrayList<SongMenuViewItems> songmenulist = new ArrayList<>();
                    for (int i = 0; i < FullscreenActivity.songDetails.length; i++) {
                        if (FullscreenActivity.songDetails[i][0] == null) {
                            FullscreenActivity.songDetails[i][0] = "Can't find title";
                        }
                        if (FullscreenActivity.songDetails[i][1] == null) {
                            FullscreenActivity.songDetails[i][1] = "Can't find author";
                        }
                        if (FullscreenActivity.songDetails[i][2] == null) {
                            FullscreenActivity.songDetails[i][2] = "Can't find key";
                        }
                        // Detect if the song is in the set
                        String whattolookfor;
                        if (FullscreenActivity.whichSongFolder.equals("") || FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                            whattolookfor = "$**_" + FullscreenActivity.mSongFileNames[i] + "_**$";
                        } else {
                            whattolookfor = "$**_" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.mSongFileNames[i] +"_**$";
                        }
                        boolean isinset = false;
                        if (FullscreenActivity.mySet.contains(whattolookfor)) {
                            isinset = true;
                        }
                        try {
                            SongMenuViewItems song = new SongMenuViewItems(FullscreenActivity.mSongFileNames[i],
                                    FullscreenActivity.songDetails[i][0], FullscreenActivity.songDetails[i][1], FullscreenActivity.songDetails[i][2], isinset);
                            songmenulist.add(song);
                        } catch (Exception e) {
                            // Probably moving too quickly
                        }
                    }

                    SongMenuAdapter lva = new SongMenuAdapter(PresenterMode.this, songmenulist);
                    song_list_view.setAdapter(lva);
                    song_list_view.setFastScrollEnabled(true);
                    song_list_view.setScrollingCacheEnabled(true);
                    lva.notifyDataSetChanged();

                    // Set the secondary alphabetical side bar
                    SongMenuAdapter.getIndexList(PresenterMode.this);
                    displayIndex();

                    // Listen for long clicks in the song menu (songs only, not folders) - ADD TO SET!!!!
                    //song_list_view.setOnItemLongClickListener(SongMenuListeners.myLongClickListener(PresenterMode.this));

                    // Listen for short clicks in the song menu (songs only, not folders) - OPEN SONG!!!!
                    //song_list_view.setOnItemClickListener(SongMenuListeners.myShortClickListener(PresenterMode.this));

                    // Flick the song drawer open once it is ready
                    findSongInFolders();
                    if (firstrun_song) {
                        openMyDrawers("song");
                        closeMyDrawers("song_delayed");
                        firstrun_song = false;
                    }

                    if (menuCount_TextView != null) {
                        String str = ""+songmenulist.size();
                        menuCount_TextView.setText(str);
                        menuCount_TextView.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void findSongInFolders() {
        //scroll to the song in the song menu
        try {
            song_list_view.setSelection(FullscreenActivity.currentSongIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void songShortClick(int mychild) {
        // Scroll to this song in the song menu
        song_list_view.smoothScrollToPosition(mychild);
        fixSet();
        // Close both drawers
        closeMyDrawers("both");
        // Load the song
        loadSong();
    }
    @Override
    public void songLongClick() {
        // Rebuild the set list as we've just added a song
        SetActions.prepareSetList();
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
    @SuppressLint("StaticFieldLeak")
    private class PrepareOptionMenu extends AsyncTask<Object, Void, String> {

        public void onPreExecute() {
            try {
                optionmenu = findViewById(R.id.optionmenu);
                optionmenu.removeAllViews();
                optionmenu.addView(OptionMenuListeners.prepareOptionMenu(PresenterMode.this));
                if (optionmenu != null) {
                    OptionMenuListeners.optionListeners(optionmenu, PresenterMode.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Get the current set list
            try {
                SetActions.prepareSetList();
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
            OptionMenuListeners.updateMenuVersionNumber(PresenterMode.this, (TextView) findViewById(R.id.menu_version_bottom));
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
    private class ResizeDrawers extends AsyncTask<Object, Void, String> {
        int width;

        @Override
        protected String doInBackground(Object... o) {
            try {
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                width = (int) ((float) metrics.widthPixels * FullscreenActivity.menuSize);
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

        String which;

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

        String which;

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

    // The overflow menu and actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.presenter_actions, menu);

        // Setup the menu item for connecting to cast devices
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
        MenuHandlers.forceOverFlow(PresenterMode.this, ab, menu);

        // Set up battery monitor
        setUpBatteryMonitor();

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuHandlers.actOnClicks(PresenterMode.this, item.getItemId());
        return super.onOptionsItemSelected(item);
    }
    public void setUpBatteryMonitor() {
        // Get clock
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df;
            if (FullscreenActivity.timeFormat24h) {
                df = new SimpleDateFormat("HH:mm", FullscreenActivity.locale);
            } else {
                df = new SimpleDateFormat("h:mm", FullscreenActivity.locale);
            }
            String formattedTime = df.format(c.getTime());
            if (FullscreenActivity.timeOn) {
                digitalclock.setVisibility(View.VISIBLE);
            } else {
                digitalclock.setVisibility(View.GONE);
            }
            digitalclock.setTextSize(FullscreenActivity.timeSize);
            digitalclock.setText(formattedTime);

            // Get battery
            int i = (int) (BatteryMonitor.getBatteryStatus(PresenterMode.this) * 100.0f);
            String charge = i + "%";
            if (FullscreenActivity.batteryOn) {
                batterycharge.setVisibility(View.VISIBLE);
            } else {
                batterycharge.setVisibility(View.GONE);
            }
            batterycharge.setTextSize(FullscreenActivity.batterySize);
            batterycharge.setText(charge);
            int abh = ab.getHeight();
            FullscreenActivity.ab_height = abh;
            if (FullscreenActivity.batteryDialOn) {
                batteryimage.setVisibility(View.VISIBLE);
            } else {
                batteryimage.setVisibility(View.INVISIBLE);
            }
            if (ab != null && abh > 0) {
                BitmapDrawable bmp = BatteryMonitor.batteryImage(i, abh, PresenterMode.this);
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

    // Prepare the stuff we need
    public void initialiseTheViews() {

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
        presenter_set = findViewById(R.id.presenter_set);
        presenter_set_buttonsListView = findViewById(R.id.presenter_set_buttonsListView);
        presenter_lyrics = findViewById(R.id.presenter_lyrics);
        presenter_lyrics_image = findViewById(R.id.presenter_lyrics_image);
        loopandtimeLinearLayout = findViewById(R.id.loopandtimeLinearLayout);
        loopCheckBox = findViewById(R.id.loopCheckBox);
        timeEditText = findViewById(R.id.timeEditText);
        startstopSlideShow = findViewById(R.id.startstopSlideShow);
        presenter_songbuttons = findViewById(R.id.presenter_songbuttons);
        preso_Action_buttons = findViewById(R.id.preso_Action_buttons);
        preso_action_buttons_scroll = findViewById(R.id.preso_action_buttons_scroll);
        presenter_setbuttons = findViewById(R.id.presenter_setbuttons);
        presenter_song_buttonsListView = findViewById(R.id.presenter_song_buttonsListView);
        autoProject = findViewById(R.id.autoProject);

        // The page columns
        col1_layout = findViewById(R.id.col1_layout);
        col2_layout = findViewById(R.id.col2_layout);
        col3_layout = findViewById(R.id.col3_layout);

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

        // The song menu
        songmenu = findViewById(R.id.songmenu);
        menuFolder_TextView = findViewById(R.id.menuFolder_TextView);
        menuCount_TextView = findViewById(R.id.menuCount_TextView);
        closeSongsFAB = findViewById(R.id.closeSongsFAB);
        side_index = findViewById(R.id.side_index);
        song_list_view = findViewById(R.id.song_list_view);

        // The option menu
        optionmenu = findViewById(R.id.optionmenu);
        optionsdisplayscrollview = findViewById(R.id.optionsdisplayscrollview);
        menuFolder_TextView = findViewById(R.id.menuFolder_TextView);
        menuFolder_TextView.setText(getString(R.string.wait));
        changefolder_LinearLayout = findViewById(R.id.changefolder_LinearLayout);
        changefolder_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "choosefolder";
                openFragment();
            }
        });

        // Make views focusable
        presenter_songtitle.isFocusable();
        presenter_songtitle.requestFocus();

        autoProject.setChecked(FullscreenActivity.autoProject);

        // Set the button listeners
        presenter_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit current set
                //mDrawerLayout.closeDrawer(expListViewOption);
                FullscreenActivity.whattodo = "editset";
                openFragment();
            }
        });
        set_view_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit current set
                //mDrawerLayout.closeDrawer(expListViewOption);
                CustomAnimations.animateFAB(set_view_fab,PresenterMode.this);
                FullscreenActivity.whattodo = "editset";
                openFragment();
            }
        });
        startstopSlideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomAnimations.animateFAB(startstopSlideShow, PresenterMode.this);
                if (isplayingautoslideshow) {
                    prepareStopAutoSlideShow();
                } else {
                    prepareStartAutoSlideShow();
                }
            }
        });
        autoProject.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.autoProject = b;
                Preferences.savePreferences();
            }
        });

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
    public void screenClickListeners() {
        songandauthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "songdetails";
                openFragment();
            }
        });
        batteryholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "actionbarinfo";
                openFragment();
            }
        });
        pres_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "songdetails";
                openFragment();
            }
        });
        presenter_order_text.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FullscreenActivity.usePresentationOrder = isChecked;
                Preferences.savePreferences();
                refreshAll();
            }
        });
        presenter_order_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FullscreenActivity.isPDF) {
                    FullscreenActivity.whattodo = "extractPDF";
                    openFragment();
                } else if (FullscreenActivity.isSong) {
                    FullscreenActivity.whattodo = "editsong";
                    openFragment();
                }
            }
        });
        nav_prevsong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(nav_prevsong, PresenterMode.this);
                tryClickPreviousSongInSet();
            }
        });
        nav_nextsong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(nav_nextsong, PresenterMode.this);
                tryClickNextSongInSet();
            }
        });
        nav_prevsection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(nav_prevsection, PresenterMode.this);
                tryClickPreviousSection();
            }
        });
        nav_nextsection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(nav_nextsection, PresenterMode.this);
                tryClickNextSection();
            }
        });
        presenter_project_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                projectButtonClick();
            }
        });
        presenter_logo_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoButtonClick();
            }
        });
        presenter_blank_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blankButtonClick();
            }
        });
        presenter_alert_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertButtonClick();
            }
        });
        presenter_audio_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioButtonClick();
            }
        });
        presenter_dB_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dBButtonClick();
            }
        });
        presenter_slide_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "customreusable_slide";
                openFragment();
            }
        });
        presenter_scripture_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "customreusable_scripture";
                openFragment();
            }
        });
        presenter_display_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "connecteddisplay";
                openFragment();
            }
        });
    }
    public void showCorrectViews() {
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
    public void setupSetButtons() {
        // Create a new button for each song in the Set
        SetActions.prepareSetList();
        presenter_set_buttonsListView.removeAllViews();
        try {
            if (FullscreenActivity.mSetList != null && FullscreenActivity.mSetList.length > 0) {
                for (int x = 0; x < FullscreenActivity.mSet.length; x++) {
                    newSetButton = ProcessSong.makePresenterSetButton(x, PresenterMode.this);
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
            FullscreenActivity.setView = true;
            FullscreenActivity.indexSongInSet = which;

            // Scroll this song to the top of the list
            presenter_setbuttons.smoothScrollTo(0, view.getTop());

            // We will use the first section in the new song
            FullscreenActivity.currentSection = 0;

            // Unhightlight all of the items in the set button list except this one
            for (int v = 0; v < presenter_set_buttonsListView.getChildCount(); v++) {
                if (v != which) {
                    // Change the background colour of this button to show it is active
                    ProcessSong.unhighlightPresenterSetButton((Button) presenter_set_buttonsListView.getChildAt(v));
                } else {
                    // Change the background colour of this button to show it is active
                    ProcessSong.highlightPresenterSetButton((Button) presenter_set_buttonsListView.getChildAt(v));
                }
            }

            // Identify our new position in the set
            FullscreenActivity.indexSongInSet = which;
            if (FullscreenActivity.mSetList != null && FullscreenActivity.mSetList.length > which) {
                FullscreenActivity.whatsongforsetwork = FullscreenActivity.mSetList[which];
                FullscreenActivity.linkclicked = FullscreenActivity.mSetList[which];
                if (which < 1) {
                    FullscreenActivity.previousSongInSet = "";
                } else {
                    FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[which - 1];
                }
                if (which == (FullscreenActivity.setSize - 1)) {
                    FullscreenActivity.nextSongInSet = "";
                } else {
                    FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[which + 1];
                }

                // Call the script to get the song location.
                SetActions.getSongFileAndFolder(PresenterMode.this);
                findSongInFolders();
                prepareSongMenu();

                // Close the drawers in case they are open
                closeMyDrawers("both");

                // Save the preferences with the new songfilename
                Preferences.savePreferences();

                // Load the song
                loadSong();
            }
        }
    }
    public void setupSongButtons() {
        // Create a new button for each songSection
        // If the 'song' is custom images, set them as the background
        presenter_song_buttonsListView.removeAllViews();
        presenter_songtitle.setText(FullscreenActivity.mTitle);
        presenter_author.setText(FullscreenActivity.mAuthor);
        presenter_copyright.setText(FullscreenActivity.mCopyright);
        if (FullscreenActivity.mPresentation.isEmpty() || FullscreenActivity.mPresentation.equals("")) {
            presenter_order_text.setText(getResources().getString(R.string.error_notset));
        } else {
            presenter_order_text.setText(FullscreenActivity.mPresentation);
        }
        // Need to decide if checkbox is on or off
        if (FullscreenActivity.usePresentationOrder) {
            presenter_order_text.setChecked(true);
        } else {
            presenter_order_text.setChecked(false);
        }

        imagelocs = null;

        if (FullscreenActivity.isPDF) {
            int pages = FullscreenActivity.pdfPageCount;
            if (pages > 0) {
                for (int p = 0; p < pages; p++) {
                    String sectionText = (p + 1) + "";
                    String buttonText = getResources().getString(R.string.pdf_selectpage) + " " + (p + 1);
                    newSongSectionGroup = ProcessSong.makePresenterSongButtonLayout(PresenterMode.this);
                    newSongSectionText = ProcessSong.makePresenterSongButtonSection(PresenterMode.this, sectionText);
                    newSongButton = ProcessSong.makePresenterSongButtonContent(PresenterMode.this, buttonText);
                    newSongButton.setOnClickListener(new SectionButtonClickListener(p));
                    newSongSectionGroup.addView(newSongSectionText);
                    newSongSectionGroup.addView(newSongButton);
                    presenter_song_buttonsListView.addView(newSongSectionGroup);
                }
            }


        } else if (FullscreenActivity.isImage) {
            String sectionText = getResources().getString(R.string.image);
            String buttonText = FullscreenActivity.songfilename;
            newSongSectionGroup = ProcessSong.makePresenterSongButtonLayout(PresenterMode.this);
            newSongSectionText = ProcessSong.makePresenterSongButtonSection(PresenterMode.this, sectionText);
            newSongButton = ProcessSong.makePresenterSongButtonContent(PresenterMode.this, buttonText);
            newSongButton.setOnClickListener(new SectionButtonClickListener(0));
            newSongSectionGroup.addView(newSongSectionText);
            newSongSectionGroup.addView(newSongButton);
            presenter_song_buttonsListView.addView(newSongSectionGroup);

        } else {
            if (FullscreenActivity.whichSongFolder.contains("../Images")) {
                // Custom images so split the mUser3 field by newline.  Each value is image location
                imagelocs = FullscreenActivity.mUser3.split("\n");
            }

            if (FullscreenActivity.songSections != null && FullscreenActivity.songSections.length > 0) {
                numsectionbuttons = FullscreenActivity.songSections.length;
                for (int x = 0; x < numsectionbuttons; x++) {

                    // Get the image locations if they exist
                    String thisloc = null;
                    if (imagelocs != null && imagelocs[x] != null) {
                        thisloc = imagelocs[x];
                    }

                    StringBuilder buttonText;
                    if (FullscreenActivity.songSections!=null && FullscreenActivity.songSections.length>x) {
                        buttonText = new StringBuilder(FullscreenActivity.songSections[x]);
                    } else {
                        buttonText = new StringBuilder();
                    }
                    // Get the text for the button
                    if (FullscreenActivity.isImageSlide) {
                        if (thisloc == null) {
                            thisloc = "";
                        }
                        buttonText = new StringBuilder(thisloc);
                        FullscreenActivity.file = new File(thisloc);
                        // Try to remove everything except the name
                        if (buttonText.toString().contains("/") && buttonText.lastIndexOf("/") < buttonText.length() - 1) {
                            buttonText = new StringBuilder(buttonText.substring(buttonText.lastIndexOf("/") + 1));
                        }
                    }

                    // If we aren't showing the chords, strip them out
                    if (!FullscreenActivity.presoShowChords) {
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
                        sectionText = FullscreenActivity.songSectionsLabels[x];
                    } catch (Exception e) {
                        sectionText = "";
                    }

                    newSongSectionGroup = ProcessSong.makePresenterSongButtonLayout(PresenterMode.this);
                    newSongSectionText = ProcessSong.makePresenterSongButtonSection(PresenterMode.this, sectionText.replace("_", " "));
                    newSongButton = ProcessSong.makePresenterSongButtonContent(PresenterMode.this, buttonText.toString());

                    if (FullscreenActivity.isImageSlide || FullscreenActivity.isSlide) {
                        // Make sure the time, loop and autoslideshow buttons are visible
                        loopandtimeLinearLayout.setVisibility(View.VISIBLE);
                        enabledisableButton(startstopSlideShow, true);
                        // Just in case we were playing a slide show, stop it
                        prepareStopAutoSlideShow();
                        // Set the appropiate values
                        if (FullscreenActivity.mUser1 != null) {
                            timeEditText.setText(FullscreenActivity.mUser1);
                        }
                        if (FullscreenActivity.mUser2 != null && FullscreenActivity.mUser2.equals("true")) {
                            loopCheckBox.setChecked(true);
                        } else {
                            loopCheckBox.setChecked(false);
                        }

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
        // Select the first button if we can
        FullscreenActivity.currentSection = 0;
        selectSectionButtonInSong(FullscreenActivity.currentSection);
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
            FullscreenActivity.currentSection = which;

            // Send section to other devices (checks we are in stage or presentation mode in called method
            if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
                try {
                    sendSongSectionToConnected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    ProcessSong.unhighlightPresenterSongButton((Button) row.getChildAt(1));
                } else {
                    // Change the background colour of this button to show it is active
                    ProcessSong.highlightPresenterSongButton((Button) row.getChildAt(1));
                }
            }


            // If this is an image, hide the text, show the image, otherwise show the text in the slide window
            if (FullscreenActivity.isPDF) {
                FullscreenActivity.pdfPageCurrent = which;
                loadPDFPagePreview();
            } else if (FullscreenActivity.isImage) {
                loadImagePreview(FullscreenActivity.file);
            } else if (FullscreenActivity.isImageSlide) {
                // Get the image location from the projectedSongSection
                if (imagelocs[FullscreenActivity.currentSection] != null) {
                    File imgfile = new File(imagelocs[FullscreenActivity.currentSection]);
                    loadImagePreview(imgfile);
                }
            } else {
                loadSongPreview();
            }

            // Since the slide has been armed, but not projected, turn off the project button
            // This encourages the user to click it again to update the projector screen
            unhighlightButtonClicked(presenter_project_group);
            projectButton_isSelected = false;
        }
    }
    public void unhighlightAllSetButtons() {
        // Unhighlighting all buttons
        int numbuttons = presenter_set_buttonsListView.getChildCount();
        for (int z = 0; z < numbuttons; z++) {
            ProcessSong.unhighlightPresenterSetButton((Button) presenter_set_buttonsListView.getChildAt(z));
        }
    }
    public void fixNavButtons() {
        // By default disable them all!
        //enabledisableButton(nav_prevsection,false);
        //enabledisableButton(nav_nextsection,false);
        enabledisableButton(nav_prevsong, false);
        enabledisableButton(nav_nextsong, false);

        // Show the previous section button if we currently showing a section higher than 0
        if (FullscreenActivity.currentSection > 0) {
            enabledisableButton(nav_prevsection, true);
        } else {
            enabledisableButton(nav_prevsection, false);
        }

        // Show the next section button if we are currently in a section lower than the count by 1
        int sectionsavailable = presenter_song_buttonsListView.getChildCount();
        if (FullscreenActivity.currentSection < sectionsavailable - 1) {
            enabledisableButton(nav_nextsection, true);
        } else {
            enabledisableButton(nav_nextsection, false);
        }

        // Enable the previous set button if we are in set view and indexSongInSet is >0 (but less than set size)
        int numsongsinset = 0;
        if (FullscreenActivity.mSetList != null) {
            numsongsinset = FullscreenActivity.mSetList.length;
        }
        if (FullscreenActivity.setView && FullscreenActivity.indexSongInSet > 0 && FullscreenActivity.indexSongInSet < numsongsinset) {
            enabledisableButton(nav_prevsong, true);
        } else {
            enabledisableButton(nav_prevsong, false);
        }

        // Enable the next set button if we are in set view and index SongInSet is < set size -1
        if (FullscreenActivity.setView && FullscreenActivity.indexSongInSet > -1 && FullscreenActivity.indexSongInSet < numsongsinset - 1) {
            enabledisableButton(nav_nextsong, true);
        } else {
            enabledisableButton(nav_nextsong, false);
        }

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
    public void tryClickNextSection() {
        if (FullscreenActivity.currentSection < FullscreenActivity.songSections.length - 1) {
            FullscreenActivity.currentSection += 1;
            autoproject = true;
            preso_action_buttons_scroll.smoothScrollTo(0, presenter_project_group.getTop());
            selectSectionButtonInSong(FullscreenActivity.currentSection);
        }
    }
    public void tryClickPreviousSection() {
        // Enable or disable the previous section button
        if (FullscreenActivity.currentSection > 0) {
            FullscreenActivity.currentSection -= 1;
            autoproject = true;
            preso_action_buttons_scroll.smoothScrollTo(0, presenter_project_group.getTop());
            selectSectionButtonInSong(FullscreenActivity.currentSection);
        }
    }
    public void tryClickNextSongInSet() {
        if (FullscreenActivity.mSetList != null && FullscreenActivity.mSetList.length > FullscreenActivity.indexSongInSet &&
                FullscreenActivity.indexSongInSet < FullscreenActivity.mSetList.length - 1) {
            FullscreenActivity.indexSongInSet += 1;
            FullscreenActivity.currentSection = 0;
            autoproject = true;
            doMoveInSet();
        }
    }
    public void tryClickPreviousSongInSet() {
        if (FullscreenActivity.mSetList != null && FullscreenActivity.mSetList.length > FullscreenActivity.indexSongInSet &&
                FullscreenActivity.indexSongInSet > 0) {
            FullscreenActivity.indexSongInSet -= 1;
            FullscreenActivity.currentSection = 0;
            autoproject = true;
            doMoveInSet();
        }
    }
    public void enabledisableButton(FloatingActionButton fab, boolean enable) {
        fab.setEnabled(enable);
        if (enable) {
            fab.setAlpha(1.0f);
        } else {
            fab.setAlpha(0.5f);
        }
    }
    @Override
    public void doMoveSection() {
        switch (FullscreenActivity.setMoveDirection) {
            case "forward":
                FullscreenActivity.currentSection += 1;
                selectSectionButtonInSong(FullscreenActivity.currentSection);
                break;
            case "back":
                FullscreenActivity.currentSection -= 1;
                selectSectionButtonInSong(FullscreenActivity.currentSection);
                break;
        }
    }
    public void selectSectionButtonInSong(int which) {

        FullscreenActivity.currentSection = which;
        if (FullscreenActivity.songSections != null && FullscreenActivity.songSections.length > 0) {
            // if which=-1 then we want to pick the first section of the previous song in set
            // Otherwise, move to the next one.
            // If we are at the end, move to the nextsonginset

            if (FullscreenActivity.currentSection < 0 && !isplayingautoslideshow) {
                FullscreenActivity.currentSection = 0;
                tryClickPreviousSongInSet();
            } else if (FullscreenActivity.currentSection >= FullscreenActivity.songSections.length && !isplayingautoslideshow) {
                FullscreenActivity.currentSection = 0;
                tryClickNextSongInSet();
            } else if (FullscreenActivity.currentSection < 0 || FullscreenActivity.currentSection >= FullscreenActivity.songSections.length) {
                FullscreenActivity.currentSection = 0;
            }

            // enable or disable the quick nav buttons
            fixNavButtons();

            LinearLayout row = (LinearLayout) presenter_song_buttonsListView.getChildAt(FullscreenActivity.currentSection);
            Button thisbutton = (Button) row.getChildAt(1);
            thisbutton.performClick();
        }
    }
    @Override
    public void doMoveInSet() {
        doCancelAsyncTask(do_moveinset);
        do_moveinset = new DoMoveInSet();
        try {
            do_moveinset.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class DoMoveInSet extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Get the appropriate song
            try {
                FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
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
                    FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
                    FullscreenActivity.whatsongforsetwork = FullscreenActivity.linkclicked;
                    FullscreenActivity.setMoveDirection = ""; // Expects back or forward for Stage/Performance, but not here
                    SetActions.doMoveInSet(PresenterMode.this);

                    // Set indexSongInSet position has moved
                    //invalidateOptionsMenu();

                    // Click the item in the set list
                    if (presenter_set_buttonsListView.getChildAt(FullscreenActivity.indexSongInSet) != null) {
                        presenter_set_buttonsListView.getChildAt(FullscreenActivity.indexSongInSet).performClick();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Salut stuff
    @Override
    public void onDataReceived(Object data) {
        // Attempt to extract the song details
        if (data != null && (data.toString().contains("_____") || data.toString().contains("<lyrics>") ||
                data.toString().contains("___section___"))) {
            String action = ProcessSong.getSalutReceivedLocation(data.toString(), PresenterMode.this);
            switch (action) {
                case "Location":
                    holdBeforeLoading();
                    break;
                case "HostFile":
                    holdBeforeLoadingXML();
                    break;
                case "SongSection":
                    holdBeforeLoadingSection(ProcessSong.getSalutReceivedSection(data.toString()));
                    break;
            }
        }
    }
    public void holdBeforeSending() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we send it, check this is the first time
        if (FullscreenActivity.firstSendingOfSalut) {
            // Now turn it off
            FullscreenActivity.firstSendingOfSalut = false;
            if (FullscreenActivity.network != null) {
                if (FullscreenActivity.network.isRunningAsHost) {
                    try {
                        Log.d("d","myMessage being sent="+myMessage.toString());
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
                // After a delay of 2 seconds, reset the firstSendingOfSalut;
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.firstSendingOfSalut = true;
                    }
                }, 500);
            }
        }
    }
    public void holdBeforeSendingXML() {
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

                // After a delay of 2 seconds, reset the firstSendingOfSalut;
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.firstSendingOfSalutXML = true;
                    }
                }, 500);
            }
        }
    }
    public void holdBeforeSendingSection() {
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

                // After a delay of 2 seconds, reset the firstSendingOfSalutSection;
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.firstSendingOfSalutSection = true;
                    }
                }, 500);
            }
        }
    }


    public void holdBeforeLoading() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we receive if, check this is the first time
        if (FullscreenActivity.firstReceivingOfSalut) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalut = false;
            // Decide if the file exists on this device first
            File f;
            if (FullscreenActivity.whichSongFolder.equals("") || FullscreenActivity.whichSongFolder.equals(getString(R.string.mainfoldername))) {
                f = new File(FullscreenActivity.dir,FullscreenActivity.songfilename);
            } else {
                f = new File(FullscreenActivity.dir,FullscreenActivity.whichSongFolder);
                f = new File(f,FullscreenActivity.songfilename);
            }
            if (f.exists()) {
                loadSong();
            }

            // After a delay of 2 seconds, reset the firstReceivingOfSalut;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalut = true;
                }
            },500);
        }
    }
    public void holdBeforeLoadingXML() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we receive if, check this is the first time
        if (FullscreenActivity.firstReceivingOfSalutXML) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalutXML = false;

            File f;
            if (FullscreenActivity.whichSongFolder.equals("") || FullscreenActivity.whichSongFolder.equals(getString(R.string.mainfoldername))) {
                f = new File(FullscreenActivity.dir,FullscreenActivity.songfilename);
            } else {
                f = new File(FullscreenActivity.dir,FullscreenActivity.whichSongFolder);
                f = new File(f,FullscreenActivity.songfilename);
            }
            if (!f.exists() || FullscreenActivity.receiveHostFiles) {
                FullscreenActivity.mySalutXML = FullscreenActivity.mySalutXML.replace("\\n", "$$__$$");
                FullscreenActivity.mySalutXML = FullscreenActivity.mySalutXML.replace("\\", "");
                FullscreenActivity.mySalutXML = FullscreenActivity.mySalutXML.replace("$$__$$", "\n");

                // Create the temp song file
                try {
                    if (!FullscreenActivity.dirreceived.exists()) {
                        if (!FullscreenActivity.dirreceived.mkdirs()) {
                            Log.d("d", "Couldn't make directory");
                        }
                    }
                    FullscreenActivity.file = new File(FullscreenActivity.dirreceived, "ReceivedSong");

                    FileUtils.writeStringToFile(FullscreenActivity.file, FullscreenActivity.mySalutXML, "UTF-8");

                    FullscreenActivity.songfilename = "ReceivedSong";
                    FullscreenActivity.whichSongFolder = "../Received";
                } catch (Exception e) {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.songdoesntexist);
                    ShowToast.showToast(PresenterMode.this);
                }
                loadSong();
            }
            // After a delay of 2 seconds, reset the firstReceivingOfSalut;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalutXML = true;
                }
            },500);
        }
    }
    public void holdBeforeLoadingSection(int s) {
        if (FullscreenActivity.firstReceivingOfSalutSection) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalutSection = false;
            FullscreenActivity.currentSection = s;
            selectSectionButtonInSong(FullscreenActivity.currentSection);

            // After a delay of 2 seconds, reset the firstReceivingOfSalutSection;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalutSection = true;
                }
            }, 500);
        }
    }

    public void sendSongLocationToConnected() {
        Log.d("d","Sending song");
        String messageString = FullscreenActivity.whichSongFolder + "_____" +
                FullscreenActivity.songfilename + "_____" +
                FullscreenActivity.whichDirection;

        myMessage = new SalutMessage();
        myMessage.description = messageString;
        holdBeforeSending();
    }
    public void sendSongXMLToConnected() {
        String myXML;
        if (FullscreenActivity.isSong && FullscreenActivity.myXML != null) {
            myXML = FullscreenActivity.presenterSendSong;
        } else {
            myXML = "";
        }
        mySongMessage = new SalutMessage();
        mySongMessage.description = myXML;
        holdBeforeSendingXML();
    }
    public void sendSongSectionToConnected() {
        int sectionval;
        if (FullscreenActivity.whichMode.equals("Stage") || FullscreenActivity.whichMode.equals("Presentation")) {
            sectionval = FullscreenActivity.currentSection;
            mySectionMessage = new SalutMessage();
            mySectionMessage.description = "___section___" + sectionval;
            holdBeforeSendingSection();
        }
    }
    public void getBluetoothName() {
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
    public void startRegistration() {
        try {
            FullscreenActivity.dataReceiver = new SalutDataReceiver(PresenterMode.this, PresenterMode.this);
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

    // Loading the song
    @Override
    public void loadSongFromSet() {
        Preferences.savePreferences();
        // Redraw the set buttons as the user may have changed the order
        refreshAll();

        closePopUps();

        FullscreenActivity.setView = true;
        // Specify which songinset button
        FullscreenActivity.currentSection = 0;

        // Select it
        if (presenter_set_buttonsListView.getChildCount() > FullscreenActivity.indexSongInSet) {
            Button which_song_to_click = (Button) presenter_set_buttonsListView.getChildAt(FullscreenActivity.indexSongInSet);
            which_song_to_click.performClick();
        }
    }
    public void loadSong() {
        // Don't do this for a blacklisted filetype (application, video, audio)
        if (ListSongFiles.blacklistFileType(FullscreenActivity.songfilename)) {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.file_type_unknown);
            ShowToast.showToast(PresenterMode.this);
        } else {
            // Declare we have loaded a new song (for the ccli log).
            // This stops us reporting projecting every section
            newsongloaded = true;

            // Send WiFiP2P intent
            if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
                try {
                    sendSongLocationToConnected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            doCancelAsyncTask(loadsong_async);
            loadsong_async = new LoadSong();
            try {
                loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                // Error loading the song
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class LoadSong extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                // Load up the song
                try {
                    LoadXML.loadXML(PresenterMode.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Clear the old headings (presention order looks for these)
                FullscreenActivity.foundSongSections_heading = new ArrayList<>();

                // Don't process images or image slide details here.  No need.  Only do this for songs
                if (FullscreenActivity.isPDF) {
                    LoadXML.getPDFPageCount();
                } else if (FullscreenActivity.isSong || FullscreenActivity.isSlide || FullscreenActivity.isScripture) {
                    /*if (!FullscreenActivity.presoShowChords) {
                        FullscreenActivity.myLyrics = ProcessSong.removeChordLines(FullscreenActivity.mLyrics);
                    }
                    FullscreenActivity.myLyrics = ProcessSong.removeCommentLines(FullscreenActivity.myLyrics);
                    FullscreenActivity.myLyrics = ProcessSong.removeUnderScores(FullscreenActivity.myLyrics, PresenterMode.this);
*/
                    // 1. Sort multiline verse/chord formats
                    FullscreenActivity.myLyrics = ProcessSong.fixMultiLineFormat(FullscreenActivity.myLyrics);

                    // 2. Split the song into sections
                    FullscreenActivity.songSections = ProcessSong.splitSongIntoSections(FullscreenActivity.myLyrics, PresenterMode.this);

                    // 3. Put the song into presentation order if required
                    if (FullscreenActivity.usePresentationOrder && !FullscreenActivity.mPresentation.isEmpty() && !FullscreenActivity.mPresentation.equals("")) {
                        FullscreenActivity.songSections = ProcessSong.matchPresentationOrder(FullscreenActivity.songSections, PresenterMode.this);
                    }

                    // 3b Add extra sections for double linebreaks and || code
                    FullscreenActivity.songSections = ProcessSong.splitLaterSplits(FullscreenActivity.songSections);

                    // 4. Get the section headings/types (may have changed after presentationorder
                    FullscreenActivity.songSectionsLabels = new String[FullscreenActivity.songSections.length];
                    FullscreenActivity.songSectionsTypes = new String[FullscreenActivity.songSections.length];
                    for (int sl = 0; sl < FullscreenActivity.songSections.length; sl++) {
                        FullscreenActivity.songSectionsLabels[sl] = ProcessSong.getSectionHeadings(FullscreenActivity.songSections[sl]);
                        if (!FullscreenActivity.foundSongSections_heading.contains(FullscreenActivity.songSectionsLabels[sl])) {
                            FullscreenActivity.foundSongSections_heading.add(FullscreenActivity.songSectionsLabels[sl]);
                        }
                    }

                    // 5. Get rid of the tag/heading lines
                    FullscreenActivity.songSections = ProcessSong.removeTagLines(FullscreenActivity.songSections);



                    // If we are a host then rebuild
                    if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
                        PopUpEditSongFragment.prepareSongXML();
                        // Replace the lyrics
                        FullscreenActivity.presenterSendSong = FullscreenActivity.myXML.replace(FullscreenActivity.mLyrics,
                                ProcessSong.rebuildParsedLyrics(FullscreenActivity.songSections.length));
                    } else {
                        FullscreenActivity.presenterSendSong = FullscreenActivity.myXML;
                    }

                    // Now that we have generated the song to send to a guest device, decide if we should remove chords, comments, etc.
                    for (int i=0; i<FullscreenActivity.songSections.length; i++) {
                        if (!FullscreenActivity.presoShowChords) {
                            FullscreenActivity.songSections[i] = ProcessSong.removeChordLines(FullscreenActivity.songSections[i]);
                        }
                        if (FullscreenActivity.network == null || !FullscreenActivity.network.isRunningAsHost) {
                            FullscreenActivity.songSections[i] = ProcessSong.removeCommentLines(FullscreenActivity.songSections[i]);
                        }
                        FullscreenActivity.songSections[i] = ProcessSong.removeUnderScores(FullscreenActivity.songSections[i], PresenterMode.this);
                    }

                    // We need to split each section into string arrays by line
                    FullscreenActivity.sectionContents = new String[FullscreenActivity.songSections.length][];
                    FullscreenActivity.projectedContents = new String[FullscreenActivity.songSections.length][];
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                        FullscreenActivity.sectionContents[x] = FullscreenActivity.songSections[x].split("\n");
                        FullscreenActivity.projectedContents[x] = FullscreenActivity.songSections[x].split("\n");
                    }

                    // Determine what each line type is
                    // Copy the array of sectionContents into sectionLineTypes
                    // Then we'll replace the content with the line type
                    // This keeps the array sizes the same simply
                    //FullscreenActivity.sectionLineTypes = new String[FullscreenActivity.sectionContents.length][];
                    //FullscreenActivity.projectedLineTypes = new String[FullscreenActivity.projectedContents.length][];
                    FullscreenActivity.sectionLineTypes = new String[FullscreenActivity.songSections.length][];
                    FullscreenActivity.projectedLineTypes = new String[FullscreenActivity.songSections.length][];


                    for (int x = 0; x < FullscreenActivity.sectionLineTypes.length; x++) {
                        FullscreenActivity.sectionLineTypes[x] = new String[FullscreenActivity.sectionContents[x].length];
                        for (int y = 0; y < FullscreenActivity.sectionLineTypes[x].length; y++) {
                            FullscreenActivity.sectionLineTypes[x][y] = ProcessSong.determineLineTypes(FullscreenActivity.sectionContents[x][y], PresenterMode.this);
                            if (FullscreenActivity.sectionContents[x][y] != null &&
                                    FullscreenActivity.sectionContents[x][y].length() > 0 && (FullscreenActivity.sectionContents[x][y].indexOf(" ") == 0 ||
                                    FullscreenActivity.sectionContents[x][y].indexOf(".") == 0 || FullscreenActivity.sectionContents[x][y].indexOf(";") == 0)) {
                                FullscreenActivity.sectionContents[x][y] = FullscreenActivity.sectionContents[x][y].substring(1);
                            }
                        }
                    }

                    for (int x = 0; x < FullscreenActivity.projectedLineTypes.length; x++) {
                        FullscreenActivity.projectedLineTypes[x] = new String[FullscreenActivity.projectedContents[x].length];
                        for (int y = 0; y < FullscreenActivity.projectedLineTypes[x].length; y++) {
                            FullscreenActivity.projectedLineTypes[x][y] = ProcessSong.determineLineTypes(FullscreenActivity.projectedContents[x][y], PresenterMode.this);
                            /*if (FullscreenActivity.projectedContents[x][y] != null &&
                                    FullscreenActivity.projectedContents[x][y].length() > 0 && (FullscreenActivity.projectedContents[x][y].indexOf(" ") == 0 ||
                                    FullscreenActivity.projectedContents[x][y].indexOf(".") == 0 || FullscreenActivity.projectedContents[x][y].indexOf(";") == 0)) {
                                FullscreenActivity.projectedContents[x][y] = FullscreenActivity.projectedContents[x][y].substring(1);
                            }*/
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "done";
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
                    // Now, reset the orientation.
                    FullscreenActivity.orientationchanged = false;

                    // Get the current orientation
                    FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

                    SetActions.indexSongInSet();
                    if (!FullscreenActivity.setView) {
                        // Unhighlight the set buttons
                        unhighlightAllSetButtons();
                    }
                    showCorrectViews();
                    findSongInFolders();
                    setupSongButtons();

                    // Send WiFiP2P intent
                    if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
                        try {
                            sendSongXMLToConnected();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Send the midi data if we can
                    sendMidi();

                    // If the user has shown the 'Welcome to OpenSongApp' file, and their song lists are empty,
                    // open the find new songs menu
                    if (FullscreenActivity.mTitle.equals("Welcome to OpenSongApp") &&
                            (FullscreenActivity.mSongFileNames==null ||
                                    (FullscreenActivity.mSongFileNames!=null && FullscreenActivity.mSongFileNames.length==0))) {
                        FullscreenActivity.whichOptionMenu = "FIND";
                        prepareOptionMenu();
                        Handler find = new Handler();
                        find.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                openMyDrawers("option");
                            }
                        },2000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressWarnings("deprecation")
    public void loadPDFPagePreview() {
        Bitmap bmp = ProcessSong.createPDFPage(PresenterMode.this, 800, 800, "Y");

        presenter_lyrics_image.setVisibility(View.VISIBLE);
        presenter_lyrics.setVisibility(View.GONE);

        if (bmp != null) {
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(bmp.getWidth(), bmp.getHeight());
            presenter_lyrics_image.setLayoutParams(llp);
            // Set the image to the view
            presenter_lyrics_image.setBackgroundColor(0xffffffff);
            presenter_lyrics_image.setImageBitmap(bmp);

        } else {
            // Set the image to the unhappy android
            Drawable myDrawable = getResources().getDrawable(R.drawable.unhappy_android);
            presenter_lyrics_image.setImageDrawable(myDrawable);

            // Set an intent to try and open the pdf with an appropriate application
            Intent target = new Intent(Intent.ACTION_VIEW);
            // Run an intent to try to show the pdf externally
            target.setDataAndType(Uri.fromFile(FullscreenActivity.file), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            callIntent("openpdf", target);
        }
        if (autoproject || FullscreenActivity.autoProject) {
            autoproject = false;
            presenter_project_group.performClick();
        }
    }
    public void loadImagePreview(File f) {
        FullscreenActivity.file = f;

        // Make the appropriate bits visible
        presenter_lyrics_image.setVisibility(View.VISIBLE);
        presenter_lyrics.setVisibility(View.GONE);

        // Process the image location into an URI, then get the sizes
        Uri imageUri = Uri.fromFile(f);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(FullscreenActivity.file.toString(), options);
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
                .override(glidewidth,glideheight);
        Glide.with(PresenterMode.this).load(imageUri).apply(myOptions).into(presenter_lyrics_image);

        if (autoproject || FullscreenActivity.autoProject) {
            autoproject = false;
            presenter_project_group.performClick();
        }
    }
    public void loadSongPreview() {
        // Set the appropriate views to visible
        presenter_lyrics_image.setVisibility(View.GONE);
        presenter_lyrics.setVisibility(View.VISIBLE);

        // Prepare the text to go in the view
        StringBuilder s = new StringBuilder();
        try {
            if (FullscreenActivity.projectedContents!=null && FullscreenActivity.projectedContents[FullscreenActivity.currentSection]!=null) {
                for (int w = 0; w < FullscreenActivity.projectedContents[FullscreenActivity.currentSection].length; w++) {
                    if (FullscreenActivity.presoShowChords) {
                        s.append(FullscreenActivity.projectedContents[FullscreenActivity.currentSection][w]).append("\n");
                    } else {
                        s.append(FullscreenActivity.projectedContents[FullscreenActivity.currentSection][w].trim()).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        s = new StringBuilder(s.toString().trim());

        // And write it
        presenter_lyrics.setText(s.toString());

        if (autoproject || FullscreenActivity.autoProject) {
            autoproject = false;
            presenter_project_group.performClick();
        }
    }

    // Interface listeners for PopUpPages
    @Override
    public void backupInstall(String message) {
        // Songs have been imported, so update the song menu and rebuild the search index
        showToastMessage(message);
        prepareSongMenu();
        rebuildSearchIndex();
        openMyDrawers("song");
        FullscreenActivity.whattodo = "choosefolder";
        openFragment();
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
                    FullscreenActivity.myToastMessage = getString(R.string.error);
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
    public void confirmedAction() {
        switch (FullscreenActivity.whattodo) {
            case "exit":
                this.finish();
                break;

            case "saveset":
                // Save the set
                SetActions.saveSetMessage(PresenterMode.this);
                refreshAll();
                break;

            case "clearset":
                // Clear the set
                SetActions.clearSet(PresenterMode.this);
                refreshAll();
                break;

            case "deletesong":
                // Delete current song
                ListSongFiles.deleteSong(PresenterMode.this);
                //invalidateOptionsMenu();
                Preferences.savePreferences();
                refreshAll();
                break;

            case "deleteset":
                // Delete set
                SetActions.deleteSet(PresenterMode.this);
                refreshAll();
                break;

            case "wipeallsongs":
                // Wipe all songs
                if (!ListSongFiles.clearAllSongs()) {
                    Log.d("d","Problem clearing all songs");
                }
                refreshAll();
                break;

            case "resetcolours":
                // Reset the theme colours
                PopUpThemeChooserFragment.getDefaultColours();
                Preferences.savePreferences();
                refreshAll();
                FullscreenActivity.whattodo = "changetheme";
                openFragment();
                break;
        }
    }
    @Override
    public void doEdit() {
        if (FullscreenActivity.isPDF) {
            FullscreenActivity.whattodo = "extractPDF";
            openFragment();
        } else if (FullscreenActivity.isSong) {
            FullscreenActivity.whattodo = "editsong";
            openFragment();
        }
    }
    public boolean justSong(Context c) {
        boolean isallowed = true;
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF || !FullscreenActivity.isSong) {
            showToastMessage(c.getResources().getString(R.string.not_allowed));
            isallowed = false;
        }
        return isallowed;
    }
    @Override
    public void openFragment() {
        // Load the whichSongFolder in case we were browsing elsewhere
        Preferences.loadFolderName();

        // Initialise the newFragment
        newFragment = OpenFragment.openFragment(PresenterMode.this);
        String message = OpenFragment.getMessage(PresenterMode.this);

        if (newFragment != null) {
            try {
                newFragment.show(getFragmentManager(), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onSongImportDone(String message) {
        FullscreenActivity.myToastMessage = message;
        if (!message.equals("cancel")) {
            showToastMessage(message);
            prepareSongMenu();
        }
        OnSongConvert.doBatchConvert(PresenterMode.this);
    }
    @Override
    public void shareSong() {
        if (justSong(PresenterMode.this)) {
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
    @SuppressLint("StaticFieldLeak")
    private class ShareSong extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                Intent emailIntent = ExportPreparer.exportSong(PresenterMode.this, FullscreenActivity.bmScreen);
                startActivityForResult(Intent.createChooser(emailIntent, getResources().getString(R.string.exportcurrentsong)), 12345);
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
    }
    public void shareActivityLog() {
        doCancelAsyncTask(shareactivitylog_async);
        shareactivitylog_async = new ShareActivityLog();
        try {
            shareactivitylog_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                Intent emailIntent = ExportPreparer.exportActivityLog(PresenterMode.this);
                startActivityForResult(Intent.createChooser(emailIntent, "ActivityLog.xml"), 2222);
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
    }
    public void shareSet() {
        doCancelAsyncTask(shareset_async);
        shareset_async = new ShareSet();
        try {
            shareset_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ShareSet extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                Intent emailIntent = ExportPreparer.exportSet(PresenterMode.this);
                startActivityForResult(Intent.createChooser(emailIntent, getResources().getString(R.string.exportsavedset)), 12345);
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
            FullscreenActivity.myToastMessage = message;
            ShowToast.showToast(PresenterMode.this);
        }
    }
    @Override
    public void shuffleSongsInSet() {
        SetActions.indexSongInSet();
        fixSet();
        newFragment = PopUpSetViewNew.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }
    @Override
    public void splashScreen() {
        FullscreenActivity.showSplashVersion = 0;
        Preferences.savePreferences();
        Intent intent = new Intent();
        intent.putExtra("showsplash",true);
        intent.setClass(PresenterMode.this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void toggleDrawerSwipe() {
        if (FullscreenActivity.swipeForMenus) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        closeMyDrawers("both");
    }
    @Override
    public void updateCustomStorage() {
        switch (FullscreenActivity.whattodo) {
            case "customstoragefind":
                FullscreenActivity.whattodo = "managestorage";
                openFragment();
                break;
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
    @SuppressLint("StaticFieldLeak")
    private class LoadCustomReusable extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... obj) {
            try {
                LoadXML.prepareLoadCustomReusable(FullscreenActivity.customreusabletoload, PresenterMode.this);
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
                    newFragment = PopUpCustomSlideFragment.newInstance();
                    newFragment.show(getFragmentManager(), "dialog");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    private class AddSlideToSet extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Add the slide
            try {
                CustomSlide.addCustomSlide(PresenterMode.this);
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
    @Override
    public void removeSongFromSet(int val) {
        // Vibrate to let the user know something happened
        DoVibrate.vibrate(PresenterMode.this, 50);

        // Take away the menu item
        String tempSong = FullscreenActivity.mSetList[val];
        FullscreenActivity.mSetList[val] = "";

        FullscreenActivity.mySet = "";
        StringBuilder sb = new StringBuilder();
        for (String aMSetList : FullscreenActivity.mSetList) {
            if (!aMSetList.isEmpty()) {
                sb.append("$**_").append(aMSetList).append("_**$");
            }
        }

        FullscreenActivity.mySet = sb.toString();

        // Tell the user that the song has been removed.
        showToastMessage("\"" + tempSong + "\" "
                + getResources().getString(R.string.removedfromset));

        //Check to see if our set list is still valid
        SetActions.prepareSetList();
        prepareOptionMenu();
        fixSet();
        //invalidateOptionsMenu();

        // Save set
        Preferences.savePreferences();

        closeMyDrawers("option");
    }
    @Override
    public void changePDFPage(int page, String direction) {
        FullscreenActivity.whichDirection = direction;
        FullscreenActivity.pdfPageCurrent = page;
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

    @Override
    public void sendMidi() {
        if ((Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI) &&
                FullscreenActivity.midiAuto && FullscreenActivity.midiDevice!=null &&
                FullscreenActivity.midiInputPort!=null && FullscreenActivity.mMidi!=null &&
                !FullscreenActivity.mMidi.isEmpty()) && !FullscreenActivity.mMidi.trim().equals("")) {
            // Declare the midi code
            Handler mh = new Handler();
            mh.post(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    try {
                        if (midi==null) {
                            midi = new Midi();
                        }
                        // Split the midi messages by line, after changing , into new line
                        FullscreenActivity.mMidi = FullscreenActivity.mMidi.replace(",", "\n");
                        FullscreenActivity.mMidi = FullscreenActivity.mMidi.replace("\n\n", "\n");
                        String[] midilines = FullscreenActivity.mMidi.trim().split("\n");
                        for (String ml : midilines) {
                            Log.d("d","Sending "+ml);
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

    // The song index
    public void displayIndex() {
        LinearLayout indexLayout = findViewById(R.id.side_index);
        if (FullscreenActivity.showAlphabeticalIndexInSongMenu) {
            indexLayout.setVisibility(View.VISIBLE);
        } else {
            indexLayout.setVisibility(View.GONE);
        }
        indexLayout.removeAllViews();
        TextView textView;
        List<String> indexList = new ArrayList<>(FullscreenActivity.mapIndex.keySet());
        for (String index : indexList) {
            textView = (TextView) View.inflate(PresenterMode.this,
                    R.layout.leftmenu, null);
            textView.setTextSize(FullscreenActivity.alphabeticalSize);
            int i = (int) FullscreenActivity.alphabeticalSize *2;
            textView.setPadding(i,i,i,i);
            textView.setText(index);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView selectedIndex = (TextView) view;
                    song_list_view.setSelection(FullscreenActivity.mapIndex.get(selectedIndex.getText().toString()));
                }
            });
            indexLayout.addView(textView);
        }
    }
    public void rebuildSearchIndex() {
        doCancelAsyncTask(indexsongs_task);
        indexsongs_task = new IndexSongs.IndexMySongs(PresenterMode.this);
        indexsongs_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    @Override
    public void indexingDone() {
        doCancelAsyncTask(indexing_done);
        indexing_done = new IndexingDone();
        try {
            indexing_done.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class IndexingDone extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                // Add locale sort
                Collator coll = Collator.getInstance(FullscreenActivity.locale);
                coll.setStrength(Collator.SECONDARY);
                Collections.sort(FullscreenActivity.search_database, coll);

                // Copy the full search string, now it is sorted, into a song and folder array
                FullscreenActivity.searchFileName.clear();
                FullscreenActivity.searchFolder.clear();
                FullscreenActivity.searchTitle.clear();
                FullscreenActivity.searchAuthor.clear();
                FullscreenActivity.searchShortLyrics.clear();
                FullscreenActivity.searchTheme.clear();
                FullscreenActivity.searchKey.clear();
                FullscreenActivity.searchHymnNumber.clear();

                for (int d = 0; d < FullscreenActivity.search_database.size(); d++) {
                    String[] songbits = FullscreenActivity.search_database.get(d).split("_%%%_");
                    if (songbits[0] != null && songbits[1] != null && songbits[2] != null && songbits[3] != null &&
                            songbits[4] != null && songbits[5] != null && songbits[6] != null && songbits[7] != null) {
                        FullscreenActivity.searchFileName.add(d, songbits[0].trim());
                        FullscreenActivity.searchFolder.add(d, songbits[1].trim());
                        FullscreenActivity.searchTitle.add(d, songbits[2].trim());
                        FullscreenActivity.searchAuthor.add(d, songbits[3].trim());
                        FullscreenActivity.searchShortLyrics.add(d, songbits[4].trim());
                        FullscreenActivity.searchTheme.add(d, songbits[5].trim());
                        FullscreenActivity.searchKey.add(d, songbits[6].trim());
                        FullscreenActivity.searchHymnNumber.add(d, songbits[7].trim());
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
                    prepareSongMenu();
                }
            } catch (Exception e) {
                // Ooops, error when updating song menu
            }
        }
    }
    @Override
    public boolean onQueryTextSubmit(String newText) {
        SearchViewItems item = (SearchViewItems) FullscreenActivity.sva.getItem(0);
        FullscreenActivity.songfilename = item.getFilename();
        FullscreenActivity.whichSongFolder = item.getFolder();
        FullscreenActivity.setView = false;
        FullscreenActivity.myToastMessage = FullscreenActivity.songfilename;
        //Save preferences
        Preferences.savePreferences();
        loadSong();
        return true;
    }
    @Override
    public boolean onQueryTextChange(String newText) {
        // Replace unwanted symbols
        newText = ProcessSong.removeUnwantedSymbolsAndSpaces(newText);
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
        Preferences.savePreferences();
        doEdit();
    }

    // The stuff to deal with the slideshow
    public void prepareStopAutoSlideShow() {
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
    public void prepareStartAutoSlideShow() {
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
                    if (FullscreenActivity.currentSection < FullscreenActivity.songSections.length - 1 && isplayingautoslideshow) {
                        // Move to next song section
                        FullscreenActivity.currentSection++;
                        selectSectionButtonInSong(FullscreenActivity.currentSection);
                        prepareStopAutoSlideShow();
                        prepareStartAutoSlideShow();
                    } else if (autoslideloop && FullscreenActivity.currentSection >= (FullscreenActivity.songSections.length - 1) && isplayingautoslideshow) {
                        // Go back to first song section
                        FullscreenActivity.currentSection = 0;
                        selectSectionButtonInSong(FullscreenActivity.currentSection);
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
                while (end < (start + (autoslidetime * 1000)) && isplayingautoslideshow) {
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

    // The right hand column buttons
    public void projectButtonClick() {
        try {
            projectButton_isSelected = !projectButton_isSelected;

            if (!FullscreenActivity.isPDF && !FullscreenActivity.isImage && !FullscreenActivity.isImageSlide) {
                FullscreenActivity.projectedContents[FullscreenActivity.currentSection] = presenter_lyrics.getText().toString().split("\n");
                int linesnow = FullscreenActivity.projectedContents[FullscreenActivity.currentSection].length;
                FullscreenActivity.projectedLineTypes[FullscreenActivity.currentSection] = new String[linesnow];
                for (int i = 0; i < linesnow; i++) {
                    FullscreenActivity.projectedLineTypes[FullscreenActivity.currentSection][i] =
                            ProcessSong.determineLineTypes(FullscreenActivity.projectedContents[FullscreenActivity.currentSection][i], PresenterMode.this);
                }
            }

            // Turn off the other actions buttons as we are now projecting!
            if (logoButton_isSelected) {
                presenter_logo_group.performClick();  // This turns off the logo
            }
            if (blankButton_isSelected) {
                presenter_blank_group.performClick();
            }


            // Turn on the project button for now
            highlightButtonClicked(presenter_project_group);


            // Update the projector
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.doUpdate();

                    // If we are autologging CCLI information
                    if (newsongloaded && FullscreenActivity.ccli_automatic) {
                        PopUpCCLIFragment.addUsageEntryToLog(FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename,
                                FullscreenActivity.mTitle.toString(), FullscreenActivity.mAuthor.toString(),
                                FullscreenActivity.mCopyright.toString(), FullscreenActivity.mCCLI, "5"); // Presented
                        newsongloaded = false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.doUpdate();
                    // If we are autologging CCLI information
                    if (newsongloaded && FullscreenActivity.ccli_automatic) {
                        PopUpCCLIFragment.addUsageEntryToLog(FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename,
                                FullscreenActivity.mTitle.toString(), FullscreenActivity.mAuthor.toString(),
                                FullscreenActivity.mCopyright.toString(), FullscreenActivity.mCCLI, "5"); // Presented
                        newsongloaded = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Handler unhighlight = new Handler();
            unhighlight.postDelayed(new Runnable() {
                @Override
                public void run() {
                    projectButton_isSelected = false;
                    unhighlightButtonClicked(presenter_project_group);
                }
            }, 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void logoButtonClick() {
        if (projectButton_isSelected) {
            projectButton_isSelected = false;
            unhighlightButtonClicked(presenter_project_group);
        }
        if (blankButton_isSelected) {
            blankButton_isSelected = false;
            unhighlightButtonClicked(presenter_blank_group);
        }

        logoButton_isSelected = !logoButton_isSelected;

        if (logoButton_isSelected) {
            // Fade in the logo after highlighting the button and disabling
            presenter_logo_group.setEnabled(false);
            highlightButtonClicked(presenter_logo_group);
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.showLogo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.showLogo();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter_logo_group.setEnabled(true);
                }
            }, 800);
        } else {
            // Fade out the logo after unhighlighting the button and disabling
            presenter_logo_group.setEnabled(false);
            unhighlightButtonClicked(presenter_logo_group);
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.hideLogo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.hideLogo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter_logo_group.setEnabled(true);
                }
            }, 800);
        }
    }
    public void blankButtonClick() {
        if (projectButton_isSelected) {
            projectButton_isSelected = false;
            unhighlightButtonClicked(presenter_project_group);
        }
        if (logoButton_isSelected) {
            logoButton_isSelected = false;
            unhighlightButtonClicked(presenter_logo_group);
        }

        blankButton_isSelected = !blankButton_isSelected;

        if (blankButton_isSelected) {
            // Fade out everything after highlighting the button and disabling
            presenter_blank_group.setEnabled(false);
            highlightButtonClicked(presenter_blank_group);
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.blankDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.blankDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter_blank_group.setEnabled(true);
                }
            }, 800);
        } else {
            // Fade back everything after unhighlighting the button and disabling
            presenter_blank_group.setEnabled(false);
            unhighlightButtonClicked(presenter_blank_group);
            if (mSelectedDevice != null) {
                try {
                    PresentationService.ExternalDisplay.unblankDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (FullscreenActivity.isHDMIConnected) {
                try {
                    PresentationServiceHDMI.unblankDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter_blank_group.setEnabled(true);
                }
            }, 800);
        }
    }
    public void alertButtonClick() {
        alertButton_isSelected = true;
        highlightButtonClicked(presenter_alert_group);
        FullscreenActivity.whattodo = "alert";
        openFragment();

        // After a short time, turn off the button
        Handler delay = new Handler();
        delay.postDelayed(new Runnable() {
            @Override
            public void run() {
                alertButton_isSelected = false;
                unhighlightButtonClicked(presenter_alert_group);
            }
        }, 500);
    }
    public void audioButtonClick() {
        audioButton_isSelected = true;
        highlightButtonClicked(presenter_audio_group);
        FullscreenActivity.whattodo = "presenter_audio";
        openFragment();

        // After a short time, turn off the button
        Handler delay = new Handler();
        delay.postDelayed(new Runnable() {
            @Override
            public void run() {
                alertButton_isSelected = false;
                unhighlightButtonClicked(presenter_audio_group);
            }
        }, 500);
    }
    public void dBButtonClick() {
        // Check audio record is allowed
        if (ActivityCompat.checkSelfPermission(PresenterMode.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Snackbar.make(mLayout, R.string.microphone_rationale, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(PresenterMode.this, new String[]{Manifest.permission.RECORD_AUDIO}, FullscreenActivity.REQUEST_MICROPHONE_CODE);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                        FullscreenActivity.REQUEST_MICROPHONE_CODE);
            }

        } else {
            dBButton_isSelected = true;
            highlightButtonClicked(presenter_dB_group);
            FullscreenActivity.whattodo = "presenter_db";
            openFragment();

            // After a short time, turn off the button
            Handler delay = new Handler();
            delay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertButton_isSelected = false;
                    unhighlightButtonClicked(presenter_dB_group);
                }
            }, 500);
        }
    }

    // Highlight or unhighlight the presenter (col 3) buttons
    @SuppressWarnings("deprecation")
    public void highlightButtonClicked(View v) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackgroundDrawable(ContextCompat.getDrawable(PresenterMode.this, R.drawable.presenter_box_blue_active));
        } else {
            v.setBackground(ContextCompat.getDrawable(PresenterMode.this, R.drawable.presenter_box_blue_active));
        }
    }
    @SuppressWarnings("deprecation")
    public void unhighlightButtonClicked(View v) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackgroundDrawable(null);
        } else {
            v.setBackground(null);
        }
    }

    // Enable or disable the buttons in the final column
    public void noSecondScreen() {
        unhighlightButtonClicked(presenter_project_group);
        unhighlightButtonClicked(presenter_logo_group);
        unhighlightButtonClicked(presenter_blank_group);
        unhighlightButtonClicked(presenter_alert_group);
        unhighlightButtonClicked(presenter_audio_group);
        unhighlightButtonClicked(presenter_dB_group);
        unhighlightButtonClicked(presenter_slide_group);
        unhighlightButtonClicked(presenter_scripture_group);
        unhighlightButtonClicked(presenter_display_group);
        /*presenter_project_group.setEnabled(false);
        presenter_logo_group.setEnabled(false);
        presenter_blank_group.setEnabled(false);
        presenter_alert_group.setEnabled(false);
        presenter_audio_group.setEnabled(false);
        presenter_dB_group.setEnabled(false);
        presenter_slide_group.setEnabled(false);
        presenter_scripture_group.setEnabled(false);
        presenter_display_group.setEnabled((false));*/
        presenter_project_group.setEnabled(true);
        presenter_logo_group.setEnabled(true);
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
        alertButton_isSelected = false;
        audioButton_isSelected = false;
        dBButton_isSelected = false;
        slideButton_isSelected = false;
        scriptureButton_isSelected = false;
        displayButton_isSelected = false;
    }
    public void isSecondScreen() {
        presenter_project_group.setEnabled(true);
        presenter_logo_group.setEnabled(true);
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
        alertButton_isSelected = false;
        audioButton_isSelected = false;
        dBButton_isSelected = false;
        slideButton_isSelected = false;
        scriptureButton_isSelected = false;
        displayButton_isSelected = false;
    }

    @Override
    public void allowPDFEditViaExternal() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        // Always use string resources for UI text.
        String title = getResources().getString(R.string.export_pdf);
        // Create intent to show chooser
        Intent chooser = Intent.createChooser(intent, title);

        // Verify the intent will resolve to at least one activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, FullscreenActivity.REQUEST_PDF_CODE);
        }
    }

    // The camera permissions and stuff
    @Override
    public void useCamera() {
        if (ContextCompat.checkSelfPermission(PresenterMode.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(PresenterMode.this, new String[]{Manifest.permission.CAMERA},
                    FullscreenActivity.REQUEST_CAMERA_CODE);
        } else {
            startCamera();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FullscreenActivity.REQUEST_CAMERA_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Success, go for it
                    startCamera();
                }
                break;
            }
        }
    }
    public void startCamera() {
        closeMyDrawers("option");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.garethevans.church.opensongtablet.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, FullscreenActivity.REQUEST_CAMERA_CODE);
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", FullscreenActivity.locale).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        FullscreenActivity.mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FullscreenActivity.REQUEST_CAMERA_CODE && resultCode == Activity.RESULT_OK) {
            FullscreenActivity.whattodo = "savecameraimage";
            openFragment();
        } else if (requestCode == FullscreenActivity.REQUEST_PDF_CODE) {
            // PDF sent back, so reload it
            loadSong();
        }
    }

    // The stuff to deal with the second screen
    @Override
    public void connectHDMI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                    MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        }
        updateDisplays();
    }

    @SuppressLint("NewApi")
    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            isSecondScreen();
            logoButton_isSelected = true;
            highlightButtonClicked(presenter_logo_group);
            updateDisplays();
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
                hdmi.dismiss();
            } catch (Exception e) {
                // Ooops
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
    public void updateDisplays() {
        // This is called when display devices are changed (connected, disconnected, etc.)
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
                        public void onServiceCreated(
                                CastRemoteDisplayLocalService service) {
                        }

                        @Override
                        public void onRemoteDisplaySessionStarted(
                                CastRemoteDisplayLocalService service) {
                        }

                        @Override
                        public void onRemoteDisplaySessionError(Status status) {
                            Log.d("d","onRemoteDisplaySessionError status="+status);
                        }

                        @Override
                        public void onRemoteDisplaySessionEnded(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {
                            Log.d("d","onRemoteDisplaySessionEnded");
                        }

                    });
        } else {
            // Might be a hdmi connection
            try {
                Display mDisplay = mMediaRouter.getSelectedRoute().getPresentationDisplay();
                if (mDisplay != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        hdmi = new PresentationServiceHDMI(PresenterMode.this, mDisplay);
                        hdmi.show();
                        isSecondScreen();
                        logoButton_isSelected = true;
                        highlightButtonClicked(presenter_logo_group);
                        FullscreenActivity.isHDMIConnected = true;
                    }
                }
            } catch (Exception e) {
               // Ooops
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (FullscreenActivity.isHDMIConnected) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                PresentationServiceHDMI.setUpLogo();
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                PresentationServiceHDMI.doUpdate();
                            }
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                PresentationServiceHDMI.updateFonts();
                            }
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                PresentationServiceHDMI.fixBackground();
                            }
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                PresentationServiceHDMI.changeMargins();
                            }
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
        if (FullscreenActivity.isPresenting && !FullscreenActivity.isHDMIConnected) {
            try {
                PresentationService.ExternalDisplay.updateAlert(ison);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (FullscreenActivity.isHDMIConnected) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    PresentationServiceHDMI.updateAlert(ison);
                }
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
        newFragment.show(getFragmentManager(), "dialog");
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        // To stop repeated pressing too quickly, set a handler to wait for 500ms before reenabling
        if (event.getAction() == KeyEvent.ACTION_UP && pedalsenabled) {
            if (keyCode == FullscreenActivity.pedal1) {
                doPedalAction(FullscreenActivity.pedal1shortaction);
            } else if (keyCode == FullscreenActivity.pedal2) {
                doPedalAction(FullscreenActivity.pedal2shortaction);
            } else if (keyCode == FullscreenActivity.pedal3) {
                doPedalAction(FullscreenActivity.pedal3shortaction);
            } else if (keyCode == FullscreenActivity.pedal4) {
                doPedalAction(FullscreenActivity.pedal4shortaction);
            } else if (keyCode == FullscreenActivity.pedal5) {
                doPedalAction(FullscreenActivity.pedal5shortaction);
            } else if (keyCode == FullscreenActivity.pedal6) {
                doPedalAction(FullscreenActivity.pedal6shortaction);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public void doPedalAction(String action) {
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
    void pausePedalUse() {
        pedalsenabled = false;
        // Close both drawers
        closeMyDrawers("both");

        Handler reenablepedal = new Handler();
        reenablepedal.postDelayed(new Runnable() {
            @Override
            public void run() {
                pedalsenabled = true;
            }
        }, 500);
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
    public void setupPageButtons(String s) {
        // Not using page buttons as FABs on the screen, so do nothing
    }
    @Override
    public void setUpPageButtonsColors() {
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

    // The pad - Not used in PresenterMode
    @Override
    public void preparePad() {}
    @Override
    public void killPad() {}
    @Override
    public void fadeoutPad() {}

    // Autoscroll - Not used in PresenterMode
    @Override
    public void stopAutoScroll() {}
    @Override
    public void startAutoScroll() {}
    @Override
    public void prepareLearnAutoScroll() {}

    // Metronome - Not used in PresenterMode
    @Override
    public void stopMetronome() {}

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

}