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
import com.garethevans.church.opensongtablet.databinding.EditSongTagsBinding;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.tags.TagsBottomSheet;

import java.util.ArrayList;

public class EditSongFragmentTags extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private EditSongFragmentInterface editSongFragmentInterface;
    private EditSongTagsBinding myView;
    private TagsBottomSheet tagsBottomSheet;
    private PresentationOrderBottomSheet presentationOrderBottomSheet;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        editSongFragmentInterface = (EditSongFragmentInterface) context;
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
        tagsBottomSheet = new TagsBottomSheet(this,"EditSongFragmentTags");
        presentationOrderBottomSheet = new PresentationOrderBottomSheet(this, "EditSongFragmentTags");
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
        myView.presorder.setFocusable(false);
        myView.presorder.setText(mainActivityInterface.getTempSong().getPresentationorder());

        // Resize the bottom padding to the soft keyboard height or half the screen height for the soft keyboard (workaround)
        mainActivityInterface.getWindowFlags().adjustViewPadding(mainActivityInterface,myView.resizeForKeyboardLayout);
    }

    private void setupListeners() {
        myView.tags.setOnClickListener(v -> {
            // Only allow if indexing is complete
            if (mainActivityInterface.getSongListBuildIndex().getIndexComplete()) {
                if (!tagsBottomSheet.isAdded()) {
                    tagsBottomSheet.show(requireActivity().getSupportFragmentManager(), "ThemesBottomSheet");
                }
            } else {
                mainActivityInterface.getShowToast().doIt(getString(R.string.search_index_wait));
            }

        });
        myView.presorder.setOnClickListener(v -> {
            if (!presentationOrderBottomSheet.isAdded()) {
                presentationOrderBottomSheet.show(requireActivity().getSupportFragmentManager(), "PresentationOrderBottomSheet");
            }
        });
        myView.tags.addTextChangedListener(new MyTextWatcher("tags"));
        myView.ccli.addTextChangedListener(new MyTextWatcher("ccli"));
        myView.user1.addTextChangedListener(new MyTextWatcher("user1"));
        myView.user2.addTextChangedListener(new MyTextWatcher("user2"));
        myView.user3.addTextChangedListener(new MyTextWatcher("user3"));
        myView.hymnnum.addTextChangedListener(new MyTextWatcher("hymnnum"));

        // Scroll listener
        myView.nestedScrollView.setExtendedFabToAnimate(editSongFragmentInterface.getSaveButton());
    }
    private class MyTextWatcher implements TextWatcher {

        String what;
        MyTextWatcher(String what) {
            this.what = what;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

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
        if (themes==null) {
            themes = "";
        }
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

    public void updateValue() {
        // Updating either the theme or presoorder from bottom sheet callback
        myView.tags.setText(themesSplitByLine());
        myView.presorder.setText(mainActivityInterface.getTempSong().getPresentationorder());
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
                    false, false,
                    false, true, false, false,
                    null, null,
                    null, tagToRemove, null, null);
            for (Song thisSong : songs) {
                thisSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(
                        thisSong.getFolder(), thisSong.getFilename());

                // Update this song object
                thisSong.setTheme(removeTagFromTheme(thisSong.getTheme(),tagToRemove));
                thisSong.setAlttheme(removeTagFromTheme(thisSong.getAlttheme(),tagToRemove));

                // Update the non-persistent database
                mainActivityInterface.getSQLiteHelper().updateSong(thisSong);

                // Update the persistent database if it isn't an XML file, but PDF/IMG
                if (thisSong.getFiletype().equals("PDF") || thisSong.getFiletype().equals("IMG")) {
                    mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(thisSong);

                } else if (thisSong.getFiletype().equals("XML")) {
                    // Update the actual OpenSong file since it is XML
                    mainActivityInterface.getSaveSong().updateSong(thisSong);
                }
            }
            // Update the array adapter in the bottom sheet fragment
            tagsBottomSheet.deleteTags(position);
        }

        mainActivityInterface.getTempSong().
                setTheme(removeTagFromTheme(mainActivityInterface.getTempSong().getTheme(),tagToRemove));
        myView.tags.setText(themesSplitByLine());
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.tags,2);
    }

    private String removeTagFromTheme(String currTheme, String tagToRemove) {
        // Have to be careful as removing the tag Love would wreck Mercy/Love
        // Each theme should be split by a ;
        if (currTheme.contains(";")) {
            currTheme = currTheme + ";";
        }

        String[] bits = currTheme.split(";");
        tagToRemove = tagToRemove.trim();

        // Now we have all the bits, trim them and look for matches
        // If there is a match, don't add it back.  If it doesn't match, add it
        StringBuilder stringBuilder = new StringBuilder();
        for (String bit:bits) {
            bit = bit.trim();
            if (!bit.equals(tagToRemove)) {
                stringBuilder.append(bit).append("; ");
            }
        }
        String newTheme = stringBuilder.toString().trim();
        if (newTheme.endsWith(";")) {
            // Remove final ;
            newTheme = newTheme.substring(0,newTheme.lastIndexOf(";"));
        }
        return newTheme;
    }
}