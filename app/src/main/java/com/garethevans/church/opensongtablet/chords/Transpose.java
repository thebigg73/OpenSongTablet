package com.garethevans.church.opensongtablet.chords;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class Transpose {

    // Chord format 1:  Normal A A#/Bb B C C#/Db
    // Chord format 2:  Euro   A B H C#/D#
    // Chord format 3:  Euro   A B H Cis/Des
    // Chord format 4:  Solf   DO RE MI FA
    // Chord format 5:  Nash   1 2 3 4 5
    // Chord format 6:  Num    I II III IX
    // Chord format 7:  Euro   A Bb B Cis/Des   - similar to 3, but not using B/H or Bb/B

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Transpose";

    private ArrayList<String> chordFormatNames, chordFormatAppearances;
    private final Song miniTransposeSong = new Song();
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private boolean convertToFlats = false, convertToSharps = false;

    public Transpose(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    //  A  A#/Bb  B/(Cb) C/(B#) C#/Db    D    D#/Eb  E/(Fb) (E#)/F   F#/Gb   G     G#/Ab
    //  A    B     H      C
    //  1    2     3      4      5      6      7      8      9     W(10)  X(11)   Y(12)
    // On transpose Cb -> B, B# -> C, Fb -> E,  E# -> F
    //
    // A 'number' format ├y┤ is used - y is the 'position in key' as above and ├ and ┤ are start and end markers
    // A 'number' must end ┤ except where a format has explicit minors where it will end ┤m
    // « and » before an item indicates a need to later consider a remove or add of a space after a chord
    //
    // Transpose example:  Format 1 to Format 3 Capo 1
    //   Line starts in 1 Standard                        - ???    Fm    ???
    //   From '1 Standard' F is replaced to number ««├9┤  - ???    ««├9┤m    ???
    //   The number is transposed 1 time from 9 to W(10)  - ???    ««├W┤m    ???
    //   from number ├W┤ is replaced to 3 Solfege «Solb   - ???    «««Solbm    ???
    //   '«««' is processed to remove 3 following spaces  - ???    Solbm ???
    //   In effect Solbm overwrites Fm and 3 spaces
    //
    // Replaces occur in order, with this order a necessary part of the logic.
    // Example 'protect' logic: Replace "maj7" to "¬aj7" to stop the m being treated as a minor, is followed by replace "¬" with "m" to restore it.
    // Example 'variation handling' logic: replace 'TI' with "SI, "Ti" with "SI" and "ti" with "SI" is followed by replace of "SI' with chord number "«├3┤"
    // Three variants "TI", "Ti" and "ti" are therefore handled the same as SI.

    // Chord to number: majors and sustain forms interfere so have 'protect' logic

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
            " «├2┤m",   " «├4┤m",   " «├5┤m",   " «├7┤m",   " «├9┤m",   " «├W┤m",   " «├Y┤m",
            " ««├Y┤m",  " «««├2┤m", " «├3┤m",   " «├5┤m",   " ««├7┤m",  " «├8┤m",   " «├W┤m",
            " «««├1┤m", " «««├3┤m", " «««├4┤m", " «««├6┤m", " «««├8┤m", " «««├9┤m", " «««├X┤m",
            "├2┤",      "├4┤",      "├5┤",      "├7┤",      "├9┤",      "├W┤",      "├Y┤",
            "«├Y┤",     "««├2┤",    "├3┤",      "├5┤",      "«├7┤",     "├8┤",      "├W┤",
            "««├1┤",    "««├3┤",    "««├4┤",    "««├6┤",    "««├8┤",    "««├9┤",    "««├X┤",    "sus", "m"};

    private final String[] fromChords7 =       {"maj7",     "ma7",      "maj9",     "ma9",      "sus",
            " (ais",    " (bis",   " (cis",    " (dis",    " (eis",    " (fis",    " (gis",
            " (as",     " (bes",   " (ces",    " (des",    " (es",     " (fes",    " (ges",
            " (a",      " (b",      " (c",      " (d",      " (e",      " (f",      " (g",
            " ais",     " bis",     " cis",     " dis",     " eis",     " fis",     " gis",
            " as",      " bes",     " ces",     " des",     " es",      " fes",     " ges",
            " a",       " b",       " c",       " d",       " e",       " f",       " g",
            "Ais",      "Bis",      "Cis",      "Dis",      "Eis",      "Fis",      "Gis",
            "As",       "Bes",      "Ces",      "Des",      "Es",       "Fes",      "Ges",
            "A",        "B",        "C",        "D",        "E",        "F",        "G",        "¬us", "¬"};

    private final String[] toChordsNums7 =     {"¬aj7",     "¬a7",      "¬aj9",     "¬a9",      "¬us",
            " («├2┤m",  " («├4┤m",  " («├5┤m",  " («├7┤m",  " («├9┤m",  " («├W┤m",  " («├Y┤m",
            " (««├Y┤m", " («««├2┤m"," («├3┤m",  " («├5┤m",  " (««├7┤m", " («├8┤m",  " («├W┤m",
            " («««├1┤m"," («««├3┤m"," («««├4┤m"," («««├6┤m"," («««├8┤m"," («««├9┤m"," («««├X┤m",
            " «├2┤m",   " «├4┤m",   " «├5┤m",   " «├7┤m",   " «├9┤m",   " «├W┤m",   " «├Y┤m",
            " ««├Y┤m",  " «├2┤m",   " «├3┤m",   " «├5┤m",   " ««├7┤m",  " «├8┤m",   " «├W┤m",
            " «««├1┤m", " «««├3┤m", " «««├4┤m", " «««├6┤m", " «««├8┤m", " «««├9┤m", " «««├X┤m",
            "├2┤",      "├4┤",      "├5┤",      "├7┤",      "├9┤",      "├W┤",      "├Y┤",
            "«├Y┤",     "├2┤",      "├3┤",      "├5┤",      "«├7┤",     "├8┤",      "├W┤",
            "««├1┤",    "««├3┤",    "««├4┤",    "««├6┤",    "««├8┤",    "««├9┤",    "««├X┤",    "sus", "m"};

    // Solfeggio (DoReMi) = 4
    // Also handles variations of chord name
    // Solfege variants
    private final String[] fromChords4 =    {"maj7", "ma7", "maj9", "ma9",
            // Variation handling: Transpose á é ó and case variants to 'uppercase, no accent' chords
            "La", "la", "LÁ", "Lá", "lá",
            "TI", "Ti", "ti",
            "Si", "si",
            "UT", "Ut", "ut",
            "Do", "do", "DÓ", "Dó", "dó",
            "Re", "re", "RÉ", "Ré", "ré",
            "Mi", "mi",
            "Fa", "fa", "FÁ", "Fá", "fá",
            "Sol", "sol",
            // Now, transpose 'uppercase, no accent' chords.
            "LA#",    "SI#",    "DO#",    "RE#",    "MI#",    "FA#",    "SOL#",
            "LAb",    "SIb",    "DOb",    "REb",    "MIb",    "FAb",    "SOLb",
            "LA",     "SI",     "DO",     "RE",     "MI",     "FA",     "SOL",   "¬"};
    private final String[] toChordsNums4 =  {"¬aj7", "¬a7", "¬aj9", "¬a9",
            "LA", "LA", "LA", "LA", "LA",
            "SI", "SI", "SI",
            "SI", "SI",
            "DO", "DO", "DO",
            "DO", "DO", "DO", "DO", "DO",
            "RE", "RE", "RE", "RE", "RE",
            "MI", "MI",
            "FA", "FA", "FA", "FA", "FA",
            "SOL", "SOL",
            "├2┤",    "├4┤",    "├5┤",    "├7┤",    "├9┤",    "├W┤",   "»├Y┤",
            "├Y┤",    "├2┤",    "├3┤",    "├5┤",    "├7┤",    "├8┤",   "»├W┤",
            "«├1┤",   "«├3┤",   "«├4┤",   "«├6┤",   "«├8┤",   "«├9┤",    "├X┤",    "m"};
    private String[] fromNash;
    private String[] toChordNumsNash;

    // For working out auto transpose times
    private final String[] keyText = new String[] {"A","A#","Bb","B","C","C#","Db","D","D#","Eb","E","F","F#","Gb","G","G#","Ab",
            "A","A#","Bb","B","C","C#","Db","D","D#","Eb","E","F","F#","Gb","G","G#","Ab"};
    private final int[] keyNum     = new int[]    { 0,  1,   1,   2,  3,  4,   4,   5,  6,   6,   7,  8,  9,   9,   10, 11,  11,
            12, 13,  13,  14, 15, 16,  16,  17, 18,  18,  19, 20, 21,  21,  22, 23,  23};

    // Number to chord:
    private final String[] fromChordsNum  = "├2┤ ├5┤ ├7┤ ├W┤ ├Y┤ ├1┤ ├3┤ ├4┤ ├6┤ ├8┤ ├9┤ ├X┤".split(" ");
    private final String[] toSharpChords1 = "»A# »C# »D# »F# »G# »»A »»B »»C »»D »»E »»F »»G".split(" ");
    private final String[] toFlatChords1 =  "»Bb »Db »Eb »Gb »Ab »»A »»B »»C »»D »»E »»F »»G".split(" ");
    private final String[] toSharpChords2 = "»A# »C# »D# »F# »G# »»A »»H »»C »»D »»E »»F »»G".split(" ");
    private final String[] toFlatChords2 =  "»»B »Db »Eb »Gb »Ab »»A »»H »»C »»D »»E »»F »»G".split(" ");
    // IV - Solfege out format is 'SongSelect fixed DO' - Capitals = easy to read, no accents = easy to type, DO not UT and SI not TI = most common across solfege variants
    private final String[] toSharpChords4 = "LA# DO# RE# FA# «SOL# »LA »SI »DO »RE »MI »FA SOL".split(" ");
    private final String[] toFlatChords4 =  "SIb REb MIb «SOLb LAb »LA »SI »DO »RE »MI »FA SOL".split(" ");
    //  A trick! Minors arrive ending ┤m, the m is moved into the number to give numbers for minors. '┤ma' is treated as the start of major and is protected.
    private final String[] fromChordsNumM = "┤ma ┤m ├2m┤ ├5m┤ ├7m┤ ├Wm┤ ├Ym┤ ├1m┤ ├3m┤ ├4m┤ ├6m┤ ├8m┤ ├9m┤ ├Xm┤ ├2┤ ├5┤ ├7┤ ├W┤ ├Y┤ ├1┤ ├3┤ ├4┤ ├6┤ ├8┤ ├9┤ ├X┤ ¬".split(" ");
    private final String[] toSharpChords3 = "┤¬a m┤ »ais »cis »dis »fis »gis »»»a »»»h »»»c »»»d »»»e »»»f »»»g »Ais Cis Dis Fis Gis »»A »»H »»C »»D »»E »»F »»G m".split(" ");
    private final String[] toSharpChords7 = "┤¬a m┤ »ais »cis »dis »fis »gis »»»a »»»b »»»c »»»d »»»e »»»f »»»g »Ais Cis Dis Fis Gis »»A »»B »»C »»D »»E »»F »»G m".split(" ");
    private final String[] toFlatChords3 =  "┤¬a m┤ »»»b »des »»es »ges »»as »»»a »»»h »»»c »»»d »»»e »»»f »»»g »»B Des »Es Ges »As »»A »»H »»C »»D »»E »»F »»G m".split(" ");
    private final String[] toFlatChords7 =  "┤¬a m┤ »bes »des »»es »ges »»as »»»a »»»b »»»c »»»d »»»e »»»f »»»g Bes Des »Es Ges »As »»A »»B »»C »»D »»E »»F »»G m".split(" ");

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

    private String capoKey;
    private int oldChordFormat, newChordFormat;
    private String transposeDirection;
    private int transposeTimes;

    private final String[] format2Identifiers = new String[]{"h"};
    // * Format 3 has lowercase minors. 'b' is 'flat', "h" is dealt with separately, so both are not tested
    // is/es identifiers are dealt with as 'chord ends with s' later in the logic
    private final String[] format3Identifiers = new String[]{"a","c","d","e,","f","g"}; // See *
    private final String[] format4Identifiers = new String[]{"do","dó","re","ré","mi","fa","fá","sol","la","lá", "si","ti"};
    private final String[] format5Identifiers = new String[]{"1","2","3","4","5","6","7"};
    private final String[] format6Identifiers = new String[]{"i","ii","ii","iii","iii","iv","iv","v","vi","vii"};

    // The song is sent in and the song is sent back after processing (key and lyrics get changed)
    public Song doTranspose(Song thisSong, String transposeDirection, int transposeTimes,
                            int oldChordFormat, int newChordFormat) {

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

        // Check if we have an original key set, and if not, set with the current key
        checkOriginalKeySet(thisSong);

        try {
            String startKey = thisSong.getKey();
            // Update the key to the newly transposed version
            if (startKey != null && !startKey.isEmpty()) {
                thisSong.setKey(numberToKey(transposeNumber(keyToNumber(startKey), transposeDirection, transposeTimes)));
            }

            // Change the song format
            thisSong.setDetectedChordFormat(oldChordFormat);
            thisSong.setDesiredChordFormat(newChordFormat);

            // Transpose and write the lyrics
            thisSong.setLyrics(transposeString(thisSong));

            // Put in the new chord format
            thisSong.setDetectedChordFormat(newChordFormat);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return thisSong;
    }

    // Stuff for dealing with the song key
    public String getKeyBeforeCapo(int capo, String oldkey) {
        Song tempSong = new Song();
        tempSong.setLyrics("."+oldkey);
        oldChordFormat = 1;
        newChordFormat = 1;
        tempSong.setDetectedChordFormat(1);
        tempSong.setDesiredChordFormat(1);
        transposeDirection = "-1";
        transposeTimes = capo;

        // IV - Number converts are used to return the users preferred key
        return numberToKey(keyToNumber(transposeString(tempSong).replace(".","")));
    }

    public String keyToNumber(String key) {
        // Swap the key with the chord number - use standard formatting=1
        if (key!=null && fromChords1!=null && toChordsNums1!=null) {
            for (int z = 0; z < fromChords1.length; z++)
                key = key.replace(fromChords1[z], toChordsNums1[z]);
        }
        return key;
    }

    public String numberToKey(String key) {
        // We need to decide which key the user likes the best for each one
        // Convert the key number into either a sharp or natural first
        // Then we swap sharps to flats if the user prefers these
        for (int z = 0; z < fromChordsNum.length; z++) {
            key = key.replace(fromChordsNum[z], toSharpChords1[z]);
        }

        // Remove chord space adjustment indicators
        key = key.replace("»","").replace("«","");

        if (key.equals("G#") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Ab",true)) {
            key = "Ab";
        } else if (key.equals("G#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Abm",false)) {
            key = "Abm";
        } else if (key.equals("A#")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Bb",true)) {
            key = "Bb";
        } else if (key.equals("A#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Bbm",true)) {
            key = "Bbm";
        } else if (key.equals("C#")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Db",false)) {
            key = "Db";
        } else if (key.equals("C#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Dbm",true)) {
            key = "Dbm";
        } else if (key.equals("D#")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Eb",true)) {
            key = "Eb";
        } else if (key.equals("D#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Ebm",true)) {
            key = "Ebm";
        } else if (key.equals("F#")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Gb",false)) {
            key = "Gb";
        } else if (key.equals("F#m") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Gbm",false)) {
            key = "Gbm";
        }

        return key;
    }

    private boolean keyUsesFlats(String testkey) {
        return  (testkey.equals("Ab")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Ab",true)) ||
                (testkey.equals("Bb")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Bb",true)) ||
                (testkey.equals("Db")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Db",false)) ||
                (testkey.equals("Eb")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Eb",true)) ||
                (testkey.equals("Gb")  && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Gb",false)) ||
                (testkey.equals("Bbm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Bbm",true)) ||
                (testkey.equals("Dbm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Dbm",false)) ||
                (testkey.equals("Ebm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Ebm",true)) ||
                (testkey.equals("Gbm") && mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_Gbm",false)) ||
                testkey.equals("C") ||
                testkey.equals("F") ||
                testkey.equals("Dm") ||
                testkey.equals("Gm") ||
                testkey.equals("Cm");
    }

    public String transposeChordForCapo(int capo, String string) {
        // Use the miniTransposeSong
        miniTransposeSong.setLyrics(string);
        miniTransposeSong.setCapo(String.valueOf(capo));
        miniTransposeSong.setDetectedChordFormat(mainActivityInterface.getSong().getDetectedChordFormat());
        miniTransposeSong.setDesiredChordFormat(mainActivityInterface.getSong().getDesiredChordFormat());
        transposeTimes = capo;
        transposeDirection = "-1";
        miniTransposeSong.setKey(capoKey);
        return transposeString(miniTransposeSong);
    }

    // This is the chord transpose engine
    public String transposeString(Song thisSong) {

        // Now we have the new key, we can decide if we use flats or not for any notes
        boolean forceFlats = thisSong.getKey() != null && (keyUsesFlats(thisSong.getKey()) || convertToFlats);

        // If we want to force sharps, overrule the forceFlats
        if (convertToSharps) {
            forceFlats = false;
        }

        try {
            StringBuilder sb = new StringBuilder();

            // IV - Add a trailing ¶ to force a split behaviour that copes with a trailing new line!
            for (String line : (thisSong.getLyrics()+"¶").split("\n")) {
                // IV - Use leading \n as we can be certain it is safe to remove later
                sb.append("\n");
                if (line.startsWith(".")) {
                    //Log.d(TAG,"PRETRANSPOSE LINE: "+line);
                    line = line.replaceFirst("."," ");
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
                        case 7:
                            //Log.d(TAG,"Original line:"+line);
                            for (int z = 0; z < fromChords7.length; z++) {
                                line = line.replace(fromChords7[z], toChordsNums7[z]);
                            }
                            //Log.d(TAG,"As numbers   :"+line);
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

                    // If the old format has transposable chords - transpose
                    if (oldChordFormat < 5 || oldChordFormat == 7) {
                        line = transposeNumber(line, transposeDirection, transposeTimes);
                        //Log.d(TAG,"MIDTRANSPOSE LINE: "+line);
                    }
                    //Log.d(TAG,"newChordFormat="+newChordFormat);
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
                        case 7:
                            // IV - Uses the 'm' array as the target has explicit minors
                            if (forceFlats) for (int z = 0; z < fromChordsNumM.length; z++) {
                                line = line.replace(fromChordsNumM[z], toFlatChords7[z]);
                            } else {
                                for (int z = 0; z < fromChordsNumM.length; z++) {
                                    line = line.replace(fromChordsNumM[z], toSharpChords7[z]);
                                }
                            }
                            //Log.d(TAG,"New line      :"+line);
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

                    //Log.d(TAG,"NEARLY DONE LINE: "+line);

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
                    //Log.d(TAG,"TRANSPOSED LINE: "+line);
                    line = line.replaceFirst(" ",".");
                }

                // Add it back up
                sb.append(line);
            }

            // Reset the convertToFlats/convertSharps
            convertToFlats = false;
            convertToSharps = false;

            // Return the string removing the added leading \n and trailing ¶
            return sb.substring(1).replace("¶", "");

        } catch (Exception e) {
            // Reset the convertToFlats/convertSharps
            convertToFlats = false;
            convertToSharps = false;
            // Just return the lyrics with no change as there was an issue
            return thisSong.getLyrics();
        }
    }

    public String transposeNumber(String string, String direction, int numtimes) {
        // Rotate the scale the number of times requested
        String replaceNumber = "1·2·3·4·5·6·7·8·9·W·X·Y·";
        if (direction.equals("-1")) {
            for (int x = 0; x < numtimes; x++) {
                replaceNumber = replaceNumber.substring(22) + replaceNumber.substring(0, 22);
            }
        } else {
            for (int x = 0; x < numtimes; x++) {
                replaceNumber = replaceNumber.substring(2) + replaceNumber.substring(0, 2);
            }
        }

        // For the '├y┤' number format we transpose y only. Replace y┤ with transposed y· and then · with ┤ as this prevents errors.
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

                // Includes protection for major chords and support for different 'chord follows' sequences including '<space>('
                fromNash = ("maj7_ma7_maj9_ma9_" +
                        fromnashbase.replace("¬"," ") +
                        fromnashbase.replace("¬","/") +
                        fromnashbase.replace("¬"," (") +
                        "¬").split("_");

                toChordNumsNash = ("¬aj7_¬a7_¬aj9_¬a9_" +
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

    public String capoKeyTranspose(Song thisSong) {
        if (thisSong.getKey()!=null &&
                thisSong.getCapo()!=null &&
                !thisSong.getKey().isEmpty() &&
                !thisSong.getCapo().isEmpty()) {
            capoKey = numberToKey(transposeNumber(keyToNumber(thisSong.getKey()),
                    "-1", Integer.parseInt("0" + thisSong.getCapo())));
            return capoKey;
        } else {
            return "";
        }
    }

    private ArrayList<String> quickCapoKey(String key) {
        // This is used to give the user a list starting with blank of either simple fret number or fret number with new capo key
        ArrayList<String> al = new ArrayList<>(Collections.singletonList(""));
        if (key != null && !key.isEmpty()) {
            for (int i=1; i<=11; i++) {
                al.add(i + " (" + numberToKey(transposeNumber(keyToNumber(key), "-1", i)) + ")");
            }
        } else {
            for (int i=1; i<=11; i++) {
                al.add(String.valueOf(i));
            }
        }
        return al;
    }

    public void checkChordFormat(Song thisSong) {
        // If we have specified 'use preferred' set it to that
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("chordFormatUsePreferred", false)) {
            newChordFormat = mainActivityInterface.getPreferences().getMyPreferenceInt("chordFormat",1);
            oldChordFormat = newChordFormat;
            thisSong.setDetectedChordFormat(newChordFormat);
            thisSong.setDesiredChordFormat(newChordFormat);

        } else {
            // We need to detect the chord formatting
            int contains_es_is_count = 0;
            int contains_H_count = 0;
            int contains_do_count = 0;
            int contains_nash_count = 0;
            int contains_nashnumeral_count = 0;
            boolean contains_Bb = false;


            // Process to get chords separated by spaces
            String lyrics = thisSong.getLyrics()
                    // Protect new lines
                    .replace("\n", "¬")
                    // Remove text in brackets on chord lines as they may contain text that causes problems e.g. (Repeat last x) contains La.
                    // Android Studio gets confused over escapes here - suggesting removing escapes that break the regex!  Kept lots of escapes to be sure they work!
                    .replaceAll("\\(.*?\\)", "")
                    .replaceAll("\\{.*?\\}", "")
                    .replaceAll("\\[.*?\\]", "")
                    // Replace chord delimters/modifiers
                    .replace("|", " ")
                    .replace(":", " ")
                    .replace("/", " ");

            // Quickly check for Bb chord before stripping other stuff out
            String[] BbLines = lyrics.split("¬");
            for (String l:BbLines) {
                if (l.startsWith(".") && l.contains("Bb")) {
                    contains_Bb = true;
                    break;
                }
            }

            // Now finish trimming the lines
            lyrics = lyrics
                    // Why ' ~'?  We split chords like 'Am7' into 'A ~7' - the ~ stops the number being detected as nashville
                    .replace("m", " ~") // Also handles majors
                    .replace("sus", " ~") // Removed as conflicts with format 3 tests for chord ending's'
                    .replace("b", " ~")
                    .replace("#", " ~")
                    // Remove multiple whitespace and trim
                    .replaceAll("\\s{2,}", " ").trim();

            // Check the chord format of the the song.  Go through the chord lines and look for clues
            for (String line : lyrics.split("¬")) {
                if (line.startsWith(".")) {
                    //  Split into individual chords
                    String[] chordsInLine = line.substring(1).split(" ");
                    String chordInLineLC;

                    // Now go through each chord and add to the matching format
                    // Case is needed as lowercase chords denotes minor chords for format 3
                    // Not required for format 5 (Nashville numbers)
                    for (String chordInLine : chordsInLine) {
                        chordInLineLC = chordInLine.toLowerCase(Locale.ROOT);
                        if (Arrays.asList(format6Identifiers).contains(chordInLineLC)) {
                            contains_nashnumeral_count++;
                        } else if (Arrays.asList(format5Identifiers).contains(chordInLine)) {
                            contains_nash_count++;
                        } else if (Arrays.asList(format4Identifiers).contains(chordInLineLC)) {
                            contains_do_count++;
                            // chords ending s (es, is and s ) are identifiers for format 3 as are lowercase minor chords
                        } else if (chordInLineLC.length() > 1 && "s".equals(chordInLineLC.substring(chordInLineLC.length() - 1)) ||
                                (Arrays.asList(format3Identifiers).contains(chordInLine))) {
                            contains_es_is_count++;
                        } else if (Arrays.asList(format2Identifiers).contains(chordInLineLC)) {
                            contains_H_count++;
                        }
                    }
                }
            }

            // Here we allow low levels of mis-identification
            boolean contains_es_is = (contains_es_is_count > 2);
            boolean contains_H = (contains_H_count > 2);
            boolean contains_do = (contains_do_count > 4);
            boolean contains_nash = (contains_nash_count > 4);
            boolean contains_nashnumeral = (contains_nashnumeral_count > 4);

            // Set the chord style detected - Ok so the user chord format may not quite match the song - it might though!
            int formatNum;

            if (contains_do) {
                formatNum = 4;
            } else if (contains_H && !contains_es_is) {
                formatNum = 2;
            } else if (contains_Bb && !contains_H && contains_es_is) {
                formatNum = 7;
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
        }
    }

    public String convertToPreferredChord(String chord) {
        // Changes Ab/G# to user's preference.  This sends out to another function which checks minor and major
        // By default all major chords are set to true
        chord = swapToPrefChords("Ab","G#", chord, false);
        chord = swapToPrefChords("Bb","A#", chord, true);
        chord = swapToPrefChords("Db","C#", chord, false);
        chord = swapToPrefChords("Eb","D#", chord, true);
        chord = swapToPrefChords("Gb","F#", chord, false);
        return chord;
    }

    private String swapToPrefChords(String flatOption, String sharpOption, String chord,
                                    boolean defaultFlatMinor) {

        if (chord.startsWith(flatOption+"m") || chord.startsWith(sharpOption+"m")) {
            // Check the minor chord first
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_"+flatOption+"m",defaultFlatMinor)) {
                chord = chord.replace(sharpOption+"m", flatOption+"m");
            } else {
                chord = chord.replace(flatOption+"m",sharpOption+"m");
            }
        } else if (chord.startsWith(flatOption) || chord.startsWith(sharpOption)) {
            // Now check the major chord
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("prefKey_"+flatOption, true)) {
                chord = chord.replace(sharpOption, flatOption);
            } else {
                chord = chord.replace(flatOption,sharpOption);
            }
        }
        return chord;
    }

    // For dealing with songs loaded from sets (checking key with current song)
    public int getTransposeTimes(String from, String to) {
        int startPos = -1;
        int endPos = -1;
        // Get rid of minors for transpose times - not required
        from = from.replace("m","");
        to = to.replace("m","");
        // Go through the keys until the start position is found
        // Then keep going and find the end position
        for (int x=0; x<keyText.length; x++) {
            if (startPos<0 && keyText[x].equals(from)) {
                startPos = keyNum[x];
            }
            if (startPos>-1 && endPos<0 && keyText[x].equals(to)) {
                endPos = keyNum[x];
            }
        }
        // Return the transpose times (for +1 direction).  If bigger than 6, the app actually does -1
        if (startPos>-1 && endPos>-1 && endPos>startPos) {
            return endPos - startPos;
        } else {
            // Some issue - no transpose!
            return 0;
        }
    }

    // Commonly used array for chord format names and display values
    public ArrayList<String> getChordFormatNames() {
        if (chordFormatNames==null) {
            chordFormatNames = new ArrayList<>();
            chordFormatNames.add(c.getString(R.string.chordformat_1_name));
            chordFormatNames.add(c.getString(R.string.chordformat_2_name));
            chordFormatNames.add(c.getString(R.string.chordformat_3_name));
            chordFormatNames.add(c.getString(R.string.chordformat_4_name));
            chordFormatNames.add(c.getString(R.string.chordformat_5_name));
            chordFormatNames.add(c.getString(R.string.chordformat_6_name));
            chordFormatNames.add(c.getString(R.string.chordformat_7_name));
        }
        return chordFormatNames;
    }

    public ArrayList<String> getChordFormatAppearances() {
        if (chordFormatAppearances==null) {
            chordFormatAppearances = new ArrayList<>();
            chordFormatAppearances.add(c.getString(R.string.chordformat_1));
            chordFormatAppearances.add(c.getString(R.string.chordformat_2));
            chordFormatAppearances.add(c.getString(R.string.chordformat_3));
            chordFormatAppearances.add(c.getString(R.string.chordformat_4));
            chordFormatAppearances.add(c.getString(R.string.chordformat_5));
            chordFormatAppearances.add(c.getString(R.string.chordformat_6));
            chordFormatAppearances.add(c.getString(R.string.chordformat_7));
        }
        return chordFormatAppearances;
    }

    public void checkOriginalKeySet(Song thisSong) {
        // This looks to see if we have an original key set.
        // If not, and we have a key, set it to that
        if (!originalKeyIsSet(thisSong) &&
            thisSong.getKey()!=null && !thisSong.getKey().isEmpty()) {
            // Copy the current key as the original
            thisSong.setKeyOriginal(thisSong.getKey());
        }
    }

    public boolean originalKeyIsSet(Song thisSong) {
        return thisSong!=null && thisSong.getKeyOriginal()!=null && !thisSong.getKeyOriginal().isEmpty();
    }

    public void setConvertToFlats(boolean convertToFlats) {
        this.convertToFlats = convertToFlats;
    }

    public void setConvertToSharps(boolean convertToSharps) {
        this.convertToSharps = convertToSharps;
    }
}
