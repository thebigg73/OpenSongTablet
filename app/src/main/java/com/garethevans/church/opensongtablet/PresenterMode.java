/*
 * Copyright (c) 2015.
 * The code is provided free of charge.  You can use, modify, contribute and improve it as long as this source is referenced.
 * Commercial use should seek permission.
 */

package com.garethevans.church.opensongtablet;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
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

public class PresenterMode extends Activity implements PopUpEditSongFragment.MyInterface,
        PopUpListSetsFragment.MyInterface, PopUpAreYouSureFragment.MyInterface,
        PopUpSongRenameFragment.MyInterface, PopUpSearchViewFragment.MyInterface,
        PopUpEditSetFragment.MyInterface, PopUpSongCreateFragment.MyInterface,
        PopUpSearchViewFragment.MyVibrator, PopUpSongDetailsFragment.MyInterface,
        PopUpFontsFragment.MyInterface, PopUpCustomSlideFragment.MyInterface {

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

    // Variables used by the popups
    static String whatBackgroundLoaded;

    // Which Actions buttons are selected
    boolean projectButton_isSelected = false;
    boolean logoButton_isSelected = false;
    boolean blankButton_isSelected = false;
    boolean displayButton_isSelected = false;
    //boolean scriptureButton_isSelected = false;
    //boolean slideButton_isSelected = false;
    boolean alertButton_isSelected = false;
    boolean audioButton_isSelected = false;
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
    LinearLayout presenter_slide_group;

    // Settings buttons
    ScrollView presenter_settings_buttons;
    LinearLayout presenter_backgrounds_group;
    LinearLayout presenter_layout_group;
    LinearLayout presenter_displays_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mp = new MediaPlayer();

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Load up the user preferences
        //myPreferences = getPreferences(MODE_PRIVATE);
        Preferences.loadPreferences();

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
        ListSongFiles.listSongs();
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

        if (FullscreenActivity.whichSongFolder.contains("../OpenSong Scripture") || FullscreenActivity.whichSongFolder.contains("../Images") || FullscreenActivity.whichSongFolder.contains("../Slides") || FullscreenActivity.whichSongFolder.contains("../Notes")) {
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
        }

        // Load the layout and set the title
        getActionBar().setTitle(getResources().getString(R.string.presentermode));
        setContentView(R.layout.presentermode);

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
                LyricsDisplay.parseLyrics();
                PresentPrepareSong.splitSongIntoSections();
                setupSongButtons();
            }
        });

        // Set buttons
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
        // Hide scripture for now until I get it working
        presenter_scripture_group.setVisibility(View.GONE);
        presenter_slide_group = (LinearLayout) findViewById(R.id.presenter_slide_group);
        // Hide slide for now until I get it working
        presenter_slide_group.setVisibility(View.GONE);
        presenter_alert_group = (LinearLayout) findViewById(R.id.presenter_alert_group);
        presenter_audio_group = (LinearLayout) findViewById(R.id.presenter_audio_group);

        // Settings buttons
        presenter_settings_buttons = (ScrollView) findViewById(R.id.preso_settings_scroll);
        presenter_settings_buttons.setScrollbarFadingEnabled(false);
        presenter_backgrounds_group = (LinearLayout) findViewById(R.id.presenter_backgrounds_group);
        presenter_layout_group = (LinearLayout) findViewById(R.id.presenter_layout_group);
        presenter_displays_group = (LinearLayout) findViewById(R.id.presenter_display_group);

        // Set up the navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expListViewSong = (ExpandableListView) findViewById(R.id.song_list_ex);
        expListViewOption = (ExpandableListView) findViewById(R.id.option_list_ex);

        prepareSongMenu();
        prepareOptionMenu();

        if (!isPDF) {
            PresentPrepareSong.splitSongIntoSections();
        }
        setupSongButtons();
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
        DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
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
                        //FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
                        FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
                        FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.songfilename + "_**$";
                    } else {
                        //FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/" + listDataHeaderSong.get(groupPosition));
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
                    mDrawerLayout.openDrawer(expListViewOption);
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
                        //FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
                        FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
                    } else {
                        //FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/" + listDataHeaderSong.get(groupPosition));
                        FullscreenActivity.whichSongFolder = listDataHeaderSong.get(groupPosition);
                    }
                    // Set the appropriate song filename
                    FullscreenActivity.songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

                    if (FullscreenActivity.setView.equals("Y") && FullscreenActivity.setSize > 0) {
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
                            FullscreenActivity.setView = "N";
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
                        FullscreenActivity.setView = "N";
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
        options_set.add(getResources().getString(R.string.add_custom_slide).toUpperCase(FullscreenActivity.locale));
        options_set.add(getResources().getString(R.string.options_set_edit));
        options_set.add("");

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
            if (!FullscreenActivity.mSetList[r].isEmpty()) {
                options_set.add(FullscreenActivity.mSetList[r]);
            }
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
                            DialogFragment newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 1) {
                            // Save current set
                            FullscreenActivity.whattodo = "saveset";
                            DialogFragment newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 2) {
                            // Clear current set
                            FullscreenActivity.whattodo = "clearset";
                            String message = getResources().getString(R.string.options_clearthisset);
                            DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 3) {
                            // Delete saved set
                            FullscreenActivity.whattodo = "deleteset";
                            DialogFragment newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 4) {
                            // Export current set
                            FullscreenActivity.whattodo = "exportset";
                            DialogFragment newFragment = PopUpListSetsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 5) {
                            // Add a custom slide
                            DialogFragment newFragment = PopUpCustomSlideFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 6) {
                            // Edit current set
                            // Only works for ICS or above
                            FullscreenActivity.whattodo = "editset";
                            DialogFragment newFragment = PopUpEditSetFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");

                        } else if (childPosition == 7) {
                            // Blank entry
                            Log.d("set","blank line");

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
                                DialogFragment newFragment = PopUpEditSongFragment.newInstance();
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
                                DialogFragment newFragment = PopUpSongRenameFragment.newInstance();
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
                                DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
                                newFragment.show(getFragmentManager(), "dialog");
                            }

                        } else if (childPosition == 3) {
                            // New
                            FullscreenActivity.whattodo = "createsong";
                            DialogFragment newFragment = PopUpSongCreateFragment.newInstance();
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
                            DialogFragment newFragment = PopUpFontsFragment.newInstance();
                            newFragment.show(getFragmentManager(), "dialog");


                        } else if (childPosition == 2) {
                            // Assign foot pedal
                            FullscreenActivity.whattodo = "footpedal";
                            DialogFragment newFragment = PopUpPedalsFragment.newInstance();
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
            newSongButton.setBackgroundResource(R.drawable.present_section_button);
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
        // Decide if it is a song, scripture, slide or note and get the actual file location
        if (songpart[0].length() > 0 && !songpart[0].contains(FullscreenActivity.scripture) && !songpart[0].contains(FullscreenActivity.slide) && !songpart[0].contains(FullscreenActivity.note)) {
            FullscreenActivity.whichSongFolder = songpart[0];
            //FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/" + songpart[0]);

        } else if (songpart[0].length() > 0 && songpart[0].contains(FullscreenActivity.scripture) && !songpart[0].contains(FullscreenActivity.slide) && !songpart[0].contains(FullscreenActivity.note)) {
            FullscreenActivity.whichSongFolder = "../OpenSong Scripture/_cache";
            songpart[0] = "../OpenSong Scripture/_cache";

        } else if (songpart[0].length() > 0 && !songpart[0].contains(FullscreenActivity.scripture) && songpart[0].contains(FullscreenActivity.slide) && !songpart[0].contains(FullscreenActivity.note)) {
            FullscreenActivity.whichSongFolder = "../Slides/_cache";
            songpart[0] = "../Slides/_cache";

        } else if (songpart[0].length() > 0 && !songpart[0].contains(FullscreenActivity.scripture) && !songpart[0].contains(FullscreenActivity.slide) && songpart[0].contains(FullscreenActivity.note)) {
            FullscreenActivity.whichSongFolder = "../Notes/_cache";
            songpart[0] = "../Notes/_cache";

        } else {
            FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
            //FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
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
        // Now load the appropriate song folder
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

    @Override
    public void openSongEdit() {
        // This is called from the create a new song popup
        ListSongFiles.listSongFolders();
        redrawPresenterPage();

        FullscreenActivity.whattodo = "editsong";
        DialogFragment newFragment = PopUpEditSongFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void addSlideToSet() {
        String filename;
        String templocator;

        if (FullscreenActivity.noteorslide.equals("note")) {
            filename = FullscreenActivity.dircustomnotes + "/" + FullscreenActivity.customslide_title;
            templocator = FullscreenActivity.note;
        } else {
            filename = FullscreenActivity.dircustomslides + "/" + FullscreenActivity.customslide_title;
            templocator = FullscreenActivity.slide;
        }

        // If slide content is empty - put the title in
        if (FullscreenActivity.customslide_content.isEmpty()) {
            FullscreenActivity.customslide_content = FullscreenActivity.customslide_title;
        }

        // Prepare the custom slide so it can be viewed in the app
        // When exporting/saving the set, the contents get grabbed from this
        FullscreenActivity.mynewXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        FullscreenActivity.mynewXML += "<song>\n";
        FullscreenActivity.mynewXML += "  <title>" + FullscreenActivity.customslide_title + "</title>\n";
        FullscreenActivity.mynewXML += "  <author></author>\n";
        FullscreenActivity.mynewXML += "  <user1></user1>\n";
        FullscreenActivity.mynewXML += "  <user2></user2>\n";
        FullscreenActivity.mynewXML += "  <user3></user3>\n";
        FullscreenActivity.mynewXML += "  <aka></aka>\n";
        FullscreenActivity.mynewXML += "  <key_line></key_line>\n";
        FullscreenActivity.mynewXML += "  <lyrics>" + FullscreenActivity.customslide_content +"</lyrics>\n";
        FullscreenActivity.mynewXML += "</song>";

        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;","&");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&","&amp;");

        // Now write the modified song
        FileOutputStream overWrite;
        try {
            overWrite = new FileOutputStream(filename,	false);
            overWrite.write(FullscreenActivity.mynewXML.getBytes());
            overWrite.flush();
            overWrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add to set
        FullscreenActivity.whatsongforsetwork = "$**_" + templocator + "/" + FullscreenActivity.customslide_title + "_**$";

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
        mDrawerLayout.openDrawer(expListViewOption);
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

        refreshAll();

    }

    public class sectionButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Re-enable the disabled button
            if (currentsectionbutton != null) {
                Button oldbutton = (Button) currentsectionbutton;
                oldbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.present_section_button));
                oldbutton.setTextSize(10.0f);
                oldbutton.setTextColor(0xffffffff);
                oldbutton.setPadding(10, 10, 10, 10);
                oldbutton.setMinimumHeight(0);
                oldbutton.setMinHeight(0);
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

            // Set the text of the button into the slide window
            presenter_slide_text.setText(songSections[whichviewSongSection].trim());

            // Change the background colour of this button to show it is active
            Button newbutton = (Button) v;
            newbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.present_section_button_active));
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
            FullscreenActivity.setView = "Y";
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

            DialogFragment newFragment = PopUpSongDetailsFragment.newInstance();
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
        DialogFragment newFragment = PopUpPresentationOrderFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void projectButtonClick(View view) {
        if (numdisplays > 0 && !blankButton_isSelected) {
            projectButton_isSelected = true;

            presenter_project_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
            presenter_actions_buttons.smoothScrollTo(0, presenter_project_group.getTop());

            if (!isPDF) {
                buttonPresentText = presenter_slide_text.getText().toString().trim();
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

            DialogFragment newFragment = PopUpAlertFragment.newInstance();
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

    public void backgroundButtonClick(View view) {
        if (numdisplays > 0 && !blankButton_isSelected) {
            backgroundButton_isSelected = true;

            presenter_backgrounds_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_red_active));
            presenter_settings_buttons.smoothScrollTo(0, presenter_backgrounds_group.getTop());

            DialogFragment newFragment = PopUpBackgroundsFragment.newInstance();
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

            DialogFragment newFragment = PopUpLayoutFragment.newInstance();
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

        DialogFragment newFragment = PopUpMediaStoreFragment.newInstance();
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

        if (tempfiletosearch.contains("../OpenSong Scripture/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../OpenSong Scripture/_cache/",FullscreenActivity.scripture+"/");
        } else if (tempfiletosearch.contains("../Slides/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../Slides/_cache/",FullscreenActivity.slide+"/");
        } else if (tempfiletosearch.contains("../Notes/_cache/")) {
            tempfiletosearch = tempfiletosearch.replace("../Notes/_cache/",FullscreenActivity.note+"/");
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
        if (songpart[0].length() > 0 && !songpart[0].contains(FullscreenActivity.scripture) && !songpart[0].contains(FullscreenActivity.slide) && !songpart[0].contains(FullscreenActivity.note)) {
            FullscreenActivity.whichSongFolder = songpart[0];
            //FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/"+ songpart[0]);

        } else if (songpart[0].length() > 0 && songpart[0].contains(FullscreenActivity.scripture) && !songpart[0].contains(FullscreenActivity.slide) && !songpart[0].contains(FullscreenActivity.note)) {
            FullscreenActivity.whichSongFolder = "../OpenSong Scripture/_cache";
            songpart[0] = "../OpenSong Scripture/_cache";

        } else if (songpart[0].length() > 0 && songpart[0].contains(FullscreenActivity.slide) && !songpart[0].contains(FullscreenActivity.note) && !songpart[0].contains(FullscreenActivity.scripture)) {
            FullscreenActivity.whichSongFolder = "../Slides/_cache";
            songpart[0] = "../Slides/_cache";

        } else if (songpart[0].length() > 0 && songpart[0].contains(FullscreenActivity.note) && !songpart[0].contains(FullscreenActivity.slide) && !songpart[0].contains(FullscreenActivity.scripture)) {
            FullscreenActivity.whichSongFolder = "../Notes/_cache";
            songpart[0] = "../Notes/_cache";

        } else {
            FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
            //FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
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
        if (getActionBar()!=null && menu != null){
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
        if (FullscreenActivity.setSize > 0 && FullscreenActivity.setView.equals("Y")) {
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
                FullscreenActivity.whattodo = "exit";
                DialogFragment newFragment = PopUpSearchViewFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");

/*
            // Need to fix this as the SearchViewFilterMode returns to FullscreenActivity
			Intent intent = new Intent();
			intent.setClass(PresenterMode.this,SearchViewFilterMode.class);
			startActivity(intent);
			finish();
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
                mDrawerLayout.openDrawer(expListViewOption);
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
                FullscreenActivity.setView = "Y";
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

    // Listener for popups
    @Override
    public void doVibrate() {
        // Vibrate to indicate something has happened
        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vb.vibrate(25);
    }

    @Override
    public void doEdit() {
        FullscreenActivity.whattodo = "editsong";
        DialogFragment newFragment = PopUpEditSongFragment.newInstance();
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
    public void refreshAll() {
        // Show the toast
        ShowToast.showToast(PresenterMode.this);
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
            expListViewOption.expandGroup(0);
            mDrawerLayout.openDrawer(expListViewOption);
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
                FullscreenActivity.setView = "N";
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
                FullscreenActivity.setView = "N";
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

}