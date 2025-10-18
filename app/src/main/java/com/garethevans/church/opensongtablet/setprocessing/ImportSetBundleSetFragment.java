package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsManageBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.OutputStream;

public class ImportSetBundleSetFragment extends Fragment {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "ImportSetBundleSetFrag";
    private SettingsSetsManageBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String import_set = "", exists_string = "";
    private Drawable import_drawable;
    private SetManageAdapter setManageAdapter;

    // The colours for the sort buttons
    private int activeColor, inactiveColor;
    private ColorStateList activeColorStateList, inactiveColorStateList;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.getOpenSongSetBundle().setImportSetBundleSetFragment(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface = null;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsSetsManageBinding.inflate(inflater, container, false);

        return myView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        setupViews();
        setupListeners();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            import_set = getString(R.string.set_import);
            exists_string = getString(R.string.file_exists);
            import_drawable = ContextCompat.getDrawable(getContext(), R.drawable.set_add);
            activeColor = mainActivityInterface.getPalette().secondary;
            inactiveColor = mainActivityInterface.getPalette().primaryVariant;
        }
    }

    private void setupViews() {
        // Hide the views we don't need and adapt the text
        // This view is also used in the SetManageFragment class (for osts files)
        if (getContext()!=null && myView!=null) {
            myView.setItemSelected.setVisibility(View.GONE);
            myView.setLoadInfo1.setVisibility(View.GONE);
            myView.setLoadInfo2.setVisibility(View.GONE);
            myView.loadorsaveButton.setText(import_set);
            myView.loadorsaveButton.setIcon(import_drawable);

            // Get a list of the set Categories
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(
                    getContext(), myView.setCategory, R.layout.view_exposed_dropdown_item,
                    mainActivityInterface.getSetActions().getCategories(mainActivityInterface.getSetActions().getAllSets()));
            myView.setCategory.setAdapter(exposedDropDownArrayAdapter);

            // Hopefully we have dealt with the zip content already
            // If not the helper will update when it has finished
            if (mainActivityInterface.getOpenSongSetBundle().getSetFile() != null) {
                setSetName(mainActivityInterface.getOpenSongSetBundle().getSetFileName());
            }

            // The sort buttons
            String setsSortOrder = mainActivityInterface.getPreferences().getMyPreferenceString("setsSortOrder","oldest");
            activeColorStateList = ColorStateList.valueOf(activeColor);
            inactiveColorStateList = ColorStateList.valueOf(inactiveColor);
            myView.sortAZ.setSupportBackgroundTintList(setsSortOrder.equals("az") ? activeColorStateList : inactiveColorStateList);
            myView.sortZA.setSupportBackgroundTintList(setsSortOrder.equals("za") ? activeColorStateList : inactiveColorStateList);
            myView.sortOldest.setSupportBackgroundTintList(setsSortOrder.equals("oldest") ? activeColorStateList : inactiveColorStateList);
            myView.sortNewest.setSupportBackgroundTintList(setsSortOrder.equals("newest") ? activeColorStateList : inactiveColorStateList);
        }
    }

    private void setupListeners() {
        myView.setCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateSetsForCategory();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        myView.sortAZ.setOnClickListener(view -> changeSortOrder("az"));
        myView.sortZA.setOnClickListener(view -> changeSortOrder("za"));
        myView.sortNewest.setOnClickListener(view -> changeSortOrder("newest"));
        myView.sortOldest.setOnClickListener(view -> changeSortOrder("oldest"));

        myView.loadorsaveButton.setOnClickListener(view -> {
            if (myView != null && myView.setName.getText() != null && myView.setCategory.getText() != null) {
                String category = myView.setCategory.getText().toString();
                String name = myView.setName.getText().toString();
                if (!category.isEmpty() && !name.isEmpty()) {
                    String newName;
                    if (category.equals(mainActivityInterface.getMainfoldername())) {
                        newName = name;
                    } else {
                        newName = category + mainActivityInterface.getSetActions().getSetCategorySeparator() + name;
                    }
                    Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", newName);
                    if (!mainActivityInterface.getStorageAccess().uriExists(uri) || myView.overWrite.isChecked()) {
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, "Sets", "", newName);
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                        if (outputStream != null && mainActivityInterface.getOpenSongSetBundle().getSetFile() != null
                                && mainActivityInterface.getOpenSongSetBundle().getSetFile().exists()) {
                            mainActivityInterface.getStorageAccess().copyFile(mainActivityInterface.getStorageAccess().getInputStream(Uri.fromFile(mainActivityInterface.getOpenSongSetBundle().getSetFile())), outputStream);
                            mainActivityInterface.getShowToast().success();
                        } else {
                            mainActivityInterface.getShowToast().error();
                        }
                    } else {
                        mainActivityInterface.getShowToast().doIt(exists_string);
                    }
                }
            }
        });
    }

    private void updateSetsForCategory() {
        if (getContext()!=null && myView!=null) {
            setManageAdapter = new SetManageAdapter(getContext(),null,"importbundle");
            myView.setLists.setLayoutManager(new LinearLayoutManager(getContext()));
            myView.setLists.setAdapter(setManageAdapter);

            if (myView!=null && myView.setCategory.getText()!=null) {
                mainActivityInterface.getPreferences().setMyPreferenceString("whichSetCategory", myView.setCategory.getText().toString());
            }
            setManageAdapter.prepareSetManageInfos();
        }
    }

    private void changeSortOrder(String sortOrder) {
        mainActivityInterface.getPreferences().setMyPreferenceString("setsSortOrder",sortOrder);
        if (setManageAdapter!=null) {
            setManageAdapter.changeSortOrder();
        }
        myView.sortAZ.setSupportBackgroundTintList(sortOrder.equals("az") ? activeColorStateList : inactiveColorStateList);
        myView.sortZA.setSupportBackgroundTintList(sortOrder.equals("za") ? activeColorStateList : inactiveColorStateList);
        myView.sortOldest.setSupportBackgroundTintList(sortOrder.equals("oldest") ? activeColorStateList : inactiveColorStateList);
        myView.sortNewest.setSupportBackgroundTintList(sortOrder.equals("newest") ? activeColorStateList : inactiveColorStateList);

    }

    public void setSetName(String setName) {
        if (myView != null) {
            myView.setName.post(() -> {
                // We need to check the set name doesn't have an inbuilt category
                String[] categoryAndName = mainActivityInterface.getSetActions().getSetCategoryAndName(setName);
                myView.setCategory.setText(categoryAndName[0]);
                myView.setName.setText(categoryAndName[1]);
            });
        }
    }
}
