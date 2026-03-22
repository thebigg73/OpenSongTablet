package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.databinding.BottomSheetOpenchordsForceBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;

import java.util.ArrayList;

public class OpenChordsForceBottomSheet extends BottomSheetCommon {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetOpenchordsForceBinding myView;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "OpenChordsBottomSheet";
    private final OpenChordsFragment openChordsFragment;
    private String force_push_warning_string = "", force_pull_warning_string = "",
            files_deleted_local_string = "", files_deleted_remote_string = "",
            song_string = "", set_list_string = "", files_replaced_local_string="",
            files_replaced_remote_string="",
            sync_force_uploaded_string="",
            sync_force_downloaded_string="";
    private final ArrayList<String> itemsAffected = new ArrayList<>();

    OpenChordsForceBottomSheet(OpenChordsFragment openChordsFragment) {
        this.openChordsFragment = openChordsFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = BottomSheetOpenchordsForceBinding.inflate(inflater, null, false);

        myView.dialogHeading.setClose(this);

        prepareStrings();
        setupViews();
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            force_push_warning_string = getString(R.string.sync_force_changes_warning_push);
            force_pull_warning_string = getString(R.string.sync_force_changes_warning_pull);
            files_deleted_local_string = getString(R.string.sync_items_deleted_local);
            files_deleted_remote_string = getString(R.string.sync_items_deleted_remote);
            song_string = getString(R.string.song);
            set_list_string = getString(R.string.set_list);
            files_replaced_local_string = getString(R.string.sync_newer_items_local_replaced);
            files_replaced_remote_string = getString(R.string.sync_newer_items_remote_replaced);
            sync_force_uploaded_string = getString(R.string.sync_last_force_uploaded);
            sync_force_downloaded_string = getString(R.string.sync_last_force_download);
        }
    }

    private void setupViews() {
        String value = sync_force_uploaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastForcePush");
        myView.lastForcePushInfo.setText(value);
        value = sync_force_downloaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastForcePull");
        myView.lastForcePullInfo.setText(value);

        boolean isOwner = mainActivityInterface.getOpenChordsAPI().getIsOwner();
        boolean isReadOnly = mainActivityInterface.getOpenChordsAPI().getIsReadOnly();

        // If we are the owner, we can upload fine.  If not, we can only upload if the folder isn't read only
        boolean canUpload = isOwner || !isReadOnly;
        myView.forcePush.setVisibility(canUpload? View.VISIBLE:View.GONE);

    }

    private void setupListeners() {
        myView.forcePush.setOnClickListener(view -> {
            itemsAffected.clear();

            // Add a note of any items that will be deleted in the remote folder
            // These are the ones that aren't in the local folder)
            boolean needSpace = false;
            if (mainActivityInterface.getOpenChordsAPI().getSongsNotOnLocalCount()!=0 ||
                mainActivityInterface.getOpenChordsAPI().getSetListsNotOnLocalCount()!=0) {
                String[] songs = mainActivityInterface.getOpenChordsAPI().getSongsNotOnLocalString().split(",");
                String[] sets = mainActivityInterface.getOpenChordsAPI().getSetListsNotOnLocalString().split(",");
                itemsAffected.add(files_deleted_remote_string+":");
                updateFilesAffected(songs, sets);
                needSpace = true;
            }

            // Add a note of any newer items that will be replaced in the remote folder by older local items
            if (mainActivityInterface.getOpenChordsAPI().getSongsOnLocalOlderCount()!=0 ||
                mainActivityInterface.getOpenChordsAPI().getSetListsOnLocalOlderCount()!=0) {
                String[] songs = mainActivityInterface.getOpenChordsAPI().getSongsOnLocalOlderString().split(",");
                String[] sets = mainActivityInterface.getOpenChordsAPI().getSetListsOnLocalOlderString().split(",");
                if (needSpace) {
                    itemsAffected.add("\n\n");
                }
                itemsAffected.add(files_replaced_remote_string+":");
                updateFilesAffected(songs,sets);
            }

            // Dismiss this fragment and ask if the user is sure!
            dismiss();
            AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet(
                    "openChordsForcePush", force_push_warning_string, itemsAffected,
                    "openChordsFragment", openChordsFragment, null);
            areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "areYouSure");
        });
        myView.forcePull.setOnClickListener(view -> {
            itemsAffected.clear();

            // Add a note of any items that will be deleted in the local folder
            // These are the ones that aren't in the remote folder)
            boolean needSpace = false;
            if (mainActivityInterface.getOpenChordsAPI().getSongsNotOnServerCount()!=0 ||
                    mainActivityInterface.getOpenChordsAPI().getSetListsNotOnServerCount()!=0) {
                String[] songs = mainActivityInterface.getOpenChordsAPI().getSongsNotOnServerString().split(",");
                String[] sets = mainActivityInterface.getOpenChordsAPI().getSetListsNotOnServerString().split(",");
                itemsAffected.add(files_deleted_local_string+":");
                updateFilesAffected(songs, sets);
                needSpace = true;
            }

            // Add a note of any newer items that will be replaced in the local folder by older server items
            if (mainActivityInterface.getOpenChordsAPI().getSongsOnServerOlderCount()!=0 ||
                    mainActivityInterface.getOpenChordsAPI().getSetListsOnServerOlderCount()!=0) {
                String[] songs = mainActivityInterface.getOpenChordsAPI().getSongsOnServerOlderString().split(",");
                String[] sets = mainActivityInterface.getOpenChordsAPI().getSetListsOnServerOlderString().split(",");
                if (needSpace) {
                    itemsAffected.add("\n\n");
                }
                itemsAffected.add(files_replaced_local_string+":");
                updateFilesAffected(songs,sets);
            }

            // Dismiss this fragment and ask if the user is sure!
            dismiss();

            AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet(
                    "openChordsForcePull", force_pull_warning_string, itemsAffected,
                    "openChordsFragment", openChordsFragment, null);
            areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "areYouSure");
        });
    }

    private void updateFilesAffected(String[] songs, String[] sets) {
        for (String song : songs) {
            song = song.trim();
            if (!song.isEmpty()) {
                itemsAffected.add(song_string + ": " + song);
            }
        }
        for (String set : sets) {
            set = set.trim();
            if (!set.isEmpty()) {
                itemsAffected.add(set_list_string + ": " + set);
            }
        }
    }

}