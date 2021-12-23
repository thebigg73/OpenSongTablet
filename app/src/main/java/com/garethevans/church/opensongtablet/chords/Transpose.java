package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class Transpose {

    private final String TAG = "Transpose";
    private ArrayList<String> chordFormatNames, chordFormatAppearances;

    //  A  A#/Bb  B/(Cb) C/(B#) C#/Db    D    D#/Eb  E/(Fb) (E#)/F   F#/Gb   G     G#/Ab
    //  A    B     H      C
    //  1    2     3      4      5      6      7      8      9     W(10)  X(11)   Y(12)
    // On transpose Cb -> B, B# -> C, Fb -> E,  E# -> F
    //
    // A 'number' format ├y┤ is used - y is the 'position in key' as above and ├ and ┤ are start and end markers
    // A 'number' must end ┤ except where a format has explicit minors where is will end ┤m
    // « and » before an item indicate a need to later consider a remove or add of a space after a chord
    //
    // Transpose example:  Format 1 to Format 3 Capo 1
    //   Line starts in 1 Standard                        - ???    Fm    ???
    //   From '1 Standard' F is replaced to number ««├9┤  - ???    ««├9┤m    ???
    //   The number is transposed 1 time from 9 to W(10)  - ???    ««├W┤m    ???
    //   from number ├W┤ is replaced to 3 Solfege «Solb   - ???    «««Solbm    ???
    //   '«««' is processed to remove 3 following spaces  - ???    Solbm ???
    //   In effect Solbm overwrites Fm and 3 spaces

    // Chord to number: 'majors' and sus interfere so are protected

    // Standard chord formatting = 1
    private final String [] fromChords1 =      {"maj7",  "ma7",   "maj9",  "ma9",
            "A#",    "B#",    "C#",    "D#",    "E#",    "F#",    "G#",
            "Ab",    "Bb",    "Cb",    "Db",    "Eb",    "Fb",    "Gb",
            "A",     "B",     "C",     "D",     "E",     "F",     "G",     "¬"};
    private final String [] toChordsNums1 =    {"¬aj7",  "¬a7",   "¬aj9",  "¬a9",
            "«├2┤",  "«├4┤",  "«├5┤",  "«├7┤",  "«├9┤",  "«├W┤",  "«├Y┤",
            "«├Y┤",  "«├2┤",  "«├3┤",  "«├5┤",  "«├7┤",  "«├8┤",  "«├W┤",
            "««├1┤", "««├3┤", "««├4┤", "««├6┤", "««├8┤", "««├9┤", "««├X┤", "m"};

    // Euro chord formatting (B/H) = 2
    private final String[] fromChords2 =       {"maj7",  "ma7",   "maj9",  "ma9",
            "A#",    "H#",    "C#",    "D#",    "E#",    "F#",    "G#",
            "Ab",    "B",     "Cb",    "Db",    "Eb",    "Fb",    "Gb",
            "A",     "H",     "C",     "D",     "E",     "F",     "G",     "¬"};
    private final String[] toChordsNums2 =     {"¬aj7",  "¬a7",   "¬aj9",  "¬a9",
            "«├2┤",  "«├4┤",  "«├5┤",  "«├7┤",  "«├9┤",  "«├W┤",  "«├Y┤",
            "«├Y┤",  "««├2┤", "«├3┤",  "«├5┤",  "«├7┤",  "«├8┤",  "«├W┤",
            "««├1┤", "««├3┤", "««├4┤", "««├6┤", "««├8┤", "««├9┤", "««├X┤", "m"};

    // Euro chord formatting full (B/H/es/is) = 3
    private final String[] fromChords3 =       {"maj7",     "ma7",      "maj9",     "ma9",      "sus",
            " (ais",    " (his",    " (cis",    " (dis",    " (eis",    " (fis",    " (gis",
            " (as",     " (b",      " (ces",    " (des",    " (es",     " (fes",    " (ges",
            " (a",      " (h",      " (c",      " (d",      " (e",      " (f",      " (g",
            ".ais",     ".his",     ".cis",     ".dis",     ".eis",     ".fis",     ".gis",
            ".as",      ".b",       ".ces",     ".des",     ".es",      ".fes",     ".ges",
            ".a",       ".h",       ".c",       ".d",       ".e",       ".f",       ".g",
            " ais",     " his",     " cis",     " dis",     " eis",     " fis",     " gis",
            " as",      " b",       " ces",     " des",     " es",      " fes",     " ges",
            " a",       " h",       " c",       " d",       " e",       " f",       " g",
            "Ais",      "His",      "Cis",      "Dis",      "Eis",      "Fis",      "Gis",
            "As",       "B",        "Ces",      "Des",      "Es",       "Fes",      "Ges",
            "A",        "H",        "C",        "D",        "E",        "F",        "G",        "¬us", "¬"};
    private final String[] toChordsNums3 =     {"¬aj7",     "¬a7",      "¬aj9",     "¬a9",      "¬us",
            " («├2┤m",  " («├4┤m",  " («├5┤m",  " («├7┤m",  " («├9┤m",  " («├W┤m",  " («├Y┤m",
            " (««├Y┤m", " («««├2┤m"," («├3┤m",  " («├5┤m",  " (««├7┤m", " («├8┤m",  " («├W┤m",
            " («««├1┤m"," («««├3┤m"," («««├4┤m"," («««├6┤m"," («««├8┤m"," («««├9┤m"," («««├X┤m",
            ".«├2┤m",   ".«├4┤m",   ".«├5┤m",   ".«├7┤m",   ".«├9┤m",   ".«├W┤m",   ".«├Y┤m",
            ".««├Y┤m",  ".«««├2┤m", ".«├3┤m",   ".«├5┤m",   ".««├7┤m",  ".«├8┤m",   ".«├W┤m",
            ".«««├1┤m", ".«««├3┤m", ".«««├4┤m", ".«««├6┤m", ".«««├8┤m", ".«««├9┤m", ".«««├X┤m",
            " «├2┤m",   " «├4┤m",   " «├5┤m",   " «├7┤m",   " «├9┤m",   " «├W┤m",   " «├Y┤m",
            " ««├Y┤m",  " «««├2┤m", " «├3┤m",   " «├5┤m",   " ««├7┤m",  " «├8┤m",   " «├W┤m",
            " «««├1┤m", " «««├3┤m", " «««├4┤m", " «««├6┤m", " «««├8┤m", " «««├9┤m", " «««├X┤m",
            "├2┤",      "├4┤",      "├5┤",      "├7┤",      "├9┤",      "├W┤",      "├Y┤",
            "«├Y┤",     "««├2┤",    "├3┤",      "├5┤",      "«├7┤",     "├8┤",      "├W┤",
            "««├1┤",    "««├3┤",    "««├4┤",    "««├6┤",    "««├8┤",    "««├9┤",    "««├X┤",    "sus", "m"};

    // Solfeggio (DoReMi) = 4
    // Also handles variations of chord name
    private final String[] fromChords4 =    {"maj7", "ma7", "maj9", "ma9",
            "la", "Ti", "ti", "si", "do", "Re", "re", "ré", "mi", "fa", "sol",
            "LA", "TI", "DO", "RE", "RÉ", "MI", "FA", "SOL",
            "La#",    "Si#",    "Do#",    "Ré#",    "Mi#",    "Fa#",    "Sol#",
            "Lab",    "Sib",    "Dob",    "Réb",    "Mib",    "Fab",    "Solb",
            "La",     "Si",     "Do",     "Ré",     "Mi",     "Fa",     "Sol",   "¬"};
    private final String[] toChordsNums4 =  {"¬aj7", "¬a7", "¬aj9", "¬a9",
            "La", "Si", "Si", "Si", "Do", "Ré", "Ré", "Ré", "Mi", "Fa", "Sol",
            "La", "Si", "Do", "Ré", "Ré", "Mi", "Fa", "Sol",
            "├2┤",    "├4┤",    "├5┤",    "├7┤",    "├9┤",    "├W┤",   "»├Y┤",
            "├Y┤",    "├2┤",    "├3┤",    "├5┤",    "├7┤",    "├8┤",   "»├W┤",
            "«├1┤",   "«├3┤",   "«├4┤",   "«├6┤",   "«├8┤",   "«├9┤",    "├X┤",    "m"};
    private String[] fromNash;
    private String[] toChordNumsNash;

    // Number to chord:
    private final String[] fromChordsNum  = "├2┤ ├5┤ ├7┤ ├W┤ ├Y┤ ├1┤ ├3┤ ├4┤ ├6┤ ├8┤ ├9┤ ├X┤".split(" ");
    private final String[] toSharpChords1 = "»A# »C# »D# »F# »G# »»A »»B »»C »»D »»E »»F »»G".split(" ");
    private final String[] toFlatChords1 =  "»Bb »Db »Eb »Gb »Ab »»A »»B »»C »»D »»E »»F »»G".split(" ");
    private final String[] toSharpChords2 = "»»B »C# »D# »F# »G# »»A »»H »»C »»D »»E »»F »»G".split(" ");
    private final String[] toFlatChords2 =  "»»B »Db »Eb »Gb »Ab »»A »»H »»C »»D »»E »»F »»G".split(" ");
    private final String[] toSharpChords4 = "La# Do# Ré# Fa# «Sol# »La »Si »Do »Ré »Mi »Fa Sol".split(" ");
    private final String[] toFlatChords4 =  "Sib Réb Mib «Solb Lab »La »Si »Do »Ré »Mi »Fa Sol".split(" ");
    //  A trick! Minors arrive ending ┤m, the m is moved into the number to give numbers for minors. '┤ma' is treated as the start of major and is protected.
    private final String[] fromChordsNumM = "┤ma ┤m ├2m┤ ├5m┤ ├7m┤ ├Wm┤ ├Ym┤ ├1m┤ ├3m┤ ├4m┤ ├6m┤ ├8m┤ ├9m┤ ├Xm┤ ├2┤ ├5┤ ├7┤ ├W┤ ├Y┤ ├1┤ ├3┤ ├4┤ ├6┤ ├8┤ ├9┤ ├X┤ ¬".split(" ");
    private final String[] toSharpChords3 = "┤¬a m┤ »»»b »cis »dis »fis »gis »»»a »»»h »»»c »»»d »»»e »»»f »»»g »»B Cis Dis Fis Gis »»A »»H »»C »»D »»E »»F »»G m".split(" ");
    private final String[] toFlatChords3 =  "┤¬a m┤ »»»b »des »»es »ges »»as »»»a »»»h »»»c »»»d »»»e »»»f »»»g »»B Des »Es Ges »As »»A »»H »»C »»D »»E »»F »»G m".split(" ");
    private String[] fromChordNumsNash;
    private String[] fromChordNumsNashType;
    private String[] toNash;

    // Used in the generation of the Nashville conversion arrays
    private final String[] bitsSharp      = "A A# B C C# D D# E F F# G G# A A# B C C# D D# E F F# G G#".split(" ");
    private final String[] bitsFlat       = "A Bb B C Db D Eb E F Gb G Ab A Bb B C Db D Eb E F Gb G Ab".split(" ");
    private final String [] bitsSharpNums = "♮├1┤ #├2┤ ♮├3┤ ♮├4┤ #├5┤ ♮├6┤ #├7┤ ♮├8┤ ♮├9┤ #├W┤ ♮├X┤ #├Y┤ ♮├1┤ #├2┤ ♮├3┤ ♮├4┤ #├5┤ ♮├6┤ #├7┤ ♮├8┤ ♮├9┤ #├W┤ ♮├X┤ #├Y┤".split(" ");
    private final String [] bitsFlatNums  = "♮├1┤ b├2┤ ♮├3┤ ♮├4┤ b├5┤ ♮├6┤ b├7┤ ♮├8┤ ♮├9┤ b├W┤ ♮├X┤ b├Y┤ ♮├1┤ b├2┤ ♮├3┤ ♮├4┤ b├5┤ ♮├6┤ b├7┤ ♮├8┤ ♮├9┤ b├W┤ ♮├X┤ b├Y┤".split(" ");
    private final String [] bitsNums      = "├1┤ ├2┤ ├3┤ ├4┤ ├5┤ ├6┤ ├7┤ ├8┤ ├9┤ ├W┤ ├X┤ ├Y┤ ├1┤ ├2┤ ├3┤ ├4┤ ├5┤ ├6┤ ├7┤ ├8┤ ├9┤ ├W┤ ├X┤ ├Y┤".split(" ");
    private int major;
    private int root;

    private boolean forceFlats;
    private boolean usesFlats;
    private boolean capoForceFlats;
    private boolean capoUsesFlats;
    private String capoKey;
    private int oldChordFormat, newChordFormat;
    private String transposeDirection;
    private int transposeTimes;

    private final String[] format2Identifiers = new String[]{"h"};
    private final String[] format3Identifiers = new String[]{"is","es"};
    private final String[] format4Identifiers = new String[]{"do","re","ré","mi","fa","sol","la","si"};
    private final String[] format5Identifiers = new String[]{"1","2","3","4","5","6","7"};
    private final String[] format6Identifiers = new String[]{"i","ii","ii","iii","iii","iv","iv","v","vi","vii"};

    // The song is sent in and the song is sent back after processing (key and lyrics get changed)
    public Song doTranspose(Context c, MainActivityInterface mainActivityInterface, Song thisSong,
                     String transposeDirection, int transposeTimes, int oldChordFormat, int newChordFormat) {

        // Initialise the variables that might be looked up later
        this.oldChordFormat = oldChordFormat;
        this.newChordFormat = newChordFormat;
        this.transposeDirection = transposeDirection;
        this.transposeTimes = transposeTimes;
        fromChordNumsNash = null;
        fromChordNumsNashType = null;
        toChordNumsNash = null;
        toNash = null;
        fromNash = null;
        capoKey = "";

        try {
            String originalkey = thisSong.getKey();
            // Update the key to the newly tranposed version
            if (originalkey != null && !originalkey.isEmpty()) {
                thisSong.setKey(numberToKey(c, mainActivityInterface, transposeNumber(keyToNumber(originalkey), transposeDirection, transposeTimes)));
            }

            // Now we have the new key, we can decide if we use flats or not for any notes
            if (thisSong.getKey()!=null) {
                usesFlats = keyUsesFlats(c, mainActivityInterface, thisSong.getKey());
            } else {
                usesFlats = false;
            }

            // Transpose and write the lyrics
            thisSong.setLyrics(transposeString(thisSong));

            // Change the song format
            thisSong.setDetectedChordFormat(newChordFormat);
            thisSong.setDesiredChordFormat(newChordFormat);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return thisSong;
    }

    // Stuff for dealing with the song key
    public String getKeyBeforeCapo(int capo, String oldkey) {
        // TODO
        Log.d(TAG,"getKeyBeforeCapo() NOT DOING ANYTHING PROBABLY - NEED TO FIX OR CHECK!!!!");
        Song tempSong = new Song();
        tempSong.setLyrics("."+oldkey);
        return transposeNumber(transposeString(tempSong),"-1",capo);
        /*String getkeynum = chordToNumber1(oldkey);
        getkeynum = transposeKey(getkeynum,"-1",capo);
        // IV - The returned chord may include « or » - removeAll
        return numberToChord1(getkeynum,false).replaceAll("[«»]","");*/
        //return oldkey;
    }

    public String keyToNumber(String key) {
        // Swap the key with the chord number - use standard formatting=1
        for (int z = 0; z < fromChords1.length; z++) key = key.replace(fromChords1[z], toChordsNums1[z]);
        return key;
    }

    public String numberToKey(Context c, MainActivityInterface mainActivityInterface, String key) {
        // We need to decide which key the user likes the best for each one
        // Convert the key number into either a sharp or natural first
        // Then we swap sharps to flats if the user prefers these
        for (int z = 0; z < fromChordsNum.length; z++) {
            key = key.replace(fromChordsNum[z], toSharpChords1[z]);
        }

        // Remove chord space adjustment indicators
        key = key.replace("»","").replace("«","");

        if (key.equals("G#") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyAb",true)) {
            key = "Ab";
        } else if (key.equals("G#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyAbm",false)) {
            key = "Abm";
        } else if (key.equals("A#")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyBb",true)) {
            key = "Bb";
        } else if (key.equals("A#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyBbm",true)) {
            key = "Bbm";
        } else if (key.equals("C#")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyDb",false)) {
            key = "Db";
        } else if (key.equals("C#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyDbm",true)) {
            key = "Dbm";
        } else if (key.equals("D#")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyEb",true)) {
            key = "Eb";
        } else if (key.equals("D#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyEbm",true)) {
            key = "Ebm";
        } else if (key.equals("F#")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyGb",false)) {
            key = "Gb";
        } else if (key.equals("F#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyGbm",false)) {
            key = "Gbm";
        }

        return key;
    }

    private boolean keyUsesFlats(Context c, MainActivityInterface mainActivityInterface, String testkey) {
        return  (testkey.equals("Ab")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyAb",true)) ||
                (testkey.equals("Bb")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyBb",true)) ||
                (testkey.equals("Db")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyDb",false)) ||
                (testkey.equals("Eb")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyEb",true)) ||
                (testkey.equals("Gb")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyGb",false)) ||
                (testkey.equals("Bbm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyBbm",true)) ||
                (testkey.equals("Dbm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyDbm",false)) ||
                (testkey.equals("Ebm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyEbm",true)) ||
                (testkey.equals("Gbm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyGbm",false)) ||
                testkey.equals("C") ||
                testkey.equals("F") ||
                testkey.equals("Dm") ||
                testkey.equals("Gm") ||
                testkey.equals("Cm");
    }



    // This is the chord transpose engine
    private String transposeString(Song thisSong) {
        try {
            StringBuilder sb = new StringBuilder();

            // IV - Add a trailing ¶ to force a split behaviour that copes with a trailing new line!
            for (String line : (thisSong.getLyrics()+"¶").split("\n")) {
                // IV - Use leading \n as we can be certain it is safe to remove later
                sb.append("\n");
                if (line.startsWith(".")) {
                    Log.d(TAG,"PRETRANSPOSE LINE: "+line);
                    switch (oldChordFormat) {
                        default:
                        case 1:
                            for (int z = 0; z < fromChords1.length; z++) {
                                line = line.replace(fromChords1[z], toChordsNums1[z]);
                            }
                            break;
                        case 2:
                            for (int z = 0; z < fromChords2.length; z++) {
                                line = line.replace(fromChords2[z], toChordsNums2[z]);
                            }
                            break;
                        case 3:
                            for (int z = 0; z < fromChords3.length; z++) {
                                line = line.replace(fromChords3[z], toChordsNums3[z]);
                            }
                            break;
                        case 4:
                            for (int z = 0; z < fromChords4.length; z++) {
                                line = line.replace(fromChords4[z], toChordsNums4[z]);
                            }
                            break;
                        case 5:
                        case 6:
                            if (fromNash == null) {
                                prepFromNashToNums(thisSong, thisSong.getDetectedChordFormat() == 6);
                            }
                            for (int z = 0; z < fromNash.length; z++) {
                                line = line.replace(fromNash[z], toChordNumsNash[z]);
                            }
                            break;
                    }
                    Log.d(TAG,"MIDTRANSPOSE LINE: "+line);

                    // If the old format has transposable chords - transpose
                    if (oldChordFormat < 5) {
                        line = transposeNumber(line, transposeDirection, transposeTimes);
                        Log.d(TAG,"MIDTRANSPOSE LINE: "+line);
                    }
                    Log.d(TAG,"newChordFormat="+newChordFormat);
                    switch (newChordFormat) {
                        default:
                        case 1:
                            if (forceFlats) for (int z = 0; z < fromChordsNum.length; z++) {
                                line = line.replace(fromChordsNum[z], toFlatChords1[z]);
                            } else {
                                for (int z = 0; z < fromChordsNum.length; z++) {
                                    line = line.replace(fromChordsNum[z], toSharpChords1[z]);
                                }
                            }
                            break;
                        case 2:
                            if (forceFlats) for (int z = 0; z < fromChordsNum.length; z++) {
                                line = line.replace(fromChordsNum[z], toFlatChords2[z]);
                            } else {
                                for (int z = 0; z < fromChordsNum.length; z++) {
                                    line = line.replace(fromChordsNum[z], toSharpChords2[z]);
                                }
                            }
                            break;
                        case 3:
                            // IV - Uses the 'm' array as the target has explicit minors
                            if (forceFlats) for (int z = 0; z < fromChordsNumM.length; z++) {
                                line = line.replace(fromChordsNumM[z], toFlatChords3[z]);
                            } else {
                                for (int z = 0; z < fromChordsNumM.length; z++) {
                                    line = line.replace(fromChordsNumM[z], toSharpChords3[z]);
                                }
                            }
                            break;
                        case 4:
                            if (forceFlats) for (int z = 0; z < fromChordsNum.length; z++) {
                                line = line.replace(fromChordsNum[z], toFlatChords4[z]);
                            } else {
                                for (int z = 0; z < fromChordsNum.length; z++) {
                                    line = line.replace(fromChordsNum[z], toSharpChords4[z]);
                                }
                            }
                            break;
                        case 5:
                        case 6:
                            if (toNash == null) {
                                prepFromNumsToNash(thisSong, newChordFormat == 6);
                            }
                            if (forceFlats) {
                                for (int z = 0; z < fromChordNumsNash.length; z++) {
                                    if (!fromChordNumsNashType[z].equals("#")) {
                                        line = line.replace(fromChordNumsNash[z], toNash[z]);
                                    }
                                }
                            } else {
                                for (int z = 0; z < fromChordNumsNash.length; z++) {
                                    if (!fromChordNumsNashType[z].equals("b")) {
                                        line = line.replace(fromChordNumsNash[z], toNash[z]);
                                    }
                                }
                            }
                            break;
                    }

                    Log.d(TAG,"NEARLY DONE LINE: "+line);

                    // Space adjustments: Remove patterns that cancel out
                    line = line.replace("««»»", "").replace("«»", "");

                    // Add space after the chord
                    int myindex = line.indexOf("»");
                    while (myindex > -1) {
                        line = line.substring(0, myindex) + line.substring(myindex + 1).replaceFirst(" ", "  ");
                        myindex = line.indexOf("»");
                    }
                    // Remove a space after the chord - do after 'Add space' to avoid removing a single space between chords
                    myindex = line.indexOf("«");
                    while (myindex > -1) {
                        line = line.substring(0, myindex) + line.substring(myindex + 1).replaceFirst(" {2}", " ");
                        myindex = line.indexOf("«");
                    }
                    Log.d(TAG,"TRANSPOSED LINE: "+line);

                }
                // Add it back up
                sb.append(line);
            }
            // Return the string removing the added leading \n and trailing ¶
            return sb.substring(1).replace("¶", "");

        } catch (Exception e) {
            return thisSong.getLyrics();
        }
    }

    public String transposeNumber(String string, String direction, int numtimes) {
        // Rotate the scale the number of times requested
        String replaceNumber = "1·2·3·4·5·6·7·8·9·W·X·Y·";
        if (direction.equals("-1")) {
            for (int x = 0; x < numtimes; x++) replaceNumber = replaceNumber.substring(22) + replaceNumber.substring(0, 22);
        } else {
            for (int x = 0; x < numtimes; x++) replaceNumber = replaceNumber.substring(2) + replaceNumber.substring(0, 2);
        }

        // For the '├y┤' number forma we transpose y only. Replace y┤ with transposed y· and then · with ┤ as this prevents errors.
        return string
                .replace("1┤", replaceNumber.substring(0, 2))
                .replace("2┤", replaceNumber.substring(2, 4))
                .replace("3┤", replaceNumber.substring(4, 6))
                .replace("4┤", replaceNumber.substring(6, 8))
                .replace("5┤", replaceNumber.substring(8, 10))
                .replace("6┤", replaceNumber.substring(10, 12))
                .replace("7┤", replaceNumber.substring(12, 14))
                .replace("8┤", replaceNumber.substring(14, 16))
                .replace("9┤", replaceNumber.substring(16, 18))
                .replace("W┤", replaceNumber.substring(18, 20))
                .replace("X┤", replaceNumber.substring(20, 22))
                .replace("Y┤", replaceNumber.substring(22, 24))
                .replace("·","┤");
    }

    private void prepFromNashToNums(Song thisSong, boolean numeral) {
        if (toChordNumsNash == null) {

            getNashvilleRoot(thisSong.getKey());

            if (numeral) {
                // Includes protection for major chords
                fromNash = ("maj7_ma7_maj9_ma9_" +
                        "#VII_#VI_#IV_#V_#III_#II_#I_bVII_bVI_bIV_bV_bIII_bII_bI_VII_VI_IV_V_III_II_I_#vii_#vi_#iv_#v_#iii_#ii_#i_bvii_bvi_biv_bv_biii_bii_bi_vii_vi_iv_v_iii_ii_i_¬").split("_");
                toChordNumsNash = ("¬aj7_¬a7_¬aj9_¬a9_" +
                        bitsNums[root] + "_" +
                        bitsNums[root + 9 + major] + "_" +
                        bitsNums[root + 6] + "_" +
                        bitsNums[root + 8] + "_" +
                        bitsNums[root + 4 + major] + "_" +
                        bitsNums[root + 3] + "_" +
                        bitsNums[root + 1] + "_" +
                        bitsNums[root + 9 + major] + "_" +
                        bitsNums[root + 7 + major] + "_" +
                        bitsNums[root + 4] + "_" +
                        bitsNums[root + 6] + "_" +
                        bitsNums[root + 2 + major] + "_" +
                        bitsNums[root + 1] + "_" +
                        bitsNums[root + 11] + "_" +
                        bitsNums[root + 10 + major] + "_" +
                        bitsNums[root + 8 + major] + "_" +
                        bitsNums[root + 5] + "_" +
                        bitsNums[root + 7] + "_" +
                        bitsNums[root + 3 + major] + "_" +
                        bitsNums[root + 2] + "_" +
                        bitsNums[root] + "_" +
                        bitsNums[root + 11 + major] + "m" + "_" +
                        bitsNums[root + 9 + major] + "m" + "_" +
                        bitsNums[root + 6] + "m" + "_" +
                        bitsNums[root + 8] + "m" + "_" +
                        bitsNums[root + 4 + major] + "m" + "_" +
                        bitsNums[root + 3] + "m" + "_" +
                        bitsNums[root + 1] + "m" + "_" +
                        bitsNums[root + 9 + major] + "m" + "_" +
                        bitsNums[root + 7 + major] + "m" + "_" +
                        bitsNums[root + 4] + "m" + "_" +
                        bitsNums[root + 6] + "m" + "_" +
                        bitsNums[root + 2 + major] + "m" + "_" +
                        bitsNums[root + 1] + "m" + "_" +
                        bitsNums[root + 11] + "m" + "_" +
                        bitsNums[root + 10 + major] + "mo".charAt(major) + "_" +
                        bitsNums[root + 8 + major] + "m" + "_" +
                        bitsNums[root + 5] + "m" + "_" +
                        bitsNums[root + 7] + "m" + "_" +
                        bitsNums[root + 3 + major] + "m" + "_" +
                        bitsNums[root + 2] + "om".charAt(major) + "_" +
                        bitsNums[root] + "m" + "_" +
                        "m").split("_");
            } else {
                String fromnashbase = "¬#7_¬#6_¬#5_¬#4_¬#3_¬#2_¬#1_¬b7_¬b6_¬b5_¬b4_¬b3_¬b2_¬b1_¬7_¬6_¬5_¬4_¬3_¬2_¬1_";
                String tochordnumsbase =
                        "¬" + bitsNums[root] + "_" +
                                "¬" + bitsNums[root + 9 + major] + "_" +
                                "¬" + bitsNums[root + 8] + "_" +
                                "¬" + bitsNums[root + 6] + "_" +
                                "¬" + bitsNums[root + 4 + major] + "_" +
                                "¬" + bitsNums[root + 3] + "_" +
                                "¬" + bitsNums[root + 1] + "_" +
                                "¬" + bitsNums[root + 9 + major] + "_" +
                                "¬" + bitsNums[root + 7 + major] + "_" +
                                "¬" + bitsNums[root + 6] + "_" +
                                "¬" + bitsNums[root + 4] + "_" +
                                "¬" + bitsNums[root + 2 + major] + "_" +
                                "¬" + bitsNums[root + 1] + "_" +
                                "¬" + bitsNums[root + 11] + "_" +
                                "¬" + bitsNums[root + 10 + major] + "_" +
                                "¬" + bitsNums[root + 8 + major] + "_" +
                                "¬" + bitsNums[root + 7] + "_" +
                                "¬" + bitsNums[root + 5] + "_" +
                                "¬" + bitsNums[root + 3 + major] + "_" +
                                "¬" + bitsNums[root + 2] + "_" +
                                "¬" + bitsNums[root] + "_";

                // Includes protection for major chords and support for differnt 'chord follows' sequences including '<space>('
                fromNash = ("maj7_ma7_maj9_ma9_" +
                        fromnashbase.replace("¬",".") +
                        fromnashbase.replace("¬"," ") +
                        fromnashbase.replace("¬","/") +
                        fromnashbase.replace("¬"," (") +
                        "¬").split("_");

                toChordNumsNash = ("¬aj7_¬a7_¬aj9_¬a9_" +
                        tochordnumsbase.replace("¬",".") +
                        tochordnumsbase.replace("¬"," ") +
                        tochordnumsbase.replace("¬","/") +
                        tochordnumsbase.replace("¬"," (") +
                        "m").split("_");
            }
            int diff;
            // For each item space adjustment markers are added
            for (int x = 0; x < toChordNumsNash.length; x++) {
                diff = (fromNash[x].length() - toChordNumsNash[x].length());
                if (diff < 0) {
                    toChordNumsNash[x] = "««««««««".substring(0, -diff) + toChordNumsNash[x];
                } else {
                    toChordNumsNash[x] = "»»»»»»»»".substring(0, diff) + toChordNumsNash[x];
                }
            }
        }
    }

    private void prepFromNumsToNash(Song thisSong, boolean numeral) {
        // StageMode sets StaticVariables.fromChordNumsNash to null in loadSong()
        // Display (not convert) transposes by section and we avoid repetition of array generation by store and re-use.
        if (fromChordNumsNash == null) {
            String to;
            String topostadditions = "";
            String frompostadditions = "";
            String frompreadditions;
            String topreadditions;

            getNashvilleRoot(thisSong.getKey());

            if (numeral) {
                // Pre additions handle major chords (which interfere). frompre needs 'b' and '#' versions. frompost needs only a '♮' version.
                frompreadditions =  "bmaj7_bma7_bmaj9_bma9_#maj7_#ma7_#maj9_#ma9";
                topreadditions =    "Δ7»»_Δ7»_Δ9»»_Δ9»_Δ7»»_Δ7»_Δ9»»_Δ9»";
                frompostadditions = "♮IVm_♮VIIm_♮VIIo_♮VIm_♮Vm_♮IIIm_♮IIm_♮Im_♮m7b5_♮-7b5_♮dim7_♮dim_♮o7_♮aug_♮#5";
                topostadditions =   "iv»_vii»_vii»_vi»_v»_iii»_ii»_i»_ø»»»_ø»»»_o»»»_o»»_o»_+»»_+»";
                to =                "I_II_III_IV_V_VI_VII_#I_#II_#IV_#V_#VI_I_II_III_IV_V_VI_VII_bII_bIII_bV_bVI_bVII";
            } else {
                frompreadditions = "bmaj7_bma7_bmaj9_bma9_#maj7_#ma7_#maj9_#ma9";
                topreadditions =   "Δ7»»_Δ7»_Δ9»»_Δ9»_Δ7»»_Δ7»_Δ9»»_Δ9»";
                to =               "1_2_3_4_5_6_7_#1_#2_#4_#5_#6_1_2_3_4_5_6_7_b2_b3_b5_b6_b7";
            }

            fromChordNumsNash = (frompreadditions + "_" +
                    bitsSharpNums[root] + "_" +
                    bitsSharpNums[root + 2] + "_" +
                    bitsSharpNums[root + 3 + major] + "_" +
                    bitsSharpNums[root + 5] + "_" +
                    bitsSharpNums[root + 7] + "_" +
                    bitsSharpNums[root + 8 + major] + "_" +
                    bitsSharpNums[root + 10 + major] + "_" +
                    bitsSharpNums[root + 1] + "_" +
                    bitsSharpNums[root + 3] + "_" +
                    bitsSharpNums[root + 6] + "_" +
                    bitsSharpNums[root + 8] + "_" +
                    bitsSharpNums[root + 9 + major] + "_" +
                    bitsFlatNums[root] + "_" +
                    bitsFlatNums[root + 2] + "_" +
                    bitsFlatNums[root + 3 + major] + "_" +
                    bitsFlatNums[root + 5] + "_" +
                    bitsFlatNums[root + 7] + "_" +
                    bitsFlatNums[root + 8 + major] + "_" +
                    bitsFlatNums[root + 10 + major] + "_" +
                    bitsFlatNums[root + 1] + "_" +
                    bitsFlatNums[root + 2 + major] + "_" +
                    bitsFlatNums[root + 6] + "_" +
                    bitsFlatNums[root + 7 + major] + "_" +
                    bitsFlatNums[root + 9 + major] + "_" + frompostadditions).split("_");

            toNash = (topreadditions + "_" + to + "_" + topostadditions).split("_");

            int diff;
            fromChordNumsNashType = fromChordNumsNash.clone();

            for (int x = 0; x < fromChordNumsNashType.length; x++) {
                // Extract the 'type'
                fromChordNumsNashType[x] = fromChordNumsNash[x].substring(0, 1);
                fromChordNumsNash[x] = fromChordNumsNash[x].substring(1);

                // Add space adjustment markers
                diff = (fromChordNumsNash[x].length() - toNash[x].length());

                if (diff < 0) toNash[x] = "««««««««".substring(0, -diff) + toNash[x];
                else          toNash[x] = "»»»»»»»»".substring(0,  diff) + toNash[x];
            }
        }
    }

    private void getNashvilleRoot(String key) {
        if (key!=null && key.endsWith("m")) {
            major = 0;
        } else {
            major = 1;
        }

        // Root lookup is against chord format 1 as key is stored in format 1
        root = 0;
        for (int i = 0; i < bitsSharp.length; i++) {
            if ((bitsSharp[i].equals(key) || (bitsSharp[i] + "m").equals(key) ||
                    bitsFlat[i].equals(key) || (bitsFlat[i]  + "m").equals(key))) {
                root = i;
                break;
            }
        }
    }





    String capoTranspose(Context c, MainActivityInterface mainActivityInterface) {
        // StageMode sets FullscreenActivity.capokey to "" in loadSong(), first call after sets for new song
        if (capoKey.equals("")) {
            // Get the capokey
            if (mainActivityInterface.getSong().getKey() != null) {
                capoKeyTranspose(c, mainActivityInterface);
            }
            // Determine if we need to force flats for the capo key
            capoForceFlats = keyUsesFlats(c, mainActivityInterface, capoKey);
        }
        // Transpose using force, add "." for transpose and remove on return (mCapo 0 is used when displaying a song with no capo in preferred chord format)
        transposeDirection = "-1";
        transposeTimes = Integer.parseInt("0" + mainActivityInterface.getSong().getCapo());
        // TODO - Line below from IV #136
        //return transposeString("." + string, !capoForceFlats, capoForceFlats).substring(1);
        return transposeString(mainActivityInterface.getSong());
    }

    private void capoKeyTranspose(Context c, MainActivityInterface mainActivityInterface) {
        capoKey = numberToKey(c, mainActivityInterface, transposeNumber(keyToNumber(mainActivityInterface.getSong().getKey()),
                "-1", Integer.parseInt("0" + mainActivityInterface.getSong().getCapo())));
    }

    private ArrayList<String> quickCapoKey(Context c, MainActivityInterface mainActivityInterface, String key) {
        // This is used to give the user a list starting with blank of either simple fret number or fret number with new capo key
        ArrayList<String> al = new ArrayList<>(Collections.singletonList(""));
        if (key!=null && !key.equals("") && !key.isEmpty()) {
            for (int i=1; i<=11; i++) {
                al.add(i + " (" + numberToKey(c, mainActivityInterface, transposeNumber(keyToNumber(key), "-1", i)) + ")");
            }
        } else {
            for (int i=1; i<=11; i++) {
                al.add(i + "");
            }
        }
        return al;
    }

    public void checkChordFormat(Song thisSong) {
        // We need to detect the chord formatting
        int contains_es_is_count = 0;
        int contains_H_count = 0;
        int contains_do_count = 0;
        int contains_nash_count = 0;
        int contains_nashnumeral_count = 0;

        // Check if the user is using the same chord format as the song.  Go through the chord lines and look for clues
        for (String line : thisSong.getLyrics().split("\n")) {
            if (line.startsWith(".")) {
                Log.d(TAG,"Checking line: "+line);
                // Remove text in brackets on chord lines as they may contain text that causes problems e.g. (Last x) contains La
                line = line
                        // Android Studio gets confused over escapes here - suggesting removing escapes that break the regex!  Kept lots of escapes to be sure they work!
                        .replaceAll("\\(.*?\\)","")
                        .replaceAll("\\{.*?\\}","")
                        .replaceAll("\\[.*?\\]","");

                // Trim out multiple whitespace and split into individual chords
                line = line.replaceAll("\\s{2,}", " ").
                        replace(".","").
                        toLowerCase(Locale.ROOT).trim();
                String[] chordsInLine = line.split(" ");

                // Now go through each chord and add to the matching format
                for (String chordInLine:chordsInLine) {
                    Log.d(TAG,"chordInLine: "+chordInLine);
                    if (Arrays.asList(format6Identifiers).contains(chordInLine)) {
                        contains_nashnumeral_count ++;
                    } else if (Arrays.asList(format5Identifiers).contains(chordInLine)) {
                        contains_nash_count ++;
                    } else if (Arrays.asList(format4Identifiers).contains(chordInLine)) {
                        contains_do_count ++;
                    } else if (chordInLine.length()>2 && Arrays.asList(format3Identifiers).contains(chordInLine.substring(chordInLine.length()-2))) {
                        contains_es_is_count ++;
                    } else if (Arrays.asList(format2Identifiers).contains(chordInLine)) {
                        contains_H_count ++;
                    }
                }
            }
        }

        // Here we allow low levels of mis-identification
        boolean contains_es_is = (contains_es_is_count > 1);
        boolean contains_H = (contains_H_count > 2);
        boolean contains_do = (contains_do_count > 4);
        boolean contains_nash = (contains_nash_count > 4);
        boolean contains_nashnumeral = (contains_nashnumeral_count > 4);

        Log.d(TAG, "contains_es_is_count="+contains_es_is_count);
        Log.d(TAG, "contains_H_count="+contains_H_count);
        Log.d(TAG, "contains_do_count="+contains_do_count);
        Log.d(TAG, "contains_nash_count="+contains_nash_count);
        Log.d(TAG, "contains_nashnumeral_count="+contains_nashnumeral_count);

        // Set the chord style detected - Ok so the user chord format may not quite match the song - it might though!
        int formatNum;

        if (contains_do) {
            formatNum = 4;
        } else if (contains_H && !contains_es_is) {
            formatNum = 2;
        } else if (contains_H || contains_es_is) {
            formatNum = 3;
        } else if (contains_nash) {
            formatNum = 5;
        } else if (contains_nashnumeral) {
            formatNum = 6;
        } else {
            formatNum = 1;
        }

        thisSong.setDetectedChordFormat(formatNum);
        thisSong.setDesiredChordFormat(formatNum);

        // We set the newChordFormat default to the same as detected
        newChordFormat = formatNum;

        Log.d(TAG,"formatNum="+formatNum);
    }


    public String convertToPreferredChord(Context c, MainActivityInterface mainActivityInterface, String chord) {
        // Changes Ab/G# to user's preference.  This sends out to another function which checks minor and major
        chord = swapToPrefChords(c,mainActivityInterface,"Ab","G#", chord, false);
        chord = swapToPrefChords(c,mainActivityInterface,"Bb","A#", chord, true);
        chord = swapToPrefChords(c,mainActivityInterface,"Db","C#", chord, false);
        chord = swapToPrefChords(c,mainActivityInterface,"Eb","D#", chord, true);
        chord = swapToPrefChords(c,mainActivityInterface,"Gb","F#", chord, false);
        return chord;
    }
    private String swapToPrefChords(Context c, MainActivityInterface mainActivityInterface,
                                    String flatOption, String sharpOption, String chord,
                                    boolean defaultFlatMinor) {

        if (chord.startsWith(flatOption+"m") || chord.startsWith(sharpOption+"m")) {
            // Check the minor chord first
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKey"+flatOption+"m",defaultFlatMinor)) {
                chord = chord.replace(sharpOption+"m", flatOption+"m");
            } else {
                chord = chord.replace(flatOption+"m",sharpOption+"m");
            }
        } else if (chord.startsWith(flatOption) || chord.startsWith(sharpOption)) {
            // Now check the major chord
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKey"+flatOption, true)) {
                chord = chord.replace(sharpOption, flatOption);
            } else {
                chord = chord.replace(flatOption,sharpOption);
            }
        }
        return chord;
    }







    // For dealing with songs loaded from sets (checking key with current song)
    public int getTransposeTimes(String from, String to) {
        String startAt = keyToNumber(from);
        Log.d(TAG,"from: "+from+"  startAt: "+startAt);
        String endAt = keyToNumber(to);
        Log.d(TAG,"to: "+to+"  endAt: "+endAt);
        int transposeTimes = 0;
        for (int x=1; x<13; x++) {
            if (transposeNumber(startAt,"+1",x).equals(endAt)) {
                transposeTimes = x;
            }
        }

        return transposeTimes;
    }





    // Commonly used array for chord format names and display values
    public ArrayList<String> getChordFormatNames(Context c) {
        if (chordFormatNames==null) {
            chordFormatNames = new ArrayList<>();
            chordFormatNames.add(c.getString(R.string.chordformat_1_name));
            chordFormatNames.add(c.getString(R.string.chordformat_2_name));
            chordFormatNames.add(c.getString(R.string.chordformat_3_name));
            chordFormatNames.add(c.getString(R.string.chordformat_4_name));
            chordFormatNames.add(c.getString(R.string.chordformat_5_name));
            chordFormatNames.add(c.getString(R.string.chordformat_6_name));
        }
        return chordFormatNames;
    }
    public ArrayList<String> getChordFormatAppearances(Context c) {
        if (chordFormatAppearances==null) {
            chordFormatAppearances = new ArrayList<>();
            chordFormatAppearances.add(c.getString(R.string.chordformat_1));
            chordFormatAppearances.add(c.getString(R.string.chordformat_2));
            chordFormatAppearances.add(c.getString(R.string.chordformat_3));
            chordFormatAppearances.add(c.getString(R.string.chordformat_4));
            chordFormatAppearances.add(c.getString(R.string.chordformat_5));
            chordFormatAppearances.add(c.getString(R.string.chordformat_6));
        }
        return chordFormatAppearances;
    }

    // -----------------------------------------------





    /*public Song convertChords(Context c, MainActivityInterface mainActivityInterface,
                       Song thisSong, String transposeDirection, int transposeTimes) {
        int convertTo = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"chordFormat",1);
        checkChordFormat(c,mainActivityInterface,thisSong);

        Log.d("Transpose","convertTo="+convertTo+"\ndetetctedChordFormat="+thisSong.getDetectedChordFormat());
        if (thisSong.getDetectedChordFormat() >= 5) {
            thisSong.setLyrics(convertFromNumerals(c, mainActivityInterface, thisSong));
        }// Convert to a normal format to start with
        if (convertTo>=5) {
            // We want to convert to a numeral.  If it is normal format, just do it, otherwise, convert to a normal format
            // Now convert to the correct value
            thisSong.setLyrics(convertToNumerals(c, mainActivityInterface, thisSong));
        } else {
            transposeDirection = "0";
            checkChordFormat(c, mainActivityInterface, thisSong);
            try {
                thisSong = doTranspose(c, mainActivityInterface, thisSong, transposeDirection, transposeTimes, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return thisSong;
    }

    private String convertToNumerals(Context c, MainActivityInterface mainActivityInterface,
                                     Song thisSong) {
        boolean numeral = false;
        if (mainActivityInterface.getPreferences().getMyPreferenceInt(c,"chordFormat",1)==6) {
            numeral = true;
        }
        String[] splitLyrics = thisSong.getLyrics().split("\n");

        StringBuilder sb = new StringBuilder();
        // Convert these into standard chord format to start with
        for (String line:splitLyrics) {
            if (line.startsWith(".")) {
                // Chord line, so sort it
                Log.d("Transpose","old line="+line);
                line = toNashville(line,numeral);
                Log.d("Transpose","new line="+line);
            }
            // Add it back up
            sb.append(line).append("\n");
        }

        return sb.toString();

    }

    private String convertFromNumerals(Context c, MainActivityInterface mainActivityInterface,
                                       Song thisSong) {
        // This goes through the song and converts from Nashville numbering or numerals to standard chord format first
        String lyrics = thisSong.getLyrics();
        if (thisSong.getDetectedChordFormat()==5 || thisSong.getDetectedChordFormat()==6) {
            // We currently have either a nashville system (numbers or numerals)
            String[] splitLyrics = thisSong.getLyrics().split("\n");

            boolean numeral = thisSong.getDetectedChordFormat()==6;
            StringBuilder sb = new StringBuilder();
            Log.d("Transpose","Converting from numerals first");
            // Convert these into standard chord format to start with
            for (String line:splitLyrics) {
                if (line.startsWith(".")) {
                    // Chord line, so sort it
                    Log.d("Transpose","old line="+line);
                    line = fromNashville(line,numeral);
                    Log.d("Transpose","new line="+line);

                }
                // Add it back up
                sb.append(line).append("\n");
            }

            lyrics = sb.toString();
            // If the new chordformat desired is also a numeral or number system, convert it to that
            if (mainActivityInterface.getPreferences().getMyPreferenceInt(c,"chordFormat",1)==5 ||
                    mainActivityInterface.getPreferences().getMyPreferenceInt(c, "chordFormat",1)==6) {
                thisSong.setLyrics(convertToNumerals(c, mainActivityInterface, thisSong));
            }
        } else {
            mainActivityInterface.getShowToast().doIt(c,c.getString(R.string.nashville_error));
        }
        return lyrics;
    }

    private String toNashville(String line, boolean numeral) {
        //  This compares the chords with the song key

        Log.d("Transpose","Converting to nashville");

        // A  A#/Bb  B/H  C  C#/Db  D  D#/Eb  E  F  F#/Gb  G  G#/Ab
        // 0    1     2   3    4    5    6    7  8    9    10   11

        String[] bitssharp;
        String[] bitsflat;
        switch(oldchordformat) {
            case 1:
            default:
                bitssharp = "A A# B C C# D D# E F F# G G# A A# B C C# D D# E F F# G G#".split(" ");
                bitsflat  = "A Bb B C Db D Eb E F Gb G Ab A Bb B C Db D Eb E F Gb G Ab".split(" ");
                break;

            case 2:
            case 3:
                bitssharp = "A B H C C# D D# E F F# G G# A B H C C# D D# E F F# G G#".split(" ");
                bitsflat  = "A B H C Db D Eb E F Gb G Ab A B H C Db D Eb E F Gb G Ab".split(" ");
                break;

            case 4:
                bitssharp = "La La# Si Do Do# Ré Ré# Mi Fa Fa# Sol Sol# La La# Si Do Do# Ré Ré# Mi Fa Fa# Sol Sol#".split(" ");
                bitsflat =  "La Sib Si Do Réb Ré Mib Mi Fa Solb Sol Lab La Sib Si Do Réb Ré Mib Mi Fa Solb Sol Lab".split(" ");
                break;
        }

        int root = 0;
        boolean stilllooking = true;
        for (int i=0;i<bitssharp.length;i++) {
            if (stilllooking && (bitssharp[i].equals(originalkey) || (bitssharp[i]+"m").equals(originalkey) ||
                    bitsflat[i].equals(originalkey) || (bitsflat[i]+"m").equals(originalkey))) {
                root = i;
                stilllooking = false;
            }
        }

        boolean ismajor = true;
        if (originalkey.contains("m")) {
            ismajor = false;
        }
        int num2=root+2, num3, num4=root+5, num5=root+7, num6, num7;
        if (ismajor) {
            num3 = root+4;
            num6 = root+9;
            num7 = root+11;
        } else {
            num3 = root+3;
            num6 = root+8;
            num7 = root+10;
        }

        String nash1_sharp = bitssharp[root];
        String nash1s_sharp = bitssharp[root+1];
        String nash2_sharp = bitssharp[num2];
        String nash2s_sharp = bitssharp[num2+1];
        String nash3_sharp = bitssharp[num3];
        String nash4_sharp = bitssharp[num4];
        String nash4s_sharp = bitssharp[num4+1];
        String nash5_sharp = bitssharp[num5];
        String nash5s_sharp = bitssharp[num5+1];
        String nash6_sharp = bitssharp[num6];
        String nash6s_sharp = bitssharp[num6+1];
        String nash7_sharp = bitssharp[num7];
        String nash1_flat = bitsflat[root];
        String nash2_flat = bitsflat[num2];
        String nash2b_flat = bitsflat[num2-1];
        String nash3_flat = bitsflat[num3];
        String nash3b_flat = bitsflat[num3-1];
        String nash4_flat = bitsflat[num4];
        String nash5_flat = bitsflat[num5];
        String nash5b_flat = bitsflat[num5-1];
        String nash6_flat = bitsflat[num6];
        String nash6b_flat = bitsflat[num6-1];
        String nash7_flat = bitsflat[num7];
        String nash7b_flat = bitsflat[num7-1];

        if (numeral) {
            line = fixDiminished(line);
            line = fixAugmented(line);
            line = fixMajorSeventh(line);

            String[] conditions = {"sharp","flat","natural"};
            for (String condition: conditions) {
                line = replaceBits(line,nash1_sharp,"I",condition);
                line = replaceBits(line,nash2_sharp,"II",condition);
                line = replaceBits(line,nash3_sharp,"III",condition);
                line = replaceBits(line,nash4_sharp,"IV",condition);
                line = replaceBits(line,nash5_sharp,"V",condition);
                line = replaceBits(line,nash6_sharp,"VI",condition);
                line = replaceBits(line,nash7_sharp,"VII",condition);
                line = replaceBits(line,nash1s_sharp,"#I",condition);
                line = replaceBits(line,nash2s_sharp,"#II",condition);
                line = replaceBits(line,nash4s_sharp,"#IV",condition);
                line = replaceBits(line,nash5s_sharp,"#V",condition);
                line = replaceBits(line,nash6s_sharp,"#VI",condition);
                line = replaceBits(line,nash1_flat,"I",condition);
                line = replaceBits(line,nash2_flat,"II",condition);
                line = replaceBits(line,nash3_flat,"III",condition);
                line = replaceBits(line,nash4_flat,"IV",condition);
                line = replaceBits(line,nash5_flat,"V",condition);
                line = replaceBits(line,nash6_flat,"VI",condition);
                line = replaceBits(line,nash7_flat,"VII",condition);
                line = replaceBits(line,nash2b_flat,"bII",condition);
                line = replaceBits(line,nash3b_flat,"bIII",condition);
                line = replaceBits(line,nash5b_flat,"bV",condition);
                line = replaceBits(line,nash6b_flat,"bVI",condition);
                line = replaceBits(line,nash7b_flat,"bVII",condition);

            }
            // Fix minor chords
            line = replaceChord(line,"IVm", "iv");
            line = replaceChord(line,"VIIm", "vii");
            line = replaceChord(line,"VIIo", "vii");
            line = replaceChord(line,"VIm", "vi");
            line = replaceChord(line,"Vm", "v");
            line = replaceChord(line,"IIIm", "iii");
            line = replaceChord(line,"IIm", "ii");
            line = replaceChord(line,"Im", "i");

        } else {
            line = replaceChord(line,nash1_sharp, "1");
            line = replaceChord(line,nash1s_sharp, "#1");
            line = replaceChord(line,nash2_sharp, "2");
            line = replaceChord(line,nash2s_sharp, "#2");
            line = replaceChord(line,nash3_sharp, "3");
            line = replaceChord(line,nash4_sharp, "4");
            line = replaceChord(line,nash4s_sharp, "#4");
            line = replaceChord(line,nash5_sharp, "5");
            line = replaceChord(line,nash5s_sharp, "#5");
            line = replaceChord(line,nash6_sharp, "6");
            line = replaceChord(line,nash6s_sharp, "#6");
            line = replaceChord(line,nash7_sharp, "7");
            line = replaceChord(line,nash1_flat, "1");
            line = replaceChord(line,nash2_flat, "2");
            line = replaceChord(line,nash2b_flat, "b2");
            line = replaceChord(line,nash3_flat, "3");
            line = replaceChord(line,nash3b_flat, "b3");
            line = replaceChord(line,nash4_flat, "4");
            line = replaceChord(line,nash5_flat, "5");
            line = replaceChord(line,nash5b_flat, "b5");
            line = replaceChord(line,nash6_flat, "6");
            line = replaceChord(line,nash6b_flat, "b6");
            line = replaceChord(line,nash7_flat, "7");
            line = replaceChord(line,nash7b_flat, "b7");
        }

        return line;
    }

    private String replaceBits(String line, String what, String with, String condition) {
        if (condition.equals("sharp") && what.contains("#")) {
            Log.d("Transpose", "replace '"+what+"' with '"+with+"'");
            return replaceChord(line,what,with);
        } else if (condition.equals("flat") && what.contains("b")) {
            Log.d("Transpose", "Fixing "+condition+": replace '"+what+"' with '"+with+"'");return replaceChord(line,what,with);
        } else if (condition.equals("natural") && !what.contains("#") && !what.contains("b")) {
            Log.d("Transpose", "Fixing "+condition+": replace '"+what+"' with '"+with+"'");return replaceChord(line,what,with);
        } else {
            Log.d("Transpose", "Fixing "+condition+": not replacing '"+what+"' with '"+with+"'");
            return line;
        }
    }
    private String fromNashville(String line, boolean numeral) {
        String[] bitssharp;
        String[] bitsflat;
        switch(oldchordformat) {
            case 1:
            default:
                bitssharp = "A A# B C C# D D# E F F# G G# A A# B C C# D D# E F F# G G#".split(" ");
                bitsflat  = "A Bb B C Db D Eb E F Gb G Ab A Bb B C Db D Eb E F Gb G Ab".split(" ");
                break;

            case 2:
            case 3:
                bitssharp = "A B H C C# D D# E F F# G G# A B H C C# D D# E F F# G G#".split(" ");
                bitsflat  = "A B H C Db D Eb E F Gb G Ab A B H C Db D Eb E F Gb G Ab".split(" ");
                break;

            case 4:
                bitssharp = "La La# Si Do Do# Ré Ré# Mi Fa Fa# Sol Sol# La La# Si Do Do# Ré Ré# Mi Fa Fa# Sol Sol#".split(" ");
                bitsflat =  "La Sib Si Do Réb Ré Mib Mi Fa Solb Sol Lab La Sib Si Do Réb Ré Mib Mi Fa Solb Sol Lab".split(" ");
        }

        int root = 0;
        boolean stilllooking = true;
        for (int i=0;i<bitssharp.length;i++) {
            if (stilllooking && (bitssharp[i].equals(originalkey) || (bitssharp[i]+"m").equals(originalkey) ||
                    bitsflat[i].equals(originalkey) || (bitsflat[i]+"m").equals(originalkey))) {
                root = i;
                stilllooking = false;
            }
        }

        boolean ismajor = true;
        if (originalkey.contains("m")) {
            ismajor = false;
        }
        int num2=root+2, num3, num4=root+5, num5=root+7, num6, num7;
        if (ismajor) {
            num3 = root+4;
            num6 = root+9;
            num7 = root+11;
        } else {
            num3 = root+3;
            num6 = root+8;
            num7 = root+10;
        }

        String[] bitsdefault;
        String[] conditions;
        if (originalkey.contains("b")) {
            bitsdefault = bitsflat.clone();
            conditions = new String[]{"flat","sharp","natural"};
        } else {
            bitsdefault = bitssharp.clone();
            conditions = new String[]{"sharp","flat","natural"};
        }

        if (numeral) {
            for (String condition: conditions) {

                line = replaceBits(line,"#VII",bitssharp[root],condition);
                line = replaceBits(line,"#VI",bitssharp[num6+1],condition);
                line = replaceBits(line,"#IV",bitssharp[num4+1],condition);
                line = replaceBits(line,"#V",bitssharp[num5+1],condition);
                line = replaceBits(line,"#III",bitssharp[num3+1],condition);
                line = replaceBits(line,"#II",bitssharp[num2+1],condition);
                line = replaceBits(line,"#I",bitssharp[root+1],condition);

                line = replaceBits(line,"bVII",bitsflat[num7-1],condition);
                line = replaceBits(line,"bVI",bitsflat[num6-1],condition);
                line = replaceBits(line,"bIV",bitsflat[num4-1],condition);
                line = replaceBits(line,"bV",bitsflat[num5-1],condition);
                line = replaceBits(line,"bIII",bitsflat[num3-1],condition);
                line = replaceBits(line,"bII",bitsflat[num2-1],condition);
                line = replaceBits(line,"bI",bitsflat[root+11],condition);

                line = replaceBits(line,"VII",bitsdefault[num7],condition);
                line = replaceBits(line,"VI",bitsdefault[num6],condition);
                line = replaceBits(line,"IV",bitsdefault[num4],condition);
                line = replaceBits(line,"V",bitsdefault[num5],condition);
                line = replaceBits(line,"III",bitsdefault[num3],condition);
                line = replaceBits(line,"II",bitsdefault[num2],condition);
                line = replaceBits(line,"I",bitsdefault[root],condition);

                line = replaceBits(line, "#vii", bitssharp[num7+1]+"m", condition);
                line = replaceBits(line, "#vi", bitssharp[num6+1]+"m", condition);
                line = replaceBits(line, "#iv", bitssharp[num4+1]+"m", condition);
                line = replaceBits(line, "#v", bitssharp[num5+1]+"m", condition);
                line = replaceBits(line, "#iii", bitssharp[num3+1]+"m", condition);
                line = replaceBits(line, "#ii", bitssharp[num2+1]+"m", condition);
                line = replaceBits(line, "#i", bitssharp[root+1]+"m", condition);

                line = replaceBits(line, "bvii", bitsflat[num7-1]+"m", condition);
                line = replaceBits(line, "bvi", bitsflat[num6-1]+"m", condition);
                line = replaceBits(line, "biv", bitsflat[num4-1]+"m", condition);
                line = replaceBits(line, "bv", bitsflat[num5-1]+"m", condition);
                line = replaceBits(line, "biii", bitsflat[num3-1]+"m", condition);
                line = replaceBits(line, "bii", bitsflat[num2-1]+"m", condition);
                line = replaceBits(line, "bi", bitsflat[root+11]+"m", condition);

                if (originalkey.endsWith("m")) {
                    line = replaceBits(line,"vii", bitsdefault[num7]+"m",condition);
                } else {
                    line = replaceBits(line,"vii", bitsdefault[num7]+"o",condition);
                }
                line = replaceBits(line, "vi", bitsdefault[num6]+"m", condition);
                line = replaceBits(line, "iv", bitsdefault[num4]+"m", condition);
                line = replaceBits(line, "v", bitsdefault[num5]+"m", condition);
                line = replaceBits(line, "iii", bitsdefault[num3]+"m", condition);
                if (originalkey.endsWith("m")) {
                    line = replaceBits(line, "ii", bitsdefault[num2]+"o", condition);
                } else {
                    line = replaceBits(line, "ii", bitsdefault[num2]+"m", condition);
                }
                line = replaceBits(line, "i", bitsdefault[root]+"m", condition);

            }


        } else {

            for (String condition: conditions) {

                line = replaceBits(line,".#7","."+bitssharp[root],condition);
                line = replaceBits(line," #7"," "+bitssharp[root],condition);
                line = replaceBits(line,"/#7","/"+bitssharp[root],condition);
                line = replaceBits(line,".#6","."+bitssharp[num6+1],condition);
                line = replaceBits(line," #6"," "+bitssharp[num6+1],condition);
                line = replaceBits(line,"/#6","/"+bitssharp[num6+1],condition);
                line = replaceBits(line,".#5","."+bitssharp[num5+1],condition);
                line = replaceBits(line," #5"," "+bitssharp[num5+1],condition);
                line = replaceBits(line,"/#5","/"+bitssharp[num5+1],condition);
                line = replaceBits(line,".#4","."+bitssharp[num4+1],condition);
                line = replaceBits(line," #4"," "+bitssharp[num4+1],condition);
                line = replaceBits(line,"/#4","/"+bitssharp[num4+1],condition);
                line = replaceBits(line,".#3","."+bitssharp[num3+1],condition);
                line = replaceBits(line," #3"," "+bitssharp[num3+1],condition);
                line = replaceBits(line,"/#3","/"+bitssharp[num3+1],condition);
                line = replaceBits(line,".#2","."+bitssharp[num2+1],condition);
                line = replaceBits(line," #2"," "+bitssharp[num2+1],condition);
                line = replaceBits(line,"/#2","/"+bitssharp[num2+1],condition);
                line = replaceBits(line,".#1","."+bitssharp[root+1],condition);
                line = replaceBits(line," #1"," "+bitssharp[root+1],condition);
                line = replaceBits(line," /1","/"+bitssharp[root+1],condition);

                line = replaceBits(line,".b7","."+bitssharp[num7-1],condition);
                line = replaceBits(line," b7"," "+bitssharp[num7-1],condition);
                line = replaceBits(line,"/b7","/"+bitssharp[num7-1],condition);
                line = replaceBits(line,".b6","."+bitssharp[num6-1],condition);
                line = replaceBits(line," b6"," "+bitssharp[num6-1],condition);
                line = replaceBits(line,"/b6","/"+bitssharp[num6-1],condition);
                line = replaceBits(line,".b5","."+bitssharp[num5-1],condition);
                line = replaceBits(line," b5"," "+bitssharp[num5-1],condition);
                line = replaceBits(line,"/b5","/"+bitssharp[num5-1],condition);
                line = replaceBits(line,".b4","."+bitssharp[num4-1],condition);
                line = replaceBits(line," b4"," "+bitssharp[num4-1],condition);
                line = replaceBits(line,"/b4","/"+bitssharp[num4-1],condition);
                line = replaceBits(line,".b3","."+bitssharp[num3-1],condition);
                line = replaceBits(line," b3"," "+bitssharp[num3-1],condition);
                line = replaceBits(line,"/b3","/"+bitssharp[num3-1],condition);
                line = replaceBits(line,".b2","."+bitssharp[num2-1],condition);
                line = replaceBits(line," b2"," "+bitssharp[num2-1],condition);
                line = replaceBits(line,"/b2","/"+bitssharp[num2-1],condition);
                line = replaceBits(line,".b1","."+bitssharp[root+11],condition);
                line = replaceBits(line," b1"," "+bitssharp[root+11],condition);
                line = replaceBits(line,"/b1","/"+bitssharp[root+11],condition);

                line = replaceBits(line,".7","."+bitsdefault[num7],condition);
                line = replaceBits(line," 7"," "+bitsdefault[num7],condition);
                line = replaceBits(line,"/7","/"+bitsdefault[num7],condition);
                line = replaceBits(line,".6","."+bitsdefault[num6],condition);
                line = replaceBits(line," 6"," "+bitsdefault[num6],condition);
                line = replaceBits(line,"/6","/"+bitsdefault[num6],condition);
                line = replaceBits(line,".5","."+bitsdefault[num5],condition);
                line = replaceBits(line," 5"," "+bitsdefault[num5],condition);
                line = replaceBits(line,"/5","/"+bitsdefault[num5],condition);
                line = replaceBits(line,".4","."+bitsdefault[num4],condition);
                line = replaceBits(line," 4"," "+bitsdefault[num4],condition);
                line = replaceBits(line,"/4","/"+bitsdefault[num4],condition);
                line = replaceBits(line,".3","."+bitsdefault[num3],condition);
                line = replaceBits(line," 3"," "+bitsdefault[num3],condition);
                line = replaceBits(line,"/3","/"+bitsdefault[num3],condition);
                line = replaceBits(line,".2","."+bitsdefault[num2],condition);
                line = replaceBits(line," 2"," "+bitsdefault[num2],condition);
                line = replaceBits(line,"/2","/"+bitsdefault[num2],condition);
                line = replaceBits(line,".1","."+bitsdefault[root],condition);
                line = replaceBits(line," 1"," "+bitsdefault[root],condition);
                line = replaceBits(line,"/1","/"+bitsdefault[root],condition);

            }
        }
        if (!line.startsWith(".")) {
            line = "."+line;
        }
        return line;
    }

    public String transposeSetVariationKey(String originalKey, int transposeTimes) {
        // Done for variations created from sets with specified keys
        String keyNum = keyToNumber(originalKey);
        return transposeKey(keyNum,"+1",transposeTimes);
    }

    public String transposeKey(String getkeynum, String direction, int transposetimes) {
        if (direction.equals("+1")) {
            // Put the numbers up by one.
            // Last step then fixes 13 to be 1

            // Repeat this as often as required.
            for (int repeatTranspose = 0; repeatTranspose < transposetimes; repeatTranspose++) {
                getkeynum = getkeynum.replace("$.12.$", "$.13.$");
                getkeynum = getkeynum.replace("$.11.$", "$.12.$");
                getkeynum = getkeynum.replace("$.10.$", "$.11.$");
                getkeynum = getkeynum.replace("$.09.$", "$.10.$");
                getkeynum = getkeynum.replace("$.08.$", "$.09.$");
                getkeynum = getkeynum.replace("$.07.$", "$.08.$");
                getkeynum = getkeynum.replace("$.06.$", "$.07.$");
                getkeynum = getkeynum.replace("$.05.$", "$.06.$");
                getkeynum = getkeynum.replace("$.04.$", "$.05.$");
                getkeynum = getkeynum.replace("$.03.$", "$.04.$");
                getkeynum = getkeynum.replace("$.02.$", "$.03.$");
                getkeynum = getkeynum.replace("$.01.$", "$.02.$");
                getkeynum = getkeynum.replace("$.13.$", "$.01.$");

                getkeynum = getkeynum.replace("$.42.$", "$.43.$");
                getkeynum = getkeynum.replace("$.41.$", "$.42.$");
                getkeynum = getkeynum.replace("$.40.$", "$.41.$");
                getkeynum = getkeynum.replace("$.39.$", "$.40.$");
                getkeynum = getkeynum.replace("$.38.$", "$.39.$");
                getkeynum = getkeynum.replace("$.37.$", "$.38.$");
                getkeynum = getkeynum.replace("$.36.$", "$.37.$");
                getkeynum = getkeynum.replace("$.35.$", "$.36.$");
                getkeynum = getkeynum.replace("$.34.$", "$.35.$");
                getkeynum = getkeynum.replace("$.33.$", "$.34.$");
                getkeynum = getkeynum.replace("$.32.$", "$.33.$");
                getkeynum = getkeynum.replace("$.31.$", "$.32.$");
                getkeynum = getkeynum.replace("$.43.$", "$.31.$");

                getkeynum = getkeynum.replace("$.62.$", "$.63.$");
                getkeynum = getkeynum.replace("$.61.$", "$.62.$");
                getkeynum = getkeynum.replace("$.60.$", "$.61.$");
                getkeynum = getkeynum.replace("$.59.$", "$.60.$");
                getkeynum = getkeynum.replace("$.58.$", "$.59.$");
                getkeynum = getkeynum.replace("$.57.$", "$.58.$");
                getkeynum = getkeynum.replace("$.56.$", "$.57.$");
                getkeynum = getkeynum.replace("$.55.$", "$.56.$");
                getkeynum = getkeynum.replace("$.54.$", "$.55.$");
                getkeynum = getkeynum.replace("$.53.$", "$.54.$");
                getkeynum = getkeynum.replace("$.52.$", "$.53.$");
                getkeynum = getkeynum.replace("$.51.$", "$.52.$");
                getkeynum = getkeynum.replace("$.63.$", "$.51.$");
            }


        } else if (direction.equals("-1")) {
            // Put the numbers down by one.
            // Last step then fixes 0 to be 12

            // Repeat this as often as required.
            for (int repeatTranspose = 0; repeatTranspose < transposetimes; repeatTranspose++) {
                getkeynum = getkeynum.replace("$.01.$", "$.0.$");
                getkeynum = getkeynum.replace("$.02.$", "$.01.$");
                getkeynum = getkeynum.replace("$.03.$", "$.02.$");
                getkeynum = getkeynum.replace("$.04.$", "$.03.$");
                getkeynum = getkeynum.replace("$.05.$", "$.04.$");
                getkeynum = getkeynum.replace("$.06.$", "$.05.$");
                getkeynum = getkeynum.replace("$.07.$", "$.06.$");
                getkeynum = getkeynum.replace("$.08.$", "$.07.$");
                getkeynum = getkeynum.replace("$.09.$", "$.08.$");
                getkeynum = getkeynum.replace("$.10.$", "$.09.$");
                getkeynum = getkeynum.replace("$.11.$", "$.10.$");
                getkeynum = getkeynum.replace("$.12.$", "$.11.$");
                getkeynum = getkeynum.replace("$.0.$", "$.12.$");

                getkeynum = getkeynum.replace("$.31.$", "$.30.$");
                getkeynum = getkeynum.replace("$.32.$", "$.31.$");
                getkeynum = getkeynum.replace("$.33.$", "$.32.$");
                getkeynum = getkeynum.replace("$.34.$", "$.33.$");
                getkeynum = getkeynum.replace("$.35.$", "$.34.$");
                getkeynum = getkeynum.replace("$.36.$", "$.35.$");
                getkeynum = getkeynum.replace("$.37.$", "$.36.$");
                getkeynum = getkeynum.replace("$.38.$", "$.37.$");
                getkeynum = getkeynum.replace("$.39.$", "$.38.$");
                getkeynum = getkeynum.replace("$.40.$", "$.39.$");
                getkeynum = getkeynum.replace("$.41.$", "$.40.$");
                getkeynum = getkeynum.replace("$.42.$", "$.41.$");
                getkeynum = getkeynum.replace("$.30.$", "$.42.$");

                getkeynum = getkeynum.replace("$.51.$", "$.50.$");
                getkeynum = getkeynum.replace("$.52.$", "$.51.$");
                getkeynum = getkeynum.replace("$.53.$", "$.52.$");
                getkeynum = getkeynum.replace("$.54.$", "$.53.$");
                getkeynum = getkeynum.replace("$.55.$", "$.54.$");
                getkeynum = getkeynum.replace("$.56.$", "$.55.$");
                getkeynum = getkeynum.replace("$.57.$", "$.56.$");
                getkeynum = getkeynum.replace("$.58.$", "$.57.$");
                getkeynum = getkeynum.replace("$.59.$", "$.58.$");
                getkeynum = getkeynum.replace("$.60.$", "$.59.$");
                getkeynum = getkeynum.replace("$.61.$", "$.60.$");
                getkeynum = getkeynum.replace("$.62.$", "$.61.$");
                getkeynum = getkeynum.replace("$.50.$", "$.62.$");
            }
        }
        return getkeynum;
    }

    private String[] transposeChords(String[] splitLyrics, String transposeDirection, int transposeTimes) {
        // Go through each line in turn
        for (int x = 0; x < splitLyrics.length; x++) {

            // Only do transposing if it is a chord line (starting with .)
            if (splitLyrics[x].startsWith(".")) {
                if (transposeDirection.equals("+1")) {
                    // Put the numbers up by one.
                    // Last step then fixes 12 to be 0

                    // Repeat this as often as required.
                    for (int repeatTranspose = 0; repeatTranspose < transposeTimes; repeatTranspose++) {
                        splitLyrics[x] = splitLyrics[x].replace("$.12.$", "$.13.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.11.$", "$.12.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.10.$", "$.11.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.09.$", "$.10.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.08.$", "$.09.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.07.$", "$.08.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.06.$", "$.07.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.05.$", "$.06.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.04.$", "$.05.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.03.$", "$.04.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.02.$", "$.03.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.01.$", "$.02.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.13.$", "$.01.$");

                        splitLyrics[x] = splitLyrics[x].replace("$.42.$", "$.43.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.41.$", "$.42.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.40.$", "$.41.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.39.$", "$.40.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.38.$", "$.39.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.37.$", "$.38.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.36.$", "$.37.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.35.$", "$.36.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.34.$", "$.35.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.33.$", "$.34.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.32.$", "$.33.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.31.$", "$.32.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.43.$", "$.31.$");

                        splitLyrics[x] = splitLyrics[x].replace("$.62.$", "$.63.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.61.$", "$.62.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.60.$", "$.61.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.59.$", "$.60.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.58.$", "$.59.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.57.$", "$.58.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.56.$", "$.57.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.55.$", "$.56.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.54.$", "$.55.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.53.$", "$.54.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.52.$", "$.53.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.51.$", "$.52.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.63.$", "$.51.$");
                    }
                }

                if (transposeDirection.equals("-1")) {
                    // Put the numbers down by one.
                    // Move num 0 down to -1 (if it goes to 11 it will be moved
                    // later)
                    // Last step then fixes -1 to be 11

                    // Repeat this as often as required.
                    for (int repeatTranspose = 0; repeatTranspose < transposeTimes; repeatTranspose++) {
                        splitLyrics[x] = splitLyrics[x].replace("$.01.$", "$.0.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.02.$", "$.01.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.03.$", "$.02.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.04.$", "$.03.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.05.$", "$.04.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.06.$", "$.05.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.07.$", "$.06.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.08.$", "$.07.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.09.$", "$.08.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.10.$", "$.09.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.11.$", "$.10.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.12.$", "$.11.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.0.$", "$.12.$");

                        splitLyrics[x] = splitLyrics[x].replace("$.31.$", "$.30.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.32.$", "$.31.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.33.$", "$.32.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.34.$", "$.33.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.35.$", "$.34.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.36.$", "$.35.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.37.$", "$.36.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.38.$", "$.37.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.39.$", "$.38.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.40.$", "$.39.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.41.$", "$.40.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.42.$", "$.41.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.30.$", "$.42.$");

                        splitLyrics[x] = splitLyrics[x].replace("$.51.$", "$.50.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.52.$", "$.51.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.53.$", "$.52.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.54.$", "$.53.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.55.$", "$.54.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.56.$", "$.55.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.57.$", "$.56.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.58.$", "$.57.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.59.$", "$.58.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.60.$", "$.59.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.61.$", "$.60.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.62.$", "$.61.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.50.$", "$.62.$");
                    }
                }
            }
        }
        return splitLyrics;
    }


    private String numberToChord1(String line, boolean thisorcapousesflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (thisorcapousesflats) {
            line = useFlats1(line);
        } else {
            line = useSharps1(line);
        }
        // Replace the naturals
        line = useNaturals1(line);
        return line;
    }


    private boolean keyUsesFlats(Context c, MainActivityInterface mainActivityInterface, String testkey) {

        boolean result;
        result = (testkey.equals("Ab") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyAb",true)) ||
                (testkey.equals("Bb") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyBb",true)) ||
                (testkey.equals("Db") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyDb",false)) ||
                (testkey.equals("Eb") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyEb",true)) ||
                (testkey.equals("Gb") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyGb",false)) ||
                (testkey.equals("Bbm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyBbm",true)) ||
                (testkey.equals("Dbm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyDbm",false)) ||
                (testkey.equals("Ebm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyEbm",true)) ||
                (testkey.equals("Gbm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prefKeyGbm",false)) ||
                testkey.equals("C") ||
                testkey.equals("F") ||
                testkey.equals("Dm") ||
                testkey.equals("Gm") ||
                testkey.equals("Cm");
        return result;
    }

    private String useFlats1(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = replaceChord(line,properchordflatsnumsa[z],properflatchords1a[z]);
        }
        return line;
    }

    private String useSharps1(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = replaceChord(line,properchordsharpsnumsa[z],propersharpchords1a[z]);
        }
        return line;
    }

    private String useNaturals1(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = replaceChord(line,chordnaturalnumsa[z],naturalchords1a[z]);
        }
        return line;
    }

    private String fixDiminished(String line) {
        line = replaceChord(line,"m7b5", "ø"); // Half diminished
        line = replaceChord(line,"-7b5", "ø"); // Half diminished

        line = replaceChord(line,"dim7", "o"); // Diminished
        line = replaceChord(line,"dim", "o");
        line = replaceChord(line,"o7", "o");
        return line;
    }

    private String fixMajorSeventh(String line) {
        line = replaceChord(line,"maj7","Δ7");
        line = replaceChord(line,"ma7", "Δ7");
        return line;
    }

    private String fixAugmented(String line) {
        line = replaceChord(line,"aug","+");
        line = replaceChord(line,"#5","+");
        return line;
    }

    public Song checkChordFormat(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        String[] splitLyrics = thisSong.getLyrics().split("\n");
        thisSong.setDetectedChordFormat(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "chordFormat", 1));

        // The user wants the app to guess the chord formatting, so we will detect formatting
        boolean contains_es_is = false;
        boolean contains_H = false;
        boolean contains_do = false;
        boolean contains_nash = false;
        boolean contains_nashnumeral = false;

        // Check if the user is using the same chord format as the song
        // Go through the chord lines and look for clues
        for (String splitLyric : splitLyrics) {
            if (splitLyric.startsWith(".")) {
                // Chord line
                if (splitLyric.contains("es") || splitLyric.contains("is") ||
                        splitLyric.contains(" a") || splitLyric.contains(".a") ||
                        //splitLyrics[x].contains(" b") || splitLyrics[x].contains(".b") || // Can't use for flat numeral chords
                        splitLyric.contains(" h") || splitLyric.contains(".h") ||
                        splitLyric.contains(" c") || splitLyric.contains(".c") ||
                        splitLyric.contains(" d") || splitLyric.contains(".d") ||
                        splitLyric.contains(" e") || splitLyric.contains(".e") ||
                        splitLyric.contains(" f") || splitLyric.contains(".f") ||
                        splitLyric.contains(" g") || splitLyric.contains(".g")) {
                    contains_es_is = true;
                } else if (splitLyric.contains("H")) {
                    contains_H = true;
                } else if (splitLyric.contains("Do") || splitLyric.contains("Re") || splitLyric.contains("Ré") ||
                        splitLyric.contains("Me") || splitLyric.contains("Fa") ||
                        splitLyric.contains("Sol") || splitLyric.contains("La") ||
                        splitLyric.contains("Si")) {
                    contains_do = true;
                } else if (splitLyric.contains(".2") || splitLyric.contains(" 2") ||
                        splitLyric.contains(".3") || splitLyric.contains(" 3") ||
                        splitLyric.contains(".4") || splitLyric.contains(" 4") ||
                        splitLyric.contains(".5") || splitLyric.contains(" 5") ||
                        splitLyric.contains(".6") || splitLyric.contains(" 6") ||
                        splitLyric.contains(".7") || splitLyric.contains(" 7")) {
                    contains_nash = true;
                } else if (splitLyric.contains(".I") || splitLyric.contains(" I") ||
                        splitLyric.contains(".V") || splitLyric.contains(" V") ||
                        splitLyric.contains(".IV") || splitLyric.contains(" IV")) {
                    contains_nashnumeral = true;
                }
            }
        }

        //int detected = 0;
        // Set the chord style detected
        if (contains_do && !mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            thisSong.setDetectedChordFormat(4);
        } else if (contains_H && !contains_es_is && !mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            thisSong.setDetectedChordFormat(2);
        } else if ((contains_H || contains_es_is) && !mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            thisSong.setDetectedChordFormat(3);
        } else if (contains_nash && !mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            thisSong.setDetectedChordFormat(5);
        } else if (contains_nashnumeral && !mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            thisSong.setDetectedChordFormat(6);
        } else {
            thisSong.setDetectedChordFormat(1);
        }

        // Ok so the user chord format may not quite match the song - it might though!
        return thisSong;
    }

    private String replaceChord(String line, String inChord, String outChord) {
        // IV - Add markers indicating the need to add or remove spaces to adjust for a difference of size of the original and replacemnet chord
        if (outChord.length() < inChord.length()) {
            outChord = outChord + "»»»»»»»»".substring(0,inChord.length() - outChord.length());
        }
        if (outChord.length() > inChord.length()) {
            outChord = outChord + "««««««««".substring(0,outChord.length() - inChord.length());
        }
        line = line.replace(inChord, outChord);
        // IV - Simplify
        while (line.contains("»«")) {
            line = line.replace("»«", "");
        }
        while (line.contains("«»")) {
            line = line.replace("«»", "");
        }
        return line;
    }



    private Song sortTheTransposedKey(Context c, MainActivityInterface mainActivityInterface,
                                      Song thisSong, String transposeDirection, int transposeTimes) {
        if (thisSong.getKey() != null && !thisSong.getKey().isEmpty()) {
            String newkey = keyToNumber(thisSong.getKey());

            // Transpose the key
            newkey = transposeKey(newkey, transposeDirection, transposeTimes);

            // Convert the keynumber to a key
            newkey = numberToKey(c, mainActivityInterface, newkey);

            // Decide if flats should be used
            usesFlats = keyUsesFlats(c, mainActivityInterface, newkey);

            thisSong.setKey(newkey);
        }
        return thisSong;
    }

*/
}
