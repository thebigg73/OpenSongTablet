package com.garethevans.church.opensongtablet.drummer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetBeatBuddySongsBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BottomSheetBeatBuddySongs extends BottomSheetDialogFragment {

    // This class is used to allow users to browse the default songs on the device

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private static final String TAG = "BottomSheetBBSongs";
    private BottomSheetBeatBuddySongsBinding myView;
    private final BeatBuddyFragment callingFragment;
    private final BBSQLite bbsqLite;

    BottomSheetBeatBuddySongs(BeatBuddyFragment callingFragment, BBSQLite bbsqLite) {
        this.callingFragment = callingFragment;
        this.bbsqLite = bbsqLite;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
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
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = BottomSheetBeatBuddySongsBinding.inflate(inflater, container, false);

        myView.dialogHeading.setClose(this);

        setupExposedDropDowns();

        return myView.getRoot();
    }

    private void setupExposedDropDowns() {
        myView.progressBarSongs.setVisibility(View.VISIBLE);
        // Get the unique values for each of the folders, time signatures and drum kits
        ArrayList<String> uniqueFolders = bbsqLite.searchUniqueValues(bbsqLite.COLUMN_FOLDER);
        ArrayList<String> uniqueTimeSignatures = bbsqLite.searchUniqueValues(bbsqLite.COLUMN_SIGNATURE);
        ArrayList<String> uniqueDrumKits = bbsqLite.searchUniqueValues(bbsqLite.COLUMN_KIT);

        // Set up the adapters
        if (getContext()!=null) {

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                        ExposedDropDownArrayAdapter foldersAdapter = new ExposedDropDownArrayAdapter(
                                getContext(), myView.folder, R.layout.view_exposed_dropdown_item, uniqueFolders);
                        ExposedDropDownArrayAdapter timeSignaturesAdapter = new ExposedDropDownArrayAdapter(
                                getContext(), myView.timeSignature, R.layout.view_exposed_dropdown_item, uniqueTimeSignatures);
                        ExposedDropDownArrayAdapter drumKitsAdapter = new ExposedDropDownArrayAdapter(
                                getContext(), myView.drumKit, R.layout.view_exposed_dropdown_item, uniqueDrumKits);

                        Handler h = new Handler(Looper.getMainLooper());

                        h.post(() -> {
                            myView.folder.setAdapter(foldersAdapter);
                            myView.timeSignature.setAdapter(timeSignaturesAdapter);
                            myView.drumKit.setAdapter(drumKitsAdapter);
                            myView.folder.addTextChangedListener(new MyTextWatcher());
                            myView.timeSignature.addTextChangedListener(new MyTextWatcher());
                            myView.drumKit.addTextChangedListener(new MyTextWatcher());
                            getFoundSongs();
                        });
                    });
            }
    }

    private void getFoundSongs() {
        myView.progressBarSongs.setVisibility(View.VISIBLE);
        // This gets called onCreateView and when an ExposedDropdown changes
        ArrayList<BBSong> foundSongs = bbsqLite.getSongsByFilters(myView.folder.getText().toString(),
                myView.timeSignature.getText().toString(), myView.drumKit.getText().toString());

        if (getContext()!=null) {
            BBSongAdapter bbSongAdapter = new BBSongAdapter(getContext(), foundSongs, this);
            myView.songsFound.setLayoutManager(new LinearLayoutManager(getContext()));
            myView.songsFound.setAdapter(bbSongAdapter);
        }
        myView.progressBarSongs.setVisibility(View.GONE);
    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            getFoundSongs();
        }
    }

    public void updateSong(int folder, int song) {
        // Called from bottomsheet
        callingFragment.changeSong(folder,song);
    }
}
