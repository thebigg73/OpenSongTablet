package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class SetManageFragment extends Fragment {

    private static final String TAG = "SetManageFragment";
    private SettingsSetsManageBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> categories;
    private ExposedDropDownArrayAdapter categoriesAdapter;

    // What action we should be doing
    private String whattodo;

    // The values for the set
    private String setName, setCategory;
    private boolean overwrite;

    private Uri oldSetUri, newSetUri;
    private String oldSetFilename, newSetFilename;

    // The strings to use
    private String set_current_string="", set_string="", save_string="", website_set_save_string="",
            rename_string="", delete_string="", website_set_rename_string="",
            website_set_delete_string="", export_string="", website_export_set_string="",
            set_saved_not_current_string="", load_string="", website_set_load_string="",
            new_category_string="", file_exists_string="", success_string="", error_string="",
            deeplink_export_string="", search_index_wait_string="", toolBarTitle="", webAddress,
            import_string="", website_set_import_string="";

    // If we try to do something before indexing is complete, we get the progress from the indexing
    private String progressText;

    // The colours for the sort buttons
    private int activeColor, inactiveColor;
    private ColorStateList activeColorStateList, inactiveColorStateList;

    // The adapter that populates the recyclerview
    private SetManageAdapter setManageAdapter;

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

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            prepareStrings();

            whattodo = mainActivityInterface.getWhattodo();

            // Check if we are importing a set.  If so, extract the filename/uri
            checkForImporting();

            // Create the setManageAdapter;
            setManageAdapter = new SetManageAdapter(getContext(),this,whattodo);

            // Decide what we are doing
            changeViews();

            // Prepare the set category dropdown
            prepareSetCategory();

            // Set listeners
            setListener();

            // Prepare the recyclerView
            prepareSetItems();

            // Check the toolbar title and tip
            mainActivityInterface.getMainHandler().post(() -> {
                mainActivityInterface.updateToolbar(toolBarTitle);
                mainActivityInterface.updateToolbarHelp(webAddress);
            });
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            set_current_string = getString(R.string.set_current);
            set_string = getString(R.string.set);
            save_string = getString(R.string.save);
            import_string = getString(R.string.import_basic);
            website_set_import_string = getString(R.string.website_set_import);
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

    // If we are importing, extract the name/uri and adjust the whattodo
    private void checkForImporting() {
        if (mainActivityInterface.getWhattodo().equals("importset")) {
            // If the imported set has a category, get it
            setName = mainActivityInterface.getImportFilename();
            if (setName.contains(mainActivityInterface.getSetActions().getSetCategorySeparator())) {
                String[] bits = setName.split(mainActivityInterface.getSetActions().getSetCategorySeparator());
                setCategory = bits[0];
                setName = bits[bits.length-1];
            } else {
                setCategory = mainActivityInterface.getMainfoldername();
            }
            whattodo = "importset";
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
        // If we are importing, this has already been set
        if (setCategory==null || setCategory.isEmpty()) {
            setCategory = mainActivityInterface.getMainfoldername();
        }
        if (setName==null || setName.isEmpty()) {
            setName = mainActivityInterface.getCurrentSet().getSetCurrentLastName();
            if (setName.contains("__")) {
                String[] bits = setName.split("__");
                if (bits.length > 0) {
                    setCategory = bits[0];
                    setName = bits[1];
                }
            }
        }

        if (setName.equals(set_current_string)) {
            setName = "";
        }

        // Decide on the views required
        switch (whattodo) {
            case "importset":
                toolBarTitle = set_string + ": " + import_string;
                webAddress = website_set_import_string;
                mainActivityInterface.getMainHandler().post(() -> {
                    myView.setItemSelected.setVisibility(View.VISIBLE);
                    myView.setItemSelected.setHint(mainActivityInterface.getImportFilename());
                    myView.setName.setVisibility(View.VISIBLE);
                    myView.overWrite.setVisibility(View.VISIBLE);
                    myView.setLoadInfo1.setVisibility(View.GONE);
                    myView.setLoadInfo2.setVisibility(View.GONE);
                    myView.setLoadFirst.setVisibility(View.GONE);
                    myView.setName.setText(setName);
                    myView.newCategory.setVisibility(View.VISIBLE);
                    myView.setCategory.setText(setCategory);
                    myView.loadorsaveButton.setText(save_string);
                    if (getContext() != null) {
                        myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.save, getContext().getTheme()));
                    }
                    myView.loadorsaveButton.setOnClickListener(v -> importSet());
                });
                break;

            case "saveset":
                toolBarTitle = set_string + ": " + save_string;
                webAddress = website_set_save_string;
                mainActivityInterface.getMainHandler().post(() -> {
                    myView.setItemSelected.setVisibility(View.GONE);
                    myView.setName.setVisibility(View.VISIBLE);
                    myView.overWrite.setVisibility(View.VISIBLE);
                    myView.setLoadInfo1.setVisibility(View.GONE);
                    myView.setLoadInfo2.setVisibility(View.GONE);
                    myView.setLoadFirst.setVisibility(View.GONE);
                    myView.setName.setText(setName);
                    myView.newCategory.setVisibility(View.VISIBLE);
                    myView.setCategory.setText(setCategory);
                    myView.loadorsaveButton.setText(save_string);
                    if (getContext() != null) {
                        myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.save, getContext().getTheme()));
                    }
                    myView.loadorsaveButton.setOnClickListener(v -> saveSet());
                });
                break;

            case "renameset":
                toolBarTitle = set_string + ": " + rename_string;
                webAddress = website_set_rename_string;
                mainActivityInterface.getMainHandler().post(() -> {
                    myView.setItemSelected.setVisibility(View.VISIBLE);
                    myView.setName.setVisibility(View.VISIBLE);
                    myView.overWrite.setVisibility(View.VISIBLE);
                    myView.setLoadInfo1.setVisibility(View.GONE);
                    myView.setLoadInfo2.setVisibility(View.GONE);
                    myView.setLoadFirst.setVisibility(View.GONE);
                    myView.setName.setText(setName);
                    myView.newCategory.setVisibility(View.VISIBLE);
                    myView.setCategory.setText(setCategory);
                    myView.loadorsaveButton.setText(rename_string);
                    if (getContext() != null) {
                        myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.save, getContext().getTheme()));
                    }
                    myView.loadorsaveButton.setOnClickListener(v -> renameSet());
                });
                break;

            case "deleteset":
                toolBarTitle = set_string + ": " + delete_string;
                webAddress = website_set_delete_string;
                mainActivityInterface.getMainHandler().post(() -> {
                    myView.setItemSelected.setVisibility(View.VISIBLE);
                    myView.setName.setVisibility(View.GONE);
                    myView.overWrite.setVisibility(View.GONE);
                    myView.setLoadInfo1.setVisibility(View.VISIBLE);
                    myView.setLoadInfo2.setVisibility(View.GONE);
                    myView.setLoadFirst.setVisibility(View.GONE);
                    myView.newCategory.setVisibility(View.GONE);
                    myView.setCategory.setVisibility(View.VISIBLE);
                    myView.loadorsaveButton.setText(delete_string);
                    if (getContext() != null) {
                        myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.delete, getContext().getTheme()));
                    }
                    myView.loadorsaveButton.setOnClickListener(v -> deleteSet());
                });
                break;

            case "exportset":
                toolBarTitle = set_string + ": " + export_string;
                webAddress = website_export_set_string;
                mainActivityInterface.getMainHandler().post(() -> {
                    myView.setItemSelected.setVisibility(View.VISIBLE);
                    myView.setName.setVisibility(View.GONE);
                    myView.overWrite.setVisibility(View.GONE);
                    myView.newCategory.setVisibility(View.GONE);
                    myView.setLoadInfo1.setVisibility(View.VISIBLE);
                    myView.setLoadInfo2.setVisibility(View.VISIBLE);
                    myView.setLoadFirst.setVisibility(View.GONE);
                    myView.setLoadInfo2.setText(set_saved_not_current_string);
                    myView.loadorsaveButton.setText(export_string);
                    if (getContext() != null) {
                        myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.share, getContext().getTheme()));
                    }
                    myView.loadorsaveButton.setOnClickListener(v -> exportSet());
                });
                break;

            case "loadset":
            default:
                toolBarTitle = set_string + ": " + load_string;
                webAddress = website_set_load_string;
                mainActivityInterface.getMainHandler().post(() -> {
                    myView.setItemSelected.setVisibility(View.VISIBLE);
                    myView.setName.setVisibility(View.GONE);
                    myView.overWrite.setVisibility(View.GONE);
                    myView.newCategory.setVisibility(View.GONE);
                    myView.setLoadInfo1.setVisibility(View.VISIBLE);
                    myView.setLoadInfo2.setVisibility(View.VISIBLE);
                    myView.setLoadFirst.setVisibility(View.VISIBLE);
                    myView.setLoadFirst.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("setLoadFirst", true));
                    myView.setLoadFirst.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("setLoadFirst", b));
                    myView.loadorsaveButton.setText(load_string);
                    if (getContext() != null) {
                        myView.loadorsaveButton.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.save, getContext().getTheme()));
                    }
                    myView.loadorsaveButton.setOnClickListener(v -> loadSet());
                });
                break;
        }
    }

    private void prepareSetCategory() {
        categories = mainActivityInterface.getSetActions().getCategories(mainActivityInterface.getSetActions().getAllSets());
        mainActivityInterface.getMainHandler().post(() -> {

            if (getContext() != null) {
                categoriesAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.setCategory,
                        R.layout.view_exposed_dropdown_item, categories);
                myView.setCategory.setAdapter(categoriesAdapter);
            }
        });
    }

    private void setListener() {
        // Do this on the main thread
        mainActivityInterface.getMainHandler().post(() -> {
            if (getContext() != null) {
                ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(
                        getContext(), myView.setCategory, R.layout.view_exposed_dropdown_item, categories);
                myView.setCategory.setAdapter(exposedDropDownArrayAdapter);
            }
            myView.setCategory.setText(mainActivityInterface.getPreferences().getMyPreferenceString(
                    "whichSetCategory", mainActivityInterface.getMainfoldername()));
            myView.setCategory.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    mainActivityInterface.getPreferences().setMyPreferenceString("whichSetCategory",myView.setCategory.getText().toString());
                    setManageAdapter.prepareSetManageInfos();
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

            String setsSortOrder = mainActivityInterface.getPreferences().getMyPreferenceString("setsSortOrder","oldest");
            activeColorStateList = ColorStateList.valueOf(activeColor);
            inactiveColorStateList = ColorStateList.valueOf(inactiveColor);
            changeSortIconColor(setsSortOrder);
            myView.sortAZ.setOnClickListener(view -> {
                mainActivityInterface.getPreferences().setMyPreferenceString("setsSortOrder", "az");
                changeSortIconColor("az");
                setManageAdapter.prepareSetManageInfos();
            });
            myView.sortZA.setOnClickListener(view -> {
                mainActivityInterface.getPreferences().setMyPreferenceString("setsSortOrder", "za");
                changeSortIconColor("za");
                setManageAdapter.prepareSetManageInfos();
            });
            myView.sortOldest.setOnClickListener(view -> {
                mainActivityInterface.getPreferences().setMyPreferenceString("setsSortOrder", "oldest");
                changeSortIconColor("oldest");
                setManageAdapter.prepareSetManageInfos();
            });
            myView.sortNewest.setOnClickListener(view -> {
                mainActivityInterface.getPreferences().setMyPreferenceString("setsSortOrder", "newest");
                changeSortIconColor("newest");
                setManageAdapter.prepareSetManageInfos();
            });
        });
    }

    private void changeSortIconColor(String setsSortOrder) {
        mainActivityInterface.getMainHandler().post(() -> {
            myView.sortAZ.setSupportBackgroundTintList(setsSortOrder.equals("az") ? activeColorStateList : inactiveColorStateList);
            myView.sortZA.setSupportBackgroundTintList(setsSortOrder.equals("za") ? activeColorStateList : inactiveColorStateList);
            myView.sortOldest.setSupportBackgroundTintList(setsSortOrder.equals("oldest") ? activeColorStateList : inactiveColorStateList);
            myView.sortNewest.setSupportBackgroundTintList(setsSortOrder.equals("newest") ? activeColorStateList : inactiveColorStateList);
        });
    }

    private void prepareSetItems() {
        mainActivityInterface.getMainHandler().post(() -> {
            myView.setLists.setItemAnimator(null);
            myView.setLists.setAdapter(setManageAdapter);
            setManageAdapter.prepareSetManageInfos();
        });
    }

    private void extractUserValuesBeforeProceeding() {
        setName = myView.setName.getText().toString();
        setCategory = myView.setCategory.getText().toString();
        overwrite = myView.overWrite.isChecked();
        if (newSetFilename==null || newSetFilename.isEmpty()) {
            if (setCategory.equals(mainActivityInterface.getMainfoldername())) {
                newSetFilename = setName;
            } else {
                newSetFilename = setCategory + mainActivityInterface.getSetActions().getSetCategorySeparator() + setName;
            }
        }
    }

    private void importSet() {
        // We will copy the importUri to the new chosen set name
        extractUserValuesBeforeProceeding();

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            newSetFilename = setName;
            if (!setCategory.equals(mainActivityInterface.getMainfoldername())) {
                // Add the category to the name
                newSetFilename = setCategory + mainActivityInterface.getSetActions().getSetCategorySeparator() + newSetFilename;
            }

            newSetUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", newSetFilename);
            if (!mainActivityInterface.getStorageAccess().uriExists(newSetUri) || overwrite) {
                // Get the inputStream for the file to import
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri());

                // Get the outputStream for the file to create/overwrite
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newSetUri, null, "Sets", "", newSetFilename);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newSetUri);

                // Copy the file
                if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                    Log.d(TAG,"copy successfull");
                    // Now load the set as well
                    mainActivityInterface.getMainHandler().post(this::loadSet);
                }

            } else {
                mainActivityInterface.getShowToast().doIt(file_exists_string);
            }
        });
    }

    private void saveSet() {
        // Get the set category and name
        extractUserValuesBeforeProceeding();

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            if (!setName.isEmpty()) {
                // Get a nice name
                setName = mainActivityInterface.getStorageAccess().safeFilename(setName);
                mainActivityInterface.getMainHandler().post(() -> myView.setName.setText(setName));
                if (setCategory.equals(mainActivityInterface.getMainfoldername())) {
                    newSetFilename = setName;
                } else {
                    newSetFilename = setCategory + mainActivityInterface.getSetActions().getSetCategorySeparator() + setName;
                }

                // If the file already exists and we aren't overwriting, alert the user to rename it
                newSetUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", newSetFilename);

                if (mainActivityInterface.getStorageAccess().uriExists(newSetUri) &&
                        !myView.overWrite.isChecked()) {
                    mainActivityInterface.getShowToast().doIt(file_exists_string);
                } else {
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " saveSet Create Sets/" + newSetFilename + " deleteOld=true");
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newSetUri, null,
                            "Sets", "", newSetFilename);
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newSetUri);

                    String setXML = mainActivityInterface.getSetActions().createSetXML();
                    String setAsPref = mainActivityInterface.getSetActions().getSetAsPreferenceString();
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " saveSet writeFileFromString Sets/" + newSetFilename + " with: " + setXML);
                    if (mainActivityInterface.getStorageAccess().writeFileFromString(setXML, outputStream)) {
                        // Update the last loaded set now it is saved.
                        mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits(
                                mainActivityInterface.getCurrentSet().getSetCurrent());
                        mainActivityInterface.getCurrentSet().setSetCurrent(setAsPref);
                        mainActivityInterface.getCurrentSet().setSetCurrentLastName(newSetFilename);
                        mainActivityInterface.getCurrentSet().updateSetTitleView();
                        mainActivityInterface.getShowToast().doIt(set_current_string + " - " +
                                success_string);
                    } else {
                        mainActivityInterface.getShowToast().doIt(error_string);
                    }
                }
            }
            setManageAdapter.prepareSetManageInfos();
            mainActivityInterface.getMainHandler().post(() -> myView.progressBar.setVisibility(View.GONE));
        });
    }

    private void deleteSet() {
        // Show the progressBar
        myView.progressBar.setVisibility(View.VISIBLE);

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Split the sets chosen up into individual sets and get their uris
            boolean success = true;
            // Get the checkedItems from the adapter
            ArrayList<String> checkedItems = setManageAdapter.getCheckedItems();
            for (String checkedItem:checkedItems) {
                // Get rid of the start/end identifiers to get the filename
                checkedItem = getFilenameFromIdentifier(checkedItem);
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Sets","",checkedItem);
                if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
                    // Try deleting the set file
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " deleteSet deleteFile " + uri);
                    if (!mainActivityInterface.getStorageAccess().deleteFile(uri)) {
                        success = false;
                    }
                }
            }

            if (success) {
                mainActivityInterface.getShowToast().doIt(success_string);
            } else {
                mainActivityInterface.getShowToast().doIt(error_string);
            }
            setManageAdapter.prepareSetManageInfos();
            mainActivityInterface.getMainHandler().post(() -> myView.progressBar.setVisibility(View.GONE));
        });
    }

    private void renameSet() {
        myView.progressBar.setVisibility(View.VISIBLE);
        setName = "";
        if (myView.setCategory.getText()!=null) {
            setCategory = myView.setCategory.getText().toString();
        }
        setName = "";
        if (myView.setName.getText()!=null) {
            setName = myView.setName.getText().toString();
        }
        boolean overwrite = myView.overWrite.isChecked();

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Get the set to rename from the setAdapter
            if (setManageAdapter.getCheckedItems().size()==1) {
                oldSetFilename = getFilenameFromIdentifier(setManageAdapter.getCheckedItems().get(0));
                newSetFilename = setName;
                if (!setCategory.equals(mainActivityInterface.getMainfoldername())) {
                    newSetFilename = setCategory + mainActivityInterface.getSetActions().getSetCategorySeparator() + setName;
                }
                newSetUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets","",newSetFilename);
                if (!mainActivityInterface.getStorageAccess().uriExists(newSetUri) || overwrite) {
                    String finalNewSetName = newSetFilename;
                    oldSetUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets","",oldSetFilename);
                    mainActivityInterface.getMainHandler().post(() -> {
                        AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("renameSet",
                                rename_string + ":\n" +
                                        mainActivityInterface.getSetActions().getNiceSetNameFromFile(oldSetFilename) + " > " +
                                        mainActivityInterface.getSetActions().getNiceSetNameFromFile(finalNewSetName), null,
                                "setManageFragment", this, null);
                        areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AreYouSureFragment");
                    });
                } else {
                    mainActivityInterface.getShowToast().doIt(file_exists_string);
                }
            }
            mainActivityInterface.getMainHandler().post(() -> myView.progressBar.setVisibility(View.GONE));
        });
    }

    private void exportSet() {
        myView.progressBar.setVisibility(View.VISIBLE);
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Only allow if indexing is complete
            if (mainActivityInterface.getSongListBuildIndex().getIndexComplete()) {
                // Set the "whattodo" to let the export fragment know we are exporting a set
                StringBuilder stringBuilder = new StringBuilder();
                for (String checkedItem:setManageAdapter.getCheckedItems()) {
                    stringBuilder.append(checkedItem);
                }
                mainActivityInterface.setWhattodo("exportset:" + stringBuilder);
                mainActivityInterface.getMainHandler().post(() -> mainActivityInterface.navigateToFragment(deeplink_export_string, 0));
            } else {
                progressText = "";
                mainActivityInterface.getMainHandler().post(() -> {
                    if (mainActivityInterface.getSongMenuFragment() != null) {
                        MaterialTextView progressView = mainActivityInterface.getSongMenuFragment().getProgressText();
                        if (progressView != null && progressView.getText() != null) {
                            progressText = " " + progressView.getText().toString();
                        }
                    }
                    mainActivityInterface.getShowToast().doIt(search_index_wait_string + progressText);
                });
            }
            mainActivityInterface.getMainHandler().post(() -> myView.progressBar.setVisibility(View.GONE));
        });
    }

    private void loadSet() {
        extractUserValuesBeforeProceeding();
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Show the progressBar
            mainActivityInterface.getMainHandler().post(() -> myView.progressBar.setVisibility(View.VISIBLE));

            // Get a note of how many items were in the currently loaded set
            int oldSize = mainActivityInterface.getCurrentSet().getCurrentSetSize();

            // Initialise the current set
            mainActivityInterface.getCurrentSet().initialiseTheSet();
            mainActivityInterface.getCurrentSet().setSetCurrent("");
            mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits("");

            // Notify the set menu to update to an empty set
            mainActivityInterface.notifySetFragment("clear",oldSize);

            // Because we can import multiple sets, we need to get them into an array
            ArrayList<Uri> setUris = new ArrayList<>();

            if (whattodo.equals("importset")) {
                setUris.add(newSetUri);
            } else {
                for (String checkedItem : setManageAdapter.getCheckedItems()) {
                    Log.d(TAG,"checkedItem:"+checkedItem);
                    checkedItem = getFilenameFromIdentifier(checkedItem);
                    newSetFilename = checkedItem;
                    Log.d(TAG,"checkedItem:"+checkedItem);
                    setUris.add(mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", checkedItem));
                }
            }

            Log.d(TAG,"setUris.size():"+setUris.size());
            // Use the set name as long as there is only one set chosen
            String newSetTitle = "";
            if (setUris.size()==1) {
                newSetTitle = newSetFilename;
            }
            mainActivityInterface.getCurrentSet().initialiseTheSet();

            // Empty the cache directories as new sets can have custom items
            mainActivityInterface.getSetActions().loadSets(setUris, newSetTitle);

            // Import ended - do this on the main UI
            mainActivityInterface.getMainHandler().post(() -> {
                myView.progressBar.setVisibility(View.GONE);
                mainActivityInterface.setWhattodo("pendingLoadSet");
                mainActivityInterface.getCurrentSet().updateSetTitleView();
                mainActivityInterface.navHome();
                mainActivityInterface.chooseMenu(true);
            });
        });
    }

    // This comes back from the MainActivity (via updateFragment) after a user clicks on a set or checks/unchecks the box
    public void updateSelectedSet(ArrayList<String> selectedItems) {
        // Go through the array and build the text of the sets selected
        StringBuilder stringBuilder = new StringBuilder();
        for (String selectedItem:selectedItems) {
            // Replace the start and end bits of the string
            selectedItem = selectedItem.
                    replace(mainActivityInterface.getSetActions().getItemStart(), "").
                    replace(mainActivityInterface.getSetActions().getItemEnd(), "");

            if (whattodo.equals("saveset") || whattodo.equals("importset")) {
                // We need to update the set name in the edit text
                myView.setName.setText(selectedItem.replace(myView.setCategory.getText()+mainActivityInterface.getSetActions().getSetCategorySeparator(),""));
            }

            // If the return value has __ make it look nicer
            if (selectedItem.contains(mainActivityInterface.getSetActions().getSetCategorySeparator())) {
                selectedItem = "(" + selectedItem.replace(mainActivityInterface.getSetActions().getSetCategorySeparator(), ") ");
            }

            // Add in the MAIN folder if required
            if (!selectedItem.startsWith("(")) {
                selectedItem = "(" + mainActivityInterface.getMainfoldername() + ") " + selectedItem;
            }
            stringBuilder.append(selectedItem).append(", ");
        }
        String what = stringBuilder.toString();
        if (what.endsWith(", ")) {
            what = what.substring(0,what.lastIndexOf(", "));
        }
        if (whattodo.equals("loadset")) {
            myView.setItemSelected.setHint(what);
        }
    }

    // This comes back from the activity after it gets the text from the TextInputBottomSheet dialog
    // This brings in a new category name
    public void updateValue(String value) {
        // We will temporarily add this category
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            if (!categories.contains(value)) {
                categories.add(value);
                // Sort them (remove main, sort, then re-add main to the top)
                categories.remove(mainActivityInterface.getMainfoldername());
                Collator coll = Collator.getInstance(mainActivityInterface.getLocale());
                coll.setStrength(Collator.SECONDARY);
                Collections.sort(categories, coll);
                categories.add(0, mainActivityInterface.getMainfoldername());
                mainActivityInterface.getMainHandler().post(() -> categoriesAdapter.notifyDataSetChanged());
            }
            mainActivityInterface.getMainHandler().post(() -> myView.setCategory.setText(value));
        });
    }

    public void doRename() {
        // Received back from the are you sure dialog
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " doRename renameFileFromUri " + oldSetUri + " to " + newSetUri);
            mainActivityInterface.getStorageAccess().renameFileFromUri(
                    oldSetUri, newSetUri, "Sets", "", newSetFilename);
            // Update the recycler view via the adapter
            setManageAdapter.prepareSetManageInfos();
        });
    }

    public String getFilenameFromIdentifier(String identifier) {
        return identifier.replace(mainActivityInterface.getSetActions().getItemStart(),"").
                replace(mainActivityInterface.getSetActions().getItemEnd(),"").
                replace(mainActivityInterface.getMainfoldername()+mainActivityInterface.getSetActions().getSetCategorySeparator(),"");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myView = null;
        setManageAdapter = null;
    }
}