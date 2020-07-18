package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.MenuSongsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SongMenuFragment extends Fragment implements SongListAdapter.AdapterCallback {

    // The helper classes used
    private Preferences preferences;
    private SQLiteHelper sqLiteHelper;
    private MenuSongsBinding menuSongsBinding;
    private ShowCase showCase;
    private SongForSet songForSet;
    private boolean songButtonActive = true;
    private FloatingActionButton actionButton;
    private ProgressBar progressBar;
    private LinearLayout sideIndex;
    private Button folderButton, artistButton, keyButton, tagButton, filterButton;
    private TextInputLayout folderRow, artistRow, keyRow, tagRow, filterRow;
    private EditText artistSearch, tagSearch, filterSearch;
    private AutoCompleteTextView folderSearch, keySearch;
    private String folderSearchVal="", artistSearchVal="", keySearchVal="", tagSearchVal="", filterSearchVal="";
    private boolean songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
            songListSearchByTag, songListSearchByFilter;
    private FastScrollRecyclerView songListRecyclerView;
    private ArrayList<SQLite> songsFound;
    private ExposedDropDownArrayAdapter folderArrayAdapter, keyArrayAdapter;
    private SongListAdapter songListAdapter;
    private LinearLayoutManager songListLayoutManager;
    private ArrayList<String> foundFolders;

    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        menuSongsBinding = MenuSongsBinding.inflate(inflater, container, false);

        // Initialise views
        initialiseViews();

        // Initialise helpers
        initialiseHelpers();

        // Update the song menu
        updateSongMenu();

        return menuSongsBinding.getRoot();
    }

    private void initialiseViews() {
        actionButton = menuSongsBinding.actionFAB;
        progressBar = menuSongsBinding.progressBar;
        folderButton = menuSongsBinding.folderButton;
        artistButton = menuSongsBinding.artistButton;
        keyButton = menuSongsBinding.keyButton;
        tagButton = menuSongsBinding.tagButton;
        filterButton = menuSongsBinding.filterButton;
        folderRow = menuSongsBinding.filters.folderRow;
        artistRow = menuSongsBinding.filters.artistRow;
        keyRow = menuSongsBinding.filters.keyRow;
        tagRow = menuSongsBinding.filters.tagRow;
        filterRow = menuSongsBinding.filters.filterRow;
        folderSearch = menuSongsBinding.filters.folderSearch;
        artistSearch = menuSongsBinding.filters.artistSearch;
        keySearch = menuSongsBinding.filters.keySearch;
        tagSearch = menuSongsBinding.filters.tagSearch;
        filterSearch = menuSongsBinding.filters.filterSearch;
        songListRecyclerView = menuSongsBinding.songListRecyclerView;
        sideIndex = menuSongsBinding.songmenualpha.sideIndex;
        // initialise the recycler view with a blank until we have prepared the data asynchronously
        initialiseRecyclerView();
    }

    private void initialiseRecyclerView() {
        songListRecyclerView.removeAllViews();
        sideIndex.removeAllViews();
        songListLayoutManager = new LinearLayoutManager(getActivity());
        songListLayoutManager.setOrientation(RecyclerView.VERTICAL);
        songListRecyclerView.setLayoutManager(songListLayoutManager);
        songListRecyclerView.setHasFixedSize(true);
        songListRecyclerView.setOnClickListener(null);
        List<SQLite> blank = new ArrayList<>();
        songListAdapter = new SongListAdapter(requireActivity(), blank, preferences, songForSet,
                SongMenuFragment.this);
        songListRecyclerView.setAdapter(songListAdapter);
    }

    private void initialiseHelpers() {
        preferences = new Preferences();
        sqLiteHelper = new SQLiteHelper(getActivity());
        showCase = new ShowCase();
        songForSet = new SongForSet();
    }

    private void setValues() {
        songListSearchByFolder = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByFolder", false);
        if (songListSearchByFolder && folderSearchVal.isEmpty()) {
            // Likely the first run
            folderSearchVal = StaticVariables.whichSongFolder;
            folderSearch.setText(folderSearchVal);
        }
        songListSearchByArtist = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByArtist", false);
        songListSearchByKey = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByKey", false);
        songListSearchByTag = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByTag", false);
        songListSearchByFilter = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByFilter", false);
        showHideRows(folderRow,songListSearchByFolder);
        showHideRows(artistRow,songListSearchByArtist);
        showHideRows(keyRow,songListSearchByKey);
        showHideRows(tagRow,songListSearchByTag);
        showHideRows(filterRow,songListSearchByFilter);
    }

    private void setUpExposedDropDowns() {
        new Thread(() -> {
            foundFolders = sqLiteHelper.getFolders(getActivity());
            folderArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, foundFolders);
            keyArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, getResources().getStringArray(R.array.key_choice));
            getActivity().runOnUiThread(() -> {
                folderSearch.setAdapter(folderArrayAdapter);
                folderSearchVal = StaticVariables.whichSongFolder;
                folderSearch.addTextChangedListener(new MyTextWatcher("folder"));
                if (folderSearch!=null) {
                    int pos = foundFolders.indexOf(StaticVariables.whichSongFolder);
                    if (pos>=0) {
                        folderSearch.setText(foundFolders.get(pos));
                    }
                }
                keySearch.setAdapter(keyArrayAdapter);
                keySearch.addTextChangedListener(new MyTextWatcher("key"));
                artistSearch.addTextChangedListener(new MyTextWatcher("artist"));
                tagSearch.addTextChangedListener(new MyTextWatcher("tag"));
                filterSearch.addTextChangedListener(new MyTextWatcher("filter"));
            });
        }).start();
    }

    public void showActionButton(boolean show) {
        if (show) {
            menuSongsBinding.actionFAB.show();
        } else {
            menuSongsBinding.actionFAB.hide();
        }

    }

    private void fixButtons() {
        fixColor(folderButton, songListSearchByFolder);
        fixColor(artistButton, songListSearchByArtist);
        fixColor(keyButton, songListSearchByKey);
        fixColor(tagButton, songListSearchByTag);
        fixColor(filterButton, songListSearchByFilter);
        prepareSearch();
    }
    private void fixColor(Button button,boolean active) {
        int activecolor = getResources().getColor(R.color.colorSecondary);
        int inactivecolor = getResources().getColor(R.color.transparent);
        if (active) {
            button.setBackgroundColor(activecolor);
        } else {
            button.setBackgroundColor(inactivecolor);
        }
    }

    private void setListeners() {
        actionButton.setOnClickListener(v  -> {
            Log.d("d","actionButton pressed.  active="+songButtonActive);
            if (songButtonActive) {
                songButtonActive = false;
                Handler h = new Handler();
                h.postDelayed(() -> songButtonActive = true,600);
                showActionButton(false);
                showActionDialog();
            }
        });
        songListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    actionButton.show();
                } else {
                    actionButton.hide();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        folderButton.setOnClickListener(v -> {
            songListSearchByFolder = !songListSearchByFolder;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByFolder",songListSearchByFolder);
            fixButtons();
            showHideRows(folderRow,songListSearchByFolder);
            if (songListSearchByFolder) {
                runShowCaseSequence(folderButton, folderRow, getString(R.string.filter_by_folder),
                        getString(R.string.filter_by_dropdown), true,true,"folderSearch");
            }
        });
        artistButton.setOnClickListener(v -> {
            songListSearchByArtist = !songListSearchByArtist;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByArtist",songListSearchByArtist);
            fixButtons();
            showHideRows(artistRow,songListSearchByArtist);
            if (songListSearchByArtist) {
                runShowCaseSequence(artistButton, artistRow, getString(R.string.filter_by_artist),
                        getString(R.string.filter_by_edit), true, true,"artistSearch");
            }
        });
        keyButton.setOnClickListener(v -> {
            songListSearchByKey = !songListSearchByKey;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByKey",songListSearchByKey);
            fixButtons();
            showHideRows(keyRow,songListSearchByKey);
            if (songListSearchByKey) {
                runShowCaseSequence(keyButton, keyRow, getString(R.string.filter_by_key),
                        getString(R.string.filter_by_dropdown), true,true,"keySearch");
            }
        });
        tagButton.setOnClickListener(v -> {
            songListSearchByTag = !songListSearchByTag;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByTag",songListSearchByTag);
            fixButtons();
            showHideRows(tagRow,songListSearchByTag);
            if (songListSearchByTag) {
                runShowCaseSequence(tagButton, tagRow, getString(R.string.filter_by_tag),
                        getString(R.string.filter_by_edit), true,true,"tagSearch");
            }
        });
        filterButton.setOnClickListener(v -> {
            songListSearchByFilter = !songListSearchByFilter;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByFilter",songListSearchByFilter);
            fixButtons();
            showHideRows(filterRow,songListSearchByFilter);
            if (songListSearchByFilter) {
                runShowCaseSequence(filterButton, filterRow, getString(R.string.filter_by_this_value),
                        getString(R.string.filter_by_edit), true,true,"filterSearch");
            }
        });
    }

    private void showActionDialog() {
        SongMenuDialog dialogFragment = new SongMenuDialog();
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "songMenuActions");
    }

    private void showHideRows(TextInputLayout tr, boolean show) {
        if (show) {
            tr.setVisibility(View.VISIBLE);
        } else {
            tr.setVisibility(View.GONE);
        }
    }
    // Get the values from the spinners and edit texts for filtering
    private void getSearchVals() {
        folderSearchVal = getAutoComplete(folderSearch);
        keySearchVal    = getAutoComplete(keySearch);
    }

    private String getAutoComplete(AutoCompleteTextView autoCompleteTextView) {
        if (autoCompleteTextView!=null && autoCompleteTextView.getText()!=null) {
            return autoCompleteTextView.getText().toString();
        } else {
            return "";
        }
    }

    private void buttonsEnabled(boolean enabled) {
        folderButton.setEnabled(enabled);
        artistButton.setEnabled(enabled);
        keyButton.setEnabled(enabled);
        tagButton.setEnabled(enabled);
        filterButton.setEnabled(enabled);
        actionButton.setEnabled(enabled);
    }

    private void prepareSearch() {
        getSearchVals();
        new Thread(() -> {
            requireActivity().runOnUiThread(() -> buttonsEnabled(false));
            try {
                songsFound = sqLiteHelper.getSongsByFilters(getActivity(), songListSearchByFolder,
                        songListSearchByArtist, songListSearchByKey, songListSearchByTag,
                        songListSearchByFilter, folderSearchVal, artistSearchVal, keySearchVal,
                        tagSearchVal, filterSearchVal);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("SongMenu","No songs found.  Could just be that storage isn't set properly yet");
            }
            requireActivity().runOnUiThread(this::updateSongList);
        }).start();
    }

    private void updateSongList() {
        songListRecyclerView.removeAllViews();
        sideIndex.removeAllViews();
        songListRecyclerView.setOnClickListener(null);
        songListAdapter = new SongListAdapter(requireActivity(), songsFound, preferences,
                songForSet, SongMenuFragment.this);
        songListAdapter.notifyDataSetChanged();
        songListRecyclerView.setAdapter(songListAdapter);
        songListRecyclerView.setFastScrollEnabled(true);
        displayIndex(songsFound, songListAdapter);
        progressBar.setVisibility(View.GONE);
        buttonsEnabled(true);
    }
    private void displayIndex(ArrayList<SQLite> songMenuViewItems,
                              SongListAdapter songListAdapter) {
        if (preferences.getMyPreferenceBoolean(getActivity(),"songMenuAlphaIndexShow",true)) {
            sideIndex.setVisibility(View.VISIBLE);
        } else {
            sideIndex.setVisibility(View.GONE);
        }
        sideIndex.removeAllViews();
        TextView textView;
        final Map<String,Integer> map = songListAdapter.getAlphaIndex(songMenuViewItems);
        Set<String> setString = map.keySet();
        List<String> indexList = new ArrayList<>(setString);
        for (String index : indexList) {
            textView = (TextView) View.inflate(getActivity(), R.layout.leftmenu, null);
            textView.setTextSize(preferences.getMyPreferenceFloat(getActivity(),"songMenuAlphaIndexSize",14.0f));
            int i = (int) preferences.getMyPreferenceFloat(getActivity(),"songMenuAlphaIndexSize",14.0f) *2;
            textView.setPadding(i,i,i,i);
            textView.setText(index);
            textView.setOnClickListener(view -> {
                TextView selectedIndex = (TextView) view;
                try {
                    if (selectedIndex.getText() != null && getActivity()!=null &&
                            songListRecyclerView.getLayoutManager()!=null) {
                        String myval = selectedIndex.getText().toString();
                        Integer obj = map.get(myval);
                        Log.d("SongMenuFragment","obj="+obj);
                        //noinspection ConstantConditions
                        songListLayoutManager.scrollToPositionWithOffset(obj,0);
                        mainActivityInterface.hideKeyboard();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            sideIndex.addView(textView);
        }
    }

    @Override
    public void onItemClicked(int position) {
        mainActivityInterface.hideKeyboard();
        mainActivityInterface.doSongLoad();
    }

    @Override
    public void onItemLongClicked(int position) {
        mainActivityInterface.hideKeyboard();
        actionButton.performClick();
    }

    public void runShowCaseSequence(View view1, View view2, String info1, String info2,
                                    boolean rect1, boolean rect2, String id) {
        ArrayList<View> targets = new ArrayList<>();
        ArrayList<String> infos = new ArrayList<>();
        ArrayList<String> dismisses = new ArrayList<>();
        ArrayList<Boolean> rects = new ArrayList<>();
        targets.add(view1);
        targets.add(view2);
        infos.add(info1);
        infos.add(info2);
        dismisses.add(null);
        dismisses.add(null);
        rects.add(rect1);
        rects.add(rect2);
        showCase.sequenceShowCase(requireActivity(),targets,dismisses,infos,rects,id);
    }
    public void updateSongMenu() {
        // Set values
        setValues();

        // Set up the spinners
        setUpExposedDropDowns();

        // Set up page buttons
        setListeners();

        // Prepare the song menu
        fixButtons();

        // Prepare the menu
        prepareSearch();
    }

    public void moveToSongInMenu() {
        //scroll to the song in the song menu
        int index = indexOfSongInMenu();
        Log.d("d","index="+index);
        try {
            if (index>=0) {
                new Thread(() -> requireActivity().runOnUiThread(() -> songListLayoutManager.scrollToPositionWithOffset(index,0))).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int indexOfSongInMenu() {
        String searchVal = songForSet.getSongForSet(getActivity(),StaticVariables.whichSongFolder,
                StaticVariables.songfilename);

        int position = StaticVariables.songsInList.indexOf(searchVal);
        if (position<=0) {
            StaticVariables.currentSongIndex = 0;
            StaticVariables.previousSongIndex = 0;
        } else {
            StaticVariables.currentSongIndex = position;
            StaticVariables.previousSongIndex = position-1;
        }
        if (position<StaticVariables.songsInList.size()-1) {
            StaticVariables.nextSongIndex = position+1;
        } else {
            StaticVariables.nextSongIndex = position;
        }
        return position;
    }

    private class MyTextWatcher implements TextWatcher {

        String what;
        String value = "";

        MyTextWatcher(String what) {
            this.what = what;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s!=null) {
                value = s.toString();
                saveVal();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}

        public void saveVal() {
            switch(what) {
                case "folder":
                    folderSearchVal = value;
                    break;
                case "artist":
                    artistSearchVal = value;
                    break;
                case "key":
                    keySearchVal = value;
                    break;
                case "tag":
                    tagSearchVal = value;
                    break;
                case "filter":
                    filterSearchVal = value;
                    break;
            }
            prepareSearch();
        }
    }
}
