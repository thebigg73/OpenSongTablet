package com.garethevans.church.opensongtablet.pdf;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetPdfExtractBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PDFExtractBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetPdfExtractBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final ArrayList<String> incomingPages;
    private final String filename;
    private String website_ocr_string="", create_new_song_string="",
            save_text_for_searching_string="", deeplink_edit_string="";
    public PDFExtractBottomSheet(ArrayList<String> incomingPages, String filename) {
        this.incomingPages = incomingPages;
        this.filename = filename;
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
        myView = BottomSheetPdfExtractBinding.inflate(inflater, container, false);

        prepareStrings();

        myView.dialogHeader.setClose(this);
        myView.dialogHeader.setWebHelp(mainActivityInterface, website_ocr_string);

        // Set up the lyrics
        setupLyrics();

        // Set up the views
        setupViews();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            website_ocr_string = getString(R.string.website_ocr);
            create_new_song_string = getString(R.string.create_new_song);
            save_text_for_searching_string = getString(R.string.save_text_for_searching);
            deeplink_edit_string = getString(R.string.deeplink_edit);
        }
    }
    private void setupLyrics() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string:incomingPages) {
            stringBuilder.append(string).append("\n\n");
        }

        String extractedText = mainActivityInterface.getConvertTextSong().convertText(stringBuilder.toString());
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.extractedText);
        myView.extractedText.setText(extractedText);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.extractedText,20);
        myView.extractedText.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("editTextSize",14f));

    }

    private void setupViews() {
        // Add the current song name (minus extension) to the textbox
        if (filename.contains(".")) {
            myView.filename.setText(filename.substring(0,filename.lastIndexOf(".")));
        } else {
            myView.filename.setText(filename);
        }

        // Make sure the switch is on and set the listener for changes
        myView.createNewSong.setChecked(true);
        myView.createNewSong.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                myView.newSongLayout.setVisibility(View.VISIBLE);
                myView.saveSearchable.setText(create_new_song_string);
            } else {
                myView.newSongLayout.setVisibility(View.GONE);
                myView.saveSearchable.setText(save_text_for_searching_string);
            }
        });

        // Populate the folder options
        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.folder, R.layout.view_exposed_dropdown_item, folders);
            myView.folder.setAdapter(exposedDropDownArrayAdapter);
        }
        myView.folder.setText(mainActivityInterface.getSong().getFolder());

        // Set the save listener
        myView.saveSearchable.setOnClickListener(view -> {
            if (myView.createNewSong.isChecked() && myView.filename.getText()!=null &&
            !myView.filename.getText().toString().isEmpty()) {
                // Create a new song
                Song newSong = new Song();
                newSong.setFiletype("XML");
                newSong.setFilename(myView.filename.getText().toString());
                newSong.setFolder(myView.folder.getText().toString());
                newSong.setTitle(myView.filename.getText().toString());
                newSong.setLyrics(myView.extractedText.getText().toString());
                mainActivityInterface.setSong(newSong);
                mainActivityInterface.getSQLiteHelper().createSong(newSong.getFolder(),newSong.getFilename());
                mainActivityInterface.getSQLiteHelper().updateSong(newSong);
                mainActivityInterface.getSaveSong().doSave(newSong);
            } else {
                //Add to the persistent database only
                mainActivityInterface.getSong().setLyrics(myView.extractedText.getText().toString());
                mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),true);
            }
            // Open the edit song page so the user can check for updates
            mainActivityInterface.navigateToFragment(deeplink_edit_string,0);
            dismiss();
        });
    }
}
