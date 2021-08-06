package com.garethevans.church.opensongtablet.songsandsetsmenu;

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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.MenuSongsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SongMenuFragment extends Fragment implements SongListAdapter.AdapterCallback {

    private final String TAG = "SongMenuFragment";
    // The helper classes used
    private MenuSongsBinding myView;
    private boolean songButtonActive = true;
    private String folderSearchVal = "", artistSearchVal = "", keySearchVal = "", tagSearchVal = "", filterSearchVal = "";
    private boolean songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
            songListSearchByTag, songListSearchByFilter;
    private ArrayList<Song> songsFound;
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
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = MenuSongsBinding.inflate(inflater, container, false);

        // Initialise views
        initialiseRecyclerView();

        // Update the song menu
        updateSongMenu(mainActivityInterface.getSong());

        return myView.getRoot();
    }

    private void initialiseRecyclerView() {
        myView.songListRecyclerView.removeAllViews();
        myView.songmenualpha.sideIndex.removeAllViews();
        songListLayoutManager = new LinearLayoutManager(getActivity());
        songListLayoutManager.setOrientation(RecyclerView.VERTICAL);
        myView.songListRecyclerView.setLayoutManager(songListLayoutManager);
        myView.songListRecyclerView.setHasFixedSize(true);
        myView.songListRecyclerView.setOnClickListener(null);
        List<Song> blank = new ArrayList<>();
        songListAdapter = new SongListAdapter(requireContext(), mainActivityInterface, blank,
                SongMenuFragment.this);
        myView.songListRecyclerView.setAdapter(songListAdapter);
        myView.songListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setValues(Song song) {
        songListSearchByFolder = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(), "songListSearchByFolder", false);
        if (songListSearchByFolder && folderSearchVal.isEmpty()) {
            // Likely the first run
            // Do on the UI thread - force a folder change when the song menu is opened
            myView.filters.folderSearch.post(() -> myView.filters.folderSearch.setText(song.getFolder()));
        }
        songListSearchByArtist = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(), "songListSearchByArtist", false);
        songListSearchByKey = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(), "songListSearchByKey", false);
        songListSearchByTag = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(), "songListSearchByTag", false);
        songListSearchByFilter = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(), "songListSearchByFilter", false);
        showHideRows(myView.filters.folderSearch, songListSearchByFolder);
        showHideRows(myView.filters.artistSearch, songListSearchByArtist);
        showHideRows(myView.filters.keySearch, songListSearchByKey);
        showHideRows(myView.filters.tagSearch, songListSearchByTag);
        showHideRows(myView.filters.filterSearch, songListSearchByFilter);


    }

    private void setUpExposedDropDowns() {
        new Thread(() -> {
            try {
                keyArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, getResources().getStringArray(R.array.key_choice));
                requireActivity().runOnUiThread(() -> {
                    myView.filters.keySearch.setAdapter(keyArrayAdapter);
                    myView.filters.keySearch.addTextChangedListener(new MyTextWatcher("key"));
                    myView.filters.artistSearch.addTextChangedListener(new MyTextWatcher("artist"));
                    myView.filters.tagSearch.addTextChangedListener(new MyTextWatcher("tag"));
                    myView.filters.filterSearch.addTextChangedListener(new MyTextWatcher("filter"));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setFolders() {
        new Thread(() -> {
            foundFolders = mainActivityInterface.getSQLiteHelper().getFolders(getContext(), mainActivityInterface);
            folderArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, foundFolders);
            Log.d(TAG, "foundFolders.size(): " + foundFolders.size());
            requireActivity().runOnUiThread(() -> {
                myView.filters.folderSearch.setAdapter(folderArrayAdapter);
                // folderSearchVal = mainActivityInterface.getSong().getFolder();
                myView.filters.folderSearch.addTextChangedListener(new MyTextWatcher("folder"));
                int pos = foundFolders.indexOf(mainActivityInterface.getSong().getFolder());
                if (pos >= 0) {
                    myView.filters.folderSearch.setText(foundFolders.get(pos));
                }
            });
        }).start();
    }

    private void fixButtons() {
        fixColor(myView.folderButton, songListSearchByFolder);
        fixColor(myView.artistButton, songListSearchByArtist);
        fixColor(myView.keyButton, songListSearchByKey);
        fixColor(myView.tagButton, songListSearchByTag);
        fixColor(myView.filterButton, songListSearchByFilter);
        prepareSearch();
    }

    private void fixColor(Button button, boolean active) {
        int activecolor = getResources().getColor(R.color.colorSecondary);
        int inactivecolor = getResources().getColor(R.color.transparent);
        if (active) {
            button.setBackgroundColor(activecolor);
        } else {
            button.setBackgroundColor(inactivecolor);
        }
    }

    private void setListeners() {
        myView.actionFAB.setOnClickListener(v -> {
            if (songButtonActive) {
                songButtonActive = false;
                Handler h = new Handler();
                h.postDelayed(() -> songButtonActive = true, 600);
                showActionDialog();
            }
        });
        myView.songListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    myView.actionFAB.show();
                } else {
                    myView.actionFAB.hide();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        myView.folderButton.setOnClickListener(v -> {
            songListSearchByFolder = !songListSearchByFolder;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireActivity(), "songListSearchByFolder", songListSearchByFolder);
            fixButtons();
            showHideRows(myView.filters.folderSearch, songListSearchByFolder);
            if (songListSearchByFolder) {
                runShowCaseSequence(myView.folderButton, myView.filters.folderSearch, getString(R.string.filter_by_folder),
                        getString(R.string.filter_by_dropdown), true, true, "myView.filters.folderSearch");
            }
        });
        myView.artistButton.setOnClickListener(v -> {
            songListSearchByArtist = !songListSearchByArtist;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireActivity(), "songListSearchByArtist", songListSearchByArtist);
            fixButtons();
            showHideRows(myView.filters.artistSearch, songListSearchByArtist);
            if (songListSearchByArtist) {
                runShowCaseSequence(myView.artistButton, myView.filters.artistSearch, getString(R.string.filter_by_artist),
                        getString(R.string.filter_by_edit), true, true, "myView.filters.artistSearch");
            }
        });
        myView.keyButton.setOnClickListener(v -> {
            songListSearchByKey = !songListSearchByKey;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireActivity(), "songListSearchByKey", songListSearchByKey);
            fixButtons();
            showHideRows(myView.filters.keySearch, songListSearchByKey);
            if (songListSearchByKey) {
                runShowCaseSequence(myView.keyButton, myView.filters.keySearch, getString(R.string.filter_by_key),
                        getString(R.string.filter_by_dropdown), true, true, "myView.filters.keySearch");
            }
        });
        myView.tagButton.setOnClickListener(v -> {
            songListSearchByTag = !songListSearchByTag;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireActivity(), "songListSearchByTag", songListSearchByTag);
            fixButtons();
            showHideRows(myView.filters.tagSearch, songListSearchByTag);
            if (songListSearchByTag) {
                runShowCaseSequence(myView.tagButton, myView.filters.tagSearch, getString(R.string.filter_by_tag),
                        getString(R.string.filter_by_edit), true, true, "myView.filters.tagSearch");
            }
        });
        myView.filterButton.setOnClickListener(v -> {
            songListSearchByFilter = !songListSearchByFilter;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireActivity(), "songListSearchByFilter", songListSearchByFilter);
            fixButtons();
            showHideRows(myView.filters.filterSearch, songListSearchByFilter);
            if (songListSearchByFilter) {
                runShowCaseSequence(myView.filterButton, myView.filters.filterSearch, getString(R.string.filter_by_this_value),
                        getString(R.string.filter_by_edit), true, true, "myView.filters.filterSearch");
            }
        });
    }

    private void showActionDialog() {
        SongMenuBottomSheet songMenuBottomSheet = new SongMenuBottomSheet();
        songMenuBottomSheet.show(getParentFragmentManager(), "songMenuActions");
    }

    private void showHideRows(View view, boolean show) {
        if (show) {
            view.post(() -> view.setVisibility(View.VISIBLE));
        } else {
            view.post(() -> view.setVisibility(View.GONE));
        }
    }

    // Get the values from the spinners and edit texts for filtering
    private void getSearchVals() {
        folderSearchVal = myView.filters.folderSearch.getText().toString();
        keySearchVal = myView.filters.keySearch.getText().toString();
    }

    private void buttonsEnabled(boolean enabled) {
        myView.folderButton.setEnabled(enabled);
        myView.artistButton.setEnabled(enabled);
        myView.keyButton.setEnabled(enabled);
        myView.tagButton.setEnabled(enabled);
        myView.filterButton.setEnabled(enabled);
        myView.actionFAB.setEnabled(enabled);
    }

    private void prepareSearch() {
        getSearchVals();
        new Thread(() -> {
            requireActivity().runOnUiThread(() -> buttonsEnabled(false));
            try {
                songsFound = mainActivityInterface.getSQLiteHelper().getSongsByFilters(getActivity(), mainActivityInterface,
                        songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
                        songListSearchByTag, songListSearchByFilter, folderSearchVal,
                        artistSearchVal, keySearchVal, tagSearchVal, filterSearchVal);
            } catch (Exception e) {
                e.printStackTrace();
            }
            requireActivity().runOnUiThread(this::updateSongList);
        }).start();
    }


    private void updateSongList() {
        myView.songListRecyclerView.removeAllViews();
        myView.songmenualpha.sideIndex.removeAllViews();
        myView.songListRecyclerView.setOnClickListener(null);
        songListAdapter = new SongListAdapter(requireContext(), mainActivityInterface,
                songsFound, SongMenuFragment.this);
        // TODO
        // When the list is redrawn the set checks are wiped

        myView.songListRecyclerView.setAdapter(songListAdapter);
        myView.songListRecyclerView.setFastScrollEnabled(true);
        displayIndex(songsFound, songListAdapter);
        myView.progressBar.setVisibility(View.GONE);
        buttonsEnabled(true);
        // Update the filter row values
        //setFolders();
    }

    private void displayIndex(ArrayList<Song> songMenuViewItems,
                              SongListAdapter songListAdapter) {
        myView.songmenualpha.sideIndex.removeAllViews();
        TextView textView;
        final Map<String, Integer> map = songListAdapter.getAlphaIndex(songMenuViewItems);
        Set<String> setString = map.keySet();
        List<String> indexList = new ArrayList<>(setString);
        for (String index : indexList) {
            textView = (TextView) View.inflate(getActivity(), R.layout.view_alphabetical_list, null);
            textView.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(getContext(), "songMenuAlphaIndexSize", 14.0f));
            int i = (int) mainActivityInterface.getPreferences().getMyPreferenceFloat(getContext(), "songMenuAlphaIndexSize", 14.0f) * 2;
            textView.setPadding(i, i, i, i);
            textView.setText(index);
            textView.setOnClickListener(view -> {
                TextView selectedIndex = (TextView) view;
                try {
                    if (selectedIndex.getText() != null && getActivity() != null &&
                            myView.songListRecyclerView.getLayoutManager() != null) {
                        String myval = selectedIndex.getText().toString();
                        if (!map.isEmpty()) {
                            Integer obj = map.get(myval);
                            if (obj != null) {
                                songListLayoutManager.scrollToPositionWithOffset(obj, 0);
                            }
                        }
                        mainActivityInterface.hideKeyboard();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            myView.songmenualpha.sideIndex.addView(textView);
        }
        changeAlphabeticalVisibility(mainActivityInterface.getPreferences().getMyPreferenceBoolean(getContext(), "songMenuAlphaIndexShow", true));
    }

    public void changeAlphabeticalLayout(Context c) {
        // We have asked for the visibility or the font size to change
        /*boolean showMenu = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,
                "songMenuAlphaIndexShow", true);
        boolean showChecks = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,
                "songMenuSetTicksShow", true);
        float fontSize = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,
                "songMenuAlphaIndexSize", 12.0f);
        changeAlphabeticalVisibility(showMenu);*/
        updateSongList();
    }
    private void changeAlphabeticalVisibility(boolean isVisible) {
        if (isVisible) {
            myView.songmenualpha.sideIndex.setVisibility(View.VISIBLE);
        } else {
            myView.songmenualpha.sideIndex.setVisibility(View.GONE);
        }
    }
    @Override
    public void onItemClicked(int position, String folder, String filename) {
        Log.d(TAG,"Try to load: "+folder+"/"+filename);
        mainActivityInterface.hideKeyboard();
        mainActivityInterface.doSongLoad(folder, filename);
    }

    @Override
    public void onItemLongClicked(int position, String folder, String filename) {
        mainActivityInterface.hideKeyboard();
        myView.actionFAB.performClick();
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
        mainActivityInterface.getShowCase().sequenceShowCase(requireActivity(), targets, dismisses, infos, rects, id);
    }

    public void updateSongMenu(Song song) {
        // Set values
        setValues(song);

        // Get folders
        setFolders();

        // Set up the spinners
        setUpExposedDropDowns();

        // Set up page buttons
        setListeners();

        // Prepare the song menu (includes a call to update the prepareSearch
        fixButtons();
    }

    public void moveToSongInMenu(Song song) {
        //scroll to the song in the song menu
        int index = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface, song);
        try {
            if (index >= 0) {
                new Thread(() -> requireActivity().runOnUiThread(() -> songListLayoutManager.scrollToPositionWithOffset(index, 0))).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                saveVal();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        public void saveVal() {
            switch (what) {
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
