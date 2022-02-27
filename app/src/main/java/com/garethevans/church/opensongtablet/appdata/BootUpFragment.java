package com.garethevans.church.opensongtablet.appdata;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BootupLogoBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

/*
This fragment is the first one that the main activity loads up.
It checks that we are clear to proceed - we have the required storage permissions and that we
have set it to a usable location.  If not, we get stuck on this page until we grant the permissions
and set the storage location.

When all is done, the user can click on the pulsating green start button and this will then move to
either the Performace or Presenter fragment.

Before it does that, it does a quick scan of the storage to make a very basic song menu available.
This will just contain the filenames and folders.  It does this by running the call found in the
MainActivity - the hub of the app!

The full indexing of the songs (lyrics, key, etc.) will be called on the MainActivity from either
the Performance/Presenter fragment.
*/

public class BootUpFragment extends Fragment {

    private final String TAG = "BootUpFragment";
    private String message;
    private String uriTreeString;
    private Uri uriTree;
    private boolean hide;

    private BootupLogoBinding myView;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = BootupLogoBinding.inflate(inflater, container, false);
        mainActivityInterface.registerFragment(this,"BootUpFragment");

        // TODO
        // REMOVE BEFORE RELEASE!!!!!
        //MaterialShowcaseView.resetAll(requireActivity());

        // Lock the navigation drawer and hide the actionbar and floating action button
        hideMenus();

        myView.splashlogo.setOnClickListener(view -> {
            hide = !hide;
            mainActivityInterface.getAppActionBar().translateAwayActionBar(hide);
        });
        startOrSetUp();
        return myView.getRoot();
    }

    private void hideMenus() {
        mainActivityInterface.hideActionButton(true);
        mainActivityInterface.lockDrawer(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.getAppActionBar().translateAwayActionBar(true);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    // Checks made before starting the app
    public void startOrSetUp() {
        Log.d(TAG,"startOrSetUp");
        if (storageIsCorrectlySet()) {
            startBootProcess();
        } else {
            requireStorageCheck();
        }
    }
    private boolean storageIsCorrectlySet() {
        // Check that storage permission is granted and that it has been set and that it exists
        return (storagePermissionGranted() && storageLocationSet() && storageLocationValid());
    }
    private boolean storagePermissionGranted() {
        return (getContext()!=null && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }
    private boolean storageLocationSet() {
        uriTreeString = mainActivityInterface.getPreferences().
                    getMyPreferenceString("uriTree", "");
        return !uriTreeString.isEmpty();
    }
    private boolean storageLocationValid() {
        uriTree = Uri.parse(uriTreeString);
        return mainActivityInterface.getStorageAccess().uriTreeValid(uriTree);
    }

    private void requireStorageCheck() {
        // Either permission hasn't been granted, or it isn't set properly
        // Switch to the set storage fragment
        NavHostFragment.findNavController(BootUpFragment.this).navigate(Uri.parse("opensongapp://settings/storage/setstorage"));
    }

    private void startBootProcess() {
        // Start the boot process
        if (getContext() != null) {
            new Thread(() -> {
                // Tell the user we're initialising the storage
                message = getString(R.string.processing) + ": " + getString(R.string.storage);
                mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
                updateMessage();

                // Get the last used song and folder.  If the song failed to load, reset to default
                setFolderAndSong();

                // Check for saved storage locations
                final String progress = mainActivityInterface.getStorageAccess().
                        createOrCheckRootFolders(uriTree);
                boolean foldersok = !progress.contains("Error");

                if (foldersok) {
                    // Build the basic song index by scanning the songs and creating a songIDs file
                    message = getString(R.string.processing) + "\n" + getString(R.string.wait);
                    updateMessage();

                    mainActivityInterface.quickSongMenuBuild();

                    // Finished indexing
                    message = getString(R.string.success);
                    updateMessage();

                    mainActivityInterface.setMode(mainActivityInterface.getPreferences().getMyPreferenceString("whichMode", "Performance"));

                    // Increase the boot times for prompting a user to backup their songs
                    int runssincebackup = mainActivityInterface.getPreferences().getMyPreferenceInt("runssincebackup",0);
                    mainActivityInterface.getPreferences().setMyPreferenceInt("runssincebackup", runssincebackup+1);

                    // Set up the rest of the main activity
                    requireActivity().runOnUiThread(() -> {
                        // Load up the correct Fragment
                        int destination;
                        switch (mainActivityInterface.getMode()) {
                            case "Performance":
                            case "Stage":
                            default:
                                destination = R.id.performanceFragment;
                                break;

                            case "Presenter":
                                destination = R.id.presenterFragment;
                                break;
                        }
                        NavOptions navOptions = new NavOptions.Builder().
                                setPopUpTo(R.id.bootUpFragment,false).
                                build();
                        NavHostFragment.findNavController(this).navigate(destination,null,navOptions);
                        mainActivityInterface.getAppActionBar().translateAwayActionBar(false);
                        mainActivityInterface.initialiseActivity();
                    });

                } else {
                    // There was a problem with the folders, so restart the app!
                    Log.d(TAG, "Problem with app folders - restarting");
                    requireActivity().recreate();
                }
            }).start();
        }
    }

    // If the fragment is still attached, display the update message
    private void updateMessage() {
        if (getActivity()!=null && getContext()!=null) {
            requireActivity().runOnUiThread(() -> myView.currentAction.setText(message));
        }
    }

    // Get the last used folder/filename or reset if it didn't load
    private void setFolderAndSong() {
        mainActivityInterface.getSong().setFolder(mainActivityInterface.getPreferences().getMyPreferenceString("whichSongFolder",
                getString(R.string.mainfoldername)));

        mainActivityInterface.getSong().setFilename(mainActivityInterface.getPreferences().getMyPreferenceString("songfilename",
                getString(R.string.welcome)));

        if (!mainActivityInterface.getPreferences().getMyPreferenceBoolean("songLoadSuccess",false)) {
            mainActivityInterface.getSong().setFolder(getString(R.string.mainfoldername));
            mainActivityInterface.getPreferences().setMyPreferenceString("whichSongFolder",mainActivityInterface.getSong().getFolder());
            mainActivityInterface.getSong().setFilename("Welcome to OpenSongApp");
            mainActivityInterface.getPreferences().setMyPreferenceString("songfilename","Welcome to OpenSongApp");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myView = null;
    }
}