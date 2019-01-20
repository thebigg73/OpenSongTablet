// This file uses the Storage Access Framework to allow users to specify their storage location
// For KitKat, users have to choose the default storage location
// Lollipop+ can choose their storage location via OPEN_DOCUMENT_TREE
// Older devices can't now be supported as I'm moving to DocumentFile

package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lib.folderpicker.FolderPicker;

public class BootUpCheck extends AppCompatActivity {

    // Declare helper classes:
    Preferences preferences;
    StorageAccess storageAccess;
    IndexSongs indexSongs;
    FullscreenActivity fullscreenActivity;
    SongXML songXML;
    ChordProConvert chordProConvert;
    OnSongConvert onSongConvert;
    UsrConvert usrConvert;
    TextSongConvert textSongConvert;
    SetTypeFace setTypeFace;

    // Handlers for fonts
    Handler lyrichandler, chordhandler, presohandler, presoinfohandler, customhandler, monohandler;

    // Declare views
    ProgressBar progressBar;
    TextView progressText, version, previousStorageTextView, previousStorageHeading;
    Button chooseStorageButton, goToSongsButton, userGuideButton, previousStorageButton;
    LinearLayout storageLinearLayout, readUpdate, userGuideLinearLayout;
    RelativeLayout goToSongsRelativeLayout;
    Spinner appMode, previousStorageSpinner;
    Toolbar toolbar;
    // Declare variables
    String text="", versionCode="",storagePath="";
    Uri uriTree;
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
        fullscreenActivity = new FullscreenActivity();
        songXML = new SongXML();
        chordProConvert = new ChordProConvert();
        onSongConvert = new OnSongConvert();
        usrConvert = new UsrConvert();
        textSongConvert = new TextSongConvert();
        setTypeFace = new SetTypeFace();

        // Initialise the font handlers
        lyrichandler = new Handler();
        chordhandler = new Handler();
        presohandler = new Handler();
        presoinfohandler = new Handler();
        customhandler = new Handler();
        monohandler = new Handler();

        // This will do one of 2 things - it will either show the splash screen or the welcome screen
        // To determine which one, we need to check the storage is set and is valid
        // The last version used must be the same or greater than the current app version

        // Load up all of the preferences and the user specified storage location if it exists
        storagePath = storageAccess.getStoragePreference(BootUpCheck.this, preferences);
        uriTree = storageAccess.homeFolder(BootUpCheck.this, preferences);
        showCurrentStorage(uriTree);

        // Check we have the required storage permission
        checkStoragePermission();

        // Determine the last used version and what version the app is now
        skiptoapp = versionCheck();

