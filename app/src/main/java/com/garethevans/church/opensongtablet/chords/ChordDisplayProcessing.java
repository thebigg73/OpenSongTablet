package com.garethevans.church.opensongtablet.chords;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.Locale;

public class ChordDisplayProcessing {

    private ArrayList<String> instruments, chordsInSong, fingerings, pianoNotesArray;
    private ArrayList<Integer> pianoKeysArray;
    private final String TAG = "ChordDisplayProcessing";

    ChordDisplayProcessing(Context c) {
        initialiseArrays(c);
    }
    public void initialiseArrays(Context c) {
        chordsInSong = new ArrayList<>();
        fingerings   = new ArrayList<>();
        pianoKeysArray = new ArrayList<>();
        pianoNotesArray = new ArrayList<>();
        instruments = new ArrayList<>();
        addPianoKeys();
        setupInstruments(c);
    }

    public void setupInstruments(Context c) {
        instruments = new ArrayList<>();
        instruments.add(c.getString(R.string.guitar));
        instruments.add(c.getString(R.string.ukulele));
        instruments.add(c.getString(R.string.mandolin));
        instruments.add(c.getString(R.string.banjo4));
        instruments.add(c.getString(R.string.banjo5));
        instruments.add(c.getString(R.string.cavaquinho));
        instruments.add(c.getString(R.string.piano));
    }
    public ArrayList<String> getInstruments() {
        return instruments;
    }
    public String getPrefFromInstrument(String instrument) {
        String pref;
        if (instrument.equals(instruments.get(0))) {
            pref = "g";
        } else if (instrument.equals(instruments.get(1))) {
            pref = "u";
        } else if (instrument.equals(instruments.get(2))) {
            pref = "m";
        } else if (instrument.equals(instruments.get(3))) {
            pref = "b";
        } else if (instrument.equals(instruments.get(4))) {
            pref = "B";
        } else if (instrument.equals(instruments.get(5))) {
            pref = "c";
        } else if (instrument.equals(instruments.get(6))) {
            pref = "p";
        } else {
            pref = "g";
        }
        return pref;
    }
    public String getInstrumentFromPref(String pref) {
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
    public ArrayList<String> getChordsInSong() {
        return chordsInSong;
    }
    public ArrayList<String> getFingerings() {
        return fingerings;
    }
    private void addPianoKeys() {
        pianoKeysArray.add(R.id.c0);
        pianoKeysArray.add(R.id.csharp0);
        pianoKeysArray.add(R.id.d0);
        pianoKeysArray.add(R.id.dsharp0);
        pianoKeysArray.add(R.id.e0);
        pianoKeysArray.add(R.id.f0);
        pianoKeysArray.add(R.id.fsharp0);
        pianoKeysArray.add(R.id.g0);
        pianoKeysArray.add(R.id.gsharp0);
        pianoKeysArray.add(R.id.a0);
        pianoKeysArray.add(R.id.asharp0);
        pianoKeysArray.add(R.id.b0);
        pianoKeysArray.add(R.id.c1);
        pianoKeysArray.add(R.id.csharp1);
        pianoKeysArray.add(R.id.d1);
        pianoKeysArray.add(R.id.dsharp1);
        pianoKeysArray.add(R.id.e1);
        pianoKeysArray.add(R.id.f1);
        pianoKeysArray.add(R.id.fsharp1);
        pianoKeysArray.add(R.id.g1);
        pianoKeysArray.add(R.id.gsharp1);
        pianoKeysArray.add(R.id.a1);
        pianoKeysArray.add(R.id.asharp1);
        pianoKeysArray.add(R.id.b1);
        pianoKeysArray.add(R.id.c2);
        pianoKeysArray.add(R.id.csharp2);
        pianoKeysArray.add(R.id.d2);
        pianoKeysArray.add(R.id.dsharp2);
        pianoKeysArray.add(R.id.e2);

        pianoNotesArray.add("C");
        pianoNotesArray.add("C#");
        pianoNotesArray.add("D");
        pianoNotesArray.add("D#");
        pianoNotesArray.add("E");
        pianoNotesArray.add("F");
        pianoNotesArray.add("F#");
        pianoNotesArray.add("G");
        pianoNotesArray.add("G#");
        pianoNotesArray.add("A");
        pianoNotesArray.add("A#");
        pianoNotesArray.add("B");
        pianoNotesArray.add("C");
        pianoNotesArray.add("C#");
        pianoNotesArray.add("D");
        pianoNotesArray.add("D#");
        pianoNotesArray.add("E");
        pianoNotesArray.add("F");
        pianoNotesArray.add("F#");
        pianoNotesArray.add("G");
        pianoNotesArray.add("G#");
        pianoNotesArray.add("A");
        pianoNotesArray.add("A#");
        pianoNotesArray.add("B");
        pianoNotesArray.add("C");
        pianoNotesArray.add("C#");
        pianoNotesArray.add("D");
        pianoNotesArray.add("D#");
        pianoNotesArray.add("E");
    }
    public ArrayList<Integer> getPianoKeysArray() {
        return pianoKeysArray;
    }
    public ArrayList<String> getPianoNotesArray() {
        return pianoNotesArray;
    }

    public void getChordsInSong(MainActivityInterface mainActivityInterface) {
        // First up, parse the lyrics for the chord lines and add them together
        String[] lines = mainActivityInterface.getSong().getLyrics().split("\n");
        StringBuilder chordsOnly = new StringBuilder();
        for (String thisline : lines) {
            if (thisline.startsWith(".")) {
                chordsOnly.append(thisline).append(" ");
            }
        }
        String everyChord = chordsOnly.toString().replace(".", " ");
        // Replace characters with spaces (but not slashes)
        everyChord = everyChord.replace("("," ");
        everyChord = everyChord.replace(")"," ");
        everyChord = everyChord.replace("|"," ");
        // Now split the chords individually and add unique ones to an array
        // Encode them for fixing later $..$
        chordsInSong = new ArrayList<>();
        String[] allChords = everyChord.split(" ");
        for (String chord:allChords) {
            if (!chord.trim().isEmpty() && !chordsInSong.contains("$"+chord+"$")) {
                chordsInSong.add("$"+chord+"$");
            }
        }
    }
    public void transposeChordsInSong(MainActivityInterface mainActivityInterface) {
        // Go through each chord and transpose it
        for (int i=0; i<chordsInSong.size(); i++) {
            chordsInSong.set(i, mainActivityInterface.getTranspose().getKeyBeforeCapo(
                    Integer.parseInt(mainActivityInterface.getSong().getCapo()),chordsInSong.get(i)));
        }
    }
    public void addCustomChords(MainActivityInterface mainActivityInterface, String forInstrument) {
        // If we have custom chords in the song add them (they are split by a space)
        // Only add this instrument though
        if (!mainActivityInterface.getSong().getCustomchords().isEmpty()) {
            String[] customChords = mainActivityInterface.getSong().getCustomchords().split(" ");
            for (String customChord:customChords) {
                if (customChord.contains("_"+forInstrument) && !fingerings.contains((customChord))) {
                    fingerings.add(customChord);
                    chordsInSong.add(customChord.substring(customChord.lastIndexOf("_")+1));
                }
            }
        }
    }
    public void setFingerings(ChordDirectory chordDirectory, String instrument, ArrayList<String> instruments, int chordFormat) {
        for (String chord : chordsInSong) {
            if (instrument.equals(instruments.get(0))) {
                // Guitar chords
                addFingeringOrNull(chordDirectory.guitarChords(chordFormat, chord));
            } else if (instrument.equals(instruments.get(1))) {
                // Ukelele chords
                addFingeringOrNull(chordDirectory.ukuleleChords(chordFormat, chord));
            } else if (instrument.equals(instruments.get(2))) {
                // Mandolin chords
                addFingeringOrNull(chordDirectory.mandolinChords(chordFormat, chord));
            } else if (instrument.equals(instruments.get(3))) {
                // Banjo 4 chords
                addFingeringOrNull(chordDirectory.banjo4stringChords(chordFormat, chord));
            } else if (instrument.equals(instruments.get(4))) {
                // Banjo 5 chords
                addFingeringOrNull(chordDirectory.banjo5stringChords(chordFormat, chord));
            } else if (instrument.equals(instruments.get(5))) {
                // Cavaqhino chords
                addFingeringOrNull(chordDirectory.ukuleleChords(chordFormat, chord));
            } else if (instrument.equals(instruments.get(6))) {
                // Piano chords
                addFingeringOrNull(chordDirectory.pianoChords(chordFormat, chord));
            }
        }
    }
    private void addFingeringOrNull(String fingering) {
        if (fingering!=null && !fingering.isEmpty() && !fingering.startsWith("_")) {
            fingerings.add(fingering);
        } else {
            fingerings.add(null);
        }
    }
    public String getCapoPosition(Context c, MainActivityInterface mainActivityInterface) {
        // Decide if the capo position is a number or a numeral
        String capoPosition = mainActivityInterface.getSong().getCapo();
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "capoInfoAsNumerals", false)) {
            String[] numerals = new String[] {"","I","II","III","IV","V","VI","VII","VIII","IX","X","XI,","XII"};
            int pos = Integer.parseInt(capoPosition);
            if (pos>=0 && pos<=12) {
                return numerals[pos];
            } else {
                return capoPosition;
            }
        } else {
            return capoPosition;
        }
    }

    // The display for stringed instruments
    @SuppressLint("InflateParams")
    public LinearLayout getChordDiagram(Context c, MainActivityInterface mainActivityInterface,
                                        LayoutInflater inflater, String chordName, String chordString) {
        int padding = c.getResources().getDimensionPixelSize(R.dimen.chord_padding);
        LinearLayout chordLayout = getChordLayout(c,padding);

        // Set the chord name
        // Make sure it is the preferred format though (e.g. Eb/D#)
        // If it isn't a valid chord, it will be null, in which case, ignore
        TextView chordNameTextView = getChordName(c, mainActivityInterface, chordName);
        if (chordNameTextView!=null && chordString!=null) {
            chordLayout.addView(chordNameTextView);

            // Get chord table layout
            TableLayout chordTable = getTableLayout(c);

            // The guitarstring will be in the format of 002220, xx0232, 113331_4_g_C#
            boolean fretLabel = false;
            String fretMarker = "";
            String[] chordBits = chordString.split("_");
            if (chordBits.length > 1) {
                fretMarker = chordBits[1];
                if (!fretMarker.equals("0")) {
                    fretLabel = true;
                }
            }

            Log.d(TAG, "chord: " + chordName + "  fretLabel: " + fretLabel);
            // The first bit of chordBits is the fingering.  Get the number of strings
            char[] stringPositions = chordBits[0].toCharArray();
            ArrayList<Integer> stringPosArray = new ArrayList<>();
            int maxFrets = 4;
            for (int x = 0; x <= 6; x++) {
                if (stringPositions.length > x) {
                    try {
                        int fret = Integer.parseInt(Character.toString(stringPositions[x]));
                        maxFrets = Math.max(fret, maxFrets);
                        stringPosArray.add(x, fret);
                    } catch (Exception e) {
                        // It was likely an x
                        stringPosArray.add(x, -1);
                    }
                }
            }
            TableRow stringMarkers = new TableRow(c);
            stringMarkers.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            // Now go through the frets and build the chords
            for (int fret = 0; fret < maxFrets; fret++) {
                //GuitarFret guitarFret = new GuitarFret(requireContext(),null);
                if (fret == 0) {
                    // We should show the string markers (x, o or "")
                    TextView spacer = new TextView(c);
                    setColumnIndex(spacer, 0);
                    if (!fretLabel) {
                        spacer.setVisibility(View.GONE);
                    }
                    chordTable.addView(spacer);
                    for (int pos = 0; pos < 6; pos++) {
                        @SuppressLint("InflateParams") TextView markerTextView = inflater.inflate(R.layout.view_string_marker, null).findViewById(R.id.stringMarker);
                        markerTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                        markerTextView.setText(getStringTopMarker(markerTextView, stringPosArray, pos));
                        setColumnIndex(markerTextView, pos + 1);
                        stringMarkers.addView(markerTextView);
                    }
                    chordTable.addView(stringMarkers, fret);
                }

                // Now build the string row
                TableRow guitarFret = new TableRow(c);
                guitarFret.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                // Start with the fret label if any
                TextView markerTextView = (TextView) inflater.inflate(R.layout.view_string_marker, null);
                if (fret == 0 && fretLabel) {
                    markerTextView.setText(fretMarker);
                } else if (fretLabel) {
                    markerTextView.setText("");
                } else {
                    markerTextView.setVisibility(View.GONE);
                }
                setColumnIndex(markerTextView, 0);
                guitarFret.addView(markerTextView);

                // Now go through the strings
                for (int string = 0; string < 6; string++) {
                    FrameLayout stringView;
                    if (string == 0) {
                        stringView = (FrameLayout) inflater.inflate(R.layout.view_chord_string_left, null);
                    } else if (stringPosArray.size() == string + 1) {
                        stringView = (FrameLayout) inflater.inflate(R.layout.view_chord_string_right, null);
                    } else {
                        stringView = (FrameLayout) inflater.inflate(R.layout.view_chord_string_middle, null);
                    }
                    highlightStringFingering(stringView, stringPosArray, fret, string);
                    guitarFret.addView(stringView);
                }
                chordTable.addView(guitarFret, fret + 1);
            }
            chordLayout.addView(chordTable);
            Log.d(TAG, "chordBits[0]="+chordBits[0]);
            if (chordBits[0].isEmpty() || chordBits[0].equals("xxxxxx") || chordBits[0].equals("xxxxx") || chordBits[0].equals("xxxx")) {
                // Allow clicking on this view to open a custom chord designer
                chordLayout.setOnClickListener(view -> {
                    mainActivityInterface.setWhattodo("customChord_"+chordName);
                    Log.d(TAG, "open custom chord: customChord_"+chordName);
                    mainActivityInterface.navigateToFragment("opensongapp://settings/chords/custom",0);
                });
            }
            return chordLayout;
        } else {
            return null;
        }
    }

    private LinearLayout getChordLayout(Context c, int padding) {
        LinearLayout linearLayout = new TableLayout(c);
        ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(padding,padding,padding,padding);
        linearLayout.setLayoutParams(layoutParams1);
        return linearLayout;
    }

    private TextView getChordName(Context c, MainActivityInterface mainActivityInterface, String chordName) {
        chordName = chordName.replace("$", "");
        Log.d(TAG,"chordName: "+chordName+"  isValidChord(): "+isValidChord(chordName));
        if (isValidChord(chordName)) {
            chordName = mainActivityInterface.getTranspose().convertToPreferredChord(c, mainActivityInterface, chordName);
            TextView textView = new TextView(c);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(layoutParams2);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(24);
            textView.setText(chordName);
            return textView;
        } else {
            return null;
        }
    }

    private TableLayout getTableLayout(Context c) {
        TableLayout chordTable = new TableLayout(c);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        chordTable.setLayoutParams(layoutParams3);
        return chordTable;
    }

    private String getStringTopMarker(TextView textView, ArrayList<Integer> stringPosArray, int position) {
        if (stringPosArray.size()>position) {
            if (stringPosArray.get(position) == -1) {
                return "x";
            } else if (stringPosArray.get(position) == 0) {
                return "o";
            } else {
                return "";
            }
        } else {
            textView.setVisibility(View.GONE);
            return "";
        }
    }

    private void setColumnIndex(View view, int index) {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(index);
        view.setLayoutParams(layoutParams);
    }

    private void highlightStringFingering(FrameLayout frameLayout, ArrayList<Integer> stringPosArray, int fret, int string) {
        if (stringPosArray.size()>string && stringPosArray.get(string).equals(fret+1)) {
            frameLayout.findViewById(R.id.stringOn).setVisibility(View.VISIBLE);
        } else if (stringPosArray.size()>string) {
            frameLayout.findViewById(R.id.stringOn).setVisibility(View.INVISIBLE);
        } else {
            frameLayout.setVisibility(View.GONE);
        }
    }

    private boolean isValidChord(String chord) {
        if (chord.length()>0) {
            char char1 = chord.charAt(0);
            return "abcdefgh".contains(Character.toString(char1).toLowerCase(Locale.ROOT));
        }
        return false;
    }



    // The display for piano
    @SuppressLint("InflateParams")
    public LinearLayout getChordDiagramPiano(Context c, MainActivityInterface mainActivityInterface,
                                        LayoutInflater inflater, String chordName, String chordString) {
        int padding = c.getResources().getDimensionPixelSize(R.dimen.chord_padding);
        LinearLayout chordLayout = getChordLayout(c,padding);

        // Set the chord name
        // Make sure it is the preferred format though (e.g. Eb/D#)
        // If it isn't a valid chord, it will be null, in which case, ignore
        TextView chordNameTextView = getChordName(c, mainActivityInterface, chordName);
        if (chordNameTextView!=null && chordString!=null) {
            chordLayout.addView(chordNameTextView);

            // The piano notes will be in the format of A,C#,E_p
            String[] chordBits = chordString.split("_");
            String[] notes = chordBits[0].split(",");

            // Get piano layout
            @SuppressLint("InflateParams") View pianoChord = inflater.inflate(R.layout.view_chord_piano, chordLayout);

            // Go through each note and colour tint the view
            // Get the starting position for the first note in the array
            int start = pianoNotesArray.indexOf(notes[0]);
            if (start!=-1) {
                int noteToFind = 0;
                for (int x = start; x < pianoNotesArray.size(); x++) {
                    // Look for the remaining positions in the notesArray
                    if (noteToFind < notes.length && pianoNotesArray.get(x).equals(notes[noteToFind])) {
                        tintDrawable(c, pianoChord.findViewById(pianoKeysArray.get(x)), notes[noteToFind], true);
                        noteToFind++;  // Once we've found them all, this won't get called again
                    }
                }
            }
            return chordLayout;
        } else {
            return null;
        }
    }

    public void tintDrawable(Context c, ImageView imageView, String note, boolean on) {
        Drawable drawable;
        if (imageView!=null) {
            if (on && note.contains("#")) {
                drawable = ContextCompat.getDrawable(c, R.drawable.piano_note_black_on);
            } else if (!on && note.contains("#")) {
                drawable = ContextCompat.getDrawable(c, R.drawable.piano_note_black);
            } else if (on && !note.contains("#")) {
                drawable = ContextCompat.getDrawable(c, R.drawable.piano_note_white_on);
            } else {
                drawable = ContextCompat.getDrawable(c, R.drawable.piano_note_white);
            }
            imageView.setImageDrawable(drawable);
        }
    }

    public boolean codeMatchesInstrument(String code, String instrument) {
        String instrumentLetter = getPrefFromInstrument(instrument);
        return code.contains("_"+instrumentLetter+"_");
    }
}
