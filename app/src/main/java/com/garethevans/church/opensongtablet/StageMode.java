package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.Collator;
import java.util.Collections;

public class StageMode extends AppCompatActivity implements PopUpAreYouSureFragment.MyInterface,
        PopUpEditSongFragment.MyInterface, PopUpSongDetailsFragment.MyInterface,
        PopUpPresentationOrderFragment.MyInterface, PopUpListSetsFragment.MyInterface,
        SongMenuListeners.MyInterface, OptionMenuListeners.MyInterface, MenuHandlers.MyInterface,
        SetActions.MyInterface, PopUpFullSearchFragment.MyInterface, IndexSongs.MyInterface,
        SearchView.OnQueryTextListener, PopUpSetViewNew.MyInterface,
        PopUpChooseFolderFragment.MyInterface, PopUpCustomSlideFragment.MyInterface,
        PopUpOptionMenuSet.MyInterface, PopUpOptionMenuSong.MyInterface,
        PopUpLanguageFragment.MyInterface {

    // The toolbar and menu
    public Toolbar ab_toolbar;
    public ActionBar ab;
    public TextView songandauthor;
    Menu menu;

    // The left and right menu
    DrawerLayout mDrawerLayout;
    //ExpandableListView expListViewSong;
    TextView menuFolder_TextView;
    LinearLayout songmenu;
    LinearLayout optionmenu;
    LinearLayout changefolder_LinearLayout;
    ListView song_list_view;
    //ExpandableListView expListViewOption;

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
    float biggestscale_1col = 1000.0f;
    float biggestscale_2col = 1000.0f;
    float biggestscale_3col = 1000.0f;
    int coltouse = 1;

    // Dialogue fragments and stuff
    DialogFragment newFragment;

    // ASyncTask stuff
    AsyncTask<Object, Void, String> loadsong_async;
    AsyncTask<Object, Void, String> preparesongview_async;
    AsyncTask<Object, Void, String> preparesongmenu_async;
    AsyncTask<Void, Void, String>[] resizesection_async;

    // Allow the menus to flash open to show where they are on first run
    boolean firstrun_option = true;
    boolean firstrun_song   = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("d","Stage Mode started");
        // Check storage is valid
        checkStorage();
        System.gc();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Load up the user preferences
        Preferences.loadPreferences();

        // Try language locale change
        SetLocale.setLocale(StageMode.this);

        // Load the layout and set the title
        setContentView(R.layout.stage_mode);

        // Set up the toolbar

        ab_toolbar = (Toolbar) findViewById(R.id.mytoolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(ab_toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call
        ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(false); // show or hide the default home button
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
            ab.setDisplayShowTitleEnabled(false);
        }

        // Identify the views being used
        songandauthor = (TextView) findViewById(R.id.songandauthor);
        mypage = (RelativeLayout) findViewById(R.id.mypage);
        mypage.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
        mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TESTING //
                if (FullscreenActivity.whichMode.equals("Stage")) {
                    FullscreenActivity.whichMode = "Performance";
                } else if (FullscreenActivity.whichMode.equals("Performance")){
                    FullscreenActivity.whichMode = "Stage";
                }
                loadSong();
            }
        });

        // The first song will be in the centre scrollview
        // The left and right are used for left/right scroling
        // We have to then move the views around

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
        //expListViewSong = (ExpandableListView) findViewById(R.id.song_list_ex);
        //expListViewOption = (ExpandableListView) findViewById(R.id.option_list_ex);
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

        // Load the song and get started
        loadSong();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stage_actions, menu);

        // Force overflow icon to show, even if hardware key is present
        MenuHandlers.forceOverFlow(StageMode.this,ab,menu);

        // If we are not in set mode, then hide the forward and back icons
        MenuItem set_back = null;
        MenuItem set_forward = null;
        MenuItem presentationMode = null;
        MenuItem stageMode = null;
        if (menu != null) {
            set_back = menu.findItem(R.id.set_back);
            set_forward = menu.findItem(R.id.set_forward);
            presentationMode = menu.findItem(R.id.present_mode);
            stageMode = menu.findItem(R.id.stage_mode);
        }

        // Decide if song is in the set
        SetActions.isSongInSet();

        if (set_back!=null) {
            set_back.setVisible(MenuHandlers.setSetButtonVisibility());
            set_back.setEnabled(MenuHandlers.setBackEnabled());
            set_back.getIcon().setAlpha(MenuHandlers.setBackAlpha());
        }

        if (set_forward!=null) {
            set_forward.setVisible(MenuHandlers.setSetButtonVisibility());
            set_forward.setEnabled(MenuHandlers.setForwardEnabled());
            set_forward.getIcon().setAlpha(MenuHandlers.setForwardAlpha());
        }

        if (presentationMode!=null) {
            presentationMode.setVisible(FullscreenActivity.dualDisplayCapable);
            presentationMode.getIcon().setAlpha(MenuHandlers.dualScreenAlpha());
        }

        if (stageMode!=null) {
            stageMode.setVisible(FullscreenActivity.dualDisplayCapable);
            stageMode.getIcon().setAlpha(MenuHandlers.dualScreenAlpha());
        }
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
        Log.d("d", "onResume() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        songscrollview.removeAllViews();
        Log.d("d", "onDestroy() called");
    }

    public void checkStorage() {
        if (ActivityCompat.checkSelfPermission(StageMode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            finish();
        }
    }

    @Override
    public void shareSong() {
        if (!FullscreenActivity.isSong) {
            // Editing a slide / note / scripture / image
            FullscreenActivity.myToastMessage = getResources().getString(R.string.not_allowed);
            ShowToast.showToast(StageMode.this);
        } else {
            // Export
            // Take a screenshot as a bitmap
            songscrollview.destroyDrawingCache();
            songscrollview.setDrawingCacheEnabled(true);
            FullscreenActivity.bmScreen = null;
            FullscreenActivity.bmScreen = songscrollview.getDrawingCache();

            // Send this off to be processed and sent via an intent
            Intent emailIntent = ExportPreparer.exportSong(StageMode.this,FullscreenActivity.bmScreen);
            startActivityForResult(Intent.createChooser(emailIntent, FullscreenActivity.exportcurrentsong), 12345);
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
        prepareSongMenu();
        prepareOptionMenu();
        loadSong();
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
        DrawerTweaks.openMyDrawers(mDrawerLayout,songmenu,optionmenu,which);
    }

    @Override
    public void closeMyDrawers(String which) {
        DrawerTweaks.closeMyDrawers(mDrawerLayout,songmenu,optionmenu,which);
    }

    @Override
    public void findSongInFolders() {


    }

    public void resizeDrawers() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels/2;
        songmenu.setLayoutParams(DrawerTweaks.resizeMenu(songmenu,width));
        optionmenu.setLayoutParams(DrawerTweaks.resizeMenu(optionmenu,width));
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
        // Get the next set positions and song
        SetActions.doMoveInSet(StageMode.this);

        // Set indexSongInSet position has moved
        invalidateOptionsMenu();
    }

    @Override
    public void refreshActionBar() {
        invalidateOptionsMenu();
    }

    @Override
    public void loadSong() {
        // Load the song
        if (loadsong_async!=null) {
            loadsong_async.cancel(true);
        }
        loadsong_async = new LoadSongAsync();
        loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void indexingDone() {
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
            FullscreenActivity.searchFileName.add(d, songbits[0].trim());
            FullscreenActivity.searchFolder.add(d, songbits[1].trim());
            FullscreenActivity.searchTitle.add(d, songbits[2].trim());
            FullscreenActivity.searchAuthor.add(d, songbits[3].trim());
            FullscreenActivity.searchShortLyrics.add(d, songbits[4].trim());
            FullscreenActivity.searchTheme.add(d, songbits[5].trim());
            FullscreenActivity.searchKey.add(d, songbits[6].trim());
            FullscreenActivity.searchHymnNumber.add(d,songbits[7].trim());
        }

        song_list_view.setTextFilterEnabled(true);
        song_list_view.setFastScrollEnabled(true);
        //setupSearchView();

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
        //song_list_view.setAdapter(FullscreenActivity.sva);
        song_list_view.setTextFilterEnabled(true);
        song_list_view.setFastScrollEnabled(true);
        //setupSearchView();

        //mSearchView.setOnQueryTextListener(this);
        Log.d("d","search_database="+FullscreenActivity.search_database);
        FullscreenActivity.sva.notifyDataSetChanged();
        song_list_view.setAdapter(FullscreenActivity.sva);

    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        //InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        //song_list_view.requestFocus();

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
        // Add the slide
        CustomSlide.addCustomSlide();

        // Tell the user that the song has been added.
        FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.customslide_title + "\" " + getResources().getString(R.string.addedtoset);
        ShowToast.showToast(StageMode.this);

        // Vibrate to let the user know something happened
        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vb.vibrate(200);

        invalidateOptionsMenu();
        prepareOptionMenu();
        closeMyDrawers("option_delayed");
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

            // Get the current song index
            ListSongFiles.getCurrentSongIndex();

            FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

            FullscreenActivity.presenterChords = "Y";

            // Sort song formatting
            // 1. Sort multiline verse/chord formats
            FullscreenActivity.myLyrics = ProcessSong.fixMultiLineFormat(FullscreenActivity.mLyrics);

            // 2. Split the song into sections
            FullscreenActivity.songSections = ProcessSong.splitSongIntoSections(FullscreenActivity.myLyrics);

            FullscreenActivity.usePresentationOrder = true;
            // 3. Put the song into presentation order if required
            if (FullscreenActivity.usePresentationOrder && !FullscreenActivity.mPresentation.equals("")) {
                FullscreenActivity.songSections = ProcessSong.matchPresentationOrder(FullscreenActivity.songSections);
            }

            // 4. Get the section headings/types (may have changed after presentationorder
            FullscreenActivity.songSectionsLabels = new String[FullscreenActivity.songSections.length];
            for (int sl=0; sl < FullscreenActivity.songSections.length; sl++) {
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
                    FullscreenActivity.sectionLineTypes[x][y] = ProcessSong.determineLineTypes(FullscreenActivity.sectionContents[x][y]);
                    if (FullscreenActivity.sectionContents[x][y].length() > 0 && (FullscreenActivity.sectionContents[x][y].indexOf(" ") == 0 ||
                            FullscreenActivity.sectionContents[x][y].indexOf(".") == 0 || FullscreenActivity.sectionContents[x][y].indexOf(";") == 0)) {
                        FullscreenActivity.sectionContents[x][y] = FullscreenActivity.sectionContents[x][y].substring(1);
                    }
                }
            }

            // Look for song split points
            ProcessSong.lookForSplitPoints();

            return "done";
        }

        protected void onPostExecute(String s) {
            // Any errors to show?
            if (!FullscreenActivity.myToastMessage.equals("")) {
                ShowToast.showToast(StageMode.this);
            }

            // Put the title of the song in the taskbar
            songandauthor.setText(ProcessSong.getSongAndAuthor());

            //Prepare the song views
            prepareView();
        }
    }

    public void prepareView() {
        preparesongview_async = new PrepareSongView();
        preparesongview_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    @SuppressWarnings("deprecation")
    private class PrepareSongView extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            biggestscale_1col = 1000.0f;
            biggestscale_2col = 1000.0f;
            biggestscale_3col = 1000.0f;

            // Make sure the view is animated out
            if (FullscreenActivity.whichDirection.equals("L2R")) {
/*
                for (int i=0;i<songscrollview.getChildCount();i++) {
                    songscrollview.getChildAt(i).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
                }
*/
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
            } else {
/*
                for (int i=0;i<songscrollview.getChildCount();i++) {
                    songscrollview.getChildAt(i).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
                }
*/
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
            }

            FullscreenActivity.currentSection = 0;
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

            column1_1 = ProcessSong.createLinearLayout(StageMode.this);
            column1_2 = ProcessSong.createLinearLayout(StageMode.this);
            column2_2 = ProcessSong.createLinearLayout(StageMode.this);
            column1_3 = ProcessSong.createLinearLayout(StageMode.this);
            column2_3 = ProcessSong.createLinearLayout(StageMode.this);
            column3_3 = ProcessSong.createLinearLayout(StageMode.this);

            int currentsectionstartline = 0;
            // Go through each section
            for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                LinearLayout section1_1;
                LinearLayout section1_2 = new LinearLayout(StageMode.this);
                LinearLayout section2_2 = new LinearLayout(StageMode.this);
                LinearLayout section1_3 = new LinearLayout(StageMode.this);
                LinearLayout section2_3 = new LinearLayout(StageMode.this);
                LinearLayout section3_3 = new LinearLayout(StageMode.this);

                // The single stage mode or 1 column performance mode view
                final LinearLayout section = ProcessSong.songSectionView(StageMode.this, x);
                // The other views for 2 or 3 column mode
                section1_1 = ProcessSong.songSectionView(StageMode.this, x);
                if (currentsectionstartline < FullscreenActivity.splitpoint) {
                    section1_2 = ProcessSong.songSectionView(StageMode.this, x);
                } else {
                    section2_2 = ProcessSong.songSectionView(StageMode.this, x);
                }
                if (currentsectionstartline < FullscreenActivity.thirdsplitpoint) {
                    section1_3 = ProcessSong.songSectionView(StageMode.this, x);
                } else if (currentsectionstartline >= FullscreenActivity.thirdsplitpoint && x < FullscreenActivity.twothirdsplitpoint) {
                    section2_3 = ProcessSong.songSectionView(StageMode.this, x);
                } else {
                    section3_3 = ProcessSong.songSectionView(StageMode.this, x);
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
                } else {
                    section1_1.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    section1_1.addView(ProcessSong.createTextView(StageMode.this,
                            " ",6.0f*FullscreenActivity.commentfontscalesize,FullscreenActivity.lyricsTextColor,
                            FullscreenActivity.lyricsfont));
                    column1_1.addView(section1_1);
                    if (currentsectionstartline < FullscreenActivity.splitpoint) {
                        section1_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        section1_2.addView(ProcessSong.createTextView(StageMode.this,
                                " ",6.0f*FullscreenActivity.commentfontscalesize,FullscreenActivity.lyricsTextColor,
                                FullscreenActivity.lyricsfont));
                        column1_2.addView(section1_2);
                    } else {
                        section2_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        section2_2.addView(ProcessSong.createTextView(StageMode.this,
                                " ",6.0f*FullscreenActivity.commentfontscalesize,FullscreenActivity.lyricsTextColor,
                                FullscreenActivity.lyricsfont));
                        column2_2.addView(section2_2);
                    }
                    if (currentsectionstartline < FullscreenActivity.thirdsplitpoint) {
                        section1_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        section1_3.addView(ProcessSong.createTextView(StageMode.this,
                                " ",6.0f*FullscreenActivity.commentfontscalesize,FullscreenActivity.lyricsTextColor,
                                FullscreenActivity.lyricsfont));
                        column1_3.addView(section1_3);
                    } else if (currentsectionstartline >= FullscreenActivity.thirdsplitpoint && x < FullscreenActivity.twothirdsplitpoint) {
                        section2_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        section2_3.addView(ProcessSong.createTextView(StageMode.this,
                                " ",6.0f*FullscreenActivity.commentfontscalesize,FullscreenActivity.lyricsTextColor,
                                FullscreenActivity.lyricsfont));
                        column2_3.addView(section2_3);
                    } else {
                        section3_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        section3_3.addView(ProcessSong.createTextView(StageMode.this,
                                " ",6.0f*FullscreenActivity.commentfontscalesize,FullscreenActivity.lyricsTextColor,
                                FullscreenActivity.lyricsfont));
                        column3_3.addView(section3_3);
                    }
                    currentsectionstartline += (FullscreenActivity.songSections[x].split("\n").length);
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

                // Collapse the song sections back into 1
                ProcessSong.collapseSections();

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

    public void resizeSection(View v, final int section) {

        int width = v.getWidth() + FullscreenActivity.padding;
        int height = v.getHeight() + FullscreenActivity.padding;

        // Decide on the max scale size, based on max font size
        float maxscale = FullscreenActivity.mMaxFontSize / 6.0f;

        // Scaling depends on which mode we are in
        // For stage mode, each item is scaled individually
        // For performance mode, we need to scale each possible column and then decide on the best view

        if (FullscreenActivity.whichMode.equals("Stage")) {
            // Decide the padding space
            // Padding on each side -> 2*padding
            // Thickness of the box on each side --> 2*2
            // Padding inside the box on each side --> 2*12
            int paddingspace = (2 * FullscreenActivity.padding) + (2 * 2) + (2 * 12);
            int available_width = testpane.getWidth() - paddingspace;     // Remove the padding space
            int available_height = (int) (0.70f * testpane.getHeight()) - paddingspace;   // Remove the padding space
            float x_scale = ((float) available_width / (float) width);
            float y_scale = ((float) available_height / (float) height);
            float myscale = x_scale;
            if (x_scale >= y_scale) {
                myscale = y_scale;
            }

            // Decide on the max scale size, based on max font size
            if (myscale > maxscale) {
                myscale = maxscale;
            }

            // Do the scaling
            int new_width = (int) (width * myscale);
            int new_height = (int) (height * myscale);
            v.setPivotX(0.5f);
            v.setPivotY(0.5f);
            v.setScaleX(myscale);
            v.setScaleY(myscale);

            FullscreenActivity.sectionviews[section] = v;
            FullscreenActivity.sectionrendered[section] = true;
            FullscreenActivity.viewwidth[section] = new_width;
            FullscreenActivity.viewheight[section] = new_height;
            FullscreenActivity.sectionScaleValue[section] = myscale;

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectSection(section);
                }
            });

            // Remove the view from the test pane
            ((RelativeLayout) v.getParent()).removeView(v);

            // Check if all the views are rendered.
            // If so, add them to the song section view
            boolean alldone = true;
            for (boolean yesorno : FullscreenActivity.sectionrendered) {
                if (!yesorno) {
                    alldone = false;
                }
            }
            if (alldone) {
                renderthesongsections();
            }

        } else {
            // We are in performance mode
            // Decide on the available widths for 1,2 or 3 column views
            // Decide the padding space
            // Padding on each side -> 2*padding
            // Thickness of the box on each side --> 2*2
            // Padding inside the box on each side --> 2*12
            int paddingspace = (2 * FullscreenActivity.padding) + (2 * 2) + (2 * 12);
            int available_width_1 = testpane.getWidth() - paddingspace;                // Remove the padding space
            int available_width_2 = (int) ((testpane.getWidth() - paddingspace) / 2.0f); // Remove the padding space
            int available_width_3 = (int) ((testpane.getWidth() - paddingspace) / 3.0f); // Remove the padding space
            int available_height = testpane.getHeight() - paddingspace;              // Remove the padding space
            float x_scale = 1;
            float y_scale = 1;
            float myscale;
            switch (section) {
                case 0:
                    // Single column view
                    x_scale = ((float) available_width_1 / (float) width);
                    y_scale = ((float) available_height / (float) height);
                    break;

                case 1:
                case 2:
                    // Two column view
                    x_scale = ((float) available_width_2 / (float) width);
                    y_scale = ((float) available_height / (float) height);
                    break;

                case 3:
                case 4:
                case 5:
                    // Three column view
                    x_scale = ((float) available_width_3 / (float) width);
                    y_scale = ((float) available_height / (float) height);
                    break;

            }

            // Use the smallest of the two scale values so we can fit everything in
            myscale = x_scale;
            if (x_scale >= y_scale) {
                myscale = y_scale;
            }
            // Decide on the max scale size, based on max font size
            if (myscale > maxscale) {
                myscale = maxscale;
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
            FullscreenActivity.sectionviews[section] = v;
            FullscreenActivity.sectionrendered[section] = true;
            FullscreenActivity.sectionScaleValue[section] = myscale;

            // Check to see if we've done all we need to do
            boolean alldone = true;
            for (int r = 0; r < FullscreenActivity.sectionrendered.length; r++) {
                if (!FullscreenActivity.sectionrendered[r]) {
                    alldone = false;
                }
            }

            if (alldone) {
                // Go through each section and scale to the biggest size
                // The best view is the one with the biggest scale size
                float scaletouse = biggestscale_1col;
                coltouse = 1;
                if (biggestscale_2col>scaletouse) {
                    scaletouse = biggestscale_2col;
                    coltouse = 2;
                }
                if (biggestscale_3col>scaletouse) {
                    scaletouse = biggestscale_3col;
                    coltouse = 3;
                }

                for (int r = 0; r < FullscreenActivity.sectionrendered.length; r++) {
                    // Do the scaling
                    int new_width = (int) (width * scaletouse);
                    int new_height = (int) (height * scaletouse);
                    FullscreenActivity.viewwidth[r] = new_width;
                    FullscreenActivity.viewheight[r] = new_height;

                    if (FullscreenActivity.sectionviews[r]!=null) {
                        FullscreenActivity.sectionviews[r].setPivotX(0.5f);
                        FullscreenActivity.sectionviews[r].setPivotY(0.5f);
                        FullscreenActivity.sectionviews[r].setScaleX(scaletouse);
                        FullscreenActivity.sectionviews[r].setScaleY(scaletouse);

                        // Remove the view from the test pane
                        ((RelativeLayout) FullscreenActivity.sectionviews[r].getParent()).removeView(FullscreenActivity.sectionviews[r]);

                    }


                }
                renderthesongsections();
            }
        }
    }

    public void renderthesongsections() {
        LinearLayout mysongsections = new LinearLayout(StageMode.this);
        mysongsections.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        mysongsections.setOrientation(LinearLayout.VERTICAL);
        songscrollview.removeAllViews();

        if (FullscreenActivity.whichMode.equals("Stage")) {
            for (int z = 0; z < FullscreenActivity.sectionContents.length; z++) {
                int diff = FullscreenActivity.viewheight[z] - FullscreenActivity.sectionviews[z].getHeight();
                LinearLayout.LayoutParams section = new LinearLayout.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT - 2 * FullscreenActivity.padding, ScrollView.LayoutParams.WRAP_CONTENT);

                // Decide on margins
                int side_margins = (int) ((songscrollview.getWidth() - FullscreenActivity.viewwidth[z] -
                        (FullscreenActivity.sectionScaleValue[z] * 8)) / 2.0f);
                if (side_margins < 0) {
                    side_margins = 0;
                }
                int top_margin = (int) (FullscreenActivity.sectionScaleValue[z] * 4);
                int bottom_margin = diff + (int) (FullscreenActivity.sectionScaleValue[z] * 8);
                section.setMargins(side_margins, top_margin, side_margins, bottom_margin);
                FullscreenActivity.sectionviews[z].setLayoutParams(section);
                FullscreenActivity.sectionviews[z].setAlpha(0.5f);
                // Decide on the background color
                if (FullscreenActivity.whichMode.equals("Stage")) {
                    int colortouse = ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[z]);
                    FullscreenActivity.sectionviews[z].setBackgroundResource(R.drawable.section_box);
                    GradientDrawable drawable = (GradientDrawable) FullscreenActivity.sectionviews[z].getBackground();
                    drawable.setColor(colortouse);
                }
                mysongsections.addView(FullscreenActivity.sectionviews[z]);
            }
            FullscreenActivity.sectionviews[0].setAlpha(1.0f);

        } else {
            // In Performance Mode, so decide which view to create
            LinearLayout columns = new LinearLayout(StageMode.this);
            LinearLayout.LayoutParams col_llp = new LinearLayout.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT);
            columns.setLayoutParams(col_llp);

            switch (coltouse) {
                case 1:
                    LinearLayout.LayoutParams section = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

                    // Decide on margins
                    int top_margin = 0;
                    int bottom_margin = songscrollview.getHeight();
                    int side_margins = FullscreenActivity.padding;
                    section.setMargins(side_margins, top_margin, side_margins, bottom_margin);
                    FullscreenActivity.sectionviews[0].setLayoutParams(section);
                    FullscreenActivity.sectionviews[0].setAlpha(1.0f);
                    // Decide on the background color
   /*                 if (FullscreenActivity.whichMode.equals("Stage")) {
                        int colortouse = ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[0]);
                        FullscreenActivity.sectionviews[0].setBackgroundResource(R.drawable.section_box);
                        GradientDrawable drawable = (GradientDrawable) FullscreenActivity.sectionviews[0].getBackground();
                        drawable.setColor(colortouse);
                    }

*/
                    columns.addView(FullscreenActivity.sectionviews[0]);

                    break;
            }
            mysongsections.addView(columns);
        }

        songscrollview.addView(mysongsections);
        songscrollview.scrollTo(0,0);

        // Now scroll in the song via an animation
        if (FullscreenActivity.whichDirection.equals("L2R")) {
/*
            for (int i=0;i<songscrollview.getChildCount();i++) {
                //songscrollview.getChildAt(i).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
            }
*/
            songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
        } else {
/*
            for (int i=0;i<songscrollview.getChildCount();i++) {
                //songscrollview.getChildAt(i).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
            }
*/
            songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
        }
    }

