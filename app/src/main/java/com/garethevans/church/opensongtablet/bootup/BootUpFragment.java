package com.garethevans.church.opensongtablet.bootup;

import android.Manifest;
import android.content.Context;
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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.SetTypeFace;
import com.garethevans.church.opensongtablet.databinding.BootupLogoBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.util.ArrayList;

public class BootUpFragment extends Fragment {

    private Preferences preferences;
    private StorageAccess storageAccess;
    private SetTypeFace setTypeFace;
    private String initialising, message;
    private String uT;
    private Uri uriTree;

    private BootupLogoBinding myView;
    private MainActivityInterface mainActivityInterface;
    private Bundle bundle;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityInterface.hideActionBar(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // TODO
        // REMOVE BEFORE RELEASE!!!!!
        //MaterialShowcaseView.resetAll(requireActivity());

        StaticVariables.homeFragment = false;  // Set to true for Performance/Stage/Presentation only

        bundle = savedInstanceState;

        myView = BootupLogoBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        // Initialise the helper classes
        initialiseHelpers();

        initialising = "Initialising: ";

        // Check we have the required storage permission
        startOrSetUp();

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

    // Checks made before starting the app
    private void startOrSetUp() {
        if (storageIsCorrectlySet()) {
            startBootProcess();
        } else {
            requireStorageCheck();
        }
    }
    private boolean storagePermissionGranted() {
        return (getActivity()!=null && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }
    private boolean storageLocationSet() {
        uT = preferences.getMyPreferenceString(getActivity(),"uriTree","");
        return !uT.isEmpty();
    }
    private boolean storageLocationValid() {
        uriTree = Uri.parse(uT);
        return storageAccess.uriTreeValid(requireActivity(),uriTree);
    }
    private boolean storageIsCorrectlySet() {
        // Check that storage permission is granted and that it has been set and that it exists
        return (storagePermissionGranted() && storageLocationSet() && storageLocationValid());
    }

    private void requireStorageCheck() {
        // Either permission hasn't been granted, or it isn't set properly
        // Switch to the set storage fragment
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_boot, true)
                .build();
        NavHostFragment.findNavController(BootUpFragment.this)
                .navigate(R.id.action_nav_boot_to_nav_storage,bundle,navOptions);
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


                getActivity().runOnUiThread(() -> {


                    // Tell the user we're initialising the storage
                    myView.currentAction.setText(message);
                });
                setFolderAndSong();

                // Check for saved storage locations
                final String progress = storageAccess.createOrCheckRootFolders(getActivity(), uriTree, preferences);
                boolean foldersok = !progress.contains("Error");

                if (foldersok) {

                    // Get the songIds  these are basically folder/file pairs for proper indexing later
                    // These are stored to a temporary file in the app storage folder
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
                    getActivity().runOnUiThread(() -> myView.currentAction.setText(message));

                    StaticVariables.whichMode = preferences.getMyPreferenceString(getActivity(), "whichMode", "Performance");

                    getActivity().runOnUiThread(() -> mainActivityInterface.initialiseActivity());
                } else {
                    // There was a problem with the folders, so restart the app!
                    Log.d("BootUpFragment", "problem with folders");
                    getActivity().recreate();
                }
            }).start();
        }
    }
}