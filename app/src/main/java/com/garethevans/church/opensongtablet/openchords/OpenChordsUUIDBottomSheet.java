package com.garethevans.church.opensongtablet.openchords;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.customviews.OpenChordsFolderUUIDLayout;
import com.garethevans.church.opensongtablet.databinding.BottomSheetOpenchordsAdvancedBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.UUID;

public class OpenChordsUUIDBottomSheet extends BottomSheetCommon {

    private final String TAG = "OpenChordsUUIDsBS";
    private final MainActivityInterface mainActivityInterface;
    private final String uuid_not_valid_string, user_string;
    private BottomSheetOpenchordsAdvancedBinding myView;

    public OpenChordsUUIDBottomSheet(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
        uuid_not_valid_string = c.getString(R.string.uuid_not_valid);
        user_string = c.getString(R.string.user);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetOpenchordsAdvancedBinding.inflate(inflater,container,false);

        myView.dialogHeading.setClose(this);

        setupViews();
        return myView.getRoot();
    }

    private void setupViews() {
        if (getContext()!=null && myView!=null) {
            myView.userUUID.setText(mainActivityInterface.getOpenChordsAPI().getOpenChordsUserUuid());
            myView.userUUID.setHint(user_string);
            OpenChordsFolderUUIDLayout.OnFolderActionListener listener = isLocked -> {
                Log.d(TAG,"lock clicked");
                // Handle lock action for this specific position/record
                if (isLocked) {
                    // Check for a valid UUID
                    if (myView.userUUID.checkUUIDValid()) {
                        // Update the uuid for this user
                        String uuid = myView.userUUID.getText();
                        if (!uuid.equals(mainActivityInterface.getOpenChordsAPI().getOpenChordsUserUuid())) {
                            Log.d(TAG, "update user uuid:" + uuid);
                            mainActivityInterface.getOpenChordsAPI().setOpenChordsUserUuid(uuid);
                        } else {
                            Log.d(TAG,"unchanged");
                        }
                    } else {
                        // Reset back to default
                        mainActivityInterface.getShowToast().doItBottomSheet(uuid_not_valid_string,myView.getRoot());
                        myView.userUUID.setFolderUUID(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid());
                        Log.d(TAG,"reset user uuid to default");
                    }
                }
            };
            myView.userUUID.setOnFolderActionListener(listener);
            myView.userUUID.initialise(getContext(), mainActivityInterface.getOpenChordsAPI().getOpenChordsUserUuid(), user_string);

            // Now set up the recylcer view
            OpenChordsFolderUUIDAdapter adapter = new OpenChordsFolderUUIDAdapter(getContext(), myView.getRoot());
            myView.folderUUIDs.setLayoutManager(new LinearLayoutManager(getContext()));
            myView.folderUUIDs.setAdapter(adapter);
        }


        /*myView.userUUID.setText(mainActivityInterface.getOpenChordsAPI().getOpenChordsUserUuid());
        myView.userUUID.setEnabled(false);
        myView.userUUIDLock.setOnClickListener(view -> {
            String uuid = "";
            if (myView.userUUID.getText()!=null) {
                uuid = myView.userUUID.getText().toString();
            }
            boolean uuidValid = checkUUIDValid(uuid);

            if (myView.userUUID.getEnabled()) {
                // Disable it
                myView.userUUID.setEnabled(false);
                // Enable share
                myView.userUUIDShare.setEnabled(true);
                // Change the icon to a lock
                myView.userUUIDLock.setImageDrawable(R.drawable.lock);
                if (uuidValid) {
                    mainActivityInterface.getOpenChordsAPI().setOpenChordsUserUuid(uuid);
                } else {
                    mainActivityInterface.getShowToast().doItBottomSheet(uuid_not_valid_string,myView.getRoot());
                    myView.userUUID.setText(mainActivityInterface.getOpenChordsAPI().getOpenChordsUserUuid());
                }

            } else {
                // Enable it
                myView.userUUID.setEnabled(true);
                // Disable share
                myView.userUUIDShare.setEnabled(false);
                // Change the icon to an open lock
                myView.userUUIDLock.setImageDrawable(R.drawable.lock_open);
            }
        });
        myView.userUUIDShare.setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mainActivityInterface.getOpenChordsAPI().getOpenChordsUserUuid());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });*/
    }

    private boolean checkUUIDValid(String uuid) {
        if (uuid==null || uuid.isEmpty()) {
            return false;
        } else {
            java.util.regex.Pattern UUID_REGEX = java.util.regex.Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
            java.util.regex.Pattern CUSTOM_UUID_REGEX = java.util.regex.Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{6}$");
            return UUID_REGEX.matcher(uuid).matches() || CUSTOM_UUID_REGEX.matcher(uuid).matches();
        }
    }

    private void prepareFolderUUIDs() {
        for (OpenSongFolderRecordObject recordObject:mainActivityInterface.getOpenChordsAPI().getOpenSongFolderRecordObjects()) {

        }
    }
}
