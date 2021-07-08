package com.garethevans.church.opensongtablet.setprocessing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownSelection;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsManageBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class SetManageFragment extends Fragment {

    private static final String TAG = "SetManageFragment";
    private SettingsSetsManageBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> allSets, availableSets, categories;
    private ExposedDropDownArrayAdapter categoriesAdapter;
    private ExposedDropDownSelection exposedDropDownSelection;
    private LayoutInflater inflater;
    private ViewGroup viewGroup;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSetsManageBinding.inflate(inflater, container, false);

        this.inflater = inflater;
        this.viewGroup = container;

        // Set the additional helper
        exposedDropDownSelection = new ExposedDropDownSelection();

        // Get the sets in the folder
        prepareSets();

        // Decide what we are doing
        decideAction();

        // Set listener for category change
        setListener();

        return myView.getRoot();
    }

    // Decide what to do with the views depending on what we want to do
    private void decideAction() {
        switch (mainActivityInterface.getWhattodo()) {
            case "loadset":
                changeViews(false, false);
                break;
            case "saveset":
                changeViews(true, false);
                break;
            case "deleteset":
                changeViews(false, true);
                break;
        }
    }

    private void changeViews(boolean saving, boolean deleting) {
        // Should the edit text box with the set name be shown?
        if (saving) {
            myView.setName.setVisibility(View.VISIBLE);
            myView.overWrite.setVisibility(View.VISIBLE);
            myView.setLoadInfo1.setVisibility(View.GONE);
            myView.setLoadInfo2.setVisibility(View.GONE);
            ((EditText)myView.setName.findViewById(R.id.editText)).
                    setText(mainActivityInterface.getCurrentSet().getSetName());
            myView.newCategory.setVisibility(View.VISIBLE);
            myView.loadorsaveButton.setText(getString(R.string.save));
            myView.loadorsaveButton.setOnClickListener(v -> saveSet());
        } else if (deleting) {

        } else {
            myView.setName.setVisibility(View.GONE);
            myView.overWrite.setVisibility(View.GONE);
            myView.newCategory.setVisibility(View.GONE);
            myView.setLoadInfo1.setVisibility(View.VISIBLE);
            myView.setLoadInfo2.setVisibility(View.VISIBLE);
            myView.loadorsaveButton.setText(getString(R.string.load));
            myView.loadorsaveButton.setOnClickListener(v -> loadSet());
        }
    }

    private void setListener() {
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(
                requireContext(),R.layout.view_exposed_dropdown_item,categories);
        ExposedDropDownSelection exposedDropDownSelection = new ExposedDropDownSelection();
        myView.setCategory.setAdapter(exposedDropDownArrayAdapter);
        exposedDropDownSelection.keepSelectionPosition(myView.setCategory,categories);
        myView.setCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                listAvailableSets();
            }
        });
        myView.newCategory.setOnClickListener(v -> {
            // Open up the Bottomsheet dialog fragment and get the name
            TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(this,
                    "SetManageFragment", getString(R.string.new_category),
                    getString(R.string.new_category),null,null,true);
            textInputBottomSheet.show(requireActivity().getSupportFragmentManager(),"TextInputBottomSheet");


        });


    }

    // Deal with getting the sets in the folder and showing what we want
    private void prepareSets() {
        getAllSets();
        listCategories();
        listAvailableSets();
    }
    private void getAllSets() {
        // Get a list of the files in the Sets folder
        allSets = mainActivityInterface.getSetActions().getAllSets(requireContext(), mainActivityInterface);
    }
    private void listCategories() {
        categories = mainActivityInterface.getSetActions().getCategories(requireContext(), allSets);
        categoriesAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, categories);
        myView.setCategory.setAdapter(categoriesAdapter);
        myView.setCategory.setText(mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "whichSetCategory", requireContext().getString(R.string.mainfoldername)));
        exposedDropDownSelection.keepSelectionPosition(myView.setCategory,categories);
        myView.setCategory.setText(mainActivityInterface.getPreferences().getMyPreferenceString(
                requireContext(), "whichSetCategory", getString(R.string.mainfoldername)));
        myView.setCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),
                        "whichSetCategory", s.toString());
                listAvailableSets();
            }
        });
    }
    private void listAvailableSets() {
        myView.setLists.removeAllViews();
        availableSets = mainActivityInterface.getSetActions().setsInCategory(requireContext(),
                mainActivityInterface, allSets);

        // It will also get MAIN, but it won't matter as it just replaces it
        String bitToRemove = myView.setCategory.getText().toString() + "__";

        for (String setName:availableSets) {
            Log.d(TAG,"bitToRemove"+bitToRemove);
            Log.d(TAG, "setName before: "+setName);
            @SuppressLint("InflateParams") CheckBox checkBox = (CheckBox) LayoutInflater.from(requireContext()).inflate(R.layout.view_checkbox_list_item,null);
            setName = setName.replace(bitToRemove,"");
            Log.d(TAG, "setName after: "+setName);
            checkBox.setText(setName);
            myView.setLists.addView(checkBox);
        }
    }

    private void saveSet() {
        // Get the set category
        String category = myView.setCategory.getText().toString();
        if (category.equals(getString(R.string.mainfoldername))) {
            category = "";
        } else {
            category = category + "__";
        }

        // Get the set name
        String setName = ((EditText)myView.setName.findViewById(R.id.editText)).getText().toString();
        if (setName.isEmpty()) {
            Log.d("SetManageFragment","Bad filename");
        } else {
            // Get a nice name
            setName = mainActivityInterface.getStorageAccess().safeFilename(setName);
            ((EditText)myView.setName.findViewById(R.id.editText)).setText(setName);
            setName = category + setName;

            // If the file already exists and we aren't overwriting, alert the user to rename it
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                    mainActivityInterface, "Sets", "", setName);

            if (mainActivityInterface.getStorageAccess().uriExists(requireContext(),uri) &&
                !myView.overWrite.isChecked()) {
                mainActivityInterface.getShowToast().doIt(requireContext(),
                        getString(R.string.file_exists));
            } else {
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                        requireContext(),mainActivityInterface,uri,null,
                        "Sets","", setName);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().
                        getOutputStream(requireContext(), uri);

                String setXML = mainActivityInterface.getSetActions().createSetXML(
                        requireContext(),mainActivityInterface);
                if (mainActivityInterface.getStorageAccess().writeFileFromString(
                        setXML, outputStream)) {
                    mainActivityInterface.getShowToast().doIt(requireContext(),
                            getString(R.string.success));
                } else {
                    mainActivityInterface.getShowToast().doIt(requireContext(),
                            getString(R.string.error));
                }
            }

        }
    }

    private void loadSet() {
        Log.d(TAG, "loadSet() called");
    }


    // This comes back from the activty after it gets the text from the TextInputBottomSheet dialog
    // This brings in a new category name
    public void updateValue(String which, String value) {
        Log.d(TAG, "value="+value);
        // We will temporarily add this category
        if (!categories.contains(value)) {
            categories.add(value);
            // Sort them (remove main, sort, then readd main to the top)
            categories.remove(getString(R.string.mainfoldername));
            Collections.sort(categories);
            categories.add(0,getString(R.string.mainfoldername));
            categoriesAdapter.notifyDataSetChanged();
        }
        myView.setCategory.setText(value);
    }

}