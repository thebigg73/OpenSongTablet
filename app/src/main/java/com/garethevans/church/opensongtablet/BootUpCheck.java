// This file runs before everything else.  It checks for storage permissions and current app version
// It only shows if the storage location has an issue or we have updated the app, or user returned manually
package com.garethevans.church.opensongtablet;

import static com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.make;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import lib.folderpicker.FolderPicker;

public class BootUpCheck extends AppCompatActivity {

    // Declare helper classes:
    private final String TAG = "BootUpCheck";
    private final String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private Preferences preferences;
    private StorageAccess storageAccess;
    private Permissions permissions;

    // Declare views
    private TextView progressText;
    private TextView version;
    private TextView previousStorageTextView;
    private TextView previousStorageLocationsTextView;
    private TextView previousStorageHeading;
    private TextView currentAction;
    private TextView warningText;
    private LinearLayout MyQuicktip;
    private Button chooseStorageButton;
    private Button goToSongsButton;
    private Button userGuideButton;
    private Button previousStorageButton;
    private Button resetCacheButton;
    private Button helpOnline;
    private Button helpForum;
    private FloatingActionButton quicktip;
    private LinearLayout storageLinearLayout;
    private LinearLayout readUpdate;
    private LinearLayout userGuideLinearLayout;
    private LinearLayout goToSongsLinearLayout;
    private Spinner appMode;
    private Spinner previousStorageSpinner;
    private String versionCode="";
    private String whichMode;
    private Uri uriTree;
    private Uri uriTreeHome;
    private boolean storageGranted;
    private boolean skiptoapp;
    private boolean changed;
    private int thisVersion;
    private ArrayList<String> locations;
    private File folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the helper classes (preferences)
        permissions = new Permissions();
        preferences = new Preferences();
        storageAccess = new StorageAccess();
        SetTypeFace setTypeFace = new SetTypeFace();
        StaticVariables.activity = BootUpCheck.this;

        // Initialise the font
        setTypeFace.setUpAppFonts(BootUpCheck.this,preferences,new Handler(),new Handler(), new Handler(),new Handler(), new Handler(), new Handler());
        // This will do one of 2 things - it will either show the splash screen or the welcome screen
        // To determine which one, we need to check the storage is set and is valid
        // The last version used must be the same or greater than the current app version

        // Load up all of the preferences and the user specified storage location if it exists
        checkPreferencesForStorage();

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

            // Set up the button actions
            setButtonActions();

