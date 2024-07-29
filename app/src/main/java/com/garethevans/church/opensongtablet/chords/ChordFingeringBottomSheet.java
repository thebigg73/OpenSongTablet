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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetChordsFingeringBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ChordFingeringBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ChordFingeringBottom";

    private BottomSheetChordsFingeringBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String website_chords_fingering_string="", capo_chords_string="", capo_fret_string="",
            transpose_string="", custom_string="";

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
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetChordsFingeringBinding.inflate(inflater, container, false);

        prepareStrings();

        myView.dialogHeader.setClose(this);
        myView.dialogHeader.setWebHelp(mainActivityInterface,website_chords_fingering_string);

        // Initialise the chord directory and processing helpers
        //chordDisplayProcessing = new ChordDisplayProcessing(getContext());

        // Set up the instrument listener
        setupInstruments();

        // Decide on native or capo chords
        nativeOrCapo();

        // Draw the chords to the screen
        drawChords();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            website_chords_fingering_string = getString(R.string.website_chords_fingering);
            capo_chords_string = getString(R.string.capo_chords);
            capo_fret_string = getString(R.string.capo_fret);
            transpose_string = getString(R.string.transpose);
            custom_string = getString(R.string.custom);
        }
    }
    private void setupInstruments() {

        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.instrument, R.layout.view_exposed_dropdown_item, mainActivityInterface.getChordDisplayProcessing().getInstruments());
            myView.instrument.setAdapter(exposedDropDownArrayAdapter);
        }
        myView.instrument.setText(instrumentPrefToText());
        myView.instrument.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                nativeOrCapo();
                instrumentTextToPref();
                drawChords();
            }
        });
    }

    private void nativeOrCapo() {
        if (mainActivityInterface!=null && mainActivityInterface.getSong()!=null &&
                mainActivityInterface.getSong().getCapo()!=null && !mainActivityInterface.getSong().getCapo().isEmpty() &&
        !myView.instrument.getText().toString().equals(mainActivityInterface.getChordDisplayProcessing().getInstruments().get(6))) {
            // Only for stringed instruments!
            myView.capoChords.setVisibility(View.VISIBLE);
            String capoText = capo_chords_string + " (" + capo_fret_string + " " +
                    mainActivityInterface.getChordDisplayProcessing().getCapoPosition() + ")";
            myView.capoChords.setText(capoText);
        } else if (mainActivityInterface!=null && mainActivityInterface.getSong()!=null &&
                mainActivityInterface.getSong().getCapo()!=null && !mainActivityInterface.getSong().getCapo().isEmpty() &&
                myView.instrument.getText().toString().equals(mainActivityInterface.getChordDisplayProcessing().getInstruments().get(6))) {
            // Piano shows the transpose text instead
            myView.capoChords.setVisibility(View.VISIBLE);
            String capoText = transpose_string + " (+" + mainActivityInterface.getSong().getCapo() + ")";
            myView.capoChords.setText(capoText);
        } else {
            myView.capoChords.setVisibility(View.GONE);
        }
        myView.capoChords.setOnCheckedChangeListener((compoundButton, b) -> drawChords());
    }

    private String instrumentPrefToText() {
        // If this song has a preferred instrument stored, use that
        String pref = mainActivityInterface.getSong().getPreferredInstrument();
        if (pref==null || pref.isEmpty()) {
            pref = mainActivityInterface.getPreferences().getMyPreferenceString(
                    "chordInstrument", "g");
        }
        mainActivityInterface.getMidi().setMidiInstrument(pref);
        return mainActivityInterface.getChordDisplayProcessing().getInstrumentFromPref(pref);
    }

    private void instrumentTextToPref() {
        String pref = mainActivityInterface.getChordDisplayProcessing().getPrefFromInstrument(myView.instrument.getText().toString());
        mainActivityInterface.getMidi().setMidiInstrument(pref);
        mainActivityInterface.getPreferences().setMyPreferenceString(
                "chordInstrument", pref);
    }

    private void drawChords() {
        // Clear any chords already there
        myView.chordsGridLayout.removeAllViews();
        mainActivityInterface.getChordDisplayProcessing().initialiseArrays();

        // Get the chords in the song
        mainActivityInterface.getChordDisplayProcessing().findChordsInSong();

        // If we have a capo set and want to see capo chords, transpose
        if (mainActivityInterface.getSong()!=null &&
                mainActivityInterface.getSong().getCapo()!=null && !mainActivityInterface.getSong().getCapo().isEmpty() && myView.capoChords.isChecked()) {
            mainActivityInterface.getChordDisplayProcessing().transposeChordsInSong();
        }

        // Now get the fingerings based on the instrument
        // If the chord isn't found, just don't include it.
        // This could be because it isn't defined or it's a non chord bit of text

        int chordFormat = mainActivityInterface.getPreferences().getMyPreferenceInt("chordFormat", 1);
        if (!mainActivityInterface.getPreferences().getMyPreferenceBoolean("chordFormatUsePreferred",true)) {
            chordFormat = mainActivityInterface.getSong().getDetectedChordFormat();
        }
        mainActivityInterface.getChordDisplayProcessing().setFingerings(mainActivityInterface.getChordDirectory(), myView.instrument.getText().toString(), mainActivityInterface.getChordDisplayProcessing().getInstruments(), chordFormat);

        //  Now we build the chord images and show them
        //  Piano chords get one chord per row, stringed chords get 3
        if (myView.instrument.getText().toString().equals(mainActivityInterface.getChordDisplayProcessing().getInstruments().get(6))) {
            myView.chordsGridLayout.setColumnCount(1);
        } else {
            myView.chordsGridLayout.setColumnCount(3);
        }

        for (int i=0; i<mainActivityInterface.getChordDisplayProcessing().getChordsInSong().size(); i++) {
            LinearLayout chordLayout;
            if (myView.instrument.getText().toString().equals(mainActivityInterface.getChordDisplayProcessing().getInstruments().get(6))) {
                chordLayout = mainActivityInterface.getChordDisplayProcessing().getChordDiagramPiano(getLayoutInflater(),
                        mainActivityInterface.getChordDisplayProcessing().getChordsInSong().get(i), mainActivityInterface.getChordDisplayProcessing().getFingerings().get(i));
                String thisChordCode = mainActivityInterface.getChordDisplayProcessing().getFingerings().get(i);
                if (chordLayout!=null) {
                    chordLayout.setOnClickListener(v -> mainActivityInterface.getMidi().playMidiNotes(thisChordCode, "standard", 50, 0));
                }
            } else {
                chordLayout = mainActivityInterface.getChordDisplayProcessing().getChordDiagram(getLayoutInflater(),
                        mainActivityInterface.getChordDisplayProcessing().getChordsInSong().get(i), mainActivityInterface.getChordDisplayProcessing().getFingerings().get(i));
                String thisChordCode = mainActivityInterface.getChordDisplayProcessing().getFingerings().get(i);
                if (chordLayout!=null) {
                    chordLayout.setOnClickListener(v -> mainActivityInterface.getMidi().playMidiNotes(thisChordCode, "standard", 200, 0));
                }
            }

            if (chordLayout!=null) {
                myView.chordsGridLayout.addView(chordLayout);
            }
        }

        // Add the custom chords
        if (mainActivityInterface!=null && mainActivityInterface.getSong()!=null &&
                mainActivityInterface.getSong().getCustomchords()!=null && !mainActivityInterface.getSong().getCustomchords().isEmpty()) {
            String[] customChords = mainActivityInterface.getSong().getCustomchords().split(" ");

            for (String chordCode : customChords) {
                String customChordName = custom_string;
                if (chordCode.contains("_")) {
                    customChordName = chordCode.substring(chordCode.lastIndexOf("_"));
                    customChordName = customChordName.replace("_", "");
                }
                LinearLayout customChordLayout = null;
                if (mainActivityInterface.getChordDisplayProcessing().codeMatchesInstrument(chordCode, myView.instrument.getText().toString()) &&
                        myView.instrument.getText().toString().equals(mainActivityInterface.getChordDisplayProcessing().getInstruments().get(6))) {
                    customChordLayout = mainActivityInterface.getChordDisplayProcessing().getChordDiagramPiano(getLayoutInflater(),
                            customChordName, chordCode);
                } else if (mainActivityInterface.getChordDisplayProcessing().codeMatchesInstrument(chordCode, myView.instrument.getText().toString())) {
                    customChordLayout = mainActivityInterface.getChordDisplayProcessing().getChordDiagram(getLayoutInflater(),
                            customChordName, chordCode);
                }
                if (customChordLayout != null) {
                    myView.chordsGridLayout.addView(customChordLayout);
                }
            }
        }
    }

}
