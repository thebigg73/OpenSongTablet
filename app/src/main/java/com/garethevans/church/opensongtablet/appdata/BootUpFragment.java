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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BootupLogoBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songprocessing.Song;

//import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/*
This fragment is the first one that the main activity loads up.
It checks that we are clear to proceed - we have the required storage permissions and that we
have set it to a usable location.  If not, we get stuck on this page until we grant the permissions
and set the storage location.

When all is done, the user can click on the pulsating green start button and this will then move to
either the Performace or Presentation fragment.

Before it does that, it does a quick scan of the storage to make a very basic song menu available.
This will just contain the filenames and folders.  It does this by running the call found in the
MainActivity - the hub of the app!

The full indexing of the songs (lyrics, key, etc.) will be called on the MainActivity from either
the Performance/Presentation fragment.
*/

public class BootUpFragment extends Fragment {

    private Preferences preferences;
    private StorageAccess storageAccess;
    private Song song;
    private String message;
    private String uriTreeString;
    private Uri uriTree;

    private BootupLogoBinding myView;
    private MainActivityInterface mainActivityInterface;

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

        myView = BootupLogoBinding.inflate(inflater, container, false);

        // TODO
        // REMOVE BEFORE RELEASE!!!!!
        // MaterialShowcaseView.resetAll(requireActivity());

        // Initialise the helper classes
        initialiseHelpers();

        // Lock the navigation drawer and hide the actionbar and floating action button
        hideMenus();

        // Check we have the required storage permission
        startOrSetUp();

        return myView.getRoot();
    }

    private void initialiseHelpers() {
        // Load the helper classes (preferences)
        preferences = mainActivityInterface.getPreferences();
        storageAccess = mainActivityInterface.getStorageAccess();
        song = mainActivityInterface.getSong();
    }

    private void hideMenus() {
        mainActivityInterface.hideActionBar(true);
        mainActivityInterface.hideActionButton(true);
        mainActivityInterface.lockDrawer(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    // Checks made before starting the app
    private void startOrSetUp() {
        if (storageIsCorrectlySet()) {
            Log.d("BootUpFragment", "startBootProcess");
            startBootProcess();
        } else {
            Log.d("BootUpFragment", "requireStorageCheck");
            requireStorageCheck();
        }
    }
    private boolean storagePermissionGranted() {
        return (getContext()!=null && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }
    private boolean storageLocationSet() {
        uriTreeString = preferences.getMyPreferenceString(getContext(),"uriTree","");
        return !uriTreeString.isEmpty();
    }
    private boolean storageLocationValid() {
        uriTree = Uri.parse(uriTreeString);
        return storageAccess.uriTreeValid(requireActivity(),uriTree);
    }
    private boolean storageIsCorrectlySet() {
        // Check that storage permission is granted and that it has been set and that it exists
        return (storagePermissionGranted() && storageLocationSet() && storageLocationValid());
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
                updateMessage();

                // Get the last used song and folder.  If the song failed to load, reset to default
                setFolderAndSong();

                // Check for saved storage locations
                final String progress = storageAccess.createOrCheckRootFolders(getContext(), uriTree, preferences);
                boolean foldersok = !progress.contains("Error");

                if (foldersok) {
                    // Build the basic song index by scanning the songs and creating a songIDs file
                    message = getString(R.string.processing) + "\n" + getString(R.string.wait);
                    updateMessage();

                    mainActivityInterface.quickSongMenuBuild();

                    // Finished indexing
                    message = getString(R.string.success);
                    updateMessage();

                    mainActivityInterface.setMode(preferences.getMyPreferenceString(getContext(), "whichMode", "Performance"));

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

                            case "Presentation":
                                destination = R.id.presentationFragment;
                                break;
                        }
                        NavOptions navOptions = new NavOptions.Builder().
                                setPopUpTo(R.id.bootUpFragment,false).
                                build();
                        NavHostFragment.findNavController(this).navigate(destination,null,navOptions);
                        mainActivityInterface.initialiseActivity();
                    });

                } else {
                    // There was a problem with the folders, so restart the app!
                    Log.d("BootUpFragment", "problem with folders");
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
        song.setFolder(preferences.getMyPreferenceString(getContext(), "whichSongFolder",
                getString(R.string.mainfoldername)));

        song.setFilename(preferences.getMyPreferenceString(getContext(), "songfilename",
                getString(R.string.welcome)));

        if (!preferences.getMyPreferenceBoolean(getContext(),"songLoadSuccess",false)) {
            song.setFolder(getString(R.string.mainfoldername));
            preferences.setMyPreferenceString(getContext(),"whichSongFolder",song.getFolder());
            song.setFilename("Welcome to OpenSongApp");
            preferences.setMyPreferenceString(getContext(),"songfilename","Welcome to OpenSongApp");
        }
    }
}