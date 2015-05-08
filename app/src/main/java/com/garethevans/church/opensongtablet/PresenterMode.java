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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by gareth on 03/05/15.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)

public class PresenterMode extends Activity {
    // Set the variables used here
    // SharedPreferences myPreferences;

    // General variables
    static int numdisplays;
    boolean firsttime = true;
    private boolean isPDF;
    Menu menu;
    private boolean addingtoset;
    AlertDialog.Builder popupAlert;
    AlertDialog alert;;
    LinearLayout popupLayout;
    static String myAlert = FullscreenActivity.myAlert;
    static int tempfontsize = 18;
    static boolean autoscale = FullscreenActivity.presoAutoScale;

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
    boolean layoutButton_isSelected = false;

    // Keep a note of what is shown
    static String background="image";
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
    private ExpandableListAdapter listAdapterSong;

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
    Display display;
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
    LinearLayout buttonList;
    LinearLayout newSongSectionGroup;
    TextView newSongSectionText;
    static String[] songSections;
    static String[] songSectionsLabels;
    static Button newSongButton;
    View currentsectionbutton;
    int whichviewSongSection;

    // Action buttons
    ScrollView presenter_actions_buttons;
    LinearLayout presenter_project_group;
    LinearLayout presenter_logo_group;
    LinearLayout presenter_blank_group;
    LinearLayout presenter_scripture_group;
    LinearLayout presenter_alert_group;
    LinearLayout presenter_slide_group;

    // Settings buttons
    ScrollView presenter_settings_buttons;
    LinearLayout presenter_backgrounds_group;
    LinearLayout presenter_layout_group;
    LinearLayout presenter_displays_group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
        File checkfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
        if ((FullscreenActivity.songfilename.contains(".pdf") || FullscreenActivity.songfilename.contains(".PDF")) && checkfile.exists()) {
            // File is pdf
            isPDF = true;
        }

        if (!isPDF) {
            try {
                LoadXML.loadXML();
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
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

        // Presentation order
        presenter_order_view = (CheckBox) findViewById(R.id.presenter_order_text);
        presenter_order_view.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (presenter_order_view.isChecked()) {
                    FullscreenActivity.usePresentationOrder = true;
                } else {
                    FullscreenActivity.usePresentationOrder = false;
                }
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
        presenter_song_buttons = (ScrollView) findViewById(R.id.songbuttons);
        buttonList = (LinearLayout) findViewById(R.id.songButtonsListView);
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

        // Settings buttons
        presenter_settings_buttons = (ScrollView) findViewById(R.id.preso_settings_scroll);
        presenter_settings_buttons.setScrollbarFadingEnabled(false);
        presenter_backgrounds_group = (LinearLayout) findViewById(R.id.presenter_backgrounds_group);
        presenter_layout_group = (LinearLayout) findViewById(R.id.presenter_layout_group);
        presenter_displays_group = (LinearLayout) findViewById(R.id.presenter_display_group);
        // Hide display manager for now until I get it working
        // presenter_displays_group.setVisibility(View.GONE);

        // Set up the navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expListViewSong = (ExpandableListView) findViewById(R.id.song_list_ex);
        expListViewOption = (ExpandableListView) findViewById(R.id.option_list_ex);

        prepareSongMenu();
        //prepareOptionMenu();

        if (!isPDF) {
            PresentPrepareSong.splitSongIntoSections();
        }
        setupSongButtons();
        setupSetButtons();

        invalidateOptionsMenu();

         // Turn on the secondary display if possible
        updateDisplays();

        if (numdisplays>0) {
            logoButton_isSelected = true;
            presenter_logo_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
        } else {
            logo_on = "N";
        }
    }

    public void prepareSongMenu() {
        // Initialise Songs menu
//        listDataHeaderSong = new ArrayList<String>();
//        listDataChildSong = new HashMap<String, List<String>>();
        listDataHeaderSong = new ArrayList<>();

        // Get song folders
        ListSongFiles.listSongFolders();
        listDataHeaderSong.add(getResources().getString(R.string.mainfoldername));
        for (int w = 0; w < FullscreenActivity.mSongFolderNames.length - 1; w++) {
            listDataHeaderSong.add(FullscreenActivity.mSongFolderNames[w]);
        }

        for (int s = 0; s < FullscreenActivity.mSongFolderNames.length; s++) {
            List<String> song_folders = new ArrayList<String>();
            for (int t = 0; t < FullscreenActivity.childSongs[s].length; t++) {
                song_folders.add(FullscreenActivity.childSongs[s][t]);
            }
            listDataChildSong.put(listDataHeaderSong.get(s), song_folders);
        }

        listAdapterSong = new ExpandableListAdapter(expListViewSong, this, listDataHeaderSong, listDataChildSong);
        expListViewSong.setAdapter(listAdapterSong);
        expListViewSong.setFastScrollEnabled(false);

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
                        FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
                        FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
                        FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.songfilename + "_**$";
                    } else {
                        FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/" + listDataHeaderSong.get(groupPosition));
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
                        FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
                        FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
                    } else {
                        FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/" + listDataHeaderSong.get(groupPosition));
                        FullscreenActivity.whichSongFolder = listDataHeaderSong.get(groupPosition);
                    }
                    // Set the appropriate song filename
                    FullscreenActivity.songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

                    if (FullscreenActivity.setView.equals("Y") && FullscreenActivity.setSize >= 8) {
                        // Ok look for the song in the set.
                        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                            FullscreenActivity.whatsongforsetwork = FullscreenActivity.songfilename;
                        } else {
                            FullscreenActivity.whatsongforsetwork = FullscreenActivity.whichSongFolder + "/"
                                    + FullscreenActivity.linkclicked;
                        }

                        if (FullscreenActivity.mySet.indexOf(FullscreenActivity.whatsongforsetwork) >= 0) {
                            // Song is in current set.  Find the song position in the current set and load it (and next/prev)
                            // The first song has an index of 6 (the 7th item as the rest are menu items)

                            FullscreenActivity.previousSongInSet = "";
                            FullscreenActivity.nextSongInSet = "";
                            SetActions.prepareSetList();
                            setupSetButtons();

                            for (int x = 0; x < FullscreenActivity.setSize; x++) {
                                if (FullscreenActivity.mSet[x].equals(FullscreenActivity.whatsongforsetwork)) {
                                    FullscreenActivity.indexSongInSet = x;
                                    FullscreenActivity.previousSongInSet = FullscreenActivity.mSet[x - 1];
                                    if (x == FullscreenActivity.setSize - 1) {
                                        FullscreenActivity.nextSongInSet = "";
                                    } else {
                                        FullscreenActivity.nextSongInSet = FullscreenActivity.mSet[x + 1];
                                    }
                                }
                            }

                        } else {
                            // Song isn't in the set, so just show the song
                            // Switch off the set view (buttons in action bar)
                            FullscreenActivity.setView = "N";
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
        buttonList.removeAllViews();
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
            buttonList.addView(newSongSectionGroup);
        }
        presoAuthor = FullscreenActivity.mAuthor.toString().trim();
        presoCopyright = FullscreenActivity.mCopyright.toString().trim();
        presoTitle = FullscreenActivity.mTitle.toString().trim();
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
    }

