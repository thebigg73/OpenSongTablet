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

import com.garethevans.church.opensongtablet.databinding.BottomSheetMenuSongsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SongMenuBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetMenuSongsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final String TAG = "SongMenuBottomSheet";

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
        setListeners();

        return myView.getRoot();
    }

    private void setListeners() {
        // Set up the song title
        String songTitle = mainActivityInterface.getSong().getTitle();
        Log.d(TAG,"songTitle: "+songTitle);
        if (songTitle == null || songTitle.isEmpty() || songTitle.equals("Welcome to OpenSongApp")) {
            myView.songActions.setVisibility(View.GONE);
            myView.addToSet.setVisibility(View.GONE);
        } else {
            myView.songActions.setVisibility(View.VISIBLE);
            myView.addToSet.setVisibility(View.VISIBLE);
            myView.songActions.setHint(songTitle);
            myView.addToSet.setHint(songTitle);
        }
        // Check we have songs in the menu
        if (mainActivityInterface.getSongsFound("song").size()>0) {
            myView.randomSong.setVisibility(View.VISIBLE);
        } else {
            myView.randomSong.setVisibility(View.GONE);
        }

        // Listener for buttons
        myView.songEdit.setOnClickListener(v -> navigateTo("opensongapp://settings/edit"));
        myView.songActions.setOnClickListener(v -> navigateTo("opensongapp://settings/actions"));
        myView.newSongs.setOnClickListener(v -> navigateTo("opensongapp://settings/import"));
        myView.addToSet.setOnClickListener(v -> addToSet());
        myView.randomSong.setOnClickListener(v -> {
            RandomSongBottomSheet randomSongBottomSheet = new RandomSongBottomSheet("song");
            randomSongBottomSheet.show(requireActivity().getSupportFragmentManager(),"RandomBottomSheet");
            dismiss();
        });
    }

    private void navigateTo(String deepLink) {
        mainActivityInterface.closeDrawer(true);
        mainActivityInterface.navigateToFragment(deepLink, 0);
        dismiss();
    }

    private void addToSet() {
        // Firstly add the song to the current set
        mainActivityInterface.getCurrentSet().
                addToCurrentSet(mainActivityInterface.getSetActions().
                        getSongForSetWork(requireContext(), mainActivityInterface.getSong()));
        // Now send the call to update the set menu fragment
        mainActivityInterface.updateSetList();
    }

}