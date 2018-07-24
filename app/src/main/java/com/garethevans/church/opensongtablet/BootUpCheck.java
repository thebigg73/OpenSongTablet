// This file uses the Storage Access Framework to allow users to specify their storage location
// For KitKat, users have to choose the default storage location
// Lollipop+ can choose their storage location via OPEN_DOCUMENT_TREE
// Older devices can't now be supported as I'm moving to DocumentFile

package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BootUpCheck extends AppCompatActivity {

    // Declare helper classes:
    Preferences mPreferences;
    StorageAccess mStorageAccess;

    // Declare views
    ProgressBar progressBar;
    TextView progressText, version;
    Button chooseStorageButton, goToSongsButton;
    LinearLayout storageLinearLayout, goToSongsLinearLayout, readUpdate, userGuideLinearLayout;

    // Declare variables
    String text;
    private String[] foldersNeeded = {"Backgrounds", "Export", "Fonts", "Highlighter", "Images", "Media",
            "Notes", "OpenSong Scripture", "Pads", "Profiles", "Received", "Scripture", "Sets",
            "Settings", "Slides", "Songs", "Variations"};
    boolean foldersok, storageGranted;
    String defaultKitKatStorage;
    Uri locationUri;
    int lastUsedVersion;
    int thisVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boot_up_check);

        // Load the helper classes (preferences)
        mPreferences = new Preferences();
        mStorageAccess = new StorageAccess();

        // Check we have the required storage permission
        // Check we have storage permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permission has not been granted.
            storageGranted = false;
            requestStoragePermission();
        } else {
            storageGranted = true;
        }

        // Identify the views
        identifyViews();

        // Set up the button actions
        setButtonActions();

        // Load up the user specified storage location if it exists
        loadStoragePreference();

        // Determine the last used version and what version the app is now
        boolean isgoodtogo = versionCheck();

        // Test if it exists and if so, make sure the folders it needs are there
        if (checkUriTreeIsValid()) {
            // Update the text to show the current storage
            progressText.setText(showCurrentStorage(locationUri));

            // Move to the app if we have already seen this version....
            // Otherwise wait for the user to click on the go to songs button
            if (isgoodtogo) {
                Log.d("d","Good to go and automatically load the app after checking the folders");
                goToSongs();
            }

        } else {
            // Wait for the user to choose a valid storage location
            progressText.setText(showCurrentStorage(locationUri));
        }
    }

    void identifyViews() {
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        goToSongsButton = findViewById(R.id.goToSongsButton);
        chooseStorageButton = findViewById(R.id.chooseStorageButton);
        storageLinearLayout = findViewById(R.id.storageLinearLayout);
        goToSongsLinearLayout = findViewById(R.id.goToSongsLinearLayout);
        readUpdate = findViewById(R.id.readUpdate);
        version = findViewById(R.id.version);
        userGuideLinearLayout = findViewById(R.id.userGuideLinearLayout);
    }

    void setButtonActions() {
        goToSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOrCreateSubFolders();
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
                    Log.d("d", "Error showing activity");
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
                    Log.d("d", "Error showing activity");
                }
            }
        });
    }

    void loadStoragePreference() {
        String s = mPreferences.getMyPreferenceString(BootUpCheck.this,"chosenstorage",null);
        if (s!=null) {
            locationUri = Uri.parse(s);
        } else {
            locationUri = null;
        }
    }

    boolean versionCheck() {
        // Do this as a separate thread
        mPreferences = new Preferences();
        lastUsedVersion = mPreferences.getMyPreferenceInt(BootUpCheck.this,"showSplashVersion",0);
        PackageInfo pInfo;
        String versionCode;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            thisVersion = pInfo.versionCode;
            versionCode = "V."+pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
            thisVersion = 0;
            versionCode = "";
        }
        version.setText(versionCode);
        return lastUsedVersion >= thisVersion;
    }

    boolean checkUriTreeIsValid() {
        // Do this as a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        // If the value doesn't exist, this will throw an error - it's ok though!!!!
                        DocumentFile locationDF = DocumentFile.fromTreeUri(BootUpCheck.this, locationUri);
                        foldersok = locationDF.exists() && locationDF.canWrite();
                    } catch (Exception e) {
                        // Problem with the tree location is SAF, so get the user to set it again
                        foldersok = false;
                        locationUri = null;
                    }
                } else {
                    // For Lollipop, we have to default to the default external storage allowed
                    defaultKitKatStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
                    locationUri = Uri.parse(defaultKitKatStorage);
                    foldersok = true;
                }
            }
        }).run();

        if (foldersok) {
            // Storage is ok so hide the go to storage button and show the go to songs button
            goToSongsLinearLayout.setVisibility(View.VISIBLE);
            //storageLinearLayout.setVisibility(View.GONE);
        } else {
            // Storage isn't set or isn't valid so hide the go to songs button and show storage button
            goToSongsLinearLayout.setVisibility(View.GONE);
            //storageLinearLayout.setVisibility(View.VISIBLE);
        }

        /*//
        This is a test to see if I can identify file locations using Storage Access
        Uri uri = mStorageAccess.getSongLocationAsUri(this,"Band/Temporary","Test songs");
        Log.d("BootUpCheck","testing uri of a made up song = "+uri);
        */

        // Display the current storage location
        progressText.setText(showCurrentStorage(locationUri));
        return foldersok;


    }

    boolean checkWeAreNotInsideOpenSongAsRoot() {
        // Big name, but big problem if people select inside the OpenSong folder as the root
        DocumentFile locationTree = DocumentFile.fromTreeUri(this,locationUri);
        DocumentFile foundSongs = locationTree.findFile("Songs");
        DocumentFile foundSets = locationTree.findFile("Sets");
        return foundSongs == null && foundSets == null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void chooseStorageLocation() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, 42);
    }

    // The permission requests
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                Snackbar.make(findViewById(R.id.page), R.string.storage_rationale,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(BootUpCheck.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 98765);
                    }
                }).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // Storage permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(BootUpCheck.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 98765);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK) {
            Uri treeUri = resultData.getData();
            if (treeUri!=null) {
                DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Log.d("d", "pickedDir=" + pickedDir.getUri().toString());
                mPreferences.setMyPreferenceString(this, "chosenstorage", treeUri.toString());

                // Update the variable
                locationUri = treeUri;

                // Check the storage location is valid
                checkUriTreeIsValid();

                // Display the chosen storage location
                progressText.setText(showCurrentStorage(locationUri));

                // Check we aren't inside an OpenSong folder
                if (!checkWeAreNotInsideOpenSongAsRoot()) {
                    // Make the user select again
                    ShowToast toast = new ShowToast();
                    toast.showToastMessage(this,getString(R.string.root_error));
                    chooseStorageLocation();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 98765) {
            storageGranted = grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void goToSongs() {
        // Show the progressBar
        progressBar.setVisibility(View.VISIBLE);

        // Do this as a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                foldersok = checkOrCreateSubFolders();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        progressText.setText(showCurrentStorage(locationUri));
                    }
                });
                if (foldersok) {
                    Log.d("d","Start the app!!!!!");
                }
            }
        }).start();
    }

    boolean checkOrCreateSubFolders() {
        try {
            // Define tree root uri and expose it with DocumentFile
            DocumentFile locationTree = DocumentFile.fromTreeUri(this,locationUri);

            // Create the main app directory if it doesn't exist
            String rootFolderNeeded = "TestOpenSong";
            DocumentFile df_rootFolder = locationTree.findFile(rootFolderNeeded);
            if (df_rootFolder == null || !df_rootFolder.exists()) {
                // Trying to create the main directory
                locationTree.createDirectory(rootFolderNeeded);
                df_rootFolder = locationTree.findFile(rootFolderNeeded);
            }

            // Now go through each folder we need, check it exists and if not create it
            for (String folder : foldersNeeded) {
                if (df_rootFolder.findFile(folder) == null) {
                    Log.d("CreateFiles", "Creating directory " + folder);
                    final String text = "Creating directory " + folder;
                    try {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressText.setText(text);
                                    }
                                });
                            }
                        }).run();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    df_rootFolder.createDirectory(folder);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            foldersok = false;
        }
        return foldersok;
    }

    String showCurrentStorage(Uri u) {
        String text;
        // Make this look user readable
        if (u == null) {
            text = "";
        } else {
            text = locationUri.toString();
        }
        if (text.contains("/tree/")) {
            text = text.substring(text.indexOf("/tree/"));
        }
        if (text.contains("/document/")) {
            text = text.substring(text.indexOf("/document/"));
        }
        text = text.replace("/tree/","");
        text = text.replace("/document/", "");
        text = text.replace("%3A", "/");
        text = text.replace("%2F", "/");
        text = "Storage: " + text;

        return text;
    }

}