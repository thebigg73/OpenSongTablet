package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownSelection;
import com.garethevans.church.opensongtablet.databinding.EditSongMainBinding;
import com.garethevans.church.opensongtablet.interfaces.EditSongMainFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class EditSongFragmentMain extends Fragment implements EditSongMainFragmentInterface  {

    // The variable used in this fragment
    private EditSongMainBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String newFolder;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    //TODO
    // If editing a current song and location changes, check new location doesn't exist and warn
    // The code to initialise this fragment
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = EditSongMainBinding.inflate(inflater, container, false);

        // Initialise the views
        setupValues();

        // Set listeners
        setUpListeners();

        return myView.getRoot();
    }

    // Initialise the views
    private void setupValues() {
        myView.title.setText(mainActivityInterface.getTempSong().getTitle());
        myView.author.setText(mainActivityInterface.getTempSong().getAuthor());
        myView.copyright.setText(mainActivityInterface.getTempSong().getCopyright());
        myView.songNotes.setText(mainActivityInterface.getTempSong().getNotes());
        myView.filename.setText(mainActivityInterface.getTempSong().getFilename());
        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders(getContext(), mainActivityInterface);
        newFolder = "+ " + getString(R.string.newfolder);
        folders.add(newFolder);
        ExposedDropDownArrayAdapter arrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, folders);
        myView.folder.setAdapter(arrayAdapter);
        ExposedDropDownSelection exposedDropDownSelection = new ExposedDropDownSelection();
        exposedDropDownSelection.keepSelectionPosition(myView.folder, folders);
        myView.folder.setText(mainActivityInterface.getTempSong().getFolder());
    }

    // Sor the view visibility, listeners, etc.
    private void setUpListeners() {
        myView.title.addTextChangedListener(new MyTextWatcher("title"));
        myView.folder.addTextChangedListener(new MyTextWatcher("folder"));
        myView.filename.addTextChangedListener(new MyTextWatcher("filename"));
        myView.author.addTextChangedListener(new MyTextWatcher("author"));
        myView.copyright.addTextChangedListener(new MyTextWatcher("copyright"));
        myView.songNotes.addTextChangedListener(new MyTextWatcher("notes"));
    }

    private class MyTextWatcher implements TextWatcher {

        String what;
        String value = "";

        MyTextWatcher(String what) {
            this.what = what;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null) {
                value = s.toString();
                if (what.equals("filename") || what.equals("folder") && !value.equals(newFolder)) {
                    value = mainActivityInterface.getStorageAccess().safeFilename(value);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            mainActivityInterface.showSaveAllowed(mainActivityInterface.songChanged());
            updateTempSong();
        }

        public void updateTempSong() {
            switch (what) {
                case "folder":
                    if (value.equals(newFolder)) {
                        // TODO initiate new folder bottom sheet
                    } else {
                        mainActivityInterface.getTempSong().setFolder(value);
                    }
                    break;
                case "filename":
                    mainActivityInterface.getTempSong().setFilename(value);
                    break;
                case "title":
                    mainActivityInterface.getTempSong().setTitle(value);
                    break;
                case "copyright":
                    mainActivityInterface.getTempSong().setCopyright(value);
                    break;
                case "author":
                    mainActivityInterface.getTempSong().setAuthor(value);
                    break;
                case "notes":
                    mainActivityInterface.getTempSong().setNotes(value);
                    break;
            }
        }
    }


    // Finished with this view
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
