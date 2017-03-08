package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DialogFragment;
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
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)

public class PresenterMode extends AppCompatActivity implements PopUpEditSongFragment.MyInterface,
        PopUpListSetsFragment.MyInterface, PopUpAreYouSureFragment.MyInterface,
        PopUpSongRenameFragment.MyInterface, PopUpSearchViewFragment.MyInterface,
        PopUpSetViewNew.MyInterface, PopUpSongCreateFragment.MyInterface,
        PopUpSongDetailsFragment.MyInterface,
        PopUpFontsFragment.MyInterface, PopUpCustomSlideFragment.MyInterface,
        PopUpFileChooseFragment.MyInterface, PopUpPresentationOrderFragment.MyInterface {

    DialogFragment newFragment;

    // The toolbar
    public Toolbar toolbar;
    public ActionBar ab;
    public TextView songandauthor;

    // General variables
    public static MediaPlayer mp;
    public static String mpTitle = "";
    static int numdisplays;
    boolean firsttime = true;
    private boolean isPDF;
    private boolean isSong;
    Menu menu;
    private boolean addingtoset;
    private boolean endofset = false;
    AlertDialog.Builder popupAlert;
    AlertDialog alert;
    /* LinearLayout popupLayout; */
    static String myAlert = FullscreenActivity.myAlert;
    static boolean autoscale = FullscreenActivity.presoAutoScale;
    static int whichsonginset;
    static int whichsongsection;
    static boolean pedalsenabled = true;

    String[] imagelocs;
    boolean isImage = false;
    boolean isSlide = false;
    static String imageAddress;

    // Variables used by the popups
    static String whatBackgroundLoaded;

    // Which Actions buttons are selected
    boolean projectButton_isSelected = false;
    boolean logoButton_isSelected = false;
    boolean blankButton_isSelected = false;
    boolean displayButton_isSelected = false;
    boolean scriptureButton_isSelected = false;
    boolean slideButton_isSelected = false;
    boolean alertButton_isSelected = false;
    boolean audioButton_isSelected = false;
    boolean dBButton_isSelected = false;
    boolean layoutButton_isSelected = false;
    boolean backgroundButton_isSelected = false;

    // Keep a note of what is shown
    //static String background="image";
    static String song_on = "N";
    static String logo_on = "Y";
    static String blackout = "N";
    static String alert_on = "N";

    // The left and right menus
    DrawerLayout mDrawerLayout;
    ExpandableListView expListViewSong;
    ExpandableListView expListViewOption;
    private ArrayList<String> listDataHeaderSong;
    private HashMap<String, List<String>> listDataChildSong = new HashMap<>();
    private ArrayList<String> listDataHeaderOption;
    private HashMap<String, List<String>> listDataChildOption = new HashMap<>();
    protected int lastExpandedGroupPositionOption;
    protected boolean removingfromset;
    protected int myOptionListClickedItem;
    protected View view;
    protected String linkclicked;

    // Song values
    // Lyrics are previewed in the EditText box before being presented
    public static String buttonPresentText;
    public static String presoAuthor;
    public static String presoTitle;
    public static String presoCopyright;

    // Display variables
    Display[] presentationDisplays;
    DisplayManager displayManager;
    MediaRouter mMediaRouter;
    //Display display;
    Context context;
    static int tempxmargin;
    static int tempymargin;

    // Song details
    TextView presenter_song_title;
    TextView presenter_song_author;
    TextView presenter_song_copyright;

    // Presentation order
    CheckBox presenter_order_view;

    // Set buttons
    LinearLayout presenter_set_title;
    ImageView setlisticon_ImageView;
    ScrollView presenter_set_buttons;
    LinearLayout presenter_set_buttonsListView;
    static Button newSetButton;
    String tempSongLocation;
    //int tempSetButtonId;
    String[] songpart = new String[2];
    View currentsetbutton;
    int whichviewSetSection;

    // Slide
    EditText presenter_slide_text;
    ImageView presenter_slide_image;

    // Song buttons
    ScrollView presenter_song_buttons;
    LinearLayout presenter_song_buttonsListView;
    LinearLayout newSongSectionGroup;
    TextView newSongSectionText;
    static String[] songSections;
    static String[] songSectionsLabels;
    static Button newSongButton;
    View currentsectionbutton;
    int whichviewSongSection;
    int numsectionbuttons;

    // Action buttons
    ScrollView presenter_actions_buttons;
    LinearLayout presenter_project_group;
    LinearLayout presenter_logo_group;
    LinearLayout presenter_blank_group;
    LinearLayout presenter_scripture_group;
    LinearLayout presenter_alert_group;
    LinearLayout presenter_audio_group;
    LinearLayout presenter_dB_group;
    LinearLayout presenter_slide_group;

    // Auto slideshow
    static boolean isplayingautoslideshow = false;
    LinearLayout loopandtimeLinearLayout;
    LinearLayout loopcontrolsLinearLayout;
    EditText timeEditText;
    CheckBox loopCheckBox;
    ImageButton stopSlideShow;
    ImageButton playSlideShow;
    static int autoslidetime = 0;
    static boolean autoslideloop = false;
    AsyncTask autoslideshowtask;
    AsyncTask doredraw;
    AsyncTask loadsong_async;
    //Handler mHandler;

    // Settings buttons
    ScrollView presenter_settings_buttons;
    LinearLayout presenter_backgrounds_group;
    LinearLayout presenter_layout_group;
    LinearLayout presenter_displays_group;

    View mLayout;
    private int requestMicrophone = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Check storage is valid
        if (ActivityCompat.checkSelfPermission(PresenterMode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            finish();
        }

        System.gc();

        mp = new MediaPlayer();

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Load up the user preferences
        //myPreferences = getPreferences(MODE_PRIVATE);
        Preferences.loadPreferences();

        // For now, turn off presenterchords
        FullscreenActivity.presenterChords = "N";
        // Try language locale change
        if (!FullscreenActivity.languageToLoad.isEmpty()) {
            Locale locale;
            locale = new Locale(FullscreenActivity.languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        // Load the songs

/*        ListSongFiles.listSongs();
        // Get the song indexes
        ListSongFiles.getCurrentSongIndex();

        // Load the current song and Prepare it
        isPDF = false;
        isSong = true;
        File checkfile;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            checkfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
        } else {
            checkfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename);
        }
        if ((FullscreenActivity.songfilename.contains(".pdf") || FullscreenActivity.songfilename.contains(".PDF")) && checkfile.exists()) {
            // File is pdf
            isPDF = true;
            isSong = false;
        }

        if (FullscreenActivity.whichSongFolder.contains("../Scripture") || FullscreenActivity.whichSongFolder.contains("../Images") || FullscreenActivity.whichSongFolder.contains("../Slides") || FullscreenActivity.whichSongFolder.contains("../Notes")) {
            isSong = false;
        }

        if (!isPDF) {
            try {
                LoadXML.loadXML();
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            if (FullscreenActivity.mLyrics == null) {
                FullscreenActivity.mLyrics = "";
            }

            FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
            LyricsDisplay.parseLyrics();


        } else {
            FullscreenActivity.mLyrics = getResources().getString(R.string.pdf_functionnotavailable);
            FullscreenActivity.mTitle = FullscreenActivity.songfilename;
            FullscreenActivity.mAuthor = "";
            //Preferences.savePreferences(); - Not sure why this was here?
        }

        if (!isPDF) {
            PresentPrepareSong.splitSongIntoSections();
        }*/

        // Load the layout and set the title
        setContentView(R.layout.presentermode);

        mLayout = findViewById(R.id.pagepresentermode);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();

        TextView title = (TextView) findViewById(R.id.songtitle_ab);
        if (ab != null && title != null) {
            ab.setTitle("");
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
            title.setText(getResources().getString(R.string.presentermode));
        }

        // Initialise the popupAlert
        popupAlert = new AlertDialog.Builder(this);
        alert = popupAlert.create();

        // Now identify the TextViews, ScrollViews, LinearLayouts, etc.
        // Song details
        presenter_song_title = (TextView) findViewById(R.id.presenter_songtitle);
        presenter_song_author = (TextView) findViewById(R.id.presenter_author);
        presenter_song_copyright = (TextView) findViewById(R.id.presenter_copyright);
        presenter_song_title.isFocusable();
        presenter_song_title.requestFocus();
        // Presentation order
        presenter_order_view = (CheckBox) findViewById(R.id.presenter_order_text);
        presenter_order_view.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FullscreenActivity.usePresentationOrder = presenter_order_view.isChecked();
                Preferences.savePreferences();
                /*LyricsDisplay.parseLyrics();
                // Only do this if this isn't a scripture - as it starts with numbers!
                Log.d("d","PresenterMode 357");
                Log.d("d","FullscreenActivity.whichSongFolder="+FullscreenActivity.whichSongFolder);
                PresentPrepareSong.splitSongIntoSections("presenter");
                setupSongButtons();*/
                refreshAll();
            }
        });

        // Set buttons
        presenter_set_title = (LinearLayout) findViewById(R.id.presenter_set_title);
        presenter_set_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit current set
                mDrawerLayout.closeDrawer(expListViewOption);
                FullscreenActivity.whattodo = "editset";
                newFragment = PopUpSetViewNew.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
            }
        });
        setlisticon_ImageView = (ImageView) findViewById(R.id.setlisticon_ImageView);
        presenter_set_buttons = (ScrollView) findViewById(R.id.presenter_setbuttons);
        presenter_set_buttonsListView = (LinearLayout) findViewById(R.id.presenter_set_buttonsListView);
        presenter_set_buttons.setScrollbarFadingEnabled(false);

        // Slide
        presenter_slide_text = (EditText) findViewById(R.id.presenter_lyrics);
        presenter_slide_image = (ImageView) findViewById(R.id.presenter_lyrics_image);
        if (isPDF) {
            presenter_slide_text.setVisibility(View.GONE);
            presenter_slide_image.setVisibility(View.VISIBLE);
        } else {
            presenter_slide_text.setVisibility(View.VISIBLE);
            presenter_slide_image.setVisibility(View.GONE);
        }

        // Song buttons
        presenter_song_buttons = (ScrollView) findViewById(R.id.presenter_songbuttons);
        presenter_song_buttonsListView = (LinearLayout) findViewById(R.id.presenter_song_buttonsListView);
        presenter_song_buttons.setScrollbarFadingEnabled(false);

        // Action buttons
        presenter_actions_buttons = (ScrollView) findViewById(R.id.preso_action_buttons_scroll);
        presenter_actions_buttons.setScrollbarFadingEnabled(false);
        presenter_project_group = (LinearLayout) findViewById(R.id.presenter_project_group);
        presenter_logo_group = (LinearLayout) findViewById(R.id.presenter_logo_group);
        presenter_blank_group = (LinearLayout) findViewById(R.id.presenter_blank_group);
        presenter_scripture_group = (LinearLayout) findViewById(R.id.presenter_scripture_group);
        presenter_slide_group = (LinearLayout) findViewById(R.id.presenter_slide_group);
        presenter_alert_group = (LinearLayout) findViewById(R.id.presenter_alert_group);
        presenter_audio_group = (LinearLayout) findViewById(R.id.presenter_audio_group);
        presenter_dB_group = (LinearLayout) findViewById(R.id.presenter_dB_group);

        // Settings buttons
        presenter_settings_buttons = (ScrollView) findViewById(R.id.preso_settings_scroll);
        presenter_settings_buttons.setScrollbarFadingEnabled(false);
        presenter_backgrounds_group = (LinearLayout) findViewById(R.id.presenter_backgrounds_group);
        presenter_layout_group = (LinearLayout) findViewById(R.id.presenter_layout_group);
        presenter_displays_group = (LinearLayout) findViewById(R.id.presenter_display_group);

        // Auto slideshow stuff
        loopandtimeLinearLayout = (LinearLayout) findViewById(R.id.loopandtimeLinearLayout);
        loopcontrolsLinearLayout = (LinearLayout) findViewById(R.id.loopcontrolsLinearLayout);
        timeEditText = (EditText) findViewById(R.id.timeEditText);
        loopCheckBox = (CheckBox) findViewById(R.id.loopCheckBox);
        stopSlideShow = (ImageButton) findViewById(R.id.stopSlideShow);
        playSlideShow = (ImageButton) findViewById(R.id.playSlideShow);
        stopSlideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareStopAutoSlideShow();
            }
        });
        playSlideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareStartAutoSlideShow();
            }
        });

        // Set up the navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expListViewSong = (ExpandableListView) findViewById(R.id.song_list_ex);
        expListViewOption = (ExpandableListView) findViewById(R.id.option_list_ex);

        prepareSongMenu();
        prepareOptionMenu();

