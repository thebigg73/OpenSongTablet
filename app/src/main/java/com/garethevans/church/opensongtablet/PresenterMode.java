package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;

import org.xmlpull.v1.XmlPullParserException;

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
        PopUpListSetsFragment.MyInterface, PopUpOptionMenuSet.MyInterface,
        PopUpLongSongPressFragment.MyInterface, PopUpProfileFragment.MyInterface,
        PopUpOptionMenuSong.MyInterface, PopUpDirectoryChooserFragment.MyInterface,
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
        SongMenuAdapter.MyInterface, BatteryMonitor.MyInterface,
        PopUpMenuSettingsFragment.MyInterface {

    DialogFragment newFragment;

    // Casting
    MediaRouter mMediaRouter;
    MediaRouteSelector mMediaRouteSelector;
    MyMediaRouterCallback mMediaRouterCallback = new MyMediaRouterCallback();
    CastDevice mSelectedDevice;

    // The toolbar and menu
    public Toolbar ab_toolbar;
    public static ActionBar ab;
    public RelativeLayout songandauthor;
    public TextView digitalclock;
    public TextView songtitle_ab;
    public TextView songkey_ab;
    public TextView songauthor_ab;
    public TextView batterycharge;
    public ImageView batteryimage;
    Menu menu;

    // AsyncTasks
    AsyncTask<Object, Void, String> preparesongmenu_async;
    AsyncTask<Object, Void, String> prepareoptionmenu_async;
    LoadSong loadsong_async;
    AsyncTask<Object, Void, String> autoslideshowtask;
    AsyncTask<Object, Void, String> sharesong_async;
    AsyncTask<Object, Void, String> load_customreusable;
    AsyncTask<Object, Void, String> add_slidetoset;
    IndexSongs.IndexMySongs indexsongs_task;
    AsyncTask<Object, Void, String> indexing_done;
    AsyncTask<Object, Void, String> open_drawers;
    AsyncTask<Object, Void, String> close_drawers;
    AsyncTask<Object, Void, String> resize_drawers;
    AsyncTask<Object, Void, String> do_moveinset;

    // The views
    LinearLayout mLayout;
    LinearLayout presenter_window;
    LinearLayout pres_details;
    TextView presenter_songtitle;
    TextView presenter_author;
    TextView presenter_copyright;
    CheckBox presenter_order_text;
    Button presenter_order_button;
    TextView presenter_set;
    LinearLayout presenter_set_buttonsListView;
    EditText presenter_lyrics;
    ImageView presenter_lyrics_image;
    LinearLayout loopandtimeLinearLayout;
    LinearLayout loopcontrolsLinearLayout;
    CheckBox loopCheckBox;
    EditText timeEditText;
    FloatingActionButton stopSlideShow;
    FloatingActionButton startSlideShow;
    ScrollView presenter_songbuttons;
    ScrollView preso_action_buttons_scroll;
    ScrollView presenter_setbuttons;
    ScrollView preso_settings_scroll;
    LinearLayout presenter_song_buttonsListView;
    LinearLayout preso_Action_buttons;

    // Quick nav buttons
    FloatingActionButton nav_prevsong;
    FloatingActionButton nav_nextsong;
    FloatingActionButton nav_prevsection;
    FloatingActionButton nav_nextsection;


    // Button for the song and set
    Button newSetButton;

    // The buttons
    TextView presenter_project_group;
    TextView presenter_logo_group;
    TextView presenter_blank_group;
    TextView presenter_alert_group;
    TextView presenter_audio_group;
    TextView presenter_dB_group;
    TextView presenter_slide_group;
    TextView presenter_scripture_group;
    TextView presenter_backgrounds_group;
    TextView presenter_layout_group;

    // The song and option menu stuff
    DrawerLayout mDrawerLayout;
    LinearLayout songmenu;
    TextView menuFolder_TextView;
    FloatingActionButton closeSongsFAB;
    LinearLayout side_index;
    ListView song_list_view;
    LinearLayout optionmenu;
    ScrollView optionsdisplayscrollview;
    LinearLayout changefolder_LinearLayout;
    boolean firstrun_option = true;
    boolean firstrun_song = true;

    // The media player
    public static MediaPlayer mp;
    public static String mpTitle = "";

    // Song and set button variables
    static int whichsonginset;
    static int whichsongsection;
    private boolean endofset = false;
    LinearLayout newSongSectionGroup;
    Button newSongButton;
    TextView newSongSectionText;
    int numsectionbuttons;
    View currentsetbutton;
    View currentsectionbutton;
    int whichviewSongSection;
    int whichviewSetSection;


    // Variables used by the popups
    static String whatBackgroundLoaded;

    // General variables
    String[] imagelocs;


    // Which Actions buttons are selected
    boolean projectButton_isSelected = false;
    boolean logoButton_isSelected = false;
    boolean blankButton_isSelected = false;
    //boolean displayButton_isSelected = false;
    boolean scriptureButton_isSelected = false;
    boolean slideButton_isSelected = false;
    boolean alertButton_isSelected = false;
    boolean audioButton_isSelected = false;
    boolean dBButton_isSelected = false;
    boolean layoutButton_isSelected = false;
    boolean backgroundButton_isSelected = false;

    // Auto slideshow
    boolean isplayingautoslideshow = false;
    int autoslidetime = 0;
    boolean autoslideloop = false;

    // TEMP STUFF FOR OLD MYPRESENTATION
    static String song_on = "N";
    static String logo_on = "Y";
    static String blackout = "N";
    static String alert_on = "N";
    public static String buttonPresentText;
    public static String presoAuthor;
    public static String presoTitle;
    public static String presoCopyright;
    static String imageAddress;
    static int tempxmargin;
    static int tempymargin;
    static int numdisplays;








/*
    boolean firsttime = true;
    private boolean isPDF;
    private boolean isSong;
    private boolean addingtoset;
    static String myAlert = FullscreenActivity.myAlert;
    imageAddressstatic boolean pedalsenabled = true;
    static String imageAddress;
    // Keep a note of what is shown
    static String background="image";
    Display variables
    Display[] presentationDisplays;
    DisplayManager displayManager;
    //Display display;
    Context context;
    String tempSongLocation;
    //int tempSetButtonId;
    String[] songpart = new String[2]
    static String[] songSections;
    static String[] songSectionsLabels;
    AsyncTask doredraw;
    AsyncTask loadsong_async;
    //Handler mHandler;
*/

    // The Network Discovery stuff


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

        // Set up the toolbar and views
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ab_toolbar = (Toolbar) findViewById(R.id.mytoolbar); // Attaching the layout to the toolbar object
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

                // Set up the page buttons
                setupSetButtons();

                // Set up the menus
                prepareSongMenu();
                prepareOptionMenu();

                // Set up the song buttons
                setupSongButtons();

                // Redraw the menu
                invalidateOptionsMenu();

                // If we have started for the first time (not redrawn)
                if (FullscreenActivity.firstload) {
                    FullscreenActivity.firstload = false;
                    rebuildSearchIndex();
                }
            }
        });

        // Setup the CastContext
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast("4E2B0891"))
                .build();

        // Load up the song
        loadSong();
    }

    public void initialiseTheViews() {

        // The main views
        mLayout = (LinearLayout) findViewById(R.id.pagepresentermode);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        presenter_window = (LinearLayout) findViewById(R.id.presenter_window);
        pres_details = (LinearLayout) findViewById(R.id.pres_details);
        presenter_songtitle = (TextView) findViewById(R.id.presenter_songtitle);
        presenter_author = (TextView) findViewById(R.id.presenter_author);
        presenter_copyright = (TextView) findViewById(R.id.presenter_copyright);
        presenter_order_text = (CheckBox) findViewById(R.id.presenter_order_text);
        presenter_order_button = (Button) findViewById(R.id.presenter_order_button);
        presenter_set = (TextView) findViewById(R.id.presenter_set);
        presenter_set_buttonsListView = (LinearLayout) findViewById(R.id.presenter_set_buttonsListView);
        presenter_lyrics = (EditText) findViewById(R.id.presenter_lyrics);
        presenter_lyrics_image = (ImageView) findViewById(R.id.presenter_lyrics_image);
        loopandtimeLinearLayout = (LinearLayout) findViewById(R.id.loopandtimeLinearLayout);
        loopcontrolsLinearLayout = (LinearLayout) findViewById(R.id.loopcontrolsLinearLayout);
        loopCheckBox = (CheckBox) findViewById(R.id.loopCheckBox);
        timeEditText = (EditText) findViewById(R.id.timeEditText);
        stopSlideShow = (FloatingActionButton) findViewById(R.id.stopSlideShow);
        startSlideShow = (FloatingActionButton) findViewById(R.id.startSlideShow);
        presenter_songbuttons = (ScrollView) findViewById(R.id.presenter_songbuttons);
        preso_Action_buttons = (LinearLayout) findViewById(R.id.preso_Action_buttons);
        preso_action_buttons_scroll = (ScrollView) findViewById(R.id.preso_action_buttons_scroll);
        presenter_setbuttons = (ScrollView) findViewById(R.id.presenter_setbuttons);
        preso_settings_scroll = (ScrollView) findViewById(R.id.preso_settings_scroll);
        presenter_song_buttonsListView = (LinearLayout) findViewById(R.id.presenter_song_buttonsListView);

        // Quick nav buttons
        nav_prevsong = (FloatingActionButton) findViewById(R.id.nav_prevsong);
        nav_nextsong = (FloatingActionButton) findViewById(R.id.nav_nextsong);
        nav_prevsection = (FloatingActionButton) findViewById(R.id.nav_prevsection);
        nav_nextsection = (FloatingActionButton) findViewById(R.id.nav_nextsection);

        // The buttons
        presenter_project_group = (TextView) findViewById(R.id.presenter_project_group);
        presenter_logo_group = (TextView) findViewById(R.id.presenter_logo_group);
        presenter_blank_group = (TextView) findViewById(R.id.presenter_blank_group);
        presenter_alert_group = (TextView) findViewById(R.id.presenter_alert_group);
        presenter_audio_group = (TextView) findViewById(R.id.presenter_audio_group);
        presenter_dB_group = (TextView) findViewById(R.id.presenter_dB_group);
        presenter_slide_group = (TextView) findViewById(R.id.presenter_slide_group);
        presenter_scripture_group = (TextView) findViewById(R.id.presenter_scripture_group);
        presenter_backgrounds_group = (TextView) findViewById(R.id.presenter_backgrounds_group);
        presenter_layout_group = (TextView) findViewById(R.id.presenter_layout_group);

        // The toolbar
        songandauthor = (RelativeLayout) findViewById(R.id.songandauthor);
        digitalclock = (TextView) findViewById(R.id.digitalclock);
        songtitle_ab = (TextView) findViewById(R.id.songtitle_ab);
        songkey_ab = (TextView) findViewById(R.id.songkey_ab);
        songauthor_ab = (TextView) findViewById(R.id.songauthor_ab);
        songtitle_ab.setText(getResources().getString(R.string.presentermode));
        songkey_ab.setText("");
        songauthor_ab.setText("");
        batterycharge = (TextView) findViewById(R.id.batterycharge);
        batteryimage = (ImageView) findViewById(R.id.batteryimage);

        // The song menu
        songmenu = (LinearLayout) findViewById(R.id.songmenu);
        menuFolder_TextView = (TextView) findViewById(R.id.menuFolder_TextView);
        closeSongsFAB = (FloatingActionButton) findViewById(R.id.closeSongsFAB);
        side_index = (LinearLayout) findViewById(R.id.side_index);
        song_list_view = (ListView) findViewById(R.id.song_list_view);

        // The option menu
        optionmenu = (LinearLayout) findViewById(R.id.optionmenu);
        optionsdisplayscrollview = (ScrollView) findViewById(R.id.optionsdisplayscrollview);
        menuFolder_TextView = (TextView) findViewById(R.id.menuFolder_TextView);
        menuFolder_TextView.setText(getString(R.string.wait));
        changefolder_LinearLayout = (LinearLayout) findViewById(R.id.changefolder_LinearLayout);
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
        stopSlideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareStopAutoSlideShow();
            }
        });
        startSlideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareStartAutoSlideShow();
            }
        });

        // Scrollbars
        presenter_set_buttonsListView.setScrollbarFadingEnabled(false);
        presenter_songbuttons.setScrollbarFadingEnabled(false);
        preso_action_buttons_scroll.setScrollbarFadingEnabled(false);

        // Hide some stuff
        presenter_lyrics.setVisibility(View.VISIBLE);
        presenter_lyrics_image.setVisibility(View.GONE);
    }
    public void screenClickListeners() {
        songandauthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "songdetails";
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
                FullscreenActivity.whattodo = "";
                openFragment();
            }
        });
        nav_prevsong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousSongInSet();
            }
        });
        nav_nextsong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSongInSet();
            }
        });
        nav_prevsection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryClickPreviousSection();
            }
        });
        nav_nextsection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryClickNextSection();
            }
        });
    }
    public void showCorrectViews() {
        // If this is a song, we can hide the loop, time, play and pause buttons

        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            // Image and PDF files replace the slide text with an image preview
            presenter_lyrics_image.setVisibility(View.VISIBLE);
            presenter_lyrics.setVisibility(View.GONE);
            loopandtimeLinearLayout.setVisibility(View.GONE);
            loopcontrolsLinearLayout.setVisibility(View.GONE);
        } else if (FullscreenActivity.isSong) {
            presenter_lyrics_image.setVisibility(View.GONE);
            presenter_lyrics.setVisibility(View.VISIBLE);
            loopandtimeLinearLayout.setVisibility(View.GONE);
            loopcontrolsLinearLayout.setVisibility(View.GONE);
        }
        if (FullscreenActivity.isImageSlide) {
            presenter_lyrics_image.setVisibility(View.VISIBLE);
            presenter_lyrics.setVisibility(View.GONE);
            loopandtimeLinearLayout.setVisibility(View.VISIBLE);
            loopcontrolsLinearLayout.setVisibility(View.VISIBLE);
        }
        if (FullscreenActivity.isSlide) {
            presenter_lyrics_image.setVisibility(View.GONE);
            presenter_lyrics.setVisibility(View.VISIBLE);
            loopandtimeLinearLayout.setVisibility(View.VISIBLE);
            loopcontrolsLinearLayout.setVisibility(View.VISIBLE);
        }

    }



    // Handlers for main page on/off/etc.
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
            mMediaRouter.removeCallback(mMediaRouterCallback);
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
        tryCancelAsyncTasks();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FullscreenActivity.orientationchanged = FullscreenActivity.mScreenOrientation != newConfig.orientation;
        //actionBarDrawerToggle.onConfigurationChanged(newConfig);
        if (FullscreenActivity.orientationchanged) {
            if (newFragment!=null && newFragment.getDialog()!=null) {
                PopUpSizeAndAlpha.decoratePopUp(PresenterMode.this,newFragment.getDialog());
            }
            // Now, reset the orientation.
            FullscreenActivity.orientationchanged = false;

            // Get the current orientation
            FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

            invalidateOptionsMenu();
            closeMyDrawers("both");
            resizeDrawers();
        }
    }

    public void tryCancelAsyncTasks() {
        doCancelAsyncTask(loadsong_async);
        doCancelAsyncTask(loadsong_async);
        doCancelAsyncTask(preparesongmenu_async);
        doCancelAsyncTask(prepareoptionmenu_async);
        doCancelAsyncTask(sharesong_async);
        doCancelAsyncTask(load_customreusable);
        doCancelAsyncTask(open_drawers);
        doCancelAsyncTask(close_drawers);
        doCancelAsyncTask(resize_drawers);
        doCancelAsyncTask(do_moveinset);
        doCancelAsyncTask(indexing_done);
        doCancelAsyncTask(add_slidetoset);
        doCancelAsyncTask(indexsongs_task);
        doCancelAsyncTask(autoslideshowtask);
    }
    public void doCancelAsyncTask(AsyncTask ast) {
        if (ast!=null) {
            try {
                ast.cancel(true);
            } catch (Exception e) {
                // OOps
            }
        }
    }


    @Override
    public void splashScreen() {
        SharedPreferences settings = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("showSplashVersion", 0);
        editor.apply();
        Intent intent = new Intent();
        intent.setClass(PresenterMode.this, SettingsActivity.class);
        startActivity(intent);
        finish();
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



    // Handlers for setting the window flags
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



    // Listeners for key presses
    @Override
    public void onBackPressed() {
        if (mp.isPlaying()) {
            // Stop the media player
            mp.stop();
            mp.reset();
            mpTitle = "";
        }
        String message = getResources().getString(R.string.exit);
        FullscreenActivity.whattodo = "exit";
        newFragment = PopUpAreYouSureFragment.newInstance(message);
        newFragment.show(getFragmentManager(), "dialog");
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



    // Interface listeners for PopUpPages
    @Override
    public void doEdit() {
        FullscreenActivity.whattodo = "editsong";
        openFragment();
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
    public boolean justSong(Context c) {
        boolean isallowed = true;
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF || !FullscreenActivity.isSong) {
            showToastMessage(c.getResources().getString(R.string.not_allowed));
            isallowed = false;
        }
        return isallowed;
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
    private class ShareSong extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute() {
            FullscreenActivity.bmScreen = null;
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            Intent emailIntent = ExportPreparer.exportSong(PresenterMode.this, FullscreenActivity.bmScreen);
            startActivityForResult(Intent.createChooser(emailIntent, getResources().getString(R.string.exportcurrentsong)), 12345);
            return null;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }
    }
    @Override
    public void openSongEdit() {
        // Not required really - just a legacy for FullscreenActivity.
        // When FullscreenActivity is emptied, change mListener in PopUpSongCreateFragment openSongEdit()
        // to openFragment() with FullscreenActivity.whattodo="editsong"
        FullscreenActivity.whattodo = "editsong";
        openFragment();
    }
    @Override
    public void backupInstall(String message) {
        // Songs have been imported, so update the song menu and rebuild the search index
        showToastMessage(message);
        prepareSongMenu();
        rebuildSearchIndex();
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
    public void showToastMessage(String message) {
        if (message != null && !message.isEmpty()) {
            FullscreenActivity.myToastMessage = message;
            ShowToast.showToast(PresenterMode.this);
        }
    }


    @Override
    // The navigation drawers
    public void resizeDrawers() {
        doCancelAsyncTask(resize_drawers);
        resize_drawers = new ResizeDrawers();
        try {
            resize_drawers.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class ResizeDrawers extends AsyncTask<Object, Void, String> {
        int width;

        @Override
        protected String doInBackground(Object... o) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = (int) ((float) metrics.widthPixels * FullscreenActivity.menuSize);
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



    // The overflow menu and actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.presenter_actions, menu);

        // Setup the menu item for connecting to cast devices
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
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
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", FullscreenActivity.locale);
        String formattedTime = df.format(c.getTime());
        digitalclock.setText(formattedTime);

        // Get battery
        int i = (int) (BatteryMonitor.getBatteryStatus(PresenterMode.this) * 100.0f);
        String charge = i + "%";
        batterycharge.setText(charge);
        int abh = ab.getHeight();
        FullscreenActivity.ab_height = abh;
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



    // Loading the song
    public void loadSong() {
        doCancelAsyncTask(loadsong_async);
        loadsong_async = new LoadSong();
        try {
            loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            // Error loading the song
        }
    }
    private class LoadSong extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            // Load up the song
            try {
                LoadXML.loadXML(PresenterMode.this);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }

            if (!FullscreenActivity.presoShowChords) {
                FullscreenActivity.myLyrics = ProcessSong.removeChordLines(FullscreenActivity.mLyrics);
            }
            FullscreenActivity.myLyrics = ProcessSong.removeCommentLines(FullscreenActivity.myLyrics);
            FullscreenActivity.showChords = false;
            FullscreenActivity.myLyrics = ProcessSong.removeUnderScores(FullscreenActivity.myLyrics, PresenterMode.this);

            // 1. Sort multiline verse/chord formats
            FullscreenActivity.myLyrics = ProcessSong.fixMultiLineFormat(FullscreenActivity.myLyrics, PresenterMode.this);

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
            for (int sl=0; sl < FullscreenActivity.songSections.length; sl++) {
                FullscreenActivity.songSectionsLabels[sl] = ProcessSong.getSectionHeadings(FullscreenActivity.songSections[sl]);
            }

            // 5. Get rid of the tag/heading lines
            FullscreenActivity.songSections =  ProcessSong.removeTagLines(FullscreenActivity.songSections);


            // We need to split each section into string arrays by line
            FullscreenActivity.sectionContents = new String[FullscreenActivity.songSections.length][];
            for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                FullscreenActivity.sectionContents[x] = FullscreenActivity.songSections[x].split("\n");
            }

            // Determine what each line type is
            // Copy the array of sectionContents into sectionLineTypes
            // Then we'll replace the content with the line type
            // This keeps the array sizes the same simply
            FullscreenActivity.sectionLineTypes = new String[FullscreenActivity.sectionContents.length][];
            for (int x = 0; x < FullscreenActivity.sectionLineTypes.length; x++) {
                FullscreenActivity.sectionLineTypes[x] = new String[FullscreenActivity.sectionContents[x].length];
                for (int y = 0; y < FullscreenActivity.sectionLineTypes[x].length; y++) {
                    FullscreenActivity.sectionLineTypes[x][y] = ProcessSong.determineLineTypes(FullscreenActivity.sectionContents[x][y]);
                    if (FullscreenActivity.sectionContents[x][y].length() > 0 && (FullscreenActivity.sectionContents[x][y].indexOf(" ") == 0 ||
                            FullscreenActivity.sectionContents[x][y].indexOf(".") == 0 || FullscreenActivity.sectionContents[x][y].indexOf(";") == 0)) {
                        FullscreenActivity.sectionContents[x][y] = FullscreenActivity.sectionContents[x][y].substring(1);
                    }
                }
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

                    // Show or hide the relevant views based on the file type
                    showCorrectViews();
                    findSongInFolders();
                    setupSongButtons();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    private class LoadCustomReusable extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... obj) {
            LoadXML.prepareLoadCustomReusable(FullscreenActivity.customreusabletoload, PresenterMode.this);
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
    private class AddSlideToSet extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Add the slide
            CustomSlide.addCustomSlide(PresenterMode.this);
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

                    invalidateOptionsMenu();
                    prepareOptionMenu();
                    closeMyDrawers("option_delayed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // The song menu and search index
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
    private class PrepareSongMenu extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            closeSongsFAB = (FloatingActionButton) findViewById(R.id.closeSongsFAB);
            closeSongsFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeMyDrawers("song");
                }
            });
        }

        @Override
        protected String doInBackground(Object... params) {
            // List all of the songs in the current folder
            ListSongFiles.getAllSongFolders();
            ListSongFiles.getAllSongFiles();
            ListSongFiles.getSongDetails(PresenterMode.this);
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
                            whattolookfor = FullscreenActivity.mSongFileNames[i];
                        } else {
                            whattolookfor = FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.mSongFileNames[i];
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
                    SongMenuAdapter.getIndexList();
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void displayIndex() {
        LinearLayout indexLayout = (LinearLayout) findViewById(R.id.side_index);
        indexLayout.removeAllViews();
        TextView textView;
        List<String> indexList = new ArrayList<>(FullscreenActivity.mapIndex.keySet());
        for (String index : indexList) {
            textView = (TextView) View.inflate(PresenterMode.this,
                    R.layout.leftmenu, null);
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
    private class IndexingDone extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
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
        closeMyDrawers("song");
    }


    @Override
    public void shuffleSongsInSet() {
        SetActions.indexSongInSet();
        newFragment = PopUpSetViewNew.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    // The option/settings menu
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
    private class PrepareOptionMenu extends AsyncTask<Object, Void, String> {

        public void onPreExecute() {
            optionmenu = (LinearLayout) findViewById(R.id.optionmenu);
            optionmenu.removeAllViews();
            optionmenu.addView(OptionMenuListeners.prepareOptionMenu(PresenterMode.this));
            if (optionmenu != null) {
                OptionMenuListeners.optionListeners(optionmenu, PresenterMode.this);
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Get the current set list
            SetActions.prepareSetList();
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
        }

    }


    // Opening and closing the navigation (song and option drawers)
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



    // Setup the buttons and listeners on the page for sets and songs
    public void setupSetButtons() {

        // Create a new button for each song in the Set
        invalidateOptionsMenu();
        SetActions.prepareSetList();

        presenter_set_buttonsListView.removeAllViews();
        if (FullscreenActivity.mSetList!=null && FullscreenActivity.mSetList.length>0) {
            for (int x = 0; x < FullscreenActivity.mSet.length; x++) {
                if (!FullscreenActivity.mSetList[x].isEmpty()) {
                    String buttonText = FullscreenActivity.mSetList[x];
                    newSetButton = new Button(PresenterMode.this);
                    newSetButton.setText(buttonText);
                    newSetButton.setBackgroundResource(R.drawable.present_section_setbutton);
                    newSetButton.setTextSize(10.0f);
                    newSetButton.setTextColor(0xffffffff);
                    newSetButton.setTransformationMethod(null);
                    newSetButton.setPadding(10, 10, 10, 10);
                    newSetButton.setMinimumHeight(0);
                    newSetButton.setMinHeight(0);
                    newSetButton.setId(x);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(5, 5, 5, 20);
                    newSetButton.setLayoutParams(params);
                    newSetButton.setOnClickListener(new setButtonClick());
                    presenter_set_buttonsListView.addView(newSetButton);
                }
            }
            selectSongButtonInSet();
        }
    }
    @SuppressWarnings("deprecation")
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
        //isImage = false;
        //isSlide = false;

        if (FullscreenActivity.whichSongFolder.contains("../Images")) {
            // Custom images so split the mUser3 field by newline.  Each value is image location
            imagelocs = FullscreenActivity.mUser3.split("\n");
            //isImage = true;
        }


        if (FullscreenActivity.songSections!=null && FullscreenActivity.songSections.length>0) {

            numsectionbuttons = FullscreenActivity.songSections.length;

            for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                String buttonText = FullscreenActivity.songSections[x];
                String sectionText = FullscreenActivity.songSectionsLabels[x];
                newSongSectionGroup = new LinearLayout(PresenterMode.this);
                newSongSectionGroup.setOrientation(LinearLayout.HORIZONTAL);
                newSongSectionGroup.setGravity(Gravity.CENTER_HORIZONTAL);
                newSongSectionText = new TextView(PresenterMode.this);
                newSongSectionText.setText(sectionText);
                newSongSectionText.setTextColor(0xffffffff);
                newSongSectionText.setTextSize(10.0f);
                newSongSectionText.setPadding(5, 5, 10, 5);

                newSongButton = new Button(PresenterMode.this);
                newSongButton.setText(buttonText.trim());
                newSongButton.setTransformationMethod(null);
                if (FullscreenActivity.isImageSlide) {
                    // By default, the image should be the not found one
                    Drawable drw = getResources().getDrawable(R.drawable.notfound);

                    File checkfile = new File(imagelocs[x]);
                    Bitmap ThumbImage;
                    Resources res;
                    BitmapDrawable bd;

                    if (checkfile.exists()) {
                        try {
                            ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagelocs[x]), 160, 120);
                            res = getResources();
                            bd = new BitmapDrawable(res, ThumbImage);
                            newSongButton.setBackground(bd);

                        } catch (Exception e) {
                            // Didn't work
                            newSongButton.setBackground(drw);
                        }
                    } else {
                        newSongButton.setBackground(drw);
                    }

                    //newSongButton.setHeight(120);
                    //newSongButton.setWidth(160);
                    newSongButton.setAlpha(0.4f);
                    newSongButton.setMaxWidth(200);
                    newSongButton.setMaxHeight(150);
                    LinearLayout.LayoutParams layoutSongButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutSongButton.width = 200;
                    layoutSongButton.height = 150;
                    newSongButton.setLayoutParams(layoutSongButton);

                } else {
                    newSongButton.setBackgroundResource(R.drawable.present_section_button);
                }

                if (FullscreenActivity.isImageSlide || FullscreenActivity.isSlide) {
                    // Make sure the time, loop and autoslideshow buttons are visible
                    loopandtimeLinearLayout.setVisibility(View.VISIBLE);
                    loopcontrolsLinearLayout.setVisibility(View.VISIBLE);
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
                    loopcontrolsLinearLayout.setVisibility(View.GONE);
                }

                newSongButton.setTextSize(10.0f);
                newSongButton.setTextColor(0xffffffff);
                newSongButton.setPadding(10, 10, 10, 10);
                newSongButton.setMinimumHeight(0);
                newSongButton.setMinHeight(0);
                newSongButton.setId(x);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 5, 5, 10);
                newSongSectionGroup.setLayoutParams(params);
                newSongButton.setOnClickListener(new sectionButtonClick());
                newSongSectionGroup.addView(newSongSectionText);
                newSongSectionGroup.addView(newSongButton);
                presenter_song_buttonsListView.addView(newSongSectionGroup);
            }
            presoAuthor = FullscreenActivity.mAuthor.toString().trim();
            presoCopyright = FullscreenActivity.mCopyright.toString().trim();
            presoTitle = FullscreenActivity.mTitle.toString().trim();

            // Select the first button if we can
            whichsongsection = 0;
            selectSectionButtonInSong(whichsongsection);
        }
    }
    public void selectSongButtonInSet() {
        // Get the index of the current song
        String tempfiletosearch;
        if (!FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            tempfiletosearch = FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename;
        } else {
            tempfiletosearch = FullscreenActivity.songfilename;
        }

        if (tempfiletosearch.contains("../Scripture/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../Scripture/_cache/","**"+getResources().getString(R.string.scripture)+"/");
        } else if (tempfiletosearch.contains("../Slides/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../Slides/_cache/","**"+getResources().getString(R.string.slide)+"/");
        } else if (tempfiletosearch.contains("../Notes/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../Notes/_cache/","**"+getResources().getString(R.string.note)+"/");
        } else if (tempfiletosearch.contains("../Variations/")) {
            tempfiletosearch = tempfiletosearch.replace("../Variations/","**"+getResources().getString(R.string.variation)+"/");
        } else if (tempfiletosearch.contains("../Images/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../Images/_cache/","**"+getResources().getString(R.string.image)+"/");
        }
        whichsonginset = -1;

        for (int sis = 0; sis < FullscreenActivity.setSize; sis++) {
            if (FullscreenActivity.mSetList[sis].equals(tempfiletosearch)) {
                Button whichsongtoclick = (Button) presenter_set_buttonsListView.findViewById(sis);
                whichsongtoclick.performClick();
                presenter_setbuttons.smoothScrollTo(0, whichsongtoclick.getTop());
                whichsongsection = 0;
                whichsonginset = sis;

                FullscreenActivity.indexSongInSet = whichsonginset;
                selectSectionButtonInSong(whichsonginset);
            }
        }
    }
    @SuppressWarnings("deprecation")
    private class setButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            // Re-enable the disabled button
            if (currentsetbutton != null) {
                Button oldbutton = (Button) currentsetbutton;
                oldbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.present_section_setbutton));
                oldbutton.setTextSize(10.0f);
                oldbutton.setTextColor(0xffffffff);
                oldbutton.setPadding(10, 10, 10, 10);
                oldbutton.setMinimumHeight(0);
                oldbutton.setMinHeight(0);
            }

            // Get button id
            whichviewSetSection = v.getId();

            // Scroll this song to the top of the list
            presenter_setbuttons.smoothScrollTo(0, v.getTop());

            // Change the background colour of this button to show it is active
            Button newbutton = (Button) v;
            newbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.present_section_setbutton_active));
            newbutton.setTextSize(10.0f);
            newbutton.setTextColor(0xff000000);
            newbutton.setPadding(10, 10, 10, 10);
            newbutton.setMinimumHeight(0);
            newbutton.setMinHeight(0);

            // Save a note of the button we've disabled, so we can re-enable it if we choose another
            currentsetbutton = v;
            whichsonginset = whichviewSetSection;
            FullscreenActivity.indexSongInSet = whichsonginset;
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.mSetList[whichsonginset];

            FullscreenActivity.linkclicked = FullscreenActivity.mSetList[whichviewSetSection];

            FullscreenActivity.setView = true;
            FullscreenActivity.indexSongInSet = whichviewSetSection;
            if (whichviewSetSection < 1) {
                FullscreenActivity.previousSongInSet = "";
            } else {
                FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[whichviewSetSection - 1];
            }
            if (whichviewSetSection == (FullscreenActivity.setSize - 1)) {
                FullscreenActivity.nextSongInSet = "";
            } else {
                FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[whichviewSetSection + 1];
            }
            invalidateOptionsMenu();

            // Call the script to get the song location.
            SetActions.getSongFileAndFolder(PresenterMode.this);
            findSongInFolders();

            // Close the drawers in case they are open
            closeMyDrawers("both");

            // Save the preferences with the new songfilename
            Preferences.savePreferences();

            // Load the song
            loadSong();

            // Set up the song section buttons
            //setupSongButtons();

            // Since the slide has been armed, but not projected, turn off the project button
            // This encourages the user to click it again to update the projector screen
            turnOffProjectButton();

            // Now select the first song section button (if it exists)
            //whichsongsection = 0;
            //selectSectionButtonInSong(whichsongsection);
        }
    }
    @SuppressWarnings("deprecation")
    private class sectionButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Re-enable the disabled button

            if (currentsectionbutton != null) {
                Button oldbutton = (Button) currentsectionbutton;
                if (FullscreenActivity.isImageSlide) {
                    oldbutton.setAlpha(0.4f);
                    oldbutton.setMaxWidth(160);
                    oldbutton.setMaxHeight(120);
                } else {
                    oldbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.present_section_button));
                    oldbutton.setTextSize(10.0f);
                    oldbutton.setTextColor(0xffffffff);
                    oldbutton.setPadding(10, 10, 10, 10);
                }
            }

            // Get button id
            whichviewSongSection = v.getId();
            whichsongsection = whichviewSongSection;

            // Scroll this song to the top of the list
            // Have to do this manually
            // Add the height of the buttons before the one wanted + margin
            int totalheight = 0;
            for (int d = 0; d < whichsongsection; d++) {
                totalheight += presenter_song_buttonsListView.findViewById(d).getHeight();
                totalheight += 10;
            }

            presenter_songbuttons.smoothScrollTo(0, totalheight);

            // If this is an image, hide the text, show the image, otherwise show the text in the slide window
            if (FullscreenActivity.isImageSlide) {
                presenter_lyrics.setVisibility(View.GONE);
                presenter_lyrics_image.setVisibility(View.VISIBLE);
                presenter_lyrics_image.setBackground(v.getBackground());
                presenter_lyrics_image.setMaxWidth(200);
                presenter_lyrics_image.setMaxHeight(150);
                LinearLayout.LayoutParams layoutSongButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutSongButton.width = 200;
                layoutSongButton.height = 150;
                presenter_lyrics_image.setLayoutParams(layoutSongButton);
                imageAddress = imagelocs[v.getId()];

            } else {
                presenter_lyrics_image.setVisibility(View.GONE);
                presenter_lyrics.setVisibility(View.VISIBLE);
                presenter_lyrics.setText(FullscreenActivity.songSections[whichviewSongSection].trim());
            }

            // Change the background colour of this button to show it is active
            Button newbutton = (Button) v;
            if (FullscreenActivity.isImageSlide) {
                newbutton.setAlpha(1.0f);
                LinearLayout.LayoutParams layoutSongButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutSongButton.width = 200;
                layoutSongButton.height = 150;
                newbutton.setLayoutParams(layoutSongButton);
                newbutton.setMaxWidth(200);
                newbutton.setMaxHeight(150);
            } else {
                newbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.present_section_button_active));
            }
            newbutton.setTextSize(10.0f);
            newbutton.setTextColor(0xff000000);
            newbutton.setPadding(10, 10, 10, 10);
            newbutton.setMinimumHeight(0);
            newbutton.setMinHeight(0);

            // Check the set buttons again
            invalidateOptionsMenu();

            // Save a note of the button we've disabled, so we can re-enable it if we choose another
            currentsectionbutton = v;

            // Since the slide has been armed, but not projected, turn off the project button
            // This encourages the user to click it again to update the projector screen
            turnOffProjectButton();

        }
    }
    @Override
    public void loadSongFromSet() {
        Preferences.savePreferences();
        // Redraw the set buttons as the user may have changed the order
        refreshAll();

        closePopUps();

        FullscreenActivity.setView = true;
        // Specify which songinset button
        whichsonginset = FullscreenActivity.indexSongInSet;
        whichsongsection = 0;

        // Select it
        Button which_song_to_click = (Button) presenter_set_buttonsListView.findViewById(whichsonginset);
        which_song_to_click.performClick();
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
    public void prepareView() {
        refreshAll();
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
        presenter_order_text.setText(FullscreenActivity.mPresentation);
        refreshAll();
    }


    // The stuff to deal with the slideshow
    public void prepareStopAutoSlideShow() {
        if (autoslideshowtask!=null) {
            try {
                autoslideshowtask.cancel(true);
                autoslideshowtask = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            isplayingautoslideshow = false;
        }
        startSlideShow.setClickable(true);
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

        if (autoslidetime>0) {
            // Start asynctask that recalls every autoslidetime
            // Once we have reached the end of the slide group we either
            // Start again (if autoslideloop)
            // Or we exit autoslideshow
            projectButtonClick(presenter_project_group);
            isplayingautoslideshow = true;
            doCancelAsyncTask(autoslideshowtask);
            autoslideshowtask = new AutoSlideShow();
            autoslideshowtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            showToastMessage(getResources().getString(R.string.bad_time));
        }
    }
    private class AutoSlideShow extends AsyncTask <Object,Void,String> {

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
                    if (whichsongsection < FullscreenActivity.songSections.length - 1 && isplayingautoslideshow) {
                        // Move to next song section
                        whichsongsection++;
                        selectSectionButtonInSong(whichsongsection);
                        prepareStopAutoSlideShow();
                        prepareStartAutoSlideShow();
                    } else if (autoslideloop && whichsongsection >= (FullscreenActivity.songSections.length - 1) && isplayingautoslideshow) {
                        // Go back to first song section
                        whichsongsection = 0;
                        selectSectionButtonInSong(whichsongsection);
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
            // Get clock time
            long start = System.currentTimeMillis();
            long end = start;
            while (end<(start+(autoslidetime*1000)) && isplayingautoslideshow) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.d("e","Error="+e);
                }
                end = System.currentTimeMillis();
            }
            return null;
        }
    }



    // Open new fragments up
    @Override
    public void openFragment() {
        Log.d("d","whattodo="+FullscreenActivity.whattodo);
        // Load the whichSongFolder in case we were browsing elsewhere
        Preferences.loadFolderName();

        // Initialise the newFragment
        newFragment = OpenFragment.openFragment(PresenterMode.this);
        String message = OpenFragment.getMessage(PresenterMode.this);

        if (newFragment != null) {
            newFragment.show(getFragmentManager(), message);
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
    public void updateCustomStorage() {
        switch (FullscreenActivity.whattodo) {
            case "customstoragefind":
                FullscreenActivity.whattodo = "managestorage";
                openFragment();
                break;
        }
    }



    // Set actions
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
    private class DoMoveInSet extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Get the appropriate song
            FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
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
                    SetActions.doMoveInSet(PresenterMode.this);

                    // Set indexSongInSet position has moved
                    invalidateOptionsMenu();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    public void selectSectionButtonInSong(int whichsongsection) {

        if (FullscreenActivity.songSections!=null && FullscreenActivity.songSections.length>0) {
            // if whichsongsection=-1 then we want to pick the first section of the previous song in set
            // Otherwise, move to the next one.
            // If we are at the end, move to the nextsonginset


            if (whichsongsection == -1) {
                FullscreenActivity.currentSection = 0;
                previousSongInSet();

            } else if (whichsongsection == 0) {
                if (presenter_song_buttonsListView.findViewById(0) != null) {
                    presenter_song_buttonsListView.findViewById(0).performClick();
                    FullscreenActivity.currentSection = 0;
                }
            } else if (whichsongsection < FullscreenActivity.songSections.length) {
                if (presenter_song_buttonsListView.findViewById(whichsongsection) != null) {
                    presenter_song_buttonsListView.findViewById(whichsongsection).performClick();
                    FullscreenActivity.currentSection = whichsongsection;
                }
            } else {
                if (FullscreenActivity.indexSongInSet < FullscreenActivity.mSetList.length - 1) {
                    FullscreenActivity.currentSection = 0;
                    nextSongInSet();
                    endofset = false;
                } else {
                    showToastMessage(getResources().getString(R.string.lastsong));
                    endofset = true;
                    logoButtonClick();
                }
            }
        }
    }
    public void previousSongInSet() {
        FullscreenActivity.indexSongInSet = whichsonginset;
        if ((FullscreenActivity.indexSongInSet - 1) >= 0) {
            FullscreenActivity.indexSongInSet -= 1;
            whichsongsection = 0;
            doMoveInSet();
        }
    }
    public void nextSongInSet() {
        FullscreenActivity.indexSongInSet = whichsonginset;
        FullscreenActivity.indexSongInSet += 1;
        whichsongsection = 0;
        doMoveInSet();
    }
    @Override
    public void removeSongFromSet(int val) {
        // Vibrate to let the user know something happened
        DoVibrate.vibrate(PresenterMode.this, 50);

        // Take away the menu item
        String tempSong = FullscreenActivity.mSetList[val];
        FullscreenActivity.mSetList[val] = "";

        FullscreenActivity.mySet = "";
        for (String aMSetList : FullscreenActivity.mSetList) {
            if (!aMSetList.isEmpty()) {
                FullscreenActivity.mySet = FullscreenActivity.mySet + "$**_" + aMSetList + "_**$";
            }
        }

        // Tell the user that the song has been removed.
        showToastMessage("\"" + tempSong + "\" "
                + getResources().getString(R.string.removedfromset));

        //Check to see if our set list is still valid
        SetActions.prepareSetList();
        prepareOptionMenu();
        invalidateOptionsMenu();

        // Save set
        Preferences.savePreferences();

        closeMyDrawers("option");
    }
    @Override
    public void changePDFPage(int page, String direction) {

    }



    // Click the buttons
    @SuppressWarnings("deprecation")
    public void logoButtonClick() {
        if (numdisplays > 0 && !blankButton_isSelected) {

            if (logoButton_isSelected && !endofset) {
                logoButton_isSelected = false;
                presenter_logo_group.setBackgroundDrawable(null);
                logo_on = "N";
                song_on = "N";
                blackout = "N";
                turnOffProjectButton();
                turnOffBlankButton();

                //MyPresentation.fadeOutLogo();
            } else {
                logoButton_isSelected = true;
                presenter_logo_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
                preso_action_buttons_scroll.smoothScrollTo(0, presenter_logo_group.getTop());
                song_on = "N";
                blackout = "N";
                logo_on = "Y";
                turnOffProjectButton();
                turnOffBlankButton();

                //MyPresentation.fadeInLogo();
            }
        }
    }
    @SuppressWarnings("deprecation")
    public void projectButtonClick(View view) {

        if (numdisplays > 0 && !blankButton_isSelected) {
            projectButton_isSelected = true;

            presenter_project_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
            preso_action_buttons_scroll.smoothScrollTo(0, presenter_project_group.getTop());

            if (!FullscreenActivity.isPDF && !FullscreenActivity.isImage) {
                buttonPresentText = presenter_lyrics.getText().toString().trim();
            } else if (!FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                buttonPresentText = "$$_IMAGE_$$";
            } else {
                buttonPresentText = "";
            }

            // Let the presenter window know we are projecting
            song_on = "Y";
            logo_on = "N";
            blackout = "N";

            // Turn off the other actions buttons as we are now projecting!
            turnOffLogoButton();
            turnOffBlankButton();

            // Update the projector
            //MyPresentation.UpDatePresentation();
        }
    }



    // Turn off the buttons
    public void turnOffProjectButton() {
        // if button is already selected, unselect it
        if (projectButton_isSelected) {
            projectButton_isSelected = false;
            turnOffButton(presenter_project_group);
        }
    }
    public void turnOffLogoButton() {
        // If button is already selected, unselect it
        if (logoButton_isSelected) {
            logoButton_isSelected = false;
            turnOffButton(presenter_logo_group);
        }
    }
    public void turnOffBlankButton() {
        // If button is already selected, unselect it
        if (blankButton_isSelected) {
            blankButton_isSelected = false;
            turnOffButton(presenter_blank_group);
        }
    }
    @SuppressWarnings("deprecation")
    public void turnOffButton(View v) {
        v.setBackgroundDrawable(null);
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
                invalidateOptionsMenu();
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
                ListSongFiles.clearAllSongs();
                refreshAll();
        }
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
        }
    }




    // The stuff to deal with the second screen
    @SuppressLint("NewApi")
    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            updateDisplays();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            teardown();
            mSelectedDevice = null;
        }

        void teardown() {
            CastRemoteDisplayLocalService.stopService();
        }

        @Override
        public void onRouteAdded(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {}

        @Override
        public void onRouteRemoved(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {}

        @Override
        public void onRouteChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {}

        @Override
        public void onRouteVolumeChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {}
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

                    }

                });
    }














    @SuppressWarnings("deprecation")
    public void blankButtonClick() {

        if (numdisplays > 0) {
            if (blankButton_isSelected) {
                blankButton_isSelected = false;
                presenter_blank_group.setBackgroundDrawable(null);
            } else {
                blankButton_isSelected = true;
                presenter_blank_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
                preso_action_buttons_scroll.smoothScrollTo(0, presenter_blank_group.getTop());
            }
            blackout = "Y";

            // Turn off the other actions buttons as we are now running the blackout!
            turnOffProjectButton();
            turnOffLogoButton();

            // Update the projector
            //MyPresentation.UpDatePresentation();
        }
    }
    @SuppressWarnings("deprecation")
    public void alertButtonClick() {

        if (numdisplays > 0 && !blankButton_isSelected) {
            alertButton_isSelected = true;

            presenter_alert_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
            preso_action_buttons_scroll.smoothScrollTo(0, presenter_alert_group.getTop());

            FullscreenActivity.whattodo = "alert";
            openFragment();

            // After a short time, turn off the button
            Handler delay = new Handler();
            delay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertButton_isSelected = false;
                    presenter_alert_group.setBackgroundDrawable(null);
                }
            }, 500);
        }
    }
    @SuppressWarnings("deprecation")
    public void slideButtonClick() {
        slideButton_isSelected = true;
        presenter_slide_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
        preso_action_buttons_scroll.smoothScrollTo(0, presenter_slide_group.getTop());

        FullscreenActivity.whattodo = "customreusable_slide";
        openFragment();

        // After a short time, turn off the button
        Handler delay = new Handler();
        delay.postDelayed(new Runnable() {
            @Override
            public void run() {
                slideButton_isSelected = false;
                presenter_slide_group.setBackgroundDrawable(null);
            }
        }, 500);
    }
    @SuppressWarnings("deprecation")
    public void scriptureButtonClick() {

        scriptureButton_isSelected = true;
        presenter_scripture_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
        preso_action_buttons_scroll.smoothScrollTo(0, presenter_scripture_group.getTop());

        FullscreenActivity.whattodo = "customreusable_scripture";
        openFragment();

        // After a short time, turn off the button
        Handler delay = new Handler();
        delay.postDelayed(new Runnable() {
            @Override
            public void run() {
                scriptureButton_isSelected = false;
                presenter_scripture_group.setBackgroundDrawable(null);
            }
        }, 500);

    }
    @SuppressWarnings("deprecation")
    public void backgroundButtonClick() {

        if (numdisplays > 0 && !blankButton_isSelected) {
            backgroundButton_isSelected = true;

            presenter_backgrounds_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_red_active));
            preso_action_buttons_scroll.smoothScrollTo(0, presenter_backgrounds_group.getTop());

            FullscreenActivity.whattodo = "presenter_background";
            openFragment();

            // After a short time, turn off the button
            Handler delay = new Handler();
            delay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    backgroundButton_isSelected = false;
                    presenter_backgrounds_group.setBackgroundDrawable(null);
                }
            }, 500);
        }
    }
    @SuppressWarnings("deprecation")
    public void layoutButtonClick() {

        if (numdisplays > 0 && !blankButton_isSelected) {
            layoutButton_isSelected = true;

            presenter_layout_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_red_active));
            preso_settings_scroll.smoothScrollTo(0, presenter_layout_group.getTop());

            FullscreenActivity.whattodo = "presenter_layout";
            openFragment();

            newFragment = PopUpLayoutFragment.newInstance();
            newFragment.show(getFragmentManager(), "dialog");

            // After a short time, turn off the button
            Handler delay = new Handler();
            delay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    layoutButton_isSelected = false;
                    presenter_layout_group.setBackgroundDrawable(null);
                }
            }, 500);
        }

    }
    @SuppressWarnings("deprecation")
    public void audioButtonClick() {

        audioButton_isSelected = true;

        presenter_audio_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
        preso_action_buttons_scroll.smoothScrollTo(0, presenter_audio_group.getTop());

        FullscreenActivity.whattodo = "presenter_audio";
        openFragment();

        // After a short time, turn off the button
        Handler delay = new Handler();
        delay.postDelayed(new Runnable() {
            @Override
            public void run() {
                alertButton_isSelected = false;
                presenter_audio_group.setBackgroundDrawable(null);
            }
        }, 500);

    }
    @SuppressWarnings("deprecation")
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

            presenter_dB_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
            preso_action_buttons_scroll.smoothScrollTo(0, presenter_dB_group.getTop());

            FullscreenActivity.whattodo = "presenter_db";
            openFragment();

            // After a short time, turn off the button
            Handler delay = new Handler();
            delay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertButton_isSelected = false;
                    presenter_dB_group.setBackgroundDrawable(null);
                }
            }, 500);
        }
    }


    public void tryClickNextSection() {
        if (whichsongsection<FullscreenActivity.songSections.length-1) {
            whichsongsection += 1;
            selectSectionButtonInSong(whichsongsection);

            if (endofset) {
                presenter_logo_group.performClick();
                preso_action_buttons_scroll.smoothScrollTo(0, presenter_logo_group.getTop());
            } else {
                presenter_project_group.performClick();
                preso_action_buttons_scroll.smoothScrollTo(0, presenter_project_group.getTop());
            }
        } else {
            Log.d("d","Last section in the song");
        }
    }
    public void tryClickPreviousSection() {
        if (whichsongsection>0) {
            whichsongsection -= 1;
            selectSectionButtonInSong(whichsongsection);
            presenter_project_group.performClick();
            preso_action_buttons_scroll.smoothScrollTo(0, presenter_project_group.getTop());
        } else {
            Log.d("d","First section in the song");
        }
    }

    public void doProject() {

        if (numdisplays > 0) {
            buttonPresentText = presenter_lyrics.getText().toString();
            blackout = "N";
            logo_on = "N";
            song_on = "Y";
            //MyPresentation.UpDatePresentation();
        }

    }







