package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PopUpSongFolderRenameFragment extends DialogFragment {
    // This is a quick popup to enter a new song folder name, or rename a current one
    // Once it has been completed positively (i.e. ok was clicked) it sends a refreshAll() interface call

    static String myTask;
    String mTitle;
    static ArrayList<String> oldtempfolders;
    GetFoldersAsync getFolders_async;
    String tempNewFolder;
    File checkExists;
    String tempOldFolder;
    TextView title;

    static PopUpSongFolderRenameFragment newInstance(String message) {
        myTask = message;
        PopUpSongFolderRenameFragment frag;
        frag = new PopUpSongFolderRenameFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
        void prepareSongMenu();
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

    Spinner oldFolderNameSpinner;
    EditText newFolderNameEditText;

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
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_song_rename));
        switch (myTask) {
            case "create":
                mTitle = getActivity().getResources().getString(R.string.options_song_newfolder);
                break;

            default:
                mTitle = getActivity().getResources().getString(R.string.options_song_rename);
                break;
        }
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View V = inflater.inflate(R.layout.popup_songfolderrename, container, false);

        title = (TextView) V.findViewById(R.id.dialogtitle);
        doSetTitle();
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                //saveMe.setEnabled(false);
                createOrRename();
            }
        });

        // Initialise the views
        oldFolderNameSpinner = (Spinner) V.findViewById(R.id.oldFolderNameSpinner);
        newFolderNameEditText = (EditText) V.findViewById(R.id.newFolderNameEditText);

        // Set up the folderspinner
        // Set up the spinner
        // Populate the list view with the current song folders
        // Reset to the main songs folder, so we can list them
        FullscreenActivity.currentFolder = FullscreenActivity.whichSongFolder;
        FullscreenActivity.newFolder = FullscreenActivity.whichSongFolder;
        //FullscreenActivity.whichSongFolder = "";

        // Do the time consuming bit as an asynctask
        getFolders_async = new GetFoldersAsync();
        try {
            getFolders_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Probably closed popup before folders listed\n"+e);
        }

        if (myTask.equals("create")) {
            // Hide the spinner
            oldFolderNameSpinner.setVisibility(View.GONE);
        }

        // Set the oldFolderNameSpinnerListener
        oldFolderNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FullscreenActivity.currentFolder = oldtempfolders.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set the button listeners

        return V;
    }

    public void doSetTitle() {
        title.setText(mTitle);
    }

    public void createOrRename() {
        getVariables();
        if (myTask.equals("create")) {
            doCreateFolder();
        } else {
            doRenameFolder();
        }

        ShowToast.showToast(getActivity());

        // Load the songs and the folders
        if (mListener!=null) {
            mListener.prepareSongMenu();
        }

        try {
            LoadXML.loadXML(getActivity());
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        // Save preferences
        Preferences.savePreferences();

        if (myTask.equals("create") && mListener!=null) {
            mListener.refreshAll();
        }
        dismiss();
    }

    public void doCreateFolder() {
        // Try to create
        File newfoldertomake = new File(FullscreenActivity.dir + "/" + tempNewFolder);
        if (newfoldertomake.mkdirs()) {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.newfolder) + " - " + getResources().getString(R.string.ok);
        } else {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.newfolder) + " - " + getResources().getString(R.string.createfoldererror);
        }
    }

    public void doRenameFolder() {
        // Try to rename
        File from = new File(FullscreenActivity.dir + "/" + tempOldFolder);
        File to = new File(FullscreenActivity.dir + "/" + tempNewFolder);
        if (from.renameTo(to)) {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.renametitle) + " - " + getResources().getString(R.string.ok);
        } else {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.renametitle) + " - " + getResources().getString(R.string.createfoldererror);
        }

        FullscreenActivity.whichSongFolder = tempNewFolder;
    }

    public void getVariables() {
        // Get the variables
        tempNewFolder = newFolderNameEditText.getText().toString().trim();
        checkExists = new File(FullscreenActivity.dir + "/" + tempNewFolder);

        if (!tempNewFolder.equals("") && !tempNewFolder.isEmpty() && !tempNewFolder.contains("/") &&
                !checkExists.exists() &&
                !tempNewFolder.equals(FullscreenActivity.mainfoldername)) {
            FullscreenActivity.newFolder = tempNewFolder;
            tempOldFolder = FullscreenActivity.currentFolder;
        }
    }

    private class GetFoldersAsync extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            ListSongFiles.getAllSongFolders();

            // The song folder
            oldtempfolders = new ArrayList<>();
            for (int e=0;e<FullscreenActivity.mSongFolderNames.length;e++) {
                if (FullscreenActivity.mSongFolderNames[e]!=null &&
                        !FullscreenActivity.mSongFolderNames[e].equals(FullscreenActivity.mainfoldername)) {
                    oldtempfolders.add(FullscreenActivity.mSongFolderNames[e]);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                ArrayAdapter<String> folders = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, oldtempfolders);
                folders.setDropDownViewResource(R.layout.my_spinner);
                oldFolderNameSpinner.setAdapter(folders);

                // Select the current folder as the preferred one
                oldFolderNameSpinner.setSelection(0);
                for (int w = 0; w < oldtempfolders.size(); w++) {
                    if (FullscreenActivity.whichSongFolder.equals(oldtempfolders.get(w))) {
                        oldFolderNameSpinner.setSelection(w);
                        FullscreenActivity.currentFolder = oldtempfolders.get(w);
                    }
                }
            } catch (Exception e) {
                // Error
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (getFolders_async!=null) {
            getFolders_async.cancel(true);
        }
        this.dismiss();
    }
}
