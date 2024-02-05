package com.garethevans.church.opensongtablet.filemanagement;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.make;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.databinding.StorageChooseBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/*
This fragment is used to set the storage location for the app.  It deals with the permissions for
using the external storage and allows the user to use the built in picker to choose the location
For Lollipop+, this is a URI TREE, for KitKat, it is just a location.
The app stores this (and permissions granted for later use of any files/folders attached to this location.

It is called under the following conditions:
* the first boot if the storage has never been set
* normal booting if there is an issue with the currently set storage
* the user has specified that they want to change the storage location
*/

public class SetStorageLocationFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private Uri uriTree, uriTreeHome;
    private ArrayList<String> locations;
    private File folder;
    private final String TAG = "SetStorageLocFrag";
    private String storage_reset_string="";
    private String start_string="";
    private String existing_found_string="";
    private String storage_ext_string="";
    private String mainfoldername_string="";
    private String deeplink_bootup_string="";
    private String storage_notwritable_string="";
    private String storage_change_string="";
    ActivityResultLauncher<Intent> folderChooser;
    ActivityResultLauncher<String> storagePermission;
    private ImageView screenHelp;

    private StorageChooseBinding myView;
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.setSettingsOpen(true);
        mainActivityInterface.updateToolbarHelp(webAddress);
        mainActivityInterface.updateToolbar(storage_change_string);
        checkToolbarView();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = StorageChooseBinding.inflate(inflater, container, false);

        prepareStrings();

        // Set up the views
        initialiseViews();

        // Set up intent listeners for permissions and folder choices
        setUpIntentListeners();

        // Check preferences for storage (if they exist)
        getUriTreeFromPreferences();

        // If we are sent here from the kitkat chooser, deal with that
        kitKatDealWithUri();

        // Show the current location
        showStorageLocation();

        // Check we have the required storage permission
        // If we have it, this will update the text, if not it will ask for permission
        checkStatus();
        checkToolbarView();

        // Showcase
        storageShowcase();

        return myView.getRoot();
    }

    private void checkToolbarView() {
        mainActivityInterface.setSettingsOpen(true);
        screenHelp = mainActivityInterface.disableActionBarStuff(!mainActivityInterface.getWhattodo().equals("storageOk"));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mainActivityInterface.updateToolbarHelp(webAddress);
            mainActivityInterface.updateToolbar(storage_change_string);
            screenHelp = mainActivityInterface.disableActionBarStuff(!mainActivityInterface.getWhattodo().equals("storageOk"));
        },100);
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            String website_storage_set_string = getString(R.string.website_storage_set);
            storage_reset_string = getString(R.string.storage_reset);
            start_string = getString(R.string.start);
            existing_found_string = getString(R.string.existing_found);
            storage_ext_string = getString(R.string.storage_ext);
            mainfoldername_string = getString(R.string.mainfoldername);
            deeplink_bootup_string = getString(R.string.deeplink_bootup);
            storage_notwritable_string = getString(R.string.storage_notwritable);
            storage_change_string = getString(R.string.storage_change);
            webAddress = website_storage_set_string;
        }
    }
    private void storageShowcase() {
        // Wait 1 second for all views
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity()!=null) {
                MaterialShowcaseView.Builder builder = mainActivityInterface.getShowCase().
                        getSingleShowCaseBuilderForListener(getActivity(), myView.setStorage,
                                null, storage_reset_string, true, "storageReset");
                builder.setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        // Try to show the help showcase
                        try {
                            if (screenHelp != null && mainActivityInterface!=null && getActivity()!=null) {
                                mainActivityInterface.getShowCase().singleShowCase(getActivity(), screenHelp, null, getString(R.string.help), false, "webHelp");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.build().show(getActivity());
            }
        },1000);

    }
    private void startShowcase() {
        if (myView.startApp.getVisibility()==View.VISIBLE && getActivity()!=null) {
            mainActivityInterface.getShowCase().singleShowCase(getActivity(), myView.startApp,
                    null, start_string, true, "startApp");
        }
    }

    private void initialiseViews() {
        // Lock the menu and hide the actionbar and action button
        if (mainActivityInterface.getSettingsOpen()) {
            mainActivityInterface.lockDrawer(true);
            mainActivityInterface.hideActionButton(true);
        }

        // Set up the storage location currently set in an edit box that acts like a button only
        myView.chosenLocation.setFocusable(false);
        myView.chosenLocation.setClickable(true);
        myView.chosenLocation.setMaxLines(4);
        myView.chosenLocation.setOnClickListener(t -> myView.setStorage.performClick());

        // Set the listeners for the buttons
        myView.infoButton.setOnClickListener(v -> {
            if (getActivity()!=null) {
                BottomSheetDialogFragment dialog = new SetStorageBottomSheet();
                dialog.show(getActivity().getSupportFragmentManager(), "SetStorageBottomSheet");
            }
        });
        myView.setStorage.setOnClickListener(v -> chooseStorageLocation());
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // Hide the previous location button as we can't use it without full manage storage control
            // Don't want the hassle of requesting this.
            myView.findStorage.setVisibility(View.GONE);
        } else {
            myView.findStorage.setOnClickListener(v -> startSearch());
        }
        myView.startApp.setOnClickListener(v -> goToSongs());
    }

    private void setUpIntentListeners() {
        folderChooser = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        // This is only called when using lollipop or later
                        lollipopDealWithUri(result.getData());

                        // Save the location uriTree and uriTreeHome
                        saveUriLocation();

                        // Update the storage text
                        showStorageLocation();

                        // See if we can show the start button yet
                        checkStatus();
                    }
                });

        storagePermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission is granted.
                        Log.d(TAG,"permission is granted");
                        checkStatus();
                        showStorageLocation();
                        myView.setStorage.performClick();
                    } else {
                        Log.d(TAG,"permission not granted");
                        uriTreeHome = null;
                        showStorageLocation();
                    }
                });
    }

    // Deal with the storage location set/chosen
    private void getUriTreeFromPreferences() {
        /*
        The user could have specified the actual OpenSong folder (if it already existed),
        or the location to create the OpenSong folder in on first run.  uriTree is the one we have
        permission for and could be either.  If it is the parent folder, uriTreeHome becomes the
        actual child OpenSong folder.  If it is the OpenSong folder, uriTree = uriTreeHome
        Also it could be null!
        */

        String uriTree_String = mainActivityInterface.getPreferences().getMyPreferenceString("uriTree", "");

        if (uriTree_String!=null && !uriTree_String.equals("")) {
            if (mainActivityInterface.getStorageAccess().lollipopOrLater()) {
                uriTree = Uri.parse(uriTree_String);
            } else {
                uriTree = Uri.fromFile(new File(uriTree_String));
            }
        } else {
            uriTree = null;
            uriTreeHome = null;
        }
        if (uriTree!=null && uriTreeHome==null) {
            uriTreeHome = mainActivityInterface.getStorageAccess().homeFolder(uriTree);
        }
    }

    private void warningCheck() {
        // If the user tries to set the app storage to OpenSong/Songs/ warn them!
        if (myView.chosenLocation.getText().toString().contains("OpenSong/Songs/") && getActivity()!=null) {
            Snackbar snackbar = make(getActivity().findViewById(R.id.drawer_layout), R.string.storage_warning,
                    LENGTH_INDEFINITE).setAction(android.R.string.ok, view -> {
            });
            View snackbarView = snackbar.getView();
            TextView snackTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(4);
            snackbar.show();
        }
    }
    private void saveUriLocation() {
        Log.d(TAG,"uriTree="+uriTree);
        Log.d(TAG,"uriTreeHome="+uriTreeHome);
        if (uriTree!=null) {
            // Save the preferences
            // If we are using KitKat, we have to parse the storage
            String uriTree_String = uriTree.toString();
            if (uriTreeHome==null) {
                uriTreeHome = mainActivityInterface.getStorageAccess().homeFolder(uriTree);
            }
            String uriTreeHome_String = uriTreeHome.toString();
            uriTree_String = uriTree_String.replace("file://","");
            uriTreeHome_String = uriTreeHome_String.replace("file://","");
            mainActivityInterface.getPreferences().setMyPreferenceString("uriTree", uriTree_String);
            mainActivityInterface.getPreferences().setMyPreferenceString("uriTreeHome", uriTreeHome_String);
            mainActivityInterface.getStorageAccess().setUriTreeHome(uriTreeHome);
        } else {
            mainActivityInterface.getPreferences().setMyPreferenceString("uriTree", "");
            mainActivityInterface.getPreferences().setMyPreferenceString("uriTreeHome", "");
        }
    }

    // Stuff to search for previous installation locations
    private void startSearch() {
        // Deactivate the stuff we shouldn't click on while it is being prepared
        setEnabledOrDisabled(false);

        // Initialise the available storage locations
        locations = new ArrayList<>();
        findLocations();
    }
    private void findLocations() {
        // Run in a new thread
        // This is only for KitKat.  Not allowed for newer versions without
        // Manage Storage permissions, which is overkill!
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Go through the directories recursively and add them to an arraylist
            folder = new File("/storage");
            walkFiles(folder);

            folder = Environment.getExternalStorageDirectory();
            walkFiles(folder);

            // Set up the file list, as long as the user wasn't bored and closed the window!
            mainActivityInterface.getMainHandler().post(() -> {
                if (locations != null) {
                    // Hide the  and reenable stuff
                    setEnabledOrDisabled(true);

                    if (locations.size() < 1) {
                        // No previous installations found
                        myView.previousStorageHeading.setVisibility(View.VISIBLE);
                        myView.previousStorageTextView.setVisibility(View.GONE);
                        myView.previousStorageLocations.setVisibility(View.GONE);
                        myView.previousStorageLocations.removeAllViews();
                    } else {
                        myView.previousStorageHeading.setVisibility(View.GONE);
                        myView.previousStorageTextView.setText(existing_found_string);
                        myView.previousStorageTextView.setVisibility(View.VISIBLE);
                        myView.previousStorageLocations.setVisibility(View.VISIBLE);
                        myView.previousStorageLocations.removeAllViews();
                        StringBuilder check = new StringBuilder();
                        // Add the locations to the textview
                        if (getContext()!=null) {
                            for (int x = 0; x < locations.size(); x++) {
                                Log.d(TAG, "check:" + check + "  locations.get(" + x + "):" + locations.get(x));
                                if (!check.toString().contains("¬" + locations.get(x) + "¬")) {
                                    check.append("¬").append(locations.get(x)).append("¬");
                                    TextView tv = new TextView(getContext());
                                    tv.setText(locations.get(x));
                                    myView.previousStorageLocations.addView(tv);
                                }
                            }
                        }
                    }
                }
            });

        });
    }
    private void walkFiles(File root) {

        if (root!=null && root.exists() && root.isDirectory()) {
            Log.d(TAG,"root:"+root);

            File[] list = root.listFiles();
            if (list != null) {
                for (File f : list) {
                    if (f.isDirectory()) {
                        String where = f.getAbsolutePath();
                        String extra;
                        displayWhere(where);
                        Log.d(TAG,"root:"+root+"  f:"+f);

                        if (!where.contains(".estrongs") && !where.contains("com.ttxapps") && where.endsWith("/OpenSong/Songs")) {
                             int count = mainActivityInterface.getStorageAccess().songCountAtLocation(f);
                             extra = count + " Songs";
                             // Found one and it isn't in eStrongs recycle folder or the dropsync temp files!
                             // IV - Add  a leading ¬ and remove trailing /Songs
                             where = "¬" + where.substring(0, where.length() - 6);
                             // IV - For paths identified as Internal storage (more patterns may be needed) remove the parent folder
                             where = where.
                                     replace("¬/storage/sdcard0/", "/").
                                     replace("¬/storage/emulated/0/", "/").
                                     replace("¬/storage/emulated/legacy/", "/").
                                     replace("¬/storage/self/primary/", "/");
                             if (where.startsWith("¬")) {
                                 // IV - Handle other paths as 'External'
                                 where = where.substring(10);
                                 extra = extra + ", " + storage_ext_string + " " + where.substring(0, where.indexOf("/"));
                                 where = where.substring(where.indexOf("/"));
                             }
                             where = "(" + extra + "): " + where;
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
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> myView.previousStorageTextView.setText(str));
    }

    // Deal with allowing or hiding the start button
    private void checkStatus() {
        if (mainActivityInterface.getAppPermissions().hasStoragePermissions() && isStorageSet() && isStorageValid()) {
            myView.firstRun.setVisibility(View.GONE);
            myView.startApp.setVisibility(View.VISIBLE);
            pulseButton(myView.startApp);
            startShowcase();
            myView.setStorage.clearAnimation();
            // After an attempt to change storage, set to show Welcome song
            mainActivityInterface.getSong().setFolder(mainfoldername_string);
            mainActivityInterface.getSong().setFilename("Welcome to OpenSongApp");
            mainActivityInterface.getPreferences().setMyPreferenceString("songFolder",mainfoldername_string);
            mainActivityInterface.getPreferences().setMyPreferenceString("songFilename","Welcome to OpenSongApp");

        } else {
            myView.firstRun.setVisibility(View.VISIBLE);
            myView.startApp.clearAnimation();
            myView.startApp.setVisibility(View.GONE);
            myView.setStorage.setVisibility(View.VISIBLE);
            pulseButton(myView.setStorage);
            uriTreeHome = null;
        }
    }
    private void setEnabledOrDisabled(boolean what) {
        myView.startApp.setEnabled(what);
        myView.setStorage.setEnabled(what);
        myView.findStorage.setEnabled(what);

        if (!what) {
            myView.previousStorageTextView.setVisibility(View.VISIBLE);
        } else {
            myView.previousStorageTextView.setVisibility(View.GONE);
        }
    }
    private void goToSongs() {
        mainActivityInterface.updateToolbar("");
        mainActivityInterface.setSettingsOpen(false);
        mainActivityInterface.navigateToFragment(deeplink_bootup_string,0);
    }
    private void pulseButton(View v) {
        if (getActivity()!=null) {
            CustomAnimation ca = new CustomAnimation();
            ca.pulse(getActivity(), v);
        }
    }

    // Below are some checks that will be called to see if we are good to go
    // Firstly check if we have granted the storage permission.  This is the first check
    private void checkStoragePermission() {
        if (mainActivityInterface.getAppPermissions().hasStoragePermissions()) {
            // Permission has been granted, so set the storage location
            showStorageLocation();

        } else {
            // Storage permission has not been granted.  Launch the request to allow it
            Log.d(TAG,"Doesn't have permission");
            //storagePermission.launch(mainActivityInterface.getAppPermissions().getStoragePermissions());
        }
    }

    // Checks for the storage being okay to proceed
    private boolean isStorageSet() {
        return (uriTreeHome!=null && uriTree!=null && !uriTreeHome.toString().isEmpty() && !uriTree.toString().isEmpty());
    }
    private boolean isStorageValid() {
        return (isStorageSet() && mainActivityInterface.getStorageAccess().uriTreeValid(uriTree));
    }
    private void notWriteable() {
        uriTree = null;
        uriTreeHome = null;
        mainActivityInterface.getShowToast().doIt(storage_notwritable_string);
        showStorageLocation();
    }

    // Now deal with getting a suitable storage location
    private void chooseStorageLocation() {
        if (mainActivityInterface.getAppPermissions().hasStoragePermissions()) {
            Intent intent;
            if (mainActivityInterface.getStorageAccess().lollipopOrLater()) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(mainActivityInterface.getStorageAccess().getAddPersistentWriteUriFlags());

                // IV - 'Commented in' this extra to try to always show internal and sd card storage
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true);

                folderChooser.launch(intent);

            } else {
                mainActivityInterface.navigateToFragment(null,R.id.kitKatFolderChoose);

            }
        } else {
            checkStoragePermission();
        }
    }

    private void kitKatDealWithUri() {
        if (mainActivityInterface.getWhattodo()!=null &&
                mainActivityInterface.getWhattodo().startsWith("kitkat:")) {
            String location = mainActivityInterface.getWhattodo().replace("kitkat:","");
            Log.d(TAG,"location:"+location);
            if (!location.equals("null") && !location.isEmpty()) {
                File kitkatlocation = new File(mainActivityInterface.getWhattodo().replace("kitkat:", ""));
                mainActivityInterface.setWhattodo(null);
                uriTree = Uri.fromFile(kitkatlocation);
                uriTreeHome = mainActivityInterface.getStorageAccess().homeFolder(uriTree);

                // Save the location uriTree and uriTreeHome
                saveUriLocation();
                showStorageLocation();
                checkStatus();
            }
            mainActivityInterface.setWhattodo(null);
        }
    }
    private void lollipopDealWithUri(Intent resultData) {
        // This is the newer version for Lollipop+ This is preferred!
        if (resultData != null) {
            uriTree = resultData.getData();
        } else {
            uriTree = null;
        }
        if (uriTree != null && getActivity()!=null) {
            getActivity().getContentResolver().takePersistableUriPermission(uriTree,
                    mainActivityInterface.getStorageAccess().getTakePersistentWriteUriFlags());
        }
        uriTreeHome = mainActivityInterface.getStorageAccess().homeFolder(uriTree);
        if (uriTree == null || uriTreeHome == null) {
            notWriteable();
        }
        if (uriTree != null) {
            checkStatus();
        }
    }

    // Here we deal with displaying (and processing) the chosen storage location
    private void showStorageLocation() {
        uriTreeHome = mainActivityInterface.getStorageAccess().fixBadStorage(uriTreeHome);
        if (uriTreeHome==null) {
            uriTree = null;
            saveUriLocation();
            myView.chosenLocation.setText("");
        } else {
            String[] niceLocation = mainActivityInterface.getStorageAccess().niceUriTree(uriTreeHome);
            String outputText = niceLocation[1] + "\n" + niceLocation[0];
            myView.chosenLocation.setText(outputText);
            warningCheck();
            checkStatus();
        }
    }
}