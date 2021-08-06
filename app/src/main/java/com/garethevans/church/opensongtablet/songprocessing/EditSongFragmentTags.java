package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.EditSongTagsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class EditSongFragmentTags extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private EditSongTagsBinding myView;
    private ThemesBottomSheet themesBottomSheet;
    private final String TAG = "EditSongFragmentTags";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = EditSongTagsBinding.inflate(inflater, container, false);

        // Set up the current values
        setupValues();

        // Set up the listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupValues() {
        themesBottomSheet = new ThemesBottomSheet(this,"EditSongFragmentTags");
        myView.tags.setFocusable(false);
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.tags);
        myView.tags.setText(themesSplitByLine());
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.tags,2);
        myView.aka.setText(mainActivityInterface.getTempSong().getAka());
        myView.ccli.setText(mainActivityInterface.getTempSong().getCcli());
        myView.user1.setText(mainActivityInterface.getTempSong().getUser1());
        myView.user2.setText(mainActivityInterface.getTempSong().getUser2());
        myView.user3.setText(mainActivityInterface.getTempSong().getUser3());
        myView.hymnnum.setText(mainActivityInterface.getTempSong().getHymnnum());
        myView.presorder.setText(mainActivityInterface.getTempSong().getPresentationorder());
    }

    private void setupListeners() {
        //myView.tags.setOnClickListener(v -> themesBottomSheet.show(requireActivity().getSupportFragmentManager(),"ThemesBottomSheet"));
        myView.tags.setOnClickListener(v -> {
            themesBottomSheet.show(requireActivity().getSupportFragmentManager(),"ThemesBottomSheet");
        });
        myView.tags.addTextChangedListener(new MyTextWatcher("tags"));
        myView.ccli.addTextChangedListener(new MyTextWatcher("ccli"));
        myView.user1.addTextChangedListener(new MyTextWatcher("user1"));
        myView.user2.addTextChangedListener(new MyTextWatcher("user2"));
        myView.user3.addTextChangedListener(new MyTextWatcher("user3"));
        myView.hymnnum.addTextChangedListener(new MyTextWatcher("hymnnum"));

    }
    private class MyTextWatcher implements TextWatcher {

        String what;
        MyTextWatcher(String what) {
            this.what = what;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mainActivityInterface.showSaveAllowed(mainActivityInterface.songChanged());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (what) {
                case "tags":
                    mainActivityInterface.getTempSong().setTheme(getThemesFromLines(editable.toString()));
                    mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.tags,2);
                    break;
                case "aka":
                    mainActivityInterface.getTempSong().setAka(editable.toString());
                case "user1":
                    mainActivityInterface.getTempSong().setUser1(editable.toString());
                    break;
                case "user2":
                    mainActivityInterface.getTempSong().setUser2(editable.toString());
                    break;
                case "user3":
                    mainActivityInterface.getTempSong().setUser3(editable.toString());
                    break;
                case "ccli":
                    mainActivityInterface.getTempSong().setCcli(editable.toString());
                    break;
                case "presorder":
                    mainActivityInterface.getTempSong().setPresentationorder(editable.toString());
                    break;
            }
        }
    }

    private String themesSplitByLine() {
        String themes = mainActivityInterface.getTempSong().getTheme();
        themes = themes.replace("; ", "\n");
        themes = themes.replace(";", "\n");
        return themes;
    }

    private String getThemesFromLines(String lines) {
        String[] newLines = lines.split("\n");
        StringBuilder newTheme = new StringBuilder();
        for (String newLine:newLines) {
            newLine = newLine.trim();
            if (!newLine.isEmpty()) {
                newTheme.append(newLine).append("; ");
            }
        }
        String fixedTheme = newTheme.toString();
        if (fixedTheme.endsWith("; ")) {
            fixedTheme = fixedTheme.substring(0,fixedTheme.lastIndexOf("; "));
        }
        return fixedTheme;
    }

    public void updateTheme() {
        myView.tags.setText(themesSplitByLine());
        mainActivityInterface.showSaveAllowed(mainActivityInterface.songChanged());
    }

    public void removeTags(ArrayList<String> tag) {
        // The user confirmed that they want to purge a tag from the songs.
        // The arraylist will have two entries, the tag to purge and the adapter position
        String tagToRemove = "";
        int position = -1;
        if (tag!=null && tag.size() == 2) {
            tagToRemove = tag.get(0);
            position = Integer.parseInt(tag.get(1));
        }
        if (position >= 0 && !tagToRemove.isEmpty()) {
            ArrayList<Song> songs = mainActivityInterface.getSQLiteHelper().getSongsByFilters(
                    requireContext(), mainActivityInterface, false, false,
                    false, true, false, null, null,
                    null, tagToRemove, null);
            for (Song thisSong : songs) {
                thisSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(requireContext(),mainActivityInterface,
                        thisSong.getFolder(), thisSong.getFilename());
                Log.d(TAG,"songId: "+thisSong.getSongid()+"  tagToRemove: "+tagToRemove +
                        "  currTheme: "+thisSong.getTheme() + "  fileType: "+thisSong.getFiletype());

                // Update this song object
                thisSong.setTheme(removeTagFromTheme(thisSong.getTheme(),tagToRemove));
                thisSong.setAlttheme(removeTagFromTheme(thisSong.getAlttheme(),tagToRemove));

                // Update the non-persistent database
                mainActivityInterface.getSQLiteHelper().updateSong(requireContext(),
                        mainActivityInterface, thisSong);

                // Update the persistent database if it isn't an XML file, but PDF/IMG
                if (thisSong.getFiletype().equals("PDF") || thisSong.getFiletype().equals("IMG")) {
                    mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(requireContext(),
                            mainActivityInterface, thisSong);

                } else if (thisSong.getFiletype().equals("XML")) {
                    // Update the actual OpenSong file since it is XML
                    mainActivityInterface.getSaveSong().doSave(requireContext(), mainActivityInterface,
                            thisSong);
                }
            }
            // Update the array adapter in the bottom sheet fragment
            themesBottomSheet.deleteTags(position);
        }

        mainActivityInterface.getTempSong().
                setTheme(removeTagFromTheme(mainActivityInterface.getTempSong().getTheme(),tagToRemove));
        myView.tags.setText(themesSplitByLine());
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.tags,2);
        mainActivityInterface.showSaveAllowed(mainActivityInterface.songChanged());
    }

    private String removeTagFromTheme(String currTheme, String tagToRemove) {
        if (currTheme.contains(tagToRemove + "; ")) {
            currTheme = currTheme.replace(tagToRemove + "; ", "");
        } else if (currTheme.endsWith(tagToRemove)) {
            currTheme = currTheme.substring(0, currTheme.lastIndexOf(tagToRemove));
        } else if (currTheme.contains(tagToRemove + ";")) {
            currTheme = currTheme.replace(tagToRemove + ";", "");
        }
        if (currTheme.startsWith(";")) {
            currTheme = currTheme.substring(1);
        }
        if (currTheme.endsWith(";") || currTheme.endsWith("; ")) {
            currTheme = currTheme.substring(0,currTheme.lastIndexOf(";"));
        }
        Log.d(TAG,"fixedCurrTheme:"+currTheme);
        return currTheme;
    }
}