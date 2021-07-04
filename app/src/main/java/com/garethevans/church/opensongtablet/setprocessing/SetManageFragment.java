package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownSelection;
import com.garethevans.church.opensongtablet.customviews.PrefTextLinkView;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsManageBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class SetManageFragment extends Fragment {

    private SettingsSetsManageBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> allSets, availableSets, categories;
    private ExposedDropDownSelection exposedDropDownSelection;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSetsManageBinding.inflate(inflater, container, false);

        // Set the additional helper
        exposedDropDownSelection = new ExposedDropDownSelection();

        // Get the sets in the folder
        prepareSets();

        // Decide what we are doing
        decideAction();

        return myView.getRoot();
    }

    // Decide what to do with the views depending on what we want to do
    private void decideAction() {
        switch (mainActivityInterface.getWhattodo()) {
            case "loadset":
                changeViews(false);
                break;
            case "saveset":
                changeViews(true);
                break;
        }
    }
    private void changeViews(boolean saving) {
        // Should the edit text box with the set name be shown?
        if (saving) {
            myView.setName.setVisibility(View.VISIBLE);
            ((EditText)myView.setName.findViewById(R.id.editText)).
                    setText(mainActivityInterface.getPreferences().
                            getMyPreferenceString(requireContext(),
                                    "setCurrentLastName", ""));
            myView.newCategory.setVisibility(View.VISIBLE);
            myView.loadorsaveButton.setText(getString(R.string.save));
        } else {
            myView.setName.setVisibility(View.GONE);
            myView.newCategory.setVisibility(View.GONE);
            myView.loadorsaveButton.setText(getString(R.string.load));
        }




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
        ExposedDropDownArrayAdapter categoryAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.exposed_dropdown, categories);
        myView.setCategory.setAdapter(categoryAdapter);
        exposedDropDownSelection.keepSelectionPosition(myView.setCategoryLayout, myView.setCategory,categories);
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
                        "whichSetCategory",s.toString());
                listAvailableSets();
            }
        });
    }
    private void listAvailableSets() {
        myView.setLists.removeAllViews();
        availableSets = mainActivityInterface.getSetActions().setsInCategory(requireContext(),
                mainActivityInterface, allSets);
        for (String setName:availableSets) {
            PrefTextLinkView view = new PrefTextLinkView(requireContext(),null);
            ((TextView)view.findViewById(R.id.mainText)).setText(setName);
            myView.setLists.addView(view);
        }
    }

    private void saveSet() {
        // Get the set category
        String category = myView.setCategory.getText().toString();
        if (category.equals(getString(R.string.mainfoldername))) {
            category = "";
        } else {
            category = category + "___";
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

        }
    }

}