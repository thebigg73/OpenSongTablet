package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StageMode extends AppCompatActivity implements
        PopUpAreYouSureFragment.MyInterface, PopUpPagesFragment.MyInterface,
        PopUpEditSongFragment.MyInterface, PopUpSongDetailsFragment.MyInterface,
        PopUpPresentationOrderFragment.MyInterface, PopUpListSetsFragment.MyInterface,
        SongMenuListeners.MyInterface, OptionMenuListeners.MyInterface, MenuHandlers.MyInterface,
        SetActions.MyInterface, PopUpFullSearchFragment.MyInterface, IndexSongs.MyInterface,
        SearchView.OnQueryTextListener, PopUpSetViewNew.MyInterface,
        PopUpChooseFolderFragment.MyInterface, PopUpCustomSlideFragment.MyInterface,
        PopUpImportExternalFile.MyInterface, PopUpDirectoryChooserFragment.MyInterface,
        OnSongConvert.MyInterface, PopUpStorageFragment.MyInterface,
        PopUpSongFolderRenameFragment.MyInterface, PopUpThemeChooserFragment.MyInterface,
        PopUpProfileFragment.MyInterface, PopUpExtraInfoFragment.MyInterface,
        PopUpPageButtonsFragment.MyInterface, PopUpScalingFragment.MyInterface,
        PopUpFontsFragment.MyInterface, PopUpTransposeFragment.MyInterface,
        PopUpEditStickyFragment.MyInterface, PopUpSongRenameFragment.MyInterface,
        PopUpSongCreateFragment.MyInterface, PopUpFileChooseFragment.MyInterface,
        PopUpPadFragment.MyInterface, PopUpAutoscrollFragment.MyInterface,
        PopUpMetronomeFragment.MyInterface, PopUpChordsFragment.MyInterface,
        PopUpStickyFragment.MyInterface, PopUpLinks.MyInterface, PopUpCustomChordsFragment.MyInterface,
        PopUpQuickLaunchSetup.MyInterface, PopUpLongSongPressFragment.MyInterface,
        PopUpFindNewSongsFragment.MyInterface, PopUpGroupedPageButtonsFragment.MyInterface,
        PopUpImportExportOSBFragment.MyInterface, SalutDataCallback, SongMenuAdapter.MyInterface,
        BatteryMonitor.MyInterface, PopUpMenuSettingsFragment.MyInterface,
        PopUpLayoutFragment.MyInterface, DownloadTask.MyInterface,
        PopUpExportFragment.MyInterface, PopUpActionBarInfoFragment.MyInterface,
        PopUpCreateDrawingFragment.MyInterface, PopUpABCNotationFragment.MyInterface,
        PopUpPDFToTextFragment.MyInterface, PopUpRandomSongFragment.MyInterface {

    // The toolbar and menu
    public Toolbar ab_toolbar;
    public static ActionBar ab;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public RelativeLayout songandauthor, batteryholder;
    public TextView digitalclock, songtitle_ab, songkey_ab, songauthor_ab, batterycharge;
    public ImageView batteryimage;
    Menu menu;

    // The popup window (sticky)
    PopupWindow stickyPopUpWindow;

    // The left and right menu
    DrawerLayout mDrawerLayout;
    TextView menuFolder_TextView;
    FloatingActionButton closeSongFAB;
    LinearLayout songmenu, optionmenu, changefolder_LinearLayout;
    ScrollView optionsdisplayscrollview;
    ListView song_list_view;

    // Song sections view
    RelativeLayout mypage;
    ScrollView songscrollview;
    RelativeLayout testpane, testpane1_2, testpane2_2, testpane1_3, testpane2_3, testpane3_3;
    LinearLayout column1_1, column1_2, column2_2, column1_3, column2_3, column3_3;
    ScrollView glideimage_ScrollView;
    ImageView glideimage, highlightNotes;
    LinearLayout backingtrackProgress, playbackProgress, capoInfo, learnAutoScroll;
    TextView padcurrentTime_TextView, padTimeSeparator_TextView, padtotalTime_TextView,
            currentTime_TextView, timeSeparator_TextView, totalTime_TextView, capoinfo,
            capoinfonewkey, learnAutoScroll_TextView, learnAutoScrollTime_TextView;
    float width_scale = 0f, biggestscale_1col = 0.0f, biggestscale_2col = 0.0f, biggestscale_3col = 0.0f;
    boolean overridingfull, overridingwidth, rendercalled = false, sectionpresented = false;

    int coltouse = 1;

    // Page buttons
    FloatingActionButton setButton, padButton, autoscrollButton, metronomeButton, extraButton,
            chordButton, stickyButton, notationButton, highlightButton, pageselectButton, linkButton,
            chordButton_ungrouped, stickyButton_ungrouped, notationButton_ungrouped, highlightButton_ungrouped,
            pageselectButton_ungrouped, linkButton_ungrouped, customButton, custom1Button,
            custom2Button, custom3Button, custom4Button, custom1Button_ungrouped,
            custom2Button_ungrouped, custom3Button_ungrouped, custom4Button_ungrouped,
            scrollDownButton, scrollUpButton, setBackButton, setForwardButton;
    ScrollView extrabuttons, extrabuttons2;
    @CoordinatorLayout.DefaultBehavior(FloatingActionButtonBehaviour.class)
    CoordinatorLayout coordinator_layout;

    // Casting
    MediaRouter mMediaRouter;
    MediaRouteSelector mMediaRouteSelector;
    MyMediaRouterCallback mMediaRouterCallback = new MyMediaRouterCallback();
    CastDevice mSelectedDevice;
    PresentationServiceHDMI hdmi;

    // Dialogue fragments and stuff
    DialogFragment newFragment;

    // Gestures
    ScaleGestureDetector scaleGestureDetector;
    GestureDetector gestureDetector;

    // ASyncTask stuff
    AsyncTask<Object, Void, String> loadsong_async, preparesongview_async,
            createperformanceview1col_async, createperformanceview2col_async,
            createperformanceview3col_async, preparesongmenu_async, prepareoptionmenu_async,
            createstageview1col_async, fadeout_media1, fadeout_media2, check_storage,
            sharesong_async, shareset_async, load_customreusable, open_drawers, close_drawers,
            resize_drawers, do_moveinset, indexing_done, add_slidetoset, dualscreenwork_async,
            show_sticky,show_highlight, mtask_learnautoscroll;
    AsyncTask<Void, Void, String> resizeperformance_async, resizestage_async;
    AsyncTask<String, Integer, String> mtask_autoscroll_music;
    IndexSongs.IndexMySongs indexsongs_task;
    AsyncTask<Void, Void, Integer> prepare_pad, play_pads;
    AsyncTask<String, Integer, String> do_download;
    AsyncTask<Object, Integer, String> get_scrollheight;

    // Allow the menus to flash open to show where they are on first run
    boolean firstrun_option = true, firstrun_song = true;

    // Handlers and Runnables
    Runnable padoncheck = new Runnable() {
        @Override
        public void run() {
            getPadsOnStatus();
        }
    };
    Handler handle = new Handler();
    Handler dopadProgressTime = new Handler();
    Runnable padprogressTimeRunnable = new Runnable() {
        @Override
        public void run() {
            getPadProgress();
        }
    };
    Runnable onEverySecond = new Runnable() {
        @Override
        public void run() {
            preparePadProgress();
        }
    };
    Handler delaycheckscroll;
    Runnable checkScrollPosition;
    Handler mRestoreImmersiveModeHandler = new Handler();
    Runnable restoreImmersiveModeRunnable = new Runnable() {
        public void run() {
            restoreTransparentBars();
        }
    };
    Handler delayactionBarHide = new Handler();
    Runnable hideActionBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (ab != null && ab.isShowing()) {
                ab.hide();
            }
        }
    };

    // Network discovery / connections
    public static final String TAG = "StageMode";
    SalutMessage myMessage, mySongMessage, mySectionMessage;

    // NFC
    FileUriCallback mFileUriCallback;

    // Battery
    BroadcastReceiver br;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("d", "Welcome to Stage Mode");
        FullscreenActivity.mContext = StageMode.this;

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        // Load up the user preferences
        Preferences.loadPreferences();

        checkStorage();
        PopUpStorageFragment.setUpStoragePreferences();
        Preferences.savePreferences();

        // Set the fullscreen window flags
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                setWindowFlags();
                setWindowFlagsAdvanced();

                // Try language locale change
                SetLocale.setLocale(StageMode.this);
            }
        });

        // Load the layout and set the title
        setContentView(R.layout.stage_mode);

        // Setup the CastContext
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast("4E2B0891"))
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .build();

        // Since this mode has just been opened, force an update to the cast screen
        FullscreenActivity.forcecastupdate = true;

        // Set up the gesture detector
        scaleGestureDetector = new ScaleGestureDetector(this, new simpleOnScaleGestureListener());
        gestureDetector = new GestureDetector(new SwipeDetector());

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

                songandauthor = findViewById(R.id.songandauthor);
                digitalclock = findViewById(R.id.digitalclock);
                songtitle_ab = findViewById(R.id.songtitle_ab);
                songkey_ab = findViewById(R.id.songkey_ab);
                songauthor_ab = findViewById(R.id.songauthor_ab);
                batterycharge = findViewById(R.id.batterycharge);
                batteryimage = findViewById(R.id.batteryimage);
                batteryholder = findViewById(R.id.batteryholder);
                mypage = findViewById(R.id.mypage);
                mypage.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);

                // Set up the pad and autoscroll timing display
                capoinfo = findViewById(R.id.capoinfo);
                capoinfonewkey = findViewById(R.id.capoinfonewkey);
                learnAutoScroll = findViewById(R.id.learnAutoScroll);
                learnAutoScroll.setVisibility(View.GONE);
                learnAutoScroll_TextView = findViewById(R.id.learnAutoScroll_TextView);
                learnAutoScrollTime_TextView = findViewById(R.id.learnAutoScrollTime_TextView);
                backingtrackProgress = findViewById(R.id.backingtrackProgress);
                backingtrackProgress.setVisibility(View.GONE);
                padcurrentTime_TextView = findViewById(R.id.padcurrentTime_TextView);
                padTimeSeparator_TextView = findViewById(R.id.padTimeSeparator_TextView);
                padtotalTime_TextView = findViewById(R.id.padtotalTime_TextView);
                playbackProgress = findViewById(R.id.playbackProgress);
                playbackProgress.setVisibility(View.GONE);
                capoInfo = findViewById(R.id.capoInfo);
                capoInfo.setVisibility(View.GONE);
                currentTime_TextView = findViewById(R.id.currentTime_TextView);
                timeSeparator_TextView = findViewById(R.id.timeSeparator_TextView);
                totalTime_TextView = findViewById(R.id.totalTime_TextView);

                // Identify the views being used
                songscrollview = findViewById(R.id.songscrollview);

                glideimage_ScrollView = findViewById(R.id.glideimage_ScrollView);
                glideimage = findViewById(R.id.glideimage);
                testpane = findViewById(R.id.testpane);
                testpane1_2 = findViewById(R.id.testpane1_2);
                testpane2_2 = findViewById(R.id.testpane2_2);
                testpane1_3 = findViewById(R.id.testpane1_3);
                testpane2_3 = findViewById(R.id.testpane2_3);
                testpane3_3 = findViewById(R.id.testpane3_3);
                highlightNotes = findViewById(R.id.highlightNotes);

                songscrollview.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);

                // Enable the song and author section to link to edit song
                songandauthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (FullscreenActivity.isPDF) {
                            FullscreenActivity.whattodo = "extractPDF";
                            openFragment();
                        } else if (FullscreenActivity.isSong){
                            FullscreenActivity.whattodo = "songdetails";
                            openFragment();
                        }
                    }
                });
                batteryholder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FullscreenActivity.whattodo = "actionbarinfo";
                        openFragment();
                    }
                });
                
                // Set up the navigation drawer
                mDrawerLayout = findViewById(R.id.drawer_layout);
                songmenu = findViewById(R.id.songmenu);
                optionmenu = findViewById(R.id.optionmenu);
                song_list_view = findViewById(R.id.song_list_view);
                closeSongFAB = findViewById(R.id.closeSongsFAB);
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
                closeSongFAB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeMyDrawers("song");
                    }
                });


                // Battery monitor
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                br = new BatteryMonitor();
                StageMode.this.registerReceiver(br, filter);

                // Make the drawers match half the width of the screen
                resizeDrawers();

                // Prepare the song menu
                prepareSongMenu();

                // Prepare the option menu
                prepareOptionMenu();

                // Set up the page buttons
                setupPageButtons(null);

                // Load the song and get started
                loadSong();

                // Prepare the scrollbuttons
                scrollButtons();

                // Prepare abhide listener
                setupAbHide();

                // Set up the Salut service
                getBluetoothName();
                startRegistration();

                // If we have started for the first time (not redrawn)
                if (FullscreenActivity.firstload) {
                    FullscreenActivity.firstload = false;
                    rebuildSearchIndex();
                }

                try {
                    FullscreenActivity.incomingfile = getIntent();
                    Intent intent = getIntent();
                    String action = intent.getAction();
                    String type = intent.getType();

                    Log.d("d","intent="+intent);
                    Log.d("d","action="+action);
                    Log.d("d","type="+type);

                    if (FullscreenActivity.incomingfile != null) {
                        Log.d("d","this is in oncreate and intent has been found");
                        Log.d("d","incomingfile="+FullscreenActivity.incomingfile);
                        Log.d("d","incomingfile.getData()="+FullscreenActivity.incomingfile.getData());
                        if (FullscreenActivity.incomingfile!=null && FullscreenActivity.incomingfile.getData()!=null) {
                            FullscreenActivity.file_location = FullscreenActivity.incomingfile.getData().getPath();
                            Log.d("d","file_location="+FullscreenActivity.file_location);
                            FullscreenActivity.file_name = FullscreenActivity.incomingfile.getData().getLastPathSegment();
                            Log.d("d","file_name="+FullscreenActivity.file_name);
                            FullscreenActivity.file_uri = FullscreenActivity.incomingfile.getData();
                            Log.d("d","file_uri="+FullscreenActivity.file_uri);

                        } else {
                            FullscreenActivity.file_location = "";
                            FullscreenActivity.file_name = "";
                            FullscreenActivity.file_uri = null;
                        }

                        // Check if file_uri exists
                        if (FullscreenActivity.file_location!=null) {
                            File t = new File(FullscreenActivity.file_location);
                            boolean uri_exists = t.exists();
                            if (FullscreenActivity.file_name.endsWith(".osb") && uri_exists) {
                                FullscreenActivity.whattodo = "processimportosb";
                                openFragment();
                            } else if (uri_exists){
                                FullscreenActivity.whattodo = "doimport";
                                openFragment();
                            }

                        }
                    }
                } catch (Exception e) {
                    // No file
                    //needtoimport = false;
                }

                // Set up stuff for NFC transfer (if allowed)
                if (FullscreenActivity.mAndroidBeamAvailable) {
                    FullscreenActivity.mNfcAdapter = NfcAdapter.getDefaultAdapter(StageMode.this);
                    mFileUriCallback = new FileUriCallback();
                    // Set the dynamic callback for URI requests.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        FullscreenActivity.mNfcAdapter.setBeamPushUrisCallback(mFileUriCallback,StageMode.this);
                    }
                }

                // Initialise the ab info
                adjustABInfo();
            }
        });
     }

    public void getBluetoothName() {
        try {
            if(FullscreenActivity.mBluetoothAdapter == null){
                FullscreenActivity.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            FullscreenActivity.mBluetoothName = FullscreenActivity.mBluetoothAdapter.getName();
            if(FullscreenActivity.mBluetoothName == null){
                FullscreenActivity.mBluetoothName = "Unknown";
            }
        } catch (Exception e) {
            FullscreenActivity.mBluetoothName = "Unknown";
        }
    }

    public void startRegistration() {
        try {
            FullscreenActivity.dataReceiver = new SalutDataReceiver(StageMode.this, StageMode.this);
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

    @SuppressWarnings("deprecation")
    public void setupAbHide() {
        // What happens when the navigation drawers are opened
        actionBarDrawerToggle = new ActionBarDrawerToggle(StageMode.this, mDrawerLayout, ab_toolbar, R.string.drawer_open, R.string.drawer_close) {
            // Called when a drawer has settled in a completely closed state.
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // Set a runnable to re-enable swipe
                Handler allowswipe = new Handler();
                allowswipe.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.tempswipeSet = "enable"; // enable swipe after short delay
                    }
                }, FullscreenActivity.delayswipe_time); // 1800ms delay
                hideActionBar();
                setupPageButtons("");
            }

            // Called when a drawer has settled in a completely open state.
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                FullscreenActivity.tempswipeSet = "disable";
                FullscreenActivity.wasscrolling = false;
                FullscreenActivity.scrollbutton = false;
                hideActionBar();

                if (!ab.isShowing()) {
                    ab.show();
                }
            }
        };

        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                restoreTransparentBars();
            }
        });

        decorView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                restoreTransparentBars();
            }
        });

    }

    @Override
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

        Runnable testnavbar = new Runnable() {
            @Override
            public void run() {
                getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        restoreTransparentBars();
                    }
                });

                getWindow().getDecorView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        restoreTransparentBars();
                    }
                });
            }
        };

        Handler waitandtest = new Handler();
        waitandtest.postDelayed(testnavbar, 1000);
    }

    public void restoreTranslucentBarsDelayed() {
        // we restore it now and after 500 ms!
        restoreTransparentBars();
        mRestoreImmersiveModeHandler.postDelayed(restoreImmersiveModeRunnable, 500);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            // Capable of dual head presentations
            FullscreenActivity.dualDisplayCapable = FullscreenActivity.currentapiVersion >= 17;
            setWindowFlags();
            setWindowFlagsAdvanced();
            restoreTranslucentBarsDelayed();
        }
    }

    public void restoreTransparentBars() {
        // Set runnable
        Runnable delhide = new Runnable() {
            @Override
            public void run() {
                // Hide them
                setWindowFlags();
                setWindowFlagsAdvanced();
                View rf = getCurrentFocus();
                if (rf!=null) {
                    rf.clearFocus();
                }
            }
        };

        // Wait for 1000ms then check for Navigation bar visibility
        // If it is there, hide it
        Handler delayhidehandler = new Handler();
        delayhidehandler.postDelayed(delhide, 1000);
    }

    private void toggleActionBar() {
        if (delayactionBarHide != null && hideActionBarRunnable != null) {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        }
        if (ab != null) {
            if (FullscreenActivity.wasscrolling || FullscreenActivity.scrollbutton) {
                if (FullscreenActivity.hideActionBar && !songmenu.isFocused() && !songmenu.isShown() && !optionmenu.isFocused() && !optionmenu.isShown()) {
                    ab.hide();
                }
            } else if (!songmenu.isFocused() && !songmenu.isShown() && !optionmenu.isFocused() && !optionmenu.isShown()) {
                if (ab.isShowing() && FullscreenActivity.hideActionBar) {
                    delayactionBarHide.postDelayed(hideActionBarRunnable, 500);
                    FullscreenActivity.actionbarbutton = false;
                } else {
                    ab.show();
                    // Set a runnable to hide it after 3 seconds
                    if (FullscreenActivity.hideActionBar) {
                        delayactionBarHide.postDelayed(hideActionBarRunnable, 3000);
                    }
                }
            }
        }
    }

    @Override
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
            int i = (int) (BatteryMonitor.getBatteryStatus(StageMode.this) * 100.0f);
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
                BitmapDrawable bmp = BatteryMonitor.batteryImage(i, abh, StageMode.this);
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

    public void checkStorage() {
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

    @Override
    protected void onNewIntent (Intent intent) {
        // This is used to listen for stuff being imported if the app is already open
        FullscreenActivity.whattodo = "";
        try {
            FullscreenActivity.file_location = intent.getData().getPath();
            FullscreenActivity.file_name = intent.getData().getLastPathSegment();
            FullscreenActivity.file_uri = intent.getData();
            if (FullscreenActivity.file_name.endsWith(".osb")) {
                FullscreenActivity.whattodo = "processimportosb";
            } else {
                FullscreenActivity.whattodo = "doimport";
            }
            openFragment();
        } catch (Exception e) {
            // No file
            //needtoimport = false;
        }
    }
    @Override
    public void onDataReceived(Object data) {
        // Attempt to extract the song details
        if (data!=null && (data.toString().contains("_____") || data.toString().contains("<lyrics>") ||
                data.toString().contains("___section___"))) {
            int mysection = ProcessSong.getSalutReceivedSection(data.toString());
            if (mysection>0) {
                holdBeforeLoadingSection(mysection);
            } else {
                String action = ProcessSong.getSalutReceivedLocation(data.toString(), StageMode.this);
                switch (action) {
                    case "Location":
                        holdBeforeLoading();
                        break;
                    case "HostFile":
                        holdBeforeLoadingXML();
                        break;
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckStorage extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (ActivityCompat.checkSelfPermission(StageMode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    finish();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stage_actions, menu);

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
        MenuHandlers.forceOverFlow(StageMode.this, ab, menu);

        // Set up battery monitor
        setUpBatteryMonitor();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuHandlers.actOnClicks(StageMode.this, item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        String message = getResources().getString(R.string.exit);
        FullscreenActivity.whattodo = "exit";
        newFragment = PopUpAreYouSureFragment.newInstance(message);
        newFragment.show(getFragmentManager(), "dialog");
    }

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
    public void onStop() {
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
                StageMode.this.unregisterReceiver(br);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tryCancelAsyncTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make the drawers match half the width of the screen
        resizeDrawers();
        // Fix the page flags
        setWindowFlags();
        setWindowFlagsAdvanced();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FullscreenActivity.whichPad = 0;
        killPad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Battery monitor
        if (br!=null) {
            try {
                StageMode.this.unregisterReceiver(br);
            } catch (Exception e) {
                Log.d("d","Battery monitor not registered anymore");
            }
        }
        tryCancelAsyncTasks();
        if (songscrollview !=null) {
            songscrollview.removeAllViews();
        }

        if (FullscreenActivity.network!=null && FullscreenActivity.network.isRunningAsHost) {
            try {
                FullscreenActivity.network.stopNetworkService(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (FullscreenActivity.network!=null) {
            try {
            FullscreenActivity.network.unregisterClient(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void tryCancelAsyncTasks() {
        doCancelAsyncTask(loadsong_async);
        doCancelAsyncTask(preparesongview_async);
        doCancelAsyncTask(createperformanceview1col_async);
        doCancelAsyncTask(createperformanceview2col_async);
        doCancelAsyncTask(createperformanceview3col_async);
        doCancelAsyncTask(preparesongmenu_async);
        doCancelAsyncTask(prepareoptionmenu_async);
        doCancelAsyncTask(resizeperformance_async);
        doCancelAsyncTask(resizestage_async);
        doCancelAsyncTask(createstageview1col_async);
        doCancelAsyncTask(fadeout_media1);
        doCancelAsyncTask(fadeout_media2);
        doCancelAsyncTask(mtask_learnautoscroll);
        doCancelAsyncTask(mtask_autoscroll_music);
        doCancelAsyncTask(check_storage);
        doCancelAsyncTask(sharesong_async);
        doCancelAsyncTask(shareset_async);
        doCancelAsyncTask(load_customreusable);
        doCancelAsyncTask(open_drawers);
        doCancelAsyncTask(close_drawers);
        doCancelAsyncTask(resize_drawers);
        doCancelAsyncTask(do_moveinset);
        doCancelAsyncTask(indexing_done);
        doCancelAsyncTask(add_slidetoset);
        doCancelAsyncTask(dualscreenwork_async);
        doCancelAsyncTask(indexsongs_task);
        doCancelAsyncTask(prepare_pad);
        doCancelAsyncTask(play_pads);
        doCancelAsyncTask(do_download);
        doCancelAsyncTask(show_sticky);
        doCancelAsyncTask(show_highlight);
        doCancelAsyncTask(get_scrollheight);
    }
    public void doCancelAsyncTask(AsyncTask ast) {
        try {
            if (ast!=null) {
                ast.cancel(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FullscreenActivity.orientationchanged = FullscreenActivity.mScreenOrientation != newConfig.orientation;

        if (FullscreenActivity.orientationchanged) {
            if (newFragment!=null && newFragment.getDialog()!=null) {
                PopUpSizeAndAlpha.decoratePopUp(StageMode.this,newFragment.getDialog());
            }

            // Get the current orientation
            FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

            closeMyDrawers("both");
            resizeDrawers();
            loadSong();
        }
    }

    public void sendSongLocationToConnected() {
        String messageString = FullscreenActivity.whichSongFolder + "_____" +
                FullscreenActivity.songfilename + "_____" +
                FullscreenActivity.whichDirection;

        myMessage = new SalutMessage();
        myMessage.description = messageString;
        holdBeforeSending();
    }

    public void sendSongXMLToConnected() {
        String myXML;
        if (FullscreenActivity.isSong && FullscreenActivity.myXML!=null) {
            myXML = FullscreenActivity.myXML;
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

    @Override
    public void shareSong() {
        if (justSong(StageMode.this)) {
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
        protected void onPreExecute() {
            try {
                songscrollview.destroyDrawingCache();
                songscrollview.setDrawingCacheEnabled(true);
                songscrollview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
                FullscreenActivity.bmScreen = null;
                FullscreenActivity.bmScreen = songscrollview.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            try {
                Intent emailIntent = ExportPreparer.exportSong(StageMode.this, FullscreenActivity.bmScreen);
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
                Intent emailIntent = ExportPreparer.exportSet(StageMode.this);
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
        if (FullscreenActivity.whattodo.equals("customise_exportsong")) {
            shareSong();
        } else {
            shareSet();
        }
    }

    @Override
    public void loadCustomReusable() {
        if (load_customreusable!=null) {
            load_customreusable.cancel(true);
        }

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
                LoadXML.prepareLoadCustomReusable(FullscreenActivity.customreusabletoload, StageMode.this);
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
    public void loadSongFromSet() {
        loadSong();
    }

    @Override
    public void shuffleSongsInSet() {
        SetActions.indexSongInSet();
        newFragment = PopUpSetViewNew.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void refreshAll() {
        // Show the toast if the message isn't blank
        if (!FullscreenActivity.myToastMessage.equals("")) {
            ShowToast.showToast(StageMode.this);
        }
        mypage.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
        songscrollview.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
        SetTypeFace.setTypeface();
        prepareSongMenu();
        prepareOptionMenu();
        loadSong();
    }

    @Override
    public void closePopUps() {
        if (newFragment != null && newFragment.isVisible()) {
            try {
                newFragment.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void scrollButtons() {
        delaycheckscroll = new Handler();
        checkScrollPosition = new Runnable() {
            @Override
            public void run() {
                FullscreenActivity.newPosFloat = songscrollview.getScrollY();

                if (checkCanScrollDown()) {
                    scrollDownButton.setVisibility(View.VISIBLE);
                } else {
                    scrollDownButton.setVisibility(View.INVISIBLE);
                }

                if (checkCanScrollUp()) {
                    scrollUpButton.setVisibility(View.VISIBLE);
                } else {
                    scrollUpButton.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    @Override
    public void setupPageButtons(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                coordinator_layout = findViewById(R.id.coordinator_layout);

                setButton = findViewById(R.id.setButton);
                padButton = findViewById(R.id.padButton);
                autoscrollButton = findViewById(R.id.autoscrollButton);
                metronomeButton = findViewById(R.id.metronomeButton);
                extraButton = findViewById(R.id.extraButton);
                chordButton = findViewById(R.id.chordButton);
                stickyButton = findViewById(R.id.stickyButton);
                notationButton = findViewById(R.id.notationButton);
                highlightButton = findViewById(R.id.highlightButton);
                pageselectButton = findViewById(R.id.pageselectButton);
                linkButton = findViewById(R.id.linkButton);
                chordButton_ungrouped = findViewById(R.id.chordButton_ungrouped);
                stickyButton_ungrouped = findViewById(R.id.stickyButton_ungrouped);
                notationButton_ungrouped = findViewById(R.id.notationButton_ungrouped);
                highlightButton_ungrouped = findViewById(R.id.highlightButton_ungrouped);
                pageselectButton_ungrouped = findViewById(R.id.pageselectButton_ungrouped);
                linkButton_ungrouped = findViewById(R.id.linkButton_ungrouped);
                customButton = findViewById(R.id.customButton);
                custom1Button = findViewById(R.id.custom1Button);
                custom2Button = findViewById(R.id.custom2Button);
                custom3Button = findViewById(R.id.custom3Button);
                custom4Button = findViewById(R.id.custom4Button);
                custom1Button_ungrouped = findViewById(R.id.custom1Button_ungrouped);
                custom2Button_ungrouped = findViewById(R.id.custom2Button_ungrouped);
                custom3Button_ungrouped = findViewById(R.id.custom3Button_ungrouped);
                custom4Button_ungrouped = findViewById(R.id.custom4Button_ungrouped);
                extrabuttons = findViewById(R.id.extrabuttons);
                extrabuttons.setVisibility(View.GONE);
                extrabuttons2 = findViewById(R.id.extrabuttons2);
                extrabuttons2.setVisibility(View.GONE);
                scrollDownButton = findViewById(R.id.scrollDownButton);
                scrollUpButton = findViewById(R.id.scrollUpButton);
                setBackButton = findViewById(R.id.setBackButton);
                setForwardButton = findViewById(R.id.setForwardButton);
                setUpPageButtonsColors();
                setupQuickLaunchButtons();

            }
        });


        // Set the alphas
        pageButtonAlpha("");

        // Decide if we are grouping / tidying page buttons
        groupPageButtons();

        // Set the sizes

        // Set the listeners
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(setButton,StageMode.this);
                FullscreenActivity.whattodo = "editset";
                openFragment();
            }
        });
        padButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(padButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_pad";
                    openFragment();
                }
            }
        });
        padButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(padButton,StageMode.this);
                gesture6();
                return true;
            }
        });
        autoscrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(autoscrollButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_autoscroll";
                    openFragment();
                }
            }
        });
        autoscrollButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(autoscrollButton,StageMode.this);
                gesture5();
                return true;
            }
        });
        metronomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(metronomeButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_metronome";
                    openFragment();
                }
            }
        });
        metronomeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(metronomeButton,StageMode.this);
                gesture7();
                return true;
            }
        });
        highlightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(highlightButton,StageMode.this);
                FullscreenActivity.highlightOn = !FullscreenActivity.highlightOn;
                FullscreenActivity.whattodo = "page_highlight";
                displayHighlight(false);
            }
        });
        highlightButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(highlightButton,StageMode.this);
                // Vibrate to let the user know something happened
                DoVibrate.vibrate(StageMode.this, 50);
                takeScreenShot();
                if (FullscreenActivity.bmScreen!=null) {
                    FullscreenActivity.whattodo = "drawnotes";
                    openFragment();
                } else {
                    Log.d("d","screenshot is null");
                }
                return true;
            }
        });
        highlightButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(highlightButton_ungrouped,StageMode.this);
                FullscreenActivity.whattodo = "page_highlight";
                FullscreenActivity.highlightOn = !FullscreenActivity.highlightOn;
                displayHighlight(false);
            }
        });
        highlightButton_ungrouped.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFABLong(highlightButton_ungrouped,StageMode.this);
                // Vibrate to let the user know something happened
                DoVibrate.vibrate(StageMode.this, 50);
                takeScreenShot();
                if (FullscreenActivity.bmScreen!=null) {
                    FullscreenActivity.whattodo = "drawnotes";
                    openFragment();
                } else {
                    Log.d("d","screenshot is null");
                }
                return true;
            }
        });
        extraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(extraButton,StageMode.this);
                if (extrabuttons!=null && extrabuttons.getVisibility() == View.GONE) {
                    pageButtonAlpha("extra");
                } else {
                    pageButtonAlpha("");
                }
            }
        });
        chordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(chordButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_chords";
                    openFragment();
                }
            }
        });
        chordButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(chordButton_ungrouped,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_chords";
                    openFragment();
                }
            }
        });
        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(linkButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_links";
                    openFragment();
                }
            }
        });
        linkButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(linkButton_ungrouped,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_links";
                    openFragment();
                }
            }
        });
        stickyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(stickyButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_sticky";
                    displaySticky();
                }
            }
        });
        stickyButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFAB(stickyButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_sticky";
                    openFragment();
                }
                return true;
            }
        });
        stickyButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(stickyButton_ungrouped,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_sticky";
                    displaySticky();
                }
            }
        });
        stickyButton_ungrouped.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFAB(stickyButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "page_sticky";
                    openFragment();
                }
                return true;
            }
        });
        notationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(notationButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    if (FullscreenActivity.mNotation.equals("")) {
                        FullscreenActivity.whattodo = "abcnotation_edit";
                    } else {
                        FullscreenActivity.whattodo = "abcnotation";
                    }
                    openFragment();
                }
            }
        });
        notationButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFAB(notationButton,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "abcnotation_edit";
                    openFragment();
                }
                return true;
            }
        });
        notationButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(notationButton_ungrouped,StageMode.this);
                if (justSong(StageMode.this)) {
                    if (FullscreenActivity.mNotation.equals("")) {
                        FullscreenActivity.whattodo = "abcnotation_edit";
                    } else {
                        FullscreenActivity.whattodo = "abcnotation";
                    }
                    openFragment();
                }
            }
        });
        notationButton_ungrouped.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CustomAnimations.animateFAB(notationButton_ungrouped,StageMode.this);
                if (justSong(StageMode.this)) {
                    FullscreenActivity.whattodo = "abcnotation_edit";
                    openFragment();
                }
                return true;
            }
        });
        pageselectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(pageselectButton,StageMode.this);
                if (FullscreenActivity.isPDF) {
                    FullscreenActivity.whattodo = "page_pageselect";
                    openFragment();
                } else {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
                    ShowToast.showToast(StageMode.this);
                }
            }
        });
        pageselectButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(pageselectButton_ungrouped,StageMode.this);
                if (FullscreenActivity.isPDF) {
                    FullscreenActivity.whattodo = "page_pageselect";
                    openFragment();
                } else {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
                    ShowToast.showToast(StageMode.this);
                }
            }
        });
        if (FullscreenActivity.grouppagebuttons) {
            customButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(customButton,StageMode.this);
                    FullscreenActivity.whattodo = "groupedpagebuttons";
                    openFragment();
                }
            });
        } else {
            customButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(customButton,StageMode.this);
                    if (extrabuttons2!=null && extrabuttons2.getVisibility() == View.GONE) {
                        pageButtonAlpha("custom");
                    } else {
                        pageButtonAlpha("");
                    }
                    }
            });
        }
        scrollUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomAnimations.animateFAB(scrollUpButton,StageMode.this);
                doScrollUp();
            }
        });
        scrollDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomAnimations.animateFAB(scrollDownButton,StageMode.this);
                doScrollDown();
            }
        });
        setForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(setForwardButton,StageMode.this);
                FullscreenActivity.setMoveDirection = "forward";
                FullscreenActivity.whichDirection = "R2L";
                goToNextItem();
            }
        });
        setBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(setBackButton,StageMode.this);
                FullscreenActivity.setMoveDirection = "back";
                FullscreenActivity.whichDirection = "L2R";
                goToPreviousItem();
            }
        });
    }

    @Override
    public void setUpPageButtonsColors() {
        setButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        padButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        autoscrollButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        metronomeButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        extraButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        chordButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        linkButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        pageselectButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        customButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        custom1Button.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        custom2Button.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        custom3Button.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        custom4Button.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        scrollDownButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        scrollUpButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        setBackButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        setForwardButton.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
    }

    public void onScrollAction() {
        // Reshow the scroll arrows if needed
        scrollButtons();
        delaycheckscroll.post(checkScrollPosition);
    }

    public void doScrollUp() {
        // Scroll the screen up

        if (FullscreenActivity.whichMode.equals("Stage")) {
            try {
                FullscreenActivity.currentSection -= 1;
                selectSection(FullscreenActivity.currentSection);
            } catch (Exception e) {
                FullscreenActivity.currentSection += 1;
                e.printStackTrace();
            }
            scrollButtons();
        } else {
            FullscreenActivity.wasscrolling = true;
            FullscreenActivity.scrollbutton = true;

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int barheight = 0;
            if (ab != null) {
                if (ab.isShowing()) {
                    barheight = ab.getHeight();
                }
            }

            ObjectAnimator animator;

            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY() - (int) (FullscreenActivity.scrollDistance * (metrics.heightPixels - barheight));
                animator = ObjectAnimator.ofInt(glideimage_ScrollView, "scrollY", glideimage_ScrollView.getScrollY(), (int) FullscreenActivity.newPosFloat);
            } else {
                FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() - (int) (FullscreenActivity.scrollDistance * (metrics.heightPixels - barheight));
                animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
            }

            Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
            animator.setInterpolator(customInterpolator);
            animator.setDuration(FullscreenActivity.scrollSpeed);
            animator.start();

            // Set a runnable to check the scroll position after 1 second
            delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
            hideActionBar();
        }
    }

    public void doScrollDown() {

        if (FullscreenActivity.whichMode.equals("Stage")) {
            try {
                FullscreenActivity.currentSection += 1;
                selectSection(FullscreenActivity.currentSection);

            } catch (Exception e) {
                FullscreenActivity.currentSection -= 1;
            }
            scrollButtons();
        } else {
            // Scroll the screen down
            FullscreenActivity.wasscrolling = true;
            FullscreenActivity.scrollbutton = true;

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);


            ObjectAnimator animator;

            int barheight = 0;
            if (ab != null) {
                if (ab.isShowing()) {
                    barheight = ab.getHeight();
                }
            }

            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY() + (int) (FullscreenActivity.scrollDistance * (metrics.heightPixels - barheight));
                animator = ObjectAnimator.ofInt(glideimage_ScrollView, "scrollY", glideimage_ScrollView.getScrollY(), (int) FullscreenActivity.newPosFloat);
            } else {
                FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() + (int) (FullscreenActivity.scrollDistance * (metrics.heightPixels - barheight));
                animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
            }

            Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
            animator.setInterpolator(customInterpolator);
            animator.setDuration(FullscreenActivity.scrollSpeed);
            animator.start();

            // Set a runnable to check the scroll position after 1 second
            delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
            hideActionBar();
        }
    }

    public void groupPageButtons() {
        extrabuttons.setVisibility(View.GONE);
        extrabuttons2.setVisibility(View.GONE);
        if (FullscreenActivity.grouppagebuttons) {
            setButton.setVisibility(View.GONE);
            padButton.setVisibility(View.GONE);
            autoscrollButton.setVisibility(View.GONE);
            metronomeButton.setVisibility(View.GONE);
            extraButton.setVisibility(View.GONE);
            chordButton.setVisibility(View.GONE);
            linkButton.setVisibility(View.GONE);
            stickyButton.setVisibility(View.GONE);
            notationButton.setVisibility(View.GONE);
            highlightButton.setVisibility(View.GONE);
            pageselectButton.setVisibility(View.GONE);
            chordButton_ungrouped.setVisibility(View.GONE);
            stickyButton_ungrouped.setVisibility(View.GONE);
            notationButton_ungrouped.setVisibility(View.GONE);
            highlightButton_ungrouped.setVisibility(View.GONE);
            pageselectButton_ungrouped.setVisibility(View.GONE);
            linkButton_ungrouped.setVisibility(View.GONE);
            customButton.setVisibility(View.VISIBLE);
            custom1Button.setVisibility(View.GONE);
            custom2Button.setVisibility(View.GONE);
            custom3Button.setVisibility(View.GONE);
            custom4Button.setVisibility(View.GONE);
            custom1Button_ungrouped.setVisibility(View.GONE);
            custom2Button_ungrouped.setVisibility(View.GONE);
            custom3Button_ungrouped.setVisibility(View.GONE);
            custom4Button_ungrouped.setVisibility(View.GONE);
        } else {
            setButton.setVisibility(View.VISIBLE);
            padButton.setVisibility(View.VISIBLE);
            autoscrollButton.setVisibility(View.VISIBLE);
            metronomeButton.setVisibility(View.VISIBLE);
            extraButton.setVisibility(View.VISIBLE);
            chordButton.setVisibility(View.VISIBLE);
            linkButton.setVisibility(View.VISIBLE);
            stickyButton.setVisibility(View.VISIBLE);
            notationButton.setVisibility(View.VISIBLE);
            highlightButton.setVisibility(View.VISIBLE);
            pageselectButton.setVisibility(View.VISIBLE);
            chordButton_ungrouped.setVisibility(View.VISIBLE);
            stickyButton_ungrouped.setVisibility(View.VISIBLE);
            notationButton_ungrouped.setVisibility(View.VISIBLE);
            highlightButton_ungrouped.setVisibility(View.VISIBLE);
            pageselectButton_ungrouped.setVisibility(View.VISIBLE);
            linkButton_ungrouped.setVisibility(View.VISIBLE);
            customButton.setVisibility(View.VISIBLE);
            custom1Button.setVisibility(View.VISIBLE);
            custom2Button.setVisibility(View.VISIBLE);
            custom3Button.setVisibility(View.VISIBLE);
            custom4Button.setVisibility(View.VISIBLE);
            custom1Button_ungrouped.setVisibility(View.VISIBLE);
            custom2Button_ungrouped.setVisibility(View.VISIBLE);
            custom3Button_ungrouped.setVisibility(View.VISIBLE);
            custom4Button_ungrouped.setVisibility(View.VISIBLE);
        }

        // Now decide if we are showing extra and custom buttons ungrouped or not
        if (!FullscreenActivity.grouppagebuttons && FullscreenActivity.page_extra_grouped) {
            extraButton.setVisibility(View.VISIBLE);
            chordButton_ungrouped.setVisibility(View.GONE);
            linkButton_ungrouped.setVisibility(View.GONE);
            stickyButton_ungrouped.setVisibility(View.GONE);
            notationButton_ungrouped.setVisibility(View.GONE);
            highlightButton_ungrouped.setVisibility(View.GONE);
            pageselectButton_ungrouped.setVisibility(View.GONE);
        } else if (!FullscreenActivity.grouppagebuttons && !FullscreenActivity.page_extra_grouped) {
            extraButton.setVisibility(View.GONE);
            chordButton_ungrouped.setVisibility(View.VISIBLE);
            linkButton_ungrouped.setVisibility(View.VISIBLE);
            stickyButton_ungrouped.setVisibility(View.VISIBLE);
            notationButton_ungrouped.setVisibility(View.VISIBLE);
            highlightButton_ungrouped.setVisibility(View.VISIBLE);
            pageselectButton_ungrouped.setVisibility(View.VISIBLE);
        }
        if (!FullscreenActivity.grouppagebuttons && FullscreenActivity.page_custom_grouped) {
            customButton.setVisibility(View.VISIBLE);
            custom1Button_ungrouped.setVisibility(View.GONE);
            custom2Button_ungrouped.setVisibility(View.GONE);
            custom3Button_ungrouped.setVisibility(View.GONE);
            custom4Button_ungrouped.setVisibility(View.GONE);
        } else if (!FullscreenActivity.grouppagebuttons && !FullscreenActivity.page_custom_grouped){
            customButton.setVisibility(View.GONE);
            custom1Button_ungrouped.setVisibility(View.VISIBLE);
            custom2Button_ungrouped.setVisibility(View.VISIBLE);
            custom3Button_ungrouped.setVisibility(View.VISIBLE);
            custom4Button_ungrouped.setVisibility(View.VISIBLE);
        }

        // Now hide any that the user doesn't want
        hideViewIfNeeded(setButton,FullscreenActivity.page_set_visible);
        hideViewIfNeeded(padButton,FullscreenActivity.page_pad_visible);
        hideViewIfNeeded(metronomeButton,FullscreenActivity.page_metronome_visible);
        hideViewIfNeeded(autoscrollButton,FullscreenActivity.page_autoscroll_visible);
        hideViewIfNeeded(chordButton,FullscreenActivity.page_chord_visible);
        hideViewIfNeeded(linkButton,FullscreenActivity.page_links_visible);
        hideViewIfNeeded(stickyButton,FullscreenActivity.page_sticky_visible);
        hideViewIfNeeded(notationButton,FullscreenActivity.page_notation_visible);
        hideViewIfNeeded(highlightButton,FullscreenActivity.page_highlight_visible);
        hideViewIfNeeded(pageselectButton,FullscreenActivity.page_pages_visible);
        hideViewIfNeeded(chordButton_ungrouped,FullscreenActivity.page_chord_visible);
        hideViewIfNeeded(linkButton_ungrouped,FullscreenActivity.page_links_visible);
        hideViewIfNeeded(stickyButton_ungrouped,FullscreenActivity.page_sticky_visible);
        hideViewIfNeeded(notationButton_ungrouped,FullscreenActivity.page_notation_visible);
        hideViewIfNeeded(highlightButton_ungrouped,FullscreenActivity.page_highlight_visible);
        hideViewIfNeeded(pageselectButton_ungrouped,FullscreenActivity.page_pages_visible);
        hideViewIfNeeded(custom1Button,FullscreenActivity.page_custom1_visible);
        hideViewIfNeeded(custom2Button,FullscreenActivity.page_custom2_visible);
        hideViewIfNeeded(custom3Button,FullscreenActivity.page_custom3_visible);
        hideViewIfNeeded(custom4Button,FullscreenActivity.page_custom4_visible);
        hideViewIfNeeded(custom1Button_ungrouped,FullscreenActivity.page_custom1_visible);
        hideViewIfNeeded(custom2Button_ungrouped,FullscreenActivity.page_custom2_visible);
        hideViewIfNeeded(custom3Button_ungrouped,FullscreenActivity.page_custom3_visible);
        hideViewIfNeeded(custom4Button_ungrouped,FullscreenActivity.page_custom4_visible);

        // Hide unnecessary ones
        if (FullscreenActivity.grouppagebuttons && !FullscreenActivity.page_pad_visible &&
                !FullscreenActivity.page_metronome_visible && !FullscreenActivity.page_autoscroll_visible &&
                !FullscreenActivity.page_chord_visible && !FullscreenActivity.page_links_visible &&
                !FullscreenActivity.page_sticky_visible && !FullscreenActivity.page_pages_visible &&
                !FullscreenActivity.page_custom1_visible && !FullscreenActivity.page_custom2_visible &&
                !FullscreenActivity.page_custom3_visible && !FullscreenActivity.page_custom4_visible) {
            // User doesn't have any buttons set to being visible!
            hideViewIfNeeded(customButton,false);
        }
        if (FullscreenActivity.page_extra_grouped && !FullscreenActivity.page_chord_visible &&
                !FullscreenActivity.page_links_visible && !FullscreenActivity.page_sticky_visible &&
                !FullscreenActivity.page_pages_visible) {
            // User doesn't have any extra info buttons set to being visible!
            hideViewIfNeeded(extraButton,false);
        }
        if (FullscreenActivity.page_custom_grouped && !FullscreenActivity.page_custom1_visible &&
                !FullscreenActivity.page_custom2_visible && !FullscreenActivity.page_custom3_visible &&
                !FullscreenActivity.page_custom4_visible) {
            // User doesn't have any custom buttons set to being visible!
            hideViewIfNeeded(customButton,false);
        }

    }

    public void hideViewIfNeeded(View v, boolean show) {
        if (!show) {
            v.setVisibility(View.GONE);
        }
    }

    @Override
    public void pageButtonAlpha(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float setAlpha = FullscreenActivity.pageButtonAlpha;
                float padAlpha = FullscreenActivity.pageButtonAlpha;
                float autoscrollAlpha = FullscreenActivity.pageButtonAlpha;
                float metronomeAlpha = FullscreenActivity.pageButtonAlpha;
                float extraAlpha = FullscreenActivity.pageButtonAlpha;
                float chordsAlpha = FullscreenActivity.pageButtonAlpha;
                float stickyAlpha = FullscreenActivity.pageButtonAlpha;
                float highlightAlpha = FullscreenActivity.pageButtonAlpha;
                float pageselectAlpha = FullscreenActivity.pageButtonAlpha;
                float linkAlpha = FullscreenActivity.pageButtonAlpha;
                float customAlpha = FullscreenActivity.pageButtonAlpha;
                float custom1Alpha = FullscreenActivity.pageButtonAlpha;
                float custom2Alpha = FullscreenActivity.pageButtonAlpha;
                float custom3Alpha = FullscreenActivity.pageButtonAlpha;
                float custom4Alpha = FullscreenActivity.pageButtonAlpha;
                float scrollDownAlpha = FullscreenActivity.pageButtonAlpha;
                float scrollUpAlpha = FullscreenActivity.pageButtonAlpha;
                float setBackAlpha = FullscreenActivity.pageButtonAlpha;
                float setForwardAlpha = FullscreenActivity.pageButtonAlpha;

                extrabuttons.setVisibility(View.GONE);
                extrabuttons2.setVisibility(View.GONE);
                float onval = FullscreenActivity.pageButtonAlpha + 0.3f;
                if (onval > 1.0f) {
                    onval = 1.0f;
                }

                if (s != null) {
                    switch (s) {
                        case "set":
                            setAlpha = onval;
                            break;
                        case "pad":
                            padAlpha = onval;
                            break;
                        case "autoscroll":
                            autoscrollAlpha = onval;
                            break;
                        case "metronome":
                            metronomeAlpha = onval;
                            break;
                        case "extra":
                            extraAlpha = onval;
                            extrabuttons.setVisibility(View.VISIBLE);
                            break;
                        case "chord":
                            extraAlpha = onval;
                            extrabuttons.setVisibility(View.VISIBLE);
                            chordsAlpha = onval;
                            break;
                        case "sticky":
                            extraAlpha = onval;
                            extrabuttons.setVisibility(View.VISIBLE);
                            stickyAlpha = onval;
                            break;
                        case "highlight":
                            extraAlpha = onval;
                            extrabuttons.setVisibility(View.VISIBLE);
                            highlightAlpha = onval;
                            break;
                        case "pageselect":
                            extraAlpha = onval;
                            extrabuttons.setVisibility(View.VISIBLE);
                            pageselectAlpha = onval;
                            break;
                        case "link":
                            extraAlpha = onval;
                            extrabuttons.setVisibility(View.VISIBLE);
                            linkAlpha = onval;
                            break;
                        case "custom":
                            customAlpha = onval;
                            extrabuttons2.setVisibility(View.VISIBLE);
                            break;
                        case "custom1":
                            customAlpha = onval;
                            custom1Alpha = onval;
                            extrabuttons2.setVisibility(View.VISIBLE);
                            break;
                        case "custom2":
                            customAlpha = onval;
                            custom2Alpha = onval;
                            extrabuttons2.setVisibility(View.VISIBLE);
                            break;
                        case "custom3":
                            customAlpha = onval;
                            custom3Alpha = onval;
                            extrabuttons2.setVisibility(View.VISIBLE);
                            break;
                        case "custom4":
                            customAlpha = onval;
                            custom4Alpha = onval;
                            extrabuttons2.setVisibility(View.VISIBLE);
                            break;
                    }
                }

                setButton.setAlpha(setAlpha);
                padButton.setAlpha(padAlpha);
                autoscrollButton.setAlpha(autoscrollAlpha);
                metronomeButton.setAlpha(metronomeAlpha);
                extraButton.setAlpha(extraAlpha);
                chordButton.setAlpha(chordsAlpha);
                stickyButton.setAlpha(stickyAlpha);
                notationButton.setAlpha(stickyAlpha);
                highlightButton.setAlpha(highlightAlpha);
                pageselectButton.setAlpha(pageselectAlpha);
                linkButton.setAlpha(linkAlpha);
                chordButton_ungrouped.setAlpha(chordsAlpha);
                stickyButton_ungrouped.setAlpha(stickyAlpha);
                notationButton_ungrouped.setAlpha(stickyAlpha);
                highlightButton_ungrouped.setAlpha(highlightAlpha);
                pageselectButton_ungrouped.setAlpha(pageselectAlpha);
                linkButton_ungrouped.setAlpha(linkAlpha);
                customButton.setAlpha(customAlpha);
                custom1Button.setAlpha(custom1Alpha);
                custom2Button.setAlpha(custom2Alpha);
                custom3Button.setAlpha(custom3Alpha);
                custom4Button.setAlpha(custom4Alpha);
                custom1Button_ungrouped.setAlpha(custom1Alpha);
                custom2Button_ungrouped.setAlpha(custom2Alpha);
                custom3Button_ungrouped.setAlpha(custom3Alpha);
                custom4Button_ungrouped.setAlpha(custom4Alpha);
                scrollDownButton.setAlpha(scrollDownAlpha);
                scrollUpButton.setAlpha(scrollUpAlpha);
                setBackButton.setAlpha(setBackAlpha);
                setForwardButton.setAlpha(setForwardAlpha);

                setButton.setSize(FullscreenActivity.fabSize);
                padButton.setSize(FullscreenActivity.fabSize);
                autoscrollButton.setSize(FullscreenActivity.fabSize);
                metronomeButton.setSize(FullscreenActivity.fabSize);
                extraButton.setSize(FullscreenActivity.fabSize);
                chordButton.setSize(FullscreenActivity.fabSize);
                stickyButton.setSize(FullscreenActivity.fabSize);
                notationButton.setSize(FullscreenActivity.fabSize);
                highlightButton.setSize(FullscreenActivity.fabSize);
                pageselectButton.setSize(FullscreenActivity.fabSize);
                linkButton.setSize(FullscreenActivity.fabSize);
                chordButton_ungrouped.setSize(FullscreenActivity.fabSize);
                stickyButton_ungrouped.setSize(FullscreenActivity.fabSize);
                notationButton_ungrouped.setSize(FullscreenActivity.fabSize);
                highlightButton_ungrouped.setSize(FullscreenActivity.fabSize);
                pageselectButton_ungrouped.setSize(FullscreenActivity.fabSize);
                linkButton_ungrouped.setSize(FullscreenActivity.fabSize);
                customButton.setSize(FullscreenActivity.fabSize);
                custom1Button.setSize(FullscreenActivity.fabSize);
                custom2Button.setSize(FullscreenActivity.fabSize);
                custom3Button.setSize(FullscreenActivity.fabSize);
                custom4Button.setSize(FullscreenActivity.fabSize);
                custom1Button_ungrouped.setSize(FullscreenActivity.fabSize);
                custom2Button_ungrouped.setSize(FullscreenActivity.fabSize);
                custom3Button_ungrouped.setSize(FullscreenActivity.fabSize);
                custom4Button_ungrouped.setSize(FullscreenActivity.fabSize);
                scrollDownButton.setSize(FullscreenActivity.fabSize);
                scrollUpButton.setSize(FullscreenActivity.fabSize);
                setBackButton.setSize(FullscreenActivity.fabSize);
                setForwardButton.setSize(FullscreenActivity.fabSize);
            }
        });
    }

    @Override
    public void setupQuickLaunchButtons() {
        // Based on the user's choices for the custom quicklaunch buttons,
        // set the appropriate icons and onClick listeners
        custom1Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_1));
        custom2Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_2));
        custom3Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_3));
        custom4Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_4));
        custom1Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_1));
        custom2Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_2));
        custom3Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_3));
        custom4Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_4));
        custom1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom1Button,StageMode.this);
                customButtonAction(FullscreenActivity.quickLaunchButton_1);
            }
        });
        custom2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom2Button,StageMode.this);
                customButtonAction(FullscreenActivity.quickLaunchButton_2);
            }
        });
        custom3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom3Button,StageMode.this);
                customButtonAction(FullscreenActivity.quickLaunchButton_3);
            }
        });
        custom4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom4Button,StageMode.this);
                customButtonAction(FullscreenActivity.quickLaunchButton_4);
            }
        });
        custom1Button_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom1Button_ungrouped,StageMode.this);
                customButtonAction(FullscreenActivity.quickLaunchButton_1);
            }
        });
        custom2Button_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom2Button_ungrouped,StageMode.this);
                customButtonAction(FullscreenActivity.quickLaunchButton_2);
            }
        });
        custom3Button_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom3Button_ungrouped,StageMode.this);
                customButtonAction(FullscreenActivity.quickLaunchButton_3);
            }
        });
        custom4Button_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(custom4Button_ungrouped,StageMode.this);
                customButtonAction(FullscreenActivity.quickLaunchButton_4);
            }
        });
    }

    public void customButtonAction(String s) {
        switch (s) {
            case "":
            default:
                FullscreenActivity.whattodo = "quicklaunch";
                openFragment();
                break;

            case "editsong":
            case "changetheme":
            case "autoscale":
            case "changefonts":
            case "profiles":
            case "gestures":
            case "footpedal":
            case "transpose":
            case "fullsearch":
            case "randomsong":
            case "abcnotation_edit":
            case "abcnotation":
            case "abcnotation_editsong":
                if (s.equals("editsong") && !justSong(StageMode.this) && !FullscreenActivity.isPDF) {
                    ShowToast.showToast(StageMode.this);
                } else {
                    if (FullscreenActivity.isPDF && s.equals("editsong")) {
                        s = "extractPDF";
                    }
                    FullscreenActivity.whattodo = s;
                    openFragment();
                }
                break;

            case "showchords":
                FullscreenActivity.showChords = !FullscreenActivity.showChords;
                Preferences.savePreferences();
                loadSong();
                break;

            case "showcapo":
                FullscreenActivity.showCapo = !FullscreenActivity.showCapo;
                Preferences.savePreferences();
                loadSong();
                break;

            case "showlyrics":
                FullscreenActivity.showLyrics = !FullscreenActivity.showLyrics;
                Preferences.savePreferences();
                loadSong();
                break;

            case "inc_autoscroll_speed":
                if(FullscreenActivity.autoscrollispaused) {
                    FullscreenActivity.autoscrollispaused = false;
                    FullscreenActivity.autoscroll_modifier = 0;
                }
                else {
                    FullscreenActivity.autoscroll_modifier = FullscreenActivity.autoscroll_modifier + 4;
                }
                break;

            case "dec_autoscroll_speed":
                if(FullscreenActivity.autoscroll_pixels + FullscreenActivity.autoscroll_modifier >= 4)
                    FullscreenActivity.autoscroll_modifier = FullscreenActivity.autoscroll_modifier - 4;
                if(FullscreenActivity.autoscroll_pixels + FullscreenActivity.autoscroll_modifier <= 4)
                    FullscreenActivity.autoscrollispaused = true;
                break;

            case "toggle_autoscroll_pause":
                FullscreenActivity.autoscrollispaused = !FullscreenActivity.autoscrollispaused;
                break;
        }
    }

    @Override
    public void onSongImportDone(String message) {
        FullscreenActivity.myToastMessage = message;
        if (!message.equals("cancel")) {
            showToastMessage(message);
            prepareSongMenu();
        }
        OnSongConvert.doBatchConvert(StageMode.this);
    }

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
    public void doEdit() {
        if (FullscreenActivity.isPDF) {
            FullscreenActivity.whattodo = "extractPDF";
            openFragment();
        } else if (FullscreenActivity.isSong){
            FullscreenActivity.whattodo = "editsong";
            openFragment();
        }
    }

    @Override
    public void updatePresentationOrder() {
        // User has changed the presentation order
        Preferences.savePreferences();
        doEdit();
    }

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

    @Override
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

    @Override
    public void findSongInFolders() {
        //scroll to the song in the song menu
        try {
            song_list_view.setSelection(FullscreenActivity.currentSongIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
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
    public void doMoveSection() {
        switch (FullscreenActivity.setMoveDirection) {
            case "forward":
                FullscreenActivity.currentSection += 1;
                selectSection(FullscreenActivity.currentSection);
                break;
            case "back":
                FullscreenActivity.currentSection -= 1;
                selectSection(FullscreenActivity.currentSection);
                break;
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
            if (FullscreenActivity.mSetList!=null && FullscreenActivity.indexSongInSet>-1 &&
                    FullscreenActivity.mSetList.length>FullscreenActivity.indexSongInSet) {
                FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
                FullscreenActivity.whatsongforsetwork = FullscreenActivity.linkclicked;
            } else {
                FullscreenActivity.linkclicked = "";
                FullscreenActivity.whatsongforsetwork = "";
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
                // Get the next set positions and song
                if (!cancelled) {
                    SetActions.doMoveInSet(StageMode.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void refreshActionBar() {
        invalidateOptionsMenu();
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
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            try {
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
                // Oops
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
                CustomSlide.addCustomSlide(StageMode.this);
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
                    DoVibrate.vibrate(StageMode.this, 50);

                    prepareOptionMenu();
                    closeMyDrawers("option_delayed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    // List of URIs to provide to Android Beam
    private Uri[] mFileUris = new Uri[10];
    @SuppressLint("NewApi")
    private class FileUriCallback implements NfcAdapter.CreateBeamUrisCallback {
        FileUriCallback() {}

        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            String transferFile = FullscreenActivity.songfilename;
            File extDir;
            if (FullscreenActivity.whichSongFolder.equals(getString(R.string.mainfoldername))) {
                extDir =FullscreenActivity.dir;
            } else {
                extDir = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/");
            }
            File requestFile = new File(extDir, transferFile);
            boolean b = requestFile.setReadable(true, false);
            if (!b) {
                // Get a URI for the File and add it to the list of URIs
                Uri fileUri = Uri.fromFile(requestFile);
                if (fileUri != null) {
                    mFileUris[0] = fileUri;
                } else {
                    Log.e("My Activity", "No File URI available for file.");
                }
                return mFileUris;
            } else {
                return null;
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
                }, 2000);
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
                }, 2000);
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
                }, 2000);
            }
        }
    }

    public void holdBeforeLoading() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we receive if, check this is the first time
        if (FullscreenActivity.firstReceivingOfSalut) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalut = false;
            loadSong();

            // After a delay of 2 seconds, reset the firstReceivingOfSalut;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalut = true;
                }
            },2000);
        }
    }

    public void holdBeforeLoadingXML() {
        // When a song is sent via Salut, it occassionally gets set multiple times (poor network)
        // As soon as we receive if, check this is the first time
        if (FullscreenActivity.firstReceivingOfSalutXML) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalutXML = false;
            FullscreenActivity.mySalutXML = FullscreenActivity.mySalutXML.replace("\\n","$$__$$");
            FullscreenActivity.mySalutXML = FullscreenActivity.mySalutXML.replace("\\","");
            FullscreenActivity.mySalutXML = FullscreenActivity.mySalutXML.replace("$$__$$","\n");

            Log.d("d",""+FullscreenActivity.mySalutXML);
            // Create the temp song file
            try {
                if (!FullscreenActivity.dirreceived.exists()) {
                    if (!FullscreenActivity.dirreceived.mkdirs()) {
                        Log.d("d","Couldn't make directory");
                    }
                }
                FullscreenActivity.file = new File(FullscreenActivity.dirreceived,"ReceivedSong");

                FileUtils.writeStringToFile(FullscreenActivity.file, FullscreenActivity.mySalutXML, "UTF-8");

                //FileOutputStream overWrite = new FileOutputStream(FullscreenActivity.file,	false);
                //overWrite.write(FullscreenActivity.mySalutXML.getBytes());
                //overWrite.flush();
                //overWrite.close();
                FullscreenActivity.songfilename = "ReceivedSong";
                FullscreenActivity.whichSongFolder = "../Received";
            } catch (Exception e) {
                FullscreenActivity.myToastMessage = getResources().getString(R.string.songdoesntexist);
                ShowToast.showToast(StageMode.this);
            }
            loadSong();

            // After a delay of 2 seconds, reset the firstReceivingOfSalut;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalutXML = true;
                }
            },2000);
        }
    }

    public void holdBeforeLoadingSection(int s) {
        if (FullscreenActivity.firstReceivingOfSalutSection) {
            // Now turn it off
            FullscreenActivity.firstReceivingOfSalutSection = false;
            selectSection(s);

            // After a delay of 2 seconds, reset the firstReceivingOfSalutSection;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FullscreenActivity.firstReceivingOfSalutSection = true;
                }
            }, 2000);
        }
    }

    @Override
    public void loadSong() {
        // Set the focus
        // Don't do this for a blacklisted filetype (application, video, audio)
        if (ListSongFiles.blacklistFileType(FullscreenActivity.songfilename)) {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.file_type_unknown);
            ShowToast.showToast(StageMode.this);
        } else {
            // Send WiFiP2P intent
            if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
                try {
                    sendSongLocationToConnected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // If there is a sticky note showing, remove it
            if (stickyPopUpWindow!=null && stickyPopUpWindow.isShowing()) {
                try {
                    stickyPopUpWindow.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Animate out the current song
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                    glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
                } else {
                    songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
                }
            } else {
                if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                    glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
                } else {
                    songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
                }
            }
            // If there were highlight notes showing, move them away
            if (FullscreenActivity.whichMode.equals("Performance") && highlightNotes!=null && highlightNotes.getVisibility() == View.VISIBLE) {
                if (FullscreenActivity.whichDirection.equals("L2R")) {
                    highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
                } else {
                    highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
                }
            }

            // Remove any capokey
            FullscreenActivity.capokey = "";

            // End any current autscrolling
            stopAutoScroll();
            padcurrentTime_TextView.setText(getString(R.string.zerotime));
            backingtrackProgress.setVisibility(View.GONE);

            // After animate out, load the song
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        glideimage_ScrollView.setVisibility(View.GONE);
                        songscrollview.setVisibility(View.GONE);
                        highlightNotes.setVisibility(View.GONE);
                        FullscreenActivity.highlightOn = false;
                        glideimage_ScrollView.scrollTo(0, 0);
                        songscrollview.scrollTo(0, 0);

                        // Hide the image, cause we might be loading a proper song!
                        glideimage.setBackgroundColor(0x00000000);
                        glideimage.setImageDrawable(null);

                    } catch (Exception e) {
                        Log.d("d","error updating the views");
                    }
                    // Load the song
                    doCancelAsyncTask(loadsong_async);
                    loadsong_async = new LoadSongAsync();
                    try {
                        loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 300);
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class LoadSongAsync extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                FullscreenActivity.scalingfiguredout = false;
                sectionpresented = false;

                try {
                    LoadXML.loadXML(StageMode.this);
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }

                // Open the current folder and list the songs
                ListSongFiles.getAllSongFiles();
                // Get the current song index
                try {
                    ListSongFiles.getCurrentSongIndex();
                } catch (Exception e) {
                    // Ooops
                }

                // If we are in a set, try to get the appropriate indexes

                SetActions.getSongForSetWork(StageMode.this);
                SetActions.indexSongInSet();

                if (FullscreenActivity.mLyrics != null) {
                    FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
                } else {
                    FullscreenActivity.myLyrics = "";
                }

                // Clear the old headings (presention order looks for these)
                FullscreenActivity.foundSongSections_heading = new ArrayList<>();

                if (FullscreenActivity.isSong) {

                    // Check the chord format
                    try {
                        Transpose.checkChordFormat();
                    } catch (Exception e) {
                        Log.d("d", "Error checking the chord format");
                    }

                    FullscreenActivity.presenterChords = "Y";

                    // Sort song formatting
                    // 1. Sort multiline verse/chord formats
                    FullscreenActivity.myLyrics = ProcessSong.fixMultiLineFormat(FullscreenActivity.myLyrics, StageMode.this);

                    // If we want info on the next song in the set, add it as a comment line
                    ProcessSong.addExtraInfo(StageMode.this);

                    // Decide if the pad, metronome and autoscroll are good to go
                    FullscreenActivity.padok = PadFunctions.isPadValid(StageMode.this);
                    FullscreenActivity.metronomeok = Metronome.isMetronomeValid();
                    FullscreenActivity.autoscrollok = ProcessSong.isAutoScrollValid();

                    // 2. Split the song into sections
                    FullscreenActivity.songSections = ProcessSong.splitSongIntoSections(FullscreenActivity.myLyrics, StageMode.this);

                    // 3. Put the song into presentation order if required
                    if (FullscreenActivity.usePresentationOrder && !FullscreenActivity.mPresentation.equals("")) {
                        FullscreenActivity.songSections = ProcessSong.matchPresentationOrder(FullscreenActivity.songSections, StageMode.this);
                    }

                    FullscreenActivity.songSections = ProcessSong.splitLaterSplits(FullscreenActivity.songSections);

                    // 4. Get the section headings/types (may have changed after presentationorder
                    FullscreenActivity.songSectionsLabels = new String[FullscreenActivity.songSections.length];
                    FullscreenActivity.songSectionsTypes = new String[FullscreenActivity.songSections.length];
                    for (int sl = 0; sl < FullscreenActivity.songSections.length; sl++) {
                        FullscreenActivity.songSectionsLabels[sl] = ProcessSong.getSectionHeadings(FullscreenActivity.songSections[sl]);
                    }

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
                            FullscreenActivity.sectionLineTypes[x][y] = ProcessSong.determineLineTypes(FullscreenActivity.sectionContents[x][y], StageMode.this);
                            if (FullscreenActivity.sectionContents[x][y].length() > 0 && (FullscreenActivity.sectionContents[x][y].indexOf(" ") == 0 ||
                                    FullscreenActivity.sectionContents[x][y].indexOf(".") == 0 || FullscreenActivity.sectionContents[x][y].indexOf(";") == 0)) {
                                FullscreenActivity.sectionContents[x][y] = FullscreenActivity.sectionContents[x][y].substring(1);
                            }
                        }
                    }

                    if (FullscreenActivity.whichMode.equals("Performance")) {
                        // Put the song back together for checking for splitpoints
                        ProcessSong.rebuildParsedLyrics(FullscreenActivity.songSections.length);
                        FullscreenActivity.numrowstowrite = FullscreenActivity.myParsedLyrics.length;

                        // Look for song split points
                        ProcessSong.lookForSplitPoints();

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

        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    prepareSongMenu();

                    // Fix the page flags
                    setWindowFlags();
                    setWindowFlagsAdvanced();

                    // Show the ActionBar
                    if (delayactionBarHide != null && hideActionBarRunnable != null) {
                        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
                    }

                    if (ab != null) {
                        ab.show();
                    }

                    if (FullscreenActivity.hideActionBar) {
                        if (delayactionBarHide != null) {
                            delayactionBarHide.postDelayed(hideActionBarRunnable, 1000);
                        }
                    }

                    // Any errors to show?
                    if (!FullscreenActivity.myToastMessage.equals("")) {
                        ShowToast.showToast(StageMode.this);
                    }

                    // If pads were already playing (previous song), start them up again
                    // Don't redo this if the orientation has changed (causing a reload)
                    // Stop restarting the pads if changing portrait/landscape
                    // Only play if this isn't called by an orientation change
                    if (!FullscreenActivity.orientationchanged && FullscreenActivity.padson) {
                        preparePad();
                    }

                    // Now, reset the orientation.
                    FullscreenActivity.orientationchanged = false;

                    // Get the current orientation
                    FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

                    // Put the title of the song in the taskbar
                    songtitle_ab.setText(ProcessSong.getSongTitle());
                    songkey_ab.setText(ProcessSong.getSongKey());
                    songauthor_ab.setText(ProcessSong.getSongAuthor());

                    if (FullscreenActivity.isPDF) {
                        loadPDF();

                    } else if (FullscreenActivity.isImage) {
                        loadImage();

                    } else if (FullscreenActivity.isSong) {
                        //Prepare the song views
                        prepareView();
                    }


                    // Automatically start the metronome if we wanted it to
                    if (FullscreenActivity.metronomeonoff.equals("on")) {
                        // Stop it
                        Metronome.startstopMetronome(StageMode.this);
                        // Start it again with the new values
                        gesture7();
                    }

                    // Decide if we have loaded a song in the current set
                    fixSetActionButtons();

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

                    // Send WiFiP2P intent
                    if (FullscreenActivity.network!=null && FullscreenActivity.network.isRunningAsHost) {
                        try {
                            sendSongXMLToConnected();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // If autoshowing sticky notes as a popup
                    if (FullscreenActivity.toggleAutoSticky.equals("F") && !FullscreenActivity.mNotes.equals("")) {
                        try {
                            showSticky();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void fixSetActionButtons() {
        FullscreenActivity.setView = SetActions.isSongInSet(StageMode.this);

        if (FullscreenActivity.setView) {
            // Now get the position in the set and decide on the set move buttons
            if (FullscreenActivity.indexSongInSet < 0) {
                // We weren't in set mode, so find the first instance of this song.
                SetActions.indexSongInSet();
            }
            // If we aren't at the beginning or have pdf pages before this, enable the setBackButton
            if ((FullscreenActivity.indexSongInSet > 0) ||
                    (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent>0)) {
                setBackButton.setVisibility(View.VISIBLE);
            } else {
                setBackButton.setVisibility(View.INVISIBLE);
            }

            // If we aren't at the end of the set or inside a multipage pdf, enable the setForwardButton
            if ((FullscreenActivity.indexSongInSet < FullscreenActivity.mSetList.length - 1) ||
                    (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent<FullscreenActivity.pdfPageCount - 1)) {
                setForwardButton.setVisibility(View.VISIBLE);
            } else {
                setForwardButton.setVisibility(View.INVISIBLE);
            }

        } else {
            FullscreenActivity.indexSongInSet = -1;
            setBackButton.setVisibility(View.INVISIBLE);
            setForwardButton.setVisibility(View.INVISIBLE);
        }
    }

    public void loadImage() {
        // Process the image location into an URI
        Uri imageUri = Uri.fromFile(FullscreenActivity.file);

        glideimage_ScrollView.setVisibility(View.VISIBLE);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(FullscreenActivity.file.toString(), options);
        int imgwidth = options.outWidth;
        int imgheight = options.outHeight;
        int widthavail = getAvailableWidth();
        int heightavail = getAvailableHeight();

        // Decide on the image size to use
        if (FullscreenActivity.toggleYScale.equals("Y")) {
            // Glide sorts the width vs height (keeps the image in the space available using fitCenter
            glideimage.setBackgroundColor(0x00000000);
            RequestOptions myOptions = new RequestOptions()
                    .fitCenter()
                    .override(widthavail, heightavail);
            Glide.with(StageMode.this).load(imageUri).apply(myOptions).into(glideimage);
        } else {
            // Now decide on the scaling required....
            float xscale = (float) widthavail / (float) imgwidth;
            int glideheight = (int) ((float)imgheight * xscale);
            glideimage.setBackgroundColor(0x00000000);
            RequestOptions myOptions = new RequestOptions()
                    .override(widthavail,glideheight);
            Glide.with(StageMode.this).load(imageUri).apply(myOptions).into(glideimage);
        }

        songscrollview.removeAllViews();

        // Animate the view in after a delay (waiting for slide out animation to complete
        animateInSong();

        // Check for scroll position
        delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);

        Preferences.loadSongSuccess();
    }

    @SuppressWarnings("deprecation")
    public void loadPDF() {
        Bitmap bmp = ProcessSong.createPDFPage(StageMode.this, getAvailableWidth(), getAvailableHeight(), FullscreenActivity.toggleYScale);

        glideimage_ScrollView.setVisibility(View.VISIBLE);

        // Set the ab title to include the page info if available
        if (bmp!=null) {
            songtitle_ab.setText(FullscreenActivity.mTitle);
            songkey_ab.setText("");
            String text = (FullscreenActivity.pdfPageCurrent + 1) + "/" + FullscreenActivity.pdfPageCount;
            songauthor_ab.setText(text);

            // Set the image to the view
            glideimage.setBackgroundColor(0xffffffff);
            glideimage.setImageBitmap(bmp);

        } else {
            songtitle_ab.setText(FullscreenActivity.mTitle);
            songkey_ab.setText("");
            songauthor_ab.setText(getResources().getString(R.string.nothighenoughapi));

            // Set the image to the unhappy android
            Drawable myDrawable = getResources().getDrawable(R.drawable.unhappy_android);
            glideimage.setImageDrawable(myDrawable);

            // Set an intent to try and open the pdf with an appropriate application
            Intent target = new Intent(Intent.ACTION_VIEW);
            // Run an intent to try to show the pdf externally
            target.setDataAndType(Uri.fromFile(FullscreenActivity.file), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            callIntent("openpdf",target);
        }
        Preferences.savePreferences();

        songscrollview.removeAllViews();

        // Animate the view in after a delay (waiting for slide out animation to complete
        animateInSong();

        // Check for scroll position
        delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);

        Preferences.loadSongSuccess();
    }

    @Override
    public void doDownload(String filename) {
        if (do_download!=null) {
            doCancelAsyncTask(do_download);
        }
        do_download = new DownloadTask(StageMode.this,filename);
        try {
            do_download.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void createPerformanceView1col() {
        doCancelAsyncTask(createperformanceview1col_async);
        createperformanceview1col_async = new CreatePerformanceView1Col();
        try {
            createperformanceview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class CreatePerformanceView1Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_1 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_1 = new RelativeLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            try {
                // We know how many columns we are using, so lets go for it.
                column1_1 = ProcessSong.preparePerformanceColumnView(StageMode.this);
                songbit = ProcessSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
                boxbit1_1 = ProcessSong.preparePerformanceBoxView(StageMode.this, 0, FullscreenActivity.padding);

                // Add the song sections...
                for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                    float fontsize = ProcessSong.setScaledFontSize(0);
                    LinearLayout sectionview = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                    sectionview.setPadding(0, 0, 0, 0);
                    sectionview.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    column1_1.addView(sectionview);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
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
                    songscrollview.removeAllViews();
                    LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    if (FullscreenActivity.thissong_scale.equals("Y")) {
                        llp1_1 = new LinearLayout.LayoutParams(getAvailableWidth(), getAvailableHeight());
                    } else if (FullscreenActivity.thissong_scale.equals("W")) {
                        llp1_1 = new LinearLayout.LayoutParams(getAvailableWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
                    }
                    llp1_1.setMargins(0, 0, 0, 0);
                    boxbit1_1.setLayoutParams(llp1_1);
                    boxbit1_1.addView(column1_1);
                    songbit.addView(boxbit1_1);
                    songscrollview.addView(songbit);
                    animateInSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createPerformanceView2col() {
        doCancelAsyncTask(createperformanceview2col_async);
        createperformanceview2col_async = new CreatePerformanceView2Col();
        try {
            createperformanceview2col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class CreatePerformanceView2Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_2 = new LinearLayout(StageMode.this);
        LinearLayout column2_2 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_2 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit2_2 = new RelativeLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            try {
                // We know how many columns we are using, so lets go for it.
                column1_2 = ProcessSong.preparePerformanceColumnView(StageMode.this);
                column2_2 = ProcessSong.preparePerformanceColumnView(StageMode.this);
                songbit = ProcessSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
                boxbit1_2 = ProcessSong.preparePerformanceBoxView(StageMode.this, getPixelsFromDpi(4), FullscreenActivity.padding);
                boxbit2_2 = ProcessSong.preparePerformanceBoxView(StageMode.this, 0, FullscreenActivity.padding);

                // Add the song sections...
                for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                    if (x < FullscreenActivity.halfsplit_section) {
                        float fontsize = ProcessSong.setScaledFontSize(1);
                        LinearLayout sectionview = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                        sectionview.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        sectionview.setPadding(0, 0, 0, 0);
                        column1_2.addView(sectionview);

                    } else {
                        float fontsize = ProcessSong.setScaledFontSize(2);
                        LinearLayout sectionview2 = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                        sectionview2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        sectionview2.setPadding(0, 0, 0, 0);
                        column2_2.addView(sectionview2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
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
                    songscrollview.removeAllViews();
                    LinearLayout.LayoutParams llp1_2 = new LinearLayout.LayoutParams((int) (getAvailableWidth() / 2.0f) - getPixelsFromDpi(2), LinearLayout.LayoutParams.WRAP_CONTENT);
                    LinearLayout.LayoutParams llp2_2 = new LinearLayout.LayoutParams((int) (getAvailableWidth() / 2.0f) - getPixelsFromDpi(2), LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_2.setMargins(0, 0, getPixelsFromDpi(4), 0);
                    llp2_2.setMargins(0, 0, 0, 0);
                    boxbit1_2.setLayoutParams(llp1_2);
                    boxbit2_2.setLayoutParams(llp2_2);
                    boxbit1_2.addView(column1_2);
                    boxbit2_2.addView(column2_2);
                    songbit.addView(boxbit1_2);
                    songbit.addView(boxbit2_2);
                    songscrollview.addView(songbit);
                    animateInSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createPerformanceView3col() {
        doCancelAsyncTask(createperformanceview3col_async);
        createperformanceview3col_async = new CreatePerformanceView3Col();
        try {
            createperformanceview3col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class CreatePerformanceView3Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_3 = new LinearLayout(StageMode.this);
        LinearLayout column2_3 = new LinearLayout(StageMode.this);
        LinearLayout column3_3 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_3 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit2_3 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit3_3 = new RelativeLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            try {
                // We know how many columns we are using, so lets go for it.
                column1_3 = ProcessSong.preparePerformanceColumnView(StageMode.this);
                column2_3 = ProcessSong.preparePerformanceColumnView(StageMode.this);
                column3_3 = ProcessSong.preparePerformanceColumnView(StageMode.this);
                songbit = ProcessSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
                boxbit1_3 = ProcessSong.preparePerformanceBoxView(StageMode.this, getPixelsFromDpi(3), FullscreenActivity.padding);
                boxbit2_3 = ProcessSong.preparePerformanceBoxView(StageMode.this, getPixelsFromDpi(3), FullscreenActivity.padding);
                boxbit3_3 = ProcessSong.preparePerformanceBoxView(StageMode.this, 0, FullscreenActivity.padding);

                // Add the song sections...
                for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                    if (x < FullscreenActivity.thirdsplit_section) {
                        float fontsize = ProcessSong.setScaledFontSize(3);
                        LinearLayout sectionview = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                        sectionview.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        sectionview.setPadding(0, 0, 0, 0);
                        column1_3.addView(sectionview);

                    } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                        float fontsize = ProcessSong.setScaledFontSize(4);
                        LinearLayout sectionview2 = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                        sectionview2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        sectionview2.setPadding(0, 0, 0, 0);
                        column2_3.addView(sectionview2);

                    } else {
                        float fontsize = ProcessSong.setScaledFontSize(5);
                        LinearLayout sectionview3 = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                        sectionview3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        sectionview3.setPadding(0, 0, 0, 0);
                        column3_3.addView(sectionview3);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
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
                    songscrollview.removeAllViews();
                    LinearLayout.LayoutParams llp1_3 = new LinearLayout.LayoutParams((int) (getAvailableWidth() / 3.0f) - getPixelsFromDpi(3), LinearLayout.LayoutParams.WRAP_CONTENT);
                    LinearLayout.LayoutParams llp3_3 = new LinearLayout.LayoutParams((int) (getAvailableWidth() / 3.0f) - getPixelsFromDpi(3), LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_3.setMargins(0, 0, getPixelsFromDpi(4), 0);
                    llp3_3.setMargins(0, 0, 0, 0);
                    boxbit1_3.setLayoutParams(llp1_3);
                    boxbit2_3.setLayoutParams(llp1_3);
                    boxbit3_3.setLayoutParams(llp3_3);
                    boxbit1_3.addView(column1_3);
                    boxbit2_3.addView(column2_3);
                    boxbit3_3.addView(column3_3);
                    songbit.addView(boxbit1_3);
                    songbit.addView(boxbit2_3);
                    songbit.addView(boxbit3_3);
                    songscrollview.addView(songbit);
                    animateInSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void animateInSong() {
        // If autoshowing highlighter notes
        if (FullscreenActivity.toggleAutoHighlight) {
            showHighlight();
        }
        // Now scroll in the song via an animation
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            songscrollview.setVisibility(View.GONE);
            glideimage_ScrollView.setVisibility(View.VISIBLE);
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
            } else {
                glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
            }
        } else {
            glideimage_ScrollView.setVisibility(View.GONE);
            songscrollview.setVisibility(View.VISIBLE);
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
            } else {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
            }
        }

        // Set the overrides back
        overridingwidth = false;
        overridingfull = false;

        // Check for dual screen presentation
        if (FullscreenActivity.whichMode.equals("Performance")) {
            dualScreenWork();
        } else {
            if (!sectionpresented) { // So it isn't called for each section.
                sectionpresented = true;
                dualScreenWork();
            }
        }

        // Check the scroll buttons
        onScrollAction();

        songscrollview.scrollTo(0,0);
        FullscreenActivity.newPosFloat = 0.0f;
        // Automatically start the autoscroll
        if (FullscreenActivity.autostartautoscroll && FullscreenActivity.clickedOnAutoScrollStart) {
            if (justSong(StageMode.this) && FullscreenActivity.autoscrollok) {
                songscrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        startAutoScroll();
                    }
                });
            }
        }

        setUpCapoInfo();
    }

    public void setUpCapoInfo() {
        boolean bothempty = true;
        // If we are showing capo chords, show this info
        if (capoinfo!=null && !FullscreenActivity.mCapo.equals("") && !FullscreenActivity.mCapo.equals("0")) {
            capoinfo.setText(ProcessSong.getCapoInfo());
            capoinfo.setVisibility(View.VISIBLE);
            bothempty = false;
        } else {
            capoinfo.setVisibility(View.GONE);
        }

        String capokey = ProcessSong.getCapoNewKey();
        if (!capokey.equals("")) {
            String t = " (" + capokey + ")";
            capoinfonewkey.setText(t);
            capoinfonewkey.setVisibility(View.VISIBLE);
            bothempty = false;
        } else {
            capoinfonewkey.setVisibility(View.GONE);
        }

        if (bothempty || !FullscreenActivity.showCapoChords || !FullscreenActivity.showChords) {
            capoInfo.setVisibility(View.GONE);
        } else {
            capoInfo.setVisibility(View.VISIBLE);
        }
    }

    public void dualScreenWork() {
        if (FullscreenActivity.isPresenting || FullscreenActivity.isHDMIConnected) {
            try {
                doCancelAsyncTask(dualscreenwork_async);
                dualscreenwork_async = new DualScreenWork();
                dualscreenwork_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class DualScreenWork extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void prepareView() {
        doCancelAsyncTask(preparesongview_async);
        preparesongview_async = new PrepareSongView();
        try {
            preparesongview_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class PrepareSongView extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            try {
                rendercalled = false;
                mypage.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
                songscrollview.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
                biggestscale_1col = 0f;
                biggestscale_2col = 0f;
                biggestscale_3col = 0f;

                width_scale = 0f;

                FullscreenActivity.currentSection = 0;

                testpane.removeAllViews();
                testpane1_2.removeAllViews();
                testpane2_2.removeAllViews();
                testpane1_3.removeAllViews();
                testpane2_3.removeAllViews();
                testpane3_3.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            // Set up the songviews
            try {
                FullscreenActivity.songSectionsTypes = new String[FullscreenActivity.songSections.length];
                FullscreenActivity.sectionviews = new LinearLayout[FullscreenActivity.songSections.length];
                FullscreenActivity.sectionbitmaps = new Bitmap[FullscreenActivity.songSections.length];
                FullscreenActivity.sectionScaleValue = new float[FullscreenActivity.songSections.length];
                FullscreenActivity.sectionrendered = new boolean[FullscreenActivity.songSections.length];
                FullscreenActivity.viewwidth = new int[FullscreenActivity.songSections.length];
                FullscreenActivity.viewheight = new int[FullscreenActivity.songSections.length];
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

        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // For stage mode, each section gets its own box
                    // For performance mode, all the sections get added into the one box

                    if (column1_1 != null) {
                        column1_1.removeAllViews();
                    }
                    if (column1_2 != null) {
                        column1_2.removeAllViews();
                    }
                    if (column2_2 != null) {
                        column2_2.removeAllViews();
                    }
                    if (column1_3 != null) {
                        column1_3.removeAllViews();
                    }
                    if (column2_3 != null) {
                        column2_3.removeAllViews();
                    }
                    if (column3_3 != null) {
                        column3_3.removeAllViews();
                    }

                    column1_1 = ProcessSong.createLinearLayout(StageMode.this);
                    column1_2 = ProcessSong.createLinearLayout(StageMode.this);
                    column2_2 = ProcessSong.createLinearLayout(StageMode.this);
                    column1_3 = ProcessSong.createLinearLayout(StageMode.this);
                    column2_3 = ProcessSong.createLinearLayout(StageMode.this);
                    column3_3 = ProcessSong.createLinearLayout(StageMode.this);

                    LinearLayout section1_1;
                    LinearLayout section1_2;
                    LinearLayout section2_2;
                    LinearLayout section1_3;
                    LinearLayout section2_3;
                    LinearLayout section3_3;

                    // Go through each section
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                        // The single stage mode view
                        final LinearLayout section = ProcessSong.songSectionView(StageMode.this, x, 12.0f, false);
                        section.setClipChildren(false);
                        section.setClipToPadding(false);
                        section.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        FullscreenActivity.viewwidth[x] = section.getMeasuredWidth();
                        FullscreenActivity.viewheight[x] = section.getMeasuredHeight();

                        // The other views for 2 or 3 column mode
                        section1_1 = ProcessSong.songSectionView(StageMode.this, x, 12.0f, false);
                        column1_1.addView(section1_1);

                        if (x < FullscreenActivity.halfsplit_section) {
                            section1_2 = ProcessSong.songSectionView(StageMode.this, x, 12.0f, false);
                            column1_2.addView(section1_2);
                        } else {
                            section2_2 = ProcessSong.songSectionView(StageMode.this, x, 12.0f, false);
                            column2_2.addView(section2_2);
                        }

                        if (x < FullscreenActivity.thirdsplit_section) {
                            section1_3 = ProcessSong.songSectionView(StageMode.this, x, 12.0f, false);
                            column1_3.addView(section1_3);
                        } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                            section2_3 = ProcessSong.songSectionView(StageMode.this, x, 12.0f, false);
                            column2_3.addView(section2_3);
                        } else {
                            section3_3 = ProcessSong.songSectionView(StageMode.this, x, 12.0f, false);
                            column3_3.addView(section3_3);
                        }

                        if (FullscreenActivity.whichMode.equals("Stage")) {
                            // Stage Mode
                            resizeStageView();
                        }
                    }

                    if (FullscreenActivity.whichMode.equals("Performance")) {
                        FullscreenActivity.sectionScaleValue = new float[6];
                        FullscreenActivity.viewwidth = new int[6];
                        FullscreenActivity.viewheight = new int[6];

                        // Performance Mode
                        testpane.setClipChildren(false);
                        testpane.setClipToPadding(false);
                        testpane.addView(column1_1);
                        testpane1_2.setClipChildren(false);
                        testpane1_2.setClipToPadding(false);
                        testpane1_2.addView(column1_2);
                        testpane2_2.setClipChildren(false);
                        testpane2_2.setClipToPadding(false);
                        testpane2_2.addView(column2_2);
                        testpane1_3.setClipChildren(false);
                        testpane1_3.setClipToPadding(false);
                        testpane1_3.addView(column1_3);
                        testpane2_3.setClipChildren(false);
                        testpane2_3.setClipToPadding(false);
                        testpane2_3.addView(column2_3);
                        testpane3_3.setClipChildren(false);
                        testpane3_3.setClipToPadding(false);
                        testpane3_3.addView(column3_3);

                        testpane.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane1_2.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane2_2.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane1_3.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane2_3.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        testpane3_3.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                        testpane.setVisibility(View.INVISIBLE);
                        testpane1_2.setVisibility(View.INVISIBLE);
                        testpane2_2.setVisibility(View.INVISIBLE);
                        testpane1_3.setVisibility(View.INVISIBLE);
                        testpane2_3.setVisibility(View.INVISIBLE);
                        testpane3_3.setVisibility(View.INVISIBLE);

                        resizePerformanceView();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resizeStageView() {
        doCancelAsyncTask(resizestage_async);
        resizestage_async = new ResizeStageView();
        try {
            resizestage_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ResizeStageView extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            try {
                // Remove the views from the test panes if there was any!
                testpane.removeAllViews();
                testpane1_2.removeAllViews();
                testpane2_2.removeAllViews();
                testpane1_3.removeAllViews();
                testpane2_3.removeAllViews();
                testpane3_3.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                // Decide on the best scaling
                FullscreenActivity.padding = getPixelsFromDpi(16);
                int availablewidth_1col = getAvailableWidth() - getPixelsFromDpi(16);
                int availableheight = (int) (FullscreenActivity.stagemodeScale * getAvailableHeight()) - getPixelsFromDpi(16);

                for (int f = 0; f < FullscreenActivity.sectionviews.length; f++) {
                    float myscale_1_1_col_x = availablewidth_1col / (float) FullscreenActivity.viewwidth[f];
                    float myscale_1_1_col_y = availableheight / (float) FullscreenActivity.viewheight[f];
                    FullscreenActivity.sectionScaleValue[f] = ProcessSong.getStageScaleValue(myscale_1_1_col_x, myscale_1_1_col_y);
                    float maxscale = FullscreenActivity.mMaxFontSize / 12.0f;
                    if (FullscreenActivity.sectionScaleValue[f] > maxscale) {
                        FullscreenActivity.sectionScaleValue[f] = maxscale;
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
        public void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    FullscreenActivity.scalingfiguredout = true;

                    // Now render the scaled song!
                    createStageView1col();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createStageView1col() {
        doCancelAsyncTask(createstageview1col_async);
        createstageview1col_async = new CreateStageView1Col();
        try {
            createstageview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class CreateStageView1Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_1 = new LinearLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            try {
                // Only 1 column, but many sections
                column1_1 = ProcessSong.preparePerformanceColumnView(StageMode.this);
                songbit = ProcessSong.prepareStageSongBitView(StageMode.this);

                // Add the song sections...
                for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                    float fontsize = ProcessSong.setScaledFontSize(x);
                    LinearLayout sectionview = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                    sectionview.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    LinearLayout boxbit = ProcessSong.prepareStageBoxView(StageMode.this, 0, FullscreenActivity.padding);
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(getAvailableWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp.setMargins(0, 0, 0, getPixelsFromDpi(4));
                    boxbit.setLayoutParams(llp);
                    boxbit.addView(sectionview);
                    column1_1.addView(boxbit);
                    boxbit.setAlpha(0.5f);
                    FullscreenActivity.sectionviews[x] = boxbit;
                    final int finalX = x;
                    FullscreenActivity.sectionviews[x].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectSection(finalX);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
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
                    songscrollview.removeAllViews();
                    songbit.addView(column1_1);
                    songscrollview.addView(songbit);
                    if (FullscreenActivity.sectionviews[0] != null) {
                        // Make the first section active (full alpha)
                        try {
                            FullscreenActivity.sectionviews[0].setAlpha(1.0f);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    animateInSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resizePerformanceView() {
        doCancelAsyncTask(resizeperformance_async);
        resizeperformance_async = new ResizePerformanceView();
        try {
            resizeperformance_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ResizePerformanceView extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            try {
                // Set the view widths and heights
                FullscreenActivity.viewwidth[0] = testpane.getMeasuredWidth();
                FullscreenActivity.viewwidth[1] = testpane1_2.getMeasuredWidth();
                FullscreenActivity.viewwidth[2] = testpane2_2.getMeasuredWidth();
                FullscreenActivity.viewwidth[3] = testpane1_3.getMeasuredWidth();
                FullscreenActivity.viewwidth[4] = testpane2_3.getMeasuredWidth();
                FullscreenActivity.viewwidth[5] = testpane3_3.getMeasuredWidth();
                FullscreenActivity.viewheight[0] = testpane.getMeasuredHeight();
                FullscreenActivity.viewheight[1] = testpane1_2.getMeasuredHeight();
                FullscreenActivity.viewheight[2] = testpane2_2.getMeasuredHeight();
                FullscreenActivity.viewheight[3] = testpane1_3.getMeasuredHeight();
                FullscreenActivity.viewheight[4] = testpane2_3.getMeasuredHeight();
                FullscreenActivity.viewheight[5] = testpane3_3.getMeasuredHeight();

                // Remove the views from the test panes
                testpane.removeAllViews();
                testpane1_2.removeAllViews();
                testpane2_2.removeAllViews();
                testpane1_3.removeAllViews();
                testpane2_3.removeAllViews();
                testpane3_3.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                // Decide on the best scaling
                FullscreenActivity.padding = getPixelsFromDpi(6);
                int availablewidth_1col = getAvailableWidth() - FullscreenActivity.padding;
                int availablewidth_2col = (int) (getAvailableWidth() / 2.0f) - getPixelsFromDpi(12 + 4);
                int availablewidth_3col = (int) (getAvailableWidth() / 3.0f) - getPixelsFromDpi(18 + 4 + 4);
                int availableheight = getAvailableHeight() - getPixelsFromDpi(12);

                float myscale_1_1_col_x = (float) availablewidth_1col / (float) FullscreenActivity.viewwidth[0];
                width_scale = myscale_1_1_col_x;
                float myscale_1_2_col_x = (float) availablewidth_2col / (float) FullscreenActivity.viewwidth[1];
                float myscale_2_2_col_x = (float) availablewidth_2col / (float) FullscreenActivity.viewwidth[2];
                float myscale_1_3_col_x = (float) availablewidth_3col / (float) FullscreenActivity.viewwidth[3];
                float myscale_2_3_col_x = (float) availablewidth_3col / (float) FullscreenActivity.viewwidth[4];
                float myscale_3_3_col_x = (float) availablewidth_3col / (float) FullscreenActivity.viewwidth[5];
                float myscale_1_1_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[0];
                float myscale_1_2_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[1];
                float myscale_2_2_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[2];
                float myscale_1_3_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[3];
                float myscale_2_3_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[4];
                float myscale_3_3_col_y = (float) availableheight / (float) FullscreenActivity.viewheight[5];

                FullscreenActivity.sectionScaleValue[0] = ProcessSong.getScaleValue(myscale_1_1_col_x, myscale_1_1_col_y, 12.0f);
                FullscreenActivity.sectionScaleValue[1] = ProcessSong.getScaleValue(myscale_1_2_col_x, myscale_1_2_col_y, 12.0f);
                FullscreenActivity.sectionScaleValue[2] = ProcessSong.getScaleValue(myscale_2_2_col_x, myscale_2_2_col_y, 12.0f);
                FullscreenActivity.sectionScaleValue[3] = ProcessSong.getScaleValue(myscale_1_3_col_x, myscale_1_3_col_y, 12.0f);
                FullscreenActivity.sectionScaleValue[4] = ProcessSong.getScaleValue(myscale_2_3_col_x, myscale_2_3_col_y, 12.0f);
                FullscreenActivity.sectionScaleValue[5] = ProcessSong.getScaleValue(myscale_3_3_col_x, myscale_3_3_col_y, 12.0f);
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
        public void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    float myscale;
                    float minscale = FullscreenActivity.mMinFontSize / 12.0f;
                    float maxscale = FullscreenActivity.mMaxFontSize / 12.0f;
                    float nonscaled = FullscreenActivity.mFontSize / 12.0f;

                    float minscale1col = FullscreenActivity.sectionScaleValue[0];

                    // Now we need to decide on the scale values to use and which view style we are going for.
                    // First up, if we are going for full scaling..

                    if (FullscreenActivity.thissong_scale.equals("Y")) {
                        float minscale2col = FullscreenActivity.sectionScaleValue[1];
                        float minscale3col = FullscreenActivity.sectionScaleValue[3];

                        // Decide if the other columns are smaller
                        if (FullscreenActivity.sectionScaleValue[2] < minscale2col) {
                            minscale2col = FullscreenActivity.sectionScaleValue[2];
                        }

                        if (FullscreenActivity.sectionScaleValue[4] < minscale3col) {
                            minscale3col = FullscreenActivity.sectionScaleValue[4];
                        }
                        if (FullscreenActivity.sectionScaleValue[5] < minscale3col) {
                            minscale3col = FullscreenActivity.sectionScaleValue[5];
                        }

                        // We will prefer the view with the biggest scaling
                        FullscreenActivity.myToastMessage = "";
                        myscale = minscale1col;
                        coltouse = 1;
                        if (minscale2col > myscale) {
                            myscale = minscale2col;
                            coltouse = 2;
                        }
                        if (minscale3col > myscale) {
                            myscale = minscale3col;
                            coltouse = 3;
                        }

                        // All is good unless we have exceeded the prefered max scale
                        if (myscale > maxscale) {
                            myscale = maxscale;
                        }

                        // All is good, unless myscale is below the minimum size and overriding is on!
                        if (myscale < minscale && FullscreenActivity.override_fullscale) {
                            //Set to width only
                            FullscreenActivity.thissong_scale = "W";
                            FullscreenActivity.myToastMessage = getString(R.string.override_fullautoscale);
                            FullscreenActivity.sectionScaleValue[0] = width_scale;
                            coltouse = 1;
                        }

                        if (myscale < minscale && FullscreenActivity.override_widthscale && !FullscreenActivity.override_fullscale) {
                            //Set to non scaled
                            FullscreenActivity.thissong_scale = "N";
                            FullscreenActivity.myToastMessage = getString(R.string.override_widthautoscale);
                            FullscreenActivity.sectionScaleValue[0] = nonscaled;
                            coltouse = 1;
                        }
                    }

                    // If we are autoscaling to width only...
                    if (FullscreenActivity.thissong_scale.equals("W")) {
                        myscale = width_scale;

                        // Check we haven't exceeded the max scale preference
                        if (myscale > maxscale) {
                            myscale = maxscale;
                        }

                        FullscreenActivity.sectionScaleValue[0] = myscale;
                        coltouse = 1;

                        // All is good, unless myscale is below the minimum size and overriding is on!
                        if (myscale < minscale && FullscreenActivity.override_widthscale) {
                            //Set to scaling off
                            FullscreenActivity.thissong_scale = "N";
                            FullscreenActivity.myToastMessage = getString(R.string.override_widthautoscale);
                            FullscreenActivity.sectionScaleValue[0] = nonscaled;
                            coltouse = 1;
                        }
                    }

                    // If autoscaling is off...
                    if (FullscreenActivity.thissong_scale.equals("N")) {
                        coltouse = 1;
                        FullscreenActivity.sectionScaleValue[0] = nonscaled;
                    }

                    FullscreenActivity.scalingfiguredout = true;
                    ShowToast.showToast(StageMode.this);

                    // Now render the scaled song!
                    if (coltouse == 1) {
                        createPerformanceView1col();
                    } else if (coltouse == 2) {
                        createPerformanceView2col();
                    } else {
                        createPerformanceView3col();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public int getAvailableWidth() {
        int val;
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        Method mGetRawW;

        try {
            // For JellyBeans and onward
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                display.getRealMetrics(metrics);
                val = metrics.widthPixels;
                FullscreenActivity.scalingDensity = metrics.densityDpi;

            } else {
                // Below Jellybeans you can use reflection method
                mGetRawW = Display.class.getMethod("getRawWidth");

                try {
                    val = (Integer) mGetRawW.invoke(display);
                } catch (Exception e) {
                    e.printStackTrace();
                    val = mypage.getWidth();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            val = mypage.getWidth();
        }

        FullscreenActivity.padding = getPixelsFromDpi(6);
        FullscreenActivity.myWidthAvail = val;
        return val;
    }

    public int getAvailableHeight() {
        int val;
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        Method mGetRawH;

        try {
            // For JellyBeans and onward
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                display.getRealMetrics(metrics);
                val = metrics.heightPixels;
            } else {
                // Below Jellybeans you can use reflection method
                mGetRawH = Display.class.getMethod("getRawHeight");

                try {
                    val = (Integer) mGetRawH.invoke(display);
                } catch (Exception e) {
                    e.printStackTrace();
                    val = mypage.getHeight();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            val = mypage.getHeight();
        }

        if (!FullscreenActivity.hideActionBar) {
            FullscreenActivity.ab_height = ab.getHeight();
            val = val - ab.getHeight();
        }
        FullscreenActivity.myHeightAvail = val;

        return val;
    }

    public int getPixelsFromDpi(int dps) {
        return dps * (int) (getResources().getDisplayMetrics().densityDpi / 160f);
    }

    @Override
    public void showToastMessage(String message) {
        if (message != null && !message.isEmpty()) {
            FullscreenActivity.myToastMessage = message;
            ShowToast.showToast(StageMode.this);
        }
    }

    public void showSticky() {
        doCancelAsyncTask(show_sticky);
        show_sticky = new ShowSticky();
        try {
            show_sticky.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ShowSticky extends AsyncTask<Object, Void, String> {

        long stickycurrtime;
        long stickytimetohide;
        boolean stickydonthide;

        @Override
        protected void onPreExecute() {
            try {
                // If the sticky notes were already showing, close them
                if (stickyPopUpWindow != null && stickyPopUpWindow.isShowing()) {
                    try {
                        stickyPopUpWindow.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Open the sticky note window up again
                displaySticky();

                // Get the current time
                stickycurrtime = System.currentTimeMillis();

                // Set the time to close the sticky note
                stickytimetohide = stickycurrtime + (FullscreenActivity.stickyNotesShowSecs * 1000);

                if (FullscreenActivity.stickyNotesShowSecs == 0) {
                    stickydonthide = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (!stickydonthide && !FullscreenActivity.mNotes.equals("")) {
                    while (System.currentTimeMillis() < stickytimetohide) {
                        // Do nothing
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
                    if (!stickydonthide) {
                        // If there is a sticky note showing, remove it
                        if (stickyPopUpWindow != null && stickyPopUpWindow.isShowing()) {
                            try {
                                stickyPopUpWindow.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void displaySticky() {
        if (FullscreenActivity.mNotes!=null && !FullscreenActivity.mNotes.isEmpty() && !FullscreenActivity.mNotes.equals("")) {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            @SuppressLint("InflateParams")
            final View popupView = layoutInflater.inflate(R.layout.popup_float_sticky, null);
            // Decide on the popup position
            int sw = getAvailableWidth();
            int hp = sw - FullscreenActivity.stickyWidth - (int)((float) setButton.getMeasuredWidth()*1.2f);
            if (hp<0) { hp = 0;}
            int vp = (int) ((float) ab_toolbar.getMeasuredHeight() * 1.2f);
            if (vp<0) { vp = 0;}
            stickyPopUpWindow = new PopupWindow(popupView);
            stickyPopUpWindow.setFocusable(false);
            stickyPopUpWindow.setWidth(FullscreenActivity.stickyWidth);
            stickyPopUpWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            stickyPopUpWindow.setContentView(popupView);
            FloatingActionButton closeStickyFloat = popupView.findViewById(R.id.closeMe);
            LinearLayout myTitle = popupView.findViewById(R.id.myTitle);
            TextView mySticky = popupView.findViewById(R.id.mySticky);
            mySticky.setTextColor(FullscreenActivity.stickytextColor);
            mySticky.setTextSize(FullscreenActivity.stickyTextSize);
            mySticky.setText(FullscreenActivity.mNotes);
            popupView.setBackgroundResource(R.drawable.popup_sticky);
            GradientDrawable drawable = (GradientDrawable) popupView.getBackground();
            drawable.setColor(FullscreenActivity.stickybgColor);
            popupView.setPadding(10,10,10,10);
            stickyPopUpWindow.showAtLocation(mypage, Gravity.TOP | Gravity.LEFT, hp, vp);
            RelativeLayout stickyfloat = popupView.findViewById(R.id.stickyfloat);
            stickyfloat.setAlpha(FullscreenActivity.stickyOpacity);
            closeStickyFloat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // If there is a sticky note showing, remove it
                    if (stickyPopUpWindow!=null && stickyPopUpWindow.isShowing()) {
                        try {
                            stickyPopUpWindow.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            myTitle.setOnTouchListener(new View.OnTouchListener() {
                int orgX, orgY;
                int offsetX, offsetY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            orgX = (int) event.getX();
                            orgY = (int) event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            offsetX = (int) event.getRawX() - orgX;
                            offsetY = (int) event.getRawY() - orgY;
                            stickyPopUpWindow.update(offsetX, offsetY, -1, -1, true);
                            break;
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void takeScreenShot() {
        // This option is hidden (Song Menu/Highlight) unless we are in Performance Mode
        if (!FullscreenActivity.whichMode.equals("Performance")) {
            FullscreenActivity.highlightOn = false;
            highlightNotes.setVisibility(View.GONE);
            FullscreenActivity.myToastMessage = getString(R.string.switchtoperformmode);
            ShowToast.showToast(StageMode.this);
        } else {
            if (!FullscreenActivity.thissong_scale.equals("Y")) {
                FullscreenActivity.myToastMessage = getString(R.string.highlight_notallowed);
                ShowToast.showToast(StageMode.this);
                FullscreenActivity.bmScreen = null;
            } else {
                boolean vis = false;
                if (highlightNotes!=null && highlightNotes.getVisibility()==View.VISIBLE) {
                    vis = true;
                }

                if (vis) {
                    highlightNotes.setVisibility(View.GONE);
                }
                if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                    glideimage_ScrollView.destroyDrawingCache();
                    glideimage_ScrollView.setDrawingCacheEnabled(true);
                    glideimage_ScrollView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
                    FullscreenActivity.bmScreen = null;
                    try {
                        FullscreenActivity.bmScreen = glideimage_ScrollView.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
                    } catch (Exception e) {
                        Log.d("d", "error getting the screenshot!");
                    } catch (OutOfMemoryError e) {
                        Log.d("d","not enough memory");
                    }

                } else {
                    songscrollview.destroyDrawingCache();
                    songscrollview.setDrawingCacheEnabled(true);
                    songscrollview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
                    FullscreenActivity.bmScreen = null;
                    try {
                        FullscreenActivity.bmScreen = songscrollview.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
                    } catch (Exception e) {
                        Log.d("d", "error getting the screenshot!");
                    } catch (OutOfMemoryError o) {
                        Log.d("d","Out of memory");
                    }

                }
                if (vis) {
                    highlightNotes.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    public boolean shouldHighlightsBeShown() {
        // This is the check before animating in the highlight notes.
        File f = ProcessSong.getHighlightFile(StageMode.this);
        return (FullscreenActivity.highlightOn || FullscreenActivity.toggleAutoHighlight) &&
                f.exists() && FullscreenActivity.whichMode.equals("Performance");
    }
    public void showHighlight() {
        doCancelAsyncTask(show_highlight);
        show_highlight = new ShowHighlight(true);
        try {
            show_highlight.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ShowHighlight extends AsyncTask<Object, Void, String> {

        long highlightcurrtime;
        long highlighttimetohide;
        boolean highlightdonthide;
        boolean fromautoshow;

        ShowHighlight(boolean fa) {
            fromautoshow = fa;
        }

        @Override
        protected void onPreExecute() {
            try {
                if (shouldHighlightsBeShown()) {
                    // Open the highlight note window up again
                    displayHighlight(fromautoshow);

                    // Get the current time
                    highlightcurrtime = System.currentTimeMillis();

                    // Set the time to close the sticky note
                    highlighttimetohide = highlightcurrtime + (FullscreenActivity.highlightShowSecs * 1000);

                    if (FullscreenActivity.highlightShowSecs == 0) {
                        highlightdonthide = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (shouldHighlightsBeShown() && !highlightdonthide) {
                    while (System.currentTimeMillis() < highlighttimetohide) {
                        // Do nothing
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
                    if (!highlightdonthide && highlightNotes.getVisibility() == View.VISIBLE) {
                        // If there is a highlight note showing, remove it
                        highlightNotes.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    public void displayHighlight(boolean fromautoshow) {
        if (!FullscreenActivity.whichMode.equals("Performance")) {
            FullscreenActivity.highlightOn = false;
            highlightNotes.setVisibility(View.GONE);
            if (!fromautoshow) {
                // Don't show the warning just because the app tries to autoshow it
                FullscreenActivity.myToastMessage = getString(R.string.switchtoperformmode);
                ShowToast.showToast(StageMode.this);
            }
        } else {
            // If we are trying to show notes, but they are already open, close them
            // This is only if a manual click on the hightlight button happened
            // Are the notes visible?
            if (highlightNotes.getVisibility() == View.VISIBLE && !fromautoshow) {
                // Hide it
                FullscreenActivity.highlightOn = false;
                highlightNotes.setVisibility(View.GONE);
            } else if (FullscreenActivity.thissong_scale.equals("Y")) {
                File file = ProcessSong.getHighlightFile(StageMode.this);
                if (file != null && file.exists()) {
                    // Load the image in if it exists and then show it
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    try {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.toString(), options);
                        Bitmap canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        bitmap.recycle();
                        RelativeLayout.LayoutParams rlp;
                        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                            rlp = new RelativeLayout.LayoutParams(glideimage_ScrollView.getMeasuredWidth(),
                                    glideimage_ScrollView.getMeasuredHeight());
                        } else {
                            rlp = new RelativeLayout.LayoutParams(songscrollview.getMeasuredWidth(),
                                    songscrollview.getMeasuredHeight());
                        }
                        if (FullscreenActivity.hideActionBar) {
                            rlp.addRule(RelativeLayout.BELOW, 0);
                        } else {
                            rlp.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
                        }
                        highlightNotes.setLayoutParams(rlp);

                        highlightNotes.setImageBitmap(canvasBitmap);
                        highlightNotes.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        //highlightNotes.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        //highlightNotes.setScaleType(ImageView.ScaleType.CENTER);
                        //highlightNotes.setScaleType(ImageView.ScaleType.FIT_START);
                        //highlightNotes.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        //highlightNotes.setScaleType(ImageView.ScaleType.FIT_END);
                        if (!fromautoshow) {
                            // If user manually wanted to show, otherwise song load animates it in
                            highlightNotes.setVisibility(View.VISIBLE);
                        } else {
                            highlightNotes.setVisibility(View.VISIBLE);
                            if (FullscreenActivity.whichDirection.equals("L2R")) {
                                highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
                            } else {
                                highlightNotes.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
                            }
                        }
                    } catch (OutOfMemoryError | Exception e) {
                        e.printStackTrace();
                        Log.d("d","Oops - error, likely too big an image!");
                    }
                    FullscreenActivity.highlightOn = true;
                } else {
                    if (!fromautoshow) {
                        // If the user has clicked the icon and no drawing exists, create one
                        takeScreenShot();
                        if (FullscreenActivity.bmScreen != null) {
                            FullscreenActivity.whattodo = "drawnotes";
                            openFragment();
                        } else {
                            Log.d("d", "screenshot is null");
                        }
                    }
                    FullscreenActivity.highlightOn = false;
                }
            } else {
                if (!fromautoshow) {
                    // Don't show the warning just because the app tries to autoshow it
                    FullscreenActivity.myToastMessage = getString(R.string.highlight_notallowed);
                    ShowToast.showToast(StageMode.this);
                }
                FullscreenActivity.highlightOn = false;
            }
        }
    }

    @Override
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
        protected String doInBackground(Object... params) {
            try {
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
                // List all of the songs in the current folder
                ListSongFiles.getAllSongFolders();
                ListSongFiles.getAllSongFiles();
                ListSongFiles.getSongDetails(StageMode.this);
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

                    SongMenuAdapter lva = new SongMenuAdapter(StageMode.this, songmenulist);
                    song_list_view.setAdapter(lva);
                    song_list_view.setFastScrollEnabled(true);
                    song_list_view.setScrollingCacheEnabled(true);
                    lva.notifyDataSetChanged();

                    // Set the secondary alphabetical side bar
                    SongMenuAdapter.getIndexList(StageMode.this);
                    displayIndex();

                    // Listen for long clicks in the song menu (songs only, not folders) - ADD TO SET!!!!
                    //song_list_view.setOnItemLongClickListener(SongMenuListeners.myLongClickListener(StageMode.this));

                    // Listen for short clicks in the song menu (songs only, not folders) - OPEN SONG!!!!
                    //song_list_view.setOnItemClickListener(SongMenuListeners.myShortClickListener(StageMode.this));

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
            textView = (TextView) View.inflate(StageMode.this,
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
                try {
                    optionmenu.removeAllViews();
                } catch (Exception e) {
                    Log.d("d", "Error removing view");
                }
                optionmenu.addView(OptionMenuListeners.prepareOptionMenu(StageMode.this));
                if (optionmenu != null) {
                    OptionMenuListeners.optionListeners(optionmenu, StageMode.this);
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
            OptionMenuListeners.updateMenuVersionNumber(StageMode.this, (TextView) findViewById(R.id.menu_version_bottom));
        }

    }

    @Override
    public void songLongClick() {
        // Rebuild the set list as we've just added a song
        SetActions.prepareSetList();
        prepareOptionMenu();
        prepareSongMenu();
        closeMyDrawers("song");
    }

    @Override
    public void fixSet() {
        // This is only used for the PresenterMode
    }
    @Override
    public void songShortClick(int mychild) {
        // Scroll to this song in the song menu
        mychild = mychild - FullscreenActivity.numDirs;
        song_list_view.smoothScrollToPosition(mychild);

        // Close both drawers
        closeMyDrawers("both");

        // Load the song
        loadSong();
    }

    @Override
    public void removeSongFromSet(int val) {
        // Vibrate to let the user know something happened
        DoVibrate.vibrate(StageMode.this, 50);

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

        // Save set
        Preferences.savePreferences();

        closeMyDrawers("option");
    }

    @SuppressWarnings("all")
    @Override
    public void showActionBar() {
        if (FullscreenActivity.hideActionBar) {
            // Make the songscrollview not sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((HorizontalScrollView)findViewById(R.id.horizontalscrollview)).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) ((ScrollView)findViewById(R.id.glideimage_ScrollView)).getLayoutParams();
            lp2.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.glideimage_ScrollView).setLayoutParams(lp2);
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) ((ImageView)findViewById(R.id.highlightNotes)).getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.highlightNotes).setLayoutParams(lp3);
            //RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.pagebuttons).getLayoutParams();
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, 0);
            //findViewById(R.id.pagebuttons).setLayoutParams(lp4);
            findViewById(R.id.capoInfo).setLayoutParams(lp4);
        } else {
            // Make the songscrollview sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((HorizontalScrollView)findViewById(R.id.horizontalscrollview)).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) ((ScrollView)findViewById(R.id.glideimage_ScrollView)).getLayoutParams();
            lp2.addRule(RelativeLayout.BELOW,  ab_toolbar.getId());
            findViewById(R.id.glideimage_ScrollView).setLayoutParams(lp2);
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) ((ImageView)findViewById(R.id.highlightNotes)).getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW,  ab_toolbar.getId());
            findViewById(R.id.highlightNotes).setLayoutParams(lp3);
            //RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.pagebuttons).getLayoutParams();
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            //findViewById(R.id.pagebuttons).setLayoutParams(lp4);
            findViewById(R.id.capoInfo).setLayoutParams(lp4);
        }
        toggleActionBar();
    }

    @SuppressWarnings("all")
    @Override
    public void hideActionBar() {
        if (FullscreenActivity.hideActionBar) {
            // Make the songscrollview not sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((HorizontalScrollView)findViewById(R.id.horizontalscrollview)).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) ((ScrollView)findViewById(R.id.glideimage_ScrollView)).getLayoutParams();
            lp2.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.glideimage_ScrollView).setLayoutParams(lp2);
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) ((ImageView)findViewById(R.id.highlightNotes)).getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.highlightNotes).setLayoutParams(lp3);
            //RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.pagebuttons).getLayoutParams();
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, 0);
            //findViewById(R.id.pagebuttons).setLayoutParams(lp4);
            findViewById(R.id.capoInfo).setLayoutParams(lp4);
        } else {
            // Make the songscrollview sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((HorizontalScrollView)findViewById(R.id.horizontalscrollview)).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) ((ScrollView)findViewById(R.id.glideimage_ScrollView)).getLayoutParams();
            lp2.addRule(RelativeLayout.BELOW,  ab_toolbar.getId());
            findViewById(R.id.glideimage_ScrollView).setLayoutParams(lp2);
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) ((ImageView)findViewById(R.id.highlightNotes)).getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW,  ab_toolbar.getId());
            findViewById(R.id.highlightNotes).setLayoutParams(lp3);
            //RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.pagebuttons).getLayoutParams();
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) findViewById(R.id.capoInfo).getLayoutParams();
            lp4.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            //findViewById(R.id.pagebuttons).setLayoutParams(lp4);
            findViewById(R.id.capoInfo).setLayoutParams(lp4);
        }
        toggleActionBar();
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
    public void rebuildSearchIndex() {
        doCancelAsyncTask(indexsongs_task);
        indexsongs_task = new IndexSongs.IndexMySongs(StageMode.this);
        indexsongs_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    ShowToast.showToast(StageMode.this);
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
    public void openFragment() {
        // Load the whichSongFolder in case we were browsing elsewhere
        Preferences.loadFolderName();

        // Initialise the newFragment
        newFragment = OpenFragment.openFragment(StageMode.this);
        String message = OpenFragment.getMessage(StageMode.this);

        if (newFragment != null && !this.isFinishing()) {
            newFragment.show(getFragmentManager(), message);
        }
    }

    @Override
    public void useCamera() {
        if (ContextCompat.checkSelfPermission(StageMode.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(StageMode.this, new String[]{Manifest.permission.CAMERA},
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

    public void selectSection(int whichone) {
        FullscreenActivity.currentSection = whichone;

        // Send section to other devices (checks we are in stage or presentation mode in called method
        if (FullscreenActivity.network != null && FullscreenActivity.network.isRunningAsHost) {
            try {
                sendSongSectionToConnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Set this sections alpha to 1.0f;
        try {
            FullscreenActivity.sectionviews[whichone].setAlpha(1.0f);
        } catch (Exception e) {
            Log.d("d","Section not found");
        }

        // Smooth scroll to show this view at the top of the page
        // Unless we are autoscrolling
        try {
            if (!FullscreenActivity.isautoscrolling) {
                songscrollview.smoothScrollTo(0, FullscreenActivity.sectionviews[whichone].getTop());
            }
        } catch (Exception e) {
                Log.d("d","Section not found");
        }

            // Go through each of the views and set the alpha of the others to 0.5f;
        for (int x = 0; x < FullscreenActivity.sectionviews.length; x++) {
            if (x != whichone) {
                FullscreenActivity.sectionviews[x].setAlpha(0.5f);
            }
        }
        FullscreenActivity.tempswipeSet = "enable";
        FullscreenActivity.setMoveDirection = "";

        dualScreenWork();
    }

    @Override
    public void splashScreen() {
        /*SharedPreferences settings = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("showSplashVersion", 0);
        editor.apply();
        */
        FullscreenActivity.showSplashVersion = 0;
        Preferences.savePreferences();
        Intent intent = new Intent();
        intent.putExtra("showsplash",true);
        intent.setClass(StageMode.this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void confirmedAction() {
        switch (FullscreenActivity.whattodo) {
            case "exit":
                try {
                    //this.finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                } catch (Exception e) {
                   Log.d("d","Couldn't close the application!") ;
                }
                break;

            case "saveset":
                // Save the set
                SetActions.saveSetMessage(StageMode.this);
                refreshAll();
                break;

            case "clearset":
                // Clear the set
                SetActions.clearSet(StageMode.this);
                refreshAll();
                break;

            case "deletesong":
                // Delete current song
                ListSongFiles.deleteSong(StageMode.this);
                Preferences.savePreferences();
                refreshAll();
                break;

            case "deleteset":
                // Delete set
                SetActions.deleteSet(StageMode.this);
                refreshAll();
                break;

            case "wipeallsongs":
                // Wipe all songs
                if (!ListSongFiles.clearAllSongs()) {
                    Log.d("d","Problem clearing songs");
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
    public void preparePad() {
        backingtrackProgress.setVisibility(View.VISIBLE);
        doCancelAsyncTask(prepare_pad);
        prepare_pad = new PreparePad();
        try {
            prepare_pad.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class PreparePad extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                FullscreenActivity.padson = true;
                PadFunctions.getPad1Status();
                PadFunctions.getPad2Status();

                if (FullscreenActivity.pad1Playing) {
                    // If mPlayer1 is already playing, set this to fade out and start mPlayer2
                    FullscreenActivity.pad1Fading = true;
                    FullscreenActivity.pad2Fading = false;
                    FullscreenActivity.whichPad = 2;
                    FullscreenActivity.padson = true;

                } else if (FullscreenActivity.pad2Playing) {
                    // If mPlayer2 is already playing, set this to fade out and start mPlayer1
                    FullscreenActivity.pad1Fading = false;
                    FullscreenActivity.pad2Fading = true;
                    FullscreenActivity.padson = true;
                    FullscreenActivity.whichPad = 1;

                } else {
                    // Else nothing, was playing, so start mPlayer1
                    FullscreenActivity.pad1Fading = false;
                    FullscreenActivity.pad2Fading = false;
                    FullscreenActivity.whichPad = 1;
                    FullscreenActivity.padson = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return FullscreenActivity.whichPad;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(Integer i) {
            try {
                if (!cancelled) {
                    if (FullscreenActivity.pad1Fading) {
                        FullscreenActivity.fadeWhichPad = 1;
                        fadeoutPad();
                    }
                    if (FullscreenActivity.pad2Fading) {
                        FullscreenActivity.fadeWhichPad = 2;
                        fadeoutPad();
                    }
                    try {
                        playPads(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Player1Prepared implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            FullscreenActivity.padtime_length = (int) (FullscreenActivity.mPlayer1.getDuration() / 1000.0f);
            FullscreenActivity.mPlayer1.setLooping(PadFunctions.getLoop(StageMode.this));
            FullscreenActivity.mPlayer1.setVolume(PadFunctions.getVol(0), PadFunctions.getVol(1));
            FullscreenActivity.mPlayer1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!PadFunctions.getLoop(StageMode.this)) {
                        Log.d("d", "Reached end and not looping");

                    } else {
                        Log.d("d", "Reached end but looping");
                    }
                }
            });
            String text = TimeTools.timeFormatFixer(FullscreenActivity.padtime_length);
            try {
                padtotalTime_TextView.setTextSize(FullscreenActivity.timerFontSizePad);
                padTimeSeparator_TextView.setTextSize(FullscreenActivity.timerFontSizePad);
                padcurrentTime_TextView.setTextSize(FullscreenActivity.timerFontSizePad);
                padcurrentTime_TextView.setText(getString(R.string.zerotime));
                padtotalTime_TextView.setText(text);
            } catch (Exception e) {
                e.printStackTrace(); // If called from doInBackground()
            }
            FullscreenActivity.mPlayer1.start();
            FullscreenActivity.padson = true;
            dopadProgressTime.post(onEverySecond);
        }
    }

    private class Player2Prepared implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            FullscreenActivity.padtime_length = (int) (FullscreenActivity.mPlayer2.getDuration() / 1000.0f);
            FullscreenActivity.mPlayer2.setLooping(PadFunctions.getLoop(StageMode.this));
            FullscreenActivity.mPlayer2.setVolume(PadFunctions.getVol(0), PadFunctions.getVol(1));
            FullscreenActivity.mPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!PadFunctions.getLoop(StageMode.this)) {
                        Log.d("d", "Reached end and not looping");

                    } else {
                        Log.d("d", "Reached end but looping");
                    }
                }
            });
            String text = TimeTools.timeFormatFixer(FullscreenActivity.padtime_length);
            try {
                padtotalTime_TextView.setTextSize(FullscreenActivity.timerFontSizePad);
                padTimeSeparator_TextView.setTextSize(FullscreenActivity.timerFontSizePad);
                padcurrentTime_TextView.setTextSize(FullscreenActivity.timerFontSizePad);
                padcurrentTime_TextView.setText(getString(R.string.zerotime));
                padtotalTime_TextView.setText(text);
            } catch (Exception e) {
                e.printStackTrace(); // If called from doInBackground()
            }
            FullscreenActivity.mPlayer2.start();
            FullscreenActivity.padson = true;
            dopadProgressTime.post(onEverySecond);
        }
    }

    public void playPads(int which) {
        doCancelAsyncTask(play_pads);
        play_pads = new PlayPads(which);
        try {
            play_pads.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class PlayPads extends AsyncTask<Void, Void, Integer> {
        int which;
        int path;
        boolean validlinkaudio;
        boolean error;

        PlayPads(Integer w) {
            which = w;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                ProcessSong.processKey();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return which;
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(Integer i) {
            try {
                if (!cancelled) {
                    if (FullscreenActivity.mPadFile.equals(getResources().getString(R.string.pad_auto)) ||
                            FullscreenActivity.mPadFile.equals("")) {

                        FullscreenActivity.padson = true;
                        if (FullscreenActivity.pad_filename != null && FullscreenActivity.mKey != null) {
                            path = getResources().getIdentifier(FullscreenActivity.pad_filename, "raw", getPackageName());
                            AssetFileDescriptor afd = null;
                            try {
                                afd = getResources().openRawResourceFd(path);
                            } catch (Exception e) {
                                e.printStackTrace();
                                which = 0;
                            }
                            if (which == 1) {
                                try {
                                    PadFunctions.getPad1Status();
                                    if (FullscreenActivity.pad1Playing) {
                                        FullscreenActivity.mPlayer1.stop();
                                    }
                                    FullscreenActivity.mPlayer1.reset();
                                    FullscreenActivity.mPlayer1.setOnPreparedListener(new Player1Prepared());
                                    FullscreenActivity.mPlayer1.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                    afd.close();
                                    FullscreenActivity.mPlayer1.prepareAsync();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    FullscreenActivity.fadeWhichPad = 0;
                                    FullscreenActivity.whichPad = 0;
                                    killPad();
                                }
                            } else if (which == 2) {
                                try {
                                    PadFunctions.getPad2Status();
                                    if (FullscreenActivity.pad2Playing) {
                                        FullscreenActivity.mPlayer2.stop();
                                    }
                                    FullscreenActivity.mPlayer2.reset();
                                    FullscreenActivity.mPlayer2.setOnPreparedListener(new Player2Prepared());
                                    FullscreenActivity.mPlayer2.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                    afd.close();
                                    FullscreenActivity.mPlayer2.prepareAsync();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    FullscreenActivity.fadeWhichPad = 0;
                                    FullscreenActivity.whichPad = 0;
                                    killPad();
                                }
                            }

                        } else {
                            // No key specified in the song - play nothing
                            FullscreenActivity.myToastMessage = getResources().getString(R.string.pad_error);
                            FullscreenActivity.padson = false;
                            error = true;
                            FullscreenActivity.fadeWhichPad = 0;
                            FullscreenActivity.whichPad = 0;
                            killPad();
                        }
                    }

                    // Prepare the link audio file
                    if (FullscreenActivity.mPadFile.equals(getResources().getString(R.string.link_audio))) {
                        try {
                            if (which == 1) {
                                String filetext = FullscreenActivity.mLinkAudio;
                                filetext = filetext.replace("file://", "");
                                // If this is a localised file, we need to unlocalise it to enable it to be read
                                if (filetext.startsWith("../OpenSong/")) {
                                    filetext = filetext.replace("../OpenSong/", FullscreenActivity.homedir + "/");
                                }
                                // Add the file start back:
                                filetext = "file://" + filetext;
                                PadFunctions.getPad1Status();
                                if (FullscreenActivity.pad1Playing) {
                                    FullscreenActivity.mPlayer1.stop();
                                }
                                FullscreenActivity.mPlayer1.reset();
                                FullscreenActivity.mPlayer1.setOnPreparedListener(new Player1Prepared());
                                FullscreenActivity.mPlayer1.setDataSource(filetext);
                                FullscreenActivity.mPlayer1.prepareAsync();
                            } else if (which == 2) {
                                String filetext = FullscreenActivity.mLinkAudio;
                                filetext = filetext.replace("file://", "");
                                // If this is a localised file, we need to unlocalise it to enable it to be read
                                if (filetext.startsWith("../OpenSong/")) {
                                    filetext = "file://" + filetext.replace("../OpenSong/", FullscreenActivity.homedir + "/");
                                }
                                // Add the file start back:
                                filetext = "file://" + filetext;
                                PadFunctions.getPad2Status();
                                if (FullscreenActivity.pad2Playing) {
                                    FullscreenActivity.mPlayer2.stop();
                                }
                                FullscreenActivity.mPlayer2.reset();
                                FullscreenActivity.mPlayer2.setOnPreparedListener(new Player2Prepared());
                                FullscreenActivity.mPlayer2.setDataSource(filetext);
                                FullscreenActivity.mPlayer2.prepareAsync();
                            }
                            validlinkaudio = true;
                        } catch (Exception e) {
                            validlinkaudio = false;
                            Log.d("d", "Something went wrong with the media");
                        }

                        if (!validlinkaudio) {
                            // Problem with link audio so don't use it
                            FullscreenActivity.myToastMessage = getResources().getString(R.string.link_audio) + " - " +
                                    getResources().getString(R.string.file_type_unknown);
                            FullscreenActivity.padson = false;
                            error = true;
                        }
                    }

                    // No pads wanted
                    if (FullscreenActivity.mPadFile.equals(getResources().getString(R.string.off)) && FullscreenActivity.padson) {
                        // Pad shouldn't play
                        FullscreenActivity.padson = false;
                        FullscreenActivity.fadeWhichPad = 0;
                        FullscreenActivity.whichPad = 0;
                        killPad();
                    }

                    if (error) {
                        ShowToast.showToast(StageMode.this);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getPadProgress() {
        int pos;
        boolean pad1status = PadFunctions.getPad1Status();
        boolean pad2status = PadFunctions.getPad2Status();

        boolean display1 = (pad1status && !FullscreenActivity.pad1Fading) || (pad2status && FullscreenActivity.pad2Fading);
        // Decide which player we should be getting the status of
        if (display1) {
            pos = (int) (FullscreenActivity.mPlayer1.getCurrentPosition() / 1000.0f);
        } else {
            pos = (int) (FullscreenActivity.mPlayer2.getCurrentPosition() / 1000.0f);
        }
        String text = TimeTools.timeFormatFixer(pos);
        padcurrentTime_TextView.setTextSize(FullscreenActivity.timerFontSizePad);
        padTimeSeparator_TextView.setTextSize(FullscreenActivity.timerFontSizePad);
        padtotalTime_TextView.setTextSize(FullscreenActivity.timerFontSizePad);
        padcurrentTime_TextView.setText(text);

    }

    public void preparePadProgress() {
        if (FullscreenActivity.padson) {
            dopadProgressTime.post(padprogressTimeRunnable);
            dopadProgressTime.postDelayed(onEverySecond, 1000);
        }
    }

    public void getPadsOnStatus() {
        FullscreenActivity.padson = FullscreenActivity.pad1Playing || FullscreenActivity.pad2Playing;
        if (!FullscreenActivity.padson) {
            try {
                backingtrackProgress.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.d("d", "Can't touch the view - " + e);
                // This will happen if killPads was called from an async task
            }
        }
    }

    @Override
    public void fadeoutPad() {

        switch (FullscreenActivity.fadeWhichPad) {

            case 1:
                if (FullscreenActivity.pad1Playing) {
                    // mPlayer1 is playing, so fade it out.
                    doCancelAsyncTask(fadeout_media1);
                    padcurrentTime_TextView.setText(getString(R.string.zerotime));
                    fadeout_media1 = new FadeoutMediaPlayer(StageMode.this, 1);
                    fadeout_media1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;

            case 2:
                if (FullscreenActivity.pad2Playing) {
                    // mPlayer2 is playing, so fade it out.
                    doCancelAsyncTask(fadeout_media2);
                    padcurrentTime_TextView.setText(getString(R.string.zerotime));
                    fadeout_media2 = new FadeoutMediaPlayer(StageMode.this, 2);
                    fadeout_media2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;

            case 0:
                // Fade both pads
                if (FullscreenActivity.pad1Playing) {
                    // mPlayer1 is playing, so fade it out.
                    doCancelAsyncTask(fadeout_media1);
                    padcurrentTime_TextView.setText(getString(R.string.zerotime));
                    fadeout_media1 = new FadeoutMediaPlayer(StageMode.this, 1);
                    fadeout_media1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if (FullscreenActivity.pad2Playing) {
                    // mPlayer2 is playing, so fade it out.
                    doCancelAsyncTask(fadeout_media2);
                    padcurrentTime_TextView.setText(getString(R.string.zerotime));
                    fadeout_media2 = new FadeoutMediaPlayer(StageMode.this, 2);
                    fadeout_media2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
        }

        // Set a runnable to check for the pads on or off to hide the player progress
        handle.postDelayed(padoncheck, 13000); // Cross fade has finished
    }

    @Override
    public void killPad() {

        PadFunctions.getPad1Status();
        PadFunctions.getPad2Status();

        switch (FullscreenActivity.whichPad) {
            case 1:
                if (FullscreenActivity.pad1Playing) {
                    try {
                        FullscreenActivity.mPlayer1.stop();
                        FullscreenActivity.mPlayer1.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 2:
                if (FullscreenActivity.pad2Playing) {
                    try {
                        FullscreenActivity.mPlayer2.stop();
                        FullscreenActivity.mPlayer2.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 0:
            default:
                if (FullscreenActivity.pad1Playing) {
                    try {
                        FullscreenActivity.mPlayer1.stop();
                        FullscreenActivity.mPlayer1.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (FullscreenActivity.pad2Playing) {
                    try {
                        FullscreenActivity.mPlayer2.stop();
                        FullscreenActivity.mPlayer2.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        PadFunctions.getPad1Status();
        PadFunctions.getPad2Status();
        getPadsOnStatus();
        padcurrentTime_TextView.setText(getString(R.string.zerotime));
    }

    @Override
    public void prepareLearnAutoScroll() {
        FullscreenActivity.learnPreDelay = false;
        FullscreenActivity.learnSongLength = false;
        learnAutoScroll.setVisibility(View.VISIBLE);
        learnAutoScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLearnAutoScroll();
            }
        });
        String s = getString(R.string.autoscroll_time) + "\n" + getString(R.string.start);
        learnAutoScroll_TextView.setTextSize(10.0f);
        learnAutoScroll_TextView.setText(s);
        learnAutoScrollTime_TextView.setText(TimeTools.timeFormatFixer(0));
    }

    public void startLearnAutoScroll() {
        FullscreenActivity.learnPreDelay = true;
        FullscreenActivity.learnSongLength = false;
        learnAutoScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLearnedPreDelayValue();
            }
        });
        String s = getString(R.string.autoscroll_time) + "\n" + getString(R.string.savesong);
        learnAutoScroll_TextView.setText(s);
        LearnAutoScroll mtask_learnautoscroll = new LearnAutoScroll();
        mtask_learnautoscroll.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public void getLearnedPreDelayValue() {
        int time = (int) (FullscreenActivity.time_passed - FullscreenActivity.time_start)/1000;
        if (time<0) {
            time = 0;
        }
        FullscreenActivity.learnSongLength = true;
        FullscreenActivity.learnPreDelay = false;
        FullscreenActivity.mPreDelay = time+"";
        Log.d("d", time+"");
        String s = getString(R.string.edit_song_duration) + "\n" + getString(R.string.savesong);
        learnAutoScroll_TextView.setText(s);
        learnAutoScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLearnedSongLengthValue();
            }
        });
    }
    public void getLearnedSongLengthValue() {
        int time = (int) (FullscreenActivity.time_passed - FullscreenActivity.time_start)/1000;
        if (time<0) {
            time = 0;
        }
        FullscreenActivity.mDuration = time+"";
        FullscreenActivity.learnPreDelay = false;
        FullscreenActivity.learnSongLength = false;

        if (mtask_learnautoscroll!=null) {
            try {
                mtask_learnautoscroll.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FullscreenActivity.whattodo = "page_autoscroll";
        openFragment();
    }
    @SuppressLint("StaticFieldLeak")
    private class LearnAutoScroll extends AsyncTask<String, Integer, String> {

        int time;

        @Override
        protected void onPreExecute() {
            try {
                FullscreenActivity.time_start = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                while (FullscreenActivity.learnPreDelay || FullscreenActivity.learnSongLength) {
                    FullscreenActivity.time_passed = System.currentTimeMillis();

                    long starttime = System.currentTimeMillis();
                    long currtime = System.currentTimeMillis();
                    while ((currtime - starttime) < 1000) {
                        currtime = System.currentTimeMillis();
                    }
                    time = (int) (FullscreenActivity.time_passed - FullscreenActivity.time_start)/1000;
                    if (time>28 && FullscreenActivity.learnPreDelay) {
                        publishProgress(-100);
                    } else {
                        publishProgress(time);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "dummy";
        }

        @Override
        protected void onProgressUpdate(Integer... intg) {
            if (time==-100) {
                // We've exceed the allowed predelay length, so save this value as it is max
                getLearnedPreDelayValue();
            } else {
                // Update the timer
                AutoScrollFunctions.LearnTimeRunnable runnable = new AutoScrollFunctions.LearnTimeRunnable(learnAutoScrollTime_TextView);
                AutoScrollFunctions.doautoScrollLearn.post(runnable);
            }
        }

        @Override
        protected void onPostExecute(String dummy) {
            try {
                if (!cancelled) {
                    FullscreenActivity.learnPreDelay = false;
                    FullscreenActivity.learnSongLength = false;
                    doCancelAsyncTask(mtask_learnautoscroll);
                    learnAutoScroll.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean cancelled = false;
        @Override
        public void onCancelled() {
            FullscreenActivity.learnPreDelay = false;
            FullscreenActivity.learnSongLength = false;
            cancelled = true;
            doCancelAsyncTask(mtask_learnautoscroll);
        }
    }

    @Override
    public void startAutoScroll() {
        FullscreenActivity.clickedOnAutoScrollStart = true;
        totalTime_TextView.setTextSize(FullscreenActivity.timerFontSizeAutoScroll);
        timeSeparator_TextView.setTextSize(FullscreenActivity.timerFontSizeAutoScroll);
        currentTime_TextView.setTextSize(FullscreenActivity.timerFontSizeAutoScroll);
        currentTime_TextView.setText(getString(R.string.zerotime));
        totalTime_TextView.setText(TimeTools.timeFormatFixer(FullscreenActivity.autoScrollDuration));
        playbackProgress.setVisibility(View.VISIBLE);
        doCancelAsyncTask(mtask_autoscroll_music);
        doCancelAsyncTask(get_scrollheight);
        FullscreenActivity.isautoscrolling = true;
        FullscreenActivity.pauseautoscroll = true;
        FullscreenActivity.isManualDragging = false;
        FullscreenActivity.wasscrolling = false;
        get_scrollheight = new GetScrollHeight();
        //FullscreenActivity.refWatcher.watch(get_scrollheight);
        try {
            get_scrollheight.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class GetScrollHeight extends AsyncTask<Object, Integer, String> {
        boolean viewdrawn = false;
        boolean ready = false;

        boolean cancelled = false;
        @Override
        public void onCancelled() {
            cancelled = true;
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (!cancelled) {
                    FullscreenActivity.scrollpageHeight = 0;
                    FullscreenActivity.newPosFloat = 0.0f;
                    long t1 = System.currentTimeMillis();
                    long t2 = t1 + 5000;

                    while (!ready) {
                        long ct = System.currentTimeMillis();
                        publishProgress(1);
                        if (ct > t2 || viewdrawn) {
                            ready = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... intg) {
            try {
                if (!cancelled) {
                    if (songscrollview.getChildAt(0) != null) {
                        FullscreenActivity.scrollpageHeight = songscrollview.getChildAt(0).getMeasuredHeight() -
                                songscrollview.getHeight();
                    }
                    if (FullscreenActivity.scrollpageHeight > 0) {
                        viewdrawn = true;
                        ready = true;
                        cancelled = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                if (viewdrawn) {
                    AutoScrollFunctions.getAutoScrollValues(songscrollview, mypage, ab_toolbar);
                    mtask_autoscroll_music = new AutoScrollMusic();
                    try {
                        mtask_autoscroll_music.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class AutoScrollMusic extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            try {
                FullscreenActivity.autoscroll_modifier = 0;
                FullscreenActivity.autoscrollispaused = false;
                FullscreenActivity.time_start = System.currentTimeMillis();
                songscrollview.scrollTo(0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                while (FullscreenActivity.isautoscrolling) {
                    FullscreenActivity.time_passed = System.currentTimeMillis();
                    boolean doscroll = ((FullscreenActivity.time_passed - FullscreenActivity.time_start) / 1000) >= FullscreenActivity.autoScrollDelay;
                    if (doscroll) {
                        publishProgress(1);
                    }
                    // don't scroll first time
                    if (!FullscreenActivity.pauseautoscroll) {
                        AutoScrollFunctions.ProgressTimeRunnable runnable = new AutoScrollFunctions.ProgressTimeRunnable(currentTime_TextView, totalTime_TextView, timeSeparator_TextView);
                        AutoScrollFunctions.doProgressTime.post(runnable);
                        if (doscroll) {
                            AutoScrollFunctions.AutoScrollRunnable runnable2 = new AutoScrollFunctions.AutoScrollRunnable(songscrollview);
                            AutoScrollFunctions.doautoScroll.post(runnable2);
                        }
                    } else {
                        FullscreenActivity.pauseautoscroll = false;
                    }
                    if (doscroll) {
                        if (FullscreenActivity.newPosFloat >= FullscreenActivity.scrollpageHeight) {
                            FullscreenActivity.autoscrollispaused = false;
                            FullscreenActivity.isautoscrolling = false;
                        }
                    }
                    long starttime = System.currentTimeMillis();
                    long currtime = System.currentTimeMillis();
                    while ((currtime - starttime) < FullscreenActivity.autoscroll_pause_time) {
                        currtime = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "dummy";
        }

        @Override
        protected void onProgressUpdate(Integer... intg) {
            try {
                if (!FullscreenActivity.wasscrolling && !FullscreenActivity.autoscrollispaused) {
                    if(FullscreenActivity.newPosFloat + FullscreenActivity.autoscroll_pixels + FullscreenActivity.autoscroll_modifier > 0) {
                        FullscreenActivity.newPosFloat = FullscreenActivity.newPosFloat + FullscreenActivity.autoscroll_pixels + FullscreenActivity.autoscroll_modifier;
                    }
                    else
                    {
                        FullscreenActivity.newPosFloat = FullscreenActivity.newPosFloat + FullscreenActivity.autoscroll_pixels;
                    }
                } else {
                    FullscreenActivity.newPosFloat = songscrollview.getScrollY();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String dummy) {
            try {
                if (!cancelled) {
                    if (!FullscreenActivity.isautoscrolling) {
                        FullscreenActivity.pauseautoscroll = false;
                    } else {
                        FullscreenActivity.isautoscrolling = false;
                        FullscreenActivity.pauseautoscroll = true;
                    }

                    FullscreenActivity.popupAutoscroll_stoporstart = "stop";
                    FullscreenActivity.autoscrollonoff = "off";
                    doCancelAsyncTask(mtask_autoscroll_music);
                    playbackProgress.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean cancelled = false;
        @Override
        public void onCancelled() {
            FullscreenActivity.isautoscrolling = false;
            FullscreenActivity.pauseautoscroll = true;
            FullscreenActivity.popupAutoscroll_stoporstart = "stop";
            FullscreenActivity.autoscrollonoff = "off";
            cancelled = true;
            doCancelAsyncTask(mtask_autoscroll_music);
        }
    }

    @Override
    public void stopAutoScroll() {
        playbackProgress.setVisibility(View.GONE);
        doCancelAsyncTask(mtask_autoscroll_music);
        FullscreenActivity.isautoscrolling = false;
        currentTime_TextView.setText(getString(R.string.zerotime));
    }

    public boolean checkCanScrollDown() {
        boolean showscrolldown = false;
        if (FullscreenActivity.whichMode!=null && FullscreenActivity.whichMode.equals("Stage")) {
            if (FullscreenActivity.currentSection>-1 && FullscreenActivity.songSections!=null) {
                showscrolldown = FullscreenActivity.currentSection < FullscreenActivity.songSections.length - 1;
            }
        } else {
            int height;
            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                try {
                    height = glideimage_ScrollView.getChildAt(0).getMeasuredHeight() - glideimage_ScrollView.getHeight();
                } catch (Exception e) {
                    height = 0;
                }
                if (glideimage_ScrollView!=null) {
                    FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY();
                    showscrolldown = height > glideimage_ScrollView.getScrollY() && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            } else {
                try {
                    height = songscrollview.getChildAt(0).getMeasuredHeight() - songscrollview.getHeight();
                } catch (Exception e) {
                    height = 0;
                }
                if (songscrollview != null) {
                    FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
                    showscrolldown = height > songscrollview.getScrollY() && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            }
        }
        // Decide if the down arrow should be displayed.
        return showscrolldown;
    }

    public boolean checkCanScrollUp() {
        boolean showscrollup = false;
        if (FullscreenActivity.whichMode!=null && FullscreenActivity.whichMode.equals("Stage")) {
            showscrollup = FullscreenActivity.currentSection > 0;
        } else {
            if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
                if (glideimage_ScrollView!=null) {
                    FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY();
                    showscrollup = glideimage_ScrollView.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            } else {
                if (songscrollview != null) {
                    FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
                    showscrollup = songscrollview.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
                }
            }
        }
        // Decide if the up arrow should be displayed.
        return showscrollup;
    }

    public void scrollMenu(String direction) {
        if (direction.equals("up")) {
            song_list_view.smoothScrollBy((int) (-0.8f * songmenu.getHeight()), 1600);
        } else {
            song_list_view.smoothScrollBy((int) (+0.8f * songmenu.getHeight()), 1600);
        }
    }

    public void goToNextItem() {
        FullscreenActivity.whichDirection = "R2L";
        boolean dealtwithaspdf = false;

        // If this is a PDF, check we can't move pages
        if (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent < (FullscreenActivity.pdfPageCount - 1)) {
            FullscreenActivity.pdfPageCurrent = FullscreenActivity.pdfPageCurrent + 1;

            // Load the next pdf page
            dealtwithaspdf = true;
            loadSong();

        } else {
            FullscreenActivity.pdfPageCurrent = 0;
        }

        // If this hasn't been dealt with
        if (!dealtwithaspdf && FullscreenActivity.setView) {
            // Is there another song in the set?  If so move, if not, do nothing
            if (FullscreenActivity.indexSongInSet < FullscreenActivity.mSetList.length - 1) {
                //FullscreenActivity.indexSongInSet += 1;
                FullscreenActivity.setMoveDirection = "forward";
                doMoveInSet();
            }
        } else if (!dealtwithaspdf) {
            // Try to move to the next song alphabetically
            if (FullscreenActivity.nextSongIndex < FullscreenActivity.mSongFileNames.length
                    && FullscreenActivity.nextSongIndex != -1
                    && !FullscreenActivity.songfilename.equals(FullscreenActivity.mSongFileNames[FullscreenActivity.nextSongIndex])) {
                FullscreenActivity.tempswipeSet = "disable";
                FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[FullscreenActivity.nextSongIndex];
                Preferences.savePreferences();
                loadSong();

                // Set a runnable to reset swipe back to original value after 1 second
                Handler delayfadeinredraw = new Handler();
                delayfadeinredraw.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.tempswipeSet = "enable";
                    }
                }, FullscreenActivity.delayswipe_time);
            } else {
                showToastMessage(getResources().getString(R.string.lastsong));
            }
        }
    }

    public void goToPreviousItem() {
        FullscreenActivity.whichDirection = "L2R";
        boolean dealtwithaspdf = false;

        // If this is a PDF, check we can't move pages
        if (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0) {
            FullscreenActivity.pdfPageCurrent = FullscreenActivity.pdfPageCurrent - 1;
            dealtwithaspdf = true;
            loadSong();
        } else {
            FullscreenActivity.pdfPageCurrent = 0;
        }

        // If this hasn't been dealt with
        if (!dealtwithaspdf && FullscreenActivity.setView) {
            // Is there another song in the set?  If so move, if not, do nothing
            if (FullscreenActivity.indexSongInSet > 0 && FullscreenActivity.mSetList.length > 0) {
                //FullscreenActivity.indexSongInSet -= 1;
                FullscreenActivity.setMoveDirection = "back";
                doMoveInSet();
            }
        } else if (!dealtwithaspdf) {
            // Try to move to the previous song alphabetically
            if (FullscreenActivity.previousSongIndex >= 0
                    && !FullscreenActivity.songfilename.equals(FullscreenActivity.mSongFileNames[FullscreenActivity.previousSongIndex])) {
                FullscreenActivity.tempswipeSet = "disable";

                FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[FullscreenActivity.previousSongIndex];
                Preferences.savePreferences();
                loadSong();

                // Set a runnable to reset swipe back to original value after 1 second
                Handler delayfadeinredraw = new Handler();
                delayfadeinredraw.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FullscreenActivity.tempswipeSet = "enable";
                    }
                }, FullscreenActivity.delayswipe_time);
            } else {
                showToastMessage(getResources().getString(R.string.firstsong));
            }
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
    public void changePDFPage(int page, String direction) {
        FullscreenActivity.whichDirection = direction;
        FullscreenActivity.pdfPageCurrent = page;
        loadSong();
    }

    // The page action gestures stuff is below
    public boolean oktoregistergesture() {

        boolean oktogo = false;

        if (!FullscreenActivity.pressing_button  // Button pressing
                && !setButton.isPressed() && !padButton.isPressed() && !autoscrollButton.isPressed()
                && !metronomeButton.isPressed() && !extraButton.isPressed() && !chordButton.isPressed()
                && !linkButton.isPressed() && !stickyButton.isPressed() && !notationButton.isPressed()
                && !highlightButton.isPressed() && !pageselectButton.isPressed()
                && !customButton.isPressed() && !custom1Button.isPressed() && !custom2Button.isPressed()
                && !custom3Button.isPressed() && !custom4Button.isPressed() && !linkButton_ungrouped.isPressed()
                && !chordButton_ungrouped.isPressed() && !pageselectButton_ungrouped.isPressed()
                && !stickyButton_ungrouped.isPressed() && !notationButton_ungrouped.isPressed()
                && !highlightButton_ungrouped.isPressed()
                && !custom1Button_ungrouped.isPressed() && !custom2Button_ungrouped.isPressed()
                && !custom3Button_ungrouped.isPressed() && !custom4Button_ungrouped.isPressed()
                && !scrollDownButton.isPressed() && !scrollUpButton.isPressed()
                && !mDrawerLayout.isDrawerOpen(songmenu) && !mDrawerLayout.isDrawerVisible(songmenu)
                && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerVisible(optionmenu)) {
            oktogo = true;
        }

        return oktogo;
    }

    // This bit listens for long key presses (disables the menu long press action)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            // Open up the song search intent instead of bringing up the keyboard
            if (FullscreenActivity.longKeyPress) {
                FullscreenActivity.shortKeyPress = false;
            } else {
                FullscreenActivity.shortKeyPress = true;
                FullscreenActivity.longKeyPress = false;
            }
            return true;
        }

        if (keyCode == FullscreenActivity.pageturner_DOWN || keyCode == FullscreenActivity.pageturner_UP ||
                keyCode == FullscreenActivity.pageturner_PREVIOUS || keyCode == FullscreenActivity.pageturner_NEXT) {
            event.startTracking();
            if (FullscreenActivity.longKeyPress) {
                FullscreenActivity.shortKeyPress = false;
            } else {
                FullscreenActivity.shortKeyPress = true;
                FullscreenActivity.longKeyPress = false;
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
        //return false;
    }

    // This bit listens for key presses (for page turn and scroll)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        event.startTracking();
        View rf = getCurrentFocus();
        if (rf!=null) {
            rf.clearFocus();
        }
        if (FullscreenActivity.shortKeyPress) {
            FullscreenActivity.pressing_button = false;

            // Reset immersive mode
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                restoreTranslucentBarsDelayed();
            }

            // Set a runnable to reset swipe back to original value after 1 second

            // Eat the long press event so the keyboard doesn't come up.
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (event.isLongPress()) {
                    // Open up the song search intent instead of bringing up the keyboard
                    return true;
                } else {
                    // User wants the menu opened/closed
                    if (mDrawerLayout.isDrawerOpen(optionmenu)) {
                        mDrawerLayout.closeDrawer(optionmenu);
                    }
                    if (mDrawerLayout.isDrawerOpen(songmenu)) {
                        mDrawerLayout.closeDrawer(songmenu);
                    }
                }
            }

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed();

            } else if (keyCode == FullscreenActivity.pageturner_NEXT) {
                if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
                    if (mDrawerLayout.isDrawerOpen(songmenu)) {
                        // Scroll the song menu down
                        scrollMenu("down");
                    } else {
                        if (checkCanScrollDown()) {
                            CustomAnimations.animateFAB(scrollDownButton,StageMode.this);
                            doScrollDown();
                        } else {
                            if (setForwardButton!=null && setForwardButton.getVisibility() == View.VISIBLE) {
                                CustomAnimations.animateFAB(setForwardButton,StageMode.this);
                            }
                            goToNextItem();
                        }
                    }
                } else {
                    goToNextItem();
                }

            } else if (keyCode == FullscreenActivity.pageturner_PREVIOUS) {
                if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
                    if (mDrawerLayout.isDrawerOpen(songmenu)) {
                        // Scroll the song menu up
                        scrollMenu("up");
                    } else {
                        if (checkCanScrollUp()) {
                            CustomAnimations.animateFAB(scrollUpButton,StageMode.this);
                            doScrollUp();
                        } else {
                            if (setBackButton!=null && setBackButton.getVisibility() == View.VISIBLE) {
                                CustomAnimations.animateFAB(setBackButton,StageMode.this);
                            }
                            goToPreviousItem();
                        }
                    }
                } else {
                    goToPreviousItem();
                }

            } else if (keyCode == FullscreenActivity.pageturner_UP) {
                if (mDrawerLayout.isDrawerOpen(songmenu)) {
                    // Scroll the song menu up
                    scrollMenu("up");
                } else {
                    if (checkCanScrollUp()) {
                        CustomAnimations.animateFAB(scrollUpButton,StageMode.this);
                        doScrollUp();
                    }
                }

            } else if (keyCode == FullscreenActivity.pageturner_DOWN) {
                if (mDrawerLayout.isDrawerOpen(songmenu)) {
                    // Scroll the song menu down
                    scrollMenu("down");
                } else {
                    if (checkCanScrollDown()) {
                        CustomAnimations.animateFAB(scrollDownButton,StageMode.this);
                        doScrollDown();
                    }
                }

            } else if (keyCode == FullscreenActivity.pageturner_PAD) {
                gesture6();

            } else if (keyCode == FullscreenActivity.pageturner_AUTOSCROLL) {
                gesture5();

            } else if (keyCode == FullscreenActivity.pageturner_METRONOME) {
                gesture7();
            }
        }
        FullscreenActivity.shortKeyPress = true;
        FullscreenActivity.longKeyPress = false;
        return true;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        String action = "";
        if (keyCode == FullscreenActivity.pageturner_DOWN) {
            action = FullscreenActivity.longpressdownpedalgesture;
        } else if (keyCode == FullscreenActivity.pageturner_UP) {
            action = FullscreenActivity.longpressuppedalgesture;
        } else if (keyCode == FullscreenActivity.pageturner_PREVIOUS) {
            action = FullscreenActivity.longpresspreviouspedalgesture;
        } else if (keyCode == FullscreenActivity.pageturner_NEXT) {
            action = FullscreenActivity.longpressnextpedalgesture;
        }

        switch (action) {
            case "1":
                gesture1();  // Open/close the drawers
                break;
            case "2":
                if (justSong(StageMode.this)) {
                    gesture2(); // Edit the song
                }
                break;
            case "3":
                gesture3();  // Add the song to the set
                break;
            case "4":
                gesture4();  // Refresh the current song
                break;
            case "5":
                gesture5();  // Stop/start autoscroll
                break;
            case "6":
                gesture6();  // Stop/start pad
                break;
            case "7":
                gesture7();  // Stop/start metronome
                break;
            case "8":
                gesture5();  // Stop/start autoscroll
                gesture6();  // Stop/start pad
                break;
            case "9":
                gesture5();  // Stop/start autoscroll
                gesture7();  // Stop/start metronome
                break;
            case "10":
                gesture6();  // Stop/start pad
                gesture7();  // Stop/start metronome
                break;
            case "11":
                gesture5();  // Stop/start autoscroll
                gesture6();  // Stop/start pad
                gesture7();  // Stop/start metronome
                break;
            case "12":
                FullscreenActivity.whattodo = "editset"; // Show the set
                openFragment();
            default:
                // Do nothing
                break;
        }

        if (action.length() > 0) {
            FullscreenActivity.shortKeyPress = false;
            FullscreenActivity.longKeyPress = true;
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        scaleGestureDetector.onTouchEvent(event);
//        return true;
//    }

    private class simpleOnScaleGestureListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        //int action = MotionEventCompat.getActionMasked(ev);
        int action = ev.getAction();
        // WOULD BE BETTER IF THIS WAS CALLED ON SOME KIND OF ONSCROLL LISTENER
        scaleGestureDetector.onTouchEvent(ev);
        switch (action) {

            case (MotionEvent.ACTION_MOVE):
                // Set a runnable to check the scroll position
                FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
                delaycheckscroll.post(checkScrollPosition);

                // Set a runnable to check the scroll position after 1 second
                delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
        }

        // TouchEvent dispatcher.
        if (gestureDetector != null) {
            if (!gestureDetector.onTouchEvent(ev)) {
                if (action == MotionEvent.ACTION_UP && !FullscreenActivity.scrollbutton) {
                    toggleActionBar();
                    FullscreenActivity.wasscrolling = false;

                } else {
                    if (delayactionBarHide != null && hideActionBarRunnable != null) {
                        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
                    }
                }
                FullscreenActivity.wasscrolling = false;
                FullscreenActivity.scrollbutton = false;
                FullscreenActivity.isManualDragging = false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private class SwipeDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Decide what the double tap action is
            // 1 = both menus
            // 2 = edit song
            // 3 = add to set
            // 4 = redraw
            // 5 = start/stop autoscroll
            // 6 = start/stop pad
            // 7 = start/stop metronome
            // 8 = start/stop autoscroll + pad
            // 9 = start/stop autoscroll + metronome
            //10 = start/stop autoscroll + pad + metronome
            //11 = start/stop autoscroll + pad + metronome
            // 0/else = off (highest menu item)

            // First test conditions
            if (oktoregistergesture()) {

                // Now find out which gesture we've gone for
                switch (FullscreenActivity.gesture_doubletap) {
                    case "1":
                        gesture1();  // Open/close the drawers
                        break;
                    case "2":
                        if (justSong(StageMode.this)) {
                            gesture2();
                        }
                        break;
                    case "3":
                        gesture3();  // Add the song to the set
                        break;
                    case "4":
                        gesture4();  // Refresh the current song
                        break;
                    case "5":
                        gesture5();  // Stop/start autoscroll
                        break;
                    case "6":
                        gesture6();  // Stop/start pad
                        break;
                    case "7":
                        gesture7();  // Stop/start metronome
                        break;
                    case "8":
                        gesture5();  // Stop/start autoscroll
                        gesture6();  // Stop/start pad
                        break;
                    case "9":
                        gesture5();  // Stop/start autoscroll
                        gesture7();  // Stop/start metronome
                        break;
                    case "10":
                        gesture6();  // Stop/start pad
                        gesture7();  // Stop/start metronome
                        break;
                    case "11":
                        gesture5();  // Stop/start autoscroll
                        gesture6();  // Stop/start pad
                        gesture7();  // Stop/start metronome
                        break;
                    default:
                        // Do nothing
                        break;
                }

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Decide what the long press action is
            // 1 = both menus
            // 2 = edit song
            // 3 = add to set
            // 4 = redraw
            // 5 = start/stop autoscroll
            // 6 = start/stop pad
            // 7 = start/stop metronome
            // 0/else = off (highest menu item)

            // First test conditions
            if (oktoregistergesture()) {

                // Now find out which gesture we've gone for
                switch (FullscreenActivity.gesture_longpress) {
                    case "1":
                        gesture1();  // Open/close the drawers
                        break;
                    case "2":
                        if (justSong(StageMode.this)) {
                            gesture2();
                        }
                        break;
                    case "3":
                        gesture3();  // Add the song to the set
                        break;
                    case "4":
                        gesture4();  // Refresh the current song
                        break;
                    case "5":
                        gesture5();  // Stop/start autoscroll
                        break;
                    case "6":
                        gesture6();  // Stop/start pad
                        break;
                    case "7":
                        gesture7();  // Stop/start metronome
                        break;
                    case "8":
                        gesture5();  // Stop/start autoscroll
                        gesture6();  // Stop/start pad
                        break;
                    case "9":
                        gesture5();  // Stop/start autoscroll
                        gesture7();  // Stop/start metronome
                        break;
                    case "10":
                        gesture6();  // Stop/start pad
                        gesture7();  // Stop/start metronome
                        break;
                    case "11":
                        gesture5();  // Stop/start autoscroll
                        gesture6();  // Stop/start pad
                        gesture7();  // Stop/start metronome
                        break;
                    default:
                        // Do nothing
                        break;
                }
            }
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            FullscreenActivity.wasscrolling = true;
            FullscreenActivity.isManualDragging = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {

            // Check movement along the Y-axis. If it exceeds
            // SWIPE_MAX_OFF_PATH, then dismiss the swipe.
            int screenwidth = mypage.getWidth();
            int leftmargin = 40;
            int rightmargin = screenwidth - 40;
            if (Math.abs(e1.getY() - e2.getY()) > FullscreenActivity.SWIPE_MAX_OFF_PATH) {
                return false;
            }

            if (FullscreenActivity.tempswipeSet.equals("disable")) {
                return false; // Currently disabled swiping to let screen finish drawing.
            }

            // Swipe from right to left.
            // The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE)
            // and a certain velocity (SWIPE_THRESHOLD_VELOCITY).
            if (e1.getX() - e2.getX() > FullscreenActivity.SWIPE_MIN_DISTANCE
                    && e1.getX() < rightmargin
                    && Math.abs(velocityX) > FullscreenActivity.SWIPE_THRESHOLD_VELOCITY
                    && (FullscreenActivity.swipeSet.equals("Y") || FullscreenActivity.swipeSet.equals("S"))) {

                // Trying to move to the next item
                try {
                    setForwardButton.performClick();
                    //goToNextItem();
                } catch (Exception e) {
                    // No song after
                }
                return true;
            }

            // Swipe from left to right.
            // The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE)
            // and a certain velocity (SWIPE_THRESHOLD_VELOCITY).
            if (e2.getX() - e1.getX() > FullscreenActivity.SWIPE_MIN_DISTANCE
                    && e1.getX() > leftmargin
                    && Math.abs(velocityX) > FullscreenActivity.SWIPE_THRESHOLD_VELOCITY
                    && (FullscreenActivity.swipeSet.equals("Y") || FullscreenActivity.swipeSet.equals("S"))) {

                // Go to previous item
                try {
                    setBackButton.performClick();
                    //goToPreviousItem();
                } catch (Exception e) {
                    // No song before
                }
                return true;
            }
            return false;
        }
    }

    // Open/close the drawers
    public void gesture1() {
        if (mDrawerLayout.isDrawerOpen(songmenu)) {
            closeMyDrawers("song");
        } else {
            openMyDrawers("song");
        }
        FullscreenActivity.wasscrolling = false;
        if (delayactionBarHide != null && hideActionBarRunnable != null) {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        }
    }

    // Edit song
    public void gesture2() {
        if (FullscreenActivity.isPDF) {
            FullscreenActivity.whattodo = "extractPDF";
            openFragment();
        } else if (justSong(StageMode.this)) {
            // Edit the song
            FullscreenActivity.whattodo = "editsong";
            openFragment();
        }
    }

    // Add to set
    public void gesture3() {
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.songfilename + "_**$";
        } else {
            FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename + "_**$";
        }

        // Allow the song to be added, even if it is already there
        FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
        // Tell the user that the song has been added.
        showToastMessage("\"" + FullscreenActivity.songfilename + "\" "
                + getResources().getString(R.string.addedtoset));
        // Vibrate to let the user know something happened
        DoVibrate.vibrate(StageMode.this, 50);

        // Save the set and other preferences
        Preferences.savePreferences();

        SetActions.prepareSetList();
        prepareOptionMenu();

        // Show the current set
        openMyDrawers("option");

        // Hide the menus - 1 second after opening the Option menu,
        // close it (1000ms total)
        Handler optionMenuFlickClosed = new Handler();
        optionMenuFlickClosed.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(optionmenu);
            }
        }, 1000);
    }

    // Redraw the lyrics page
    public void gesture4() {
        loadSong();
    }

    @Override
    // Stop or start autoscroll
    public void gesture5() {
        if (justSong(StageMode.this)) {
            DoVibrate.vibrate(StageMode.this, 50);
            if (FullscreenActivity.isautoscrolling) {
                stopAutoScroll();
            } else {
                if (FullscreenActivity.autoscrollok) {
                    startAutoScroll();
                } else {
                    showToastMessage(getResources().getString(R.string.autoscroll) + " - " +
                            getResources().getString(R.string.notset));
                }
            }
        }
    }

    @Override
    public void gesture6() {
        if (justSong(StageMode.this)) {
            // Stop or start pad
            PadFunctions.getPad1Status();
            PadFunctions.getPad2Status();
            DoVibrate.vibrate(StageMode.this, 50);
            if (FullscreenActivity.pad1Playing || FullscreenActivity.pad2Playing) {
                if (FullscreenActivity.pad1Playing) {
                    FullscreenActivity.mPlayer1.stop();
                    FullscreenActivity.mPlayer1.reset();
                } else if (FullscreenActivity.pad2Playing) {
                    FullscreenActivity.mPlayer2.stop();
                    FullscreenActivity.mPlayer2.reset();
                }
                FullscreenActivity.padson = false;
            } else if (PadFunctions.isPadValid(StageMode.this)) {
                preparePad();
            } else {
                FullscreenActivity.whattodo = "page_pad";
                openFragment();
            }
        }
    }

    @Override
    public void gesture7() {
        if (justSong(StageMode.this)) {
            // Start or stop metronome
            // Vibrate to let the user know something happened
            DoVibrate.vibrate(StageMode.this, 50);
            if (FullscreenActivity.metronomeok) {
                Metronome.startstopMetronome(StageMode.this);
            } else {
                showToastMessage(getResources().getString(R.string.metronome) + " - " +
                        getResources().getString(R.string.notset));
            }
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
            try {
                updateDisplays();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                Log.d("d","Trying teardown");
                CastRemoteDisplayLocalService.stopService();
                if (hdmi!=null && hdmi.isShowing()) {
                    try {
                        hdmi.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                 }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    @SuppressLint("ServiceCast")
    public void updateDisplays() {
        // This is called when display devices are changed (connected, disconnected, etc.)
        Intent intent = new Intent(StageMode.this,
                StageMode.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                StageMode.this, 0, intent, 0);

        Log.d("updateDislpays","Called");
        CastRemoteDisplayLocalService.NotificationSettings settings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(notificationPendingIntent).build();

        if (mSelectedDevice!=null) {
            Log.d("updateDislpays","startService");
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
                Log.d("updateDislpays","hdmi");
                Display mDisplay = mMediaRouter.getSelectedRoute().getPresentationDisplay();
                if (mDisplay!=null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        hdmi = new PresentationServiceHDMI(StageMode.this,mDisplay);
                        hdmi.show();
                        FullscreenActivity.isHDMIConnected = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void refreshSecondaryDisplay(String which) {
        Log.d("refreshSecondaryDisplay","which="+which);
        try {
            switch (which) {
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
}