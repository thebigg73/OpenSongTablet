package com.garethevans.church.opensongtablet.openchords;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetOpenchordsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class OpenChordsBottomSheet extends BottomSheetDialogFragment {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetOpenchordsBinding myView;
    private final String TAG = "OpenChordsBottomSheet";
    private final String what;
    private final OpenChordsFragment openChordsFragment;
    private String download_title_string="", upload_title_string="", download_new_items_string="",
            upload_new_items_string="", update_local_items_string="", update_remote_items_string="";
    OpenChordsBottomSheet(OpenChordsFragment openChordsFragment, String what) {
        this.openChordsFragment = openChordsFragment;
        this.what = what;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = BottomSheetOpenchordsBinding.inflate(inflater, null, false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        prepareStrings();

        setupViews();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            download_title_string = getString(R.string.sync_download_from_openchords);
            upload_title_string = getString(R.string.sync_upload_to_openchords);
            upload_new_items_string = getString(R.string.sync_upload_new_items);
            download_new_items_string = getString(R.string.sync_download_new_items);
            update_local_items_string = getString(R.string.sync_update_local_items);
            update_remote_items_string = getString(R.string.sync_update_remote_items);
        }
    }
    private void setupViews() {

        //TODO
        for (OpenChordsCompareObject object:mainActivityInterface.getOpenChordsAPI().getLocalSongsCompareObjects()) {
            Log.d(TAG,"local "+object.getType()+": "+object.getTitle());
        }
        for (OpenChordsCompareObject object:mainActivityInterface.getOpenChordsAPI().getServerSongsCompareObjects()) {
            Log.d(TAG,"server "+object.getType()+": "+object.getTitle());
        }

        switch (what) {
            case "download":
                // Sort the title
                myView.dialogHeading.setText(download_title_string);

                // New songs on the server that need downloaded
                myView.newSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsNotOnLocalCount()>0 ? View.VISIBLE:View.GONE);
                myView.newSongs.setHint(mainActivityInterface.getOpenChordsAPI().getSongsNotOnLocalString());
                myView.newSongsAction.setText(download_new_items_string);

                // Songs that need updated in the local folder
                myView.updateSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsOnLocalOlderCount()>0 ? View.VISIBLE:View.GONE);
                myView.updateSongs.setHint(mainActivityInterface.getOpenChordsAPI().getSetListsOnLocalOlderString());
                myView.updateSongsAction.setText(update_local_items_string);

                // New sets on the server that need downloaded
                myView.newSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnLocalCount()>0 ? View.VISIBLE:View.GONE);
                myView.newSets.setHint(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnLocalString());
                myView.newSetsAction.setText(download_new_items_string);

                // Sets that need updated in the local folder
                myView.updateSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsOnLocalOlderCount()>0 ? View.VISIBLE:View.GONE);
                myView.updateSets.setHint(mainActivityInterface.getOpenChordsAPI().getSetListsOnLocalOlderString());
                myView.updateSetsAction.setText(update_local_items_string);

                break;

            case "upload":
                // Sort the title
                myView.dialogHeading.setText(upload_title_string);

                // New songs in the local folder that need uploaded
                myView.newSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsNotOnServerCount()>0 ? View.VISIBLE:View.GONE);
                myView.newSongs.setHint(mainActivityInterface.getOpenChordsAPI().getSongsNotOnServerString());
                myView.newSongsAction.setText(upload_new_items_string);

                // Songs that need updated in the remote folder
                myView.updateSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsOnServerOlderCount()>0 ? View.VISIBLE:View.GONE);
                myView.updateSongs.setHint(mainActivityInterface.getOpenChordsAPI().getSetListsOnServerOlderString());
                myView.updateSongsAction.setText(update_remote_items_string);

                // New sets in the local folder that need uploads
                myView.newSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnServerCount()>0 ? View.VISIBLE:View.GONE);
                myView.newSets.setHint(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnServerString());
                myView.newSetsAction.setText(upload_new_items_string);

                // Sets that need updated in the remote folder
                myView.updateSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsOnServerOlderCount()>0 ? View.VISIBLE:View.GONE);
                myView.updateSets.setHint(mainActivityInterface.getOpenChordsAPI().getSetListsOnServerOlderString());
                myView.updateSetsAction.setText(update_remote_items_string);
                break;
        }

        // Show the no changes required if appropriate
        myView.songsNoChangesRequired.setVisibility(
                myView.newSongsLayout.getVisibility()==View.VISIBLE ||
                        myView.updateSongsLayout.getVisibility()==View.VISIBLE ?
                        View.GONE:View.VISIBLE);
        myView.setsNoChangesRequired.setVisibility(
                myView.newSetsLayout.getVisibility()==View.VISIBLE ||
                        myView.updateSetsLayout.getVisibility()==View.VISIBLE ?
                        View.GONE:View.VISIBLE);

    }

    private void setListeners() {
        switch (what) {
            case "download":

                break;

            case "upload":
                break;
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

}
