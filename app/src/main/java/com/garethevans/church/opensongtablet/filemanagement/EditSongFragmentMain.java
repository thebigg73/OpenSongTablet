package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.chords.TransposeDialogFragment;
import com.garethevans.church.opensongtablet.databinding.FragmentEditSong1Binding;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentMainInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditSongFragmentMain extends Fragment implements EditSongFragmentMainInterface {

    // The helper classes used
    private Preferences preferences;
    private StorageAccess storageAccess;
    private SQLiteHelper sqLiteHelper;
    private EditContent editContent;

    // The variable used in this fragment
    private FragmentEditSong1Binding myView;
    MaterialButton openSongFormat, chordProFormat, transpose, autoFix;
    MaterialButtonToggleGroup formatButton;
    TextInputEditText title, artist, lyrics;
    AutoCompleteTextView folder, key;
    int activeColor, inactiveColor;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> foundFolders;
    private ExposedDropDownArrayAdapter folderArrayAdapter, keyArrayAdapter;
    private List<String> keys;
    boolean changes;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.registerFragment(this,"EditSongFragmentMain");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.registerFragment(null,"EditSongFragmentMain");
    }

    //TODO
    // If editing a current song and location changes, check new location doesn't exist and warn
    // Folder menu shoud include + NEW... option
    // View 2: Song features (pad, tempo, duration, timesig, capo, midi, links)
    // View 3: Copyright, Presentation order, score, notes, search tags

    // The code to initialise this fragment
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = FragmentEditSong1Binding.inflate(inflater, container, false);

        // Initialise helpers
        initialiseHelpers();

        // Hide the main action button and send the fragment reference if it was showing
        mainActivityInterface.hideActionButton(true);

        // Initialise the views
        initialiseViews();

        // Set up the drop down lists
        setUpDropDowns();

        // Set the current values
        setCurrentValues();

        // Set listeners
        setUpListeners();

        return myView.getRoot();
    }

    // Initialise the views
    private void initialiseViews() {
        folder = myView.folderName;
        title = myView.title;
        key = myView.key;
        artist = myView.artist;
        lyrics = myView.lyrics;

        activeColor = requireContext().getResources().getColor(R.color.colorSecondary);
        inactiveColor = requireContext().getResources().getColor(R.color.colorPrimary);

        openSongFormat = myView.openSongFormat;
        chordProFormat = myView.chordProFormat;
        formatButton = myView.formatButton;

        transpose = myView.transpose;
        autoFix = myView.autoFix;
    }

    // Set up the drop down lists
    private void setUpDropDowns() {
        new Thread(() -> {
            foundFolders = sqLiteHelper.getFolders(getActivity());
            folderArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, foundFolders);
            keyArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, getResources().getStringArray(R.array.key_choice));
            getActivity().runOnUiThread(() -> {
                folder.setAdapter(folderArrayAdapter);
                if (editContent.getFolder() != null) {
                    int pos = foundFolders.indexOf(editContent.getFolder());
                    if (pos >= 0) {
                        folder.setText(foundFolders.get(pos));
                    }
                }
                key.setAdapter(keyArrayAdapter);
                keys = Arrays.asList(getResources().getStringArray(R.array.key_choice));
                if (editContent.getKey() != null) {
                    int pos = keys.indexOf(editContent.getKey());
                    if (pos >= 0) {
                        key.setText(keys.get(pos));
                    }
                }
            });
        }).start();
    }

    // Set the current loaded values
    private void setCurrentValues() {
        title.setText(editContent.getTitle());
        artist.setText(editContent.getArtist());
        lyrics.setText(editContent.getLyrics());
    }

    // Deal with the edit mode
    private void dealWithEditMode(boolean openSongFormatting) {
        preferences.setMyPreferenceBoolean(requireContext(), "editAsChordPro", !openSongFormatting);
        Log.d("d", "Display as OpenSongFormat" + openSongFormatting);
    }

    // Finished with this view
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }


    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        preferences = new Preferences();
        storageAccess = new StorageAccess();
        sqLiteHelper = new SQLiteHelper(getActivity());
        editContent = new EditContent();
    }


    // Sor the view visibility, listeners, etc.
    private void setUpListeners() {
        openSongFormat.addOnCheckedChangeListener((button, isChecked) -> {
            openSongFormat.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            chordProFormat.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
            dealWithEditMode(true);
        });
        chordProFormat.addOnCheckedChangeListener((button, isChecked) -> {
            chordProFormat.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            openSongFormat.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
            dealWithEditMode(false);
        });
        if (preferences.getMyPreferenceBoolean(requireContext(), "editAsChordPro", false)) {
            chordProFormat.performClick();
        } else {
            openSongFormat.performClick();
        }

        title.addTextChangedListener(new MyTextWatcher("title"));
        artist.addTextChangedListener(new MyTextWatcher("artist"));
        lyrics.addTextChangedListener(new MyTextWatcher("lyrics"));
        key.addTextChangedListener(new MyTextWatcher("key"));
        folder.addTextChangedListener(new MyTextWatcher("folder"));

        transpose.setOnClickListener(v -> {
            Log.d("Editsong", "key="+key);
            TransposeDialogFragment dialogFragment = new TransposeDialogFragment(true,
                    editContent.getKey(),editContent.getLyrics());
            dialogFragment.show(requireActivity().getSupportFragmentManager(), "transposeAction");
        });
        autoFix.setOnClickListener(v -> doAutoFix());
    }

    private void doAutoFix(){
        //TODO
        Log.d("d","doAutoFix() called");
    }

    public void updateKeyAndLyrics(String newkey, String newlyrics) {
        // This is called if we ran a transpose while editing
        editContent.setKey(newkey);
        editContent.setLyrics(newlyrics);
        key.setText(keys.get(keys.indexOf(newkey)));
        lyrics.setText(newlyrics);
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
                if (what.equals("title")) {
                    value = storageAccess.makeFilenameSafe(value);
                }
                saveVal();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            saveButtonAccent(editContent.areThereChanges());
        }

        public void saveVal() {
            switch (what) {
                case "folder":
                    editContent.setFolder(value);
                    break;
                case "title":
                    editContent.setTitle(value);
                    break;
                case "key":
                    editContent.setKey(value);
                    break;
                case "artist":
                    editContent.setLyrics(value);
                    break;
                case "lyrics":
                    editContent.setArtist(value);
                    break;
            }
        }
    }

    private void saveButtonAccent(boolean newchanges) {
        Log.d("d","aretherechanges()="+newchanges+"  changes="+changes);
        // If changes are made, tint the button green and pulse gently
        if (newchanges && newchanges!=changes) {
            // Only do this for new changes - save sending a callback repeatedly
            mainActivityInterface.editSongSaveButtonAnimation(true);
            changes = true;
        } else if (!newchanges && newchanges!=changes){
            mainActivityInterface.editSongSaveButtonAnimation(false);
            changes = false;
        }
    }
}
