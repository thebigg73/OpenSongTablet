/*
package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lib.folderpicker.FolderPicker;

public class PopUpStorageFragment extends DialogFragment {

    static PopUpStorageFragment newInstance() {
        PopUpStorageFragment frag;
        frag = new PopUpStorageFragment();
        return frag;
    }

    public interface MyInterface {
        void rebuildSearchIndex();
        void prepareSongMenu();
    }

    private MyInterface mListener;

    StorageAccess storageAccess;
    Preferences mPreferences;


    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        try {
            mListener = (MyInterface) activity;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                dismiss();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        try {
            mListener = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            try {
                dismiss();
            } catch (Exception e) {
                Log.d("d","Problem closing fragment");
            }
        }
    }

    RelativeLayout page;
    TextView progressText, previousStorageTextView, previousStorageHeading;
    Button chooseStorageButton, previousStorageButton;
    Spinner previousStorageSpinner;
    boolean storageGranted, foldersok, changed;
    Uri uriTree;
    FloatingActionButton saveMe, closeMe;
    String storagePath = "", text="";
    ArrayList<String> locations;
    File folder;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        View V = inflater.inflate(R.layout.popup_storage, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.storage_choose));
        closeMe = V.findViewById(R.id.closeMe);
        saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStorageLocation();
            }
        });

        storageAccess = new StorageAccess();
        mPreferences = new Preferences();

        // Initialise the views
        initialiseTheViews(V);

        // Load up all of the preferences and the user specified storage location if it exists
        storagePath = storageAccess.getStoragePreference(getActivity());
        uriTree = storageAccess.homeFolder(getActivity());
        showCurrentStorage(uriTree);

        // Check we have the required storage permission
        checkStoragePermission();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        return V;
    }

    void initialiseTheViews(View V) {
        page = V.findViewById(R.id.page);
        progressText = V.findViewById(R.id.progressText);
        previousStorageTextView = V.findViewById(R.id.previousStorageTextView);
        previousStorageHeading = V.findViewById(R.id.previousStorageHeading);
        chooseStorageButton = V.findViewById(R.id.chooseStorageButton);
        previousStorageButton = V.findViewById(R.id.previousStorageButton);
        previousStorageSpinner = V.findViewById(R.id.previousStorageSpinner);


        chooseStorageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                chooseStorageLocation();
            }
        });
        previousStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
            }
        });
    }

    @SuppressLint("InlinedApi")
    void chooseStorageLocation() {
        if (storageGranted) {
            Intent intent;
            if (storageAccess.lollipopOrLater()) {
                Log.d("d","Lollipop action");
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, 42);
            } else {
                openFragment();
            }

        } else {
            requestStoragePermission();
        }
    }
    public void openFragment() {
        Log.d("d","KitKat action to choose storage location");
        Intent intent = new Intent(getActivity(), FolderPicker.class);
        intent.putExtra("title", getString(R.string.changestorage));
        intent.putExtra("pickFiles", false);
        if (uriTree!=null) {
            intent.putExtra("location", uriTree.getPath());
        }
        startActivityForResult(intent, 7789);
    }

    void showCurrentStorage(Uri u) {
        if (u!=null) {
            if (storageAccess.lollipopOrLater()) {
                try {
                    //String id = storageAccess.getDocumentsContractId(u);
                    List<String> bits = u.getPathSegments();
                    StringBuilder sb = new StringBuilder();
                    for (String b : bits) {
                        sb.append("/");
                        sb.append(b);
                    }
                    text = sb.toString();
                    if (!text.endsWith(storageAccess.appFolder)) {
                        text += "/" + storageAccess.appFolder;
                    }
                    text = text.replace("tree", "/");
                    text = text.replace(":", "/");
                    while (text.contains("//")) {
                        text = text.replace("//", "/");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    text = "" + uriTree;
                }
            } else {
                text = u.getPath();
            }

        } else {
            text = "";
        }

        if (progressText!=null) {
            // We aren't just passing through, so we can set the text
            progressText.setText(text);
        }
    }

    void checkStoragePermission() {
        Log.d("d","checkStoragePermission");

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permission has not been granted.
            storageGranted = false;
            requestStoragePermission();
        } else {
            storageGranted = true;
        }
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                Snackbar.make(page, R.string.storage_rationale,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    }
                }).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
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
            storageGranted = grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        checkReadiness();
    }

    boolean checkStorageIsValid() {
        // Check that the location exists and is writeable
        // Since the OpenSong folder may not yet exist, we can check for the locationUri or it's parent

        if (uriTree!=null) {
            DocumentFile df = storageAccess.documentFileFromUri(getActivity(), uriTree,
                    storageAccess.getStoragePreference(getActivity()));
            return df != null && df.canWrite();
        }
        return false;
    }

    void checkReadiness() {
        Log.d("d","storageGranted="+storageGranted);
        Log.d("d","checkStorageIsValid()="+checkStorageIsValid());

        if (checkStorageIsValid() && storageGranted) {
            // We're good to go, but need to wait for the user to click on the save button
            saveMe.setVisibility(View.VISIBLE);
            closeMe.setVisibility(View.GONE);
        } else {
            // Not ready, so hide the start button
            saveMe.setVisibility(View.GONE);
            closeMe.setVisibility(View.VISIBLE);
        }
    }

    public void startSearch() {
        // Deactivate the stuff we shouldn't click on while it is being prepared
        setEnabledOrDisabled(false);

        // Initialise the available storage locations
        locations = new ArrayList<>();

        FindLocations findlocations = new FindLocations();
        findlocations.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void setEnabledOrDisabled(boolean what) {
        saveMe.setEnabled(what);
        closeMe.setEnabled(what);
        previousStorageButton.setEnabled(what);
        chooseStorageButton.setEnabled(what);
    }

    public void walkFiles(File root) {
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

    public void displayWhere(String msg) {
        final String str = msg;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                previousStorageTextView.setText(str);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class FindLocations extends AsyncTask<Object, String, String> {

        String s;

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

                } else {
                    // Listen for the clicks!
                    previousStorageHeading.setVisibility(View.VISIBLE);
                    locations.add(0,"");
                    previousStorageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (changed) {
                                if (position>0) {
                                    uriTree = Uri.parse(locations.get(position));
                                    chooseStorageButton.performClick();
                                }
                            } else {
                                changed=true;
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) { }
                    });
                    ArrayAdapter<String> listAdapter = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, locations);
                    previousStorageSpinner.setAdapter(listAdapter);
                    previousStorageSpinner.setVisibility(View.VISIBLE);
                    previousStorageTextView.setVisibility(View.GONE);
                }
            }
        }
    }
    public void saveStorageLocation() {
        FullscreenActivity.uriTree = uriTree;
        // Do this as a separate thread
        new Thread(new Runnable() {
            String message;
            @Override
            public void run() {
                Looper.prepare();
                // Check if the folders exist, if not, create them
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        message = getString(R.string.storage_check);
                        progressText.setText(message);
                    }
                });
                final String progress = storageAccess.createOrCheckRootFolders(getActivity());
                foldersok = !progress.contains("Error");

                if (foldersok) {
                    // Load up all of the preferences into FullscreenActivity (static variables)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            message = getString(R.string.load_preferences);
                            progressText.setText(message);
                        }
                    });
                    FullscreenActivity fullscreenActivity = new FullscreenActivity();
                    fullscreenActivity.mainSetterOfVariables(getActivity());

                    // Search for the user's songs
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            message = getString(R.string.initialisesongs_start).replace("-","").trim();
                            progressText.setText(message);
                        }
                    });
                    try {
                        storageAccess.listSongs(getActivity());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Show how many songs have been found and display this to the user
                            // This will remain as until the current folder is build
                            int numsongs = FullscreenActivity.songIds.size();
                            String result = numsongs + " " + getString(R.string.initialisesongs_end);
                            progressText.setText(result);
                        }
                    });


                    ListSongFiles listSongFiles = new ListSongFiles();
                    try {
                        listSongFiles.songUrisInFolder(getActivity());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            message = progressText.getText().toString() + "\n" + getString(R.string.success);
                            progressText.setText(message);
                        }
                    });

                    // Now save the appropriate variables and then start the intent
                    Preferences.savePreferences();

                    if (mListener!=null) {
                        mListener.rebuildSearchIndex();
                        mListener.prepareSongMenu();
                    }

                    try {
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // There was a problem with the folders, so restart the app!
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), BootUpCheck.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        }).start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode==7789 && resultData!=null && resultData.getExtras()!=null) {
                // This is for Android KitKat - deprecated file method
                String folderLocation = resultData.getExtras().getString("data");
                uriTree = Uri.parse(folderLocation);

                // If we can write to this all is good, if not, tell the user (likely to be SD card)
                if (!storageAccess.canWrite(getActivity(), uriTree)) {
                    uriTree = null;
                    ShowToast showToast = new ShowToast();
                    showToast.showToastMessage(getActivity(), getString(R.string.storage_notwritable));
                    if (locations.size()>0) {
                        // Revert back to the blank selection as the one chosen can't be used
                        previousStorageSpinner.setSelection(0);
                    }
                }


            } else {
                // This is the newer version for Lollipop+ This is preferred!
                if (resultData!=null) {
                    uriTree = resultData.getData();
                } else {
                    uriTree = null;
                }
                if (uriTree!=null) {
                    getActivity().getContentResolver().takePersistableUriPermission(uriTree,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }

            // Save the location
            if (uriTree!=null) {
                FullscreenActivity.uriTree = uriTree;
                mPreferences.setMyPreferenceString(getActivity(), "uriTree", uriTree.toString());
            } else {
                mPreferences.setMyPreferenceString(getActivity(), "uriTree", "");
            }

            // Update the storage text
            showCurrentStorage(uriTree);

            // See if we can show the start button yet
            checkReadiness();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            Log.d("d","Problem closing fragment");
        }
    }

}*/
