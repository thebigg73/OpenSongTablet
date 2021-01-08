package com.garethevans.church.opensongtablet.songprocessing;

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
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.EditSongMainFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditSongFragmentMain extends Fragment implements EditSongMainFragmentInterface  {

    // The helper classes used
    private Preferences preferences;
    private StorageAccess storageAccess;
    private SQLiteHelper sqLiteHelper;
    private CommonSQL commonSQL;
    private ConvertChoPro convertChoPro;
    private ProcessSong processSong;

    // The variable used in this fragment
    private FragmentEditSong1Binding myView;
    MaterialButton openSongFormat, chordProFormat, transpose, autoFix;
    MaterialButtonToggleGroup formatButton;
    TextInputEditText title, filename, artist, lyrics;
    AutoCompleteTextView folder, key;
    int activeColor, inactiveColor;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> foundFolders;
    private ExposedDropDownArrayAdapter folderArrayAdapter, keyArrayAdapter;
    private List<String> keys;
    boolean changes, editAsChoPro;
    Song song, originalSong;

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

        // Get the song
        song = mainActivityInterface.getSong();
        originalSong = mainActivityInterface.getOriginalSong();

        editAsChoPro = preferences.getMyPreferenceBoolean(requireContext(),"editAsChordPro",false);

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

    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        preferences = mainActivityInterface.getPreferences();
        storageAccess = mainActivityInterface.getStorageAccess();
        sqLiteHelper = mainActivityInterface.getSQLiteHelper();
        commonSQL = mainActivityInterface.getCommonSQL();
        convertChoPro = mainActivityInterface.getConvertChoPro();
        processSong = mainActivityInterface.getProcessSong();
    }

    // Initialise the views
    private void initialiseViews() {
        filename = myView.fileName;
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
            foundFolders = sqLiteHelper.getFolders(getActivity(),commonSQL);
            folderArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, foundFolders);
            keyArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, getResources().getStringArray(R.array.key_choice));
            getActivity().runOnUiThread(() -> {
                folder.setAdapter(folderArrayAdapter);
                if (song.getFolder() != null) {
                    int pos = foundFolders.indexOf(song.getFolder());
                    if (pos >= 0) {
                        folder.setText(foundFolders.get(pos));
                    }
                }
                key.setAdapter(keyArrayAdapter);
                keys = Arrays.asList(getResources().getStringArray(R.array.key_choice));
                if (song.getKey() != null) {
                    int pos = keys.indexOf(song.getKey());
                    if (pos >= 0) {
                        key.setText(keys.get(pos));
                    }
                }
            });
        }).start();
    }

    // Set the current loaded values
    private void setCurrentValues() {
        // Exposed drop downs are set separately (once values are loaded up)
        filename.setText(song.getFilename());
        title.setText(song.getTitle());
        artist.setText(song.getAuthor());
        lyrics.setText(song.getLyrics());
        if (editAsChoPro) {
            // Do the conversion
            // Initially set this to false so it triggers
            editAsChoPro = false;
            dealWithEditMode(true);
            setButtonOn(chordProFormat,true);
            setButtonOn(openSongFormat,false);
        } else {
            setButtonOn(chordProFormat,false);
            setButtonOn(openSongFormat,true);
        }
    }

    // Deal with the edit mode
    private void dealWithEditMode(boolean choProFormatting) {
        String text = null;
        if (lyrics.getText()!=null && lyrics.getText().toString()!=null) {
            text = lyrics.getText().toString();
        }
        if (choProFormatting) {
            // We can switch to ChoPro format
            setButtonOn(chordProFormat,true);
            setButtonOn(openSongFormat,false);
            // Only process if we are actually changing
            if (!editAsChoPro && text!=null && !text.isEmpty()) {
                song.setLyrics(convertChoPro.fromOpenSongToChordPro(requireContext(), processSong, text));
                lyrics.setText(song.getLyrics());
            }
            preferences.setMyPreferenceBoolean(requireContext(), "editAsChordPro", true);
            editAsChoPro = true;

        } else if (!choProFormatting && editAsChoPro && text!=null && !text.isEmpty()) {
            // We can switch to OpenSong format
            setButtonOn(chordProFormat,false);
            setButtonOn(openSongFormat,true);

            // Only process if we are actually changing
            if (editAsChoPro && text!=null && !text.isEmpty()) {
                song.setLyrics(convertChoPro.fromChordProToOpenSong(text));
                lyrics.setText(song.getLyrics());
            }
            preferences.setMyPreferenceBoolean(requireContext(), "editAsChordPro", false);
            editAsChoPro = false;
        }
        mainActivityInterface.updateSong(song);
    }

    private void setButtonOn(MaterialButton button, boolean on) {
        if (on) {
            button.setBackgroundTintList(ColorStateList.valueOf(activeColor));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
        }
    }
    // Finished with this view
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }



    // Sor the view visibility, listeners, etc.
    private void setUpListeners() {
        openSongFormat.setOnClickListener(v -> dealWithEditMode(false));
        chordProFormat.setOnClickListener(v -> dealWithEditMode(true));
        filename.addTextChangedListener(new MyTextWatcher("filename"));
        title.addTextChangedListener(new MyTextWatcher("title"));
        artist.addTextChangedListener(new MyTextWatcher("artist"));
        lyrics.addTextChangedListener(new MyTextWatcher("lyrics"));
        key.addTextChangedListener(new MyTextWatcher("key"));
        folder.addTextChangedListener(new MyTextWatcher("folder"));

        transpose.setOnClickListener(v -> {
            Log.d("Editsong", "key="+key);
            TransposeDialogFragment dialogFragment = new TransposeDialogFragment(true,song);
            dialogFragment.show(requireActivity().getSupportFragmentManager(), "transposeAction");
        });
        autoFix.setOnClickListener(v -> doAutoFix());
    }

    private void doAutoFix(){
        //TODO
        Log.d("d","doAutoFix() called");
    }

    public void updateKeyAndLyrics(Song song) {
        // This is called if we ran a transpose while editing
        this.song = song;
        mainActivityInterface.updateSong(song);
        key.setText(keys.get(keys.indexOf(song.getKey())));
        lyrics.setText(song.getLyrics());
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
                if (what.equals("filename")) {
                    value = storageAccess.safeFilename(value);
                }
                saveVal();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            saveButtonAccent(mainActivityInterface.songChanged());
        }

        public void saveVal() {
            switch (what) {
                case "folder":
                    song.setFolder(value);
                    break;
                case "filename":
                    song.setFilename(value);
                    break;
                case "title":
                    song.setTitle(value);
                    break;
                case "key":
                    song.setKey(value);
                    break;
                case "artist":
                    song.setLyrics(value);
                    break;
                case "lyrics":
                    song.setAuthor(value);
                    break;
            }
            mainActivityInterface.updateSong(song);
        }
    }

    private void saveButtonAccent(boolean newchanges) {
        // Only do this for new changes - save sending a callback repeatedly
        if (!changes && newchanges) {
            // If changes are made, tint the button green and pulse gently
            Log.d("EditSongFragmentMain","Changes made");
            mainActivityInterface.editSongSaveButtonAnimation(true);
            changes = true;
        } else if (!newchanges) {
            Log.d("EditSongFragmentMain","No changes made");
            mainActivityInterface.editSongSaveButtonAnimation(false);
            changes = false;
        }
    }
}
