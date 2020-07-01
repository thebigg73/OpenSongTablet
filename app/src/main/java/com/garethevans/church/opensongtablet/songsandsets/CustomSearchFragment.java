package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.SQLiteHelper;
import com.garethevans.church.opensongtablet.StaticVariables;
import com.garethevans.church.opensongtablet.StorageAccess;
import com.garethevans.church.opensongtablet.databinding.FragmentSonglistCustomBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class CustomSearchFragment extends Fragment {

    private Preferences preferences;
    private SQLiteHelper sqLiteHelper;

    private ArrayList<String> foundFolders, foundAuthors, foundThemes, foundKeys;
    private ArrayAdapter<String> folderAdapter, authorAdapter, themeAdapter, keyAdapter;
    private String folderSearch, authorSearch, keySearch, themeSearch, otherSearch;
    private boolean searchFolder, searchAuthor, searchKey, searchTheme, searchOther;

    private StorageAccess storageAccess;

    private FragmentSonglistCustomBinding myView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = FragmentSonglistCustomBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        // initialise helpers
        initialiseHelpers();

        // Load up the saved values for searching
        loadPreferences();

        // Set values
        setValues();

        // Set up the spinners, and set the values to saved ones
        setUpControls();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    private void initialiseHelpers() {
        preferences = new Preferences();
        storageAccess = new StorageAccess();
        sqLiteHelper = new SQLiteHelper(getActivity());
    }

    private void setValues() {
        myView.fragmentheader.pageHeading.setText(R.string.custom_search);
        myView.fragmentheader.previousHeading.setText(R.string.song_list);
        String s = getString(R.string.edit_song_lyrics) + " / " +
                getString(R.string.edit_song_user1) + " / " +
                getString(R.string.edit_song_user2) + " / " +
                getString(R.string.edit_song_user3) + " / " +
                getString(R.string.edit_song_hymn);
        myView.otherIncludes.setText(s);
        myView.otherValue.setText(otherSearch);
        changeVisibility(myView.otherValue, true);
    }

    private void changeVisibility(View v, boolean visible) {
        if (visible) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    private void setUpControls() {
        // Do this on a new Thread
        if (getActivity()!=null) {
            changeVisibility(myView.progressBar, true);
            foundFolders = new ArrayList<>();
            foundAuthors = new ArrayList<>();
            foundThemes = new ArrayList<>();
            foundKeys = new ArrayList<>();
            new Thread(() -> {
                // Get the spinner data
                foundFolders = sqLiteHelper.getFolders(getActivity());
                foundAuthors = sqLiteHelper.getAuthors(getActivity());
                foundThemes = sqLiteHelper.getThemes(getActivity(), preferences, storageAccess);
                foundKeys = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.key_choice)));

                // Add blanks to the first row if they aren't already like that
                if (foundFolders.size() > 0 && !foundFolders.get(0).equals("")) {
                    foundFolders.add(0, "");
                }
                if (foundAuthors.size() > 0 && !foundAuthors.get(0).equals("")) {
                    foundAuthors.add(0, "");
                }
                if (foundKeys.size() > 0 && !foundKeys.get(0).equals("")) {
                    foundKeys.add(0, "");
                }
                if (foundThemes.size() > 0 && !foundThemes.get(0).equals("")) {
                    foundThemes.add(0, "");
                }

                folderAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinnerlayout, foundFolders);
                authorAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinnerlayout, foundAuthors);
                keyAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinnerlayout, foundKeys);
                themeAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinnerlayout, foundThemes);
                folderAdapter.setDropDownViewResource(R.layout.spinnerlayout_dropdown);
                authorAdapter.setDropDownViewResource(R.layout.spinnerlayout_dropdown);
                keyAdapter.setDropDownViewResource(R.layout.spinnerlayout_dropdown);
                themeAdapter.setDropDownViewResource(R.layout.spinnerlayout_dropdown);

                // Now back to the UI to set the selected items, listeners and adapters
                getActivity().runOnUiThread(() -> {
                    myView.folderSpinner.setAdapter(folderAdapter);
                    myView.authorSpinner.setAdapter(authorAdapter);
                    myView.keySpinner.setAdapter(keyAdapter);
                    myView.themeSpinner.setAdapter(themeAdapter);

                    setUpListeners();
                    // Look for current saved values and get their position
                    myView.folderSpinner.setSelection(folderAdapter.getPosition(folderSearch));
                    myView.authorSpinner.setSelection(authorAdapter.getPosition(authorSearch));
                    myView.keySpinner.setSelection(keyAdapter.getPosition(keySearch));
                    myView.themeSpinner.setSelection(themeAdapter.getPosition(themeSearch));
                    myView.otherValue.setText(otherSearch);

                    myView.folderSwitch.setChecked(searchFolder);
                    changeVisibility(myView.folderTable,searchFolder);
                    myView.authorSwitch.setChecked(searchAuthor);
                    changeVisibility(myView.authorTable,searchAuthor);
                    myView.keySwitch.setChecked(searchKey);
                    changeVisibility(myView.keyTable,searchKey);
                    myView.themeSwitch.setChecked(searchTheme);
                    changeVisibility(myView.themeTable,searchTheme);
                    myView.otherSwitch.setChecked(searchOther);
                    changeVisibility(myView.otherValue,true);
                    //changeVisibility(myView.otherTable,searchOther);
                    updateSearchText();
                    changeVisibility(myView.progressBar, false);
                    myView.otherValue.getParent().requestLayout();
                    changeVisibility(myView.otherTable,searchOther);
                });
            }).start();
        }
    }

    private void loadPreferences() {
        searchFolder = doLoadPrefBoolean("songListSearchCustomFolder");
        searchAuthor = doLoadPrefBoolean("songListSearchCustomAuthor");
        searchKey = doLoadPrefBoolean("songListSearchCustomKey");
        searchTheme = doLoadPrefBoolean("songListSearchCustomTheme");
        searchOther = doLoadPrefBoolean("songListSearchCustomOther");
        folderSearch = doLoadPrefString("songListCustomFolderVal");
        authorSearch = doLoadPrefString("songListCustomAuthorVal");
        keySearch = doLoadPrefString("songListCustomKeyVal");
        themeSearch = doLoadPrefString("songListCustomThemeVal");
        otherSearch = doLoadPrefString("songListCustomOtherVal");
    }
    private String doLoadPrefString(String prefName) {
        return preferences.getMyPreferenceString(getActivity(),prefName, "");
    }
    private boolean doLoadPrefBoolean(String prefName) {
        return preferences.getMyPreferenceBoolean(getActivity(),prefName, false);
    }
    private void saveSpinnerAndTextValues() {
        // Only called on close rather than every key press!
        if (myView.otherValue.getText()!=null) {
            doSavePrefString("songListCustomOtherVal", myView.otherValue.getText().toString());
        }
        if (myView.folderSpinner.getSelectedItem()!=null) {
            doSavePrefString("songListCustomFolderVal", myView.folderSpinner.getSelectedItem().toString());
        }
        if (myView.authorSpinner.getSelectedItem()!=null) {
            doSavePrefString("songListCustomAuthorVal", myView.authorSpinner.getSelectedItem().toString());
        }
        if (myView.keySpinner.getSelectedItem()!=null) {
            doSavePrefString("songListCustomKeyVal", myView.keySpinner.getSelectedItem().toString());
        }
        if (myView.themeSpinner.getSelectedItem()!=null) {
            doSavePrefString("songListCustomThemeVal", myView.themeSpinner.getSelectedItem().toString());
        }

        doSavePrefBoolean("songListSearchCustomFolder",myView.folderSwitch.isChecked());
        doSavePrefBoolean("songListSearchCustomAuthor",myView.authorSwitch.isChecked());
        doSavePrefBoolean("songListSearchCustomKey",myView.keySwitch.isChecked());
        doSavePrefBoolean("songListSearchCustomTheme",myView.themeSwitch.isChecked());
        doSavePrefBoolean("songListSearchCustomOther",myView.otherSwitch.isChecked());
    }
    private void doSavePrefString(String pref, String value) {
        preferences.setMyPreferenceString(getActivity(), pref,value);
    }
    private void doSavePrefBoolean(String pref, boolean value) {
        preferences.setMyPreferenceBoolean(getActivity(), pref,value);
    }

    private void setUpListeners() {
        myView.fragmentheader.fragBackButton.setOnClickListener(v -> {
            saveSpinnerAndTextValues();
            if (StaticVariables.whichMode.equals("Presentation") || StaticVariables.whichMode.equals("Stage")) {
                NavHostFragment.findNavController(CustomSearchFragment.this).navigate(R.id.nav_presentation);
            } else {
                NavHostFragment.findNavController(CustomSearchFragment.this).navigate(R.id.nav_performance);
            }
        });
        myView.otherValue.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                myView.otherValue.clearFocus();
                if (getActivity()!=null) {
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (in != null) {
                        in.hideSoftInputFromWindow(myView.otherValue.getWindowToken(), 0);
                    }
                }
                updateSearchText();
                return true;
            }
            return false;
        });
        myView.doSearchButton.setOnClickListener(v -> myView.fragmentheader.fragBackButton.performClick());
        // If the switches are turned off, hide the tablelayouts with their details
        myView.folderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateSearchValues(myView.folderTable,isChecked));
        myView.authorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateSearchValues(myView.authorTable,isChecked));
        myView.keySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateSearchValues(myView.keyTable,isChecked));
        myView.themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateSearchValues(myView.themeTable,isChecked));
        myView.otherSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateSearchValues(myView.otherTable,isChecked));
        myView.folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSearchText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        myView.authorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSearchText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        myView.keySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSearchText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        myView.themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSearchText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateSearchValues(View v, boolean isChecked) {
        changeVisibility(v,isChecked);
        updateSearchText();
    }

    private void updateSearchText() {
        String userSearchText = "";

        if (myView.folderSwitch.isChecked() && myView.folderSpinner.getSelectedItem() != null &&
                !myView.folderSpinner.getSelectedItem().equals("")) {
            userSearchText += getString(R.string.folder) + ": '" + myView.folderSpinner.getSelectedItem().toString() + "'\n";
        }
        if (myView.authorSwitch.isChecked() && myView.authorSpinner.getSelectedItem() != null &&
                !myView.authorSpinner.getSelectedItem().equals("")) {
            userSearchText += getString(R.string.edit_song_author) + ": '" + myView.authorSpinner.getSelectedItem().toString() + "'\n";
        }
        if (myView.keySwitch.isChecked() && myView.keySpinner.getSelectedItem() != null &&
                !myView.keySpinner.getSelectedItem().equals("")) {
            userSearchText += getString(R.string.edit_song_key) + ": '" + myView.keySpinner.getSelectedItem().toString() + "'\n";
        }
        if (myView.themeSwitch.isChecked() && myView.themeSpinner.getSelectedItem() != null &&
                !myView.themeSpinner.getSelectedItem().equals("")) {
            userSearchText += getString(R.string.edit_song_theme) + ": '" + myView.themeSpinner.getSelectedItem().toString() + "'\n";
        }
        if (myView.otherSwitch.isChecked() && myView.otherValue.getText() != null &&
                !myView.otherValue.getText().toString().equals("")) {
            userSearchText += getString(R.string.other) + ": '" + myView.otherValue.getText().toString() + "'\n";
        }

        userSearchText = userSearchText.trim();
        myView.currentSearchText.setText(userSearchText);

        folderAdapter.notifyDataSetChanged();
        authorAdapter.notifyDataSetChanged();
        keyAdapter.notifyDataSetChanged();
        themeAdapter.notifyDataSetChanged();
    }

}
