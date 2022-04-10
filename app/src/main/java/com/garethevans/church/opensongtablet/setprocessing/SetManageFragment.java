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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsManageBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class SetManageFragment extends Fragment {

    private static final String TAG = "SetManageFragment";
    private SettingsSetsManageBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> allSets;
    private ArrayList<String> categories;
    private ExposedDropDownArrayAdapter categoriesAdapter;
    private String chosenSets = "";
    private String whattodo;
    private String renameSetName;
    private String renameSetCategory;
    private Uri oldSetUri, newSetUri;
    private String newSetFilename;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSetsManageBinding.inflate(inflater, container, false);

        whattodo = mainActivityInterface.getWhattodo();

        // Check if we want to load a specific file
        checkForLoadSpecific();

        // Get the sets in the folder
        prepareSets();

        // Decide what we are doing
        changeViews();

        // Set listener for category change
        setListener();

        return myView.getRoot();
    }

    private void checkForLoadSpecific() {
        if (mainActivityInterface.getWhattodo().startsWith("loadset:")) {
            Log.d(TAG, "trying to load a set automatically");
            String lookFor = mainActivityInterface.getWhattodo().replace("loadset:", "");
            chosenSets = chosenSets + "%_%" + lookFor + "%_%";
        }
    }

    // Decide what to do with the views depending on what we want to do
    private void changeViews() {
        if (whattodo.startsWith("exportset")) {
            whattodo = "exportset";
        }
        if (whattodo.startsWith("loadset")) {
            whattodo = "loadset";
        }
        if (whattodo.startsWith("renameset")) {
            whattodo = "renameset";
        }

        // Get the current set
        String category = getString(R.string.mainfoldername);
        String setname = mainActivityInterface.getCurrentSet().getSetName();
        if (mainActivityInterface.getCurrentSet().getSetName().contains("__")) {
            String[] bits = mainActivityInterface.getCurrentSet().getSetName().split("__");
            if (bits.length > 0) {
                category = bits[0];
                setname = bits[1];
            }
        }
        if (setname.equals(getString(R.string.set_current))) {
            setname = "";
        }

        // Decide on the views required
        switch (whattodo) {
            case "saveset":
                mainActivityInterface.updateToolbar(getString(R.string.set) + ": " + getString(R.string.save));
                myView.setName.setVisibility(View.VISIBLE);
                myView.overWrite.setVisibility(View.VISIBLE);
                myView.setLoadInfo1.setVisibility(View.GONE);
                myView.setLoadInfo2.setVisibility(View.GONE);
                myView.setName.setText(setname);
                myView.newCategory.setVisibility(View.VISIBLE);
                myView.setCategory.setText(category);
                myView.loadorsaveButton.setText(getString(R.string.save));
                myView.loadorsaveButton.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_content_save_white_36dp));
                myView.loadorsaveButton.setOnClickListener(v -> saveSet());
                break;

            case "renameset":
                mainActivityInterface.updateToolbar(getString(R.string.set) + ": " + getString(R.string.rename));
                myView.setName.setVisibility(View.VISIBLE);
                myView.overWrite.setVisibility(View.VISIBLE);
                myView.setLoadInfo1.setVisibility(View.GONE);
                myView.setLoadInfo2.setVisibility(View.GONE);
                myView.setName.setText(setname);
                myView.newCategory.setVisibility(View.VISIBLE);
                myView.setCategory.setText(category);
                myView.loadorsaveButton.setText(getString(R.string.rename));
                myView.loadorsaveButton.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_content_save_white_36dp));
                myView.loadorsaveButton.setOnClickListener(v -> renameSet());
                break;

            case "deleteset":
                mainActivityInterface.updateToolbar(getString(R.string.set) + ": " + getString(R.string.delete));
                myView.setName.setVisibility(View.GONE);
                myView.overWrite.setVisibility(View.GONE);
                myView.setLoadInfo1.setVisibility(View.VISIBLE);
                myView.setLoadInfo2.setVisibility(View.GONE);
                myView.newCategory.setVisibility(View.GONE);
                myView.setCategory.setVisibility(View.VISIBLE);
                myView.loadorsaveButton.setText(getString(R.string.delete));
                myView.loadorsaveButton.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_white_36dp));
                myView.loadorsaveButton.setOnClickListener(v -> deleteSet());
                break;

            case "exportset":
                mainActivityInterface.updateToolbar(getString(R.string.set) + ": " + getString(R.string.export));
                myView.setName.setVisibility(View.GONE);
                myView.overWrite.setVisibility(View.GONE);
                myView.newCategory.setVisibility(View.GONE);
                myView.setLoadInfo1.setVisibility(View.VISIBLE);
                myView.setLoadInfo2.setVisibility(View.VISIBLE);
                myView.setLoadInfo2.setText(getString(R.string.set_saved_not_current));
                myView.loadorsaveButton.setText(getString(R.string.export));
                myView.loadorsaveButton.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_share_variant_white_36dp));
                myView.loadorsaveButton.setOnClickListener(v -> exportSet());
                break;

            case "loadset":
            default:
                mainActivityInterface.updateToolbar(getString(R.string.set) + ": " + getString(R.string.load));
                myView.setName.setVisibility(View.GONE);
                myView.overWrite.setVisibility(View.GONE);
                myView.newCategory.setVisibility(View.GONE);
                myView.setLoadInfo1.setVisibility(View.VISIBLE);
                myView.setLoadInfo2.setVisibility(View.VISIBLE);
                myView.loadorsaveButton.setText(getString(R.string.load));
                myView.loadorsaveButton.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_content_save_white_36dp));
                myView.loadorsaveButton.setOnClickListener(v -> loadSet());
                break;
        }
    }

    private void setListener() {
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(
                requireContext(), myView.setCategory, R.layout.view_exposed_dropdown_item, categories);
        myView.setCategory.setAdapter(exposedDropDownArrayAdapter);
        myView.setCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                listAvailableSets();
            }
        });
        myView.newCategory.setOnClickListener(v -> {
            // Open up the Bottomsheet dialog fragment and get the name
            TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(this,
                    "SetManageFragment", getString(R.string.new_category),
                    getString(R.string.new_category), null, null, null, true);
            textInputBottomSheet.show(requireActivity().getSupportFragmentManager(), "TextInputBottomSheet");
        });

        myView.nestedScrollView.setExtendedFabToAnimate(myView.loadorsaveButton);
    }

    // Deal with getting the sets in the folder and showing what we want
    private void prepareSets() {
        getAllSets();
        listCategories();
        listAvailableSets();
    }

    private void getAllSets() {
        // Get a list of the files in the Sets folder
        allSets = mainActivityInterface.getSetActions().getAllSets();
    }

    private void listCategories() {
        categories = mainActivityInterface.getSetActions().getCategories(allSets);
        categoriesAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.setCategory,
                R.layout.view_exposed_dropdown_item, categories);
        myView.setCategory.setAdapter(categoriesAdapter);
        myView.setCategory.setText(mainActivityInterface.getPreferences().getMyPreferenceString(
                "whichSetCategory", requireContext().getString(R.string.mainfoldername)));
        myView.setCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mainActivityInterface.getPreferences().setMyPreferenceString(
                        "whichSetCategory", s.toString());
                listAvailableSets();
            }
        });
    }

    private void listAvailableSets() {
        myView.setLists.removeAllViews();
        ArrayList<String> availableSets;
        if (whattodo.equals("renameset")) {
            availableSets = mainActivityInterface.getSetActions().listSetsWithCategories(allSets);
        } else {
            availableSets = mainActivityInterface.getSetActions().setsInCategory(allSets);
        }

        // It will also get MAIN, but it won't matter as it just replaces it
        String bitToRemove = myView.setCategory.getText().toString() + "__";

        for (String setName : availableSets) {
            @SuppressLint("InflateParams") CheckBox checkBox = (CheckBox) LayoutInflater.from(requireContext()).inflate(R.layout.view_checkbox_list_item, null);
            checkBox.setTag(setName.replace("__","/"));
            setName = setName.replace(bitToRemove, "");
            checkBox.setText(setName);
            if (whattodo.equals("saveset") || whattodo.equals("renameset")) {
                checkBox.setButtonDrawable(null);
                checkBox.setAlpha(0.6f);
            } else {
                String toFind = bitToRemove + setName;
                toFind = toFind.replace(getString(R.string.mainfoldername) + "__", "");
                checkBox.setChecked(chosenSets.contains("%_%" + toFind + "%_%"));
                Log.d(TAG, "Looking for " + "%_%" + toFind + "%_%" + "   in chosenSets:" + chosenSets);
            }
            String setCategory = myView.setCategory.getText().toString();
            String finalSetName;
            if (setCategory.equals(getString(R.string.mainfoldername))) {
                finalSetName = setName;
            } else {
                finalSetName = setCategory + "__" + setName;
            }
            if (!whattodo.equals("renameset") && !whattodo.equals("saveset")) {
                checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (!whattodo.equals("renameset")) {
                        if (b && !chosenSets.contains("%_%" + finalSetName + "%_%")) {
                            chosenSets = chosenSets + "%_%" + finalSetName + "%_%";
                        } else if (!b) {
                            chosenSets = chosenSets.replace("%_%" + finalSetName + "%_%", "");
                        }
                    }
                });
            }
            if (whattodo.equals("renameset") || whattodo.equals("saveset")) {
                checkBox.setAlpha(0.6f);
                checkBox.setOnClickListener(view -> {
                    String text = view.getTag().toString();
                    if (text.contains("/")) {
                        String[] bits = text.split("/");
                        if (bits.length>1) {
                            renameSetCategory = bits[0];
                            renameSetName = bits[1];
                            newSetFilename = renameSetCategory + "__" + renameSetName;
                        }
                    } else {
                        renameSetCategory = getString(R.string.mainfoldername);
                        renameSetName = text;
                        newSetFilename = text;
                    }
                    myView.setName.setText(renameSetName);
                    myView.setCategory.setText(renameSetCategory);
                    updateCheckList(text);
                });
            }
            myView.setLists.addView(checkBox);
        }
    }

    private void updateCheckList(String tag) {
        // Go through the checkbox list and if the tag matches, set full alpha, otherwise dim it
        for (int x=0; x<myView.setLists.getChildCount(); x++) {
            CheckBox cb = ((CheckBox)myView.setLists.getChildAt(x));
            if (cb.getTag().equals(tag)) {
                cb.setAlpha(1f);
            } else {
                cb.setAlpha(0.6f);
            }
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
        String setName = myView.setName.getText().toString();
        if (!setName.isEmpty()) {
            // Get a nice name
            setName = mainActivityInterface.getStorageAccess().safeFilename(setName);
            myView.setName.setText(setName);
            setName = category + setName;

            // If the file already exists and we aren't overwriting, alert the user to rename it
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", setName);

            if (mainActivityInterface.getStorageAccess().uriExists(uri) &&
                    !myView.overWrite.isChecked()) {
                mainActivityInterface.getShowToast().doIt(
                        getString(R.string.file_exists));
            } else {
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null,
                        "Sets", "", setName);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().
                        getOutputStream(uri);

                String setXML = mainActivityInterface.getSetActions().createSetXML();
                if (mainActivityInterface.getStorageAccess().writeFileFromString(
                        setXML, outputStream)) {
                    // Update the last loaded set now it is saved.
                    mainActivityInterface.getPreferences().setMyPreferenceString(
                            "setCurrentBeforeEdits",
                            mainActivityInterface.getCurrentSet().getCurrentSetString());
                    mainActivityInterface.getShowToast().doIt(getString(R.string.set_current) + " - " +
                            getString(R.string.success));
                } else {
                    mainActivityInterface.getShowToast().doIt(getString(R.string.error));
                }
            }
        }
        prepareSets();
    }

    private void deleteSet() {
        // Show the progressBar
        myView.progressBar.setVisibility(View.VISIBLE);

        // Split the sets chosen up into individual sets and get their uris
        boolean success = true;
        String[] setBits = chosenSets.split("%_%");
        for (String setBit : setBits) {
            if (setBit != null && !setBit.isEmpty()) {
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", setBit);
                if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
                    // Try deleting the set file
                    if (!mainActivityInterface.getStorageAccess().deleteFile(uri)) {
                        success = false;
                    }
                }
            }
        }
        if (success) {
            mainActivityInterface.getShowToast().doIt(getString(R.string.success));
        } else {
            mainActivityInterface.getShowToast().doIt(getString(R.string.error));
        }

        // Hide the progress bar
        myView.progressBar.setVisibility(View.GONE);

        prepareSets();
    }

    private void renameSet() {
        String oldSetText;
        String oldSetFilename;
        if (renameSetCategory == null || renameSetCategory.isEmpty() || renameSetCategory.equals(getString(R.string.mainfoldername))) {
            oldSetFilename = renameSetName;
            oldSetText = getString(R.string.mainfoldername) + "/" + renameSetName;
        } else {
            oldSetFilename = renameSetCategory + "__" + renameSetName;
            oldSetText = renameSetCategory + "/" + renameSetName;
        }
        oldSetUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets","", oldSetFilename);

        Editable mycat = myView.setCategory.getText();
        Editable mynam = myView.setName.getText();
        if (mycat!=null && !mycat.toString().isEmpty() && mynam!=null && !mynam.toString().isEmpty()) {
            String newSetText;
            if (mycat.toString().isEmpty() || mycat.toString().equals(getString(R.string.mainfoldername))) {
                newSetFilename = mynam.toString();
                newSetText = getString(R.string.mainfoldername) + "/" + mynam;
            } else {
                newSetFilename = mycat + "__" + mynam;
                newSetText = mycat + "/" + mynam;
            }
            newSetUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets","",newSetFilename);

            boolean exists = mainActivityInterface.getStorageAccess().uriExists(newSetUri);
            if (exists && !myView.overWrite.isChecked()) {
                mainActivityInterface.getShowToast().doIt(getString(R.string.file_exists));
            } else {
                AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("renameSet",
                        getString(R.string.rename)+":\n"+ oldSetText +" > "+ newSetText,null,
                        "setManageFragment", this, null);
                areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"AreYouSureFragment");
            }

        }
    }

    private void exportSet() {
        // Only allow if indexing is complete
        if (mainActivityInterface.getSongListBuildIndex().getIndexComplete()) {
            // Set the "whattodo" to let the export fragment know we are exporting a set
            mainActivityInterface.setWhattodo("exportset:" + chosenSets);
            mainActivityInterface.navigateToFragment("opensongapp://settings/actions/export", 0);
        } else {
            mainActivityInterface.getShowToast().doIt(getString(R.string.search_index_wait));
        }
    }

    private void loadSet() {
        // Show the progressBar
        myView.progressBar.setVisibility(View.VISIBLE);
        // Initialise the current set
        mainActivityInterface.getCurrentSet().initialiseTheSet();
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent", "");
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrentBeforeEdits", "");

        // Because we can import multiple sets, we need to get them into an array
        ArrayList<Uri> setUris = new ArrayList<>();
        StringBuilder setNameBuilder = new StringBuilder("_");

        // Split the sets chosen up into individual sets and get their uris
        String[] setBits = chosenSets.split("%_%");
        for (String setBit : setBits) {
            if (setBit != null && !setBit.isEmpty()) {
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", setBit);
                setUris.add(uri);
                setNameBuilder.append(setBit).append("_");
            }
        }

        String setName = setNameBuilder.substring(0, setNameBuilder.lastIndexOf("_"));
        if (setName.startsWith("_")) {
            setName = setName.replaceFirst("_", "");
        }
        mainActivityInterface.getPreferences().setMyPreferenceString(
                "setCurrentLastName", setName);

        new Thread(() -> {
            // Empty the cache directories as new sets can have custom items
            mainActivityInterface.getSetActions().loadSets(setUris);
            // Import ended
            requireActivity().runOnUiThread(() -> {
                myView.progressBar.setVisibility(View.GONE);
                mainActivityInterface.navHome();
                mainActivityInterface.chooseMenu(true);
            });
        }).start();
    }


    // This comes back from the activity after it gets the text from the TextInputBottomSheet dialog
    // This brings in a new category name
    public void updateValue(String value) {
        // We will temporarily add this category
        if (!categories.contains(value)) {
            categories.add(value);
            // Sort them (remove main, sort, then readd main to the top)
            categories.remove(getString(R.string.mainfoldername));
            Collections.sort(categories);
            categories.add(0, getString(R.string.mainfoldername));
            categoriesAdapter.notifyDataSetChanged();
        }
        myView.setCategory.setText(value);
    }

    public void doRename() {
        // Received back from the are you sure dialog
        mainActivityInterface.getStorageAccess().renameFileFromUri(
                oldSetUri,newSetUri,"Sets","",newSetFilename);
        prepareSets();
    }

}