/*
        if (!isPDF) {
            PresentPrepareSong.splitSongIntoSections();
        }
*/
        //setupSongButtons();
        setupSetButtons();

        invalidateOptionsMenu();

        // Turn on the secondary display if possible
        updateDisplays();

        if (numdisplays > 0) {
            logoButton_isSelected = true;
            presenter_logo_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
        } else {
            logo_on = "N";
        }

        resizeDrawers();

        doredraw = new DoRedraw();
        doredraw.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void windowFlags() {

    }

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

    public void prepareSongMenu() {
        // Initialise Songs menu
        listDataHeaderSong = new ArrayList<>();

        // Get song folders
        ListSongFiles.listSongFolders();
        listDataHeaderSong.add(getResources().getString(R.string.mainfoldername));
        listDataHeaderSong.addAll(Arrays.asList(FullscreenActivity.mSongFolderNames).subList(0, FullscreenActivity.mSongFolderNames.length - 1));

        for (int s = 0; s < FullscreenActivity.mSongFolderNames.length; s++) {
            List<String> song_folders = new ArrayList<>();
            Collections.addAll(song_folders, FullscreenActivity.childSongs[s]);
            listDataChildSong.put(listDataHeaderSong.get(s), song_folders);
        }

        ExpandableListAdapter listAdapterSong = new ExpandableListAdapter(expListViewSong, this, listDataHeaderSong, listDataChildSong);

        expListViewSong.setAdapter(listAdapterSong);

        // Listen for song folders being opened/expanded
        expListViewSong.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            private int lastExpandedGroupPositionSong;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != lastExpandedGroupPositionSong) {
                    expListViewSong.collapseGroup(lastExpandedGroupPositionSong);
                }
                lastExpandedGroupPositionSong = groupPosition;
            }
        });

        // Listen for long clicks in the song menu (songs only, not folders) - ADD TO SET!!!!
        expListViewSong.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                addingtoset = true;
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    // Vibrate to indicate something has happened
                    Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vb.vibrate(25);

                    FullscreenActivity.songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

                    if (listDataHeaderSong.get(groupPosition).equals(FullscreenActivity.mainfoldername)) {
                        FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
                        FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.songfilename + "_**$";
                    } else {
                        FullscreenActivity.whichSongFolder = listDataHeaderSong.get(groupPosition);
                        FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename + "_**$";
                    }
                    // Set the appropriate song filename
                    FullscreenActivity.songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

                    // Allow the song to be added, even if it is already there
                    FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;

                    // Tell the user that the song has been added.
                    FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.songfilename + "\" " + getResources().getString(R.string.addedtoset);
                    ShowToast.showToast(PresenterMode.this);

                    // Save the set and other preferences
                    Preferences.savePreferences();

                    // Show the current set
                    invalidateOptionsMenu();
                    SetActions.prepareSetList();
                    setupSetButtons();
                    prepareOptionMenu();
                    //mDrawerLayout.openDrawer(expListViewOption);
                    setlisticon_ImageView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse));
                    mDrawerLayout.closeDrawer(expListViewSong);
                    expListViewOption.expandGroup(0);

                    // Hide the menus - 1 second after opening the Option menu,
                    // close it (1000ms total)
                    Handler optionMenuFlickClosed = new Handler();
                    optionMenuFlickClosed.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.closeDrawer(expListViewOption);
                            addingtoset = false;
                        }
                    }, 1000); // 1000ms delay
                }
                return false;
            }
        });

        // Listen for short clicks in the song menu (songs only, not folders) - OPEN SONG!!!!
        expListViewSong.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                if (!addingtoset) {
                    // Set the appropriate folder name

                    if (listDataHeaderSong.get(groupPosition).equals(FullscreenActivity.mainfoldername)) {
                        FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
                    } else {
                        FullscreenActivity.whichSongFolder = listDataHeaderSong.get(groupPosition);
                    }
                    // Set the appropriate song filename
                    FullscreenActivity.songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

                    if (FullscreenActivity.setView && FullscreenActivity.setSize > 0) {
                        // Get the name of the song to look for (including folders if need be)
                        SetActions.getSongForSetWork();

                        if (FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                            // Song is in current set.  Find the song position in the current set and load it (and next/prev)
                            // The first song has an index of 6 (the 7th item as the rest are menu items)

                            FullscreenActivity.previousSongInSet = "";
                            FullscreenActivity.nextSongInSet = "";
                            SetActions.prepareSetList();
                            setupSetButtons();

                        } else {
                            // Song isn't in the set, so just show the song
                            // Switch off the set view (buttons in action bar)
                            FullscreenActivity.setView = false;
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

                        }
                    } else {
                        // User wasn't in set view, or the set was empty
                        // Switch off the set view (buttons in action bar)
                        FullscreenActivity.setView = false;
                    }

                    // Now save the preferences
                    Preferences.savePreferences();

                    invalidateOptionsMenu();

                    // Close both drawers
                    mDrawerLayout.closeDrawer(expListViewSong);
                    mDrawerLayout.closeDrawer(expListViewOption);

                    // Click the first song section
                    whichsongsection = 0;
                    selectSectionButtonInSong();

                    // Redraw the Lyrics View
                    redrawPresenterPage();

                } else {
                    addingtoset = false;
                }
                return false;
            }

        });
    }

    public void prepareOptionMenu() {
        // preparing list data
        listDataHeaderOption = new ArrayList<>();

        // Adding headers for option menu data
        listDataHeaderOption.add(getResources().getString(R.string.options_set));
        listDataHeaderOption.add(getResources().getString(R.string.options_song));
        listDataHeaderOption.add(getResources().getString(R.string.options_options));

        // Adding child data
        List<String> options_set = new ArrayList<>();
        options_set.add(getResources().getString(R.string.options_set_load));
        options_set.add(getResources().getString(R.string.options_set_save));
        options_set.add(getResources().getString(R.string.options_set_clear));
        options_set.add(getResources().getString(R.string.options_set_delete));
        options_set.add(getResources().getString(R.string.options_set_export));
        options_set.add(getResources().getString(R.string.add_custom_slide));
        options_set.add(getResources().getString(R.string.options_set_edit));
        options_set.add(getResources().getString(R.string.customise_set_item));

        // Parse the saved set
        FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$$**_", "_**$%%%$**_");
        // Break the saved set up into a new String[]
        FullscreenActivity.mSetList = FullscreenActivity.mySet.split("%%%");
        // Restore the set back to what it was
        FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$%%%$**_", "_**$$**_");
        FullscreenActivity.setSize = FullscreenActivity.mSetList.length;
        invalidateOptionsMenu();

        for (int r = 0; r < FullscreenActivity.mSetList.length; r++) {
            FullscreenActivity.mSetList[r] = FullscreenActivity.mSetList[r].replace("$**_", "");
            FullscreenActivity.mSetList[r] = FullscreenActivity.mSetList[r].replace("_**$", "");
/*
            if (!FullscreenActivity.mSetList[r].isEmpty()) {

                options_set.add(FullscreenActivity.mSetList[r]);
            }
*/
        }

        List<String> options_song = new ArrayList<>();
        options_song.add(getResources().getString(R.string.options_song_edit));
        options_song.add(getResources().getString(R.string.options_song_rename));
        options_song.add(getResources().getString(R.string.options_song_delete));
        options_song.add(getResources().getString(R.string.options_song_new));
        options_song.add(getResources().getString(R.string.options_song_export));

        List<String> options_options = new ArrayList<>();
        options_options.add(getResources().getString(R.string.options_options_menuswipe));
        options_options.add(getResources().getString(R.string.options_options_fonts));
        options_options.add(getResources().getString(R.string.options_options_pedal));
        options_options.add(getResources().getString(R.string.options_options_help));
        options_options.add(getResources().getString(R.string.options_options_start));

        listDataChildOption.put(listDataHeaderOption.get(0), options_set); // Header, Child data
        listDataChildOption.put(listDataHeaderOption.get(1), options_song);
        listDataChildOption.put(listDataHeaderOption.get(2), options_options);

        ExpandableListAdapter listAdapterOption = new ExpandableListAdapter(expListViewOption, this, listDataHeaderOption, listDataChildOption);

        // setting list adapter
        expListViewOption.setAdapter(listAdapterOption);
        expListViewOption.setFastScrollEnabled(false);

        // Listen for options menus being expanded (close the others and keep a note that this one is open)
        expListViewOption.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != lastExpandedGroupPositionOption) {
                    expListViewOption.collapseGroup(lastExpandedGroupPositionOption);
                }
                lastExpandedGroupPositionOption = groupPosition;
            }
        });