    public void getSongLocation() {
        // Since song can be in sub folders, each should have a / in them
        // Songs in the main folder won't so add a / at the beginning.
        if (tempSongLocation.indexOf("/")<0) {
            // Right it doesn't, so add the /
            tempSongLocation = "/" + tempSongLocation;
        }

        // Now split the linkclicked into two song parts 0=folder 1=file
        songpart = tempSongLocation.split("/");

        // If the folder length isn't 0, it is a folder
        // Decide if it is a song, scripture or slide and get the actual file location
        if (songpart[0].length() > 0 && !songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
            FullscreenActivity.whichSongFolder = songpart[0];
            FullscreenActivity.dir = new File(
                    FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/" + songpart[0]);

        } else if (songpart[0].length() > 0 && songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
            FullscreenActivity.whichSongFolder = "../OpenSong Scripture/_cache";
            songpart[0] = "../OpenSong Scripture/_cache";

        } else if (songpart[0].length() > 0 && !songpart[0].contains("Scripture") && songpart[0].contains("Slide")) {
            FullscreenActivity.whichSongFolder = "../Slides/_cache";
            songpart[0] = "../Slides/_cache";

        } else {
            FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
            FullscreenActivity.dir = new File(
                    FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
        }
    }

    public void doProject(View v) {
        if (numdisplays>0) {
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
        File checkfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
        if ((FullscreenActivity.songfilename.contains(".pdf") || FullscreenActivity.songfilename.contains(".PDF")) && checkfile.exists()) {
            // File is pdf
            isPDF = true;
            presenter_slide_text.setVisibility(View.GONE);
            presenter_slide_image.setVisibility(View.VISIBLE);
        }

        if (!isPDF) {
            try {
                LoadXML.loadXML();
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
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
    }

    public class sectionButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Re-enable the disabled button
            if (currentsectionbutton!=null) {
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
            presenter_slide_text.setText(songSections[whichviewSongSection].trim());
            // Change the background colour of this button to show it is active
            Button newbutton = (Button) v;
            newbutton.setBackgroundDrawable(getResources().getDrawable(R.drawable.present_section_button_active));
            newbutton.setTextSize(10.0f);
            newbutton.setTextColor(0xff000000);
            newbutton.setPadding(10, 10, 10, 10);
            newbutton.setMinimumHeight(0);
            newbutton.setMinHeight(0);

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
            if (currentsetbutton!=null) {
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

            //tempSetButtonId = v.getId();
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
        }
    }

    public void popupSongDetails(View view) {
        // This is called when a user clicks on the Song details box
        // It opens a simple alert that lists the normal song details a presenter wants to see
        DialogFragment newFragment = PopUpSongDetailsFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void popupPresentationOrder(View view) {
        // This is called when a user clicks on the Edit presentation order button
        // It opens a simple alert that allows the user to edit the presentation order
        DialogFragment newFragment = PopUpPresentationOrderFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void popupBackgrounds(View view) {
        // This is called when a user clicks on the Backgrounds button
        // It opens a simple alert that allows the user to edit the backgrounds
        DialogFragment newFragment = PopUpBackgroundsFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void projectButtonClick(View view) {
        if (numdisplays>0 && !blankButton_isSelected) {
            projectButton_isSelected = true;

            presenter_project_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));

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
        if (numdisplays>0 && !blankButton_isSelected) {
            alertButton_isSelected = true;

            presenter_alert_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));

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

    public void layoutButtonClick(View view) {
        if (numdisplays>0 && !blankButton_isSelected) {
            layoutButton_isSelected = true;

            presenter_layout_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));

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
        if (numdisplays>0 && !blankButton_isSelected) {

            if (logoButton_isSelected) {
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
                song_on = "N";
                blackout = "N";
                logo_on = "Y";
                turnOffProjectButton();
                turnOffBlankButton();

                MyPresentation.fadeInLogo();
            }
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
        if (numdisplays>0) {
            if (blankButton_isSelected) {
                blankButton_isSelected = false;
                presenter_blank_group.setBackgroundDrawable(null);
            } else {
                blankButton_isSelected = true;
                presenter_blank_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
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
        presenter_displays_group.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_blue_active));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.presenter_actions, menu);
        this.menu = menu;
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
        //Now decide if the song being viewed has a song before it.
        //Otherwise disable the back button
        if (FullscreenActivity.indexSongInSet < 1) {
            set_back.setEnabled(false);
            set_back.getIcon().setAlpha(30);
        }
        //Now decide if the song being viewed has a song after it.
        //Otherwise disable the forward button
        if (FullscreenActivity.indexSongInSet >= (FullscreenActivity.setSize - 1)) {
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
/*
                if (mDrawerLayout.isDrawerOpen(expListViewSong)) {
                    mDrawerLayout.closeDrawer(expListViewSong);
                } else {
                    mDrawerLayout.openDrawer(expListViewSong);
                }
*/
                return true;

            case R.id.action_fullsearch:
/*
            // Need to fix this as the SearchViewFilterMode returns to FullscreenActivity
			Intent intent = new Intent();
			intent.setClass(PresenterMode.this,SearchViewFilterMode.class);
			startActivity(intent);
			finish();
*/
                return true;

            case R.id.action_settings:
/*
                if (mDrawerLayout.isDrawerOpen(expListViewOption)) {
                    mDrawerLayout.closeDrawer(expListViewOption);
                } else {
                    mDrawerLayout.openDrawer(expListViewOption);
                }
*/
                return true;

            case R.id.set_add:
/*
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

                // Save the set and other preferences
                Preferences.savePreferences();

                SetActions.prepareSetList();
                prepareOptionMenu();

                // Hide the menus - 1 second after opening the Option menu, close it
                // (1000ms total)
                Handler optionMenuFlickClosed = new Handler();
                optionMenuFlickClosed.postDelayed(new Runnable() {
                    @Override public void run() {
                        mDrawerLayout.closeDrawer(expListViewOption);
                    }
                }, 1000); // 1000ms delay
*/
                return true;


            case R.id.set_back:
/*
                FullscreenActivity.indexSongInSet -= 1;
                doMoveInSet();
                expListViewOption.setSelection(FullscreenActivity.indexSongInSet);
*/
                return true;

            case R.id.set_forward:
/*
                FullscreenActivity.indexSongInSet += 1;
                doMoveInSet();
                expListViewOption.setSelection(FullscreenActivity.indexSongInSet);
                return true;
*/
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {
        Log.d("debug", "onResume called");
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
        Log.d("debug", "onPause called");
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
    protected void onStop() {
        // Be sure to call the super class.
        Log.d("debug", "onStop called");
        numdisplays = presentationDisplays.length;
        if (numdisplays != 0) {
            for (Display display : presentationDisplays) {
                MyPresentation mPresentation = new MyPresentation(this, display);
                mPresentation.dismiss();
            }
        }
        super.onStop();
    }
}