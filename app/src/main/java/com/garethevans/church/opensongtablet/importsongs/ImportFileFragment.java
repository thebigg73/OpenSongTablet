package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.SettingsImportFileBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class ImportFileFragment extends Fragment {

    private final String TAG = "ImportFileFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsImportFileBinding myView;
    private Song newSong = new Song();
    private ArrayList<String> folders;
    private boolean isIMGorPDF;
    private String basename, requiredExtension;
    private Uri tempFile, copyTo;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsImportFileBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.import_from_file));
        myView.nestedScrollView.setFabToAnimate(myView.importButton);

        // Hide everything other than the progress bar while we process the song
        myView.progress.setVisibility(View.VISIBLE);
        myView.content.setVisibility(View.GONE);
        myView.importButton.setVisibility(View.GONE);
        myView.imageView.setVisibility(View.GONE);
        myView.folder.setVisibility(View.GONE);
        myView.filename.setVisibility(View.GONE);

        new Thread(() -> {
            // Get the available folders and base name
            folders = mainActivityInterface.getSQLiteHelper().getFolders();
            basename = mainActivityInterface.getImportFilename().replaceAll("\\.[^.]*$", "");
            newSong.setFilename(mainActivityInterface.getImportFilename());
            isIMGorPDF = mainActivityInterface.getStorageAccess().isIMGorPDF(newSong);
            Log.d(TAG,"isIMGorPDF:"+isIMGorPDF);
            Log.d(TAG,"filename:"+mainActivityInterface.getImportFilename());
            Log.d(TAG,"uri:"+mainActivityInterface.getImportUri());
            Log.d(TAG,"basename:"+basename);
            requiredExtension = "";
            if (mainActivityInterface.getImportFilename().contains(".") && isIMGorPDF) {
                requiredExtension = mainActivityInterface.getImportFilename().substring(mainActivityInterface.getImportFilename().lastIndexOf("."));
            }
            Log.d(TAG,"required extension:"+requiredExtension);

            // Try to read in the song using the import information.  This copies to the Variations/_cache folder
            readInFile();

            requireActivity().runOnUiThread(() -> {
                // Update the views and get the
                updateViews();

                // Add listeners
                setupListeners();

                myView.progress.setVisibility(View.GONE);
                myView.filename.setVisibility(View.VISIBLE);
                myView.folder.setVisibility(View.VISIBLE);
                myView.importButton.setVisibility(View.VISIBLE);
            });
        }).start();

        return myView.getRoot();
    }

    private void readInFile() {
        // Make a temporary copy of the song in the Variations/_cache folder
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri());
        tempFile = mainActivityInterface.getStorageAccess().getUriForItem("Variations","_cache",mainActivityInterface.getImportFilename());
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false,tempFile,null,"Variations","_cache",mainActivityInterface.getImportFilename());
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(tempFile);
        mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);

        if (isIMGorPDF) {
            myView.imageView.setVisibility(View.VISIBLE);
            if (mainActivityInterface.getImportFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                // Load in a preview if the version of Android is high enough
                Bitmap bmp = mainActivityInterface.getProcessSong().getBitmapFromPDF(null,null,1,200,200,"N");
                GlideApp.with(requireContext()).load(bmp).into(myView.imageView);
                newSong.setFiletype("PDF");
            } else {
                GlideApp.with(requireContext()).load(mainActivityInterface.getImportUri()).into(myView.imageView);
                newSong.setFiletype("IMG");
            }
        } else {
            try {
                newSong.setFolder("**Variation/_cache");
                newSong = mainActivityInterface.getLoadSong().doLoadSongFile(newSong,false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            myView.content.setText(newSong.getTitle());
            myView.content.setHintMonospace();
            myView.content.setHint(newSong.getLyrics());
        }
    }

    private void updateViews() {
        myView.filename.setText(basename);

        // Set up the folder exposed dropdown
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),myView.folder, R.layout.view_exposed_dropdown_item,folders);
        myView.folder.setAdapter(exposedDropDownArrayAdapter);
        // Default to the current folder
        myView.folder.setText(mainActivityInterface.getSong().getFolder());

        myView.content.setVisibility(View.VISIBLE);

        // Check if file with this name already exists
        checkIfFileExists();
    }

    private boolean checkIfFileExists() {
        newSong.setFilename(myView.filename.getText().toString());
        newSong.setTitle(myView.filename.getText().toString());
        newSong.setFolder(myView.folder.getText().toString());

        mainActivityInterface.setImportUri(mainActivityInterface.getStorageAccess().getUriForItem(
                "Songs",myView.folder.getText().toString(),myView.filename.getText().toString()));
        if (mainActivityInterface.getStorageAccess().uriExists(mainActivityInterface.getImportUri())) {
            myView.existing.setVisibility(View.VISIBLE);
            return true;
        } else {
            myView.existing.setVisibility(View.GONE);
            return false;
        }
    }

    private void setupListeners() {
        myView.filename.addTextChangedListener(new MyTextWatcher());
        myView.folder.addTextChangedListener(new MyTextWatcher());
        myView.importButton.setOnClickListener(v->doImport());
    }

    private void doImport() {
        // Only proceed if song doesn't exist or we have checked the overwrite button
        if (checkIfFileExists() && !myView.overwrite.isChecked()) {
            // Don't proceed
            mainActivityInterface.getShowToast().doIt(getString(R.string.file_exists));
        } else {
            // Create the new song location
            String folder = myView.folder.getText().toString();
            String filename = myView.filename.getText().toString();

            if (isIMGorPDF) {
                // Put the required extension back if it isn't there
                if (!requiredExtension.isEmpty() && !filename.contains(requiredExtension)) {
                    filename = filename + requiredExtension;
                }
                myView.filename.setText(filename);
            }

            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri());
            copyTo = mainActivityInterface.getStorageAccess().getUriForItem("Songs",myView.folder.getText().toString(),myView.filename.getText().toString());
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,copyTo,null,"Songs",myView.folder.getText().toString(),myView.filename.getText().toString());
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(copyTo);
            if (mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream)) {
                // Remove the temp file from the variations
                mainActivityInterface.getStorageAccess().deleteFile(tempFile);

                // Set the song
                mainActivityInterface.getSong().setFolder(folder);
                mainActivityInterface.getSong().setFilename(filename);
                mainActivityInterface.getPreferences().setMyPreferenceString("songFolder",folder);
                mainActivityInterface.getPreferences().setMyPreferenceString("songFilename",filename);
                mainActivityInterface.navHome();
            }
        }
    }
    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            checkIfFileExists();
        }
    }
}
