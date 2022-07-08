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
    ExposedDropDownArrayAdapter keyAdapter, folderAdapter, filenameAdapter;

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

        myView.dialogHeading.setText(getString(R.string.edit_set_item));

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

    private void setupExposedDropdowns() {
        keyAdapter = new ExposedDropDownArrayAdapter(requireContext(),myView.editKey,R.layout.view_exposed_dropdown_item,getResources().getStringArray(R.array.key_choice));
        myView.editKey.setAdapter(keyAdapter);
        myView.editKey.setText(mainActivityInterface.getCurrentSet().getKey(0));

        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();
        folders.add("**"+getString(R.string.note));
        folders.add("**"+getString(R.string.variation));
        folders.add("**"+getString(R.string.scripture));
        folders.add("**"+getString(R.string.slide));
        folderAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.editFolder,R.layout.view_exposed_dropdown_item, folders);
        myView.editFolder.setAdapter(folderAdapter);
        myView.editFolder.setText(mainActivityInterface.getCurrentSet().getFolder(0));

        filenames = new ArrayList<>();
        filenameAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.editFilename, R.layout.view_exposed_dropdown_item,filenames);
        myView.editFilename.setAdapter(filenameAdapter);
        updateFilesInFolder(mainActivityInterface.getCurrentSet().getFolder(0));

        checkAllowEdit();
    }

    private void setupListeners() {
        myView.editFolder.addTextChangedListener(new MyTextWatcher("folder"));
        myView.editFilename.addTextChangedListener(new MyTextWatcher("filename"));
        myView.editKey.addTextChangedListener(new MyTextWatcher("key"));
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
        filenameAdapter = new ExposedDropDownArrayAdapter(requireContext(),myView.editFilename,R.layout.view_exposed_dropdown_item,filenames);
        myView.editFilename.setAdapter(filenameAdapter);
        myView.editFilename.setText(mainActivityInterface.getCurrentSet().getFilename(setPosition));
    }

    private void buildSetItems(LayoutInflater inflater, ViewGroup container) {
        // Firstly show the progressBar (list is hidden already)
        myView.progressBar.setVisibility(View.VISIBLE);
        for (int x=0; x<mainActivityInterface.getCurrentSet().getSetItems().size(); x++) {
            // Show all items, but disable the non-songs
            String displayNum = (x+1)+".";
            String folder = mainActivityInterface.getCurrentSet().getFolder(x);
            String title = getTitleAndKey(x);
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

    private String getTitleAndKey(int position) {
        String key = mainActivityInterface.getCurrentSet().getKey(position);
        if (key!=null && !key.equals("null") && !key.isEmpty()) {
            key = " ("+key+")";
        } else {
            key = "";
        }
        return mainActivityInterface.getCurrentSet().getFilename(position) + key;
    }

    private void checkAllowEdit() {
        // Only allow key change for song or variation
        String folderChosen = myView.editFolder.getText().toString();
        myView.editKey.setEnabled(folderChosen.startsWith("**Variation") || !folderChosen.startsWith("**"));
    }

    private void updateEditView() {
        myView.editFolder.setText(mainActivityInterface.getCurrentSet().getFolder(setPosition));
        myView.editFilename.setText(mainActivityInterface.getCurrentSet().getFilename(setPosition));
        myView.editKey.setText(mainActivityInterface.getCurrentSet().getKey(setPosition));
        myView.editVariation.setChecked(mainActivityInterface.getCurrentSet().getFolder(setPosition).contains("**Variation"));
    }

    private void setAsVariation(boolean createVariation) {
        String newFolder;
        if (createVariation) {
            // Make the variation file which also updates the set references
            mainActivityInterface.getSetActions().makeVariation(setPosition);
            // Update the matching card
            newFolder = "**"+getString(R.string.variation);

        } else {
            // Delete the variation file and put the original folder back?
            Uri variationUri = mainActivityInterface.getStorageAccess().getUriForItem("Variations","",myView.editFilename.getText().toString());
            if (mainActivityInterface.getStorageAccess().uriExists(variationUri)) {
                mainActivityInterface.getStorageAccess().deleteFile(variationUri);
            }
            // Update the matching card
            newFolder = currentSetFolder.get(setPosition);
            if (newFolder.startsWith("**")) {
                newFolder = getString(R.string.mainfoldername);
            }
            // Fix the item in the set
            String filename = mainActivityInterface.getCurrentSet().getFilename(setPosition);
            String key = mainActivityInterface.getCurrentSet().getKey(setPosition);
            mainActivityInterface.getSetActions().adjustItemInSet(setPosition,newFolder,filename,key);
        }

        // Change the dropdown to match.  This also triggers a change in the card here
        myView.editFolder.setText(newFolder);

        // Update the cardview in the setList behind.  Pass position as string in array
        updateCurrentSetView();
    }

    private void updateCurrentSetView() {
        String currentSetString = mainActivityInterface.getSetActions().getSetAsPreferenceString();
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent",currentSetString);

        Log.d(TAG,"set: "+currentSetString);

        ArrayList<String> val = new ArrayList<>();
        val.add(""+setPosition);
        mainActivityInterface.updateFragment("set_updateItem",this,val);
    }

    private class MyTextWatcher implements TextWatcher {

        private final String which;

        private MyTextWatcher(String which) {
            this.which = which;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable!=null) {
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
                ((TextView)selectedCard.findViewById(R.id.cardview_songtitle)).setText(getTitleAndKey(setPosition));
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
