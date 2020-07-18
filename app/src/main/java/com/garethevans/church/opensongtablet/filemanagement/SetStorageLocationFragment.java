package com.garethevans.church.opensongtablet.filemanagement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.databinding.FragmentSetstoragelocationBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lib.folderpicker.FolderPicker;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.make;

public class SetStorageLocationFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private Preferences preferences;
    private StorageAccess storageAccess;
    private ShowToast showToast;
    private Uri uriTree, uriTreeHome;
    private Bundle bundle;
    private Button setStorage, findStorage, startApp;
    private TextView progressText, previousStorageTextView, previousStorageHeading, previousStorageLocationsTextView;
    private Spinner previousStorageSpinner;
    private boolean changed;
    private ArrayList<String> locations;
    private File folder;

    private FragmentSetstoragelocationBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mainActivityInterface = (MainActivityInterface) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d("SetStorageFragment","Here");

        bundle = savedInstanceState;
        myView = FragmentSetstoragelocationBinding.inflate(inflater, container, false);

        // Initialise the helper classes
        initialiseHelpers();

        // Set up the views
        initialiseViews();

        // Check preferences for storage (if they exist)
        checkPreferencesForStorage();

        // Check we have the required storage permission
        checkStoragePermission();
        
        // Set the storage location
        setStorageLocation();
        
        return myView.getRoot();
    }

    private void initialiseHelpers() {
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        showToast = new ShowToast();
    }

    private void initialiseViews() {
        setStorage = myView.setStorage;
        setStorage.setOnClickListener(v -> {
            Log.d("SetStorageLocation","isStorageGranted()="+isStorageGranted());
            Log.d("SetStorageLocation","isstorageSet()="+isStorageSet());
            Log.d("SetStorageLocation","isStorageValid()="+isStorageValid());
            if (isStorageGranted()) {
                chooseStorageLocation();
            } else {
                requestStoragePermission();
            }
        });
        findStorage = myView.findStorage;
        findStorage.setOnClickListener(v -> startSearch());
        startApp = myView.startApp;
        startApp.setOnClickListener(v -> goToSongs());
        progressText = myView.progressText;
        previousStorageSpinner = myView.previousStorageSpinner;
        previousStorageTextView = myView.previousStorageTextView;
        previousStorageHeading = myView.previousStorageHeading;
        previousStorageLocationsTextView = myView.previousStorageLocationsTextView;
        mainActivityInterface.lockDrawer(true);
        mainActivityInterface.hideActionButton(true);
    }

    private void checkPreferencesForStorage() {
        String uT  = preferences.getMyPreferenceString(requireActivity(),"uriTree","");
        String uTH = preferences.getMyPreferenceString(requireActivity(),"uriTreeHome","");
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
            uriTreeHome = storageAccess.homeFolder(requireActivity(),uriTree,preferences);
        }
    }

    private void pulseButton(View v) {
        CustomAnimation ca = new CustomAnimation();
        ca.pulse(requireActivity(), v);
    }

    private boolean isStorageSet() {
        return (uriTreeHome!=null && uriTree!=null && !uriTreeHome.toString().isEmpty() && !uriTree.toString().isEmpty());
    }

    private boolean isStorageValid() {
        Log.d("d","uriTree="+uriTree);
        return (isStorageSet() && storageAccess.uriTreeValid(getActivity(),uriTree));
    }

    private void setStorageLocation() {
        progressText.setText(parseLocation());
        checkStatus();
    }

    private String parseLocation() {
        String text;
        if (isStorageValid()) {
            if (storageAccess.lollipopOrLater()) {
                try {
                    List<String> bits = uriTreeHome.getPathSegments();
                    StringBuilder sb = new StringBuilder();
                    for (String b : bits) {
                        sb.append("/");
                        sb.append(b);
                    }
                    text = sb.toString();
                    Log.d("SetStorageLocation", "text=" + text);
                    if (!text.endsWith(storageAccess.appFolder)) {
                        text += "/" + storageAccess.appFolder;
                    }
                    text = text.replace("tree", "/");
                    text = text.replace(":", "/");
                    while (text.contains("//")) {
                        text = text.replace("//", "/");
                    }

                    // Finally, the storage location is likely something like /9016-4EF8/OpenSong/document/9016-4EF8/OpenSong
                    // This is due to the content using a document contract
                    // Strip this back to the bit after document
                    if (text.contains("OpenSong/document/")) {
                        text = text.substring(text.lastIndexOf("OpenSong/document/") + 18);
                    }
                    if (text.startsWith("primary/")) {
                        text = text.replace("primary/","");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    text = "" + uriTreeHome;
                }
            } else {
                text = uriTreeHome.getPath();
            }
            return text;
        }
        return getString(R.string.notset);
    }

    private void chooseStorageLocation() {
        if (isStorageGranted()) {
            Intent intent;
            if (storageAccess.lollipopOrLater()) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                intent.putExtra("android.content.extra.FANCY", true);
                intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
                intent.putExtra("android.content.extra.INITIAL_URI", uriTree);
                startActivityForResult(intent, 42);
            } else {
                openFragment();
            }
        } else {
            requestStoragePermission();
        }
    }
    private void openFragment() {
        Intent intent = new Intent(requireActivity(), FolderPicker.class);
        intent.putExtra("title", getString(R.string.changestorage));
        intent.putExtra("pickFiles", false);
        if (uriTree!=null) {
            intent.putExtra("location", uriTree.getPath());
        }
        startActivityForResult(intent, 7789);
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
        startApp.setEnabled(what);
        setStorage.setEnabled(what);
        findStorage.setEnabled(what);
        previousStorageSpinner.setEnabled(what);
        if (!what) {
            //progressBar.setVisibility(View.VISIBLE);
            previousStorageTextView.setVisibility(View.VISIBLE);
        } else {
            //progressBar.setVisibility(View.GONE);
            previousStorageTextView.setVisibility(View.GONE);
        }
    }

    private void notWriteable() {
        uriTree = null;
        uriTreeHome = null;
        showToast.doIt(requireActivity(), getString(R.string.storage_notwritable));
        if (locations != null && locations.size() > 0) {
            // Revert back to the blank selection as the one chosen can't be used
            previousStorageSpinner.setSelection(0);
        }
    }
    private void saveUriLocation() {
        if (uriTree!=null) {
            // Save the preferences
            preferences.setMyPreferenceString(requireActivity(), "uriTree", uriTree.toString());
            preferences.setMyPreferenceString(requireActivity(), "uriTreeHome", uriTreeHome.toString());
        } else {
            preferences.setMyPreferenceString(requireActivity(), "uriTree", "");
            preferences.setMyPreferenceString(requireActivity(), "uriTreeHome", "");
        }
    }
    private void checkStoragePermission() {
        if (!isStorageGranted()) {
            // Storage permission has not been granted.
            requestStoragePermission();
        }
        // Set the storage location
        setStorageLocation();
    }
    private void requestStoragePermission() {
        if (getActivity()!=null && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                make(getActivity().findViewById(R.id.drawer_layout), R.string.storage_rationale,
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

    private boolean isStorageGranted() {
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            checkStatus();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        // Set the storage location
        setStorageLocation();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 7789 && resultData != null && resultData.getExtras() != null) {
                kitKatDealWithUri(resultData);
            } else if (resultData!=null) {
                lollipopDealWithUri(resultData);
            }

            // Save the location uriTree and uriTreeHome
            saveUriLocation();
            Log.d("d","onActivityResult()");
            // Update the storage text
            setStorageLocation();

            // See if we can show the start button yet
            checkStatus();
        }
    }
    private void kitKatDealWithUri(Intent resultData) {
        String folderLocation;
        if (resultData!=null && resultData.getExtras()!=null) {
            // This is for Android KitKat - deprecated file method
            folderLocation = resultData.getExtras().getString("data");
        } else {
            File f = requireActivity().getExternalFilesDir("OpenSong");
            if (f!=null) {
                folderLocation = f.toString();
            } else {
                folderLocation = null;
            }
        }
        if (folderLocation!=null) {
            uriTree = Uri.parse(folderLocation);
            uriTreeHome = storageAccess.homeFolder(requireActivity(),uriTree,preferences);

            // If we can write to this all is good, if not, tell the user (likely to be SD card)
            if (!storageAccess.canWrite(requireActivity(), uriTree)) {
                notWriteable();
            }
        } else {
            notWriteable();
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
            requireActivity().getContentResolver().takePersistableUriPermission(uriTree,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        uriTreeHome = storageAccess.homeFolder(requireActivity(),uriTree,preferences);
        if (uriTree==null || uriTreeHome==null) {
            notWriteable();
        }
    }


    private void checkStatus() {
        if (isStorageGranted() && isStorageSet() && isStorageValid()) {
            startApp.setVisibility(View.VISIBLE);
            pulseButton(startApp);
            setStorage.clearAnimation();
        } else {
            startApp.setVisibility(View.GONE);
            pulseButton(setStorage);
            startApp.clearAnimation();
        }
    }

    private void goToSongs() {
        Log.d("d","Going to songs()");
        NavOptions navOptions = new NavOptions.Builder()
                .build();
        NavHostFragment.findNavController(SetStorageLocationFragment.this)
                .navigate(R.id.nav_boot,bundle,navOptions);
    }

    private void walkFiles(File root) {
        if (root!=null && root.exists() && root.isDirectory()) {
            File[] list = root.listFiles();
            if (list != null) {
                for (File f : list) {
                    if (f.isDirectory()) {
                        String where = f.getAbsolutePath();
                        if (where.endsWith("/OpenSong/Songs") && !where.contains(".estrongs") && !where.contains("com.ttxapps")) {
                            // Found one and it isn't in eStrongs recycle folder or the dropsync temp files!
                            where = where.substring(0, where.length() - 15);
                            locations.add(where);
                        }
                        folder = f;
                        displayWhere(where);
                        walkFiles(f);
                    }
                }
            }
        }
    }

    private void displayWhere(String msg) {
        final String str = msg;
        requireActivity().runOnUiThread(() -> previousStorageTextView.setText(str));
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
                        if (!sb.toString().contains(str+" ")) {
                            sb.append(str).append(" \n");
                        }
                    }
                    previousStorageTextView.setVisibility(View.GONE);
                    previousStorageLocationsTextView.setText(sb.toString().trim());
                    locations.add(0,"");
                    previousStorageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (changed) {
                                if (position>0) {
                                    File f = new File(locations.get(position));
                                    uriTree = Uri.fromFile(f);
                                    setStorage.performClick();
                                }
                            } else {
                                changed=true;
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) { }
                    });
                    ArrayAdapter<String> listAdapter = new ArrayAdapter<>(requireActivity(), R.layout.my_spinner, locations);
                    previousStorageSpinner.setAdapter(listAdapter);
                    previousStorageSpinner.setVisibility(View.GONE);
                }
            }
        }
    }
}
