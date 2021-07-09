package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetVariationBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class SetVariationBottomSheet extends BottomSheetDialogFragment {

    // This allows the user to select a set item to make it a variation
    private MainActivityInterface mainActivityInterface;
    private BottomSheetVariationBinding myView;
    private final String TAG = "SetVariationBottomSheet";

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
        myView = BottomSheetVariationBinding.inflate(inflater, container, false);

        myView.dialogHeading.setText(getString(R.string.select_item_variation));

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Build the set view items
        buildSetItems(inflater, container);

        return myView.getRoot();
    }

    private void buildSetItems(LayoutInflater inflater, ViewGroup container) {
        // Firstly show the progressBar (list is hidden already)
        myView.progressBar.setVisibility(View.VISIBLE);
        for (int x=0; x<mainActivityInterface.getCurrentSet().getSetItems().size(); x++) {
            // Show all items, but disable the non-songs
            String displayNum = (x+1)+".";
            String filename = mainActivityInterface.getCurrentSet().getFilename(x);
            String folder = mainActivityInterface.getCurrentSet().getFolder(x);
            String key = mainActivityInterface.getCurrentSet().getKey(x);
            if (key!=null && !key.equals("null") && !key.isEmpty()) {
                key = " ("+key+")";
            } else {
                key = "";
            }
            String title = filename + key;
            View cardView = inflater.inflate(R.layout.view_set_item, container);
            // Hide the icon
            ((TextView)cardView.findViewById(R.id.cardview_item)).setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            ((TextView)cardView.findViewById(R.id.cardview_item)).setText(displayNum);
            ((TextView)cardView.findViewById(R.id.cardview_songtitle)).setText(title);
            ((TextView)cardView.findViewById(R.id.cardview_folder)).setText(folder);
            final int position = x;
            cardView.setOnClickListener(v -> {
                Log.d(TAG,"position="+position);
                // Update the icon in the list to stop us doing it again
                String newFolder = "**" + getString(R.string.variation);
                ((TextView)v.findViewById(R.id.cardview_folder)).setText(newFolder);
                disableCardView(v);

                // Make the variation file which also updates the set references
                mainActivityInterface.getSetActions().makeVariation(getContext(),
                        mainActivityInterface, position);

                // Update the cardview in the setList behind.  Pass position as string in array
                ArrayList<String> val = new ArrayList<>();
                val.add(""+position);
                mainActivityInterface.updateFragment("set_updateItem",this,val);
            });

            if (folder.contains("**") || mainActivityInterface.getStorageAccess().
                            isSpecificFileExtension("imageorpdf", filename)) {
                disableCardView(cardView);
            }
            myView.setList.addView(cardView);
        }
        // Now show the content and hide the progressBar
        myView.setList.setVisibility(View.VISIBLE);
        myView.progressBar.setVisibility(View.GONE);
    }

    private void disableCardView(View cardView) {
        cardView.setEnabled(false);
        cardView.setAlpha(0.8f);
    }
}
