package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;

public class ProcessSong extends Activity {

    public static void processKey() {
        switch (FullscreenActivity.mKey) {
            case "A":
                FullscreenActivity.pad_filename = "a";
                FullscreenActivity.keyindex = 1;
                break;
            case "A#":
                FullscreenActivity.pad_filename = "asharp";
                FullscreenActivity.keyindex = 2;
            case "Bb":
                FullscreenActivity.pad_filename = "asharp";
                FullscreenActivity.keyindex = 3;
                break;
            case "B":
                FullscreenActivity.pad_filename = "b";
                FullscreenActivity.keyindex = 4;
                break;
            case "C":
                FullscreenActivity.pad_filename = "c";
                FullscreenActivity.keyindex = 5;
                break;
            case "C#":
                FullscreenActivity.pad_filename = "csharp";
                FullscreenActivity.keyindex = 6;
                break;
            case "Db":
                FullscreenActivity.pad_filename = "csharp";
                FullscreenActivity.keyindex = 7;
                break;
            case "D":
                FullscreenActivity.pad_filename = "d";
                FullscreenActivity.keyindex = 8;
                break;
            case "D#":
                FullscreenActivity.pad_filename = "dsharp";
                FullscreenActivity.keyindex = 9;
                break;
            case "Eb":
                FullscreenActivity.pad_filename = "dsharp";
                FullscreenActivity.keyindex = 10;
                break;
            case "E":
                FullscreenActivity.pad_filename = "e";
                FullscreenActivity.keyindex = 11;
                break;
            case "F":
                FullscreenActivity.pad_filename = "f";
                FullscreenActivity.keyindex = 12;
                break;
            case "F#":
                FullscreenActivity.pad_filename = "fsharp";
                FullscreenActivity.keyindex = 13;
                break;
            case "Gb":
                FullscreenActivity.pad_filename = "fsharp";
                FullscreenActivity.keyindex = 14;
                break;
            case "G":
                FullscreenActivity.pad_filename = "g";
                FullscreenActivity.keyindex = 15;
                break;
            case "G#":
                FullscreenActivity.pad_filename = "gsharp";
                FullscreenActivity.keyindex = 16;
                break;
            case "Ab":
                FullscreenActivity.pad_filename = "gsharp";
                FullscreenActivity.keyindex = 17;
                break;
            case "Am":
                FullscreenActivity.pad_filename = "am";
                FullscreenActivity.keyindex = 18;
                break;
            case "A#m":
                FullscreenActivity.pad_filename = "asharpm";
                FullscreenActivity.keyindex = 19;
                break;
            case "Bbm":
                FullscreenActivity.pad_filename = "asharpm";
                FullscreenActivity.keyindex = 20;
                break;
            case "Bm":
                FullscreenActivity.pad_filename = "bm";
                FullscreenActivity.keyindex = 21;
                break;
            case "Cm":
                FullscreenActivity.pad_filename = "cm";
                FullscreenActivity.keyindex = 22;
                break;
            case "C#m":
                FullscreenActivity.pad_filename = "csharpm";
                FullscreenActivity.keyindex = 23;
                break;
            case "Dbm":
                FullscreenActivity.pad_filename = "csharpm";
                FullscreenActivity.keyindex = 24;
                break;
            case "Dm":
                FullscreenActivity.pad_filename = "dm";
                FullscreenActivity.keyindex = 25;
                break;
            case "D#m":
                FullscreenActivity.pad_filename = "dsharpm";
                FullscreenActivity.keyindex = 26;
                break;
            case "Ebm":
                FullscreenActivity.pad_filename = "dsharpm";
                FullscreenActivity.keyindex = 27;
                break;
            case "Em":
                FullscreenActivity.pad_filename = "em";
                FullscreenActivity.keyindex = 28;
                break;
            case "Fm":
                FullscreenActivity.pad_filename = "fm";
                FullscreenActivity.keyindex = 29;
                break;
            case "F#m":
                FullscreenActivity.pad_filename = "fsharpm";
                FullscreenActivity.keyindex = 30;
                break;
            case "Gbm":
                FullscreenActivity.pad_filename = "fsharpm";
                FullscreenActivity.keyindex = 31;
                break;
            case "Gm":
                FullscreenActivity.pad_filename = "gm";
                FullscreenActivity.keyindex = 32;
                break;
            case "G#m":
                FullscreenActivity.pad_filename = "gsharpm";
                FullscreenActivity.keyindex = 33;
                break;
            case "Abm":
                FullscreenActivity.pad_filename = "gsharpm";
                FullscreenActivity.keyindex = 34;
                break;
            default:
                FullscreenActivity.pad_filename = "";
                FullscreenActivity.keyindex = 0;
        }

    }

