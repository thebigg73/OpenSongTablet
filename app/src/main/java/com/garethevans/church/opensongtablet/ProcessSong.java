package com.garethevans.church.opensongtablet;

import android.app.Activity;

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
        if (FullscreenActivity.mTempo==null || FullscreenActivity.mTempo.equals("")
                || FullscreenActivity.mTempo.isEmpty()) {
            FullscreenActivity.temposlider =39;
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
}