/*
        // Listen for long clicks on songs in current set to remove them
        expListViewOption.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removingfromset = true;
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    myOptionListClickedItem = position;
                    if (myOptionListClickedItem > 8 && groupPosition == 0) {
                        // Long clicking on the 8th or later options will remove the
                        // song from the set
                        // Remove this song from the set. Remember it has tags at the start and end
                        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vb.vibrate(25);

                        // Take away the menu items (8)
                        String tempSong = FullscreenActivity.mSetList[childPosition - 8];
                        FullscreenActivity.mSetList[childPosition - 8] = "";

                        FullscreenActivity.mySet = "";
                        for (int w = 0; w < FullscreenActivity.mSetList.length; w++) {
                            if (!FullscreenActivity.mSetList[w].isEmpty()) {
                                FullscreenActivity.mySet = FullscreenActivity.mySet + "$**_" + FullscreenActivity.mSetList[w] + "_**$";
                            }
                        }

                        // Save set
                        SetActions.prepareSetList();

                        SetActions.indexSongInSet();
                        Preferences.savePreferences();

                        // Reload the set menu
                        invalidateOptionsMenu();
                        setupSetButtons();
                        prepareOptionMenu();
                        expListViewOption.expandGroup(0);

                        // Tell the user that the song has been removed.
                        FullscreenActivity.myToastMessage = "\"" + tempSong + "\" "
                                + getResources().getString(R.string.removedfromset);
                        ShowToast.showToast(PresenterMode.this);

                        // Close the drawers again so accidents don't happen!
                        mDrawerLayout.closeDrawer(expListViewSong);
                        mDrawerLayout.closeDrawer(expListViewOption);
                    }
                }
                removingfromset = false;
                return false;
            }
        });
*/

        // Listview on child click listener
        expListViewOption.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (!removingfromset) {
                    // Make sure the song menu is closed
                    mDrawerLayout.closeDrawer(expListViewSong);

                    String chosenMenu = listDataHeaderOption.get(groupPosition);

                    if (chosenMenu.equals(getResources().getString(R.string.options_set))) {

                        // Close the menu for now
                        mDrawerLayout.closeDrawer(expListViewOption);

                        // Load up a list of saved sets as it will likely be needed
                        SetActions.updateOptionListSets();
                        Arrays.sort(FullscreenActivity.mySetsFiles);
                        Arrays.sort(FullscreenActivity.mySetsDirectories);
                        Arrays.sort(FullscreenActivity.mySetsFileNames);
                        Arrays.sort(FullscreenActivity.mySetsFolderNames);

                        // First up check for set options clicks
                        if (childPosition == 0) {
                            // Load a set
                            FullscreenActivity.whattodo = "loadset";
                            newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 1) {
                            // Save current set
                            FullscreenActivity.whattodo = "saveset";
                            newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 2) {
                            // Clear current set
                            FullscreenActivity.whattodo = "clearset";
                            String message = getResources().getString(R.string.options_clearthisset);
                            newFragment = PopUpAreYouSureFragment.newInstance(message);
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 3) {
                            // Delete saved set
                            FullscreenActivity.whattodo = "deleteset";
                            newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 4) {
                            // Export current set
                            FullscreenActivity.whattodo = "exportset";
                            newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 5) {
                            // Add a custom slide
                            newFragment = PopUpCustomSlideFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 6) {
                            // Edit current set
                            FullscreenActivity.whattodo = "editset";
                            newFragment = PopUpSetViewNew.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 7) {
                            // Create a set item variation
                            mDrawerLayout.closeDrawer(expListViewOption);
                            FullscreenActivity.whattodo = "setitemvariation";
                            newFragment = PopUpSetViewNew.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        }
/*
                        } else {
                            // Load song in set
                            FullscreenActivity.setView = "Y";
                            // Set item is 8 less than childPosition
                            FullscreenActivity.indexSongInSet = childPosition - 8;
                            if (FullscreenActivity.indexSongInSet == 0) {
                                // Already first item
                                FullscreenActivity.previousSongInSet = "";
                            } else {
                                FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet - 1];
                            }

                            if (FullscreenActivity.indexSongInSet == (FullscreenActivity.setSize - 1)) {
                                // Last item
                                FullscreenActivity.nextSongInSet = "";
                            } else {
                                FullscreenActivity.nextSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet + 1];
                            }

                            // Specify which songinset button
                            whichsonginset = FullscreenActivity.indexSongInSet;
                            whichsongsection = 0;

                            // Select it
                            Button which_song_to_click = (Button) presenter_set_buttonsListView.findViewById(whichsonginset);
                            which_song_to_click.performClick();

                        }
*/

                    } else if (chosenMenu.equals(getResources().getString(R.string.options_song))) {
                        linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Close the drawer
                        mDrawerLayout.closeDrawer(expListViewOption);

                        // Now check for song options clicks
                        // Only allow 0=edit, 1=rename, 2=delete, 3=new, 4=export
                        if (childPosition == 0) {
                            // Edit
                            if (isPDF) {
                                // Can't do this action on a pdf!
                                FullscreenActivity.myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
                                ShowToast.showToast(PresenterMode.this);
                            } else if (!isSong) {
                                // Can't do this action on a scripture/slide/note!
                                FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(PresenterMode.this);
                            } else {
                                FullscreenActivity.whattodo = "editsong";
                                newFragment = PopUpEditSongFragment.newInstance();
                                newFragment.show(getFragmentManager(), "dialog");
                            }

                        } else if (childPosition == 1) {
                            // Rename
                            if (!isPDF && !isSong) {
                                // Can't do this action on a scripture/slide/note!
                                FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(PresenterMode.this);
                                FullscreenActivity.whattodo = "renamesong";
                            } else {
                                newFragment = PopUpSongRenameFragment.newInstance();
                                newFragment.show(getFragmentManager(), "dialog");
                            }

                        } else if (childPosition == 2) {
                            // Delete
                            // Give the user an are you sure prompt!
                            if (!isSong && !isPDF) {
                                // Can't do this action on a scripture/slide/note!
                                FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(PresenterMode.this);
                                FullscreenActivity.whattodo = "deletesong";
                            } else {
                                String message = getResources().getString(R.string.options_song_delete) +
                                        " \"" + FullscreenActivity.songfilename + "\"?";
                                newFragment = PopUpAreYouSureFragment.newInstance(message);
                                newFragment.show(getFragmentManager(), "dialog");
                            }

                        } else if (childPosition == 3) {
                            // New
                            FullscreenActivity.whattodo = "createsong";
                            newFragment = PopUpSongCreateFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 4) {
                            // Export
                            // The current song is the songfile
                            // Believe it or not, it works!!!!!
                            if (!isSong && !isPDF) {
                                // Can't do this action on a scripture/slide/note!
                                FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(PresenterMode.this);
                            } else {
                                // Run the script that generates the email text which has the set details in it.
                                try {
                                    ExportPreparer.songParser();
                                } catch (IOException | XmlPullParserException e) {
                                    e.printStackTrace();
                                }

                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                emailIntent.setType("text/plain");
                                emailIntent.putExtra(Intent.EXTRA_TITLE, FullscreenActivity.songfilename);
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, FullscreenActivity.songfilename);
                                emailIntent.putExtra(Intent.EXTRA_TEXT, FullscreenActivity.emailtext);
                                FullscreenActivity.emailtext = "";
                                File file;
                                if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                                    file = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
                                } else {
                                    file = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename);
                                }
                                Uri uri = Uri.fromFile(file);
                                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                startActivity(Intent.createChooser(emailIntent, FullscreenActivity.exportcurrentsong));
                            }
                        }

                    } else if (chosenMenu.equals(getResources().getString(R.string.options_options))) {

                        // Close the menu for now
                        mDrawerLayout.closeDrawer(expListViewOption);

                        // Now check for option options clicks
                        if (childPosition == 0) {
                            // Toggle menu swipe on/off
                            if (FullscreenActivity.swipeDrawer.equals("Y")) {
                                FullscreenActivity.swipeDrawer = "N";
                                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                                FullscreenActivity.myToastMessage = getResources().getString(
                                        R.string.drawerswipe)
                                        + " " + getResources().getString(R.string.off);
                                ShowToast.showToast(PresenterMode.this);
                            } else {
                                FullscreenActivity.swipeDrawer = "Y";
                                mDrawerLayout
                                        .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                FullscreenActivity.myToastMessage = getResources().getString(
                                        R.string.drawerswipe)
                                        + " " + getResources().getString(R.string.on);
                                ShowToast.showToast(PresenterMode.this);
                            }
                            Preferences.savePreferences();


                        } else if (childPosition == 1) {
                            // Change fonts
                            FullscreenActivity.whattodo = "changefonts";
                            newFragment = PopUpFontsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");


                        } else if (childPosition == 2) {
                            // Assign foot pedal
                            FullscreenActivity.whattodo = "footpedal";
                            newFragment = PopUpPedalsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");


                        } else if (childPosition == 3) {
                            // Help (online)
                            String url = "https://sites.google.com/site/opensongtabletmusicviewer/home";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);

                        } else if (childPosition == 4) {
                            // Splash screen
                            // First though, set the preference to show the current version
                            // Otherwise it won't show the splash screen
                            SharedPreferences settings = getSharedPreferences("mysettings",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt("showSplashVersion", 0);
                            editor.apply();
                            Intent intent = new Intent();
                            intent.setClass(PresenterMode.this, SettingsActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
                return false;
            }
        });

        findSongInFolder();
    }

    public void resizeDrawers() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels/2;
        DrawerLayout.LayoutParams paramsSong = (android.support.v4.widget.DrawerLayout.LayoutParams) expListViewSong.getLayoutParams();
        DrawerLayout.LayoutParams paramsOption = (android.support.v4.widget.DrawerLayout.LayoutParams) expListViewOption.getLayoutParams();
        paramsSong.width = width;
        paramsOption.width = width;
        expListViewSong.setLayoutParams(paramsSong);
        expListViewOption.setLayoutParams(paramsOption);
    }

    public void updateDisplays() {
        // This is called when display devices are changed (connected, disconnected, etc.)
        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);

        // Get the media router service.
        mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);

        numdisplays = presentationDisplays.length;
        if (numdisplays == 0) {
            if (firsttime) {
                firsttime = false;
                FullscreenActivity.myToastMessage = getResources().getText(R.string.nodisplays).toString();
                ShowToast.showToast(PresenterMode.this);
            }
            //Disable buttons that may throw an error....
            // projectButton.setClickable(false);

        } else {
            if (firsttime) {
                firsttime = false;
                FullscreenActivity.myToastMessage = getResources().getText(R.string.extradisplay).toString();
                ShowToast.showToast(PresenterMode.this);
            }
            //Activate present and logo button
            //projectButton.setClickable(true);

        }

        for (Display display : presentationDisplays) {
            MyPresentation mPresentation = new MyPresentation(this, display);
            mPresentation.show();
        }
    }

    public void setupSongButtons() {
        // Create a new button for each songSection
        // If the 'song' is custom images, set them as the background
        presenter_song_buttonsListView.removeAllViews();
        presenter_song_title.setText(FullscreenActivity.mTitle);
        presenter_song_author.setText(FullscreenActivity.mAuthor);
        presenter_song_copyright.setText(FullscreenActivity.mCopyright);
        if (FullscreenActivity.mPresentation.isEmpty() || FullscreenActivity.mPresentation.equals("")) {
            presenter_order_view.setText(getResources().getString(R.string.error_notset));
        } else {
            presenter_order_view.setText(FullscreenActivity.mPresentation);
        }

        // Need to decide if checkbox is on or off
        if (FullscreenActivity.usePresentationOrder) {
            presenter_order_view.setChecked(true);
        } else {
            presenter_order_view.setChecked(false);
        }

        imagelocs = null;
        isImage = false;
        isSlide = false;

        if (FullscreenActivity.whichSongFolder.contains("../Images")) {
            // Custom images so split the mUser3 field by newline.  Each value is image location
            imagelocs = FullscreenActivity.mUser3.split("\n");
            isImage = true;
        }

        if (FullscreenActivity.whichSongFolder.contains("../Slides")) {
            // Custom slide
            isSlide = true;
        }

        numsectionbuttons = songSections.length;

        for (int x = 0; x < songSections.length; x++) {
            String buttonText = songSections[x];
            String sectionText = songSectionsLabels[x];
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
            if (isImage) {
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

            if (isImage || isSlide) {
                // Make sure the time, loop and autoslideshow buttons are visible
                loopandtimeLinearLayout.setVisibility(View.VISIBLE);
                loopcontrolsLinearLayout.setVisibility(View.VISIBLE);
                // Set the appropiate values
                if (FullscreenActivity.mUser1!=null) {
                    timeEditText.setText(FullscreenActivity.mUser1);
                }
                if (FullscreenActivity.mUser2!=null && FullscreenActivity.mUser2.equals("true")) {
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
        selectSectionButtonInSong();
    }

    public void setupSetButtons() {
        // Create a new button for each song in the Set
        invalidateOptionsMenu();
        SetActions.prepareSetList();

        presenter_set_buttonsListView.removeAllViews();
        for (int x = 0; x < FullscreenActivity.mSet.length; x++) {
            if (!FullscreenActivity.mSet[x].isEmpty()) {
                String buttonText = FullscreenActivity.mSet[x];
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
                params.setMargins(5, 5, 5, 10);
                newSetButton.setLayoutParams(params);
                newSetButton.setOnClickListener(new setButtonClick());
                presenter_set_buttonsListView.addView(newSetButton);
            }
        }
        selectSongButtonInSet();
    }

    public void getSongLocation() {
        // Since song can be in sub folders, each should have a / in them
        // Songs in the main folder won't so add a / at the beginning.
        if (!tempSongLocation.contains("/")) {
            // Right it doesn't, so add the /
            tempSongLocation = "/" + tempSongLocation;
        }

        // Now split the linkclicked into two song parts 0=folder 1=file
        songpart = tempSongLocation.split("/");

        // If the folder length isn't 0, it is a folder
        // Decide if it is a song, scripture, slide, image or note and get the actual file location
        if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = songpart[0];

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Scripture/_cache";
            songpart[0] = "../Scripture/_cache";

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Slides/_cache";
            songpart[0] = "../Slides/_cache";

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Notes/_cache";
            songpart[0] = "../Notes/_cache";

        } else if (songpart[0].length() > 0 &&
                songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Images/_cache";
            songpart[0] = "../Images/_cache";

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Variations";
            songpart[0] = "../Variations";

        } else {
            FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
        }
    }

    public void doProject(View v) {
        if (numdisplays > 0) {
            buttonPresentText = presenter_slide_text.getText().toString();
            blackout = "N";
            logo_on = "N";
            song_on = "Y";
            MyPresentation.UpDatePresentation();
        }
    }

    public void redrawPresenterPage() {
        // Now load the appropriate song folder as an asynctask
        // Once this is done (onpostexecute) it loads the song asynchronously
        // Then it parses them
        // Then it splits into sections
        // Then sets up the song buttons
        // Then find the song in the folder
        doredraw = new DoRedraw();
        doredraw.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);




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
        */
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resizeDrawers();
    }

    @Override
    public void openSongEdit() {
        // This is called from the create a new song popup
        ListSongFiles.listSongFolders();
        redrawPresenterPage();

        FullscreenActivity.whattodo = "editsong";
        newFragment = PopUpEditSongFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void addSlideToSet() {
        String filename;
        String reusablefilename;
        String templocator;

        // Get rid of illegal characters
        String filetitle = FullscreenActivity.customslide_title.replaceAll("[|?*<\":>+\\[\\]']", " ");

        switch (FullscreenActivity.noteorslide) {
            case "note":
                filename = FullscreenActivity.dircustomnotes + "/" + filetitle;
                reusablefilename = FullscreenActivity.homedir + "/Notes/" + filetitle;
                templocator = FullscreenActivity.text_note;
                FullscreenActivity.customimage_list = "";
                break;
            case "slide":
                filename = FullscreenActivity.dircustomslides + "/" + filetitle;
                reusablefilename = FullscreenActivity.homedir + "/Slides/" + filetitle;
                templocator = FullscreenActivity.text_slide;
                FullscreenActivity.customimage_list = "";
                break;
            case "scripture":
                filename = FullscreenActivity.dirscriptureverses + "/" + filetitle;
                reusablefilename = FullscreenActivity.dirscripture + "/" + filetitle;
                templocator = FullscreenActivity.text_scripture;
                FullscreenActivity.customreusable = false;
                FullscreenActivity.customimage_list = "";
                break;
            default:
                filename = FullscreenActivity.dircustomimages + "/" + filetitle;
                reusablefilename = FullscreenActivity.homedir + "/Images/" + filetitle;
                templocator = FullscreenActivity.image;
                break;
        }

        // If slide content is empty - put the title in
        if (FullscreenActivity.customslide_content.isEmpty() && !FullscreenActivity.noteorslide.equals("image")) {
            FullscreenActivity.customslide_content = FullscreenActivity.customslide_title;
        }

        // Prepare the custom slide so it can be viewed in the app
        // When exporting/saving the set, the contents get grabbed from this
        FullscreenActivity.mynewXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        FullscreenActivity.mynewXML += "<song>\n";
        FullscreenActivity.mynewXML += "  <title>" + FullscreenActivity.customslide_title + "</title>\n";
        FullscreenActivity.mynewXML += "  <author></author>\n";
        FullscreenActivity.mynewXML += "  <user1>" + FullscreenActivity.customimage_time + "</user1>\n";  // This is used for auto advance time
        FullscreenActivity.mynewXML += "  <user2>" + FullscreenActivity.customimage_loop + "</user2>\n";  // This is used for loop on or off
        FullscreenActivity.mynewXML += "  <user3>" + FullscreenActivity.customimage_list + "</user3>\n";  // This is used as links to a background images
        FullscreenActivity.mynewXML += "  <aka></aka>\n";
        FullscreenActivity.mynewXML += "  <key_line></key_line>\n";
        FullscreenActivity.mynewXML += "  <hymn_number></hymn_number>\n";
        FullscreenActivity.mynewXML += "  <lyrics>" + FullscreenActivity.customslide_content +"</lyrics>\n";
        FullscreenActivity.mynewXML += "</song>";

        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;","&");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&","&amp;");

        // Now write the modified song
        FileOutputStream overWrite;
        try {
            overWrite = new FileOutputStream(filename, false);
            overWrite.write(FullscreenActivity.mynewXML.getBytes());
            overWrite.flush();
            overWrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If this is to be a reusable custom slide
        if (FullscreenActivity.customreusable) {
            // Now write the modified song
            FileOutputStream overWriteResuable;
            try {
                overWriteResuable = new FileOutputStream(reusablefilename, false);
                overWriteResuable.write(FullscreenActivity.mynewXML.getBytes());
                overWriteResuable.flush();
                overWriteResuable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FullscreenActivity.customreusable = false;
        }

        // Add to set
        FullscreenActivity.whatsongforsetwork = "$**_**" + templocator + "/" + filetitle + "_**$";

        // Allow the song to be added, even if it is already there
        FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;

        // Tell the user that the song has been added.
        FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.customslide_title + "\" " + getResources().getString(R.string.addedtoset);
        ShowToast.showToast(PresenterMode.this);

        // Save the set and other preferences
        Preferences.savePreferences();

        // Show the current set
        SetActions.prepareSetList();
        invalidateOptionsMenu();
        prepareOptionMenu();
        //mDrawerLayout.openDrawer(expListViewOption);
        setlisticon_ImageView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse));

        //expListViewOption.expandGroup(0);

        // Hide the menus - 1 second after opening the Option menu,
        // close it (1000ms total)
        Handler optionMenuFlickClosed = new Handler();
        optionMenuFlickClosed.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(expListViewOption);
                addingtoset = false;
            }
        }, 1000); // 1000ms delay

        refreshAll();
    }

    @Override
    public void loadCustomReusable() {
        // This is called from the file chooser fragment.
        // Load in the custom reusable, then reopen the custom slide editor
        // Put the old myXML and song fields into temporary memory while we load in the new ones
        LoadXML.prepareLoadCustomReusable(FullscreenActivity.customreusabletoload);
        // This reopens the choose backgrounds popupFragment
        newFragment = PopUpCustomSlideFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void updatePresentationOrder() {
        presenter_order_view.setText(FullscreenActivity.mPresentation);
        redrawPresenterPage();
    }

    public class sectionButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Re-enable the disabled button
            if (currentsectionbutton != null) {
                Button oldbutton = (Button) currentsectionbutton;
                if (isImage) {
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

            presenter_song_buttons.smoothScrollTo(0, totalheight);

            // If this is an image, hide the text, show the image, otherwise show the text in the slide window
            if (isImage) {
                presenter_slide_text.setVisibility(View.GONE);
                presenter_slide_image.setVisibility(View.VISIBLE);
                presenter_slide_image.setBackground(v.getBackground());
                presenter_slide_image.setMaxWidth(200);
                presenter_slide_image.setMaxHeight(150);
                LinearLayout.LayoutParams layoutSongButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutSongButton.width = 200;
                layoutSongButton.height = 150;
                presenter_slide_image.setLayoutParams(layoutSongButton);
                imageAddress = imagelocs[v.getId()];

            } else {
                presenter_slide_image.setVisibility(View.GONE);
                presenter_slide_text.setVisibility(View.VISIBLE);
                presenter_slide_text.setText(songSections[whichviewSongSection].trim());
            }

            // Change the background colour of this button to show it is active
            Button newbutton = (Button) v;
            if (isImage) {
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

    public class setButtonClick implements View.OnClickListener {
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
            presenter_set_buttons.smoothScrollTo(0, v.getTop());

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

            tempSongLocation = FullscreenActivity.mSetList[whichviewSetSection];

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
            getSongLocation();
            findSongInFolder();

            // Close the drawers in case they are open
            mDrawerLayout.closeDrawer(expListViewOption);
            mDrawerLayout.closeDrawer(expListViewSong);

            // Save the preferences with the new songfilename
            FullscreenActivity.songfilename = songpart[1];
            Preferences.savePreferences();

            // Redraw the Lyrics View
            redrawPresenterPage();

            // Since the slide has been armed, but not projected, turn off the project button
            // This encourages the user to click it again to update the projector screen
            turnOffProjectButton();

            // Now select the first song section button (if it exists)
            whichsongsection = 0;
            selectSectionButtonInSong();
        }
    }

    public void songDetailsButtonClick(View view) {
            findViewById(R.id.pres_details).setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));

            newFragment = PopUpSongDetailsFragment.newInstance();
            newFragment.show(getFragmentManager(), "dialog");

            // After a short time, turn off the button
            Handler delay = new Handler();
            delay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    layoutButton_isSelected = false;
                    findViewById(R.id.pres_details).setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue));
                }
            }, 500);
    }

    public void popupPresentationOrder(View view) {
        // This is called when a user clicks on the Edit presentation order button
        // It opens a simple alert that allows the user to edit the presentation order
        newFragment = PopUpPresentationOrderFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void projectButtonClick(View view) {
        if (numdisplays > 0 && !blankButton_isSelected) {
            projectButton_isSelected = true;

            presenter_project_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
            presenter_actions_buttons.smoothScrollTo(0, presenter_project_group.getTop());

            if (!isPDF && !isImage) {
                buttonPresentText = presenter_slide_text.getText().toString().trim();
            } else if (!isPDF && isImage) {
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
            //turnOffAlertButton();
            //turnOffScriptureButton();

            // Update the projector
            MyPresentation.UpDatePresentation();
        }
    }

    public void turnOffProjectButton() {
        // if button is already selected, unselect it
        if (projectButton_isSelected) {
            projectButton_isSelected = false;
            presenter_project_group.setBackgroundDrawable(null);
        }
    }

    public void alertButtonClick(View view) {
        if (numdisplays > 0 && !blankButton_isSelected) {
            alertButton_isSelected = true;

            presenter_alert_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
            presenter_actions_buttons.smoothScrollTo(0, presenter_alert_group.getTop());

            newFragment = PopUpAlertFragment.newInstance();
            newFragment.show(getFragmentManager(), "dialog");

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

    public void slideButtonClick(View view) {
        slideButton_isSelected = true;
        presenter_slide_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
        presenter_actions_buttons.smoothScrollTo(0, presenter_slide_group.getTop());

        FullscreenActivity.whattodo = "customreusable_slide";
        newFragment = PopUpCustomSlideFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");

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

    public void scriptureButtonClick(View view) {
        scriptureButton_isSelected = true;
        presenter_scripture_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
        presenter_actions_buttons.smoothScrollTo(0, presenter_scripture_group.getTop());

        FullscreenActivity.whattodo = "customreusable_scripture";
        newFragment = PopUpCustomSlideFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");

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


    public void backgroundButtonClick(View view) {
        if (numdisplays > 0 && !blankButton_isSelected) {
            backgroundButton_isSelected = true;

            presenter_backgrounds_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_red_active));
            presenter_settings_buttons.smoothScrollTo(0, presenter_backgrounds_group.getTop());

            newFragment = PopUpBackgroundsFragment.newInstance();
            newFragment.show(getFragmentManager(), "dialog");

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

    public void layoutButtonClick(View view) {
        if (numdisplays > 0 && !blankButton_isSelected) {
            layoutButton_isSelected = true;

            presenter_layout_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_red_active));
            presenter_settings_buttons.smoothScrollTo(0, presenter_layout_group.getTop());

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

    public void logoButtonClick(View view) {


        if (numdisplays > 0 && !blankButton_isSelected) {

            if (logoButton_isSelected && !endofset) {
                logoButton_isSelected = false;
                presenter_logo_group.setBackgroundDrawable(null);
                logo_on = "N";
                song_on = "N";
                blackout = "N";
                turnOffProjectButton();
                turnOffBlankButton();

                MyPresentation.fadeOutLogo();
            } else {
                logoButton_isSelected = true;
                presenter_logo_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
                presenter_actions_buttons.smoothScrollTo(0, presenter_logo_group.getTop());
                song_on = "N";
                blackout = "N";
                logo_on = "Y";
                turnOffProjectButton();
                turnOffBlankButton();

                MyPresentation.fadeInLogo();
            }
        }
    }

    public void audioButtonClick(View view) {
        audioButton_isSelected = true;

        presenter_audio_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
        presenter_actions_buttons.smoothScrollTo(0, presenter_audio_group.getTop());

        newFragment = PopUpMediaStoreFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");

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

    public void dBButtonClick(View view) {
        // Check audio record is allowed
        if (ActivityCompat.checkSelfPermission(PresenterMode.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Snackbar.make(mLayout, R.string.microphone_rationale, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(PresenterMode.this, new String[]{Manifest.permission.RECORD_AUDIO}, requestMicrophone);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                        requestMicrophone);
            }

        } else {

            dBButton_isSelected = true;

            presenter_dB_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
            presenter_actions_buttons.smoothScrollTo(0, presenter_dB_group.getTop());

            newFragment = PopUpSoundLevelMeterFragment.newInstance();
            newFragment.show(getFragmentManager(), "dialog");

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

    public void turnOffLogoButton() {
        // if button is already selected, unselect it
        if (logoButton_isSelected) {
            logoButton_isSelected = false;
            presenter_logo_group.setBackgroundDrawable(null);
        }
    }

    public void blankButtonClick(View view) {
        if (numdisplays > 0) {
            if (blankButton_isSelected) {
                blankButton_isSelected = false;
                presenter_blank_group.setBackgroundDrawable(null);
            } else {
                blankButton_isSelected = true;
                presenter_blank_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
                presenter_actions_buttons.smoothScrollTo(0, presenter_blank_group.getTop());
            }
            blackout = "Y";

            // Turn off the other actions buttons as we are now running the blackout!
            turnOffProjectButton();
            turnOffLogoButton();
            //turnOffAlertButton();
            //turnOffScriptureButton();

            // Update the projector
            MyPresentation.UpDatePresentation();
        }
    }

    public void turnOffBlankButton() {
        // if button is already selected, unselect it
        if (blankButton_isSelected) {
            blankButton_isSelected = false;
            presenter_blank_group.setBackgroundDrawable(null);
        }
    }

    public void checkDisplays(View view) {
        firsttime = true;
        displayButton_isSelected = true;
        presenter_displays_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_red_active));
        presenter_settings_buttons.smoothScrollTo(0, presenter_displays_group.getTop());
        updateDisplays();
        // After a short time, turn off the button
        Handler delay = new Handler();
        delay.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayButton_isSelected = false;
                presenter_displays_group.setBackgroundDrawable(null);
            }
        }, 500);

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
            tempfiletosearch = tempfiletosearch.replace("../Scripture/_cache/","**"+FullscreenActivity.text_scripture+"/");
        } else if (tempfiletosearch.contains("../Slides/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../Slides/_cache/","**"+FullscreenActivity.text_slide+"/");
        } else if (tempfiletosearch.contains("../Notes/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../Notes/_cache/","**"+FullscreenActivity.text_note+"/");
        } else if (tempfiletosearch.contains("../Variations/")) {
            tempfiletosearch = tempfiletosearch.replace("../Variations/","**"+FullscreenActivity.text_variation+"/");
        } else if (tempfiletosearch.contains("../Images/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../Images/_cache/","**"+FullscreenActivity.image+"/");
        }
        whichsonginset = -1;

        for (int sis = 0; sis < FullscreenActivity.setSize; sis++) {
            if (FullscreenActivity.mSetList[sis].equals(tempfiletosearch)) {
                Button whichsongtoclick = (Button) presenter_set_buttonsListView.findViewById(sis);
                whichsongtoclick.performClick();
                //presenter_set_buttons.smoothScrollTo(0, whichsongtoclick.getTop());
                whichsongsection = 0;
                whichsonginset = sis;

                FullscreenActivity.indexSongInSet = whichsonginset;
                selectSectionButtonInSong();
            }
        }
    }

    public void selectSectionButtonInSong() {
        // if whichsongsection=-1 then we want to pick the first section of the previous song in set
        // Otherwise, move to the next one.
        // If we are at the end, move to the nextsonginset

        if (whichsongsection == -1) {
            whichsongsection = 0;
            previousSongInSet();

        } else if (whichsongsection == 0) {
            if (presenter_song_buttonsListView.findViewById(0) != null) {
                presenter_song_buttonsListView.findViewById(0).performClick();
                whichsongsection = 0;
            }
        } else if (whichsongsection < songSections.length) {
            if (presenter_song_buttonsListView.findViewById(whichsongsection) != null) {
                presenter_song_buttonsListView.findViewById(whichsongsection).performClick();
            }
        } else {
            if (FullscreenActivity.indexSongInSet<FullscreenActivity.mSetList.length-1) {
                whichsongsection = 0;
                nextSongInSet();
                endofset = false;
            } else {
                FullscreenActivity.myToastMessage = getResources().getString(R.string.lastsong);
                ShowToast.showToast(PresenterMode.this);
                endofset = true;
                whichsongsection -= 1;
                logoButtonClick(presenter_logo_group);

            }
        }
    }

    public void nextSongInSet() {
        FullscreenActivity.indexSongInSet = whichsonginset;
        FullscreenActivity.indexSongInSet += 1;
        whichsongsection = 0;
        doMoveInSet();
    }

    public void previousSongInSet() {
        FullscreenActivity.indexSongInSet = whichsonginset;
        if ((FullscreenActivity.indexSongInSet - 1) >= 0) {
            FullscreenActivity.indexSongInSet -= 1;
            whichsongsection = 0;
            doMoveInSet();
        }
    }

    public void doMoveInSet() {
        invalidateOptionsMenu();
        FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
        if (!FullscreenActivity.linkclicked.contains("/")) {
            // Right it doesn't, so add the /
            FullscreenActivity.linkclicked = "/" + FullscreenActivity.linkclicked;
        }

        // Now split the linkclicked into two song parts 0=folder 1=file
        String[] songpart = FullscreenActivity.linkclicked.split("/");

        // If the folder length isn't 0, it is a folder
        if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = songpart[0];

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Scripture/_cache";
            songpart[0] = "../Scripture/_cache";

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Slides/_cache";
            songpart[0] = "../Slides/_cache";

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Notes/_cache";
            songpart[0] = "../Notes/_cache";

        } else if (songpart[0].length() > 0 &&
                songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Images/_cache";
            songpart[0] = "../Images/_cache";

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Variations";
            songpart[0] = "../Variations";

        } else {
            FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
        }

        FullscreenActivity.songfilename = songpart[1];

        // Look for song in song folder
        findSongInFolder();

        // Redraw the Lyrics View
        FullscreenActivity.songfilename = null;
        FullscreenActivity.songfilename = "";
        FullscreenActivity.songfilename = songpart[1];

        // Save the preferences
        Preferences.savePreferences();

        selectSongButtonInSet();

        redrawPresenterPage();

        expListViewOption.setSelection(FullscreenActivity.indexSongInSet);

    }

    public void findSongInFolder() {
        // Try to open the appropriate Song folder on the left menu
        expListViewSong.expandGroup(0);
        expListViewSong.setFastScrollEnabled(false);
        //expListViewSong.setFastScrollEnabled(true);
        for (int z = 0; z < listDataHeaderSong.size() - 1; z++) {
            if (listDataHeaderSong.get(z).equals(FullscreenActivity.whichSongFolder)) {
                expListViewSong.expandGroup(z);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu = this.menu;

        ab = getSupportActionBar();
        if (Build.VERSION.SDK_INT >= 14 && ab != null) {
            ab.setHomeButtonEnabled(false); // disable the button
            ab.setDisplayHomeAsUpEnabled(false); // remove the left caret
            ab.setDisplayShowHomeEnabled(false); // remove the icon
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        SetActions.prepareSetList();
        SetActions.indexSongInSet();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.presenter_actions, menu);
        this.menu = menu;

        // Force overflow icon to show, even if hardware key is present
        try {
            ViewConfiguration config = ViewConfiguration.get(PresenterMode.this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        // Force icons to show in overflow menu
        if (ab != null && menu != null){
            if(menu.getClass().getSimpleName().equals("MenuBuilder")){
                try{
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                }
                catch(NoSuchMethodException e){
                    Log.e("menu", "onMenuOpened", e);
                }
                catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
        }

        MenuItem set_back = menu.findItem(R.id.set_back);
        MenuItem set_forward = menu.findItem(R.id.set_forward);
        if (FullscreenActivity.setSize > 0 && FullscreenActivity.setView) {
            set_back.setVisible(true);
            set_forward.setVisible(true);
            set_back.getIcon().setAlpha(255);
            set_forward.getIcon().setAlpha(255);

        } else {
            set_back.setVisible(false);
            set_forward.setVisible(false);
        }
        //Now decide if the song being viewed has a song section before it.
        //Otherwise disable the back button
        if (FullscreenActivity.indexSongInSet < 1 && whichsongsection < 1) {
            set_back.setEnabled(false);
            set_back.getIcon().setAlpha(30);
        }
        //Now decide if the song being viewed has a song section after it.
        //Otherwise disable the forward button
        if (FullscreenActivity.indexSongInSet >= (FullscreenActivity.setSize - 1) && whichsongsection >= (numsectionbuttons - 1)) {
            set_forward.setEnabled(false);
            set_forward.getIcon().setAlpha(30);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.perform_mode:
                // Switch to performance mode
                FullscreenActivity.whichMode = "Performance";
                Preferences.savePreferences();
                Intent performmode = new Intent();
                performmode.setClass(PresenterMode.this, FullscreenActivity.class);
                startActivity(performmode);
                finish();
                return true;

            case R.id.action_search:
                if (mDrawerLayout.isDrawerOpen(expListViewSong)) {
                    mDrawerLayout.closeDrawer(expListViewSong);
                } else {
                    mDrawerLayout.openDrawer(expListViewSong);
                }
                return true;

            case R.id.action_fullsearch:
                FullscreenActivity.whattodo = "presentermodesearchreturn";
                if (FullscreenActivity.safetosearch) {
                    Intent intent = new Intent();
                    intent.setClass(PresenterMode.this, SearchViewFilterModeNew.class);
                    startActivity(intent);
                    finish();

                } else {
                    FullscreenActivity.myToastMessage = getString(R.string.wait);
                    ShowToast.showToast(PresenterMode.this);
                }

/*



                newFragment = PopUpSearchViewFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
*/
                return true;

            case R.id.action_settings:
                if (mDrawerLayout.isDrawerOpen(expListViewOption)) {
                    mDrawerLayout.closeDrawer(expListViewOption);
                } else {
                    mDrawerLayout.openDrawer(expListViewOption);
                }
                return true;

            case R.id.set_add:
                //mDrawerLayout.openDrawer(expListViewOption);
                setlisticon_ImageView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse));

                if (FullscreenActivity.whichSongFolder
                        .equals(FullscreenActivity.mainfoldername)) {
                    FullscreenActivity.whatsongforsetwork = "$**_"
                            + FullscreenActivity.songfilename + "_**$";
                } else {
                    FullscreenActivity.whatsongforsetwork = "$**_"
                            + FullscreenActivity.whichSongFolder + "/"
                            + FullscreenActivity.songfilename + "_**$";
                }
                // Allow the song to be added, even if it is already there
                FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
                // Tell the user that the song has been added.
                FullscreenActivity.myToastMessage = "\""
                        + FullscreenActivity.songfilename + "\" "
                        + getResources().getString(R.string.addedtoset);
                ShowToast.showToast(PresenterMode.this);

                // Switch set view on
                FullscreenActivity.setView = true;
                //SetActions.prepareSetList();
                SetActions.indexSongInSet();

                // Save the set and other preferences
                Preferences.savePreferences();

                prepareOptionMenu();
                setupSetButtons();
                setupSongButtons();

                // Update the menu items
                invalidateOptionsMenu();

                // Hide the menus - 1 second after opening the Option menu, close it
                // (1000ms total)
                Handler optionMenuFlickClosed = new Handler();
                optionMenuFlickClosed.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawer(expListViewOption);
                    }
                }, 1000); // 1000ms delay

                return true;


            case R.id.set_back:
                whichsongsection -= 1;
                selectSectionButtonInSong();
                presenter_project_group.performClick();
                presenter_actions_buttons.smoothScrollTo(0, presenter_project_group.getTop());
                return true;

            case R.id.set_forward:
                whichsongsection += 1;
                selectSectionButtonInSong();
                presenter_project_group.performClick();
                presenter_actions_buttons.smoothScrollTo(0, presenter_project_group.getTop());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        numdisplays = presentationDisplays.length;
        if (numdisplays != 0) {
            for (Display display : presentationDisplays) {
                MyPresentation mPresentation = new MyPresentation(this, display);
                mPresentation.dismiss();
            }
        }
        // Be sure to call the super class.
        super.onResume();
    }

    @Override
    protected void onPause() {
        numdisplays = presentationDisplays.length;
        if (numdisplays != 0) {
            for (Display display : presentationDisplays) {
                MyPresentation mPresentation = new MyPresentation(this, display);
                mPresentation.dismiss();
            }
        }
        // Be sure to call the super class.
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        numdisplays = presentationDisplays.length;
        if (numdisplays != 0) {
            for (Display display : presentationDisplays) {
                MyPresentation mPresentation = new MyPresentation(this, display);
                mPresentation.dismiss();
            }
        }
        // Be sure to call the super class.
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        // Be sure to call the super class.
        numdisplays = presentationDisplays.length;
        if (numdisplays != 0) {
            for (Display display : presentationDisplays) {
                MyPresentation mPresentation = new MyPresentation(this, display);
                mPresentation.dismiss();
            }
        }
        super.onStop();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // To stop repeated pressing too quickly, set a handler to wait for 1 sec before reenabling
        if (event.getAction() == KeyEvent.ACTION_UP && pedalsenabled) {
            if (keyCode == FullscreenActivity.pageturner_PREVIOUS || keyCode == FullscreenActivity.pageturner_DOWN) {
                pedalsenabled = false;
                // Close both drawers
                mDrawerLayout.closeDrawer(expListViewSong);
                mDrawerLayout.closeDrawer(expListViewOption);

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
                presenter_actions_buttons.smoothScrollTo(0, presenter_project_group.getTop());
            } else if (keyCode == FullscreenActivity.pageturner_NEXT || keyCode == FullscreenActivity.pageturner_UP) {
                pedalsenabled = false;
                // Close both drawers
                mDrawerLayout.closeDrawer(expListViewSong);
                mDrawerLayout.closeDrawer(expListViewOption);
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
                    presenter_actions_buttons.smoothScrollTo(0, presenter_logo_group.getTop());
                } else {
                    presenter_project_group.performClick();
                    presenter_actions_buttons.smoothScrollTo(0, presenter_project_group.getTop());
                }
            }
        }

        return super.onKeyUp(keyCode,event);
    }

    @Override
    public void doEdit() {
        FullscreenActivity.whattodo = "editsong";
        newFragment = PopUpEditSongFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void searchResults() {
        // Load up the song
        try {
            LoadXML.loadXML();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        refreshAll();
    }

    @Override
    public void songLongClick() {

    }

    @Override
    public void loadSongFromSet() {
        Preferences.savePreferences();
        // Redraw the set buttons as the user may have changed the order
        refreshAll();

        try {
            newFragment.dismiss();
        } catch (Exception e) {
            Log.d("d","Fragment already closed");
        }

        FullscreenActivity.setView = true;
        // Specify which songinset button
        whichsonginset = FullscreenActivity.indexSongInSet;
        whichsongsection = 0;

        // Select it
        Button which_song_to_click = (Button) presenter_set_buttonsListView.findViewById(whichsonginset);
        which_song_to_click.performClick();
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
            ShowToast.showToast(PresenterMode.this);
        }
        whichsonginset = 0;
        whichsongsection = 0;
        SetActions.prepareSetList();
        prepareSongMenu();
        prepareOptionMenu();
        // Expand set group
        setupSetButtons();
        setupSongButtons();
        redrawPresenterPage();
        SetActions.indexSongInSet();
        invalidateOptionsMenu();
        findSongInFolder();

        // Reopen the set or song menu if something has changed here
        if (FullscreenActivity.whattodo.equals("loadset") || FullscreenActivity.whattodo.equals("clearset")) {
            //expListViewOption.expandGroup(0);
            //mDrawerLayout.openDrawer(expListViewOption);
            setlisticon_ImageView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse));
        }
        if (FullscreenActivity.whattodo.equals("renamesong") || FullscreenActivity.whattodo.equals("createsong")) {
            findSongInFolder();
            mDrawerLayout.openDrawer(expListViewSong);
        }

        // If menus are open, close them after 1 second
        Handler closeMenus = new Handler();
        closeMenus.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(expListViewSong);
                mDrawerLayout.closeDrawer(expListViewOption);
            }
        }, 1000); // 1000ms delay
    }

    @Override
    public void closePopUps() {
        if (newFragment!=null) {
            newFragment.dismiss();
        }
    }

    @Override
    public void pageButtonAlpha(String s) {
        // Do nothing
        // This override is for StageMode
    }

    @Override
    public void openFragment() {

    }

    @Override
    public void confirmedAction() {
        switch (FullscreenActivity.whattodo) {
            case "saveset":
                FullscreenActivity.whattodo = "";
                CreateNewSet.doCreation();
                if (FullscreenActivity.myToastMessage.equals("yes")) {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.set_save)
                            + " - " + getResources().getString(R.string.ok);
                } else {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.set_save)
                            + " - " + getResources().getString(R.string.no);
                }
                break;

            case "clearset":
                // Clear the set
                FullscreenActivity.mySet = "";
                FullscreenActivity.mSetList = null;
                FullscreenActivity.setView = false;
                invalidateOptionsMenu();

                // Save the new, empty, set
                Preferences.savePreferences();

                FullscreenActivity.myToastMessage = getResources().getString(R.string.options_set_clear) + " " + getResources().getString(R.string.ok);

                // Refresh all stuff needed
                refreshAll();

                break;

            case "deleteset":
                // Load the set up
                File settodelete = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
                if (settodelete.delete()) {
                    FullscreenActivity.myToastMessage = FullscreenActivity.settoload + " " + getResources().getString(R.string.sethasbeendeleted);
                }

                // Refresh all stuff needed
                refreshAll();

                break;

            case "deletesong":
                // Delete current song
                FullscreenActivity.setView = false;
                String setFileLocation;
                if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                    setFileLocation = FullscreenActivity.dir + "/" + FullscreenActivity.songfilename;
                } else {
                    setFileLocation = FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename;
                }
                File filetoremove = new File(setFileLocation);
                if (filetoremove.delete()) {
                    FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.songfilename + "\" "
                            + getResources().getString(R.string.songhasbeendeleted);
                } else {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.deleteerror_start)
                            + " \"" + FullscreenActivity.songfilename + "\" "
                            + getResources().getString(R.string.deleteerror_end_song);
                }

                invalidateOptionsMenu();

                // Save the new, empty, set
                Preferences.savePreferences();

                // Refresh all stuff needed
                refreshAll();

                break;

            case "exit":
                Intent viewsong = new Intent(this, FullscreenActivity.class);
                FullscreenActivity.whichMode = "Performance";
                Preferences.savePreferences();
                viewsong.setClass(PresenterMode.this, FullscreenActivity.class);
                startActivity(viewsong);
                this.finish();
        }
    }

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
        //playSlideShow.setClickable(true);

    }

    public void prepareStartAutoSlideShow() {
        // Stop the slideshow if it already happening
        prepareStopAutoSlideShow();

        if (timeEditText.getText().toString()!=null) {
            try {
                autoslidetime = Integer.parseInt(timeEditText.getText().toString());
            } catch (Exception e) {
                autoslidetime = 0;
            }
        } else {
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
            autoslideshowtask = new AutoSlideShow();
            autoslideshowtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.bad_time);
            ShowToast.showToast(PresenterMode.this);
        }
    }

    private class AutoSlideShow extends AsyncTask <Object,Void,String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Check if we can move to the next section in the song
           if (whichsongsection < songSections.length-1 && isplayingautoslideshow) {
                // Move to next song section
                whichsongsection ++;
                selectSectionButtonInSong();
                prepareStopAutoSlideShow();
                prepareStartAutoSlideShow();
            } else if (autoslideloop && whichsongsection>=(songSections.length-1) && isplayingautoslideshow) {
                // Go back to first song section
                whichsongsection = 0;
                selectSectionButtonInSong();
                prepareStopAutoSlideShow();
                prepareStartAutoSlideShow();
            } else {
                // Stop autoplay
                prepareStopAutoSlideShow();
            }
        }

        @Override
        protected String doInBackground(Object[] params) {
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
                presenter_slide_text.setVisibility(View.GONE);
                presenter_slide_image.setVisibility(View.VISIBLE);
                presenter_slide_image.setBackground(getResources().getDrawable(R.drawable.unhappy_android));

            }

            loadsong_async = new LoadSongAsync();
            loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class LoadSongAsync extends AsyncTask <Object,Void,String> {

        @Override
        protected void onPreExecute() {
            isPDF = false;
            isSong = true;
            if ((FullscreenActivity.songfilename.contains(".pdf") || FullscreenActivity.songfilename.contains(".PDF"))) {
                // File is pdf
                isPDF = true;
                isSong = false;
            }

            if (FullscreenActivity.whichSongFolder.contains("../Scripture") || FullscreenActivity.whichSongFolder.contains("../Images") || FullscreenActivity.whichSongFolder.contains("../Slides") || FullscreenActivity.whichSongFolder.contains("../Notes")) {
                isSong = false;
            }

            if (!isPDF) {
/*
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
*/
                presenter_slide_text.setVisibility(View.VISIBLE);
                presenter_slide_image.setVisibility(View.GONE);

/*
            } else {
                FullscreenActivity.mLyrics = getResources().getString(R.string.pdf_functionnotavailable);
                // Re-initialise all song tags
                LoadXML.initialiseSongTags();

                Preferences.savePreferences();
 */
            }

            currentsectionbutton = null;
            //FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
        }


        @Override
        protected String doInBackground(Object... params) {
            if (!isPDF) {
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                FullscreenActivity.mLyrics = getResources().getString(R.string.pdf_functionnotavailable);
                // Re-initialise all song tags
                LoadXML.initialiseSongTags();
                Preferences.savePreferences();
            }

            // FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
            // LyricsDisplay.parseLyrics();
            // Sort song formatting
            // First remove chord lines
            FullscreenActivity.presenterChords.equals("N");
            FullscreenActivity.myLyrics = ProcessSong.removeChordLines(FullscreenActivity.mLyrics);
            FullscreenActivity.myLyrics = ProcessSong.removeCommentLines(FullscreenActivity.myLyrics);
            FullscreenActivity.showChords = false;
            FullscreenActivity.myLyrics = ProcessSong.removeUnderScores(FullscreenActivity.myLyrics);

            // 1. Sort multiline verse/chord formats
            FullscreenActivity.myLyrics = ProcessSong.fixMultiLineFormat(FullscreenActivity.myLyrics);

            // 2. Split the song into sections
            songSections = ProcessSong.splitSongIntoSections(FullscreenActivity.myLyrics);

            // 3. Put the song into presentation order if required
            if (FullscreenActivity.usePresentationOrder && !FullscreenActivity.mPresentation.isEmpty() && !FullscreenActivity.mPresentation.equals("")) {
                songSections = ProcessSong.matchPresentationOrder(songSections);
            }

            // 3b Add extra sections for double linebreaks and || code
            songSections = ProcessSong.splitLaterSplits(songSections);

            // 4. Get the section headings/types (may have changed after presentationorder
            songSectionsLabels = new String[songSections.length];
            for (int sl=0; sl < songSections.length; sl++) {
                songSectionsLabels[sl] = ProcessSong.getSectionHeadings(songSections[sl]);
            }

            // 5. Get rid of the tag/heading lines
            songSections =  ProcessSong.removeTagLines(songSections);

/*
            // Only do this if this isn't a scripture - as it starts with numbers!
            PresentPrepareSong.splitSongIntoSections("presenter");
*/
            return "done";
        }

        protected void onPostExecute(String s) {
            findSongInFolder();
            setupSongButtons();
        }
    }

}