    public static void processTimeSig() {
        switch (FullscreenActivity.mTimeSig) {
            case "2/4":
                FullscreenActivity.timesigindex = 1;
                FullscreenActivity.beats = 2;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "3/4":
                FullscreenActivity.timesigindex = 2;
                FullscreenActivity.beats = 3;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "3/8":
                FullscreenActivity.timesigindex = 3;
                FullscreenActivity.beats = 3;
                FullscreenActivity.noteValue = 8;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "4/4":
                FullscreenActivity.timesigindex = 4;
                FullscreenActivity.beats = 4;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "5/4":
                FullscreenActivity.timesigindex = 5;
                FullscreenActivity.beats = 5;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "5/8":
                FullscreenActivity.timesigindex = 6;
                FullscreenActivity.beats = 5;
                FullscreenActivity.noteValue = 8;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "6/4":
                FullscreenActivity.timesigindex = 7;
                FullscreenActivity.beats = 6;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "6/8":
                FullscreenActivity.timesigindex = 8;
                FullscreenActivity.beats = 6;
                FullscreenActivity.noteValue = 8;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "7/4":
                FullscreenActivity.timesigindex = 9;
                FullscreenActivity.beats = 7;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "7/8":
                FullscreenActivity.timesigindex = 10;
                FullscreenActivity.beats = 7;
                FullscreenActivity.noteValue = 8;
                FullscreenActivity.mTimeSigValid = true;
                break;
            default:
                FullscreenActivity.timesigindex = 0;
                FullscreenActivity.beats = 4;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = false;
                break;
        }
    }

    public static void processTempo() {
        FullscreenActivity.temposlider = 39;
        if (FullscreenActivity.mTempo == null || FullscreenActivity.mTempo.equals("")
                || FullscreenActivity.mTempo.isEmpty()) {
            FullscreenActivity.temposlider = 39;
        } else {
            try {
                FullscreenActivity.temposlider = Integer
                        .parseInt(FullscreenActivity.mTempo.replaceAll("[\\D]", ""));
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
                FullscreenActivity.temposlider = 39;
            }
        }
        FullscreenActivity.temposlider = FullscreenActivity.temposlider - 39;
    }

    public static String removeUnwantedSymbolsAndSpaces(String string) {
        // Replace unwanted symbols
        string = string.replace("_", "");
        string = string.replace("|", " ");
        string = string.replace(",", " ");
        string = string.replace(".", " ");
        string = string.replace(":", " ");
        string = string.replace(";", " ");
        string = string.replace("!", " ");
        string = string.replace("'", "");
        string = string.replace("(", " ");
        string = string.replace(")", " ");
        string = string.replace("-", " ");

        // Now remove any double spaces
        while (string.contains("  ")) {
            string = string.replace("  ", " ");
        }

        return string;
    }

    public static String determineLineTypes(String string) {
        String type;
        if (string.indexOf(".")==0) {
            type = "chord";
        } else if (string.indexOf(";")==0) {
            type = "comment";
        } else if (string.indexOf("[")==0) {
            type = "heading";
        } else {
            type = "lyric";
        }
        return type;
    }

