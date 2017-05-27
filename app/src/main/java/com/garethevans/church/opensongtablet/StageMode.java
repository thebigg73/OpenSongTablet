package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutServiceData;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
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
        PopUpOptionMenuSet.MyInterface, PopUpOptionMenuSong.MyInterface,
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
        PopUpLayoutFragment.MyInterface {

    // The toolbar and menu
    public Toolbar ab_toolbar;
    public static ActionBar ab;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public RelativeLayout songandauthor;
    public TextView digitalclock;
    public TextView songtitle_ab;
    public TextView songkey_ab;
    public TextView songauthor_ab;
    public TextView batterycharge;
    public ImageView batteryimage;
    Menu menu;

    // The left and right menu
    DrawerLayout mDrawerLayout;
    TextView menuFolder_TextView;
    FloatingActionButton closeSongFAB;
    LinearLayout songmenu;
    LinearLayout optionmenu;
    ScrollView optionsdisplayscrollview;
    LinearLayout changefolder_LinearLayout;
    ListView song_list_view;

    // Song sections view
    RelativeLayout mypage;
    ScrollView songscrollview;
    RelativeLayout testpane;
    RelativeLayout testpane1_2;
    RelativeLayout testpane2_2;
    RelativeLayout testpane1_3;
    RelativeLayout testpane2_3;
    RelativeLayout testpane3_3;
    LinearLayout column1_1;
    LinearLayout column1_2;
    LinearLayout column2_2;
    LinearLayout column1_3;
    LinearLayout column2_3;
    LinearLayout column3_3;
    ScrollView glideimage_ScrollView;
    ImageView glideimage;
    LinearLayout backingtrackProgress;
    TextView padcurrentTime_TextView;
    TextView padtotalTime_TextView;
    LinearLayout playbackProgress;
    TextView currentTime_TextView;
    TextView totalTime_TextView;
    float width_scale = 0f;
    float biggestscale_1col = 0.0f;
    float biggestscale_2col = 0.0f;
    float biggestscale_3col = 0.0f;
    boolean overridingfull;
    boolean overridingwidth;
    boolean rendercalled = false;
    boolean sectionpresented = false;

    int coltouse = 1;

    // Page buttons
    FloatingActionButton setButton;
    FloatingActionButton padButton;
    FloatingActionButton autoscrollButton;
    FloatingActionButton metronomeButton;
    FloatingActionButton extraButton;
    FloatingActionButton chordButton;
    FloatingActionButton stickyButton;
    FloatingActionButton pageselectButton;
    FloatingActionButton linkButton;
    FloatingActionButton chordButton_ungrouped;
    FloatingActionButton stickyButton_ungrouped;
    FloatingActionButton pageselectButton_ungrouped;
    FloatingActionButton linkButton_ungrouped;
    FloatingActionButton customButton;
    FloatingActionButton custom1Button;
    FloatingActionButton custom2Button;
    FloatingActionButton custom3Button;
    FloatingActionButton custom1Button_ungrouped;
    FloatingActionButton custom2Button_ungrouped;
    FloatingActionButton custom3Button_ungrouped;
    FloatingActionButton scrollDownButton;
    FloatingActionButton scrollUpButton;
    FloatingActionButton setBackButton;
    FloatingActionButton setForwardButton;
    LinearLayout extrabuttons;
    LinearLayout extrabuttons2;

    // Casting
    MediaRouter mMediaRouter;
    MediaRouteSelector mMediaRouteSelector;
    MyMediaRouterCallback mMediaRouterCallback = new MyMediaRouterCallback();
    CastDevice mSelectedDevice;

    // Dialogue fragments and stuff
    DialogFragment newFragment;

    // Gestures
    ScaleGestureDetector scaleGestureDetector;
    GestureDetector gestureDetector;

    // ASyncTask stuff
    AsyncTask<Object, Void, String> loadsong_async;
    AsyncTask<Object, Void, String> preparesongview_async;
    AsyncTask<Object, Void, String> createperformanceview1col_async;
    AsyncTask<Object, Void, String> createperformanceview2col_async;
    AsyncTask<Object, Void, String> createperformanceview3col_async;
    AsyncTask<Object, Void, String> preparesongmenu_async;
    AsyncTask<Object, Void, String> prepareoptionmenu_async;
    AsyncTask<Void, Void, String> resizeperformance_async;
    AsyncTask<Void, Void, String> resizestage_async;
    AsyncTask<Object, Void, String> createstageview1col_async;
    AsyncTask<Object, Void, String> fadeout_media1;
    AsyncTask<Object, Void, String> fadeout_media2;
    AsyncTask<String, Integer, String> mtask_autoscroll_music;
    AsyncTask<Object, Void, String> check_storage;
    AsyncTask<Object, Void, String> sharesong_async;
    AsyncTask<Object, Void, String> load_customreusable;
    AsyncTask<Object, Void, String> open_drawers;
    AsyncTask<Object, Void, String> close_drawers;
    AsyncTask<Object, Void, String> resize_drawers;
    AsyncTask<Object, Void, String> do_moveinset;
    AsyncTask<Object, Void, String> indexing_done;
    AsyncTask<Object, Void, String> add_slidetoset;
    AsyncTask<Object, Void, String> dualscreenwork_async;
    IndexSongs.IndexMySongs indexsongs_task;
    AsyncTask<Void, Void, Integer> prepare_pad;
    AsyncTask<Void, Void, Integer> play_pads;

    // Allow the menus to flash open to show where they are on first run
    boolean firstrun_option = true;
    boolean firstrun_song = true;

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
    SalutMessage myMessage;
    SalutMessage mySongMessage;


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
                .build();

        // Set up the gesture detector
        scaleGestureDetector = new ScaleGestureDetector(this, new simpleOnScaleGestureListener());
        gestureDetector = new GestureDetector(new SwipeDetector());

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

                songandauthor = (RelativeLayout) findViewById(R.id.songandauthor);
                digitalclock = (TextView) findViewById(R.id.digitalclock);
                songtitle_ab = (TextView) findViewById(R.id.songtitle_ab);
                songkey_ab = (TextView) findViewById(R.id.songkey_ab);
                songauthor_ab = (TextView) findViewById(R.id.songauthor_ab);
                batterycharge = (TextView) findViewById(R.id.batterycharge);
                batteryimage = (ImageView) findViewById(R.id.batteryimage);
                mypage = (RelativeLayout) findViewById(R.id.mypage);
                mypage.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);

                // Set up the pad and autoscroll timing display
                backingtrackProgress = (LinearLayout) findViewById(R.id.backingtrackProgress);
                backingtrackProgress.setVisibility(View.GONE);
                padcurrentTime_TextView = (TextView) findViewById(R.id.padcurrentTime_TextView);
                padtotalTime_TextView = (TextView) findViewById(R.id.padtotalTime_TextView);
                playbackProgress = (LinearLayout) findViewById(R.id.playbackProgress);
                playbackProgress.setVisibility(View.GONE);
                currentTime_TextView = (TextView) findViewById(R.id.currentTime_TextView);
                totalTime_TextView = (TextView) findViewById(R.id.totalTime_TextView);

                // Identify the views being used
                songscrollview = (ScrollView) findViewById(R.id.songscrollview);

                glideimage_ScrollView = (ScrollView) findViewById(R.id.glideimage_ScrollView);
                glideimage = (ImageView) findViewById(R.id.glideimage);
                testpane = (RelativeLayout) findViewById(R.id.testpane);
                testpane1_2 = (RelativeLayout) findViewById(R.id.testpane1_2);
                testpane2_2 = (RelativeLayout) findViewById(R.id.testpane2_2);
                testpane1_3 = (RelativeLayout) findViewById(R.id.testpane1_3);
                testpane2_3 = (RelativeLayout) findViewById(R.id.testpane2_3);
                testpane3_3 = (RelativeLayout) findViewById(R.id.testpane3_3);

                songscrollview.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);

                // Enable the song and author section to link to edit song
                songandauthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FullscreenActivity.whattodo = "songdetails";
                        openFragment();
                    }
                });

                // Set up the navigation drawer
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                songmenu = (LinearLayout) findViewById(R.id.songmenu);
                optionmenu = (LinearLayout) findViewById(R.id.optionmenu);
                song_list_view = (ListView) findViewById(R.id.song_list_view);
                closeSongFAB = (FloatingActionButton) findViewById(R.id.closeSongsFAB);
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
                closeSongFAB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeMyDrawers("song");
                    }
                });

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
                songscrollview.requestFocus();
                setWindowFlags();
                setWindowFlagsAdvanced();
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
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", FullscreenActivity.locale);
            String formattedTime = df.format(c.getTime());
            digitalclock.setText(formattedTime);

            // Get battery
            int i = (int) (BatteryMonitor.getBatteryStatus(StageMode.this) * 100.0f);
            String charge = i + "%";
            batterycharge.setText(charge);
            int abh = ab.getHeight();
            FullscreenActivity.ab_height = abh;
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
    public void onDataReceived(Object data) {
        // Attempt to extract the song details
        if (data!=null && (data.toString().contains("_____") || data.toString().contains("<lyrics>"))) {
            String action = ProcessSong.getSalutReceivedLocation(data.toString(), StageMode.this);

            if (action.equals("Location")) {
                holdBeforeLoading();
            } else if (action.equals("HostFile")) {
                holdBeforeLoadingXML();
            }
        }
    }

    private class CheckStorage extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            if (ActivityCompat.checkSelfPermission(StageMode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                finish();
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
            mMediaRouter.removeCallback(mMediaRouterCallback);
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
        tryCancelAsyncTasks();
        songscrollview.removeAllViews();
        if(FullscreenActivity.network.isRunningAsHost) {
            try {
                FullscreenActivity.network.stopNetworkService(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
            FullscreenActivity.network.unregisterClient(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void tryCancelAsyncTasks() {
        doCancelAsyncTask(loadsong_async);
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
        doCancelAsyncTask(mtask_autoscroll_music);
        doCancelAsyncTask(check_storage);
        doCancelAsyncTask(sharesong_async);
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

    }
    public void doCancelAsyncTask(AsyncTask ast) {
        if (ast!=null) {
            ast.cancel(true);
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
            // Now, reset the orientation.
            FullscreenActivity.orientationchanged = false;

            // Get the current orientation
            FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

            invalidateOptionsMenu();
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
    private class ShareSong extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute() {
            songscrollview.destroyDrawingCache();
            songscrollview.setDrawingCacheEnabled(true);
            FullscreenActivity.bmScreen = null;
            FullscreenActivity.bmScreen = songscrollview.getDrawingCache();
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Send this off to be processed and sent via an intent
            Intent emailIntent = ExportPreparer.exportSong(StageMode.this, FullscreenActivity.bmScreen);
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
    private class LoadCustomReusable extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... obj) {
            LoadXML.prepareLoadCustomReusable(FullscreenActivity.customreusabletoload, StageMode.this);
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
        prepareSongMenu();
        prepareOptionMenu();
        loadSong();
    }

    @Override
    public void closePopUps() {
        if (newFragment != null) {
            newFragment.dismiss();
        }
    }

    public void scrollButtons() {
        delaycheckscroll = new Handler();
        checkScrollPosition = new Runnable() {
            @Override
            public void run() {

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
                setButton = (FloatingActionButton) findViewById(R.id.setButton);
                padButton = (FloatingActionButton) findViewById(R.id.padButton);
                autoscrollButton = (FloatingActionButton) findViewById(R.id.autoscrollButton);
                metronomeButton = (FloatingActionButton) findViewById(R.id.metronomeButton);
                extraButton = (FloatingActionButton) findViewById(R.id.extraButton);
                chordButton = (FloatingActionButton) findViewById(R.id.chordButton);
                stickyButton = (FloatingActionButton) findViewById(R.id.stickyButton);
                pageselectButton = (FloatingActionButton) findViewById(R.id.pageselectButton);
                linkButton = (FloatingActionButton) findViewById(R.id.linkButton);
                chordButton_ungrouped = (FloatingActionButton) findViewById(R.id.chordButton_ungrouped);
                stickyButton_ungrouped = (FloatingActionButton) findViewById(R.id.stickyButton_ungrouped);
                pageselectButton_ungrouped = (FloatingActionButton) findViewById(R.id.pageselectButton_ungrouped);
                linkButton_ungrouped = (FloatingActionButton) findViewById(R.id.linkButton_ungrouped);
                customButton = (FloatingActionButton) findViewById(R.id.customButton);
                custom1Button = (FloatingActionButton) findViewById(R.id.custom1Button);
                custom2Button = (FloatingActionButton) findViewById(R.id.custom2Button);
                custom3Button = (FloatingActionButton) findViewById(R.id.custom3Button);
                custom1Button_ungrouped = (FloatingActionButton) findViewById(R.id.custom1Button_ungrouped);
                custom2Button_ungrouped = (FloatingActionButton) findViewById(R.id.custom2Button_ungrouped);
                custom3Button_ungrouped = (FloatingActionButton) findViewById(R.id.custom3Button_ungrouped);
                extrabuttons = (LinearLayout) findViewById(R.id.extrabuttons);
                extrabuttons.setVisibility(View.GONE);
                extrabuttons2 = (LinearLayout) findViewById(R.id.extrabuttons2);
                extrabuttons2.setVisibility(View.GONE);
                scrollDownButton = (FloatingActionButton) findViewById(R.id.scrollDownButton);
                scrollUpButton = (FloatingActionButton) findViewById(R.id.scrollUpButton);
                setBackButton = (FloatingActionButton) findViewById(R.id.setBackButton);
                setForwardButton = (FloatingActionButton) findViewById(R.id.setForwardButton);

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
                FullscreenActivity.whattodo = "page_pad";
                openFragment();
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
                FullscreenActivity.whattodo = "page_autoscroll";
                openFragment();
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
                FullscreenActivity.whattodo = "page_metronome";
                openFragment();
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
        extraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(extraButton,StageMode.this);
                if (extrabuttons.getVisibility() == View.GONE) {
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
                FullscreenActivity.whattodo = "page_chords";
                openFragment();
            }
        });
        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(linkButton,StageMode.this);
                FullscreenActivity.whattodo = "page_links";
                openFragment();
            }
        });
        stickyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(stickyButton,StageMode.this);
                FullscreenActivity.whattodo = "page_sticky";
                openFragment();
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
        chordButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(chordButton_ungrouped,StageMode.this);
                FullscreenActivity.whattodo = "page_chords";
                openFragment();
            }
        });
        linkButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(linkButton_ungrouped,StageMode.this);
                FullscreenActivity.whattodo = "page_links";
                openFragment();
            }
        });
        stickyButton_ungrouped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(stickyButton_ungrouped,StageMode.this);
                FullscreenActivity.whattodo = "page_sticky";
                openFragment();
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
                    if (extrabuttons2.getVisibility() == View.GONE) {
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
                //FullscreenActivity.indexSongInSet += 1;
                FullscreenActivity.whichDirection = "R2L";
                doMoveInSet();
            }
        });
        setBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(setBackButton,StageMode.this);
                FullscreenActivity.setMoveDirection = "back";
                //FullscreenActivity.indexSongInSet -= 1;
                FullscreenActivity.whichDirection = "L2R";
                doMoveInSet();
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
        FullscreenActivity.wasscrolling = true;
        FullscreenActivity.scrollbutton = true;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ObjectAnimator animator;

        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY() - (int) (0.60 * metrics.heightPixels);
            animator = ObjectAnimator.ofInt(glideimage_ScrollView, "scrollY", glideimage_ScrollView.getScrollY(), (int) FullscreenActivity.newPosFloat);
        } else {
            FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() - (int) (0.60 * metrics.heightPixels);
            animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
        }

        Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
        animator.setInterpolator(customInterpolator);
        animator.setDuration(1500);
        animator.start();

        // Set a runnable to check the scroll position after 1 second
        delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
        hideActionBar();
    }

    public void doScrollDown() {
        // Scroll the screen down
        FullscreenActivity.wasscrolling = true;
        FullscreenActivity.scrollbutton = true;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ObjectAnimator animator;

        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY() + (int) (0.60 * metrics.heightPixels);
            animator = ObjectAnimator.ofInt(glideimage_ScrollView, "scrollY", glideimage_ScrollView.getScrollY(), (int) FullscreenActivity.newPosFloat);
        } else {
            FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() + (int) (0.60 * metrics.heightPixels);
            animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
        }

        Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
        animator.setInterpolator(customInterpolator);
        animator.setDuration(1500);
        animator.start();

        // Set a runnable to check the scroll position after 1 second
        delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
        hideActionBar();
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
            pageselectButton.setVisibility(View.GONE);
            chordButton_ungrouped.setVisibility(View.GONE);
            stickyButton_ungrouped.setVisibility(View.GONE);
            pageselectButton_ungrouped.setVisibility(View.GONE);
            linkButton_ungrouped.setVisibility(View.GONE);
            customButton.setVisibility(View.VISIBLE);
            custom1Button.setVisibility(View.GONE);
            custom2Button.setVisibility(View.GONE);
            custom3Button.setVisibility(View.GONE);
            custom1Button_ungrouped.setVisibility(View.GONE);
            custom2Button_ungrouped.setVisibility(View.GONE);
            custom3Button_ungrouped.setVisibility(View.GONE);
        } else {
            setButton.setVisibility(View.VISIBLE);
            padButton.setVisibility(View.VISIBLE);
            autoscrollButton.setVisibility(View.VISIBLE);
            metronomeButton.setVisibility(View.VISIBLE);
            extraButton.setVisibility(View.VISIBLE);
            chordButton.setVisibility(View.VISIBLE);
            linkButton.setVisibility(View.VISIBLE);
            stickyButton.setVisibility(View.VISIBLE);
            pageselectButton.setVisibility(View.VISIBLE);
            linkButton.setVisibility(View.VISIBLE);
            stickyButton.setVisibility(View.VISIBLE);
            pageselectButton.setVisibility(View.VISIBLE);
            chordButton_ungrouped.setVisibility(View.VISIBLE);
            stickyButton_ungrouped.setVisibility(View.VISIBLE);
            pageselectButton_ungrouped.setVisibility(View.VISIBLE);
            linkButton_ungrouped.setVisibility(View.VISIBLE);
            customButton.setVisibility(View.VISIBLE);
            custom1Button.setVisibility(View.VISIBLE);
            custom2Button.setVisibility(View.VISIBLE);
            custom3Button.setVisibility(View.VISIBLE);
            custom1Button_ungrouped.setVisibility(View.VISIBLE);
            custom2Button_ungrouped.setVisibility(View.VISIBLE);
            custom3Button_ungrouped.setVisibility(View.VISIBLE);
        }

        // Now decide if we are showing extra and custom buttons ungrouped or not
        if (!FullscreenActivity.grouppagebuttons && FullscreenActivity.page_extra_grouped) {
            extraButton.setVisibility(View.VISIBLE);
            chordButton_ungrouped.setVisibility(View.GONE);
            linkButton_ungrouped.setVisibility(View.GONE);
            stickyButton_ungrouped.setVisibility(View.GONE);
            pageselectButton_ungrouped.setVisibility(View.GONE);
        } else if (!FullscreenActivity.grouppagebuttons && !FullscreenActivity.page_extra_grouped) {
            extraButton.setVisibility(View.GONE);
            chordButton_ungrouped.setVisibility(View.VISIBLE);
            linkButton_ungrouped.setVisibility(View.VISIBLE);
            stickyButton_ungrouped.setVisibility(View.VISIBLE);
            pageselectButton_ungrouped.setVisibility(View.VISIBLE);
        }
        if (!FullscreenActivity.grouppagebuttons && FullscreenActivity.page_custom_grouped) {
            customButton.setVisibility(View.VISIBLE);
            custom1Button_ungrouped.setVisibility(View.GONE);
            custom2Button_ungrouped.setVisibility(View.GONE);
            custom3Button_ungrouped.setVisibility(View.GONE);
        } else if (!FullscreenActivity.grouppagebuttons && !FullscreenActivity.page_custom_grouped){
            customButton.setVisibility(View.GONE);
            custom1Button_ungrouped.setVisibility(View.VISIBLE);
            custom2Button_ungrouped.setVisibility(View.VISIBLE);
            custom3Button_ungrouped.setVisibility(View.VISIBLE);
        }

        // Now hide any that the user doesn't want
        hideViewIfNeeded(setButton,FullscreenActivity.page_set_visible);
        hideViewIfNeeded(padButton,FullscreenActivity.page_pad_visible);
        hideViewIfNeeded(metronomeButton,FullscreenActivity.page_metronome_visible);
        hideViewIfNeeded(autoscrollButton,FullscreenActivity.page_autoscroll_visible);
        hideViewIfNeeded(chordButton,FullscreenActivity.page_chord_visible);
        hideViewIfNeeded(linkButton,FullscreenActivity.page_links_visible);
        hideViewIfNeeded(stickyButton,FullscreenActivity.page_sticky_visible);
        hideViewIfNeeded(pageselectButton,FullscreenActivity.page_pages_visible);
        hideViewIfNeeded(chordButton_ungrouped,FullscreenActivity.page_chord_visible);
        hideViewIfNeeded(linkButton_ungrouped,FullscreenActivity.page_links_visible);
        hideViewIfNeeded(stickyButton_ungrouped,FullscreenActivity.page_sticky_visible);
        hideViewIfNeeded(pageselectButton_ungrouped,FullscreenActivity.page_pages_visible);
        hideViewIfNeeded(custom1Button,FullscreenActivity.page_custom1_visible);
        hideViewIfNeeded(custom2Button,FullscreenActivity.page_custom2_visible);
        hideViewIfNeeded(custom3Button,FullscreenActivity.page_custom3_visible);
        hideViewIfNeeded(custom1Button_ungrouped,FullscreenActivity.page_custom1_visible);
        hideViewIfNeeded(custom2Button_ungrouped,FullscreenActivity.page_custom2_visible);
        hideViewIfNeeded(custom3Button_ungrouped,FullscreenActivity.page_custom3_visible);

        // Hide unnecessary ones
        if (FullscreenActivity.grouppagebuttons && !FullscreenActivity.page_pad_visible &&
                !FullscreenActivity.page_metronome_visible && !FullscreenActivity.page_autoscroll_visible &&
                !FullscreenActivity.page_chord_visible && !FullscreenActivity.page_links_visible &&
                !FullscreenActivity.page_sticky_visible && !FullscreenActivity.page_pages_visible &&
                !FullscreenActivity.page_custom1_visible && !FullscreenActivity.page_custom2_visible &&
                !FullscreenActivity.page_custom3_visible) {
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
                !FullscreenActivity.page_custom2_visible && !FullscreenActivity.page_custom3_visible) {
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
                float pageselectAlpha = FullscreenActivity.pageButtonAlpha;
                float linkAlpha = FullscreenActivity.pageButtonAlpha;
                float customAlpha = FullscreenActivity.pageButtonAlpha;
                float custom1Alpha = FullscreenActivity.pageButtonAlpha;
                float custom2Alpha = FullscreenActivity.pageButtonAlpha;
                float custom3Alpha = FullscreenActivity.pageButtonAlpha;
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
                    }
                }

                setButton.setAlpha(setAlpha);
                padButton.setAlpha(padAlpha);
                autoscrollButton.setAlpha(autoscrollAlpha);
                metronomeButton.setAlpha(metronomeAlpha);
                extraButton.setAlpha(extraAlpha);
                chordButton.setAlpha(chordsAlpha);
                stickyButton.setAlpha(stickyAlpha);
                pageselectButton.setAlpha(pageselectAlpha);
                linkButton.setAlpha(linkAlpha);
                chordButton_ungrouped.setAlpha(chordsAlpha);
                stickyButton_ungrouped.setAlpha(stickyAlpha);
                pageselectButton_ungrouped.setAlpha(pageselectAlpha);
                linkButton_ungrouped.setAlpha(linkAlpha);
                customButton.setAlpha(customAlpha);
                custom1Button.setAlpha(custom1Alpha);
                custom2Button.setAlpha(custom2Alpha);
                custom3Button.setAlpha(custom3Alpha);
                custom1Button_ungrouped.setAlpha(custom1Alpha);
                custom2Button_ungrouped.setAlpha(custom2Alpha);
                custom3Button_ungrouped.setAlpha(custom3Alpha);
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
                pageselectButton.setSize(FullscreenActivity.fabSize);
                linkButton.setSize(FullscreenActivity.fabSize);
                chordButton_ungrouped.setSize(FullscreenActivity.fabSize);
                stickyButton_ungrouped.setSize(FullscreenActivity.fabSize);
                pageselectButton_ungrouped.setSize(FullscreenActivity.fabSize);
                linkButton_ungrouped.setSize(FullscreenActivity.fabSize);
                customButton.setSize(FullscreenActivity.fabSize);
                custom1Button.setSize(FullscreenActivity.fabSize);
                custom2Button.setSize(FullscreenActivity.fabSize);
                custom3Button.setSize(FullscreenActivity.fabSize);
                custom1Button_ungrouped.setSize(FullscreenActivity.fabSize);
                custom2Button_ungrouped.setSize(FullscreenActivity.fabSize);
                custom3Button_ungrouped.setSize(FullscreenActivity.fabSize);
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
        custom1Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_1));
        custom2Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_2));
        custom3Button_ungrouped.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this, FullscreenActivity.quickLaunchButton_3));
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
                FullscreenActivity.whattodo = s;
                openFragment();
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
    }

    @Override
    public void doEdit() {
        FullscreenActivity.whattodo = "editsong";
        newFragment = PopUpEditSongFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
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
    private class DoMoveInSet extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Get the appropriate song
            FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.linkclicked;
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
                    // Set indexSongInSet position has moved
                    invalidateOptionsMenu();
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
    private class AddSlideToSet extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            // Add the slide
            CustomSlide.addCustomSlide(StageMode.this);
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

                    invalidateOptionsMenu();
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

    @Override
    public void openSongEdit() {
        // Not required really - just a legacy for FullscreenActivity.
        // When FullscreenActivity is emptied, change mListener in PopUpSongCreateFragment openSongEdit()
        // to openFragment() with FullscreenActivity.whattodo="editsong"
        FullscreenActivity.whattodo = "editsong";
        openFragment();
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
            Log.d("d","myXML="+FullscreenActivity.mySalutXML);

            // Create the temp song file
            try {
                FullscreenActivity.file = new File(getFilesDir()+"/ReceivedSong");
                FileOutputStream overWrite = new FileOutputStream(FullscreenActivity.file,	false);
                overWrite.write(FullscreenActivity.mynewXML.getBytes());
                overWrite.flush();
                overWrite.close();
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

    @Override
    public void loadSong() {
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

            // After animate out, load the song
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    glideimage_ScrollView.setVisibility(View.GONE);
                    songscrollview.setVisibility(View.GONE);
                    glideimage_ScrollView.scrollTo(0, 0);
                    songscrollview.scrollTo(0, 0);

                    // Hide the image, cause we might be loading a proper song!
                    glideimage.setBackgroundColor(0x00000000);
                    glideimage.setImageDrawable(null);

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
    private class LoadSongAsync extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
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
            prepareSongMenu();
            // Get the current song index
            try {
                ListSongFiles.getCurrentSongIndex();
            } catch (Exception e) {
                // Ooops
            }

            // If we are in a set, try to get the appropriate indexes

            SetActions.getSongForSetWork(StageMode.this);
            SetActions.indexSongInSet();

            FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

            // Now, reset the orientation.
            FullscreenActivity.orientationchanged = false;

            // Get the current orientation
            FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

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
                FullscreenActivity.songSections = ProcessSong.splitSongIntoSections(FullscreenActivity.myLyrics,StageMode.this);

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
                        FullscreenActivity.sectionLineTypes[x][y] = ProcessSong.determineLineTypes(FullscreenActivity.sectionContents[x][y],StageMode.this);
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

                    // Put the title of the song in the taskbar
                    songtitle_ab.setText(ProcessSong.getSongTitle());
                    songkey_ab.setText(ProcessSong.getSongKey());
                    songauthor_ab.setText(ProcessSong.getSongAuthor());

                    if (FullscreenActivity.isPDF) {
                        loadPDF();
                        glideimage_ScrollView.requestFocus();
                        glideimage_ScrollView.isFocused();

                    } else if (FullscreenActivity.isImage) {
                        glideimage_ScrollView.requestFocus();
                        glideimage_ScrollView.isFocused();
                        loadImage();

                    } else if (FullscreenActivity.isSong) {
                        //Prepare the song views
                        songscrollview.requestFocus();
                        songscrollview.isFocused();
                        prepareView();
                    }

                    // Automatically start the pad if we were playing


                    // Automatically start the metronome if we wanted it to
                    if (FullscreenActivity.metronomeonoff.equals("on")) {
                        // Stop it
                        Metronome.startstopMetronome(StageMode.this);
                        // Start it again with the new values
                        gesture7();
                    }

                    // Decide if we have loaded a song in the current set
                    fixSetActionButtons();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Send WiFiP2P intent
            if (FullscreenActivity.network!=null && FullscreenActivity.network.isRunningAsHost) {
                try {
                    sendSongXMLToConnected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            // If we aren't at the beginning, enable the setBackButton
            if (FullscreenActivity.indexSongInSet > 0) {
                setBackButton.setVisibility(View.VISIBLE);
            } else {
                setBackButton.setVisibility(View.INVISIBLE);
            }

            // If we aren't at the end of the set, enable the setForwardButton
            if (FullscreenActivity.indexSongInSet < FullscreenActivity.mSetList.length - 1) {
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
        float xscale = (float) widthavail / (float) imgwidth;
        float yscale = (float) heightavail / (float) imgheight;

        // Decide on the image size to use
        if (FullscreenActivity.toggleYScale.equals("Y")) {
            // Now decide on the scaling required....
            if (xscale>yscale) {
                xscale = yscale;
            }
            int glidewidth = (int) ((float)imgwidth * xscale);
            int glideheight = (int) ((float)imgheight * xscale);
            glideimage.setBackgroundColor(0x00000000);
            Glide.with(StageMode.this).load(imageUri).override(glidewidth,glideheight).into(glideimage);
        } else {
            // Now decide on the scaling required....
            int glideheight = (int) ((float)imgheight * xscale);
            glideimage.setBackgroundColor(0x00000000);
            Glide.with(StageMode.this).load(imageUri).override(widthavail,glideheight).centerCrop().into(glideimage);
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

    public void createPerformanceView1col() {
        doCancelAsyncTask(createperformanceview1col_async);
        createperformanceview1col_async = new CreatePerformanceView1Col();
        try {
            createperformanceview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class CreatePerformanceView1Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_1 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_1 = new RelativeLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            // We know how many columns we are using, so lets go for it.
            column1_1 = ProcessSong.preparePerformanceColumnView(StageMode.this);
            songbit = ProcessSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
            boxbit1_1 = ProcessSong.preparePerformanceBoxView(StageMode.this, 0, FullscreenActivity.padding);

            // Add the song sections...
            for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                float fontsize = ProcessSong.setScaledFontSize(0);
                LinearLayout sectionview = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                sectionview.setPadding(0,0,0,0);
                sectionview.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                column1_1.addView(sectionview);
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
    private class CreatePerformanceView2Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_2 = new LinearLayout(StageMode.this);
        LinearLayout column2_2 = new LinearLayout(StageMode.this);
        RelativeLayout boxbit1_2 = new RelativeLayout(StageMode.this);
        RelativeLayout boxbit2_2 = new RelativeLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
            // We know how many columns we are using, so lets go for it.
            column1_2 = ProcessSong.preparePerformanceColumnView(StageMode.this);
            column2_2 = ProcessSong.preparePerformanceColumnView(StageMode.this);
            songbit = ProcessSong.preparePerformanceSongBitView(StageMode.this, true); // true for horizontal
            boxbit1_2 = ProcessSong.preparePerformanceBoxView(StageMode.this, getPixelsFromDpi(4),FullscreenActivity.padding);
            boxbit2_2 = ProcessSong.preparePerformanceBoxView(StageMode.this, 0, FullscreenActivity.padding);

            // Add the song sections...
            for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                if (x < FullscreenActivity.halfsplit_section) {
                    float fontsize = ProcessSong.setScaledFontSize(1);
                    LinearLayout sectionview = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                    sectionview.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    sectionview.setPadding(0,0,0,0);
                    column1_2.addView(sectionview);

                } else {
                    float fontsize = ProcessSong.setScaledFontSize(2);
                    LinearLayout sectionview2 = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                    sectionview2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    sectionview2.setPadding(0,0,0,0);
                    column2_2.addView(sectionview2);
                }
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
                    sectionview.setPadding(0,0,0,0);
                    column1_3.addView(sectionview);

                } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                    float fontsize = ProcessSong.setScaledFontSize(4);
                    LinearLayout sectionview2 = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                    sectionview2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    sectionview2.setPadding(0,0,0,0);
                    column2_3.addView(sectionview2);

                } else {
                    float fontsize = ProcessSong.setScaledFontSize(5);
                    LinearLayout sectionview3 = ProcessSong.songSectionView(StageMode.this, x, fontsize, false);
                    sectionview3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    sectionview3.setPadding(0,0,0,0);
                    column3_3.addView(sectionview3);
                }
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
        // Now scroll in the song via an animation
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            songscrollview.setVisibility(View.GONE);
            glideimage_ScrollView.setVisibility(View.VISIBLE);
            glideimage_ScrollView.setFocusable(true);
            glideimage_ScrollView.isFocused();
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
            } else {
                glideimage_ScrollView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
            }
        } else {
            glideimage_ScrollView.setVisibility(View.GONE);
            songscrollview.setVisibility(View.VISIBLE);
            songscrollview.setFocusable(true);
            songscrollview.isFocused();
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
    }

    public void dualScreenWork() {
        if (FullscreenActivity.isPresenting) {
            try {
                doCancelAsyncTask(dualscreenwork_async);
                dualscreenwork_async = new DualScreenWork();
                dualscreenwork_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
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
                    PresentationService.ExternalDisplay.doUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void prepareView() {
        doCancelAsyncTask(preparesongview_async);
        preparesongview_async = new PrepareSongView();
        try {
            preparesongview_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("deprecation")
    private class PrepareSongView extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {

            rendercalled = false;
            mypage.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
            songscrollview.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
            biggestscale_1col = 0f;
            biggestscale_2col = 0f;
            biggestscale_3col = 0f;

            width_scale = 0f;

            /*// Make sure the view is animated out
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
            } else {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
            }*/

            FullscreenActivity.currentSection = 0;

            testpane.removeAllViews();
            testpane1_2.removeAllViews();
            testpane2_2.removeAllViews();
            testpane1_3.removeAllViews();
            testpane2_3.removeAllViews();
            testpane3_3.removeAllViews();
        }

        @Override
        protected String doInBackground(Object... params) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            // Set up the songviews
            FullscreenActivity.songSectionsTypes = new String[FullscreenActivity.songSections.length];
            FullscreenActivity.sectionviews = new LinearLayout[FullscreenActivity.songSections.length];
            FullscreenActivity.sectionbitmaps = new Bitmap[FullscreenActivity.songSections.length];
            FullscreenActivity.sectionScaleValue = new float[FullscreenActivity.songSections.length];
            FullscreenActivity.sectionrendered = new boolean[FullscreenActivity.songSections.length];
            FullscreenActivity.viewwidth = new int[FullscreenActivity.songSections.length];
            FullscreenActivity.viewheight = new int[FullscreenActivity.songSections.length];
            invalidateOptionsMenu();

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
    private class ResizeStageView extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            // Remove the views from the test panes if there was any!
            testpane.removeAllViews();
            testpane1_2.removeAllViews();
            testpane2_2.removeAllViews();
            testpane1_3.removeAllViews();
            testpane2_3.removeAllViews();
            testpane3_3.removeAllViews();
        }

        @Override
        protected String doInBackground(Void... voids) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            // Decide on the best scaling
            FullscreenActivity.padding = getPixelsFromDpi(16);
            int availablewidth_1col = getAvailableWidth() - getPixelsFromDpi(16);
            int availableheight = (int) (0.7f * getAvailableHeight()) - getPixelsFromDpi(16);

            for (int f = 0; f < FullscreenActivity.sectionviews.length; f++) {
                float myscale_1_1_col_x = availablewidth_1col / (float) FullscreenActivity.viewwidth[f];
                float myscale_1_1_col_y = availableheight / (float) FullscreenActivity.viewheight[f];
                FullscreenActivity.sectionScaleValue[f] = ProcessSong.getStageScaleValue(myscale_1_1_col_x, myscale_1_1_col_y);
                float maxscale = FullscreenActivity.mMaxFontSize / 12.0f;
                if (FullscreenActivity.sectionScaleValue[f] > maxscale) {
                    FullscreenActivity.sectionScaleValue[f] = maxscale;
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
    private class CreateStageView1Col extends AsyncTask<Object, Void, String> {

        LinearLayout songbit = new LinearLayout(StageMode.this);
        LinearLayout column1_1 = new LinearLayout(StageMode.this);

        @Override
        protected void onPreExecute() {
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
    private class ResizePerformanceView extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
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
        }

        @Override
        protected String doInBackground(Void... voids) {
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
    private class PrepareSongMenu extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            // List all of the songs in the current folder
            ListSongFiles.getAllSongFolders();
            ListSongFiles.getAllSongFiles();
            ListSongFiles.getSongDetails(StageMode.this);
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

                    SongMenuAdapter lva = new SongMenuAdapter(StageMode.this, songmenulist);
                    song_list_view.setAdapter(lva);
                    song_list_view.setFastScrollEnabled(true);
                    song_list_view.setScrollingCacheEnabled(true);
                    lva.notifyDataSetChanged();

                    // Set the secondary alphabetical side bar
                    SongMenuAdapter.getIndexList();
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
        LinearLayout indexLayout = (LinearLayout) findViewById(R.id.side_index);
        indexLayout.removeAllViews();
        TextView textView;
        List<String> indexList = new ArrayList<>(FullscreenActivity.mapIndex.keySet());
        for (String index : indexList) {
            textView = (TextView) View.inflate(StageMode.this,
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
            optionmenu.addView(OptionMenuListeners.prepareOptionMenu(StageMode.this));
            if (optionmenu != null) {
                OptionMenuListeners.optionListeners(optionmenu, StageMode.this);
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
        invalidateOptionsMenu();

        // Save set
        Preferences.savePreferences();

        closeMyDrawers("option");
    }

    @Override
    public void showActionBar() {
        if (FullscreenActivity.hideActionBar) {
            // Make the songscrollview not sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.horizontalscrollview).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
        } else {
            // Make the songscrollview sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.horizontalscrollview).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
        }
        toggleActionBar();
    }

    @Override
    public void hideActionBar() {
        if (FullscreenActivity.hideActionBar) {
            // Make the songscrollview not sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.horizontalscrollview).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, 0);
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
        } else {
            // Make the songscrollview sit below toolbar
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.horizontalscrollview).getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, ab_toolbar.getId());
            findViewById(R.id.horizontalscrollview).setLayoutParams(lp);
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

        if (newFragment != null) {
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

        // Set this sections alpha to 1.0f;
        FullscreenActivity.sectionviews[whichone].setAlpha(1.0f);

        // Smooth scroll to show this view at the top of the page
        // Unless we are autoscrolling
        if (!FullscreenActivity.isautoscrolling) {
            songscrollview.smoothScrollTo(0, FullscreenActivity.sectionviews[whichone].getTop());
        }

        // Go through each of the views and set the alpha of the others to 0.5f;
        for (int x = 0; x < FullscreenActivity.sectionviews.length; x++) {
            if (x != whichone) {
                FullscreenActivity.sectionviews[x].setAlpha(0.5f);
            }
        }
        FullscreenActivity.tempswipeSet = "enable";
        FullscreenActivity.setMoveDirection = "";
        invalidateOptionsMenu();

        dualScreenWork();
    }

    @Override
    public void splashScreen() {
        SharedPreferences settings = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("showSplashVersion", 0);
        editor.apply();
        Intent intent = new Intent();
        intent.setClass(StageMode.this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void confirmedAction() {
        switch (FullscreenActivity.whattodo) {
            case "exit":
                this.finish();
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
                invalidateOptionsMenu();
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
                ListSongFiles.clearAllSongs();
                refreshAll();
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

    private class PreparePad extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            FullscreenActivity.padson = true;
            PadFunctions.getPad1Status();
            PadFunctions.getPad2Status();

            if (FullscreenActivity.pad1Playing) {
                // If mPlayer1 is already playing, set this to fade out and start mPlayer2
                Log.d("d", "mPlayer1 is already playing, set this to fade out and start mPlayer2");
                FullscreenActivity.pad1Fading = true;
                FullscreenActivity.whichPad = 2;
                FullscreenActivity.padson = true;

            } else if (FullscreenActivity.pad2Playing) {
                // If mPlayer2 is already playing, set this to fade out and start mPlayer1
                Log.d("d", "mPlayer2 is already playing, set this to fade out and start mPlayer1");
                FullscreenActivity.pad2Fading = true;
                FullscreenActivity.padson = true;
                FullscreenActivity.whichPad = 1;

            } else {
                // Else nothing, was playing, so start mPlayer1
                Log.d("d", "Nothing playing, start mPlayer1");
                FullscreenActivity.whichPad = 1;
                FullscreenActivity.padson = true;
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
            Log.d("d", "mPlayer1 is prepared");
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

    private class PlayPads extends AsyncTask<Void, Void, Integer> {
        int which;
        int path;
        boolean validlinkaudio;
        boolean error;

        PlayPads(Integer w) {
            which = w;
        }

        @Override
        protected void onPreExecute() {
            if (which == 1 && FullscreenActivity.mPlayer1 != null) {
                FullscreenActivity.mPlayer1.setOnPreparedListener(new Player1Prepared());
            } else if (which == 2 && FullscreenActivity.mPlayer1 != null) {
                FullscreenActivity.mPlayer2.setOnPreparedListener(new Player2Prepared());
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            ProcessSong.processKey();

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
                                    //FullscreenActivity.mPlayer2.prepareAsync();
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
                                PadFunctions.getPad2Status();
                                if (FullscreenActivity.pad1Playing) {
                                    FullscreenActivity.mPlayer1.stop();
                                }
                                FullscreenActivity.mPlayer1.reset();
                                FullscreenActivity.mPlayer1.setOnPreparedListener(new Player1Prepared());
                                FullscreenActivity.mPlayer1.setDataSource(filetext);
                                FullscreenActivity.mPlayer1.prepareAsync();
                            } else if (which == 2) {
                                String filetext = FullscreenActivity.mLinkAudio;
                                // If this is a localised file, we need to unlocalise it to enable it to be read
                                if (filetext.startsWith("../OpenSong/")) {
                                    filetext = "file://" + filetext.replace("../OpenSong/", FullscreenActivity.homedir + "/");
                                }
                                PadFunctions.getPad2Status();
                                if (FullscreenActivity.pad2Playing) {
                                    FullscreenActivity.mPlayer2.stop();
                                }
                                FullscreenActivity.mPlayer2.reset();
                                FullscreenActivity.mPlayer2.setOnPreparedListener(new Player1Prepared());
                                FullscreenActivity.mPlayer1.setDataSource(filetext);
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

        boolean display1 = pad1status && !FullscreenActivity.pad1Fading || pad2status && FullscreenActivity.pad2Fading;

        // Decide which player we should be getting the status of
        if (display1) {
            pos = (int) (FullscreenActivity.mPlayer1.getCurrentPosition() / 1000.0f);
        } else {
            pos = (int) (FullscreenActivity.mPlayer2.getCurrentPosition() / 1000.0f);
        }
        String text = TimeTools.timeFormatFixer(pos);
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
                    fadeout_media1 = new FadeoutMediaPlayer(StageMode.this, 1);
                    fadeout_media1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;

            case 2:
                if (FullscreenActivity.pad2Playing) {
                    // mPlayer2 is playing, so fade it out.
                    doCancelAsyncTask(fadeout_media2);
                    fadeout_media2 = new FadeoutMediaPlayer(StageMode.this, 2);
                    fadeout_media2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;

            case 0:
                // Fade both pads
                if (FullscreenActivity.pad1Playing) {
                    // mPlayer1 is playing, so fade it out.
                    doCancelAsyncTask(fadeout_media1);
                    fadeout_media1 = new FadeoutMediaPlayer(StageMode.this, 1);
                    fadeout_media1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if (FullscreenActivity.pad2Playing) {
                    // mPlayer2 is playing, so fade it out.
                    doCancelAsyncTask(fadeout_media2);
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
    }

    @Override
    public void startAutoScroll() {
        totalTime_TextView.setText(TimeTools.timeFormatFixer(FullscreenActivity.autoScrollDuration));
        playbackProgress.setVisibility(View.VISIBLE);
        AutoScrollFunctions.getAutoScrollValues(songscrollview, mypage, ab_toolbar);
        doCancelAsyncTask(mtask_autoscroll_music);
        FullscreenActivity.isautoscrolling = true;
        mtask_autoscroll_music = new AutoScrollMusic();
        try {
            mtask_autoscroll_music.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class AutoScrollMusic extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            FullscreenActivity.scrollpageHeight = songscrollview.getChildAt(0).getMeasuredHeight() - songscrollview.getHeight();
            FullscreenActivity.time_start = System.currentTimeMillis();
        }

        @Override
        protected String doInBackground(String... args) {
            while (FullscreenActivity.isautoscrolling) {
                long starttime = System.currentTimeMillis();
                FullscreenActivity.time_passed = System.currentTimeMillis();
                boolean doscroll = ((FullscreenActivity.time_passed - FullscreenActivity.time_start) / 1000) >= FullscreenActivity.autoScrollDelay;
                if (doscroll) {
                    publishProgress(1);
                }
                // don't scroll first time
                if (!FullscreenActivity.pauseautoscroll) {
                    AutoScrollFunctions.ProgressTimeRunnable runnable = new AutoScrollFunctions.ProgressTimeRunnable(currentTime_TextView);
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

                long currtime = System.currentTimeMillis();
                while ((currtime - starttime) < FullscreenActivity.autoscroll_pause_time) {
                    currtime = System.currentTimeMillis();
                }
            }
            return "dummy";
        }

        @Override
        protected void onProgressUpdate(Integer... intg) {
            if (!FullscreenActivity.wasscrolling) {
                FullscreenActivity.newPosFloat = FullscreenActivity.newPosFloat + FullscreenActivity.autoscroll_pixels;
            } else {
                FullscreenActivity.newPosFloat = songscrollview.getScrollY();
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
                    playbackProgress.setVisibility(View.INVISIBLE);
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
    }

    public boolean checkCanScrollDown() {
        int height;
        boolean showscrolldown;
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            try {
                height = glideimage_ScrollView.getChildAt(0).getMeasuredHeight() - glideimage_ScrollView.getHeight();
            } catch (Exception e) {
                height = 0;
            }
            FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY();
            showscrolldown = height > glideimage_ScrollView.getScrollY() && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
        } else {
            try {
                height = songscrollview.getChildAt(0).getMeasuredHeight() - songscrollview.getHeight();
            } catch (Exception e) {
                height = 0;
            }
            FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
            showscrolldown = height > songscrollview.getScrollY() && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
        }

        // Decide if the down arrow should be displayed.
        return showscrolldown;
    }

    public boolean checkCanScrollUp() {
        boolean showscrollup;
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            FullscreenActivity.newPosFloat = (float) glideimage_ScrollView.getScrollY();
            showscrollup =  glideimage_ScrollView.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
        } else {
            FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
            showscrollup =  songscrollview.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu);
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
                && !linkButton.isPressed() && !stickyButton.isPressed() && !pageselectButton.isPressed()
                && !customButton.isPressed() && !custom1Button.isPressed() && !custom2Button.isPressed()
                && !custom3Button.isPressed()
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
                            doScrollDown();
                        } else {
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
                            doScrollUp();
                        } else {
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
                        doScrollUp();
                    }
                }

            } else if (keyCode == FullscreenActivity.pageturner_DOWN) {
                if (mDrawerLayout.isDrawerOpen(songmenu)) {
                    // Scroll the song menu down
                    scrollMenu("down");
                } else {
                    if (checkCanScrollDown()) {
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
                gesture1();
                break;
            case "2":
                gesture2();
                break;
            case "3":
                gesture3();
                break;
            case "4":
                gesture4();
                break;
            case "5":
                gesture5();
                break;
            case "6":
                gesture6();
                break;
            case "7":
                gesture7();
                break;
            default:
                break;
        }

        if (action.length() > 0) {
            FullscreenActivity.shortKeyPress = false;
            FullscreenActivity.longKeyPress = true;
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class simpleOnScaleGestureListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
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
            // 0/else = off (highest menu item)

            // First test conditions
            if (oktoregistergesture()) {

                // Now find out which gesture we've gone for
                switch (FullscreenActivity.gesture_doubletap) {
                    case "1":
                        gesture1();

                        break;
                    case "2":
                        if (justSong(StageMode.this)) {
                            gesture2();
                        }

                        break;
                    case "3":
                        gesture3();

                        break;
                    case "4":
                        gesture4();

                        break;
                    case "5":
                        gesture5();

                        break;
                    case "6":
                        gesture6();

                        break;
                    case "7":
                        gesture7();
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
                        gesture1();
                        break;
                    case "2":
                        gesture2();
                        break;
                    case "3":
                        gesture3();
                        break;
                    case "4":
                        gesture4();
                        break;
                    case "5":
                        gesture5();
                        break;
                    case "6":
                        gesture6();
                        break;
                    case "7":
                        gesture7();
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

            FullscreenActivity.SWIPE_MIN_DISTANCE = (int) ((float) getAvailableWidth() / 3f);       // Quarter the screen width
            FullscreenActivity.SWIPE_MAX_OFF_PATH = FullscreenActivity.SWIPE_MIN_DISTANCE / 3;     // Quarter the allowed width
            FullscreenActivity.SWIPE_THRESHOLD_VELOCITY = FullscreenActivity.SWIPE_MIN_DISTANCE * 3; //Cover this in one second

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
                    goToNextItem();
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
                    goToPreviousItem();
                } catch (Exception e) {
                    // No song before
                }
                return true;
            }
            return false;
        }
    }

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

    public void gesture2() {
        if (justSong(StageMode.this)) {
            // Edit the song
            FullscreenActivity.whattodo = "editsong";
            openFragment();
        }
    }

    public void gesture3() {
        // Add to set
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
        invalidateOptionsMenu();
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

    public void gesture4() {
        // Redraw the lyrics page
        loadSong();
    }

    @Override
    public void gesture5() {
        // Stop or start autoscroll
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
    @SuppressLint("ServiceCast")
    public void updateDisplays() {
        // This is called when display devices are changed (connected, disconnected, etc.)
        Intent intent = new Intent(StageMode.this,
                StageMode.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                StageMode.this, 0, intent, 0);

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
    @Override
    public void refreshSecondaryDisplay(String which) {
        try {
            switch (which) {
                case "all":
                case "chords":
                case "autoscale":
                case "maxfontsize":
                case "manualfontsize":
                default:
                    PresentationService.ExternalDisplay.doUpdate();
                    break;

                case "info":
                    PresentationService.ExternalDisplay.updateFonts();
                    break;

                case "backgrounds":
                    PresentationService.ExternalDisplay.fixBackground();
                    break;

                case "margins":
                    PresentationService.ExternalDisplay.changeMargins();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}