package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.cast.CastMediaControlIntent;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class StageMode extends AppCompatActivity implements
        PopUpAreYouSureFragment.MyInterface,
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
        PopUpQuickLaunchSetup.MyInterface, PopUpLongSongPressFragment.MyInterface {

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
    LinearLayout songmenu;
    LinearLayout optionmenu;
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
    float column1_1_scale;
    float column1_2_scale;
    float column2_2_scale;
    float column1_3_scale;
    float column2_3_scale;
    float column3_3_scale;
    LinearLayout backingtrackProgress;
    TextView padcurrentTime_TextView;
    TextView padtotalTime_TextView;
    LinearLayout playbackProgress;
    TextView currentTime_TextView;
    TextView totalTime_TextView;
    float biggestscale_1col = 1000.0f;
    float biggestscale_2col = 1000.0f;
    float biggestscale_3col = 1000.0f;
    boolean overridingfull;
    boolean overridingwidth;
    boolean rendercalled = false;

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
    FloatingActionButton customButton;
    FloatingActionButton custom1Button;
    FloatingActionButton custom2Button;
    FloatingActionButton custom3Button;
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

    // Dialogue fragments and stuff
    DialogFragment newFragment;

    // Gestures
    ScaleGestureDetector scaleGestureDetector;
    GestureDetector gestureDetector;

    // ASyncTask stuff
    AsyncTask<Object, Void, String> loadsong_async;
    AsyncTask<Object, Void, String> preparesongview_async;
    AsyncTask<Object, Void, String> preparesongmenu_async;
    AsyncTask<Object, Void, String> prepareoptionmenu_async;
    AsyncTask<Void, Void, String>[] resizesection_async;
    AsyncTask<Object,Void,String> fadeout_media1;
    AsyncTask<Object,Void,String> fadeout_media2;
    AsyncTask<String,Integer,String> mtask_autoscroll_music;

    // Allow the menus to flash open to show where they are on first run
    boolean firstrun_option = true;
    boolean firstrun_song   = true;

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

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("d","Welcome to Stage Mode");

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        checkStorage();

        // Clear the cache to maximise memory
        System.gc();

        // Set the fullscreen window flags
        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                setWindowFlags();
                setWindowFlagsAdvanced();

                // Load up the user preferences
                Preferences.loadPreferences();

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
                        newFragment = PopUpSongDetailsFragment.newInstance();
                        newFragment.show(getFragmentManager(), "dialog");
                    }
                });

                // Set up the navigation drawer
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                songmenu = (LinearLayout) findViewById(R.id.songmenu);
                optionmenu = (LinearLayout) findViewById(R.id.optionmenu);
                song_list_view = (ListView) findViewById(R.id.song_list_view);
                menuFolder_TextView = (TextView) findViewById(R.id.menuFolder_TextView);
                menuFolder_TextView.setText(FullscreenActivity.whichSongFolder);
                changefolder_LinearLayout = (LinearLayout) findViewById(R.id.changefolder_LinearLayout);
                changefolder_LinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FullscreenActivity.whattodo = "choosefolder";
                        openFragment();
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

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        // Fix the page flags
        setWindowFlags();
        setWindowFlagsAdvanced();
    }

    @Override
    public void onStop() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
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
                showpagebuttons();
            }

            // Called when a drawer has settled in a completely open state.
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                FullscreenActivity.tempswipeSet="disable";
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
        waitandtest.postDelayed(testnavbar,1000);
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
        if (delayactionBarHide!=null && hideActionBarRunnable!=null) {
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

    public void setUpBatteryMonitor() {
        // Get clock
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm",FullscreenActivity.locale);
        String formattedTime = df.format(c.getTime());
        digitalclock.setText(formattedTime);

        // Get battery
        int i = (int) (BatteryMonitor.getBatteryStatus(StageMode.this) * 100.0f);
        String charge = i + "%";
        batterycharge.setText(charge);
        int abh = ab.getHeight();
        if (ab!=null && abh>0) {
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
        },60000);
    }

    public void checkStorage() {
        AsyncTask<Object,Void,String> check_storage = new CheckStorage();
        try {
            check_storage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class CheckStorage extends AsyncTask<Object,Void,String>{

        @Override
        protected String doInBackground(Object... objects) {
            if (ActivityCompat.checkSelfPermission(StageMode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
            return null;
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
        if (mMediaRouteSelector!=null) {
            mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        }

        // Force overflow icon to show, even if hardware key is present
        MenuHandlers.forceOverFlow(StageMode.this,ab,menu);

        // If we are not in set mode, then hide the forward and back icons
        MenuItem presentationMode;
        MenuItem stageMode;
        presentationMode = menu.findItem(R.id.present_mode);
        stageMode = menu.findItem(R.id.stage_mode);

        // Decide if song is in the set
        SetActions.isSongInSet();

        if (presentationMode!=null) {
            presentationMode.setVisible(FullscreenActivity.dualDisplayCapable);
            presentationMode.getIcon().setAlpha(MenuHandlers.dualScreenAlpha());
        }

        if (stageMode!=null) {
            stageMode.setVisible(FullscreenActivity.dualDisplayCapable);
            stageMode.getIcon().setAlpha(MenuHandlers.dualScreenAlpha());
        }

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        songscrollview.removeAllViews();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FullscreenActivity.orientationchanged = FullscreenActivity.mScreenOrientation != newConfig.orientation;
        //actionBarDrawerToggle.onConfigurationChanged(newConfig);
        if (FullscreenActivity.orientationchanged) {
            invalidateOptionsMenu();
            closeMyDrawers("both");
            resizeDrawers();
            loadSong();
        }
    }

    @Override
    public void shareSong() {
        if (!FullscreenActivity.isSong) {
            // Editing a slide / note / scripture / image
            FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
            ShowToast.showToast(StageMode.this);
        } else {
            // Export - Take a screenshot as a bitmap
            AsyncTask<Object, Void, String> sharesong_async = new ShareSong();
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
            Intent emailIntent = ExportPreparer.exportSong(StageMode.this,FullscreenActivity.bmScreen);
            startActivityForResult(Intent.createChooser(emailIntent, FullscreenActivity.exportcurrentsong), 12345);
            return null;
        }
    }

    @Override
    public void loadCustomReusable() {
        AsyncTask<Object,Void,String> load_customreusable = new LoadCustomReusable();
        try {
            load_customreusable.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class LoadCustomReusable extends AsyncTask<Object,Void,String>{

        @Override
        protected String doInBackground(Object...obj) {
            LoadXML.prepareLoadCustomReusable(FullscreenActivity.customreusabletoload);
            return null;
        }

        @Override
        protected void onPostExecute(String s){
            // This reopens the choose backgrounds popupFragment
            newFragment = PopUpCustomSlideFragment.newInstance();
            newFragment.show(getFragmentManager(), "dialog");
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
        if (newFragment!=null) {
            newFragment.dismiss();
        }
    }

    public void scrollButtons() {
        delaycheckscroll = new Handler();
        checkScrollPosition = new Runnable() {
            @Override
            public void run() {
                int height;
                try {
                    height = songscrollview.getChildAt(0).getMeasuredHeight() - songscrollview.getHeight();
                } catch (Exception e) {
                    height=0;
                }

                // Decide if the down arrow should be displayed.
                if (height > songscrollview.getScrollY() && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu)) {
                    scrollDownButton.setVisibility(View.VISIBLE);
                } else {
                    scrollDownButton.setVisibility(View.INVISIBLE);
                }
                // Decide if the up arrow should be displayed.
                if (songscrollview.getScrollY() > 0 && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerOpen(songmenu)) {
                    scrollUpButton.setVisibility(View.VISIBLE);
                } else {
                    scrollUpButton.setVisibility(View.INVISIBLE);
                }
                FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY();
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
                customButton = (FloatingActionButton) findViewById(R.id.customButton);
                custom1Button = (FloatingActionButton) findViewById(R.id.custom1Button);
                custom2Button = (FloatingActionButton) findViewById(R.id.custom2Button);
                custom3Button = (FloatingActionButton) findViewById(R.id.custom3Button);
                extrabuttons = (LinearLayout) findViewById(R.id.extrabuttons);
                extrabuttons.setVisibility(View.GONE);
                extrabuttons2 = (LinearLayout) findViewById(R.id.extrabuttons2);
                extrabuttons2.setVisibility(View.GONE);
                scrollDownButton = (FloatingActionButton) findViewById(R.id.scrollDownButton);
                scrollUpButton = (FloatingActionButton) findViewById(R.id.scrollUpButton);
                setBackButton = (FloatingActionButton) findViewById(R.id.setBackButton);
                setForwardButton = (FloatingActionButton) findViewById(R.id.setForwardButton);

                setupQuickLaunchButtons();

            }
        });
        // Set the alphas
        pageButtonAlpha("");

        // Set the sizes

        // Set the listeners
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "editset";
                openFragment();
            }
        });
        padButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "page_pad";
                openFragment();
            }
        });
        padButton.setOnLongClickListener(new View.OnLongClickListener() {
                                             @Override
                                             public boolean onLongClick(View view) {
                                                 gesture6();
                                                 return true;
                                             }
                                         });
        autoscrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "page_autoscroll";
                openFragment();
            }
        });
        autoscrollButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                gesture5();
                return true;
            }
        });
        metronomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "page_metronome";
                openFragment();
            }
        });
        metronomeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                gesture7();
                return true;
            }
        });
        extraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                FullscreenActivity.whattodo = "page_chords";
                openFragment();
            }
        });
        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "page_links";
                openFragment();
            }
        });
        stickyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "page_sticky";
                openFragment();
            }
        });
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extrabuttons2.getVisibility() == View.GONE) {
                    pageButtonAlpha("custom");
                } else {
                    pageButtonAlpha("");
                }
            }
        });
        scrollUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doScrollUp();
            }
        });
        scrollDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doScrollDown();
            }
        });
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
        FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() - (int) (0.60 * metrics.heightPixels);
        ObjectAnimator animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
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
        FullscreenActivity.newPosFloat = (float) songscrollview.getScrollY() + (int) (0.60 * metrics.heightPixels);
        ObjectAnimator animator = ObjectAnimator.ofInt(songscrollview, "scrollY", songscrollview.getScrollY(), (int) FullscreenActivity.newPosFloat);
        Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
        animator.setInterpolator(customInterpolator);
        animator.setDuration(1500);
        animator.start();

        // Set a runnable to check the scroll position after 1 second
        delaycheckscroll.postDelayed(checkScrollPosition, FullscreenActivity.checkscroll_time);
        hideActionBar();
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
                customButton.setAlpha(customAlpha);
                custom1Button.setAlpha(custom1Alpha);
                custom2Button.setAlpha(custom2Alpha);
                custom3Button.setAlpha(custom3Alpha);
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
                customButton.setSize(FullscreenActivity.fabSize);
                custom1Button.setSize(FullscreenActivity.fabSize);
                custom2Button.setSize(FullscreenActivity.fabSize);
                custom3Button.setSize(FullscreenActivity.fabSize);
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
        custom1Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this,FullscreenActivity.quickLaunchButton_1));
        custom2Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this,FullscreenActivity.quickLaunchButton_2));
        custom3Button.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(StageMode.this,FullscreenActivity.quickLaunchButton_3));
        custom1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customButtonAction(FullscreenActivity.quickLaunchButton_1);
            }
        });
        custom2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customButtonAction(FullscreenActivity.quickLaunchButton_2);
            }
        });
        custom3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customButtonAction(FullscreenActivity.quickLaunchButton_3);
            }
        });
    }

    public void customButtonAction(String s) {
        switch(s) {
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
    public void showpagebuttons() {

    }

    @Override
    public void onSongImportDone(String message) {
        FullscreenActivity.myToastMessage = message;
        if (!message.equals("cancel")) {
            ShowToast.showToast(StageMode.this);
            prepareSongMenu();
        }
        OnSongConvert.doBatchConvert(StageMode.this);
    }

    @Override
    public void backupInstall(String message) {
        // Songs have been imported, so update the song menu
        FullscreenActivity.myToastMessage = message;
        ShowToast.showToast(StageMode.this);
        prepareSongMenu();
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
        AsyncTask<Object,Void,String> open_drawers = new OpenMyDrawers(which);
        try {
            open_drawers.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class OpenMyDrawers extends AsyncTask<Object,Void,String> {

        String which;
        OpenMyDrawers(String w) {
            which = w;
        }

        @Override
        protected String doInBackground(Object... obj) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            DrawerTweaks.openMyDrawers(mDrawerLayout,songmenu,optionmenu,which);
        }
    }

    @Override
    public void closeMyDrawers(String which) {
        AsyncTask<Object,Void,String> open_drawers = new CloseMyDrawers(which);
        try {
            open_drawers.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class CloseMyDrawers extends AsyncTask<Object,Void,String> {

        String which;
        CloseMyDrawers(String w) {
            which = w;
        }

        @Override
        protected String doInBackground(Object... obj) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            DrawerTweaks.closeMyDrawers(mDrawerLayout,songmenu,optionmenu,which);
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

    public void resizeDrawers() {
        AsyncTask<Object,Void,String> resize_drawers = new ResizeDrawers();
        try {
            resize_drawers.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public class ResizeDrawers extends AsyncTask<Object,Void,String> {
        int width;

        @Override
        protected String doInBackground(Object...o) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = metrics.widthPixels / 2;
            return null;
        }

        @Override
        protected void onPostExecute(String s){
            songmenu.setLayoutParams(DrawerTweaks.resizeMenu(songmenu, width));
            optionmenu.setLayoutParams(DrawerTweaks.resizeMenu(optionmenu, width));
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
        AsyncTask<Object,Void,String> do_moveinset = new DoMoveInSet();
        try {
            do_moveinset.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class DoMoveInSet extends AsyncTask<Object,Void,String> {

        @Override
        protected String doInBackground(Object... objects) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            // Get the next set positions and song
            SetActions.doMoveInSet(StageMode.this);

            // Set indexSongInSet position has moved
            invalidateOptionsMenu();
        }
    }

    @Override
    public void refreshActionBar() {
        invalidateOptionsMenu();
    }

    @Override
    public void indexingDone() {
        AsyncTask<Object,Void,String> indexing_done = new IndexingDone();
        try {
            indexing_done.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class IndexingDone extends AsyncTask<Object,Void,String>{

        @Override
        protected String doInBackground(Object... objects) {
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

            for (int d=0;d<FullscreenActivity.search_database.size();d++) {
                String[] songbits = FullscreenActivity.search_database.get(d).split("_%%%_");
                if (songbits[0]!=null && songbits[1]!=null && songbits[2]!=null && songbits[3]!=null &&
                        songbits[4]!=null && songbits[5]!=null && songbits[6]!=null && songbits[7]!=null) {
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

        @Override
        protected void onPostExecute(String s) {
            song_list_view.setTextFilterEnabled(true);
            song_list_view.setFastScrollEnabled(true);

            for (int i = 0; i < FullscreenActivity.search_database.size(); i++) {
                SearchViewItems song = new SearchViewItems(FullscreenActivity.searchFileName.get(i),
                        FullscreenActivity.searchTitle.get(i) ,
                        FullscreenActivity.searchFolder.get(i),
                        FullscreenActivity.searchAuthor.get(i),
                        FullscreenActivity.searchKey.get(i),
                        FullscreenActivity.searchTheme.get(i),
                        FullscreenActivity.searchShortLyrics.get(i),
                        FullscreenActivity.searchHymnNumber.get(i));
                FullscreenActivity.searchlist.add(song);
            }

            FullscreenActivity.sva = new SearchViewAdapter(StageMode.this, FullscreenActivity.searchlist, "songmenu");
            song_list_view.setTextFilterEnabled(true);
            song_list_view.setFastScrollEnabled(true);
            FullscreenActivity.sva.notifyDataSetChanged();
            song_list_view.setAdapter(FullscreenActivity.sva);
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
        if (FullscreenActivity.sva!=null) {
            FullscreenActivity.sva.getFilter().filter(newText);
        }
        return false;
    }

    @Override
    public void addSlideToSet() {
        AsyncTask<Object,Void,String> add_slidetoset = new AddSlideToSet();
        try {
            add_slidetoset.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class AddSlideToSet extends AsyncTask<Object,Void,String>{

        @Override
        protected String doInBackground(Object... objects) {
            // Add the slide
            CustomSlide.addCustomSlide();
            return null;
        }

        @Override
        protected void onPostExecute(String s){
            // Tell the user that the song has been added.
            FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.customslide_title + "\" " + getResources().getString(R.string.addedtoset);
            ShowToast.showToast(StageMode.this);

            // Vibrate to let the user know something happened
            DoVibrate.vibrate(StageMode.this,200);

            invalidateOptionsMenu();
            prepareOptionMenu();
            closeMyDrawers("option_delayed");
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
    public void stickyNotesUpdate() {
        // Not required really - just a legacy for FullscreenActivity.
        // When FullscreenActivity is emptied, change mListener in PopUpStickyEdit stickyNoteUpdate()
        // to loadSong()
        loadSong();
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
    public void loadSong() {
        // Animate out the current song
        if (FullscreenActivity.whichDirection.equals("L2R")) {
            songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
        } else {
            songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
        }

        // After animate out, load the song
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                songscrollview.setAlpha(0.0f);
                // Load the song
                if (loadsong_async!=null) {
                    loadsong_async.cancel(true);
                }
                loadsong_async = new LoadSongAsync();
                try {
                    loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },200);
    }
    private class LoadSongAsync extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            if (!FullscreenActivity.isPDF) {
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }

            // Open the current folder and list the songs
            ListSongFiles.getAllSongFiles();
            prepareSongMenu();
            // Get the current song index
            ListSongFiles.getCurrentSongIndex();

            FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

            // Now, reset the orientation.
            FullscreenActivity.orientationchanged = false;

            // Get the current orientation
            FullscreenActivity.mScreenOrientation = getResources().getConfiguration().orientation;

            // Clear the old headings (presention order looks for these)
            FullscreenActivity.foundSongSections_heading = new ArrayList<>();

            // Check the chord format
            Transpose.checkChordFormat();

            FullscreenActivity.presenterChords = "Y";

            // Sort song formatting
            // 1. Sort multiline verse/chord formats
            FullscreenActivity.myLyrics = ProcessSong.fixMultiLineFormat(FullscreenActivity.mLyrics);

            // 2. Split the song into sections
            FullscreenActivity.songSections = ProcessSong.splitSongIntoSections(FullscreenActivity.myLyrics);

            // 3. Put the song into presentation order if required
            if (FullscreenActivity.usePresentationOrder && !FullscreenActivity.mPresentation.equals("")) {
                FullscreenActivity.songSections = ProcessSong.matchPresentationOrder(FullscreenActivity.songSections);
            }

            FullscreenActivity.songSections = ProcessSong.splitLaterSplits(FullscreenActivity.songSections);

            // 4. Get the section headings/types (may have changed after presentationorder
            FullscreenActivity.songSectionsLabels = new String[FullscreenActivity.songSections.length];
            for (int sl=0; sl < FullscreenActivity.songSections.length; sl++) {
                FullscreenActivity.songSectionsLabels[sl] = ProcessSong.getSectionHeadings(FullscreenActivity.songSections[sl]);
            }

            // We need to split each section into string arrays by line
            ProcessSong.trimSongSections();
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

            if (FullscreenActivity.whichMode.equals("Performance")) {
                // Put the song back together for checking for splitpoints
                String rebuiltlyrics = ProcessSong.rebuildParsedLyrics(FullscreenActivity.songSections.length);

                FullscreenActivity.numrowstowrite = FullscreenActivity.myParsedLyrics.length;

                // Look for song split points
                ProcessSong.lookForSplitPoints(rebuiltlyrics);

            }
            return "done";
        }

        protected void onPostExecute(String s) {
            // Fix the page flags
            setWindowFlags();
            setWindowFlagsAdvanced();

            // Show the ActionBar
            if (delayactionBarHide!=null && hideActionBarRunnable!=null) {
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

            //Prepare the song views
            prepareView();

            // Decide if we have loaded a song in the current set
            fixSetActionButtons();
        }
    }

    public void fixSetActionButtons() {
        FullscreenActivity.setView = SetActions.isSongInSet();
        if (FullscreenActivity.setView) {
            // Now get the position in the set and decide on the set move buttons
            if (FullscreenActivity.indexSongInSet < 0) {
                // We weren't in set mode, so find the first instance of this song.
                SetActions.indexSongInSet();

                // If we aren't at the beginning, enable the setBackButton
                if (FullscreenActivity.indexSongInSet>0) {
                    setBackButton.setVisibility(View.VISIBLE);
                } else {
                    setBackButton.setVisibility(View.GONE);
                }

                // If we aren't at the end of the set, enable the setForwardButton
                if (FullscreenActivity.indexSongInSet<FullscreenActivity.mSetList.length-1) {
                    setForwardButton.setVisibility(View.VISIBLE);
                } else {
                    setForwardButton.setVisibility(View.GONE);
                }
            }
        } else {
            FullscreenActivity.indexSongInSet = -1;
            setBackButton.setVisibility(View.GONE);
            setForwardButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void prepareView() {
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
            biggestscale_1col = 1000.0f;
            biggestscale_2col = 1000.0f;
            biggestscale_3col = 1000.0f;

            // Make sure the view is animated out
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
            } else {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
            }

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
            // Set up the songviews
            FullscreenActivity.songSectionsTypes = new String[FullscreenActivity.songSections.length];
            FullscreenActivity.sectionviews = new View[FullscreenActivity.songSections.length];
            FullscreenActivity.sectionScaleValue = new float[FullscreenActivity.songSections.length];
            FullscreenActivity.sectionrendered = new boolean[FullscreenActivity.songSections.length];
            FullscreenActivity.viewwidth = new int[FullscreenActivity.songSections.length];
            FullscreenActivity.viewheight = new int[FullscreenActivity.songSections.length];

            resizesection_async = new ResizeSongSections[FullscreenActivity.songSections.length];
            invalidateOptionsMenu();

            return null;
        }

        protected void onPostExecute(String s) {

            // For stage mode, each section gets its own box
            // For performance mode, all the sections get added into the one box

            if (column1_1!=null) {
                column1_1.removeAllViews();
            }
            if (column1_2!=null) {
                column1_2.removeAllViews();
            }
            if (column2_2!=null) {
                column2_2.removeAllViews();
            }
            if (column1_3!=null) {
                column1_3.removeAllViews();
            }
            if (column2_3!=null) {
                column2_3.removeAllViews();
            }
            if (column3_3!=null) {
                column3_3.removeAllViews();
            }

            column1_1 = ProcessSong.createLinearLayout(StageMode.this);
            column1_2 = ProcessSong.createLinearLayout(StageMode.this);
            column2_2 = ProcessSong.createLinearLayout(StageMode.this);
            column1_3 = ProcessSong.createLinearLayout(StageMode.this);
            column2_3 = ProcessSong.createLinearLayout(StageMode.this);
            column3_3 = ProcessSong.createLinearLayout(StageMode.this);

            // Go through each section
            for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                LinearLayout section1_1;
                LinearLayout section1_2;
                LinearLayout section2_2;
                LinearLayout section1_3;
                LinearLayout section2_3;
                LinearLayout section3_3;

                // The single stage mode or 1 column performance mode view
                final LinearLayout section = ProcessSong.songSectionView(StageMode.this, x);

                // The other views for 2 or 3 column mode
                section1_1 = ProcessSong.songSectionView(StageMode.this, x);
                section1_1.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                column1_1.addView(section1_1);

                if (x < FullscreenActivity.halfsplit_section) {
                    section1_2 = ProcessSong.songSectionView(StageMode.this, x);
                    section1_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    column1_2.addView(section1_2);
                } else {
                    section2_2 = ProcessSong.songSectionView(StageMode.this, x);
                    section2_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    column2_2.addView(section2_2);
               }

                if (x < FullscreenActivity.thirdsplit_section) {
                    section1_3 = ProcessSong.songSectionView(StageMode.this, x);
                    column1_3.addView(section1_3);
                } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                    section2_3 = ProcessSong.songSectionView(StageMode.this, x);
                    column2_3.addView(section2_3);
                } else {
                    section3_3 = ProcessSong.songSectionView(StageMode.this, x);
                    column3_3.addView(section3_3);
                }

                if (FullscreenActivity.whichMode.equals("Stage")) {
                    // Stage Mode
                    testpane.addView(section);
                    final int val = x;
                    ViewTreeObserver vto = section.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                section.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                section.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                            resizeSection(section, val);
                        }
                    });
                }
            }

            if (FullscreenActivity.whichMode.equals("Performance")) {
                FullscreenActivity.songSectionsTypes = new String[6];
                FullscreenActivity.sectionviews = new View[6];
                FullscreenActivity.sectionScaleValue = new float[6];
                FullscreenActivity.sectionrendered = new boolean[6];
                FullscreenActivity.viewwidth = new int[6];
                FullscreenActivity.viewheight = new int[6];
                resizesection_async = new ResizeSongSections[6];

                // Performance Mode
                testpane.addView(column1_1);
                testpane1_2.addView(column1_2);
                testpane2_2.addView(column2_2);
                testpane1_3.addView(column1_3);
                testpane2_3.addView(column2_3);
                testpane3_3.addView(column3_3);

                testpane.setVisibility(View.INVISIBLE);
                testpane1_2.setVisibility(View.INVISIBLE);
                testpane2_2.setVisibility(View.INVISIBLE);
                testpane1_3.setVisibility(View.INVISIBLE);
                testpane2_3.setVisibility(View.INVISIBLE);
                testpane3_3.setVisibility(View.INVISIBLE);

                // Create View Tree Observers to listen for the view being drawn in multicolumn mode
                ViewTreeObserver[] vto_cols = new ViewTreeObserver[6];
                vto_cols[0] = testpane.getViewTreeObserver();
                vto_cols[1] = testpane1_2.getViewTreeObserver();
                vto_cols[2] = testpane2_2.getViewTreeObserver();
                vto_cols[3] = testpane1_3.getViewTreeObserver();
                vto_cols[4] = testpane2_3.getViewTreeObserver();
                vto_cols[5] = testpane3_3.getViewTreeObserver();

                final LinearLayout[] vto_views = new LinearLayout[6];
                vto_views[0] = column1_1;
                vto_views[1] = column1_2;
                vto_views[2] = column2_2;
                vto_views[3] = column1_3;
                vto_views[4] = column2_3;
                vto_views[5] = column3_3;

                //
                for (int x=0; x<vto_cols.length; x++) {
                    final int val = x;
                    vto_cols[x].addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                vto_views[val].getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                vto_views[val].getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }

                            resizeSection(vto_views[val], val);
                        }
                    });
                }
            }
        }
    }

    public void resizeSection(LinearLayout v, final int section) {
        resizesection_async[section] = new ResizeSongSections(v,testpane,section);
        try {
            resizesection_async[section].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public class ResizeSongSections extends AsyncTask<Void, Void, String> {

        LinearLayout v;
        RelativeLayout test;
        int width;
        int height;
        int paddingspace;
        int available_width;
        int available_width_1;
        int available_width_2;
        int available_width_3;
        int available_height;
        int section;
        float myscale;

        // Decide on the max scale size, based on max font size
        float maxscale = FullscreenActivity.mMaxFontSize / 6.0f;
        // Decide on the min scale size, based on min font size
        float minscale = FullscreenActivity.mMinFontSize / 6.0f;
        float nonscaled = FullscreenActivity.mFontSize / 6.0f;

        ResizeSongSections(LinearLayout v, RelativeLayout test, int section){
            this.v = v;
            this.test = test;
            this.section = section;
        }

        @Override
        protected void onPreExecute() {

            // The padding space that will be used in the section boxes is 8dp each side = 16dp
            // However, to convert to pixels, we need to scale by the screen density

            width = v.getWidth();
            height = v.getHeight();

            paddingspace = getPixelsFromDpi(16);
            FullscreenActivity.padding = paddingspace;

            available_width = getAvailableWidth();
            available_height = getAvailableHeight();

        }

        @Override
        protected String doInBackground(Void... voids) {

            // Scaling depends on which mode we are in
            // For stage mode, each item is scaled individually
            // For performance mode, we need to scale each possible column and then decide on the best view

            if (FullscreenActivity.whichMode.equals("Stage")) {
                available_width = available_width - paddingspace;      // Remove the padding space
                available_height = (int) (0.70f * available_height) - paddingspace;   // Remove the padding space
                float x_scale = ((float) available_width / (float) width);
                float y_scale = ((float) available_height / (float) height);
                myscale = x_scale;
                if (x_scale >= y_scale) {
                    myscale = y_scale;
                }

                // Decide on the max scale size, based on max font size
                if (myscale > maxscale) {
                    myscale = maxscale;
                }

                // Do the scaling
                int new_width = (int) ((width)* myscale);
                int new_height = (int) (height * myscale);

                FullscreenActivity.sectionrendered[section] = true;
                FullscreenActivity.viewwidth[section] = new_width;
                FullscreenActivity.viewheight[section] = new_height;
                FullscreenActivity.sectionScaleValue[section] = myscale;

            } else {
                // We are in performance mode
                // Decide on the available widths for 1,2 or 3 column views
                // Decide the padding space
                // Padding on each side -> 2*padding
                // Thickness of the box on each side --> 2*2
                // Padding inside the box on each side --> 2*12
                available_width_1 = available_width - paddingspace;                                            // Remove the padding space
                available_width_2 = (int) ((available_width) / 2.0f) - paddingspace - getPixelsFromDpi(4);   // Remove the padding space
                available_width_3 = (int) ((available_width) / 3.0f) - paddingspace - getPixelsFromDpi(4)*2; // Remove the padding space
                available_height = available_height - paddingspace;                                            // Remove the padding space
                float x_scale;
                float y_scale = ((float) available_height / (float) height);
                switch (section) {
                    case 0:
                    default:
                        // Single column view
                        x_scale = ((float) available_width_1 / (float) width);
                        // Use the smallest of the two scale values so we can fit everything in
                        myscale = x_scale;
                        if (x_scale >= y_scale) {
                            myscale = y_scale;
                        }
                        // If we are scaling to width only
                        if (FullscreenActivity.toggleYScale.equals("W")) {
                            myscale = x_scale;
                        }

                        // If autoscale is off
                        if (FullscreenActivity.toggleYScale.equals("N")) {
                            // Test font size is 6.0f - base scaling on this
                            myscale = FullscreenActivity.mFontSize / 6.0f;
                        }
                        // Decide on the max scale size, based on max font size
                        if (myscale > maxscale) {
                            myscale = maxscale;
                        }
                        break;

                    case 1:
                    case 2:
                        // Two column view
                        x_scale = ((float) available_width_2 / (float) width);
                        // Use the smallest of the two scale values so we can fit everything in
                        myscale = x_scale;
                        if (x_scale >= y_scale) {
                            myscale = y_scale;
                        }
                        // If we are scaling to width only
                        if (FullscreenActivity.toggleYScale.equals("W")) {
                            myscale = x_scale;
                        }

                        // If autoscale is off
                        if (FullscreenActivity.toggleYScale.equals("N")) {
                            // Test font size is 6.0f - base scaling on this
                            myscale = FullscreenActivity.mFontSize / 6.0f;
                        }
                        // Decide on the max scale size, based on max font size
                        if (myscale > maxscale) {
                            myscale = maxscale;
                        }
                        break;

                    case 3:
                    case 4:
                    case 5:
                        // Three column view
                        x_scale = ((float) available_width_3 / (float) width);
                        // Use the smallest of the two scale values so we can fit everything in
                        myscale = x_scale;
                        if (x_scale >= y_scale) {
                            myscale = y_scale;
                        }
                        // If we are scaling to width only
                        if (FullscreenActivity.toggleYScale.equals("W")) {
                            myscale = x_scale;
                        }

                        // If autoscale is off
                        if (FullscreenActivity.toggleYScale.equals("N")) {
                            // Test font size is 6.0f - base scaling on this
                            myscale = FullscreenActivity.mFontSize / 6.0f;
                        }
                        // Decide on the max scale size, based on max font size
                        if (myscale > maxscale) {
                            myscale = maxscale;
                        }
                        break;
                }


                // Decide on the min scale size, based on min font size


                // If we've had to go smaller than a previous column, set this is as the biggest scale
                switch (section) {
                    case 0:
                        if (myscale < biggestscale_1col) {
                            biggestscale_1col = myscale;
                        }
                        break;
                    case 1:
                    case 2:
                        if (myscale < biggestscale_2col) {
                            biggestscale_2col = myscale;
                        }
                        break;
                    case 3:
                    case 4:
                    case 5:
                        if (myscale < biggestscale_3col) {
                            biggestscale_3col = myscale;
                        }
                        break;
                }
                FullscreenActivity.sectionrendered[section] = true;
                FullscreenActivity.sectionScaleValue[section] = myscale;
            }
            return "done";
        }

        @Override
        protected void onPostExecute(String s) {

            if (section==0) {
                FullscreenActivity.sectionScaleValue[0] = myscale;
                column1_1_scale = myscale;
            } else if (section==1) {
                FullscreenActivity.sectionScaleValue[1] = myscale;
                column1_2_scale = myscale;
            } else if (section==2) {
                FullscreenActivity.sectionScaleValue[2] = myscale;
                column2_2_scale = myscale;
            } else if (section==3) {
                FullscreenActivity.sectionScaleValue[3] = myscale;
                column1_3_scale = myscale;
            } else if (section==4) {
                FullscreenActivity.sectionScaleValue[4] = myscale;
                column2_3_scale = myscale;
            } else if (section==5) {
                FullscreenActivity.sectionScaleValue[5] = myscale;
                column3_3_scale = myscale;
            }

            test.removeView(v);
            FullscreenActivity.sectionviews[section] = v;

            if (FullscreenActivity.whichMode.equals("Stage")) {
                // Stage mode with separate sections
                FullscreenActivity.sectionviews[section].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectSection(section);
                    }
                });

                // Check if all the views are rendered, if so, add them to the song section view
                boolean alldone = true;
                for (View yesorno : FullscreenActivity.sectionviews) {
                    if (yesorno==null) {
                        alldone = false;
                    }
                }
                if (alldone) {
                    if (!rendercalled) {
                        // Go through each section and do the scaling
                        for (int w=0;w<FullscreenActivity.sectionviews.length;w++) {
                            FullscreenActivity.sectionviews[w].setPivotX(0.5f);
                            FullscreenActivity.sectionviews[w].setPivotY(0.5f);
                            FullscreenActivity.sectionviews[w].setScaleX(FullscreenActivity.sectionScaleValue[w]);
                            FullscreenActivity.sectionviews[w].setScaleY(FullscreenActivity.sectionScaleValue[w]);
                        }

                        renderthesongsections();
                        rendercalled = true;
                    }
                }

            } else {
                // Performance mode
                boolean alldone = true;
                for (int r = 0; r < 6; r++) {
                    if (!FullscreenActivity.sectionrendered[r] || FullscreenActivity.sectionviews[r]==null) {
                        alldone = false;
                    }
                }

                if (alldone) {
                    // Go through each section and scale to the biggest size
                    // The best view is the one with the biggest scale size
                    float scaletouse = biggestscale_1col;
                    coltouse = 1;
                    if (biggestscale_2col>scaletouse) {
                        coltouse = 2;
                    }
                    if (biggestscale_3col>scaletouse) {
                        coltouse = 3;
                    }

                    // Decide if 2 cols or 3 cols are all bigger than the minscale
                    float smallest2colscale;
                    if (column1_2_scale>column2_2_scale) {
                        smallest2colscale = column2_2_scale;
                    } else {
                        smallest2colscale = column1_2_scale;
                    }
                    float smallest3colscale = column1_3_scale;
                    if (column2_3_scale<smallest3colscale) {
                        smallest3colscale = column2_3_scale;
                    }
                    if (column3_3_scale<smallest3colscale) {
                        smallest3colscale = column3_3_scale;
                    }

                    boolean acceptable1cols = column1_1_scale>=minscale;
                    coltouse = 1;

                    boolean acceptable2cols = column1_2_scale>=minscale && column2_2_scale>=minscale;
                    if (FullscreenActivity.toggleYScale.equals("Y") && acceptable2cols && smallest2colscale>column1_1_scale && smallest2colscale>smallest3colscale) {
                        coltouse = 2;
                        overridingfull = false;
                        overridingwidth = false;
                    }

                    boolean acceptable3cols = column1_3_scale>=minscale && column2_3_scale>=minscale && column3_3_scale>=minscale;
                    if (FullscreenActivity.toggleYScale.equals("Y") && acceptable3cols && smallest2colscale>column1_1_scale && smallest3colscale>=smallest2colscale) {
                        coltouse = 3;
                        overridingfull = false;
                        overridingwidth = false;
                    }

                    if (!acceptable1cols && !acceptable2cols && !acceptable3cols &&
                            FullscreenActivity.toggleYScale.equals("Y") && FullscreenActivity.override_fullscale) {
                        coltouse = 1;
                        FullscreenActivity.sectionScaleValue[0] = (float)available_width / (float)FullscreenActivity.sectionviews[0].getWidth();
                        overridingfull = true;

                        if (FullscreenActivity.sectionScaleValue[0]<minscale && FullscreenActivity.override_widthscale) {
                            FullscreenActivity.sectionScaleValue[0] = nonscaled;
                            overridingwidth = true;
                        }
                    }

                    for (int w=0;w<FullscreenActivity.sectionviews.length;w++) {
                        // Do the scaling
                        FullscreenActivity.sectionviews[w].setPivotX(0.5f);
                        FullscreenActivity.sectionviews[w].setPivotY(0.5f);
                        FullscreenActivity.sectionviews[w].setScaleX(FullscreenActivity.sectionScaleValue[w]);
                        FullscreenActivity.sectionviews[w].setScaleY(FullscreenActivity.sectionScaleValue[w]);
                    }

                    for (int r = 0; r < FullscreenActivity.sectionrendered.length; r++) {
                        width = FullscreenActivity.sectionviews[r].getWidth();
                        height = FullscreenActivity.sectionviews[r].getHeight();
                        int new_width = (int) (width * FullscreenActivity.sectionScaleValue[r]);
                        int new_height = (int) (height * FullscreenActivity.sectionScaleValue[r]);
                        FullscreenActivity.viewwidth[r] = new_width;
                        FullscreenActivity.viewheight[r] = new_height;
                    }
                    renderthesongsections();
                }
            }
        }
    }

    public int getAvailableWidth(){
        int val;
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        Method mGetRawW;

        try {
            // For JellyBeans and onward
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                display.getRealMetrics(metrics);
                val = metrics.widthPixels;
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

        return val;
    }

    public int getAvailableHeight(){
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
            val = val - ab.getHeight();
        }

        return val;
    }

    public int getPixelsFromDpi(int dps) {
        return dps * (int) (getResources().getDisplayMetrics().densityDpi / 160f);
    }

    public void renderthesongsections() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout mysongsections = new LinearLayout(StageMode.this);
                mysongsections.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
                mysongsections.setOrientation(LinearLayout.VERTICAL);
                songscrollview.removeAllViews();

                if (FullscreenActivity.whichMode.equals("Stage")) {
                    for (int z = 0; z < FullscreenActivity.sectionContents.length; z++) {
                        ProcessSong.removeParents(FullscreenActivity.sectionviews[z]);
                        if (FullscreenActivity.sectionviews[z] != null && FullscreenActivity.sectionviews[z].getParent()==null) {
                            ProcessSong.setUpStageModeSectionView(z);
                            RelativeLayout sectionholder = ProcessSong.createStageModeSectionBox(StageMode.this,z);
                            mysongsections.addView(sectionholder);
                        }
                    }

                    if (FullscreenActivity.sectionviews[0]!=null) {
                        // Make the first section active (full alpha)
                        try {
                            FullscreenActivity.sectionviews[0].setAlpha(1.0f);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    // In Performance Mode, so decide which view to create

                    int height = getAvailableHeight();
                    int width = getAvailableWidth();

                    LinearLayout columns = new LinearLayout(StageMode.this);
                    ScrollView.LayoutParams col_llp = new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT);
                    columns.setLayoutParams(col_llp);

                    if (FullscreenActivity.toggleYScale.equals("W") ||
                            (FullscreenActivity.toggleYScale.equals("Y") && FullscreenActivity.override_fullscale && overridingfull && !overridingwidth)) {
                        Log.d("d","Overriding to width only");
                        coltouse = 1;
                        width = FullscreenActivity.viewwidth[0];
                        height = FullscreenActivity.viewheight[0];
                    } else if (FullscreenActivity.toggleYScale.equals("N") ||
                            (FullscreenActivity.toggleYScale.equals("Y") && FullscreenActivity.override_widthscale && overridingwidth)) {
                        Log.d("d","Overriding to autoscale off");
                        coltouse = 1;
                        width = FullscreenActivity.viewwidth[0];
                        height = FullscreenActivity.viewheight[0];
                    }

                    // If we have overriden the scaling, there will be a toast message ready
                    FullscreenActivity.myToastMessage = "";
                    if (coltouse==1 && (FullscreenActivity.override_fullscale || FullscreenActivity.override_widthscale)) {
                        int pageheight = getAvailableHeight();
                        int pagewidth = getAvailableWidth();
                        if (height>pageheight) {
                            // Page is taller than available, so definitely overriding
                            FullscreenActivity.myToastMessage = getResources().getString(R.string.override_fullautoscale);
                            if (width>pagewidth) {
                                // Page is wider than available, so definitely overriding
                                FullscreenActivity.myToastMessage = getResources().getString(R.string.override_widthautoscale);
                            }
                        }
                        ShowToast.showToast(StageMode.this);
                    }

                    switch (coltouse) {

                        case 1:
                            ProcessSong.removeParents(FullscreenActivity.sectionviews[0]);
                            ProcessSong.setWidthAndHeightOfSectionViewIfZero(0);
                            ProcessSong.setUpPerformanceModeSectionView(0,width - getPixelsFromDpi(16),height - getPixelsFromDpi(16));
                            RelativeLayout sectionholder = ProcessSong.createPerformanceModeSectionBox(StageMode.this,0,0);
                            columns.addView(sectionholder);
                            break;

                        case 2:
                            columns.setOrientation(LinearLayout.HORIZONTAL);
                            ProcessSong.removeParents(FullscreenActivity.sectionviews[1]);
                            ProcessSong.setWidthAndHeightOfSectionViewIfZero(1);
                            ProcessSong.setUpPerformanceModeSectionView(1,width / 2 - getPixelsFromDpi(16 + 2),height - getPixelsFromDpi(16));
                            RelativeLayout sectionholder1_2 = ProcessSong.createPerformanceModeSectionBox(StageMode.this,1,getPixelsFromDpi(4));
                            columns.addView(sectionholder1_2);

                            ProcessSong.removeParents(FullscreenActivity.sectionviews[2]);
                            ProcessSong.setWidthAndHeightOfSectionViewIfZero(2);
                            ProcessSong.setUpPerformanceModeSectionView(2,width / 2 - getPixelsFromDpi(16 + 2),height - getPixelsFromDpi(16));
                            RelativeLayout sectionholder2_2 = ProcessSong.createPerformanceModeSectionBox(StageMode.this,2,0);
                            columns.addView(sectionholder2_2);
                            break;

                        case 3:
                            columns.setOrientation(LinearLayout.HORIZONTAL);

                            ProcessSong.removeParents(FullscreenActivity.sectionviews[3]);
                            ProcessSong.setWidthAndHeightOfSectionViewIfZero(3);
                            ProcessSong.setUpPerformanceModeSectionView(3,width / 3 - getPixelsFromDpi(16 + 3),height - getPixelsFromDpi(16));
                            RelativeLayout sectionholder1_3 = ProcessSong.createPerformanceModeSectionBox(StageMode.this,3,getPixelsFromDpi(3));
                            columns.addView(sectionholder1_3);

                            ProcessSong.removeParents(FullscreenActivity.sectionviews[4]);
                            ProcessSong.setWidthAndHeightOfSectionViewIfZero(4);
                            ProcessSong.setUpPerformanceModeSectionView(4,width / 3 - getPixelsFromDpi(16 + 3),height - getPixelsFromDpi(16));
                            RelativeLayout sectionholder2_3 = ProcessSong.createPerformanceModeSectionBox(StageMode.this,4,getPixelsFromDpi(3));
                            columns.addView(sectionholder2_3);

                            ProcessSong.removeParents(FullscreenActivity.sectionviews[5]);
                            ProcessSong.setWidthAndHeightOfSectionViewIfZero(5);
                            ProcessSong.setUpPerformanceModeSectionView(5,width / 3 - getPixelsFromDpi(16 + 3),height - getPixelsFromDpi(16));
                            RelativeLayout sectionholder3_3 = ProcessSong.createPerformanceModeSectionBox(StageMode.this,5,0);
                            columns.addView(sectionholder3_3);
                            break;
                    }
                    mysongsections.addView(columns);
                }

                songscrollview.addView(mysongsections);
                HorizontalScrollView.LayoutParams hsvlp = new HorizontalScrollView.LayoutParams(HorizontalScrollView.LayoutParams.WRAP_CONTENT,HorizontalScrollView.LayoutParams.WRAP_CONTENT);
                songscrollview.setLayoutParams(hsvlp);
                songscrollview.scrollTo(0, 0);

                songscrollview.setAlpha(1.0f);
                // Now scroll in the song via an animation
                if (FullscreenActivity.whichDirection.equals("L2R")) {
                    songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
                } else {
                    songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
                }

                // Set the overrides back
                overridingwidth = false;
                overridingfull = false;

                // Check the scroll buttons
                onScrollAction();
            }
        });
    }

    @Override
    public void showToastMessage(String message) {
        if (message!=null && !message.isEmpty()) {
            FullscreenActivity.myToastMessage = message;
            ShowToast.showToast(StageMode.this);
        }
    }

    @Override
    public void prepareSongMenu() {
        if (preparesongmenu_async != null) {
            preparesongmenu_async.cancel(true);
        }
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
            // List all of the songs in the current folder
            ListSongFiles.getAllSongFolders();
            ListSongFiles.getAllSongFiles();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            // Set the name of the current folder
            menuFolder_TextView.setText(FullscreenActivity.whichSongFolder);

            // Get the song indexes
            ListSongFiles.getCurrentSongIndex();

            // Set the ListView to show the songs
            ArrayAdapter<String> lva = new SongMenuAdapter(StageMode.this, FullscreenActivity.mSongFileNames);
            song_list_view.setAdapter(lva);
            song_list_view.setFastScrollEnabled(true);
            song_list_view.setScrollingCacheEnabled(true);
            lva.notifyDataSetChanged();

            // Listen for long clicks in the song menu (songs only, not folders) - ADD TO SET!!!!
            song_list_view.setOnItemLongClickListener(SongMenuListeners.myLongClickListener(StageMode.this));

            // Listen for short clicks in the song menu (songs only, not folders) - OPEN SONG!!!!
            song_list_view.setOnItemClickListener(SongMenuListeners.myShortClickListener(StageMode.this));

            // Flick the song drawer open once it is ready
            findSongInFolders();
            if (firstrun_song) {
                openMyDrawers("song");
                closeMyDrawers("song_delayed");
                firstrun_song = false;
            }
        }
    }

    @Override
    public void prepareOptionMenu() {
        if (prepareoptionmenu_async!=null) {
            prepareoptionmenu_async.cancel(true);
        }
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
            if (optionmenu!=null) {
                OptionMenuListeners.optionListeners(optionmenu, StageMode.this);
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Get the current set list
            SetActions.prepareSetList();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (firstrun_option) {
                openMyDrawers("option");
                closeMyDrawers("option_delayed");
                firstrun_option = false;
            }
        }

    }

    @Override
    public void songLongClick() {
        // Rebuild the set list as we've just added a song
        SetActions.prepareSetList();
        closeMyDrawers("song");
        openMyDrawers("option");
        closeMyDrawers("option_delayed");
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
        DoVibrate.vibrate(StageMode.this,200);

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
        FullscreenActivity.myToastMessage = "\"" + tempSong + "\" "
                + getResources().getString(R.string.removedfromset);
        ShowToast.showToast(StageMode.this);

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
        IndexSongs.IndexMySongs task = new IndexSongs.IndexMySongs(StageMode.this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        }
    }

    @Override
    public void openFragment() {
        // Initialise the newFragment
        newFragment = null;
        String message = "dialog";
        switch (FullscreenActivity.whattodo) {
            case "loadset":
            case "saveset":
            case "deleteset":
            case "exportset":
                newFragment = PopUpListSetsFragment.newInstance();
                break;

            case "clearset":
                message = getResources().getString(R.string.options_clearthisset);
                newFragment = PopUpAreYouSureFragment.newInstance(message);
                break;

            case "deletesong":
                message = getResources().getString(R.string.options_song_delete) +
                        " \"" + FullscreenActivity.songfilename + "\"?";
                newFragment = PopUpAreYouSureFragment.newInstance(message);
                break;

            case "customcreate":
                newFragment = PopUpCustomSlideFragment.newInstance();
                break;

            case "editset":
            case "setitemvariation":
                newFragment = PopUpSetViewNew.newInstance();
                break;

            case "editsong":
                newFragment = PopUpEditSongFragment.newInstance();
                break;

            case "editnotes":
                newFragment = PopUpEditStickyFragment.newInstance();
                break;

            case "renamesong":
                newFragment = PopUpSongRenameFragment.newInstance();
                break;

            case "createsong":
                newFragment = PopUpSongCreateFragment.newInstance();
                break;

            case "transpose":
                newFragment = PopUpTransposeFragment.newInstance();
                break;

            case "chordformat":
                newFragment = PopUpChordFormatFragment.newInstance();
                break;

            case "changetheme":
                newFragment = PopUpThemeChooserFragment.newInstance();
                break;

            case "autoscale":
                newFragment = PopUpScalingFragment.newInstance();
                break;

            case "changefonts":
                newFragment = PopUpFontsFragment.newInstance();
                break;

            case "pagebuttons":
                newFragment = PopUpPageButtonsFragment.newInstance();
                break;

            case "popupsettings":
                newFragment = PopUpDefaultsFragment.newInstance();
                break;

            case "extra":
                newFragment = PopUpExtraInfoFragment.newInstance();
                break;

            case "profiles":
                newFragment = PopUpProfileFragment.newInstance();
                break;

            case "footpedal":
                newFragment = PopUpPedalsFragment.newInstance();
                break;

            case "quicklaunch":
                newFragment = PopUpQuickLaunchSetup.newInstance();
                break;

            case "gestures":
                newFragment = PopUpGesturesFragment.newInstance();
                break;

            case "newfolder":
                message = "create";
                newFragment = PopUpSongFolderRenameFragment.newInstance(message);
                break;

            case "editfoldername":
                message = "rename";
                newFragment = PopUpSongFolderRenameFragment.newInstance(message);
                break;

            case "managestorage":
                newFragment = PopUpStorageFragment.newInstance();
                break;

            case "customstoragefind":
                newFragment = PopUpDirectoryChooserFragment.newInstance();
                Bundle args = new Bundle();
                args.putString("type", "folder");
                newFragment.setArguments(args);
                break;

            case "filechooser":
                newFragment = PopUpDirectoryChooserFragment.newInstance();
                Bundle args2 = new Bundle();
                args2.putString("type", "file");
                newFragment.setArguments(args2);
                break;

            case "wipeallsongs":
                newFragment = PopUpAreYouSureFragment.newInstance(getResources().getString(R.string.wipesongs));
                break;

            case "errorlog":
                newFragment = PopUpWebViewFragment.newInstance();
                break;

            case "crossfade":
                newFragment = PopUpCrossFadeFragment.newInstance();
                break;

            case "autoscrolldefaults":
                newFragment = PopUpAutoScrollDefaultsFragment.newInstance();
                break;

            case "language":
                newFragment = PopUpLanguageFragment.newInstance();
                break;

            case "fullsearch":
                newFragment = PopUpFullSearchFragment.newInstance();
                break;

            case "choosefolder":
                newFragment = PopUpChooseFolderFragment.newInstance();
                break;

            case "choosechordformat":
                newFragment = PopUpChordFormatFragment.newInstance();
                break;

            case "importosb":
                newFragment = PopUpImportExternalFile.newInstance();
                break;

            case "importos":
                newFragment = PopUpImportExternalFile.newInstance();
                break;

            case "page_pad":
                newFragment = PopUpPadFragment.newInstance();
                break;

            case "page_autoscroll":
                newFragment = PopUpAutoscrollFragment.newInstance();
                break;

            case "page_metronome":
                newFragment = PopUpMetronomeFragment.newInstance();
                break;

            case "page_chords":
                newFragment = PopUpChordsFragment.newInstance();
                break;

            case "customchords":
                newFragment = PopUpCustomChordsFragment.newInstance();
                break;

            case "page_links":
                newFragment = PopUpLinks.newInstance();
                break;

            case "page_sticky":
                newFragment = PopUpStickyFragment.newInstance();
                break;

            case "page_pageselect":
                newFragment = PopUpPagesFragment.newInstance();
                break;

            case "songlongpress":
                newFragment = PopUpLongSongPressFragment.newInstance();
                break;

        }

        if (newFragment!=null) {
            newFragment.show(getFragmentManager(), message);
        } else {
            FullscreenActivity.myToastMessage = "Fragment not found!";
            ShowToast.showToast(StageMode.this);
        }

        //FullscreenActivity.whattodo = "";

    }

    public void selectSection(int whichone) {
        FullscreenActivity.currentSection = whichone;

        // Set this sections alpha to 1.0f;
        FullscreenActivity.sectionviews[whichone].setAlpha(1.0f);

        // Smooth scroll to show this view at the top of the page
        // Unless we are autoscrolling
        if (!FullscreenActivity.isautoscrolling) {
            songscrollview.smoothScrollTo(0,((RelativeLayout)(FullscreenActivity.sectionviews[whichone].getParent())).getTop());
        }

        // Go through each of the views and set the alpha of the others to 0.5f;
        for (int x=0; x<FullscreenActivity.sectionviews.length; x++) {
            if (x!=whichone) {
                FullscreenActivity.sectionviews[x].setAlpha(0.5f);
            }
        }
        FullscreenActivity.tempswipeSet = "enable";
        FullscreenActivity.setMoveDirection = "";
        invalidateOptionsMenu();
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
                ListSongFiles.clearAllSongs(StageMode.this);
                refreshAll();
        }
    }

    @Override
    public void preparePad() {
        backingtrackProgress.setVisibility(View.VISIBLE);
        AsyncTask<Void,Void,Integer> prepare_pad = new PreparePad();
        try {
            prepare_pad.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class PreparePad extends AsyncTask<Void,Void,Integer> {

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
                Log.d("d","mPlayer2 is already playing, set this to fade out and start mPlayer1");
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

        @Override
        protected void onPostExecute(Integer i) {
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
            } catch (Exception e ){
                e.printStackTrace();
            }
        }
    }

    public class Player1Prepared implements MediaPlayer.OnPreparedListener {
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
    public class Player2Prepared implements MediaPlayer.OnPreparedListener {

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
        AsyncTask<Void,Void,Integer> play_pads = new PlayPads(which);
        try {
            play_pads.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class PlayPads extends AsyncTask<Void,Void,Integer> {
        int which;
        int path;
        boolean validlinkaudio;
        boolean error;

        PlayPads(Integer w) {
            which = w;
        }

        @Override
        protected void onPreExecute(){
            if (which == 1 && FullscreenActivity.mPlayer1!=null) {
                FullscreenActivity.mPlayer1.setOnPreparedListener(new Player1Prepared());
            } else if (which == 2 && FullscreenActivity.mPlayer1!=null) {
                FullscreenActivity.mPlayer2.setOnPreparedListener(new Player2Prepared());
            }
        }

        @Override
        protected Integer doInBackground(Void... voids){

            ProcessSong.processKey();

            return which;
        }

        @Override
        protected void onPostExecute(Integer i){
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
                        which=0;
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
                        filetext = filetext.replace("file://","");
                        // If this is a localised file, we need to unlocalise it to enable it to be read
                        if (filetext.startsWith("../OpenSong/")) {
                            filetext = filetext.replace("../OpenSong/",FullscreenActivity.homedir+"/");
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
                            filetext = "file://"+filetext.replace("../OpenSong/",FullscreenActivity.homedir+"/");
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
                    Log.d("d","Something went wrong with the media");
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
                Log.d("d","Can't touch the view - "+e);
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
                    if (fadeout_media1!=null) {
                        fadeout_media1.cancel(true);
                    }
                    fadeout_media1 = new FadeoutMediaPlayer(StageMode.this,1);
                    fadeout_media1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;

            case 2:
                if (FullscreenActivity.pad2Playing) {
                    // mPlayer2 is playing, so fade it out.
                    if (fadeout_media2!=null) {
                        fadeout_media2.cancel(true);
                    }
                    fadeout_media2 = new FadeoutMediaPlayer(StageMode.this,2);
                    fadeout_media2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;

            case 0:
                // Fade both pads
                if (FullscreenActivity.pad1Playing) {
                    // mPlayer1 is playing, so fade it out.
                    if (fadeout_media1!=null) {
                        fadeout_media1.cancel(true);
                    }
                    fadeout_media1 = new FadeoutMediaPlayer(StageMode.this,1);
                    fadeout_media1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if (FullscreenActivity.pad2Playing) {
                    // mPlayer2 is playing, so fade it out.
                    if (fadeout_media2!=null) {
                        fadeout_media2.cancel(true);
                    }
                    fadeout_media2 = new FadeoutMediaPlayer(StageMode.this,2);
                    fadeout_media2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
        }

        // Set a runnable to check for the pads on or off to hide the player progress
        handle.postDelayed(padoncheck,13000); // Cross fade has finished
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
    public void startAutoScroll(){
        totalTime_TextView.setText(TimeTools.timeFormatFixer(FullscreenActivity.autoScrollDuration));
        playbackProgress.setVisibility(View.VISIBLE);
        AutoScrollFunctions.getAutoScrollValues(songscrollview,mypage,ab_toolbar);
        if (mtask_autoscroll_music != null) {
            mtask_autoscroll_music.cancel(true);
            mtask_autoscroll_music = null;
        }
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
            FullscreenActivity.newPosFloat = FullscreenActivity.newPosFloat + FullscreenActivity.autoscroll_pixels;
        }

        @Override
        protected void onPostExecute(String dummy) {
            if (!FullscreenActivity.isautoscrolling) {
                FullscreenActivity.pauseautoscroll = false;
            } else {
                FullscreenActivity.isautoscrolling = false;
                FullscreenActivity.pauseautoscroll = true;
            }

            FullscreenActivity.popupAutoscroll_stoporstart = "stop";
            FullscreenActivity.autoscrollonoff = "off";
            if (mtask_autoscroll_music != null) {
                mtask_autoscroll_music.cancel(true);
                mtask_autoscroll_music = null;
            }
            playbackProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCancelled() {
            FullscreenActivity.isautoscrolling = false;
            FullscreenActivity.pauseautoscroll = true;
            FullscreenActivity.popupAutoscroll_stoporstart = "stop";
            FullscreenActivity.autoscrollonoff = "off";
            if (mtask_autoscroll_music != null) {
                mtask_autoscroll_music.cancel(true);
            }
        }
    }

    @Override
    public void stopAutoScroll() {
        playbackProgress.setVisibility(View.GONE);
        if (mtask_autoscroll_music != null) {
            mtask_autoscroll_music.cancel(true);
            mtask_autoscroll_music = null;
        }
        FullscreenActivity.isautoscrolling = false;
    }




    // The page action gestures stuff is below
    public boolean oktoregistergesture() {

        boolean oktogo = false;

        if (!FullscreenActivity.pressing_button  // Button pressing
                && !setButton.isFocused() && !padButton.isFocused() && !autoscrollButton.isFocused()
                && !metronomeButton.isFocused() && !extraButton.isFocused() && !chordButton.isFocused()
                && !linkButton.isFocused() && !stickyButton.isFocused() && !pageselectButton.isFocused()
                && !customButton.isFocused() && !custom1Button.isFocused() && !custom2Button.isFocused()
                && !custom3Button.isFocused()
                && !scrollDownButton.isFocused() && !scrollUpButton.isFocused()
                && !mDrawerLayout.isDrawerOpen(songmenu) && !mDrawerLayout.isDrawerVisible(songmenu)
                && !mDrawerLayout.isDrawerOpen(optionmenu) && !mDrawerLayout.isDrawerVisible(optionmenu)) {
            oktogo =true;
        }

        return oktogo;
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

                } else {
                    if (delayactionBarHide!=null && hideActionBarRunnable!=null) {
                        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
                    }
                }
                FullscreenActivity.wasscrolling = false;
                FullscreenActivity.scrollbutton = false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    public class SwipeDetector extends GestureDetector.SimpleOnGestureListener {

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
                        if (FullscreenActivity.isPDF) {
                            // Can't do this action on a pdf!
                            FullscreenActivity.myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                            ShowToast.showToast(StageMode.this);
                        } else if (!FullscreenActivity.isSong) {
                            // Editing a slide / note / scripture / image
                            FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
                            ShowToast.showToast(StageMode.this);
                        } else {
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
                        if (FullscreenActivity.isPDF) {
                            // Can't do this action on a pdf!
                            FullscreenActivity.myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                            ShowToast.showToast(StageMode.this);
                        } else if (!FullscreenActivity.isSong) {
                            // Editing a slide / note / scripture / image
                            FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
                            ShowToast.showToast(StageMode.this);
                        } else {
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
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            FullscreenActivity.wasscrolling = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {

            FullscreenActivity.SWIPE_MIN_DISTANCE = (int) ((float)getAvailableWidth()/3f);       // Third the screen width
            FullscreenActivity.SWIPE_MAX_OFF_PATH = FullscreenActivity.SWIPE_MIN_DISTANCE/2;     // Half the allowed width
            FullscreenActivity.SWIPE_THRESHOLD_VELOCITY = FullscreenActivity.SWIPE_MIN_DISTANCE; //Cover this in one second

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
                    // Set the direction as right to left
                    FullscreenActivity.whichDirection = "R2L";
                    // If we are viewing a set, move to the next song.

                    if (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent < (FullscreenActivity.pdfPageCount - 1)) {
                        FullscreenActivity.pdfPageCurrent = FullscreenActivity.pdfPageCurrent + 1;
                        //redrawTheLyricsTable(main_page);
                        return false;
                    } else {
                        FullscreenActivity.pdfPageCurrent = 0;
                    }

                    if (FullscreenActivity.setSize > 1 && FullscreenActivity.setView && FullscreenActivity.indexSongInSet >= 0
                            && FullscreenActivity.indexSongInSet < (FullscreenActivity.setSize - 1)) {
                        // temporarily disable swipe
                        FullscreenActivity.tempswipeSet = "disable";
                        FullscreenActivity.indexSongInSet += 1;
                        doMoveInSet();
                        // Set a runnable to reset swipe back to original value
                        // after 1 second
                        Handler delayfadeinredraw = new Handler();
                        delayfadeinredraw.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FullscreenActivity.tempswipeSet = "enable";
                            }
                        }, FullscreenActivity.delayswipe_time);

                    }
                    // If not in a set, see if we can move to the next song in the list
                    // Only if swipeSet is Y (not S)
                    if (!FullscreenActivity.setView && FullscreenActivity.swipeSet.equals("Y")
                            && !FullscreenActivity.whichSongFolder.equals("../Variations")
                            && !FullscreenActivity.whichSongFolder.equals("../Scripture/_cache")
                            && !FullscreenActivity.whichSongFolder.equals("../Images/_cache")
                            && !FullscreenActivity.whichSongFolder.equals("../Slides/_cache")
                            && !FullscreenActivity.whichSongFolder.equals("../Notes/_cache")) {
                        if (FullscreenActivity.nextSongIndex < FullscreenActivity.mSongFileNames.length
                                && FullscreenActivity.nextSongIndex != -1
                                && !FullscreenActivity.songfilename.equals(FullscreenActivity.mSongFileNames[FullscreenActivity.nextSongIndex])) {
                            // Move to the next song
                            // temporarily disable swipe
                            FullscreenActivity.tempswipeSet = "disable";

                            FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[FullscreenActivity.nextSongIndex];
                            loadSong();

                            // Set a runnable to reset swipe back to original value after 1 second
                            Handler delayfadeinredraw = new Handler();
                            delayfadeinredraw.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    FullscreenActivity.tempswipeSet = "enable";
                                }
                            }, FullscreenActivity.delayswipe_time);
                        }
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
                    // Set the direction as left to right
                    FullscreenActivity.whichDirection = "L2R";

                    if (FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0) {
                        FullscreenActivity.pdfPageCurrent = FullscreenActivity.pdfPageCurrent - 1;
                        //redrawTheLyricsTable(main_page);
                        return false;
                    } else {
                        FullscreenActivity.pdfPageCurrent = 0;
                    }

                    // If we are viewing a set, move to the previous song.
                    if (FullscreenActivity.setSize > 2 && FullscreenActivity.setView && FullscreenActivity.indexSongInSet >= 1) {
                        // temporarily disable swipe
                        FullscreenActivity.tempswipeSet = "disable";

                        FullscreenActivity.indexSongInSet -= 1;
                        doMoveInSet();
                        // Set a runnable to reset swipe back to original value after 1 second
                        Handler delayfadeinredraw = new Handler();
                        delayfadeinredraw.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FullscreenActivity.tempswipeSet = "enable";
                            }
                        }, FullscreenActivity.delayswipe_time);
                    }
                    // If not in a set, see if we can move to the previous song in the list
                    // Only if swipeSet is Y (not S)
                    if (!FullscreenActivity.setView && FullscreenActivity.swipeSet.equals("Y")
                            && !FullscreenActivity.whichSongFolder.equals("../Variations")
                            && !FullscreenActivity.whichSongFolder.equals("../Scripture/_cache")
                            && !FullscreenActivity.whichSongFolder.equals("../Images/_cache")
                            && !FullscreenActivity.whichSongFolder.equals("../Slides/_cache")
                            && !FullscreenActivity.whichSongFolder.equals("../Notes/_cache")) {

                        if (FullscreenActivity.previousSongIndex < FullscreenActivity.mSongFileNames.length
                                && FullscreenActivity.previousSongIndex != -1
                                && !FullscreenActivity.songfilename.equals(FullscreenActivity.mSongFileNames[FullscreenActivity.previousSongIndex])) {
                            // temporarily disable swipe
                            FullscreenActivity.tempswipeSet = "disable";

                            // Move to the previous song
                            FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[FullscreenActivity.previousSongIndex];
                            loadSong();

                            // Set a runnable to reset swipe back to original value
                            // after 1 second
                            Handler delayfadeinredraw = new Handler();
                            delayfadeinredraw.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    FullscreenActivity.tempswipeSet = "enable";
                                }
                            }, FullscreenActivity.delayswipe_time);
                        }
                    }
                    return true;
                }

            return false;
        }
    }
    public void gesture1() {
        openMyDrawers("song");
        FullscreenActivity.wasscrolling = false;
        if (delayactionBarHide != null && hideActionBarRunnable != null) {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        }
    }
    public void gesture2() {
        // Edit the song
        FullscreenActivity.whattodo = "editsong";
        openFragment();
    }
    public void gesture3() {
        // Add to set
        if (FullscreenActivity.isSong) {
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.songfilename + "_**$";
            } else {
                FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/"
                        + FullscreenActivity.songfilename + "_**$";
            }

            // Allow the song to be added, even if it is already there
            FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
            // Tell the user that the song has been added.
            FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.songfilename + "\" "
                    + getResources().getString(R.string.addedtoset);
            // Vibrate to let the user know something happened
            DoVibrate.vibrate(StageMode.this,200);
            ShowToast.showToast(StageMode.this);

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
    }
    public void gesture4() {
        // Redraw the lyrics page
        loadSong();
    }
    public void gesture5() {
        // Stop or start autoscroll
        DoVibrate.vibrate(StageMode.this,200);
        if (FullscreenActivity.isautoscrolling) {
            stopAutoScroll();
        } else {
            startAutoScroll();
        }
    }
    public void gesture6() {
        // Stop or start pad
        PadFunctions.getPad1Status();
        PadFunctions.getPad2Status();
        DoVibrate.vibrate(StageMode.this,200);
        if (FullscreenActivity.pad1Playing || FullscreenActivity.pad2Playing) {
            if (FullscreenActivity.pad1Playing) {
                FullscreenActivity.mPlayer1.stop();
                FullscreenActivity.mPlayer1.reset();
            } else if (FullscreenActivity.pad2Playing) {
                FullscreenActivity.mPlayer2.stop();
                FullscreenActivity.mPlayer2.reset();
            }
            FullscreenActivity.padson = false;
        } else if (PadFunctions.isPadValid(StageMode.this)){
            preparePad();
        } else {
            FullscreenActivity.whattodo = "page_pad";
            openFragment();
        }
    }
    public void gesture7() {
        // Start or stop metronome
        // Vibrate to let the user know something happened
        DoVibrate.vibrate(StageMode.this,200);
        PopUpMetronomeFragment.startstopMetronome(StageMode.this);
    }

}