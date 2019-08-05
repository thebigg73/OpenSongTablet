// This file runs before everything else.  It checks for storage permissions and current app version
// It only shows if the storage location has an issue or we have updated the app, or user returned manually
package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lib.folderpicker.FolderPicker;

import static com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.make;

public class BootUpCheck extends AppCompatActivity {

    // Declare helper classes:
    Preferences preferences;
    StorageAccess storageAccess;
    IndexSongs indexSongs;
    SongXML songXML;
    ChordProConvert chordProConvert;
    OnSongConvert onSongConvert;
    UsrConvert usrConvert;
    TextSongConvert textSongConvert;
    SetTypeFace setTypeFace;

    // Declare views
    //ProgressBar progressBar;
    TextView progressText, version, previousStorageTextView, previousStorageLocationsTextView, previousStorageHeading, currentAction;
    Button chooseStorageButton, goToSongsButton, userGuideButton, previousStorageButton, resetCacheButton;
    LinearLayout storageLinearLayout, readUpdate, userGuideLinearLayout, goToSongsLinearLayout;
    Spinner appMode, previousStorageSpinner;
    Toolbar toolbar;
    // Declare variables
    String text="", versionCode="", whichMode;
    Uri uriTree, uriTreeHome;
    boolean foldersok, storageGranted, skiptoapp, changed;
    int lastUsedVersion, thisVersion;
    ArrayList<String> locations;
    File folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the helper classes (preferences)
        preferences = new Preferences();
        storageAccess = new StorageAccess();
        indexSongs = new IndexSongs();
        //fullscreenActivity = new FullscreenActivity();
        songXML = new SongXML();
        chordProConvert = new ChordProConvert();
        onSongConvert = new OnSongConvert();
        usrConvert = new UsrConvert();
        textSongConvert = new TextSongConvert();
        setTypeFace = new SetTypeFace();

        // Initialise the font
        setTypeFace.setUpAppFonts(BootUpCheck.this,preferences,new Handler(),new Handler(), new Handler(),new Handler(), new Handler(),new Handler());
        // This will do one of 2 things - it will either show the splash screen or the welcome screen
        // To determine which one, we need to check the storage is set and is valid
        // The last version used must be the same or greater than the current app version

        // Load up all of the preferences and the user specified storage location if it exists
        checkPreferencesForStorage();

        showCurrentStorage(uriTreeHome);

        StaticVariables.whichSongFolder = preferences.getMyPreferenceString(BootUpCheck.this, "whichSongFolder",
                getString(R.string.mainfoldername));
        StaticVariables.songfilename = preferences.getMyPreferenceString(BootUpCheck.this, "songfilename",
                "Welcome to OpenSongApp");

        // Check if we have used the app already, but the last song didn't load
        if (!preferences.getMyPreferenceBoolean(BootUpCheck.this,"songLoadSuccess",false)) {
            StaticVariables.whichSongFolder = "";
            StaticVariables.songfilename = "Welcome to OpenSongApp";
        } else {
            StaticVariables.whichSongFolder = preferences.getMyPreferenceString(BootUpCheck.this, "whichSongFolder",
                    getString(R.string.mainfoldername));
        }

        // If whichSongFolder is empty, reset to main
        if (StaticVariables.whichSongFolder == null || StaticVariables.whichSongFolder.isEmpty()) {
            StaticVariables.whichSongFolder = getString(R.string.mainfoldername);
            preferences.setMyPreferenceString(BootUpCheck.this,"whichSongFolder",StaticVariables.whichSongFolder);
        }

        // Check we have the required storage permission
        checkStoragePermission();

        // Determine the last used version and what version the app is now
        skiptoapp = versionCheck();