/*
    public void resizeSection(LinearLayout v, final int section) {
        resizesection_async[section] = new ResizeSongSections(v,testpane,section);
        resizesection_async[section].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
*/

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

        public ResizeSongSections(LinearLayout v, RelativeLayout test, int section){
            this.v = v;
            this.test = test;
            this.section = section;
        }

        @Override
        protected void onPreExecute() {
            width = v.getWidth() + FullscreenActivity.padding;
            height = v.getHeight() + FullscreenActivity.padding;
            // Decide the padding space
            // Padding on each side -> 2*padding
            // Thickness of the box on each side --> 2*2
            // Padding inside the box on each side --> 2*12
            paddingspace = (2 * FullscreenActivity.padding) + (2 * 2) + (2 * 12);
            available_width = test.getWidth();     // Remove the padding space
            available_height = test.getHeight();   // Remove the padding space
        }

        @Override
        protected String doInBackground(Void... voids) {

            // Scaling depends on which mode we are in
            // For stage mode, each item is scaled individually
            // For performance mode, we need to scale each possible column and then decide on the best view

            if (FullscreenActivity.whichMode.equals("Stage")) {
                int paddingspace = (2 * FullscreenActivity.padding) + (2 * 2) + (2 * 12);
                available_width = available_width - paddingspace;                     // Remove the padding space
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
                int new_width = (int) (width * myscale);
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
                available_width_1 = available_width - paddingspace;                  // Remove the padding space
                available_width_2 = (int) ((available_width - paddingspace) / 2.0f); // Remove the padding space
                available_width_3 = (int) ((available_width - paddingspace) / 3.0f); // Remove the padding space
                available_height = available_height - paddingspace;                  // Remove the padding space
                float x_scale;
                float y_scale = ((float) available_height / (float) height);
                switch (section) {
                    case 0:
                    default:
                        // Single column view
                        x_scale = ((float) available_width_1 / (float) width);
                        break;

                    case 1:
                    case 2:
                        // Two column view
                        x_scale = ((float) available_width_2 / (float) width);
                        break;

                    case 3:
                    case 4:
                    case 5:
                        // Three column view
                        x_scale = ((float) available_width_3 / (float) width);
                        break;
                }

                // Use the smallest of the two scale values so we can fit everything in
                myscale = x_scale;
                if (x_scale >= y_scale) {
                    myscale = y_scale;
                }
                // Decide on the max scale size, based on max font size
                if (myscale > maxscale) {
                    myscale = maxscale;
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

            v.setPivotX(0.5f);
            v.setPivotY(0.5f);
            v.setScaleX(myscale);
            v.setScaleY(myscale);

            FullscreenActivity.sectionviews[section] = v;

            if (FullscreenActivity.whichMode.equals("Stage")) {
                // Stage mode with separate sections
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectSection(section);
                    }
                });

                // Remove the view from the test pane
                ((RelativeLayout) v.getParent()).removeView(v);

                // Check if all the views are rendered.
                // If so, add them to the song section view
                boolean alldone = true;
                for (boolean yesorno : FullscreenActivity.sectionrendered) {
                    if (!yesorno) {
                        alldone = false;
                    }
                }
                if (alldone) {
                    renderthesongsections();
                }

            } else {
                // Performance mode with up to 3 columns

                // Remove the view from the test pane
                ((RelativeLayout) v.getParent()).removeView(v);
                // Check to see if we've done all we need to do
                boolean alldone = true;
                for (int r = 0; r < 6; r++) {
                    if (FullscreenActivity.sectionrendered.length<6 || !FullscreenActivity.sectionrendered[r]) {
                        alldone = false;
                    }
                }

                if (alldone) {
                    // Go through each section and scale to the biggest size
                    // The best view is the one with the biggest scale size
                    float scaletouse = biggestscale_1col;
                    coltouse = 1;
                    if (biggestscale_2col>scaletouse) {
                        scaletouse = biggestscale_2col;
                        coltouse = 2;
                    }
                    if (biggestscale_3col>scaletouse) {
                        scaletouse = biggestscale_3col;
                        coltouse = 3;
                    }

                    for (int r = 0; r < FullscreenActivity.sectionrendered.length; r++) {
                        // Do the scaling
                        int new_width = (int) (width * scaletouse);
                        int new_height = (int) (height * scaletouse);
                        FullscreenActivity.viewwidth[r] = new_width;
                        FullscreenActivity.viewheight[r] = new_height;

                    }


                    renderthesongsections();
                }
            }
        }
    }

    @Override
    public void prepareSongMenu() {
        if (preparesongmenu_async!=null) {
            preparesongmenu_async.cancel(true);
        }
        preparesongmenu_async = new PrepareSongMenu();
        preparesongmenu_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            // Set the ListView to show the songs
            ArrayAdapter<String> lva = new SongMenuAdapter(StageMode.this, FullscreenActivity.mSongFileNames);
            song_list_view.setAdapter(lva);

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
    public void songLongClick(int mychild) {
        /*// Rebuild the options and menu to update the set items
        SetActions.prepareSetList();
        FullscreenActivity.whichOptionMenu = "SET";
        optionmenu.invalidate();
        prepareOptionMenu();
        invalidateOptionsMenu();
        openMyDrawers("option");
        closeMyDrawers("option_delayed");*/

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
        // Let the user know something is happening
        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vb.vibrate(200);

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
    public void prepareOptionMenu() {
        optionmenu = (LinearLayout) findViewById(R.id.optionmenu);
        optionmenu.removeAllViews();
        Log.d("d","optionmenu="+optionmenu);
        if (optionmenu!=null) {
            optionmenu.addView(OptionMenuListeners.prepareOptionMenu(StageMode.this));
            OptionMenuListeners.optionListeners(optionmenu, StageMode.this);
        }
        if (firstrun_option) {
            openMyDrawers("option");
            closeMyDrawers("option_delayed");
            firstrun_option = false;
        }
    }

    @Override
    public void showActionBar() {
        if (ab != null) {
            ab.show();
        }
    }

    @Override
    public void hideActionBar() {
        if (ab != null) {
            ab.hide();
        }
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
                newFragment = PopUpDisplayOptionsFragment.newInstance();
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

            case "extra":
                newFragment = PopUpExtraInfoFragment.newInstance();
                break;

            case "profiles":
                newFragment = PopUpProfileFragment.newInstance();
                break;

            case "footpedal":
                newFragment = PopUpPedalsFragment.newInstance();
                break;

            case "gestures":
                newFragment = PopUpGesturesFragment.newInstance();
                break;

            case "newfolder":
                message = getString(R.string.newfolder);
                newFragment = PopUpSongFolderCreateFragment.newInstance(message);
                break;

            case "editfoldername":
                message = getString(R.string.options_song_editfolder);
                newFragment = PopUpSongFolderRenameFragment.newInstance(message);
                break;

            case "managestorage":
                newFragment = PopUpStorageFragment.newInstance();
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

        }

        FullscreenActivity.whattodo = "";

        if (newFragment!=null) {
            newFragment.show(getFragmentManager(), message);
        } else {
            FullscreenActivity.myToastMessage = "Fragment not found!";
            ShowToast.showToast(StageMode.this);
        }
    }

    public void selectSection(int whichone) {
        FullscreenActivity.currentSection = whichone;

        // Set this sections alpha to 1.0f;
        FullscreenActivity.sectionviews[whichone].setAlpha(1.0f);

        Log.d("d","section="+whichone);
        // Smooth scroll to show this view at the top of the page
        songscrollview.smoothScrollTo(0,FullscreenActivity.sectionviews[whichone].getTop());

        // Go through each of the views and set the alpha of the others to 0.5f;
        for (int x=0; x<FullscreenActivity.sectionviews.length; x++) {
            if (x!=whichone) {
                FullscreenActivity.sectionviews[x].setAlpha(0.5f);
            }
        }
        FullscreenActivity.tempswipeSet = "enable";
        FullscreenActivity.setMoveDirection = "";
        invalidateOptionsMenu();

        // TESTING //
        if (FullscreenActivity.whichMode.equals("Stage") && whichone>0) {
            FullscreenActivity.whichMode = "Performance";
            loadSong();
        } else if (FullscreenActivity.whichMode.equals("Performance")){
            FullscreenActivity.whichMode = "Stage";
            loadSong();
        }
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
                Intent viewsong = new Intent(this, FullscreenActivity.class);
                FullscreenActivity.whichMode = "Performance";
                Preferences.savePreferences();
                viewsong.setClass(StageMode.this, FullscreenActivity.class);
                startActivity(viewsong);
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

        }
    }
}