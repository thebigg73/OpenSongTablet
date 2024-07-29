package com.garethevans.church.opensongtablet.songprocessing;

import android.app.Dialog;
import android.content.Context;
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
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;
import com.garethevans.church.opensongtablet.databinding.BottomSheetEditLyricsCopychordsBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class LyricsChordCopyBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetEditLyricsCopychordsBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "LyricsBottomSheet";
    private final String[] sections;
    ArrayList<String> sectionsWithChords, previewsWithChords, previewsAll;
    private final EditSongFragmentLyrics openingFragment;

    public LyricsChordCopyBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        openingFragment = null;
        sections = new String[]{};
        dismiss();
    }

    // Initialise with a reference to the opening fragment
    LyricsChordCopyBottomSheet(EditSongFragmentLyrics openingFragment, String[] sections) {
        this.openingFragment = openingFragment;
        this.sections = sections;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = BottomSheetEditLyricsCopychordsBinding.inflate(inflater,container,false);
        myView.dialogHeading.setClose(this);

        // Prepare chords from sections
        prepareChordsFromSections();

        // Prepare views and listeners
        prepareViews();

        return myView.getRoot();

    }

    private void prepareChordsFromSections() {
        sectionsWithChords = new ArrayList<>();
        previewsAll = new ArrayList<>();
        previewsWithChords = new ArrayList<>();
        for (int x=0; x<sections.length; x++) {
            String section = sections[x];
            String[] lines = section.split("\n");
            if (lines.length > 0) {
                if (lines[0].startsWith("[") && lines[0].contains("]")) {
                    previewsAll.add(". "+lines[0].substring(0,lines[0].indexOf("]")+1));
                } else if (lines[0].length()>6) {
                    // Show an elipsis
                    previewsAll.add(". "+lines[0].substring(0,6)+"...");
                } else {
                    previewsAll.add(". "+lines[0]);
                }
            } else {
                previewsAll.add("");
            }

            boolean hasChords=false;
            for (String line:lines) {
                if (line.startsWith(".")) {
                    hasChords = true;
                    break;
                }
            }
            if (hasChords) {
                sectionsWithChords.add(section);
                previewsWithChords.add(previewsAll.get(x));
            }
        }
    }

    private void fixText(MyMaterialTextView textView) {
        textView.setHintMonospace();
        textView.setHorizontalScroll(true);
    }

    private void updateCopyFrom() {
        if (myView.copyFrom.getText()!=null && !myView.copyFrom.getText().toString().isEmpty()) {
            int position = positionFromText(myView.copyFrom.getText().toString())-1;
            myView.extractFromAll.post(() -> {
                myView.extractFromAll.setHint(sectionsWithChords.get(position));
                updateCopyTo();
            });

        }
    }

    private void updateCopyTo() {
        if (myView.copyTo.getText()!=null && !myView.copyTo.getText().toString().isEmpty() &&
                !sectionsWithChords.isEmpty()) {
            // Get the position number
            int positionFrom = positionFromText(myView.copyFrom.getText().toString())-1;
            int positionTo = positionFromText(myView.copyTo.getText().toString())-1;
            String textToWorkOn = sections[positionTo];
            myView.copyIntoBefore.setHint(textToWorkOn);

            Log.d(TAG,"positionFrom:"+positionFrom+"  positionTo:"+positionTo);
            Log.d(TAG,"textToworkOn:"+textToWorkOn);
            // Now the logic to do the copy!!  Split into lines and work on one at a time
            ArrayList<String> linesToImprove = new ArrayList<>(Arrays.asList(textToWorkOn.split("\n")));
            ArrayList<String> linesToCopy = new ArrayList<>(Arrays.asList((sectionsWithChords.get(positionFrom).split("\n"))));

            // Go through the extract from array and try to insert these chords into the other array
            for (int l=0; l<linesToCopy.size(); l++) {
                if (linesToCopy.get(l).startsWith(".")) {
                    // This is a chord line, so we will compare with the linesToImprove
                    if (linesToImprove.size()>l) {
                        if (linesToImprove.get(l).startsWith(".")) {
                            // Existing line is already a chord line, so replace it
                            linesToImprove.set(l,linesToCopy.get(l));
                        } else {
                            // No chord line, so insert it
                            linesToImprove.add(l,linesToCopy.get(l));
                        }
                    } else {
                        // We've run out of text, so just add the new chord line
                        linesToImprove.add(linesToCopy.get(l));
                    }
                }
            }

            // Now get the improved text
            StringBuilder stringBuilder = new StringBuilder();
            for (String newline:linesToImprove) {
                stringBuilder.append(newline).append("\n");
            }
            myView.copyIntoAfter.setHint(stringBuilder.toString());
        }
    }

    private int positionFromText(String text) {
        if (text.length()>3 && text.contains(".") && text.indexOf(".")<3) {
            text = text.substring(0,text.indexOf(".")).replace(".","").trim();
            text = text.replaceAll("\\D","");
        }
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            return 1;
        }
    }

    private void prepareViews() {
        // Extract the sections that actually have chords!
        ArrayList<String> sectionNum = new ArrayList<>();
        for (int i=0; i<sectionsWithChords.size(); i++) {
            // Get a small preview text for the dropdown list
            sectionNum.add((i+1)+previewsWithChords.get(i));
        }
        ArrayList<String> toSections = new ArrayList<>();
        for (int i=0; i<sections.length; i++) {
            toSections.add((i+1)+previewsAll.get(i));
        }

        if (getContext()!=null && sectionsWithChords!=null && !sectionsWithChords.isEmpty()) {
            ExposedDropDownArrayAdapter copyFromAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.copyFrom, R.layout.view_exposed_dropdown_item, sectionNum);
            myView.copyFrom.setAdapter(copyFromAdapter);
            ExposedDropDownArrayAdapter copyToAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.copyTo, R.layout.view_exposed_dropdown_item, toSections);
            myView.copyTo.setAdapter(copyToAdapter);
            myView.copyIntoAfter.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
        }
        fixText(myView.extractFromAll);
        fixText(myView.copyIntoBefore);
        fixText(myView.copyIntoAfter);

        myView.copyFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCopyFrom();
                //updateCopyTo();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        myView.copyTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //updateCopyFrom();
                updateCopyTo();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (!sectionsWithChords.isEmpty()) {
            myView.copyFrom.setText("1" + previewsWithChords.get(0));
        }
        if (sections.length>0) {
            myView.copyTo.setText("1" + previewsAll.get(0));
        }

        myView.applyChanges.setOnClickListener(v -> {
            String oldText = myView.copyIntoBefore.getHint().toString();
            String newText = myView.copyIntoAfter.getHint().toString();
            if (openingFragment!=null) {
                openingFragment.doCopyChords(oldText, newText);
            }
            dismiss();
        });
    }

}
