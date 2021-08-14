package com.garethevans.church.opensongtablet.chords;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class ChordDisplayProcessing {

    private ArrayList<String> chordsInSong, fingerings;
    private final String TAG = "ChordDisplayProcessing";

    public void initialiseArrays() {
        chordsInSong = new ArrayList<>();
        fingerings   = new ArrayList<>();
    }

    public ArrayList<String> getChordsInSong() {
        return chordsInSong;
    }
    public ArrayList<String> getFingerings() {
        return fingerings;
    }

    public void addFingeringOrNull(String fingering) {
        if (fingering!=null && !fingering.isEmpty() && !fingering.startsWith("_")) {
            fingerings.add(fingering);
        } else {
            fingerings.add(null);
        }
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
                if (customChord.contains("_"+forInstrument+"_") && !fingerings.contains((customChord))) {
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





    // The display
    @SuppressLint("InflateParams")
    public LinearLayout getChordDiagram(Context c, LayoutInflater inflater, String chordName, String chordString) {
        int padding = c.getResources().getDimensionPixelSize(R.dimen.chord_padding);
        LinearLayout chordLayout = getChordLayout(c,padding);

        // Set the chord name
        chordLayout.addView(getChordName(c,chordName));

        // Get chord table layout
        TableLayout chordTable = getTableLayout(c);

        // The guitarstring will be in the format of 002220, xx0232, 113331_4_g_C#
        boolean fretLabel = (chordString.length()>5 && Character.toString(chordString.charAt(4)).equals("_") && !Character.toString(chordString.charAt(5)).equals("0")) ||
                (chordString.length()>6 && Character.toString(chordString.charAt(5)).equals("_") && !Character.toString(chordString.charAt(6)).equals("0")) ||
                (chordString.length()>7 && Character.toString(chordString.charAt(6)).equals("_") && !Character.toString(chordString.charAt(7)).equals("0"));

        String fretMarker="";
        String[] chordBits = chordString.split("_");
        if (fretLabel && chordBits.length>0) {
            fretMarker = chordBits[1];
        }

        Log.d(TAG,"chord: "+chordName+ "  fretLabel: "+fretLabel);
        // The first bit of chordBits is the fingering.  Get the number of strings
        char[] stringPositions = chordBits[0].toCharArray();
        ArrayList<Integer> stringPosArray = new ArrayList<>();
        int maxFrets = 4;
        for (int x=0; x<=6; x++) {
            if (stringPositions.length>x) {
                try {
                    int fret = Integer.parseInt(Character.toString(stringPositions[x]));
                    maxFrets = Math.max(fret,maxFrets);
                    stringPosArray.add(x,fret);
                } catch (Exception e) {
                    // It was likely an x
                    stringPosArray.add(x,-1);
                }
            }
        }
        TableRow stringMarkers = new TableRow(c);
        stringMarkers.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        // Now go through the frets and build the chords
        for (int fret=0; fret<maxFrets; fret++) {
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
                chordTable.addView(stringMarkers,fret);
            }

            // Now build the string row
            TableRow guitarFret = new TableRow(c);
            guitarFret.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            // Start with the fret label if any
            TextView markerTextView = (TextView)inflater.inflate(R.layout.view_string_marker, null);
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
                if (string==0) {
                    stringView = (FrameLayout)inflater.inflate(R.layout.view_chord_string_left, null);
                } else if (stringPosArray.size()==string+1) {
                    stringView = (FrameLayout)inflater.inflate(R.layout.view_chord_string_right, null);
                } else {
                    stringView = (FrameLayout)inflater.inflate(R.layout.view_chord_string_middle, null);
                }
                highlightStringFingering(stringView,stringPosArray,fret,string);
                guitarFret.addView(stringView);
            }
            chordTable.addView(guitarFret,fret+1);
        }
        chordLayout.addView(chordTable);
        return chordLayout;
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

    private TextView getChordName(Context c, String chordName) {
        chordName = chordName.replace("$","");
        TextView textView = new TextView(c);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams2);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(24);
        textView.setText(chordName);
        return textView;
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

}
