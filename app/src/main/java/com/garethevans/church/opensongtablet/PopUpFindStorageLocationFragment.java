package com.garethevans.church.opensongtablet;

// This file is used to try to find any previous storage locations for the app
// This should help users who claim the app has deleted all of their songs!  Fed up of this one!

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PopUpFindStorageLocationFragment extends DialogFragment {

    static PopUpFindStorageLocationFragment newInstance() {
        PopUpFindStorageLocationFragment frag;
        frag = new PopUpFindStorageLocationFragment();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

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
            this.dismiss();
        }
    }

    TextView location;
    ListView fileListView;
    ProgressBar progressBar;
    FloatingActionButton saveMe;
    FloatingActionButton closeMe;
    AsyncTask<Object, String, String> findlocations;
    ArrayList<String> locations;
    File folder;
    TextView title;
    String chosen = Environment.getExternalStorageDirectory() + "/documents";
    boolean nothingchosen = true;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_file_chooser, container, false);

        title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.storage_choose));
        closeMe = V.findViewById(R.id.closeMe);
        saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(saveMe, getActivity());
                    saveMe.setEnabled(false);
                    saveStorageLocation();
                    reopenStorageChooser();
                }
            });
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                reopenStorageChooser();
            }
        });

        // Initialise the views
        location = V.findViewById(R.id.location);
        location.setTextColor(0xFFFFFF00);
        fileListView = V.findViewById(R.id.fileListView);
        progressBar = V.findViewById(R.id.progressBar);

        startSearch();

        return V;
    }

    public void startSearch() {
        // Deactivate the stuff we shouldn't click on while it is being prepared
        saveMe.setEnabled(false);
        fileListView.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        title.setText(R.string.options_connections_searching);

        locations = new ArrayList<>();

        findlocations = new FindLocations();
        findlocations.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class FindLocations extends AsyncTask<Object, String, String>{

        String s;

        @Override
        protected void onPreExecute() {

        }

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
            if (locations!=null && getActivity()!=null) {
                // Hide the progressbar
                progressBar.setVisibility(View.GONE);

                title.setText(R.string.storage_choose);

                // Reenable the stuff we can now use
                closeMe.setEnabled(true);
                saveMe.setEnabled(true);
                fileListView.setEnabled(true);

                if (locations.size()<1) {
                    // No previous installations found
                    locations.add(getString(R.string.nofound));
                }
                ArrayAdapter<String> listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_activated_1, locations);
                fileListView.setAdapter(listAdapter);

                // Listen for the clicks!
                fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if (!new File(chosen).canWrite()) {
                            // not writable
                            FullscreenActivity.myToastMessage = getActivity().getString(R.string.storage_notwritable);
                            ShowToast.showToast(getActivity());
                            adapterView.setSelection(-1);
                            chosen = Environment.getExternalStorageDirectory() + "/documents";
                            nothingchosen = true;
                        } else {
                            nothingchosen = false;
                            chosen = locations.get(i);
                        }
                    }
                });

                location.setText(R.string.found);
            }
        }
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
                location.setText(str);
            }
        });
    }

    public void saveStorageLocation() {
        // Save the storage location, then reopen the storage chooser
        String mydefault = Environment.getExternalStorageDirectory() + "/documents";
        if (nothingchosen || chosen.equals(getString(R.string.nofound))) {
            // Nothing is selected, so don't save
            Log.d("d","Nothing chosen");
        } else if (chosen.equals(mydefault)) {
            // User wants the default internal storage
            FullscreenActivity.prefStorage = "int";
            Preferences.savePreferences();
        } else {
            // User wants the custom storage
            FullscreenActivity.prefStorage = "custom";
            FullscreenActivity.customStorage = chosen;
            Preferences.savePreferences();
        }
    }

    public void reopenStorageChooser() {
        if (mListener!=null) {
            FullscreenActivity.whattodo = "managestorage";
            mListener.openFragment();
        }
        try {
            dismiss();
        } catch (Exception e) {
            Log.d("d","Problem closing dialog");
        }
    }
}
