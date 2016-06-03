package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    // This class covers the splash screen and main settings page
    // Users then have the option to move into the FullscreenActivity

    // Let's define the variables needed for the Settings Page.
    Handler delayfadeinredraw;

    int showSplashVersion; // Show splash on start up first time only if lower than current version

    static int version;
    static SharedPreferences myPreferences;
    static SharedPreferences indexSongPreferences;
    static int test;
    static int want;

    private static boolean storageGranted = false;
    private static final int requestStorage = 0;

    private View mLayout;

    // This class is called when the application first opens.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        version = 0;
        // Decide if user has already seen the splash screen
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
        myPreferences = getSharedPreferences("mysettings", MODE_PRIVATE);
        showSplashVersion = myPreferences.getInt("showSplashVersion", version);

        indexSongPreferences = getSharedPreferences("indexsongs",MODE_PRIVATE);
        Editor editor_index = indexSongPreferences.edit();
        editor_index.putBoolean("buildSearchIndex", true);
        editor_index.apply();

        setContentView(R.layout.activity_logosplash);

        mLayout = findViewById(R.id.pagesplash);
        test = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        want = PackageManager.PERMISSION_GRANTED;
        storageGranted = test == want;

        // Wait 1000ms before either showing the introduction page or the main app
        delayfadeinredraw = new Handler();
        delayfadeinredraw.postDelayed(new Runnable() {
            @Override
            public void run() {
                // This bit then redirects the user to the main app if they've got the newest version

                if (showSplashVersion>version && test==want) {
                    //User version is bigger than current - this means they've seen the splash
                    showSplashVersion = version+1;
                    //Rewrite the shared preference
                    Editor editor = myPreferences.edit();
                    editor.putInt("showSplashVersion", showSplashVersion);
                    editor.apply();
                    gotothesongs(null);
                    return;
                } else {
                    //Set the showSplashVersion to the next level - it will only show on next update
                    showSplashVersion = version+1;
                    //Rewrite the shared preference
                    Editor editor = myPreferences.edit();
                    editor.putInt("showSplashVersion", showSplashVersion);
                    editor.apply();
                }

                setContentView(R.layout.activity_splashscreen);

                setupToolbar();

                PackageInfo pinfo;
                int versionNumber = 0;
                String versionName = "?";
                try {
                    pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    versionNumber = pinfo.versionCode;
                    versionName = pinfo.versionName;
                } catch (NameNotFoundException e1) {
                    e1.printStackTrace();
                }

                TextView showVersion = (TextView) findViewById(R.id.version);
                String temptext = "V"+versionName+" ("+versionNumber+")";
                if (showVersion!=null) {
                    showVersion.setText(temptext);
                }

                mLayout = findViewById(R.id.page);

                if (test!=want) {
                    requestStoragePermission();
                    storageGranted=false;
                } else {
                    storageGranted=true;
                }

            }
        }, 1500); // 1500ms delay
    }

    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void gotothesongs(View view) {
        if (storageGranted) {
            Intent intent = new Intent();
            intent.setClass(this, FullscreenActivity.class);
            startActivity(intent);
            finish();
        } else {
            requestStoragePermission();
        }
    }

    public void webLink(View view) {
        String url = "http://www.opensong.org";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void latestUpdates(View view) {
        String url = "http://www.opensongapp.com/latest-updates";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void userGuide(View view) {
        String url = "http://www.opensongapp.com/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    // The permission requests
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(mLayout, R.string.storage_rationale, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestStorage);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestStorage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == requestStorage) {
            storageGranted = grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (storageGranted) {
                gotothesongs(mLayout);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}