/*
    public void redrawPresenterPage() {
        // Now load the appropriate song folder as an asynctask
        // Once this is done (onpostexecute) it loads the song asynchronously
        // Then it parses them
        // Then it splits into sections
        // Then sets up the song buttons
        // Then find the song in the folder

        doredraw = new DoRedraw();
        doredraw.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
*/




/*
        ListSongFiles.listSongs();
        invalidateOptionsMenu();
        // Redraw the Lyrics View
        isPDF = false;
        File checkfile;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            checkfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
        } else {
            checkfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename);
        }
        if ((FullscreenActivity.songfilename.contains(".pdf") || FullscreenActivity.songfilename.contains(".PDF")) && checkfile.exists()) {
            // File is pdf
            isPDF = true;
            presenter_slide_text.setVisibility(View.GONE);
            presenter_slide_image.setVisibility(View.VISIBLE);
            presenter_slide_image.setBackground(getResources().getDrawable(R.drawable.unhappy_android));

        }


        if (!isPDF) {
            try {
                LoadXML.loadXML();
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            presenter_slide_text.setVisibility(View.VISIBLE);
            presenter_slide_image.setVisibility(View.GONE);

        } else {
            FullscreenActivity.mLyrics = getResources().getString(R.string.pdf_functionnotavailable);
            // Re-initialise all song tags
            LoadXML.initialiseSongTags();

            Preferences.savePreferences();
        }
        currentsectionbutton = null;
        FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
        LyricsDisplay.parseLyrics();
        PresentPrepareSong.splitSongIntoSections();
        setupSongButtons();
        findSongInFolder();

    }
*/