    public static String[] getChordPositions(String string) {
        // Given a chord line, get the character positions that each chord starts at
        // Go through the line character by character
        // If the character isn't a " " and the character before is " ", "." or "|" it's a new chord
        // Add the positions to an array
        ArrayList<String> chordpositions = new ArrayList<>();

        boolean lookingforstartpos = false;

        int startpos = 0;

        for (int x = 1; x < string.length(); x++) {

            if (lookingforstartpos) {
                if (!string.substring(x, x + 1).equals(" ") &&
                        (string.substring(x - 1, x).equals(" ") || string.substring(x - 1, x).equals("."))) {
                    // Get the starting position of this chord
                    startpos = x;
                    lookingforstartpos = false;
                }
            } else if (string.substring(x, x + 1).equals(" ") && !string.substring(x - 1, x).equals(" ")) {
                lookingforstartpos = true;

                // Add the position to the array
                chordpositions.add(startpos + "");
            }
        }

        String[] chordpos = new String[chordpositions.size()];
        chordpos = chordpositions.toArray(chordpos);

        return chordpos;
    }

    public static String[] getChordSections(String string, String[] pos_string) {
        // Go through the chord positions and extract the substrings
        ArrayList<String> chordsections = new ArrayList<>();
        int startpos = 0;
        int endpos;

        for (int x=0;x<pos_string.length;x++) {
            if (pos_string[x].equals("0")) {
                // First chord is at the start of the line
                startpos = 0;
            } else if (x == pos_string.length - 1) {
                // Last chord, so end position is end of the line
                // First get the second last section
                endpos = Integer.parseInt(pos_string[x]);
                chordsections.add(string.substring(startpos, endpos));

                // Now get the last one
                startpos = Integer.parseInt(pos_string[x]);
                endpos = string.length();
                chordsections.add(string.substring(startpos, endpos));

            } else {
                // We are at the start of a chord somewhere other than the start or end
                // Get the bit of text in the previous section;
                endpos = Integer.parseInt(pos_string[x]);
                chordsections.add(string.substring(startpos, endpos));
                startpos = endpos;
            }
        }
        String[] sections = new String[chordsections.size()];
        sections = chordsections.toArray(sections);

        return sections;
    }

    public static String[] getLyricSections(String string, String[] pos_string) {
        // Go through the chord positions and extract the substrings
        ArrayList<String> lyricsections = new ArrayList<>();
        int startpos = 0;
        int endpos;

        for (int x=0;x<pos_string.length;x++) {
            if (pos_string[x].equals("0")) {
                // First chord is at the start of the line
                startpos = 0;
            } else if (x == pos_string.length - 1) {
                // Last chord, so end position is end of the line
                // First get the second last section
                endpos = Integer.parseInt(pos_string[x]);
                lyricsections.add(string.substring(startpos, endpos));

                // Now get the last one
                startpos = Integer.parseInt(pos_string[x]);
                endpos = string.length();
                lyricsections.add(string.substring(startpos, endpos));

            } else {
                // We are at the start of a chord somewhere other than the start or end
                // Get the bit of text in the previous section;
                endpos = Integer.parseInt(pos_string[x]);
                lyricsections.add(string.substring(startpos, endpos));
                startpos = endpos;
            }
        }
        String[] sections = new String[lyricsections.size()];
        sections = lyricsections.toArray(sections);

        return sections;
    }

    public static String chordlinetoHTML(String[] chords) {
        String chordhtml = "";
        for (String bit:chords) {
            if (bit.indexOf(".")==0 && bit.length()>1) {
                bit = bit.substring(1);
            }
            chordhtml += "<td class=\"chord\" name=\""+bit.trim()+"\">"+bit.trim()+"</td>";
        }
        Log.d("d","chordHTML="+chordhtml);
        return chordhtml;
    }

    public static String lyriclinetoHTML(String[] lyrics) {
        String lyrichtml = "";
        for (String bit:lyrics) {
            lyrichtml += "<td class=\"lyric\">"+bit.replace(" ","&nbsp;")+"</td>";
        }
        Log.d("d","lyricHTML="+lyrichtml);
        return lyrichtml;
    }
}