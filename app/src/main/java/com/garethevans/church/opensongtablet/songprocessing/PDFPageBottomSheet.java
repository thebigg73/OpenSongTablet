package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.databinding.BottomSheetPdfPageBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PDFPageBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetPdfPageBinding myView;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetPdfPageBinding.inflate(inflater, container, false);

        // Set up views
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        int isPDFVisible;
        int isNotPDFVisible;
        if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            isPDFVisible = View.VISIBLE;
            isNotPDFVisible = View.GONE;
            myView.pageSlider.setValueFrom(1);
            myView.pageSlider.setValueTo(mainActivityInterface.getPDFSong().getPdfPageCount());
            myView.pageSlider.setValue(mainActivityInterface.getPDFSong().getPdfPageCurrent());
            myView.pageNumber.setText(mainActivityInterface.getPDFSong().getPdfPageCurrent());
        } else {
            isPDFVisible = View.GONE;
            isNotPDFVisible = View.VISIBLE;
        }
        myView.pagesNotavailable.setVisibility(isNotPDFVisible);
        myView.pageSlider.setVisibility(isPDFVisible);
        myView.pageNumber.setVisibility(isPDFVisible);
        myView.nextPage.setVisibility(isPDFVisible);
        myView.previousPage.setVisibility(isPDFVisible);
        checkButtonEnable(false);
    }

    private void setupListeners() {
        myView.previousPage.setOnClickListener(v -> {
            if (mainActivityInterface.getPDFSong().getPdfPageCurrent()>1) {
                mainActivityInterface.getPDFSong().setPdfPageCurrent(mainActivityInterface.getPDFSong().getPdfPageCurrent()-1);
            }
            checkButtonEnable(true);
        });
        myView.nextPage.setOnClickListener(v -> {
            if (mainActivityInterface.getPDFSong().getPdfPageCurrent()<mainActivityInterface.getPDFSong().getPdfPageCount()) {
                mainActivityInterface.getPDFSong().setPdfPageCurrent(mainActivityInterface.getPDFSong().getPdfPageCurrent()+1);
            }
            checkButtonEnable(true);
        });
        myView.pageSlider.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getPDFSong().setPdfPageCurrent((int)value);
            checkButtonEnable(true);
        });
    }

    private void checkButtonEnable(boolean hasChanged) {
        myView.previousPage.setEnabled(mainActivityInterface.getPDFSong().getPdfPageCurrent() > 1);
        myView.nextPage.setEnabled(mainActivityInterface.getPDFSong().getPdfPageCurrent() < mainActivityInterface.getPDFSong().getPdfPageCount());
        if (hasChanged) {
            // Update the page number text
            myView.pageNumber.setText(mainActivityInterface.getPDFSong().getPdfPageCurrent());
            // TODO action move to page
        }
    }
}
