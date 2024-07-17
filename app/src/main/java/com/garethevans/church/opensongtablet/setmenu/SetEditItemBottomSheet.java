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

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "SetEditItemBottomSheet";
    private final ArrayList<String> arguments = new ArrayList<>();
    // This allows the user to select a set item to make it a variation
    private MainActivityInterface mainActivityInterface;
    private BottomSheetSetitemeditBinding myView;
    private int setPosition = 0;
    private ExposedDropDownArrayAdapter keyAdapter, folderAdapter, filenameAdapter;
    private String edit_set_item_string = "", note_string = "", variation_string = "", scripture_string = "",
            slide_string = "";
    private String[] key_choice_string = {};
    private ArrayList<String> filenames;
    private SetItemInfo setItemInfo;
    private String desiredKey = "";
    private String originalKey = "";
    private String newFolder = "";
    private String newFilename = "";
    private String[] originalFolderFilename;
    private boolean isAnyVariation, isKeyVariation, isNormalVariation;

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

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setText(edit_set_item_string);
        myView.dialogHeading.setClose(this);

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Get the current set item
            setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(setPosition);

            // Set the variation switch based on the folder/filename
            isAnyVariation = mainActivityInterface.getVariations().getIsNormalOrKeyVariation(setItemInfo.songfolder,setItemInfo.songfilename);
            isKeyVariation = mainActivityInterface.getVariations().getIsKeyVariation(setItemInfo.songfolder,setItemInfo.songfilename);
            isNormalVariation = mainActivityInterface.getVariations().getIsNormalVariation(setItemInfo.songfolder,setItemInfo.songfilename);

            // Because the song could be a temporary transposed variation, get the originals
            originalFolderFilename = mainActivityInterface.getVariations().getPreVariationInfo(setItemInfo);

            // Set up the dropdowns
            setupExposedDropdowns();

            // Set up the listeners
            setupListeners();
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            edit_set_item_string = getString(R.string.edit_set_item);
            key_choice_string = getResources().getStringArray(R.array.key_choice);
            note_string = getString(R.string.note);
            variation_string = getString(R.string.variation);
            scripture_string = getString(R.string.scripture);
            slide_string = getString(R.string.slide);
        }
    }

    private void setupExposedDropdowns() {
        // Return to the main thread
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                // The key dropdown
                if (getContext() != null) {
                    keyAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editKey, R.layout.view_exposed_dropdown_item, key_choice_string);
                }
                myView.editKey.setAdapter(keyAdapter);
                myView.editKey.setUserEditing(false);
                myView.editKey.setText(setItemInfo.songkey);
                myView.editKey.setUserEditing(true);

                // The folders dropdown
                ArrayList<String> folders = getFolders();
                if (getContext() != null) {
                    folderAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editFolder, R.layout.view_exposed_dropdown_item, folders);
                }
                myView.editFolder.setAdapter(folderAdapter);

                setupExposedDropdownsPart2();
            }
        });
    }

    private void setupExposedDropdownsPart2() {
        filenames = new ArrayList<>();
        // Try to do as much as possible on a background thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Update the UI on the main thread
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView != null) {
                    myView.editFolder.setUserEditing(false);

                    Log.d(TAG,"setItemInfo:"+setItemInfo.songfolder+"  /  "+setItemInfo.songfilename);
                    Log.d(TAG,"isAnyVariation:"+isAnyVariation+"  isKeyVariation:"+isKeyVariation+"  isNormalVariation:"+isNormalVariation);

                    myView.editVariation.setChecked(isAnyVariation);

                    if (isNormalVariation) {
                        myView.editFolder.setText(setItemInfo.songfolder);
                    } else {
                        myView.editFolder.setText(originalFolderFilename[0]);
                    }
                    myView.editFolder.setEnabled(!myView.editVariation.getChecked());
                    myView.editFolder.setUserEditing(true);


                    myView.editFilename.setEnabled(!myView.editVariation.getChecked());
                    if (getContext() != null) {
                        filenameAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editFilename, R.layout.view_exposed_dropdown_item, filenames);
                    }
                    myView.editFilename.setAdapter(filenameAdapter);

                    // Set the initial value (we have still to populate the dropdown)
                    myView.editFilename.setUserEditing(false);
                    if (isKeyVariation) {
                        myView.editFilename.setText(originalFolderFilename[1]);
                    } else {
                        myView.editFilename.setText(setItemInfo.songfilename);
                    }

                    myView.editFilename.setUserEditing(true);

                    // Now populate and update the filename options dropdown
                    updateFilesInFolder(originalFolderFilename);
                }
            });
        });
    }

    private ArrayList<String> getFolders() {
        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();
        // Remove custom folders and then re-add to put them at the bottom
        folders.remove("**" + note_string);
        folders.remove("**" + variation_string);
        folders.remove("**" + scripture_string);
        folders.remove("**" + slide_string);

        folders.add(0,mainActivityInterface.getMainfoldername());
        if (setItemInfo.songfolder.startsWith("**") || setItemInfo.songfolder.startsWith("../")) {
            folders.add("**" + note_string);
        }
        if (isNormalVariation) {
            folders.add("**" + variation_string);
        }
        if (setItemInfo.songfolder.startsWith("**") || setItemInfo.songfolder.startsWith("../")) {
            folders.add("**" + scripture_string);
            folders.add("**" + slide_string);
        }
        return folders;
    }

    private void setupListeners() {
        myView.editFolder.addTextChangedListener(new MyTextWatcher(myView.editFolder, "folder"));
        myView.editFilename.addTextChangedListener(new MyTextWatcher(myView.editFilename, "filename"));
        myView.editKey.addTextChangedListener(new MyTextWatcher(myView.editKey, "key"));
        myView.editVariation.setOnCheckedChangeListener((compoundButton, b) -> {
            // Change the folder to Variation and create the variation
            // Or change back to the original folder and delete the variation
            setAsVariation(b);
            if (myView != null) {
                myView.editFolder.setEnabled(!b);
            }
        });
        // Disable the variation switch if this is a PDF/Image
        if (setItemInfo.songicon.equals("Image") || setItemInfo.songicon.equals("PDF")) {
            myView.editVariation.setEnabled(false);
        }
    }

    private void updateFilesInFolder(String[] originalFolderFile) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Do this check as we might be using Notes, Variations, etc.
            String[] foldersFromNice = mainActivityInterface.getStorageAccess().getActualFoldersFromNice(originalFolderFile[0]);
            filenames = mainActivityInterface.getStorageAccess().listFilesInFolder(foldersFromNice[0], foldersFromNice[1]);

            mainActivityInterface.getMainHandler().post(() -> {
                if (myView != null) {
                    if (getContext() != null) {
                        filenameAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editFilename, R.layout.view_exposed_dropdown_item, filenames);
                    }
                    myView.editFilename.setAdapter(filenameAdapter);
                    myView.editFilename.setUserEditing(false);
                    if (isKeyVariation) {
                        myView.editFilename.setText(originalFolderFile[1]);
                    } else {
                        myView.editFilename.setText(setItemInfo.songfilename);
                    }
                    myView.editFilename.setUserEditing(true);

                    checkAllowEdit();
                }
            });
        });
    }

    private void checkAllowEdit() {
        // Only allow key change for song or variation
        if (myView != null) {
            String folderChosen = myView.editFolder.getText().toString();
            myView.editKey.setEnabled(folderChosen.startsWith("**Variation") || !folderChosen.startsWith("**"));
        }
    }

    private void setAsVariation(boolean createVariation) {
        // Get a note of the currently chosen key
        if (myView != null) {
            desiredKey = myView.editKey.getText().toString();
            originalKey = mainActivityInterface.getSQLiteHelper().getKey(myView.editFolder.getText().toString(), myView.editFilename.getText().toString());
        } else {
            desiredKey = "";
            originalKey = "";
        }
        newFolder = setItemInfo.songfolder;
        newFilename = setItemInfo.songfilename;

        // Run this on a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            if (createVariation) {
                // Firstly make sure the starting key matches the actual song (as we might be currently using a key variation file)
                // Update the dropdown and item back to this key on the main thread
                setItemInfo.songkey = originalKey;

                if (!desiredKey.equals(originalKey)) {
                    mainActivityInterface.getMainHandler().post(() -> {
                        if (myView != null) {
                            myView.editKey.setUserEditing(false);
                            myView.editKey.setText(originalKey);
                            myView.editKey.setUserEditing(true);
                            setAsVariationPart2();
                        }
                    });
                } else {
                    setAsVariationPart2();
                }
            } else {
                // Delete the variation file and put the original folder back?
                Uri variationUri = mainActivityInterface.getStorageAccess().getUriForItem("Variations", "", myView.editFilename.getText().toString());

                if (mainActivityInterface.getStorageAccess().uriExists(variationUri)) {
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " setAsVariation deleteFile " + variationUri);
                    mainActivityInterface.getStorageAccess().deleteFile(variationUri);
                }

                // Update the matching card
                originalFolderFilename = mainActivityInterface.getVariations().getPreVariationInfo(setItemInfo);
                newFolder = originalFolderFilename[0];
                newFilename = originalFolderFilename[1];

                mainActivityInterface.getMainHandler().post(() -> {
                    if (myView != null) {
                        myView.editFilename.setEnabled(true);
                        myView.editFolder.setEnabled(true);

                        setAsVariationPart3();
                    }
                });
            }
        });
    }


    private void setAsVariationPart2() {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Make the variation file which also updates the set references
            mainActivityInterface.getVariations().makeVariation(setPosition);
            // Get the updated values
            newFolder = setItemInfo.songfolder;
            newFilename = setItemInfo.songfilename;

            setItemInfo.songicon = "Variation";

            // Main thread tasks
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView != null) {
                    // Update the matching card
                    myView.editFilename.setEnabled(false);
                    myView.editFolder.setEnabled(false);
                    myView.editFilename.setUserEditing(false);
                    myView.editFilename.setText(newFilename);
                    myView.editFilename.setUserEditing(true);

                    // Now, if we wanted the variation in a different key to the original song, transpose it
                    if (!desiredKey.equals(originalKey)) {
                        myView.editKey.setText(desiredKey);
                    }

                    setAsVariationPart3();
                }
            });
        });
    }

    private void setAsVariationPart3() {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Fix the item in the set
            setItemInfo.songfolder = newFolder;
            setItemInfo.songfoldernice = newFolder;
            setItemInfo.songfilename = newFilename;

            // Try to guess the icon
            setItemInfo.songicon = mainActivityInterface.getSetActions().getIconIdentifier(newFolder, newFilename);

            // Update the cardview in the setList behind.  Pass position as string in array
            // Update the set item in the background and notify the set menu for changes
            mainActivityInterface.getCurrentSet().setSetItemInfo(setPosition, setItemInfo);
            mainActivityInterface.updateFragment("set_updateItem", null, arguments);

            mainActivityInterface.getMainHandler().post(() -> {
                if (myView != null) {
                    // Change the dropdown to match.  This also triggers a change in the card here
                    myView.editFolder.setUserEditing(false);
                    myView.editFolder.setText(newFolder);
                    myView.editFolder.setUserEditing(true);

                    myView.editFilename.setUserEditing(false);
                    myView.editFilename.setText(newFilename);
                    myView.editFilename.setUserEditing(true);

                    updateCurrentSetView();
                }
            });
        });
    }

    private void updateCurrentSetView() {
        ArrayList<String> val = new ArrayList<>();
        val.add(String.valueOf(setPosition));
        mainActivityInterface.updateFragment("set_updateItem", this, val);
    }

    @Override
    public void onDestroy() {
        myView = null;
        super.onDestroy();
    }

    private class MyTextWatcher implements TextWatcher {

        private final String which;
        private final ExposedDropDown exposedDropDown;

        private MyTextWatcher(ExposedDropDown exposedDropDown, String which) {
            this.exposedDropDown = exposedDropDown;
            this.which = which;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Get a note of the original key if required
            if (charSequence != null) {
                originalKey = charSequence.toString();
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (myView != null) {
                checkAllowEdit();

                if (exposedDropDown.getUserEditing()) {
                    switch (which) {
                        case "folder":
                            if (myView.editFolder.getText() != null) {
                                String folder = myView.editFolder.getText().toString();
                                setItemInfo.songfolder = folder;
                                setItemInfo.songfoldernice = folder;
                                updateFilesInFolder(new String[]{folder, setItemInfo.songfilename});

                            }
                            break;
                        case "filename":
                            if (myView.editFilename.getText() != null) {
                                String filename = myView.editFilename.getText().toString();
                                setItemInfo.songfilename = filename;
                                // Because we have indexed the songs, we can look up the title of the new song
                                Song tempSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(setItemInfo.songfolder, filename);
                                setItemInfo.songtitle = tempSong.getTitle();
                            }
                            break;
                        case "key":
                            if (myView.editKey.getText() != null) {
                                setItemInfo.songkey = myView.editKey.getText().toString();
                            }
                            // If we are creating a variation and now changed key, do that
                            if (myView.editVariation.getChecked()) {
                                Song songToTranspose = new Song();
                                songToTranspose.setFolder(setItemInfo.songfolder);
                                songToTranspose.setFilename(setItemInfo.songfilename);
                                songToTranspose = mainActivityInterface.getLoadSong().doLoadSongFile(songToTranspose, false);
                                songToTranspose = mainActivityInterface.getVariations().makeKeyVariation(songToTranspose, setItemInfo.songkey, false, false);
                                mainActivityInterface.getVariations().makeVariation(setPosition);
                                // Save the transposed variation on top of the original
                                mainActivityInterface.getSaveSong().updateSong(songToTranspose, false);
                            }
                            break;
                    }

                    // Update the set item in the background and notify the set menu for changes
                    mainActivityInterface.getCurrentSet().setSetItemInfo(setPosition, setItemInfo);
                    mainActivityInterface.updateFragment("set_updateItem", null, arguments);

                }
            }
        }
    }
}