/*
 *//*

    }
*/


/*
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        // To stop repeated pressing too quickly, set a handler to wait for 1 sec before reenabling
        if (event.getAction() == KeyEvent.ACTION_UP && pedalsenabled) {
            if (keyCode == FullscreenActivity.pageturner_PREVIOUS || keyCode == FullscreenActivity.pageturner_DOWN) {
                pedalsenabled = false;
                // Close both drawers
                closeMyDrawers("both");

                Handler reenablepedal = new Handler();
                reenablepedal.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pedalsenabled = true;
                    }
                },500);
                whichsongsection -= 1;
                selectSectionButtonInSong();
                presenter_project_group.performClick();
                preso_action_buttons_scroll.smoothScrollTo(0, presenter_project_group.getTop());
            } else if (keyCode == FullscreenActivity.pageturner_NEXT || keyCode == FullscreenActivity.pageturner_UP) {
                pedalsenabled = false;
                // Close both drawers
                closeMyDrawers("both");

                Handler reenablepedal = new Handler();
                reenablepedal.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pedalsenabled = true;
                    }
                },500);
                whichsongsection += 1;
                selectSectionButtonInSong();

                if (endofset) {
                    presenter_logo_group.performClick();
                    preso_action_buttons_scroll.smoothScrollTo(0, presenter_logo_group.getTop());
                } else {
                    presenter_project_group.performClick();
                    preso_action_buttons_scroll.smoothScrollTo(0, presenter_project_group.getTop());
                }
            }
        }


        return super.onKeyUp(keyCode,event);
    }
*/

