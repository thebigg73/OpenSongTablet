package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.databinding.BottomSheetFindAndReplaceBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class FindAndReplaceBottomSheet extends BottomSheetCommon {

    private MainActivityInterface mainActivityInterface;

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "FindAndReplaceBottomSheet";
    private final EditSongFragmentLyrics editSongFragmentLyrics;
    private BottomSheetFindAndReplaceBinding myView;
    private String what = "", with = "";
    private boolean matchCase = false;

    public FindAndReplaceBottomSheet(EditSongFragmentLyrics editSongFragmentLyrics) {
        this.editSongFragmentLyrics = editSongFragmentLyrics;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetFindAndReplaceBinding.inflate(inflater, container, false);

        prepareStrings();

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Set up views
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {

    }

    private void setupViews() {
        if (editSongFragmentLyrics!=null) {
            myView.editFind.setText(editSongFragmentLyrics.getSelectedText());
        }
    }

    private void setupListeners() {
        myView.btnFindNext.setOnClickListener((button) -> {
            what = myView.editFind.getText().toString();
            matchCase = myView.chkCaseSensitive.isChecked();
            findNext();
        });

        myView.btnReplace.setOnClickListener((button) -> {
            what = myView.editFind.getText().toString();
            with = myView.editReplace.getText().toString();
            matchCase = myView.chkCaseSensitive.isChecked();
            replace();
        });

        myView.btnReplaceAll.setOnClickListener((button) -> {
            what = myView.editFind.getText().toString();
            with = myView.editReplace.getText().toString();
            matchCase = myView.chkCaseSensitive.isChecked();
            replaceAll();
        });
    }

    private void findNext() {
        if (editSongFragmentLyrics!=null) {
            editSongFragmentLyrics.findNext(what, matchCase);
        }
    }

    private void replace() {
        if (editSongFragmentLyrics!=null) {
            editSongFragmentLyrics.replaceText(what, with, matchCase);
        }
    }

    private void replaceAll() {
        if (editSongFragmentLyrics!=null) {
            editSongFragmentLyrics.replaceAll(what, with, matchCase);
        }
    }

}
