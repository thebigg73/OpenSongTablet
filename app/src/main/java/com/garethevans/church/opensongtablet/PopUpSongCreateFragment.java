package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PopUpSongCreateFragment extends DialogFragment {
    // This is a quick popup to enter a new song folder name, or rename a current one
    // Once it has been completed positively (i.e. ok was clicked) it sends a refreshAll() interface call

    static ArrayList<String> newtempfolders;
    Spinner newFolderSpinner;
    EditText newSongNameEditText;
    Button createSongCancelButton;
    Button createSongOkButton;
    private MyInterface mListener;

    static PopUpSongCreateFragment newInstance() {
        PopUpSongCreateFragment frag;
        frag = new PopUpSongCreateFragment();
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.createanewsong));
        View V = inflater.inflate(R.layout.popup_songcreate, container, false);

        // Initialise the views
        newFolderSpinner = (Spinner) V.findViewById(R.id.newFolderSpinner);
        newSongNameEditText = (EditText) V.findViewById(R.id.newSongNameEditText);
        createSongCancelButton = (Button) V.findViewById(R.id.createSongCancelButton);
        createSongOkButton = (Button) V.findViewById(R.id.createSongOkButton);

        newSongNameEditText.setText("");

        // Set up the folderspinner
        // Populate the list view with the current song folders
        // Reset to the main songs folder, so we can list them
        FullscreenActivity.currentFolder = FullscreenActivity.whichSongFolder;
        FullscreenActivity.newFolder = FullscreenActivity.whichSongFolder;
        //FullscreenActivity.whichSongFolder = "";
        ListSongFiles.listSongFolders();

        // The song folder
        newtempfolders = new ArrayList<>();
        newtempfolders.add(FullscreenActivity.mainfoldername);
        for (int e = 0; e < FullscreenActivity.mSongFolderNames.length; e++) {
            if (FullscreenActivity.mSongFolderNames[e] != null &&
                    !FullscreenActivity.mSongFolderNames[e].equals(FullscreenActivity.mainfoldername)) {
                newtempfolders.add(FullscreenActivity.mSongFolderNames[e]);
            }
        }
        ArrayAdapter<String> folders = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, newtempfolders);
        folders.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newFolderSpinner.setAdapter(folders);

        // Select the current folder as the preferred one - i.e. rename into the same folder
        newFolderSpinner.setSelection(0);
        for (int w = 0; w < newtempfolders.size(); w++) {
            if (FullscreenActivity.currentFolder.equals(newtempfolders.get(w)) ||
                    FullscreenActivity.currentFolder.equals("(" + newtempfolders.get(w) + ")")) {
                newFolderSpinner.setSelection(w);
                FullscreenActivity.newFolder = newtempfolders.get(w);
            }
        }

        // Set the newFolderSpinnerListener
        newFolderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FullscreenActivity.newFolder = newtempfolders.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set the button listeners
        createSongCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just close this view
                dismiss();
            }
        });

        createSongOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the variables
                String tempNewSong = newSongNameEditText.getText().toString().trim();

                File to;
                if (FullscreenActivity.newFolder.equals(FullscreenActivity.mainfoldername)) {
                    to = new File(FullscreenActivity.dir + "/" + tempNewSong);
                } else {
                    to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.newFolder + "/" + tempNewSong);
                }

                if (!tempNewSong.equals("") && !tempNewSong.isEmpty()
                        && !tempNewSong.contains("/") && !to.exists()
                        && !tempNewSong.equals(FullscreenActivity.mainfoldername)) {

                    FullscreenActivity.whichSongFolder = FullscreenActivity.newFolder;

                    // Try to create
                    if (tempNewSong.endsWith(".pdf") || tempNewSong.endsWith(".PDF")) {
                        // Naughty, naughty, it shouldn't be a pdf extension
                        tempNewSong = tempNewSong.replace(".pdf", "");
                        tempNewSong = tempNewSong.replace(".PDF", "");
                    }

                    LoadXML.initialiseSongTags();

                    // Prepare the XML
                    FullscreenActivity.songfilename = tempNewSong;
                    FullscreenActivity.mTitle = tempNewSong;
                    Preferences.savePreferences();

                    PopUpEditSongFragment.prepareBlankSongXML();

                    // Save the file
                    try {
                        PopUpEditSongFragment.justSaveSongXML();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Load the XML up into memory
                    try {
                        LoadXML.loadXML();
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }

                    // Tell the main page to now edit the song
                    mListener.openSongEdit();

                    // Close the popup
                    dismiss();
                } else {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.error_notset);
                    ShowToast.showToast(getActivity());
                }

            }

        });
        return V;

    }

    public interface MyInterface {
        void openSongEdit();
    }
}
