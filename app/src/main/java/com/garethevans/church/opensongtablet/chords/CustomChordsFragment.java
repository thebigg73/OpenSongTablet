package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsChordsCustomBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

import java.util.ArrayList;
import java.util.HashSet;

public class CustomChordsFragment extends Fragment {

    private SettingsChordsCustomBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> customChordCode, customChordsFingering, customChordsFret,
            customChordsName;
    private ArrayList<String> chordsCodeForInstrument, chordsNameForInstrument,
            chordsFretForInstrument, chordsFingeringForInstrument;
    // currentCode is updated as notes are changed.  defaultCode is the loaded/initial notes
    private String currentCode, defaultCode;
    private ArrayList<Boolean> pianoKeysOn;
    private boolean delete = false;
    private int selectedIndex = 0;

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "CustomChordsFrag";
    private String custom_chords_string="", website_chords_custom_string="", piano_string="",
            customchords_name_string="", guitar_string="", banjo5_string="", custom_chord_exists_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(custom_chords_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        myView = SettingsChordsCustomBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_chords_custom_string;

        myView.instrument.setFocusable(false);
        myView.chordName.setFocusable(false);
        // Get the chords in the song
        getChordsInSong();

        // Check which chords aren't in the database
        getCurrentCustomChords();

        // Set up instruments
        setupInstruments();

        // Update the exposed dropdown with any chords for this instrument
        updateCustomChordDropDown();

        // Set up listeners
        setupListeners();

        // Build the guitar frets from the child layouts
        // Show the correct chord diagram (or hide if no custom chord)
        // Build the chord
        if (isPiano()) {
            adjustPianoVisibility();
            vtoPiano();
            buildPiano();
            displayPianoChord();
        } else {
            adjustGuitarVisibility();
            vtoGuitar();
            buildGuitarFrets();
            displayGuitarChord();
        }

        canShowSave();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            custom_chords_string = getString(R.string.custom_chords);
            website_chords_custom_string = getString(R.string.website_chords_custom);
            customchords_name_string = getString(R.string.customchords_name);
            guitar_string = getString(R.string.guitar);
            banjo5_string = getString(R.string.banjo5);
            piano_string = getString(R.string.piano);
            custom_chord_exists_string = getString(R.string.custom_chord_exists);
        }
    }

    // Get info from the song
    private void getChordsInSong() {
        StringBuilder chordsInSongBuilder = new StringBuilder();
        StringBuilder chordsNotInDatabaseBuilder = new StringBuilder();
        int chordFormat = mainActivityInterface.getSong().getDetectedChordFormat();

        // Figure out the chords in the song
        mainActivityInterface.getChordDisplayProcessing().findChordsInSong();

        // Now go through each one in turn
        for (String chord:mainActivityInterface.getChordDisplayProcessing().getChordsInSong()) {
            // Chords are encoded, so remove the $
            chord = chord.replace("$","");
            chordsInSongBuilder.append(chord).append(", ");
            // Any chords in the database exist for all instruments.  Check piano
            String piano = mainActivityInterface.getChordDirectory().pianoChords(chordFormat,chord);
            if (piano.startsWith("_p")) {
                // No notes, so no chord found
                chordsNotInDatabaseBuilder.append(chord).append(", ");
            }

        }
        String chordsFound = chordsInSongBuilder.toString();
        String chordsMissing = chordsNotInDatabaseBuilder.toString();


        if (chordsFound.contains(", ")) {
            chordsFound = chordsFound.substring(0, chordsFound.lastIndexOf(", "));
            chordsFound = chordsFound.trim();
        }
        if (chordsMissing.contains(", ")) {
            chordsMissing = chordsMissing.substring(0, chordsMissing.lastIndexOf(", "));
            chordsMissing = chordsMissing.trim();
        }

        if (!chordsFound.isEmpty()) {
            myView.chordsInSong.setHint(chordsFound);
            myView.chordsInSong.setVisibility(View.VISIBLE);
        } else {
            myView.chordsInSong.setVisibility(View.GONE);
        }
        if (!chordsMissing.isEmpty()) {
            myView.chordsMissing.setHint(chordsMissing);
            myView.chordsMissing.setVisibility(View.VISIBLE);
        } else {
            myView.chordsMissing.setVisibility(View.GONE);
        }
    }
    private void getCurrentCustomChords() {
        // If the songs already has some, we'll add them to the array.
        customChordCode = new ArrayList<>();
        customChordsFingering = new ArrayList<>();
        customChordsFret = new ArrayList<>();
        customChordsName = new ArrayList<>();
        if (mainActivityInterface.getSong().getCustomchords()!=null &&
            !mainActivityInterface.getSong().getCustomchords().isEmpty()) {
            // They are split by a space, so add to this array
            String[] chordsSaved = mainActivityInterface.getSong().getCustomchords().split(" ");
            for (String chord:chordsSaved) {
                // The chord is {fingering/notes}_{fret not piano}_{instrument}_{chord name}
                String[] chordBits = chord.split("_");

                // Only proceed if the custom chord has enough bits!
                if (chordBits.length>=3) {
                    customChordCode.add(chord);
                    customChordsFingering.add(chordBits[0]);
                    if (chordBits[1].equals("p") && chordBits.length==3) {
                        // Piano
                        customChordsFret.add("");
                        customChordsName.add(chordBits[2]);
                    } else if (chordBits.length==4) {
                        // Stringed instrument
                        customChordsFret.add(chordBits[1]);
                        customChordsName.add(chordBits[3]);
                    }
                }
            }
        }
    }

    // Set up the drop down menus
    private void setupInstruments() {
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.instrument, R.layout.view_exposed_dropdown_item,
                    mainActivityInterface.getChordDisplayProcessing().getInstruments());
            myView.instrument.setAdapter(exposedDropDownArrayAdapter);
        }
        String instrumentPref = mainActivityInterface.getPreferences().getMyPreferenceString(
                "chordInstrument", "g");
        myView.instrument.setText(mainActivityInterface.getChordDisplayProcessing().getInstrumentFromPref(instrumentPref));
    }
    private void updateCustomChordDropDown() {
        chordsCodeForInstrument = new ArrayList<>();
        chordsNameForInstrument = new ArrayList<>();
        chordsFretForInstrument = new ArrayList<>();
        chordsFingeringForInstrument = new ArrayList<>();
        String chordPref = mainActivityInterface.getChordDisplayProcessing().
                getPrefFromInstrument(myView.instrument.getText().toString());
        for (int i=0; i<customChordCode.size(); i++) {
            if (customChordCode.get(i).contains("_"+chordPref+"_")) {

                chordsCodeForInstrument.add(customChordCode.get(i));
                chordsNameForInstrument.add(customChordsName.get(i));
                chordsFretForInstrument.add(customChordsFret.get(i));
                chordsFingeringForInstrument.add(customChordsFingering.get(i));
            }
        }
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.chordName, R.layout.view_exposed_dropdown_item, chordsNameForInstrument);
            myView.chordName.setAdapter(exposedDropDownArrayAdapter);
        }

        if (selectedIndex>-1 && chordsNameForInstrument.size()>selectedIndex) {
            // We've already selected a chord in this menu
            myView.chordName.setText(chordsNameForInstrument.get(selectedIndex));
            currentCode = chordsCodeForInstrument.get(selectedIndex);

        } else if (chordsNameForInstrument.size()>0) {
            // Nothing previously chosen, but chords exist
            myView.chordName.setText(chordsNameForInstrument.get(0));
            currentCode = chordsCodeForInstrument.get(0);

        } else {
            // Nothing in the menu
            myView.chordName.setText("");
            currentCode = "";
            selectedIndex = -1;
        }
        defaultCode = currentCode;
    }

    // Listeners for the menus and options
    private void setupListeners() {

        myView.instrument.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                // Save the chosen instrument as our preference
                String pref = mainActivityInterface.getChordDisplayProcessing().getPrefFromInstrument(editable.toString());
                mainActivityInterface.getPreferences().setMyPreferenceString("chordInstrument", pref);
                mainActivityInterface.getMidi().setMidiInstrument(pref);

                // This building part draws and measures
                // Once measured, the views are shown, not hidden

                if (isPiano()) {
                    myView.guitarChordLayout.setVisibility(View.GONE);
                    myView.pianoChordLayout.piano.setVisibility(View.INVISIBLE);
                } else {
                    myView.pianoChordLayout.piano.setVisibility(View.GONE);
                    myView.guitarChordLayout.setVisibility(View.INVISIBLE);
                }

                // Update the list of chords available for edit (that match this instrument)
                // Because this sets the chordName selected as well, this triggers that listener
                // This will cause views to be drawn, etc.
                selectedIndex = -1;
                updateCustomChordDropDown();
            }
        });

        myView.chordName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable!=null && !editable.toString().isEmpty()) {
                    selectedIndex = chordsNameForInstrument.indexOf(editable.toString());
                    if (isPiano()) {
                        // Build the piano view which includes the vto
                        buildPiano();
                        // Build the chord
                        displayPianoChord();
                    } else {
                        // Set the VTO for when the view is ready
                        vtoGuitar();
                        // Build the guitar view
                        buildGuitarFrets();
                        // Build the chord
                        displayGuitarChord();
                    }
                } else {
                    currentCode = "";
                    myView.customCode.setHint("");
                    selectedIndex = -1;
                }

                // Decide if we should show the save button as there has been a change
                canShowSave();
            }
        });
        myView.save.setOnClickListener(v -> doSave());
        myView.deleteChord.setOnClickListener(v -> {
            if (customChordsName.size()>0 && !myView.chordName.getText().toString().isEmpty()) {
                // Simply set this chord code to empty, then trigger the save which replaces it with nothing!
                myView.customCode.setHint("");
                delete = true;
                doSave();
            }
        });
        myView.newChord.setOnClickListener(v -> {
            // Open the bottom sheet dialog and get the text back from the MainActivity
            TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(this,
                    "CustomChordsFragment",custom_chords_string,customchords_name_string,null,
                    null,null,true);
            textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"textInputBottomSheet");
        });
        myView.playChord.setOnClickListener(v -> mainActivityInterface.getMidi().playMidiNotes(myView.customCode.getHint().toString(),"standard",200,800));
    }

    // Simple getters based on the instrument chosen
    private int numberOfStrings() {
        String currInstr = myView.instrument.getText().toString();
        if (currInstr.equals(guitar_string)) {
            return 6;
        } else if (currInstr.equals(banjo5_string)) {
            return 5;
        } else if (isPiano()) {
            return 0;
        } else {
            return 4;
        }
    }
    private boolean isPiano() {
        return myView.instrument.getText().toString().equals(piano_string);
    }

    // Deal with the string display for guitar, etc
    private void adjustGuitarVisibility() {
        // The view we want is set to invisible, so it can be drawn, measured, scaled
        // The associated VTO will set to visible once done
        if (currentCode!=null && !currentCode.isEmpty()) {
            myView.guitarChordLayout.setVisibility(View.INVISIBLE);
        } else {
            myView.guitarChordLayout.setVisibility(View.GONE);
        }
        // Hide the piano completely
        myView.pianoChordLayout.piano.setVisibility(View.GONE);
    }
    private void buildGuitarFrets() {
        myView.guitarChordLayout.removeAllViews();

        // Set the string markers (and space) for the first row
        if (getContext()!=null) {
            TableRow markers = new TableRow(getContext());
            TextView textViewSpacer = new TextView(getContext());
            textViewSpacer.setId(View.generateViewId());
            markers.addView(textViewSpacer);

            for (int markerpos = 1; markerpos < numberOfStrings() + 1; markerpos++) {
                TextView marker = getLayoutInflater().inflate(R.layout.view_string_marker, markers).
                        findViewById(R.id.stringMarker);
                marker.setTag("stringMarker" + markerpos);
                marker.setText("o");
                marker.setId(View.generateViewId());
                marker.setLayoutParams(new TableRow.LayoutParams(markerpos));
                marker.setOnClickListener(v -> stringMarkerListener(v.getTag().toString()));
            }
            myView.guitarChordLayout.addView(markers);

            // Now add the strings for 5 frets
            for (int fret = 1; fret < 6; fret++) {
                TableRow frets = new TableRow(getContext());
                frets.setId(View.generateViewId());
                TextView textView;
                if (fret == 1) {
                    textView = getLayoutInflater().inflate(R.layout.view_chord_fret_marker, frets).
                            findViewById(R.id.fretMarker);
                    textView.setTag("fretMarker");
                    textView.setText("1");
                    textView.setOnClickListener(v -> increaseFretNumber());

                } else {
                    textView = new TextView(getContext());
                    frets.addView(textView);
                    textView.setTag("spacerFret" + fret);
                }
                textView.setId(View.generateViewId());
                textView.setLayoutParams(new TableRow.LayoutParams(0));

                for (int string = 1; string < numberOfStrings() + 1; string++) {
                    View view;
                    if (string == 1) {
                        view = getLayoutInflater().inflate(R.layout.view_chord_string_left, frets, false);
                    } else if (string == numberOfStrings()) {
                        view = getLayoutInflater().inflate(R.layout.view_chord_string_right, frets, false);
                    } else {
                        view = getLayoutInflater().inflate(R.layout.view_chord_string_middle, frets, false);
                    }
                    String stringTag = "fret" + fret + "_string" + string;
                    view.setTag(stringTag);
                    view.findViewById(R.id.stringOn).setTag("fret" + fret + "_stringOn" + string);
                    view.setLayoutParams(new TableRow.LayoutParams(string));
                    view.setOnClickListener(v -> stringNoteListener(view.getTag().toString()));
                    frets.addView(view);
                }
                myView.guitarChordLayout.addView(frets);
            }
        }
    }
    private void setMarkerText(String tag, String text) {
        if (myView.guitarChordLayout.findViewWithTag(tag)!=null) {
            ((TextView) myView.guitarChordLayout.findViewWithTag(tag)).setText(text);
        }
    }
    private void vtoGuitar() {
        myView.guitarChordLayout.getViewTreeObserver().
                addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getActivity() != null) {
                    try {
                        // Measure the layout
                        int childWidth = myView.guitarChordLayout.getMeasuredWidth();
                        int childHeight = myView.guitarChordLayout.getMeasuredHeight();
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int width = displayMetrics.widthPixels;
                        if (childWidth > 0) {
                            float scale = ((float) width / 2f) / (float) childWidth;
                            myView.guitarChordLayout.setGravity(Gravity.CENTER | Gravity.TOP);
                            ViewGroup.LayoutParams layoutParams = myView.guitarChordLayout.getLayoutParams();
                            layoutParams.height = (int) (childHeight * scale);
                            myView.guitarChordLayout.setPivotX(childWidth / 2f);
                            myView.guitarChordLayout.setPivotY(0);
                            myView.guitarChordLayout.setScaleX(scale);
                            myView.guitarChordLayout.setScaleY(scale);
                            myView.guitarChordLayout.setLayoutParams(layoutParams);
                            myView.guitarChordLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            myView.layout.invalidate();
                            myView.guitarChordLayout.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private TextView getFretMarker() {
        return myView.guitarChordLayout.findViewWithTag("fretMarker");
    }
    private String getFretMarkerText() {
        if (getFretMarker()!=null && getFretMarker().getText()!=null) {
            return getFretMarker().getText().toString();
        } else {
            return "";
        }
    }
    private void displayGuitarChord() {
        if (!myView.chordName.getText().toString().isEmpty()) {
            int which = chordsNameForInstrument.indexOf(myView.chordName.getText().toString());
            if (which >= 0) {
                // Update the code
                currentCode = chordsCodeForInstrument.get(which);
                myView.customCode.setHint(currentCode);
                // Set the fret marker
                setMarkerText("fretMarker",chordsFretForInstrument.get(which));
                // Now go through the strings
                String[] notes = chordsFingeringForInstrument.get(which).split("");
                for (int i = 1; i < numberOfStrings() + 1; i++) {
                    // The notes index starts at 0, not 1, so decrease by 1
                    if (notes.length >= i && notes[i - 1] != null && notes[i - 1].equals("x")) {
                        setMarkerText("stringMarker" + i, "x");
                        hideNotesOnString(i);
                    } else if (notes.length >= i && notes[i - 1] != null && notes[i - 1].equals("0")) {
                        setMarkerText("stringMarker" + i, "o");
                        hideNotesOnString(i);
                    } else if (notes.length >= i && notes[i - 1] != null &&
                            myView.guitarChordLayout.findViewWithTag("stringMarker"+i)!=null) {
                        ((TextView)myView.guitarChordLayout.findViewWithTag("stringMarker" + i)).setText("");
                        if (!notes[i - 1].isEmpty()) {
                            try {
                                int fret = Integer.parseInt(notes[i - 1]);
                                markString("fret" + fret + "_string" + i);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                currentCode = "";
                myView.customCode.setHint("");
            }
        } else {
            currentCode = "";
            myView.customCode.setHint("");
        }
    }

    // Listeners for clicking on the guitar frets/markers
    private void stringMarkerListener(String tag) {
        // The string is the end of the tag
        int string = Integer.parseInt(tag.replace("stringMarker",""));

        // Get the current value
        String currVal = ((TextView)myView.guitarChordLayout.findViewWithTag(tag)).getText().toString();
        if (currVal.equals("x")) {
            currVal = "o";
        } else {
            currVal = "x";
        }
        setMarkerText(tag,currVal);

        // Make sure all frets on this string are hidden
        hideNotesOnString(string);
        getGuitarCode();
    }
    private void hideNotesOnString(int string) {
        // Make sure all frets on this string are hidden
        for (int fret=1; fret<6; fret++) {
            String tagNote = "fret"+fret+"_stringOn"+string;
            if (myView.guitarChordLayout.findViewWithTag(tagNote)!=null) {
                myView.guitarChordLayout.findViewWithTag(tagNote).setVisibility(View.INVISIBLE);
            }
        }
    }
    private void stringNoteListener(String tag) {
        markString(tag);
        getGuitarCode();
    }
    private void markString(String tag) {
        // Tag is in the format fret1_string1
        String[] tagbits = tag.split("_");
        int fret = Integer.parseInt(tagbits[0].replace("fret",""));
        int string = Integer.parseInt(tagbits[1].replace("string",""));

        // The user has clicked on a string position, so clear any others
        String stringMarkerTag = "stringMarker"+string;
        if (myView.guitarChordLayout.findViewWithTag(stringMarkerTag)!=null) {
            ((TextView) myView.guitarChordLayout.findViewWithTag(stringMarkerTag)).setText("");
        }
        for (int i=1; i<6; i++) {
            String stringTag = "fret"+i+"_stringOn"+string;
            View view = myView.guitarChordLayout.findViewWithTag(stringTag);
            if (view!=null && i==fret) {
                view.setVisibility(View.VISIBLE);
            } else if (view!=null) {
                view.setVisibility(View.INVISIBLE);
            }
        }
    }
    private void increaseFretNumber() {
        // Get the current fret
        int fret = Integer.parseInt(getFretMarkerText());
        if (fret==11) {
            fret=1;
        } else {
            fret++;
        }
        String newFret = String.valueOf(fret);
        getFretMarker().setText(newFret);
        getGuitarCode();
    }
    private void getGuitarCode() {
        if (!myView.chordName.getText().toString().isEmpty()) {
            // The chord is {fingering/notes}_{fret not piano}_{instrument}_{chord name}
            // First get the fret for each string
            StringBuilder stringBuilder = new StringBuilder();
            for (int string = 1; string < numberOfStrings() + 1; string++) {
                String marker = ((TextView) myView.guitarChordLayout.findViewWithTag("stringMarker" + string)).
                        getText().toString();
                if (marker.equals("x")) {
                    stringBuilder.append(marker);
                } else if(marker.equals("o") || marker.equals("0")) {
                    stringBuilder.append("0");
                } else {
                    // Look for fret positions
                    for (int fret = 1; fret < 6; fret++) {
                        View stringPos = myView.guitarChordLayout.findViewWithTag("fret" + fret + "_stringOn" + string);
                        if (stringPos.getVisibility() == View.VISIBLE) {
                            stringBuilder.append((fret));
                        }
                    }
                }
            }
            // Update the text
            String codeString = stringBuilder + "_" + getFretMarkerText() + "_" +
                    mainActivityInterface.getChordDisplayProcessing().
                            getPrefFromInstrument(myView.instrument.getText().toString()) +
                    "_" + myView.chordName.getText().toString();
            myView.customCode.setHint(codeString);

            currentCode = codeString;
            canShowSave();
        }
    }

    // Deal with the piano display
    private void buildPiano() {
        // This measures the piano view and scales
        resetPianoNotes();
        setPianoListeners();
        vtoPiano();
    }
    private void getPianoNotes() {
        // Go through the boolean array and get the notes as a string
        StringBuilder stringBuilder = new StringBuilder();
        for (int x=0;x<pianoKeysOn.size();x++) {
            if (pianoKeysOn.get(x)) {
                stringBuilder.append(mainActivityInterface.getChordDisplayProcessing().
                        getPianoNotesArray().get(x)).append(",");
            }
        }
        // Remove the final ","
        String fingering = stringBuilder.toString();
        if (fingering.endsWith(",")) {
            fingering = fingering.substring(0,fingering.lastIndexOf(","));
        }
        currentCode = fingering + "_p_" + myView.chordName.getText();
        myView.customCode.setHint(currentCode);
        // Check is save should be shown
        canShowSave();
    }
    private void adjustPianoVisibility() {
        // The view we want is set to invisible, so it can be drawn, measured, scaled
        // The associated VTO will set to visible once done
        if (currentCode!=null && !currentCode.isEmpty()) {
            myView.pianoChordLayout.piano.setVisibility(View.INVISIBLE);
        } else {
            myView.pianoChordLayout.piano.setVisibility(View.GONE);
        }
        // Hide the guitar completely
        myView.guitarChordLayout.setVisibility(View.GONE);
    }
    private void displayPianoChord() {
        // Set the boolean key on array to all be false for now
        pianoKeysOn = new ArrayList<>();
        for (int x=0; x<=28; x++) {
            pianoKeysOn.add(false);
        }

        if (!myView.chordName.getText().toString().isEmpty()) {
            int which = chordsNameForInstrument.indexOf(myView.chordName.getText().toString());
            if (which >= 0) {
                // Update the code
                currentCode = chordsCodeForInstrument.get(which);
                defaultCode = currentCode;
                myView.customCode.setHint(defaultCode);
                // The piano notes will be in the format of A,C#,E_p
                String[] notes = chordsFingeringForInstrument.get(which).split(",");
                // Go through each note and colour tint the view
                // Get the starting position for the first note in the array
                int start = mainActivityInterface.getChordDisplayProcessing().getPianoNotesArray().indexOf(notes[0]);
                int noteToFind = 0;
                if (start != -1) {
                    for (int x = start; x < mainActivityInterface.getChordDisplayProcessing().
                            getPianoKeysArray().size(); x++) {
                        // Look for the remaining positions in the notesArray
                        if (noteToFind < notes.length && mainActivityInterface.getChordDisplayProcessing().
                                getPianoNotesArray().get(x).equals(notes[noteToFind])) {
                            mainActivityInterface.getChordDisplayProcessing().tintDrawable(
                                    myView.pianoChordLayout.piano.findViewById(mainActivityInterface.getChordDisplayProcessing().
                                            getPianoKeysArray().get(x)), notes[noteToFind], true);
                            // Add the piano key array true value for this key
                            pianoKeysOn.set(x,true);
                            noteToFind++;  // Once we've found them all, this won't get called again
                        } else if (noteToFind < notes.length) {
                            mainActivityInterface.getChordDisplayProcessing().tintDrawable(
                                    myView.pianoChordLayout.piano.findViewById(mainActivityInterface.getChordDisplayProcessing().
                                            getPianoKeysArray().get(x)),
                                    mainActivityInterface.getChordDisplayProcessing().
                                            getPianoNotesArray().get(x), false);
                            pianoKeysOn.set(x,false);
                        }
                    }
                } else {
                    resetPianoNotes();
                }
            } else {
                currentCode = "";
                myView.customCode.setHint("");
                resetPianoNotes();
            }
        } else {
            currentCode = "";
            myView.customCode.setHint("");
        }
    }

    private void resetPianoNotes() {
        if (pianoKeysOn==null || pianoKeysOn.size()==0) {
            pianoKeysOn = new ArrayList<>(mainActivityInterface.getChordDisplayProcessing().getPianoNotesArray().size());
        }
        for (int x=0; x<mainActivityInterface.getChordDisplayProcessing().getPianoKeysArray().size(); x++) {
            mainActivityInterface.getChordDisplayProcessing().tintDrawable(
                    myView.pianoChordLayout.piano.findViewById(mainActivityInterface.getChordDisplayProcessing().
                            getPianoKeysArray().get(x)), mainActivityInterface.getChordDisplayProcessing().getPianoNotesArray().get(x), false);
            if (pianoKeysOn.size()>x) {
                pianoKeysOn.set(x, false);
            }
        }
    }

    // Listeners for clicking on the piano keyboard
    private void setPianoListeners() {
        // Go through each key and add a listener
        for (int pos=0; pos<mainActivityInterface.getChordDisplayProcessing().getPianoKeysArray().size(); pos++) {
            ImageView imageView = myView.pianoChordLayout.piano.findViewById(mainActivityInterface.
                    getChordDisplayProcessing().getPianoKeysArray().get(pos));
            if (imageView!=null) {
                int finalPos = pos;
                imageView.setOnClickListener(v -> {
                    // Change the array value to the opposite of what is currently is
                    pianoKeysOn.set(finalPos,!pianoKeysOn.get(finalPos));
                    // Now update the tints
                    mainActivityInterface.getChordDisplayProcessing().tintDrawable(
                            myView.pianoChordLayout.piano.findViewById(mainActivityInterface.
                                    getChordDisplayProcessing().getPianoKeysArray().get(finalPos)),
                            mainActivityInterface.getChordDisplayProcessing().getPianoNotesArray().
                                    get(finalPos),pianoKeysOn.get(finalPos));
                    getPianoNotes();
                });
            }
        }
    }
    private void vtoPiano() {
        myView.pianoChordLayout.piano.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Measure the layout
                try {
                    int childWidth = myView.pianoChordLayout.piano.getMeasuredWidth();
                    int childHeight = myView.pianoChordLayout.piano.getMeasuredHeight();
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int width = displayMetrics.widthPixels;
                    double padding = Math.ceil(16 * getResources().getDisplayMetrics().density);
                    if (childWidth>0) {
                        float scale = ((float) (width - 2 * padding)) / (float) childWidth;
                        myView.pianoChordLayout.piano.setGravity(Gravity.CENTER | Gravity.TOP);
                        ViewGroup.LayoutParams layoutParams = myView.pianoChordLayout.piano.getLayoutParams();
                        layoutParams.height = (int) (childHeight * scale);
                        myView.pianoChordLayout.piano.setPivotX(childWidth / 2f);
                        myView.pianoChordLayout.piano.setPivotY(0);
                        myView.pianoChordLayout.piano.setScaleX(scale);
                        myView.pianoChordLayout.piano.setScaleY(scale);
                        myView.pianoChordLayout.piano.setLayoutParams(layoutParams);
                        myView.pianoChordLayout.piano.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        myView.layout.invalidate();
                        myView.pianoChordLayout.piano.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Deal with save logic
    private void canShowSave() {
        // We can only save if we meet the following criteria
        // 1. We have a name in the dropdown
        // 2. The current chord code is different to the one loaded up
        boolean name = !myView.chordName.getText().toString().isEmpty();
        boolean diff = !currentCode.equals(defaultCode);
        if (name && diff) {
            myView.save.show();
        } else {
            myView.save.hide();
        }
    }
    private void doSave() {
        // Replace the currently edited chord
        // Remove the default code (original)
        if (!currentCode.equals(defaultCode)) {
            customChordCode.remove(defaultCode);
        }
        int position = customChordCode.indexOf(currentCode);
        if (position>-1) {
            if (delete) {
                delete = false;
                customChordCode.set(position, "");
            } else {
                customChordCode.set(position, currentCode);
            }
        } else if (currentCode!=null && !currentCode.isEmpty()){
            if (!delete) {
                customChordCode.add(0, currentCode);
            }
        }

        // Go through the arrayLists and build the custom chords
        StringBuilder customChordText = new StringBuilder();
        // Remove duplicates
        HashSet<String> hashSet = new HashSet<>(customChordCode);
        customChordCode.clear();
        customChordCode.addAll(hashSet);
        for (String code:customChordCode) {
            if (!code.trim().isEmpty()) {
                customChordText.append(code).append(" ");
            }
        }
        mainActivityInterface.getSong().setCustomChords(customChordText.toString().trim());
        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
        currentCode = "";


        // Load the chords in the song back up as a custom chord might fix a chord not in the database
        getChordsInSong();

        // Hide the views
        if (isPiano()) {
            myView.guitarChordLayout.setVisibility(View.GONE);
            myView.pianoChordLayout.piano.setVisibility(View.INVISIBLE);
        } else {
            myView.pianoChordLayout.piano.setVisibility(View.GONE);
            myView.guitarChordLayout.setVisibility(View.INVISIBLE);
        }

        // Rebuild the arrays with new chords
        getCurrentCustomChords();
        updateCustomChordDropDown();
    }

    // Added a new custom chord name for this instrument via a BottomSheet/MainActivity
    public void updateValue(String newChord) {
        // Received from the textInputBottomSheet via the MainActivity
        if (newChord!=null) {
            // Only allow if this chord doesn't already exist for this instrument
            String instrCode = "_" + mainActivityInterface.getChordDisplayProcessing().
                    getPrefFromInstrument(myView.instrument.getText().toString()) + "_" + newChord;
            boolean alreadyExists = false;
            for (int x=0; x<customChordCode.size(); x++) {
                if (customChordCode.get(x).contains(instrCode)) {
                    alreadyExists = true;
                }
            }
            if (alreadyExists) {
                mainActivityInterface.getShowToast().doIt(custom_chord_exists_string);
            } else {
                // Build a default chord
                if (isPiano()) {
                    defaultCode = instrCode;
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int x=1; x<numberOfStrings()+1; x++) {
                        stringBuilder.append("0");
                    }
                    defaultCode = stringBuilder + "_1" + instrCode;
                }

                // Set the new code to the code hint and trigger the save
                // Because it doesn't match the currentCode, it won't overwrite anything
                //currentCode = "new_chord";
                currentCode = defaultCode;
                myView.customCode.setHint(defaultCode);
                doSave();
            }
        }
    }

}
