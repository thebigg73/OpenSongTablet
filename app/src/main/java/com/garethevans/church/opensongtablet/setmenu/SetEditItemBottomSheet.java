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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetSetitemeditBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class SetEditItemBottomSheet extends BottomSheetDialogFragment {

    // This allows the user to select a set item to make it a variation
    private MainActivityInterface mainActivityInterface;
    private BottomSheetSetitemeditBinding myView;
    private final String TAG = "SetEditItemBottomSheet";
    private ArrayList<String> filenames;
    private ArrayList<String> currentSetFolder;
    private int setPosition = 0;
    private View selectedCard;
    private boolean useTitle=true;
    ExposedDropDownArrayAdapter keyAdapter, folderAdapter, filenameAdapter;
    private String edit_set_item_string="", note_string="", variation_string="", scripture_string="",
            slide_string="";
    private String[] key_choice_string = {};

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

        myView.dialogHeading.setText(edit_set_item_string);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Get a copy of the original set folders in case we want to undo
        currentSetFolder = mainActivityInterface.getCurrentSet().getSetFolders();

        // Build the set view items
        buildSetItems(inflater, container);

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
        myView.editKey.setText(mainActivityInterface.getCurrentSet().getKey(0));
        myView.editKey.setUserEditing(true);

        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();
        folders.add("**"+note_string);
        folders.add("**"+variation_string);
        folders.add("**"+scripture_string);
        folders.add("**"+slide_string);
        if (getContext()!=null) {
            folderAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editFolder, R.layout.view_exposed_dropdown_item, folders);
        }
        myView.editFolder.setAdapter(folderAdapter);
        myView.editFolder.setUserEditing(false);
        myView.editFolder.setText(mainActivityInterface.getCurrentSet().getFolder(0));
        myView.editFolder.setUserEditing(true);

        filenames = new ArrayList<>();
        if (getContext()!=null) {
            filenameAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editFilename, R.layout.view_exposed_dropdown_item, filenames);
        }
        myView.editFilename.setAdapter(filenameAdapter);
        updateFilesInFolder(mainActivityInterface.getCurrentSet().getFolder(0));

        checkAllowEdit();
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

    private void updateFilesInFolder(String folder) {
        // Do this check as we might be using Notes, Variations, etc.
        String[] foldersFromNice = mainActivityInterface.getStorageAccess().getActualFoldersFromNice(folder);
        Log.d(TAG,"folderNames: "+foldersFromNice[0]+" / "+foldersFromNice[1]);
        filenames = mainActivityInterface.getStorageAccess().listFilesInFolder(foldersFromNice[0],foldersFromNice[1]);
        if (getContext()!=null) {
            filenameAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.editFilename, R.layout.view_exposed_dropdown_item, filenames);
        }
        myView.editFilename.setAdapter(filenameAdapter);
        myView.editFilename.setUserEditing(false);
        myView.editFilename.setText(mainActivityInterface.getCurrentSet().getFilename(setPosition));
        myView.editFilename.setUserEditing(true);
    }

    private void buildSetItems(LayoutInflater inflater, ViewGroup container) {
        // Firstly show the progressBar (list is hidden already)
        myView.progressBar.setVisibility(View.VISIBLE);
        useTitle = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
        for (int x=0; x<mainActivityInterface.getCurrentSet().getSetItems().size(); x++) {
            // Show all items, but disable the non-songs
            String displayNum = (x+1)+".";
            String folder = mainActivityInterface.getCurrentSet().getFolder(x);
            String title = getTitleAndKey(x,useTitle);
            View cardView = inflater.inflate(R.layout.view_set_item, container);
            if (x==0) {
                selectedCard = cardView;
            }
            // Hide the icon
            ((TextView)cardView.findViewById(R.id.cardview_item)).setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            ((TextView)cardView.findViewById(R.id.cardview_item)).setText(displayNum);
            ((TextView)cardView.findViewById(R.id.cardview_songtitle)).setText(title);
            ((TextView)cardView.findViewById(R.id.cardview_folder)).setText(folder);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.topMargin = 8;
            llp.bottomMargin = 8;
            cardView.setLayoutParams(llp);
            final int position = x;
            cardView.setOnClickListener(v -> {
                selectedCard = v;
                setPosition = position;
                updateEditView();
                Log.d(TAG,"position="+position);
            });
            myView.setList.addView(cardView);
        }
        // Now show the content and hide the progressBar
        myView.setList.setVisibility(View.VISIBLE);
        myView.progressBar.setVisibility(View.GONE);
    }

    private String getTitleAndKey(int position, boolean useTitle) {
        String key = mainActivityInterface.getCurrentSet().getKey(position);
        if (key!=null && !key.equals("null") && !key.isEmpty()) {
            key = " ("+key+")";
        } else {
            key = "";
        }
        String name = useTitle ?
                mainActivityInterface.getCurrentSet().getTitle(position) :
                mainActivityInterface.getCurrentSet().getFilename(position);
        return name + key;
    }

    private void checkAllowEdit() {
        // Only allow key change for song or variation
        String folderChosen = myView.editFolder.getText().toString();
        myView.editKey.setEnabled(folderChosen.startsWith("**Variation") || !folderChosen.startsWith("**"));
    }

    private void updateEditView() {
        myView.editFolder.setUserEditing(false);
        myView.editFilename.setUserEditing(false);
        myView.editKey.setUserEditing(false);
        myView.editFolder.setText(mainActivityInterface.getCurrentSet().getFolder(setPosition));
        myView.editFilename.setText(mainActivityInterface.getCurrentSet().getFilename(setPosition));
        myView.editKey.setText(mainActivityInterface.getCurrentSet().getKey(setPosition));
        myView.editVariation.setChecked(mainActivityInterface.getCurrentSet().getFolder(setPosition).contains("**Variation"));
        myView.editFolder.setUserEditing(true);
        myView.editFilename.setUserEditing(true);
        myView.editKey.setUserEditing(true);
    }

    private void setAsVariation(boolean createVariation) {
        String newFolder;
        if (createVariation) {
            // Make the variation file which also updates the set references
            mainActivityInterface.getSetActions().makeVariation(setPosition);
            // Update the matching card
            newFolder = "**"+variation_string;

        } else {
            // Delete the variation file and put the original folder back?
            Uri variationUri = mainActivityInterface.getStorageAccess().getUriForItem("Variations","",myView.editFilename.getText().toString());
            if (mainActivityInterface.getStorageAccess().uriExists(variationUri)) {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" setAsVariation deleteFile "+variationUri);
                mainActivityInterface.getStorageAccess().deleteFile(variationUri);
            }
            // Update the matching card
            newFolder = currentSetFolder.get(setPosition);
            if (newFolder.startsWith("**")) {
                Log.d(TAG,"newFolder="+newFolder);
                // Try to find a matching song in the database, if not it will return mainfoldername
                newFolder = mainActivityInterface.getSQLiteHelper().getFolderForSong(mainActivityInterface.getCurrentSet().getFilename(setPosition));
            }
            // Fix the item in the set
            String filename = mainActivityInterface.getCurrentSet().getFilename(setPosition);
            String key = mainActivityInterface.getCurrentSet().getKey(setPosition);
            mainActivityInterface.getSetActions().adjustItemInSet(setPosition,newFolder,filename,key);
        }

        // Change the dropdown to match.  This also triggers a change in the card here
        myView.editFolder.setUserEditing(false);
        myView.editFolder.setText(newFolder);
        myView.editFolder.setUserEditing(true);

        // Update the cardview in the setList behind.  Pass position as string in array
        updateCurrentSetView();
    }

    private void updateCurrentSetView() {
        String currentSetString = mainActivityInterface.getSetActions().getSetAsPreferenceString();
        //mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent",currentSetString);

        // TODO remove after fixing weird set behaviour
        //String[] setbits = currentSetString.replace("_**$","SPLIT").split("SPLIT");
        //for (int x=0; x<setbits.length; x++) {
        //    Log.d(TAG,x+". "+setbits[x].replace("$**_",""));
        //}

        ArrayList<String> val = new ArrayList<>();
        val.add(""+setPosition);
        mainActivityInterface.updateFragment("set_updateItem",this,val);
    }

    private class MyTextWatcher implements TextWatcher {

        private final String which;
        private final ExposedDropDown exposedDropDown;
        private MyTextWatcher(ExposedDropDown exposedDropDown, String which) {
            this.exposedDropDown = exposedDropDown;
            this.which = which;
        }

        //private MyTextWatcher(String which) {
        //    this.which = which;
        //}
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable!=null && exposedDropDown!=null && exposedDropDown.getUserEditing()) {
                switch (which) {
                    case "folder":
                        mainActivityInterface.getCurrentSet().setFolder(setPosition, editable.toString());
                        updateFilesInFolder(editable.toString());
                        checkAllowEdit();
                        // Update the matching card
                        ((TextView)selectedCard.findViewById(R.id.cardview_folder)).setText(editable.toString());
                        break;
                    case "filename":
                        mainActivityInterface.getCurrentSet().setFilename(setPosition, editable.toString());
                        break;
                    case "key":
                        mainActivityInterface.getCurrentSet().setKey(setPosition, editable.toString());
                        break;
                }

                // Update the cardview
                ((TextView)selectedCard.findViewById(R.id.cardview_songtitle)).setText(getTitleAndKey(setPosition,useTitle));
                ((TextView)selectedCard.findViewById(R.id.cardview_folder)).setText(mainActivityInterface.getCurrentSet().getFolder(setPosition));

                // Update the cardview in the setList behind.  Pass position as string in array
                updateCurrentSetView();

                // Fix the item in the set
                String folder = mainActivityInterface.getCurrentSet().getFolder(setPosition);
                String filename = mainActivityInterface.getCurrentSet().getFilename(setPosition);
                String key = mainActivityInterface.getCurrentSet().getKey(setPosition);
                mainActivityInterface.getSetActions().adjustItemInSet(setPosition,folder,filename,key);
            }
        }
    }
}
