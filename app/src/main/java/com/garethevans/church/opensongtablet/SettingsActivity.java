package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

//import static com.garethevans.church.opensongtablet.FullscreenActivity.myPreferences;

public class SettingsActivity extends AppCompatActivity implements PopUpStorageFragment.SettingsInterface,
PopUpDirectoryChooserFragment.SettingsInterface {

    // This class covers the splash screen and main settings page
    // Users then have the option to move into the FullscreenActivity

    // Let's define the variables needed for the Settings Page.
    Handler delayfadeinredraw;

    static int test;
    static int want;
    LinearLayout readUpdate;
    Button goToSongs;
    Button manageStorage;
    Button user_guide;
    File myroot;
    boolean showsplash;

    private static boolean storageGranted = false;
    private static final int requestStorage = 0;

    private View mLayout;

    // This class is called when the application first opens.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupMyShortCuts();

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            FullscreenActivity.version = pInfo.versionCode;
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
            FullscreenActivity.version = 0;
        }


        FullscreenActivity.myPreferences = getPreferences(MODE_PRIVATE);
        Log.d("d","SettingsActivity preload showSplashVersion="+FullscreenActivity.showSplashVersion);
        Preferences.loadPreferences();
        Log.d("d","SettingsActivity postload showSplashVersion="+FullscreenActivity.showSplashVersion);
        showsplash = getIntent().getBooleanExtra("showsplash",false);
        if (showsplash) {
            Log.d("d","intent to showsplash");
            FullscreenActivity.showSplashVersion = 0;
        }
        if (FullscreenActivity.showSplashVersion>FullscreenActivity.version) {
            showsplash=false;
        }
        // If version is pre v4 reset the gesture choices
        if (FullscreenActivity.showSplashVersion>0 && FullscreenActivity.showSplashVersion<120) {
            FullscreenActivity.resetSomePreferences = true;
        }

        // Decide if user has already seen the splash screenLog.d("d","SettingsActivity showSplashVersion="+FullscreenActivity.showSplashVersion);
        Log.d("d","SettingsActivity version="+FullscreenActivity.version);

        //myPreferences = getSharedPreferences("mysettings", MODE_PRIVATE);
        //showSplashVersion = myPreferences.getInt("showSplashVersion", version);


        //indexSongPreferences = getSharedPreferences("indexsongs",MODE_PRIVATE);


        // Set up the folders
        // Load up the user preferences
        //FullscreenActivity.myPreferences = getPreferences(MODE_PRIVATE);
        //Preferences.loadPreferences();

        // We may have arrived via a shortcut (Nougat+)
        String newMode = "";
        try {
            if (getIntent() != null && getIntent().getStringExtra("whichMode") != null) {
                newMode = getIntent().getStringExtra("whichMode");
            }
        } catch (Exception e) {
            // Oops
            e.printStackTrace();
        }
        if (newMode != null && (newMode.equals("Performance") || newMode.equals("Stage") || newMode.equals("Presentation"))) {
            FullscreenActivity.whichMode = newMode;
            Log.d("d", "newMode=" + newMode);
            Preferences.savePreferences();
        }

        setContentView(R.layout.activity_logosplash);

        mLayout = findViewById(R.id.pagesplash);
        test = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        want = PackageManager.PERMISSION_GRANTED;
        storageGranted = test == want;

        boolean other_works = tryCustom();
        boolean ext_works = tryExternal();

        switch (FullscreenActivity.prefStorage) {
            case "other":
                if (other_works) {
                    myroot = new File(FullscreenActivity.customStorage);
                } else if (ext_works) {
                    myroot = new File(System.getenv("SECONDARY_STORAGE"));
                    FullscreenActivity.prefStorage = "ext";
                } else {
                    myroot = new File(Environment.getExternalStorageDirectory() + "/documents/");
                    FullscreenActivity.prefStorage = "int";
                }
                break;
            case "ext":
                if (ext_works) {
                    myroot = new File(System.getenv("SECONDARY_STORAGE"));
                    FullscreenActivity.prefStorage = "ext";
                } else {
                    myroot = new File(Environment.getExternalStorageDirectory() + "/documents/");
                    FullscreenActivity.prefStorage = "int";
                }
                break;
            case "int":
            default:
                myroot = new File(Environment.getExternalStorageDirectory() + "/documents/");
                break;
        }

        PopUpStorageFragment.getOtherFolders(myroot);
        final boolean storageexists = PopUpStorageFragment.checkBasicDirectoriesExistOnly();
        PopUpStorageFragment.wipeExportFolder();

        delayfadeinredraw = new Handler();
        delayfadeinredraw.postDelayed(new Runnable() {
            @Override
            public void run() {
                // This bit then redirects the user to the main app if they've got the newest version

                if (!showsplash && test == want && storageexists) {
                    //User version is bigger than current - this means they've seen the splash
                    FullscreenActivity.showSplashVersion = FullscreenActivity.version + 1;
                    Preferences.savePreferences();
                    gotothesongs();
                    return;
                } else {
                    //Set the showSplashVersion to the next level - it will only show on next update
                    FullscreenActivity.showSplashVersion = FullscreenActivity.version + 1;
                    Preferences.savePreferences();
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
                String temptext = "V" + versionName + " (" + versionNumber + ")";
                if (showVersion != null) {
                    showVersion.setText(temptext);
                }

                readUpdate = (LinearLayout) findViewById(R.id.readUpdate);
                readUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        latestUpdates();
                    }
                });

                goToSongs = (Button) findViewById(R.id.goToSongs);
                manageStorage = (Button) findViewById(R.id.manageStorage);
                manageStorage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openStorageFragment();
                    }
                });
                recheckStorage();

                user_guide = (Button) findViewById(R.id.user_guide);
                user_guide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userGuide();
                    }
                });

                TextView weblink = (TextView) findViewById(R.id.webLink);
                weblink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        webLink();
                    }
                });
                mLayout = findViewById(R.id.page);

                if (test != want) {
                    requestStoragePermission();
                    storageGranted = false;
                } else {
                    storageGranted = true;
                }
            }

        }, 1500); // 1500ms delay
    }

    // This is for Nougat+ when users can long press on the launcher to quick open a mode
    private void setupMyShortCuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClass(SettingsActivity.this, SettingsActivity.class);

            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            ShortcutInfo shortcut1 = new ShortcutInfo.Builder(this, "performance")
                    .setShortLabel(getResources().getString(R.string.perform))
                    .setLongLabel(getResources().getString(R.string.performancemode))
                    .setIcon(Icon.createWithResource(SettingsActivity.this, R.drawable.microphone_variant))
                    .setIntent(intent.putExtra("whichMode","Performance"))
                    .build();
            ShortcutInfo shortcut2 = new ShortcutInfo.Builder(this, "stage")
                    .setShortLabel(getResources().getString(R.string.stage))
                    .setLongLabel(getResources().getString(R.string.stagemode))
                    .setIcon(Icon.createWithResource(SettingsActivity.this, R.drawable.lan_connect))
                    .setIntent(intent.putExtra("whichMode","Stage"))
                    .build();
            ShortcutInfo shortcut3 = new ShortcutInfo.Builder(this, "present")
                    .setShortLabel(getResources().getString(R.string.present))
                    .setLongLabel(getResources().getString(R.string.presentermode))
                    .setIcon(Icon.createWithResource(SettingsActivity.this, R.drawable.projector))
                    .setIntent(intent.putExtra("whichMode","Presentation"))
                    .build();
            shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut3,shortcut2, shortcut1));
        }
    }

    private boolean tryCustom() {
        boolean works;
        try {
            new File(FullscreenActivity.customStorage);
            works = true;
        } catch (Exception e) {
            works = false;
        }
        return works;
    }

    private boolean tryExternal() {
        boolean works;
        try {
            new File(System.getenv("SECONDARY_STORAGE"));
            works = true;
        } catch (Exception e) {
            works = false;
        }
        return works;
    }

    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void gotothesongs() {
        if (storageGranted) {
            Preferences.savePreferences();
            Intent intent = new Intent();
            intent.setClass(this, FullscreenActivity.class);
            startActivity(intent);
            finish();
        } else {
            requestStoragePermission();
        }
    }

    public void webLink() {
        String url = "http://www.opensongapp.com";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void latestUpdates() {
        String url = "http://www.opensongapp.com/latest-updates";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void userGuide() {
        String url = "http://www.opensongapp.com/user-guide";
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
        recheckStorage();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == requestStorage) {
            storageGranted = grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (storageGranted) {
                recheckStorage();
            } else {
                recheckStorage();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            recheckStorage();
        }
    }

    @Override
    public void openStorageFragment() {
        FullscreenActivity.whattodo = "splashpagestorage";
        DialogFragment newFragment = PopUpStorageFragment.newInstance();
        newFragment.show(getFragmentManager(), "splashpagestorage");
    }

    @Override
    public void recheckStorage() {
        // This is called when the storage fragment has been closed
        boolean storageexists = PopUpStorageFragment.checkDirectoriesExistOnly();
        if (storageexists) {
            goToSongs.setText(getResources().getString(R.string.gotosong));
            goToSongs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gotothesongs();
                }
            });
        } else if (storageGranted){
            goToSongs.setText(getResources().getString(R.string.storage_help));
            goToSongs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openStorageFragment();
                }
            });
        } else {
            goToSongs.setText(getResources().getString(R.string.storage_help));
            goToSongs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestStoragePermission();
                }
            });
        }
    }

    @Override
    public void selectStorage() {
        FullscreenActivity.whattodo = "splashpagestorage";
        DialogFragment newFragment = PopUpDirectoryChooserFragment.newInstance();
        Bundle args = new Bundle();
        args.putString("type", "folder");
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "splashpagestorage");
    }
}