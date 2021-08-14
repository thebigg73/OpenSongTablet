package com.garethevans.church.opensongtablet.chords;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetChordsFingeringBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class ChordFingeringBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetChordsFingeringBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final String TAG = "ChordFingeringFragment";
    private ArrayList<String> instruments;
    private ChordDirectory chordDirectory;
    private ChordDisplayProcessing chordDisplayProcessing;

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
            }
        });
        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetChordsFingeringBinding.inflate(inflater, container, false);

        myView.dialogHeader.setClose(this);

        // Initialise the chord directory and processing helpers
        chordDirectory = new ChordDirectory();
        chordDisplayProcessing = new ChordDisplayProcessing();

        // Set up the instrument listener
        setupInstruments();

        // Decide on native or capo chords
        nativeOrCapo();

        // TEST A CHORD
        //myView.chordsGridLayout.addView(chordDisplayProcessing.getChordDiagram(requireContext(), inflater, "G","320033"));
        //myView.chordsGridLayout.addView(chordDisplayProcessing.getChordDiagram(requireContext(), inflater, "D","xx0232"));
        //myView.chordsGridLayout.addView(chordDisplayProcessing.getChordDiagram(requireContext(), inflater, "Eb","111343_1_g_Eb"));

        // Draw the chords to the screen
        drawChords();

        return myView.getRoot();
    }

    private void setupInstruments() {
        instruments = new ArrayList<>();
        instruments.add(getString(R.string.guitar));
        instruments.add(getString(R.string.ukulele));
        instruments.add(getString(R.string.mandolin));
        instruments.add(getString(R.string.banjo4));
        instruments.add(getString(R.string.banjo5));
        instruments.add(getString(R.string.cavaquinho));
        instruments.add(getString(R.string.piano));

        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.instrument, R.layout.view_exposed_dropdown_item, instruments);
        myView.instrument.setAdapter(exposedDropDownArrayAdapter);
        myView.instrument.setText(instrumentPrefToText());
        myView.instrument.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                instrumentTextToPref();
            }
        });
    }

    private void nativeOrCapo() {
        if (!mainActivityInterface.getSong().getCapo().isEmpty()) {
            myView.capoChords.setVisibility(View.VISIBLE);
        } else {
            myView.capoChords.setVisibility(View.GONE);
        }
        myView.capoChords.setOnCheckedChangeListener((compoundButton, b) -> drawChords());
    }

    private String instrumentPrefToText() {
        String pref = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "chordInstrument", "g");
        switch (pref) {
            case "g":
            default:
                return instruments.get(0);
            case "u":
                return instruments.get(1);
            case "m":
                return instruments.get(2);
            case "b":
                return instruments.get(3);
            case "B":
                return instruments.get(4);
            case "c":
                return instruments.get(5);
            case "p":
                return instruments.get(6);
        }
    }

    private void instrumentTextToPref() {
        String text = myView.instrument.getText().toString();
        String pref;
        if (text.equals(instruments.get(0))) {
            pref = "g";
        } else if (text.equals(instruments.get(1))) {
            pref = "u";
        } else if (text.equals(instruments.get(2))) {
            pref = "m";
        } else if (text.equals(instruments.get(3))) {
            pref = "b";
        } else if (text.equals(instruments.get(4))) {
            pref = "B";
        } else if (text.equals(instruments.get(5))) {
            pref = "c";
        } else if (text.equals(instruments.get(6))) {
            pref = "p";
        } else {
            pref = "g";
        }
        mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),
                "chordInstrument", pref);
    }

    private void drawChords() {
        // Clear any chords already there
        myView.chordsGridLayout.removeAllViews();
        chordDisplayProcessing.initialiseArrays();

        // Get the chords in the song
        chordDisplayProcessing.getChordsInSong(mainActivityInterface);

        // If we have a capo set and want to see capo chords, transpose
        if (!mainActivityInterface.getSong().getCapo().isEmpty() && myView.capoChords.isChecked()) {
            chordDisplayProcessing.transposeChordsInSong(mainActivityInterface);
        }

        // Now get the fingerings based on the instrument
        // If the chord isn't found, just don't include it.
        // This could be because it isn't defined or it's a non chord bit of text

        int chordFormat = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "chordFormat", 1);
        chordDisplayProcessing.setFingerings(chordDirectory, myView.instrument.getText().toString(), instruments, chordFormat);

        //  Now we build the chord images and show them
        for (int i=0; i<chordDisplayProcessing.getChordsInSong().size(); i++) {
            myView.chordsGridLayout.addView(
            chordDisplayProcessing.getChordDiagram(requireContext(), getLayoutInflater(),
                    chordDisplayProcessing.getChordsInSong().get(i), chordDisplayProcessing.getFingerings().get(i)));
        }
    }


    // TODO
    // Instrument change not working
    // Make sure chord names are in preferred format (D#/Eb)
    // Hide diagrams that don't have fingerings (currently empty diagrams, horrible names)
    // Custom chords

}
