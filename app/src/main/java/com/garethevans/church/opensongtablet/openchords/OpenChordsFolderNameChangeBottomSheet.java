package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;
import com.garethevans.church.opensongtablet.databinding.BottomSheetOpenchordsFolderChangeBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.UUID;

public class OpenChordsFolderNameChangeBottomSheet extends BottomSheetCommon {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "OpenChordsolderNameChangeBS";
    private MainActivityInterface mainActivityInterface;
    private BottomSheetOpenchordsFolderChangeBinding myView;
    private final OpenChordsFragment openChordsFragment;
    private final String localName;
    private String currentLocal="", currentRemote="";

    OpenChordsFolderNameChangeBottomSheet(OpenChordsFragment openChordsFragment, String localName) {
        this.openChordsFragment = openChordsFragment;
        this.localName = localName;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = BottomSheetOpenchordsFolderChangeBinding.inflate(inflater, null, false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        prepareStrings();
        setupViews();
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            currentLocal = getString(R.string.sync_local_folder_name) + ": " + localName;
            currentRemote = getString(R.string.sync_remote_folder_name) + ": " + mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName();
        }
    }

    private void setupViews() {
        // If we have a local folder that matches the remote folder, we can switch
        // This also applies if the remote folder name is MAIN
        if ((mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName().equals(mainActivityInterface.getMainfoldername()) ||
                mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName().equals("MAIN")) ||
                mainActivityInterface.getOpenChordsAPI().getValidFolders().contains(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName())) {
            myView.switchLocalFolderLayout.setVisibility(View.VISIBLE);
            myView.newLocalFolderLayout.setVisibility(View.GONE);
        } else {
            myView.switchLocalFolderLayout.setVisibility(View.GONE);
            myView.newLocalFolderLayout.setVisibility(View.VISIBLE);
        }

        // Set the names of the local and remote folders
        myView.localName.setText(currentLocal);
        myView.remoteName.setText(currentRemote);

        // Let the user see the changes
        updateView(myView.updateRemoteFolder,mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName(),localName);
        updateView(myView.newLocalFolder,null,mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
        updateView(myView.newRemoteFolder,localName,null);
        updateView(myView.switchLocalFolder,localName,mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
    }

    private void updateView(MyMaterialTextView view, String from, String to) {
        String newHint;
        if (to == null) {
            newHint = view.getHint().toString() + " (" + from + ")";
        } else if (from == null) {
            newHint = view.getHint().toString() + " (" + to + ")";
        } else {
            newHint = view.getHint().toString() + " (" + from + " -> " + to + ")";
        }
        view.setHint(newHint);
    }

    private void setupListeners() {
        myView.updateRemoteFolder.setOnClickListener(view -> {
            // Change the name of the remote folder
            mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderName(localName);
            mainActivityInterface.getOpenChordsAPI().setLocalFolderName(localName);
            // Let the fragment know to keep these values
            openChordsFragment.setKeepLocalFolderName(localName);
            // Update the title with no further query
            updateTitleAndQuery(localName);
        });
        myView.newRemoteFolder.setOnClickListener(view -> {
            // We want to create a new remote folder.
            // This means we need to create a new UUID for this local folder
            // Change the OpenChords foldername/uuid to the local values
            // We then run the query again
            String newUuid = String.valueOf(UUID.randomUUID());
            mainActivityInterface.getOpenChordsAPI().changeOpenSongFolderUUID(
                    mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid(),
                    newUuid);
            mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderUuid(newUuid);
            mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderName(localName);
            // Now update the title and run the query again
            updateTitleAndQuery(localName);
        });
        myView.newLocalFolder.setOnClickListener(view -> {
            // We need to create a new UUID for this local folder and then run the query again
            // Stick to using the OpenChords foldername/uuid
            String newUuid = String.valueOf(UUID.randomUUID());
            mainActivityInterface.getOpenChordsAPI().changeOpenSongFolderUUID(
                    mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid(),
                    newUuid);
            updateTitleAndQuery(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
        });
        myView.switchLocalFolder.setOnClickListener(view -> {
            // We need to change the existing local folder uuid to a new random UUID
            String newUuid = String.valueOf(UUID.randomUUID());
            mainActivityInterface.getOpenChordsAPI().changeOpenSongFolderUUID(
                    mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid(),
                    newUuid);
            mainActivityInterface.getOpenChordsAPI().changeOpenSongFolderUUID(
                    mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid(),newUuid);
            // We then need to change the switch the local folder uuid to match the OpenChords uuid
            String oldUuid = mainActivityInterface.getOpenChordsAPI().getOpenSongFolderUuidFromName(
                    mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
            mainActivityInterface.getOpenChordsAPI().changeOpenSongFolderUUID(
                    oldUuid,
                    mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid());
            // Now update the title and run the query again
            updateTitleAndQuery(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
        });
    }

    private void updateTitleAndQuery(String newTitle) {
        // Pretend this has come from the server and update the title (so a query isn't run)
        mainActivityInterface.getOpenChordsAPI().setIsServerResponse(true);
        openChordsFragment.justUpdateTitle(newTitle);
        // Now query the server
        openChordsFragment.queryOpenChordsServer();
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}