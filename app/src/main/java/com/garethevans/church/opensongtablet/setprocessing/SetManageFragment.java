package com.garethevans.church.opensongtablet.setprocessing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsManageBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;
import com.google.android.material.textview.MaterialTextView;

import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private String mainfoldername_string="";
    private String set_current_string="";
    private String set_string="";
    private String save_string="";
    private String website_set_save_string="";
    private String rename_string="";
    private String delete_string="";
    private String website_set_rename_string="";
    private String website_set_delete_string="";
    private String export_string="";
    private String website_export_set_string="";
    private String set_saved_not_current_string="";
    private String load_string="";
    private String website_set_load_string="";
    private String new_category_string="";
    private String file_exists_string="";
    private String success_string="";
    private String error_string="";
    private String deeplink_export_string="";
    private String search_index_wait_string="";
    private String toolBarTitle="";
    private String webAddress;
    private int activeColor, inactiveColor;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(toolBarTitle);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSetsManageBinding.inflate(inflater, container, false);

        prepareStrings();

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

    private void prepareStrings() {
        if (getContext()!=null) {
            mainfoldername_string = getString(R.string.mainfoldername);
            set_current_string = getString(R.string.set_current);
            set_string = getString(R.string.set);
            save_string = getString(R.string.save);
            website_set_save_string = getString(R.string.website_set_save);
            rename_string = getString(R.string.rename);
            website_set_rename_string = getString(R.string.website_set_rename);
            delete_string = getString(R.string.delete);
            website_set_delete_string = getString(R.string.website_set_delete);
            export_string = getString(R.string.export);
            website_export_set_string = getString(R.string.website_export_set);
            set_saved_not_current_string = getString(R.string.set_saved_not_current);
            load_string = getString(R.string.load);
            website_set_load_string = getString(R.string.website_set_load);
            new_category_string = getString(R.string.new_category);
            file_exists_string = getString(R.string.file_exists);
            success_string = getString(R.string.success);
            error_string = getString(R.string.error);
            deeplink_export_string = getString(R.string.deeplink_export);
            search_index_wait_string = getString(R.string.index_songs_wait);
            activeColor = getContext().getResources().getColor(R.color.colorSecondary);
            inactiveColor = getContext().getResources().getColor(R.color.colorAltPrimary);
        }
    }
    private void checkForLoadSpecific() {
        if (mainActivityInterface.getWhattodo().startsWith("loadset:")) {
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
        String category = mainfoldername_string;
        String setname = mainActivityInterface.getCurrentSet().getSetCurrentLastName();
        if (setname.contains("__")) {
            String[] bits = setname.split("__");
            if (bits.length > 0) {
                category = bits[0];
                setname = bits[1];
            }
        }
        if (setname.equals(set_current_string)) {
            setname = "";
        }

        // Decide on the views required
        switch (whattodo) {
            case "saveset":
                toolBarTitle = set_string + ": " + save_string;
                webAddress = website_set_save_string;
                myView.setName.setVisibility(View.VISIBLE);
                myView.overWrite.setVisibility(View.VISIBLE);
                myView.setLoadInfo1.setVisibility(View.GONE);
                myView.setLoadInfo2.setVisibility(View.GONE);
                myView.setLoadFirst.setVisibility(View.GONE);
                myView.setName.setText(setname);
                myView.newCategory.setVisibility(View.VISIBLE);
                myView.setCategory.setText(category);
                myView.loadorsaveButton.setText(save_string);
                if (getContext()!=null) {
                    myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.save, getContext().getTheme()));
                }
                myView.loadorsaveButton.setOnClickListener(v -> saveSet());
                break;

            case "renameset":
                toolBarTitle = set_string + ": " + rename_string;
                webAddress = website_set_rename_string;
                myView.setName.setVisibility(View.VISIBLE);
                myView.overWrite.setVisibility(View.VISIBLE);
                myView.setLoadInfo1.setVisibility(View.GONE);
                myView.setLoadInfo2.setVisibility(View.GONE);
                myView.setLoadFirst.setVisibility(View.GONE);
                myView.setName.setText(setname);
                myView.newCategory.setVisibility(View.VISIBLE);
                myView.setCategory.setText(category);
                myView.loadorsaveButton.setText(rename_string);
                if (getContext()!=null) {
                    myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.save, getContext().getTheme()));
                }
                myView.loadorsaveButton.setOnClickListener(v -> renameSet());
                break;

            case "deleteset":
                toolBarTitle = set_string + ": " + delete_string;
                webAddress = website_set_delete_string;
                myView.setName.setVisibility(View.GONE);
                myView.overWrite.setVisibility(View.GONE);
                myView.setLoadInfo1.setVisibility(View.VISIBLE);
                myView.setLoadInfo2.setVisibility(View.GONE);
                myView.setLoadFirst.setVisibility(View.GONE);
                myView.newCategory.setVisibility(View.GONE);
                myView.setCategory.setVisibility(View.VISIBLE);
                myView.loadorsaveButton.setText(delete_string);
                if (getContext()!=null) {
                    myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.delete, getContext().getTheme()));
                }
                myView.loadorsaveButton.setOnClickListener(v -> deleteSet());
                break;

            case "exportset":
                toolBarTitle = set_string + ": " + export_string;
                webAddress = website_export_set_string;
                myView.setName.setVisibility(View.GONE);
                myView.overWrite.setVisibility(View.GONE);
                myView.newCategory.setVisibility(View.GONE);
                myView.setLoadInfo1.setVisibility(View.VISIBLE);
                myView.setLoadInfo2.setVisibility(View.VISIBLE);
                myView.setLoadFirst.setVisibility(View.GONE);
                myView.setLoadInfo2.setText(set_saved_not_current_string);
                myView.loadorsaveButton.setText(export_string);
                if (getContext()!=null) {
                    myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.share, getContext().getTheme()));
                }
                myView.loadorsaveButton.setOnClickListener(v -> exportSet());
                break;

            case "loadset":
            default:
                toolBarTitle = set_string + ": " + load_string;
                webAddress = website_set_load_string;
                myView.setName.setVisibility(View.GONE);
                myView.overWrite.setVisibility(View.GONE);
                myView.newCategory.setVisibility(View.GONE);
                myView.setLoadInfo1.setVisibility(View.VISIBLE);
                myView.setLoadInfo2.setVisibility(View.VISIBLE);
                myView.setLoadFirst.setVisibility(View.VISIBLE);
                myView.setLoadFirst.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("setLoadFirst",true));
                myView.setLoadFirst.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("setLoadFirst",b));
                myView.loadorsaveButton.setText(load_string);
                if (getContext()!=null) {
                    myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.save, getContext().getTheme()));
                }
                myView.loadorsaveButton.setOnClickListener(v -> loadSet());
                break;
        }
    }

    private void setListener() {
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(
                    getContext(), myView.setCategory, R.layout.view_exposed_dropdown_item, categories);
            myView.setCategory.setAdapter(exposedDropDownArrayAdapter);
        }
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
            if (getActivity() != null) {
                TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(this,
                        "SetManageFragment", new_category_string,
                        new_category_string, null, null, null, true);
                textInputBottomSheet.show(getActivity().getSupportFragmentManager(), "TextInputBottomSheet");
            }
        });

        myView.nestedScrollView.setExtendedFabToAnimate(myView.loadorsaveButton);
        myView.sortAZ.setOnClickListener(view -> {
            mainActivityInterface.getPreferences().setMyPreferenceString("setsSortOrder","az");
            listAvailableSets();
        });
        myView.sortZA.setOnClickListener(view -> {
            mainActivityInterface.getPreferences().setMyPreferenceString("setsSortOrder","za");
            listAvailableSets();
        });
        myView.sortOldest.setOnClickListener(view -> {
            mainActivityInterface.getPreferences().setMyPreferenceString("setsSortOrder","oldest");
            listAvailableSets();
        });
        myView.sortNewest.setOnClickListener(view -> {
            mainActivityInterface.getPreferences().setMyPreferenceString("setsSortOrder","newest");
            listAvailableSets();
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
        allSets = mainActivityInterface.getSetActions().getAllSets();
    }

    private void listCategories() {
        categories = mainActivityInterface.getSetActions().getCategories(allSets);
        if (getContext()!=null) {
            categoriesAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.setCategory,
                    R.layout.view_exposed_dropdown_item, categories);
            myView.setCategory.setAdapter(categoriesAdapter);
        }
        myView.setCategory.setText(mainActivityInterface.getPreferences().getMyPreferenceString(
                "whichSetCategory", mainfoldername_string));
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

        // Get a better array of the sets which includes last modified dates
        ArrayList<FoundSet> foundSets = new ArrayList<>();
        for (int x=0; x<availableSets.size(); x++) {
            String filename = availableSets.get(x);
            FoundSet foundSet = new FoundSet();
            foundSet.setFilename(filename);
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Sets","",filename);
            foundSet.setUri(uri);
            foundSet.setTitle(filename.replace(bitToRemove, ""));
            foundSet.setTag(filename.replace("__", "/"));
            long lastModifiedLong = mainActivityInterface.getStorageAccess().getLastModifiedDate(uri);
            String lastModifiedString = mainActivityInterface.getTimeTools().getDateFromMillis(mainActivityInterface.getLocale(),lastModifiedLong);
            foundSet.setLastModifiedLong(lastModifiedLong);
            foundSet.setLastModifiedString(lastModifiedString);
            foundSets.add(foundSet);
        }

        String setsSortOrder = mainActivityInterface.getPreferences().getMyPreferenceString("setsSortOrder","oldest");
        // Now do the sorting based on the user preference
        switch (setsSortOrder) {
            case "az":
                Collections.sort(foundSets, (FoundSet a, FoundSet z) -> a.getTitle().compareTo(z.getTitle()));
                break;
            case "za":
                Collections.sort(foundSets, (FoundSet a, FoundSet z) -> z.getTitle().compareTo(a.getTitle()));
                break;
            case "newest":
                Collections.sort(foundSets, (o1, o2) -> Long.compare(o2.getLastModifiedLong(), o1.getLastModifiedLong()));
                break;
            case "oldest":
                Collections.sort(foundSets, (o1, o2) -> Long.compare(o1.getLastModifiedLong(), o2.getLastModifiedLong()));
                break;
        }
        ColorStateList active = ColorStateList.valueOf(activeColor);
        ColorStateList inactive = ColorStateList.valueOf(inactiveColor);
        myView.sortAZ.setSupportBackgroundTintList(setsSortOrder.equals("az") ? active:inactive);
        myView.sortZA.setSupportBackgroundTintList(setsSortOrder.equals("za") ? active:inactive);
        myView.sortOldest.setSupportBackgroundTintList(setsSortOrder.equals("oldest") ? active:inactive);
        myView.sortNewest.setSupportBackgroundTintList(setsSortOrder.equals("newest") ? active:inactive);

        if (getContext()!=null) {
            for (int x=0; x<foundSets.size(); x++) {
                @SuppressLint("InflateParams") LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_checkbox_list_item, null);
                CheckBox checkBoxItem = linearLayout.findViewById(R.id.checkBoxItem);
                MaterialTextView modifiedDate = linearLayout.findViewById(R.id.modifiedDate);
                checkBoxItem.setTag(foundSets.get(x).getTag());
                checkBoxItem.setText(foundSets.get(x).getTitle());

                modifiedDate.setText(foundSets.get(x).getLastModifiedString());

                if (whattodo.equals("saveset") || whattodo.equals("renameset")) {
                    checkBoxItem.setButtonDrawable(null);
                    linearLayout.setAlpha(0.6f);
                } else {
                    String toFind = bitToRemove + foundSets.get(x).getFilename();
                    toFind = toFind.replace(mainfoldername_string + "__", "");
                    checkBoxItem.setChecked(chosenSets.contains("%_%" + toFind + "%_%"));
                }
                String finalSetName = foundSets.get(x).getFilename();
                if (!whattodo.equals("renameset") && !whattodo.equals("saveset")) {
                    checkBoxItem.setOnCheckedChangeListener((compoundButton, b) -> {
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
                    linearLayout.setAlpha(0.6f);
                    checkBoxItem.setOnClickListener(view -> {
                        String text = view.getTag().toString();
                        if (text.contains("/")) {
                            String[] bits = text.split("/");
                            if (bits.length > 1) {
                                renameSetCategory = bits[0];
                                renameSetName = bits[1];
                                newSetFilename = renameSetCategory + "__" + renameSetName;
                            }
                        } else {
                            renameSetCategory = mainfoldername_string;
                            renameSetName = text;
                            newSetFilename = text;
                        }
                        myView.setName.setText(renameSetName);
                        myView.setCategory.setText(renameSetCategory);
                        updateCheckList(text);
                    });
                }
                myView.setLists.addView(linearLayout);
            }
        }
    }

    private void updateCheckList(String tag) {
        // Go through the checkbox list and if the tag matches, set full alpha, otherwise dim it
        for (int x=0; x<myView.setLists.getChildCount(); x++) {
            try {
                CheckBox cb = myView.setLists.getChildAt(x).findViewById(R.id.checkBoxItem);
                if (cb.getTag().equals(tag)) {
                    cb.setAlpha(1f);
                } else {
                    cb.setAlpha(0.6f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveSet() {
        // Get the set category
        String category = myView.setCategory.getText().toString();
        if (category.equals(mainfoldername_string)) {
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
                        file_exists_string);
            } else {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" saveSet Create Sets/"+setName+" deleteOld=true");
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null,
                        "Sets", "", setName);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().
                        getOutputStream(uri);

                String setXML = mainActivityInterface.getSetActions().createSetXML();
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" saveSet writeFileFromString Sets/"+setName+" with: "+setXML);
                if (mainActivityInterface.getStorageAccess().writeFileFromString(
                        setXML, outputStream)) {
                    // Update the last loaded set now it is saved.
                    mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits(
                            mainActivityInterface.getCurrentSet().getSetCurrent());
                    mainActivityInterface.getCurrentSet().setSetCurrentLastName(setName);
                    mainActivityInterface.getCurrentSet().updateSetTitleView();
                    mainActivityInterface.getShowToast().doIt(set_current_string + " - " +
                            success_string);
                } else {
                    mainActivityInterface.getShowToast().doIt(error_string);
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
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" deleteSet deleteFile "+uri);
                    if (!mainActivityInterface.getStorageAccess().deleteFile(uri)) {
                        success = false;
                    }
                }
            }
        }
        if (success) {
            mainActivityInterface.getShowToast().doIt(success_string);
        } else {
            mainActivityInterface.getShowToast().doIt(error_string);
        }

        // Hide the progress bar
        myView.progressBar.setVisibility(View.GONE);

        prepareSets();
    }

    private void renameSet() {
        String oldSetText;
        String oldSetFilename;
        if (renameSetCategory == null || renameSetCategory.isEmpty() || renameSetCategory.equals(mainfoldername_string)) {
            oldSetFilename = renameSetName;
            oldSetText = mainfoldername_string + "/" + renameSetName;
        } else {
            oldSetFilename = renameSetCategory + "__" + renameSetName;
            oldSetText = renameSetCategory + "/" + renameSetName;
        }
        oldSetUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets","", oldSetFilename);

        Editable mycat = myView.setCategory.getText();
        Editable mynam = myView.setName.getText();
        if (mycat!=null && !mycat.toString().isEmpty() && mynam!=null && !mynam.toString().isEmpty()) {
            String newSetText;
            if (mycat.toString().isEmpty() || mycat.toString().equals(mainfoldername_string)) {
                newSetFilename = mynam.toString();
                newSetText = mainfoldername_string + "/" + mynam;
            } else {
                newSetFilename = mycat + "__" + mynam;
                newSetText = mycat + "/" + mynam;
            }
            newSetUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets","",newSetFilename);

            boolean exists = mainActivityInterface.getStorageAccess().uriExists(newSetUri);
            if (exists && !myView.overWrite.isChecked()) {
                mainActivityInterface.getShowToast().doIt(file_exists_string);
            } else {
                AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("renameSet",
                        rename_string+":\n"+ oldSetText +" > "+ newSetText,null,
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
            mainActivityInterface.navigateToFragment(deeplink_export_string, 0);
        } else {
            String progressText = "";
            if (mainActivityInterface.getSongMenuFragment()!=null) {
                MaterialTextView progressView = mainActivityInterface.getSongMenuFragment().getProgressText();
                if (progressView!=null && progressView.getText()!=null) {
                    progressText = " " + progressView.getText().toString();
                }
            }
            mainActivityInterface.getShowToast().doIt(search_index_wait_string + progressText);
        }
    }

    private void loadSet() {
        // Show the progressBar
        myView.progressBar.setVisibility(View.VISIBLE);
        // Initialise the current set
        mainActivityInterface.getCurrentSet().initialiseTheSet();
        mainActivityInterface.getCurrentSet().setSetCurrent("");
        mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits("");

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
        mainActivityInterface.getCurrentSet().setSetCurrentLastName(setName);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        String finalSetName = setName;
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            // Empty the cache directories as new sets can have custom items
            mainActivityInterface.getSetActions().loadSets(setUris, finalSetName);
            // Import ended
            handler.post(() -> {
                myView.progressBar.setVisibility(View.GONE);
                mainActivityInterface.setWhattodo("pendingLoadSet");
                mainActivityInterface.navHome();
                mainActivityInterface.getCurrentSet().updateSetTitleView();
                mainActivityInterface.chooseMenu(true);
            });

        });
    }


    // This comes back from the activity after it gets the text from the TextInputBottomSheet dialog
    // This brings in a new category name
    public void updateValue(String value) {
        // We will temporarily add this category
        if (!categories.contains(value)) {
            categories.add(value);
            // Sort them (remove main, sort, then readd main to the top)
            categories.remove(mainfoldername_string);
            Collator coll = Collator.getInstance(mainActivityInterface.getLocale());
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(categories, coll);
            categories.add(0, mainfoldername_string);
            categoriesAdapter.notifyDataSetChanged();
        }
        myView.setCategory.setText(value);
    }

    public void doRename() {
        // Received back from the are you sure dialog
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doRename renameFileFromUri "+oldSetUri+" to "+newSetUri);
        mainActivityInterface.getStorageAccess().renameFileFromUri(
                oldSetUri,newSetUri,"Sets","",newSetFilename);
        prepareSets();
    }

}