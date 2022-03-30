package com.garethevans.church.opensongtablet.songmenu;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetMenuSongsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SongMenuBottomSheet extends BottomSheetDialogFragment {

    private final String TAG = "SongMenuBottomSheet";
    private BottomSheetMenuSongsBinding myView;
    private MainActivityInterface mainActivityInterface;

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
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetMenuSongsBinding.inflate(inflater, container, false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Set up the views
        setupViews();
        setListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        // Set up the song title
        String songTitle = mainActivityInterface.getSong().getTitle();
        if (!mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
            myView.songSpecificActions.setVisibility(View.GONE);
            myView.otherOptions.setVisibility(View.GONE);
        } else {
            myView.songSpecificActions.setVisibility(View.VISIBLE);
            myView.otherOptions.setVisibility(View.VISIBLE);
            myView.songTitle.setHint(getString(R.string.file)+": "+songTitle);
        }
        // Check we have songs in the menu
        if (mainActivityInterface.getSongsFound("song").size()>0) {
            myView.randomSong.setVisibility(View.VISIBLE);
        } else {
            myView.randomSong.setVisibility(View.GONE);
        }
    }

    private void setListeners() {


        // Listener for buttons
        myView.songEdit.setOnClickListener(v -> navigateTo("opensongapp://settings/edit"));
        myView.songActions.setOnClickListener(v -> navigateTo("opensongapp://settings/actions"));
        myView.newSongs.setOnClickListener(v -> navigateTo("opensongapp://settings/import"));
        myView.addToSet.setOnClickListener(v -> addToSet());
        myView.addVariationToSet.setOnClickListener(v -> addVariationToSet());
        myView.randomSong.setOnClickListener(v -> {
            RandomSongBottomSheet randomSongBottomSheet = new RandomSongBottomSheet("song");
            randomSongBottomSheet.show(requireActivity().getSupportFragmentManager(),"RandomBottomSheet");
            dismiss();
        });
        myView.rebuildIndex.setOnClickListener(v -> {
            if (mainActivityInterface.getSongListBuildIndex().getIndexComplete()) {
                mainActivityInterface.getSongListBuildIndex().buildBasicFromFiles();
                mainActivityInterface.indexSongs();
                dismiss();
            } else {
                dismiss();
                mainActivityInterface.getShowToast().doIt(getString(R.string.search_index_wait));
            }
        });
    }

    private void navigateTo(String deepLink) {
        mainActivityInterface.closeDrawer(true);
        mainActivityInterface.navigateToFragment(deepLink, 0);
        dismiss();
    }

    private void addToSet() {
        // For a received song (which is about to become a variation) use the stored received song filename
        // TODO from IV pull request #136 - UNTESTED
        if (mainActivityInterface.getSong().getFilename().equals("ReceivedSong")) {
            mainActivityInterface.getSong().setFilename(mainActivityInterface.getSong().getTitle());
        }

        // Add the song to the current set
        addToCurrentSet();

        // Let the user know and close
        alertSuccess(mainActivityInterface.getSong().getFilename() + " " +
                getString(R.string.added_to_set));
    }

    private void addVariationToSet() {
        // For a received song (which is about to become a variation) use the stored received song filename
        // TODO from IV pull request #136 - UNTESTED
        if (mainActivityInterface.getSong().getFilename().equals("ReceivedSong")) {
            mainActivityInterface.getSong().setFilename(mainActivityInterface.getSong().getTitle());
        }

        // Add the song to the current set
        addToCurrentSet();

        // Now change it to a variation
        int position = mainActivityInterface.getCurrentSet().getSetItems().size() - 1;
        if (position>=0) {
            mainActivityInterface.getSetActions().makeVariation(position);
        }

        // Let the user know and close
        alertSuccess(getString(R.string.variation) + " " +
                mainActivityInterface.getSong().getFilename() + " " +
                getString(R.string.added_to_set));
    }

    private void addToCurrentSet() {
        String songforsetwork = mainActivityInterface.getSetActions().getSongForSetWork(mainActivityInterface.getSong());
        Log.d(TAG,"songforsetwork="+songforsetwork);
        mainActivityInterface.getCurrentSet().addToCurrentSetString(songforsetwork);
        mainActivityInterface.getCurrentSet().addSetItem(songforsetwork);
        mainActivityInterface.getCurrentSet().addSetValues(mainActivityInterface.getSong());
    }

    private void alertSuccess(String message) {
        mainActivityInterface.getShowToast().doIt(message);

        // Now send the call to update the set menu fragment
        mainActivityInterface.updateSetList();

        dismiss();
    }
}