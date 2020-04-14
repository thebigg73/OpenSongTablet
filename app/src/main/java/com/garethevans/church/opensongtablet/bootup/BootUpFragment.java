package com.garethevans.church.opensongtablet.bootup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavHost;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.SQLiteHelper;
import com.garethevans.church.opensongtablet.SetTypeFace;
import com.garethevans.church.opensongtablet.StaticVariables;
import com.garethevans.church.opensongtablet.StorageAccess;
import com.garethevans.church.opensongtablet.databinding.BootupLogoBinding;

import java.util.ArrayList;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.make;

public class BootUpFragment extends Fragment {

    private Preferences preferences;
    private StorageAccess storageAccess;
    private SetTypeFace setTypeFace;
    private String initialising, message;
    private Uri uriTree;

    private BootupLogoBinding myView;

    private Bundle bundle;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        StaticVariables.homeFragment = false;  // Set to true for Performance/Stage/Presentation only

        bundle = savedInstanceState;

        myView = BootupLogoBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        // Initialise the helper classes
        initialiseHelpers();

        initialising = "Initialising: ";

        // Check we have the required storage permission
        checkStoragePermission();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    private void initialiseHelpers() {
        // Load the helper classes (preferences)
        preferences = new Preferences();
        storageAccess = new StorageAccess();
        setTypeFace = new SetTypeFace();
        StaticVariables.activity = getActivity();
    }

    private void checkPreferencesForStorage() {
        String uT  = preferences.getMyPreferenceString(getActivity(),"uriTree","");
        if (!uT.equals("")) {
            uriTree = Uri.parse(uT);
        } else {
            uriTree = null;
        }
    }

    private void setFolderAndSong() {
        StaticVariables.whichSongFolder = preferences.getMyPreferenceString(getActivity(), "whichSongFolder",
                getString(R.string.mainfoldername));
        StaticVariables.songfilename = preferences.getMyPreferenceString(getActivity(), "songfilename",
                "Welcome to OpenSongApp");

        // Check if we have used the app already, but the last song didn't load
        if (!preferences.getMyPreferenceBoolean(getActivity(),"songLoadSuccess",false)) {
            StaticVariables.whichSongFolder = getString(R.string.mainfoldername);
            preferences.setMyPreferenceString(getActivity(),"whichSongFolder",StaticVariables.whichSongFolder);
            StaticVariables.songfilename = "Welcome to OpenSongApp";
            preferences.setMyPreferenceString(getActivity(),"songfilename",StaticVariables.songfilename);
        }
    }

    private void checkStoragePermission() {
        if (getActivity()!=null && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permission has not been granted.
            requestStoragePermission();
        } else {
            startBootProcess();
        }
    }
    private void requestStoragePermission() {
        if (getActivity()!=null && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                make(getActivity().findViewById(R.id.pagesplash), R.string.storage_rationale,
                        LENGTH_INDEFINITE).setAction(R.string.ok, view -> {
                            if (getActivity()!=null) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                            }
                        }).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (getActivity()!=null) {
            try {
                // Storage permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            startBootProcess();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startBootProcess() {
        // Start the boot process

        Handler lyrichandler = new Handler();
        Handler chordhandler = new Handler();
        Handler stickyhandler = new Handler();
        Handler presohandler = new Handler();
        Handler presoinfohandler = new Handler();
        Handler customhandler = new Handler();

        if (getActivity() != null) {
            new Thread(() -> {
                Log.d("BootUpFragment", "run()");
                // Set up the Typefaces
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        message = initialising + getString(R.string.choose_fonts);
                        myView.currentAction.setText(message);
                    });
                }
                Looper.prepare();

                setTypeFace.setUpAppFonts(getActivity(), preferences, lyrichandler, chordhandler,
                        stickyhandler, presohandler, presoinfohandler, customhandler);

                message = initialising + getString(R.string.storage);

                Log.d("BootUpFragment", message);

                // Check for saved storage locations
                getActivity().runOnUiThread(() -> myView.currentAction.setText(message));
                checkPreferencesForStorage();
                setFolderAndSong();

                final String progress = storageAccess.createOrCheckRootFolders(getActivity(), uriTree, preferences);
                boolean foldersok = !progress.contains("Error");

                if (foldersok) {

                    ArrayList<String> songIds = new ArrayList<>();
                    try {
                        songIds = storageAccess.listSongs(getActivity(), preferences);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int numSongs = songIds.size();
                    message = numSongs + " " + getString(R.string.processing) + "\n" + getString(R.string.wait);
                    Log.d("BootUpFragment", message);

                    getActivity().runOnUiThread(() -> myView.currentAction.setText(message));

                    // Write a crude text file (line separated) with the song Ids (folder/file)
                    storageAccess.writeSongIDFile(getActivity(), preferences, songIds);

                    // Try to create the basic databases
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
                    sqLiteHelper.resetDatabase(getActivity());
                    NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getActivity());
                    nonOpenSongSQLiteHelper.initialise(getActivity(), storageAccess, preferences);

                    // Add entries to the database that have songid, folder and filename fields
                    // This is the minimum that we need for the song menu.
                    // It can be upgraded asynchronously in StageMode/PresenterMode to include author/key
                    // Also will later include all the stuff for the search index as well
                    sqLiteHelper.insertFast(getActivity(), storageAccess);

                    // Finished indexing
                    message = getString(R.string.success);
                    Log.d("BootUpFragment", message);

                    // Check for saved storage locations
                    getActivity().runOnUiThread(() -> myView.currentAction.setText(message));

                    StaticVariables.whichMode = preferences.getMyPreferenceString(getActivity(), "whichMode", "Performance");

                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_boot, true)
                            .build();

                    getActivity().runOnUiThread(() -> {
                        switch (StaticVariables.whichMode) {
                            case "Performance":
                            case "Stage":
                            default:
                                NavHostFragment.findNavController(BootUpFragment.this)
                                        .navigate(R.id.nav_performance,bundle,navOptions);
                                break;

                            case "Presentation":
                                NavHostFragment.findNavController(BootUpFragment.this)
                                        .navigate(R.id.nav_presentation,bundle,navOptions);
                                break;
                        }
                    });
                } else {
                    // There was a problem with the folders, so restart the app!
                    Log.d("BootUpFragment", "problem with folders");
                    getActivity().recreate();
                }
            }).start();
        }
    }
}