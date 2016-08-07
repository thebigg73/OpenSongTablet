package com.garethevans.church.opensongtablet;

import android.Manifest;
//import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import android.webkit.WebSettings.RenderPriority;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StageMode extends AppCompatActivity implements PopUpAreYouSureFragment.MyInterface,
PopUpEditSongFragment.MyInterface, PopUpSongDetailsFragment.MyInterface, PopUpPresentationOrderFragment.MyInterface {

    // The toolbar and menu
    public Toolbar ab_toolbar;
    public ActionBar ab;
    public TextView songandauthor;
    Menu menu;

    // The left and right menus
    DrawerLayout mDrawerLayout;
    ExpandableListView expListViewSong;
    ExpandableListView expListViewOption;
    private ArrayList<String> listDataHeaderSong;
    private HashMap<String, List<String>> listDataChildSong = new HashMap<>();
    //private ArrayList<String> listDataHeaderOption;
    //private HashMap<String, List<String>> listDataChildOption = new HashMap<>();
    //protected int lastExpandedGroupPositionOption;

    // Type of file
    boolean isPDF = false;

    // Song sections
    static String[] songSections;
    static String[] songSectionsLabels;
    static String[] songSectionsTypes;
    static String[][] sectionContents;
    static String[][] sectionLineTypes;
    //static String[] songHTML;
    View[] sectionviews;
    boolean[] sectionrendered;

    // HTML and WebView
    //LinearLayout webframe;

    // Song sections view
    RelativeLayout mypage;
    ScrollView songscrollview;
    RelativeLayout testpane;
    int padding = 15;
    int[] viewwidth;
    int[] viewheight;

    // Dialogue fragments and stuff
    DialogFragment newFragment;

    // ASyncTask stuff
    AsyncTask<Object, Void, String> loadsong_async;
    //AsyncTask<Object, Void, String> preparesonghtml_async;
    AsyncTask<Object, Void, String> preparesongview_async;
    AsyncTask<Object, Void, String> preparesongmenu_async;

    // Set actions
    private boolean addingtoset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        setLocale();

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
        songscrollview = (ScrollView) findViewById(R.id.songscrollview);
        testpane = (RelativeLayout) findViewById(R.id.testpane);

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
        expListViewSong = (ExpandableListView) findViewById(R.id.song_list_ex);
        expListViewOption = (ExpandableListView) findViewById(R.id.option_list_ex);

        // Prepare the song menu
        prepareSongMenu();

        // Load the song and get started
        loadSong();
    }

    public void prepareSongMenu() {
        preparesongmenu_async = new PrepareSongMenu();
        preparesongmenu_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void loadSong() {
        // Load the song
        loadsong_async = new LoadSongAsync();
        loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stage_actions, menu);

        // Force overflow icon to show, even if hardware key is present
        try {
            ViewConfiguration config = ViewConfiguration.get(StageMode.this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        // Force icons to show in overflow menu
        if (ab != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                    Log.e("menu", "onMenuOpened", e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.perform_mode:
                // Switch to performance mode
                FullscreenActivity.whichMode = "Performance";
                Preferences.savePreferences();
                Intent performmode = new Intent();
                performmode.setClass(StageMode.this, FullscreenActivity.class);
                startActivity(performmode);
                finish();
                return true;
        }

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
        Log.d("d", "onResume() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        songscrollview.removeAllViews();
        Log.d("d", "onDestroy() called");
    }

    //@SuppressLint("SetJavaScriptEnabled")
/*
    public void setUpViews() {

        // Go through each section and create the WebViews
        //webframe.removeAllViews();
        for (String string : songHTML) {
            final WebView myView = new WebView(this);
            myView.setVisibility(View.INVISIBLE);
            myView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            myView.setBackgroundColor(0xff000000);
            myView.setWebViewClient(new WebViewClient());
            myView.getSettings().setAppCacheEnabled(false);
            myView.getSettings().setJavaScriptEnabled(true);
            myView.getSettings().setDomStorageEnabled(false);
            myView.getSettings().setDatabaseEnabled(false);
            myView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            myView.getSettings().setSupportZoom(false);
            //myView.setFocusable(true);
            myView.getSettings().setDefaultFontSize(18);
            //myView.setFocusableInTouchMode(true);
            //myView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            //myView.setScrollbarFadingEnabled(false);
            //myView.getSettings().setBuiltInZoomControls(true);
            //myView.getSettings().setDisplayZoomControls(false);
            //myView.getSettings().setLoadWithOverviewMode(true);
            //myView.getSettings().setUseWideViewPort(true);
            //myView.getSettings().setTextZoom(100);
            myView.getSettings().setJavaScriptEnabled(true);
            myView.getSettings().setSupportZoom( true );
            myView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR)
            //myView.setMinimumHeight(50);
            myView.loadDataWithBaseURL("file:///android_asset/", string, "text/html", "utf-8", null);


            //myView.setText(string);
            webframe.addView(myView);
            myView.setVisibility(View.VISIBLE);

            ImageView newLine = new ImageView(this);
            newLine.setImageResource(R.drawable.grey_line);
            newLine.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            webframe.addView(newLine);

        }

        TextView mytext = new TextView(this);
        mytext.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mytext.setText(songHTML[0]);
        mytext.setBackgroundColor(0xffffffff);
        mytext.setTextColor(0xff000000);
        mytext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSongTEST();
            }
        });
        webframe.addView(mytext);
    }
*/

    public void setSongAndAuthorTitle() {
        // If key is set
        String keytext = "";
        if (!FullscreenActivity.mKey.isEmpty() && !FullscreenActivity.mKey.equals("")) {
            keytext = " (" + FullscreenActivity.mKey + ")";
        }
        String text = FullscreenActivity.songfilename + keytext + "\n" + FullscreenActivity.mAuthor;
        songandauthor.setText(text);
    }

    public void checkStorage() {
        if (ActivityCompat.checkSelfPermission(StageMode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            finish();
        }
    }

    public void setLocale() {
        if (!FullscreenActivity.languageToLoad.isEmpty()) {
            Locale locale;
            locale = new Locale(FullscreenActivity.languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
    }

    public void resizeSection(View v, final int section) {

        int width = v.getWidth() + padding;
        int height = v.getHeight() + padding;

        // Decide the padding space
        // Padding on each side -> 2*padding
        // Thickness of the box on each side --> 2*2
        // Padding inside the box on each side --> 2*12
        int paddingspace = (2 * padding) + (2 * 4) + (2 * 12);
        int available_width = testpane.getWidth() - paddingspace;     // Remove the padding space
        int available_height = testpane.getHeight() - paddingspace;   // Remove the padding space
        float x_scale = ((float) available_width / (float) width);
        float y_scale = ((float) available_height / (float) height);
        float myscale = x_scale;
        if (x_scale >= y_scale) {
            myscale = y_scale;
        }

        // Decide on the max scale size, based on max font size
        float maxscale = FullscreenActivity.mMaxFontSize/16.0f;

        if (myscale>maxscale) {
            myscale = maxscale;
        }

        myscale = myscale - 0.05f;


        // Do the scaling
        int new_width = (int) (width * myscale);
        int new_height = (int) (height * myscale);
        v.setPivotX(0.5f);
        v.setPivotY(0.5f);
        v.setScaleX(myscale);
        v.setScaleY(myscale);

        v.requestLayout();

        sectionviews[section] = v;
        sectionrendered[section] = true;
        viewwidth[section] = new_width;
        viewheight[section] = new_height;

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
        for (boolean yesorno : sectionrendered) {
            if (!yesorno) {
                alldone = false;
            }
        }
        if (alldone) {
            renderthesongsections();
        }
    }

    public void renderthesongsections() {
        LinearLayout mysongsections = new LinearLayout(StageMode.this);
        mysongsections.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        mysongsections.setOrientation(LinearLayout.VERTICAL);

        for (int z = 0; z < sectionContents.length; z++) {
            int diff = viewheight[z] - sectionviews[z].getHeight();
            LinearLayout.LayoutParams section = new LinearLayout.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT-2*padding, ScrollView.LayoutParams.WRAP_CONTENT);
            sectionviews[z].requestLayout();
            // Decide on margins
            int side_margins = (int) ((songscrollview.getWidth() - viewwidth[z])/2.0f);
            int top_margin = padding;
            int bottom_margin = diff + padding;
            section.setMargins(side_margins,top_margin,side_margins,bottom_margin);
            sectionviews[z].setLayoutParams(section);
            sectionviews[z].setAlpha(0.5f);
            // Decide on the background color
            int colortouse = ProcessSong.getSectionColors(songSectionsTypes[z]);
            sectionviews[z].setBackgroundResource(R.drawable.section_box);
            GradientDrawable drawable = (GradientDrawable) sectionviews[z].getBackground();
            drawable.setColor(colortouse);
            mysongsections.addView(sectionviews[z]);
        }
        sectionviews[0].setAlpha(1.0f);
        songscrollview.addView(mysongsections);
        songscrollview.scrollTo(0,0);

        // Now scroll in the song via an animation
        if (FullscreenActivity.whichDirection.equals("L2R")) {
            songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
        } else {
            songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
        }
    }

    public void prepareView() {
        preparesongview_async = new PrepareSongView();
        preparesongview_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void prepareHTML() {
        //preparesonghtml_async = new PrepareSongHTML();
        //preparesonghtml_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void refreshAll() {
        // Show the toast if the message isn't blank
        if (!FullscreenActivity.myToastMessage.equals("")) {
            ShowToast.showToast(StageMode.this);
        }
        prepareSongMenu();
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

    private class LoadSongAsync extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {

            if (!isPDF) {
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
            FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

            FullscreenActivity.presenterChords = "Y";

            // Sort song formatting
            // 1. Sort multiline verse/chord formats
            FullscreenActivity.myLyrics = ProcessSong.fixMultiLineFormat(FullscreenActivity.mLyrics);

            // 2. Split the song into sections
            songSections = ProcessSong.splitSongIntoSections(FullscreenActivity.myLyrics);

            FullscreenActivity.usePresentationOrder = true;
            // 3. Put the song into presentation order if required
            if (FullscreenActivity.usePresentationOrder && !FullscreenActivity.mPresentation.equals("")) {
                songSections = ProcessSong.matchPresentationOrder(songSections);
            }

            // 4. Get the section headings/types (may have changed after presentationorder
            songSectionsLabels = new String[songSections.length];
            for (int sl=0; sl < songSections.length; sl++) {
                songSectionsLabels[sl] = ProcessSong.getSectionHeadings(songSections[sl]);
            }

            // We need to split each section into string arrays by line
            sectionContents = new String[songSections.length][];
            for (int x = 0; x < songSections.length; x++) {
                sectionContents[x] = songSections[x].split("\n");
            }

            // Determine what each line type is
            // Copy the array of sectionContents into sectionLineTypes
            // Then we'll replace the content with the line type
            // This keeps the array sizes the same simply
            sectionLineTypes = new String[sectionContents.length][];
            for (int x = 0; x < sectionLineTypes.length; x++) {
                sectionLineTypes[x] = new String[sectionContents[x].length];
                for (int y = 0; y < sectionLineTypes[x].length; y++) {
                    sectionLineTypes[x][y] = ProcessSong.determineLineTypes(sectionContents[x][y]);
                    if (sectionContents[x][y].length() > 0 && (sectionContents[x][y].indexOf(" ") == 0 ||
                            sectionContents[x][y].indexOf(".") == 0 || sectionContents[x][y].indexOf(";") == 0)) {
                        sectionContents[x][y] = sectionContents[x][y].substring(1);
                    }
                }
            }
            return "done";
        }

        protected void onPostExecute(String s) {
            // Any errors to show?
            if (!FullscreenActivity.myToastMessage.equals("")) {
                ShowToast.showToast(StageMode.this);
            }

            // Put the title of the song in the taskbar
            setSongAndAuthorTitle();

            // Prepare the HTML code for the secondary display
            prepareHTML();

            //Prepare the song views
            prepareView();
        }
    }

    @SuppressWarnings("deprecation")
    private class PrepareSongView extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            // Make sure the view is animated out
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
            } else {
                songscrollview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
            }

            // Remove any views already there
            songscrollview.removeAllViews();
        }

        @Override
        protected String doInBackground(Object... params) {
            // Set up the songviews
            songSectionsTypes = new String[songSections.length];
            sectionviews = new View[songSections.length];
            sectionrendered = new boolean[songSections.length];
            viewwidth = new int[songSections.length];
            viewheight = new int[songSections.length];

            return null;
        }

        protected void onPostExecute(String s) {

            for (int x = 0; x < songSections.length; x++) {
                LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                final LinearLayout ll = new LinearLayout(StageMode.this);
                ll.setLayoutParams(llparams);
                ll.setOrientation(LinearLayout.VERTICAL);

                // Add section title
                String[] returnvals = ProcessSong.beautifyHeadings(songSectionsLabels[x]);
                ll.addView(ProcessSong.titletoTextView(StageMode.this, returnvals[0]));

                // Identify the section type
                songSectionsTypes[x] = returnvals[1];
                int linenums = sectionContents[x].length;

                for (int y = 0; y < linenums; y++) {
                    String nextlinetype = "";
                    String previouslinetype = "";
                    if (y < linenums - 1) {
                        nextlinetype = sectionLineTypes[x][y + 1];
                    }
                    if (y>0) {
                        previouslinetype = sectionLineTypes[x][y - 1];
                    }
                    String[] positions_returned;
                    String[] chords_returned;
                    String[] lyrics_returned;
                    TableLayout tl = new TableLayout(StageMode.this);

                    switch (ProcessSong.howToProcessLines(y, linenums, sectionLineTypes[x][y], nextlinetype, previouslinetype)) {

                        // If this is a chord line followed by a lyric line.
                        case "chord_then_lyric":
                            if (sectionContents[x][y].length()>sectionContents[x][y+1].length()) {
                                sectionContents[x][y+1] = ProcessSong.fixLineLength(sectionContents[x][y+1],sectionContents[x][y].length());
                            }
                            positions_returned = ProcessSong.getChordPositions(sectionContents[x][y]);
                            chords_returned = ProcessSong.getChordSections(sectionContents[x][y], positions_returned);
                            lyrics_returned = ProcessSong.getLyricSections(sectionContents[x][y + 1], positions_returned);
                            tl.addView(ProcessSong.chordlinetoTableRow(StageMode.this, chords_returned));
                            tl.addView(ProcessSong.lyriclinetoTableRow(StageMode.this, lyrics_returned));
                            ll.addView(tl);
                            break;

                        case "chord_only":
                            chords_returned = new String[1];
                            chords_returned[0] = sectionContents[x][y];
                            tl.addView(ProcessSong.chordlinetoTableRow(StageMode.this, chords_returned));
                            ll.addView(tl);
                            break;

                        case "lyric_no_chord":
                            lyrics_returned = new String[1];
                            lyrics_returned[0] = sectionContents[x][y];
                            tl.addView(ProcessSong.lyriclinetoTableRow(StageMode.this, lyrics_returned));
                            tl.setBackgroundColor(FullscreenActivity.lyricsCommentColor);
                            ll.addView(tl);
                            break;

                        case "comment_no_chord":
                            lyrics_returned = new String[1];
                            lyrics_returned[0] = sectionContents[x][y];
                            tl.addView(ProcessSong.commentlinetoTableRow(StageMode.this, lyrics_returned));
                            ll.addView(tl);
                            break;

                    }
                }
                testpane.addView(ll);
                final int val = x;
                ViewTreeObserver vto = ll.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            ll.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        resizeSection(ll, val);
                    }
                });
            }
        }
    }
/*

    private class PrepareSongHTML extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            // Go through each line and create the HTML
            // This gets stored in an array
            songHTML = new String[songSections.length];
            for (int x = 0; x < songSections.length; x++) {
                String mHTML = "<div class=\"heading\">" + ProcessSong.beautifyHeadings(songSectionsLabels[x]).toString() + "</div>\n";
                for (int y = 0; y < sectionContents[x].length; y++) {
                    // If this is a chord line followed by a lyric line.
                    if (y < sectionLineTypes[x].length - 1 && sectionLineTypes[x][y].equals("chord") && (sectionLineTypes[x][y + 1].equals("lyric") || sectionLineTypes[x][y + 1].equals("comment"))) {
                        // Check the length of the lyric line at least matches the chord line length
                        if (sectionContents[x][y].length() > sectionContents[x][y + 1].length()) {
                            sectionContents[x][y + 1] = ProcessSong.fixLineLength(sectionContents[x][y + 1], sectionContents[x][y].length());
                        }
                        String[] positions_returned = ProcessSong.getChordPositions(sectionContents[x][y]);
                        String[] chords_returned = ProcessSong.getChordSections(sectionContents[x][y], positions_returned);
                        String[] lyrics_returned = ProcessSong.getLyricSections(sectionContents[x][y + 1], positions_returned);
                        mHTML += "<table class=\"lyrictable\">\n";
                        mHTML += "<tr>" + ProcessSong.chordlinetoHTML(chords_returned) + "</tr>\n";
                        mHTML += "<tr>" + ProcessSong.lyriclinetoHTML(lyrics_returned) + "</tr>\n";
                        mHTML += "</table>\n";
                    }
                }
                songHTML[x] = ProcessSong.songHTML(mHTML);
            }
            return songHTML[0];
        }

        protected void onPostExecute(String s) {
            setUpViews();
        }
    }

*/
    private class PrepareSongMenu extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            // Initialise Songs menu
            listDataHeaderSong = new ArrayList<>();
            listDataHeaderSong.add(getResources().getString(R.string.mainfoldername));
        }

        @Override
        protected String doInBackground(Object... params) {
            // Get song folders
            ListSongFiles.listSongFolders();
            listDataHeaderSong.addAll(Arrays.asList(FullscreenActivity.mSongFolderNames).subList(0, FullscreenActivity.mSongFolderNames.length - 1));
            for (int s = 0; s < FullscreenActivity.mSongFolderNames.length; s++) {
                List<String> song_folders = new ArrayList<>();
                Collections.addAll(song_folders, FullscreenActivity.childSongs[s]);
                listDataChildSong.put(listDataHeaderSong.get(s), song_folders);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            ExpandableListAdapter listAdapterSong = new ExpandableListAdapter(expListViewSong, StageMode.this, listDataHeaderSong, listDataChildSong);
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
                        ShowToast.showToast(StageMode.this);

                        // Save the set and other preferences
                        Preferences.savePreferences();

                        mDrawerLayout.closeDrawer(expListViewSong);
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

                        if (FullscreenActivity.setView.equals("Y") && FullscreenActivity.setSize > 0) {
                            // Get the name of the song to look for (including folders if need be)
                            SetActions.getSongForSetWork();

                            if (FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                                // Song is in current set.  Find the song position in the current set and load it (and next/prev)
                                // The first song has an index of 6 (the 7th item as the rest are menu items)

                                FullscreenActivity.previousSongInSet = "";
                                FullscreenActivity.nextSongInSet = "";
                                SetActions.prepareSetList();
                                //setupSetButtons();

                            } else {
                                // Song isn't in the set, so just show the song
                                // Switch off the set view (buttons in action bar)
                                FullscreenActivity.setView = "N";
                                // Re-enable the disabled button

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

                        loadSong();
                    } else {
                        addingtoset = false;
                    }
                    return false;
                }
            });
        }
    }

    public void selectSection(int whichone) {
        // Set this sections alpha to 1.0f;
        sectionviews[whichone].setAlpha(1.0f);

        // Smooth scroll to show this view at the top of the page
        songscrollview.smoothScrollTo(0,sectionviews[whichone].getTop());

        // Go through each of the views and set the alpha of the others to 0.5f;
        for (int x=0; x<sectionviews.length; x++) {
            if (x!=whichone) {
                sectionviews[x].setAlpha(0.5f);
            }
        }
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
        }
    }
}