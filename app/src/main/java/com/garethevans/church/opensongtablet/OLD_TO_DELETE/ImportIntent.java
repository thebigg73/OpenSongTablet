/*
package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._BootUpCheck;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._ShowToast;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class ImportIntent extends AppCompatActivity implements PopUpImportExportOSBFragment.MyInterface,
        PopUpImportExternalFile.MyInterface {

    // This class is called when users click on compatible files outwith the app and choose to open them with OpenSongApp

    private StorageAccess storageAccess;
    private ProcessSong processSong;

    // Variables
    private boolean storageGranted = false;
    private DialogFragment newFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._activity_logosplash);

        // Load the helper classes (preferences)
        // Declare helper classes:
        storageAccess = new StorageAccess();
        processSong = new ProcessSong();

        // Load up the user preferences
        //Preferences.loadPreferences(ImportIntent.this);

        // Check we have the required permissions for storage
        checkStoragePermission();
        Log.d("d", "storageGranted=" + storageGranted);
        Intent i = getIntent();
        Log.d("d", "i=" + i);
        dealWithIntent(i);
    }

    private void checkStoragePermission() {
        Log.d("ImportIntent", "checkStoragePermission");

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
                        ActivityCompat.requestPermissions(ImportIntent.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    }
                }).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // Storage permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(ImportIntent.this,
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("d", "Dealing with intent");
        super.onNewIntent(intent);
        dealWithIntent(intent);
    }

    private void dealWithIntent(Intent intent) {
        try {
            String action = intent.getAction();
            String type = intent.getType();

            Log.d("d", "action=" + action);
            Log.d("d", "type=" + type);

            // Sharing clipboard text
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if ("text/plain".equals(type) && storageGranted) {
                    handleSendText(intent); // Handle text being sent
                }
            }

            // Only works for files
            if (intent.getData() != null && intent.getData().getPath() != null && storageGranted) {
                Uri file_uri = intent.getData();
                FullscreenActivity.file_uri = file_uri;
                Log.d("d", "file_uri=" + file_uri);
                String file_name = file_uri.getLastPathSegment();
                Log.d("d", "file_name=" + file_name);

                if (file_name != null) {
                    // Check the file exists!
                    if (storageAccess.uriExists(ImportIntent.this, file_uri)) {
                        if (file_name.endsWith(".osb")) {
                            // This is an OpenSong backup file
                            StaticVariables.whattodo = "processimportosb";
                            showFragment();
                        } else {
                            // This is an file opensong can deal with (hopefully)
                            StaticVariables.whattodo = "doimport";
                            showFragment();
                        }
                    }
                }

            }
        } catch (Exception e) {
            // No file or intent data
            e.printStackTrace();
            // Clear the current intent data as we've dealt with it
        }
    }

    private void handleSendText(Intent intent) {
        StringBuilder sharedText = new StringBuilder(intent.getStringExtra(Intent.EXTRA_TEXT));
        String title;
        // Fix line breaks (if they exist)
        sharedText = new StringBuilder(processSong.fixlinebreaks(sharedText.toString()));

        // If this is imported from YouVersion bible app, it should contain https://bible
        if (sharedText.toString().contains("https://bible")) {

            title = getString(R.string.scripture);
            // Split the text into lines
            String[] lines = sharedText.toString().split("\n");
            if (lines.length > 0) {
                // Remove the last line (http reference)
                if (lines.length - 1 > 0 && lines[lines.length - 1] != null &&
                        lines[lines.length - 1].contains("https://bible")) {
                    lines[lines.length - 1] = "";
                }

                // The 2nd last line is likely to be the verse title
                if (lines.length - 2 > 0 && lines[lines.length - 2] != null) {
                    title = lines[lines.length - 2];
                    lines[lines.length - 2] = "";
                }

                // Now put the string back together.
                sharedText = new StringBuilder();
                for (String l : lines) {
                    sharedText.append(l).append("\n");
                }
                sharedText = new StringBuilder(sharedText.toString().trim());
            }

            // Now split it into smaller lines to better fit the screen size
            Bible bibleC = new Bible();
            sharedText = new StringBuilder(bibleC.shortenTheLines(sharedText.toString(), 40, 6));

            StaticVariables.whattodo = "importfile_customreusable_scripture";
            FullscreenActivity.scripture_title = title;
            FullscreenActivity.scripture_verse = sharedText.toString();
        } else {
            // Just standard text, so create a new song
            StaticVariables.whattodo = "importfile_newsong_text";
            FullscreenActivity.scripture_title = "importedtext_in_scripture_verse";
            FullscreenActivity.scripture_verse = sharedText.toString();
        }
    }

    private void showFragment() {
        // Initialise the newFragment
        Log.d("d", "showFragment called");
        try {
            newFragment = OpenFragment.openFragment(ImportIntent.this);
            Log.d("d", "newFragment=" + newFragment);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String message = "";
        try {
            message = OpenFragment.getMessage(ImportIntent.this);
            Log.d("d", "message=" + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (newFragment != null && !ImportIntent.this.isFinishing()) {
            try {
                newFragment.show(getSupportFragmentManager(), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void refreshAll() {
        Log.d("d", "refreshAll called");
        rebuildSearchIndex();
    }

    private void rebuildSearchIndex() {
        Log.d("d", "rebuildSearchIndex called");
        StaticVariables.whattodo = "";
        FullscreenActivity.needtorefreshsongmenu = false;
        // If the app is already running, send the call to run BootUpCheck
        if (FullscreenActivity.appRunning) {
            // Close this intent and send the listener to rebuild inded
            if (StaticVariables.whichMode.equals("Presentation")) {
                PresenterMode pm = new PresenterMode();
                pm.rebuildSearchIndex();
            } else {
                StageMode sm = new StageMode();
                sm.rebuildSearchIndex();
            }
        } else {
            Intent intent = new Intent();
            intent.setClass(ImportIntent.this, _BootUpCheck.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onSongImportDone() {
        Log.d("d", "onSongImportDone called");
        rebuildSearchIndex();
    }

    @Override
    public void openFragment() {
        Log.d("d", "openFragment called");
    }

    @Override
    public void showToastMessage(String message) {
        Log.d("d", "showToastMessage called");
        _ShowToast.showToast(ImportIntent.this);
    }

    @Override
    public void backupInstall() {
        Log.d("d", "backupInstall called");
        rebuildSearchIndex();
    }

    @Override
    public void selectAFileUri(String s) {
        Log.d("d", "selectAFileUri called");
    }
}
*/
