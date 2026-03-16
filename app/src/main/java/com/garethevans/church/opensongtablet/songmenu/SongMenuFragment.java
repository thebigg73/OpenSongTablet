package com.garethevans.church.opensongtablet.songmenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.MyMaterialButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.customviews.WrapContentLinearLayoutManager;
import com.garethevans.church.opensongtablet.databinding.MenuSongsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class SongMenuFragment extends Fragment implements SongListAdapter.AdapterCallback {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SongMenuFragment";
    private SongMenuSongs songMenuSongs;
    // The helper classes used
    private MenuSongsBinding myView;
    private boolean songButtonActive = true;
    private boolean hasShownMenuShowcase = false, adapterReady = false;
    private String folderSearchVal = "", artistSearchVal = "", keySearchVal = "", tagSearchVal = "",
            filterSearchVal = "", titleSearchVal = "";
    private boolean songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
            songListSearchByTag, songListSearchByFilter, songListSearchByTitle;
    private String songListSearchByFolderValue;
    private ExposedDropDownArrayAdapter folderArrayAdapter, keyArrayAdapter;
    private SongListAdapter songListAdapter;
    private LinearLayoutManager songListLayoutManager;
    private ArrayList<String> foundFolders;
    private String filter_by_folder_string="", filter_by_dropdown_string="",
            new_folder_info_string="", filter_by_artist_string="", filter_by_edit_string="",
            filter_by_key_string="", filter_by_tag_string="", tag_song_info_string="",
            filter_by_this_value_string="", filter_by_title_string="", deeplink_tags_string="",
            deeplink_manage_storage_string="", add_all_songs_to_set_string="", songs_string="";
    private String[] key_choice_string={};
    private boolean songMenuSortTitles;
    private final Handler waitBeforeSearchHandler = new Handler();
    private final Runnable waitBeforeSearchRunnable = this::prepareSearch;
    private String longClickFilename = "";
    private IndexAdapter indexAdapter;

    private static MainActivityInterface mainActivityInterface;

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
    public void onResume() {
        super.onResume();
        prepareStrings();
        getSongListSearchByFolder();
        getSongListSearchByFolderValue();

        //initialiseRecyclerView();
        try {
            moveToSongInMenu(mainActivityInterface.getSong());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = MenuSongsBinding.inflate(inflater, container, false);

        // Create the view model
        songMenuSongs = new ViewModelProvider(this).get(SongMenuSongs.class);
        return myView.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tint the views based on the colorPalette
        myView.getRoot().setBackgroundColor(mainActivityInterface.getPalette().background);
        mainActivityInterface.getMyThemeColors().tintProgressBar(myView.progressBar);

        prepareStrings();

        mainActivityInterface.registerFragment(this,"SongMenuFragment");

        // Initialize views
        adapterReady = false;
        initialiseRecyclerView();

        // Update the song menu
        try {
            updateSongMenu();
            moveToSongInMenu(mainActivityInterface.getSong());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            add_all_songs_to_set_string = getString(R.string.add_all_songs_to_set);
            songs_string = getString(R.string.songs);
        }
    }

    private void initialiseRecyclerView() {
        // Some of these need to run on the UI
        songListLayoutManager = new WrapContentLinearLayoutManager(getContext());
        //songListLayoutManager = new LinearLayoutManager(getContext());
        songListLayoutManager.setOrientation(RecyclerView.VERTICAL);
        songListAdapter = new SongListAdapter(getContext(), this, songMenuSongs);

        myView.songListRecyclerView.setLayoutManager(songListLayoutManager);
        myView.songListRecyclerView.setHasFixedSize(false);
        myView.songListRecyclerView.setOnClickListener(null);
        myView.songListRecyclerView.setAdapter(songListAdapter);

        adapterReady = true;
    }

    public void updateSongMenu() {
        if (getContext() != null) {

            // Set values
            setValues();

            // Get folders
            setFolders();

            // Set up the spinners
            setUpExposedDropDowns();

            // Set up page buttons
            setListeners();

            // Prepare the song menu (includes a call to update the prepareSearch
            fixButtons();
        }

    }

    private void setValues() {
        getSongListSearchByFolder();
        getSongListSearchByFolderValue();
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
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            if (getContext()!=null) {
                try {
                    keyArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                            myView.filters.keySearch, R.layout.view_exposed_dropdown_item, key_choice_string);
                    mainActivityInterface.getMainHandler().post(() -> {
                        if (myView!=null) {
                            myView.filters.keySearch.setText("");
                            myView.filters.artistSearch.setText("");
                            myView.filters.tagSearch.setText("");
                            myView.filters.filterSearch.setText("");
                            myView.filters.titleSearch.setText("");
                            myView.filters.keySearch.setAdapter(keyArrayAdapter);
                            myView.filters.keySearch.addTextChangedListener(new MyTextWatcher("key"));
                            myView.filters.artistSearch.addTextChangedListener(new MyTextWatcher("artist"));
                            myView.filters.tagSearch.addTextChangedListener(new MyTextWatcher("tag"));
                            myView.filters.filterSearch.addTextChangedListener(new MyTextWatcher("filter"));
                            myView.filters.titleSearch.addTextChangedListener(new MyTextWatcher("title"));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setFolders() {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            foundFolders = mainActivityInterface.getSQLiteHelper().getFolders();

            // We always want MAIN as the top folder (regardless of alphabetical sort position)
            foundFolders.remove(mainActivityInterface.getMainfoldername());
            foundFolders.add(0,mainActivityInterface.getMainfoldername());

            if (getContext()!=null) {
                mainActivityInterface.getMainHandler().post(() -> {
                    if (getContext()!=null) {
                        folderArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.filters.folderSearch, R.layout.view_exposed_dropdown_item, foundFolders);
                        myView.filters.folderSearch.setAdapter(folderArrayAdapter);
                        myView.filters.folderSearch.addTextChangedListener(new MyTextWatcher("folder"));

                        // If we are filtering folders, then set that position
                        if (songListSearchByFolder && !songListSearchByFolderValue.isEmpty() &&
                                foundFolders.contains(songListSearchByFolderValue) && myView!=null) {
                            myView.filters.folderSearch.setText(songListSearchByFolderValue);

                        } else if (songListSearchByFolder && myView!=null) {
                            // Clear the saved search folder and hide the filter as the folder doesn't exist
                            setSongListSearchByFolderValue("");
                            setSongListSearchByFolder(false);
                            showHideRows(myView.filters.folderLayout,false);
                            myView.filters.folderSearch.setText("");
                        }
                    }
                });
            }
        });
    }

    public void removeFiltersFromLoadSong() {
        // If we load a song from the set or received, we can hide the filters (may not be appropriate)
        boolean needToUpdateSearch = songListSearchByArtist || songListSearchByKey ||
                songListSearchByTag || songListSearchByTitle || songListSearchByFilter;

        if (songListSearchByArtist) {
            songListSearchByArtist = false;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByArtist", false);
            showHideRows(myView.filters.artistSearch, songListSearchByArtist);
        }

        if (songListSearchByKey) {
            songListSearchByKey = false;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByKey", false);
            showHideRows(myView.filters.keySearch, songListSearchByKey);
        }

        if (songListSearchByTag) {
            songListSearchByTag = false;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByTag", false);
            showHideRows(myView.filters.tagLayout, songListSearchByTag);
        }

        if (songListSearchByTitle) {
            songListSearchByTitle = false;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByTitle",false);
            showHideRows(myView.filters.titleSearch, songListSearchByTitle);
        }

        if (songListSearchByFilter) {
            songListSearchByFilter = false;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByFilter", false);
            showHideRows(myView.filters.filterSearch, songListSearchByFilter);
        }

        if (needToUpdateSearch) {
            fixButtons();
            mainActivityInterface.getMainHandler().postDelayed(() -> {
                try {
                    moveToSongInMenu(mainActivityInterface.getSong());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },1000);
        }
    }
    private void fixButtons() {
        fixColor(myView.filterButtons.folderButton, songListSearchByFolder);
        fixColor(myView.filterButtons.artistButton, songListSearchByArtist);
        fixColor(myView.filterButtons.keyButton, songListSearchByKey);
        fixColor(myView.filterButtons.tagButton, songListSearchByTag);
        fixColor(myView.filterButtons.filterButton, songListSearchByFilter);
        fixColor(myView.filterButtons.titleButton, songListSearchByTitle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myView.filterButtons.searchButtonGroup.setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().primary));
        } else {
            myView.filterButtons.searchButtonGroup.setBackgroundColor(mainActivityInterface.getPalette().primary);
        }
        prepareSearch();
    }

    private void fixColor(MyMaterialButton button, boolean active) {
        try {
            if (getContext()!=null) {
                int activecolor = mainActivityInterface.getPalette().secondary;
                int inactivecolor = getResources().getColor(R.color.transparent);
                if (active) {
                    button.setBackgroundColor(activecolor);
                } else {
                    button.setBackgroundColor(inactivecolor);
                }
                button.setIconTint(ColorStateList.valueOf(mainActivityInterface.getPalette().onPrimary));
                button.setStrokeColor(ColorStateList.valueOf(mainActivityInterface.getPalette().secondary));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setListeners() {
        myView.actionFAB.setOnClickListener(v -> {
            if (songButtonActive) {
                myView.songListRecyclerView.stopScroll();
                songButtonActive = false;
                mainActivityInterface.getMainHandler().postDelayed(() -> songButtonActive = true, 600);
                longClickFilename = mainActivityInterface.getSong().getFilename();
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
            myView.songListRecyclerView.stopScroll();
            songListSearchByFolder = !songListSearchByFolder;
            setSongListSearchByFolder(songListSearchByFolder);
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
            myView.songListRecyclerView.stopScroll();
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
            myView.songListRecyclerView.stopScroll();
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
            myView.songListRecyclerView.stopScroll();
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
            myView.songListRecyclerView.stopScroll();
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
            myView.songListRecyclerView.stopScroll();
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
        myView.filters.manageFolders.setOnClickListener(v -> {
            myView.songListRecyclerView.stopScroll();
            mainActivityInterface.navigateToFragment(deeplink_manage_storage_string,0);
        });
        myView.filters.manageTags.setOnClickListener(v -> {
            myView.songListRecyclerView.stopScroll();
            mainActivityInterface.navigateToFragment(deeplink_tags_string,0);
        });
        myView.songTitleStuff.setCheckTitle.setOnClickListener(v -> {
            if (songListAdapter!=null) {
                AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("addallsongstoset", add_all_songs_to_set_string + " (" + songListAdapter.getItemCount() + " " + songs_string + ")", null, "SongMenuFragment", this, null);
                areYouSureBottomSheet.show(getParentFragmentManager(), "AreYouSureBottomSheet");
            }
        });
    }

    private void showActionDialog() {
        SongMenuBottomSheet songMenuBottomSheet = new SongMenuBottomSheet(longClickFilename);
        songMenuBottomSheet.show(getParentFragmentManager(), "songMenuActions");
    }

    private void showHideRows(View view, boolean show) {
        view.post(() -> view.setVisibility(show ? View.VISIBLE:View.GONE));
    }

    // Get the values from the spinners and edit texts for filtering
    private void getSearchVals() {
        if (myView!=null) {
            folderSearchVal = myView.filters.folderSearch.getText().toString();
            keySearchVal = myView.filters.keySearch.getText().toString();
        } else {
            folderSearchVal="";
            keySearchVal="";
        }
    }

    private void buttonsEnabled(boolean enabled) {
        if (mainActivityInterface!=null) {
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView != null) {
                    try {
                        myView.filterButtons.folderButton.setEnabled(enabled);
                        myView.filterButtons.artistButton.setEnabled(enabled);
                        myView.filterButtons.keyButton.setEnabled(enabled);
                        myView.filterButtons.tagButton.setEnabled(enabled);
                        myView.filterButtons.filterButton.setEnabled(enabled);
                        myView.filterButtons.titleButton.setEnabled(enabled);
                        myView.actionFAB.setEnabled(enabled);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void refreshSongList() {
        prepareSearch();
    }
    public void prepareSearch() {
        if (mainActivityInterface != null) {
            if (!adapterReady) {
                //Try again soon
                mainActivityInterface.getMainHandler().postDelayed(this::prepareSearch, 1000);
            } else {
                adapterReady = false;
                songMenuSortTitles = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles", true);
                getSearchVals();
                buttonsEnabled(false);

                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    try {
                        // 1. Get the current list (Snapshotted to avoid the reference trap)
                        List<Song> oldList = new ArrayList<>();
                        if (songMenuSongs.getFoundSongs() != null) {
                            oldList.addAll(songMenuSongs.getFoundSongs());
                        }

                        // 2. Fetch the "new" list from DB
                        // Get the songs based on the filters
                        ArrayList<Song> newList = mainActivityInterface.getSQLiteHelper().getSongsByFilters(
                                songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
                                songListSearchByTag, songListSearchByFilter, songListSearchByTitle,
                                folderSearchVal, artistSearchVal, keySearchVal, tagSearchVal,
                                filterSearchVal, titleSearchVal, songMenuSortTitles);

                        if (newList == null) newList = new ArrayList<>();

                        // 3. Calculate the diff (This is where the Log.d will trigger)
                        SongDiffCallback diffCallback = new SongDiffCallback(oldList, newList);
                        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

                        // NEW: Calculate the index map here on the background thread!
                        LinkedHashMap<String, Integer> newAlphaIndex = songListAdapter.getAlphaIndex(newList);

                        // 4. Update the UI
                        ArrayList<Song> finalNewList = newList;
                        if (mainActivityInterface!=null) {
                            mainActivityInterface.getMainHandler().post(() -> {
                                // Update the underlying data source
                                songMenuSongs.updateSongs(finalNewList);

                                // Tell the adapter to animate the specific changes
                                diffResult.dispatchUpdatesTo(songListAdapter);

                                // Clean up UI
                                // Pass the pre-calculated index map from the adapter
                                displayIndex(newAlphaIndex);
                                updateSongCount();
                                if (myView != null) myView.progressBar.setVisibility(View.GONE);
                                adapterReady = true;
                                buttonsEnabled(true);
                            });
                        }

                    } catch (Exception e) {
                        Log.e("SongMenu", "Search failed", e);
                    }
                });

            }
        }
    }

    public void updateCheckForThisSong(Song thisSong) {
        // Call to update something about a specific song
        getSongsFound();
        if (songMenuSongs.getFoundSongs()!=null) {
            int pos = -1;
            for (int i = 0; i < songMenuSongs.getCount(); i++) {
                if (songMenuSongs.getFoundSongs().get(i).getFilename().equals(thisSong.getFilename()) &&
                        songMenuSongs.getFoundSongs().get(i).getFolder().equals(thisSong.getFolder())) {
                    pos = i;
                    break;
                }
            }
            if (pos > -1) {
                // Update the checklist in the adapter
                if (songListAdapter.getItemCount()==0) {
                    prepareSearch();
                    songListAdapter.changeCheckBox(pos);
                } else {
                    songListAdapter.changeCheckBox(pos);
                }
            }
        }
    }

    private void displayIndex(LinkedHashMap<String, Integer> indexMap) {
        if (myView == null || indexMap == null) return;

        boolean isIndexEnabled = mainActivityInterface.getPreferences()
                .getMyPreferenceBoolean("songMenuAlphaIndexShow", true);

        myView.songmenualpha.sideIndexRecyclerView.setVisibility(isIndexEnabled? View.VISIBLE:View.GONE);

        // 1. Ensure the adapter exists
        if (indexAdapter == null) {
            indexAdapter = new IndexAdapter();
        }

        // 2. IMPORTANT: Always ensure the RecyclerView has the adapter attached
        // This fixes the "No adapter attached; skipping layout" warning
        if (myView.songmenualpha.sideIndexRecyclerView.getAdapter() == null) {
            myView.songmenualpha.sideIndexRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            myView.songmenualpha.sideIndexRecyclerView.setAdapter(indexAdapter);
        }

        // 3. Update the data
        // Add the missing boolean preference for Level 2 here as well
        boolean level2Pref = mainActivityInterface.getPreferences()
                .getMyPreferenceBoolean("songMenuAlphaIndexLevel2", false);

        indexAdapter.setIndexTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuAlphaIndexSize",14f));
        indexAdapter.setData(indexMap, (text, position, isIndexSelfScroll) -> {
            if (isIndexSelfScroll) {
                // SCROLL THE INDEX BAR
                myView.songmenualpha.sideIndexRecyclerView.post(() -> {
                    LinearLayoutManager lm = (LinearLayoutManager) myView.songmenualpha.sideIndexRecyclerView.getLayoutManager();
                    if (lm != null) {
                        lm.scrollToPositionWithOffset(position, 0);
                    }
                });
            } else {
                // SCROLL THE MAIN SONG LIST
                if (position != -1 && myView.songListRecyclerView.getLayoutManager()!=null) {
                    ((LinearLayoutManager) myView.songListRecyclerView.getLayoutManager())
                            .scrollToPositionWithOffset(position, 0);
                }
            }
        }, level2Pref); // Don't forget to pass the boolean to your new setData method
    }

    public void changeAlphabeticalLayout() {
        // We have asked for the visibility or the font size to change
        boolean isIndexEnabled = mainActivityInterface.getPreferences()
                .getMyPreferenceBoolean("songMenuAlphaIndexShow", true);

        if (myView != null) {
            // Toggle the entire sidebar container
            myView.songmenualpha.sideIndexRecyclerView.setVisibility(
                    isIndexEnabled ? View.VISIBLE : View.GONE
            );

            // If it's enabled, make sure the data is fresh
            if (isIndexEnabled) {
                displayIndex(songListAdapter.getAlphaIndex(songMenuSongs.getFoundSongs()));
            }
        }
    }

    @Override
    public void onItemClicked(int position, String folder, String filename, String key, boolean inSet) {
        myView.songListRecyclerView.stopScroll();
        mainActivityInterface.getWindowFlags().hideKeyboard();
        // Default the slide animations to be next (R2L)
        mainActivityInterface.getDisplayPrevNext().setSwipeDirection("R2L");
        if (inSet) {
            mainActivityInterface.loadSongFromSet(mainActivityInterface.getCurrentSet().getIndexSongInSet());
        } else {
            mainActivityInterface.doSongLoad(folder, filename, true);
        }
        // Collapse any level 2 alphabetical index
        if (indexAdapter != null) {
            indexAdapter.collapseAll();
        }
    }

    @Override
    public void onItemLongClicked(int position, String folder, String filename, String key) {
        myView.songListRecyclerView.stopScroll();
        longClickFilename = filename;
        mainActivityInterface.getWindowFlags().hideKeyboard();
        mainActivityInterface.doSongLoad(folder, filename,false);
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


    public void moveToSongInMenu(Song song) {
        // scroll to the song in the song menu
        try {
            if (mainActivityInterface!=null) {
                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    if (mainActivityInterface != null && songListLayoutManager != null) {
                        mainActivityInterface.getMainHandler().post(() -> {
                            try {
                                if (songListAdapter.getItemCount() > songListAdapter.getPositionOfSong(song)) {
                                    int position = songListAdapter.getPositionOfSong(song);
                                    if (position == -1) {
                                        position = 0;
                                    }
                                    songListLayoutManager.scrollToPositionWithOffset(position, 0);
                                    // IV - Reset to a 1 char alphabetic index
                                    displayIndex(songListAdapter.getAlphaIndex(songMenuSongs.getFoundSongs()));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                });
            }
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
        if (songMenuSongs!=null && songMenuSongs.getFoundSongs()==null) {
            try {
                ArrayList<Song> tempSongsFound = mainActivityInterface.getSQLiteHelper().getSongsByFilters(
                        songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
                        songListSearchByTag, songListSearchByFilter, songListSearchByTitle,
                        folderSearchVal, artistSearchVal, keySearchVal, tagSearchVal,
                        filterSearchVal, titleSearchVal, songMenuSortTitles);
                songMenuSongs.updateSongs(tempSongsFound);

            } catch (Exception e) {
                ArrayList<Song> tempSongsFound = new ArrayList<>();
                songMenuSongs.updateSongs(tempSongsFound);
                e.printStackTrace();
            }
        }
        if (songMenuSongs!=null) {
            updateSongCount();

            return songMenuSongs.getFoundSongs();
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<Song> getSongs() {
        return songMenuSongs.getFoundSongs();
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
                    setSongListSearchByFolderValue(folderSearchVal);
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

    public MyMaterialSimpleTextView getProgressText() {
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

    private void updateSongCount() {
        if (myView!=null) {
            myView.songTitleStuff.getRoot().post(() -> {
                if (myView!=null) {
                    myView.songTitleStuff.setCheckTitle.setTextColor(mainActivityInterface.getPalette().textColor);
                    myView.songTitleStuff.songCount.setTextColor(mainActivityInterface.getPalette().textColor);
                    myView.songTitleStuff.songtitleTitle.setTextColor(mainActivityInterface.getPalette().textColor);
                }
            });
            myView.songTitleStuff.songCount.post(() -> {
                if (myView!=null) {
                    myView.songTitleStuff.songCount.setVisibility(View.GONE);
                    if (songMenuSongs.getFoundSongs() != null) {
                        myView.songTitleStuff.songCount.setVisibility(View.VISIBLE);
                        String count = String.valueOf(songMenuSongs.getCount());
                        myView.songTitleStuff.songCount.setTextColor(mainActivityInterface.getPalette().textColor);
                        myView.songTitleStuff.songCount.setText(count);
                        if (getContext()!=null) {
                            Drawable songCountBlob = AppCompatResources.getDrawable(getContext(), R.drawable.rounded_box);
                            if (songCountBlob!=null) {
                                DrawableCompat.setTint(songCountBlob, mainActivityInterface.getPalette().secondary);
                                myView.songTitleStuff.songCount.setBackgroundDrawable(songCountBlob);
                                int padding = Math.round(8 * getContext().getResources().getDisplayMetrics().density);
                                myView.songTitleStuff.songCount.setPadding(padding,padding,padding,padding);
                            }
                        }
                    }
                }
            });
        }
    }

    private void setSongListSearchByFolder(boolean songListSearchByFolder) {
        this.songListSearchByFolder = songListSearchByFolder;
        if (mainActivityInterface!=null && getContext()!=null) {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songListSearchByFolder", songListSearchByFolder);
        }
    }
    private void getSongListSearchByFolder() {
        if (mainActivityInterface!=null && getContext()!=null) {
            songListSearchByFolder = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songListSearchByFolder", false);
        }
    }

    private void setSongListSearchByFolderValue(String songListSearchByFolderValue) {
        this.songListSearchByFolderValue =  songListSearchByFolderValue;
        mainActivityInterface.getPreferences().setMyPreferenceString("songListSearchByFolderValue",songListSearchByFolderValue);
    }

    private void getSongListSearchByFolderValue() {
        songListSearchByFolderValue = mainActivityInterface.getPreferences().getMyPreferenceString("songListSearchByFolderValue","");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            //myView.getRoot().removeAllViews();
            myView = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAllSongsToSet() {
        songListAdapter.addAllSongsToSet();
    }

    public void updateTheme() {
        // First the filter buttons
        if (myView!=null && mainActivityInterface!=null) {
            myView.filterButtons.searchButtonGroup.setPalette(mainActivityInterface.getPalette());
            fixButtons();
            updateSongCount();
        }
    }
}
