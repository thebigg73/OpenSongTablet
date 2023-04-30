package com.garethevans.church.opensongtablet.beatbuddy;

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
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BottomSheetBeatBuddySongs extends BottomSheetDialogFragment {

    // This class is used to allow users to browse the default songs on the device

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BottomSheetBBSongs";
    private MainActivityInterface mainActivityInterface;
    private BottomSheetBeatBuddySongsBinding myView;
    private final BBCommandsFragment callingFragment;
    private final BBSQLite bbsqLite;
    private ArrayList<String> uniqueFolders, uniqueTimeSignatures, uniqueDrumKits,
            myUniqueFolders;
    private ExposedDropDownArrayAdapter foldersAdapter, timeSignaturesAdapter, drumKitsAdapter,
            myfoldersAdapter;

    BottomSheetBeatBuddySongs(BBCommandsFragment callingFragment, BBSQLite bbsqLite) {
        this.callingFragment = callingFragment;
        this.bbsqLite = bbsqLite;
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
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = BottomSheetBeatBuddySongsBinding.inflate(inflater, container, false);

        myView.dialogHeading.setClose(this);

        setListeners();

        setupExposedDropDowns();

        return myView.getRoot();
    }

    private void setListeners() {
        myView.folder.addTextChangedListener(new MyTextWatcher());
        myView.timeSignature.addTextChangedListener(new MyTextWatcher());
        myView.drumKit.addTextChangedListener(new MyTextWatcher());
        myView.useDefaultOrImported.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getBeatBuddy().setBeatBuddyUseImported(value==1);
            myView.folder.setText("");
            changeViewsDefaultOrImported();
        });
        myView.useDefaultOrImported.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getBeatBuddy().setBeatBuddyUseImported(myView.useDefaultOrImported.getValue()==1);
                myView.folder.setText("");
                changeViewsDefaultOrImported();
            }
        });
    }
    private void setupExposedDropDowns() {
        myView.progressBarSongs.setVisibility(View.VISIBLE);

        // Set up the adapters
        if (getContext() != null) {

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                // Get the unique values for each of the folders, time signatures and drum kits
                uniqueFolders = bbsqLite.searchUniqueValues(bbsqLite.COLUMN_FOLDER_NUM + "," +
                        bbsqLite.COLUMN_FOLDER_NAME, bbsqLite.TABLE_NAME_DEFAULT_SONGS, bbsqLite.COLUMN_FOLDER_NUM);
                uniqueTimeSignatures = bbsqLite.searchUniqueValues(bbsqLite.COLUMN_SIGNATURE,
                        bbsqLite.TABLE_NAME_DEFAULT_SONGS, bbsqLite.COLUMN_SIGNATURE);
                uniqueDrumKits = bbsqLite.searchUniqueValues(bbsqLite.COLUMN_KIT_NUM, bbsqLite.TABLE_NAME_DEFAULT_SONGS, bbsqLite.COLUMN_KIT_NUM);
                myUniqueFolders = bbsqLite.searchUniqueValues(bbsqLite.COLUMN_FOLDER_NUM + "," +
                        bbsqLite.COLUMN_FOLDER_NAME, bbsqLite.TABLE_NAME_MY_SONGS, bbsqLite.COLUMN_FOLDER_NUM);

                myView.useDefaultOrImported.setSliderPos(mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported() ? 1:0);

                foldersAdapter = new ExposedDropDownArrayAdapter(
                        getContext(), myView.folder, R.layout.view_exposed_dropdown_item, uniqueFolders);
                timeSignaturesAdapter = new ExposedDropDownArrayAdapter(
                        getContext(), myView.timeSignature, R.layout.view_exposed_dropdown_item, uniqueTimeSignatures);
                drumKitsAdapter = new ExposedDropDownArrayAdapter(
                        getContext(), myView.drumKit, R.layout.view_exposed_dropdown_item, uniqueDrumKits);
                myfoldersAdapter = new ExposedDropDownArrayAdapter(
                        getContext(), myView.folder, R.layout.view_exposed_dropdown_item, myUniqueFolders);

                Handler h = new Handler(Looper.getMainLooper());

                h.post(() -> {
                    changeViewsDefaultOrImported();
                    getFoundSongs();
                });
            });
        }
    }

    private void changeViewsDefaultOrImported() {
        if (mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported()) {
            myView.timeSignature.setAdapter(null);
            myView.drumKit.setAdapter(null);
            myView.folder.setAdapter(myfoldersAdapter);
        } else {
            myView.timeSignature.setAdapter(timeSignaturesAdapter);
            myView.drumKit.setAdapter(drumKitsAdapter);
            myView.folder.setAdapter(foldersAdapter);
        }
        myView.timeSignature.setVisibility(mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported() ? View.GONE:View.VISIBLE);
        myView.drumKit.setVisibility(mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported() ? View.GONE:View.VISIBLE);
        getFoundSongs();
    }


    private void getFoundSongs() {
        myView.progressBarSongs.setVisibility(View.VISIBLE);
        // This gets called onCreateView and when an ExposedDropdown changes
        ArrayList<BBSong> foundSongs;
        if (mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported()) {
            foundSongs = bbsqLite.getMySongsByFolder(myView.folder.getText().toString());
        } else {
            foundSongs = bbsqLite.getSongsByFilters(myView.folder.getText().toString(),
                    myView.timeSignature.getText().toString(), myView.drumKit.getText().toString());
        }
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
