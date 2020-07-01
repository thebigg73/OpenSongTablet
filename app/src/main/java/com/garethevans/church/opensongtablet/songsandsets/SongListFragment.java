package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.SQLite;
import com.garethevans.church.opensongtablet.SQLiteHelper;
import com.garethevans.church.opensongtablet.StaticVariables;
import com.garethevans.church.opensongtablet.databinding.FragmentSonglistBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SongListFragment extends Fragment {

    // The helper classes used
    private Preferences preferences;
    private SQLiteHelper sqLiteHelper;
    private SongListAdapter songListAdapter;

    // The variable used in this fragment
    private static String whichMode;
    private NavController navController;
    private ArrayList<SQLite> songsFound;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> foundFolders, foundArtists;
    private String songListBy, songListSearchFolder, songListSearchArtist, songListSearchCustom,
            folderSearch, authorSearch, keySearch, themeSearch , otherSearch;
    private boolean searchFolder, searchAuthor, searchKey, searchTheme, searchOther;
    private FragmentSonglistBinding myView;


    // The code to initialise this fragment
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = FragmentSonglistBinding.inflate(inflater, container, false);
        View root = myView.getRoot();


        //navController = NavHostFragment.findNavController(this);

        // initialise helpers
        initialiseHelpers();

        // initialise the recycler view with a blank until we have prepared the data asynchronously
        initialiseRecyclerView();

        // Hide what we don't need
        hideViews();

        // Set values
        setValues();

        // Set the mode we entered this fragment via
        whichMode = preferences.getMyPreferenceString(getActivity(),"whichMode","Performance");
        songListBy = preferences.getMyPreferenceString(getActivity(), "songListBy", "folder");
        songListSearchArtist = preferences.getMyPreferenceString(getActivity(), "songListSearchArtist", "");
        songListSearchCustom = preferences.getMyPreferenceString(getActivity(), "songListSearchCustom", "");

        // Show which action we are taking
        fixButtons();

        // Set listeners
        setUpListeners();

        return root;
    }


    // Finished with this view
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }


    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        preferences = new Preferences();
        sqLiteHelper = new SQLiteHelper(getActivity());
    }
    private void setValues() {
        myView.fragmentheader.pageHeading.setText(R.string.song_list);
        searchFolder = preferences.getMyPreferenceBoolean(getActivity(),"songListSearchCustomFolder",false);
        searchAuthor = preferences.getMyPreferenceBoolean(getActivity(),"songListSearchCustomAuthor",false);
        searchKey = preferences.getMyPreferenceBoolean(getActivity(),"songListSearchCustomKey",false);
        searchTheme = preferences.getMyPreferenceBoolean(getActivity(),"songListSearchCustomTheme",false);
        searchOther = preferences.getMyPreferenceBoolean(getActivity(),"songListSearchCustomOther",false);
        folderSearch = preferences.getMyPreferenceString(getActivity(),"songListCustomFolderVal","");
        authorSearch = preferences.getMyPreferenceString(getActivity(),"songListCustomAuthorVal","");
        keySearch = preferences.getMyPreferenceString(getActivity(),"songListCustomKeyVal","");
        themeSearch = preferences.getMyPreferenceString(getActivity(),"songListCustomThemeVal","");
        otherSearch = preferences.getMyPreferenceString(getActivity(),"songListCustomOtherVal","");
        parseCustomSearch();
    }


    // Sor the view visibility, listeners, etc.
    private void hideViews() {
        myView.fragmentheader.previousHeading.setVisibility(View.GONE);
        myView.fragmentheader.separatorHeading.setVisibility(View.GONE);
    }
    private void searchVis(int spinnerVis, int textVis) {
        myView.songmenubuttons.spinnerRow.setVisibility(spinnerVis);
        myView.songmenubuttons.customRow.setVisibility(textVis);
    }
    private void fixButtons() {
        switch (songListBy) {
            case "folder":
            default:
                searchVis(View.VISIBLE,View.GONE);
                searchButtons(true,false,false);
                setUpSearchFoldersThread();
                break;

            case "artist":
                searchVis(View.VISIBLE,View.GONE);
                searchButtons(false,true,false);
                setUpSearchArtistsThread();
                break;

            case "custom":
                searchVis(View.GONE,View.VISIBLE);
                searchButtons(false,false,true);
                buildSongListThread();
                break;
        }
    }
    private void searchButtons(boolean folderOn, boolean artistOn, boolean customOn) {
        setViewVisibility(myView.songmenubuttons.folderViewOn,folderOn);
        setViewVisibility(myView.songmenubuttons.folderViewOff,!folderOn);
        setViewVisibility(myView.songmenubuttons.artistViewOn,artistOn);
        setViewVisibility(myView.songmenubuttons.artistViewOff,!artistOn);
        setViewVisibility(myView.songmenubuttons.customViewOn,customOn);
        setViewVisibility(myView.songmenubuttons.customViewOff,!customOn);
    }
    private void counterButtons(boolean visible, String text) {
        if (!visible) {
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
        }
    }
    private void setViewVisibility(View v, boolean visibile) {
        if (visibile) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }
    private void initialiseRecyclerView() {
        myView.songListRecyclerView.removeAllViews();
        myView.songmenualpha.sideIndex.removeAllViews();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        myView.songListRecyclerView.setLayoutManager(llm);
        myView.songListRecyclerView.setHasFixedSize(true);
        List<SQLite> blank = new ArrayList<>();
        //songListAdapter = new SongListAdapter(requireActivity(), blank, preferences, mListener);
        myView.songListRecyclerView.setAdapter(songListAdapter);
    }

    private void setUpListeners() {
        /*myView.fragmentheader.fragBackButton.setOnClickListener(v -> {
            preferences.setMyPreferenceString(getActivity(),"songListBy",songListBy);
            preferences.setMyPreferenceString(getActivity(),"songListSearchArtist",songListSearchArtist);
            preferences.setMyPreferenceString(getActivity(),"songListSearchCustom",songListSearchCustom);

            if (StaticVariables.whichMode.equals("Presentation") || StaticVariables.whichMode.equals("Stage")) {
                NavHostFragment.findNavController(SongListFragment.this).navigate(R.id.nav_presentation);
            } else {
                NavHostFragment.findNavController(SongListFragment.this).navigate(R.id.nav_performance);
            }
        });
        myView.songmenubuttons.changeCustom.setOnClickListener(v -> NavHostFragment.findNavController(SongListFragment.this).navigate(R.id.nav_songlist_custom));
        myView.songmenubuttons.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                myView.songmenubuttons.searchEditText.clearFocus();
                if (getActivity()!=null) {
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (in != null) {
                        in.hideSoftInputFromWindow(myView.songmenubuttons.searchEditText.getWindowToken(), 0);
                    }
                }
                buildSongListThread();
                return true;
            }
            return false;
        });
        myView.songmenubuttons.folderViewOn.setOnClickListener(v -> {
            songListBy = "folder";
            preferences.setMyPreferenceString(getActivity(), "songListBy", songListBy);
            searchVis(View.VISIBLE, View.GONE);
            searchButtons(true,false,false);
            setUpSearchFoldersThread();
        });
        myView.songmenubuttons.folderViewOff.setOnClickListener(v -> {
            songListBy = "folder";
            preferences.setMyPreferenceString(getActivity(), "songListBy", songListBy);
            searchVis(View.VISIBLE, View.GONE);
            searchButtons(true,false,false);
            setUpSearchFoldersThread();
        });
        myView.songmenubuttons.artistViewOn.setOnClickListener(v -> {
            songListBy = "artist";
            preferences.setMyPreferenceString(getActivity(), "songListBy", songListBy);
            searchVis(View.VISIBLE, View.GONE);
            searchButtons(false,true,false);
            setUpSearchArtistsThread();
        });
        myView.songmenubuttons.artistViewOff.setOnClickListener(v -> {
            songListBy = "artist";
            preferences.setMyPreferenceString(getActivity(), "songListBy", songListBy);
            searchVis(View.VISIBLE, View.GONE);
            searchButtons(false,true,false);
            setUpSearchArtistsThread();
        });
        myView.songmenubuttons.customViewOn.setOnClickListener(v -> {
            songListBy = "custom";
            preferences.setMyPreferenceString(getActivity(), "songListBy", songListBy);
            searchVis(View.GONE, View.VISIBLE);
            searchButtons(false,false,true);
            buildSongListThread();
        });
        myView.songmenubuttons.customViewOff.setOnClickListener(v -> {
            songListBy = "custom";
            preferences.setMyPreferenceString(getActivity(), "songListBy", songListBy);
            searchVis(View.GONE, View.VISIBLE);
            searchButtons(false,false,true);
            buildSongListThread();
        });
        myView.addSongsButton.setOnClickListener(v -> showPopup(myView.addSongsButton));
        myView.songListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    myView.addSongsButton.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 ||dy<0 && myView.addSongsButton.isShown()) {
                    myView.addSongsButton.hide();
                }
            }
        });*/
    }
    private void setUpSearchFoldersThread() {
        new Thread(() -> {
            foundFolders = sqLiteHelper.getFolders(getActivity());
            arrayAdapter = new ArrayAdapter<>(requireActivity(),R.layout.spinnerlayout_dropdown,foundFolders);
            //arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

            getActivity().runOnUiThread(() -> {
                myView.songmenubuttons.searchSpinner.setAdapter(arrayAdapter);
                myView.songmenubuttons.searchSpinner.setSelection(foundFolders.indexOf(StaticVariables.whichSongFolder));
                myView.songmenubuttons.searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        songListSearchFolder = foundFolders.get(position);
                        buildSongListThread();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            });
        }).start();
    }
    private void setUpSearchArtistsThread() {
        new Thread(() -> {
            foundArtists = sqLiteHelper.getAuthors(getActivity());
            arrayAdapter = new ArrayAdapter<>(requireActivity(),R.layout.spinnerlayout,foundArtists);
            arrayAdapter.setDropDownViewResource(R.layout.spinnerlayout_dropdown);
            getActivity().runOnUiThread(() -> {
                if (arrayAdapter!=null) {
                    myView.songmenubuttons.searchSpinner.setAdapter(arrayAdapter);
                    myView.songmenubuttons.searchSpinner.setSelection(foundArtists.indexOf(songListSearchArtist));
                    myView.songmenubuttons.searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            songListSearchArtist = foundArtists.get(position);
                            preferences.setMyPreferenceString(getActivity(), "songListSearchArtist", songListSearchArtist);
                            buildSongListThread();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }
            });
        }).start();
    }
    private void buildSongListThread() {
        // Show the user we're waiting on something happening
        myView.waitProgressBar.setVisibility(View.VISIBLE);
        counterButtons(false,"");

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
                myView.songListRecyclerView.removeAllViews();
                myView.songmenualpha.sideIndex.removeAllViews();
                //songListAdapter = new SongListAdapter(requireActivity(), songsFound, preferences,mListener);
                myView.songListRecyclerView.setAdapter(songListAdapter);
                songListAdapter.notifyDataSetChanged();
                String t = "" + songsFound.size();
                counterButtons(true,t);
                myView.waitProgressBar.setVisibility(View.GONE);
                // Sort the alphabetical list
                displayIndex(songsFound, songListAdapter);
            });
        }).start();
    }
    private void displayIndex(ArrayList<SQLite> songMenuViewItems,
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
                        //noinspection ConstantConditions
                        ((LinearLayoutManager)myView.songListRecyclerView.getLayoutManager()).scrollToPositionWithOffset(obj,0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            myView.songmenualpha.sideIndex.addView(textView);
        }
    }
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

        myView.songmenubuttons.searchText.setText(searchValue.trim());
    }
    private String getFilterText() {
        if (myView.songmenubuttons.searchEditText.getText()!=null) {
            return myView.songmenubuttons.searchEditText.getText().toString();
        } else {
            return "";
        }
    }

    // Adding more songs button action
    private void showPopup(View v) {
        // Animate the FAB
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.addsongs, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.create:
                default:
                    Log.d("d","Create a new song");
                    return true;
                case R.id.web:
                    Log.d("d","Import from the web");
                    return true;
                case R.id.file:
                    Log.d("d","Import a file");
                    return true;
                case R.id.camera:
                    Log.d("d","Use the camera");
                    return false;
            }
        });
    }

    // When calling load song, go back to the correct start fragment
    public static void loadSong(NavController navCont) {
        if (whichMode.equals("Presentation")) {
            navCont.navigate(R.id.nav_presentation);
        } else {
            navCont.navigate(R.id.nav_performance);
        }
    }

}