package com.garethevans.church.opensongtablet.setmenu;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetSetitemeditBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class SetEditItemBottomSheet extends BottomSheetDialogFragment {

    // This allows the user to select a set item to make it a variation
    private MainActivityInterface mainActivityInterface;
    private BottomSheetSetitemeditBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetEditItemBottomSheet";
    private int setPosition = 0;
    private final ArrayList<String> arguments = new ArrayList<>();
    private ExposedDropDownArrayAdapter keyAdapter, folderAdapter, filenameAdapter;
    private String edit_set_item_string="", note_string="", variation_string="", scripture_string="",
            slide_string="";
    private String[] key_choice_string = {};
    private ArrayList<String> filenames;
    private SetItemInfo setItemInfo;

    @SuppressWarnings("unused")
    SetEditItemBottomSheet() {
        // The default constructor which wasn't set up properly
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Instantiate with the desired item
    SetEditItemBottomSheet(int setPosition) {
        this.setPosition = setPosition;
        arguments.add(String.valueOf(setPosition));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                // Stop dragging
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetSetitemeditBinding.inflate(inflater, container, false);

        prepareStrings();

        // Get the chosen item
        setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(setPosition);

        myView.dialogHeading.setText(edit_set_item_string);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Set up the dropdowns
        setupExposedDropdowns();

        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            edit_set_item_string = getString(R.string.edit_set_item);
            key_choice_string = getResources().getStringArray(R.array.key_choice);
            note_string = getString(R.string.note);
            variation_string = getString(R.string.variation);
            scripture_string = getString(R.string.scripture);
            slide_string = getString(R.string.slide);
        }
    }

    private void setupExposedDropdowns() {
        if (getContext()!=null) {
            keyAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editKey, R.layout.view_exposed_dropdown_item, key_choice_string);
        }
        myView.editKey.setAdapter(keyAdapter);
        myView.editKey.setUserEditing(false);
        myView.editKey.setText(setItemInfo.songkey);
        myView.editKey.setUserEditing(true);

        ArrayList<String> folders = getFolders();

        if (getContext()!=null) {
            folderAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editFolder, R.layout.view_exposed_dropdown_item, folders);
        }

        myView.editFolder.setAdapter(folderAdapter);

        // Because the song could be a temporary transposed variation, get the originals
        String[] originalFolderFilename = mainActivityInterface.getSetActions().getPreVariationFolderFilename(setItemInfo);

        myView.editFolder.setUserEditing(false);
        myView.editFolder.setText(originalFolderFilename[0]);
        myView.editFolder.setUserEditing(true);

        myView.editFolder.setEnabled(!myView.editVariation.getChecked());
        // Set the variation swith based on the filder
        myView.editVariation.setChecked(originalFolderFilename[0].equals("**" + variation_string) || originalFolderFilename[0].equals("**Variations"));
        myView.editFolder.setEnabled(!myView.editVariation.getChecked());
        myView.editFilename.setEnabled(!myView.editVariation.getChecked());

        filenames = new ArrayList<>();
        if (getContext()!=null) {
            filenameAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editFilename, R.layout.view_exposed_dropdown_item, filenames);
        }
        myView.editFilename.setAdapter(filenameAdapter);
        updateFilesInFolder(originalFolderFilename);

        checkAllowEdit();
    }

    private ArrayList<String> getFolders() {
        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();
        // Remove custom folders and then re-add to put them at the bottom
        folders.remove("**" + note_string);
        folders.remove("**" + variation_string);
        folders.remove("**" + scripture_string);
        folders.remove("**" + slide_string);

        folders.add("**" + note_string);
        folders.add("**" + variation_string);
        folders.add("**" + scripture_string);
        folders.add("**" + slide_string);
        return folders;
    }

    private void setupListeners() {
        myView.editFolder.addTextChangedListener(new MyTextWatcher(myView.editFolder,"folder"));
        myView.editFilename.addTextChangedListener(new MyTextWatcher(myView.editFilename,"filename"));
        myView.editKey.addTextChangedListener(new MyTextWatcher(myView.editKey,"key"));
        myView.editVariation.setOnCheckedChangeListener((compoundButton, b) -> {
            // Change the folder to Variation and create the variation
            // Or change back to the original folder and delete the variation
            setAsVariation(b);
            myView.editFolder.setEnabled(!b);
        });
    }

    private void updateFilesInFolder(String[] originalFolderFile) {
        // Do this check as we might be using Notes, Variations, etc.
        String[] foldersFromNice = mainActivityInterface.getStorageAccess().getActualFoldersFromNice(originalFolderFile[0]);
        filenames = mainActivityInterface.getStorageAccess().listFilesInFolder(foldersFromNice[0],foldersFromNice[1]);
        if (getContext()!=null) {
            filenameAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editFilename, R.layout.view_exposed_dropdown_item, filenames);
        }
        myView.editFilename.setAdapter(filenameAdapter);
        myView.editFilename.setUserEditing(false);
        myView.editFilename.setText(originalFolderFile[1]);
        myView.editFilename.setUserEditing(true);
    }

    private void checkAllowEdit() {
        // Only allow key change for song or variation
        String folderChosen = myView.editFolder.getText().toString();
        myView.editKey.setEnabled(folderChosen.startsWith("**Variation") || !folderChosen.startsWith("**"));
    }

    private void setAsVariation(boolean createVariation) {
        Log.d(TAG,"setAsVariation("+createVariation+")");
        String newFolder = setItemInfo.songfolder;
        String newFilename = setItemInfo.songfilename;
        Log.d(TAG,"Original values newFolder:"+newFolder+"  newFilename:"+newFilename);
        if (createVariation) {
            Log.d(TAG,"make the variation)");
            // Make the variation file which also updates the set references
            mainActivityInterface.getSetActions().makeVariation(setPosition);
            // Get the updated values
            newFolder = setItemInfo.songfolder;
            newFilename = setItemInfo.songfilename;
            Log.d(TAG,"Updated values newFolder:"+newFolder+"  newFilename:"+newFilename);
            // Update the matching card
            setItemInfo.songicon = "Variation";
            myView.editFilename.setEnabled(false);
            myView.editFolder.setEnabled(false);
            myView.editFilename.setUserEditing(false);
            myView.editFilename.setText(newFilename);
            myView.editFilename.setUserEditing(true);

        } else {
            // Delete the variation file and put the original folder back?
            Uri variationUri = mainActivityInterface.getStorageAccess().getUriForItem("Variations","",myView.editFilename.getText().toString());

            if (mainActivityInterface.getStorageAccess().uriExists(variationUri)) {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" setAsVariation deleteFile "+variationUri);
                mainActivityInterface.getStorageAccess().deleteFile(variationUri);
            }

            // Update the matching card
            if (newFilename.contains("_")) {
                newFolder = newFilename.substring(0,newFilename.lastIndexOf("_"));
                newFilename = newFilename.replace(newFolder,"").replace("_","");
                newFolder = newFolder.replace("_","/").replace("//","/");
            }

            myView.editFilename.setEnabled(true);
            myView.editFolder.setEnabled(true);
        }

        // Change the dropdown to match.  This also triggers a change in the card here
        myView.editFolder.setUserEditing(false);
        myView.editFolder.setText(newFolder);
        myView.editFolder.setUserEditing(true);

        myView.editFilename.setUserEditing(false);
        myView.editFilename.setText(newFilename);
        myView.editFilename.setUserEditing(true);

        // Fix the item in the set
        setItemInfo.songfolder = newFolder;
        setItemInfo.songfoldernice = newFolder;
        setItemInfo.songfilename = newFilename;

        // Try to guess the icon
        setItemInfo.songicon = mainActivityInterface.getSetActions().getIconIdentifier(myView.editFolder.getText().toString(),myView.editFilename.getText().toString());

        // Update the cardview in the setList behind.  Pass position as string in array
        // Update the set item in the background and notify the set menu for changes
        mainActivityInterface.getCurrentSet().setSetItemInfo(setPosition,setItemInfo);
        mainActivityInterface.updateFragment("set_updateItem",null, arguments);

        updateCurrentSetView();

        Log.d(TAG,"currentSet:"+mainActivityInterface.getCurrentSet().getSetCurrent());
    }

    private void updateCurrentSetView() {
        ArrayList<String> val = new ArrayList<>();
        val.add(String.valueOf(setPosition));
        mainActivityInterface.updateFragment("set_updateItem",this,val);
    }

    private class MyTextWatcher implements TextWatcher {

        private final String which;
        private final ExposedDropDown exposedDropDown;
        private MyTextWatcher(ExposedDropDown exposedDropDown, String which) {
            this.exposedDropDown = exposedDropDown;
            this.which = which;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            checkAllowEdit();


            if (exposedDropDown.getUserEditing()) {
                switch (which) {
                    case "folder":
                        if (myView.editFolder.getText()!=null) {
                            String folder = myView.editFolder.getText().toString();
                            setItemInfo.songfolder = folder;
                            setItemInfo.songfoldernice = folder;
                            updateFilesInFolder(new String[]{folder,setItemInfo.songfilename});
                            checkAllowEdit();
                        }
                        break;
                    case "filename":
                        if (myView.editFilename.getText()!=null) {
                            String filename = myView.editFilename.getText().toString();
                            setItemInfo.songfilename = filename;
                            // Because we have indexed the songs, we can look up the title of the new song
                            Song tempSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(setItemInfo.songfolder,filename);
                            setItemInfo.songtitle = tempSong.getTitle();
                        }
                        break;
                    case "key":
                        if (myView.editKey.getText()!=null) {
                            setItemInfo.songkey = myView.editKey.getText().toString();
                        }
                        break;
                }

                // Update the set item in the background and notify the set menu for changes
                mainActivityInterface.getCurrentSet().setSetItemInfo(setPosition,setItemInfo);
                mainActivityInterface.updateFragment("set_updateItem",null, arguments);

            }
        }
    }
}
