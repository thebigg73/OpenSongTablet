package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.MenuSongsBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SongMenuFragment extends Fragment implements SongListAdapter.AdapterCallback {

    private final String TAG = "SongMenuFragment";
    // The helper classes used
    private Preferences preferences;
    private StorageAccess storageAccess;
    private Locale locale;
    private SQLiteHelper sqLiteHelper;
    private CommonSQL commonSQL;
    private MenuSongsBinding myView;
    private ShowCase showCase;
    private SetActions setActions;
    private CurrentSet currentSet;
    private boolean songButtonActive = true;
    private String folderSearchVal="", artistSearchVal="", keySearchVal="", tagSearchVal="", filterSearchVal="";
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

        // Initialise helpers
        initialiseHelpers();

        // Initialise views
        initialiseRecyclerView();

        // Update the song menu
        updateSongMenu(mainActivityInterface.getSong());

        return myView.getRoot();
    }

    private void initialiseHelpers() {
        preferences = mainActivityInterface.getPreferences();
        storageAccess = mainActivityInterface.getStorageAccess();
        locale = mainActivityInterface.getLocale();
        sqLiteHelper = mainActivityInterface.getSQLiteHelper();
        commonSQL = mainActivityInterface.getCommonSQL();
        currentSet = mainActivityInterface.getCurrentSet();
        showCase = mainActivityInterface.getShowCase();
        setActions = mainActivityInterface.getSetActions();
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
    }



    private void setValues(Song song) {
        songListSearchByFolder = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByFolder", false);
        if (songListSearchByFolder && folderSearchVal.isEmpty()) {
            // Likely the first run
            folderSearchVal = song.getFolder();
            // Do on the UI thread
            myView.filters.folderSearch.post(() -> myView.filters.folderSearch.setText(folderSearchVal));
        }
        songListSearchByArtist = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByArtist", false);
        songListSearchByKey = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByKey", false);
        songListSearchByTag = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByTag", false);
        songListSearchByFilter = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByFilter", false);
        showHideRows(myView.filters.folderRow,songListSearchByFolder);
        showHideRows(myView.filters.artistRow,songListSearchByArtist);
        showHideRows(myView.filters.keyRow,songListSearchByKey);
        showHideRows(myView.filters.tagRow,songListSearchByTag);
        showHideRows(myView.filters.filterRow,songListSearchByFilter);
    }

    private void setUpExposedDropDowns(Song song) {
        new Thread(() -> {
            try {
                foundFolders = sqLiteHelper.getFolders(getContext(), mainActivityInterface);
                folderArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, foundFolders);
                keyArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, getResources().getStringArray(R.array.key_choice));
                requireActivity().runOnUiThread(() -> {
                    myView.filters.folderSearch.setAdapter(folderArrayAdapter);
                    folderSearchVal = song.getFolder();
                    myView.filters.folderSearch.addTextChangedListener(new MyTextWatcher("folder"));
                    if (myView.filters.folderSearch != null) {
                        int pos = foundFolders.indexOf(song.getFolder());
                        if (pos >= 0) {
                            myView.filters.folderSearch.setText(foundFolders.get(pos));
                        }
                    }
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

    public void showActionButton(boolean show) {
        if (show) {
            myView.actionFAB.show();
        } else {
            myView.actionFAB.hide();
        }

    }

    private void fixButtons() {
        fixColor(myView.folderButton, songListSearchByFolder);
        fixColor(myView.artistButton, songListSearchByArtist);
        fixColor(myView.keyButton, songListSearchByKey);
        fixColor(myView.tagButton, songListSearchByTag);
        fixColor(myView.filterButton, songListSearchByFilter);
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
        myView.actionFAB.setOnClickListener(v  -> {
            if (songButtonActive) {
                songButtonActive = false;
                Handler h = new Handler();
                h.postDelayed(() -> songButtonActive = true,600);
                showActionButton(false);
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
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByFolder",songListSearchByFolder);
            fixButtons();
            showHideRows(myView.filters.folderRow,songListSearchByFolder);
            if (songListSearchByFolder) {
                runShowCaseSequence(myView.folderButton, myView.filters.folderRow, getString(R.string.filter_by_folder),
                        getString(R.string.filter_by_dropdown), true,true,"myView.filters.folderSearch");
            }
        });
        myView.artistButton.setOnClickListener(v -> {
            songListSearchByArtist = !songListSearchByArtist;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByArtist",songListSearchByArtist);
            fixButtons();
            showHideRows(myView.filters.artistRow,songListSearchByArtist);
            if (songListSearchByArtist) {
                runShowCaseSequence(myView.artistButton, myView.filters.artistRow, getString(R.string.filter_by_artist),
                        getString(R.string.filter_by_edit), true, true,"myView.filters.artistSearch");
            }
        });
        myView.keyButton.setOnClickListener(v -> {
            songListSearchByKey = !songListSearchByKey;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByKey",songListSearchByKey);
            fixButtons();
            showHideRows(myView.filters.keyRow,songListSearchByKey);
            if (songListSearchByKey) {
                runShowCaseSequence(myView.keyButton, myView.filters.keyRow, getString(R.string.filter_by_key),
                        getString(R.string.filter_by_dropdown), true,true,"myView.filters.keySearch");
            }
        });
        myView.tagButton.setOnClickListener(v -> {
            songListSearchByTag = !songListSearchByTag;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByTag",songListSearchByTag);
            fixButtons();
            showHideRows(myView.filters.tagRow,songListSearchByTag);
            if (songListSearchByTag) {
                runShowCaseSequence(myView.tagButton, myView.filters.tagRow, getString(R.string.filter_by_tag),
                        getString(R.string.filter_by_edit), true,true,"myView.filters.tagSearch");
            }
        });
        myView.filterButton.setOnClickListener(v -> {
            songListSearchByFilter = !songListSearchByFilter;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByFilter",songListSearchByFilter);
            fixButtons();
            showHideRows(myView.filters.filterRow,songListSearchByFilter);
            if (songListSearchByFilter) {
                runShowCaseSequence(myView.filterButton, myView.filters.filterRow, getString(R.string.filter_by_this_value),
                        getString(R.string.filter_by_edit), true,true,"myView.filters.filterSearch");
            }
        });
    }

    private void showActionDialog() {
        SongMenuDialog dialogFragment = new SongMenuDialog(myView.actionFAB);
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "songMenuActions");
    }

    private void showHideRows(TextInputLayout tr, boolean show) {
        if (show) {
            tr.post(() -> tr.setVisibility(View.VISIBLE));
        } else {
            tr.post(() -> tr.setVisibility(View.GONE));
        }
    }
    // Get the values from the spinners and edit texts for filtering
    private void getSearchVals() {
        folderSearchVal = getAutoComplete(myView.filters.folderSearch);
        keySearchVal    = getAutoComplete(myView.filters.keySearch);
    }

    private String getAutoComplete(AutoCompleteTextView autoCompleteTextView) {
        if (autoCompleteTextView!=null && autoCompleteTextView.getText()!=null) {
            return autoCompleteTextView.getText().toString();
        } else {
            return "";
        }
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
                 songsFound = sqLiteHelper.getSongsByFilters(getActivity(), mainActivityInterface,
                        songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
                        songListSearchByTag, songListSearchByFilter, folderSearchVal,
                       artistSearchVal, keySearchVal, tagSearchVal, filterSearchVal);
            } catch (Exception e) {
                e.printStackTrace();
            }
            requireActivity().runOnUiThread(this::updateSongList);
        }).start();
    }

    public void setBasicSongMenu() {
        // This quickly scans the song folder and gets a list of songIds (folder+filname)
        // Build a basic, but quick song menu!

        // Update the file
        storageAccess.listSongs(getContext(),preferences,locale);

        // Build the basic menu
        sqLiteHelper.insertFast(getContext(),mainActivityInterface);

        // Update the view
        prepareSearch();
    }



    private void updateSongList() {
        myView.songListRecyclerView.removeAllViews();
        myView.songmenualpha.sideIndex.removeAllViews();
        myView.songListRecyclerView.setOnClickListener(null);
        songListAdapter = new SongListAdapter(requireContext(), mainActivityInterface,
                songsFound,SongMenuFragment.this);
        songListAdapter.notifyDataSetChanged();
        myView.songListRecyclerView.setAdapter(songListAdapter);
        myView.songListRecyclerView.setFastScrollEnabled(true);
        displayIndex(songsFound, songListAdapter);
        myView.progressBar.setVisibility(View.GONE);
        buttonsEnabled(true);
    }
    private void displayIndex(ArrayList<Song> songMenuViewItems,
                              SongListAdapter songListAdapter) {
        if (preferences.getMyPreferenceBoolean(getActivity(),"songMenuAlphaIndexShow",true)) {
            myView.songmenualpha.sideIndex.setVisibility(View.VISIBLE);
        } else {
            myView.songmenualpha.sideIndex.setVisibility(View.GONE);
        }
        myView.songmenualpha.sideIndex.removeAllViews();
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
                            myView.songListRecyclerView.getLayoutManager()!=null) {
                        String myval = selectedIndex.getText().toString();
                        Integer obj = map.get(myval);

                        songListLayoutManager.scrollToPositionWithOffset(obj,0);
                        mainActivityInterface.hideKeyboard();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            myView.songmenualpha.sideIndex.addView(textView);
        }
    }

    @Override
    public void onItemClicked(int position, String folder, String filename) {
        mainActivityInterface.hideKeyboard();
        mainActivityInterface.doSongLoad(folder,filename);
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
        showCase.sequenceShowCase(requireActivity(),targets,dismisses,infos,rects,id);
    }
    public void updateSongMenu(Song song) {
        // Set values
        setValues(song);

        // Set up the spinners
        setUpExposedDropDowns(song);

        // Set up page buttons
        setListeners();

        // Prepare the song menu (includes a call to update the prepareSearch
        fixButtons();
    }

    public void moveToSongInMenu(Song song) {
        //scroll to the song in the song menu
        int index = indexOfSongInMenu(song);
        try {
            if (index>=0) {
                new Thread(() -> requireActivity().runOnUiThread(() -> songListLayoutManager.scrollToPositionWithOffset(index,0))).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int indexOfSongInMenu(Song song) {
        if (setActions!=null && currentSet!=null && song!=null) {
            setActions.indexSongInSet(mainActivityInterface);
            return currentSet.getIndexSongInSet();
        } else {
            return -1;
        }
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