        if (checkStorageIsValid() && storageGranted && skiptoapp) {
            setContentView(R.layout.activity_logosplash);
            goToSongs();

        } else {
            setContentView(R.layout.boot_up_check);

            // Identify the views
            identifyViews();

            // Update the verion and storage
            showCurrentStorage(uriTreeHome);
            version.setText(versionCode);

            // Set up the button actions
            setButtonActions();

            // Check our state of play (based on if location is set and valid)
            checkReadiness();
        }

    }

    void identifyViews() {
        //progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        goToSongsButton = findViewById(R.id.goToSongsButton);
        chooseStorageButton = findViewById(R.id.chooseStorageButton);
        storageLinearLayout = findViewById(R.id.storageLinearLayout);
        goToSongsLinearLayout = findViewById(R.id.goToSongsLinearLayout);
        readUpdate = findViewById(R.id.readUpdate);
        version = findViewById(R.id.version);
        version.setText(versionCode);
        userGuideLinearLayout = findViewById(R.id.userGuideLinearLayout);
        userGuideButton = findViewById(R.id.userGuideButton);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        appMode = findViewById(R.id.appMode);
        previousStorageSpinner = findViewById(R.id.previousStorageSpinner);
        previousStorageButton = findViewById(R.id.previousStorageButton);
        previousStorageTextView = findViewById(R.id.previousStorageTextView);
        previousStorageLocationsTextView = findViewById(R.id.previousStorageLocationsTextView);
        previousStorageHeading = findViewById(R.id.previousStorageHeading);
        resetCacheButton = findViewById(R.id.resetCacheButton);
        currentAction = findViewById(R.id.currentAction);
        // Set the 3 options
        ArrayList<String> appModes = new ArrayList<>();
        appModes.add(getString(R.string.performancemode));
        appModes.add(getString(R.string.stagemode));
        appModes.add(getString(R.string.presentermode));
        ArrayAdapter<String> aa = new ArrayAdapter<>(BootUpCheck.this,R.layout.my_spinner,appModes);
        appMode.setAdapter(aa);
        if (whichMode==null) {
            whichMode = preferences.getMyPreferenceString(BootUpCheck.this, "whichMode", "Performance");
        }
        // Select the appropriate one
        switch (whichMode) {
            case "Stage":
                appMode.setSelection(1);
                break;
            case "Presentation":
                appMode.setSelection(2);
                break;
            case "Performance":
            default:
                appMode.setSelection(0);
                break;
        }
    }
    void setButtonActions() {
        showLoadingBar();
        goToSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (appMode.getSelectedItemPosition()) {
                    case 0:
                    default:
                        whichMode = "Performance";
                        preferences.setMyPreferenceString(BootUpCheck.this,"whichMode",whichMode);
                        break;

                    case 1:
                        whichMode = "Stage";
                        preferences.setMyPreferenceString(BootUpCheck.this,"whichMode",whichMode);
                        break;

                    case 2:
                        whichMode = "Presentation";
                        preferences.setMyPreferenceString(BootUpCheck.this,"whichMode",whichMode);
                        break;
                }
                goToSongs();
            }
        });
        chooseStorageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                chooseStorageLocation();
            }
        });
        readUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.opensongapp.com/latest-updates";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(i);
                } catch (Exception e) {
                    Log.d("BootUpCheck", "Error showing activity");
                }
            }
        });
        userGuideLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.opensongapp.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(i);
                } catch (Exception e) {
                    Log.d("BootUpCheck", "Error showing activity");
                }
            }
        });
        userGuideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.opensongapp.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(i);
                } catch (Exception e) {
                    Log.d("BootUpCheck", "Error showing activity");
                }
            }
        });
        previousStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
            }
        });
        resetCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTheCaches();
            }
        });
    }
    void pulseStartButton() {
        CustomAnimations ca = new CustomAnimations();
        ca.pulse(BootUpCheck.this, goToSongsButton);
    }
    void pulseStorageButton() {
        CustomAnimations ca2 = new CustomAnimations();
        ca2.pulse(BootUpCheck.this, chooseStorageButton);
    }
    void showLoadingBar() {
        pulseStartButton();
        readUpdate.setClickable(true);
        storageLinearLayout.setClickable(true);
        goToSongsButton.setClickable(true);
        appMode.setClickable(true);
        goToSongsButton.setVisibility(View.VISIBLE);
        appMode.setVisibility(View.VISIBLE);
        userGuideLinearLayout.setClickable(true);
        userGuideButton.setClickable(true);
    }
    void checkPreferencesForStorage() {
        String uT  = preferences.getMyPreferenceString(BootUpCheck.this,"uriTree","");
        String uTH = preferences.getMyPreferenceString(BootUpCheck.this,"uriTreeHome","");
        if (!uT.equals("")) {
            uriTree = Uri.parse(uT);
        } else {
            uriTree = null;
        }
        if (!uTH.equals("")) {
            uriTreeHome = Uri.parse(uTH);
        } else {
            uriTreeHome = null;
        }
        if (uriTree!=null && uriTreeHome==null) {
            uriTreeHome = storageAccess.homeFolder(BootUpCheck.this,uriTree,preferences);
        }
    }
    void checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permission has not been granted.
            storageGranted = false;
            requestStoragePermission();
        } else {
            storageGranted = true;
        }
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                make(findViewById(R.id.page), R.string.storage_rationale,
                        LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(BootUpCheck.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    }
                }).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // Storage permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(BootUpCheck.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            storageGranted = grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        checkReadiness();
    }

    void showCurrentStorage(Uri u) {
        // This tries to make the uri more user readable!
        if (u!=null) {
            if (storageAccess.lollipopOrLater()) {
                try {
                    List<String> bits = u.getPathSegments();
                    StringBuilder sb = new StringBuilder();
                    for (String b : bits) {
                        sb.append("/");
                        sb.append(b);
                    }
                    text = sb.toString();
                    if (!text.endsWith(storageAccess.appFolder)) {
                        text += "/" + storageAccess.appFolder;
                    }
                    text = text.replace("tree", "/");
                    text = text.replace(":", "/");
                    while (text.contains("//")) {
                        text = text.replace("//", "/");
                    }

                    // Finally, the storage location is likely something like /9016-4EF8/OpenSong/document/9016-4EF8/OpenSong
                    // This is due to the content using a document contract
                    // Strip this back to the bit after document
                    if (text.contains("OpenSong/document/")) {
                        text = text.substring(text.lastIndexOf("OpenSong/document/")+18);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    text = "" + uriTreeHome;
                }
            } else {
                text = u.getPath();
            }

        } else {
            text = getString(R.string.notset);
        }

        if (progressText!=null) {
            // We aren't just passing through, so we can set the text
            progressText.setText(text);
        }
    }
    @SuppressLint("InlinedApi")
    void chooseStorageLocation() {
        if (storageGranted) {
            Intent intent;
            if (storageAccess.lollipopOrLater()) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                intent.putExtra("android.content.extra.FANCY", true);
                intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
                intent.putExtra("android.content.extra.INITIAL_URI", uriTree);
                startActivityForResult(intent, 42);
            } else {
                openFragment();
            }

        } else {
            requestStoragePermission();
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode==7789 && resultData!=null && resultData.getExtras()!=null) {
                kitKatDealWithUri(resultData);
            } else {
                lollipopDealWithUri(resultData);
            }

            // Save the location uriTree and uriTreeHome
            saveUriLocation();

            // Update the storage text
            showCurrentStorage(uriTreeHome);

            // See if we can show the start button yet
            checkReadiness();
        }
    }

    void kitKatDealWithUri(Intent resultData) {
        String folderLocation;
        if (resultData!=null && resultData.getExtras()!=null) {
            // This is for Android KitKat - deprecated file method
            folderLocation = resultData.getExtras().getString("data");
        } else {
            File f = getExternalFilesDir("OpenSong");
            if (f!=null) {
                folderLocation = f.toString();
            } else {
                folderLocation = null;
            }
        }
        if (folderLocation!=null) {
            uriTree = Uri.parse(folderLocation);
            uriTreeHome = storageAccess.homeFolder(BootUpCheck.this,uriTree,preferences);

            // If we can write to this all is good, if not, tell the user (likely to be SD card)
            if (!storageAccess.canWrite(BootUpCheck.this, uriTree)) {
                notWriteable();
            }
        } else {
            notWriteable();
        }
    }
    void lollipopDealWithUri(Intent resultData) {
        // This is the newer version for Lollipop+ This is preferred!
        if (resultData!=null) {
            uriTree = resultData.getData();
        } else {
            uriTree = null;
        }
        if (uriTree!=null) {
            getContentResolver().takePersistableUriPermission(uriTree,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        uriTreeHome = storageAccess.homeFolder(BootUpCheck.this,uriTree,preferences);
        if (uriTree==null || uriTreeHome==null) {
            notWriteable();
        }
    }

    void notWriteable() {
        uriTree = null;
        uriTreeHome = null;
        ShowToast showToast = new ShowToast();
        showToast.showToastMessage(BootUpCheck.this, getString(R.string.storage_notwritable));
        if (locations != null && locations.size() > 0) {
            // Revert back to the blank selection as the one chosen can't be used
            previousStorageSpinner.setSelection(0);
        }
    }
    void saveUriLocation() {
        if (uriTree!=null) {
            // Save the preferences
            preferences.setMyPreferenceString(BootUpCheck.this, "uriTree", uriTree.toString());
            preferences.setMyPreferenceString(BootUpCheck.this, "uriTreeHome", uriTreeHome.toString());
        } else {
            preferences.setMyPreferenceString(BootUpCheck.this, "uriTree", "");
            preferences.setMyPreferenceString(BootUpCheck.this, "uriTreeHome", "");
        }
    }

    boolean checkStorageIsValid() {
        // Check that the location exists and is writeable
        // Since the OpenSong folder may not yet exist, we check for the uriTree and if it is writeable
        try {
            if (uriTree != null) {
                DocumentFile df = storageAccess.documentFileFromRootUri(BootUpCheck.this, uriTree, uriTree.getPath());
                if (df==null || !df.canWrite()) {
                    progressText.setText(R.string.notset);
                }
                return df != null && df.canWrite();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    boolean versionCheck() {
        // Do this as a separate thread.  0 is for fresh installs.  1 is for user choice to return to menu
        lastUsedVersion = preferences.getMyPreferenceInt(BootUpCheck.this, "lastUsedVersion", 0);
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                thisVersion = (int) pInfo.getLongVersionCode();
                versionCode = "V."+pInfo.versionName;
            } else {
                //noinspection
                thisVersion = pInfo.versionCode;
                versionCode = "V."+pInfo.versionName;
            }
        } catch (Exception e) {
            thisVersion = 0;
            versionCode = "";
        }

        // If this is a fresh install (or updating from before V4.8.5) clear the cache in case

        return lastUsedVersion >= thisVersion;
    }

    void clearTheCaches() {
        // Clear the user preferences
        File cacheDirectory = getCacheDir();
        File applicationDirectory;
        if (cacheDirectory!=null && cacheDirectory.getParent()!=null) {
            applicationDirectory = new File(cacheDirectory.getParent());
        } else {
            applicationDirectory = cacheDirectory;
        }
        if (applicationDirectory!=null && applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            if (fileNames!=null) {
                for (String fileName : fileNames) {
                    if (!fileName.equals("lib")) {
                        File ftodel = new File(applicationDirectory,fileName);
                        doDeleteFile(ftodel);
                    }
                }
            }
        }
        try {
            PreferenceManager.getDefaultSharedPreferences(BootUpCheck.this).edit().clear().apply();
        } catch (Exception e) {
            Log.d("d","Error clearing new preferences");
        }

        // Clear the old preferences (that will eventually get phased out!)
        try {
            BootUpCheck.this.getSharedPreferences("OpenSongApp", Context.MODE_PRIVATE).edit().clear().apply();
        } catch (Exception e) {
            Log.d("d","Error clearing old preferences");
            e.printStackTrace();
        }


        // Clear the cache and data folder
        try {
            File dir = BootUpCheck.this.getCacheDir();
            doDeleteCacheFile(dir);

            // Set the last used version to 1 (otherwise we get stuck in a loop!)
            preferences.setMyPreferenceInt(BootUpCheck.this,"lastUsedVersion",1);
            // Now restart the BootUp activity
            BootUpCheck.this.recreate();

        } catch (Exception e) {
            Log.d("d","Error clearing the cache directory");
            e.printStackTrace();
        }

        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            Log.d("BootUpCheck","Clearing data");
            if (am!=null) {
                am.clearApplicationUserData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean doDeleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                if (children!=null) {
                    for (String child : children) {
                        deletedAll = doDeleteFile(new File(file, child)) && deletedAll;
                    }
                }
            } else {
                deletedAll = file.delete();
            }
        }
        return deletedAll;
    }

    public boolean doDeleteCacheFile(File file) {

        if (file != null && file.isDirectory()) {
            String[] children = file.list();
            if (children!=null) {
                for (String child : children) {
                    boolean success = doDeleteCacheFile(new File(file, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return file.delete();
        } else if(file!= null && file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }

    void checkReadiness() {
        if (skiptoapp) {
            try {
                goToSongsLinearLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (checkStorageIsValid() && storageGranted && !skiptoapp) {
            // We're good to go, but need to wait for the user to click on the start button
            goToSongsLinearLayout.setVisibility(View.VISIBLE);
            chooseStorageButton.clearAnimation();

        } else {
            // Not ready, so hide the start button
            goToSongsLinearLayout.setVisibility(View.GONE);
            // Show the storage as a pulsing button
            pulseStorageButton();

        }
    }

    void goToSongs() {
        // Intialise the views needed
        currentAction = findViewById(R.id.currentAction);
        currentAction.setVisibility(View.VISIBLE);

        // Do this as a separate thread
        GoToSongs goToSongsAsync = new GoToSongs();
        goToSongsAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class GoToSongs extends AsyncTask<Object, String, String> {

        int numSongs;
        String message;
        boolean cancelled = false;
        Intent intent;

        @Override
        protected void onPreExecute() {
            // Set the preferred storage - it must be good, otherwise we couldn't click the button!
            preferences.setMyPreferenceString(BootUpCheck.this,"uriTree",uriTree.toString());
            preferences.setMyPreferenceString(BootUpCheck.this,"uriTreeHome",uriTreeHome.toString());
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Check if the folders exist, if not, create them
            message = getString(R.string.storage_check);
            publishProgress("setmessage");
            final String progress = storageAccess.createOrCheckRootFolders(BootUpCheck.this, uriTree, preferences);
            foldersok = !progress.contains("Error");

            if (foldersok) {
                // Load up all of the preferences into FullscreenActivity (static variables)
                message = getString(R.string.load_preferences);
                publishProgress("setmessage");

                // Search for the user's songs
                message = getString(R.string.initialisesongs_start).replace("-", "").trim();
                publishProgress("setmessage");
                ArrayList<String> songIds = new ArrayList<>();
                try {
                    songIds = storageAccess.listSongs(BootUpCheck.this, preferences);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Show how many songs have been found and display this to the user
                // This will remain as until the current folder is build
                numSongs = songIds.size();
                message = numSongs + " " + getString(R.string.processing) + "\n" + getString(R.string.wait);
                publishProgress("setmessage");
                // Write a crude text file (line separated) with the song Ids (folder/file)
                storageAccess.writeSongIDFile(BootUpCheck.this, preferences, songIds);

                // Try to create the basic database
                SQLiteHelper sqLiteHelper = new SQLiteHelper(BootUpCheck.this);
                sqLiteHelper.resetDatabase(BootUpCheck.this);

                // Add entries to the database that have songid, folder and filename fields
                // This is the minimum that we need for the song menu.
                // It can be upgraded asynchronously in StageMode/PresenterMode to include author/key
                // Also will later include all the stuff for the search index as well
                sqLiteHelper.insertFast(BootUpCheck.this, storageAccess);

                // Finished indexing
                message = getString(R.string.success);
                publishProgress("setmessage");

                // Decide on where we are going and set the intent to launch it
                intent = new Intent();

                if (whichMode==null) {
                    whichMode = "Performance";
                }

                switch (whichMode) {
                    case "Performance":
                    case "Stage":
                    default:
                        intent.setClass(BootUpCheck.this, StageMode.class);
                        break;

                    case "Presentation":
                        intent.setClass(BootUpCheck.this, PresenterMode.class);
                        break;
                }

            } else {
                // There was a problem with the folders, so restart the app!
                Log.d("BootUpCheck", "problem with folders");
                BootUpCheck.this.recreate();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... string) {
           /* if (settingProgressBarUp) {
                settingProgressBarUp = false;
                hProgressBar.setVisibility(View.VISIBLE);
                hProgressBar.setMax(numSongs);
            }
            if (currentSongNum > 0) {
                hProgressBar.setProgress(currentSongNum);
            }*/
            //currentAction.setVisibility(View.VISIBLE);
            currentAction.setText(message);
        }

        @Override
        protected void onPostExecute(String s) {
            if (!cancelled) {
                try {
                    // Now save the appropriate variables and then start the intent
                    // Set the current version
                    preferences.setMyPreferenceInt(BootUpCheck.this, "lastUsedVersion", thisVersion);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Show the progressBar if we were on the BootUpCheck screen
                /*if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }*/

                startActivity(intent);
                finish();

            } else {
                showLoadingBar();
            }
        }
    }

    public void startSearch() {
        // Deactivate the stuff we shouldn't click on while it is being prepared
        setEnabledOrDisabled(false);

        // Initialise the available storage locations
        locations = new ArrayList<>();

        FindLocations findlocations = new FindLocations();
        findlocations.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void setEnabledOrDisabled(boolean what) {
        goToSongsButton.setEnabled(what);
        chooseStorageButton.setEnabled(what);
        readUpdate.setEnabled(what);
        userGuideLinearLayout.setEnabled(what);
        previousStorageButton.setEnabled(what);
        previousStorageSpinner.setEnabled(what);
        if (!what) {
            //progressBar.setVisibility(View.VISIBLE);
            previousStorageTextView.setVisibility(View.VISIBLE);
        } else {
            //progressBar.setVisibility(View.GONE);
            previousStorageTextView.setVisibility(View.GONE);
        }
    }
    public void openFragment() {
        Intent intent = new Intent(this, FolderPicker.class);
        intent.putExtra("title", getString(R.string.changestorage));
        intent.putExtra("pickFiles", false);
        if (uriTree!=null) {
            intent.putExtra("location", uriTree.getPath());
        }
        startActivityForResult(intent, 7789);
    }

    public void walkFiles(File root) {
        if (root!=null && root.exists() && root.isDirectory()) {
            File[] list = root.listFiles();
            if (list != null) {
                for (File f : list) {
                    if (f.isDirectory()) {
                        String where = f.getAbsolutePath();
                        if (where.endsWith("/OpenSong/Songs") && !where.contains(".estrongs") && !where.contains("com.ttxapps")) {
                            // Found one and it isn't in eStrongs recycle folder or the dropsync temp files!
                            where = where.substring(0, where.length() - 15);
                            locations.add(where);
                        }
                        folder = f;
                        displayWhere(where);
                        walkFiles(f);
                    }
                }
            }
        }
    }

    public void displayWhere(String msg) {
        final String str = msg;
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    previousStorageTextView.setText(str);
                }
            });
    }

    @SuppressLint("StaticFieldLeak")
    private class FindLocations extends AsyncTask<Object, String, String> {

        String s;

        @Override
        protected String doInBackground(Object... objects) {
            // Go through the directories recursively and add them to an arraylist
            folder = new File("/storage");
            walkFiles(folder);

            folder = Environment.getExternalStorageDirectory();
            walkFiles(folder);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            // Set up the file list, as long as the user wasn't bored and closed the window!
            if (locations!=null) {
                // Hide the  and reenable stuff
                setEnabledOrDisabled(true);

                if (locations.size()<1) {
                    // No previous installations found
                    previousStorageTextView.setText(getString(R.string.nofound));
                    previousStorageTextView.setVisibility(View.VISIBLE);
                    //previousStorageSpinner.setVisibility(View.GONE);
                    previousStorageHeading.setVisibility(View.GONE);
                } else {
                    // Listen for the clicks!
                    previousStorageHeading.setVisibility(View.VISIBLE);
                    // Add the locations to the textview
                    StringBuilder sb = new StringBuilder();
                    for (String str:locations) {
                        if (!sb.toString().contains(str+" ")) {
                            sb.append(str).append(" \n");
                        }
                    }
                    previousStorageTextView.setVisibility(View.GONE);
                    previousStorageLocationsTextView.setText(sb.toString().trim());
                    locations.add(0,"");
                    previousStorageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (changed) {
                                if (position>0) {
                                    File f = new File(locations.get(position));
                                    uriTree = Uri.fromFile(f);
                                    chooseStorageButton.performClick();
                                }
                            } else {
                                changed=true;
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) { }
                    });
                    ArrayAdapter<String> listAdapter = new ArrayAdapter<>(BootUpCheck.this, R.layout.my_spinner, locations);
                    previousStorageSpinner.setAdapter(listAdapter);
                    previousStorageSpinner.setVisibility(View.GONE);
                }
            }
        }
    }

}