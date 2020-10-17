package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;


//TODO Edit this to allow filtered searching

public class PopUpFullSearchFragment extends DialogFragment {

    private androidx.appcompat.widget.SearchView mSearchView;
    private ListView mListView;
    private ProcessSong processSong;
    private Preferences preferences;
    private SQLiteHelper sqLiteHelper;
    private final String searchPhrase = "";

    // TODO allow user to set these (switch on/off)
    private boolean searchTitle, searchAuthor, searchCopyright, searchLyrics, searchTheme, searchKey,
    searchHymn, searchUser1, searchUser2, searchUser3, searchCCLI, searchFolder, searchAka;

    private View V;
    private SearchViewAdapter adapter;
    private LinearLayout searchFilters, filtersLayout, mainSearchStuff;
    private FloatingActionButton filtersFAB1, filtersFAB2;

    // The array that holds the search information
    private ArrayList<SQLite> searchlist = new ArrayList<>();

    public interface MyInterface {
        void loadSong();
        void songLongClick();
    }

    public static PopUpFullSearchFragment newInstance () {
        PopUpFullSearchFragment frag;
        frag = new PopUpFullSearchFragment();
        return frag;
    }

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        V = inflater.inflate(R.layout.popup_searchview, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.action_search));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        super.onCreate(savedInstanceState);

        processSong = new ProcessSong();
        preferences = new Preferences();
        sqLiteHelper = new SQLiteHelper(getActivity());

        initialiseViews();
        setFABListeners();
        getSearchPreferences();
        setUpSearchView();
        setUpPreferenceViews();
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void initialiseViews() {
        mainSearchStuff = V.findViewById(R.id.mainSearchStuff);
        filtersFAB1 = V.findViewById(R.id.filtersFAB1);
        filtersFAB2 = V.findViewById(R.id.filtersFAB2);
        filtersLayout = V.findViewById(R.id.filtersLayout);
        mSearchView = V.findViewById(R.id.search_view);
        //EditText searchEditText = mSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
        EditText searchEditText = mSearchView.findViewById(R.id.search_src_text);

        int id = mSearchView.getContext().getResources().
                getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText2 = mSearchView.findViewById(id);
        searchFilters = V.findViewById(R.id.searchFilters);
        try {
            if (mSearchView!=null) {
                mSearchView.clearFocus();
            }
            if (searchEditText!=null) {
                searchEditText.clearFocus();
            }
            if (searchEditText2!=null) {
                searchEditText2.clearFocus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (searchEditText!=null) {
            searchEditText.setTextColor(getResources().getColor(R.color.white));
            searchEditText.setHintTextColor(getResources().getColor(R.color.white));
            searchEditText.setFocusable(true);
            searchEditText.requestFocus();
            searchEditText.setSelection(0);

        } else if (searchEditText2!=null) {
            searchEditText2.setTextColor(getResources().getColor(R.color.white));
            searchEditText2.setHintTextColor(getResources().getColor(R.color.white));
            searchEditText2.setFocusable(true);
            searchEditText2.requestFocus();
            searchEditText2.setSelection(0);
        }

        mListView = V.findViewById(R.id.list_view);
        mListView.setTextFilterEnabled(true);
        mListView.setFastScrollEnabled(true);
    }

    private void setFABListeners() {
        filtersFAB1.setOnClickListener(v -> {
            mainSearchStuff.setVisibility(View.GONE);
            filtersLayout.setVisibility(View.VISIBLE);
        });
        filtersFAB2.setOnClickListener(v -> {
            mainSearchStuff.setVisibility(View.VISIBLE);
            filtersLayout.setVisibility(View.GONE);
        });
    }

    private void setUpPreferenceViews() {
        // Create the new checkboxes
        searchFilters.removeAllViews();

        CheckBox titleCheckBox = new CheckBox(getActivity());
        CheckBox authorCheckBox = new CheckBox(getActivity());
        CheckBox copyrightCheckBox = new CheckBox(getActivity());
        CheckBox lyricsCheckBox = new CheckBox(getActivity());
        CheckBox themeCheckBox = new CheckBox(getActivity());
        CheckBox keyCheckBox = new CheckBox(getActivity());
        CheckBox hymnCheckBox = new CheckBox(getActivity());
        CheckBox user1CheckBox = new CheckBox(getActivity());
        CheckBox user2CheckBox = new CheckBox(getActivity());
        CheckBox user3CheckBox = new CheckBox(getActivity());
        CheckBox ccliCheckBox = new CheckBox(getActivity());
        CheckBox folderCheckBox = new CheckBox(getActivity());
        CheckBox akaCheckBox = new CheckBox(getActivity());

        // Set them ticked as appropriate
        titleCheckBox.setChecked(searchTitle);
        authorCheckBox.setChecked(searchAuthor);
        copyrightCheckBox.setChecked(searchCopyright);
        lyricsCheckBox.setChecked(searchLyrics);
        themeCheckBox.setChecked(searchTheme);
        keyCheckBox.setChecked(searchKey);
        hymnCheckBox.setChecked(searchHymn);
        user1CheckBox.setChecked(searchUser1);
        user2CheckBox.setChecked(searchUser2);
        user3CheckBox.setChecked(searchUser3);
        ccliCheckBox.setChecked(searchCCLI);
        folderCheckBox.setChecked(searchFolder);
        akaCheckBox.setChecked(searchAka);

        // Add the text
        titleCheckBox.setText(R.string.edit_song_title);
        authorCheckBox.setText(R.string.edit_song_author);
        copyrightCheckBox.setText(R.string.edit_song_copyright);
        lyricsCheckBox.setText(R.string.edit_song_lyrics);
        themeCheckBox.setText(R.string.edit_song_theme);
        keyCheckBox.setText(R.string.edit_song_key);
        hymnCheckBox.setText(R.string.edit_song_hymn);
        user1CheckBox.setText(R.string.edit_song_user1);
        user2CheckBox.setText(R.string.edit_song_user2);
        user3CheckBox.setText(R.string.edit_song_user3);
        ccliCheckBox.setText(R.string.edit_song_ccli);
        folderCheckBox.setText(R.string.songfolder);
        akaCheckBox.setText(R.string.edit_song_aka);

        // Set the view listeners
        titleCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchTitle",isChecked);
            searchTitle = isChecked;
        });
        authorCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchAuthor",isChecked);
            searchAuthor = isChecked;
        });
        copyrightCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchCopyright",isChecked);
            searchCopyright = isChecked;
        });
        lyricsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchLyrics",isChecked);
            searchLyrics = isChecked;
        });
        themeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchTheme",isChecked);
            searchTheme = isChecked;
        });
        keyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchKey",isChecked);
            searchKey = isChecked;
        });
        hymnCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchHymn",isChecked);
            searchHymn = isChecked;
        });
        user1CheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchUser1",isChecked);
            searchUser1 = isChecked;
        });
        user2CheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchUser2",isChecked);
            searchUser2 = isChecked;
        });
        user3CheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchUser3",isChecked);
            searchUser3 = isChecked;
        });
        ccliCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchCCLI",isChecked);
            searchCCLI = isChecked;
        });
        folderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchFolder",isChecked);
            searchFolder = isChecked;
        });
        akaCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"searchAka",isChecked);
            searchAka = isChecked;
        });

        // Add the views to the linearlayout
        searchFilters.addView(titleCheckBox);
        searchFilters.addView(authorCheckBox);
        searchFilters.addView(copyrightCheckBox);
        searchFilters.addView(lyricsCheckBox);
        searchFilters.addView(themeCheckBox);
        searchFilters.addView(keyCheckBox);
        searchFilters.addView(hymnCheckBox);
        searchFilters.addView(user1CheckBox);
        searchFilters.addView(user2CheckBox);
        searchFilters.addView(user3CheckBox);
        searchFilters.addView(ccliCheckBox);
        searchFilters.addView(folderCheckBox);
        searchFilters.addView(akaCheckBox);

    }

    private String addIfWanted(boolean wantit, String string) {
        if (wantit && string!=null) {
            return " " + string;
        } else {
            return "";
        }
    }

    private void getSearchPreferences() {
        searchTitle = preferences.getMyPreferenceBoolean(getActivity(),"searchTitle",true);
        searchAuthor = preferences.getMyPreferenceBoolean(getActivity(),"searchAuthor",true);
        searchCopyright = preferences.getMyPreferenceBoolean(getActivity(),"searchCopyright",true);
        searchLyrics = preferences.getMyPreferenceBoolean(getActivity(),"searchLyrics",true);
        searchTheme = preferences.getMyPreferenceBoolean(getActivity(),"searchTheme",true);
        searchKey = preferences.getMyPreferenceBoolean(getActivity(),"searchKey",true);
        searchHymn = preferences.getMyPreferenceBoolean(getActivity(),"searchHymn",true);
        searchUser1 = preferences.getMyPreferenceBoolean(getActivity(),"searchUser1",true);
        searchUser2 = preferences.getMyPreferenceBoolean(getActivity(),"searchUser2",true);
        searchUser3 = preferences.getMyPreferenceBoolean(getActivity(),"searchUser3",true);
        searchFolder = preferences.getMyPreferenceBoolean(getActivity(),"searchFolder",true);
        searchAka = preferences.getMyPreferenceBoolean(getActivity(),"searchAka",true);
        searchCCLI = preferences.getMyPreferenceBoolean(getActivity(),"searchCCLI",true);
    }

    private void setUpSearchView() {
        new Thread(() -> {
            try {
                searchlist = sqLiteHelper.getAllSongs(getActivity());
                Log.d("FullSearchFragment","size of searchlist="+searchlist.size());
                // Add the relevant stuff
                for (int i=0;i<searchlist.size();i++) {
                    String searchableContent = "";
                    // Don't add folders
                    if (searchlist.get(i).getFilename() != null && !searchlist.get(i).getFilename().isEmpty() &&
                            !searchlist.get(i).getFilename().equals(" ")) {
                        // If the title is empty, use the filename
                        if (searchlist.get(i).getTitle()==null || searchlist.get(i).getTitle().isEmpty()) {
                            searchlist.get(i).setTitle(searchlist.get(i).getFilename());
                        }

                        Log.d("FullSearchFragment", "getFilename()="+searchlist.get(i).getFilename());
                        searchableContent = searchableContent + searchlist.get(i).getFilename();
                        searchableContent = searchableContent + addIfWanted(searchFolder, searchlist.get(i).getFilename());
                        searchableContent = searchableContent + addIfWanted(searchAka, searchlist.get(i).getAka());
                        searchableContent = searchableContent + addIfWanted(searchAuthor, searchlist.get(i).getAuthor());
                        searchableContent = searchableContent + addIfWanted(searchCCLI, searchlist.get(i).getCcli());
                        searchableContent = searchableContent + addIfWanted(searchCopyright, searchlist.get(i).getCopyright());
                        searchableContent = searchableContent + addIfWanted(searchHymn, searchlist.get(i).getHymn_num());
                        searchableContent = searchableContent + addIfWanted(searchKey, getString(R.string.edit_song_key) + " " + searchlist.get(i).getKey());
                        searchableContent = searchableContent + addIfWanted(searchLyrics, searchlist.get(i).getLyrics());
                        searchableContent = searchableContent + addIfWanted(searchTheme, searchlist.get(i).getTheme() + " " + searchlist.get(i).getAlttheme());
                        searchableContent = searchableContent + addIfWanted(searchTitle, searchlist.get(i).getTitle());
                        searchableContent = searchableContent + addIfWanted(searchUser1, searchlist.get(i).getUser1());
                        searchableContent = searchableContent + addIfWanted(searchUser2, searchlist.get(i).getUser2());
                        searchableContent = searchableContent + addIfWanted(searchUser3, searchlist.get(i).getUser3());
                        searchlist.get(i).setLyrics(searchableContent);
                    }
                }

                adapter = new SearchViewAdapter(getActivity(), searchlist, searchPhrase);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        mListView.setAdapter(adapter);
                        mListView.setTextFilterEnabled(true);
                        mListView.setFastScrollEnabled(true);
                        mSearchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String s) {
                                InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                                }
                                mListView.requestFocus();
                                if (mListView.getCount() == 0) {
                                    dismiss();
                                    return false;

                                } else {
                                    SQLite item = (SQLite) adapter.getItem(0);
                                    StaticVariables.songfilename = item.getFilename();
                                    StaticVariables.whichSongFolder = item.getFolder();
                                    StaticVariables.setView = false;
                                    StaticVariables.myToastMessage = StaticVariables.songfilename;

                                    // Vibrate to indicate something has happened
                                    DoVibrate.vibrate(getActivity(), 50);
                                    if (mListener != null) {
                                        mListener.songLongClick();
                                        mListener.loadSong();
                                    }
                                    dismiss();
                                    return true;
                                }
                            }

                            @Override
                            public boolean onQueryTextChange(String s) {
                                // Replace unwanted symbols
                                if (s == null) {
                                    s = "";
                                }
                                s = processSong.removeUnwantedSymbolsAndSpaces(getActivity(), preferences, s);
                                if (adapter != null) {
                                    adapter.getFilter().filter(s);
                                }
                                return false;
                            }
                        });
                        mListView.setOnItemClickListener(new SongClickListener());
                        mListView.setOnItemLongClickListener(new SongLongClickListener());
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private class SongClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Vibrate to indicate that something has happened.
            DoVibrate.vibrate(Objects.requireNonNull(getActivity()),50);

            TextView mFilename = view.findViewById(R.id.cardview_filename);
            TextView mFoldername = view.findViewById(R.id.cardview_folder);
            StaticVariables.songfilename = mFilename.getText().toString();
            StaticVariables.whichSongFolder = mFoldername.getText().toString();
            StaticVariables.setView = false;

            // Hide the keyboard if it is visible
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            if (mListener!=null) {
                mListener.loadSong();
            }
            dismiss();
        }
    }

    // This listener listens for long clicks in the song menu
    private class SongLongClickListener implements
            ListView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // Each song is saved in the set string as $**_Love everlasting_**$
            // Vibrate to indicate that something has happened.
            DoVibrate.vibrate(Objects.requireNonNull(getActivity()),50);

            TextView mFilename = view.findViewById(R.id.cardview_filename);
            TextView mFoldername = view.findViewById(R.id.cardview_folder);
            String tsong = mFilename.getText().toString();
            String tfolder = mFoldername.getText().toString();

            // We need to figure out the file name and the folder (if any) it is in
            if (tfolder.equals(getActivity().getString(R.string.mainfoldername)) || tfolder.equals("MAIN") || tfolder.equals("")) {
                StaticVariables.whatsongforsetwork = "$**_" + tsong + "_**$";
            } else {
                StaticVariables.whatsongforsetwork = "$**_" + tfolder + "/"	+ tsong + "_**$";
            }

            // Allow the song to be added, even if it is already there
            String val = preferences.getMyPreferenceString(getActivity(),"setCurrent","") + StaticVariables.whatsongforsetwork;
            preferences.setMyPreferenceString(getActivity(),"setCurrent",val);
            // Tell the user that the song has been added.
            StaticVariables.myToastMessage = "\"" + tsong + "\" " +getResources().getString(R.string.addedtoset);
            ShowToast.showToast(getActivity());

            if (mListener!=null) {
                mListener.songLongClick();
            }
            return true;
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}
