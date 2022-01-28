package com.garethevans.church.opensongtablet;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

class Transpose {
    //  A  A#/Bb  B/(Cb) C/(B#) C#/Db    D    D#/Eb  E/(Fb) (E#)/F   F#/Gb   G     G#/Ab
    //  A    B      H      C
    //  1    2      3      4      5      6      7      8      9      W(10)  X(11)  Y(12)
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
    //
    // Replaces occur in order, with this used to perform logic.
    // Example protect logic: Replace "maj7" to "¬aj7" to stop the m being treated as a minor, is followed by replace "¬" with "m" to restore it.
    // Example variation handling logic: replace 'TI' with "SI, "Ti" with "SI" and "ti" with "SI" is followed by replace of "SI' with chord number "«├3┤"
    // Three variants "TI", "Ti" and "ti" are therefore handled the same as SI.

    // Chord to number: 'majors' and sus interfere so are protected
    private final String [] fromchords1 =   {"maj7", "ma7", "maj9", "ma9",
                                                         "A#",     "B#",     "C#",     "D#",     "E#",     "F#",     "G#",
                                                         "Ab",     "Bb",     "Cb",     "Db",     "Eb",     "Fb",     "Gb",
                                                         "A",      "B",      "C",      "D",      "E",      "F",      "G",     "¬"};
    private final String [] tochordsnums1 = {"¬aj7", "¬a7", "¬aj9", "¬a9",
                                                       "«├2┤",   "«├4┤",   "«├5┤",   "«├7┤",   "«├9┤",   "«├W┤",   "«├Y┤",
                                                       "«├Y┤",   "«├2┤",   "«├3┤",   "«├5┤",   "«├7┤",   "«├8┤",   "«├W┤",
                                                      "««├1┤",  "««├3┤",  "««├4┤",  "««├6┤",  "««├8┤",  "««├9┤",  "««├X┤",    "m"};
    private final String[] fromchords2 =    {"maj7", "ma7", "maj9", "ma9",
                                                         "A#",     "H#",     "C#",     "D#",     "E#",     "F#",     "G#",
                                                         "Ab",     "B",      "Cb",     "Db",     "Eb",     "Fb",     "Gb",
                                                         "A",      "H",      "C",      "D",      "E",      "F",      "G",     "¬"};
    private final String[] tochordsnums2 =  {"¬aj7", "¬a7", "¬aj9", "¬a9",
                                                       "«├2┤",   "«├4┤",   "«├5┤",   "«├7┤",   "«├9┤",   "«├W┤",   "«├Y┤",
                                                       "«├Y┤",  "««├2┤",   "«├3┤",   "«├5┤",   "«├7┤",   "«├8┤",   "«├W┤",
                                                      "««├1┤",  "««├3┤",  "««├4┤",  "««├6┤",  "««├8┤",  "««├9┤",  "««├X┤",    "m"};
    private final String[] fromchords3 =    {"maj7", "ma7", "maj9", "ma9", "sus",
                                                    " (ais",    " (his",    " (cis",    " (dis",    " (eis",    " (fis",    " (gis",
                                                    " (as",     " (b",      " (ces",    " (des",    " (es",     " (fes",    " (ges",
                                                    " (a",      " (h",      " (c",      " (d",      " (e",      " (f",      " (g",
                                                    " ais",     " his",     " cis",     " dis",     " eis",     " fis",     " gis",
                                                    " as",      " b",       " ces",     " des",     " es",      " fes",     " ges",
                                                    " a",       " h",       " c",       " d",       " e",       " f",       " g",
                                                    "Ais",      "His",      "Cis",      "Dis",      "Eis",      "Fis",      "Gis",
                                                    "As",       "B",        "Ces",      "Des",      "Es",       "Fes",      "Ges",
                                                    "A",        "H",        "C",        "D",        "E",        "F",        "G",    "¬us", "¬"};
    private final String[] tochordsnums3 =  {"¬aj7", "¬a7", "¬aj9", "¬a9", "¬us",
                                                " («├2┤m",  " («├4┤m",  " («├5┤m",  " («├7┤m",  " («├9┤m",  " («├W┤m",  " («├Y┤m",
                                               " (««├Y┤m"," («««├2┤m",  " («├3┤m",  " («├5┤m", " (««├7┤m",  " («├8┤m",  " («├W┤m",
                                              " («««├1┤m"," («««├3┤m"," («««├4┤m"," («««├6┤m"," («««├8┤m"," («««├9┤m"," («««├X┤m",
                                                 " «├2┤m",   " «├4┤m",   " «├5┤m",   " «├7┤m",   " «├9┤m",   " «├W┤m",   " «├Y┤m",
                                                " ««├Y┤m", " «««├2┤m",   " «├3┤m",   " «├5┤m",  " ««├7┤m",   " «├8┤m",   " «├W┤m",
                                               " «««├1┤m", " «««├3┤m", " «««├4┤m", " «««├6┤m", " «««├8┤m", " «««├9┤m", " «««├X┤m",
                                                   "├2┤",      "├4┤",      "├5┤",      "├7┤",      "├9┤",      "├W┤",      "├Y┤",
                                                  "«├Y┤",    "««├2┤",      "├3┤",      "├5┤",     "«├7┤",      "├8┤",      "├W┤",
                                                 "««├1┤",    "««├3┤",    "««├4┤",    "««├6┤",    "««├8┤",    "««├9┤",    "««├X┤",   "sus", "m"};
    // Solfege variants
    private final String[] fromchords4 =    {"maj7", "ma7", "maj9", "ma9",
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
    private final String[] tochordsnums4 =  {"¬aj7", "¬a7", "¬aj9", "¬a9",
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
    private String[] fromnash;
    private String[] tochordnumsnash;

    // Number to chord:
    private final String[] fromchordsnum  = "├2┤ ├5┤ ├7┤ ├W┤ ├Y┤ ├1┤ ├3┤ ├4┤ ├6┤ ├8┤ ├9┤ ├X┤".split(" ");
    private final String[] tosharpchords1 = "»A# »C# »D# »F# »G# »»A »»B »»C »»D »»E »»F »»G".split(" ");
    private final String[] toflatchords1 =  "»Bb »Db »Eb »Gb »Ab »»A »»B »»C »»D »»E »»F »»G".split(" ");
    private final String[] tosharpchords2 = "»»B »C# »D# »F# »G# »»A »»H »»C »»D »»E »»F »»G".split(" ");
    private final String[] toflatchords2 =  "»»B »Db »Eb »Gb »Ab »»A »»H »»C »»D »»E »»F »»G".split(" ");
    // IV - Solfege out format is 'SongSelect fixed DO' - Capitals = easy to read, no accents = easy to type, DO not UT and SI not TI = most common across solfege variants
    private final String[] tosharpchords4 = "LA# DO# RE# FA# «SOL# »LA »SI »DO »RE »MI »FA SOL".split(" ");
    private final String[] toflatchords4 =  "SIb REb MIb «SOLb LAb »LA »SI »DO »RE »MI »FA SOL".split(" ");
    //  A trick! Minors arrive ending ┤m, the m is moved into the number to give numbers for minors. '┤ma' is treated as the start of major and is protected.
    private final String[] fromchordsnumm = "┤ma ┤m ├2m┤ ├5m┤ ├7m┤ ├Wm┤ ├Ym┤ ├1m┤ ├3m┤ ├4m┤ ├6m┤ ├8m┤ ├9m┤ ├Xm┤ ├2┤ ├5┤ ├7┤ ├W┤ ├Y┤ ├1┤ ├3┤ ├4┤ ├6┤ ├8┤ ├9┤ ├X┤ ¬".split(" ");
    private final String[] tosharpchords3 = "┤¬a m┤ »»»b »cis »dis »fis »gis »»»a »»»h »»»c »»»d »»»e »»»f »»»g »»B Cis Dis Fis Gis »»A »»H »»C »»D »»E »»F »»G m".split(" ");
    private final String[] toflatchords3 =  "┤¬a m┤ »»»b »des »»es »ges »»as »»»a »»»h »»»c »»»d »»»e »»»f »»»g »»B Des »Es Ges »As »»A »»H »»C »»D »»E »»F »»G m".split(" ");
    private String[] fromchordnumsnash;
    private String[] fromchordnumsnashtype;
    private String[] tonash;

    // Used in the generation of the Nashville conversion arrays
    private final String[] bitssharp      = "A A# B C C# D D# E F F# G G# A A# B C C# D D# E F F# G G#".split(" ");
    private final String[] bitsflat       = "A Bb B C Db D Eb E F Gb G Ab A Bb B C Db D Eb E F Gb G Ab".split(" ");
    private final String [] bitssharpnums = "♮├1┤ #├2┤ ♮├3┤ ♮├4┤ #├5┤ ♮├6┤ #├7┤ ♮├8┤ ♮├9┤ #├W┤ ♮├X┤ #├Y┤ ♮├1┤ #├2┤ ♮├3┤ ♮├4┤ #├5┤ ♮├6┤ #├7┤ ♮├8┤ ♮├9┤ #├W┤ ♮├X┤ #├Y┤".split(" ");
    private final String [] bitsflatnums  = "♮├1┤ b├2┤ ♮├3┤ ♮├4┤ b├5┤ ♮├6┤ b├7┤ ♮├8┤ ♮├9┤ b├W┤ ♮├X┤ b├Y┤ ♮├1┤ b├2┤ ♮├3┤ ♮├4┤ b├5┤ ♮├6┤ b├7┤ ♮├8┤ ♮├9┤ b├W┤ ♮├X┤ b├Y┤".split(" ");
    private final String [] bitsnums      = "├1┤ ├2┤ ├3┤ ├4┤ ├5┤ ├6┤ ├7┤ ├8┤ ├9┤ ├W┤ ├X┤ ├Y┤ ├1┤ ├2┤ ├3┤ ├4┤ ├5┤ ├6┤ ├7┤ ├8┤ ├9┤ ├W┤ ├X┤ ├Y┤".split(" ");
    private int major;
    private int root;

    private static final String[] format2Identifiers = new String[]{"h"};
    private static final String[] format3Identifiers = new String[]{"a","c","d","e,","f","g"}; // Format 3 has lowecase minors. 'b' is 'flat', "h" is dealt with separatly so both not tested
    private static final String[] format4Identifiers = new String[]{"do","dó","re","ré","mi","fa","fá","sol","la","lá", "si","ti"};
    private static final String[] format5Identifiers = new String[]{"1","2","3","4","5","6","7"};
    private static final String[] format6Identifiers = new String[]{"i","ii","ii","iii","iii","iv","iv","v","vi","vii"};

    void doTranspose(Context c, Preferences preferences, boolean forcesharps, boolean forceflats) {
        // Initialise the variables that might be looked up later
        fromchordnumsnash = null;
        fromchordnumsnashtype = null;
        tochordnumsnash = null;
        tonash = null;
        fromnash = null;

        try {
            String originalkey = StaticVariables.mKey;
            // Update the key number
            if (originalkey != null && !originalkey.equals("")) {
                StaticVariables.mKey = numberToKey(c, preferences, transposeNumber(keyToNumber(originalkey), StaticVariables.transposeDirection, StaticVariables.transposeTimes));
            }
            // Transpose and write the lyrics
            StaticVariables.transposedLyrics = transposeString(c, preferences, StaticVariables.mLyrics, forcesharps, forceflats);
            writeImprovedXML(c, preferences);
        }
        catch (Exception ignored) {}
    }

    // This is the chord transpose engine
    String transposeString(Context c, Preferences preferences, String string, boolean forcesharps, boolean forceflats) {
        // StaticVariables: detectedChordFormat, newChordFormat, transposeDirection, transposeTimes must be set before call
        try {
            StringBuilder sb = new StringBuilder();

            // If the call does not force the use of sharps or flats then determine from the key
            if (!forcesharps && !forceflats) forceflats = keyUsesFlats(c, preferences, StaticVariables.mKey);

            // IV - Add a trailing ¶ to force a split behaviour that copes with a trailing new line!
            for (String line : (string+"¶").split("\n")) {
                // IV - Use leading \n as we can be certain it is safe to remove later
                sb.append("\n");
                if (line.startsWith(".")) {
                    line = line.replaceFirst("."," ");
                    switch (StaticVariables.detectedChordFormat) {
                        default:
                        case 1:
                            for (int z = 0; z < fromchords1.length; z++) line = line.replace(fromchords1[z], tochordsnums1[z]);
                            break;
                        case 2:
                            for (int z = 0; z < fromchords2.length; z++) line = line.replace(fromchords2[z], tochordsnums2[z]);
                            break;
                        case 3:
                            for (int z = 0; z < fromchords3.length; z++) line = line.replace(fromchords3[z], tochordsnums3[z]);
                            break;
                        case 4:
                            for (int z = 0; z < fromchords4.length; z++) line = line.replace(fromchords4[z], tochordsnums4[z]);
                            break;
                        case 5:
                        case 6:
                            if (fromnash == null) prepfromnashtonums(StaticVariables.detectedChordFormat == 6);
                            for (int z = 0; z < fromnash.length; z++) line = line.replace(fromnash[z], tochordnumsnash[z]);
                    }

                    // If the old format has transposable chords - transpose
                    if (StaticVariables.newChordFormat < 5) line = transposeNumber(line, StaticVariables.transposeDirection, StaticVariables.transposeTimes);

                    switch (StaticVariables.newChordFormat) {
                        default:
                        case 1:
                            if (forceflats) for (int z = 0; z < fromchordsnum.length; z++) line = line.replace(fromchordsnum[z], toflatchords1[z]);
                            else            for (int z = 0; z < fromchordsnum.length; z++) line = line.replace(fromchordsnum[z], tosharpchords1[z]);
                            break;
                        case 2:
                            if (forceflats) for (int z = 0; z < fromchordsnum.length; z++) line = line.replace(fromchordsnum[z], toflatchords2[z]);
                            else            for (int z = 0; z < fromchordsnum.length; z++) line = line.replace(fromchordsnum[z], tosharpchords2[z]);
                            break;
                        case 3:
                            // IV - Uses the 'm' array as the target has explicit minors
                            if (forceflats) for (int z = 0; z < fromchordsnumm.length; z++) line = line.replace(fromchordsnumm[z], toflatchords3[z]);
                            else            for (int z = 0; z < fromchordsnumm.length; z++) line = line.replace(fromchordsnumm[z], tosharpchords3[z]);
                            break;
                        case 4:
                            if (forceflats) for (int z = 0; z < fromchordsnum.length; z++) line = line.replace(fromchordsnum[z], toflatchords4[z]);
                            else            for (int z = 0; z < fromchordsnum.length; z++) line = line.replace(fromchordsnum[z], tosharpchords4[z]);
                            break;
                        case 5:
                        case 6:
                            if (tonash == null) prepfromnumstonash(StaticVariables.newChordFormat == 6);
                            if (forceflats) for (int z = 0; z < fromchordnumsnash.length; z++) { if (!fromchordnumsnashtype[z].equals("#")) line = line.replace(fromchordnumsnash[z], tonash[z]); }
                            else            for (int z = 0; z < fromchordnumsnash.length; z++) { if (!fromchordnumsnashtype[z].equals("b")) line = line.replace(fromchordnumsnash[z], tonash[z]); }
                    }

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
                    line = line.replaceFirst(" ",".");
                }
                // Add it back up
                sb.append(line);
            }
            // Return the string removing the added leading \n and trailing ¶
            return sb.substring(1).replace("¶", "");

        } catch(Exception e) { return string; }
    }

    String transposeNumber(String string, String direction, int numtimes) {
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

    private void prepfromnashtonums(boolean numeral) {
        // StageMode sets StaticVariables.tochordnumsnash to null in loadSong()
        if (StaticVariables.tochordnumsnash == null) {

            getNashvilleroot();

            if (numeral) {
                // Includes protection for major chords
                fromnash = ("maj7_ma7_maj9_ma9_" +
                            "#VII_#VI_#IV_#V_#III_#II_#I_bVII_bVI_bIV_bV_bIII_bII_bI_VII_VI_IV_V_III_II_I_#vii_#vi_#iv_#v_#iii_#ii_#i_bvii_bvi_biv_bv_biii_bii_bi_vii_vi_iv_v_iii_ii_i_¬").split("_");
                tochordnumsnash = ("¬aj7_¬a7_¬aj9_¬a9_" +
                        bitsnums[root] + "_" +
                        bitsnums[root + 9 + major] + "_" +
                        bitsnums[root + 6] + "_" +
                        bitsnums[root + 8] + "_" +
                        bitsnums[root + 4 + major] + "_" +
                        bitsnums[root + 3] + "_" +
                        bitsnums[root + 1] + "_" +
                        bitsnums[root + 9 + major] + "_" +
                        bitsnums[root + 7 + major] + "_" +
                        bitsnums[root + 4] + "_" +
                        bitsnums[root + 6] + "_" +
                        bitsnums[root + 2 + major] + "_" +
                        bitsnums[root + 1] + "_" +
                        bitsnums[root + 11] + "_" +
                        bitsnums[root + 10 + major] + "_" +
                        bitsnums[root + 8 + major] + "_" +
                        bitsnums[root + 5] + "_" +
                        bitsnums[root + 7] + "_" +
                        bitsnums[root + 3 + major] + "_" +
                        bitsnums[root + 2] + "_" +
                        bitsnums[root] + "_" +
                        bitsnums[root + 11 + major] + "m" + "_" +
                        bitsnums[root + 9 + major] + "m" + "_" +
                        bitsnums[root + 6] + "m" + "_" +
                        bitsnums[root + 8] + "m" + "_" +
                        bitsnums[root + 4 + major] + "m" + "_" +
                        bitsnums[root + 3] + "m" + "_" +
                        bitsnums[root + 1] + "m" + "_" +
                        bitsnums[root + 9 + major] + "m" + "_" +
                        bitsnums[root + 7 + major] + "m" + "_" +
                        bitsnums[root + 4] + "m" + "_" +
                        bitsnums[root + 6] + "m" + "_" +
                        bitsnums[root + 2 + major] + "m" + "_" +
                        bitsnums[root + 1] + "m" + "_" +
                        bitsnums[root + 11] + "m" + "_" +
                        bitsnums[root + 10 + major] + "mo".charAt(major) + "_" +
                        bitsnums[root + 8 + major] + "m" + "_" +
                        bitsnums[root + 5] + "m" + "_" +
                        bitsnums[root + 7] + "m" + "_" +
                        bitsnums[root + 3 + major] + "m" + "_" +
                        bitsnums[root + 2] + "om".charAt(major) + "_" +
                        bitsnums[root] + "m" + "_" +
                        "m").split("_");
            } else {
                String fromnashbase = "¬#7_¬#6_¬#5_¬#4_¬#3_¬#2_¬#1_¬b7_¬b6_¬b5_¬b4_¬b3_¬b2_¬b1_¬7_¬6_¬5_¬4_¬3_¬2_¬1_";
                String tochordnumsbase =
                        "¬" + bitsnums[root] + "_" +
                                "¬" + bitsnums[root + 9 + major] + "_" +
                                "¬" + bitsnums[root + 8] + "_" +
                                "¬" + bitsnums[root + 6] + "_" +
                                "¬" + bitsnums[root + 4 + major] + "_" +
                                "¬" + bitsnums[root + 3] + "_" +
                                "¬" + bitsnums[root + 1] + "_" +
                                "¬" + bitsnums[root + 9 + major] + "_" +
                                "¬" + bitsnums[root + 7 + major] + "_" +
                                "¬" + bitsnums[root + 6] + "_" +
                                "¬" + bitsnums[root + 4] + "_" +
                                "¬" + bitsnums[root + 2 + major] + "_" +
                                "¬" + bitsnums[root + 1] + "_" +
                                "¬" + bitsnums[root + 11] + "_" +
                                "¬" + bitsnums[root + 10 + major] + "_" +
                                "¬" + bitsnums[root + 8 + major] + "_" +
                                "¬" + bitsnums[root + 7] + "_" +
                                "¬" + bitsnums[root + 5] + "_" +
                                "¬" + bitsnums[root + 3 + major] + "_" +
                                "¬" + bitsnums[root + 2] + "_" +
                                "¬" + bitsnums[root] + "_";

                // Includes protection for major chords and support for different 'chord follows' sequences including '<space>('
                fromnash = ("maj7_ma7_maj9_ma9_" +
                        fromnashbase.replace("¬"," ") +
                        fromnashbase.replace("¬","/") +
                        fromnashbase.replace("¬"," (") +
                        "¬").split("_");

                tochordnumsnash = ("¬aj7_¬a7_¬aj9_¬a9_" +
                        tochordnumsbase.replace("¬"," ") +
                        tochordnumsbase.replace("¬","/") +
                        tochordnumsbase.replace("¬"," (") +
                        "m").split("_");
            }

            int diff;

            // For each item space adjustment markers are added
            for (int x = 0; x < tochordnumsnash.length; x++) {

                diff = (fromnash[x].length() - tochordnumsnash[x].length());

                if (diff < 0) tochordnumsnash[x] = "««««««««".substring(0, -diff) + tochordnumsnash[x];
                else tochordnumsnash[x] = "»»»»»»»»".substring(0, diff) + tochordnumsnash[x];
            }
            // Store for re-use
            StaticVariables.tochordnumsnash = tochordnumsnash.clone();
            StaticVariables.fromnash = fromnash.clone();

        } else {
            // Re-use
            tochordnumsnash = StaticVariables.tochordnumsnash.clone();
            fromnash = StaticVariables.fromnash.clone();
        }
    }

    private void prepfromnumstonash(boolean numeral) {
        // StageMode sets StaticVariables.fromchordnumsnash to null in loadSong()
        // Display (not convert) transposes by section and we avoid repetition of array generation by store and re-use.
        if (StaticVariables.fromchordnumsnash == null) {
            String to;
            String topostadditions = "";
            String frompostadditions = "";
            String frompreadditions;
            String topreadditions;

            getNashvilleroot();

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

            fromchordnumsnash = (frompreadditions + "_" +
                    bitssharpnums[root] + "_" +
                    bitssharpnums[root + 2] + "_" +
                    bitssharpnums[root + 3 + major] + "_" +
                    bitssharpnums[root + 5] + "_" +
                    bitssharpnums[root + 7] + "_" +
                    bitssharpnums[root + 8 + major] + "_" +
                    bitssharpnums[root + 10 + major] + "_" +
                    bitssharpnums[root + 1] + "_" +
                    bitssharpnums[root + 3] + "_" +
                    bitssharpnums[root + 6] + "_" +
                    bitssharpnums[root + 8] + "_" +
                    bitssharpnums[root + 9 + major] + "_" +
                    bitsflatnums[root] + "_" +
                    bitsflatnums[root + 2] + "_" +
                    bitsflatnums[root + 3 + major] + "_" +
                    bitsflatnums[root + 5] + "_" +
                    bitsflatnums[root + 7] + "_" +
                    bitsflatnums[root + 8 + major] + "_" +
                    bitsflatnums[root + 10 + major] + "_" +
                    bitsflatnums[root + 1] + "_" +
                    bitsflatnums[root + 2 + major] + "_" +
                    bitsflatnums[root + 6] + "_" +
                    bitsflatnums[root + 7 + major] + "_" +
                    bitsflatnums[root + 9 + major] + "_" + frompostadditions).split("_");

            tonash = (topreadditions + "_" + to + "_" + topostadditions).split("_");

            int diff;
            fromchordnumsnashtype = fromchordnumsnash.clone();

            for (int x = 0; x < fromchordnumsnashtype.length; x++) {
                // Extract the 'type'
                fromchordnumsnashtype[x] = fromchordnumsnash[x].substring(0, 1);
                fromchordnumsnash[x] = fromchordnumsnash[x].substring(1);

                // Add space adjustment markers
                diff = (fromchordnumsnash[x].length() - tonash[x].length());

                if (diff < 0) tonash[x] = "««««««««".substring(0, -diff) + tonash[x];
                else          tonash[x] = "»»»»»»»»".substring(0,  diff) + tonash[x];
            }
            // Store for re-use
            StaticVariables.fromchordnumsnash = fromchordnumsnash.clone();
            StaticVariables.fromchordnumsnashtype = fromchordnumsnashtype.clone();
            StaticVariables.tonash = tonash.clone();

        } else {
            // Re-use
            fromchordnumsnash = StaticVariables.fromchordnumsnash.clone();
            fromchordnumsnashtype = StaticVariables.fromchordnumsnashtype.clone();
            tonash = StaticVariables.tonash.clone();
        }
    }

    private void getNashvilleroot() {
        String originalkey = StaticVariables.mKey;

        if (originalkey.endsWith("m")) major = 0; else major = 1;
        // Root lookup is against chord format 1 as key is stored in format 1
        root = 0;
        for (int i = 0; i < bitssharp.length; i++) {
            if ((bitssharp[i].equals(originalkey) || (bitssharp[i] + "m").equals(originalkey) ||
                  bitsflat[i].equals(originalkey) || (bitsflat[i]  + "m").equals(originalkey))) {
                root = i;
                break;
            }
        }
   }

    String keyToNumber(String key) {
        // Swap the key with the chord number
        for (int z = 0; z < fromchords1.length; z++) key = key.replace(fromchords1[z], tochordsnums1[z]);
        return key;
    }

    String numberToKey(Context c, Preferences preferences, String key) {
        // We need to decide which key the user likes the best for each one
        // Convert the key number into either a sharp or natural first
        // Then we swap sharps to flats if the user prefers these

        for (int z = 0; z < fromchordsnum.length; z++) key = key.replace(fromchordsnum[z], tosharpchords1[z]);
        // Remove chord space adjustment indicators
        key = key.replace("»","").replace("«","");

        if      (key.equals("G#") && preferences.getMyPreferenceBoolean(c,"prefKeyAb",true))    key = "Ab";
        else if (key.equals("G#m") && preferences.getMyPreferenceBoolean(c,"prefKeyAbm",false)) key = "Abm";
        else if (key.equals("A#")  && preferences.getMyPreferenceBoolean(c,"prefKeyBb",true))   key = "Bb";
        else if (key.equals("A#m") && preferences.getMyPreferenceBoolean(c,"prefKeyBbm",true))  key = "Bbm";
        else if (key.equals("C#")  && preferences.getMyPreferenceBoolean(c,"prefKeyDb",false))  key = "Db";
        else if (key.equals("C#m") && preferences.getMyPreferenceBoolean(c,"prefKeyDbm",true))  key = "Dbm";
        else if (key.equals("D#")  && preferences.getMyPreferenceBoolean(c,"prefKeyEb",true))   key = "Eb";
        else if (key.equals("D#m") && preferences.getMyPreferenceBoolean(c,"prefKeyEbm",true))  key = "Ebm";
        else if (key.equals("F#")  && preferences.getMyPreferenceBoolean(c,"prefKeyGb",false))  key = "Gb";
        else if (key.equals("F#m") && preferences.getMyPreferenceBoolean(c,"prefKeyGbm",false)) key = "Gbm";

        return key;
    }

    private boolean keyUsesFlats(Context c, Preferences preferences, String testkey) {
        return  (testkey.equals("Ab")  && preferences.getMyPreferenceBoolean(c,"prefKeyAb",true)) ||
                (testkey.equals("Bb")  && preferences.getMyPreferenceBoolean(c,"prefKeyBb",true)) ||
                (testkey.equals("Db")  && preferences.getMyPreferenceBoolean(c,"prefKeyDb",false)) ||
                (testkey.equals("Eb")  && preferences.getMyPreferenceBoolean(c,"prefKeyEb",true)) ||
                (testkey.equals("Gb")  && preferences.getMyPreferenceBoolean(c,"prefKeyGb",false)) ||
                (testkey.equals("Bbm") && preferences.getMyPreferenceBoolean(c,"prefKeyBbm",true)) ||
                (testkey.equals("Dbm") && preferences.getMyPreferenceBoolean(c,"prefKeyDbm",false)) ||
                (testkey.equals("Ebm") && preferences.getMyPreferenceBoolean(c,"prefKeyEbm",true)) ||
                (testkey.equals("Gbm") && preferences.getMyPreferenceBoolean(c,"prefKeyGbm",false)) ||
                testkey.equals("C") ||
                testkey.equals("F") ||
                testkey.equals("Dm") ||
                testkey.equals("Gm") ||
                testkey.equals("Cm");
    }

    String capoTranspose(Context c, Preferences preferences, String string) {
        // StageMode sets FullscreenActivity.capokey to "" in loadSong(), first call after sets for new song
        if (FullscreenActivity.capokey.equals("")) {
            // Get the capokey
            if (StaticVariables.mKey != null) capoKeyTranspose(c, preferences);
            // Determine if we need to force flats for the capo key
            StaticVariables.capoforceflats = keyUsesFlats(c, preferences, FullscreenActivity.capokey);
        }

        // If not showing Capo chords then 'tranpose' Capo 0 to display preferred chords
        if (!preferences.getMyPreferenceBoolean(c, "displayCapoChords", true)) {
            StaticVariables.transposeTimes = 0;
        } else {
            StaticVariables.transposeTimes = Integer.parseInt("0" + StaticVariables.mCapo);
        }

        // Transpose using force, add "." for transpose and remove on return (mCapo 0 is used when displaying a song with no capo in preferred chord format)
        StaticVariables.transposeDirection = "-1";
        return transposeString(c, preferences,"." + string, !StaticVariables.capoforceflats, StaticVariables.capoforceflats).substring(1);
    }

    void capoKeyTranspose(Context c, Preferences preferences) {
        FullscreenActivity.capokey = numberToKey(c, preferences, transposeNumber(keyToNumber(StaticVariables.mKey), "-1", Integer.parseInt("0" + StaticVariables.mCapo)));
    }

    ArrayList<String> quickCapoKey(Context c, Preferences preferences, String key) {
        // This is used to give the user a list starting with blank of either simple fret number or fret number with new capo key
        ArrayList<String> al = new ArrayList<>(Collections.singletonList(""));

        if (key!=null && !key.equals("") && !key.isEmpty()) {
            for (int i=1; i<=11; i++) al.add(i + " (" + numberToKey(c, preferences, transposeNumber(keyToNumber(key), "-1", i)) + ")");
        } else {
            for (int i=1; i<=11; i++) al.add(i + "");
        }
        return al;
    }

    public static void checkChordFormat() {
        // We need to detect the chord formatting
        int contains_es_is_count = 0;
        int contains_H_count = 0;
        int contains_do_count = 0;
        int contains_nash_count = 0;
        int contains_nashnumeral_count = 0;

        // Process to get chords separated by spaces
        String mLyrics = StaticVariables.mLyrics
                // Protect new lines
                .replace("\n", "¬")
                // Remove text in brackets on chord lines as they may contain text that causes problems e.g. (Repeat last x) contains La.
                // Android Studio gets confused over escapes here - suggesting removing escapes that break the regex!  Kept lots of escapes to be sure they work!
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("\\{.*?\\}", "")
                .replaceAll("\\[.*?\\]", "")
                // Replace chord delimters/modifiers
                .replace("|"," ")
                .replace(":"," ")
                .replace ("/"," ")
                // Why ' ~'?  We split chords like 'Am7' into 'A ~7' - the ~ stops thr number being detected as nashville
                .replace ("m", " ~") // Also hadles majors
                .replace("sus", " ~") // Removed as conflicts with format 3 test for chord ending's'
                .replace ("b", " ~")
                .replace("#"," ~")
                // Remove multiple whitespace and trim
                .replaceAll("\\s{2,}", " ").trim();

        // Check the chord format of the the song.  Go through the chord lines and look for clues
        for (String line : mLyrics.split("¬")) {
            if (line.startsWith(".")) {
                //  Split into individual chords
                String[] chordsInLine = line.substring(1).split(" ");
                String chordInLineLC;
                // Now go through each chord and add to the matching format
                // Case is needed as lowercase chords denotes minor chords for format 3
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
        if      (contains_do)                   StaticVariables.detectedChordFormat = 4;
        else if (contains_H && !contains_es_is) StaticVariables.detectedChordFormat = 2;
        else if (contains_H || contains_es_is)  StaticVariables.detectedChordFormat = 3;
        else if (contains_nash)                 StaticVariables.detectedChordFormat = 5;
        else if (contains_nashnumeral)          StaticVariables.detectedChordFormat = 6;
        else                                    StaticVariables.detectedChordFormat = 1;
        // We set the newChordFormat default to the same as detected
        StaticVariables.newChordFormat = StaticVariables.detectedChordFormat;
    }

    void writeImprovedXML (Context c, Preferences preferences) {
        // Write the improved XML file
        StaticVariables.mLyrics = StaticVariables.transposedLyrics;

        PopUpEditSongFragment.prepareSongXML();
        PopUpEditSongFragment.justSaveSongXML(c, preferences);

        StaticVariables.transposedLyrics = "";
        FullscreenActivity.myParsedLyrics = null;
        FullscreenActivity.myLyrics = "";
        FullscreenActivity.mynewXML = "";
        FullscreenActivity.myXML = "";
    }
}