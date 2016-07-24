package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public class StageMode extends AppCompatActivity implements PopUpAreYouSureFragment.MyInterface {

    // The toolbar and menu
    public Toolbar toolbar;
    public ActionBar ab;
    public TextView songandauthor;
    Menu menu;

    // Type of file
    boolean isPDF = false;

    // Song sections
    static String[] songSections;
    static String[] songSectionsLabels;
    static String[][] sectionContents;
    static String[][] sectionLineTypes;
    static String[] songHTML;

    // HTML and WebView
    String mHTML;
    WebView mainwebview;

    // Dialogue fragments and stuff
    DialogFragment newFragment;

    // ASyncTask stuff
    AsyncTask<Object, Void, String> loadsong_async;
    AsyncTask<Object, Void, String> preparesonghtml_async;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check storage is valid
        checkStorage();
        System.gc();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Load up the user preferences
        //FullscreenActivity.myPreferences = getPreferences(MODE_PRIVATE);
        Preferences.loadPreferences();

        // Try language locale change
        setLocale();

        // Load the layout and set the title
        setContentView(R.layout.stage_mode);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        invalidateOptionsMenu();

        // Identify the views being used
        songandauthor = (TextView) findViewById(R.id.songandauthor);
        mainwebview = (WebView) findViewById(R.id.mainwebview);

        // Load the song and get started
        loadsong_async = new LoadSongAsync();
        loadsong_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        getMenuInflater().inflate(R.menu.stage_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stage_actions, menu);
        this.menu = menu;

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
        if (ab != null && menu != null) {
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

    public void setUpViews() {

    }

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

    public void prepareHTML() {
        preparesonghtml_async = new PrepareSongHTML();
        preparesonghtml_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            // Sort song formatting
            LyricsDisplay.parseLyrics();

            // Split into sections (also fixes multiverses)
            FullscreenActivity.presenterChords = "Y";
            PresentPrepareSong.splitSongIntoSections("stage");

            Log.d("d", "mLyrics=" + FullscreenActivity.mLyrics);

            // We need to split each section into string arrays by line
            sectionContents = new String[songSections.length][];
            for (int x = 0; x < songSections.length; x++) {
                sectionContents[x] = songSections[x].split("\n");
            }

            // Determine what each line type is
            sectionLineTypes = new String[songSections.length][];
            for (int x = 0; x < songSections.length; x++) {
                sectionLineTypes[x] = new String[sectionContents.length];
                for (int w = 0; w < sectionLineTypes[x].length; w++) {
                    sectionLineTypes[x][w] = ProcessSong.determineLineTypes(sectionContents[x][w]);
                    if (sectionContents[x][w].length()>1 && (sectionContents[x][w].indexOf(" ")==0 ||
                            sectionContents[x][w].indexOf(".")==0 || sectionContents[x][w].indexOf(";")==0)) {
                        sectionContents[x][w] = sectionContents[x][w].substring(1);
                    }

                }
            }

            // Should now have the full song in different sections
            // Each section has a string array of stuff line by line
            // Each section line is matched to type

            return "done";
        }

        protected void onPostExecute(String s) {
            // Put the title of the song in the taskbar
            setSongAndAuthorTitle();

            // Prepare the HTML code
            prepareHTML();
        }
    }

    private class PrepareSongHTML extends AsyncTask<Object, Void, String> {

/*
        @Override
        protected void onPreExecute() {

        }
*/

        @Override
        protected String doInBackground(Object... params) {

            // Go through each line and create the HTML
            // Just do this for first section just now
            mHTML = "<html>\n<head>\n<style>\n";
            mHTML += ".page       {background-color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsBackgroundColor)) + ";}\n";
            mHTML += ".heading    {color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; text-decoration:underline}\n";
            mHTML += ".lyric      {color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
            mHTML += ".lyrictable {border-spacing:0; border-collapse: collapse; border:0px;}\n";



            mHTML += ".chord   {color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + ";}\n";
            mHTML += "</style>\n</head>\n";
            mHTML += "<body class=\"page\">\n";
            mHTML += "<div class=\"heading\">" + songSectionsLabels[0] + "</div>\n<table class=\"lyrictable\">";

            for (int x = 0; x < sectionLineTypes[0].length; x++) {
                // If this is a chord line followed by a lyric line..
                if (x < sectionLineTypes[0].length - 1 && sectionLineTypes[0][x].equals("chord") && (sectionLineTypes[0][x + 1].equals("lyric") || sectionLineTypes[0][x + 1].equals("comment"))) {
                    String[] positions_returned = ProcessSong.getChordPositions(sectionContents[0][x]);
                    String[] chords_returned = ProcessSong.getChordSections(sectionContents[0][x], positions_returned);
                    String[] lyrics_returned = ProcessSong.getLyricSections(sectionContents[0][x + 1], positions_returned);
                    mHTML += "\n<tr>" + ProcessSong.chordlinetoHTML(chords_returned) + "</tr>\n";
                    mHTML += "\n<tr>" + ProcessSong.lyriclinetoHTML(lyrics_returned) + "</tr>";
                }
            }

            mHTML += "</table>\n</body>\n</html>";
            return mHTML;
        }

        protected void onPostExecute(String s) {
            Log.d("d", s);
            mainwebview.loadData(s, "text/html", "utf-8");
            setUpViews();
        }
    }

    private class XXXSOMETHINGXXX extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Object... params) {

            return "done";
        }

        protected void onPostExecute(String s) {

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