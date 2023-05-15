package com.garethevans.church.opensongtablet.songmenu;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongMenuFragment extends Fragment implements SongListAdapter.AdapterCallback {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SongMenuFragment";
    // The helper classes used
    private MenuSongsBinding myView;
    private boolean songButtonActive = true;
    private boolean hasShownMenuShowcase = false;
    private String folderSearchVal = "", artistSearchVal = "", keySearchVal = "", tagSearchVal = "",
            filterSearchVal = "", titleSearchVal = "";
    private boolean songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
            songListSearchByTag, songListSearchByFilter, songListSearchByTitle;
    private ArrayList<Song> songsFound;
    private ExposedDropDownArrayAdapter folderArrayAdapter, keyArrayAdapter;
    private SongListAdapter songListAdapter;
    private LinearLayoutManager songListLayoutManager;
    private ArrayList<String> foundFolders;
    private int alphalistposition = -1;
    private String alphaSelected = "", filter_by_folder_string="", filter_by_dropdown_string="",
            new_folder_info_string="", filter_by_artist_string="", filter_by_edit_string="",
            filter_by_key_string="", filter_by_tag_string="", tag_song_info_string="",
            filter_by_this_value_string="", filter_by_title_string="", deeplink_tags_string="",
            deeplink_manage_storage_string="", mainfolder_string="";
    private String[] key_choice_string={};
    private boolean songMenuSortTitles;
    private final Handler waitBeforeSearchHandler = new Handler();
    private final Runnable waitBeforeSearchRunnable = this::prepareSearch;
    private String longClickFilename = "";


    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = MenuSongsBinding.inflate(inflater, container, false);

        prepareStrings();

        mainActivityInterface.registerFragment(this,"SongMenuFragment");
        // Initialise views
        initialiseRecyclerView();

        // Update the song menu
        try {
            updateSongMenu(mainActivityInterface.getSong());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            key_choice_string = getResources().getStringArray(R.array.key_choice);
            filter_by_folder_string = getString(R.string.filter_by_folder);
            filter_by_dropdown_string = getString(R.string.filter_by_dropdown);
            new_folder_info_string = getString(R.string.new_folder_info);
            filter_by_artist_string = getString(R.string.filter_by_artist);
            filter_by_edit_string = getString(R.string.filter_by_edit);
            filter_by_key_string = getString(R.string.filter_by_key);
            filter_by_tag_string = getString(R.string.filter_by_tag);
            tag_song_info_string = getString(R.string.tag_song_info);
            filter_by_this_value_string = getString(R.string.filter_by_this_value);
            filter_by_title_string = getString(R.string.filter_by_title);
            deeplink_manage_storage_string = getString(R.string.deeplink_manage_storage);
            deeplink_tags_string = getString(R.string.deeplink_tags);
            mainfolder_string = getString(R.string.mainfoldername);
        }
    }

    private void initialiseRecyclerView() {
        myView.songListRecyclerView.removeAllViews();
        myView.songmenualpha.sideIndex.removeAllViews();
        if (getContext()!=null) {
            try {
                songListLayoutManager = new LinearLayoutManager(getContext()){
                    @Override
                    public boolean supportsPredictiveItemAnimations() {
                        return false;
                    }

                    @Override
                    public void scrollToPosition(int position) {
                        try {
                            super.scrollToPosition(position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                songListLayoutManager.setOrientation(RecyclerView.VERTICAL);
                myView.songListRecyclerView.setLayoutManager(songListLayoutManager);
                myView.songListRecyclerView.setHasFixedSize(false);
                myView.songListRecyclerView.setOnClickListener(null);
                List<Song> blank = new ArrayList<>();
                songListAdapter = new SongListAdapter(getContext(), blank,
                        SongMenuFragment.this);
                myView.songListRecyclerView.setAdapter(songListAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setValues(Song song) {
        songListSearchByFolder = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songListSearchByFolder", false);
        if (songListSearchByFolder && folderSearchVal.isEmpty()) {
            // Likely the first run
            // Do on the UI thread - force a folder change when the song menu is opened
            myView.filters.folderSearch.post(() -> myView.filters.folderSearch.setText(song.getFolder()));
        }
        songListSearchByArtist = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songListSearchByArtist", false);
        songListSearchByKey = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songListSearchByKey", false);
        songListSearchByTag = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songListSearchByTag", false);
        songListSearchByFilter = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songListSearchByFilter", false);
        songListSearchByTitle = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songListSearchByTitle",false);
        showHideRows(myView.filters.folderLayout, songListSearchByFolder);
        showHideRows(myView.filters.artistSearch, songListSearchByArtist);
        showHideRows(myView.filters.keySearch, songListSearchByKey);
        showHideRows(myView.filters.tagLayout, songListSearchByTag);
        showHideRows(myView.filters.filterSearch, songListSearchByFilter);
        showHideRows(myView.filters.titleSearch, songListSearchByTitle);
    }

    private void setUpExposedDropDowns() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            if (getContext()!=null) {
                try {
                    keyArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                            myView.filters.keySearch, R.layout.view_exposed_dropdown_item, key_choice_string);
                    handler.post(() -> {
                        myView.filters.keySearch.setAdapter(keyArrayAdapter);
                        myView.filters.keySearch.addTextChangedListener(new MyTextWatcher("key"));
                        myView.filters.artistSearch.addTextChangedListener(new MyTextWatcher("artist"));
                        myView.filters.tagSearch.addTextChangedListener(new MyTextWatcher("tag"));
                        myView.filters.filterSearch.addTextChangedListener(new MyTextWatcher("filter"));
                        myView.filters.titleSearch.addTextChangedListener(new MyTextWatcher("title"));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setFolders() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            foundFolders = mainActivityInterface.getSQLiteHelper().getFolders();

            // We always want MAIN as the top folder (regardless of alphabetical sort position)
            foundFolders.remove(mainfolder_string);
            foundFolders.add(0,mainfolder_string);

            if (getContext()!=null) {
                handler.post(() -> {
                    if (getContext()!=null) {
                        folderArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.filters.folderSearch, R.layout.view_exposed_dropdown_item, foundFolders);
                        myView.filters.folderSearch.setAdapter(folderArrayAdapter);
                        myView.filters.folderSearch.setMultiselect(true);
                        myView.filters.folderSearch.addTextChangedListener(new MyTextWatcher("folder"));
                        int pos = foundFolders.indexOf(mainActivityInterface.getSong().getFolder());
                        if (pos >= 0) {
                            myView.filters.folderSearch.setText(foundFolders.get(pos));
                        }
                    }
                });
            }
        });
    }

    private void fixButtons() {
        fixColor(myView.filterButtons.folderButton, songListSearchByFolder);
        fixColor(myView.filterButtons.artistButton, songListSearchByArtist);
        fixColor(myView.filterButtons.keyButton, songListSearchByKey);
        fixColor(myView.filterButtons.tagButton, songListSearchByTag);
        fixColor(myView.filterButtons.filterButton, songListSearchByFilter);
        fixColor(myView.filterButtons.titleButton, songListSearchByTitle);
        prepareSearch();
    }

    private void fixColor(Button button, boolean active) {
        try {
            int activecolor = getResources().getColor(R.color.colorSecondary);
            int inactivecolor = getResources().getColor(R.color.transparent);
            if (active) {
                button.setBackgroundColor(activecolor);
            } else {
                button.setBackgroundColor(inactivecolor);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        myView.filterButtons.folderButton.setOnClickListener(v -> {
            songListSearchByFolder = !songListSearchByFolder;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByFolder", songListSearchByFolder);
            fixButtons();
            showHideRows(myView.filters.folderLayout, songListSearchByFolder);
            if (songListSearchByFolder) {
                runShowCaseSequence(new View[] {myView.filterButtons.folderButton, myView.filters.folderSearch, myView.filters.manageFolders},
                        new String[] {filter_by_folder_string,
                                filter_by_dropdown_string,
                                new_folder_info_string},
                        new Boolean[] {true, true, true}, "myView.filters.folderSearch");
            }
        });
        myView.filterButtons.artistButton.setOnClickListener(v -> {
            songListSearchByArtist = !songListSearchByArtist;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByArtist", songListSearchByArtist);
            fixButtons();
            showHideRows(myView.filters.artistSearch, songListSearchByArtist);
            if (songListSearchByArtist) {
                runShowCaseSequence(new View[] {myView.filterButtons.artistButton, myView.filters.artistSearch},
                        new String[] {filter_by_artist_string,
                                filter_by_edit_string},
                        new Boolean[]{true, true}, "myView.filters.artistSearch");
            }
        });
        myView.filterButtons.keyButton.setOnClickListener(v -> {
            songListSearchByKey = !songListSearchByKey;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByKey", songListSearchByKey);
            fixButtons();
            showHideRows(myView.filters.keySearch, songListSearchByKey);
            if (songListSearchByKey) {
                runShowCaseSequence(new View[] {myView.filterButtons.keyButton, myView.filters.keySearch},
                        new String[] {filter_by_key_string,
                                filter_by_dropdown_string},
                        new Boolean[]{true, true}, "myView.filters.keySearch");
            }
        });
        myView.filterButtons.tagButton.setOnClickListener(v -> {
            songListSearchByTag = !songListSearchByTag;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByTag", songListSearchByTag);
            fixButtons();
            showHideRows(myView.filters.tagLayout, songListSearchByTag);
            if (songListSearchByTag) {
                runShowCaseSequence(new View[] {myView.filterButtons.tagButton, myView.filters.tagSearch, myView.filters.manageTags},
                        new String[] {filter_by_tag_string,
                                filter_by_edit_string, tag_song_info_string},
                        new Boolean[] {true, true, true}, "myView.filters.tagSearch");
            }
        });
        myView.filterButtons.filterButton.setOnClickListener(v -> {
            songListSearchByFilter = !songListSearchByFilter;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByFilter", songListSearchByFilter);
            fixButtons();
            showHideRows(myView.filters.filterSearch, songListSearchByFilter);
            if (songListSearchByFilter) {
                runShowCaseSequence(new View[] {myView.filterButtons.filterButton, myView.filters.filterSearch},
                        new String[] {filter_by_this_value_string,
                                filter_by_edit_string},
                        new Boolean[] {true, true}, "myView.filters.filterSearch");
            }
        });
        myView.filterButtons.titleButton.setOnClickListener(v -> {
            songListSearchByTitle = !songListSearchByTitle;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByTitle",songListSearchByTitle);
            fixButtons();
            showHideRows(myView.filters.titleSearch, songListSearchByTitle);
            if (songListSearchByTitle) {
                runShowCaseSequence(new View[] {myView.filterButtons.titleButton, myView.filters.titleSearch},
                        new String[] {filter_by_title_string,
                                filter_by_edit_string},
                        new Boolean[] {true, true}, "myView.filters.titleSearch");
            }
        });
        myView.filters.manageFolders.setOnClickListener(v -> mainActivityInterface.navigateToFragment(deeplink_manage_storage_string,0));
        myView.filters.manageTags.setOnClickListener(v -> mainActivityInterface.navigateToFragment(deeplink_tags_string,0));
    }

    private void showActionDialog() {
        SongMenuBottomSheet songMenuBottomSheet = new SongMenuBottomSheet(longClickFilename);
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
        myView.filterButtons.folderButton.setEnabled(enabled);
        myView.filterButtons.artistButton.setEnabled(enabled);
        myView.filterButtons.keyButton.setEnabled(enabled);
        myView.filterButtons.tagButton.setEnabled(enabled);
        myView.filterButtons.filterButton.setEnabled(enabled);
        myView.filterButtons.titleButton.setEnabled(enabled);
        myView.actionFAB.setEnabled(enabled);
    }

    public void refreshSongList() {
        prepareSearch();
    }
    public void prepareSearch() {
        if (mainActivityInterface!=null) {
            songMenuSortTitles = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles", true);
            getSearchVals();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> buttonsEnabled(false));
                try {
                    songsFound = mainActivityInterface.getSQLiteHelper().getSongsByFilters(
                            songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
                            songListSearchByTag, songListSearchByFilter, songListSearchByTitle,
                            folderSearchVal, artistSearchVal, keySearchVal, tagSearchVal,
                            filterSearchVal, titleSearchVal, songMenuSortTitles);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.post(() -> {
                    updateSongList();
                    displayIndex();
                });
            });
        }
    }

    public void updateCheckForThisSong(Song thisSong) {
        // Call to update something about a specific song
        int pos = -1;
        for (int i=0; i<songsFound.size(); i++) {
            if (songsFound.get(i).getFilename().equals(thisSong.getFilename()) &&
                    songsFound.get(i).getFolder().equals(thisSong.getFolder())) {
                pos = i;
                break;
            }
        }
        if (pos>-1) {
            // Update the checklist in the adapter
            songListAdapter.changeCheckBox(pos);
        }
    }

    public void updateSongList() {
        if (getContext()!=null) {
            try {
                myView.songListRecyclerView.removeAllViews();
                myView.songmenualpha.sideIndex.removeAllViews();
                myView.songListRecyclerView.setOnClickListener(null);
                songListAdapter = new SongListAdapter(getContext(),
                        songsFound, SongMenuFragment.this);
                myView.songListRecyclerView.setAdapter(songListAdapter);
                displayIndex();
                myView.progressBar.setVisibility(View.GONE);
                buttonsEnabled(true);
                // Update the filter row values
                //setFolders();
            } catch (Exception e) {
                Log.d(TAG, "The app closed before the menu was finished");
            }
        }
    }

    public void displayIndex() {
        if (mainActivityInterface!=null && getContext()!=null) {
            try {
                myView.songmenualpha.sideIndex.removeAllViews();
                TextView textView;
                final Map<String, Integer> map = songListAdapter.getAlphaIndex(songsFound);
                Set<String> setString = map.keySet();
                List<String> indexList = new ArrayList<>(setString);
                int i = (int) mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuAlphaIndexSize", 14.0f);
                for (int p = 0; p < indexList.size(); p++) {
                    String index = indexList.get(p);
                    if (getActivity() != null) {
                        textView = (TextView) View.inflate(getActivity(), R.layout.view_alphabetical_list, null);
                        if (textView != null) {
                            textView.setTextSize(i);
                            textView.setPadding(i, i, i, i);
                            textView.setMinimumWidth(16);
                            textView.setMinimumHeight(16);
                            textView.setText(index);
                            int finalP = p;
                            textView.setOnClickListener(view -> {
                                TextView selectedIndex = (TextView) view;
                                try {
                                    if (selectedIndex.getText() != null &&
                                            songListLayoutManager != null) {
                                        String myval = selectedIndex.getText().toString();

                                        if (!map.isEmpty()) {
                                            Integer obj = map.get(myval);
                                            if (obj != null) {
                                                songListLayoutManager.scrollToPositionWithOffset(obj, 0);
                                            }
                                        }
                                        alphaSelected = index;
                                        alphalistposition = finalP;
                                        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuAlphaIndexLevel2", false)) {
                                            displayIndex2();
                                        }
                                        mainActivityInterface.getWindowFlags().hideKeyboard();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            myView.songmenualpha.sideIndex.addView(textView);
                        }
                    }
                }
                changeAlphabeticalVisibility(mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuAlphaIndexShow", true));

                myView.songmenualpha.sideIndex.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        myView.songmenualpha.sideIndex.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (myView.songmenualpha.sideIndex.getChildCount() > alphalistposition && myView.songmenualpha.sideIndex.getChildAt(alphalistposition) != null) {
                            myView.songmenualpha.indexScrollview.scrollTo(0, myView.songmenualpha.sideIndex.getChildAt(alphalistposition).getTop());
                        } else {
                            myView.songmenualpha.indexScrollview.scrollTo(0, 0);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void displayIndex2() {
        try {
            myView.songmenualpha.sideIndex.removeAllViews();
            TextView textView;
            final Map<String, Integer> map2 = songListAdapter.getAlphaIndex2();
            Set<String> setString = map2.keySet();
            List<String> indexList = new ArrayList<>(setString);
            for (String index : indexList) {
                textView = (TextView) View.inflate(getActivity(), R.layout.view_alphabetical_list, null);
                if (textView != null) {
                    int i = (int) mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuAlphaIndexSize", 14.0f);
                    textView.setTextSize(i);
                    textView.setPadding(i, i, i, i);
                    textView.setMinimumWidth(16);
                    textView.setMinimumHeight(16);
                    // IV - Display the 2 char index over 1 or 2 lines.  The trim removes trailing '\n ' giving one line when the second char of the index is a space.
                    String indexlines = (index.charAt(0) + "\n" + index.charAt(1)).trim();
                    textView.setText(indexlines);
                    textView.setOnClickListener(view -> {
                        TextView selectedIndex = (TextView) view;
                        try {
                            if (selectedIndex.getText() != null &&
                                    songListLayoutManager != null) {
                                // IV - Recover the 2 char index from the 1 or 2 lines of displayed text
                                String myval = (selectedIndex.getText().toString().replace("\n", "") + " ").substring(0,2);

                                if (!map2.isEmpty()) {
                                    Integer obj = map2.get(myval);
                                    if (obj != null) {
                                        songListLayoutManager.scrollToPositionWithOffset(obj, 0);
                                    }
                                }
                                displayIndex();
                                mainActivityInterface.getWindowFlags().hideKeyboard();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    myView.songmenualpha.sideIndex.addView(textView);
                }
            }
            changeAlphabeticalVisibility(mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuAlphaIndexShow", true));
            myView.songmenualpha.sideIndex.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int pos = songListAdapter.getPositionOfAlpha2fromAlpha(alphaSelected);
                    myView.songmenualpha.sideIndex.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (myView.songmenualpha.sideIndex.getChildCount()>pos && myView.songmenualpha.sideIndex.getChildAt(pos)!=null) {
                        int childTop = myView.songmenualpha.sideIndex.getChildAt(pos).getTop();
                        myView.songmenualpha.indexScrollview.scrollTo(0, childTop);
                    } else {
                        myView.songmenualpha.indexScrollview.scrollTo(0,0);
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeAlphabeticalLayout() {
        // We have asked for the visibility or the font size to change
        refreshSongList();
    }
    private void changeAlphabeticalVisibility(boolean isVisible) {
        if (isVisible) {
            myView.songmenualpha.sideIndex.setVisibility(View.VISIBLE);
        } else {
            myView.songmenualpha.sideIndex.setVisibility(View.GONE);
        }
    }
    @Override
    public void onItemClicked(int position, String folder, String filename, String key) {
        mainActivityInterface.getWindowFlags().hideKeyboard();
        // Default the slide animations to be next (R2L)
        mainActivityInterface.getDisplayPrevNext().setSwipeDirection("R2L");
        mainActivityInterface.doSongLoad(folder, filename,true);
        songListLayoutManager.scrollToPositionWithOffset(position,0);
        // Make sure the alphabetical index shows single letters
        displayIndex();
    }

    @Override
    public void onItemLongClicked(int position, String folder, String filename, String key) {
        longClickFilename = filename;
        mainActivityInterface.getWindowFlags().hideKeyboard();
        mainActivityInterface.doSongLoad(folder, filename,false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> songListLayoutManager.scrollToPositionWithOffset(position,0),1000);
        showActionDialog();
    }

    public void runShowCaseSequence(View[] views, String[] information, Boolean[] rectangles, String id) {
        if (getActivity() != null) {
            ArrayList<View> targets = new ArrayList<>(Arrays.asList(views));
            ArrayList<String> infos = new ArrayList<>(Arrays.asList(information));
            ArrayList<Boolean> rects = new ArrayList<>(Arrays.asList(rectangles));
            mainActivityInterface.getShowCase().sequenceShowCase(getActivity(), targets, null, infos, rects, id);
        }
    }

    public void updateSongMenu(Song song) {
        if (getContext()!=null) {
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

            if (songListAdapter != null) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        if (mainActivityInterface!=null && myView!=null) {
                            try {
                                myView.menuSongs.findViewById(R.id.setCheckTitle).setVisibility(
                                        mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSetTicksShow", true) ?
                                                View.VISIBLE : View.GONE);

                                songListAdapter.notifyItemRangeChanged(0, songListAdapter.getItemCount());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                });
            }
        }
    }

    public void moveToSongInMenu(Song song) {
        //scroll to the song in the song menu
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                if (songListLayoutManager!=null) {
                    handler.post(() -> {
                        try {
                            songListLayoutManager.scrollToPositionWithOffset(songListAdapter.getPositionOfSong(song),0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPositionInSongMenu(Song song) {
        if (song!=null && songListAdapter!=null) {
            return songListAdapter.getPositionOfSong(song);
        } else {
            return -1;
        }
    }

    public ArrayList<Song> getSongsFound() {
        if (songsFound==null) {
            try {
                songsFound = mainActivityInterface.getSQLiteHelper().getSongsByFilters(
                        songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
                        songListSearchByTag, songListSearchByFilter, songListSearchByTitle,
                        folderSearchVal, artistSearchVal, keySearchVal, tagSearchVal,
                        filterSearchVal, titleSearchVal, songMenuSortTitles);
            } catch (Exception e) {
                songsFound = new ArrayList<>();
                e.printStackTrace();
            }
        }
        return songsFound;
    }
    public void setSongsFound(ArrayList<Song> songsFound) {
        this.songsFound = songsFound;
    }
    public ArrayList<Song> getSongs() {
        return songsFound;
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
                case "title":
                    titleSearchVal = value;
                    break;
            }
            waitBeforeSearchHandler.removeCallbacks(waitBeforeSearchRunnable);
            waitBeforeSearchHandler.postDelayed(waitBeforeSearchRunnable,500);
        }
    }


    public void scrollMenu(int height) {
        try {
            myView.songListRecyclerView.smoothScrollBy(0, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MaterialTextView getProgressText() {
        return myView.progressText;
    }

    public void updateSongMenuSortTitles() {
        if (songListAdapter!=null && mainActivityInterface!=null) {
            songListAdapter.updateSongMenuSortTitles(mainActivityInterface.getPreferences().
                    getMyPreferenceBoolean("songMenuSortTitles",true));
        }
    }


    // Showing the main showcase for the menu gets triggered onDrawerOpened.  This can be called
    // twice in quick succession before the preference is checked.  Add this check
    public boolean getHasShownMenuShowcase() {
        return hasShownMenuShowcase;
    }

    public void setHasShownMenuShowcase(boolean hasShownMenuShowcase) {
        this.hasShownMenuShowcase = hasShownMenuShowcase;
    }

}
