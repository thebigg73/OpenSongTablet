package com.garethevans.church.opensongtablet.songprocessing;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetSongCreateBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class CreateSongBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetSongCreateBinding myView;

    private MainActivityInterface mainActivityInterface;

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "CreateSongBottomSheet";
    private String website_song_new="", create_new_song="",
            not_saved_filename="", file_exists="", deeplink_edit;
    private ArrayList<String> foldersFound;

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
        myView = BottomSheetSongCreateBinding.inflate(inflater, container, false);

        prepareStrings();

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);
        myView.dialogHeading.setText(create_new_song);
        myView.dialogHeading.setWebHelp(mainActivityInterface,website_song_new);

        // Set up views
        setupViews();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            website_song_new = getString(R.string.website_song_new);
            create_new_song = getString(R.string.create_new_song);
            not_saved_filename = getString(R.string.not_saved_filename);
            file_exists = getString(R.string.file_exists);
            deeplink_edit = getString(R.string.deeplink_edit);
        }
    }

    private void setupViews() {
        // Run as thread as long as we have context
        if (myView!=null && getContext()!=null) {
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                // Get the list of folders for the exposed dropdown
                foldersFound = mainActivityInterface.getSQLiteHelper().getFolders();

                // Now back to the main UI to deal with the views
                mainActivityInterface.getMainHandler().post(() -> {
                    // Set up the folder dropdown
                    ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.folderDropdown, R.layout.view_exposed_dropdown_item, foldersFound);
                    myView.folderDropdown.setAdapter(exposedDropDownArrayAdapter);
                    myView.folderDropdown.setText(mainActivityInterface.getSong().getFolder());

                    // Add a filename and folder text watcher to check for errors
                    myView.filenameEditText.setErrorEnabled(true);
                    myView.filenameEditText.addTextChangedListener(new MyTextWatcher());
                    myView.folderDropdown.addTextChangedListener(new MyTextWatcher());

                    // Set a blank filename (so it isn't null) - this should trigger the error
                    myView.filenameEditText.setText("");

                    // Set up the continue button listener
                    myView.continueButton.setOnClickListener((view) -> doContinue(myView.folderDropdown.getText().toString(), myView.filenameEditText.getText().toString()));
                });
            });
        }
    }

    private class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            // Check for null or empty test
            String folder = "";
            if (myView.folderDropdown.getText()!=null) {
                folder = myView.folderDropdown.getText().toString();
            }
            String filename = "";
            if (myView.filenameEditText.getText()!=null) {
                filename = myView.filenameEditText.getText().toString();
            }
            if (filename.isEmpty()) {
                myView.filenameEditText.setError(not_saved_filename);
                myView.continueButton.setEnabled(false);
                myView.continueButton.setAlpha(0.5f);
            } else if (mainActivityInterface.getSQLiteHelper().songExists(folder, filename)) {
                myView.filenameEditText.setError(file_exists);
                myView.continueButton.setEnabled(false);
                myView.continueButton.setAlpha(0.5f);
            } else {
                myView.filenameEditText.setError(null);
                myView.continueButton.setEnabled(true);
                myView.continueButton.setAlpha(1f);
            }
        }
    }
    private void doContinue(String folder, String filename) {
        // Do this on a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // We should be good to proceed
            // Prepare the new song for editing
            mainActivityInterface.setSong(new Song());
            mainActivityInterface.getSong().setFolder(folder);
            mainActivityInterface.getSong().setFilename(filename);
            mainActivityInterface.getSong().setTitle(filename);

            // Now go to the song edit window and close this dialogue
            mainActivityInterface.navigateToFragment(deeplink_edit, 0);
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
