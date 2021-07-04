package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetMenuSongsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongMenuBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetMenuSongsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final FloatingActionButton fab;

    SongMenuBottomSheet(FloatingActionButton fab) {
        this.fab = fab;
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
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetMenuSongsBinding.inflate(inflater,container,false);

        // Set up the views
        setListeners();

        return myView.getRoot();
    }

    private void setListeners() {
        // Set up the dialog title
        ((TextView)myView.dialogHeading.findViewById(R.id.title)).setText("");

        // Set up the song title
        String songTitle = mainActivityInterface.getSong().getTitle();
        if (songTitle==null || songTitle.isEmpty() || songTitle.equals("Welcome to OpenSongApp")) {
            myView.songActions.setVisibility(View.GONE);
            myView.addToSet.setVisibility(View.GONE);
        } else {
            myView.songActions.setVisibility(View.VISIBLE);
            myView.addToSet.setVisibility(View.VISIBLE);
            ((TextView)myView.songActions.findViewById(R.id.subText)).setText(songTitle);
            ((TextView)myView.addToSet.findViewById(R.id.subText)).setText(songTitle);
        }

        // Listener for buttons
        myView.dialogHeading.findViewById(R.id.close).setOnClickListener(v -> dismiss());
        myView.songActions.setOnClickListener(v -> navigateTo("opensongapp://settings/actions"));
        myView.newSongs.setOnClickListener(v -> navigateTo("opensongapp://settings/import"));
        myView.addToSet.setOnClickListener(v -> addToSet());
    }

    private void navigateTo(String deepLink) {
        mainActivityInterface.closeDrawer(true);
        mainActivityInterface.navigateToFragment(deepLink,0);
        dismiss();
    }

    private void addToSet() {
        // Firstly add the song to the current set
        mainActivityInterface.getCurrentSet().
                addToCurrentSet(mainActivityInterface.getSetActions().
                        getSongForSetWork(requireContext(),mainActivityInterface.getSong()));

    }

}