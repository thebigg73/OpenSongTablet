package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.SQLite;
import com.garethevans.church.opensongtablet.SQLiteHelper;
import com.garethevans.church.opensongtablet.StaticVariables;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.animation.SongMenuFAB;
import com.garethevans.church.opensongtablet.databinding.MenuSongsBinding;
import com.garethevans.church.opensongtablet.interfaces.LoadSongInterface;
import com.garethevans.church.opensongtablet.interfaces.ShowCaseInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class SongMenuFragment extends Fragment implements SongListAdapter.AdapterCallback, ShowCaseInterface {

    // The helper classes used
    private Preferences preferences;
    private SQLiteHelper sqLiteHelper;
    private MenuSongsBinding menuSongsBinding;
    private SongMenuFAB songMenuFAB;
    private ShowCase showCase;
    private boolean songButtonActive = true;
    private ExtendedFloatingActionButton editSong, createSong, importSong, exportSong;
    private FloatingActionButton actionButton;
    private ProgressBar progressBar;
    private LinearLayout sideIndex;
    private Button folderButton, artistButton, keyButton, tagButton, filterButton, refreshSearch;
    private TableRow folderRow, artistRow, keyRow, tagRow, filterRow;
    private EditText artistSearch, tagSearch, filterSearch;
    private Spinner folderSearch, keySearch;
    private String folderSearchVal, artistSearchVal, keySearchVal, tagSearchVal, filterSearchVal;
    private boolean songListSearchByFolder, songListSearchByArtist, songListSearchByKey,
            songListSearchByTag, songListSearchByFilter;
    private FastScrollRecyclerView songListRecyclerView;
    private ArrayList<SQLite> songsFound;
    private ArrayAdapter<String> arrayAdapter;
    private SongListAdapter songListAdapter;
    private ArrayList<String> foundFolders;

    private LoadSongInterface loadSongInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        loadSongInterface = (LoadSongInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        menuSongsBinding = MenuSongsBinding.inflate(inflater, container, false);

        // Initialise views
        initialiseViews();

        // Initialise helpers
        initialiseHelpers();

        // Set values
        setValues();

        // Set up the spinners
        setUpSpinners();

        // Set up page buttons
        setListeners();

        // Prepare the song menu
        fixButtons();

        return menuSongsBinding.getRoot();
    }

    private void initialiseViews() {
        actionButton = menuSongsBinding.songMenuButtons.actionFAB;
        createSong = menuSongsBinding.songMenuButtons.createSong;
        editSong = menuSongsBinding.songMenuButtons.editSong;
        importSong = menuSongsBinding.songMenuButtons.importSong;
        exportSong = menuSongsBinding.songMenuButtons.exportSong;
        progressBar = menuSongsBinding.progressBar;
        folderButton = menuSongsBinding.folderButton;
        artistButton = menuSongsBinding.artistButton;
        keyButton = menuSongsBinding.keyButton;
        tagButton = menuSongsBinding.tagButton;
        filterButton = menuSongsBinding.filterButton;
        folderRow = menuSongsBinding.folderRow;
        artistRow = menuSongsBinding.artistRow;
        keyRow = menuSongsBinding.keyRow;
        tagRow = menuSongsBinding.tagRow;
        filterRow = menuSongsBinding.filterRow;
        folderSearch = menuSongsBinding.folderSearch;
        artistSearch = menuSongsBinding.artistSearch;
        keySearch = menuSongsBinding.keySearch;
        tagSearch = menuSongsBinding.tagSearch;
        filterSearch = menuSongsBinding.filterSearch;
        songListRecyclerView = menuSongsBinding.songListRecyclerView;
        sideIndex = menuSongsBinding.songmenualpha.sideIndex;
        refreshSearch = menuSongsBinding.refreshSearch;
        // initialise the recycler view with a blank until we have prepared the data asynchronously
        initialiseRecyclerView();
    }

    private void initialiseRecyclerView() {
        songListRecyclerView.removeAllViews();
        sideIndex.removeAllViews();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        songListRecyclerView.setLayoutManager(llm);
        songListRecyclerView.setHasFixedSize(true);
        songListRecyclerView.setOnClickListener(null);
        List<SQLite> blank = new ArrayList<>();
        songListAdapter = new SongListAdapter(requireActivity(), blank, preferences,SongMenuFragment.this);
        songListRecyclerView.setAdapter(songListAdapter);
    }

    private void initialiseHelpers() {
        songMenuFAB = new SongMenuFAB(actionButton,editSong,createSong,importSong,exportSong);
        songMenuFAB.animateFABButton(requireActivity(),false);
        preferences = new Preferences();
        sqLiteHelper = new SQLiteHelper(getActivity());
        showCase = new ShowCase();
    }

    private void setValues() {
        songListSearchByFolder = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByFolder", true);
        songListSearchByArtist = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByArtist", false);
        songListSearchByKey = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByKey", false);
        songListSearchByTag = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByTag", false);
        songListSearchByFilter = preferences.getMyPreferenceBoolean(getActivity(), "songListSearchByFilter", false);
    }

    private void setUpSpinners() {
        new Thread(() -> {
            foundFolders = sqLiteHelper.getFolders(getActivity());
            arrayAdapter = new ArrayAdapter<>(requireActivity(),R.layout.spinnerlayout_dropdown,foundFolders);
            getActivity().runOnUiThread(() -> {
                folderSearch.setAdapter(arrayAdapter);
                folderSearch.setSelection(foundFolders.indexOf(StaticVariables.whichSongFolder));
                folderSearchVal = StaticVariables.whichSongFolder;
                folderSearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        prepareSearch();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
                ArrayAdapter<CharSequence> keyadapter = ArrayAdapter.createFromResource(requireActivity(),
                        R.array.key_choice, R.layout.spinnerlayout_dropdown);
                keyadapter.setDropDownViewResource(R.layout.spinnerlayout_dropdown);
                keySearch.setAdapter(keyadapter);
                keySearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        prepareSearch();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        }).start();

    }

    private void animateButtons() {
        float rotation = actionButton.getRotation();
        if (rotation==0) {
            songMenuFAB.animateFABButton(requireActivity(),true);
        } else {
            songMenuFAB.animateFABButton(requireActivity(),false);
        }
    }
    private void fixButtons() {
        fixColor(folderButton, songListSearchByFolder);
        fixColor(artistButton, songListSearchByArtist);
        fixColor(keyButton, songListSearchByKey);
        fixColor(tagButton, songListSearchByTag);
        fixColor(filterButton, songListSearchByFilter);
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
            if (songButtonActive) {
                songButtonActive = false;
                Handler h = new Handler();
                h.postDelayed(() -> songButtonActive = true,600);
                animateButtons();
            }
        });
        folderButton.setOnClickListener(v -> {
            songListSearchByFolder = !songListSearchByFolder;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByFolder",songListSearchByFolder);
            fixButtons();
            showHideRows(folderRow,songListSearchByFolder);
        });
        artistButton.setOnClickListener(v -> {
            songListSearchByArtist = !songListSearchByArtist;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByArtist",songListSearchByArtist);
            fixButtons();
            showHideRows(artistRow,songListSearchByArtist);
        });
        keyButton.setOnClickListener(v -> {
            songListSearchByKey = !songListSearchByKey;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByKey",songListSearchByKey);
            fixButtons();
            showHideRows(keyRow,songListSearchByKey);
        });
        tagButton.setOnClickListener(v -> {
            songListSearchByTag = !songListSearchByTag;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByTag",songListSearchByTag);
            fixButtons();
            showHideRows(tagRow,songListSearchByTag);
        });
        filterButton.setOnClickListener(v -> {
            songListSearchByFilter = !songListSearchByFilter;
            preferences.setMyPreferenceBoolean(requireActivity(),"songListSearchByFilter",songListSearchByFilter);
            fixButtons();
            showHideRows(filterRow,songListSearchByFilter);
        });
        refreshSearch.setOnClickListener(v -> prepareSearch());
    }

    private void showHideRows(TableRow tr, boolean show) {
        if (show) {
            tr.setVisibility(View.VISIBLE);
        } else {
            tr.setVisibility(View.GONE);
        }
    }
    // Get the values from the spinners and edit texts for filtering
    private void getSearchVals() {
        folderSearchVal = getSpinnerVal(folderSearch);
        artistSearchVal = getEditTextVal(artistSearch);
        keySearchVal    = getSpinnerVal(keySearch);
        tagSearchVal    = getEditTextVal(tagSearch);
        filterSearchVal = getEditTextVal(filterSearch);
    }
    private String getSpinnerVal(Spinner spinner) {
        if (spinner!=null && spinner.getSelectedItem()!=null) {
            return spinner.getSelectedItem().toString();
        } else {
            return "";
        }
    }
    private String getEditTextVal(EditText editText) {
        if (editText!=null && editText.getText()!=null) {
            return editText.getText().toString();
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
        refreshSearch.setEnabled(enabled);
        actionButton.setEnabled(enabled);
        createSong.setEnabled(enabled);
        editSong.setEnabled(enabled);
        importSong.setEnabled(enabled);
        exportSong.setEnabled(enabled);
    }

    private void prepareSearch() {
        new Thread(() -> {
            requireActivity().runOnUiThread(() -> {
                buttonsEnabled(false);
                getSearchVals();
            });

            try {
                songsFound = sqLiteHelper.getSongsByFilters(getActivity(), songListSearchByFolder,
                        songListSearchByArtist, songListSearchByKey, songListSearchByTag,
                        songListSearchByFilter, folderSearchVal, artistSearchVal, keySearchVal,
                        tagSearchVal, filterSearchVal);
            } catch (Exception e) {
                Log.d("SongMenu","No songs found.  Could just be that storage isn't set properly yet");
            }
            requireActivity().runOnUiThread(this::updateSongList);
        }).start();
    }

    private void updateSongList() {
        songListRecyclerView.removeAllViews();
        sideIndex.removeAllViews();
        songListRecyclerView.setOnClickListener(null);
        songListAdapter = new SongListAdapter(requireActivity(), songsFound, preferences, SongMenuFragment.this);
        songListRecyclerView.setAdapter(songListAdapter);
        songListRecyclerView.setFastScrollEnabled(true);
        songListAdapter.notifyDataSetChanged();
        //String t = "" + songsFound.size();
        //counterButtons(true,t);
        // Sort the alphabetical list
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
                        //noinspection ConstantConditions
                        ((LinearLayoutManager)songListRecyclerView.getLayoutManager()).scrollToPositionWithOffset(obj,0);
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
        Log.d("SongMenuFragment","onItemClicked() called, position:"+position);
        loadSongInterface.doSongLoad();
    }

    @Override
    public void onItemLongClicked(int position) {
        Log.d("SongMenuFragment","onItemLongClicked() called");
        actionButton.performClick();
    }

    @Override
    public void runShowCase() {
        Log.d("SongMenuFragment","Running showcase");
        MaterialShowcaseView.resetAll(requireActivity());
        ArrayList<View> views = new ArrayList<>();
        ArrayList<String> infos = new ArrayList<>();
        ArrayList<String> dismisses = new ArrayList<>();
        views.add(menuSongsBinding.searchButtonGroup);
        views.add(menuSongsBinding.songMenuButtons.actionFAB);
        views.add(menuSongsBinding.songListRecyclerView);
        infos.add("Choose filter your song list.  You can filter by folder, artist, key, tag or phrase");
        infos.add("Create, import, export or edit songs using this button");
        infos.add("Click on a song to view it, use the checkbox to add or remove it from your set and long press a song to choose up actions.");
        dismisses.add(null);
        dismisses.add(null);
        dismisses.add(null);
        showCase.sequenceShowCase(requireActivity(),views,dismisses,infos,"songMenu");
    }



   /*
    private void parseCustomSearch() {
        String searchValue = "";

        // This makes the search look nicer for the user

        if (searchFolder && !folderSearch.isEmpty()) {
            searchValue += getString(R.string.folder) + ": '" + folderSearch + "'\n";
        }
        if (searchAuthor && !authorSearch.isEmpty()) {
            searchValue += getString(R.string.edit_song_author) + ": '" + authorSearch + "'\n";
        }
        if (searchKey && !keySearch.isEmpty()) {
            searchValue += getString(R.string.edit_song_key) + ": '" + keySearch + "'\n";
        }
        if (searchTheme && !themeSearch.isEmpty()) {
            searchValue += getString(R.string.edit_song_theme) + ": '" + themeSearch + "'\n";
        }
        if (searchOther && !otherSearch.isEmpty()) {
            searchValue += getString(R.string.edit_song_lyrics) + " / " +
                    getString(R.string.edit_song_user1) + " / " +
                    getString(R.string.edit_song_user2) + " / " +
                    getString(R.string.edit_song_user3) + ": '" + otherSearch + "'\n";
        }

        menuSongsBinding.customFilter.setText(searchValue.trim());
    }
    private String getFilterText() {
        if (menuSongsBinding.seachView.getQuery()!=null) {
            return menuSongsBinding.seachView.getQuery().toString();
        } else {
            return "";
        }
    }

    private void setUpSearchArtistsThread() {
        new Thread(() -> {
            foundArtists = sqLiteHelper.getAuthors(getActivity());
            for (String artist:foundArtists) {
                Log.d("SongMenuFragment", "Artist found:  "+artist);
            }
            arrayAdapter = new ArrayAdapter<>(requireActivity(), R.layout.spinnerlayout_dropdown, foundArtists);
            getActivity().runOnUiThread(() -> {
                arrayAdapter.notifyDataSetInvalidated();
                menuSongsBinding.searchSpinner.setAdapter(null);
                menuSongsBinding.searchSpinner.setAdapter(arrayAdapter);
                if (songListSearchArtist!=null && foundArtists.contains(songListSearchArtist)) {
                    menuSongsBinding.searchSpinner.setSelection(foundArtists.indexOf(songListSearchArtist));
                }
                *//*menuSongsBinding.searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        songListSearchArtist = foundArtists.get(position);
                        preferences.setMyPreferenceString(getActivity(), "songListSearchArtist", songListSearchArtist);
                        buildSongListThread();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });*//*
            });
        }).start();
    }
    private void buildSongListThread() {
        // Show the user we're waiting on something happening
        progressBar.setVisibility(View.VISIBLE);

        // Disable the buttons until we're done
        folderButton.setEnabled(false);
        artistButton.setEnabled(false);
        customButton.setEnabled(false);
        new Thread(() -> {
            // Initialist the lists
            StaticVariables.songsInList = new ArrayList<>(); // This is for swiping left/right
            StaticVariables.currentSet = preferences.getMyPreferenceString(getActivity(), "setCurrent", "");

            switch (preferences.getMyPreferenceString(getActivity(), "songListBy", "folder")) {
                case "folder":
                default:
                    songsFound = sqLiteHelper.getSongsInFolder(getActivity(), songListSearchFolder, getFilterText());
                    break;

                case "artist":
                    songsFound = sqLiteHelper.getSongsByArtist(getActivity(), songListSearchArtist, getFilterText());
                    break;

                case "custom":
                    // User can add multiple searches.  Go through one at a time (split by search term and content
                    songsFound = sqLiteHelper.getSongsByCustom(getActivity(), searchFolder, searchAuthor, searchKey,
                            searchTheme, searchOther, folderSearch, authorSearch, keySearch, themeSearch, otherSearch,getFilterText());
                    break;
            }
            requireActivity().runOnUiThread(() -> {


            });
        }).start();
    }
    private void setFilterButtonColor(int folder, int artist, int custom) {
        menuSongsBinding.folderButton.setBackgroundTintList(ColorStateList.valueOf(folder));
        menuSongsBinding.artistButton.setBackgroundTintList(ColorStateList.valueOf(artist));
        menuSongsBinding.customButton.setBackgroundTintList(ColorStateList.valueOf(custom));
    }
    private void counterButtons(boolean visible, String text) {
        // TODO
        *//*if (!visible) {
            // Hiding, so reset the text as well
            myView.songmenubuttons.folderCount.setText(text);
            myView.songmenubuttons.authorCount.setText(text);
            myView.songmenubuttons.customCount.setText(text);
            setViewVisibility(myView.songmenubuttons.folderCount,visible);
            setViewVisibility(myView.songmenubuttons.authorCount,visible);
            setViewVisibility(myView.songmenubuttons.customCount,visible);
        } else {
            // Only switch on the correct views
            switch (songListBy) {
                case "folder":
                default:
                    myView.songmenubuttons.folderCount.setText(text);
                    setViewVisibility(myView.songmenubuttons.folderCount,visible);
                    break;

                case "artist":
                    myView.songmenubuttons.authorCount.setText(text);
                    setViewVisibility(myView.songmenubuttons.authorCount,visible);
                    break;

                case "custom":
                    myView.songmenubuttons.customCount.setText(text);
                    setViewVisibility(myView.songmenubuttons.customCount,visible);
                    break;
            }
        }*//*
    }

    private void findSongInFolders() {
        //scroll to the song in the song menu
        try {
            indexOfSongInMenu();
            songListRecyclerView.getChildAt(FullscreenActivity.currentSongIndex).setSelected(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void indexOfSongInMenu() {
        int position = StaticVariables.songsInList.indexOf(StaticVariables.songfilename);
        if (position<=0) {
            FullscreenActivity.currentSongIndex = 0;
            FullscreenActivity.previousSongIndex = 0;
        } else {
            FullscreenActivity.currentSongIndex = position;
            FullscreenActivity.previousSongIndex = position-1;
        }
        if (position<StaticVariables.songsInList.size()-1) {
            FullscreenActivity.nextSongIndex = position+1;
        } else {
            FullscreenActivity.nextSongIndex = position;
        }
    }
*/
}