        if (checkStorageIsValid() && storageGranted && skiptoapp) {
            Log.d("BootUpCheck", "Ready to go straight to the app");
            setContentView(R.layout.activity_logosplash);
            goToSongs();

        } else {
            setContentView(R.layout.boot_up_check);

            // Identify the views
            identifyViews();

            // Update the verion and storage
            showCurrentStorage(uriTree);
            version.setText(versionCode);

            // Set up the button actions
            setButtonActions();

            // Check our state of play (based on if location is set and valid)
            checkReadiness();

        }

    }

    void identifyViews() {
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        goToSongsButton = findViewById(R.id.goToSongsButton);
        chooseStorageButton = findViewById(R.id.chooseStorageButton);
        storageLinearLayout = findViewById(R.id.storageLinearLayout);
        goToSongsRelativeLayout = findViewById(R.id.goToSongsRelativeLayout);
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
        previousStorageHeading = findViewById(R.id.previousStorageHeading);
        // Set the 3 options
        ArrayList<String> appModes = new ArrayList<>();
        appModes.add(getString(R.string.performancemode));
        appModes.add(getString(R.string.stagemode));
        appModes.add(getString(R.string.presentermode));
        ArrayAdapter<String> aa = new ArrayAdapter<>(BootUpCheck.this,R.layout.my_spinner,appModes);
        appMode.setAdapter(aa);
        // Select the appropriate one
        switch (FullscreenActivity.whichMode) {
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
        showLoadingBar(true);
        goToSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (appMode.getSelectedItemPosition()) {
                    case 0:
                    default:
                        FullscreenActivity.whichMode = "Performance";
                        break;

                    case 1:
                        FullscreenActivity.whichMode = "Stage";
                        break;

                    case 2:
                        FullscreenActivity.whichMode = "Presentation";
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
    }
    void pulseStartButton() {
        CustomAnimations ca = new CustomAnimations();
        ca.pulse(BootUpCheck.this, goToSongsButton);
    }
    void showLoadingBar(boolean clickable) {
        int progressvisibility;
        int visibility;

        if (clickable) {
            visibility = View.VISIBLE;
            progressvisibility = View.GONE;
            pulseStartButton();
        } else {
            visibility = View.INVISIBLE;
            progressvisibility = View.VISIBLE;
            goToSongsButton.clearAnimation();
        }

        // This bit disables the buttons, stops the animation on the start button
        progressBar.setVisibility(progressvisibility);
        readUpdate.setClickable(clickable);
        storageLinearLayout.setClickable(clickable);
        goToSongsButton.setClickable(clickable);
        appMode.setClickable(clickable);
        goToSongsButton.setVisibility(visibility);
        appMode.setVisibility(visibility);
        userGuideLinearLayout.setClickable(clickable);
        userGuideButton.setClickable(clickable);
    }
    void checkStoragePermission() {
        Log.d("BootUpCheck", "checkStoragePermission");

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
                Snackbar.make(findViewById(R.id.page), R.string.storage_rationale,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
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
                } catch (Exception e) {
                    e.printStackTrace();
                    text = "" + uriTree;
                }
            } else {
                text = u.getPath();
            }

        } else {
            text = "";
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
                Log.d("BootUpCheck", "uriTree=" + uriTree);
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
                // This is for Android KitKat - deprecated file method
                String folderLocation = resultData.getExtras().getString("data");
                if (folderLocation!=null) {
                    uriTree = Uri.parse(folderLocation);

                    // If we can write to this all is good, if not, tell the user (likely to be SD card)
                    if (!storageAccess.canWrite(BootUpCheck.this, uriTree)) {
                        uriTree = null;
                        ShowToast showToast = new ShowToast();
                        showToast.showToastMessage(BootUpCheck.this, getString(R.string.storage_notwritable));
                        if (locations.size() > 0) {
                            // Revert back to the blank selection as the one chosen can't be used
                            previousStorageSpinner.setSelection(0);
                        }
                    }
                }


            } else {
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
            }

            // Save the location
            if (uriTree!=null) {
                FullscreenActivity.uriTree = uriTree;
                preferences.setMyPreferenceString(this, "uriTree", uriTree.toString());
            } else {
                preferences.setMyPreferenceString(this, "uriTree", "");
            }

            // Update the storage text
            showCurrentStorage(uriTree);

            // See if we can show the start button yet
            checkReadiness();
        }
    }

    boolean checkStorageIsValid() {
        // Check that the location exists and is writeable
        // Since the OpenSong folder may not yet exist, we can check for the locationUri or it's parent

        if (uriTree!=null) {
            DocumentFile df = storageAccess.documentFileFromRootUri(BootUpCheck.this, uriTree,
                    storageAccess.getStoragePreference(BootUpCheck.this, preferences));
            return df != null && df.canWrite();
        }
        return false;
    }
    boolean versionCheck() {
        // Do this as a separate thread
        lastUsedVersion = preferences.getMyPreferenceInt(BootUpCheck.this, "lastUsedVersion", 0);
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            thisVersion = pInfo.versionCode;
            versionCode = "V."+pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
            thisVersion = 0;
            versionCode = "";
        }
        Log.d("BootUpCheck", "lastUsedVersion =" + lastUsedVersion);
        Log.d("BootUpCheck", "thisversion=" + thisVersion);
        return lastUsedVersion >= thisVersion;
    }

    void checkReadiness() {
        Log.d("BootUpCheck", "storageGranted=" + storageGranted);
        Log.d("BootUpCheck", "checkStorageIsValid()=" + checkStorageIsValid());
        Log.d("BootUpCheck", "skiptoapp=" + skiptoapp);

        if (checkStorageIsValid() && storageGranted && !skiptoapp) {
            // We're good to go, but need to wait for the user to click on the start button
            goToSongsRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            // Not ready, so hide the start button
            goToSongsRelativeLayout.setVisibility(View.GONE);
        }
    }

    void goToSongs() {
        // Show the progressBar if we were on the BootUpCheck screen
        if (progressBar!=null) {
            showLoadingBar(false);
        }

        // Load up the storage into the FullscreenActivity
        FullscreenActivity.uriTree = uriTree;

        final TextView tv = findViewById(R.id.currentAction);
        final ProgressBar progressBarHorizontal = findViewById(R.id.progressBarHorizontal);
        tv.setVisibility(View.VISIBLE);
        tv.setText("");
        progressBarHorizontal.setVisibility(View.INVISIBLE);

        // Do this as a separate thread
        GoToSongs goToSongsAsync = new GoToSongs(progressBarHorizontal, tv);
        goToSongsAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class GoToSongs extends AsyncTask<Object, String, String> {

        ProgressBar hProgressBar;
        TextView textView;
        int numSongs;
        int currentSongNum;
        String currentSongName, message;
        boolean settingProgressBarUp = false;
        Intent intent;

        GoToSongs(ProgressBar pb, TextView tv) {
            hProgressBar = pb;
            textView = tv;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Object... objects) {
            Looper.prepare();

            // Load up custom font

            // Check if the folders exist, if not, create them
            message = getString(R.string.storage_check);
            publishProgress("setmessage");
            final String progress = storageAccess.createOrCheckRootFolders(BootUpCheck.this, preferences);
            foldersok = !progress.contains("Error");
            Log.d("BootUpCheck", "progress=" + progress);

            if (foldersok) {
                // Load up all of the preferences into FullscreenActivity (static variables)
                message = getString(R.string.load_preferences);
                publishProgress("setmessage");
                fullscreenActivity.mainSetterOfVariables(BootUpCheck.this, preferences, setTypeFace,
                        lyrichandler, chordhandler, presohandler, presoinfohandler, customhandler, monohandler);

                // Search for the user's songs
                message = getString(R.string.initialisesongs_start).replace("-", "").trim();
                publishProgress("setmessage");
                try {
                    storageAccess.listSongs(BootUpCheck.this, preferences);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Show how many songs have been found and display this to the user
                // This will remain as until the current folder is build
                numSongs = FullscreenActivity.songIds.size();
                settingProgressBarUp = true;
                message = numSongs + " " + getString(R.string.processing) + "\n" + getString(R.string.wait);
                publishProgress("setupprogressbar");

                try {
                    indexSongs.initialiseIndexStuff();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Listener for conversion of files
                boolean hadtoconvert = false;
                for (currentSongNum = 0; currentSongNum < numSongs; currentSongNum++) {
                    currentSongName = FullscreenActivity.songIds.get(currentSongNum);
                    if (currentSongName.contains("OpenSong/Songs/")) {
                        currentSongName = currentSongName.substring(currentSongName.lastIndexOf("OpenSong/Songs/") + 15);
                    }
                    message = currentSongName + "\n(" + currentSongNum + "/" + numSongs + ")";
                    publishProgress(currentSongName);
                    boolean converted = indexSongs.doIndexThis(BootUpCheck.this, storageAccess, preferences, songXML,
                            chordProConvert, usrConvert, onSongConvert, textSongConvert, currentSongNum);
                    if (converted) {
                        message = "Converted song...";
                        publishProgress("setmessage");
                        hadtoconvert = true;
                    }
                }

                indexSongs.completeLog();

                // If we had to convert songs from OnSong, ChordPro, etc, we need to reindex to get sorted songs again
                // This is because the filename will likely have changed alphabetical position
                // Alert the user to the need for rebuilding and repeat the above
                if (hadtoconvert) {
                    Log.d("d", "Conversion had to happen, so rebuild the search index");
                    message = "Updating indexes of converted songs...";
                    publishProgress("setmessage");

                    try {
                        indexSongs.initialiseIndexStuff();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Listener for conversion of files
                    for (currentSongNum = 0; currentSongNum < numSongs; currentSongNum++) {
                        currentSongName = FullscreenActivity.songIds.get(currentSongNum);
                        if (currentSongName.contains("OpenSong/Songs/")) {
                            currentSongName = currentSongName.substring(currentSongName.lastIndexOf("OpenSong/Songs/") + 15);
                        }
                        message = currentSongName + "\n(" + currentSongNum + "/" + numSongs + ")";
                        publishProgress(currentSongName);
                        indexSongs.doIndexThis(BootUpCheck.this, storageAccess, preferences, songXML,
                                chordProConvert, usrConvert, onSongConvert, textSongConvert, currentSongNum);
                    }
                    indexSongs.completeLog();
                }

                indexSongs.getSongDetailsFromIndex();

                // Finished indexing
                message = getString(R.string.success);
                publishProgress("setmessage");

                // Decide on where we are going and set the intent to launch it
                intent = new Intent();

                switch (FullscreenActivity.whichMode) {
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
                Intent intent = new Intent();
                intent.setClass(BootUpCheck.this, BootUpCheck.class);
                startActivity(intent);
                finish();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... string) {
            if (settingProgressBarUp) {
                settingProgressBarUp = false;
                hProgressBar.setVisibility(View.VISIBLE);
                hProgressBar.setMax(numSongs);
            }
            if (currentSongNum > 0) {
                hProgressBar.setProgress(currentSongNum);
            }
            textView.setText(message);
        }

        @Override
        protected void onPostExecute(String s) {

            try {
                // Now save the appropriate variables and then start the intent
                // Set the current version
                preferences.setMyPreferenceInt(BootUpCheck.this, "lastUsedVersion", thisVersion);
                Preferences.savePreferences();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Show the progressBar if we were on the BootUpCheck screen
            if (progressBar != null) {
                showLoadingBar(true);
            }

            startActivity(intent);
            finish();

            // For now, just stay here
            //progressBar.setVisibility(View.GONE);
            //showLoadingBar(true);
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
            progressBar.setVisibility(View.VISIBLE);
            previousStorageTextView.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
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
                    previousStorageSpinner.setVisibility(View.GONE);
                    previousStorageHeading.setVisibility(View.GONE);
                } else {
                    // Listen for the clicks!
                    previousStorageHeading.setVisibility(View.VISIBLE);
                    locations.add(0,"");
                    previousStorageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (changed) {
                                if (position>0) {
                                    File f = new File(locations.get(position));
                                    uriTree = Uri.fromFile(f);
                                    Log.d("BootUpCheck", "uriTree=" + uriTree);
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
                    previousStorageSpinner.setVisibility(View.VISIBLE);
                    previousStorageTextView.setVisibility(View.GONE);
                }
            }
        }
    }

}