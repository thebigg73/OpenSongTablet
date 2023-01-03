package com.garethevans.church.opensongtablet.tags;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsTagManageBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

import java.util.ArrayList;
import java.util.Collections;

public class BulkTagAssignFragment extends Fragment {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "BulkTagAssign";
    private MainActivityInterface mainActivityInterface;
    private SettingsTagManageBinding myView;
    private String folderSearchVal = "", artistSearchVal = "", keySearchVal = "", tagSearchVal = "",
            filterSearchVal = "", titleSearchVal = "", thisTag = "";
    private boolean songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
            songListSearchByTag, songListSearchByFilter, songListSearchByTitle, showForThisTag;
    private TagSongListAdapter tagSongListAdapter;
    private ArrayList<String> newValues;
    private int activecolor, inactivecolor;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsTagManageBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.tag_song));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_tags));

        setupViews();

        setupListeners();

        // Do the showcase for info
        setupShowcase();

        return myView.getRoot();
    }

    private void setupViews() {
        // Initialise the recyclerview
        initialiseRecyclerView();

        // Hide the filter rows
        fixFilterRows();

        // Populate the dropdowns
        setupDropdowns();
        setupTagsToAddRemove();

        // Set the filterButton colours
        activecolor = getResources().getColor(R.color.colorSecondary);
        inactivecolor = getResources().getColor(R.color.transparent);
        // This bit also prepares the songList
        fixButtons();
    }

    private void setupListeners() {
        myView.filterButtons.folderButton.setOnClickListener(v -> {
            songListSearchByFolder = !songListSearchByFolder;
            showHideRows(myView.filters.folderLayout,songListSearchByFolder);
            fixColor(myView.filterButtons.folderButton,songListSearchByFolder);
        });
        myView.filterButtons.titleButton.setOnClickListener(v -> {
            songListSearchByTitle = !songListSearchByTitle;
            showHideRows(myView.filters.titleSearch,songListSearchByTitle);
            fixColor(myView.filterButtons.titleButton,songListSearchByTitle);
        });
        myView.filterButtons.tagButton.setOnClickListener(v -> {
            songListSearchByTag = !songListSearchByTag;
            showHideRows(myView.filters.tagLayout,songListSearchByTag);
            fixColor(myView.filterButtons.tagButton,songListSearchByTag);
        });
        myView.filterButtons.keyButton.setOnClickListener(v -> {
            songListSearchByKey = !songListSearchByKey;
            showHideRows(myView.filters.keySearch,songListSearchByKey);
            fixColor(myView.filterButtons.keyButton,songListSearchByKey);
        });
        myView.filterButtons.artistButton.setOnClickListener(v -> {
            songListSearchByArtist = !songListSearchByArtist;
            showHideRows(myView.filters.artistSearch,songListSearchByArtist);
            fixColor(myView.filterButtons.artistButton,songListSearchByArtist);
        });
        myView.filterButtons.filterButton.setOnClickListener(v -> {
            songListSearchByFilter = !songListSearchByFilter;
            showHideRows(myView.filters.filterSearch,songListSearchByFilter);
            fixColor(myView.filterButtons.filterButton,songListSearchByFilter);
        });
        myView.filters.titleSearch.addTextChangedListener(new MyTextWatcher("title"));
        myView.filters.filterSearch.addTextChangedListener(new MyTextWatcher("filter"));
        myView.filters.artistSearch.addTextChangedListener(new MyTextWatcher("artist"));
        myView.filters.keySearch.addTextChangedListener(new MyTextWatcher("key"));
        myView.filters.folderSearch.addTextChangedListener(new MyTextWatcher("folder"));
        myView.filters.tagSearch.addTextChangedListener(new MyTextWatcher("tag"));
        myView.thisTag.addTextChangedListener(new MyTextWatcher("currentTag"));
        myView.searchThisTag.setOnClickListener(v -> {
            showForThisTag = !showForThisTag;
            if (showForThisTag) {
                myView.searchThisTag.setImageResource(R.drawable.filter_off);
                songListSearchByTag = true;
                showHideRows(myView.filters.tagSearch, true);
                fixColor(myView.filterButtons.tagButton, true);
                myView.filters.tagSearch.setText(myView.thisTag.getText().toString());
            } else {
                myView.searchThisTag.setImageResource(R.drawable.filter_on);
                songListSearchByTag = false;
                showHideRows(myView.filters.tagSearch,false);
                fixColor(myView.filterButtons.tagButton, false);
                myView.filters.tagSearch.setText("");
            }
        });
        myView.addNewTag.setOnClickListener(v -> {
            TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(this,
                    "BulkTagAssignFragment", getString(R.string.new_category),getString(R.string.tag),
                    null,null,null,true);
            textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"textInputBottomSheet");
        });
    }

    private void initialiseRecyclerView() {
        myView.songList.removeAllViews();
        LinearLayoutManager songListLayoutManager = new LinearLayoutManager(requireContext());
        songListLayoutManager.setOrientation(RecyclerView.VERTICAL);
        myView.songList.setLayoutManager(songListLayoutManager);
        myView.songList.setHasFixedSize(false);
        myView.songList.setOnClickListener(null);
        tagSongListAdapter = new TagSongListAdapter(requireContext(),myView.songList);
        myView.songList.setAdapter(tagSongListAdapter);
    }

    private void fixFilterRows() {
        // Hide the edit folder/tag buttons as not required here
        myView.filters.manageTags.setVisibility(View.GONE);
        myView.filters.manageFolders.setVisibility(View.GONE);
        showHideRows(myView.filters.folderLayout, songListSearchByFolder);
        showHideRows(myView.filters.artistSearch, songListSearchByArtist);
        showHideRows(myView.filters.keySearch, songListSearchByKey);
        showHideRows(myView.filters.tagLayout, songListSearchByTag);
        showHideRows(myView.filters.filterSearch, songListSearchByFilter);
        showHideRows(myView.filters.titleSearch, songListSearchByTitle);
    }

    private void showHideRows(View view, boolean show) {
        if (show) {
            view.post(() -> view.setVisibility(View.VISIBLE));
        } else {
            view.post(() -> view.setVisibility(View.GONE));
        }
    }

    private void fixButtons() {
        fixColor(myView.filterButtons.titleButton,false);
        fixColor(myView.filterButtons.filterButton,false);
        fixColor(myView.filterButtons.keyButton,false);
        fixColor(myView.filterButtons.artistButton,false);
        fixColor(myView.filterButtons.folderButton,false);
        fixColor(myView.filterButtons.tagButton,false);
        prepareResults();
    }

    private void fixColor(Button button, boolean active) {
        try {
            if (active) {
                button.setBackgroundColor(activecolor);
            } else {
                button.setBackgroundColor(inactivecolor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDropdowns() {
        ExposedDropDownArrayAdapter keyArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),myView.filters.keySearch,R.layout.view_exposed_dropdown_item, getResources().getStringArray(R.array.key_choice));
        ExposedDropDownArrayAdapter folderArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),myView.filters.folderSearch,R.layout.view_exposed_dropdown_item, mainActivityInterface.getSQLiteHelper().getFolders());
        myView.filters.keySearch.setAdapter(keyArrayAdapter);
        myView.filters.folderSearch.setAdapter(folderArrayAdapter);
    }

    private void setupTagsToAddRemove() {
        ArrayList<String> values = mainActivityInterface.getSQLiteHelper().getThemeTags();
        if (newValues==null) {
            newValues = new ArrayList<>();
        }
        for (String newValue:newValues) {
            if (!values.contains(newValue)) {
                values.add(newValue);
            }
        }
        Collections.sort(values);

        ExposedDropDownArrayAdapter tagArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.thisTag, R.layout.view_exposed_dropdown_item, values);
        myView.thisTag.setAdapter(tagArrayAdapter);
        myView.thisTag.setText(thisTag);
    }

    private void setupShowcase() {
        ArrayList<View> targets = new ArrayList<>();
        targets.add(myView.filterButtons.getRoot());
        targets.add(myView.thisTag);
        targets.add(myView.addNewTag);
        targets.add(myView.searchThisTag);

        ArrayList<String> infos = new ArrayList<>();
        infos.add(getString(R.string.filter_songs));
        infos.add(getString(R.string.tag_to_use));
        infos.add(getString(R.string.tag_new));
        infos.add(getString(R.string.tag_search));


        mainActivityInterface.getShowCase().sequenceShowCase(requireActivity(),targets,null,
                infos,null,"bulkTagAssign");
    }
    private void prepareResults() {
        tagSongListAdapter.updateSongsFound(myView.thisTag.getText().toString(),
                songListSearchByFolder, songListSearchByArtist,
                songListSearchByKey, songListSearchByTag, songListSearchByFilter,
                songListSearchByTitle, folderSearchVal, artistSearchVal, keySearchVal,
                tagSearchVal, filterSearchVal, titleSearchVal);
    }

    private class MyTextWatcher implements TextWatcher {

        private final String which;
        private MyTextWatcher(String which) {
            this.which = which;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String string = s.toString();
            switch (which) {
                case "title":
                    titleSearchVal = string;
                    break;
                case "filter":
                    filterSearchVal = string;
                    break;
                case "artist":
                    artistSearchVal = string;
                    break;
                case "key":
                    keySearchVal = string;
                    break;
                case "folder":
                    folderSearchVal = string;
                    break;
                case "tag":
                    tagSearchVal = string;
                    break;
                case "currentTag":
                    thisTag = string;
                    if (showForThisTag) {
                        myView.filters.tagSearch.setText(string);
                        tagSearchVal = string;
                    }
                    break;
            }
            prepareResults();
        }
    }

    // Returned from bottomsheet via MainActivity
    public void addNewTag(String newTag) {
        if (newValues==null) {
            newValues = new ArrayList<>();
        }
        thisTag = newTag;
        newValues.add(newTag);
        setupTagsToAddRemove();
    }
}