            // Check for Google Play availability
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
                installPlayServices();
            }

            // Update the version
            version.setText(versionCode);

            // Always update the storage location text (used in checkReadiness)
            showCurrentStorage(uriTreeHome);

            // See if we can show the start button yet
            checkReadiness();
        }
    }

    private void identifyViews() {
        warningText = findViewById(R.id.warningText);
        progressText = findViewById(R.id.progressText);
        MyQuicktip = findViewById(R.id.MyQuicktip);
        helpOnline = findViewById(R.id.helponline);
        helpForum = findViewById(R.id.helpforum);
        goToSongsButton = findViewById(R.id.goToSongsButton);
        chooseStorageButton = findViewById(R.id.chooseStorageButton);
        storageLinearLayout = findViewById(R.id.storageLinearLayout);
        goToSongsLinearLayout = findViewById(R.id.goToSongsLinearLayout);
        readUpdate = findViewById(R.id.readUpdate);
        version = findViewById(R.id.version);
        version.setText(versionCode);
        quicktip = findViewById(R.id.quicktip);
        userGuideLinearLayout = findViewById(R.id.userGuideLinearLayout);
        userGuideButton = findViewById(R.id.userGuideButton);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
    private void setButtonActions() {
        showLoadingBar();
        goToSongsButton.setOnClickListener(v -> {
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
            // IV - We're good to do a logo splash startup
            setContentView(R.layout.activity_logosplash);
            goToSongs();
        });
        chooseStorageButton.setOnClickListener(v -> chooseStorageLocation());
        readUpdate.setOnClickListener(v -> {
            String url = "http://www.opensongapp.com/latest-updates";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(i);
            } catch (Exception e) {
                Log.d("BootUpCheck", "Error showing activity");
            }
        });
        quicktip.setOnClickListener(v -> {
            // IV - Toggle quicktip display
            if (MyQuicktip.getVisibility() == View.GONE)  {
                MyQuicktip.setVisibility(View.VISIBLE);
            } else {
                MyQuicktip.setVisibility(View.GONE);
            }
        });
        helpOnline.setOnClickListener(v -> {
            String url = "https://www.opensongapp.com/user-guide/setting-up-and-using-opensongapp/setting-up-opensong-tablet";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(i);
            } catch (Exception e) {
                Log.d("BootUpCheck", "Error showing activity");
            }
        });
        helpForum.setOnClickListener(v -> {
            String url = "https://groups.google.com/g/opensongapp";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(i);
            } catch (Exception e) {
                Log.d("BootUpCheck", "Error showing activity");
            }
        });
        userGuideLinearLayout.setOnClickListener(v -> {
            String url = "http://www.opensongapp.com/user-guide";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(i);
            } catch (Exception e) {
                Log.d("BootUpCheck", "Error showing activity");
            }
        });
        userGuideButton.setOnClickListener(v -> {
            String url = "http://www.opensongapp.com/user-guide";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(i);
            } catch (Exception e) {
                Log.d("BootUpCheck", "Error showing activity");
            }
        });
        previousStorageButton.setOnClickListener(v -> startSearch());
        resetCacheButton.setOnClickListener(v -> clearTheCaches());
    }
    private void pulseStartButton() {
        CustomAnimations ca = new CustomAnimations();
        ca.pulse(BootUpCheck.this, goToSongsButton);
    }
    private void pulseStorageButton() {
        CustomAnimations ca2 = new CustomAnimations();
        ca2.pulse(BootUpCheck.this, chooseStorageButton);
    }
    private void showLoadingBar() {
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
    private void checkPreferencesForStorage() {
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
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R && !permissions.checkForPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            storageGranted = false;
            requestStoragePermission();
        } else {
            storageGranted = true;
        }
    }
    private void requestStoragePermission() {
        if (permissions.shouldShowRequestRationale(this,storagePermission)) {
            try {
                make(findViewById(R.id.page), R.string.storage_rationale,
                        LENGTH_INDEFINITE).setAction(R.string.ok, view -> permissions.requestForPermissions(this,new String[]{storagePermission},101)).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // Storage permission has not been granted yet. Request it directly.
                permissions.requestForPermissions(BootUpCheck.this,new String[]{storagePermission},101);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"storageGranted:"+storageGranted);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showCurrentStorage(Uri u) {
        // This tries to make the uri more user readable!
        // IV - A text value of null can be set to force a valid selection
        String text = null;
        // IV - Provides extra information on the folder
        String extra = "";

        // IV - Try to get the path with leading / and trailing /OpenSong
        try {
            text = u.getPath();
            assert text != null;


            // The  storage location getPath is likely something like /tree/primary:/document/primary:/OpenSong
            // This is due to the content using a document contract
            // IV: Exclude raw storage
            if (!text.startsWith("/tree/raw:") & !text.startsWith("/tree/msd:") & text.contains(":")) {
                // IV - When not an internal path (more patterns may be needed) indicate as external
                if (!text.contains("/tree/primary")) { extra = this.getResources().getString(R.string.storage_ext); }
                text = "/" + text.substring(text.lastIndexOf(":") + 1);
                text = text.replace("//", "/");
                if (!text.endsWith("/" + storageAccess.appFolder)) {
                    text += "/" + storageAccess.appFolder;
                }
            } else if (uriTree.getPath().startsWith("/storage") || uriTreeHome.getPath().startsWith("file:///")){
                // GE - For KitKat, the uriTreeHome will start with file:/// and the uri will start with /storage
                text = text.replace("file:///","");
            } else {
                uriTree = null;
            }
            // Decide if the user needs blocking as they have selected a subfolder of an OpenSong folder
            if ((warningText!=null) && (text.contains("/OpenSong/"))) {
                warningText.setVisibility(View.VISIBLE);
            } else if (warningText!=null){
                warningText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            uriTree = null;
        }

        // IV - If we do not have a valid uri force 'Please select'
        if (uriTree == null) {
            uriTreeHome = null;
            text = getString(R.string.pleaseselect);
        }

        saveUriLocation();

        if (progressText!=null) {
            // IV - If we have a path try to give extra info of a 'songs' count
            // IV - Do a song count for pre R only as R and above do not list songs as they are not media files
            if (checkStorageIsValid() & Build.VERSION.SDK_INT < Build.VERSION_CODES.R && text.startsWith("/") && text.endsWith("/OpenSong")) {
                ArrayList<String> songIds;
                try {
                    storageAccess = new StorageAccess();
                    songIds = storageAccess.listSongs(BootUpCheck.this, preferences);
                    // Only items that don't end with / are songs!
                    int count = 0;
                    for (String s:songIds) {
                        if (!s.endsWith("/")) {
                            count++;
                        }
                    }
                    if (extra.length() > 0) { extra = ", " + extra; }
                    extra = count + " Songs" + extra;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (extra.equals("")) {
                text = getString(R.string.currentstorage) + ": " + text;
            } else {
                text = getString(R.string.currentstorage) + " (" + extra + "): " + text;
            }

            // We aren't just passing through, so we can set the text
            progressText.setText(text);
        }
    }
    @SuppressLint("InlinedApi")
    private void chooseStorageLocation() {
        progressText.setText(String.format("%s ...", getString(R.string.currentstorage)));
        if (storageGranted) {
            Intent intent;
            if (storageAccess.lollipopOrLater()) {
                try {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    // IV - 'Commented in' this extra to try to always show internal and sd card storage
                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    //intent.putExtra("android.content.extra.FANCY", true);
                    //intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,uriTree);
                    startActivityForResult(intent, 42);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                openFragment();
            }

        } else {
            requestStoragePermission();
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode,resultCode,resultData);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode==7789 && resultData!=null && resultData.getExtras()!=null) {
                kitKatDealWithUri(resultData);
            } else {
                lollipopDealWithUri(resultData);
            }

            // Save the location uriTree and uriTreeHome
            saveUriLocation();

            // After an attempt to change storage, set to show Welcome song
            StaticVariables.whichSongFolder = getString(R.string.mainfoldername);
            StaticVariables.songfilename = "Welcome to OpenSongApp";
            preferences.setMyPreferenceString(BootUpCheck.this,"whichSongFolder",StaticVariables.whichSongFolder);
            preferences.setMyPreferenceString(BootUpCheck.this,"songfilename",StaticVariables.songfilename);

        }

        // Always update the storage location text (used in checkReadiness)
        showCurrentStorage(uriTreeHome);

        // See if we can show the start button yet
        checkReadiness();
    }

    private void kitKatDealWithUri(Intent resultData) {
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
        }
    }
    private void lollipopDealWithUri(Intent resultData) {
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
    }

    private void saveUriLocation() {
        if (uriTree!=null) {
            // Save the preferences
            preferences.setMyPreferenceString(BootUpCheck.this, "uriTree", uriTree.toString());
            preferences.setMyPreferenceString(BootUpCheck.this, "uriTreeHome", uriTreeHome.toString());
        } else {
            preferences.setMyPreferenceString(BootUpCheck.this, "uriTree", "");
            preferences.setMyPreferenceString(BootUpCheck.this, "uriTreeHome", "");
        }
    }

    private boolean checkStorageIsValid() {
        // Check that the location exists and is writeable
        // Since the OpenSong folder may not yet exist, we check for the uriTree and if it is writeable
        try {
            if (uriTree != null) {
                if (uriTree.toString().contains(("/OpenSong/"))) {
                    return false;
                // GE - added after 5.3.1 users on KitKat had problems
                } else if (storageAccess.lollipopOrLater()) {
                    DocumentFile df = storageAccess.documentFileFromRootUri(BootUpCheck.this, uriTree, uriTree.getPath());
                    if (df == null || !df.canWrite()) {
                        String s = getString(R.string.currentstorage) + ": " + getString(R.string.pleaseselect);
                        progressText.setText(s);
                    }
                    return df != null && df.canWrite();
                } else {
                    // This if for KitKat users
                    File f = new File(uriTree.getPath());
                    return f.canWrite();
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    private boolean versionCheck() {
        // Do this as a separate thread.  0 is for fresh installs.  1 is for user choice to return to menu
        int lastUsedVersion = preferences.getMyPreferenceInt(BootUpCheck.this, "lastUsedVersion", 0);
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                thisVersion = (int) pInfo.getLongVersionCode();
            } else {
                //noinspection
                thisVersion = pInfo.versionCode;
            }
            versionCode = "V."+pInfo.versionName;
        } catch (Exception e) {
            thisVersion = 0;
            versionCode = "";
        }

        // If this is a fresh install (or updating from before V4.8.5) clear the cache in case

        return lastUsedVersion >= thisVersion;
    }

    private void clearTheCaches() {
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

    private static boolean doDeleteFile(File file) {
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

    private boolean doDeleteCacheFile(File file) {

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

    private void checkReadiness() {
        if (skiptoapp) {
            try {
                goToSongsLinearLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // We retrieve the selected location (as set by showCurrentStorage)
        String text = (String) progressText.getText();

        if (checkStorageIsValid() && storageGranted && !skiptoapp && !text.contains("/OpenSong/")) {
            // We're good to go, but need to wait for the user to click on the start button
            goToSongsLinearLayout.setVisibility(View.VISIBLE);
            chooseStorageButton.clearAnimation();
        } else {
            // Not ready, so hide the start button
            goToSongsLinearLayout.setVisibility(View.GONE);
            // Show the storage as a pulsing button
            pulseStorageButton();
            if (progressText!=null && !((String) progressText.getText()).contains(getString(R.string.pleaseselect))) {
                // IV - Inform the user the folder is not usable
                progressText.setText(String.format("%s\n%s", text, getString(R.string.storage_notwritable)));
            }
        }
    }

    private void goToSongs() {
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
        final boolean cancelled = false;
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
            boolean foldersok = !progress.contains("Error");

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

                // Try to create the basic databases
                SQLiteHelper sqLiteHelper = new SQLiteHelper(BootUpCheck.this);
                sqLiteHelper.resetDatabase(BootUpCheck.this);
                NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(BootUpCheck.this);
                nonOpenSongSQLiteHelper.initialise(BootUpCheck.this,storageAccess,preferences);

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
                    whichMode = preferences.getMyPreferenceString(BootUpCheck.this,"whichMode","Performance");
                }

                // Set the app mode
                StaticVariables.whichMode = whichMode;

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

    private void startSearch() {
        // Deactivate the stuff we shouldn't click on while it is being prepared
        setEnabledOrDisabled(false);

        // Initialise the available storage locations
        locations = new ArrayList<>();

        FindLocations findlocations = new FindLocations();
        findlocations.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setEnabledOrDisabled(boolean what) {
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
    private void openFragment() {
        Intent intent = new Intent(this, FolderPicker.class);
        intent.putExtra("title", getString(R.string.changestorage));
        intent.putExtra("pickFiles", false);
        if (uriTree!=null) {
            intent.putExtra("location", uriTree.getPath());
        }
        startActivityForResult(intent, 7789);
    }

    private void walkFiles(File root) {
        if (root!=null && root.exists() && root.isDirectory()) {
            File[] list = root.listFiles();
            if (list != null) {
                for (File f : list) {
                    if (f.isDirectory()) {
                        String where = f.getAbsolutePath();
                        displayWhere(where);
                        if (!where.contains(".estrongs") && !where.contains("com.ttxapps") && where.endsWith("/OpenSong/Songs")) {
                            String extra = "";
                            // IV - Do a song count for pre R only as R and above do not list songs as they are not media files
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                                extra = storageAccess.songCountAtLocation(f) + " Songs";
                            }
                            // Found one and it isn't in eStrongs recycle folder or the dropsync temp files!
                            // IV - Add  a leading ¬ and remove trailing /Songs
                            where = "¬" + where.substring(0, where.length() - 6);
                            // IV - For paths identified as Internal storage (more patterns may be needed) remove the parent folder
                            where = where.
                                replace("¬/storage/sdcard0/"         ,"/").
                                replace("¬/storage/emulated/0/"      ,"/").
                                replace("¬/storage/emulated/legacy/" ,"/").
                                replace("¬/storage/self/primary/"    ,"/");
                            if (where.startsWith("¬")) {
                                // IV - Handle other paths as 'External'
                                where = where.substring(10);
                                if (!extra.equals("")) {
                                    extra = extra + ", ";
                                }
                                extra = extra + this.getResources().getString(R.string.storage_ext) + " " + where.substring(0, where.indexOf("/"));
                                where = where.substring(where.indexOf("/"));
                            }
                            if (!extra.equals("")) {
                                where = "(" + extra + "): " + where;
                            }
                            locations.add(where);
                        }
                        folder = f;
                        walkFiles(f);
                    }
                }
            }
        }
    }

    private void displayWhere(String msg) {
        final String str = msg;
        runOnUiThread(() -> previousStorageTextView.setText(str));
    }

    @SuppressLint("StaticFieldLeak")
    private class FindLocations extends AsyncTask<Object, String, String> {

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
                        if (!sb.toString().contains("¬" + str + "¬")) {
                            sb.append("¬").append(str).append("¬").append(" \n");
                        }
                    }
                    previousStorageTextView.setVisibility(View.GONE);
                    previousStorageLocationsTextView.setText(sb.toString().replace("¬","").trim());
                    previousStorageLocationsTextView.setVisibility(View.VISIBLE);
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

    private void installPlayServices() {
        // We've identified that the user doesn't have the Google Play Store installed
        // Warn them that some features won't work, but give them the option to fix it!
        findViewById(R.id.play_services_error).setVisibility(View.VISIBLE);
        findViewById(R.id.play_services_how).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_services_help)));
            startActivity(i);
        });
    }

}