/*


/*
    @Override
    public void refreshAll() {
        // Hide the views we don't need
        showCorrectViews();

        // Show the toast if the message isn't blank
        if (!FullscreenActivity.myToastMessage.equals("")) {
            ShowToast.showToast(PresenterMode.this);
        }
        whichsonginset = 0;
        whichsongsection = 0;
        SetActions.prepareSetList();
        prepareSongMenu();
        prepareOptionMenu();
        */
/*
        // Expand set group
        if (FullscreenActivity.mSet!=null) {
            setupSetButtons();
        }
        setupSongButtons();
        redrawPresenterPage();
        SetActions.indexSongInSet();
        invalidateOptionsMenu();
        findSongInFolders();

        // Reopen the set or song menu if something has changed here
        if (FullscreenActivity.whattodo.equals("loadset") || FullscreenActivity.whattodo.equals("clearset")) {
            //expListViewOption.expandGroup(0);
            //mDrawerLayout.openDrawer(expListViewOption);
            presenter_set.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse));
        }
        if (FullscreenActivity.whattodo.equals("renamesong") || FullscreenActivity.whattodo.equals("createsong")) {
            findSongInFolders();
            openMyDrawers("song");
        }

        // If menus are open, close them after 1 second
        Handler closeMenus = new Handler();
        closeMenus.postDelayed(new Runnable() {
            @Override
            public void run() {
                closeMyDrawers("both");
            }
        }, 1000); // 1000ms delay
*//*

    }
*/


/*
    private class DoRedraw extends AsyncTask <Object,Void,String> {

        @Override
        protected String doInBackground(Object... params) {
            ListSongFiles.listSongs();
            return "done";
        }


        @Override
        protected void onPostExecute(String s) {
            invalidateOptionsMenu();
            // Redraw the Lyrics View
            isPDF = false;
            File checkfile;
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                checkfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
            } else {
                checkfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename);
            }
            if ((FullscreenActivity.songfilename.contains(".pdf") || FullscreenActivity.songfilename.contains(".PDF")) && checkfile.exists()) {
                // File is pdf
                isPDF = true;
                presenter_lyrics.setVisibility(View.GONE);
                presenter_lyrics_image.setVisibility(View.VISIBLE);
                presenter_lyrics_image.setBackground(getResources().getDrawable(R.drawable.unhappy_android));

            }

            loadsong_async = new LoadSongAsync();
            loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
*/

}