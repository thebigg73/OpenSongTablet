package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.MaterialTextView;
import com.garethevans.church.opensongtablet.databinding.SettingsChordsCustomBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class CustomChordsFragment extends Fragment {

    private SettingsChordsCustomBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ChordDirectory chordDirectory;
    private ChordDisplayProcessing chordDisplayProcessing;
    private ArrayList<String> customChordCode, customChordsFingering, customChordsFret, customChordsInstrument, customChordsName;
    private ArrayList<String> chordsCodeForInstrument, chordsNameForInstrument, chordsFretForInstrument, chordsFingeringForInstrument;
    private final String TAG = "CustomChordsFragment";
    private String currentCode;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsChordsCustomBinding.inflate(inflater, container, false);
        mainActivityInterface.updateToolbar(getString(R.string.custom_chords));

        // Initialise the chord helpers
        chordDirectory = new ChordDirectory();
        chordDisplayProcessing = new ChordDisplayProcessing(requireContext());

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
        buildGuitarFrets();

        displayChord();
        return myView.getRoot();
    }

    private void getChordsInSong() {
        StringBuilder chordsInSongBuilder = new StringBuilder();
        StringBuilder chordsNotInDatabaseBuilder = new StringBuilder();
        int chordFormat = mainActivityInterface.getSong().getDetectedChordFormat();

        // Figure out the chords in the song
        chordDisplayProcessing.getChordsInSong(mainActivityInterface);

        // Now go through each one in turn
        for (String chord:chordDisplayProcessing.getChordsInSong()) {
            // Chords are encoded, so remove the $
            chord = chord.replace("$","");
            chordsInSongBuilder.append(chord).append(", ");
            // Any chords in the database exist for all instruments.  Check piano
            String piano = chordDirectory.pianoChords(chordFormat,chord);
            if (piano.startsWith("_p")) {
                // No notes, so no chord found
                chordsNotInDatabaseBuilder.append(chord).append(", ");
            }

        }
        String chordsFound = chordsInSongBuilder.toString();
        String chordsMissing = chordsNotInDatabaseBuilder.toString();


        chordsFound = chordsFound.substring(0, chordsFound.lastIndexOf(", "));
        chordsMissing = chordsMissing.substring(0, chordsMissing.lastIndexOf(", "));

        myView.chordsInSong.setHint(chordsFound);
        myView.chordsMissing.setHint(chordsMissing);
    }

    private void getCurrentCustomChords() {
        // If the songs already has some, we'll add them to the array.
        customChordCode = new ArrayList<>();
        customChordsFingering = new ArrayList<>();
        customChordsInstrument = new ArrayList<>();
        customChordsFret = new ArrayList<>();
        customChordsName = new ArrayList<>();
        if (mainActivityInterface.getSong().getCustomchords()!=null &&
            !mainActivityInterface.getSong().getCustomchords().isEmpty()) {
            // They are split by a space, so add to this array
            String[] chordsSaved = mainActivityInterface.getSong().getCustomchords().split(" ");
            for (String chord:chordsSaved) {
                // Keep a note of the full chord code (for easy searching later)
                customChordCode.add(chord);

                // The chord is {fingering/notes}_{instrument}_{fret not piano}_{chord name}
                String[] chordBits = chord.split("_");
                customChordsFingering.add(chordBits[0]);
                customChordsInstrument.add(chordBits[1]);
                if (chordBits[1].equals("p")) {
                    customChordsFret.add("");
                    customChordsName.add(chordBits[2]);
                } else {
                    customChordsFret.add(chordBits[2]);
                    customChordsFret.add(chordBits[3]);
                }
            }
        }
    }

    private void setupInstruments() {
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.instrument, R.layout.view_exposed_dropdown_item, chordDisplayProcessing.getInstruments());
        myView.instrument.setAdapter(exposedDropDownArrayAdapter);
        String instrumentPref = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"chordInstrument", "g");
        myView.instrument.setText(chordDisplayProcessing.getInstrumentFromPref(instrumentPref));
    }

    private void setupListeners() {
        myView.instrument.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                // Update the list of chords available for edit (that match this instrument
                updateCustomChordDropDown();
                drawChosenChord();
                canShowSave();
            }
        });

        myView.chordName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                displayChord();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                displayChord();
                canShowSave();
            }
        });
    }

    private void updateCustomChordDropDown() {
        chordsCodeForInstrument = new ArrayList<>();
        chordsNameForInstrument = new ArrayList<>();
        chordsFretForInstrument = new ArrayList<>();
        chordsFingeringForInstrument = new ArrayList<>();
        String chordPref = chordDisplayProcessing.getPrefFromInstrument(myView.instrument.getText().toString());
        for (int i=0; i<customChordCode.size(); i++) {
            if (customChordCode.get(i).contains("_"+chordPref+"_")) {
                chordsCodeForInstrument.add(customChordCode.get(i));
                chordsNameForInstrument.add(customChordsName.get(i));
                chordsFretForInstrument.add(customChordsFret.get(i));
                chordsFingeringForInstrument.add(customChordsFingering.get(i));
            }
        }
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.chordName, R.layout.view_exposed_dropdown_item, chordsNameForInstrument);
        myView.chordName.setAdapter(exposedDropDownArrayAdapter);

        if (chordsCodeForInstrument.size()>0) {
            myView.chordName.setText(chordsNameForInstrument.get(0));
        }

    }

    private int numberOfStrings() {
        String currInstr = myView.instrument.getText().toString();
        if (currInstr.equals(getString(R.string.guitar))) {
            return 6;
        } else if (currInstr.equals(getString(R.string.banjo5))) {
            return 5;
        } else if (currInstr.equals(getString(R.string.piano))) {
            return 0;
        } else {
            return 4;
        }
    }
    private void buildGuitarFrets() {
        myView.guitarChordLayout.removeAllViews();

        // Set the string markers (and space) for the first row
        TableRow markers = new TableRow(requireContext());
        TextView textViewSpacer = new TextView(requireContext());
        textViewSpacer.setId(View.generateViewId());
        markers.addView(textViewSpacer);

        for (int markerpos=1; markerpos < numberOfStrings()+1; markerpos++) {
            TextView marker = (TextView)getLayoutInflater().inflate(R.layout.view_string_marker,markers).findViewById(R.id.stringMarker);
            marker.setTag("stringMarker"+markerpos);
            marker.setText("x");
            marker.setId(View.generateViewId());
            marker.setLayoutParams(new TableRow.LayoutParams(markerpos));
            marker.setOnClickListener(v -> stringMarkerListener(v.getTag().toString()));
        }
        myView.guitarChordLayout.addView(markers);

        // Now add the strings for 5 frets
        for (int fret=1; fret<6; fret++) {
            TableRow frets = new TableRow(requireContext());
            frets.setId(View.generateViewId());
            TextView textView;
            if (fret==1) {
                textView = (TextView)getLayoutInflater().inflate(R.layout.view_chord_fret_marker,frets).findViewById(R.id.fretMarker);
                textView.setTag("fretMarker");
                textView.setText("1");
                textView.setOnClickListener(v->increaseFretNumber());

            } else {
                textView = (TextView) new TextView(requireContext());
                frets.addView(textView);
                textView.setTag("spacerFret"+fret);
            }
            textView.setId(View.generateViewId());
            textView.setLayoutParams(new TableRow.LayoutParams(0));

            for (int string=1; string<numberOfStrings()+1; string++) {
                View view;
                if (string==1) {
                    view = getLayoutInflater().inflate(R.layout.view_chord_string_left,frets,false);
                } else if (string==numberOfStrings()) {
                    view = getLayoutInflater().inflate(R.layout.view_chord_string_right,frets,false);
                } else {
                    view = getLayoutInflater().inflate(R.layout.view_chord_string_middle,frets,false);
                }
                Log.d(TAG,"string:"+string);
                String stringTag = "fret"+fret+"_string"+string;
                view.setTag(stringTag);
                //view.setId(View.generateViewId());
                view.findViewById(R.id.stringOn).setTag("fret"+fret+"_stringOn"+string);
                view.setLayoutParams(new TableRow.LayoutParams(string));
                view.setOnClickListener(v -> stringNoteListener(view.getTag().toString()));
                frets.addView(view);
            }
            myView.guitarChordLayout.addView(frets);
        }
        getGuitarCode();
        scaleStringChords();
    }

    private void drawChosenChord() {
        boolean ispiano = myView.instrument.getText().toString().equals(getString(R.string.piano));
        if (ispiano) {
            myView.guitarChordLayout.setVisibility(View.GONE);
            myView.pianoChordLayout.setVisibility(View.VISIBLE);
        } else {
            myView.pianoChordLayout.setVisibility(View.GONE);
            myView.guitarChordLayout.setVisibility(View.VISIBLE);
        }
    }

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
        ((TextView)myView.guitarChordLayout.findViewWithTag(tag)).setText(currVal);

        // Make sure all frets on this string are hidden
        for (int fret=1; fret<6; fret++) {
            String tagNote = "fret"+fret+"_stringOn"+string;
            myView.guitarChordLayout.findViewWithTag(tagNote).setVisibility(View.INVISIBLE);
        }
        getGuitarCode();
    }

    private void stringNoteListener(String tag) {
        // Tag is in the format fret1_string1
        String[] tagbits = tag.split("_");
        int fret = Integer.parseInt(tagbits[0].replace("fret",""));
        int string = Integer.parseInt(tagbits[1].replace("string",""));

        Log.d(TAG,"string: "+string+"  fret:"+fret);
        // The user has clicked on a string position, so clear any others
        String stringMarkerTag = "stringMarker"+string;
        ((TextView)myView.guitarChordLayout.findViewWithTag(stringMarkerTag)).setText("");
        for (int i=1; i<6; i++) {
            String stringTag = "fret"+i+"_stringOn"+string;
            View view = myView.guitarChordLayout.findViewWithTag(stringTag);
            if (view!=null && i==fret) {
                view.setVisibility(View.VISIBLE);
            } else if (view!=null) {
                view.setVisibility(View.INVISIBLE);
            }
        }
        getGuitarCode();
    }

    private void increaseFretNumber() {
        // Get the current fret
        int fret = Integer.parseInt(getFretMarkerText());
        if (fret==11) {
            fret=1;
        } else {
            fret++;
        }
        String newFret = ""+fret;
        getFretMarker().setText(newFret);
        getGuitarCode();
    }

    private void scaleStringChords() {
        myView.guitarChordLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Measure the layout
                int childWidth = myView.guitarChordLayout.getMeasuredWidth();
                int childHeight = myView.guitarChordLayout.getMeasuredHeight();
                DisplayMetrics displayMetrics = new DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;
                float scale = ((float)width/2f)/(float)childWidth;
                myView.guitarChordLayout.setGravity(Gravity.CENTER|Gravity.TOP);
                ViewGroup.LayoutParams layoutParams = myView.guitarChordLayout.getLayoutParams();
                layoutParams.height=(int)(childHeight*scale);
                myView.guitarChordLayout.setPivotX(childWidth/2f);
                myView.guitarChordLayout.setPivotY(0);
                myView.guitarChordLayout.setScaleX(scale);
                myView.guitarChordLayout.setScaleY(scale);
                myView.guitarChordLayout.setLayoutParams(layoutParams);
                myView.guitarChordLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                myView.layout.invalidate();
            }
        });
    }

    private void getGuitarCode() {
        // Guitar code should be in the format of 000000_g_0_G
        // First get the fret for each string
        StringBuilder stringBuilder = new StringBuilder();
        for (int string=1; string<numberOfStrings()+1; string++) {
            String marker = ((TextView)myView.guitarChordLayout.findViewWithTag("stringMarker"+string)).getText().toString();
            if (marker.equals("x")) {
                stringBuilder.append(marker);
                Log.d(TAG,"string"+string+": "+marker);
            } else if (marker.equals("o")) {
                stringBuilder.append("0");
            } else {
                // Look for fret positions
                for (int fret=1; fret<6; fret++) {
                    View stringPos = myView.guitarChordLayout.findViewWithTag("fret"+fret+"_stringOn"+string);
                    if (stringPos.getVisibility()==View.VISIBLE) {
                        stringBuilder.append((fret+1));
                    }
                }
            }
        }
        // Update the text
        String codeString = stringBuilder + "_" + chordDisplayProcessing.getPrefFromInstrument(returnTextOrEmpty(myView.instrument)) + "_" + getFretMarkerText() +"_"+returnTextOrEmpty(myView.chordName);
        myView.customCode.setText(codeString);
        canShowSave();
    }

    private TextView getFretMarker() {
        return myView.guitarChordLayout.findViewWithTag("fretMarker");
    }
    private String getFretMarkerText() {
        return returnTextOrEmpty(getFretMarker());
    }
    private String returnTextOrEmpty(TextView textView) {
        if (textView!=null && textView.getText()!=null) {
            return textView.getText().toString();
        } else {
            return "";
        }
    }
    private String returnTextOrEmpty(ExposedDropDown exposedDropDown) {
        if (exposedDropDown!=null && exposedDropDown.getText()!=null){
            return exposedDropDown.getText().toString();
        } else {
            return "";
        }
    }
    private String returnTextOrEmpty(MaterialTextView materialTextView) {
        if (materialTextView!=null && materialTextView.getText()!=null) {
            return materialTextView.getText().toString();
        } else {
            return "";
        }
    }

    private void displayChord() {
        if (myView.instrument.getText().toString().equals(getString(R.string.piano))) {

        } else {
            // Get the chord bits
            int which = customChordsName.indexOf(returnTextOrEmpty(myView.chordName));
            if (which>=0) {
                // Update the code
                myView.customCode.setText(customChordCode.get(which));
                // Now go through the strings
                String[] notes = customChordsFingering.get(which).split("");
                for (int i=0; i<numberOfStrings(); i++) {
                    if (notes.length>i && notes[i]!=null && notes[i].equals("x")) {
                        ((TextView)myView.guitarChordLayout.findViewWithTag("stringMarker"+i)).setText("x");
                    } else if (notes.length>i && notes[i]!=null && notes[i].equals("0")) {
                        ((TextView)myView.guitarChordLayout.findViewWithTag("stringMarker"+i)).setText("o");
                    } else if (notes.length>i && notes[i]!=null) {
                        ((TextView)myView.guitarChordLayout.findViewWithTag("stringMarker"+i)).setText("");
                        int fret = Integer.parseInt(notes[i]);
                        myView.guitarChordLayout.findViewWithTag("fret"+fret+"_string"+(i+1)).performClick();
                    }
                }
            }

        }
    }

    private boolean ispiano() {
        return returnTextOrEmpty(myView.instrument).equals(getString(R.string.piano));
    }
    private void canShowSave() {
        // We can only save if we meet the following criteria
        // 1. We have a name in the dropdown
        // 2. The current chord code is different to the one loaded up
        boolean name = !returnTextOrEmpty(myView.chordName).isEmpty();
        boolean diff = !returnTextOrEmpty(myView.customCode).equals(currentCode);
        if (name && diff) {
            myView.save.show();
        } else {
            myView.save.hide();
        }
    }
}