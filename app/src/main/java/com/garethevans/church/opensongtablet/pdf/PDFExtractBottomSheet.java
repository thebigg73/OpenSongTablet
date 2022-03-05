package com.garethevans.church.opensongtablet.pdf;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetPdfExtractBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PDFExtractBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetPdfExtractBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final ArrayList<String> incomingPages;
    private final String filename;

    public PDFExtractBottomSheet(ArrayList<String> incomingPages, String filename) {
        this.incomingPages = incomingPages;
        this.filename = filename;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = BottomSheetPdfExtractBinding.inflate(inflater, container, false);

        myView.dialogHeader.setClose(this);

        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        // Set up the lyrics
        setupLyrics();

        // Set up the views
        setupViews();

        return myView.getRoot();
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
                myView.saveSearchable.setText(R.string.create_new_song);
            } else {
                myView.newSongLayout.setVisibility(View.GONE);
                myView.saveSearchable.setText(getString(R.string.save_text_for_searching));
            }
        });

        // Populate the folder options
        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),myView.folder,R.layout.view_exposed_dropdown_item,folders);
        myView.folder.setAdapter(exposedDropDownArrayAdapter);
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
                mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong());
            }
            // Open the edit song page so the user can check for updates
            mainActivityInterface.navigateToFragment("opensongapp://settings/edit",0);
            dismiss();
        });
    }
}
